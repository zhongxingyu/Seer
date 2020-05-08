 package org.openmrs.module.amrsmobileforms;
 
 import java.io.File;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.amrsmobileforms.util.MobileFormEntryUtil;
 import org.openmrs.module.amrsmobileforms.util.RelationshipBuider;
 import org.openmrs.module.amrsmobileforms.util.RelationshipCodes;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Processes Successfully processed patient forms.
  *
  * It is a temporary solution since most of what is happening here should be
  * done by xforms This processor will accomplish the following tasks (a), Add
  * all HCT IDs as Secondary IDs (b), Insert if available, Economic Survey Id
  * (c), Create relationships between household persons (d), Add Contact Phone
  * Person Attribute
  *
  *
  * @author Samuel Mbugua
  *
  */
 @Transactional
 public class MobileFormPostProcessor {
 
 	private static final Log log = LogFactory.getLog(MobileFormPostProcessor.class);
 	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	private DocumentBuilder docBuilder;
 	private XPathFactory xPathFactory;
 	private MobileFormEntryService mobileService;
 	
 	// allow only one running instance
 	private static Boolean isRunning = false;
 
 	/**
 	 * Default Constructor
 	 */
 	public MobileFormPostProcessor() {
 		try {
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 			this.getMobileService();
 		} catch (Exception e) {
 			log.error("Problem occurred while creating document builder", e);
 		}
 	}
 
 	/**
 	 * Process an existing entries in the mobile form post-process queue
 	 *
 	 * @param filePath
 	 * @param queue
 	 * @throws APIException
 	 */
 	private void processPostProcessForm(String filePath, MobileFormQueue queue) throws APIException {
 		log.debug("Performing post process, This adds relationships and person attributes");
 		boolean canArchive = false;
 		try {
 			String formData = queue.getFormData();
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 			XPathFactory xpf = getXPathFactory();
 			XPath xp = xpf.newXPath();
 			Document doc = docBuilder.parse(IOUtils.toInputStream(formData));
 
 			Node curNode = (Node) xp.evaluate(MobileFormEntryConstants.PATIENT_NODE, doc, XPathConstants.NODE);
 			String patientIdentifier = xp.evaluate(MobileFormEntryConstants.PATIENT_IDENTIFIER, curNode);
 			String hctID = xp.evaluate(MobileFormEntryConstants.PATIENT_HCT_IDENTIFIER, curNode);
 			String householdIdentifier = xp.evaluate(MobileFormEntryConstants.PATIENT_HOUSEHOLD_IDENTIFIER, curNode);
 			String phoneNumber = xp.evaluate(MobileFormEntryConstants.PATIENT_PHONE, curNode);
 
 			curNode = (Node) xp.evaluate(MobileFormEntryConstants.OBS_NODE, doc, XPathConstants.NODE);
 			String relationshipToHead = xp.evaluate(MobileFormEntryConstants.OBS_RELATIONSHIP, curNode);
 
 			Patient pat;
 
 			// First Ensure there is at least a patient identifier in the form
 			if (!StringUtils.hasText(MobileFormEntryUtil.getPatientIdentifier(doc))) {
 				// form has no patient identifier : log an error
 				log.debug("Patient has no identifier, or the identifier provided is invalid");
 				return;
 			} else {
 				//create the patient
 				pat = MobileFormEntryUtil.getPatient(patientIdentifier);
 			}
 
 			//Check ID and add new HCT ID
 			if (StringUtils.hasText(hctID)) {
 				try {
 					PatientIdentifierType patIdType = Context.getPatientService().getPatientIdentifierType(
 							MobileFormEntryConstants.DEFAULT_HCT_IDENTIFIER_TYPE);
 					Location loc = Context.getLocationService().getLocation(
 							MobileFormEntryConstants.DEFAULT_HCT_IDENTIFIER_LOCATION);
 					PatientIdentifier iden = new PatientIdentifier(hctID, patIdType, loc);
 					pat.addIdentifier(iden);
 				} catch(Exception ex) {
 					log.warn("could not add HCT patient identifier.", ex);
 				}
 			}
 			
 			//Check Phone number and add it
 			if (StringUtils.hasText(phoneNumber)) {
 				try {
 					PersonAttributeType perAttType = Context.getPersonService().getPersonAttributeType(
 							MobileFormEntryConstants.DEFAULT_PHONENUMBER_ATTRIBUTE_TYPE);
 					PersonAttribute personAttribute = new PersonAttribute(perAttType, phoneNumber);
 					pat.addAttribute(personAttribute);
 				} catch(Exception ex) {
 					log.warn("could not add phone number as person attribute.", ex);
 				}
 			}
 			
 			// save the patient
 			Context.getPersonService().savePerson(pat);
 
 			// add this person as a member of its household in Household Module data model
 			HouseholdModuleConverter.getInstance().addMembership(pat, householdIdentifier, 
 					OpenmrsUtil.nullSafeEquals(relationshipToHead, RelationshipCodes.SELF));
			
 			//For this person attempt to create a relationship.
 			if (StringUtils.hasText(relationshipToHead) && StringUtils.hasText(householdIdentifier)) {
 				canArchive = RelationshipBuider.createRelationship(pat, relationshipToHead, householdIdentifier);
 			}
 		} catch (Throwable t) {
 			log.error("Error Post Processing", t);
 			canArchive = true;
 		}
 
 		//put form in archive if ready to archive - actually delete it
 		if (canArchive) {
 			saveFormInArchive(filePath);
 		}
 	}
 
 	/**
 	 * Processes each post process entry. If there are no pending items in the
 	 * queue, this method simply returns quietly.
 	 */
 	public void processPostProcessQueue() {
 		synchronized (isRunning) {
 			if (isRunning) {
 				log.warn("MobileForms Post processor aborting (another processor already running)");
 				return;
 			}
 			isRunning = true;
 		}
 
 		try {
 			File postProcessQueueDir = MobileFormEntryUtil.getMobileFormsPostProcessDir();
 			for (File file : postProcessQueueDir.listFiles()) {
 				MobileFormQueue queue = mobileService.getMobileFormEntryQueue(file.getAbsolutePath());
 				processPostProcessForm(file.getAbsolutePath(), queue);
 			}
 		} catch (Exception e) {
 			log.error("Problem occured while processing post-process queue", e);
 		} finally {
 			isRunning = false;
 		}
 	}
 
 	/**
 	 * Archives a mobile form after successful processing
 	 */
 	private void saveFormInArchive(String formPath) {
 		//Since xforms has already archived this file delete this copy
 		MobileFormEntryUtil.deleteFile(formPath);
 	}
 
 	/**
 	 * @return XPathFactory to be used for obtaining data from the parsed XML
 	 */
 	private XPathFactory getXPathFactory() {
 		if (xPathFactory == null) {
 			xPathFactory = XPathFactory.newInstance();
 		}
 		return xPathFactory;
 	}
 
 	/**
 	 * @return MobileFormEntryService to be used by the process
 	 */
 	private MobileFormEntryService getMobileService() {
 		if (mobileService == null) {
 			try {
 				mobileService = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
 			} catch (APIException e) {
 				log.debug("MobileFormEntryService not found", e);
 				return null;
 			}
 		}
 		return mobileService;
 	}
 }

 package org.openmrs.module.remoteformentry;
 
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.api.APIException;
 import org.openmrs.api.FormService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.formentry.FormEntryError;
 import org.openmrs.module.formentry.FormEntryService;
 import org.springframework.transaction.annotation.Transactional;
 import org.w3c.dom.Document;
 
 /**
  * Processes RemoteFormEntryPendingQueue entries. This should only be turned
  * on for the central server.  Each entry is attempted to be matched to an 
  * existing patient.  
  * 1a) If that patient is found, new items in the patient header is copied 
  *     to the patient object.  
  * 1b) If the patient doesn't exist and the form qualifies for a created 
  *     patient (is an initial form) than a new patient is created.
  * 1c) If the patient isn't found and this is a non-initial form, copy to the
  *     formentry_error queue
  *  2) The entry is copied to the formentry_queue for normal processing
  * 
  * @see org.openmrs.module.remoteformentry.ProcessPendingQueueItemsTask
  */
 @Transactional
 public class RemoteFormEntryPendingProcessor{
 
 	private static final Log log = LogFactory
 	.getLog(RemoteFormEntryPendingProcessor.class);
 
 	private DocumentBuilderFactory documentBuilderFactory;
 	private XPathFactory xPathFactory;
 	private static Boolean isRunning = false; // allow only one running
 
 	/**
 	 * Empty constructor (requires context to be set before any other calls are
 	 * made)
 	 */
 	public RemoteFormEntryPendingProcessor() {
 	}
 
 	/**
 	 * Process this pending queue item.
 	 * 
 	 * Create the patient if they are not found and this is an initial encounter
 	 * 
 	 * Add the patient details if the patient is found
 	 * 
 	 * Throw a fatal error if the patient is not found and this is not an initial encounter
 	 * 
 	 * @param pendingQueue
 	 *            entry to be transformed
 	 */
 	public void processRemoteFormEntryPendingQueue(RemoteFormEntryPendingQueue pendingQueue) {
 		log.debug("Transforming form entry queue");
 		
 		RemoteFormEntryService remoteFormEntryService = (RemoteFormEntryService)Context.getService(RemoteFormEntryService.class);
 		PatientService patientService = Context.getPatientService();
 		FormService formService = Context.getFormService();
 		
 		String formData = pendingQueue.getFormData();
 		Integer formId = null;
 		String errorDetails = null;
 		List<PatientIdentifier> identifiers = null;
 		
 		// First we parse the FormEntry xml data to obtain the formId of the
 		// form that was used to create the xml data
 		try {
 			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			XPathFactory xpf = getXPathFactory();
 			XPath xp = xpf.newXPath();
 			Document doc = db.parse(IOUtils.toInputStream(formData));
 			formId = Integer.parseInt(xp.evaluate("/form/@id", doc));
 			
 			// try to get the form
 			Form form = formService.getForm(formId);
 			if (form == null)
 				throw new RemoteFormEntryException("Error retrieving form id from data");
 			
 			// try to get the patient id
 			//String pId = xp.evaluate("/form/patient/patient.patient_id", doc);
 			//if (pId == null)
 			//	throw new RemoteFormEntryException("Patient's id was not found in data in /form/patient/patient.patient_id");
 			//Integer patientId = Integer.valueOf(pId);
 			
 			// try to get the patient identifier
 			
 			identifiers = RemoteFormEntryUtil.getPatientIdentifiers(doc, xp, null);
 			
 			if (identifiers.size() < 1)
 				throw new RemoteFormEntryException("Unable to find any patient identifiers");
 			
 			// try to find this patient by identifier
 			Patient patient = null;
 			int i = 0;
 			while (patient == null && i < identifiers.size()) {
 				PatientIdentifier currentIdentifier = identifiers.get(i++);
 				String identifierString = currentIdentifier.getIdentifier();
 				Integer identifierTypeId = currentIdentifier.getIdentifierType().getPatientIdentifierTypeId();
 				
 				// search the database for all patients with this identifier string
				List<Patient> patients = patientService.getPatients(null, identifierString, null, true);
 				
 				for (Patient p : patients) {
 					// loop over this patient's identifiers to make sure they have this 
 					// one _and_ this type.  if they do, great, its them.  if not, keep trying 
 					for (PatientIdentifier identifier : p.getIdentifiers()) {
 						if (identifier.getIdentifier().equals(identifierString) && 
 							identifierTypeId.equals(identifier.getIdentifierType().getPatientIdentifierTypeId())) {
 							patient = p;
 							break;
 						}
 					}
 				}
 			}
 			
 			// if the patient wasn't found...
 			if (patient == null) {
 				if (log.isDebugEnabled())
 					log.debug("patient is null for pendingQueue: " + pendingQueue.getRemoteFormEntryPendingQueueId());
 				
 				// this patient has yet to be defined.  Do we need to create it?
 				List<EncounterType> initialEncounterTypes = remoteFormEntryService.getInitialEncounterTypes();
 				
 				if (initialEncounterTypes.contains(form.getEncounterType())) {
 					// the patient doesn't exist and this is an initial form.  Go to town on the patient. 
 					patient = remoteFormEntryService.createPatientInDatabase(doc, xp);
 				}
 				else {
 					// The patient doesn't exist and this is not an initial form.  Throw an error
 					// to get this put in the formentry archive
 					StringBuilder errorMessage = new StringBuilder("This form's encounter type is not set as an initial encounter and no patient was found with identifiers: ");
 					for (PatientIdentifier identifier : identifiers) {
 						errorMessage.append(identifier.getIdentifier());
 						errorMessage.append(":");
 						errorMessage.append(identifier.getIdentifierType());
 						errorMessage.append(", ");
 					}
 					
 					throw new RemoteFormEntryException(errorMessage.toString());
 				}
 				
 			}
 			else {
 				if (log.isDebugEnabled())
 					log.debug("patientid is " + patient + " for pendingQueue: " + pendingQueue.getRemoteFormEntryPendingQueueId());
 				
 				// patient exists, update their information from the data on the form
 				remoteFormEntryService.updatePatientInDatabase(patient, doc, xp);
 			}
 			
 			//assign the patient to this form
 			remoteFormEntryService.createFormEntryQueueForPatient(formData, patient);
 			
 			// remove the now useless pending queue item
 			remoteFormEntryService.deleteRemoteFormEntryPendingQueue(pendingQueue);
 			
 		} 
 		catch (Throwable t) {
 			errorDetails = RemoteFormEntryUtil.join(t.getStackTrace(), "\n");
 			setFatalError(pendingQueue, t.getMessage(),
 			              errorDetails);
 			
 			log.error("Error while parsing remoteformentry pending queue ("
 			          + pendingQueue.getRemoteFormEntryPendingQueueId() + ")", t);
 		}
 	}
 
 	/**
 	 * Transform the next pending RemoteFormEntryPendingQueue entry. If there are no pending
 	 * items in the queue, this method simply returns quietly.
 	 * 
 	 * @return true if a queue entry was processed, false if queue was empty
 	 */
 	public boolean processNextRemoteFormEntryPendingQueue() {
 		boolean transformOccurred = false;
 		RemoteFormEntryService remoteService = null; 
 		try {
 			remoteService = (RemoteFormEntryService)Context.getService(RemoteFormEntryService.class);
 		}
 		catch (APIException e) {
 			log.debug("RemoteFormEntryService not found");
 			return false;
 		}
 		
 		RemoteFormEntryPendingQueue rfeq;
 		if ((rfeq = remoteService.getNextRemoteFormEntryPendingQueue()) != null) {
 			processRemoteFormEntryPendingQueue(rfeq);
 			transformOccurred = true;
 		}
 		
 		return transformOccurred;
 	}
 
 	/**
 	 * @return DocumentBuilderFactory to be used for parsing XML
 	 */
 	private DocumentBuilderFactory getDocumentBuilderFactory() {
 		if (documentBuilderFactory == null)
 			documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		return documentBuilderFactory;
 	}
 
 	/**
 	 * @return XPathFactory to be used for obtaining data from the parsed XML
 	 */
 	private XPathFactory getXPathFactory() {
 		if (xPathFactory == null)
 			xPathFactory = XPathFactory.newInstance();
 		return xPathFactory;
 	}
 
 	/**
 	 * Convenience method to handle fatal errors. In this case, a FormEntryError
 	 * object is built and stored based on the given queue entry and then the
 	 * given queue entry is removed from the queue.
 	 * 
 	 * @param pendingQueue
 	 *            queue entry with fatal error
 	 * @param error
 	 *            name and/or brief description of the error
 	 * @param errorDetails
 	 *            specifics for the fatal error
 	 */
 	private void setFatalError(RemoteFormEntryPendingQueue pendingQueue, String error,
 			String errorDetails) {
 
 		// set up the error queue item
 		FormEntryError formEntryError = new FormEntryError();
 		formEntryError.setFormData(pendingQueue.getFormData());
 		
 		String classString = RemoteFormEntryException.class.getName();
 		formEntryError.setError(classString + ": " + error);
 		formEntryError.setErrorDetails(errorDetails);
 
 		// create the error queue item
 		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
 		formEntryService.createFormEntryError(formEntryError);
 
 		// delete the queue item that was just moved to the error queue
 		RemoteFormEntryService remoteFormEntryService = (RemoteFormEntryService)Context.getService(RemoteFormEntryService.class);
 		remoteFormEntryService.deleteRemoteFormEntryPendingQueue(pendingQueue);
 	}
 
 	/**
 	 * Starts up a thread to process all existing RemoteFormEntryPendingQueue entries
 	 */
 	public void processRemoteFormEntryPendingQueue() throws APIException {
 		synchronized (isRunning) {
 			if (isRunning) {
 				log.warn("Processor aborting (another processor already running)");
 				return;
 			}
 			isRunning = true;
 		}
 		try {
 			log.debug("Start processing RemoteFormEntry pending queue");
 			while (processNextRemoteFormEntryPendingQueue()) {
 				// loop until queue is empty
 			}
 			log.debug("Done processing RemoteFormEntry pending queue");
 		}
 		finally {
 			isRunning = false;
 		}
 	}
 
 }

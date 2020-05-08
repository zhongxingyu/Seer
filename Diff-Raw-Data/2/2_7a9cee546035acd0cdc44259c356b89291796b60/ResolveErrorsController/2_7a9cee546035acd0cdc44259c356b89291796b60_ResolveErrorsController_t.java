 package org.openmrs.module.amrsmobileforms.web.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.servlet.http.HttpSession;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import org.apache.commons.lang.StringUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.amrsmobileforms.MobileFormEntryConstants;
 import org.openmrs.module.amrsmobileforms.MobileFormEntryError;
 import org.openmrs.module.amrsmobileforms.MobileFormEntryErrorModel;
 import org.openmrs.module.amrsmobileforms.MobileFormEntryService;
 import org.openmrs.module.amrsmobileforms.MobileFormQueue;
 import org.openmrs.module.amrsmobileforms.util.MobileFormEntryUtil;
 import org.openmrs.module.amrsmobileforms.util.XFormEditor;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Controller for Mobile errors resolution jsp pages
  *
  * @author Samuel Mbugua
  */
 @Controller
 public class ResolveErrorsController {
 
 	private static final Log log = LogFactory.getLog(ResolveErrorsController.class);
 
 	/**
 	 * Controller for Error list jsp page
 	 */
 	@RequestMapping(value = "/module/amrsmobileforms/resolveErrors")
 	public String showErrorList() {
 		return "/module/amrsmobileforms/resolveErrors";
 	}
 
 	/**
 	 * Controller for commentOnError jsp Page
 	 */
 	@ModelAttribute("errorFormComment")
 	@RequestMapping(value = "/module/amrsmobileforms/resolveErrorComment", method = RequestMethod.GET)
 	public List<MobileFormEntryErrorModel> populateCommentForm(@RequestParam Integer errorId) {
 		return getErrorObject(errorId);
 	}
 
 	/**
 	 * Controller for commentOnError post jsp Page
 	 */
 	@RequestMapping(value = "/module/amrsmobileforms/resolveErrorComment", method = RequestMethod.POST)
 	public String saveComment(HttpSession httpSession, @RequestParam Integer errorId, @RequestParam String comment) {
 		if (comment.trim().length() > 0) {
 			MobileFormEntryService mfs = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
 			MobileFormEntryError error = mfs.getErrorById(errorId);
 			error.setComment(comment);
 			error.setCommentedBy(Context.getAuthenticatedUser());
 			error.setDateCommented(new Date());
 			mfs.saveErrorInDatabase(error);
 		} else {
 			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Invalid Comment");
 		}
 		return "redirect:resolveErrors.list";
 	}
 
 	/**
 	 * Controller for resolveError jsp Page
 	 */
 	@ModelAttribute("errorFormResolve")
 	@RequestMapping(value = "/module/amrsmobileforms/resolveError", method = RequestMethod.GET)
 	public List<MobileFormEntryErrorModel> populateErrorForm(@RequestParam Integer errorId) {
 		return getErrorObject(errorId);
 	}
 
 	/**
 	 * Controller for resolveError post jsp Page
 	 */
 	@RequestMapping(value = "/module/amrsmobileforms/resolveError", method = RequestMethod.POST)
 	public String resolveError(HttpSession httpSession, @RequestParam("householdId") String householdId,
 		@RequestParam("errorId") Integer errorId, @RequestParam("errorItemAction") String errorItemAction,
 		@RequestParam("birthDate") String birthDate, @RequestParam("patientIdentifier") String patientIdentifier,
 		@RequestParam("providerId") String providerId, @RequestParam("householdIdentifier") String householdIdentifier) {
 		MobileFormEntryService mobileService;
 		String filePath;
 		
		log.debug("Error ID is "+errorId);
 
 		// user must be authenticated (avoids authentication errors)
 		if (Context.isAuthenticated()) {
 			if (!Context.getAuthenticatedUser().hasPrivilege(
 				MobileFormEntryConstants.PRIV_RESOLVE_MOBILE_FORM_ENTRY_ERROR)) {
 				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "amrsmobileforms.action.noRights");
 				return "redirect:resolveErrors.list";
 			}
 
 			mobileService = Context.getService(MobileFormEntryService.class);
 
 			// fetch the MobileFormEntryError item from the database
 			MobileFormEntryError errorItem = mobileService.getErrorById(errorId);
 			filePath = MobileFormEntryUtil.getMobileFormsErrorDir().getAbsolutePath() + errorItem.getFormName();
 			if ("linkHousehold".equals(errorItemAction)) {
 				if (mobileService.getHousehold(householdId) == null) {
 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "amrsmobileforms.resolveErrors.action.createLink.error");
 					return "redirect:resolveErrors.list";
 				} else {
 					if (XFormEditor.editNode(filePath,
 						MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_HOUSEHOLD_IDENTIFIER, householdId)) {
 						// put form in queue for normal processing
 						moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), errorItem);
 					}
 				}
 			} else if ("assignBirthdate".equals(errorItemAction)) {
 				// format provided birthdate and insert into patient data like so:
 				// <patient.birthdate openmrs_table="patient" openmrs_attribute="birthdate">2009-12-25</patient.birthdate>
 				if (StringUtils.isNotEmpty(birthDate)) {
 					DateFormat reader = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
 					DateFormat writer = new SimpleDateFormat("yyyy-MM-dd");
 					try {
 						String formattedDate = writer.format(reader.parse(birthDate));
 						if (XFormEditor.editNode(filePath,
 							MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_BIRTHDATE, formattedDate)) {
 							// put form in queue for normal processing
 							moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), errorItem);
 						}
 					} catch (ParseException e) {
 						String error = "Birthdate was not assigned, Invalid date entered: " + birthDate;
 						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
 						log.error(error, e);
 						return "redirect:resolveErrors.list";
 					}
 				} else {
 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Birthdate was not assigned, Null object entered");
 					return "redirect:resolveErrors.list";
 				}
 			} else if ("newIdentifier".equals(errorItemAction)) {
 				if (patientIdentifier != null && patientIdentifier.trim() != "") {
 					if (reverseNodes(filePath, patientIdentifier)) {
 						// put form in queue for normal processing
 						moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), errorItem);
 					}
 				} else {
 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "amrsmobileforms.resolveErrors.action.newIdentifier.error");
 					return "redirect:resolveErrors.list";
 				}
 			} else if ("linkProvider".equals(errorItemAction)) {
 				if (providerId != null && providerId.trim() != "") {
 					providerId = Context.getUserService().getUser(Integer.parseInt(providerId)).getSystemId();
 					if (XFormEditor.editNode(filePath,
 						MobileFormEntryConstants.ENCOUNTER_NODE + "/" + MobileFormEntryConstants.ENCOUNTER_PROVIDER, providerId)) {
 						// put form in queue for normal processing
 						moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), errorItem);
 					}
 				} else {
 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "(Null) Invalid provider ID");
 					return "redirect:resolveErrors.list";
 				}
 			} else if ("createPatient".equals(errorItemAction)) {
 				// put form in queue for normal processing
 				moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), errorItem);
 			} else if ("deleteError".equals(errorItemAction)) {
 				// delete the mobileformentry error queue item
 				mobileService.deleteError(errorItem);
 				//and delete from the file system
 				MobileFormEntryUtil.deleteFile(filePath);
 
 			} else if ("deleteComment".equals(errorItemAction)) {
 				//set comment to null and save
 				errorItem.setComment(null);
 				mobileService.saveErrorInDatabase(errorItem);
 			} else if ("newHousehold".equals(errorItemAction)) {
 				if (householdIdentifier != null && householdIdentifier.trim() != "") {
 					// first change household id
 					if (XFormEditor.editNode(filePath,
 						MobileFormEntryConstants.HOUSEHOLD_PREFIX + MobileFormEntryConstants.HOUSEHOLD_META_PREFIX + "/"
 						+ MobileFormEntryConstants.HOUSEHOLD_META_HOUSEHOLD_ID, householdIdentifier)) {
 					} else {
 						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error assigning new household identififer");
 						return "redirect:resolveErrors.list";
 					}
 
 					// then change all patient household pointers
 					if (XFormEditor.editNodeList(filePath,
 						MobileFormEntryConstants.HOUSEHOLD_PREFIX + MobileFormEntryConstants.HOUSEHOLD_INDIVIDUALS_PREFIX,
 						"patient/" + MobileFormEntryConstants.PATIENT_HOUSEHOLD_IDENTIFIER, householdIdentifier)) {
 						// drop form in queue for normal processing
 						moveAndDeleteError(MobileFormEntryUtil.getMobileFormsDropDir().getAbsolutePath(), errorItem);
 					} else {
 						httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error assigning new household identififer");
 						return "redirect:resolveErrors.list";
 					}
 				} else {
 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error assigning new household identififer");
 					return "redirect:resolveErrors.list";
 				}
 			} else if ("noChange".equals(errorItemAction)) {
 				// do nothing here
 			} else {
 				throw new APIException("Invalid action selected for: " + errorId);
 			}
 		}
 
 		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "amrsmobileforms.resolveErrors.action.success");
 		return "redirect:resolveErrors.list";
 	}
 
 	/**
 	 * Given an id, this method creates an error model
 	 *
 	 * @param errorId
 	 * @return List of errors
 	 */
 	private static List<MobileFormEntryErrorModel> getErrorObject(Integer errorId) {
 		MobileFormEntryService mfs = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
 		List<MobileFormEntryErrorModel> list = new Vector<MobileFormEntryErrorModel>();
 		MobileFormEntryError error = mfs.getErrorById(errorId);
 		if (error != null) {
 			String formName = error.getFormName();
 			String filePath = getAbsoluteFilePath(formName, mfs);
 			error.setFormName(createFormData(error.getFormName(), mfs));
 			MobileFormEntryErrorModel errorForm = new MobileFormEntryErrorModel(error, getFormType(formName));
 			errorForm.setFormPath(filePath);
 			list.add(errorForm);
 		}
 		return list;
 	}
 
 	private static String getFormType(String formName) {
 		if (StringUtils.isEmpty(formName)) {
 			return null;
 		}
 		// TODO make this more secure ... not all forms will have "HCT" in the name.
 		if (formName.contains("HCT")) {
 			return "household";
 		}
 		return "patient";
 	}
 
 	/**
 	 * Converts an xml file specified by <b>formPath</b> to a string
 	 *
 	 * @param formName
 	 * @param mfs
 	 * @return String representation of the file
 	 */
 	private static String createFormData(String formName, MobileFormEntryService mfs) {
 
 		MobileFormQueue queue = mfs.getMobileFormEntryQueue(MobileFormEntryUtil.getMobileFormsErrorDir().getAbsolutePath()
 			+ formName);
 		return queue.getFormData();
 	}
 
 	/**
 	 * Takes in Mobile Queue and returns an absolute Path
 	 *
 	 * @param formName
 	 * @param mfs
 	 * @return String absolute path of the file
 	 */
 	private static String getAbsoluteFilePath(String formName, MobileFormEntryService mfs) {
 
 		MobileFormQueue queue = mfs.getMobileFormEntryQueue(MobileFormEntryUtil.getMobileFormsErrorDir().getAbsolutePath()
 			+ formName);
 		return queue.getFileSystemUrl();
 	}
 
 	/**
 	 * Stores a form in a specified folder
 	 */
 	private static void saveForm(String oldFormPath, String newFormPath) {
 		try {
 			if (oldFormPath != null) {
 				File file = new File(oldFormPath);
 
 				//move the file to specified new directory
 				file.renameTo(new File(newFormPath));
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 * Reverses patient Identifier nodes after for a form with more than one
 	 *
 	 * @param filePath
 	 * @param patientIdentifier
 	 * @return
 	 */
 	private static boolean reverseNodes(String filePath, String patientIdentifier) {
 		try {
 
 			File file = new File(filePath);
 
 			// Create instance of DocumentBuilderFactory
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = factory.newDocumentBuilder();
 
 			// Using existing XML Document
 			Document doc = docBuilder.parse(file);
 			XPathFactory xpf = XPathFactory.newInstance();
 			XPath xp = xpf.newXPath();
 
 			Node curNode = (Node) xp.evaluate(MobileFormEntryConstants.PATIENT_NODE, doc, XPathConstants.NODE);
 			String patientAmpathIdentifier = xp.evaluate(MobileFormEntryConstants.PATIENT_HCT_IDENTIFIER, curNode);
 
 			// If patient has an AMPATH ID we use it to create the patient
 			if (patientAmpathIdentifier != null && patientAmpathIdentifier != "") {
 				XFormEditor.editNode(filePath,
 					MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_IDENTIFIER, patientAmpathIdentifier);
 				XFormEditor.editNode(filePath,
 					MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_IDENTIFIER_TYPE, "3");
 				XFormEditor.editNode(filePath,
 					MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_HCT_IDENTIFIER, patientIdentifier);
 			} else {
 				//Patient has only one id
 				XFormEditor.editNode(filePath,
 					MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_IDENTIFIER, patientIdentifier);
 				XFormEditor.editNode(filePath,
 					MobileFormEntryConstants.PATIENT_NODE + "/" + MobileFormEntryConstants.PATIENT_IDENTIFIER_TYPE, "8");
 			}
 		} catch (Throwable t) {
 			log.error("Error reversing nodes", t);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Controller for reprocessing errors from list page
 	 */
 	@RequestMapping(value = "/module/amrsmobileforms/reprocessBatch")
 	public String reprocessBatch(
 		@RequestParam(value="errorIds", required=false) List<Integer> errorIds,
 		@RequestParam(value="all", required=false) Boolean all,
 		@RequestParam(value="query", required=false) String query,
 		HttpSession httpSession) {
 
 		// give up quickly if nothing is specified
 		if (errorIds == null && all == null && query == null) {
 			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Nothing specified for reprocessing.");
 			return "redirect:resolveErrors.list";
 		}
 
 		List<MobileFormEntryError> errors;
 		MobileFormEntryService mfs = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
 		// look first at the "all" indicator
 		if (all != null) {
 			// build list from all matching query (ignoring selected ids)
 			errors = mfs.getErrorBatch(0, null, query);
 		} else {
 			// build list from ids
 			errors = new ArrayList<MobileFormEntryError>();
 			for(Integer id: errorIds) {
 				errors.add(mfs.getErrorById(id));
 			}
 		}
 
 		// reprocess each error -- placing into initial drop directory for now
 		// TODO see if there's some way to decide where this file should go if not always the drop dir
 		Integer countHouseholds = 0;
 		Integer countPatients = 0;
 		for (MobileFormEntryError error: errors) {
 			if (OpenmrsUtil.nullSafeEquals(getFormType(error.getFormName()), "household")) {
 				moveAndDeleteError(MobileFormEntryUtil.getMobileFormsDropDir().getAbsolutePath(), error);
 				countHouseholds++;
 			} else {
 				moveAndDeleteError(MobileFormEntryUtil.getMobileFormsQueueDir().getAbsolutePath(), error);
 				countPatients++;
 			}
 		}
 
 		// send a message back
 		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, 
 			"Sent " + errors.size() + " form" + (errors.size() == 1 ? "" : "s")
 				+ " back for reprocessing: "
 				+ countHouseholds + " household" + (countHouseholds == 1 ? "" : "s") + ", "
 				+ countPatients + " patient" + (countPatients == 1 ? "" : "s") + ".");
 		return "redirect:resolveErrors.list";
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private MobileFormEntryService getMobileFormEntryService(){
 		return Context.getService(MobileFormEntryService.class);
 	}
 
 	/**
 	 *
 	 * @param destination
 	 * @param error
 	 */
 	private void moveAndDeleteError(String destination, MobileFormEntryError error) {
 		// find error location
 		String filePath = MobileFormEntryUtil.getMobileFormsErrorDir().getAbsolutePath() + error.getFormName();
 		// put form in queue for normal processing
 		saveForm(filePath, destination + error.getFormName());
 		// delete the mobileformentry error queue item
 		getMobileFormEntryService().deleteError(error);
 	}
 
 	/**
 	 * return a batch of errors according to request
 	 *
 	 * @param iDisplayStart start index for search
 	 * @param iDisplayLength amount of terms to return
 	 * @param sSearch search term(s)
 	 * @param sEcho check digit for datatables
 	 * @return batch of error objects to be converted to JSON
 	 * @throws IOException
 	 */
 	@RequestMapping("/module/amrsmobileforms/errorList.json")
 	public @ResponseBody
 	Map<String, Object> getErrorBatchAsJson(
 		@RequestParam("iDisplayStart") int iDisplayStart,
 		@RequestParam("iDisplayLength") int iDisplayLength,
 		@RequestParam("sSearch") String sSearch,
 		@RequestParam("sEcho") int sEcho) throws IOException {
 
 		// get the data
 		MobileFormEntryService mfs = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
 
 		List<MobileFormEntryError> errors = mfs.getErrorBatch(iDisplayStart, iDisplayLength, sSearch);
 
 		// form the results dataset
 		List<Object> results = new ArrayList<Object>();
 		for (MobileFormEntryError error : errors) {
 			results.add(generateObjectMap(error));
 		}
 
 		// build the response
 		Map<String, Object> response = new HashMap<String, Object>();
 		response.put("iTotalRecords", mfs.countErrors(null));
 		response.put("iTotalDisplayRecords", mfs.countErrors(sSearch));
 		response.put("sEcho", sEcho);
 		response.put("aaData", results.toArray());
 
 		// send it
 		return response;
 	}
 
 	/**
 	 * create an object array for a given error
 	 *
 	 * @param error MobileFormEntryError object
 	 * @return object array for use with datatables
 	 */
 	private Map<String, Object> generateObjectMap(MobileFormEntryError error) {
 		// try to stick to basic types; String, Integer, etc (not Date)
 		// JSP expects: [id, error, details, form name, comment]
 		Map<String, Object> result = new HashMap<String, Object>();
 		result.put("id", error.getId());
 		result.put("error", error.getError());
 		result.put("errorDetails", error.getErrorDetails());
 		result.put("formName", error.getFormName());
 		result.put("comment", error.getComment());
 		return result;
 	}
 
 }

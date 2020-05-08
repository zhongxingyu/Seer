 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.openmrs.module.amrsmobileforms.web;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.amrsmobileforms.*;
 import org.openmrs.module.amrsmobileforms.util.MobileFormEntryUtil;
 
 import java.util.List;
 import java.util.Vector;
import org.openmrs.module.amrsmobileforms.MobileFormEntryService;
import java.util.Date;
 /**
  *
  * @author jkeiper
  */
 public class DWRAMRSMobileFormsService {
 	public EconomicConceptMap saveEconomicConceptMap(Integer id, Integer economicId, Integer conceptId) {
 		MobileFormEntryService service = Context.getService(MobileFormEntryService.class);
 
 		EconomicConceptMap ecm;
 		
 		if (id == null) {
 			ecm = new EconomicConceptMap();
 		} else {
 			ecm = service.getEconomicConceptMap(id);
 		}
 		
 		ecm.setEconomic(service.getEconomicObjectById(economicId));
 		ecm.setConcept(Context.getConceptService().getConcept(conceptId));
 
 		return service.saveEconomicConceptMap(ecm);
 	}
 
     ///////////////////////////////////////////////////////////
 
     public List<MobileFormEntryErrorModel> populateCommentForm(Integer errorId) {
         return getErrorObject(errorId);
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
	 * Controller for commentOnError post jsp Page
	 */
    public String saveComment(Integer errorId, String comment) {
		if (comment.trim().length() > 0) {
			MobileFormEntryService mfs = (MobileFormEntryService) Context.getService(MobileFormEntryService.class);
			MobileFormEntryError error = mfs.getErrorById(errorId);
			error.setComment(comment);
			error.setCommentedBy(Context.getAuthenticatedUser());
			error.setDateCommented(new Date());
			mfs.saveErrorInDatabase(error);
			return "Comment saved successfully";
		} else {
			
			return "A null comment was encountered";
		}
		
	}
 }

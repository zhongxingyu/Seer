 /**
  * 
  * @author pandyas
  * 
 * $Id: HistopathologyAction.java,v 1.19 2008-08-14 16:53:04 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.18  2007/09/12 19:36:40  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.17  2007/07/23 17:40:43  pandyas
  * Fixed typo in word occurred
  *
  * Revision 1.16  2007/06/13 19:39:38  pandyas
  * Modified code for EVS trees after formal testing
  *
  * Revision 1.15  2007/05/17 18:43:23  pandyas
  * Added printouts for more attributes
  *
  * Revision 1.14  2007/04/30 20:10:17  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  *
  * Revision 1.13  2006/10/23 16:51:56  pandyas
  * added printout of units for debug
  *
  * Revision 1.12  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.11  2006/04/20 19:19:43  pandyas
  * Moved save Assoc Met from AnimalModel to the Histopathology
  *
  * Revision 1.10  2006/04/17 19:09:41  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.9  2005/11/09 00:17:26  georgeda
  * Fixed delete w/ constraints
  *
  * Revision 1.8  2005/11/07 19:14:14  pandyas
  * modified for clinical marker screen
  *
  * Revision 1.7  2005/11/07 13:57:16  georgeda
  * Minor tweaks
  *
  * Revision 1.6  2005/11/04 14:44:25  georgeda
  * Cleaned up histopathology/assoc metastasis
  *
  * Revision 1.5  2005/11/03 22:29:12  georgeda
  * Added validation/delete capabilities to histo screens
  *
  * Revision 1.4  2005/11/03 21:48:07  georgeda
  * Cleanup
  *
  * Revision 1.3  2005/11/03 18:54:10  pandyas
  * Modified for histopathology screens
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Histopathology;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.HistopathologyManager;
 import gov.nih.nci.camod.webapp.form.AssociatedMetastasisForm;
 import gov.nih.nci.camod.webapp.form.HistopathologyForm;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * HistopathologyAction Class
  */
 
 public class HistopathologyAction extends BaseAction {
 
     /**
      * Edit
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward editHistopathology(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
        log.debug("<HistopathologyAction> Entering 'edit' method");
 
         // Grab the current aHistopathID from the session
         String aHistopathologyID = request.getParameter("aHistopathologyID");
         
         // Grab the current modelID from the session
         String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);        
 
         // Create a form to edit
         HistopathologyForm histopathologyForm = (HistopathologyForm) form;
 
        log.debug("<HistopathologyAction edit> following Characteristics:" + "\n\t  HistopathID: " + aHistopathologyID
                 + "\n\t organ: " + histopathologyForm.getOrgan() 
                 + "\n\t organTissueName: " + histopathologyForm.getOrganTissueName() 
                 + "\n\t organTissueCode: " + histopathologyForm.getOrganTissueCode() 
                 + "\n\t DiagnosisName: " + histopathologyForm.getDiagnosisName() 
                 + "\n\t DiagnosisCode: " + histopathologyForm.getDiagnosisCode()                
                 + "\n\t TumorClassification: " + histopathologyForm.getTumorClassification()
                 + "\n\t OtherTumorClassification: " + histopathologyForm.getOtherTumorClassification()  
                 + "\n\t ageOfOnset: " + histopathologyForm.getAgeOfOnset() 
                 + "\n\t ageOfOnsetUnit: " + histopathologyForm.getAgeOfOnsetUnit()
                 + "\n\t ageOfDetection: " + histopathologyForm.getAgeOfDetection()
                 + "\n\t ageOfDetectionUnit: " + histopathologyForm.getAgeOfDetectionUnit()
                 + "\n\t weightOfTumor: " + histopathologyForm.getWeightOfTumor()
                 + "\n\t volumeOfTumor: " + histopathologyForm.getVolumeOfTumor() + "\n\t tumorIncidenceRate: "
                 + histopathologyForm.getTumorIncidenceRate() + "\n\t survivalInfo: "
                 + histopathologyForm.getMethodOfObservation() + "\n\t grossDescription: "
                 + histopathologyForm.getGrossDescription() + "\n\t microscopicDescription: "
                 + histopathologyForm.getMicroscopicDescription() + "\n\t observation: "
                 + histopathologyForm.getObservation() + "\n\t methodOfObservation: "
                 + histopathologyForm.getMethodOfObservation() + "\n\t comparativeData: "
                 + histopathologyForm.getComparativeData() + "\n\t comments: " + histopathologyForm.getComments()
                 + "\n\t theModelId: " + theModelId
                 + "\n\t aHistopathologyID: " + aHistopathologyID
                 + "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
         HistopathologyManager histopathologyManager = (HistopathologyManager) getBean("histopathologyManager");
         String theAction = (String) request.getParameter(Constants.Parameters.ACTION);
 
         AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);        
         
         try {
 
             if ("Delete".equals(theAction)) {
 
                 histopathologyManager.remove(aHistopathologyID, theAnimalModel);
 
                 ActionMessages msg = new ActionMessages();
                 msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("histopathology.delete.successful"));
                 saveErrors(request, msg);
 
             } else {
 
                 Histopathology theHistopathology = histopathologyManager.get(aHistopathologyID);
                 histopathologyManager.updateHistopathology(theAnimalModel, histopathologyForm, theHistopathology);
 
                 ActionMessages msg = new ActionMessages();
                 msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("histopathology.edit.successful"));
                 saveErrors(request, msg);
             }
         } catch (Exception e) {
 
             log.error("Unable to get a Histpathology action: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
 
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 
     /**
      * Edit
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward editMetastasis(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.debug("<HistopathologyAction> Entering 'editMetastasis' method");
 
         // Grab the current aHistopathID from the session
         String aHistopathologyID = request.getParameter("aHistopathologyID");
         
         // Grab the current modelID from the session
         String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);         
 
         // Grab the current aAssocMetastasisID we are working with related to
         // this animalModel
         String aAssociatedMetastasisID = request.getParameter("aAssociatedMetastasisID");
 
         // Create a form to edit
         AssociatedMetastasisForm assocMetastasisForm = (AssociatedMetastasisForm) form;
 
         log.debug("<HistopathologyAction editMetastasis> following Characteristics:" + "\n\t ParentHistopathID: "
                 + aHistopathologyID + "\n\t aAssociatedMetastasisID: " + aAssociatedMetastasisID
                 + "\n\t organ: " + assocMetastasisForm.getOrgan() 
                 + "\n\t organTissueName: " + assocMetastasisForm.getOrganTissueName()
                 + "\n\t organTissueCode: " + assocMetastasisForm.getOrganTissueCode() 
                 + "\n\t DiagnosisName: " + assocMetastasisForm.getDiagnosisName() 
                 + "\n\t DiagnosisCode: " + assocMetastasisForm.getDiagnosisCode()                 
                 + "\n\t TumorClassification: " + assocMetastasisForm.getTumorClassification()
                 + "\n\t OtherTumorClassification: " + assocMetastasisForm.getOtherTumorClassification() 
                 + assocMetastasisForm.getDiagnosisName() + "\n\t ageOfOnset: " + assocMetastasisForm.getAgeOfOnset()
                 + "\n\t ageOfDetection: " + assocMetastasisForm.getAgeOfDetection()
                 + "\n\t weightOfTumor: " + assocMetastasisForm.getWeightOfTumor() + "\n\t volumeOfTumor: "
                 + assocMetastasisForm.getVolumeOfTumor() + "\n\t tumorIncidenceRate: "
                 + assocMetastasisForm.getTumorIncidenceRate() + "\n\t survivalInfo: "
                 + assocMetastasisForm.getMethodOfObservation() + "\n\t grossDescription: "
                 + assocMetastasisForm.getGrossDescription() + "\n\t microscopicDescription: "
                 + assocMetastasisForm.getMicroscopicDescription() + "\n\t observation: "
                 + assocMetastasisForm.getObservation() + "\n\t methodOfObservation: "
                 + assocMetastasisForm.getMethodOfObservation() + "\n\t comparativeData: "
                 + assocMetastasisForm.getComparativeData() + "\n\t comments: " + assocMetastasisForm.getComments()
                 + "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
         HistopathologyManager histopathologyManager = (HistopathologyManager) getBean("histopathologyManager");
         String theAction = (String) request.getParameter(Constants.Parameters.ACTION);
         
         AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);        
 
         try {
 
             if ("Delete".equals(theAction)) {
 
                 Histopathology theHistopathology = histopathologyManager.get(aHistopathologyID);
                 histopathologyManager.removeAssociatedMetastasis(aAssociatedMetastasisID, theHistopathology);
 
                 ActionMessages msg = new ActionMessages();
                 msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("assocmetastasis.delete.successful"));
                 saveErrors(request, msg);
 
             } else {
 
                 Histopathology theAssociatedMetastasis = histopathologyManager.get(aAssociatedMetastasisID);
                 // Pass in the histopathology_id (aAssociatedMetastasisID) of
                 // the current metastatsis also
                 histopathologyManager.updateAssociatedMetastasis(theAnimalModel, assocMetastasisForm, theAssociatedMetastasis);
 
                 ActionMessages msg = new ActionMessages();
                 msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("assocmetastasis.edit.successful"));
                 saveErrors(request, msg);
             }
         } catch (Exception e) {
 
             log.error("Unable to get a Histpathology action: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
 
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 
     /**
      * Save
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward saveHistopathology(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
        log.debug("<HistopathologyAction> Entering 'save' method");
 
         // Grab the current modelID from the session
         String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
         // Create a form to edit
         HistopathologyForm histopathologyForm = (HistopathologyForm) form;
 
        log.debug("<HistopathologyAction save> following Characteristics:" 
         		+ "\n\t organ: " + histopathologyForm.getOrgan() 
         		+ "\n\t organTissueName: " + histopathologyForm.getOrganTissueName() 
         		+ "\n\t organTissueCode: " + histopathologyForm.getOrganTissueCode() 
                 + "\n\t DiagnosisName: " + histopathologyForm.getDiagnosisName() 
                 + "\n\t DiagnosisCode: " + histopathologyForm.getDiagnosisCode()                
                 + "\n\t TumorClassification: " + histopathologyForm.getTumorClassification()
                 + "\n\t OtherTumorClassification: " + histopathologyForm.getOtherTumorClassification()  
 
                 + "\n\t ageOfOnset: " + histopathologyForm.getAgeOfOnset() 
                 + "\n\t ageOfOnsetUnit: " + histopathologyForm.getAgeOfOnsetUnit()
                 + "\n\t ageOfDetection: " + histopathologyForm.getAgeOfDetection()
                 + "\n\t ageOfDetectionUnit: " + histopathologyForm.getAgeOfDetectionUnit()
                 + "\n\t weightOfTumor: " + histopathologyForm.getWeightOfTumor()
                 + "\n\t volumeOfTumor: " + histopathologyForm.getVolumeOfTumor() + "\n\t tumorIncidenceRate: "
                 + histopathologyForm.getTumorIncidenceRate() + "\n\t survivalInfo: "
                 + histopathologyForm.getMethodOfObservation() + "\n\t grossDescription: "
                 + histopathologyForm.getGrossDescription() + "\n\t microscopicDescription: "
                 + histopathologyForm.getMicroscopicDescription() + "\n\t observation: "
                 + histopathologyForm.getObservation() + "\n\t methodOfObservation: "
                 + histopathologyForm.getMethodOfObservation() + "\n\t comparativeData: "
                 + histopathologyForm.getComparativeData() + "\n\t comments: " + histopathologyForm.getComments()
                 + "\n\t theModelId: " + theModelId
                 + "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
         try {
             AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
             AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);
 
             theAnimalModelManager.addHistopathology(theAnimalModel, histopathologyForm);
 
             log.debug("New histopathology created");
 
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("histopathology.creation.successful"));
             saveErrors(request, msg);
 
             log.debug("<HistopathologyAction> Exiting 'save' method");
 
         } catch (Exception e) {
 
             log.error("Unable to get add an histopathology: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
         log.debug("<HistopathologyAction> Exiting saveHistopathology");
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 
     /**
      * Save
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward saveMetastasis(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.debug("<HistopathologyAction> Entering 'saveMetastasis' method");
 
         // Create a form to edit
         AssociatedMetastasisForm assocMetastasisForm = (AssociatedMetastasisForm) form;
         request.getSession().setAttribute(Constants.FORMDATA, assocMetastasisForm);
 
         // Grab the current modelID from the session
         String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
         // Grab the current aHistopathID from the session
         String aHistopathologyID = request.getParameter("aHistopathologyID");
 
         log.debug("<HistopathologyAction saveMetastasis> following Characteristics:" 
                  + "\n\t ParentHistopathID: " + aHistopathologyID  
                 + "\n\t organ: " + assocMetastasisForm.getOrgan() 
                 + "\n\t organTissueName: " + assocMetastasisForm.getOrganTissueName()
                 + "\n\t organTissueCode: " + assocMetastasisForm.getOrganTissueCode() 
                 + "\n\t DiagnosisName: " + assocMetastasisForm.getDiagnosisName() 
                 + "\n\t DiagnosisCode: " + assocMetastasisForm.getDiagnosisCode()                 
                 + "\n\t TumorClassification: " + assocMetastasisForm.getTumorClassification()
                 + "\n\t OtherTumorClassification: " + assocMetastasisForm.getOtherTumorClassification() 
                 + "\n\t ageOfOnset: " + assocMetastasisForm.getAgeOfOnset()
                 + "\n\t ageOfDetection: " + assocMetastasisForm.getAgeOfDetection()
                 + "\n\t weightOfTumor: " + assocMetastasisForm.getWeightOfTumor() + "\n\t volumeOfTumor: "
                 + assocMetastasisForm.getVolumeOfTumor() + "\n\t tumorIncidenceRate: "
                 + assocMetastasisForm.getTumorIncidenceRate() + "\n\t survivalInfo: "
                 + assocMetastasisForm.getMethodOfObservation() + "\n\t grossDescription: "
                 + assocMetastasisForm.getGrossDescription() + "\n\t microscopicDescription: "
                 + assocMetastasisForm.getMicroscopicDescription() + "\n\t observation: "
                 + assocMetastasisForm.getObservation() + "\n\t methodOfObservation: "
                 + assocMetastasisForm.getMethodOfObservation() + "\n\t comparativeData: "
                 + assocMetastasisForm.getComparativeData() + "\n\t comments: " + assocMetastasisForm.getComments()
                 + "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
         String theForward = "AnimalModelTreePopulateAction";
         
         HistopathologyManager theHistopathologyManager = (HistopathologyManager) getBean("histopathologyManager");
         Histopathology theParentHistopathology = theHistopathologyManager.get(aHistopathologyID);
 
         // retrieve model and update w/ new values
         AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);        
 
         try {
 
             theHistopathologyManager.addAssociatedMetastasis(theAnimalModel, theParentHistopathology, assocMetastasisForm);
 
             log.debug("New metastasis created");
 
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("assocmetastasis.creation.successful"));
             saveErrors(request, msg);
 
             log.debug("<HistopathologyAction> Exiting 'saveMetastasis' method");
 
         } catch (Exception e) {
             log.error("Exception occurred creating AssociatedExpression", e);
 
             // Encountered an error saving the model.
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
 
         log.debug("<HistopathologyAction> Exiting saveMetastasis");
         return mapping.findForward(theForward);
     }
 
 }

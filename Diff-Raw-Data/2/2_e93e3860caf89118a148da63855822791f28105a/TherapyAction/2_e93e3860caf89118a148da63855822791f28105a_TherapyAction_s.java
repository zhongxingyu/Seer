 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.webapp.form.TherapyForm;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.*;
 
 /**
  * TherapyAction Class
  */
 public final class TherapyAction extends BaseAction {
 	
     /**
      * Delete
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward delete(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
     throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'delete' method");
         }
 
         return mapping.findForward("");
     }
 
 	/**
 	 * Cancel
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
     public ActionForward duplicate(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
     throws Exception {
     	
     	 return mapping.findForward("");
     }    
     
     
     /**
      * Edit
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward edit(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
     throws Exception {
 
         log.trace("Entering edit");
 
 		// Create a form to edit
 		TherapyForm therapyForm = ( TherapyForm ) form;        
         
         // Grab the current modelID from the session
         String aTherapyID = request.getParameter("aTherapyID");        
         
 		System.out.println( "<TherapyAction save> following Characteristics:" + 
 				"\n\t name: " + therapyForm.getName() + 
 				"\n\t NSCNumber: " + therapyForm.getNSCNumber() + 
 				"\n\t CASNumber: " + therapyForm.getCASNumber() + 
 				"\n\t toxicityGrade: " + therapyForm.getToxicityGrade() +
 				"\n\t chemicalClassName: " + therapyForm.getChemicalClassName() +
 				"\n\t processName: " + therapyForm.getProcessName() +
 				"\n\t targetName: " + therapyForm.getType() +
 				"\n\t dosage: " + therapyForm.getAdministrativeRoute() +
 				"\n\t type: " + therapyForm.getType() +
 				"\n\t age: " + therapyForm.getAgeAtTreatment() +
 				"\n\t administrativeRoute: " + therapyForm.getAdministrativeRoute() +	
 				"\n\t biomarker: " + therapyForm.getBiomarker() +
 				"\n\t tumorResponse: " + therapyForm.getTumorResponse() +
 				"\n\t experiment: " + therapyForm.getExperiment() +	
 				"\n\t results: " + therapyForm.getResults() +
 				"\n\t comments: " + therapyForm.getComments() +								
 				"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
 		TherapyManager therapyManager = (TherapyManager) getBean("therapyManager");
         
         try {
         	Therapy theTherapy = therapyManager.get(aTherapyID);
         	therapyManager.update(therapyForm, theTherapy);
 
             // Add a message to be displayed in submitOverview.jsp saying you've
             // created a new model successfully
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("therapy.edit.successful"));
             saveErrors(request, msg);
 
         } catch (Exception e) {
 
             log.error("Unable to get add a chemical drug action: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
 
         return mapping.findForward("AnimalModelTreePopulateAction");
 
     }
 
     /**
      * Save
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward save(ActionMapping mapping, ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
     throws Exception {
 
         log.trace("Entering save"); 
         System.out.println( "<TherapyAction save> entering" );
 
         // Grab the current modelID from the session
         String modelID = (String) request.getSession().getAttribute(Constants.MODELID);        
         
 		// Create a form to edit
 		TherapyForm therapyForm = ( TherapyForm ) form;
     	
 		System.out.println( "<TherapyAction save> following Characteristics:" + 
 							"\n\t name: " + therapyForm.getName() + 
 							"\n\t NSCNumber: " + therapyForm.getNSCNumber() + 
 							"\n\t CASNumber: " + therapyForm.getCASNumber() + 
 							"\n\t toxicityGrade: " + therapyForm.getToxicityGrade() +
 							"\n\t chemicalClassName: " + therapyForm.getChemicalClassName() +
 							"\n\t processName: " + therapyForm.getProcessName() +
 							"\n\t targetName: " + therapyForm.getType() +
 							"\n\t dosage: " + therapyForm.getAdministrativeRoute() +
 							"\n\t type: " + therapyForm.getType() +
 							"\n\t age: " + therapyForm.getAgeAtTreatment() +
 							"\n\t administrativeRoute: " + therapyForm.getAdministrativeRoute() +	
 							"\n\t biomarker: " + therapyForm.getBiomarker() +
 							"\n\t tumorResponse: " + therapyForm.getTumorResponse() +
 							"\n\t experiment: " + therapyForm.getExperiment() +	
 							"\n\t results: " + therapyForm.getResults() +
 							"\n\t comments: " + therapyForm.getComments() +								
 							"\n\t user: " + (String) request.getSession().getAttribute( "camod.loggedon.username" ) );
 		
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
 
         AnimalModel animalModel = animalModelManager.get(modelID);		
 
         try {		
             animalModelManager.addTherapy(animalModel, therapyForm);
 
             // Add a message to be displayed in submitOverview.jsp saying you've
             // created a new model successfully
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("therapy.creation.successful"));
             saveErrors(request, msg);
 
         } catch (Exception e) {
 
            log.error("Unable to get add a chemical drug action: ", e);
 
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
         }
 
         return mapping.findForward("AnimalModelTreePopulateAction");
     }
 }

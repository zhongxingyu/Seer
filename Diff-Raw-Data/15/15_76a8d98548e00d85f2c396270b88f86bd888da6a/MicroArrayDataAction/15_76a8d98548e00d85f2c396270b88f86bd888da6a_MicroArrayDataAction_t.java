 /**
  * 
  * $Id: MicroArrayDataAction.java,v 1.7 2008-08-14 20:09:18 schroedn Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.6  2008/08/14 19:01:41  schroedn
  * removed sys.out
  *
  * Revision 1.5  2008/08/14 06:17:03  schroedn
  * Microarray - added new field for other urls
  *
  * Revision 1.4  2006/04/17 19:09:41  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.MicroArrayData;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.MicroArrayDataManager;
 import gov.nih.nci.camod.webapp.form.MicroArrayDataForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 
 /**
  * MicroArrayDataAction Class
  */
 public final class MicroArrayDataAction extends BaseAction {
 	   
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
/*        if (!isTokenValid(request)) {
 			return mapping.findForward("failure");
 		}
*/
 		MicroArrayDataForm microArrayDataForm = (MicroArrayDataForm) form;	
 		
 		// Grab the current aCellID from the session
 		String aMicroArrayID = request.getParameter("aMicroArrayDataID");
 		
 		log.debug("<MicroArrayDataAction save> following Characteristics:" 
 				+ "\n\t CellLineName: " + microArrayDataForm.getExperimentName() 
 				+ "\n\t Experiment: " + microArrayDataForm.getUrl()				
 				+ "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
 		String theAction = (String) request.getParameter(Constants.Parameters.ACTION);
 
 		try {
 			MicroArrayDataManager theMicroArrayDataManager = (MicroArrayDataManager) getBean("microArrayDataManager");
             if ("Delete".equals(theAction)) {
                 
                 // Grab the current modelID from the session
                 String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
                 AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
                 AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);
                 
                 theMicroArrayDataManager.remove(aMicroArrayID, theAnimalModel);
 
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("microarraydata.delete.successful"));
 				saveErrors(request, msg);
 
 			} else {
 
 				MicroArrayData theMicroArrayData = theMicroArrayDataManager.get(aMicroArrayID);
 
 				theMicroArrayDataManager.update(microArrayDataForm, theMicroArrayData);
 
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("microarraydata.edit.successful"));
 				saveErrors(request, msg);
 			}
 		} catch (Exception e) {
 
 			log.error("Exception occurred creating Micro Array Data", e);
 
 			// Encountered an error saving the model.
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(request, msg);
 		}
 
 		log.trace("Exiting edit");
//		resetToken(request);
 
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
         if (!isTokenValid(request)) {
 			return mapping.findForward("failure");
 		}
         
 		MicroArrayDataForm microArrayDataForm = (MicroArrayDataForm) form;		
 		//CellLineForm cellLineForm = (CellLineForm) form;
 
 		// Grab the current modelID from the session
 		String theModelId = (String) request.getSession().getAttribute(Constants.MODELID);
 
 		log.debug("<MicroArrayDataAction save> following Characteristics:" 
 				+ "\n\t CellLineName: " + microArrayDataForm.getExperimentName() 
 				+ "\n\t Experiment: " + microArrayDataForm.getUrl()				
 				+ "\n\t user: " + (String) request.getSession().getAttribute("camod.loggedon.username"));
 
 		String theForward = "AnimalModelTreePopulateAction";
 
 		try {
 			// retrieve model and update w/ new values
 			AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
 			AnimalModel theAnimalModel = theAnimalModelManager.get(theModelId);
 
 			theAnimalModelManager.addMicroArrayData(theAnimalModel, microArrayDataForm);
 
 			log.debug("New MicroArrayData created");
 
 			// Add a message to be displayed in submitOverview.jsp saying you've
 			// created a new model successfully
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("microarraydata.creation.successful"));
 			saveErrors(request, msg);
 
 		} catch (Exception e) {
 			log.error("Exception occurred creating cell line", e);
 
 			// Encountered an error saving the model.
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(request, msg);
 
 			theForward = "failure";
 		}
 
 		log.trace("Exiting save");
 		resetToken(request);
 
 		return mapping.findForward(theForward);
 	}
 }

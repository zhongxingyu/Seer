 /**
  *  @author dgeorge
  *  
  *  $Id: ChangeAnimalModelStatePopulateAction.java,v 1.15 2007-09-12 19:36:40 pandyas Exp $
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.14  2007/09/12 19:09:21  pandyas
  *  Added if statement to check for model_id from GUI - this used to work
  *
  *  Revision 1.13  2007/08/14 12:04:53  pandyas
  *  Working on model id display on search results screen
  *
  *  Revision 1.12  2006/10/17 16:11:00  pandyas
  *  modified during development of caMOD 2.2 - various
  *
  *  Revision 1.11  2006/08/17 18:06:57  pandyas
  *  Defect# 410: Externalize properties files - Code changes to get properties
  *
  *  Revision 1.10  2005/11/28 13:48:37  georgeda
  *  Defect #192, handle back arrow for curation changes
  *
  *  Revision 1.9  2005/10/24 13:28:17  georgeda
  *  Cleanup changes
  *
  *  Revision 1.8  2005/09/22 15:17:36  georgeda
  *  More changes
  *
  *  Revision 1.7  2005/09/19 14:21:47  georgeda
  *  Added interface for URL parameters
  *
  *  Revision 1.6  2005/09/19 13:38:42  georgeda
  *  Cleaned up parameter passing
  *
  *  Revision 1.5  2005/09/19 13:09:52  georgeda
  *  Added header
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Log;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.impl.LogManagerSingleton;
 import gov.nih.nci.camod.webapp.form.AnimalModelStateForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 public class ChangeAnimalModelStatePopulateAction extends BaseAction {
 
 	/**
 	 * Change the state for the curation model
 	 */
 	public ActionForward execute(ActionMapping inMapping, ActionForm inForm, HttpServletRequest inRequest,
 			HttpServletResponse inResponse) throws Exception {
 
 		log.debug("Entering ChangeAnimalModelStatePopulateAction.execute");
 
 		String theForward = "next";
         String theModelId = null;
         AnimalModel theAnimalModel = null;
 
 		try {
 
 			// Get the attributes from the request
 			/* Grab the current modelID from the session */
 			theModelId = (String) inRequest.getSession().getAttribute(Constants.Parameters.MODELID);	
             log.debug("theModelId: " + theModelId);
 			String theEvent = inRequest.getParameter(Constants.Parameters.EVENT);
             log.debug("theEvent: " + theEvent);
             
 			AnimalModelManager theAnimalModelManager = (AnimalModelManager) getBean("animalModelManager");
             log.debug("here1: " ); 
             
             if (theModelId != null){            
                 theAnimalModel = theAnimalModelManager.get(theModelId);
             } else {
                 theModelId = inRequest.getParameter("aModelID");
                 log.debug("theModelId from getParameter: " + theModelId); 
                 theAnimalModel = theAnimalModelManager.get(theModelId);
             }
             log.debug("here2: " ); 
 
 			// Set up the form
 			AnimalModelStateForm theForm = (AnimalModelStateForm) inForm;
 			theForm.setEvent(theEvent);
             theForm.setModelId(theModelId);
 			theForm.setModelDescriptor(theAnimalModel.getModelDescriptor());
             log.debug("here3: " );            
 
 			// Null out the list in case it had been already set
 			inRequest.getSession().setAttribute(Constants.Dropdowns.USERSFORROLEDROP, null);
 
 			log.debug("<ChangeAnimalModelStatePopulateAction> The model id: " + theModelId + " and event: " + theEvent);
 			inRequest.setAttribute("wiki_cs_help", "");
 			
 			// Setting the action. This is used to customize the jsp display
 			if (theEvent.equals(Constants.Admin.Actions.ASSIGN_SCREENER)) {
 				inRequest.setAttribute("action", "Assigning Screener to ");
 				inRequest.setAttribute("wiki_cs_help", "1");
 				NewDropdownUtil.populateDropdown(inRequest, Constants.Dropdowns.USERSFORROLEDROP,
 						Constants.Admin.Roles.SCREENER);
 			} else if (theEvent.equals(Constants.Admin.Actions.ASSIGN_EDITOR)) {
 				inRequest.setAttribute("action", "Assigning Editor to ");
 				inRequest.setAttribute("wiki_cs_help", "2");
 				NewDropdownUtil.populateDropdown(inRequest, Constants.Dropdowns.USERSFORROLEDROP,
 						Constants.Admin.Roles.EDITOR);
 			} else if (theEvent.equals(Constants.Admin.Actions.NEED_MORE_INFO)) {
 
 				// Assign to the current person
 				Log theLog = LogManagerSingleton.instance().getCurrentByModel(theAnimalModel);
 				theForm.setAssignedTo(theLog.getSubmitter().getUsername());
 				inRequest.setAttribute("action", "Requesting more information for ");
 
 			} else if (theEvent.equals(Constants.Admin.Actions.SCREENER_REJECT)) {
 
 				// Assign to the coordinator
 				Properties camodProperties = new Properties();
 				String camodPropertiesFileName = null;
 
 				camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 				
 				try {
 			
 				FileInputStream in = new FileInputStream(camodPropertiesFileName);
 				camodProperties.load(in);
 	
 				} 
 				catch (FileNotFoundException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				} catch (IOException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				}
 				String theCoordinator = camodProperties.getProperty("coordinator.username");
 				theForm.setAssignedTo(theCoordinator);
 
 				inRequest.setAttribute("action", "Rejecting ");
 			} else if (theEvent.equals(Constants.Admin.Actions.SCREENER_APPROVE) || theEvent.equals(Constants.Admin.Actions.EDITOR_APPROVE)) {
 
 				
 				// Assign to the coordinator
 				Properties camodProperties = new Properties();
 				String camodPropertiesFileName = null;
 
 				camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 				
 				try {
 			
 				FileInputStream in = new FileInputStream(camodPropertiesFileName);
 				camodProperties.load(in);
 	
 				} 
 				catch (FileNotFoundException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				} catch (IOException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				}
 				
 				String theCoordinator = camodProperties.getProperty("coordinator.username");
 				theForm.setAssignedTo(theCoordinator);
 				
 				inRequest.setAttribute("action", "Approving ");
 				
 			} else if (theEvent.equals(Constants.Admin.Actions.INACTIVATE)) {
 
 				log.debug("<ChangeAnimalModelStatePopulateAction> Inside inactive loop - the event is: " + theEvent);				
 				// Assign to the coordinator
 				Properties camodProperties = new Properties();
 				String camodPropertiesFileName = null;
 
 				camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 				
 				try {
 			
 				FileInputStream in = new FileInputStream(camodPropertiesFileName);
 				camodProperties.load(in);
 	
 				} 
 				catch (FileNotFoundException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				} catch (IOException e) {
 					log.error("Caught exception finding file for properties: ", e);
 					e.printStackTrace();			
 				}
 				
 				String theCoordinator = camodProperties.getProperty("coordinator.username");
 				theForm.setAssignedTo(theCoordinator);
 				log.debug("<ChangeAnimalModelStatePopulateAction> setting the coordinator to: " + theCoordinator);
 				
 				inRequest.setAttribute("action", "Inactivating ");
 			} 
 			else {
 				throw new IllegalArgumentException("Unknown event type: " + theEvent);
 			}
 		} catch (Exception e) {
 
 			log.error("Caught an exception populating the data: ", e);
 
 			// Encountered an error
 			ActionMessages theMsg = new ActionMessages();
 			theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
 			saveErrors(inRequest, theMsg);
 
 			theForward = "failure";
 		}
 		log.debug("Exiting ChangeAnimalModelStatePopulateAction.execute");
 
 		return inMapping.findForward(theForward);
 	}
 }

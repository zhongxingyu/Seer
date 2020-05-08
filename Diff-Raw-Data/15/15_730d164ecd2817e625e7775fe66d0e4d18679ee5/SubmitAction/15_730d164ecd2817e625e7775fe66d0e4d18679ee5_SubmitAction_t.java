 /**
  *  
 *  $Id: SubmitAction.java,v 1.22 2009-06-11 19:48:35 pandyas Exp $
  *  
  *  $Log: not supported by cvs2svn $
 *  Revision 1.21  2008/08/14 16:57:47  pandyas
 *  modified debug line to use log
 *
  *  Revision 1.20  2008/01/15 19:32:07  pandyas
  *  Modified debug statements to build to dev tier
  *
  *  Revision 1.19  2008/01/15 10:23:20  pandyas
  *  enabled debug statements for testing of errors on DEV
  *  setCancerModels error stops submission of models and superuser not being implemented from camod.properties
  *
  *  Revision 1.18  2007/09/12 19:36:40  pandyas
  *  modified debug statements for build to stage tier
  *
  *  Revision 1.17  2007/08/14 12:05:12  pandyas
  *  Implementing EVSPreferredName for Zebrafish models
  *
  *  Revision 1.16  2007/04/09 12:37:09  pandyas
  *  modified after caMOD 2.3 unit testing
  *
  *  Revision 1.15  2007/03/26 12:02:30  pandyas
  *  caMOd 2.3 enhancements for Zebrafish support
  *
  *  Revision 1.14  2006/08/17 18:10:05  pandyas
  *  Defect# 410: Externalize properties files - Code changes to get properties
  *
  *  Revision 1.13  2005/12/02 16:17:09  georgeda
  *  Defect #247, set the formdata in the session
  *
  *  Revision 1.12  2005/10/24 13:28:17  georgeda
  *  Cleanup changes
  *
  *  Revision 1.11  2005/09/22 18:56:37  georgeda
  *  Get coordinator from user in properties file
  *
  *  Revision 1.10  2005/09/22 15:18:43  georgeda
  *  More changes
  *
  *  Revision 1.9  2005/09/16 15:52:55  georgeda
  *  Changes due to manager re-write
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Species;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.webapp.form.AnimalModelStateForm;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 public class SubmitAction extends BaseAction {
 
     /**
      * called from SubmitModels.jsp from list of models links
      * 
      */
     public ActionForward setModelConstants(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.info("<SubmitAction setModelConstants> Constants.Parameters.MODELID="
                 + request.getParameter(Constants.Parameters.MODELID));
 
         String modelID = request.getParameter(Constants.Parameters.MODELID);
        log.info("modelID: " + modelID);
 
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
 
         String theForward = "AnimalModelTreePopulateAction";
 
         try {
             AnimalModel am = animalModelManager.get(modelID);
             String speciesName = am.getStrain().getSpecies().getCommonName();
             
             // Set animal model species up front for genetic description (mgi, zfin, or rgd id)
             request.getSession().setAttribute(Constants.AMMODELSPECIESCOMMONNAME, speciesName);            
 
             request.getSession().setAttribute(Constants.MODELID, am.getId().toString());
             // Used for submitOverview to set model back to previous states
             request.getSession().setAttribute(Constants.Parameters.MODELID, am.getId().toString());
             request.getSession().setAttribute(Constants.MODELDESCRIPTOR, am.getModelDescriptor());
             request.getSession().setAttribute(Constants.MODELSTATUS, am.getState());
             
             AnimalModelStateForm theForm = new AnimalModelStateForm();
             theForm.setModelId(am.getId().toString());
            log.info("setModelId: " + am.getId().toString());
             
             // Get the coordinator
     		Properties camodProperties = new Properties();
     		String camodPropertiesFileName = null;
 
     		camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
    		log.info("camodPropertiesFileName: " + camodPropertiesFileName.toString());
     		
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
             request.getSession().setAttribute(Constants.FORMDATA, theForm);
 
         } catch (Exception e) {
             log.error("Exception occurred in setModelConstants", e);
 
             // Encountered an error saving the model.
             // created a new model successfully
             ActionMessages theMsg = new ActionMessages();
             theMsg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, theMsg);
 
             theForward = "failure";
         }
         return mapping.findForward(theForward);
     }
 }

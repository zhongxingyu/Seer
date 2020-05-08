 /**
  * 
  * $Id: MicroArrayDataPopulateAction.java,v 1.4 2008-08-14 20:09:18 schroedn Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.3  2008/08/14 06:16:52  schroedn
  * Microarray - added new field for other urls
  *
  * Revision 1.2  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.MicroArrayData;
 import gov.nih.nci.camod.service.MicroArrayDataManager;
 import gov.nih.nci.camod.webapp.form.MicroArrayDataForm;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class MicroArrayDataPopulateAction extends BaseAction {
 	
 	/** 
 	 * Pre-populate all field values in the form <FormName> 
 	 *  Used by <jspName>
 	 * 
 	 */ 
 	public ActionForward populate( ActionMapping mapping, 
 								   ActionForm form,
 						           HttpServletRequest request,
 						           HttpServletResponse response)
 	  throws Exception {
 		
 		// Create a form to edit
 		MicroArrayDataForm microArrayForm = (MicroArrayDataForm) form;
 
 		// Grab the current MicroarrayData we are working with related to this
 		// animalModel
 		String aMicroArrayID = request.getParameter("aMicroArrayDataID");
 		
 		MicroArrayDataManager theMicroArrayDataManager = (MicroArrayDataManager) getBean("microArrayDataManager");
 		MicroArrayData microArray = theMicroArrayDataManager.get(aMicroArrayID);
 		
 		if (microArray == null) {
 			request.setAttribute(Constants.Parameters.DELETED, "true");
 		} else {
 			request.setAttribute("aMicroArrayDataID", aMicroArrayID);
 			
 			microArrayForm.setExperimentName(microArray.getExperimentName());
 			microArrayForm.setUrl(microArray.getUrl());			
 		}
 		
//		saveToken(request);
 		return mapping.findForward("submitMicroarrayData");
 	}	
 
 }

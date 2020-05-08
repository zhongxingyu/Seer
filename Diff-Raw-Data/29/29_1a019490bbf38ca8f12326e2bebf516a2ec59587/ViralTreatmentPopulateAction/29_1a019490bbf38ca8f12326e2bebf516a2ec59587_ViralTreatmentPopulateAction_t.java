 /**
  * 
 * $Id: ViralTreatmentPopulateAction.java,v 1.11 2005-12-21 15:44:15 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.10  2005/11/03 13:59:10  georgeda
 * Fixed delete functionality
 *
  * Revision 1.9  2005/10/31 13:46:28  georgeda
  * Updates to handle back arrow
  *
  * Revision 1.8  2005/10/28 12:47:26  georgeda
  * Added delete functionality
  *
  * Revision 1.7  2005/10/27 19:25:06  georgeda
  * Validation changes
  *
  * Revision 1.6  2005/10/20 20:40:51  pandyas
  * added javadocs
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.webapp.form.ViralTreatmentForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class ViralTreatmentPopulateAction extends BaseAction {
 
 	/**
 	 * Pre-populate all field values in the form SurgeryForm Used by <jspName>
 	 * 
 	 */
 	public ActionForward populate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		System.out.println("<ViralTreatmentPopulateAction populate> ... ");
 
 		ViralTreatmentForm viralTreatmentForm = (ViralTreatmentForm) form;
 
 		String aTherapyID = request.getParameter("aTherapyID");
 
 		TherapyManager therapyManager = (TherapyManager) getBean("therapyManager");
 		Therapy therapy = therapyManager.get(aTherapyID);
 
 		// Handle back-arrow on the delete
 		if (therapy == null) {
 			request.setAttribute(Constants.Parameters.DELETED, "true");
 		} else {
 
 			request.setAttribute("aTherapyID", aTherapyID);
 
 			// Set the otherName and/or the selected name attribute
 			if (therapy.getAgent().getNameUnctrlVocab() != null) {
 				viralTreatmentForm.setName(Constants.Dropdowns.OTHER_OPTION);
 				viralTreatmentForm.setOtherName(therapy.getAgent().getNameUnctrlVocab());
 			} else {
 				viralTreatmentForm.setName(therapy.getAgent().getName());
 			}
 
 			if (therapy.getTreatment().getSexDistribution() != null) {
 				viralTreatmentForm.setType(therapy.getTreatment().getSexDistribution().getType());
 			}
 			viralTreatmentForm.setAgeAtTreatment(therapy.getTreatment().getAgeAtTreatment());
 			viralTreatmentForm.setDosage(therapy.getTreatment().getDosage());
 
 			viralTreatmentForm.setRegimen(therapy.getTreatment().getRegimen());
 			viralTreatmentForm.setAdministrativeRoute(therapy.getTreatment().getAdministrativeRoute());
 
            if (therapy.getTreatment().getAdminRouteUnctrlVocab() != null) {
                viralTreatmentForm.setAdministrativeRoute(Constants.Dropdowns.OTHER_OPTION);
                viralTreatmentForm.setOtherAdministrativeRoute(therapy.getTreatment().getAdminRouteUnctrlVocab());
            }
 		}
 
 		// Prepopulate all dropdown fields, set the global Constants to the
 		// following
 		this.dropdown(request, response);
 
 		// Store the Form in session to be used by the JSP
 		request.getSession().setAttribute(Constants.FORMDATA, viralTreatmentForm);
 
 		return mapping.findForward("submitViralTreatment");
 	}
 
 	/**
 	 * Populate the dropdown menus for submitSurgeryOther
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward dropdown(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
 		System.out.println("<ViralTreatmentPopulateAction dropdown> ... ");
 
 		// blank out the FORMDATA Constant field
 		ViralTreatmentForm viralTreatmentForm = (ViralTreatmentForm) form;
 		request.getSession().setAttribute(Constants.FORMDATA, viralTreatmentForm);
 
 		// setup dropdown menus
 		this.dropdown(request, response);
 
 		System.out.println("<ViralTreatmentPopulateAction> exiting... ");
 
 		return mapping.findForward("submitViralTreatment");
 	}
 
 	/**
 	 * Populate all drowpdowns for this type of form
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	public void dropdown(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		System.out.println("<ViralTreatmentPopulateAction dropdown> Entering... ");
 
 		// Prepopulate all dropdown fields, set the global Constants to the
 		// following
 
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SEXDISTRIBUTIONDROP,
 				Constants.Dropdowns.ADD_BLANK);
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.AGEUNITSDROP, "");
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.VIRUSDROP, Constants.Dropdowns.ADD_BLANK);
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.VIRALTREATUNITSDROP, "");
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ADMINISTRATIVEROUTEDROP,
 				Constants.Dropdowns.ADD_BLANK);
 	}
 }

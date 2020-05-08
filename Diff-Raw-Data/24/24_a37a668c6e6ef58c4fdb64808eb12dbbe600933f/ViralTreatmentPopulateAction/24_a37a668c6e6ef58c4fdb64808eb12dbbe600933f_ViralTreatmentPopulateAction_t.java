 /**
  * 
 * $Id: ViralTreatmentPopulateAction.java,v 1.14 2007-10-31 18:40:16 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.13  2006/11/09 17:32:48  pandyas
 * Commented out debug code
 *
  * Revision 1.12  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.11  2005/12/21 15:44:15  georgeda
  * Defect #283 - fixed population of other admin route
  *
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
 import gov.nih.nci.camod.domain.CarcinogenExposure;
 import gov.nih.nci.camod.service.CarcinogenExposureManager;
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
 
         log.debug("<ViralTreatmentPopulateAction populate> ... ");
 
 		ViralTreatmentForm viralTreatmentForm = (ViralTreatmentForm) form;
 
         String aCarcinogenExposureID = request.getParameter("aCarcinogenExposureID");
 
         CarcinogenExposureManager carcinogenExposureManager = (CarcinogenExposureManager) getBean("carcinogenExposureManager");
         CarcinogenExposure ce = carcinogenExposureManager.get(aCarcinogenExposureID);      
 
 		// Handle back-arrow on the delete
 		if (ce == null) {
 			request.setAttribute(Constants.Parameters.DELETED, "true");
 		} else {
 
             request.setAttribute("aCarcinogenExposureID", aCarcinogenExposureID);
 
 			// Set the otherName and/or the selected name attribute
			if (ce.getEnvironmentalFactor().getNameAlternEntry() != null) {
 				viralTreatmentForm.setName(Constants.Dropdowns.OTHER_OPTION);
				viralTreatmentForm.setOtherName(ce.getEnvironmentalFactor().getNameAlternEntry());
 			} else {
 				viralTreatmentForm.setName(ce.getEnvironmentalFactor().getName());
 			}
 
 			if (ce.getTreatment().getSexDistribution() != null) {
 				viralTreatmentForm.setType(ce.getTreatment().getSexDistribution().getType());
 			}
 			viralTreatmentForm.setAgeAtTreatment(ce.getTreatment().getAgeAtTreatment());
             viralTreatmentForm.setAgeAtTreatmentUnit(ce.getTreatment().getAgeAtTreatmentUnit());            
 			viralTreatmentForm.setDosage(ce.getTreatment().getDosage());
             viralTreatmentForm.setDosageUnit(ce.getTreatment().getDosageUnit());
 
 			viralTreatmentForm.setRegimen(ce.getTreatment().getRegimen());
 			viralTreatmentForm.setAdministrativeRoute(ce.getTreatment().getAdministrativeRoute());
 
            if (ce.getTreatment().getAdminRouteAlternEntry() != null) {
                 viralTreatmentForm.setAdministrativeRoute(Constants.Dropdowns.OTHER_OPTION);
                viralTreatmentForm.setOtherAdministrativeRoute(ce.getTreatment().getAdminRouteAlternEntry());
             }
            
            if (ce.getEnvironmentalFactor().getComments() != null)
            {
            	viralTreatmentForm.setComments(ce.getEnvironmentalFactor().getComments());
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
 
 		log.debug("<ViralTreatmentPopulateAction dropdown> ... ");
 
 		// blank out the FORMDATA Constant field
 		ViralTreatmentForm viralTreatmentForm = (ViralTreatmentForm) form;
 		request.getSession().setAttribute(Constants.FORMDATA, viralTreatmentForm);
 
 		// setup dropdown menus
 		this.dropdown(request, response);
 
 		log.debug("<ViralTreatmentPopulateAction> exiting... ");
 
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
 
 		log.debug("<ViralTreatmentPopulateAction dropdown> Entering... ");
 
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

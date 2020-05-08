 /**
  * 
 * $Id: NutritionalFactorPopulateAction.java,v 1.13 2007-10-31 18:31:59 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.12  2006/11/09 17:30:15  pandyas
 * Commented out debug code
 *
  * Revision 1.11  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
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
  * Revision 1.6  2005/10/20 20:39:17  pandyas
  * added javadocs
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.CarcinogenExposure;
 import gov.nih.nci.camod.service.CarcinogenExposureManager;
 import gov.nih.nci.camod.webapp.form.NutritionalFactorForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class NutritionalFactorPopulateAction extends BaseAction {
 
 	/**
 	 * Pre-populate all field values in the form <FormName> Used by <jspName>
 	 * 
 	 */
 	public ActionForward populate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
         log.debug("<NutritionalFactorPopulateAction populate> Entering populate() ");
 
 		// Create a form to edit
 		NutritionalFactorForm nutritForm = (NutritionalFactorForm) form;
 
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
 				nutritForm.setName(Constants.Dropdowns.OTHER_OPTION);
				nutritForm.setOtherName(ce.getEnvironmentalFactor().getNameAlternEntry());
 			} else {
 				nutritForm.setName(ce.getEnvironmentalFactor().getName());
 			}
 
 			if (ce.getTreatment().getSexDistribution() != null) {
 				nutritForm.setType(ce.getTreatment().getSexDistribution().getType());
 			}
 			nutritForm.setDosage(ce.getTreatment().getDosage());
             nutritForm.setDosageUnit(ce.getTreatment().getDosageUnit());
 			nutritForm.setRegimen(ce.getTreatment().getRegimen());
 			nutritForm.setAgeAtTreatment(ce.getTreatment().getAgeAtTreatment());
             nutritForm.setAgeAtTreatmentUnit(ce.getTreatment().getAgeAtTreatmentUnit());
            
            if (ce.getEnvironmentalFactor().getComments() != null)
            {
            	nutritForm.setComments(ce.getEnvironmentalFactor().getComments());
            }            
 
 		}
 
 		// Prepopulate all dropdown fields, set the global Constants to the
 		// following
 		this.dropdown(request, response);
 
 		return mapping.findForward("submitNutritionalFactors");
 	}
 
 	/**
 	 * Populate the dropdown menus for submitEnvironmentalFactors
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
 
         log.debug("<NutritionalFactorPopulateAction dropdown> Entering dropdown() ");
 
 		// blank out the FORMDATA Constant field
 		NutritionalFactorForm nutritForm = (NutritionalFactorForm) form;
 		request.getSession().setAttribute(Constants.FORMDATA, nutritForm);
 
 		// setup dropdown menus
 		this.dropdown(request, response);
 
 		log.debug("<NutritionalFactorPopulateAction dropdown> before return submitRadiation ");
 
 		return mapping.findForward("submitNutritionalFactors");
 
 	}
 
 	/**
 	 * Populate all drowpdowns for this type of form
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	public void dropdown(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		log.debug("<NutritionalFactorPopulateAction dropdown> Entering void dropdown()");
 
 		// Prepopulate all dropdown fields, set the global Constants to the
 		// following
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.NUTRITIONFACTORDROP,
 				Constants.Dropdowns.ADD_BLANK);
 
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.NUTFACTORUNITSDROP, "");
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.AGEUNITSDROP, "");
 		NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SEXDISTRIBUTIONDROP,
 				Constants.Dropdowns.ADD_BLANK);
 
 		log.debug("<NutritionalFactorPopulateAction dropdown> Exiting void dropdown()");
 
 	}
 }

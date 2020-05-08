 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.webapp.form.ChemicalDrugForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 public class ChemicalDrugPopulateAction extends BaseAction {
 
     /**
     * Pre-populate all field values in the form ChemicalDrug Used by <jspName>
      * 
      */
     public ActionForward populate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         System.out.println("<ChemicalDrugPopulateAction populate> ... ");
 
         ChemicalDrugForm chemicalDrugForm = (ChemicalDrugForm) form;
 
         String aTherapyID = request.getParameter("aTherapyID");
 
         String modelID = "" + request.getSession().getAttribute(Constants.MODELID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
 
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         this.dropdown(request, response);
 
         // retrieve the list of all therapies from the current animalModel
         List therapyList = am.getTherapyCollection();
 
         Therapy ty = new Therapy();
 
         // find the specific one we need
         for (int i = 0; i < therapyList.size(); i++) {
             ty = (Therapy) therapyList.get(i);
             if (ty.getId().toString().equals(aTherapyID))
                 break;
         }
 
         chemicalDrugForm.setType(ty.getTreatment().getSexDistribution().getType());
         if (ty.getTreatment().getAgeAtTreatment() != null) {
             chemicalDrugForm.setAgeAtTreatment(ty.getTreatment().getAgeAtTreatment());
         }
 
         // Parse the doseUnit out of the Doseage
         // only matches the .txt file
         // List doseUnitList = (ArrayList) request.getSession().getAttribute(
         // Constants.Dropdowns.DOSAGEUNITSDROP );
         // String doseUnit = ty.getTreatment().getDosage();
         // System.out.println( "Checking for doseUnit matches" );
 
         /*
          * for ( int i=0; i < doseUnitList.size(); i++ ) { String t = (String)
          * doseUnitList.get(i); System.out.println("Unit=" + t);
          * 
          * if ( doseUnit.indexOf( t ) != -1 ) System.out.println( "found a
          * match" ); }
          */
 
         chemicalDrugForm.setDosage(ty.getTreatment().getDosage());
         // chemicalDrugForm.setDoseUnit();
         chemicalDrugForm.setName(ty.getAgent().getName());
         chemicalDrugForm.setRegimen(ty.getTreatment().getRegimen());
         chemicalDrugForm.setAdministrativeRoute(ty.getTreatment().getAdministrativeRoute());
         chemicalDrugForm.setCASNumber(ty.getAgent().getCasNumber());
         
         if (ty.getAgent().getNscNumber() != null) {
             chemicalDrugForm.setNSCNumber(ty.getAgent().getNscNumber().toString());
         }
 
         // Store the Form in session to be used by the JSP
         request.getSession().setAttribute(Constants.FORMDATA, chemicalDrugForm);
 
         return mapping.findForward("submitChemicalDrug");
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
 
         System.out.println("<ChemicalDrugPopulateAction dropdown> ... ");
 
         // blank out the FORMDATA Constant field
         ChemicalDrugForm chemicalDrugForm = (ChemicalDrugForm) form;
         request.getSession().setAttribute(Constants.FORMDATA, chemicalDrugForm);
 
         // setup dropdown menus
         this.dropdown(request, response);
 
         System.out.println("<ChemicalDrugPopulateAction> exiting... ");
 
         return mapping.findForward("submitChemicalDrug");
     }
 
     /**
      * Populate all drowpdowns for this type of form
      * 
      * @param request
      * @param response
      * @throws Exception
      */
     public void dropdown(HttpServletRequest request, HttpServletResponse response) throws Exception {
 
         System.out.println("<ChemicalDrugPopulateAction dropdown> Entering... ");
 
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SEXDISTRIBUTIONDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.AGEUNITSDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.CHEMICALDRUGDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.DOSAGEUNITSDROP, "");
         System.out.println("Before admin");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ADMINISTRATIVEROUTEDROP, "");
 
         System.out.println("Finishing dropdown");
     }
 
 }

 /**
  * 
 * $Id: TherapyPopulateAction.java,v 1.18 2006-04-27 18:34:53 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.17  2006/04/18 17:55:10  pandyas
 * Fixed Therapy multi-select collections -  populate issue
 *
  * Revision 1.16  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.15  2005/12/29 18:29:14  pandyas
  * Clean up - removed code for TumorResponse
  *
  * Revision 1.14  2005/11/28 13:50:47  georgeda
  * Defect #207, handle nulls for pages w/ uncontrolled vocab
  *
  * Revision 1.13  2005/11/18 22:50:02  georgeda
  * Defect #184.  Cleanup editing of old models
  *
  * Revision 1.12  2005/11/16 19:29:13  pandyas
  * modified typo in comments
  *
  * Revision 1.11  2005/11/03 13:59:10  georgeda
  * Fixed delete functionality
  *
  * Revision 1.10  2005/10/31 13:46:28  georgeda
  * Updates to handle back arrow
  *
  * Revision 1.9  2005/10/28 12:47:26  georgeda
  * Added delete functionality
  *
  * Revision 1.8  2005/10/27 19:42:05  georgeda
  * Cleanup
  *
  * Revision 1.7  2005/10/26 14:10:49  georgeda
  * Added other administrative route to therapy
  *
  * Revision 1.6  2005/10/25 19:42:15  georgeda
  * Finished Therapy page
  *
  * Revision 1.5  2005/10/20 20:40:21  pandyas
  * added javadocs
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AgentTarget;
 import gov.nih.nci.camod.domain.BiologicalProcess;
 import gov.nih.nci.camod.domain.ChemicalClass;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.webapp.form.TherapyForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class TherapyPopulateAction extends BaseAction
 {
 
     /**
      * Pre-populate all field values in the form <FormName> Used by <jspName>
      * 
      */
     public ActionForward populate(ActionMapping mapping,
                                   ActionForm form,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception
     {
 
         log.trace("Entering TherapyPopulateAction.populate");
 
         // Create a form to edit
         TherapyForm therapyForm = (TherapyForm) form;
 
         // Grab the current Therapy we are working with related to this
         // animalModel
         String aTherapyID = request.getParameter("aTherapyID");
 
         TherapyManager therapyManager = (TherapyManager) getBean("therapyManager");
         Therapy therapy = therapyManager.get(aTherapyID);
 
         if (therapy == null)
         {
             request.setAttribute(Constants.Parameters.DELETED, "true");
         }
         else
         {
             request.setAttribute("aTherapyID", aTherapyID);
 
             if (therapy.getTreatment() != null)
             {
                 if (therapy.getTreatment().getSexDistribution() != null)
                 {
                     therapyForm.setType(therapy.getTreatment().getSexDistribution().getType());
                 }
                 if (therapy.getTreatment().getAgeAtTreatment() != null)
                 {
                     therapyForm.setAgeAtTreatment(therapy.getTreatment().getAgeAtTreatment());
                     therapyForm.setAgeAtTreatmentUnit(therapy.getTreatment().getAgeAtTreatmentUnit());
                 }
 
                 therapyForm.setDosage(therapy.getTreatment().getDosage());
                 therapyForm.setDosageUnit(therapy.getTreatment().getDosageUnit());
 
                 if (therapy.getTreatment().getAdminRouteUnctrlVocab() !=null)
                 {
                     therapyForm.setAdministrativeRoute(Constants.Dropdowns.OTHER_OPTION);
                     therapyForm.setOtherAdministrativeRoute(therapy.getTreatment().getAdminRouteUnctrlVocab());
                 }
                 else
                 {
                     therapyForm.setAdministrativeRoute(therapy.getTreatment().getAdministrativeRoute());
                 }
             }
 
             //therapy.agent attribute
             therapyForm.setName(therapy.getAgent().getName());
 
             //agent attributes
             if (therapy.getAgent().getCasNumber() != null)
             {
                 therapyForm.setCasNumber(therapy.getAgent().getCasNumber());
             }
 
             if (therapy.getAgent().getNscNumber() != null)
             {
                 therapyForm.setNscNumber(therapy.getAgent().getNscNumber().toString());
             }
 
             // Therapy object attributes
             therapyForm.setToxicityGrade(therapy.getToxicityGrade());
             therapyForm.setBiomarker(therapy.getBiomarker());
             therapyForm.setExperiment(therapy.getExperiment());
             therapyForm.setResults(therapy.getResults());
             therapyForm.setTumorResponse(therapy.getTumorResponse());
             therapyForm.setComments(therapy.getComments());
 
             // Get the collection/Set of agent targets
             List<AgentTarget> theAgentTargetsList = new ArrayList(therapy.getAgent().getAgentTargetCollection());
             String[] theTargets = new String[theAgentTargetsList.size()];
             for (int i = 0; i < theAgentTargetsList.size(); i++)
             {
                 AgentTarget theAgentTarget = (AgentTarget) theAgentTargetsList.get(i);
                 theTargets[i] = theAgentTarget.getName();
             }
             therapyForm.setSelectedTargets(theTargets);
 
 
             // Get the collection/Set  of biological processes
             List theProcessesList = new ArrayList(therapy.getAgent().getBiologicalProcessCollection());
             String[] theProcesses = new String[theProcessesList.size()];
             for (int i = 0; i < theProcessesList.size(); i++)
             {
                 BiologicalProcess theBiologicalProcess = (BiologicalProcess) theProcessesList.get(i);
                 theProcesses[i] = theBiologicalProcess.getName();
             }
             therapyForm.setSelectedProcesses(theProcesses);
 
             // Get the collection/Set of Chemical Classes
             List theChemicalClassesList = new ArrayList(therapy.getAgent().getChemicalClassCollection());
             String[] theChemicalClasses = new String[theChemicalClassesList.size()];
             for (int i = 0; i < theChemicalClassesList.size(); i++)
             {
                 ChemicalClass theChemicalClass = (ChemicalClass) theChemicalClassesList.get(i);
                 theChemicalClasses[i] = theChemicalClass.getName();
             }
             therapyForm.setSelectedChemicalClasses(theChemicalClasses);
         }
 
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         this.dropdown(request, response);
         log.trace("Exiting TherapyPopulateAction.populate");
 
         return mapping.findForward("submitTherapy");
     }
 
     /**
     * Populate the dropdown menus for submitTherapy
      * 
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward dropdown(ActionMapping mapping,
                                   ActionForm form,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception
     {
 
         System.out.println("<TherapyPopulateAction dropdown> Entering ActionForward dropdown()");
 
         // setup dropdown menus
         this.dropdown(request, response);
 
         System.out.println("<TherapyPopulateAction dropdown> Exiting ActionForward dropdown()");
 
         return mapping.findForward("submitTherapy");
     }
 
     /**
      * Populate all drowpdowns for this type of form
      * 
      * @param request
      * @param response
      * @throws Exception
      */
     public void dropdown(HttpServletRequest request,
                          HttpServletResponse response) throws Exception
     {
 
         System.out.println("<TherapyPopulateAction dropdown> Entering void dropdown()");
 
         // Prepopulate all dropdown fields, set the global Constants to the
         // following
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.CHEMICALCLASSESDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.BIOLOGICALPROCESSDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.THERAPEUTICTARGETSDROP, "");
 
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.CHEMTHERAPYDOSEUNITSDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.AGEUNITSDROP, "");
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SEXDISTRIBUTIONDROP, Constants.Dropdowns.ADD_BLANK);
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.TOXICITYGRADESDROP, Constants.Dropdowns.ADD_BLANK);
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ADMINISTRATIVEROUTEDROP, Constants.Dropdowns.ADD_BLANK);
 
         System.out.println("<TherapyPopulateAction dropdown> Exiting void dropdown()");
 
     }
 }

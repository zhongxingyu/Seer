 /**
  * 
 * $Id: AdvancedSearchPopulateAction.java,v 1.16 2007-06-20 19:29:22 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.15  2007/05/21 17:33:45  pandyas
 * Modified simple and adv search species drop down to pull from DB (approved model species only)
 *
  * Revision 1.14  2007/05/18 14:40:49  pandyas
  * Modified species and organ tree to default to no organ button and to work when user goes from species to empty selection
  *
  * Revision 1.13  2007/05/16 12:29:35  pandyas
  * Modified adv and simple search vocab tree section to populate depending on species selected
  *
  * Revision 1.12  2007/03/28 18:22:28  pandyas
  * Modified for the following Test Track item:
  * #494 - Advanced search for Carcinogens for Jackson Lab data - removed code to populate drop downs now removed
  *
  * Revision 1.9  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.8  2006/05/10 14:15:39  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.7  2006/04/28 19:23:34  schroedn
  * Defect # 261
  * Prepopulates the advanced search page with the saved query data to edit
  *
  * Revision 1.6  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import java.util.Set;
 import gov.nih.nci.camod.domain.SavedQuery;
 import gov.nih.nci.camod.domain.SavedQueryAttribute;
 import gov.nih.nci.camod.domain.Species;
 import gov.nih.nci.camod.service.SavedQueryManager;
 import gov.nih.nci.camod.service.impl.SpeciesManagerSingleton;
 import gov.nih.nci.camod.webapp.form.SearchForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 import gov.nih.nci.camod.Constants;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 public class AdvancedSearchPopulateAction extends BaseAction {
 
     /**
      * Pre-populate all field values in the form <FormName> Used by <jspName>
      * 
      */
     public ActionForward populate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         log.info("In AdvancedSearchPopulateAction.populate");		       
 
         // Reset the non-simple-search options
         SearchForm theSearchForm = (SearchForm) form;
                 
         String aQueryId = request.getParameter( Constants.Parameters.QUERYID ); 
       
         //Populate form field with savedQuery criteria
         if ( aQueryId != null )
         {
             theSearchForm.allFieldsReset();
             
             //Set Constant aQueryId
             //This will be used to determine if we are editing a query 
             request.getSession().setAttribute( Constants.ASAVEDQUERYID, aQueryId );
                         
             SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
             
             try {                
                 SavedQuery inSavedQuery = savedQueryManager.get( aQueryId );
                 
                 log.info("<AdvancedSearchPopulateAction.populate> inSavedQuery.getQueryName(): " +inSavedQuery.getQueryName());
                 
                 request.getSession().setAttribute( Constants.QUERY_NAME, inSavedQuery.getQueryName() );
                 
                 // Retrieve the saved criteria and populate the fields in the SearchForm
                 Set<SavedQueryAttribute> inSavedQueryAttributeList = inSavedQuery.getSavedQueryAttributes();                                
                 savedQueryManager.buildSearchData( inSavedQueryAttributeList, theSearchForm );                
                 
             } catch ( Exception e) {
                 // Set the error message
                 ActionMessages msg = new ActionMessages();
                 msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
                 saveErrors(request, msg);
             }
         } else {
             
             //Clear out Constant aQueryId
             request.getSession().setAttribute( Constants.QUERY_NAME, null );
             request.getSession().setAttribute( Constants.ASAVEDQUERYID, null );
         }        
         
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYDROP,
                 Constants.Dropdowns.ADD_BLANK);
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.APPROVEDSPECIESDROP,
                 Constants.Dropdowns.ADD_BLANK_OPTION);
 
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.INDUCEDMUTATIONAGENTQUERYDROP,
                 Constants.Dropdowns.ADD_BLANK);        
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.EXTERNALSOURCEQUERYDROP,
                 Constants.Dropdowns.ADD_BLANK);   
 
         // The dropdown will be empty initially - populates whole list
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.CARCINOGENICAGENTSQUERYDROP, 
         		Constants.Dropdowns.ADD_BLANK);
     
         // Added so customized search will find selected name to display and allow other queries without CE to run
         if (theSearchForm.getCarcinogenicIntervention() != null){
         	NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP, 
         		theSearchForm.getCarcinogenicIntervention());        
         } else {
         	NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP, 
         			Constants.Dropdowns.ADD_BLANK);   	
         }
         return mapping.findForward("next");
     }
     
 	/**
 	 * Repopulate the Agent Name dropdown matching the agent type selected
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward setAgentNameDropdown(ActionMapping mapping,
 			ActionForm form, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 
         // Get the form used
         SearchForm theSearchForm = (SearchForm) form;
         
         log.info("<setAgentNameDropdown> theSearchForm.getCarcinogenicIntervention(): " + theSearchForm.getCarcinogenicIntervention());
         //Set constant for the Agent type here 
         request.getSession().setAttribute(Constants.ENVFactors.AGENT_TYPE, theSearchForm.getCarcinogenicIntervention());        
 
 		NewDropdownUtil.populateDropdown(request,
 				Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP, theSearchForm.getCarcinogenicIntervention());
 
 		return mapping.findForward("searchAdvanced");
 
 	}
 	
    public ActionForward setSpeciesForTrees(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
     	
     	String theSearchSpecies = null;    	
         SearchForm theSearchForm = (SearchForm) form;
         
         // Check if null - if user goes from species to empty this correctly redirects to screen
         if (theSearchForm.getSpecies() !=null && theSearchForm.getSpecies().length() > 0){        
 	        log.info("theSearchForm.getSpecies(): "+ theSearchForm.getSpecies());
 	        
 	        // Set selected species to a constant to determine which organ tree displays 
 	        // using common name because Rat has two species
 	        Species species = SpeciesManagerSingleton.instance().getByName(theSearchForm.getSpecies());
 	        theSearchSpecies = species.getCommonName();
 	        log.info("<setSpeciesForOrganTree> theSearchSpecies: "+ theSearchSpecies);
         } 
         
         request.getSession().setAttribute(Constants.SEARCHSPECIESCOMMONNAME, theSearchSpecies);
 
        NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.ZEBRAFISHDIAGNOSISDROP, Constants.Dropdowns.ADD_BLANK_AND_OTHER_OPTION);        

         return mapping.findForward("searchAdvanced");    	
     }	
 	
 }

 /**
  * 
 * $Id: SearchAction.java,v 1.15 2009-06-11 17:02:05 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.14  2009/06/11 16:43:57  pandyas
  * modified to test #21666  	Error when Table of Content -? Cardiovascular System is selected
  *
  * Revision 1.13  2008/02/08 16:48:24  pandyas
  * modified log statement for final deployment to QA
  *
  * Revision 1.12  2007/12/17 18:03:22  pandyas
  * Removed * in searchFilter used for getting e-mail from LDAP
  * Apps Support ticket was submitted (31169 - incorrect e-mail associated with my caMOD account) stating:
  *
  * Cheryl Marks submitted a ticket to NCICB Application Support in which she requested that the e-mail address associated with her account in the "User Settings" screen in caMOD be corrected. She has attempted to correct it herself, but because the program queries the LDAP Server for the e-mail address, her corrections were not retained.
  *
  * Revision 1.11  2007/10/03 16:54:20  pandyas
  * modified debug statement
  *
  * Revision 1.10  2007/08/27 15:39:53  pandyas
  * hide debug code printout
  *
  * Revision 1.9  2007/07/31 12:02:55  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.8  2006/10/17 16:11:00  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.7  2006/05/22 19:40:12  schroedn
  * Added log statement
  *
  * Revision 1.6  2006/05/11 15:43:05  schroedn
  * Updates number of results and excute time of SavedQueries on re-run or edit
  *
  * Revision 1.5  2006/05/10 14:15:39  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.4  2006/04/28 19:28:10  schroedn
  * Defect # 261
  * Added QueryHistory save on performing any search if a user is logged in
  *
  * Revision 1.3  2005/11/16 19:43:30  georgeda
  * Added clear to search forms
  *
  * Revision 1.2  2005/10/07 21:14:24  georgeda
  * Changed the forward action
  *
  * Revision 1.1  2005/10/03 13:52:04  georgeda
  * Search changes
  *
  * Revision 1.2  2005/09/16 15:52:56  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.SavedQuery;
 import gov.nih.nci.camod.domain.SavedQueryAttribute;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.service.PersonManager;
 import gov.nih.nci.camod.service.SavedQueryManager;
 import gov.nih.nci.camod.util.CriteriaTableUtil;
 import gov.nih.nci.camod.webapp.form.SearchForm;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 /**
  * 
  * Action used to implement the search for animal models
  * 
  */
 public final class SearchAction extends BaseAction {
 
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 
 		log.info("<SearchAction> entered ");
 
 		SearchForm theForm = new SearchForm();
 		SavedQuery resubmittedSavedQuery = null;
 
 		SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
 
 		String aQueryId = request.getParameter(Constants.Parameters.QUERYID);
 		String aSavedQueryId = (String) request.getSession().getAttribute(
 				Constants.ASAVEDQUERYID);
 
 		// Retrieve the query to resubmit
 		if (aSavedQueryId != null || aQueryId != null) {
 			try {
 
 				if (aQueryId != null) {
 					// Used for re-running query
 					resubmittedSavedQuery = savedQueryManager.get(aQueryId);
 
 					// From the saved criteria populate the fields in the
 					// simpleSearchForm
 					Set<SavedQueryAttribute> sqaList = resubmittedSavedQuery
 							.getSavedQueryAttributes();
 
 					// Retrieve the saved criteria and populate the fields in
 					// the SearchForm
 					savedQueryManager.buildSearchData(sqaList, theForm);
 
 					request.getSession().setAttribute(Constants.RERUN_QUERY,
 							"true");
 
 				} else {
 					// Used for editing query
 					resubmittedSavedQuery = savedQueryManager
 							.get(aSavedQueryId);
 					theForm = (SearchForm) form;
 
 					request.getSession().setAttribute(Constants.RERUN_QUERY,
 							null);
 				}
 
 			} catch (Exception e) {
 				// Set the error message
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 						"errors.admin.message"));
 				saveErrors(request, msg);
 			}
 		} else {
 			// Normal Search
 			theForm = (SearchForm) form;
 			request.getSession().setAttribute(Constants.QUERY_NAME, null);
 			request.getSession().setAttribute(Constants.ASAVEDQUERYID, null);
 			request.getSession().setAttribute(Constants.RERUN_QUERY, null);
 		}
 
 		if (theForm.getKeyword() != null && theForm.getKeyword().length() > 0) {
 			log.info("<SearchAction> Keyword: " + theForm.getKeyword());
 			request.getSession().setAttribute(Constants.KEYWORD_HIGHLIGHT,
 					theForm.getKeyword());
 		}
 
 		String theAction = (String) request
 				.getParameter(Constants.Parameters.ACTION);
 		String theForward = "next";
 
 		// Clear the form
 		if ("Clear".equals(theAction)) {
 			theForm.allFieldsReset();
 			theForward = "back";
 		}
 
 		// Do the search
 		else {
 
 			try {
 				log.info("<SearchAction> In search loop: ");
 				AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
 
 				// Calculate the elasped time of search
 				long start = System.currentTimeMillis();
 				log.info("<SearchAction> start: " + start);
 
 				// Perform the search
 				List results = animalModelManager.search(theForm);
 
 				long elapsedTimeMillis = System.currentTimeMillis() - start;
 				log.info("<SearchAction> elapsedTimeMillis: " + elapsedTimeMillis);
 
 				// Set search results constant
 				request.getSession().setAttribute(Constants.SEARCH_RESULTS,
 						results);
 
 				// Create new QUERY HISTORY item
 				SavedQuery savedQuery = new SavedQuery();
 				savedQuery.setSearchData(theForm);
 				savedQuery.setElapsedTime(elapsedTimeMillis);
 				savedQuery.setExecuteTime(new Date());
 				savedQuery.setIsSaved(new Long(0));
 				savedQuery.setNumberResults(results.size());
 
 				log
 						.info("<SearchAction>: savedQuery.setQueryName( No Name Provided )");
 				savedQuery.setQueryName("No Name Provided");
 
 				if (resubmittedSavedQuery != null) {
 					savedQuery.setQueryName(resubmittedSavedQuery
 							.getQueryName());
 
 					// Update the number of results and last executed time
 					resubmittedSavedQuery.setElapsedTime(savedQuery
 							.getElapsedTime());
 					resubmittedSavedQuery.setExecuteTime(savedQuery
 							.getExecuteTime());
 					resubmittedSavedQuery.setNumberResults(results.size());
 					savedQueryManager.update(resubmittedSavedQuery);
 
 					// Set Global Constants for use later
 					request.getSession().setAttribute(Constants.QUERY_NAME,
 							resubmittedSavedQuery.getQueryName());
 					request.getSession().setAttribute(Constants.ASAVEDQUERYID,
 							resubmittedSavedQuery.getId().toString());
 				}
 
 				// Get elapsed time in seconds
 				request.getSession().setAttribute(Constants.ELAPSED_TIME,
 						savedQuery.getElapsedTime());
 				request.getSession().setAttribute(Constants.EXECUTE_TIME,
 						savedQuery.getExecuteTime());
 
 				// If a user is logged in, save the query to the history
 				String currentUser = (String) request.getSession()
 						.getAttribute(Constants.CURRENTUSER);
 				
 				log.info("<SearchAction>: currentUser: " + currentUser);				
 
 				if (currentUser != null && currentUser.length() >0) {
 					// Set the user for this save
 					PersonManager personManager = (PersonManager) getBean("personManager");
 					savedQuery.setUser(personManager
 							.getByUsername((String) request.getSession()
 									.getAttribute(Constants.CURRENTUSER)));
 					// Add criteria to be saved. Create a List of
 					// SavedQueryAttributes from inQuery.getSearchData()
 					Set<SavedQueryAttribute> criteriaList = savedQueryManager
 							.buildCriteriaList(theForm);
 					savedQuery.setSavedQueryAttributes(criteriaList);
 					// Save search to query history
 					savedQueryManager.save(savedQuery);
 				}
 
 				// Save the current form data into constants
 				request.getSession().setAttribute(Constants.SEARCH_FORM,
 						theForm);
 
 			} catch (Exception e) {
 				log.info(e);
 				// Set the error message
 				ActionMessages msg = new ActionMessages();
 				msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 						"errors.admin.message"));
 				saveErrors(request, msg);
 			}
 		}
 
 		// Create the Criteria HTML table to be display on the search result
 		// page
 		try {
 			request.getSession().setAttribute(Constants.CRITERIATABLE,
 					CriteriaTableUtil.buildCriteriaDisplayTable(theForm));
 
 			request.getSession().setAttribute(Constants.DUP_NAME, "false");
 
 			request.getSession().setAttribute(Constants.NOSAVEOPTION, "false");
 
 		} catch (Exception e) {
 			// Set the error message
 			ActionMessages msg = new ActionMessages();
 			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 					"errors.admin.message"));
 			saveErrors(request, msg);
 		}
 		log.info("<SearchAction>: Exiting: " );
 		return mapping.findForward(theForward);
 	}
 }

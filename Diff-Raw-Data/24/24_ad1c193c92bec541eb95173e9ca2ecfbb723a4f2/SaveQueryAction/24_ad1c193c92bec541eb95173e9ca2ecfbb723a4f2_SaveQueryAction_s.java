 /**
  * 
 * $Id: SaveQueryAction.java,v 1.2 2006-05-10 14:15:39 schroedn Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2006/04/28 19:26:58  schroedn
  * Defect # 261
  * Saves / Updates / Deletes SaveQuery items
  *
  *
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.SavedQuery;
 import gov.nih.nci.camod.domain.SavedQueryAttribute;
 import gov.nih.nci.camod.service.PersonManager;
 import gov.nih.nci.camod.service.SavedQueryManager;
 import gov.nih.nci.camod.webapp.form.SaveQueryForm;
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
  * @arthur schroedn
  */
 public class SaveQueryAction extends BaseAction
 {
     /**
      * Save builds the SavedQuery and saves it
      * 
      * @param mapping
      * @param form
      * @param request 
      * @param response
      * 
      * @exception IOException
      * @exception ServletException
      *                when anything goes wrong.
      */
     public ActionForward save(ActionMapping mapping,
                               ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException
     {
 
         log.debug("Entering SaveQueryAction.save");
 
         SaveQueryForm theForm = (SaveQueryForm) form;
         SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
         
         List results = (List) request.getSession().getAttribute(Constants.SEARCH_RESULTS);
         request.getSession().setAttribute(Constants.NOSAVEOPTION, "true");
         
         String theForward = "noSaveOption";
 
         try
         {
             if (theForm.getSaveAsNew().equals("no"))
             {
                 // Update Saved Query
                 log.debug("Updating SavedQuery");
 
                 request.getSession().setAttribute(Constants.DUP_NAME, "false");
 
                 String aSavedQueryId = (String) request.getSession().getAttribute(Constants.ASAVEDQUERYID);
                 SavedQuery theQueryToUpdate = savedQueryManager.get(aSavedQueryId);
 
                 theQueryToUpdate.setElapsedTime((Long) request.getSession().getAttribute(Constants.ELAPSED_TIME));
                 theQueryToUpdate.setExecuteTime((Date) request.getSession().getAttribute(Constants.EXECUTE_TIME));
                 theQueryToUpdate.setNumberResults(results.size());
 
                 SearchForm aSearchForm = (SearchForm) request.getSession().getAttribute(Constants.SEARCH_FORM);
                 Set<SavedQueryAttribute> criteriaList = theQueryToUpdate.getSavedQueryAttributes();
                 criteriaList.clear();
                 criteriaList.addAll(savedQueryManager.buildCriteriaList(aSearchForm));
                 
                 savedQueryManager.update(theQueryToUpdate);
             }
 
             if (theForm.getSaveAsNew().equals("yes"))
             {
                 log.debug("Saving new SavedQuery");
 
                 // Create NEW Saved Query               
                 SavedQuery theQueryToSave = new SavedQuery();
 
                 boolean duplicateName = false;
                 request.getSession().setAttribute(Constants.DUP_NAME, "false");
 
                 List allSaved = savedQueryManager.getSavedQueriesByUsername((String) request.getSession().getAttribute( Constants.CURRENTUSER ) );
                 for (int i = 0; i < allSaved.size(); i++)
                 {
                     SavedQuery theSavedQuery = (SavedQuery) allSaved.get(i);
                     if (theForm.getQueryName().equals(theSavedQuery.getQueryName()))
                     {
                         log.debug( "Query name is a duplicate" );
                         
                         duplicateName = true;
                         request.getSession().setAttribute(Constants.DUP_NAME, "true");
                        request.getSession().setAttribute(Constants.NOSAVEOPTION, "false");                       
                         theForward = "next";
                     }
                 }
 
                 if (duplicateName == false)
                 {
                     theQueryToSave.setQueryName(theForm.getQueryName());
                     theQueryToSave.setIsSaved(1l);
 
                     theQueryToSave.setElapsedTime((Long) request.getSession().getAttribute(Constants.ELAPSED_TIME));
                     theQueryToSave.setExecuteTime((Date) request.getSession().getAttribute(Constants.EXECUTE_TIME));
                     theQueryToSave.setNumberResults(results.size());
 
                     // Add list of criteria
                     Set<SavedQueryAttribute> criteriaList = savedQueryManager.buildCriteriaList((SearchForm) request.getSession().getAttribute( Constants.SEARCH_FORM));
                     theQueryToSave.setSavedQueryAttributes(criteriaList);
 
                     //Set the user for this save
                     PersonManager inPersonManager = (PersonManager) getBean("personManager");
                     theQueryToSave.setUser(inPersonManager.getByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER)));
 
                     savedQueryManager.save(theQueryToSave);
 
                     // Used for sidebar, update the number of saved queries for current user
                     List savedQueriesList = savedQueryManager.getSavedQueriesByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER));
                     
                     request.getSession().setAttribute(Constants.NUMBEROFSAVEDQUERIES, String.valueOf(savedQueriesList.size()));
                     request.getSession().setAttribute(Constants.QUERY_NAME, theForm.getQueryName());
                 }                
             }
         }
         catch (Exception e)
         {
             // Set the error message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
 
         return mapping.findForward(theForward);
     }
 
     /**
      * Returns User Queries
      * 
      * @param mapping
      * @param form
      * @param request 
      * @param response
      * 
      * @exception IOException
      * @exception ServletException
      *                when anything goes wrong.
      */
     public ActionForward returnUserQueries(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws IOException, ServletException
     {
 
         log.debug("Entering SaveQueryAction.returnUserQueries");
 
         String theForward = "searchQueryHistory";
 
         try
         {
             SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
             List savedQueriesList = savedQueryManager.getAllByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER));
             request.getSession().setAttribute(Constants.USERSQUERYLIST, savedQueriesList);
 
         }
         catch (Exception e)
         {
 
             // Set the error message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
 
         return mapping.findForward(theForward);
     }
 
     /**
      * Returns Saved User Queries
      * 
      * @param mapping
      * @param form
      * @param request 
      * @param response
      * 
      * @exception IOException
      * @exception ServletException
      *                when anything goes wrong.
      */
     public ActionForward returnSavedUserQueries(ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws IOException, ServletException
     {
 
         log.debug("Entering SaveQueryAction.returnSavedUserQueries");
 
         String theForward = "searchSavedQueries";
 
         try
         {
             SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
             List savedQueriesList = savedQueryManager.getSavedQueriesByUsername((String) request.getSession().getAttribute( Constants.CURRENTUSER));
             request.getSession().setAttribute(Constants.USERSAVEDQUERYLIST, savedQueriesList);
         }
         catch (Exception e)
         {
 
             // Set the error message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
 
         return mapping.findForward(theForward);
     }
 
     /**
      * Delete a SavedQuery
      * 
      * @param mapping
      * @param form
      * @param request 
      * @param response
      * 
      * @exception IOException
      * @exception ServletException
      *                when anything goes wrong.
      */
     public ActionForward delete(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, ServletException
     {
 
         log.debug("Entering SaveQueryAction.delete");
 
         String theForward = "querydeleted";
 
         SavedQueryManager savedQueryManager = (SavedQueryManager) getBean("savedQueryManager");
 
         String aQueryId = request.getParameter( Constants.Parameters.QUERYID );
 
         try
         {
             //Delete from db
             savedQueryManager.remove(aQueryId);
 
             // Used for sizebar, update the number of saved queries for current user
             List savedQueriesList = savedQueryManager.getSavedQueriesByUsername((String) request.getSession().getAttribute(
                                                                                                                            Constants.CURRENTUSER));
             request.getSession().setAttribute(Constants.NUMBEROFSAVEDQUERIES, String.valueOf(savedQueriesList.size()));
 
             //Add deletion successful message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("delete.successful"));
             saveErrors(request, msg);
 
         }
         catch (Exception e)
         {
 
             // Set the error message
             ActionMessages msg = new ActionMessages();
             msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("errors.admin.message"));
             saveErrors(request, msg);
         }
 
         return mapping.findForward(theForward);
     }
 }

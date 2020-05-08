 /**
  * 
 * $Id: CustomizeSearchResultsAction.java,v 1.3 2006-05-10 17:09:16 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.2  2006/05/10 14:15:39  schroedn
 * New Features - Changes from code review
 *
  * Revision 1.1  2006/04/28 19:25:17  schroedn
  * Defect # 238
  * Saves / Updates user settings for search result columns
  *
  *
  */
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.ResultSettings;
 import gov.nih.nci.camod.domain.ResultSettingsColumns;
 import gov.nih.nci.camod.service.PersonManager;
 import gov.nih.nci.camod.service.ResultSettingsManager;
 import gov.nih.nci.camod.webapp.form.CustomizeSearchResultsForm;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * 
  * @author schroedn
  *
  */
 public class CustomizeSearchResultsAction extends BaseAction
 {
     /**
      * Saves or updates user Search Result settings
      * 
      */
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception
     {
 
         log.debug("Entering CustomizeSearchResultsAction.execute");
 
         // Create a form to edit
         CustomizeSearchResultsForm customResultsForm = (CustomizeSearchResultsForm) form;
 
         // Update your Settings        
         ResultSettingsManager resultSettingsManager = (ResultSettingsManager) getBean("resultSettingsManager");
 
         // Get ResultSettings by username
         ResultSettings rSettings = resultSettingsManager.getByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER));
 
         Set<ResultSettingsColumns> columnList = new HashSet<ResultSettingsColumns>();
         boolean containsModelDescriptor = false;
 
         String[] columns = customResultsForm.getSelectedColumnsToDisplay();
         for (int i = 0; i < columns.length; i++)
         {
             ResultSettingsColumns resultColumn = new ResultSettingsColumns();
             resultColumn.setColumnName(columns[i]);
             if (columns[i].equals("Model Descriptor"))
                 containsModelDescriptor = true;
 
             columnList.add(resultColumn);
         }
 
         // Check Max Number of Columns
         if (columnList.size() > 5)
         {
             request.getSession().setAttribute(Constants.ERRORMESSAGE, "* Too many columns selected (5 max)");
             return mapping.findForward("failed");
         }
 
         // Check Model Descriptor (required)
         if (!containsModelDescriptor)
         {
             request.getSession().setAttribute(Constants.ERRORMESSAGE, "* Model Descriptor is a required column");
             return mapping.findForward("failed");
         }
 
         // Save to database
         if (rSettings == null)
         {
             // Save new if no settings are found
             rSettings = new ResultSettings();
 
             //rSettings.setColumnsToDisplay( strColumns );
             rSettings.setItemsPerPage(Integer.parseInt(customResultsForm.getItemsPerPage().trim()));
             rSettings.setResultSettingsColumns(columnList);
 
             // Set the current user
             PersonManager personManager = (PersonManager) getBean("personManager");
             rSettings.setUser(personManager.getByUsername((String) request.getSession().getAttribute(Constants.CURRENTUSER)));
 
             resultSettingsManager.save(rSettings);
         }
         else
         {
             // Update settings
             Set<ResultSettingsColumns> resultColumnList = rSettings.getResultSettingsColumns();            
             resultColumnList.clear();
             resultColumnList.addAll(columnList);
             
             rSettings.setItemsPerPage(Integer.parseInt(customResultsForm.getItemsPerPage().trim()));
             
             resultSettingsManager.update(rSettings);
         }
 
         // Print out what is being saved
         request.getSession().setAttribute(Constants.ITEMSPERPAGE, customResultsForm.getItemsPerPage());
         request.getSession().setAttribute(Constants.SEARCHRESULTCOLUMNS, customResultsForm.getSelectedColumnsToDisplay());
        
        // If the columns have changed, then we will require a new search
        request.getSession().setAttribute( Constants.SEARCH_RESULTS, null );
        
         // Get the attributes from the request
         String theForward = "success";
 
         log.debug("Forwarding to model section: " + theForward);
         log.debug("Exiting CustomizeSearchResultsAction.execute");
 
         return mapping.findForward(theForward);
     }
 }

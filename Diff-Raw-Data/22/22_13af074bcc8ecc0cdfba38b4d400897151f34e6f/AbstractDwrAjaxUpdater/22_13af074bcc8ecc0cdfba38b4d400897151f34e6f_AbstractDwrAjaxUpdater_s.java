 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.ajax;
 
 import gov.nih.nci.caintegrator.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator.web.DisplayableUserWorkspace;
 import gov.nih.nci.caintegrator.web.SessionHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * Abstract class for creating dynamically updated reverse-ajax pages.
  */
 public abstract class AbstractDwrAjaxUpdater {
 
     /**
      * Used to show a loading symbol.
      */
    protected static final String AJAX_LOADING_GIF = "<img src=\"images/ajax-loader.gif\"/>";
     private WorkspaceService workspaceService;
     private DwrUtilFactory dwrUtilFactory;
 
     /**
      * {@inheritDoc}
      */
     public void initializeJsp() {
         WebContext wctx = WebContextFactory.get();
         DisplayableUserWorkspace workspace = (DisplayableUserWorkspace)
                         wctx.getSession().getAttribute("displayableWorkspace");
         if (StringUtils.isNotBlank(SessionHelper.getUsername()) && !SessionHelper.isAnonymousUser()) {
             workspace.refresh(workspaceService, true);
             associateJobWithSession(dwrUtilFactory, workspace.getUserWorkspace().getUsername(),
                                     new Util(wctx.getScriptSession()));
             initializeDynamicTable(workspace);
         }
     }
 
     /**
      * Abstract class which is used when initializing the JSP to associate a job type with
      * a username on the session.
      * @param utilFactory global object which stores the map of username -> Util objects.
      * @param username current users username.
      * @param util dwr util object.
      */
     protected abstract void associateJobWithSession(DwrUtilFactory utilFactory,
                                                      String username,
                                                      Util util);
 
     /**
      * For the dynamic table to initialize which shows the status of the objects.
      * @param workspace current users workspace.
      */
     protected abstract void initializeDynamicTable(DisplayableUserWorkspace workspace);
 
     /**
      * Retrieves table options for DWR created tables.
      * @param counter - to switch it from odd/even rows in a table.
      * @return dwr table row options.
      */
     protected String retrieveRowOptions(int counter) {
         String bgcolor = "#dcdcdc";
         if (counter % 2 == 0) {
             bgcolor = "fff";
         }
         return "{ rowCreator:function(options) { "
             + " var row = document.createElement(\"tr\");"
             + " row.style.background=\"" + bgcolor + "\";"
             + "return row;"
             + "},"
             + "cellCreator:function(options) { "
             + "var cellNumber = options.cellNum;"
             + "var cellID = 'dwrCellID_' + cellNumber;"
             + "var td = document.createElement(\"td\");"
             + "td.setAttribute('id',cellID) ;"
             + "return td;"
             + "},"
             + " escapeHtml:false }";
     }
 
     /**
      * Given a date it returns the formatted date string.
      * @param date to convert to string.
      * @return string conversion.
      */
     protected String getDateString(Date date) {
         return date == null ? null : new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(date);
     }
 
     /**
      * @param username for dwr util.
      * @return dwr util.
      */
     protected abstract Util getDwrUtil(String username);
 
     void refreshJsp(String username) {
         getDwrUtil(username).addFunctionCall("location.reload(true)");
     }
 
     /**
      * @return the workspaceService
      */
     public WorkspaceService getWorkspaceService() {
         return workspaceService;
     }
 
     /**
      * @param workspaceService the workspaceService to set
      */
     public void setWorkspaceService(WorkspaceService workspaceService) {
         this.workspaceService = workspaceService;
     }
 
     /**
      * @return the dwrUtilFactory
      */
     public DwrUtilFactory getDwrUtilFactory() {
         return dwrUtilFactory;
     }
 
     /**
      * @param dwrUtilFactory the dwrUtilFactory to set
      */
     public void setDwrUtilFactory(DwrUtilFactory dwrUtilFactory) {
         this.dwrUtilFactory = dwrUtilFactory;
     }
 
 }

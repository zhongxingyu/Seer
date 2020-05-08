 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/svn/osp/trunk/reports/tool/src/java/org/theospi/portfolio/reports/tool/ReportsTool.java $
  * $Id:ReportsTool.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2005, 2006 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.reports.tool;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.*;
 import org.sakaiproject.api.app.scheduler.JobDetailWrapper;
 import org.sakaiproject.api.app.scheduler.SchedulerManager;
 import org.sakaiproject.api.app.scheduler.TriggerWrapper;
 import org.sakaiproject.authz.api.PermissionsHelper;
 import org.sakaiproject.component.app.scheduler.JobDetailWrapperImpl;
 import org.sakaiproject.component.app.scheduler.TriggerWrapperImpl;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.FilePickerHelper;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.exception.*;
 import org.sakaiproject.metaobj.shared.mgt.IdManager;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.tool.api.Tool;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.reports.service.ReportExecutionException;
 import org.sakaiproject.reports.service.ReportFunctions;
 import org.sakaiproject.reports.service.ReportsManager;
 import org.sakaiproject.reports.model.Report;
 import org.sakaiproject.reports.model.ReportDefinition;
 import org.sakaiproject.reports.model.ReportResult;
 import org.sakaiproject.reports.tool.DecoratedReport;
 import org.sakaiproject.reports.tool.DecoratedReportDefinition;
 import org.sakaiproject.reports.tool.DecoratedReportParam;
 import org.sakaiproject.reports.tool.DecoratedReportResult;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * This class is the controller and model to the jsp view.<BR>
  * <p/>
  * There is an inner class for allowing the report data classes to
  * interact with the jsp.<BR>
  * <p/>
  * Each session gets its own ReportsTool.<BR><BR>
  * <p/>
  * &nbsp; &nbsp; Testing procedures:<BR>
  * <p/>
  * Test the different parameter types
  * Make sure the sql param is pulling data
  * Test a live and a non-live report
  * Save the results
  * Re-run a live report, save results
  * External dependencies:
  * worksite, users, tool,
  *
  * @author andersjb
  */
 
 public class ReportsTool {
 
     private ResourceLoader toolBundle;
     private String jobName;
     private String triggerName;
     private String triggerExpression;
     private JobDetail jobDetail = null;
     private JobDetailWrapper jobDetailWrapper = new JobDetailWrapperImpl();
     private boolean isSelectAllTriggersSelected = false;
     private List filteredTriggersWrapperList;
     /**
      * A singlton manager for reports
      */
     private ReportsManager reportsManager = null;
     private SchedulerManager schedulerManager;
     /**
      * The reports to which the user has access
      */
     private List decoratedReportDefinition = null;
 
     /**
      * The reportDefinition from which the tool is working with
      */
     private DecoratedReportDefinition workingReportDefinition = null;
 
     /**
      * The report from which the tool is working with
      */
     private DecoratedReport workingReport = null;
 
     /**
      * The reportresult from which the tool is working with
      */
     private DecoratedReportResult workingResult = null;
 
     private Site worksite = null;
 
     private Tool tool = null;
     private Map userCan = null;
 
     protected static final String mainPage = "main";
     protected static final String createReportPage = "processCreateReport";
     protected static final String createReportParamsPage = "processCreateReportParams";
     protected static final String reportResultsPage = "showReportResults";
     protected static final String saveResultsPage = "saveReportResults";
     protected static final String importReportDef = "importReportDef";
     protected static final String shareReportResult = "shareReportResult";
     protected static final String scheduleReport = "scheduleReport";
     protected static final String createTrigger = "createTrigger";
     protected static final String deleteTriggers = "deleteTriggers";
 
     private static final String CRON_CHECK_ASTERISK = "**";
     private static final String CRON_CHECK_QUESTION_MARK = "??";
     //	import variables
     private String importFilesString = "";
     private List importFiles = new ArrayList();
 
     private ContentHostingService contentHosting;
 
     protected final Log logger = LogFactory.getLog(getClass());
     private IdManager idManager;
 
     private boolean invalidImport = false;
     private boolean invalidXslFile = false;
     private String invalidImportMessage = "Invalid Report Definition XML File";
 
     public SchedulerManager getSchedulerManager() {
         return schedulerManager;
     }
 
     public void setSchedulerManager(SchedulerManager schedulerManager) {
         this.schedulerManager = schedulerManager;
     }
 
     public String getInvalidImportMessage() {
         return invalidImportMessage;
     }
 
     public void setInvalidImportMessage(String invalidImportMessage) {
         this.invalidImportMessage = invalidImportMessage;
     }
 
     public boolean isInvalidImport() {
         return invalidImport;
     }
 
     public void setInvalidImport(boolean invalidImport) {
         this.invalidImport = invalidImport;
     }
 
     public boolean isInvalidXslFile() {
         return invalidXslFile;
     }
 
     public void setInvalidXslFile(boolean invalidXslFile) {
         this.invalidXslFile = invalidXslFile;
     }
 
     public IdManager getIdManager() {
         return idManager;
     }
 
     public void setIdManager(IdManager idManager) {
         this.idManager = idManager;
     }
 
     /**
      * when a live report is saved, tell the user
      */
     private boolean savedLiveReport = false;
 
 
     /**
      * getter for the ReportsManager property
      *
      * @return ReportsManager
      */
     public ReportsManager getReportsManager() {
         return reportsManager;
     }
 
     /**
      * setter for the ReportsManager property
      *
      * @param reportsManager
      */
     public void setReportsManager(ReportsManager reportsManager) {
         this.reportsManager = reportsManager;
     }
 
     public Tool getTool() {
         if (tool == null) {
             tool = ToolManager.getCurrentTool();
         }
         return tool;
     }
 
     public void setTool(Tool tool) {
         this.tool = tool;
     }
 
     public Site getWorksite() {
         if (worksite == null) {
             try {
                 worksite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
             }
             catch (IdUnusedException e) {
                 throw new RuntimeException(e);
             }
         }
         return worksite;
     }
 
     public String getReportFunctionPrefix() {
         return ReportFunctions.REPORT_FUNCTION_PREFIX;
     }
 
     public String getPermissionsMessage() {
         return getMessageFromBundle("perm_description", new Object[]{
                 getTool().getTitle(), getWorksite().getTitle()});
     }
 
     public void setWorkingReportDefinition(DecoratedReportDefinition workingReportDefinition) {
         this.workingReportDefinition = workingReportDefinition;
     }
 
     /**
      * getter for the WorkingReportDefinition property
      *
      * @return DecoratedReportDefinition
      */
     public DecoratedReportDefinition getWorkingReportDefinition() {
         return workingReportDefinition;
     }
 
     /**
      * setter for the Working Report
      *
      * @param workingReport DecoratedReport
      */
     public void setWorkingReport(DecoratedReport workingReport) {
         this.workingReport = workingReport;
     }
 
     /**
      * getter for the WorkingReport property
      *
      * @return DecoratedReport
      */
     public DecoratedReport getWorkingReport() {
         return workingReport;
     }
 
     public void setWorkingResult(DecoratedReportResult workingResult) {
         this.workingResult = workingResult;
         getReportsManager().setCurrentResult(workingResult.getReportResult());
     }
 
     /**
      * getter for the WorkingReport property
      *
      * @return DecoratedReport
      */
     public DecoratedReportResult getWorkingResult() {
         return workingResult;
     }
 
     /**
      * This method gets the list of reports encapsulated by
      * DecoratedReportDefinition.
      *
      * @return List of DecoratedReportDefinition
      */
     public List getReports() {
         List reportDefinitions = reportsManager.getReportDefinitions();
         decoratedReportDefinition = new ArrayList();
 
         Iterator iter = reportDefinitions.iterator();
         while (iter.hasNext()) {
             ReportDefinition reportDef = (ReportDefinition) iter.next();
             decoratedReportDefinition.add(new DecoratedReportDefinition(reportDef, this));
         }
         return decoratedReportDefinition;
     }
 
     public List getResults() {
         List decoratedResults = new ArrayList();
 
         List results = reportsManager.getCurrentUserResults();
 
         List tempResults = reportsManager.getReportsByViewer();
 
         Iterator iter = results.iterator();
         while (iter.hasNext()) {
             Object rr = iter.next();
 
             if (rr instanceof ReportResult)
                 decoratedResults.add(new DecoratedReportResult((ReportResult) rr, this));
             else if (rr instanceof Report)
                 decoratedResults.add(new DecoratedReport((Report) rr, this));
         }
 
         for (Iterator i = tempResults.iterator(); i.hasNext();) {
             decoratedResults.add(new DecoratedReportResult((ReportResult) i.next(), this));
 
         }
         return decoratedResults;
     }
 
     /**
      * Tells the interface if the live report was saved.  it goes to false
      * after the message is complete.
      */
     public boolean getSavedLiveReport() {
         boolean saved = savedLiveReport;
         return saved;
     }
 
     //***********************************************************
     //***********************************************************
     //	Actions for the JSP
 
 
     /**
      * An action called from the JSP through the JSF framework.
      * This is called when the user wants to move to the next screen
      *
      * @return String the next page
      */
     public String processReportBaseProperties() {
         String nextPage = ReportsTool.createReportParamsPage;
 
         //	ensure that there is a title for the report
         if (getWorkingReport().testInvalidateTitle())
             nextPage = "";
 
         if (getWorkingReport().getReportParams().size() > 0){
             return nextPage;
         }else {
             return processEditParamsContinue();
         }
 
     }
 
     /**
      * An action called from the JSP through the JSF framework.
      * Called when the user wants to stop creating a new report
      *
      * @return String the next page
      */
     public String processCancelReport() {
         savedLiveReport = false;
 
         //	remove the working report
         setWorkingReport(null);
 
         return ReportsTool.mainPage;
     }
 
     public String processCancelExport() {
         savedLiveReport = false;
         return ReportsTool.reportResultsPage;
     }
 
     /**
      * This goes from entering the parameter values to the results page
      *
      * @return Next page
      */
     public String processEditParamsContinue() {
         //check that the parameters are all good
         if (!getWorkingReport().getParamsAreValid()) {
 
             String msg = "";
             for (Iterator iter = getWorkingReport().getReportParams().iterator(); iter.hasNext();) {
                 DecoratedReportParam drp = (DecoratedReportParam) iter.next();
 
                 if (!drp.getIsValid()) {
                     if (msg.length() != 0)
                         msg += "<BR />";
                    msg += getMessageFromBundle("badParam_start");
                    msg += drp.getReportDefinitionParam().getTitle();
                    msg += getMessageFromBundle("badParam_mid");
                    if (drp.getIsString())
                        msg += getMessageFromBundle("badParam_string_reason");
                    if (drp.getIsInteger())
                        msg += getMessageFromBundle("badParam_int_reason");
                    if (drp.getIsFloat())
                        msg += getMessageFromBundle("badParam_float_reason");
                    if (drp.getIsDate())
                        msg += getMessageFromBundle("badParam_date_reason");
                    msg += getMessageFromBundle("badParam_end");
                 }
             }
             getWorkingReport().setParamErrorMessages(msg);
             return "";
         }
 
         try {
             //	get the results
             ReportResult result = reportsManager.generateResults(getWorkingReport().getReport());
 
             //	make it the working result
             setWorkingResult(new DecoratedReportResult(result, this));
 
             //	go to the results page
             return reportResultsPage;
         } catch (ReportExecutionException ree) {
             getWorkingReport().setParamErrorMessages(getMessageFromBundle("run_report_problem"));
             return "";
         }
     }
 
    public String processEditParamsBack() {
         return createReportPage;
     }
 
     public String processChangeViewXsl() {
         savedLiveReport = false;
         return reportResultsPage;
     }
 
     /**
      * We want to use an action to forward to the helper.  We don't want
      * to forward to the permission helper in the jsp beause we need to
      * clear out the cached permissions
      *
      * @return String unused
      */
     public String processPermissions() {
         ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
 
         userCan = null;
 
 
         try {
             String url = "sakai.permissions.helper.helper/tool?" +
                     "session." + PermissionsHelper.DESCRIPTION + "=" +
                     getPermissionsMessage() +
                     "&session." + PermissionsHelper.TARGET_REF + "=" +
                     getWorksite().getReference() +
                     "&session." + PermissionsHelper.PREFIX + "=" +
                     getReportFunctionPrefix();
 
             context.redirect(url);
         }
         catch (IOException e) {
             throw new RuntimeException("Failed to redirect to helper", e);
         }
         return null;
     }
 
     /**
      * An action called from the JSP through the JSF framework.
      *
      * @return String the next page
      */
     public String gotoOptions() {
         return mainPage;
     }
 
     public String processSaveResults() {
         savedLiveReport = false;
         return saveResultsPage;
     }
 
     public String processCancelSave() {
         savedLiveReport = false;
         return reportResultsPage;
     }
 
     public String processSaveResultsToDB() {
         savedLiveReport = false;
         reportsManager.saveReportResult(getWorkingResult().getReportResult());
 
         return reportResultsPage;
     }
 
     public String processSaveResultsToResources(DecoratedReportResult reportResult) throws IOException {
         ReportResult result = reportsManager.loadResult(reportResult.getReportResult());
         String fileName = reportsManager.processSaveResultsToResources(result);
         if (fileName.length() > 0) {
             FacesContext.getCurrentInstance().addMessage(null, getFacesMessageFromBundle("resource_saved", new Object[]{fileName}));
         }
         return ReportsTool.mainPage;
     }
 
     public String processSaveReport() {
         reportsManager.saveReport(getWorkingResult().getReportResult().getReport());
         savedLiveReport = true;
         return reportResultsPage;
     }
 
     /**
      * this function loads the full report result and the report
      * sets these in the tool
      *
      * @return String which page to go to next
      */
     public String processSelectReportResult(DecoratedReportResult reportResult) {
         ReportResult result = reportsManager.loadResult(reportResult.getReportResult());
         Report report = result.getReport();
 
         setWorkingReport(new DecoratedReport(report, this));
         setWorkingResult(new DecoratedReportResult(result, this));
 
         return ReportsTool.reportResultsPage;
     }
 
     /**
      * this function loads a live report.  It generates a new result,
      * sets the report as having been saved (aka, it was loaded from the db)
      *
      * @param report DecoratedReport
      * @return String the next page
      */
     public String processSelectLiveReport(DecoratedReport report) {
         ReportResult result = reportsManager.generateResults(report.getReport());
 
         result.getReport().setIsSaved(true);
 
         //	make it the working result
         setWorkingReport(new DecoratedReport(result.getReport(), this));
         setWorkingResult(new DecoratedReportResult(result, this));
 
         return ReportsTool.reportResultsPage;
     }
 
     /**
      * When deleting a report result, delete the report result...
      * then if the report is not live, then delete the report as well
      *
      * @param reportResult
      * @return String the next page
      */
     public String processDeleteReportResult(DecoratedReportResult reportResult) {
         reportsManager.deleteReportResult(reportResult.getReportResult());
 
         return "";
     }
 
     /**
      * @param report
      * @return String the next page
      */
     public String processDeleteLiveReport(DecoratedReport report) {
         reportsManager.deleteReport(report.getReport(), true);
         return "";
     }
 
     public String processDeleteReportDef(ReportDefinition reportDef) {
         reportsManager.deleteReportDefXmlFile(reportDef);
         return ReportsTool.mainPage;
     }
 
     public String processActionCancel() {
         setImportFilesString(null);
         setInvalidImportMessage(null);
         setInvalidImport(false);
         return ReportsTool.mainPage;
     }
 
     /**
      * @param report
      * @return String the next page
      */
     public String processEditLiveReport(DecoratedReport report) {
         getReportsManager().checkEditAccess();
         setWorkingReport(report);
         if (report.getReportParams().size() > 0){
               return createReportParamsPage;
         }
         else {
             return reportResultsPage;
         }
     }
 
     public Map getUserCan() {
         if (userCan == null) {
             userCan = getReportsManager().getAuthorizationsMap();
         }
         return userCan;
     }
 
     public void setUserCan(Map userCan) {
         this.userCan = userCan;
     }
 
     public boolean isMaintainer() {
         return getReportsManager().isMaintaner();
     }
 
    // todo figure out how to implement this -- JDE
     public void processActionAudienceHelper(DecoratedReportResult reportResult) {
       /*
         ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
         //Tool tool = ToolManager.getCurrentTool();
         ToolSession session = SessionManager.getCurrentToolSession();
         ServerConfigurationService configService =
                 org.sakaiproject.component.cover.ServerConfigurationService.getInstance();
         String baseUrl = configService.getServerUrl();
         String url = baseUrl + "/sakai-reports-tool/viewReportResults.osp?id=" + reportResult.getReportResult().getResultId();
         url += "&" + Tool.PLACEMENT_ID + "=" + SessionManager.getCurrentToolSession().getPlacementId();
 
         ResourceBundle myResources =
                 ResourceBundle.getBundle(ReportsManager.REPORTS_MESSAGE_BUNDLE);
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_FUNCTION, "reports.view");
 
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_PORTFOLIO_WIZARD, "false");
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_QUALIFIER, reportResult.getReportResult().getResultId().getValue());
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_GLOBAL_TITLE, myResources.getString("title_share_report"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_INSTRUCTIONS,
                 myResources.getString("instructions_addViewersToPresentation"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_GROUP_TITLE,
                 myResources.getString("instructions_publishToGroup"));
 
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_INDIVIDUAL_TITLE,
                 myResources.getString("instructions_publishToIndividual"));
 
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_PUBLIC_FLAG, "false");
         // session.setAttribute(AudienceSelectionHelper.AUDIENCE_PUBLIC_TITLE, myResources.getString("instructions_publishToInternet"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_SELECTED_TITLE,
                 myResources.getString("instructions_selectedAudience"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_FILTER_INSTRUCTIONS,
                 myResources.getString("instructions_selectFilterUserList"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_GUEST_EMAIL, "true");
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_WORKSITE_LIMITED, "false");
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_PUBLIC_INSTRUCTIONS,
                 myResources.getString("publish_message"));
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_PUBLIC_URL, url);
 
         session.setAttribute(AudienceSelectionHelper.AUDIENCE_BROWSE_INDIVIDUAL,
                 myResources.getString("audience_browse_individual"));
         try {
             context.redirect("osp.audience.helper/tool.jsf?panel=Main");
         }
         catch (IOException e) {
             throw new RuntimeException("Failed to redirect to helper", e);
         }
         */
     }
 
     public String processImportDefinition() {
         return ReportsTool.importReportDef;
     }
 
 
     public String processPickImportFiles() {
         ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
         ToolSession session = SessionManager.getCurrentToolSession();
         session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS, new Boolean(true).toString());
         /*
         List wsItemRefs = EntityManager.newReferenceList();
 
         for (Iterator i=importFiles.iterator();i.hasNext();) {
            WizardStyleItem wsItem = (WizardStyleItem)i.next();
            wsItemRefs.add(wsItem.getBaseReference().getBase());
         }*/
 
         session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, importFiles);
         session.setAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER,
                 ComponentManager.get("org.sakaiproject.content.api.ContentResourceFilter.reportImportFile"));
 
         try {
             context.redirect("sakai.filepicker.helper/tool");
         }
         catch (IOException e) {
             throw new RuntimeException("Failed to redirect to helper", e);
         }
         return null;
     }
 
     /**
      * This is called to put the file names into the text box.
      * It updates the list of files if the user is returning from the file picker
      *
      * @return String the names of the files being imported
      */
     public String getImportFilesString() {
         ToolSession session = SessionManager.getCurrentToolSession();
         if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
                 && session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
 
             List refs = (List) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
             //importFiles.clear();
             importFilesString = "";
             for (int i = 0; i < refs.size(); i++) {
                 Reference ref = (Reference) refs.get(i);
                 String nodeId = getContentHosting().getUuid(ref.getId());
                 String id = getContentHosting().resolveUuid(nodeId);
 
                 ContentResource resource = null;
                 try {
                     resource = getContentHosting().getResource(id);
                 } catch (PermissionException pe) {
                     throw new RuntimeException("Failed loading content: no permission to view file", pe);
                 } catch (TypeException pe) {
                     throw new RuntimeException("Wrong type", pe);
                 } catch (IdUnusedException pe) {
                     throw new RuntimeException("UnusedId: ", pe);
                 }
 
                 importFilesString += resource.getProperties().getProperty(
                         resource.getProperties().getNamePropDisplayName()) + " ";
             }
             importFiles = refs;
             session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
         } else if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
                 && session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) == null) {
             importFiles.clear();
             importFilesString = "";
         }
 
         return importFilesString;
     }
 
     public void setImportFilesString(String importFilesString) {
         this.importFilesString = importFilesString;
     }
 
 
     /**
      * Called when the user clicks the Import Button
      *
      * @return String next view
      */
     public String processImportReports() {
 
         if (importFiles.size() == 0) {
             return ReportsTool.importReportDef;
         }
 
         for (Iterator i = importFiles.iterator(); i.hasNext();) {
             Reference ref = (Reference) i.next();
 
             try {
                 reportsManager.importResource(
                         getIdManager().getId(getWorksite().getId()),
                         getContentHosting().getUuid(ref.getId()));
             } catch (ImportException ie) {
                 this.setInvalidImport(true);
                 return "";
             } catch (UnsupportedFileTypeException ufte) {
                 this.setInvalidImport(true);
                 return "";
             } catch (Exception ie) {
                 this.setInvalidImportMessage(invalidImportMessage + " " + ie.getMessage());
                 this.setInvalidImport(true);
                 return "";
             }
 
         }
 
         return ReportsTool.mainPage;
     }
 
     public ContentHostingService getContentHosting() {
         return contentHosting;
     }
 
     public void setContentHosting(ContentHostingService contentHosting) {
         this.contentHosting = contentHosting;
     }
 
     public String processScheduleReport(DecoratedReport report) {
         setJobDetail(reportsManager.processCreateJob(report.getReport()));
         jobDetailWrapper.setJobDetail(getJobDetail());
         jobDetailWrapper.setTriggerWrapperList(getJobTriggerList(getJobDetail()));
         return ReportsTool.scheduleReport;
     }
 
     public String getTriggerExpression() {
         return triggerExpression;
     }
 
     public void setTriggerExpression(String triggerExpression) {
         this.triggerExpression = triggerExpression;
     }
 
     public void validateTriggerExpression(FacesContext context,
                                           UIComponent component, Object value) {
         if (value != null) {
             try {
                 String expression = (String) value;
                 CronTrigger trigger = new CronTrigger();
                 trigger.setCronExpression(expression);
 
                 // additional checks
                 // quartz does not check for more than 7 tokens in expression
                 String[] arr = expression.split("\\s");
                 if (arr.length > 7) {
                     throw new ParseException("Expression has more than 7 tokens", 7);
                 }
 
                 //(check that last 2 entries are not both * or ?
                 String trimmed_expression = expression.replaceAll("\\s", ""); // remove whitespace
                 if (trimmed_expression.endsWith(CRON_CHECK_ASTERISK)
                         || trimmed_expression.endsWith(CRON_CHECK_QUESTION_MARK)) {
                     throw new ParseException("Cannot End in * * or ? ?", 1);
                 }
             }
             catch (ParseException e) {
                 // not giving a detailed message to prevent line wraps
                 FacesMessage message = new FacesMessage("Parse Exception");
                 message.setSeverity(FacesMessage.SEVERITY_WARN);
                 throw new ValidatorException(message);
             }
         }
 
     }
 
     public void validateTriggerName(FacesContext context, UIComponent component,
                                     Object value) {
         if (value != null) {
             try {
                 Trigger trigger = schedulerManager.getScheduler().getTrigger(
                         (String) value, Scheduler.DEFAULT_GROUP);
                 if (trigger != null) {
                     FacesMessage message = new FacesMessage("Existing Trigger Name");
                     message.setSeverity(FacesMessage.SEVERITY_WARN);
                     throw new ValidatorException(message);
                 }
             }
             catch (SchedulerException e) {
                 logger.error("Scheduler down!");
             }
         }
     }
 
     public JobDetail getJobDetail() {
         return jobDetail;
     }
 
     public void setJobDetail(JobDetail jobDetail) {
         this.jobDetail = jobDetail;
     }
 
     public List setJobTriggers(JobDetail jobDetail) {
         return getJobTriggerList(jobDetail);
     }
 
     public List getJobTriggerList(JobDetail jobDetail) {
         List triggerWrapperList = new ArrayList();
         try {
 
             jobDetailWrapper.setJobDetail(jobDetail);
             Trigger[] triggerArr = getSchedulerManager().getScheduler().getTriggersOfJob(jobDetail.getName(), ReportsManager.reportGroup);
 
             TriggerWrapper tw;
             for (int j = 0; j < triggerArr.length; j++) {
                 tw = new TriggerWrapperImpl();
                 tw.setTrigger(triggerArr[j]);
                 triggerWrapperList.add(tw);
             }
             jobDetailWrapper.setTriggerWrapperList(triggerWrapperList);
 
         }
         catch (SchedulerException e) {
             logger.error("scheduler error while getting job detail");
         }
         return triggerWrapperList;
     }
 
     public String getJobName() {
         return jobName;
     }
 
     public void setJobName(String jobName) {
         this.jobName = jobName;
     }
 
     public String getTriggerName() {
         return triggerName;
     }
 
     public void setTriggerName(String triggerName) {
         this.triggerName = triggerName;
     }
 
     public JobDetailWrapper getJobDetailWrapper() {
         return jobDetailWrapper;
     }
 
     public void setJobDetailWrapper(JobDetailWrapper jobDetailWrapper) {
         this.jobDetailWrapper = jobDetailWrapper;
     }
 
     public String processCreateTrigger() {
         Scheduler scheduler = schedulerManager.getScheduler();
         if (scheduler == null) {
             logger.error("Scheduler is down!");
             return "error";
         }
         try {
             Trigger trigger = new CronTrigger(triggerName, ReportsManager.reportGroup,
                     getJobDetail().getName(), ReportsManager.reportGroup, triggerExpression);
             scheduler.scheduleJob(trigger);
             TriggerWrapper tempTriggerWrapper = new TriggerWrapperImpl();
             tempTriggerWrapper.setTrigger(trigger);
             getJobDetailWrapper().getTriggerWrapperList().add(tempTriggerWrapper);
 
 
             triggerName = null;
             triggerExpression = null;
             return scheduleReport;
         }
         catch (Exception e) {
             triggerName = null;
             triggerExpression = null;
             logger.error("Failed to create trigger");
             return "error";
         }
     }
      public String processSelectAllTriggers()
   {
 
     isSelectAllTriggersSelected = !isSelectAllTriggersSelected;
     for (Iterator i = getJobDetailWrapper().getTriggerWrapperList().iterator(); i.hasNext();)
     {
       if (isSelectAllTriggersSelected)
       {
         ((TriggerWrapper) i.next()).setIsSelected(true);
       }
       else
       {
         ((TriggerWrapper) i.next()).setIsSelected(false);
       }
     }
     return scheduleReport;
   }
 
     public List getFilteredTriggersWrapperList() {
         return filteredTriggersWrapperList;
     }
 
     public void setFilteredTriggersWrapperList(List filteredTriggersWrapperList) {
         this.filteredTriggersWrapperList = filteredTriggersWrapperList;
     }
 
     public String processRefreshFilteredTriggers()
   {
     filteredTriggersWrapperList = new ArrayList();
     for (Iterator i = getJobDetailWrapper().getTriggerWrapperList()
         .iterator(); i.hasNext();)
     {
       TriggerWrapper triggerWrapper = (TriggerWrapper) i.next();
       if (triggerWrapper.getIsSelected())
       {
         filteredTriggersWrapperList.add(triggerWrapper);
       }
     }
     return deleteTriggers;
   }
      public String processDeleteTriggers()
   {
     try
     {
       TriggerWrapper triggerWrapper;
       for (Iterator i = filteredTriggersWrapperList.iterator(); i.hasNext();)
       {
         triggerWrapper = (TriggerWrapper) i.next();
         schedulerManager.getScheduler().unscheduleJob(
             triggerWrapper.getTrigger().getName(), ReportsManager.reportGroup);
         getJobDetailWrapper().getTriggerWrapperList().remove(triggerWrapper);
       }
     }
     catch (SchedulerException e)
     {
       logger.error("Scheduler Down");
     }
     return scheduleReport;
   }
 
    public String getMessageFromBundle(String key, Object[] args) {
       return MessageFormat.format(getMessageFromBundle(key), args);
    }
 
    public FacesMessage getFacesMessageFromBundle(String key, Object[] args) {
       return new FacesMessage(getMessageFromBundle(key, args));
    }
 
    public String getMessageFromBundle(String key) {
       if (toolBundle == null) {
          String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
          toolBundle = new ResourceLoader(bundle);
       /*   Locale requestLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
          if (requestLocale != null) {
             toolBundle = ResourceBundle.getBundle(
                   bundle, requestLocale);
          }
          else {
             toolBundle = ResourceBundle.getBundle(bundle);
          }*/
       }
       return toolBundle.getString(key);
    }
 
 }

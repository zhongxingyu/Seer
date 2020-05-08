 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caArray
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caArray Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caArray Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caArray Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.web.ajax;
 
 import gov.nih.nci.caintegrator2.application.study.AbstractClinicalSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.DelimitedTextClinicalSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 
 import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * This is an object which is turned into an AJAX javascript file using the DWR framework.  
  */
 public class SubjectDataSourceAjaxUpdater extends AbstractDwrAjaxUpdater
     implements ISubjectDataSourceAjaxUpdater {
     
     private static final String STATUS_TABLE = "subjectSourceJobStatusTable";
     private static final String ERROR_MESSAGE = "errors";
     private static final String JOB_TYPE = "subjectSourceType_";
     private static final String JOB_DESCRIPTION = "subjectSourceDescription_";
     private static final String JOB_DEPLOYMENT_STATUS = "subjectSourceStatus_";
     private static final String JOB_LAST_MODIFIED_DATE = "subjectSourceLastModifiedDate_";
     private static final String JOB_EDIT_URL = "subjectSourceEditUrl_";
     private static final String JOB_LOAD_URL = "subjectSourceLoadUrl_";
     private static final String JOB_DELETE_URL = "subjectSourceDeleteUrl_";
     private static final String JOB_ACTION_BAR1 = "subjectSourceActionBar1_";
     private static final String JOB_ACTION_BAR2 = "subjectSourceActionBar2_";
     private static final String SUBJECT_SOURCES_LOADER = "subjectSourceLoader";
     
     private StudyManagementService studyManagementService;
 
     /**
      * {@inheritDoc}
      */
     protected void initializeDynamicTable(DisplayableUserWorkspace workspace) {
         try {
             StudyConfiguration studyConfiguration = workspace.getCurrentStudyConfiguration();
             if (studyConfiguration != null && studyConfiguration.getId() != null) {
                 studyConfiguration = studyManagementService.
                     getRefreshedEntity(workspace.getCurrentStudyConfiguration());
                 initializeDynamicTable(workspace.getUserWorkspace().getUsername(), studyConfiguration);
             }
         } finally {
             getDwrUtil(workspace.getUserWorkspace().getUsername()).setValue(SUBJECT_SOURCES_LOADER, "");
         }
         
     }
     
     private void initializeDynamicTable(String username, StudyConfiguration studyConfiguration) {
         int counter = 0;
         for (AbstractClinicalSourceConfiguration clinicalSource 
                 : studyConfiguration.getClinicalConfigurationCollection()) {
             if (!(clinicalSource instanceof DelimitedTextClinicalSourceConfiguration)) {
                 throw new IllegalStateException("Unknown type of clinical source.");
             }
             DelimitedTextClinicalSourceConfiguration textSource = 
                 (DelimitedTextClinicalSourceConfiguration) clinicalSource;
             getDwrUtil(username).addRows(STATUS_TABLE, 
                                             createRow(textSource), 
                                             retrieveRowOptions(counter));
             updateJobStatus(username, textSource);
             counter++;
         }
     }
     
     /**
      * Removes table then reinitializes.
      * @param username of user.
      * @param studyConfiguration the study.
      */
     public void reinitializeDynamicTable(String username, StudyConfiguration studyConfiguration) {
         getDwrUtil(username).removeAllRows(STATUS_TABLE);
         initializeDynamicTable(username, studyConfiguration);
         toggleModalWaitDialog(username, studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void associateJobWithSession(DwrUtilFactory dwrUtilFactory, String username, Util util) {
         dwrUtilFactory.associateSubjectDataSourceWithSession(username, util);
     }
 
     private String[][] createRow(DelimitedTextClinicalSourceConfiguration clinicalSource) {
         String[][] rowString = new String[1][5];
         String id = clinicalSource.getId().toString();
         String startSpan = "<span id=\"";
         String endSpan = "\"> </span>";
         rowString[0][0] = startSpan + JOB_TYPE + id + endSpan;
         rowString[0][1] = startSpan + JOB_DESCRIPTION + id + endSpan;
         rowString[0][2] = startSpan + JOB_DEPLOYMENT_STATUS + id + endSpan;
         rowString[0][3] = startSpan + JOB_LAST_MODIFIED_DATE + id + endSpan;
         rowString[0][4] = startSpan + JOB_EDIT_URL + id + endSpan
                           + startSpan + getLoadActionUrl(clinicalSource) + id + endSpan
                           + startSpan + JOB_DELETE_URL + id + endSpan;
         
         return rowString;
     }
     
     private String getLoadActionUrl(DelimitedTextClinicalSourceConfiguration subjectSource) {
         if (subjectSource.isLoadable()) {
             return JOB_LOAD_URL;
         }
         return "NOT_LOADABLE";
     }
 
     /**
      * {@inheritDoc}
      */
     public void runJob(Long studyConfigurationId, Long subjectSourceId, SubjectDataSourceAjaxRunner.JobType jobType) {
         Thread subjectSourceRunner = new Thread(new SubjectDataSourceAjaxRunner(this, 
                 studyConfigurationId, subjectSourceId, jobType));
         subjectSourceRunner.start();
     }
     
 
     /**
      * {@inheritDoc}
      */
     @Override 
     protected Util getDwrUtil(String username) {
         return getDwrUtilFactory().retrieveSubjectDataSourceUtil(username);
     }
     
     /**
      * Saves clinicalSource status to database, then updates the status to JSP.
      * @param username to update the status to.
      * @param clinicalSource to save and update.
      */
     public void saveAndUpdateJobStatus(String username, DelimitedTextClinicalSourceConfiguration clinicalSource) {
         getStudyManagementService().saveSubjectSourceStatus(clinicalSource);
         updateJobStatus(username, clinicalSource);
     }
     
     /**
      * Adds error to JSP.
      * @param errorMessage .
      * @param username to associate JSP script session to.
      */
     public void addError(String errorMessage, String username) {
         getDwrUtil(username).setValue(ERROR_MESSAGE, errorMessage);
     }
 
     /**
      * Updates studyConfiguration status.
      * @param username to update the status to.
      * @param clinicalSource to update.
      */
     public void updateJobStatus(String username, DelimitedTextClinicalSourceConfiguration clinicalSource) {
         Util utilThis = getDwrUtil(username);
         String clinicalSourceId = clinicalSource.getId().toString();
         utilThis.setValue(JOB_TYPE + clinicalSourceId, 
                 clinicalSource.getType().toString());
         utilThis.setValue(JOB_DESCRIPTION + clinicalSourceId, 
                 clinicalSource.getDescription());
         utilThis.setValue(JOB_DEPLOYMENT_STATUS + clinicalSourceId, getStatusMessage(clinicalSource));
         utilThis.setValue(JOB_LAST_MODIFIED_DATE + clinicalSourceId, 
                 clinicalSource.getDisplayableLastModifiedDate());
         updateRowActions(clinicalSource, utilThis, clinicalSourceId);
         toggleModalWaitDialog(username, clinicalSource.getStudyConfiguration());
         
     }
 
     private void toggleModalWaitDialog(String username, StudyConfiguration studyConfiguration) {
         if (Cai2Util.isAnySubjectSourceInProgress(studyConfiguration)) {
             getDwrUtil(username).addFunctionCall("showBusyDialog");
             
         } else {
             getDwrUtil(username).addFunctionCall("hideBusyDialog");
         }
     }
 
     private void updateRowActions(DelimitedTextClinicalSourceConfiguration subjectSource, 
             Util utilThis, String subjectSourceId) {
         if (!Status.PROCESSING.equals(subjectSource.getStatus())) { // Not processing gets actions
             addNonProcessingActions(subjectSource, utilThis, subjectSourceId);
         } else { // Processing has no actions.
             utilThis.setValue(JOB_EDIT_URL + subjectSourceId, "");
             utilThis.setValue(getLoadActionUrl(subjectSource) + subjectSourceId, "");
             utilThis.setValue(JOB_DELETE_URL + subjectSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR1 + subjectSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR2 + subjectSourceId, "");
         }
     }
 
     private void addNonProcessingActions(DelimitedTextClinicalSourceConfiguration subjectSource, Util utilThis,
             String subjectSourceId) {
         String jobActionBarString = "&nbsp;";
         utilThis.setValue(JOB_EDIT_URL + subjectSourceId, 
                 retrieveUrl(subjectSource, "editClinicalSource", "Edit Annotations", "edit_annotations", false),
                 false);
         utilThis.setValue(JOB_ACTION_BAR1 + subjectSourceId, jobActionBarString, false);
         if (subjectSource.isLoadable()) {
             addLoadAction(subjectSource, utilThis, subjectSourceId, jobActionBarString);
         }
         utilThis.setValue(JOB_DELETE_URL + subjectSourceId, 
                 retrieveUrl(subjectSource, "deleteClinicalSource", "Delete", "delete", true),
                 false);
     }
 
     private void addLoadAction(DelimitedTextClinicalSourceConfiguration subjectSource, Util utilThis,
             String subjectSourceId, String jobActionBarString) {
         if (!subjectSource.isCurrentlyLoaded()) {
             utilThis.setValue(JOB_LOAD_URL + subjectSourceId, 
                     retrieveUrl(subjectSource, "loadClinicalSource", 
                     "Load Subject Annotation Source", "load", false),
                     false);
         } else {
             utilThis.setValue(JOB_LOAD_URL + subjectSourceId, 
                     retrieveUrl(subjectSource, "reLoadClinicalSource", 
                     "Reload All Subject Annotation Sources", "reload", false),
                     false);
         }
         utilThis.setValue(JOB_ACTION_BAR2 + subjectSourceId, jobActionBarString, false);
     }
 
     private String retrieveUrl(DelimitedTextClinicalSourceConfiguration subjectSource, String actionName, 
             String linkDisplay, String linkCssClass, boolean isDelete) {
        String deleteString = "";
         if (isDelete) {
             deleteString = 
                 "onclick=\"return confirm('The subject annotation source file will be permanently deleted.')\"";
         }
 
         return "<a style=\"margin: 0pt;\" class=\"btn\" href=\"" + actionName + ".action?studyConfiguration.id=" 
         + subjectSource.getStudyConfiguration().getId() 
        + "&clinicalSource.id=" + subjectSource.getId() + "\"" 
         + deleteString + "><span class=\"btn_img\"><span class=\""
         + linkCssClass + "\">" + linkDisplay + "</span></span></a>";
         
     }
     
     private String getStatusMessage(AbstractClinicalSourceConfiguration source) {
         Status subjectSourceStatus = source.getStatus();
         if (Status.PROCESSING.equals(subjectSourceStatus)) {
             return AJAX_LOADING_GIF + " " + subjectSourceStatus.getValue();
         } else if (Status.NOT_LOADED.equals(subjectSourceStatus) && !source.isLoadable()) {
             return Status.DEFINITION_INCOMPLETE.getValue();
         } else if (Status.ERROR.equals(subjectSourceStatus)) {
             return "<a href=\"editClinicalSource.action?studyConfiguration.id=" 
             + source.getStudyConfiguration().getId() 
             + "&clinicalSource.id=" + source.getId() + "\">" + Status.ERROR.getValue() + "</a>";
         }
         return subjectSourceStatus.getValue();
     }
 
     /**
      * @return the studyManagementService
      */
     public StudyManagementService getStudyManagementService() {
         return studyManagementService;
     }
 
     /**
      * @param studyManagementService the studyManagementService to set
      */
     public void setStudyManagementService(StudyManagementService studyManagementService) {
         this.studyManagementService = studyManagementService;
     }
 
 
 }

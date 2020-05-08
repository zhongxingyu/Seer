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
 
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 
 import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * This is an object which is turned into an AJAX javascript file using the DWR framework.  
  */
 public class GenomicDataSourceAjaxUpdater extends AbstractDwrAjaxUpdater
     implements IGenomicDataSourceAjaxUpdater {
     
     private static final String STATUS_TABLE = "genomicSourceJobStatusTable";
     private static final String JOB_HOST_NAME = "genomicSourceHostName_";
     private static final String JOB_EXPERIMENT_IDENTIFIER = "genomicSourceExperimentId_";
     private static final String JOB_DEPLOYMENT_STATUS = "genomicSourceStatus_";
     private static final String JOB_EDIT_URL = "genomicSourceEditUrl_";
     private static final String JOB_MAP_SAMPLES_URL = "genomicSourceMapSamplesUrl_";
     private static final String JOB_CONFIGURE_COPY_NUMBER_URL = "genomicSourceConfigureCopyNumberUrl_";
     private static final String JOB_DELETE_URL = "genomicSourceDeleteUrl_";
     private static final String JOB_FILE_DESCRIPTION = "genomicSourceFileDescription_";
     private static final String JOB_ACTION_BAR1 = "genomicSourceActionBar1_";
     private static final String JOB_ACTION_BAR2 = "genomicSourceActionBar2_";
     private static final String JOB_ACTION_BAR3 = "genomicSourceActionBar3_";
     private static final String GENOMIC_SOURCES_LOADER = "genomicSourceLoader";
     
     private StudyManagementService studyManagementService;
 
     /**
      * {@inheritDoc}
      */
     protected void initializeDynamicTable(DisplayableUserWorkspace workspace) {
         String username = workspace.getUserWorkspace().getUsername();
         try {
             StudyConfiguration studyConfiguration = workspace.getCurrentStudyConfiguration();
             if (studyConfiguration != null) {
                 int counter = 0;
                 for (GenomicDataSourceConfiguration genomicSource : studyConfiguration.getGenomicDataSources()) {
                     getDwrUtil(username).addRows(STATUS_TABLE, 
                                                     createRow(genomicSource), 
                                                     retrieveRowOptions(counter));
                     updateJobStatus(username, genomicSource, false);
                     counter++;
                 }
             } 
        } finally {
             getDwrUtil(username).setValue(GENOMIC_SOURCES_LOADER, "");
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void associateJobWithSession(DwrUtilFactory dwrUtilFactory, String username, Util util) {
         dwrUtilFactory.associateGenomicDataSourceWithSession(username, util);
     }
 
     private String[][] createRow(GenomicDataSourceConfiguration genomicSource) {
         String[][] rowString = new String[1][5];
         String id = genomicSource.getId().toString();
         String startSpan = "<span id=\"";
         String endSpan = "\"> </span>";
         rowString[0][0] = startSpan + JOB_HOST_NAME + id + endSpan;
         rowString[0][1] = startSpan + JOB_EXPERIMENT_IDENTIFIER + id + endSpan;
         rowString[0][2] = startSpan + JOB_FILE_DESCRIPTION + id + endSpan;
         rowString[0][3] = startSpan + JOB_DEPLOYMENT_STATUS + id + endSpan;
         rowString[0][4] = startSpan + JOB_EDIT_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR1 + id + endSpan
                           + startSpan + JOB_MAP_SAMPLES_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR2 + id + endSpan
                           + startSpan + JOB_CONFIGURE_COPY_NUMBER_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR3 + id + endSpan
                           + startSpan + JOB_DELETE_URL + id + endSpan;
         
         return rowString;
     }
 
     /**
      * {@inheritDoc}
      */
     public void runJob(GenomicDataSourceConfiguration genomicSource) {
         Thread genomicSourceRunner = new Thread(new GenomicDataSourceAjaxRunner(this, genomicSource));
         genomicSourceRunner.start();
     }
     
 
     /**
      * @param username
      * @return
      */
     private Util getDwrUtil(String username) {
         return getDwrUtilFactory().retrieveGenomicDataSourceUtil(username);
     }
     
     /**
      * Saves studyConfiguration to database, then updates the status to JSP.
      * @param username to update the status to.
      * @param genomicSource to save and update.
      */
     public void saveAndUpdateJobStatus(String username, GenomicDataSourceConfiguration genomicSource) {
         getStudyManagementService().saveGenomicDataSource(genomicSource);
         updateJobStatus(username, genomicSource, true);
     }
 
     /**
      * Updates studyConfiguration status.
      * @param username to update the status to.
      * @param genomicSource to update.
      * @param checkDeployButton determines whether to update the deploy button or not.
      */
     public void updateJobStatus(String username, GenomicDataSourceConfiguration genomicSource, 
             boolean checkDeployButton) {
         Util utilThis = getDwrUtil(username);
         String genomicSourceId = genomicSource.getId().toString();
         utilThis.setValue(JOB_HOST_NAME + genomicSourceId, 
                             genomicSource.getServerProfile().getHostname());
         utilThis.setValue(JOB_EXPERIMENT_IDENTIFIER + genomicSourceId, 
                             genomicSource.getExperimentIdentifier());
         updateRowFileDescriptions(utilThis, genomicSource, genomicSourceId);
         utilThis.setValue(JOB_DEPLOYMENT_STATUS + genomicSourceId, getStatusMessage(genomicSource.getStatus()));
         updateRowActions(genomicSource, utilThis, genomicSourceId);
         if (checkDeployButton && genomicSource.getStudyConfiguration().isDeployable()) {
             utilThis.addFunctionCall("enableDeployButton");
         }
     }
 
     private void updateRowFileDescriptions(Util utilThis, GenomicDataSourceConfiguration genomicSource,
             String genomicSourceId) {
         StringBuffer fileDescriptionString = new StringBuffer();
         String brString = "<br>";
         fileDescriptionString.append("<i>Mapping FIle(s): </i>");
         for (String fileName : genomicSource.getSampleMappingFileNames()) {
             fileDescriptionString.append(fileName);
             fileDescriptionString.append(brString);
         }
         fileDescriptionString.append("<i>Control Sample Mapping File(s): </i>");
         for (String fileName : genomicSource.getControlSampleMappingFileNames()) {
             fileDescriptionString.append(fileName);
             fileDescriptionString.append(brString);
         }
         fileDescriptionString.append("<i>Copy Number Mapping File: </i>");
         fileDescriptionString.append(genomicSource.getCopyNumberMappingFileName());
         utilThis.setValue(JOB_FILE_DESCRIPTION + genomicSourceId, fileDescriptionString.toString());
     }
 
     private void updateRowActions(GenomicDataSourceConfiguration genomicSource, Util utilThis, String genomicSourceId) {
         if (!Status.PROCESSING.equals(genomicSource.getStatus())) { // Not processing gets actions
             addNonProcessingActions(genomicSource, utilThis, genomicSourceId);
         } else { // Processing has no actions.
             utilThis.setValue(JOB_EDIT_URL + genomicSourceId, "");
             utilThis.setValue(JOB_MAP_SAMPLES_URL + genomicSourceId, "");
             utilThis.setValue(JOB_CONFIGURE_COPY_NUMBER_URL + genomicSourceId, "");
             utilThis.setValue(JOB_DELETE_URL + genomicSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR1 + genomicSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR2 + genomicSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR3 + genomicSourceId, "");
         }
     }
 
     private void addNonProcessingActions(GenomicDataSourceConfiguration genomicSource, Util utilThis,
             String genomicSourceId) {
         String jobActionBarString = "&nbsp;|&nbsp;";
         if (!Status.ERROR.equals(genomicSource.getStatus())) {
             addNonErrorActions(genomicSource, utilThis, genomicSourceId, jobActionBarString);
         }
         utilThis.setValue(JOB_EDIT_URL + genomicSourceId, 
                 retrieveUrl(genomicSource, "editGenomicSource", "Edit", false),
                 false);
         utilThis.setValue(JOB_ACTION_BAR1 + genomicSourceId, jobActionBarString, false);
         utilThis.setValue(JOB_DELETE_URL + genomicSourceId, 
                 retrieveUrl(genomicSource, "deleteGenomicSource", "Delete", true),
                 false);
     }
 
     private void addNonErrorActions(GenomicDataSourceConfiguration genomicSource, 
                     Util utilThis, String genomicSourceId, String jobActionBarString) {
         utilThis.setValue(JOB_MAP_SAMPLES_URL + genomicSourceId, 
                 retrieveUrl(genomicSource, "editSampleMapping", "Map Samples", false),
                 false);
         utilThis.setValue(JOB_ACTION_BAR2 + genomicSourceId, jobActionBarString, false);
         utilThis.setValue(JOB_CONFIGURE_COPY_NUMBER_URL + genomicSourceId, 
                 retrieveUrl(genomicSource, "editCopyNumberDataConfiguration", "ConfigureCopyNumberData", false),
                 false);
         utilThis.setValue(JOB_ACTION_BAR3 + genomicSourceId, jobActionBarString, false);
     }
 
     private String retrieveUrl(GenomicDataSourceConfiguration genomicSource, String actionName, 
             String linkDisplay, boolean isDelete) {
         String deleteString = "";
         if (isDelete) {
             deleteString = "onclick=\"return confirm('The Genomic Data Source " 
                             + genomicSource.getExperimentIdentifier() + " will be permanently deleted.')\"";
         }
         return "<a href=\"" + actionName + ".action?studyConfiguration.id=" 
                     + genomicSource.getStudyConfiguration().getId() 
                     + "&genomicSource.id=" + genomicSource.getId() + "\"" 
                     + deleteString + ">" + linkDisplay + "</a>";
     }
     
     private String getStatusMessage(Status genomicSourceStatus) {
         if (Status.PROCESSING.equals(genomicSourceStatus)) {
             return AJAX_LOADING_GIF + " " + genomicSourceStatus.getValue();
         }
         return genomicSourceStatus.getValue();
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

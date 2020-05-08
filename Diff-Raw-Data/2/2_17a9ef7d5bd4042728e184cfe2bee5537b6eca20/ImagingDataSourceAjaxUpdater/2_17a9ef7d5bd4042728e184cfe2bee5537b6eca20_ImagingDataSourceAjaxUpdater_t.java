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
 
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceMappingTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 
 import java.io.File;
 
 import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * This is an object which is turned into an AJAX javascript file using the DWR framework.  
  */
 public class ImagingDataSourceAjaxUpdater extends AbstractDwrAjaxUpdater implements IImagingDataSourceAjaxUpdater {
     
     private static final String STATUS_TABLE = "imagingSourceJobStatusTable";
     private static final String JOB_HOST_NAME = "imagingSourceHostName_";
     private static final String JOB_COLLECTION_NAME = "imagingSourceCollectionName_";
     private static final String JOB_DEPLOYMENT_STATUS = "imagingSourceStatus_";
     private static final String JOB_LAST_MODIFIED_DATE = "imagingSourceLastModified_";
     private static final String JOB_EDIT_URL = "imagingSourceEditUrl_";
     private static final String JOB_EDIT_ANNOTATIONS_URL = "imagingSourceEditAnnotationsUrl_";
     private static final String JOB_LOAD_ANNOTATIONS_URL = "imagingSourceLoadAnnotationsUrl_";
     private static final String JOB_DELETE_URL = "imagingSourceDeleteUrl_";
     private static final String JOB_FILE_DESCRIPTION = "imagingSourceFileDescription_";
     private static final String JOB_ACTION_BAR1 = "imagingSourceActionBar1_";
     private static final String JOB_ACTION_BAR2 = "imagingSourceActionBar2_";
     private static final String JOB_ACTION_BAR3 = "imagingSourceActionBar3_";
     private static final String IMAGING_SOURCES_LOADER = "imagingSourceLoader";
     
     private StudyManagementService studyManagementService;
 
     /**
      * {@inheritDoc}
      */
     protected void initializeDynamicTable(DisplayableUserWorkspace workspace) {
         String username = workspace.getUserWorkspace().getUsername();
         try {
             StudyConfiguration studyConfiguration = workspace.getCurrentStudyConfiguration();
             if (studyConfiguration != null && studyConfiguration.getId() != null) {
                 studyConfiguration = studyManagementService.getRefreshedEntity(studyConfiguration);
                 int counter = 0;
                 for (ImageDataSourceConfiguration imagingSource : studyConfiguration.getImageDataSources()) {
                     getDwrUtil(username).addRows(STATUS_TABLE, 
                                                     createRow(imagingSource), 
                                                     retrieveRowOptions(counter));
                     updateJobStatus(username, imagingSource);
                     counter++;
                 }
             } 
         } finally {
             getDwrUtil(username).setValue(IMAGING_SOURCES_LOADER, "");
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void associateJobWithSession(DwrUtilFactory dwrUtilFactory, String username, Util util) {
         dwrUtilFactory.associateImagingDataSourceWithSession(username, util);
     }
 
     private String[][] createRow(ImageDataSourceConfiguration imagingSource) {
         String[][] rowString = new String[1][6];
         String id = imagingSource.getId().toString();
         String startSpan = "<span id=\"";
         String endSpan = "\"> </span>";
         rowString[0][0] = startSpan + JOB_HOST_NAME + id + endSpan;
         rowString[0][1] = startSpan + JOB_COLLECTION_NAME + id + endSpan;
         rowString[0][2] = startSpan + JOB_FILE_DESCRIPTION + id + endSpan;
         rowString[0][3] = startSpan + JOB_DEPLOYMENT_STATUS + id + endSpan;
         rowString[0][4] = startSpan + JOB_LAST_MODIFIED_DATE + id + endSpan;
         rowString[0][5] = startSpan + JOB_EDIT_URL + id + endSpan
                           + startSpan + JOB_EDIT_ANNOTATIONS_URL + id + endSpan
                           + startSpan + JOB_LOAD_ANNOTATIONS_URL + id + endSpan
                           + startSpan + JOB_DELETE_URL + id + endSpan;
         
         return rowString;
     }
     
     /**
      * {@inheritDoc}
      */
     public void runJob(Long imagingSourceId, 
                        File imageClinicalMappingFile, 
                        ImageDataSourceMappingTypeEnum mappingType,
                        boolean mapOnly, boolean loadAimAnnotation) {
         Thread imagingSourceRunner = new Thread(new ImagingDataSourceAjaxRunner(this, imagingSourceId,
                 imageClinicalMappingFile, mappingType, mapOnly, loadAimAnnotation));
         imagingSourceRunner.start();
     }
     
 
     /**
      * @param username
      * @return
      */
     private Util getDwrUtil(String username) {
         return getDwrUtilFactory().retrieveImagingDataSourceUtil(username);
     }
     
     /**
      * Saves imagingSource to database, then updates the status to JSP.
      * @param username to update the status to.
      * @param imagingSource to save and update.
      */
     public void saveAndUpdateJobStatus(String username, ImageDataSourceConfiguration imagingSource) {
         getStudyManagementService().daoSave(imagingSource);
         updateJobStatus(username, imagingSource);
     }
 
     /**
      * Updates imagingSource status.
      * @param username to update the status to.
      * @param imagingSource to update.
      */
     public void updateJobStatus(String username, ImageDataSourceConfiguration imagingSource) {
         Util utilThis = getDwrUtil(username);
         String imagingSourceId = imagingSource.getId().toString();
         utilThis.setValue(JOB_HOST_NAME + imagingSourceId, 
                             imagingSource.getServerProfile().getHostname());
         utilThis.setValue(JOB_COLLECTION_NAME + imagingSourceId, 
                             imagingSource.getCollectionName());
         updateRowFileDescriptions(utilThis, imagingSource, imagingSourceId);
         utilThis.setValue(JOB_DEPLOYMENT_STATUS + imagingSourceId, getStatusMessage(imagingSource.getStatus()));
         utilThis.setValue(JOB_LAST_MODIFIED_DATE + imagingSourceId, 
                 imagingSource.getDisplayableLastModifiedDate());
         updateRowActions(imagingSource, utilThis, imagingSourceId);        
     }
 
     private void updateRowFileDescriptions(Util utilThis, ImageDataSourceConfiguration imagingSource,
             String imagingSourceId) {
         StringBuffer fileDescriptionString = new StringBuffer();
         String brString = "<br>";
         if (isAimDataService(imagingSource)) {
            fileDescriptionString.append("<i>AIM Service URL: </i>");
             fileDescriptionString
                     .append(imagingSource.getImageAnnotationConfiguration().getAimServerProfile().getUrl());
         } else {
             updateFileDescriptionString(imagingSource, fileDescriptionString, brString);
         }
         utilThis.setValue(JOB_FILE_DESCRIPTION + imagingSourceId, fileDescriptionString.toString());
     }
 
     private boolean isAimDataService(ImageDataSourceConfiguration imagingSource) {
         return imagingSource.getImageAnnotationConfiguration() != null
              && imagingSource.getImageAnnotationConfiguration().isAimDataService();
     }
 
     private void updateFileDescriptionString(ImageDataSourceConfiguration imagingSource,
             StringBuffer fileDescriptionString, String brString) {
         fileDescriptionString.append("<i>Annotation File: </i>");
         if (imagingSource.getImageAnnotationConfiguration() != null) {
             fileDescriptionString.append(imagingSource.getImageAnnotationConfiguration().
                     getAnnotationFile().getFile().getName());
         } else {
             fileDescriptionString.append("None");
         }
         fileDescriptionString.append(brString);
         fileDescriptionString.append("<i>Mapping File: </i>");
         fileDescriptionString.append(imagingSource.getMappingFileName());
     }
 
     private void updateRowActions(ImageDataSourceConfiguration imagingSource, Util utilThis, String imagingSourceId) {
         if (!Status.PROCESSING.equals(imagingSource.getStatus())) { // Not processing gets actions
             addNonProcessingActions(imagingSource, utilThis, imagingSourceId);
         } else { // Processing has no actions.
             utilThis.setValue(JOB_EDIT_URL + imagingSourceId, "");
             utilThis.setValue(JOB_EDIT_ANNOTATIONS_URL + imagingSourceId, "");
             utilThis.setValue(JOB_LOAD_ANNOTATIONS_URL + imagingSourceId, "");
             utilThis.setValue(JOB_DELETE_URL + imagingSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR1 + imagingSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR2 + imagingSourceId, "");
             utilThis.setValue(JOB_ACTION_BAR3 + imagingSourceId, "");
         }
     }
 
     private void addNonProcessingActions(ImageDataSourceConfiguration imagingSource, Util utilThis,
             String imagingSourceId) {
         String jobActionBarString = "&nbsp;";
         if (!Status.ERROR.equals(imagingSource.getStatus())) {
             addNonErrorActions(imagingSource, utilThis, imagingSourceId, jobActionBarString);
         }
         utilThis.setValue(JOB_EDIT_URL + imagingSourceId, 
                 retrieveUrl(imagingSource, "editImagingSource", "Edit", "edit", false),
                 false);
         utilThis.setValue(JOB_ACTION_BAR1 + imagingSourceId, jobActionBarString, false);
         if (isAimDataService(imagingSource) && !Status.LOADED.equals(imagingSource.getStatus())) {
             utilThis.setValue(JOB_LOAD_ANNOTATIONS_URL + imagingSourceId, 
                     retrieveUrl(imagingSource, "loadAimAnnotation", "Load AIM Annotations", "load", false),
                     false);
             utilThis.setValue(JOB_ACTION_BAR2 + imagingSourceId, "");
         }
         utilThis.setValue(JOB_DELETE_URL + imagingSourceId, 
                 retrieveUrl(imagingSource, "deleteImagingSource", "Delete", "delete", true),
                 false);
     }
 
     private void addNonErrorActions(ImageDataSourceConfiguration imagingSource, 
                     Util utilThis, String imagingSourceId, String jobActionBarString) {
         utilThis.setValue(JOB_EDIT_ANNOTATIONS_URL + imagingSourceId, retrieveUrl(imagingSource, 
                 "editImagingSourceAnnotations", "Edit Annotations", "edit_annotations", false));
         utilThis.setValue(JOB_ACTION_BAR2 + imagingSourceId, jobActionBarString, false);
         
         if (imagingSource.getImageAnnotationConfiguration() != null) {
             addLoadAnnotationAction(imagingSource, utilThis, imagingSourceId, jobActionBarString);
         } 
         
     }
 
     private void addLoadAnnotationAction(ImageDataSourceConfiguration imagingSource, Util utilThis,
             String imagingSourceId, String jobActionBarString) {
         if (isAimDataService(imagingSource)) {
             if (!Status.LOADED.equals(imagingSource.getStatus())) {
                 utilThis.setValue(JOB_LOAD_ANNOTATIONS_URL + imagingSourceId, 
                     retrieveUrl(imagingSource, "loadAimAnnotation", "Load AIM Annotations", "load", false),
                     false);
                 utilThis.setValue(JOB_ACTION_BAR3 + imagingSourceId, jobActionBarString, false);
             }
         } else if (imagingSource.getImageAnnotationConfiguration().isLoadable() 
                 && !imagingSource.getImageAnnotationConfiguration().isCurrentlyLoaded()) {
             utilThis.setValue(JOB_LOAD_ANNOTATIONS_URL + imagingSourceId, 
                     retrieveUrl(imagingSource, "loadImagingSource", "Load Annotations", "load", false),
                     false);
             utilThis.setValue(JOB_ACTION_BAR3 + imagingSourceId, jobActionBarString, false);
         }
     }
 
     private String retrieveUrl(ImageDataSourceConfiguration imagingSource, String actionName, 
             String linkDisplay, String linkCssClass, boolean isDelete) {
         String deleteString = "";
         if (isDelete) {
             deleteString = "onclick=\"return confirm('The Imaging Data Source " 
                             + imagingSource.getCollectionName() + " will be permanently deleted.')\"";
         }
 
         return "<a style=\"margin: 0pt;\" class=\"btn\" href=\"" + actionName + ".action?studyConfiguration.id=" 
                     + imagingSource.getStudyConfiguration().getId() 
                     + "&imageSourceConfiguration.id=" + imagingSource.getId() + "\"" 
                     + deleteString + "><span class=\"btn_img\"><span class=\""
                     + linkCssClass + "\">" + linkDisplay + "</span></span></a>";
     }
     
     private String getStatusMessage(Status imagingSourceStatus) {
         if (Status.PROCESSING.equals(imagingSourceStatus)) {
             return AJAX_LOADING_GIF + " " + imagingSourceStatus.getValue();
         }
         return imagingSourceStatus.getValue();
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

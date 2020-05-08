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
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.domain.genomic.PlatformConfiguration;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * This is an object which is turned into an AJAX javascript file using the DWR framework.  
  */
 public class PlatformDeploymentAjaxUpdater extends AbstractDwrAjaxUpdater
     implements IPlatformDeploymentAjaxUpdater {
     
     private static final String UNAVAILABLE_STRING = "---";
     private static final String STATUS_TABLE = "platformDeploymentJobStatusTable";
     private static final String JOB_PLATFORM_NAME = "platformName_";
     private static final String JOB_PLATFORM_TYPE = "platformType_";
     private static final String JOB_PLATFORM_VENDOR = "platformVendor_";
     private static final String JOB_PLATFORM_STATUS = "platformStatus_";
     private static final String JOB_ARRAY_NAME = "platformArrayName_";
     private static final String JOB_DELETE_PLATFORM_URL = "platformJobDeleteUrl_";
     private ArrayDataService arrayDataService;
 
     /**
      * {@inheritDoc}
      */
     protected void initializeDynamicTable(DisplayableUserWorkspace workspace) {
         int counter = 0;
         List <PlatformConfiguration> platformConfigurationList = new ArrayList<PlatformConfiguration>();
         platformConfigurationList.addAll(arrayDataService.getPlatformConfigurations());
         Comparator<PlatformConfiguration> nameComparator = new Comparator<PlatformConfiguration>() {
             public int compare(PlatformConfiguration configuration1, PlatformConfiguration configuration2) {
                 return retrievePlatformName(configuration1).
                        compareToIgnoreCase(retrievePlatformName(configuration2));
             }
         };
         Collections.sort(platformConfigurationList, nameComparator);
         String username = workspace.getUserWorkspace().getUsername();
         for (PlatformConfiguration platformConfiguration : platformConfigurationList) {
             getDwrUtil(username).addRows(STATUS_TABLE, 
                                             createRow(platformConfiguration), 
                                             retrieveRowOptions(counter));
             updateJobStatus(username, platformConfiguration);
             counter++;
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     protected void associateJobWithSession(DwrUtilFactory dwrUtilFactory, String username, Util util) {
         dwrUtilFactory.associatePlatformConfigurationJobWithSession(username, util);
     }
 
     private String[][] createRow(PlatformConfiguration platformConfiguration) {
         String[][] rowString = new String[1][6];
         String id = platformConfiguration.getId().toString();
         String startSpan = "<span id=\"";
         String endSpan = "\"> </span>";
         rowString[0][0] = startSpan + JOB_PLATFORM_NAME + id + endSpan;
         rowString[0][1] = startSpan + JOB_PLATFORM_TYPE + id + endSpan;
         rowString[0][2] = startSpan + JOB_PLATFORM_VENDOR + id + endSpan;
         rowString[0][3] = startSpan + JOB_ARRAY_NAME + id + endSpan;
         rowString[0][4] = startSpan + JOB_PLATFORM_STATUS + id + endSpan;
         rowString[0][5] = startSpan + JOB_DELETE_PLATFORM_URL + id + endSpan;
         return rowString;
     }
 
     /**
      * {@inheritDoc}
      */
     public void runJob(PlatformConfiguration platformConfiguration, String username) {
         Thread platformConfigurationRunner = new Thread(
                 new PlatformDeploymentAjaxRunner(this, platformConfiguration, username));
         platformConfigurationRunner.start();
     }
     
     void addError(String errorMessage, String username) {
         getDwrUtil(username).setValue("errorMessages", errorMessage);
     }
 
     /**
      * @param username
      * @return
      */
     private Util getDwrUtil(String username) {
         return getDwrUtilFactory().retrievePlatformConfigurationUtil(username);
     }
 
     void updateJobStatus(String username, PlatformConfiguration platformConfiguration) {
         Util utilThis = getDwrUtil(username);
         String platformConfigurationId = platformConfiguration.getId().toString();
         utilThis.setValue(JOB_PLATFORM_NAME + platformConfigurationId, 
                           retrievePlatformName(platformConfiguration));
         utilThis.setValue(JOB_PLATFORM_TYPE + platformConfigurationId, 
                             retrievePlatformType(platformConfiguration));
         utilThis.setValue(JOB_PLATFORM_VENDOR + platformConfigurationId, 
                           retrievePlatformVendor(platformConfiguration));
         utilThis.setValue(JOB_ARRAY_NAME + platformConfigurationId, 
                           retrievePlatformArrayNames(platformConfiguration));
         utilThis.setValue(JOB_PLATFORM_STATUS + platformConfigurationId, 
                           getStatusMessage(platformConfiguration.getStatus()));
         updateRowActions(platformConfiguration, utilThis, platformConfigurationId);
     }
 
     private String retrievePlatformName(PlatformConfiguration platformConfiguration) {
         return platformConfiguration.getPlatform() == null ? platformConfiguration.getName() 
                                        : platformConfiguration.getPlatform().getName();
     }
     
     private String retrievePlatformType(PlatformConfiguration platformConfiguration) {
return platformConfiguration.getPlatform() == null ? UNAVAILABLE_STRING
                                        : platformConfiguration.getPlatformType().getValue();
     }    
 
     private String retrievePlatformVendor(PlatformConfiguration platformConfiguration) {
         return platformConfiguration.getPlatform() == null ? UNAVAILABLE_STRING 
                                        : platformConfiguration.getPlatform().getVendor().getValue();
     }
 
     private String retrievePlatformArrayNames(PlatformConfiguration platformConfiguration) {
         return platformConfiguration.getPlatform() == null ? UNAVAILABLE_STRING
                                        : platformConfiguration.getPlatform().getDisplayableArrayNames();
     }
 
     private void updateRowActions(PlatformConfiguration platformConfiguration, Util utilThis, 
             String platformConfigurationId) {
         if (!Status.PROCESSING.equals(platformConfiguration.getStatus()) && !platformConfiguration.isInUse()) {
             utilThis.setValue(JOB_DELETE_PLATFORM_URL + platformConfigurationId, 
                     "<a href=\"deletePlatform.action?platformConfigurationId=" 
                     + platformConfiguration.getId() 
                     + "\" onclick=\"return confirm('This platform will be permanently deleted.')\">Delete</a>",
                     false);
         } else {
             utilThis.setValue(JOB_DELETE_PLATFORM_URL, "None");
         }
     }
     
     private String getStatusMessage(Status studyConfigurationStatus) {
         if (Status.PROCESSING.equals(studyConfigurationStatus)) {
             return AJAX_LOADING_GIF + " " + studyConfigurationStatus.getValue();
         }
         return studyConfigurationStatus.getValue();
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 }

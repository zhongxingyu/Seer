 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
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
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
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
 package gov.nih.nci.caintegrator2.web.action.study.management;
 
 import gov.nih.nci.caintegrator2.application.analysis.grid.GridDiscoveryServiceJob;
 import gov.nih.nci.caintegrator2.application.study.CopyNumberDataConfiguration;
 import gov.nih.nci.caintegrator2.common.ConfigurationHelper;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 
 /**
  * Action called to create or edit a <code>GenomicDataSourceConfiguration</code>.
  */
 public class EditCopyNumberDataConfigurationAction extends AbstractGenomicSourceAction {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Action name for edit.
      */
     public static final String EDIT_ACTION = "edit";
 
     /**
      * Action name for save.
      */
     public static final String SAVE_ACTION = "save";
     
     private String action;
     private File copyNumberMappingFile;
     private String copyNumberMappingFileContentType;
     private String copyNumberMappingFileFileName;
     private String gladUrl;
     private String caDnaCopyUrl;
     private boolean useGlad = false;
     private ConfigurationHelper configurationHelper;
     private static final String COPY_NUMBER_MAPPING_FILE = "copyNumberMappingFile";
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void prepare() {
         super.prepare();
         if (getCopyNumberDataConfiguration().isCaDNACopyConfiguration()) {
             setCaDnaCopyUrl(getCopyNumberDataConfiguration().getSegmentationService().getUrl());
             setUseGlad(false);
         } else {
             setGladUrl(getCopyNumberDataConfiguration().getSegmentationService().getUrl());
             setUseGlad(true);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean isFileUpload() {
         return true;
     }
 
     /**
      * Called to open the copy number data configuration page.
      * 
      * @return the struts results
      */
     public String edit() {
         getCopyNumberDataConfiguration();
         setAction(SAVE_ACTION);
         return SUCCESS;
     }
 
     /**
      * Returns the copy number data configuration.
      * 
      * @return the configuration.
      */
     public CopyNumberDataConfiguration getCopyNumberDataConfiguration() {
         if (getGenomicSource().getCopyNumberDataConfiguration() == null) {
             CopyNumberDataConfiguration configuration = new CopyNumberDataConfiguration();
             getGenomicSource().setCopyNumberDataConfiguration(configuration);
             if (getUseGlad()) {
                 configuration.getSegmentationService().setUrl(
                         configurationHelper.getString(ConfigurationParameter.GENE_PATTERN_URL));
             }
         }
         return getGenomicSource().getCopyNumberDataConfiguration();
     }
     
     /**
      * Called to save the copy number mapping data configuration.
      * 
      * @return the struts results
      */
     public String save() {
         try {
             updateServiceUrl();
             getStudyManagementService().saveCopyNumberMappingFile(getGenomicSource(), getCopyNumberMappingFile(), 
                     getCopyNumberMappingFileFileName());
             getStudyManagementService().save(getStudyConfiguration());
             return SUCCESS;
         } catch (Exception e) {
             addActionError("An unexpected error has occurred, please report this problem - " + e.getMessage());
             return INPUT;
         } 
     }
 
     private void updateServiceUrl() {
         if (getUseGlad()) {
             getCopyNumberDataConfiguration().getSegmentationService().setUrl(getGladUrl());
         } else {
             getCopyNumberDataConfiguration().getSegmentationService().setUrl(getCaDnaCopyUrl());
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         if (SAVE_ACTION.equals(getAction())) {
             validateSave();
         }
         prepareValueStack();
     }
 
     private void validateSave() {
         if (copyNumberMappingFile == null) {
             addFieldError(COPY_NUMBER_MAPPING_FILE, " File is required");
         } else if (copyNumberMappingFile.length() == 0) {
             addFieldError(COPY_NUMBER_MAPPING_FILE, " File is empty");
         } else {
             validateFileFormat();
         }
         validateServiceUrl();
     }
     
     private void validateFileFormat() {
         CSVReader reader;
         try {
             reader = new CSVReader(new FileReader(copyNumberMappingFile));
             String[] fields;
             int lineNum = 0;
             while ((fields = reader.readNext()) != null) {
                 lineNum++;
                 if (fields.length != 3) {
                     addFieldError(COPY_NUMBER_MAPPING_FILE,
                             " File must have 3 columns instead of " + fields.length
                             + " on line number " + lineNum);
                     return;
                 }
             }
         } catch (IOException e) {
             addFieldError(COPY_NUMBER_MAPPING_FILE, " Error reading mapping file");
         }
     }
 
     private void validateServiceUrl() {
         if (getUseGlad() && StringUtils.isBlank(getGladUrl())) {
             addFieldError("gladUrl", "GLAD Service URL is required.");
         } else if (!getUseGlad() && StringUtils.isBlank(getCaDnaCopyUrl())) {
             addFieldError("caDnaCopyUrl", "CaDNACopy Service URL is required.");
         }
     }
 
     /**
      * @return the copyNumberMappingFile
      */
     public File getCopyNumberMappingFile() {
         return copyNumberMappingFile;
     }
 
     /**
      * @param copyNumberMappingFile the copyNumberMappingFile to set
      */
     public void setCopyNumberMappingFile(File copyNumberMappingFile) {
         this.copyNumberMappingFile = copyNumberMappingFile;
     }
 
     /**
      * @return the copyNumberMappingFileContentType
      */
     public String getCopyNumberMappingFileContentType() {
         return copyNumberMappingFileContentType;
     }
 
     /**
      * @param copyNumberMappingFileContentType the copyNumberMappingFileContentType to set
      */
     public void setCopyNumberMappingFileContentType(String copyNumberMappingFileContentType) {
         this.copyNumberMappingFileContentType = copyNumberMappingFileContentType;
     }
 
     /**
      * @return the copyNumberMappingFileFileName
      */
     public String getCopyNumberMappingFileFileName() {
         return copyNumberMappingFileFileName;
     }
 
     /**
      * @param copyNumberMappingFileFileName the copyNumberMappingFileFileName to set
      */
     public void setCopyNumberMappingFileFileName(String copyNumberMappingFileFileName) {
         this.copyNumberMappingFileFileName = copyNumberMappingFileFileName;
     }
 
     /**
      * @return the action
      */
     public String getAction() {
         return action;
     }
 
     /**
      * @param action the action to set
      */
     public void setAction(String action) {
         this.action = action;
     }
 
     /**
      * @return available PCA services.
      */
     public Map<String, String> getCaDnaCopyServices() {
         return GridDiscoveryServiceJob.getGridCaDnaCopyServices();
     }
 
     /**
      * @return the gladUrl
      */
     public String getGladUrl() {
         return gladUrl;
     }
 
     /**
      * @param gladUrl the gladUrl to set
      */
     public void setGladUrl(String gladUrl) {
         this.gladUrl = gladUrl;
     }
 
     /**
      * @return the caDnaCopyUrl
      */
     public String getCaDnaCopyUrl() {
         return caDnaCopyUrl;
     }
 
     /**
      * @param caDnaCopyUrl the caDnaCopyUrl to set
      */
     public void setCaDnaCopyUrl(String caDnaCopyUrl) {
         this.caDnaCopyUrl = caDnaCopyUrl;
     }
 
     /**
      * @return the useGlad
      */
     public boolean getUseGlad() {
         return useGlad;
     }
 
     /**
      * @param useGlad the useGlad to set
      */
     public void setUseGlad(boolean useGlad) {
         this.useGlad = useGlad;
     }
 
     /**
      * @return the configurationHelper
      */
     public ConfigurationHelper getConfigurationHelper() {
         return configurationHelper;
     }
 
     /**
      * @param configurationHelper the configurationHelper to set
      */
     public void setConfigurationHelper(ConfigurationHelper configurationHelper) {
         this.configurationHelper = configurationHelper;
     }
 }

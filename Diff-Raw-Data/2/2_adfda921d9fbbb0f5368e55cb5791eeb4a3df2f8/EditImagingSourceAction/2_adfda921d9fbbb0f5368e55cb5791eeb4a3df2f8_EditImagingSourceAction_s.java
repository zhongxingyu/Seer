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
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceMappingTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.LogEntry;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.InvalidImagingCollectionException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.external.ncia.NCIAFacade;
 import gov.nih.nci.caintegrator2.web.ajax.IImagingDataSourceAjaxUpdater;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  */
 public class EditImagingSourceAction extends AbstractImagingSourceAction {
     
     private static final long serialVersionUID = 1L;
     private static final String DEFAULT_WEB_URL = "https://imaging.nci.nih.gov/ncia";
     private static final String NBIA_WEB_URL_STRING = "https?://.*/ncia$";
     private static final Pattern NBIA_WEB_URL_PATTERN = Pattern.compile(NBIA_WEB_URL_STRING);
     private File imageClinicalMappingFile;
     private String imageClinicalMappingFileFileName;
     private ImageDataSourceMappingTypeEnum mappingType = ImageDataSourceMappingTypeEnum.AUTO;
     private IImagingDataSourceAjaxUpdater updater;
     private NCIAFacade nciaFacade;
     
     private boolean validateAddSource() {
         validateMappingFile();
         if (StringUtils.isBlank(getImageSourceConfiguration().getCollectionName())) {
             addFieldError("imageSourceConfiguration.collectionName", "Collection Name is required.");
         }
         if (StringUtils.isBlank(getImageSourceConfiguration().getServerProfile().getUrl())) {
             addFieldError("imageSourceConfiguration.serverProfile.url", "URL is required.");
         }
         validateWebUrl();
         if (!checkErrors()) {
             return false;
         }
         checkConnection();
         return checkErrors();
     }
 
     private boolean validateWebUrl() {
         if (StringUtils.isBlank(getImageSourceConfiguration().getServerProfile().getWebUrl())
                 || !NBIA_WEB_URL_PATTERN.matcher(
                         getImageSourceConfiguration().getServerProfile().getWebUrl()).find()) {
             addFieldError("imageSourceConfiguration.serverProfile.webUrl", "Web url must be of the pattern " 
                     + "http[s]://imaging.url[:port]/ncia");
         }
         return checkErrors();
     }
 
     private void checkConnection() {
         ImageDataSourceConfiguration imageSourceToTest = createNewImagingSource();
         try {
             nciaFacade.validateImagingSourceConnection(imageSourceToTest.getServerProfile(), imageSourceToTest
                     .getCollectionName());
         } catch (ConnectionException e) {
             addFieldError("imageSourceConfiguration.serverProfile.url", "Unable to connect to the server.");
         } catch (InvalidImagingCollectionException e) {
             addActionError(e.getMessage());
         }
     }
 
     private boolean validateMappingFile() {
         if (imageClinicalMappingFile == null && !ImageDataSourceMappingTypeEnum.AUTO.equals(mappingType)) {
             addFieldError("imageClinicalMappingFile", "Image to Clinical Mapping File is required");
         }
         return checkErrors();
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String execute() {
         if (getImageSourceConfiguration().getId() == null) {
             getImageSourceConfiguration().getServerProfile().setWebUrl(DEFAULT_WEB_URL);
         }
         return SUCCESS;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void fixUrlFromInternetExplorer() {
        if (!StringUtils.isBlank(getImageSourceConfiguration().getServerProfile().getUrl())) {
            getImageSourceConfiguration().getServerProfile().setUrl(
                Cai2Util.fixUrlForEditableSelect(getImageSourceConfiguration().getServerProfile().getUrl()));
        }
        if (!StringUtils.isBlank(getImageSourceConfiguration().getServerProfile().getWebUrl())) {
            getImageSourceConfiguration().getServerProfile().setWebUrl(
                Cai2Util.fixUrlForEditableSelect(getImageSourceConfiguration().getServerProfile().getWebUrl()));
        }
     }
     
     /**
      * {@inheritDoc}
      */
     protected boolean isFileUpload() {
         return true;
     }
     
     /**
      * @return String.
      */
     public String saveImagingSource() {
         if (!validateAddSource()) {
             return INPUT;
         }
         if (getImageSourceConfiguration().getId() != null) {
             ImageDataSourceConfiguration newImagingSource = createNewImagingSource();
             delete();
             setImageSourceConfiguration(newImagingSource);
         }
         getImageSourceConfiguration().getServerProfile().setHostname(
                 Cai2Util.getHostNameFromUrl(getImageSourceConfiguration().getServerProfile().getUrl()));
         return runAsynchronousJob(false);
     }
 
     private String runAsynchronousJob(boolean mapOnly) {
         storeImageMappingFileName();      
         File newMappingFile = null;
         try {
             newMappingFile = storeTemporaryMappingFile();
         } catch (IOException e) {
             addActionError("Unable to save uploaded file.");
             return INPUT;
         }
         getImageSourceConfiguration().setStatus(Status.PROCESSING);
         if (!mapOnly) {
             getStudyManagementService().addImageSourceToStudy(getStudyConfiguration(), getImageSourceConfiguration());
         }
         getStudyManagementService().daoSave(getImageSourceConfiguration());
         setStudyLastModifiedByCurrentUser(getImageSourceConfiguration(), 
                 LogEntry.getSystemLogSave(getImageSourceConfiguration()));
         updater.runJob(getImageSourceConfiguration().getId(), newMappingFile, mappingType, mapOnly, false);
         return SUCCESS;
     }
 
     private File storeTemporaryMappingFile() throws IOException {
         if (!ImageDataSourceMappingTypeEnum.AUTO.equals(mappingType)) {
             return getStudyManagementService().saveFileToStudyDirectory(getStudyConfiguration(), 
                     getImageClinicalMappingFile());
         }
         return null;
     }
     
     /**
      * Delete an imaging source file.
      * @return string
      */
     public String delete() {
         try {
             setStudyLastModifiedByCurrentUser(getImageSourceConfiguration(),
                     LogEntry.getSystemLogDeleteImagingSource(getImageSourceConfiguration()));
             getStudyManagementService().delete(getStudyConfiguration(), getImageSourceConfiguration());
         } catch (ValidationException e) {
             addActionError(e.getResult().getInvalidMessage());
             return ERROR;
         }
         return SUCCESS;
     }
     
     private ImageDataSourceConfiguration createNewImagingSource() {
         ImageDataSourceConfiguration configuration = new ImageDataSourceConfiguration();
         ServerConnectionProfile newProfile = configuration.getServerProfile();
         ServerConnectionProfile oldProfile = getImageSourceConfiguration().getServerProfile();
         newProfile.setUrl(oldProfile.getUrl());
         newProfile.setWebUrl(oldProfile.getWebUrl());
         newProfile.setHostname(oldProfile.getHostname());
         newProfile.setPort(oldProfile.getPort());
         newProfile.setUsername(oldProfile.getUsername());
         newProfile.setPassword(oldProfile.getPassword());
         configuration.setCollectionName(getImageSourceConfiguration().getCollectionName());
         return configuration;
     }
     
     /**
      * Loads image annotations.
      * @return struts result.
      */
     public String loadImageAnnotations() {
         try {
             getStudyManagementService().loadImageAnnotation(getImageSourceConfiguration());
             setStudyLastModifiedByCurrentUser(getImageSourceConfiguration(),
                     LogEntry.getSystemLogLoad(getImageSourceConfiguration()));
         } catch (ValidationException e) {
             addActionError(e.getResult().getInvalidMessage());
             return ERROR;
         }
         return SUCCESS;
     }
 
     /**
      * Maps the image series acquisition to clinical subjects.
      * @return struts result.
      */
     public String mapImagingSource() {
         if (!validateMappingFile()) {
             return INPUT;
         }
         return runAsynchronousJob(true);
     }
     
     /**
      * Saves the image source only (web URL in particular).
      * @return struts result.
      */
     public String updateImagingSource() {
         if (!validateWebUrl()) {
             return INPUT;
         }
         getStudyManagementService().daoSave(getImageSourceConfiguration());
         return SUCCESS;
     }
 
     private void storeImageMappingFileName() {
         if (!StringUtils.isBlank(imageClinicalMappingFileFileName)) {
             getImageSourceConfiguration().setMappingFileName(imageClinicalMappingFileFileName);
         } else {
             getImageSourceConfiguration().setMappingFileName(ImageDataSourceConfiguration.AUTOMATIC_MAPPING);
         }
     }
 
     /**
      * @return the imageClinicalMappingFile
      */
     public File getImageClinicalMappingFile() {
         return imageClinicalMappingFile;
     }
 
     /**
      * @param imageClinicalMappingFile
      *            the imageClinicalMappingFile to set
      */
     public void setImageClinicalMappingFile(File imageClinicalMappingFile) {
         this.imageClinicalMappingFile = imageClinicalMappingFile;
     }
 
     /**
      * @return the mappingType
      */
     public String getMappingType() {
         if (mappingType != null) {
             return mappingType.getValue();
         }
         return "";
     }
 
     /**
      * @param mappingType the mappingType to set
      */
     public void setMappingType(String mappingType) {
         if (StringUtils.isBlank(mappingType)) {
             this.mappingType = null;
         } else {
             this.mappingType = ImageDataSourceMappingTypeEnum.getByValue(mappingType);
         }
     }
 
     /**
      * @return the imageClinicalMappingFileFileName
      */
     public String getImageClinicalMappingFileFileName() {
         return imageClinicalMappingFileFileName;
     }
 
     /**
      * @param imageClinicalMappingFileFileName the imageClinicalMappingFileFileName to set
      */
     public void setImageClinicalMappingFileFileName(String imageClinicalMappingFileFileName) {
         this.imageClinicalMappingFileFileName = imageClinicalMappingFileFileName;
     }
 
     /**
      * @return the updater
      */
     public IImagingDataSourceAjaxUpdater getUpdater() {
         return updater;
     }
 
     /**
      * @param updater the updater to set
      */
     public void setUpdater(IImagingDataSourceAjaxUpdater updater) {
         this.updater = updater;
     }
 
     /**
      * @return available NBIA services.
      */
     public Set<String> getNbiaServices() {
         return GridDiscoveryServiceJob.getGridNbiaServices().keySet();
     }
 
     /**
      * @return the nciaFacade
      */
     public NCIAFacade getNciaFacade() {
         return nciaFacade;
     }
 
     /**
      * @param nciaFacade the nciaFacade to set
      */
     public void setNciaFacade(NCIAFacade nciaFacade) {
         this.nciaFacade = nciaFacade;
     }
 
 }

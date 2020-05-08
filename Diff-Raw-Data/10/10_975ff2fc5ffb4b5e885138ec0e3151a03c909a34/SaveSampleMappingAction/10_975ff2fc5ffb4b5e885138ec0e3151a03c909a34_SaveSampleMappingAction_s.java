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
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataLoadingTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.LogEntry;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 
 
 /**
  * Action called to create or edit a <code>GenomicDataSourceConfiguration</code>.
  */
 public class SaveSampleMappingAction extends AbstractGenomicSourceAction {
 
     private static final long serialVersionUID = 1L;
 
     /////////
     // Sample Mapping
     /////////
     private File sampleMappingFile;
     private String sampleMappingFileContentType;
     private String sampleMappingFileFileName;
     /////////
     // Control Mapping
     /////////
     private String controlSampleSetName;
     private File controlSampleFile;
     private String controlSampleFileContentType;
     private String controlSampleFileFileName;
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void prepare() {
         super.prepare();
         try {
             this.getStudyManagementService().checkForSampleUpdates(getStudyConfiguration());
         } catch (Exception e) {
             LOG.error("Error retrieving sample update information.", e);
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
      * {@inheritDoc}
      */
     @Override
     public String execute() {
         String returnString = SUCCESS;
         returnString = executeSampleMapping();
         if (SUCCESS.equals(returnString)) {
             returnString = executeControlSampleMapping();
         }
         return returnString;
     }
 
     private String executeControlSampleMapping() {
         if (getControlSampleFile() != null) {
             try {
                 getStudyManagementService().addControlSampleSet(getGenomicSource(),
                         getControlSampleSetName(), getControlSampleFile(), getControlSampleFileFileName());
                 setStudyLastModifiedByCurrentUser(getGenomicSource(),
                         LogEntry.getSystemLogAddControlSampleMappingFile(getGenomicSource(),
                                 getControlSampleFileFileName()));
                 return SUCCESS;
             } catch (ValidationException e) {
                 setControlMappingFieldError(getText("struts.messages.exception.invalid.file",
                         getArgs(e.getResult().getInvalidMessage())));
                 return INPUT;
             } catch (IOException e) {
                 setControlMappingFieldError(getText("struts.messages.exception.file.ioexception",
                         getArgs(e.getMessage())));
                 return INPUT;
             }
         }
         return SUCCESS;
     }
 
     private String executeSampleMapping() {
         if (getSampleMappingFile() != null) {
             try {
                 getStudyManagementService().mapSamples(getStudyConfiguration(), getSampleMappingFile(),
                         getGenomicSource());
                 persistFileName();
                 setStudyLastModifiedByCurrentUser(getGenomicSource(),
                         LogEntry.getSystemLogAddSampleMappingFile(getGenomicSource(), getSampleMappingFileFileName()));
                 return SUCCESS;
             } catch (ValidationException e) {
                 setSampleMappingFieldError(getText("struts.messages.exception.invalid.file",
                         getArgs(e.getResult().getInvalidMessage())));
                 return INPUT;
             } catch (IOException e) {
                 setSampleMappingFieldError(getText("struts.messages.exception.file.ioexception",
                         getArgs(e.getMessage())));
                 return INPUT;
             } catch (Exception e) {
                 setSampleMappingFieldError(getText("struts.messages.exception.unexpected", getArgs(e.getMessage())));
                 return INPUT;
             } finally {
                 prepare(); // Reloads the genomic source if there's an exception.
             }
         }
         return SUCCESS;
     }
 
     private void persistFileName() {
         try {
             getGenomicSource().setSampleMappingFileName(getSampleMappingFileFileName());
             if (!ArrayDataLoadingTypeEnum.PARSED_DATA.equals(getGenomicSource().getLoadingType())) {
                 getStudyManagementService().saveSampleMappingFile(getGenomicSource(), getSampleMappingFile(),
                         getSampleMappingFileFileName());
             }
             getStudyManagementService().save(getStudyConfiguration());
         } catch (Exception e) {
             addActionError(getText("struts.messages.exception.unexpected",
                     getArgs(e.getMessage())));
         }
     }
 
     private void setSampleMappingFieldError(String errorMessage) {
         addFieldError("sampleMappingFile", errorMessage);
     }
 
     private void setControlMappingFieldError(String errorMessage) {
         addFieldError("sampleMappingFile", errorMessage);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         if (sampleMappingFile == null && controlSampleFile == null) {
             addActionError(getText("struts.messages.error.file.required", getArgs("")));
         }
         if (sampleMappingFile != null) {
             validateSampleMappingFile();
         }
         if (controlSampleFile != null) {
             validateControlMappingFile();
         }
         prepareValueStack();
     }
 
     private void validateControlMappingFile() {
         if (StringUtils.isEmpty(getControlSampleSetName())) {
             addFieldError("controlSampleSetName", getText("struts.messages.error.name.required",
                     getArgs("")));
         } else if (getStudyConfiguration().getControlSampleSet(getControlSampleSetName()) != null) {
             addFieldError("controlSampleSetName", getText("struts.messages.error.duplicate.name",
                     getArgs("Control Set", getControlSampleSetName())));
         }
         if (getControlSampleFile().length() == 0) {
             setControlMappingFieldError(getText("struts.messages.error.file.empty", getArgs("")));
         }
     }
 
     private void validateSampleMappingFile() {
         if (sampleMappingFile.length() == 0) {
             setSampleMappingFieldError(getText("struts.messages.error.file.empty", getArgs("")));
         }
     }
 
 
     /**
      * @return the sampleMappingFile
      */
     public File getSampleMappingFile() {
         return sampleMappingFile;
     }
 
     /**
      * @param sampleMappingFile the sampleMappingFile to set
      */
     public void setSampleMappingFile(File sampleMappingFile) {
         this.sampleMappingFile = sampleMappingFile;
     }
 
     /**
      * @return the sampleMappingFileContentType
      */
     public String getSampleMappingFileContentType() {
         return sampleMappingFileContentType;
     }
 
     /**
      * @param sampleMappingFileContentType the sampleMappingFileContentType to set
      */
     public void setSampleMappingFileContentType(String sampleMappingFileContentType) {
         this.sampleMappingFileContentType = sampleMappingFileContentType;
     }
 
     /**
      * @return the sampleMappingFileFileName
      */
     public String getSampleMappingFileFileName() {
         return sampleMappingFileFileName;
     }
 
     /**
      * @param sampleMappingFileFileName the sampleMappingFileFileName to set
      */
     public void setSampleMappingFileFileName(String sampleMappingFileFileName) {
         this.sampleMappingFileFileName = sampleMappingFileFileName;
     }
 
     /**
      * @return the controlSampleSetName
      */
     public String getControlSampleSetName() {
         return controlSampleSetName;
     }
 
     /**
      * @param controlSampleSetName the controlSampleSetName to set
      */
     public void setControlSampleSetName(String controlSampleSetName) {
         this.controlSampleSetName = controlSampleSetName;
     }
 
     /**
      * @return the controlSampleFile
      */
     public File getControlSampleFile() {
         return controlSampleFile;
     }
 
     /**
      * @param controlSampleFile the controlSampleFile to set
      */
     public void setControlSampleFile(File controlSampleFile) {
         this.controlSampleFile = controlSampleFile;
     }
 
     /**
      * @return the controlSampleFileContentType
      */
     public String getControlSampleFileContentType() {
         return controlSampleFileContentType;
     }
 
     /**
      * @param controlSampleFileContentType the controlSampleFileContentType to set
      */
     public void setControlSampleFileContentType(String controlSampleFileContentType) {
         this.controlSampleFileContentType = controlSampleFileContentType;
     }
 
     /**
      * @return the controlSampleFileFileName
      */
     public String getControlSampleFileFileName() {
         return controlSampleFileFileName;
     }
 
     /**
      * @param controlSampleFileFileName the controlSampleFileFileName to set
      */
     public void setControlSampleFileFileName(String controlSampleFileFileName) {
         this.controlSampleFileFileName = controlSampleFileFileName;
     }
 
 }

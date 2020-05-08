 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caintegrator2-war
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees and 5AM Solutions, Inc. (5AM). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This caintegrator2-war Software License (the License) is between NCI and You. You (or
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
  * its rights in the caintegrator2-war Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caintegrator2-war Software; (ii) distribute and
  * have distributed to and by third parties the caintegrator2-war Software and any
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
  * developed by 5AM and the National Cancer Institute. If You do not include
  * such end-user documentation, You shall include this acknowledgment in the
  * Software itself, wherever such third-party acknowledgments normally appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", or "5AM"
  * to endorse or promote products derived from this Software. This License does
  * not authorize You to use any trademarks, service marks, trade names, logos or
  * product names of either NCI or 5AM, except as required to comply with the
  * terms of this License.
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this
  * Software into Your proprietary programs and into any third party proprietary
  * programs. However, if You incorporate the Software into third party
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software
  * into such third party proprietary programs and for informing Your
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
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC. OR THEIR
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.application.study;
 
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * Copy study.
  * @author mshestopalov
  *
  */
 public class CopyStudyHelper {
 
     private final StudyManagementService studyManagementSvc;;
     /**
      * Const.
      * @param svc study management service.
      */
     public CopyStudyHelper(StudyManagementService svc) {
         studyManagementSvc = svc;
     }
 
     /**
      * Copy external links.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      */
     public void copyExternalLinks(StudyConfiguration copyFrom, StudyConfiguration copyTo) {
         if (!copyFrom.getExternalLinkLists().isEmpty()) {
 
             for (ExternalLinkList extList : copyFrom.getExternalLinkLists()) {
                 ExternalLinkList newExtList = new ExternalLinkList();
                 newExtList.setDescription(extList.getDescription());
                 newExtList.setName(extList.getName());
                 newExtList.setFileName(extList.getFileName());
 
                 for (ExternalLink extLink : extList.getExternalLinks()) {
                     ExternalLink newExtLink = new ExternalLink();
                     newExtLink.setCategory(extLink.getCategory());
                     newExtLink.setName(extLink.getName());
                     newExtLink.setUrl(extLink.getUrl());
                     newExtList.getExternalLinks().add(newExtLink);
                 }
                 copyTo.getExternalLinkLists().add(newExtList);
             }
         }
     }
 
     /**
      * Copy survival definitions.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      */
     public void copySurvivalDefinitions(StudyConfiguration copyFrom, StudyConfiguration copyTo) {
         if (CollectionUtils.isNotEmpty(copyFrom.getStudy().getSurvivalValueDefinitionCollection())) {
             for (SurvivalValueDefinition survivalValueDef : copyFrom.getStudy()
                     .getSurvivalValueDefinitionCollection()) {
                 SurvivalValueDefinition newSurv = new SurvivalValueDefinition();
                 newSurv.setName(survivalValueDef.getName());
                 newSurv.setDeathDate(survivalValueDef.getDeathDate());
                 newSurv.setLastFollowupDate(survivalValueDef.getLastFollowupDate());
                 newSurv.setSurvivalLength(survivalValueDef.getSurvivalLength());
                 newSurv.setSurvivalLengthUnits(survivalValueDef.getSurvivalLengthUnits());
                 newSurv.setSurvivalStartDate(survivalValueDef.getSurvivalStartDate());
                 newSurv.setSurvivalStatus(survivalValueDef.getSurvivalStatus());
                 newSurv.setValueForCensored(survivalValueDef.getValueForCensored());
                 newSurv.setSurvivalValueType(survivalValueDef.getSurvivalValueType());
                 copyTo.getStudy().getSurvivalValueDefinitionCollection().add(newSurv);
             }
         }
     }
 
     /**
      * Copy Subject Annotation Groups.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      * @throws ValidationException on error
      * @throws IOException on error
      */
     public void copySubjectAnnotationGroups(StudyConfiguration copyFrom, StudyConfiguration copyTo)
         throws ValidationException, IOException {
         if (!copyFrom.getClinicalConfigurationCollection().isEmpty()) {
             for (AbstractClinicalSourceConfiguration clinicalSource
                     : copyFrom.getClinicalConfigurationCollection()) {
 
                 DelimitedTextClinicalSourceConfiguration orgTextSource =
                         (DelimitedTextClinicalSourceConfiguration) clinicalSource;
                 File newFile = orgTextSource.getAnnotationFile().getFile();
                 processSubjectAnnotationFile(copyTo, clinicalSource, newFile);
             }
         }
     }
 
     private void processSubjectAnnotationFile(StudyConfiguration copyTo,
             AbstractClinicalSourceConfiguration clinicalSource,
             File newFile) throws ValidationException, IOException {
         DelimitedTextClinicalSourceConfiguration newClinicalSource = null;
         DelimitedTextClinicalSourceConfiguration orgTextSource =
             (DelimitedTextClinicalSourceConfiguration) clinicalSource;
         if (newFile != null && newFile.exists()) {
             newClinicalSource =
                 studyManagementSvc.addClinicalAnnotationFile(copyTo, newFile, newFile.getName(), false);
            newClinicalSource.setLastModifiedDate(copyTo.getLastModifiedDate());
             newClinicalSource.setStatus(orgTextSource.getStatus());
         } else {
             studyManagementSvc.setStudyLastModifiedByCurrentUser(copyTo, copyTo.getUserWorkspace(),
                     null, LogEntry.getSystemLogSkipSubjAnotCopy(newFile.getPath()));
         }
     }
 
     /**
      * Copy annotation groups.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      * @throws ValidationException on error
      * @throws ConnectionException on error
      * @throws IOException on error
      */
     public void copyAnnotationGroups(StudyConfiguration copyFrom, StudyConfiguration copyTo)
         throws ValidationException, ConnectionException, IOException {
 
         if (CollectionUtils.isNotEmpty(copyFrom.getStudy().getAnnotationGroups())) {
             for (AnnotationGroup orgAnGrp : copyFrom.getStudy().getAnnotationGroups()) {
                 AnnotationGroup newGrp = new AnnotationGroup();
                 newGrp.setName(orgAnGrp.getName());
                 newGrp.setDescription(orgAnGrp.getDescription());
                 newGrp.setStudy(copyTo.getStudy());
                 studyManagementSvc.saveAnnotationGroup(newGrp, copyTo, null);
                 for (AnnotationFieldDescriptor anFieldDesc : orgAnGrp.getAnnotationFieldDescriptors()) {
                     AnnotationFieldDescriptor newAnFieldDesc = new AnnotationFieldDescriptor();
                     newAnFieldDesc.setAnnotationEntityType(anFieldDesc.getAnnotationEntityType());
                     newAnFieldDesc.setAnnotationGroup(newGrp);
                     newAnFieldDesc.setDefinition(anFieldDesc.getDefinition());
                     newAnFieldDesc.setHasValidationErrors(anFieldDesc.isHasValidationErrors());
                     newAnFieldDesc.setUsePermissibleValues(anFieldDesc.isUsePermissibleValues());
                     newAnFieldDesc.setType(anFieldDesc.getType());
                     newAnFieldDesc.setName(anFieldDesc.getName());
                     newAnFieldDesc.setShownInBrowse(anFieldDesc.isShownInBrowse());
                     newGrp.getAnnotationFieldDescriptors().add(newAnFieldDesc);
                 }
                 copyTo.getStudy().getAnnotationGroups().add(newGrp);
            }
         }
     }
 
     /**
      * Copy study logo.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      * @throws IOException on error
      */
     public void copyStudyLogo(StudyConfiguration copyFrom, StudyConfiguration copyTo) throws IOException {
         if (copyFrom.getStudyLogo() != null) {
             File originalFile = new File(copyFrom.getStudyLogo().getPath());
             if (originalFile != null && originalFile.exists()) {
                 studyManagementSvc.addStudyLogo(copyTo, originalFile,
                     copyFrom.getStudyLogo().getFileName(), copyFrom.getStudyLogo().getFileType());
             } else if (!copyFrom.getStudyLogo().getPath().isEmpty()) {
                 studyManagementSvc.setStudyLastModifiedByCurrentUser(copyTo, copyTo.getUserWorkspace(),
                         null, LogEntry.getSystemLogSkipLogoCopy(copyFrom.getStudyLogo().getPath()));
 
             }
         }
     }
 
     /**
      * Copy study genomic source configuration.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      */
     public void copyStudyGenomicSource(StudyConfiguration copyFrom, StudyConfiguration copyTo) {
         for (GenomicDataSourceConfiguration genomicDsConf : copyFrom.getGenomicDataSources()) {
             GenomicDataSourceConfiguration newGenomicDsConf = new GenomicDataSourceConfiguration();
             newGenomicDsConf.setDataType(genomicDsConf.getDataType());
             newGenomicDsConf.setDataTypeString(genomicDsConf.getDataTypeString());
             newGenomicDsConf.setExperimentIdentifier(genomicDsConf.getExperimentIdentifier());
             copyServerConnectionProfile(genomicDsConf.getServerProfile(), newGenomicDsConf.getServerProfile());
             newGenomicDsConf.setPlatformName(genomicDsConf.getPlatformName());
             newGenomicDsConf.setPlatformVendor(genomicDsConf.getPlatformVendor());
             newGenomicDsConf.setPlatformVendorString(genomicDsConf.getPlatformVendorString());
             newGenomicDsConf.setLoadingType(genomicDsConf.getLoadingType());
             newGenomicDsConf.setLoadingTypeString(genomicDsConf.getLoadingTypeString());
             newGenomicDsConf.setStatus(Status.NOT_MAPPED);
             newGenomicDsConf.setSampleMappingFileName("None Configured");
             newGenomicDsConf.setTechnicalReplicatesCentralTendency(genomicDsConf
                     .getTechnicalReplicatesCentralTendency());
             newGenomicDsConf.setTechnicalReplicatesCentralTendencyString(genomicDsConf
                     .getTechnicalReplicatesCentralTendencyString());
             newGenomicDsConf.setUseHighVarianceCalculation(genomicDsConf.isUseHighVarianceCalculation());
             newGenomicDsConf.setHighVarianceCalculationType(genomicDsConf.getHighVarianceCalculationType());
             newGenomicDsConf.setHighVarianceCalculationTypeString(genomicDsConf
                     .getHighVarianceCalculationTypeString());
             newGenomicDsConf.setHighVarianceThreshold(genomicDsConf.getHighVarianceThreshold());
             newGenomicDsConf.setLastModifiedDate(copyTo.getLastModifiedDate());
             copyTo.getGenomicDataSources().add(newGenomicDsConf);
         }
     }
 
     private void copyServerConnectionProfile(ServerConnectionProfile copyFrom, ServerConnectionProfile copyTo) {
         copyTo.setHostname(copyFrom.getHostname());
         copyTo.setPassword(copyFrom.getPassword());
         copyTo.setUrl(copyFrom.getUrl());
         copyTo.setPort(copyFrom.getPort());
         copyTo.setUsername(copyFrom.getUsername());
         copyTo.setWebUrl(copyFrom.getWebUrl());
     }
 
     /**
      * Copy study image source configuration.
      * @param copyFrom original study configuration.
      * @param copyTo new study configuration.
      */
     public void copyStudyImageSource(StudyConfiguration copyFrom, StudyConfiguration copyTo) {
         for (ImageDataSourceConfiguration imageDsConf : copyFrom.getImageDataSources()) {
             ImageDataSourceConfiguration newImageDsConf = new ImageDataSourceConfiguration();
             copyServerConnectionProfile(imageDsConf.getServerProfile(), newImageDsConf.getServerProfile());
             newImageDsConf.setCollectionName(imageDsConf.getCollectionName());
             newImageDsConf.setMappingFileName(imageDsConf.getMappingFileName());
             newImageDsConf.setStatus(Status.NOT_MAPPED);
             newImageDsConf.setLastModifiedDate(copyTo.getLastModifiedDate());
             copyTo.getImageDataSources().add(newImageDsConf);
         }
     }
 }

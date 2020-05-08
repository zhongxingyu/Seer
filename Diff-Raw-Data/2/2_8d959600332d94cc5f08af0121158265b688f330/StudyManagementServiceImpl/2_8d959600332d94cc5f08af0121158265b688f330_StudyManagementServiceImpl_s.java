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
 package gov.nih.nci.caintegrator2.application.study;
 
 import gov.nih.nci.caintegrator2.application.CaIntegrator2BaseService;
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.common.AnnotationValueUtil;
 import gov.nih.nci.caintegrator2.common.DateUtil;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.common.PermissibleValueUtil;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.CommonDataElement;
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.ValueDomain;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.domain.translational.Timepoint;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacade;
 import gov.nih.nci.caintegrator2.external.caarray.CopyNumberFilesNotFoundException;
 import gov.nih.nci.caintegrator2.external.caarray.ExperimentNotFoundException;
 import gov.nih.nci.caintegrator2.external.caarray.SamplesNotFoundException;
 import gov.nih.nci.caintegrator2.external.cadsr.CaDSRFacade;
 import gov.nih.nci.caintegrator2.external.ncia.NCIAFacade;
 import gov.nih.nci.caintegrator2.file.FileManager;
 import gov.nih.nci.caintegrator2.security.SecurityManager;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSSecurityException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 
 /**
  * Entry point to the StudyManagementService subsystem.
  */
 @Transactional(propagation = Propagation.REQUIRED)
 @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength" })   // see configure study
 public class StudyManagementServiceImpl extends CaIntegrator2BaseService implements StudyManagementService {
 
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(StudyManagementServiceImpl.class);
     private static final int DEFINITION_LENGTH = 1000;
     private static final int MAX_ERROR_MESSAGE_LENGTH = 500;
     private FileManager fileManager;
     private CaDSRFacade caDSRFacade;
     private NCIAFacade nciaFacade;
     private CaArrayFacade caArrayFacade;
     private WorkspaceService workspaceService;
     private SecurityManager securityManager;
 
     /**
      * {@inheritDoc}
      */
     public void save(StudyConfiguration studyConfiguration) {
         if (isNew(studyConfiguration)) {
             configureNew(studyConfiguration);
             getWorkspaceService().subscribe(getWorkspaceService().getWorkspace(), studyConfiguration.getStudy());
         }
         persist(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     @Transactional(rollbackFor = ValidationException.class)
     public void save(AnnotationDefinition definition) throws ValidationException {
         if (!definition.getAnnotationValueCollection().isEmpty()) {
             Set<AbstractAnnotationValue> valuesToUpdate = new HashSet<AbstractAnnotationValue>();
             valuesToUpdate.addAll(definition.getAnnotationValueCollection());
             for (AbstractAnnotationValue value : valuesToUpdate) {
                 value.convertAnnotationValue(definition);
             }
         }
         daoSave(definition);
     }
     
     /**
      * {@inheritDoc}
      */
     public void createProtectionElement(StudyConfiguration studyConfiguration) throws CSException {
         securityManager.createProtectionElement(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration) throws CSException {
         securityManager.deleteProtectionElement(studyConfiguration);
         fileManager.deleteStudyDirectory(studyConfiguration);
         getWorkspaceService().unsubscribeAll(studyConfiguration.getStudy());
         daoSave(studyConfiguration.getUserWorkspace());
         studyConfiguration.setUserWorkspace(null);
         getDao().delete(studyConfiguration);
     }
     
     private boolean isNew(StudyConfiguration studyConfiguration) {
         return studyConfiguration.getId() == null;
     }
 
     private void configureNew(StudyConfiguration studyConfiguration) {
         configureNew(studyConfiguration.getStudy());
     }
 
     @SuppressWarnings({"PMD.CyclomaticComplexity", 
                         "PMD.ExcessiveMethodLength", 
                         "PMD.NPathComplexity" })   // multiple simple null checks
     private void configureNew(Study study) {
         if (study.getDefaultTimepoint() == null) {
             Timepoint defaultTimepoint = new Timepoint();
             String studyTitle = "";
             if (study.getShortTitleText() != null) {
                 studyTitle = study.getShortTitleText();
             } else if (study.getLongTitleText() != null) {
                 studyTitle = study.getLongTitleText();
             }
             defaultTimepoint.setDescription("Default Timepoint For Study '" + studyTitle + "'");
             defaultTimepoint.setName("Default");
             study.setDefaultTimepoint(defaultTimepoint);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DelimitedTextClinicalSourceConfiguration addClinicalAnnotationFile(StudyConfiguration studyConfiguration,
             File inputFile, String filename, boolean createNewAnnotationDefinition)
     throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename, studyConfiguration);
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, getDao(), studyConfiguration,
                 EntityTypeEnum.SUBJECT, createNewAnnotationDefinition);
         DelimitedTextClinicalSourceConfiguration clinicalSourceConfig = 
             new DelimitedTextClinicalSourceConfiguration(annotationFile, studyConfiguration);
         daoSave(studyConfiguration);
         return clinicalSourceConfig;
     }
     
     /**
      * {@inheritDoc}
      */
     public void addStudyLogo(StudyConfiguration studyConfiguration,
                              File imageFile,
                              String fileName, 
                              String fileType) throws IOException {
         if (studyConfiguration.getStudyLogo() == null) {
             studyConfiguration.setStudyLogo(new StudyLogo());
         }
         studyConfiguration.getStudyLogo().setFileName(fileName);
         studyConfiguration.getStudyLogo().setFileType(fileType);
         File studyLogoFile = getFileManager().storeStudyFile(imageFile, fileName, studyConfiguration);
         studyConfiguration.getStudyLogo().setPath(studyLogoFile.getPath());
         daoSave(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public StudyLogo retrieveStudyLogo(Long studyId, String studyShortTitleText) {
         return getDao().retrieveStudyLogo(studyId, studyShortTitleText);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadClinicalAnnotation(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSourceConfiguration)
         throws ValidationException {
         if (validateAnnotationFieldDescriptors(studyConfiguration, 
                 clinicalSourceConfiguration.getAnnotationDescriptors(), EntityTypeEnum.SUBJECT)) {
             clinicalSourceConfiguration.loadAnnontation();
             save(studyConfiguration);
         } else {
             throw new ValidationException("Unable to load clinical source due to invalid values being loaded.  " 
                 + "Check the annotations on the edit screen for more details.");
         }
     }
 
     private boolean validateAnnotationFieldDescriptors(StudyConfiguration studyConfiguration,
             Collection<AnnotationFieldDescriptor> descriptors, EntityTypeEnum entityType)
     throws ValidationException {
         boolean isValid = true;
         for (AnnotationFieldDescriptor descriptor : descriptors) {
             populatePermissibleValues(studyConfiguration.getStudy(), entityType, descriptor);
             AnnotationDefinition definition = descriptor.getDefinition();
             if (definition != null && !definition.getPermissibleValueCollection().isEmpty()) {
                 try {
                     validateAnnotationDefinition(descriptor, studyConfiguration.getStudy(), 
                         entityType, definition);
                     if (descriptor.isHasValidationErrors()) {
                         makeFieldDescriptorValid(descriptor);
                     }
                 } catch (ValidationException e) {
                     isValid = false;
                     descriptor.setHasValidationErrors(true);
                     String invalidMessage = e.getResult().getInvalidMessage();
                     descriptor.setValidationErrorMessage(invalidMessage.length() >= MAX_ERROR_MESSAGE_LENGTH
                             ? invalidMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH - 1) : invalidMessage);
                     daoSave(descriptor);
                 }
             }
         }
         return isValid;
     }
 
     private void populatePermissibleValues(Study study, EntityTypeEnum entityType,
             AnnotationFieldDescriptor descriptor) throws ValidationException {
         if (descriptor.isUsePermissibleValues()
                 && descriptor.getDefinition().getPermissibleValueCollection().isEmpty()) {
             Set<Object> uniqueValues = validateAndRetrieveUniqueValues(study, entityType, 
                     descriptor, descriptor.getDefinition());
             descriptor.getDefinition().addPermissibleValues(uniqueValues);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void makeFieldDescriptorValid(AnnotationFieldDescriptor descriptor) {
         descriptor.setHasValidationErrors(false);
         descriptor.setValidationErrorMessage(null);
         daoSave(descriptor);
     }
 
     /**
      * {@inheritDoc}
      */
     public void reLoadClinicalAnnotation(StudyConfiguration studyConfiguration) throws ValidationException {
         deleteClinicalAnnotation(studyConfiguration);
         for (AbstractClinicalSourceConfiguration configuration 
                 : studyConfiguration.getClinicalConfigurationCollection()) {
             configuration.reLoadAnnontation();
         }        
         getDao().removeObjects(studyConfiguration.removeObsoleteSubjectAssignment());
         save(studyConfiguration);
     }
 
     private void deleteClinicalAnnotation(StudyConfiguration studyConfiguration) {
         Study study = studyConfiguration.getStudy();
         for (StudySubjectAssignment studySubjectAssignment : study.getAssignmentCollection()) {
             for (SubjectAnnotation subjectAnnotation : studySubjectAssignment.getSubjectAnnotationCollection()) {
                 subjectAnnotation.removeValueFromDefinition();
                 getDao().delete(subjectAnnotation);
             }
             studySubjectAssignment.getSubjectAnnotationCollection().clear();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSource) throws ValidationException {
         deleteClinicalAnnotation(studyConfiguration);
         studyConfiguration.getClinicalConfigurationCollection().remove(clinicalSource);
         getDao().delete(clinicalSource);
         reLoadClinicalAnnotation(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, GenomicDataSourceConfiguration genomicSource) {
         studyConfiguration.getGenomicDataSources().remove(genomicSource);
         for (Sample sample : genomicSource.getSamples()) {
             sample.removeSampleAcquisitionAssociations();
             for (Array array : sample.getArrayCollection()) {
                 array.getSampleCollection().remove(sample);
                 if (array.getSampleCollection().isEmpty()) {
                     getDao().delete(array);
                 }
             }
             sample.getArrayCollection().clear();
         }
         getDao().delete(genomicSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource) 
         throws ValidationException {
         studyConfiguration.getImageDataSources().remove(imageSource);
         getDao().delete(imageSource);
         
         reLoadImageAnnotation(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList) {
         studyConfiguration.getExternalLinkLists().remove(externalLinkList);
         getDao().delete(externalLinkList);
     }
     
     private void deleteImageAnnotation(StudyConfiguration studyConfiguration) {
         Study study = studyConfiguration.getStudy();
         for (StudySubjectAssignment studySubjectAssignment : study.getAssignmentCollection()) {
             for (ImageSeriesAcquisition imageSeriesAcquisition : studySubjectAssignment.getImageStudyCollection()) {
                 getDao().delete(imageSeriesAcquisition);
             }
             studySubjectAssignment.getImageStudyCollection().clear();
         }
     }
     
     private void reLoadImageAnnotation(StudyConfiguration studyConfiguration) throws ValidationException {
         deleteImageAnnotation(studyConfiguration);
         for (ImageDataSourceConfiguration configuration 
                 : studyConfiguration.getImageDataSources()) {
             if (configuration.getImageAnnotationConfiguration() != null) {
                 configuration.getImageAnnotationConfiguration().reLoadAnnontation();
             }
         }
         getDao().removeObjects(studyConfiguration.removeObsoleteSubjectAssignment());
         save(studyConfiguration);
     }
     
     private void persist(StudyConfiguration studyConfiguration) {
         for (StudySubjectAssignment assignment : studyConfiguration.getStudy().getAssignmentCollection()) {
             saveSubjectAnnotations(assignment.getSubjectAnnotationCollection());
             saveSampleAcquisitions(assignment.getSampleAcquisitionCollection());
             daoSave(assignment.getSubject());
             daoSave(assignment);
         }
         daoSave(studyConfiguration);
     }
 
     private void saveSampleAcquisitions(Collection<SampleAcquisition> sampleAcquisitionCollection) {
         for (SampleAcquisition sampleAcquisition : sampleAcquisitionCollection) {
             daoSave(sampleAcquisition.getSample());
             daoSave(sampleAcquisition);
         }
     }
 
     private void saveSubjectAnnotations(Collection<SubjectAnnotation> subjectAnnotations) {
         for (SubjectAnnotation annotation : subjectAnnotations) {
             daoSave(annotation.getAnnotationValue());
             daoSave(annotation);
         }
     }
     
     /**
      * {@inheritDoc}
      * @throws ValidationException 
      * @throws IOException 
      */
     @Transactional(rollbackFor = {ValidationException.class, IOException.class })
     public void mapSamples(StudyConfiguration studyConfiguration, File mappingFile, 
             GenomicDataSourceConfiguration genomicSource)
         throws ValidationException, IOException {
         new SampleMappingHelper(studyConfiguration, mappingFile, genomicSource).mapSamples();
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void addControlSampleSet(GenomicDataSourceConfiguration genomicSource,
             String controlSampleSetName, File controlSampleFile, String controlSampleFileName)
             throws ValidationException, IOException {
         new ControlSampleHelper(genomicSource, controlSampleFile).addControlSamples(controlSampleSetName,
                 controlSampleFileName);
         save(genomicSource.getStudyConfiguration());
     }
     
     /**
      * {@inheritDoc}
      */
     public void addGenomicSource(StudyConfiguration studyConfiguration, GenomicDataSourceConfiguration genomicSource)
         throws ConnectionException, ExperimentNotFoundException {
         addGenomicSourceToStudy(studyConfiguration, genomicSource);
         loadGenomicSource(genomicSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void addGenomicSourceToStudy(StudyConfiguration studyConfiguration, 
                                         GenomicDataSourceConfiguration genomicSource) {
         studyConfiguration.getGenomicDataSources().add(genomicSource);
         genomicSource.setStudyConfiguration(studyConfiguration);
         daoSave(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadGenomicSource(GenomicDataSourceConfiguration genomicSource) 
     throws ConnectionException, ExperimentNotFoundException {
         if (genomicSource.isExpressionData()) {
             handleLoadGeneExpression(genomicSource);
         } else if (genomicSource.isCopyNumberData() || genomicSource.isSnpData()) {
             handleLoadDnaAnalysis(genomicSource);
         }
         genomicSource.setStatus(Status.LOADED);
         daoSave(genomicSource);
     }
 
     private void handleLoadGeneExpression(GenomicDataSourceConfiguration genomicSource) throws ConnectionException,
             ExperimentNotFoundException {
         List<Sample> samples = getCaArrayFacade().getSamples(genomicSource.getExperimentIdentifier(), 
                 genomicSource.getServerProfile());
         if (samples.isEmpty()) {
             throw new SamplesNotFoundException(
                     "No samples found for this caArray experiment (verify that sample data is accessible in caArray)");
         }
         genomicSource.setSamples(samples);
         for (Sample sample : samples) {
             sample.setGenomicDataSource(genomicSource);
         }
     }
     
     private void handleLoadDnaAnalysis(GenomicDataSourceConfiguration genomicSource) throws ConnectionException,
     ExperimentNotFoundException {
         String errorMessage = 
             "No samples found for this caArray experiment (verify that sample data is accessible in caArray)";
         try {
             if (getCaArrayFacade().retrieveFilesForGenomicSource(genomicSource).isEmpty()) {
                 throw new CopyNumberFilesNotFoundException(errorMessage);
             }
         } catch (FileNotFoundException e) {
             throw new CopyNumberFilesNotFoundException(errorMessage, e);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public GenomicDataSourceConfiguration getRefreshedGenomicSource(Long id) {
         GenomicDataSourceConfiguration genomicSource = new GenomicDataSourceConfiguration();
         genomicSource.setId(id);
         genomicSource = getRefreshedEntity(genomicSource);
         HibernateUtil.loadCollection(genomicSource.getStudyConfiguration());
         return genomicSource;
     }
     
     /**
      * {@inheritDoc}
      */
     public StudyConfiguration getRefreshedSecureStudyConfiguration(String username, Long id) 
     throws CSSecurityException {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.setId(id);
         studyConfiguration = getRefreshedEntity(studyConfiguration);
         Set<StudyConfiguration> managedStudyConfigurations = new HashSet<StudyConfiguration>();
         try {
             managedStudyConfigurations = 
                 securityManager.retrieveManagedStudyConfigurations(username, getDao().getStudies(username)); 
         } catch (CSException e) {
             throw new IllegalStateException("Error retrieving CSM data from SecurityManager.");
         }
         if (!managedStudyConfigurations.contains(studyConfiguration)) {
             throw new CSSecurityException("User doesn't have access to this study.");
         } 
         return studyConfiguration;
     }
 
     /**
      * @return the fileManager
      */
     public FileManager getFileManager() {
         return fileManager;
     }
 
     /**
      * @param fileManager the fileManager to set
      */
     public void setFileManager(FileManager fileManager) {
         this.fileManager = fileManager;
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public List<AnnotationDefinition> getMatchingDefinitions(List<String> keywords) {
         return getDao().findMatches(keywords);
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public List<CommonDataElement> getMatchingDataElements(List<String> keywords) {
         return caDSRFacade.retreiveCandidateDataElements(keywords);
     }
 
     /**
      * @return the caDSRFacade
      */
     public CaDSRFacade getCaDSRFacade() {
         return caDSRFacade;
     }
 
     /**
      * @param caDSRFacade the caDSRFacade to set
      */
     public void setCaDSRFacade(CaDSRFacade caDSRFacade) {
         this.caDSRFacade = caDSRFacade;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.ExcessiveMethodLength")
     @Transactional(rollbackFor = {ConnectionException.class, ValidationException.class })
     public void setDataElement(AnnotationFieldDescriptor fieldDescriptor, 
                                 CommonDataElement dataElement, 
                                 Study study, 
                                 EntityTypeEnum entityType,
                                 String keywords) 
     throws ConnectionException, ValidationException {
         if (dataElement.getValueDomain() == null) {
             retrieveValueDomain(dataElement);
         }
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.setCommonDataElement(dataElement);
         addDefinitionToStudy(fieldDescriptor, study, entityType, annotationDefinition);
         if (dataElement.getDefinition().length() > DEFINITION_LENGTH) {
             dataElement.setDefinition(dataElement.getDefinition().substring(0, DEFINITION_LENGTH - 7) + "...");
         }
         annotationDefinition.setKeywords(dataElement.getLongName());
         daoSave(annotationDefinition);
         validateAnnotationDefinition(fieldDescriptor, study, entityType, annotationDefinition);
         daoSave(fieldDescriptor);
     }
 
     private void validateAnnotationDefinition(AnnotationFieldDescriptor fieldDescriptor, 
             Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) throws ValidationException {
         Set<Object> uniqueValues = validateAndRetrieveUniqueValues(study, entityType, 
                 fieldDescriptor, annotationDefinition); 
         if (!annotationDefinition.getPermissibleValueCollection().isEmpty()) {
             validateValuesWithPermissibleValues(uniqueValues, annotationDefinition);
         }
     }
     
     private Set<Object> validateAndRetrieveUniqueValues(Study study, EntityTypeEnum entityType, 
             AnnotationFieldDescriptor fieldDescriptor, 
             AnnotationDefinition annotationDefinition) throws ValidationException {
         AnnotationTypeEnum annotationType = annotationDefinition.getDataType();
         if (annotationType == null) {
             throw new IllegalArgumentException("Data Type for the Annotation Definition is unknown.");
         }
         Set<Object> valueObjects = new HashSet<Object>();
         for (FileColumn fileColumn : getDao().getFileColumnsUsingAnnotationFieldDescriptor(fieldDescriptor)) {
             valueObjects.addAll(retrieveAndValidateValuesForFileColumn(study, entityType, annotationDefinition, 
                     annotationType, fileColumn));
         }
         return valueObjects;
     }
 
     @SuppressWarnings("unchecked") // For the "class" type.
     private Set<Object> retrieveAndValidateValuesForFileColumn(Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition, AnnotationTypeEnum annotationType,
             FileColumn fileColumn)
             throws ValidationException {
         if (Boolean.valueOf(fileColumn.getAnnotationFile().getCurrentlyLoaded())) {
             annotationDefinition.validateValuesWithType();
             return new HashSet<Object>(getDao().retrieveUniqueValuesForStudyAnnotation(study, annotationDefinition, 
                     entityType, annotationType.getClassType()));
         } else {
             return fileColumn.getUniqueDataValues(annotationType.getClassType());
         }
     }
     
     private void validateValuesWithPermissibleValues(Set<Object> uniqueValues, 
             AnnotationDefinition annotationDefinition) throws ValidationException {
         ValidationResult validationResult = new ValidationResult();
         validationResult.setValid(true);
         Set<String> invalidValues =  
                 PermissibleValueUtil.retrieveValuesNotPermissible(uniqueValues, annotationDefinition);
         if (!invalidValues.isEmpty()) {
             StringBuffer message = new StringBuffer();
             message.append("The following values exist that are NOT permissible for '" 
                             + annotationDefinition.getDisplayName() + "': {");
             for (String invalidValue : invalidValues) {
                 message.append(" '" + invalidValue + "' ");
             }
             message.append("} Please select a different Data Element.");
             validationResult.setValid(false);
             validationResult.setInvalidMessage(message.toString());
             throw new ValidationException(validationResult);
         }
     }
 
     private void retrieveValueDomain(CommonDataElement dataElement)
             throws ConnectionException {
         ValueDomain valueDomain;
         String dataElementVersion = dataElement.getVersion(); 
         valueDomain = caDSRFacade.retrieveValueDomainForDataElement(dataElement.getPublicID(), 
                                                     NumberUtils.isNumber(dataElementVersion) 
                                                     ? Float.valueOf(dataElementVersion) : null);
         dataElement.setValueDomain(valueDomain);
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(rollbackFor = ValidationException.class)
     public void setDefinition(Study study, AnnotationFieldDescriptor fieldDescriptor, 
             AnnotationDefinition annotationDefinition, EntityTypeEnum entityType) throws ValidationException {
         if (fieldDescriptor.getDefinition() == null 
             || !fieldDescriptor.getDefinition().equals(annotationDefinition)) {
             addDefinitionToStudy(fieldDescriptor, study, entityType, annotationDefinition);
             validateAnnotationDefinition(fieldDescriptor, study, entityType, annotationDefinition);
             
             daoSave(annotationDefinition);
             daoSave(fieldDescriptor);
             daoSave(study);
         }
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
 
     /**
      * @return the caArrayFacade
      */
     public CaArrayFacade getCaArrayFacade() {
         return caArrayFacade;
     }
 
     /**
      * @param caArrayFacade the caArrayFacade to set
      */
     public void setCaArrayFacade(CaArrayFacade caArrayFacade) {
         this.caArrayFacade = caArrayFacade;
     }
 
     /**
      * {@inheritDoc}
      */
     public File saveFileToStudyDirectory(StudyConfiguration studyConfiguration, File file) throws IOException {
         return fileManager.storeStudyFile(file, file.getName(), studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public ImageAnnotationConfiguration addImageAnnotationFile(
             ImageDataSourceConfiguration imageDataSourceConfiguration,
             File inputFile, String filename, boolean createNewAnnotationDefinition)
     throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename,
                 imageDataSourceConfiguration.getStudyConfiguration());
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, getDao(), 
                 imageDataSourceConfiguration.getStudyConfiguration(),
                EntityTypeEnum.IMAGE, createNewAnnotationDefinition);
         ImageAnnotationConfiguration imageAnnotationConfiguration = 
             new ImageAnnotationConfiguration(annotationFile, imageDataSourceConfiguration);
         imageAnnotationConfiguration.setImageDataSourceConfiguration(imageDataSourceConfiguration);
         imageDataSourceConfiguration.setImageAnnotationConfiguration(imageAnnotationConfiguration);
         imageDataSourceConfiguration.setStatus(retrieveImageSourceStatus(imageDataSourceConfiguration
                 .getImageAnnotationConfiguration()));
         daoSave(imageDataSourceConfiguration.getStudyConfiguration());
         return imageAnnotationConfiguration;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addImageSource(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource) 
         throws ConnectionException {
         addImageSourceToStudy(studyConfiguration, imageSource);
         loadImageSource(imageSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void addImageSourceToStudy(StudyConfiguration studyConfiguration, 
             ImageDataSourceConfiguration imageSource) {
         imageSource.setStudyConfiguration(studyConfiguration);
         studyConfiguration.getImageDataSources().add(imageSource);
         daoSave(imageSource);
         daoSave(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void loadImageSource(ImageDataSourceConfiguration imageSource) throws ConnectionException {
         List<ImageSeriesAcquisition> acquisitions = getNciaFacade().getImageSeriesAcquisitions(
                 imageSource.getCollectionName(), imageSource.getServerProfile());
         imageSource.getImageSeriesAcquisitions().addAll(acquisitions);
         for (ImageSeriesAcquisition acquisition : acquisitions) {
             acquisition.setImageDataSource(imageSource);
         }
         imageSource.setStatus(Status.LOADED);
         daoSave(imageSource);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadImageAnnotation(ImageDataSourceConfiguration imageDataSource) throws ValidationException {
         ImageAnnotationConfiguration imageAnnotationConfiguration = imageDataSource.getImageAnnotationConfiguration();
         if (validateAnnotationFieldDescriptors(imageDataSource.getStudyConfiguration(), 
                 imageAnnotationConfiguration.getAnnotationDescriptors(), EntityTypeEnum.IMAGESERIES)) {
             imageAnnotationConfiguration.loadAnnontation();
             imageDataSource.setStatus(retrieveImageSourceStatus(imageAnnotationConfiguration));
             daoSave(imageDataSource);
         } else {
             throw new ValidationException("Unable to load clinical source due to invalid values being loaded.  " 
                 + "Check the annotations on the edit screen for more details.");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void mapImageSeriesAcquisitions(StudyConfiguration studyConfiguration, 
             ImageDataSourceConfiguration imageSource, File mappingFile, ImageDataSourceMappingTypeEnum mappingType)
         throws ValidationException, IOException {
         new ImageSeriesAcquisitionMappingHelper(studyConfiguration, mappingFile, 
                 mappingType, imageSource).mapImageSeries();
         imageSource.setStatus(retrieveImageSourceStatus(imageSource.getImageAnnotationConfiguration()));
         daoSave(imageSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void updateImageDataSourceStatus(StudyConfiguration studyConfiguration) {
         for (ImageDataSourceConfiguration imageSource : studyConfiguration.getImageDataSources()) {
             if (Status.PROCESSING.equals(imageSource.getStatus())) {
                 continue;
             }
             Status status = retrieveImageSourceStatus(imageSource.getImageAnnotationConfiguration());
             if (imageSource.getStatus() != status) {
                 imageSource.setStatus(status);
                 daoSave(imageSource);
             }
         }
     }
 
     private Status retrieveImageSourceStatus(ImageAnnotationConfiguration annotationConfiguration) {
         if (annotationConfiguration != null) {
             if (annotationConfiguration.isLoadable() 
                  && !annotationConfiguration.isCurrentlyLoaded()) {
                 return Status.NOT_LOADED;
             } else if (annotationConfiguration.isCurrentlyLoaded()) {
                 return Status.LOADED;
             } else if (!annotationConfiguration.isLoadable()) {
                 return Status.DEFINITION_INCOMPLETE;
             }
         } 
         return Status.LOADED;
     }
     
     /**
      * {@inheritDoc}
      */
     public ImageDataSourceConfiguration getRefreshedImageSource(Long id) {
         ImageDataSourceConfiguration imagingSource = new ImageDataSourceConfiguration();
         imagingSource.setId(id);
         imagingSource = getRefreshedEntity(imagingSource);
         HibernateUtil.loadCollection(imagingSource.getStudyConfiguration());
         return imagingSource;
     }
 
     /**
      * {@inheritDoc}
      */
     public AnnotationDefinition createDefinition(AnnotationFieldDescriptor descriptor, 
                                                  Study study, 
                                                  EntityTypeEnum entityType,
                                                  AnnotationTypeEnum annotationType) throws ValidationException {
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.getCommonDataElement().setLongName(descriptor.getName());
         annotationDefinition.getCommonDataElement().getValueDomain().setDataType(annotationType);
         annotationDefinition.setKeywords(annotationDefinition.getDisplayName());
         addDefinitionToStudy(descriptor, study, entityType, annotationDefinition);
         daoSave(annotationDefinition);
         daoSave(descriptor);
         daoSave(study);
         return annotationDefinition;
     }
     
     @SuppressWarnings({ "PMD.ExcessiveMethodLength" }) // Switch Statement and null checks
     private void addDefinitionToStudy(AnnotationFieldDescriptor descriptor, Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) throws ValidationException {
         AnnotationDefinition annotationDefinitionToRemove = null;
         if (descriptor.getDefinition() != null) {
             annotationDefinitionToRemove = descriptor.getDefinition();
             moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
             if (EntityTypeEnum.SUBJECT.equals(entityType)) {
                 moveDefinitionInSurvivalDefinitions(study, annotationDefinitionToRemove, annotationDefinition);
             }
         }
         descriptor.setDefinition(annotationDefinition);
         descriptor.setAnnotationEntityType(entityType);
     }
 
     /**
      * Moves AbstractAnnotationValues from one AnnotationDefinition to another.
      * @param study - Study that the values belong to.
      * @param annotationDefinition - new AnnotationDefinition where the Values will belong.
      * @param annotationDefinitionToRemove - Old AnnotationDefinition.
      * @throws ValidationException if unable to move old values to new definition.
      */
     private void moveValuesToNewDefinition(Study study, AnnotationDefinition annotationDefinition,
             AnnotationDefinition annotationDefinitionToRemove) throws ValidationException {
         if (annotationDefinitionToRemove.getAnnotationValueCollection() != null 
             && !annotationDefinitionToRemove.getAnnotationValueCollection().isEmpty()) {
             List<AbstractAnnotationValue> valuesToConvert = new ArrayList<AbstractAnnotationValue>();
             for (AbstractAnnotationValue value : annotationDefinitionToRemove.getAnnotationValueCollection()) {
                 if (studyContainsAnnotationValue(value, study)) {
                     valuesToConvert.add(value); // To not get a ConcurrentModificationException.
                 }
             }
             for (AbstractAnnotationValue valueToConvert : valuesToConvert) {
                 valueToConvert.convertAnnotationValue(annotationDefinition);
             }
         }
     }
     
     private boolean studyContainsAnnotationValue(AbstractAnnotationValue value, Study study) {
         if (value.getSubjectAnnotation() != null 
                 && study.equals(value.getSubjectAnnotation().getStudySubjectAssignment().getStudy())) {
             return true;
         } else if (value.getImageSeries() != null 
                 && study.equals(value.getImageSeries().getImageStudy().getAssignment().getStudy())) {
             return true;
         } else if (value.getSampleAcquisition() != null
                 && study.equals(value.getSampleAcquisition().getAssignment().getStudy())) { 
             return true;
         } else if (value.getImage() != null 
                 && study.equals(value.getImage().getSeries().getImageStudy().getAssignment().getStudy())) {
             return true;
         }
         return false;
     }
     
     private void moveDefinitionInSurvivalDefinitions(Study study, 
             AnnotationDefinition oldDefinition, AnnotationDefinition newDefinition) {
         for (SurvivalValueDefinition definition : study.getSurvivalValueDefinitionCollection()) {
             if (oldDefinition.equals(definition.getSurvivalStartDate())) {
                 definition.setSurvivalStartDate(newDefinition);
             }
             if (oldDefinition.equals(definition.getLastFollowupDate())) {
                 definition.setLastFollowupDate(newDefinition);
             }
             if (oldDefinition.equals(definition.getDeathDate())) {
                 definition.setDeathDate(newDefinition);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void addExternalLinksToStudy(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList) 
         throws ValidationException, IOException {
         ExternalLinksLoader.loadLinks(externalLinkList);
         studyConfiguration.getExternalLinkLists().add(externalLinkList);
         daoSave(studyConfiguration);
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
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public boolean isDuplicateStudyName(Study study, String username) {
         return getDao().isDuplicateStudyName(study, username);
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeSurvivalValueDefinition(Study study, SurvivalValueDefinition survivalValueDefinition) {
        study.getSurvivalValueDefinitionCollection().remove(survivalValueDefinition);
        Collection <SurvivalValueDefinition> objectsToRemove = new HashSet<SurvivalValueDefinition>();
        objectsToRemove.add(survivalValueDefinition);
        getDao().removeObjects(objectsToRemove);
        daoSave(study);
     }
 
     /**
      * {@inheritDoc}
      */
     public ImageDataSourceConfiguration retrieveImageDataSource(Study study) {
         ImageDataSourceConfiguration dataSource = getDao().retrieveImagingDataSourceForStudy(study);
         if (dataSource != null) {
             Hibernate.initialize(dataSource.getServerProfile());
         }
         return dataSource;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws IOException 
      */
     public void saveCopyNumberMappingFile(GenomicDataSourceConfiguration source,
             File mappingFile, String filename) throws IOException {
         File savedFile = getFileManager().storeStudyFile(mappingFile, filename, source.getStudyConfiguration());
         if (source.getCopyNumberDataConfiguration() == null) {
             source.setCopyNumberDataConfiguration(new CopyNumberDataConfiguration());
         }
         source.getCopyNumberDataConfiguration().setMappingFilePath(savedFile.getAbsolutePath());
         daoSave(source);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws IOException 
      */
     public void saveSampleMappingFile(GenomicDataSourceConfiguration source,
             File mappingFile, String filename) throws IOException {
         File savedFile = getFileManager().storeStudyFile(mappingFile, filename, source.getStudyConfiguration());
         source.setSampleMappingFilePath(savedFile.getAbsolutePath());
         daoSave(source);
     }
 
     /**
      * @param securityManager the securityManager to set
      */
     public void setSecurityManager(SecurityManager securityManager) {
         this.securityManager = securityManager;
     }
 
    /**
     * {@inheritDoc}
     */
     public void setLastModifiedByCurrentUser(StudyConfiguration studyConfiguration, UserWorkspace lastModifiedBy) {
         studyConfiguration.setLastModifiedBy(lastModifiedBy);
         studyConfiguration.setLastModifiedDate(new Date());
         daoSave(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(rollbackFor = {ConnectionException.class, ValidationException.class })
     public void saveAnnotationGroup(AnnotationGroup annotationGroup,
             StudyConfiguration studyConfiguration, File annotationGroupFile)
     throws ValidationException, ConnectionException, IOException {
         if (annotationGroup.getStudy() != studyConfiguration.getStudy()) {
             annotationGroup.setStudy(studyConfiguration.getStudy());
             studyConfiguration.getStudy().getAnnotationGroups().add(annotationGroup);
         }
         if (annotationGroupFile != null) {
             uploadAnnotationGroup(studyConfiguration, annotationGroup, annotationGroupFile);
         }
         daoSave(annotationGroup);
         daoSave(studyConfiguration);
     }
     
     private void uploadAnnotationGroup(StudyConfiguration studyConfiguration,
             AnnotationGroup annotationGroup, File uploadFile)
     throws ConnectionException, ValidationException, IOException {
         AnnotationGroupUploadFileHandler uploadFileHandler = new AnnotationGroupUploadFileHandler(uploadFile);
         List<AnnotationGroupUploadContent> uploadContents = uploadFileHandler.extractUploadData();
         if (uploadContents != null) {
             StringBuffer validationMsg = new StringBuffer();
             for (AnnotationGroupUploadContent uploadContent : uploadContents) {
                 try {
                     createAnnotation(studyConfiguration, annotationGroup, uploadContent);
                 } catch (ValidationException e) {
                     validationMsg.append(e.getMessage());
                 }
             }
             if (validationMsg.length() > 0) {
                 throw new ValidationException(validationMsg.toString());
             }
         }
     }
         
     private void createAnnotation(StudyConfiguration studyConfiguration,
             AnnotationGroup annotationGroup, AnnotationGroupUploadContent uploadContent)
     throws ConnectionException, ValidationException {
         checkForExistingAnnotationFieldDescriptor(studyConfiguration, uploadContent.getColumnName());
         AnnotationFieldDescriptor annotationFieldDescriptor = uploadContent.createAnnotationFieldDescriptor();
         annotationFieldDescriptor.setAnnotationGroup(annotationGroup);
         if (!AnnotationFieldType.IDENTIFIER.equals(uploadContent.getAnnotationType())) {
             AnnotationDefinition annotationDefinition = createAnnotationDefinition(uploadContent);
             annotationFieldDescriptor.setDefinition(annotationDefinition);
         }
         annotationGroup.getAnnotationFieldDescriptors().add(annotationFieldDescriptor);
     }
 
     private void checkForExistingAnnotationFieldDescriptor(StudyConfiguration studyConfiguration, String name)
     throws ValidationException {
         if (studyConfiguration.getExistingFieldDescriptorInStudy(name) != null) {
             throw new ValidationException("Definition: " + name + " already exist.\n");
         }
     }
 
     private AnnotationDefinition createAnnotationDefinition(AnnotationGroupUploadContent uploadContent)
     throws ConnectionException, ValidationException {
         AnnotationDefinition annotationDefinition = getDao().getAnnotationDefinition(
                 uploadContent.getDefinitionName(), uploadContent.getDataType());
         if (annotationDefinition == null) {
             if (uploadContent.getCdeId() != null) {
                 annotationDefinition = getCaDsrAnnotationDefinition(uploadContent.getCdeId(),
                     uploadContent.getVersion());
                 annotationDefinition.setKeywords(uploadContent.getDefinitionName());
             } else {
                 annotationDefinition = uploadContent.createAnnotationDefinition();
             }
         }
         return annotationDefinition;
     }
     
     private AnnotationDefinition getCaDsrAnnotationDefinition(Long cdeId, Float version)
     throws ConnectionException, ValidationException {
         AnnotationDefinition annotationDefinition = getDao().getAnnotationDefinition(
                 cdeId, version);
         if (annotationDefinition == null) {
             annotationDefinition = new AnnotationDefinition();
             annotationDefinition.setCommonDataElement(retrieveDataElement(
                     cdeId, version));
             retrieveValueDomain(annotationDefinition.getCommonDataElement());
             
         }
         return annotationDefinition;
     }
 
     private CommonDataElement retrieveDataElement(Long dataElementId, Float version)
     throws ConnectionException, ValidationException {
         CommonDataElement commonDataElement = caDSRFacade.retrieveDataElement(dataElementId, version);
         if (commonDataElement == null) {
             throw new ValidationException("Error cdeId not found: " + dataElementId.toString());
         }
         return commonDataElement;
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, AnnotationGroup annotationGroup) {
         studyConfiguration.getStudy().getAnnotationGroups().remove(annotationGroup);
         getDao().delete(annotationGroup);
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(rollbackFor = ValidationException.class)
     public AnnotationFieldDescriptor updateFieldDescriptorType(AnnotationFieldDescriptor fieldDescriptor, 
             AnnotationFieldType type) throws ValidationException {
         AnnotationFieldDescriptor returnFieldDescriptor = fieldDescriptor;
         for (FileColumn fileColumn : getDao().getFileColumnsUsingAnnotationFieldDescriptor(fieldDescriptor)) {
             if (AnnotationFieldType.IDENTIFIER.equals(type)) {
                 fileColumn.checkValidIdentifierColumn();
                 fileColumn.getAnnotationFile().setIdentifierColumn(fileColumn);
                 returnFieldDescriptor = fileColumn.getFieldDescriptor();
                 daoSave(fileColumn);
             } else if (AnnotationFieldType.TIMEPOINT.equals(type)) {
                 fileColumn.getAnnotationFile().setTimepointColumn(fileColumn);
                 returnFieldDescriptor = fileColumn.getFieldDescriptor();
                 daoSave(fileColumn);
             }
         }
         
         returnFieldDescriptor.setType(type);
         daoSave(returnFieldDescriptor);
         return returnFieldDescriptor;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.EmptyCatchBlock") // See message inside catch block.
     public Set<String> getAvailableValuesForFieldDescriptor(AnnotationFieldDescriptor fieldDescriptor) 
     throws ValidationException {
         Set<String> allAvailableValues = new HashSet<String>();
         for (FileColumn fileColumn : getDao().getFileColumnsUsingAnnotationFieldDescriptor(fieldDescriptor)) {
             List<String> fileDataValues = fileColumn.getAnnotationFile() != null ? fileColumn.getDataValues()
                     : new ArrayList<String>();
             if (AnnotationTypeEnum.DATE.equals(fieldDescriptor.getDefinition().getDataType())) {
                 try {
                     fileDataValues = DateUtil.toString(fileDataValues);
                 } catch (ParseException e) {
                     // noop - if it doesn't fit the date format just let it keep going.
                     // This function is for JSP display so it can't fail.
                 }
             }
             allAvailableValues.addAll(AnnotationValueUtil.getAdditionalValue(fieldDescriptor.getDefinition()
                     .getAnnotationValueCollection(), fileDataValues, PermissibleValueUtil
                     .getDisplayPermissibleValue(fieldDescriptor.getDefinition().getPermissibleValueCollection())));
         }
         return allAvailableValues;
     }
 
     /**
      * {@inheritDoc}
      */
     public void daoSave(Object persistentObject) {
         getDao().save(persistentObject);
     }
 }

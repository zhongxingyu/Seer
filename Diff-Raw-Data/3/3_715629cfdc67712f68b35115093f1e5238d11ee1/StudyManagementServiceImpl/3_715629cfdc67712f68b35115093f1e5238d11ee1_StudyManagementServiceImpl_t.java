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
 
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.common.PermissibleValueUtil;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
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
 public class StudyManagementServiceImpl implements StudyManagementService {
 
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(StudyManagementServiceImpl.class);
     private static final int DEFINITION_LENGTH = 1000;
     private CaIntegrator2Dao dao;
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
         dao.save(definition);
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
     public void saveGenomicDataSource(GenomicDataSourceConfiguration genomicSource) {
         dao.save(genomicSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void saveImagingDataSource(ImageDataSourceConfiguration imagingSource) {
         dao.save(imagingSource);
     }
 
     /**
      *  {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration) throws CSException {
         securityManager.deleteProtectionElement(studyConfiguration);
         fileManager.deleteStudyDirectory(studyConfiguration);
         getWorkspaceService().unsubscribeAll(studyConfiguration.getStudy());
         dao.save(studyConfiguration.getUserWorkspace());
         studyConfiguration.setUserWorkspace(null);
         dao.delete(studyConfiguration);
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
             File inputFile, String filename) throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename, studyConfiguration);
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, dao);
         DelimitedTextClinicalSourceConfiguration clinicalSourceConfig = 
             new DelimitedTextClinicalSourceConfiguration(annotationFile, studyConfiguration);
         dao.save(studyConfiguration);
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
         dao.save(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public StudyLogo retrieveStudyLogo(Long studyId, String studyShortTitleText) {
         return dao.retrieveStudyLogo(studyId, studyShortTitleText);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadClinicalAnnotation(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSourceConfiguration)
         throws ValidationException {
         clinicalSourceConfiguration.loadAnnontation();
         save(studyConfiguration);
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
         dao.removeObjects(studyConfiguration.removeObsoleteSubjectAssignment());
         refreshAnnotationDefinitions(studyConfiguration.getStudy());
         save(studyConfiguration);
     }
     
     private void refreshAnnotationDefinitions(Study study) {
         for (AnnotationDefinition annotationDefinition : study.getSubjectAnnotationCollection()) {
             dao.refresh(annotationDefinition);
         }
     }
     
     private void deleteClinicalAnnotation(StudyConfiguration studyConfiguration) {
         Study study = studyConfiguration.getStudy();
         for (StudySubjectAssignment studySubjectAssignment : study.getAssignmentCollection()) {
             for (SubjectAnnotation subjectAnnotation : studySubjectAssignment.getSubjectAnnotationCollection()) {
                 dao.delete(subjectAnnotation);
             }
             studySubjectAssignment.getSubjectAnnotationCollection().clear();
         }
         study.getSubjectAnnotationCollection().clear();
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSource) throws ValidationException {
         deleteClinicalAnnotation(studyConfiguration);
         studyConfiguration.getClinicalConfigurationCollection().remove(clinicalSource);
         dao.delete(clinicalSource);
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
                     dao.delete(array);
                 }
             }
             sample.getArrayCollection().clear();
         }
         dao.delete(genomicSource);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource) 
         throws ValidationException {
         studyConfiguration.getImageDataSources().remove(imageSource);
         dao.delete(imageSource);
         
         reLoadImageAnnotation(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList) {
         studyConfiguration.getExternalLinkLists().remove(externalLinkList);
         dao.delete(externalLinkList);
     }
     
     private void deleteImageAnnotation(StudyConfiguration studyConfiguration) {
         Study study = studyConfiguration.getStudy();
         for (StudySubjectAssignment studySubjectAssignment : study.getAssignmentCollection()) {
             for (ImageSeriesAcquisition imageSeriesAcquisition : studySubjectAssignment.getImageStudyCollection()) {
                 dao.delete(imageSeriesAcquisition);
             }
             studySubjectAssignment.getImageStudyCollection().clear();
         }
         studyConfiguration.getStudy().getImageSeriesAnnotationCollection().clear();
     }
     
     private void reLoadImageAnnotation(StudyConfiguration studyConfiguration) throws ValidationException {
         deleteImageAnnotation(studyConfiguration);
         for (ImageDataSourceConfiguration configuration 
                 : studyConfiguration.getImageDataSources()) {
             if (configuration.getImageAnnotationConfiguration() != null) {
                 configuration.getImageAnnotationConfiguration().reLoadAnnontation();
             }
         }
         dao.removeObjects(studyConfiguration.removeObsoleteSubjectAssignment());
         refreshAnnotationDefinitions(studyConfiguration.getStudy());
         save(studyConfiguration);
     }
     
     private void persist(StudyConfiguration studyConfiguration) {
         for (StudySubjectAssignment assignment : studyConfiguration.getStudy().getAssignmentCollection()) {
             saveSubjectAnnotations(assignment.getSubjectAnnotationCollection());
             saveSampleAcquisitions(assignment.getSampleAcquisitionCollection());
             dao.save(assignment.getSubject());
             dao.save(assignment);
         }
         dao.save(studyConfiguration);
     }
 
     private void saveSampleAcquisitions(Collection<SampleAcquisition> sampleAcquisitionCollection) {
         for (SampleAcquisition sampleAcquisition : sampleAcquisitionCollection) {
             dao.save(sampleAcquisition.getSample());
             dao.save(sampleAcquisition);
         }
     }
 
     private void saveSubjectAnnotations(Collection<SubjectAnnotation> subjectAnnotations) {
         for (SubjectAnnotation annotation : subjectAnnotations) {
             dao.save(annotation.getAnnotationValue());
             dao.save(annotation);
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
      * @param dao the dao to set
      */
     public void setDao(CaIntegrator2Dao dao) {
         this.dao = dao;
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
         dao.save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadGenomicSource(GenomicDataSourceConfiguration genomicSource) 
     throws ConnectionException, ExperimentNotFoundException {
         if (GenomicDataSourceDataTypeEnum.EXPRESSION.equals(genomicSource.getDataType())) {
             handleLoadGeneExpression(genomicSource);
         } else if (GenomicDataSourceDataTypeEnum.COPY_NUMBER.equals(genomicSource.getDataType())) {
             handleLoadCopyNumber(genomicSource);
         }
         genomicSource.setStatus(Status.LOADED);
         dao.save(genomicSource);
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
     
     private void handleLoadCopyNumber(GenomicDataSourceConfiguration genomicSource) throws ConnectionException,
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
         genomicSource = getRefreshedStudyEntity(genomicSource);
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
         studyConfiguration = getRefreshedStudyEntity(studyConfiguration);
         Set<StudyConfiguration> managedStudyConfigurations = new HashSet<StudyConfiguration>();
         try {
             managedStudyConfigurations = 
                 securityManager.retrieveManagedStudyConfigurations(username, dao.getStudies(username)); 
         } catch (CSException e) {
             throw new IllegalStateException("Error retrieving CSM data from SecurityManager.");
         }
         if (!managedStudyConfigurations.contains(studyConfiguration)) {
             throw new CSSecurityException("User doesn't have access to this study.");
         } 
         return studyConfiguration;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Transactional(readOnly = true)
     public <T> T getRefreshedStudyEntity(T entity) {
         Long id;
         try {
             id = (Long) entity.getClass().getMethod("getId").invoke(entity);
         } catch (Exception e) {
             throw new IllegalArgumentException("Entity doesn't have a getId() method", e);
         }
         if (id == null) {
             throw new IllegalArgumentException("Id was null");
         }
         return (T) dao.get(id, entity.getClass());
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
         return dao.findMatches(keywords);
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
     public void setDataElement(FileColumn fileColumn, 
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
         addDefinitionToStudy(fileColumn.getFieldDescriptor(), study, entityType, annotationDefinition);
         if (dataElement.getDefinition().length() > DEFINITION_LENGTH) {
             dataElement.setDefinition(dataElement.getDefinition().substring(0, DEFINITION_LENGTH - 7) + "...");
         }
         annotationDefinition.setKeywords(dataElement.getLongName());
         dao.save(annotationDefinition);
        validateAnnotationDefinition(fileColumn, study, entityType, annotationDefinition);
         dao.save(fileColumn);
     }
 
     private void validateAnnotationDefinition(FileColumn fileColumn, Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) throws ValidationException {
         Set<Object> uniqueValues = validateAndRetrieveUniqueValues(study, entityType, 
                 fileColumn, annotationDefinition); 
         if (!annotationDefinition.getPermissibleValueCollection().isEmpty()) {
             validateValuesWithPermissibleValues(uniqueValues, annotationDefinition);
         }
     }
     
     @SuppressWarnings("unchecked") // For the "class" type.
     private Set<Object> validateAndRetrieveUniqueValues(Study study, EntityTypeEnum entityType, 
             FileColumn fileColumn, AnnotationDefinition annotationDefinition) throws ValidationException {
         AnnotationTypeEnum annotationType = annotationDefinition.getDataType();
         if (annotationType == null) {
             throw new IllegalArgumentException("Data Type for the Annotation Definition is unknown.");
         }
         Set<Object> valueObjects = new HashSet<Object>();
         if (Boolean.valueOf(fileColumn.getAnnotationFile().getCurrentlyLoaded())) {
             annotationDefinition.validateValuesWithType();
             valueObjects.addAll(dao.retrieveUniqueValuesForStudyAnnotation(study, annotationDefinition, entityType, 
                     annotationType.getClassType()));
         } else {
             valueObjects.addAll(fileColumn.getUniqueDataValues(annotationType.getClassType()));
         }
         return valueObjects;
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
                             + annotationDefinition.getDisplayName() + "': <br> {");
             for (String invalidValue : invalidValues) {
                 message.append(" '" + invalidValue + "' ");
             }
             message.append("} <br> Please select a different Data Element.");
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
     public void setDefinition(Study study, FileColumn fileColumn, AnnotationDefinition annotationDefinition, 
                                 EntityTypeEnum entityType) throws ValidationException {
         if (fileColumn.getFieldDescriptor().getDefinition() == null 
             || !fileColumn.getFieldDescriptor().getDefinition().equals(annotationDefinition)) {
             addDefinitionToStudy(fileColumn.getFieldDescriptor(), study, entityType, annotationDefinition);
             validateAnnotationDefinition(fileColumn, study, entityType, annotationDefinition);
             
             dao.save(annotationDefinition);
             dao.save(fileColumn);
             dao.save(study);
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
             File inputFile, String filename) throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename,
                 imageDataSourceConfiguration.getStudyConfiguration());
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, dao);
         ImageAnnotationConfiguration imageAnnotationConfiguration = 
             new ImageAnnotationConfiguration(annotationFile, imageDataSourceConfiguration);
         imageAnnotationConfiguration.setImageDataSourceConfiguration(imageDataSourceConfiguration);
         imageDataSourceConfiguration.setImageAnnotationConfiguration(imageAnnotationConfiguration);
         imageDataSourceConfiguration.setStatus(retrieveImageSourceStatus(imageDataSourceConfiguration
                 .getImageAnnotationConfiguration()));
         dao.save(imageAnnotationConfiguration);
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
         dao.save(imageSource);
         dao.save(studyConfiguration);
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
         dao.save(imageSource);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadImageAnnotation(ImageDataSourceConfiguration imageDataSource) throws ValidationException {
         imageDataSource.getImageAnnotationConfiguration().loadAnnontation();
         imageDataSource.setStatus(retrieveImageSourceStatus(imageDataSource.getImageAnnotationConfiguration()));
         dao.save(imageDataSource);
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
         dao.save(imageSource);
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
                 dao.save(imageSource);
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
         imagingSource = getRefreshedStudyEntity(imagingSource);
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
         dao.save(annotationDefinition);
         dao.save(descriptor);
         dao.save(study);
         return annotationDefinition;
     }
     
     @SuppressWarnings({ "PMD.ExcessiveMethodLength" }) // Switch Statement and null checks
     private void addDefinitionToStudy(AnnotationFieldDescriptor descriptor, Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) throws ValidationException {
         AnnotationDefinition annotationDefinitionToRemove = null;
         if (descriptor.getDefinition() != null) {
             annotationDefinitionToRemove = descriptor.getDefinition();
         }
         descriptor.setDefinition(annotationDefinition);
         switch(entityType) {
             case SUBJECT:
                 study.getSubjectAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getSubjectAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             case IMAGESERIES:
                 study.getImageSeriesAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getImageSeriesAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             case SAMPLE:
                 study.getSampleAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getSampleAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             default:
                 throw new IllegalStateException("Unknown EntityTypeEnum");
         }
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
     
     /**
      * {@inheritDoc}
      */
     public void addExternalLinksToStudy(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList) 
         throws ValidationException, IOException {
         ExternalLinksLoader.loadLinks(externalLinkList);
         studyConfiguration.getExternalLinkLists().add(externalLinkList);
         dao.save(studyConfiguration);
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
         return dao.isDuplicateStudyName(study, username);
     }
     
     /**
      * {@inheritDoc}
      */
     public SurvivalValueDefinition createNewSurvivalValueDefinition(Study study) {
         SurvivalValueDefinition survivalValueDefinition = new SurvivalValueDefinition();
         study.getSurvivalValueDefinitionCollection().add(survivalValueDefinition);
         survivalValueDefinition.setName("[New Name]");
         dao.save(study);
         return survivalValueDefinition;
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeSurvivalValueDefinition(Study study, SurvivalValueDefinition survivalValueDefinition) {
        study.getSurvivalValueDefinitionCollection().remove(survivalValueDefinition);
        Collection <SurvivalValueDefinition> objectsToRemove = new HashSet<SurvivalValueDefinition>();
        objectsToRemove.add(survivalValueDefinition);
        dao.removeObjects(objectsToRemove);
        dao.save(study);
     }
 
     /**
      * {@inheritDoc}
      */
     public ImageDataSourceConfiguration retrieveImageDataSource(Study study) {
         ImageDataSourceConfiguration dataSource = dao.retrieveImagingDataSourceForStudy(study);
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
         dao.save(source);
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
         dao.save(studyConfiguration);
     }
 
 
 
 }

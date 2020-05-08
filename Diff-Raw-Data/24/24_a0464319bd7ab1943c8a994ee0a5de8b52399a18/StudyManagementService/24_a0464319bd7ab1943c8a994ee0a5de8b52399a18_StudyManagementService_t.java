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
 
 import gov.nih.nci.caintegrator2.application.CaIntegrator2EntityRefresher;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.CommonDataElement;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.TimeStampable;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.InvalidImagingCollectionException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.external.caarray.ExperimentNotFoundException;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSSecurityException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Service used to create, define, deploy and update studies.
  */
 @SuppressWarnings("PMD.ExcessiveClassLength")
 public interface StudyManagementService extends CaIntegrator2EntityRefresher {
     
     /**
      * Saves a study.
      * 
      * @param studyConfiguration study to save
      */
     void save(StudyConfiguration studyConfiguration);
     
     /**
      * Saves the annotation definition.
      * @param definition to persist.
      * @throws ValidationException if values are invalid for the given type.
      */
     void save(AnnotationDefinition definition) throws ValidationException;
     
     /**
      * Saves the status/status description only for the given source.
      * @param source to save status for.
      */
     void saveSubjectSourceStatus(AbstractClinicalSourceConfiguration source);
     
     /**
      * Creates a protection element for the Study Configuration.
      * @param studyConfiguration to create protection element for.
      * @throws CSException if there's a problem creating the protection element.
      */
     void createProtectionElement(StudyConfiguration studyConfiguration) throws CSException;
     
     /**
      * Deletes a study.
      * 
      * @param studyConfiguration study to delete
      * @throws CSException if there's a problem deleting the protection element from CSM api.
      */
     void delete(StudyConfiguration studyConfiguration) throws CSException;
     
     /**
      * Deletes a clinical source.
      * 
      * @param studyConfiguration study configuration of the clinical source
      * @param clinicalSource clinical source to delete
      * @throws ValidationException fail to reload
      */
     void delete(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSource)
         throws ValidationException;
     
     /**
      * Deletes a genomic source.
      * @param studyConfiguration of the clinical source.
      * @param genomicSource to delete.
      */
     void delete(StudyConfiguration studyConfiguration, GenomicDataSourceConfiguration genomicSource);
     
     /**
      * Delete an imaging source.
      * @param studyConfiguration of the imaging source.
      * @param imageSource to delete.
      * @throws ValidationException fail to reload.
      */
     void delete(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource) 
     throws ValidationException;
     
     /**
      * Deletes an externalLinkList.
      * @param studyConfiguration of the external links.
      * @param externalLinkList to delete.
      */
     void delete(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList);
 
     /**
      * Adds a clinical annotation file for use. The file given will be copied to permanent storage allowing the
      * file provided as an argument to be removed after completion of this method.
      * 
      * @param studyConfiguration add the annotation file to this study
      * @param annotationFile annotation file to add.
      * @param filename the name with which the annotation file should be stored 
      *        (allows for the use of files with temp names as input)
      * @param createNewAnnotationDefinition create new annotation definition when one is not found
      * @return the clinical source configuration created.
      * @throws ValidationException if the file was not a valid annotation file.
      * @throws IOException if the annotation file couldn't be copied to permanent storage.
      */
     DelimitedTextClinicalSourceConfiguration addClinicalAnnotationFile(StudyConfiguration studyConfiguration, 
             File annotationFile, String filename, boolean createNewAnnotationDefinition)
     throws ValidationException, IOException;
     
     /**
      * Adds a logo to the study.
      * @param studyConfiguration add the logo to this study.
      * @param imageFile object to add.
      * @param fileName name of the file for the logo.
      * @param fileType - type of file (such as image\jpeg).
      * @throws IOException if the image file couldn't be loaded.
      */
     void addStudyLogo(StudyConfiguration studyConfiguration, 
                         File imageFile, 
                         String fileName, 
                         String fileType) throws IOException;
 
     /**
      * Retrieves study logo from the database given a study id and name.
      * @param studyId - ID of the Study object.
      * @param studyShortTitleText - Short Title Text of Study Object.
      * @return Object retrieved from database.
      */
     StudyLogo retrieveStudyLogo(Long studyId, String studyShortTitleText);
 
     /**
      * Loads a specific clinical annotation from study configuration.
      * 
      * @param studyConfiguration study configuration to load
      * @param clinicalSourceConfiguration clinical source configuration to load
      * @throws ValidationException fail to load
      * @throws InvalidFieldDescriptorException  if field descriptors are invalid.
      */
     void loadClinicalAnnotation(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSourceConfiguration)
         throws ValidationException, InvalidFieldDescriptorException;
 
     /**
      * ReLoads clinical annotations for study configuration.
      * 
      * @param studyConfiguration study configuration to load
      * @throws ValidationException fail to load
      */
     void reLoadClinicalAnnotation(StudyConfiguration studyConfiguration)
         throws ValidationException;
 
     /**
      * Unloads all clinical annotation from study configuration.
      * 
      * @param studyConfiguration study configuration to unload
      */
     void unloadAllClinicalAnnotation(StudyConfiguration studyConfiguration);
     
     /**
      * Adds a new, initialized genomic data source to the study. Samples related to this data source are
      * retrieved from the source and added to the study.
      * 
      * @param studyConfiguration study configuration to add genomic data source to
      * @param genomicSource genomic source to add
      * @throws ConnectionException if the configured server couldn't be reached.
      * @throws ExperimentNotFoundException if the experiment cannot be found.
      */
     void addGenomicSource(StudyConfiguration studyConfiguration, GenomicDataSourceConfiguration genomicSource) 
     throws ConnectionException, ExperimentNotFoundException;
     
     
     /**
      * Adds a new genomic data source to the study.
      * @param studyConfiguration to add source to.
      * @param genomicSource to add.
      */
     void addGenomicSourceToStudy(StudyConfiguration studyConfiguration, 
             GenomicDataSourceConfiguration genomicSource);
     
     /**
      * Samples related to this data source are retrieved from the source and added to the study.
      * 
      * @param genomicSource genomic source to add
      * @throws ConnectionException if the configured server couldn't be reached.
      * @throws ExperimentNotFoundException if the experiment cannot be found.
      */
     void loadGenomicSource(GenomicDataSourceConfiguration genomicSource) 
     throws ConnectionException, ExperimentNotFoundException;
     
     /**
      * Retrieves refreshed genomic source.
      * @param id of the genomic source.
      * @return refreshed genomic source.
      */
     GenomicDataSourceConfiguration getRefreshedGenomicSource(Long id);
     
     /**
      * Retrieves refreshed clinical source.
      * @param id of the clinical source.
      * @return refreshed clinical source.
      */
     StudyConfiguration getRefreshedStudyConfiguration(Long id);
     
     /**
      * Retrieves refreshed study configuration.
      * @param username of the user trying to access study.
      * @param id of the study configuration.
      * @return refreshed study configuration.
      * @throws CSSecurityException if user doesn't have access to this study. 
      */
     StudyConfiguration getRefreshedSecureStudyConfiguration(String username, Long id) 
     throws CSSecurityException;
     
     /**
      * Saves a file to the study directory.
      * @param studyConfiguration to get the directory.
      * @param file to save.
      * @return saved file.
      * @throws IOException if unable to read/save file.
      */
     File saveFileToStudyDirectory(StudyConfiguration studyConfiguration, File file) throws IOException;
 
     /**
      * Adds a new, initialized image data source to the study. The <code>ImageSeriesAcquisition</code> related to this 
      * data source is retrieved from the source and added to the study.
      * 
      * @param studyConfiguration study configuration to add image data source to
      * @param imageSource image source to add
      * @throws ConnectionException if the configured server couldn't be reached.
      * @throws InvalidImagingCollectionException if there are no images associated with collection.
      */
     void addImageSource(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource) 
     throws ConnectionException, InvalidImagingCollectionException;
     
     /**
      * Adds a new image source to study.
      * @param studyConfiguration to add image source to.
      * @param imageSource to add to study.
      */
     void addImageSourceToStudy(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource);
     
     /**
      * Loads the image source.
      * @param imageSource to load.
      * @throws ConnectionException if can't connect to imaging server.
      * @throws InvalidImagingCollectionException if there are no images associated with collection.
      */
     void loadImageSource(ImageDataSourceConfiguration imageSource) 
     throws ConnectionException, InvalidImagingCollectionException;
     
     
     /**
      * Adds an image series annotation file to the study. The file given will be copied to permanent storage 
      * allowing the file provided as an argument to be removed after completion of this method.
      * 
      * @param imageDataSourceConfiguration add the annotation file to this image data source
      * @param annotationFile annotation file to add.
      * @param filename the name with which the annotation file should be stored 
      *        (allows for the use of files with temp names as input)
      * @param createNewAnnotationDefinition create new annotation definition when one is not found
      * @return the clinical source configuration created.
      * @throws ValidationException if the file was not a valid annotation file.
      * @throws IOException if the annotation file couldn't be copied to permanent storage.
      */
     ImageAnnotationConfiguration addImageAnnotationFile(ImageDataSourceConfiguration imageDataSourceConfiguration, 
             File annotationFile, String filename, boolean createNewAnnotationDefinition)
     throws ValidationException, IOException;
 
     /**
      * Adds aim annotation source to the image source configuration.
      * @param aimConnection connection to aim.
      * @param imageSource imaging source.
      * @return annotation configuration.
      */
     ImageAnnotationConfiguration addAimAnnotationSource(ServerConnectionProfile aimConnection, 
             ImageDataSourceConfiguration imageSource);
     
     /**
      * Loads image annotations given an image data source configuration.
      * 
      * @param imageDataSource to load
      * @throws ValidationException fail to load
      */
     void loadImageAnnotation(ImageDataSourceConfiguration imageDataSource) throws ValidationException;
     
     /**
      * Loads the aim annotations.
      * @param imageSource image source to load annotations for.
      * @throws ConnectionException unable to connect to AIM service.
      * @throws ValidationException if unable to create annotation field descriptor.
      */
     void loadAimAnnotations(ImageDataSourceConfiguration imageSource) throws ConnectionException, ValidationException;
 
     /**
      * Updates the status of the imaging sources.
      * @param studyConfiguration which contains the imaging sources.
      */
     void updateImageDataSourceStatus(StudyConfiguration studyConfiguration);
     
     /**
      * Returns the refreshed ImageDataSourceConfiguration attached to the current Hibernate session.
      * @param id of the image data source configuration.
      * @return refreshed entity.
      */
     ImageDataSourceConfiguration getRefreshedImageSource(Long id);
 
     /**
      * Returns an ordered list of existing definitions that match the keywords contained
      * in the given column.
      * 
      * @param keywords match definitions for these keywords.
      * @return the list of matching candidate definitions.
      */
     List<AnnotationDefinition> getMatchingDefinitions(List<String> keywords);
 
     /**
      * Returns an ordered list of existing CaDSR data elements that match the keywords contained
      * in the given column.
      * 
      * @param keywords match data elements for these keywords.
      * @return the list of matching candidate data elements.
      */
     List<CommonDataElement> getMatchingDataElements(List<String> keywords);
 
     /**
      * Selects an existing CaDSR data element as the definition for a column.
 
      * @param fieldDescriptor fieldDescriptor receiving definition.
      * @param dataElement the selected data element.
      * @param study the study that the FileColumn belongs to.
      * @param entityType the entityType for the data element.
      * @param keywords the keywords that were used to find this data element.
      * @throws ConnectionException if underlying data sources couldn't be reached.
      * @throws ValidationException if the data element selected is invalid for this definition.
      */
     void setDataElement(AnnotationFieldDescriptor fieldDescriptor, CommonDataElement dataElement, 
             Study study, EntityTypeEnum entityType, String keywords)
     throws ConnectionException, ValidationException;
 
     /**
      * Selects an existing annotation definition for a column.
      * 
      * @param study is the study that the definition is getting set for.
      * @param fieldDescriptor fieldDescriptor receiving definition.
      * @param annotationDefinition the selected definition.
      * @param entityType entity type for the annotation definition.
      * @throws ValidationException if invalid definition for the given values.
      */
     void setDefinition(Study study, AnnotationFieldDescriptor fieldDescriptor, 
             AnnotationDefinition annotationDefinition, EntityTypeEnum entityType) throws ValidationException;
 
     /**
      * Create the associations between subjects in the study and samples.
      * 
      * @param studyConfiguration study containing the subjects and samples
      * @param mappingFile comma-separated value file that maps subject identifiers to sample names
      * @param genomicSource to map samples for.
      * @throws ValidationException if the file was not a valid mapping file.
      * @throws IOException unexpected IO exception
      * 
      */
     void mapSamples(StudyConfiguration studyConfiguration, File mappingFile, 
             GenomicDataSourceConfiguration genomicSource)
         throws ValidationException, IOException;
 
     /**
      * Create the associations between subjects in the study and image series.
      * 
      * @param studyConfiguration study containing the subjects and image series
      * @param mappingFile comma-separated value file that maps subject identifiers to image series identifiers
      * @param mappingType the type of mapping file it is.
      * @param imageSource the imageSource to map images.
      * @throws IOException unexpected IO exception
      * @throws ValidationException validation exception
      */
     void mapImageSeriesAcquisitions(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource,  
             File mappingFile, ImageDataSourceMappingTypeEnum mappingType)
         throws ValidationException, IOException;
     
     /**
      * Creates a new AnnotationDefinition based on an AnnotationFieldDescriptor.
      * @param descriptor annotation descriptor to use.
      * @param study object to correlate the newly created definition to.
      * @param entityType entity type for the annotation definition.
      * @param annotationType data type for the annotation definition.
      * @return The annotation definition that was created.
      * @throws ValidationException if there's a problem creating definition.
      */
     AnnotationDefinition createDefinition(AnnotationFieldDescriptor descriptor, Study study, EntityTypeEnum entityType,
             AnnotationTypeEnum annotationType) throws ValidationException;
 
     /**
      * Adds the samples specified by identifier in the file to a new set of control samples in the
      * study.
      * 
      * @param genomicSource add controls for this genomicSource
      * @param controlSampleSetName the controlSampleSet name
      * @param controlSampleFile file containing the sample identifiers, one per line
      * @param controlSampleFileName the controlSampleFile file name
      * @throws ValidationException if the file is invalid.
      * @throws IOException unexpected IO exception
      */
     void addControlSampleSet(GenomicDataSourceConfiguration genomicSource, String controlSampleSetName,
             File controlSampleFile, String controlSampleFileName)
         throws ValidationException, IOException;
 
     /**
      * Check for duplicate study name in the database.
      * 
      * @param study
      *            the study object
      * @param username the user checking to see if it's a duplicate, because it is based
      * on that users UserGroup privileges.
      * @return true or false
      */
     boolean isDuplicateStudyName(Study study, String username);
 
     /**
      * Removes a survivalValueDefinition from a study and deletes the object.
      * @param study - Study to remove from.
      * @param survivalValueDefinition - Object to remove from study.
      */
     void removeSurvivalValueDefinition(Study study, SurvivalValueDefinition survivalValueDefinition);
     
     /**
      * Retrieves the image data source for a given study configuration. (If there's more than one,
      * it takes the first one found).
      * @param study to use for data source.
      * @return image data source configuration for the study.
      */
     ImageDataSourceConfiguration retrieveImageDataSource(Study study);
 
     /**
      * Associates a copy number mapping file to the given genomic data source.
      * 
      * @param genomicDataSourceConfiguration  copy number data is associated to this source.
      * @param mappingFile the file containing the mapping of subjects to samples to copy number files.
      * @param filename the filename to save the file as. 
      * @throws IOException if the file couldn't be saved.
     * @throws ValidationException if unable to map the samples.
      */
     void saveDnaAnalysisMappingFile(GenomicDataSourceConfiguration genomicDataSourceConfiguration, 
            File mappingFile, String filename) throws IOException, ValidationException;
 
     /**
      * Associates a sample mapping file to the given genomic data source.
      * 
      * @param genomicDataSourceConfiguration  copy number data is associated to this source.
      * @param mappingFile the file containing the mapping of subjects to samples to level2 data files.
      * @param filename the filename to save the file as. 
      * @throws IOException if the file couldn't be saved.
      */
     void saveSampleMappingFile(GenomicDataSourceConfiguration genomicDataSourceConfiguration, 
             File mappingFile, String filename) throws IOException;
 
     /**
      * Sets the lastModifiedBy attribute of a StudyConfiguration to the current user's workspace.
      * @param studyConfiguration that is being modified by the user.
      * @param lastModifiedBy the user to last modify this study.
      * @param timeStampedStudyObject the specific study object that is to be timestamped, if left null it
      * @param systemLogMessage to add a log to the study configuration with the given message.
      * it will only timestamp the given studyConfiguration.
      */
     void setStudyLastModifiedByCurrentUser(StudyConfiguration studyConfiguration, UserWorkspace lastModifiedBy,
             TimeStampable timeStampedStudyObject, String systemLogMessage);
     
     /**
      * Adds the given ExternalLinkList to the StudyConfiguration.
      * @param studyConfiguration to add externalLinkList to.
      * @param externalLinkList to load and add to study.
      * @throws ValidationException if invalid file format.
      * @throws IOException if there's a problem with opening the file.
      */
     void addExternalLinksToStudy(StudyConfiguration studyConfiguration, ExternalLinkList externalLinkList) 
     throws ValidationException, IOException;
 
     /**
      * @param annotationGroup to create and add to study.
      * @param studyConfiguration that the group belongs to.
      * @param annotationGroupFile optional file to use to load into the group.
      * @throws ValidationException if invalid.
      * @throws ConnectionException caDSR connection problem
      * @throws IOException I/O exception
      */
     void saveAnnotationGroup(AnnotationGroup annotationGroup, StudyConfiguration studyConfiguration,
             File annotationGroupFile)
     throws ValidationException, ConnectionException, IOException;
     
     /**
      * Deletes AnnotationGroup from study.
      * @param studyConfiguration to delete annotationGroup from.
      * @param annotationGroup to delete from study.
      */
     void delete(StudyConfiguration studyConfiguration, AnnotationGroup annotationGroup);
     
     /**
      * Updates field descriptor type.
      * @param fieldDescriptor to update the type for.
      * @param type new type.
      * @return the newly saved AFD.
      * @throws ValidationException if invalid type for the existing data.
      */
     AnnotationFieldDescriptor updateFieldDescriptorType(AnnotationFieldDescriptor fieldDescriptor, 
             AnnotationFieldType type) 
     throws ValidationException;
     
     /**
      * Gets all values (from all files as well as currently loaded values) in string format for a fieldDescriptor.
      * @param fieldDescriptor to get string values for.
      * @return set of string values.
      * @throws ValidationException if validation problem.
      */
     Set<String> getAvailableValuesForFieldDescriptor(AnnotationFieldDescriptor fieldDescriptor) 
     throws ValidationException;
     
     /**
      * Makes a fieldDescriptor valid, and then saves it.
      * @param descriptor to save.
      */
     void makeFieldDescriptorValid(AnnotationFieldDescriptor descriptor);
 
     /**
      * @param persistentObject to save
      */
     void daoSave(Object persistentObject);
 }

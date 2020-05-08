 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.application.study.deployment;
 
 import gov.nih.nci.caintegrator.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.ValidationException;
 import gov.nih.nci.caintegrator.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator.domain.genomic.Array;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayDataType;
 import gov.nih.nci.caintegrator.domain.genomic.Platform;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator.domain.genomic.Sample;
 import gov.nih.nci.caintegrator.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator.external.ConnectionException;
 import gov.nih.nci.caintegrator.external.DataRetrievalException;
 import gov.nih.nci.caintegrator.external.caarray.CaArrayFacade;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Provides base handling to retrieve copy number data based on a copy number mapping file.
  */
 public abstract class AbstractUnparsedSupplementalMappingFileHandler extends AbstractSupplementalMappingFileHandler {
 
     AbstractUnparsedSupplementalMappingFileHandler(GenomicDataSourceConfiguration genomicSource,
             CaArrayFacade caArrayFacade, ArrayDataService arrayDataService, CaIntegrator2Dao dao) {
         super(genomicSource, caArrayFacade, arrayDataService, dao);
     }
 
     /**
      *
      * @param sampleName the sample name to retrieve
      * @param subjectIdentifier the study assignment id
      * @return the sample object
      * @throws ValidationException Validation exception
      * @throws FileNotFoundException IO exception
      */
     protected Sample getSample(String sampleName, String subjectIdentifier)
             throws FileNotFoundException, ValidationException {
         Sample sample = getSampleNameToSampleMap().get(sampleName);
         if (sample == null) {
             sample = new Sample();
             sample.setName(sampleName);
             getGenomicSource().getSamples().add(sample);
         }
         addSampleAcquisition(subjectIdentifier, sample);
         return sample;
     }
 
     private void addSampleAcquisition(String subjectIdentifier, Sample sample) throws ValidationException,
             FileNotFoundException {
         StudySubjectAssignment assignment = getSubjectAssignment(subjectIdentifier);
         SampleAcquisition acquisition = new SampleAcquisition();
         acquisition.setAssignment(assignment);
         acquisition.setSample(sample);
         sample.getSampleAcquisitions().add(acquisition);
         assignment.getSampleAcquisitionCollection().add(acquisition);
         getDao().save(sample);
     }
 
     /**
      *
      * @return the mapping file.
      * @throws FileNotFoundException IO exception.
      */
     @Override
     protected File getMappingFile() throws FileNotFoundException {
         return getGenomicSource().getDnaAnalysisDataConfiguration().getMappingFile();
     }
 
     abstract List<ArrayDataValues> loadArrayData()
     throws ConnectionException, DataRetrievalException, ValidationException, IOException;
 
     /**
      * Get the platform.
      * @param reporterListNames the set of report lists.
      * @return Platform
      * @throws ValidationException when platform not found or not unique.
      */
     protected Platform getPlatform(Set<String> reporterListNames) throws ValidationException {
         Set<Platform> platforms = new HashSet<Platform>();
         for (String reporterListName : reporterListNames) {
             ReporterList reporterList = getDao().getReporterList(reporterListName);
             if (reporterList == null) {
                 throw new ValidationException("There is no platform that supports chip type " + reporterListName);
             }
             platforms.add(reporterList.getPlatform());
         }
         if (platforms.size() > 1) {
             String platformNamesInErrorMsg = "";
             for (Platform platform : platforms) {
                 platformNamesInErrorMsg = platformNamesInErrorMsg.concat(platform.getName());
                 platformNamesInErrorMsg = platformNamesInErrorMsg.concat(" , ");
             }
 
             throw new ValidationException(
                     "DNA analysis data files for a single sample are mapped to the following platforms:"
                     + platformNamesInErrorMsg + " instead of one platform.  Confirm that "
                     + "reporter names are used in only one platform.");
         }
         return platforms.iterator().next();
     }
 
     /**
      * Clean up.
      * @param dataFile the data file to delete.
      */
     @Override
     protected void doneWithFile(File dataFile) {
         dataFile.delete();
     }
 
     @Override
     abstract String getFileType();
 
     /**
      * Create the ArrayData for the sample.
      * @param sample the sample to get arrayData for.
      * @param reporterLists the set of report lists.
      * @param dataType the array data type.
      * @return ArrayData
      */
     protected ArrayData createArrayData(Sample sample, Set<ReporterList> reporterLists, ArrayDataType dataType) {
         ArrayData arrayData = new ArrayData();
         arrayData.setType(dataType);
         arrayData.setSample(sample);
         sample.getArrayDataCollection().add(arrayData);
         arrayData.setStudy(getGenomicSource().getStudyConfiguration().getStudy());
         Array array = new Array();
         array.getArrayDataCollection().add(arrayData);
         arrayData.setArray(array);
         array.getSampleCollection().add(sample);
         sample.getArrayCollection().add(array);
         if (!reporterLists.isEmpty()) {
             arrayData.getReporterLists().addAll(reporterLists);
             for (ReporterList reporterList : reporterLists) {
                 reporterList.getArrayDatas().add(arrayData);
             }
             array.setPlatform(reporterLists.iterator().next().getPlatform());
         }
         return arrayData;
     }
 
     private StudySubjectAssignment getSubjectAssignment(String subjectIdentifier)
     throws ValidationException, FileNotFoundException {
         StudySubjectAssignment assignment =
             getGenomicSource().getStudyConfiguration().getSubjectAssignment(subjectIdentifier);
         if (assignment == null) {
             throw new ValidationException("Subject identifier " + subjectIdentifier + " in DNA analysis mapping file "
                     + getMappingFile().getAbsolutePath() + " doesn't map to a known subject in the study.");
         }
         return assignment;
     }
 
 }

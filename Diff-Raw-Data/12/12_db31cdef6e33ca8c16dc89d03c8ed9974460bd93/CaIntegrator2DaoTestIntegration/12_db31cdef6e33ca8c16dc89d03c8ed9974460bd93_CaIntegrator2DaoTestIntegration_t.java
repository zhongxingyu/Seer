 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.data;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import gov.nih.nci.caintegrator.application.query.InvalidCriterionException;
 import gov.nih.nci.caintegrator.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator.domain.annotation.NumericAnnotationValue;
 import gov.nih.nci.caintegrator.domain.annotation.PermissibleValue;
 import gov.nih.nci.caintegrator.domain.annotation.StringAnnotationValue;
 import gov.nih.nci.caintegrator.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator.domain.application.CopyNumberAlterationCriterion;
 import gov.nih.nci.caintegrator.domain.application.CopyNumberCriterionTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.GenomicIntervalTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.NumericComparisonCriterion;
 import gov.nih.nci.caintegrator.domain.application.NumericComparisonOperatorEnum;
 import gov.nih.nci.caintegrator.domain.application.SegmentBoundaryTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator.domain.application.WildCardTypeEnum;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayDataType;
 import gov.nih.nci.caintegrator.domain.genomic.ChromosomalLocation;
 import gov.nih.nci.caintegrator.domain.genomic.Gene;
 import gov.nih.nci.caintegrator.domain.genomic.GeneChromosomalLocation;
 import gov.nih.nci.caintegrator.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator.domain.genomic.GeneLocationConfiguration;
 import gov.nih.nci.caintegrator.domain.genomic.GenomeBuildVersionEnum;
 import gov.nih.nci.caintegrator.domain.genomic.Platform;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator.domain.genomic.Sample;
 import gov.nih.nci.caintegrator.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator.domain.imaging.ImageSeries;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 import gov.nih.nci.caintegrator.domain.translational.StudySubjectAssignment;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
 
 /**
  * caIntegrator dao integration tests.
  *
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:integration-test-config.xml")
@Transactional
public class CaIntegrator2DaoTestIntegration {
     @Autowired
     private CaIntegrator2Dao dao;
 
    @Test
     public void testGetWorkspace() {
         UserWorkspace workspace = new UserWorkspace();
         workspace.setUsername("username");
         dao.save(workspace);
 
         UserWorkspace workspace2 = this.dao.getWorkspace("username");
         assertEquals(workspace.getId(), workspace2.getId());
     }
 
     @Test
     public void testRetrieveAllSubscribedWorkspaces() {
         Study study = new Study();
         Study study2 = new Study();
 
         UserWorkspace workspace1 = new UserWorkspace();
         StudySubscription subscription1 = new StudySubscription();
         subscription1.setStudy(study);
         workspace1.getSubscriptionCollection().add(subscription1);
 
         UserWorkspace workspace2 = new UserWorkspace();
         StudySubscription subscription2 = new StudySubscription();
         subscription2.setStudy(study);
         workspace2.getSubscriptionCollection().add(subscription2);
 
         UserWorkspace workspace3 = new UserWorkspace();
         StudySubscription subscription3 = new StudySubscription();
         subscription3.setStudy(study2);
         workspace3.getSubscriptionCollection().add(subscription3);
         dao.save(study);
         dao.save(study2);
         dao.save(workspace1);
         dao.save(workspace2);
         dao.save(workspace3);
 
         assertEquals(2, dao.retrieveAllSubscribedWorkspaces(study).size());
         assertEquals(1, dao.retrieveAllSubscribedWorkspaces(study2).size());
     }
 
     @Test
     public void testSave() {
         StudyConfiguration studyConfiguration1 = new StudyConfiguration();
         Study study1 = studyConfiguration1.getStudy();
         study1.setLongTitleText("longTitleText");
         study1.setShortTitleText("shortTitleText");
         assertNull(studyConfiguration1.getId());
         assertNull(study1.getId());
         dao.save(studyConfiguration1);
         assertNotNull(studyConfiguration1.getId());
         assertNotNull(study1.getId());
         dao.refresh(studyConfiguration1);
 
         StudyConfiguration studyConfiguration2 = dao.get(studyConfiguration1.getId(), StudyConfiguration.class);
         Study study2 = studyConfiguration2.getStudy();
 
         assertEquals(studyConfiguration2, study2.getStudyConfiguration());
         assertEquals(study1.getShortTitleText(), study2.getShortTitleText());
         assertEquals(study1.getLongTitleText(), study2.getLongTitleText());
         assertEquals(study1, study2);
         assertEquals(studyConfiguration1, studyConfiguration2);
     }
 
     @Test
     public void testFindMatches() {
         // First load 2 AnnotationFieldDescriptors.
         AnnotationDefinition afd = new AnnotationDefinition();
         afd.setKeywords("congestive heart failure");
         afd.setDisplayName("Congestive Heart Failure");
         dao.save(afd);
 
         AnnotationDefinition afd2 = new AnnotationDefinition();
         afd2.setKeywords("congestive");
         afd2.setDisplayName("Congestive");
         dao.save(afd2);
 
         AnnotationDefinition afd3 = new AnnotationDefinition();
         afd3.setKeywords("congestive failure");
         afd3.setDisplayName("Congestive Failure");
         dao.save(afd3);
 
         // Now search for our item on the string "congestive"
         List<String> searchWords = new ArrayList<String>();
         searchWords.add("CoNgeStiVe");
         searchWords.add("HearT");
         searchWords.add("failure");
         List<AnnotationDefinition> afds1 = dao.findMatches(searchWords);
 
         assertNotNull(afds1);
         // Make sure it sorted them properly.
         assertEquals(afds1.get(0).getDisplayName(), "Congestive Heart Failure");
         assertEquals(afds1.get(1).getDisplayName(), "Congestive Failure");
         assertEquals(afds1.get(2).getDisplayName(), "Congestive");
 
         List<String> searchWords2 = new ArrayList<String>();
         searchWords2.add("afdsefda");
         List<AnnotationDefinition> afds2 = dao.findMatches(searchWords2);
         assertEquals(0, afds2.size());
     }
 
     @Test
     public void testFindMatchingSamples() {
         StudyHelper studyHelper = new StudyHelper();
         dao.save(studyHelper.getPlatform());
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
 
         // Now need to create the criterion items and see if we can retrieve back the proper values.
         NumericComparisonCriterion criterion = new NumericComparisonCriterion();
         criterion.setNumericValue(12.0);
         criterion.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL);
         criterion.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         criterion.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples = dao.findMatchingSamples(criterion, study);
 
         assertEquals(4, matchingSamples.size());
 
         // Try a different number combination to test a different operator
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(11.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.LESSOREQUAL);
         criterion2.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         criterion2.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples2 = dao.findMatchingSamples(criterion2, study);
 
         assertEquals(3, matchingSamples2.size());
 
         // Try a selectedValueCriterion now (should be size 3)
         SelectedValueCriterion criterion3 = new SelectedValueCriterion();
         Collection<PermissibleValue> permissibleValues1 = new HashSet<PermissibleValue>();
         permissibleValues1.add(studyHelper.getPermval1());
         criterion3.setValueCollection(permissibleValues1);
         criterion3.setEntityType(EntityTypeEnum.SAMPLE);
         criterion3.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         List<SampleAcquisition> matchingSamples3 = dao.findMatchingSamples(criterion3, study);
 
         assertEquals(1, matchingSamples3.size());
 
         // Try the other permissible values (should be size 2)
         SelectedValueCriterion criterion4 = new SelectedValueCriterion();
         Collection<PermissibleValue> permissibleValues2 = new HashSet<PermissibleValue>();
         permissibleValues2.add(studyHelper.getPermval2());
         criterion4.setValueCollection(permissibleValues2);
         criterion4.setEntityType(EntityTypeEnum.SAMPLE);
         criterion4.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         List<SampleAcquisition> matchingSamples4 = dao.findMatchingSamples(criterion4, study);
 
         assertEquals(0, matchingSamples4.size());
 
 
         // Try using a different Annotation Definition and verify that it returns 0 from that.
         NumericComparisonCriterion criterion5 = new NumericComparisonCriterion();
         criterion5.setNumericValue(13.0);
         criterion5.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL);
         criterion5.setAnnotationFieldDescriptor(studyHelper.getImageSeriesAnnotationFieldDescriptor());
         criterion5.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples5 = dao.findMatchingSamples(criterion5, study);
 
         assertEquals(0, matchingSamples5.size());
     }
 
     @Test
     public void testFindMatchingImageSeries() {
         StudyHelper studyHelper = new StudyHelper();
         dao.save(studyHelper.getPlatform());
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
 
         StringComparisonCriterion criterion1 = new StringComparisonCriterion();
         criterion1.setStringValue("string1");
         criterion1.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion1.setAnnotationFieldDescriptor(studyHelper.getImageSeriesAnnotationFieldDescriptor());
         List<ImageSeries> matchingImageSeries = dao.findMatchingImageSeries(criterion1, study);
 
         assertEquals(1, matchingImageSeries.size());
 
         // Try a wildcard search now.
         StringComparisonCriterion criterion2 = new StringComparisonCriterion();
         criterion2.setStringValue("string");
         criterion2.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion2.setWildCardType(WildCardTypeEnum.WILDCARD_AFTER_STRING);
         criterion2.setAnnotationFieldDescriptor(studyHelper.getImageSeriesAnnotationFieldDescriptor());
         List<ImageSeries> matchingImageSeries2 = dao.findMatchingImageSeries(criterion2, study);
 
         assertEquals(5, matchingImageSeries2.size());
 
         // Change only the annotation definition and see if it returns 0.
         StringComparisonCriterion criterion3 = new StringComparisonCriterion();
         criterion3.setStringValue("string1");
         criterion3.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion3.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         List<ImageSeries> matchingImageSeries3 = dao.findMatchingImageSeries(criterion3, study);
         assertEquals(0, matchingImageSeries3.size());
     }
 
     @Test
     public void testFindMatchingSubjects() {
         StudyHelper studyHelper = new StudyHelper();
         dao.save(studyHelper.getPlatform());
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
 
         NumericComparisonCriterion criterion1 = new NumericComparisonCriterion();
         criterion1.setNumericValue(2.0);
         criterion1.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER);
         criterion1.setEntityType(EntityTypeEnum.SUBJECT);
         criterion1.setAnnotationFieldDescriptor(studyHelper.getSubjectAnnotationFieldDescriptor());
         List<StudySubjectAssignment> matchingStudySubjectAssignments = dao.findMatchingSubjects(criterion1, study);
 
         assertEquals(3, matchingStudySubjectAssignments.size());
 
         // Change only the annotation definition and see if it returns 0.
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(2.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER);
         criterion2.setEntityType(EntityTypeEnum.SUBJECT);
         criterion2.setAnnotationFieldDescriptor(studyHelper.getSampleAnnotationFieldDescriptor());
         List<StudySubjectAssignment> matchingStudySubjectAssignments2 = dao.findMatchingSubjects(criterion2, study);
 
         assertEquals(0, matchingStudySubjectAssignments2.size());
     }
 
     @Test
     public void testFindGeneExpressionReporters() {
         Study study = new Study();
         Gene gene = new Gene();
         gene.setSymbol("TEST");
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         Platform platform = new Platform();
         ReporterList reporterList = platform.addReporterList("reporterList", ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
         reporter.setReporterList(reporterList);
         reporterList.getReporters().add(reporter);
         reporter.setIndex(0);
         reporter.getGenes().add(gene);
         StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
         study.getAssignmentCollection().add(studySubjectAssignment);
         SampleAcquisition sampleAcquisition = new SampleAcquisition();
         studySubjectAssignment.getSampleAcquisitionCollection().add(sampleAcquisition);
         Sample sample = new Sample();
         sampleAcquisition.setSample(sample);
         ArrayData arrayData = new ArrayData();
         arrayData.setStudy(study);
         sample.getArrayDataCollection().add(arrayData);
         arrayData.getReporterLists().add(reporterList);
         reporterList.getArrayDatas().add(arrayData);
         dao.save(sample);
         dao.save(study);
         dao.save(gene);
         Set<String> geneSymbols = new HashSet<String>();
         geneSymbols.add("TEST");
         assertEquals(1, dao.findReportersForGenes(geneSymbols, ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET, study, null).size());
     }
 
     @Test
     public void testRetrieveUniqueValuesForStudyAnnotation() {
         Study study = new Study();
 
         StudySubjectAssignment studySubjectAssignment1 = new StudySubjectAssignment();
         studySubjectAssignment1.setStudy(study);
         StudySubjectAssignment studySubjectAssignment2 = new StudySubjectAssignment();
         studySubjectAssignment2.setStudy(study);
         StudySubjectAssignment studySubjectAssignment3 = new StudySubjectAssignment();
         studySubjectAssignment3.setStudy(study);
 
         study.getAssignmentCollection().add(studySubjectAssignment1);
         study.getAssignmentCollection().add(studySubjectAssignment3);
         study.getAssignmentCollection().add(studySubjectAssignment3);
 
         SubjectAnnotation subjectAnnotation1 = new SubjectAnnotation();
         SubjectAnnotation subjectAnnotation2 = new SubjectAnnotation();
         SubjectAnnotation subjectAnnotation3 = new SubjectAnnotation();
         SubjectAnnotation subjectAnnotation4 = new SubjectAnnotation();
         SubjectAnnotation subjectAnnotation5 = new SubjectAnnotation();
         SubjectAnnotation subjectAnnotation6 = new SubjectAnnotation();
 
 
         studySubjectAssignment1.getSubjectAnnotationCollection().add(subjectAnnotation1);
         studySubjectAssignment2.getSubjectAnnotationCollection().add(subjectAnnotation2);
         studySubjectAssignment3.getSubjectAnnotationCollection().add(subjectAnnotation3);
         studySubjectAssignment1.getSubjectAnnotationCollection().add(subjectAnnotation4);
         studySubjectAssignment2.getSubjectAnnotationCollection().add(subjectAnnotation5);
         studySubjectAssignment3.getSubjectAnnotationCollection().add(subjectAnnotation6);
 
         // First test is for Strings
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.setDataType(AnnotationTypeEnum.STRING);
 
         StringAnnotationValue genderStringValue1 = new StringAnnotationValue();
         genderStringValue1.setStringValue("M");
         genderStringValue1.setSubjectAnnotation(subjectAnnotation1);
         subjectAnnotation1.setAnnotationValue(genderStringValue1);
         genderStringValue1.setAnnotationDefinition(annotationDefinition);
 
         StringAnnotationValue genderStringValue2 = new StringAnnotationValue();
         genderStringValue2.setStringValue("M");
         genderStringValue2.setSubjectAnnotation(subjectAnnotation2);
         subjectAnnotation2.setAnnotationValue(genderStringValue2);
         genderStringValue2.setAnnotationDefinition(annotationDefinition);
 
         StringAnnotationValue genderStringValue3 = new StringAnnotationValue();
         genderStringValue3.setStringValue("F");
         genderStringValue3.setSubjectAnnotation(subjectAnnotation3);
         subjectAnnotation3.setAnnotationValue(genderStringValue3);
         genderStringValue3.setAnnotationDefinition(annotationDefinition);
 
         annotationDefinition.getAnnotationValueCollection().add(genderStringValue1);
         annotationDefinition.getAnnotationValueCollection().add(genderStringValue2);
         annotationDefinition.getAnnotationValueCollection().add(genderStringValue3);
 
         // Next test is for numerics.
         AnnotationDefinition annotationDefinition2 = new AnnotationDefinition();
         annotationDefinition2.setDataType(AnnotationTypeEnum.NUMERIC);
 
         NumericAnnotationValue numericValue1 = new NumericAnnotationValue();
         numericValue1.setNumericValue(1.0);
         numericValue1.setSubjectAnnotation(subjectAnnotation4);
         subjectAnnotation4.setAnnotationValue(numericValue1);
         numericValue1.setAnnotationDefinition(annotationDefinition2);
 
         NumericAnnotationValue numericValue2 = new NumericAnnotationValue();
         numericValue2.setNumericValue(1.0);
         numericValue2.setSubjectAnnotation(subjectAnnotation5);
         subjectAnnotation5.setAnnotationValue(numericValue2);
         numericValue2.setAnnotationDefinition(annotationDefinition2);
 
         NumericAnnotationValue numericValue3 = new NumericAnnotationValue();
         numericValue3.setNumericValue(2.0);
         numericValue3.setSubjectAnnotation(subjectAnnotation6);
         subjectAnnotation6.setAnnotationValue(numericValue3);
         numericValue3.setAnnotationDefinition(annotationDefinition2);
 
         annotationDefinition2.getAnnotationValueCollection().add(numericValue1);
         annotationDefinition2.getAnnotationValueCollection().add(numericValue2);
         annotationDefinition2.getAnnotationValueCollection().add(numericValue3);
         dao.save(annotationDefinition);
         dao.save(annotationDefinition2);
         dao.save(study);
         // First test is 3 strings, M, M, and F, and we want just M / F to come out of it.
         List<String> values = dao.retrieveUniqueValuesForStudyAnnotation(study, annotationDefinition, EntityTypeEnum.SUBJECT, String.class);
         int numberM = 0;
         int numberF = 0;
 
         for(String value : values) {
 
             if (value.equals("M")) {
                 numberM++;
             }
             if (value.equals("F")) {
                 numberF++;
             }
         }
         assertEquals(1, numberM);
         assertEquals(1, numberF);
         assertEquals(2, values.size());
         assertEquals(3, annotationDefinition.getAnnotationValueCollection().size());
 
         // Next test is 3 numbers, 1.0, 1.0, and 2.0, and we want just 1.0 / 2.0 to come out of it.
         List<Double> numericValues = dao.retrieveUniqueValuesForStudyAnnotation(study, annotationDefinition2, EntityTypeEnum.SUBJECT, Double.class);
         int number1 = 0;
         int number2 = 0;
 
         for(Double value : numericValues) {
 
             if (value.equals(1.0)) {
                 number1++;
             }
             if (value.equals(2.0)) {
                 number2++;
             }
         }
         assertEquals(1, number1);
         assertEquals(1, number2);
         assertEquals(2, numericValues.size());
         assertEquals(3, annotationDefinition2.getAnnotationValueCollection().size());
     }
 
     @Test
     public void testRetrieveImagingDataSourceForStudy() {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         Study study = studyConfiguration.getStudy();
         ImageDataSourceConfiguration imageDataSource = new ImageDataSourceConfiguration();
         studyConfiguration.getImageDataSources().add(imageDataSource);
         dao.save(studyConfiguration);
         assertEquals(imageDataSource, dao.retrieveImagingDataSourceForStudy(study));
     }
 
     @Test
     public void testRetrieveNumberImage() {
         StudyHelper studyHelper = new StudyHelper();
         dao.save(studyHelper.getPlatform());
         Study study = studyHelper.populateAndRetrieveStudyWithSourceConfigurations();
         dao.save(study.getStudyConfiguration());
         dao.save(study);
         int numImageSeries = dao.retrieveNumberImages(
                 study.getStudyConfiguration().getImageDataSources().get(0)
                     .getImageSeriesAcquisitions().get(0).getSeriesCollection());
         assertEquals(2, numImageSeries);
     }
 
     @Test
     public void testRetrievePlatformsForGenomicSource() {
         StudyHelper studyHelper = new StudyHelper();
         dao.save(studyHelper.getPlatform());
         Study study = studyHelper.populateAndRetrieveStudyWithSourceConfigurations();
         dao.save(study.getStudyConfiguration());
         dao.save(study);
         List<Platform> platforms = dao.retrievePlatformsForGenomicSource(
                     study.getStudyConfiguration().getGenomicDataSources().get(0));
         assertEquals(2, platforms.size());
     }
 
     @Test
     public void testFindMatchingSegmentDatas() throws InvalidCriterionException {
         Platform platform = new Platform();
         platform.setName("platform");
         ReporterList reporterList = platform.addReporterList("name", ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
         reporterList.setGenomeVersion("hg18");
         dao.save(platform);
         StudyHelper studyHelper = new StudyHelper();
         studyHelper.setArrayDataType(ArrayDataType.COPY_NUMBER);
         studyHelper.setPlatform(platform);
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
 
         // Segment Data type
         CopyNumberAlterationCriterion copyNumberCriterion = new CopyNumberAlterationCriterion();
         copyNumberCriterion.setCopyNumberCriterionType(CopyNumberCriterionTypeEnum.SEGMENT_VALUE);
         copyNumberCriterion.setLowerLimit(.02f);
         copyNumberCriterion.setUpperLimit(50f);
         copyNumberCriterion.setGenomicIntervalType(GenomicIntervalTypeEnum.CHROMOSOME_COORDINATES);
         copyNumberCriterion.setChromosomeCoordinateHigh(1800000);
         copyNumberCriterion.setChromosomeCoordinateLow(20000);
         copyNumberCriterion.setChromosomeNumber("3");
         List<SegmentData> segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(1, segmentDatas.size());
         copyNumberCriterion.setUpperLimit(.08f);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(1, segmentDatas.size());
 
         copyNumberCriterion.setChromosomeCoordinateLow(0);
         copyNumberCriterion.setChromosomeCoordinateHigh(1000000);
         copyNumberCriterion.setUpperLimit(.12f);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(1, segmentDatas.size());
 
         copyNumberCriterion.setGenomicIntervalType(GenomicIntervalTypeEnum.GENE_NAME);
         copyNumberCriterion.setGeneSymbol("GENE_3, GENE_4");
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(4, segmentDatas.size());
 
         // Try matching the segment datas based on the location of the two previous ones.
         segmentDatas = dao.findMatchingSegmentDatasByLocation(segmentDatas, study, platform);
         assertEquals(4, segmentDatas.size());
 
         copyNumberCriterion.setUpperLimit(.03f);
         copyNumberCriterion.setLowerLimit(null);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(3, segmentDatas.size());
 
         copyNumberCriterion.setGeneSymbol("");
         copyNumberCriterion.setLowerLimit(.04f);
         copyNumberCriterion.setUpperLimit(.02f);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(5, segmentDatas.size());
 
         copyNumberCriterion.setUpperLimit(.15f);
         copyNumberCriterion.setLowerLimit(.01f);
         copyNumberCriterion.setChromosomeCoordinateLow(20000);
         copyNumberCriterion.setChromosomeCoordinateHigh(40000);
         copyNumberCriterion.setSegmentBoundaryType(SegmentBoundaryTypeEnum.ONE_OR_MORE);
         copyNumberCriterion.setGenomicIntervalType(GenomicIntervalTypeEnum.CHROMOSOME_COORDINATES);
         copyNumberCriterion.setChromosomeNumber("2");
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(1, segmentDatas.size());
 
         // Calls type
         copyNumberCriterion = new CopyNumberAlterationCriterion();
         copyNumberCriterion.setCopyNumberCriterionType(CopyNumberCriterionTypeEnum.CALLS_VALUE);
         copyNumberCriterion.setGenomicIntervalType(GenomicIntervalTypeEnum.CHROMOSOME_COORDINATES);
         copyNumberCriterion.setChromosomeNumber("4");
         copyNumberCriterion.getCallsValues().add(0);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(0, segmentDatas.size());
 
         copyNumberCriterion.getCallsValues().add(-1);
         segmentDatas = dao.findMatchingSegmentDatas(copyNumberCriterion, study, platform);
         assertEquals(1, segmentDatas.size());
     }
 
     @Test
     public void testFindGenesByLocation() {
         Gene gene1 = createGene("gene1");
         Gene gene2 = createGene("gene2");
         Gene gene3 = createGene("gene3");
         Gene gene4 = createGene("gene4");
         Gene gene5 = createGene("gene5");
         Gene gene6 = createGene("gene6");
         Gene gene7 = createGene("gene7");
         dao.save(gene1);
         dao.save(gene2);
         dao.save(gene3);
         dao.save(gene4);
         dao.save(gene5);
         dao.save(gene6);
         dao.save(gene7);
 
 
         GeneLocationConfiguration geneLocationConfiguration = new GeneLocationConfiguration();
         geneLocationConfiguration.setGenomeBuildVersion(GenomeBuildVersionEnum.HG18);
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene1", "1", 1, 3));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene2", "1", 2, 6));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene3", "1", 2, 11));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene4", "1", 8, 9));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene5", "1", 10, 12));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene6", "1", 11, 15));
         geneLocationConfiguration.getGeneLocations().add(createGeneChromosomalLocation("gene7", "2", 1, 15));
         dao.save(geneLocationConfiguration);
 
         List<Gene> genes = dao.findGenesByLocation("1", 2, 10, GenomeBuildVersionEnum.HG18);
         assertEquals(5, genes.size());
 
         genes = dao.findGenesByLocation("1", 6, 8, GenomeBuildVersionEnum.HG18);
         assertEquals(3, genes.size());
         genes = dao.findGenesByLocation("2", 3, 4, GenomeBuildVersionEnum.HG18);
         assertEquals(1, genes.size());
         genes = dao.findGenesByLocation("2", 3, 4, GenomeBuildVersionEnum.HG19);
         assertEquals(0, genes.size());
         genes = dao.findGenesByLocation("3", 3, 4, GenomeBuildVersionEnum.HG18);
         assertEquals(0, genes.size());
     }
 
     /**
      *
      */
     private Gene createGene(String symbol) {
         Gene gene = new Gene();
         gene.setSymbol(symbol);
         return gene;
     }
 
     /**
      * @return
      */
     private GeneChromosomalLocation createGeneChromosomalLocation(String symbol,
             String chromosome, Integer start, Integer end) {
         GeneChromosomalLocation gcl = new GeneChromosomalLocation();
         gcl.setGeneSymbol(symbol);
         ChromosomalLocation location = new ChromosomalLocation();
         location.setChromosome(chromosome);
         location.setStartPosition(start);
         location.setEndPosition(end);
         gcl.setLocation(location);
         return gcl;
     }
 }

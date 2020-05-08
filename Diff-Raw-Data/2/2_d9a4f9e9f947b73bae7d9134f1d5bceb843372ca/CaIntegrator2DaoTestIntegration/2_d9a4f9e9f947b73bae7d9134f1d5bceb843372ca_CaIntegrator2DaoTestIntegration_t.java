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
 package gov.nih.nci.caintegrator2.data;
 
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.NumericComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.NumericComparisonOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.application.WildCardTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeries;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 public final class CaIntegrator2DaoTestIntegration extends AbstractTransactionalSpringContextTests {
     
     private CaIntegrator2Dao dao;
     private SessionFactory sessionFactory;
 
    
     protected String[] getConfigLocations() {
         return new String[] {"classpath*:/**/dao-test-config.xml"};
     }
 
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
         workspace1.setSubscriptionCollection(new HashSet<StudySubscription>());
         workspace1.getSubscriptionCollection().add(subscription1);
         
         UserWorkspace workspace2 = new UserWorkspace();
         StudySubscription subscription2 = new StudySubscription();
         subscription2.setStudy(study);
         workspace2.setSubscriptionCollection(new HashSet<StudySubscription>());
         workspace2.getSubscriptionCollection().add(subscription2);
         
         UserWorkspace workspace3 = new UserWorkspace();
         StudySubscription subscription3 = new StudySubscription();
         subscription3.setStudy(study2);
         workspace3.setSubscriptionCollection(new HashSet<StudySubscription>());
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
     @SuppressWarnings({"PMD.ExcessiveMethodLength"})
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
     @SuppressWarnings({"PMD.ExcessiveMethodLength"})
     public void testFindMatchingSamples() {
         StudyHelper studyHelper = new StudyHelper();
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
         
         // Now need to create the criterion items and see if we can retrieve back the proper values.
         NumericComparisonCriterion criterion = new NumericComparisonCriterion();
         criterion.setNumericValue(12.0);
         criterion.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL);
         criterion.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         criterion.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples = dao.findMatchingSamples(criterion, study);
         
         assertEquals(4, matchingSamples.size());
         
         // Try a different number combination to test a different operator
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(11.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.LESSOREQUAL);
         criterion2.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         criterion2.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples2 = dao.findMatchingSamples(criterion2, study);
         
         assertEquals(3, matchingSamples2.size());
         
         // Try a selectedValueCriterion now (should be size 3)
         SelectedValueCriterion criterion3 = new SelectedValueCriterion();
         Collection<AbstractPermissibleValue> permissibleValues1 = new HashSet<AbstractPermissibleValue>();
         permissibleValues1.add(studyHelper.getPermval1());
         criterion3.setValueCollection(permissibleValues1);
         criterion3.setEntityType(EntityTypeEnum.SAMPLE);
         criterion3.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<SampleAcquisition> matchingSamples3 = dao.findMatchingSamples(criterion3, study);
         
         assertEquals(1, matchingSamples3.size());
         
         // Try the other permissible values (should be size 2)
         SelectedValueCriterion criterion4 = new SelectedValueCriterion();
         Collection<AbstractPermissibleValue> permissibleValues2 = new HashSet<AbstractPermissibleValue>();
         permissibleValues2.add(studyHelper.getPermval2());
         criterion4.setValueCollection(permissibleValues2);
         criterion4.setEntityType(EntityTypeEnum.SAMPLE);
         criterion4.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<SampleAcquisition> matchingSamples4 = dao.findMatchingSamples(criterion4, study);
         
         assertEquals(0, matchingSamples4.size());
         
         
         // Try using a different Annotation Definition and verify that it returns 0 from that.
         NumericComparisonCriterion criterion5 = new NumericComparisonCriterion();
         criterion5.setNumericValue(13.0);
         criterion5.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL);
         criterion5.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         criterion5.setEntityType(EntityTypeEnum.SAMPLE);
         List<SampleAcquisition> matchingSamples5 = dao.findMatchingSamples(criterion5, study);
         
         assertEquals(0, matchingSamples5.size());
     }
     
     @Test
     @SuppressWarnings({"PMD.ExcessiveMethodLength"})
     public void testFindMatchingImageSeries() {
         StudyHelper studyHelper = new StudyHelper();
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
         
         StringComparisonCriterion criterion1 = new StringComparisonCriterion();
         criterion1.setStringValue("string1");
         criterion1.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion1.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         List<ImageSeries> matchingImageSeries = dao.findMatchingImageSeries(criterion1, study);
         
         assertEquals(1, matchingImageSeries.size());
         
         // Try a wildcard search now.
         StringComparisonCriterion criterion2 = new StringComparisonCriterion();
         criterion2.setStringValue("string");
         criterion2.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion2.setWildCardType(WildCardTypeEnum.WILDCARD_AFTER_STRING);
         criterion2.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         List<ImageSeries> matchingImageSeries2 = dao.findMatchingImageSeries(criterion2, study);
         
         assertEquals(5, matchingImageSeries2.size());
         
         // Change only the annotation definition and see if it returns 0.
         StringComparisonCriterion criterion3 = new StringComparisonCriterion();
         criterion3.setStringValue("string1");
         criterion3.setEntityType(EntityTypeEnum.IMAGESERIES);
         criterion3.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<ImageSeries> matchingImageSeries3 = dao.findMatchingImageSeries(criterion3, study);
         assertEquals(0, matchingImageSeries3.size());
     }
     
     @Test
     public void testFindMatchingSubjects() {
         StudyHelper studyHelper = new StudyHelper();
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         dao.save(study);
         
         NumericComparisonCriterion criterion1 = new NumericComparisonCriterion();
         criterion1.setNumericValue(2.0);
         criterion1.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER);
         criterion1.setEntityType(EntityTypeEnum.SUBJECT);
         criterion1.setAnnotationDefinition(studyHelper.getSubjectAnnotationDefinition());
         List<StudySubjectAssignment> matchingStudySubjectAssignments = dao.findMatchingSubjects(criterion1, study);
         
         assertEquals(3, matchingStudySubjectAssignments.size());
         
         // Change only the annotation definition and see if it returns 0.
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(2.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER);
         criterion2.setEntityType(EntityTypeEnum.SUBJECT);
         criterion2.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<StudySubjectAssignment> matchingStudySubjectAssignments2 = dao.findMatchingSubjects(criterion2, study);
         
         assertEquals(0, matchingStudySubjectAssignments2.size());
     }
     
     @Test
     public void testFindGeneExpressionReporters() {
         Study study = new Study();
         Gene gene = new Gene();
         gene.setSymbol("TEST");
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         ReporterList reporterList = new ReporterList();
         reporterList.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
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
         arrayData.setReporterList(reporterList);
         reporterList.getArrayDatas().add(arrayData);
         dao.save(study);
         dao.save(gene);
         Set<String> geneSymbols = new HashSet<String>();
         geneSymbols.add("TEST");
         assertEquals(1, dao.findReportersForGenes(geneSymbols, ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET, study).size());
     }
     
     @Test
     public void testRetrieveValueForAnnotationSubject() {
         StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
         
         SubjectAnnotation genderSubjectAnnotation = new SubjectAnnotation();
         SubjectAnnotation weightSubjectAnnotation = new SubjectAnnotation();
         studySubjectAssignment.getSubjectAnnotationCollection().add(genderSubjectAnnotation);
         studySubjectAssignment.getSubjectAnnotationCollection().add(weightSubjectAnnotation);
         
         AnnotationDefinition genderAnnotationDefinition = new AnnotationDefinition();
         AnnotationDefinition weightAnnotationDefinition = new AnnotationDefinition();
         
         StringAnnotationValue genderStringValue = new StringAnnotationValue();
         genderStringValue.setStringValue("M");
         genderStringValue.setSubjectAnnotation(genderSubjectAnnotation);
         genderSubjectAnnotation.setAnnotationValue(genderStringValue);
         genderStringValue.setAnnotationDefinition(genderAnnotationDefinition);
         
         NumericAnnotationValue weightAnnotationValue = new NumericAnnotationValue();
         weightAnnotationValue.setNumericValue(180.0);
         weightAnnotationValue.setSubjectAnnotation(weightSubjectAnnotation);
         weightSubjectAnnotation.setAnnotationValue(weightAnnotationValue);
         weightAnnotationValue.setAnnotationDefinition(weightAnnotationDefinition);
         
         dao.save(studySubjectAssignment);
         
         assertEquals(genderStringValue, 
                 dao.retrieveValueForAnnotationSubject(studySubjectAssignment, genderAnnotationDefinition));
         
         assertEquals(weightAnnotationValue, 
                 dao.retrieveValueForAnnotationSubject(studySubjectAssignment, weightAnnotationDefinition));
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
         annotationDefinition.setType(AnnotationTypeEnum.STRING.getValue());
         annotationDefinition.setAnnotationValueCollection(new HashSet<AbstractAnnotationValue>());
         
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
         annotationDefinition2.setType(AnnotationTypeEnum.NUMERIC.getValue());
         annotationDefinition2.setAnnotationValueCollection(new HashSet<AbstractAnnotationValue>());
         
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
         Study study = studyHelper.populateAndRetrieveStudyWithSourceConfigurations();
         dao.save(study.getStudyConfiguration());
         dao.save(study);
         List<Platform> platforms = dao.retrievePlatformsForGenomicSource(
                     study.getStudyConfiguration().getGenomicDataSources().get(0));
         assertEquals(2, platforms.size());
     }
     
     /**
      * @param caIntegrator2Dao the caIntegrator2Dao to set
      */
     public void setDao(CaIntegrator2Dao caIntegrator2Dao) {
         this.dao = caIntegrator2Dao;
     }
 
     /**
      * @return the sessionFactory
      */
     public SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     /**
      * @param sessionFactory the sessionFactory to set
      */
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
 }

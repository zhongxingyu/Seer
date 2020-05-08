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
 
 import gov.nih.nci.caintegrator2.application.arraydata.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.NumericComparisonOperatorEnum;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.WildCardTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissableValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator2.domain.application.NumericComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayDataMatrix;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterSet;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeries;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
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
         
         StudyConfiguration studyConfiguration2 = dao.get(studyConfiguration1.getId(), StudyConfiguration.class);
         Study study2 = studyConfiguration2.getStudy();
         
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
         criterion.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL.getValue());
         criterion.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         criterion.setEntityType(EntityTypeEnum.SAMPLE.getValue());
         List<SampleAcquisition> matchingSamples = dao.findMatchingSamples(criterion, study);
         
         assertEquals(4, matchingSamples.size());
         
         // Try a different number combination to test a different operator
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(11.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.LESSOREQUAL.getValue());
         criterion2.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         criterion2.setEntityType(EntityTypeEnum.SAMPLE.getValue());
         List<SampleAcquisition> matchingSamples2 = dao.findMatchingSamples(criterion2, study);
         
         assertEquals(3, matchingSamples2.size());
         
         // Try a selectedValueCriterion now (should be size 3)
         SelectedValueCriterion criterion3 = new SelectedValueCriterion();
         Collection<AbstractPermissableValue> permissableValues1 = new HashSet<AbstractPermissableValue>();
         permissableValues1.add(studyHelper.getPermval1());
         criterion3.setValueCollection(permissableValues1);
         criterion3.setEntityType(EntityTypeEnum.SAMPLE.getValue());
         criterion3.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<SampleAcquisition> matchingSamples3 = dao.findMatchingSamples(criterion3, study);
         
         assertEquals(3, matchingSamples3.size());
         
         // Try the other permissable values (should be size 2)
         SelectedValueCriterion criterion4 = new SelectedValueCriterion();
         Collection<AbstractPermissableValue> permissableValues2 = new HashSet<AbstractPermissableValue>();
         permissableValues2.add(studyHelper.getPermval2());
         criterion4.setValueCollection(permissableValues2);
         criterion4.setEntityType(EntityTypeEnum.SAMPLE.getValue());
         criterion4.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<SampleAcquisition> matchingSamples4 = dao.findMatchingSamples(criterion4, study);
         
         assertEquals(2, matchingSamples4.size());
         
         
         // Try using a different Annotation Definition and verify that it returns 0 from that.
         NumericComparisonCriterion criterion5 = new NumericComparisonCriterion();
         criterion5.setNumericValue(13.0);
         criterion5.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATEROREQUAL.getValue());
         criterion5.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         criterion5.setEntityType(EntityTypeEnum.SAMPLE.getValue());
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
         criterion1.setEntityType(EntityTypeEnum.IMAGESERIES.getValue());
         criterion1.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         List<ImageSeries> matchingImageSeries = dao.findMatchingImageSeries(criterion1, study);
         
         assertEquals(1, matchingImageSeries.size());
         
         // Try a wildcard search now.
         StringComparisonCriterion criterion2 = new StringComparisonCriterion();
         criterion2.setStringValue("string");
         criterion2.setEntityType(EntityTypeEnum.IMAGESERIES.getValue());
         criterion2.setWildCardType(WildCardTypeEnum.WILDCARD_AFTER_STRING.getValue());
         criterion2.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         List<ImageSeries> matchingImageSeries2 = dao.findMatchingImageSeries(criterion2, study);
         
         assertEquals(5, matchingImageSeries2.size());
         
         // Change only the annotation definition and see if it returns 0.
         StringComparisonCriterion criterion3 = new StringComparisonCriterion();
         criterion3.setStringValue("string1");
         criterion3.setEntityType(EntityTypeEnum.IMAGESERIES.getValue());
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
         criterion1.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER.getValue());
         criterion1.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         criterion1.setAnnotationDefinition(studyHelper.getSubjectAnnotationDefinition());
         List<StudySubjectAssignment> matchingStudySubjectAssignments = dao.findMatchingSubjects(criterion1, study);
         
         assertEquals(3, matchingStudySubjectAssignments.size());
         
         // Change only the annotation definition and see if it returns 0.
         NumericComparisonCriterion criterion2 = new NumericComparisonCriterion();
         criterion2.setNumericValue(2.0);
         criterion2.setNumericComparisonOperator(NumericComparisonOperatorEnum.GREATER.getValue());
         criterion2.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         criterion2.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         List<StudySubjectAssignment> matchingStudySubjectAssignments2 = dao.findMatchingSubjects(criterion2, study);
         
         assertEquals(0, matchingStudySubjectAssignments2.size());
     }
     
     @Test
     public void testFindMatchingGenes() {
         Study study = new Study();
         GeneNameCriterion criterion = new GeneNameCriterion();
         criterion.setGeneSymbol("TEST");
         Gene gene = new Gene();
         gene.setSymbol("TEST");
         Collection<GeneExpressionReporter> reporterCollection = new HashSet<GeneExpressionReporter>();
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         ReporterSet reporterSet = new ReporterSet();
         Collection<ArrayData> arrayDataCollection = new HashSet<ArrayData>();
         ArrayData arrayData = new ArrayData();
         arrayData.setStudy(study);
         arrayDataCollection.add(arrayData);
         reporterSet.setArrayDataCollection(arrayDataCollection);
         reporter.setReporterSet(reporterSet);
         reporterCollection.add(reporter);
         gene.setReporterCollection(reporterCollection);
         reporter.setGene(gene);
         dao.save(study);
         dao.save(gene);
         assertEquals(1, dao.findMatchingGenes(criterion, study).size());
     }
     
     @Test
     public void testGetArrayDataMatrixes() {
         ArrayDataMatrix matrix1 = new ArrayDataMatrix();
         ArrayDataMatrix matrix2 = new ArrayDataMatrix();
         ArrayDataMatrix matrix3 = new ArrayDataMatrix();
         Study study1 = new Study();
         Study study2 = new Study();
         ReporterSet reporterSet1 = new ReporterSet();
         ReporterSet reporterSet2 = new ReporterSet();
         reporterSet1.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_GENE.getValue());
         reporterSet2.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET.getValue());
         matrix1.setReporterSet(reporterSet1);
         matrix2.setReporterSet(reporterSet2);
         matrix3.setReporterSet(reporterSet1);
         matrix1.setStudy(study1);
         matrix2.setStudy(study1);
         matrix3.setStudy(study1);
         dao.save(matrix1);
         dao.save(matrix2);
         dao.save(matrix3);
         dao.save(study2);
         List<ArrayDataMatrix> retrieved = dao.getArrayDataMatrixes(study1, ReporterTypeEnum.GENE_EXPRESSION_GENE);
         assertEquals(2, retrieved.size());
         assertTrue(retrieved.contains(matrix1) && retrieved.contains(matrix3));
         retrieved = dao.getArrayDataMatrixes(study2, ReporterTypeEnum.GENE_EXPRESSION_GENE);
         assertEquals(0, retrieved.size());
         retrieved = dao.getArrayDataMatrixes(study1, ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
         assertEquals(1, retrieved.size());
         assertEquals(matrix2, retrieved.get(0));
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

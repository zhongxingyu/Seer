 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caArray
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caArray Software License (the License) is between NCI and You. You (or 
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
  * its rights in the caArray Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caArray Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator Software and any 
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
 package gov.nih.nci.caintegrator2.application.query;
 
 import gov.nih.nci.caintegrator2.application.study.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoImpl;
 import gov.nih.nci.caintegrator2.data.StudyHelper;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.NumericAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.StringAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.application.ResultValue;
 import gov.nih.nci.caintegrator2.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 /**
  * Tests that the CompoundCriterionHandler object can get the matches for various CompoundCriterion.
  */
 @SuppressWarnings("PMD")
 public class QueryTranslatorTestIntegration extends AbstractTransactionalSpringContextTests {
 
     private CaIntegrator2DaoImpl dao;
     private ResultHandlerImpl resultHandler;
     
     protected String[] getConfigLocations() {
         return new String[] {"classpath*:/**/query-test-integration.xml"};
     }
 
     @Test
     @SuppressWarnings({"PMD"})
     public void testExecute() {
         StudyHelper studyHelper = new StudyHelper();
         StudySubscription studySubscription = studyHelper.populateAndRetrieveStudy();
         Study study = studySubscription.getStudy();
         dao.save(study);
         
         ResultColumn column1 = new ResultColumn();
         ResultColumn column2 = new ResultColumn();
         ResultColumn column3 = new ResultColumn();
         
         column1.setAnnotationDefinition(studyHelper.getImageSeriesAnnotationDefinition());
         column1.setColumnIndex(0);
         column1.setEntityType(EntityTypeEnum.IMAGESERIES.getValue());
         
         column2.setAnnotationDefinition(studyHelper.getSampleAnnotationDefinition());
         column2.setColumnIndex(1);
         column2.setEntityType(EntityTypeEnum.SAMPLE.getValue());
         column2.setSortOrder(2);
         column2.setSortType(SortTypeEnum.ASCENDING.getValue());
         
         column3.setAnnotationDefinition(studyHelper.getSubjectAnnotationDefinition());
         column3.setColumnIndex(2);
         column3.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         column3.setSortOrder(1);
         column3.setSortType(SortTypeEnum.DESCENDING.getValue());
         
         Collection<ResultColumn> columnCollection = new HashSet<ResultColumn>();
         columnCollection.add(column1);
         columnCollection.add(column2);
         columnCollection.add(column3);
         
         CompoundCriterion compoundCriterion = studyHelper.createCompoundCriterion1();
         compoundCriterion.setBooleanOperator(BooleanOperatorEnum.OR.getValue());
         
         Query query = studyHelper.createQuery(compoundCriterion, columnCollection, studySubscription);
         
         QueryTranslator queryTranslator = new QueryTranslator(query, dao, resultHandler);
         
         QueryResult queryResult = queryTranslator.execute();
        assertEquals(6, queryResult.getRowCollection().size());
         for (ResultRow row : queryResult.getRowCollection()) {
             assertNotNull(row.getRowIndex());
             if (row.getRowIndex() == 5) {
                 // Subject should be 1 because these were sorted Descending
                 for (SubjectAnnotation value : row.getSubjectAssignment().getSubjectAnnotationCollection()) {
                     NumericAnnotationValue numericValue = (NumericAnnotationValue) value.getAnnotationValue();
                    assertEquals(Double.valueOf(1.0), numericValue.getNumericValue());
                 }
                 // Sample should be 1.0 because samples are sorted Ascending
                 for (AbstractAnnotationValue value : row.getSampleAcquisition().getAnnotationCollection()) {
                     NumericAnnotationValue numericValue = (NumericAnnotationValue) value;
                    assertEquals(Double.valueOf(1.0), numericValue.getNumericValue());
                 }
             }
             
             if (row.getRowIndex() == 6) {
                 // Subject should be 1 because these were sorted Descending
                 for (SubjectAnnotation value : row.getSubjectAssignment().getSubjectAnnotationCollection()) {
                     NumericAnnotationValue numericValue = (NumericAnnotationValue) value.getAnnotationValue();
                    assertEquals(Double.valueOf(1.0), numericValue.getNumericValue());
                 }
                 // Sample should be 10.0 because samples are sorted Ascending
                 for (AbstractAnnotationValue value : row.getSampleAcquisition().getAnnotationCollection()) {
                     NumericAnnotationValue numericValue = (NumericAnnotationValue) value;
                    assertEquals(Double.valueOf(10.0), numericValue.getNumericValue());
                 }
             }
         }
     }
     @SuppressWarnings({ "PMD" })
     public void testVasariQuery() {
         StudyHelper studyHelper = new StudyHelper();
         // If we ever use usernames, be sure to change this signature
         Study vasariStudy = studyHelper.retrieveStudyByName("VASARI", "", dao);
         if (vasariStudy != null) {
             StudySubscription vasariStudySubscription = new StudySubscription();
             vasariStudySubscription.setStudy(vasariStudy);
             
             AnnotationDefinition genderDefinition = dao.getAnnotationDefinition("Gender");
             AnnotationDefinition raceDefinition = dao.getAnnotationDefinition("race");
             AnnotationDefinition karnofskyDefinition = dao.getAnnotationDefinition("karnofsky");
             
             ResultColumn genderColumn = new ResultColumn();
             ResultColumn raceColumn = new ResultColumn();
             ResultColumn karnofskyColumn = new ResultColumn();
             
             genderColumn.setAnnotationDefinition(genderDefinition);
             genderColumn.setEntityType(EntityTypeEnum.SUBJECT.getValue());
             genderColumn.setColumnIndex(0);
             
             raceColumn.setAnnotationDefinition(raceDefinition);
             raceColumn.setEntityType(EntityTypeEnum.SUBJECT.getValue());
             raceColumn.setColumnIndex(1);
             raceColumn.setSortOrder(1);
             
             karnofskyColumn.setAnnotationDefinition(karnofskyDefinition);
             karnofskyColumn.setEntityType(EntityTypeEnum.SUBJECT.getValue());
             karnofskyColumn.setColumnIndex(2);
             karnofskyColumn.setSortOrder(2);
             karnofskyColumn.setSortType(SortTypeEnum.DESCENDING.getValue());
             
             StringComparisonCriterion maleCriterion = new StringComparisonCriterion();
             maleCriterion.setStringValue("F");
             maleCriterion.setEntityType(EntityTypeEnum.SUBJECT.getValue());
             maleCriterion.setAnnotationDefinition(genderDefinition);
             
             List<ResultColumn> columnCollection = new ArrayList<ResultColumn>();
             columnCollection.add(raceColumn);
             columnCollection.add(genderColumn);
             columnCollection.add(karnofskyColumn);
             
             Collection<AbstractCriterion> criterionCollection = new HashSet<AbstractCriterion>();
             criterionCollection.add(maleCriterion);
             
             CompoundCriterion compoundCriterion = new CompoundCriterion();
             compoundCriterion.setCriterionCollection(criterionCollection);
             Query query = studyHelper.createQuery(compoundCriterion, columnCollection, vasariStudySubscription);
             
             QueryTranslator queryTranslator = new QueryTranslator(query, dao, resultHandler);
             QueryResult queryResult = queryTranslator.execute();
             
             for (ResultRow row : queryResult.getRowCollection()) {
                 System.out.print("Row " + row.getRowIndex() 
                         + " for Subject " + row.getSubjectAssignment().getIdentifier());
                 for (ResultColumn column : columnCollection) {
                     ResultValue value = Cai2Util.retrieveValueFromRowColumn(row, column);
                     if (value != null) {
                         if (value.getValue() instanceof StringAnnotationValue) {
                             StringAnnotationValue stringValue = (StringAnnotationValue) value.getValue();
                             System.out.print(" - " + stringValue.getStringValue());
                         }
                         if (value.getValue() instanceof NumericAnnotationValue) {
                             NumericAnnotationValue numericValue = (NumericAnnotationValue) value.getValue();
                             System.out.print(" - " + numericValue.getNumericValue());    
                         }
                     }
                 }
                 System.out.println();
             }
         }
     }
 
     
     /**
      * @param caIntegrator2Dao the caIntegrator2Dao to set
      */
     public void setDao(CaIntegrator2DaoImpl caIntegrator2Dao) {
         this.dao = caIntegrator2Dao;
     }
 
     /**
      * @param resultHandler the resultHandler to set
      */
     public void setResultHandler(ResultHandlerImpl resultHandler) {
         this.resultHandler = resultHandler;
     }
 
 }

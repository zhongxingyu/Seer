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
 package gov.nih.nci.caintegrator2.application.query;
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.data.StudyHelper;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Test;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 /**
  * Tests that the CompoundCriterionHandler object can get the matches for various CompoundCriterion
  */
 public class CompoundCriterionHandlerTestIntegration extends AbstractTransactionalSpringContextTests {
 
     private CaIntegrator2Dao dao;
     private ArrayDataService arrayDataService;
     
     protected String[] getConfigLocations() {
         return new String[] {"classpath*:/**/dao-test-config.xml"};
     }
 
     @Test
     @SuppressWarnings({"PMD"})
     public void testGetMatches() throws InvalidCriterionException {
         StudyHelper studyHelper = new StudyHelper();
         Study study = studyHelper.populateAndRetrieveStudy().getStudy();
         Set<EntityTypeEnum> entityTypesInQuery = new HashSet<EntityTypeEnum>();
         dao.save(study);
         
         Query query = new Query();
         StudySubscription subscription = new StudySubscription();
         subscription.setStudy(study);
         query.setSubscription(subscription);
 
         // Try compoundCriterion1
         CompoundCriterion compoundCriterion1 = studyHelper.createCompoundCriterion1();
         entityTypesInQuery.add(EntityTypeEnum.IMAGESERIES);
         entityTypesInQuery.add(EntityTypeEnum.SAMPLE);
         entityTypesInQuery.add(EntityTypeEnum.SUBJECT);
         compoundCriterion1.setBooleanOperator(BooleanOperatorEnum.AND);
         CompoundCriterionHandler compoundCriterionHandler1 = CompoundCriterionHandler.create(compoundCriterion1);
         
         assertEquals(1, compoundCriterionHandler1.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
         
         compoundCriterion1.setBooleanOperator(BooleanOperatorEnum.OR);
         assertEquals(10, compoundCriterionHandler1.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
         
         // Try compoundCriterion2.
         CompoundCriterion compoundCriterion2 = studyHelper.createCompoundCriterion2();
         compoundCriterion2.setBooleanOperator(BooleanOperatorEnum.AND);
         CompoundCriterionHandler compoundCriterionHandler2 = CompoundCriterionHandler.create(compoundCriterion2);
         assertEquals(0, compoundCriterionHandler2.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
 
         compoundCriterion2.setBooleanOperator(BooleanOperatorEnum.OR);
         assertEquals(11, compoundCriterionHandler2.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
         
         // Try to combine criterion 1 and criterion 2 into a new compoundCriterion.
         CompoundCriterion compoundCriterion3 = new CompoundCriterion();
         compoundCriterion3.setCriterionCollection(new HashSet<AbstractCriterion>());
         compoundCriterion3.getCriterionCollection().add(compoundCriterion1);
         compoundCriterion3.getCriterionCollection().add(compoundCriterion2);
         // 6 results for 1
         compoundCriterion1.setBooleanOperator(BooleanOperatorEnum.OR);
         // 4 results for 2
         compoundCriterion2.setBooleanOperator(BooleanOperatorEnum.OR);
         
         // If we AND them together we should get 7 results
         compoundCriterion3.setBooleanOperator(BooleanOperatorEnum.AND);
         CompoundCriterionHandler compoundCriterionHandler3 = CompoundCriterionHandler.create(compoundCriterion3);
         assertEquals(7, compoundCriterionHandler3.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
         
         // If we OR them together we should get 7 results, all 5 subjects, plus 2 more samples for subject1.
         compoundCriterion3.setBooleanOperator(BooleanOperatorEnum.OR);
         Set<ResultRow> mostComplexRows = compoundCriterionHandler3.getMatches(dao, arrayDataService, query, entityTypesInQuery);
         assertEquals(14, mostComplexRows.size());
         
         // Test out the selected value criterion on the permissible value's for Samples.
         entityTypesInQuery = new HashSet<EntityTypeEnum>();
         entityTypesInQuery.add(EntityTypeEnum.SAMPLE);
         CompoundCriterion compoundCriterion4 = studyHelper.createCompoundCriterion3();
         CompoundCriterionHandler compoundCriterionHandler4 = CompoundCriterionHandler.create(compoundCriterion4);
         assertEquals(1, compoundCriterionHandler4.getMatches(dao, arrayDataService, query, entityTypesInQuery).size());
     }
     
     /**
      * @param caIntegrator2Dao the caIntegrator2Dao to set
      */
     public void setDao(CaIntegrator2Dao caIntegrator2Dao) {
         this.dao = caIntegrator2Dao;
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
 }

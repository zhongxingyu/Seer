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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataServiceStub;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoStub;
 import gov.nih.nci.caintegrator2.domain.application.CopyNumberAlterationCriterion;
 import gov.nih.nci.caintegrator2.domain.application.CopyNumberCriterionTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.GenomicCriterionTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.genomic.Array;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class CopyNumberAlterationCriterionHandlerTest {
 
     private CaIntegrator2DaoStub daoStub = new DaoStub();
     private ArrayDataServiceStub arrayDataServiceStub = new ArrayDataServiceStub();
     private Query query;
     private Study study;
     private SegmentData segmentData;
     
     @Before
     public void setUp() {
         Platform platform = daoStub.getPlatform("platformName");
         ReporterList reporterList = platform.addReporterList("reporterList", ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
         
         daoStub.clear();       
         study = new Study();
         query = new Query();
         query.setCopyNumberPlatform(platform);
         StudySubscription subscription = new StudySubscription();
         subscription.setStudy(study);
         query.setSubscription(subscription);
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         study.getAssignmentCollection().add(assignment);
         SampleAcquisition acquisition = new SampleAcquisition();
         Sample sample = new Sample();
         Array array = new Array();
         array.setPlatform(platform);
         ArrayData arrayData = new ArrayData();
         arrayData.setStudy(study);
         arrayData.setArray(array);
         arrayData.getReporterLists().add(reporterList);
         reporterList.getArrayDatas().add(arrayData);
         arrayData.setSample(sample);
         sample.setSampleAcquisition(acquisition);
         sample.getArrayDataCollection().add(arrayData);
         segmentData = new SegmentData();
         arrayData.getSegmentDatas().add(segmentData);
         segmentData.setArrayData(arrayData);
         acquisition.setSample(sample);
         assignment.getSampleAcquisitionCollection().add(acquisition);
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.getGenomicDataSources().add(new GenomicDataSourceConfiguration());
         study.setStudyConfiguration(studyConfiguration);
     }
 
     @Test
     public void testGetMatches() throws InvalidCriterionException {        
         CopyNumberAlterationCriterion criterion = new CopyNumberAlterationCriterion();
         CopyNumberAlterationCriterionHandler handler = CopyNumberAlterationCriterionHandler.create(criterion);
         Set<ResultRow> rows = new HashSet<ResultRow>();
         rows = handler.getMatches(daoStub, arrayDataServiceStub, query, new HashSet<EntityTypeEnum>());
         assertEquals(1, rows.size());
     }
     
     @Test
     public void testGetSegmentDataMatches() throws InvalidCriterionException {
         CopyNumberAlterationCriterion criterion = new CopyNumberAlterationCriterion();
         CopyNumberAlterationCriterionHandler handler = CopyNumberAlterationCriterionHandler.create(criterion);
         Set<SegmentData> segments = handler.getSegmentDataMatches(daoStub, study, null);
         assertEquals(1, segments.size());
         assertEquals(segmentData, segments.iterator().next());
     }
     
     @Test
     public void testGetters() {
         String platformName = "Test Platform";
         CopyNumberAlterationCriterion criterion = new CopyNumberAlterationCriterion();
         criterion.setPlatformName(platformName);
         
         CopyNumberAlterationCriterionHandler handler = CopyNumberAlterationCriterionHandler.create(criterion);
         assertFalse(handler.hasReporterCriterion());
         assertFalse(handler.isReporterMatchHandler());
         assertTrue(handler.hasCriterionSpecifiedSegmentValues());
         assertFalse(handler.hasCriterionSpecifiedReporterValues());
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(2f));
         
         criterion.setLowerLimit(1f);
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(2f));
         assertEquals(GenomicCriteriaMatchTypeEnum.NO_MATCH, handler.getSegmentValueMatchCriterionType(.1f));
         
         criterion.setLowerLimit(null);
         criterion.setUpperLimit(4f);
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(4f));
         assertEquals(GenomicCriteriaMatchTypeEnum.NO_MATCH, handler.getSegmentValueMatchCriterionType(5f));
         
         criterion.setLowerLimit(2f);
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(2f));
         assertEquals(GenomicCriteriaMatchTypeEnum.NO_MATCH, handler.getSegmentValueMatchCriterionType(1f));
         assertEquals(GenomicCriteriaMatchTypeEnum.NO_MATCH, handler.getSegmentValueMatchCriterionType(5f));
         
         criterion.setCopyNumberCriterionType(CopyNumberCriterionTypeEnum.CALLS_VALUE);
         Set<Integer> callsValues = new HashSet<Integer>();
         callsValues.add(Integer.decode("1"));
         criterion.setCallsValues(callsValues);
         assertTrue(handler.hasCriterionSpecifiedSegmentValues());
         assertTrue(handler.hasCriterionSpecifiedSegmentCallsValues());
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentCallsValueMatchCriterionType(1));
         assertFalse(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE.equals(handler.getSegmentCallsValueMatchCriterionType(2)));
         
         // test for various upper and lower limits
         criterion.setLowerLimit(2f);
         assertEquals(String.valueOf(2f),criterion.getDisplayLowerLimit());
         criterion.setUpperLimit(1f);
         assertEquals(String.valueOf(1f),criterion.getDisplayUpperLimit());
         assertFalse(criterion.isInsideBoundaryType());
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(2f));
         criterion.setLowerLimit(1f);
         criterion.setUpperLimit(2f);
         assertTrue(criterion.isInsideBoundaryType());
         assertEquals(GenomicCriteriaMatchTypeEnum.NO_MATCH, handler.getSegmentValueMatchCriterionType(3f));
         criterion.setLowerLimit(3f);
         criterion.setUpperLimit(1f);
         assertEquals(GenomicCriteriaMatchTypeEnum.MATCH_POSITIVE_OR_NEGATIVE, handler.getSegmentValueMatchCriterionType(4f));
         // test rest of criterion value possibilities
         criterion.setLowerLimit(1f);
         criterion.setUpperLimit(3f);
         assertEquals("",criterion.getDisplayChromosomeCoordinateHigh());
         criterion.setChromosomeCoordinateHigh(Integer.valueOf("2000"));
         assertEquals("2000",criterion.getDisplayChromosomeCoordinateHigh());
         assertEquals("",criterion.getDisplayChromosomeCoordinateLow());
         criterion.setChromosomeCoordinateLow(Integer.valueOf("2"));
         assertEquals("2",criterion.getDisplayChromosomeCoordinateLow());
         criterion.setChromosomeNumber("13");
         assertEquals("13",criterion.getChromosomeNumber());
         // test for platform name
         assertEquals(platformName,criterion.getPlatformName(GenomicCriterionTypeEnum.COPY_NUMBER));
         assertEquals(null,criterion.getPlatformName(GenomicCriterionTypeEnum.GENE_EXPRESSION));
     }
     
     private class DaoStub extends CaIntegrator2DaoStub {
         
 
         @Override
         public List<SegmentData> findMatchingSegmentDatas(CopyNumberAlterationCriterion copyNumberCriterion,
                 Study study, Platform platform) {
             List<SegmentData> segmentDatas = new ArrayList<SegmentData>();
             segmentDatas.add(segmentData);
             return segmentDatas;
         }
         
         @Override
         public List<SegmentData> findMatchingSegmentDatasByLocation(List<SegmentData> segmentDatasToMatch, 
                 Study study, Platform platform) {
             return findMatchingSegmentDatas(null, study, platform);
         }
         
     }
  
 }

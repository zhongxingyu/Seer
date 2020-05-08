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
 package gov.nih.nci.caintegrator2.application.arraydata;
 
 import static org.junit.Assert.assertEquals;
import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValueType;
import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
import gov.nih.nci.caintegrator2.application.arraydata.DataRetrievalRequest;
import gov.nih.nci.caintegrator2.application.arraydata.NetCDFManager;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.file.FileManagerStub;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Test;
 
 import ucar.nc2.NCdumpW;
 
 public class NetCDFManagerTest {
     
     private FileManagerStub fileManagerStub = new FileManagerStub();
     private NetCDFManager manager = new NetCDFManager(fileManagerStub);
     private long nextArrayDataId = 0L;
     
     @After
     public void tearDown() {
         File testFile = new File(fileManagerStub.getStudyDirectory(null), "data1.nc");
         if (testFile.exists()) {
             testFile.delete();
         }
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testStoreValuesEmptyArrayDatas() {
         ArrayDataValues values = new ArrayDataValues(new ArrayList<AbstractReporter>());
         manager.storeValues(values);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testStoreValuesMultipleStudies() {
         List<AbstractReporter> reporters = new ArrayList<AbstractReporter>();
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         reporter.setReporterList(new ReporterList());
         reporters.add(reporter);
         ArrayDataValues values = new ArrayDataValues(reporters);
         ArrayData arrayData1 = createArrayData(new Study());
         ArrayData arrayData2 = createArrayData(new Study());
         values.setFloatValue(arrayData1, reporter, ArrayDataValueType.EXPRESSION_SIGNAL, 0.0f);
         values.setFloatValue(arrayData2, reporter, ArrayDataValueType.EXPRESSION_SIGNAL, 0.0f);
         manager.storeValues(values);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testStoreValuesNullArrayDataIds() {
         List<AbstractReporter> reporters = new ArrayList<AbstractReporter>();
         GeneExpressionReporter reporter = new GeneExpressionReporter();
         ReporterList reporterList = new ReporterList();
         reporter.setReporterList(reporterList);
         reporterList.setId(1L);
         reporters.add(reporter);
         ArrayDataValues values = new ArrayDataValues(reporters);
         Study study = new Study();
         study.setId(1L);
         ArrayData arrayData = createArrayData(study);
         arrayData.getReporterLists().add(reporterList);
         arrayData.setId(null);
         values.setFloatValue(arrayData, reporter, ArrayDataValueType.EXPRESSION_SIGNAL, 0.0f);
         manager.storeValues(values);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testStoreValuesMultipleReporterLists() {
         List<AbstractReporter> reporters = new ArrayList<AbstractReporter>();
         GeneExpressionReporter reporter1 = new GeneExpressionReporter();
         reporter1.setReporterList(new ReporterList());
         GeneExpressionReporter reporter2 = new GeneExpressionReporter();
         reporter2.setReporterList(new ReporterList());
         reporters.add(reporter1);
         reporters.add(reporter2);
         ArrayDataValues values = new ArrayDataValues(reporters);
         ArrayData arrayData = createArrayData(new Study());
         values.setFloatValue(arrayData, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL, 0.0f);
         manager.storeValues(values);
     }
     
     @Test
     public void testStoreValues() throws IOException {
         Platform platform = new Platform();
         platform.setId(1l);
         List<AbstractReporter> reporters = new ArrayList<AbstractReporter>();
         GeneExpressionReporter reporter1 = new GeneExpressionReporter();
         ReporterList reporterList = new ReporterList();
         reporterList.setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
         reporterList.setId(1L);
         reporterList.setPlatform(platform);
         platform.getReporterLists().add(reporterList);
         reporter1.setReporterList(reporterList);
         reporter1.setIndex(0);
         GeneExpressionReporter reporter2 = new GeneExpressionReporter();
         reporter2.setReporterList(reporterList);
         reporter2.setIndex(1);
         GeneExpressionReporter reporter3 = new GeneExpressionReporter();
         reporter3.setReporterList(reporterList);
         reporter3.setIndex(2);
         reporters.add(reporter1);
         reporters.add(reporter2);
         reporters.add(reporter3);
         ArrayDataValues values = new ArrayDataValues(reporters);
         Study study = new Study();
         study.setId(1L);
         ArrayData arrayData1 = createArrayData(study);
         arrayData1.getReporterLists().add(reporterList);
         ArrayData arrayData2 = createArrayData(study);
         arrayData2.getReporterLists().add(reporterList);
         
         // Store first set of values
         values.setFloatValue(arrayData1, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 1.1f);
         values.setFloatValue(arrayData1, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 2.2f);
         values.setFloatValue(arrayData1, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 3.3f);
         values.setFloatValue(arrayData2, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 4.4f);
         values.setFloatValue(arrayData2, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 5.5f);
         values.setFloatValue(arrayData2, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 6.6f);
         values.setFloatValue(arrayData1, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL, 7.7f);
         values.setFloatValue(arrayData1, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL, 8.8f);
         values.setFloatValue(arrayData1, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL, 9.9f);
         values.setFloatValue(arrayData2, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL, 10.10f);
         values.setFloatValue(arrayData2, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL, 11.11f);
         values.setFloatValue(arrayData2, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL, 12.12f);
         manager.storeValues(values);
        NCdumpW.print("/tmp/data1.nc", new OutputStreamWriter(System.out), true, true, true, true, null, null);
        
         // Store additional values
         values = new ArrayDataValues(reporters);
         ArrayData arrayData3 = createArrayData(study);
         arrayData3.getReporterLists().add(reporterList);
         values.setFloatValue(arrayData3, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 13.13f);
         values.setFloatValue(arrayData3, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 14.14f);
         values.setFloatValue(arrayData3, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO, 15.15f);
         values.setFloatValue(arrayData3, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL, 16.16f);
         values.setFloatValue(arrayData3, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL, 17.17f);
         values.setFloatValue(arrayData3, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL, 18.18f);
         manager.storeValues(values);
        NCdumpW.print("/tmp/data1.nc", new OutputStreamWriter(System.out), true, true, true, true, null, null);
 
         // Retrieve values and compare
         DataRetrievalRequest request = new DataRetrievalRequest();
         request.addArrayData(arrayData1);
         request.addArrayData(arrayData2);
         request.addArrayData(arrayData3);
         request.addType(ArrayDataValueType.COPY_NUMBER_LOG2_RATIO);
         request.addType(ArrayDataValueType.EXPRESSION_SIGNAL);
         request.addReporters(reporters);
         values = manager.retrieveValues(request);
         assertEquals(3, values.getArrayDatas().size());
         assertEquals(3, values.getReporters().size());
         assertEquals(2, values.getTypes().size());
         assertEquals(1.1f, values.getFloatValue(arrayData1, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(2.2f, values.getFloatValue(arrayData1, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(3.3f, values.getFloatValue(arrayData1, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(4.4f, values.getFloatValue(arrayData2, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(5.5f, values.getFloatValue(arrayData2, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(6.6f, values.getFloatValue(arrayData2, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(7.7f, values.getFloatValue(arrayData1, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(8.8f, values.getFloatValue(arrayData1, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(9.9f, values.getFloatValue(arrayData1, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(10.10f, values.getFloatValue(arrayData2, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(11.11f, values.getFloatValue(arrayData2, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(12.12f, values.getFloatValue(arrayData2, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(13.13f, values.getFloatValue(arrayData3, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(14.14f, values.getFloatValue(arrayData3, reporter2, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(15.15f, values.getFloatValue(arrayData3, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(16.16f, values.getFloatValue(arrayData3, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(17.17f, values.getFloatValue(arrayData3, reporter2, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(18.18f, values.getFloatValue(arrayData3, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
 
         request = new DataRetrievalRequest();
         request.addArrayData(arrayData1);
         request.addArrayData(arrayData3);
         request.addType(ArrayDataValueType.COPY_NUMBER_LOG2_RATIO);
         request.addType(ArrayDataValueType.EXPRESSION_SIGNAL);
         request.addReporter(reporter1);
         request.addReporter(reporter3);
         values = manager.retrieveValues(request);
         assertEquals(2, values.getArrayDatas().size());
         assertEquals(2, values.getReporters().size());
         assertEquals(2, values.getTypes().size());
         assertEquals(1.1f, values.getFloatValue(arrayData1, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(3.3f, values.getFloatValue(arrayData1, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(7.7f, values.getFloatValue(arrayData1, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(9.9f, values.getFloatValue(arrayData1, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(13.13f, values.getFloatValue(arrayData3, reporter1, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(15.15f, values.getFloatValue(arrayData3, reporter3, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO), 0.0f);
         assertEquals(16.16f, values.getFloatValue(arrayData3, reporter1, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
         assertEquals(18.18f, values.getFloatValue(arrayData3, reporter3, ArrayDataValueType.EXPRESSION_SIGNAL), 0.0f);
 }
 
     private ArrayData createArrayData(Study study) {
         ArrayData arrayData = new ArrayData();
         arrayData.setStudy(study);
         arrayData.setId(nextArrayDataId++);
         arrayData.setSample(new Sample());
         arrayData.getSample().setSampleAcquisition(new SampleAcquisition());
         arrayData.getSample().getSampleAcquisition().setAssignment(new StudySubjectAssignment());
         arrayData.getSample().getSampleAcquisition().getAssignment().setStudy(study);
         return arrayData;
     }
 
 }

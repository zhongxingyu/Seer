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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.TestDataFiles;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoStub;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.cadsr.CaDSRFacadeStub;
 import gov.nih.nci.caintegrator2.external.cadsr.DataElement;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class StudyManagementServiceImplTest {
     
     private StudyManagementService studyManagementService;
     private CaIntegrator2DaoStub daoStub;
     private CaDSRFacadeStub caDSRFacadeStub;
 
     @Before
     public void setUp() throws Exception {
         ApplicationContext context = new ClassPathXmlApplicationContext("service-test-config.xml", StudyManagementServiceImplTest.class); 
         studyManagementService = (StudyManagementService) context.getBean("studyManagementService"); 
 		daoStub = (CaIntegrator2DaoStub) context.getBean("dao");
         daoStub.clear();                
         caDSRFacadeStub = (CaDSRFacadeStub) context.getBean("caDSRFacadeStub");
         caDSRFacadeStub.clear();
     }
 
     @Test
     public void testUpdate() {
         StudyConfiguration configTest = new StudyConfiguration();
         studyManagementService.save(configTest);
         assertTrue(daoStub.saveCalled);
     }
     
     @Test
     public void testDeploy() {
         StudyConfiguration configTest = new StudyConfiguration();
         studyManagementService.deployStudy(configTest);
         assertEquals(Status.DEPLOYED, configTest.getStatus());
         assertTrue(daoStub.saveCalled);
     }
     
     @Test
     public void testAddClinicalAnnotationFile() throws ValidationException, IOException {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyManagementService.save(studyConfiguration);
         DelimitedTextClinicalSourceConfiguration sourceConfiguration = 
             studyManagementService.addClinicalAnnotationFile(studyConfiguration, TestDataFiles.VALID_FILE, TestDataFiles.VALID_FILE.getName());
         assertEquals(1, studyConfiguration.getClinicalConfigurationCollection().size());
         assertTrue(studyConfiguration.getClinicalConfigurationCollection().contains(sourceConfiguration));
         assertEquals(4, sourceConfiguration.getAnnotationFile().getColumns().size());
         assertTrue(daoStub.saveCalled);
     }
     
     @Test
     public void testLoadClinicalAnnotation() throws ValidationException, IOException {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyManagementService.save(studyConfiguration);
         DelimitedTextClinicalSourceConfiguration sourceConfiguration = 
             studyManagementService.addClinicalAnnotationFile(studyConfiguration, TestDataFiles.VALID_FILE, TestDataFiles.VALID_FILE.getName());
         sourceConfiguration.getAnnotationFile().setIdentifierColumnIndex(0);
         studyManagementService.loadClinicalAnnotation(studyConfiguration); 
     }
     
     @Test 
     public void testAddGenomicSource() throws ConnectionException {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         GenomicDataSourceConfiguration genomicDataSourceConfiguration = new GenomicDataSourceConfiguration();
         studyManagementService.addGenomicSource(studyConfiguration, genomicDataSourceConfiguration);
         genomicDataSourceConfiguration.setId(Long.valueOf(1));        
         assertTrue(studyConfiguration.getGenomicDataSources().contains(genomicDataSourceConfiguration));
         assertTrue(daoStub.saveCalled);
     }
     
     @Test
     public void testGetManagedStudies() {
         assertNotNull(studyManagementService.getManagedStudies("username"));
         assertTrue(daoStub.getManagedStudiesCalled);
     }
     
     @Test
     public void testGetStudyEntity() {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.setId(1L);
         assertNotNull(studyManagementService.getRefreshedStudyEntity(studyConfiguration));
         assertTrue(daoStub.getCalled);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testGetStudyEntityNoId() {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyManagementService.getRefreshedStudyEntity(studyConfiguration);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void testGetStudyEntityIllegalClass() {
         Object object = new Object();
         studyManagementService.getRefreshedStudyEntity(object);
     }
     
     @Test
     public void testGetMatchingDefinitions() {
         FileColumn fileColumn = new FileColumn();
         fileColumn.setFieldDescriptor(new AnnotationFieldDescriptor());
         fileColumn.getFieldDescriptor().setKeywords("test keywords");
         List<AnnotationDefinition> definitions = studyManagementService.getMatchingDefinitions(fileColumn);
         assertEquals(1, definitions.size());
         assertEquals("definitionName", definitions.get(0).getDisplayName());
     }
     
     @Test
     public void testGetMatchingDataElements() {
         FileColumn fileColumn = new FileColumn();
         fileColumn.setFieldDescriptor(new AnnotationFieldDescriptor());
         fileColumn.getFieldDescriptor().setKeywords("test keywords");
         studyManagementService.getMatchingDataElements(fileColumn);
         assertTrue(caDSRFacadeStub.retreiveCandidateDataElementsCalled);
     }
     
     @Test
     public void testSetDefinition() {
         FileColumn fileColumn = new FileColumn();
         fileColumn.setFieldDescriptor(new AnnotationFieldDescriptor());
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.setId(1L);
         studyManagementService.setDefinition(fileColumn, annotationDefinition);
         assertTrue(daoStub.saveCalled);
         assertEquals(annotationDefinition, fileColumn.getFieldDescriptor().getDefinition());
     }
     
     @Test
     public void testSetDataElement() {
         FileColumn fileColumn = new FileColumn();
         fileColumn.setFieldDescriptor(new AnnotationFieldDescriptor());
         DataElement dataElement = new DataElement();
         dataElement.setLongName("longName");
         dataElement.setDefinition("definition");
         dataElement.setPublicId(1234L);
         studyManagementService.setDataElement(fileColumn, dataElement);
         assertTrue(daoStub.saveCalled);
         assertNotNull(fileColumn.getFieldDescriptor().getDefinition());
         assertNotNull(fileColumn.getFieldDescriptor().getDefinition().getCde());
         assertEquals("longName", fileColumn.getFieldDescriptor().getDefinition().getDisplayName());
         assertEquals("definition", fileColumn.getFieldDescriptor().getDefinition().getPreferredDefinition());
         assertEquals("1234", fileColumn.getFieldDescriptor().getDefinition().getCde().getPublicID());
     }
     
     @Test
     public void testMapSamples() {
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.getStudy().setAssignmentCollection(new HashSet<StudySubjectAssignment>());
         StudySubjectAssignment assignment1 = new StudySubjectAssignment();
         assignment1.setId(1L);
         assignment1.setIdentifier("E05004");
         assignment1.setSubjectAnnotation(new HashSet<SubjectAnnotation>());
         studyConfiguration.getStudy().getAssignmentCollection().add(assignment1);
         StudySubjectAssignment assignment2 = new StudySubjectAssignment();
         assignment2.setId(2L);
         assignment2.setIdentifier("E05012");
         assignment2.setSubjectAnnotation(new HashSet<SubjectAnnotation>());
         studyConfiguration.getStudy().getAssignmentCollection().add(assignment2);
         GenomicDataSourceConfiguration genomicDataSourceConfiguration = new GenomicDataSourceConfiguration();
         Sample sample1 = new Sample();
         sample1.setId(1L);
         sample1.setName("5500024030700072107989.B09");
         genomicDataSourceConfiguration.getSamples().add(sample1);
         Sample sample2 = new Sample();
         sample2.setId(2L);
         sample2.setName("5500024030700072107989.G10");
         genomicDataSourceConfiguration.getSamples().add(sample2);
         studyConfiguration.getGenomicDataSources().add(genomicDataSourceConfiguration);
        studyManagementService.mapSamples(studyConfiguration, TestDataFiles.SIMPLE_SAMPLE_MAPPING_FILE);
         assertEquals(1, assignment1.getSampleAcquisitionCollection().size());
         assertEquals(sample1, assignment1.getSampleAcquisitionCollection().iterator().next().getSample());
         assertEquals(1, assignment2.getSampleAcquisitionCollection().size());
         assertEquals(sample2, assignment2.getSampleAcquisitionCollection().iterator().next().getSample());
     }
 
 }

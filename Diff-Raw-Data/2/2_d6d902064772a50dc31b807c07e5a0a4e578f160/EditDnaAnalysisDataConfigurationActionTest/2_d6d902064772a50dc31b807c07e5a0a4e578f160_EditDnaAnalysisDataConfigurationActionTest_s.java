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
 package gov.nih.nci.caintegrator2.web.action.study.management;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.TestDataFiles;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementServiceStub;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 import gov.nih.nci.caintegrator2.web.action.AbstractSessionBasedTest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 @SuppressWarnings("PMD")
 public class EditDnaAnalysisDataConfigurationActionTest extends AbstractSessionBasedTest {
 
     private EditDnaAnalysisDataConfigurationAction action;
     private StudyManagementServiceStub studyManagementServiceStub;
 
     @Before
     public void setUp() {
         super.setUp();
 
         ApplicationContext context = new ClassPathXmlApplicationContext("study-management-action-test-config.xml", EditDnaAnalysisDataConfigurationActionTest.class); 
         action = (EditDnaAnalysisDataConfigurationAction) context.getBean("editDnaAnalysisDataConfigurationAction");
         studyManagementServiceStub = (StudyManagementServiceStub) context.getBean("studyManagementService");
         studyManagementServiceStub.clear();
     }
     
     @Test
     public void testEdit() {
         action.setUseGlad(true);
         action.setGladUrl(ConfigurationParameter.GENE_PATTERN_URL.getDefaultValue());
         action.getGenomicSource().setDnaAnalysisDataConfiguration(null);
         action.prepare();
         action.edit();
         assertNotNull(action.getGenomicSource().getDnaAnalysisDataConfiguration());
         assertEquals(ConfigurationParameter.GENE_PATTERN_URL.getDefaultValue(), action.getGladUrl());
     }
     
     @Test
     public void testSave() {
         action.prepare();
         action.setUseGlad(true);
         action.setGladUrl("gladUrl");
         action.setCaDnaCopyUrl("caDnaCopyUrl");
         action.save();
         assertEquals("gladUrl", action.getDnaAnalysisDataConfiguration().getSegmentationService().getUrl());
         assertTrue(studyManagementServiceStub.saveDnaAnalysisMappingFileCalled);
         assertTrue(studyManagementServiceStub.saveCalled);
         action.setUseGlad(false);
         action.save();
         assertEquals("caDnaCopyUrl", action.getDnaAnalysisDataConfiguration().getSegmentationService().getUrl());
     }
     
     @Test
     public void testValidate() {
         action.prepare();
         action.setFormAction(EditDnaAnalysisDataConfigurationAction.EDIT_ACTION);
         action.validate();
         assertFalse(action.hasFieldErrors());
         action.setFormAction(EditDnaAnalysisDataConfigurationAction.SAVE_ACTION);
         action.validate();
         assertTrue(action.hasFieldErrors());
         action.clearErrorsAndMessages();
         action.setMappingFile(TestDataFiles.XBA_COPY_NUMBER_CHP_FILE);
         action.getDnaAnalysisDataConfiguration().getSegmentationService().setUrl("");
         action.getGenomicSource().setPlatformVendor("Affymetrix");
         action.validate();
         assertTrue(action.hasFieldErrors());
         action.clearErrorsAndMessages();
         action.setUseGlad(false);
         action.setCaDnaCopyUrl("url");
         action.validate();
         assertTrue(action.hasFieldErrors());
         action.clearErrorsAndMessages();
         GenomicDataSourceConfiguration genomicSource = new GenomicDataSourceConfiguration();
         genomicSource.setPlatformVendor("Affymetrix");
         action.setGenomicSource(genomicSource);
         action.setMappingFile(TestDataFiles.REMBRANDT_COPY_NUMBER_FILE);
         action.validate();
         assertFalse(action.hasFieldErrors());
         genomicSource.setPlatformVendor("Agilent");
         action.setMappingFile(TestDataFiles.REMBRANDT_COPY_NUMBER_FILE);
         action.validate();
         assertTrue(action.hasFieldErrors());
         action.clearErrorsAndMessages();
         action.setMappingFile(TestDataFiles.SHORT_AGILENT_COPY_NUMBER_FILE);
         action.validate();
         assertFalse(action.hasFieldErrors());
        action.getGenomicSource().setSingleDataFile(true);
         action.validate();
         assertTrue(action.hasFieldErrors());
         action.setMappingFile(TestDataFiles.REMBRANDT_COPY_NUMBER_SINGLE_FILE);
     }
     
 }

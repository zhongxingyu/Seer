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
 package gov.nih.nci.caintegrator2.application.analysis;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import edu.mit.broad.genepattern.gp.services.GenePatternServiceException;
 import edu.mit.broad.genepattern.gp.services.JobInfo;
 import edu.mit.broad.genepattern.gp.services.ParameterInfo;
 import edu.mit.broad.genepattern.gp.services.TaskInfo;
 import gov.nih.nci.caintegrator2.application.kmplot.CaIntegratorKMPlotServiceStub;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlot;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlotServiceCaIntegratorImpl;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlotTypeEnum;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementServiceForKMPlotStub;
import gov.nih.nci.caintegrator2.application.query.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoStub;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AnalysisServiceTest {
 
     private AnalysisService service;
     private TestGenePatternClientStub genePatternClientStub = new TestGenePatternClientStub();
     // KMPlot Items
     private CaIntegratorKMPlotServiceStub caIntegratorPlotServiceStub = new CaIntegratorKMPlotServiceStub();
     private CaIntegrator2DaoStub daoStub = new CaIntegrator2DaoStub();
     private QueryManagementServiceForKMPlotStub queryManagementServiceStub = new QueryManagementServiceForKMPlotStub();
     
     @Before
     public void setUp() {
         AnalysisServiceImpl serviceImpl = new AnalysisServiceImpl();
         KMPlotServiceCaIntegratorImpl kmPlotService = new KMPlotServiceCaIntegratorImpl();
         kmPlotService.setCaIntegratorPlotService(caIntegratorPlotServiceStub);
         caIntegratorPlotServiceStub.clear();
         serviceImpl.setGenePatternClient(genePatternClientStub); 
         serviceImpl.setDao(daoStub);
         serviceImpl.setKmPlotService(kmPlotService);
         serviceImpl.setQueryManagementService(queryManagementServiceStub);
         service = serviceImpl;
     }
 
     @Test
     public void testGetGenePatternMethods() throws GenePatternServiceException {
         ServerConnectionProfile server = new ServerConnectionProfile();
         List<AnalysisMethod> methods = service.getGenePatternMethods(server);
         assertEquals(2, methods.size());
         assertEquals(AnalysisServiceType.GENEPATTERN, methods.get(0).getServiceType());
         assertEquals("task1", methods.get(0).getName());
         assertEquals("description1", methods.get(0).getDescription());
         assertEquals(3, methods.get(0).getParameters().size());
         assertEquals("parameter1", methods.get(0).getParameters().get(0).getName());
         assertEquals(true, methods.get(0).getParameters().get(0).isRequired());
         assertEquals(false, methods.get(0).getParameters().get(1).isRequired());
         assertEquals(AnalysisParameterType.STRING, methods.get(0).getParameters().get(0).getType());
         AnalysisParameter parameterWithChoices = methods.get(0).getParameters().get(2);
         IntegerParameterValue value = (IntegerParameterValue) parameterWithChoices.getDefaultValue();
         assertEquals(methods.get(0).getParameters().get(2), value.getParameter());
         assertEquals(Integer.valueOf(2), value.getValue());
         assertEquals("parameter3", parameterWithChoices.getName());
         assertEquals(2, parameterWithChoices.getChoiceKeys().size());
         assertEquals(Integer.valueOf(1), ((IntegerParameterValue) parameterWithChoices.getChoice("choice1")).getValue());
     }
     
     @Test
     public void testExecuteGenePatternJob() throws GenePatternServiceException {
         AnalysisMethodInvocation invocation = new AnalysisMethodInvocation();
         ServerConnectionProfile server = new ServerConnectionProfile();
         AnalysisMethod method = new AnalysisMethod();
         server.setUrl("http://localhost");
         method.setName("method");
         invocation.setMethod(method);
         StringParameterValue parameterValue = new StringParameterValue();
         parameterValue.setValue("value");
         AnalysisParameter parameter = new AnalysisParameter();
         parameter.setType(AnalysisParameterType.STRING);
         parameter.setName("parameter");
         method.getParameters().add(parameter);
         parameterValue.setParameter(parameter);
         invocation.setParameterValue(parameter, parameterValue);
         service.executeGenePatternJob(server, invocation);
         assertEquals("method", genePatternClientStub.taskName);
         assertEquals(1, genePatternClientStub.parameters.size());
         ParameterInfo genePatternParameter = genePatternClientStub.parameters.get(0);
         assertEquals("parameter", genePatternParameter.getName());
         assertEquals("value", genePatternParameter.getValue());
     }
     
     private void runKMPlotTest(KMPlotStudyCreator studyCreator, StudySubscription subscription, AbstractKMParameters annotationParameters) {
         KMPlot kmPlot = service.createKMPlot(subscription, annotationParameters);
         
         assertNotNull(kmPlot);
         assertTrue(caIntegratorPlotServiceStub.computeLogRankPValueBetweenCalled);
         assertTrue(caIntegratorPlotServiceStub.getChartCalled);
         assertTrue(daoStub.retrieveValueForAnnotationSubjectCalled);
         boolean exceptionCaught = false;
         try { // Try giving no survival value definition.
             annotationParameters.setSurvivalValueDefinition(null);
             kmPlot = service.createKMPlot(subscription, annotationParameters);
         } catch (IllegalArgumentException e) {
             exceptionCaught = true;
         }
         assertTrue(exceptionCaught);
         
         exceptionCaught = false;
         studyCreator.getSurvivalValueDefinition().setLastFollowupDate(null);
         try { // Try giving survivalValueDefinition without a followup date
             annotationParameters.setSurvivalValueDefinition(studyCreator.getSurvivalValueDefinition());
             kmPlot = service.createKMPlot(subscription, annotationParameters);
         } catch (IllegalArgumentException e) {
             exceptionCaught = true;
         }
         assertTrue(exceptionCaught);
     }
     
     @Test
     public void testCreateAnnotationBasedKMPlot() {
         KMPlotStudyCreator studyCreator = new KMPlotStudyCreator();
         Study study = studyCreator.createKMPlotStudy();
         StudySubscription subscription = new StudySubscription();
         subscription.setStudy(study);
         queryManagementServiceStub.kmPlotType = KMPlotTypeEnum.ANNOTATION_BASED;
         KMAnnotationBasedParameters annotationParameters = new KMAnnotationBasedParameters();
         annotationParameters.setEntityType(EntityTypeEnum.SUBJECT);
         annotationParameters.setSelectedAnnotation(studyCreator.getGroupAnnotationField());
         annotationParameters.getSelectedValues().addAll(studyCreator.getPlotGroupValues());
         annotationParameters.setSurvivalValueDefinition(studyCreator.getSurvivalValueDefinition());
         assertTrue(annotationParameters.validate());
         runKMPlotTest(studyCreator, subscription, annotationParameters);
         
     }
     
     @Test
     public void testCreateGeneExpressionBasedKMPlot() {
         KMPlotStudyCreator studyCreator = new KMPlotStudyCreator();
         Study study = studyCreator.createKMPlotStudy();
         StudySubscription subscription = new StudySubscription();
         subscription.setStudy(study);
         queryManagementServiceStub.kmPlotType = KMPlotTypeEnum.GENE_EXPRESSION;
         KMGeneExpressionBasedParameters geneExpressionParameters = new KMGeneExpressionBasedParameters();
         geneExpressionParameters.setGeneSymbol("EGFR");
         geneExpressionParameters.setOverexpressedFoldChangeNumber(2.0F);
         geneExpressionParameters.setUnderexpressedFoldChangeNumber(2.0F);
         geneExpressionParameters.setSurvivalValueDefinition(studyCreator.getSurvivalValueDefinition());
         assertTrue(geneExpressionParameters.validate());
         runKMPlotTest(studyCreator, subscription, geneExpressionParameters);
         
     }
     
     @Test
     public void testCreateQueryBasedKMPlot() {
         KMPlotStudyCreator studyCreator = new KMPlotStudyCreator();
         Study study = studyCreator.createKMPlotStudy();
         StudySubscription subscription = new StudySubscription();
         subscription.setStudy(study);
         queryManagementServiceStub.kmPlotType = KMPlotTypeEnum.QUERY_BASED;
         KMQueryBasedParameters queryBasedParameters = new KMQueryBasedParameters();
         queryBasedParameters.setExclusiveGroups(true);
         queryBasedParameters.setAddPatientsNotInQueriesGroup(true);
         Query query1 = new Query();
         query1.setId(Long.valueOf(1));
         query1.setSubscription(subscription);
        query1.setResultType(ResultTypeEnum.GENOMIC.getValue());
         queryBasedParameters.getQueries().add(query1);
         Query query2 = new Query();
         query2.setId(Long.valueOf(2));
         query2.setSubscription(subscription);
         queryBasedParameters.getQueries().add(query2);
         queryBasedParameters.setSurvivalValueDefinition(studyCreator.getSurvivalValueDefinition());
         assertTrue(queryBasedParameters.validate());
         runKMPlotTest(studyCreator, subscription, queryBasedParameters);
        assertEquals(ResultTypeEnum.CLINICAL.getValue(), query1.getResultType());
     }
     
     
 
     private final class TestGenePatternClientStub extends GenePatternClientStub {
 
         private String taskName;
         private List<ParameterInfo> parameters;
 
         @SuppressWarnings("unchecked")
         public TaskInfo[] getTasks() throws GenePatternServiceException {
             TaskInfo[] tasks = new TaskInfo[4];
             tasks[0] = new TaskInfo();
             tasks[0].setName("task1");
             tasks[0].setDescription("description1");
             tasks[0].setTaskInfoAttributes(new HashMap<String, String>());
             tasks[0].setParameterInfoArray(new ParameterInfo[3]);
             tasks[0].getParameterInfoArray()[0] = new ParameterInfo();
             tasks[0].getParameterInfoArray()[0].setName("parameter1");
             tasks[0].getParameterInfoArray()[0].setAttributes(new HashMap<String, String>());
             tasks[0].getParameterInfoArray()[0].getAttributes().put("type", "java.lang.String");
             tasks[0].getParameterInfoArray()[0].getAttributes().put("optional", "");
             tasks[0].getParameterInfoArray()[0].setDefaultValue("default");
             tasks[0].getParameterInfoArray()[0].setChoices(new HashMap());
             
 
             tasks[0].getParameterInfoArray()[1] = new ParameterInfo();
             tasks[0].getParameterInfoArray()[1].setName("parameter2");
             tasks[0].getParameterInfoArray()[1].setAttributes(new HashMap<String, String>());
             tasks[0].getParameterInfoArray()[1].getAttributes().put("type", "java.io.File");
             tasks[0].getParameterInfoArray()[1].getAttributes().put("fileFormat", "gct;res");
             tasks[0].getParameterInfoArray()[1].getAttributes().put("optional", "on");
             
             tasks[0].getParameterInfoArray()[2] = new ParameterInfo();
             tasks[0].getParameterInfoArray()[2].setName("parameter3");
             tasks[0].getParameterInfoArray()[2].setDefaultValue("2");
             tasks[0].getParameterInfoArray()[2].setAttributes(new HashMap<String, String>());
             tasks[0].getParameterInfoArray()[2].getAttributes().put("type", "java.lang.Integer");
             tasks[0].getParameterInfoArray()[2].setValue("1=choice1;2=choice2");
             
             tasks[1] = new TaskInfo();
             tasks[1].setName("task2");
             tasks[1].setDescription("description2");
             tasks[1].setParameterInfoArray(new ParameterInfo[2]);
             tasks[1].setTaskInfoAttributes(new HashMap<String, String>());
             tasks[1].getParameterInfoArray()[0] = new ParameterInfo();
             tasks[1].getParameterInfoArray()[0].setName("parameter1");
             tasks[1].getParameterInfoArray()[0].setAttributes(new HashMap<String, String>());
             tasks[1].getParameterInfoArray()[0].getAttributes().put("type", "java.lang.String");
             tasks[1].getParameterInfoArray()[0].setChoices(new HashMap());
             tasks[1].getParameterInfoArray()[1] = new ParameterInfo();
             tasks[1].getParameterInfoArray()[1].setName("parameter2");
             tasks[1].getParameterInfoArray()[1].setAttributes(new HashMap<String, String>());
             tasks[1].getParameterInfoArray()[1].getAttributes().put("type", "java.io.File");
             tasks[1].getParameterInfoArray()[1].getAttributes().put("fileFormat", "cls");
             tasks[1].getParameterInfoArray()[1].setChoices(new HashMap());
             
             tasks[2] = new TaskInfo();
             tasks[2].setName("task3");
             tasks[2].setDescription("description3");
             tasks[2].setTaskInfoAttributes(new HashMap<String, String>());
             tasks[2].setParameterInfoArray(new ParameterInfo[3]);
             tasks[2].getParameterInfoArray()[0] = new ParameterInfo();
             tasks[2].getParameterInfoArray()[0].setName("parameter1");
             tasks[2].getParameterInfoArray()[0].setAttributes(new HashMap<String, String>());
             tasks[2].getParameterInfoArray()[0].getAttributes().put("type", "java.lang.Float");
             tasks[2].getParameterInfoArray()[0].setChoices(new HashMap());
 
             tasks[2].getParameterInfoArray()[1] = new ParameterInfo();
             tasks[2].getParameterInfoArray()[1].setName("parameter2");
             tasks[2].getParameterInfoArray()[1].setAttributes(new HashMap<String, String>());
             tasks[2].getParameterInfoArray()[1].getAttributes().put("type", "java.io.File");
             tasks[2].getParameterInfoArray()[1].getAttributes().put("fileFormat", "gct;res");
             tasks[2].getParameterInfoArray()[1].setChoices(new HashMap());
 
             tasks[2].getParameterInfoArray()[2] = new ParameterInfo();
             tasks[2].getParameterInfoArray()[2].setName("parameter3");
             tasks[2].getParameterInfoArray()[2].setAttributes(new HashMap<String, String>());
             tasks[2].getParameterInfoArray()[2].getAttributes().put("type", "java.io.File");
             tasks[2].getParameterInfoArray()[2].getAttributes().put("fileFormat", "cls");
             tasks[2].getParameterInfoArray()[2].setChoices(new HashMap());
             
             tasks[3] = new TaskInfo();
             tasks[3].setName("task3");
             tasks[3].setDescription("description3");
             Map<String, String> task4Attributes = new HashMap<String, String>();
             task4Attributes.put("taskType", "Visualizer");
             tasks[3].setTaskInfoAttributes(task4Attributes);
             tasks[3].setParameterInfoArray(new ParameterInfo[3]);
             return tasks;
         }
         
         /**
          * {@inheritDoc}
          */
         @Override
         public JobInfo runAnalysis(String taskName, List<ParameterInfo> parameters) throws GenePatternServiceException {
             this.taskName = taskName;
             this.parameters = parameters;
             return null;
         }
 
 
     }
 
 }

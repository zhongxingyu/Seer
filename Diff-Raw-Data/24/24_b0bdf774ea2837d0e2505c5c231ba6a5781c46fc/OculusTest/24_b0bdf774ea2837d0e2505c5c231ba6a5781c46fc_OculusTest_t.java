 /*******************************************************************************
 * Copyright 2012 Ivan Shubin http://mindengine.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
 package net.mindengine.oculus.experior.framework.test;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.mindengine.oculus.experior.ClassUtils;
 import net.mindengine.oculus.experior.ExperiorConfig;
 import net.mindengine.oculus.experior.annotations.Action;
 import net.mindengine.oculus.experior.annotations.InputParameter;
 import net.mindengine.oculus.experior.annotations.OutputParameter;
 import net.mindengine.oculus.experior.annotations.events.AfterTest;
 import net.mindengine.oculus.experior.annotations.events.BeforeAction;
 import net.mindengine.oculus.experior.annotations.events.BeforeErrorHandler;
 import net.mindengine.oculus.experior.annotations.events.BeforeRollback;
 import net.mindengine.oculus.experior.annotations.events.BeforeTest;
 import net.mindengine.oculus.experior.annotations.events.OnTestFailure;
 import net.mindengine.oculus.experior.exception.TestConfigurationException;
 import net.mindengine.oculus.experior.framework.report.DefaultReport;
 import net.mindengine.oculus.experior.reporter.Report;
 import net.mindengine.oculus.experior.reporter.ReportIcon;
 import net.mindengine.oculus.experior.reporter.nodes.BranchReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.ReportNode;
 import net.mindengine.oculus.experior.reporter.remote.ReportClient;
 import net.mindengine.oculus.experior.reporter.remote.wrappers.TestRun;
 import net.mindengine.oculus.experior.reporter.remote.wrappers.TestRunParameter;
 import net.mindengine.oculus.experior.reporter.remote.wrappers.TestRunStatus;
 import net.mindengine.oculus.experior.reporter.render.ReportRender;
 import net.mindengine.oculus.experior.reporter.render.XmlReportRender;
 import net.mindengine.oculus.experior.suite.Suite;
 import net.mindengine.oculus.experior.suite.SuiteRunner;
 import net.mindengine.oculus.experior.test.TestSession;
 import net.mindengine.oculus.experior.test.descriptors.ActionInformation;
 import net.mindengine.oculus.experior.test.descriptors.ErrorInformation;
 import net.mindengine.oculus.experior.test.descriptors.FieldDescriptor;
 import net.mindengine.oculus.experior.test.descriptors.RollbackInformation;
 import net.mindengine.oculus.experior.test.descriptors.TestDescriptor;
 import net.mindengine.oculus.experior.test.descriptors.TestInformation;
 
 public class OculusTest {
     protected Report report;
     protected TestSession testSession;
     protected Date startTime;
     protected Suite suite;
 
     
     public String getSuiteParameter(String parameterName) {
     	if ( suite != null && suite.getParameters() != null ) {
     		return suite.getParameters().get(parameterName);
     	}
     	return null;
     }
     
     /**
      * 
      * @param testInformation
      */
     @BeforeTest
     public void onBeforeTest(TestInformation testInformation) throws Exception {
         SuiteRunner suiteRunner = testInformation.getTestRunner().getSuiteRunner();
         if(suiteRunner!=null) {
             suite = suiteRunner.getSuite();
         }
         
         startTime = new Date();
         /*
          * Checking if it is a root test
          */
         testSession = testInformation.getTestRunner().getTestSession();
         
         //Checking whether the test is root or not
         if (testInformation.getTestRunner().getParent()==null) {
             /*
              * Creating an instance of reporter
              */
             report = new DefaultReport(ExperiorConfig.getInstance().getReportConfiguration());
             testSession.getData().put("report", report);
         } else {
             report = (Report) testSession.getData().get("report");
             if(report==null) {
                 throw new TestConfigurationException("Cannot find report in testSession");
             }
         }
     }
 
     @AfterTest
     public void onAfterTest(TestInformation testInformation) throws Exception {
         if (testInformation.getTestRunner().getParent()==null) {
             
             Long suiteRunId = getSuiteIdForTest(testInformation);
             /*
              * Collecting test run data
              */
             
             
             TestRun testRun = new TestRun();
             testRun.setSuiteRunId(suiteRunId);
             testRun.setName(testInformation.getTestName());
             testRun.setDescription(testInformation.getTestRunner().getTestDefinition().getDescription());
             testRun.setStartTime(startTime.getTime());
             testRun.setEndTime(new Date().getTime());
             testRun.setReport(encodeReport());
             testRun.setParameters(collectTestRunParameters(testInformation.getTestRunner().getTestDescriptor()));
             resolveTestRunStatus(testInformation, testRun);
             
             ReportClient reportClient = getReportClient();
             
             Long testRunId = reportClient.createTestRun(suiteRunId, testRun);
             testInformation.setTestRunId(testRunId);
 
             testRun.setId(testRunId);
            
            if ( suite != null ) {
                /*
                 * Adding just created test run to the suite
                 */
                if (suite.getTestRuns() == null) {
                    suite.setTestRuns(new LinkedList<TestRun>());
                }
                suite.getTestRuns().add(testRun);
             }
         }
     }
 
     private ReportClient getReportClient() {
         ExperiorConfig cfg = ExperiorConfig.getInstance();
         return new ReportClient(cfg.getMandatoryField(ExperiorConfig.OCULUS_URL), cfg.get(ExperiorConfig.OCULUS_API_AUTH_TOKEN));
     }
 
     private void resolveTestRunStatus(TestInformation testInformation, TestRun testRun) {
         ReportNode reportNode = report.getMainBranch();
         
         if (testInformation.getFailureCause() != null || reportNode.hasLevel(ReportNode.ERROR)) {
             testRun.setStatus(TestRunStatus.FAILED);
             /*
              * Fetching reasons and translating them to the needed format
              */
             testRun.setReasons(report.getMainBranch().collectReasons(ReportNode.ERROR));
 
             // Setting test status to testInformation so it could be fetched
             // by test-run-manager system
             testInformation.setStatus(TestInformation.STATUS_FAILED);
         } else if (reportNode.hasLevel(ReportNode.WARN)) {
             testRun.setStatus(TestRunStatus.WARNING);
                 
             testRun.setReasons(report.getMainBranch().collectReasons(ReportNode.WARN));
             
             // Setting test status to testInformation so it could be fetched
             // by test-run-manager system
             testInformation.setStatus(TestInformation.STATUS_WARNING);
         } else {
             testRun.setStatus(TestRunStatus.PASSED);
 
             // Setting test status to testInformation so it could be fetched
             // by test-run-manager system
             testInformation.setStatus(TestInformation.STATUS_PASSED);
         }
         
     }
 
     private String encodeReport() {
         ReportNode reportNode = report.getMainBranch();
         ReportRender reportRender = new XmlReportRender();
         return reportRender.render(reportNode);
     }
 
     private Long getSuiteIdForTest(TestInformation testInformation) {
         SuiteRunner suiteRunner = testInformation.getTestRunner().getSuiteRunner();
         if (suiteRunner != null) {
             suite = suiteRunner.getSuite();
             if (suite != null) {
                 return suite.getId();
             }
         }
         return 0L;
     }
 
     private List<TestRunParameter> collectTestRunParameters(TestDescriptor testDescriptor) {
         /*
          * Saving input parameter values
          */
         List<TestRunParameter> parameters = new LinkedList<TestRunParameter>();
         Map<String, FieldDescriptor> inputParametersMap = testDescriptor.getFieldDescriptors(InputParameter.class);
         
         if(inputParametersMap!=null) {
             addAllParameters(parameters, inputParametersMap, true);
         }
         /*
          * Saving output parameter values
          */
         Map<String, FieldDescriptor> outputParametersMap = testDescriptor.getFieldDescriptors(OutputParameter.class);
         if(outputParametersMap!=null){
             addAllParameters(parameters, outputParametersMap, false);
         }
 
         return parameters;
     }
 
     private void addAllParameters(List<TestRunParameter> parameters, Map<String, FieldDescriptor> inputParametersMap,
             boolean isInput) {
         for (Map.Entry<String, FieldDescriptor> parameter : inputParametersMap.entrySet()) {
             Object value = null;
             try {
                 value = ClassUtils.getFieldValue(parameter.getValue().getField(), this);
             }
             catch (Exception e) {
                 
             }
             String name = parameter.getKey();
             String strValue;
    
             if (value != null) {
                 strValue = ClassUtils.convertParameterToString(value.getClass(), value);
             } else
                 strValue = "null";
             parameters.add(new TestRunParameter(name, strValue, isInput));
         }
     }
 
     @BeforeAction
     public void onBeforeAction(ActionInformation actionInformation) {
         Action action = actionInformation.getActionMethod().getAnnotation(Action.class);
         
         if ( action != null && action.silent()) {
             //Avoid making report branch for this action
             return;
         }
         report.branch(BranchReportNode.ACTION).title(actionInformation.getActionName()).icon(ReportIcon.ACTION);
     }
 
     @BeforeRollback
     public void onBeforeRollback(RollbackInformation rollbackInformation) {
         report.branch(BranchReportNode.ACTION).title(rollbackInformation.getName()).icon(ReportIcon.ROLLBACK);
     }
 
     @BeforeErrorHandler
     public void onBeforeError(ErrorInformation errorInformation) {
         report.branch(BranchReportNode.ACTION).title(errorInformation.getName()).icon(ReportIcon.ACTION);
     }
 
     @OnTestFailure
     public void onTestFailure(TestInformation testInformation) {
         report.error(testInformation.getFailureCause());
     }
 
     public Report getReport() {
         return report;
     }
 
     public void setReport(Report report) {
         this.report = report;
     }
 
 }

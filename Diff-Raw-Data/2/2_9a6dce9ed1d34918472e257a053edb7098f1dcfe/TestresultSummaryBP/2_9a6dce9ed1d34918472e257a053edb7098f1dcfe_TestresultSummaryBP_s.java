 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.ClientTestFactory;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterDetailsPO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestResultPO;
 import org.eclipse.jubula.client.core.model.ITestResultSummary;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.model.TestResult;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.MonitoringConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 /**
  * Class to get keywords and summary from testresultnode to persist in database
  * 
  * @author BREDEX GmbH
  * @created Mar 4, 2010
  */
 public class TestresultSummaryBP {
     /**
      * <code>autrun</code>
      * if autconfig is null because autrun ist used,
      * use this constant for summary table
      */
     public static final String AUTRUN = "autrun"; //$NON-NLS-1$
 
     /** constant for keyword type Test Step */
     public static final int TYPE_TEST_STEP = 3;
     
     /** constant for keyword type Test Case */
     public static final int TYPE_TEST_CASE = 2;
     
     /** constant for keyword type Test Suite */
     public static final int TYPE_TEST_SUITE = 1;
     
     /** instance */
     private static TestresultSummaryBP instance = null;
     
     /** id of parent keyword*/
     private Long m_parentKeyWordId;
     
     /**
      * @param result The Test Result containing the source data.
      * @param summary The Test Result Summary to fill with data.
      */
     public void populateTestResultSummary(TestResult result,
             ITestResultSummary summary) {
         TestExecution te = TestExecution.getInstance();
         ITestSuitePO ts = te.getStartedTestSuite();
         IAUTMainPO startedAut = te.getConnectedAut();
        if (result.getAutConfigMap() != null) {
             String autConfigName = result.getAutConfigName();
             for (IAUTConfigPO conf : startedAut.getAutConfigSet()) {
                 if (conf.getValue(AutConfigConstants.CONFIG_NAME, "invalid") //$NON-NLS-1$
                         .equals(autConfigName)) {
                     summary.setInternalAutConfigGuid(conf.getGuid());
                     break;
                 }
             }
         } else {
             summary.setInternalAutConfigGuid(AUTRUN);
         }
         summary.setAutConfigName(result.getAutConfigName());
         summary.setAutCmdParameter(result.getAutArguments());
         summary.setAutId(te.getConnectedAutId().getExecutableName());
         
         summary.setAutOS(System.getProperty("os.name")); //$NON-NLS-1$
         IAUTMainPO aut = startedAut != null ? startedAut : ts.getAut();
         if (aut != null) {
             summary.setInternalAutGuid(aut.getGuid());
             summary.setAutName(aut.getName());
             summary.setAutToolkit(aut.getToolkit());
         }
         summary.setTestsuiteDate(new Date());
         summary.setInternalTestsuiteGuid(ts.getGuid());
         summary.setTestsuiteName(ts.getName());
         summary.setInternalProjectGuid(result.getProjectGuid());
         summary.setInternalProjectID(result.getProjectId());
         summary.setProjectName(result.getProjectName() + StringConstants.SPACE
                 + result.getProjectMajorVersion() + StringConstants.DOT
                 + result.getProjectMinorVersion());
         summary.setProjectMajorVersion(result.getProjectMajorVersion());
         summary.setProjectMinorVersion(result.getProjectMinorVersion());
         Date startTime = ClientTestFactory.getClientTest()
                 .getTestsuiteStartTime();
         summary.setTestsuiteStartTime(startTime);
         Date endTime = new Date();
         summary.setTestsuiteEndTime(endTime);
         summary.setTestsuiteDuration(getDurationString(startTime, endTime));
         summary.setTestsuiteExecutedTeststeps(te.getNumberOfTestedSteps());
         summary.setTestsuiteExpectedTeststeps(te.getExpectedNumberOfSteps());
         summary.setTestsuiteEventHandlerTeststeps(
                 te.getNumberOfEventHandlerSteps() 
                     + te.getNumberOfRetriedSteps());
         summary.setTestsuiteFailedTeststeps(te.getNumberOfFailedSteps());
         summary.setTestsuiteLanguage(te.getLocale().getDisplayName());
         summary.setTestsuiteRelevant(ClientTestFactory.getClientTest()
                         .isRelevant());
         ITestJobPO tj = te.getStartedTestJob();
         if (tj != null) {
             summary.setTestJobName(tj.getName());
             summary.setInternalTestJobGuid(tj.getGuid());
             summary.setTestJobStartTime(ClientTestFactory.getClientTest()
                     .getTestjobStartTime());
         }
         summary.setTestsuiteStatus(
                 result.getRootResultNode().getStatus());      
         //set default monitoring values.       
         summary.setInternalMonitoringId(
                 MonitoringConstants.EMPTY_MONITORING_ID); 
         summary.setReport(MonitoringConstants.EMPTY_REPORT);
         summary.setReportWritten(false);
         summary.setMonitoringValueType(MonitoringConstants.EMPTY_TYPE); 
     }
     
     /**
      * @param result The Test Result.
      * @param summaryId id of test result summary
      * @return session of test result details to persist in database
      */
     public EntityManager createTestResultDetailsSession(TestResult result,
             Long summaryId) {
         final EntityManager sess = Hibernator.instance().openSession();
         Hibernator.instance().getTransaction(sess);
         buildTestResultDetailsSession(
                 result.getRootResultNode(), sess, summaryId, 1, 1);
         return sess;
     }
     
     /**
      * Recursively build list of test result details to persist in database.
      * 
      * @param result TestResultNode
      * @param sess Session
      * @param summaryId id of testrun summary
      * @param nodeLevel "Indentation"-level of the node.
      * @param startingNodeSequence Initial sequence number for this section
      *                             of the Test Results.
      *                             
      * @return the continuation of the sequence number.
      */
     private int buildTestResultDetailsSession(TestResultNode result,
             EntityManager sess, Long summaryId, final int nodeLevel, 
             final int startingNodeSequence) {
         int nodeSequence = startingNodeSequence;
         TestResultNode resultNode = result;
         ITestResultPO keyword = PoMaker.createTestResultPO();
         fillNode(keyword, resultNode, sess);
         keyword.setKeywordLevel(nodeLevel);
         keyword.setKeywordSequence(nodeSequence);
         keyword.setInternalTestResultSummaryID(summaryId);
         keyword.setInternalParentKeywordID(m_parentKeyWordId);
         sess.persist(keyword);
         for (TestResultNode node : resultNode.getResultNodeList()) {
             m_parentKeyWordId = keyword.getId();
             nodeSequence = buildTestResultDetailsSession(node, sess, summaryId, 
                     nodeLevel + 1, nodeSequence + 1);
         }
         
         return nodeSequence;
     }
     
     /**
      * fill result node
      * @param keyword ITestResultPO
      * @param node ITestResultPO
      * @param sess the session to use (required e.g. BLOBs)
      */
     private void fillNode(ITestResultPO keyword, TestResultNode node, 
         EntityManager sess) {
         INodePO inode = node.getNode();
         keyword.setKeywordName(node.getNode().getName());
         keyword.setInternalKeywordGuid(inode.getGuid());
         keyword.setKeywordComment(inode.getComment());
         keyword.setInternalKeywordStatus(node.getStatus());
         keyword.setKeywordStatus(node.getStatusString());
         if (node.getTimeStamp() != null) {
             keyword.setTimestamp(node.getTimeStamp());
         }
         
         if (node.getParent() != null) {
             keyword.setInternalParentKeywordID(
                     node.getParent().getNode().getId());
         }
         
         if (inode instanceof ICapPO) {
             keyword.setInternalKeywordType(TYPE_TEST_STEP);
             keyword.setKeywordType("Test Step"); //$NON-NLS-1$
             
             //set component name, type and action name
             ICapPO cap = (ICapPO)inode;
             String compNameGuid = cap.getComponentName();
             keyword.setInternalComponentNameGuid(compNameGuid);
             keyword.setComponentName(
                     StringUtils.defaultString(node.getComponentName()));
             keyword.setInternalComponentType(cap.getComponentType());
             keyword.setComponentType(CompSystemI18n.getString(
                     cap.getComponentType()));
             keyword.setInternalActionName(cap.getActionName());
             keyword.setActionName(CompSystemI18n.getString(
                     cap.getActionName()));
             //set parameters
             addParameterListToResult(keyword, node, cap);
             //add error details
             addErrorDetails(keyword, node, sess);
         } else if (inode instanceof ITestCasePO) {
             keyword.setInternalKeywordType(TYPE_TEST_CASE);
             keyword.setKeywordType("Test Case"); //$NON-NLS-1$
         } else if (inode instanceof ITestSuitePO) {
             keyword.setInternalKeywordType(TYPE_TEST_SUITE);
             keyword.setKeywordType("Test Suite"); //$NON-NLS-1$
         }
     }
     
     /**
      * get a list of parameters for cap
      * @param node TestResultNode
      * @param cap ICapPO
      * @param keyword ITestResultPO
      * @return result mit parameter
      */
     private ITestResultPO addParameterListToResult(ITestResultPO keyword,
             TestResultNode node, ICapPO cap) {
         
         int index = 0;
         for (IParamDescriptionPO param : cap.getParameterList()) {
             IParameterDetailsPO parameter = PoMaker.createParameterDetailsPO();
             
             parameter.setParameterName(param.getName());
             parameter.setInternalParameterType(param.getType());
             parameter.setParameterType(CompSystemI18n.getString(
                     param.getType(), true));
             
             String paramValue = StringConstants.EMPTY;
             //parameter-value
             if (node.getParamValues().size() >= index + 1) {
                 final String value = node.getParamValues().get(index);
                 if (value != null) {
                     if (value.length() == 0) {
                         paramValue = TestDataConstants.EMPTY_SYMBOL;
                     } else {
                         paramValue = value;                        
                     }
                 }
             }
             parameter.setParameterValue(paramValue);
             keyword.addParameter(parameter);
             index++;
         }
         
         return keyword;
     }
     
     /**
      * add error details to test result element
      * @param keyword ITestResultPO
      * @param node TestResultNode
      * @param sess the session to use (required e.g. BLOBs)
      */
     private void addErrorDetails(ITestResultPO keyword, TestResultNode node, 
         EntityManager sess) {
         if (node.getStatus() == TestResultNode.ERROR 
                 || node.getStatus() == TestResultNode.RETRYING) {
             keyword.setStatusType(I18n.getString(node.getEvent().getId(),
                     true));
             
             Set keys = node.getEvent().getProps().keySet();
             if (node.getEvent().getId().equals(
                 TestErrorEvent.ID.IMPL_CLASS_ACTION_ERROR)) {
                 String key = (String)node.getEvent().getProps().get(
                     TestErrorEvent.Property.DESCRIPTION_KEY);
                 Object[] args = (Object[])node.getEvent().getProps().get(
                         TestErrorEvent.Property.PARAMETER_KEY);
                 //error description
                 if (key != null) {
                     keyword.setStatusDescription(String.valueOf(I18n.getString(
                             key, args)));
                 }
             } else {
                 for (Object key : keys) {
                     String value = String.valueOf(
                             node.getEvent().getProps().get(key));
                     if (key.equals(TestErrorEvent.Property.OPERATOR_KEY)) {
                         keyword.setStatusOperator(value);
                     }
                     if (key.equals(TestErrorEvent.Property.PATTERN_KEY)) {
                         keyword.setExpectedValue(value);
                     }
                     if (key.equals(TestErrorEvent.Property.ACTUAL_VALUE_KEY)) {
                         keyword.setActualValue(value);
                     }
                 }
             }
             if (node.getScreenshot() != null) {
                 keyword.setImage(node.getScreenshot());
             }
         }
     }
 
     /**
      * @return instance of TestresultSummaryBP
      */
     public static TestresultSummaryBP getInstance() {
         if (instance == null) {
             instance = new TestresultSummaryBP();
         }
         return instance;
     }
     
     /**
      * 
      * @param startTime The start time.
      * @param endTime The end time.
      * @return a String representation of the difference between the provided 
      *         times.
      */
     private String getDurationString(Date startTime, Date endTime) {
         long timeInSeconds = endTime.getTime() - startTime.getTime();
         timeInSeconds = timeInSeconds / 1000;
         long hours, minutes, seconds;
         hours = timeInSeconds / 3600;
         timeInSeconds = timeInSeconds - (hours * 3600);
         minutes = timeInSeconds / 60;
         timeInSeconds = timeInSeconds - (minutes * 60);
         seconds = timeInSeconds;
         String secondsString = (seconds < 10) ? "0" + seconds : String.valueOf(seconds); //$NON-NLS-1$ 
         String minutesString = (minutes < 10) ? "0" + minutes : String.valueOf(minutes); //$NON-NLS-1$ 
         String hoursString = (hours < 10) ? "0" + hours : String.valueOf(hours); //$NON-NLS-1$ 
         return hoursString + StringConstants.COLON + minutesString 
             + StringConstants.COLON + secondsString;
     }
 }

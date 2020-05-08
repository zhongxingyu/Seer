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
 package org.eclipse.jubula.client.core.model;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.persistence.Basic;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.MapKeyColumn;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.objects.IMonitoringValue;
 import org.eclipse.jubula.tools.objects.MonitoringValue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 /**
  * @author BREDEX GmbH
  * @created 22.01.2010
  */
 @Entity
 @Table(name = "TESTRESULT_SUMMARY")
 class TestResultSummaryPO implements ITestResultSummaryPO {
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(TestResultSummaryPO.class); 
     /** hibernate OID */
     private transient Long m_id = null;
     
     /** hibernate version id */
     private transient Integer m_version = null;
     
     /** Test job name */
     private String m_testJobName;
     
     /** used coverage tooling for this summary */
     private String m_monitoringId;
     /** used coverage tooling for this summary */
     private Map<String, MonitoringValuePO> m_monitoringValues;   
     /** the significant value */
     private String m_monitoringValue;
     
     /** internal Test job guid */
     private String m_testJobGuid;
     
     /** start time of test job */
     private Date m_testJobStartTime;
         
     /** Date of test run */
     private Date m_testsuiteDate;
     
     /** Start time */
     private Date m_testsuiteStartTime;
     
     /** end time */
     private Date m_testsuiteEndTime;
     
     /** duration */
     private String m_testsuiteDuration;
     
     /** project id */
     private Long m_projectID;
     
     /** project guid */
     private String m_projectGuid;
     
     /** project name */
     private String m_projectName;
     
     /** project Major Version */
     private int m_projectMajorVersion;
     
     /** project Minor Version */
     private int m_projectMinorVersion;
     
     /** expected caps */
     private int m_testsuiteExpectedTeststeps;
     
     /** executed caps */
     private int m_testsuiteExecutedTeststeps;
     
     /** eventhandler caps */
     private int m_testsuiteEventHandlerTeststeps;
     
     /** language */
     private String m_testsuiteLanguage;
     
     /** Ts name */
     private String m_testsuiteName;
     
     /** Ts guid */
     private String m_testsuiteGuid;
     
     /** Ts status */
     private int m_testsuiteStatus;
     
     /** AUT name */
     private String m_autName;
 
     /** AUT ID */
     private String m_autId;
     
     /** AUT Guid */
     private String m_autGuid;
     
     /** AUT conf */
     private String m_autConfigName;
     
     /** AUT conf guid */
     private String m_autConfigGuid;
     
     /** AUT server */
     private String m_autAgentName;
     
     /** AUT hostname */
     private String m_autHostname;
 
     /** AUT OS */
     private String m_autOS;
 
     /** cmd param */
     private String m_autCmdParameter;
     
     /** information about the used Toolkit */
     private String m_autToolkit = null;
     
     /** true if testrun is relevant, false otherwise */
     private boolean m_testsuiteRelevant = true;
 
     /** <code>m_testsuiteFailedTeststeps</code> numer of failed test steps */
     private int m_testsuiteFailedTeststeps = 
         DEFAULT_NUMBER_OF_FAILED_TEST_STEPS;
 
     /** the GUID */
     private String m_guid;
     
     /** comment title */
     private String m_commentTitle;
     
     /** comment detail */
     private String m_commentDetail;
     /** Monitoring-Report */
     private byte[] m_monitoringReport;
     /** true if blob was written, false otherwise */
     private boolean m_blobWritten = false;
     /** monitoring value typ */
     private String m_monitoringValueType;
     /**
      * only for hibernate
      */
     @SuppressWarnings("unused")
     private TestResultSummaryPO() {
         //default
     }
 
     /**
      * Constructor
      * 
      * @param guid The GUID for the created object.
      */
     TestResultSummaryPO(String guid) {
         setInternalGuid(guid);
     }
     
     /**
      * only for hibernate
      * @return Returns the id.
      */
     @Id
     @GeneratedValue
     @Column(name = "ID")
     public Long getId()  {
         return m_id;
     }
     /**
      * only for hibernate
      * @param id The id to set.
      */
     void setId(Long id) {
         m_id = id;
     }
     
     /** 
      * {@inheritDoc}
      */
     @Version
     @Column(name = "INTERNAL_VERSION")
     public Integer getVersion() {        
         return m_version;
     }
 
     /**
      * @param version version
      */
     @SuppressWarnings("unused")
     private void setVersion(Integer version) {
         m_version = version;
     }
     
     /**
      * @return the testJobName
      */
     @Basic()
     @Column(name = "TEST_JOB_NAME", length = 4000)
     public String getTestJobName() {
         return m_testJobName;
     }
 
     /**
      * @param testJobName the testJobName to set
      */
     public void setTestJobName(String testJobName) {
         m_testJobName = testJobName;
     }
 
     /**
      * @return the testJobGuid
      */
     @Basic
     @Column(name = "INTERNAL_TEST_JOB_GUID", length = 32)
     public String getInternalTestJobGuid() {
         return m_testJobGuid;
     }
 
     /**
      * @param testJobGuid the testJobGuid to set
      */
     public void setInternalTestJobGuid(String testJobGuid) {
         m_testJobGuid = testJobGuid;
     }
 
     /**
      * @return the testJobStartTime
      */
     @Basic
     @Column(name = "TEST_JOB_START_TIME")
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date getTestJobStartTime() {
         return m_testJobStartTime;
     }
 
     /**
      * @param testJobStartTime the testJobStartTime to set
      */
     public void setTestJobStartTime(Date testJobStartTime) {
         m_testJobStartTime = testJobStartTime;
     }
 
     /**
      * @param projectID the projectID to set
      */
     public void setInternalProjectID(Long projectID) {
         m_projectID = projectID;
     }
 
     /**
      * @return the projectID
      */
     @Basic
     @Column(name = "INTERNAL_PROJECT_ID")
     public Long getInternalProjectID() {
         return m_projectID;
     }
     
     /**
      * @param projectName the projectName to set
      */
     public void setProjectName(String projectName) {
         m_projectName = projectName;
     }
 
     /**
      * @return the projectName
      */
     @Basic
     @Column(name = "PROJECT_NAME", length = 4000)
     public String getProjectName() {
         return m_projectName;
     }
 
     /**
      * @return projectMajorVersion
      */
     @Basic
     @Column(name = "PROJECT_MAJOR_VERSION", nullable = false)
     public int getProjectMajorVersion() {
         return m_projectMajorVersion;
     }
 
     /**
      * @param projectMajorVersion the projectMajorVersion to set
      */
     public void setProjectMajorVersion(int projectMajorVersion) {
         m_projectMajorVersion = projectMajorVersion;
     }
     
     
     /**
      * @return the ProjectMinorVersion
      */
     @Basic
     @Column(name = "PROJECT_MINOR_VERSION", nullable = false)
     public int getProjectMinorVersion() {
         return m_projectMinorVersion;
     }
 
     /**
      * @param projectMinorVersion the projectMinorVersion to set
      */
     public void setProjectMinorVersion(int projectMinorVersion) {
         m_projectMinorVersion = projectMinorVersion;
     }
 
     /**
      * @param projectGuid the projectGuid to set
      */
     public void setInternalProjectGuid(String projectGuid) {
         m_projectGuid = projectGuid;
     }
 
     /**
      * @return the projectGuid
      */
     @Basic
     @Column(name = "INTERNAL_PROJECT_GUID", length = 32)
     public String getInternalProjectGuid() {
         return m_projectGuid;
     }
     
     /**
      * @param tsName the tsName to set
      */
     public void setTestsuiteName(String tsName) {
         m_testsuiteName = tsName;
     }
 
     /**
      * @return the tsName
      */
     @Basic
     @Column(name = "TS_NAME", length = 4000)
     public String getTestsuiteName() {
         return m_testsuiteName;
     }
 
     /**
      * @param tsGuid the tsGuid to set
      */
     public void setInternalTestsuiteGuid(String tsGuid) {
         m_testsuiteGuid = tsGuid;
     }
 
     /**
      * @return the tsGuid
      */
     @Basic
     @Column(name = "INTERNAL_TESTSUITE_GUID", length = 32)
     public String getInternalTestsuiteGuid() {
         return m_testsuiteGuid;
     }
     
     /**
      * @param tsStatus the tsStatus to set
      */
     public void setTestsuiteStatus(int tsStatus) {
         m_testsuiteStatus = tsStatus;
     }
 
     /**
      * @return the tsStatus
      */
     @Basic
     @Column(name = "TS_STATUS", nullable = false)
     public int getTestsuiteStatus() {
         return m_testsuiteStatus;
     }
     
     /**
      * @return Returns the autName.
      */
     @Basic
     @Column(name = "AUT_NAME", length = 4000)
     public String getAutName() {
         return m_autName;
     }
 
     /**
      * @param autName
      *            The autName to set.
      */
     public void setAutName(String autName) {
         m_autName = autName;
     }
     
     /**
      * @return Returns the autId.
      */
     @Basic
     @Column(name = "AUT_ID", length = 4000)
     public String getAutId() {
         return m_autId;
     }
 
     /**
      * @param autId the autId to set
      */
     public void setAutId(String autId) {
         m_autId = autId;
     }
     
     /**
      * @return Returns the autGuid.
      */
     @Basic
     @Column(name = "INTERNAL_AUT_GUID", length = 32)
     public String getInternalAutGuid() {
         return m_autGuid;
     }
 
     /**
      * @param autGuid
      *            The autGuid to set.
      */
     public void setInternalAutGuid(String autGuid) {
         m_autGuid = autGuid;
     }
     
     /**
      * @param autConf the autConf to set
      */
     public void setAutConfigName(String autConf) {
         m_autConfigName = autConf;
     }
 
     /**
      * @return the autConf
      */
     @Basic
     @Column(name = "AUT_CONFIG_NAME", length = 4000)
     public String getAutConfigName() {
         return m_autConfigName;
     }
     
     /**
      * @param autConfGuid the autConfGuid to set
      */
     public void setInternalAutConfigGuid(String autConfGuid) {
         m_autConfigGuid = autConfGuid;
     }
 
     /**
      * @return the autConfGuid
      */
     @Basic
     @Column(name = "INTERNAL_AUT_CONFIG_GUID", length = 32)
     public String getInternalAutConfigGuid() {
         return m_autConfigGuid;
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "INTERNAL_GUID", length = 32, unique = true)
     public String getInternalGuid() {
         return m_guid;
     }
 
     /**
      * 
      * @param guid The GUID to set.
      */
     private void setInternalGuid(String guid) {
         m_guid = guid;
     }
     
     /**
      * @param autServer the autServer to set
      */
     public void setAutAgentName(String autServer) {
         m_autAgentName = autServer;
     }
 
     /**
      * @return the autagent name
      */
     @Basic
     @Column(name = "AUT_AGENT_NAME", length = 4000)
     public String getAutAgentName() {
         return m_autAgentName;
     }
     
     
     /**
      * @return the autHostname
      */
     @Basic
     @Column(name = "AUT_HOSTNAME", length = 4000)
     public String getAutHostname() {
         return m_autHostname;
     }
 
     /**
      * @param autHostname the autHostname to set
      */
     public void setAutHostname(String autHostname) {
         m_autHostname = autHostname;
     }
     
     
     /**
      * @return the autOS
      */
     @Basic
     @Column(name = "AUT_OS", length = 4000)
     public String getAutOS() {
         return m_autOS;
     }
 
     /**
      * @param autOS the autOS to set
      */
     public void setAutOS(String autOS) {
         m_autOS = autOS;
     }
     
     /**
      * @param language the language to set
      */
     public void setTestsuiteLanguage(String language) {
         m_testsuiteLanguage = language;
     }
 
     /**
      * @return the language
      */
     @Basic
     @Column(name = "TS_LANGUAGE")    
     public String getTestsuiteLanguage() {
         return m_testsuiteLanguage;
     }
 
     /**
      * only for hibernate !!!
      * @return the toolkit
      */
     @Basic
     @Column(name = "AUT_TOOLKIT")
     private String getHbmToolkit() {
         return m_autToolkit;
     }
     
     /**
      * {@inheritDoc}
      */
     @Transient
     public String getAutToolkit() {
         return getHbmToolkit();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setAutToolkit(String toolkit) {
         setHbmToolkit(toolkit);
     }
     
     /**
      * @param toolkit the toolkit
      */
     private void setHbmToolkit(String toolkit) {
         m_autToolkit = toolkit;
     }
 
     /**
      * @param testrunDate the date to set
      */
     public void setTestsuiteDate(Date testrunDate) {
         m_testsuiteDate = testrunDate;
     }
 
     /**
      * @return the date
      */
     @Basic
     @Column(name = "TS_DATE")
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date getTestsuiteDate() {
         return m_testsuiteDate;
     }
 
     /**
      * @param startTime the startTime to set
      */
     public void setTestsuiteStartTime(Date startTime) {
         m_testsuiteStartTime = startTime;
     }
 
     /**
      * @return the startTime
      */
     @Basic
     @Column(name = "TS_START_TIME")
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date getTestsuiteStartTime() {
         return m_testsuiteStartTime;
     }
 
     /**
      * @param endTime the endTime to set
      */
     public void setTestsuiteEndTime(Date endTime) {
         m_testsuiteEndTime = endTime;
     }
 
     /**
      * @return the endTime
      */
     @Basic
     @Column(name = "TS_END_TIME")
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date getTestsuiteEndTime() {
         return m_testsuiteEndTime;
     }
 
     /**
      * @param duration the duration to set
      */
     public void setTestsuiteDuration(String duration) {
         m_testsuiteDuration = duration;
     }
 
     /**
      * @return the duration
      */
     @Basic
     @Column(name = "TS_DURATION", length = 4000)    
     public String getTestsuiteDuration() {
         return m_testsuiteDuration;
     }
 
     /**
      * @param expecCaps the expecCaps to set
      */
     public void setTestsuiteExpectedTeststeps(int expecCaps) {
         m_testsuiteExpectedTeststeps = expecCaps;
     }
 
     /**
      * @return the expecCaps
      */
     @Basic
     @Column(name = "TS_EXPECTED_TESTSTEPS", nullable = false)
     public int getTestsuiteExpectedTeststeps() {
         return m_testsuiteExpectedTeststeps;
     }
 
     /**
      * @param execCaps the execCaps to set
      */
     public void setTestsuiteExecutedTeststeps(int execCaps) {
         m_testsuiteExecutedTeststeps = execCaps;
     }
 
     /**
      * @return the execCaps
      */
     @Basic
     @Column(name = "TS_EXECUTED_TESTSTEPS", nullable = false)
     public int getTestsuiteExecutedTeststeps() {
         return m_testsuiteExecutedTeststeps;
     }
     
     /**
      * @param handlerCaps the eventhandler caps to set
      */
     public void setTestsuiteEventHandlerTeststeps(int handlerCaps) {
         m_testsuiteEventHandlerTeststeps = handlerCaps;
     }
 
     /**
      * @return the eventhandler caps
      */
     @Basic
     @Column(name = "TS_EVENTHANDLER_TESTSTEPS", nullable = false)
     public int getTestsuiteEventHandlerTeststeps() {
         return m_testsuiteEventHandlerTeststeps;
     }
 
     /**
      * @param cmdParam the cmdParam to set
      */
     public void setAutCmdParameter(String cmdParam) {
         m_autCmdParameter = cmdParam;
     }
 
     /**
      * @return the cmdParam
      */
     @Basic
     @Column(name = "AUT_CMD_PARAMETER", length = 400)
     public String getAutCmdParameter() {
         return m_autCmdParameter;
     }
     
     
     /**
      * @return true, if testrun is relevant, false otherwise
      */
     @Basic
     @Column(name = "TS_RELEVANT", nullable = false)
     public boolean isTestsuiteRelevant() {
         return m_testsuiteRelevant;
     }
 
     /**
      * @param relevant the relevant flag to set
      */
     public void setTestsuiteRelevant(boolean relevant) {
         m_testsuiteRelevant = relevant;
     }
     
 
     /**
      * {@inheritDoc}
      */
     public String toString() {
         return super.toString() + StringConstants.SPACE 
             + StringConstants.LEFT_PARENTHESES + m_id.toString()
             + StringConstants.RIGHT_PARENTHESES;
     }
     
     /**
      * {@inheritDoc}
      * @return if there is a database ID it returns true if the ID is equal.
      * If there is no ID it will be compared to identity.
      */
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof TestResultSummaryPO 
                 || obj instanceof ITestResultSummaryPO)) {
             return false;
         }
         ITestResultSummaryPO o = (ITestResultSummaryPO)obj;
         return getInternalGuid().equals(o.getInternalGuid());
     }
 
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         return getInternalGuid().hashCode();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public String getTestRunState() {
         switch (getTestsuiteStatus()) {
             case TestResultNode.NOT_YET_TESTED:
                 break;
             case TestResultNode.NO_VERIFY:
                 return STATE_OK;
             case TestResultNode.TESTING:
                 return STATE_STOPPED;
             case TestResultNode.SUCCESS:
                 return STATE_OK;
             case TestResultNode.ERROR:
                 return STATE_FAILED;
             case TestResultNode.ERROR_IN_CHILD:
                 return STATE_FAILED;
             case TestResultNode.NOT_TESTED:
                 return STATE_FAILED;
             case TestResultNode.RETRYING:
                 return STATE_STOPPED;
             case TestResultNode.SUCCESS_RETRY:
                 return STATE_OK;
             case TestResultNode.ABORT:
                 return STATE_FAILED;
             default:
                 return null;
         }
         return null;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public String getStatusString() {
         switch (getTestsuiteStatus()) {
             case TestResultNode.ERROR:
                 return Messages.TestResultNodeStepfailed;
             case TestResultNode.ERROR_IN_CHILD:
                 return Messages.TestResultNodeErrorInChildren;
             case TestResultNode.NOT_YET_TESTED:
                 return Messages.TestResultNodeNotYetTested;
             case TestResultNode.SUCCESS:
                 return Messages.TestResultNodeSuccessfullyTested;
             case TestResultNode.TESTING:
                 return Messages.TestResultNodeTesting;
             case TestResultNode.RETRYING:
                 return Messages.TestResultNodeRetrying;
             case TestResultNode.SUCCESS_RETRY:
                 return Messages.TestResultNodeSuccessRetry;
             case TestResultNode.ABORT:
                 return Messages.TestResultNodeAbort;
             default:
                 break;
         }
         return Messages.TestResultNodeUnknown;
     }
 
     /**
      * @return the number of failed test steps
      */
     @Basic
     @Column(name = "TS_FAILED_TESTSTEPS", nullable = false)
     public int getTestsuiteFailedTeststeps() {
         return m_testsuiteFailedTeststeps;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setTestsuiteFailedTeststeps(int failedCaps) {
         m_testsuiteFailedTeststeps = failedCaps;
     }
     
     /**
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "COMMENT_TITLE", length = 4000)
     public String getCommentTitle() {
         return m_commentTitle;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setCommentTitle(String commentTitle) {
         m_commentTitle = commentTitle;
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "COMMENT_DETAIL", length = 4000)
     public String getCommentDetail() {
         return m_commentDetail;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setCommentDetail(String commentDetail) {
         m_commentDetail = commentDetail;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setInternalMonitoringId(String monitoringId) {
         m_monitoringId = monitoringId;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "INTERNAL_MONITORING_ID", length = 50)
     public String getInternalMonitoringId() {
         return m_monitoringId;
     }
 
     /**
      * @param monitoringValue the monitoringValue to set
      */
    
     private void setHbmMonitoringValues(
             Map<String, MonitoringValuePO> monitoringValue) {
         m_monitoringValues = monitoringValue;
     }
 
     /**
      * 
      * @return returns the stored monitored values
      */
     @ElementCollection(fetch = FetchType.EAGER)    
     @CollectionTable(name = "MONITORING_VALUE")
    @MapKeyColumn(name = "KEY")
     private Map<String, MonitoringValuePO> getHbmMonitoringValues() {
         return m_monitoringValues;
     } 
     /**
      * {@inheritDoc}
      */
     @Transient
     public Map<String, IMonitoringValue> getMonitoringValues() {
       
         Map<String, MonitoringValuePO> tmpMapPO = getHbmMonitoringValues();
         Map<String, IMonitoringValue> tmpMap = 
             new HashMap<String, IMonitoringValue>();
         Iterator it = tmpMapPO.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry)it.next();           
             MonitoringValuePO tmp = (MonitoringValuePO)pairs.getValue(); 
             tmpMap.put((String)pairs.getKey(), 
                     new MonitoringValue(tmp.getValue(), tmp.getType(), 
                             tmp.getCategory(), tmp.isSignificant()));
         }                
         return tmpMap;
     }
     /**
      * {@inheritDoc}
      */
     @Transient
     public void setMonitoringValues(Map<String, IMonitoringValue> map) {
                 
         Map<String, MonitoringValuePO> tmpMap = 
             new HashMap<String, MonitoringValuePO>();
         
         Iterator it = map.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry)it.next();           
             MonitoringValue tmp = (MonitoringValue)pairs.getValue();         
             tmpMap.put((String)pairs.getKey(), 
                     new MonitoringValuePO(tmp.getValue(), tmp.getType(), 
                             tmp.getCategory(), tmp.isSignificant()));
         }        
         setHbmMonitoringValues(tmpMap);
     }
     
     /**
      * @return the report as byte[]
      */
     @Basic
     @Lob
     @Column(name = "MONITORING_REPORT")
     public byte[] getReport() {
         return m_monitoringReport;
     }
 
     /**
      * @param report the report to set
      */
     public void setReport(byte[] report) {
         m_monitoringReport = report;
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "M_REPORT_WRITTEN", nullable = false)
     public boolean isReportWritten() {
         
         return m_blobWritten;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setReportWritten(boolean isWritten) {
         
         this.m_blobWritten = isWritten;
     }
 
     /**
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "M_VALUE_TYPE", length = 30)
     public String getMonitoringValueType() {
         
         return m_monitoringValueType;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setMonitoringValueType(String type) {
         
         this.m_monitoringValueType = type;
         
     }
     /**
      * {@inheritDoc}
      */    
     @Basic
     @Column(name = "MONITORING_VALUE")
     public String getMonitoringValue() {
         
         return m_monitoringValue;
         
     }
     /**
      * {@inheritDoc}
      */
     public void setMonitoringValue(String monitoringValue) {
         
         this.m_monitoringValue = monitoringValue;
     }
 
     
     
 }

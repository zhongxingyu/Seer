 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. 
  * See http://www.eu-egee.org/partners/ for details on the copyright
  * holders.  
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
  
 package org.glite.ce.creamapi.jobmanagement.cmdexecutor;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 import org.glite.ce.commonj.db.DatabaseException;
 import org.glite.ce.creamapi.cmdmanagement.AbstractCommandExecutor;
 import org.glite.ce.creamapi.cmdmanagement.Command;
 import org.glite.ce.creamapi.cmdmanagement.CommandException;
 import org.glite.ce.creamapi.cmdmanagement.CommandExecutorException;
 import org.glite.ce.creamapi.cmdmanagement.CommandManagerException;
 import org.glite.ce.creamapi.cmdmanagement.CommandResult;
 import org.glite.ce.creamapi.eventmanagement.Event;
 import org.glite.ce.creamapi.eventmanagement.EventManagerException;
 import org.glite.ce.creamapi.eventmanagement.EventManagerFactory;
 import org.glite.ce.creamapi.jobmanagement.Job;
 import org.glite.ce.creamapi.jobmanagement.JobEnumeration;
 import org.glite.ce.creamapi.jobmanagement.JobManagementException;
 import org.glite.ce.creamapi.jobmanagement.JobStatus;
 import org.glite.ce.creamapi.jobmanagement.JobStatusChangeListener;
 import org.glite.ce.creamapi.jobmanagement.JobWrapper;
 import org.glite.ce.creamapi.jobmanagement.Lease;
 import org.glite.ce.creamapi.jobmanagement.command.JobCommand;
 import org.glite.ce.creamapi.jobmanagement.command.JobIdFilterFailure;
 import org.glite.ce.creamapi.jobmanagement.command.JobIdFilterResult;
 import org.glite.ce.creamapi.jobmanagement.db.JobDBInterface;
 import org.glite.ce.creamapi.jobmanagement.jdl.JobFactory;
 import org.glite.ce.creamapi.jobmanagement.lb.LBLogger;
 
 public abstract class AbstractJobExecutor extends AbstractCommandExecutor implements JobStatusChangeListener {
     private final static Logger logger = Logger.getLogger(AbstractJobExecutor.class.getName());
     /** Labels for JobWrapper */
     
     public final static String JOB_WRAPPER_DELEGATION_TIME_SLOT      = "JOB_WRAPPER_DELEGATION_TIME_SLOT";
     public final static String JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT = "JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT";
     public final static String JOB_WRAPPER_COPY_RETRY_COUNT_ISB      = "JOB_WRAPPER_COPY_RETRY_COUNT_ISB";
     public final static String JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB = "JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB";
     public final static String JOB_WRAPPER_COPY_RETRY_COUNT_OSB      = "JOB_WRAPPER_COPY_RETRY_COUNT_OSB";
     public final static String JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB = "JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB";
 
     public final static String DELEGATION_PROXY_CERT_SANDBOX_URI     = "DELEGATION_PROXY_CERT_SANDBOX_URI";
     public final static String SANDBOX_TRANSFER_METHOD               = "SANDBOX_TRANSFER_METHOD";
     public final static String GSIFTP_SANDBOX_TRANSFER_METHOD        = "GSIFTP";
     public final static String LRMS_SANDBOX_TRANSFER_METHOD          = "LRMS";
     public final static String LRMS_INPUT_FILES                      = "LRMS_INPUT_FILES";
     public final static String LRMS_OUTPUT_FILES                     = "LRMS_OUTPUT_FILES";
     public final static String JOB_WRAPPER_TEMPLATE_PATH             = "JOB_WRAPPER_TEMPLATE_PATH";
 
     /** Label for Cream job sensor host */
     public final static String CREAM_JOB_SENSOR_HOST = "CREAM_JOB_SENSOR_HOST";
 
     /** Label for Cream job sensor port */
     public final static String CREAM_JOB_SENSOR_PORT = "CREAM_JOB_SENSOR_PORT";
     
     /** Label for Cream job submission manager enable property */
     public final static String JOB_SUBMISSION_MANAGER_ENABLE ="JOB_SUBMISSION_MANAGER_ENABLE";
     
     /** Label for Cream job submission manager script path property */
     public final static String JOB_SUBMISSION_MANAGER_SCRIPT_PATH = "JOB_SUBMISSION_MANAGER_SCRIPT_PATH";
     
     /** Default value for JOB_PURGE_RATE property. */
     private static int JOB_PURGE_RATE_DEFAULT = 300; // minutes.
     
     private static Timer timer = null;
     
     private JobDBInterface jobDB;
     private LeaseManager leaseManager;
     private JobSubmissionManager jobSubmissionManager;
     private JobPurger jobPurger = null;
     private Socket socket = null;
     private ObjectOutputStream oos = null;
     
 
     protected AbstractJobExecutor(String name) throws CommandExecutorException {
         super(name, JobCommandConstant.JOB_MANAGEMENT);
         
         setCommands(JobCommandConstant.commandNameList);
         
         if (timer == null) {
             timer = new Timer("TIMER", true);
         }
     }
 
     public void initExecutor() throws CommandExecutorException {
         String cream_sandbox_dir = getParameterValueAsString("CREAM_SANDBOX_DIR");
         if (cream_sandbox_dir == null) {
             throw new CommandExecutorException("parameter \"CREAM_SANDBOX_DIR\" not defined!");
         }
         String sensorHost = getParameterValueAsString(CREAM_JOB_SENSOR_HOST);
         if (sensorHost == null) {
             logger.warn("CREAM_JOB_SENSOR_HOST parameter not specified!");
         }
         File dir = new File(cream_sandbox_dir);
 
         try {
             if (!dir.exists()) {
                 dir.mkdirs();
             }
             makeVODir(dir);
         } catch (IOException ioe) {
             throw new CommandExecutorException(ioe.getMessage());
         } finally {
             dir = null;
         }
 
         String lbAddress = getParameterValueAsString("LB_DEFAULT_ADDRESS");
         String lbILPrefix = getParameterValueAsString("LB_IL_PREFIX");
 
         if (lbAddress == null) {
             logger.warn("parameter \"LB_DEFAULT_ADDRESS\" not defined, LBLogger disabled");
         } else {
             try { 
                 URI lbdefault = new URI(lbAddress);
                 LBLogger.setDefaultLBURI(lbdefault);
                 LBLogger.setILPrefix(lbILPrefix);
                 LBLogger.getInstance();
             } catch (Throwable t) {
                 logger.error("LBLogger initialization failed: " + t.getMessage(), t );
             }
         }
     }
 
     public void destroy() {
         logger.info("destroy invoked!");
 
         super.destroy();
         
         if (socket != null && !socket.isClosed()) {
             try {
                 socket.close();
             } catch (IOException e1) {
             }
         }
         socket = null;
 
         if (oos != null) {
             try {
                 oos.close();
             } catch (IOException e1) {
             }
         }
 
         if (jobPurger != null) {
             jobPurger.terminate();
         }
 
         timer.cancel();
         timer.purge();
         timer = null;
         
         jobDB = null;
 
         logger.info("destroyed!");
     }
 
     public Job retrieveJob(String jobId) throws CommandException {
         logger.debug("Begin retrieveJob for jobId = " + jobId);
         Job job = null;
         try {
             job = jobDB.retrieveJob(jobId, null);
         } catch (Exception e) {
             throw new CommandException(e.getMessage());
         }
                 
         logger.debug("End retrieveJob for jobId = " + jobId);
         return job;
     }
 
     public void updateJob(Job job) throws CommandException {
         try {
             jobDB.update(job);
         } catch (Exception e) {
             throw new CommandException(e.getMessage());
         }
     }
 
     private JobEnumeration getJobList(final Command cmd) throws IllegalArgumentException, CommandException {
         logger.debug("Begin getJobList");
         if (cmd == null) {
             throw new IllegalArgumentException("cmd not defined!");
         }
 
         List<Integer> compatibleStatusList = new ArrayList<Integer>(0);
 
         int cmdType = getCommandType(cmd.getName());
 
         if (JobCommandConstant.JOB_START.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.REGISTERED);
 
         } else if (JobCommandConstant.JOB_CANCEL.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.REGISTERED);
             compatibleStatusList.add(JobStatus.IDLE);
             compatibleStatusList.add(JobStatus.HELD);
             compatibleStatusList.add(JobStatus.REALLY_RUNNING);
             compatibleStatusList.add(JobStatus.RUNNING);
 
         } else if (JobCommandConstant.JOB_SET_LEASEID.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.REGISTERED);
             compatibleStatusList.add(JobStatus.PENDING);
             compatibleStatusList.add(JobStatus.IDLE);
             compatibleStatusList.add(JobStatus.HELD);
             compatibleStatusList.add(JobStatus.REALLY_RUNNING);
             compatibleStatusList.add(JobStatus.RUNNING);
 
         } else if (JobCommandConstant.JOB_PURGE.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.ABORTED);
             compatibleStatusList.add(JobStatus.REGISTERED);
             compatibleStatusList.add(JobStatus.CANCELLED);
             compatibleStatusList.add(JobStatus.DONE_OK);
             compatibleStatusList.add(JobStatus.DONE_FAILED);
 
         } else if (JobCommandConstant.JOB_RESUME.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.HELD);
  
         } else if (JobCommandConstant.JOB_SUSPEND.equals(cmd.getName())) {
             compatibleStatusList.add(JobStatus.IDLE);
             compatibleStatusList.add(JobStatus.REALLY_RUNNING);
             compatibleStatusList.add(JobStatus.RUNNING);
 
 //        case JobCommandConstant.UPDATE_PROXY_TO_SANDBOX.equals(cmd.getName())) {
 //            compatibleStatusList.add(JobStatus.PENDING);
 //            compatibleStatusList.add(JobStatus.IDLE);
 //            compatibleStatusList.add(JobStatus.HELD);
 //            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
 //            compatibleStatusList.add(JobStatus.RUNNING);
 //            break;
         } else {
             compatibleStatusList.add(JobStatus.REGISTERED);
             compatibleStatusList.add(JobStatus.PENDING);
             compatibleStatusList.add(JobStatus.IDLE);
             compatibleStatusList.add(JobStatus.RUNNING);
             compatibleStatusList.add(JobStatus.REALLY_RUNNING);
             compatibleStatusList.add(JobStatus.CANCELLED);
             compatibleStatusList.add(JobStatus.HELD);
             compatibleStatusList.add(JobStatus.DONE_OK);
             compatibleStatusList.add(JobStatus.DONE_FAILED);
             compatibleStatusList.add(JobStatus.PURGED);
             compatibleStatusList.add(JobStatus.ABORTED);
         }
 
         String userId = null;
         
         if (cmd.getParameterAsString("IS_ADMIN") == null || cmd.getParameterAsString("IS_ADMIN").equalsIgnoreCase("false")) {
             userId = cmd.getUserId();
         }
 
         // Retrieving jobFilter parameters.
         List<String> jobList    = null;
         Calendar fromDate       = null;
         Calendar toDate         = null;
         Calendar fromStatusDate = null;
         Calendar toStatusDate   = null;
         String leaseId          = null;
         String delegationId     = null;
 
         // jobId list parameter
         if (cmd.containsParameterKey("JOB_ID")) {
             jobList = new ArrayList<String>(0);
             jobList.add(cmd.getParameterAsString("JOB_ID"));
         } else if (cmd.containsParameterKey("JOB_ID_LIST")) {
             jobList = cmd.getParameterMultivalue("JOB_ID_LIST");
         }
 
         // fromDate parameter
         fromDate = makeDate(cmd.getParameterAsString("FROM_DATE"));
         // toDate parameter
         toDate = makeDate(cmd.getParameterAsString("TO_DATE"));
         // leaseId parameter
         leaseId = cmd.getParameterAsString("LEASE_ID");
         // delegationId parameter
         delegationId = cmd.getParameterAsString("DELEGATION_PROXY_ID");
 
         // fromStatusDate parameter
         fromStatusDate = makeDate(cmd.getParameterAsString("FROM_STATUS_DATE"));
         // toStatusDate parameter
         toStatusDate = makeDate(cmd.getParameterAsString("TO_STATUS_DATE"));
 
         // statusList parameter
         List<String> statusList = cmd.getParameterMultivalue("JOB_STATUS_LIST");
 
         if (statusList == null) {
             logger.debug("statusList is null!");
         }
         if ((statusList != null) && (statusList.size() == 0)) {
             logger.debug("statusList is empty!");
         }
 
         List<Integer> compatibleStatusToFindList = new ArrayList<Integer>(0);
 
         if (statusList != null) {
             for (int i = 0; i < statusList.size(); i++) {
                 for (int x = 0; x < JobStatus.statusName.length; x++) {
                     if (compatibleStatusList.contains(x) && statusList.get(i).equals(JobStatus.getNameByType(x))) {
                         compatibleStatusToFindList.add(x);
                         continue;
                     }
                 }
             }
         } else {
             compatibleStatusToFindList = compatibleStatusList;
         }
         
         if ((compatibleStatusToFindList == null) || (compatibleStatusToFindList.size() == 0)) {
             logger.error("Status specified not compatible for the request command.");
             throw new CommandException("Status specified not compatible for the request command.");
         }
 
         int[] compatibleStatusToFind = new int[compatibleStatusToFindList.size()];
         for (int i = 0; i < compatibleStatusToFindList.size(); i++) {
             compatibleStatusToFind[i] = compatibleStatusToFindList.get(i);
         }
 
         List<String> jobIdFound = null;
         List<JobIdFilterResult> jobIdFilterResultList = new ArrayList<JobIdFilterResult>(0);
         JobIdFilterResult jobIdFilterResult = null;
         List<String> helpJobList = new ArrayList<String>(0);
 
         try {
             if ((jobList != null) && (jobList.size() > 0)) {
                 logger.debug("JobList parameter is not null in jobFilter.");
                 // find job not existing.
                 helpJobList.addAll(jobList);
                 jobIdFound = jobDB.retrieveJobId(jobList, userId);
 
                 helpJobList.removeAll(jobIdFound);
                 for (int i = 0; i < helpJobList.size(); i++) {
                     jobIdFilterResult = new JobIdFilterResult();
                     jobIdFilterResult.setJobId(helpJobList.get(i));
                     jobIdFilterResult.setErrorCode(JobIdFilterFailure.JOBID_ERRORCODE);
                     jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.JOBID_ERRORCODE]);
                     jobIdFilterResultList.add(jobIdFilterResult);
                     logger.debug("JobId = " + helpJobList.get(i) + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.JOBID_ERRORCODE]);
                 }
                 
                 helpJobList.clear();
                 helpJobList.addAll(jobIdFound);
 
                 // find job by status.
                 if (jobIdFound != null && jobIdFound.size() > 0) {
                     jobIdFound = jobDB.retrieveJobId(jobIdFound, userId, compatibleStatusToFind, fromStatusDate, toStatusDate);
                     
                     helpJobList.removeAll(jobIdFound);
                     for (int i = 0; i < helpJobList.size(); i++) {
                         jobIdFilterResult = new JobIdFilterResult();
                         jobIdFilterResult.setJobId(helpJobList.get(i));
                         jobIdFilterResult.setErrorCode(JobIdFilterFailure.STATUS_ERRORCODE);
                         jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.STATUS_ERRORCODE]);
                         jobIdFilterResultList.add(jobIdFilterResult);
                         logger.debug("JobId = " + helpJobList.get(i) + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.STATUS_ERRORCODE]);
                     }
                     helpJobList.clear();
                     helpJobList.addAll(jobIdFound);
                 }
 
                 // find job by date.
                 if ((jobIdFound != null) && jobIdFound.size() > 0 && (fromDate != null || toDate != null)) {
                     jobIdFound = jobDB.retrieveByDate(jobIdFound, userId, fromDate, toDate);
                     helpJobList.removeAll(jobIdFound);
                     for (int i = 0; i < helpJobList.size(); i++) {
                         jobIdFilterResult = new JobIdFilterResult();
                         jobIdFilterResult.setJobId(helpJobList.get(i));
                         jobIdFilterResult.setErrorCode(JobIdFilterFailure.DATE_ERRORCODE);
                         jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.DATE_ERRORCODE]);
                         jobIdFilterResultList.add(jobIdFilterResult);
                         logger.debug("JobId = " + helpJobList.get(i) + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.DATE_ERRORCODE]);
                     }
                     helpJobList.clear();
                     helpJobList.addAll(jobIdFound);
                 }
 
                 // find job by delegationId.
                 if ((jobIdFound != null) && jobIdFound.size() > 0 && (delegationId != null)) {
                     jobIdFound = jobDB.retrieveJobId(jobIdFound, delegationId, null, userId);
                     helpJobList.removeAll(jobIdFound);
                     for (int i = 0; i < helpJobList.size(); i++) {
                         jobIdFilterResult = new JobIdFilterResult();
                         jobIdFilterResult.setJobId(helpJobList.get(i));
                         jobIdFilterResult.setErrorCode(JobIdFilterFailure.DELEGATIONID_ERRORCODE);
                         jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.DELEGATIONID_ERRORCODE]);
                         jobIdFilterResultList.add(jobIdFilterResult);
                         logger.debug("JobId = " + helpJobList.get(i) + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.DELEGATIONID_ERRORCODE]);
                     }
                     helpJobList.clear();
                     helpJobList.addAll(jobIdFound);
                 }
 
                 // find job by leaseId.
                 if ((jobIdFound != null) && jobIdFound.size() > 0 && (leaseId != null)) {
                     jobIdFound = jobDB.retrieveJobId(jobIdFound, null, leaseId, userId);
                     helpJobList.removeAll(jobIdFound);
                     for (int i = 0; i < helpJobList.size(); i++) {
                         jobIdFilterResult = new JobIdFilterResult();
                         jobIdFilterResult.setJobId(helpJobList.get(i));
                         jobIdFilterResult.setErrorCode(JobIdFilterFailure.LEASEID_ERRORCODE);
                         jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.LEASEID_ERRORCODE]);
                         jobIdFilterResultList.add(jobIdFilterResult);
                         logger.debug("JobId = " + helpJobList.get(i) + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.LEASEID_ERRORCODE]);
                     }
                 }
             } else { // jobList is null or empty.
                 logger.debug("JobList parameter is null in jobFilter.");
                /*
                 jobIdFound = jobDB.retrieveJobId(null, userId, compatibleStatusToFind, fromStatusDate, toStatusDate);
 
                 if ((jobIdFound != null) && (jobIdFound.size() > 0) && (fromDate != null || toDate != null)) {
                     jobIdFound = jobDB.retrieveByDate(jobIdFound, userId, fromDate, toDate);
                 }
 
                 if ((jobIdFound != null) && (jobIdFound.size() > 0) && ((delegationId != null) || (leaseId != null))) {
                     jobIdFound = jobDB.retrieveJobId(jobIdFound, delegationId, leaseId, userId);
                 }
                */
                 jobIdFound = jobDB.retrieveJobId(userId, delegationId, compatibleStatusToFind, fromStatusDate, toStatusDate, leaseId, fromDate, toDate);
             }
             // job found.
             if (jobIdFound != null) {
                 for (int i = 0; i < jobIdFound.size(); i++) {
                     jobIdFilterResult = new JobIdFilterResult();
                     jobIdFilterResult.setJobId(jobIdFound.get(i));
                     jobIdFilterResult.setErrorCode(JobIdFilterFailure.OK_ERRORCODE);
                     jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.OK_ERRORCODE]);
                     jobIdFilterResultList.add(jobIdFilterResult);
                 }
             }
         } catch (DatabaseException e) {
             logger.error(e.getMessage());
             throw new CommandException(e.getMessage());
         }
 
         JobEnumeration jobEnum = new JobEnumeration(jobIdFound, jobDB);
         cmd.getResult().addParameter("JOBID_FILTER_RESULT_LIST", jobIdFilterResultList);
         cmd.getResult().addParameter("JOB_ENUM", jobEnum);
 
         return jobEnum;
     }
 
     private Calendar makeDate(String timestamp) {
         if (timestamp == null) {
             return null;
         }
         Calendar date = null;
         try {
             Long time = Long.parseLong(timestamp);
             date = Calendar.getInstance();
             date.setTimeInMillis(time);
         } catch (Throwable e) {
         }
 
         return date;
     }
 
     private int getCommandType(String cmdName) {
         if (cmdName == null) {
             return -1;
         }
 
         List<String> commandNames = getCommands();
         for (int i=0; i<commandNames.size(); i++) {
             if (cmdName.equals(commandNames.get(i))) {
                 return i;
             }
         }
 
         return -1;
     }
 
     private void insertJobCommand(JobCommand jobCommand, String delegationId, int[] statusType) throws CommandException {
         if (jobCommand == null) {
             throw new CommandException("jobCommand not defined!");
         }
         
         try {
             jobDB.insertJobCommand(jobCommand, delegationId, statusType);
 
             logger.debug("insertJobCommand local user " + jobCommand.getUserId() + " delegId = " + delegationId);
         } catch (DatabaseException de) {
             logger.error("insertJobCommand error: " + de.getMessage());
             throw new CommandException(de.getMessage());         
         } catch (IllegalArgumentException ie) {
             throw new CommandException(ie.getMessage());
         }
     }
     
     public void execute(final Command command) throws CommandExecutorException, CommandException {
         logger.debug("BEGIN execute");
         
         if (command == null) {
             throw new CommandExecutorException("command not defined!");
         }
 
         logger.debug("executing command: " + command.toString());
         
         if (!command.getCategory().equalsIgnoreCase(getCategory())) {
             throw new CommandException("command category mismatch: found \"" + command.getCategory() + "\" required \"" + getCategory() + "\"");
         }
 
         if (command.isAsynchronous() && command.getCommandGroupId() != null && "COMPOUND".equals(command.getCommandGroupId())) {
             List<String> jobIdList = null;
 
             if (command.containsParameterKey("JOB_ID_LIST")) {
                 jobIdList = command.getParameterMultivalue("JOB_ID_LIST");
 
                 command.deleteParameter("JOB_ID_LIST");
             } else {
                 jobIdList = getJobList(command).getJobIdList();
             }
 
             if (command.containsParameterKey("EXECUTION_MODE")) {
                 if (Command.EXECUTION_MODE_SERIAL.equals(command.getParameterAsString("EXECUTION_MODE"))) {
                     command.setExecutionMode(Command.ExecutionModeValues.SERIAL);
                 } else {
                     command.setExecutionMode(Command.ExecutionModeValues.PARALLEL);
                 }
             }
 
             if (command.containsParameterKey("PRIORITY_LEVEL")) {
                 command.setPriorityLevel(Integer.parseInt(command.getParameterAsString("PRIORITY_LEVEL")));
             }
 
             if (jobIdList != null) {
                 for (String jobId : jobIdList) {
                     command.addParameter("JOB_ID", jobId);
                     command.setCommandGroupId(jobId);
 
                     try {
                         getCommandManager().execute(command);
                     } catch (CommandManagerException e) {
                         logger.error(e.getMessage());
                         throw new CommandExecutorException(e.getMessage());
                     }
                 }
             }
 
             if (command.getExecutionCompletedTime() == null) {
                 command.setExecutionCompletedTime(Calendar.getInstance());
             }
 
             logger.debug("END execute");
             return;
         }
 
         String userId = command.getUserId();
 
         if (userId == null) {
             throw new CommandException("userId not defined!");
         }
 
         boolean isAdmin = command.getParameterAsString("IS_ADMIN") != null && command.getParameterAsString("IS_ADMIN").equalsIgnoreCase("true");
 
         int cmdType = getCommandType(command.getName());
 
         if (JobCommandConstant.GET_SERVICE_INFO.equals(command.getName())) {
             JobSubmissionManagerInfo jobSubmissionManagerInfo = JobSubmissionManager.getInstance().getJobSubmissionManagerInfo();
             command.getResult().addParameter("ACCEPT_NEW_JOBS", "" + jobSubmissionManagerInfo.isAcceptNewJobs());
             command.getResult().addParameter("SUBMISSION_THRESHOLD_MESSAGE", jobSubmissionManagerInfo.getShowMessage());
             command.getResult().addParameter("SUBMISSION_ERROR_MESSAGE", jobSubmissionManagerInfo.getTestErrorMessage());
             command.getResult().addParameter("SUBMISSION_EXECUTION_TIMESTAMP", jobSubmissionManagerInfo.getExecutionTimestamp());
             
             String sensorHost = getParameterValueAsString(CREAM_JOB_SENSOR_HOST);
             if (sensorHost != null && !"changeme".equals(sensorHost)) {
                 command.getResult().addParameter("CEMON_URL", "https://" + sensorHost + ":8443/ce-monitor/services/CEMonitor");
             }
 
             jobSubmissionManagerInfo = null; //gc
 
         } else if (JobCommandConstant.SET_ACCEPT_NEW_JOBS.equals(command.getName())) {
             String accept = command.getParameterAsString("ACCEPT_NEW_JOBS");
             if (accept == null) {
                 throw new CommandException("ACCEPT_NEW_JOBS value not specified!");
             }
             if (!isAdmin) {
                 throw new CommandException("Operation reserved only to administrator!");
             }
             try {
                 jobSubmissionManager.enableAcceptNewJobs(Integer.parseInt(accept));
             } catch (NumberFormatException nfe) {
                 throw new CommandException("ACCEPT_NEW_JOBS value not valid!");
             }
 
         } else if (JobCommandConstant.PROXY_RENEW.equals(command.getName())) {
             logger.debug("Calling updateProxyToSandbox.");
 
             String delegId = command.getParameterAsString("DELEGATION_PROXY_ID");
             if (delegId == null) {               
                 throw new CommandException("parameter \"DELEGATION_PROXY_ID\" not defined!");
             }
 
             String delegProxyInfo = command.getParameterAsString("DELEGATION_PROXY_INFO");
             if (delegProxyInfo == null) {               
                 throw new CommandException("parameter \"DELEGATION_PROXY_INFO\" not defined!");
             }
 
             Calendar now = Calendar.getInstance();
             int[] statusType = new int[] {JobStatus.REGISTERED, JobStatus.HELD, JobStatus.IDLE, JobStatus.PENDING, JobStatus.REALLY_RUNNING, JobStatus.RUNNING};
 
             JobCommand jobCmd = new JobCommand();
             jobCmd.setCreationTime(command.getCreationTime());
             //jobCmd.setDescription(command.getDescription());
             jobCmd.setDescription(delegProxyInfo);
             jobCmd.setStartSchedulingTime(command.getStartProcessingTime());
             jobCmd.setStartProcessingTime(now);
             jobCmd.setExecutionCompletedTime(now);
             jobCmd.setType(cmdType);
             jobCmd.setStatus(JobCommand.SUCCESSFULL);
             jobCmd.setCommandExecutorName(getName());
             jobCmd.setUserId(userId);
 
             insertJobCommand(jobCmd, delegId, statusType);
 
         } else if (JobCommandConstant.JOB_REGISTER.equals(command.getName())) {
             logger.debug("Calling jobRegister.");
             jobRegister(command);
 
         } else if (JobCommandConstant.JOB_SET_LEASEID.equals(command.getName())) {
             logger.debug("Calling jobSetLeaseId.");
             jobSetLeaseId(command);
 
         } else if (JobCommandConstant.DELETE_LEASE.equals(command.getName())) {
             logger.debug("Calling deleteLease.");
             deleteLease(command);
 
         } else if (JobCommandConstant.SET_LEASE.equals(command.getName())) {
             logger.debug("Calling setLease.");
             setLease(command);
 
         } else if (JobCommandConstant.GET_LEASE.equals(command.getName())) {
             logger.debug("Calling getLease.");
             getLease(command);
 
         } else if (JobCommandConstant.JOB_INFO.equals(command.getName())) {
             logger.debug("Calling jobInfo.");
             getJobList(command);
 
         } else if (JobCommandConstant.JOB_STATUS.equals(command.getName())) {
             logger.debug("Calling jobStatus.");
             JobEnumeration jobEnum = getJobList(command);
             String user = null;
 
             if (!isAdmin) {
                 user = userId;
             }
 
             try {
                 List<JobStatus> jobStatusList = jobDB.retrieveLastJobStatus(jobEnum.getJobIdList(), user);
                 command.getResult().addParameter("JOB_STATUS_LIST", jobStatusList);
 
                 if (command.containsParameterKey("JOB_ID") || command.containsParameterKey("JOB_ID_LIST")) {
                     if (jobStatusList.size() < jobEnum.getJobIdList().size()) {                
                         List<String> jobIdList = new ArrayList<String>(jobStatusList.size());
 
                         for (JobStatus status : jobStatusList) {
                             jobIdList.add(status.getJobId());
                         }
 
                         jobEnum.getJobIdList().removeAll(jobIdList);
 
                         int found = 0;
 
                         List<JobIdFilterResult> jobIdFilterResultList = (List<JobIdFilterResult>) command.getResult().getParameter("JOBID_FILTER_RESULT_LIST");
 
                         for (JobIdFilterResult filterResult : jobIdFilterResultList) {
                             if (jobEnum.getJobIdList().contains(filterResult.getJobId())) {
                                 filterResult.setErrorCode(JobIdFilterFailure.JOBID_ERRORCODE);
                                 filterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.JOBID_ERRORCODE]);
 
                                 if (++found == jobEnum.getJobIdList().size()) {
                                     break;
                                 }
                             }
                         }
 
                         command.getResult().addParameter("JOB_ENUM", new JobEnumeration(jobIdList, jobDB));
                     }
                 }
             } catch (DatabaseException e) {
                 throw new CommandException(e.getMessage());
             }
         } else if (JobCommandConstant.QUERY_EVENT.equals(command.getName())) {
             logger.debug("Calling queryEvent.");
             int maxEvents = 100;
             
             try {
                 maxEvents = Integer.parseInt(command.getParameterAsString("MAX_QUERY_EVENT_RESULT_SIZE"));
             } catch (Throwable t) {
                 logger.warn("queryEvent: wrong value for MAX_QUERY_EVENT_RESULT_SIZE");
             }
             
             int[] jobStatusTypeArray = null;
             if (command.containsParameterKey("statusList")) {
                 List<String> statusList = command.getParameterMultivalue("statusList");
                 
                 jobStatusTypeArray = new int[statusList.size()];
 
                 for (int i = 0; i < statusList.size(); i++) {
                     jobStatusTypeArray[i] = -1;
                     
                     for (int x = 0; x < JobStatus.statusName.length; x++) {
                         if (statusList.get(i).equals(JobStatus.getNameByType(x))) {
                             jobStatusTypeArray[i] = x;
                             continue;
                         }
                     }
                 }
             }
             
             JobStatusEventManagerInterface jobStatusEventManager = (JobStatusEventManagerInterface) EventManagerFactory.getEventManager("JOB_STATUS_EVENT_MANAGER");
             List<Event> eventList = null;
             
             try {
                 eventList = jobStatusEventManager.getEvents(command.getParameterAsString("FROM_EVENT_ID"), command.getParameterAsString("TO_EVENT_ID"),
                         makeDate(command.getParameterAsString("FROM_DATE")), makeDate(command.getParameterAsString("TO_DATE")), jobStatusTypeArray,
                         maxEvents, userId);                
             } catch (EventManagerException e) {
                 throw new CommandException(e.getMessage());
             }
 
             command.getResult().addParameter("EVENT_LIST", eventList);
         } else if (JobCommandConstant.SET_JOB_STATUS.equals(command.getName())) {
             logger.debug("Calling setJobStatus.");
 
             int statusType = Integer.valueOf(command.getParameterAsString("STATUS_TYPE"));
             String jobId = command.getParameterAsString("JOB_ID");
             String workerNode = command.getParameterAsString("WORKER_NODE");
             String lrmsJobId = command.getParameterAsString("LRMS_JOB_ID");
             String exitCode = command.getParameterAsString("EXIT_CODE");
             String failureReason = command.getParameterAsString("FAILURE_REASON");
             Calendar changeTime = null;
  
             if (command.containsParameterKey("STATUS_CHANGE_TIME")) {
                 changeTime = new GregorianCalendar();
                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 dateFormat.setCalendar(changeTime);
                 try {
                      dateFormat.parse(command.getParameterAsString("STATUS_CHANGE_TIME"));
                 } catch (ParseException e) {
                    logger.error(e.getMessage());
                 }
             }
 
             JobStatus status = new JobStatus(statusType, jobId, changeTime);
             status.setExitCode(exitCode);
             status.setFailureReason(failureReason);
 
             Job job = null;
             try {
                 job = jobDB.retrieveJob(status.getJobId(), null);
             } catch (Exception e) {
                 logger.warn("job " + status.getJobId() + " not found!");
                 return;
             }
 
             try {
                 if(doOnJobStatusChanged(status, job)) {
                     boolean updateJob = false;
                     if (lrmsJobId != null && (job.getLRMSJobId() == null || job.getLRMSJobId().equalsIgnoreCase("N/A"))) {
                         job.setLRMSJobId(lrmsJobId);
                         updateJob = true;
                     }
 
                     if (workerNode != null) {
                         boolean isReallyRunning = false;
 
                         if(job.getWorkerNode() != null && !job.getWorkerNode().equals("N/A") && status.getType() != JobStatus.REALLY_RUNNING) {
                             for(JobStatus oldStatus : job.getStatusHistory()) {
                                 if(oldStatus.getType() == JobStatus.REALLY_RUNNING) {
                                     isReallyRunning = true;
                                     break;
                                 }
                             }
                         }
 
                         if(!isReallyRunning) {
                             job.setWorkerNode(workerNode);
                             updateJob = true;
                         }
                     }
 
                     if (updateJob) {
                         try {
                             jobDB.update(job);
                         } catch (Throwable e) {
                             logger.error(e);
                         }
                     }
                 }
             } catch (JobManagementException e) {
                logger.error(e.getMessage());
             }
         } else if (JobCommandConstant.JOB_LIST.equals(command.getName())) {
             logger.debug("Calling jobList.");
             try {
                 String user = null;
 
                 if (!isAdmin) {
                     user = userId;
                 }
 
                 List<String> jobIdFound = jobDB.retrieveJobId(user);
                 JobEnumeration jobEnum = new JobEnumeration(jobIdFound, jobDB);
                 command.getResult().addParameter("JOB_ENUM", jobEnum);
             } catch (DatabaseException e) {
                 logger.error(e.getMessage());
                 throw new CommandException(e.getMessage());
             }
         } else {
             JobEnumeration jobEnum = getJobList(command);
             
             try {
                 List<Job> jobList = new ArrayList<Job>(0);
                 Calendar now = Calendar.getInstance();
                 
                 while (jobEnum.hasMoreJobs()) {
                     Job job = jobEnum.nextJob();
 
                     JobCommand jobCmd = new JobCommand();
                     jobCmd.setJobId(job.getId());
                     jobCmd.setCreationTime(command.getCreationTime());
                     jobCmd.setDescription(command.getDescription());
                     jobCmd.setStartSchedulingTime(command.getStartProcessingTime());
                     jobCmd.setStartProcessingTime(now);
                     jobCmd.setType(cmdType);
                     jobCmd.setCommandExecutorName(getName());
 
                     if (!isAdmin || job.getUserId().equals(command.getUserId())) {
                         jobCmd.setUserId(command.getUserId());
                     }
 
                     if ((JobCommandConstant.JOB_CANCEL.equals(command.getName())) && (jobCmd.getDescription() == null)) {
                         if (!isAdmin || job.getUserId().equals(command.getUserId())) {
                             jobCmd.setDescription("Cancelled by user");
                         } else {
                             jobCmd.setDescription("Cancelled by CE admin");
                         }
                     } 
                     
                     logger.debug("Calling jobDB.insertJobCommand.");
                     try {
                         jobDB.insertJobCommand(jobCmd);
                     } catch (Throwable e) {
                         logger.error(e.getMessage());
                         continue;
                     }
 
                     logger.debug("jobDB.insertJobCommand has been executed.");
 
                     if (jobCmd.getStatus() != JobCommand.ERROR) {
                         job.addCommandHistory(jobCmd);
                         jobList.add(job);
                     }
                 }
 
                 for (Job j : jobList) {
                     JobCommand jobCmd = j.getLastCommand();
                     if (jobCmd == null) {
                         continue;
                     }
 
                     jobCmd.setStatus(JobCommand.PROCESSING);
 
                     if (LBLogger.isEnabled()) {
                         try {
                             LBLogger.getInstance().execute(j, command, LBLogger.START, null, null);
                         } catch (Throwable t) {
                             logger.warn("LBLogger.execute() failed: " + t.getMessage());
                         }
                     }
 
                     try {
                         if (JobCommandConstant.JOB_CANCEL.equals(command.getName())) {
                             if (j.getLastStatus() != null && j.getLastStatus().getType() == JobStatus.REGISTERED) {
                                 JobStatus status = new JobStatus(JobStatus.CANCELLED, j.getId(), now);
                                 status.setDescription(jobCmd.getDescription());
                                 doOnJobStatusChanged(status, j);
                             } else {
                                 cancel(j);
                             }
 
                         } else if (JobCommandConstant.JOB_PURGE.equals(command.getName())) {
                             purge(j);
                         
                         } else if (JobCommandConstant.JOB_SUSPEND.equals(command.getName())) {
                             suspend(j);
                         
                         } else if (JobCommandConstant.JOB_RESUME.equals(command.getName())) {
                             resume(j);
                         
                         } else if (JobCommandConstant.JOB_START.equals(command.getName())) {
                             command.addParameter("JOB", j);
                             jobStart(command);
 
                             StringBuffer sb = new StringBuffer(command.toString());
 
                             if (j.getLRMSAbsLayerJobId() != null) {
                                 sb.append(" lrmsAbsJobId=").append(j.getLRMSAbsLayerJobId()).append(";");
                             }
 
                             if (j.getLRMSJobId() != null) {
                                 sb.append(" lrmsJobId=").append(j.getLRMSJobId()).append(";");
                             }
 
                             logger.info(sb.toString());
                             sb = null;
                             break;
 //                        case JobCommandConstant.UPDATE_PROXY_TO_SANDBOX.equals(command.getName())) {
 //                            logger.info(logInfo + jobCmd.toString() + " delegationId=" + j.getDelegationProxyId() + ";");
 //
 //                            String attr = j.getExtraAttribute("PROXY_RENEWAL");
 //                            if (attr.equals("ENABLED")) {
 //                                String delegationProxyInfo = command.getParameterAsString("DELEGATION_PROXY_INFO");
 //                                if (delegationProxyInfo != null) {
 //                                    j.setDelegationProxyInfo(delegationProxyInfo);
 //                                    jobDB.update(j);
 //                                }
 //                            }
                         }
 
                         if (!JobCommandConstant.JOB_PURGE.equals(command.getName())) {
                             jobCmd.setStatus(JobCommand.SUCCESSFULL);
 
                             jobDB.updateJobCommand(jobCmd);
                         }
 
                         if (LBLogger.isEnabled()) {
                             try {
                                 LBLogger.getInstance().execute(j, command, LBLogger.OK, j.getLRMSAbsLayerJobId(), null);
                             } catch (Throwable t) {
                                 logger.warn("LBLogger.execute() failed: " + t.getMessage());
                             }
                         }
                     } catch (CommandException ce) {
                         jobCmd.setStatus(JobCommand.ERROR);
                         jobCmd.setFailureReason(ce.getMessage());
 
                         jobDB.updateJobCommand(jobCmd);
 
                         if (LBLogger.isEnabled()) {
                             try {
                                 LBLogger.getInstance().execute(j, command, LBLogger.FAILED, null, ce);
                             } catch (Throwable t) {
                                 logger.warn("LBLogger.execute() failed: " + t.getMessage());
                             }
                         }
                     }
                 }
 
                 List<String> invalidJobIdlist = (List<String>) command.getResult().getParameter("JOB_ID_LIST_STATUS_NOT_COMPATIBLE_FOUND");
 
                 if (invalidJobIdlist != null) {
                     for (String jobId : invalidJobIdlist) {
                         try {
                             JobCommand jobCmd = new JobCommand(cmdType, jobId);
                             jobCmd.setCreationTime(command.getCreationTime());
                             jobCmd.setDescription(command.getDescription());
                             jobCmd.setStartSchedulingTime(command.getStartProcessingTime());
                             jobCmd.setStatus(JobCommand.ERROR);
                             jobCmd.setFailureReason("status not compatible with the specified command!");
 
                             if (!isAdmin) {
                                 jobCmd.setUserId(command.getUserId());
                             }
 
                             jobDB.insertJobCommand(jobCmd);
                         } catch (DatabaseException e) {
                             logger.error(e.getMessage());
                         }
                     }
                 }
             } catch (DatabaseException e) {
                 throw new CommandException(e.getMessage());
             } catch (JobManagementException e) {
                 throw new CommandException(e.getMessage());
             }
         }
 
         if (command.getExecutionCompletedTime() == null) {
             command.setExecutionCompletedTime(Calendar.getInstance());
         }
 
         logger.debug("END execute");
     }
     
     private String normalize(String s) {
         if (s != null) {
             return s.replaceAll("\\W", "_");
         }
         return null;
     }
     
     private void jobStart(Command command) throws CommandException {
         if (command == null) {
             throw new CommandException("command not defined!");
         }
         
         if (!command.containsParameterKey("JOB")) {
             throw new CommandException("job not defined!");
         }
 
         String userDN = command.getParameterAsString("USER_DN");
         if (userDN == null) {
             throw new CommandException("parameter \"USER_DN\" not defined!");
         }
                        
         Job job = (Job)command.getParameter("JOB");
         
         logger.debug("Begin jobStart for job " + job.getId());
 
         JobStatus status = new JobStatus(JobStatus.PENDING, job.getId());
         try {
             doOnJobStatusChanged(status, job);
         } catch (Throwable t) {
           throw new CommandException(t.getMessage());
         } 
 
         Calendar now = Calendar.getInstance();
         CommandResult cr = null;
         String failureReason = null;
         
         for (int i=1; i<4 && cr == null; i++) {
             failureReason = null;
 
             try {
                 cr = submit(job);
             } catch (CommandException ce) {
                 failureReason = ce.getMessage();
                 logger.warn("submission to BLAH failed [jobId=" + job.getId() + "; reason=" + failureReason + "; retry count=" + i + "/3]");
 
                 synchronized(now) {
                     try {
                         logger.debug("sleeping 10 sec...");
                         now.wait(10000);
                         logger.debug("sleeping 10 sec... done");
                     } catch (InterruptedException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 }
             }
         }
         
         if (cr == null) {
             status = new JobStatus(JobStatus.ABORTED, job.getId());
             status.setDescription("submission to BLAH failed [retry count=3]");
             status.setFailureReason(failureReason);
 
             try {
                 doOnJobStatusChanged(status, job);
             } catch (Throwable te) {
                 throw new CommandException(te.getMessage());
             }
 
             setLeaseExpired(job);
 
             throw new CommandException("submission to BLAH failed [retry count=3]" + (failureReason != null ? ": " + failureReason : ""));
         }
 
         job.setLRMSJobId(cr.getParameterAsString("LRMS_JOB_ID"));
         job.setLRMSAbsLayerJobId(cr.getParameterAsString("LRMS_ABS_JOB_ID"));
 
         try {
             if (isEmptyField(job.getLRMSAbsLayerJobId())) {
                 status = new JobStatus(JobStatus.ABORTED, job.getId());
                 status.setFailureReason("LRMSAbsLayerJobId not found!");
 
                 doOnJobStatusChanged(status, job);
 
                 setLeaseExpired(job);
             } else {
                 jobDB.update(job);
 
                 JobStatus lastStatus = jobDB.retrieveLastJobStatus(job.getId(), command.getUserId());
                 if (lastStatus.getType() == JobStatus.PENDING) {
                     status = new JobStatus(JobStatus.IDLE, job.getId(), now);
 
                     doOnJobStatusChanged(status, job);
                 }
             }
         } catch (Throwable te) {
             throw new CommandException(te.getMessage());
         }
 
         logger.debug("End jobStart for job " + job.getId());
     }
 
     private void jobSetLeaseId(final Command command) throws CommandException {
         logger.debug("Begin setJobLeaseId");
         List<String> jobIdList = null;
         String leaseId = command.getParameterAsString("NEW_LEASE_ID");
         String userId = command.getUserId();
 
         if (userId == null) {
             throw new CommandException("userId not specified!");
         }
 
         JobEnumeration jobEnum = getJobList(command);
         jobIdList = jobEnum.getJobIdList();
         int commandType = getCommandType(JobCommandConstant.JOB_SET_LEASEID);
 
         for (int index = 0; index < jobIdList.size(); index++) {
             JobCommand jobCmd = new JobCommand(commandType, jobIdList.get(index));
             jobCmd.setCreationTime(command.getCreationTime());
             // jobCmd.setDescription(msg);
             jobCmd.setUserId(userId);
             jobCmd.setStartSchedulingTime(command.getStartSchedulingTime());
             jobCmd.setStartProcessingTime(command.getStartProcessingTime());
             jobCmd.setStatus(JobCommand.PROCESSING);
             try {
                 jobDB.insertJobCommand(jobCmd);
                 
                 leaseManager.setJobLeaseId(jobIdList.get(index), leaseId, userId);
 
                 jobCmd.setStatus(JobCommand.SUCCESSFULL);
                                 
                 jobDB.updateJobCommand(jobCmd);
             } catch (Exception e) {
                 logger.error(e.getMessage());
                 jobCmd.setStatus(JobCommand.ERROR);
                 jobCmd.setFailureReason(e.getMessage());
                 try {
                     jobDB.updateJobCommand(jobCmd);
                 } catch (DatabaseException de) {
                     logger.error(e.getMessage());
                 }
             }
         }// for
         logger.debug("End setJobLeaseId");
     }
 
     private void getLease(final Command command) throws CommandException {
         logger.debug("Begin getLease");
         List<Lease> leaseList = new ArrayList<Lease>(0);
 
         try {
             if (command.containsParameterKey("LEASE_ID")) {
                 Lease lease = jobDB.retrieveJobLease(command.getParameterAsString("LEASE_ID"), command.getUserId());
                 if (lease != null) {
                     leaseList.add(lease);
                 }
             } else {
                 List<Lease> lease = jobDB.retrieveJobLease(command.getUserId());
                 if (lease != null) {
                     leaseList = lease;
                 }
             }
 
             command.getResult().addParameter("LEASE_LIST", leaseList);
         } catch (DatabaseException e) {
             throw new CommandException(e.getMessage());
         }
         logger.debug("End getLease");
     }
 
     private void deleteLease(final Command command) throws CommandException {
         logger.debug("Begin deleteLease");
         if (!command.containsParameterKey("LEASE_ID")) {
             throw new CommandException("lease id not specified!");
         }
 
         String leaseId = command.getParameterAsString("LEASE_ID");
         String userId = command.getUserId();
 
         if (userId == null) {
             throw new CommandException("userId not specified!");
         }
 
         leaseManager.deleteLease(leaseId, userId);
         logger.debug("End deleteLease");
     }
 
     private void setLease(final Command command) throws CommandException {
         logger.debug("BEGIN setLease");
         if (!command.containsParameterKey("LEASE_ID")) {
             throw new CommandException("lease id not specified!");
         }
         if (!command.containsParameterKey("LEASE_TIME")) {
             throw new CommandException("lease time not specified!");
         }
 
         String userId = command.getUserId();
         String leaseId = command.getParameterAsString("LEASE_ID");
 
         Calendar leaseTime = null;
         Calendar boundedLeaseTime = null;
         try {
             Long timestamp = Long.parseLong(command.getParameterAsString("LEASE_TIME"));
             leaseTime = Calendar.getInstance();
             leaseTime.setTimeInMillis(timestamp);
             logger.debug("leaseTime = " + leaseTime.getTime());
             Lease lease = new Lease();
             lease.setLeaseId(leaseId);
             lease.setUserId(userId);
             lease.setLeaseTime(leaseTime);
             boundedLeaseTime = leaseManager.setLease(lease);
             command.getResult().addParameter("LEASE_TIME", boundedLeaseTime);
             logger.debug("boundedLeaseTime = " + boundedLeaseTime);
         } catch (IllegalArgumentException e) {
             logger.error(e.getMessage());
             throw new CommandException(e.getMessage());
         } catch (CommandException e) {
             logger.error(e.getMessage());
             throw e;
         } catch (Exception e) {
             logger.error(e.getMessage());
             throw new CommandException(e.getMessage());
         }
 
         logger.debug("END setLease");
     }
 
     private void makeVODir(File dir) throws IOException, IllegalArgumentException {
         if (dir == null) {
             throw new IllegalArgumentException("parameter \"dir\" not specified!");
         }
         
         Runtime runtime = Runtime.getRuntime();
         runtime.exec("chmod 775 " + dir.getAbsolutePath());
 
         Process proc = Runtime.getRuntime().exec("groups");
 
         InputStreamReader isr = new InputStreamReader(proc.getInputStream());
         BufferedReader in = new BufferedReader(isr);
         String inputLine = "";
         String groups = "";
 
         try {
             while ((inputLine = in.readLine()) != null) {
                 groups += inputLine + " ";
             }
         } catch (IOException e) {
             throw e;
         } finally {
             in.close();
         }
 
         try {
             proc.waitFor();
         } catch (InterruptedException e) {
             logger.warn("makeVODir: " + e.getMessage());
         } finally {
             try {
                 proc.getInputStream().close();
             } catch (IOException ioe) {}
             try {
                 proc.getErrorStream().close();
             } catch (IOException ioe) {}
             try {
                 proc.getOutputStream().close();
             } catch (IOException ioe) {}
         }
 
         String[] groupArray = groups.split(" ");
 
         for (int i = 0; i < groupArray.length; i++) {
             String subDirPath = dir.getAbsolutePath() + "/" + groupArray[i];
             File subDir = new File(subDirPath);
             if (!subDir.exists()) {
                 subDir.mkdir();
                 runtime.exec("chmod 770 " + subDirPath);
                 runtime.exec("chgrp " + groupArray[i] + " " + subDirPath);
             }
             
             subDir = null;
         }
     }
 
     private boolean isEmptyField(String field) {
         return (field == null || Job.NOT_AVAILABLE_VALUE.equals(field) || field.length() == 0);        
     }
 
     protected void createJobSandboxDir(Job job, String gsiFTPcreamURL) throws CommandException, InterruptedException, IOException {
         Runtime runtime = Runtime.getRuntime();
         BufferedReader in = null;
         BufferedOutputStream os = null;
         BufferedReader readErr = null;
         Process proc = null;
 
         if (!containsParameterKey(JOB_WRAPPER_TEMPLATE_PATH)) {
             throw new CommandException("parameter \"JOB_WRAPPER_TEMPLATE_PATH\" not defined!");
         }
 
         String cream_sandbox_dir = getParameterValueAsString("CREAM_SANDBOX_DIR");
         if (cream_sandbox_dir == null) {
             throw new CommandException("parameter \"CREAM_SANDBOX_DIR\" not defined!");
         }
 
         job.setCREAMSandboxBasePath(cream_sandbox_dir);
 
         if(!containsParameterKey("CREAM_CREATE_SANDBOX_BIN_PATH")) {
             throw new CommandException("parameter \"CREAM_CREATE_SANDBOX_BIN_PATH\" not defined!");
         }
 
         try {
             String[] cmd = new String[] { "sudo", "-S", "-n", "-u", job.getLocalUser(), getParameterValueAsString("CREAM_CREATE_SANDBOX_BIN_PATH"), cream_sandbox_dir, job.getUserId(), job.getId(), "true" };
 
             try {
                 proc = runtime.exec(cmd);
             } catch (Throwable e) {
                logger.error("createJobSandboxDir: " + e.getMessage());
                 throw (new IOException("Cannot create the sandbox for job " +  job.getId() + "! " + e.getMessage()));
             }
 
             in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
             os = new BufferedOutputStream(proc.getOutputStream());
             readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 
             try {
                 job.setWorkingDirectory(in.readLine());
 
                 if(job.getWorkingDirectory() == null) {
                     job.setWorkingDirectory(Job.NOT_AVAILABLE_VALUE);
 
                     throw new IOException();
                 }
             } catch (IOException e) {
                 throw new IOException("cannot create the job's working directory!");
             }
 
             if (gsiFTPcreamURL != null && job.getWorkingDirectory() != null) {
                 job.setCREAMInputSandboxURI(gsiFTPcreamURL + job.getWorkingDirectory() + "/ISB");
                 job.setCREAMOutputSandboxURI(gsiFTPcreamURL + job.getWorkingDirectory() + "/OSB");
 
                 String dsUploadFile = null;
                 if (job.containsVolatilePropertyKeys(Job.OUTPUT_DATA)) {
                     if (Job.NOT_AVAILABLE_VALUE.equals(job.getGridJobId()) || job.getGridJobId() == null) {                
                         dsUploadFile = "DSUpload_" + job.getId() + ".out";
                         job.getOutputFiles().add(dsUploadFile);
                         job.addVolatileProperty(Job.DS_UPLOAD_OUTPUT_FILE, dsUploadFile);
                     } else {
                         job.addVolatileProperty(Job.DS_UPLOAD_OUTPUT_FILE, "DSUpload_" + job.getGridJobId().substring(job.getGridJobId().lastIndexOf('/') +1) + ".out");
                     }
                 }
 
                 if (containsParameterKey(SANDBOX_TRANSFER_METHOD)) {
                     job.addVolatileProperty(SANDBOX_TRANSFER_METHOD, getParameterValueAsString(SANDBOX_TRANSFER_METHOD));
                 } else {
                     job.addVolatileProperty(SANDBOX_TRANSFER_METHOD, GSIFTP_SANDBOX_TRANSFER_METHOD);
                 }
 
                 if (GSIFTP_SANDBOX_TRANSFER_METHOD.equalsIgnoreCase((String)job.getVolatileProperty(SANDBOX_TRANSFER_METHOD))) {
                     if (job.getOutputFiles() != null && job.getOutputFiles().size() > 0) {
                         if (job.getOutputSandboxBaseDestURI() != null) {
                             if ("gsiftp://localhost".equalsIgnoreCase(job.getOutputSandboxBaseDestURI())) {
                                 job.setOutputSandboxBaseDestURI(job.getCREAMOutputSandboxURI());
                             }
                         } else {
                             int index = 0, i=0;
                             String outputFile = null;
                             ArrayList<String> outputSandboxDestURI = new ArrayList<String>(0);
 
                             if (dsUploadFile != null) {
                                 job.getOutputSandboxDestURI().add(job.getCREAMOutputSandboxURI() + File.separator + dsUploadFile);
                             }
 
                             for (String file : job.getOutputSandboxDestURI()) {
                                 if (file.startsWith("gsiftp://localhost")) {
                                     if (file.equals("gsiftp://localhost")) {
                                         outputFile = job.getOutputFiles().get(i);
                                     } else {
                                         outputFile = file.substring("gsiftp://localhost".length()+1);
                                     }
 
                                     index = outputFile.lastIndexOf("/");
                                     outputFile = index > 0? outputFile.substring(index + 1): outputFile;
 
                                     outputSandboxDestURI.add(job.getCREAMOutputSandboxURI() + outputFile);
                                 } else {
                                     outputSandboxDestURI.add(file);
                                 }
                             
                                 i++;
                             }
 
                             job.setOutputSandboxDestURI(outputSandboxDestURI);
                         }
                     }
                 } else if (LRMS_SANDBOX_TRANSFER_METHOD.equalsIgnoreCase((String)job.getVolatileProperty(SANDBOX_TRANSFER_METHOD))) {
                     int index=0;
 
                     if (job.getInputFiles() != null && job.getInputFiles().size() > 0) {
                         String inputFile = null;
                         StringBuffer transferInput = new StringBuffer();
                         ArrayList<String> inputFiles = new ArrayList<String>(0);
                         ArrayList<String> lrmsInputFiles = new ArrayList<String>(0);
 
                         for (String file : job.getInputFiles()) {
                             if (file.startsWith("/") || file.startsWith("file://") ||
                                     (job.getInputSandboxBaseURI() == null && !file.startsWith("gsiftp://") && !file.startsWith("https://"))) {
                                 index = file.lastIndexOf("/");
                                 inputFile = index > 0? file.substring(index + 1): file;
 
                                 transferInput.append(inputFile).append(",");
 
                                 lrmsInputFiles.add(inputFile);
                             } else {
                                 inputFiles.add(file);
                             }
                         }
 
                         if (transferInput.length() > 0) {
                             transferInput.replace(transferInput.length()-1, transferInput.length(), "\"");
                             job.addExtraAttribute("TransferInput", "TransferInput=\"" + transferInput.toString());
                             job.addExtraAttribute("iwd", "iwd=\"" + job.getWorkingDirectory() + "/ISB\"");
                         }
 
                         if (inputFiles.size() == 0) {
                             job.setInputSandboxBaseURI(null);
                             job.setInputFiles(null);
                         } else {
                             job.setInputFiles(inputFiles);
                         }
 
                         if (lrmsInputFiles.size() > 0) {
                             job.addVolatileProperty(LRMS_INPUT_FILES, lrmsInputFiles);
                         }
                     }
 
                     if (job.getOutputFiles() != null && job.getOutputFiles().size() > 0) {
                         String outputFile = null;
                         String osbDir = job.getWorkingDirectory() + "/OSB/";
                         StringBuffer transferOutput = new StringBuffer();
                         StringBuffer transferOutputRemaps = new StringBuffer();
                         ArrayList<String> outputFiles = new ArrayList<String>(0);
                         ArrayList<String> lrmsOutputFiles = new ArrayList<String>(0);
                         ArrayList<String> outputSandboxDestURI = new ArrayList<String>(0);
 
                         if (job.getOutputSandboxBaseDestURI() != null) {
                             if ("gsiftp://localhost".equalsIgnoreCase(job.getOutputSandboxBaseDestURI())) {
                                 for (String file : job.getOutputFiles()) { 
                                     index = file.lastIndexOf("/");
                                     outputFile = index > 0? file.substring(index + 1): file;
 
                                     lrmsOutputFiles.add(outputFile);
 
                                     transferOutput.append(outputFile).append(",");
                                     transferOutputRemaps.append(outputFile).append("=").append(osbDir).append(outputFile).append(";");
                                 }
 
                                 job.setOutputFiles(null);
                                 job.setOutputSandboxBaseDestURI(null);
                             }
                         } else {
                             int i=0;
 
                             if (dsUploadFile != null) {
                                 job.getOutputSandboxDestURI().add("gsiftp://localhost");
                             }
 
                             for (String file : job.getOutputSandboxDestURI()) {
                                 outputFile = job.getOutputFiles().get(i++);
 
                                 if (file.startsWith("gsiftp://localhost")) {
                                     index = outputFile.lastIndexOf("/");
                                     outputFile = index > 0? outputFile.substring(index + 1): outputFile;
 
                                     lrmsOutputFiles.add(outputFile);
 
                                     transferOutput.append(outputFile).append(",");
                                     transferOutputRemaps.append(outputFile).append("=").append(osbDir);
 
                                     if (file.length() > "gsiftp://localhost".length()) {
                                         outputFile = file.substring("gsiftp://localhost".length()+1);
                                         index = outputFile.lastIndexOf("/");
                                         outputFile = index > 0? file.substring(index + 1): outputFile;
                                     }
 
                                     transferOutputRemaps.append(outputFile).append(";");
                                 } else {
                                     outputFiles.add(outputFile);
                                     outputSandboxDestURI.add(file);                                    
                                 }
                             }
 
                             job.setOutputFiles(outputFiles.size() > 0 ? outputFiles : null);    
                             job.setOutputSandboxDestURI(outputSandboxDestURI.size() > 0 ? outputSandboxDestURI : null);
                         }
 
                         if (transferOutput.length() > 0) {
                             transferOutput.replace(transferOutput.length()-1, transferOutput.length(), "\"");
                             transferOutputRemaps.replace(transferOutputRemaps.length()-1, transferOutputRemaps.length(), "\"");
                             job.addExtraAttribute("TransferOutput", "TransferOutput=\"" + transferOutput.toString());
                             job.addExtraAttribute("TransferOutputRemaps", "TransferOutputRemaps=\"" + transferOutputRemaps.toString());
                         }
 
                         if (lrmsOutputFiles.size() > 0) {
                             job.addVolatileProperty(LRMS_OUTPUT_FILES, lrmsOutputFiles);
                         }
                     }
                 } else {
                     throw new CommandException("sandbox transfer mode \"" + job.getVolatileProperty(SANDBOX_TRANSFER_METHOD) + "\" not supported!");
                 }
             } else {
                 job.setCREAMInputSandboxURI(Job.NOT_AVAILABLE_VALUE);
                 job.setCREAMOutputSandboxURI(Job.NOT_AVAILABLE_VALUE);
             }
 
             String id = "" + StrictMath.random();
 
             if (id.charAt(1) == '.') {
                 id = id.substring(2);
             }
 
             if (id.length() > 20) {
                 id = id.substring(0, 20);
             }
 
             if (gsiFTPcreamURL != null && (isEmptyField(job.getICEId()) || (!isEmptyField(job.getICEId()) && !isEmptyField(job.getMyProxyServer())))) {
                 job.addExtraAttribute("PROXY_RENEWAL", "ENABLED");
                 job.addVolatileProperty(DELEGATION_PROXY_CERT_SANDBOX_URI, gsiFTPcreamURL + job.getDelegationProxyCertPath());
 
                 if(containsParameterKey(JOB_WRAPPER_DELEGATION_TIME_SLOT)) {
                     job.addVolatileProperty(JOB_WRAPPER_DELEGATION_TIME_SLOT, (String)getParameterValue(JOB_WRAPPER_DELEGATION_TIME_SLOT));
                 }
             } else {
                 job.addExtraAttribute("PROXY_RENEWAL", "DISABLED");
             }
 
             if (containsParameterKey(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT)) {
                 job.addVolatileProperty(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT, (String)getParameterValue(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT));
             }
             
             if (containsParameterKey(JOB_WRAPPER_COPY_RETRY_COUNT_ISB)) {
                 job.addVolatileProperty(JOB_WRAPPER_COPY_RETRY_COUNT_ISB, (String)getParameterValue(JOB_WRAPPER_COPY_RETRY_COUNT_ISB));
             }
 
             if (containsParameterKey(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB)) {
                 job.addVolatileProperty(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB, (String)getParameterValue(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_ISB));
             }
 
             if (containsParameterKey(JOB_WRAPPER_COPY_RETRY_COUNT_OSB)) {
                 job.addVolatileProperty(JOB_WRAPPER_COPY_RETRY_COUNT_OSB, (String)getParameterValue(JOB_WRAPPER_COPY_RETRY_COUNT_OSB));
             }
 
             if (containsParameterKey(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB)) {
                 job.addVolatileProperty(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB, (String)getParameterValue(JOB_WRAPPER_COPY_RETRY_FIRST_WAIT_OSB));
             }
 
             job.addVolatileProperty(JOB_WRAPPER_TEMPLATE_PATH, getParameterValueAsString(JOB_WRAPPER_TEMPLATE_PATH));
             
             String tmpWrapper = null;
 
             try {
                 tmpWrapper = JobWrapper.buildWrapper(job);
             } catch (Throwable e) {
                 proc.destroy();
                 throw new CommandException("cannot generate the job wrapper! the problem seems to be related to the jdl: " + e.getMessage());
             }
 
             if (tmpWrapper == null) {
                 throw new CommandException("cannot generate the job wrapper!");
             }
 
             try {
                 os.write(tmpWrapper.getBytes());
                 os.flush();
                 os.close();
                 os = null;
             } catch (Throwable e) {
                 throw new CommandException("cannot write the job wrapper (jobId = " + job.getId() + ")!");
             }
         } catch (CommandException ce) {
             throw ce;
         } catch (Throwable exx) {
             if (proc != null) {
                 proc.waitFor();
 
                 if(proc.exitValue() != 0) {            
                     String procErrorMessage = null;
 
                     if (readErr.ready()) {
                         procErrorMessage = " [failure reason = \"" + readErr.readLine() + "\"]";
                     }
 
                     procErrorMessage += " [exit code = " + proc.exitValue() + "]";
 
                     throw new CommandException(exx.getMessage() + procErrorMessage);
                 }
             } else {
                 throw new CommandException(exx.getMessage());
             }
         } finally {
             if (proc != null) {
                 proc.waitFor();
                 
                 try {
                     proc.getInputStream().close();
                 } catch (IOException ioe) {}
                 try {
                     proc.getErrorStream().close();
                 } catch (IOException ioe) {}
                 try {
                     proc.getOutputStream().close();
                 } catch (IOException ioe) {}
             }
 
             if(in != null) {
                 try {
                     in.close();
                 } catch (IOException ioe) {}
             }
             if(os != null) {
                 try {
                     os.close();
                 } catch (IOException ioe) {}
             }
             if(readErr != null) {
                 try {
                     readErr.close();
                 } catch (IOException ex) {}
             }
         }
     }
 
     protected Job makeJobFromCmd(Command cmd) throws CommandException {
     	String jdl = cmd.getParameterAsString("JDL");
     	if (jdl == null) {
             throw new CommandException("JDL not defined!");
         }
     	try {
             return JobFactory.makeJob(jdl);
         } catch (Throwable e) {
            logger.error(e.getMessage());
             throw new CommandException(e.getMessage());
         }
     }
 
     private void jobRegister(final Command cmd) throws CommandException, CommandExecutorException {
         logger.debug("BEGIN jobRegister");
 
         if (cmd == null) {
             throw new CommandException("command not defined!");
         }
         
         JobCommand jobCmd = new JobCommand(getCommandType(JobCommandConstant.JOB_REGISTER));
         jobCmd.setStatus(cmd.getStatus());
         jobCmd.setCreationTime(cmd.getCreationTime());
         jobCmd.setDescription(cmd.getDescription());
         jobCmd.setUserId(cmd.getUserId());
         jobCmd.setStartSchedulingTime(cmd.getStartProcessingTime());
 
         try {
             String userId = cmd.getUserId();
             
             if (userId == null) {
                 throw new CommandException("userId not defined!");
             }
 
             Job job = makeJobFromCmd(cmd);
             
             JobStatus status = new JobStatus(JobStatus.REGISTERED, job.getId());
 
             job.setUserId(userId);
             job.setLocalUser(cmd.getParameterAsString("LOCAL_USER"));
             job.setJDL(cmd.getParameterAsString("JDL"));
             job.setICEId(cmd.getParameterAsString("ICE_ID"));
             job.addCommandHistory(jobCmd);
             
             if (cmd.containsParameterKey("USER_VO")) {
                 job.setVirtualOrganization(cmd.getParameterAsString("USER_VO"));
             }
 
             if (isEmptyField(job.getBatchSystem())) {
                 throw new CommandException("\"BatchSystem\" attribute not defined into the JDL");
             }
 
             if (isEmptyField(job.getQueue())) {
                 throw new CommandException("\"QueueName\" attribute not defined into the JDL");
             }
 
             if (!isBatchSystemSupported(job.getBatchSystem())) {
                 throw new CommandException("Batch System " + job.getBatchSystem() + " not supported!");
             }
 
             String cream_sandbox_dir = getParameterValueAsString("CREAM_SANDBOX_DIR");
             if (cream_sandbox_dir == null) {
                 throw new CommandException("parameter \"CREAM_SANDBOX_DIR\" not defined!");
             }
 
             job.setCreamURL(cmd.getParameterAsString("CREAM_URL"));
             job.setDelegationProxyId(cmd.getParameterAsString("DELEGATION_PROXY_ID"));
             job.setDelegationProxyInfo(cmd.getParameterAsString("DELEGATION_PROXY_INFO"));
             job.setDelegationProxyCertPath(cmd.getParameterAsString("DELEGATION_PROXY_PATH"));
             job.setLRMSAbsLayerJobId(Job.NOT_AVAILABLE_VALUE);
             job.setLRMSJobId(Job.NOT_AVAILABLE_VALUE);
             job.setWorkerNode(Job.NOT_AVAILABLE_VALUE);
             job.setWorkingDirectory(Job.NOT_AVAILABLE_VALUE);
 
             if (cmd.containsParameterKey("USER_DN")) {
                 job.addExtraAttribute("USER_DN", cmd.getParameterAsString("USER_DN").replaceAll("\\s+", "\\\\ "));
             }
 
             if (cmd.containsParameterKey("USER_DN_X500")) {
                 job.addExtraAttribute("USER_DN_X500", cmd.getParameterAsString("USER_DN_X500").replaceAll("\\s+", "\\\\ "));
             }
 
             if (cmd.containsParameterKey("LOCAL_USER_GROUP")) {
                 job.addExtraAttribute("LOCAL_USER_GROUP", cmd.getParameterAsString("LOCAL_USER_GROUP"));
             }
 
             if (cmd.containsParameterKey("USER_FQAN")) {
                 List<String> fqanList = cmd.getParameterMultivalue("USER_FQAN");
                 
                 if (fqanList != null && fqanList.size() > 0) {                    
                     StringBuffer fqanBuffer = new StringBuffer();
                     
                     for (String fqan : fqanList) {
                         fqanBuffer.append("\\\"userFQAN=").append(fqan.replaceAll("\\s+", "\\\\ ")).append("\\\"\\ ");
                     }
 
                     fqanBuffer.deleteCharAt(fqanBuffer.length() - 1);
                     fqanBuffer.deleteCharAt(fqanBuffer.length() - 1);
                     
                     job.addExtraAttribute("USER_FQAN", fqanBuffer.toString());
                 }
             }
              
             if (this.containsParameterKey("LRMS_EVENT_LISTENER_PORT")) {
                 job.setLoggerDestURI(InetAddress.getLocalHost().getHostAddress() + ":" + getParameterValueAsString("LRMS_EVENT_LISTENER_PORT"));
             }
 
             if (job.getCreamURL() != null) {
                 try {
                     URL url = new URL(job.getCreamURL());
                     job.setCeId(url.getHost() + ":" + url.getPort() + "/cream-" + job.getBatchSystem() + "-" + job.getQueue());
                 } catch (MalformedURLException e) {
                 }
             }
 
             if (cmd.containsParameterKey("LEASE_ID")) {
                 String leaseId = cmd.getParameterAsString("LEASE_ID");
 
                 if (leaseId != null && leaseId.length() > 0) {
                     Lease lease = jobDB.retrieveJobLease(leaseId, userId);
                     if (lease != null) {
                         logger.debug("found lease \"" + leaseId + "\" = " + lease.getLeaseTime().getTime());
                         job.setLease(lease);
                     } else {
                         throw new CommandException("lease id \"" + leaseId + "\" not found!");
                     }
                 }
             }
 
             boolean jobInserted = false;
             int count = 0;
 
             while (!jobInserted && count < 5) {
                 try {
                     jobDB.insert(job);
                     jobInserted = true;
                 } catch (DatabaseException de) {
                     if (de.getMessage().indexOf("Duplicate entry") > -1) {
                         job.setId(job.generateJobId());
                         count++;
                     } else {
                         throw new CommandException(de.getMessage());
                     }
                 } catch (IllegalArgumentException ie) {
                     throw new CommandException(ie.getMessage());
                 }
             }
 
             if (!jobInserted) {
                 throw new CommandException("Duplicate jobId error: cannot insert the new job (" + job.getId() + ") into the database");
             }
 
             jobCmd.setJobId(job.getId());
             jobCmd.setStatus(JobCommand.SUCCESSFULL);
            
             if (LBLogger.isEnabled()) {
                 try {
                     LBLogger.getInstance().register(job);
                 } catch (Throwable e) {
                     logger.warn("LBLogger.register() failed: " + e.getMessage(),e );
                 }
 
                 try {
                     LBLogger.getInstance().accept(job);
                 } catch (Throwable e) {
                     logger.warn("LBLogger.accept() failed: " + e.getMessage(),e );
                 }
             }
 
             try {
                 createJobSandboxDir(job, cmd.getParameterAsString("GSI_FTP_CREAM_URL"));
             } catch (Throwable e) {
                 jobCmd.setStatus(JobCommand.ERROR);
                 jobCmd.setFailureReason(e.getMessage());
 
                 status.setType(JobStatus.ABORTED);
                 status.setFailureReason(e.getMessage());
 
                 doOnJobStatusChanged(status, job);
                 
                 throw new CommandException(e.getMessage());
             } finally {
                 jobDB.update(job);
                 jobDB.updateJobCommand(jobCmd);
             }
 
             doOnJobStatusChanged(status, job);
 
             String autostart = cmd.getParameterAsString("AUTOSTART");
             Boolean autostart_b = autostart != null ? new Boolean(autostart) : new Boolean(Boolean.FALSE);
 
             if (autostart_b.booleanValue()) {
                 cmd.addParameter("JOB", job);
 
                 jobStart(cmd);
             }
 
             cmd.getResult().addParameter("JOB", job);
         } catch (CommandException e) {
             logger.error(e.getMessage());
             throw e;
         } catch (Throwable e) {
             logger.error(e.getMessage(), e);
             throw new CommandExecutorException(e);
         } 
 
         logger.debug("END jobRegister");
     }
 
     private void purge(Job job) throws CommandException, IllegalArgumentException {
         if (job == null) {
             throw new IllegalArgumentException("job not defined!");
         }
         
         if (!isEmptyField(job.getWorkingDirectory())) {
             Process proc = null;
 
             try {
                 String[] cmd = new String[] { "sudo", "-S", "-n", "-u", job.getLocalUser(), getParameterValueAsString("CREAM_PURGE_SANDBOX_BIN_PATH"), job.getWorkingDirectory() };
 
                 proc = Runtime.getRuntime().exec(cmd);
             } catch (Throwable e) {
                     logger.error(e.getMessage());
             } finally {
                 if (proc != null) {
                     try {
                         proc.waitFor();
                     } catch (InterruptedException e) {
                     }
                     
                     StringBuffer errorMessage = null;
 
                     if(proc.exitValue() != 0) {
                         BufferedReader readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 
                         errorMessage = new StringBuffer();
                         String inputLine = null;
 
                         try {
                             while ((inputLine = readErr.readLine()) != null) {
                                 errorMessage.append(inputLine);
                             }
                         } catch (IOException ioe) {
                             logger.error(ioe.getMessage());
                         } finally {
                             try {
                                 readErr.close();
                             } catch (IOException ioe) {}
                         }
                         
                         if (errorMessage.length() > 0) {
                             errorMessage.append("\n");
                         }
                     }
 
                     try {
                         proc.getInputStream().close();
                     } catch (IOException ioe) {}
                     try {
                         proc.getErrorStream().close();
                     } catch (IOException ioe) {}
                     try {
                         proc.getOutputStream().close();
                     } catch (IOException ioe) {}
 
                     if(errorMessage != null) {
                         throw new CommandException(errorMessage.toString());
                     }
                 }
             }
         }
 
         try {
             jobDB.delete(job.getId(), job.getUserId());
             logger.info("purge: purged job " + job.getId());
         } catch (DatabaseException e) {
             logger.error(e.getMessage());
             throw new CommandException(e.getMessage());
         }
     }
 
     public void execute(List<Command> commandList) throws CommandExecutorException, CommandException {
         if (commandList == null) {
             return;
         }
 
         for (Command command : commandList) {
             execute(command);
         }
     }
 
     public abstract CommandResult submit(Job job) throws CommandException;
 
     public abstract void cancel(Job job) throws CommandException;
 
     public abstract void suspend(Job job) throws CommandException;
 
     public abstract void resume(Job job) throws CommandException;
 
     public abstract void renewProxy(Job job) throws CommandException;
 
     public abstract void renewProxy(Job job, boolean sendToWN) throws CommandException;
 
     public abstract boolean isBatchSystemSupported(String bs);
 
 
     public boolean doOnJobStatusChanged(JobStatus status) throws IllegalArgumentException, JobManagementException {
         Job job = null;
         try {
             job = jobDB.retrieveJob(status.getJobId(), null);
         } catch (Exception e) {
          //   logger.error(e.getMessage());
 
             logger.warn("job " + status.getJobId() + " not found!");
             return false;
             
           //  throw new JobManagementException("job " + status.getJobId() + " not found! " + e.getMessage());
         }
 
         return doOnJobStatusChanged(status, job);
     }
     
     private boolean doOnJobStatusChanged(JobStatus status, Job job) throws IllegalArgumentException, JobManagementException {
         boolean statusUpdated = false;
 
         if (status == null) {
             throw new IllegalArgumentException("job status not defined!");
         }
 
         if (status.getType() == JobStatus.PURGED) {
             return statusUpdated;
         }
 
         if (job == null) {
             logger.warn("job " + status.getJobId() + " not found!");
             return false;
         }
       
         JobStatus lastStatus = job.getLastStatus();
 /*
         if (lastStatus == null) {
             throw new JobManagementException("job status " + status.getJobId() + " not found!");
         }
 */
         if (lastStatus != null && status.getType() == lastStatus.getType()) {
             if (lastStatus.getName().startsWith("DONE")) {
                 try {
                     if (!"W".equalsIgnoreCase(lastStatus.getExitCode())) {
                         status.setExitCode(lastStatus.getExitCode());
                     }
 
                     if (status.getFailureReason() != null) {
                         if (lastStatus.getFailureReason() != null &&
                                 !lastStatus.getFailureReason().equals(job.NOT_AVAILABLE_VALUE) &&
                                 !status.getFailureReason().equals(lastStatus.getFailureReason())) {
                             status.setFailureReason(lastStatus.getFailureReason() + "; " + status.getFailureReason());
                         } else {
                             status.setFailureReason(status.getFailureReason());
                         }
                     }
 
                     status.setId(lastStatus.getId());
 
                     jobDB.updateStatus(status, null);
 
                     statusUpdated = true;
 
                     if (LBLogger.isEnabled()) {
                         try {
                             LBLogger.getInstance().statusChanged(job, status, null, LBLogger.START);
                         } catch (Exception e) {
                             logger.warn("LBLogger.statusChanged failed: " + e.getMessage());
                         }
                     }
 
                     logger.info("JOB " + status.getJobId() + " STATUS UPDATED: " + status.getName());
 
                     try {
                         sendNotification(job);
                     } catch (Throwable e) {
                         logger.error(e.getMessage());
                     }
                 } catch (IllegalArgumentException e) {
                     logger.error(e);
                     throw new JobManagementException(e);
                 } catch (DatabaseException e) {
                     logger.error(e);
                     throw new JobManagementException(e);
                 }
             }
         } else {
             if (lastStatus != null && (lastStatus.getType() == JobStatus.ABORTED || lastStatus.getType() == JobStatus.CANCELLED ||
                     lastStatus.getType() == JobStatus.DONE_OK || lastStatus.getType() == JobStatus.DONE_FAILED)) {
                 return statusUpdated;
             }
 
             switch (status.getType()) {
             case JobStatus.ABORTED:
                 setLeaseExpired(job);
                 break;
 
             case JobStatus.CANCELLED:
                 status.setDescription("Cancelled by CE admin");
                 int cancelType = getCommandType(JobCommandConstant.JOB_CANCEL);
 
                 for (int i = job.getCommandHistoryCount() - 1; i >= 0; i--) {
                     if (job.getCommandHistoryAt(i).getType() == cancelType) {
                         status.setDescription(job.getCommandHistoryAt(i).getDescription());
                         break;
                     }
                 }
                 setLeaseExpired(job);
                 break;
 
             case JobStatus.DONE_OK:
             case JobStatus.DONE_FAILED:
                 if (status.getType() == JobStatus.DONE_FAILED) {
                     int cancelledType = getCommandType(JobCommandConstant.JOB_CANCEL); 
 
                     for (int i = job.getCommandHistoryCount() - 1; i >= 0; i--) {
                         if (job.getCommandHistoryAt(i).getType() == cancelledType) {                         
                             status.setType(JobStatus.CANCELLED);
                             status.setExitCode(null);
                             status.setDescription(job.getCommandHistoryAt(i).getDescription());
                             break;
                         }
                     }                  
                 }
                 
                 if ("W".equalsIgnoreCase(status.getExitCode())) {
                     Calendar time = Calendar.getInstance();
                     time.add(Calendar.MINUTE, 1);
                     timer.schedule(new GetSTDTask(status.getJobId()), time.getTime());
                     timer.purge();
                 }               
 
                 setLeaseExpired(job);
                 break;
 
             case JobStatus.REALLY_RUNNING:
                 if (lastStatus != null && (lastStatus.getType() == JobStatus.ABORTED || lastStatus.getType() == JobStatus.CANCELLED ||
                         lastStatus.getType() == JobStatus.DONE_OK || lastStatus.getType() == JobStatus.DONE_FAILED)) {
                     return statusUpdated;
                 }
                 break;
 
             case JobStatus.RUNNING:
                 if (status.getTimestamp().compareTo(lastStatus.getTimestamp()) <= 0) {
                     return statusUpdated;
                 }
 
                 if (lastStatus != null && (lastStatus.getType() == JobStatus.REALLY_RUNNING || lastStatus.getType() == JobStatus.ABORTED ||
                         lastStatus.getType() == JobStatus.CANCELLED || lastStatus.getType() == JobStatus.DONE_OK || lastStatus.getType() == JobStatus.DONE_FAILED)) {
                     return statusUpdated;
                 }
                 try {
                     List<JobStatus> statusList = jobDB.retrieveJobStatusHistory(status.getJobId(), null);
 
                     if (statusList != null && statusList.size() > 2) {
                         JobStatus oldStatus = statusList.get(statusList.size() - 2);
                         status.setType(oldStatus.getType() == JobStatus.REALLY_RUNNING ? JobStatus.REALLY_RUNNING : JobStatus.RUNNING);
                     }
                 } catch (DatabaseException e) {
                     throw new JobManagementException(e);
                 }
                 break;
 
             case JobStatus.HELD:
             case JobStatus.IDLE:
             case JobStatus.PENDING:
             case JobStatus.REGISTERED:
                 if (lastStatus != null && (status.getTimestamp().compareTo(lastStatus.getTimestamp()) < 0)) {
                     logger.warn("the timestamp of the new status " + status.getName() + " for the JOB " + status.getJobId() + " is older than the last one");
                     return statusUpdated;
                 }
 
                 break;
             }
 
             try {
                 job.addStatus(status);
 
                 logger.debug("inserting new status " + status.getName() + " for the JOB " + status.getJobId());
                 jobDB.insertStatus(status, null);
 
                 statusUpdated = true;
 
                 if (LBLogger.isEnabled()) {
                     try {
                         LBLogger.getInstance().statusChanged(job, status, null, LBLogger.START);
                     } catch (Exception e) {
                         logger.warn("LBLogger.statusChanged failed: " + e.getMessage());
                     }
                 }
 
                 StringBuffer logInfo = new StringBuffer("JOB ");
                 logInfo.append(status.getJobId());
 
                 if (lastStatus == null) {
                     logInfo.append(" STATUS CHANGED: -- => ").append(status.getName());
                 } else {
                     logInfo.append(" STATUS CHANGED: ").append(lastStatus.getName()).append(" => ").append(status.getName());
                 }
 
                 if (!isEmptyField(status.getDescription())) {
                     logInfo.append(" [description=").append(status.getDescription()).append("]");
                 }
 
                 if (!isEmptyField(status.getFailureReason())) {
                     logInfo.append(" [failureReason=" + status.getFailureReason() + "]");
                 } 
 
                 if (!isEmptyField(status.getExitCode())) {
                     logInfo.append(" [exitCode=").append(status.getExitCode()).append("]");
                 }
 
                 if (!isEmptyField(job.getLocalUser())) {
                     logInfo.append(" [localUser=" + job.getLocalUser() + "]");
                 }
 
                 if (!isEmptyField(job.getGridJobId())) {
                     logInfo.append(" [gridJobId=" + job.getGridJobId() + "]");
                 }
 
                 if (!isEmptyField(job.getLRMSJobId())) {
                     logInfo.append(" [lrmsJobId=").append(job.getLRMSJobId()).append("]");
                 }
 
                 if (!isEmptyField(job.getWorkerNode())) {
                     logInfo.append(" [workerNode=").append(job.getWorkerNode()).append("]");
                 }
 
                 if (!isEmptyField(job.getDelegationProxyId())) {
                     logInfo.append(" [delegationId=").append(job.getDelegationProxyId()).append("]");
                 }
 
                 logger.info(logInfo.toString());
             } catch (IllegalArgumentException e) {
                 logger.error(e);
                 throw new JobManagementException(e);
             } catch (DatabaseException e) {
                 logger.error(e);
                 throw new JobManagementException(e);
             }
 
             try {
                 sendNotification(job);
             } catch (Throwable e) {
                 logger.error(e.getMessage());
             }
         }
 
         if (LBLogger.isEnabled() && statusUpdated) {
             try {
                 LBLogger.getInstance().statusChanged(job, status, lastStatus, LBLogger.OK);
             } catch (Exception e) {
                 logger.warn("LBLogger.statusChanged failed: " + e.getMessage());
             }
         }
 
         return statusUpdated;
     }
 
     private class GetSTDTask extends TimerTask {
         private String jobId = null;
 
         public GetSTDTask(String jobId) {
             this.jobId = jobId;
         }
 
         public void run() {
             Job job = null;
             try {
                 job = jobDB.retrieveJob(jobId, null);
             } catch (IllegalArgumentException e) {
                 logger.error("GetSTDTask - IllegalArgumentException: " + e.getMessage());
                 return;
             } catch (DatabaseException e) {
                 logger.error("GetSTDTask - DatabaseException: " + e.getMessage());
                 return;
             }
 
             if (job == null) {
                 return;
             }
 
             JobStatus lastStatus = job.getLastStatus();
             if (lastStatus == null) {
                 return;
             }
 
             boolean update = false;
 
             if ("W".equalsIgnoreCase(lastStatus.getExitCode())) {
                 try {
                     String exitCode = getExitCode(job.getWorkingDirectory() + "/StandardOutput", job.getLocalUser());
                     lastStatus.setExitCode(exitCode);
                 } catch (Exception e) {
                     lastStatus.setExitCode(Job.NOT_AVAILABLE_VALUE);
                 } finally {
                     update = true;
                 }
             }
 
             if (lastStatus.getType() == JobStatus.DONE_FAILED || lastStatus.getType() == JobStatus.CANCELLED) {
                 try {
                     String stdErrorMessage = readFile(job.getWorkingDirectory() + "/StandardError", job.getLocalUser());
                     String errorMessage = null;
                     if (stdErrorMessage != null && !stdErrorMessage.equals("")) {
                         errorMessage = lastStatus.getFailureReason();
                         
                         if (errorMessage != null && !stdErrorMessage.equals(stdErrorMessage)) {
                             errorMessage += "; " + stdErrorMessage;
                         } else {
                             errorMessage = stdErrorMessage;
                         }    
                     } else {
                         errorMessage = Job.NOT_AVAILABLE_VALUE;
                     }
 
                     lastStatus.setFailureReason(errorMessage);
                 } catch (Exception e) {
                     if (lastStatus.getFailureReason() == null || lastStatus.getFailureReason().length() == 0) {
                         lastStatus.setFailureReason(Job.NOT_AVAILABLE_VALUE);
                     }
                 } finally {
                     update = true;
                 }
             }
 
             if(update) {
                 Command statusCmd = new Command(JobCommandConstant.SET_JOB_STATUS, getCategory());
                 //statusCmd.setCommandExecutorName(blahExec.getName());
                 statusCmd.setAsynchronous(true);
                 statusCmd.setUserId("admin");
                 statusCmd.addParameter("JOB_ID", lastStatus.getJobId());
                 statusCmd.addParameter("STATUS_TYPE", ""+lastStatus.getType());
                // statusCmd.addParameter("STATUS_CHANGE_TIME", lastStatus.getTimestamp());
                // statusCmd.addParameter("WORKER_NODE", workerNode);
                // statusCmd.addParameter("LRMS_JOB_ID", batchJobId);
                 statusCmd.addParameter("IS_ADMIN", "true");
                 statusCmd.addParameter("EXIT_CODE", lastStatus.getExitCode());
                 statusCmd.addParameter("FAILURE_REASON", lastStatus.getFailureReason());
                 statusCmd.setPriorityLevel(Command.MEDIUM_PRIORITY);
                 statusCmd.setExecutionMode(Command.ExecutionModeValues.SERIAL);
                 statusCmd.setCommandGroupId(lastStatus.getJobId());
 
                 if (lastStatus.getTimestamp() != null) {
                     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
                     statusCmd.addParameter("STATUS_CHANGE_TIME", dateFormat.format(lastStatus.getTimestamp().getTime()));
                 }
 
                 try {
                     getCommandManager().execute(statusCmd);
                 } catch (Throwable e) {
                     logger.error(e.getMessage());
                 }
 /*
                 try {
                     doOnJobStatusChanged(lastStatus, job);
                 } catch (IllegalArgumentException e) {
                     logger.error(e);
                 } catch (JobManagementException e) {
                     logger.error(e);
                 }
 */
             }
         }
     }
 
     private String getExitCode(String filePath, String userId) throws IllegalArgumentException, Exception {
         String s = readFile(filePath, userId);
         if (s == null) {
             return Job.NOT_AVAILABLE_VALUE;
         }
 
         String pattern = "job exit status = ";
         int index = s.indexOf(pattern);
         if (index > -1) {
             s = s.substring(index + pattern.length());
             s = s.substring(0, s.indexOf(" "));
             s = s.trim();
         } else {
             s = Job.NOT_AVAILABLE_VALUE;
         }
         return s;
     }
 
     private String readFile(String stdErrorFilePath, String userId) throws IllegalArgumentException, InterruptedException, IOException {
         String message = "";
 
         if (stdErrorFilePath == null) {
             throw (new IllegalArgumentException("stdErrorFilePath not specified"));
         }
 
         if (userId == null) {
             throw (new IllegalArgumentException("userId not specified!"));
         }
 
         String[] cmd = new String[] { "sudo", "-S", "-n", "-u", userId, "/bin/cat", stdErrorFilePath };
 
         Process proc = null;
         BufferedReader readIn = null;
         
         try {
             proc = Runtime.getRuntime().exec(cmd);
 
             readIn = new BufferedReader(new InputStreamReader(proc.getInputStream()));
             String strLine = null;
             
             while ((strLine = readIn.readLine()) != null) {
                 message += strLine + " ";
             }
         } catch (Throwable e) {
             if (proc != null) {
                 proc.destroy();
             }
         } finally {
             if (proc != null) {
                 proc.waitFor();
                 
                 StringBuffer errorMessage = null;
 
                 if(proc.exitValue() != 0) {
                     BufferedReader readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 
                     errorMessage = new StringBuffer();
                     String inputLine = null; 
 
                     try {
                         while ((inputLine = readErr.readLine()) != null) {
                             errorMessage.append(inputLine);
                         }
                     } catch (IOException ioe) {
                         logger.error(ioe.getMessage());
                     } finally {
                         readErr.close();                        
                     }
 
                     if (errorMessage.length() > 0) {
                         errorMessage.append("\n");
                     }
                 }
 
                 try {
                     proc.getInputStream().close();
                 } catch (IOException ioe) {}
                 try {
                     proc.getErrorStream().close();
                 } catch (IOException ioe) {}
                 try {
                     proc.getOutputStream().close();
                 } catch (IOException ioe) {}
                 
                 if(errorMessage != null) {
                     throw new IOException(errorMessage.toString());
                 }
             }
             
             try {
                 readIn.close();
             } catch (IOException ioe) {}
         }
 
         if (message.length() == 0) {
             throw (new FileNotFoundException("file \"" + stdErrorFilePath + " not found!"));
         }
 
         return message.trim();
     }
 
     public JobDBInterface getJobDB() {
         return jobDB;
     }
 
 //    public void setDelegationManager(DelegationManagerInterface delegationManager) throws IllegalArgumentException {
 //        this.delegationManager = delegationManager;
 //        if (delegationManager != null) {
 //            try {
 //                delegationSuffix = delegationManager.getDelegationSuffix();
 //            } catch (Exception ex) {
 //                logger.error("setDelegationManager error: cannot retrieve the delegationSuffix from db! " + ex.getMessage());
 //            }
 //        }
 //    }
     
     public void setJobDB(JobDBInterface jobDB) throws IllegalArgumentException {
         if (jobDB == null) {
             throw new IllegalArgumentException("jobDB not specified!");
         }
         
         this.jobDB = jobDB;
 
         LBLogger.setJobDB(jobDB);
  
         EventManagerFactory.addEventManager("JOB_STATUS_EVENT_MANAGER", new JobStatusEventManager(jobDB, 500));
         
         try {
             Job job = null;
             List<String> jobPendingList = jobDB.retrieveJobId(new int[] { JobStatus.PENDING }, null, null, null);
 
             for (String jobId : jobPendingList) {
                 job = jobDB.retrieveJob(jobId, null);
 
                 if (job != null && (job.getLRMSAbsLayerJobId() == null || Job.NOT_AVAILABLE_VALUE.equals(job.getLRMSAbsLayerJobId()))) {
                     if (isEmptyField(job.getLRMSAbsLayerJobId())) {
                         if (job.getCommandHistoryCount() > 1) {
                             JobCommand cmd = job.getCommandHistoryAt(1);
                             cmd.setStatus(JobCommand.ABORTED);
                             cmd.setFailureReason("command aborted because its execution has been interrupted by the CREAM shutdown");
 
                             if ("ADMINISTRATOR".equals(cmd.getUserId())) {
                                 cmd.setUserId(job.getUserId());
                             }
 
                             jobDB.updateJobCommand(cmd);
                             logger.info(cmd.toString());
                         }
 
                         JobStatus status = job.getLastStatus();
                         status.setType(JobStatus.ABORTED);
                         status.setFailureReason("job aborted because the execution of the JOB_START command has been interrupted by the CREAM shutdown");
                         status.setTimestamp(job.getLastCommand().getExecutionCompletedTime());
 
                         jobDB.updateStatus(status, null);
                         logger.info("job " + job.getId() + " aborted because the execution of the JOB_START command has been interrupted by the CREAM shutdown");
 
                         try {
                             sendNotification(job);
                         } catch (JobManagementException e) {
                             logger.error(e.getMessage());
                         }
                     }
                 }
             }
 
             jobDB.updateAllUnterminatedJobCommand();
         } catch (DatabaseException e) {
             logger.error(e.getMessage());
         }
 
         int maxLeaseTime = 10800; // 3 hours
         int leaseExecutionRate = 30; // 30 minutes
 
         if (containsParameterKey("MAX_LEASE_TIME")) {
             try {
                 maxLeaseTime = Integer.parseInt(getParameterValueAsString("MAX_LEASE_TIME"));
             } catch (NumberFormatException e) {
                 logger.warn("MAX_LEASE_TIME parameter has a wrong value: " + getParameterValueAsString("MAX_LEASE_TIME") + " => using the default one: " + maxLeaseTime);
             }
         }
 
         if (containsParameterKey("LEASE_EXECUTION_RATE")) {
             try {
                 leaseExecutionRate = Integer.parseInt(getParameterValueAsString("LEASE_EXECUTION_RATE"));
             } catch (NumberFormatException e) {
                 logger.warn("LEASE_EXECUTION_RATE parameter has a wrong value: " + getParameterValueAsString("LEASE_EXECUTION_RATE") + " => using the default one: " + leaseExecutionRate);
             }
         }
 
         leaseManager = new LeaseManager(this, maxLeaseTime);
         timer.schedule(leaseManager, 0, leaseExecutionRate*60000);
         logger.info("LeaseManager started [maxLeaseTime=" + maxLeaseTime + "; leaseExecutionRate=" + leaseExecutionRate + "]");
 
         if(containsParameterKey("JOB_PURGE_POLICY")) {
             int jobPurgeRate = JOB_PURGE_RATE_DEFAULT;
             if(containsParameterKey("JOB_PURGE_RATE")) {
                 try {
                     jobPurgeRate = Integer.parseInt(getParameterValueAsString("JOB_PURGE_RATE"));
                 } catch (NumberFormatException nfe) {
                     jobPurgeRate = JOB_PURGE_RATE_DEFAULT;
                 }
             }
 
             jobPurger = new JobPurger(this, getParameterValueAsString("JOB_PURGE_POLICY"), jobPurgeRate);
         }
 
         String disableSubmissionPolicy = getParameterValueAsString(JOB_SUBMISSION_MANAGER_ENABLE);
         if((disableSubmissionPolicy != null ) && ("TRUE".equals(disableSubmissionPolicy.toUpperCase()))) {
             JobSubmissionManager.setGliteCreamLoadMonitorScriptPath(getParameterValueAsString(JOB_SUBMISSION_MANAGER_SCRIPT_PATH));
             //jobSubmissionManager = new JobSubmissionManager(getParameterValueAsString(JOB_SUBMISSION_MANAGER_SCRIPT_PATH));
             jobSubmissionManager = JobSubmissionManager.getInstance();
             timer.schedule(jobSubmissionManager, 300000, 600000); // task executed every 10 minutes, 5 minutes after the LeaseManager task
         }
     }
 
     private void setLeaseExpired(Job job) {
         if (job == null || job.getLease() == null) {
             return;
         }
 
         try {
             jobDB.setLeaseExpired(job.getId(), job.getLease());
         } catch (Throwable e) {
             logger.error("setLeaseExpired: " + e.getMessage());
         }
     }
 
     /**
      * Initialises the socket connection.
      * 
      * @throws JobManagementException
      */
     private void initSocket(String sensorHost, String sensorPort) throws JobManagementException {
         if (socket == null) {
             if (sensorHost == null) {
                 throw new JobManagementException("CREAM_JOB_SENSOR_HOST parameter not specified!");
             }
 
             if (sensorPort == null) {
                 throw new JobManagementException("CREAM_JOB_SENSOR_PORT parameter not specified!");
             }
 
             logger.debug("initSocket: CREAM_JOB_SENSOR_HOST = " + sensorHost + " CREAM_JOB_SENSOR_PORT = " + sensorPort);
             // Create a socket object for communicating
             try {
                 int sensorPortNumber = Integer.parseInt(sensorPort);
                 socket = new Socket();
                 socket.connect(new InetSocketAddress(sensorHost, sensorPortNumber), 500);
                 oos = new ObjectOutputStream(socket.getOutputStream());
             } catch (Exception e) {
                 throw new JobManagementException(e.getMessage());
             }
             logger.info("initSocket: created socket for host=" + sensorHost + ":" + sensorPort);
         }
     }
 
     /**
      * Sends a notification to the server serializing current <code>Job</code>
      * object.
      * 
      * @param job
      *            The Job to be notified.
      * @throws JobManagementException
      */
     protected void sendNotification(Job job) throws JobManagementException {
         String sensorHost = getParameterValueAsString(CREAM_JOB_SENSOR_HOST);
         String sensorPort = getParameterValueAsString(CREAM_JOB_SENSOR_PORT);
 
         if ((job == null) || (sensorHost == null) || (sensorPort == null)){
            return;
         }
 
         if (socket == null) {
             initSocket(sensorHost, sensorPort);
         }
 
         if (socket.isConnected()) {
             synchronized(socket) {
                 try {
                     logger.debug("sendNotification: Retrieved job for jobId=" + job.getId());
                     job.writeExternal(oos);
                     logger.debug("sendNotification: writeExternal perfomed");
                     oos.flush();
                 } catch (Throwable e) {
                     logger.error("sendNotification error: " + e.getMessage());
                     if (socket != null && !socket.isClosed()) {
                         try {
                             socket.close();
                         } catch (IOException e1) {
                             throw new JobManagementException(e1.getMessage());
                         }
                     }
                     socket = null;
 
                     if (oos != null) {
                         try {
                             oos.close();
                         } catch (IOException e1) {
                             throw new JobManagementException(e1.getMessage());
                         }
                     }
                 } finally {
                     if(socket != null) {
                         socket.notifyAll();
                     }
                 }
             }
         } else {
             logger.warn("sendNotification: the socket is NOT connected");
             if (socket != null && !socket.isClosed()) {
                 try {
                     socket.close();
                 } catch (IOException e) {
                     throw new JobManagementException(e.getMessage());
                 }
             }
             socket = null;
 
             if (oos != null) {
                 try {
                     oos.close();
                 } catch (IOException e) {
                     throw new JobManagementException("socket is not connected");
                 }
             }
         }
     }
 }

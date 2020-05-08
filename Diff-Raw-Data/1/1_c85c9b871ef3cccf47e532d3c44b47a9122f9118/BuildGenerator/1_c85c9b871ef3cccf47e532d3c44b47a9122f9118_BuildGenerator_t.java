 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-4-27
  * Time: 6:22:18
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import ognl.OgnlException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.util.DOMElementWriter;
 import org.apache.tools.ant.util.DateUtils;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.quartz.Scheduler;
 import org.quartz.StatefulJob;
 import org.w3c.dom.Element;
 
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.dependency.DependencyResolver;
 import com.luntsys.luntbuild.facades.BuildParams;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.listeners.Listener;
 import com.luntsys.luntbuild.notifiers.Notifier;
 import com.luntsys.luntbuild.reports.Report;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.services.QuartzService;
 import com.luntsys.luntbuild.utility.EmptyBuildListenerImpl;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.LuntbuildLogger;
 import com.luntsys.luntbuild.utility.OgnlHelper;
 import com.luntsys.luntbuild.utility.Revisions;
 import com.luntsys.luntbuild.vcs.Vcs;
 
 /**
  * This class performs the actual build process and will generate
  * new build or rebuild existing builds upon the trigger of a <code>Schedule</code>.
  *
  * @author robin shine
  * @see Schedule
  */
 public class BuildGenerator implements StatefulJob {
     /**
      * Keep tracks of version of this class, used when do serialization-deserialization
      */
     static final long serialVersionUID = 1;
     /** File name of the plain text revision log. */
     public static final String REVISION_LOG = "revision_log.txt";
     /** File name of the XML revision log. */
     public static final String REVISION_XML_LOG = "revision_log.xml";
     /** File name of the HTML revision log. */
     public static final String REVISION_HTML_LOG = "revision_log.html";
     /** File name of the plain text build log. */
     public static final String BUILD_LOG = "build_log.txt";
     /** File name of the HTML build log. */
     public static final String BUILD_XML_LOG = "build_log.xml";
     /** File name of the HTML build log. */
     public static final String BUILD_HTML_LOG = "build_log.html";
 
     private static Log logger = LogFactory.getLog(BuildGenerator.class);
 
     // Trigger group for different purposes
     /**
      * Triggered manually
      */
     public static final String MANUALBUILD_GROUP = "manual build group";
     /**
      * Triggered through rebuild command
      */
     public static final String REBUILD_GROUP = "rebuild group";
     /**
      * Triggered through dependency check
      */
     public static final String DEPENDENT_GROUP = "dependent group";
 
     /**
      * The global lock used when evaluating version values
      */
     public static final Object versionLock = new Object();
 
     /**
      * Triggered by Quartz to execute actual building process.
      *
      * @param context job context
      * @throws JobExecutionException if execution fails
      */
     public void execute(JobExecutionContext context) throws JobExecutionException {
         SecurityHelper.runAsSiteAdmin();
         if (context.getJobDetail().getName().equals(QuartzService.DUMMY_JOB_NAME) ||
                 context.getTrigger().getName().equals(QuartzService.DUMMY_TRIGGER_NAME))
             return; // if this is only DUMMY job or trigger used to help other operations
 
         String triggerGroup = context.getTrigger().getGroup();
 
         if (triggerGroup.equals(Scheduler.DEFAULT_GROUP)) { // build request triggered by schedules
             long scheduleId = new Long(context.getTrigger().getName()).longValue();
             com.luntsys.luntbuild.facades.BuildParams buildParams = new BuildParams();
             Schedule schedule = Luntbuild.getDao().loadSchedule(scheduleId);
             buildParams = schedule.getBuildParams();
 
             new Thread(new BuildDependencyWalker(buildParams)).start();
             return;
         }
 
         if (triggerGroup.equals(MANUALBUILD_GROUP)) { // triggered manually
             BuildParams buildParams = Schedule.parseTriggerName(context.getTrigger().getName());
             Luntbuild.getSchedService().
                 removeUnNecessaryManualTriggers(Luntbuild.getDao().loadSchedule(buildParams.getScheduleId()));
             new Thread(new BuildDependencyWalker(
                     Schedule.parseTriggerName(context.getTrigger().getName()))).start();
             return;
         }
 
         // create a ant antProject used to receive ant task logs
         org.apache.tools.ant.Project antProject = Luntbuild.createAntProject();
         Log4jBuildListener log4jBuildListener = new Log4jBuildListener();
 
         // re-direct ant logs to log4j logger
         antProject.addBuildListener(log4jBuildListener);
         Schedule schedule = null;
         int notifyStrategy = Constants.NOTIFY_IF_FAILED;
         Revisions revisions = new Revisions();
         try {
             Build currentBuild = null;
 
             if (triggerGroup.equals(DEPENDENT_GROUP)) { // triggered by dependent resolving
                 com.luntsys.luntbuild.facades.BuildParams buildParams =
                     Schedule.parseTriggerName(context.getTrigger().getName());
                 schedule = Luntbuild.getDao().loadSchedule(buildParams.getScheduleId());
                 // Reload project to initialize project lazy collection members
                 schedule.setProject(Luntbuild.getDao().loadProject(schedule.getProject().getId()));
 
                 schedule.validateAtBuildTime();
                 notifyStrategy = buildParams.getNotifyStrategy();
                 // update status of this schedule
                 schedule.setStatus(Constants.SCHEDULE_STATUS_RUNNING);
                 schedule.setStatusDate(new Date());
                 schedule.setCurrentThread(Thread.currentThread());
                 Luntbuild.getDao().saveSchedule(schedule);
 
                 String workingDir = schedule.getWorkDirRaw();
                 if (!new File(workingDir).exists()) {
                     buildParams.setBuildType(Constants.BUILD_TYPE_CLEAN);
                     Luntbuild.createDir(workingDir);
                 }
 
                 setupOgnlHelper(schedule, antProject);
                 if (isBuildNecessary(buildParams.getBuildNecessaryCondition())) {
                     revisions = getRevisions();
                     currentBuild = new Build();
                     currentBuild.setSchedule(schedule);
                     currentBuild.setStartDate(new Date());
                     currentBuild.setStatus(Constants.BUILD_STATUS_RUNNING);
                     currentBuild.setRebuild(false);
                     currentBuild.setBuildType(buildParams.getBuildType());
                     JobDataMap dMap = context.getJobDetail().getJobDataMap();
                     currentBuild.setUser((dMap != null)?dMap.getString("USER"):null);
 
                     synchronized (versionLock) {
                         OgnlHelper.setAntProject(antProject);
                         OgnlHelper.setTestMode(false);
                         Schedule scheduleUpToDate = Luntbuild.getDao().loadSchedule(schedule.getId());
                         // Reload project to initialize project lazy collection members
                         scheduleUpToDate.setProject(Luntbuild.getDao().loadProject(scheduleUpToDate.getProject().getId()));
                         if (!buildParams.getBuildVersion().equals("")) {
                             currentBuild.setVersion(Luntbuild.evaluateExpression(scheduleUpToDate,
                                     buildParams.getBuildVersion()));
                         } else {
                             currentBuild.setVersion(Luntbuild.evaluateExpression(scheduleUpToDate,
                                     scheduleUpToDate.getNextVersion()));
                             if (!Luntbuild.isVariablesContained(scheduleUpToDate.getNextVersion())) {
                                 scheduleUpToDate.setNextVersion(
                                         Luntbuild.increaseBuildVersion(scheduleUpToDate.getNextVersion()));
                                 Luntbuild.getDao().saveSchedule(scheduleUpToDate);
                             }
                         }
                         scheduleUpToDate.setNextVersionValue(currentBuild.getVersion());
                         Luntbuild.getDao().saveSchedule(scheduleUpToDate);
                         currentBuild.setSchedule(scheduleUpToDate);
                     }
 
                     currentBuild.setLabelStrategy(buildParams.getLabelStrategy());
                     currentBuild.setPostbuildStrategy(buildParams.getPostbuildStrategy());
                     currentBuild.setVcsList(deriveBuildTimeVcsList(schedule.getProject().getVcsList(),
                             antProject));
                     currentBuild.setBuilderList(deriveBuildTimeBuilderList(schedule.getAssociatedBuilders(),
                             currentBuild, antProject));
                     currentBuild.setPostbuilderList(deriveBuildTimeBuilderList(
                             schedule.getAssociatedPostbuilders(), currentBuild, antProject));
                     logger.info("Perform build in \"" + currentBuild.getSchedule().getProject().getName() +
                             "/" + currentBuild.getSchedule().getName() + "\"...");
                 } else
                     logger.info("Build necessary condition not met, build not performed!");
             } else { // triggered by rebuild
                 revisions.getChangeLogs().add("========== Change log ignored: rebuild performed ==========");
                 String triggerName = context.getTrigger().getName();
                 String[] fields = triggerName.split("\\" + Luntbuild.TRIGGER_NAME_SEPERATOR);
                 long buildId = new Long(fields[0]).longValue();
                 currentBuild = Luntbuild.getDao().loadBuild(buildId);
                 schedule = currentBuild.getSchedule();
                 // reload project to initialize project lazy members
                 schedule.setProject(Luntbuild.getDao().loadProject(schedule.getProject().getId()));
                 notifyStrategy = new Integer(fields[1]).intValue();
                 currentBuild.setPostbuildStrategy(new Integer(fields[2]).intValue());
                 // update status of this schedule
                 schedule.setStatus(Constants.SCHEDULE_STATUS_RUNNING);
                 schedule.setStatusDate(new Date());
                 Luntbuild.getDao().saveSchedule(schedule);
 
                 String workingDir = schedule.getWorkDirRaw();
                 // create working directory if it does not exist
                 Luntbuild.createDir(workingDir);
 
                 if (!currentBuild.isHaveLabelOnHead())
                     throw new IllegalStateException("ERROR: this build should not be " +
                             "rebuildable because no label was created for this build when " +
                             "it was initially built");
                 currentBuild.setStatus(Constants.BUILD_STATUS_RUNNING);
                 currentBuild.setRebuild(true);
                 currentBuild.setStartDate(new Date());
                 currentBuild.setEndDate(null);
                 logger.info("Perform rebuild for \"" + currentBuild.getSchedule().getProject().getName() +
                         "/" + currentBuild.getSchedule().getName() + "/" + currentBuild.getVersion() + "\"");
             }
 
             if (currentBuild != null) {
                 Build lastBuild = Luntbuild.getDao().loadLastBuild(schedule);
                 // save build to database
                 Luntbuild.getDao().saveBuild(currentBuild);
                 writeRevisionLog(currentBuild, revisions);
                 Iterator it = Luntbuild.listeners.values().iterator();
                 while (it.hasNext()) {
                     Listener listener = (Listener) it.next();
                     try {
                         listener.buildStarted(currentBuild);
                     } catch (Throwable throwable) {
                         logger.error("Error calling build listener", throwable);
                     }
                 }
 
                 checkoutAndBuild(currentBuild);
 
                 Luntbuild.getDao().saveBuild(currentBuild);
                 it = Luntbuild.listeners.values().iterator();
                 while (it.hasNext()) {
                     Listener listener = (Listener) it.next();
                     try {
                         listener.buildFinished(currentBuild);
                     } catch (Throwable throwable) {
                         logger.error("Error calling build listener", throwable);
                     }
                 }
 
                 // send notifications about current build
                 if (notifyStrategy == Constants.NOTIFY_ALWAYS) {
                     sendBuildNotification(currentBuild, revisions.getChangeLogins(), antProject);
                 } else if (notifyStrategy == Constants.NOTIFY_IF_SUCCESS) {
                     if (currentBuild.getStatus() ==
                         Constants.BUILD_STATUS_SUCCESS)
                         sendBuildNotification(currentBuild, revisions.getChangeLogins(), antProject);
                 } else if (notifyStrategy == Constants.NOTIFY_IF_FAILED) {
                     if (currentBuild.getStatus() ==
                         Constants.BUILD_STATUS_FAILED)
                         sendBuildNotification(currentBuild, revisions.getChangeLogins(), antProject);
                 } else if (notifyStrategy == Constants.NOTIFY_WHEN_STATUS_CHANGED) {
                     // notify when build status changes
                     if (lastBuild == null || currentBuild.getStatus() != lastBuild.getStatus())
                         sendBuildNotification(currentBuild, revisions.getChangeLogins(), antProject);
                 } else if (notifyStrategy == Constants.NOTIFY_IF_FAILED_OR_CHANGED) {
                     // notify when build fails or status changes
                     if (lastBuild == null || currentBuild.getStatus() == Constants.BUILD_STATUS_FAILED
                     	|| currentBuild.getStatus() != lastBuild.getStatus())
                         sendBuildNotification(currentBuild, revisions.getChangeLogins(), antProject);
                 }
             }
 
             // reload schedule to keep schedule data as new as possible before save
             schedule = Luntbuild.getDao().loadSchedule(schedule.getId());
             schedule.setStatus(Constants.SCHEDULE_STATUS_SUCCESS);
             schedule.setStatusDate(new Date());
             schedule.setCurrentThread(null);
             Luntbuild.getDao().saveSchedule(schedule);
             if (schedule.getBuildCleanupStrategy() ==
                 Constants.BUILD_KEEP_BY_COUNT) {
                 int reserveCount = new Integer(schedule.getBuildCleanupStrategyData()).intValue();
                 Luntbuild.getDao().reserveBuildsByCount(schedule, reserveCount);
             }
         } catch (Throwable throwable) {
             logger.error("Exception catched during job execution", throwable);
             if (schedule != null) {
                 // reload schedule to keep schedule data as new as possible before save
                 schedule = Luntbuild.getDao().loadSchedule(schedule.getId());
                 schedule.setProject(Luntbuild.getDao().loadProject(schedule.getProject().getId()));
                 schedule.setStatus(Constants.SCHEDULE_STATUS_FAILED);
                 schedule.setStatusDate(new Date());
                 schedule.setCurrentThread(null);
                 Luntbuild.getDao().saveSchedule(schedule);
                 if (notifyStrategy != Constants.NOTIFY_NONE)
                     sendScheduleNotification(schedule, antProject);
             }
         }
     }
 
     /**
      * Write the revision log for specified build.
      * 
      * @param build the build
      * @param revisions the revisions object
      */
     private void writeRevisionLog(Build build, Revisions revisions) {
         String publishDir = build.getPublishDir();
         Luntbuild.createDir(publishDir);
 
         writeTXTRevisionLog(publishDir, build, revisions);
         writeXMLRevisionLog(publishDir, build, revisions);
         writeHTMLRevisionLog(publishDir, build, revisions);
     }
 
     /**
      * Write the revision log for specified build in plain text.
      * 
      * @param publishDir the location to put the log
      * @param build the build
      * @param revisions the revisions object
      */
     private void writeTXTRevisionLog(String publishDir, Build build, Revisions revisions) {
         String revisionLogPath = publishDir + File.separator + BuildGenerator.REVISION_LOG;
         PrintStream revisionLogStream = null;
         try {
             revisionLogStream = new PrintStream(new FileOutputStream(revisionLogPath));
             Iterator it = revisions.getChangeLogs().iterator();
             while (it.hasNext()) {
                 String line = (String) it.next();
                 revisionLogStream.println(line);
             }
             revisionLogStream.close();
             revisionLogStream = null;
         } catch (IOException e) {
             logger.error("Error while writing revision log!", e);
         } finally {
             if (revisionLogStream != null)
                 revisionLogStream.close();
         }
     }
 
     /**
      * Write the revision log for specified build in XML format.
      * 
      * @param publishDir the location to put the log
      * @param build the build
      * @param revisions the revisions object
      */
     private void writeXMLRevisionLog(String publishDir, Build build, Revisions revisions) {
         String revisionXmlLogPath = publishDir + File.separator + BuildGenerator.REVISION_XML_LOG;
         revisions.setTimeAttribute(DateUtils.getDateForHeader());
         Element top = (Element) revisions.getLog();
 
         Writer output = null;
         OutputStream stream = null;
         try {
             // specify output in UTF8 otherwise accented characters will blow
             // up everything
             stream = new FileOutputStream(revisionXmlLogPath);
             output = new OutputStreamWriter(stream, "UTF8");
             output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
             output.write("<?xml-stylesheet type=\"text/xsl\" href=\""
                     + Luntbuild.installDir + "/revision.xsl" + "\"?>\n\n");
             (new DOMElementWriter()).write(top, output, 0, "\t");
             output.flush();
         } catch (IOException exc) {
             throw new BuildException("Unable to write log file", exc);
         } finally {
             if (output != null) {
                 try {
                     output.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
             if (stream != null) {
                 try {
                 	stream.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 
     /**
      * Write the revision log for specified build in HTML format.
      * 
      * @param publishDir the location to put the log
      * @param build the build
      * @param revisions the revisions object
      */
     private void writeHTMLRevisionLog(String publishDir, Build build, Revisions revisions) {
         String revisionXmlLogPath = publishDir + File.separator + BuildGenerator.REVISION_XML_LOG;
         String revisionHtmlLogPath = publishDir + File.separator + BuildGenerator.REVISION_HTML_LOG;
         String revisionTextLogPath = publishDir + File.separator + BuildGenerator.REVISION_LOG;
 
         File xmlFile = new File(revisionXmlLogPath);
         File xsltFile = new File(Luntbuild.installDir + File.separator + "revision.xsl");
         // JAXP reads data using the Source interface
         Source xmlSource = new StreamSource(xmlFile);
         Source xsltSource = new StreamSource(xsltFile);
 
         PrintStream pout = null;
         try {
             // the factory pattern supports different XSLT processors
             TransformerFactory transFact = TransformerFactory.newInstance();
             Transformer trans = transFact.newTransformer(xsltSource);
 
             // Output
             pout = new PrintStream(new FileOutputStream(revisionHtmlLogPath));
             trans.transform(xmlSource, new StreamResult(pout));
         } catch (Exception e) {
             if (logger.isDebugEnabled())
                 logger.info("Can't transform file " + revisionXmlLogPath +
                         " using XSL " + xsltFile.getAbsolutePath() + " reason: " + e.getMessage());
             LuntbuildLogger.logHtmlFromText(revisionTextLogPath, revisionHtmlLogPath);
         } finally {
              if (pout != null) {
                  pout.close();
              }
              if (xmlSource != null) try {((StreamSource)xmlSource).getReader().close();} catch (Exception e) {}
              if (xsltSource != null) try {((StreamSource)xsltSource).getReader().close();} catch (Exception e) {}
         }
     }
 
     /**
      * Derive the build time VCS list from specified VCS list.
      * 
      * @param vcsList the VCS list
      * @param antProject the ant project used for logging purpose
      * @return the build time VCS list
      */
     private List deriveBuildTimeVcsList(List vcsList, Project antProject) {
         List buildVcsList = new ArrayList();
         Iterator it = vcsList.iterator();
         while (it.hasNext()) {
             Vcs vcs = (Vcs) it.next();
             buildVcsList.add(vcs.deriveBuildTimeVcs(antProject));
         }
         return buildVcsList;
     }
 
     /**
      * Derive build time builder list from specified builder list.
      *
      * @param builderList the builder list
      * @param build the build
      * @param antProject the ant project used for logging purpose
      * @return the build time builder list
      */
     private List deriveBuildTimeBuilderList(List builderList, Build build, Project antProject)
     throws Throwable {
         List buildTimeBuilderList = new ArrayList();
         Iterator it = builderList.iterator();
         while (it.hasNext()) {
             Builder builder = (Builder) it.next();
             Builder derivedBuilder = (Builder) builder.clone();
             derivedBuilder.resolveEmbeddedOgnlVariables(build, antProject);
             buildTimeBuilderList.add(derivedBuilder);
         }
         return buildTimeBuilderList;
     }
 
 	/**
 	 * Gets the revisions for the working schedule.
 	 * 
 	 * @return the revisions object
 	 * @throws IllegalStateException if unable to get revisions
 	 * @see OgnlHelper#getRevisions()
 	 */
     private Revisions getRevisions() {
         Revisions revisions = OgnlHelper.getRevisions();
         if (revisions != null)
             return revisions;
         OgnlHelper.getWorkingSchedule().isVcsModified();
         revisions = OgnlHelper.getRevisions();
         if (revisions == null)
             throw new IllegalStateException("Revisions should not be null at this point!");
         return revisions;
     }
 
 	/**
 	 * Initializes the <code>OgnlHelper</code>.
 	 * 
 	 * @param schedule the working schedule
 	 * @param antProject the ant project used for logging purpose
 	 * @see OgnlHelper
 	 */
     private void setupOgnlHelper(Schedule schedule, Project antProject) {
         OgnlHelper.setAntProject(antProject);
         OgnlHelper.setWorkingSchedule(schedule);
         OgnlHelper.setRevisions(null);
         OgnlHelper.setTestMode(false);
     }
 
 	/**
 	 * Determines if a build is necessary for the working schedule based on an OGNL expression.
 	 * 
 	 * @param buildNecessaryCondition the OGNL expression
 	 * @return <code>true</code> if a build is necessary
 	 * @throws RuntimeException if unable to evaluate the build necessary condition
 	 */
     private boolean isBuildNecessary(String buildNecessaryCondition) {
         String workDir = OgnlHelper.getWorkingSchedule().getWorkDirRaw();
         File work = new File(workDir);
         if (!work.exists()) return true;
         try {
         	Boolean buildNecessaryValue =
         		(Boolean)Luntbuild.evaluateExpression(OgnlHelper.getWorkingSchedule(), buildNecessaryCondition, Boolean.class);
             if (buildNecessaryValue == null)
                 return false;
             else
                 return buildNecessaryValue.booleanValue();
         } catch (ClassCastException e) {
             throw new RuntimeException(e);
         } catch (OgnlException e) {
             throw new RuntimeException("Unable to evaluate expression \"" + buildNecessaryCondition +
                     "\". Please make sure that your VCS server's time is in sync with your Luntbuild machine!",
                     e);
         }
     }
 
     /**
      * Sends a notification about specified schedule.
      *
      * @param schedule the schedule to notify about
      * @param antProject the ant project used for logging purpose
      */
     private void sendScheduleNotification(Schedule schedule, Project antProject) {
         Set subscribeUsers = new HashSet(schedule.getProject().getUsersToNotify());
         subscribeUsers.remove(Luntbuild.getDao().loadUser(User.CHECKIN_USER_NAME_RECENT));
         subscribeUsers.remove(Luntbuild.getDao().loadUser(User.CHECKIN_USER_NAME_ALL));
 
         if (schedule.getProject().getNotifiers() != null) {
             Iterator it = Luntbuild.getNotifierInstances(Luntbuild.getNotifierClasses(schedule.getProject().
                     getNotifiers())).iterator();
             while (it.hasNext()) {
                 Notifier notifier = (Notifier) it.next();
                 notifier.sendScheduleNotification(subscribeUsers, schedule, antProject);
             }
         }
     }
 
     /**
      * Sends a notification about specified build.
      *
      * @param build the build to notify about
      * @param checkinLogins the VCS logins that have checked-in recently
      * @param antProject the ant project used for logging purpose
      */
     private void sendBuildNotification(Build build, Set checkinLogins,
                                        Project antProject) {
         com.luntsys.luntbuild.db.Project project = build.getSchedule().getProject();
         User virtualRecentCheckinUser = Luntbuild.getDao().loadUser(User.CHECKIN_USER_NAME_RECENT);
         User virtualCheckinUser = Luntbuild.getDao().loadUser(User.CHECKIN_USER_NAME_ALL);
 
         Set checkinUsers = new HashSet();
         if (project.getUsersToNotify().contains(virtualCheckinUser)) {
             Build prevBuild = build;
             do {
                 List allUsers = Luntbuild.getDao().loadUsers();
                 Set allCheckinLogins = Revisions.readChangeLogins(prevBuild);
                 Iterator it = allCheckinLogins.iterator();
                 while (it.hasNext()) {
                     String checkinLogin = (String) it.next();
                     User checkinUser = project.getUserByVcsLogin(checkinLogin, allUsers);
                     if (checkinUser == null) {
                         logger.warn("User for VCS login \"" +
                                 checkinLogin + "\" is not Luntbuild user for the project \"" + project.getName() + "\"!");
                     } else
                     	checkinUsers.add(checkinUser);
                 }
                 prevBuild = prevBuild.getPrevBuild();
             } while ( prevBuild != null && prevBuild.getStatus() != Constants.BUILD_STATUS_SUCCESS);
         } else if (project.getUsersToNotify().contains(virtualRecentCheckinUser)) {
             List allUsers = Luntbuild.getDao().loadUsers();
             Iterator it = checkinLogins.iterator();
             while (it.hasNext()) {
                 String checkinLogin = (String) it.next();
                 User checkinUser = project.getUserByVcsLogin(checkinLogin, allUsers);
                 if (checkinUser == null) {
                     logger.warn("User for VCS login \"" +
                             checkinLogin + "\" is not Luntbuild user for the project \"" + project.getName() + "\"!");
                 } else
                 	checkinUsers.add(checkinUser);
             }
         }
 
         Set subscribeUsers = new HashSet();
         Iterator it = project.getUsersToNotify().iterator();
         while (it.hasNext()) {
             User user = (User) it.next();
             if (!user.getName().equals(User.CHECKIN_USER_NAME_RECENT)
                     && !user.getName().equals(User.CHECKIN_USER_NAME_ALL)
                     && !checkinUsers.contains(user))
                 subscribeUsers.add(user);
         }
 
         if (project.getNotifiers() != null) {
             it = Luntbuild.getNotifierInstances(Luntbuild.getNotifierClasses(project.getNotifiers())).iterator();
             while (it.hasNext()) {
                 Notifier notifier = (Notifier) it.next();
                 notifier.sendBuildNotification(checkinUsers, subscribeUsers, build, antProject);
             }
         }
     }
 
     /**
      * Checkout and build based on specified build object.
      *
      * @param build the build object
      */
     private void checkoutAndBuild(Build build) {
         final String publishDirPathSep = build.getPublishDir() + File.separator;
         String buildLogPath = publishDirPathSep + BuildGenerator.BUILD_LOG;
 
         // create a ant antProject used to receive ant task logs
         Project antProject = Luntbuild.createAntProject();
         LuntbuildLogger buildLogger = new LuntbuildLogger();
         build.setLogger(buildLogger);
         buildLogger.setDirectMode(false);
         buildLogger.setEmacsMode(false);
         final Schedule theSchedule = build.getSchedule();
         buildLogger.setMessageOutputLevel(
                 Luntbuild.convertToAntLogLevel(theSchedule.getProject().getLogLevel()));
         antProject.addBuildListener(buildLogger);
         PrintStream logStream = null;
         boolean isCheckoutSuccess = false;
         long currentTime = System.currentTimeMillis();
         try {
             buildLogger.setOutputPath(buildLogPath);
             buildLogger.setErrorPath(buildLogPath);
             logStream = new PrintStream(new FileOutputStream(buildLogPath));
             buildLogger.setOutputPrintStream(logStream);
             buildLogger.setErrorPrintStream(logStream);
 
             // checks if there are any failed dependencies unless build condition is always
             if (!theSchedule.isAlways()) {
                 Schedule notSatisfiedDependentSchedule = theSchedule.getNotSatisfiedDependency();
                 if (notSatisfiedDependentSchedule != null) {
                     throw new BuildException("Dependency not satisfied : latest build in schedule \"" +
                             notSatisfiedDependentSchedule.getProject().getName() + "/" +
                             notSatisfiedDependentSchedule.getName() +
                             "\" has failed or there are no builds in this schedule.");
                 }
             }
 
             String user = build.getUser();
             if (user != null) {
                 logger.info("User \"" + user + "\" started the build: " + theSchedule.getName());
                 antProject.log("User \"" + user + "\" started the build", Project.MSG_INFO);
             }
 
             if (build.isCleanBuild() || build.isRebuild()) {
                 String message = "Cleaning up project work directory \"" +
                 theSchedule.getWorkDirRaw() + "\"...";
                 antProject.log(message, Project.MSG_INFO);
                 logger.info(message);
                 Iterator it = build.getVcsList().iterator();
                 while (it.hasNext()) {
                     Vcs vcs = (Vcs) it.next();
                     vcs.cleanupCheckout(theSchedule, antProject);
                 }
             }
 
             logger.info("Checking out code from the defined Version Control Systems...");
             Iterator it = build.getVcsList().iterator();
             while (it.hasNext()) {
                 Vcs vcs = (Vcs) it.next();
                 antProject.log("Perform checkout operation for VCS setting: ", Project.MSG_INFO);
                 antProject.log(vcs.toString(), Project.MSG_INFO);
                 vcs.checkout(build, antProject);
             }
             isCheckoutSuccess = true;
             antProject.log("Duration of the checkout operation: " +
                     (System.currentTimeMillis() - currentTime) / 60000 + " minutes",
                     org.apache.tools.ant.Project.MSG_INFO);
             currentTime = System.currentTimeMillis();
 
             Luntbuild.createDir(publishDirPathSep + Builder.ARTIFACTS_DIR);
             Iterator reportsiter = Luntbuild.reports.values().iterator();
             while (reportsiter.hasNext()) {
                 Report report = (Report) reportsiter.next();
                 Luntbuild.createDir(publishDirPathSep + File.separator + report.getReportDir());
             }
 
             logger.info("Build with defined builders...");
             it = build.getBuilderList().iterator();
             while (it.hasNext()) {
                 Builder builder = (Builder) it.next();
                 antProject.log("Perform build with builder setting: ", Project.MSG_INFO);
                 antProject.log(builder.toString(), Project.MSG_INFO);
                 buildLogger.setBuilderName(builder.getName());
                 builder.build(build, buildLogger);
                 buildLogger.setBuilderName(null);
             }
 
             antProject.log("Duration of the builder(s) execution: " +
                     (System.currentTimeMillis() - currentTime) / 60000 + " minutes",
                     org.apache.tools.ant.Project.MSG_INFO);
            currentTime = System.currentTimeMillis();
             build.setStatus(Constants.BUILD_STATUS_SUCCESS);
         } catch (Throwable e) {
             build.setStatus(Constants.BUILD_STATUS_FAILED);
             if (e instanceof BuildException) {
                 antProject.log(e.getMessage(), Project.MSG_ERR);
                 logger.error("Build failed: " + e.getMessage());
             } else {
                 if (logStream != null)
                     e.printStackTrace(logStream);
                 logger.error("Build failed: ", e);
             }
         }
 
         if (build.getPostbuildStrategy() == Constants.POSTBUILD_ALWAYS ||
                 build.getPostbuildStrategy() ==
                 Constants.POSTBUILD_IF_SUCCESS &&
                 build.getStatus() == Constants.BUILD_STATUS_SUCCESS ||
                 build.getPostbuildStrategy() == Constants.POSTBUILD_IF_FAILED &&
                 build.getStatus() == Constants.BUILD_STATUS_FAILED) {
             try {
                 antProject.log("");
                 if (build.getPostbuilderList().size() == 0)
                     throw new BuildException("ERROR: Fail to post-build: No post-builders defined!");
 
                 logger.info("Post-build with defined post-builders...");
                 Iterator it = build.getPostbuilderList().iterator();
                 while (it.hasNext()) {
                     Builder builder = (Builder) it.next();
                     antProject.log("Perform post-build with builder setting: ", Project.MSG_INFO);
                     antProject.log(builder.toString(), Project.MSG_INFO);
                     buildLogger.setBuilderName(builder.getName());
                     builder.build(build, buildLogger);
                 }
 
                 antProject.log("Duration of the post-builder(s) execution: " +
                         (System.currentTimeMillis() - currentTime) / 60000 + " minutes",
                         Project.MSG_INFO);
                 logger.info("Run of ant post-build script succeed!");
             } catch (Throwable throwable) {
                 build.setStatus(Constants.BUILD_STATUS_FAILED);
                 if (throwable instanceof BuildException) {
                     antProject.log(throwable.getMessage(), org.apache.tools.ant.Project.MSG_ERR);
                     logger.error("Post-build failed: " + throwable.getMessage());
                 } else {
                     if (logStream != null)
                         throwable.printStackTrace(logStream);
                     logger.error("Post-build failed: ", throwable);
                 }
             }
         }
 
         try {
             if (isCheckoutSuccess) {
                 // insert a blank line into the  log
                 antProject.log("\n", org.apache.tools.ant.Project.MSG_INFO);
                 if (!build.isRebuild()) {
                     if (build.getLabelStrategy() == Constants.LABEL_ALWAYS ||
                             build.getLabelStrategy() == Constants.LABEL_IF_SUCCESS &&
                             build.getStatus() == Constants.BUILD_STATUS_SUCCESS) {
                         build.setHaveLabelOnHead(true);
                         Iterator it = build.getVcsList().iterator();
                         while (it.hasNext()) {
                             Vcs vcs = (Vcs) it.next();
                             vcs.label(build, antProject);
                         }
                     } else {
                         Iterator it = build.getVcsList().iterator();
                         while (it.hasNext()) {
                             Vcs vcs = (Vcs) it.next();
                             vcs.unlabel(build, antProject);
                         }
                     }
                 } else {
                     String message = "Cleaning up project work directory \"" +
                             theSchedule.getWorkDirRaw() + "\" after rebuild...";
                     antProject.log(message, Project.MSG_INFO);
                     logger.info(message);
                     Iterator it = build.getVcsList().iterator();
                     while (it.hasNext()) {
                         Vcs vcs = (Vcs) it.next();
                         vcs.cleanupCheckout(theSchedule, antProject);
                     }
                 }
             }
         } catch (Throwable e) {
             build.setStatus(Constants.BUILD_STATUS_FAILED);
             if (e instanceof BuildException) {
                 antProject.log(e.getMessage(), org.apache.tools.ant.Project.MSG_ERR);
                 logger.error("Build failed: " + e.getMessage());
             } else {
                 if (logStream != null)
                     e.printStackTrace(logStream);
                 logger.error("Build failed: ", e);
             }
         } finally {
             if (logStream != null)
                 logStream.close();
             build.setEndDate(new Date());
             String buildXmlPath = publishDirPathSep + BuildGenerator.BUILD_XML_LOG;
             String buildHtmlPath = publishDirPathSep + BuildGenerator.BUILD_HTML_LOG;
             String buildTextPath = publishDirPathSep + BuildGenerator.BUILD_LOG;
 
             buildLogger.logHtml(buildXmlPath, Luntbuild.installDir + "/log.xsl", buildHtmlPath, buildTextPath);
             build.removeLogger();
             buildLogger.close();
         }
     }
 
     /**
      * This class receives ant logs and re-directs to log4j loggers.
      */
     private static class Log4jBuildListener extends EmptyBuildListenerImpl {
         /**
          * Logs a message for an event.
          * 
          * @param event the event
          */
         public void messageLogged(BuildEvent event) {
             String prefix = "";
             if (event.getTask() != null)
                 prefix += "        [" + event.getTask().getTaskName() + "]";
             String msg = event.getMessage();
             if (event.getPriority() == Project.MSG_ERR)
                 if (msg != null && msg.trim().length() > 0) logger.error(prefix + msg);
             else if (event.getPriority() == Project.MSG_WARN)
             	if (msg != null && msg.trim().length() > 0) logger.warn(prefix + msg);
             else if (event.getPriority() == Project.MSG_INFO)
             	if (msg != null && msg.trim().length() > 0) logger.info(prefix + msg);
             else
             	if (msg != null && msg.trim().length() > 0) logger.debug(prefix + msg);
         }
     }
 
     /**
      * 
      */
     private static class BuildDependencyWalker implements Runnable {
         private BuildParams buildParams;
 
         /**
          * Creates a new build dependency walker.
          * 
          * @param buildParams the build parameters
          */
         public BuildDependencyWalker(BuildParams buildParams) {
             this.buildParams = buildParams;
         }
 
         /**
          * Executes the build dependency walking.
          */
         public void run() {
             try {
                 SecurityHelper.runAsSiteAdmin();
                 List schedules = Luntbuild.getDao().loadSchedules();
                 Schedule schedule = new Schedule();
                 schedule.setId(buildParams.getScheduleId());
                 int index = schedules.indexOf(schedule);
                 if (index == -1) {
                     logger.error("Failed to walk through build dependency: schedule with id \"" + String.valueOf(schedule.getId()) + "\" not found!");
                     return;
                 }
                 schedule = (Schedule) schedules.get(index);
                 DependencyResolver resolver = new DependencyResolver(schedules, null);
                 Set scheduleSet = new HashSet(schedules);
                 resolver.detectDependencyLoop(scheduleSet);
 
                 if (buildParams.getTriggerDependencyStrategy() == Constants.TRIGGER_ALL_DEPENDENT_SCHEDULES ||
                         buildParams.getTriggerDependencyStrategy() == Constants.TRIGGER_SCHEDULES_THIS_DEPENDS_ON)
                     resolver.visitNodesThisNodeDependsOnRecursively(schedule);
 
                 schedule.visit(buildParams);
 
                 if (buildParams.getTriggerDependencyStrategy() == Constants.TRIGGER_ALL_DEPENDENT_SCHEDULES ||
                         buildParams.getTriggerDependencyStrategy() == Constants.TRIGGER_SCHEDULES_DEPENDS_ON_THIS)
                     resolver.visitNodesDependsOnThisNodeRecursively(scheduleSet, schedule);
             } catch (Throwable throwable) {
                 logger.error("Failed while walking through build dependency: " + throwable.getMessage());
             }
         }
     }
 }

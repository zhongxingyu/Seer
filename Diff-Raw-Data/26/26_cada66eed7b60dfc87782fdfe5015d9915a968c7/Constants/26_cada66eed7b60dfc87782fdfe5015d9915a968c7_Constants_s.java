 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-12-17
  * Time: 16:25:02
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
 package com.luntsys.luntbuild.facades;
 
 /**
  * Container for commonly used constant values.
  *
  * @author robin shine
  */
 public class Constants {
 	// Trigger type constants
 	/** Total number of options for trigger type */
 	public static final int NUM_TRIGGER_TYPE = 3;
 	/** Trigger type, manual */
 	public static final int TRIGGER_TYPE_MANUAL = 0;
 	/** Trigger type, simple */
 	public static final int TRIGGER_TYPE_SIMPLE = 1;
 	/** Trigger type, cron */
 	public static final int TRIGGER_TYPE_CRON = 2;
 
 	// Schedule status constants
 	/** Total number of options for schedule status */
 	public static final int NUM_SCHEDULE_STATUS = 4;
 	/** Schedule status, just created */
 	public static final int SCHEDULE_STATUS_CREATED = 0;
 	/** Schedule status, success */
 	public static final int SCHEDULE_STATUS_SUCCESS = 1;
 	/** Schedule status, failed */
 	public static final int SCHEDULE_STATUS_FAILED = 2;
 	/** Schedule status, currently running */
 	public static final int SCHEDULE_STATUS_RUNNING = 3;
 
 	// Build status constants
 	/** Total number of options for build status */
 	public static final int NUM_BUILD_STATUS = 4;
 	/** Build status, all */
 	public static final int BUILD_STATUS_ALL = 0;
 	/** Build status, success */
 	public static final int BUILD_STATUS_SUCCESS = 1;
 	/** Build status, failed */
 	public static final int BUILD_STATUS_FAILED = 2;
 	/** Build status, currently running */
 	public static final int BUILD_STATUS_RUNNING = 3;
 
 	// Build type constants
 	/** Total number of options for build type */
 	public static final int NUM_BUILD_TYPE = 2;
 	/** Build type, clean */
 	public static final int BUILD_TYPE_CLEAN = 0;
 	/** Build type, increment */
 	public static final int BUILD_TYPE_INCREMENT = 1;
 
 	// Build strategy constants
 	// What is this for?
 	/** Total number of options for build strategy */
 	public static final int NUM_BUILD_STRATEGY = 4;
 
 	// Label strategy constants
 	/** Total number of options for label strategy */
 	public static final int NUM_LABEL_STRATEGY = 3;
 	/** Label strategy, if success */
 	public static final int LABEL_IF_SUCCESS = 0;
 	/** Label strategy, always */
 	public static final int LABEL_ALWAYS = 1;
 	/** Label strategy, don't label */
 	public static final int LABEL_NONE = 2;
 
 	// Notify strategy constants
 	/** Total number of options for notify strategy */
 	public static final int NUM_NOTIFY_STRATEGY = 6;
 	/** Notify strategy, when status changed */
 	public static final int NOTIFY_WHEN_STATUS_CHANGED = 0;
 	/** Notify strategy, if success */
 	public static final int NOTIFY_IF_SUCCESS = 1;
 	/** Notify strategy, if failed */
 	public static final int NOTIFY_IF_FAILED = 2;
 	/** Notify strategy, always */
 	public static final int NOTIFY_ALWAYS = 3;
 	/** Notify strategy, don't notify */
 	public static final int NOTIFY_NONE = 4;
 	/** Notify strategy, if failed or status changed */
 	public static final int NOTIFY_IF_FAILED_OR_CHANGED = 5;
 
 	// Trigger dependents strategy
 	/** Total number of options for dependency trigger strategy */
 	public static final int NUM_TRIGGER_DEPENDENCY_STRATEGY = 4;
 	/** Dependency trigger strategy, trigger schedules that this depends on */
 	public static final int TRIGGER_SCHEDULES_THIS_DEPENDS_ON = 0;
 	/** Dependency trigger strategy, trigger schedules that depends on this */
 	public static final int TRIGGER_SCHEDULES_DEPENDS_ON_THIS = 1;
 	/** Dependency trigger strategy, all dependent schedules */
 	public static final int TRIGGER_ALL_DEPENDENT_SCHEDULES = 2;
 	/** Dependency trigger strategy, no schedules */
 	public static final int TRIGGER_NONE_DEPENDENT_SCHEDULES = 3;
 
 	// Builds cleanup strategy
 	/** Total number of options for build cleanup strategy */
 	public static final int NUM_BUILD_CLEANUP_STRATEGY = 3;
 	/** Build cleanup strategy, don't cleanup */
 	public static final int BUILD_CLEANUP_NONE = 0;
 	/** Build cleanup strategy, keep latest builds by days */
 	public static final int BUILD_KEEP_BY_DAYS = 1;
 	/** Build cleanup strategy, keep latest builds by count */
 	public static final int BUILD_KEEP_BY_COUNT = 2;
 
 	// Postbuild strategy constants
 	/** Total number of options for post-build strategy */
 	public static final int NUM_POSTBUILD_STRATEGY = 4;
 	/** Post-build strategy, don't post-build */
 	public static final int POSTBUILD_NONE = 0;
 	/** Post-build strategy, if success */
 	public static final int POSTBUILD_IF_SUCCESS = 1;
 	/** Post-build strategy, if failed */
 	public static final int POSTBUILD_IF_FAILED = 2;
 	/** Post-build strategy, always */
 	public static final int POSTBUILD_ALWAYS = 3;
 
 	// Build timing constants
 	/** Total number of options for build timing prefix */
 	public static final int NUM_BUILD_TIMING = 4;
 	/** Build timing prefix, now */
 	public static final int BUILD_TIMING_NOW = 0;
 	/** Build timing prefix, after */
 	public static final int BUILD_TIMING_AFTER = 1;
 	/** Build timing prefix, at */
 	public static final int BUILD_TIMING_AT = 2;
 	/** Build timing prefix, later */
 	public static final int BUILD_TIMING_LATER = 3;
 
 
 	// Log level constants
 	/** Total number of options for log level */
 	public static final int NUM_LOG_LEVEL = 3;
 	/** Log level, brief */
 	public static final int LOG_LEVEL_BRIEF = 0;
 	/** Log level, normal */
 	public static final int LOG_LEVEL_NORMAL = 1;
 	/** Log level, verbose */
 	public static final int LOG_LEVEL_VERBOSE = 2;
 
     // Jabber Server Type
 	/** Total number of options for Jabber server type */
     public static final int NUM_JABBER_SERVER_TYPE = 3;
     /** Jabber server type, normal */
     public static final int JABBER_SERVER_TYPE_NORMAL = 0;
     /** Jabber server type, SSL */
     public static final int JABBER_SERVER_TYPE_SSL = 1;
     /** Jabber server type, Google */
     public static final int JABBER_SERVER_TYPE_GOOGLE = 2;
 
 	// System level property name constants
    /** Name of Luntbuild servlet URL property */
	public static final String SERVLET_URL = "servletUrl";
     /** Name of default working directory property */
 	public static final String WORKING_DIR = "workingDir";
     /** Name of root publishing directory property */
 	public static final String PUBLISH_DIR = "publishDir";
     /** Name of page refresh interval property */
 	public static final String PAGE_REFRESH_INTERVAL = "pageRefreshInterval";
     /** Name of build thread count property */
 	public static final String BUILD_THREAD_COUNT = "buildThreadCount";
     /** Name of database backup file name property */
 	public static final String BACKUP_FILE_NAME = "backupFileName";
     /** Name of database backup cron expression property */
 	public static final String BACKUP_CRON_EXPRESSION = "backupCronExpression";
 
 	// Special build version constants
 	/** Special build version, last build */
 	public static final String LAST_BUILD = "last";
 	/** Special build version, last successful build */
 	public static final String LAST_SUCCESS_BUILD = "last_success";
 
 	/**
 	 * Gets the display text for build type.
 	 * 
 	 * @param buildType the build type
 	 * @return the display text
 	 * @see #BUILD_TYPE_CLEAN
 	 * @see #BUILD_TYPE_INCREMENT
 	 */
 	public static String getBuildTypeText(int buildType) {
 		if (buildType == BUILD_TYPE_CLEAN)
 			return "clean";
 		else if (buildType == BUILD_TYPE_INCREMENT)
 			return "increment";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for post-build strategy.
 	 * 
 	 * @param postbuildStrategy the post-build strategy
 	 * @return the display text
 	 * @see #POSTBUILD_ALWAYS
 	 * @see #POSTBUILD_NONE
 	 * @see #POSTBUILD_IF_FAILED
 	 * @see #POSTBUILD_IF_SUCCESS
 	 */
 	public static String getPostbuildStrategyText(int postbuildStrategy) {
 		if (postbuildStrategy == POSTBUILD_ALWAYS)
 			return "post-build always";
 		else if (postbuildStrategy == POSTBUILD_NONE)
 			return "do not post-build";
 		else if (postbuildStrategy == POSTBUILD_IF_FAILED)
 			return "post-build if failed";
 		else if (postbuildStrategy == POSTBUILD_IF_SUCCESS)
 			return "post-build if success";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for label strategy.
 	 * 
 	 * @param labelStrategy the label strategy
 	 * @return the display text
 	 * @see #LABEL_ALWAYS
 	 * @see #LABEL_NONE
 	 * @see #LABEL_IF_SUCCESS
 	 */
 	public static String getLabelStrategyText(int labelStrategy) {
 		if (labelStrategy == LABEL_ALWAYS)
 			return "label always";
 		else if (labelStrategy == LABEL_NONE)
 			return "do not label";
 		else if (labelStrategy == LABEL_IF_SUCCESS)
 			return "label if success";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for notify strategy.
 	 * 
 	 * @param notifyStrategy the notify strategy
 	 * @return the display text
 	 * @see #NOTIFY_ALWAYS
 	 * @see #NOTIFY_IF_FAILED
 	 * @see #NOTIFY_IF_SUCCESS
 	 * @see #NOTIFY_NONE
 	 * @see #NOTIFY_WHEN_STATUS_CHANGED
 	 * @see #NOTIFY_IF_FAILED_OR_CHANGED
 	 */
 	public static String getNotifyStrategyText(int notifyStrategy) {
 		if (notifyStrategy == NOTIFY_ALWAYS)
 			return "notify always";
 		else if (notifyStrategy == NOTIFY_IF_FAILED)
 			return "notify when failed";
 		else if (notifyStrategy == NOTIFY_IF_SUCCESS)
 			return "notify when success";
 		else if (notifyStrategy == NOTIFY_NONE)
 			return "do not notify";
 		else if (notifyStrategy == NOTIFY_WHEN_STATUS_CHANGED)
 			return "notify when status changed";
 		else if (notifyStrategy == NOTIFY_IF_FAILED_OR_CHANGED)
 			return "notify when failed or status changed";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for dependency triggering strategy.
 	 * 
 	 * @param triggerDependencyStrategy the dependency triggering strategy
 	 * @return the display text
 	 * @see #TRIGGER_ALL_DEPENDENT_SCHEDULES
 	 * @see #TRIGGER_NONE_DEPENDENT_SCHEDULES
 	 * @see #TRIGGER_SCHEDULES_DEPENDS_ON_THIS
 	 * @see #TRIGGER_SCHEDULES_THIS_DEPENDS_ON
 	 */
 	public static String getTriggerDependencyStrategyText(int triggerDependencyStrategy) {
 		if (triggerDependencyStrategy == TRIGGER_ALL_DEPENDENT_SCHEDULES)
 			return "trigger all dependent schedules";
 		else if (triggerDependencyStrategy == TRIGGER_NONE_DEPENDENT_SCHEDULES)
 			return "do not trigger any dependent schedules";
 		else if (triggerDependencyStrategy == TRIGGER_SCHEDULES_DEPENDS_ON_THIS)
 			return "trigger schedules that depends on this schedule";
 		else if (triggerDependencyStrategy == TRIGGER_SCHEDULES_THIS_DEPENDS_ON)
 			return "trigger schedules this schedule depends on";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for trigger type.
 	 * 
 	 * @param triggerType the trigger type
 	 * @return the display text
 	 * @see #TRIGGER_TYPE_CRON
 	 * @see #TRIGGER_TYPE_MANUAL
 	 * @see #TRIGGER_TYPE_SIMPLE
 	 */
 	public static String getTriggerTypeText(int triggerType) {
 		if (triggerType == TRIGGER_TYPE_CRON)
 			return "cron";
 		else if (triggerType == TRIGGER_TYPE_MANUAL)
 			return "manual";
 		else if (triggerType == TRIGGER_TYPE_SIMPLE)
 			return "simple";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for build status.
 	 * 
 	 * @param buildStatus the build status
 	 * @return the display text
 	 * @see #BUILD_STATUS_FAILED
 	 * @see #BUILD_STATUS_RUNNING
 	 * @see #BUILD_STATUS_SUCCESS
 	 * @see #BUILD_STATUS_ALL
 	 */
 	public static String getBuildStatusText(int buildStatus) {
 		if (buildStatus == BUILD_STATUS_FAILED)
 			return "failed";
 		else if (buildStatus == BUILD_STATUS_RUNNING)
 			return "running";
 		else if (buildStatus == BUILD_STATUS_SUCCESS)
 			return "success";
 		else if (buildStatus == BUILD_STATUS_ALL)
 			return "all status";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for schedule status.
 	 * 
 	 * @param scheduleStatus the schedule status
 	 * @return the display text
 	 * @see #SCHEDULE_STATUS_FAILED
 	 * @see #SCHEDULE_STATUS_RUNNING
 	 * @see #SCHEDULE_STATUS_SUCCESS
 	 * @see #SCHEDULE_STATUS_CREATED
 	 */
 	public static String getScheduleStatusText(int scheduleStatus) {
 		if (scheduleStatus == SCHEDULE_STATUS_FAILED)
 			return "failed";
 		else if (scheduleStatus == SCHEDULE_STATUS_RUNNING)
 			return "running";
 		else if (scheduleStatus == SCHEDULE_STATUS_SUCCESS)
 			return "success";
 		else if (scheduleStatus == SCHEDULE_STATUS_CREATED)
 			return "created";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for build timing prefix.
 	 * 
 	 * @param buildTiming the build timing prefix
 	 * @return the display text
 	 * @see #BUILD_TIMING_AFTER
 	 * @see #BUILD_TIMING_AT
 	 * @see #BUILD_TIMING_LATER
 	 * @see #BUILD_TIMING_NOW
 	 */
 	public static String getBuildTimingText(int buildTiming) {
 		if (buildTiming == BUILD_TIMING_AFTER)
 			return "after";
 		else if (buildTiming == BUILD_TIMING_AT)
 			return "at";
 		else if (buildTiming == BUILD_TIMING_LATER)
 			return "later at";
 		else if (buildTiming == BUILD_TIMING_NOW)
 			return "now";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for log level.
 	 * 
 	 * @param logLevel the log level
 	 * @return the display text
 	 * @see #LOG_LEVEL_BRIEF
 	 * @see #LOG_LEVEL_NORMAL
 	 * @see #LOG_LEVEL_VERBOSE
 	 */
 	public static String getLogLevelText(int logLevel) {
 		if (logLevel == LOG_LEVEL_BRIEF)
 			return "brief";
 		else if (logLevel == LOG_LEVEL_NORMAL)
 			return "normal";
 		else if (logLevel == LOG_LEVEL_VERBOSE)
 			return "verbose";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for build cleanup strategy.
 	 * 
 	 * @param buildCleanupStrategy the build cleanup strategy
 	 * @return the display text
 	 * @see #BUILD_CLEANUP_NONE
 	 * @see #BUILD_KEEP_BY_DAYS
 	 * @see #BUILD_KEEP_BY_COUNT
 	 */
 	public static String getBuildCleanupStrategyText(int buildCleanupStrategy) {
 		if (buildCleanupStrategy == BUILD_CLEANUP_NONE)
 			return "do not cleanup builds automatically";
 		else if (buildCleanupStrategy == BUILD_KEEP_BY_DAYS)
 			return "keep builds by days";
 		else if (buildCleanupStrategy == BUILD_KEEP_BY_COUNT)
 			return "keep builds by count";
 		else
 			return "";
 	}
 
 	/**
 	 * Gets the display text for Jabber server type.
 	 * 
 	 * @param type the Jabber server type
 	 * @return the display text
 	 * @see #JABBER_SERVER_TYPE_NORMAL
 	 * @see #JABBER_SERVER_TYPE_SSL
 	 * @see #JABBER_SERVER_TYPE_GOOGLE
 	 */
     public static String getJabberServerType(int type) {
         if (type == JABBER_SERVER_TYPE_NORMAL)
             return "normal";
         else if (type == JABBER_SERVER_TYPE_SSL)
             return "ssl";
         else if (type == JABBER_SERVER_TYPE_GOOGLE)
             return "google";
         else
             return "";
     }
 
     /**
      * Gets the Jabber server type value.
      * 
      * @param type the server type
      * @return the server type value
      */
     public static int getJabberServerTypeValue(String type) {
         if (type.equalsIgnoreCase("normal"))
             return JABBER_SERVER_TYPE_NORMAL;
         else if (type.equalsIgnoreCase("ssl"))
             return JABBER_SERVER_TYPE_SSL;
         else if (type.equalsIgnoreCase("google"))
             return JABBER_SERVER_TYPE_GOOGLE;
         else
             return JABBER_SERVER_TYPE_NORMAL;
     }
 
     /**
      * Gets the list of Jabber server types.
      * 
      * @return the list of Jabber server types
      */
     public static String[] getJabberServerTypes() {
         return new String[] {"normal", "ssl", "google"};
     }
 
 }

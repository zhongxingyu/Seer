 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-18
  * Time: 16:31:17
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
 package com.luntsys.luntbuild.services;
 
 import com.luntsys.luntbuild.BuildGenerator;
 import com.luntsys.luntbuild.facades.BuildParams;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.maintenance.SystemBackup;
 import com.luntsys.luntbuild.maintenance.SystemCare;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.TriggerStartTimeComparator;
 import com.luntsys.luntbuild.utility.ValidationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.*;
 import org.quartz.impl.StdSchedulerFactory;
 
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * Quartz scheduler implementation. This will loaded by Spring framework as a singleton.
  * 
  * @author robin shine
  */
 public class QuartzService implements IScheduler {
 	private static Log logger = LogFactory.getLog(QuartzService.class);
 	private Object schedLock = new Object();
 	/**
 	 * The quartz {@link org.quartz.Scheduler} object which will be initialized and destroyed respectively
 	 * in servlet init and destroy
 	 */
 	private Scheduler sched = null;
 
 	/** Name for triggers that should be ignored. */
 	public static final String DUMMY_TRIGGER_NAME = "dummy";
 	/** Name for jobs that should be ignored. */
 	public static final String DUMMY_JOB_NAME = "dummy";
 
 	/**
 	 * Reschedules builds based on build scheduling information existed in the persistent storage.
 	 * 
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void rescheduleBuilds() {
 		validateSched();
 		synchronized (schedLock) {
 			try {
 				String[] names = sched.getTriggerNames(Scheduler.DEFAULT_GROUP);
 				for (int i = 0; i < names.length; i++) {
 					sched.unscheduleJob(names[i], Scheduler.DEFAULT_GROUP);
 				}
 
 				// Jobs could not be deleted due to a bug in quartz-1.4.0 which will cause
 				// stateful job blocked permernantly if it is deleted while executing
 /*
 				groups = sched.getJobGroupNames();
 				for (int i = 0; i < groups.length; i++) {
 					String[] names = sched.getJobNames(groups[i]);
 					for (int j = 0; j < names.length; j++) {
 						sched.deleteJob(names[j], groups[i]);
 					}
 				}
 */
 				ListIterator itSchedule = Luntbuild.getDao().loadSchedules().listIterator();
 				while (itSchedule.hasNext()) {
 					Schedule schedule = (Schedule) itSchedule.next();
 					if (schedule.getTrigger() == null || schedule.isDisabled())
 						continue;
                     String jobName = schedule.getJobName();
 					JobDetail jobDetail = sched.getJobDetail(jobName, Scheduler.DEFAULT_GROUP);
 					if (jobDetail == null) {
                         jobDetail = new JobDetail();
 						jobDetail.setDurability(true);
 						jobDetail.setGroup(Scheduler.DEFAULT_GROUP);
 						jobDetail.setName(jobName);
 						jobDetail.setJobClass(BuildGenerator.class);
 						JobDataMap dMap = new JobDataMap();
 						dMap.put("USER", SecurityHelper.getPrincipalAsString());
 						jobDetail.setJobDataMap(dMap);
 						sched.addJob(jobDetail, false);
 					}
 
 					// should not save scheduleId here, cause one job instance can associate with
 					// more than one schedules, and scheduleId may get messed up if two or more
 					// schedules with same job instance calls this method
 /*
                     JobDataMap dataMap = new JobDataMap();
                     dataMap.put("scheduleId", String.valueOf(schedule.getId()));
                     jobDetail.setJobDataMap(dataMap);
 */
 
                     Trigger trigger = (Trigger) schedule.getTrigger().clone();
 					trigger.setGroup(Scheduler.DEFAULT_GROUP);
 					trigger.setName(String.valueOf(schedule.getId()));
 					trigger.setJobGroup(Scheduler.DEFAULT_GROUP);
 					trigger.setJobName(jobDetail.getName());
 					if (trigger instanceof SimpleTrigger) {
						SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
						simpleTrigger.setStartTime(new Date(System.currentTimeMillis() +
								simpleTrigger.getRepeatInterval()));
					} else
						trigger.setStartTime(new Date(System.currentTimeMillis()));
 					sched.scheduleJob(trigger);
 				}
 			} catch (Exception e) {
 				logger.error("Error in rescheduleBuilds: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Validates if the scheduler is currently initialized and started
 	 */
 	private void validateSched() {
 		try {
 			if (sched == null)
 				throw new SchedulerException("Scheduler not initialized!");
 			if (sched.isShutdown())
 				throw new SchedulerException("Scheduler not started!");
 		} catch (Exception e) {
 			logger.error("Error in validateSched: ", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Schedules a build for specified schedule using specified trigger.
 	 *
 	 * @param schedule the schedule
 	 * @param trigger the trigger
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void scheduleBuild(Schedule schedule, Trigger trigger) {
 		synchronized (schedLock) {
 			try {
                 String jobName = schedule.getJobName();
 				JobDetail jobDetail = sched.getJobDetail(jobName, Scheduler.DEFAULT_GROUP);
 				if (jobDetail == null) {
 					jobDetail = new JobDetail();
 					jobDetail.setDurability(true);
 					jobDetail.setGroup(Scheduler.DEFAULT_GROUP);
 					jobDetail.setName(jobName);
 					jobDetail.setJobClass(BuildGenerator.class);
 					JobDataMap dMap = new JobDataMap();
 					dMap.put("USER", SecurityHelper.getPrincipalAsString());
 					jobDetail.setJobDataMap(dMap);
 					sched.addJob(jobDetail, false);
 				}
 
 				// should not save scheduleId here, cause one job instance can associate with
 				// more than one schedules, and scheduleId may get messed up if two or more
 				// schedules with same job instance calls this method
 /*
                 JobDataMap dataMap = new JobDataMap();
                 dataMap.put("scheduleId", String.valueOf(schedule.getId()));
                 jobDetail.setJobDataMap(dataMap);
 */
 
 				trigger.setJobGroup(jobDetail.getGroup());
 				trigger.setJobName(jobDetail.getName());
 				sched.scheduleJob(trigger);
 			} catch (Exception e) {
 				logger.error("Error in scheduleBuilds: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Removes the specified trigger.
 	 *
 	 * @param triggerName the name of the trigger
 	 * @param triggerGroup the group the trigger is in
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void unscheduleBuild(String triggerName, String triggerGroup) {
 		synchronized (schedLock) {
 			try {
 				sched.unscheduleJob(triggerName, triggerGroup);
 			} catch (Exception e) {
 				logger.error("Error in unscheduleBuild: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Gets the list of triggers manually triggered for the specified schedule, the list is ordered ascending
 	 * by the start time of the trigger.
 	 *
 	 * @param schedule the schedule
 	 * @return the manual triggers
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public List getWaitingManualBuildTriggers(Schedule schedule) {
 		synchronized (schedLock) {
 			try {
 				List waitingManualBuildTriggers = new ArrayList();
 				Trigger manualTriggers[] = sched.getTriggersOfJob(schedule.getJobName(), Scheduler.DEFAULT_GROUP);
 				List executingJobs = sched.getCurrentlyExecutingJobs();
 				for (int i = 0; i < manualTriggers.length; i++) {
 					Trigger manualTrigger = manualTriggers[i];
 					if (!manualTrigger.getGroup().equals(BuildGenerator.MANUALBUILD_GROUP))
 						continue;
 					BuildParams buildParams = Schedule.parseTriggerName(manualTrigger.getName());
 					if (buildParams.getScheduleId() != schedule.getId())
 						continue;
 					Iterator itExecutingJob = executingJobs.iterator();
 					boolean executing = false;
 					while (itExecutingJob.hasNext()) {
 						JobExecutionContext jobExecutionContext = (JobExecutionContext) itExecutingJob.next();
 						if (manualTrigger.equals(jobExecutionContext.getTrigger())) {
 							executing = true;
 							break;
 						}
 					}
 					if (!executing)
 						waitingManualBuildTriggers.add(manualTrigger);
 				}
 				Collections.sort(waitingManualBuildTriggers, new TriggerStartTimeComparator());
 				return waitingManualBuildTriggers;
 			} catch (Exception e) {
 				logger.error("Error in getWaitingManualBuildTriggers: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Gets any rebuilds for the specified schedule, rebuilds are a map from trigger to build.
 	 * It is ordered by the start time of the trigger.
 	 *
 	 * @param schedule
 	 * @return a map of rebuilds
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public Map getRebuilds(Schedule schedule) {
 		synchronized (schedLock) {
 			try {
 				Map rebuilds = new HashMap();
 				String[] rebuildTriggerNames = sched.getTriggerNames(BuildGenerator.REBUILD_GROUP);
 				List executingJobs = sched.getCurrentlyExecutingJobs();
 				for (int i = 0; i < rebuildTriggerNames.length; i++) {
 					String[] fields = rebuildTriggerNames[i].split("\\" + Luntbuild.TRIGGER_NAME_SEPERATOR);
 					long buildId = new Long(fields[0]).longValue();
 					Build build = Luntbuild.getDao().loadBuild(buildId);
 					if (build.getSchedule().getId() != schedule.getId())
 						continue;
 					Trigger rebuildTrigger = sched.getTrigger(rebuildTriggerNames[i], BuildGenerator.REBUILD_GROUP);
 					Iterator itExecutingJob = executingJobs.iterator();
 					boolean executing = false;
 					while (itExecutingJob.hasNext()) {
 						JobExecutionContext jobExecutionContext = (JobExecutionContext) itExecutingJob.next();
 						if (rebuildTrigger.equals(jobExecutionContext.getTrigger())) {
 							executing = true;
 							break;
 						}
 					}
 					if (!executing)
 						rebuilds.put(rebuildTrigger, build);
 				}
 				List rebuildTriggers = new ArrayList(rebuilds.keySet());
 				Collections.sort(rebuildTriggers, new TriggerStartTimeComparator());
 				Iterator itRebuildTrigger = rebuildTriggers.iterator();
 				Map orderedRebuilds = new LinkedHashMap();
 				while (itRebuildTrigger.hasNext()) {
 					Trigger trigger = (Trigger) itRebuildTrigger.next();
 					orderedRebuilds.put(trigger, rebuilds.get(trigger));
 				}
 				return orderedRebuilds;
 			} catch (Exception e) {
 				logger.error("Error in getRebuilds: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Initialize and startup the scheduler.
 	 * 
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void startup() {
 		try {
 			StdSchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
 			Properties props = new Properties();
             InputStream quartzIs = Luntbuild.appContext.getServletContext().getResourceAsStream("/WEB-INF/quartz.properties");
             if (quartzIs != null) {
             	try {
             		props.load(quartzIs);
             	} catch (Exception e) {
     				logger.error("Failed to load quartz properties");
     			}
             } else {
             	logger.error("Unable to initialize /WEB-INF/quartz.properties");
             	throw new Exception("Unable to initialize /WEB-INF/quartz.properties");
             }
             String quartzStore = props.getProperty("org.quartz.jobStore.class", "");
             if (!StringUtils.isEmpty(quartzStore) && quartzStore.equals("org.quartz.impl.jdbcjobstore.JobStoreTX"))
             	resolveJdbcVars(props);
             
 			String buildThreadCountText = (String) Luntbuild.getProperties().get(Constants.BUILD_THREAD_COUNT);
 			if (!Luntbuild.isEmpty(buildThreadCountText))
 				props.setProperty("org.quartz.threadPool.threadCount", buildThreadCountText);
 			else
 				props.setProperty("org.quartz.threadPool.threadCount", "10");
 
 			schedFact.initialize(props);
 			sched = schedFact.getScheduler();
 			sched.start();
 		} catch (Exception e) {
 			logger.error("Error in startup: ", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void resolveJdbcVars(Properties props) {
 		Properties jdbcProps = new Properties();
         InputStream jdbcIs = Luntbuild.appContext.getServletContext().getResourceAsStream("/WEB-INF/jdbc.properties");
         if (jdbcIs != null) {
         	try {
         		jdbcProps.load(jdbcIs);
         	} catch (Exception e) {
         		logger.error("Unable to initialize /WEB-INF/jdbc.properties");
 			}
         } else {
         	logger.error("Unable to initialize /WEB-INF/jdbc.properties");
         }
         
 		String propStr = jdbcProps.getProperty("quartz.delegateClassName");
 		if (!StringUtils.isEmpty(propStr)) {
 			props.setProperty("org.quartz.jobStore.driverDelegateClass", propStr);
 		} else {
 			logger.error("Unable to set property org.quartz.dataSource.LuntbuildDS.driver");
 		}
 		propStr = jdbcProps.getProperty("jdbc.driverClassName");
 		if (!StringUtils.isEmpty(propStr)) {
 			props.setProperty("org.quartz.dataSource.LuntbuildDS.driver", propStr);
 		} else {
 			logger.error("Unable to set property org.quartz.dataSource.LuntbuildDS.driver");
 		}
 		propStr = jdbcProps.getProperty("jdbc.url");
 		if (!StringUtils.isEmpty(propStr)) {
 	        Luntbuild.setEmbeddedDbUrls(jdbcProps);
 			String propVal = Luntbuild.resolveProperty(jdbcProps, propStr);
 			props.setProperty("org.quartz.dataSource.LuntbuildDS.URL", propVal);
 		} else {
 			logger.error("Unable to set property org.quartz.dataSource.LuntbuildDS.URL");
 		}
 	}
 	
 	/**
 	 * Shutdown the scheduler. The scheduler can not be re-started after a shutdown.
 	 */
 	public void shutdown() {
 		if (sched != null) {
 			try {
 				sched.shutdown(true);
 			} catch (Exception e) {
 				logger.debug("Error in shutdown: ", e);
 			}
 		}
 	}
 
 	/**
 	 * Validates a trigger.
 	 * 
 	 * @param trigger the trigger
 	 * @throws ValidationException if the trigger is not valid
 	 * @throws RuntimeException if validate fails without a message
 	 */
 	public void validateTrigger(Trigger trigger) {
 		synchronized (schedLock) {
 			try {
 				JobDetail jobDetail = sched.getJobDetail(DUMMY_JOB_NAME, Scheduler.DEFAULT_GROUP);
 				if (jobDetail == null) {
 					jobDetail = new JobDetail();
 					jobDetail.setDurability(true);
 					jobDetail.setGroup(Scheduler.DEFAULT_GROUP);
 					jobDetail.setName(DUMMY_JOB_NAME);
 					jobDetail.setJobClass(BuildGenerator.class);
 					sched.addJob(jobDetail, false);
 				}
 				trigger.setGroup(Scheduler.DEFAULT_GROUP);
 				trigger.setName(DUMMY_TRIGGER_NAME);
 				trigger.setJobGroup(Scheduler.DEFAULT_GROUP);
 				trigger.setJobName(DUMMY_JOB_NAME);
 
 				sched.scheduleJob(trigger);
 				sched.unscheduleJob(DUMMY_TRIGGER_NAME, Scheduler.DEFAULT_GROUP);
 			} catch (Exception e) {
 				logger.error("Error in validateTrigger", e);
 				if (e.getMessage() != null)
 					throw new ValidationException(e.getMessage());
 				else
 					throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Removes un-necessary manual builds for a schedule. Un-necessary manual
 	 * builds are defined as builds that are scheduled to occur before time this function is called.
 	 * 
 	 * @param schedule the schedule
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void removeUnNecessaryManualTriggers(Schedule schedule) {
 		synchronized (schedLock) {
 			try {
 				Trigger manualTriggers[] = sched.getTriggersOfJob(schedule.getJobName(), Scheduler.DEFAULT_GROUP);
 				List executingJobs = sched.getCurrentlyExecutingJobs();
 				for (int i = 0; i < manualTriggers.length; i++) {
 					Trigger manualTrigger = manualTriggers[i];
 					if (!manualTrigger.getGroup().equals(BuildGenerator.MANUALBUILD_GROUP))
 						continue;
 					BuildParams buildParams = Schedule.parseTriggerName(manualTrigger.getName());
 					if (buildParams.getScheduleId() != schedule.getId())
 						continue;
 					Iterator itExecutingJob = executingJobs.iterator();
 					boolean executing = false;
 					while (itExecutingJob.hasNext()) {
 						JobExecutionContext jobExecutionContext = (JobExecutionContext) itExecutingJob.next();
 						if (manualTrigger.equals(jobExecutionContext.getTrigger())) {
 							executing = true;
 							break;
 						}
 					}
 					if (!executing && manualTrigger.getStartTime().before(new Date()))
 						sched.unscheduleJob(manualTrigger.getName(), manualTrigger.getGroup());
 				}
 			} catch (Exception e) {
 				logger.error("Error in removeUnNecessaryManualTriggers: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Checks if the specified trigger is scheduled.
 	 * 
 	 * @param trigger the trigger
 	 * @return <code>true</code> if the trigger is scheduled
 	 */
 	public boolean isTriggerAvailable(Trigger trigger) {
 		synchronized (schedLock) {
 			try {
 				if (sched.getTriggerState(trigger.getName(), trigger.getGroup()) != Trigger.STATE_NONE)
 					return true;
 				else
 					return false;
 			} catch (SchedulerException e) {
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Schedules system backup jobs. Ensures that the jobs are schedule, safe to call multiple times.
 	 * 
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void scheduleSystemBackup() {
 		validateSched();
 		synchronized (schedLock) {
 			try {
 				if (sched.getTriggerState(SystemBackup.TRIGGER_NAME, SystemBackup.TRIGGER_GROUP) != Trigger.STATE_NONE)
 					sched.unscheduleJob(SystemBackup.TRIGGER_NAME, SystemBackup.TRIGGER_GROUP);
 				String cronExpression = (String) Luntbuild.getProperties().get(Constants.BACKUP_CRON_EXPRESSION);
 				if (!Luntbuild.isEmpty(cronExpression)) {
 					JobDetail jobDetail = sched.getJobDetail(SystemBackup.JOB_NAME, SystemBackup.JOB_GROUP);
 					if (jobDetail == null) {
 						jobDetail = new JobDetail();
 						jobDetail.setDurability(true);
 						jobDetail.setGroup(SystemBackup.JOB_GROUP);
 						jobDetail.setName(SystemBackup.JOB_NAME);
 						jobDetail.setJobClass(SystemBackup.class);
 						sched.addJob(jobDetail, false);
 					}
 					CronTrigger trigger = new CronTrigger();
 					trigger.setGroup(SystemBackup.TRIGGER_GROUP);
 					trigger.setName(SystemBackup.TRIGGER_NAME);
 					trigger.setJobGroup(SystemBackup.JOB_GROUP);
 					trigger.setJobName(SystemBackup.JOB_NAME);
 					trigger.setCronExpression(cronExpression);
 					trigger.setStartTime(new Date(System.currentTimeMillis()));
 					sched.scheduleJob(trigger);
 				}
 			} catch (Exception e) {
 				logger.error("Error in scheduleSystemBackup: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Schedules system care jobs. Ensures that the jobs are schedule, safe to call multiple times.
 	 * 
 	 * @throws RuntimeException from {@link Scheduler}
 	 */
 	public void scheduleSystemCare() {
 		validateSched();
 		synchronized (schedLock) {
 			try {
 				if (sched.getTriggerState(SystemCare.TRIGGER_NAME, SystemCare.TRIGGER_GROUP) != Trigger.STATE_NONE)
 					sched.unscheduleJob(SystemCare.TRIGGER_NAME, SystemCare.TRIGGER_GROUP);
 				JobDetail jobDetail = sched.getJobDetail(SystemCare.JOB_NAME, SystemCare.JOB_GROUP);
 				if (jobDetail == null) {
 					jobDetail = new JobDetail();
 					jobDetail.setDurability(true);
 					jobDetail.setGroup(SystemCare.JOB_GROUP);
 					jobDetail.setName(SystemCare.JOB_NAME);
 					jobDetail.setJobClass(SystemCare.class);
 					sched.addJob(jobDetail, false);
 				}
 				CronTrigger trigger = new CronTrigger();
 				trigger.setGroup(SystemCare.TRIGGER_GROUP);
 				trigger.setName(SystemCare.TRIGGER_NAME);
 				trigger.setJobGroup(SystemCare.JOB_GROUP);
 				trigger.setJobName(SystemCare.JOB_NAME);
 				trigger.setCronExpression("0 0 1 * * ?");
 				trigger.setStartTime(new Date(System.currentTimeMillis()));
 				sched.scheduleJob(trigger);
 			} catch (Exception e) {
 				logger.error("Error in scheduleSystemCare: ", e);
 				throw new RuntimeException(e);
 			}
 		}
 	}
 }

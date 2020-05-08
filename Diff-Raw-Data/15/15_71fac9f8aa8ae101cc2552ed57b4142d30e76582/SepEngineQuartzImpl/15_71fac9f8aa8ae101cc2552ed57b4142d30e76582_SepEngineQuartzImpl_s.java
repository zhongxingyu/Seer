 /*
 Copyright (c) 2013 J. L. Canales Gasco
  
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA}]
  */
 package org.rotarysource.core.sep;
 
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.quartz.JobBuilder;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import static org.quartz.TriggerBuilder.*;
 import static org.quartz.SimpleScheduleBuilder.*;
 import org.quartz.Trigger;
 import org.quartz.TriggerBuilder;
 import org.rotarysource.core.sep.job.JobDescription;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.ObjectFactory;
 import org.springframework.context.Lifecycle;
 import org.springframework.util.Assert;
 
 /**
  * Scheduled Event Processor engine. This class Manage the job scheduling
  * 
  */
 public class SepEngineQuartzImpl implements SepEngine, Lifecycle {
 
 	/**
 	 * Apache commons login logger instance
 	 */
 	private static Logger log = LoggerFactory
 			.getLogger(SepEngineQuartzImpl.class);
 
 	/**
 	 * Quartz scheduler wrapper
 	 */
 	private transient final Scheduler scheduler;
 
 	/**
 	 * Quartz jobDetail factory Map This Map include the different object
 	 * factories to create all job types available
 	 */
 	private transient final HashMap<String, ObjectFactory<JobDetail>> jobDetailFactoryMap;
 
 	/**
 	 * Constructor of the <code>PEMScheduler</code> object.
 	 * 
 	 * This object is created by spring framework using this constructor.
 	 * 
 	 * @param scheduler
 	 *            The Quartz scheduler
 	 * @param jobDetailFactory
 	 *            Job detail factory.
 	 * @param jobTriggerFactory
 	 *            Trigger factory.
 	 * @throws SchedulerException
 	 *             Returned exception when a Quartz initialization error happens
 	 */
 	public SepEngineQuartzImpl(
 			final Scheduler aiScheduler,
 			final HashMap<String, ObjectFactory<JobDetail>> aiJobDetailFactoryMap) {
 
 		Assert.notNull(aiScheduler,
 				"An scheduler is needed to create SepEngine");
 		Assert.notNull(aiJobDetailFactoryMap, "Job Factory Map cannot be null!");
 
 
 		this.scheduler = aiScheduler;
 		this.jobDetailFactoryMap = aiJobDetailFactoryMap;
 
 		if (log.isInfoEnabled()) {
 			log.info("==============================================");
 			log.info("Initializing Scheduled Events Processor Engine");
 			log.info("==============================================");
 
 			Set<String> jobKeys = this.jobDetailFactoryMap.keySet();
 			Iterator<String> keyIt = jobKeys.iterator();
 			while (keyIt.hasNext()) {
 				String key = (String) keyIt.next();
 				log.info("Registered jobDetailFactory for job type: {}", key);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.rotarysource.core.sep.SepEngineIntrf#scheduleJob(org.rotarysource
 	 * .core.sep.job.JobDescription)
 	 */
 	@Override
 	public final void scheduleJob(final JobDescription jobDescription)
 			throws SchedulerException {
 		Assert.notNull(jobDescription, "Job to scheduler cannot be null!");
 
 		if (jobDetailFactoryMap.containsKey(jobDescription.getJobFactoryId()) == false) {
 			// Manage exception
 			StringBuffer errorTxt = new StringBuffer("Job Type not found for: ");
 			errorTxt.append(jobDescription.toString());
 
 			log.error(errorTxt.toString());
 			throw (new IllegalArgumentException(errorTxt.toString()));
 
 		}
 
 		JobDetail jobDetail = jobDetailFactoryMap.get(jobDescription.getJobFactoryId())
 												 .getObject();
 		
 		if (jobDescription.getTaskParams() != null)
 			jobDetail.getJobDataMap().put("taskParams", jobDescription.getTaskParams());
 
 		jobDetail = jobDetail.getJobBuilder()
 							.withIdentity(jobDescription.getName(), jobDescription.getGroup())
 							.requestRecovery(true)
 							.build();
 		
 		
 	    // Trigger the job to run now, and then repeat every 40 seconds
 	    Trigger trigger = newTrigger()
 	    					.withIdentity(jobDescription.getName(), jobDescription.getGroup())
 							.withSchedule(simpleSchedule()
 											.withRepeatCount(0)
 											.withMisfireHandlingInstructionFireNow())
 							.startAt(jobDescription.getFireDate())
 							.build();
 		
 		
 		innerScheduleJob(jobDetail, trigger);
 	}
 
 	/**
 	 * Schedule or reschedule a job based on its jobDetail and Trigger If the
 	 * asked job doesnt exists it will be scheduled with trigger. If the asked
 	 * job exists in the scheduler, its definition will be updated with the new
 	 * params and it will be rescheduled with the new trigger definition.
 	 * 
 	 * @param jobDetail
 	 *            Job to be scheduled
 	 * @param trigger
 	 *            Trigger definition for the job
 	 * @throws SchedulerException
 	 */
 	private void innerScheduleJob(final JobDetail jobDetail,
 			final Trigger trigger) throws SchedulerException {
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
		if (null == scheduler.getTrigger(trigger.getKey())) {
 			log.info("Scheduling New Job {}  at date: {}",
 					(jobDetail.getKey().getGroup() +":"+jobDetail.getKey().getName()), 
 					df.format(trigger.getStartTime()));
 
 			scheduler.scheduleJob(jobDetail, trigger);
 		} else {
 			log.info("Rescheduling Existing Job {}  at date: {}",
 					(jobDetail.getKey().getGroup() +":"+jobDetail.getKey().getName()), 
 					df.format(trigger.getStartTime()));
 			
 			scheduler.addJob(jobDetail, true);
			scheduler.rescheduleJob(trigger.getKey(),
					trigger);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.context.Lifecycle#isRunning()
 	 */
 	@Override
 	public boolean isRunning() {
 		boolean status = false;
 		try {
 			status = this.scheduler.isStarted();
 		} catch (SchedulerException e) {
 			log.error(
 					"ERROR accessing scheduler Quarth Engine. Nested exception: {}",
 					e.getMessage());
 			status = false;
 		}
 		return status;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.context.Lifecycle#start()
 	 */
 	@Override
 	public void start() {
 
 		// Start Scheduler
 		log.info("Starting scheduler Quarth Engine");
 		try {
 			if (!this.scheduler.isStarted()) {
 				this.scheduler.startDelayed(15);
 				log.info("Quarth Engine will be Started in 15 seconds;");
 			} else {
 				log.info("Quarth Engine Already Started; ");
 			}
 		} catch (SchedulerException e) {
 			log.error(
 					"ERROR starting scheduler Quarth Engine. Nested exception: {}",
 					e.getMessage());
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.context.Lifecycle#stop()
 	 */
 	@Override
 	public void stop() {
 		// Stop Scheduler
 		log.info("Stoping scheduler Quarth Engine");
 		try {
 			if (this.scheduler.isStarted()) {
 				this.scheduler.shutdown();
 				log.info("Quarth Engine Stopped;");
 			} else {
 				log.info("Quarth Engine Already Stopped; ");
 			}
 		} catch (SchedulerException e) {
 			log.error(
 					"ERROR stopping scheduler Quarth Engine. Nested exception: {}",
 					e.getMessage());
 		}
 
 	}
 }

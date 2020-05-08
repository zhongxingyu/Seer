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
 
 import com.luntsys.luntbuild.db.Schedule;
 import org.quartz.Trigger;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Luntbuild scheduler interface.
  *
  * @author robin shine
  */
 public interface IScheduler {
 	/**
 	 * Reschedules builds based on build scheduling information existed in the persistent storage.
 	 */
 	void rescheduleBuilds();
 
 	/**
 	 * Schedules a build for specified schedule using specified trigger.
 	 *
 	 * @param schedule the schedule
 	 * @param trigger the trigger
 	 */
 	void scheduleBuild(Schedule schedule, Trigger trigger);
 
 	/**
 	 * Removes the specified trigger.
 	 *
 	 * @param triggerName the name of the trigger
 	 * @param triggerGroup the group the trigger is in
 	 */
 	void unscheduleBuild(String triggerName, String triggerGroup);
 
 	/**
 	 * Gets the list of triggers manually triggered for the specified schedule, the list is ordered ascending
 	 * by the start time of the trigger.
 	 *
 	 * @param schedule the schedule
 	 * @return the manual triggers
 	 */
 	List getWaitingManualBuildTriggers(Schedule schedule);
 
 	/**
 	 * Gets any rebuilds for the specified schedule, rebuilds are a map from trigger to build.
 	 * It is ordered by the start time of the trigger.
 	 *
 	 * @param schedule
 	 * @return a map of rebuilds
 	 */
 	Map getRebuilds(Schedule schedule);
 
 	/**
 	 * Initialize and startup the scheduler.
 	 */
 	void startup();
 
 	/**
 	 * Shutdown the scheduler.
 	 */
 	void shutdown();
 
 	/**
 	 * Validates a trigger.
 	 * 
 	 * @param trigger the trigger
 	 */
 	void validateTrigger(Trigger trigger);
 
 	/**
 	 * Removes un-necessary manual builds for a schedule. Un-necessary manual
 	 * builds are defined as builds that are scheduled to occur before time this function is called.
 	 * 
 	 * @param schedule the schedule
 	 */
	void resetManualTriggers(Schedule schedule);
 
 	/**
 	 * Checks if the specified trigger is scheduled.
 	 * 
 	 * @param trigger the trigger
 	 * @return <code>true</code> if the trigger is scheduled
 	 */
 	boolean isTriggerAvailable(Trigger trigger);
 
 	/**
 	 * Schedules system backup jobs. Ensures that the jobs are schedule, safe to call multiple times.
 	 */
 	void scheduleSystemBackup();
 
 	/**
 	 * Schedules system care jobs. Ensures that the jobs are schedule, safe to call multiple times.
 	 */
 	void scheduleSystemCare();
 }

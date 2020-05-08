 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.util.scheduler;
 
 import java.text.ParseException;
 
 import org.quartz.CronTrigger;
 import org.quartz.Trigger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public abstract class PeriodicTask extends TysanTask {
 	public enum ExecutionMode {
 		// Seconds, Minutes, Hours, Day-of-month, Month, Day-of-week, Year
 		HOURLY("0 0 */1 * * ?"), ONCE_EVERY_TWO_HOURS("0 0 */2 * * ?"), ONCE_EVERY_FOUR_HOURS(
 				"0 0 */4 * * ?"), ONCE_EVERY_SIX_HOURS("0 0 */6 * * ?"), ONCE_EVERY_TWELVE_HOURS(
 				"0 0 */12 * * ?"), DAILY("0 0 6 * * ?"), WEEKLY("0 0 6 * * SUN"), MONTHLY(
 				"0 0 6 1 * ?"), ANNUALLY("0 0 6 1 1 ?"), EVERY_FIVE_MINUTES(
				"* */5 * * * ?"), DEBUG("* */1 * * * ?");
 
 		/**
 		 * 
 		 */
 		private ExecutionMode(String cronExpression) {
 			this.cronExpression = cronExpression;
 		}
 
 		private final String cronExpression;
 
 		/**
 		 * @return the cronExpression
 		 */
 		String getCronExpression() {
 			return cronExpression;
 		}
 	}
 
 	private static Logger log = LoggerFactory.getLogger(PeriodicTask.class);
 
 	private ExecutionMode executionMode;
 
 	protected PeriodicTask(String name, String group,
 			ExecutionMode executionMode) {
 		super(name, group);
 		this.executionMode = executionMode;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.util.scheduler.TysanTask#getQuartzTrigger()
 	 */
 	@Override
 	public Trigger getQuartzTrigger() {
 
 		CronTrigger trigger = new CronTrigger(getName(), getGroup());
 		try {
 			trigger.setCronExpression(executionMode.getCronExpression());
 		} catch (ParseException e) {
 			log.error(e.getMessage(), e);
 		}
 
 		return trigger;
 
 		// For debugging
 		// return new SimpleTrigger(getName(), getGroup(), 0,
 		// 0);
 	}
 
 }

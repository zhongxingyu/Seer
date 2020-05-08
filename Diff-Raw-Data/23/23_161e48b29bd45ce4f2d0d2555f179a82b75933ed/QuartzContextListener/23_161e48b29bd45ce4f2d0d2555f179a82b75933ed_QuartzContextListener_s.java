 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.quartz.web;
 
 import iddb.quartz.job.JobManager;
 
 import java.util.Map.Entry;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class QuartzContextListener implements ServletContextListener {
 
 	private static Logger log = LoggerFactory.getLogger(QuartzContextListener.class);
 	
 	private Scheduler scheduler;
 	
 	/* (non-Javadoc)
 	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
 	 */
 	@Override
 	public void contextInitialized(ServletContextEvent sce) {
 		// Initialize the Quartz Engine
 		log.debug("Init Quartz Engine");
 		try {
 			scheduler = StdSchedulerFactory.getDefaultScheduler();
 			
 			log.debug("Loading jobs");
 			JobManager manager = new JobManager();
 			for (Entry<JobDetail, Trigger> job : manager.getJobs().entrySet()) {
 				scheduler.scheduleJob(job.getKey(), job.getValue());
 			}
 			log.debug("Starting scheduller");
 			scheduler.start();
 		} catch (SchedulerException e) {
 			log.error(e.getMessage());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
 	 */
 	@Override
 	public void contextDestroyed(ServletContextEvent sce) {
 		log.debug("Shutting down quartz engine");
 		try {
			if (scheduler != null) scheduler.shutdown();
 		} catch (SchedulerException e) {
 			log.error(e.getMessage());
 		}
 	}
 
 	
 }

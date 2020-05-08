 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.timer;
 
 import com.flexive.core.security.UserTicketImpl;
 import com.flexive.core.timer.jobs.MaintenanceJob;
 import com.flexive.shared.FxContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.quartz.impl.SchedulerRepository;
 import org.quartz.impl.StdSchedulerFactory;
 import org.quartz.impl.jdbcjobstore.JobStoreCMT;
 import org.quartz.simpl.SimpleThreadPool;
 
 import java.util.Properties;
 
 /**
  * Quartz scheduler [fleXive] integration
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev
  */
 public class FxQuartz {
 
     private static transient Log LOG = LogFactory.getLog(FxQuartz.class);
 
     public final static String GROUP_INTERNAL = "FxInternal";
     public final static String JOB_MAINTENANCE = "FxMaintenanceJob";
 
     /**
      * Get the Scheduler for the current division
      *
      * @return Scheduler for the current division
      */
     public static Scheduler getScheduler() {
         return SchedulerRepository.getInstance().lookup("FxQuartzScheduler_Division_" + FxContext.get().getDivisionId());
     }
 
     /**
      * Start the Quartz scheduler
      *
      * @throws SchedulerException on errors
      */
     public static void startup() throws SchedulerException {
 
         FxContext currCtx = FxContext.get();
         if (currCtx.isTestDivision()) {
             //disable scheduler for embedded JBoss container (I couldn't figure out how to configure a non managed connection ...)
             LOG.info("Quartz scheduler disabled for embedded (test) container.");
             return;
         }
 
         // Grab the Scheduler instance from the Factory
         Properties props = new Properties();
         props.put(StdSchedulerFactory.PROP_DATASOURCE_PREFIX, "fxQuartzDS");
        props.put("org.quartz.dataSource.fxQuartzDS." + StdSchedulerFactory.PROP_CONNECTION_PROVIDER_CLASS, FxQuartzConnectionProvider.class.getCanonicalName());
         props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "FxScheduler");
         props.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, JobStoreCMT.class.getCanonicalName());
         props.put(StdSchedulerFactory.PROP_JOB_STORE_PREFIX, "QRTZ_");
         props.put(StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID, true);
         props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getCanonicalName());
         props.put("org.quartz.threadPool.threadCount", String.valueOf(5));
         props.put("org.quartz.jobStore.dataSource", "fxQuartzDS");
         props.put("org.quartz.jobStore.nonManagedTXDataSource", "fxQuartzNoTXDS");
         props.put("org.quartz.dataSource.fxQuartzNoTXDS." + StdSchedulerFactory.PROP_CONNECTION_PROVIDER_CLASS, FxQuartzConnectionProviderNonTX.class.getCanonicalName());
         props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "FxQuartzScheduler_Division_" + FxContext.get().getDivisionId());
 
         Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
         FxContext ctx = FxContext._getEJBContext(currCtx);
         ctx.overrideTicket(UserTicketImpl.getGuestTicket().cloneAsGlobalSupervisor());
         scheduler.getContext().put("com.flexive.ctx", ctx);
         // and start it off
         scheduler.start();
 
         //delete maintenance job incase it exists or is persisted
         scheduler.deleteJob(JOB_MAINTENANCE, GROUP_INTERNAL);
 
         JobDetail job = new JobDetail(JOB_MAINTENANCE, GROUP_INTERNAL, MaintenanceJob.class);
         job.setVolatility(true);
         //trigger every 30 minutes
         SimpleTrigger tr = new SimpleTrigger(JOB_MAINTENANCE, GROUP_INTERNAL);
         tr.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
         tr.setRepeatInterval(1800000L); //30 minutes
 //        tr.setRepeatInterval(10000L); //10 seconds for testing purposes
         tr.setVolatility(true);
 
         scheduler.scheduleJob(job, tr);
         LOG.info("Quartz started and maintenance job is scheduled.");
     }
 
     /**
      * Shutdown the scheduler
      *
      * @throws SchedulerException on errors
      */
     public static void shutdown() throws SchedulerException {
         if (isInstalled())
             getScheduler().shutdown();
     }
 
     /**
      * Is the scheduler installed?
      *
      * @return scheduler installed
      */
     public static boolean isInstalled() {
         return getScheduler() != null;
     }
 }

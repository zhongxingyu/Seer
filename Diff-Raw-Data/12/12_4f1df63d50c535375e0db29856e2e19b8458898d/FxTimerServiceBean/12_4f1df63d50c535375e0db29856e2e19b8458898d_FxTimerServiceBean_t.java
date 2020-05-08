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
 package com.flexive.ejb.beans;
 
 import com.flexive.core.timer.FxQuartz;
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.interfaces.FxTimerService;
 import com.flexive.shared.interfaces.FxTimerServiceLocal;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.structure.TypeStorageMode;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 
 /**
  * Timer- and scheduling service based on Quartz
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "FxTimerService")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class FxTimerServiceBean implements FxTimerService, FxTimerServiceLocal {
 
     private static transient Log LOG = LogFactory.getLog(FxTimerServiceBean.class);
 
     /**
      * timer interval in minutes
      */
 //    private static int INTERVAL = 10;
 
     /**
      * Signature used to check if the timer is installed
      */
 //    private final static String TIMER_SIGNATURE = "FxTimer";
 
 //    private volatile boolean foundMBean = false;
 
     @Resource
     SessionContext ctx;
 
 
     /**
      * {@inheritDoc}
      */
     public boolean install(boolean reinstall) {
         try {
             FxQuartz.startup();
         } catch (SchedulerException e) {
             LOG.error("Failed to start Quartz scheduler: " + e.getMessage(), e);
             return false;
         }
         return true;
         /*
         //original EJB timer code ...
         if (isInstalled())
             uninstall();
         //install a timer that runs every minute
         final boolean installed = isInstalled();
         if (!installed && ctx != null && ctx.getTimerService() != null) {
             ctx.getTimerService().createTimer(1000L * 60 * INTERVAL, 1000L * 60 * INTERVAL, TIMER_SIGNATURE);
             LOG.info("FxTimer created");
             return true;
         } else {
             if (ctx != null)
                 LOG.fatal("TimerService is not available! (Still an alpha build?)");
             else {
                 LOG.warn("TimerService already installed!");
                 return true;
             }
         }
         LOG.warn("Performing timer maintenance on startup due to failed service!");
         perform(null);
         return false;*/
     }
 
     /**
      * {@inheritDoc}
      */
     public void uninstall() {
         try {
             FxQuartz.shutdown();
         } catch (SchedulerException e) {
             LOG.error("Failed to shutdown Quartz scheduler: " + e.getMessage(), e);
         }
         /*
         //original EJB timer code ...
 
         try {
             if (ctx != null && ctx.getTimerService() != null) {
                 for (Timer t : (Collection<Timer>) ctx.getTimerService().getTimers()) {
                     if (t.getInfo().equals(TIMER_SIGNATURE)) {
                         t.cancel();
                         LOG.info(TIMER_SIGNATURE + " canceled!");
                     }
                 }
             }
         } catch (Exception e) {
             LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
         }*/
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public boolean isInstalled() {
         return FxQuartz.isInstalled();
         /*
 
         //original EJB timer code ...
         try {
             if (ctx != null && ctx.getTimerService() != null) {
                 for (Timer t : (Collection<Timer>) ctx.getTimerService().getTimers()) {
                     if (t.getInfo().equals(TIMER_SIGNATURE))
                         return true;
                 }
             }
         } catch (Exception e) {
             LOG.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
         }
         return false;*/
     }
 
     /*
 
       //original EJB timer code ...
 
         / **
          * Timer function, calls periodical tasks for all active divisions
          *
          * @param timer the timer
          * /
         @Timeout
         @TransactionAttribute(TransactionAttributeType.REQUIRED)
         public void perform(Timer timer) {
             if( true) {
                 System.out.println("Skipping EJB Timer ...");
                 return;
             }
             if (!foundMBean) {
                 if (!CacheAdmin.isCacheMBeanInstalled()) {
                     //this is where cache errors would occur due to serialized timers that are restarted in jboss
                     return;
                 } else
                     foundMBean = true;
             }
             //place periodic maintenance code here ...
             try {
                 for (DivisionData dd : EJBLookup.getGlobalConfigurationEngine().getDivisions()) {
                     if (dd.getId() <= 0 || !dd.isAvailable())
                         continue;
                     Connection con = null;
                     try {
                         con = Database.getDbConnection(dd.getId());
                         for (TypeStorageMode mode : TypeStorageMode.values()) {
                             if (!mode.isSupported())
                                 continue;
                             StorageManager.getContentStorage(dd, mode).maintenance(con);
                         }
                     } catch (Exception e) {
                         LOG.error("Failed to perform maintenance for division #" + dd.getId() + ": " + e.getMessage(), e);
                     } finally {
                         Database.closeObjects(FxTimerServiceBean.class, con, null);
                     }
                 }
             } catch (FxApplicationException e) {
                 LOG.error("Maintenance error: " + e.getMessage(), e);
             }
         }
     */
 
     /**
      * {@inheritDoc}
      */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
     public void maintenance() {
         try {
             for (DivisionData dd : EJBLookup.getGlobalConfigurationEngine().getDivisions()) {
                 if (dd.getId() <= 0 || !dd.isAvailable())
                     continue;
                 if (LOG.isDebugEnabled())
                     LOG.debug("Performing maintenance for division #" + dd.getId() + " ... ");
                 Connection con = null;
                 try {
                     con = Database.getDbConnection(dd.getId());
                     for (TypeStorageMode mode : TypeStorageMode.values()) {
                         if (!mode.isSupported())
                             continue;
                         StorageManager.getContentStorage(dd, mode).maintenance(con);
                     }
                 } catch (Exception e) {
                     LOG.error("Failed to perform maintenance for division #" + dd.getId() + ": " + e.getMessage(), e);
                 } finally {
                     Database.closeObjects(FxTimerServiceBean.class, con, null);
                 }
             }
         } catch (FxApplicationException e) {
             LOG.error("Maintenance error: " + e.getMessage(), e);
         }
 
     }
 }

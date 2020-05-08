 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.FxTimerService;
 import com.flexive.shared.interfaces.FxTimerServiceLocal;
 import com.flexive.shared.structure.TypeStorageMode;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.util.Collection;
 
 /**
  * Periodical timer service
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "FxTimerService")
 @TransactionAttribute(TransactionAttributeType.REQUIRED)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class FxTimerServiceBean implements FxTimerService, FxTimerServiceLocal {
 
     private static transient Log LOG = LogFactory.getLog(FxTimerServiceBean.class);
 
     /**
      * timer interval in minutes
      */
     private static int INTERVAL = 10;
 
     /**
      * Signature used to check if the timer is installed
      */
     private final static String TIMER_SIGNATURE = "FxTimer";
 
     @Resource
     SessionContext ctx;
 
 
     /**
      * {@inheritDoc}
      */
     public boolean install(boolean reinstall) {
         if( isInstalled() )
             uninstall();
         //install a timer that runs every minute
         final boolean installed = isInstalled();
        if (!installed && ctx != null && ctx.getTimerService() != null) {
             ctx.getTimerService().createTimer(1000L * 60 * INTERVAL, 1000L * 60 * INTERVAL, TIMER_SIGNATURE);
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
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public void uninstall() {
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
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public boolean isInstalled() {
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
         return false;
     }
 
     /**
      * Timer function, calls periodical tasks for all active divisions
      *
      * @param timer the timer
      */
     @Timeout
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void perform(Timer timer) {
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
 }

 /**
  * This file is part of RibbonWeb application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package controllers;
 
 import java.util.List;
 import play.db.ebean.Model;
 
 /**
  * Thread wich post messages to the server and mark probes.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class SenderThread extends Thread {
     
     /**
      * Lock for beetween this thread and 
      */
     private Object dataLock = new Object();
     
     public Integer postCounter = 0;
     
     public Integer editCounter = 0;
     
     public Integer errCounter = 0;
     
     @Override
     public void run() {
         while (MiniGate.isGateReady) {
             postMessages();
             try {
                 Thread.sleep(30 * 1000);
             } catch (InterruptedException ex) {
                 //Do nothing just postMessages()
             }
         }
     }
     
     /**
      * Get lock for data.
      * @return lock object;
      */
     public Object getLock() {
         return dataLock;
     }
     
     /**
      * Check database for new messages post and mark them.
      */
     private void postMessages() {
        List<models.MessageProbe> probes = (List<models.MessageProbe>) new Model.Finder(String.class, models.MessageProbe.class).where().ne("curr_status", 1).findList();
         for (models.MessageProbe currProbe : probes) {
             if ((currProbe.curr_status == models.MessageProbe.STATUS.WITH_ERROR) || (currProbe.curr_status == models.MessageProbe.STATUS.NEW)) {
                 String contextErr = MiniGate.gate.sendCommandWithCheck("RIBBON_NCTL_ACCESS_CONTEXT:{" + currProbe.author + "}");
                 if (contextErr != null) {
                     synchronized (dataLock) {
                         currProbe.curr_status = models.MessageProbe.STATUS.WITH_ERROR;
                         currProbe.curr_error = contextErr;
                         currProbe.update();
                         errCounter++;
                     }
                 } else {
                     String postErr = MiniGate.gate.sendCommandWithCheck(currProbe.getCsvToPost());
                     synchronized (dataLock) {
                         if (postErr != null) {
                             currProbe.curr_status = models.MessageProbe.STATUS.WITH_ERROR;
                             currProbe.curr_error = postErr;
                             errCounter++;
                         } else {
                             currProbe.curr_status = models.MessageProbe.STATUS.POSTED;
                             currProbe.curr_error = null;
                             postCounter++;
                         }
                         currProbe.update();
                     }
                 }
             } else if (currProbe.curr_status == models.MessageProbe.STATUS.DELETED) {
                 currProbe.delete();
             } else if (currProbe.curr_status == models.MessageProbe.STATUS.EDITED) {
                 String contextErr = MiniGate.gate.sendCommandWithCheck("RIBBON_NCTL_ACCESS_CONTEXT:{" + currProbe.author + "}");
                 if (contextErr != null) {
                     synchronized (dataLock) {
                         currProbe.curr_status = models.MessageProbe.STATUS.WITH_ERROR;
                         currProbe.curr_error = contextErr;
                         currProbe.update();
                         errCounter++;
                     }
                 } else {
                     String postErr = MiniGate.gate.sendCommandWithCheck(currProbe.getCsvToModify());
                     synchronized (dataLock) {
                         if (postErr != null) {
                             currProbe.curr_status = models.MessageProbe.STATUS.WITH_ERROR;
                             currProbe.curr_error = postErr;
                             errCounter++;
                         } else {
                             currProbe.curr_status = models.MessageProbe.STATUS.WAIT_CONFIRM;
                             currProbe.curr_error = null;
                             editCounter++;
                         }
                         currProbe.update();
                     }
                 }
             }
         }
     }
     
 }

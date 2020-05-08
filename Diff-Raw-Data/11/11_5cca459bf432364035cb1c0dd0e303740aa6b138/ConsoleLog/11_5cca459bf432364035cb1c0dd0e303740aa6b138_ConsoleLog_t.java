 
 /*
 * ConsoleLog.java            $Revision: 1.4 $ $Date: 2001/05/23 13:54:04 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.util;
 
 
 /**
  * Class ConsoleLog
  *
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision, $Date: 2001/05/23 13:54:04 $
  */
 public class ConsoleLog implements LogService {
 
    private int severity;
 
     public ConsoleLog()
     {
        this.severity = Log.SEV_ERROR;
     }
 
     public ConsoleLog(int severity)
     {
         this.severity = severity;
     }
 
     public void logEntry(int sev, String service, String message)
     {
         if (sev > this.severity) {
             return;
         }
 
         String name = Thread.currentThread().getName();
 
         System.out.println(name + ":" + service + ": " + message);
     }
 
     public void logEntry(int sev, String service, Throwable exception)
     {
         if (sev > this.severity) {
             return;
         }
 
         String name = Thread.currentThread().getName();
 
         System.out.println(name + ":" + service + ": "
                            + exception.toString());
         exception.printStackTrace();
     }
 
     public boolean isLogged(int sev)
     {
         return this.severity >= sev;
     }
 
     /**
      * Method setSeverity
      *
      *
      * @param severity
      *
      */
     public void setSeverity(int severity)
     {
         this.severity = severity;
     }
 }

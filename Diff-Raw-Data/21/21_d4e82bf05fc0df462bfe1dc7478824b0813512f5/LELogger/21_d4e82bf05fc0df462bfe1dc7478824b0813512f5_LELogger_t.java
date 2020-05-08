 /*
  * Kajona Language File Editor Core
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.core.logger;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author sidler
  */
 public class LELogger {
     private static LELogger instance = null;
 
     private ArrayList<ILELoggingListener> listener = new ArrayList<ILELoggingListener>();
 
    private boolean logToConsole = true;
     private boolean includeDate = false;
 
     private LELogger() {
         
     }
 
     public static LELogger getInstance()  {
         if(instance == null)
             instance = new LELogger();
 
         return instance;
     }
 
     public void logInfo(String logContent) {
         //create log-format
         if(includeDate)
             logContent = (new Date()).toString()+" "+logContent;
 
         if(logToConsole)
             System.out.println(logContent);
 
         for(ILELoggingListener listenerInstance : listener)
             listenerInstance.logCallback(logContent);
     }
 
     public void addLoggingListener(ILELoggingListener listener) {
         if(logToConsole)
             System.out.println("Registering LogListener");
         this.listener.add(listener);
         this.logInfo("Listener Startup at "+(new Date()).toString());
     }
 
     public boolean isLogToConsole() {
         return logToConsole;
     }
 
     public void setLogToConsole(boolean logToConsole) {
         this.logToConsole = logToConsole;
     }
 
     public boolean isIncludeDate() {
         return includeDate;
     }
 
     public void setIncludeDate(boolean includeDate) {
         this.includeDate = includeDate;
     }
 
     
 
 
 
 
 }

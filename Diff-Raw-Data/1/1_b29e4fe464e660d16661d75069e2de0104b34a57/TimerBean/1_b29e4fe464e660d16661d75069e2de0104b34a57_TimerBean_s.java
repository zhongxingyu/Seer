 /*
  *   Copyright 2009-2010 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.common.jsf.support.beans;
 
 import java.io.Serializable;
 
 import com.thruzero.common.core.utils.DateTimeFormatUtilsExt;
 import com.thruzero.common.core.utils.PerformanceTimerUtils;
 import com.thruzero.common.core.utils.PerformanceTimerUtils.PerformanceLogger;
 
 /**
  * A timer that can be started from a JSF page, via #{timerBean.init}, and then retrieving the
  * resulting elapsed time via #{timerBean.elapsedTime}.
  *
  * @author George Norman
  */
 @javax.faces.bean.ManagedBean(name="timerBean")
 @javax.faces.bean.RequestScoped
 public class TimerBean implements Serializable {
   private static final long serialVersionUID = 1L;
 
   public TimerBean() {
     PerformanceTimerUtils.set(new PerformanceLogger());
   }
 
   public String getInit() {
     try {
       PerformanceLogger performanceLogger = PerformanceTimerUtils.get();
 
       performanceLogger.debug("# START page");
       performanceLogger.resetAndStart();
     } catch (Exception e) {
       // ignore (don't let stats break app)
     }
 
     return "";
   }
 
   public String getElapsedTime() {
     String result;
 
     try {
       PerformanceLogger performanceLogger = PerformanceTimerUtils.get();
 
      performanceLogger.stop();
       result = DateTimeFormatUtilsExt.formatElapsedTime(performanceLogger.getRunningElapsedMillis());
     } catch (Exception e) {
       result = "N/A";
     }
 
     return result;
   }
 
   public String getDump() {
     String result;
 
     try {
       PerformanceLogger performanceLogger = PerformanceTimerUtils.get();
 
       performanceLogger.debug("# END page");
       result = performanceLogger.getLogEntries();
     } catch (Exception e) {
       result = "N/A";
     }
 
     return "<!-- " + result + " -->";
   }
 }

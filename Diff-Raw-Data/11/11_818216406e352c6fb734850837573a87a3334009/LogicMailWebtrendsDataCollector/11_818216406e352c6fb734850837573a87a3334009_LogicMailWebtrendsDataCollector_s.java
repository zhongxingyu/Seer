 /*-
  * Copyright (c) 2011, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail;
 
 import java.util.Hashtable;
 
 import com.webtrends.mobile.analytics.IllegalWebtrendsParameterValueException;
 import com.webtrends.mobile.analytics.rim.WebtrendsConfigurator;
 import com.webtrends.mobile.analytics.rim.WebtrendsDataCollector;
 import com.webtrends.mobile.analytics.rim.WebtrendsLogger;
 
 public class LogicMailWebtrendsDataCollector extends AnalyticsDataCollector {
     private static String STARTUP_CLASS = "org.logicprobe.LogicMail.LogicMailStartup";
     private static String ANALYTICS_CONFIG_FILE = "webtrends.xml";
     
     private static boolean initialized;
     private final WebtrendsDataCollector dataCollector;
     private final WebtrendsLogger collectorLogger;
     
     public LogicMailWebtrendsDataCollector() {
         if(!initialized) {
             WebtrendsConfigurator.LoadConfigFile(STARTUP_CLASS, ANALYTICS_CONFIG_FILE);
             WebtrendsDataCollector.getInstance().Initialize();
             initialized = true;
         }
         
         dataCollector = WebtrendsDataCollector.getInstance();
         collectorLogger = WebtrendsDataCollector.getLog();
     }
     
     public void setConfigured(boolean configured) {
         WebtrendsDataCollector.setConfigured(configured);
     }
 
     public void onApplicationStart() {
         try {
             dataCollector.onApplicationForeground(AppInfo.getName(), null);
         } catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onApplicationTerminate() {
         try {
             dataCollector.onApplicationTerminate(AppInfo.getName(), null);
         } catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onApplicationForeground() {
         try {
             dataCollector.onApplicationForeground(AppInfo.getName(), null);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onApplicationBackground() {
         try {
             dataCollector.onApplicationBackground(AppInfo.getName(), null);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onApplicationError(String errorMessage) {
         Hashtable customParams = new Hashtable();
         customParams.put("WT.er", errorMessage);
         try {
             dataCollector.onApplicationError(AppInfo.getName(), customParams);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }        
     }
 
     public void onButtonClick(
             String eventPath,
             String eventDesc,
             String eventType) {
         try {
            dataCollector.onButtonClick(eventPath, eventDesc, eventType, null);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onContentView(
             String eventPath,
             String eventDesc,
             String eventType,
             String contentGroup) {
         try {
             dataCollector.onContentView(eventPath, eventDesc, eventType, null, contentGroup);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onMediaEvent(
             String eventPath,
             String eventDesc,
             String eventType,
             String contentGroup,
             String mediaName,
             String mediaType,
             String mediaEventType) {
         try {
            dataCollector.onMediaEvent(eventPath, eventDesc, eventType, null,
                     contentGroup, mediaName, mediaType, mediaEventType);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 
     public void onCustomEvent(
             String eventPath,
             String eventDesc,
             Hashtable customData) {
         try {
             dataCollector.onCustomEvent(eventPath, eventDesc, customData);
         }
         catch (IllegalWebtrendsParameterValueException err) {
             collectorLogger.e(err.getMessage());
         }
     }
 }

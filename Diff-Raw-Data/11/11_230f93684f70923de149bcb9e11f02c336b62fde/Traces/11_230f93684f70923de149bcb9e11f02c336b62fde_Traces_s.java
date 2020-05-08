 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.glass.log.trace;
 
 import org.apache.commons.lang.StringUtils;
 import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.helpers.MessageFormatter;
 
 import com.github.dbourdette.glass.job.annotation.JobArgumentBean;
 import com.github.dbourdette.glass.log.execution.Execution;
 import com.github.dbourdette.glass.util.Page;
 import com.github.dbourdette.glass.util.Query;
 
 /**
  * Sends logs to log store and through slf4j.
  *
  * @author damien bourdette
  */
 public class Traces {
     private static final Logger LOGGER = LoggerFactory.getLogger(Traces.class);
 
     private static final String[] EMPTY_ARGS = new String[]{};
 
     private ThreadLocal<JobExecutionContext> localContext = new ThreadLocal<JobExecutionContext>();
 
     private TraceStore traceStore;
 
     public Traces(TraceStore traceStore) {
         this.traceStore = traceStore;
     }
 
     public void setContext(JobExecutionContext context) {
         localContext.set(context);
     }
 
     public void debug(String message) {
         log(TraceLevel.DEBUG, message);
 
         LOGGER.debug(message);
     }
 
     public void debug(String format, Object... args) {
         log(TraceLevel.DEBUG, format(format, args));
 
         LOGGER.debug(format, args);
     }
 
     public void debug(String message, Throwable throwable) {
         log(TraceLevel.DEBUG, message, throwable);
 
         LOGGER.debug(message, throwable);
     }
 
     public void info(String message) {
         log(TraceLevel.INFO, message);
 
         LOGGER.info(message);
     }
 
     public void info(String format, Object... args) {
         log(TraceLevel.INFO, format(format, args));
 
         LOGGER.info(format, args);
     }
 
     public void info(String message, Throwable throwable) {
         log(TraceLevel.INFO, message, throwable);
 
         LOGGER.info(message, throwable);
     }
 
     public void warn(String message) {
         log(TraceLevel.WARN, message);
 
         LOGGER.warn(message);
     }
 
     public void warn(String format, Object... args) {
         log(TraceLevel.WARN, format(format, args));
 
         LOGGER.warn(format, args);
     }
 
     public void warn(String message, Throwable throwable) {
         log(TraceLevel.WARN, message, throwable);
 
         LOGGER.warn(message, throwable);
     }
 
     public void error(String message) {
         log(TraceLevel.ERROR, message);
 
         LOGGER.error(message);
     }
 
     public void error(String format, Object... args) {
         log(TraceLevel.ERROR, format(format, args));
 
         LOGGER.error(format, args);
     }
 
     public void error(String message, Throwable throwable) {
         log(TraceLevel.ERROR, message, throwable);
 
         LOGGER.error(message, throwable);
     }
 
     public Page<Trace> getLogs(Long executionId, Query query) {
         return traceStore.getLogs(executionId, query);
     }
 
     public Page<Trace> getLogs(Query query) {
         return traceStore.getLogs(query);
     }
 
     public void clear() {
         traceStore.clear();
     }
 
     private void log(TraceLevel level, String message) {
         log(level, message, EMPTY_ARGS);
     }
 
     private void log(TraceLevel level, String format, Object... args) {
         JobExecutionContext context = localContext.get();
 
         TraceLevel logLevelFromContext = getLogLevelFromContext(context);
 
         if (level.ordinal() >= logLevelFromContext.ordinal()) {
             traceStore.add(Trace.message(Execution.getFromContext(context), level, format(format, args)));
         }
 
         if (level.ordinal() >= TraceLevel.WARN.ordinal()) {
             Execution.failed(context);
         }
     }
 
     private void log(TraceLevel level, String message, Throwable throwable) {
         JobExecutionContext context = localContext.get();
 
         TraceLevel logLevelFromContext = getLogLevelFromContext(context);
 
         if (level.ordinal() >= logLevelFromContext.ordinal()) {
             traceStore.add(Trace.exception(Execution.getFromContext(context), level, message, throwable));
         }
 
         if (level.ordinal() >= TraceLevel.WARN.ordinal()) {
             Execution.failed(context);
         }
     }
 
     private String format(String format, Object... args) {
         if (args.length == 0) {
             return format;
         }
 
         return MessageFormatter.arrayFormat(format, args).getMessage();
     }
 
     private TraceLevel getLogLevelFromContext(JobExecutionContext context) {
         String value = context.getMergedJobDataMap().getString(JobArgumentBean.LOG_LEVEL_ARGUMENT);
 
         if (StringUtils.isEmpty(value)) {
             return TraceLevel.WARN;
         }
 
         try {
             return TraceLevel.valueOf(value);
         } catch (Exception e) {
             LOGGER.warn("{} has an incorrect value ({}) for job, defaulting to WARN", JobArgumentBean.LOG_LEVEL_ARGUMENT, value);
 
             return TraceLevel.WARN;
         }
     }
 }

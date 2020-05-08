 /**
  * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.config;
 
 import com.crashnote.core.build.Builder;
 import com.crashnote.core.collect.Collector;
 import com.crashnote.core.log.LogLog;
 import com.crashnote.core.log.LogLogFactory;
 import com.crashnote.core.model.excp.CrashnoteException;
 import com.crashnote.core.model.types.LogLevel;
 import com.crashnote.core.report.Reporter;
 import com.crashnote.core.send.Sender;
 import com.crashnote.core.util.SystemUtil;
 import com.crashnote.external.config.Config;
 import com.crashnote.external.config.ConfigException;
 import com.crashnote.external.config.ConfigRenderOptions;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Main configuration object, home to all configurable settings of the notifier like
  * authentication and behaviour properties. It is initialized in the according appender/handler,
  * user customizations are applied and then each important class receives a copy.
  * <p/>
  * It assumes that each 'set' method receives a String and converts it to the actual data type by
  * manual parsing/converting, thus being independent of the way the concrete logger handles it.
  */
 public class CrashConfig {
 
     // CONST ======================================================================================
 
     /**
      * default prefix for the libraries properties (e.g. in command line)
      */
     public static final String LIB_NAME = "crashnote";
 
     /**
      * URL to service's website
      */
     public static final String LIB_URL = "https://app.crashnote.com";
 
 
     // VARS =======================================================================================
 
     private final LogLog logger;
 
     /**
      * time of JVM deployment / start up
      */
     private long startTime;
 
     /**
      * internal configuration
      */
     private Config conf;
 
     /**
      * factory to create an instance of the internal log
      */
     protected LogLogFactory logFactory;
 
 
     // SETUP ======================================================================================
 
     public CrashConfig(final Config c) {
         conf = c.withOnlyPath(LIB_NAME);
         logger = getLogger(this.getClass());
         startTime = new Date().getTime();
     }
 
 
     // INTERFACE ==================================================================================
 
     /**
      * Validate config instance against config schema
      */
     public void validate(final Config schema) {
         if (isEnabled()) {
             logger.info("Status: ON");
 
             // validate config ("fail fast")
             conf.checkValid(schema, "crashnote");
 
             // validate project id
             final String projectId = getProjectId();
             if (projectId == null || projectId.length() == 0) {
                 throw new IllegalStateException(
                     "The project id is missing; please login to '" + LIB_URL + "', and retrieve it.");
             }
 
             // validate API key
             final String key = getKey();
             if (key == null || key.length() == 0) {
                 throw new IllegalStateException(
                     "The API key is missing; please login to '" + LIB_URL + "', and retrieve it.");
 
             } else if (key.length() != 36)
                 throw new IllegalStateException(
                     "The API key appears to be invalid (it should be 32 characters long with 4 dashes); " +
                         "please login to '" + LIB_URL + "' and retrieve it.");
         } else {
             logger.info("Status: OFF");
         }
     }
 
 
     // FACTORY ====================================================================================
 
     /**
      * Create an instance of module 'Reporter'
      */
     public Reporter getReporter() {
         return new Reporter(this);
     }
 
     /**
      * Create an instance of module 'Sender'
      */
     public Sender getSender() {
         return new Sender(this);
     }
 
     /**
      * Create an instance of module 'Collector'
      */
     public Collector getCollector() {
         return new Collector(this);
     }
 
     /**
      * Create an instance of module 'Builder'
      */
     public Builder getBuilder() {
         return new Builder();
     }
 
     /**
      * Create an instance of the system utility
      */
     public SystemUtil getSystemUtil() {
         return new SystemUtil();
     }
 
     /**
      * Create an instance of the internal logger
      */
     public LogLog getLogger(final String name) {
         return getLogFactory().getLogger(name);
     }
 
     /**
      * Create an instance of the internal logger
      */
     public LogLog getLogger(final Class<?> clazz) {
         return getLogFactory().getLogger(clazz);
     }
 
 
     // SHARED =====================================================================================
 
     /**
      * Create an instance of a log factory
      */
     protected LogLogFactory getLogFactory() {
         if (logFactory == null) logFactory = new LogLogFactory(this);
         return logFactory;
     }
 
 
     // ==== READ CONFIG
 
     protected void print() {
         System.out.println(conf.root().render(ConfigRenderOptions.defaults().setComments(false)));
     }
 
     protected boolean getBool(final String name) {
         try {
             return conf.getBoolean(getConfName(name));
         } catch (ConfigException.Missing e) {
             throw new CrashnoteException("can not find config key '" + name + "'", e);
         } catch (ConfigException.WrongType e) {
             throw new CrashnoteException("type of config key '" + name + "' is not 'bool'", e);
         }
     }
 
     protected boolean getBool(final String name, final boolean def) {
         try {
             return conf.getBoolean(getConfName(name));
         } catch (Exception ignored) {
             return def;
         }
     }
 
     protected int getInt(final String name) {
         try {
             return conf.getInt(getConfName(name));
         } catch (ConfigException.Missing e) {
             throw new CrashnoteException("can not find config key '" + name + "'", e);
         } catch (ConfigException.WrongType e) {
             throw new CrashnoteException("type of config key '" + name + "' is not 'intl'", e);
         }
     }
 
     protected int getOptInt(final String name, final int defaultValue) {
         try {
             return conf.getInt(getConfName(name));
         } catch (Exception ignored) {
             return defaultValue;
         }
     }
 
     protected Long getMillis(final String name) {
         try {
            return conf.getMilliseconds(getConfName(name));
         } catch (ConfigException.Missing e) {
             throw new CrashnoteException("can not find config key '" + name + "'", e);
         } catch (ConfigException.BadValue e) {
             throw new CrashnoteException("type of config key '" + name + "' is not a duration", e);
         }
     }
 
     protected String getString(final String name) {
         try {
             return conf.getString(getConfName(name));
         } catch (ConfigException.Missing e) {
             throw new CrashnoteException("can not find config key '" + name + "'", e);
         } catch (ConfigException.WrongType e) {
             throw new CrashnoteException("type of config key '" + name + "' is not 'string'", e);
         }
     }
 
     protected String getString(final String name, final String def) {
         final String r = getOptString(name);
         return r == null ? def : r;
     }
 
     protected String getOptString(final String name) {
         try {
             return conf.getString(getConfName(name));
         } catch (Exception ignored) {
             return null;
         }
     }
 
     protected List<String> getStrings(final String name) {
         try {
             return conf.getStringList(getConfName(name));
         } catch (ConfigException.Missing e) {
             throw new CrashnoteException("can not find config key '" + name + "'", e);
         } catch (ConfigException.WrongType e) {
             throw new CrashnoteException("config key '" + name + "' is not a list of strings", e);
         }
     }
 
     protected List<String> getStrings(final String name, final List<String> def) {
         try {
             return conf.getStringList(getConfName(name));
         } catch (Exception ignored) {
             return def;
         }
     }
 
     protected List<String> getOptStrings(final String name) {
         final List<String> r = getStrings(name, null);
         return r == null ? new ArrayList<String>() : r;
     }
 
     protected String getConfName(final String name) {
         return LIB_NAME + "." + name;
     }
 
 
     // GET+ =======================================================================================
 
     public String getPostURL() {
         final boolean secure = getBool("network.secure", true);
         final String host = getString("network.host");
         final int port = getOptInt("network.port", 0);
         final String protocol = secure ? "https" : "http";
         final String url = protocol + "://" + getProjectId() + ":" + getKey() + "@" +
             host + (port > 0 ? ":" + port : "");
         logger.debug("resolved POST target URL: {}", url);
         return url;
     }
 
     public LogLevel getLogLevel() {
         final LogLevel maxLvl = LogLevel.INFO;
         final LogLevel reportLvl = getReportLogLevel();
         //final LogLevel historyLvl = getReportHistoryLevel();
         return LogLevel.getMaxLevel(maxLvl, reportLvl);
     }
 
     public String getClientInfo() {
         return getString("about.name", "crashnote") + ":" + getString("about.version", "?");
     }
 
 
     // GET ========================================================================================
 
     public long getStartTime() {
         return startTime;
     }
 
     public boolean isSync() {
         return getBool("sync");
     }
 
     public LogLevel getReportLogLevel() {
         return LogLevel.ERROR; // TODO: make configurable
     }
 
     public String getKey() {
         return getString("key");
     }
 
     public String getProjectId() {
         String v = getOptString("projectId");
         if (v == null)
             v = getOptString("projectID");
         if (v == null)
             v = getOptString("projectid");
         if (v == null)
             v = getOptString("project");
         if (v == null)
             v = getOptString("project_id");
         if (v == null)
             v = getOptString("id");
         return v;
     }
 
     public boolean isEnabled() {
         return getBool("enabled", false);
     }
 
     public List<String> getEnvironmentFilters() {
         return getStrings("filter.environment");
     }
 
     public String getAppEnv() {
         String v = getOptString("env");
         if (v == null)
             v = getOptString("app.env");
         return v;
     }
 
     public String getAppVersion() {
         String v = getOptString("version");
         if (v == null)
             v = getOptString("app.version");
         return v;
     }
 
     public String getAppBuild() {
         String v = getOptString("build");
         if (v == null)
             v = getOptString("app.build");
         return v;
     }
 
     public int getConnectionTimeout() {
         return getMillis("network.timeout").intValue();
     }
 
     public boolean isDebug() {
         return getBool("debug", false);
     }
 }

 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
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
 import com.crashnote.core.config.helper.Config;
 import com.crashnote.core.config.helper.ConfigRenderOptions;
 import com.crashnote.core.log.LogLog;
 import com.crashnote.core.log.LogLogFactory;
 import com.crashnote.core.model.types.LogLevel;
 import com.crashnote.core.report.Reporter;
 import com.crashnote.core.send.Sender;
 import com.crashnote.core.util.IDUtil;
 import com.crashnote.core.util.SystemUtil;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Main configuration object, home to all configurable settings of the notifier like
  * authentication and behaviour properties. It is initialized in the according appender/handler,
  * user customizations are applied and then each important class receives a copy.
  * <p/>
  * It assumes that each 'set' method receives a String and converts it to the actual data type by
  * manual parsing/converting, thus being independent of the way the concrete logger handles it.
  */
 public class CrashConfig<C extends CrashConfig<C>> {
 
     // CONST ======================================================================================
 
     /**
      * default prefix for the libraries properties (e.g. in command line)
      */
     public static final String LIB_NAME = "crashnote";
     public static final String LIB_URL = "https://www.crashnote.com";
     public static final String LIB_URL_BOARD = LIB_URL + "/dashboard";
 
 
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
      * unique identifier for environment
      */
     private long uid;
 
     /**
      * list of listeners that are notified on any change to the configuration
      */
     private volatile List<IConfigChangeListener> listeners = new ArrayList<IConfigChangeListener>();
 
     /**
      * factory to create an instance of the internal log
      */
     protected LogLogFactory<C> logFactory;
 
 
     // SETUP ======================================================================================
 
     public CrashConfig(final Config c) {
         conf = c.withOnlyPath("crashnote");
         uid = IDUtil.createUID();
         logger = getLogger(this.getClass());
         startTime = new Date().getTime();
     }
 
 
     // INTERFACE ==================================================================================
 
     public void addListener(final IConfigChangeListener<C> listener) {
         if (!listeners.contains(listener)) listeners.add(listener);
     }
 
     public void removeListener(final IConfigChangeListener<C> listener) {
         listeners.remove(listener);
     }
 
     public void removeAllListeners() {
         listeners.clear();
     }
 
     /**
      * Notifies all listeners about changes to the config
      */
     public void updateComponentsConfig() {
         for (final IConfigChangeListener l : listeners) {
             if (l != null) l.updateConfig(this);
         }
     }
 
     public void validate(final Config schema) {
         if (isEnabled()) {
             logger.info("Status: ON");
 
             // validate schema
             conf.checkValid(schema, "crashnote");
 
             // validate API key
             final String key = getKey();
             if (key == null || key.length() == 0) {
                 throw new IllegalArgumentException(
                         "The API Key is missing, please login to the web app under [" + LIB_URL_BOARD + "], " +
                                 "browse to your app and consult the 'Docs'.");
 
             } else if (key.length() != 36)
                 throw new IllegalArgumentException(
                         "The API Key appears to be invalid (it should be 32 characters long with 4 dashes), " +
                                 "please login to the web app under [" + LIB_URL_BOARD + "], " +
                                 "browse to your app and consult the 'Docs'.");
         } else {
             logger.info("Status: OFF");
         }
     }
 
 
     // FACTORY ====================================================================================
 
     /**
      * Create an instance of module 'Reporter'
      */
     public Reporter<C> getReporter() {
         return new Reporter(this);
     }
 
     /**
      * Create an instance of module 'Sender'
      */
     public Sender<C> getSender() {
         return new Sender(this);
     }
 
     /**
      * Create an instance of module 'Collector'
      */
     public Collector<C> getCollector() {
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
     public LogLog getLogger(final Class clazz) {
         return getLogFactory().getLogger(clazz);
     }
 
 
     // SHARED =====================================================================================
 
     /**
      * Create an instance of a log factory
      */
     protected LogLogFactory<C> getLogFactory() {
         if (logFactory == null) logFactory = new LogLogFactory(this);
         return logFactory;
     }
 
 
     // ==== READ CONFIG
 
     protected void print() {
         System.out.println(conf.root().render(ConfigRenderOptions.defaults().setComments(false)));
     }
 
     protected boolean getBool(final String name) {
         return conf.getBoolean("crashnote." + name);
     }
 
     protected int getInt(final String name) {
         return conf.getInt("crashnote." + name);
     }
 
     protected Long getMillis(final String name) {
         return conf.getMilliseconds("crashnote." + name);
     }
 
     protected String getString(final String name) {
         return conf.getString("crashnote." + name);
     }
 
    protected String getString(final String name, final String def) {
        final String r = getOptString(name);
        return r == null ? def : r;
    }

     protected String getOptString(final String name) {
         try {
             return conf.getString("crashnote." + name);
         } catch (Exception e) {
             return null;
         }
     }
 
     protected List<String> getStrings(final String name) {
         return conf.getStringList("crashnote." + name);
     }
 
 
     // INTERNALS ==================================================================================
 
     private String getBaseUrl() {
         final boolean ssl = getBool("network.ssl");
         final String host = getString("network.host");
         final int port = (ssl ? getInt("network.port-ssl") : getInt("network.port"));
         final String protocol = ssl ? "https://" : "http://";
         return protocol + host + ":" + port;
     }
 
 
     // GET+ =======================================================================================
 
     public String getPostUrl() {
         final String url = getBaseUrl() + "/errors?key=" + getKey();
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
 
     public List<IConfigChangeListener> getListeners() {
         return listeners;
     }
 
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
 
     public boolean isEnabled() {
         return getBool("enabled");
     }
 
     public List<String> getEnvironmentFilters() {
         return getStrings("filter.environment");
     }
 
     public String getAppProfile() {
         return getOptString("app.profile");
     }
 
     public String getVersion() {
         return getOptString("app.version");
     }
 
     public String getBuild() {
         return getOptString("app.build");
     }
 
     public int getConnectionTimeout() {
         return getMillis("network.timeout").intValue();
     }
 
     public boolean isDebug() {
         return getBool("debug");
     }
 }

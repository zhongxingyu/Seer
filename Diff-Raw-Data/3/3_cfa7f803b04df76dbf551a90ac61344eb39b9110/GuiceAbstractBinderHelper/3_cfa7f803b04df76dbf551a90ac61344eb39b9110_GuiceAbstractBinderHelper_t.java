 package com.g4share.jSynch.guice;
 
 import com.g4share.jSynch.config.ConfigReader;
 import com.g4share.jSynch.config.ConfigStore;
 import com.g4share.jSynch.config.XmlReader;
 import com.g4share.jSynch.guice.Binder.AbstractBindModule;
 import com.g4share.jSynch.guice.Factory.PointStoreHelperFactory;
 import com.g4share.jSynch.log.LogLevel;
 import com.g4share.jSynch.log.Logger;
 import com.g4share.jSynch.share.ConfigInfo;
 import com.g4share.jSynch.share.service.StatusInfo;
 import com.g4share.jSynch.share.service.SynchManager;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * User: gm
  * Date: 4/7/12
  */
 public abstract class GuiceAbstractBinderHelper {
     protected Injector injector;
 
     private Logger defaultLogger;
     private Logger fileLogger;
     private SynchManager synchManager;
     private ConfigStore configStore;
     private XmlReader xmlReader;
     private ConfigReader configReader;
     private StatusInfo statusInfo;
 
     private PointStoreHelperFactory pointStoreHelperFactory;
 
     private LogLevel level = LogLevel.TRACE;
     protected String currentPath;
 
     protected abstract AbstractBindModule getBindModuleInternal();
 
     protected abstract Logger getDefaultLoggerInternal();
     protected abstract Logger getLoggerInternal();
     protected abstract SynchManager getSynchManagerInternal();
     protected abstract ConfigStore getConfigStoreInternal();
     protected abstract XmlReader getXmlReaderInternal();
     protected abstract ConfigReader getConfigReaderInternal();
     protected abstract StatusInfo getStatusInfoInternal();
 
     protected abstract PointStoreHelperFactory getPointStoreHelperFactoryInternal();
 
     public GuiceAbstractBinderHelper(String currentPath) {
         this.currentPath = currentPath;
         injector = Guice.createInjector(getBindModuleInternal());
     }
 
     public final Logger getDefaultLogger(){
         if (defaultLogger == null) {
             defaultLogger = getDefaultLoggerInternal();
             defaultLogger.setLevel(level);
         }
         return defaultLogger;
     }
 
     public final Logger getLogger(){
         if (fileLogger == null) {
             fileLogger = getLoggerInternal();
             fileLogger.setLevel(level);
         }
 
         return fileLogger;
     }
 
     public final SynchManager getSynchManager(){
         if (synchManager == null) {
             synchManager = getSynchManagerInternal();
         }
         return synchManager;
     }
 
     public final ConfigStore getConfigStore(){
         if (configStore == null) {
             configStore = getConfigStoreInternal();
         }
         return configStore;
     }
 
     public final XmlReader getXmlReader(){
         if (xmlReader == null) {
             xmlReader = getXmlReaderInternal();
         }
         return xmlReader;
     }
 
     public final ConfigReader getConfigReader(){
         if (configReader == null) {
             configReader = getConfigReaderInternal();
         }
         return configReader;
     }
 
     public final PointStoreHelperFactory getPointStoreHelperFactory() {
         if (pointStoreHelperFactory == null) {
             pointStoreHelperFactory = getPointStoreHelperFactoryInternal();
         }
         return pointStoreHelperFactory;
     }
 
     public final StatusInfo getStatusInfo() {
         if (statusInfo == null) {
             statusInfo = getStatusInfoInternal();
         }
         return statusInfo;
     }
 
 
     public final ConfigInfo readConfigInfo(){
         ConfigReader cReader = getConfigReader();
         ConfigInfo cInfo = cReader.read(currentPath + "/config.xml");
 
 
        level = configStore.getLogLevel();
         if (defaultLogger == null) defaultLogger.setLevel(level);
         if (fileLogger == null) fileLogger.setLevel(level);
 
         return cInfo;
     }
 
 }

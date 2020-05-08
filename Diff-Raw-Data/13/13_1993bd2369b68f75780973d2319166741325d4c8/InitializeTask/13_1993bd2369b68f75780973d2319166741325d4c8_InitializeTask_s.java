 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.worker;
 
 import java.util.Properties;
 import java.util.concurrent.Callable;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.eclipse.jgit.api.errors.RefNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.meltmedia.cadmium.core.ContentService;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.history.HistoryManager;
 import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;
 
 public class InitializeTask implements Callable<GitService> {
   private final Logger logger = LoggerFactory.getLogger(getClass());
   
   private String contentRoot = null;
   private String warName = null;
   private SiteConfigProcessor metaProcessor = null;
   private ContentService servlet = null;
   private ConfigManager configManager;  
   private HistoryManager historyManager = null;
 
   @Inject
   public InitializeTask(ConfigManager configManager, ContentService servlet, SiteConfigProcessor metaProcessor, @Named("sharedContentRoot") String contentRoot, @Named("warName") String warName, HistoryManager historyManager) {
     
     this.contentRoot = contentRoot;
     this.warName = warName;
     this.metaProcessor = metaProcessor;
     this.servlet = servlet;
     this.configManager = configManager;
     this.historyManager = historyManager;
   }
 
   @Override
   public GitService call() throws Exception {
     
     Properties configProperties = configManager.getDefaultProperties();
     String branch = configProperties.getProperty("com.meltmedia.cadmium.branch");
     String repoUri = configProperties.getProperty("com.meltmedia.cadmium.git.uri");
     
     GitService cloned = null;
     if(repoUri != null && branch != null) {
       Throwable t = null;
 
       try {
         logger.info("Attempting to initialize content for `{}` into `{}`", warName, contentRoot);
         cloned = GitService.initializeContentDirectory(repoUri, branch, contentRoot, warName, historyManager, configManager);
 
         String contentDirectory = configProperties.getProperty("com.meltmedia.cadmium.lastUpdated");
         
         if(metaProcessor != null && contentDirectory != null) {
           logger.info("Processing META-INF dir in `{}`", warName);
           this.metaProcessor.processDir(contentDirectory);
         }
         if(servlet != null) {
           logger.info("Switching content root of {}", warName);
           this.servlet.switchContent(System.currentTimeMillis());
         }
         logger.info("Successfully initialized `{}`", warName);
       } catch(RefNotFoundException e) {
         logger.warn("Branch `"+branch+"` does not exist.", e);
         return null;
       } catch(Throwable t1) {
         logger.error("Failed to initialize git repo `"+warName+"`", t);
         t = t1;
       }
       if(t != null) {
         throw new Exception(t);
       }
     } else {
       logger.warn("Invalid setting for content repo {} and branch {}", repoUri, branch);
     }
     return cloned;
   }
 
   @Override
   public String toString() {
     return warName;
   }
 
 }

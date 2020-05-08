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
 package com.meltmedia.cadmium.vault;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import jodd.lagarto.dom.jerry.Jerry;
 import static jodd.lagarto.dom.jerry.Jerry.jerry;
 import jodd.lagarto.dom.jerry.JerryFunction;
 
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.messaging.Message;
 import com.meltmedia.cadmium.core.messaging.MessageSender;
 import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
 import com.meltmedia.cadmium.core.meta.ConfigProcessor;
 import com.meltmedia.cadmium.vault.service.VaultService;
 
 @Singleton
 public class VaultContentPreprocessor implements ConfigProcessor, VaultListener {
   private final Logger log = LoggerFactory.getLogger(getClass());
   
   @Inject
   protected VaultService loader;
   
   private boolean safetyMissing = false;
   private boolean error = false;
   
   //private Object timerSync = new Object();
   //private Timer timer = new Timer();
   
   @Inject
   protected MessageSender sender;
   
   @Inject
  @Named("config.properties")
  protected Properties configProperties;
 
   @Override
   public void processFromDirectory(String metaDir) throws Exception {
     /*synchronized(timerSync) {
       timer.cancel();
       timer = new Timer();
     }*/
     safetyMissing = false;
     error = false;
     String contentDir = FileSystemManager.getParent(metaDir);
     log.debug("Looking in contentDir [{}]", contentDir);
     
     if(contentDir != null) {
       log.info("Checking for vault safety!");
       List<String> files = new ArrayList<String>();
       files.add(contentDir);
       for(int i=0 ; i<files.size(); i++) {
         String file = files.get(i);
         if(FileSystemManager.isDirector(file) && !file.startsWith(metaDir)) {
           log.debug("Dir [" + file + "]");
           String newDirs[] = FileSystemManager.getDirectoriesInDirectory(file, null);
           if(newDirs.length > 0) {
             log.debug("Adding Dirs [{}]", newDirs);
             addFiles(files, i, file, newDirs);
           }
           String filesInDir[] = FileSystemManager.getFilesInDirectory(file, ".html");
           if(filesInDir.length > 0) {
             log.debug("Adding html files [{}]", filesInDir);
             addFiles(files, i, file, filesInDir);
           }
           filesInDir = FileSystemManager.getFilesInDirectory(file, ".htm");
           if(filesInDir.length > 0) {
             log.debug("Adding htm files [{}]", filesInDir);
             addFiles(files, i, file, filesInDir);
           }
         } else if(!FileSystemManager.isDirector(file) && (file.endsWith(".html") || file.endsWith(".htm"))){
           log.debug("File ["+file+"]");
           processHtmlFile(file);
         }
       }
     }
     
     if(safetyMissing) {
       throw new SafetyMissingException("Safety not loaded fully");
     }
     
     if(error) {
       throw new IOException("An error happened that prevented safety from loading! Trying again later.");
     }
   }
 
   private void addFiles(List<String> files, int i, String file, String[] newDirs) {
     for(String fileName : newDirs) {
       files.add(i+1, new File(file, fileName).getAbsolutePath());
     }
   }
   
   /**
    * <p>Processes all div blocks with "data-vault-guid" attributes that are present in the specified html file. All pages without these blocks will not be touched.</p>
    * <p>Example: <code>&lt;div data-vault-guid="VAULT_GUID"&gt;&lt;/div&gt</code></p>
    * @param htmlFile The file to process for vault resources.
    * @throws Exception If an error occurs while reading or parsing the file.
    */
   public void processHtmlFile(String htmlFile) throws Exception {
     String fileContent = FileSystemManager.getFileContents(htmlFile);
     Jerry htmlParser = jerry().parse(fileContent);
     Jerry divs = htmlParser.$("div[data-vault-guid]");
     if(divs.size() > 0) {
       divs.each(new JerryFunction() {
   
         @Override
         public boolean onNode(Jerry $this, int index) {
           String safety = $this.attr("data-vault-guid");
           try {
             $this.html(loader.getSafety(safety));
           } catch(SafetyMissingException e) {
             safetyMissing = true;
           } catch(IOException e) {
             log.error("Failed to process update!", e);
             error = true;
           }
           return true;
         }
         
       });
       
       String newContents = htmlParser.html();
       if(newContents != null && newContents.trim().length() > 0 && !fileContent.equals(newContents)) {
         File htmlFileObj = new File(htmlFile);
         FileSystemManager.writeStringToFile(htmlFileObj.getParent(), htmlFileObj.getName(), newContents);
       }
     }
   }
 
   @Override
   public void makeLive() {}
   
   @Override
   public void finalize() {
     /*synchronized(timerSync) {
       timer.cancel();
     }*/
   }
 
   @Override
   public void safetyUpdated(String[] guids) {
     log.info("Safety was updated");
     String sha = configProperties.getProperty("updating.to.sha", configProperties.getProperty("git.ref.sha"));
     String branch = configProperties.getProperty("updating.to.branch", configProperties.getProperty("branch"));
     
     sendUpdateMessage(branch, sha, "Safety updated");
   }
 
   @Override
   public void safetyUpdateFailed() {
     error = true;
     log.warn("An update failed.");
     /*timer.schedule(new TimerTask() {
       public void run() {
         log.warn("Sending message to start new update.");
         String sha = configProperties.getProperty("updating.to.sha");
         String branch = configProperties.getProperty("updating.to.branch");
     
         sendUpdateMessage(branch, sha, "Rescheduled for update that failed due to safety.");
       }
     }, 300000l);*/
   }
 
   @Override
   public void safetyMissing() {
     safetyMissing = true;
   }
   
   private void sendUpdateMessage(String branch, String sha, String comment) {
     if(sha != null) {
       Message msg = new Message();
       msg.setCommand(ProtocolMessage.UPDATE);
       msg.getProtocolParameters().put("sha", sha);
       if(branch != null) {
         msg.getProtocolParameters().put("branch", branch);
       }
       msg.getProtocolParameters().put("openId", "AUTO");
       msg.getProtocolParameters().put("comment", comment);
       msg.getProtocolParameters().put("nonRevertible", "true");
       
       try {
         sender.sendMessage(msg, null);
       } catch(Exception e) {
         log.warn("Failed to send update message.", e);
       }
     } 
   }
 
 }

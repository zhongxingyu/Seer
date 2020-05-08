 package com.bouncingdata.plfdemo.service;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.Attachment;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.ExecutionResult;
 import com.bouncingdata.plfdemo.util.Utils;
 
 public class ApplicationStoreService {
   
   private Logger logger = LoggerFactory.getLogger(ApplicationStoreService.class);
     
   private String storePath;
   private String logDir;
   
   public void setStorePath(String storePath) {
     this.storePath = storePath;
   }
 
   public void setLogDir(String logDir) {
     this.logDir = logDir;
   }
   
   public String getStorePath() {
     return storePath;
   }
   
   public void createApplicationFile(String guid, String language, String code) throws IOException {
     String storeAbsPath = storePath; //servletContext.getRealPath(Utils.FILE_SEPARATOR + storePath);
     logger.debug("Application store absolute path: {}", storeAbsPath);
     File dir = new File(storeAbsPath + Utils.FILE_SEPARATOR + guid);
     if (!dir.isDirectory()) {
       dir.mkdirs(); 
     }
     String filename = Utils.getApplicationFilename(language);
     FileUtils.write(new File(dir.getAbsolutePath() + Utils.FILE_SEPARATOR + filename), code);
     //logging
     logger.debug("Successfully write " + guid + "/" + filename);
   }
   
   public String getScriptCode(String guid, String language) throws IOException {
     String storeAbsPath = storePath; //servletContext.getRealPath(Utils.FILE_SEPARATOR + storePath);
     String filename = Utils.getApplicationFilename(language);
     String code = FileUtils.readFileToString(new File(storeAbsPath + Utils.FILE_SEPARATOR + guid 
         + Utils.FILE_SEPARATOR + filename));
     return code;
   }
     
   public void saveApplicationCode(String guid, String language, String code) throws IOException {
     String storeAbsPath = storePath;
     String filename = Utils.getApplicationFilename(language);
     FileUtils.write(new File(storeAbsPath + Utils.FILE_SEPARATOR + guid + Utils.FILE_SEPARATOR + filename), code);
   }
   
   public String getVisualization(String guid, String vGuid, String type) throws IOException {
     File f = new File(storePath + Utils.FILE_SEPARATOR + guid + Utils.FILE_SEPARATOR + "/v/" + vGuid + "." + type.toLowerCase());
     if (!f.isFile()) {
       // 
       if (logger.isDebugEnabled()) {
         logger.debug("Visualization file {} does not existed.", f.getAbsolutePath());
       }
       return null;
     }
     if ("png".equals(type)) {
       byte[] bytes = FileUtils.readFileToByteArray(f);
       return new String(Base64.encodeBase64(bytes));
     } else {
       String content = FileUtils.readFileToString(f);
       return content;
     }
   }
   
   public void copyVisualization(String guid, String vGuid, String type, File target) throws IOException {
     File f = new File(storePath + Utils.FILE_SEPARATOR + guid + Utils.FILE_SEPARATOR + "/v/" + vGuid + "." + type.toLowerCase());
     FileUtils.copyFile(f, target);
   }
   
   public String getTemporaryVisualization(String executionId, String name, String type) throws IOException {
     File f = new File(logDir + Utils.FILE_SEPARATOR + executionId + Utils.FILE_SEPARATOR + name + "." + type.toLowerCase());
     if (!f.isFile()) {
       // 
       if (logger.isDebugEnabled()) {
         logger.debug("Visualization file {} does not existed.", f.getAbsolutePath());
       }
       return null;
     }
     if ("png".equals(type)) {
       byte[] bytes = FileUtils.readFileToByteArray(f);
       return new String(Base64.encodeBase64(bytes));
     } else {
       String content = FileUtils.readFileToString(f);
       return content;
     }
   }
   
   public List<Attachment> getAttachmentData(String guid) {
     File dir = new File(storePath + Utils.FILE_SEPARATOR + guid);
     if (!dir.isDirectory()) {
       logger.debug("The application directory {} does not exist.", guid);
       return null;
     }
     
     File[] attachedFiles = dir.listFiles(new FileFilter() {
       
       @Override
       public boolean accept(File pathname) {
         return pathname.isFile() && pathname.getName().endsWith(".att");
       }
     });
     
     ObjectMapper mapper = new ObjectMapper();
     List<Attachment> results = null;
     if (attachedFiles != null) {
       results = new ArrayList<Attachment>();
       for (File f : attachedFiles) {
         String s;
         try {
           s = FileUtils.readFileToString(f);
         } catch (IOException e) {
           logger.debug("Can't read attachment file {}", f.getAbsolutePath());
           continue;
         }
         if (s == null || s.isEmpty()) continue;
         
         try {
           JsonNode root = mapper.readTree(s);
           String name = root.get("name").getTextValue();
           String description = null;
           if (root.has("description")) {
             description = root.get("description").getTextValue();
           }
           //String data = root.get("data").toString();
           JsonNode dataNode = root.get("data");
           Iterator<JsonNode> dataIter =  dataNode.getElements();
           List<Object[]> data = new ArrayList<Object[]>();
           boolean firstEle = true;
           if (dataIter.hasNext()) {
             JsonNode ele = dataIter.next();
             if (firstEle) { 
               Iterator<String> iter = ele.getFieldNames();
               List<String> firstRow = new ArrayList<String>();
               while(iter.hasNext()) {
                 firstRow.add(iter.next());
               }
               data.add(firstRow.toArray());
               firstEle = false;
             } else {
               Iterator<Entry<String, JsonNode>> fieldIter =  ele.getFields();
               List<String> row = new ArrayList<String>();
               while (fieldIter.hasNext()) {
                row.add(fieldIter.next().getValue().getTextValue());
               }
               data.add(row.toArray());
             }
             
           }
           Attachment attachment = new Attachment(-1, name, description, mapper.writeValueAsString(data));
           results.add(attachment);
         } catch (IOException e) {
           logger.debug("Cannot parse attachment file {}", f.getAbsoluteFile());
           continue;
         }
         
       }
     }
     
     return results;
   }
   
   public Attachment getAttachment(String guid, String name) {
     File dir = new File(storePath + Utils.FILE_SEPARATOR + guid);
     if (!dir.isDirectory()) {
       logger.debug("The application directory {} does not exist.", guid);
       return null;
     }
     
     File attFile = new File(dir.getAbsolutePath() + Utils.FILE_SEPARATOR + name + ".att");
     if (!attFile.isFile()) {
       logger.debug("The attachment {}/{} does not exist.", guid, name);
       return null;
     }
     
     ObjectMapper mapper = new ObjectMapper();
     String s;
     try {
       s = FileUtils.readFileToString(attFile);
     } catch (IOException e) {
       logger.debug("Can't read the attachment {}", attFile.getAbsolutePath());
       return null;
     }
     if (s == null || s.isEmpty()) return null;
     
     try {
       JsonNode root = mapper.readTree(s);
       //String name = root.get("name").getTextValue();
       String description = null;
       if (root.has("description")) {
         description = root.get("description").getTextValue();
       }
       String data = root.get("data").toString();
       Attachment attachment = new Attachment(-1, name, description, data);
       return attachment;
     } catch (Exception e) {
       logger.debug("Cannot parse attachment file {}", attFile.getAbsoluteFile());
       logger.debug("Exception detail", e);
       return null;
     }
   }
   
   public void resizeRPlot(String anlsGuid, String vizGuid, int width, int height) throws IOException {
     /*File f = new File(storePath + Utils.FILE_SEPARATOR + anlsGuid + Utils.FILE_SEPARATOR + "/v/" + vizGuid + ".png");    
     if (!f.isFile()) {
       // 
       if (logger.isDebugEnabled()) {
         logger.debug("Visualization file {} does not existed.", f.getAbsolutePath());
       }
       return;
     }*/
     
     File snapshot = new File(storePath + Utils.FILE_SEPARATOR + anlsGuid + Utils.FILE_SEPARATOR + "/v/" + vizGuid + ".snapshot");
     if (!snapshot.isFile()) {
       if (logger.isDebugEnabled()) {
         logger.debug("Snapshot file {} not found.", snapshot.getAbsolutePath());
       }
       return;
     }
     
     String script = "library(datastore)\n" 
                   + "datastore::resizePlot('" 
                   + snapshot.getAbsolutePath() 
                   + "'," + width + "," + height + ")";
     
     File temp = new File(snapshot.getParentFile().getAbsolutePath() + "/temp.R");
     FileUtils.writeStringToFile(temp, script);
     
     // 
     ProcessBuilder pb = new ProcessBuilder("Rscript", temp.getAbsolutePath());
     if (!pb.environment().containsKey("R_DEFAULT_DEVICE")) {
       pb.environment().put("R_DEFAULT_DEVICE", "png");
     }
     
     pb.redirectErrorStream(true);
     pb.directory(snapshot.getParentFile());
     
     try {
       final Process p = pb.start();
       Timer t = new Timer();
       t.schedule(new TimerTask() {      
         @Override
         public void run() {
           try {
             p.exitValue();
           } catch (IllegalThreadStateException e) {
             //logger.info("");
             e.printStackTrace();
             p.destroy();
           }
           this.cancel();
         }
       }, 1000*60);
       
       InputStream appOutputStream = new BufferedInputStream(p.getInputStream());
       int c;
       StringBuilder outputBuilder = new StringBuilder();
       byte[] b = new byte[4096];
       try {
         while ((c = appOutputStream.read(b)) != -1) {
           String chunk = new String(b, 0, c);
           outputBuilder.append(chunk);
         }
       } catch (IOException e) {
         // the stream maybe closed due to timeout or unknown error
         logger.debug("Exception occurs");
         e.printStackTrace();
       }
       String output = outputBuilder.toString();
       logger.info("RESIZE OUTPUT: " + output);
       try {
         p.exitValue();
       } catch (IllegalThreadStateException e) {
         p.destroy();
         t.cancel();
       }
       
       if (temp.isFile()) {
         temp.delete();
       }
     } catch (IOException e) {
       e.printStackTrace();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
   
   public void storeReferenceDocument(String guid, String name, MultipartFile file) throws IOException {
     File dir = new File(storePath + Utils.FILE_SEPARATOR + guid);
     if (!dir.isDirectory()) {
       dir.mkdirs(); 
     }
     File refFile = new File(storePath + Utils.FILE_SEPARATOR + guid + Utils.FILE_SEPARATOR + name);
     FileUtils.writeByteArrayToFile(refFile, file.getBytes());
   }
   
   public File getReferenceDocument(String guid, String name) throws IOException {
     File refFile = new File(storePath + Utils.FILE_SEPARATOR + guid + Utils.FILE_SEPARATOR + name);
     if (!refFile.isFile()) return null;
     return refFile;
   }
   
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package itracker.io.file;
 
 import itracker.io.common.IRequest;
 import itracker.io.manager.CacheManager;
 import itracker.util.Text;
 import itracker.util.Time;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.regex.Pattern;
import sun.rmi.runtime.Log;
 
 
 /**
  *
  * @author KerneL
  */
 public class FileManager extends CacheManager {
 
     class FileEntry implements Entry {        
         long timestamp = Time.current();
         File file;
 
         public FileEntry(File source) {
             this.file = source;
         }                
 
         @Override
         public String getData() {                        
             if (file == null) {
                 return null;
             }
             
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(file);
                byte[] data = new byte[(int)file.length()];
                fis.read(data);
                 return new String(data);                                            
             } catch (IOException ex) {
                 FileManager.this.deinit();
                 file = null;
             } finally {
                 try {
                     if (fis != null) {
                         fis.close();
                     }
                 } catch(IOException ex) {
                 }
             }            
             return null;
         }
 
         @Override
         public String getName() {            
             return "file[" + file.getName() + "]@" + timestamp;
         }
 
         @Override
         public synchronized void remove() {            
             if (file == null) {
                 return;
             }        
            String name = file.getName();           
            name = name.replace(".cache", ".removed");
             file.renameTo(new File(name));
             file = null;
         }
     }        
     
     File dataFolder = new File("");
     File dataFile = new File("");
     FileOutputStream dataOs;
     
     public File getDataFolder() {        
         return dataFolder;
     }    
     
     public void setDataFolder(File dataFolder) {
         if (isRunning()) {
             throw new IllegalStateException("cannot change data folder, once manager is started");
         }
         this.dataFolder = dataFolder;
     }    
     
     public synchronized boolean isDataFolderAccessible() {
         return this.dataFolder.isDirectory();                
     }
     
     private boolean initialized = false;
     private synchronized boolean init() {        
         if (initialized) {
             return true;
         }
         
         deinit();
 
         if (!dataFolder.isDirectory()) {
             if (!dataFolder.mkdirs()) {            
                 return false;
             }                
         }
                 
         Pattern p = Pattern.compile("track.*\\.cache");        
         File[] files = dataFolder.listFiles();
         for (File f : files) {            
             String name = f.getName();            
             if (!p.matcher(name).matches()) {
                 continue;
             }       
             push(new FileEntry(f));
         }    
         
         initialized = true;
         return true;
     }
     
     private synchronized void deinit() {
         initialized = false;
         flushDataStream();
         purgeCacheQueue();
     }
     
     @Override
     protected void startup() {
         init();
     }
 
     @Override
     protected void cleanup() {
         deinit();
     }
     
     private long fileSizeLimit = 1024*64;
 
     public long getFileSizeLimit() {
         return fileSizeLimit;
     }
 
     public void setFileSizeLimit(long fileSizeLimit) {
         this.fileSizeLimit = fileSizeLimit;
     }            
     
     private long bytesWritten;
     private synchronized boolean checkDataStream() {
         if (dataOs != null) {
             if (bytesWritten < fileSizeLimit) {                    
                 return true;
             }            
             flushDataStream();
         }
 
         try {
            dataFile = new File(dataFolder + "/track-" 
                 + Text.formatCurrentTimestamp() 
                + "-" + Text.randomHash() + ".cache");                           
             dataOs = new FileOutputStream(dataFile);                
         } catch(IOException ex) {
             deinit();
             return false;
         }
         
         return true;
     }
     
     private synchronized void flushDataStream() {
         if (dataOs == null) {
             return;
         }
 
         try {
             dataOs.close();
         } catch (IOException ex) {                    
         }                
         dataOs = null;
         
         if (init()) {
             push(new FileEntry(dataFile));
         }
 
         dataFile = null;
         bytesWritten = 0;
     }    
     
     @Override
     protected synchronized String process(IRequest request) {                
         if (!init()) {        
             return "init failed";
         }
         
         if (!checkDataStream()) {
             return "create file failed";
         }
         
         String s = request.getData();
         if (s == null) {
             return "empty data";
         }
         
         try {
             dataOs.write(s.getBytes());
             dataOs.flush();
         } catch (IOException ex) {
             deinit();
             return "write file failed";
         }        
         
         bytesWritten += s.length();
         return null;
     }
     
     @Override
     public synchronized IRequest getMoveRequest() {
         if (!init()) {
             return null;
         }
         
         if (super.isCacheQueueEmpty()) {
             flushDataStream();
         }
         
         return super.getMoveRequest();
     }        
     
     public boolean isReady() {
         return dataFolder.isDirectory();
     }
     
     @Override
     public String getName() {
         return "file";
     }
 
     @Override
     public boolean isCacheQueueEmpty() {        
         return super.isCacheQueueEmpty() && (dataOs == null);
     }   
     
 }

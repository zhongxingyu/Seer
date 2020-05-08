 package com.dpillay.projects.yafu.upload;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.multipart.MultipartFile;
 
 public class UploadResource {
     protected static Logger log = LoggerFactory.getLogger(UploadResource.class);
 
     protected volatile double lastProgress;
     protected volatile long bytesUploaded;
     protected volatile long bytesTotal;
     protected volatile boolean done;
 
     protected final transient OutputStream outputStream;
     protected final transient File outputFile;
     protected final transient InputStream inputStream;
     protected final String key;
     protected final String outputBaseDir;
 
     public UploadResource(String sessionId, String key, MultipartFile file, String outputBaseDir) throws IOException {
         super();
         this.key = key;
         this.outputBaseDir = outputBaseDir;
         this.bytesUploaded = 0;
         this.bytesTotal = file.getSize();
         this.done = false;
 
         synchronized (this) {
             this.inputStream = file.getInputStream();
             this.outputFile = new File(outputBaseDir + "/" + file.getOriginalFilename().replace(" ", "_") + "_"
                     + sessionId);
             this.outputStream = new FileOutputStream(this.outputFile);
         }
     }
 
     public synchronized void update() {
         this.lastProgress = Double.valueOf(this.bytesUploaded) / Double.valueOf(this.bytesTotal);
         if (log.isDebugEnabled())
             log.debug("For key: {}, uploaded : {} / {} -> progress {} ", new Object[] { this.key, this.bytesUploaded,
                     this.bytesTotal, this.lastProgress });
     }
 
     public synchronized void read() {
         if (!this.done) {
             byte[] bytes = new byte[4096];
             int bytesRead = 0;
             double progressLimit = this.lastProgress + 0.05;
             try {
                 while ((bytesRead = this.inputStream.read(bytes)) != -1) {
                     this.bytesUploaded += bytesRead;
                    this.outputStream.write(bytes, 0, bytesRead);
                     this.update();
                     if (this.lastProgress > progressLimit) {
                         break;
                     }
                 }
             } catch (Exception e) {
                 log.error(e.getLocalizedMessage(), e);
             }
 
             if (this.bytesTotal == this.bytesUploaded) {
                 this.done = true;
                 try {
                     this.inputStream.close();
                     this.outputStream.close();
                 } catch (IOException e) {
                     log.error(e.getLocalizedMessage(), e);
                 }
             }
         }
     }
 
     public synchronized void cancel() throws IOException {
         this.close();
         this.delete();
     }
 
     public synchronized void close() throws IOException {
         this.done = true;
         this.outputStream.close();
         this.inputStream.close();
     }
 
     public synchronized void delete() {
         this.outputFile.delete();
     }
 
     public double getLastProgress() {
         return lastProgress;
     }
 
     public void setLastProgress(double lastProgress) {
         this.lastProgress = lastProgress;
     }
 
     public long getBytesUploaded() {
         return bytesUploaded;
     }
 
     public void setBytesUploaded(long bytesUploaded) {
         this.bytesUploaded = bytesUploaded;
     }
 
     public long getBytesTotal() {
         return bytesTotal;
     }
 
     public void setBytesTotal(long bytesTotal) {
         this.bytesTotal = bytesTotal;
     }
 
     public boolean isDone() {
         return done;
     }
 
     public void setDone(boolean done) {
         this.done = done;
     }
 
     public OutputStream getOutputStream() {
         return outputStream;
     }
 
     public File getOutputFile() {
         return outputFile;
     }
 
     public InputStream getInputStream() {
         return inputStream;
     }
 }

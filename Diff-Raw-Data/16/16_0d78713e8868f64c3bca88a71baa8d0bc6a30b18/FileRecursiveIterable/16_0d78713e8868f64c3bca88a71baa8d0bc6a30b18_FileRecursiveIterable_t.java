 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class FileRecursiveIterable implements Iterable<File> {
 
     private final String name;
     private final File startfile;
     
     public FileRecursiveIterable(String name, File startfile){
         this.name = name;
         if (startfile == null){
             startfile =  new File(".");
             startfile = startfile.getAbsoluteFile();
             startfile = startfile.getParentFile();
         }
         this.startfile = startfile;
     }
 
     public Iterator<File> iterator() {
         return new FileRecursiveIterator(name, startfile);
     }
     
     private class FileRecursiveIterator implements Iterator<File> {
     
         private final BlockingQueue<File> filequeue = new LinkedBlockingQueue<File>();
         private final Thread thread;
         
         private final Logger log = LoggerFactory.getLogger(getClass());
     
         
         public FileRecursiveIterator(String name, File startfile){
             thread = new Thread(new FileRecursiveRunnable(name, startfile, filequeue));
             thread.start();
         }
         
         public boolean hasNext() {
             while (thread.isAlive()) {
                 
                 if (!filequeue.isEmpty()) {
                     return true;
                 }
                 
                 try {
                     Thread.sleep(10);
                 } catch (InterruptedException e) {
                     log.error("Interrupted waiting for queue to be non-empty");
                     thread.interrupt();
                     return false;
                 }
             }
             if (filequeue.isEmpty()) {
                 //can only guarantee no more files when thread is dead and list is empty
                 return false;
             } else {
                 return true;
             }
         }
     
         public File next() {
             while (thread.isAlive()) {
                 if (!filequeue.isEmpty()) {
                     try {
                         return filequeue.take();
                     } catch (InterruptedException e) {
                         log.error("Interupted taking item from queue", e);
                         return null;
                     }
                 }
                 
                 try {
                     Thread.sleep(10);
                 } catch (InterruptedException e) {
                     log.error("Interrupted waiting for queue to be non-empty");
                     thread.interrupt();
                     return null;
                 }
             }
             if (filequeue.isEmpty()) {
                 //can only guarantee no more files when thread is dead and list is empty
                 return null;
             } else {
                 try {
                     return filequeue.take();
                 } catch (InterruptedException e) {
                     log.error("Interupted taking item from queue", e);
                     return null;
                 }
             }
         }
     
         public void remove() {
             throw new RuntimeException("GlobIterator.remove() not implemented");
         }
         
         private class FileRecursiveRunnable implements Runnable {
             private final String name;
             private final File startfile;
             private final BlockingQueue<File> filequeue;
             
             private final Logger log = LoggerFactory.getLogger(getClass());
     
             public FileRecursiveRunnable(String name, File startfile, BlockingQueue<File> filequeue) {
                 this.name = name;
                 this.startfile = startfile;
                 this.filequeue = filequeue;
             }
     
             public void run() {
                 try {
                     getRecursiveFiles(startfile);
                 } catch (InterruptedException e) {
                     log.error("Interrupted while matching "+name, e);
                 } 
             }
             
             private void getRecursiveFiles(File startfile) throws InterruptedException {
                 File[] subfiles = startfile.listFiles();
                if (subfiles != null && subfiles.length > 0) {
                    Arrays.sort(subfiles);
                    for (File testfile : subfiles){
                        if (testfile.isDirectory()) {
                            getRecursiveFiles(testfile);
                        } else if (testfile.isFile()) {
                            if (name.equals(testfile.getName())){
                               filequeue.put(testfile.getAbsoluteFile()); 
                            }
                         }
                     }
                 }
             }
         }
     }
 }

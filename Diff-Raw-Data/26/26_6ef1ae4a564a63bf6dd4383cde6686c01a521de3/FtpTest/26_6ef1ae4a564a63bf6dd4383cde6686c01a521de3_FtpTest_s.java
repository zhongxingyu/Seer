 package com.yola.ftptest;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.io.File;
 import java.util.Collection;
 import java.net.URL;
 import java.net.MalformedURLException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.IOFileFilter;
 
 public class FtpTest {
   static ExecutorService dirExecutor = null;
   static ExecutorService fileExecutor = null;
   static URL uri = null;
   static List<Future<Boolean>> fileList = new ArrayList<Future<Boolean>>();
 
   public static void traverse(File dir) {
 
       if(dir.isDirectory())
       {
          System.out.println(dir) ;
          // Get a list of all the entries in the directory
          File[] entries = dir.listFiles() ;
          System.out.println("ENTRIES:") ;
 
          // Ensure that the list is not null
          if( entries != null )
          {
             // Loop over all the entries
             List<Future<File[]>> dirList = new ArrayList<Future<File[]>>();
             for( File entry : entries )
             {
                if(entry.isDirectory()) {
                  Callable<File[]> dir_worker = new FtpDirMaker(entry, uri);
                  Future<File[]> submit_dir = dirExecutor.submit(dir_worker);
                  System.out.println("MKDIR: " + entry.getPath());
                  dirList.add(submit_dir);
                  
                  List<File[]> dirResults = new ArrayList<File[]>();
                  // Now retrieve the result and start ftping the files...
                  for (Future<File[]> future : dirList) {
                    try {
                      File[] filesToUpload = future.get();
                      dirResults.add(filesToUpload);
                     for (File g : filesToUpload) {
                         if(!g.isDirectory()) {
                             Callable<Boolean> file_worker = new FtpFileUploader(g, uri);
                             Future<Boolean> submit_file = fileExecutor.submit(file_worker);
                             fileList.add(submit_file);
                             System.out.println("UPLOAD: " + g.getPath());
                         }
                     }
 
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    } catch (ExecutionException e) {
                      e.printStackTrace();
                    }
                  }
                  System.out.println(entry.getPath()) ;
                }
                // Recursive call to traverse
             }
 
             for( File entry : entries )
             {
                if(entry.isDirectory()) {
                  traverse(entry);
                  System.out.println(entry.getPath()) ;
                }
             }
          }
       }
    }
 
   public static void main(String[] args) {
 
     int concurrency = 5;
 
     if(args.length < 2) {
         System.err.println("Usage: ftptest <concurrency> <path> <ftp-uri>");
         System.exit(1);
     }
     try {
         concurrency = Integer.parseInt(args[0]);
     } catch (NumberFormatException e) {
         System.err.println("Argument concurrency must be an integer");
         System.exit(1);
     }
 
     try {
         uri = new URL(args[2]);
     } catch (MalformedURLException e) {
         System.err.println("Argument ftp-uri must be an ftp url: ftp://user:pass@foo.com/bar");
         System.exit(1);
     }
 
     String path =  args[1];
 
     Collection<File> listFile = FileUtils.listFilesAndDirs(new File(path), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
     File root = new File(path);
     fileExecutor = Executors.newFixedThreadPool(concurrency);
     dirExecutor = Executors.newFixedThreadPool(concurrency);
     // do root files
    for (File g : root.listFiles()) {
        
         if(!g.isDirectory()) {
             Callable<Boolean> file_worker = new FtpFileUploader(g, uri);
             Future<Boolean> submit_file = fileExecutor.submit(file_worker);
             fileList.add(submit_file);
             System.out.println("UPLOAD: " + g.getPath());
         }

    }
     traverse(root);
     List<Boolean> fileResults = new ArrayList<Boolean>();
     // Now retrieve the result
     for (Future<Boolean> future : fileList) {
       try {
         //System.out.println(future);
         fileResults.add(future.get());
       } catch (InterruptedException e) {
         e.printStackTrace();
       } catch (ExecutionException e) {
         e.printStackTrace();
       }
     }
     dirExecutor.shutdown();
     fileExecutor.shutdown();
     System.exit(0);
   }
 
 
 } 

 package com.frugs.filesync.local.system;
 
 import org.apache.commons.io.IOUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 public class SystemCommandExecutor {
 
     private final Logger logger;
 
     private static final Runtime currentRuntime = Runtime.getRuntime();
 
     public SystemCommandExecutor(Logger logger) {
         this.logger = logger;
     }
 
     public InputStream gitDiffHead() throws IOException {
        logger.info("performing 'git difftool -y -x \"diff -c\" HEAD'");
         return execute("git difftool -y -x \"diff -c\" HEAD");
     }
 
     public InputStream interDiff(String diffFile1, String diffFile2) throws IOException {
         logger.info("performing interdiff");
         return execute("interdiff " + diffFile1 + " " + diffFile2);
     }
 
     public void gitApply(String diffFile) throws IOException {
         logger.info("applying diff");
         execute("patch<" + diffFile);
     }
 
     private InputStream execute(String command) throws IOException {
         Process process = currentRuntime.exec(command);
         String error = IOUtils.toString(process.getErrorStream());
         if (!error.equals("")) {
             logger.severe(error);
             throw new RuntimeException(error);
         }
         return process.getInputStream();
     }
 }

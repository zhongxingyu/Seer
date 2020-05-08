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
         logger.info("performing git diff head");
         return execute("git diff HEAD");
     }
 
     public InputStream interDiff(String diffFile1, String diffFile2) throws IOException {
         logger.info("performing interdiff");
         return execute("interdiff " + diffFile1 + " " + diffFile2);
     }
 
     public void gitApply(String diffFile) throws IOException {
         logger.info("applying diff");
        logger.info(IOUtils.toString(execute("git apply" + diffFile)));
     }
 
     private InputStream execute(String command) throws IOException {
         return currentRuntime.exec(command).getInputStream();
     }
 }

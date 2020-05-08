 package org.krall.security.command;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.LogOutputStream;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.exec.ShutdownHookProcessDestroyer;
 import org.krall.security.commandline.AppOptions;
 import org.krall.security.compare.WatcherCaptureAndCompareImages;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class INotifyWait implements Runnable {
 
     private static final Logger logger = LoggerFactory.getLogger(INotifyWait.class);
 
     private DefaultExecutor executor = new DefaultExecutor();
 
     private CommandLine command;
 
     private WatcherCaptureAndCompareImages watcherCaptureAndCompareImages;
 
     public INotifyWait(WatcherCaptureAndCompareImages watcherCaptureAndCompareImages) {
         this.watcherCaptureAndCompareImages = watcherCaptureAndCompareImages;
         executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
         executor.setStreamHandler(new PumpStreamHandler(new CustomLogOutputStream()));
         AppOptions options = AppOptions.getInstance();
        command = CommandLine.parse(String.format("inotifywait -m -e close_write --format %%w%%f -q %s ",
                                                   options.getInputDirectory().getAbsolutePath()));
     }
 
     @Override
     public void run() {
         try {
             executor.execute(command);
             executor.wait();
         } catch (Exception e) {
             logger.error("Error while running command", e);
             throw new RuntimeException("Error while running command", e);
         }
     }
 
     private class CustomLogOutputStream extends LogOutputStream {
 
         @Override
         protected void processLine(String line) {
             logger.info("Got Line: {}", line);
             watcherCaptureAndCompareImages.processFile(line);
         }
 
         @Override
         protected void processLine(String line, int level) {
             processLine(line);
         }
     }
 }

 package com.cazcade.billabong.snapshot.impl;
 
 import com.cazcade.billabong.common.DateHelper;
 import com.cazcade.billabong.snapshot.Capturer;
 import com.cazcade.billabong.snapshot.Snapshot;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.UUID;
 
 /**
  * Wrapper class for the CutyCapt executable.
  */
 public class WKHTMLCapturer implements Capturer {
     private final String executable;
     private String outputType = "png";
     private String outputPath = System.getProperty("cazcade.home", ".") + "/billabong/wkhtml/tmp";
     private int maxWidth = 1024;
    private int maxHeight = 4096;
 
     private final DateHelper dateHelper;
     private final String userAgent = "Billabong 1.1 (WKHTMLImage) Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) " +
                                      "AppleWebKit/534.52.7 (KHTML, " +
                                      "like Gecko) Version/5.1.2 Safari/534.52.7";
     private int maxWait = 60 * 1000;
 
     @SuppressWarnings({"SameParameterValue", "SameParameterValue"})
     public WKHTMLCapturer(String executable, DateHelper dateHelper) {
         this.executable = executable;
         this.dateHelper = dateHelper;
     }
 
     @Override
     public Snapshot getSnapshot(URI uri, final int delayInSeconds, String waitForWindowStatus) {
         long maxProcessWait = (delayInSeconds * 1000) + maxWait;
 
         initOutputPath();
         UUID uuid = UUID.randomUUID();
         File outputFile = new File(outputPath, uuid.toString() + "." + outputType);
         outputFile.getParentFile().mkdirs();
         final String delayString = String.valueOf(delayInSeconds * 1000);
         ProcessBuilder processBuilder;
         if (waitForWindowStatus != null) {
             processBuilder = new ProcessBuilder(
                     executable,
                     "--width", String.valueOf(maxWidth),
 //                    "--crop-w", String.valueOf(minWidth),
                     "--height", String.valueOf(maxHeight),
 //                    "--crop-h", String.valueOf(minHeight),
                     "--use-xserver",
                     "--custom-header", "User-Agent", userAgent,
                     //we don't stop slow scripts because (in theory) we're waiting on a window.status value
                     "--no-stop-slow-scripts",
                     "--window-status", waitForWindowStatus,
 //                    "--javascript-delay", delayString,
                     uri.toString(),
                     outputFile.toString()
             );
         }
         else {
             processBuilder = new ProcessBuilder(
                     executable,
                     "--width", String.valueOf(maxWidth),
 //                    "--crop-w", String.valueOf(minWidth),
                     "--height", String.valueOf(maxHeight),
 //                    "--crop-h", String.valueOf(minHeight),
                     "--use-xserver",
                     "--custom-header", "User-Agent", userAgent,
                     "--javascript-delay", delayString,
                     uri.toString(),
                     outputFile.toString()
             );
         }
         processBuilder.redirectErrorStream(true);
         try {
             Process captureProcess = processBuilder.start();
             InputStreamReader inputStream = new InputStreamReader(
                     new BufferedInputStream(captureProcess.getInputStream())
             );
             try {
                 boolean done = false;
                 long maxEndTime = System.currentTimeMillis() + maxProcessWait;
                 StringBuffer output = new StringBuffer();
                 char[] buffer = new char[4096];
                 while (!done && System.currentTimeMillis() < maxEndTime) {
                     int length = inputStream.read(buffer);
                     if (length >= 0) {
                         output.append(buffer, 0, length);
                     }
                     else {
                         try {
                             int result = captureProcess.exitValue();
                             done = true;
                             if (result != 0) {
                                 throw new RuntimeException("Failed to capture URI image successfully:\n" +
                                                            uri + "\n" + output.toString()
                                 );
                             }
                         } catch (IllegalThreadStateException e) {
                             //expected - work not yet done...
                             //The only case I've yet found where an empty catch block may be justified.
                         }
                     }
 
                 }
                 System.err.println(output);
                 if (!done) {
                     captureProcess.destroy();
                 }
             } finally {
                 inputStream.close();
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return new FileSnapshot(uri, outputFile, dateHelper.current());
     }
 
 
     @SuppressWarnings({"SameParameterValue", "SameParameterValue"})
     public void setOutputType(String outputType) {
         this.outputType = outputType;
     }
 
     @SuppressWarnings({"SameParameterValue"})
     public void setOutputPath(String outputPath) {
         this.outputPath = outputPath;
 
     }
 
     private void initOutputPath() {
         File outputPathFile = new File(outputPath);
         if (!outputPathFile.exists()) {
             outputPathFile.mkdirs();
         }
     }
 
     public void setMaxWidth(int maxWidth) {
         this.maxWidth = maxWidth;
     }
 
     public void setMaxHeight(int maxHeight) {
         this.maxHeight = maxHeight;
     }
 
     public void setMaxWait(int maxWait) {
         this.maxWait = maxWait;
     }
 
 
 }

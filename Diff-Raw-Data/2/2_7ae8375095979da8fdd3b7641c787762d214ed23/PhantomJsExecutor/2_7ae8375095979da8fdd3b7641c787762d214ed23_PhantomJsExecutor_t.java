 package net.awired.jstest.executor;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.SequenceInputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.cli.Commandline;
 
 public class PhantomJsExecutor implements Executor {
 
     private static final String RUN_QUNIT_JS = "/run-qunit.js";
     private static final String RUNNER_RESOURCE = "runnerResource"+RUN_QUNIT_JS;
     private StringBuilder CMD = new StringBuilder("phantomjs "); 
     private Process process = null;
     private File targetSrcDir = null;
     private Log log = null;
 
     public PhantomJsExecutor() {
     }
 
     public void execute(String runnerUrl) throws Exception {
         copyTestRunner(runnerUrl);
         
        CMD.append("\""+targetSrcDir.getCanonicalPath() + RUN_QUNIT_JS+"\" ");
         CMD.append(runnerUrl+"?emulator=true");
         log.info("command " + CMD);
         
         Commandline cl = new Commandline(CMD.toString());
 		//cl.setWorkingDirectory(workingDirectory);
 		Process process = cl.execute();
 		InputStream inputStream = process.getInputStream();
 		InputStream errorStream = process.getErrorStream();
 		SequenceInputStream sequenceInputStream = new SequenceInputStream(inputStream, errorStream);
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sequenceInputStream));
 
         try {
             String line;
             // Read every line of input
             while ((line = bufferedReader.readLine()) != null) {
                 // Print all output to the console
             	if (line.contains("finished")) break;
                 System.out.println(line);
             }    
 
         } catch (IOException e) {
             throw new Exception(e);
 
         } finally {
             // Close the input reader
             try {
                 bufferedReader.close();
             } catch(IOException e) {
                 throw new Exception(e);
             }
         }
     }
 
     public void close() {
         if (process != null) {
             process.destroy();
         }
     }
 
     public void setTargetSrcDir(File targetSourceDirectory) {
         this.targetSrcDir = targetSourceDirectory;
     }
 
     private void copyTestRunner(String url) throws Exception {
         try {
             URL runnerUrl = new URL(url+RUNNER_RESOURCE);
             File testRunnerFile = new File(targetSrcDir, RUN_QUNIT_JS);
             if (!targetSrcDir.exists()) { 
                 targetSrcDir.mkdirs();
             }
             if (!testRunnerFile.exists()) {
                 testRunnerFile.createNewFile();
             }
             URLConnection connection = runnerUrl.openConnection();
 //            InputStream is = new FileInputStream(connection.); //PhantomJsExecutor.class.getResourceAsStream(RUNNER_RESOURCE);
 
             BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
             try {
                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(testRunnerFile));
                 try {
                     int c;
                     while ((c = bis.read()) != -1) {
                         bos.write(c);
                     }
                 } finally {
                     bos.close();
                 }
             } finally {
                 bis.close();
             }
         } catch (IOException e) {
            log.error("Unable to copy " + RUN_QUNIT_JS + " : " + e.toString());
             e.printStackTrace();
             /*throw new Exception(
                     "Unable to copy " + RUN_QUNIT_JS + " : " + e.toString());*/
         }
     }
 
 	public void setLog(Log log) {
 		this.log = log;
 	}
 
 }

 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.cli;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.DefaultPlatform;
 import org.geotools.util.DefaultProgressListener;
 import org.opengis.util.ProgressListener;
 
 import py4j.GatewayServer;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Throwables;
 
 /**
  * Provides an entry point using the py4j library, to expose GeoGit functionality to python
  * applications
  */
 public class GeogitPy4JEntryPoint {
 
     ConsoleReader consoleReader;
 
     /**
      * A class to parse and store console output of GeoGit commands
      */
     public class ToStringOutputStream extends OutputStream {
 
         StringBuffer sb = new StringBuffer();
 
         @Override
         public void write(int b) throws IOException {
             char c = (char) b;
             String s = String.valueOf(c);
             sb.append(s);
         }
 
         public void clear() {
             sb = new StringBuffer();
         }
 
         public String asString() {
             return sb.toString();
         }
     }
 
     private PrintStream stream;
 
     private ToStringOutputStream os;
 
     private GeoGitPy4JProgressListener listener;
 
     public GeogitPy4JEntryPoint() {
         listener = new SilentProgressListener();
         os = new ToStringOutputStream();
         stream = new PrintStream(os);
         try {
             consoleReader = new ConsoleReader(System.in, stream);
             consoleReader.getTerminal().setEchoEnabled(true);
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
     }
 
     /**
      * Runs a command on a given repository
      * 
      * @param folder the repository folder
      * @param args the args to run, including the command itself and additional parameters
      * @return
      */
     public int runCommand(String folder, String[] args) {
         System.gc();
         GeogitCLI cli = new GeogitCLI(consoleReader) {
             public synchronized ProgressListener getProgressListener() {
                if (this.progressListener == null) {
 
                    this.progressListener = new DefaultProgressListener() {
                         public void setDescription(String s) {
                            listener.setProgressText(s);
                         }
 
                         @Override
                         public synchronized void progress(float percent) {
                            listener.setProgress(percent);
 
                         }
                     };
                 }
                return this.progressListener;
             }
         };
         cli.tryConfigureLogging();
         DefaultPlatform platform = new DefaultPlatform();
         platform.setWorkingDir(new File(folder));
         cli.setPlatform(platform);
         String command = Joiner.on(" ").join(args);
         os.clear();
         System.out.print("Running command: " + command);
         int ret = cli.processCommand(args);
         cli.close();
         if (ret == 0) {
             System.out.println(" [OK]");
         } else {
             System.out.println(" [Error]");
         }
 
         return ret;
     }
 
     /**
      * Returns the output of the last command
      * 
      * @return the output of the last commmand
      * @throws IOException
      */
     public String lastOutput() throws IOException {
         stream.flush();
         os.flush();
         return os.asString();
     }
 
     public boolean isGeoGitServer() {
         return true;
     }
 
     /**
      * Shutdowns the server
      */
     public void shutdown() {
         System.out.println("Shutting down GeoGit server.");
         System.exit(0);
     }
 
     /**
      * Sets the progress listener that will receive progress updates
      * 
      * @param listener
      */
     public void setProgressListener(GeoGitPy4JProgressListener listener) {
         this.listener = listener;
     }
 
     /**
      * Removes the progress listener, if set
      */
     public void removeProgressListener() {
         this.listener = new SilentProgressListener();
     }
 
     public static void main(String[] args) {
         GatewayServer gatewayServer = new GatewayServer(new GeogitPy4JEntryPoint());
         gatewayServer.start();
         System.out.println("GeoGit server correctly started and waiting for conections at port "
                 + Integer.toString(gatewayServer.getListeningPort()));
     }
 }
 
 class SilentProgressListener implements GeoGitPy4JProgressListener {
 
     @Override
     public void setProgress(float i) {
 
     }
 
     @Override
     public void setProgressText(String s) {
 
     }
 
 }

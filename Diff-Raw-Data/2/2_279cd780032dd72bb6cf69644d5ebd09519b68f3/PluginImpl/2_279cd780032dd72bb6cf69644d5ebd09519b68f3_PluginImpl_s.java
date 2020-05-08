 /*
  * The MIT License
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package hudson.plugins.hadoop;
 
 import hudson.FilePath;
 import static hudson.FilePath.TarCompression.GZIP;
 import hudson.Launcher.LocalLauncher;
 import hudson.Plugin;
 import hudson.Proc;
 import hudson.model.Computer;
 import hudson.model.Hudson;
 import hudson.model.TaskListener;
 import hudson.remoting.Channel;
 import hudson.remoting.Which;
 import hudson.slaves.Channels;
 import hudson.util.ArgumentListBuilder;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.FileUtils;
 
 /**
  * Hadoop plugin.
  *
  * @author Kohsuke Kawaguchi
  */
 public class PluginImpl extends Plugin {
     /*package*/ Channel channel;
 
     @Override
     public void start() throws Exception {
         Hudson.getInstance().getActions().add(new HadoopPage());
     }
 
     /**
      * Determines the HDFS URL.
      */
     public String getHdfsUrl() throws MalformedURLException {
         // TODO: port should be configurable
         String rootUrl = Hudson.getInstance().getRootUrl();
         if(rootUrl==null)
             return null;
         URL url = new URL(rootUrl);
         return "hdfs://"+url.getHost()+":9000/";
     }
 
     /**
      * Determines the job tracker address.
      */
     public String getJobTrackerAddress() throws MalformedURLException {
         // TODO: port should be configurable
         String rootUrl = Hudson.getInstance().getRootUrl();
         if(rootUrl==null)
             return null;
         URL url = new URL(rootUrl);
         return url.getHost()+":"+JOB_TRACKER_PORT_NUMBER;
     }
 
     /**
      * Launches Hadoop in a separate JVM.
      *
      * @param rootDir
      *      The slave/master root.
      */
     static /*package*/ Channel createHadoopVM(File rootDir, TaskListener listener) throws IOException, InterruptedException {
         // install Hadoop if it's not there
         rootDir = new File(rootDir,"hadoop");
         File distDir = new File(rootDir,"dist");
         File logDir = new File(rootDir,"logs");
         if(shouldInstallBinary(distDir)) {
             listener.getLogger().println("Installing Hadoop binaries");
             if(distDir.exists())
                 new FilePath(distDir).deleteContents();
             new FilePath(distDir).untarFrom(PluginImpl.class.getResourceAsStream("hadoop.tar.gz"),GZIP);
             FileUtils.writeStringToFile(new File(distDir,"MD5"),getHadoopTarGzMd5());
         }
         logDir.mkdirs();
 
         // launch Hadoop in a new JVM and have them connect back to us
         ServerSocket serverSocket = new ServerSocket();
         serverSocket.bind(null);
         serverSocket.setSoTimeout(10*1000);
 
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(new File(System.getProperty("java.home"),"bin/java"));
         args.add("-Dhadoop.log.dir="+logDir); // without this job tracker dies with NPE
         args.add("-jar");
         args.add(Which.jarFile(Channel.class));
 
         // build up a classpath
         StringBuilder classpath = new StringBuilder();
         for( String mask : new String[]{"hadoop-*-core.jar","lib/**/*.jar"}) {
             for(FilePath jar : new FilePath(distDir).list(mask)) {
                 if(classpath.length()>0)    classpath.append(File.pathSeparatorChar);
                 classpath.append(jar.getRemote());
             }
         }
         args.add("-cp").add(classpath);
 
         args.add("-connectTo","localhost:"+serverSocket.getLocalPort());
 
         listener.getLogger().println("Starting Hadoop");
         Proc p = new LocalLauncher(listener).launch(args.toCommandArray(), new String[0], listener.getLogger(), null);
 
         Socket s = serverSocket.accept();
         serverSocket.close();
 
         return Channels.forProcess("Channel to Hadoop", Computer.threadPoolForRemoting,
                 new BufferedInputStream(s.getInputStream()), new BufferedOutputStream(s.getOutputStream()), p);
     }
 
     private static boolean shouldInstallBinary(File distDir) throws IOException {
         if(!distDir.exists())   return true;
 
         File checksum = new File(distDir, "MD5");
         if(checksum.exists()) {
             String md5 = FileUtils.readFileToString(checksum);
             if(md5.equals(getHadoopTarGzMd5()))
                 return false;   // correct
         }
 
         return true;
     }
 
     private static String getHadoopTarGzMd5() throws IOException {
        InputStream in = PluginImpl.class.getResourceAsStream("hadoop.tar.gz.md5");
         try {
             return IOUtils.toString(in);
         } finally {
             in.close();
         }
     }
 
     @Override
     public void stop() throws Exception {
         if(channel!=null)
             channel.close();
     }
 
     public static PluginImpl get() {
         return Hudson.getInstance().getPlugin(PluginImpl.class);
     }
 
     /**
      * Job tracker port number.
      */
     public static final int JOB_TRACKER_PORT_NUMBER = 50040;
 }

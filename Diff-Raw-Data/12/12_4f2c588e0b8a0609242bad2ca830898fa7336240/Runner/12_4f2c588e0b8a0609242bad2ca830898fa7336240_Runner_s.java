 package org.kevoree.watchdog;
 
 import org.kevoree.log.Log;
 import org.kevoree.resolver.MavenResolver;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 /**
  * Created by duke on 16/05/13.
  */
 public class Runner {
 
     public static final String pingPortProperty = "ping.port";
     public static final String pingTimeoutProperty = "ping.timeout";
     public static final String outFileLogProperty = "log.out";
     public static final String errFileLogProperty = "log.err";
 
 
     private static final String baseRuntimeMvnUrl = "mvn:org.kevoree.platform:org.kevoree.platform.standalone:";
     private static final String mavenCentral = "http://repo.maven.apache.org/maven2";
     private static final String ossRepository = "http://oss.sonatype.org/content/groups/public";
     private static MavenResolver artifactResolver = new MavenResolver();
 
     //private static MavenVersionResolver snapshotResolver = new MavenVersionResolver();
     private static ChildManager childManager = new ChildManager();
     public static WatchDogCheck checker = new WatchDogCheck();
 
     public static void configureSystemProps() {
         //Configuration of property
         Object pingportValue = System.getProperty(pingPortProperty);
         if (pingportValue != null) {
             try {
                 WatchDogCheck.internalPort = Integer.parseInt(pingportValue.toString());
             } catch (Exception e) {
                 System.err.println("Bad ping port specified : " + pingportValue + ", take default value : " + WatchDogCheck.internalPort);
             }
         }
         Object pingTimeoutValue = System.getProperty(pingTimeoutProperty);
         if (pingTimeoutValue != null) {
             try {
                 WatchDogCheck.checkTime = Integer.parseInt(pingTimeoutValue.toString());
             } catch (Exception e) {
                 System.err.println("Bad ping timeout specified : " + pingportValue + ", take default value : " + WatchDogCheck.checkTime);
             }
         }
     }
 
     public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
 
         configureSystemProps();
 
         Runtime.getRuntime().addShutdownHook(new Thread(childManager));
         System.out.println("Kevoree Boot Service");
 
         /*
         if (args.length != 2 && args.length != 1) {
             System.out.println("Usage : ([kevoree.version]) ([bootmodel])");
             System.exit(-1);
         }*/
 
         String kevoreeVersion = "latest";
         //String kevoreeVersion = "RELEASE";
 
         if (args.length > 0) {
             kevoreeVersion = args[0];
         }
 
 
         String runtimeMvnUrl = baseRuntimeMvnUrl + kevoreeVersion;
        ArrayList<String> repos = new ArrayList<String>();
         repos.add(mavenCentral);
         repos.add(ossRepository);
         Log.info("Resolving runtime Jar file ...");
         File runtime = artifactResolver.resolve(runtimeMvnUrl, repos);
         Log.info("Resolution done");
 
         if (runtime != null) {
             checker.setRuntimeFile(runtime);
             childManager.setSubProcess(checker);
 
             Object fileoutNameProp = System.getProperty(outFileLogProperty);
             if (fileoutNameProp != null) {
                 File syso = new File(fileoutNameProp.toString());
                 if (syso.exists()) {
                     syso.renameTo(new File(fileoutNameProp.toString() + "-" + System.currentTimeMillis()));
                     syso = new File(fileoutNameProp.toString());
                 }
                 checker.setSysoutFile(syso);
             }
             Object fileerrNameProp = System.getProperty(errFileLogProperty);
             if (fileerrNameProp != null) {
                 File syserr = new File(fileerrNameProp.toString());
                 if (syserr.exists()) {
                     syserr.renameTo(new File(fileerrNameProp.toString() + "-" + System.currentTimeMillis()));
                     syserr = new File(fileerrNameProp.toString());
                 }
                 checker.setSyserrFile(syserr);
             }
 
             //look for bootmodel
             if (args.length == 2) {
                 String modelPath = args[1];
                 File modelFile = new File(modelPath);
                 if (modelFile.exists()) {
                     checker.setModelFile(modelFile);
                 }
             }
             System.out.println("Kevoree " + kevoreeVersion + " Ready to run ");
             checker.startServer();
             checker.startKevoreeProcess();
         } else {
             System.err.println("Could not start runtime, because the file returned by the MavenResolver for " + runtimeMvnUrl + " is null");
         }
     }
 
 }

 package com.zenika.dorm.maven.importer.launcher;
 
 import com.zenika.dorm.maven.importer.MavenRepositoryImporter;
 import com.zenika.dorm.maven.importer.utils.DormCredentials;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.ExampleMode;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class Launch {
 
     private static final Logger LOG = LoggerFactory.getLogger(Launch.class);
 
     @Option(name = "-h", usage = "Host of the Dorm server", required = true, aliases = {"--host"})
     private String host;
     @Option(name = "-p", usage = "Port of the Dorm server", aliases = "--port")
     private int port = 80;
     @Option(name = "-P", usage = "Root path of the maven resource", aliases = "--Path")
     private String path = "";
     @Option(name = "-l", usage = "Repository path", aliases = "--localRepository")
     private String localRepository = "~/.m2/repository";
     @Option(name = "-s", usage = "Get full stack trace", aliases = "--stackTrace")
     private boolean stackTraceActive;
     @Option(name = "-u", usage = "DORM user name", required = true, aliases = "--user")
     private String user;
    @Option(name = "-p", usage = "DORM password", required = true, aliases = "--password")
     private String password;
 
     public static void main(String[] args){
         Launch launch = new Launch();
         launch.run(args);
     }
 
     private void run(String[] args){
         CmdLineParser parser = new CmdLineParser(this);
         try {
             parser.parseArgument(args);
             if (!host.contains("http://")){
                 host = "http://" + host;
             }
             MavenRepositoryImporter importer = new MavenRepositoryImporter(localRepository,
                     host, port, path, new DormCredentials(user, password));
             LOG.info("Start import...");
             importer.start();
             LOG.info("Done import in : " + importer.getTime() + " ms");
             LOG.info("Import success : " + importer.getImportSuccess());
             LOG.info("Import fail : " + importer.getImportFail());
             if (importer.getImportFileFail().size() > 0){
                 LOG.info("List of file import fail : ");
                 for(File file : importer.getImportFileFail()){
                     LOG.info("File : " + file.getName() + " at : " + file.getAbsolutePath());
                 }
             }
         } catch (CmdLineException e) {
             LOG.error(e.getMessage());
             LOG.error("\nTest [options...] arguments");
             LOG.error(parser.printExample(ExampleMode.ALL));
         }
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public void setLocalRepository(String localRepository) {
         this.localRepository = localRepository;
     }
 }

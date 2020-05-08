 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Properties;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AccessionerDriver extends AbstractInfileDriver<AccessionerTask> {
     
     @Option(name = "--output", aliases={"-o"}, usage = "output filename")
     private String outputFilename;
 
     @Option(name = "--hostname", aliases={"-n"}, usage = "server hostname")
     private String hostname;
 
     @Option(name = "--port", usage = "server port") 
     private Integer port;
    // -p -o -r -t are all used elsewhere!
 
     @Option(name = "--database", aliases={"-d"}, usage = "server database")
     private String database;
 
     @Option(name = "--username", aliases={"-u"}, usage = "server username")
     private String username;
 
     @Option(name = "--password", aliases={"-p"}, usage = "server password")
     private String password;
 
     private Accessioner accessioner = null;
     
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public static void main(String[] args) {
         new AccessionerDriver().doMain(args);
     }
 
     @Override
     protected void preProcess() {
         //load defaults
         Properties properties = new Properties();
         try {
             InputStream is = AccessionerDriver.class.getResourceAsStream("/oracle.properties");
             properties.load(is);
         } catch (IOException e) {
             log.error("Unable to read resource oracle.properties", e);
         }
         if (hostname == null) {
             hostname = properties.getProperty("hostname");
         }
         if (port == null){
             port = new Integer(properties.getProperty("port"));
         }
         if (database == null) {
             database = properties.getProperty("database");
         }
         if (username == null) {
             username = properties.getProperty("username");
         }
         if (password == null) {
             password = properties.getProperty("password");
         }
 
         //create the accessioner
         try {
             accessioner = new AccessionerENA(hostname, port, database, username, password);
         } catch (ClassNotFoundException e) {
             log.error("Unable to find interface class ", e);
             System.exit(2);
             return;
         } catch (SQLException e) {
             log.error("Error in SQL", e);
             System.exit(2);
             return;
         }
     }
     
     @Override
     protected AccessionerTask getNewTask(File inputFile) {
         File outputFile = new File(inputFile.getParentFile(), outputFilename);
         Corrector c = new Corrector();
         return new AccessionerTask(inputFile, outputFile, accessioner, c);
     }
     
     @Override
     protected void postProcess() {
         if (accessioner != null){
             accessioner.close();
         }
     }
 }

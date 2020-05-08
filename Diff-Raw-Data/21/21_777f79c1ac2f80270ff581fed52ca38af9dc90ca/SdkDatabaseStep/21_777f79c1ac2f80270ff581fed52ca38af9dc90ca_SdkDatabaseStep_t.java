 package org.cagrid.tests.data.styles.cacore42.steps;
 
 import gov.nih.nci.cagrid.common.StreamGobbler;
 import gov.nih.nci.cagrid.common.Utils;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.tests.data.styles.cacore42.DatabaseProperties;
 import org.cagrid.tests.data.styles.cacore42.ExampleProjectInfo;
 
 public class SdkDatabaseStep extends AbstractDatabaseStep {
     
     private static Log LOG = LogFactory.getLog(SdkDatabaseStep.class);
     
     private DatabaseOperation operation = null;
 
     public SdkDatabaseStep(DatabaseOperation operation) {
         super();
         this.operation = operation;
     }
     
     
     public void runStep() {
         try {
             switch (operation) {
                 case CREATE:
                     LOG.debug("CREATING caCORE SDK DATABASE");
                     getDatabase().createDatabaseIfNeeded();
                     break;
                 case INSTALL:
                     LOG.debug("INSTALLING caCORE SDK DATABASE");
                    createDatabase();
                     break;
                 case DESTROY:
                     LOG.debug("DESTROYING caCORE SDK DATABASE");
                     getDatabase().destroyDatabase();
                     break;
             }
         } catch (Exception ex) {
             String message = "Error performing database operation " + operation.name() + ": " + ex.getMessage();
             LOG.error(message, ex);
             fail(message);
         }
     }
     
     
    private void createDatabase() {
         try {
             File[] dbScripts = ExampleProjectInfo.getMysqlDatabaseInstallFiles();
             for (File script : dbScripts) {
                 LOG.debug("Executing db script " + script.getAbsolutePath());
                 List<String> command = new ArrayList<String>();
                 command.add(getMysqlExecutable());
                 command.add("-u");
                 command.add(DatabaseProperties.getUsername());
                 if (DatabaseProperties.getPassword() != null && DatabaseProperties.getPassword().length() != 0) {
                     command.add("-p");
                     command.add(DatabaseProperties.getPassword());
                 }
                 command.add("-D");
                 command.add(DatabaseProperties.getSchemaName());
                 String[] cmdArray = new String[command.size()];
                 cmdArray = command.toArray(cmdArray);
                 // read the script
                 StringBuffer sql = Utils.fileToStringBuffer(script);
                 Process proc = Runtime.getRuntime().exec(cmdArray);
                 // push the script into the mysql process to load the DB
                 BufferedOutputStream out = new BufferedOutputStream(proc.getOutputStream());
                 out.write(sql.toString().getBytes());
                 new StreamGobbler(proc.getErrorStream(), StreamGobbler.TYPE_ERR, System.err).start();
                 new StreamGobbler(proc.getInputStream(), StreamGobbler.TYPE_OUT, System.out).start();
                 out.close();
                 int code = -1;
                 try {
                     code = proc.waitFor();
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                    fail("Error running database script " + script.getName() + ": " + ex.getMessage());
                 }
                assertEquals("Unexpected exit code from mysql script " + script.getName(), 0, code);
             }
         } catch (IOException ex) {
             String message = "Error reading database creation script: " + ex.getMessage();
             LOG.error(message, ex);
             fail(message);
         }
     }
     
     
     private String getMysqlExecutable() {
         StringTokenizer pathTokenizer = new StringTokenizer(System.getenv("PATH"), File.pathSeparator);
         String mysqlName = "mysql";
         boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
         if (isWindows) {
             mysqlName += ".exe";
         }
         while (pathTokenizer.hasMoreTokens()) {
             String path = pathTokenizer.nextToken();
             File maybeMysql = new File(path, mysqlName);
             if (maybeMysql.exists() && maybeMysql.isFile()) {
                 LOG.debug("Probably found mysql on the path: " + maybeMysql.getAbsolutePath());
                 return maybeMysql.getAbsolutePath();
             }
         }
         return null;
     }
     
     
     public static enum DatabaseOperation {
         CREATE, INSTALL, DESTROY
     }
 }

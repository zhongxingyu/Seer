 package main;
 
 import database.SDBDatabaseConnection;
 import database.SPARQLController;
 import extraction.RISExtractor;
 import java.io.File;
 import java.io.PrintWriter;
 import org.apache.commons.cli.*;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author dgcliff
  */
 public class ris2n3
 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws ParseException
     {
        String username, password, jdbcURL, RISfolder, outputFile;
         
         Options options = new Options();
         
         options.addOption("username", true, "User name for JDBC connection");
         options.addOption("password", true, "Password for JDBC connection");
         options.addOption("jdbcURL", true, "JDBC URL for database connection");
        options.addOption("RISfolder", true, "Folder where RIS files are contained");
        options.addOption("outputFile", true, "Output file name");
         
         HelpFormatter helpFormatter = new HelpFormatter();
         
         CommandLineParser parser = new PosixParser();
         CommandLine cmd = parser.parse(options, args);
         
         PrintWriter pw = new PrintWriter(System.out, true); 
         
         username = cmd.getOptionValue("username");
         password = cmd.getOptionValue("password");
         jdbcURL = cmd.getOptionValue("jdbcURL");
        RISfolder = cmd.getOptionValue("RISfolder");
        outputFile = cmd.getOptionValue("outputFile");
         
        if(username == null || password == null || jdbcURL == null || RISfolder == null || outputFile == null)
         {
             System.out.println("This program requries a user name, a password and a JDBC url to work.\n");
            helpFormatter.printUsage(pw, 120, "RIS2N3", options);
             System.exit(0);
         }
         
         SDBDatabaseConnection sdbC = new SDBDatabaseConnection(username, password, jdbcURL);
         SPARQLController sparqlC = new SPARQLController(sdbC.getStore());
         
        File dir = new File(RISfolder);
         
         RISExtractor risEx = new RISExtractor(dir, sparqlC);
                 
         risEx.extractAuthorNames();        
         
        risEx.extractToN3(outputFile);
     }
 }

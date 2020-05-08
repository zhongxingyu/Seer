 package com.fiveclouds.jasper;
 
 import net.sf.jasperreports.engine.*;
 import net.sf.jasperreports.engine.export.JRPdfExporter;
 import org.apache.commons.cli.*;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 /**
  * A simple helper which can be used to export a PDF of a Jasper Report
  * <p/>
  * Note this is a very simple little utility
  */
 public class JasperRunner {
 
     public static void main(String[] args) {
 
         // Set-up the options for the utility
         Options options = new Options();
         options.addOption("report", true, "jasper report to run (i.e. /path/to/report.jrxml)");
         options.addOption("driver", true, "the jdbc driver class (i.e. com.mysql.jdbc.Driver)");
         options.addOption("jdbcurl", true, "database jdbc url (i.e. jdbc:mysql://localhost:3306/database)");
         options.addOption("username", true, "database username");
         options.addOption("password", true, "database password");
         options.addOption("output", true, "the output filename (i.e. path/to/report.pdf");
         options.addOption("help", false, "print this message");
 
         Option property = OptionBuilder.withArgName("property=value")
                 .hasArgs(2)
                 .withValueSeparator()
                 .withDescription("use value as report property")
                 .create("D");
 
         options.addOption(property);
 
         // Parse the options and build the report
         CommandLineParser parser = new PosixParser();
         try {
             CommandLine cmd = parser.parse(options, args);
 
             if (cmd.hasOption("help")) {
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("jasper-runner", options);
             } else {
 
                System.out.println("Building report "+cmd.getOptionValue("report"));
                 try {
                     Class.forName(cmd.getOptionValue("driver"));
                    Connection connection = DriverManager.getConnection(cmd.getOptionValue("jdbcurl"), cmd.getOptionValue("username"), cmd.getOptionValue("password"));
                    System.out.println("Connected to "+cmd.getOptionValue("jdbcUrl"));
                     JasperReport jasperReport = JasperCompileManager.compileReport(cmd.getOptionValue("report"));
                     JRPdfExporter pdfExporter = new JRPdfExporter();
                     JasperPrint print = JasperFillManager.fillReport(jasperReport, cmd.getOptionProperties("D"), connection);
 
                     pdfExporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                     pdfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, cmd.getOptionValue("output"));
                    System.out.println("Exporting report to "+cmd.getOptionValue("output"));
                     pdfExporter.exportReport();
                 } catch (JRException e) {
                     System.err.print("Unable to parse report file (" + cmd.getOptionValue("r") + ")");
                     e.printStackTrace();
                    System.exit(1);
                 } catch (ClassNotFoundException e) {
                     System.err.print("Unable to find the database driver,  is it on the classpath?");
                     e.printStackTrace();
                    System.exit(1);
                 } catch (SQLException e) {
                     System.err.print("An SQL exception has occurred (" + e.getMessage() + ")");
                     e.printStackTrace();
                    System.exit(1);
                 }
             }
         } catch (ParseException e) {
             System.err.print("Unable to parse command line options (" + e.getMessage() + ")");
            System.exit(1);
         }
     }
 }

 /* Copyright 2010 The Tor Project
  * See LICENSE for licensing information */
 package org.torproject.ernie.db;
 
 import java.io.*;
 import java.text.*;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.logging.*;
 /**
  * Initialize logging configuration.
  *
  * Log levels used by ERNIE:
  *
  * - SEVERE: An event made it impossible to continue program execution.
  * - WARNING: A potential problem occurred that requires the operator to
  *   look after the otherwise unattended setup
  * - INFO: Messages on INFO level are meant to help the operator in making
  *   sure that operation works as expected.
  * - FINE: Debug messages that are used to identify problems and which are
  *   turned on by default.
  * - FINER: More detailed debug messages to investigate problems in more
  *   detail. Not turned on by default. Increase log file limit when using
  *   FINER.
  * - FINEST: Most detailed debug messages. Not used.
  */
 public class LoggingConfiguration {
   public LoggingConfiguration() {
 
     /* Remove default console handler. */
     for (Handler h : Logger.getLogger("").getHandlers()) {
       Logger.getLogger("").removeHandler(h);
     }
 
     /* Disable logging of internal Sun classes. */
     Logger.getLogger("sun").setLevel(Level.OFF);
 
     /* Set minimum log level we care about from INFO to FINER. */
     Logger.getLogger("").setLevel(Level.FINER);
 
     /* Create log handler that writes messages on WARNING or higher to the
      * console. */
     final SimpleDateFormat dateTimeFormat =
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     Formatter cf = new Formatter() {
       public String format(LogRecord record) {
         return dateTimeFormat.format(new Date(record.getMillis())) + " "
             + record.getMessage() + "\n";
       }
     };
     Handler ch = new ConsoleHandler();
     ch.setFormatter(cf);
     ch.setLevel(Level.WARNING);
     Logger.getLogger("").addHandler(ch);
 
     /* Initialize own logger for this class. */
     Logger logger = Logger.getLogger(
         LoggingConfiguration.class.getName());
 
     /* Create log handler that writes all messages on FINE or higher to a
      * local file. */
     Formatter ff = new Formatter() {
       public String format(LogRecord record) {
         return dateTimeFormat.format(new Date(record.getMillis())) + " "
             + record.getLevel() + " " + record.getSourceClassName() + " "
             + record.getSourceMethodName() + " " + record.getMessage()
             + "\n";
       }
     };
     try {
       FileHandler fh = new FileHandler("log", 5000000, 5, true);
       fh.setFormatter(ff);
       fh.setLevel(Level.FINE);
       Logger.getLogger("").addHandler(fh);
     } catch (SecurityException e) {
       logger.log(Level.WARNING, "No permission to create log file. "
           + "Logging to file is disabled.", e);
     } catch (IOException e) {
       logger.log(Level.WARNING, "Could not write to log file. Logging to "
           + "file is disabled.", e);
     }
 
     /* Create log handler that writes messages on INFO or higher to a
      * local HTML file for display on the website. */
     Handler wh = new Handler() {
       private StringBuilder infos = new StringBuilder();
       private StringBuilder warnings = new StringBuilder();
       public void close() {
         if (this.infos == null || this.warnings == null) {
           return;
         }
         try {
           BufferedWriter bw = new BufferedWriter(
               new FileWriter("website/log.html"));
           bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
                 + "Transitional//EN\">\n"
               + "<html>\n"
               + "  <head>\n"
               + "    <title>Tor Metrics Portal: Last execution "
                 + "logs</title>\n"
               + "    <meta http-equiv=Content-Type content=\"text/html; "
                 + "charset=iso-8859-1\">\n"
               + "    <link href=\"http://www.torproject.org/stylesheet-"
               + "ltr.css\" type=text/css rel=stylesheet>\n"
               + "    <link href=\"http://www.torproject.org/favicon.ico\""
                 + " type=image/x-icon rel=\"shortcut icon\">\n"
               + "  </head>\n"
               + "  <body>\n"
               + "    <div class=\"center\">\n"
               + "      <table class=\"banner\" border=\"0\" "
                 + "cellpadding=\"0\" cellspacing=\"0\" summary=\"\">\n"
               + "        <tr>\n"
               + "          <td class=\"banner-left\"><a href=\"https://"
                 + "www.torproject.org/\"><img src=\"http://www.torproject"
                 + ".org/images/top-left.png\" alt=\"Click to go to home "
                 + "page\" width=\"193\" height=\"79\"></a></td>\n"
               + "          <td class=\"banner-middle\">\n"
               + "            <a href=\"/\">Home</a>\n"
               + "            <a href=\"graphs.html\">Graphs</a>\n"
               + "            <a href=\"research.html\">Research</a>\n"
               + "            <a href=\"status.html\">Status</a>\n"
               + "            <br/>\n"
               + "            <font size=\"2\">\n"
               + "              <a href=\"exonerator.html\">ExoneraTor</a>\n"
              + "              <a href=\"relay-search.html\">Relay Search</a>\n"
               + "              <a href=\"consensus-health.html\">Consensus Health</a>\n"
               + "              <a class=\"current\">Last Log</a>\n"
               + "            </font>\n"
               + "          </td>\n"
               + "          <td class=\"banner-right\"></td>\n"
               + "        </tr>\n"
               + "      </table>\n"
               + "      <div class=\"main-column\">\n"
               + "        <h2>Tor Metrics Portal: Last execution "
                 + "logs</h2>\n"
               + "        <br/>\n"
               + "        <p>This page shows the warnings and info logs "
                 + "of the last program execution. All timestamps are in "
                 + "UTC.</p>\n"
               + "        <br/>\n"
               + "        <h3>Warnings</h3>\n"
               + "        <br/>\n"
               + "        <p><i>Warning messages indicate that a "
                 + "potential problem has occurred that requires the "
                 + "operator to look after the otherwise unattended "
                 + "setup.</i></p>\n"
               + "        <br/>\n"
               + "        <table border=\"0\" cellpadding=\"4\" "
               + "cellspacing=\"0\" summary=\"\">\n"
               + "          <colgroup>\n"
               + "            <col width=\"160\">\n"
               + "            <col width=\"640\">\n"
               + "          </colgroup>\n");
           if (this.warnings.length() < 1) {
             bw.write("          <tr><td>(No messages.)</td><td/></tr>\n");
           } else {
             bw.write(warnings.toString());
           }
           bw.write("        </table>\n"
               + "        <br/>\n"
               + "        <br/>\n"
               + "        <h3>Infos</h3>\n"
               + "        <br/>\n"
               + "        <p><i>Info messages are meant to help the "
                 + "operator in making sure that operation works as "
                 + "expected.</i></p>\n"
               + "        <br/>\n"
               + "        <table border=\"0\" cellpadding=\"4\" "
                 + "cellspacing=\"0\" summary=\"\">\n"
               + "          <colgroup>\n"
               + "            <col width=\"160\">\n"
               + "            <col width=\"640\">\n"
               + "          </colgroup>\n");
           if (this.infos.length() < 1) {
             bw.write("          <tr><td>(No messages.)</td><td/></tr>\n");
           } else {
             bw.write(this.infos.toString());
           }
           bw.write("        </table>\n"
               + "      </div>\n"
               + "    </div>\n"
               + "    <div class=\"bottom\" id=\"bottom\">\n"
 
 
               + "      <p>This material is supported in part by the "
                 + "National Science Foundation under Grant No. "
                 + "CNS-0959138. Any opinions, finding, and conclusions "
                 + "or recommendations expressed in this material are "
                 + "those of the author(s) and do not necessarily reflect "
                 + "the views of the National Science Foundation.</p>\n"
               + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
                 + "href=\"https://www.torproject.org/trademark-faq.html"
                 + ".en\">"
               + "registered trademarks</a> of The Tor Project, "
                 + "Inc.</p>\n"
               + "      <p>Data on this site is freely available under a "
                 + "<a href=\"http://creativecommons.org/publicdomain/"
                 + "zero/1.0/\">CC0 no copyright declaration</a>: To the "
                 + "extent possible under law, the Tor Project has waived "
                 + "all copyright and related or neighboring rights in "
                 + "the data. Graphs are licensed under a <a "
                 + "href=\"http://creativecommons.org/licenses/by/3.0/"
                 + "us/\">Creative Commons Attribution 3.0 United States "
                 + "License</a>.</p>\n"
               + "    </div>\n"
               + "  </body>\n"
               + "</html>");
           bw.close();
           this.infos = null;
           this.warnings = null;
         } catch (IOException e) {
         }
       }
       public void flush() {
       }
       public void publish(LogRecord record) {
         if (this.infos == null || this.warnings == null) {
           return;
         }
         String logMessage = "          <tr>\n"
             + "            <td>"
             + dateTimeFormat.format(new Date(record.getMillis()))
             + "</td>\n"
             + "            <td>"
             + record.getMessage().replaceAll("\n", "<br/>")
             + "</td>\n"
             + "          </tr>\n";
         if (record.getLevel().equals(Level.FINE) ||
             record.getLevel().equals(Level.FINER) ||
             record.getLevel().equals(Level.FINEST)) {
           /* Ignore messages on FINE, FINER, and FINEST. */
         } else if (record.getLevel().equals(Level.INFO)) {
           this.infos.append(logMessage);
         } else {
           this.warnings.append(logMessage);
         }
       }
     };
     wh.setLevel(Level.INFO);
     Logger.getLogger("").addHandler(wh);
   }
 }

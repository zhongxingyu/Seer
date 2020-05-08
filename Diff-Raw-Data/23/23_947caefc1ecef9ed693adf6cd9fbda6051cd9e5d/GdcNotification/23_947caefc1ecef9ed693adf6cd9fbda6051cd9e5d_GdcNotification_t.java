 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.processor;
 
 import java.io.*;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.*;
 
 import com.gooddata.config.Metric;
 import com.gooddata.config.NotificationConfig;
 import com.gooddata.config.NotificationMessage;
 import com.gooddata.config.Report;
 import com.gooddata.exception.*;
 import com.gooddata.filter.DuplicateMessageFilter;
 import com.gooddata.filter.MessageFilter;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.transport.NotificationTransport;
 import com.gooddata.transport.SfdcChatterTransport;
 import com.sforce.ws.ConnectionException;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.jexl2.Expression;
 import org.apache.commons.jexl2.JexlContext;
 import org.apache.commons.jexl2.JexlEngine;
 import org.apache.commons.jexl2.MapContext;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.util.FileUtil;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 /**
  * The GoodData Data Integration CLI processor.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcNotification {
 
     private static Logger l = Logger.getLogger(GdcNotification.class);
 
     //Options data
     public static String[] CLI_PARAM_GDC_USERNAME = {"username","u"};
     public static String[] CLI_PARAM_GDC_PASSWORD = {"password","p"};
 
     public static String[] CLI_PARAM_TRANSPORT_USERNAME = {"transportusername","d"};
     public static String[] CLI_PARAM_TRANSPORT_PASSWORD = {"transportpassword","c"};
 
     public static String[] CLI_PARAM_GDC_HOST = {"host","h"};
     public static String[] CLI_PARAM_VERSION = {"version","V"};
     public static String CLI_PARAM_CONFIG = "config";
 
     private static String DEFAULT_PROPERTIES = "gdi.properties";
 
     // mandatory options
     public static Option[] mandatoryOptions = { };
 
     // optional options
     public static Option[] optionalOptions = {
         new Option(CLI_PARAM_GDC_USERNAME[1], CLI_PARAM_GDC_USERNAME[0], true, "GoodData username"),
         new Option(CLI_PARAM_GDC_PASSWORD[1], CLI_PARAM_GDC_PASSWORD[0], true, "GoodData password"),
         new Option(CLI_PARAM_TRANSPORT_USERNAME[1], CLI_PARAM_TRANSPORT_USERNAME[0], true, "Salesforce username"),
         new Option(CLI_PARAM_TRANSPORT_PASSWORD[1], CLI_PARAM_TRANSPORT_PASSWORD[0], true, "Salesforce password"),
         new Option(CLI_PARAM_GDC_HOST[1], CLI_PARAM_GDC_HOST[0], true, "GoodData host"),
         new Option(CLI_PARAM_VERSION[1], CLI_PARAM_VERSION[0], false, "Prints the tool version."),
     };
 
     private CliParams cliParams = null;
     private boolean finishedSucessfuly = false;
 
     private final static String BUILD_NUMBER = "";
 
     private GdcNotification(CommandLine ln, Properties defaults) {
         try {
             cliParams = parse(ln, defaults);
             cliParams.setHttpConfig(new NamePasswordConfiguration("https",
                     cliParams.get(CLI_PARAM_GDC_HOST[0]),
                     cliParams.get(CLI_PARAM_GDC_USERNAME[0]), cliParams.get(CLI_PARAM_GDC_PASSWORD[0])));
             String config = cliParams.get(CLI_PARAM_CONFIG);
             if(config != null && config.length()>0) {
                 execute(config);
             }
             else {
                 l.error("No config file given.");
                 commandsHelp();
                 System.exit(1);
             }
             finishedSucessfuly = true;
         }
         catch (ConnectionException e) {
             l.error("Can't connect to SFDC: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Can't connect to SFDC:",e);
         }
         catch (InvalidArgumentException e) {
             l.error("Invalid command line argument: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Invalid command line argument:",e);
             l.info(commandsHelp());
         }
         catch (SfdcException e) {
             l.error("Error communicating with SalesForce: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error communicating with SalesForce.",e);
         }
         catch (GdcLoginException e) {
             l.error("Error logging to GoodData. Please check your GoodData username and password: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error logging to GoodData. Please check your GoodData username and password.",e);
         }
         catch (IOException e) {
             l.error("Encountered an IO problem. Please check that all files that you use in your command line arguments and commands exist. More info: '"+e.getMessage()+"'");
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Encountered an IO problem. Please check that all files that you use in your command line arguments and commands exist. More info: '"+e.getMessage()+"'",e);
         }
         catch (InternalErrorException e) {
             Throwable c = e.getCause();
             if( c != null && c instanceof SQLException) {
                 l.error("Error extracting data. Can't process the incoming data. Please check the CSV file " +
                         "separator and consistency (same number of columns in each row). Also, please make sure " +
                         "that the number of columns in your XML config file matches the number of rows in your " +
                         "data source. Make sure that your file is readable by other users (particularly the mysql user). " +
                         "More info: '"+c.getMessage()+"'");
                 l.debug("Error extracting data. Can't process the incoming data. Please check the CSV file " +
                         "separator and consistency (same number of columns in each row). Also, please make sure " +
                         "that the number of columns in your XML config file matches the number of rows in your " +
                         "data source. Make sure that your file is readable by other users (particularly the mysql user). " +
                         "More info: '"+c.getMessage()+"'",c);
             }
             else {
                 l.error("Internal error: "+e.getMessage());
                 c = e.getCause();
                 while(c!=null) {
                     l.error("Caused by: "+c.getMessage());
                     c = c.getCause();
                 }
                 l.debug("REST API invocation error: ",e);
             }
         }
         catch (HttpMethodException e) {
             l.error("Error executing GoodData REST API: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error executing GoodData REST API.",e);
         }
         catch (GdcRestApiException e) {
             l.error("REST API invocation error: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 if(c instanceof HttpMethodException) {
                     HttpMethodException ex = (HttpMethodException)c;
                     String msg = ex.getMessage();
                     if(msg != null && msg.length()>0 && msg.indexOf("/ldm/manage")>0) {
                         l.error("Error creating/updating logical data model (executing MAQL DDL).");
                         if(msg.indexOf(".date")>0) {
                             l.error("Bad time dimension schemaReference.");
                         }
                         else {
                            l.error("You are either trying to create a data object that already exists " +
                                    "(executing the same MAQL multiple times) or providing a wrong reference " +
                                    "or schemaReference in your XML configuration.");
                         }
                     }
                 }
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("REST API invocation error: ", e);
         }
         catch (GdcException e) {
             l.error("Unrecognized error: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Unrecognized error: ",e);
         }
     }
 
     private boolean decide(Object result) {
         if(result == null)
             return false;
         else if(result instanceof Boolean)
             return ((Boolean)result).booleanValue();
         else if (result instanceof Number)
             return ((Number)result).doubleValue() != 0;
         else if(result instanceof String)
             return ((String)result).length() > 0;
         else
             return false;        
     }
 
 
     private NotificationTransport selectTransport(String uri) {
         if(uri.startsWith("sfdc")) {            
             return SfdcChatterTransport.createTransport(cliParams.get(CLI_PARAM_TRANSPORT_USERNAME[0]),
                     cliParams.get(CLI_PARAM_TRANSPORT_PASSWORD[0]));
         }
         throw new InvalidParameterException("Can't find transport for uri "+uri);
     }
 
     private final static String DEFAULT_FORMAT = "#,###.00";
 
     private void execute(String config) throws ConnectionException, IOException {
 
         NotificationConfig c = NotificationConfig.fromXml(new File(config));
 
         MessageFilter dupFilter = DuplicateMessageFilter.createFilter();
 
         try {
             for(NotificationMessage m : c.getMessages()) {
                 String dupFilterKind = m.getDupFilterKind();
                 if(dupFilterKind != null && dupFilterKind.length()>0) {
                     if(!dupFilter.filter(m.getMessage(), dupFilterKind)) {
                         l.debug("Message filtered out by the dup kind filter.");
                         l.info("Message filtered out by the dup kind filter.");
                         continue;
                     }
                 }
                 GdcRESTApiWrapper rest = new GdcRESTApiWrapper(cliParams.getHttpConfig());
                 rest.login();
                 Expression e = null;
                 JexlEngine jexl = new JexlEngine();
                 e = jexl.createExpression(m.getCondition());
                 JexlContext jc = new MapContext();
                 List<Metric> metrics = m.getMetrics();
                 double[] values = null;
                 if(metrics != null && metrics.size() >0) {
                     values = new double[metrics.size()];
                     for(int i=0; i<metrics.size(); i++) {
                         values[i] = rest.computeMetric(metrics.get(i).getUri());
                         jc.set(metrics.get(i).getAlias(), new Double(values[i]));
                     }
                 }
                 String[] texts = null;
                 List<Report> reports = m.getReports();
                 if(reports != null && reports.size() >0) {
                     texts = new String[reports.size()];
                     for(int i=0; i<reports.size(); i++) {
                         texts[i] = rest.computeReport(reports.get(i).getUri());
                     }
                 }
                 boolean result = decide(e.evaluate(jc));
                 if(result) {
                     NotificationTransport t = selectTransport(m.getUri());
                     String msg = m.getMessage();
                     if(values != null && values.length > 0 && metrics != null && metrics.size() > 0) {
                         for(int i = 0 ; i < metrics.size(); i++) {
                             String fmt = metrics.get(i).getFormat();
                             if(fmt == null || fmt.length() <= 0)
                                 fmt = DEFAULT_FORMAT;
                             DecimalFormat df = new DecimalFormat(fmt);
                             msg = msg.replace("%"+metrics.get(i).getAlias()+"%", df.format(values[i]));
                         }
                     }
                     if(texts!= null && texts.length > 0 && reports != null && reports.size() > 0) {
                         for(int i = 0 ; i < reports.size(); i++) {
                             msg = msg.replace("%"+reports.get(i).getAlias()+"%", texts[i]);
                         }
                     }
                     String dupFilterExact = m.getDupFilterExact();
                     if(dupFilterExact != null && dupFilterExact.length()>0) {
                         if(!dupFilter.filter(msg, dupFilterExact)) {
                             l.debug("Message filtered out by the dup exact filter.");
                             l.info("Message filtered out by the dup exact filter.");
                             continue;
                         }
                     }
                     String fmt = m.getMessageTimestampFormat();
                     if(fmt != null && fmt.length() > 0)
                         t.send(msg+" (at "+getTimestamp(fmt)+")");
                     else
                         t.send(msg);
                     dupFilter.update(msg);
                     dupFilter.update(m.getMessage());
                     l.info("Notification sent.");
                 }
             }
             dupFilter.save();
         }
         catch (Exception e) {
             throw new IOException(e);
         }
     }
 
     private String getTimestamp(String fmt) {
         DateTimeFormatter f = DateTimeFormat.forPattern(fmt);
         return f.print(new DateTime());
     }
 
     /**
      * Returns all cli options
      * @return all cli options
      */
     public static Options getOptions() {
         Options ops = new Options();
         for( Option o : mandatoryOptions)
             ops.addOption(o);
         for( Option o : optionalOptions)
             ops.addOption(o);
         return ops;
     }
 
     /**
      * Parse and validate the cli arguments
      * @param ln parsed command line
      * @return parsed cli parameters wrapped in the CliParams
      * @throws InvalidArgumentException in case of nonexistent or incorrect cli args
      */
     protected CliParams parse(CommandLine ln, Properties defaults) throws InvalidArgumentException {
         l.debug("Parsing cli "+ln);
         CliParams cp = new CliParams();
 
         for( Option o : mandatoryOptions) {
             String name = o.getLongOpt();
             if (ln.hasOption(name))
                 cp.put(name,ln.getOptionValue(name));
             else if (defaults.getProperty(name) != null) {
             	cp.put(name, defaults.getProperty(name));
             } else {
                 throw new InvalidArgumentException("Missing the '"+name+"' commandline parameter.");
             }
 
         }
 
         for( Option o : optionalOptions) {
             String name = o.getLongOpt();
             if (ln.hasOption(name)) {
                 cp.put(name,ln.getOptionValue(name));
             } else if (defaults.getProperty(name) != null) {
             	cp.put(name, defaults.getProperty(name));
             }
         }
 
         if(cp.containsKey(CLI_PARAM_VERSION[0])) {
            l.info("GoodData Notification Tool version 1.2.21-BETA" +
                     ((BUILD_NUMBER.length()>0) ? ", build "+BUILD_NUMBER : "."));
             System.exit(0);
 
         }
 
 
         // use default host if there is no host in the CLI params
         if(!cp.containsKey(CLI_PARAM_GDC_HOST[0])) {
             cp.put(CLI_PARAM_GDC_HOST[0], Defaults.DEFAULT_HOST);
         }
 
         l.debug("Using host "+cp.get(CLI_PARAM_GDC_HOST[0]));
 
         if (ln.getArgs().length == 0) {
             throw new InvalidArgumentException("No config file has been given, quitting.");
         }
 
         String configs = "";
         for (final String arg : ln.getArgs()) {
             if(configs.length()>0)
                 configs += ","+arg;
             else
                 configs += arg;
         }
         cp.put(CLI_PARAM_CONFIG, configs);
 
         return cp;
     }
 
 
     /**
      * Returns the help for commands
      * @return help text
      */
     public static String commandsHelp() {
         try {
         	final InputStream is = CliParams.class.getResourceAsStream("/com/gooddata/processor/COMMANDS.txt");
         	if (is == null)
         		throw new IOException();
             return FileUtil.readStringFromStream(is);
         } catch (IOException e) {
             l.error("Could not read com/gooddata/processor/COMMANDS.txt");
         }
         return "";
     }
     
 
     private static boolean checkJavaVersion() {
         String version = System.getProperty("java.version");
         if(version.startsWith("1.6") || version.startsWith("1.5"))
             return true;
         l.error("You're running Java "+version+". Please use Java 1.5 or higher for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for the Java 6 installation.");
         throw new InternalErrorException("You're running Java "+version+". Please use use Java 1.5 or higher for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for the Java 6 installation.");
     }
 
     /**
      * The main CLI processor
      * @param args command line argument
      */
     public static void main(String[] args) {
 
         checkJavaVersion();
         String logConfig = System.getProperty("log4j.configuration");
         if(logConfig != null && logConfig.length()>0) {
             File lc = new File(logConfig);
             if(lc.exists()) {
                 PropertyConfigurator.configure(logConfig);
                 Properties defaults = loadDefaults();
                 try {
                     Options o = getOptions();
                     CommandLineParser parser = new GnuParser();
                     CommandLine cmdline = parser.parse(o, args);
                     GdcNotification gdi = new GdcNotification(cmdline, defaults);
                     if (!gdi.finishedSucessfuly) {
                         System.exit(1);
                     }
                 } catch (org.apache.commons.cli.ParseException e) {
                     l.error("Error parsing command line parameters: "+e.getMessage());
                     l.debug("Error parsing command line parameters",e);
                 }
             }
             else {
                 l.error("Can't find the logging config. Please configure the logging via the log4j.configuration.");
             }
         }
         else {
             l.error("Can't find the logging config. Please configure the logging via the log4j.configuration.");
         }
     }
 
 
     /**
      * Loads default values of common parameters from a properties file searching
      * the working directory and user's home.
      * @return default configuration 
      */
     private static Properties loadDefaults() {
 		final String[] dirs = new String[]{ "user.dir", "user.home" };
 		final Properties props = new Properties();
 		for (final String d : dirs) {
 			String path = System.getProperty(d) + File.separator + DEFAULT_PROPERTIES;
 			File f = new File(path);
 			if (f.exists() && f.canRead()) {
 				try {
 					FileInputStream is = new FileInputStream(f);
 					props.load(is);
                     l.debug("Successfully red the gdi configuration from '" + f.getAbsolutePath() + "'.");                    
 					return props;
 				} catch (IOException e) {
 					l.warn("Readable gdi configuration '" + f.getAbsolutePath() + "' found be error occurred reading it.");
 					l.debug("Error reading gdi configuration '" + f.getAbsolutePath() + "': ", e);
 				}
 			}
 		}
 		return props;
 	}
 
 }

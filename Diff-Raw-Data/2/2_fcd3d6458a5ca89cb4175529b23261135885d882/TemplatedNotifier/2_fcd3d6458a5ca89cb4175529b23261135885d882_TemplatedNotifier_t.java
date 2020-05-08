 package com.luntsys.luntbuild.notifiers;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import ognl.OgnlException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.app.event.EventCartridge;
 import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 import com.luntsys.luntbuild.BuildGenerator;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.reports.Report;
 import com.luntsys.luntbuild.scrapers.MSVSScraper;
 import com.luntsys.luntbuild.utility.Luntbuild;
 
 /**
  * Encapsulates the logic for processing templates within Luntbuild.
  *
  * @author Dustin Hunter
  * @author Jason Archer
  */
 public abstract class TemplatedNotifier extends Notifier implements ReferenceInsertionEventHandler {
     /** Logger */
     protected Log logger = null;
     /** Template dir */
     public String templateDir = null;
     /** Template sub dir */
     public String subDir = null;
     /** Build template file */
     public String templateBuildFile = null;
     /** Schedule template file */
     public String templateScheduleFile = null;
 
     private Object ognlRoot = null;
     private MSVSScraper visualStudioScraper = new MSVSScraper();
 
     /** Base template directory */
     public static final String TEMPLATE_BASE_DIR = Luntbuild.installDir + File.separator + "templates";
     private static final String QUOTE_FILE = "quotes.txt";
     private static final String TEMPLATE_DEF_FILE = "set-template.txt";
     private static final String DEFAULT_TEMPLATE_BUILD = "simple-build.vm";
     private static final String DEFAULT_TEMPLATE_SCHEDULE = "simple-schedule.vm";
 
     /**
      * Creates a templated notifier.
      * 
      * @param logClass the log class
      * @param subdir the template subdir (in installdir/templates)
      */
     public TemplatedNotifier(Class logClass, String subdir) {
         logger = LogFactory.getLog(logClass);
         templateDir = TEMPLATE_BASE_DIR;
         subDir = subdir;
         setTemplateFiles();
     }
 
     /**
      * Sets the template files from the default properties.
      */
     private void setTemplateFiles() {
     	setTemplateFiles("");
     }
 
     /**
      * Sets the template files from the specified property. If the property does not exist, the default
      * properties will be used.  If the default properties do not exist, the default file names will be used.
      * 
      * @param templatePropertyName the property name to use
      */
     private void setTemplateFiles(String templatePropertyName) {
         File f = new File(templateDir + File.separator + subDir + File.separator + TEMPLATE_DEF_FILE);
         if (!f.exists()) {
             logger.error("Unable to find template definition file " + f.getPath());
             templateBuildFile = subDir + "/" + DEFAULT_TEMPLATE_BUILD;
             templateScheduleFile = subDir + "/" + DEFAULT_TEMPLATE_SCHEDULE;
             return;
         }
         Properties props = new Properties();
         FileInputStream in = null;
         try {
             in = new FileInputStream(f);
             props.load(in);
         } catch (IOException e) {
             logger.error("Unable to read template definition file " + f.getPath());
             templateBuildFile = subDir + "/" + DEFAULT_TEMPLATE_BUILD;
             templateScheduleFile = subDir + "/" + DEFAULT_TEMPLATE_SCHEDULE;
             return;
         } finally {
             if (in != null) try { in.close(); } catch (Exception e) {/*Ignore*/}
         }
         templateBuildFile = props.getProperty(templatePropertyName + "_buildTemplate");
         templateScheduleFile = props.getProperty(templatePropertyName + "_scheduleTemplate");
         if (templateBuildFile == null) templateBuildFile = props.getProperty("buildTemplate");
         if (templateScheduleFile == null) templateScheduleFile = props.getProperty("scheduleTemplate");
         if (templateBuildFile == null) templateBuildFile = DEFAULT_TEMPLATE_BUILD;
         if (templateScheduleFile == null) templateScheduleFile = DEFAULT_TEMPLATE_SCHEDULE;
         templateBuildFile = subDir + "/" + templateBuildFile;
         templateScheduleFile = subDir + "/" + templateScheduleFile;
     }
 
     /**
      * Initializes Velocity and uses the specified property for template files.
      * 
      * @param templatePropertyName the property name to use
      * @throws Exception from {@link Velocity#init(Properties)}
      */
     private void init(String templatePropertyName) throws Exception {
         Properties props = new Properties();
         props.put("file.resource.loader.path", templateDir);
         props.put("runtime.log", "velocity.log");
         Velocity.init(props);
         setTemplateFiles(templatePropertyName);
     }
 
     /**
      * Processes the template for a build notification.
      * 
      * @param build the build
      * @param ctx a Velocity context
      * @throws Exception from {@link Velocity#getTemplate(String)}
      */
     private String processTemplate(Build build, VelocityContext ctx) throws Exception {
         return processTemplate(Velocity.getTemplate(templateBuildFile),
                 build, ctx);
     }
 
     /**
      * Processes the template for a schedule notification.
      * 
      * @param schedule the schedule
      * @param ctx a Velocity context
      * @throws Exception from {@link Velocity#getTemplate(String)}
      */
     private String processTemplate(Schedule schedule, VelocityContext ctx) throws Exception {
         return processTemplate(Velocity.getTemplate(templateScheduleFile), schedule, ctx);
     }
 
     /**
      * Processes the template for a build notification.
      * 
      * @param template the template
      * @param build the build
      * @param ctx a Velocity context
      * @throws Exception from {@link Velocity#init()}
      * @throws Exception from {@link #createContext(Build, VelocityContext)}
      * @throws Exception from {@link Template#merge(org.apache.velocity.context.Context, Writer)}
      */
     private String processTemplate(Template template, Build build, VelocityContext ctx) throws Exception {
 
         Velocity.init();
 
         VelocityContext context = createContext(build, ctx);
         EventCartridge ec = new EventCartridge();
         ec.addEventHandler(this);
         ec.attachToContext(context);
         ognlRoot = build;
 
         // Process the template
         StringWriter writer = null;
         try {
             writer = new StringWriter();
 
             if (template != null)
                 template.merge(context, writer);
             return writer.toString();
         } finally {
             writer.close();
         }
     }
 
     /**
      * Processes the template for a schedule notification.
      * 
      * @param template the template
      * @param schedule the schedule
      * @param ctx a Velocity context
      * @throws Exception from {@link Velocity#init()}
      * @throws Exception from {@link #createContext(Schedule, VelocityContext)}
      * @throws Exception from {@link Template#merge(org.apache.velocity.context.Context, Writer)}
      */
     private String processTemplate(Template template, Schedule schedule, VelocityContext ctx) throws Exception {
 
         Velocity.init();
 
         VelocityContext context = createContext(schedule, ctx);
         EventCartridge ec = new EventCartridge();
         ec.addEventHandler(this);
         ec.attachToContext(context);
         ognlRoot = schedule;
 
         // Process the template
         StringWriter writer = null;
         try {
             writer = new StringWriter();
 
             if (template != null)
                 template.merge(context, writer);
             return writer.toString();
         } finally {
             writer.close();
         }
     }
 
     /**
      * @inheritDoc
      */
     public Object referenceInsert(String reference, Object value) {
         if (value != null) return value;
         try {
         	return Luntbuild.evaluateExpression(ognlRoot, reference);
         } catch (OgnlException ex) {
             return value;
         }
     }
 
 	/**
      * Populates the context with the variables which are exposed to the build template.
      * 
      * @param build the build
      * @param ctx the Velocity context
      * @throws Exception from {@link #extractRootUrl(String)}
      */
     private VelocityContext createContext(Build build, VelocityContext ctx) throws Exception {
         VelocityContext context = new VelocityContext(ctx);
 
         // System info
         context.put("luntbuild_webroot", extractRootUrl(build.getUrl()));
         context.put("luntbuild_servlet_url", Luntbuild.getServletUrl());
         context.put("luntbuild_systemlog_url", Luntbuild.getSystemLogUrl());
 
         // Project Info
         context.put("build_project", build.getSchedule().getProject().getName());
         context.put("build_project_desc", build.getSchedule().getProject().getDescription());
 
         // Schedule Info
         context.put("build_schedule", build.getSchedule().getName());
         context.put("build_schedule_desc", build.getSchedule().getDescription());
         context.put("build_schedule_url", build.getSchedule().getUrl());
         context.put("build_schedule_status", Constants.getScheduleStatusText(build.getSchedule().getStatus()));
         context.put("build_schedule_status_date",
                 Luntbuild.DATE_DISPLAY_FORMAT.format(build.getSchedule().getStatusDate()));
 
         // Build Info
         context.put("build_url", build.getUrl());
         context.put("build_version", build.getVersion());
         context.put("build_status", Constants.getBuildStatusText(build.getStatus()));
         context.put("build_isSuccess", new Boolean(build.getStatus() == Constants.BUILD_STATUS_SUCCESS));
         context.put("build_isFailure", new Boolean(build.getStatus() == Constants.BUILD_STATUS_FAILED));
         context.put("build_artifactsdir", build.getArtifactsDir());
         context.put("build_publishdir", build.getPublishDir());
         context.put("build_type", Constants.getBuildTypeText(build.getBuildType()));
         context.put("build_labelstrategy", Constants.getLabelStrategyText(build.getLabelStrategy()));
         context.put("build_changelist", build.getChangelist());
 
         // Time Info
         context.put("build_start", Luntbuild.DATE_DISPLAY_FORMAT.format(build.getStartDate()));
         context.put("build_end", Luntbuild.DATE_DISPLAY_FORMAT.format(build.getEndDate()));
         long diffSec = (build.getEndDate().getTime()-build.getStartDate().getTime())/1000;
         context.put("build_duration", "" + diffSec + " seconds");
 
         // Log info
         context.put("build_revisionlog_url", build.getRevisionLogUrl());
         context.put("build_revisionlog_text", readFile(build.getPublishDir()
             + File.separator + BuildGenerator.REVISION_LOG));
         context.put("build_buildlog_url", build.getBuildLogUrl());
         String buildText = readFile(build.getPublishDir() + File.separator + BuildGenerator.BUILD_LOG);
         context.put("build_buildlog_text", buildText);
 
         // Reports
         Enumeration reports = Luntbuild.reports.keys();
         while (reports.hasMoreElements()) {
             String report_name = (String) reports.nextElement();
             context.put("build_" + report_name + "_reporturl", ((Report)Luntbuild.reports.get(report_name)).getReportUrl(build.getPublishDir()));
         }
 
         visualStudioScraper.scrape(buildText, build, context);
 
         // Just for fun
         try {
        	context.put("build_randomquote", getRandomQuote(templateDir + "/" + subDir));
         } catch (Exception ex) {
             // If we fail, this should not affect the rest of the message
         }
         return context;
     }
 
     /**
      * Populates the context with the variables which are exposed to the schedule template.
      * 
      * @param schedule the schedule
      * @param ctx the Velocity context
      * @throws Exception from {@link #extractRootUrl(String)}
      */
     private VelocityContext createContext(Schedule schedule, VelocityContext ctx) throws Exception {
         VelocityContext context = new VelocityContext(ctx);
 
         // System info
         context.put("luntbuild_webroot", extractRootUrl(schedule.getUrl()));
         context.put("luntbuild_servlet_url", Luntbuild.getServletUrl());
         context.put("luntbuild_systemlog_url", Luntbuild.getSystemLogUrl());
 
         // Project Info
         context.put("schedule_project", schedule.getProject().getName());
         context.put("schedule_project_desc", schedule.getProject().getDescription());
 
         // Schedule Info
         context.put("schedule_name", schedule.getName());
         context.put("schedule_desc", schedule.getDescription());
         context.put("schedule_url", schedule.getUrl());
         context.put("schedule_status", Constants.getScheduleStatusText(schedule.getStatus()));
         context.put("schedule_status_date",
             Luntbuild.DATE_DISPLAY_FORMAT.format(schedule.getStatusDate()));
         context.put("schedule_publishdir", schedule.getPublishDir());
         context.put("schedule_type", Constants.getBuildTypeText(schedule.getBuildType()));
         context.put("schedule_labelstrategy", Constants.getLabelStrategyText(schedule.getLabelStrategy()));
 
         return context;
     }
 
     /**
      * Creates a message title for a schedule notification.
      * 
      * @param schedule the schedule
      * @return the message title
      */
     protected String constructNotificationTitle(Schedule schedule) {
         String scheduleDesc = schedule.getProject().getName() + "/" + schedule.getName();
         return "[luntbuild] schedule \"" + scheduleDesc + "\" " +
             com.luntsys.luntbuild.facades.Constants.getScheduleStatusText(schedule.getStatus());
     }
 
     /**
      * Creates a message body for a schedule notification.
      * 
      * @param schedule the schedule
      * @return the message body
      */
     protected String constructNotificationBody(Schedule schedule) {
         return constructNotificationBody(schedule, null);
     }
 
     /**
      * Creates a message title for a build notification.
      * 
      * @param build the build
      * @return the message title
      */
     protected String constructNotificationTitle(Build build) {
         String buildDesc = build.getSchedule().getProject().getName() +
             "/" + build.getSchedule().getName() + "/" + build.getVersion();
         return "[luntbuild] build of \"" + buildDesc +
             "\" " + com.luntsys.luntbuild.facades.Constants.getBuildStatusText(build.getStatus());
     }
 
     /**
      * Creates a message body for a build notification for recent checkin users.
      * 
      * @param build the build
      * @return the message body
      */
     protected String constructNotificationBody4CheckinUsers(Build build) {
         VelocityContext context = new VelocityContext();
         context.put("build_user_msg",
             "You have received this note because you've made checkins in the source repository recently.");
         return constructNotificationBody(build, context);
     }
 
     /**
      * Creates a message body for a build notification for subscribed users.
      * 
      * @param build the build
      * @return the message body
      */
     protected String constructNotificationBody(Build build) {
         VelocityContext context = new VelocityContext();
         context.put("build_user_msg",
             "You have received this email because you asked to be notified.");
         return constructNotificationBody(build, context);
     }
 
     /**
      * Creates a message body for a build notification.
      * 
      * @param build the build
      * @param ctx the Velocity context
      * @return the message body
      */
     private String constructNotificationBody(Build build, VelocityContext ctx) {
         try {
             init(build.getSchedule().getProject().getName().replaceAll(" ","_") + "_" + build.getSchedule().getName().replaceAll(" ","_"));
             return processTemplate(build, ctx);
         }
         catch (ResourceNotFoundException rnfe) {
             logger.error("Could not load template file: " + templateBuildFile +
                 "\nTemplateDir = " + templateDir, rnfe);
             return "Could not load template file: " + templateBuildFile + "\nTemplateDir = " +
                 templateDir;
         }
         catch (ParseErrorException pee) {
             logger.error("Unable to parse template file: " + templateBuildFile +
                 "\nTemplateDir = " + templateDir, pee);
             return "Unable to parse template file: " + templateBuildFile + "\nTemplateDir = " +
                 templateDir;
         }
         catch(Exception ex) {
             // Wrap in a runtime exception and throw it up the stack
             logger.error("Failed to process template", ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * Creates a message body for a schedule notification.
      * 
      * @param schedule the schedule
      * @param ctx the Velocity context
      * @return the message body
      */
     private String constructNotificationBody(Schedule schedule, VelocityContext ctx) {
         try {
             init(schedule.getProject().getName().replaceAll(" ","_") + "_" + schedule.getName().replaceAll(" ","_"));
             return processTemplate(schedule, ctx);
         }
         catch (ResourceNotFoundException rnfe) {
             logger.error("Could not load template file: " + templateScheduleFile +
                 "\nTemplateDir = " + templateDir, rnfe);
             return "Could not load template file: " + templateScheduleFile + "\nTemplateDir = " +
                 templateDir;
         }
         catch (ParseErrorException pee) {
             logger.error("Unable to parse template file: " + templateScheduleFile +
                 "\nTemplateDir = " + templateDir, pee);
             return "Unable to parse template file: " + templateScheduleFile + "\nTemplateDir = " +
                 templateDir;
         }
         catch(Exception ex) {
             // Wrap in a runtime exception and throw it up the stack
             logger.error("Failed to process template", ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * Gets the contents of a file.
      * 
      * @param filename the name of the file
      * @return the contents of the file
      * @throws Exception if the file cannot be read
      */
     private static final String readFile(String filename) throws Exception {
         FileInputStream fis = null;
         BufferedInputStream bis = null;
         try {
             File file = new File(filename);
             if (!file.exists())
                 throw new Exception("Cannot load system file: " + filename +
                         "\nFull Path = " + file.getAbsolutePath());
 
             fis = new FileInputStream(file);
             bis = new BufferedInputStream(fis);
 
             StringBuffer sbuf = new StringBuffer();
             int readin = 0;
             byte[] buf = new byte[1024];
             while ((readin = bis.read(buf)) > 0) {
                 sbuf.append(new String(buf, 0, readin));
             }
 
             return sbuf.toString();
         }
         finally {
             if (bis != null) bis.close();
             if (fis != null) fis.close();
         }
     }
 
     /**
      * Determines the host name and port of the server.
      * 
      * @param text the URL of the server
      * @return the root of the URL with only the protocol, host name and port
      */
     private static final String extractRootUrl(String text) throws Exception {
         URL url = new URL(text);
         return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
     }
 
     /**
      * Gets a random quote from the quote file in the specified directory.
      * 
      * @param templateDir the template directory
      * @return a random quote, or <code>null</code> if the quote file could not be found
      * @throws Exception if the quote file cannot be read
      */
     private static final String getRandomQuote(String templateDir) throws Exception {
     	try {
     		String quotes = readFile(templateDir + "/" + QUOTE_FILE);
             StringTokenizer tokenizer = new StringTokenizer(quotes, "\r");
             int tokens = tokenizer.countTokens();
             int index = (int)(Math.random() * tokens);
             while (--index > 1) tokenizer.nextToken();
             return tokenizer.nextToken();
         }
         catch(FileNotFoundException ex) {
         	// If the files not there, the just ignore it
             return null;
         }
     }
 }

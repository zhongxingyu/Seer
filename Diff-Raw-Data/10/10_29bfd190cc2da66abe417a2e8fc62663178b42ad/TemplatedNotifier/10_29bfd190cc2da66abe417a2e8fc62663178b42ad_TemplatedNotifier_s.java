 package com.luntsys.luntbuild.notifiers;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 
 import ognl.Ognl;
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
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.scrapers.MSVSScraper;
 
 /**
  * Encapsulates the logic for processing templates within Luntbuild.
  *
  * @author Dustin Hunter
  */
 public abstract class TemplatedNotifier extends Notifier implements ReferenceInsertionEventHandler {
     /** logger */
     protected Log logger = null;
     /** template dir */
     public String templateDir = null;
     /** Build template file */
     public String templateBuildFile = null;
     /** Schedule template file */
     public String templateScheduleFile = null;
    /** Subdir */
    public String subdir = null;
 
     private Object ognlRoot = null;
     private MSVSScraper visualStudioScraper = new MSVSScraper();
 
     /** base template dir */
     public static final String TEMPLATE_BASE_DIR = Luntbuild.installDir + File.separator + "templates";
     private static final String QUOTE_FILE = "quotes.txt";
     private static final String TEMPLATE_DEF_FILE = "set-template.txt";
     private static final String DEFAULT_TEMPLATE_BUILD = "simple-build.vm";
     private static final String DEFAULT_TEMPLATE_SCHEDULE = "simple-schedule.vm";
 
     /** Constructor
      * @param logClass log class
      * @param subdir template subdir (in installdir/templates)
      */
     public TemplatedNotifier(Class logClass, String subdir) {
         this.logger = LogFactory.getLog(logClass);
         this.templateDir = TEMPLATE_BASE_DIR + File.separator + subdir;
        this.subdir = subdir;
         setTemplateFiles();
     }
 
     private void setTemplateFiles() {
     	setTemplateFiles("");
     }
 
     private void setTemplateFiles(String templatePropertyName) {
         File f = new File(this.templateDir + File.separator + TEMPLATE_DEF_FILE);
         if (!f.exists()) {
             this.logger.error("Unable to find template definition file " + f.getPath());
             this.templateBuildFile = DEFAULT_TEMPLATE_BUILD;
             this.templateScheduleFile = DEFAULT_TEMPLATE_SCHEDULE;
             return;
         }
         Properties props = new Properties();
         FileInputStream in = null;
         try {
             in = new FileInputStream(f);
             props.load(in);
         } catch (IOException e) {
             this.logger.error("Unable to read template definition file " + f.getPath());
             this.templateBuildFile = DEFAULT_TEMPLATE_BUILD;
             this.templateScheduleFile = DEFAULT_TEMPLATE_SCHEDULE;
             return;
         } finally {
             if (in != null) try { in.close(); } catch (Exception e) {/*Ignore*/}
         }
         this.templateBuildFile = props.getProperty(templatePropertyName + "_buildTemplate");
         this.templateScheduleFile = props.getProperty(templatePropertyName + "_scheduleTemplate");
         if (this.templateBuildFile == null) this.templateBuildFile = props.getProperty("buildTemplate");
         if (this.templateScheduleFile == null) this.templateScheduleFile = props.getProperty("scheduleTemplate");
         if (this.templateBuildFile == null) this.templateBuildFile = DEFAULT_TEMPLATE_BUILD;
         if (this.templateScheduleFile == null) this.templateScheduleFile = DEFAULT_TEMPLATE_SCHEDULE;
     }
 
     /**
      *
      * Initialize velocity
      */
     private void init(String templatePropertyName) throws Exception {
         Properties props = new Properties();
         props.put("file.resource.loader.path", this.templateDir);
         props.put("runtime.log", "velocity.log");
         Velocity.init(props);
         setTemplateFiles(templatePropertyName);
     }
 
     private void init() throws Exception {
         init("");
     }
 
     /**
      * Process the template.
      */
     private String processTemplate(Build build, VelocityContext ctx) throws Exception {
        return processTemplate(Velocity.getTemplate(this.subdir + "/" + this.templateBuildFile),
                 build, ctx);
     }
 
     private String processTemplate(Schedule schedule, VelocityContext ctx) throws Exception {
        return processTemplate(Velocity.getTemplate(this.subdir + "/" + this.templateScheduleFile),
                 schedule, ctx);
     }
 
     private String processTemplate(Template template, Build build, VelocityContext ctx) throws Exception {
 
         Velocity.init();
 
         VelocityContext context = createContext(build, ctx);
         EventCartridge ec = new EventCartridge();
         ec.addEventHandler(this);
         ec.attachToContext(context);
         this.ognlRoot = build;
 
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
 
     private String processTemplate(Template template, Schedule schedule, VelocityContext ctx)
     throws Exception {
 
         Velocity.init();
 
         VelocityContext context = createContext(schedule, ctx);
         EventCartridge ec = new EventCartridge();
         ec.addEventHandler(this);
         ec.attachToContext(context);
         this.ognlRoot = schedule;
 
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
 
     public Object referenceInsert(String reference, Object value) {
         if (value != null) return value;
         try {
             if (reference.startsWith("${")) {
                 reference = reference.substring(2, reference.length() - 1);
             }
             Object expression = Ognl.parseExpression(reference);
             Map ctx = Ognl.createDefaultContext(this.ognlRoot);
             Object ognlValue;
             ognlValue = Ognl.getValue(expression, ctx, this.ognlRoot, String.class);
             return ognlValue;
         } catch (OgnlException ex) {
             return value;
         }
     }
 
 	/**
      * Populates the context with the variables which are exposed to the
      * build template.
      */
     private VelocityContext createContext(Build build, VelocityContext ctx) throws Exception {
         VelocityContext context = new VelocityContext(ctx);
 
         context.put("luntbuild_webroot", extractRootUrl(build.getUrl()));
 
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
 
         // Time Info
         context.put("build_start", Luntbuild.DATE_DISPLAY_FORMAT.format(build.getStartDate()));
         context.put("build_end", Luntbuild.DATE_DISPLAY_FORMAT.format(build.getEndDate()));
         long diffSec = (build.getEndDate().getTime()-build.getStartDate().getTime())/1000;
 
 
         context.put("build_duration", "" + diffSec + " seconds");
 
         context.put("build_artifactsdir", build.getArtifactsDir());
         context.put("build_publishdir", build.getPublishDir());
 
         // Log info
         context.put("build_revisionlog_url", build.getRevisionLogUrl());
         context.put("build_revisionlog_text", readFile(build.getPublishDir()
                 + File.separator + BuildGenerator.REVISION_LOG));
         context.put("build_buildlog_url", build.getBuildLogUrl());
         String buildText = readFile(build.getPublishDir()+ File.separator + BuildGenerator.BUILD_LOG);
         context.put("build_buildlog_text", buildText);
         context.put("luntbuild_systemlog_url", Luntbuild.getSystemLogUrl());
 
         context.put("build_junit_reportdir", build.getJunitHtmlReportDir());
         context.put("build_type",
                 com.luntsys.luntbuild.facades.Constants.getBuildTypeText(build.getBuildType()));
         context.put("build_labelstrategy",
                 com.luntsys.luntbuild.facades.Constants.getLabelStrategyText(build.getLabelStrategy()));
         context.put("luntbuild_servlet_url", Luntbuild.getServletUrl());
 
         this.visualStudioScraper.scrape(buildText, build, context);
 
         // Just for fun
         try {
         	context.put("build_randomquote", getRandomQuote(this.templateDir));
         }
         catch (Exception ex) {
             // If we fail, this should not affect the rest of the message
         }
         return context;
     }
 
     /**
      * Populates the context with the variables which are exposed to the
      * build template.
      */
     private VelocityContext createContext(Schedule schedule, VelocityContext ctx) throws Exception {
         VelocityContext context = new VelocityContext(ctx);
 
         context.put("luntbuild_webroot", extractRootUrl(schedule.getUrl()));
 
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
 
         // Log info
         context.put("luntbuild_systemlog_url", Luntbuild.getSystemLogUrl());
 
         context.put("schedule_type",
                 com.luntsys.luntbuild.facades.Constants.getBuildTypeText(schedule.getBuildType()));
         context.put("schedule_labelstrategy",
                 com.luntsys.luntbuild.facades.Constants.getLabelStrategyText(schedule.getLabelStrategy()));
         context.put("luntbuild_servlet_url", Luntbuild.getServletUrl());
 
         return context;
     }
 
     protected String constructNotificationTitle(Schedule schedule) {
         String scheduleDesc = schedule.getProject().getName() + "/" + schedule.getName();
         return "[luntbuild] schedule \"" + scheduleDesc + "\" " +
             com.luntsys.luntbuild.facades.Constants.getScheduleStatusText(schedule.getStatus());
     }
 
     protected String constructNotificationBody(Schedule schedule) {
         return constructNotificationBody(schedule, null);
     }
 
     protected String constructNotificationTitle(Build build) {
         String buildDesc = build.getSchedule().getProject().getName() +
         "/" + build.getSchedule().getName() + "/" + build.getVersion();
         return "[luntbuild] build of \"" + buildDesc +
         "\" " + com.luntsys.luntbuild.facades.Constants.getBuildStatusText(build.getStatus());
     }
 
     protected String constructNotificationBody4CheckinUsers(Build build) {
         VelocityContext context = new VelocityContext();
         context.put("build_user_msg",
                 "You have received this note because you've made checkins in the source repository recently.");
         return constructNotificationBody(build, context);
     }
 
     protected String constructNotificationBody(Build build) {
         VelocityContext context = new VelocityContext();
         context.put("build_user_msg",
                 "You have received this email because you asked to be notified.");
         return constructNotificationBody(build, context);
     }
 
     private String constructNotificationBody(Build build, VelocityContext ctx) {
         try {
             init(build.getSchedule().getProject().getName().replaceAll(" ","_") + "_" + build.getSchedule().getName().replaceAll(" ","_"));
             return processTemplate(build, ctx);
         }
         catch (ResourceNotFoundException rnfe) {
             this.logger.error("Could not load template file: " + this.templateBuildFile
                     + "\nTemplateDir = " + this.templateDir, rnfe);
             return "Could not load template file: " + this.templateBuildFile + "\nTemplateDir = " +
             this.templateDir;
         }
         catch (ParseErrorException pee) {
             this.logger.error("Unable to parse template file: " + this.templateBuildFile +
                     "\nTemplateDir = " + this.templateDir, pee);
             return "Unable to parse template file: " + this.templateBuildFile + "\nTemplateDir = " +
             this.templateDir;
         }
         catch(Exception ex) {
             // Wrap in a runtime exception and throw it up the stack
             this.logger.error("Failed to process template", ex);
             throw new RuntimeException(ex);
         }
     }
 
     private String constructNotificationBody(Schedule schedule, VelocityContext ctx) {
         try {
             init(schedule.getProject().getName().replaceAll(" ","_") + "_" + schedule.getName().replaceAll(" ","_"));
             return processTemplate(schedule, ctx);
         }
         catch (ResourceNotFoundException rnfe) {
             this.logger.error("Could not load template file: " + this.templateScheduleFile
                     + "\nTemplateDir = " + this.templateDir, rnfe);
             return "Could not load template file: " + this.templateScheduleFile + "\nTemplateDir = " +
             this.templateDir;
         }
         catch (ParseErrorException pee) {
             this.logger.error("Unable to parse template file: " + this.templateScheduleFile +
                     "\nTemplateDir = " + this.templateDir, pee);
             return "Unable to parse template file: " + this.templateScheduleFile + "\nTemplateDir = " +
             this.templateDir;
         }
         catch(Exception ex) {
             // Wrap in a runtime exception and throw it up the stack
             this.logger.error("Failed to process template", ex);
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * A convience method for reading in a file.
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
      * A convience method for reading the contents at a url.
      */
     private static final String readUrl(String url) throws Exception{
         URL source = new URL(url);
         DataInputStream dis = new DataInputStream(source.openStream());
 
         StringBuffer sbuf = new StringBuffer();
         int readin = 0;
         byte[] buf = new byte[1024];
         while ((readin = dis.read(buf)) > 0) {
             sbuf.append(new String(buf, 0, readin));
         }
         dis.close();
         return sbuf.toString();
     }
 
     /**
      * A convience method for determining the hostname and port of the
      * server.
      */
     private static final String extractRootUrl(String text) throws Exception {
         URL url = new URL(text);
         return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
     }
 
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

 package info.mikaelsvensson.ftpbackup.log.report;
 
 import info.mikaelsvensson.ftpbackup.command.fileprocessing.FileProcessingStrategyEvent;
 import info.mikaelsvensson.ftpbackup.command.fileprocessing.FileProcessingStrategyMovedEvent;
 import info.mikaelsvensson.ftpbackup.util.Configuration;
 import info.mikaelsvensson.ftpbackup.util.UnknownNamedArgumentException;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.MultiPartEmail;
 import org.apache.log4j.Logger;
 
 import javax.mail.util.ByteArrayDataSource;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.Date;
 
 public class EmailReport extends AbstractSummaryReport {
 // ------------------------------ FIELDS ------------------------------
 
     private static final Logger LOGGER = Logger.getLogger(EmailReport.class);
     private static final String DEFAULT_BODY_TEMPLATE = "MESSAGE TEMPLATE NOT SPECIFIED.";
     private static final String DEFAULT_SUBJECT_TEMPLATE = "Backup report";
     private static final String DEFAULT_BODY_DESTINATION_TEMPLATE = "Report for destination {id} was not configured.";
     private static final String DEFAULT_REMOTE_ACTIONS_LOG_FORMAT = "{0,date,HH:mm:ss}, destination {6}: {4,choice,0#failed to|1#successfully} {3} {5}";
     private static final String DEFAULT_REMOTE_MOVE_ACTION_LOG_FORMAT = "{0,date,HH:mm:ss}, destination {7}: {4,choice,0#failed to|1#successfully} {3} {5} to {6}";
     public static final String TO = "to";
     public static final String SUBJECT_TEMPLATE = "subjectTemplate";
     public static final String BODY_TEMPLATE = "bodyTemplate";
     public static final String BODY_DESTINATION_TEMPLATE = "bodyDestinationTemplate";
     public static final String ATTACH_REMOTE_ACTIONS_LOG = "attachRemoteActionsLog";
     public static final String REMOTE_ACTIONS_LOG_DEFAULT_ITEM_FORMAT = "remoteActionsLogDefaultItemFormat";
     public static final String REMOTE_ACTIONS_LOG_MOVED_ITEM_FORMAT = "remoteActionsLogMovedItemFormat";
     private final String to;
     private final String subjectTemplate;
     private final String bodyTemplate;
     private final String bodyDestinationTemplate;
     private ByteArrayOutputStream remoteActionsLogStream = null;
     private RemoteActionsLogFileReport remoteActionsLog = null;
     private final boolean attachRemoteActionsLog;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public EmailReport(String to, String subjectTemplate, String bodyTemplate, String bodyDestinationTemplate, Date backupStartDate, boolean attachRemoteActionsLog, String remoteActionsLogDefaultItemFormat, String remoteActionsLogMovedItemFormat) {
         super(backupStartDate);
         this.bodyTemplate = StringUtils.defaultIfEmpty(bodyTemplate, DEFAULT_BODY_TEMPLATE);
         this.subjectTemplate = StringUtils.defaultIfEmpty(subjectTemplate, DEFAULT_SUBJECT_TEMPLATE);
         this.bodyDestinationTemplate = StringUtils.defaultIfEmpty(bodyDestinationTemplate, DEFAULT_BODY_DESTINATION_TEMPLATE);
         this.to = to;
         this.attachRemoteActionsLog = attachRemoteActionsLog;
         remoteActionsLogDefaultItemFormat = StringUtils.defaultIfEmpty(remoteActionsLogDefaultItemFormat, DEFAULT_REMOTE_ACTIONS_LOG_FORMAT);
         remoteActionsLogMovedItemFormat = StringUtils.defaultIfEmpty(remoteActionsLogMovedItemFormat, DEFAULT_REMOTE_MOVE_ACTION_LOG_FORMAT);
 
         if (attachRemoteActionsLog) {
             remoteActionsLogStream = new ByteArrayOutputStream();
             remoteActionsLog = new RemoteActionsLogFileReport(
                     new PrintWriter(
                             new OutputStreamWriter(
                                     remoteActionsLogStream,
                                     Charset.forName("UTF-8"))),
                     remoteActionsLogDefaultItemFormat,
                     remoteActionsLogMovedItemFormat);
         }
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Report ---------------------
 
     @Override
     public void generate() {
         LOGGER.info("Sending backup report to " + to);
         try {
             MultiPartEmail email = createEmail();
             email.addTo(to);
             email.setSubject(getFormattedText(subjectTemplate, null));
             email.setMsg(getFormattedText(bodyTemplate, bodyDestinationTemplate));
             if (attachRemoteActionsLog) {
                 remoteActionsLog.generate();
                 try {
                     email.attach(new ByteArrayDataSource(remoteActionsLogStream.toByteArray(), "text/plain"), "remote-actions.log", "");
                     remoteActionsLogStream.close();
                 } catch (IOException e) {
                     LOGGER.debug("IOException when generating e-mail attachment or closing output stream.", e);
                 } catch (EmailException e) {
                     LOGGER.info("Could not attach remote actions log to e-mail.", e);
                 }
             }
             email.send();
         } catch (EmailException e) {
             LOGGER.warn("Could not send backup report to " + to, e);
         } catch (UnknownNamedArgumentException e) {
             LOGGER.warn("The text template contains an invalid argument name", e);
         }
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     private MultiPartEmail createEmail() throws EmailException {
         MultiPartEmail email = new MultiPartEmail();
        String smtpUserName = Configuration.getString(getClass(), "username");
        String smtpPassword = Configuration.getString(getClass(), "password");
        String smtpHost = Configuration.getString(getClass(), "host");
        String smtpFrom = Configuration.getString(getClass(), "from");
        email.setAuthentication(smtpUserName, smtpPassword);
        email.setHostName(smtpHost);
        email.setFrom(smtpFrom);
        email.setSmtpPort(Configuration.getInteger(getClass(), "port", 25));
        return email;
     }
 
     @Override
     public void onFolderCreated(final FileProcessingStrategyEvent event) {
         super.onFolderCreated(event);
         if (remoteActionsLog != null) {
             remoteActionsLog.onFolderCreated(event);
         }
     }
 
     @Override
     public void onFileDeleted(final FileProcessingStrategyEvent event) {
         super.onFileDeleted(event);
         if (remoteActionsLog != null) {
             remoteActionsLog.onFileDeleted(event);
         }
     }
 
     @Override
     public void onFolderDeleted(final FileProcessingStrategyEvent event) {
         super.onFolderDeleted(event);
         if (remoteActionsLog != null) {
             remoteActionsLog.onFolderDeleted(event);
         }
     }
 
     @Override
     public void onFileMoved(final FileProcessingStrategyMovedEvent event) {
         super.onFileMoved(event);
         if (remoteActionsLog != null) {
             remoteActionsLog.onFileMoved(event);
         }
     }
 
     @Override
     public void onFileUploaded(final FileProcessingStrategyEvent event) {
         super.onFileUploaded(event);
         if (remoteActionsLog != null) {
             remoteActionsLog.onFileUploaded(event);
         }
     }
 
 }

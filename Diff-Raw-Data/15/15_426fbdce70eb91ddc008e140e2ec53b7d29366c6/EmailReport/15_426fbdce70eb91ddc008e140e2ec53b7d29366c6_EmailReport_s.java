 package info.mikaelsvensson.ftpbackup.log.report;
 
 import info.mikaelsvensson.ftpbackup.util.Configuration;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.apache.log4j.Logger;
 
 public class EmailReport extends AbstractSummaryReport {
 // ------------------------------ FIELDS ------------------------------
 
     private static final Logger LOGGER = Logger.getLogger(EmailReport.class);
     private final String to;
     private final String subjectTemplate;
     private final String bodyTemplate;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public EmailReport(String to, String subjectTemplate, String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
        this.subjectTemplate = subjectTemplate;
         this.to = to;
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Report ---------------------
 
     @Override
     public void generate() {
         LOGGER.info("Sending backup report to " + to);
 
         try {
             SimpleEmail email = createEmail();
             email.addTo(to);
             email.setSubject(getFormattedText(subjectTemplate));
             email.setMsg(getFormattedText(bodyTemplate));
             email.send();
         } catch (EmailException e) {
             LOGGER.warn("Could not send backup report to " + to, e);
         }
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     private SimpleEmail createEmail() throws EmailException {
         SimpleEmail email = new SimpleEmail();
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
 }

 package fi.helsinki.cs.okkopa.main.stage;
 
 import fi.helsinki.cs.okkopa.database.FailedEmailDAO;
 import fi.helsinki.cs.okkopa.file.save.Saver;
 import fi.helsinki.cs.okkopa.mail.send.EmailSender;
 import fi.helsinki.cs.okkopa.main.ExceptionLogger;
 import fi.helsinki.cs.okkopa.shared.Settings;
 import fi.helsinki.cs.okkopa.model.FailedEmailDbModel;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import javax.mail.MessagingException;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class RetryFailedEmailsStage extends Stage {
 
     private static final Logger LOGGER = Logger.getLogger(RetryFailedEmailsStage.class.getName());
     private ExceptionLogger exceptionLogger;
     private EmailSender emailSender;
     private String saveRetryFolder;
     private Saver fileSaver;
     private FailedEmailDAO failedEmailDatabase;
     private int retryExpirationMinutes;
 
     /**
      *
      * @param emailSender
      * @param exceptionLogger
      * @param settings
      * @param fileSaver
      * @param failedEmailDatabase
      */
     @Autowired
     public RetryFailedEmailsStage(EmailSender emailSender, ExceptionLogger exceptionLogger,
             Settings settings, Saver fileSaver, FailedEmailDAO failedEmailDatabase) {
         this.emailSender = emailSender;
         this.exceptionLogger = exceptionLogger;
         this.fileSaver = fileSaver;
         this.failedEmailDatabase = failedEmailDatabase;
 
         this.saveRetryFolder = settings.getProperty("mail.send.retrysavefolder");
         this.retryExpirationMinutes = Integer.parseInt(settings.getProperty("mail.send.retryexpirationminutes"));
     }
 
     @Override
     public void process(Object in) {
         checkFailedEmails();
         processNextStages(null);
     }
 
     private void sendEmail(FailedEmailDbModel failedEmail, InputStream attachment) throws MessagingException {
         LOGGER.debug("Lähetetään sähköposti.");
         emailSender.send(failedEmail.getReceiverEmail(), attachment);
     }
 
     private void checkFailedEmails() {
         // Get failed email send attachments (PDF-files)
         LOGGER.debug("Yritetään lähettää sähköposteja uudelleen.");
 
         ArrayList<File> fileList = fileSaver.list(saveRetryFolder);
 
         if (fileList == null) {
             LOGGER.debug("Ei uudelleenlähetettävää.");
             return;
         }
 
         // Get list of failed emails from database
         List<FailedEmailDbModel> failedEmails;
         try {
             failedEmails = failedEmailDatabase.listAll();
         } catch (SQLException ex) {
             exceptionLogger.logException(ex);
             return;
         }
         matchFilesAndSend(failedEmails, fileList);
         cleanNonmatching(failedEmails, fileList);
     }
 
     private boolean retryFailedEmail(File pdf, FailedEmailDbModel failedEmail) {
         FileInputStream fis;
         try {
             fis = new FileInputStream(pdf);
         } catch (FileNotFoundException ex) {
             exceptionLogger.logException(ex);
             LOGGER.debug("Tiedostoa ei ollutkaan levyllä vaikka listaus sen palautti.");
             return true;
         }
 
         try {
             sendEmail(failedEmail, fis);
             pdf.delete();
             LOGGER.debug("Lähetettiin sähköposti onnistuneesti (uusintayritys).");
         } catch (MessagingException ex) {
             exceptionLogger.logException(ex);
             deleteFileIfTooOld(failedEmail, pdf);
             return true;
         }
         IOUtils.closeQuietly(fis);
         return false;
     }
 
     private void matchFilesAndSend(List<FailedEmailDbModel> failedEmails, ArrayList<File> fileList) {
         // Match files and send
         for (FailedEmailDbModel failedEmail : failedEmails) {
             for (File pdf : fileList) {
                 if (failedEmail.getFilename().equals(pdf.getName())
                         && retryFailedEmail(pdf, failedEmail)) {
                     continue;
                 }
             }
         }
     }
 
     private void deleteFileIfTooOld(FailedEmailDbModel failedEmail, File pdf) {
         // Delete if too old.
         long ageInMinutes = TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - failedEmail.getFailTime().getTime());
         if (ageInMinutes > retryExpirationMinutes) {
             LOGGER.debug("Vanhentunut viesti poistettu.");
             pdf.delete();
             try {
                 failedEmailDatabase.deleteFailedEmail(failedEmail);
             } catch (SQLException ex) {
                 exceptionLogger.logException(ex);
             }
         }
     }
 
     private void cleanNonmatching(List<FailedEmailDbModel> failedEmails, ArrayList<File> fileList) {
         // TODO clean nonmatching database <-> folder
     }
 }

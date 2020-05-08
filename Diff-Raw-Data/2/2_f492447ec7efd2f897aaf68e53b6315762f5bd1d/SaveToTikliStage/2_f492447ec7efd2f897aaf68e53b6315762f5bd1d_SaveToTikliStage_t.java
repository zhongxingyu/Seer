 package fi.helsinki.cs.okkopa.main.stage;
 
 import com.unboundid.ldap.sdk.LDAPException;
 import fi.helsinki.cs.okkopa.shared.database.OracleConnector;
 import fi.helsinki.cs.okkopa.shared.exception.NotFoundException;
 import fi.helsinki.cs.okkopa.ldap.LdapConnector;
 import fi.helsinki.cs.okkopa.main.BatchDetails;
 import fi.helsinki.cs.okkopa.main.ExceptionLogger;
 import fi.helsinki.cs.okkopa.shared.Settings;
 import fi.helsinki.cs.okkopa.shared.database.model.CourseDbModel;
 import fi.helsinki.cs.okkopa.model.ExamPaper;
 import fi.helsinki.cs.okkopa.shared.database.model.FeedbackDbModel;
 import fi.helsinki.cs.okkopa.shared.database.model.StudentDbModel;
 import java.security.GeneralSecurityException;
 import java.sql.SQLException;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SaveToTikliStage extends Stage<ExamPaper, ExamPaper> {
 
     private static final Logger LOGGER = Logger.getLogger(SaveToTikliStage.class.getName());
     private boolean tikliEnabled;
     private LdapConnector ldapConnector;
     private ExceptionLogger exceptionLogger;
     private OracleConnector oc;
     private Settings settings;
     private BatchDetails batch;
 
     /**
      *
      * @param ldapConnector
      * @param settings
      * @param exceptionLogger
      * @param batch
      */
     @Autowired
     public SaveToTikliStage(LdapConnector ldapConnector, Settings settings,
             ExceptionLogger exceptionLogger, BatchDetails batch) {
         this.settings = settings;
         this.ldapConnector = ldapConnector;
         tikliEnabled = Boolean.parseBoolean(settings.getProperty("tikli.enable"));
         this.exceptionLogger = exceptionLogger;
         this.oc = new OracleConnector(settings);
         this.batch = batch;
     }
 
     @Override
     public void process(ExamPaper examPaper) {
         if (batch.getCourseCode() != null && tikliEnabled) {
             try {
                 // Get student number from LDAP:
                 ldapConnector.setStudentInfo(examPaper.getStudent());
                 
                 saveToTikli(examPaper);
                 LOGGER.debug("Koepaperi tallennettu Tikliin.");
                 batch.addSuccessfulTikliSave();
                 
             } catch (NotFoundException | LDAPException | GeneralSecurityException ex) {
                 exceptionLogger.logException(ex);
             }
         }
         processNextStages(examPaper);
     }
 
     private void saveToTikli(ExamPaper examPaper) {
         CourseDbModel course = getCourceDbModel();
         FeedbackDbModel feedback = getFeedbackDbModel(course, examPaper);
         StudentDbModel student = getStudentDbModel(examPaper);
         
         connectToKurkiAndInsertFeedback(course, student, feedback);
         LOGGER.debug("Tikliin tallennus päättyi!");
     }
 
     private CourseDbModel getCourceDbModel() {
         return new CourseDbModel(batch.getCourseCode(), batch.getPeriod(),
                 batch.getYear(), batch.getType(), batch.getCourseNumber());
     }
 
     private FeedbackDbModel getFeedbackDbModel(CourseDbModel course, ExamPaper examPaper) {
         return new FeedbackDbModel(settings, course, examPaper.getPdf(),
                 examPaper.getStudent().getStudentNumber());
     }
 
     private StudentDbModel getStudentDbModel(ExamPaper examPaper) {
         return new StudentDbModel(examPaper.getStudent().getStudentNumber());
     }
 
     private void connectToKurkiAndInsertFeedback(CourseDbModel course, StudentDbModel student, FeedbackDbModel feedback) {
         try {
             oc.connect();
             LOGGER.debug("Connected to Kurki db.");
             
             boolean courseExists = oc.courseExists(course);
             LOGGER.debug("Course found from Kurki: " + courseExists);
             
             boolean studentExists = oc.studentExists(student);
             LOGGER.debug("Student found from Kurki: " + studentExists);
             
             if (courseExists && studentExists) {
                 oc.insertFeedBackRow(feedback);
             }
         } catch (SQLException ex) {
            exceptionLogger.logException(ex);
         } finally {
             oc.disconnect();
         }
     }
 }

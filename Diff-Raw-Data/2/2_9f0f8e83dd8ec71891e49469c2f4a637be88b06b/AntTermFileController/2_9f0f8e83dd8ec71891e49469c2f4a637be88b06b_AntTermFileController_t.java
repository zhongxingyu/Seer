 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Term;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.repository.ITermDAO;
 
 import java.io.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Controller provides implementation for generating files with term for the ants aglorithm
  * @author Michal Partyka
  */
 @Controller
 public class AntTermFileController {
     private static final File termsFile = new File("/tmp/termsFile.txt");
     private static final Logger LOGGER = LoggerFactory.getLogger(AntTermFileController.class.getName());
     private StreamedContent streamedTerms;
 
     @Autowired
     ITermDAO termDAO;
 
     @Autowired
     ISubjectDAO subjectDAO;
 
     public void generateTermsFile(Enroll enrollment) {
         LOGGER.debug("I am in generate Terms...");
         try (BufferedWriter output = new BufferedWriter( new FileWriter(termsFile))) {
             output.write("\n" + basicTermsInformation(subjectDAO.getSubjectsByEnrollment(enrollment)));
         } catch (IOException e) {
             LOGGER.error("Some IO Problems: ", e);
         }
 
         try {
             streamedTerms = new DefaultStreamedContent( new FileInputStream(termsFile));
         } catch (FileNotFoundException e) {
             LOGGER.debug("there is no given file :(: " + termsFile.getName(), e);
         }
     }
 
     private String basicTermsInformation(List<Subject> subjects) {
         StringBuilder singleTermDetails = new StringBuilder();
         StringBuilder collisions = new StringBuilder();
 
         for (Subject subject: subjects) {
             if (!subject.getHasInteractive()) {
                 //skip subjects with only certain fields
                 continue;
             }
             //make subjectID a header: (ant format)
             singleTermDetails.append("[").append(subject.getSubjectID()).append("]\n");
             List<Term> terms = termDAO.getTermsBySubjectOderByTermID(subject);
 
             for (Term term: terms) {
                 if (term.getCertain()) {
                     //skip certain terms:
                     continue;
                 }
                 //Iterate through subjects and for every subject put line into file:
                 //(Clean foreach costs some performance issue :-)
                 singleTermDetails.append(createLineWithSeparator(":", term.getTermPerSubjectID(),
                    term.getCapacity(), getAntFormatOfWeekDay(term.getStartTime()),
                    getAntFormatOfDate(term.getStartTime()), getAntFormatOfDate(term.getEndTime())));
                 singleTermDetails.append("\n");
 
                 for (Term termCollision: terms) {
                       //captured two times situation when term is in collision with another term - ant format require it
                     collisions.append(checkForCollision(term, termCollision));
                 }
             }
         }
         return singleTermDetails.toString() + "[kolizje]\n" + collisions.toString();
     }
 
     private String checkForCollision(Term term, Term termCollision) {
         if (term.equals(termCollision)) {
             return "";
         }
         Calendar firstTermStart = Calendar.getInstance();
         Calendar firstTermEnd = Calendar.getInstance();
         Calendar secondTermStart = Calendar.getInstance();
         Calendar secondTermEnd = Calendar.getInstance();
 
         firstTermStart.setTime(term.getStartTime());
         firstTermEnd.setTime(term.getEndTime());
         secondTermStart.setTime(termCollision.getStartTime());
         secondTermEnd.setTime(termCollision.getEndTime());
 
        if ( !firstTermEnd.before(secondTermStart) && !firstTermStart.after(secondTermEnd)) {
             //print in the ant format:
             return term.getSubject().getSubjectID().toString() + "," +
                     term.getTermPerSubjectID() + ";" + termCollision.getSubject().getSubjectID() +
                     "," + termCollision.getTermPerSubjectID() + "\n";
         }
         return "";
     }
 
 
     private String createLineWithSeparator(String delimiter, Object... objects) {
         StringBuilder stringBuilder = new StringBuilder();
         int i=0; //number of objects already printed
         for (Object part: objects) {
             stringBuilder.append(part.toString());
             if (++i < objects.length) {  // no the last one? add delimiter:
                 stringBuilder.append(delimiter);
             }
         }
         return stringBuilder    .toString();
     }
 
     private Integer getAntFormatOfDate(Date date) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
 
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         int hourDiff = hour - 7;
 
         int minute = calendar.get(Calendar.MINUTE);
 
         //Every hour 4 terms, every 15minutes 1 term, counting from 0
         return hourDiff*4 + minute/15;
     }
 
     private Integer getAntFormatOfWeekDay(Date date) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
 
         return calendar.get(Calendar.DAY_OF_WEEK)-2;
     }
 
     public StreamedContent getStreamedTerms() {
         return streamedTerms;
     }
 }

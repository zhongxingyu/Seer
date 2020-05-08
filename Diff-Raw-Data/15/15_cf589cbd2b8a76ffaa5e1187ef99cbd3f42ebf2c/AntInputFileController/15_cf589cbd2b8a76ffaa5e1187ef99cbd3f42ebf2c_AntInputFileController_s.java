 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.UploadedFile;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.StudentPointsPerTerm;
 import pl.agh.enrollme.model.Term;
 import pl.agh.enrollme.model.TermPK;
 import pl.agh.enrollme.repository.IPersonDAO;
 import pl.agh.enrollme.repository.IStudentPointsPerTermDAO;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.repository.ITermDAO;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 public class AntInputFileController {
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AntTermFileController.class.getName());
     private UploadedFile file;
     private static final String REGEX = "\\[[0-9]+\\]";
 
     @Autowired
     private IPersonDAO personDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private IStudentPointsPerTermDAO studentPointsPerTermDAO;
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
 
     public UploadedFile getFile() {
         return file;
     }
 
     public void setFile(UploadedFile file) {
         this.file = file;
     }
 
     public void upload() {
         if (file == null) {
             FacesMessage msg = new FacesMessage("Failed!", "No file have been uploaded! Try again?");
             FacesContext.getCurrentInstance().addMessage(null, msg);
         }
 
         Integer index = 0; //current user index (current while parsing)
 
         Pattern p = Pattern.compile(REGEX);
         String line;
 
         try (BufferedReader bufferedReader = new BufferedReader( new InputStreamReader (file.getInputstream()))) {
             while ((line = bufferedReader.readLine()) != null) {
                 line = line.trim();
                 Matcher m = p.matcher(line);
                 if (m.lookingAt()) {
                     line = line.substring(1, line.length() -1);
                     LOGGER.debug(" I read index: " + line);
                     index = Integer.parseInt(line);
                 } else {
                     String[] assignedTermData = line.split(":");
                     if (assignedTermData.length > 1) {
                         LOGGER.debug("subjectID: " + assignedTermData[0]);
                         LOGGER.debug("TermID: " + assignedTermData[1]);
 
                         try {
                             assignTerm(index, Integer.parseInt(assignedTermData[0]), Integer.parseInt(assignedTermData[1]));
                         } catch (IllegalStateException e) {
                             FacesMessage msg = new FacesMessage("IllegalStateException: ", e.getMessage());
                             FacesContext.getCurrentInstance().addMessage(null, msg);
                             throw e;
                         }
 
                     } else {
                         LOGGER.debug("Empty line? ");
                     }
                 }
             }
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             LOGGER.debug("IOException while importing file: ", e);
         }
 
 
     }
 
     private void assignTerm(Integer index, Integer subjectID, Integer termPerSubjectID) {
         Person person = personDAO.getByIndex(index);
         if (person == null) {
             throw new IllegalStateException("there is no person with provided index in DB!. Index: " + index);
         }
 
         Term term = termDAO.getByPK(new TermPK(subjectDAO.getByPK(subjectID), termPerSubjectID));
         if (term == null) {
             throw new IllegalStateException("There is no Term with subjectID: " + subjectID + "termID: " +
                     termPerSubjectID);
         }
 
         StudentPointsPerTerm studentPointsPerTerm = studentPointsPerTermDAO.getByPersonAndTerm(person, term);
         if (studentPointsPerTerm == null) {
            throw new IllegalStateException("Lack of record for the given term: " + term + " and person: " + person +
                    "in studentPointsPerTerm table");
         }
        studentPointsPerTerm.setAssigned(true);
        studentPointsPerTermDAO.update(studentPointsPerTerm);
     }
 }

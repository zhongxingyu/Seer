 package pl.agh.enrollme.service;
 
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.SelectableDataModelForSubjects;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.model.Teacher;
 import pl.agh.enrollme.utils.Color;
 import pl.agh.enrollme.utils.DayOfWeek;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Service
 public class SubjectChoosingService implements ISubjectChoosingService {
 
     private Subject[] chosenSubjects;
     private SelectableDataModelForSubjects model;
 
     @Override
     public List<Subject> getAvailableSubjectForEnrollment(Enroll enroll) {
         //TODO: later get from database, enroll.getSubjects() ;-)
//        Teacher teacher1 = new Teacher("dr", "Stanisław", "Sobieszko", "4.11");
//        Teacher teacher2 = new Teacher("dr", "Stasio", "Mieszko", "4.11");
//        Subject subject1 = new Subject(enroll, null, "Analiza", 1, "#00ffff", "4.33", teacher1, DayOfWeek.MONDAY,
//                null, null);
//        Subject subject2 = new Subject(enroll, null, "Fizyka", 1, "#ff0000", "4.11", teacher2, DayOfWeek.FRIDAY,
//                null, null);
//        subject1.setSubjectID(1);
//        subject2.setSubjectID(2);
//        List<Subject> subjects = new ArrayList<Subject>(2);
//        subjects.add(subject1);
//        subjects.add(subject2);
//        model = new SelectableDataModelForSubjects(subjects);
        return null;
     }
 
     public boolean userAlreadySubmitedSubjects() {
         return false;
     }
 
     public void setModel(SelectableDataModelForSubjects model) {
         this.model = model;
     }
 
     public SelectableDataModelForSubjects getModel() {
         return model;
     }
 
     public void setChosenSubjects(Subject[] chosenSubjects) {
         this.chosenSubjects = chosenSubjects;
     }
 
     public Subject[] getChosenSubjects() {
         return chosenSubjects;
     }
 }

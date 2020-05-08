 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.SelectableDataModel;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.SelectableDataModelForSubjects;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.repository.IEnrollmentDAO;
 import pl.agh.enrollme.repository.IPersonDAO;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.service.ISubjectChoosingService;
 import pl.agh.enrollme.service.PersonService;
 
 import javax.faces.bean.ViewScoped;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 @ViewScoped
 public class SubjectChoosingController implements ISubjectChoosingService {
 
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SubjectChoosingController.class.getName());
     private Subject[] chosenSubjects;
     private SelectableDataModel<Subject> model;
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private IPersonDAO personDAO;
 
     @Autowired
     private IEnrollmentDAO enrollDAO;
 
     @Autowired
     private PersonService personService;
 
     public boolean userAlreadySubmitedSubjects() {
         return false;
     }
 
     @Override
     public List<Subject> getAvailableSubjectForEnrollment(Enroll enroll) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setModel(SelectableDataModel<Subject> model) {
         this.model = model;
     }
 
     public SelectableDataModel<Subject> getModel() {
         return model;
     }
 
     public void createModel(Enroll enrollment) {
         enrollment = enrollDAO.getByPK(enrollment.getEnrollID());
         final Person person = personService.getCurrentUser();
         final List<Subject> personSubjects = person.getSubjects();
         final List<Subject> choosenList = new ArrayList<>();
         for (Subject subject : personSubjects) {
             subject = subjectDAO.getSubject(subject.getSubjectID());
             Enroll subjectEnroll = subject.getEnroll();
            subjectEnroll = enrollDAO.getByPK(subjectEnroll.getEnrollID());
             if (subjectEnroll.equals(enrollment)) {
                 choosenList.add(subject);
             }
         }
         chosenSubjects = choosenList.toArray(new Subject[]{});
         List<Subject> subjects = subjectDAO.getSubjectsByEnrollment(enrollment);
         model = new SelectableDataModelForSubjects(subjects);
     }
 
     public void setChosenSubjects(Subject[] chosenSubjects) {
         this.chosenSubjects = chosenSubjects;
     }
 
     public Subject[] getChosenSubjects() {
         return chosenSubjects;
     }
 }

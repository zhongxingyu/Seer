 package pl.agh.enrollme.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.controller.termmanagement.AdminScheduleController;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.repository.ISubjectDAO;
 import pl.agh.enrollme.repository.ITeacherDAO;
 import pl.agh.enrollme.repository.ITermDAO;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Piotr Turek
  */
 @Service
 public class TermManagementService implements ITermManagementService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TermManagementService.class);
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private ITeacherDAO teacherDAO;
 
     @PersistenceContext
     EntityManager em;
 
     @Override
     @Transactional
     public AdminScheduleController createScheduleController(Enroll enroll) {
         //Enroll configuration of the current enroll
         final EnrollConfiguration enrollConfiguration = enroll.getEnrollConfiguration();
 
         final List<Subject> subjects = subjectDAO.getSubjectsByEnrollment(enroll);
         LOGGER.debug("Subjects of " + enroll + " enrollment retrieved: " + subjects);
 
         final List<Teacher> teachers = teacherDAO.getList();
         LOGGER.debug("Teachers retrieved: " + teachers);
 
         //list of terms to display
         final List<Term> terms = new ArrayList<>();
 
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             LOGGER.debug("Terms of subject: " + s.getSubjectID() + " retrieved: " + termsBySubject);
             if (termsBySubject.size() > 0) {
                 terms.addAll(termsBySubject);
                 LOGGER.debug(termsBySubject.size() + " terms added to terms list");
             }
         }
         LOGGER.debug("Terms retrieved (" + terms.size() + " of them): " + terms);
 
         //creating schedule controller
         final AdminScheduleController scheduleController = new AdminScheduleController(enrollConfiguration, subjects,
                 terms, teachers);
         LOGGER.debug("ScheduleController created: " + scheduleController);
 
         return scheduleController;
     }
 
 
     @Override
     @Transactional
     public void saveScheduleState(AdminScheduleController scheduleController) {
         final List<Subject> subjects = scheduleController.getSubjects();
         clearSubjectTerms(subjects);    //delete all terms belonging to subjects from current enrollment
         LOGGER.debug("Terms cleared");
 
         final List<Term> terms = scheduleController.getTerms();
         LOGGER.debug(terms.size() + " terms retrieved from schedule");
         for (Term t : terms) {
             LOGGER.debug("Term retrieved: " + t);
             if (t.getStartTime() == null || t.getEndTime() == null || t.getSubject() == null || t.getTeacher() == null) {
                 LOGGER.debug("faulty term!");
                 FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Faulty data!",
                         "Data could not be saved due to illegal/faulty state. Try reloading the view.");
                 addMessage(message);
                 return;
             }
         }
 
         final Map<Integer, Integer> termCounters = new HashMap<>();         //map containing term counters; subjectID is the key
         final List<Term> certainTerms = new ArrayList<>();                  //list containing _certain_ terms of the current subject
 
         //Initialize map counters
         for (Subject subject : subjects) {
             termCounters.put(subject.getSubjectID(), 1);
             LOGGER.debug("Subject counter init, ID=" + subject.getSubjectID());
         }
         LOGGER.debug("Counters initialized");
 
         //Setting TermPerSubjectID of non-certain terms
         for (Term term : terms) {
             if (term.getCertain()) {
                 certainTerms.add(term);
                 LOGGER.debug("Term: " + term + " is certain. Saving it for later processing");
                 continue;
             }
 
             setTermPerSubjectID(termCounters, term);
         }
         LOGGER.debug("Non-certain terms' ids set");
 
         //Setting TermPerSubjectID of certain terms
         for (Term term : certainTerms) {
             setTermPerSubjectID(termCounters, term);
         }
         LOGGER.debug("IDs set");
 
         for (Term term : terms) {
             term.setSubject(subjectDAO.update(term.getSubject()));
             term.setTeacher(teacherDAO.update(term.getTeacher()));
             final Subject subject = term.getSubject();
 
             if(!subject.getHasInteractive() && !term.getCertain()) {
                 subject.setHasInteractive(true);
             }
 
             termDAO.add(term);
             LOGGER.debug("Term: " + term + " has been persisted");
         }
         LOGGER.debug("State persisted");
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Data saved", terms.size() + " terms were saved.");
         addMessage(message);
     }
 
     private void setTermPerSubjectID(Map<Integer, Integer> termCounters, Term term) {
         final Subject termSubject = term.getSubject();
         final Integer id = termCounters.get(termSubject.getSubjectID());
         term.setTermPerSubjectID(id);
         termCounters.put(termSubject.getSubjectID(), id+1);
         LOGGER.debug("Term: " + term + " set termpersubjectid to " + id);
     }
 
     /**
      * Deletes from database all terms belonging to subjects from subjects list
      * @param subjects
      */
     private void clearSubjectTerms(List<Subject> subjects) {
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             for (Term t : termsBySubject) {
                 LOGGER.debug("Removing term: " + t);
                 termDAO.remove(t);
             }
         }
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 }

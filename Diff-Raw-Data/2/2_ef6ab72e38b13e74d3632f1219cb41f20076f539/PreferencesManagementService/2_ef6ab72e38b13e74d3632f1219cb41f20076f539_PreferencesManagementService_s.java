 package pl.agh.enrollme.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.controller.preferencesmanagement.PreferencesManagementController;
 import pl.agh.enrollme.controller.preferencesmanagement.ProgressRingController;
 import pl.agh.enrollme.controller.preferencesmanagement.ScheduleController;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.repository.*;
 import pl.agh.enrollme.utils.EnrollmentMode;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Piotr Turek
  */
 @Service
 public class PreferencesManagementService implements IPreferencesManagementService {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(PreferencesManagementService.class);
 
     @Autowired
     private IPersonDAO personDAO;
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     @Autowired
     private ITermDAO termDAO;
 
     @Autowired
     private IStudentPointsPerTermDAO pointsDAO;
 
     @Autowired
     private IEnrollmentDAO enrollDAO;
 
     @Autowired
     private PersonService personService;
 
     @Override
     @Transactional
     public ScheduleController createScheduleController(Enroll enroll) {
         final Person person = personService.getCurrentUser();
         LOGGER.debug("Person: " + person + " retrieved from security context");
 
         //Enroll configuration of the current enroll
         final EnrollConfiguration enrollConfiguration = enroll.getEnrollConfiguration();
         LOGGER.debug("EnrollConfiguration: " + enrollConfiguration + " retrieved from enroll: " + enroll);
 
         final List<Subject> subjectsByEnrollment = subjectDAO.getSubjectsByEnrollment(enroll);
         LOGGER.debug("Subjects of " + enroll + " enrollment retrieved: " + subjectsByEnrollment);
 
         final List<Subject> personSubjects = subjectDAO.getSubjectsByPerson(person);
         LOGGER.debug("Subjects of " + person + " person retrieved: " + personSubjects);
 
         //list of subjects belonging to the currentEnrollment, choosen by person
         final List<Subject> subjects = new ArrayList<>();
 
         for (Subject subject : personSubjects) {
             if (subjectsByEnrollment.contains(subject)) {
                 subjects.add(subject);
             }
         }
         LOGGER.debug("Intersection found: " + subjects);
 
         //list of terms to display
         final List<Term> terms = new ArrayList<>();
 
         for (Subject s : subjects) {
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             if (termsBySubject.size() > 0) {
                 terms.addAll(termsBySubject);
             }
         }
         LOGGER.debug("Terms retrieved (" + terms.size() + " of them): " + terms);
 
         //list of currently assigned points
         final List<StudentPointsPerTerm> points = new ArrayList<>();
 
         for (Term t : terms) {
             final StudentPointsPerTerm byPersonAndTerm = pointsDAO.getByPersonAndTerm(person, t);
             if (byPersonAndTerm != null) {
                 points.add(byPersonAndTerm);
             }
         }
         LOGGER.debug("Current preferences retrieved (" + points.size() + " of them): " + points);
 
         //create missing point per terms so that every term has its sppt (they can be missing because it's the first
         //the user entered preferences-management or there were some zero-point sppt that didn't get persisted)
         createMissingSPPT(terms, points, person);
         LOGGER.debug("Missing point per terms created, there are " + points.size() + " in total.");
 
         //creating progress ring controller
         final ProgressRingController ringController = new ProgressRingController(enrollConfiguration, subjects, terms, points);
         LOGGER.debug("ProgressRingController created: " + ringController);
 
         //creating schedule controller
         final ScheduleController scheduleController = new ScheduleController(ringController, enrollConfiguration, subjects, terms, points);
         LOGGER.debug("ScheduleController created: " + scheduleController);
 
         return scheduleController;
 
     }
 
     @Override
     @Transactional
     public void saveScheduleController(Enroll currentEnroll, ScheduleController scheduleController) {
         currentEnroll = enrollDAO.getByPK(currentEnroll.getEnrollID());
 
         //Importing enroll configuration
         final EnrollConfiguration enrollConfiguration = currentEnroll.getEnrollConfiguration();
 
         if (currentEnroll.getEnrollmentMode() == EnrollmentMode.CLOSED
                 || currentEnroll.getEnrollmentMode()  == EnrollmentMode.COMPLETED) {
             FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Enroll closed!", "Current enrollment has" +
                     " either closed or completed. Unfortunatelly, saving is no longer possible. Contact ... someone ;)");
             addMessage(message);
 
             LOGGER.debug("Save requested after enroll had been completed/closed!");
 
             return;
         }
 
         //Importing subject/points map
         final Map<Integer, Integer> pointsMap = scheduleController.getProgressController().getPointsMap();
 
         //Importing extra points used
         final int extraPointsUsed = scheduleController.getProgressController().getExtraPointsUsed();
 
         //Importing point per terms
         final List<StudentPointsPerTerm> termPoints = scheduleController.getPoints();
 
         //Validating
         if (!validateMinimumReached(pointsMap, enrollConfiguration)) {
             FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Rule Broken!", "You have not reached minimum" +
                     "rule for all subjects. Cannot save!");
             addMessage(message);
 
             LOGGER.debug("Minimum rule broken!");
 
             return;
         }
         LOGGER.debug("Points validated");
 
         int addedCount = 0;
         int updatedCount = 0;
         int removedCount = 0;
 
         //Persisting points
         for (StudentPointsPerTerm tp : termPoints) {
             StudentPointsPerTerm termPoint;
             Term term;
 
             termPoint = pointsDAO.getByPK(tp.getId());
 
             if (termPoint != null) {
                 term = termPoint.getTerm();
             } else {
                 term = null;
             }
 
             //termPoint is not present in the database
             if (termPoint == null) {
                 if (tp.getPoints() != 0) {
                     pointsDAO.add(tp);
                     LOGGER.debug("Term points: " + termPoint + " added to the datebase");
                     addedCount++;
                 } else {
                     LOGGER.debug("Term points: " + termPoint + " not present in the datebase, but zero-point");
                 }
             } else if (!termPoint.getAssigned() && !term.getCertain()) { //is present in the database and isn't assigned yet and corresponding term isn't certain
                 if (termPoint.getPoints() == 0) {
                     pointsDAO.remove(termPoint);
                     LOGGER.debug("Term points: " + termPoint + " removed from the datebase");
                     removedCount++;
                 } else {
                     pointsDAO.update(termPoint);
                     LOGGER.debug("Term points: " + termPoint + " updated in the datebase");
                     updatedCount++;
                 }
             }
         }
         LOGGER.debug("Points persisted");
 
         final Person person = personService.getCurrentUser();
         LOGGER.debug("Current person retrieved");
 
         final List<Subject> subjects = person.getSubjects();
         final List<Subject> subjectsSaved = person.getSubjectsSaved();
 
         for (Subject subject : subjects) {
             if (subject.getHasInteractive() && !subjectsSaved.contains(subject)) {
                 subjectsSaved.add(subject);
                 LOGGER.debug("Subject: " + subject + " added to persons: " + person + " saved subjects list");
             }
         }
         LOGGER.debug("Saved subjects updated");
 
         personDAO.update(person);
         LOGGER.debug("Person: " + person + " updated");
 
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Choice updated", "Changes successfully saved. "
             + addedCount + " term points have been added, " + updatedCount + " updated, " + removedCount + " removed from the datebase");
         addMessage(message);
 
         LOGGER.debug("Saving finished");
     }
 
     /**
      * Validates if minimum point usage has been reached for all subjects
      */
     private boolean validateMinimumReached(Map<Integer, Integer> pointsMap, EnrollConfiguration enrollConfiguration) {
         final Integer minimumPointsPerSubject = enrollConfiguration.getMinimumPointsPerSubject();
         for (Integer used : pointsMap.values()) {
             if (used != null && used < minimumPointsPerSubject) {
                 LOGGER.debug("Minimum rule has not been reached!");
                 return false;
             }
         }
         LOGGER.debug("Points validated correctly");
         return true;
     }
 
     /**
      * Creates missing SPPTs
      */
     private void createMissingSPPT(List<Term> terms, List<StudentPointsPerTerm> points, Person person) {
         Map<Term, Boolean> termsPresent = new HashMap<Term, Boolean>();
 
         for (StudentPointsPerTerm sppt : points) {
             final Term term = sppt.getTerm();
             termsPresent.put(term, true);
             LOGGER.debug("Term: " + term + " has points");
         }
 
         for (Term term : terms) {
             Boolean hasPoints = termsPresent.get(term);
             if (hasPoints == null || hasPoints == false) {
                 points.add(new StudentPointsPerTerm(term, person, 0, "", false));
                 LOGGER.debug("Points created for term: " + term);
             }
         }
     }
 
     private void addMessage(FacesMessage message) {
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
 
 }

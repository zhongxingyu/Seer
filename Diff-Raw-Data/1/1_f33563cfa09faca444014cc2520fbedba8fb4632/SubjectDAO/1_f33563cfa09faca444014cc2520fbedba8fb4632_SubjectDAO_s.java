 package pl.agh.enrollme.repository;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.*;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class SubjectDAO extends GenericDAO<Subject> implements ISubjectDAO {
     private final static Logger LOGGER = LoggerFactory.getLogger(SubjectDAO.class.getName());
 
     @Autowired
     IEnrollmentDAO enrollmentDAO;
 
     @Autowired
     IPersonDAO personDAO;
 
     @Autowired
     IStudentPointsPerTermDAO pointsDAO;
 
     @Autowired
     ITermDAO termDAO;
 
     public SubjectDAO() {
         super(Subject.class);
     }
 
     @PersistenceContext
     EntityManager em;
 
     /**
      * Add subjects to the current user subjects list
      * @param subjects - array of subjects.
      */
     @Override
     @Transactional
     public void fillCurrentUserSubjectList(Subject[] subjects) {
         UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         Person person = personDAO.findByUsername(userDetails.getUsername());
 
         LOGGER.debug("User: " + person.getUsername() + " [" + person.getIndeks() + "] submitted subjects: " +
                 Arrays.asList(subjects));
 
         final List<Subject> toRemove = new ArrayList<>();
         final List<Subject> personSubjects = person.getSubjects();
         for (Subject s : personSubjects) {
             boolean contained = false;
             for (Subject s2 : subjects) {
                 if (s2.equals(s)) {
                     contained = true;
                     break;
                 }
             }
             if (!contained) {
                 toRemove.add(s);
             }
         }
         LOGGER.debug("To remove list computed: " + toRemove);
 
         //Removing points assigned to terms belonging to subjects that are no longer choosen
         for (Subject s : toRemove) {
             s = getByPK(s.getSubjectID());
             final List<Term> termsBySubject = termDAO.getTermsBySubject(s);
             LOGGER.debug("Terms of subject: " + s + " retrieved");
             for (Term t : termsBySubject) {
                 final StudentPointsPerTerm byPersonAndTerm = pointsDAO.getByPersonAndTerm(person, t);
                 if (byPersonAndTerm != null) {
                     pointsDAO.remove(byPersonAndTerm);
                     LOGGER.debug("Points of term: " + t + " removed");
                 }
             }
             s.getPersons().remove(person);
             LOGGER.debug("Person removed from subject");
         }
 
         personSubjects.removeAll(toRemove);
         LOGGER.debug("Unnecessary subjects removed");
 
         for (Subject subject : subjects) {
             subject.addPerson(person);
             person.addSubject(subject);
             LOGGER.debug("add new subject to student: " + subject);
         }
         em.merge(person);
         LOGGER.debug("Person updated");
     }
 
     @Override
     @Transactional
     public List<Subject> getSubjectsByPerson(Person person) {
         person = personDAO.getByPK(person.getId());
         return person.getSubjects();
     }
 
     @Override
     @Transactional
     /**
      * @return list of subjects assigned to the given enrollment
      */
     public List<Subject> getSubjectsByEnrollment(Enroll enrollment) {
         LOGGER.debug("getSubjectsByEnrollment: enrollment: " + enrollment.getEnrollID() + " " + enrollment.getName());
         enrollment = enrollmentDAO.getByPK(enrollment.getEnrollID());
         return enrollment.getSubjects();
     }
 
     @Override
     @Transactional
     @Deprecated
     public Subject getSubject(Integer id) {
         return em.find(Subject.class, id);
     }
 
     @Override
     @Transactional
     @Deprecated
     public List<Subject> getSubjectsWithGroups(Enroll enroll) {
 //        Teacher teacher1 = new Teacher("dr", "Stanisław", "Sobieszko", "4.11");
 //        Teacher teacher2 = new Teacher("dr", "Stasio", "Mieszko", "4.11");
 //        Subject subject1 = new Subject(enroll, null, "Mikroprocki", 2, "#00ffff", "4.33", teacher1, DayOfWeek.MONDAY,
 //                null, null);
 //        Subject subject2 = new Subject(enroll, null, "PSI 2", 4, "#ff0000", "4.11", teacher2, DayOfWeek.FRIDAY,
 //                null, null);
         //subject1.setSubjectID(1);
         //subject2.setSubjectID(2);
 //        List<Subject> subjects = new ArrayList<Subject>(2);
 //        subjects.add(subject1);
 //        subjects.add(subject2);
         //em.merge(enroll);
         //em.persist(enroll);
         //em.merge(subject1);
 //        em.persist(subject1);
         //em.merge(subject2);
 //        em.persist(subject2);
         return null;
     }
 }

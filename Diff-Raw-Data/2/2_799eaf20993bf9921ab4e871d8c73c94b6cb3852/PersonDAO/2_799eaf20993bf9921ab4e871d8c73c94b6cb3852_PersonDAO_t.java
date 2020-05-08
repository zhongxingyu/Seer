 package pl.agh.enrollme.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.Subject;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.*;
 
 @Repository
 public class PersonDAO extends GenericDAO<Person> implements IPersonDAO {
 
     @Autowired
     private IEnrollmentDAO enrollmentDAO;
 
     @PersistenceContext
     private EntityManager em;
 
     public PersonDAO() {
         super(Person.class);
     }
 
     @Transactional
     @Override
     public Person findByUsername(String username) {
         final TypedQuery<Person> query = em.createQuery("Select p from Person p where p.username = :username",
                 Person.class).setParameter("username", username);
         final List<Person> resultList = query.getResultList();
 
         if (resultList.size() > 1) {
            throw new IllegalStateException("User " + username + " is not unique in data source!");
         }
 
         if (resultList.isEmpty()) {
             return null;
         } else {
             return resultList.get(0);
         }
     }
 
     @Override
     @Transactional
     public Person getCurrentUser() {
         UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         Person person = (Person)userDetails;
         return getByPK(person.getId());
     }
 
     @Transactional
     @Override
     public List<Person> getPeopleWhoSavedPreferencesForCustomEnrollment(Enroll enrollment) {
         Enroll enrollmentFromDB = enrollmentDAO.getByPK(enrollment.getEnrollID());
         List<Person> people = enrollmentFromDB.getPersons();
         Set<Person> peopleWithSavedSubjects = new LinkedHashSet<>();
         for (Person person: people) {
             if (!person.getSubjectsSaved().isEmpty()) {
                 peopleWithSavedSubjects.add(person);
             }
         }
        return new ArrayList<>(peopleWithSavedSubjects);
     }
 
     @Override
     public List<Subject> getSavedSubjects(Person person) {
         Person obtainedFromDB = getByPK(person.getId());
         return obtainedFromDB.getSubjects();
     }
 
     @Override
     @Transactional
     public Person getByIndex(Integer index) {
         final TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p WHERE p.indeks = :index", Person.class).
                 setParameter("index", index);
         final List<Person> resultList = query.getResultList();
 
         if (resultList.size() > 1) {
             throw new IllegalStateException("Indeks " + index + " is not unique in DB!");
         }
 
         return resultList.isEmpty() ? null : resultList.get(0);
     }
 }

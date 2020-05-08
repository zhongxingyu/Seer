 package pl.agh.enrollme.repository;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.StudentPointsPerTerm;
 import pl.agh.enrollme.model.Term;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 @Repository
 public class StudentPointsPerTermDAO extends GenericDAO<StudentPointsPerTerm> implements IStudentPointsPerTermDAO {
 
     @PersistenceContext
     private EntityManager em;
 
     public StudentPointsPerTermDAO() {
         super(StudentPointsPerTerm.class);
     }
 
     @Override
     @Transactional
     public StudentPointsPerTerm getByPersonAndTerm(Person person, Term term) {
         final TypedQuery<StudentPointsPerTerm> query = em.createQuery("Select s from StudentPointsPerTerm s where s.person = :person and s.term = :term",
                 StudentPointsPerTerm.class).setParameter("person", person).setParameter("term", term);
 
        final List<StudentPointsPerTerm> resultList = query.getResultList();

        if (resultList.isEmpty()) {
            return null;
        } else if (resultList.size() == 1) {
            return resultList.get(0);
        }

        throw new IllegalStateException("There is more than one points entity for person: " + person + " and term: " + term);
     }
 }

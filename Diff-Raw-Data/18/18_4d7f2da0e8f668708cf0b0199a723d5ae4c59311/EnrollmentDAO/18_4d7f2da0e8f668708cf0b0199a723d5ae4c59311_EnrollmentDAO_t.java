 package pl.agh.enrollme.repository;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enrollment;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 @Repository
 public class EnrollmentDAO implements IEnrollmentDAO {
 
     @PersistenceContext
     EntityManager em;
 
     @Transactional
     public void addEnrollment(Enrollment enrollment) {
         em.persist(enrollment);
     }
 
     @Transactional
     public List<Enrollment> listEnrollments() {
        /*CriteriaQuery<Enrollment> c = em.getCriteriaBuilder().createQuery(Enrollment.class);
         Root<Enrollment> from = c.from(Enrollment.class);
        c.orderBy(em.getCriteriaBuilder().asc(from.get("name")));*/
 
        return (List<Enrollment>)em.createQuery("FROM Enrollment").getResultList();
     }
 }

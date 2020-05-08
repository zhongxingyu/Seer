 package pl.agh.enrollme.repository;
 
 import org.hibernate.Hibernate;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.EnrollConfiguration;
 import pl.agh.enrollme.utils.EnrollmentMode;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class EnrollmentDAO extends GenericDAO<Enroll> implements IEnrollmentDAO {
 
     public EnrollmentDAO() {
         super(Enroll.class);
     }
 
    /*
     * Just call getSubjects on enrollment in the same transaction you retrieve that enrollment.
     * Also, I think you shouldn't use hibernate directly when using JPA.
     */
//    public Enroll getByPK(Object PK) {
//        Enroll enrollment = super.getByPK(PK);
//        Hibernate.initialize(enrollment.getSubjects());
//        return enrollment;
//    }
     
     //Onlyfor debug TODO: remove later
     public void testByID(Integer id) {
         Enroll enroll = getEntityManager().find(Enroll.class, id);
         enroll.getSubjects();
     }
 }

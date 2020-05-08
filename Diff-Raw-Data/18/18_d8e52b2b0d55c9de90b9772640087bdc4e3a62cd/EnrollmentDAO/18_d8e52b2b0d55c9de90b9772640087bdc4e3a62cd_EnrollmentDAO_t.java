 package pl.agh.enrollme.repository;
 
 import org.hibernate.Hibernate;
 import org.springframework.stereotype.Repository;
 import pl.agh.enrollme.model.Enroll;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class EnrollmentDAO extends GenericDAO<Enroll> implements IEnrollmentDAO {
 
     public EnrollmentDAO() {
         super(Enroll.class);
     }
 
     public Enroll getByPK(Object PK) {
         Enroll enrollment = super.getByPK(PK);
         Hibernate.initialize(enrollment.getSubjects());
         return enrollment;
     }
 }

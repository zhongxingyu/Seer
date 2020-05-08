 package pl.agh.enrollme.repository;
 
 import pl.agh.enrollme.model.Enrollment;
 
 import java.util.List;
 
 public interface IEnrollmentDAO {
     void addEnrollment(Enrollment enrollment);
    List<Enrollment> listEnrollments();
 }

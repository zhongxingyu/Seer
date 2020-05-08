 package pl.agh.enrollme.service;
 
 import org.primefaces.event.RowEditEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.repository.IEnrollmentDAO;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Service
 public class EnrollmentService implements Serializable, IEnrollmentService {
 
     @Autowired
     private IEnrollmentDAO enrollmentDAO;
 
 private static final long serialVersionUID = -5771235478609230476L;
 
    @Override
     public List<Enroll> getEnrollmentList() {
         return enrollmentDAO.getEnrollments();
    }
 
     @Override
     public void onEdit(RowEditEvent event) {
         Enroll editedEnrollment = (Enroll) event.getObject();
 
         if (editedEnrollment != null) {
             enrollmentDAO.update(editedEnrollment);
         }
     }
 }

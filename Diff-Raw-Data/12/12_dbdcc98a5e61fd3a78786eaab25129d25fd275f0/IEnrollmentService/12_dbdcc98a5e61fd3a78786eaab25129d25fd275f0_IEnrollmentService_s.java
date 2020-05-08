 package pl.agh.enrollme.service;
 
 import org.primefaces.event.RowEditEvent;
 import pl.agh.enrollme.model.Enroll;
 
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 public interface IEnrollmentService {
     void onEdit(RowEditEvent event);
 
    List<Enroll> getEnrollmentList();
 }

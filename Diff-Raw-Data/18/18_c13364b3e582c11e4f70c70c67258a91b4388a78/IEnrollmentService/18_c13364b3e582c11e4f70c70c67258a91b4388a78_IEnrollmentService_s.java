 package pl.agh.enrollme.service;
 
 import org.primefaces.event.RowEditEvent;
 
 /**
  * @author Michal Partyka
  */
 public interface IEnrollmentService {
     void onEdit(RowEditEvent event);
 }

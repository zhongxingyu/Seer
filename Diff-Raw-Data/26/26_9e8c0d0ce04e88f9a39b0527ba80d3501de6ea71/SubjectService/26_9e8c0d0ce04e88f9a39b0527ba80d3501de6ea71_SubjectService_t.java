 package pl.agh.enrollme.service;
 
 import org.primefaces.event.RowEditEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.repository.ISubjectDAO;
 
 /**
  * @author Michal Partyka
  */
 @Service
public class SubjectService {
 
     @Autowired
     private ISubjectDAO subjectDAO;
 
     public void onEdit(RowEditEvent event) {
         Subject editedEnrollment = (Subject) event.getObject();
 
         if (editedEnrollment != null) {
             subjectDAO.update(editedEnrollment);
         }
     }
 }

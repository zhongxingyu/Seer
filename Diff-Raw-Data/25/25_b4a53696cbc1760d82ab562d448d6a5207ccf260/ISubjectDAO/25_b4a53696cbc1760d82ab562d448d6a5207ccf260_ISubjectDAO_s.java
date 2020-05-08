 package pl.agh.enrollme.repository;
 
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Subject;
 
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 public interface ISubjectDAO {
     void fillCurrentUserSubjectList(Subject[] subjects);
 
     List<Subject> getSubjectsByEnrollment(Enroll enrollment);
 
     List<Subject> getSubjectsWithGroups(Enroll enroll);
 
 }

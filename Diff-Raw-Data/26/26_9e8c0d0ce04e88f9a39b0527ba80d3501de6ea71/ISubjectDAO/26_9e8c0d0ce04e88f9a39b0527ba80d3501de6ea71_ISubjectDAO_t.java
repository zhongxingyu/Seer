 package pl.agh.enrollme.repository;
 
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Subject;
 
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 public interface ISubjectDAO {
     @Transactional
     void fillCurrentUserSubjectList(Subject[] subjects);
 
     @Transactional
     List<Subject> getSubjectsByEnrollment(Enroll enrollment);
 
     @Transactional
     void update(Subject update);

    @Transactional
    List<Subject> getSubjectsWithGroups(Enroll enroll);

    @Transactional
    Subject getSubject(Integer id);
 }

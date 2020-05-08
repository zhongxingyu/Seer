 package pl.agh.enrollme.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Subject;
 
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class SubjectDAO extends GenericDAO<Subject> implements ISubjectDAO {
 
     @Autowired
     IEnrollmentDAO enrollmentDAO;
 
     public SubjectDAO() {
         super(Subject.class);
     }
 
     @Override
     public void fillCurrentUserSubjectList(Subject[] subjects) {
         //TODO: fill the subjects into databse under currentUser. (for subjects, user.addSubject())...
         System.out.println(( (UserDetails) SecurityContextHolder.getContext().getAuthentication().
                 getPrincipal()).getUsername());
     }
 
     @Override
     public List<Subject> getSubjectsByEnrollment(Enroll enrollment) {
         enrollmentDAO.getByPK(enrollment.getEnrollID());
         return enrollment.getSubjects();
     }
 }

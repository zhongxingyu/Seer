 package no.niths.application.rest.school;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.aop.ApiEvent;
 import no.niths.application.rest.AbstractRESTControllerImpl;
 import no.niths.application.rest.RESTConstants;
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.application.rest.school.interfaces.StudentController;
 import no.niths.common.constants.AppNames;
 import no.niths.common.constants.SecurityConstants;
 import no.niths.common.helpers.ValidationHelper;
 import no.niths.domain.Domain;
 import no.niths.domain.school.Course;
 import no.niths.domain.school.Student;
 import no.niths.domain.security.Role;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.school.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 @RequestMapping(AppNames.STUDENTS)
 public class StudentControllerImpl extends AbstractRESTControllerImpl<Student>
         implements StudentController {
 
     private static final Logger logger = LoggerFactory
             .getLogger(StudentControllerImpl.class);
 
     private StudentList studentList = new StudentList();
 
     @Autowired
     private StudentService service;
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR +
             " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
     @RequestMapping(
             value   = "{id}",
             method  = RequestMethod.GET,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public Student getById(@PathVariable Long id) {
         return super.getById(id);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or " +
             "(hasRole('ROLE_STUDENT') and principal.studentId == #domain.id)")
     public void update(@RequestBody Student domain) {
         logger.info(
                 domain.getEmail()     + " : " +
                 domain.getId()        + " : " +
                 domain.getFirstName() + " : " +
                 domain.getLastName()  + " : " +
                 domain.getGender());
         super.update(domain);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @ApiEvent(title="Student created")
     public void create(@RequestBody Student domain, HttpServletResponse res) {
         super.create(domain, res);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR +
             " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
     public void delete(@PathVariable long id) {
         super.delete(id);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     public ArrayList<Student> getAll(Student domain) {
         return super.getAll(domain);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     public ArrayList<Student> getAll(
             Student domain,
             @PathVariable int firstResult,
             @PathVariable int maxResults) {
         return super.getAll(domain, firstResult, maxResults);
     }
 
     /**
      * {@inheritDoc}
      */
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value   = "course",
             method  = RequestMethod.GET,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public List<Student> getStudentsWithNamedCourse(Course course) {
         String name = course.getName();
         logger.info(name);
         renewList(service.getStudentsWithNamedCourse(name));
         return studentList;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/course/{courseId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Added")
     public void addCourse(
             @PathVariable Long studentId,
             @PathVariable Long courseId) {
         service.addCourse(studentId, courseId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/course/{courseId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Removed")
     public void removeCourse(
             @PathVariable Long studentId,
             @PathVariable Long courseId) {
         service.removeCourse(studentId, courseId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/committee/{committeeId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Added")
     public void addCommittee(
             @PathVariable Long studentId,
             @PathVariable Long committeeId) {
         service.addCommittee(studentId,committeeId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/committee/{committeeId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Removed")
     public void removeCommittee(
             @PathVariable Long studentId,
             @PathVariable Long committeeId) {
         service.removeCommittee(studentId,committeeId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/feed/{feedId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Added")
     public void addFeed(
             @PathVariable Long studentId,
             @PathVariable Long feedId) {
         service.addFeed(studentId,feedId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/feed/{feedId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Removed")
     public void removeFeed(
             @PathVariable Long studentId,
             @PathVariable Long feedId) {
         service.removeFeed(studentId, feedId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/loan/{loanId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Added")
     public void addLoan(
             @PathVariable Long studentId,
             @PathVariable Long loanId) {
         service.addLoan(studentId,loanId); 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "{studentId}/loan/{loanId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Removed")
     public void removeLoan(
             @PathVariable Long studentId,
             @PathVariable Long loanId) {
         service.removeLoan(studentId, loanId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GenericService<Student> getService() {
         return service;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ListAdapter<Student> getList() {
         return studentList;
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ONLY_ADMIN)
     @RequestMapping(
             value  = { "{studentId}/role/{roleId}" },
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Role added")
     public void addRole(
             @PathVariable Long studentId,
             @PathVariable Long roleId) {
         service.addRole(studentId,roleId);
     }
 
     private void validateObject(Domain obj, Class<?> clazz) {
         ValidationHelper.isObjectNull(obj, clazz);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ONLY_ADMIN)
     @RequestMapping(
             value  = { "{studentId}/role/{roleId}" },
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Role removed")
     public void removeRole(
             @PathVariable Long studentId,
             @PathVariable Long roleId) {
         service.removeRole(studentId,roleId);
     }
 
     @Override
     @PreAuthorize(SecurityConstants.ONLY_ADMIN)
     @RequestMapping(
             value  = { "{studentId}/roles" },
             method = RequestMethod.DELETE)
     @ResponseStatus(
             value  = HttpStatus.OK,
             reason = "Roles removed from student")
     public void removeAllRolesFromStudent(@PathVariable Long studentId) {
         service.removeAllRoles(studentId);
     }
 
     
     @RequestMapping(
             value  = { "{studentId}/{roleName}" },
             method = RequestMethod.GET)
     @ResponseStatus(value = HttpStatus.OK, reason = "Student has role")
     public void isStudentInRole(
             @PathVariable Long studentId,
             @PathVariable String roleName) {
         Student stud = service.getStudentWithRoles(studentId);
         validateObject(stud, Student.class);
 
         boolean hasRole = false;
 
         for (Role r : stud.getRoles()) {
             logger.debug(r.getRoleName());
             if (r.getRoleName().equals(roleName)) {
                 hasRole = true;
                 break;
             }
         }
 
         if (!hasRole) {
             throw new NotInCollectionException(
                     "Student does not have the role");
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
            value  = { "{studentId}/locker/{lockerId}}" },
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Locker add to student")
     public void addLocker(
             @PathVariable Long studentId,
             @PathVariable Long lockerId) {
         service.addLocker(studentId, lockerId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = { "{studentId}/locker/{lockerId}" },
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Locker add to student")
     public void removeLocker(
             @PathVariable Long studentId,
             @PathVariable Long lockerId) {
         service.removeLocker(studentId, lockerId);
     }
 }

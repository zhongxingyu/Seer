 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.aop.ApiEvent;
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.StudentController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Committee;
 import no.niths.domain.Course;
 import no.niths.domain.Domain;
 import no.niths.domain.Feed;
 import no.niths.domain.Student;
 import no.niths.domain.battlestation.Loan;
 import no.niths.domain.security.Role;
 import no.niths.services.battlestation.interfaces.LoanService;
 import no.niths.services.interfaces.CommitteeService;
 import no.niths.services.interfaces.CourseService;
 import no.niths.services.interfaces.FeedService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoleService;
 import no.niths.services.interfaces.StudentService;
 
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
 @RequestMapping(AppConstants.STUDENTS)
 public class StudentControllerImpl extends AbstractRESTControllerImpl<Student>
 		implements StudentController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(StudentControllerImpl.class);
 
 	private StudentList studentList = new StudentList();
 
 	@Autowired
 	private StudentService service;
 
     @Autowired
 	private CourseService courseService;
 
     @Autowired
 	private CommitteeService committeeService;
 
     @Autowired
 	private FeedService feedService;
 
     @Autowired
 	private LoanService loanService;
     
     @Autowired
     private RoleService roleService;
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
 	@RequestMapping(value = "{id}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public Student getById(@PathVariable Long id) {
 		return super.getById(id);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #domain.id)")
 	public void update(@RequestBody Student domain) {
 		logger.info(domain.getEmail() +" : "+ domain.getId() +" : " + domain.getFirstName()  +" : "+ domain.getLastName() +" : " + domain.getGender());
 		super.update(domain);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@ApiEvent(title="Student crated")
 	public void create(@RequestBody Student domain, HttpServletResponse res) {
 		super.create(domain, res);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 	
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public ArrayList<Student> getAll(Student domain) {
 		return super.getAll(domain);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public ArrayList<Student> getAll(Student domain, @PathVariable int firstResult,
 			@PathVariable int maxResults) {
 		return super.getAll(domain, firstResult, maxResults);
 				
 	}
 	
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = "course", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public List<Student> getStudentsWithNamedCourse(@RequestBody Course course) {
 		String name = course.getName();
 		logger.info(name);
 		renewList(service.getStudentsWithNamedCourse(name));
 		return studentList;
 	}
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/add/course/{courseId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Added")
     public void addCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         Course course = courseService.getById(courseId);
         ValidationHelper.isObjectNull(course, Course.class);
 
         student.getCourses().add(course);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/remove/course/{courseId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Removed")
     public void removeCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         boolean isRemoved = false;
 
         for (int i = 0; i < student.getCourses().size(); i++) {
             if (student.getCourses().get(i).getId() == courseId) {
                 student.getCourses().remove(i);
                 isRemoved = true;
                 break;
             }
         }
 
         if (isRemoved) {
             service.update(student);
         } else {
             logger.debug("Course not found");
             throw new ObjectNotFoundException("Course not found");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/add/committee/{committeeId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Added")
     public void addCommittee(@PathVariable Long studentId, @PathVariable Long committeeId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         Committee committee = committeeService.getById(committeeId);
         ValidationHelper.isObjectNull(committee, Committee.class);
 
         student.getCommittees().add(committee);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/remove/committee/{committeeId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Removed")
     public void removeCommittee(@PathVariable Long studentId, @PathVariable Long committeeId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         boolean isRemoved = false;
 
         for (int i = 0; i < student.getCommittees().size(); i++) {
             if (student.getCommittees().get(i).getId() == committeeId) {
                 student.getCommittees().remove(i);
                 isRemoved = true;
                 break;
             }
         }
 
         if (isRemoved) {
             service.update(student);
         } else {
             logger.debug("Committee not found");
             throw new ObjectNotFoundException("Committee not found");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/add/feed/{feedId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Added")
     public void addFeed(@PathVariable Long studentId, @PathVariable Long feedId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         Feed feed = feedService.getById(feedId);
         ValidationHelper.isObjectNull(feed, Feed.class);
 
         student.getFeeds().add(feed);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/remove/feed/{feedId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Removed")
     public void removeFeed(@PathVariable Long studentId, @PathVariable Long feedId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         boolean isRemoved = false;
 
         for (int i = 0; i < student.getFeeds().size(); i++) {
             if (student.getFeeds().get(i).getId() == feedId) {
                 student.getFeeds().remove(i);
                 isRemoved = true;
                 break;
             }
         }
 
         if (isRemoved) {
             service.update(student);
         } else {
             logger.debug("Game not found");
             throw new ObjectNotFoundException("Game not found");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/add/loan/{loanId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Added")
     public void addLoan(@PathVariable Long studentId, @PathVariable Long loanId) {
        Student student = service.getStudentWithRoles(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         Loan loan = loanService.getById(loanId);
         ValidationHelper.isObjectNull(loan, Loan.class);
 
         student.getLoans().add(loan);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{studentId}/remove/loan/{loanId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Removed")
     public void removeLoan(@PathVariable Long studentId, @PathVariable Long loanId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, Student.class);
 
         boolean isRemoved = false;
 
         for (int i = 0; i < student.getLoans().size(); i++) {
             if (student.getLoans().get(i).getId() == loanId) {
                 student.getLoans().remove(i);
                 isRemoved = true;
                 break;
             }
         }
 
         if (isRemoved) {
             service.update(student);
         } else {
             logger.debug("Loan not found");
             throw new ObjectNotFoundException("Loan not found");
         }
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
 	@RequestMapping(value = { "{studentId}/add/role/{roleId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Role added")
 	public void addRole(@PathVariable Long studentId,@PathVariable Long roleId) {
 		Student stud = service.getStudentWithRoles(studentId);
 		validateObject(stud, Student.class);
 
 		boolean hasRole = false;
 		for (Role r : stud.getRoles()) {
 			if (r.getId() == roleId) {
 				hasRole = true;
 				break;
 			}
 		}
 
 		if (!hasRole) {
 			Role role = roleService.getById(roleId);
 			validateObject(role, Role.class);
 
 			stud.getRoles().add(role);
 			service.update(stud);
 
 			logger.debug("Added role to student: " + role.getRoleName());
 
 		}else{
 			throw new DuplicateEntryCollectionException("Student got the role");
 		}
 		
 	}
 
 	private void validateObject(Domain obj, Class<?> clazz) {
 		ValidationHelper.isObjectNull(obj, clazz);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	@RequestMapping(value = { "{studentId}/remove/role/{roleId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Role removed")
 	public void removeRole(@PathVariable Long studentId,@PathVariable Long roleId) {
 		Student stud = service.getById(studentId);
 		validateObject(stud, Student.class);
 
 		boolean hasRole = false;
 		for (Role r : stud.getRoles()) {
 			if (r.getId() == roleId) {
 				stud.getRoles().remove(r);
 				service.update(stud);
 				logger.debug("Removed role from student: " + r.getRoleName());
 				hasRole = true;
 				break;
 			}
 		}
 		
 		if(!hasRole){
 			throw new ObjectNotFoundException("Student does not have the role");
 		}
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ONLY_ADMIN)
 	@RequestMapping(value = { "{studentId}/remove/roles" }, method = RequestMethod.POST)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Roles removed from student")
 	public void removeAllRolesFromStudent(@PathVariable Long studId) {
 		Student stud = service.getStudentWithRoles(studId);
 		validateObject(stud, Student.class);
 
 		if (!(stud.getRoles().isEmpty())) {
 			stud.getRoles().clear();
 			service.update(stud);
 			logger.debug("All roles removed from student");
 		}else{
 			logger.debug("Student did not have any roles");
 		}
 		
 
 	}
 
 	@RequestMapping(value = { "{studId}/{roleName}" }, method = RequestMethod.GET)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Student has role")
 	public void isStudentInRole(@PathVariable Long studId,
 			@PathVariable String roleName) {
 		Student stud = service.getStudentWithRoles(studId);
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
 			throw new NotInCollectionException("Student does not have the role");
 		}
 		
 	}
 }

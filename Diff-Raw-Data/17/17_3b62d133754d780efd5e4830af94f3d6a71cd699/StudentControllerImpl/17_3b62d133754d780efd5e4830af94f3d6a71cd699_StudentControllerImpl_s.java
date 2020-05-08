 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.StudentController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
	import no.niths.domain.Committee;
 import no.niths.domain.Course;
 import no.niths.domain.Feed;
<<<<<<< Updated upstream
=======
 import no.niths.domain.Game;
>>>>>>> Stashed changes
 import no.niths.domain.Loan;
 import no.niths.domain.Student;
 import no.niths.services.interfaces.CommitteeService;
 import no.niths.services.interfaces.CourseService;
 import no.niths.services.interfaces.FeedService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.LoanService;
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
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
 	@RequestMapping(value = "{id}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public Student getById(@PathVariable Long id) {
 		Student student = super.getById(id);
 		if (student != null) {
 			for (int i = 0; i < student.getCommittees().size(); i++) {
 				student.getCommittees().get(i).setEvents(null);
 				student.getCommittees().get(i).setLeaders(null);
 				student.getCommittees().get(i).setMembers(null);
 			}
 			
 			for (int i = 0; i < student.getCourses().size(); i++) {
 				student.getCourses().get(i).setCourseRepresentatives(null);
 				student.getCourses().get(i).setSubjects(null);
 			}
 
 			if(student.getRepresentativeFor()!=null){
 				student.getRepresentativeFor().setCourseRepresentatives(null);
 				student.getRepresentativeFor().setSubjects(null);
 				
 			}
 			
 			for(Feed f: student.getFeeds()){
 				f.setStudent(null);
 				f.setLocation(null);
 			}
 			for (Loan l : student.getLoans()) {
 			    l.setConsoles(null);
 			    l.setGames(null);
 			    l.setStudent(null);
 			}
 			
 		}
 		return student;
 	}
 	
 	
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #domain.id)")
 	public void update(@RequestBody Student domain) {
 		logger.warn(domain.getEmail() +" : "+ domain.getId() +" : " + domain.getFirstName()  +" : "+ domain.getLastName() +" : " + domain.getGender());
 		super.update(domain);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Student domain) {
 		super.create(domain);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #id)")
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 	
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public ArrayList<Student> getAll(Student domain) {
 		super.getAll(domain);
 		
 		for (int i = 0; i < studentList.size(); i++) {
 			studentList.get(i).setRepresentativeFor(null);
 		}
 		
 		return studentList;
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public ArrayList<Student> getAll(Student domain, @PathVariable int firstResult,
 			@PathVariable int maxResults) {
 		super.getAll(domain, firstResult, maxResults);
 		
 		for (int i = 0; i < studentList.size(); i++) {
 			studentList.get(i).setRepresentativeFor(null);
 		}
 		
 		return studentList;
 				
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
 
 		for (int i = 0; i < studentList.size(); i++) {
 			studentList.get(i).setCommittees(null);
 			studentList.get(i).setCourses(null);
 			studentList.get(i).setRepresentativeFor(null);
 		}
 		return studentList;
 	}
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "add/course/{studentId}/{courseId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Added")
     public void addCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
         Course course = courseService.getById(courseId);
         ValidationHelper.isObjectNull(course, "Course does not exist");
 
         student.getCourses().add(course);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "remove/course/{studentId}/{courseId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Course Removed")
     public void removeCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
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
     @RequestMapping(value = "add/committee/{studentId}/{committeeId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Added")
     public void addCommittee(@PathVariable Long studentId, @PathVariable Long committeeId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
         Committee committee = committeeService.getById(committeeId);
         ValidationHelper.isObjectNull(committee, "Committee does not exist");
 
         student.getCommittees().add(committee);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "remove/committee/{studentId}/{committeeId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Committee Removed")
     public void removeCommittee(@PathVariable Long studentId, @PathVariable Long committeeId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
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
     @RequestMapping(value = "add/feed/{studentId}/{feedId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Added")
     public void addFeed(@PathVariable Long studentId, @PathVariable Long feedId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
         Feed feed = feedService.getById(feedId);
         ValidationHelper.isObjectNull(feed, "Feed does not exist");
 
         student.getFeeds().add(feed);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "remove/feed/{studentId}/{feedId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Feed Removed")
     public void removeFeed(@PathVariable Long studentId, @PathVariable Long feedId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
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
     @RequestMapping(value = "add/loan/{studentId}/{loanId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Added")
     public void addLoan(@PathVariable Long studentId, @PathVariable Long loanId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
         Loan loan = loanService.getById(loanId);
         ValidationHelper.isObjectNull(loan, "Loan does not exist");
 
         student.getLoans().add(loan);
         service.update(student);
         logger.debug("Student updated");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "remove/loan/{studentId}/{loanId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Loan Removed")
     public void removeLoan(@PathVariable Long studentId, @PathVariable Long loanId) {
         Student student = service.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student does not exist");
 
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
 }

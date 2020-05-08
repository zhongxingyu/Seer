 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.CourseController;
 import no.niths.application.rest.lists.CourseList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.SubjectList;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Course;
 import no.niths.domain.Subject;
 import no.niths.services.interfaces.CourseService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.SubjectService;
 
 import org.hibernate.NonUniqueObjectException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 /**
  * Controller for course
  *
  */
 @Controller
 @RequestMapping(AppConstants.COURSES)
 public class CourseControllerImpl extends AbstractRESTControllerImpl<Course> implements CourseController{
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(CourseControllerImpl.class);
 
 	@Autowired
 	private CourseService courseService;
 
 	@Autowired
 	private SubjectService subjectService;
 
 	private CourseList courseList = new CourseList();
 	
 	private SubjectList subjectList = new SubjectList();
 
 	
 	@Override
 	@RequestMapping(method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public ArrayList<Course> getAll(Course domain) {
 		courseList = (CourseList) super.getAll(domain);
     	for (int i = 0; i < courseList.size(); i++){
     		courseList.get(i).setSubjects(null);
     	}
 		return courseList;
 	}
 	/**
 	 * Returns all topics inside a course
 	 * 
 	 * @param id
 	 *            the course id
 	 * @return List with subject
 	 */
 	@RequestMapping(value = "subject/{id}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public List<Subject> getCourseSubjects(@PathVariable Long id) {
 		Course course = courseService.getById(id);
 		ValidationHelper.isObjectNull(course);
 		subjectList.clear();
 		subjectList.addAll(course.getSubjects());
 		subjectList.setData(course.getSubjects());
 		ValidationHelper.isListEmpty(subjectList);
 		return subjectList;
 	}
 
 	/**
 	 * Adds a topic to a course
 	 * 
 	 * @param courseId
 	 *            the id of the course
 	 * @param subjectId
 	 *            the id of the topic to be added
 	 */
 	@RequestMapping(value = { "{courseId}/{subjectId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Subject added to course")
 	public void addSubjectToCourse(@PathVariable Long courseId,
 			@PathVariable Long subjectId) {
 
 		Course course = courseService.getById(courseId);
 		ValidationHelper.isObjectNull(course);
 
 		Subject subject = subjectService.getById(subjectId);
 		ValidationHelper.isObjectNull(subject);
 
 		course.getSubjects().add(subject);
 		courseService.update(course);
 	}
 
 
 	/**
 	 * Catches constraint violation exceptions
 	 * Ex: Topic already added to course
 	 */
 	@ExceptionHandler(NonUniqueObjectException.class)
 	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Already added")
 	public void notUniqueObject() {
 	}
 
 	@Override
 	public GenericService<Course> getService() {
 		return courseService;
 	}
 
 	@Override
 	public ListAdapter<Course> getList() {
 		return courseList;
 	}
 }

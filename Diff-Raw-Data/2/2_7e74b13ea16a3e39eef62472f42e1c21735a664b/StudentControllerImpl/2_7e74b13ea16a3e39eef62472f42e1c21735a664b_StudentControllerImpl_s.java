 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.interfaces.StudentController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Course;
 import no.niths.domain.Student;
 import no.niths.services.interfaces.GenericService;
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
 				student.getCourses().get(i).setSubjects(null);
 			}
 		}
 		return student;
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #domain.id)")
 	public void update(@RequestBody Student domain) {
 		super.update(domain);
 	}
 	
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Student domain) {
 		super.create(domain);
 	}
 	
 	@Override
	@PreAuthorize(SecurityConstants.ADMIN_AND_SR + " or (hasRole('ROLE_STUDENT') and principal.studentId == #domain.id)")
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 	
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public ArrayList<Student> getAll(Student domain) {
 		studentList = (StudentList) super.getAll(domain);
 		for (int i = 0; i < studentList.size(); i++) {
 			studentList.get(i).setCommittees(null);
 			studentList.get(i).setCourses(null);
 			//studentList.get(i).setFadderGroup(null);
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
 			//studentList.get(i).setFadderGroup(null);
 		}
 		return studentList;
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

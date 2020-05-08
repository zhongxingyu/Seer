 package no.niths.application.rest;
 
 import java.util.ArrayList;
 
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.interfaces.SubjectController;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.SubjectList;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Student;
 import no.niths.domain.Subject;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.StudentService;
 import no.niths.services.interfaces.SubjectService;
 
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
 /**
  * Controller for subjects
  *
  */
 @Controller
 @RequestMapping(AppConstants.SUBJECTS)
 public class SubjectControllerImpl extends AbstractRESTControllerImpl<Subject> implements SubjectController{
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(SubjectControllerImpl.class);
 
 	@Autowired
 	private SubjectService service;
 	
 	@Autowired
 	private StudentService studentService;
 
 	private SubjectList subjectList = new SubjectList();
 	
 	/**
 	 * {@inheritDoc}
 	 */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value ="addTutor/{subjectId}/{studentId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Tutor added to subject")
     public void addTutor(@PathVariable Long subjectId, @PathVariable Long studentId) {
         Subject subject = service.getById(subjectId);
         ValidationHelper.isObjectNull(subject, "Subject not found");
         Student student = studentService.getById(studentId);
         ValidationHelper.isObjectNull(student, "Student not found");
         
         subject.getTutors().add(student);
        service.updateForTutor(subject);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
     		value ="removeTutor/{subjectId}/{studentId}",
     		method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Tutor removed to subject")
     public void removeTutor(@PathVariable Long subjectId, @PathVariable Long studentId) {
     	Subject subject = service.getById(subjectId);
     	ValidationHelper.isObjectNull(subject, "Subject not found");
     	Student student = studentService.getById(studentId);
     	ValidationHelper.isObjectNull(student, "Student not found");
     	
     	if(subject.getTutors().remove(student)){
    		service.updateForTutor(subject);    		
     	}else{
     		throw new NotInCollectionException("Student is not a tutor");
     	}
     		
     }
 
 	@Override
 	public Subject getById(@PathVariable Long id) {
 		Subject s = super.getById(id);
 		for (Student stud: s.getTutors()){
 			stud.setCommittees(null);
 			stud.setCourses(null);
 			stud.setFeeds(null);
 		}
 		if(s.getRoom() != null){
 			s.getRoom().setAccessFields(null);
 		}
 		return s;
 	}
 
 	@Override
 	@RequestMapping(method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public ArrayList<Subject> getAll(Subject domain) {
 		SubjectList list = (SubjectList) super.getAll(domain);
 		for (int i = 0; i< list.size(); i++){
 			list.get(i).setTutors(null);
 			list.get(i).setRoom(null);
 		}
 		return list;
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Subject domain) {
 		super.create(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void update(@RequestBody Subject domain) {
 		super.update(domain);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Subject> getService() {
 		return service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Subject> getList() {
 		return subjectList;
 	}
 
 
 }

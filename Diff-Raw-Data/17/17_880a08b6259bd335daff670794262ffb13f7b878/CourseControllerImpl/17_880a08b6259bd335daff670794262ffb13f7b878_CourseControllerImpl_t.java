 package no.niths.application.rest.school;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.AbstractRESTControllerImpl;
 import no.niths.application.rest.lists.CourseList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.school.interfaces.CourseController;
 import no.niths.common.AppNames;
 import no.niths.common.SecurityConstants;
 import no.niths.domain.school.Course;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.school.interfaces.CourseService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Controller for course
  * 
  */
 @Controller
 @RequestMapping(AppNames.COURSES)
 public class CourseControllerImpl extends AbstractRESTControllerImpl<Course>
         implements CourseController {
 
 	@Autowired
     private CourseService courseService;
 
     private CourseList courseList = new CourseList();
 
     @Override
     public Course getById(@PathVariable Long id) {
         Course course = super.getById(id);
         
         return course;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(
             value  = "{courseId}/representative/{studentId}",
             method = RequestMethod.POST)
     @ResponseStatus(
             value  = HttpStatus.OK,
             reason = "Representative added to course")
     public void addRepresentative(
             @PathVariable Long courseId,
             @PathVariable Long studentId) {
         courseService.addRepresentative(courseId, studentId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(
             value  = "{courseId}/representative/{studentId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(
             value  = HttpStatus.OK,
             reason = "Representative removed from course")
     public void removeRepresentative(
             @PathVariable Long courseId,
             @PathVariable Long studentId) {
         courseService.removeRepresentative(courseId, studentId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     public void create(@RequestBody Course domain, HttpServletResponse res) {
         super.create(domain, res);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     public void update(@RequestBody Course domain) {
         super.update(domain);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     public void delete(@PathVariable long id) {
         super.delete(id);
     }
 
     /**
      * Adds a topic to a course
      * 
      * @param courseId
      *            the id of the course
      * @param subjectId
      *            the id of the topic to be added
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value = "{courseId}/subject/{subjectId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Subject added to course")
     public void addSubject(
             @PathVariable Long courseId,
             @PathVariable Long subjectId) {
         courseService.addSubject(courseId, subjectId);
     }
 
     @Override
     @RequestMapping(
             value  = "{courseId}/subject/{subjectId}",
             method = RequestMethod.DELETE)
    @ResponseStatus(
            value  = HttpStatus.OK,
            reason = "Subject removed from course")
     public void removeSubject(
             @PathVariable Long courseId,
             @PathVariable Long subjectId) {
         courseService.removeSubject(courseId, subjectId);
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

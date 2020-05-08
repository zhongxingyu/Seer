 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.interfaces.CommitteeController;
 import no.niths.application.rest.lists.CommitteeList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Committee;
 import no.niths.domain.Event;
 import no.niths.domain.Student;
 import no.niths.services.interfaces.CommitteeService;
 import no.niths.services.interfaces.EventsService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * This class provides CRUD actions in RESTstyle <br />
  * 
  * Mapping :<br />
  * host:port/committees<br />
  * 
  * Headers : <br />
  * Accept:application/json<br />
  * Content-Type:appilcation/json <br />
  * Accept:application/xml<br />
  * Content-Type:appilcation/xml<br />
  * 
  */
 @Controller
 @RequestMapping(AppConstants.COMMITTEES)
 public class CommitteeControllerImpl extends
         AbstractRESTControllerImpl<Committee> implements CommitteeController {
 
     @Autowired
     private CommitteeService committeeService;
     
     @Autowired
     private EventsService eventService;
 
     @Autowired
     private StudentService studentService;
 
     private Logger logger = org.slf4j.LoggerFactory
             .getLogger(CommitteeControllerImpl.class);
 
     private CommitteeList committeeList = new CommitteeList();
 
     @Override
     @RequestMapping(
             value = "{id}",
             method = RequestMethod.GET,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public Committee getById(@PathVariable Long id) {
         logger.debug(id+"");
         Committee c = super.getById(id);
 
         if(c != null){
             List<Student> leaders = c.getLeaders();
             for (int i = 0; i < leaders.size(); i++){
                 leaders.get(i).setCommittees(null);
                 leaders.get(i).setCourses(null);
                 //leaders.get(i).setFadderGroup(null);
             }
             if(c.getEvents().isEmpty()){
                 c.setEvents(null);
             }
         }
         return c;
     }
 
     @Override
     @RequestMapping(
             method = RequestMethod.GET,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public ArrayList<Committee> getAll(Committee domain) {
         committeeList = (CommitteeList) super.getAll(domain);
         for (int i = 0; i < committeeList.size(); i++) {
             committeeList.get(i).setEvents(null);
             committeeList.get(i).setLeaders(null);
         }
         return committeeList;
     }
 
     /**
      * Adds a leader to a committee
      * 
      * @param committeeId
      *            The id of the committee
      * @param studentId
      *            The id of the student to add as leader
      */
     @RequestMapping(
             value ="leaders/{committeeId}/{studentId}",
             method = RequestMethod.POST)
     @ResponseStatus(value = HttpStatus.OK, reason = "Leader added to committee")
    public void addLeader(@PathVariable Long committeeId,
             @PathVariable Long studentId) {
         Committee committee = committeeService.getById(committeeId);
         ValidationHelper.isObjectNull(committee);
         Student student = studentService.getById(studentId);
         ValidationHelper.isObjectNull(student);
         committee.getLeaders().add(student);
         committeeService.update(committee);
     }
 
     /**
      * Removes a leader from a committee
      * 
      * @param committeeId
      *            The id of the committee to remove leader from
      * @param studentId
      *            The id of the student to remove
      */
     @RequestMapping(
             value = "leaders/{committeeId}/{studentId}",
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
     public void removeLeader(@PathVariable Long committeeId,
             @PathVariable Long studentId) {
         Committee committee = committeeService.getById(committeeId);
 
         ValidationHelper.isObjectNull(committee);
 
         Student studentLeader = studentService.getById(studentId);
         ValidationHelper.isObjectNull(studentLeader);
         ValidationHelper.isStudentLeaderInCommittee(committee, studentLeader);
         committee.getLeaders().remove(studentLeader);
         committeeService.update(committee);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(
             value = "addEvent/{committeeId}/{eventId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Event added")
     public void addEvent(
             @PathVariable Long committeeId,
             @PathVariable Long eventId) {
         Committee committee = committeeService.getById(committeeId);
         ValidationHelper.isObjectNull(committee);
         Event event = eventService.getById(eventId);
         ValidationHelper.isObjectNull(event);
         committee.getEvents().add(event);
         committeeService.update(committee);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(
             value = "removeEvent/{committeeId}/{eventId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Event removed")
     public void removeEvent(
             @PathVariable Long committeeId,
             @PathVariable Long eventId) {
         Committee committee = committeeService.getById(committeeId);
         ValidationHelper.isObjectNull(committee);
         Event event = eventService.getById(eventId);
         ValidationHelper.isObjectNull(event);
 
         if(committee.getEvents().contains(event)){
             committee.getEvents().remove(event);
             committeeService.update(committee);
         }
     }
 
     /**
      * Catches constraint violation exceptions Ex: Leader already added to
      * committee
      */
     @ExceptionHandler(DataIntegrityViolationException.class)
     @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Already added")
     public void notUniqueObject() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GenericService<Committee> getService() {
         return committeeService;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ListAdapter<Committee> getList() {
         return committeeList;
     }
 }

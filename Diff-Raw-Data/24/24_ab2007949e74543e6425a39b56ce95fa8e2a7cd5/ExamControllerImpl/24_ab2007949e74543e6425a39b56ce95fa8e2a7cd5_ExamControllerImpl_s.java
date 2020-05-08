 package no.niths.application.rest;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.ExamController;
 import no.niths.application.rest.lists.ExamList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Exam;
 import no.niths.domain.Subject;
 import no.niths.domain.location.Room;
 import no.niths.services.interfaces.ExamService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.RoomService;
 import no.niths.services.interfaces.SubjectService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Controller for exams
  */
 @Controller
 @RequestMapping(AppConstants.EXAMS)
 public class ExamControllerImpl extends AbstractRESTControllerImpl<Exam> implements ExamController {
 
     private static final Logger logger = LoggerFactory
             .getLogger(ExamControllerImpl.class);
 
     @Autowired
     private ExamService examService;
     
     @Autowired
     private RoomService roomService;
     
     @Autowired
     private SubjectService subjectService;
 
     private ExamList examList = new ExamList();
 
     @Override
     @RequestMapping(value = "add/room/{examId}/{roomId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Room Added")
     public void addRoom(@PathVariable Long examId, @PathVariable Long roomId) {
         Exam exam = examService.getById(examId);
         ValidationHelper.isObjectNull(exam, "Exam does not exist");
 
         Room room = roomService.getById(roomId);
         ValidationHelper.isObjectNull(room, "Room does not exist");
         
         exam.getRooms().add(room);
         examService.update(exam);
         logger.debug("Exam updated");
     }
 
     @Override
     @RequestMapping(value = "remove/room/{examId}/{roomId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Room Removed")
     public void removeRoom(@PathVariable Long examId, @PathVariable Long roomId) {
         Exam exam = examService.getById(examId);
         ValidationHelper.isObjectNull(exam, "Exam does not exist");
         
         boolean isRemoved = false;
         
         for (int i = 0; i < exam.getRooms().size(); i++) {
             if (exam.getRooms().get(i).getId() == roomId) {
                 exam.getRooms().remove(i);
                 isRemoved = true;
                 break;
             }
         }
 
         if (isRemoved) {
             examService.update(exam);
         } else {
             logger.debug("Room not found");
             throw new ObjectNotFoundException("Room not found");
         }
     }
 
     @Override
     @RequestMapping(value = "add/subject/{examId}/{subjectId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Subject Added")
     public void addSubject(@PathVariable Long examId, @PathVariable Long subjectId) {
         Exam exam = examService.getById(examId);
         ValidationHelper.isObjectNull(exam, "Exam does not exist");
 
         Subject subject = subjectService.getById(subjectId);
         ValidationHelper.isObjectNull(subject, "Subject does not exist");
 
         exam.setSubject(subject);
         examService.update(exam);
         logger.debug("Exam updated");
     }
 
     @Override
     @RequestMapping(value = "remove/subject/{examId}/{subjectId}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Subject Removed")
     public void removeSubject(@PathVariable Long examId, @PathVariable Long subjectId) {
         Exam exam = examService.getById(examId);
         ValidationHelper.isObjectNull(exam, "Exam does not exist");
 
         boolean isRemoved = false;
 
         if (exam.getSubject() != null && exam.getSubject().getId() == subjectId) {
             exam.setSubject(null);
             isRemoved = true;
         }
 
         if (isRemoved) {
             examService.update(exam);
         } else {
             logger.debug("Subject not found");
             throw new ObjectNotFoundException("Subject not found");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public GenericService<Exam> getService() {
         return examService;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ListAdapter<Exam> getList() {
         return examList;
     }
 }

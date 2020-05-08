 package org.sukrupa.event;
 
 import com.google.common.collect.Sets;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.sukrupa.platform.DoNotRemove;
 import org.sukrupa.student.Student;
 import org.sukrupa.student.StudentRepository;
 
 import java.util.List;
 import java.util.Set;
 
 @Component
 public class EventService {
 
     private EventRepository eventRepository;
     private StudentRepository studentRepository;
 
     @DoNotRemove
     EventService() {
     }
 
     @Autowired
     public EventService(EventRepository eventRepository, StudentRepository studentRepository) {
         this.eventRepository = eventRepository;
         this.studentRepository = studentRepository;
     }
 
     @Transactional
     public void save(Event event, String... studentIdsOfAttendees) {
         event.addAttendees(studentRepository.load(studentIdsOfAttendees));
         eventRepository.save(event);
     }
 
     public Event getEvent(int eventId) {
         return eventRepository.load(eventId);
     }
 
     public List<Event> list() {
         return eventRepository.list();
     }
 
 	public Set<String> validateStudentIdsOfAttendees(Set<String> studentIdsOfAttendees) {
 		Set<Student> students = studentRepository.load(studentIdsOfAttendees.toArray(new String[]{}));
 		Set<String> loadedStudentsIds = Sets.newHashSet();
 
 		for (Student student : students) {
 			loadedStudentsIds.add(student.getStudentId());
 		}
 		return Sets.difference(studentIdsOfAttendees, loadedStudentsIds);
 	}
 	
 }

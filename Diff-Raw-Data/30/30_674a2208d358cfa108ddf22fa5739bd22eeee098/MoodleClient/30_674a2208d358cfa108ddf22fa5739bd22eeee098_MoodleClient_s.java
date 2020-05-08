 package de.thm.arsnova.connector.moodle.client;
 
import org.springframework.stereotype.Service;

 import de.thm.arsnova.connector.moodle.model.Courses;
 import de.thm.arsnova.connector.moodle.model.Membership;
 
@Service
 public interface MoodleClient {
 	Membership isMember(String username, String courseid);
 	Courses getCourses(String username);
}

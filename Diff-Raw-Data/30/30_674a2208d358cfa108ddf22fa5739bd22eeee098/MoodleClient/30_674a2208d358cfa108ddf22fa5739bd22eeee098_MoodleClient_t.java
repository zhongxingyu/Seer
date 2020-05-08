 package de.thm.arsnova.connector.moodle.client;
 
 import de.thm.arsnova.connector.moodle.model.Courses;
 import de.thm.arsnova.connector.moodle.model.Membership;
 
 public interface MoodleClient {
 	Membership isMember(String username, String courseid);
 	Courses getCourses(String username);
}

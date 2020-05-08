 package de.thm.arsnova.connector.moodle.client;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.client.RestTemplate;
 
 import de.thm.arsnova.connector.moodle.model.Courses;
 import de.thm.arsnova.connector.moodle.model.Membership;
 
 public class MoodleClientImpl implements MoodleClient {
 	private final RestTemplate restTemplate = new RestTemplate();
 
	private static final String ISMEMBER_URI = "/{username}/membership/{courseid}";
	private static final String GETCOURSES_URI = "/{username}/courses";
 
 	private String uriHostPart;
 
 	@Autowired
 	public void setServiceLocation(String serviceLocation) {
 		uriHostPart = serviceLocation;
 	}
 
 	@Override
 	public Membership isMember(String username, String courseid) {
		return restTemplate.getForObject(uriHostPart + ISMEMBER_URI, Membership.class, username, courseid);
 	}
 
 	@Override
 	public Courses getCourses(String username) {
		return restTemplate.getForObject(uriHostPart + GETCOURSES_URI, Courses.class, username);
 	}
 }

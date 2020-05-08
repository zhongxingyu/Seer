 package de.thm.arsnova.connector.moodle.client;
 
 import java.nio.charset.Charset;
 
 import org.apache.commons.codec.binary.Base64;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.RestTemplate;
 
 import de.thm.arsnova.connector.moodle.model.Courses;
 import de.thm.arsnova.connector.moodle.model.Membership;
 
 public class MoodleClientImpl implements MoodleClient {
 	private final RestTemplate restTemplate = new RestTemplate();
 
 	private static final String ISMEMBER_URI = "/{username}/membership/{courseid}";
 	private static final String GETCOURSES_URI = "/{username}/courses";
 	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
 
 	private String uriHostPart;
	private String httpUsername;
	private String httpPassword;
 
 	@Autowired
 	public void setServiceLocation(String serviceLocation) {
 		uriHostPart = serviceLocation;
 	}
 
 	@Autowired
	public void setUsername(String username) {
		httpUsername = username;
 	}
 
 	@Autowired
	public void setPassword(String password) {
		httpPassword = password;
 	}
 
 	private HttpEntity<Membership> createMembershipEntity() {
 		return new HttpEntity<Membership>(getAuthorizationHeader());
 	}
 
 	private HttpEntity<Courses> createCoursesEntity() {
 		return new HttpEntity<Courses>(getAuthorizationHeader());
 	}
 
 	private HttpHeaders getAuthorizationHeader() {
 		HttpHeaders httpHeaders = new HttpHeaders();
		String authorisation = httpUsername + ":" + httpPassword;
 		httpHeaders.add(
 				"Authorization",
 				"Basic " + Base64.encodeBase64String(authorisation.getBytes(UTF8_CHARSET))
 		);
 		return httpHeaders;
 	}
 
 	private String buildRequestUri(String relativeUri) {
 		return uriHostPart + relativeUri;
 	}
 
 	@Override
 	public Membership isMember(String username, String courseid) {
 		ResponseEntity<Membership> response = restTemplate.exchange(
 				buildRequestUri(ISMEMBER_URI),
 				HttpMethod.GET,
 				createMembershipEntity(),
 				Membership.class,
 				username,
 				courseid
 		);
 		return response.getBody();
 	}
 
 	@Override
 	public Courses getCourses(String username) {
 		ResponseEntity<Courses> response = restTemplate.exchange(
 				buildRequestUri(GETCOURSES_URI),
 				HttpMethod.GET,
 				createCoursesEntity(),
 				Courses.class,
 				username
 		);
 		return response.getBody();
 	}
 }

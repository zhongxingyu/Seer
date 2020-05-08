 package uk.ac.cam.dashboard.forms;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.FormParam;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
 import uk.ac.cam.dashboard.models.Notification;
 import uk.ac.cam.dashboard.models.NotificationUser;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.Mail;
 import uk.ac.cam.dashboard.util.Strings;
 import uk.ac.cam.dashboard.util.Util;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 public class CreateNotificationForm {
 	@FormParam("message") String message;
 	@FormParam("section") String section;
 	@FormParam("link") String link;
 	@FormParam("users") String users;
 	@FormParam("foreignId") String foreignId;
 	
 	// Logger
 	private static Logger log = LoggerFactory.getLogger(CreateNotificationForm.class);
 	
 	public int handle() {
 		
 		Session session = HibernateUtil.getInstance().getSession();
 		
 		Notification notification = new Notification(message, section, "/" + section + "/" + link, foreignId);
 		
 		session.save(notification);
 		
 		// Create notificationUser objects
 		Set<User> userList = new HashSet<User>();
 		if(!users.equals("")){			
 			String[] crsids = users.split(",");
 			for(String u : crsids){
				log.info("Notifying: {}",u);
 				User user = User.registerUser(u);
 				if (u != null && user != null) {
 					NotificationUser nUser = new NotificationUser(user, notification);
 					session.save(nUser);
 					userList.add(user);
 				} else {
 					log.error("Could not push notification to user with crsid: " + u);
 				}
				log.info("Done");
 			}		
 		}
 		
 		Mail.sendNotificationEmail(notification.getMessage(), userList, notification.getSection());
 		
 		return notification.getId();
 				
 	}
 	
 	public ImmutableMap<String, List<String>> validate() {
 		ArrayListMultimap<String, String> errors = ArrayListMultimap.create();
 		
 		// Message
 		if (message == null || message.equals("")) {
 			errors.put("message", Strings.NOTIFICATION_NO_MESSAGE);
 		} else if (message.length() >= 255) {
 			errors.put("message", Strings.NOTIFICATION_MESSAGE_LENGTH);
 		}
 
 		// Section
 
 		if (section == null || section.equals("")) {
 			errors.put("section", Strings.NOTIFICATION_NO_SECTION);
 		} else if (!Arrays.asList(Notification.VALID_SECTIONS).contains(section)) {
 			errors.put("section", Strings.NOTIFICATION_INVALID_SECTION);
 		}
 		
 		// Link
 		if (link == null || link.equals("")) {
 			errors.put("link", Strings.NOTIFICATION_NO_LINK);
 		}
 		
 		// Users
 		if (users == null || users.equals("")) {
 			errors.put("users", Strings.NOTIFICATION_NO_USERS);
 		} else if (users.split(",").length > 50) {
 			errors.put("users", Strings.NOTIFICATION_USERS_MAX);
 		}
 		
 		// Foreign id
 		if (foreignId == null) {
 			foreignId = "none";
 		}
 
 		return Util.multimapToImmutableMap(errors);
 	}
 	
 	public ImmutableMap<String, ?> toMap() {
 		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
 		
 		String localMessage = (message == null ? "" : message);
 		builder.put("message", localMessage);
 		
 		String localSection = (section == null ? "" : section);
 		builder.put("section", localSection);
 		
 		String localLink = (link == null ? "" : link);
 		builder.put("link", localLink);
 		
 		String localUsers = (users == null ? "" : users);
 		builder.put("users", localUsers);
 		
 		String localForeignId = (foreignId == null ? "" : foreignId);
 		builder.put("foreignId", localForeignId);
 		
 		return builder.build();
 	}
 	
 }

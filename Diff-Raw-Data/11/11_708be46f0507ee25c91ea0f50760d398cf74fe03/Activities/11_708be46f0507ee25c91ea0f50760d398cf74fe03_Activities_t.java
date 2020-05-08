 package controllers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import models.Activity;
 import models.ActivityMessage;
 import models.ActivityStatus;
 import models.User;
 import models.UserFriend;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.db.jpa.JPABase;
 import play.libs.Mail;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Activities extends Controller {
 
 	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	
 	public static void show(Long activityId) {
 		Activity activity = Activity.findById(activityId);
 		User user = User.findUserByUsername(Security.connected());
 		ActivityStatus actStatus = ActivityStatus.find("byUserAndActivity",
 				user, activity).first();
 
 		List<ActivityMessage> actMessages = ActivityMessage.find(
 				"activity = ? order by date desc", activity).fetch();
 
 		render(activity, actStatus, actMessages);
 	}
 
 	public static void updateStatus(Long activityId, int newstatus) {
 		Activity activity = Activity.findById(activityId);
 		User user = User.findUserByUsername(Security.connected());
 		ActivityStatus actStatus = ActivityStatus.find("byUserAndActivity",
 				user, activity).first();
 		actStatus.status = newstatus;
 		actStatus.save();
 		show(activityId);
 	}
 
 	public static void listAll() {
 		User user = User.findUserByUsername(Security.connected());
 		List<ActivityStatus> statuses = ActivityStatus
 				.findAllActivityStatusesForUser(user);
 		List<Activity> activities = new ArrayList<Activity>();
 		for (ActivityStatus activityStatus : statuses) {
 			activities.add(activityStatus.activity);
 		}
 
 		render(activities, user);
 	}
 
 	public static void newActivity() {
 		List<User> friends = new ArrayList<User>();
 		List<UserFriend> UserFriends = UserFriend.find("byUserId",
 				Security.connected()).fetch();
 		for (UserFriend userFriend : UserFriends) {
 			friends.add(User.findUserByUsername(userFriend.friendId));
 		}
 		render(friends);
 	}
 
 	public static void edit(Long activityId) {
 		Activity activity = Activity.findById(activityId);
 		List<User> friends = new ArrayList<User>();
 		List<UserFriend> UserFriends = UserFriend.find("byUserId",
 				Security.connected()).fetch();
 		for (UserFriend userFriend : UserFriends) {
 			friends.add(User.findUserByUsername(userFriend.friendId));
 		}
 
 		List<User> actUsers = new ArrayList<User>();
 		List<ActivityStatus> actStatuses = activity.activityStatuses;
 		for (ActivityStatus activityStatus : actStatuses) {
 			actUsers.add(activityStatus.user);
 		}
 		render(activity, friends, actUsers);
 	}
 
 	public static void update(Long activityId,
 			@Required(message = "Ange ett namn på aktiviteten") String name,
 			@Required(message = "Ange plats") String location,
 			@Required(message = "Ange datum") String date, String information, String[] friends) {
 
 		if (validation.hasErrors()) {
 			render("Activities/edit.html");
 		}
 
 		Date activityDate = parseDate(date);
 
 		Activity activity = Activity.findById(activityId);
 		activity.name = name;
 		activity.location = location;
 		activity.date = activityDate;
 		activity.information = information;
 		activity.save();
 		
 		if (friends != null) {
 			updateParticipants(friends, activity);
 		}
 		
 		show(activityId);
 	}
 
 	private static Date parseDate(String date) {
 		Date activityDate = null;
 		try {
 			activityDate = formatter.parse(date);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return activityDate;
 	}
 
 	private static void updateParticipants(String[] friends, Activity activity) {
 		List<ActivityStatus> actStatuses = activity.activityStatuses;
 		boolean found = false;
 		for (String participant : friends) {
 			for (ActivityStatus status : actStatuses) {
 				if (participant.equals(status.user.email)) {
 					found = true;
 				}
 			}
 			if (!found) {
 				ActivityStatus status = new ActivityStatus(User.findUserByUsername(participant), activity, -1);
 				status.save();
 				createAndSendMail(participant, activity);
 			}
 			found = false;
 		}
 	}
 
 	public void createActivity() {
 		
 	}
 
 	public static void form() {
 		render();
 	}
 
 	public static void save(
 			@Required(message = "Ange ett namn på aktiviteten") String name,
 			@Required(message = "Ange plats") String location,
 			@Required(message = "Ange datum") String date, String information) {
 
 		Date activityDate = parseDate(date);
 		Activity activity = new Activity(name, location, activityDate,
 				information, User.findUserByUsername(Security.connected()));
 
 		if (params.getAll("friends") == null || params.getAll("friends").length == 0) {
 			validation.addError("friends", "Minst en deltagare måste vara vald");
 		}
 		
 		if (validation.hasErrors()) {
 			List<User> friends = getFriends();
 			render("Activities/newActivity.html", friends, activity);
 		}
 		
 		activity.save();
 
		String[] activityFriends = params.getAll("friends");
		for (String friend : activityFriends) {
 			ActivityStatus status = new ActivityStatus(User.findUserByUsername(friend), activity, -1);
 			status.save();
 			createAndSendMail(friend, activity);
 		}
 
 		ActivityStatus status = new ActivityStatus(
 				User.findUserByUsername(Security.connected()), activity, 1);
 		status.save();
 		show(activity.id);
 	}
 
 	public static void createActivityMessage(Long activityId, String message) {
 		Activity activity = Activity.findById(activityId);
 		User user = User.findUserByUsername(Security.connected());
 		ActivityMessage actMessage = new ActivityMessage(message, new Date(),
 				activity, user);
 		actMessage.save();
 		show(activityId);
 	}
 	
 	private static List<User> getFriends() {
 		List<User> friends = new ArrayList<User>();
 		List<UserFriend> UserFriends = UserFriend.find("byUserId",
 				Security.connected()).fetch();
 		for (UserFriend userFriend : UserFriends) {
 			friends.add(User.findUserByUsername(userFriend.friendId));
 		}
 		return friends;
 	}
 
 	private static void createAndSendMail(String emailAddress, Activity activity) {
 		try {
 			SimpleEmail email = new SimpleEmail();
			email.setCharset("UTF-8");
 			email.setFrom("info@klarlistan.nu");
 			email.addTo(emailAddress);
 			email.setSubject("Du har blivit inbjuden till en aktivitet");
 			email.setMsg("Meddelande från klarlistan\n\nDu har blivit inbjuden till:\n\n" + 
 			"Vad: " + activity.name +"\n" +
 			"Var: " + activity.location + "\n" +
 			"När: " + formatter.format(activity.date) + "\n\n" +
 					"Klicka på länken www.klarlistan.nu/activities/show?activityId=" + activity.id + " för att ta tacka ja eller nej\n\n" +
 							"Hälsningar, Klarlistan");
 			Mail.send(email);
 		} catch (EmailException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }

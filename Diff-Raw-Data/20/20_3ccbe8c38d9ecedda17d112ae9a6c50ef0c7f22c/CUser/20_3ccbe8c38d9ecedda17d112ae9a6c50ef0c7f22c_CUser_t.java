 package controllers;
 
 import java.io.File;
 import java.text.ParseException;
 
 import models.Answer;
 import models.Entry;
 import models.Notification;
 import models.Question;
 import models.User;
 import models.Vote;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.mvc.With;
 
 /**
  * The controller for all routes that concern the {@link User}
  * 
  * @author Group3
  * 
  */
 @With(Secure.class)
 public class CUser extends BaseController {
 
 	/**
 	 * Deletes the {@link User} and all it's {@link Question}' {@link Answer}'s
 	 * {@link Vote}'s.
 	 * 
 	 * Instead of deleting all {@link Entry}'s of a {@link User}, these entries
 	 * can optionally be kept in anonymized form by setting their owners to
 	 * <code>null</code> first.
 	 * 
 	 * @param anonymize
 	 *            whether to anonymize or just plain delete the user's entries
 	 * @throws Throwable
 	 */
 	public static void deleteUser(boolean anonymize)
 			throws Throwable {
 		User user = Session.user();
 		if (anonymize)
 			user.anonymize(true);
 		else
 			Cache.delete("index.questions");
 		user.delete();
 		flash.success("secure.userdeletedflash");
 		Secure.logout();
 		Application.index(0);
 	}
 
 	/**
 	 * Save profile the profile a {@link User}.
 	 * 
 	 * @param name
 	 *            the name of the {@link User}.
 	 * @param email
 	 *            the email of the {@link User}.
 	 * @param fullname
 	 *            the fullname of the {@link User}.
 	 * @param birthday
 	 *            the birthday of the {@link User}.
 	 * @param website
 	 *            the website of the {@link User}.
 	 * @param profession
 	 *            the profession of the{@link User}.
 	 * @param employer
 	 *            the employer of the{@link User}.
 	 * @param biography
 	 *            the biography of the {@link User}.
 	 * @param oldPassword
 	 *            the old password of the {@link User}.
 	 * @param newPassword
 	 *            the new password of the {@link User}.
 	 * @throws ParseException
 	 * 
 	 */
 	public static void saveProfile(String name, String email, String fullname,
 			String birthday, String website, String profession,
 			String employer, String biography, String oldPassword,
 			String newPassword) throws ParseException {
 		User user = Database.users().get(name);
 		if (!Application.userCanEditProfile(user)) {
 			flash.error("secure.editprofileerror");
 			Application.showprofile(user.getName());
 		}
 		if (email != null) {
 			user.setEmail(email);
 		}
 		if (fullname != null) {
 			user.setFullname(fullname);
 		}
 		if (birthday != null) {
 			user.setDateOfBirth(birthday);
 		}
 		if (website != null) {
 			user.setWebsite(website);
 		}
 		if (profession != null) {
 			user.setProfession(profession);
 		}
 		if (employer != null) {
 			user.setEmployer(employer);
 		}
 		if (biography != null) {
 			user.setBiography(biography);
 		}
 		if (!newPassword.isEmpty()) {
 			if (user.checkPW(oldPassword)) {
 				user.setSHA1Password(newPassword);
 			} else if (!user.checkPW(oldPassword)) {
 				flash.error("secure.passwordChangeError");
 			}
 		}
 		if (!oldPassword.isEmpty() && newPassword.isEmpty()) {
 			flash.error("secure.passwordNewFieldEmptyError");
 		}
 
 		flash.success("secure.editprofileflash");
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Follow notification.
 	 * 
 	 * @param id
 	 *            the id of the notification.
 	 */
 	public static void followNotification(int id) {
 		User user = Session.user();
 		Notification notification = user.getNotification(id);
 		if (notification != null) {
 			notification.unsetNew();
 		}
 		if (notification != null) {
 			if (notification.getAbout() instanceof Answer) {
 				Application.question(((Answer) notification.getAbout())
 						.getQuestion().id());
 			} else if (notification.getAbout() instanceof Question) {
 				Application.question(((Question) notification.getAbout()).id());
 			}
 		} else if (!redirectToCallingPage()) {
 			Application.notifications(0);
 		}
 	}
 
 	/**
 	 * Clear new notifications. Notifications will no longer appear as new.
 	 */
 	public static void clearNewNotifications() {
 		User user = Session.user();
 		for (Notification n : user.getNewNotifications()) {
 			n.unsetNew();
 		}
 		flash.success("secure.notificationsmarkedasreadflash");
 
 		if (!redirectToCallingPage()) {
 			Application.index(0);
 		}
 	}
 
 	/**
 	 * Delete a notification.
 	 * 
 	 * @param id
 	 *            the id of the notification to be deleted.
 	 */
 	public static void deleteNotification(int id) {
 		User user = Session.user();
 		Notification n = user.getNotification(id);
 		if (n != null) {
 			n.unregister();
 			flash.success("secure.deletenotificationflash");
 		}
 
 		if (!redirectToCallingPage()) {
 			Application.index(0);
 		}
 	}
 
 	/**
 	 * Block a {@link User}.
 	 * 
 	 * @param username
 	 *            the username of the {@link User} to be unblocked.
 	 * @param reason
 	 *            the reason the {@link User} is being blocked.
 	 */
 	public static void blockUser(String username, String reason) {
 		User user = Database.users().get(username);
 		User mod = Session.user();
 		if (mod.isModerator() && mod != user) {
 			if (reason.equals("")) {
 				reason = "secure.blockreasonerror";
 			}
 			user.block(reason);
 			flash.success("secure.blockuserflash");
 		}
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Unblock a {@link User}.
 	 * 
 	 * @param username
 	 *            the username of the {@link User} to be unblocked.
 	 */
 	public static void unblockUser(String username) {
 		User user = Database.users().get(username);
 		User mod = Session.user();
 		if (mod.isModerator() && mod != user) {
 			user.unblock();
 			flash.success("secure.unlockuserflash");
 		}
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Load an XML database file
 	 * 
 	 * @param xml
 	 *            the XML database file to be loaded. This field is mandatory.
 	 */
 	public static void loadXML(@Required File xml) {
 		if (!Session.user().isModerator()) {
 			Application.index(0);
 		}
 		if (xml == null) {
 			flash.error("secure.xmlselecterror");
 			Application.admin();
 		}
 
 		try {
 			Database.importXML(xml);
 			flash.success("secure.xmlloadflash");
 		} catch (Throwable e) {
 			flash.error("secure.xmlloaderror", e.getMessage());
 			e.printStackTrace();
 			Application.admin();
 		}
 		if (xml != null) {
 			xml.delete();
 		}
 		Application.index(0);
 	}
 
 	/**
 	 * Clear the entire database except for the administrator users.
 	 */
 	public static void clearDB() {
 		if (!Session.user().isModerator()) {
 			flash.error("secure.cleardberror");
 			Application.index(0);
 		}
		Database.clearKeepAdmins();
 		flash.success("secure.cleardbflash");
 		Application.admin();
 	}
 }

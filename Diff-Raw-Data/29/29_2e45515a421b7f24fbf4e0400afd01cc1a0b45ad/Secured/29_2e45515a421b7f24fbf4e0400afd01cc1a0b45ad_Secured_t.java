 package controllers;
 
 import java.io.File;
 import java.text.ParseException;
 
 import models.Answer;
 import models.Comment;
 import models.Notification;
 import models.Question;
 import models.User;
 import models.database.Database;
 import models.database.importers.Importer;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Secured extends Controller {
 	public static void newQuestion(@Required String content, String tags) {
 		if (!Validation.hasErrors()) {
 			User user = Session.get().currentUser();
 			Question question = Database.get().questions().add(user, content);
 			question.setTagString(tags);
 			user.startObserving(question);
 			question.setTagString(tags);
 			flash.success("Good luck for getting a reasonable answer!");
 			Application.question(question.id());
 		} else {
 			flash.error("Please don't ask empty questions.");
 			Application.index(0);
 		}
 	}
 
 	public static void newAnswer(int questionId, @Required String content) {
 		Question question = Database.get().questions().get(questionId);
 		if (!Validation.hasErrors() && question != null) {
 			User thisUser = Session.get().currentUser();
 			if (!question.isLocked()) {
 				question.answer(thisUser, content);
 				flash.success("Thanks for posting an answer.");
 			}
 		} else {
 			flash.error("Please don't give empty answers.");
 		}
 		Application.question(questionId);
 	}
 
 	public static void newCommentQuestion(int questionId,
 			@Required String content) {
 		Question question = Database.get().questions().get(questionId);
 		if (!Validation.hasErrors() && question != null && !question.isLocked()) {
 			User thisUser = Session.get().currentUser();
 			question.comment(thisUser, content);
 			flash
 					.success("May your comment be helpful in clarifying the question!");
 			Application.question(questionId);
 		}
 	}
 
 	public static void newCommentAnswer(int questionId, int answerId,
 			@Required String content) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question != null ? question.getAnswer(answerId) : null;
 		if (!Validation.hasErrors() && answer != null && !question.isLocked()) {
 			answer.comment(Session.get().currentUser(), content);
 			flash
 					.success("May your comment be helpful in clarifying the answer!");
 			Application.question(questionId);
 		}
 	}
 
 	public static void voteQuestionUp(int id) {
 		Question question = Database.get().questions().get(id);
 		if (question != null) {
 			question.voteUp(Session.get().currentUser());
 			flash.success("Your up-vote has been registered.");
 			if (!redirectToCallingPage()) {
 				Application.question(id);
 			}
 		} else {
 			Application.index(0);
 		}
 	}
 
 	public static void voteQuestionDown(int id) {
 		Question question = Database.get().questions().get(id);
 		if (question != null) {
 			question.voteDown(Session.get().currentUser());
 			flash.success("Your down-vote has been registered.");
 			if (!redirectToCallingPage()) {
 				Application.question(id);
 			}
 		} else {
 			Application.index(0);
 		}
 	}
 
 	public static void voteAnswerUp(int question, int id) {
 		Question q = Database.get().questions().get(question);
 		Answer answer = q.getAnswer(id);
 		if (answer != null) {
 			answer.voteUp(Session.get().currentUser());
 			flash.success("Your up-vote has been registered");
 			Application.question(question);
 		} else {
 			Application.index(0);
 		}
 	}
 
 	public static void voteAnswerDown(int question, int id) {
 		Question q = Database.get().questions().get(question);
 		Answer answer = q.getAnswer(id);
 		if (answer != null) {
 			answer.voteDown(Session.get().currentUser());
 			flash.success("Your down-vote has been registered.");
 			Application.question(question);
 		} else {
 			Application.index(0);
 		}
 	}
 
 	public static void deleteQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		flash
 				.success("The question '%s' has been deleted.", question
 						.summary());
 		question.unregister();
 		Application.index(0);
 	}
 
 	public static void deleteAnswer(int questionId, int answerId) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		answer.unregister();
 		flash.success("The answer '%s' has been deleted.", answer.summary());
 		Application.question(questionId);
 	}
 
 	public static void deleteCommentQuestion(int questionId, int commentId) {
 		Question question = Database.get().questions().get(questionId);
 		Comment comment = question.getComment(commentId);
 		question.unregister(comment);
 		flash.success("The comment '%s' has been deleted.", comment.summary());
 		Application.question(questionId);
 	}
 
 	public static void deleteCommentAnswer(int questionId, int answerId,
 			int commentId) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		Comment comment = answer.getComment(commentId);
 		answer.unregister(comment);
 		flash.success("The comment '%s' has been deleted.", comment.summary());
 		Application.question(questionId);
 	}
 
 	public static void deleteUser(String name) throws Throwable {
 		User user = Database.get().users().get(name);
 		if (hasPermissionToDelete(Session.get().currentUser(), user)) {
 			boolean deleteSelf = name.equals(Session.get().currentUser()
 					.getName());
 			user.delete();
 			flash.success("User %s has been deleted.", name);
 			if (deleteSelf) {
 				Secure.logout();
 			}
 		}
 		flash.error("You're not allowed to delete user %s!", name);
 		if (!redirectToCallingPage()) {
			Application.index(0);
 		}
 	}
 
 	public static void anonymizeUser(String name) throws Throwable {
 		User user = Database.get().users().get(name);
 		if (hasPermissionToDelete(Session.get().currentUser(), user)) {
 			user.anonymize(true, false);
 		}
 		deleteUser(name);
 	}
 
 	public static void selectBestAnswer(int questionId, int answerId) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		question.setBestAnswer(answer);
 		flash.success("We're glad that you've been helped!");
 		Application.question(questionId);
 	}
 
 	private static boolean hasPermissionToDelete(User currentUser, User user) {
 		return currentUser.getName().equals(user.getName());
 	}
 
 	private static boolean redirectToCallingPage() {
 		Http.Header referer = request.headers.get("referer");
 		if (referer == null)
 			return false;
 		redirect(referer.value());
 		return true;
 	}
 
 	public static void saveProfile(String name, String email, String fullname,
 			String birthday, String website, String profession,
 			String employer, String biography) throws ParseException {
 		User user = Database.get().users().get(name);
 		if (!Application.mayLoggedInUserEditProfileOf(user)) {
 			flash.error("You're not allowed to edit %s's profile!", name);
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
 		flash.success("Thanks for keeping your profile up-to-date.");
 		Application.showprofile(user.getName());
 	}
 
 	public static void updateTags(int id, String tags) {
 		Question question = Database.get().questions().get(id);
 		User user = Session.get().currentUser();
 		if (question != null && user.canEdit(question)) {
 			flash
 					.success("Thanks for keeping this question's labels up-to-date.");
 			question.setTagString(tags);
 		}
 		Application.question(id);
 	}
 
 	public static void watchQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		User user = Session.get().currentUser();
 		if (question != null) {
 			user.startObserving(question);
 			flash.success("You're now watching this question.");
 		}
 		Application.question(id);
 	}
 
 	public static void unwatchQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		User user = Session.get().currentUser();
 		if (question != null) {
 			user.stopObserving(question);
 			flash.success("You're no longer watching this question.");
 		}
 		Application.question(id);
 	}
 
 	public static void unwatchQuestionFromList(int id) {
 		Question question = Database.get().questions().get(id);
 		User user = Session.get().currentUser();
 		if (question != null) {
 			user.stopObserving(question);
 			flash.success(
 					"You're no longer watching <a href='/question/%d'>%s</a>.",
 					id, question.summary());
 		}
 		Application.notifications(1);
 	}
 
 	public static void followNotification(int id) {
 		User user = Session.get().currentUser();
 		Notification notification = user.getNotification(id);
 		if (notification != null) {
 			notification.unsetNew();
 		}
 		if (notification != null && notification.getAbout() instanceof Answer) {
 			Application.question(((Answer) notification.getAbout())
 					.getQuestion().id());
 		} else if (!redirectToCallingPage()) {
 			Application.notifications(0);
 		}
 	}
 
 	public static void clearNewNotifications() {
 		User user = Session.get().currentUser();
 		for (Notification n : user.getNewNotifications()) {
 			n.unsetNew();
 		}
 		flash.success("All notifications have been marked as read.");
 		Application.notifications(0);
 	}
 
 	public static void deleteNotification(int id) {
 		User user = Session.get().currentUser();
 		Notification n = user.getNotification(id);
 		if (n != null) {
 			n.unregister();
 			flash.success("You've got one notification less to care about.");
 		}
 		Application.notifications(0);
 	}
 
 	public static void blockUser(String username, String reason) {
 		User user = Database.get().users().get(username);
 		User mod = Session.get().currentUser();
 		if (mod.isModerator() && mod != user) {
 			if (reason.equals("")) {
 				reason = "no reason given";
 			}
 			user.block(reason);
 			flash.success("User %s has been blocked (%s).", username, reason);
 		}
 		Application.showprofile(user.getName());
 	}
 
 	public static void unblockUser(String username) {
 		User user = Database.get().users().get(username);
 		User mod = Session.get().currentUser();
 		if (mod.isModerator() && mod != user) {
 			user.unblock();
 			flash.success("User %s has been unblocked.", username);
 		}
 		Application.showprofile(user.getName());
 	}
 
 	public static void lockQuestion(int id) {
 		User user = Session.get().currentUser();
 		if (user.isModerator()) {
 			Question question = Database.get().questions().get(id);
 			question.lock();
 			flash.success("This question has been locked.");
 			Application.question(id);
 		}
 	}
 
 	public static void unlockQuestion(int id) {
 		User user = Session.get().currentUser();
 		if (user.isModerator()) {
 			Question question = Database.get().questions().get(id);
 			question.unlock();
 			flash.success("This question has been unlocked.");
 			Application.question(id);
 		}
 	}
 
 	public static void loadXML(@Required File xml) {
 		if (!Session.get().currentUser().isModerator()) {
			Application.index(0);
 		}
 
 		try {
 			Importer.importXML(xml);
 			flash.success("XML file successfully loaded to the database.");
 		} catch (Throwable e) {
 			flash.error("Couldn't load xml file!", e.getMessage());
 			e.printStackTrace();
 		}
 		if (xml != null) {
 			xml.delete();
 		}
		Application.index(0);
 	}
 
 	public static void clearDB() {
 		if (!Session.get().currentUser().isModerator()) {
 			flash.error("You're a naughty boy");
			Application.index(0);
 		}
 		Database.clearKeepAdmins();
		Application.index(0);
 	}
 }

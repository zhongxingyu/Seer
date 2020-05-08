 package controllers;
 
 import java.text.ParseException;
 
 import models.Answer;
 import models.Comment;
 import models.Question;
 import models.User;
 import play.data.validation.Required;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Secured extends Controller {
 	public static User currentUser() {
 		return User.get(Security.connected());
 	}
 
 	public static void newQuestion(@Required String content) {
 		if (!validation.hasErrors()) {
			Question question = new Question(currentUser(), content);
 			Application.question(question.id());
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void newAnswer(int questionId, @Required String content) {
 		if (!validation.hasErrors() && Question.get(questionId) != null) {
 			Question.get(questionId).answer(currentUser(), content);
 			Application.question(questionId);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void newCommentQuestion(int questionId,
 			@Required String content) {
 		if (!validation.hasErrors() && Question.get(questionId) != null) {
 			Question.get(questionId).comment(currentUser(), content);
 			Application.commentQuestion(questionId);
 		}
 	}
 
 	public static void newCommentAnswer(int questionId, int answerId,
 			@Required String content) {
 		Question question = Question.get(questionId);
 		Answer answer = question.getAnswer(answerId);
 
 		if (!validation.hasErrors() && answer != null) {
 			answer.comment(currentUser(), content);
 			Application.commentAnswer(questionId, answerId);
 		}
 	}
 
 	public static void voteQuestionUp(int id) {
 		if (Question.get(id) != null) {
 			Question.get(id).voteUp(currentUser());
 			if (!redirectToCallingPage())
 				Application.question(id);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void voteQuestionDown(int id) {
 		if (Question.get(id) != null) {
 			Question.get(id).voteDown(currentUser());
 			if (!redirectToCallingPage())
 				Application.question(id);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void voteAnswerUp(int question, int id) {
 		if (Question.get(question) != null
 				&& Question.get(question).getAnswer(id) != null) {
 			Question.get(question).getAnswer(id).voteUp(currentUser());
 			Application.question(question);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void voteAnswerDown(int question, int id) {
 		if (Question.get(question) != null
 				&& Question.get(question).getAnswer(id) != null) {
 			Question.get(question).getAnswer(id).voteDown(currentUser());
 			Application.question(question);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void deleteQuestion(int questionId) {
 		Question question = Question.get(questionId);
 		question.unregister();
 		Application.index();
 	}
 
 	public static void deleteAnswer(int answerId, int questionId) {
 		Question question = Question.get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		answer.unregister();
 		Application.question(questionId);
 	}
 
 	public static void deleteCommentQuestion(int commentId, int questionId) {
 		Question question = Question.get(questionId);
 		Comment comment = question.getComment(commentId);
 		question.unregister(comment);
 		Application.commentQuestion(questionId);
 	}
 
 	public static void deleteCommentAnswer(int commentId, int questionId,
 			int answerId) {
 		Question question = Question.get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		Comment comment = answer.getComment(commentId);
 		answer.unregister(comment);
 		Application.commentAnswer(questionId, answerId);
 	}
 
 	public static void deleteUser(String name) throws Throwable {
 		User user = User.get(name);
 		if (hasPermissionToDelete(currentUser(), user)) {
 			boolean deleteSelf = name.equals(currentUser().name());
 			user.delete();
 			if (deleteSelf)
 				Secure.logout();
 		}
 		Application.index();
 	}
 
 	public static void anonymizeUser(String name) throws Throwable {
 		User user = User.get(name);
 		if (hasPermissionToDelete(currentUser(), user))
 			user.anonymize(true, false);
 		deleteUser(name);
 	}
 
 	public static void selectBestAnswer(int questionId, int answerId) {
 		Question question = Question.get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		question.setBestAnswer(answer);
 		Application.question(questionId);
 	}
 
 	private static boolean hasPermissionToDelete(User currentUser, User user) {
 		return currentUser.name().equals(user.name());
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
 
 		User user = currentUser();
 		if (email != null)
 			user.setEmail(email);
 		if (fullname != null)
 			user.setFullname(fullname);
 		if (birthday != null)
 			user.setDateOfBirth(birthday);
 		if (website != null)
 			user.setWebsite(website);
 		if (profession != null)
 			user.setProfession(profession);
 		if (employer != null)
 			user.setEmployer(employer);
 		if (biography != null)
 			user.setBiography(biography);
 		Application.showprofile();
 	}
 }

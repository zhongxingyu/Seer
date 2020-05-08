 package controllers;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import models.Answer;
 import models.Comment;
 import models.Notification;
 import models.Question;
 import models.Tag;
 import models.TimeTracker;
 import models.User;
 import models.database.Database;
 import models.helpers.Tools;
 import play.data.validation.Required;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
 	@Before
 	static void setConnectedUser() {
 		if (controllers.Secure.Security.isConnected()) {
 			User user = Database.get().users()
 					.get(controllers.Secure.Security.connected());
 			renderArgs.put("user", user);
 		}
 	}
 
 	public static void index() {
 		List<Question> questions = Database.get().questions().all();
 		render(questions);
 	}
 
 	public static void question(int id) {
 		Question question = Database.get().questions().get(id);
 		if (question == null) {
 			render();
 		} else {
 			List<Question> similarQuestions = (new ArrayList(question
 					.getSimilarQuestions()));
 			if (similarQuestions.size() > 5)
 				similarQuestions = similarQuestions.subList(0, 5);
 			List<Answer> answers = question.answers();
 			render(question, answers, similarQuestions);
 		}
 	}
 
 	public static void answerQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		List<Question> questions = Database.get().questions().all();
 		List<Answer> answers = question.answers();
 		int count = question.answers().size();
 		render(questions, question, answers, count);
 	}
 
 	public static void commentQuestion(int id) {
 		Question question = Database.get().questions().get(id);
 		List<Question> questions = Database.get().questions().all();
 		List<Comment> comments = question.comments();
 		int count = question.comments().size();
 		render(questions, question, comments, count);
 	}
 
 	public static void commentAnswer(int questionId, int answerId) {
 		Question question = Database.get().questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		List<Comment> comments = answer.comments();
 		render(answer, comments, question);
 	}
 
 	public static void deleteuser(User user) {
 		render();
 	}
 
 	public static void register() {
 		render();
 	}
 
 	public static void signup(@Required String username, String password,
 			String passwordrepeat, String email) {
 
 		if (Tools.checkEmail(email) && password.equals(passwordrepeat)
 				&& User.isAvailable(username)) {
 			User user = Database.get().users().register(username, password);
 			user.setEmail(email);
 			// Mark user as connected
 			session.put("username", username);
 			index();
 		} else {
 			flash.keep("url");
 			if (!Tools.checkEmail(email)) {
 				flash.error("secure.emailerror");
 			}
 			if (!User.isAvailable(username)) {
 				flash.error("secure.usernameerror");
 			}
 			if (!password.equals(passwordrepeat)) {
 				flash.error("secure.passworderror");
 			}
 			params.flash();
 			register();
 		}
 	}
 
 	public static void showprofile(String userName) {
 		User showUser = Database.get().users().get(userName);
		User user = Session.get().currentUser();
		boolean canEdit = user != null
				&& (user == showUser || user.isModerator());
 		render(showUser, canEdit);
 	}
 
 	public static void editProfile(String userName) {
		User user = Database.get().users().get(userName);
		render(user);
 	}
 
 	public static void tags(String term, String content) {
 		String tagString = "";
 		for (Tag tag : Database.get().tags().all()) {
 			if (term == null || tag.getName().startsWith(term.toLowerCase())) {
 				tagString += tag.getName() + " ";
 			}
 		}
 		tagString += Tools.extractImportantWords(content);
 		// make sure not to return an array with a single empty string ([""])
 		String[] tags = tagString.split("\\s+");
 		if (tagString.length() == 0) {
 			tags = new String[0];
 		}
 		renderJSON(tags);
 	}
 
 	public static void search(String term) {
 		List<Question> results = Database.get().questions().searchFor(term);
 		render(results, term);
 	}
 
 	public static void notifications(int content) {
 		User user = Session.get().currentUser();
 		if (user != null) {
 			List<Question> suggestedQuestions = user.getSuggestedQuestions();
 			List<Notification> notifications = user.getNotifications();
 			List<Question> questions = Database.get().questions().all();
 			ArrayList<Question> watchingQuestions = new ArrayList<Question>();
 			for (Question question : questions) {
 				if (question.hasObserver(user)) {
 					watchingQuestions.add(question);
 				}
 			}
 			render(notifications, watchingQuestions, suggestedQuestions, content);
 		} else {
 			Application.index();
 		}
 	}
 
 	public static void showStatisticalOverview() {
 		GregorianCalendar now = new GregorianCalendar();
 		TimeTracker t = TimeTracker.getRealTimeTracker();
 		int numberOfUsers = Database.get().users().count();
 		int numberOfQuestions = Database.get().questions().count();
 		int numberOfAnswers = Database.get().questions().countAllAnswers();
 		int numberOfHighRatedAnswers = Database.get().questions()
 				.countHighRatedAnswers();
 		int numberOfBestAnswers = Database.get().questions()
 				.countBestRatedAnswers();
 		float questionsPerDay = (float) numberOfQuestions / t.getDays(now);
 		float questionsPerWeek = (float) numberOfQuestions / t.getWeeks(now);
 		float questionsPerMonth = (float) numberOfQuestions / t.getMonths(now);
 		float answersPerDay = (float) numberOfAnswers / t.getDays(now);
 		float answersPerWeek = (float) numberOfAnswers / t.getWeeks(now);
 		float answersPerMonth = (float) numberOfAnswers / t.getMonths(now);
 
 		render(numberOfQuestions, numberOfAnswers, numberOfUsers,
 				numberOfHighRatedAnswers, numberOfBestAnswers, questionsPerDay,
 				questionsPerWeek, questionsPerMonth, answersPerDay,
 				answersPerWeek, answersPerMonth);
 	}
 }

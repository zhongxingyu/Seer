 package controllers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import models.Answer;
 import models.Comment;
import models.DbManager;
 import models.Question;
 import models.User;
 import play.mvc.Controller;
 import play.mvc.With;
 
 /**
  * The Class Admin controls access on the database such as adding Answers or
  * Questions or voting and also managing the users.
  */
 @With(Secure.class)
 public class Admin extends Controller {
 
 	private static DbManager manager = DbManager
 			.getInstance();
 
 	private static Calendar calendar = Calendar.getInstance();
 
 	public static void showQuestionForm() {
 		render();
 	}
 	
 	public static void showEditQuestionForm(int qid) {
 		Question question = manager.getQuestionById(qid);
 		render(question, qid);
 	}
 	
 	/**
 	 * Sets the content of the question to the new value
 	 * @param qid
 	 * @param newContent
 	 */
 	public static void editQuestion(int qid, String newContent){
 		manager.getQuestionById(qid).setContent(newContent);
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showEditAnswerForm(int answerId, int qid) {
 		Answer answer = manager.getAnswerById(answerId);
 		render(answer, answerId, qid);
 	}
 	
 	/**
 	 * Sets the content of the answer to the new value
 	 * @param answerId
 	 * @param newContent
 	 */
 	public static void editAnswer(int answerId, int qid, String newContent) {
 		manager.getAnswerById(answerId).setContent(newContent);
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showQuestionCommentForm(String qid) {
 		render(qid);
 	}
 	
 	public static void showAnswerCommentForm(int answerId, String qid) {
 		render(answerId, qid);
 	}
 
 	public static void addQuestion(String newQuestion, String tags) {
 		User user = manager.getUserByName(session.get("username"));
 		if (newQuestion.equals("") || newQuestion.equals(" ")) {
 			String message = "Your question is empty!";
 			render(message);
 		} else if (manager.checkQuestionDuplication(newQuestion)) {
 			String message = "Your question already exists!";
 			render(message);
 		} else if (!Question.checkTags(tags).isEmpty()) {
 			String message = "The following tags already exist: "
 					+ Question.checkTags(tags) + ". Please review your tags.";
 			render(message);
 		} else {
 			@SuppressWarnings("unused")
 			Question question = new Question(newQuestion, user);
 			question.addTags(tags);
 			redirect("/");
 		}
 	}
 
 	public static void addAnswer(String qid, String newAnswer) {
 		int intId = Integer.parseInt(qid);
 		User user = manager.getUserByName(session.get("username"));
 		if (newAnswer.equals("") || newAnswer.equals(" ")) {
 			String message = "Your answer is empty!";
 			render(message, qid);
 		} else {
 			@SuppressWarnings("unused")
 			Answer answer = new Answer(newAnswer, user,
 					manager.getQuestionById(intId));
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 
 	public static void addCommentToQuestion(int qid ,String newComment) {
 		User user = manager.getUserByName(session.get("username"));
 		Question question = manager.getQuestionById(qid);
 		if (newComment.equals("") || newComment.equals(" ")) {
 			String message = "Your comment is empty!";
 			render(message);
 		} else {
 			Comment comment = new Comment(user, question, newComment);
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 	
 	public static void addCommentToAnswer(int answerId, int qid, String newComment) {
 		User user = manager.getUserByName(session.get("username"));
 		Answer answer = manager.getAnswerById(answerId);
 		if (newComment.equals("") || newComment.equals(" ")) {
 			String message = "Your comment is empty!";
 			render(message);
 		} else {
 			Comment comment = new Comment(user, answer, newComment);
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 
 	public static void voteQuestion(String qid, String vote) {
 		int id = Integer.parseInt(qid);
 		User user = manager.getUserByName(session.get("username"));
 		if (manager.getQuestionById(id).getOwner()
 				.equals(session.get("username"))) {
 			String message = "You cannot vote your own question!";
 			render(message, qid);
 		} else if (manager.getQuestionById(id).checkUserVotedForQuestion(user)) {
 			String message = "You already voted this question";
 			render(message, qid);
 		} else {
 			manager.getQuestionById(id).vote(vote);
 			manager.getQuestionById(id).userVotedForQuestion(user);
 			redirect("/");
 		}
 	}
 
 	public static void voteAnswer(String qid, String aid, String vote) {
 		int id = Integer.parseInt(aid);
 		User user = manager.getUserByName(session.get("username"));
 		@SuppressWarnings("unused")
 		Answer answer = manager.getAnswerById(id);
 		if (manager.getAnswerById(id).getOwner()
 				.equals(session.get("username"))) {
 			String message = "You cannot vote your own answer!";
 			render(message, qid);
 		} else if (manager.getAnswerById(id).checkUserVotedForAnswer(user)) {
 			String message = "You already voted this question";
 			render(message, qid);
 		} else {
 			manager.getAnswerById(id).vote(vote);
 			manager.getAnswerById(id).userVotedForAnswer(user);
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 
 	public static void showAnswerForm(String qid) {
 		int intId = Integer.parseInt(qid);
 		ArrayList<Answer> answers = manager.getAllAnswersByQuestionId(intId);
 		Question question = manager.getQuestionById(intId);
 		render(answers, question);
 	}
 
 	public static void showUsers() {
 		if (manager.getUsers().isEmpty()) {
 			String message = "no users";
 			render(message);
 		} else {
 			ArrayList<User> users = manager.getUsers();
 			render(users);
 		}
 	}
 
 	public static void showUserLog(String uname) {
 		ArrayList<String> userLog = manager.getUserLog(uname);
 		if (userLog.size() == 1) {
 			String message = "no activities so far.";
 			render(message);
 		}
 		render(userLog);
 	}
 
 }

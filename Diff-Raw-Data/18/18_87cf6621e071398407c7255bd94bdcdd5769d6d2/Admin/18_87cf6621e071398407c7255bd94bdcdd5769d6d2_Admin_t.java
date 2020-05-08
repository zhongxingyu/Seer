 package controllers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import models.Answer;
 import models.Comment;
 import models.DbManager;
 import models.Question;
 import models.User;
 import models.UserGroups;
 import play.mvc.Controller;
 import play.mvc.With;
 
 /**
  * The Class Admin controls access on the database such as adding Answers or
  * Questions or voting and also managing the users.
  */
 @With(Secure.class)
 public class Admin extends Controller {
 
 	private static DbManager manager = DbManager.getInstance();
 
 	private static Calendar calendar = Calendar.getInstance();
 
 	public static void showQuestionForm(String newQuestion, String tags,
 			String message) {
 		render(newQuestion, tags, message);
 	}
 
 	public static void showEditQuestionForm(int qid) {
 		Question question = manager.getQuestionById(qid);
		if (session.get("username") == question.getOwner()
 				.getName()) {
 			render(question, qid);
 		} else {
 			String message = "you're not allowed to edit this post!";
 			render(question, qid, message);
 		}
 	}
 
 	public static void showEditCommentForm(int aid, int qid, int cid) {
 		Comment comment = manager.getCommentById(cid);
 		render(comment, aid, qid);
 	}
 
 	/**
 	 * Sets the content of the comment to the new value
 	 * 
 	 * @param comment
 	 * @param newContent
 	 */
 
 	public static void editComment(Comment comment, int qid, int cid,
 			String newContent) {
 		comment.setContent(newContent, session.get("Username"));
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	/**
 	 * Sets the content of the question to the new value
 	 * 
 	 * @param qid
 	 * @param newContent
 	 */
 	public static void editQuestion(int qid, String newContentQuestion,
 			String newContentTag) {
 		manager.getQuestionById(qid).getTags().clear();
 		manager.getQuestionById(qid).setContent(newContentQuestion,
 				session.get("Username"));
		manager.getQuestionById(qid).addTags(newContentTag);
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showEditAnswerForm(int answerId, int qid) {
 		Answer answer = manager.getAnswerById(answerId);
 		if (session.get(answer.getOwner().getName()) == answer.getOwner()
 				.getName()) {
 			render(answer, answerId, qid);
 		} else {
 			String message = "you're not allowed to edit this post!";
 			render(answer, answerId, qid, message);
 		}
 	}
 
 	/**
 	 * Sets the content of the answer to the new value
 	 * 
 	 * @param answerId
 	 * @param newContent
 	 */
 	public static void editAnswer(int answerId, int qid, String newContent,
 			User user) {
 		manager.getAnswerById(answerId).setContent(newContent,
 				session.get("username"));
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showQuestionCommentForm(String qid) {
 		render(qid);
 	}
 
 	public static void showAnswerCommentForm(int answerId, String qid) {
 		render(answerId, qid);
 	}
 
 	public static void addQuestion(String newQuestion, String tags) {
 		// Store the overgiven tags in another object to prevent information
 		// loss due to splitting the tag list.
 		String copyTags = "" + tags;
 
 		User user = manager.getUserByName(session.get("username"));
 		if (newQuestion.equals("") || newQuestion.equals(" ")) {
 			String message = "Your question is empty!";
 			showQuestionForm(newQuestion, tags, message);
 		} else if (manager.checkQuestionDuplication(newQuestion)) {
 			String message = "Your question already exists!";
 			showQuestionForm(newQuestion, tags, message);
 		} else if (!Question.checkTags(copyTags).isEmpty()) {
 			String message = "The following tags already exist: "
 					+ Question.checkTags(copyTags)
 					+ ". Please review your tags.";
 			showQuestionForm(newQuestion, tags, message);
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
 			Answer answer = new Answer(newAnswer, user, manager
 					.getQuestionById(intId));
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 
 	public static void addCommentToQuestion(int qid, String newComment) {
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
 
 	public static void addCommentToAnswer(int answerId, int qid,
 			String newComment) {
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
 
 	public static void voteQuestionUp(int qid) {
 		voteQuestion(qid, 1);
 	}
 
 	public static void voteQuestionDown(int qid) {
 		voteQuestion(qid, -1);
 	}
 
 	public static void voteQuestion(int qid, int vote) {
 		// int id = Integer.parseInt(qid);
 		User user = manager.getUserByName(session.get("username"));
 		if (manager.getQuestionById(qid).getOwner().equals(
 				session.get("username"))) {
 			String message = "You cannot vote your own question!";
 			render(message, qid);
 		} else if (manager.getQuestionById(qid).checkUserVotedForQuestion(user)) {
 			String message = "You already voted this question";
 			render(message, qid);
 		} else {
 			manager.getQuestionById(qid).vote(vote);
 			manager.getQuestionById(qid).userVotedForQuestion(user);
 			redirect("/");
 		}
 	}
 
 	public static void voteAnswerUp(int qid, int aid) {
 		voteAnswer(qid, aid, 1);
 	}
 
 	public static void voteAnswerDown(int qid, int aid) {
 		voteAnswer(qid, aid, -1);
 	}
 
 	public static void voteAnswer(int qid, int aid, int vote) {
 		// int id = Integer.parseInt(aid);
 		User user = manager.getUserByName(session.get("username"));
 		@SuppressWarnings("unused")
 		Answer answer = manager.getAnswerById(aid);
 		if (manager.getAnswerById(aid).getOwner().equals(
 				session.get("username"))) {
 			String message = "You cannot vote your own answer!";
 			render(message, qid);
 		} else if (manager.getAnswerById(aid).checkUserVotedForAnswer(user)) {
 			String message = "You already voted this question";
 			render(message, qid);
 		} else {
 			manager.getAnswerById(aid).vote(vote);
 			manager.getAnswerById(aid).userVotedForAnswer(user);
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
 
 	// TODO: Übergabe der Werte aus radio check boxes & speichern dieser.
 	public static void editUserGroup(String uname, String group) {
 		ArrayList<User> users = manager.getUsers();
 		User user = manager.getUserByName(uname);
 		UserGroups ugroup;
 
 		if (manager.getUserByName(session.get("username")).isAdmin()) {
 			if (uname == null && group == null)
 				render(users);
 			else {
 				if (group.equals("admin")) {
 					ugroup = UserGroups.admin;
 					user.setGroup(ugroup);
 				}
 				if (group.equals("moderator")) {
 					ugroup = UserGroups.moderator;
 					user.setGroup(ugroup);
 				}
 				if (group.equals("user")) {
 					ugroup = UserGroups.user;
 					user.setGroup(ugroup);
 				}
 				String message = "Settings changed.";
 				render(users, message);
 			}
 		} else {
 			redirect("/");
 		}
 	}
 
 	public static void showUser(String uname) {
 		User user = manager.getUserByName(session.get("username"));
 		if (manager.getUserByName(session.get("username")).isAdmin()) {
 			manager.deleteUser(uname);
 			redirect("/");
 		} else {
 			String message = "You're not authorised!";
 			render(message);
 		}
 		render(user);
 	}
 
 	public static void showAdminPage() {
 		String uname = session.get("username");
 		if (!manager.getUserByName(uname).isAdmin())
 			redirect("/");
 		else
 			render(uname);
 
 	}
 
 }

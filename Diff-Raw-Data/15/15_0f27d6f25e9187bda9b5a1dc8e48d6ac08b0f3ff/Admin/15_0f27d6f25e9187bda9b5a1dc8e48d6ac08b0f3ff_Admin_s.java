 package controllers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import models.Answer;
 import models.Comment;
 import models.DbManager;
 import models.Notification;
 import models.Post;
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
 
 	public static void showEditQuestionForm(int qid, String newContent,
 			String tags, String message) {
 		Question question = manager.getQuestionById(qid);
 		render(question, newContent, qid, message, tags);
 	}
 
 	public static void showEditAnswerForm(int answerId, int qid,
 			String newContent, String message) {
 		Answer answer = manager.getAnswerById(answerId);
 		render(answer, answerId, qid, newContent, message);
 	}
 
 	public static void showEditCommentForm(int qid, int cid) {
 		Comment comment = manager.getCommentById(cid);
 		String CommentContent = manager.getCommentById(cid).getContent();
 		render(comment, qid, cid, CommentContent);
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
 	 * @param newContentQuestion
 	 * @param newContentTag
 	 */
 	public static void editQuestion(int qid, String newContentQuestion,
 			String newContentTag) {
 		// --> Bitte noch nicht löschen, evtl. steht hier noch Refactoring an!
 		// String checked = checkQuestion(newContentQuestion, newContentTag);
 		// if (!checked.isEmpty())
 		// showEditQuestionForm(qid, newContentQuestion, newContentTag,
 		// checked);
 		// else {
 		// manager.getQuestionById(qid).addVersion(newContentQuestion,
 		// newContentTag, session.get("username"));
 		// redirect("/question/" + qid + "/answers/");
 		// }
 		// Store the overgiven tags in another object to prevent information
 		// loss due to splitting the tag list.
 		String copyTags = "" + newContentTag;
 
 		User user = manager.getUserByName(session.get("username"));
 		if (newContentQuestion.equals("") || newContentQuestion.equals(" ")) {
 			String message = "Your question is empty!";
 			showEditQuestionForm(qid, newContentQuestion, newContentTag, message);
 		} else if (!Question.checkTags(copyTags).isEmpty()) {
 			String message = "The following tags already exist: "
 					+ Question.checkTags(copyTags)
 					+ ". Please review your tags.";
 			showEditQuestionForm(qid, newContentQuestion, newContentTag, message);
 		}
 		// if (!checkQuestion(newQuestion, copyTags).isEmpty()) {
 		// showQuestionForm(newQuestion, tags, checkQuestion(newQuestion,
 		// copyTags));
 		//			
 		// }
 		else {
 			manager.getQuestionById(qid).addVersion(newContentQuestion,
 					newContentTag, session.get("username"));
 			redirect("/question/" + qid + "/answers/");
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
 		if (newContent.equals("") || newContent.equals(" ")) {
 			String message = "Your answer is empty.";
 			showEditAnswerForm(answerId, qid, newContent, message);
 		}
 		manager.getAnswerById(answerId).addVersion(newContent,
 				session.get("username"));
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showQuestionForm(String newQuestion, String tags,
 			String message) {
 		render(newQuestion, tags, message);
 	}
 
 	public static void showQuestionCommentForm(String qid) {
 		render(qid);
 	}
 
 	public static void showAnswerCommentForm(int answerId, String qid) {
 		render(answerId, qid);
 	}
 	
 	public static void deleteQuestion(int qid){
 		Question question = manager.getQuestionById(qid);
 		manager.deleteQuestion(question);
 		redirect("/");
 	}
 	
 	public static void deleteAnswer(int aid, int qid){
 		Answer answer = manager.getAnswerById(aid);
 		manager.deleteAnswer(answer);
 		redirect("/question/" + qid + "/answers/");
 	}
 	
 	public static void deleteComment(int qid, int cid){
 		Comment comment = manager.getCommentById(cid);
 		manager.deleteComment(comment);
 		redirect("/question/" + qid + "/answers/");
 	}
 	
 	public static void deleteUser(String uname){
 		manager.deleteUser(uname);
 		redirect("/editUserGroup");
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
 		}
 		// --> Bitte nicht löschen, evtl. Refactoring.
 		// if (!checkQuestion(newQuestion, copyTags).isEmpty()) {
 		// showQuestionForm(newQuestion, tags, checkQuestion(newQuestion,
 		// copyTags));
 		//			
 		// }
 		else {
 			@SuppressWarnings("unused")
 			Question question = new Question(true, newQuestion, user);
 			question.addTags(tags);
 			redirect("/question/" + question.getId() + "/answers/");
 		}
 	}
 
 	// --> Bitte noch nicht löschen, evtl. Refactoring.
 	//	private static String checkQuestion(String newQuestion, String tags) {
 	// // Store the overgiven tags in another object to prevent information
 	// // loss due to splitting the tag list.
 	// String copyTags = "" + tags;
 	// String message = "";
 	//
 	// if (newQuestion.equals("") || newQuestion.equals(" ")) {
 	// message = "Your question is empty!\n";
 	// }
 	// if (!Question.checkTags(copyTags).isEmpty()) {
 	// message = "The following tags already exist: "
 	// + Question.checkTags(copyTags)
 	// + ". Please review your tags.\n";
 	// }
 	// return message;
 	// }
 
 	public static void addAnswer(String qid, String newAnswer) {
 		int intId = Integer.parseInt(qid);
 		User user = manager.getUserByName(session.get("username"));
 		if (newAnswer.equals("") || newAnswer.equals(" ")) {
 			String message = "Your answer is empty!";
 			Application.showAnswers(qid, newAnswer, message);
 			// render(message, qid);
 		} else {
 			@SuppressWarnings("unused")
 			Answer answer = new Answer(true, newAnswer, user, manager
 					.getQuestionById(intId));
 			redirect("/question/" + qid + "/answers/");
 		}
 	}
 
 	public static void addCommentToQuestion(int qid, String newComment) {
 		User user = manager.getUserByName(session.get("username"));
 		Post question = manager.getQuestionById(qid);
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
 	
 	public static void voteQuestionNeutral(int qid) {
 		voteQuestion(qid, 0);
 	}
 
 	public static void voteQuestionDown(int qid) {
 		voteQuestion(qid, -1);
 	}
 
 	public static void voteQuestion(int qid, int vote) {
 		// int id = Integer.parseInt(qid);
 		Question question = manager.getQuestionById(qid);
 		User user = manager.getUserByName(session.get("username"));
 		user.addvotedPost(question);
 		question.userVotedForPost(user);
 		if(user.getVotedPost(question).voteChangeable()==false){
 			String message = "You already voted for this post!";
 			render(message, qid);
 		}
		if(user.getVotedPost(question).getVoteSetTime()==null){
			user.getVotedPost(question).setvoteSetTime();
		}
 		if (vote==1&&user.getVotedPost(question).getcurrentVote()!=1) {
 			user.getVotedPost(question).setcurrentVote(vote);
 			question.setTempVote(1);
 			String message = "Your current vote is +1";
 			render(message, qid);
 		}
 		if (vote==0&&user.getVotedPost(question).getcurrentVote()!=0){
 			user.getVotedPost(question).setcurrentVote(vote);
 			question.setTempVote(0);
 			String message = "Your current vote is 0";
 			render(message, qid);
 		}
 		if (vote==-1&&user.getVotedPost(question).getcurrentVote()!=-1){
 			user.getVotedPost(question).setcurrentVote(vote);
 			question.setTempVote(-1);
 			String message = "Your current vote is -1";
 			render(message, qid);
 		}
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void voteAnswerUp(int qid, int aid) {
 		voteAnswer(qid, aid, 1);
 	}
 	
 	public static void voteAnswerNeutral(int qid, int aid) {
 		voteAnswer(qid, aid, 0);
 	}
 
 	public static void voteAnswerDown(int qid, int aid) {
 		voteAnswer(qid, aid, -1);
 	}
 
 	public static void voteAnswer(int qid, int aid, int vote) {
 		// int id = Integer.parseInt(qid);
 		Answer answer = manager.getAnswerById(aid);
 		User user = manager.getUserByName(session.get("username"));
 		user.addvotedPost(answer);
 		answer.userVotedForPost(user);
 		if(answer.checkUserVotedForPost(user)==true&&user.getVotedPost(answer).voteChangeable()==false){
 			String message = "You already voted for this post!";
 			render(message, qid);
 		}
		if(user.getVotedPost(answer).getVoteSetTime()==null){
			user.getVotedPost(answer).setvoteSetTime();
		}
 		if (vote==1&&user.getVotedPost(answer).getcurrentVote()!=1) {
 			user.getVotedPost(answer).setcurrentVote(vote);
 			answer.setTempVote(1);
 			String message = "Your current vote is +1";
 			render(message, qid);
 		}
 		if (vote==0&&user.getVotedPost(answer).getcurrentVote()!=0){
 			user.getVotedPost(answer).setcurrentVote(vote);
 			answer.setTempVote(0);
 			String message = "Your current vote is 0";
 			render(message, qid);
 		}
 		if (vote==-1&&user.getVotedPost(answer).getcurrentVote()!=-1){
 			user.getVotedPost(answer).setcurrentVote(vote);
 			answer.setTempVote(-1);
 			String message = "Your current vote is -1";
 			render(message, qid);
 		}
 		redirect("/question/" + qid + "/answers/");
 	}
 
 	public static void showAnswerForm(String qid) {
 		int intId = Integer.parseInt(qid);
 		ArrayList<Answer> answers = manager.getAllAnswersByQuestionId(intId);
 		Post question = manager.getQuestionById(intId);
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
 
 	public static void showNotifications() {
 		User user = manager.getUserByName(session.get("username"));
 		ArrayList<Notification> notifications = (ArrayList<Notification>) user
 				.getAllNotifications().clone();
 		user.clearAllNotifications();
 		render(notifications);
 	}
 
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
 
 	public static void showAdminPage() {
 		String uname = session.get("username");
 		if (!manager.getUserByName(uname).isAdmin())
 			redirect("/");
 		else
 			render(uname);
 
 	}
 
 	public static void restoreQuestion(int actualId, String oldContent,
 			String oldTags) {
 		String uname = session.get("username");
 		// unschön, aber es erfüllt den zweck:
 		String tags = oldTags.replace("[", "").replace("]", "")
 				.replace(",", "");
 		Question actualQuestion = manager.getQuestionById(actualId);
 		actualQuestion.addVersion(oldContent, tags, uname);
 		redirect("/question/" + actualQuestion.getId() + "/history");
 	}
 
 	public static void restoreAnswer(int actualId, String oldContent) {
 		String uname = session.get("username");
 		Answer actualAnswer = manager.getAnswerById(actualId);
 		actualAnswer.restoreOldVersion(oldContent, uname);
 		redirect("/answer/" + actualAnswer.getId() + "/history");
 	}
 
 }

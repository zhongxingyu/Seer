 package controllers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Answer;
 import models.Comment;
 import models.Notification;
 import models.Question;
 import models.Tag;
 import models.TimeTracker;
 import models.User;
 import models.database.IQuestionDatabase;
 import models.helpers.Tools;
 import notifiers.Mails;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.i18n.Lang;
 import play.libs.Codec;
 import play.libs.Images;
 
 /**
  * The Application controller controls all the views and none of the actions
  * associated with the views (see {@link CUser}, {@link CCQuestion} and
  * {@link CAnswer} for these).
  */
 public class Application extends BaseController {
 
 	private static final int entriesPerPage = 15;
 	public static final TimeTracker timeTracker = new TimeTracker();
 
 	/**
 	 * Leads to the index page at a given page of {@link Question}'s.
 	 * 
 	 * @param index
 	 *            the number of the page of {@link Question}'s.
 	 */
 	public static void index(int index) {
 		List<Question> questions = (List<Question>) Cache
 				.get("index.questions");
 		if (questions == null) {
 			questions = Database.questions().all();
 			Collections.sort(questions, new Comparator<Question>() {
 				public int compare(Question q1, Question q2) {
 					return q2.timestamp().compareTo(q1.timestamp());
 				}
 			});
 			Cache.set("index.questions", questions, "10mn");
 		}
 		int maxIndex = Tools.determineMaximumIndex(questions, entriesPerPage);
 		questions = Tools.paginate(questions, entriesPerPage, index);
 
 		render(questions, index, maxIndex);
 	}
 
 	/**
 	 * Leads to the detailed view of a {@link Question}.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void question(int id) {
 		Question question = Database.questions().get(id);
 		if (question == null) {
 			render();
 		} else {
 			List<Question> similarQuestions = (List<Question>) Cache
 					.get("question." + id + ".similar");
 			if (similarQuestions == null) {
 				similarQuestions = Database.questions().findSimilar(question);
 				if (similarQuestions.size() > 5) {
 					similarQuestions = similarQuestions.subList(0, 5);
 				}
 				// the Cache chokes on sublists!
 				similarQuestions = new ArrayList(similarQuestions);
 				Cache.set("question." + id + ".similar", similarQuestions,
 						"10mn");
 			}
 			List<Answer> answers = question.answers();
 			render(question, answers, similarQuestions);
 		}
 	}
 
 	/**
 	 * Leads to the detailed view of the {@link Question} and the field to
 	 * submit a comment.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void commentQuestion(int id) {
 		Question question = Database.questions().get(id);
 		render(question);
 	}
 
 	/**
 	 * Leads to the detailed view of the {@link Answer} and the field to submit
 	 * a comment.
 	 * 
 	 * @param id
 	 *            the id of the {@link Answer}
 	 */
 	public static void commentAnswer(int questionId, int answerId) {
 		Question question = Database.questions().get(questionId);
 		Answer answer = getAnswer(questionId, answerId);
 		render(answer, question);
 	}
 
 	/**
 	 * Prompts the user to confirm the deletion of the {@link Question}.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}.
 	 */
 	public static void confirmDeleteQuestion(int id) {
 		Question question = Database.questions().get(id);
 		render(question);
 	}
 
 	/**
 	 * Prompts the user to mark this {@link Question} as Spam.
 	 * 
 	 * @param id
 	 *            the id of the {@link Question}
 	 */
 	public static void confirmMarkSpamQuestion(int id) {
 		Question question = Database.questions().get(id);
 		render(question);
 	}
 
 	/**
 	 * Prompts the user to mark this {@link Answer} as Spam.
 	 * 
 	 * @param questionId
 	 *            the id of the {@link Question} this {@link Answer} belongs to
 	 * @param answerId
 	 *            the id of the {@link Answer}
 	 */
 	public static void confirmMarkSpamAnswer(int questionId, int answerId) {
 		Question question = Database.questions().get(questionId);
 		Answer answer = getAnswer(questionId, answerId);
 		render(question, answer);
 	}
 
 	/**
 	 * Prompts the user to mark {@link Comment} as Spam.
 	 * 
 	 * @param questionId
 	 *            the id of the {@link Question} this {@link Answer} belongs to
 	 * @param answerId
 	 *            the id of the {@link Answer} this {@link Comment} belongs to
 	 * @param commentId
 	 *            the id of the {@link Comment}
 	 */
 	public static void confirmMarkSpamAnswerComment(int questionId,
 			int answerId, int commentId) {
 		Question question = Database.questions().get(questionId);
 		Answer answer = question.getAnswer(answerId);
 		Comment comment = answer.getComment(commentId);
 		render(question, answer, comment);
 	}
 
 	/**
 	 * Prompts the user to mark {@link Comment} as Spam.
 	 * 
 	 * @param questionId
 	 *            the id of the {@link Question} this {@link Answer} belongs to
 	 * @param commentId
 	 *            the id of the {@link Comment}
 	 */
 	public static void confirmMarkSpamQuestionComment(int questionId,
 			int commentId) {
 		Question question = Database.questions().get(questionId);
 		Comment comment = question.getComment(commentId);
 		render(question, comment);
 	}
 
 	/**
 	 * Displays the "are you sure" page before deleting a user.
 	 */
 	public static void deleteuser() {
 		User showUser = Session.user();
 		render(showUser);
 	}
 
 	/**
 	 * Displays the registration form.
 	 */
 	public static void register() {
 		// random identifier for the CAPTCHA
 		String randomID = Codec.UUID();
 		render(randomID);
 	}
 
 	/**
 	 * Lets the {@link User} sign up. Error-messages will be displayed, if the
 	 * information submitted by the {@link User} is wrong.
 	 * 
 	 * @param username
 	 *            the name the {@link User} has entered. This field is
 	 *            mandatory.
 	 * @param password
 	 *            the password the {@link User} chooses.
 	 * @param passwordrepeat
 	 *            the repeated password.
 	 */
 	public static void signup(@Required String username, String password,
 			@Required String email, String passwordrepeat,
 			@Required String code, String randomID) {
 		boolean isUsernameAvailable = Database.users().isAvailable(
 				username);
 		validation.equals(code, Cache.get("captcha." + randomID));
		validation.equals(code, Cache.get(randomID));
 		if (validation.hasErrors()) {
 			flash.error("captcha.invalid");
 			params.flash();
 			register();
 		}
 		if (password.equals(passwordrepeat) && isUsernameAvailable) {
 			User user = Database.users().register(username, password,
 					email);
 			boolean success = Mails.welcome(user);
 			if (success) {
 				flash.success("secure.mail.success");
 				index(0);
 			} else {
 				user.delete();
 				flash.error("secure.mail.error");
 				register();
 			}
 		} else {
 			flash.keep("url");
 			if (!isUsernameAvailable) {
 				flash.error("secure.usernameerror");
 			}
 			if (!password.equals(passwordrepeat)) {
 				flash.error("secure.passworderror");
 			}
 			params.flash();
 			register();
 		}
 	}
 
 	/**
 	 * Leads to the view of a {@link User}'s profile.
 	 * 
 	 * @param userName
 	 *            the name of the {@link User} who is the owner of the profile.
 	 */
 	public static void showprofile(String userName) {
 		User showUser = Database.users().get(userName);
 		List<Tag> expertise = Database.questions().getExpertise(showUser);
 		boolean canEdit = userCanEditProfile(showUser);
 		render(showUser, expertise, canEdit);
 	}
 
 	/**
 	 * Renders a JSON list combining all the currently used tags starting with a
 	 * given term and the most often used words in a given (question's) content.
 	 * This list can be used for implementing client-side tag autocompletion.
 	 * 
 	 * @param term
 	 *            the part of the tag a user has already entered and that is
 	 *            supposed to be auto-completed
 	 * @param content
 	 *            the content of e.g. a question to search through for often
 	 *            occurring words that might also be useful as tags
 	 */
 	public static void tags(String term, String content) {
 		List<String> tags = Database.tags().suggestTagNames(term);
 		if (content != null) {
 			tags.addAll(Tools.extractImportantWords(content));
 		}
 		renderJSON(tags);
 	}
 
 	/**
 	 * Performs a search for the entered term. The view is displayed at the
 	 * given index. Prevents a user from searching something different too soon
 	 * or anonymous users from searching at all - with the exception of the
 	 * search for a single tag which remains unrestricted.
 	 * 
 	 * @param term
 	 *            the term to be searched for.
 	 * @param index
 	 *            the page-number which will be displayed.
 	 */
 	public static void search(String term, int index) {
 		List<Question> results = (List<Question>) Cache.get("search." + term);
 		User user = Session.user();
 		boolean isPureTagSearch = term.matches("^tag:\\S+$");
 
 		if (results != null) {
 			// we've already done this search lately, so we can
 			// let the user do it with hardly any additional cost
 		} else if (isPureTagSearch) {
 			// we currently allow the search for a single tag for all
 			// users all the time
 		} else if (user == null) {
 			flash.error("search.notloggedin");
 			// don't redirect to the calling page, as that might be a search
 			// page which would lead to an infinite loop of failing
 			index(0);
 		} else if (!user.canSearchFor(term)) {
 			flash.error("search.hastowait");
 			// don't redirect to the calling page, as that might be a search
 			// page which would lead to an infinite loop of failing
 			index(0);
 		}
 
 		if (results == null) {
 			results = Database.questions().searchFor(term);
 			Cache.set("search." + term, results, "5mn");
 		}
 		int maxIndex = Tools.determineMaximumIndex(results, entriesPerPage);
 		results = Tools.paginate(results, entriesPerPage, index);
 		if (user != null && !isPureTagSearch) {
 			user.setLastSearch(term, new Date());
 		}
 		render(results, term, index, maxIndex);
 	}
 
 	/**
 	 * Displays one of four different view-parts concerned with user
 	 * notifications:
 	 * <ol start="0">
 	 * <li>A user's notifications about watched questions so that the user can
 	 * easily access all the new answers to his/her questions.
 	 * <li>A list of all the questions the user is currently watching so that
 	 * he/she can easily unwatch questions from a centralized place.
 	 * <li>A list of questions suggested to the user because he/she might know
 	 * to answer them as well, as they do have the same tags as questions the
 	 * user has already successfully answered.
 	 * <li>For moderators only: A list of questions and answers that have been
 	 * marked by other users as possibly being spam so that the moderator can
 	 * verify these claims and also easily delete spam and block spamming users.
 	 * </ol>
 	 * 
 	 * @param content
 	 *            the index of what view-part to display (0 =
 	 *            watch-notifications, 1 = watched questions, 2 = suggested
 	 *            questions, 3 = spam reports)
 	 */
 	public static void notifications(int content) {
 		User user = Session.user();
 		if (user != null) {
 			List<Notification> spamNotification = new LinkedList();
 			List<Question> suggestedQuestions = Database.questions()
 					.suggestQuestions(user);
 			List<Notification> notifications = user.getNotifications();
 			List<Question> watchingQuestions = Database.questions()
 					.getWatchList(user);
 			if (user.isModerator()) {
 				spamNotification.addAll(Database.users()
 						.getModeratorMailbox().getAllNotifications());
 			}
 			render(notifications, watchingQuestions, suggestedQuestions,
 					spamNotification, content);
 		} else {
 			Application.index(0);
 		}
 	}
 
 	/**
 	 * Leads to the statistical overview. The statistical data is calculated via
 	 * the {@link TimeTracker}.
 	 */
 	public static void showStatisticalOverview() {
 		TimeTracker t = Application.timeTracker;
 		IQuestionDatabase questionDB = Database.questions();
 		int numberOfUsers = Database.users().count();
 		int numberOfQuestions = questionDB.count();
 		int numberOfAnswers = questionDB.countAllAnswers();
 		int numberOfHighRatedAnswers = questionDB.countHighRatedAnswers();
 		int numberOfBestAnswers = questionDB.countBestRatedAnswers();
 		float questionsPerDay = (float) numberOfQuestions / t.getDays();
 		float questionsPerWeek = (float) numberOfQuestions / t.getWeeks();
 		float questionsPerMonth = (float) numberOfQuestions / t.getMonths();
 		float answersPerDay = (float) numberOfAnswers / t.getDays();
 		float answersPerWeek = (float) numberOfAnswers / t.getWeeks();
 		float answersPerMonth = (float) numberOfAnswers / t.getMonths();
 
 		render(numberOfQuestions, numberOfAnswers, numberOfUsers,
 				numberOfHighRatedAnswers, numberOfBestAnswers, questionsPerDay,
 				questionsPerWeek, questionsPerMonth, answersPerDay,
 				answersPerWeek, answersPerMonth);
 	}
 
 	/**
 	 * Leads to the admin page where a moderator can edit several options e.g.
 	 * clear the database.
 	 */
 	public static void admin() {
 		User user = Session.user();
 		if (user == null || !user.isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	/**
 	 * Leads to the clearDB page.
 	 */
 	public static void clearDB() {
 		User user = Session.user();
 		if (user == null || !user.isModerator()) {
 			flash.error("secure.moderatorerror");
 			Application.index(0);
 		}
 		render();
 	}
 
 	/**
 	 * Changes the language of the user interface.
 	 * 
 	 * @param langId
 	 *            an ISO-631-like language code (e.g. en, de, fr)
 	 */
 	public static void selectLanguage(@Required String langId) {
 		if (langId != null) {
 			Lang.change(langId);
 			if (!Lang.get().equals(langId)) {
 				flash.error("Unknown language %s!", langId);
 			}
 		} else {
 			flash.error("Wanna silence me? Try again!");
 		}
 		if (!redirectToCallingPage()) {
 			index(0);
 		}
 	}
 
 	/**
 	 * Leads to the edit-view of the {@link User}'s profile
 	 * 
 	 * @param userName
 	 *            the name of the {@link User} who owns the profile
 	 */
 	public static void editProfile(@Required String userName) {
 		User showUser = Database.users().get(userName);
 		if (!userCanEditProfile(showUser)) {
 			showprofile(userName);
 		}
 		render(showUser);
 	}
 
 	/**
 	 * Confirm a {@link User}'s profile if they clicked on the right link
 	 * 
 	 * @param username
 	 *            of the {@link User}
 	 * @param key
 	 *            for the Confirmation
 	 */
 	public static void confirmUser(@Required String username, String key) {
 		User user = Database.users().get(username);
 		boolean existsUser = Database.users().isAvailable(username);
 		if (!existsUser && key.equals(user.getConfirmKey())) {
 			user.confirm();
 			flash.success("user.confirm.success");
 			try {
 				Secure.login();
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		} else {
 			flash.error("user.confirm.error");
 			index(0);
 		}
 	}
 
 	/**
 	 * Generates a random captcha-image for a new user to confirm when he/she is
 	 * registering for an account (before even a confirmation e-mail is sent).
 	 * 
 	 * @param id
 	 *            a random ID identify a specific CAPTCHA
 	 */
 	public static void captcha(String id) {
 		Images.Captcha captcha = Images.captcha();
 		String code = captcha.getText("#ff8400");
		Cache.set(id, "captcha." + code, "3mn");
 		renderBinary(captcha);
 	}
 
 }

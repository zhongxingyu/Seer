 package controllers;
 
 import java.util.*;
 
 import javax.swing.text.Utilities;
 
 import play.mvc.*;
 import play.*;
 import play.data.validation.*;
 import play.data.validation.Error;
 import models.*;
 import notifiers.*;
 import utilities.*;
 
 /**
  * This controller holds all of the methods and functions associated with the
  * main functions of the application, including submitting and answering
  * questions, and generating the statistics pages.
  * 
  * @author Nick Barnwell
  * 
  */
 
 public class Application extends Controller {
 	/**
 	 * Searches for a question in the database and then returns it to the
 	 * question template in views/application/question.html
 	 * 
 	 * @param id
 	 *            The id of the question being searched for in the database.
 	 *            Refers to the primary key of the Question in question.
 	 */
 	public static void question(Long id) {
 		Question question = Question.findById(id);
 		render(question);
 	}
 
 	/**
 	 * Generates the statistics page sorted by number of words written in
 	 * response to questions and answers.
 	 * 
 	 * The function works by first creating a HashMap/dictionary of type User,
 	 * Integer. Then a list of all users is fetched from the database and
 	 * iterated through, adding each user to the dictionary. For each User, a
 	 * list of all their questions and answers are added, and then iterated
 	 * through as well. For each question and answer, the
 	 * {@link models.Question} wordcount is found, and added to to count kept in
 	 * the dictionary. Once this is finished, the dictionary is iterated through
 	 * and each User:Key pair inserted into a Binary Tree of type
 	 * {@link utilities.Pair}<User, Integer>, sorted in-order and returned as an
 	 * ArrayList collection to be rendered by the template
 	 * Application/stats.html
 	 * 
 	 * @param id
 	 *            The id of the question to be displayed
 	 */
 	public static void questionStats(String setSelf, String setRecipient) {
 		HashMap<User, Integer> results = new HashMap<User, Integer>();
 		List<User> users = User.all().fetch(); // Fetch all users
 		for (User u : users) {
 			results.get(u);
 			List<Question> questions = Question.find("byAuthor", u).fetch();
 			for (Question q : questions) {
 				Integer count = results.get(u);
 				results.put(u, (count == null) ? q.wordcount : count
 						+ q.wordcount); // Ternary operator to avoid NPEs
 			}
 		}
 		BTree<Pair<User, Integer>> tree = new BTree();
 		for (User u : users) {
 			Pair<User, Integer> pair = new Pair(u, results.get(u));
 			tree.insert(pair);
 		}
 		ArrayList<Pair<User, Integer>> res = tree.sortInOrder();
 
 		render("Application/stats.html", res);
 
 	}
 
 	/**
 	 * Method that approves questions and answers. Users click a link in an
 	 * email with a one-time use URL to approve.
 	 * 
 	 * The first step in determining whether to approve something is to check
 	 * whether it is a question or an answer. Questions' hashes start with the
 	 * letter 'q', whilst Answers' start with 'a'. The hash is substringed and
 	 * then dispatched to the appropriate processor for handling. The algorithm
 	 * used in both instances is identical, but to avoid having to cast it has
 	 * been split into an if/else structure.
 	 * <p>
 	 * The first step is to try and find the appropriate item to be edited. This
 	 * is done through the ORM, checking if there are any items with a matching
 	 * approval key. If so, it is checked if they have been approved yet. If
 	 * not, it is then to approved, and the record is updated. The flash message
 	 * is set to "Your question/ answer has been approved", and they are
 	 * redirected to view the item. Otherwise, they are just redirected to the
 	 * item.
 	 * 
 	 * @param key
 	 *            The key is passed as a querystring in an HTTP GET. The router
 	 *            then handles passing this to the approval function. Should be
 	 *            an 129 bit MD5 hash
 	 */
 	public static void approve(String key) {
 		if (key.substring(0, 1).equals("q")) {
 			try {
 				Question question = Question.find("byApprovalKey", key).first();
 				if (question.approval != 1) {
 					question.approval = 1;
 					question.save();
 					flash.success("Your question has been approved");
 					Mails.notifyQuestion(question);
 					question(question.id);
 				} else {
 					question(question.id);
 				}
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 		} else if (key.substring(0, 1).equals("a")) {
 			try {
 				Answer answer = Answer.find("byApprovalKey", key).first();
 				if (answer.approval != 1) {
 					answer.approval = 1;
 					answer.save();
 					question(answer.question.id);
 				} else {
 					question(answer.question.id);
 				}
 			} catch (Exception e) {
 				System.out.println("No such amswer");
 			}
 		}
 	}
 
 	/**
 	 * This method exists only to serve as a redirect to the proper submission
 	 * form. This is because the framework does not promote checking what method
 	 * has been called and presenting a form, then a different action when
 	 * POSTing.
 	 */
 	public static void submitForm() {
 		render("Application/submit.html");
 	}
 
 	/**
 	 * The method that is called when a POST is made to $appUrl/submit. This is
 	 * used for submitting questions
 	 * 
 	 * This function works by finding hte author and the reciever by their full
 	 * names as listed in the database. Next, it checks if either the author or
 	 * the receiver have been not found, and if so causes a Flash error to
 	 * display and the form to be rerendered, as if a validation had failed.
 	 * <p>
 	 * Otherwise, it will create a new question with the specified parameters
 	 * 
 	 * @param author
 	 *            The name of the author as specified in the "Author" field on
 	 *            the form
 	 * @param receiver
 	 *            The name of the user to receive the question as specified in
 	 *            the "receiver" field on the form
 	 * @param content
 	 *            The text to be saved with the question as specified in the
 	 *            Text field of the form
 	 */
 	public static void submit(
 			@Required(message = "Your name should be here.") String author,
 			@Required(message = "You need to enter a recipient's name!") String receiver,
 			@Required(message = "A question is required") String content) {
 		if (validation.hasErrors()) {
 			params.flash(); // add http parameters to the flash scope
 			validation.keep(); // keep the errors for the next request
 			submitForm();
 		}
 		User uAuthor = User.find("byFullname", author).first();
 		User uReceiver = User.find("byFullname", receiver).first();
 		if (uAuthor == (null) || uReceiver == (null)) {
 			flash.error("User not found, please try again");
 			render("Application/submit.html");
 		} else {
 			Question submission = new Question(content, uAuthor, uReceiver)
 					.save();
 			flash.success("Thanks for posting %s", submission.author.fullname);
 			Mails.approveQuestion(submission);
 			question(submission.id);
 		}
 	}
 
 	/**
 	 * This method is called when a POST is made on an approved question
 	 * 
 	 * @param questionID
 	 *            ID of question the answer is attached to
 	 * @param content
 	 *            The body of the answer
 	 */
	public static void answerQuestion(
			@Required(message = "You need a questionID") Long questionID,
			@Required(message = "You need a proper answer!") String content) {
 		Question question = Question.findById(questionID);
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			question(questionID);
		}
 		Answer answer = question.addAnswer(question.receiver, content);
 		Mails.approveAnswer(answer);
 		question(questionID);
 
 	}
 }

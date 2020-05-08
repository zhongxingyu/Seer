 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.FootNote;
 import models.Paragraph;
 import models.Post;
 import models.Reading;
 import models.Thread;
 import models.User;
 
 import org.parsit.IFootNote;
 
 import play.Logger;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
 import utils.Helper;
 import utils.Pagination;
 
 /**
  * This controller defines actions related to threads and messages
  * @author julien
  *
  */
 @With(Dbtit.class)
 public class Debate extends Controller {
     /**
      * Display a thread
      * @param id
      */
     public static void thread(String hash) {
     	Thread thread = Thread.find("byHash", hash).first();
     	notFoundIfNull(thread);
     	Post post = thread.rootPost;
     	Reading reading;
     	Date lastReading;
     	Set<Paragraph> paragraphsToShow = new HashSet<Paragraph>();
     	List<FootNote> footNotes = new ArrayList<FootNote>();
     	
     	User user = Dbtit.connectedUser();
     	if (user != null) {
 			reading = user.lastReading(thread);
 			lastReading = reading.date;
     	} else {
     		reading = null;
     		lastReading = Helper.getMinDate();
     	}
     	thread.getAnsweredParagraphsBefore(lastReading, reading, paragraphsToShow, footNotes);
     	
     	/*if (params._contains("s")) {
     		String h = params.get("s");
     		String[] paragraphs = h.split("\\.");
     		if (paragraphs != null) {
     			for (String pId : paragraphs) {
     				Paragraph p = Paragraph.findById(Long.parseLong(pId));
 	    			if (p != null) {
 	    				paragraphsToShow.add(p);
 	    			}
 	    		}
     		}
     	}
     	if (params._contains("h")) {
     		String h = params.get("h");
     		String[] paragraphs = h.split("\\.");
     		if (paragraphs != null) {
 	    		for (String pId : paragraphs) {
 	    			Paragraph p = Paragraph.findById(Long.parseLong(pId));
 	    			if (p != null) {
 	    				paragraphsToShow.remove(p);
 	    			}
 	    		}
     		}
     	}*/
 
     	render(thread, post, lastReading, paragraphsToShow, footNotes);
     }
     
     /**
      * Display a thread starting from a specific post (possibly not the root post)
      */
     public static void branch(String hash, Long paragraphId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (paragraph.post.thread != thread)
     		notFound();
     	
     	Reading reading;
     	Date lastReading;
     	Set<Paragraph> paragraphsToShow = new HashSet<Paragraph>();
     	List<FootNote> footNotes = new ArrayList<FootNote>();
     	
     	User user = Dbtit.connectedUser();
     	if (user != null) {
 			reading = user.lastReading(thread);
 			lastReading = reading.date;
     	} else {
     		reading = null;
     		lastReading = Helper.getMinDate();
     	}
    	paragraph.getParagraphAndAnswersBefore(lastReading, reading, paragraphsToShow, footNotes);
     	
     	render(thread, paragraph, lastReading, paragraphsToShow, footNotes);
     }
     
     /**
      * Return the answers of a given paragraph of a thread
      * (This operation is currently used by an AJAX call to show posts ansers)
      * @param hash Thread hash
      * @param paragraphId
      */
     public static void answers(String hash, Long paragraphId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (paragraph.post.thread != thread)
     		notFound();
     	
     	Date lastReading;
     	Reading reading;
     	Set<Paragraph> paragraphsToShow = new HashSet<Paragraph>();
     	List<FootNote> footNotesList = new ArrayList<FootNote>();
     	
     	User user = Dbtit.connectedUser();
     	if (user != null) {
     		reading = user.lastReading(thread);
     		lastReading = reading.date;
     	} else {
     		reading = null;
     		lastReading = paragraph.post.date;
     	}
     	paragraph.getAnsweredParagraphsBefore(lastReading, reading, paragraphsToShow, footNotesList);
     	
     	render(thread, paragraph, lastReading, paragraphsToShow, footNotesList);
     }
     
     /**
      * Display a form to create a thread
      */
     @Authenticated
     public static void newThread() {
     	render();
     }
     
     /**
      * Creates a thread and display it
      * @param title
      * @param content
      */
     @Authenticated
     public static void createThread(@Required String threadTitle, @Required String content) {
     	User user = Dbtit.connectedUser();
     	
     	if (validation.hasErrors()) {
     		render("@newThread", threadTitle, content);
     	}
     	
     	if (params._contains("preview")) {
     		List<String> paragraphs = new ArrayList<String>();
     		List<IFootNote> footNotes = new ArrayList<IFootNote>();
     		Post.preview(content, paragraphs, footNotes);
     		renderArgs.put("preview", true);
     		render("@newThread", threadTitle, content, paragraphs, footNotes);
     	}
     	
     	Thread thread = Thread.create(user, threadTitle, content);
     	flash.success(Messages.get("threadCreated", thread.title));
     	Logger.info("Thread (%s) created by user %s (%s)", thread.id, user.name, user.id);
     	
     	thread(thread.hash);
     }
     
     /**
      * Diplay the reply form
      * @param thread
      * @param post
      */
     @Authenticated
     public static void reply(String hash, Long paragraphId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (paragraph.post.thread != thread)
     		notFound();
     	
     	render(thread, paragraph);
     }
     
     /**
      * Add a reply to a post and display the thread
      * @param threadId
      * @param paragraphId
      * @param content
      */
     @Authenticated
     public static void postReply(String hash, Long paragraphId, @Required String content) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	User author = Dbtit.connectedUser(); // author can't be null thx to the @Authenticated annotation
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (paragraph.post.thread != thread)
     		notFound();
     	
     	if (validation.hasErrors()) {
     		render("@reply", thread, paragraph, content);
     	}
     	
     	if (params._contains("preview")) {
     		List<String> paragraphs = new ArrayList<String>();
     		List<IFootNote> footNotes = new ArrayList<IFootNote>();
     		Post.preview(content, paragraphs, footNotes);
     		renderArgs.put("preview", true);
     		render("@reply", thread, paragraph, content, paragraphs, footNotes);
     	}
     	
     	Post reply = paragraph.reply(author, content);
     	flash.success(Messages.get("postAdded"));
     	Logger.info("Reply (%s) posted to paragraph (%s) by user %s (%s)", reply.id, paragraph.id, author.name, author.id);
     	
     	thread(thread.hash);
     }
     
     /**
      * Display a page with a form allowing to edit a post
      * @param hash
      * @param postId
      */
     @Authenticated
     public static void edit(String hash, Long postId)
     {
     	Thread thread = Thread.find("byHash", hash).first();
     	Post post = Post.findById(postId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(post);
     	if (post.hasAnswers() || post.thread != thread)
     		notFound(); // On ne modifie pas un post qui a déjà reçu des réponses (ou qui fait partie d’un autre thread)
     	
     	String content = post.content;
     	
     	render(thread, post, content);
     }
     
     /**
      * Handle the editing of a post
      * @param hash
      * @param postId
      * @param content
      */
     @Authenticated
     public static void postEdit(String hash, Long postId, @Required String content)
     {
     	Thread thread = Thread.find("byHash", hash).first();
     	Post post = Post.findById(postId);
     	User author = Dbtit.connectedUser();
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(post);
     	if (post.thread != thread || post.author != author)
     		notFound();
     	
     	if (post.hasAnswers()) {
     		boolean postReplied = true;
     		render("@edit", thread, post, content, postReplied);
     	}
     	
     	if (validation.hasErrors()) {
     		render("@edit", thread, post, content);
     	}
     	
     	if (params._contains("preview")) {
     		List<String> paragraphs = new ArrayList<String>();
     		List<IFootNote> footNotes = new ArrayList<IFootNote>();
     		Post.preview(content, paragraphs, footNotes);
     		renderArgs.put("preview", true);
     		render("@edit", thread, post, content, paragraphs, footNotes);
     	}
     	
     	post.edit(content);
     	flash.success(Messages.get("postUpdated"));
     	Logger.info("Post (%s) edited by user %s (%s)", post.id, author.name, author.id);
     	
     	thread(thread.hash);
     }
     
     /**
      * Make a user follow a thread
      * @param hash
      * @param url
      */
     @Authenticated
     public static void followThread(String hash, String url) {
     	Thread thread = Thread.find("byHash", hash).first();
     	User user = Dbtit.connectedUser();
     	
     	notFoundIfNull(thread);
     	
     	user.follow(thread);
     	Logger.info("Thread (%s) followed by user %s (%s)", thread.id, user.name, user.id);
     	
     	redirect(url);
     }
     
     /**
      * Make a use stop following a thread
      * @param hash
      * @param url
      */
     @Authenticated
     public static void doNotFollowThread(String hash, String url) {
     	Thread thread = Thread.find("byHash", hash).first();
     	User user = Dbtit.connectedUser();
     	
     	notFoundIfNull(thread);
     	
     	user.doNotFollow(thread);
     	Logger.info("Thread (%s) stopped being followed by user %s (%s)", thread.id, user.name, user.id);
     	
     	redirect(url);
     }
     
     /**
      * Display the following threads page for the current user
      */
     @Authenticated
     public static void followThreads() {
     	User user = Dbtit.connectedUser();
     	
     	String pageVar = "p";
     	int currentPage = 1;
     	if (params._contains(pageVar))
     		currentPage = params.get(pageVar, Integer.class);
     	Pagination pagination = new Pagination(user.followedThreads.size(), currentPage, 10, pageVar);
     	
     	List<Thread> followedThreads = user.followedThreads.subList(pagination.getCurrentOffset(), pagination.getCurrentLimit());
     	
     	render(followedThreads, pagination);
     }
 }

 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Paragraph;
 import models.Post;
 import models.Room;
 import models.Thread;
 import models.User;
 
 import org.parsit.IFootNote;
 
 import play.Logger;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
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
      * @param hash Thread hash
      */
     public static void showThread(String hash) {
     	Thread thread = Thread.find("byHash", hash).first();
     	notFoundIfNull(thread);
     	Post post = thread.rootPost;
     	
     	render(thread, post);
     }
     
     /**
      * Display a thread starting from a specific post (possibly not the root post)
      */
     public static void branch(String hash, Long paragraphId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (!paragraph.post.thread.id.equals(thread.id))
     		notFound();
     	
     	render(thread, paragraph);
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
     	if (!paragraph.post.thread.id.equals(thread.id))
     		notFound();
     	
     	render(thread, paragraph);
     }
     
     @LoggedIn
     public static void readPost(String hash, Long postId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	notFoundIfNull(thread);
     	
     	Post post = Post.findById(postId);
     	notFoundIfNull(post);
     	
     	if (!post.thread.id.equals(thread.id))
     		notFound();
     	
     	User user = Dbtit.connectedUser();
     	user.read(post);
     	Logger.info("Post (%s) read by user (%s)", post.id, user.id);
     }
     
     @LoggedIn
     public static void unreadPost(String hash, Long postId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	notFoundIfNull(thread);
     	
     	Post post = Post.findById(postId);
     	notFoundIfNull(post);
     	
     	if (!post.thread.id.equals(thread.id))
     		notFound();
     	
     	User user = Dbtit.connectedUser();
     	user.unread(post);
     	Logger.info("Post (%s) marked as unread by user (%s)", post.id, user.id);
     }
     
     /**
      * Display a form to create a thread
      */
     @LoggedIn
     public static void newThread(String hash) {
     	Room room = Room.find("byHash", hash).first();
     	notFoundIfNull(room);
     	
     	render(room);
     }
     
     /**
      * Creates a thread and display it
      * @param title
      * @param content
      */
     @LoggedIn
     public static void createThread(@Required String hash, @Required String threadTitle, @Required String content) {
     	Room room = Room.find("byHash", hash).first();
     	notFoundIfNull(room);
     	
     	User user = Dbtit.connectedUser();
     	
     	if (validation.hasErrors()) {
     		render("@newThread", room, threadTitle, content);
     	}
     	
     	if (params._contains("preview")) {
     		List<String> paragraphs = new ArrayList<String>();
     		List<IFootNote> footNotes = new ArrayList<IFootNote>();
     		Post.preview(content, paragraphs, footNotes);
     		renderArgs.put("preview", true);
     		render("@newThread", room, threadTitle, content, paragraphs, footNotes);
     	}
     	
     	Thread thread = Thread.create(user, room, threadTitle, content);
     	flash.success(Messages.get("threadCreated", thread.title));
     	Logger.info("Thread (%s) created by user %s (%s)", thread.id, user.name, user.id);
     	
     	showThread(thread.hash);
     }
     
     /**
      * Diplay the reply form
      * @param thread
      * @param post
      */
     @LoggedIn
     public static void reply(String hash, Long paragraphId) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (!paragraph.post.thread.id.equals(thread.id))
     		notFound();
     	
     	render(thread, paragraph);
     }
     
     /**
      * Add a reply to a post and display the thread
      * @param threadId
      * @param paragraphId
      * @param content
      */
     @LoggedIn
     public static void postReply(String hash, Long paragraphId, @Required String content) {
     	Thread thread = Thread.find("byHash", hash).first();
     	Paragraph paragraph = Paragraph.findById(paragraphId);
     	User author = Dbtit.connectedUser(); // author can't be null thx to the @LoggedIn annotation
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(paragraph);
     	if (!paragraph.post.thread.id.equals(thread.id))
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
     	
     	showThread(thread.hash);
     }
     
     /**
      * Display a page with a form allowing to edit a post
      * @param hash
      * @param postId
      */
     @LoggedIn
     public static void edit(String hash, Long postId)
     {
     	Thread thread = Thread.find("byHash", hash).first();
     	Post post = Post.findById(postId);
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(post);
     	if (post.hasAnswers() || !post.thread.id.equals(thread.id))
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
     @LoggedIn
     public static void postEdit(String hash, Long postId, @Required String content)
     {
     	Thread thread = Thread.find("byHash", hash).first();
     	Post post = Post.findById(postId);
     	User author = Dbtit.connectedUser();
     	
     	notFoundIfNull(thread);
     	notFoundIfNull(post);
     	if (!post.thread.id.equals(thread.id) || !post.author.id.equals(author.id))
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
     	
     	showThread(thread.hash);
     }
     
     /**
      * Make a user follow a thread
      * @param hash
      * @param url
      */
     @LoggedIn
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
     @LoggedIn
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
     @LoggedIn
     public static void followThreads() {
     	User user = Dbtit.connectedUser();
    	Pagination pagination = new Pagination(params, 10);
     	List<Thread> followedThreads = user.followedThreads.subList(pagination.getFrom(), Math.min(pagination.getTo(), user.followedThreads.size()));
     	
     	render(followedThreads, pagination);
     }
 }

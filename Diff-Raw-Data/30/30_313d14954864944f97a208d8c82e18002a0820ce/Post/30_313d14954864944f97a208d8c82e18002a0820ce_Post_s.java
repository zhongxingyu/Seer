 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 
 import org.parsit.IFootNote;
 import org.parsit.Parsit;
 import org.parsit.SimpleFootNoteFactory;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Post extends Model
 {
 	/** Author of the post */
 	@Required
 	@ManyToOne
 	public User author;
 	
 	/** Date when the message was posted */
 	@Required
 	public Date date;
 	
 	/** The paragraph that this post answers */
 	@ManyToOne
 	public Paragraph parent;
 	
 	/** Raw content of the whole post (in Parsit syntax) */
 	@MaxSize(10000)
 	@Lob
 	public String content;
 	
 	/** The paragraphs of this post */
 	@OneToMany(mappedBy="post", cascade=CascadeType.REMOVE)
 	@OrderBy("number ASC")
 	public List<Paragraph> paragraphs;
 	
 	/** The thread that this post belongs to */
 	@Required
 	@ManyToOne
 	public Thread thread;
 
 	/**
 	 * Protected constructor since the static helper should be used for better consistency
 	 * @param author
 	 * @param content
 	 * @param parent
 	 */
 	public Post(User author, Paragraph parent, Thread thread, Date date) // FIXME Add a content parameter?
 	{
 		this.thread = thread;
 		this.author = author;
 		this.parent = parent;
 		this.date = date;
 		this.paragraphs = new ArrayList<Paragraph>();
 	}
 	
 	/**
 	 * Convenient function to create a post.
 	 * @param author Author of the  post
 	 * @param content Content of the post
 	 * @param parent Parent post
 	 * @return The created post
 	 */
 	public static Post create(User author, String content, Paragraph parent, Thread thread)
 	{
 		Post post = new Post(author, parent, thread, new Date()).save(); // The save is needed because the paragraphs about to be created will reference this post
 		post.setParagraphs(content);
 		return post;
 	}
 	
 	@Override
 	public String toString() {
 		String content = paragraphs.get(0).content; // A post must have at least one paragraph
 		return content.substring(0, Math.min(content.length(), 40));
 	}
 	
 	// TODO make this method non static
 	public static void preview(String content, List<String> paragraphs, List<IFootNote> footNotes) {
 		Parsit parsit = new Parsit(new SimpleFootNoteFactory());
 		for (String p : parsit.parseParagraphs(content)) {
 			paragraphs.add(p);
 		}
 		for (IFootNote footNote : parsit.getFootNotes()) {
 			footNotes.add(footNote);
 		}
 	}
 	
 	/**
 	 * Change the content of the post
 	 * @param content
 	 */
 	public void edit(String content) {
 		setParagraphs(content);
 	}
 	
 	/**
 	 * Change the content of the post
 	 * The actual paragraphs will be removed and new paragraphs will
 	 * be created from the content.
 	 * @param content New content.
 	 */
 	private void setParagraphs(String content) {
 		this.content = content;
 		
 		// Remove current paragraphs and footnotes
 		for (Paragraph paragraph : paragraphs) {
 			paragraph.delete();
 		}
 		paragraphs.clear();
 		
 		// Parse content
 		Parsit parsit = new Parsit(new FootNote.Factory());
 		String[] parsedParagraphs = parsit.parseParagraphs(content);
 		
 		// Set new paragraphs
 		int number = 0;
 		for (String p : parsedParagraphs) {
 			Paragraph paragraph = new Paragraph(this, p, number++).save();
 			for (IFootNote footNote : parsit.getFootNotes()) {
 				paragraph.addFootNote((FootNote)footNote); // HACK But I know I gave a FootNote.Factory a few lines above
 			}
 			paragraphs.add(paragraph);
 		}
 		this.save();
 	}
 	
 	/**
 	 * Check if this post has answers
 	 * @return true if at least one of its paragraph has at least one answer.
 	 */
 	public boolean hasAnswers()
 	{
     	for (Paragraph paragraph : paragraphs) {
     		if (paragraph.answers.size() != 0)
     			return true;
     	}
 		return false;
 	}
 	
 	/**
 	 * TODO TESTS and *optimization*
 	 * @param deadline
 	 * @param paragraphs
 	 */
 	public void getAnsweredParagraphsBefore(Date lastReading, Reading reading, Set<Paragraph> paragraphs, List<FootNote> footNotesList) {
		for (Paragraph p : this.paragraphs) {
 			footNotesList.addAll(p.footNotes);
		}
 		if (lastReading.compareTo(date) >= 0) {
 			for (Paragraph paragraph : this.paragraphs) {
 				paragraph.getAnsweredParagraphsBefore(lastReading, reading, paragraphs, footNotesList);
 			}
 		}
 	}
 
 }

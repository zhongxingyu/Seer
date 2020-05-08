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
 
 import play.data.validation.MaxSize;
 import play.db.jpa.Model;
 
 @Entity
 public class Paragraph extends Model {
 
 	/** HTML content of the paragraph */
 	@MaxSize(1000)
 	@Lob
 	public String content;
 	
 	/** Answers of the paragraph */
 	@OneToMany(mappedBy="parent", cascade=CascadeType.REMOVE)
 	public List<Post> answers;
 	
 	/** Footnotes referenced by this post */
 	@OneToMany(mappedBy="paragraph", cascade=CascadeType.REMOVE)
 	public List<FootNote> footNotes;
 	
 	/** Post which this paragraph belongs to */
 	@ManyToOne
 	public Post post;
 	
 	/** Number of the post */
 	public Integer number;
 	
 	public Paragraph(Post post, String content, Integer number)
 	{
 		this.post = post;
 		this.content = content;
 		this.answers = new ArrayList<Post>();
 		this.footNotes = new ArrayList<FootNote>();
 		this.number = number;
 	}
 	
 	@Override
 	public String toString() {
 		return content.substring(0, Math.min(content.length(), 40));
 	}
 	
 	/**
 	 * Reply to this paragraph
 	 * @param author Author of the reply
 	 * @param content Content of the reply post
 	 */
 	public Post reply(User author, String content) {
 		Post post = Post.create(author, content, this, this.post.thread);
 		this.answers.add(post);
 		author.follow(post.thread);
 		return post;
 	}
 
 	/**
 	 * Convenient function to add a footnote to a paragraph
 	 */
 	public void addFootNote(FootNote footNote) {
 		footNote.paragraph = this;
 		footNotes.add(footNote);
 		footNote.save();
 	}
 	
 	public void getAnsweredParagraphsBefore(Date lastReading, Reading reading, Set<Paragraph> paragraphs, List<FootNote> footNotesList) {
 		if (answers.size() != 0) {
 			paragraphs.add(this); // This paragraph contains answers, add it to the list of answered paragraphs
 			for (Post answer : answers) {
 				answer.getAnsweredParagraphsBefore(lastReading, reading, paragraphs, footNotesList);
 				if (reading != null) { // This answer is after the last post the user has already read
 					reading.updateDate(answer.date);
 				}
 			}
 		}
 	}
 }

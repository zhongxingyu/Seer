 package models;
 
 import com.avaje.ebean.Ebean;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.libs.Yaml;
 import play.test.WithApplication;
 
 import java.util.*;
 
 import static org.junit.Assert.*;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.inMemoryDatabase;
 
 public class CommentsTest extends WithApplication {
 
 	@Before
 	public void setUp() {
 		start(fakeApplication(inMemoryDatabase()));
 		Ebean.save((List) Yaml.load("test-data.yml"));
 	}
 
 	@Test
 	public void useTheCommentsRelation() {
		Note.create(new Note("My note"));
 		Note note = Note.find.where().eq("title", "My note").findUnique();
 
 		note.addComment("Jeff", "Nice post");
 		note.addComment("Tom", "Awesome");
 
 		// assertEquals(2, Comment.count());
 
 		assertEquals(2, note.comments.size());
 		assertEquals("Jeff", note.comments.get(0).author);
 
 		note.delete();
 
 		// assertEquals(0, Comment.count());
 	}
 
 	@Test
 	public void createComment() {
 		Note.create(new Note("My note", User.findByEmail("test@notes.com")));
     	Note note = Note.find.where().eq("title", "My note").findUnique();
 
 		new Comment(note, "Jeff", "Nice post").save();
 		new Comment(note, "Tom", "Awesome").save();
 
 		List<Comment> commentsOnNote = note.comments;
 
 		assertEquals(2, commentsOnNote.size());
 
 		Comment firstComment = commentsOnNote.get(0);
 		assertNotNull(firstComment);
 		assertEquals("Jeff", firstComment.author);
 		assertEquals("Nice post", firstComment.content);
 		assertNotNull(firstComment.postedAt);
 
 		Comment secondComment = commentsOnNote.get(1);
 		assertNotNull(secondComment);
 		assertEquals("Tom", secondComment.author);
 		assertEquals("Awesome", secondComment.content);
 		assertNotNull(secondComment.postedAt);
 	}
 }

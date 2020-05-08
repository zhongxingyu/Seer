 package org.test.bugtracker.impl.bo;
 
import static org.testng.Assert.*;
 
 import org.hibernate.PropertyValueException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.test.bugtracker.bo.BugBO;
 import org.test.bugtracker.bo.CommentBO;
 import org.test.bugtracker.bo.UserBO;
 import org.test.bugtracker.impl.model.BugImpl;
 import org.test.bugtracker.impl.model.CommentImpl;
 import org.test.bugtracker.impl.model.UserImpl;
 import org.test.bugtracker.model.Bug;
 import org.test.bugtracker.model.Comment;
 import org.test.bugtracker.model.User;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations = { "classpath:spring/context.xml" })
 public class CommentImplTest extends AbstractTestNGSpringContextTests {
     private static final String MESSAGE = "message";
     private static final String USER_NAME = CommentImplTest.class.getSimpleName();
 
     @Autowired
     private ApplicationContext context;
     @Autowired
     private UserBO userBO;
     @Autowired
     private BugBO bugBO;
     @Autowired
     private CommentBO commentBO;
     
     private User user;
     private Bug bug;
     
     @BeforeClass
     private void setup() {
         user = new UserImpl();
         user.setLogin(USER_NAME);
         user.setPass("pass");
         userBO.save(user);
         assertNotNull(user.getId());
         bug = new BugImpl();
         bug.setTitle("title");
         bug.setMessage(MESSAGE);
         bug.setAuthor(user);
         bugBO.save(bug);
         assertNotNull(bug.getId());
     }
 
     @BeforeMethod
     public void cleanupComments() {
         bug.setComments(null);
         bugBO.save(bug);
     }
     
     @Test
     public void createNewComment() {
         Comment comment = new CommentImpl();
         comment.setMessage(MESSAGE);
         comment.setAuthor(user);
         comment.setBug(bug);
         commentBO.save(comment);
         assertNotNull(comment.getId());
         Comment comment2 = commentBO.findById(comment.getId());
         assertNotNull(comment2);
         assertEquals(MESSAGE,comment2.getMessage());
     }
     
     @Test(expectedExceptions={PropertyValueException.class})
     public void createNewCommentWithoutAuthor() {
         Comment comment = new CommentImpl();
         comment.setMessage(MESSAGE);
         comment.setBug(bug);
         commentBO.save(comment);
     }
 
     @Test(expectedExceptions={PropertyValueException.class})
     public void createNewCommentWithoutBug() {
         Comment comment = new CommentImpl();
         comment.setMessage(MESSAGE);
         comment.setAuthor(user);
         commentBO.save(comment);
     }
     
     @Test(expectedExceptions={PropertyValueException.class})
     public void createNewCommentWithoutMessage() {
         Comment comment = new CommentImpl();
         comment.setBug(bug);
         comment.setAuthor(user);
         commentBO.save(comment);
     }
     
     @Test
     public void updateComment() {
         Comment comment = new CommentImpl();
         comment.setMessage(MESSAGE);
         comment.setAuthor(user);
         comment.setBug(bug);
         commentBO.save(comment);
         Long id = comment.getId();
         assertNotNull(id);
         Comment comment2 = commentBO.findById(id);
         assertNotNull(comment2);
         assertEquals(MESSAGE,comment2.getMessage());
         String message2 = MESSAGE+"2";
         comment2.setMessage(message2);
         commentBO.update(comment2);
         Comment comment3 = commentBO.findById(id);
         assertNotNull(comment3);
         assertEquals(message2,comment3.getMessage());
         
     }
     
     @Test()
     public void deleteComment() {
         Comment comment = new CommentImpl();
         comment.setMessage(MESSAGE);
         comment.setAuthor(user);
         comment.setBug(bug);
         commentBO.save(comment);
         Long id = comment.getId();
         assertNotNull(id);
         Comment comment2 = commentBO.findById(id);
         assertEquals(MESSAGE,comment2.getMessage());
         commentBO.delete(comment2);
         assertNull(commentBO.findById(id));
     }
 }

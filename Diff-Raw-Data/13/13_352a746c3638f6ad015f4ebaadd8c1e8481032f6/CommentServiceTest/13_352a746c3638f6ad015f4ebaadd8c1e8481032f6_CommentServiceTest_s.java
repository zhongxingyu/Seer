 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Comment;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 public class CommentServiceTest {

     @Test
     public void shouldAddNewCommentInDatabase(){
         CommentService commentService = new CommentService();
         Comment comment = commentService.addNewComment(1, "mib","AddNewComment");
         assertEquals("mib",comment.name);
     }
 
     @Test(expected = RuntimeException.class)
     public void  shouldNotInsertTheCommentIfMessageIsAEmptyString(){
         CommentService commentService = new CommentService();
         Comment comment = commentService.addNewComment(1, "EmptyComment","");
     }

     @Test
     public void shouldInsertCommentWithNameAsAnonymousIfNameIsEmptyString(){
         CommentService commentService = new CommentService();
         Comment comment = commentService.addNewComment(1, "","EmptyName");
         assertEquals("anonymous",comment.name);
     }
 
     @Test (expected = RuntimeException.class)
     public void shouldThrowExceptionIfBoardIdIsNull(){
         CommentService commentService = new CommentService();
         Comment comment = commentService.addNewComment(null, "","Board ID Null");
     }
 
     @Test
     public void shouldReturnTotalNumberOfCommentsForABoard(){
         CommentService commentService = new CommentService();
         int boardID = 99;
         commentService.deleteComments(boardID);
         commentService.addNewComment(boardID, "mib","Should Return no. of comments");
         Integer numberOfComments=commentService.getCommentsCount(99);
         assertEquals( 1,numberOfComments.intValue());
     }
 
     @Test
     public void shouldDeleteAllCommentsForABoard(){
         CommentService commentService = new CommentService();
         int boardID = 9;
         commentService.deleteComments(boardID);
         Integer returnedCommentCount=commentService.getCommentsCount(boardID);
         assertEquals(0,returnedCommentCount.intValue());
     }
     @Test
     public void shouldGetCommentsCountForOneIdeaboard(){
         int boardID = 1;
         CommentService commentService = addComments(boardID);
         List<Comment> comments = commentService.getAllComments(boardID);
         assertEquals(3,comments.size());
     }
 
     private CommentService addComments(int boardID) {
         CommentService commentService = new CommentService();
         commentService.deleteComments(boardID);
         commentService.addNewComment(boardID, "test","comment1");
         commentService.addNewComment(boardID, "test","comment2");
         commentService.addNewComment(boardID, "test","comment3");
         return commentService;
     }
 
     @Test
     public void shouldGetAllCommentsOfABoard(){
         int boardID = 1;
         CommentService commentService = new CommentService();
         List<Comment> theseComments = new ArrayList<Comment>();
         insertComments(boardID, commentService, theseComments);
 
         List<Comment> comments = commentService.getAllComments(boardID);
 
         assertEquals(theseComments,comments);
     }
 
     private void insertComments(int boardID, CommentService commentService, List<Comment> theseComments) {
         commentService.deleteComments(boardID);
         theseComments.add(commentService.addNewComment(boardID, "test", "comment1"));
         theseComments.add(commentService.addNewComment(boardID, "test","comment2"));
         theseComments.add(commentService.addNewComment(boardID, "test","comment3"));
     }
 }
 
 

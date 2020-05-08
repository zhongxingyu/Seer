 package controllers;
 
 import models.Comment;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 
 import views.html.*;
 
 import java.util.List;
 
 import static play.data.Form.form;
 
 public class Application extends Controller {
     static final Form<Comment> commentForm = form(Comment.class);
 
     public static Result index() {
         return ok(index.render("TESTING"));
     }
 
     public static Result game() {
        List<Comment> allComments = Comment.getCommentsNewestFirst();
         return ok(game.render(allComments, commentForm));
     }
 
     public static Result newComment() {
         Form<Comment> filledCommentForm = commentForm.bindFromRequest();
        List<Comment> allComments;
         if(filledCommentForm.hasErrors()) {
            allComments = Comment.getCommentsNewestFirst();
             return badRequest(game.render(allComments, commentForm));
         } else {
             Comment comment = filledCommentForm.get();
             comment.save();
            allComments = Comment.getCommentsNewestFirst();
             return ok(game.render(allComments, commentForm));
         }
     }
   
 }

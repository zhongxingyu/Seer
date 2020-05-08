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
        List<Comment> allComments = Comment.find.orderBy("postDate desc").findList();
         return ok(game.render(allComments, commentForm));
     }
 
     public static Result newComment() {
         Form<Comment> filledCommentForm = commentForm.bindFromRequest();
        List<Comment> allComments = Comment.find.orderBy("postDate desc").findList();
         if(filledCommentForm.hasErrors()) {
             return badRequest(game.render(allComments, commentForm));
         } else {
             Comment comment = filledCommentForm.get();
             comment.save();
             return ok(game.render(allComments, commentForm));
         }
     }
   
 }

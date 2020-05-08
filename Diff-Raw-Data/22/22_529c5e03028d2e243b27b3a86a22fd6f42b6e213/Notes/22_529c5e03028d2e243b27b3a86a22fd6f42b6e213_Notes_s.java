 package controllers;
 
 import java.util.*;
 import play.mvc.*;
 import play.data.*;
 import play.Logger;
 import models.*;
 import views.html.*;
 import securesocial.core.java.SecureSocial;
 import securesocial.core.Identity;
 
 public class Notes extends Controller {
 
 	static Form<Note> noteForm = Form.form(Note.class);
 	static Form<Comment> commentForm = Form.form(Comment.class);
 	static Form<String> searchForm = Form.form(String.class);
 
 	public static Result index() {
		return ok(views.html.index.render(Note.all(), noteForm, searchForm));
 	}
 
 	public static Result list() {
		return ok(views.html.index.render(Note.all(), noteForm, searchForm));
 	}
 
 	public static Result show(Long id) {
		return ok(views.html.notes.show.render(Note.find.ref(id), noteForm, commentForm, searchForm));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result newNote() {
 		Form<Note> filledForm = noteForm.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
			return badRequest(views.html.index.render(Note.all(), filledForm, searchForm));
 		} else {
 			Note.create(filledForm.get(), Form.form().bindFromRequest().get("tagList"), User.currentUser());
 			return redirect(routes.Notes.list());
 		}
 	}
 
 	@SecureSocial.SecuredAction(authorization = WithPrivilegeLevel.class, params = {PrivilegeLevel.USER, PrivilegeLevel.ADMIN})
 	public static Result delete(Long id) {
 		Note.delete(id);
 		return redirect(routes.Notes.list());
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result newComment(Long id) {
 		Form<Comment> filledForm = commentForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
			//return badRequest(views.html.index.render(Note.all(), filledForm)); // should redirect to show note view later 
 		} else {
 			Comment.create(id, filledForm.get(), User.currentUser());
 		}
 		return redirect(routes.Notes.show(id));
 	}
 
 	//@SecureSocial.SecuredAction(authorization = WithPrivilegeLevel.class, params = {PrivilegeLevel.USER, PrivilegeLevel.ADMIN})
 	public static Result deleteComment(Long id, Long commentId) {
 		Comment.delete(commentId);
 		return redirect(routes.Notes.show(id));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result toggleUpVote(Long id) {
 		Note.find.ref(id).toggleUpVote(User.currentUser());
 		return redirect(routes.Notes.show(id));
 	}
 	
 	@SecureSocial.SecuredAction
 	public static Result toggleDownVote(Long id) {
 		Note.find.ref(id).toggleDownVote(User.currentUser());
 		return redirect(routes.Notes.show(id));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result edit(Long id) {
 		Form<Note> filledForm = noteForm.fill(Note.find.ref(id));
		return ok(views.html.notes.edit.render(Note.find.ref(id), filledForm));
 	}
 
 	@SecureSocial.SecuredAction
 	public static Result update(Long id) {
 		Form<Note> filledForm = noteForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
			return badRequest(views.html.index.render(Note.all(), filledForm, searchForm));
 		} else {
 			Note.update(filledForm.get(), Form.form().bindFromRequest().get("tagList"));
 		return redirect(routes.Notes.show(id));
 		}
 	}
 }

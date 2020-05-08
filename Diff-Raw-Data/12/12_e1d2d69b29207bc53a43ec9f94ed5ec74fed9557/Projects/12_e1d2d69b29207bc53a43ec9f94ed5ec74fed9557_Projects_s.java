 package controllers;
 
 import java.util.List;
 
 import models.InviteForm;
 import models.Project;
 import models.User;
 import play.Logger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 @Security.Authenticated(Secured.class)
 public class Projects extends Controller{
 	
 	
 	public static Result newProject(){
 		Form<Project> projectForm = form(Project.class);
 		return ok(views.html.project.create.render(projectForm));
 	}
   
 	
 	public static Result invite(Long projectId){
 		List<User> invitables = User.findNotPartecipating(projectId);
 		return ok(views.html.project.invite.render(invitables,projectId));
 	}
 	
 	public static Result inviteSave(Long projectId){
 		String u = session("currentUser");
 //		User user = User.find.where().eq("username", u).findUnique();
 //		Logger.error(request().body().asText());
 //		Form<InviteForm> form = form(InviteForm.class).bindFromRequest();
 //		Logger.error(""+form);
 //		Project p = Project.find.byId(projectId);
 //		List<User> invited = User.find.where().idIn(form.get().invite).findList();
 //		p.members.addAll(invited);
 //		p.save();
 		return redirect(routes.Users.view(u));
 	}
 	
   public static Result saveProject(){
	    String u = session("currentUser");
	    if(u==null){
	    	return unauthorized();
	    }else{
	    	User user = User.find.where().eq("username", u).findUnique();
	    
 	    Form<Project> projectForm = form(Project.class).bindFromRequest();
 	    if(projectForm.hasErrors()) {
             return badRequest(views.html.project.create.render(projectForm));
         }
 	    Project p = new Project();
 	    p.name = projectForm.get().name;
 	    p.creator = user;
 	    p.save();
	    }
		return redirect(routes.Users.view(u));
 	}
   
   public static Result show(Long  id){
 	  Project p = Project.find.setId(id).findUnique();
 	  Logger.error("id:" +p.name);
 	  if(p==null){
 		  return notFound();
 	  }
 	  return ok(views.html.project.view.render(p));
   }
 }

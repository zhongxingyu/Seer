 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import views.html.*;
 
 import models.*;
 
 /**
  * Manage projects related operations.
  */
 @Security.Authenticated(Secured.class)
 public class VoteController extends Controller {
 
 	static Form<Criteria> criteriaForm = form(Criteria.class);
 	static Form<Project> projectForm = form(Project.class);
 	static Form<Ballot> ballotForm = form(Ballot.class);
 
 	public static Long pjid;
 
 	public static Result vote(){
 
 		if(User.getUserTypeId(User.findByUsername(request().username())) == 9) {
      		 return ok(views.html.adminVote.render(Project.findAllProject()
   				      , Criteria.all()
   				      , projectForm
   				      , ballotForm
   				      , User.findByUsername(request().username()))
      		 		);
  		}
 
     	else{
 			return ok(vote.render(Project.findAllProject()
   				      , Criteria.all()
  				      , criteriaForm
   				      , projectForm
   				      , User.findByUsername(request().username()))
   					);
 		}
 
 
 
   		
 	}
 
 	public static Result saveProject(Long id){
 		return ok(vote.render(Project.findAllProject()
 				      , Criteria.all()
 				      , projectForm
 				      , ballotForm
 				      , User.findByUsername(request().username()))
 		);
 	}
 
 	public static Result voteForProject() {
 		Form<Ballot> bff = ballotForm.bindFromRequest();
 		System.out.println(bff.get().project_id);
 		System.out.println(bff.get().criteria_id);
 		System.out.println(bff.get().score);
 		if (bff.get().score != 0)
 			Ballot.saveBallot(bff.get() , User.findByUsername(request().username()));
 		else 
 			redirect(routes.VoteController.vote());
 		return TODO;
 	}
   
 }

 package controllers;
 
 import java.util.List;
 
 import models.Archetype;
 import models.Benutzer;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.index;
 import views.html.modalRemote;
import views.html.showForm;
 
 public class Application extends Controller {
 
     @Security.Authenticated(Secured.class)
     public static Result index() {
     	Benutzer benutzer = Benutzer.find.byId(request().username());
     	List<Archetype> names = Archetype.find.all();
         return ok(index.render(benutzer,names));
     }
     public static Result showArchetypeModal(String archetypeId){
         Archetype arche = Archetype.find.byId(archetypeId);
         return ok(modalRemote.render(arche));
     }
 
    public static Result showForm(String archetypeID) {
    	Archetype arche = Archetype.find.byId(archetypeID);
    	Benutzer benutzer = Benutzer.find.byId(request().username());
    	List<Archetype> names = Archetype.find.all();
    	return ok(main.render(benutzer,names,arche));
   }
    
     public static Result logout(){
         session().clear();
         flash("success","You've been logged out");
         return redirect(routes.LoginController.login());
     }
 }

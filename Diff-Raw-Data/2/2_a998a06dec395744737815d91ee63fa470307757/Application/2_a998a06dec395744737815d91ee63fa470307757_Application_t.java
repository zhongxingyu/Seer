 package controllers;
 
 import play.*;
 import play.mvc.*;
 import views.html.*;
 import models.*;
 import play.data.*;
 import java.text.*;
 import java.util.*;
 
 public class Application extends Controller {
   
     public static Result index() {
         return ok(index.render("Your new application is ready."));
     }
     
     public static Result profLogin() {
     	return ok(login.render("T"));
     }
     
     @Security.Authenticated(Secured.class)
     public static Result profSeancesListe(String log) {
     	return ok(seancesListe.render(Seance.page(session()),log));
     }
     
 	public static Result profAuthenticate()
 	{
 		DynamicForm fullInfos = Form.form().bindFromRequest();
 		String identifiant = fullInfos.get("login");
		if(Professeur.find.where().eq("username",identifiant).findList().isEmpty()){
 			session().clear();
 			return badRequest(login.render("F"));
 		}else{
 			session().clear();
 			session("username",identifiant);
 			return profSeancesListe("");
 		}
 	}
 	
 	
 	//Gestion des séances
 	public static Result addSeance(){
 		DynamicForm fullInfos = Form.form().bindFromRequest();
 		String day = fullInfos.get("day");
 		String month = fullInfos.get("month");
 		String year = fullInfos.get("year");
 		String hour = fullInfos.get("hour");
 		
 		Form<Seance> seanceForm = Form.form(Seance.class).bindFromRequest();
 		if(fullInfos.get("matiere")!=""){
 			Seance newSeance = new Seance();
 			if(fullInfos.get("intitule")==""){
 				newSeance.intitule="Intitulé";
 			}else{
 				newSeance.intitule=fullInfos.get("intitule");
 			}
 			newSeance.matiere=fullInfos.get("matiere");
 			newSeance.professeur=Professeur.find.ref(session("username"));
 			try{
 			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			newSeance.date = df.parse(year + "/" + month + "/" + day + " " + hour +":00:00");
 			} catch(ParseException e){
 				e.printStackTrace();
 			}
 			Seance.addSeance(newSeance);
 			return redirect(routes.Application.profSeancesListe("Séance ajoutée avec succès."));
 		}
 		return redirect(routes.Application.profSeancesListe("Cette matière n'existe pas."));
 	}
 	public static Result removeSeance(Long id){
 		Seance.removeSeance(id);
 		return redirect(routes.Application.profSeancesListe("Séance supprimée."));
 	}
 	public static Result displayEditSeance(Long id){
 		return ok(editSeance.render(Seance.find.ref(id)));
 	}
 	public static Result editSeance(Long id){
 		DynamicForm fullInfos = Form.form().bindFromRequest();
 		String day = fullInfos.get("day");
 		String month = fullInfos.get("month");
 		String year = fullInfos.get("year");
 		String hour = fullInfos.get("hour");
 		
 		Form<Seance> seanceForm = Form.form(Seance.class).bindFromRequest();
 		if(fullInfos.get("matiere")!=""){
 			removeSeance(id);
 			
 			Seance newSeance = new Seance();
 			newSeance.id=id;
 			if(fullInfos.get("intitule")==""){
 				newSeance.intitule="Intitulé";
 			}else{
 				newSeance.intitule=fullInfos.get("intitule");
 			}
 			newSeance.matiere=fullInfos.get("matiere");
 			newSeance.professeur=Professeur.find.ref(session("username"));
 			try{
 			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			newSeance.date = df.parse(year + "/" + month + "/" + day + " " + hour +":00:00");
 			} catch(ParseException e){
 				e.printStackTrace();
 			}
 			Seance.addSeance(newSeance);
 			return redirect(routes.Application.profSeancesListe("Séance éditée."));
 		}
 		return redirect(routes.Application.profSeancesListe("Erreur dans l'édition de la séance, cette matière n'existe pas."));
 	}
 	
 }

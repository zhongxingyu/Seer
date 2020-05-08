 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 import org.apache.commons.io.FileUtils;
 
 import models.Aliment;
 import models.EtatFrigo;
 import models.ListeDeCourse;
 import models.Recette;
 import models.User;
 
 
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.With;
 import utils.ApplicationUtils;
 
 
 @With(Secure.class)
 public class Application extends Controller {
 	
 //	@Before
 //    static void checkAuthentification() {
 //        if(session.get("user") == null) login();
 //    }
 
 	@Before
 	static void setConnectedUser() {
 		if(Security.isConnected()) {
 			User user = User.find("byEmail", Security.connected()).first();
 			renderArgs.put("user", user);
 		}
 	}
 
 	@Before
 	static void listeCourante() {
 		
 		/* On recupère l'utilisateur en session */
 		User user = User.find("byEmail", Security.connected()).first();
 
 		/* On recupère sa liste de course courante (non nulle) */
 		ListeDeCourse listeCourante = user.listeDeCourse.get(0);
 		List<Aliment> articles = listeCourante.article;
 		List<Aliment> artFruitsLegumes = new ArrayList<Aliment>();
 		List<Aliment> artViandes = new ArrayList<Aliment>();
 		List<Aliment> artLaitages = new ArrayList<Aliment>();
 		List<Aliment> artBoissons= new ArrayList<Aliment>() ;
 		List<Aliment> artAutre = new ArrayList<Aliment>();
 		List<Aliment> artEpicerie = new ArrayList<Aliment>();
 
 		/* tri par section */
 		ApplicationUtils.misAJourListes(articles, artFruitsLegumes, artViandes, artLaitages, artBoissons, artAutre, artEpicerie);
 
 		renderArgs.put("artFruitsLegumes", artFruitsLegumes);
 		renderArgs.put("artLaitages", artLaitages);
 		renderArgs.put("artViandes", artViandes);
 		renderArgs.put("artBoissons", artBoissons);
 		renderArgs.put("artAutre", artAutre);
 		renderArgs.put("artEpicerie", artEpicerie);
 	}
 
 	public static void index() {	
 		/* On recupère l'utilisateur en session */
 		User user = User.find("byEmail", Security.connected()).first();	
 		
 		/* On recupère le dernier etat */
 		EtatFrigo etatFrigo = EtatFrigo.find("user like ? order by date desc", user).first();
 	
 		
 		/* on génère la liste correspondante */
 		List<Aliment> aliments = etatFrigo.aliment;
 		List<Aliment> fruitsLegumes = new ArrayList<Aliment>();
 		List<Aliment> viandes = new ArrayList<Aliment>();
 		List<Aliment> laitages = new ArrayList<Aliment>();
 		List<Aliment> boissons= new ArrayList<Aliment>() ;
 		List<Aliment> autre = new ArrayList<Aliment>();
 		List<Aliment> epicerie = new ArrayList<Aliment>();
 
 		/* tri par section */
 		ApplicationUtils.misAJourListes(aliments, fruitsLegumes, viandes, laitages, boissons, autre, epicerie);
 		
 		/* On passe en paramètre la page dans laquelle on était */
 		session.put("page", "index");	
 		
 		renderTemplate("Application/afficheEtat.html",fruitsLegumes, viandes, laitages, boissons, autre, epicerie, etatFrigo);
 	}
 
 	public static void ancienEtat(Long id) {
 		if (id == null){
 		}
 		/* On recupère l'état frigo correspondant à l'id */
 		EtatFrigo etatFrigo = EtatFrigo.findById(id);	
 		
 		/* on génère la liste correspondante */
 		List<Aliment> aliments = etatFrigo.aliment;
 		List<Aliment> fruitsLegumes = new ArrayList<Aliment>();
 		List<Aliment> viandes = new ArrayList<Aliment>();
 		List<Aliment> laitages = new ArrayList<Aliment>();
 		List<Aliment> boissons= new ArrayList<Aliment>() ;
 		List<Aliment> autre = new ArrayList<Aliment>();
 		List<Aliment> epicerie = new ArrayList<Aliment>();
 		
 		/* tri par section */
 		ApplicationUtils.misAJourListes(aliments, fruitsLegumes, viandes, laitages, boissons, autre, epicerie);
 		
 		/* On passe en paramètre la page dans laquelle on était */
 		session.put("page", "ancienEtat");
 		session.put("idfrigo", etatFrigo.id);
 		renderArgs.put("etatFrigo", etatFrigo);
 		renderTemplate("Application/afficheEtat.html",fruitsLegumes, viandes, laitages, boissons, autre, epicerie, etatFrigo);
 	}
 	
 	public static void historique() {
 		render();
 	}
 
 	public static void recettesSuggerees() {
 		render();
 	}
 	public static void recettesFavorites() {
 		render();
 	}
 	public static void carottesRapees() {
 		render();
 	}
 
 	public static void listesArchivees() {
 		render();
 	}
 
 	public static void listesFavorites() {
 		render();
 	}
 
 	public static void profil() {
 		long nbOuvertureJour = ApplicationUtils.nombreOuvertureFrigoParJour(new Date());
 		render(nbOuvertureJour);
 
 	}
 
 	public static void photo() {
 		render();
 	}   
 
 	
 	public static void ajoutAlimentListe(String aliment, Long id) {
 		
 		/* On recupère l'utilisateur en session */
 		User user = User.find("byEmail", Security.connected()).first();
 		
 		if (id == null){
 			String idS = session.get("idfrigo");
 			id = Long.parseLong(idS);
 		}
 
 		/* On recupère sa liste courante (non nulle) */
 		ListeDeCourse listeCourante = user.listeDeCourse.get(0);
 		listeCourante.addAliment(aliment);
 		String page = session.get("page");
 		
 		switch (page){
 		case "index":
 			index();
 			break;
 		case "ancienEtat":
 			ancienEtat(id);
 			break;
 			default:
 				break;
 				
 		}
 
 	}
 
 	public static void supprimeAlimentListe(Long id, Long idfrigo) {
 
 		Aliment aliment = Aliment.findById(id);
 		aliment.delete();
 		String page = session.get("page");
 
 		switch (page){
 		case "index":
 			index();
 			break;
 		case "ancienEtat":
 			ancienEtat(idfrigo);
 			break;
 		default:
 			break;
 
 		}
 	}
 
 }

 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import models.Journee;
 import models.Matche;
 import models.PointsJournee;
 import models.PointsSaison;
 import models.Pronostique;
 import models.Saison;
 import models.Sys_parameter;
 import models.Utilisateur;
 import play.api.mvc.MultipartFormData;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security.Authenticated;
 import views.html.inscription;
 import views.html.membres;
 import views.html.profilUtilisateur;
 import views.html.statistiques;
 
 @Authenticated(Secured.class)
 public class Statistiques extends Controller {
 	
 	public static Result getStat(String id) {
 		Long idLong;
 		
 		Date maintenant = new Date();
 		Sys_parameter system = Sys_parameter.find.byId((long) 1);
 		Saison saison = system.getSaisonEnCours();
 		Journee premiereJournee = saison.getPremiereJournee();
 		Journee derniereJournee= saison.getDerniereJournee();
 		
 		List<Journee> journeesPassees = Journee.find.where().le("id", derniereJournee.getId()).ge("id", premiereJournee.getId()).lt("dateJournee", maintenant).orderBy().desc("dateJournee").findList();
 		
 		if (id.equalsIgnoreCase("0")) {
 			idLong = journeesPassees.get(0).id;
 		} else {
 			idLong = Long.parseLong(id);
 		}
 		Utilisateur user = Utilisateur.findByPseudo(request().username());
 
 		Journee journee = Journee.find.byId(idLong);
 		
 		Integer points = PointsSaison.find.where().eq("saison", saison).eq("user", user).findList().get(0).pointsTotalSaison;
 		
 		// Meilleur pronostiqueur de la journée
 		List<PointsJournee> classementPronostiqueurJournee = PointsJournee.find.where().eq("journee", journee).orderBy().desc("points").findList();
 		Integer i=1;
 		Integer scorePremier = classementPronostiqueurJournee.get(0).getPoints();
 		List<PointsJournee> meilleurPronostiqueurs = new ArrayList<PointsJournee>();
 		meilleurPronostiqueurs.add(classementPronostiqueurJournee.get(0));
 		while (classementPronostiqueurJournee.size()!=i && classementPronostiqueurJournee.get(i).getPoints()==scorePremier) {
 			PointsJournee egalite = classementPronostiqueurJournee.get(i);
 			meilleurPronostiqueurs.add(egalite);
 			i++;
 		}
 		
 		// Plus mauvais de la journée
 		List<PointsJournee> classementPronostiqueurAscJournee = PointsJournee.find.where().eq("journee", journee).orderBy().asc("points").findList();
 		i=1;
 		Integer scoreDernier = classementPronostiqueurAscJournee.get(0).getPoints();
 		List<PointsJournee> mauvaisPronostiqueurs = new ArrayList<PointsJournee>();
 		mauvaisPronostiqueurs.add(classementPronostiqueurAscJournee.get(0));
 		while (classementPronostiqueurAscJournee.size()!=i && classementPronostiqueurAscJournee.get(i).getPoints()==scoreDernier) {
 			PointsJournee egalite = classementPronostiqueurAscJournee.get(i);
 			mauvaisPronostiqueurs.add(egalite);
 			i++;
 		}
 		
 		// Scores corrects journée
 		List<PointsJournee> classementPointsCorrectsJournee = PointsJournee.find.where().eq("journee", journee).orderBy().desc("scoresCorrects").findList();
 		i=1;
 		Integer scoreCorrectsJournee = classementPointsCorrectsJournee.get(0).getScoresCorrects();
 		List<PointsJournee> pointsCorrectsJournee = new ArrayList<PointsJournee>();
 		pointsCorrectsJournee.add(classementPointsCorrectsJournee.get(0));
 		while (classementPointsCorrectsJournee.size()!=i && classementPointsCorrectsJournee.get(i).getScoresCorrects()==scoreCorrectsJournee) {
 			PointsJournee egalite = classementPointsCorrectsJournee.get(i);
 			pointsCorrectsJournee.add(egalite);
 			i++;
 		}
 		
 		// Plus grand nombre de scores corrects au total
 		List<PointsSaison> classementScoresCorrectsTotal = PointsSaison.find.where().eq("saison", saison).orderBy().desc("totalScoresCorrects").findList();
 		i=1;
 		Integer maxScoreCorrects = classementScoresCorrectsTotal.get(0).getTotalScoresCorrects();
 		List<PointsSaison> scoreCorrectsTotal = new ArrayList<PointsSaison>();
 		scoreCorrectsTotal.add(classementScoresCorrectsTotal.get(0));
 		while (classementScoresCorrectsTotal.size()!=i && classementScoresCorrectsTotal.get(i).getTotalScoresCorrects()==maxScoreCorrects) {
 			PointsSaison egalite = classementScoresCorrectsTotal.get(i);
 			scoreCorrectsTotal.add(egalite);
 			i++;
 		}
 		
 		// Plus grand nombre de scores corrects en une journée
 		List<PointsJournee> classementScoreCorrectsUneJournee = PointsJournee.find.where().ge("journee",saison.getPremiereJournee()).
 				le("journee", saison.getDerniereJournee()).orderBy().desc("scoresCorrects").findList();
 		i=1;
 		Integer maxScoreCorrectsUneJournee = classementScoreCorrectsUneJournee.get(0).getScoresCorrects();
 		List<PointsJournee> scoresCorrectsUneJournee = new ArrayList<PointsJournee>();
 		scoresCorrectsUneJournee.add(classementScoreCorrectsUneJournee.get(0));
 		while (classementScoreCorrectsUneJournee.size()!=i && classementScoreCorrectsUneJournee.get(i).getScoresCorrects()==maxScoreCorrectsUneJournee) {
 			PointsJournee egalite = classementScoreCorrectsUneJournee.get(i);
 			scoresCorrectsUneJournee.add(egalite);
 			i++;
 		}
 		
 		// Meilleur score en une journée
 		List<PointsJournee> classementPointsUneJournee = PointsJournee.find.where().ge("journee",saison.getPremiereJournee()).
 				le("journee", saison.getDerniereJournee()).orderBy().desc("points").findList();
 		i=1;
 		Integer maxPointsUneJournee = classementPointsUneJournee.get(0).getPoints();
 		List<PointsJournee> pointsUneJournee = new ArrayList<PointsJournee>();
 		pointsUneJournee.add(classementPointsUneJournee.get(0));
 		while (classementPointsUneJournee.size()!=i && classementPointsUneJournee.get(i).getPoints()==maxPointsUneJournee) {
 			PointsJournee egalite = classementPointsUneJournee.get(i);
 			pointsUneJournee.add(egalite);
 			i++;
 		}
 		
 		// Plus grand nombre de fois premier
		List<PointsSaison> classementFoisPremier = PointsSaison.find.where().eq("saison", saison).findList();
 		i=1;
 		Integer maxFoisPremier = classementFoisPremier.get(0).getNbFoisPremier();
 		List<PointsSaison> nbFoisPremier = new ArrayList<PointsSaison>();
 		nbFoisPremier.add(classementFoisPremier.get(0));
 		while (classementFoisPremier.size()!=i && classementFoisPremier.get(i).getNbFoisPremier()==maxFoisPremier) {
 			PointsSaison egalite = classementFoisPremier.get(i);
 			nbFoisPremier.add(egalite);
 			i++;
 		}
 		
 		
 		return ok(statistiques.render(user, journeesPassees, journee, points, meilleurPronostiqueurs, mauvaisPronostiqueurs, pointsCorrectsJournee,
 				scoreCorrectsTotal, scoresCorrectsUneJournee, pointsUneJournee, nbFoisPremier));
 	}
 	
 }

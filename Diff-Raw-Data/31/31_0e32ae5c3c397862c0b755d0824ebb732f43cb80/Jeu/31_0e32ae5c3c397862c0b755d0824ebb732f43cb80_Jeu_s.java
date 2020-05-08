 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import models.PointGPS;
 import models.Seance;
 import models.Joueur;
 import models.Quete;
 import play.libs.Json;
 import play.data.DynamicForm;
 import static play.data.Form.*;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 
 import views.html.jeu.*;
 
 @Security.Authenticated(Secured.class)
 public class Jeu extends Controller {
 
 	private static class Realisation {
 		public Integer duree;
 		public Integer distance;
 
 		private Realisation(Integer duree, Integer distance) {
 			this.duree = duree;
 			this.distance = distance;
 		}
 		private Realisation() {
 			this(0, 0);
 		}
 		
 		public static Realisation calculer(List<PointGPS> liste) {
 			if(liste.size() <= 1) {
 				return new Realisation();
 			}
 			
 			Integer duree = 0; // calcul en secondes
 			float distance = 0; // calcul en mètres
 
 			duree = duree(liste.get(0), liste.get(liste.size() - 1));
 			
 			PointGPS precedent = null;
 			for(PointGPS point : liste) {
 				if(precedent != null) {
 					distance += distance(precedent, point);
 				}
 				
 				precedent = point;
 			}
 			
 			return new Realisation(duree / 60, (int)(distance / 1000));
 		}
 		
 		// http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
 		private static float distance(PointGPS precedent, PointGPS point) {
 			double lat1 = precedent.latitude;
 			double lng1 = precedent.longitude;
 			double lat2 = point.latitude;
 			double lng2 = point.longitude;
 			
 			double earthRadius = 3958.75;
 			double dLat = Math.toRadians(lat2 - lat1);
 			double dLng = Math.toRadians(lng2 - lng1);
 			double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
 			           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
 			           Math.sin(dLng/2) * Math.sin(dLng/2);
 			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 			double dist = earthRadius * c;
 			
 			int meterConversion = 1609;
 			
 			return new Float(dist * meterConversion).floatValue();
 		}
 		
 		private static Integer duree(PointGPS premier, PointGPS dernier) {
 			long dateDebut = premier.date.getMillis();
 			long dateDernier = dernier.date.getMillis();
 			
 			return (int) ((dateDernier - dateDebut) / 1000);
 		}
 	}
 
 	public static Result profil() {
 		return ok(profil.render(
 			Joueur.find.byId(request().username())
 		));
 	}
 	
 	public static Result quetes(Integer id) {
 		List<Quete> listeQuetes = Quete.listByJoueur(request().username());
 		Quete queteActuelle = null;
 		
 		if(id > 0) {
 			queteActuelle = Quete.find.byId(id);
 		}
 		
 		if(queteActuelle == null) {
 			for(Quete quete : listeQuetes) {
 				Seance seance = quete.getSeance(request().username());
 				if(seance.etat.equals(Seance.Etat.NOUVELLE) ||
 				   seance.etat.equals(Seance.Etat.EN_COURS) ||
 				   seance.etat.equals(Seance.Etat.TERMINEE)) {
 					queteActuelle = Quete.find.byId(seance.quete.id);
 				}
 			}
 		}
 		
 		return ok(quetes.render(
 			Joueur.find.byId(request().username()),
 			listeQuetes,
 			queteActuelle
 		));
 	}
 
 	public static Result validerQuete() {
 		DynamicForm requestData = form().bindFromRequest();
 		Integer idQuete = Integer.parseInt(requestData.get("idQuete"));
 		String action = requestData.get("action");
 		
 		if(action.equals("next")) {
 			Seance.valider(idQuete, request().username());
 		} else if(action.equals("accept")) {
 			Seance.accepter(idQuete, request().username());
 		}
 		
 		return redirect(
 			routes.Jeu.quetes(0)
 		);
 	}
 
 	public static Result seance(Integer idQuete) {
 		Quete queteActuelle = null;
 		if(idQuete > 0) {
 			queteActuelle = Quete.find.byId(idQuete);
 		}
 	
 		return ok(seance.render(
 			Joueur.find.byId(request().username()),
 			queteActuelle
 		));
 	}
 
 	public static Result validerSeance() {
 		DynamicForm requestData = form().bindFromRequest();
		Integer idQuete = Integer.parseInt(requestData.get("idQuete"));
 		
 		Quete quete = Quete.find.byId(idQuete);
 		Joueur joueur = Joueur.find.byId(request().username());
 		List<PointGPS> liste = Jeu.parsePointsGPS(requestData.get("positions"));
 		Realisation real = Realisation.calculer(liste);
 		
 		if(quete == null) {
 			return Jeu.enregistrerEntrainement(joueur, real.distance, real.duree);
 		} else {
 			return Jeu.enregistrerQuete(joueur, quete, real.distance, real.duree);
 		}
 	}
 
 	private static List<PointGPS> parsePointsGPS(String positionsJson) {
 	        DateTimeFormatter parserDate = ISODateTimeFormat.dateTime();
 		List<PointGPS> listePoints = new ArrayList<PointGPS>();
 		
 		JsonNode jsonList = Json.parse(positionsJson);
 		
 		for(JsonNode jsonElt : jsonList) {
 			PointGPS point = new PointGPS();
 			point.longitude = (float) jsonElt.findPath("lon").asDouble();
 			point.latitude = (float) jsonElt.findPath("lat").asDouble();
 			point.altitude =  (float) jsonElt.findPath("alt").asDouble();
 			point.precision =  jsonElt.findPath("acc").asInt();
 			point.date =  parserDate.parseDateTime(jsonElt.findPath("time").asText());
 			listePoints.add(point);
 		}
 		
 		return listePoints;
 	}
 
 	private static Result enregistrerEntrainement(Joueur joueur,
 			Integer distance, Integer duree) {
 		int gainEnd = 0;
 		int gainVit = 0;
 		String status = "ko";
 		
 		if(distance > 0 && duree > 0) {
 			gainEnd = calculerGainEndurance(distance, duree);
 			gainVit = calculerGainVitesse(distance, duree);
 
 			joueur.xpEndurance += gainEnd;
 			joueur.xpVitesse += gainVit;
 			joueur.save();
 			
 			status = "ok";
 		}
 		
 		ObjectNode result = Json.newObject();
 		result.put("status", status);
 		result.put("gainEndurance", gainEnd);
 		result.put("gainVitesse", gainVit);
 		
 		return ok(result);
 	}
 
 	private static Result enregistrerQuete(Joueur joueur, Quete quete,
 			Integer distance, Integer duree) {
 		Integer objDistance = quete.getObjDistance(joueur);
 		Integer objDuree = quete.getObjDuree(joueur);
 		int gainEnd = 0;
 		int gainVit = 0;
 		int gainScore = 0;
 		String status = "ko";
 		
 		if(distance >= objDistance && duree >= objDuree) {
 			gainEnd = calculerGainEndurance(distance, duree) / 2;
 			gainVit = calculerGainVitesse(distance, duree) / 2;
 			gainScore = (objDistance * 30 + objDuree) / 10;
 
 			joueur.xpEndurance += gainEnd;
 			joueur.xpVitesse += gainVit;
 			joueur.score += gainScore;
 			joueur.save();
 			
			Seance.terminer(quete.id, joueur.pseudo);
 			
 			status = "ok";
 		}
 		
 		ObjectNode result = Json.newObject();
 		result.put("status", status);
 		result.put("gainEndurance", gainEnd);
 		result.put("gainVitesse", gainVit);
 		
 		return ok(result);
 	}
 
 	private static int calculerGainEndurance(Integer distance, Integer duree) {
 		float f_distance = distance;
 		float f_duree = duree;
 		float vitMoyenne = f_distance / (f_duree / 60);
 		
 		return Math.max((int)((f_duree / 60) * 10 * vitMoyenne), 5);
 	}
 
 	private static int calculerGainVitesse(Integer distance, Integer duree) {
 		float f_distance = distance;
 		float f_duree = duree;
 		float vitMoyenne = f_distance / (f_duree / 60);
 		
 		return Math.max((int)(f_distance * vitMoyenne), 5);
 	}
 }

 package controllers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.persistence.TypedQuery;
 
 import org.codehaus.groovy.runtime.ConvertedClosure;
 
 import com.sun.jmx.snmp.Timestamp;
 
 import models.MesPassagers;
 import models.Notification;
 import models.Trajet;
 import models.Troncon;
 import models.Utilisateur;
 import models.Annonce;
 import models.Ville;
 import models.Voiture;
 import models.DemandeAnnonceEnAttente;
 import play.*;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.db.jpa.Model;
 import play.mvc.*;
 import play.mvc.Scope.RenderArgs;
 
 public class Annonces extends Controller {
 
 	@Before
 	static void setConnectedUser() {
 		if (Security.isConnected()) {
 			Utilisateur user = Utilisateur
 					.find("byEmail", Security.connected()).first();
 			renderArgs.put("user", user);
 			renderArgs.put("security", Security.connected());
 		}
 	}
 
 	@Before
 	static void nbDemandes() {
 		if (Security.isConnected()) {
 			Utilisateur user = Utilisateur
 					.find("byEmail", Security.connected()).first();
 			int nbDemandes = user.mesDemandes.size();
 
 			List<Annonce> mesAnnonces = Annonce.find("byMonUtilisateur_id",
 					user.id).fetch();
 			for (Annonce a : mesAnnonces) {
 				nbDemandes += a.mesDemandePassagers.size();
 			}
 
 			List<Notification> mesNotifications = Notification.find(
 					"byMonUtilisateur_id", user.id).fetch();
 
 			nbDemandes += mesNotifications.size();
 
 			renderArgs.put("nbDemandes", nbDemandes);
 		}
 	}
 
 	public static void annonces() {
 
 		if (Security.isConnected()) {
 
 			List<Annonce> annonces = Annonce.findAll();
 			List<Ville> lesVilles = Ville.find("ORDER BY nom").fetch();
 			flash.clear();
 			// for (Annonce a : annonces) {
 			// if (new Date().compareTo(a.monTrajet.dateDepart) > 0) {
 			// annonces.remove(a);
 			// }
 			// }
 			render(annonces, lesVilles);
 
 		}
 		render();
 	}
 
 	public static void mesannonces() {
 		if (Security.isConnected()) {
 			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 					.first();
 			List<Annonce> mesAnnonces = Annonce.find("byMonUtilisateur", moi)
 					.fetch();
 			flash.clear();
 			render(mesAnnonces);
 		}
 		render();
 	}
 
 	public static void editermonannonce(long id_annonce) {
 		if (Security.isConnected()) {
 			Annonce annonce = Annonce.findById(id_annonce);
 			List<Ville> lesVilles = Ville.find("ORDER BY nom").fetch();
 			renderArgs.put("annonce", annonce);
 			renderArgs.put("etapes", annonce.monTrajet.mesEtapes);
 			renderArgs.put("lesVilles", lesVilles);
 		}
 		render();
 	}
 
 	public static Date retournerDate(String date, String heure)
 			throws ParseException {
 		StringTokenizer st = new StringTokenizer(date, "/");
 		String[] tabDate = new String[6];
 		int i = 0;
 		while (st.hasMoreTokens()) {
 			tabDate[i] = st.nextToken();
 			i++;
 		}
 
 		StringTokenizer stH = new StringTokenizer(heure, ":");
 		while (stH.hasMoreTokens()) {
 			tabDate[i] = stH.nextToken();
 			i++;
 		}
 
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		return (formatter.parse(tabDate[2] + "-" + tabDate[1] + "-"
 				+ tabDate[0] + " " + tabDate[3] + ":" + tabDate[4]));
 
 	}
 
 	public static void sauvegardermonannonce(Annonce annonce, Long depart,
 			Long arrivee, @Required String date, @Required String heure,
 			String mesEscales) throws ParseException {
 		// Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 		// .first();
 		validation.valid(date);
 		validation.valid(heure);
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			editermonannonce(annonce.id);
 		} else {
 			annonce.monTrajet.villeDepart = Ville.findById(depart);
 			annonce.monTrajet.villeArrivee = Ville.findById(arrivee);
 			annonce.monTrajet.dateDepart = retournerDate(date, heure);
 
 			List<Ville> etapes = new ArrayList<Ville>();
 			StringTokenizer st = new StringTokenizer(mesEscales, " ");
 			while (st.hasMoreTokens()) {
 				Ville v = Ville.findById(Long.parseLong(st.nextToken()));
 				etapes.add(v);
 			}
 
 			annonce.monTrajet.mesEtapes.clear();
 			annonce.monTrajet.mesEtapes = etapes;
 			annonce.monTrajet.save();
 			annonce.save();
 			flash.success("Sauvegarde réussie");
 			// List<Annonce> mesAnnonces = Annonce.find("byMonUtilisateur", moi)
 			// .fetch();
 			mesannonces();
 		}
 	}
 
 	public static void details(long id_annonce) {
 		if (Security.isConnected()) {
 			Annonce annonce = Annonce.find("byId", id_annonce).first();
 			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 					.first();
 			flash.clear();
 			// on test si l'annonce est terminée
 			boolean dateok = false, isMonAnnonce = false;
 			if (new Date().compareTo(annonce.monTrajet.dateDepart) < 0) {
 				dateok = true;
 			}
 			if (annonce.monUtilisateur.id == moi.id) {
 				isMonAnnonce = true;
 			}
 			render(moi, annonce, dateok, isMonAnnonce);
 		}
 		render();
 	}
 
 	public static void recherche(String villeDepart_id, String villeArrivee_id,
 			String dateDepart, String heureDepart) {
 		if (Security.isConnected()) {
 			List<Annonce> pAnnonces = new ArrayList<Annonce>();
 			List<Trajet> trajets = Trajet.findAll();
 			List<Trajet> trajets_trouves = new ArrayList<Trajet>();
 
 			try {
 				// tous les champs remplis (ok)
 				if (!villeDepart_id.isEmpty() && !villeArrivee_id.isEmpty()
 						&& !dateDepart.isEmpty() && !heureDepart.isEmpty()) {
 					Date searchDate;
 
 					searchDate = retournerDate(dateDepart, heureDepart);
 					trajets_trouves = Trajet
 							.find("villeDepart_id like ? and villeArrivee_id like ? and dateDepart like ?",
 									Integer.parseInt(villeDepart_id),
 									Integer.parseInt(villeArrivee_id),
 									searchDate).fetch();
 
 				} else if (!villeDepart_id.isEmpty()
 						&& !villeArrivee_id.isEmpty() && dateDepart.isEmpty()
 						&& heureDepart.isEmpty()) {
 					// champs rempli : villeDepart + villeArrivee (ok)
 					Ville villeA = new Ville(), villeB = new Ville();
 					for (Trajet t : trajets) {
 
 						for (Ville v : t.mesEtapes) {
 							if (v.id == Integer.parseInt(villeDepart_id)) {
 								villeA = v;
 							} else if (v.id == Integer
 									.parseInt(villeArrivee_id))
 								villeB = v;
 						}
 						if (villeA != null
 								&& villeB != null
 								&& t.mesEtapes.indexOf(villeA) < t.mesEtapes
 										.indexOf(villeB))
 							trajets_trouves.add(t);
 					}
 
 				} else if (!villeDepart_id.isEmpty()
 						&& villeArrivee_id.isEmpty() && dateDepart.isEmpty()
 						&& heureDepart.isEmpty()) {
 					// champs rempli : villeDepart (ok)
 
 					for (Trajet t : trajets)
 						for (Ville v : t.mesEtapes)
 							if (v.id == Integer.parseInt(villeDepart_id))
 								trajets_trouves.add(t);
 
 				} else if (villeDepart_id.isEmpty()
 						&& !villeArrivee_id.isEmpty() && dateDepart.isEmpty()
 						&& heureDepart.isEmpty()) {
 					// champs rempli : villeArrivee (ok)
 					for (Trajet t : trajets)
 						for (Ville v : t.mesEtapes)
 							if (v.id == Integer.parseInt(villeArrivee_id))
 								trajets_trouves.add(t);
 
 				} else if (villeDepart_id.isEmpty()
 						&& villeArrivee_id.isEmpty() && !dateDepart.isEmpty()
 						&& heureDepart.isEmpty()) {
 					// champs rempli : dateDepart (ok)
 					Date dateDebut, dateFin;
 					dateDebut = retournerDate(dateDepart, "00:00");
 					dateFin = retournerDate(dateDepart, "23:59");
 					trajets_trouves = Trajet.find(
 							"dateDepart > ? and dateDepart < ?", dateDebut,
 							dateFin).fetch();
 
 					// champs rempli : dateDepart + heure (ok)
 				} else if (villeDepart_id.isEmpty()
 						&& villeArrivee_id.isEmpty() && !dateDepart.isEmpty()
 						&& !heureDepart.isEmpty()) {
 					Date date;
 
 					date = retournerDate(dateDepart, heureDepart);
 					trajets_trouves = Trajet.find("dateDepart like ? ", date)
 							.fetch();
 
 				} else if (!villeDepart_id.isEmpty()
 						&& !villeArrivee_id.isEmpty() && !dateDepart.isEmpty()
 						&& heureDepart.isEmpty()) {
 					// champs rempli : dateDepart + ville arrivee + ville
 					// depart(ok)
 					Date dateDebut, dateFin;
 
 					dateDebut = retournerDate(dateDepart, "00:00");
 					dateFin = retournerDate(dateDepart, "23:59");
 					List<Trajet> trajets_temp = Trajet.find(
 							"dateDepart > ? and dateDepart < ?", dateDebut,
 							dateFin).fetch();
 					Ville villeA = new Ville(), villeB = new Ville();
 					for (Trajet t : trajets_temp) {
 
 						for (Ville v : t.mesEtapes) {
 							if (v.id == Integer.parseInt(villeDepart_id)) {
 								villeA = v;
 							} else if (v.id == Integer
 									.parseInt(villeArrivee_id))
 								villeB = v;
 						}
 						if (villeA != null
 								&& villeB != null
 								&& t.mesEtapes.indexOf(villeA) < t.mesEtapes
 										.indexOf(villeB))
 							trajets_trouves.add(t);
 					}
 
 				} else {
 					pAnnonces = Annonce.findAll();
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 
 			if (trajets_trouves != null) {
 				for (Trajet t : trajets_trouves) {
 					Annonce a = Annonce.find("byMonTrajet_id", t.id).first();
 					pAnnonces.add(a);
 				}
 			}
 
 			renderArgs.put("lesVilles", Ville.find("ORDER BY nom").fetch());
 			renderArgs.put("annonces", pAnnonces);
 			renderTemplate("Annonces/annonces.html");
 		}
 
 		render();
 	}
 
 	public static void creation(Annonce annonce) {
 		if (Security.isConnected()) {
 			List<Ville> lesVilles = Ville.find("ORDER BY nom").fetch();
 			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 					.first();
 			if (moi.maVoiture == null) {
 				flash.error("Merci d'enregistrer votre véhicule avant de poster une annonce.");
 				render();
 			}
 			render(annonce, lesVilles, moi);
 		}
 		render();
 	}
 
 	public static void enregistrerannonce(Annonce annonce,
 			@Required Long depart, @Required Long arrivee,
 			@Required String dateDepart, @Required String heureDepart,
 			String mesEscales) throws ParseException {
 		validation.valid(dateDepart);
 		validation.valid(heureDepart);
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			creation(annonce);
 		} else {
 			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 					.first();
 
 			Ville villeDepart = Ville.findById(depart);
 			Ville villeArrivee = Ville.findById(arrivee);
 
 			Date madate = retournerDate(dateDepart, heureDepart);
 			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
 			dateFormat.format(madate);
 
 			List<Ville> etapes = new ArrayList<Ville>();
 			StringTokenizer st = new StringTokenizer(mesEscales, " ");
 			while (st.hasMoreTokens()) {
 				Ville v = Ville.findById(Long.parseLong(st.nextToken()));
 				etapes.add(v);
 			}
 
 			Trajet monTrajet = new Trajet(madate, villeDepart, villeArrivee,
 					etapes);
 			annonce.placesRestantes = Integer.parseInt(moi.maVoiture.nbPlaces) - 1;
 			annonce.monTrajet = monTrajet;
 			annonce.monUtilisateur = Utilisateur.find("byEmail",
 					Security.connected()).first();
 
 			monTrajet.save();
 			annonce.save();
 
 			flash.success("Annonce enregistrée");
 			mesannonces();
 
 		}
 	}
 
 	public static void demandeparticipation(long id) {
 		Annonce annonce = Annonce.find("byId", id).first();
		int derniereVille = annonce.monTrajet.mesEtapes.size() - 1;
		annonce.monTrajet.mesEtapes.remove(derniereVille);
 		render(annonce);
 		// Utilisateur passager = Utilisateur
 		// .find("byEmail", Security.connected()).first();
 		// annonce.mesDemandePassagers.add(passager);
 		// annonce.save();
 		// flash.success("Demande envoyée");
 		// details(id);
 
 	}
 
 	public static void annulerparticipation(long id) {
 		if (Security.isConnected()) {
 			Annonce annonce = Annonce.findById(id);
 			Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 					.first();
 			List<MesPassagers> m = MesPassagers.find(
 					"byMesPassagers_idAndAnnonce_id", moi.id, annonce.id)
 					.fetch();
 			MesPassagers dernier = m.get(m.size() - 1);
 			annonce.placesRestantes += dernier.nbPassagers;
 			annonce.mesPassagers.remove(dernier);
 
 			annonce.save();
 			details(annonce.id);
 		}
 	}
 
 	public static void cherchervillesarrivees(Long depart, Long annonce_id) {
 		Annonce annonce = Annonce.findById(annonce_id);
 		List<Ville> villesSuivantes = annonce.monTrajet.mesEtapes;
		Collections.reverse(villesSuivantes);
 		Ville villeDepart = Ville.findById(depart);
 		int ville = villesSuivantes.lastIndexOf(villeDepart);
		int villeFin = villesSuivantes.size() - 1;
		for (int i = villeFin; i >= ville; i--) {
 			villesSuivantes.remove(i);
 		}
		Collections.reverse(villesSuivantes);
 		render(villesSuivantes);
 	}
 
 	public static void calculertarifparpersonne(Long ville_depart,
 			Long ville_arrivee, int nb_personnes, String type_voiture,
 			Long tarif_base, Long id_annonce) {
 
 		int tarifTotal = 0, kmsTotal = 0;
 		Ville villeDepart = Ville.findById(ville_depart);
 		Ville villeArrivee = Ville.findById(ville_arrivee);
 		List<Ville> mesEtapes = toutesLesEtapes(villeDepart, villeArrivee);
 		for (int i = 0; i < mesEtapes.size() - 1; i++) {
 			Troncon troncon = Troncon.find(
 					"byVilleActuelle_idAndVilleSuivante_id",
 					mesEtapes.get(i).id, mesEtapes.get(i + 1)).first();
 			kmsTotal += troncon.nbKms;
 		}
 		// coefficient d'essence
 
 		if (type_voiture.equals("petite"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.20);
 		else if (type_voiture.equals("moyenne"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.35);
 		else if (type_voiture.equals("grande"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.50);
 
 		int nbPersonneTotalSurTroncon = 0;
 		Annonce annonce = Annonce.findById(id_annonce);
 		for (MesPassagers p : annonce.mesPassagers) {
 			if (p.villeDebut.id == ville_depart) {
 				nbPersonneTotalSurTroncon += p.nbPassagers;
 			}
 		}
 
 		tarifTotal = tarifTotal
 				/ (nbPersonneTotalSurTroncon + nb_personnes + 1);
 
 		render(tarifTotal);
 	}
 
 	public static void sauvegarderparticipation(Long id, Long depart,
 			Long arrivee, int nbPassagers, int prixParPassager) {
 
 		Utilisateur moi = Utilisateur.find("byEmail", Security.connected())
 				.first();
 
 		Annonce annonce = Annonce.findById(id);
 		Ville villeDebut = Ville.findById(depart);
 		Ville villeFin = Ville.findById(arrivee);
 
 		DemandeAnnonceEnAttente demande = new DemandeAnnonceEnAttente(annonce,
 				moi, nbPassagers, villeDebut, villeFin, prixParPassager);
 		demande.save();
 		annonce.mesDemandePassagers.add(demande);
 		annonce.save();
 
 		flash.success("Demande de participation envoyée avec succès.");
 		details(id);
 	}
 
 	public static List<Ville> toutesLesEtapes(Ville villeDepart,
 			Ville villeArrivee) {
 		List<Ville> mesEtapes = new ArrayList<Ville>();
 		List<Troncon> lesTroncons = Troncon.find("").fetch();
 		ArrayList<Noeud> fileAStar = new ArrayList<Noeud>();
 		if (villeDepart != villeArrivee) {
 			Noeud n = new Noeud(villeDepart,
 					villeDepart.calculerDistanceVers(villeArrivee), 0, null);
 			Noeud.insererNoeudHeuristique(n, fileAStar);
 			while (!fileAStar.isEmpty()
 					&& !fileAStar.get(0).getVille().equals(villeArrivee)) {
 				Noeud nP = fileAStar.get(0);
 				fileAStar.remove(0);
 				lesTroncons.clear();
 				lesTroncons = Troncon.find("byVilleActuelle", nP.getVille())
 						.fetch();
 				for (Troncon t : lesTroncons) {
 					if (t.villeSuivante != nP.getVille()) {
 						Noeud.insererNoeudHeuristique(
 								new Noeud(t.villeSuivante, t.villeSuivante
 										.calculerDistanceVers(villeArrivee)
 										+ nP.getDistReelle() + t.nbKms, t.nbKms
 										+ nP.getDistReelle(), nP), fileAStar);
 					}
 				}
 				// System.out.println(nP.getVille().nom);
 			}
 
 			// Recuperation du trajet optimal avec les noeuds parents
 			if (!fileAStar.isEmpty()) {
 				Noeud noeud = fileAStar.get(0);
 				while (noeud != null) {
 					mesEtapes.add(noeud.getVille());
 					noeud = noeud.getNoeudParent();
 				}
 				Collections.reverse(mesEtapes);
 			}
 		} else {
 			mesEtapes.add(villeDepart);
 			mesEtapes.add(villeArrivee);
 		}
 		return mesEtapes;
 	}
 
 	public static void chercheretapes(Long depart, Long arrivee,
 			String mesAnciennesEtapes, String type_voiture) {
 		List<Long> mesAnciennes = new ArrayList<Long>();
 		if (mesAnciennesEtapes == "") {
 			StringTokenizer st = new StringTokenizer(mesAnciennesEtapes, "-");
 			while (st.hasMoreTokens()) {
 				mesAnciennes.add(Long.parseLong(st.nextToken()));
 			}
 		}
 
 		// Création de la liste des étapes
 		List<Ville> mesEtapes = new ArrayList<Ville>();
 		// Recuperation des villes
 		Ville villeDepart = Ville.findById(depart);
 		Ville villeArrivee = Ville.findById(arrivee);
 
 		// Appelle de la fonction toutesLesEtapes qui correspond à A*
 		mesEtapes = toutesLesEtapes(villeDepart, villeArrivee);
 
 		int tarif = 0;
 		if (depart != arrivee)
 			tarif = calculertarif(mesEtapes, type_voiture);
 
 		render(mesEtapes, depart, arrivee, mesAnciennes, tarif);
 	}
 
 	public static void chercheretapeschargement(Long depart, Long arrivee,
 			String mesAnciennesEtapes, String type_voiture, String ancien_tarif) {
 		List<Long> mesAnciennes = new ArrayList<Long>();
 		if (mesAnciennesEtapes != "") {
 			StringTokenizer st = new StringTokenizer(mesAnciennesEtapes, "-");
 			while (st.hasMoreTokens()) {
 				mesAnciennes.add(Long.parseLong(st.nextToken()));
 			}
 		}
 
 		// Création de la liste des étapes
 		List<Ville> mesEtapes = new ArrayList<Ville>();
 		// Recuperation des villes
 		Ville villeDepart = Ville.findById(depart);
 		Ville villeArrivee = Ville.findById(arrivee);
 
 		// Appelle de la fonction toutesLesEtapes qui correspond à A*
 		mesEtapes = toutesLesEtapes(villeDepart, villeArrivee);
 
 		int tarif = 0;
 		if (depart != arrivee)
 			tarif = calculertarif(mesEtapes, type_voiture);
 
 		render(mesEtapes, depart, arrivee, mesAnciennes, tarif, ancien_tarif);
 	}
 
 	public static int calculertarif(List<Ville> mesEtapes, String type_voiture) {
 		int tarifTotal = 0, kmsTotal = 0;
 		for (int i = 0; i < mesEtapes.size() - 1; i++) {
 			Troncon troncon = Troncon.find(
 					"byVilleActuelle_idAndVilleSuivante_id",
 					mesEtapes.get(i).id, mesEtapes.get(i + 1)).first();
 			kmsTotal += troncon.nbKms;
 		}
 		// coefficient d'essence
 		if (type_voiture.equals("petite"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.25);
 		else if (type_voiture.equals("moyenne"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.40);
 		else if (type_voiture.equals("grande"))
 			tarifTotal = (int) Math.ceil((kmsTotal / 10) * 1.55);
 		return tarifTotal;
 	}
 
 	public static void profil(long id) {
 		if (Security.isConnected()) {
 			Utilisateur user = Utilisateur.find("byId", id).first();
 			render(user);
 		}
 		render();
 	}
 
 	public static void supprimerannonce(long id) {
 		if (Security.isConnected()) {
 			Annonce annonce = Annonce.findById(id);
 			annonce.mesDemandePassagers.clear();
 			annonce.save();
 			annonce.mesPassagers.clear();
 			annonce.save();
 			MesPassagers.delete("annonce_id like ?", id);
 			Annonce.delete("id like ?", id);
 
 			mesannonces();
 
 		}
 	}
 }

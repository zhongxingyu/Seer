 package controllers;
 
 import java.util.ArrayList;
 
 import models.Utilisateur;
 import models.Voiture;
 import play.*;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.mvc.*;
 
 public class Utilisateurs extends Controller {
 	@Before
 	static void addDefaults() {
 		renderArgs.put("covamiTitle",
 				Play.configuration.getProperty("covami.title"));
 		renderArgs.put("covamiBaseline",
 				Play.configuration.getProperty("covami.baseline"));
 	}
 
 	@Before
 	static void setConnectedUser() {
 		if (Security.isConnected()) {
 			Utilisateur user = Utilisateur
 					.find("byEmail", Security.connected()).first();
 			renderArgs.put("user", user);
 			renderArgs.put("security", Security.connected());
 			flash.success("Connexion réussie");
 		}
 	}
 
 	public static void moncompte() {
 		flash.clear();
 		render();
 	}
 
 	public static void editermoncompte(Utilisateur usr, Voiture v, String c1,
 			String c2, String c3) {
 		if (Security.isConnected()) {
 			Utilisateur user = Utilisateur
 					.find("byEmail", Security.connected()).first();
 			v = user.maVoiture;
			if (!user.mesCriteres.isEmpty()) {
 				for (String c : user.mesCriteres) {
 					if (c.equals("animaux")) {
 						c1 = c;
 					} else if (c.equals("musique")) {
 						c2 = c;
 					} else if (c.equals("fumeur")) {
 						c3 = c;
 					}
 				}
 			}
 			render(user, v, c1, c2, c3);
 		}
 		render();
 	}
 
 	public static void sauvegardermoncompte(@Required @Valid Utilisateur user,
 			Voiture v, String c1, String c2, String c3) {
 		validation.valid(user);
 		if (validation.hasErrors()) {
 			// add http parameters to the flash scope
 			params.flash();
 			// keep the errors for the next request
 			validation.keep();
 			editermoncompte(user, v, c1, c2, c3);
 		} else {
 			if (v.type.equals("-1") || v.nbPlaces.equals("-1")) {
 				user.maVoiture = null;
 			} else {
 				v.save();
 				user.maVoiture = v;
 			}
 			ArrayList<String> mesCriteres = new ArrayList<String>();
 			if (c1 != null && c1.equals("animaux")) {
 				mesCriteres.add("animaux");
 			}
 			if (c2 != null && c2.equals("musique")) {
 				mesCriteres.add("musique");
 			}
 			if (c3 != null && c3.equals("fumeur")) {
 				mesCriteres.add("fumeur");
 			}
 			if (!mesCriteres.isEmpty()) {
 				user.mesCriteres = mesCriteres;
 			}
 			user.save(); // explicit save here
 			flash.success("Sauvegarde réussie");
 			moncompte();
 		}
 	}
 
 	public static void inscription(Utilisateur user, Voiture v, String c1,
 			String c2, String c3) {
 		if (Security.isConnected()) {
 			Application.index();
 		}
 		render(user, v, c1, c2, c3);
 	}
 
 	public static void enregistrerinscription(@Valid Utilisateur user,
 			Voiture v, String c1, String c2, String c3) {
 		validation.valid(user);
 		// validation.valid(v);
 		if (validation.hasErrors()) {
 			// add http parameters to the flash scope
 			params.flash();
 			// keep the errors for the next request
 			validation.keep();
 			inscription(user, v, c1, c2, c3);
 		} else {
 			if (Utilisateur.find("byEmail", user.email).first() != null) {
 				flash.error("E-mail existant");
 				inscription(user, v, c1, c2, c3);
 			} else {
 				if (v.type.equals("-1") || v.nbPlaces.equals("-1")) {
 					user.maVoiture = null;
 				} else {
 					v.save();
 					user.maVoiture = v;
 				}
 				ArrayList<String> mesCriteres = new ArrayList<String>();
 				if (c1 != null && c1.equals("animaux")) {
 					mesCriteres.add("animaux");
 				}
 				if (c2 != null && c2.equals("musique")) {
 					mesCriteres.add("musique");
 				}
 				if (c3 != null && c3.equals("fumeur")) {
 					mesCriteres.add("fumeur");
 				}
 				if (!mesCriteres.isEmpty()) {
 					user.mesCriteres = mesCriteres;
 				}
 				user.save(); // explicit save here
 				flash.success("Inscription réussie");
 				if (!Security.authentify(user.email, user.password)) {
 					flash.error("Erreur d'authentification");
 					return;
 				}
 				session.put("username", user.email);
 				flash.success("Votre compte a bien été créé.");
 				Application.index();
 			}
 		}
 		Application.index();
 	}
 
 }

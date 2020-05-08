 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import play.*;
 import play.jobs.*;
 import play.test.*;
 
 import models.*;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	@SuppressWarnings("deprecation")
 	public void doJob() {
 		(new Pays("France")).save();
 		if (Ville.count() == 0) {
 			initialiserVilles();
 		}
 		if (Troncon.count() == 0) {
 			initialiserTroncons();
 		}
 		if (Utilisateur.count() == 0) {
 			Fixtures.load("initial-data.yml");
 		}
 
 	}
 
 	public void initialiserVilles() {
 		Pays france = Pays.find("byNom", "France").first();
 		try {
 			BufferedReader fichier = new BufferedReader(new FileReader(
 					"public/csv/villes.csv"));
 			String chaine;
 			while ((chaine = fichier.readLine()) != null) {
 				// Sépare à l'aide du ; la ligne dans un tableau de chaines
 				String[] champs = chaine.split(";");
 				// Ajout de la ville en cours
 				(new Ville(champs[0], champs[1], champs[2],
 						Float.parseFloat(champs[3]),
 						Float.parseFloat(champs[4]), france)).save();
 			}
 			fichier.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void initialiserTroncons() {
 		// Sert à savoir si le champs parser est le nom (le premier champs d'une
 		// ligne)
 		String autoroute = "";
 		String ville_precedente = "";
 		try {
 			BufferedReader fichier = new BufferedReader(new FileReader(
 					"public/csv/autoroutes.csv"));
 			String chaine;
 			while ((chaine = fichier.readLine()) != null) {
 				StringTokenizer st = new StringTokenizer(chaine, ";");
 				while (st.hasMoreTokens()) {
 
 					if (autoroute == "") {
 						// Si c'est le premier champs, c'est le nom de
 						// l'autoroute
 						autoroute = st.nextToken();
 
 						// premier tronçon
 						Ville actuelle = Ville.find("byCodeInsee",
 								st.nextToken()).first();
 
 						Ville suivante = Ville.find("byCodeInsee",
 								st.nextToken()).first();
 						(new Troncon(autoroute, suivante, actuelle,
 								distanceVolOiseau(actuelle, suivante))).save();
 						(new Troncon(autoroute, actuelle, suivante,
 								distanceVolOiseau(actuelle, suivante))).save();
 
 						ville_precedente = suivante.codeInsee;
 
 					} else {
 						// Sinon c'est la ville à ajouter à un nouveau tronçon
 						Ville actuelle = Ville.find("byCodeInsee",
 								ville_precedente).first();
 
 						Ville suivante = Ville.find("byCodeInsee",
 								st.nextToken()).first();
 
 						(new Troncon(autoroute, suivante, actuelle,
 								distanceVolOiseau(actuelle, suivante))).save();
 						(new Troncon(autoroute, actuelle, suivante,
 								distanceVolOiseau(actuelle, suivante))).save();
 
						ville_precedente = suivante.codeInsee;
 					}
 				}
 				// Reaffectation la var à null pour la prochaine ligne
 				autoroute = "";
 			}
 			fichier.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public double distanceVolOiseau(Ville a, Ville b) {
 		return 6367445 * Math.acos(Math.sin(Math.PI * a.latitude / 180)// 6367445
 																		// =
 																		// rayon
 																		// de la
 																		// terre
 				* Math.sin(Math.PI * b.latitude / 180)
 				+ Math.cos(Math.PI * a.latitude / 180)
 				* Math.cos(Math.PI * b.latitude / 180)
 				* Math.cos((Math.PI * a.longitude / 180)
 						- (Math.PI * b.longitude / 180))) / 1000;
 	}
 }

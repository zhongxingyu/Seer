 package org.ecn.edtemps.models.inflaters;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.managers.BddGestion;
 import org.ecn.edtemps.models.identifie.EvenementComplet;
 import org.ecn.edtemps.models.identifie.SalleIdentifie;
 import org.ecn.edtemps.models.identifie.UtilisateurIdentifie;
 
 /**
  * Génération d'une salle complète à partir d'une ligne de base de données.
  * Il est préférable de réutiliser un EvenementCompletInflater pour créer un grand nombre d'événements
  * car les matières et types des calendriers sont mémorisés pour éviter de faire plusieurs fois un même requête.
  * 
  * @author Remi
  */
 public class EvenementCompletInflater extends AbsEvenementInflater<EvenementComplet> {
 	
 	// Mise en cache de certaines matières/types
 	protected Hashtable<Integer, String> matieresCalendriers;
 	protected Hashtable<Integer, String> typesCalendriers;
 	
 	public EvenementCompletInflater() {
 		matieresCalendriers = new Hashtable<Integer, String>();
 		typesCalendriers = new Hashtable<Integer, String>();
 	}
 	
 	@Override
 	public EvenementComplet inflate(String nom, Date dateDebut, Date dateFin, ArrayList<Integer> idCalendriers, Integer idCreateur,
 			ArrayList<SalleIdentifie> salles, ArrayList<UtilisateurIdentifie> intervenants, 
 			ArrayList<UtilisateurIdentifie> responsables, int id, ResultSet reponse, BddGestion bdd) throws DatabaseException {
 		
 		ArrayList<String> matieres = new ArrayList<String>();
 		ArrayList<String> types = new ArrayList<String>();
 		
 		// Récupération des matières et types depuis le cache de cet inflater si possible
 		boolean allCached = true;
 		for(int i : idCalendriers) {
 			if(!matieresCalendriers.containsKey(i)) {
 				allCached = false;
 				break;
 			}
 		}
 		
 		if(allCached) {
 			for(int i : idCalendriers) {
 				matieres.add(matieresCalendriers.get(i));
 				types.add(typesCalendriers.get(i));
 			}
 		}
 		else {
 			// Récupération des types et matières depuis la base
 			ResultSet reponseCalendriers = bdd.executeRequest("SELECT matiere.matiere_nom, typecalendrier.typecal_libelle " +
 					"FROM edt.calendrier " +
					"LEFT JOIN edt.matiere ON matiere.matiere_id=calendrier.matiere_id " +
					"LEFT JOIN edt.typecalendrier ON typecalendrier.typecal_id=calendrier.typecal_id " +
 					"INNER JOIN edt.evenementappartient ON evenementappartient.cal_id=calendrier.cal_id " +
 					"AND evenementappartient.eve_id=" + id);
 
 			try {
 				while(reponseCalendriers.next()) {
					String nomMatiere = reponseCalendriers.getString("matiere_nom");
					if(nomMatiere != null) {
						matieres.add(nomMatiere);
					}
					
					String libelleType = reponseCalendriers.getString("typecal_libelle");
					if(libelleType != null) {
						types.add(libelleType);
					}
 				}
 				reponseCalendriers.close();
 				
 				// Mise en cache
 				if(matieres.size() == 1) { // cas le plus fréquent
 					for(int i : idCalendriers) {
 						matieresCalendriers.put(i, matieres.get(0));
 						typesCalendriers.put(i, types.get(0)); // types doit être de la même taille que matieres
 					}
 				}
 				
 			} catch (SQLException e) {
 				throw new DatabaseException(e);
 			}
 		}
 		
 		return new EvenementComplet(nom, dateDebut, dateFin, idCalendriers, idCreateur, salles, intervenants, responsables, 
 				id, matieres, types);
 	}
 
 }

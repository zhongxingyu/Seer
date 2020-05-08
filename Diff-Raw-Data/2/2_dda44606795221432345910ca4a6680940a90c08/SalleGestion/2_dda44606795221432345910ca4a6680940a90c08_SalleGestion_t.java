 package org.ecn.edtemps.managers;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.exceptions.EdtempsException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.models.Materiel;
 import org.ecn.edtemps.models.Salle;
 import org.ecn.edtemps.models.identifie.SalleIdentifie;
 import org.ecn.edtemps.models.identifie.SalleRecherche;
 import org.ecn.edtemps.models.inflaters.SalleIdentifieInflater;
 import org.ecn.edtemps.models.inflaters.SalleRechercheInflater;
 
 /**
  * Classe de gestion des salles
  * 
  * @author Joffrey
  */
 public class SalleGestion {
 
 	/** Gestionnaire de base de données */
 	protected BddGestion _bdd;
 
 	/**
 	 * Initialise un gestionnaire de salles
 	 * @param bdd Gestionnaire de base de données à utiliser
 	 */
 	public SalleGestion(BddGestion bdd) {
 		_bdd = bdd;
 	}
 
 	/**
 	 * Récupérer une salle dans la base de données
 	 * @param identifiant Identifiant de la salle à récupérer
 	 * @param createTransaction Indique si il faut créer une transaction (sinon appeler la méthode dans une transaction)
 	 * @return la salle
 	 * @throws EdtempsException
 	 */
 	public SalleIdentifie getSalle(int identifiant, boolean createTransaction) throws EdtempsException {
 
 		SalleIdentifie salleRecuperee = null;
 
 		try {
 
 			if(createTransaction) {
 				_bdd.startTransaction();
 			}
 
 			// Récupère la salle en base
 			ResultSet requeteSalle = _bdd.executeRequest("SELECT * FROM edt.salle WHERE salle_id=" + identifiant);
 
 			// Accède au premier élément du résultat
 			if (requeteSalle.next()) {
 				salleRecuperee = new SalleIdentifieInflater().inflateSalle(requeteSalle, _bdd);
 			}
 			requeteSalle.close();
 
 			if(createTransaction) {
 				_bdd.commit();
 			}
 
 		} catch (DatabaseException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		} catch (SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 		return salleRecuperee;
 
 	}
 
 	/**
 	 * Modifie une salle en base de données
 	 * @param salle Salle à modifier
 	 * @throws EdtempsException
 	 */
 	public void modifierSalle(SalleIdentifie salle) throws EdtempsException {
 
 		if (salle==null) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR,
 					"Tentative d'enregistrer un objet NULL en base de données.");
 		}
 
 		int id = salle.getId();
 		String nom = salle.getNom();
 		String batiment = salle.getBatiment();
 		Integer niveau = salle.getNiveau();
 		Integer numero = salle.getNumero();
 		Integer capacite = salle.getCapacite();
 		ArrayList<Materiel> materiels = salle.getMateriels();
 
 		// Vérification de la cohérence des valeurs
 		if (StringUtils.isBlank(nom)) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR,
 					"Tentative d'enregistrer une salle en base de données sans nom.");
 		}
 		
 		if(!StringUtils.isAlphanumericSpace(nom)) {
 			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom de la salle doit être alphanumérique");
 		}
 			
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 
 			// Vérifie que le nom de la salle n'est pas déjà pris
 			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT salle_id FROM edt.salle WHERE salle_nom=?");
 			nomDejaPris.setString(1, nom);
 			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
 			if (nomDejaPrisResult.next()) {
 				int idRecupere = nomDejaPrisResult.getInt(1); 
 				if (idRecupere!=id) {
 					throw new EdtempsException(ResultCode.NAME_TAKEN,
 							"Tentative d'enregistrer une salle en base de données avec un nom déjà utilisé.");
 				}
 			}
 			nomDejaPrisResult.close();
 
 			// Prépare la requête
 			PreparedStatement requete = _bdd.getConnection().prepareStatement(
 					"UPDATE edt.salle SET" +
 					" salle_batiment=?" +
 					", salle_niveau=" + niveau +
 					", salle_numero=" + numero +
 					", salle_capacite=" + capacite +
 					", salle_nom=? WHERE salle_id=" + id);
 			requete.setString(1, batiment);
 			requete.setString(2, nom);
 
 			// Exécute la requête
 			requete.execute();
 			
 			// Suppression de l'ancienne liste des matériels
 			_bdd.executeUpdate("DELETE FROM edt.contientmateriel WHERE salle_id="+id);
 
 			// Ajout des liens avec le matériel
 			for (Materiel materiel : materiels) {
 				if (materiel.getQuantite()>0) {
 					_bdd.executeUpdate("INSERT INTO edt.contientmateriel (salle_id, materiel_id, contientmateriel_quantite) " +
 							"VALUES (" + id + ", " + materiel.getId() + ", " + materiel.getQuantite() + ")");
 				}
 			}
 
 			// Termine la transaction
 			_bdd.commit();
 
 		} catch (DatabaseException | SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 	}
 
 	/**
 	 * Enregistrer une salle dans la base de données
 	 * @param salle Salle à enregistrer
 	 * @return l'identifiant de la ligne insérée
 	 * @throws EdtempsException
 	 */
 	public int sauverSalle(Salle salle) throws EdtempsException {
 
 		int idInsertion = -1;
 		
 		if (salle==null) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR,
 					"Tentative d'enregistrer un objet NULL en base de données.");
 		}
 
 		String nom = salle.getNom();
 		String batiment = salle.getBatiment();
 		Integer niveau = salle.getNiveau();
 		Integer numero = salle.getNumero();
 		Integer capacite = salle.getCapacite();
 		ArrayList<Materiel> materiels = salle.getMateriels();
 		
 		// Vérification de la cohérence des valeurs
 		if (StringUtils.isBlank(nom)) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR,
 					"Tentative d'enregistrer une salle en base de données sans nom.");
 		}
 
 		if(!StringUtils.isAlphanumericSpace(nom)) {
 			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom de la salle doit être alphanumérique");
 		}
 
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 
 			// Vérifie que le nom de la salle n'est pas déjà pris
 			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT * FROM edt.salle WHERE salle_nom=?");
 			nomDejaPris.setString(1, nom);
 			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
 			if (nomDejaPrisResult.next()) {
 				throw new EdtempsException(ResultCode.NAME_TAKEN,
 						"Tentative d'enregistrer une salle en base de données avec un nom déjà utilisé.");
 			}
 			nomDejaPrisResult.close();
 
 			// Ajoute la salle dans la bdd et récupère l'identifiant de la ligne
 			PreparedStatement requete = _bdd.getConnection().prepareStatement(
 					"INSERT INTO edt.salle (salle_batiment, salle_niveau, salle_numero, salle_capacite, salle_nom) " +
 					"VALUES (?, " + niveau + ", " + numero + ", " + capacite + ", ?) RETURNING salle_id");
 			requete.setString(1, batiment);
 			requete.setString(2, nom);
 
 			// Exécute la requête
 			ResultSet resultat = requete.executeQuery();
 
 			// Récupère l'identifiant de la ligne ajoutée
 			resultat.next();
 			idInsertion = resultat.getInt(1);
 			resultat.close();
 
 			// Ajout des liens avec le matériel
 			for (Materiel materiel : materiels) {
 				if (materiel.getQuantite()>0) {
 					_bdd.executeUpdate("INSERT INTO edt.contientmateriel (salle_id, materiel_id, contientmateriel_quantite) " +
 							"VALUES (" + idInsertion + ", " + materiel.getId() + ", " + materiel.getQuantite() + ")");
 				}
 			}
 
 			// Termine la transaction
 			_bdd.commit();
 
 		} catch (DatabaseException | SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 		return idInsertion;
 	}
 
 	/**
 	 * Supprime une salle en base de données
 	 * @param idSalle Identifiant de la salle à supprimer
 	 * @throws DatabaseException
 	 */
 	public void supprimerSalle(int idSalle) throws DatabaseException {
 
 		// Démarre une transaction
 		_bdd.startTransaction();
 
 		// Supprime le matériel
 		_bdd.executeUpdate("DELETE FROM edt.contientmateriel WHERE salle_id=" + idSalle);
 
 		// Supprime les liens avec les événements
 		_bdd.executeUpdate("DELETE FROM edt.alieuensalle WHERE salle_id=" + idSalle);
 
 		// Supprime la salle
 		_bdd.executeUpdate("DELETE FROM edt.salle WHERE salle_id=" + idSalle);
 
 		// Termine la transaction
 		_bdd.commit();
 
 	}
 
 
 	/**
 	 * Listing des salles disponibles pour la création d'un nouvel événement
 	 * @param dateDebut Date de début de l'événement
 	 * @param dateFin Date de fin de l'événement
 	 * @param materiels Liste de matériel nécessaire dans la salle recherchée
 	 * @param capacite Nombre de personne que la salle doit pouvoir accueillir
 	 * @param sallesOccupeesNonCours Renvoyer aussi les salles occupées par des évènements autres que des cours
 	 * @param createTransaction Nécessité de créer les transactions dans cette méthode, sinon appeler dans une transaction
 	 * @param idEvenementIgnorer Evénement à ignorer en recherchant la disponibilité (si on modifie un événement notamment)
 	 * @return Liste des salles disponibles
 	 * @throws DatabaseException
 	 */
 	public ArrayList<SalleRecherche> rechercherSalle(Date dateDebut, Date dateFin, ArrayList<Materiel> materiels, 
 			int capacite, boolean sallesOccupeesNonCours, boolean createTransaction, Integer idEvenementIgnorer) throws DatabaseException {
 
 		String requeteString =
 		"SELECT salle.salle_id, salle.salle_batiment, salle.salle_niveau, salle.salle_nom, salle.salle_numero, salle.salle_capacite, " +
 				"COUNT(evenement.eve_id)>0 AS salle_est_occupe FROM edt.salle";
 		
 	    // Join avec les matériels que la salle contient et qui sont nécessaires, si il y en a
 		if (!materiels.isEmpty()) {
 			requeteString += " LEFT JOIN edt.contientmateriel ON salle.salle_id = contientmateriel.salle_id AND (";
 			for (int i = 0 ; i < materiels.size() ; i++) {
 				if (i!=0) {
 					requeteString += " OR ";
 				}
 				requeteString += "(contientmateriel.materiel_id = "+materiels.get(i).getId()+" AND contientmateriel.contientmateriel_quantite >= "+materiels.get(i).getQuantite()+")";
 			}
 			requeteString += ")";
 		}
 
 		// Join avec les évènements qui se passent dans la salle au créneau demandé
 		requeteString += " LEFT JOIN edt.alieuensalle ON alieuensalle.salle_id = salle.salle_id "
 				+ "LEFT JOIN edt.evenement ON evenement.eve_id = alieuensalle.eve_id " 
 				+ "AND (evenement.eve_datedebut < ?) AND (evenement.eve_datefin > ?) ";
 		
 		// S'il faut ignorer un événement
 		if (idEvenementIgnorer!=null && idEvenementIgnorer>0) {
 			requeteString += "AND (evenement.eve_id <> "+idEvenementIgnorer+") ";
 		}
 		
 		// Lien avec les groupes de participants : repérer si l'évènement de la salle est un cours
 		if(sallesOccupeesNonCours) {
 		    requeteString += 
 			    "LEFT JOIN edt.evenementappartient ON evenement.eve_id=evenementappartient.eve_id " + 
 			    "LEFT JOIN edt.calendrierappartientgroupe ON evenementappartient.cal_id=calendrierappartientgroupe.cal_id " +
 			    "LEFT JOIN edt.groupeparticipant groupecours ON calendrierappartientgroupe.groupeparticipant_id=groupecours.groupeparticipant_id " +
			    "AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) ";
 		}
 		
 		// Vérifie la capacité de la salle
 	    requeteString += "WHERE salle.salle_capacite>=" + capacite
 	    		+ " GROUP BY salle.salle_id "; // On somme les matériels *par salle*
 		
 	    if(sallesOccupeesNonCours) {
 	    	// Aucun évènement de cours dans le créneau donné
 	    	requeteString += "HAVING COUNT(groupecours.groupeparticipant_id)=0";
 	    }
 	    else {
 	    	// Aucun évènement qui se passe dans la salle au créneau demandé (LEFT JOIN, donc aucune correspondance -> colonnes null)
 	    	requeteString += "HAVING COUNT(evenement.eve_id)=0";
 	    }
 	    
 		if(!materiels.isEmpty()) {
 			// Le nombre de types de matériels que la salle contient et qui sont nécessaires correspond avec le nombre de matériels demandés
 			requeteString += " AND COUNT(DISTINCT contientmateriel.materiel_id) = "+materiels.size();
 		}
 	    
 		requeteString += " ORDER BY salle.salle_capacite";
 
 		ArrayList<SalleRecherche> resultatRecherche = new ArrayList<SalleRecherche>();
 		try {
 			// Prépare la requête
 			PreparedStatement requetePreparee = _bdd.getConnection().prepareStatement(requeteString);
 			requetePreparee.setTimestamp(1, new java.sql.Timestamp(dateFin.getTime()));
 			requetePreparee.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
 			
 		    // Effectue la requête
 			ResultSet requete = requetePreparee.executeQuery();
 
 			// Balayage pour chaque élément retour de la requête
 			SalleRechercheInflater inflater = new SalleRechercheInflater(dateDebut, dateFin, createTransaction);
 			while(requete.next()) {
 				resultatRecherche.add(inflater.inflateSalle(requete, _bdd));
 			}
 
 			// Ferme la requête
 			requete.close();
 
 			// Retourne le résultat de la recherche
 			return resultatRecherche;
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	
 	/**
 	 * Récupération des salles dans lesquelles se déroulent un évènement
 	 * @param evenementId ID de l'évènement concerné
 	 * @return Liste des salles enregistrées
 	 * @throws DatabaseException 
 	 */
 	public ArrayList<SalleIdentifie> getSallesEvenement(int evenementId) throws DatabaseException {
 		
 		ResultSet reponse = _bdd.executeRequest("SELECT salle.salle_id, salle.salle_nom, salle.salle_batiment, salle.salle_niveau," +
 				"salle.salle_numero, salle.salle_capacite " +
 				"FROM edt.salle INNER JOIN edt.alieuensalle ON alieuensalle.salle_id = salle.salle_id " +
 				"AND alieuensalle.eve_id = " + evenementId);
 		
 		ArrayList<SalleIdentifie> res = new ArrayList<SalleIdentifie>();
 		try {
 			SalleIdentifieInflater inflater = new SalleIdentifieInflater();
 			while(reponse.next()) {
 				res.add(inflater.inflateSalle(reponse, _bdd));
 			}
 			
 			reponse.close();
 			
 			return res;
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	/**
 	 * Indique si des salles sont libres pendant un créneau donné.
 	 * 
 	 * @param idSalles ID des salles à vérifier
 	 * @param dateDebut Début du créneau
 	 * @param dateFin Fin du créneau
 	 * @return true si toutes les salles sont libres ; false sinon
 	 * @throws DatabaseException
 	 */
 	public boolean sallesLibres(List<Integer> idSalles, Date dateDebut, Date dateFin) throws DatabaseException {
 		return sallesLibres(idSalles, dateDebut, dateFin, null);
 	}
 	
 	/**
 	 * Indique si des salles sont libres pendant un créneau donné.
 	 * Une valeur null pour idEvenementIgnorer n'ignore aucun événement
 	 * 
 	 * @param idSalles ID des salles à vérifier
 	 * @param dateDebut Début du créneau
 	 * @param dateFin Fin du créneau
 	 * @param idEvenementIgnorer Evénement à ignorer en recherchant la disponibilité (si on modifie un événement notamment)
 	 * @return true si toutes les salles sont libres ; false sinon
 	 * @throws DatabaseException
 	 */
 	public boolean sallesLibres(List<Integer> idSalles, Date dateDebut, Date dateFin, Integer idEvenementIgnorer) throws DatabaseException {
 		try {
 			
 			String strIdSalles = StringUtils.join(idSalles, ",");
 			
 			String requete = "SELECT COUNT(evenement.eve_id) FROM edt.evenement " +
 					"INNER JOIN edt.alieuensalle ON alieuensalle.eve_id=evenement.eve_id AND alieuensalle.salle_id IN (" + strIdSalles + ") " +
 					"WHERE evenement.eve_datedebut < ? AND evenement.eve_datefin > ?";
 			
 			if(idEvenementIgnorer != null) {
 				requete += " AND evenement.eve_id <> " + idEvenementIgnorer;
 			}
 			
 			PreparedStatement statement = _bdd.getConnection().prepareStatement(requete);
 			
 			statement.setTimestamp(1, new java.sql.Timestamp(dateFin.getTime()));
 			statement.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
 			
 			ResultSet response = statement.executeQuery();
 			
 			response.next();
 			
 			return response.getInt(1) == 0;
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}	
 	}
 
 	
 	/**
 	 * Lister toutes les salles de la base de données
 	 * @return la liste des salles récupérées
 	 * @throws DatabaseException
 	 */
 	public List<SalleIdentifie> listerToutesSalles() throws DatabaseException {
 		
 		try {
 			
 			ResultSet requete = _bdd.executeRequest("SELECT salle_id, salle_batiment, salle_nom," +
 					" salle_niveau, salle_numero, salle_capacite FROM edt.salle ORDER BY salle_nom");
 
 			List<SalleIdentifie> res = new ArrayList<SalleIdentifie>();
 			while(requete.next()) {
 				res.add(new SalleIdentifieInflater().inflateSalle(requete, _bdd));
 			}
 			requete.close();
 			
 			return res;
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}	
 	}
 
 }

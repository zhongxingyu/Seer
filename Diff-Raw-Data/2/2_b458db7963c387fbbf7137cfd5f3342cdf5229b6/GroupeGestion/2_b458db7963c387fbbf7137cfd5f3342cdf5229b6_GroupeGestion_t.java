 package org.ecn.edtemps.managers;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.exceptions.EdtempsException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.managers.UtilisateurGestion.ActionsEdtemps;
 import org.ecn.edtemps.models.identifie.CalendrierComplet;
 import org.ecn.edtemps.models.identifie.GroupeComplet;
 import org.ecn.edtemps.models.identifie.GroupeIdentifie;
 import org.ecn.edtemps.models.identifie.GroupeIdentifieAbonnement;
 import org.ecn.edtemps.models.inflaters.CalendrierCompletInflater;
 import org.ecn.edtemps.models.inflaters.GroupeCompletInflater;
 import org.ecn.edtemps.models.inflaters.GroupeIdentifieInflater;
 
 /**
  * Classe de gestion des groupes de participants
  * 
  * @author Joffrey
  */
 public class GroupeGestion {
 	
 	/** Gestionnaire de base de données */
 	protected BddGestion _bdd;
 
 	public static final String NOM_TEMPTABLE_ABONNEMENTS = "tmp_requete_abonnements_groupe";
 	public static final String NOM_TEMPTABLE_PARENTSENFANTS = "tmp_requete_parents_enfants_groupe";
 	public static final String NOM_TEMPTABLE_ENFANTS = "tmp_requete_enfants_groupe";
 	
 	/** Nombre maximum de groupes qu'un utilisateur peut créer */
 	public static final int LIMITE_GROUPES_PAR_UTILISATEUR = 20;
 
 	/** Nombre maximum de groupes qu'un utilisateur peut créer avec un droit étendu */
 	public static final int LIMITE_GROUPES_PAR_UTILISATEUR_ETENDUE = 100;
 	
 	/** Nombre de caractères maximum pour le nom d'un groupe */
 	public static final int TAILLE_MAX_NOM_GROUPE = 50;
 
 
 	/**
 	 * Initialise un gestionnaire de groupes de participants
 	 * @param bdd Gestionnaire de base de données à utiliser
 	 */
 	public GroupeGestion(BddGestion bdd) {
 		_bdd = bdd;
 	}
 	
 
 	/**
 	 * Récupérer un groupe identifié de participants dans la base de données
 	 * @param identifiant Identifiant du groupe à récupérer
 	 * @return le groupe identifié (standard)
 	 * @throws EdtempsException
 	 */
 	public GroupeIdentifie getGroupe(int identifiant) throws DatabaseException {
 
 		GroupeIdentifie groupeRecupere = null;
 
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 
 			// Récupère le groupe en base
 			ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp," +
 							"groupeparticipant_estcours, groupeparticipant_estcalendrierunique, groupeparticipant_createur" +
 							" FROM edt.groupeparticipant WHERE groupeparticipant_id="+identifiant);
 
 			// Accède au premier élément du résultat
 			if(requeteGroupe.next()) {
 				groupeRecupere = new GroupeIdentifieInflater().inflateGroupe(requeteGroupe, _bdd);
 			}
 			requeteGroupe.close();
 
 		} catch (DatabaseException | SQLException e) {
 			throw new DatabaseException(e);
 		}
 
 		return groupeRecupere;
 	}
 	
 
 	/**
 	 * Récupérer un groupe complet de participants dans la base de données
 	 * @param identifiant Identifiant du groupe à récupérer
 	 * @return le groupe complet
 	 * @throws EdtempsException
 	 */
 	public GroupeComplet getGroupeComplet(int identifiant) throws EdtempsException {
 
 		GroupeComplet groupeRecupere = null;
 
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 
 			// Récupère le groupe en base
 			ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp," +
 							"groupeparticipant_estcours, groupeparticipant_estcalendrierunique, groupeparticipant_createur" +
 							" FROM edt.groupeparticipant WHERE groupeparticipant_id="+identifiant);
 
 			// Accède au premier élément du résultat
 			if(requeteGroupe.next()) {
 				groupeRecupere = new GroupeCompletInflater().inflateGroupe(requeteGroupe, _bdd);
 			}
 			requeteGroupe.close();
 		} catch (DatabaseException | SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 		return groupeRecupere;
 	}
 	
 	
 	/**
 	 * Enregistrer un groupe de participants en base de données
 	 * @param nom Nom du groupe
 	 * @param idGroupeParent Identifiant du groupe parent
 	 * @param rattachementAutorise VRAI si le groupe accepte le rattachement
 	 * @param estCours VRAI si le groupe est un cours
 	 * @param listeIdProprietaires Liste des identifiants des propriétaires du groupe
 	 * @return l'identifiant de la ligne insérée
 	 * @throws EdtempsException
 	 */
 	public int sauverGroupe(String nom, Integer idGroupeParent, boolean rattachementAutorise, boolean estCours, List<Integer> listeIdProprietaires, Integer userId) throws EdtempsException {
 
 		int idInsertion = -1;
 		
 		if (StringUtils.isBlank(nom) || CollectionUtils.isEmpty(listeIdProprietaires) || userId==null) {
 			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe doit avoir un nom, un créateur et au moins un responsable");
 		}
 
 		if (nom.length() > TAILLE_MAX_NOM_GROUPE || !StringUtils.isAlphanumericSpace(nom)) {
 			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un groupe doit être alphanumérique et de moins de " + TAILLE_MAX_NOM_GROUPE + " caractères");
 		}
 		
 		// Vérifier que l'utilisateur a le droit de créer un groupe de cours
 		UtilisateurGestion userGestion = new UtilisateurGestion(_bdd);
 		if (estCours && !userGestion.aDroit(ActionsEdtemps.CREER_GROUPE_COURS, userId)) {
 			throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR, "Action non autorisée");
 		}
 		
 		// Vérifier que l'utilisateur n'a pas créé trop de groupes
 		boolean limiteEtendue = userGestion.aDroit(ActionsEdtemps.LIMITE_CALENDRIERS_ETENDUE, userId);
 		int nbGroupesDejaCrees = this.getNombresGroupesCrees(userId);
 		int nbGroupesAutorisesACreer = limiteEtendue ? LIMITE_GROUPES_PAR_UTILISATEUR_ETENDUE : LIMITE_GROUPES_PAR_UTILISATEUR;
 		if (nbGroupesDejaCrees >= nbGroupesAutorisesACreer) {
 			throw new EdtempsException(ResultCode.QUOTA_EXCEEDED, "L'utilisateur a dépassé son quota de création de groupes de participants");
 		}
 		
 		// Vérifie que le créateur fait partie de la liste des propriétaires
 		if (!listeIdProprietaires.contains(userId)) {
 			throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Le créateur doit faire partie de la liste des propriétaires");
 		}
 
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 
 			// Vérifie que le nom n'est pas déjà en base de données
 			PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT * FROM edt.groupeparticipant WHERE groupeparticipant_nom=?");
 			nomDejaPris.setString(1, nom);
 			ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
 			if (nomDejaPrisResult.next()) {
 				throw new EdtempsException(ResultCode.NAME_TAKEN,
 						"Tentative d'enregistrer un groupe en base de données avec un nom déjà utilisé.");
 			}
 			nomDejaPrisResult.close();
 			
 			// Prépare la requête avec un traitement différent si un groupe parent a été indiqué
 			PreparedStatement req = null;
 			if (idGroupeParent == null) {
 				// Requête simple dans le cas où pas de groupe parent
 				req = _bdd.getConnection().prepareStatement("INSERT INTO edt.groupeparticipant (groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_estcalendrierunique, groupeparticipant_estcours, groupeparticipant_createur) VALUES (" +
 						"?, '" + rattachementAutorise + "', 'FALSE', '"+ estCours +"', "+userId+") RETURNING groupeparticipant_id ");
 				req.setString(1, nom);
 			} else {
 				// Requête pour récupérer la liste des propriétaires du groupe parent
 				List<Integer> listeProprioGpParent = _bdd.recupererIds(_bdd.getConnection().prepareStatement(
 						"SELECT utilisateur_id FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+idGroupeParent), "utilisateur_id");
 				
 				// Préparation de la requête avec un idParent temporaire si le propriétaire du groupe parent n'est pas l'utilisateur en cours
 				req = _bdd.getConnection().prepareStatement("INSERT INTO edt.groupeparticipant (groupeparticipant_nom, groupeparticipant_rattachementautorise, "+(listeProprioGpParent.contains(userId) ? "groupeparticipant_id_parent" : "groupeparticipant_id_parent_tmp")+", groupeparticipant_estcalendrierunique, groupeparticipant_estcours, groupeparticipant_createur) VALUES (" +
 						"?, '"	+ rattachementAutorise + "', " + idGroupeParent + ", 'FALSE', '"+ estCours +"', "+userId+") RETURNING groupeparticipant_id");
 				req.setString(1, nom);
 			}
 
 			// Exécute la requête
 			ResultSet resultat = req.executeQuery(); 
 
 			// Récupère l'identifiant de la ligne ajoutée
 			resultat.next();
 			idInsertion = resultat.getInt(1);
 			resultat.close();
 
 			// Ajout des propriétaires
 			if (CollectionUtils.isNotEmpty(listeIdProprietaires)) {
 				for (Integer idProprietaire : listeIdProprietaires) {
 					_bdd.executeUpdate("INSERT INTO edt.proprietairegroupeparticipant (utilisateur_id, groupeparticipant_id) VALUES (" +
 							idProprietaire + ", "	+
 							idInsertion	+ ")");
 				}
 			} else {
 				throw new EdtempsException(ResultCode.DATABASE_ERROR,
 						"Tentative d'enregistrer un groupe en base de données sans propriétaire.");
 			}
 
 			// Termine la transaction
 			_bdd.commit();
 
 		} catch (DatabaseException | SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 		return idInsertion;
 	}
 
 
 	/**
 	 * Modifier un groupe de participants en base de données
 	 * @param id Identifiant du groupe
 	 * @param nom Nom du groupe
 	 * @param idGroupeParent Identifiant du groupe parent
 	 * @param rattachementAutorise VRAI si le groupe accepte le rattachement
 	 * @param estCours VRAI si le groupe est un cours
 	 * @param listeIdProprietaires Liste des identifiants des propriétaires du groupe
 	 * @throws EdtempsException
 	 */
 	public void modifierGroupe(int id, String nom, Integer idGroupeParent, boolean rattachementAutorise, 
 			boolean estCours, List<Integer> listeIdProprietaires, int userId) throws EdtempsException {
 
 		if(StringUtils.isBlank(nom) || CollectionUtils.isEmpty(listeIdProprietaires)) {
 			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe doit avoir un nom et au moins un responsable");
 		}
 
 		if(nom.length() > TAILLE_MAX_NOM_GROUPE || !StringUtils.isAlphanumericSpace(nom)) {
 			throw new EdtempsException(ResultCode.ALPHANUMERIC_REQUIRED, "Le nom d'un groupe doit être alphanumérique et de moins de " + TAILLE_MAX_NOM_GROUPE + " caractères");
 		}
 
 		if(idGroupeParent != null && idGroupeParent.equals(id)) {
 			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un groupe ne peut pas être son propre parent");
 		}
 		
 		// Vérification si l'utilisateur a le droit de créer un groupe de cours
 		UtilisateurGestion userGestion = new UtilisateurGestion(_bdd);
 		if (estCours && !userGestion.aDroit(ActionsEdtemps.CREER_GROUPE_COURS, userId)) {
 			throw new EdtempsException(ResultCode.AUTHORIZATION_ERROR);
 		}
 
 		try {
 
 			// Démarre une transaction
 			_bdd.startTransaction();
 			
 			// Vérification des rattachements cycliques (pas de rattachement à un de ses fils)
 			if(idGroupeParent != null) {
 				makeTempTableListeEnfants(_bdd, id);
 				
 				ResultSet resRattachementCyclique = _bdd.executeRequest("SELECT groupeparticipant_id FROM " + NOM_TEMPTABLE_ENFANTS +
 						" WHERE groupeparticipant_id=" + idGroupeParent);
 				
 				if(resRattachementCyclique.next()) {
 					resRattachementCyclique.close();
 					throw new EdtempsException(ResultCode.INVALID_OBJECT, "Impossible d'utiliser un enfant d'un groupe comme groupe parent");
 				}
 				
 				resRattachementCyclique.close();
 			}
 			
 			if (StringUtils.isNotBlank(nom)) {
 
 				// Récupération de l'ancien groupe
 				ResultSet ancienGroupeParent = _bdd.executeRequest(
 						"SELECT groupeparticipant_nom, groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_createur" +
 						" FROM edt.groupeparticipant WHERE groupeparticipant_id="+id);
 				ancienGroupeParent.next();
 				String ancienNom = ancienGroupeParent.getString("groupeparticipant_nom");
 				int ancienIdParent = ancienGroupeParent.getInt("groupeparticipant_id_parent");
 				int ancienIdParentTmp = ancienGroupeParent.getInt("groupeparticipant_id_parent_tmp");
 				int createur = ancienGroupeParent.getInt("groupeparticipant_createur");
 				ancienGroupeParent.close();
 				
 				// Vérifie que le créateur fait partie de la liste des propriétaires
 				if (!listeIdProprietaires.contains(createur)) {
 					throw new EdtempsException(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Le créateur doit faire partie de la liste des propriétaires");
 				}
 
 				// Vérifie que le nom n'est pas déjà en base de données
 				if (!StringUtils.equals(nom, ancienNom)) {
 					PreparedStatement nomDejaPris = _bdd.getConnection().prepareStatement("SELECT COUNT(*) FROM edt.groupeparticipant WHERE groupeparticipant_nom=?");
 					nomDejaPris.setString(1, nom);
 					ResultSet nomDejaPrisResult = nomDejaPris.executeQuery();
 					nomDejaPrisResult.next();
 					if (nomDejaPrisResult.getInt(1)>0) {
 						throw new EdtempsException(ResultCode.NAME_TAKEN, "Tentative de modifier un groupe en base de données avec un nom déjà utilisé.");
 					}
 					nomDejaPrisResult.close();
 				}
 				
 				// Prépare la requête avec un traitement différent si un groupe parent a été indiqué (else) 
 				PreparedStatement req = null;
 				if (idGroupeParent == null) {
 					
 					// Suppression du lien avec un potentiel groupe parent antérieur
 					_bdd.executeUpdate("UPDATE edt.groupeparticipant SET" +
 						" groupeparticipant_id_parent=NULL, groupeparticipant_id_parent_tmp=NULL" +
 						" WHERE groupeParticipant_id="+id);
 					
 					// Préparation de la requête
 					req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
 							" groupeParticipant_nom=?," +
 							" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
 							" groupeparticipant_estcours='"+estCours+"'," +
 							" groupeparticipant_id_parent=NULL" +
 							" WHERE groupeParticipant_id="+id);
 				} else {
 					// S'il n'y a pas eu de modification
 					if (idGroupeParent.equals(ancienIdParent) || idGroupeParent.equals(ancienIdParentTmp)) {
 						// Préparation de la requête
 						req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
 								" groupeParticipant_nom=?," +
 								" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
 								" groupeparticipant_estcours='"+estCours+"'" +
 								" WHERE groupeParticipant_id=" + id);
 					} else {
 						
 						// Requête pour récupérer la liste des propriétaires du groupe parent
 						List<Integer> listeProprioGpParent = _bdd.recupererIds(_bdd.getConnection().prepareStatement(
 								"SELECT utilisateur_id FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id="+idGroupeParent), "utilisateur_id");
 					
 						// Préparation de la requête avec un idParent temporaire si le propriétaire du groupe parent n'est pas l'utilisateur en cours 
 						req = _bdd.getConnection().prepareStatement("UPDATE edt.groupeparticipant SET" +
 								" groupeParticipant_nom=?," +
 								" groupeParticipant_rattachementAutorise='"+rattachementAutorise+"'," +
 								" groupeparticipant_estcours='"+estCours+"'," +
 								" groupeparticipant_id_parent="+(listeProprioGpParent.contains(userId) ? idGroupeParent : "NULL")+"," +
 								" groupeparticipant_id_parent_tmp="+(listeProprioGpParent.contains(userId) ? "NULL" : idGroupeParent) +
 								" WHERE groupeParticipant_id=" + id);
 					}
 					
 				}
 				
 				// Execute la requête
 				req.setString(1, nom);
 				req.execute();
 
 				// Supprime les liens avec les propriétaires
 				_bdd.executeUpdate("DELETE FROM edt.proprietairegroupeparticipant WHERE groupeParticipant_id=" + id);
 
 				// Ajout des nouveaux propriétaires
 				if (CollectionUtils.isNotEmpty(listeIdProprietaires)) {
 					for (int idProprietaire : listeIdProprietaires) {
 						_bdd.executeUpdate("INSERT INTO edt.proprietairegroupeparticipant (utilisateur_id, groupeParticipant_id) VALUES ("
 								+ idProprietaire + ", " + id + ")");
 					}
 				} else {
 					throw new EdtempsException(ResultCode.INVALID_OBJECT,
 							"Tentative de modifier un groupe en base de données sans propriétaire.");
 				}
 
 				
 			} else {
 				throw new EdtempsException(ResultCode.DATABASE_ERROR,
 						"Tentative de modifier un groupe en base de données sans nom.");
 			}
 
 			// Termine la transaction
 			_bdd.commit();
 
 		} catch (DatabaseException | SQLException e) {
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 
 	}
 
 	/**
 	 * Supprime un groupe en base de données
 	 * @param idGroupe Identifiant du groupe à supprimer
 	 * @param startTransaction Démarrer une transaction dans cette méthode. Sinon la méthode doit être appelée dans une transaction
 	 * @throws EdtempsException Si le groupe n'est pas supprimable
 	 */
 	public void supprimerGroupe(int idGroupe, boolean startTransaction) throws DatabaseException {
 		supprimerGroupe(idGroupe, startTransaction, false);
 	}
 	
 	/**
 	 * Supprime un groupe en base de données
 	 * @param idGroupe Identifiant du groupe à supprimer
 	 * @param startTransaction Démarrer une transaction dans cette méthode. Sinon la méthode doit être appelée dans une transaction
 	 * @param ignorerVerifUnique Ignorer la vérification "le groupe n'est pas un groupe calendrier unique"
 	 * @throws EdtempsException Si le groupe n'est pas supprimable
 	 */
 	public void supprimerGroupe(int idGroupe, boolean startTransaction, boolean ignorerVerifUnique) throws DatabaseException {
 
 		// Vérifie si c'est un groupe unique et, le cas échéant, arrêter la suppression
 		if (!ignorerVerifUnique && this.getGroupe(idGroupe).estCalendrierUnique()) {
 			throw new DatabaseException("Impossible de supprimer le groupe unique lié à un calendrier.");
 		}
 
 		// Démarre une transaction
 		if(startTransaction) {
 			_bdd.startTransaction();
 		}
 
 		// Supprime les liens avec les propriétaires
 		_bdd.executeUpdate("DELETE FROM edt.proprietairegroupeparticipant WHERE groupeparticipant_id=" + idGroupe);
 
 		// Supprime les liens avec les calendriers
 		_bdd.executeUpdate("DELETE FROM edt.calendrierappartientgroupe WHERE groupeparticipant_id=" + idGroupe);
 
 		// Supprime les abonnements
 		_bdd.executeUpdate("DELETE FROM edt.abonnegroupeparticipant WHERE groupeparticipant_id=" + idGroupe);
 
 		// Pour tous les fils, suppression du parent ID
 		_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent=NULL WHERE groupeparticipant_id_parent=" + idGroupe);
 		
 		// Supprime le groupe
 		_bdd.executeUpdate("DELETE FROM edt.groupeparticipant WHERE groupeparticipant_id=" + idGroupe);
 
 		// Termine la transaction
 		if(startTransaction) {
 			_bdd.commit();
 		}
 	}
 	
 	/**
 	 * Créé une table temporaire de listing de groupes
 	 * @param bdd Gestionnaire de base de données
 	 * @param nomTable Nom de la table à créer
 	 * @throws DatabaseException
 	 */
 	protected static void makeTempTableListeGroupes(BddGestion bdd, String nomTable) throws DatabaseException {
 		bdd.executeUpdate("CREATE TEMP TABLE " + nomTable + " (groupeparticipant_id INTEGER NOT NULL, groupeparticipant_nom VARCHAR, " +
 			"groupeparticipant_rattachementautorise BOOLEAN NOT NULL,groupeparticipant_id_parent INTEGER, groupeparticipant_id_parent_tmp INTEGER, groupeparticipant_estcours BOOLEAN, " +
 			"groupeparticipant_estcalendrierunique BOOLEAN NOT NULL, groupeparticipant_createur INTEGER) ON COMMIT DROP");
 	}
 	
 	protected static void dropTempTableListeGroupes(BddGestion bdd, String nomTable) throws DatabaseException {
 		bdd.executeUpdate("DROP TABLE " + nomTable);
 	}
 	
 	/**
 	 * Itère sur les parents des lignes d'une temporaire de groupes pour les ajouter à cette même table
 	 * @param bdd Gestionnaire de base de données
 	 * @param nomTable Nom de la table temporaire à utiliser
 	 * @throws DatabaseException
 	 */
 	protected static void completerParentsTempTableListeGroupes(BddGestion bdd, String nomTable) throws DatabaseException {
 		try {
 			int nbInsertions = -1;
 			
 			PreparedStatement statementParents = bdd.getConnection().prepareStatement("INSERT INTO " + nomTable + "(groupeparticipant_id," +
 					"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 					"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
 					"SELECT DISTINCT parent.groupeparticipant_id," +
 					"parent.groupeparticipant_nom, parent.groupeparticipant_rattachementautorise," +
 					"parent.groupeparticipant_id_parent, parent.groupeparticipant_id_parent_tmp, parent.groupeparticipant_estcours, parent.groupeparticipant_estcalendrierunique " +
 					"FROM edt.groupeparticipant parent " +
 					"INNER JOIN " + nomTable + " enfant " +
 					"ON parent.groupeparticipant_id=enfant.groupeparticipant_id_parent " +
 					"LEFT JOIN " + nomTable + " deja_inseres " +
 					"ON deja_inseres.groupeparticipant_id=parent.groupeparticipant_id " +
 					"WHERE deja_inseres.groupeparticipant_id IS NULL");
 			
 			while(nbInsertions != 0) {
 				nbInsertions = statementParents.executeUpdate();
 			}
 			
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	/**
 	 * Itère sur les enfants des lignes d'une temporaire de groupes pour les ajouter à cette même table
 	 * @param bdd Gestionnaire de base de données
 	 * @param nomTable Nom de la table temporaire à utiliser
 	 * @throws DatabaseException
 	 */
 	protected static void completerEnfantsTempTableListeGroupes(BddGestion bdd, String nomTable) throws DatabaseException {
 
 		PreparedStatement statementEnfants;
 		try {
 			
 			int nbInsertions = -1;
 			statementEnfants = bdd.getConnection().prepareStatement("INSERT INTO " + nomTable + "(groupeparticipant_id," +
 					"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 					"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
 					"SELECT DISTINCT enfant.groupeparticipant_id," +
 					"enfant.groupeparticipant_nom, enfant.groupeparticipant_rattachementautorise," +
 					"enfant.groupeparticipant_id_parent, enfant.groupeparticipant_id_parent_tmp, enfant.groupeparticipant_estcours, enfant.groupeparticipant_estcalendrierunique " +
 					"FROM edt.groupeparticipant enfant " +
 					"INNER JOIN " + nomTable + " parent " +
 					"ON parent.groupeparticipant_id=enfant.groupeparticipant_id_parent " +
 					"LEFT JOIN " + nomTable + " deja_inseres " +
 					"ON deja_inseres.groupeparticipant_id=enfant.groupeparticipant_id " +
 					"WHERE deja_inseres.groupeparticipant_id IS NULL");
 			
 			while(nbInsertions != 0) {
 				nbInsertions = statementEnfants.executeUpdate();
 			}
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 
 	
 	/**
 	 * Créé une table temporaire de groupes d'utilisateur contenant les abonnements de l'utilisateur fourni.
 	 * Les abonnements listés dans cette table comprennent les abonnements directs, mais aussi les parents et enfants dans l'arbre.
 	 * La table temporaire contient les mêmes colonnes que la table groupeparticipant.
 	 * Elle est supprimée automatiquement lors d'un commit. Cette méthode doit donc être appelée à l'intérieur d'une transaction.
 	 * Le nom de la table créée est défini par la constante {@link GroupeGestion#NOM_TEMPTABLE_ABONNEMENTS}
 	 * @param bdd Gestionnaire de base de données
 	 * @param idUtilisateur
 	 * @throws DatabaseException
 	 */
 	public static void makeTempTableListeGroupesAbonnement(BddGestion bdd, int idUtilisateur) throws DatabaseException {
 		// Création d'une table temporaire pour les résultats
 		makeTempTableListeGroupes(bdd, NOM_TEMPTABLE_ABONNEMENTS);
 		
 		// Ajout des abonnements directs
 		bdd.executeUpdate("INSERT INTO " + NOM_TEMPTABLE_ABONNEMENTS + "(groupeparticipant_id," +
 				"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 				"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique, groupeparticipant_createur) " +
 				"SELECT groupeparticipant.groupeparticipant_id," +
 				"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 				"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique, groupeparticipant_createur " +
 				"FROM edt.groupeparticipant " +
 				"INNER JOIN edt.abonnegroupeparticipant ON abonnegroupeparticipant.groupeparticipant_id=groupeparticipant.groupeparticipant_id " +
 				"AND abonnegroupeparticipant.utilisateur_id=" + idUtilisateur);
 		
 		completerEnfantsTempTableListeGroupes(bdd, NOM_TEMPTABLE_ABONNEMENTS);
 		completerParentsTempTableListeGroupes(bdd, NOM_TEMPTABLE_ABONNEMENTS);
 	}
 	
 	
 	/**
 	 * Ajouter un groupe dans la table temporaire des groupes
 	 * @param bdd Gestionnaire de base de données
 	 * @param idGroupe Identifiant du groupe à ajouter
 	 * @param nomTable Nom de la table temporaire à affecter
 	 * @throws DatabaseException
 	 */
 	protected static void ajouterGroupeTempTableListeGroupes(BddGestion bdd, int idGroupe, String nomTable) throws DatabaseException {
 		ajouterGroupesTempTableListeGroupes(bdd, "SELECT groupeparticipant.groupeparticipant_id," +
 			"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 			"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique " +
 			"FROM edt.groupeparticipant " +
 			"WHERE groupeparticipant.groupeparticipant_id = " + idGroupe, nomTable);
 	}
 	
 	/**
 	 * Ajoute des groupes dans la table temporaire des groupes
 	 * @param bdd Gestionnaire de base de données
 	 * @param selectRequest Requête SELECT permettant d'obtenir les groupes
 	 * @param nomTable Nom de la table temporaire à affecter
 	 * @throws DatabaseException
 	 */
 	protected static void ajouterGroupesTempTableListeGroupes(BddGestion bdd, String selectRequest, String nomTable) throws DatabaseException {
 		bdd.executeUpdate("INSERT INTO " + nomTable + "(groupeparticipant_id," +
 			"groupeparticipant_nom, groupeparticipant_rattachementautorise," +
 			"groupeparticipant_id_parent, groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique) " +
 			selectRequest);
 	}
 	
 	/**
 	 * Créé une table temporaire de groupes d'utilisateur étant <b>parents ou enfants</b> du groupe fourni. Le groupe lui-même est aussi listé.
 	 * La table temporaire contient les mêmes colonnes que la table groupeparticipant.
 	 * Elle est supprimée automatiquement lors d'un commit. Cette méthode doit donc être appelée à l'intérieur d'une transaction.
 	 * <b>Cette méthode ne peut être appelée qu'une fois par transaction</b>
 	 * Le nom de la table créée est défini par la constante {@link GroupeGestion#NOM_TEMPTABLE_PARENTSENFANTS}
 	 * @param bdd Gestionnaire de base de données
 	 * @param idGroupe ID du groupe pour lequel les parents et enfants sont à lister.
 	 * @throws DatabaseException
 	 */
 	public static void makeTempTableListeParentsEnfants(BddGestion bdd, int idGroupe) throws DatabaseException {
 		// Création de la table temporaire
 		makeTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 		
 		ajouterGroupeTempTableListeGroupes(bdd, idGroupe, NOM_TEMPTABLE_PARENTSENFANTS);
 		
 		completerEnfantsTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 		completerParentsTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 	}
 	
 	public static void dropTempTableListeParentsEnfants(BddGestion bdd) throws DatabaseException {
 		dropTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 	}
 	
 	/**
 	 * Créé une table temporaire de groupes d'utilisateur étant <b>parents ou enfants</b> des groupes récupérés par une requête SELECT. 
 	 * Les groupes récupérés sont aussi listés.
 	 * La table temporaire contient les mêmes colonnes que la table groupeparticipant.
 	 * Elle est supprimée automatiquement lors d'un commit. Cette méthode doit donc être appelée à l'intérieur d'une transaction.
 	 * <b>Cette méthode ne peut être appelée qu'une fois par transaction</b>
 	 * Le nom de la table créée est défini par la constante {@link GroupeGestion#NOM_TEMPTABLE_PARENTSENFANTS}
 	 * @param bdd Gestionnaire de base de données
 	 * @param idGroupe ID du groupe pour lequel les parents et enfants sont à lister.
 	 * @throws DatabaseException
 	 */
 	public static void makeTempTableListeParentsEnfants(BddGestion bdd, String selectRequest) throws DatabaseException {
 		makeTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 		ajouterGroupesTempTableListeGroupes(bdd, selectRequest, NOM_TEMPTABLE_PARENTSENFANTS);
 		
 		completerEnfantsTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 		completerParentsTempTableListeGroupes(bdd, NOM_TEMPTABLE_PARENTSENFANTS);
 	}
 	
 	/**
 	 * Créé une table temporaire de groupes d'utilisateur étant <b>enfants</b> du groupe fourni. Le groupe lui-même est aussi listé.
 	 * La table temporaire contient les mêmes colonnes que la table groupeparticipant.
 	 * Elle est supprimée automatiquement lors d'un commit. Cette méthode doit donc être appelée à l'intérieur d'une transaction.
 	 * <b>Cette méthode ne peut être appelée qu'une fois par transaction</b>
 	 * Le nom de la table créée est défini par la constante {@link GroupeGestion#NOM_TEMPTABLE_ENFANTS}
 	 * @param bdd Gestionnaire de base de données
 	 * @param idGroupe ID du groupe pour lequel les parents et enfants sont à lister.
 	 * @throws DatabaseException
 	 */
 	public static void makeTempTableListeEnfants(BddGestion bdd, int idGroupe) throws DatabaseException {
 		// Création de la table temporaire
 		makeTempTableListeGroupes(bdd, NOM_TEMPTABLE_ENFANTS);
 		
 		ajouterGroupeTempTableListeGroupes(bdd, idGroupe, NOM_TEMPTABLE_ENFANTS);
 		
 		completerEnfantsTempTableListeGroupes(bdd, NOM_TEMPTABLE_ENFANTS);
 	}
 	
 	/**
 	 * Listing de l'ensemble des groupes de participants existants en base
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon (false), elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param rattachementAutorise indique s'il faut uniquement lister les groupes dont le rattachement est autorisé
 	 * @return Liste de groupes de participants trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifie> listerGroupes(boolean createTransaction, boolean rattachementAutorise) throws DatabaseException {
 		return listerGroupes(createTransaction, rattachementAutorise, null);
 	}
 	
 	/**
 	 * Listing de l'ensemble des groupes de participants existants en base
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon (false), elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param rattachementAutorise indique s'il faut uniquement lister les groupes dont le rattachement est autorisé
 	 * @param ignorerEnfantsGroupe ID d'un groupe dont les enfants sont à ignorer (ce groupe compris). null pour ne rien préciser.
 	 * @return Liste de groupes de participants trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifie> listerGroupes(boolean createTransaction, boolean rattachementAutorise, Integer ignorerEnfantsGroupe) throws DatabaseException {
 		
 		try {
 			if(createTransaction){
 				_bdd.startTransaction();
 			}
 			
 			String requete = "SELECT grp.groupeparticipant_id, grp.groupeparticipant_nom, grp.groupeparticipant_rattachementautorise, grp.groupeparticipant_createur, "
 					+ "grp.groupeparticipant_id_parent, grp.groupeparticipant_id_parent_tmp, grp.groupeparticipant_estcours, grp.groupeparticipant_estcalendrierunique "
 					+ "FROM edt.groupeparticipant grp";
 			
 			if(ignorerEnfantsGroupe != null) {
 				makeTempTableListeEnfants(_bdd, ignorerEnfantsGroupe);
 				requete += " LEFT JOIN " + NOM_TEMPTABLE_ENFANTS + " enfants ON enfants.groupeparticipant_id = grp.groupeparticipant_id";
 			}
 			
 			if(ignorerEnfantsGroupe != null || rattachementAutorise) {
 				requete += " WHERE";
 				
 				if(rattachementAutorise) {
 					requete += " grp.groupeparticipant_rattachementautorise = TRUE";
 				}
 				
 				if(ignorerEnfantsGroupe != null) {
 					requete += (rattachementAutorise ? " AND" : "") + " enfants.groupeparticipant_id IS NULL";
 				}
 			}
 			
 			// Lecture des groupes de la table
 			ResultSet resGroupes = _bdd.executeRequest(requete);
 			
 			// Création d'objets "groupes identifiés" pour les groupes rencontrés dans la table
 			ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
 			while(resGroupes.next()) {
 				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
 			}
 			resGroupes.close();
 
 			if(createTransaction){
 				_bdd.commit();
 			}
 			
 			return res;
 		}
 		catch(SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 
 	
 	/**
 	 * Listing des groupes auxquels est abonné l'utilisateur, soit directement soit indirectement (par parenté d'un groupe à l'autre)
 	 * @param idUtilisateur ID de l'utilisateur en question
 	 * @param createTransaction Créer une transaction pour les requêtes. Si false, doit obligatoirement être appelé à l'intérieur d'une transaction
 	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
 	 * 
 	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
 	 * 
 	 * @return Liste de groupes trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifie> listerGroupesAbonnement(int idUtilisateur, boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
 		
 		try {
 			if(createTransaction)
 				_bdd.startTransaction(); // Définit la durée de vie de la table temporaire
 			
 			if(!reuseTempTableAbonnements)
 				makeTempTableListeGroupesAbonnement(_bdd, idUtilisateur);
 			
 			// Lecture des groupes de la table
 			ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant_id, groupeparticipant_nom, groupeparticipant_rattachementautorise, groupeparticipant_id_parent, groupeparticipant_createur, " +
 					"groupeparticipant_id_parent_tmp, groupeparticipant_estcours, groupeparticipant_estcalendrierunique, groupeparticipant_createur FROM " + NOM_TEMPTABLE_ABONNEMENTS);
 
 			ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
 			
 			while(resGroupes.next()) {
 				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
 			}
 			resGroupes.close();
 			
 			// Supprime aussi la table temporaire
 			if(createTransaction)
 				_bdd.commit();
 			
 			return res;
 		}
 		catch(SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 
 	
 	/**
 	 * Listing des groupes auxquels n'est pas abonné l'utilisateur directement
 	 * @param idUtilisateur ID de l'utilisateur en question
 	 * 
 	 * @return Liste de groupes trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifieAbonnement> listerGroupesNonAbonnement(int idUtilisateur) throws DatabaseException {
 		
 		try {
 			// Requete des groupes auxquels l'utilisateur n'est pas abonné
 			ResultSet resGroupes = _bdd.executeRequest(
 					"SELECT * FROM edt.groupeparticipant" +
 					" LEFT JOIN (SELECT * FROM edt.abonnegroupeparticipant WHERE utilisateur_id = 9) AS gpesUser" + 
 					" ON (gpesUser.groupeparticipant_id = groupeparticipant.groupeparticipant_id)" + 
 					" WHERE utilisateur_id IS NULL"  
 			);
 			
 			
 			ArrayList<GroupeIdentifieAbonnement> res = new ArrayList<GroupeIdentifieAbonnement>();
 			
 			while(resGroupes.next()) {
 				int id = resGroupes.getInt("groupeparticipant_id");
 				String nom = resGroupes.getString("groupeparticipant_nom");
 				int parentId = resGroupes.getInt("groupeparticipant_id_parent");
 				boolean abonnementObligatoire = resGroupes.getBoolean("abonnementgroupeparticipant_obligatoire");
 				boolean estCalendrierUnique = resGroupes.getBoolean("groupeparticipant_estcalendrierunique");
 				List<Integer> rattachementsDuCalendrier = new ArrayList<Integer>();
 				if (estCalendrierUnique) {
 					ResultSet rs_rattachements = _bdd.executeRequest(
 							"SELECT * FROM edt.calendrierappartientgroupe " +
 							"WHERE cal_id IN (" +
 									"SELECT cal_id " +
 								     "FROM edt.calendrierappartientgroupe " +
 								     "WHERE groupeparticipant_id = " + id + " " +
 								     ") " +
 							"AND groupeparticipant_id != " + id
 					);
 					while(rs_rattachements.next()) {
 						rattachementsDuCalendrier.add(rs_rattachements.getInt("groupeparticipant_id"));
 					}
 					rs_rattachements.close();
 				}
 				res.add(new GroupeIdentifieAbonnement(id, nom, parentId, estCalendrierUnique, abonnementObligatoire, rattachementsDuCalendrier));
 			}
 			resGroupes.close();
 			
 			return res;
 		}
 		catch(SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	/**
 	 * Listing des groupes auxquels n'est pas abonné l'utilisateur directement
 	 * @param idUtilisateur ID de l'utilisateur en question
 	 * 
 	 * @return Liste de groupes trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifieAbonnement> listerGroupesAbonnementDirect(int idUtilisateur) throws DatabaseException {
 		
 		try {
 			// Requete des groupes auxquels l'utilisateur est abonné directement
 			ResultSet resGroupes = _bdd.executeRequest(
 					"SELECT * FROM edt.abonnegroupeparticipant "
 					+ "INNER JOIN edt.groupeparticipant "
 					+ "ON (abonnegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id)"
 					+ "WHERE utilisateur_id = " + idUtilisateur  
 			);
 			
 			ArrayList<GroupeIdentifieAbonnement> res = new ArrayList<GroupeIdentifieAbonnement>();
 			
 			while(resGroupes.next()) {
 				int id = resGroupes.getInt("groupeparticipant_id");
 				String nom = resGroupes.getString("groupeparticipant_nom");
 				int parentId = resGroupes.getInt("groupeparticipant_id_parent");
 				boolean abonnementObligatoire = resGroupes.getBoolean("abonnementgroupeparticipant_obligatoire");
 				boolean estCalendrierUnique = resGroupes.getBoolean("groupeparticipant_estcalendrierunique");
 				List<Integer> rattachementsDuCalendrier = new ArrayList<Integer>();
 				if (estCalendrierUnique) {
 					ResultSet rs_rattachements = _bdd.executeRequest(
 							"SELECT * FROM edt.calendrierappartientgroupe " +
 							"WHERE cal_id IN (" +
 									"SELECT cal_id " +
 								     "FROM edt.calendrierappartientgroupe " +
 								     "WHERE groupeparticipant_id = " + id + " " +
 								     ") " +
 							"AND groupeparticipant_id != " + id
 					);
 					while(rs_rattachements.next()) {
 						rattachementsDuCalendrier.add(rs_rattachements.getInt("groupeparticipant_id"));
 					}
 					rs_rattachements.close();
 				}
 				res.add(new GroupeIdentifieAbonnement(id, nom, parentId, estCalendrierUnique, abonnementObligatoire, rattachementsDuCalendrier));
 			}
 			resGroupes.close();
 			
 			return res;
 		}
 		catch(SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	/**
 	 * Listing des groupes auxquels l'utilisateur est abonné directement (sans remonter ni descendre les parents/enfants)
 	 * @param idUtilisateur Utilisateur dont les abonnements sont à lister
 	 * @return Liste des groupes trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifie> listerGroupesAssocies(int idUtilisateur) throws DatabaseException {
 		ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
 				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
 					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant.groupeparticipant_createur " +
 					"FROM edt.groupeparticipant " +
 					"INNER JOIN edt.abonnegroupeparticipant ON abonnegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id " +
 					"AND abonnegroupeparticipant.utilisateur_id = " + idUtilisateur);
 		
 		ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
 
 		try {
 			while(resGroupes.next()) {
 				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
 			}
 			resGroupes.close();
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 		
 		return res;
 	}
 	
 
 	/**
 	 * Listing des groupes desquels l'utilisateur fait parti des propriétaires (sans remonter ni descendre les parents/enfants)
 	 * @param idUtilisateur Utilisateur dont les groupes sont à lister
 	 * @return Liste des groupes trouvés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<GroupeIdentifie> listerGroupesProprietaire(int idProprietaire) throws DatabaseException {
 		
 		ResultSet resGroupes = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom, " +
 				"groupeparticipant.groupeparticipant_rattachementautorise,groupeparticipant.groupeparticipant_id_parent,groupeparticipant.groupeparticipant_id_parent_tmp," +
 					"groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant.groupeparticipant_createur" +
 					" FROM edt.groupeparticipant" +
 					" INNER JOIN edt.proprietairegroupeparticipant ON proprietairegroupeparticipant.groupeparticipant_id = groupeparticipant.groupeparticipant_id" +
 					" AND groupeparticipant.groupeparticipant_estcalendrierunique=FALSE " +
 					" AND proprietairegroupeparticipant.utilisateur_id="+idProprietaire +
 					" ORDER BY groupeparticipant.groupeparticipant_nom");
 		
 		ArrayList<GroupeIdentifie> res = new ArrayList<GroupeIdentifie>();
 
 		try {
 			while(resGroupes.next()) {
 				res.add(new GroupeIdentifieInflater().inflateGroupe(resGroupes, _bdd));
 			}
 			resGroupes.close();
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Fonction permettant à un utilisateur de s'abonner à un groupe de participants
 	 * @param idUtilisateur
 	 * @param idGroupe
 	 * @param obligatoire : booléen indiquant si le rattachement est obligatoire
 	 * @throws DatabaseException
 	 */
 	public void sAbonner(int idUtilisateur, int idGroupe, boolean obligatoire) throws DatabaseException {
 		_bdd.executeUpdate(
 			"INSERT INTO edt.abonnegroupeparticipant "
 			+ "(utilisateur_id, groupeparticipant_id, abonnementgroupeparticipant_obligatoire) "
 			+ "VALUES (" + idUtilisateur + ", " + idGroupe + ", " + obligatoire + ")"
 		);
 	}
 	
 	/**
 	 * Fonction permettant à un utilisateur de se désabonner d'un groupe de participants
 	 * @param idUtilisateur
 	 * @param idGroupe
 	 * @param uniquementSiCoursNonObligatoire : booléen indiquant si on supprime le rattachement dans le cas où il est obligatoire
 	 * @throws DatabaseException
 	 */
 	public void seDesabonner(int idUtilisateur, int idGroupe, boolean uniquementSiCoursNonObligatoire) throws DatabaseException {
 		String s = "DELETE FROM edt.abonnegroupeparticipant " +
 				   "WHERE utilisateur_id = " + idUtilisateur +
 				   " AND groupeparticipant_id = " + idGroupe ;
 		if (uniquementSiCoursNonObligatoire) {
 			  s += " AND abonnementgroupeparticipant_obligatoire = FALSE" ;
 		}
 		_bdd.executeUpdate(s);
 	}
 	
 	
 	/**
 	 * Récupérer la liste des groupes qui sont en attente de rattachement pour un utilisateur donné
 	 * @param userId Identifiant de l'utilisateur
 	 * @return liste des groupes
 	 * @throws DatabaseException
 	 * @throws SQLException
 	 */
 	public List<GroupeComplet> listerDemandesDeRattachementGroupes(int userId) throws DatabaseException, SQLException {
 		
 		List<GroupeComplet> groupesEnAttenteDeValidation = new ArrayList<GroupeComplet>();
 
 		// Démarre une transaction
 		_bdd.startTransaction();
 
 		// Récupère les groupes qui sont en attente de rattachement
 		ResultSet requeteGroupe = _bdd.executeRequest("SELECT groupeparticipant.groupeparticipant_id, groupeparticipant.groupeparticipant_nom," +
 				" groupeparticipant.groupeparticipant_rattachementautorise, groupeparticipant.groupeparticipant_id_parent, groupeparticipant.groupeparticipant_id_parent_tmp," +
 				" groupeparticipant.groupeparticipant_estcours, groupeparticipant.groupeparticipant_estcalendrierunique, groupeparticipant_createur" +
 				" FROM edt.groupeparticipant WHERE groupeparticipant.groupeparticipant_id_parent_tmp IN" +
 				" (SELECT proprietairegroupeparticipant.groupeparticipant_id" +
 				" FROM edt.proprietairegroupeparticipant WHERE proprietairegroupeparticipant.utilisateur_id="+userId+")");
 
 		// Récupère et traite le résultat
 		while (requeteGroupe.next()) {
 			groupesEnAttenteDeValidation.add(new GroupeCompletInflater().inflateGroupe(requeteGroupe, _bdd));
 		}
 		requeteGroupe.close();
 		
 		return groupesEnAttenteDeValidation;
 	}
 	
 
 	/**
 	 * Récupérer la liste des calendriers qui sont en attente de rattachement pour un utilisateur donné
 	 * @param userId Identifiant de l'utilisateur
 	 * @return liste des groupes
 	 * @throws SQLException
 	 * @throws EdtempsException 
 	 */
 	public List<CalendrierComplet> listerDemandesDeRattachementCalendriers(int userId) throws SQLException, EdtempsException {
 		
 		List<CalendrierComplet> calendriersEnAttenteDeValidation = new ArrayList<CalendrierComplet>();
 
 		// Démarre une transaction
 		_bdd.startTransaction();
 
 		// Récupère les calendriers qui sont en attente de rattachement
		ResultSet requete = _bdd.executeRequest("SELECT DISTINCT A.cal_id, C.cal_nom, C.cal_createur, M.matiere_nom, T.typecal_libelle," +
 				" FALSE AS estcours " + // "met estCours à False : valeur inutile dans cette méthode donc pour éviter la surcharge on met une valeur par défaut
 				" FROM edt.calendrierappartientgroupe A" +
 				" INNER JOIN edt.calendrier C ON C.cal_id=A.cal_id" +
 				" LEFT JOIN edt.matiere M ON M.matiere_id=C.matiere_id" +
 				" LEFT JOIN edt.typecalendrier T ON T.typecal_id=C.typecal_id" +
 				" WHERE A.groupeparticipant_id_tmp IN" +
 				" (SELECT P.groupeparticipant_id" +
 				" FROM edt.proprietairegroupeparticipant P WHERE P.utilisateur_id="+userId+")");
 
 		// Récupère et traite le résultat
 		UtilisateurGestion gestionnaireUtilisateur = new UtilisateurGestion(_bdd);
 		while (requete.next()) {
 			CalendrierComplet cal = new CalendrierCompletInflater().inflateCalendrier(requete, _bdd);
 			cal.setCreateur(gestionnaireUtilisateur.getUtilisateur(cal.getIdCreateur()));
 			
 			calendriersEnAttenteDeValidation.add(cal);
 		}
 		requete.close();
 		
 		return calendriersEnAttenteDeValidation;
 	}
 	
 	/**
 	 * Décide du rattachement (accepté ou refusé) d'un groupe à un groupe de participant
 	 * @param accepte VRAI si le rattachement est accepté
 	 * @param groupeId Identifiant du groupe dont le rattachement a été décidé
 	 * @throws DatabaseException 
 	 */
 	public void deciderRattachementGroupe(boolean accepte, int groupeId) throws DatabaseException {
 		
 		// Démarre une transaction
 		_bdd.startTransaction();
 
 		// Décide le rattachement
 		if (accepte) {
 			_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent=groupeparticipant_id_parent_tmp, groupeparticipant_id_parent_tmp=NULL WHERE groupeparticipant_id="+groupeId);
 		} else {
 			_bdd.executeUpdate("UPDATE edt.groupeparticipant SET groupeparticipant_id_parent_tmp=NULL WHERE groupeparticipant_id="+groupeId);
 		}
 
 		// Termine la transaction
 		_bdd.commit();
 
 	}
 	
 
 	/**
 	 * Décide du rattachement (accepté ou refusé) d'un calendrier à un groupe de participant
 	 * @param accepte VRAI si le rattachement est accepté
 	 * @param groupeIdParent Identifiant du groupe parent
 	 * @param calendrierId Identifiant du calendrier qui demande le rattachement
 	 * @throws DatabaseException 
 	 */
 	public void deciderRattachementCalendrier(boolean accepte, int groupeIdParent, int calendrierId) throws DatabaseException {
 		
 		// Démarre une transaction
 		_bdd.startTransaction();
 
 		// Décide le rattachement
 		if (accepte) {
 			_bdd.executeUpdate("UPDATE edt.calendrierappartientgroupe" +
 					" SET groupeparticipant_id=groupeparticipant_id_tmp, groupeparticipant_id_tmp=NULL" +
 					" WHERE groupeparticipant_id_tmp="+groupeIdParent+" AND cal_id="+calendrierId);
 		} else {
 			_bdd.executeUpdate(
 					"DELETE FROM edt.calendrierappartientgroupe" +
 					" WHERE groupeparticipant_id_tmp="+groupeIdParent+
 					" AND cal_id="+calendrierId);
 		}
 
 		// Termine la transaction
 		_bdd.commit();
 
 	}
 	
 	
 	/**
 	 * Supprimer le propriétaire d'un groupe de participants
 	 * @param proprietaireId Identifiant du propriétaire à supprimer
 	 * @param groupeId Identifiant
 	 * @param startTransaction Indique si une transaction doit être démarrée dans la méthode, sinon elle doit être appelée dans une transaction
 	 * @throws DatabaseException 
 	 * @throws EdtempsException Le groupe n'a pas d'autres propriétaire que celui supprimé
 	 */
 	public void supprimerProprietaire(int proprietaireId, int groupeId, boolean startTransaction) throws EdtempsException {
 
 		// Démarre une transaction
 		if(startTransaction) {
 			_bdd.startTransaction();
 		}
 
 		// Vérifie qu'on n'est pas en train de supprimer le créateur (qui doit rester propriétaire)
 		GroupeIdentifie groupe = getGroupe(groupeId);
 		if(groupe.getIdCreateur() == proprietaireId) {
 			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Le créateur d'un groupe doit en rester propriétaire");
 		}
 		
 		// Supprime le propriétaire pour le groupe
 		_bdd.executeUpdate("DELETE FROM edt.proprietairegroupeparticipant" +
 				" WHERE utilisateur_id="+proprietaireId+" AND groupeparticipant_id="+groupeId);
 	
 		if(startTransaction) {
 			_bdd.commit();
 		}
 	}
 	
 
 	/**
 	 * Récupérer le nombre de groupes créés par un utilisateur
 	 * @param userId Identifiant de l'utilisateur
 	 * @return nombre de groupes
 	 * @throws DatabaseException 
 	 */
 	public int getNombresGroupesCrees(int userId) throws DatabaseException {
 		try {
 			ResultSet res = _bdd.executeRequest("SELECT COUNT(*) FROM edt.groupeparticipant WHERE groupeparticipant_createur="+userId);
 			res.next();
 			int nombre = res.getInt(1);
 			res.close();
 			return nombre;
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 }

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
 import org.ecn.edtemps.models.identifie.EvenementComplet;
 import org.ecn.edtemps.models.identifie.EvenementIdentifie;
 import org.ecn.edtemps.models.inflaters.AbsEvenementInflater;
 import org.ecn.edtemps.models.inflaters.EvenementCompletInflater;
 import org.ecn.edtemps.models.inflaters.EvenementIdentifieInflater;
 
 /** 
  * Classe de gestion des événements
  * 
  * @author Maxime TERRADE
  */
 public class EvenementGestion {
 
 	/** Gestionnaire de base de données */
 	protected BddGestion _bdd;
 	
 	/**
 	 * Initialise un gestionnaire d'evenements
 	 * @param bdd Gestionnaire de base de données à utiliser
 	 */
 	public EvenementGestion(BddGestion bdd) {
 		_bdd = bdd;
 	}
 		
 	
 	/**
 	 * Méthode d'enregistrement d'un evenement dans la base de données
 	 * 
 	 * @param evenement
 	 */
 	public void sauverEvenement(String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<Integer> idSalles, 
 			List<Integer> idIntervenants, List<Integer> idResponsables, boolean startTransaction) throws EdtempsException {
 		
 		if(StringUtils.isBlank(nom) || idCalendriers.isEmpty() || idResponsables.isEmpty()) {
 			throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un évènement doit avoir un nom, un calendrier et un responsable");
 		}
 		
 		try {
 			
 			if(startTransaction) {
 				_bdd.startTransaction();
 			}
 			
 			// Vérification de la disponibilité de la salle
 			SalleGestion salleGestion = new SalleGestion(_bdd);
 			if(!salleGestion.sallesLibres(idSalles, dateDebut, dateFin)) {
 				throw new EdtempsException(ResultCode.SALLE_OCCUPEE, "Une des salles demandées n'est pas/plus libre");
 			}
 			
 			
 			// On crée l'événement dans la base de données
 			PreparedStatement req = _bdd.getConnection().prepareStatement("INSERT INTO edt.evenement "
 					+ "(eve_nom, eve_dateDebut, eve_dateFin) "
 					+ "VALUES (?, ?, ?) "
 				    + "RETURNING eve_id");
 			req.setString(1, nom);
 			req.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
 			req.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
 			
 			ResultSet rsLigneCreee = req.executeQuery();
 			 
 			// On récupère l'id de l'evenement créé
 			rsLigneCreee.next();
 			int idEvenement = rsLigneCreee.getInt("eve_id");
 			rsLigneCreee.close();
 			
 			// On rattache l'evenement aux calendriers
 			for(int idCalendrier : idCalendriers) {
 				_bdd.executeRequest(
 					"INSERT INTO edt.evenementappartient "
 					+ "(eve_id, cal_id) "
 					+ "VALUES (" + idEvenement + ", " + idCalendrier + ")"
 					);
 			}
 			
 			// On rattache l'evenement aux salles
 			for(int idSalle : idSalles) {
 				_bdd.executeRequest(
 					"INSERT INTO edt.alieuensalle "
 					+ "(eve_id, salle_id) "
 					+ "VALUES ("+ idEvenement + ", " + idSalle + ")");
 			}
 			
 			// On indique le(s) responsable(s) dans la base
 			for(int idResponsable : idResponsables) {
 				_bdd.executeRequest(
 					"INSERT INTO edt.responsableevenement "
 					+ "(eve_id, utilisateur_id) "
 					+ "VALUES ("+ idEvenement + ", " + idResponsable + ")");
 			}
 			
 			// On indique le(s) intervenant(s) dans la base
 			for(int idIntervenant : idIntervenants) {
 				_bdd.executeRequest(
 					"INSERT INTO edt.intervenantevenement "
 					+ "(eve_id, utilisateur_id) "
 					+ "VALUES ("+ idEvenement + ", " + idIntervenant + ")");
 			}
 			
 			if(startTransaction) {
 				_bdd.commit();
 			}
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 		
 			
 	}
 	
 	/**
 	 * Modification d'un événement existant (en base de données)
 	 * 
 	 * <p>Permet d'actualiser dans la base de données les anciens attributs d'un événements (date, nom, intervenant, ...)
 	 * avec de nouveaux ayant été modifiés par un utilisateur</p>
 	 * 
 	 * @param 
 	 * @throws DatabaseException Erreur de communication avec la base de données
 	 */
 	public void modifierEvenement(int id, String nom, Date dateDebut, Date dateFin, List<Integer> idCalendriers, List<Integer> idSalles, 
 			List<Integer> idIntervenants, List<Integer> idResponsables, boolean createTransaction) throws EdtempsException{
 		try {
 			
 			if(StringUtils.isBlank(nom) || idCalendriers.isEmpty() || idResponsables.isEmpty()) {
 				throw new EdtempsException(ResultCode.INVALID_OBJECT, "Un évènement doit avoir un nom, un calendrier et un responsable");
 			}
 			
 			//début d'une transaction si requis
 			if (createTransaction){
 				_bdd.startTransaction();
 			}
 			
 			// Vérification de la disponibilité de la salle
 			SalleGestion salleGestion = new SalleGestion(_bdd);
 			if(!salleGestion.sallesLibres(idSalles, dateDebut, dateFin)) {
 				throw new EdtempsException(ResultCode.SALLE_OCCUPEE, "Une des salles demandées n'est pas/plus libre");
 			}
 			
 			// Modifier l'évenement (nom, date début, date fin)
 			PreparedStatement requetePreparee = _bdd.getConnection().prepareStatement(
 					"UPDATE edt.evenement "
 					+ "SET eve_nom = ?, "
 					+ "eve_datedebut = ?, "
 					+ "eve_datefin = ? "
 					+ "WHERE eve_id = " + id);
 			requetePreparee.setString(1, nom);
 			requetePreparee.setTimestamp(2, new java.sql.Timestamp(dateDebut.getTime()));
 			requetePreparee.setTimestamp(3, new java.sql.Timestamp(dateFin.getTime()));
 			requetePreparee.execute();
 			
 			// Modifier  les intervenants de l'évenement (supprimer les anciens puis ajouter les nouveaux)
 			_bdd.executeRequest(
 					"DELETE FROM edt.intervenantevenement "
 					 + "WHERE eve_id = " + id);
 			
 			PreparedStatement addIntervenantStatement = _bdd.getConnection().prepareStatement(
 					"INSERT INTO edt.intervenantevenement(utilisateur_id, eve_id) VALUES(?, " + id + ")");
 			
 			for(int idIntervenant : idIntervenants) {
 				
 				addIntervenantStatement.setInt(1, idIntervenant);
 				addIntervenantStatement.execute();
 			}
 			
 			// Modifier  les responsables de l'événement
 			_bdd.executeRequest(
 					"DELETE FROM edt.responsableevenement "
 					 + "WHERE eve_id = " + id);
 			
 			PreparedStatement addResponsableStatement = _bdd.getConnection().prepareStatement(
 					"INSERT INTO edt.responsableevenement(utilisateur_id, eve_id) VALUES(?," + id + ")");
 			
 			for (int idResponsable : idResponsables){
 				addResponsableStatement.setInt(1, idResponsable);
 				addResponsableStatement.execute();
 			}
 			
 			// Modifier les calendriers associés à l'événement
 			_bdd.executeRequest(
 					"DELETE FROM edt.evenementappartient "
 					+ "WHERE eve_id = " + id);
 			
 			PreparedStatement addCalendrierStatement = _bdd.getConnection().prepareStatement(
 					"INSERT INTO edt.evenementappartient(cal_id, eve_id) VALUES(?," + id + ")");
 			
 			for (int idCalendrier : idCalendriers){
 				addCalendrierStatement.setInt(1, idCalendrier);
 				addCalendrierStatement.execute();
 			}
 			
 			// Modifier les salles de l'évènement
 			_bdd.executeRequest(
 					"DELETE FROM edt.alieuensalle "
 					+ "WHERE eve_id = " + id);
 			
 			PreparedStatement addSalleStatement = _bdd.getConnection().prepareStatement(
 					"INSERT INTO edt.alieuensalle(salle_id, eve_id) VALUES(?," + id + ")");
 			
 			for (int idSalle : idSalles){
 				addSalleStatement.setInt(1, idSalle);
 				addSalleStatement.execute();
 			}
 			
 			// fin transaction si requis
 			if (createTransaction){
 				_bdd.commit();
 			}
 			
 		}  catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	/**
 	 * Suppression d'un évènement
 	 * 
 	 * Permet de supprimer un évènement dans la base de données,
 	 * l'évènement est identifié par son ID entier
 	 * 
 	 * @param idEvement idetifiant de l'événement
 	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @throws EdtempsException
 	 */
 	public void supprimerEvenement(int idEvenement, boolean createTransaction) throws EdtempsException {
 		try {
 			// Début transaction si nécessaire
 			if (createTransaction) {
 				_bdd.startTransaction();
 			}
 			
 			// Supprimer l'association aux intervenants de l'événement
 			_bdd.executeRequest(
 					"DELETE FROM edt.intervenantevenement "
 					 + "WHERE eve_id = " + idEvenement);
 			
 			// Supprimer l'association au matériel n�cessaire pour l'événement
 			_bdd.executeRequest(
 					"DELETE FROM edt.necessitemateriel "
 					 + "WHERE eve_id = " + idEvenement);
 			
 			// Supprimer l'association aux intervenants de l'évenement
 			_bdd.executeRequest(
 					"DELETE FROM edt.responsabletevenement "
 					 + "WHERE eve_id = " + idEvenement);
 			
 			// Supprimer l'asosciation aux salles de l'événement
 			_bdd.executeRequest(
 					"DELETE FROM edt.alieuensalle "
 					 + "WHERE eve_id = " + idEvenement);
 			
 			// Supprimer l'association aux calendriers
 			_bdd.executeRequest(
 					"DELETE FROM edt.evenementappartient "
 					 + "WHERE eve_id = " + idEvenement);
 			
 			// Supprimer l'évenement
 			_bdd.executeRequest(
 					"DELETE FROM edt.evenement "
 					 + "WHERE eve_id = " + idEvenement);
 
 			
 			// fin transaction si nécessaire
 			if (createTransaction){
 				_bdd.commit();
 			}
 			
 		} catch (DatabaseException e){
 			throw new EdtempsException(ResultCode.DATABASE_ERROR, e);
 		}
 	}
 	
 	/**
 	 * Récupération d'un évènement en base
 	 * @param idEvenement ID de l'évènement à récupérer
 	 * @return Evènement récupéré
 	 * @throws DatabaseException 
 	 */
 	public EvenementIdentifie getEvenement(int idEvenement) throws DatabaseException {
 		ResultSet reponse = _bdd.executeRequest(
 				"SELECT eve_id, eve_nom, eve_datedebut, eve_datefin "
 				+ "FROM edt.evenement "
 				+ "WHERE eve_id=" + idEvenement);
 		
 		EvenementIdentifie res = null;
 		try {
 			if(reponse.next()) {
 				res = new EvenementIdentifieInflater().inflateEvenement(reponse, _bdd);
 			}
 			reponse.close();
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 			
 		return res;
 	}
 	
 	
 	/**
 	 * Liste les évènements auxquels un utilisateur est abonné par l'intermédiaire de ses abonnements aux groupes, et donc aux calendriers
 	 * @param idUtilisateur Utilisateur dont les évènements sont à récupérer
 	 * @param createTransaction Indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param reuseTempTableAbonnements makeTempTableListeGroupesAbonnement() a déjà été appelé dans la transaction en cours
 	 * @param dateDebut
 	 * @param dateFin
 	 * 
 	 * @see GroupeGestion#makeTempTableListeGroupesAbonnement(BddGestion, int)
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementIdentifie> listerEvenementsUtilisateur(int idUtilisateur, Date dateDebut, Date dateFin, 
 			boolean createTransaction, boolean reuseTempTableAbonnements) throws DatabaseException {
 		
 		ArrayList<EvenementIdentifie> res = null;
 	
 		try {
 			if(createTransaction)
 				_bdd.startTransaction();
 			
 			if(!reuseTempTableAbonnements)
 				GroupeGestion.makeTempTableListeGroupesAbonnement(_bdd, idUtilisateur);
 			
 			PreparedStatement req = _bdd.getConnection().prepareStatement("SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 					"FROM edt.evenement " +
 					"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
 					"INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
 					"INNER JOIN " + GroupeGestion.NOM_TEMPTABLE_ABONNEMENTS + " abonnements ON abonnements.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
 					"WHERE evenement.eve_datefin >= ? AND evenement.eve_datedebut <= ?");
 			
 			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
 			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
 			
 			ResultSet reponse = req.executeQuery();
 			
 			res = new ArrayList<EvenementIdentifie>();
 			EvenementIdentifieInflater inflater = new EvenementIdentifieInflater();
 			while(reponse.next()) {
 				res.add(inflater.inflateEvenement(reponse, _bdd));
 			}
 			
 			reponse.close();
 			
 			if(createTransaction)
 				_bdd.commit();
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Liste les évènements liés à un groupe d'utilisateurs
 	 * @param idGroupe groupe dont les évènements sont à récupérer
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementComplet> listerEvenementsGroupe(int idGroupe, Date dateDebut, Date dateFin, boolean createTransaction) throws DatabaseException {
 		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"FROM edt.evenement " +
 				"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
 				"INNER JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
 				"WHERE calendrierappartientgroupe.cal_id = " + idGroupe + " "
 				+ "AND evenement.eve_datefin >= ? "
 				+ "AND evenement.eve_datedebut <= ?";
 		ArrayList<EvenementComplet> res = listerEvenements(request, dateDebut, dateFin, new EvenementCompletInflater(), createTransaction);
 		return res;
 	}
 	
 	/**
 	 * Méthode de listing des évènements de cours ou pas de cours (exclusivement l'un ou l'autre) d'une salle
 	 * 
 	 * @param idSalle ID de la salle en question
 	 * @param dateDebut Date de début de la fenêtre de recherche d'évènements
 	 * @param dateFin Date de fin de la fenêtre de recherche d'évènements
 	 * @param estCours Lister uniquement les évènements qui sont des cours (true) ou uniquement ceux qui n'en sont pas (false)
 	 * @param createTransaction Indique si il faut créer une transaction dans cette méthode, ou si elle sera déjà appelée dans une transaction
 	 * @return Liste des évènements trouvés
 	 * @throws DatabaseException  Erreur de communication avec la base de données
 	 */
 	public ArrayList<EvenementIdentifie> listerEvenementsSalleCoursOuPas(int idSalle, Date dateDebut, Date dateFin, boolean estCours, boolean createTransaction) throws DatabaseException {
 		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"FROM edt.evenement " +
 				"INNER JOIN edt.alieuensalle ON evenement.eve_id = alieuensalle.eve_id " +
 				"LEFT JOIN edt.evenementappartient ON evenementappartient.eve_id = evenement.eve_id " +
 				"LEFT JOIN edt.calendrierappartientgroupe ON calendrierappartientgroupe.cal_id = evenementappartient.cal_id " +
 				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id = calendrierappartientgroupe.groupeparticipant_id " +
 					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours)" +
 				"WHERE alieuensalle.salle_id = " + idSalle +" "
 				+ "AND evenement.eve_datefin >= ? "
 				+ "AND evenement.eve_datedebut <= ? " +
 				"GROUP BY evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"HAVING COUNT(groupecours.groupeparticipant_id)" + (estCours ? " > 0" : " = 0");
 		ArrayList<EvenementIdentifie> res = listerEvenements(request, dateDebut, dateFin, new EvenementIdentifieInflater(), createTransaction);
 		return res;
 	}
 	
 	/**
 	 * Méthode générique de listing d'évènements complets ou incomplets d'une salle
 	 * 
 	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
 	 * @param dateDebut
 	 * @param dateFin
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param inflater
 	 * @return
 	 * @throws DatabaseException
 	 */
 	protected <T extends EvenementIdentifie> ArrayList<T> listerEvenementsSalle(int idSalle, Date dateDebut, Date dateFin, 
 			boolean createTransaction, AbsEvenementInflater<T> inflater) throws DatabaseException {
 		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"FROM edt.evenement " +
 				"INNER JOIN edt.alieuensalle ON evenement.eve_id = alieuensalle.eve_id " +
 				"WHERE alieuensalle.salle_id = " + idSalle +" "
 				+ "AND evenement.eve_datefin >= ? "
 				+ "AND evenement.eve_datedebut <= ?";
 		ArrayList<T> res = listerEvenements(request, dateDebut, dateFin, inflater, createTransaction);
 		return res;
 	}
 	
 	/**
 	 * Liste les évènements liés à une salle, sous forme d'évènement complet
 	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param dateDebut
 	 * @param dateFin
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementComplet> listerEvenementCompletsSalle(int idSalle, Date dateDebut, Date dateFin, 
 			boolean createTransaction) throws DatabaseException {
 		return listerEvenementsSalle(idSalle, dateDebut, dateFin, createTransaction, new EvenementCompletInflater());
 	}
 	
 	/**
 	 * Liste les évènements liés à une salle, sous forme d'évènement identifié
 	 * @param idSalle identifiant de la salle dont les évènements sont à récupérer
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param dateDebut
 	 * @param dateFin
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementIdentifie> listerEvenementIdentifiesSalle(int idSalle, Date dateDebut, Date dateFin, 
 			boolean createTransaction) throws DatabaseException {
 		return listerEvenementsSalle(idSalle, dateDebut, dateFin, createTransaction, new EvenementIdentifieInflater());
 	}
 	
 	
 	/**
 	 * Liste les évènements liés à un responsable
 	 * @param idResponsable identifiant de l'utilisateur responsable des évènements à récupérer
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param dateDebut
 	 * @param dateFin
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementComplet> listerEvenementsResponsable(int idResponsable, Date dateDebut, Date dateFin, 
 			boolean createTransaction) throws DatabaseException {
 		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"FROM edt.evenement " +
 				"INNER JOIN edt.responsableevenement ON evenement.eve_id = responsableevenement.eve_id " +
 				"WHERE responsableevenement.utilisateur_id = " + idResponsable + " "
 				+ "AND evenement.eve_datefin >= ? "
 				+ "AND evenement.eve_datedebut <= ?";
 		ArrayList<EvenementComplet> res = listerEvenements(request, dateDebut, dateFin, new EvenementCompletInflater(), createTransaction);
 		return res;
 	}
 	
 	/**
 	 * Liste les évènements liés à un calendrier
 	 * @param idCalendrier identifiant du calendrier dont les évènements sont à récupérer
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * @param dateDebut
 	 * @param dateFin
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	public ArrayList<EvenementIdentifie> listerEvenementsCalendrier(int idCalendrier, Date dateDebut, Date dateFin, 
 			boolean createTransaction) throws DatabaseException {
 		String request = "SELECT DISTINCT evenement.eve_id, evenement.eve_nom, evenement.eve_datedebut, evenement.eve_datefin " +
 				"FROM edt.evenement " +
 				"INNER JOIN edt.evenementappartient ON evenement.eve_id = evenementappartient.eve_id " +
 				"WHERE evenementappartient.cal_id = " + idCalendrier + " "
 				+ "AND evenement.eve_datefin >= ? "
 				+ "AND evenement.eve_datedebut <= ?";
 		ArrayList<EvenementIdentifie> res = listerEvenements(request, dateDebut, dateFin, new EvenementIdentifieInflater(), createTransaction);
 		return res;
 	}
 	
 	/**
 	 * Suppression de l'association d'un évènement "non cours" à une salle.
 	 * Utile pour ajouter des évènements de cours dans une salle déjà occupée par autre chose (les cours sont prioritaires)
 	 * 
 	 * @param idSalles ID des salles à libérer (peut contenir plus de salles que celles occupées)
 	 * @param idEvenements ID des évènements concernés
 	 * @throws DatabaseException Erreur de communication avec la base de données
 	 */
 	public void supprimerSallesEvenementsNonCours(List<Integer> idSalles, List<Integer> idEvenements) throws DatabaseException {
 		
 		if(idSalles.isEmpty() || idEvenements.isEmpty()) {
 			return;
 		}
 		
 		String strIdSalles = StringUtils.join(idSalles, ",");
 		String strIdEvenements = StringUtils.join(idEvenements, ",");
 		
		_bdd.executeRequest("DELETE FROM edt.alieuensalle tablesuppr WHERE EXISTS (" +
				"SELECT 1 FROM edt.alieuensalle " +
				"LEFT JOIN edt.evenementappartient ON evenementappartient.eve_id=alieuensalle.eve_id " +
				"LEFT JOIN edt.calendrierappartientgroupe ON evenementappartient.cal_id=calendrierappartientgroupe.cal_id " +
				"LEFT JOIN edt.groupeparticipant groupecours ON groupecours.groupeparticipant_id=calendrierappartientgroupe.groupeparticipant_id " +
					"AND (groupecours.groupeparticipant_estcours OR groupecours.groupeparticipant_aparentcours) " +
				"WHERE alieuensalle.eve_id IN (" + strIdEvenements + ") AND alieuensalle.salle_id IN (" + strIdSalles + ") " +
					"AND alieuensalle.eve_id = tablesuppr.eve_id AND alieuensalle.salle_id = tablesuppr.salle_id " +
				"GROUP BY alieuensalle.eve_id, alieuensalle.salle_id " +
				"HAVING COUNT(groupecours.groupeparticipant_id) = 0)");
 	}
 	
 	/**
 	 * Liste les évènements correspondant à une requête préparée (pour obtenir les événements liés à un groupe, à une salle, à un calendrier, à un responsable)
 	 * @param request requêre SQL pour obtenir les événements souhaités
 	 * @param dateDebut
 	 * @param dateFin
 	 * @parm inflater Inflater permettant de créer l'objet voulu à partir des lignes de base de donnée
 	 * @param createTransaction indique s'il faut créer une transaction dans cette méthode. Sinon, elle DOIT être appelée à l'intérieur d'une transaction.
 	 * 
 	 * @return Liste d'évènements récupérés
 	 * @throws DatabaseException
 	 */
 	private <T  extends EvenementIdentifie> ArrayList<T> listerEvenements(String request, Date dateDebut, Date dateFin, 
 			AbsEvenementInflater<T> inflater, boolean createTransaction) throws DatabaseException {
 		
 		ArrayList<T> res = null;
 		try {
 			if(createTransaction){
 				_bdd.startTransaction();
 			}
 						
 			PreparedStatement req = _bdd.getConnection().prepareStatement(request);
 					
 			req.setTimestamp(1, new java.sql.Timestamp(dateDebut.getTime()));
 			req.setTimestamp(2, new java.sql.Timestamp(dateFin.getTime()));
 			
 			ResultSet reponse = req.executeQuery();
 			
 			res = new ArrayList<T>();
 			while(reponse.next()) {
 				res.add(inflater.inflateEvenement(reponse, _bdd));
 			}
 			
 			reponse.close();
 			
 			if(createTransaction){
 				_bdd.commit();
 			}	
 			return res;
 			
 		} catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 }

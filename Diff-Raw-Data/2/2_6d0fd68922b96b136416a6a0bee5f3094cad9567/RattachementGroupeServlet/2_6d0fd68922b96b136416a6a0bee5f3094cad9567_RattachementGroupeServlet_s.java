 package org.ecn.edtemps.servlets.impl;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.json.Json;
 import javax.json.JsonException;
 import javax.json.JsonObject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.exceptions.EdtempsException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.json.ResponseManager;
 import org.ecn.edtemps.managers.BddGestion;
 import org.ecn.edtemps.managers.GroupeGestion;
 import org.ecn.edtemps.models.identifie.GroupeComplet;
 import org.ecn.edtemps.servlets.RequiresConnectionServlet;
 
 /**
  * Servlet pour la gestion rattachement aux groupes de participants
  * 
  * @author Joffrey Terrade
  */
 public class RattachementGroupeServlet extends RequiresConnectionServlet {
 
 	private static final long serialVersionUID = 6204340565926277288L;
 	private static Logger logger = LogManager.getLogger(RattachementGroupeServlet.class.getName());
 
 	/**
 	 * Méthode générale du servlet appelée par la requête POST
 	 * Elle redirige vers les différentes méthodes possibles
 	 * 
 	 * @param userId
 	 * 			identifiant de l'utilisateur qui a fait la requête
 	 * @param bdd
 	 * 			gestionnaire de la base de données
 	 * @param req
 	 * 			requête
 	 * @param resp
 	 * 			réponse pour le client
 	 */
 	@Override
 	protected void doPostAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		// Vérification des valeurs possibles dans le path de la requête
 		String pathInfo = req.getPathInfo();
 		if (!pathInfo.equals("/listermesdemandes") && !pathInfo.equals("/accepter") && !pathInfo.equals("/refuser") ) {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
 			bdd.close();
 			return;
 		}
 
 		try {
 
 			// Renvoies vers les différentes fonctionnalités
 			switch (pathInfo) {
 				case "/listermesdemandes":
 					doListerMesDemandesDeRattachement(userId, bdd, req, resp);
 					break;
 				case "/accepter":
 					doAccepterDemandeDeRattachement(userId, bdd, req, resp);
 					break;
 				case "/refuser":
 					doRefuserDemandeDeRattachement(userId, bdd, req, resp);
 					break;
 			}
 
 			// Ferme l'accès à la base de données
 			bdd.close();
 		
 		} catch(JsonException | ClassCastException e) {
 			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON incorrect", null));
 			bdd.close();
 		} catch(EdtempsException e) {
 			logger.error("Erreur avec le servlet de rattachement à un groupe de participants (listerDemandes ou accepter ou refuser)", e);
 			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
 			bdd.close();
 		} catch(SQLException e) {
 			logger.error("Erreur avec le servlet de rattachement à un groupe de participants (listerDemandes ou accepter ou refuser)", e);
 			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.DATABASE_ERROR, e.getMessage(), null));
 			bdd.close();
 		}
 
 	}
 	
 	/**
 	 * Lister les demandes de rattachements d'un utilisateur
 	 * @param userId Identifiant de l'utilisateur qui fait la requête
 	 * @param bdd Gestionnaire de la base de données
 	 * @param resp Réponse à compléter
 	 * @param requete Requête
 	 * @throws SQLException 
 	 * @throws DatabaseException 
 	 * @throws IOException 
 	 */
 	protected void doListerMesDemandesDeRattachement(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws SQLException, DatabaseException, IOException {
 		GroupeGestion gestionnaireGroupes = new GroupeGestion(bdd);
 		List<GroupeComplet> listeGroupes = gestionnaireGroupes.listerDemandesDeRattachement(userId);
		JsonObject data = Json.createObjectBuilder().add("listeMateriels", JSONUtils.getJsonArray(listeGroupes)).build();
 		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Liste des groupes en attente de rattachement récupérée", data));
 	}
 	
 	/**
 	 * Accepter une demande de rattachement
 	 * @param userId Identifiant de l'utilisateur qui fait la requête
 	 * @param bdd Gestionnaire de la base de données
 	 * @param resp Réponse à compléter
 	 * @param requete Requête
 	 * @throws EdtempsException
 	 * @throws IOException
 	 */
 	protected void doAccepterDemandeDeRattachement(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
 		
 	}
 
 	/**
 	 * Refuser une demande de rattachement
 	 * @param userId Identifiant de l'utilisateur qui fait la requête
 	 * @param bdd Gestionnaire de la base de données
 	 * @param resp Réponse à compléter
 	 * @param requete Requête
 	 * @throws EdtempsException
 	 * @throws IOException
 	 */
 	protected void doRefuserDemandeDeRattachement(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws EdtempsException, IOException {
 		
 	}
 
 }

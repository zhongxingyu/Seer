 package org.ecn.edtemps.servlets.impl;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import javax.json.Json;
 import javax.json.JsonArray;
 import javax.json.JsonException;
 import javax.json.JsonObject;
 import javax.json.JsonReader;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.ecn.edtemps.exceptions.EdtempsException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.json.ResponseManager;
 import org.ecn.edtemps.managers.BddGestion;
 import org.ecn.edtemps.managers.GroupeGestion;
 import org.ecn.edtemps.servlets.RequiresConnectionServlet;
 
 /**
  * Servlet pour la gestion des groupes de participants
  * 
  * @author Joffrey Terrade
  */
 public class GroupeParticipantsServlet extends RequiresConnectionServlet {
 
 	private static final long serialVersionUID = -61618228879909663L;
 	private static Logger logger = LogManager.getLogger(GroupeParticipantsServlet.class.getName());
 
 	/**
 	 * Méthode générale du servlet appelée par la requête POST
 	 * Elle redirige vers les trois méthodes possibles : ajout, modification et suppression
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
 		if (!pathInfo.equals("/ajouter") && !pathInfo.equals("/modifier") && !pathInfo.equals("/supprimer")) {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
 			bdd.close();
 			return;
 		}
 
		// Récupération des infos de l'objet groupe
		String strGroupe = req.getParameter("groupe");
		if (strGroupe == null) {
			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe manquant", null));
			bdd.close();
			return;
		}

 		try {
			JsonReader reader = Json.createReader(new StringReader(strGroupe));
			JsonObject jsonGroupe = reader.readObject();
 
 			// Renvoies vers les trois fonctionnalités possibles : ajout, modification et suppression 
 			switch (pathInfo) {
 				case "/ajouter":
 					
 					// Récupération des informations sur le groupe
 					String nom = jsonGroupe.getString("nom");
 					Integer idGroupeParent = Integer.valueOf(jsonGroupe.getString("idGroupeParent"));
 					Boolean rattachementAutorise = jsonGroupe.getBoolean("rattachementAutorise");
 					Boolean estCours = jsonGroupe.getBoolean("estCours");
 					JsonArray jsonIdProprietaires = jsonGroupe.getJsonArray("proprietaires");
 					List<Integer> listeIdProprietaires = (jsonIdProprietaires == null) ? null : JSONUtils.getIntegerArrayList(jsonIdProprietaires);
 					
 					// Vérification que l'objet est bien complet
 					if (StringUtils.isBlank(nom) || rattachementAutorise == null || estCours == null || CollectionUtils.isEmpty(listeIdProprietaires)) {
 						resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Objet groupe incomplet : paramètres manquants", null));
 						bdd.close();
 						return;
 					}
 
 					// Lance la méthode d'ajout
 					doAjouterGroupeParticipants(userId, bdd, resp, nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires);
					
 					break;
 				case "/modifier":
 					
 					doModifierGroupeParticipants();
					
 					break;
					
 				case "/supprimer":
 					// Lance la méthode de suppression
					Integer a = jsonGroupe.getInt("groupeId");
					doSupprimerGroupeParticipants(bdd, a, resp);
 					break;
 			}
 
 			// Ferme l'accès à la base de données
 			bdd.close();
 		
 		} catch(JsonException | ClassCastException e) {
 			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.WRONG_PARAMETERS_FOR_REQUEST, "Format de l'objet JSON 'groupe de participants' incorrect", null));
 			bdd.close();
 		} catch(EdtempsException e) {
 			logger.error("Erreur lors de l'ajout/modification/suppression d'un groupe de participants", e);
 			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
 			bdd.close();
 		}
 
 	}
 
 	
 	/**
 	 * Ajoutter un groupe de participants
 	 * 
 	 * @param userId
 	 * 			identifiant de l'utilisateur qui fait la demande d'ajout
 	 * @param bdd
 	 * 			gestionnaire de la base de données
 	 * @param resp
 	 * 			réponse à compléter
 	 * @param nom
 	 * 			nom du groupe à créer
 	 * @param idGroupeParent
 	 * 			identifiant du groupe parent du groupe à créer
 	 * @param rattachementAutorise
 	 * 			VRAI si le rattachement au groupe est autorisé
 	 * @param estCours
 	 * 			VRAI si le groupe est un cours
 	 * @param listeIdProprietaires
 	 * 			liste des identifiants des propriétaires
 	 * 
 	 * @throws EdtempsException
 	 * @throws IOException
 	 */
 	protected void doAjouterGroupeParticipants(int userId, BddGestion bdd, HttpServletResponse resp, String nom, Integer idGroupeParent, Boolean rattachementAutorise, Boolean estCours, List<Integer> listeIdProprietaires) throws EdtempsException, IOException {
 		GroupeGestion groupeGestion = new GroupeGestion(bdd);
 		groupeGestion.sauverGroupe(nom, idGroupeParent, rattachementAutorise, estCours, listeIdProprietaires);
 		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe ajouté", null));
 	}
 
 	
 	protected void doModifierGroupeParticipants() throws EdtempsException, IOException {
 	}
 
 	
 	/**
 	 * Supprimer un groupe de participants
 	 * 
 	 * @param bdd
 	 * 		gestionnaire de la base de données
 	 * @param idGroupe
 	 * 		identifiant du groupe à supprimer
 	 * @param resp
 	 * 		réponse à compléter
 	 * @throws EdtempsException
 	 * @throws IOException
 	 */
 	protected void doSupprimerGroupeParticipants(BddGestion bdd, Integer idGroupe, HttpServletResponse resp) throws EdtempsException, IOException {
 		GroupeGestion groupeGestion = new GroupeGestion(bdd);
 		groupeGestion.supprimerGroupe(idGroupe);
 		resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Groupe supprimé", null));
 	}
 
 }

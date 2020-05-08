 package org.ecn.edtemps.servlets.impl;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.managers.BddGestion;
 import org.ecn.edtemps.managers.UtilisateurGestion;
 
 /**
  * Servlet pour la connexion en grand administrateur
  * 
  * @author Joffrey Terrade
  */
 public class AdministrateurServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 5303879641965806292L;
 	private static Logger logger = LogManager.getLogger(AdministrateurServlet.class.getName());
 
 	/**
 	 * Servlet pour la connexion et la déconnexion en mode grand administrateur
 	 * @param req Requête
 	 * @param resp Réponse pour le client
 	 */
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		
 		// Vérification des valeurs possibles dans le path de la requête
 		String pathInfo = req.getPathInfo();
 		if (!pathInfo.equals("/connexion")) {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 
 		HttpSession session = req.getSession(true);
 
 		try {
 			switch (pathInfo) {
 				case "/connexion":
 					doConnexionAdministrateur(req, resp, session);
 					break;
 			}
 		} catch (IOException | ServletException e) {
 			logger.error("Erreur lors de l'écriture de la réponse");
 			session.setAttribute("connect", "KO");
 		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
 			logger.error("Erreur lors du cryptage du mot de passe");
 			session.setAttribute("connect", "KO");
 		} catch (SQLException | DatabaseException e) {
 			logger.error("Erreur liée à la base de données");
 			session.setAttribute("connect", "KO");
 		}
 		
 	}
 	
 	
 	/**
 	 * Connecter mode Administrateur
 	 * @param resp Réponse à compléter
 	 * @param requete Requête
 	 * @param session Session HTTP
 	 * @throws IOException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws InvalidKeyException 
 	 * @throws DatabaseException 
 	 * @throws SQLException 
 	 * @throws ServletException 
 	 */
 	protected void doConnexionAdministrateur(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws IOException, InvalidKeyException, NoSuchAlgorithmException, DatabaseException, SQLException, ServletException {
 
 		BddGestion bdd = new BddGestion();
 		
 		// Récupération des identifiants passés en paramètre
 		String login = req.getParameter("login");
 		String password = req.getParameter("password");
 		String cryptedPassword = UtilisateurGestion.hmac_sha256("Chaine de cryptage", password);
 		logger.debug("Tentative de connexion à l'espace d'administration avec le login : " + login);
 		
 		// Prépare la requête
 		PreparedStatement reqPreparee = bdd.getConnection().prepareStatement(
 				"SELECT COUNT(*) FROM edt.administrateurs WHERE admin_login=? AND admin_password=?");
 		reqPreparee.setString(1, login);
 		reqPreparee.setString(2, cryptedPassword);
 
 		// Exécute la requête
 		ResultSet reqResultat = reqPreparee.executeQuery();
 		reqResultat.next();
 		
 		// Alimente les attributs de la session http
 		session.setAttribute("login", login);
 
 		if (reqResultat.getInt(1)>0) {
 			logger.error("Connexion réussie");
 			session.setAttribute("connect", "OK");
 			resp.sendRedirect("../admin/general.jsp");
 		} else {
 			logger.error("Echec de connexion : identifiant ou mot de passe erroné.");
 			session.setAttribute("connect", "KO");
 			resp.sendRedirect("../admin/login.jsp");
 		}
 
 	}
 
 }

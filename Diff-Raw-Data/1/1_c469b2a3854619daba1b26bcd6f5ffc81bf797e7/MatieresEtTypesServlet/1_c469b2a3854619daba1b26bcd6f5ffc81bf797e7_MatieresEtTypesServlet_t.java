 package org.ecn.edtemps.servlets;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.json.Json;
 import javax.json.JsonArrayBuilder;
 import javax.json.JsonObjectBuilder;
 import javax.json.JsonValue;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.ecn.edtemps.exceptions.DatabaseException;
 import org.ecn.edtemps.exceptions.ResultCode;
 import org.ecn.edtemps.json.ResponseManager;
 import org.ecn.edtemps.managers.BddGestion;
 
 
 /**
  * Servlet permettant la récupération des matieres et types (qui qualifient un calendrier)
  * @author Maxime Terrade
  *
  */
 public class MatieresEtTypesServlet extends RequiresConnectionServlet {
 	
 	private static Logger logger = LogManager.getLogger(MatieresEtTypesServlet.class.getName());
 	
 	@Override
 	protected void doGetAfterLogin(int userId, BddGestion bdd, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		JsonValue data;
 		
 		try {
 			BddGestion bddGestion = new BddGestion();
 			
 			// Récupération des matieres
 			ResultSet resMatieres = bddGestion.executeRequest("SELECT matiere_nom FROM edt.matiere");
 			JsonArrayBuilder jsonMatieres = Json.createArrayBuilder();	
 			while(resMatieres.next()) {
 				jsonMatieres.add(resMatieres.getString("matiere_nom"));
 			}
 			
 			// Récupération des types
 			ResultSet resTypes = bddGestion.executeRequest("SELECT typecal_libelle FROM edt.typecalendrier");
 			JsonArrayBuilder jsonTypes = Json.createArrayBuilder();
 			while(resTypes.next()) {
 				jsonTypes.add(resTypes.getString("typecal_libelle"));
 			}
 			
 			// Création de la réponse
 			data = Json.createObjectBuilder()
 					.add("matieres", jsonMatieres)
 					.add("types", jsonTypes)
 					.build();
 			
 			// Génération réponse
 			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.SUCCESS, "Récupération des matières et types de calendriers réussie", data));
 			
 		} catch (DatabaseException e) {
 			resp.getWriter().write(ResponseManager.generateResponse(e.getResultCode(), e.getMessage(), null));
 			logger.error("Erreur d'accès à la base de données lors de l'accès aux matieres/types", e);
 		} catch (SQLException e) {
 			resp.getWriter().write(ResponseManager.generateResponse(ResultCode.DATABASE_ERROR, e.getMessage(), null));
 			logger.error("Erreur dans le traitement des données SQL lors de l'accès aux matieres/types", e);
 		}
 
 		bdd.close();
 	}
 
 }
 

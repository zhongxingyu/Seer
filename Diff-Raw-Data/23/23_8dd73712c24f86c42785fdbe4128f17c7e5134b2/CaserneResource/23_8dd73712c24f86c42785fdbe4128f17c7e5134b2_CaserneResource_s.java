 package org.agetac.server.resources.impl;
 
 import java.util.List;
 
 import org.agetac.model.impl.Caserne;
 import org.agetac.server.db.Casernes;
 import org.agetac.server.resources.sign.IServerResource;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ServerResource;
 
 public class CaserneResource extends ServerResource implements IServerResource {
 
 	public Representation getResource() throws Exception {
 		// Cre une representation JSON vide
 		JsonRepresentation result = null;
 		// Rcupre l'identifiant unique de la ressource demande.
 		String casId = (String) this.getRequestAttributes().get("caserneId");
 		System.out.println(casId);
 		// Rcupration des casernes de l'intervention
 		List<Caserne> casernes = Casernes.getInstance().getCasernes();
 
 		Caserne caserne = null;
 
 		// Si on demande une caserne prcise
 		if (casId != null) {
 			// Recherche de la caserne demande
 			for (int i = 0; i < casernes.size(); i++) {
 				if (casernes.get(i).getUniqueID().equals(casId)) {
 					caserne = casernes.get(i);
 				}
 			}
 			// Si la caserne n'est pas trouv
 			if (caserne == null) {
 				result = null;
 				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
 			} else {
 				result = new JsonRepresentation(caserne.toJSON());
 			}
 			// Si on veut toutes les casernes
 		} else if (casId == null) {
 
 			JSONArray jsonAr = new JSONArray(); // Cration d'une liste Json
 			for (int i = 0; i < casernes.size(); i++) {
 				// On ajoute un jsonObject contenant la caserne dans le jsonArray
				jsonAr.put(new JSONObject(casernes.get(i).toJSON()));
 			}
 			// On cre la reprsentation de la liste
 			result = new JsonRepresentation(jsonAr);
 		}
 
 		// Retourne la reprsentation, le code status indique au client si elle
 		// est valide
 		return result;
 	}
 
 	@Override
 	public Representation putResource(Representation representation)
 			throws Exception {
 		
 		// Rcupre la reprsentation JSON du caserne
 		JsonRepresentation jsonRepr = new JsonRepresentation(representation);
 
 		// Transforme la representation en objet java
 		JSONObject jsObj = jsonRepr.getJsonObject();
 		Caserne caserne = new Caserne(jsObj);
 
 		// Ajoute l'caserne a la base de donne
 		Casernes.getInstance().addCaserne(caserne);
 		// Pas besoin de retourner de reprsentation au client
 		return null;
 	}
 
 	@Override
 	public Representation deleteResource() {
 		// Rcupre l'id dans l'url
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String casId = (String) this.getRequestAttributes().get("caserneId");
 		
 		
 		// On s'assure qu'il n'est plus prsent en base de donnes
 		Casernes.getInstance().deleteCaserne(casId);
 		
 		//for (int i = 0; i < casernes.size(); i++) {
 		//	if (casernes.get(i).getUniqueID().equals(casId)) {
 		//		casernes.remove(casernes.get(i));
 		//	}
 		//}
 		
 		return null;
 	}
 
 	@Override
 	public Representation postResource(Representation representation)
 			throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }

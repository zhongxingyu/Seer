 package org.agetac.server.resources.impl;
 
 import java.util.List;
 
 import org.agetac.model.impl.Implique;
 import org.agetac.model.impl.Intervention;
 import org.agetac.server.db.Interventions;
 import org.agetac.server.resources.sign.IServerResource;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ServerResource;
 
 public class ImpliqueResource extends ServerResource implements IServerResource {
 
 	@Override
 	public Representation getResource() throws Exception {
 		// Cre une representation JSON vide
 		JsonRepresentation result = null;
 		// Rcupre l'identifiant unique de la resource demande.
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String impId = (String) this.getRequestAttributes().get("impliqueId");
 	
 		// Rcupration des impliques de l'intervention
 		List<Implique> impliques = Interventions.getInstance().getIntervention(interId).getImpliques();
 
 		Implique implique = null;
 		
 		// Si on demande un implique prcis
 		if (impId != null) {
 			// Recherche du implique demand
 			for (int i = 0; i < impliques.size(); i++) {
 				if (impliques.get(i).getUniqueID().equals(impId)) {
 					implique = impliques.get(i);
 				}
 			}
 			// Si le implique n'est pas trouv
 			if (implique == null) {
 				result = null;
 				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
 			} else {
 				result = new JsonRepresentation(implique.toJSON());
 			}
 		// Si on veut tous les impliques
 		} else if (impId == null) {
 			
 			JSONArray jsonAr = new JSONArray(); //Cration d'une liste Json
 			for(int i=0; i< impliques.size();i++){
				jsonAr.put(impliques.get(i).toJSON()); // On ajoute un jsonObject contenant le implique dans le jsonArray
 			}
 			
 			result = new JsonRepresentation(jsonAr); // On cre la reprsentation de la liste
 		}
 
 		// Retourne la reprsentation, le code status indique au client si elle est valide
 		return result;
 	}
 
 	@Override
 	public Representation putResource(Representation representation)
 			throws Exception {
 		// Rcupre l'identifiant unique de la resimplique demande.
 		String interId = (String) this.getRequestAttributes().get("interId");
 
 		// Rcupre la reprsentation JSON du implique
 		JsonRepresentation jsonRepr = new JsonRepresentation(representation);
 		// System.out.println("JsonRepresentation : " + jsonRepr.getText());
 
 		// Transforme la representation en objet java
 		JSONObject jsObj = jsonRepr.getJsonObject();
 		Implique implique = new Implique(jsObj);
 		// System.out.println("Implique : " + implique.toJSON());
 
 		// Ajoute l'implique a la base de donne
 		Intervention i = Interventions.getInstance().getIntervention(interId);
 		List<Implique> lm = i.getImpliques();
 		lm.add(implique);
 		// Impliques.getInstance().addImplique(implique);
 		// Pas besoin de retourner de reprsentation au client
 		return null;
 	}
 
 	@Override
 	public Representation deleteResource() {
 		// Rcupre l'id dans l'url
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String impId = (String) this.getRequestAttributes().get("impliqueId");
 		
 		// On s'assure qu'il n'est plus prsent en base de donnes
 	
 		Intervention inter = Interventions.getInstance().getIntervention(interId);
 		List<Implique> impliques = inter.getImpliques();
 		for (int i = 0; i < impliques.size(); i++) {
 			if (impliques.get(i).getUniqueID().equals(impId)) {
 				impliques.remove(impliques.get(i));
 			}
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Representation postResource(Representation representation)
 			throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 }

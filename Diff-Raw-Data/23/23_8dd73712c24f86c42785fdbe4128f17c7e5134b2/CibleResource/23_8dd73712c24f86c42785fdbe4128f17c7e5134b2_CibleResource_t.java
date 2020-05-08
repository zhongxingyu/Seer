 package org.agetac.server.resources.impl;
 
 import java.util.List;
 
 import org.agetac.model.impl.Cible;
 import org.agetac.model.impl.Intervention;
 import org.agetac.server.db.Interventions;
 import org.agetac.server.resources.sign.IServerResource;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ServerResource;
 
 public class CibleResource extends ServerResource implements IServerResource {
 
 	@Override
 	public Representation getResource() throws Exception {
 		// Cre une representation JSON vide
 		JsonRepresentation result = null;
 		// Rcupre l'identifiant unique de la ressource demande.
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String cibId = (String) this.getRequestAttributes().get("cibleId");
 		System.out.println(cibId);
 		// Rcupration des cibles de l'intervention
 		List<Cible> cibles = Interventions.getInstance().getIntervention(interId).getCibles();
 
 		Cible cible = null;
 		
 		// Si on demande un cible prcis
 		if (cibId != null) {
 			// Recherche du cible demand
 			for (int i = 0; i < cibles.size(); i++) {
 				if (cibles.get(i).getUniqueID().equals(cibId)) {
 					cible = cibles.get(i);
 				}
 			}
 			// Si le cible n'est pas trouv
 			if (cible == null) {
 				result = null;
 				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
 			} else {
 				result = new JsonRepresentation(cible.toJSON());
 			}
 		// Si on veut tous les cibles
 		} else if (cibId == null) {
 			
 			JSONArray jsonAr = new JSONArray(); //Cration d'une liste Json
 			for(int i=0; i< cibles.size();i++){
				jsonAr.put(cibles.get(i).toJSON()); // On ajoute un jsonObject contenant le cible dans le jsonArray
 			}
 			
 			result = new JsonRepresentation(jsonAr); // On cre la reprsentation de la liste
 		}
 
 		// Retourne la reprsentation, le code status indique au client si elle est valide
 		return result;
 	}
 
 	@Override
 	public Representation putResource(Representation representation)
 			throws Exception {
 		// Rcupre l'identifiant unique de la ressource demande.
 		String interId = (String) this.getRequestAttributes().get("interId");
 
 		// Rcupre la reprsentation JSON du cible
 		JsonRepresentation jsonRepr = new JsonRepresentation(representation);
 		// System.out.println("JsonRepresentation : " + jsonRepr.getText());
 
 		// Transforme la representation en objet java
 		JSONObject jsObj = jsonRepr.getJsonObject();
 		Cible cible = new Cible(jsObj);
 		// System.out.println("Cible : " + cible.toJSON());
 
 		// Ajoute l'cible a la base de donne
 		Intervention i = Interventions.getInstance().getIntervention(interId);
 		List<Cible> lm = i.getCibles();
 		lm.add(cible);
 		// Cibles.getInstance().addCible(cible);
 		// Pas besoin de retourner de reprsentation au client
 		return null;
 	}
 
 	@Override
 	public Representation deleteResource() {
 		// Rcupre l'id dans l'url
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String cibId = (String) this.getRequestAttributes().get("cibleId");
 		
 		// On s'assure qu'il n'est plus prsent en base de donnes
 	
 		Intervention inter = Interventions.getInstance().getIntervention(interId);
 		List<Cible> cibles = inter.getCibles();
 		for (int i = 0; i < cibles.size(); i++) {
 			if (cibles.get(i).getUniqueID().equals(cibId)) {
 				cibles.remove(cibles.get(i));
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

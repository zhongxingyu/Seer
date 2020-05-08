 package org.agetac.server.resources.impl;
 
 import java.util.List;
 
 import org.agetac.model.impl.Intervention;
 import org.agetac.model.impl.Source;
 import org.agetac.server.db.Interventions;
 import org.agetac.server.resources.sign.IServerResource;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ServerResource;
 
 public class SourceResource extends ServerResource implements IServerResource {
 
 	@Override
 	public Representation getResource() throws Exception {
 		// Cre une representation JSON vide
 		JsonRepresentation result = null;
 		// Rcupre l'identifiant unique de la ressource demande.
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String srcId = (String) this.getRequestAttributes().get("sourceId");
 		System.out.println(srcId);
 		// Rcupration des sources de l'intervention
 		List<Source> sources = Interventions.getInstance().getIntervention(interId).getSources();
 
 		Source source = null;
 		
 		// Si on demande un source prcis
 		if (srcId != null) {
 			// Recherche du source demand
 			for (int i = 0; i < sources.size(); i++) {
 				if (sources.get(i).getUniqueID().equals(srcId)) {
 					source = sources.get(i);
 				}
 			}
 			// Si le source n'est pas trouv
 			if (source == null) {
 				result = null;
 				getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
 			} else {
 				result = new JsonRepresentation(source.toJSON());
 			}
 		// Si on veut tous les sources
 		} else if (srcId == null) {
 			
 			JSONArray jsonAr = new JSONArray(); //Cration d'une liste Json
 			for(int i=0; i< sources.size();i++){
				jsonAr.put(sources.get(i).toJSON()); // On ajoute un jsonObject contenant le source dans le jsonArray
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
 
 		// Rcupre la reprsentation JSON du source
 		JsonRepresentation jsonRepr = new JsonRepresentation(representation);
 		// System.out.println("JsonRepresentation : " + jsonRepr.getText());
 
 		// Transforme la representation en objet java
 		JSONObject jsObj = jsonRepr.getJsonObject();
 		Source source = new Source(jsObj);
 		// System.out.println("Source : " + source.toJSON());
 
 		// Ajoute l'source a la base de donne
 		Intervention i = Interventions.getInstance().getIntervention(interId);
 		List<Source> lm = i.getSources();
 		lm.add(source);
 		// Sources.getInstance().addSource(source);
 		// Pas besoin de retourner de reprsentation au client
 		return null;
 	}
 
 	@Override
 	public Representation deleteResource() {
 		// Rcupre l'id dans l'url
 		String interId = (String) this.getRequestAttributes().get("interId");
 		String srcId = (String) this.getRequestAttributes().get("sourceId");
 		
 		// On s'assure qu'il n'est plus prsent en base de donnes
 	
 		Intervention inter = Interventions.getInstance().getIntervention(interId);
 		List<Source> sources = inter.getSources();
 		for (int i = 0; i < sources.size(); i++) {
 			if (sources.get(i).getUniqueID().equals(srcId)) {
 				sources.remove(sources.get(i));
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

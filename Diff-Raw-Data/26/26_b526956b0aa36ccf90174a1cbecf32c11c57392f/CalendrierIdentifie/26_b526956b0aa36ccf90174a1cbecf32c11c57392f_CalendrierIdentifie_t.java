 package org.ecn.edtemps.models.identifie;
 
 import java.util.List;
 
 import javax.json.Json;
import javax.json.JsonObjectBuilder;
 import javax.json.JsonValue;
 
 import org.ecn.edtemps.json.JSONAble;
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.models.Calendrier;
 
 /**
  * Classe modèle d'un calendrier identifié
  * 
  * @author Maxime TERRADE
  */
 public class CalendrierIdentifie extends Calendrier implements JSONAble {
 
 	/** Identifiant du calendrier dans la base de données */
 	protected int id;
 	
 	/** Constructeur avec tous les attributs */
 	public CalendrierIdentifie(String nom, String type, String matiere, List<Integer> idProprietaires, int id) {
 		super(nom, type, matiere, idProprietaires);
 		this.id = id;
 	}
 	
 	/**
 	 * Getter de l'ID
 	 * 
 	 * @return id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Setter de l'ID
 	 * 
 	 * @param id
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	@Override
 	public JsonValue toJson() {
		JsonObjectBuilder builder =  Json.createObjectBuilder()
 				.add("id", id)
 				.add("nom", nom)
				.add("proprietaires", JSONUtils.getJsonIntArray(idProprietaires));
		
		if(type != null) {
			builder.add("type", type);
		}
		else {
			builder.addNull("type");
		}
		
		if(matiere != null) {
			builder.add("matiere", matiere);
		}
		else {
			builder.addNull("type");
		}
		
		return builder.build();
 	}
 
 }

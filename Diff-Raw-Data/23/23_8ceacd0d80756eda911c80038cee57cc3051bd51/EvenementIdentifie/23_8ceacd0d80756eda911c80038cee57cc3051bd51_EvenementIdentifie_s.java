 package org.ecn.edtemps.models.identifie;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.json.Json;
 import javax.json.JsonObjectBuilder;
 import javax.json.JsonValue;
 
 import org.ecn.edtemps.json.JSONAble;
 import org.ecn.edtemps.json.JSONUtils;
 import org.ecn.edtemps.models.Evenement;
 
 /**
  * Classe modèle d'un événement identifié = défini dans la base de données
  * 
  * @author Maxime TERRADE
  */
 public class EvenementIdentifie extends Evenement implements JSONAble {
 
 	/** Identifiant de l'evenement dans la base de données */
 	protected int id;
 	
 	/** Constructeur avec tous les attributs */
 	public EvenementIdentifie(String nom, Date dateDebut, Date dateFin,
 			List<Integer> idCalendriers, Integer idCreateur, List<SalleIdentifie> salles,
 			List<UtilisateurIdentifie> intervenants, List<UtilisateurIdentifie> responsables, int id) {
 		super(nom, dateDebut, dateFin, idCalendriers, idCreateur, salles, intervenants, responsables);
 		this.id=id;
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
 
 	/**
 	 * Génération et remplissage du JsonObjectBuilder de cet objet pour la conversion en JsonValue.
 	 * Peut être surclassé pour ajouter des champs supplémentaires
 	 * 
 	 * @return Le builder créé et initialisé
 	 */
 	protected JsonObjectBuilder makeJsonObjectBuilder() {
		return Json.createObjectBuilder()
 			.add("id", id)
 			.add("nom", nom)
 			.add("dateDebut", dateDebut.getTime())
 			.add("dateFin", dateFin.getTime())
 			.add("calendriers", JSONUtils.getJsonIntArray(idCalendriers))
 			.add("salles", JSONUtils.getJsonArray(salles))
 			.add("intervenants", JSONUtils.getJsonArray(intervenants))
 			.add("responsables", JSONUtils.getJsonArray(responsables));
 	}
 
 	@Override
 	public final JsonValue toJson() {
 		return makeJsonObjectBuilder().build();
 	}
 }

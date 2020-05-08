 package org.ecn.edtemps.models;
 
import java.util.List;

 /**
  * Classe modèle d'un utiliateur
  * 
  * @author Remi
  */
 public class Utilisateur {
 
 	protected String nom;
 	protected String prenom;
 	protected String email;
	protected List<Integer> type;
 	
 
 	/**
 	 * Constructeur utilisant les paramètres indispensables
 	 * @param nom
 	 * @param prenom
 	 * @param email
 	 */
 	public Utilisateur(String nom, String prenom, String email) {
 		this.nom = nom;
 		this.prenom = prenom;
 		this.email = email;
 	}
 	
 	
 	public String getNom() {
 		return nom;
 	}
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 	public String getPrenom() {
 		return prenom;
 	}
 	public void setPrenom(String prenom) {
 		this.prenom = prenom;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
	public List<Integer> getType() {
 		return type;
 	}
	public void setType(List<Integer> type) {
 		this.type = type;
 	}
 
 }

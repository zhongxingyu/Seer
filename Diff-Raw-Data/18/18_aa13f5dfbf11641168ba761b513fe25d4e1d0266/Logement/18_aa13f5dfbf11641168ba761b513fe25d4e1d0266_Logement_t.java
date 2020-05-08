 import java.util.*;
 import java.io.*;
 
 /**	Classe : Logement
 *	Méthodes :
 * 		Constructeur avec saisie
 * 		getType : retourne le type de Logement
 * 		getAdresse : retourne l'adresse du Logement
 * 		getCapacite : retourne la capacité du Logement
 * 		getPrix : retourne le pris du Logement
 * 		Méthode de calcul du prix en fonction du nombre d'adultes (nba) et d'enfants (nbe)
 * 		Méthode renvoyant toutes les informations sur le Logement
 * 		getDispo : retourne la disponibilité du logement
 * 		reserved : met dispo à faux
 */
 public abstract class Logement implements java.io.Serializable
 {
 	private String type,adresse;
 	private int capacite;
 	private int prix;
 	private boolean dipso;
 	
 	/** Constructeur
 	* 	avec saisie des données
 	*/
 	public Logement (String type,int capacite,String adresse,int prix)
 	{
 		this.type=type;
 		this.adresse=adresse;
 		this.capacite=capacite;
 		this.prix=prix;
		this.dispo=true;
 	}
 	
 	/** getDispo : retourne la disponibilité du logement
 	* @return dispo  disponibilité du logement
 	*/
	public boolean getDispo()
 	{
 		return dispo;
 	}
 	
 	/** reserved : met dispo à faux
 	* i.e.: logement réservé
 	*/
 	public void reserved()
 	{
 		dispo=false;
 	}
 	
 	/** getType : retourne le type de Logement
 	* @return type le type de Logement
 	*/
 	public String getType()
 	{
 		return this.type;
 	}
 	
 	/** getAdresse : retourne l'adresse du Logement
 	* @return adresse l'adresse du Logement
 	*/
 	public String getAdresse()
 	{
 		return this.adresse;
 	}
 	
 	/** getCapacite : retourne la capacité du Logement
 	* @return capacite la capacité du Logement
 	*/
 	public int getCapacite()
 	{
 		return this.capacite;
 	}
 	
 	/** getPrix : retourne le prix du Logement
 	* @return prix le prix du Logement
 	*/
 	public int getPrix()
 	{
 		return this.prix;
 	}
 	
 	/** Méthode permettant de calculer le prix en fonction
 	*	du nombre d'enfants et d'adultes
 	* @return prix le prix
 	* @param nba nombre d'adultes
 	* @param nbe nombre d'enfants
 	*/
 	public abstract int calculprix(int nba,int nbe);
 
 	/** Méthode toString redéfinie pour renvoyer une phrase
 	 * 	contenant toutes les informations sur le Logement
 	* @return type
 	* @return adresse
 	* @return capacite
 	* @return prix
 	*/
 	public String toString()
 	{
 		return this.type+" "+this.adresse+" "+this.prix+"€";
 	}
 }

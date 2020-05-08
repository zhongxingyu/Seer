 import java.util.*;
 import java.io.*;
 
 /**	Classe : Reservation
 *	Méthodes :
 * 		Constructeur avec saisie des données
 * 		Méthode retournant le nom du locataire principal et le prix
 */
 public abstract class Reservation implements java.io.Serializable
 {
 	private List <Logement> logements;
 	private String nomloc;
 	private int adultes,enfants;
 	private String nomprop;
 	
 	/** Constructeur
 	*	avec saisie des données
 	*/
 	public Reservation (List logements,String nomloc,int adultes,int enfants,String nomprop)
 	{
 		this.logements=logements;
 		this.nomloc=nomloc;
 		this.adultes=adultes;
 		this.enfants=enfants;
 		this.nomprop=nomprop;	
 	}
 	
 	/** Méthode toString retournant le nom du
 	*	locataire principal et le prix
 	* @return nomloc le nom du locataire principal
 	* @return prix le prix du logement
 	* @see Logement#calculPrix
 	*/
 	public String toString()
 	{
 		int prix=0;
 		for (Logement l : logements)
 		{
			prix+=l.calculPrix(adultes,enfants);
 		}
 		return this.nomloc+" "+prix;
 	}
 	
 	/** Méthode toString retournant le nom du
 	*	locataire principal et le prix
 	* @return nomloc le nom du locataire principal
 	* @return prix le prix du logement
 	* @see Logement#calculPrix
 	*/
 	public String propositionToString()
 	{
 		int prix=0;
 		for (Logement l : logements)
 		{
			prix+=l.calculPrix(adultes,enfants);
 		}
 		return this.nomloc+" "+logement.calculPrix(adultes,enfants);
 	}
 	
 	/** Méthode valide 
 	*	mise à jour de:
 	* 	la disponibilité des logements
 	*	le chiffre d'affaire du proprietaire
 	*	la disponibilite totale du proprietaire
 	*/
 	public void valide(ListeProprio listep)
 	{
		Proprietaire bailleur=listep.get(nomprop);
 		for (Logement l:logements)
 		{
 			l.reserved();
 			bailleur.addCa(l.calculPrix(adultes,enfants));
 			bailleur.addDisp(-(adultes+enfants));
 		}
 	}
 }

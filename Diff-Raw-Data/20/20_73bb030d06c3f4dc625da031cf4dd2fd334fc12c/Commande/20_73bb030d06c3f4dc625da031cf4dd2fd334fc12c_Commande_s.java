 package Modele;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Fonctionnement des commandes
  * 1 Ecran Achat billet : 
  * 		1 Choix d'un billet à partir d'une liste (sera amélioré après !)
  * 		2 Choix de la quantite, subventionne, paye, donne
  * 		3 Deux boutons : Terminer la commande ou Continuer la commande
  * 2 Si "Continuer la commande" appuye
  * 		1 Verifie si l'achat est possible
  * 		2 Si c'est possible, on ajoute le billet à la listeValidee, listeAchats de la personne et on repercute sur le billet
  * 		3 Si ce n'est pas possible, une fenêtre de "forçage" est presente
  * 			1 Si on valide, on force le processus même si il y a des erreurs (pas suffisament de billet sub par ex)
  * 			2 Si on annule, la commande n'est pas prise en compte
  * 3 Terminer la commande
  */
 
 public class Commande {
 	private Personne personne;
 	private float nbArticle;
 	private int prixTotal;
 	private List<Achat> listeValidee = new ArrayList<Achat>();
 	private Achat achatEnCours;
 	
 	
 	public Commande(Personne personne) {
 		this.personne = personne;
 		this.nbArticle = 0;
 		this.prixTotal = 0;
 	}
 	
 	/********** Methodes ************/
 	/**
 	 * Realise un achat si cela est possible
 	 * @param billet
 	 * @param qt
 	 * @param paye
 	 * @param donne
 	 * @param subventionne
 	 * @return vrai si l operation a reussie
 	 * @throws AchatException 
 	 */
 	public void ajoutCommande(Billet billet, int qt, boolean paye, boolean donne, boolean subventionne) throws AchatException {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("id", billet.getId() + "Z" + personne.getId() + "Z" + personne.getAchats().getNbAchats());
 		map.put("id_billet", billet.getId());
 		map.put("id_personne", personne.getId());
 		map.put("quantite", qt);
 		map.put("paye", paye);
 		map.put("donne", donne);
 		map.put("subventionne", subventionne);
 		if (subventionne) {
 			map.put("prix_unitaire", billet.getPrixRed());
 			map.put("prix_total", billet.getPrixRed() * qt);
 		} else {
 			map.put("prix_unitaire", billet.getPrixNor());
 			map.put("prix_total", billet.getPrixNor() * qt);
 		}
		//TODO Date
 		achatEnCours = new Achat(map, personne);
 		
 		if (this.achatPossible(billet, qt, subventionne)) {
 			valider();
 		}
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode valide la derniere commande
 	 */
 	public void valider() {
 		listeValidee.add(achatEnCours);
 		achatEnCours.ajoute();
 		prixTotal += achatEnCours.getPrixTotal();
 		achatEnCours = null;
 		nbArticle++;
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode annule la derniere commande en l'enlevant de la liste
 	 */
 	public void annuler() {
 		achatEnCours = null;
 	}
 	
 	/**
	 * Vide la listeValidee
 	 */
 	public void cloturer() {
 		listeValidee.clear();
 	}
 	
 	/**
 	 * Verifie si un achat est possible
 	 * Si ce n'est pas possible, cette methode envoie des exceptions
 	 * @param billet
 	 * @param qt
 	 * @param subventionne
 	 * @return
 	 * @throws AchatException
 	 */
 	public boolean achatPossible(Billet billet, int qt, boolean subventionne) throws AchatException{
 		if (billet.getNbPlace() >= qt) {
 			if(subventionne) {
 				if (billet.getNbPlaceSub() >= qt) {
 					if (billet.getNbPlacePerso() - personne.nbBilletsAchete(billet) >= qt) {
 						
 					} else {
 						throw new AchatException(0);
 					}
 				} else {
 					throw new AchatException(2);
 				}
 			}
 		} else {
 			throw new AchatException(1);
 		}
 		return true;
 	}
 	
 	
 	/********** Getters ************/
 	public float getNbArticle() {
 		return nbArticle;
 	}
 	public int getPrixTotal() {
 		return prixTotal;
 	}
 
 	
 	/********** Methodes de base ************/
 	public String toString() {
 		return "Commande en cours :\nnb articles : " + nbArticle + "\nprix total : " + prixTotal + "\n" + listeValidee;
 	}
 
 	
 }

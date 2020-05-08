 package modele;
 
 import general.Langue;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Fonctionnement des commandes
  * Lorque l'on clique sur "Passer commande"
  * 1 L'ecran Achat billet apparait, il permet de : 
  * 		1 Choisir un billet à partir d'un tableau -> fait
  * 		2 Choisir la quantite -> fait
  * 		3 Choisir subventionne (permet de savoir si le billet est subventionne)
  * 		4 Choisir donne (permet de savoir si le billet a ete remis a la personne)
  * 		5 Choisir paye (permet de savoir si le billet a ete paye par la personne)
  * 		6 Deux boutons : Terminer la commande ou Continuer la commande
  * 			-> Le bouton "Continuer la Commande" fait appel aux fonctions 
  * 					1 ajoutCommande(param) (cf 2.)
  * 					2 affiche un nouvel ecran Achat Billet
  * 			-> Le bouton "Terminer la Commande" fait appel aux fonctions 
  * 					1 ajoutCommande(param) (cf 2.)
  * 					2 affiche la fenetre "Fin de la commande" (cf 3.)
  *
  * 2 Fonction ajoutCommande : La fonction verifie si l'achat est possible (pas suffisament de billet sub par ex)
  * 		-> Si c'est possible, l'achat est automatiquement valide (ajout a la liste des achats de la personne)
  * 		-> Si ce n'est pas possible, une erreur est lance par ajoutCommande
  * 			=> une fenêtre de "forçage" doit être proposés
  * 				1 Si on valide, cella appelle la focntion valider() : on force le processus même si il y a des erreurs 
  * 				2 Si on annule, cella appelle la focntion annuler() : la commande n'est pas prise en compte
  *
  * 3 Si "Terminer la commande"
  * 		2 Cette fenetre presente un recapitulatif des billets achetes ainsi que le montant à payer
  * 		3 Un unique bouton qui affiche "Terminer"
  */
 
 public class Commande {
 	private Personne personne;
 	private int nbArticle;
 	private double prixTotal;
 	private List<Achat> listeValidee = new ArrayList<Achat>();
 	
 	private Billet billet;
 	private int qt;
 	private int qt_acceptable;
 	private boolean donne;
 	private boolean paye;
 	private boolean subventionne;
 	
 	
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
 	 * @throws AchatException 
 	 */
 	public void ajoutCommande(Billet billet, int qt, boolean paye, boolean donne, boolean subventionne) throws AchatException {
 		this.billet = billet;
 		this.qt = qt;
 		this.paye = paye;
 		this.donne = donne;
 		this.subventionne = subventionne;
 		if (this.achatPossible()) {
 			valider();
 		}
 	}
 	
 	private void creer(Billet billet, int qt, boolean paye, boolean donne, boolean subventionne) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("quantite", qt);
 		map.put("paye", paye);
 		map.put("donne", donne);
 		map.put("subventionne", subventionne);
 		
 		Date maDateAvecFormat=new Date();
 		SimpleDateFormat dateStandard = new SimpleDateFormat("dd/MM/yyyy-HH-mm");
 		map.put("date", dateStandard.format(maDateAvecFormat));
 		
 		if (subventionne) {
 			map.put("prix_unitaire", billet.getPrixRed());
 			map.put("prix_total", billet.getPrixRed() * qt);
 		} else {
 			map.put("prix_unitaire", billet.getPrixNor());
 			map.put("prix_total", billet.getPrixNor() * qt);
 		}
 		Achat achatEnCours = new Achat(map, personne, billet);
 		listeValidee.add(achatEnCours);
 		prixTotal += achatEnCours.getPrixTotal();
 		nbArticle++;
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode valide la derniere commande
 	 */
 	public void valider() {
 		this.creer(billet, qt, paye, donne, subventionne);		
 	}
 	
 	public void completer() {
 		if(qt_acceptable > 0)
 			this.creer(billet, qt_acceptable, paye, donne, subventionne);
 		if(qt_acceptable < 0)
 			qt_acceptable = 0;
 		this.creer(billet, qt-qt_acceptable, paye, donne, false);
 	}
 	
 	/**
 	 * Si le message d'erreur quota depasse est lance par la commande ajoutCommande. 
 	 * Une boite de dialogue est affichee pour valider cette action normalement non autorisee
 	 * Cette methode annule la derniere commande en l'enlevant de la liste
 	 */
 	public void annuler() {
 		//TODO
 	}
 	
 	/**
 	 * Vide la listeValidee :  clorture une commande
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
 	public boolean achatPossible() throws AchatException{
 		if (billet.getNbPlace() >= qt) {
 			if(subventionne) {
 				if (billet.getNbPlaceSub() >= qt) {
 					qt_acceptable = billet.getNbPlacePerso() - personne.getNbBilletsAcheteSub(billet);
 					if (qt_acceptable >= qt) {
 						qt_acceptable = qt;
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
 	
 	
 	/********** Getters & Setters ************/
 	public int getNbArticle() {
 		return nbArticle;
 	}
 	public double getPrixTotal() {
 		return prixTotal;
 	}
 	public String toString() {
 		return Langue.getTraduction("shopping") + " : " + nbArticle + " " + 
 				Langue.getTraduction("articles") + " | " + prixTotal + " euros";
 	}
 }

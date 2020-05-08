 package modele;
 
 import general.Constantes;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 
 public class Personne extends Objet{
 	
 	private ListeAchats listeAchats = null;
 	private static int prochainId; 	// id utilise par la prochaine personne creee
 
 	
 	/********** Constructeurs ************/
 	/**
 	 * Constructeur d'une personne deja presente dans la bdd, 
 	 * La map contient l'ensemble des attributs de l'objet ainsi que son id
 	 * @param map
 	 * @param bill
 	 */
 	public Personne (Map<String, Object> map, Billeterie bill){
 		super();
 		this.map = map;
 		this.billeterie = bill;
 		this.listeAchats = new ListeAchats(this);
 		listeAchats.metEnMemoire();
 	}
 	
 	/**
 	 * Constructeur d'une personne qui n'est pas present dans la bdd. La personne a ete creee par l'utilisateur
 	 * La map contient l'ensemble des attributs de l'objet mais pas son Id. Cet Id sera ajouté par le constructeur
 	 * @param map
 	 * @param bill
 	 * @param useless ce param sert juste a diffrencier les deux constructeurs
 	 */
 	public Personne (Map<String,Object> map, Billeterie bill, int useless){
 		super();
 		this.map = map;
 		this.billeterie = bill;
 
 		// Attribue un Id a cette nouvelle personne
 		if (!map.containsKey("id")) {
 			map.put("id", prochainId);
 			prochainId++;
 		}
 		
 		// Enregistre la nouvelle personne dans la bdd
 		try {
 			bill.getBdd().ajoutBDD("personne", map);
 		} catch (SQLException e) {
 			Constantes.afficherException(e);
 		}
 		
 		this.listeAchats = new ListeAchats(this);
 		listeAchats.metEnMemoire();
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 *  Cette methode modifie un billet et l'enregistre dans la bdd
 	 *  @param map
 	 */
 	public void modifie(Map<String,Object> nouvelleMap) {
 		try {
 			// Remplace l'ancienne map par la nouvelle en ajoutant l'id si celui-ci n'est pas present dans la nouvelle
 			int ancId = this.getId();
 			this.map = nouvelleMap;
 			if (!map.containsKey("id")) {
 				map.put("id", ancId);
 			}
 			
 			// Sauvegarde les modifs dans la bdd
 			billeterie.getBdd().enregistreBDD("personne", map); //NOM BDD
 		} catch (SQLException e) {
 			Constantes.afficherException(e);
 		}
 		billeterie.getListePersonnes().modifier();
 	}
 	
 	/**
 	 * Cette methode supprimer une personne de la memoire ainsi que dans la ListePersonnes
 	 */
 	public void supprimer() {
 		billeterie.getBdd().supprimer("Personne", this.getId()+"");
 		billeterie.getListePersonnes().supprimer(this);
 		List<Objet> listeObjet = listeAchats.getListeAchats();
 		for (int i = 0; i < listeObjet.size(); i++) {
 			listeObjet.get(i).supprimer();
 		}
 	}
 	
 	/**
 	 * Cette methode supprimer l'ensemble des achats lies au billet
 	 */
 	public void supprimerAchat(Billet billet) {
 		List<Objet> listeObjet = listeAchats.getListeAchats();
 		for (int i = 0; i < listeObjet.size(); i++) {
 			Achat achat = (Achat) listeObjet.get(i);
			if (achat.getBillet().equals(billet))
 				achat.supprimer();
 		}
 	}
 	
 	/**
 	 * @param billet
 	 * @return le nombre de billet deja achete pour un Billet
 	 */
 	public int getNbBilletsAchete(Billet billet) {
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		int id = billet.getId();
 		int resul = 0;
 		
 		for (int i = 0; i < liste.size(); i++) {
 			if (id == ((Achat)liste.get(i)).getBillet().getId()) {
 				resul += ((Achat) liste.get(i)).getQt();
 			}
 		}
 		return resul;
 	}
 	
 	/**
 	 * @param billet
 	 * @return le nombre de billet deja achete pour un Billet
 	 */
 	public int getNbBilletsAcheteSub(Billet billet) {
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		int id = billet.getId();
 		int resul = 0;
 		for (int i = 0; i < liste.size(); i++) {
 			Achat achat = (Achat) liste.get(i);
 			if (id == achat.getBillet().getId() && achat.isQuotaEnCours()) {
 				resul += ((Achat) liste.get(i)).getQt();
 			}
 		}
 		return resul;
 	}
 
 	/**
 	 * @return Retourne ce qui reste a payer (lorsqu'un billet n'est pas directement paye)
 	 */
 	public double getRestantAPayer() {				
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		double resul = 0;
 		
 		for (int i = 0; i < liste.size(); i++) {
 			if (! ((Achat) liste.get(i)).getPaye()) {
 				resul += ((Achat) liste.get(i)).getPrixTotal();
 			}
 		}
 
 		return resul;
 	}
 	
 	/* Methodes pour les statistiques*/
 	/**
 	 * @return Retourne le montant total des achats
 	 */
 	public double getTotalPrix() {				
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		double resul = 0;
 		
 		for (int i = 0; i < liste.size(); i++) {
 				resul += ((Achat) liste.get(i)).getPrixTotal();
 		}
 
 		return resul;
 	}
 	
 	/**
 	 * @return le Retourne le nombre total des achats
 	 */
 	public int getTotalArticles() {				
 		return listeAchats.getNbAchats();
 	}
 	
 	/**
 	 * @param date
 	 * @return le montant total des achats depuis la date
 	 */
 	public double getMoisPrix(Date date) {				
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		double resul = 0;
 				
 		for (int i = 0; i < liste.size(); i++) {
 			Achat achat = ((Achat) liste.get(i));
 			if (date.before(achat.getDate())) {	
 				resul += achat.getPrixTotal();
 			}
 		}
 
 		return resul;
 	}
 	
 	/**
 	 * @param date
 	 * @return le nombre d'achat depuis la date
 	 */
 	public int getDateArticles(Date date) {				
 		List<Objet> liste = this.listeAchats.getListeAchats();
 		int resul = 0;
 				
 		for (int i = 0; i < liste.size(); i++) {
 			Achat achat = ((Achat) liste.get(i));
 			if (date.before(achat.getDate())) {	
 				resul += 1;
 			}
 		}
 
 		return resul;
 	}
 
 	/********** Getters sur les attributs de la BDD ************/
 	public String getNom() {
 		return (String) map.get("nom");
 	}
 	public String getPrenom() {
 		return (String) map.get("prenom");
 	}
 
 	/********** Getters & Setters ************/
 	public static void setProchainId(int prochainId) {
 		Personne.prochainId = prochainId;
 	}
 	public static int getProchainId() {
 		return prochainId;
 	}
 	public ListeAchats getAchats() {
 		return this.listeAchats;
 	}
 	public boolean equal(Personne pers) {
 		return this.getId() == pers.getId();
 	}
 	public String toString () {
 		return map.get("prenom") + " " + map.get("nom");
 	}
 }

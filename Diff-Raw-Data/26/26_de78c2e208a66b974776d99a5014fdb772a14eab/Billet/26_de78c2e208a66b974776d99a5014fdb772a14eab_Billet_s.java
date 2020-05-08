 package modele;
 
 import java.sql.SQLException;
 import java.util.Map;
 
 
 public class Billet extends Objet {
 	private static int prochainId; 	// id utilise par le prochain billet cree
 
 	
 	/********** Constructeurs ************/
 	/**
 	 * Constructeur d'un billet deja present dans la bdd, 
 	 * La map contient l'ensemble des attributs de l'objet ainsi que son id
 	 * @param map
 	 * @param bill
 	 */
 	public Billet(Map<String,Object> map, Billeterie bill) {
 		super();
 		this.map = map;
 		this.billeterie = bill;
 	}
 	
 	/**
 	 * Constructeur d'un billet qui n'est pas present dans la bdd. Le billet a ete cree par l'utilisateur
 	 * La map doit contenir l'ensemble des attributs de l'objet MAIS PAS SON ID. Cet Id sera ajouté par le constructeur
 	 * @param map
 	 * @param bill
 	 * @param useless ce param sert juste a diffrencier les deux constructeurs
 	 */
 	public Billet(Map<String,Object> map, Billeterie bill, int useless) {
 		super();
 		this.map = map;
 		this.billeterie = bill;
 		
 		// Attribue un Id a ce nouveau billet
 		if (!map.containsKey("id")) { //NOM BDD
 			map.put("id", prochainId);
 			prochainId++;
 		}
 		
 		// Enregistre le nouveau billet dans la bdd
 		try {
 			bill.getBdd().ajoutBDD("billet", map); //NOM BDD
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 *  Cette methode enregistre un billet en memoire grace a une requete update
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
 			billeterie.getBdd().enregistreBDD("billet", map); //NOM BDD
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		billeterie.getListeBillets().modifier();
 	}
 	
 	/**
 	 * Cette methode supprimer un billet de la memoire ainsi que dans la ListeBillets
 	 */
 	public void supprimer() {
 		billeterie.getBdd().supprimer("Billet", this.getId());
 		billeterie.getListeBillets().supprimer(this);
 	}
 	
 	/**
 	 * Augmente ou diminue le nombre de billet restant, 
 	 * subventionne permet de savoir si les billets à enlever sont des billets subventionnes
 	 * Si qt > 0, cela ajoute des billets
 	 * Quand on ajoute des billets sub, on ajoute aussi la meme quantite de billet non sub
 	 * @param qt
 	 * @param subventionne
 	 */
 	public void modifieQt(int qt, boolean subventionne) {
 		if(subventionne) {
 			map.put("nb_sub", getNbPlaceSub()+qt);
 			map.put("nb_total", getNbPlace()+qt);
 		} else {
 			map.put("nb_total", getNbPlace()+qt);
 		}	
 		if((Integer) map.get("nb_sub") < 0) {
 			map.put("nb_sub", 0);
 		}
 		if((Integer) map.get("nb_total") < 0) {
 			map.put("nb_total", 0);
 		}
 		if((Integer) map.get("nb_total") < (Integer) map.get("nb_sub")) {
 			map.put("nb_sub", (Integer) map.get("nb_total"));
  		}
 		modifie(this.map);
 	}
 	
 	
 	/********** Getters sur les attributs de la BDD ************/
 	public int getNbPlace() {
 		return (Integer) map.get("nb_total"); //NOM BDD
 	}
 	public int getNbPlaceSub() {
 		return (Integer) map.get("nb_sub"); //NOM BDD
 	}
	public double getPrixRed() {
		return (Double) map.get("prix_sub"); //NOM BDD
 	}
	public double getPrixNor() {
		return (Double) map.get("prix"); //NOM BDD
 	}
 	public int getNbPlacePerso() {
 		return (Integer) map.get("nb_sub_par_personne"); //NOM BDD
 	}
 	
 	/********** Methodes de bases ************/
 	public static int getProchainId() {
 		return prochainId;
 	}
 	public static void setProchainId(int prochainId) {
 		Billet.prochainId = prochainId;
 	}
 	public boolean equal(Billet bill) {
 		return this.getId() == bill.getId();
 	}
 	public String toString () { //NOM BDD
 		return map.get("categorie") + " : " + map.get("sous_categorie");
 	}
 }

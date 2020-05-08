 package modele;
 
 import general.Constantes;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 public class ListeBillets extends ListeObjet{
 	private static final long serialVersionUID = 1L;
 	
 	
 	/********** Constructeur ************/
 	/**
 	 * Crée l'objet en mettant en mémoire l ensemble des billets de la bdd
 	 * @param billeterie 
 	 */
 	public ListeBillets(Billeterie billeterie) {
 		super(billeterie);
 		attributs();
 		this.metEnMemoire(); 
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 * Met en memoire l'ensemble des billets.
 	 */
 	public void metEnMemoire() {
 		List<Map<String, Object>> list = billeterie.getBdd().getObjets("SELECT * from billet"); //NOM BDD
 		for (int i = 0; i < list.size(); i++){
 			listeObjet.add(new Billet(list.get(i), billeterie));
 		}
 		Billet.setProchainId((Integer)list.get(listeObjet.size() - 1).get("id")+1);
 		this.sauvegarde();
 	}
 	
 	/**
 	 * Cette methode permet de récuperer la liste des attributs pour l'objet Billet
 	 */
 	private void attributs() {
 		Map<String, Integer> map = billeterie.getBdd().getAttributs("Billet");
 		List<Attribut> resul = new ArrayList<Attribut>();
 		
 		resul.add(new Attribut("categorie", "Categorie", map.get("categorie"))); map.remove("categorie");
 		resul.add(new Attribut("sous_categorie", "Sous categorie", map.get("sous_categorie"))); map.remove("sous_categorie");
 		resul.add(new Attribut("prix", "Prix", map.get("prix"))); map.remove("prix");
 		resul.add(new Attribut("prix_sub", "Prix Subventionne", map.get("prix_sub"))); map.remove("prix_sub");
 		resul.add(new Attribut("nb_total", "Quantite", map.get("nb_total"))); map.remove("nb_total");
		resul.add(new Attribut("nb_sub_par_personne", "Quantite Sub/Personne", map.get("nb_sub_par_personne"))); map.remove("nb_sub_par_personne");
		resul.add(new Attribut("nb_sub", "Quantite Sub", map.get("nb_sub"))); map.remove("nb_sub");
 		
 		Set<String> set = map.keySet();
 		Iterator<String> it = set.iterator();
 		while (it.hasNext()) {
 			String nom = it.next();
 			resul.add(new Attribut(nom, nom, map.get(nom)));
 		}
 		this.attributs = resul;
 	}
 	
 	/**
 	 * Renvoie l'ensemble des billets lies a la recherche
 	 * @param chaine a trouver dans le billet
 	 * @return la liste des billets
 	 */
 	public void recherche(String chaine) {
 		reinitialise();
 		List<Objet> resul= new ArrayList<Objet>();
 		
 		String query = requete(chaine);
 		List<Map<String, Object>> list = billeterie.getBdd().getObjets(query);
 		
 		if(!list.isEmpty()) {
 			for (int i = 0; i< listeObjet.size(); i++) {
 				int Id = listeObjet.get(i).getId();
 				boolean stop = false;
 				for (int j = 0; j < list.size() && !stop; j++) {
 					if (Id == (Integer)list.get(j).get("id")) {
 						resul.add(listeObjet.get(i));
 						stop = true;
 					}
 				}
 			}
 			listeObjet = resul;
 		}
 		
 		fireTableDataChanged();
 	}
 	
 	/**
 	 * Cette methode permet d'ajouter un Billet dans la liste
 	 * @param la liste des attributs sous forme d'une map
 	 */
 	public void ajouter(Map<String, Object> map) {
 		reinitialise();
 		Billet billet = new Billet(map, billeterie, 1);
 		listeObjet.add(billet);
 		fireTableDataChanged();
 		sauvegarde();
 	}
 	
 	/**
 	 * Methode creant une requete pour la recherche
 	 */
 	public String requete (String chaine) {
 		String retour = "SELECT id FROM billet WHERE ";//NOM BDD
 		boolean premier = true;
 		List<Attribut> list = getAttributs();
 		
 		for (int i = 0; i < list.size() ; i++) {
 			if(list.get(i).getType() == Constantes.STRING) {
 				String aAjouter = list.get(i).getNomBDD() + " Like '"+chaine+"%' ";
 				if(!premier) {
 					retour = retour.concat(" OR ");
 				}
 				retour = retour.concat(aAjouter);
 				premier = false;
 			}
 		}
 		return retour;
 	}
 }

 package modele;
 
 
 import general.Constantes;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ListeAchats extends ListeObjet{
 	private static final long serialVersionUID = 1L;
 	private Personne personne;
 	private int nbAchats;
 	
 	
 	/********** Constructeur ************/
 	public ListeAchats(Personne perso) {
 		super(perso.getBilleterie());
 		this.personne = perso;
 		attributs();
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 * Met en memoire liste)Achats a partir de la BDD
 	 */
 	public void metEnMemoire() {
		personne.getBilleterie().getListeBillets().reinitialise();
 		String query = "SELECT * from achat WHERE id_personne=" + personne.getId(); //NOM BDD
 		List<Map<String, Object>> list = personne.getBilleterie().getBdd().getObjets(query); 
 		for (int i = 0; i < list.size(); i++){
 			listeObjet.add(new Achat(list.get(i), personne));
 			nbAchats++;
 		}
 		personne.setAchatEnMem(true);
 		sauvegarde();
 	}
 	
 	/**
 	 * Cette methode permet de récuperer la liste des attributs pour l'objet Personne
 	 */
 	private void attributs() {
 		Map<String, Integer> map = billeterie.getBdd().getAttributs("Achat");
 		List<Attribut> resul = new ArrayList<Attribut>();
 		resul.add(new Attribut("Description", "Description", Constantes.STRING));
 		
 		Set<String> set = map.keySet();
 		Iterator<String> it = set.iterator();
 		while (it.hasNext()) {
 			String nom = it.next();
 			resul.add(new Attribut(nom, nom, map.get(nom)));
 		}
 		this.attributs = resul;
 	}
 	
 	/**
 	 * Ajoute un achat a la listeAchats
 	 * @param achat
 	 */
 	public void ajouter(Achat achat) {
 		if(!personne.isAchatEnMem()) {
 			metEnMemoire();
 		}
 		listeObjet.add(achat);
 		nbAchats++;
 	}
 	
 	/** Methodes non implementees */
 	public void ajouter(Map<String, Object> map) {}
 	public void recherche(String chaine) {}
 	
 	/********** Methodes de base ************/
 	public List<Objet> getListeAchats() {
 		if(!personne.isAchatEnMem()) {
 			metEnMemoire();
 		}
 		return listeObjet;
 	}
 	public int getNbAchats() {
 		return nbAchats;
 	}
 	public String toString() {
 		return listeObjet.toString();
 	}
 }

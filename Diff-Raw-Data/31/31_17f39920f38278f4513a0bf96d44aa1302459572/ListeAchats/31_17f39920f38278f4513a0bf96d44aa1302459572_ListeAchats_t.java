 package Modele;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class ListeAchats {
 	private List<Achat> listeAchats = new ArrayList<Achat>();
 	private Personne personne;
 	
 	
 	/********** Constructeur ************/
 	public ListeAchats(Personne perso) {
 		this.personne = perso;
 	}
 	
 	
 	/********** Methodes ************/
 	/**
 	 * Met en memoire listeAchats a partir de la BDD
 	 */
 	public void metEnMemoire() {
 		try {
			String query = "SELECT * from achat WHERE id_personne=" + personne.getId(); //NOM BDD
 			List<Map<String, Object>> list = personne.getBilleterie().getBdd().query(query); 
 			for (int i = 0; i < list.size(); i++){
 				listeAchats.add(new Achat(list.get(i), personne));
 			}
 			personne.setAchatEnMem(true);
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Ajoute un achat a la listeAchats
 	 * @param achat
 	 */
 	public void ajoutAchat(Achat achat) {
 		if(!personne.isAchatEnMem()) {
 			metEnMemoire();
 		}
 		listeAchats.add(achat);
 	}
 	
 	
 	/********** Methodes de base ************/
 	public List<Achat> getListeAchats() {
 		if(!personne.isAchatEnMem()) {
 			metEnMemoire();
 		}
 		return listeAchats;
 	}
 	public String toString() {
 		return listeAchats.toString();
 	}
 }

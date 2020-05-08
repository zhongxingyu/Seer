 package modele;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 public abstract class ListeObjet extends AbstractTableModel{
 	private static final long serialVersionUID = 1L;
 	protected Billeterie billeterie;	
 	protected List<Objet> listeObjet = new ArrayList<Objet>();
 	protected List<Objet> listeObjetSauvegarde = new ArrayList<Objet>();
 	protected JTable tableau;
 	protected List<Attribut> attributs = new ArrayList<Attribut>();
 
 	
 	/**
 	 * Constructeur
 	 * @param billeterie
 	 */
 	public ListeObjet(Billeterie billeterie) {
 		this.billeterie = billeterie;
 	}
 	
 	/**
 	 * Cette methode permet de mettre en memoire les objets a partir de la bdd
 	 */
 	public abstract void metEnMemoire();
 	
 	/**
 	 * Renvoie l'ensemble des billets lies a la recherche
 	 * @param chaine la chaine a trouver dans le billet
 	 * @return la liste des billets
 	 */
 	public abstract void recherche(String chaine);
 	
 	/**
 	 * Cette methode met a jour le modele de donnes lors de suppressions
 	 * @param objet
 	 */
 	public void supprimer(Objet objet) {
 		listeObjet.remove(objet);
 		listeObjetSauvegarde.remove(objet);
 		fireTableDataChanged();
 	}
 	
 	/**
 	 * Cette methode met a jour le modele de donnes lors de modifications
 	 */
 	public void modifier() {
 		reinitialise();
 		fireTableDataChanged();
 		sauvegarde();
 	}
 	
 	/**
 	 * Ajoute un objet dans la liste
 	 * @param objet
 	 */
 	public abstract void ajouter(Map<String, Object> map);
 	
 	/**
 	 * @param index
 	 * @return L'objet situe a l'index index
 	 * @throws Exception 
 	 */
 	public Objet getObjetByIndex(int index) throws Exception{
 		Objet objet = null;
 		objet = listeObjet.get(index);
 		if (objet == null){
 			throw new Exception("Objet d'index " + index + " non existante");
 		}
 		return objet;
 	}
 	
 	/**
 	 * @param id
 	 * @return l'objet d'id id
 	 * @throws Exception
 	 */
 	public Objet getObjetById(int id) throws Exception {
 		Objet objet = null;
 		boolean stop = false;
 		for (int i = 0; i < listeObjet.size() && !stop; i++) {
 			if (id == listeObjet.get(i).getId()) {
 				objet = listeObjet.get(i);
 				stop = true;
 			}
 		}
 		if (objet == null){
 			throw new Exception("Objet d'id " + id + " non existante");
 		}
 		return objet;
 	}
 	
 	
 	/********** Methodes privees ************/
 	/**
 	 * Cette methode permet de sauvegarder la liste des billets
 	 * Par exemple lorsque l'on fait une recherche, listeBillets sera remplacee
 	 * par une liste de billets trouves.
 	 */
 	protected void sauvegarde() {
 		List<Objet> list = new ArrayList<Objet>();
 		for (int i = 0; i < listeObjet.size(); i++) {
 			list.add(listeObjet.get(i));
 		}
 		listeObjetSauvegarde = list;
 	}
 	
 	/**
 	 * Permet de remplacer la liste de billets par la liste de billets d'origine
 	 */
	public void reinitialise() {
 		listeObjet = listeObjetSauvegarde;
 		sauvegarde();
 	}
 	
 	
 	/********** Methodes pour la gestion de l'affichage ************/
 	public int getRowCount() {
     	return listeObjet.size();
     }
     public int getColumnCount() {
     	return getAttributs().size();
     }
     public String getColumnName(int columnIndex) {
     	return attributs.get(columnIndex).getNomInterface();
     }
     public Object getValueAt(int rowIndex, int columnIndex) {
     	return listeObjet.get(rowIndex).getHashMap().get(attributs.get(columnIndex).getNomBDD());    	
     }
     
     
 	/********** getters & Setters ************/
 	public Billeterie getBilleterie() {
 		return billeterie;
 	}
 	public List<Attribut> getAttributs() {
 		return attributs;
 	}
 	public JTable getTableau() {
 		return tableau;
 	}
 	public void setTableau(JTable tableau) {
 		this.tableau = tableau;
 	}
 	public String toString () {
 		return listeObjet.toString();
 	}
 }

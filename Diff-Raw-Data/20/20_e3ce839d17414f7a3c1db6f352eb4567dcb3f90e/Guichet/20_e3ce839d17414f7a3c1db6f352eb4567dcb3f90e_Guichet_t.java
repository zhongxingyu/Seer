 package noyau;
 
 import java.awt.Point;
 import java.util.LinkedList;
 
 import org.w3c.dom.Element;
 
 /**
  * Objet représentant un guichet,
  * par lequel on va ajouter des bagages 
  * à l'aéroport.
  * 
  * @author H4402
  */
 public class Guichet {
 	/**
 	 * Identifiant d'un guichet
 	 */
 	protected int id;
 
 	/**
 	 * Tapis sur lequel le guichet dépose les bagages.
 	 * 
 	 * @uml.property  name="tapis"
 	 * @uml.associationEnd  multiplicity="(1 1)"
 	 */
 	private Tapis tapis;
 	
 	/**
 	 * Coordonnées du Guichet.
 	 */
 	private Point coordonnees;
 	
 	/**
 	 * Liste des bagages qu'on ne peut 
 	 * mettre sur le tapis (on peut considéré
 	 * que c'est une file de voyageur).
 	 * Gérée en FIFO.
 	 * 
 	 * @uml.property  name="listBagages"
 	 */
 	private LinkedList<Bagage> listBagages;
 
 	/**
 	 * Constructeur pour GreenUML.
 	 * 
 	 * @param tapis Tapis sur lequel on dépose les bagages.
 	 * @param listBagages Liste des bagages en attente d'etre déposés.
 	 * @param coordonnees Coordonnées du guichet.
 	 */
 	public Guichet(Tapis tapis, LinkedList<Bagage> listBagages,
 		Point coordonnees) {
 		super();
 		this.tapis = tapis;
 		this.listBagages = listBagages;
 		this.coordonnees = coordonnees;
 	}
 	
 	/**
 	 * Constructeur par défault.
 	 */
 	public Guichet() {
 		this.tapis = null;
 		this.listBagages = new LinkedList<Bagage>();
 		this.coordonnees = new Point();
 	}
 
 	/**
 	 * Ajout de bagages dans le guichet.
 	 * Si la file de voyageur est vide,
 	 * on ajoute directement celui-la,
 	 * sinon, on ajoute celui en tete de file
 	 * et on ajoute le bagage de parametre en fin de file.
 	 */
 	public void ajoutBagage(Bagage b) {
		if(b != null) {
			listBagages.offerLast(b);
 		}
		Bagage bPrio = listBagages.peek();
		if(bPrio != null) {
			if(tapis.ajouterBagage(bPrio)) {	
 				listBagages.poll();
 			}
 		}
 	}
 	
 	/**
 	 * Retourne les coordonnées du Guichet
 	 * 
 	 * @return Coordonnées du guichet.
 	 */
 	public Point getCoordonnees() {
 		return coordonnees;
 	}
 	
 
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Permet de compléter un objet vide à partir du XML
 	 * 
 	 * @param guichetElement Element XML Guichet
 	 * @param aeroport Aeroport
 	 * @return Résultat du parsing
 	 */
 	public int construireAPartirDeXML(Element guichetElement, Aeroport aeroport)
 	{
         // On récupère l'id et les coordonnées
 		this.id = Integer.parseInt(guichetElement.getAttribute("id"));
         this.coordonnees.move(Integer.parseInt(guichetElement.getAttribute("posX")),
         		Integer.parseInt(guichetElement.getAttribute("posY")));
         
         // On récupère le noeud associé
         int idNoeud = Integer.parseInt(guichetElement.getAttribute("idNoeud"));
         Noeud noeudOld = aeroport.getNoeud(idNoeud);
 
         NoeudTapis noeudTapis = new NoeudTapis(idNoeud, noeudOld.getCoordonnees(), noeudOld.getListeRails());
         		
         //Création du tapis
         this.tapis = new Tapis(noeudTapis, this);	
        
         aeroport.ajouterTapis(this.tapis);
         
         aeroport.getListeNoeuds().remove(aeroport.getNoeud(idNoeud));
 		aeroport.ajouterNoeud(noeudTapis);
         
         return Aeroport.PARSE_OK;
     }
 }

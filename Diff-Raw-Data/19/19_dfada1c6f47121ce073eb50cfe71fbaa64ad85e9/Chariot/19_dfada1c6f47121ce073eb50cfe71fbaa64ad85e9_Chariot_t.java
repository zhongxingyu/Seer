 package noyau;
 
 import java.awt.geom.Point2D;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.w3c.dom.Element;
 
 // TODO: Mieux géré la destination.
 
 /**
  * Composante de l'application qui se déplace
  * d'un noeud à l'autre sur des rails et qui 
  * achemine un bagage à la fois.
  * 
  * @author H4402
  */
 public class Chariot {
 	
 	/**
 	 * Vitesse minimale d'un chariot.
 	 */
 	public static int VIT_MIN;
 	
 	/**
 	 * Vitesse maximale d'un chariot.
 	 */
 	public static int VIT_MAX;
 	
 	/**
 	 * Identifiant d'un noeud
 	 */
 	protected int id;
 	
 	/**
 	 * Coordonnées actuelles du chariot sur le rail.
 	 * 
 	 * @uml.property  name="coordonnees"
 	 */
 	private Point2D.Float coordonnees;
 
 	/**
 	 * Bagage à acheminer.
 	 * 
 	 * @uml.property  name="bagage"
 	 * @uml.associationEnd  multiplicity="(1 1)"
 	 */
 	private Bagage bagage;
 	
 	/**
 	 * Vitesse du chariot en metre/millisecondes.
 	 * 
 	 * @uml.property  name="vitesse"
 	 */
 	private float vitesse;
 	
 	/**
 	 * Liste des noeud a parcourir par le chariot
 	 * avant d'arriver à destination.
 	 * Gérée en FIFO.
 	 * 
 	 * @uml.property  name="chemin"
 	 */
 	private LinkedList<Noeud> chemin;
 	
 	/**
 	 * Distance sur le rail depuis le noeud d'entrée.
 	 */
 	private float distanceDepuisNoeudDepart;
 	
 	/**
 	 * Prochain Noeud qui n'est pas dans le chemin
 	 * car dans le chemin on garde le noeud de sortie du
 	 * prochain rail.
 	 */
 	private Noeud nextNode;
 	
 	/**
 	 * Noeud précédent...
 	 */
 	private Noeud prevNoeud;
 	
 	/**
 	 * Constructeur nécéssaire à GreenUML.
 	 * 
 	 * @param coordonnees Coordonnées initiale du chariot.
 	 * @param bagage Bagage du nouveau chariot.
 	 * @param vitesse Vitesse initiale du chariot.
 	 * @param chemin Liste des noeud avant la destination.
 	 * @param nextNode Prochain noeud.
 	 * @param prevNoeud Noeud précédent.
 	 */
 	public Chariot(int id, Point2D.Float coordonnees, Bagage bagage, float vitesse,
 			LinkedList<Noeud> chemin, Noeud nextNode, Noeud prevNoeud) {
 		/* TODO : enlever le bagage en parametre du constructeur, et mettre
 		 * bagage = null ici
 		 */
 		super();
 		this.id = id;
 		this.coordonnees = coordonnees;
 		this.bagage = bagage;
 		this.vitesse = vitesse;
 		this.chemin = chemin;
 		this.distanceDepuisNoeudDepart = 0;
 		this.nextNode = nextNode;
 		this.prevNoeud = prevNoeud;
 	}
 	
 	/**
 	 * Constructeur par défault.
 	 * 
 	 * @param id Id du chariot.
 	 */
 	public Chariot(int id) {
 		this.id = id;
 		this.coordonnees = new Point2D.Float();
 		this.bagage = null;
 		this.vitesse = 0;
 		this.chemin = new LinkedList<Noeud>();
 		this.distanceDepuisNoeudDepart = 0;
 		this.nextNode = null;
 		this.prevNoeud = null;
 	}
 	
 	public float getVitesse() {
 		return vitesse;
 	}
 
 	public void setVitesse(float vitesse) {
 		this.vitesse = vitesse;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * Retourne le noeud précédent.
 	 * 
 	 * @return Noeud précédent.
 	 */
 	public Noeud getPrevNoeud() {
 		return prevNoeud;
 	}
 
 	/**
 	 * Calcul la nouvelle distance sur le meme rail
 	 * après un top horloge.
 	 */
 	public float calculerNouvPos() {
 		return (float)(distanceDepuisNoeudDepart + vitesse*Aeroport.lapsTemps/1000);
 	}
 	
 	/**
 	 * Calcul les nouveau coordonnées en fonction
 	 * d'un rail et d'une distance sur ce rail.
 	 * 
 	 * @param entree Noeud d'entrée du rail.
 	 * @param vectUnitaire Vecteur unitaire du rail.
 	 * @param nouvDist Nouvelle distance du chariot sur le rail.
 	 */
 	public void majPos(Noeud entree, Point2D.Float vectUnitaire, float nouvDist) {
 		
 		
 		distanceDepuisNoeudDepart = nouvDist;
 		if(entree != null && vectUnitaire != null) {
 			coordonnees.x = entree.getCoordonnees().x + distanceDepuisNoeudDepart*vectUnitaire.x;
 			coordonnees.y = entree.getCoordonnees().y + distanceDepuisNoeudDepart*vectUnitaire.y;
 		}
 		else {
 			coordonnees.x = 0;
 			coordonnees.y = 0;
 		}
 	}
 
 	/**
 	 * Retourne le prochain rail dans le chemin.
 	 * 
 	 * @param noeudSortie Noeud duquel on doit trouver le suivant.
 	 * @return Prochain rails où se rendre dans le chemin, null si aucun.
 	 */
 	public Rail getProchainRail(Noeud n) {
 
 		List<Rail> l = n.getListeRails();
 		
 		for (Rail r : l) {
 			/*
 			System.out.println("On teste le rail n°" + r.getId());
 			System.out.println("Le noeud de sortie de ce rail est : " + r.getNoeudSortie().getId());
 			System.out.println("Mon chemin: ");
 			for(Noeud n0 : chemin) {
 				System.out.println("\t "+ n0.getId());
 			}
 			System.out.println("Chemin Peek : " + chemin.peek().getId());
 			*/
 			/*
 			 * On parle du meme objet ici, donc
 			 * on peut faire ce test, mais la logique voudrait
 			 * peut-être faire un equals.
 			 * 
 			 * Il faudrait peut-être commencer avec les LinkedList,
 			 * afin de pouvoir récupérer le premier élément plus facilement.
 			 */
 			if(r.getNoeudSortie().equals(chemin.peek())) {
 				//System.out.println("CHARIOT: On donne un truc");
 				chemin.peek();
 				return r;
 			}
 		}
 		//System.out.println("On ne trouve pas de chemin!");
 		
 		return null;
 	}
 	
 	/**
 	 * Retourne le noeud de sa fin de parcours.
 	 * 
 	 * @return Noeud de la fin de parcours.
 	 */
 	public Noeud getDestination() {
 		if(chemin.peekLast() == null) {
 			return nextNode;
 		}
 		else {
 			return chemin.peekLast();
 		}
 	}
 	
 	/**
 	 * Supprime et retourne le bagage du chariot.
 	 * Supprime le chemin.
 	 * 
 	 * @return L'ancien bagage du chariot.
 	 */
 	public Bagage viderChariot() {
 		Bagage b = bagage;
 		bagage = null;
 		chemin.clear();
 		return b;
 	}
 	
 	/**
 	 * Calcul le chemin le plus court d'un point à un autre
 	 * de l'aéroport. Rempli la liste des noeud du chemin.
 	 * 
 	 * @param depart Noeud de départ.
 	 * @param arrivee Noeud d'arrivée.
 	 */
 	public void calculerChemin(Noeud depart, Noeud arrivee) {
 		
 		if(depart == null || arrivee == null) {
 			chemin.clear();
 			nextNode = null;
 		}
 		else {
 			TreeMap<Noeud, DijStuff> graph = new TreeMap<Noeud, DijStuff>();
 			
 			graph.put(depart, new DijStuff(0, null));
 			Noeud courant = null;
 			while((courant = minNoeud(graph)) != null) {
 				graph.get(courant).visited = true;
 				
 				for(Rail r : courant.getListeRails()) {
 					r.getNoeudSortie();
 					if(!graph.containsKey(r.getNoeudSortie())) {
 						graph.put(r.getNoeudSortie(), new DijStuff());
 					}
 					float distance = graph.get(courant).dist + r.getLongueur();
 					if(graph.get(r.getNoeudSortie()).dist > distance) {
 						graph.get(r.getNoeudSortie()).dist = distance;
 						graph.get(r.getNoeudSortie()).pred = courant;
 					}
 				}
 			}
 			chemin.clear();
 			chemin.addFirst(arrivee);
 			DijStuff dijCourant = graph.get(arrivee);
 			if(dijCourant != null) {
 			
 				while(dijCourant.pred != depart) {
 					chemin.addFirst(dijCourant.pred);
 					dijCourant = graph.get(dijCourant.pred);
 					if(dijCourant == null) {
 						// Pas de solution
 						chemin.clear();
 						break;
 					}
 				}
 				
 			}
 			else {
 				chemin.clear();
 			}
 			//chemin.addFirst(depart);
 			nextNode = depart;
 		}
 		
 	}
 	
 	/**
 	 * Méthode privé pour Dijsktra qui rend le noeud accessible
 	 * avec la plus petite distance.
 	 * 
 	 * @param graph Graph de recherche
 	 * 
 	 * @return Le noeud le plus proche.
 	 */
 	private Noeud minNoeud(TreeMap<Noeud, DijStuff> graph) {
 		
 		float x = Integer.MAX_VALUE;
 		Noeud y = null;
 		for (Entry<Noeud, DijStuff> entree : graph.entrySet()) {
 			
 			if(!entree.getValue().visited && entree.getValue().dist < x) {
 				y = entree.getKey();
 				x = entree.getValue().dist;
 			}
 		}
 		return y;
 	}
 
 	/**
 	 * Mettre un bagage dans un chariot vide.
 	 * 
 	 * @param b Bagage à ajouter.
 	 */
 	public void mettreBagage(Noeud depart, Bagage b) {
 		bagage = b;
 		if(Aeroport.getMode() == Aeroport.Mode.AUTO) 
 			calculerChemin(depart, b.getTogobban().getNoeud());
 	}
 	
 	/**
 	 * Vérifie que le noeud peut-etre ajouté.
 	 * 
 	 * @param n Noeud à ajouter.
 	 * @return Possible ou pas.
 	 */
 	public boolean noeudElligible(Noeud n) {
 		if(chemin.isEmpty()) {
 			if(nextNode != null) {
 				for(Rail r : nextNode.getListeRails()) {
 					if(r.getNoeudSortie() == n) {
 						return true;
 					}
 				}
 				return false;
 			}
 			//return true;
 		}
 		else {
 			for(Rail r : chemin.getLast().listRailsSortie) {
 				if(r.getNoeudSortie() == n) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Ajoute un noeud au chemin.
 	 * 
 	 * @param n Noeud du chemin.
 	 */
 	public void ajouterNoeud(Noeud n) {
 		chemin.offerLast(n);
 	}
 	
 	/**
 	 * Retourne les coordonnées du Chariot.
 	 * 
 	 * @return Coordonnées actuelles du chariot.
 	 */
 	public Point2D.Float getCoordonnees() {
 		return coordonnees;
 	}
 	
 	/**
 	 * Distance sur le rail depuis le noeud de départ.
 	 * 
 	 * @return Distance depuis le noeud de départ.
 	 */
 	public float getDistance() {
 		return distanceDepuisNoeudDepart;
 	}
 	
 	/**
 	 * Retourne le prochain Noeud
 	 * 
 	 * @return Prochain noeud du chemin.
 	 */
 	public Noeud getProchainNoeud() {
 		return nextNode;
 	}
 	
 	/**
 	 * Retourne le bagage du chariot si présent.
 	 * 
 	 * @return Le bagage du chariot, null sinon.
 	 */
 	public Bagage getBagage() {
 		return bagage;
 	}
 	
 	/**
 	 * Permet de compléter un objet vide à partir du XML
 	 * 
 	 * @param chariotElement Element XML Chariot
 	 * @param aeroport Aeroport
 	 * @return Résultat du parsing
 	 */
 	public int construireAPartirDeXML(Element chariotElement, Aeroport aeroport)
 	{
 		// On récupère les coordonnées du noeud associé
         int idNoeudGarage = Integer.parseInt(chariotElement.getAttribute("noeudParDefaut"));
         
         this.coordonnees.setLocation(aeroport.getNoeud(idNoeudGarage).getCoordonnees());
         
         // On récupère la vitesse par défaut
         this.vitesse = Integer.parseInt(chariotElement.getAttribute("vitesseParDefaut"));;
         
         return Aeroport.PARSE_OK;
     }
 
 	/**
 	 * Supprime le premier (prochain) rail du chemin.
 	 * Complémentaire de getProchainRail.
 	 */
 	public void suppProchainRail() {
 		prevNoeud = nextNode;
 		nextNode = chemin.poll();
 	}
 
 	/**
 	 * Efface le chemin courant du chariot.
 	 */
 	public void effacerChemin() {
 		chemin.clear();
 	}
 }
 
 /**
  * Classe privée, pratique pour calculer Dijsktra.
  * 
  * @author H4402
  */
 class DijStuff {
 	
 	/**
 	 * Distance actuelle du noeud à l'arrivée.
 	 */
 	public float dist;
 	
 	/**
 	 * Noeud précédent dans le graphe.
 	 */
 	public Noeud pred;
 	
 	/**
 	 * Noeud déja visité ou non?
 	 */
 	public boolean visited;
 	
 	/**
 	 * Constructeur par défault.
 	 */
 	public DijStuff() {
 		dist = Integer.MAX_VALUE;
 		pred = null;
 		visited = false;
 	}
 	
 	/**
 	 * Améliorateur.
 	 * 
 	 * @param n Noeud précédent.
 	 */
 	public DijStuff(Noeud n) {
 		dist = Integer.MAX_VALUE;
 		pred = n;
 		visited = false;
 	}
 	
 	/**
 	 * Améliorateur.
 	 * 
 	 * @param dist Distance parcourue.
 	 * @param n  Noeud précédent.
 	 */
 	public DijStuff(int dist, Noeud n) {
 		this.dist = dist;
 		pred = n;
 		visited = false;
 	}
 	
 }

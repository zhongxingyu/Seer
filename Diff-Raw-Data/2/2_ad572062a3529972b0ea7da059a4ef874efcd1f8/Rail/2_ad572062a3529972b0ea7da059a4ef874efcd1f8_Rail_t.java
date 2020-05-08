 package noyau;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.w3c.dom.Element;
 
 /**
  * Classe représentant un rail dans l'application.
  * Un rail possèdent des chariots
  * qui roulent dessus, un noeud d'entrée
  * et un noeud de sortie.
  * 
  * @author H4402
  */
 public class Rail {
 	/**
 	 * Identifiant d'un rail
 	 */
 	private int id;
 	
 	/**
 	 * Distance de sécurité entre deux chariots
 	 */
 	public static int distSecu; 
 	
 	/**
 	 * Liste des chariots roulant actuellement
 	 * sur le rail, où étant bloqué.
 	 * Cette liste est gérée en FIFO.
 	 * 
 	 * @uml.property  name="listChariots"
 	 */
 	private LinkedList<Chariot> listChariots;
 	
 	/**
 	 * Noeud d'entrée du rail.
 	 * 
 	 * @uml.property  name="noeudEntree"
 	 */
 	private Noeud noeudEntree;
 	
 	/**
 	 * Noeud de sortie du rail.
 	 * 
 	 * @uml.property  name="noeudSortie"
 	 */
 	private Noeud noeudSortie;
 	
 	/**
 	 * Vecteur unitaire du rail.
 	 */
 	private Point2D.Float vectUnitaire;
 	
 	/**
 	 * Taille du rail.
 	 */
 	private float longueur;
 	
 	/**
 	 * Retourne la direction du rail.
 	 * (Recalculé à chaque appel)
 	 * 
 	 * @return Direction du vecteur.
 	 */
 	public Point getDirection() {
 		return new Point(noeudSortie.getCoordonnees().x - noeudEntree.getCoordonnees().x,
 				noeudSortie.getCoordonnees().y - noeudEntree.getCoordonnees().y);
 	}
 
 	/**
 	 * Constructeur pratique pour GreenUML.
 	 * 
 	 * @param listChariots Liste des chariots roulants
 	 * @param noeudEntree Noeud d'entrée du rail.
 	 * @param noeudSortie Noeud de sortie du rail.
 	 */
 	public Rail(LinkedList<Chariot> listChariots, Noeud noeudEntree, Noeud noeudSortie) {
 		super();
 		this.listChariots = listChariots;
 		this.noeudEntree = noeudEntree;
 		this.noeudSortie = noeudSortie;
 		Point direction = getDirection();
 		longueur = (float) Math.sqrt(direction.x*direction.x + direction.y*direction.y);
 		vectUnitaire = new Point2D.Float(direction.x/longueur, direction.y/longueur);
 	}
 	
 	/**
 	 * Constructeur par défaut
 	 * 
 	 */
 	public Rail() {
 		super();
 		this.listChariots = new LinkedList<Chariot>();
 		this.noeudEntree = null;
 		this.noeudSortie = null;
 		vectUnitaire = new Point2D.Float();
 		longueur = 0;
 	}
 	
 	/**
 	 * Ajoute les noeuds au rail et calcul le vecteur unitaire et la longueur.
 	 * 
 	 * @param noeudEntree Noeud d'entrée du rail.
 	 * @param noeudSortie Noeud de sortie du rail.
 	 */
 	public void ajouterNoeuds(Noeud noeudEntree, Noeud noeudSortie) {
 		this.noeudEntree = noeudEntree;
 		this.noeudSortie = noeudSortie;
 
 		Point direction = getDirection();
 		longueur = (float) Math.sqrt(direction.x*direction.x + direction.y*direction.y);
 		vectUnitaire = new Point2D.Float(direction.x/longueur, direction.y/longueur);
 	}
 
 	/**
 	 * Avancer les chariots du rail lors d'un top horloge.
 	 */
 	public void avancerChariots() {
 		Iterator<Chariot> it = listChariots.iterator();
 		Chariot prev = null;
 		
 		while ( it.hasNext() ) {
 			
 		    Chariot c = it.next();
 		    
 			float distChariot = c.calculerNouvPos();
 			/* 
 			 * Si il n'y a pas de chariot précédent, ou si on ne le dépasse pas.
 			 */
 			if(prev == null || distChariot+distSecu < prev.getDistance()) {
 				c.setFreine(false);
 				/*
 				 * Si le chariot est toujours dans le rail,
 				 * on l'avance.
 				 */
 				if(distChariot < longueur) {
 					c.majPos(noeudEntree, getVectUnitaire(), distChariot);
 				}
 				else {
 					c.majPos(noeudEntree, getVectUnitaire(), longueur);
 				
 					if(noeudSortie.equals(c.getDestination())) {
 						if(c.getDestination() instanceof NoeudGarage) {
 							if(c.getBagage() == null) {
 								((NoeudGarage)c.getDestination()).getGarage().ajouterChariotVide(c);
 								it.remove();
 								continue;
 							}
 						}
 						else if(c.getDestination() instanceof NoeudToboggan) {
 							if(c.getBagage() != null) {
 								if(c.getBagage().getTogobban().getNoeud().equals(c.getDestination())) {
 									((NoeudToboggan)c.getDestination()).getToboggan().ajouterBagage((c.viderChariot()));
 									if(Aeroport.getMode() == Aeroport.Mode.AUTO) {
 										
 										Noeud part = Aeroport.garage.getProchaineCommande();
 										if(part != null) {
 											c.calculerChemin(noeudSortie, part);
 										}
 										else {
 											c.calculerChemin(noeudSortie, Aeroport.garage.getNoeud());
 										}
 										
 										//c.calculerChemin(noeudSortie, Aeroport.garage.getNoeud());
 										
 									}
 									prev = c;
 									continue;
 								}
 							}
 							 
 						}
 						else if(c.getDestination() instanceof NoeudTapis) {
 							if(c.getBagage() == null) {
 								((NoeudTapis)c.getDestination()).avertirChariotPresent(c);
 								prev = c;
 								continue;
 							}
 						}
 					}
 					/*
 					else {
 						if(noeudSortie instanceof NoeudToboggan) {
 							if(c.getBagage() == null && Aeroport.getMode() == Aeroport.Mode.AUTO) {
 								c.calculerChemin(noeudSortie, Aeroport.garage.getNoeud());
 								
 								
 								//Noeud part = Aeroport.garage.getProchaineCommande();
 								//if(part != null) {
 								//	c.calculerChemin(noeudSortie, part);
 								//}
 								//else {
 								//	c.calculerChemin(noeudSortie, Aeroport.garage.getNoeud());
 								//}
 							}
 						}
 					}
 					*/
 					/*
 					 * Si on est la, le chariot continue son chemin,
 					 * on prend le prochain rail
 					 * si il existe et
 					 * on essai de le placer.
 					 */
 					Rail r = c.getProchainRail(noeudSortie);
 					if(r != null) {
 						if (r.ajoutChariot(c)) {
							if(noeudSortie instanceof NoeudTapis) 
								((NoeudTapis)noeudSortie).avertirChariotPlein();
 							c.suppProchainRail();
 							it.remove();
 						}
 					}
 					else {
 						if(noeudSortie.getListeRails().size() == 1) {
 							if (noeudSortie.getListeRails().get(0).ajoutChariot(c)) {
 								c.setProchainNoeud(noeudSortie.getListeRails().get(0).getNoeudSortie());
 								it.remove();
 							}
 						}
 					}
 				}
 			}
 			
 			else {
 				c.setFreine(true);
 				/*
 				 * Si on est la, c'est qu'on peut dépasser un chariot
 				 * On met alors notre chariot le plus près possible du précédent.
 				 */
 				if(prev.getDistance()-distSecu > 0) {
 					c.majPos(noeudEntree, getVectUnitaire(), prev.getDistance()-distSecu);
 				}
 			}
 			prev = c;
 		}
 	}
 
 	/**
 	 * Ajoute un chariot à un rail si c'est possible,
 	 * c'est à dire s'il n'y a pas de collisions.
 	 * 
 	 * @param c Chariot à ajouter.
 	 * @return Si l'opération a réussi ou non.
 	 */
 	public boolean ajoutChariot(Chariot c) {
 		
 		/*
 		 * Si il n'y a pas de bagage qui vient d'arriver sur le rail,
 		 * je l'ajoute.
 		 */
 		if((listChariots.size() > 0 && listChariots.getLast().getDistance() > distSecu)
 				|| listChariots.size() == 0) {
 			listChariots.addLast(c);
 			c.majPos(noeudEntree, getVectUnitaire(), 0);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Retourne la liste des chariots d'un rail.
 	 * 
 	 * @return Liste des chariots du rail.
 	 */
 	public LinkedList<Chariot> getListChariots() {
 		return listChariots;
 	}
 	
 	/**
 	 * Retourne le vecteur unitaire correspondant au rail.
 	 * @return Vecteur unitaire du rail.
 	 */
 	public Point2D.Float getVectUnitaire() {
 		return vectUnitaire;
 	}
 	
 	/**
 	 * Retourne le noeud de sortie d'un rail.
 	 * 
 	 * @return Noeud de sortie du rail.
 	 */
 	public Noeud getNoeudSortie() {
 		return noeudSortie;
 	}
 	
 	/**
 	 * Retourne le noeud d'entrée d'un rail.
 	 * 
 	 * @return Noeud d'entrée du rail.
 	 */
 	public Noeud getNoeudEntree() {
 		return noeudEntree;
 	}
 	
 	/**
 	 * Retourne la longueur du rail.
 	 * 
 	 * @return Longueur du rail.
 	 */
 	public float getLongueur() {
 		return longueur;
 	}
 	
 	/**
 	 * Retourne l'ID du rail.
 	 * 
 	 * @return Id du rail.
 	 */
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * Permet de compléter un objet vide à partir du XML
 	 * 
 	 * @param railElement Element XML Rail
 	 * @param aeroport Aeroport
 	 * @return Résultat du parsing
 	 */
 	public int construireAPartirDeXML(Element railElement, Aeroport aeroport)
 	{
 		//lecture des attributs
 		this.id = Integer.parseInt(railElement.getAttribute("id"));
 		
 		int idNoeudEntree = Integer.parseInt(railElement.getAttribute("noeudEntree"));
 		int idNoeudSortie = Integer.parseInt(railElement.getAttribute("noeudSortie"));
 		
 		this.ajouterNoeuds(aeroport.getNoeud(idNoeudEntree), aeroport.getNoeud(idNoeudSortie));
         
 		//il faut également le faire dans l'autre sens (référencer le rail dans le noeud
 		this.noeudEntree.ajouterRailDeSortie(this);
 		
 		return Aeroport.PARSE_OK;
     }
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Rail other = (Rail) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 }
 

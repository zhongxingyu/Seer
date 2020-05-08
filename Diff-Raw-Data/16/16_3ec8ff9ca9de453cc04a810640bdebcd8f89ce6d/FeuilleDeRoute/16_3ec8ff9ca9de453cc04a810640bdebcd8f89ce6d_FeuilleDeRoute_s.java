 package model;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import Exception.GraphException;
 
 import tsp.GraphLivraisons;
 import dijkstra.Dijkstra;
 
 public class FeuilleDeRoute
 {
 	protected
 	ArrayList<Schedule> timeZones;
 	ZoneGeo zoneGeo;
 	final int DUREE_LIVRAISON = 15;
 	EtatFDR etat = EtatFDR.INIT;
 	Delivery warehouseDelivery;
 	Schedule retourSch;
 	
 	
 	
 	public FeuilleDeRoute(ArrayList<Schedule> timeZones, ZoneGeo zoneGeo)
 	{
 		super();
 		this.timeZones = timeZones;
 		this.zoneGeo = zoneGeo;
 		
 		//ajout d'un schedule réservé au retour à l'entrepot
 		retourSch = new Schedule(0, 1440, Color.BLACK);
 		warehouseDelivery = new Delivery(zoneGeo.getWarehouse(), retourSch);
 		retourSch.appendDelivery(warehouseDelivery);
 		timeZones.add(retourSch);
 		
 	}
 	
 	public Delivery getWarehouse() {
 		return warehouseDelivery;
 	}
 
 	public ZoneGeo getZoneGeo() {
 		return zoneGeo;
 	}
 	
 	public ArrayList<Schedule> getTimeZones()
 	{
 		return timeZones;
 	}
 	
 	
 	/**
 	 * 
 	 * @return liste ordonnée de tous les noeuds (y compris ceux permetant de passer d'une livraison à l'autre)
 	 */
 	public ArrayList<Node> getFullPath()
 	{
 		ArrayList<Node> fullPath = new ArrayList<Node>();
 		for (Schedule s : timeZones)
 		{
 			for (Delivery d : s.getDeliveries())
 			{
 				 fullPath.addAll(d.getPathToDest().getTrajectory());
 				 fullPath.add(d.getDest());
 			}
 		}
 		return fullPath;
 	}
 	
 	public ArrayList<Delivery> getAllDeliveries()
 	{
 		ArrayList<Delivery> retour = new ArrayList<Delivery>();
 		for (Schedule sch : timeZones)
 		{
 			for (Delivery d : sch.getDeliveries())
 			{
 				retour.add(d);
 			}
 		}
 		return retour;
 	}
 	
 	/**
 	 * 	@param n node dont on veut le schedule
 	 * 	@return schedule contenant le node n, null si le node n'est acutellement pas un point de livraison
 	 */
 	public Delivery getDelivery(Node n) {
 		for (Delivery d : getAllDeliveries()) {
 			if (d.getDest().getID() == n.getID()) {
 				return d;
 			}
 		}
 		return null;
 	}
 
 	public EtatFDR getEtat()
 	{
 		return etat;
 	}
 
 	/**
 	 * (re)calcule la tournée optimale sur la base des données actuelles de la FeuilleDeRoute (schedules, deliveries).
 	 * @throws GraphException
 	 */
 	public void computeWithTSP() throws GraphException
 	{
 		GraphLivraisons gl = new GraphLivraisons(this);
 		gl.createGraph();
 		ArrayList<Delivery> result = gl.calcItineraire();
 		
 		// pour chaque Schedule, on cherche les deliveries concernées
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> newDelivs = new LinkedList<Delivery>();
 			for (Delivery d : result)
 			{
 				if (d.schedule == sch)
 				{
 					//on les insère, dans l'ordre, dans la nouvelle liste du Schedule
 					
 					newDelivs.add(d);
 					
 				}
 			}
 			sch.setDeliveries(newDelivs);
 		}
 		computeArrivalTimes();
 		etat = EtatFDR.OPTIM;
 	}
 
 	/**
 	 * calcule les HeurePrevue et RetardPrevu de toutes les delivery actuellement dans la FeuilleDeRoute
 	 */
 	protected void computeArrivalTimes()
 	{
 		int theTime = timeZones.get(0).getStartTime();
 		for (Schedule sch : timeZones)
 		{
 			//pause attente début livraison
 			if (theTime<sch.getStartTime())
 				theTime = sch.getStartTime();
 			
 			for (Delivery d : sch.getDeliveries())
 			{
 				theTime+= d.getPathToDest().getDuration();
 				d.setHeurePrevue(theTime);
 				d.setRetardPrevu(theTime > sch.getEndTime());	//si arrive hors schedule
 				theTime+= DUREE_LIVRAISON;
 			}
 		}
 	}
 	
 	public void generateReport(File path) throws IOException
 	{
 	    PrintWriter writer;
 	    int n = 5;
 
 	    writer =  new PrintWriter(new BufferedWriter(new FileWriter(path)));
 	   
 	    writer.println("Rapport de la feuille de route pour le livreur.\n");
 	    writer.println("La tournee est divisee en "+timeZones.size()+" creneaux.");
 	    for(Schedule s: timeZones)
 	    {
 		    writer.println("Le prochain creneau s'etend de" + Schedule.timeToString(s.startTime)+  " a "+Schedule.timeToString(s.endTime));
 		    writer.println("Dans ce creneau il y a "+s.getDeliveries().size()+ " livraisons a faire." );
 		    for(Delivery d : s.getDeliveries())
 		    {
 			    writer.println("La prochaine livraison aura lieu a "+d.getDest().getID()+" ." );
 			    writer.println("Arrivee prevue a  "+ Schedule.timeToString(d.getHeurePrevue()));
 			    Integer previous=d.getPathToDest().getTrajectory().get(0).getID();
 		    	for(Arc a :d.getPathToDest().getArcs())
 		    	{
 		    		writer.print("Passer par le point "+ a.getDest().getID());
 					writer.println(" en empruntant la rue "+a.getName()+".");
 		    	}
 			    writer.println("Quand vous serez arrive, vous disposez de 15 minutes pour livrer le colis au client.");
 		    }
 	    }
 	    writer.println("Toutes les livraisons ont ete faites, la tournee est finie.");
 	    writer.close();
 	}
 
 	// ajoute un node dans un schedule donné
 	// utile seulement à l'init, avant le premier appel de computeWithTSP
 	public void addNode(Node node, Schedule schedule) throws RuntimeException
 	{
 		if (etat != EtatFDR.INIT)
 		{
 			throw new RuntimeException("trying to FeuilleDeRoute.addNode() while already initialized");
 		}
 		Delivery deliv = new Delivery(node, schedule);
 		schedule.appendDelivery(deliv);
 	}
 	
 	/**
 	 * supprime la livraison concernant node si elle existe, et recalcule le chemin entre les noeuds englobants
 	 * @param node noeud a supprimer
 	 */
 	public void delNode(Node node)
 	{
 		Delivery deliv = getDelivery(node);
 		if (deliv != null)
 		{
 			if (etat == EtatFDR.INIT)
 			{
 				deliv.getSchedule().removeDelivery(deliv);
 			}
 			else	//already init
 			{
 				Delivery next = nextDelivery(deliv);
 				deliv.getSchedule().removeDelivery(deliv);
 				recalcPathTo(next);
 				computeArrivalTimes();
 			}
 		}
 
 	}
 	
 	/**
 	 * insère inserted avant reference, dans la meme plage horraire que celui-ci
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer
 	 */
 	public void insertNodeBefore(Node inserted, Delivery reference)
 	{
 		insertNode(inserted, reference, true);
 	}
 	
 	/**
 	 * insère inserted apres reference, dans la meme plage horraire que celui-ci
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer
 	 */
 	public void insertNodeAfter(Node inserted, Delivery reference)
 	{
 		insertNode(inserted, reference, false);
 	}
 	
 	
 	/**
 	 * @param inserted	noeud a inserer
 	 * @param reference	noeud vis-a-vis du quel on cherche à insérer inserted
 	 * @param before	insertion de inserted avant reference si true, après sinon
 	 */
 	protected void insertNode(Node inserted, Delivery reference, boolean before)
 	{
 		boolean refFound = false;
 		
 		//recherche du bon schedule
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(reference))	//si le schedule contient la livraison de reference
 			{
 				refFound = true;
 				
 			//insertion du noeud dans le schedule
 				Delivery newDelivery = new Delivery(inserted, sch);
 				int insertIndex;	//futur index du noeud inséré
 				if (before)
 				{
 					insertIndex = schDeliveries.indexOf(reference);
 				}
 				else
 				{
 					insertIndex = schDeliveries.indexOf(reference) +1;
 				}
 				sch.insert(newDelivery, insertIndex);
 				
 				
 				
 			//re-calcul du chemin précedent
 				recalcPathTo(newDelivery);
 			//re-calcul du chemin suivant
 				recalcPathTo(nextDelivery(newDelivery));
 				
 				computeArrivalTimes();
 				
 				etat = EtatFDR.MODIF;
 			}
 		}
 		if (!refFound)
 			throw new RuntimeException("FeuilleDeRoute.insertNode(): reference delivery not found");
 	}
 	
 	
 	/**
 	 * @param delivery
 	 * @return delivery precedant la delivery passée en parametre
 	 */
 	protected Delivery previousDelivery(Delivery delivery)
 	{
 		Delivery returned = null;
 		for (Schedule sch : timeZones)
 		{
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(delivery))	//si le schedule contient la livraison de reference
 			{
 				int idx = schDeliveries.indexOf(delivery);
 				if (idx == 0)	//si premier element du schedule
 				{
 					
 					//recherche du dernier node du schedule précédent non vide	
 					returned = prevNonemptySchedule(sch).getDeliveries().getLast();
 				}
 				else
 				{
 					returned = schDeliveries.get(idx-1);
 				}
 			}
 		}
 		if (returned == null)
 			throw new RuntimeException("FeuilleDeRoute.previousDelivery(): delivery not found");
 		
 		return returned;
 	}
 	
 	/**
 	 * @param delivery
 	 * @return delivery suivant la delivery passée en parametre
 	 */
 	protected Delivery nextDelivery(Delivery delivery)
 	{
 		int idxSch = -1;
 		Delivery returned = null;
 		for (Schedule sch : timeZones)
 		{
 			idxSch++;
 			LinkedList<Delivery> schDeliveries = sch.getDeliveries();
 
 			if (schDeliveries.contains(delivery))	//si le schedule contient la livraison de reference
 			{
 				int idx = schDeliveries.indexOf(delivery);
 				if (delivery == schDeliveries.getLast())	//si dernier element du schedule
 				{
 					//recherche du premier node du schedule suivant non vide
 					returned = nextNonemptySchedule(sch).getDeliveries().getFirst();	//dernier node du sch précédent
 				}
 				else
 				{
 					returned = schDeliveries.get(idx+1);
 				}
 			}
 		}
 		if (returned == null)
 			throw new RuntimeException("FeuilleDeRoute.nextDelivery(): delivery not found");
 		return returned;
 	}
 
 	
 	Schedule prevNonemptySchedule(Schedule sch)
 	{
 		int idx = timeZones.indexOf(sch);
 		Schedule prev = null;
 		if (idx == 0)
 		{
 			prev = retourSch;	//bouclage
 		}
 		else
 		{
 			prev = timeZones.get(idx-1);
 		}
 
 		
 		if (prev.getDeliveries().size() != 0)
 		{
 			return prev;
 		}
 		else
 		{
			return  prevNonemptySchedule(timeZones.get(idx-1));
 		}
 	}
 	
 	Schedule nextNonemptySchedule(Schedule sch)
 	{
 		int idx = timeZones.indexOf(sch);
 		Schedule next = null;
		if (idx == 0)
		{
			next = retourSch;	//bouclage
		}
		else
		{
			next = timeZones.get(idx+1);
		}
 
 		
 		if (next.getDeliveries().size() != 0)
 		{
 			return next;
 		}
 		else
 		{
			return  nextNonemptySchedule(timeZones.get(idx+1));
 		}
 	
 	}
 
 	/**
 	 * @param delivery delivery dont le PathToDest doit etre recalculé
 	 */
 	protected void recalcPathTo(Delivery delivery)
 	{
 		//recherche chemin
 		ArrayList<Node> singleton = new ArrayList<Node>();
 		singleton.add(delivery.getDest());
 		ArrayList<Chemin> result = Dijkstra.solve(zoneGeo, previousDelivery(delivery).getDest(), singleton);
 		delivery.pathToDest = result.get(0);
 		
 	}
 
 }

 package adp2.implementations;
 
 import adp2.interfaces.Ant;
 import adp2.interfaces.Graph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 	
 	
 
 public class AntImpl implements Ant {
 	private List<Integer> path; // der aktuelle Weg
 	private int weglaenge;	// die aktuelle Weglaenge
 	private Set<Integer> unvisitedNodes;	//alle Knoten des Graphen, bei dem Ameise noch nicht war
 	private double alpha; 
 	private Graph g;
 	boolean finished;
 	private int waitingTime=0;
 	
 	
 	private AntImpl(int startNode, double alpha, Graph g){
 		path = new ArrayList<Integer>();
 		weglaenge = 0;
 		path.add(startNode);
 		this.g = g;
 		unvisitedNodes = g.allNodes();
 		this.alpha = alpha;
 		finished = false;
 	}
 	
 	public static Ant valueOf(int startNode, double alpha, Graph g){
 		if (g == null || g instanceof NaG) {
 			return Values.NaA();
 		}
 		return new AntImpl(startNode, alpha, g);
 	}
 	
 	
 	@Override
	public List<Integer> traveledPath() {
		return this.path;
 	}
 	
 	public boolean hasFinished(){
 		return finished;
 	}
 
 	@Override
 	public int position() {
 		return this.path.get(path.size()-1);
 	}
 	
 	
 	private Set<Integer> unvisitedNeighbors(){
 		Set<Integer> neighbors = g.neighbors(position());
 		neighbors.retainAll(unvisitedNodes);
 		return neighbors;
 	}
 	
 	
 	/*bildet zur aktuellen Position alle unbesuchten Nachbarn dieses Knotens auf einen Balance-Wert ab
 	 * 
 	 * Formel zur Berechnung der Balance: [alpha * (Pheromon/maxPheromon) + [(1- alpha) * (Distanz/maxDistanz)]
 	 * 			dabei geben Pheromon und Distanz die jeweiligen Werte der aktuellen Kante zurueck
 	 * 			die MaxWerte die maximalen Werte aller inzidenten Kanten des aktuellen Knotens
 	 */
 	private Map<Integer, Double> balances(){
 		Map<Integer,Double> result = new HashMap<Integer,Double>();
 		//suche maximale Entfernung zu allen Nachbarn + 
 		//suche maximalen Pheromongehalt auf den Kanten zu allen Nachbarn
 		double maxDist = -1.0;
 		double maxPher = -1.0;
 		for(Integer elem : this.unvisitedNeighbors()){
 			if(g.distance(position(), elem) > maxDist){
 				maxDist = g.distance(position(), elem);
 			}
 			if(g.intensity(position(), elem) > maxPher){
 				maxPher = g.intensity(position(), elem);
 			}
 		}
 		
 		// berechne die balances und speichere sie in der Map
 		for(Integer elem : this.unvisitedNeighbors()){
 			double balance = alpha * (g.intensity(position(), elem) / maxPher);
 			balance += (1- alpha) * (g.distance(position(), elem) / maxDist);
 			result.put(elem,balance);
 		}
 		return result;
 		
 		
 	}
 	
 	private double sumOfValues(Map<?,Double> m){
 		double summe = 0;
 		for(Double elem : m.values()){
 			summe += elem;
 		}
 		return summe;
 	}
 
 	@Override
 	public void step() {
 		if(finished) System.out.println("Tote Ameise, kann nicht laufen");
 		if(waitingTime>0) waitingTime--;
 		else if (!finished){
 			if(unvisitedNodes.isEmpty()){
 				unvisitedNodes.add(path.get(0));
 			}
 			
 			
 			 
 			// berechnung der balances
 			Map<Integer,Double> probability = balances();		
 			
 			
 			/*
 			 * bildet die Summe aus allen Balancewerten ==> aus den einzelnen Werten und der Summe wird im 
 			 * naechsten Schritt die Wahrscheinlichkeit gebildet
 			 */
 			double summe = sumOfValues(probability);
 			
 			
 			
 			/*bildet die Nachbarn auf einen Wert zwischen 0 und 1 ab
 			 * Wert = eigene Wahrscheinlichkeit + vorher berechneter Wert
 			 * Bsp:
 			 * 
 			 * Node			Wahrscheinlichkeit		Wert
 			 * 1			0.3						0.3
 			 * 2			0.1						0.4
 			 * 3			0.5						0.9
 			 * 4			0.1						1.0
 			 */
 			double vorgaenger = 0;
 			
 			for(Map.Entry<Integer,Double> entry : probability.entrySet()){
 				double wahrscheinlichkeit = entry.getValue()/summe + vorgaenger;
 				if(wahrscheinlichkeit > 1.0) wahrscheinlichkeit = 1;
 				probability.put(entry.getKey(), wahrscheinlichkeit);
 				vorgaenger = wahrscheinlichkeit;
 			}
 			
 			
 			/*
 			 * Integriert die zufaellig Auswahl des Weges der Ameise durch verrechnen eines
 			 * Random Wertes (0.0 - 1.0) mit der vorher errechneten Wahrscheinlichkeit
 			 * ==> Wahl des naechsten Punktes auf dem Weg 
 			 * 
 			 * 
 			 * minNode evtl auf -1 setzen ==> fehlerbehandlung danach moeglich
 			 */
 			double wert = Math.random();
 			int minNode = 1;
 			double minValue = 1;
 			for(Map.Entry<Integer, Double> e : probability.entrySet()){
 				if(e.getValue() >= wert && e.getValue() <= minValue){
 					minNode = e.getKey();
 					minValue = e.getValue();
 				}
 			}
 			
 			
 			weglaenge += g.minDist(position(), minNode);
 			for(Integer elem : g.pointsBetween(position(),minNode)){
 				path.add(elem);
 				unvisitedNodes.remove(elem);
 			}
 			path.add(minNode);
 			unvisitedNodes.remove(minNode);
 			
 			if(unvisitedNodes.isEmpty() && path.get(path.size() - 1) == path.get(0)){
 				finished = true;
 			}
 			
 			waitingTime=g.distance(path.get(path.size()-2), path.get(path.size()-1));
 		}
 
 		
 	}
 
 	@Override
 	public int weglaenge() {
 		return this.weglaenge;
 	}
 
 		
 	public double alpha(){
 		return this.alpha;
 	}
 	
 	public int getWaitingTime(){
 		return this.waitingTime;
 	}
 	
 	
 	
 
 }

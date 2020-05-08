 package Markov;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 
 public class UnknownNode<T> extends Node<T>{
 	
 	// Number of 'votes' that this node is to be some particular value
 	// HashMap<KnownMarkovNode<T>, Float> isNode = new HashMap<KnownMarkovNode<T>, Float>();
 	
 	HashMap<Coordinate, KnownNode<T>> knownNeighbors = new HashMap<Coordinate, KnownNode<T>>();
 	HashMap<Coordinate, UnknownNode<T>> unkNeighbors = new HashMap<Coordinate, UnknownNode<T>>();	
 	
 	//HashMap<UnknownMarkovNode<T>, MarkovCoordinate> unkNeighbors = new HashMap<UnknownMarkovNode<T>, MarkovCoordinate>();
 	int currentIteration = 0;
 		
 	HashMap<KnownNode<T>, Float> baseVotes;
 	double baseTotal = 0.0;
 	
 	HashMap<KnownNode<T>, Float> votes = new HashMap<KnownNode<T>, Float>();
 	double totalVotes = 0.0;
 	
 	KnownNode<T> currentIdentity;
 	double currentWeight;
 	
 	int id;
 	static int MAX_ID = 0;
 
 	Coordinate unkCoordinate; 
 	
 	public UnknownNode(Coordinate coord){
 		id = MAX_ID++;
 		unkCoordinate = coord;
 	}
 	
 	public String toString(){
 		return "?<"+id+">"; 
 	}
 		
 	public void addSuggestions(HashMap<KnownNode<T>, Float> possibilities, Coordinate dist, float weight){
 //		System.out.println("Distance from suggesstee is " + unkCoordinate + " , " + dist + " : " + unkCoordinate.distance(dist));
 		//System.out.println("Distance from suggesstee is " + dist.distance() + " : " + dist);
//		float weightShift = (float) (1/ dist.distance());
 		//System.out.println("Distance Weight is " + weightShift);
//		weight = (float) weight * weightShift;
 		//System.out.println("Weight is " + weight);
 		
 		double possibilitiesSum = 0.0;
 		for(Float f : possibilities.values()){
 			possibilitiesSum += f;
 		}
 		
 		for(Entry<KnownNode<T>, Float> possibleEntry : possibilities.entrySet() ){
 			float possibilityWeight = (float) ((possibleEntry.getValue() / possibilitiesSum ) * weight);
 			//System.out.println("Vote for possibility " + possibleEntry.getKey() + " is " + possibilityWeight);
 			totalVotes += possibilityWeight;
 			if(votes.containsKey(possibleEntry.getKey())){	// Add to existing votes
 				votes.put(possibleEntry.getKey(), votes.get(possibleEntry.getKey()) + possibilityWeight );
 			}else{	// Cast the first vote
 				votes.put(possibleEntry.getKey(), possibilityWeight );
 			}
 		}
 	}
 	
 	public boolean decideIdentity(){
 		float maxWeight = 0.0f;
 		KnownNode<T> maxIdentity = null;
 		// Find the possibility with the highest weight
 		for(Entry<KnownNode<T>, Float> possibility : votes.entrySet() ){
 			if(possibility.getValue() > maxWeight){
 				maxWeight = possibility.getValue();
 				maxIdentity = possibility.getKey();
 			}
 		}
 		
 		boolean changed = currentIdentity != maxIdentity || Math.abs(currentWeight - (maxWeight / totalVotes)) > 0.001;
 		
 		currentIdentity = maxIdentity;
 		currentWeight = maxWeight / totalVotes;
 		
		System.out.println(this +" decided to be " + currentIdentity + " with probability " + currentWeight);
 		
 		// Reset votes to get ready for next iteration
 		votes = new HashMap<KnownNode<T>, Float>(baseVotes);
 		totalVotes = baseTotal;
 		
 		return changed;
 	}
 	
 	public void notifyNeighbors(){
 		// Given our current tentative identity, what do we think each of our unknown neighbors should be?
 		if(currentIdentity != null){
 			for(Entry<Coordinate, UnknownNode<T>> uNeighbor : unkNeighbors.entrySet() ){
 				if(currentIdentity.atOffset.containsKey(uNeighbor.getKey())){
 					uNeighbor.getValue().addSuggestions(currentIdentity.atOffset.get(uNeighbor.getKey()), uNeighbor.getKey(), (float) currentWeight);
 				}
 			}
 		}
 	}
 	
 	
 	public void initializeIdentity(){
 		System.out.println("known neighbors are " + knownNeighbors.size() + " unk " + unkNeighbors.size());
 		// What does the known neighbors think this node should be?
 		for(Entry<Coordinate, KnownNode<T>> kNeighbor : knownNeighbors.entrySet() )
 		{
 			Coordinate dBack = new Coordinate(distanceBack(kNeighbor.getKey().coords));
 			HashMap<KnownNode<T>, Float> kSuggestion =kNeighbor.getValue().atOffset.get( dBack );
 			//System.out.println(""+kNeighbor.getValue() + " suggestion at " + dBack + " is " + kSuggestion);
 			if(kSuggestion != null)
 				addSuggestions(kSuggestion, dBack, 1.0f  );
 		}		
 		
 		baseVotes = new HashMap<KnownNode<T>, Float>(votes);
 		baseTotal = totalVotes;
 	}
 	
 	public void addConnection(Node<T> node, float weight, int... distance){
 		if(node instanceof KnownNode){
 			knownNeighbors.put(new Coordinate(distance), (KnownNode<T>)node);
 		}else{
 			unkNeighbors.put(new Coordinate(distance), (UnknownNode<T>) node);
 		}
 	}
 	
 	public T getValue(int... coords){
 		if(currentIdentity != null){
 			return currentIdentity.getValue(coords);
 		}
 		else{
 			return null;
 		}
 	}
 
 }

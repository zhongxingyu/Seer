 package aeroport.sgbag.kernel;
 
 import java.util.*;
 import lombok.*;
 import lombok.extern.log4j.*;
 
 @NoArgsConstructor
 @ToString(exclude={"maxMoveDistance","halfLength", "cheminPrevu"})
 @Log4j
 public class Chariot {
 
 	@Getter
 	@Setter
 	private int maxMoveDistance;
 
	private int halfLength;
 
 	@Getter
 	@Setter
 	private int position;
 
 	@Getter
 	@Setter
 	private Noeud destination;
 
 	@Getter
 	@Setter
 	private Bagage bagage;
 	
 	@Getter
 	@Setter
 	private ElementCircuit parent;
 
 	@Getter
 	@Setter
 	private LinkedList<ElementCircuit> cheminPrevu;
 
 	public Chariot(int maxMoveDistance,
 			Noeud destination, LinkedList<ElementCircuit> cheminPrevu) {
 		this(maxMoveDistance, 50, maxMoveDistance, destination, null,
 				cheminPrevu);
 	}
 
 	public Chariot(int maxMoveDistance, int length,
 			int position, Noeud destination, Bagage bagage,
 			LinkedList<ElementCircuit> cheminPrevu) {
 		super();
 		this.maxMoveDistance = maxMoveDistance;
 		this.halfLength = length;
 		this.position = position;
 		this.destination = destination;
 		this.bagage = bagage;
 		this.cheminPrevu = cheminPrevu;
 		log.debug("Création du chariot " + this);
 	}
 
 	public Rail getNextRail() {
 		log.trace("recherche du prochain rail " + cheminPrevu);
 		if(cheminPrevu == null 
 				|| cheminPrevu.size() == 0) {
 			
 			// Le chariot n'a pas de chemin : on recalcule un chemin.
 			
 			// TODO amener chariot vers tapis roulant
 			
 			return null;
 		}
 		
 		ElementCircuit nextElemC = cheminPrevu.pop();
 
 		if (nextElemC instanceof Rail)
 			return (Rail) nextElemC;
 
 		return getNextRail();
 	}
 
 	public Boolean isEmpty() {
 		return (bagage == null);
 	}
 
 	public Boolean moveBagageToFile(FileBagage file) {
 		file.addBagage(bagage);
 		return removeBagage();
 	}
 
 	public Boolean removeBagage() {
 		bagage = null;
 		
 		log.debug("Vidage du chariot " + this);
 
 		return this.isEmpty();
 	}
 
 	public Boolean isColliding(Chariot chariotSuivant) {
 		if (position + halfLength > chariotSuivant.position
 				- chariotSuivant.halfLength)
 			return true;
 
 		return false;
 	}
 
 	public Boolean willCollide(int newPosition, Chariot chariotSuivant) {
 		if (newPosition + halfLength > chariotSuivant.position
 				- chariotSuivant.halfLength)
 			return true;
 
 		return false;
 	}
 
 	public int getLength() {
		return halfLength * 2;
 	}
 	
 	public void setLength(int len){
		halfLength = len/2;
 	}
 	
 	public int getRearPosition(){
		return (position - halfLength);
 	}
 	
 	public boolean hasBagage() {
 		return bagage != null;
 	}
 }

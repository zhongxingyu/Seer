 package roborally.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Raw;
 import roborally.basics.*;
 import roborally.utils.BatteryComparator;
 import roborally.utils.Calculator;
 import roborally.utils.PositionPair;
 import roborally.utils.PositionPairComparatorDistance;
 import roborally.utils.PositionPairComparatorEnergy;
 
 /**
  * Een klasse om robots voor te stellen.
  * 
  * @invar	De hoeveelheid energie van een batterij moet altijd geldig zijn.
  * 			|isValidRobotEnergyAmount(getEnergy())
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 2.1
  */
 public class Robot extends Entity{
 
 	/**
 	 * De energiekost van 1 move (geen rekening houdend met bezittingen).
 	 */
 	private final static Energy MOVE_COST = new Energy(500);
 	/**
 	 * De energiekost van 1 draai.
 	 */
 	public final static Energy TURN_COST = new Energy(100);
 	/**
 	 * De energiekost die bij een move bijkomt per kg gewicht in bezittingen.
 	 */
 	private final static Energy MOVE_COST_PER_KG = new Energy(50);
 	/**
 	 * De minimale energie van een robot.
 	 */
 	public final static Energy MINENERGY = new Energy(0);
 	/**
 	 * De maximale energie van een robot.
 	 */
 	public final static Energy MAXENERGY = new Energy(20000);
 	/**
 	 * De kost van 1 schot met zijn laser.
 	 */
 	public final static Energy SHOOT_COST = new Energy(1000);
 	/**
 	 * De energie van de robot.
 	 */
 	private final Energy energy;
 	/**
 	 * De orintatie van de robot.
 	 */
 	private Orientation orientation;
 
 	/**
 	 * De set van batterijen die de robot bezit.
 	 */
 	private SortedSet<Battery> Possessions = new TreeSet<Battery>(new BatteryComparator());
 
 	/**
 	 * Deze methode maakt een nieuwe robot aan.
 	 * 
 	 * @param	orientation
 	 * 			De initile orintatie van de robot.
 	 * 
 	 * @param	energy
 	 * 			De initile orintatie van de robot.
 	 * 
 	 * @post	De energie van de robot is gelijk aan de opgegeven energie.
 	 * 			|new.getEnergy().equals(energy)
 	 * 
 	 * @post	De orintatie van de robot is gelijk aan de opgegeven orintatie.
 	 * 			|new.getOrientation().equals(orientation)
 	 */
 	public Robot(Orientation orientation, Energy energy){
 		this.energy = energy;
 		setOrientation(orientation);
 	}
 
 	/**
 	 * Methode om de orintatie van een robot te wijzigen.
 	 *  
 	 * @param 	or
 	 * 			De orintatie die de robot moet krijgen.
 	 * 
 	 * @post	De orintatie van de robot is gelijk aan de gegeven parameter.
 	 * 			|new.getOrientation() == or
 	 */
 	private void setOrientation(Orientation or) {
 		this.orientation = or;
 	}
 
 	/**
 	 * Methode om de orintatie van de robot te verkrijgen.
 	 * 
 	 * @return 	De orintatie van de robot.
 	 * 			|this.orientation 			
 	 */
 	@Basic
 	public Orientation getOrientation(){
 		return this.orientation;
 	}
 
 	/**
 	 * Methode om de energie van de robot te verkrijgen.
 	 * 
 	 * @return 	De energie van de robot.
 	 * 			|this.energy
 	 */
 	@Basic
 	public Energy getEnergy(){
 		return this.energy;
 	}
 
 	/**
 	 * Methode die controleert of de opgegeven hoeveelheid energie een geldige hoeveelheid is.
 	 * 
 	 * @param 	energy
 	 * 			De hoeveelheid energie.
 	 *
 	 * @return	Boolean die weergeeft of de hoeveelheid energie geldig is.
 	 * 			|(energy.getEnergy() >= MINENERGY.getEnergy() && energy.getEnergy() <= MAXENERGY.getEnergy())
 	 */
 	public static boolean isValidRobotEnergyAmount(Energy energy){
 		return (energy.getEnergy() >= MINENERGY.getEnergy() && energy.getEnergy() <= MAXENERGY.getEnergy());
 	}
 
 	/**
 	 * Deze methode berekent de verhouding tussen de huidige hoeveelheid energie en de maximale hoeveelheid energie.
 	 * 
 	 * @return	De verhouding tussen de huidige hoeveelheid energie en de maximale hoeveelheid energie.
 	 * 			|this.getEnergy().getEnergy()/MAXENERGY.getEnergy()
 	 */
 	public double getEnergyFraction(){
 		return this.getEnergy().getEnergy()/MAXENERGY.getEnergy();
 	}
 
 	/**
 	 * Draait de robot 1 keer in wijzerzin.
 	 * 
 	 * @post	De nieuwe orintatie van de robot is gelijk aan de volgende orintatie in wijzerzin.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy())))
 	 * 			|	new.getOrientation() == this.getOrienation().getClockwiseOrientation()
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy())))
 	 * 			|	new.getEnergy().getEnergy().equals(this.getEnergy().getEnergy() - TURN_COST.getEnergy())
 	 */
 	public void turnClockWise(){
 		if(this.canTurn()){
 			this.setOrientation(this.getOrientation().getClockwiseOrientation());
 			this.getEnergy().setEnergy(this.getEnergy().getEnergy() - TURN_COST.getEnergy());
 		}
 	}
 
 	/**
 	 * Draait de robot 1 keer in tegenwijzerzin.
 	 * 
 	 * @post	De nieuwe orintatie van de robot is gelijk aan de volgende orintatie in wijzerzin.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy())))
 	 * 			|	new.getOrientation() == this.getOrienation().getCounterClockwiseOrientation()
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy())))
 	 * 			|	new.getEnergy().getEnergy().equals(this.getEnergy().getEnergy() - TURN_COST.getEnergy())
 	 */
 	public void turnCounterClockWise(){
 		if(this.canTurn()){
 			this.setOrientation(this.getOrientation().getCounterClockwiseOrientation());
 			this.getEnergy().setEnergy(this.getEnergy().getEnergy() - TURN_COST.getEnergy());
 		}
 	}
 
 	/**
 	 * Deze methode beweegt de robot een stap vooruit indien mogelijk.
 	 * 
 	 * @throws 	IllegalStateException
 	 * 			De robot heeft onvoldoende energie om te bewegen.
 	 * 			|!this.canMove() || !Position.isValidPosition(this.getOrientation().getNextPosition(this.getPosition()))
 	 * 
 	 * @effect	De positie van de robot is veranderd (indien het mogelijk was om te bewegen).
 	 */
 	public void move() throws IllegalStateException{
 		if(!this.canMove()){
 			throw new IllegalStateException("De robot heeft onvoldoende energie om te bewegen.");
 		}
 		Position destination;
 		try{
 			destination = Calculator.getNextPosition(this.getPosition(), this.getOrientation());
 		}catch (IllegalStateException e){
 			throw new IllegalStateException("De positie waarnaar bewogen moet worden is ongeldig.");
 		}
 		if(this.getBoard().isPlacableOnPosition(destination)){
 		this.setPosition(destination);
 		this.getEnergy().setEnergy(this.getEnergy().getEnergy() - moveCost(this).getEnergy());
 		}else{
 			throw new IllegalStateException("De positie is al bezet.");
 		}
 	}
 
 	/**
 	 * Deze methode geeft de energie terug die nodig is om een bepaalde plaats te bereiken.
 	 * 
 	 * @param 	position
 	 * 			De plaats die bereikt moet worden.
 	 * 
 	 * @return	De energie die nodig is om de plaats te bereiken.
 	 */
 	public Energy getEnergyRequiredToReach(Position position){
 		HashMap<String, Node> resultpad = Calculator.aStarOnTo(this, position);
 		Node n = resultpad.get(position.toString());
 		return n.getGCost();
 	}
 
 	/**
 	 * Deze methode verplaatst de robot zo dicht mogelijk bij een andere robot.
 	 * 
 	 * @param	robot
 	 * 			De robot waar naartoe moet bewogen worden.
 	 */
 	public void moveNextTo(Robot robot){
 		HashMap<String,Node> thisReachables = Calculator.getReachables(this);
 		HashMap<String,Node> otherReachables = Calculator.getReachables(robot);
 		Set<String> thisKeys = thisReachables.keySet();
 		Set<String> otherKeys = otherReachables.keySet();
 		ArrayList<PositionPair> posPairs = new ArrayList<>();
 		for(String thisPosString: thisKeys){
 			Position thisPos = Calculator.getPositionFromString(thisPosString);
 			for(String otherPosString: otherKeys){
 				Position otherPos = Calculator.getPositionFromString(otherPosString);
 				Node thisNode = thisReachables.get(thisPos.toString());
 				Node otherNode = otherReachables.get(otherPos.toString());
 				PositionPair toAdd = new PositionPair(thisPos, otherPos,thisNode.getGCost(),
 						otherNode.getGCost(), thisNode.getOrientation(),otherNode.getOrientation());
 				posPairs.add(toAdd);
 			}
 		}
 		ArrayList<PositionPair> semivalidPosPairs = new ArrayList<PositionPair>();
 		for(PositionPair pp: posPairs){
 			if((pp.getManhattanDistance() != 0) && !(pp.getPos1().toString().equals(pp.getPos2().toString())))
 				semivalidPosPairs.add(pp);
 		}
 		Collections.sort(semivalidPosPairs, new PositionPairComparatorDistance());
 		PositionPair firstpp = semivalidPosPairs.get(0);
 		ArrayList<PositionPair> validPosPairs = new ArrayList<PositionPair>();
 		for(PositionPair pp: semivalidPosPairs){
 			if((pp.getManhattanDistance() == firstpp.getManhattanDistance()))
 				validPosPairs.add(pp);
 		}
 		Collections.sort(validPosPairs, new PositionPairComparatorEnergy());
 		PositionPair thePair = validPosPairs.get(0);
 		this.setPosition(thePair.getPos1());
 		robot.setPosition(thePair.getPos2());
 		this.setOrientation(thePair.getOr1());
 		robot.setOrientation(thePair.getOr2());
 		this.getEnergy().setEnergy(Energy.energyDifference(this.getEnergy(),thePair.getCost1()).getEnergy());
 		robot.getEnergy().setEnergy(Energy.energyDifference(robot.getEnergy(),thePair.getCost2()).getEnergy());		
 	}
 
 	/**
 	 * Deze methode doet een robot schieten met zijn laser.
 	 * 
 	 * @effect	Mogelijks wordt een object op het bord geraakt en verwijderd.
 	 * 
 	 * @throws	IllegalStateException
 	 * 			De robot staat niet op een bord.
 	 * 			|!this.isOnBoard()
 	 * 
 	 * @post	De robot verliest energie. De hoeveelheid is bepaald in de constante SHOOT_COST.
 	 * 			|new.getEnergy() == new Energy(this.getEnergy().getEnergy() - SHOOT_COST.getEnergy())
 	 */
 	public void shoot() throws IllegalStateException{
 		if(this.isOnBoard()){	
 			Position beginpos = this.getPosition();
 			Orientation beginor = this.getOrientation();
 			boolean found = false;
 			while(this.getBoard().isValidBoardPosition(Calculator.getNextPosition(beginpos, beginor)) && !found){
 				beginpos = Calculator.getNextPosition(beginpos, beginor);
 				HashSet<Entity> content = this.getBoard().getEntityOnPosition(beginpos);
 				if(content != null){
 					Random rndm = new Random();
 					((Entity) content.toArray()[rndm.nextInt(content.toArray().length)]).destroy();
 					found = true;
 				}
 			}
 			this.getEnergy().setEnergy(this.getEnergy().getEnergy() - SHOOT_COST.getEnergy());
 		}else{
 			throw new IllegalStateException("De robot staat niet op een bord.");
 		}
 	}
 
 	/**
 	 * Deze methode laadt een robot op met de opgegeven hoeveelheid energie.
 	 * 
 	 * @param 	energy
 	 * 			Energie waarmee moet opgeladen worden.
 	 * 
 	 * @post	De robot is opgeladen met de opgegeven hoeveelheid energie.
 	 * 			|if(!isValidRobotEnergyAmount(Energy.energySum(this.getEnergy(), energy).getEnergy())){
 	 * 			|	new.getEnergy() == new Energy(MAXENERGY)
 	 * 			|}else{
 	 * 			|	new.getEnergy() == Energy.energySum(this.getEnergy(), energy)
 	 * 			|}
 	 * 			
 	 */
 	public void recharge(Energy energy){
 		Energy newEnergy = Energy.energySum(this.getEnergy(), energy);
 		if(!isValidRobotEnergyAmount(newEnergy))
 			newEnergy = MAXENERGY;
 		this.getEnergy().setEnergy(newEnergy.getEnergy());
 	}
 
 	/**
 	 * Deze methode berekent de kost van 1 move.
 	 * 
 	 * @param	robot
 	 * 			De robot waarvoor de berekening moet uitgevoerd worden.
 	 * 
 	 * @return	de kost terug van 1 move
 	 * 			|new Energy(MOVE_COST.getEnergy() + MOVE_COST_PER_KG.getEnergy()*(robot.getTotalWeight().getWeight() / 1000))
 	 */
 	public static Energy moveCost(Robot robot){
 		return new Energy(MOVE_COST.getEnergy() + MOVE_COST_PER_KG.getEnergy()*(robot.getTotalWeight().getWeight() / 1000));
 	}
 
 	/**
 	 * Geeft het totale gewicht van alles wat de robot draagt.
 	 * 
 	 * @return	Het totale gewicht van alles wat de robot draagt.
 	 */
 	public Weight getTotalWeight(){
 		int totalWeight = 0;
 		Iterator<Battery> itr = this.getPossessions().iterator();
 		while(itr.hasNext())
 			totalWeight += itr.next().getWeight().getWeight();
 		return new Weight(totalWeight);
 	}
 
 	/**
 	 * Geeft een set terug van alle batterijen die de robot momenteel draagt.
 	 * 
 	 * @return	De set van batterijen
 	 * 			|this.Possessions
 	 */
 	public Set<Battery> getPossessions(){
 		return this.Possessions;
 	}
 
 	/**
 	 * Deze methode neemt een batterij op in de bezittingen van de robot.
 	 * 
 	 * @param 	battery
 	 * 			De batterij die moet opgenomen worden.
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			De batterij staat niet op dezelfde plaats als de robot of niet op hetzelfde bord.
 	 * 			|!this.getBoard().equals(battery.getBoard()) || !this.getPosition().equals(battery.getPosition())
 	 * 
 	 * @throws 	IllegalStateException
 	 * 			De robot staat niet op een bord.
 	 * 			|!this.isOnBoard()
 	 * 
 	 * @post	De batterij zit in de bezittingen van de robot.
 	 * 			|new.getPossessions().contains(battery)
 	 * 
 	 * @post	De batterij staat niet meer op een bord.
 	 * 			|!battery.isOnBoard()
 	 */
 	public void pickUp(Battery battery) throws IllegalArgumentException, IllegalStateException{
 		if(!this.isOnBoard()){
 			throw new IllegalStateException("De robot staat niet op een bord.");
 		}else if(!this.getBoard().equals(battery.getBoard())){
 			throw new IllegalArgumentException("De robot staat niet op hetzelfde bord als de batterij.");
		}else if(!this.getPosition().equals(battery.getPosition())){
 			throw new IllegalArgumentException("De robot staat niet op dezelfde positie als de batterij.");
 		}else{
 			battery.removeFromBoard();
 			this.getPossessions().add(battery);
 		}
 	}
 
 	/**
 	 * Deze methode laadt de robot op met de energie in een batterij die hij bij zich heeft.
 	 * 
 	 * @param	battery
 	 * 			De batterij die gebruikt moet worden.
 	 * 
 	 * @post	//TODO
 	 */
 	public void use(Battery battery) {
 		if (this.getPossessions().contains(battery)){
 			if(battery.isDestroyed()){
 				this.getPossessions().remove(battery);
 			}else{
 				Energy toRecharge = Energy.energyDifference(battery.getEnergy(), Energy.energyDifference(Robot.MAXENERGY, this.getEnergy()));
 				battery.getEnergy().setEnergy(toRecharge.getEnergy());
 				this.getEnergy().setEnergy(Energy.energySum(this.getEnergy(), toRecharge).getEnergy());
 				if(battery.getEnergy().getEnergy() == 0){
 					this.getPossessions().remove(battery);
 					battery.destroy();
 				}
 			}
 		}
 		throw new IllegalArgumentException("Deze batterij is niet in het bezit van de robot."); 
 	}
 
 	/**
 	 * Deze methode plaats een batterij uit de bezittingen van de robot op de huidige positie van de robot in het bord van de robot.
 	 * 
 	 * @param	battery
 	 * 			De batterij die op het bord geplaatst moet worden.
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			De batterij is niet in het bezit van de robot.
 	 * 
 	 * @throws 	IllegalStateException
 	 * 			De robot staat niet op een bord.
 	 */
 	public void drop(Battery battery) throws IllegalArgumentException, IllegalStateException{
 		if(!this.isOnBoard()){
 			throw new IllegalStateException("De robot staat niet op een bord.");
 		}else if(!this.getPossessions().contains(battery)){
 			throw new IllegalArgumentException("De robot heeft deze batterij niet.");
 		}else{
 			this.getPossessions().remove(battery);
 			battery.putOnBoard(this.getBoard(), this.getPosition());
 		}
 	}
 
 	/**
 	 * Deze methode kijkt na of de robot kan draaien.
 	 * 
 	 * @return	Boolean die true is als de robot voldoende energie heeft om te draaien.
 	 * 			|if(this.getEnergy().getEnergy() - TURN_COST.getEnergy() < 0)
 	 * 			|	false
 	 * 			|isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy()))
 	 */
 	@Raw
 	public boolean canTurn(){
 		if(this.getEnergy().getEnergy() - TURN_COST.getEnergy() < 0)
 			return false;
 		return isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST.getEnergy()));
 	}
 
 	/**
 	 * Deze methode kijkt na of de robot kan moven.
 	 * 
 	 * @return	Boolean die true is als de robot voldoende energie heeft om te moven.
 	 * 			|if(this.getEnergy().getEnergy() - moveCost(this).getEnergy() < 0)
 	 * 			|	false
 	 * 			|isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - moveCost(this).getEnergy()))
 	 * 
 	 * @note	Deze methode houdt enkel rekening met de minimale energie en niet met de positie.
 	 */
 	@Raw
 	public boolean canMove(){
 		if(this.getEnergy().getEnergy() - moveCost(this).getEnergy() < 0)
 			return false;
 		return isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - moveCost(this).getEnergy()));
 	}
 
 	/*
 	 * De robot en al zijn bezittingen worden vernietigd.
 	 * 
 	 * @post	De robot heeft geen bezittingen meer.
 	 * 			|new.getPossessions().isEmpty()
 	 * 
 	 * @see 	roborally.model.Entity#destroy()
 	 */
 	@Override
 	public void destroy() {
 		Iterator<Battery> itr = this.getPossessions().iterator();
 		while(itr.hasNext()){
 			Battery current = itr.next();
 			current.destroy();
 			this.getPossessions().remove(current);
 		}
 		super.destroy();
 	}
 
 }

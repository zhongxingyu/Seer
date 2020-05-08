 package roborally.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Raw;
 import roborally.property.*;
 import roborally.utils.*;
 
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
 	 * De maximale bovengrens van de energie van een robot.
 	 */
 	public final static Energy MAXENERGY = new Energy(20000);
 	/**
 	 * De kost van 1 schot met zijn laser.
 	 */
 	public final static Energy SHOOT_COST = new Energy(1000);
 
 	public final static Energy SHOOT_DAMAGE = new Energy(4000);
 	/**
 	 * De energie van de robot.
 	 */
 	private Energy energy;
 	/**
 	 * Het maxima van de energy 
 	 */
 	private Energy maxEnergy;
 	/**
 	 * De orintatie van de robot.
 	 */
 	private Orientation orientation;
 	/**
 	 * De lijst van batterijen die de robot bezit.
 	 * 
 	 * @note	Gebruik altijd Collections.sort met een BatteryComparator() wanneer deze lijst gewijzigd wordt.
 	 */
 	private ArrayList<Item> possessions = new ArrayList<>();
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
 	 * 			|new.getEnergy().equals(energy) == true
 	 * 
 	 * @post	De orintatie van de robot is gelijk aan de opgegeven orintatie.
 	 * 			|new.getOrientation().equals(orientation) == true
 	 */
 	public Robot(Orientation orientation, Energy energy){
 		this.setEnergy(energy);
 		setOrientation(orientation);
 		setMaxEnergy(energy);
 	}
 
 	/**
 	 * Deze methode wijzigt de energie van de robot.
 	 * 
 	 * @param	energy
 	 * 			De nieuwe energie die de robot moet krijgen.
 	 * 
 	 * @post	De energie van de robot is gelijk aan de gegeven energie.
 	 * 			|new.getEnergy().equals(energy) == true
 	 */
 	private void setEnergy(Energy energy) {
 		this.energy = energy;
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
 	 * 
 	 * @return
 	 */
 	//TODO: doc
 	@Basic
 	public Energy getMaxEnergy() {
 		return this.maxEnergy;
 	}
 
 	/**
 	 * Deze methode stelt de bovengrens in van de nergie van de robot
 	 * 
 	 * @param currentMaxEnergy
 	 */
 	//TODO: doc + hoe uitgewerkt? nom def of tot?
 	@Basic
 	public void setMaxEnergy(Energy currentMaxEnergy) {
 		if (currentMaxEnergy.getEnergy() < Robot.MAXENERGY.getEnergy())
 			this.maxEnergy = currentMaxEnergy;
 		this.maxEnergy = Robot.MAXENERGY;
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
 	 * 			|if(isValidRobotEnergyAmount(Energy.energyDifference(this.getEnergy(), TURN_COST)))
 	 * 			|	new.getOrientation() == this.getOrienation().getClockwiseOrientation()
 	 * 
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(Energy.energyDifference(this.getEnergy(), TURN_COST)))
 	 * 			|	new.getEnergy().equals(Energy.energyDifference(this.getEnergy(), TURN_COST))
 	 */
 	public void turnClockWise(){
 		if(this.canTurn()){
 			this.setOrientation(this.getOrientation().getClockwiseOrientation());
 			this.setEnergy(Energy.energyDifference(this.getEnergy(), TURN_COST));
 		}
 	}
 
 	/**
 	 * Draait de robot 1 keer in tegenwijzerzin.
 	 * 
 	 * @post	De nieuwe orintatie van de robot is gelijk aan de volgende orintatie in wijzerzin.
 	 * 			|if(isValidRobotEnergyAmount(Energy.energyDifference(this.getEnergy(), TURN_COST)))
 	 * 			|	new.getOrientation() == this.getOrienation().getCounterClockwiseOrientation()
 	 * 
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(Energy.energyDifference(this.getEnergy(), TURN_COST)))
 	 * 			|	new.getEnergy().equals(Energy.energyDifference(this.getEnergy(), TURN_COST))
 	 */
 	public void turnCounterClockWise(){
 		if(this.canTurn()){
 			this.setOrientation(this.getOrientation().getCounterClockwiseOrientation());
 			this.setEnergy(Energy.energyDifference(this.getEnergy(), TURN_COST));
 		}
 	}
 
 	/**
 	 * Deze methode beweegt de robot een stap vooruit indien mogelijk.
 	 * 
 	 * @throws 	IllegalStateException
 	 * 			De robot heeft onvoldoende energie om te bewegen of de positie waarnaar bewogen moet worden is ongeldig of reeds bezet.
 	 * 			|!this.canMove() || !Position.isValidPosition(this.getOrientation().getNextPosition(this.getPosition())) || !this.getBoard().isPlacableOnPosition(Calculator.getNextPosition(this.getPosition(), this.getOrientation()))
 	 * 
 	 * @post	De robot staat een plaats verder.
 	 * 			|new.getPosition().equals(Calculator.getNextPosition(this.getPosition(), this.getOrientation())) == true
 	 * 
 	 * @post	De robot heeft energie verbruikt.
 	 * 			|new.getEnergy().equals(Energy.energyDifference(this.getEnergy(), moveCost(this))) == true
 	 */
 	public void move() throws IllegalStateException{
 		if(!this.canMove()){
 			throw new IllegalStateException("De robot heeft onvoldoende energie om te bewegen.");
 		}
 		Position destination;
 		try{
 			destination = getNextPosition(this.getPosition(), this.getOrientation());
 		}catch (IllegalStateException e){
 			throw new IllegalStateException("De positie waarnaar bewogen moet worden is ongeldig.");
 		}
 		if(this.getBoard().isPlacableOnPosition(destination,this)){
 			this.setPosition(destination);
 			this.setEnergy(Energy.energyDifference(this.getEnergy(), moveCost(this)));
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
 	 * 			|Calculator.aStarOnTo(this, position).get(position.toString()).getGCost()
 	 * @throws TargetNotReachableException 
 	 *
 	 */
 	//TODO: Moet een negatief getal terugeven indien niet bereikbaar of te weinig energie
 	public Energy getEnergyRequiredToReach(Position position) throws TargetNotReachableException{
		HashMap<String,Node> reachables = getReachables(this);
		if (reachables.containsKey(position.toString())){
 			HashMap<String, Node> resultpad = aStarOnTo(this, position);
 			Node n = resultpad.get(position.toString());
 			Energy cost = n.getGCost();
 			return cost;
 		}
		throw new TargetNotReachableException();
 	}
 
 	/**
 	 * Deze methode verplaatst de robot zo dicht mogelijk bij een andere robot.
 	 * 
 	 * @param	robot
 	 * 			De robot waar naartoe moet bewogen worden.
 	 * 
 	 * @post	De robot staat zo dicht mogelijk bij de gegeven robot met een zo laag mogelijk energieverbruik.
 	 * 			|TODO: formele post doc bij moveNextTo()
 	 */
 	public void moveNextTo(Robot robot){
 		if (!this.equals(robot) && robot != null && this.getBoard().equals(robot.getBoard())) {
 			HashMap<String,Node> thisReachables = getReachables(this);
 			HashMap<String,Node> otherReachables = getReachables(robot);
 			Set<String> thisKeys = thisReachables.keySet();
 			Set<String> otherKeys = otherReachables.keySet();
 			ArrayList<PositionPair> posPairs = new ArrayList<>();
 			for(String thisPosString: thisKeys){
 				for(String otherPosString: otherKeys){
 					Node thisNode = thisReachables.get(thisPosString);
 					Node otherNode = otherReachables.get(otherPosString);
 					PositionPair toAdd = new PositionPair(Robot.getPositionFromString(thisPosString), Robot.getPositionFromString(otherPosString),thisNode.getGCost(),
 							otherNode.getGCost(), thisNode.getOrientation(),otherNode.getOrientation());
 					posPairs.add(toAdd);
 				}
 			}
 			ArrayList<PositionPair> semiValidPosPairs = new ArrayList<>();
 			for(PositionPair pp: posPairs){
 				if((pp.getManhattanDistance() != 0) && !(pp.getPos1().toString().equals(pp.getPos2().toString())))
 					semiValidPosPairs.add(pp);
 			}
 			if(!semiValidPosPairs.isEmpty()){
 				Collections.sort(semiValidPosPairs, new PositionPairComparatorDistance());
 				PositionPair firstpp = semiValidPosPairs.get(0);
 				ArrayList<PositionPair> validPosPairs = new ArrayList<>();
 				for(PositionPair pp: semiValidPosPairs){
 					if((pp.getManhattanDistance() == firstpp.getManhattanDistance()))
 						validPosPairs.add(pp);
 				}
 				Collections.sort(validPosPairs, new PositionPairComparatorEnergy());
 				PositionPair thePair = validPosPairs.get(0);
 				this.setPosition(thePair.getPos1());
 				robot.setPosition(thePair.getPos2());
 				this.setOrientation(thePair.getOr1());
 				robot.setOrientation(thePair.getOr2());
 				this.setEnergy(Energy.energyDifference(this.getEnergy(),thePair.getCost1()));
 				robot.setEnergy(Energy.energyDifference(robot.getEnergy(),thePair.getCost2()));	
 			}
 		}
 	}
 
 	/**
 	 * Deze methode doet een robot schieten met zijn laser.
 	 * 
 	 * @throws	IllegalStateException
 	 * 			De robot staat niet op een bord.
 	 * 			|!this.isOnBoard()
 	 * 
 	 * @post	De robot verliest energie. De hoeveelheid is bepaald in de constante SHOOT_COST.
 	 * 			|new.getEnergy().equals(Energy.energyDifference(this.getEnergy(), SHOOT_COST)) == true
 	 * 
 	 * @post	Mogelijks wordt een object geraakt en vernietigd.
 	 * 			|TODO: formele post doc bij shoot()
 	 */
 	public void shoot() throws IllegalStateException{
 		if(this.isOnBoard()){	
 			Position beginpos = this.getPosition();
 			Orientation beginor = this.getOrientation();
 			boolean found = false;
 			while(this.getBoard().isValidBoardPosition(getNextPosition(beginpos, beginor)) && !found){
 				beginpos = getNextPosition(beginpos, beginor);
 				HashSet<Entity> content = this.getBoard().getEntityOnPosition(beginpos);
 				if(content != null){
 					Random rndm = new Random();
 					((Entity) content.toArray()[rndm.nextInt(content.toArray().length)]).destroy();
 					found = true;
 				}
 			}
 			this.setEnergy(Energy.energyDifference(this.getEnergy(), SHOOT_COST));
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
 		this.setEnergy(newEnergy);
 	}
 
 	/**
 	 * Deze methode berekent de kost van 1 move.
 	 * 
 	 * @param	robot
 	 * 			De robot waarvoor de berekening moet uitgevoerd worden.
 	 * 
 	 * @return	de kost terug van 1 move
 	 * 			|Energy.energySum(MOVE_COST, new Energy(MOVE_COST_PER_KG.getEnergy()*(robot.getTotalWeight().getWeight() / 1000)))
 	 */
 	public static Energy moveCost(Robot robot){
 		return Energy.energySum(MOVE_COST, new Energy(MOVE_COST_PER_KG.getEnergy()*(robot.getTotalWeight().getWeight() / 1000)));
 	}
 
 	/**
 	 * Geeft het totale gewicht van alles wat de robot draagt.
 	 * 
 	 * @return	Het totale gewicht van alles wat de robot draagt.
 	 * 			|int totalWeight = 0;
 	 * 			|for(Battery batt: this.getPossessions()){
 	 * 			|	totalWeight += batt.getWeight().getWeight();
 	 * 			|}
 	 * 			|new Weight(totalWeight)
 	 */
 	public Weight getTotalWeight(){
 		int totalWeight = 0;
 		for(Item item: this.getPossessions()){
 			totalWeight += item.getWeight().getWeight();
 		}
 		return new Weight(totalWeight);
 	}
 
 	/**
 	 * Geeft een lijst terug van alle items die de robot momenteel draagt.
 	 * 
 	 * @return	De lijst van batterijen
 	 * 			|this.Possessions
 	 */
 	public ArrayList<Item> getPossessions(){
 		return this.possessions;
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
 	public void pickUp(Item item) throws IllegalArgumentException, IllegalStateException{
 		if(!this.isOnBoard()){
 			throw new IllegalStateException("De robot staat niet op een bord.");
 		}else if(!this.getBoard().equals(item.getBoard())){
 			throw new IllegalArgumentException("De robot staat niet op hetzelfde bord als de batterij.");
 		}else if(!this.getPosition().toString().equals(item.getPosition().toString())){
 			throw new IllegalArgumentException("De robot staat niet op dezelfde positie als de batterij.");
 		}else{
 			item.removeFromBoard();
 			this.getPossessions().add(item);
 			Collections.sort(this.getPossessions(), new ItemComparator());
 		}
 	}
 
 
 	/**
 	 * Deze methode laadt de robot op met de energie in een batterij die hij bij zich heeft.
 	 * 
 	 * @param	battery
 	 * 			De batterij die gebruikt moet worden.
 	 * 
 	 * @post	De robot is opgeladen met een hoeveelheid energie uit de batterij (dit kan onveranderd blijven).
 	 * 			|new.getEnergy().equals(Energy.energySum(this.getEnergy(), Energy.energyDifference(battery.getEnergy(), Energy.energyDifference(Robot.MAXENERGY, this.getEnergy())))) == true
 	 * 
 	 * @post	De batterij heeft mogelijks energie verloren.
 	 * 			|(new battery).getEnergy().equals(Energy.energyDifference(battery.getEnergy(), Energy.energyDifference(Robot.MAXENERGY, this.getEnergy()))) == true
 	 */
 	//TODO: neke checke
 	public void use(Item item) {
 		if (this.getPossessions().contains(item)){
 			if(item.isTerminated()){
 				this.getPossessions().remove(item);
 			}else{
 				if (item instanceof Battery){
 					Energy toRecharge = Energy.energyDifference(item.getEnergy(), Energy.energyDifference(Robot.MAXENERGY, this.getEnergy()));
 					item.setEnergy(toRecharge);
 					this.setEnergy(Energy.energySum(this.getEnergy(), toRecharge));
 					if(item.getEnergy().getEnergy() == 0){
 						this.getPossessions().remove(item);
 						Collections.sort(this.getPossessions(), new ItemComparator());
 						item.destroy();
 					}
 				} else if (item instanceof SurpriseBox){
 					getSurpriseBoxAction(item);
 				}else {
 					// item instanceof Repairkit
 				}
 			}
 		}else{
 			throw new IllegalArgumentException("Deze batterij is niet in het bezit van de robot."); 
 		}
 	}
 
 	public void getSurpriseBoxAction(Item item){
 		Random rand = new Random();
 		int choice = 1 + rand.nextInt(3);
 		if (choice == 1){
 			//explosion
 			this.setMaxEnergy(new Energy(this.getMaxEnergy().getEnergy()- SHOOT_DAMAGE.getEnergy()));
 			if (this.getEnergy().getEnergy() < this.getMaxEnergy().getEnergy()){
 				this.setEnergy(this.getMaxEnergy());
 			}
 			item.destroy();
 		}else if (choice == 2){
 			//teleport
 			Position teleport = getRandomBoardPosition();
 			this.setPosition(teleport);
 			item.destroy();
 		}else{
 			//random new item
 			choice = 1 + rand.nextInt(3);
 			if (choice == 1){
 				//SurpriseBox
 				SurpriseBox box = new SurpriseBox(item.getWeight());
 				this.getPossessions().add(box);
 				item.destroy();
 			}else if (choice == 2){
 				//Battery
 				Battery bat = new Battery(new Energy(rand.nextInt((int)Battery.MAXBATTERYENERGY.getEnergy()) + 1),item.getWeight());
 				this.getPossessions().add(bat);
 				item.destroy();
 			}else{
 				//Repairkit
 				RepairKit kit = new RepairKit(new Energy(rand.nextInt((int)RepairKit.MAX_ENERGY.getEnergy()) + 1) ,item.getWeight());
 				this.getPossessions().add(kit);
 				item.destroy();
 			}
 		}
 	}
 
 	private Position getRandomBoardPosition() {
 		long height = this.getBoard().getHeight();
 		long width = this.getBoard().getHeight();
 		Random rand = new Random();
 		long randomX = 0;
 		long randomY = 0;
 		boolean xfound = false;
 		boolean yfound = false;		
 		while (!xfound){
 			randomX = rand.nextLong();
 			if (randomX >= 0 && randomX <= width){
 				xfound = true;
 			}
 		}
 		while (!yfound){
 			randomY = rand.nextLong();
 			if (randomY >= 0 && randomY <= height){
 				xfound = true;
 			}
 		}
 		Position teleport = new Position(randomX, randomY);
 		if (this.getBoard().isPlacableOnPosition(teleport, this)){
 			return teleport;		
 		}
 		return getRandomBoardPosition();
 	}
 
 	/**
 	 * Deze methode plaats een Item uit de bezittingen van de robot op de huidige positie van de robot in het bord van de robot.
 	 * 
 	 * @param	item
 	 * 			De batterij die op het bord geplaatst moet worden.
 	 * 
 	 * @throws 	IllegalArgumentException
 	 * 			Het item is niet in het bezit van de robot.
 	 * 
 	 * @throws 	IllegalStateException
 	 * 		 	Het item staat niet op een bord.
 	 * 
 	 * @post	Het item is niet langer in het bezit van de robot.
 	 * 			|new.getPossessions().contains(item) == false
 	 * 
 	 * @post	Het item bevindt zich op dezelfde positie als de robot.
 	 * 			|(new item).getPosition().equals(new.getPosition()) == true
 	 * 
 	 * @post	Het item bevindt zich op hetzelfde bord als de robot.
 	 * 			|(new item).getBoard().equals(new.getBoard()) == true
 	 */
 	public void drop(Item item) throws IllegalArgumentException, IllegalStateException{
 		if(!this.isOnBoard()){
 			throw new IllegalStateException("De robot staat niet op een bord.");
 		}else if(!this.getPossessions().contains(item)){
 			throw new IllegalArgumentException("De robot heeft dit item niet.");
 		}else{
 			this.getPossessions().remove(item);
 			item.putOnBoard(this.getBoard(), this.getPosition());
 			Collections.sort(this.getPossessions(), new ItemComparator());
 		}
 	}
 
 	/**
 	 * Deze methode kijkt na of de robot kan draaien.
 	 * 
 	 * @return	Boolean die true is als de robot voldoende energie heeft om te draaien.
 	 * 			|if(this.getEnergy().getEnergy() - TURN_COST.getEnergy() < 0)
 	 * 			|	false
 	 * 			|true
 	 */
 	@Raw
 	public boolean canTurn(){
 		if(this.getEnergy().getEnergy() - TURN_COST.getEnergy() < 0)
 			return false;
 		return true;
 	}
 
 	/**
 	 * Deze methode kijkt na of de robot kan moven.
 	 * 
 	 * @return	Boolean die true is als de robot voldoende energie heeft om te moven.
 	 * 			|if(this.getEnergy().getEnergy() - moveCost(this).getEnergy() < 0)
 	 * 			|	false
 	 * 			|true
 	 * 
 	 * @note	Deze methode houdt enkel rekening met de minimale energie en niet met de positie.
 	 */
 	@Raw
 	public boolean canMove(){
 		if(this.getEnergy().getEnergy() - moveCost(this).getEnergy() < 0)
 			return false;
 		return true;
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
 		for(Item item: this.getPossessions()){
 			item.destroy();
 		}
 		this.getPossessions().clear();
 		super.destroy();
 	}
 
 	/**
 	 * Deze methode geeft de volgende positie weer met de huidige orintatie.
 	 * 
 	 * @param	pos
 	 * 			Positie van waaruit vertrokken moet worden.
 	 * 
 	 * @param	or
 	 * 			De huidige orintatie.
 	 * 
 	 * @return	De volgende positie met de huidige orintatie
 	 * 			|if(or.equals(Orientation.UP))
 	 *			|	new Position(pos.getX(), pos.getY() - 1)
 	 *			|if(or.equals(Orientation.RIGHT))
 	 *			|	new Position(pos.getX() + 1, pos.getY())
 	 *			|if(or.equals(Orientation.DOWN))
 	 *			|	new Position(pos.getX(), pos.getY() + 1);
 	 *			|if(or.equals(Orientation.LEFT))
 	 *			|	new Position(pos.getX() - 1, pos.getY())
 	 *			|null
 	 *
 	 * @throws	IllegalStateException
 	 * 			Er bestaat geen verdere positie meer met deze orintatie.
 	 */
 	private Position getNextPosition(Position pos, Orientation or) throws IllegalStateException{
 		Position result = null;
 		switch(or){
 		case UP:
 			try {
 				result = new Position(pos.getX(), pos.getY() - 1);
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 			break;
 		case RIGHT:
 			try {
 				result = new Position(pos.getX() + 1, pos.getY());
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 			break;
 		case DOWN:
 			try {
 				result = new Position(pos.getX(), pos.getY() + 1);
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 			break;
 		case LEFT:
 			try {
 				result = new Position(pos.getX() - 1, pos.getY());
 			} catch (IllegalArgumentException e) {
 				throw new IllegalStateException("Er bestaat geen verdere positie meer met deze orintatie.");
 			}
 			break;
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param map
 	 * @return
 	 */
 	private Node getMinimalFNode(HashMap<String, Node> map){
 		Collection<Node> c = map.values();
 		Iterator<Node> itr = c.iterator();
 		Node minimalNode = itr.next();
 		for (Node node : c){
 			if (node.getFCost().getEnergy() < minimalNode.getFCost().getEnergy())
 				minimalNode = node;
 		}
 		return minimalNode;
 	}
 
 	/**
 	 * methode voor het aantal turns terug te geven om van een node met orientatie m naar een nabijgelegen node te 
 	 * bewegen (vlak naast)
 	 *  
 	 * @param 	node
 	 * 
 	 * @param 	pos 
 	 * 	
 	 * @return
 	 */
 	private int getTurns(Node node, Position pos){
 		int result = 0;
 		if(node.getPosition().getX() == pos.getX() && node.getPosition().getY() == pos.getY())
 			return result;
 		/*
 		 * In dit gedeelte kijken we de verhouding van de huidige robot met zijn bestemming na. Hier worden alle gevallen overlopen.
 		 * Om dit visueel voor te stellen staan er letters die de posities voorstellen. De hoekpunten zijn in wijzerszin A, B, C en D.
 		 * Vervolgens worden de middens van elke rand voorgesteld met E, F, G en H.
 		 */
 		if(pos.getX() == node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// E
 			switch(node.getOrientation()){
 			case RIGHT:
 			case LEFT:
 				result = 1;
 				break;
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() == node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// G
 			switch(node.getOrientation()){
 			case LEFT:
 			case RIGHT:
 				result = 1;
 				break;
 			case UP:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// F
 			switch(node.getOrientation()){
 			case DOWN:
 			case UP:
 				result = 1;
 				break;
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// B
 			switch(node.getOrientation()){
 			case RIGHT:
 			case UP:
 				result = 1;
 				break;
 			case DOWN:
 			case LEFT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() > node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// C
 			switch(node.getOrientation()){
 			case DOWN:
 			case RIGHT:
 				result = 1;
 				break;
 			case LEFT:
 			case UP:
 				result = 2;	
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() == node.getPosition().getY()){
 			// H
 			switch(node.getOrientation()){
 			case UP:
 			case DOWN:
 				result = 1;
 				break;
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() < node.getPosition().getY()){
 			// A
 			switch(node.getOrientation()){
 			case UP:
 			case LEFT:
 				result = 1;
 				break;
 			case RIGHT:
 			case DOWN:
 				result = 2;
 				break;
 			}
 		}else if(pos.getX() < node.getPosition().getX() && pos.getY() > node.getPosition().getY()){
 			// D
 			switch(node.getOrientation()){
 			case LEFT:
 			case DOWN:
 				result = 1;
 				break;
 			case UP:
 			case RIGHT:
 				result = 2;
 				break;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param 	currentNode
 	 * 
 	 * @param 	pos
 	 * 
 	 * @param 	robot
 	 * 
 	 * @return	
 	 * 
 	 */
 	private Energy getGCost(Node currentNode, Position pos, Robot robot) {
 		return Energy.energySum(Energy.energySum(currentNode.getGCost(), moveCost(robot)),
 				new Energy(TURN_COST.getEnergy()*getTurns(currentNode, pos)));
 	}
 
 	/**
 	 * Deze methode gaat de heuristieke kost naar een gegeven positie vanuit de meegegeven node berekenen.
 	 * 
 	 * @param 	position
 	 * 			
 	 * @param 	orientation
 	 * 			
 	 * @param 	pos
 	 * 			
 	 * @param 	robot
 	 * 			
 	 * @return	Energy
 	 * 		
 	 * 			
 	 */
 	private Energy getHCost(Position position, Orientation orientation, Position pos, Robot robot) {
 		//de manhattan kost om met de robot van de beginpositie tot de eindpos te geraken.
 		Energy manHattanCost = new Energy(moveCost(robot).getEnergy() * (int) calculateManhattan(position, pos));
 		Energy turnCost = new Energy(TURN_COST.getEnergy()*getTurns(new Node(position,orientation,robot.getBoard()),pos));
 		return Energy.energySum(manHattanCost, turnCost);
 	}
 
 	/**
 	 * Deze methode gaat de orientatie bepalen die een node op een positie volgend op de currentnode zou krijgen.
 	 *   
 	 * @param 	currentNode
 	 * 
 	 * @param 	pos
 	 * 
 	 * @return
 	 * 
 	 */
 	private Orientation getNodeOrientation(Node currentNode, Position pos) {
 		Position previousPosition = currentNode.getPosition();
 		if (previousPosition.getX() == pos.getX()){
 			if (previousPosition.getY() < pos.getY())
 				return Orientation.DOWN;
 			return Orientation.UP;
 		}
 		if (previousPosition.getX() > pos.getX())
 			return Orientation.LEFT;
 		return Orientation.RIGHT;
 	}
 
 	/**
 	 * Deze methode gaat op basis van het A* algorithme het optimale pad naar een gegeven positie teruggeven.
 	 * 
 	 * @param 	a
 	 * 
 	 * @param 	pos
 	 * 
 	 * @return
 	 * 
 	 * 	
 	 */
 	private HashMap<String,Node> aStarOnTo(Robot a, Position pos){
 		//deze gaat direct naar de positie die opgegeven wordt
 
 		HashMap<String,Node> open = new HashMap<>(); 
 		// de experimentele posities die nog gevalueerd moeten/kunnen worden
 		Node startNode = new Node(a.getPosition(), a.getBoard(), new Energy(0) , 
 				getHCost(a.getPosition(), a.getOrientation(),pos, a),a.getOrientation(), null);
 
 		open.put(a.getPosition().toString(), startNode);
 		// de startPositie aan de open list toevoegen
 		HashMap<String,Node> closed = new HashMap<>(); 
 		// de lijst met al gevalueerde posities
 		Board board = a.getBoard();
 
 		while ( !open.isEmpty()){
 			Node currentNode = getMinimalFNode(open);
 			if (pos.toString().equals(currentNode.getPosition().toString())){
 				open.remove(currentNode.getPosition().toString());
 				closed.put(currentNode.getPosition().toString(), currentNode);
 				return closed;
 			}
 
 			open.remove(currentNode.getPosition().toString());
 			closed.put(currentNode.getPosition().toString(), currentNode);
 
 			ArrayList<Position> neighbours = currentNode.getPosition().getNeighbours(a.getBoard());
 			for (Position neighbour : neighbours){
 				double gCostNeighbour = getGCost(currentNode, neighbour, a ).getEnergy();
 				if (closed.containsKey(neighbour.toString())){
 					if(closed.get(neighbour.toString()).getGCost().getEnergy() > gCostNeighbour){
 						closed.get(neighbour.toString()).setGCost(new Energy(gCostNeighbour));
 						closed.get(neighbour.toString()).setParent(currentNode);
 					}
 					continue;
 				}
 				else if ((open.containsKey(neighbour.toString())) && (open.get(neighbour.toString()).getGCost().getEnergy() > gCostNeighbour)){
 					open.get(neighbour.toString()).setGCost(new Energy(gCostNeighbour));
 					open.get(neighbour.toString()).setParent(currentNode);
 				}
 				else if (board.isPlacableOnPosition(neighbour,a)){
 						open.put(neighbour.toString(), new Node(neighbour,board, new Energy(gCostNeighbour),
 								getHCost(neighbour,getNodeOrientation(currentNode,neighbour) , pos, a),
 								getNodeOrientation(currentNode,neighbour),currentNode));
 				} else 
 					open.remove(neighbour);
 			} 
 		}
 	return closed;
 }
 
 /**
  * Deze methode gaat posities waar een wall op staat uit de ArrayList gaan verwijderen.
  * 
  * @param 	neighbours
  * 
  * @param 	board
  * 
  * @return	
  * 			
  */
 private ArrayList<Position> removeWalls(ArrayList<Position> neighbours, Board board) {
 	ArrayList<Position> result = new ArrayList<>();
 	for (Position pos : neighbours){
 		if (!board.containsWall(pos)){
 			result.add(pos);
 		}
 	}
 	return result;
 }
 
 /**
  * Deze methode gaat alle nodes teruggeven waar deze robot zou kunnen geraken.
  * 
  * @param 	robot
  * 			De robot waarvan je de haalbare posities wilt weten.
  * 
  * @return	HashMap<String,Node> 
  * 			
  */
 private HashMap<String,Node> getReachables(Robot robot){
 	Energy upperbound = robot.getEnergy();
 	ArrayList<String> explorable = new ArrayList<>();
 	explorable.add(robot.getPosition().toString());
 	HashMap<String,Node> reachables = new HashMap<>();
 
 	while(!explorable.isEmpty()){
 		Position currentPos = getPositionFromString(explorable.get(0));
 		HashMap<String,Node> pad = aStarOnTo(robot,currentPos);
 		Node currentNode = pad.get(currentPos.toString());
 		explorable.remove(0);
 		if(currentNode.getGCost().getEnergy() <= upperbound.getEnergy()){
 			reachables.put(currentPos.toString(),currentNode);
 			ArrayList<Position> preNeighbours = currentPos.getNeighbours(robot.getBoard());
 			ArrayList<Position> neighbours = removeWalls(preNeighbours,robot.getBoard());
 			for(Position neighbour: neighbours){
 				if((!reachables.containsKey(neighbour.toString())) && (!explorable.contains(neighbour.toString())))
 					explorable.add(neighbour.toString());
 			}
 		}			
 	}
 	return reachables;
 }
 
 /**
  * Deze methode gaat van een string van de vorm long1,long2 een object maken met die longs als positions.
  * 
  * @param 	posString
  * 			De string waarvan je het Positie object wil maken.
  * 
  * @return	Position 
  * 			|result.getX() = Long.parseLong(posString.split(",")[0])
  * 			|result.getY() = Long.parseLong(posString.split(",")[1])
  *
  * @throws IllegalArgumentException
  */
 public static Position getPositionFromString(String posString) throws IllegalArgumentException{
 	if(posString.indexOf(",") == -1)
 		throw new IllegalArgumentException("De String is niet geformatteerd als een positie.");
 	String[] split = posString.split(", ");
 	long x, y;
 	try{
 		x = Long.parseLong(split[0]);
 	}catch(NumberFormatException e){
 		throw new IllegalArgumentException(e.getMessage());
 	}
 	try{
 		y = Long.parseLong(split[1]);
 	}catch(NumberFormatException e){
 		throw new IllegalArgumentException(e.getMessage());
 	}
 	Position result;
 	try{
 		result = new Position(x, y);
 	}catch(IllegalArgumentException e){
 		throw new IllegalArgumentException(e.getMessage());
 	}
 	return result;		
 }
 
 /**
  * Deze Methode gaat de manhattandistance van punt A met pos pos1 tot punt B met pos pos2 berekenen.
  * 
  * @param 	pos1
  * 			Het eerste cordinatenkoppel.
  * 
  * @param 	pos2
  * 	 		Het tweede cordinatenkoppel.
  * 
  *@return 	long 
  *			Een long die de manhattan teruggeeft.
  *			|return result = (Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()))
  * 
  */
 public static long calculateManhattan(Position pos1, Position pos2){
 	return (Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()));
 }
 
 }
 
 // hier ga ik de Random todo's zetten dus dingen die volgens mij ng moeten gebeuren.
 //TODO: Inputstream met scanner om die programmas deftig te kunnen inlezen.
 //TODO: Private shit beginnen maken.
 //TODO: merge aanpassen.
 //TODO: Alles van die programma's. 
 //TODO: GetEnergyRequiredToReach aanpassen om de juiste dingen terug te geven.
 //TODO: Testen veel testen.
//TODO: Interfaces moet nimeer dagtek
//TODO: Error gevonden voor pick up 
//TODO: Error ge kunt geen 2 robots op 1 positie aanmaken 
//		maar als ge et via ne moveto doet kunnen ze uiteindelijk wel op dez pos uitkomen.

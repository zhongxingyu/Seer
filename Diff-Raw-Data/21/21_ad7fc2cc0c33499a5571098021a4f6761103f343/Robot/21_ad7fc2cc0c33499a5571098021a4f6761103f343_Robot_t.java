 package roborally.model;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import roborally.basics.*;
 import roborally.interfaces.IRobot;
 import roborally.utils.BatteryComparator;
 
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
 public class Robot extends Entity implements IRobot{
 
 	/**
 	 * De energiekost van 1 move.
 	 */
 	private final static int MOVE_COST = 500;
 	/**
 	 * De energiekost van 1 draai.
 	 */
 	public final static int TURN_COST = 100;
 	
 	private final static int MOVE_COST_PER_KG = 50;
 	/**
 	 * De minimale energie van een robot.
 	 */
 	public final static int MINENERGY = 0;
 	/**
 	 * De maximale energie van een robot.
 	 */
 	public final static int MAXENERGY = 20000;
 
 	/**
 	 * De energie van de robot.
 	 */
 	private Energy energy;
 	/**
 	 * De orintatie van de robot.
 	 */
 	private Orientation orientation;
 	
 	/**
 	 * Een instance van de comparator waarmee batterijen vergeleken kunnen worden op basis van gewicht.
 	 */
 	private BatteryComparator bc;
 	
 	/**
 	 * De set van batterijen die de robot bezit.
 	 */
 	private Set<Battery> Possessions = new TreeSet<Battery>(bc);
 	
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
 		setEnergy(energy);
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
 		return orientation;
 	}
 
 	/**
 	 * Methode om de energie van een robot te wijzigen.
 	 * 
 	 * @param	newEnergy
 	 * 			De energie die de robot moet krijgen.
 	 * 
	 * @pre		De hoeveelheid energie moet geldig zijn.
	 * 			|isValidRobotEnergyAmount(newEnergy)
	 * 
 	 * @post 	De energie van de robot is newEnergy.
 	 * 			|new.getEnergy() == newEnergy
 	 */
 	private void setEnergy(Energy newEnergy) {
 		this.energy = newEnergy;
 	}
 
 	/**
 	 * Methode om de energie van de robot te verkrijgen.
 	 * 
 	 * @return 	De energie van de robot.
 	 * 			|this.energy
 	 */
 	@Basic
 	public Energy getEnergy(){
 		return energy;
 	}
 
 	/**
 	 * Methode die controleert of de opgegeven hoeveelheid energie een geldige hoeveelheid is.
 	 * 
 	 * @param 	energy
 	 * 			De hoeveelheid energie.
 	 *
 	 * @return	boolean
	 * 			|(energy.getEnergy() >= MINENERGY) && (energy.getEnergy() <= MAXENERGY)
 	 */
 	public static boolean isValidRobotEnergyAmount(Energy energy){
 		return (energy.getEnergy() >= MINENERGY) && (energy.getEnergy() <= MAXENERGY);
 	}
 
 	/**
 	 * Deze methode berekent de verhouding tussen de huidige hoeveelheid energie en de maximale hoeveelheid energie.
 	 * 
 	 * @return	De verhouding tussen de huidige hoeveelheid energie en de maximale hoeveelheid energie.
 	 * 			|this.getEnergy().getEnergy()/MAXENERGY
 	 */
 	public double getEnergyFraction(){
 		return this.getEnergy().getEnergy()/MAXENERGY;
 	}
 
 	/**
 	 * Draait de robot 1 keer in wijzerzin.
 	 * 
 	 * @post	De nieuwe orintatie van de robot is gelijk aan de volgende orintatie in wijzerzin.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST)))
 	 * 			|	new.getOrientation() == this.getOrienation().getClockwiseOrientation()
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST)))
 	 * 			|	new.getEnergy().getEnergy().equals(this.getEnergy().getEnergy() - TURN_COST)
 	 */
 	public void turnClockWise(){
 		if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST))){
 			this.setOrientation(this.getOrientation().getClockwiseOrientation());
 			this.getEnergy().setEnergy(this.getEnergy().getEnergy() - TURN_COST);
 		}
 	}
 
 	/**
 	 * Draait de robot 1 keer in tegenwijzerzin.
 	 * 
 	 * @post	De nieuwe orintatie van de robot is gelijk aan de volgende orintatie in wijzerzin.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST)))
 	 * 			|	new.getOrientation() == this.getOrienation().getCounterClockwiseOrientation()
 	 * @post	De energie van de robot is verminderd met benodigde energie voor een draai.
 	 * 			|if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST)))
 	 * 			|	new.getEnergy().getEnergy().equals(this.getEnergy().getEnergy() - TURN_COST)
 	 */
 	public void turnCounterClockWise(){
 		if(isValidRobotEnergyAmount(new Energy(this.getEnergy().getEnergy() - TURN_COST))){
 			this.setOrientation(this.getOrientation().getCounterClockwiseOrientation());
 			this.getEnergy().setEnergy(this.getEnergy().getEnergy() - TURN_COST);
 		}
 	}
 	
 	/**
 	 * Deze methode beweegt de robot een stap vooruit indien mogelijk.
 	 * 
 	 * @effect	De positie van de robot is veranderd (indien het mogelijk was om te bewegen).
 	 */
 	public void move(){
 		//TODO
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
 		//TODO
 		return null;
 	}
 	
 	/**
 	 * Deze methode verplaatst de robot zo dicht mogelijk bij een andere robot.
 	 * 
 	 * @param	robot
 	 * 			De robot waar naartoe moet bewogen worden.
 	 */
 	public void moveNextTo(Robot robot){
 		//TODO
 	}
 	
 	/**
 	 * Deze methode doet een robot schieten met zijn laser.
 	 * 
 	 * @effect	Mogelijks wordt een object op het bord geraakt en verwijderd.
 	 * 
 	 * @throws	IllegalStateException
 	 * 			De robot staat niet op een bord.
 	 * 			|!this.isOnBoard()
 	 */
 	public void shoot() throws IllegalStateException{
 		if(this.isOnBoard()){
 			//TODO
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
 	 */
 	public void recharge(Energy energy){
 		//TODO
 		Energy newEnergy = Energy.energySum(this.getEnergy(), energy);
 		if(newEnergy.getEnergy() > MAXENERGY)
 			newEnergy = new Energy(MAXENERGY);
 		if(newEnergy.getEnergy() < MINENERGY)
 			newEnergy = new Energy(MINENERGY);
 		this.setEnergy(newEnergy);
 	}
 	
 	/**
 	 * Deze methode berekent de kost terug van 1 move.
 	 * 
 	 * @return	de kost terug van 1 move
 	 * 			|MOVE_COST + MOVE_COST_PER_KG*(this.getTotalWeight().getWeight() / 1000)
 	 */
 	public int moveCost(){
 		return MOVE_COST + MOVE_COST_PER_KG*(this.getTotalWeight().getWeight() / 1000);
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
 
 	public void use(Battery battery) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void drop(Battery battery) {
 		// TODO Auto-generated method stub
 	}
 
 }

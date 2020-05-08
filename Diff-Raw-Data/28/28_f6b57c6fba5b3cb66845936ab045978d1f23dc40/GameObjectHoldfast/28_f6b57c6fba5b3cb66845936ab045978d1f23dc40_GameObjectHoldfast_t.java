 package slug.soc.game.gameObjects;
 
 import java.awt.Color;
 
 import slug.soc.game.GameCalendar;
 import slug.soc.game.GameCalendarEvent;
 import slug.soc.game.RandomProvider;
 import slug.soc.game.gameObjects.tiles.faction.TileHoldfast;
 
 public class GameObjectHoldfast extends GameObject {
 
 	private int troopPopulation;
 	private int civPopulation;
 
 	public GameObjectHoldfast(Color color, Faction owner, int x, int y){
 		super(new TileHoldfast(color), owner, x, y);
 		troopPopulation = RandomProvider.getInstance().nextInt(1000) + 200;
 		civPopulation = RandomProvider.getInstance().nextInt(10000) + 1000;
 		if(owner != null){
 			dateCreated.addEvent(new GameCalendarEvent("The completion of work on " + owner.toString() + " holdfast.", this));
 		}
 	}
 
 	@Override
 	public String[] getStringDesc() {
 		String[] desc = new String[2];
 		desc[0] = owner.toString() + " holdfast";
 		desc[1] = "Property of the " + owner.toString() + " family";
 		return desc;
 	}
 
 	public String toString(){
 		return "Holdfast";
 	}
 
 	@Override
 	public String getDetailedDesc() {		
 		return owner.toString()  + " Holdfast belongs to the " + owner.toString() + " family(i). " + " It is located in " + location + ". " +
 				"Work was completed on " + dateCreated.toString() + "(b). It has a total population of " + (troopPopulation + civPopulation) + ", consisting of " + troopPopulation +
 				" soilders and " + civPopulation + " peasants.";
 	}
 
 	@Override
 	public void act() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public String getSpecialCondition() {
 		// TODO Auto-generated method stub
 		return "";
 	}
 
 	@Override
 	public String getName() {
 		return owner.toString() + " holdfast";
 	}
 
 }

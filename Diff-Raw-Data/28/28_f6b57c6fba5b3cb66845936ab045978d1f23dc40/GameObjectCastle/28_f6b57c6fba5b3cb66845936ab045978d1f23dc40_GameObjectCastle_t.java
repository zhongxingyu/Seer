 package slug.soc.game.gameObjects;
 
 import java.awt.Color;
 
 import slug.soc.game.GameCalendar;
 import slug.soc.game.GameCalendarEvent;
 import slug.soc.game.RandomProvider;
 import slug.soc.game.gameObjects.tiles.GameTile;
 import slug.soc.game.gameObjects.tiles.faction.TileCastle;
 
 public class GameObjectCastle extends GameObject {
 
 	private int troopPopulation;
 	private int civPopulation;
 
 	public GameObjectCastle(Color color, Faction owner, int x, int y) {
 		super(new TileCastle(color), owner, x, y);
 		if(owner != null){
 			troopPopulation = RandomProvider.getInstance().nextInt(5000) + 1000;
 			civPopulation = RandomProvider.getInstance().nextInt(10000) + 1000;
 
 			dateCreated.addEvent(new GameCalendarEvent("The complettion of work on castle" + owner.toString(), this));
 		}
 	}
 
 	public String[] getStringDesc(){
 		String[] desc = new String[3];
 		desc[0] = "Castle " + owner.toString();
 		desc[1] = "Property of the " + owner.toString() + " family";
 		desc[2] = "Number of troops : " + troopPopulation;
 		return desc;
 	}
 
 	public void act(){
 
 	}
 
 	public String toString(){
 		return "Castle";
 	}
 
 	public String getName(){
 		return "Castle " + owner.toString(); 
 	}
 
 	@Override
 	public String getDetailedDesc() {
 		return "Castle " + owner.toString() + " belongs to the " + owner.toString() + " family(i). " +
 				"It is located in " + location + ". " +
 				"Work was completed on " + dateCreated.toString() + ". The caste has a total population of " + (troopPopulation + civPopulation) + ", consisting of " + troopPopulation +
 				" soilders and " + civPopulation + " peasants.";
 	}
 
 	@Override
 	public String getSpecialCondition() {
 		// TODO Auto-generated method stub
 		return "";
 	}
 
 }

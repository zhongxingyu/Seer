 package slug.soc.game.gameObjects;
 
 import java.awt.Color;
 
 import slug.soc.game.GameCalendar;
 import slug.soc.game.GameCalendarEvent;
 import slug.soc.game.RandomProvider;
 import slug.soc.game.gameObjects.tiles.faction.TileTown;
 import slug.soc.game.gameState.GameModeState;
 import slug.soc.game.worldBuilding.WordGenerator;
 
 public class GameObjectTown extends GameObject {
 
 	//represented as 1 in X
 	private static final int CHANGE_OF_PLAGUE = 1000;
 
 	private boolean hasPlague;
 
 	private int troops;
 	private int population;
 	private String name;
 
 	public GameObjectTown(Color color, Faction owner, int x, int y) {
 		super(new TileTown(color), owner, x, y);
 		troops = RandomProvider.getInstance().nextInt(1000) +300;
 		population = RandomProvider.getInstance().nextInt(100000) + 10000;
 		name = WordGenerator.getInstance().getRandomPlaceName();
 		if(owner != null){
 			dateCreated.addEvent(new GameCalendarEvent("The founding of " + name, this));
			//GameCalendar.getInstance().addKeyDate(dateCreated);
 		}
 	}
 
 	@Override
 	public String[] getStringDesc() {	
 		String[] desc = new String[3];
 		desc[0] = "Town of " + name;
 		desc[1] = "Property of the " + owner.toString() + " family";
 		desc[2] = "Population : " + (population + troops);
 		return desc;
 	}
 
 	public String toString(){
 		return "Town";
 	}
 
 	@Override
 	public String getDetailedDesc() {
 		String string = "The town of " + name + " is owned by the " + owner + " family(i)." + " It is located in " + location + ". " +
 				"The town was founded on " + dateCreated.toString() + "(b). It has a population of " + (population + troops) + " with a militia of " + troops + "."
 				+ " About " + population/100 + " gold is colelcted in taxes each day.";
 				return string;
 	}
 
 	@Override
 	public void act() {
 		if(hasPlague){
 			if(population > 10){
 				population -= 10;
 			}
 			if(RandomProvider.getInstance().nextInt(100) == 1){
 				hasPlague = false;
 				hasSpecialCondition = false;
 				GameCalendar.getInstance().getCurrentDate().addEvent(new GameCalendarEvent("End of plague in " + name, this));
 				//GameCalendar.getInstance().addKeyDate(GameCalendar.getInstance().getCurrentDate());
 			}
 		}
 		else if(RandomProvider.getInstance().nextInt(1000) == 1){
 			hasPlague = true;
 			hasSpecialCondition = true;
 			GameCalendar.getInstance().getCurrentDate().addEvent(new GameCalendarEvent("Outbreak of plague in " + name,this));
 			//GameCalendar.getInstance().addKeyDate(GameCalendar.getInstance().getCurrentDate());
 			GameModeState.getInstance().addNotification("The town of " + name+ " has had an outbreak of plague.");
 		}
 		owner.changeMoney(population / 100);
 	}
 
 	@Override
 	public String getSpecialCondition() {
 		String out ="";
 		if(hasPlague){
 			out += "This town is currently suffering from a plague outbreak.";
 		}
 		return out;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 }

 package slug.soc.game.gameObjects;
 
 import java.awt.Color;
 
 import slug.soc.game.GameCalendar;
 import slug.soc.game.GameCalendarEvent;
 import slug.soc.game.RandomProvider;
 import slug.soc.game.gameObjects.tiles.faction.TileVillage;
 import slug.soc.game.worldBuilding.WordGenerator;
 
 public class GameObjectVillage extends GameObject {
 
 	private String name = "NAME_GOES_HERE";
 	private int troops;
 	private int population;
 
 	public GameObjectVillage(Color color, Faction owner, int x, int y){
 		super(new TileVillage(color), owner, x, y);
 		troops = RandomProvider.getInstance().nextInt(200) + 50;
 		population = RandomProvider.getInstance().nextInt(1000) + 200;
 		name = WordGenerator.getInstance().getRandomPlaceName();
 		if(owner != null){
 			dateCreated.addEvent(new GameCalendarEvent("The founding of " + name, this));
			GameCalendar.getInstance().addKeyDate(dateCreated);
 		}
 	}
 
 	@Override
 	public String[] getStringDesc() {
 		String[] desc = new String[3];
 		desc[0] = "Village of " + name;
 		desc[1] = "Property of the " + owner.toString() + " family";
 		desc[2] = "Population : " + (population + troops);
 		return desc;
 	}
 
 	public void act(){
 		owner.changeMoney(population/100);
 	}
 
 	@Override
 	public String getDetailedDesc() {
 		String string = "The village of " + name + " is owned by the " + owner + " family(i)." + " It is located in " + location + ". " +
 				"The village was founded on " + dateCreated.toString() +". It has a population of " + (population + troops) + " with a militia of " + troops + "."
 				+ " About " + population/100 + " gold is collected in taxes each day";
 		return string;
 	}
 
 	public String toString(){
 		return "Village";
 	}
 
 	@Override
 	public String getSpecialCondition() {
 		// TODO Auto-generated method stu
 		return "";
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 }

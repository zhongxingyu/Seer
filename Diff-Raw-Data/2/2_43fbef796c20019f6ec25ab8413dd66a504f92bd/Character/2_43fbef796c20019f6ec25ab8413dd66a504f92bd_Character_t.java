 package game;
 
 /**
  * The character class represents the entity that different players control.  It has an inventory, location, difficulty and race.
  * @author grant
  * @author
  */
 public class Character 
 {
 	private Inventory inventory;
 	private Plot centerPlot;
 	private static final int[] beginner={8,4,0};
 	private static final int[] standard={4,2,0};
 	private static final int[] tournament={4,2,0};
 	private static final int flapperStart=1600;
 	private static final int humanStart=600;
 	private static final int otherStart=1000;
 
 	/**
 	 *The Character paramaterless constructor  sets the difficulty to standard and start to human.
 	 *
 	 *
 	 */
 	public Character() {
 		this(2,2);
 	}
 	/**
 	 *The Character constructor sets the starting inventory for a given difficultry and race.
 	 *
 	 *
 	 *@param difficulty-the difficulty for the Character
 	 *@param start - the race for the Character
 	 *
 	 */
 	public Character( int difficulty, int start) 
 	{
		
 		inventory = new Inventory();
 		switch(difficulty) {
 			case 1:
 				inventory.changeFood(beginner[1]);
 				inventory.changeEnergy(beginner[2]);
 				inventory.changeOre(beginner[3]);
 				break;
 			case 3: 
 				inventory.changeFood(tournament[1]);
 				inventory.changeEnergy(tournament[2]);
 				inventory.changeOre(tournament[3]);
 				break;
 			default:	
 				inventory.changeFood(standard[1]);
 				inventory.changeEnergy(standard[2]);
 				inventory.changeOre(standard[3]);
 			
 		}
 		if(start==1){
 			inventory.changeMoney(flapperStart);
 		}else if(start==2) {
 			inventory.changeMoney(humanStart);
 		}else {
 			inventory.changeMoney(otherStart);
 		}
 	}
 	
 	public void update()
 	{
 		
 	}
 	
 	public void moveLeft()
 	{
 	}
 	
 	public void moveRight()
 	{
 	}
 	
 	public void moveUp()
 	{
 	}
 	
 	public void moveDown()
 	{
 	}
 	
 	public void act()
 	{
 	}
 }

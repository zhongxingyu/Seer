 package game;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 
 import ui.render.Render;
 import ui.render.Renderable;
 
 /**
  * The character class represents the entity that different players control.
  */
 public class Player implements Renderable
 {	
 	public static final int WIDTH = 13;
 	public static final int HEIGHT = 19;
 	
 	public static int MOVEMENT_SPEED = 2;  
 	
 	private Point location;
 	private Point oldLocation;
 	
 	private String name;
 	private Color color;
 	private PlayerType type;
 	
 	private Inventory inventory;
 	private Follower follower;
 	
 	private String id;
 	
 	private Render render;
 
 	/**
 	 * Create a default player
 	 */
 	public Player() 
 	{		
 		inventory = new Inventory();
                 
 		setType(PlayerType.HUMAN);
 		setDifficulty(Difficulty.BEGINNER);
 
 		location = new Point(0, 0);
 		oldLocation = new Point(0, 0);
 		
 		render = new Render();
 		render.x = location.x;
 		render.y = location.y;
 		render.width = Player.WIDTH;
 		render.height = Player.HEIGHT;
 		render.addImage("assets/images/player/human.png");
 	}
 	
 	/**
 	 * Create a player without filling inventory with defaults.
 	 * Side effects from 'setDifficulty(...)' and 'setType(...)' forced me to do this.
 	 */
 	public Player(boolean fillInventory)
 	{
        if (!fillInventory)
         {
 		    inventory = new Inventory();
 
 		    location = new Point(0, 0);
 		    oldLocation = new Point(0, 0);
 		
 		    render = new Render();
 		    render.x = location.x;
 		    render.y = location.y;
 		    render.width = Player.WIDTH;
 		    render.height = Player.HEIGHT;
 		    render.addImage("assets/images/player/human.png");
         }
 	}
 	
 	public Player(Player player)
 	{
 		this.location = new Point();
 		this.location.x = player.location.x;
 		this.location.y = player.location.y; 
 
 		this.oldLocation = new Point();
 		this.oldLocation.x = player.oldLocation.x;
 		this.oldLocation.y = player.oldLocation.y; 
 		
 		this.render = player.render;
 
 		this.name = player.name;
 		this.color = player.color;
 		this.type = player.type;
 		this.follower = player.follower;
 		this.id = player.id;
 		
 		this.inventory = new Inventory(player.inventory);
 	}
 	
 	/** 
 	 * Get the player's id
 	 * @return The id
 	 */
 	public String getId()
 	{
 		return id;
 	}
 	
 	/**
 	 * Set the player's id
 	 * @param id The id
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 	
 	/**
 	 * Get the player's type
 	 * @return The type
 	 */
 	public PlayerType getType()
 	{
 		return type;
 	}
 	
 	/**
 	 * Set the player's type
 	 * @param The type
 	 */
 	public void setType(PlayerType type)
 	{
 		this.type = type;
 		inventory.money = type.getMoney();
 	}
 	
 	/**
 	 * Get the player's score
 	 * @return The player's score
 	 */
     public double getScore()
     {
         return inventory.getScore();
     }
     
 	/**
 	 * Update the player
 	 */
 	public void update()
 	{
 		if (follower != null)
 		{
 			follower.update();
 		}
 		
 		oldLocation.x = location.x;
 		oldLocation.y = location.y;
 	}
 	
 	/**
 	 * Sets food and energy levels for the corresponding difficulty
 	 * @param difficulty one of the options in the Difficulty enum
 	 */
 	public void setDifficulty(Difficulty difficulty)
 	{
 		inventory.food = Difficulty.getStartingFood(difficulty);
 		inventory.energy = Difficulty.getStartingEnergy(difficulty);
 	}
 	
 	/**
 	 * Add a plot to the player
 	 * @param plotId The a plot to the player
 	 */
     public void addPlot(String plotId)
     {
         inventory.ownedPlotIds.add(plotId);
     }
             
     /**
      * Get A list of plot ids owned by the player
      * @return A list of plot ids owned by the player
      */
     public ArrayList<String> getPlotIds() 
     {
         return inventory.ownedPlotIds;
     }
         
     /**
      * Set the player's name
      * @param name The player's name
      */
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	/**
 	 * Increment the players amount of ore
 	 * @param ore The amount of ore
 	 */
 	public void incrementOre(int ore)
 	{
 		inventory.ore += ore;
 	}
 
 	/**
 	 * Increment the players amount of crystite
 	 * @param crystite The amount of crystite
 	 */
 	public void incrementCrystite(int crystite)
 	{
 		inventory.crystite += crystite;
         if(inventory.crystite<0){
             inventory.crystite = 0;
         }
 	}
 
 	/**
 	 * Increment the players amount of food
 	 * @param food The amount of food
 	 */
 	public void incrementFood(int food)
 	{
 		inventory.food += food;
         if(inventory.food<0){
             inventory.food = 0;
         }
 	}
 
 	/**
 	 * Increment the players amount of energy
 	 * @param energy The amount of energy
 	 */
 	public void incrementEnergy(int energy)
 	{
 		inventory.energy += energy;
         if(inventory.energy<0){
             inventory.energy = 0;
         }
 	}
 
 	/**
 	 * Increment the players amount of money
 	 * @param money The amount of money
 	 */
 	public void incrementMoney(int money)
 	{
 		inventory.money += money;
         if (inventory.money < 0)
         {
             inventory.money = 0;
         }
 	}
 	
 	/**
 	 * Check if a player can buy an item at a specific cost
 	 * @param cost The cost
 	 * @return Whether the player can purchase the item
 	 */
 	public boolean checkBuy(int cost)
 	{
 		return cost >= inventory.money;
 	}
 	
 	public boolean checkSell(String resource, int sellAmount)
 	{
 		boolean canSell = false;
 		switch (resource) 
 		{
 			case "ore":
 				canSell = inventory.ore >= sellAmount;
 				break;
 			case "crystite":
 				canSell = inventory.crystite >= sellAmount;
 				break;
 			case "food":
 				canSell = inventory.food >= sellAmount;
 				break;
 			case "energy":
 				canSell = inventory.energy >= sellAmount;
 				break;
 		}
 		return canSell;
 	}
 	
 	public void sellResource(String resource, int quantity, int price)
 	{
 		switch (resource) {
 			case "ore":
 				incrementOre(-quantity);
 				incrementMoney(price * quantity);
 				break;
 			case "crystite":
 				incrementCrystite(-quantity);
 				incrementMoney(price * quantity);
 				break;
 			case "food":
 				incrementFood(-quantity);
 				incrementMoney(price * quantity);
 				break;
 			case "energy":
 				incrementEnergy(-quantity);
 				incrementMoney(price * quantity);
 				break;
 		}
 	}
 	
 	public void buyResource(String resource, int quantity, int price) 
 	{
 		switch (resource) {
 		case "ore":
 			incrementOre(quantity);
 			incrementMoney( -(price * quantity) );
 			break;
 		case "crystite":
 			incrementCrystite(quantity);
 			incrementMoney(- (price * quantity));
 			break;
 		case "food":
 			incrementFood(quantity);
 			incrementMoney( -(price * quantity));
 			break;
 		case "energy":
 			incrementEnergy(quantity);
 			incrementMoney( -(price * quantity) );
 			break;
 		}
 	}
 		
 	public String getName()
 	{
 		return name;
 	}
 	
 	public int getMoney()
 	{
 		return inventory.money;
 	}
 	
 	public int getOre() 
 	{
 		return inventory.ore;
 	}
 	
 	public int getFood()
 	{
 		return inventory.food;
 	}
 	
 	public int getCrystite()
 	{
 		return inventory.crystite;
 	}
 	
 	public int getEnergy()
 	{
 		return inventory.energy;
 	}
 	
 	public void setMoney(int amount) 
 	{
 		inventory.money = amount;
 	}
 	
 	public void setColor(Color color)
 	{
 		this.color = color;
 	}
 	
 	public Color getColor()
 	{
 		return color;
 	}
 
 	public void setX(int x)
 	{
 		location.x = x;
 	}
 
 	public int getX()
 	{
 		return (int)location.getX();
 	}
 
 	public void setY(int y)
 	{
 		location.y = y;
 	}
 
 	public int getY()
 	{
 		return (int)location.getY();
 	}
 	
 	public int getOldX()
 	{
 		return (int) oldLocation.getX();
 	}
 	
 	public int getOldY() 
 	{
 		return (int) oldLocation.getY();
 	}
 	
 	public void applyForce(int x, int y)
 	{
 		location.x += x;
 		location.y += y;
 	}
 	
 	public void setFollower(Follower follower)
 	{
 		this.follower = follower;
 	}
 	
 	public Follower getFollower()
 	{
 		return follower;
 	}
 	
 	public Render getRender()
 	{
 		render.x = location.x;
 		render.y = location.y;
 		return render;
 	}
 }

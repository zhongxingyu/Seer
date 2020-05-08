 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.event.ILevelListener;
 import ca.couchware.wezzle2d.event.LevelEvent;
 import ca.couchware.wezzle2d.manager.PropertyManager.Key;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.util.Util;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * Manages the world.
  * 
  * @author kgrad
  *
  */
 
 public class WorldManager implements ILevelListener
 {       
 	//--------------------------------------------------------------------------
 	// Instance Members
 	//--------------------------------------------------------------------------
 	
     /**
 	 * The property manager.
 	 */
 	private final PropertyManager propertyMan;
     
 	/**
 	 * The current level
 	 */
 	private int level;
     
 	/**
 	 * The item list.
 	 */
 	private LinkedList<Item> itemList;
         
         /**
          * The multiplier list
          */
         private ArrayList<Item> multList;
         
     
     /**
      * The master rule list.  Contains all the rules that should exist
      * at the start of a new game.
      */
     private LinkedList<Rule> masterRuleList;
     
     /**
      * The current rule list.  Contains all the rules that have not yet 
      * been realized for the current game.
      */
     private LinkedList<Rule> currentRuleList;
     
     /**
      * Is a game in progress?
      */
     private boolean gameInProgress = false;
     
 	/**
 	 * The difficulty level.
 	 */
 	private int difficulty;	
     
     /**
      * The maximum items on the screen at once.
      */
     private int maxItems;
     
     /**
      * The maximum multipliers on the screen at once.
      */
     private int maxMults;
     
     /**
      * The percentage of tiles to maintain.
      */
     private int tileRatio = 80;
     
     /**
      * The minimum drop.
      */
     private int minimumDrop = 1;
     
     /**
      * The number of pieces to drop in concurrently.
      */
     private int parallelDropInAmount = 4;
     
     /**
      * The minimum timer value.
      */
     private int timerMin = 5;
     
     /**
      * The initial timer value.
      */	
     private int initialTimer = 15;
         
     /**
      * The level at which the difficulty begins to increase.
      */
     private int difficultyIncreaseLevel = 9;
     
     /**
      * The number of levels before the difficulty level increases.
      */
     private int levelDifficultySpeed = 3;
     
     //--------------------------------------------------------------------------
 	// Constructor
 	//--------------------------------------------------------------------------
     
 	/**
 	 * The constructor.
 	 * @param fragment
 	 * @param board
 	 * @param scoreManager
 	 */
 	private WorldManager(final PropertyManager propertyMan)
 	{						
 		// Store a reference to the property manager.
 		this.propertyMan = propertyMan;
 		
 		// Load the properties;
 		this.difficulty = propertyMan.getIntProperty(Key.DIFFICULTY);
 		
 		// Ensure that we are at least level 1.
 		if (this.difficulty < 1)
 			this.difficulty = 1;
 				
 		// Set the starting level.
 		setLevel(1);
         
         // Set the max items and mults.
         this.maxItems = 3;
         this.maxMults = 3;
         
         // Set the items.
 	itemList = new LinkedList<Item>();
 	itemList.add(new Item(TileType.NORMAL, 28, 5, 100));
         
         // Set the multipliers.
         multList = new ArrayList<Item>();
         
 	multList.add(new Item(TileType.X2, 2, 50, 3));
         multList.add(new Item(TileType.X3, 0, 20, 1));
         multList.add(new Item(TileType.X4, 0, 10, 1));
         //multList.add(new Item(TileType.NORMAL, 0, 5, 100));
         
         // Set the rules.
         masterRuleList = new LinkedList<Rule>();
         
         // Make it so the rocket block is added on level 4.
         masterRuleList.add(new Rule(Rule.Type.LEVEL, Rule.Operation.EQ, 4)
         {
             @Override
             public void onMatch()
             {
                 // Increase the number of colours.
                 itemList.add(new Item(TileType.ROCKET, 1, 55, 3));
             }            
         });  
         
         // Make it so the bomb block is added on level 8.
         masterRuleList.add(new Rule(Rule.Type.LEVEL, Rule.Operation.EQ, 8)
         {
             @Override
             public void onMatch()
             {
                 // Increase the number of colours.
                 itemList.add(new Item(TileType.BOMB, 1, 10, 1));
             }            
         });   
         
         // Make it so the star block is added on level 12.
         masterRuleList.add(new Rule(Rule.Type.LEVEL, Rule.Operation.EQ, 12)
         {
             @Override
             public void onMatch()
             {
                 // Increase the number of colours.
                 itemList.add(new Item(TileType.STAR, 0, 5, 1));
             }            
         });   
         
         // Make it so a new block color is added on level 5.
 //        masterRuleList.add(new Rule(Rule.Type.LEVEL, Rule.Operation.EQ, 5)
 //        {            
 //            @Override
 //            public void performAction(Game game)
 //            {
 //                // Increase the number of colours.
 //                game.boardMan.setNumberOfColors(6);
 //            }            
 //        });
         
         
         
         currentRuleList = new LinkedList<Rule>();
         currentRuleList.addAll(masterRuleList);
 	}
 	
         
         // Public API
         
         public static WorldManager newInstance(final PropertyManager propMan)
         {
             return new WorldManager(propMan);
         }
     //--------------------------------------------------------------------------
 	// Instance Methods
 	//--------------------------------------------------------------------------
     
     /**
 	 * A method to generate a target score given the level. 
 	 * 
 	 * @param currentLevel The level to generate the score for.
 	 * @return The score.
 	 */
 	public int generateTargetLevelScore(int currentLevel)
 	{
 		return currentLevel * 1200;
 	}
         
     public int generateTargetLevelScore()
     {
         return generateTargetLevelScore(level);
     }     
     
     public void levelUp(final Game game)
     {        
         this.incrementCurrentLevel();
         
         int currentLevelScore = game.scoreMan.getLevelScore() - 
                 game.scoreMan.getTargetLevelScore();
         int targetLevelScore = generateTargetLevelScore(level);
         
         if(currentLevelScore > targetLevelScore / 2)
             currentLevelScore = targetLevelScore / 2;
         
         game.scoreMan.setLevelScore(currentLevelScore);        
 		game.scoreMan.setTargetLevelScore(targetLevelScore);
         
         game.progressBar.setProgressMax(game.scoreMan.getTargetLevelScore());
                
         // Change the timer.
         int time = game.timerMan.getInitialTime();
         
         if(time > this.timerMin)
             time--;
         
         game.timerMan.setInitialTime(time);
     }
     
     /**
      * Calculates the number of tiles to drop.
      * 
      * @param game The game.
      * @param pieceSize The size of the piece consumed.
      * @return The number of tiles do drop.
      */
     public int calculateDropNumber(final Game game, int pieceSize)
     {
         float tiles = game.boardMan.getNumberOfTiles();
         float totalSpots = game.boardMan.getColumns() * game.boardMan.getRows();
         
         // The number of tiles for the current level.
         int levelDrop = (this.level / this.levelDifficultySpeed);
         
         // Check for difficulty ramp up.
         if (this.level > this.difficultyIncreaseLevel)
             levelDrop = (this.difficultyIncreaseLevel / this.levelDifficultySpeed);
         
         // The percent of the board to readd.
         int  boardPercentage = (int)((totalSpots - tiles) * 0.1f); 
         
         // We are low. drop in a percentage of the tiles, increasing if there 
         // are fewer tiles.
         if ((tiles / totalSpots) * 100 < this.tileRatio)
         {
             // If we are past the level ramp up point, drop in more.
             if (this.level > this.difficultyIncreaseLevel)
             {
                   return pieceSize +  levelDrop 
                     + (this.level - this.difficultyIncreaseLevel) 
                     + boardPercentage + this.minimumDrop;
             }
             else
             {
                 return pieceSize + levelDrop + boardPercentage 
                         + this.minimumDrop;
             }
         }
         else
         {
             // If we are past the level ramp up point, drop in more.
             if (this.level > this.difficultyIncreaseLevel)
             {
                 return pieceSize + levelDrop
                     + (this.level - this.difficultyIncreaseLevel) 
                     + this.minimumDrop;
             }
             else
                 return pieceSize + levelDrop + this.minimumDrop;
         }
     }  
     
     /**
      * Restarts the board manager to appropriate settings for the first level.
      */
     public void restart()
     {
         // Reset to level 1.
         this.setLevel(1);
         
         // Reset the rules.
         currentRuleList.clear();
         currentRuleList.addAll(masterRuleList);
     }
     
     //--------------------------------------------------------------------------
     // Logic
     //--------------------------------------------------------------------------
     
     public void updateLogic(final Game game)
     {       
         // If the board is refactoring, do not logicify.
         if (game.isBusy() == true)
              return;
         
         // See if we need to create a new board.
         if (isGameInProgress() == false 
                 && game.tutorialMan.isTutorialInProgress() == false)
         {
             // Game is now in progress.
             //setGameInProgress(true);
             
             // Generate the game board.            
             //game.boardMan.clearBoard();            
             //game.startBoardShowAnimation();
         }                
         
         for (Iterator<Rule> it = currentRuleList.iterator(); it.hasNext(); )
         {
             Rule rule = it.next();
             
             if (rule.evaluate(game) == true)
             {
                 rule.onMatch();
                 it.remove();
             }
         }            
     }
    
     //--------------------------------------------------------------------------
 	// Getters and Setters
 	//--------------------------------------------------------------------------
     
     /**
      * Is a game currently in progress?
      * 
      * @return True if a game is in progress, false otherwise.
      */
     public boolean isGameInProgress()
     {
         return gameInProgress;
     }
 
     public void setGameInProgress(boolean gameInProgress)
     {
         this.gameInProgress = gameInProgress;
     }        
     
 	/**
      * Get the current level.
      * 
 	 * @return the currentLevel
 	 */
 	public int getLevel()
 	{
 		return level;
 	}	
 	
 	/**
 	 * @param currentLevel the currentLevel to set
 	 */
 	public void setLevel(int level)
 	{
 		// Set the level.
 		this.level = level;								
 	}	
 	
     /**
      * @return the maximum number of items.
      */
     public int getMaxItems()
     {
         return maxItems;
     }
     
     /**
      * set the maximum number of items.
      */
     public void setMaxItems(int maxItems)
     {
         this.maxItems = maxItems;
     }    
     
     
      /**
      * @return the maximum number of mults.
      */
     public int getMaxMults()
     {
         return maxMults;
     }
     
     /**
      * set the maximum number of mults.
      */
     public void setMaxMults(int maxMults)
     {
         this.maxMults = maxMults;
     }    
     
     
     /**
      * Get the initial timer value.
      * @return The initial timer.
      */
     public int getInitialTimer()
     {
         return initialTimer;
     }        
     
 	/**
 	 * Increment the level.
 	 */
 	public void incrementCurrentLevel()
 	{
 		// Increment initial amount of normal tiles.		
 		getItem(0).incrementInitialAmount();
 		
 		// Increment the level.
 		setLevel(level + 1);
 	}	
 	            
     /**
      * Get the parallel drop in amount.
      * @return The amount.
      */
     public int getParallelTileDropInAmount()
     {
         return this.parallelDropInAmount;
     }
     
 	/**
 	 * Get the descriptor for the item at the specified index.
 	 * 
 	 * @param index The index.
 	 * @return The ItemDescriptor.
 	 */
 	public Item getItem(int index)
 	{
 		return itemList.get(index);
 	}
 	
 	/**
 	 * Get the descriptor for the item with the specified class.
 	 * Returns null if the class does not exist.
 	 * 
 	 * @param type
 	 * @return 
 	 */
 	public Item getItem(TileType type)
 	{
 		for (Item item : itemList)
         {
 			if (item.getTileType() == type)
 				return item;
         }
 		
 		return null;
 	}
        
     /**
      * Gets a random item.
      * 
      * @return 
      */
     public Item getItem( int numItems, int numMults)
 	{	
         
             //check if we miss.
             int test = Util.random.nextInt(100);
             if( test <= 5)
             {
                return new Item(TileType.NORMAL, 0, 0, 0);
             }
             ArrayList<Item> items = new ArrayList<Item>();
             
                 if (numItems < maxItems)
                 {
                     for (Item item : itemList)
                     {
                        if (item.getProbability() > 0 && item.getTileType() != TileType.NORMAL)
                             items.add(item);
                     }
                 }
                 if (numMults < maxMults)
                 {  
                     for (Item item : multList)
                     {
                         if (item.getProbability() > 0)
                             items.add(item);
                     }
                 }
		// Create an array representing the item distribution.
 		int dist[] = new int[items.size() + 1];
 		dist[0] = 0;
 		
 		// Determine the distribution.
 		int i = 1;
 		for (Item item : items)
 		{            
 			if (item.getProbability() == -1)
 				dist[i] = dist[i - 1];
 			else			
 				dist[i] = dist[i - 1] + item.getProbability();
 			
 			i++;
 		}
 		
 		// Pick a random number between 0 and dist[dist.length - 1].
 		int randomNumber = Util.random.nextInt(dist[dist.length - 1]);
 		
 		for (int j = 1; j < dist.length; j++)
 		{
 			if (randomNumber < dist[j])
                         {
 				return items.get(j - 1);
                         }
 		}
 		
 		// We should never get here.
 		LogManager.recordWarning(
                 "Random number out of range! (" + randomNumber + ").", 
                 "WorldManager#getItem");
         
 		return items.get(0);
 	}
     
     /**
 	 * @return The items and mults
 	 */
 	public LinkedList<Item> getItemList()
 	{
             LinkedList<Item> items = new LinkedList<Item>();
             
             for (Item item : itemList)
                 items.add(item);
             for(Item item: multList)
                 items.add(item);
                     
 		return items;
 	}               	
                 
     public void handleLevelEvent(LevelEvent e)
     {
         for (int i = 0; i < e.getLevelChange(); i++)
             this.levelUp(e.getGame());
     }
 	
 }

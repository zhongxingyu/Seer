 /*
  * BlockScape
  * Written and Developed by Rock
  * Help from Darth
  */
 
 
 package net.blockscape;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.IOException;
 import java.net.URL;
 
 import net.blockscape.block.Block;
 import net.blockscape.gui.Button;
 import net.blockscape.gui.OptionsScreenEnum;
 import net.blockscape.helper.*;
 import net.blockscape.lib.MainReference;
 import net.blockscape.registry.GameRegistry;
 import net.blockscape.save.SaveData;
 import net.blockscape.save.WorldSave;
 import net.blockscape.world.World;
 import net.blockscape.world.WorldBlock;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 public class BlockScape extends PApplet
 {
 	private static final long serialVersionUID = -1390024970025652247L; //Dunno
 	
 	public static float distBetweenPlayerAndMouse; //The distance between the player and the user's mouse
 	
 	public static int saveDisplayCounter = 100;
 	
 	public static OptionsScreenEnum screenSelected;
 	
 	//Booleans
 	public static boolean canPlaceOrRemoveBlock;
 	public boolean ground;
 	//public static boolean isPaused;
 	//public static boolean isStartMenu;
 	public static boolean isFlyMode;
 	public static boolean isSaving;
 	
 	//Fonts
 	public static PFont buttonFont;
 	public static PFont inGameFont;
 	public static PFont titleFont;
 	
 	//Buttons
 	public static Button returnToGame;
 	public static Button exitGame;
 	public static Button flyMode;
 	
 	
 	/**
      * @param args
      */
     public static void main(String[] args)
     {
         PApplet.main(new String[]{"net.blockscape.BlockScape"});
     }
     
 	/**
 	 * Called on game start
 	 */
 	public void setup()
 	{
 	    buttonFont = createFont("Arial", 24, true);
 	    inGameFont = createFont("Arial", 14, true);
 	    titleFont = loadFont("AtomicAge-48.vlw");
 	    
 	    ground = false;
 	    isFlyMode = false;
 	    screenSelected = OptionsScreenEnum.noScreen; //TODO Main Screen
 	    
 	    returnToGame = new Button(540, 360, 200, 70, "Return To Game", true, buttonFont, this);
 	    exitGame = new Button(540, 500, 200, 70, "Exit Game", true, buttonFont, this);
 	    flyMode = new Button(540, 220, 200, 70, "Fly Mode: Off", true, buttonFont, this);
         
 	    LogHelper.init();
 	    SaveData.initDirectory();
 	    IconHelper.init(this);
 	    Player.initPlayer(width/2, 0, this);
 	    GameRegistry.initialize();
        Block.blockInit();
 		
         
 		size(1280,720);
 		
 		if(frame != null)
 		{
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			URL icon = classLoader.getResource(MainReference.ICON_LOCATION);
 			frame.setTitle(MainReference.GAME_NAME);
 			frame.setIconImage(getToolkit().getImage(icon));
 		}
 		
 		frameRate(MainReference.FRAME_RATE);
 		
 		//TODO Menu generation
 		generateNewWorld("testWorld2", this);
 		
 		addMouseWheelListener(new MouseWheelListener() { public void mouseWheelMoved(MouseWheelEvent mwe) { mouseWheel(mwe.getWheelRotation()); }});
 	}
 	
 	/**
 	 * main game loop
 	 */
 	public void draw()
 	{
 	    if (screenSelected == OptionsScreenEnum.noScreen)
 	    {
 	        isSaving = true;
 	        saveDisplayCounter = 100;
 	        
 	        background(100, 100, 255);
 	        DrawingAndLogicHelper.drawGame(this);
 	    }
 	    else if (screenSelected == OptionsScreenEnum.mainPause)
 	    {
 	        background(100, 100, 175);
 	        DrawingAndLogicHelper.drawPauseMenu(this);
 	        
	        if (saveDisplayCounter == 100)
 	        {
 	            SaveData.saveGame(new WorldSave("testWorld2", World.getWorld()));
 	        }
 	        
	        if (--saveDisplayCounter < 0)
	            isSaving = false;
	        
 	    }
 	}
 	
 	/**
 	 * called when a key is pressed
 	 */
 	public void keyPressed()
 	{
 	    if (screenSelected != OptionsScreenEnum.noScreen)
 	    {
 	        key = 0;
 	        return;
 	    }
 	    
     	if (key == ' ' && ground)
     	    Player.setYvelocity(-3);
     	if (key == 'a')
     	    Player.left = true;
     	if (key == 'd')
     	    Player.right = true;
     	if (key == 'w' && isFlyMode)
             Player.up = true;
     	if (key == 's' && isFlyMode)
             Player.down = true;
     	if (key == ENTER)
     	{
     		Player.setX(mouseX);
     		Player.setY(mouseY);
     	}
     	if (key == 'r' && !World.isBlockLocationOpen(mouseX / 16, (height - mouseY) / 16))
     	{
     	    WorldBlock block = World.getBlock(mouseX / 16, (height - mouseY) / 16);
             
             if (block != null)
             {
                 selectedBlockID = block.getBlock().blockID;
                 selectedBlock = block.getBlock();
             }
     	}
     	
     	if (key == ESC)
     	{
     	    key = 0;
     	    screenSelected = OptionsScreenEnum.mainPause;
     	}
 	}
 	
 	/**
 	 * called when a key is released
 	 */
 	public void keyReleased()
 	{
     	if (key=='a')
     	    Player.left = false;
     	if (key=='d')
     	    Player.right = false;
     	if (key == 'w')
             Player.up = false;
         if (key == 's')
             Player.down = false;
     	if (key=='l')
     	{
     		try
     		{
     			//read data and set the respective things in-game
 				World.setWorld(SaveData.getWorldSaveData("testWorld2", this));
 	            Player.setX(SaveData.getPlayerX("testWorld2"));
 	            Player.setY(SaveData.getPlayerY("testWorld2"));
 			}
     		catch (IOException e)
 			{
 				e.printStackTrace();
 			}
     	}
 	}
 	
 	
 	//Mouse Wheel Constants
 	public final int mwUP = -1;
 	public final int mwDWN = 1;
 	public static int selectedBlockID = 1;
 	public static Block selectedBlock = Block.blockStone; //Default Block
 	
 	/**
 	 * called when the mouse wheel is used
 	 * @param delta up (-1) down (1)
 	 */
 	public void mouseWheel(int delta)
 	{
 		  
 	    if (delta == mwUP)
 	    {
 	        selectedBlockID++;
 	        
 	        if (selectedBlockID > GameRegistry.getBlockRegistrySize())
 	            selectedBlockID = 1;
 	    }
 	    
 	    if (delta == mwDWN)
 	    {
 	        selectedBlockID--;
 	        
 	        if (selectedBlockID < 1)
 	            selectedBlockID = GameRegistry.getBlockRegistrySize();
 	    }
 	    
 	    selectedBlock = GameRegistry.getBlock(selectedBlockID);
 	}
 	
 	public void mouseReleased()
     {
         if (BlockScape.flyMode.held)
             isFlyMode = !isFlyMode;
         if (BlockScape.returnToGame.held)
             screenSelected = OptionsScreenEnum.noScreen;
         if (BlockScape.exitGame.held)
             BlockScape.endgame();
     }
 	
 	public static void setOptionsScreen(OptionsScreenEnum screenSelected_)
 	{
 	    screenSelected = screenSelected_;
 	}
 	
 	public static void clearOptionsScreen()
 	{
 	    screenSelected = OptionsScreenEnum.noScreen;
 	}
 	
 	public static void endgame()
 	{
 	    System.exit(0);
 	}
 	
 	public static void generateNewWorld(String name, PApplet host)
 	{
 	    World.initBlankWorld();
         SaveData.addWorld(new WorldSave(name, World.getWorld()));
         TerrainGenerationHelper.generateWorld(host);
 	}
 }

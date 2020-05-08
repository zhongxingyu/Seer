 package client;
 
 import hardware.Keys;
 import hardware.MouseInput;
 import io.ChunkManager;
 import items.Item;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import render.MainMenu;
 import render.Render;
 import render.RenderGlobal;
 import render.RenderMenu;
 import spells.Spell;
 import utils.ErrorUtils;
 import utils.FileManager;
 import utils.ItemStack;
 import utils.LightUtils;
 import world.World;
 import world.WorldHell;
 import world.WorldSky;
 import audio.SoundEngine;
 import blocks.Block;
 import entities.EntityPlayer;
 import enums.EnumPlayerDifficulty;
 import enums.EnumWorldDifficulty;
 
 /**
  * <code>GameEngine</code> is the class responsible for running the main game loop, and other core features of multiple worlds.
  * <code>GameEngine</code> defines 4 important methods. 
  * <br><br>
  * The method {@link #startGame(World, EntityPlayer)} defines the method to actually start the game with the specified World object
  * and player. This will close the main menu and begin rendering based on the Chunk data. 
  * <br><br>
  * Most of the application's life cycle is spent in {@link #run()} method, which contains the main game loop. This handles
  * everything from the main menu to the game, and save menu. Before exiting this method, {@link #saveSettings()} will be called
  * to update any settings changed during runtime. 
  * <br><br>
  * Additional methods of interest: <br>
  * {@link #changeWorld(int)}, {@link #closeGameToMenu()}
  *   
  * 
  * @author      Alec Sobeck
  * @author      Matthew Robertson
  * @version     1.0
  * @since       1.0
  */
 public class GameEngine 
 {
 	/** The number of game ticks per second - this will always be 20 */
 	public static final int TICKS_PER_SECOND = 20;
 	/** In single player one render mode can be active. This decides what world is updated, loaded, and rendered. The names
 	 * correspond directly to that respective world. */
 	public final static int RENDER_MODE_WORLD_EARTH = 1,
 							   RENDER_MODE_WORLD_HELL = 2,
 							   RENDER_MODE_WORLD_SKY  = 3;
 	/** The currently selected RENDER_MODE_WORLD value, detailing what world to update, load, and render.*/
 	private int renderMode;
 	//private GuiMainMenu mainMenu;
 	private MainMenu mainMenu;
 	private World world;
 	private WorldHell worldHell;
 	private WorldSky worldSky;
 	private EntityPlayer player;
 	private Settings settings;
 	private SoundEngine soundEngine;
 	private RenderMenu renderMenu;
 	public ChunkManager chunkManager;
 	private String universeName;
 	
 	/**
 	 * Creates a new instance of GameEngine. This includes setting the renderMode to RENDER_MODE_WORLD_EARTH
 	 * and loading the settings object from disk. If a settings object cannot be found a new one is created. 
 	 */
 
 	public GameEngine()
 	{
 		renderMode = RENDER_MODE_WORLD_EARTH;
 		try 
 		{
 			loadSettings();
 		}
 		catch (IOException e) 
 		{
 			settings = new Settings();
 		}
 		catch (ClassNotFoundException e) 
 		{
 			settings = new Settings();
 		}
 	}
 	
 	public void run()
 	{
 		try
 		{
 			//Variables for the gameloop cap (20 times / second)
 	        
 			final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
 			final int MAX_FRAMESKIP = 5;
 			long next_game_tick = System.currentTimeMillis();
 			long start, end;
 			long fps = 0;
 			int loops;
 			start = System.currentTimeMillis();
 			
 		    while(!TerraeRasa.done) //Main Game Loop
 		    {
 		        loops = 0;
 		        while(System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) //Update the game 20 times/second 
 		        {
 		        	if(player != null && player.defeated)
 		        	{
 		        		hardcoreDeath();
 		        	}
 		        	
 		        	Keys.universalKeyboard(world, player, settings, settings.keybinds);
 		        	
 		        	//If the music is already playing,set the volume to whatever the settings say it is
 		        	if (soundEngine.getCurrentMusic() != null){
 		        		soundEngine.setVolume((float) settings.musicVolume);
 		        		//System.out.println("Settings: " + settings.volume + " Music: " + soundEngine.getVolume());
 		        	}
 		        	
 		        	if(settings.menuOpen) //Ingame pause menu - distribute keyboard/mouse input appropriately
 		        	{ 
 		        		soundEngine.setCurrentMusic("Main Music");
 		        		renderMenu.mouse(settings);
 		        		renderMenu.keyboard(settings);
 		        	}
 		        	else if(!TerraeRasa.isMainMenuOpen) //Handle game inputs if the main menu isnt open (aka the game is being played)
 		        	{
 		        		soundEngine.setCurrentMusic("Pause Music");
 		        		Keys.keyboard(world, player, settings, settings.keybinds);	            
 		        		MouseInput.mouse(world, player);
 		        				        			
 		        		if(renderMode == RENDER_MODE_WORLD_EARTH) //Player is in the overworld ('earth')
 		        		{
 		        			world.onWorldTick(player);
 		        		}
 		        		else if(renderMode == RENDER_MODE_WORLD_HELL) //Player is in the hell dimension 
 		        		{
 		        			
 		        		}
 		        		else if(renderMode == RENDER_MODE_WORLD_SKY) //Player is in the sky dimension
 		        		{
 		        			
 		        		}
 		        	}
 		        	
 		        	TerraeRasa.terraeRasa.checkWindowSize();
 		        	 
 		        	next_game_tick += SKIP_TICKS;
  		            loops++;
 		        }
 		        
 		        //Make sure the game loop doesn't fall very far behind and have to accelerate the 
 		        //game for an extended period of time
 		        if(System.currentTimeMillis() - next_game_tick > 1000)
 		        {
 		        	next_game_tick = System.currentTimeMillis();
 		        }
 		        
 		        Display.update();
 		    	GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 				GL11.glClearColor(0,0,0,0);
 				GL11.glColor4f(1, 1, 1, 1);
 				GL11.glPushMatrix();		        
 		        
 		        if(TerraeRasa.isMainMenuOpen) //if the main menu is open, render that, otherwise render the game
 		        {
 		        	soundEngine.setCurrentMusic("Placeholder Music");
 		        	mainMenu.render(settings);
 			    }
 		        else
 		        {
 		        	if(renderMode == RENDER_MODE_WORLD_EARTH)
 		    		{
 		        	 	RenderGlobal.render(world, player, renderMode, settings); //Renders Everything on the screen for the game
 		     		}
 		    		else if(renderMode == RENDER_MODE_WORLD_HELL)
 		    		{
 		    		 	RenderGlobal.render(worldHell, player, renderMode, settings); //Renders Everything on the screen for the game
 		    		}
 		    		else if(renderMode == RENDER_MODE_WORLD_SKY)
 		    		{
 		    		 	RenderGlobal.render(worldSky, player, renderMode, settings); //Renders Everything on the screen for the game
 		    		}
 		        	fps++;
 		        }
 		        
 		        if(settings.menuOpen)
 		        {
 		        	renderMenu.render(world, settings);
 		        }
 		        
 		        GL11.glPopMatrix();		        
 		        Display.swapBuffers(); //allows the display to update when using VBO's, probably
 		        Display.update(); //updates the display
 				
 		        if(System.currentTimeMillis() - start >= 5000)
 		        {
 		        	start = System.currentTimeMillis();
 	        		end = System.currentTimeMillis();     
 	        		fps = 0;
 		    	}
 	        	//     System.out.println(end - start);
 		    }     
 		}
 		catch(Exception e) //Fatal error catching
 		{
 			e.printStackTrace();			
 			ErrorUtils errorUtils = new ErrorUtils();
 			errorUtils.writeErrorToFile(e, true);			
 		}
 		finally
 		{
 			//Save the settings
 		    try 
 		    {
 				soundEngine.destroy();
 		    	saveSettings();
 			}
 		    catch (FileNotFoundException e) 
 		    {
 				e.printStackTrace();
 			}
 		    catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Starts a game from the main menu.
 	 * @param world the world to play on.
 	 * @param player the player to play on.
 	 */
 	public void startGame(String universeName, World world, EntityPlayer player)
 	{
 //		if(this.world != null)
 //		{
 //			throw new RuntimeException("World already exists!");
 //		}
 		this.universeName = universeName;
 		this.world = world;
 		this.world.chunkManager = chunkManager;
 		this.world.chunkManager.setUniverseName(universeName);
 		this.world.initSoundEngine(soundEngine);
 		this.player = player;
 		world.addPlayerToWorld(player);
 		TerraeRasa.isMainMenuOpen = false;
 		mainMenu = null;
 	}
 		
 	/**
 	 * Initializes miscellaneous things within the game engine. This should be called after object creation so 
 	 * that the UI thread does not throw an exception while loading textures.
 	 */
 	public void init()
 	{
 		Render.initializeTextures(getWorld());
 		chunkManager = new ChunkManager();
 		soundEngine = new SoundEngine(settings);
 		mainMenu = new MainMenu(settings);
 		renderMenu = new RenderMenu(settings);
 		Display.setVSyncEnabled(settings.vsyncEnabled);
 		debugCheats();
 	}
 	
 	/**
 	 * Loads the Settings for the entire game upon starting the game.
 	 * @throws IOException Indicates the saving operation has failed
 	 * @throws ClassNotFoundException Indicates the Settings class does not exist with the correct version
 	 */
 	public void loadSettings() 
 			throws IOException, ClassNotFoundException
 	{
 		FileManager fileManager = new FileManager();
 		this.settings = fileManager.loadSettings();
 	}
 	
 	/**
 	 * Saves the Settings for the entire game to disk, this is called before exiting the run method.
 	 * @throws IOException Indicates the saving operation has failed
 	 * @throws FileNotFoundException Indicates the desired directory (file) is not found on the filepath
 	 */
 	public void saveSettings() 
 			throws FileNotFoundException, IOException
 	{
 		FileManager fileManager = new FileManager();
 		fileManager.saveSettings(settings);
 	}
 	
 	/**
 	 * Inits some cheats and inventory stuff for debugging
 	 */
 	private void debugCheats()
 	{
 		if(TerraeRasa.initInDebugMode)
 		{
 			TerraeRasa.isMainMenuOpen = false;
 			FileManager fileManager = new FileManager();
 			world = fileManager.generateNewWorld("World", 1200, 800, EnumWorldDifficulty.NORMAL);//EnumWorldSize.LARGE.getWidth(), EnumWorldSize.LARGE.getHeight());
 			world.initSoundEngine(soundEngine);
 			world.chunkManager = chunkManager;
 			world.chunkManager.setUniverseName("World");
 			player = fileManager.generateAndSavePlayer("!!", EnumPlayerDifficulty.HARD);//new EntityLivingPlayer("Test player", EnumDifficulty.NORMAL);
 			world.addPlayerToWorld(player);
 			world.assessForAverageSky();
 			LightUtils utils = new LightUtils();
 			utils.applyAmbient(world);
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.stone, 100));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.torch, 100));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.chest, 100));
 
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.ironChest, 100));
 
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldIngot, 100));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.silverIngot, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.healthPotion1, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.healthPotion2, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaPotion1, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaPotion2, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaStar, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.furnace, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.craftingTable, 6));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.plank, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldPickaxe));		
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperIngot, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironIngot, 6));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.absorbPotion1, 20));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.criticalChancePotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.criticalChancePotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageBuffPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageBuffPotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageSoakPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.damageSoakPotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.dodgePotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.dodgePotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.steelSkinPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.steelSkinPotion2, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.swiftnessPotion1, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.swiftnessPotion2, 5));			
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperOre, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.tinOre, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldOre, 15));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.tinIngot, 100));	
  
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldHelmet));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldHelmet));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldBody));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldPants));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldGloves));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldBoots));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldBelt));
 
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.jasper));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.sapphire));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.emerald));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.rubyRing));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.diamond));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.opal));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldRing, 6));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.berserkersEssence));
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Block.glass, 40));
 
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.snowball, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.vialEmpty, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.vialOfWater, 100));
 			
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldSword));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.silverSword));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironSword));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeSword));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperSword));
 //			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldPickaxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.silverPickaxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironPickaxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzePickaxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperPickaxe));
 //			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.goldAxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.silverAxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironAxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeAxe));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperAxe));
 //			
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Helmet));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Body));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Pants));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Gloves));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Boots));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.t6Belt));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumHelmet));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumBody));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumPants));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumGloves));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumBoots));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.lunariumBelt));
			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.mastersBracer));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.steadfastShield));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.eaglesFeather));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.woodenArrow, 50));
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.gemSmartheal1, 40));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.gemDefense1, 40));
 			
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironHelmet));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironBody));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironPants));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironGloves));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironBoots));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ironBelt));
 			
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeHelmet));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeBody));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzePants));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeGloves));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeBoots));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.bronzeBelt));
 			
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperHelmet));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperBody));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperPants));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperGloves));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperBoots));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.copperBelt));
 
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.absorbPotion2, 200));
 
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.snowball));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.woodenArrow, 100));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaCrystal, 2));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.angelsSigil, 2));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.ringOfVigor, 5));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.regenerationPotion2, 200));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.attackSpeedPotion1, 200));
 //			player.inventory.pickUpItemStack(world, player, new ItemStack(Item.manaRegenerationPotion2, 200));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Spell.rejuvenate));
 			player.inventory.pickUpItemStack(world, player, new ItemStack(Spell.bulwark));
 			
 			
 		}		
 	}
 	
 	/**
 	 * Initiates a hardcore death - which deletes the player and exits to the main menu. For now.
 	 */
 	public void hardcoreDeath()
 	{
 		//Delete the player
 		FileManager manager = new FileManager();
 		manager.deletefile("/Player Saves/" + player.getName());
 		this.player = null;
 		//Save the world
 		if(renderMode == RENDER_MODE_WORLD_EARTH)
 		{
 			world.saveRemainingWorld();
 		}
 		//Reset to the main menu
 		resetMainMenu();
 		TerraeRasa.isMainMenuOpen = true; //Sets menu open
 		System.out.println("Game Over.");
 	}
 	
 	public void setWorld(World world)
 	{
 		this.world = world;
 	}
 	
 	/**
 	 * Gets the world object currently in use by the game. This will be null if the main menu is open or something breaks.
 	 * @return the world object for the current game, which may be null if on the main menu
 	 */
 	public World getWorld()
 	{
 		return world;
 	}
 	
 	/**
 	 * Changes the loaded world to something else. For example, changing from Earth to Hell would use this. Calling
 	 * this method forces a save of the remaining World and then loads what's required for the new World. The value 
 	 * of the param newMode should correspond to the class variables in GameEngine such as RENDER_MODE_WORLD_EARTH or
 	 * RENDER_MODE_WORLD_HELL. Supplying an incorrect value will not load a new World.
 	 * @param newMode the final integer value for the new world (indicating what object to manipulate)
 	 * @throws IOException indicates a general failure to load the file, not relating to a version error
 	 * @throws ClassNotFoundException indicates the saved world version is incompatible
 	 */
 	public void changeWorld(int newMode)
 			throws IOException, ClassNotFoundException
 	{
 		String worldName = "";
 		if(renderMode == RENDER_MODE_WORLD_EARTH)
 		{
 			world.saveRemainingWorld();
 			world = null;
 		}
 		else if(renderMode == RENDER_MODE_WORLD_HELL)
 		{
 			worldHell.saveRemainingWorld();
 			worldHell = null;
 		}
 		else if(renderMode == RENDER_MODE_WORLD_SKY)
 		{
 			worldName = worldSky.getWorldName();
 			worldSky.saveRemainingWorld();
 			worldSky = null;
 		}
 		
 		this.renderMode = newMode;
 		FileManager manager = new FileManager();
 		
 		if(renderMode == RENDER_MODE_WORLD_EARTH)
 		{
 			manager.loadWorld("Earth", worldName);
 		}
 		else if(renderMode == RENDER_MODE_WORLD_HELL)
 		{
 			manager.loadWorld("Hell", worldName);
 		}
 		else if(renderMode == RENDER_MODE_WORLD_SKY)
 		{
 			manager.loadWorld("Sky", worldName);
 		}
 	}
 	
 	/**
 	 * Saves the remaining world that's loaded and the player to their respective save locations before
 	 * exiting to the main menu.
 	 * @throws FileNotFoundException indicates a failure to find the save location of the player or world
 	 * @throws IOException indicates a general failure to save, not relating to the file
 	 */
 	public void closeGameToMenu() 
 			throws FileNotFoundException, IOException
 	{
 		if(!player.defeated)
 		{
 			FileManager manager = new FileManager();
 			manager.savePlayer(player);
 		}
 		
 		if(renderMode == RENDER_MODE_WORLD_EARTH)
 		{
 			world.saveRemainingWorld();
 		}
 		else if(renderMode == RENDER_MODE_WORLD_HELL)
 		{
 			world.saveRemainingWorld();
 		}
 		else if(renderMode == RENDER_MODE_WORLD_SKY)
 		{
 			world.saveRemainingWorld();
 		}
 		
 		TerraeRasa.isMainMenuOpen = true;
 		resetMainMenu();
 	}
 	
 	/**
 	 * Creates a new GuiMainMenu and assigns it to mainMenu.
 	 */
 	public void resetMainMenu()
 	{
 		mainMenu = new MainMenu(settings);
 	}
 }

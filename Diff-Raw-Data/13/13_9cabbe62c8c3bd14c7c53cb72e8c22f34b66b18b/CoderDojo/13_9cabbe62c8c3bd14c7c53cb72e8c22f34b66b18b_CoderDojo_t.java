 package coderdojo;
 
 /*
 * This class is the main class for our mod.  The two @EventHandler methods below is similar to a main method.
 * This is where all the methods and objects from all other classes will be called.
 * This code will produce a lot of errors as its being typed because we use so many different classes.  
 * Usually the fix is simply pressing ctrl+shift+o to automatically import the required classes.
 * 
  * Note: Don't worry about the imports in any of these classes
  * eclipse will automatically import everything needed with the shortcut
  * ctrl+shift+o (Mac: Command+shift+o)
  * if it asks you what to import, always pick the one with either forge, fml or minecraft in its name.
  */
 
 import net.minecraft.block.Block;
 import coderdojo.blocks.BlockCoderDojo;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 //Tells the game that this class is a mod and to add it to the game.
 @Mod(modid = "CoderDojo", name = "Coder Dojo Mod", version = "Week 1")
 public class CoderDojo {
 	
 	//Publicly declares the Block so it can be used anywhere.
 	public static Block coderDojoBlock;
 	
 	/**
 	 * Runs before the game loads.
 	 * Used to add Blocks, Items, Entities etc. to the game.
 	 * @EventHandler is important here, mod will not load without it
 	 */
 	@EventHandler
 	public static void preInit(FMLPreInitializationEvent event) {
 		/*
		 * Creates the Coder Dojo block from BlockCoderDojo.java
		 * Here I use 1024 as the ID, as I know its not used in Minecraft.
 		 * Explained in BlockCoderDojo.java
 		 */
 		coderDojoBlock = new BlockCoderDojo(1024);
 		
 		//adds the block created above to the game;
 		GameRegistry.registerBlock(coderDojoBlock, "coderDojoBlock");
 	}
 	
 	/**
 	 * Runs as the game loads.
	 * Used for adding names and recipes to the game
 	 * @EventHandler is important here, mod will not load without it
 	 */
 	@EventHandler
 	public static void init(FMLInitializationEvent event) {
 		//Registers the name of the block with Minecraft
 		LanguageRegistry.addName(coderDojoBlock, "Coder Dojo Block");
 	}
 }

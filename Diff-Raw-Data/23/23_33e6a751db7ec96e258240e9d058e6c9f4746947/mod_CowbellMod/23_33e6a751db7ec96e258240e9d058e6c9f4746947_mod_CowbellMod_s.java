 //
 // This is Kew's first mod.
 //
 // We wanted to do CowBell, but we are following the tutorial at
 // http://www.youtube.com/watch?v=Yy2QAWOPiBI
 //
 
 package net.minecraft.src;
 
 import java.util.List;
 import java.util.Random;
 import java.util.Map;
 import net.minecraft.client.Minecraft;
 
 
 // This is the actual  block mod
 public class mod_CowbellMod extends BaseMod
 {
 	
 	// Properties
 
 	// The actual block
	public static final Block cowbellBlock;
 	
 	// The Item...
 	public static Item cowbellItem;
 	
	// Constructors	
	static 
	{
		// Create the block
		cowbellBlock = new mod_CowbellBlock();
		
		// Create a cowbell item...
		cowbellItem = (new Item(150)).setFull3D().setItemName("cowbell");		
	}
 	
 	
     public mod_CowbellMod()
     {
     	//super();
     	
     	// Hook 
 		ModLoader.SetInGUIHook(this, true, true);
 		ModLoader.SetInGameHook(this, true, true);    	
     }
 
     // Methods
     
     //@Override
     public String getVersion()
     {
    	return "1.0.0";
     }
 
 	//@Override
 	public void load() 
 	{
 			System.out.println("Loading \"Kew's Cowbell Mod\" - By Kew McParlane - 2012");
 			
 	    	ModLoader.RegisterBlock(cowbellBlock);
 	    	ModLoader.AddName(cowbellBlock, "Cowbell");    	                                                                       
 	    	cowbellBlock.blockIndexInTexture = ModLoader.addOverride("/terrain.png","/cowbell_texture.png");
 	
 	    	ModLoader.AddName(cowbellItem, "Cowbell Block");	    	
 	    	cowbellItem.iconIndex = ModLoader.addOverride("/gui/items.png", "/cowbell_block.png");
 	
 	    	//
 	    	// Here we define our recipe
 	    	//
 	    	
 	    	// This is the pattern to put into the crafting table..
 	    	Object[] recipe =  new Object[] {
 	    			" S ",
 	    			"III",
 	    			"INI",
 	    			
 	    			Character.valueOf('S'),Item.silk,
 	    			Character.valueOf('I'),Item.ingotGold,
 	    			Character.valueOf('N'),Item.goldNugget
 	    	};
 
 	    	// ... and this is what we build.
 	    	//ItemStack stack = new ItemStack(cowbellBlock,1);
 	    	
 	    	ItemStack stack = new ItemStack(cowbellBlock,1);
 	    	
 	    	// Add the recipe to Minecraft.
 	    	
 	    	ModLoader.AddRecipe(stack,recipe);
 	}	
 	
 	
 	//
 	// Add too GuiContainerCreative
 	//
 	
 	// Flag that represents that your have added your item to the container
 	public boolean haveAddedToCreativeContainer = false;
 	
 	// On every tick of the GUI
 	public boolean OnTickInGUI(float f, Minecraft minecraft, GuiScreen guiscreen)
 	{		
 		// If we are on a server
 		if ((minecraft.theWorld == null) || minecraft.theWorld.multiplayerWorld) 
 		{
 			//  We don't want to do this...			
 			return true;
 		}
 		else
 		{
 			// If this is the Creative container
 			if(guiscreen instanceof GuiContainerCreative) 
 			{		
 	
 				// And we have not added it yet..
 				if (!haveAddedToCreativeContainer)
 				{
 					// Then add it...					
 					Object obj = (ContainerCreative)((GuiContainerCreative) guiscreen).inventorySlots;
 					((ContainerCreative)obj).itemList.add(new ItemStack(cowbellBlock));
 					
 					// Mark it as added..
 					haveAddedToCreativeContainer = true;
 				}
 			}
 			else
 			{
 				// Next time we are not in the GuiContainerCreative, we forget we added it
 				haveAddedToCreativeContainer = false;		
 			}
 		}
 		
 		return true;
 	}
 	
 	
 }

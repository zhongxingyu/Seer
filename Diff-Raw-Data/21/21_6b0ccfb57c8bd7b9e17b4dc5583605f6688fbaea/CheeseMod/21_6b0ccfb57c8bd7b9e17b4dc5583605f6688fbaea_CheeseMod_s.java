 package com.github.gustav9797.CheeseMod;
 
 import java.util.Dictionary;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 @Mod(modid="CheeseMod", name="CheeseMod", version="0.0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=true)
 
 public class CheeseMod {
 	private int[] ids;
 	
 	public static Block cheese;
 	public static Block infectedCheese;
 	public static Block gravestone;
 	public static Block glowcheese;
 	
 	//public static Block liquidCheeseFlowing;
 	//public static Block liquidCheeseStill;
 	
 	public static OstWorldGenerator worldGen = new OstWorldGenerator();
 	
 	public static EntityMob graveZombie;
 	
 	public static Map<Object, Object> cheeseTypes = new HashMap<Object, Object>();
 			
 	@Instance("CheeseMod")
     public static CheeseMod instance;
     
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="com.github.gustav9797.CheeseMod.ClientProxy", serverSide="com.github.gustav9797.CheeseMod.CommonProxy")
     public static CommonProxy proxy;
     
     @PreInit
 	public void preLoad(FMLPreInitializationEvent e) {
 		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
 		ids = new int[] {
 				config.getBlock("blocks", "cheeseBlock", 2301).getInt(),
 				config.getBlock("blocks", "infectedCheeseBlock", 2302).getInt(),
 				config.getBlock("blocks","graveStone", 2303).getInt(),
 				config.getBlock("blocks","glowcheese", 2304).getInt()//,
 				//config.getBlock("blocks","liquidCheeseFlowing",2305).getInt(),
 				//config.getBlock("blocks","liquidCheeseStill",2306).getInt()
 		};
 		config.save();
 		
 		//cheeseTypes.put(new int(?), new int(?));
 
 		MinecraftForge.EVENT_BUS.register(proxy);
 	}
     
     @Init
     public void load(FMLInitializationEvent event) {
     	
     	GameRegistry.registerWorldGenerator(worldGen);
     	
     	 ModLoader.registerEntityID(EntityGraveZombie.class, "Grave Zombie",  ModLoader.getUniqueEntityId());
     	
     	cheese = new CheeseBlock(ids[0], 0, Material.ground, ids[1])
         .setHardness(0.5F).setStepSound(Block.soundClothFootstep)
         .setBlockName("cheese").setCreativeTab(CreativeTabs.tabDecorations);
     	
         LanguageRegistry.addName(cheese, "Cheese");
         MinecraftForge.setBlockHarvestLevel(cheese, "shovel", 0);
         GameRegistry.registerBlock(cheese, "cheese");
     	
         
     	infectedCheese = new InfectedCheeseBlock(ids[1], 1, Material.ground)
         .setHardness(0.5F).setStepSound(Block.soundClothFootstep)
        .setBlockName("cheese").setCreativeTab(CreativeTabs.tabDecorations);
     	
         LanguageRegistry.addName(infectedCheese, "Infected Cheese");
         MinecraftForge.setBlockHarvestLevel(infectedCheese, "shovel", 0);
         GameRegistry.registerBlock(infectedCheese, "infectedCheese");
         
         
         gravestone = new Gravestone(ids[2], 0)
         .setBlockName("Gravestone").setCreativeTab(CreativeTabs.tabDecorations);
         
         LanguageRegistry.addName(gravestone, "Gravestone");
         MinecraftForge.setBlockHarvestLevel(gravestone, "pickaxe", 3);
         GameRegistry.registerBlock(gravestone, "gravestone");
         
         
         glowcheese = new Glowcheese(ids[3], 2,Material.glass)
         .setHardness(1).setStepSound(Block.soundGlassFootstep).setLightValue(1)
         .setBlockName("Glowcheese").setCreativeTab(CreativeTabs.tabBlock);
         
         LanguageRegistry.addName(glowcheese, "Glowcheese");
         MinecraftForge.setBlockHarvestLevel(glowcheese, "pickaxe", 2);
         GameRegistry.registerBlock(glowcheese, "glowcheese");
         
        
        /*liquidCheeseFlowing = new LiquidCheeseFlowing(ids[4],Material.lava)
        .setBlockName("LiquidCheeseFlowing").setCreativeTab(CreativeTabs.tabBlock);
        
        LanguageRegistry.addName(liquidCheeseFlowing, "LiquidCheeseFlowing");
        GameRegistry.registerBlock(liquidCheeseFlowing, "liquidCheeseFlowing");
        
        
        liquidCheeseStill = new LiquidCheeseStill(ids[5],Material.lava)
        .setBlockName("LiquidCheeseStill").setCreativeTab(CreativeTabs.tabBlock);
        
        LanguageRegistry.addName(liquidCheeseStill, "LiquidCheeseStill");
        GameRegistry.registerBlock(liquidCheeseStill, "liquidCheeseStill");*/
       
        // End Basic Blocks
        
            proxy.registerRenderers();
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
             // Stub Method
     }
 }

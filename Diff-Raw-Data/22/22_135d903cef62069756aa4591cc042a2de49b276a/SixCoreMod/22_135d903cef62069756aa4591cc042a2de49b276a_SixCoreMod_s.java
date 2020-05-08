 package com.basspro.scm;
 
 import java.io.File;
 
 import net.minecraft.creativetab.CreativeTabs;
 
 import com.basspro.scm.block.SixCoreModBlocks;
 import com.basspro.scm.configuration.ConfigurationHandler;
 import com.basspro.scm.core.handlers.LocalizationHandler;
 import com.basspro.scm.core.proxy.CommonProxy;
 import com.basspro.scm.creativetab.TabSixCoreBlock;
 import com.basspro.scm.creativetab.TabSixCoreCombat;
 import com.basspro.scm.creativetab.TabSixCoreFood;
 import com.basspro.scm.creativetab.TabSixCoreMaterial;
 import com.basspro.scm.creativetab.TabSixCoreOre;
 import com.basspro.scm.item.SixCoreModItems;
 import com.basspro.scm.lib.Reference;
 import com.basspro.scm.recipe.RecipesSixCoreMod;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class SixCoreMod
 {
 
     @Instance(Reference.MOD_ID)
     public static SixCoreMod instance;
 
     @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
     public static CommonProxy proxy;
 
     public static CreativeTabs tabSixCoreModBlock = new TabSixCoreBlock(
             CreativeTabs.getNextID(), Reference.MOD_ID + "Block");
 
     public static CreativeTabs tabSixCoreModOre = new TabSixCoreOre(
             CreativeTabs.getNextID(), Reference.MOD_ID + "Ore");
 
     public static CreativeTabs tabSixCoreMaterial = new TabSixCoreMaterial(
             CreativeTabs.getNextID(), Reference.MOD_ID + "Material");
 
     public static CreativeTabs tabSixCoreFood = new TabSixCoreFood(
             CreativeTabs.getNextID(), Reference.MOD_ID + "Food");
 
 //    public static CreativeTabs tabSixCoreCombat = new TabSixCoreCombat(
 //            CreativeTabs.getNextID(), Reference.MOD_ID + "Combat");
 
     // public static CreativeTabs tabSixCoreModTools = new
     // TabSixCoreModTools(CreativeTabs.getNextID(), "Six Core Tools");
     // public static CreativeTabs tabSixCoremodMobs = new
     // TabSixCoreModMobs(CreativeTabs.getNextID(), "SiX Core Mobs");
 
     @Mod.EventHandler
     public void init(FMLPreInitializationEvent event)
     {
         // Load the localization files into the LanguageRegistry
         LocalizationHandler.loadLanguages();
 
         ConfigurationHandler.init(new File(event.getModConfigurationDirectory()
                 .getAbsolutePath()
                 + File.separator
                 + Reference.CHANNEL_NAME
                 + File.separator + Reference.MOD_ID + ".cfg"));
 
         // Blocks
         SixCoreModBlocks.init();
 
         // Items
         SixCoreModItems.init();
 
         // Recipes
         RecipesSixCoreMod.init();
 
         // World Generation
 //        SixCoreModWorldGen.init();
         
         // Biome Generation
 //        SCMBiomeGenBase.init();
     }
 
     @Mod.EventHandler
     private void load(FMLInitializationEvent event)
     {
 
     }
 }

 package mods.Electrolysm.electro;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.Item;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.relauncher.Side;
 
 
 import mods.Electrolysm.electro.common.PacketHandler;
 import mods.Electrolysm.electro.data.VersionData;
 import mods.Electrolysm.electro.data.VersionCheck;
 import mods.Electrolysm.electro.data.VersionHelper;
 import mods.Electrolysm.electro.data.data;
 import mods.Electrolysm.electro.machines.gui.GuiHandler;
 import mods.Electrolysm.electro.metals.hiddenDust;
 import mods.Electrolysm.electro.metals.hiddenIngot;
 import mods.Electrolysm.electro.metals.sydiumLava;
 import mods.Electrolysm.electro.metals.tier1.babbitt;
 import mods.Electrolysm.electro.metals.tier1.ionicElectrum;
 import mods.Electrolysm.electro.metals.tier1.ironisedGold;
 import mods.Electrolysm.electro.metals.tier1.pewter;
 import mods.Electrolysm.electro.metals.tier1.sydium;
 import mods.Electrolysm.electro.metals.tier1.tibetanSilver;
 import mods.Electrolysm.electro.metals.tier1.tumbaga;
 import mods.Electrolysm.electro.metals.tier2.alnikog;
 import mods.Electrolysm.electro.metals.tier2.furrobabbitt;
 import mods.Electrolysm.electro.metals.tier2.marrtanezer;
 import mods.Electrolysm.electro.metals.tier2.ormogo;
 import mods.Electrolysm.electro.metals.tier2.rhodite;
 import mods.Electrolysm.electro.metals.tier2.syold;
 import mods.Electrolysm.electro.world.WorldGenOres;
 import mods.Electrolysm.electro.world.mixedOre;
 import mods.Electrolysm.electro.world.metalOreDrops.copperDust;
 import mods.Electrolysm.electro.world.metalOreDrops.electrumDust;
 import mods.Electrolysm.electro.world.metalOreDrops.ferrousDust;
 import mods.Electrolysm.electro.world.metalOreDrops.ironDust;
 import mods.Electrolysm.electro.world.metalOreDrops.leadDust;
 import mods.Electrolysm.electro.world.metalOreDrops.silverDust;
 import mods.Electrolysm.electro.world.metalOreDrops.tinDust;
 import mods.Electrolysm.electro.world.plant.plantSeeds;
 import mods.Electrolysm.electro.world.plant.silkFibre;
 import mods.Electrolysm.electro.machines.electroFurnace;
 import mods.Electrolysm.electro.machines.magmaticExtractor;
 import mods.Electrolysm.electro.learning.matterResearch;
 import mods.Electrolysm.electro.learning.toolsResearch;
 import mods.Electrolysm.electro.world.plant.silkPlant;
 
 
	@Mod(modid="Electrolysm", name="Electrolysm", version= "0.2.0")
 
 	@NetworkMod(channels = { "Electrolysm" }, clientSideRequired = true, serverSideRequired = true, packetHandler = PacketHandler.class)
 	
 
 	public class electrolysmCore {
 	
 		  
 		//Creative Tab
 		public static CreativeTabs TabElectrolysm = new TabElectrolysm(CreativeTabs.getNextID(),"Electrolysm");
 		//End
 
 		
 		
 		
 /*
  * ===============================================================================================================
  * 										World Generation + Ores
  * ===============================================================================================================
  */
 		
 		public static int mixedOreID = 508;
 		
 		public static Block mixedOre = new mixedOre(mixedOreID);
 
 		//Ore Drops
 		public static electrumDust electrumDust = new electrumDust(510);
 		public static copperDust copperDust = new copperDust(511);
 		public static tinDust tinDust = new tinDust(512);
 		public static ferrousDust ferrousDust = new ferrousDust(513);
 		public static leadDust leadDust = new leadDust(514);
 		public static silverDust silverDust = new silverDust(515);
 		public static ironDust ironDust = new ironDust(516);
 		
 		public static hiddenDust hiddenDust = new hiddenDust(517);
 
 /*
 * ===========================================================================================================
 * 										Plant
 * ===========================================================================================================
 */
 		public static silkFibre silkFibre = new silkFibre(520);
 		public static plantSeeds plantSeeds = new plantSeeds(521);
 		public static Block silkPlant = new silkPlant(522);
 		
 		
 		
 
 /*
 * ===========================================================================================================
 * 										Machines
 * ===========================================================================================================
 */
 		//public static Block electroFurnace = new electroFurnace(500, null);
 		public static Block magmaticExtractor = new magmaticExtractor(501, null);
 		
 		//Parts(Products)
 		public static sydiumLava sydiumLava = new sydiumLava(650);
 		
 /*
  * ===============================================================================================================
  * 											All Ingots
  * ===============================================================================================================		
  */	
 	        
 		//Hidden Ingot
 		public static final hiddenIngot hiddenIngot = new hiddenIngot(599);
 /*
 * ============================================================================
 * 									Tier 1
 * ============================================================================		
 */	
 		//Electrum + Iron (Strongish, light in colour)
 		public static final ionicElectrum ionicElectrum = new ionicElectrum(600);
 		
 		//Iron + Gold (Brittle, Conductor of heat + electricity)
 		public static final ironisedGold ironisedGold = new ironisedGold(601);
 		
 		//Gold + Copper (Strong, Conductor of heat + electricity)
 		public static final tumbaga tumbaga = new tumbaga(602);
 		
 		//Copper + Tin (Flexible, Light in Colour)
 		public static final babbitt babbitt = new babbitt(603);
 		
 		//Tin + ferrous (Flexible, Heavy) TextureDone
 		public static final pewter pewter = new pewter(604);
 		
 		//Ferrous + Lead (Poisonous, Flammable, unstable(Deteriates into Syanic Acid(Liquid, like lava))) TextureDone
 		public static final sydium sydium = new sydium(605);
 		//public static syanicAcid syanicAcid = new syanicAcid(null);
 				
 		//Lead + Silver (Conductor of heat + electricity, Dark and shinny in colour) TextureDone
 		public static final tibetanSilver tibetanSilver = new tibetanSilver(607);
 		
 /*
 * ============================================================================
 * 									Tier 2
 * ============================================================================		
 */	
 				//ionicElectrum + tibetanSilver (Strongish, Dark and shinny in colour)
 				public static final rhodite rhodite = new rhodite(610);
 				
 				//tibetanSilver + tumbaga (Strong, Conductor of heat + electricity)
 				public static final alnikog alnikog = new alnikog(611);
 				
 				//tumbaga + babbitt (Flexible, Conductor of heat + electricity, strong)
 				public static final furrobabbitt furrobabbitt = new furrobabbitt(612);
 				
 				//babbitt + pewter (Flexible, Light in Colour, Heavy)
 				public static final ormogo ormogo = new ormogo(613);
 				
 				//pewter + sydium (Unstable, Poisonous, Heavy)
 				public static final marrtanezer marrtanezer = new marrtanezer(614);
 				
 				//sydium + ironisedGold (Poisonous, Conductor of heat + electricity, unstable(Deteriates into Syanic Acid(Liquid, like lava)))
 				public static final syold syold = new syold(615);
 		
 		
 				
 				
 				
 		
 /*
 * ===========================================================================================================
 * 										GUIs
 * ===========================================================================================================
 */		
 		public static GuiHandler guihandler = new GuiHandler();
 		
         @Instance
         public static electrolysmCore GUIinstance;
         
 /*
 * ===========================================================================================================
 * 										Research/Learning
 * ===========================================================================================================
 */	
         public static matterResearch matterResearch = new matterResearch(550);
         public static toolsResearch toolsResearch = new toolsResearch(551);
  
  
 /* 
  * ===============================================================================================================
  * ===============================================================================================================
  * 										Config (In game Stuff)
  * ===============================================================================================================
  * ===============================================================================================================
  */
 		
 		
 		@PreInit
 		public void loadConfiguration(FMLPreInitializationEvent evt) {
 			
 			//Version Check	
 			// Initialize the Version Check Tick Handler (Client only)
 	        TickRegistry.registerTickHandler(new VersionCheck(), Side.CLIENT);
 	        TickRegistry.registerTickHandler(new VersionHelper(), Side.CLIENT);
 
 
 /*
  * ===============================================================================================================
  * 										World Generation + Ores
  * ===============================================================================================================
  */{
 		
 	 			GameRegistry.registerWorldGenerator(new WorldGenOres());
 	 			//GameRegistry.registerWorldGenerator(new WorldGenStructures());
 	 			GameRegistry.registerBlock(mixedOre);
 
 	 			LanguageRegistry.addName(mixedOre, "Einsteinium Ore");
 	 				 				 			
                 //OreDictionary.registerOre("ingotEinsteinium", new ItemStack(einsteiniumIngot));
 	 			LanguageRegistry.addName(electrumDust, "Electrum Dust");
 	 			LanguageRegistry.addName(copperDust, "Copper Dust");
 	 			LanguageRegistry.addName(tinDust, "Tin Dust");
 	 			LanguageRegistry.addName(ferrousDust, "Ferrous Dust");
 	 			LanguageRegistry.addName(leadDust, "Lead Dust");
 	 			LanguageRegistry.addName(silverDust, "Silver Dust");
 	 			LanguageRegistry.addName(ironDust, "Iron Dust");
 	 			
 
 	 			LanguageRegistry.addName(hiddenDust, "Hidden Dust");
 	 			LanguageRegistry.addName(hiddenIngot, "Hidden Matter Ingot");
 	
 
 
 	 			//Crafting
 	 			GameRegistry.addRecipe(new ItemStack(electrumDust, 2),
 	 					"XY ",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(copperDust, 2),
 	 					"XYY",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(tinDust, 3),
 	 					"XYY", "Y  ",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(ferrousDust, 5),
 	 					"XYY", "YY ",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(leadDust, 1),
 	 					"XYY", "YYY",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(silverDust, 2),
 	 					"XYY", "YYY", "Y  ",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 	 			
 	 			GameRegistry.addRecipe(new ItemStack(ironDust, 3),
 	 					"XYY", "YYY", "YY ",
 	 					Character.valueOf('X'), mixedOre,
 	 					Character.valueOf('Y'), Block.cobblestone);
 			}
  /*
   * ===============================================================================================================
  * 											All Ingots
   * ===============================================================================================================		
   */	
  				
  				//Tier 1 Ingots
  				LanguageRegistry.addName(ionicElectrum, "Ionic Electrum Ingot");
  				LanguageRegistry.addName(ironisedGold, "Iroised Gold Ingot");
  				LanguageRegistry.addName(tumbaga, "Tumbaga Ingot");
  				LanguageRegistry.addName(babbitt, "Babbitt Ingot");
  				LanguageRegistry.addName(pewter, "Pewter Ingot");
  				LanguageRegistry.addName(sydium, "Sydium Ingot");
  				LanguageRegistry.addName(tibetanSilver, "Tibetan Silver Ingot");
 
  				//Tier 2 Ingots
  				LanguageRegistry.addName(rhodite, "Rhodite Ingot");
  				LanguageRegistry.addName(alnikog, "Alnikog Ingot");
  				LanguageRegistry.addName(furrobabbitt, "Furrobabbitt Ingot");
  				LanguageRegistry.addName(ormogo, "Ormogo Ingot");
  				LanguageRegistry.addName(marrtanezer, "Marrtanezer Ingot");
  				LanguageRegistry.addName(syold, "Syold Ingot");
  				
  				//Crafting
  				//Tier 1
  				GameRegistry.addRecipe(new ItemStack(ionicElectrum, 2),
  						"XYZ",
  						Character.valueOf('X'), electrumDust,
  						Character.valueOf('Y'), ironDust,
  						Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(ironisedGold, 2),
  						"XYZ",
  						Character.valueOf('X'), ironDust,
  						Character.valueOf('Y'), net.minecraft.item.Item.ingotGold,
  						Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(tumbaga, 2),
  						"XYZ",
  						Character.valueOf('X'), net.minecraft.item.Item.ingotGold,
  						Character.valueOf('Y'), copperDust,
 						Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(babbitt, 2),
  						"XYZ",
  						Character.valueOf('X'), copperDust,
  						Character.valueOf('Y'), tinDust,
  						Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(pewter, 2),
  						"XYZ",
  						Character.valueOf('X'), tinDust,
  						Character.valueOf('Y'), ferrousDust,
 					    Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(sydium, 2),
  						"XYZ",
  						Character.valueOf('X'), ferrousDust,
  						Character.valueOf('Y'), leadDust,
 						Character.valueOf('Z'), net.minecraft.item.Item.coal);
  				
  				GameRegistry.addRecipe(new ItemStack(tibetanSilver, 2),
  						"XYZ",
  						Character.valueOf('X'), leadDust,
  						Character.valueOf('Y'), silverDust,
 						Character.valueOf('Z'), net.minecraft.item.Item.coal);
 /*
 * ===========================================================================================================
 * 										GUIs
 * ===========================================================================================================
 */
  		        NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
                 //GameRegistry.registerTileEntity(TileEntityISPComp.class, "containerISPComp");
 
 /*
 * ===========================================================================================================
 * 										Machines
 * ===========================================================================================================
 */
  		       //GameRegistry.registerBlock(electroFurnace); //Combinder
  		       //LanguageRegistry.addName(electroFurnace, "Electric Powered Smelter");//Combinder
  		       
  		       GameRegistry.registerBlock(magmaticExtractor);
  		       LanguageRegistry.addName(magmaticExtractor, "Magmatic Extractor");
  		       
  		       LanguageRegistry.addName(sydiumLava, "Lava Contained by Sydium");
  		       
  		       //Crafting
  		       //Machines
  		       GameRegistry.addRecipe(new ItemStack(magmaticExtractor),
  		    		   "XYX", "ZMZ", "XNX",
  		    		   Character.valueOf('X'), net.minecraft.item.Item.ingotIron,
  		    		   Character.valueOf('Y'), net.minecraft.item.Item.redstone,
  		    		   Character.valueOf('Z'), net.minecraft.item.Item.ingotGold,
  		    		   Character.valueOf('M'), net.minecraft.item.Item.bucketEmpty,
  		    		   Character.valueOf('N'), tumbaga);
 /*
 * ===========================================================================================================
 * 										Research/Learning
 * ===========================================================================================================
 */	
  		       LanguageRegistry.addName(matterResearch, "Matter Research");
  		       LanguageRegistry.addName(toolsResearch, "Matter Tool Research");
 /*
 * ===========================================================================================================
 * 										Plant
 * ===========================================================================================================
 */
  		       	
  		       GameRegistry.registerBlock(silkPlant);
  		       LanguageRegistry.addName(silkPlant, "Silky Plant");
  		       	
  		       	
 		
 		
 		}
 	}
 
  				
 	

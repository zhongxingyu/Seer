 package net.minecraft.src;
 
 import java.util.*;
 import java.lang.reflect.*;
 import java.io.PrintWriter;
 import net.minecraft.client.Minecraft;
 import net.minecraft.server.MinecraftServer;
 import moapi.api.*;
 
 public class mod_MyWay extends BaseMod implements MMWHookInterface<String> {
   // Copyright/license info
   private static final String Name = "Minecraft My Way";
  private static final String Version = "0.71 (For use with Minecraft 1.4.4)";
 	private static final String Copyright = "All original code and images (C) 2011-2012, Jonathan \"Wyrd\" Brazell";
 	private static final String License = "This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.";
   // Options
   private static ModOptions optionsMain;
   private static ModOptions optionsHostileSpawns;
   private static ModOptions optionsPeacefulSpawns;
   private static ModOptions optionsRecipes;
   private static ModOptions optionsSpecial;
   private static ModOptions optionsWorldGen;
   private static ModOptions optionsAdventureMode;
   // Save states
 	private static boolean removeDiamondTools = false;
 	private static boolean removeDiamondArmor = false;
 	private static boolean removeWoodAxes = false;
 	private static boolean removeCobblestoneTools = false;
 	private static boolean removeTNT = false;
 	private static boolean craftFlint = false;
 	private static boolean craftCobblestone = false;
 	private static boolean flintTools = false;
 	private static boolean extendedCrafting = false;
 	private static boolean timeFlow = false;
   // World Generation Options
   public static boolean genCaves = true;
   public static boolean genStrongholds = true;
   public static boolean genVillages = true;
   public static boolean genMineshafts = true;
   public static boolean genScatteredFeatures = true;
   public static boolean genRavines = true;
   // Mob Spawns
   public static ModBooleanOption optBats;
   public static ModBooleanOption optChicken;
   public static ModBooleanOption optCow;
   public static ModBooleanOption optIronGolem;
   public static ModBooleanOption optMooshroom;
   public static ModBooleanOption optNPC;
   public static ModBooleanOption optOcelot;
   public static ModBooleanOption optPeaceful;
   public static ModBooleanOption optPig;
   public static ModBooleanOption optSheep;
   public static ModBooleanOption optSnowman;
   public static ModBooleanOption optSquid;
   public static ModBooleanOption optWolf;
 	public static ModBooleanOption optBlaze;
 	public static ModBooleanOption optCaveSpider;
 	public static ModBooleanOption optCreeper;
 	public static ModBooleanOption optDragon;
 	public static ModBooleanOption optEnderman;
 	public static ModBooleanOption optGhast;
 	public static ModBooleanOption optGiantZombie;
 	public static ModBooleanOption optHostile;
 	public static ModBooleanOption optMagmaCube;
 	public static ModBooleanOption optPigZombie;
 	public static ModBooleanOption optSilverfish;
 	public static ModBooleanOption optSkeleton;
 	public static ModBooleanOption optSlime;
 	public static ModBooleanOption optSpider;
 	public static ModBooleanOption optWitch;
 	public static ModBooleanOption optWither;
 	public static ModBooleanOption optZombie;
   // Peaceful mob adjustments
   public static boolean peacefulDropBones = false;
   public static boolean peacefulAdjustDrops = false;
   // Items for flint tools
   public static Item hoeFlint;
   public static Item axeFlint;
   public static Item pickaxeFlint;
   public static Item spearFlint;  
   // Items for hatchets
   public static Item hatchetFlint;
   public static Item hatchetWood;
   public static Item hatchetStone;
   public static Item hatchetIron;
   public static Item hatchetDiamond;
   public static Item hatchetGold;
   // Other variables
   private static MMWWorldType mmwWorldType = new MMWWorldType();
   private static MMWFoodStats mmwFoodStats = new MMWFoodStats();
   private static Minecraft mc = Minecraft.getMinecraft();
   private static MMWGuiIngame mmwGuiIngame = new MMWGuiIngame(mc);
   private static Block mmwStone;
   private static Block mmwSand;
   private static Block mmwGravel;
   private static Block mmwDirt;
   private static Item saveItemDye = Item.dyePowder;
   private static int tickCounter = 0;
   private static int baseItemID = 12120;
       	
   public void load() {
     // Set up options menu for mod options
     initModOptionsAPI();
     baseItemID = Integer.parseInt(optionsRecipes.getOptionValue("Base Item ID"));
 
     // Enable StringTranslate hook for altering menus
     (new MMWStringTranslate(StringTranslate.getInstance().getCurrentLanguage())).setHook(this);
 
     // Replace Stone
     Block.blocksList[Block.stone.blockID] = null;
     mmwStone = (new MMWBlockStone(1, 1)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setBlockName("stone");
 
     // Replace Sand
     Block.blocksList[Block.sand.blockID] = null;
     mmwSand = (new MMWBlockSand(12, 18)).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setBlockName("sand");
 
     // Replace Gravel
     Block.blocksList[Block.gravel.blockID] = null;
     mmwGravel = (new MMWBlockGravel(13, 19)).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setBlockName("gravel");
 
     // Replace Dirt
     Block.blocksList[Block.dirt.blockID] = null;
     mmwDirt = (new MMWBlockDirt(3, 2)).setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setBlockName("dirt");
 
     // Replace dye to allow bonemeal to be disabled.
     Item.itemsList[Item.dyePowder.shiftedIndex] = null;
     Item.dyePowder = (new MMWItemDye(95)).setIconCoord(14, 4).setItemName("dyePowder");
 
     // Remove spawns for vanilla mobs, add custom mob spawns to replace them
     MMWUtil.removeSpawn(EntityCow.class, EnumCreatureType.creature);
     MMWUtil.removeSpawn(EntityPig.class, EnumCreatureType.creature);
     MMWUtil.removeSpawn(EntitySheep.class, EnumCreatureType.creature);
     MMWUtil.registerEntityID(MMWEntityCow.class, "Cow", 92);
     MMWUtil.registerEntityID(MMWEntityPig.class, "Pig", 90);
     MMWUtil.registerEntityID(MMWEntitySheep.class, "Sheep", 91);
     MMWUtil.addSpawn(MMWEntityCow.class, 8, 4, 4, EnumCreatureType.creature);
     MMWUtil.addSpawn(MMWEntityPig.class, 10, 4, 4, EnumCreatureType.creature);
     MMWUtil.addSpawn(MMWEntitySheep.class, 12, 4, 4, EnumCreatureType.creature);
 
     // Add flint tool items
     hoeFlint = new MMWItemHoe(baseItemID+0, MMWEnumToolMaterial.FLINT).setItemName("hoeFlint").setIconCoord(1, 8);
     axeFlint = new MMWItemAxe(baseItemID+1, MMWEnumToolMaterial.FLINT).setItemName("axeFlint").setIconCoord(1, 7);
     pickaxeFlint = new MMWItemPickaxe(baseItemID+2, MMWEnumToolMaterial.FLINT).setItemName("pickaxeFlint").setIconCoord(1, 6);
     spearFlint = new MMWItemSpear(baseItemID+3, MMWEnumToolMaterial.FLINT).setItemName("spearFlint").setIconCoord(1, 4);
     MMWUtil.addLocalization(pickaxeFlint, "Flint Pickaxe");
     MMWUtil.addLocalization(spearFlint, "Flint Spear");      
     MMWUtil.addLocalization(axeFlint, "Flint Axe");
     MMWUtil.addLocalization(hoeFlint, "Flint Hoe");
     if (optionsRecipes.getToggleValue("Flint Tools")) {
       flintTools = true;
       MMWUtil.addRecipe(new ItemStack(spearFlint, 1), new Object[] {"F", "S", "S", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(pickaxeFlint, 1), new Object[] {"FFF", " S ", " S ", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(axeFlint, 1), new Object[] {"FF", "FS", " S", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(hoeFlint, 1), new Object[] {"FF", " S", " S", 'F', Item.flint, 'S', Item.stick});
     }
 
     // Add hatchets
     hatchetFlint = new MMWItemHatchetFlint(baseItemID+4, MMWEnumToolMaterial.FLINT).setItemName("hatchetFlint").setIconCoord(1, 7);
     hatchetWood = new MMWItemHatchet(baseItemID+5, EnumToolMaterial.WOOD).setItemName("hatchetWood").setIconCoord(0, 7);
     hatchetStone = new MMWItemHatchet(baseItemID+6, EnumToolMaterial.STONE).setItemName("hatchetStone").setIconCoord(1, 7);
     hatchetIron = new MMWItemHatchet(baseItemID+7, EnumToolMaterial.IRON).setItemName("hatchetIron").setIconCoord(2, 7);
     hatchetDiamond = new MMWItemHatchet(baseItemID+8, EnumToolMaterial.EMERALD).setItemName("hatchetDiamond").setIconCoord(3, 7);
     hatchetGold = new MMWItemHatchet(baseItemID+9, EnumToolMaterial.GOLD).setItemName("hatchetGold").setIconCoord(4, 7);
     MMWUtil.addLocalization(hatchetFlint, "Flint Hatchet");
     MMWUtil.addLocalization(hatchetWood, "Wood Hatchet");
     MMWUtil.addLocalization(hatchetStone, "Stone Hatchet");
     MMWUtil.addLocalization(hatchetIron, "Iron Hatchet");
     MMWUtil.addLocalization(hatchetDiamond, "Diamond Hatchet");
     MMWUtil.addLocalization(hatchetGold, "Gold Hatchet");
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting") && optionsRecipes.getToggleValue("Flint Tools")) {
       MMWUtil.addRecipe(new ItemStack(hatchetFlint, 1), new Object[] {"FS", "S ", 'F', Item.flint, 'S', Item.stick}); 
     } 
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting") && optionsRecipes.getToggleValue("Wood Axes")) {
       MMWUtil.addRecipe(new ItemStack(hatchetWood, 1), new Object[] {"WS", "S ", 'W', Block.planks, 'S', Item.stick}); 
     } 
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting") && optionsRecipes.getToggleValue("Cobblestone Tools")) {
       MMWUtil.addRecipe(new ItemStack(hatchetStone, 1), new Object[] {"CS", "S ", 'C', Block.cobblestone, 'S', Item.stick}); 
     } 
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting")) {
       MMWUtil.addRecipe(new ItemStack(hatchetIron, 1), new Object[] {"IS", "S ", 'I', Item.ingotIron, 'S', Item.stick}); 
     }
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting") && optionsRecipes.getToggleValue("Diamond Tools")) {
       MMWUtil.addRecipe(new ItemStack(hatchetDiamond, 1), new Object[] {"DS", "S ", 'D', Item.diamond, 'S', Item.stick}); 
     } 
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting")) {
       MMWUtil.addRecipe(new ItemStack(hatchetGold, 1), new Object[] {"GS", "S ", 'G', Item.ingotGold, 'S', Item.stick}); 
     } 
      
     // Check for option changes every tick
     ModLoader.setInGameHook(this, true, false);
   }
 
   public String StringTranslateHook(String param) {
     if (param!="selectWorld.moreWorldOptions") {
       return param;
     }
     if (!optionsAdventureMode.getToggleValue("Allow on Creation")) {
       return param;
     }
     GuiScreen screen = mc.currentScreen;
     if (screen instanceof GuiCreateWorld) {
       GuiButton buttonGameMode;
       try {
         buttonGameMode = (GuiButton)MMWUtil.getPrivateValue(GuiCreateWorld.class, screen, "buttonGameMode", "w");
       } catch (Exception ignored) {
         return param;
       }
       if (!(buttonGameMode instanceof MMWModeButton)) {
         buttonGameMode = new MMWModeButton(screen);
         try {
           MMWUtil.setPrivateValue(GuiCreateWorld.class, screen, "buttonGameMode", "w", buttonGameMode);
         } catch (Exception ignored) { }
         screen.controlList.set(2, buttonGameMode);
       }
     }
     return param;
   }
 
   public boolean onTickInGame(float f, Minecraft paramMinecraft) {
     processTimeFlow();
     processXP();
     killCritters();
     // HUD Hook
     mc.ingameGUI = mmwGuiIngame;
     if (mc.thePlayer!=null && !optionsSpecial.getToggleValue("Sprinting")) {
       mc.thePlayer.setSprinting(false);
     }
     // Don't need to check other options every tick.
     tickCounter++;
     if ((tickCounter & 0x001f)!=0x0001)
       return true;
     processAdventureMode();  
     processSpawns();
     processSpecial();
     processRecipes();
     return true;
   }
 
   public static void processTimeFlow() {
     if (optionsSpecial.getOptionValue("Time Flow")=="Always Day") {
       for (int idx=0; idx<MinecraftServer.getServer().worldServers.length; idx++) {
         long time = MinecraftServer.getServer().worldServers[idx].getWorldTime();
         if ((time%24000)>12000) {
           time = (time / 24000) * 24000; 
           MinecraftServer.getServer().worldServers[idx].setWorldTime(time);
         }
       }
     } else if (optionsSpecial.getOptionValue("Time Flow")=="Always Night") {
       for (int idx=0; idx<MinecraftServer.getServer().worldServers.length; idx++) {
         long time = MinecraftServer.getServer().worldServers[idx].getWorldTime();
         if (((time%24000)<13800) || ((time%24000)>22200)) {
           time = ((time / 24000) * 24000) + 13800; 
           MinecraftServer.getServer().worldServers[idx].setWorldTime(time);
         }
       }
     } else if (optionsSpecial.getOptionValue("Time Flow")=="Rapid") {
       for (int idx=0; idx<MinecraftServer.getServer().worldServers.length; idx++) {
         long time = MinecraftServer.getServer().worldServers[idx].getWorldTime();
         MinecraftServer.getServer().worldServers[idx].setWorldTime(time + 1);
       }
     } else if (optionsSpecial.getOptionValue("Time Flow")=="Slow") {
       if (timeFlow) {
         for (int idx=0; idx<MinecraftServer.getServer().worldServers.length; idx++) {
           long time = MinecraftServer.getServer().worldServers[idx].getWorldTime();
           MinecraftServer.getServer().worldServers[idx].setWorldTime(time - 1);
         }
       }
       timeFlow = !timeFlow;
     } else if (optionsSpecial.getOptionValue("Time Flow")=="Single Day") {
       for (int idx=0; idx<MinecraftServer.getServer().worldServers.length; idx++) {
         long totaltime = MinecraftServer.getServer().worldServers[idx].worldInfo.getWorldTotalTime();
         long time = MinecraftServer.getServer().worldServers[idx].getWorldTime();
         if ((totaltime>13800) && (((time%24000)<13800) || ((time%24000)>22200))) {
           time = ((time / 24000) * 24000) + 13800; 
           MinecraftServer.getServer().worldServers[idx].setWorldTime(time);
         }
       }
     } 
   }
 
   public static void processAdventureMode() {
     if (optionsAdventureMode.getToggleValue("Hands break sand/gravel")) {
       Material.sand.func_85158_p();
     } else {
       MMWUtil.setPrivateValue(Material.class, Material.sand, "field_85159_M", "M", Boolean.FALSE);
     }
     if (optionsAdventureMode.getToggleValue("Hands break leaves")) {
       Material.leaves.func_85158_p();
     } else {
       MMWUtil.setPrivateValue(Material.class, Material.leaves, "field_85159_M", "M", Boolean.FALSE);
     }
     if (optionsAdventureMode.getToggleValue("Hands break dirt")) {
       Material.ground.func_85158_p();
       Material.grass.func_85158_p();
     } else {
       MMWUtil.setPrivateValue(Material.class, Material.ground, "field_85159_M", "M", Boolean.FALSE);
       MMWUtil.setPrivateValue(Material.class, Material.grass, "field_85159_M", "M", Boolean.FALSE);
     }
     if (optionsAdventureMode.getToggleValue("Hands break clay")) {
       Material.clay.func_85158_p();
     } else {
       MMWUtil.setPrivateValue(Material.class, Material.clay, "field_85159_M", "M", Boolean.FALSE);
     }
     if (optionsAdventureMode.getToggleValue("Hands break cactus")) {
       Material.cactus.func_85158_p();
     } else {
       MMWUtil.setPrivateValue(Material.class, Material.cactus, "field_85159_M", "M", Boolean.FALSE);
     }
   }
 
   public static void processXP() {
     mmwGuiIngame.optXP = optionsSpecial.getToggleValue("XP");
     if (!mmwGuiIngame.optXP) {
       for (int idx=0; idx<mc.theWorld.loadedEntityList.size(); idx++) {
         Entity ent = (Entity)mc.theWorld.loadedEntityList.get(idx);
         if ((ent!=null) && (ent instanceof EntityXPOrb)) {
           ((EntityXPOrb)ent).setDead();
         }
       }
       mc.thePlayer.experience = 0;
       mc.thePlayer.experienceLevel = 0;
       if (mc.thePlayer.worldObj.isRemote) {
         for (int idx=0; idx<mc.getIntegratedServer().getConfigurationManager().playerEntityList.size(); idx++) {
           ((EntityPlayerMP)mc.getIntegratedServer().getConfigurationManager().playerEntityList.get(idx)).experience = 0;
           ((EntityPlayerMP)mc.getIntegratedServer().getConfigurationManager().playerEntityList.get(idx)).experienceLevel = 0;
         }
       }
     }
   }
 
   public static void processWorldGen() {
     // World Gen Options - Called from MMWChunkProvider
     genCaves = optionsWorldGen.getToggleValue("Caves");
     genMineshafts = optionsWorldGen.getToggleValue("Mineshafts");
     genRavines = optionsWorldGen.getToggleValue("Ravines");
     genScatteredFeatures = optionsWorldGen.getToggleValue("Scattered Features");
     genStrongholds = optionsWorldGen.getToggleValue("Strongholds");
     genVillages = optionsWorldGen.getToggleValue("Villages");
   }
 
   public static void processSpecial() {
     // Bonemeal
     ((MMWItemDye)Item.dyePowder).disableBonemeal = !optionsSpecial.getToggleValue("Bonemeal Instagrowth");
     // Sand
     ((MMWBlockSand)mmwSand).gravityWorks = optionsSpecial.getToggleValue("Sand Gravity"); 
     // Gravel
     ((MMWBlockGravel)mmwGravel).gravityWorks = optionsSpecial.getToggleValue("Gravel Gravity"); 
     // Dirt
     ((MMWBlockDirt)mmwDirt).gravityWorks = optionsSpecial.getToggleValue("Dirt Gravity"); 
     // Stone
     ((MMWBlockStone)Block.blocksList[Block.stone.blockID]).drop = optionsSpecial.getToggleValue("Stone Drops Gravel") ? Block.gravel.blockID : Block.cobblestone.blockID;
     // Set custom FoodStats
     if (mc.thePlayer!=null) {
       mc.thePlayer.foodStats = mmwFoodStats;
       if (mc.thePlayer.worldObj.isRemote) {
         for (int idx=0; idx<mc.getIntegratedServer().getConfigurationManager().playerEntityList.size(); idx++) {
           ((EntityPlayerMP)mc.getIntegratedServer().getConfigurationManager().playerEntityList.get(idx)).foodStats = mmwFoodStats;
         }
       }
     }
     // Fatigue/Exhaustion
     if (optionsSpecial.getToggleValue("Exhaustion")) {
       mmwFoodStats.setFoodExhaustionLevel(0.0F);
     }
     // Hunger
     mmwFoodStats.hunger = (mmwGuiIngame.optHunger = optionsSpecial.getToggleValue("Hunger"));
     // Healing
     mmwFoodStats.healing = optionsSpecial.getToggleValue("Auto Healing");
   }
 
   public static void processRecipes() {
     // Diamond Tools
     if (!optionsRecipes.getToggleValue("Diamond Tools")) {
       if (!removeDiamondTools) {
         removeDiamondTools = true;
         MMWUtil.removeRecipe(new ItemStack(Item.swordDiamond, 1), new Object[] {"D", "D", "S", 'D', Item.diamond, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.shovelDiamond, 1), new Object[] {"D", "S", "S", 'D', Item.diamond, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.pickaxeDiamond, 1), new Object[] {"DDD", " S ", " S ", 'D', Item.diamond, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.axeDiamond, 1), new Object[] {"DD", "DS", " S", 'D', Item.diamond, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.hoeDiamond, 1), new Object[] {"DD", " S", " S", 'D', Item.diamond, 'S', Item.stick}); 
       }
     } else if (removeDiamondTools) {
       removeDiamondTools = false;
       MMWUtil.addRecipe(new ItemStack(Item.swordDiamond, 1), new Object[] {"D", "D", "S", 'D', Item.diamond, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.shovelDiamond, 1), new Object[] {"D", "S", "S", 'D', Item.diamond, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.pickaxeDiamond, 1), new Object[] {"DDD", " S ", " S ", 'D', Item.diamond, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.axeDiamond, 1), new Object[] {"DD", "DS", " S", 'D', Item.diamond, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.hoeDiamond, 1), new Object[] {"DD", " S", " S", 'D', Item.diamond, 'S', Item.stick}); 
     }
     // Diamond Armor
     if (!optionsRecipes.getToggleValue("Diamond Armor")) {
       if (!removeDiamondArmor) {
         removeDiamondArmor = true;
         MMWUtil.removeRecipe(new ItemStack(Item.helmetDiamond, 1), new Object[] {"DDD", "D D", 'D', Item.diamond}); 
         MMWUtil.removeRecipe(new ItemStack(Item.plateDiamond, 1), new Object[] {"D D", "DDD", "DDD", 'D', Item.diamond}); 
         MMWUtil.removeRecipe(new ItemStack(Item.legsDiamond, 1), new Object[] {"DDD", "D D", "D D", 'D', Item.diamond}); 
         MMWUtil.removeRecipe(new ItemStack(Item.bootsDiamond, 1), new Object[] {"D D", "D D", 'D', Item.diamond}); 
       }
     } else if (removeDiamondArmor) {
       removeDiamondArmor = false;
       MMWUtil.addRecipe(new ItemStack(Item.helmetDiamond, 1), new Object[] {"DDD", "D D", 'D', Item.diamond}); 
       MMWUtil.addRecipe(new ItemStack(Item.plateDiamond, 1), new Object[] {"D D", "DDD", "DDD", 'D', Item.diamond}); 
       MMWUtil.addRecipe(new ItemStack(Item.legsDiamond, 1), new Object[] {"DDD", "D D", "D D", 'D', Item.diamond}); 
       MMWUtil.addRecipe(new ItemStack(Item.bootsDiamond, 1), new Object[] {"D D", "D D", 'D', Item.diamond}); 
     }
     // Wood Axes
     if (!optionsRecipes.getToggleValue("Wood Axes")) {
       if (!removeWoodAxes) {
         removeWoodAxes = true;
         MMWUtil.removeRecipe(new ItemStack(Item.pickaxeWood, 1), new Object[] {"WWW", " S ", " S ", 'W', Block.planks, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.axeWood, 1), new Object[] {"WW", "WS", " S", 'W', Block.planks, 'S', Item.stick}); 
       }
     } else if (removeWoodAxes) {
       removeWoodAxes = false;
       MMWUtil.addRecipe(new ItemStack(Item.pickaxeWood, 1), new Object[] {"WWW", " S ", " S ", 'W', Block.planks, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.axeWood, 1), new Object[] {"WW", "WS", " S", 'W', Block.planks, 'S', Item.stick}); 
     }
     // Cobblestone Tools
     if (!optionsRecipes.getToggleValue("Cobblestone Tools")) {
       if (!removeCobblestoneTools) {
         removeCobblestoneTools = true;
         MMWUtil.removeRecipe(new ItemStack(Item.swordStone, 1), new Object[] {"C", "C", "S", 'C', Block.cobblestone, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.shovelStone, 1), new Object[] {"C", "S", "S", 'C', Block.cobblestone, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.pickaxeStone, 1), new Object[] {"CCC", " S ", " S ", 'C', Block.cobblestone, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.axeStone, 1), new Object[] {"CC", "CS", " S", 'C', Block.cobblestone, 'S', Item.stick}); 
         MMWUtil.removeRecipe(new ItemStack(Item.hoeStone, 1), new Object[] {"CC", " S", " S", 'C', Block.cobblestone, 'S', Item.stick}); 
       }
     } else if (removeCobblestoneTools) {
       removeCobblestoneTools = false;
       MMWUtil.addRecipe(new ItemStack(Item.swordStone, 1), new Object[] {"C", "C", "S", 'C', Block.cobblestone, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.shovelStone, 1), new Object[] {"C", "S", "S", 'C', Block.cobblestone, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.pickaxeStone, 1), new Object[] {"CCC", " S ", " S ", 'C', Block.cobblestone, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.axeStone, 1), new Object[] {"CC", "CS", " S", 'C', Block.cobblestone, 'S', Item.stick}); 
       MMWUtil.addRecipe(new ItemStack(Item.hoeStone, 1), new Object[] {"CC", " S", " S", 'C', Block.cobblestone, 'S', Item.stick}); 
     }
     // TNT
     if (optionsRecipes.getToggleValue("TNT")) {
       if (!removeTNT) {
         removeTNT = true;
         MMWUtil.removeRecipe(new ItemStack(Block.tnt, 1), new Object[] {"X#X", "#X#", "X#X", 'X', Item.gunpowder, '#', Block.sand});
       }
     } else if (removeTNT) {
       removeTNT = false;
       MMWUtil.addRecipe(new ItemStack(Block.tnt, 1), new Object[] {"X#X", "#X#", "X#X", 'X', Item.gunpowder, '#', Block.sand});
     }
     // Craft Flint
     if (optionsRecipes.getToggleValue("Craft Flint")) {
       if (!craftFlint) {
         craftFlint = true;
         MMWUtil.addRecipe(new ItemStack(Item.flint, 1), new Object[] {"#", '#', Block.gravel});
       }
     } else if (craftFlint) {
       craftFlint = false;
       MMWUtil.removeRecipe(new ItemStack(Item.flint, 1), new Object[] {"#", '#', Block.gravel});
     }
     // Craft Cobblestone
     if (optionsRecipes.getToggleValue("Craft Cobblestone")) {
       if (!craftCobblestone) {
         craftCobblestone = true;
         MMWUtil.addRecipe(new ItemStack(Block.cobblestone, 2), new Object[] {"#c", "c#", '#', Block.gravel, 'c', Item.clay});
       }
     } else if (craftCobblestone) {
       craftCobblestone = false;
       MMWUtil.removeRecipe(new ItemStack(Block.cobblestone, 2), new Object[] {"#c", "c#", '#', Block.gravel, 'c', Item.clay});
     }
     // Flint Tools
     if (optionsRecipes.getToggleValue("Flint Tools")) {
       if (!flintTools) {
         flintTools = true;
         MMWUtil.addRecipe(new ItemStack(spearFlint, 1), new Object[] {"F", "S", "S", 'F', Item.flint, 'S', Item.stick}); 
         MMWUtil.addRecipe(new ItemStack(pickaxeFlint, 1), new Object[] {"FFF", " S ", " S ", 'F', Item.flint, 'S', Item.stick}); 
         MMWUtil.addRecipe(new ItemStack(axeFlint, 1), new Object[] {"FF", "FS", " S", 'F', Item.flint, 'S', Item.stick}); 
         MMWUtil.addRecipe(new ItemStack(hoeFlint, 1), new Object[] {"FF", " S", " S", 'F', Item.flint, 'S', Item.stick});
       }
     } else if (flintTools) {
       flintTools = false;
       MMWUtil.removeRecipe(new ItemStack(spearFlint, 1), new Object[] {"F", "S", "S", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(pickaxeFlint, 1), new Object[] {"FFF", " S ", " S ", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(axeFlint, 1), new Object[] {"FF", "FS", " S", 'F', Item.flint, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(hoeFlint, 1), new Object[] {"FF", " S", " S", 'F', Item.flint, 'S', Item.stick});
     }
     // Extended 2x2 Crafting
 	  if (optionsRecipes.getToggleValue("Extended 2x2 Crafting")) {
       if (!extendedCrafting) {
         extendedCrafting = true;
         MMWUtil.addRecipe(new ItemStack(Item.stick, 2), new Object[] {"#", "#", '#', Block.sapling}); 
         MMWUtil.addRecipe(new ItemStack(Item.stick, 2), new Object[] {"#", "#", '#', Block.cactus}); 
         MMWUtil.addRecipe(new ItemStack(Item.stick, 2), new Object[] {"/", "/", '/', Item.bone});
         MMWUtil.addRecipe(new ItemStack(Item.silk, 2), new Object[] {"#", '#', Block.cloth});
         if (optionsRecipes.getToggleValue("Flint Tools")) {
           MMWUtil.addRecipe(new ItemStack(hatchetFlint, 1), new Object[] {"FS", "S ", 'F', Item.flint, 'S', Item.stick});
         }
     	  if (optionsRecipes.getToggleValue("Wood Axes")) {
           MMWUtil.addRecipe(new ItemStack(hatchetWood, 1), new Object[] {"WS", "S ", 'W', Block.planks, 'S', Item.stick}); 
         } 
     	  if (optionsRecipes.getToggleValue("Cobblestone Tools")) {
           MMWUtil.addRecipe(new ItemStack(hatchetStone, 1), new Object[] {"CS", "S ", 'C', Block.cobblestone, 'S', Item.stick}); 
         } 
         MMWUtil.addRecipe(new ItemStack(hatchetIron, 1), new Object[] {"IS", "S ", 'I', Item.ingotIron, 'S', Item.stick}); 
     	  if (optionsRecipes.getToggleValue("Diamond Tools")) {
           MMWUtil.addRecipe(new ItemStack(hatchetDiamond, 1), new Object[] {"DS", "S ", 'D', Item.diamond, 'S', Item.stick}); 
         } 
         MMWUtil.addRecipe(new ItemStack(hatchetGold, 1), new Object[] {"GS", "S ", 'G', Item.ingotGold, 'S', Item.stick}); 
         // Dagger (" #", "/ ", #=Material, /=stick)
         // Sling ("#", "@", #=Leather, @=string)
         // Sling stones? gravel -> stones?
         // Trowel ("#", "/", #=Material, /=stick) Shovel/hoe
         // Hand Pick ("##", "/ ", #=Material, /=stick)
         // Campfire ("//", "//", /=stick)
         // Bow Drill ("/@", "/#", /=stick, @=string, #=planks)
       }
     } else if (extendedCrafting) {
       extendedCrafting = false;
       MMWUtil.removeRecipe(new ItemStack(Item.stick, 1), new Object[] {"#", "#", '#', Block.sapling}); 
       MMWUtil.removeRecipe(new ItemStack(Item.stick, 2), new Object[] {"#", "#", '#', Block.cactus}); 
       MMWUtil.removeRecipe(new ItemStack(Item.stick, 2), new Object[] {"/", "/", '/', Item.bone});
       MMWUtil.removeRecipe(new ItemStack(Item.silk, 2), new Object[] {"#", '#', Block.cloth});
       MMWUtil.removeRecipe(new ItemStack(hatchetFlint, 1), new Object[] {"FS", "S ", 'F', Item.flint, 'S', Item.stick});
       MMWUtil.removeRecipe(new ItemStack(hatchetWood, 1), new Object[] {"WS", "S ", 'W', Block.planks, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(hatchetStone, 1), new Object[] {"CS", "S ", 'C', Block.cobblestone, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(hatchetIron, 1), new Object[] {"IS", "S ", 'I', Item.ingotIron, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(hatchetDiamond, 1), new Object[] {"DS", "S ", 'D', Item.diamond, 'S', Item.stick}); 
       MMWUtil.removeRecipe(new ItemStack(hatchetGold, 1), new Object[] {"GS", "S ", 'G', Item.ingotGold, 'S', Item.stick}); 
     }
   }  
 
   public static void killCritters() {
     boolean allowHostile = optHostile.getValue();
     boolean allowPeaceful = optPeaceful.getValue(); 
       
     // Hostile & Peaceful Spawn overrides
     mc.theWorld.setAllowedSpawnTypes(allowHostile, allowPeaceful);
    IntegratedServer server = mc.getIntegratedServer();
    if (server!=null) {
      for (int idx=0;idx<server.worldServers.length;idx++) {
        server.worldServers[idx].setAllowedSpawnTypes(allowHostile, allowPeaceful);
      }    
    }
     if (!allowHostile) {
       MMWUtil.killAll(EntityBlaze.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityCaveSpider.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityCreeper.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityDragon.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityEnderman.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityGhast.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityGiantZombie.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityMagmaCube.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityPigZombie.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntitySilverfish.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntitySkeleton.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntitySlime.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntitySpider.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityWitch.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityWither.class, EnumCreatureType.monster);
       MMWUtil.killAll(EntityZombie.class, EnumCreatureType.monster);
     }    
     if (!allowPeaceful) {
       MMWUtil.killAll(EntityVillager.class, EnumCreatureType.creature); // Enum is a dummy value in this case.
       MMWUtil.killAll(EntityBat.class, EnumCreatureType.ambient);
       MMWUtil.killAll(EntityChicken.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityCow.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityIronGolem.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityMooshroom.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityOcelot.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityPig.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntitySheep.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntitySnowman.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntityWolf.class, EnumCreatureType.creature);
       MMWUtil.killAll(EntitySquid.class, EnumCreatureType.waterCreature);
     }
     // NPCs
     mc.getIntegratedServer().setCanSpawnNPCs(optNPC.getValue());
     if (!optNPC.getValue()) { 
       MMWUtil.killAll(EntityVillager.class, EnumCreatureType.creature); // Enum is a dummy value in this case.
     }
     if (!allowHostile && !allowPeaceful) {
       return;
     }
     if (allowPeaceful) {
       if (!optBats.getValue()) {
         MMWUtil.killAll(EntityBat.class, EnumCreatureType.ambient);
       }
       if (!optChicken.getValue()) {
         MMWUtil.killAll(EntityChicken.class, EnumCreatureType.creature);
       }
       if (!optCow.getValue()) {
         MMWUtil.killAll(EntityCow.class, EnumCreatureType.creature);
         MMWUtil.killAll(MMWEntityCow.class, EnumCreatureType.creature);
       }
       if (!optIronGolem.getValue()) {
         MMWUtil.killAll(EntityIronGolem.class, EnumCreatureType.creature);
       }
       if (!optMooshroom.getValue()) {
         MMWUtil.killAll(EntityMooshroom.class, EnumCreatureType.creature);
       }
       if (!optOcelot.getValue()) {
         MMWUtil.killAll(EntityOcelot.class, EnumCreatureType.creature);
       }
       if (!optPig.getValue()) {
         MMWUtil.killAll(EntityPig.class, EnumCreatureType.creature);
         MMWUtil.killAll(MMWEntityPig.class, EnumCreatureType.creature);
       } 
       if (!optSheep.getValue()) {
         MMWUtil.killAll(EntitySheep.class, EnumCreatureType.creature);
         MMWUtil.killAll(MMWEntitySheep.class, EnumCreatureType.creature);
       }
       if (!optSnowman.getValue()) {
         MMWUtil.killAll(EntitySnowman.class, EnumCreatureType.creature);
       }
       if (!optWolf.getValue()) {
         MMWUtil.killAll(EntityWolf.class, EnumCreatureType.creature);
       }
       if (!optSquid.getValue()) {
         MMWUtil.killAll(EntitySquid.class, EnumCreatureType.waterCreature);
       }
     }
     if (allowHostile) {
       if (!optBlaze.getValue()) {
         MMWUtil.killAll(EntityBlaze.class, EnumCreatureType.monster);
       }
       if (!optCaveSpider.getValue()) {
         MMWUtil.killAll(EntityCaveSpider.class, EnumCreatureType.monster);
       }
       if (!optCreeper.getValue()) {
         MMWUtil.killAll(EntityCreeper.class, EnumCreatureType.monster);
       }
       if (!optDragon.getValue()) {
         MMWUtil.killAll(EntityDragon.class, EnumCreatureType.monster);
       }
       if (!optEnderman.getValue()) {
         MMWUtil.killAll(EntityEnderman.class, EnumCreatureType.monster);
       }
       if (!optGhast.getValue()) {
         MMWUtil.killAll(EntityGhast.class, EnumCreatureType.monster);
       }
       if (!optGiantZombie.getValue()) {
         MMWUtil.killAll(EntityGiantZombie.class, EnumCreatureType.monster);
       }
       if (!optMagmaCube.getValue()) {
         MMWUtil.killAll(EntityMagmaCube.class, EnumCreatureType.monster);
       }
       if (!optPigZombie.getValue()) {
         MMWUtil.killAll(EntityPigZombie.class, EnumCreatureType.monster);
       }
       if (!optSilverfish.getValue()) {
         MMWUtil.killAll(EntitySilverfish.class, EnumCreatureType.monster);
       }
       if (!optSkeleton.getValue()) {
         MMWUtil.killAll(EntitySkeleton.class, EnumCreatureType.monster);
       }
       if (!optSlime.getValue()) {
         MMWUtil.killAll(EntitySlime.class, EnumCreatureType.monster);
       }
       if (!optSpider.getValue()) {
         MMWUtil.killAll(EntitySpider.class, EnumCreatureType.monster);
       }
       if (!optWitch.getValue()) {
         MMWUtil.killAll(EntityWitch.class, EnumCreatureType.monster);
       }
       if (!optWither.getValue()) {
         MMWUtil.killAll(EntityWither.class, EnumCreatureType.monster);
       }
       if (!optZombie.getValue()) {
         MMWUtil.killAll(EntityZombie.class, EnumCreatureType.monster);
       }
     }
   }
   
   public static void processSpawns() {
     boolean allowHostile = optHostile.getValue();
     boolean allowPeaceful = optPeaceful.getValue(); 
       
     if (!allowHostile && !allowPeaceful) {
       return;
     }
     // Set peaceful mob spawns
     if (allowPeaceful) {
       // Set peaceful mob adjustments
       peacefulDropBones = optionsPeacefulSpawns.getToggleValue("Drop bones");
       peacefulAdjustDrops = optionsPeacefulSpawns.getToggleValue("Adjust drops");
       if (optBats.getValue()) {
         MMWUtil.addSpawn(EntityBat.class, 10, 8, 8, EnumCreatureType.ambient);
       } else {
         MMWUtil.removeSpawn(EntityBat.class, EnumCreatureType.ambient);
       }
       if (optChicken.getValue()) {
         MMWUtil.addSpawn(EntityChicken.class, 10, 4, 4, EnumCreatureType.creature);
       } else {
         MMWUtil.removeSpawn(EntityChicken.class, EnumCreatureType.creature);
       }
       if (optCow.getValue()) {
         MMWUtil.addSpawn(MMWEntityCow.class, 8, 4, 4, EnumCreatureType.creature);
       } else {
         MMWUtil.removeSpawn(EntityCow.class, EnumCreatureType.creature);
         MMWUtil.removeSpawn(MMWEntityCow.class, EnumCreatureType.creature);
       }
       if (optMooshroom.getValue()) {
         MMWUtil.addSpawn(EntityMooshroom.class, 8, 4, 8, EnumCreatureType.creature, new BiomeGenBase[] {BiomeGenBase.mushroomIsland});
       } else {
         MMWUtil.removeSpawn(EntityMooshroom.class, EnumCreatureType.creature);
       }
       if (optOcelot.getValue()) {
         MMWUtil.addSpawn(EntityOcelot.class, 2, 1, 1, EnumCreatureType.creature, new BiomeGenBase[] {BiomeGenBase.jungle});
       } else {
         MMWUtil.removeSpawn(EntityOcelot.class, EnumCreatureType.creature);
       }
       if (optPig.getValue()) {
         MMWUtil.addSpawn(MMWEntityPig.class, 10, 4, 4, EnumCreatureType.creature);
       } else {
         MMWUtil.removeSpawn(EntityPig.class, EnumCreatureType.creature);
         MMWUtil.removeSpawn(MMWEntityPig.class, EnumCreatureType.creature);
       } 
       if (optSheep.getValue()) {
         MMWUtil.addSpawn(MMWEntitySheep.class, 12, 4, 4, EnumCreatureType.creature);
       } else {
         MMWUtil.removeSpawn(EntitySheep.class, EnumCreatureType.creature);
         MMWUtil.removeSpawn(MMWEntitySheep.class, EnumCreatureType.creature);
       }
       if (optWolf.getValue()) {
         MMWUtil.addSpawn(EntityWolf.class, 5, 4, 4, EnumCreatureType.creature, new BiomeGenBase[] {BiomeGenBase.forest});
         MMWUtil.addSpawn(EntityWolf.class, 8, 4, 4, EnumCreatureType.creature, new BiomeGenBase[] {BiomeGenBase.taiga});
       } else {
         MMWUtil.removeSpawn(EntityWolf.class, EnumCreatureType.creature);
       }
       if (optSquid.getValue()) {
         MMWUtil.addSpawn(EntitySquid.class, 10, 4, 4, EnumCreatureType.waterCreature);
       } else {
         MMWUtil.removeSpawn(EntitySquid.class, EnumCreatureType.waterCreature);
       }
     }
     if (allowHostile) {
       if (optCreeper.getValue()) {
         MMWUtil.addSpawn(EntityCreeper.class, 10, 4, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntityCreeper.class, EnumCreatureType.monster);
       }
       if (optEnderman.getValue()) {
         MMWUtil.addSpawn(EntityEnderman.class, 1, 1, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntityEnderman.class, EnumCreatureType.monster);
       }
       if (optGhast.getValue()) {
         MMWUtil.addSpawn(EntityGhast.class, 50, 4, 4, EnumCreatureType.monster, new BiomeGenBase[] {BiomeGenBase.hell});
       } else {
         MMWUtil.removeSpawn(EntityGhast.class, EnumCreatureType.monster);
       }
       if (optMagmaCube.getValue()) {
         MMWUtil.addSpawn(EntityMagmaCube.class, 1, 4, 4, EnumCreatureType.monster, new BiomeGenBase[] {BiomeGenBase.hell});
       } else {
         MMWUtil.removeSpawn(EntityMagmaCube.class, EnumCreatureType.monster);
       }
       if (optPigZombie.getValue()) {
         MMWUtil.addSpawn(EntityPigZombie.class, 100, 4, 4, EnumCreatureType.monster, new BiomeGenBase[] {BiomeGenBase.hell});
       } else {
         MMWUtil.removeSpawn(EntityPigZombie.class, EnumCreatureType.monster);
       }
       if (optSkeleton.getValue()) {
         MMWUtil.addSpawn(EntitySkeleton.class, 10, 4, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntitySkeleton.class, EnumCreatureType.monster);
       }
       if (optSlime.getValue()) {
         MMWUtil.addSpawn(EntitySlime.class, 10, 4, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntitySlime.class, EnumCreatureType.monster);
       }
       if (optSpider.getValue()) {
         MMWUtil.addSpawn(EntitySpider.class, 10, 4, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntitySpider.class, EnumCreatureType.monster);
       }
       if (optZombie.getValue()) {
         MMWUtil.addSpawn(EntityZombie.class, 10, 4, 4, EnumCreatureType.monster);
       } else {
         MMWUtil.removeSpawn(EntityZombie.class, EnumCreatureType.monster);
       }
     }
   }
 
   private void initModOptionsAPI() {
     // Create option screens
     optionsMain = new ModOptions("Minecraft My Way");
     ModOptionsAPI.addMod(optionsMain);
     optionsAdventureMode = new ModOptions("Minecraft My Way - Adventure Mode");
     optionsMain.addSubOptions(optionsAdventureMode);
     optionsWorldGen = new ModOptions("Minecraft My Way - Default World Gen");
     optionsMain.addSubOptions(optionsWorldGen);
     optionsRecipes = new ModOptions("Minecraft My Way - Recipes");
     optionsMain.addSubOptions(optionsRecipes);
     optionsPeacefulSpawns = new ModOptions("Minecraft My Way - Peaceful Spawns");
     optionsMain.addSubOptions(optionsPeacefulSpawns);
     optionsHostileSpawns = new ModOptions("Minecraft My Way - Hostile Spawns");
     optionsMain.addSubOptions(optionsHostileSpawns);
     optionsSpecial = new ModOptions("Minecraft My Way - Special");
     optionsMain.addSubOptions(optionsSpecial);
     // Add options to Hostile Spawn screen
 	  optZombie = (ModBooleanOption)optionsHostileSpawns.addToggle("Zombie").setValue(true);
 	  optWither = (ModBooleanOption)optionsHostileSpawns.addToggle("Wither").setValue(true);
 	  optWitch = (ModBooleanOption)optionsHostileSpawns.addToggle("Witch").setValue(true);
 	  optSpider = (ModBooleanOption)optionsHostileSpawns.addToggle("Spider").setValue(true);
 	  optSlime = (ModBooleanOption)optionsHostileSpawns.addToggle("Slime").setValue(true);
 	  optSkeleton = (ModBooleanOption)optionsHostileSpawns.addToggle("Skeleton").setValue(true);
 	  optSilverfish = (ModBooleanOption)optionsHostileSpawns.addToggle("Silverfish").setValue(true);
 	  optPigZombie = (ModBooleanOption)optionsHostileSpawns.addToggle("Pig Zombie").setValue(true);
 	  optMagmaCube = (ModBooleanOption)optionsHostileSpawns.addToggle("Magma Cube").setValue(true);
 	  optGiantZombie = (ModBooleanOption)optionsHostileSpawns.addToggle("Giant Zombie").setValue(true);
 	  optGhast = (ModBooleanOption)optionsHostileSpawns.addToggle("Ghast").setValue(true);
 	  optEnderman = (ModBooleanOption)optionsHostileSpawns.addToggle("Enderman").setValue(true);
 	  optDragon = (ModBooleanOption)optionsHostileSpawns.addToggle("Dragon").setValue(true);
 	  optCreeper = (ModBooleanOption)optionsHostileSpawns.addToggle("Creeper").setValue(true);
 	  optCaveSpider = (ModBooleanOption)optionsHostileSpawns.addToggle("Cave Spider").setValue(true);
 	  optBlaze = (ModBooleanOption)optionsHostileSpawns.addToggle("Blaze").setValue(true);
 	  optHostile = (ModBooleanOption)optionsHostileSpawns.addToggle("Allow Hostile Spawns").setValue(true);
     // Add options to Peaceful Spawn screen
 	  optWolf = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Wolf").setValue(true);
 	  optSquid = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Squid").setValue(true);
 	  optSnowman = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Snowman").setValue(true);
 	  optSheep = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Sheep").setValue(true);
 	  optPig = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Pig").setValue(true);
 	  optOcelot = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Ocelot/Cat").setValue(true);
 	  optNPC = (ModBooleanOption)optionsPeacefulSpawns.addToggle("NPCs").setValue(true);
 	  optMooshroom = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Mooshroom").setValue(true);
 	  optIronGolem = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Iron Golem").setValue(true);
 	  optCow = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Cow").setValue(true);
 	  optChicken = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Chicken").setValue(true);
 	  optBats = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Bats").setValue(true);
     optionsPeacefulSpawns.addToggle("Drop bones").setValue(false);
     optionsPeacefulSpawns.addToggle("Adjust drops").setValue(false);
 	  optPeaceful = (ModBooleanOption)optionsPeacefulSpawns.addToggle("Allow Peaceful Spawns").setValue(true);
     // Add options to Recipe screen
 	  optionsRecipes.addToggle("Wood Axes").setValue(true);
 	  optionsRecipes.addToggle("TNT").setValue(true);
 	  optionsRecipes.addToggle("Flint Tools").setValue(false);
 	  optionsRecipes.addToggle("Extended 2x2 Crafting").setValue(false);
 	  optionsRecipes.addToggle("Diamond Armor").setValue(true);
 	  optionsRecipes.addToggle("Craft Flint").setValue(false);
 	  optionsRecipes.addToggle("Craft Cobblestone").setValue(false);
 	  optionsRecipes.addToggle("Cobblestone Tools").setValue(true);
     optionsRecipes.addToggle("Diamond Tools").setValue(true);
     optionsRecipes.addTextOption("Base Item ID", "12120", 5).setWide(true);
     // Add options to Special screen
 	  optionsSpecial.addToggle("XP").setValue(true);
     optionsSpecial.addMultiOption("Time Flow", new String[]{"Normal", "Always Day", "Always Night", "Rapid", "Slow", "Single Day"});
 	  optionsSpecial.addToggle("Stone Drops Gravel").setValue(false);
     optionsSpecial.addToggle("Sprinting").setValue(true);
 	  optionsSpecial.addToggle("Sand Gravity").setValue(true);
     optionsSpecial.addToggle("Hunger").setValue(true);
 	  optionsSpecial.addToggle("Gravel Gravity").setValue(true);
     optionsSpecial.addToggle("Exhaustion").setValue(true);
 	  optionsSpecial.addToggle("Dirt Gravity").setValue(false);
 	  optionsSpecial.addToggle("Bonemeal Instagrowth").setValue(true);
     optionsSpecial.addToggle("Auto Healing").setValue(true);
     // Add options to World Gen screen
 	  optionsWorldGen.addToggle("Villages").setValue(true);
 	  optionsWorldGen.addToggle("Strongholds").setValue(true);
 	  optionsWorldGen.addToggle("Scattered Features").setValue(true);
 	  optionsWorldGen.addToggle("Ravines").setValue(true);
 	  optionsWorldGen.addToggle("Mineshafts").setValue(true);
 	  optionsWorldGen.addToggle("Caves").setValue(true);
 	  // Add options to Adventure Mode screen
     optionsAdventureMode.addToggle("Hands break sand/gravel").setValue(false).setWide(true);
     optionsAdventureMode.addToggle("Hands break leaves").setValue(false);
     optionsAdventureMode.addToggle("Hands break dirt").setValue(false);
     optionsAdventureMode.addToggle("Hands break clay").setValue(false);
     optionsAdventureMode.addToggle("Hands break cactus").setValue(false);
 	  optionsAdventureMode.addToggle("Allow on Creation").setValue(false).setWide(true);
 	  // Load saved values over defaults (if any)
     optionsMain.loadValues();
     // Save current values
     optionsMain.save();
   }
 
   public String getName() {
     return Name;
   }
 	
   public String getVersion() {
     return Version;
   }
 }

 package arcadia;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.command.ICommand;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.src.ModLoader;
 import net.minecraft.stats.Achievement;
 import net.minecraft.stats.AchievementList;
 import net.minecraftforge.common.AchievementPage;
 import net.minecraftforge.common.EnumHelper;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = arcadia.modid, name = "World of Arcadia", version = "0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class arcadia
 {
 	   @Instance(arcadia.modid)
 	   public static arcadia instance;
 	   public static final String modid = "arcadia";
        
 	   @SidedProxy(clientSide="arcadia.client.ClientProxyArcadia", serverSide="arcadia.CommonProxyArcadia")
        public static CommonProxyArcadia proxy;
 	   
 	   public static CreativeTabs tabArcadiaBlocks;
 	   public static CreativeTabs tabArcadiaItems;
        public static CreativeTabs tabArcadiaPotions;
        
        private GuiHandlerArcadia guiHandlerArcadia = new GuiHandlerArcadia();
 	          
        static EnumToolMaterial toolSILVER = EnumHelper.addToolMaterial("Silver", 2, 80, 10F, 3F, 18);
        static EnumToolMaterial toolBRONZE = EnumHelper.addToolMaterial("Bronze", 2, 500, 6.5F, 2F, 15);
        static EnumToolMaterial toolRUBY = EnumHelper.addToolMaterial("Ruby", 2, 800, 11F, 4, 18);
    	   static EnumToolMaterial toolSAPPHIRE= EnumHelper.addToolMaterial("Sapphire", 2, 2000, 8F, 2, 16);
        static EnumArmorMaterial armorSILVER = EnumHelper.addArmorMaterial("Silver", 10, new int[]{2, 5, 4, 1}, 18);
        static EnumArmorMaterial armorBRONZE = EnumHelper.addArmorMaterial("Bronze", 18, new int[]{2, 6, 5, 2}, 15);
 	   static EnumArmorMaterial armorRUBY = EnumHelper.addArmorMaterial("Ruby", 22, new int[]{3, 8, 6, 3}, 18);
 	   static EnumArmorMaterial armorSAPPHIRE = EnumHelper.addArmorMaterial("Sapphire", 44, new int[]{3, 7, 5, 3}, 16);
 	   	
        /*
        WOOD(0, 59, 2.0F, 0.0F, 15),
 	   STONE(1, 131, 4.0F, 1.0F, 5),
 	   IRON(2, 250, 6.0F, 2.0F, 14),
 	   EMERALD(3, 1561, 8.0F, 3.0F, 10),
 	   GOLD(0, 32, 12.0F, 0.0F, 22);
        CLOTH(5, new int[]{1, 3, 2, 1}, 15),
        CHAIN(15, new int[]{2, 5, 4, 1}, 12),
        IRON(15, new int[]{2, 6, 5, 2}, 9),
        GOLD(7, new int[]{2, 5, 3, 1}, 25),
        DIAMOND(33, new int[]{3, 8, 6, 3}, 10);*/
        
 	   //----Ingots----//
        public static Item ingotSilver;
        public static Item ingotCopper;
        public static Item ingotTin;
        public static Item ingotLead;
        public static Item ingotBronze;
        public static Item blankBronze;
        public static Item gemRuby;
        public static Item gemSapphire;
        
        //----Swords----//
        public static Item swordSilver;
        public static Item swordBronze;
        public static Item swordRuby;
        public static Item swordSapphire;
        
        //----Pickaxe----//
        public static Item pickaxeSilver;
        public static Item pickaxeBronze;
        public static Item pickaxeRuby;
        public static Item pickaxeSapphire;
        
        //----Shovel----//
        public static Item shovelSilver;
        public static Item shovelBronze;
        public static Item shovelRuby;
        public static Item shovelSapphire;
        
        //----Axe----//
        public static Item axeSilver;
        public static Item axeBronze;
        public static Item axeRuby;
        public static Item axeSapphire;
        
        //----Hoe----//
        public static Item hoeSilver;
        public static Item hoeBronze;
        public static Item hoeRuby;
        public static Item hoeSapphire;
        
        //----Helmet----//
        public static Item helmetSilver;
        public static Item helmetBronze;
        public static Item helmetRuby;
        public static Item helmetSapphire;
        
        //----Chestplate----//
        public static Item chestplateSilver;
        public static Item chestplateBronze;
        public static Item chestplateRuby;
        public static Item chestplateSapphire;
        
        //----Leggings----//
        public static Item leggingsSilver;
        public static Item leggingsBronze;
        public static Item leggingsRuby;
        public static Item leggingsSapphire;
        
        //----Boots----//
        public static Item bootsSilver;
        public static Item bootsBronze;
        public static Item bootsRuby;
        public static Item bootsSapphire;
        
        //----Crossbows----//
        public static Item crossbowWood;
        
        //----Bags----//
        public static Item bagSmall;
        public static Item bagMedium;
        public static Item bagHuge;
        public static Item bagEnder;
        
        /** This is used to keep track of GUIs that we make*/
        private static int modGuiIndex = 0;
 
        /** Set our custom inventory Gui index to the next available Gui index */
       //public static final int ItemInventoryGuiIndex = modGuiIndex++;
       public static final int ItemInventoryGuiIndex = 1;
        
        
        
        
        //----Ores----//
        public static Block oreSilver;
        public static Block oreCopper;
        public static Block oreTin;
        public static Block oreLead;
        public static Block oreRuby;
        public static Block oreSapphire;
        
        //----Oreblocks----//       
        public static Block blockSilver;
        public static Block blockCopper;
        public static Block blockTin;
        public static Block blockLead;
        public static Block blockRuby;
        public static Block blockSapphire;
        public static Block blockBronze;
        
        //----RedRock----//
        public static Block redRock;
        
        //----Enchantments----//
        public static final Enchantment knockup = new EnchantmentKnockup(52, 1);
 	   public static final Enchantment stun = new EnchantmentStun(53, 1);
 	   public static final Enchantment poison = new EnchantmentPoison(54, 1);
 	   public static final Enchantment wither = new EnchantmentWither(55, 1);
 	   
 	   //----Anvil----//
 	   public static Block anvilArcadia;
 	   
 	   //----Achievements----//
 	   public static AchievementPage pageArcadia;
 	   public static Achievement achievementNewOreArcadia;
 	   public static Achievement achievementShinyThingsArcadia;
 	   
 	   //----Commands----/
 	   public static final ICommand commandHeal = new CommandHealArcadia();;
 	   public static final ICommand commandEnderchest = new CommandEnderChest(); 
 	   
 	   //----Potions----//
 	   public static Item potionHaste;
 	   public static Item potionMiningFatigue;
 	   public static Item potionJumpBoost;
 	   public static Item potionNausea;
 	   public static Item potionWaterBreathing;
 	   public static Item potionBlindness;
 	   public static Item potionWither;
 	   
 	   public static final String waterBreathing = "-0+1-2-3&4-4+13";
 	   //Absorption,Resistence,Hunger,Saturation PotionHelper
 	   
 	   //----Food----//
 	   public static Item friedEgg;
 	   
 	   
 	   
 	   //TODO	   
        EventManager eventmanager = new EventManager();
       
        @EventHandler
        public void load(FMLInitializationEvent event)
        {
 
     	   tabArcadiaBlocks = new CreativeTabs("tabArcadiaBlocks") { public ItemStack getIconItemStack() {
                return new ItemStack(blockSilver, 1, 0);}};
     	   tabArcadiaItems = new CreativeTabs("tabArcadiaItems") { public ItemStack getIconItemStack() {
                        return new ItemStack(ingotSilver, 1, 0);}};
            tabArcadiaPotions = new CreativeTabs("tabArcadiaPotions") { public ItemStack getIconItemStack() {
     	                   return new ItemStack(potionHaste, 1, 0);}};
     	   
 	                   
 	       //----Ingots----//
     	   ingotSilver = new ItemArcadia(1001).setUnlocalizedName("ingotSilver");
     	   ingotCopper = new ItemArcadia(1002).setUnlocalizedName("ingotCopper");
     	   ingotTin = new ItemArcadia(1003).setUnlocalizedName("ingotTin");
     	   ingotLead = new ItemArcadia(1004).setUnlocalizedName("ingotLead");
     	   ingotBronze = new ItemArcadia(1005).setUnlocalizedName("ingotBronze");
     	   blankBronze = new ItemArcadia(1006).setUnlocalizedName("blankBronze");
     	   gemRuby = new ItemArcadia(1007).setUnlocalizedName("gemRuby");
     	   gemSapphire = new ItemArcadia(1008).setUnlocalizedName("gemSapphire");
     	   
     	   //----Swords----//
     	   swordSilver = new ItemArcadiaSword(1010, toolSILVER).setUnlocalizedName("swordSilver");
     	   swordBronze = new ItemArcadiaSword(1011, toolBRONZE).setUnlocalizedName("swordBronze");
     	   swordRuby = new ItemArcadiaSword(1012, toolRUBY).setUnlocalizedName("swordRuby");
     	   swordSapphire = new ItemArcadiaSword(1013, toolSAPPHIRE).setUnlocalizedName("swordSapphire");
     	   
     	   //----Pickaxe----//
     	   pickaxeSilver = new ItemArcadiaPickaxe(1020, toolSILVER).setUnlocalizedName("pickaxeSilver");
     	   pickaxeBronze = new ItemArcadiaPickaxe(1021, toolBRONZE).setUnlocalizedName("pickaxeBronze");
     	   pickaxeRuby = new ItemArcadiaPickaxe(1022, toolRUBY).setUnlocalizedName("pickaxeRuby");
     	   pickaxeSapphire = new ItemArcadiaPickaxe(1023, toolSAPPHIRE).setUnlocalizedName("pickaxeSapphire");
     	   
     	   //----Shovel----//
     	   shovelSilver = new ItemArcadiaShovel(1030, toolSILVER).setUnlocalizedName("shovelSilver");
     	   shovelBronze = new ItemArcadiaShovel(1031, toolBRONZE).setUnlocalizedName("shovelBronze");
     	   shovelRuby = new ItemArcadiaShovel(1032, toolRUBY).setUnlocalizedName("shovelRuby");
     	   shovelSapphire = new ItemArcadiaShovel(1033, toolSAPPHIRE).setUnlocalizedName("shovelSapphire");
     	   
     	   //----Axe----//
     	   axeSilver = new ItemArcadiaAxe(1040, toolSILVER).setUnlocalizedName("axeSilver");
     	   axeBronze = new ItemArcadiaAxe(1041, toolBRONZE).setUnlocalizedName("axeBronze");
     	   axeRuby = new ItemArcadiaAxe(1042, toolRUBY).setUnlocalizedName("axeRuby");
     	   axeSapphire = new ItemArcadiaAxe(1043, toolSAPPHIRE).setUnlocalizedName("axeSapphire");
     	   
     	   //----Hoe----//
     	   hoeSilver = new ItemArcadiaHoe(1050, toolSILVER).setUnlocalizedName("hoeSilver");
     	   hoeBronze = new ItemArcadiaHoe(1051, toolBRONZE).setUnlocalizedName("hoeBronze");
     	   hoeRuby = new ItemArcadiaHoe(1052, toolRUBY).setUnlocalizedName("hoeRuby");
     	   hoeSapphire = new ItemArcadiaHoe(1053, toolSAPPHIRE).setUnlocalizedName("hoeSapphire");
     	   
     	   //----Helmet----//
     	   helmetSilver = new ItemArcadiaArmor(1060, armorSILVER, ModLoader.addArmor("Silver"), 0).setUnlocalizedName("helmetSilver");
     	   helmetBronze = new ItemArcadiaArmor(1061, armorBRONZE, ModLoader.addArmor("Bronze"), 0).setUnlocalizedName("helmetBronze");
     	   helmetRuby = new ItemArcadiaArmor(1062, armorRUBY, ModLoader.addArmor("Ruby"), 0).setUnlocalizedName("helmetRuby");
     	   helmetSapphire = new ItemArcadiaArmor(1063, armorSAPPHIRE, ModLoader.addArmor("Sapphire"), 0).setUnlocalizedName("helmetSapphire");
     	   
     	   //----Chestplate----//
     	   chestplateSilver = new ItemArcadiaArmor(1070, armorSILVER, ModLoader.addArmor("Silver"), 1).setUnlocalizedName("chestplateSilver");
     	   chestplateBronze = new ItemArcadiaArmor(1071, armorBRONZE, ModLoader.addArmor("Bronze"), 1).setUnlocalizedName("chestplateBronze");
     	   chestplateRuby = new ItemArcadiaArmor(1072, armorRUBY, ModLoader.addArmor("Ruby"), 1).setUnlocalizedName("chestplateRuby");
     	   chestplateSapphire = new ItemArcadiaArmor(1073, armorSAPPHIRE, ModLoader.addArmor("Sapphire"), 1).setUnlocalizedName("chestplateSapphire");
     	   
     	   //----Leggings----//
     	   leggingsSilver = new ItemArcadiaArmor(1080, armorSILVER, ModLoader.addArmor("Silver"), 2).setUnlocalizedName("leggingsSilver");
     	   leggingsBronze = new ItemArcadiaArmor(1081, armorBRONZE, ModLoader.addArmor("Bronze"), 2).setUnlocalizedName("leggingsBronze");
     	   leggingsRuby = new ItemArcadiaArmor(1082, armorRUBY, ModLoader.addArmor("Ruby"), 2).setUnlocalizedName("leggingsRuby");
     	   leggingsSapphire = new ItemArcadiaArmor(1083, armorSAPPHIRE, ModLoader.addArmor("Sapphire"), 2).setUnlocalizedName("leggingsSapphire");
     	   
     	   //----Boots----//
     	   bootsSilver = new ItemArcadiaArmor(1090, armorSILVER, ModLoader.addArmor("Silver"), 3).setUnlocalizedName("bootsSilver");
     	   bootsBronze = new ItemArcadiaArmor(1091, armorBRONZE, ModLoader.addArmor("Bronze"), 3).setUnlocalizedName("bootsBronze");
     	   bootsRuby = new ItemArcadiaArmor(1092, armorRUBY, ModLoader.addArmor("Ruby"), 3).setUnlocalizedName("bootsRuby");
     	   bootsSapphire = new ItemArcadiaArmor(1093, armorSAPPHIRE, ModLoader.addArmor("Sapphire"), 3).setUnlocalizedName("bootsSapphire");
     	   
     	   //----Potions----//
     	   potionHaste = new ItemArcadiaPotion(1100, Potion.digSpeed.id, 180, 0).setUnlocalizedName("potionHaste");
     	   potionMiningFatigue = new ItemArcadiaPotion(1101, Potion.digSlowdown.id, 180, 0).setUnlocalizedName("potionMiningFatigue");
     	   potionJumpBoost = new ItemArcadiaPotion(1102, Potion.jump.id, 180, 0).setUnlocalizedName("potionJumpBoost");
     	   potionNausea = new ItemArcadiaPotion(1103, Potion.confusion.id, 180, 0).setUnlocalizedName("potionNausea");
     	   potionWaterBreathing = new ItemArcadiaPotion(1104, Potion.waterBreathing.id, 180, 0).setPotionEffect(Potion.waterBreathing.id, 180, 0, 1F).setUnlocalizedName("potionWaterBreathing");
     	   potionBlindness = new ItemArcadiaPotion(1105, Potion.blindness.id, 180, 0).setUnlocalizedName("potionBlindness");
     	   potionWither = new ItemArcadiaPotion(1106, Potion.wither.id, 45, 0).setUnlocalizedName("potionWither");
     	   gemRuby.setPotionEffect(waterBreathing);
     	   
     	   //----Crossbows----//
     	   crossbowWood = new ItemCrossbowArcadia(1110).setUnlocalizedName("crossbow");
     	   
     	   //----Food----//
     	   friedEgg = new ItemFoodArcadia(1120, 6, 2F, false).setUnlocalizedName("friedEgg");
     	   
     	   //----Bags----//
     	   bagEnder = new ItemEnderBagArcadia(1121).setUnlocalizedName("bagEnder");
     	   bagHuge = new ItemArcadiaBag(1122).setUnlocalizedName("bagHuge");
     	   
     	   //----Ores----//
     	   oreSilver = new BlockArcadia(500, Material.rock).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSilver");
     	   oreCopper = new BlockArcadia(501, Material.rock).setHardness(2.5F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCopper");
     	   oreTin = new BlockArcadia(502, Material.rock).setHardness(2.5F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreTin");
     	   oreLead = new BlockArcadia(503, Material.rock).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreLead");
     	   oreRuby = new BlockArcadia(504, Material.rock).setHardness(4.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreRuby");
     	   oreSapphire = new BlockArcadia(505, Material.rock).setHardness(4.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSapphire");
     	   
     	   //----Oreblocks----//
     	   blockSilver = new BlockArcadia(510, Material.iron).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockSilver");
     	   blockCopper = new BlockArcadia(511, Material.iron).setHardness(2.5F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockCopper");
     	   blockTin = new BlockArcadia(512, Material.iron).setHardness(2.5F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockTin");
     	   blockLead = new BlockArcadia(513, Material.iron).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockLead");
     	   blockRuby = new BlockArcadia(514, Material.iron).setHardness(6.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockRuby");
     	   blockSapphire = new BlockArcadia(515, Material.iron).setHardness(6.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockSapphire");
     	   blockBronze = new BlockArcadia(516, Material.iron).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockBronze");
     	   
     	   //----RedRock----//
     	   redRock = new BlockArcadiaRedRock(530, Material.rock).setUnlocalizedName("redRock");
     	   
     	   //----Anvil----//
     	   anvilArcadia = new BlockAnvilArcadia(600).setHardness(2.5F).setResistance(20.0F).setStepSound(Block.soundAnvilFootstep).setUnlocalizedName("anvil");
     	   NetworkRegistry.instance().registerGuiHandler(this, guiHandlerArcadia);
     	   
     	   //----Achievement----//
     	   achievementNewOreArcadia = new Achievement(1000, "NewOre", 0, 0, oreSilver, AchievementList.mineWood).registerAchievement();
     	   achievementShinyThingsArcadia = new Achievement(1001, "ShinyThings", -1, 2, gemRuby, achievementNewOreArcadia).setSpecial().registerAchievement();
     	   
     	   pageArcadia = new AchievementPage("World of Arcadia", achievementNewOreArcadia, achievementShinyThingsArcadia);
     	   AchievementPage.registerAchievementPage(pageArcadia);
     	   
     	   //----Commands----//
     	  // commandHeal = new CommandHealArcadia();
     	   //ICommandManager manager = MinecraftServer.getServer().getCommandManager();
     	   //ServerCommandManager servermanager = ((ServerCommandManager)manager);
     	   //servermanager.registerCommand(commandHeal);
     	   //ModLoader.addCommand(commandHeal);
     	   
     	   
     	   
     	   
     	   
     	   //TODO
     	   GameRegistry.registerWorldGenerator(eventmanager);
     	   GameRegistry.registerCraftingHandler(new CraftingHandler());
     	   GameRegistry.registerPickupHandler(new PickupHandler());
     	   
     	   LanguageRegistry.instance().addStringLocalization("itemGroup.tabArcadiaBlocks", "en_US", "\u00a79World of Arcadia Blocks");
     	   LanguageRegistry.instance().addStringLocalization("itemGroup.tabArcadiaItems", "en_US", "\u00a79World of Arcadia Items");
     	   LanguageRegistry.instance().addStringLocalization("itemGroup.tabArcadiaPotions", "en_US", "\u00a79World of Arcadia Potions");
     	   
     	   
     	   ArcadiaCrafting.addRecipes();
     	   registerBlocks();
     	   registerLanguage();
     	   AchievementLocalizations();
        }
        
        @ServerStarting
        public void serverLoad(FMLServerStartingEvent event) {
     	   event.registerServerCommand(new CommandHealArcadia());
     	   event.registerServerCommand(new CommandEnderChest());
        }
        
        private void registerBlocks(){
     	   //----Ores----//
     	   GameRegistry.registerBlock(oreSilver, modid + oreSilver.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(oreCopper, modid + oreCopper.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(oreTin, modid + oreTin.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(oreLead, modid + oreLead.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(oreRuby, modid + oreRuby.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(oreSapphire, modid + oreSapphire.getUnlocalizedName().substring(5));
     	   
     	   //----Oreblock----//
     	   GameRegistry.registerBlock(blockSilver, modid + blockSilver.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockCopper, modid + blockCopper.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockTin, modid + blockTin.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockLead, modid + blockLead.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockRuby, modid + blockRuby.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockSapphire, modid + blockSapphire.getUnlocalizedName().substring(5));
     	   GameRegistry.registerBlock(blockBronze, modid + blockBronze.getUnlocalizedName().substring(5));
     	   
     	   //----RedRock----//
     	   GameRegistry.registerBlock(redRock, ItemBlockArcadiaRedRock.class, modid + (redRock.getUnlocalizedName().substring(5)));
     	   
     	   GameRegistry.registerBlock(anvilArcadia, modid + anvilArcadia.getUnlocalizedName().substring(5));
     	   
        }
        
        private void registerLanguage(){
     	   //----Items-----//
     	   LanguageRegistry.addName(ingotSilver, "Silver Ingot");
     	   LanguageRegistry.addName(ingotCopper, "Copper Ingot");
 	   	   LanguageRegistry.addName(ingotTin, "Tin Ingot");
 	   	   LanguageRegistry.addName(ingotLead, "Lead Ingot");
 	   	   LanguageRegistry.addName(ingotBronze, "Bronze Ingot");
 	   	   LanguageRegistry.addName(blankBronze, "Blank Bronze");
 	   	   LanguageRegistry.addName(gemRuby, "\u00a7cRuby");
 	   	   LanguageRegistry.addName(gemSapphire, "\u00a7bSapphire");
 	   	   
 		   LanguageRegistry.addName(swordSilver, "Silver Sword");
 		   LanguageRegistry.addName(swordBronze, "Bronze Sword");
 		   LanguageRegistry.addName(swordRuby, "\u00a7cRuby Sword");
 	       LanguageRegistry.addName(swordSapphire, "\u00a7bSapphire Sword");
     	   
 	       LanguageRegistry.addName(pickaxeSilver, "Silver Pickaxe");
 	       LanguageRegistry.addName(pickaxeBronze, "Bronze Pickaxe");
 	       LanguageRegistry.addName(pickaxeRuby, "\u00a7cRuby Pickaxe");
 	       LanguageRegistry.addName(pickaxeSapphire, "\u00a7bSapphire Pickaxe");
     	   
 	       LanguageRegistry.addName(shovelSilver, "Silver Shovel");
 	       LanguageRegistry.addName(shovelBronze, "Bronze Shovel");
 	       LanguageRegistry.addName(shovelRuby, "\u00a7cRuby Shovel");
 	       LanguageRegistry.addName(shovelSapphire, "\u00a7bSapphire Shovel");
 	       
     	   LanguageRegistry.addName(axeSilver, "Silver Axe");
 	   	   LanguageRegistry.addName(axeBronze, "Bronze Axe");
 	   	   LanguageRegistry.addName(axeRuby, "\u00a7cRuby Axe");
 	   	   LanguageRegistry.addName(axeSapphire, "\u00a7bSapphire Axe");
     	   
 	   	   LanguageRegistry.addName(hoeSilver, "Silver Hoe");
 	   	   LanguageRegistry.addName(hoeBronze, "Bronze Hoe");
 	   	   LanguageRegistry.addName(hoeRuby, "\u00a7cRuby Hoe");
 	   	   LanguageRegistry.addName(hoeSapphire, "\u00a7bSapphire Hoe");
     	   
 		   LanguageRegistry.addName(helmetSilver, "Silver Helmet");
 		   LanguageRegistry.addName(helmetBronze, "Bronze Helmet");
 		   LanguageRegistry.addName(helmetRuby, "\u00a7cRuby Helmet");
 		   LanguageRegistry.addName(helmetSapphire, "\u00a7bSapphire Helmet");
 		   LanguageRegistry.addName(chestplateSilver, "Silver Chestplate");
 		   LanguageRegistry.addName(chestplateBronze, "Bronze Chestplate");
 		   LanguageRegistry.addName(chestplateRuby, "\u00a7cRuby Chestplate");
 		   LanguageRegistry.addName(chestplateSapphire, "\u00a7bSapphire Chestplate");
 		   LanguageRegistry.addName(leggingsSilver, "Silver Leggings");
 		   LanguageRegistry.addName(leggingsBronze, "Bronze Leggings");
 		   LanguageRegistry.addName(leggingsRuby, "\u00a7cRuby Leggings");
 		   LanguageRegistry.addName(leggingsSapphire, "\u00a7bSapphire Leggings");
 		   LanguageRegistry.addName(bootsSilver, "Silver Boots");
 		   LanguageRegistry.addName(bootsBronze, "Bronze Boots");
 		   LanguageRegistry.addName(bootsRuby, "\u00a7cRuby Boots");
 		   LanguageRegistry.addName(bootsSapphire, "\u00a7bSapphire Boots");
 		   
 		   LanguageRegistry.addName(potionHaste, "Potion of Haste");
 		   LanguageRegistry.addName(potionMiningFatigue, "Potion of Dullness");
 		   LanguageRegistry.addName(potionJumpBoost, "Potion of Leaping");
 		   LanguageRegistry.addName(potionNausea, "Potion of Nausea");
 		   LanguageRegistry.addName(potionWaterBreathing, "Potion of Water Breathing");
 		   LanguageRegistry.addName(potionBlindness, "Potion of Blindness");
 		   LanguageRegistry.addName(potionWither, "Potion of Decay");
 		   
 		   LanguageRegistry.addName(crossbowWood, "Wood Crossbow");
 		   
 		   LanguageRegistry.addName(friedEgg, "Fried Egg");
 		   
 		   LanguageRegistry.addName(bagEnder, "Ender Bag");
 		   LanguageRegistry.addName(bagHuge, "Huge Bag");
 		   
     	   
     	   //----Blocks----//
     	   LanguageRegistry.addName(oreSilver, "Silver Ore");
     	   LanguageRegistry.addName(oreCopper, "Copper Ore");
     	   LanguageRegistry.addName(oreTin, "Tin Ore");
     	   LanguageRegistry.addName(oreLead, "Lead Ore");
     	   LanguageRegistry.addName(oreRuby, "\u00a7cRuby Ore");
     	   LanguageRegistry.addName(oreSapphire, "\u00a7bSapphire Ore");
     	   
     	   LanguageRegistry.addName(blockSilver, "Block of Silver");
     	   LanguageRegistry.addName(blockCopper, "Block of Copper");
     	   LanguageRegistry.addName(blockTin, "Block of Tin");
     	   LanguageRegistry.addName(blockLead, "Block of Lead");
     	   LanguageRegistry.addName(blockRuby, "\u00a7cBlock of Ruby");
     	   LanguageRegistry.addName(blockSapphire, "\u00a7bBlock of Sapphire");
     	   LanguageRegistry.addName(blockBronze, "Block of Bronze");
     	   
     	   LanguageRegistry.addName(new ItemStack(redRock, 1, 0), "Red Rock");
 	   	   LanguageRegistry.addName(new ItemStack(redRock, 1, 1), "Red Rock Cobblestone");
 	   	   LanguageRegistry.addName(new ItemStack(redRock, 1, 2), "Red Rock Brick");
 	   	   LanguageRegistry.addName(new ItemStack(redRock, 1, 3), "Red Rock Brick Carved");
 	   	   LanguageRegistry.addName(new ItemStack(redRock, 1, 4), "Red Rock Brick Cracked");
     	   
 	   	   
     	   LanguageRegistry.addName(anvilArcadia, "Anvil Arcadia");
     	       	   
     	       	   
        }
        
        public void AchievementLocalizations(){
     	   LanguageRegistry.instance().addStringLocalization("achievement.NewOre", "en_US", "Search for new Ores");
            LanguageRegistry.instance().addStringLocalization("achievement.NewOre.desc", "en_US", "You found new Ores!");
            LanguageRegistry.instance().addStringLocalization("achievement.ShinyThings", "en_US", "Look for new stuff");
            LanguageRegistry.instance().addStringLocalization("achievement.ShinyThings.desc", "en_US", "You found Shiny Things!");
            
        }
               	   
 }

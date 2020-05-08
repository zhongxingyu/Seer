 package ruby.bamboo;
 
 import static ruby.bamboo.Config.*;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import ruby.bamboo.block.*;
 import ruby.bamboo.item.*;
 import ruby.bamboo.item.magatama.*;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStairs;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.settings.GameSettings;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 
 public class BambooInit
 {
     private static final int ITEMID_OFFSET = 256;
     //instance
     public static BambooInit instance = new BambooInit();
     //blocks
     public static int bambooBlockBID;
     public static int bambooBID;
     public static int sakuraLogBID;
     public static int kitunebiBID;
     public static int spaBID;
     public static int jpchestBID;
     public static int dSquareBID;
     public static int andonBID;
     public static int dHalfSquareBID;
     public static int bamboopaneBID;
     public static int sakuraleavsBID;
     public static int sakuraBID;
     public static int rooftileBID;
     public static int spaunitBID;
     public static int delude_widthBID;
     public static int delude_heightBID;
     public static int delude_stairBID;
     public static int campfireBID;
     public static int decoBID;
     public static int halfDecoBID;
     public static int twoDirDecoBID;
     public static int halfTwoDirDecoBID;
     public static int kawara_stairBID;
     public static int halfKawaraBID;
     public static int riceFieldBID;
     public static int ricePlantBID;
     public static int millStoneBID;
     public static int decoCarpetBID;
     public static int thickSakuraPillarBID;
     public static int thinSakuraPillarBID;
     public static int thickOrcPillarBID;
     public static int thinOrcPillarBID;
     public static int thickSprucePillarBID;
     public static int thinSprucePillarBID;
     public static int delude_plateBID;
     public static int manekiBID;
     //items
     public static int bambooBasketIID;
     public static int firecrackerIID;
     public static int foodsIID;
     public static int windChimeIID;
     public static int itemSackIID;
     public static int shavedIceIID;
     public static int bambooSpearIID;
     public static int slideDoorsIID;
     public static int kakezikuIID;
     public static int hutonIID;
     public static int takenokoIID;
     public static int boiledEggIID;
     public static int snowBallIID;
     public static int fanIID;
     public static int katanaIID;
     public static int bambooBowIID;
     public static int windmillIID;
     public static int waterWheelIID;
     public static int bambooIID;
     public static int tuduraIID;
     public static int kaginawaIID;
     public static int bambooSwordIID;
     public static int obonIID;
     public static int bitchuHoeIID;
     public static int seedRiceIID;
     public static int strawIID;
     public static int magatamaIID;
 
     public static void init()
     {
         instance.idsInit();
         instance.blocksInit();
         instance.itemsInit();
     }
     private void blocksInit()
     {
         try
         {
            registerBlock(new BlockBamboo(bambooBID, getChiledName()).setUnlocalizedName("bamboo"), ItemBlock.class, false);
             registerBlock(new BlockCustomRenderSingleTexture(bambooBlockBID).setUnlocalizedName("singleTexDeco"), ItemCustomRenderSingleTexture.class, true);
             registerBlock(new BlockKitunebi(kitunebiBID).setUnlocalizedName("kitunebi").setTextureName("kitunebi"));
             registerBlock(new BlockJpchest(jpchestBID).setUnlocalizedName("jpChest"));
             registerBlock(new BlockDSquare(dSquareBID, false).addTexName("tatami_x", "tatami_y", "tatami_x", "tatami_y", "kaya_x", "kaya_y", "kaya_x", "kaya_y").addTexName("tatami_tan_x", "tatami_tan_y", "tatami_tan_nsx", "tatami_tan_nsy", "tatami_nsx", "tatami_nsy", "tatami_nsx", "tatami_nsy").setUnlocalizedName("dirSquare"), ItemDSquare.class, true);
             registerBlock(new BlockDSquare(dHalfSquareBID, true).addTexName("tatami_x", "tatami_y", "tatami_x", "tatami_y", "kaya_x", "kaya_y", "kaya_x", "kaya_y").addTexName("tatami_tan_x", "tatami_tan_y", "tatami_tan_nsx", "tatami_tan_nsy", "tatami_nsx", "tatami_nsy", "tatami_nsx", "tatami_nsy").setUnlocalizedName("halfDirSquare"), ItemDSquare.class, true);
             registerBlock(new BlockAndon(andonBID).setUnlocalizedName("andon"));
             registerBlock(new BlockBambooPane(bamboopaneBID, Material.ground).setUnlocalizedName("bambooPanel"), ItemBambooPane.class, true);
             registerBlock(new BlockSakuraLeaves(sakuraleavsBID).setUnlocalizedName("sakuraLeaves"), ItemSakuraPetal.class, true);
             registerBlock(new BlockSakura(sakuraBID).setUnlocalizedName("sakura").setTextureName("sakura"));
             registerBlock(new BlockRooftile(rooftileBID).setUnlocalizedName("rooftile"), ItemRooftile.class, true);
             registerBlock(new BlockSpaWater(spaBID, Material.water).setUnlocalizedName("spaWater"), ItemBlock.class, false);
             registerBlock(new BlockSpaUnit(spaunitBID).setUnlocalizedName("spaUnit"));
             registerBlock(new BlockSakuraLog(sakuraLogBID).setUnlocalizedName("sakuraLog"), ItemBlock.class, true);
             registerBlock(new BlockDelude(delude_widthBID, false).setUnlocalizedName("delude_width"));
             registerBlock(new BlockDelude(delude_heightBID, true).setUnlocalizedName("delude_height"));
             registerBlock(new BlockDeludeStair(delude_stairBID).setUnlocalizedName("delude_stair"));
             registerBlock(new BlockCampfire(campfireBID).setUnlocalizedName("campfire"));
             registerBlock(new BlockDecorations(decoBID, Material.ground, false).addTexName("plaster", "namako", "check_oak", "check_pine", "check_birch", "kawara").setUnlocalizedName("deco"), ItemDecorationBlocks.class, true);
             registerBlock(new BlockDecorations(halfDecoBID, Material.ground, true).addTexName("plaster", "namako", "check_oak", "check_pine", "check_birch", "kawara").setUnlocalizedName("halfDeco"), ItemDecorationBlocks.class, true);
             registerBlock(new BlockTwoDirections(twoDirDecoBID, Material.wood, false).addTexName("yoroiita", "sakuraplank").setUnlocalizedName("twoDirDeco"), ItemDecorationBlocks.class, true);
             registerBlock(new BlockTwoDirections(halfTwoDirDecoBID, Material.wood, true).addTexName("yoroiita", "sakuraplank").setUnlocalizedName("halfTwoDirDeco"), ItemDecorationBlocks.class, true);
             registerBlock(new BlockKawaraStair(kawara_stairBID, getBlockInstance(decoBID), 5).setUnlocalizedName("kawara_stair"));
             registerBlock(new BlockRiceField(riceFieldBID, Material.ground).setUnlocalizedName("ricefield"), ItemBlock.class, false);
             registerBlock(new BlockRicePlant(ricePlantBID).setUnlocalizedName("riceplant"), ItemBlock.class, false);
             registerBlock(new BlockMillStone(millStoneBID).setUnlocalizedName("millstone"));
             registerBlock(new BlockDecoCarpet(decoCarpetBID).setUnlocalizedName("decoCarpet"), ItemDecoCarpet.class, true);
             registerBlock(new BlockPillar(thickSakuraPillarBID,getBlockInstance(twoDirDecoBID),2, 0.3F, 0.7F, 0.2F).setUnlocalizedName("thickSakuraPillar"), ItemPillar.class, true);
             registerBlock(new BlockPillar(thinSakuraPillarBID,getBlockInstance(twoDirDecoBID),2, 0.4F, 0.6F, 0.15F).setUnlocalizedName("thinSakuraPillar"), ItemPillar.class, true);
             registerBlock(new BlockPillar(thickOrcPillarBID,Block.planks,0, 0.3F, 0.7F, 0.2F).setUnlocalizedName("thickOrcPillar"), ItemPillar.class, true);
             registerBlock(new BlockPillar(thinOrcPillarBID,Block.planks,0, 0.4F, 0.6F, 0.15F).setUnlocalizedName("thinOrcPillar"), ItemPillar.class, true);
             registerBlock(new BlockPillar(thickSprucePillarBID,Block.wood,1, 0.3F, 0.7F, 0.2F).setUnlocalizedName("thickSprucePillar"), ItemPillar.class, true);
             registerBlock(new BlockPillar(thinSprucePillarBID,Block.wood,1, 0.4F, 0.6F, 0.15F).setUnlocalizedName("thinSprucePillar"), ItemPillar.class, true);
             registerBlock(new BlockDeludePressurePlate(delude_plateBID).setUnlocalizedName("delude_plate"));
             registerBlock(new BlockManeki(manekiBID, Material.ground).setUnlocalizedName("maneki"));
         }
         catch (IllegalArgumentException e)
         {
             slotOccupiedException(e);
         }
     }
     public static Block getBlockInstance(int blockID)
     {
         return BambooUtil.getBlcokInstance(blockID);
     }
     public static Item getItemInstance(int itemID)
     {
         return BambooUtil.getItemInstance(itemID);
     }
     private void itemsInit()
     {
         new ItemBambooBasket(bambooBasketIID - ITEMID_OFFSET).setMaxStackSize(64).setUnlocalizedName("bamboobasket").setCreativeTab(BambooCore.tabBamboo);
         new ItemFirecracker(firecrackerIID - ITEMID_OFFSET).setMaxStackSize(64).setUnlocalizedName("firecracker");
         new ItemBambooFood(foodsIID - ITEMID_OFFSET).setMaxStackSize(64).setUnlocalizedName("bambooFood");
         new ItemBambooSpear(bambooSpearIID - ITEMID_OFFSET).setUnlocalizedName("bamboospear");
         new ItemSlideDoor(slideDoorsIID - ITEMID_OFFSET).setUnlocalizedName("slideDoor");
         new ItemKakeziku(kakezikuIID - ITEMID_OFFSET).setUnlocalizedName("kakeziku");
         new ItemHuton(hutonIID - ITEMID_OFFSET).setUnlocalizedName("huton");
         new ItemBambooshoot(takenokoIID - ITEMID_OFFSET, bambooBID).setUnlocalizedName("bambooshoot").setMaxStackSize(64);
         new ItemBoiledEgg(boiledEggIID - ITEMID_OFFSET).setUnlocalizedName("boiledEgg");
         new ItemShavedIce(shavedIceIID - ITEMID_OFFSET).setUnlocalizedName("shavedice");
         new ItemWindChime(windChimeIID - ITEMID_OFFSET).setUnlocalizedName("windchime");
         new ItemSack(itemSackIID - ITEMID_OFFSET).setUnlocalizedName("itemsack");
         new ItemDirtySnowball(snowBallIID - ITEMID_OFFSET).setUnlocalizedName("snowball");
         new ItemFan(fanIID - ITEMID_OFFSET).setUnlocalizedName("fan");
         new ItemKatana(katanaIID - ITEMID_OFFSET).setUnlocalizedName("katana");
         new ItemBambooBow(bambooBowIID - ITEMID_OFFSET).setUnlocalizedName("bamboobow");
         new ItemTudura(tuduraIID - ITEMID_OFFSET).setUnlocalizedName("tudura").setCreativeTab(BambooCore.tabBamboo);
         new ItemWindmill(windmillIID - ITEMID_OFFSET).setUnlocalizedName("windmill");
         new ItemWaterwheel(waterWheelIID - ITEMID_OFFSET).setUnlocalizedName("waterwheel");
         new ItemBamboo(bambooIID - ITEMID_OFFSET).setCreativeTab(BambooCore.tabBamboo).setUnlocalizedName("itembamboo");
         new ItemKaginawa(kaginawaIID - ITEMID_OFFSET).setCreativeTab(BambooCore.tabBamboo).setUnlocalizedName("kaginawa");
         new ItemBambooSword(bambooSwordIID - ITEMID_OFFSET).setCreativeTab(BambooCore.tabBamboo).setUnlocalizedName("bamboosword");
         new ItemObon(obonIID - ITEMID_OFFSET).setCreativeTab(BambooCore.tabBamboo).setUnlocalizedName("obon");
         new ItemBitchuHoe(bitchuHoeIID - ITEMID_OFFSET).setUnlocalizedName("bitchuhoe").setCreativeTab(BambooCore.tabBamboo);
         new ItemSeeds(seedRiceIID - ITEMID_OFFSET, ricePlantBID, riceFieldBID).setUnlocalizedName("seedrice").setTextureName("seedrice").setCreativeTab(BambooCore.tabBamboo);
         MinecraftForge.addGrassSeed(new ItemStack(seedRiceIID, 1, 0), 10);
         new Item(strawIID - ITEMID_OFFSET).setUnlocalizedName("straw").setTextureName("straw");
         new ItemMagatama(magatamaIID - ITEMID_OFFSET).setUnlocalizedName("magatama").setTextureName("magatama").setCreativeTab(BambooCore.tabBamboo);
     }
 
     public static void registerBlock(Block b)
     {
         registerBlock(b, ItemBlock.class, true);
     }
 
     public static void registerBlock(Block b, Class c, boolean creativeTabs)
     {
         if (creativeTabs)
         {
             b.setCreativeTab(BambooCore.tabBamboo);
         }
        GameRegistry.registerBlock(b, c, b.getUnlocalizedName());
     }
 
    private String getChiledName()
     {
         String chiledName = "bambooshoot";
         Calendar ci = Calendar.getInstance();
         int date = ci.get(Calendar.DATE);
 
         switch (ci.get(Calendar.MONTH))
         {
             case Calendar.JANUARY:
                 if (date == 1 || date == 2 || date == 3)
                 {
                     chiledName = "bambooshoot0101";
                 }
 
                 break;
 
             case Calendar.DECEMBER:
                 if (date == 24 || date == 25)
                 {
                     chiledName = "bambooshoot1224";
                 }
 
                 break;
 
             case Calendar.FEBRUARY:
                 chiledName = "bambooshoot0214";
                 break;
 
             case Calendar.MARCH:
                 if (date == 03)
                 {
                     chiledName = "bambooshoot0303";
                 }
                 else if (date == 14)
                 {
                     chiledName = "bambooshoot0314";
                 }
 
                 break;
 
             case Calendar.APRIL:
                 if (date == 01)
                 {
                     chiledName = "bambooshoot0401";
                 }
 
                 break;
 
             case Calendar.MAY:
                 if (date == 05)
                 {
                     chiledName = "bambooshoot0505";
                 }
 
                 ci.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                 ci.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
 
                 if (ci.get(Calendar.DATE) == date)
                 {
                     chiledName = "bambooshoot_mother";
                 }
 
                 break;
 
             case Calendar.JUNE:
                 ci.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                 ci.set(Calendar.DAY_OF_WEEK_IN_MONTH, 3);
 
                 if (ci.get(Calendar.DATE) == date)
                 {
                     chiledName = "bambooshoot_father";
                 }
 
                 break;
         }
 
         return chiledName;
     }
 
     private void idsInit()
     {
         File file = new File(Loader.instance().getConfigDir(), "mod_BambooIDConfig.cfg");
         Configuration conf = new Configuration(file);
         conf.load();
         blockIdInit(conf);
         itemIdInit(conf);
         idShifter(conf);
         conf.save();
     }
     private void blockIdInit(Configuration conf)
     {
         bambooBlockBID = getBlockId(conf, "bambooBlock", 3238);
         bambooBID = getBlockId(conf, "bamboo", 3239);
         sakuraLogBID = getBlockId(conf, "sakuraLog", 3240);
         kitunebiBID = getBlockId(conf, "kitunebi", 3241);
         spaBID = getBlockId(conf, "spaWater", 3242);
         jpchestBID = getBlockId(conf, "jpchest", 3243);
         dSquareBID = getBlockId(conf, "dSquare", 3244);
         andonBID = getBlockId(conf, "andon", 3245);
         dHalfSquareBID = getBlockId(conf, "dHalfSquare", 3246);
         bamboopaneBID = getBlockId(conf, "bamboopane", 3247);
         sakuraleavsBID = getBlockId(conf, "sakuraleavs", 3248);
         sakuraBID = getBlockId(conf, "sakura", 3249);
         rooftileBID = getBlockId(conf, "rooftile", 3250);
         spaunitBID = getBlockId(conf, "spaunit", 3251);
         delude_widthBID = getBlockId(conf, "delude_width", 3252);
         delude_heightBID = getBlockId(conf, "delude_height", 3253);
         delude_stairBID = getBlockId(conf, "delude_stair", 3254);
         campfireBID = getBlockId(conf, "campfire", 3255);
         decoBID = getBlockId(conf, "deco", 3256);
         halfDecoBID = getBlockId(conf, "halfDeco", 3257);
         twoDirDecoBID = getBlockId(conf, "twoDirDeco", 3258);
         halfTwoDirDecoBID = getBlockId(conf, "halfTwoDirDeco", 3259);
         kawara_stairBID = getBlockId(conf, "kawara_stair", 3260);
         riceFieldBID = getBlockId(conf, "riceField", 3261);
         ricePlantBID = getBlockId(conf, "ricePlant", 3262);
         millStoneBID = getBlockId(conf, "millStone", 3263);
         decoCarpetBID = getBlockId(conf, "decoCarpet", 3264);
         thickSakuraPillarBID = getBlockId(conf, "thickSakuraPillar", 3265);
         thinSakuraPillarBID = getBlockId(conf, "thinSakuraPillar", 3266);
         thickOrcPillarBID = getBlockId(conf, "thickOrcPillar", 3267);
         thinOrcPillarBID = getBlockId(conf, "thinOrcPillar", 3268);
         thickSprucePillarBID = getBlockId(conf, "thickSprucePillar", 3269);
         thinSprucePillarBID = getBlockId(conf, "thinSprucePillar", 3270);
         delude_plateBID = getBlockId(conf, "delude_plate", 3271);
         manekiBID = getBlockId(conf, "maneki", 3272);
     }
     private int getBlockId(Configuration conf, String key, int defaultId)
     {
         return conf.getBlock(key, defaultId).getInt(defaultId);
     }
     private void itemIdInit(Configuration conf)
     {
         bambooBasketIID = getItemId(conf, "bambooBasket", 23535);
         firecrackerIID = getItemId(conf, "firecracker", 23536);
         foodsIID = getItemId(conf, "foods", 23539);
         windChimeIID = getItemId(conf, "windChime", 23540);
         itemSackIID = getItemId(conf, "itemSack", 23541);
         shavedIceIID = getItemId(conf, "shavedIce", 23542);
         bambooSpearIID = getItemId(conf, "bambooSpear", 23543);
         slideDoorsIID = getItemId(conf, "slideDoors", 23544);
         kakezikuIID = getItemId(conf, "kakeziku", 23545);
         seedRiceIID = getItemId(conf, "seedRice", 23546);
         hutonIID = getItemId(conf, "huton", 23547);
         takenokoIID = getItemId(conf, "takenoko", 23548);
         strawIID = getItemId(conf, "straw", 23549);
         boiledEggIID = getItemId(conf, "boiledEgg", 23550);
         snowBallIID = getItemId(conf, "snowBall", 23551);
         fanIID = getItemId(conf, "fan", 23552);
         katanaIID = getItemId(conf, "katana", 23553);
         bambooBowIID = getItemId(conf, "bambooBow", 23554);
         windmillIID = getItemId(conf, "windmill", 23555);
         waterWheelIID = getItemId(conf, "waterWheel", 23556);
         bambooIID = getItemId(conf, "bamboo", 23557);
         tuduraIID = getItemId(conf, "tudura", 23558);
         kaginawaIID = getItemId(conf, "kaginawa", 23559);
         bambooSwordIID = getItemId(conf, "bambooSword", 23560);
         obonIID = getItemId(conf, "obon", 23561);
         bitchuHoeIID = getItemId(conf, "bitchuHoe", 23562);
         magatamaIID = getItemId(conf, "magatama", 23563);
     }
     private int getItemId(Configuration conf, String key, int defaultId)
     {
         return conf.getItem(key, defaultId).getInt(defaultId);
     }
     private void idShifter(Configuration conf)
     {
         int blockShiftIndex = conf.get("A", "blockShiftIndex", 0, "BlockID bulk movement").getInt(0);
         int itemShiftIndex = conf.get("A", "itemShiftIndex", 0, "ItemID bulk movement").getInt(0);
 
         for (Field f : this.getClass().getFields())
         {
             try
             {
                 if (f.getName().matches(".*BID"))
                 {
                     f.setInt(this, f.getInt(this) + blockShiftIndex);
                 }
                 else if (f.getName().matches(".*IID"))
                 {
                     f.setInt(this, f.getInt(this) + itemShiftIndex + ITEMID_OFFSET);
                 }
             }
             catch (Exception e)
             {
             	FMLCommonHandler.instance().raiseException(e,"[mod_bamboo] IDShift exception",true);
             }
         }
     }
 
     private void slotOccupiedException(IllegalArgumentException e)
     {
         if (!(FMLCommonHandler.instance().getMinecraftServerInstance() != null && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()))
         {
             if (Minecraft.getMinecraft().gameSettings.language.equals("ja_JP"))
             {
                 String[] message = e.getMessage().split(" ");
                 throw new IllegalArgumentException("\n[mod_Bamboo]BlockID " + message[1] + " が被ってます、トピックへ報告しないでください。\n"
                                                    + "対象Block： " + message[6] + " と " + message[9] + " です。\n"
                                                    + "BlockID変更方法のサポートは致しかねます、各自検索していただくようお願い申し上げます。\n" + e.getMessage());
             }
             else
             {
                 throw e;
             }
         }
         else
         {
             throw e;
         }
     }
 }

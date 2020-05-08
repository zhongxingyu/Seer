 package net.minecraft.src;
 
 import net.minecraft.client.Minecraft;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.*;
 import java.lang.reflect.*;
 import java.io.*;
 import java.util.*;
 import java.sql.Timestamp;
 import java.nio.*;
 import java.util.concurrent.locks.*;
 
 public final class ZMod {
     public static final String version = "7.0-pre for MC 1.5-pre";
     
     private static final String MCPnames[] = {
         // GuiAchievement
         "ga_theAchievement",        "theAchievement",           "f",  // [a-z]+\.[a-z]+, [a-z0-9]+ \+ 8, [a-z0-9]+ \+ 8\);  * starts with theAchivement
         // Entity
         "e_isImmuneToFire",         "isImmuneToFire",           "af", // find "ghast" and guess
         // EntityLiving
         "el_health",                "health",                   "aQ", // "Health" * the one with 3 results
         // EntityPlayerSP
         "eps_sprintToggleTimer",    "sprintToggleTimer",        "d",  // [a-zA-Z]+ = 7; * class has exacty two matches of "* = 7;"
         // EntityPlayer
         "ep_flyToggleTimer",        "flyToggleTimer",           "bO", // ... the second result
         // EntityMinecart
         "em_fuel",                  "fuel",                     "e",  // "Fuel"
         // EntityEnderman
         "ee_canCarryBlocks",        "carriableBlocks",          "d",  // [a-z]+\[[a-zA-Z]+\.[a-zA-Z]+\.[a-zA-Z]+\] = true;
         // TileEntityFurnace
         "tef_furnaceItemStacks",    "furnaceItemStacks",        "d",  // "CookTime"
         // TileEntityChest
         "tec_chestContents",        "chestContents",            "i",  // [^a-z][a-z] = new [a-z]+\[36\]; * smaller file (~5KB)
         // TileEntityDispenser
         "ted_dispenserContents",    "dispenserContents",        "a",  // [^a-z][a-z] = new [a-z]+\[9\]; * bigger file (~3KB)
         // CraftingManager
         "cm_recipes",               "recipes",                  "b",  // ###
         // ShapedRecipes
         "sr_recipeWidth",           "recipeWidth",              "b",  // for\(int i = 0; i <= 3  * last 4 in constructor
         "sr_recipeHeight",          "recipeHeight",             "c",
         "sr_recipeItems",           "recipeItems",              "d",
         "sr_recipeOutput",          "recipeOutput",             "e",
         // ShapelessRecipes
         "lr_recipeOutput",          "recipeOutput",             "a",  // if\(i >= 3\) * in constructor
         "lr_recipeItems",           "recipeItems",              "b",
         // InventoryCrafting
         "ic_stackList",             "stackList",                "a",  // return "container.crafting"; * array there
         // GuiNewChat
         "gnc_ChatLines",       "chatLines",          "c",  // ; [a-z]+\.size\(\) > 100;
         // Block
         "b_blockHardness",          "blockHardness",            "cn", // 1.0F / [a-zA-Z]+ / 100F;
         "b_blockResistance",        "blockResistance",          "co", // return [a-zA-Z]+ / 5F;
         // BlockFire
         "bf_chanceToEncourageFire", "chanceToEncourageFire",    "a",  // , 15, 100\); * arrays in class, no idea what is what - assume ordering
         "bf_abilityToCatchFire",    "abilityToCatchFire",       "b"
     };
     
     //#Consts#Enums#Lookups###################################################
     private static final int KNOWN     = 0x00000001, SOLID    = 0x00000002, LIQUID  = 0x00000004, CRAFT  = 0x00000008,
                              BASIC     = 0x00000010, SPACE    = 0x00000020, TREE    = 0x00000040, GRASS  = 0x00000080,
                              COBBLE    = 0x00000100, DECAL    = 0x00000200, SAND    = 0x00000400, GRAVEL = 0x00000800,
                              ORE       = 0x00001000, OBSIDIAN = 0x00002000, SPAWN   = 0x00004000, TOUCH  = 0x00008000,
                              SANDSTONE = 0x00010000, GROW     = 0x00020000, STORAGE = 0x00040000, EMPTY  = 0x00080000;
     private static final int GHAST=1, COW=2, SPIDER=3, SHEEP=4, 
                              SKELLY=5, CREEPER=6, ZOMBIE=7, SLIME=8, 
                              PIG=9, CHICKEN=10, SQUID=11, PIGZOMBIE=12, 
                              PLAYER=13, LIVING=14, WOLF=15, CAVESPIDER=16, 
                              ENDERMAN=17, SILVERFISH=18, LAVASLIME=19, REDCOW=20, 
                              VILLAGER=21, SNOWMAN=22, BLAZE=23, DRAGON=24, 
                              GOLEM=25, OCELOT=26, MAXTYPE=27;
     private static final String typeName[] = { "???", "Ghast", "Cow", "Spider", "Sheep", 
                              "Skeleton", "Creeper", "Zombie", "Slime", 
                              "Pig", "Chicken", "Squid", "PigZombie", 
                              "Player", "UnknownCreature", "Wolf", "CaveSpider",
                              "Enderman", "Silverfish", "LavaSlime", "MushroomCow", 
                              "Villager", "SnowMan", "Blaze", "EnderDragon",
                              "VillagerGolem", "Ozelot" }; // omitting "Giant"
 
     private static final int IGNORE = 0, NAMEMAP = 1, RECIPES = 2, FUEL = 3, SMELTING = 4, ITEMS = 5;
     private static final int MTAG1 = 0, MTAG2 = 1, MINFO = 2, MERR = 3, MDEBUG = 4, MTAGR = 5, MAXMSG = 6;
     private static int block[] = new int[4096];
     private static void initBlockLookupArray() {
         block[  0] = KNOWN | SPACE | BASIC | EMPTY; // air
         block[  1] = KNOWN | SOLID | BASIC;         // stone
         block[  2] = KNOWN | SOLID | GRASS;         // grass
         block[  3] = KNOWN | SOLID | BASIC;         // dirt
         block[  4] = KNOWN | SOLID | COBBLE;        // cobble
         block[  5] = KNOWN | SOLID | CRAFT;         // plank
         block[  6] = KNOWN | SPACE | DECAL | GROW;  // sapling
         block[  7] = KNOWN | SOLID | BASIC;         // bedrock
         block[  8] = KNOWN | SPACE | LIQUID;        // water - updating
         block[  9] = KNOWN | SPACE | LIQUID;        // water
         block[ 10] = KNOWN | LIQUID;                // lava - updating
         block[ 11] = KNOWN | LIQUID;                // lava
         block[ 12] = KNOWN | SOLID | SAND;          // sand
         block[ 13] = KNOWN | SOLID | GRAVEL;        // gravel
         block[ 14] = KNOWN | SOLID | ORE | BASIC;   // gold ore
         block[ 15] = KNOWN | SOLID | ORE | BASIC;   // iron ore
         block[ 16] = KNOWN | SOLID | ORE | BASIC;   // coal ore
         block[ 17] = KNOWN | SOLID | TREE;          // trunk
         block[ 18] = KNOWN | SOLID | TREE;          // leaves
         block[ 19] = KNOWN | SOLID | CRAFT;         // sponge
         block[ 20] = KNOWN | SOLID | CRAFT;         // glass
         block[ 21] = KNOWN | SOLID | ORE | BASIC;   // blue ore
         block[ 22] = KNOWN | SOLID | CRAFT;         // blue block
         block[ 23] = KNOWN | SOLID | CRAFT | STORAGE;// dispenser
         block[ 24] = KNOWN | SOLID | SANDSTONE;     // sandstone
         block[ 25] = KNOWN | SOLID | CRAFT | TOUCH; // note
         block[ 26] = KNOWN | SOLID | CRAFT;         // bed
         block[ 27] = KNOWN | SPACE | DECAL;         // rail booster
         block[ 28] = KNOWN | SPACE | DECAL;         // rail detector
         block[ 30] = KNOWN | SPACE | DECAL;         // web
         block[ 31] = KNOWN | SPACE | DECAL;         // tall grass
         block[ 32] = KNOWN | SPACE | DECAL;         // dead shrubs
         block[ 33] = KNOWN | SOLID | CRAFT;         // piston
         block[ 34] = KNOWN | SOLID | CRAFT;         // piston plate
         block[ 35] = KNOWN | SOLID | CRAFT;         // wool
         block[ 36] = KNOWN | SOLID;                 // block moved by piston / aka. reserved slot  - it is indestructible
         block[ 37] = KNOWN | SPACE | DECAL | GROW;  // yellow flower
         block[ 38] = KNOWN | SPACE | DECAL | GROW;  // red flower
         block[ 39] = KNOWN | SPACE | DECAL | GROW;  // brown mushroom
         block[ 40] = KNOWN | SPACE | DECAL | GROW;  // red mushroom
         block[ 41] = KNOWN | SOLID | CRAFT;         // gold block
         block[ 42] = KNOWN | SOLID | CRAFT;         // iron block
         block[ 43] = KNOWN | SOLID | CRAFT;         // double slab
         block[ 44] = KNOWN | SOLID | CRAFT;         // slab
         block[ 45] = KNOWN | SOLID | CRAFT;         // brick
         block[ 46] = KNOWN | SOLID | CRAFT;         // tnt
         block[ 47] = KNOWN | SOLID | CRAFT;         // bookshelf
         block[ 48] = KNOWN | SOLID | BASIC;         // mossy
         block[ 49] = KNOWN | SOLID | OBSIDIAN;      // obsidian
         block[ 50] = KNOWN | SPACE | DECAL;         // torch
         block[ 51] = KNOWN | SPACE | DECAL;         // fire
         block[ 52] = KNOWN | SOLID | BASIC;         // spawner
         block[ 53] = KNOWN | SOLID | CRAFT;         // wooden stairs
         block[ 54] = KNOWN | SOLID | CRAFT | STORAGE;// chest
         block[ 55] = KNOWN | SPACE | DECAL;         // wire
         block[ 56] = KNOWN | SOLID | ORE | BASIC;   // diamond ore
         block[ 57] = KNOWN | SOLID | CRAFT;         // diamond block
         block[ 58] = KNOWN | SOLID | CRAFT;         // bench
         block[ 59] = KNOWN | SPACE | DECAL;         // wheat
         block[ 60] = KNOWN | SOLID | CRAFT;         // farmland
         block[ 61] = KNOWN | SOLID | CRAFT | STORAGE;// furnace
         block[ 62] = KNOWN | SOLID | CRAFT | STORAGE;// furnace - burning
         block[ 63] = KNOWN | SPACE | DECAL | TOUCH; // sign on post
         block[ 64] = KNOWN | SPACE | DECAL | TOUCH; // wooden door
         block[ 65] = KNOWN | SPACE | DECAL;         // ladder
         block[ 66] = KNOWN | SPACE | DECAL;         // rails
         block[ 67] = KNOWN | SOLID | CRAFT;         // cobble stairs
         block[ 68] = KNOWN | SPACE | DECAL | TOUCH; // sign on wall
         block[ 69] = KNOWN | SPACE | DECAL | TOUCH; // lever
         block[ 70] = KNOWN | SPACE | DECAL;         // stone plate
         block[ 71] = KNOWN | SPACE | DECAL | TOUCH; // iron door
         block[ 72] = KNOWN | SPACE | DECAL;         // wooden plate
         block[ 73] = KNOWN | SOLID | ORE | BASIC;   // redstone ore
         block[ 74] = KNOWN | SOLID | ORE | BASIC;   // redstone ore - glowing
         block[ 75] = KNOWN | SPACE | DECAL;         // red torch - off
         block[ 76] = KNOWN | SPACE | DECAL;         // red torch - on
         block[ 77] = KNOWN | SPACE | DECAL | TOUCH; // button
         block[ 78] = KNOWN | SPACE;                 // snowcap
         block[ 79] = KNOWN | SOLID | BASIC;         // ice
         block[ 80] = KNOWN | SOLID | CRAFT;         // snow block
         block[ 81] = KNOWN | SOLID | BASIC;         // cactus
         block[ 82] = KNOWN | SOLID | BASIC;         // clay
         block[ 83] = KNOWN | SPACE | DECAL | GROW;  // reed
         block[ 84] = KNOWN | SOLID | CRAFT;         // jukebox
         block[ 85] = KNOWN | SOLID | CRAFT;         // fence
         block[ 86] = KNOWN | SOLID | BASIC | GROW;  // pumpkin
         block[ 87] = KNOWN | SOLID | BASIC;         // netherrack
         block[ 88] = KNOWN | SOLID | BASIC;         // soul sand
         block[ 89] = KNOWN | SOLID | ORE | BASIC;   // glowstone
         block[ 90] = KNOWN | SPACE;                 // portal
         block[ 91] = KNOWN | SOLID | CRAFT;         // pumpkin - torch
         block[ 92] = KNOWN | SOLID | CRAFT | TOUCH; // cacke
         block[ 93] = KNOWN | SOLID | CRAFT | TOUCH; // redstone repeater - off
         block[ 94] = KNOWN | SOLID | CRAFT | TOUCH; // redstone repeater - on
         block[ 95] = KNOWN | SOLID | CRAFT;         // locked chest
         block[ 96] = KNOWN | SOLID | CRAFT;         // trapdoor
         block[ 97] = KNOWN | SOLID | BASIC;         // stone with silverfish in it
         block[ 98] = KNOWN | SOLID | BASIC;         // stone brick
         block[ 99] = KNOWN | SOLID | BASIC;         // brown scroom block
         block[100] = KNOWN | SOLID | BASIC;         // red shroom block
         block[101] = KNOWN | SOLID | BASIC;         // iron bars
         block[102] = KNOWN | SOLID | CRAFT;         // glass plane
         block[103] = KNOWN | SOLID | BASIC;         // melon
         block[104] = KNOWN | SPACE | DECAL;         // pumpkin seeds
         block[105] = KNOWN | SPACE | DECAL;         // melon seeds
         block[106] = KNOWN | SPACE | DECAL;         // vines
         block[107] = KNOWN | SOLID | CRAFT;         // fence gate
         block[108] = KNOWN | SOLID | CRAFT;         // brick stairs
         block[109] = KNOWN | SOLID | CRAFT;         // stone brick stairs
         block[110] = KNOWN | SOLID | BASIC;         // mycelium
         block[111] = KNOWN | SPACE | BASIC;         // lily pad
         block[112] = KNOWN | SOLID | BASIC;         // nether brick (TODO: disable spawn option)
         block[113] = KNOWN | SOLID | CRAFT;         // nether brick fence
         block[114] = KNOWN | SOLID | CRAFT;         // nether brick stairs
         block[115] = KNOWN | SPACE | BASIC;         // nether wart
         block[116] = KNOWN | SOLID | CRAFT;         // enchantment table
         block[117] = KNOWN | SOLID | CRAFT;         // brewing stand
         block[118] = KNOWN | SOLID | CRAFT;         // cauldron
         block[119] = KNOWN | SPACE;                 // air portal
         block[120] = KNOWN | SOLID | CRAFT;         // air portal frame
         block[121] = KNOWN | SOLID | BASIC;         // end stone
         block[122] = KNOWN | SOLID | BASIC;         // dragon egg
         block[123] = KNOWN | SOLID | CRAFT;         // redstone lamp (off)
         block[124] = KNOWN | SOLID | CRAFT;         // redstone lamp (on)
         for (int i=0;i<256;i++) if (getBlockIsSpawn(i)) block[i] |= SPAWN;
     }
     
     // classes and features list
     private static final int
         CLASS_BEGIN                    = 1,
         CLASS_ENTITYPLAYER             = 1,
         CLASS_ENTITYPLAYERSP           = 2,
         CLASS_ENTITYPLAYERMP           = 3,
         CLASS_ENUMGAMETYPE             = 4,
         CLASS_EXPLOSION                = 5,
         CLASS_GUICONTAINER             = 6,
         CLASS_MINECRAFTAPPLETIMPL      = 7,
         CLASS_MOVEMENTINPUTFROMOPTIONS = 8,
         CLASS_NETCLIENTHANDLER         = 9,
         CLASS_NETSERVERHANDLER         = 10,
         CLASS_PLAYERCONTROLLERMP       = 11,
         CLASS_RENDERLIVING             = 12,
         CLASS_TILEENTITYFURNACE        = 13,
         CLASS_WORLDPROVIDER            = 14,
         CLASS_END                      = 15,
         HOOK_BEGIN                     = CLASS_END,
         HOOK_CLIENTTICK                = HOOK_BEGIN+0,
         HOOK_SERVERTICK                = HOOK_BEGIN+1,
         HOOK_END                       = HOOK_BEGIN+2,
         MOD_BEGIN                      = HOOK_END,
         MOD_BOOM                       = MOD_BEGIN+0,
         MOD_BUILD                      = MOD_BEGIN+1,
         MOD_CART                       = MOD_BEGIN+2,
         MOD_CHEAT                      = MOD_BEGIN+3,
         MOD_CHEST                      = MOD_BEGIN+4,
         MOD_CLOUD                      = MOD_BEGIN+5,
         MOD_COMPASS                    = MOD_BEGIN+6,
         MOD_CRAFT                      = MOD_BEGIN+7,
         MOD_DEATH                      = MOD_BEGIN+8,
         MOD_DIG                        = MOD_BEGIN+9,
         MOD_FLY                        = MOD_BEGIN+10,
         MOD_FURNACE                    = MOD_BEGIN+11,
         MOD_GROWTH                     = MOD_BEGIN+12,
         MOD_ICON                       = MOD_BEGIN+13,
         MOD_INFO                       = MOD_BEGIN+14,
         MOD_ITEM                       = MOD_BEGIN+15,
         MOD_ORE                        = MOD_BEGIN+16,
         MOD_PATH                       = MOD_BEGIN+17,
         MOD_RECIPE                     = MOD_BEGIN+18,
         MOD_RESIZE                     = MOD_BEGIN+19,
         MOD_SAFE                       = MOD_BEGIN+20,
         MOD_SPAWN                      = MOD_BEGIN+21,
         MOD_SUN                        = MOD_BEGIN+22,
         MOD_TELEPORT                   = MOD_BEGIN+23,
         MOD_WEATHER                    = MOD_BEGIN+24,
         MOD_WIELD                      = MOD_BEGIN+25,
         MOD_END                        = MOD_BEGIN+26,
         FEATURE_BEGIN                  = MOD_END,
         FEATURE_BROKEN                 = FEATURE_BEGIN+0,
         FEATURE_FLY                    = FEATURE_BEGIN+1,
         FEATURE_NOCLIP                 = FEATURE_BEGIN+2,
         FEATURE_POSSESSION             = FEATURE_BEGIN+3,
         FEATURE_PROJECTION             = FEATURE_BEGIN+4,
         FEATURE_END                    = FEATURE_BEGIN+5;
 
     private static final int
         STATUS_AVAILABLE = 0,
         STATUS_MISSING   = 1,
         STATUS_BROKEN    = 2,
         STATUS_BADMODE   = 4,
         STATUS_FORBIDDEN = 8;
 
     private static int status[] = new int[FEATURE_END];
     
     private static int classStatus(Class c) {
         return checkClass(c) ? STATUS_AVAILABLE : STATUS_MISSING;
     }
     private static int classStatus(Class c, String name) {
         return checkClass(name, c) ? STATUS_AVAILABLE : STATUS_MISSING;
     }
     
     private static void initStatusLookupArray() {
         status[0] = STATUS_AVAILABLE;
         
         status[CLASS_ENTITYPLAYER]             = classStatus(EntityPlayer.class);
         status[CLASS_ENTITYPLAYERMP]           = classStatus(EntityPlayerMP.class, "zmodmarker2");
         status[CLASS_ENTITYPLAYERSP]           = classStatus(EntityPlayerSP.class, "zmodmarker2");
         status[CLASS_ENUMGAMETYPE]             = classStatus(EnumGameType.class);
         status[CLASS_EXPLOSION]                = classStatus(Explosion.class);
         status[CLASS_GUICONTAINER]             = classStatus(GuiContainer.class);
         status[CLASS_MINECRAFTAPPLETIMPL]      = classStatus(MinecraftAppletImpl.class);
         status[CLASS_MOVEMENTINPUTFROMOPTIONS] = classStatus(MovementInputFromOptions.class);
         status[CLASS_NETCLIENTHANDLER]         = classStatus(NetClientHandler.class);
         status[CLASS_NETSERVERHANDLER]         = classStatus(NetServerHandler.class);
         status[CLASS_PLAYERCONTROLLERMP]       = classStatus(PlayerControllerMP.class);
         status[CLASS_RENDERLIVING]             = classStatus(RenderLiving.class);
         status[CLASS_TILEENTITYFURNACE]        = classStatus(TileEntityFurnace.class);
         status[CLASS_WORLDPROVIDER]            = classStatus(WorldProvider.class);
         
         status[HOOK_CLIENTTICK] = status[CLASS_ENTITYPLAYER];
         status[HOOK_SERVERTICK] = status[CLASS_ENTITYPLAYER]
                                 & status[CLASS_NETSERVERHANDLER];
         
         status[MOD_BOOM]     = STATUS_BROKEN
                              | status[CLASS_EXPLOSION];
         status[MOD_BUILD]    = STATUS_AVAILABLE
                              | status[HOOK_SERVERTICK];
         status[MOD_CART]     = STATUS_BROKEN;
         status[MOD_CHEAT]    = status[CLASS_ENTITYPLAYER];
         status[MOD_CHEST]    = STATUS_BROKEN;
         status[MOD_CLOUD]    = STATUS_AVAILABLE;
         status[MOD_COMPASS]  = STATUS_AVAILABLE;
         status[MOD_CRAFT]    = STATUS_BROKEN
                              | status[CLASS_GUICONTAINER];
         status[MOD_DEATH]    = status[CLASS_ENTITYPLAYER]
                              | status[CLASS_ENTITYPLAYERMP];
         status[MOD_DIG]      = status[CLASS_PLAYERCONTROLLERMP];
         status[MOD_FLY]      = status[CLASS_ENTITYPLAYER];
         status[MOD_FURNACE]  = STATUS_BROKEN
                              | status[CLASS_TILEENTITYFURNACE];
         status[MOD_GROWTH]   = STATUS_BROKEN;
         status[MOD_ICON]     = STATUS_AVAILABLE;
         status[MOD_INFO]     = STATUS_AVAILABLE;
         status[MOD_ITEM]     = STATUS_AVAILABLE;
         status[MOD_ORE]      = STATUS_BROKEN
                              | status[HOOK_SERVERTICK];
         status[MOD_PATH]     = STATUS_AVAILABLE;
         status[MOD_RECIPE]   = STATUS_AVAILABLE;
         status[MOD_RESIZE]   = STATUS_BROKEN
                              | status[CLASS_RENDERLIVING];
         status[MOD_SAFE]     = STATUS_AVAILABLE;
         status[MOD_SPAWN]    = STATUS_BROKEN;
         status[MOD_SUN]      = status[CLASS_WORLDPROVIDER];
         status[MOD_TELEPORT] = STATUS_BROKEN;
         status[MOD_WEATHER]  = STATUS_AVAILABLE;
         status[MOD_WIELD]    = STATUS_AVAILABLE;
         
         status[FEATURE_BROKEN] = STATUS_BROKEN;
         status[FEATURE_FLY]    = status[CLASS_ENTITYPLAYER];
         status[FEATURE_NOCLIP] = status[CLASS_ENTITYPLAYER] 
                                | status[CLASS_NETSERVERHANDLER];
         status[FEATURE_POSSESSION] = STATUS_AVAILABLE;
         status[FEATURE_PROJECTION] = status[CLASS_ENTITYPLAYERSP];
     }
 
     //#Global#Handles#########################################################
     //========================================================================
     private static Minecraft minecraft;
     private static Random rnd = new Random();
     private static String path; // mod data folder path
     private static PrintStream out; // log output stream
     private static String logPath; // log file location
     private static String showError; // encountered error to be shown on screen
     private static ZRG render; // our RenderGlobal replacement
     private static Properties conf; // configuration
     private static boolean exceptionReported; // used to show only one reflection error
     private static boolean keys[] = new boolean[Keyboard.KEYBOARD_SIZE]; // used to detect key presses
     private static boolean keysM[] = new boolean[3];
     private static HashMap names;
     private static boolean chatWelcomed; // servers welcome message has passed
     private static boolean initialized; // true if config has been parsed
     private static int logErrors = 8;
     private static Text texts[] = new Text[MAXMSG];
     private static int clearDisplayedError, showOptions;
 
     public static void initialize(Minecraft mc) {
         log("*** initialization ***"); // also initializes log and path.
         minecraft = mc; // my precious ...
         // load config
         conf = new Properties();
         try {
             Properties tmp;
             (tmp = new Properties()).load(new FileInputStream(path + "config.txt"));
             conf = tmp;
             log("info: processing configuration");
         } catch(Exception error) {
             err("error: failed to load configuration from config.txt", error);
         }
         // catch errors
         try {
         // base config
         if (getBool("disableAllMods", false)) { log("info: all mods disabled"); return; }
         optionsModModpack();
         parse(null, "names.txt", NAMEMAP); names = pNames; // load names
         initBlockLookupArray();
         initStatusLookupArray();
         // load mods
         int mods = 0;
         optionsModBoom();     if (modBoomActive)     ++mods;
         optionsModBuild();    if (modBuildActive)    ++mods;
         optionsModCart();     if (modCartActive)     ++mods;
         optionsModCheat();    if (modCheatActive)    ++mods;
         optionsModChest();    if (modChestActive)    ++mods;
         optionsModCloud();    if (modCloudActive)    ++mods;
         optionsModCompass();  if (modCompassActive)  ++mods;
         optionsModCraft();    if (modCraftActive)    ++mods;
         optionsModDeath();    if (modDeathActive)    ++mods;
         optionsModDig();      if (modDigActive)      ++mods;
         optionsModFly();      if (modFlyActive)      ++mods;
         optionsModFurnace();  if (modFurnaceActive)  ++mods;
         optionsModGrowth();   if (modGrowthActive)   ++mods;
         optionsModIcon();     if (modIconActive)     ++mods; ZP250.init(); // hande them even without the mod.
         optionsModInfo();     if (modInfoActive)     ++mods;
         optionsModItem();     if (modItemActive)     ++mods;
         optionsModOre();      if (modOreActive)      ++mods;
         optionsModPath();     if (modPathActive)     ++mods;
         optionsModRecipe();   if (modRecipeActive)   ++mods;
         optionsModResize();   if (modResizeActive)   ++mods;
         optionsModSafe();     if (modSafeActive)     ++mods;
         optionsModSpawn();    if (modSpawnActive)    ++mods;
         optionsModSun();      if (modSunActive)      ++mods;
         optionsModTeleport(); if (modTeleportActive) ++mods;
         optionsModWield();    if (modWieldActive)    ++mods;
         optionsModWeather();  if (modWeatherActive)  ++mods;
         // shared init
         if (optItemChangeSpawner || modRecipeEnabled || modBuildEnabled) initModItemShared();
         // done
         if (mods==0) err("warning: no mods are enabled! Press F7 to configure them!");
         log("info: configuration loaded");
         } catch(Exception error) { err("error: initialization failed", error); }
         initialized = true;
         log("*** done ***");
     }
     
     private static void optionsModModpack() {
         showOptions = getSetBind(showOptions, "showOptions", Keyboard.KEY_F7, "Show options screen");
         clearDisplayedError = getSetBind(clearDisplayedError, "clearDisplayedError", Keyboard.KEY_F9, "Clear displayed error");
     }
     
     private static boolean optionsMod(String name, boolean enabled) {
         return optionsMod(name, enabled, 0);
     }
     
     private static boolean optionsMod(String name, boolean enabled, int mod) {
         int column = 14;
         int x = (optionModNr / column) * 10 + 2, y = (optionModNr % column) + 1;
         optionsModEnabled = true;
         if (drawBtn(x, y, 9, name, null ,optionsSelMod == optionModNr, enabled, false, false, status[mod])) { optionsSelMod = optionModNr; optionSel = -1; optionNr = -100; optionOfs = 0; }
         optionsModEnabled = enabled;
         return optionsSelMod == optionModNr++;
     }
     
     private static Options opt;
     private static int optionsSelMod = -1, optionSel = -1, optionNr, optionModNr, optionOfs = 0, optionsPage = 20;
     private static boolean optionsModEnabled;
     private static void optionsMods(Options options) {
         if (modInfoActive && optInfoHideAchievement) killAchievement(); // kill achievement in options screen
 
         opt = options;
         optionNr = optionModNr = 0;
         opt.drawRect(0, 0, opt.width, opt.height, 0xc0000000);
         showText("Zombe's modpack v"+version+":", 2, 2, 0xcccccc);
         
         if (optionOfs > 0) {
             if (drawBtn(22, 1, 20, "^^^", null, false, false, true, true, 0)) { optionOfs -= optionsPage; return; }
             optionNr++;
         }
 
         if (optionsMod("\u00a7emodpack", true)) optionsModModpack();
         if (optionsMod("Boom"    , modBoomActive    , MOD_BOOM    )) optionsModBoom();
         if (optionsMod("Build"   , modBuildActive   , MOD_BUILD   )) optionsModBuild();
         if (optionsMod("Cart"    , modCartActive    , MOD_CART    )) optionsModCart();
         if (optionsMod("Cheat"   , modCheatActive   , MOD_CHEAT   )) optionsModCheat();
         if (optionsMod("Chest"   , modChestActive   , MOD_CHEST   )) optionsModChest();
         if (optionsMod("Cloud"   , modCloudActive   , MOD_CLOUD   )) optionsModCloud();
         if (optionsMod("Compass" , modCompassActive , MOD_COMPASS )) optionsModCompass();
         if (optionsMod("Craft"   , modCraftActive   , MOD_CRAFT   )) optionsModCraft();
         if (optionsMod("Death"   , modDeathActive   , MOD_DEATH   )) optionsModDeath();
         if (optionsMod("Dig"     , modDigActive     , MOD_DIG     )) optionsModDig();
         if (optionsMod("Fly"     , modFlyActive     , MOD_FLY     )) optionsModFly();
         if (optionsMod("Furnace" , modFurnaceActive , MOD_FURNACE )) optionsModFurnace();
         if (optionsMod("Growth"  , modGrowthActive  , MOD_GROWTH  )) optionsModGrowth();
         if (optionsMod("Icon"    , modIconActive    , MOD_ICON    )) optionsModIcon();
         if (optionsMod("Info"    , modInfoActive    , MOD_INFO    )) optionsModInfo();
         if (optionsMod("Item"    , modItemActive    , MOD_ITEM    )) optionsModItem();
         if (optionsMod("Ore"     , modOreActive     , MOD_ORE     )) optionsModOre();
         if (optionsMod("Path"    , modPathActive    , MOD_PATH    )) optionsModPath();
         if (optionsMod("Recipe"  , modRecipeActive  , MOD_RECIPE  )) optionsModRecipe();
         if (optionsMod("Resize"  , modResizeActive  , MOD_RESIZE  )) optionsModResize();
         if (optionsMod("Safe"    , modSafeActive    , MOD_SAFE    )) optionsModSafe();
         if (optionsMod("Spawn"   , modSpawnActive   , MOD_SPAWN   )) optionsModSpawn();
         if (optionsMod("Sun"     , modSunActive     , MOD_SUN     )) optionsModSun();
         if (optionsMod("Teleport", modTeleportActive, MOD_TELEPORT)) optionsModTeleport();
         if (optionsMod("Weather" , modWeatherActive , MOD_WEATHER )) optionsModWeather();
         if (optionsMod("Wield"   , modWieldActive   , MOD_WIELD   )) optionsModWield();
 
         if (optionOfs + optionsPage < optionNr) {
             optionNr = optionOfs + optionsPage + 1;
             if (drawBtn(22, optionNr - optionOfs, 20, "vvv", null, false, false, true, true, 0)) optionOfs += optionsPage;
         }
 
     }
 
     public static Minecraft getMinecraft() { return minecraft; }
 
     // =======================================================================
     public static void pingRespawnHandle(boolean first) { try {
         if (!isMultiplayer && modDeathActive && !first) respawnDeathMod();
         if (modInfoActive && !first) respawnInfoMod();
         } catch(Exception error) { err("error: respawn failed", error); }
     }
     
     // =======================================================================
     public static void initOverrides() {
         try {
             log("info: init render");
             overloadRenderGlobal();
             overloadEntityRender();
         } catch(Exception error) { err("error: overrides failed", error); }
     }
     
     //
     private static boolean isServerPlayer(EntityPlayer ent) {
         return (ent != null && player != null && ent instanceof EntityPlayerMP 
              && getPlayerName(ent).equals(getPlayerName(player)));
     }
     
     //=Hook=ClientTick========================================================
     private static void onClientTick(EntityPlayerSP ent) {
         
     }
     
     //=Hook=ServerTick========================================================
     private static void onServerTick(EntityPlayerMP ent) {
         buildOnServerTick(ent);
     }
     
     //=Hook=PlayerDeath
     private static void onServerPlayerDeath(EntityPlayerMP ent) {
         deathOnServerPlayerDeath(ent);
     }
     
     //=Hook=PlayerUpdate======================================================
     private static void onClientPlayerUpdate(EntityPlayerSP ent) {
         cheatOnClientPlayerUpdate(ent);
         flyOnClientPlayerUpdate(ent);
     }
 
     private static void onServerPlayerUpdate(EntityPlayerMP ent) {
         cheatOnServerPlayerUpdate(ent);
         deathOnServerPlayerUpdate(ent);
         flyOnServerPlayerUpdate(ent);
         if (status[CLASS_NETSERVERHANDLER] != 0) {
             onServerTick(ent);
         }
     }
     
     //=Handle=EntityPlayerMP.onDeath==========================================
     public static void onPlayerDeath(EntityPlayer ent) {
         if (isServerPlayer(ent)) {
             onServerPlayerDeath((EntityPlayerMP) ent);
         }
     }
     
     //=Handle=EntityPlayer.onUpdate===========================================
     public static void onPlayerUpdate(EntityPlayer ent) {
         if (player == null) return;
         if (ent == player) {
             onClientPlayerUpdate((EntityPlayerSP) ent);
         }
         if (isServerPlayer(ent)) {
             onServerPlayerUpdate((EntityPlayerMP) ent);
         }
     }
     
     //=Handle=NetClientHandler.networkTick====================================
     public static void onNetworkTick(EntityPlayerSP ent) {
         if (ent == player) {
             onClientTick(ent);
         }
     }
     
     //=Handle=NetServerHandler.networkTick====================================
     public static void onNetworkTick(EntityPlayerMP ent) {
         if (isServerPlayer(ent)) {
             onServerTick(ent);
         }
     }
     
     // =======================================================================
     private static Object prevPC, PC; // used to detect world change
     private static boolean deferredInit;
     private static boolean isMenu, isMultiplayer, isHell, isMapChange, isWorldChange;
     private static double posX, posY, posZ, motionX, motionY, motionZ;
     private static String chatLast;
     private static int mouseX, mouseY;
     private static Entity inWhat;
     private static EntityClientPlayerMP player;
     private static EntityClientPlayerMP prevPlayer;
     private static MovementInput  playerMovementInput;
     private static WorldInfo world;
     private static World map;
     private static InventoryPlayer inv;
 
     public static void pingUpdateHandle() {
         // deferred init
         if (!deferredInit) try {
             deferredInit = true;
             deferredModItem();
             deferredModRecipe();
         } catch(Exception error) { err("error: deferredInit failed", error); }
         // handle all mods
         try {
             // update state
             if ((player = getPlayer()) == null) { deathHaveInv = deathHaveExp = false; return; }
             if (player != prevPlayer) {
                 playerMovementInput = player.movementInput;
                 prevPlayer = player;
             }
             isMapChange = map != getMap();
             if ((map = getMap()) == null) return;
             if (getRenderer() == null) return;
             List list = getEntities();
             posX = getEntityPosX(player); posY = getEntityPosY(player); posZ = getEntityPosZ(player);
             motionX = getEntityMotionX(player); motionY = getEntityMotionY(player); motionZ = getEntityMotionZ(player);
             isMenu = getIsMenu(); isMultiplayer = getIsMultiplayer(); isHell = getIsHell();
             inWhat = getOnEntity(player);
             inv = getInventory(player); invItemsArr = getInvItems(inv); invArmorsArr = getInvArmors(inv);
             world = getWorld();
             PC = getPlayerController();
             if (isMenu) updateMousePos();
             if (isWorldChange = PC != prevPC) {
                 modFlyAllowed = true;
                 modCheatAllowed = true;
                 modNoClipAllowed = !isMultiplayer;
                 chatWelcomed = false;
                 iconMPSupport = false;
                 infoDeathY = 1024;
                 if (isMultiplayer && optIconMP) sendChat("/zombe-icon");
             }
             prevPC = PC;
             List chat = getChat();
             if (!chatWelcomed) for (int line=0;line<chat.size(); ++line) {
                 String msg = getChatLine(chat, line);
                 if (msg == null) continue;
                 if (msg == chatLast) break;
                 if (msg.contains("joined the game")) { chatWelcomed = true; continue; }
                 if (msg.contains("\u00a7f \u00a7f \u00a71 \u00a70 \u00a72 \u00a74")) modFlyAllowed = false;
                 if (msg.contains("\u00a7f \u00a7f \u00a72 \u00a70 \u00a74 \u00a78")) modCheatAllowed = false;
                 if (msg.contains("\u00a7f \u00a7f \u00a74 \u00a70 \u00a79 \u00a76")) modNoClipAllowed = modFlyAllowed;
                 if (msg.matches(".*(\\W|^)no-z-fly(\\W|$).*")) modFlyAllowed = false;
                 if (msg.matches(".*(\\W|^)no-z-cheat(\\W|$).*")) modCheatAllowed = false;
                 if (msg.matches(".*(\\W|^)z-cheat(\\W|$).*")) modNoClipAllowed = modFlyAllowed;
             }
             if (chat.size()>0) chatLast = getChatLine(chat, 0);
             delMsg(1);
             // update logging
             if (showError != null && !isMenu && keyPress(clearDisplayedError)) { showError = null; delMsg(3); }
             // show options
             if (keyPress(showOptions)) minecraft.displayGuiScreen(minecraft.currentScreen instanceof Options ? null : new Options());
             // update mods
             try { updateModRecipeShared(); } catch(Exception error) { err("error: \"recipe\" update failed", error); }
             try { updateModItem(); } catch(Exception error) { err("error: \"item\" update failed", error); }
             try { updateModDeath(); } catch(Exception error) { err("error: \"death\" update failed", error); }
             try { updateModInfo(list); } catch(Exception error) { err("error: \"info\" update failed", error); }
             try { updateModChest(list); } catch(Exception error) { err("error: \"chest\" update failed", error); }
             try { updateModGrowth(list); } catch(Exception error) { err("error: \"growth\" update failed", error); }
             try { updateModWeather(); } catch(Exception error) { err("error: \"weather\" update failed", error); }
             try { updateModCloud(); } catch(Exception error) { err("error: \"cloud\" update failed", error); }
             try { updateModCart(list); } catch(Exception error) { err("error: \"cart\" update failed", error); }
             try { updateModWield(); } catch(Exception error) { err("error: \"wield\" update failed", error); }
             try { updateModBuild(); } catch(Exception error) { err("error: \"build\" update failed", error); }
             try { updateModCompass(); } catch(Exception error) { err("error: \"compass\" update failed", error); }
             try { updateModSun(); } catch(Exception error) { err("error: \"sun\" update failed", error); }
             try { updateModFly(); } catch(Exception error) { err("error: \"fly\" update failed", error); }
             try { updateModPath(); } catch(Exception error) { err("error: \"\" update failed", error); }
             try { updateModRecipe(); } catch(Exception error) { err("error: \"recipe\" update failed", error); }
             try { updateModSafe(); } catch(Exception error) { err("error: \"safe\" update failed", error); }
             try { updateModSpawn(list); } catch(Exception error) { err("error: \"spawn\" update failed", error); }
             try { updateModOre(); } catch(Exception error) { err("error: \"ore\" update failed", error); }
             try { updateModTeleport(list); } catch(Exception error) { err("error: \"teleport\" update failed", error); }
             try { updateModCheat(list); } catch(Exception error) { err("error: \"cheat\" update failed", error); }
             try { updateResizeMod(list); } catch(Exception error) { err("error: \"resize\" update failed", error); }
         } catch(Exception error) { err("error: update-handle failed", error); }
     }
 
     // ===========================================================================================================================
     private static void startCheatRender() {
         try {
             if (modCheatAllowed && modCheatActive && cheating && cheatSee)
                 obliqueNearPlaneClip(0.0f, 0.0f, -1.0f, -optCheatSeeDist);
         } catch(Exception error) { err("error: see-through setup failed", error); }
     }
     
     public static int onSortAndRender(EntityLiving view, int pass, double delta) {
         ZMod.startCheatRender();
         int ret = render.forwardSortAndRender(view, pass, delta);
         return ret;
     }
     
     // ===========================================================================================================================
     private static long prevTick, curTick;
     private static float seconds; // current frame delta time
     private static boolean origClouds;
     private static int origFog;
     private static float frameDelta;
     public static void drawModsRender(float delta) {
         //minecraft.gameSettings.clouds = origClouds;
         //minecraft.gameSettings.renderDistance = origFog;
         if (player == null || map == null || getRenderer() == null) return;
         try {
             List list = getEntities();
             // update player position
             posX = getEntityPosX(player); posY = getEntityPosY(player); posZ = getEntityPosZ(player);
             // update time
             curTick = System.nanoTime();
             seconds = ((float)(curTick - prevTick)) * 0.000000001f;
             if (seconds > 1f) seconds = 0f;
             frameDelta = delta;
             prevTick = curTick;
             // draw in 3d
             float px = (float)posX, py = (float)posY, pz = (float)posZ;
             float mx = (float)getEntityPrevPosX(player), my = (float)getEntityPrevPosY(player), mz = (float)getEntityPrevPosZ(player);
             if (cheatView != null) {
                 px = (float)getEntityPosX(cheatView);
                 py = (float)getEntityPosY(cheatView);
                 pz = (float)getEntityPosZ(cheatView);
                 mx = (float)getEntityPrevPosX(cheatView);
                 my = (float)getEntityPrevPosY(cheatView);
                 mz = (float)getEntityPrevPosZ(cheatView);
             }
             float x = mx + (px - mx) * delta, y = my + (py - my) * delta, z = mz + (pz - mz) * delta;
             
             boolean gltex2d = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
             boolean gldepth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
             boolean glblend = GL11.glGetBoolean(GL11.GL_BLEND);
             boolean glfog   = GL11.glGetBoolean(GL11.GL_FOG);
             // handle mods
             try { drawModIcon(x,y,z); } catch(Exception error) { err("error: \"icon\" draw failed", error); }
             try { drawModCheat(x,y,z,list); } catch(Exception error) { err("error: \"cheat\" draw failed", error); }
             try { drawModPath(x,y,z); } catch(Exception error) { err("error: \"path\" draw failed", error); }
             try { drawModSafe(x,y,z); } catch(Exception error) { err("error: \"safe\" draw failed", error); }
             try { drawModBuild(x,y,z); } catch(Exception error) { err("error: \"build\" draw failed", error); }
             // cleaning
             if (glfog)   GL11.glEnable( GL11.GL_FOG);
             else         GL11.glDisable(GL11.GL_FOG);
             if (glblend) GL11.glEnable( GL11.GL_BLEND);
             else         GL11.glDisable(GL11.GL_BLEND);
             if (gldepth) GL11.glEnable( GL11.GL_DEPTH_TEST);
             else         GL11.glDisable(GL11.GL_DEPTH_TEST);
             if (gltex2d) GL11.glEnable( GL11.GL_TEXTURE_2D);
             else         GL11.glDisable(GL11.GL_TEXTURE_2D);
         } catch(Exception error) { err("error: draw-handle failed", error); }
     }
 
     public static void spoofCloudFogConfig() {
         origClouds = minecraft.gameSettings.clouds; minecraft.gameSettings.clouds = true;
         origFog = minecraft.gameSettings.renderDistance; minecraft.gameSettings.renderDistance = 0;
     }
 
     // ===========================================================================================================================
     private static boolean ML_loaded;
     private static Method ML_OnTick;
     public static void pingDrawGUIHandle(float delta) {
         // text overlay
         if (!isHideGUI() && !getIsOptions()) {
             // set state
             GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); GL11.glLoadIdentity(); setOrtho();
             GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); GL11.glLoadIdentity();
             GL11.glTranslatef(0.0F, 0.0F, -2000F);
             GL11.glDisable(GL11.GL_LIGHTING);
             // draw text
             int yofs = 0;
             if (isShowDebug()) {
                 setMsg(MDEBUG, pingDebugHandle(), 0xffffff, 2, 90);
                 Text tmp = texts[MDEBUG];
                 String lines[] = tmp.msg.split("\n");
                 int x = tmp.x, y = tmp.y + yofs, c = tmp.color;
                 for (int line=0;line<lines.length;line++,y+=10) showText(lines[line], x, y, c);
             } else if (!isMenu) {
                 setMsg(MTAG1, pingTextHandle());
                 for (int i=0;i<texts.length;i++) {
                     Text tmp = texts[i];
                     if (tmp == null || (isMenu && i != MTAG1) || i == MDEBUG) continue;
                     String lines[] = tmp.msg.split("\n");
                     int x = tmp.x, y = tmp.y + yofs, c = tmp.color, width = getScrWidthS() - 2;
                     if (i == MTAGR) y = 2;
                     for (int line=0;line<lines.length;line++,y+=10) {
                         if (i == MTAGR) x = width - showTextLength(lines[line]);
                         showText(lines[line], x, y, c);
                     }
                 }
             }
             // handle mods
             drawGuiModInfo();
             drawGuiModRecipe();
             // restore state
             GL11.glPopMatrix();
             GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix();
             GL11.glMatrixMode(GL11.GL_MODELVIEW);
         }
         // modLoader compatibility
         if (!ML_loaded) try {
             ML_loaded = true;
             ML_OnTick = Class.forName("ModLoader").getDeclaredMethod("onTick", new Class[]{ Float.TYPE, Minecraft.class });  // ModLoader.OnTick(tick, game);
         } catch(Exception whatever) { ML_OnTick = null; }
         if (ML_OnTick != null) getResult(ML_OnTick, null, delta, minecraft);
     }
 
     // ===========================================================================================================================
     public static String pingTextHandle() {
         if (player == null || map == null) return "";
         String infoLine = "";
         infoLine = textModSun(infoLine);
         infoLine = textModInfo(infoLine);
         infoLine = textModCompassShared(infoLine);
         infoLine = textModFly(infoLine);
         infoLine = textModSafe(infoLine);
         infoLine = textModCheat(infoLine);
         infoLine = textModBuild(infoLine);
         infoLine = textModRecipe(infoLine);
         infoLine = textModCloud(infoLine);
         infoLine = textModCart(infoLine);
         infoLine = textModWeather(infoLine);
         infoLine = textModInfoBiome(infoLine);
         return infoLine;
     }
 
     // ===========================================================================================================================
     public static String pingDebugHandle() {
         return "";
     }
 
     //#ZMod#Mods##############################################################
 
     //=ZMod=Item==============================================================
     private static boolean modItemEnabled, modItemActive;
     private static boolean optItemChangeFence, optItemChangeGlass, optItemChangeIce, optItemChangeLeaves, optItemChangeTorch, optItemChangeFarmland;
     private static boolean optItemChangeSponge, optItemChangeWater, optItemChangeSpawner, optItemChangeShelf, optItemChangeStairs, optItemDump;
     private static boolean optItemChangeLava;
     private static Block itemFenceO, itemGlassO, itemIceO, itemLeavesO, itemTorchO, itemFarmlandO, itemSpongeO, itemWaterO, itemSpawnerO, itemShelfO, itemStairsO, itemLavaO, itemGlassPO;
     private static Block itemFenceM, itemGlassM, itemIceM, itemLeavesM, itemTorchM, itemFarmlandM, itemSpongeM, itemWaterM, itemSpawnerM, itemShelfM, itemStairsM, itemLavaM, itemGlassPM;
     private static int optItemLeavesDrop, optItemOakChance, optItemOakSpecial, optItemBirchChance, optItemBirchSpecial, optItemPineChance, optItemPineSpecial;
     private static boolean itemModified;
     private static Mark itemOrig[], itemMine[];
 
     private static boolean initModItem() {
         if (!checkStatus(MOD_ITEM, "item")) return false;
         log("info: loading config for \"item\" - deferred");
         optItemDump = getBool("optItemDump", false);
         if (optItemChangeFence = getBool("optItemChangeFence", true)) { itemFenceO = getBlock(85); setBlock(85, null); itemFenceM = new ZBF(); setBlock(85, itemFenceO); }
         if (optItemChangeGlass = getBool("optItemChangeGlass", true)) {
             itemGlassO  = getBlock(20);  setBlock(20, null);  itemGlassM = new ZBG();   setBlock(20, itemGlassO);
             itemGlassPO = getBlock(102); setBlock(102, null); itemGlassPM = new ZBGP(); setBlock(102, itemGlassPO);
         }
         if (optItemChangeIce = getBool("optItemChangeIce", true)) { itemIceO = getBlock(79); setBlock(79, null); itemIceM = new ZBI(); setBlock(79, itemIceO); }
         if (optItemChangeLeaves = getBool("optItemChangeLeaves", true)) { itemLeavesO = getBlock(18); setBlock(18, null); itemLeavesM = new ZBL(); setBlock(18, itemLeavesO); }
         if (optItemChangeSponge = getBool("optItemChangeSponge", false)) { itemSpongeO = getBlock(19); setBlock(19, null); itemSpongeM = new ZBS(); setBlock(19, itemSpongeO); }
         if (optItemChangeShelf = getBool("optItemChangeShelf", true)) { itemShelfO = getBlock(47); setBlock(47, null); itemShelfM = new ZBB(); setBlock(47, itemShelfO); }
         if (optItemChangeWater = getBool("optItemChangeWater", false)) { itemWaterO = getBlock(8); setBlock(8, null); itemWaterM = new ZBW(true); setBlock(8, itemWaterO); }
         if (optItemChangeLava = getBool("optItemChangeLava", false)) { itemLavaO = getBlock(10); setBlock(10, null); itemLavaM = new ZBW(false); setBlock(10, itemLavaO); }
         getDeprecated("optItemChangeStairs");
         optItemChangeSpawner = getBool("optItemChangeSpawner", true);
         getDeprecated("optItemChangeTorch");
         if (optItemChangeFarmland = getBool("optItemChangeFarmland", true)) { itemFarmlandO = getBlock(60); setBlock(60, null); itemFarmlandM = new ZBFL(); setBlock(60, itemFarmlandO); }
         if (optItemChangeLeaves) {
             optItemLeavesDrop = getInt("optItemLeavesDrop", 20, 1, 100);
             optItemOakChance = getInt("optItemOakChance", 3, 0, 100);
             optItemOakSpecial = getItemId("optItemOakSpecial", 260);
             optItemBirchChance = getInt("optItemBirchChance", 1, 0, 100);
             optItemBirchSpecial = getItemId("optItemBirchSpecial", 351 | (3 << 16));
             optItemPineChance = getInt("optItemPineChance", 5, 0, 100);
             optItemPineSpecial = getItemId("optItemPineSpecial", 344);
         }
         return modItemActive = true;
     }
     
     private static void quitModItem() {
         if (!modItemActive) return;
         modItemActive = false;
         itemOrig = null;
         itemMine = null;
     }
     
     private static void optionsModItem() {
         boolean wasEnabled = modItemEnabled;
         modItemEnabled = getSetBool(modItemEnabled, "modItemEnabled", false,     "Enable Item mod");
         if (wasEnabled != modItemEnabled) {
             if (modItemEnabled && !modItemActive) initModItem(); 
             if (!modItemEnabled && modItemActive) quitModItem();
         }
         
     }
 
     private static void deferredModItem() {
         if (!modItemActive) return;
         if (itemOrig == null) {
             log("info: continuing to load \"item\"");
             itemOrig = new Mark[Item.itemsList.length]; itemMine = new Mark[Item.itemsList.length];
             for (int itemId=0;itemId<itemOrig.length;itemId++) if (getItem(itemId) != null) {
                 itemOrig[itemId] = Mark.makeItem(itemId);
                 itemMine[itemId] = Mark.makeItem(itemId);
             }
             parse(null, "items.txt", ITEMS);
         }
         if (optItemDump) {
             log("==== item dump ====\n");
             for (int itemId=0;itemId<Item.itemsList.length;itemId++) {
                 if (itemId==0) log("// blocks");
                 else if (itemId==256) log("\n// items");
                 if (itemId < 256) {
                     Block block = getBlock(itemId);
                     if (block==null) continue;
                     log(String.format(Locale.ENGLISH, "%-14s %4d %2d %3d %5.1f %5.1f %4.1f %2d %3d", getNameForId(itemId), getItemMax(getItem(itemId)),
                         getBlockLight(itemId), getBlockOpacity(itemId), getBlockStrength(block), getBlockResist(block), getBlockSlip(block),
                         getFireSpread(itemId), getFireBurn(itemId)));
                 } else {
                     Item item = getItem(itemId);
                     if (item==null) continue;
                     // itemName stack
                     if (getItemHasSubTypes(item) || getItemDmgCap(item)==0) log(String.format(Locale.ENGLISH, "%-14s %4d", getNameForId(itemId), getItemMax(item)));
                     // itemName stack maxDamage
                     else log(String.format(Locale.ENGLISH, "%-14s %4d %2d", getNameForId(itemId), getItemMax(item), getItemDmgCap(item)));
                 }
             }
         }
     }
     
     private static void updateModItem() {
         if (!modItemActive) return;
         if (isMultiplayer && itemModified) {
             itemModified = false;
             for (int itemId=0;itemId<itemOrig.length;itemId++) if (itemOrig[itemId]!=null) itemOrig[itemId].activate(itemId);
             if (optItemChangeFence) setBlock(85, itemFenceO);
             if (optItemChangeGlass) { setBlock(20, itemGlassO); setBlock(102, itemGlassPO); }
             if (optItemChangeIce) setBlock(79, itemIceO);
             if (optItemChangeLeaves) setBlock(18, itemLeavesO);
             if (optItemChangeTorch) setBlock(50, itemTorchO);
             if (optItemChangeFarmland) setBlock(60, itemFarmlandO);
             if (optItemChangeSponge) setBlock(19, itemSpongeO);
             if (optItemChangeWater) setBlock(8, itemWaterO);
             if (optItemChangeLava) setBlock(10, itemLavaO);
             if (optItemChangeSpawner) setBlock(52, itemSpawnerO);
             if (optItemChangeShelf) setBlock(47, itemShelfO);
         }
         if (!isMultiplayer && !itemModified) {
             itemModified = true;
             for (int itemId=0;itemId<itemMine.length;itemId++) if (itemMine[itemId]!=null) itemMine[itemId].activate(itemId);
             if (optItemChangeFence) setBlock(85, itemFenceM);
             if (optItemChangeGlass) { setBlock(20, itemGlassM); setBlock(102, itemGlassPM); }
             if (optItemChangeIce) setBlock(79, itemIceM);
             if (optItemChangeLeaves) setBlock(18, itemLeavesM);
             if (optItemChangeTorch) setBlock(50, itemTorchM);
             if (optItemChangeFarmland) setBlock(60, itemFarmlandM);
             if (optItemChangeSponge) setBlock(19, itemSpongeM);
             if (optItemChangeWater) setBlock(8, itemWaterM);
             if (optItemChangeLava) setBlock(10, itemLavaM);
             if (optItemChangeSpawner) setBlock(52, itemSpawnerM);
             if (optItemChangeShelf) setBlock(47, itemShelfM);
         }
     }
 
     private static void initModItemShared() {
         itemSpawnerO = getBlock(52);
         setBlock(52, null);
         itemSpawnerM = new ZBM();
         setBlock(52, itemSpawnerO);
     }
 
     public static String mobTypeHandle() {
         return recipesMobType != 0 ? typeName[recipesMobType] : null;
     }
 
     public static boolean spawnderDropHandle() {
         return !isMultiplayer && optItemChangeSpawner;
     }
 
     public static int leavesDropHandle() {
         return rnd.nextInt(optItemLeavesDrop) == 0 ? 1 : 0;
     }
 
     public static int leavesHandle(int meta) {
         int chance = rnd.nextInt(100);
         if (meta==0 && chance < optItemOakChance) return optItemOakSpecial;
         if (meta==1 && chance < optItemPineChance) return optItemPineSpecial;
         if (meta==2 && chance < optItemBirchChance) return optItemBirchSpecial;
         return 6 | (meta << 16);
     }
 
     public static void itemGraphicsLevelHandle(boolean flag) {
         if (optItemChangeLeaves) {
             setBlockGraphicsLevel(itemLeavesO, flag);
             setBlockGraphicsLevel(itemLeavesM, flag);
         }
     }
 
     //=ZMod=Death=============================================================
     private static boolean modDeathEnabled, modDeathActive;
     private static boolean optDeathDropInv, optDeathLoseExp;
     private static int optDeathHPPenalty;
     private static boolean deathHaveInv, deathHaveExp;
     private static ItemStack deathInv[];
     private static ItemStack deathArmor[];
     private static int deathXpLevel, deathXpTotal;
     private static float deathXpP;
 
     private static boolean initModDeath() {
         if (!checkStatus(MOD_DEATH, "death")) return false;
         log("info: loading config for \"death\"");
         return modDeathActive = true;
     }
     
     private static void quitModDeath() {
         if (!modDeathActive) return;
         modDeathActive = false;
     }
     
     private static void optionsModDeath() {
         boolean wasEnabled = modDeathEnabled;
         modDeathEnabled = getSetBool(modDeathEnabled, "modDeathEnabled", false,     "Enable Death mod");
         if (wasEnabled != modDeathEnabled) {
             if (modDeathEnabled && !modDeathActive) initModDeath(); 
             if (!modDeathEnabled && modDeathActive) quitModDeath();
         }
         optDeathDropInv   = getSetBool(optDeathDropInv,  "optDeathDropInv",   false,     "Drop inventory on death");
         optDeathLoseExp   = getSetBool(optDeathLoseExp,  "optDeathLoseExp",   false,     "Lose experience on death");
         optDeathHPPenalty = getSetInt(optDeathHPPenalty, "optDeathHPPenalty", 0, 0, 100, "Respawn HP penalty", FEATURE_BROKEN);
     }
 
     private static void updateModDeath() {
         
     }
     
     private static void deathOnServerPlayerDeath(EntityPlayer ent) {
         if (!modDeathActive || isMultiplayer) return;
         if (!optDeathDropInv) { // save inventory
             deathHaveInv = true;
             InventoryPlayer inv = ent.inventory;
             deathInv = new ItemStack[inv.mainInventory.length];
             deathArmor = new ItemStack[inv.armorInventory.length];
             for (int i = 0; i < deathInv.length; ++i) { 
                 deathInv[i]   = inv.mainInventory[i];
                 inv.mainInventory[i] = null;
             }
             for (int i = 0; i < deathArmor.length; ++i) {
                 deathArmor[i] = inv.armorInventory[i];
                 inv.armorInventory[i] = null;
             }
         }
         if (!optDeathLoseExp) {
             deathHaveExp = true;
             deathXpTotal = ent.experienceTotal;
             deathXpP     = ent.experience;
             deathXpLevel = ent.experienceLevel;
         }
     }
 
     private static void deathOnServerPlayerUpdate(EntityPlayer ent) {
         if (!modDeathActive || isMultiplayer) return;
         if (ent.isDead || getHealth(ent) <= 0) return;
         if (!optDeathDropInv && deathHaveInv) {
             deathHaveInv = false;
             InventoryPlayer inv = ent.inventory;
             for (int i = 0; i < deathInv.length;   ++i) inv.mainInventory[i]  = inv.mainInventory[i]  == null ? deathInv[i]   : inv.mainInventory[i];
             for (int i = 0; i < deathArmor.length; ++i) inv.armorInventory[i] = inv.armorInventory[i] == null ? deathArmor[i] : inv.armorInventory[i];
         }
         if (!optDeathLoseExp && deathHaveExp) {
             deathHaveExp = false;
             if (ent.experienceTotal == 0) {
                 ent.experienceTotal = deathXpTotal;
                 ent.experience      = deathXpP;
                 ent.experienceLevel = deathXpLevel;
             }
         }
     }
 
     private static void respawnDeathMod() {
         EntityPlayerSP ent = getPlayer();
         if (optDeathHPPenalty != 0) {
             int hp = getHealth(ent) - optDeathHPPenalty;
             if (hp < 1) hp = 1;
             setHealth(ent, hp);
         }
     }
 
     //=ZMod=Info==============================================================
     private static boolean modInfoEnabled, modInfoActive;
     private static int keyInfoToggle, keyInfoPlayersToggle;
     private static String optInfoPrefixNear, optInfoPrefixFar;
     private static boolean optInfoShowPos, optInfoShowTime, optInfoShowBiome, optInfoShowHealth, optInfoHideAchievement, optInfoShowItem, optInfoShowFPS;
     private static float optInfoRangeMax = 1000, optInfoRangeNear = 1;
     private static int optInfoTimeOffset;
     private static boolean infoShow, infoPlayerShow;
     private static int infoDeathX, infoDeathY, infoDeathZ, infoFrames = 0;
     private static long infoTime = 0;
     private static String infoFps = "";
 
     private static boolean initModInfo() {
         if (!checkStatus(MOD_INFO, "info")) return false;
         log("info: loading config for \"info\"");
         optInfoTimeOffset = getInt("optInfoTimeOffset", 300, 0, 1199) * 20;
         optInfoPrefixNear = getString("optInfoPrefixNear", "\u00a7b");
         optInfoPrefixFar = getString("optInfoPrefixFar", "\u00a79");
         infoDeathY = 1024; // just a magic number denoting that there are no coord.
         return modInfoActive = true;
     }
     
     private static void quitModInfo() {
         if (!modInfoActive) return;
         modInfoActive = false;
     }
 
     private static void optionsModInfo() {
         boolean wasEnabled = modInfoEnabled;
         modInfoEnabled = getSetBool(modInfoEnabled, "modInfoEnabled", false,     "Enable Info mod");
         if (wasEnabled != modInfoEnabled) {
             if (modInfoEnabled && !modInfoActive) initModInfo(); 
             if (!modInfoEnabled && modInfoActive) quitModInfo();
         }
         keyInfoToggle = getSetBind(keyInfoToggle, "keyInfoToggle", Keyboard.KEY_F12, "Toggle info screen");
         optInfoShowPos = getSetBool(optInfoShowPos, "optInfoShowPos", true, "Show your coordinates");
         optInfoShowTime = getSetBool(optInfoShowTime, "optInfoShowTime", true, "Show time");
         optInfoShowBiome = getSetBool(optInfoShowBiome, "optInfoShowBiome", false, "Show biome name");
         optInfoShowItem = getSetBool(optInfoShowItem, "optInfoShowItem", true, "Show selected item information");
         optInfoShowHealth = getSetBool(optInfoShowHealth, "optInfoShowHealth", false, "Show critter health at all times");
         optInfoHideAchievement = getSetBool(optInfoHideAchievement, "optInfoHideAchievement", false, "Hide the obnoxious achievements");
         keyInfoPlayersToggle = getSetBind(keyInfoPlayersToggle, "keyInfoPlayersToggle", Keyboard.KEY_F4, "Toggle player list");
         optInfoRangeNear = getSetLog(optInfoRangeNear, "optInfoRangeNear", 50f, 1f, Math.max(Math.min(optInfoRangeMax,500),10), "Player 'near' range");
         optInfoRangeMax = getSetLog(optInfoRangeMax, "optInfoRangeMax", 1000f, Math.min(Math.max(optInfoRangeNear,10),500), 1000f, "Player 'far' range");
         optInfoShowFPS = getSetBool(optInfoShowFPS, "optInfoShowFPS", false, "Show FPS counter on info bar");
     }
 
     private static void updateModInfo(List list) {
         if (!modInfoActive) return;
         if (!isMenu && keyPress(keyInfoToggle)) infoShow = !infoShow;
         if (!isMenu && keyPress(keyInfoPlayersToggle)) infoPlayerShow = !infoPlayerShow;
         delMsg(MINFO);
         String info = "";
         int x = fix(posX), y = fix(posY), z = fix(posZ);
         if (getHealth(player) <= 0) { infoDeathX = x; infoDeathY = y; infoDeathZ = z; }
         if (!infoShow && infoPlayerShow && !isMenu) { // isMultiplayer
             int mX, mY, mZ, dist;
             String add = "";
             Iterator it = list.iterator();
             while (it.hasNext()) {
                 Object obj = it.next();
                 if (!(obj instanceof EntityPlayer) || obj==player) continue;
                 EntityPlayer ent = (EntityPlayer)obj;
                 //if(!(obj instanceof EntitySheep)) continue; EntityLiving ent = (EntityLiving)obj;
                 mX = fix(getEntityPosX(ent)) - x;
                 mY = fix(getEntityPosY(ent)) - y;
                 mZ = fix(getEntityPosZ(ent)) - z;
                 dist = (int)Math.sqrt(mX*mX + mY*mY + mZ*mZ);
                 if (dist > optInfoRangeMax) continue;
                 String dir = getAngleName((float)Math.atan2(mX, mZ) * (-180f / 3.141592654f));
                 add += (dist < optInfoRangeNear ? optInfoPrefixNear : optInfoPrefixFar) + getEntityName(ent) + "\u00a7f (\u00a79"+dist+"\u00a7fm \u00a79"+dir+"\u00a7f)\n";
             }
             if (add.length() == 0) add = "no players nearby";
             setMsg(MTAGR, add);
         } else delMsg(MTAGR);
         
         if (infoShow && !isMenu) {
             int mx, my, mz, id, meta, cap, cnt, cx = x >> 4, cz = z >> 4;
             long time = getTime(), timeRT = time;
             float val;
             ChunkCoordinates at;
             if (modSunActive && sunTimeOffset != 0) time += sunTimeOffset;
             // your location
             info += "Your position:   \u00a79" + x + "\u00a7f , \u00a79" + y + "\u00a7f , \u00a79" + z + "\u00a7f    Fog: \u00a79" + getViewDistance() + "\u00a7f    Exp-orbs: \u00a79" + player.experienceTotal;
             if (y >= 0) info += "\n  Light level:   \u00a79" + getLightLevel(x,y,z) + "\u00a7f   (   min: \u00a78"+getLightLevel(x,y,z,16)+"\u00a7f   max: \u00a7e"+getLightLevel(x,y,z,0)+"\u00a7f   )"; // current light level, min, max
             info += "\n  Biome:   \u00a79" + getBiomeName(x,z); // biome
 //            val = getTemp(); if (!Float.isNaN(val)) info += "\u00a7f   temp = \u00a79" + (int)(val * 40) + "\u00a7f C";
 //            val = getHumid(); if (!Float.isNaN(val)) info += "\u00a7f   humid = \u00a79" + (int)(val * 100) + "\u00a7f %";
             Random rnd = new Random(getSeed() + (long)(cx * cx * 0x4c1906) + (long)(cx * 0x5ac0db) + (long)(cz * cz) * 0x4307a7L + (long)(cz * 0x5f24f) ^ 0x3ad8025f); // the silliest nonsense i have ever seen x_x
             info += "\n  Slime spawns:   \u00a79" + (rnd.nextInt(10)==0 ? "yes " : "no  ") + "\u00a7f  chunk: \u00a79"+cx+"\u00a7f , \u00a79"+cz;
             ChunkPosition stronghold = map.findClosestStructure("Stronghold", x, y, z);
             if (stronghold != null) {
                 info += "\n  Stronghold:    \u00a79" + (mx = stronghold.x) + "\u00a7f , \u00a79" + stronghold.y + "\u00a7f , \u00a79" + (mz = stronghold.z);
                 mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
             }
             // the world
             info += "\nCompasspoint:   \u00a79" + (mx = world.getSpawnX()) + "\u00a7f , \u00a79" + (my = world.getSpawnY()) + "\u00a7f , \u00a79" + (mz = world.getSpawnZ());
             mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
             if (modCompassActive && compassHaveMine) {
                 if (compassShowOrig) info += "\n  Alt: \u00a79" + (mx = compassMX) + "\u00a7f , \u00a79" + (my = compassMY) + "\u00a7f , \u00a79" + (mz = compassMZ);
                 else info += "\n  Orig: \u00a79" + (mx = compassOX) + "\u00a7f , \u00a79" + (my = compassOY) + "\u00a7f , \u00a79" + (mz = compassOZ);
                 mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
             }
             at = getSpawn(player);
             if (at != null) {
                 info += "\nSpawnpoint:   \u00a79" + (mx = getX(at)) + "\u00a7f , \u00a79" + (my = getY(at)) + "\u00a7f , \u00a79" + (mz = getZ(at)); // spawnpoint
                 mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
                 if (isMultiplayer && getBed(player)==null) info += "  \u00a74Bed-override?";
             }
             at = getBed(player);
             if (at != null) {
                 info += "\nYour bed:   \u00a79" + (mx = getX(at)) + "\u00a7f , \u00a79" + (my = getY(at)) + "\u00a7f , \u00a79" + (mz = getZ(at)); // bedpoint
                 mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
             }
             if (infoDeathY != 1024) {
                 info += "\nYou died:   \u00a79" + (mx = infoDeathX) + "\u00a7f , \u00a79" + (my = infoDeathY) + "\u00a7f , \u00a79" + (mz = infoDeathZ); // deathpoint
                 mx -= x; mz -= z; info += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + mz*mz) + "\u00a7fm)";
             }
             if (!isMultiplayer) info += "\nWorld Name:   \u00a79" + getName(); // world name
             info += "\nWorld Seed:   \u00a79" + getSeed(); // world seed
             info += "\nWorld Age (real time):   \u00a79" + getRealTime(timeRT);
             // state of world
             info += "\nTime:   \u00a79" + getTime(time);
             if (time != timeRT) info += "\u00a7f   (actual time: \u00a79" + getTime(timeRT)+"\u00a7f )";
             if (!isMultiplayer) {
                 info += "\nRain:   \u00a79" + (getRain() ? "raining" : "not raining");
                 if ((!modWeatherActive || !optWeatherLocked) && !isHell) info += "\u00a7f the next \u00a79" + (getRainTime() / 20) + "\u00a7f seconds"; // rain
                 info += "\nThunder:   \u00a79" + (getThunder() ? "thundering" : "not thundering");
                 if ((!modWeatherActive || !optWeatherLocked) && !isHell) info += "\u00a7f the next \u00a79" + (getThunderTime() / 20) + "\u00a7f seconds"; // thunder
             }
             // item in hand
             ItemStack items = invItemsArr[getInvCur()];
             if (optInfoShowItem && items != null) {
                 id = getItemsId(items);
                 meta = getItemsInfo(items);
                 cnt = getItemsCount(items);
                 Item item = getItem(id);
                 info += "\nSelected item:   \u00a79" + getItemName(item) + "\u00a7f  id: \u00a79" + id + (getItemHasSubTypes(item) ? "\u00a7f/\u00a79" + meta : "") + "\u00a7f  stack: \u00a79" + cnt + "\u00a7f/\u00a79" + getItemMax(item);
                 if ((cap = getItemDmgCap(item)) != 0) info += "\u00a7f  damage: \u00a79" + meta + "\u00a7f/\u00a79" + cap;
                 info += "\u00a7f  type: \u00a79" + (id < 256 ? "Block" : "Item");
                 if (id < 256) {
                     Block block = getBlock(id);
                     info += "\n  Properties:   strength = \u00a79" + getBlockStrength(block) + "\u00a7f  resistance = \u00a79" + getBlockResist(block) + "\u00a7f  slipperiness = \u00a79" + getBlockSlip(block);
                     info += "\n  Light:   emission = \u00a79" + getBlockLight(id) + "\u00a7f  reduction = \u00a79" + (getBlockIsOpaque(id) ? "opaque" : getBlockOpacity(id) );
                     info += "\n  Fire:   spread = \u00a79" + getFireSpread(id) + "\u00a7f  burn = \u00a79" + getFireBurn(id);
                     Material mat = getBlockMaterial(block);
                     info += "\n  Mater\u00a7fial:   \u00a79"; int i = 0; // MACRO madness :p
                     if (getIsSolid(mat)) { info += " solid"; i++; }
                     if (getIsBurnable(mat)) { info += " burnable"; i++; }
                     if (getIsReplaceable(mat)) { info += " replaceable"; i++; }
                     if (getIsLiquid(mat)) { info += " liquid"; i++; }
                     if (getIsCover(mat)) { info += " cover"; i++; }
                     if (i==0) info += "-";
                 }
             }
             // players
             if (isMultiplayer) {
                 int width = getScrWidthS(), cur, len; cnt = 0; int i = 0;
                 String tmp = "", add;
                 cur = showTextLength("Players nearby (00):  ");
                 Iterator it = list.iterator();
                 while (it.hasNext()) {
                     Object obj = it.next();
                     if (!(obj instanceof EntityPlayer) || obj==player) continue;
                     EntityPlayer ent = (EntityPlayer)obj;
                     tmp += (cnt==0?" \u00a79":", \u00a79"); cur += showTextLength(cnt==0?" \u00a79":", \u00a79");
                     add = getPlayerName(ent);
                     mx = fix(getEntityPosX(ent)) - x;
                     my = fix(getEntityPosY(ent)) - y;
                     mz = fix(getEntityPosZ(ent)) - z;
                     add += "\u00a7f (\u00a79" + (int)Math.sqrt(mx*mx + my*my + mz*mz) + "\u00a7fm)";
                     len = showTextLength(add); i++;
                     if (width < cur + len || i > 4) { i = 0; tmp += "\n   \u00a79" + add; cur = len + showTextLength("   "); } else { tmp += add; cur += len; }
                     cnt++;
                 }
                 if (cnt==0) tmp += "\u00a74none";
                 info += "\nPlayers nearby (\u00a79" + cnt + "\u00a7f):  " + tmp;
             }
             setMsg(MINFO, info);
         }
     }
 
     private static void respawnInfoMod() {
         infoDeathX = fix(posX);
         infoDeathY = fix(posY);
         infoDeathZ = fix(posZ);
     }
 
     private static void drawGuiModInfo() {
         if (modInfoActive && optInfoHideAchievement) killAchievement();
     }
     
     private static String textModInfo(String txt) {
         if (!modInfoActive) return txt;
         if (optInfoShowFPS) {
             long time = System.currentTimeMillis();
             infoFrames++;
             if (time > infoTime + 1000) {
                 infoFps = ""+infoFrames+"FPS ";
                 infoTime = time;
                 infoFrames = 0;
             }
             txt += infoFps;
         }
         if (optInfoShowTime) {
             txt += "[" + getTime(getTime() + sunTimeOffset) + "] ";
         }
         return txt;
     }
 
     private static String textModInfoBiome(String txt) {
         if (!modInfoActive || !optInfoShowBiome) return txt;
         return txt + getBiomeName(fix(posX),fix(posZ)) + " ";
     }
 
 
     //=ZMod=Icon==============================================================
     private static boolean modIconEnabled, modIconActive;
     private static boolean optIconShowChest, optIconShowDispenser, optIconShowFurnace, optIconMP;
     private static boolean iconMPSupport;
 
     private static boolean initModIcon() {
         if (!checkStatus(MOD_ICON, "icon")) return false;
         log("info: loading config for \"icon\"");
         optIconMP = getBool("optIconMP", false);
         iconMPSupport = false;
         return modIconActive = true;
     }
     
     private static void quitModIcon() {
         if (!modIconActive) return;
         modIconActive = false;
     }
     
     private static void optionsModIcon() {
         boolean wasEnabled = modIconEnabled;
         modIconEnabled = getSetBool(modIconEnabled, "modIconEnabled", false,     "Enable Icon mod");
         if (wasEnabled != modIconEnabled) {
             if (modIconEnabled && !modIconActive) initModIcon(); 
             if (!modIconEnabled && modIconActive) quitModIcon();
         }
         optIconShowChest = getSetBool(optIconShowChest, "optIconShowChest", true, "Show chest contents (first item)");
         optIconShowDispenser = getSetBool(optIconShowDispenser, "optIconShowDispenser", true, "Show dispenser contents (first item)");
         optIconShowFurnace = getSetBool(optIconShowFurnace, "optIconShowFurnace", true, "Show furnace contents");
     }
 
     public static void drawModIcon(float x, float y, float z) {
         if ((isMultiplayer && !iconMPSupport) || !modIconActive) return;
         int ix = fix(posX), iy = fix(posY), iz = fix(posZ), range = 16, blockId;
         textureBlock = "/terrain.png"; textureItems = "/gui/items.png"; texture = null;
         GL11.glBegin(GL11.GL_QUADS);
         for (int dx=-range;dx<=range;dx++) for (int dy=-range;dy<=range;dy++) for (int dz=-range;dz<=range;dz++) if ((block[blockId = mapXGetId(ix+dx, iy+dy, iz+dz)] & STORAGE) != 0) {
             Icon icon = null; 
             int side, i, xx = ix+dx, yy = iy+dy, zz = iz+dz;
             TileEntity tent = getTileEntity(xx, yy, zz);
             if (tent == null) continue;
             float vx, vy, vz, tx, ty;
             if (blockId == 54) { // chest
                 if (!optIconShowChest) continue;
                 /*
                 ItemStack items[] = getChestItems(tent);
                 for (i=0;i<27;i++) if (items[i] != null) { icon = bindAndGetIcon(items[i]); break; }
                 if (icon < 0) continue;
                 vx = 0.5f + xx - x; vy = 0.5f + yy - y; vz = 0.5f + zz - z; tx = 0.0625f * (icon % 16); ty = 0.0625f * (icon / 16);
                 if ((block[mapXGetId(xx-1, yy, zz)] & SPACE) != 0) chestDrawIcon(4, getLight(xx-1, yy, zz), tx, ty, vx, vy, vz, -0.4f, -0.1f, -0.4f, -0.1f, -0.0625f);
                 if ((block[mapXGetId(xx+1, yy, zz)] & SPACE) != 0) chestDrawIcon(5, getLight(xx+1, yy, zz), tx, ty, vx, vy, vz, -0.4f, -0.1f, -0.4f, -0.1f, -0.0625f);
                 if ((block[mapXGetId(xx, yy, zz-1)] & SPACE) != 0) chestDrawIcon(2, getLight(xx, yy, zz-1), tx, ty, vx, vy, vz, -0.4f, -0.1f, -0.4f, -0.1f, -0.0625f);
                 if ((block[mapXGetId(xx, yy, zz+1)] & SPACE) != 0) chestDrawIcon(3, getLight(xx, yy, zz+1), tx, ty, vx, vy, vz, -0.4f, -0.1f, -0.4f, -0.1f, -0.0625f);
             } else if (blockId == 23) { // dispenser
                 if (!optIconShowDispenser) continue;
                 ItemStack items[] = getDispItems(tent);
                 for (i=0;i<9;i++) if (items[i] != null) { icon = bindAndGetIcon(items[i]); break; }
                 if (icon == null) continue;
                 vx = 0.5f + xx - x; vy = 0.5f + yy - y; vz = 0.5f + zz - z; tx = 0.0625f * (icon % 16); ty = 0.0625f * (icon / 16);
                 side = mapXGetMeta(xx, yy, zz); if (side < 4) zz += side == 2 ? -1 : 1; else xx += side == 4 ? -1 : 1;
                 if ((block[mapXGetId(xx, yy, zz)] & SPACE) != 0) chestDrawIcon(side, getLight(xx, yy, zz), tx, ty, vx, vy, vz, -0.1f, 0.1f, -0.1f, 0.1f, 0.0f);
             } else { // furnace
                 if (!optIconShowFurnace) continue;
                 ItemStack items[] = getFurnaceItems(tent);
                 vx = 0.5f + xx - x; vy = 0.5f + yy - y; vz = 0.5f + zz - z;
                 side = mapXGetMeta(xx, yy, zz); if (side < 4) zz += side == 2 ? -1 : 1; else xx += side == 4 ? -1 : 1;
                 if ((block[mapXGetId(xx, yy, zz)] & SPACE) != 0) {
                     float light = getLight(xx, yy, zz);
                     if (items[0] != null) { // input
                         icon = bindAndGetIcon(items[0]); tx = 0.0625f * (icon % 16); ty = 0.0625f * (icon / 16);
                         chestDrawIcon(side, light, tx, ty, vx, vy, vz, -0.4f, -0.2f,  0.2f,  0.4f, 0.0f);
                     }
                     if (items[1] != null) { // fuel
                         icon = bindAndGetIcon(items[1]); tx = 0.0625f * (icon % 16); ty = 0.0625f * (icon / 16);
                         chestDrawIcon(side, light, tx, ty, vx, vy, vz, -0.1f,  0.1f, -0.4f, -0.2f, 0.0f);
                     }
                     if (items[2] != null) { // output
                         icon = bindAndGetIcon(items[2]); tx = 0.0625f * (icon % 16); ty = 0.0625f * (icon / 16);
                         chestDrawIcon(side, light, tx, ty, vx, vy, vz,  0.2f,  0.4f,  0.2f,  0.4f, 0.0f);
                     }
                 }
                 */
             }
         }
         GL11.glEnd();
     }
     
     public static void packet250Handle(ZP250 obj) {
         iconMPSupport = true;
         loadTileEntityFromNBT(obj);
     }
 
     private static String textureBlock, textureItems, texture;
     private static final float iconSize = 1f / 16f;
     private static void chestDrawIcon(int side, float color, float tx, float ty, float x, float y, float z, float xs, float xe, float ys, float ye, float depth) {
         depth += 0.52f;
         GL11.glColor3f(color, color, color);
         float txe = tx + iconSize, tye = ty + iconSize;
         if (side == 4) { // -x
             GL11.glTexCoord2f(txe,tye); GL11.glVertex3f(x-depth, y+ys, z+xe);
             GL11.glTexCoord2f(txe,ty ); GL11.glVertex3f(x-depth, y+ye, z+xe);
             GL11.glTexCoord2f(tx ,ty ); GL11.glVertex3f(x-depth, y+ye, z+xs);
             GL11.glTexCoord2f(tx ,tye); GL11.glVertex3f(x-depth, y+ys, z+xs);
         } else if (side == 5) { // +x
             GL11.glTexCoord2f(txe,tye); GL11.glVertex3f(x+depth, y+ys, z-xe);
             GL11.glTexCoord2f(txe,ty ); GL11.glVertex3f(x+depth, y+ye, z-xe);
             GL11.glTexCoord2f(tx ,ty ); GL11.glVertex3f(x+depth, y+ye, z-xs);
             GL11.glTexCoord2f(tx ,tye); GL11.glVertex3f(x+depth, y+ys, z-xs);
         } else if (side == 2) { // -z
             GL11.glTexCoord2f(txe,tye); GL11.glVertex3f(x-xe, y+ys, z-depth);
             GL11.glTexCoord2f(txe,ty ); GL11.glVertex3f(x-xe, y+ye, z-depth);
             GL11.glTexCoord2f(tx ,ty ); GL11.glVertex3f(x-xs, y+ye, z-depth);
             GL11.glTexCoord2f(tx ,tye); GL11.glVertex3f(x-xs, y+ys, z-depth);
         } else if (side == 3) { // +z
             GL11.glTexCoord2f(txe,tye); GL11.glVertex3f(x+xe, y+ys, z+depth);
             GL11.glTexCoord2f(txe,ty ); GL11.glVertex3f(x+xe, y+ye, z+depth);
             GL11.glTexCoord2f(tx ,ty ); GL11.glVertex3f(x+xs, y+ye, z+depth);
             GL11.glTexCoord2f(tx ,tye); GL11.glVertex3f(x+xs, y+ys, z+depth);
         }
     }
 
     private static Icon bindAndGetIcon(ItemStack items) {
         int id = getItemsId(items);
         if (getItem(id) == null) return null; // chest contains unknown stuff
         if (id == 35) setItemsInfo(items, 15 - getItemsInfo(items)); // wool icon fix (why the hell is this messed up !?)
         String tex = id < 256 ? textureBlock : textureItems;
         Icon icon = getItemsIcon(items);
         if (id == 35) setItemsInfo(items, 15 - getItemsInfo(items)); // wool icon fix
         if (tex == texture) return icon;
         /*
         GL11.glEnd();
         GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture = tex);
         GL11.glBegin(GL11.GL_QUADS);
         */
         bindTexture(texture = tex);
         return icon;
     }
     
     
     //=ZMod=Chest=============================================================
     private static boolean modChestEnabled, modChestActive;
     private static boolean optChestStore;
     private static int optChestStoreRadius, optChestStoreBlock;
 
     private static boolean initModChest() {
         if (!checkStatus(MOD_CHEST, "chest")) return false;
         log("info: loading config for \"chest\"");
         optChestStoreBlock = getBlockId("optChestStoreBlock", 58); // workbench
         return modChestActive = true;
     }
     
     private static void quitModChest() {
         if (!modChestActive) return;
         modChestActive = false;
     }
     
     private static void optionsModChest() {
         boolean wasEnabled = modChestEnabled;
         modChestEnabled = getSetBool(modChestEnabled, "modChestEnabled", false,     "Enable Chest mod");
         if (wasEnabled != modChestEnabled) {
             if (modChestEnabled && !modChestActive) initModChest(); 
             if (!modChestEnabled && modChestActive) quitModChest();
         }
         optChestStore = getSetBool(optChestStore, "optChestStore", true, "Autostore items on top of chests");
         optChestStoreRadius = getSetInt(optChestStoreRadius, "optChestStoreRadius", 2, 0, 8, "Search radius for 'store' block"); // 0 = off
     }
 
     private static void updateModChest(List list) {
         if (!modChestActive || isMultiplayer || (!optChestStore && optChestStoreRadius<=0)) return;
         Iterator iter = list.iterator();
         while (iter.hasNext()) {
             Object obj = iter.next();
             if (!(obj instanceof EntityItem)) continue;
             EntityItem ent = (EntityItem)obj;
             if (getEntityAge(ent) < 0) continue;
             TileEntityChest tent = null;
             ItemStack items[] = null;
             ItemStack sub = getEntityItemStack(ent);
             int findId = getItemsId(sub), findMeta = getItemsInfo(sub), count;
             int entX = fix(getEntityPosX(ent)), entY = fix(getEntityPosY(ent)) - 1, entZ = fix(getEntityPosZ(ent));
             int blockId = mapXGetId(entX,entY,entZ), slot = 0, score = 0, space = 0, maxspace = getItemMax(getItem(findId)), empty = -1;
             boolean hadMatch;
             // score: 0-cannot, 1=free-space, 2=free-space-and-same-items, 3=found-nonfull-matching-stack
             int radius = -1;
             if (blockId == optChestStoreBlock && optChestStoreRadius > 0) radius = optChestStoreRadius;
             else if (optChestStore && blockId == 54) radius = 0;
             if (radius >= 0) {
                 // find a nice chest
                 Search: for (int dX=-radius;dX<=radius;dX++) for (int dY=-radius;dY<=radius;dY++) for (int dZ=-radius;dZ<=radius;dZ++) if (mapXGetId(entX+dX, entY+dY, entZ+dZ) == 54) {
                     TileEntityChest foundtent = (TileEntityChest)getTileEntity(entX+dX, entY+dY, entZ+dZ);
                     ItemStack found[] = getChestItems(foundtent);
                     empty = -1; hadMatch = false;
                     for (int chestSlot=0;chestSlot<27;chestSlot++)
                         if (found[chestSlot]==null) {
                             if (empty == -1) empty = chestSlot;
                             if (score<2 && hadMatch) { items = found; tent = foundtent; slot = chestSlot; score = 2; space = maxspace; }
                             else if (score==0) { items = found; tent = foundtent; slot = chestSlot; score = 1; space = maxspace; }
                         } else if (getItemsId(found[chestSlot])==findId && getItemsInfo(found[chestSlot])==findMeta) {
                             hadMatch = true;
                             if (empty != -1 && score == 1) { items = found; tent = foundtent; slot = empty; score = 2; space = maxspace; }
                             if ((count = getItemsCount(found[chestSlot])) < maxspace) { items = found; tent = foundtent; slot = chestSlot; score = 3; space = maxspace - count; break Search; }
                         }
                 }
                 // found one?
                 if (tent != null) {
                     // squeeze it in ...
                     count = getItemsCount(sub);
                     if (items[slot]==null) items[slot] = newItems(findId, count > space ? space : count, findMeta);
                     else {
                         if (count > space) setItemsCount(items[slot], maxspace);
                         else setItemsCount(items[slot], getItemsCount(items[slot]) + count);
                     }
                     count -= space;
                     if (count <= 0) dieEntity(ent);
                     setChanged(tent);
                     if (count > 0) { setItemsCount(sub, count); continue; }
                 }
                 // skip testing it for 5 seconds
                 setEntityAge(ent, -100);
             }
         }
     }
 
 
     //=ZMod=Cloud=============================================================
     private static boolean modCloudEnabled, modCloudActive;
     private static String tagCloudVanilla;
     private static int keyCloudToggle, keyCloudUp, keyCloudDown, keyCloudVanilla;
     private static boolean optCloudShow, optCloudVanilla;
     private static float optCloudOffset;
     private static double cloudOrigPlayerY;
 
     private static boolean initModCloud() {
         if (!checkStatus(MOD_CLOUD, "cloud")) return false;
         log("info: loading config for \"cloud\"");
         tagCloudVanilla       = getString("tagCloudVanilla", "no-cloud-mod");
         return modCloudActive = true;
     }
     
     private static void quitModCloud() {
         if (!modCloudActive) return;
         modCloudActive = false;
     }
     
     private static void optionsModCloud() {
         boolean wasEnabled = modCloudEnabled;
         modCloudEnabled = getSetBool(modCloudEnabled, "modCloudEnabled", false,     "Enable Cloud mod");
         if (wasEnabled != modCloudEnabled) {
             if (modCloudEnabled && !modCloudActive) initModCloud(); 
             if (!modCloudEnabled && modCloudActive) quitModCloud();
         }
         keyCloudToggle        = getSetBind(keyCloudToggle, "keyCloudToggle",     Keyboard.KEY_MULTIPLY, "Toggle clouds");
         optCloudShow          = getSetBool(optCloudShow, "optCloudShow", true, "Show clouds by default");
         keyCloudVanilla       = getSetBind(keyCloudVanilla, "keyCloudVanilla",    Keyboard.KEY_V, "Toggle vanilla clouds");
         keyCloudUp            = getSetBind(keyCloudUp, "keyCloudUp",         Keyboard.KEY_NONE, "Move clouds up");
         keyCloudDown          = getSetBind(keyCloudDown, "keyCloudDown",       Keyboard.KEY_NONE, "Move clouds down");
         optCloudOffset        = getSetFloat(optCloudOffset, "optCloudOffset", 4F, -60F, 140F, "Cloud offset");
     }
 
     private static void updateModCloud() {
         if (!modCloudActive || isMenu) return;
         if (keyPress(keyCloudVanilla)) optCloudVanilla = !optCloudVanilla;
         if (keyPress(keyCloudToggle)) { if (optCloudVanilla) optCloudVanilla = false; else optCloudShow = !optCloudShow; }
         if (keyPress(keyCloudUp))     { if (optCloudVanilla) optCloudVanilla = false; else optCloudOffset += 1f; }
         if (keyPress(keyCloudDown))   { if (optCloudVanilla) optCloudVanilla = false; else optCloudOffset -= 1f; }
     }
 
     public static void drawModCloud(float delta) {
         
     }
     
     public static void onRenderClouds(float delta) {
         if (modCloudActive && !optCloudVanilla) {
             if (optCloudShow && player != null) {
                 double mov = getEntityPrevPosY(player);
                 setEntityPrevPosY(player, mov + (getEntityPosY(player) - mov) * delta - optCloudOffset);
                 render.forwardRenderClouds(0F);
                 setEntityPrevPosY(player, mov);
             }
         } else {
             render.forwardRenderClouds(delta);
         }
     }
     
     private static String textModCloud(String txt) {
         if (!modCloudActive || !optCloudVanilla || tagCloudVanilla.length()==0) return txt;
         return txt + tagCloudVanilla + " ";
     }
     
     
     //=ZMod=Cart==============================================================
     private static boolean modCartEnabled, modCartActive;
     private static String tagCartPerpetual;
     private static int keyCartStop, keyCartPerpetual;
     private static boolean optCartPerpetual, optCartInfiniteFuel;
     private static float optCartSpeedAccumCap, optCartAcceleration;
     private static double cartSpeed;
 
     private static boolean initModCart() {
         if (!checkStatus(MOD_CART, "cart")) return false;
         log("info: loading config for \"cart\"");
         tagCartPerpetual      = getString("tagCartPerpetual", "perpetual");
         return modCartActive = true;
     }
     
     private static void quitModCart() {
         if (!modCartActive) return;
         modCartActive = false;
     }
     
     private static void optionsModCart() {
         boolean wasEnabled = modCartEnabled;
         modCartEnabled = getSetBool(modCartEnabled, "modCartEnabled", false,     "Enable Cart mod");
         if (wasEnabled != modCartEnabled) {
             if (modCartEnabled && !modCartActive) initModCart(); 
             if (!modCartEnabled && modCartActive) quitModCart();
         }
         keyCartStop           = getSetBind(keyCartStop, "keyCartStop",        Keyboard.KEY_RETURN, "Stop the minecart instantly");
         keyCartPerpetual      = getSetBind(keyCartPerpetual, "keyCartPerpetual",   Keyboard.KEY_UP, "Toggle perpetual motion mode");
         optCartSpeedAccumCap  = getSetFloat(optCartSpeedAccumCap, "optCartSpeedAccumCap", 1f, 0.5f, 5f, "Speed accumulation cap");
         optCartAcceleration   = getSetFloat(optCartAcceleration, "optCartAcceleration", 2f, 0.5f, 10f, "Acceleration");
         optCartInfiniteFuel   = getSetBool(optCartInfiniteFuel, "optCartInfiniteFuel", true, "Infinite fuel for powered minecart");
     }
 
     private static void updateModCart(List list) {
         if (isMultiplayer || !modCartActive || !(inWhat instanceof EntityMinecart)) return;
         double mx = getEntityMotionX(inWhat) + motionX * optCartAcceleration, mz = getEntityMotionZ(inWhat) + motionZ * optCartAcceleration;
         double speed = Math.sqrt(mx*mx+mz*mz), rate;
         if (!isMenu && keyPress(keyCartPerpetual)) {
             optCartPerpetual = !optCartPerpetual;
             if (optCartPerpetual) cartSpeed = speed;
         }
         //if(keyPress(getKeyJump())) { inWhat.motionY += 1.0f; } // damn, cart is glued on rails
         if ((speed > optCartSpeedAccumCap) || (optCartPerpetual && speed > 0.001d )) {
             rate = optCartPerpetual ? (cartSpeed / speed) : (optCartSpeedAccumCap / speed);
             mz *= rate; mz *= rate;
         }
         if (!isMenu && keyDown(keyCartStop)) { mx = mz = 0.0d; optCartPerpetual = false; }
         setEntityMotionX(inWhat, mx); setEntityMotionZ(inWhat, mz);
         // handle powered minecarts
         if (optCartInfiniteFuel) {
             Iterator iter = list.iterator();
             while (iter.hasNext()) {
                 Object obj = iter.next();
                 if (!(obj instanceof EntityMinecart)) continue;
                 EntityMinecart ent = (EntityMinecart)obj;
                 //if (getCartType(ent) != 2) continue;
                 //if (getCartFuel(ent) > 0) setCartFuel(ent, 1200);
             }
         }
     }
     
     private static String textModCart(String txt) {
         if (isMultiplayer || !modCartActive || !optCartPerpetual || tagCartPerpetual.length()==0) return txt;
         return txt + tagCartPerpetual + " ";
     }
     
     
     //=ZMod=Wield=============================================================
     public static boolean modWieldEnabled, modWieldActive;
     public static String tagWieldAmmo;
     public static int keyWield;
     public static boolean optWieldBowFirst, optWieldShowAmmo;
 
     private static boolean initModWield() {
         if (!checkStatus(MOD_WIELD, "wield")) return false;
         log("info: loading config for \"wield\"");
         tagWieldAmmo          = getString("tagWieldAmmo", "Arrows :") + " ";
         return modWieldActive = true;
     }
     
     private static void quitModWield() {
         if (!modWieldActive) return;
         modWieldActive = false;
     }
     
     private static void optionsModWield() {
         boolean wasEnabled = modWieldEnabled;
         modWieldEnabled = getSetBool(modWieldEnabled, "modWieldEnabled", false,     "Enable Wield mod");
         if (wasEnabled != modWieldEnabled) {
             if (modWieldEnabled && !modWieldActive) initModWield(); 
             if (!modWieldEnabled && modWieldActive) quitModWield();
         }
         keyWield              = getSetBind(keyWield, "keyWield",           Keyboard.KEY_R, "Wield key");
         optWieldBowFirst      = getSetBool(optWieldBowFirst, "optWieldBowFirst", true, "Wield bow first");
         optWieldShowAmmo      = getSetBool(optWieldShowAmmo, "optWieldShowAmmo", true, "Show arrow count");
     }
 
     private static void updateModWield() {
         if (!modWieldActive) return;
         int bow = -1, swd = -1, cur = getInvCur();
         int arrows = 0;
         boolean haveBow = false;
         for (int i=0;i<invItemsArr.length;i++) {
             int id = invItemsArr[i]==null ? 0 : getItemsId(invItemsArr[i]);
             if (id==262) arrows += getItemsCount(invItemsArr[i]);
             if (i<9) { if (id==261) { bow = i; haveBow = true; } else if (id==268 || id==272 || id==267 || id==276 || id==283) swd = i; }
         }
         if (arrows==0) bow = -1;
         if (bow == -1) bow = swd; else if (swd == -1) swd = bow;
         int set = optWieldBowFirst ? bow : swd;
         if (cur == set) set = set==bow ? swd : bow;
         if (!isMenu && keyPress(keyWield) && set != -1) setInvCur(set);
         if (optWieldShowAmmo && haveBow) setMsg(MTAG2, tagWieldAmmo + arrows, arrows > 8 ? (arrows > 32 ? (arrows > 64 ? 0xbbffbb : 0x22dd22) : 0xeeee11) : 0xdd3333);
     }
     
     
     //=ZMod=Build=============================================================
     private static boolean modBuildEnabled, modBuildActive;
     private static String tagBuildEnabled;
     private static int keyBuildToggle, keyBuildA, keyBuildB, keyBuildMark, keyBuildCopy, keyBuildPaste, keyBuildSet, keyBuildFill, keyBuildRemove, keyBuildDown, keyBuildDeselect;
     private static int optBuildLockQuantityToNr, optBuildHarvestRule;
     private static float optBuildDigSpeed, optBuildReach;
     private static boolean optBuild, optBuildExtension, optBuildLockQuantity;
     private static int buildSets[][], buildBufBlock[], buildBufExtra[];
     private static int buildSX, buildEX, buildSY, buildEY, buildSZ, buildEZ, buildMark = 0;
     private static int buildSizeX = 0, buildSizeY = 0, buildSizeZ = 0;
     private static int buildHandSlot, buildHandCount;
     private static NBTTagCompound buildBufNBT[];
     private static ItemStack buildHand;
     private static Lock buildLock = new ReentrantLock();
     private static List<int[]> buildActions;
 
     private static final int
         BUILD_ACTION_BITS = 3,
         BUILD_ACTION_SET = 1,
         BUILD_ACTION_COPY = 2,
         BUILD_ACTION_PASTE = 3,
         BUILD_MODIFIER_FILL = 64,
         BUILD_MODIFIER_REMOVE = 128;
         
 
     private static boolean initModBuild() {
         if (!checkStatus(MOD_BUILD, "build")) return false;
         log("info: loading config for \"build\"");
         optBuildExtension = getBool("optBuildExtension", false);
         if (!optBuildExtension) log("info: build extension is disabled");
         tagBuildEnabled       = getString("tagBuildEnabled", "builder");
         String sets[] = new String[]{
             getString("optBuildA1", ""),
             getString("optBuildA2", ""),
             getString("optBuildA3", ""),
             getString("optBuildA4", ""),
             getString("optBuildA5", ""),
             getString("optBuildA6", ""),
             getString("optBuildA7", ""),
             getString("optBuildA8", ""),
             getString("optBuildA9", ""),
             getString("optBuildB1", ""),
             getString("optBuildB2", ""),
             getString("optBuildB3", ""),
             getString("optBuildB4", ""),
             getString("optBuildB5", ""),
             getString("optBuildB6", ""),
             getString("optBuildB7", ""),
             getString("optBuildB8", ""),
             getString("optBuildB9", "")
         };
         buildSets = new int[sets.length][9];
         for (int set=0;set<sets.length;set++) if (!sets[set].equals("")) {
             String got[] = sets[set].split("[\\t ]*,[\\t ]*");
             int defs = got.length; if (defs>9) defs = 9;
             for (int slot=0;slot<defs;slot++) {
                 if (names.containsKey(got[slot])) buildSets[set][slot] = (Integer)(names.get(got[slot]));
                 else {
                     int id = parseIdInfo(got[slot]);
                     if (id==-1) err("error: config.txt @ optBuild"+(set>9?"B":"A")+((set%9)+1)+" - unknown item name or invalid code: \""+got[slot]+"\"");
                     else buildSets[set][slot] = id;
                 }
             }
         }
         buildMark = 0;
         buildActions = new LinkedList<int[]>();
         return modBuildActive = true;
     }
     
     private static void quitModBuild() {
         if (!modBuildActive) return;
         modBuildActive = false;
         buildSets = null;
         buildActions = null;
     }
 
     private static void optionsModBuild() {
         buildLock.lock();
         try {
         boolean wasEnabled = modBuildEnabled;
         modBuildEnabled = getSetBool(modBuildEnabled, "modBuildEnabled", false,     "Enable Build mod");
         if (wasEnabled != modBuildEnabled) {
             if (modBuildEnabled && !modBuildActive) initModBuild(); 
             if (!modBuildEnabled && modBuildActive) quitModBuild();
         }
         keyBuildToggle        = getSetBind(keyBuildToggle, "keyBuildToggle",     Keyboard.KEY_B, "Toggle builder mode");
         keyBuildA             = getSetBind(keyBuildA, "keyBuildA",          Keyboard.KEY_LSHIFT, "A item sets (this + number)");
         keyBuildB             = getSetBind(keyBuildB, "keyBuildB",          Keyboard.KEY_LCONTROL, "B item sets (this + number)");
         optBuild              = getSetBool(optBuild, "optBuild", false, "Builder mode is enabled by default");
         optBuildLockQuantity  = getSetBool(optBuildLockQuantity, "optBuildLockQuantity", true, "Lock item quantity", FEATURE_BROKEN);
         optBuildLockQuantityToNr = getSetInt(optBuildLockQuantityToNr, "optBuildLockQuantityToNr", 0, 0, 32, "Lock item quantity to nr (0 = don't)");
         optBuildDigSpeed      = getSetFloat(optBuildDigSpeed, "optBuildDigSpeed", 1f, 0.1f, 6f, "Digging speed", FEATURE_BROKEN);
         optBuildHarvestRule   = getSetInt(optBuildHarvestRule, "optBuildHarvestRule", -1, -1, 1, "Harvest rule (-1=never, 0=vanilla, 1=always)", FEATURE_BROKEN);
         optBuildReach         = getSetLog(optBuildReach, "optBuildReach", 16f, 2f, 128f, "Arm length", FEATURE_BROKEN);
         optBuildExtension     = getSetBool(optBuildExtension, "optBuildExtension", false, "Build extension enabled");
         keyBuildMark          = getSetBind(keyBuildMark, "keyBuildMark",       Keyboard.KEY_X, "Set marker");
         keyBuildCopy          = getSetBind(keyBuildCopy, "keyBuildCopy",       Keyboard.KEY_C, "Copy selected area");
         keyBuildPaste         = getSetBind(keyBuildPaste, "keyBuildPaste",      Keyboard.KEY_P, "Paste into selected area");
         keyBuildSet           = getSetBind(keyBuildSet, "keyBuildSet",        Keyboard.KEY_Z, "Set in selected area");
         keyBuildFill          = getSetBind(keyBuildFill, "keyBuildFill",       Keyboard.KEY_LSHIFT, "Modifier to fill only empty space");
         keyBuildRemove        = getSetBind(keyBuildRemove, "keyBuildRemove",     Keyboard.KEY_RSHIFT, "Modifier to remove matching");
         keyBuildDown          = getSetBind(keyBuildDown, "keyBuildDown",       Keyboard.KEY_LCONTROL, "Modifier to set marker at feet level");
         keyBuildDeselect      = getSetBind(keyBuildDeselect, "keyBuildDeselect",   Keyboard.KEY_NONE, "Remove markers");
         } finally {
             buildLock.unlock();
         }
     }
 
     private static void updateModBuild() {
         buildLock.lock();
         try {
         if (!modBuildActive) return;
         if (!optBuild || isMenu || !optBuildLockQuantity || isMultiplayer) buildHandSlot = -1;
         if (isMenu) return;
         if (keyPress(keyBuildToggle)) optBuild = !optBuild;
         if (isMapChange) optBuild = false;
         if (!optBuild) { buildMark = 0; return; } // deselect if build mode is not active
         // repeats
 /*                    if (keyPress(Keyboard.KEY_NUMPAD1) && rayTrace(256d, 0f)) {
             int optBuildMaxRepeat = 16;
             int x = rayHitX(), y = rayHitY(), z = rayHitZ(), s = rayHitSide(), ax = 0, ay = 0, az = 0;
             if (s==0) ay--; if (s==1) ay++; if (s==2) az--; if (s==3) az++; if (s==4) ax--; if (s==5) ax++;
             int id = mapXGetId(x,y,z), meta = mapXGetMeta(x,y,z);
             while (optBuildMaxRepeat-- > 0 && mapXGetId(x+=ax, y+=ay, z+=az) == 0) {
                 mapXSetIdMeta(x,y,z,id,meta);
             }
         } else if (keyPress(Keyboard.KEY_NUMPAD0) && rayTrace(256d, 0f)) {
             int x = rayHitX(), y = rayHitY(), z = rayHitZ(), s = rayHitSide(), ax = 0, ay = 0, az = 0;
         } */
         // sets
         int set = -1;
         if (keyDown(keyBuildA)) for (int i=Keyboard.KEY_1;i<=Keyboard.KEY_9;i++) if (keyPress(i)) set = i - Keyboard.KEY_1;
         if (keyDown(keyBuildB)) for (int i=Keyboard.KEY_1;i<=Keyboard.KEY_9;i++) if (keyPress(i)) set = 9 + i - Keyboard.KEY_1;
         if (set!=-1) for (int slot=0;slot<9;slot++) if (buildSets[set][slot]!=0) invItemsArr[slot] = newItemsE(buildSets[set][slot], 32);
         // map edit part of mod follows
         int x = fix(posX),y = fix(posY),z = fix(posZ);
         if (keyPress(keyBuildDeselect)) buildMark = 0; // deselect
         if (optBuildExtension && keyPress(keyBuildMark) && buildActions.isEmpty()) {
             if (buildMark == 1) {
                 buildEX = x; 
                 buildEY = keyDown(keyBuildDown) ? y - 1 : y; 
                 buildEZ = z; 
                 buildMark = 2; 
             } else { 
                 buildSX = x; 
                 buildSY = keyDown(keyBuildDown) ? y - 1 : y; 
                 buildSZ = z; buildMark = 1; 
             }
         } else if (buildMark == 2 && buildActions.isEmpty()) {
             // fix coords
             int tmp;
             if (buildSX > buildEX) { tmp = buildSX; buildSX = buildEX; buildEX = tmp; }
             if (buildSY > buildEY) { tmp = buildSY; buildSY = buildEY; buildEY = tmp; }
             if (buildSZ > buildEZ) { tmp = buildSZ; buildSZ = buildEZ; buildEZ = tmp; }
             // ready
             if (keyPress(keyBuildSet) && !isMultiplayer) {
                 int id = 0, meta = 0;
                 if (invItemsArr[getInvCur()] != null) {
                     id = getItemsId(invItemsArr[getInvCur()]);
                     meta = getItemsInfo(invItemsArr[getInvCur()]);
                     if (id >= 4096) id = meta = 0;
                 }
                 int[] data = new int[9];
                 data[0] = BUILD_ACTION_SET
                         | (keyDown(keyBuildFill) ? BUILD_MODIFIER_FILL : 0)
                         | (keyDown(keyBuildRemove) ? BUILD_MODIFIER_REMOVE : 0);
                 data[1] = buildSX; data[2] = buildSY; data[3] = buildSZ;
                 data[4] = buildEX; data[5] = buildEY; data[6] = buildEZ;
                 data[7] = id; data[8] = meta;
                 buildActions.add(data);
             } else if (keyPress(keyBuildCopy) && !isMultiplayer) {
                 int[] data = new int[7];
                 data[0] = BUILD_ACTION_COPY;
                 data[1] = buildSX; data[2] = buildSY; data[3] = buildSZ;
                 data[4] = buildEX; data[5] = buildEY; data[6] = buildEZ;
                 buildActions.add(data);
             } else if (keyPress(keyBuildPaste) && !isMultiplayer) {
                 int sx = 1 + buildEX - buildSX; sx = sx > buildSizeX ? sx % buildSizeX : 0;
                 int sy = 1 + buildEY - buildSY; sy = sy > buildSizeY ? sy % buildSizeY : 0;
                 int sz = 1 + buildEZ - buildSZ; sz = sz > buildSizeZ ? sz % buildSizeZ : 0;
                 if (sx!=0 || sy!=0 || sz!=0) { // adjust selection (try to avoid partial copy)
                     buildEX -= sx; buildEY -= sy; buildEZ -= sz;
                 }
                 int[] data = new int[7];
                 data[0] = BUILD_ACTION_PASTE
                         | (keyDown(keyBuildFill) ? BUILD_MODIFIER_FILL : 0)
                         | (keyDown(keyBuildRemove) ? BUILD_MODIFIER_REMOVE : 0);
                 data[1] = buildSX; data[2] = buildSY; data[3] = buildSZ;
                 data[4] = buildEX; data[5] = buildEY; data[6] = buildEZ;
                 buildActions.add(data);
             }
         } else if (false) {
             // fix coords
             int tmp;
             if (buildSX > buildEX) { tmp = buildSX; buildSX = buildEX; buildEX = tmp; }
             if (buildSY > buildEY) { tmp = buildSY; buildSY = buildEY; buildEY = tmp; }
             if (buildSZ > buildEZ) { tmp = buildSZ; buildSZ = buildEZ; buildEZ = tmp; }
             // ready
             if (keyPress(keyBuildSet) && !isMultiplayer) {
                 int id = 0, got, meta = 0;
                 if (invItemsArr[getInvCur()] != null) {
                     id = getItemsId(invItemsArr[getInvCur()]);
                     meta = getItemsInfo(invItemsArr[getInvCur()]);
                     if (id >= 4096) id = meta = 0;
                 }
                 if (keyDown(keyBuildFill)) {
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) if (mapXGetId(x,y,z)==0) mapXSetIdMetaNoUpdate(x,y,z,id,meta);
                 } else if (keyDown(keyBuildRemove)) {
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) if ((got=mapXGetId(x,y,z))==id || (id==8 && got==9) || (id==10 && got==11)) mapXSetIdMetaNoUpdate(x,y,z,0,0);
                 } else {
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) mapXSetIdMetaNoUpdate(x,y,z,id,meta);
                 }
                 mapXNeedsUpdate(buildSX-1,buildSY-1,buildSZ-1,buildEX+1,buildEY+1,buildEZ+1);
             } else if (keyPress(keyBuildCopy)) {
                 buildMark = 0;
                 buildSizeX = 1 + buildEX - buildSX;
                 buildSizeY = 1 + buildEY - buildSY;
                 buildSizeZ = 1 + buildEZ - buildSZ;
                 int size = buildSizeX * buildSizeY * buildSizeZ, at = 0;
                 buildBufBlock = new int[size];
                 buildBufExtra = new int[size];
                 buildBufNBT = new NBTTagCompound[size];
                 for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) {
                     buildBufBlock[at] = mapXGetId(x,y,z);
                     buildBufExtra[at] = mapXGetMeta(x,y,z);
                     buildBufNBT[at] = mapGetTileCopy(x,y,z);
                     at++;
                 }
             } else if (keyPress(keyBuildPaste) && buildBufBlock!=null && !isMultiplayer) {
                 int sx = 1 + buildEX - buildSX; sx = sx > buildSizeX ? sx % buildSizeX : 0;
                 int sy = 1 + buildEY - buildSY; sy = sy > buildSizeY ? sy % buildSizeY : 0;
                 int sz = 1 + buildEZ - buildSZ; sz = sz > buildSizeZ ? sz % buildSizeZ : 0;
                 if (sx!=0 || sy!=0 || sz!=0) { // adjust selection (try to avoid partial copy)
                     buildEX -= sx; buildEY -= sy; buildEZ -= sz;
                 } else if (keyDown(keyBuildFill)) { // fill space
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) if (mapXGetId(x,y,z)==0) {
                         int cx = (x-buildSX) % buildSizeX, cy = (y-buildSY) % buildSizeY, cz = (z-buildSZ) % buildSizeZ;
                         int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                         mapXSetIdMetaNoUpdate(x,y,z,buildBufBlock[at],buildBufExtra[at]);
                     }
                 } else if (keyDown(keyBuildRemove)) { // remove matching
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) {
                         int cx = (x-buildSX) % buildSizeX, cy = (y-buildSY) % buildSizeY, cz = (z-buildSZ) % buildSizeZ;
                         int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                         int id = buildBufBlock[at], got = mapXGetId(x,y,z);
                         if (id == got || (id==8 && got==9) || (id==10 && got==11)) mapXSetIdMetaNoUpdate(x,y,z,0,0);
                     }
                 } else { // replace
                     for (x=buildSX;x<=buildEX;x++) for (y=buildSY;y<=buildEY;y++) for (z=buildSZ;z<=buildEZ;z++) {
                         int cx = (x-buildSX) % buildSizeX, cy = (y-buildSY) % buildSizeY, cz = (z-buildSZ) % buildSizeZ;
                         int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                         mapXSetIdMetaNoUpdate(x,y,z,buildBufBlock[at],buildBufExtra[at]);
                         if (buildBufNBT[at] != null) mapSetTileCopy(buildBufNBT[at], x,y,z);
                     }
                 }
                 mapXNeedsUpdate(buildSX-1,buildSY-1,buildSZ-1,buildEX+1,buildEY+1,buildEZ+1);
             }
         } else if (buildMark==1 && keyPress(keyBuildPaste) && !isMultiplayer && buildActions.isEmpty()) {
             buildEX = buildSX + buildSizeX - 1; buildEY = buildSY + buildSizeY - 1; buildEZ = buildSZ + buildSizeZ - 1; buildMark = 2;
         }
         // lock items in hand
         if (optBuildLockQuantity && !isMultiplayer) {
             if (optBuildLockQuantityToNr != 0) {
                 for (int slot=0;slot<invItemsArr.length;slot++) if (invItemsArr[slot] != null) setItemsCount(invItemsArr[slot], optBuildLockQuantityToNr);
             } else {
                 int cur = getInvCur();
                 if (cur != buildHandSlot || (invItemsArr[cur] != null && invItemsArr[cur] != buildHand)) {
                     buildHandSlot = cur;
                     buildHand = invItemsArr[cur];
                     buildHandCount = buildHand != null ? getItemsCount(buildHand) : 0;
                 } else if (buildHand != null && (invItemsArr[cur] == null || invItemsArr[cur] == buildHand)) {
                     setItemsCount(buildHand, buildHandCount);
                     setInvItems(cur, buildHand);
                 }
             }
         }
         } finally {
             buildLock.unlock();
         }
     }
     
     private static void buildAction(World world, int[] data) {
         int action = data[0] & BUILD_ACTION_BITS;
         boolean fill = (data[0] & BUILD_MODIFIER_FILL) != 0;
         boolean remove = (data[0] & BUILD_MODIFIER_REMOVE) != 0;
         
         int sx = data[1], sy = data[2], sz = data[3];
         int ex = data[4], ey = data[5], ez = data[6];
         
         if (action == BUILD_ACTION_SET) {
             int id = data[7], meta = data[8];
             
             if (fill) {
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z)
                 if (getWorldId(world, x,y,z) == 0) 
                     setWorldIdMetaWithoutNotify(world, x,y,z, id,meta);
             } else if (remove) {
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z) {
                     int got = getWorldId(world, x,y,z);
                     if (got==id || (id==8 && got==9) || (id==10 && got==11)) 
                         setWorldIdMetaWithoutNotify(world, x,y,z, 0,0);
                 }
             } else {
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z)
                 setWorldIdMetaWithoutNotify(world, x,y,z, id,meta);
             }
             markWorldNeedsUpdate(world, sx-1,sy-1,sz-1, ex+1,ey+1,ez+1);
         } else if (action == BUILD_ACTION_COPY) {
             buildMark = 0;
             buildSizeX = 1 + ex - sx;
             buildSizeY = 1 + ey - sy;
             buildSizeZ = 1 + ez - sz;
             int size = buildSizeX * buildSizeY * buildSizeZ, at = 0;
             buildBufBlock = new int[size];
             buildBufExtra = new int[size];
             buildBufNBT = new NBTTagCompound[size];
             for (int x = sx; x <= ex; ++x)
             for (int y = sy; y <= ey; ++y)
             for (int z = sz; z <= ez; ++z) {
                 buildBufBlock[at] = getWorldId(world, x,y,z);
                 buildBufExtra[at] = getWorldMeta(world, x,y,z);
                 buildBufNBT[at] = getTileEntityCopy(getWorldTileEntity(world, x,y,z));
                 at++;
             }
         }
         if (action == BUILD_ACTION_PASTE && buildBufBlock != null) {
             int wx = 1 + ex - sx; wx = (wx > buildSizeX) ? wx % buildSizeX : 0;
             int wy = 1 + ey - sy; wy = (wy > buildSizeY) ? wy % buildSizeY : 0;
             int wz = 1 + ez - sz; wz = (wz > buildSizeZ) ? wz % buildSizeZ : 0;
             if (wx != 0 || wy != 0 || wz != 0) {
                 ex -= wx; ey -= wy; ez -= wz;
                 // assert : (1+ex-sx) % buildSizeX == 0 || (1+ex-sx) < buildSizeX
             }
             if (fill) { // fill space
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z)
                 if (getWorldId(world, x,y,z) == 0) {
                     int cx = (x-sx) % buildSizeX, cy = (y-sy) % buildSizeY, cz = (z-sz)% buildSizeZ;
                     int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                     setWorldIdMetaWithoutNotify(world, x,y,z, buildBufBlock[at],buildBufExtra[at]);
                 }
             } else if (remove) { // remove matching
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z) {
                     int cx = (x-sx) % buildSizeX, cy = (y-sy) % buildSizeY, cz = (z-sz)% buildSizeZ;
                     int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                     int id = buildBufBlock[at], got = getWorldId(world, x,y,z);
                     if (id == got || (id==8 && got==9) || (id==10 && got==11)) 
                         setWorldIdMetaWithoutNotify(world, x,y,z, 0,0);
                 }
             } else { // replace
                 for (int x = sx; x <= ex; ++x)
                 for (int y = sy; y <= ey; ++y)
                 for (int z = sz; z <= ez; ++z) {
                     int cx = (x-sx) % buildSizeX, cy = (y-sy) % buildSizeY, cz = (z-sz)% buildSizeZ;
                     int at = (cx*buildSizeY + cy)*buildSizeZ + cz;
                     setWorldIdMetaWithoutNotify(world, x,y,z, buildBufBlock[at],buildBufExtra[at]);
                     if (buildBufNBT[at] != null) {
                         setTileEntityFromCopy(getWorldTileEntity(world, x,y,z), 
                                               setNBTPos(buildBufNBT[at], x,y,z));
                     }
                 }
             }
             markWorldNeedsUpdate(world, sx-1,sy-1,sz-1, ex+1,ey+1,ez+1);
         }
     }
     
     private static void buildOnServerTick(EntityPlayerMP ent) {
         buildLock.lock();
         try {
             if (!modBuildActive) return;
             World world = getEntityWorld(ent);
             if (!buildActions.isEmpty()) {
                 for (int[] action : buildActions) {
                     buildAction(world, action);
                 }
                 buildActions.clear();
             }
         } finally {
             buildLock.unlock();
         }
     }
     
     public static void drawModBuild(float x, float y, float z) {
         if (!modBuildActive || buildMark <= 0) return;
         // calculate selection box
         float sx = (float)buildSX - x - 0.1f, ex = (float)(buildMark==2 ? buildEX : buildSX) - x + 1.1f;
         float sy = (float)buildSY - y - 0.1f, ey = (float)(buildMark==2 ? buildEY : buildSY) - y + 1.1f;
         float sz = (float)buildSZ - z - 0.1f, ez = (float)(buildMark==2 ? buildEZ : buildSZ) - z + 1.1f;
         // change state
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         GL11.glDepthMask(false);
         GL11.glDisable(GL11.GL_CULL_FACE);
         // draw selection box sides
         if (buildMark == 2) {
             GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
             GL11.glColor4ub((byte)255,(byte)64,(byte)32,(byte)32);
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glVertex3f(sx,sy,sz); GL11.glVertex3f(sx,sy,ez); GL11.glVertex3f(sx,ey,ez); GL11.glVertex3f(sx,ey,sz);
                 GL11.glVertex3f(ex,sy,sz); GL11.glVertex3f(ex,sy,ez); GL11.glVertex3f(ex,ey,ez); GL11.glVertex3f(ex,ey,sz);
                 GL11.glVertex3f(sx,sy,sz); GL11.glVertex3f(sx,sy,ez); GL11.glVertex3f(ex,sy,ez); GL11.glVertex3f(ex,sy,sz);
                 GL11.glVertex3f(sx,ey,sz); GL11.glVertex3f(sx,ey,ez); GL11.glVertex3f(ex,ey,ez); GL11.glVertex3f(ex,ey,sz);
                 GL11.glVertex3f(sx,sy,sz); GL11.glVertex3f(sx,ey,sz); GL11.glVertex3f(ex,ey,sz); GL11.glVertex3f(ex,sy,sz);
                 GL11.glVertex3f(sx,sy,ez); GL11.glVertex3f(sx,ey,ez); GL11.glVertex3f(ex,ey,ez); GL11.glVertex3f(ex,sy,ez);
             GL11.glEnd();
             GL11.glDisable(GL11.GL_BLEND);
         }
         // draw selection box
         GL11.glColor3ub((byte)32,(byte)64,(byte)255);
         GL11.glBegin(GL11.GL_LINES);
             GL11.glVertex3f(sx,sy,sz); GL11.glVertex3f(sx,sy,ez);
             GL11.glVertex3f(sx,sy,ez); GL11.glVertex3f(sx,ey,ez);
             GL11.glVertex3f(sx,ey,ez); GL11.glVertex3f(sx,ey,sz);
             GL11.glVertex3f(sx,ey,sz); GL11.glVertex3f(sx,sy,sz);
 
             GL11.glVertex3f(ex,sy,sz); GL11.glVertex3f(ex,sy,ez);
             GL11.glVertex3f(ex,sy,ez); GL11.glVertex3f(ex,ey,ez);
             GL11.glVertex3f(ex,ey,ez); GL11.glVertex3f(ex,ey,sz);
             GL11.glVertex3f(ex,ey,sz); GL11.glVertex3f(ex,sy,sz);
 
             GL11.glVertex3f(sx,sy,sz); GL11.glVertex3f(ex,sy,sz);
             GL11.glVertex3f(sx,sy,ez); GL11.glVertex3f(ex,sy,ez);
             GL11.glVertex3f(sx,ey,ez); GL11.glVertex3f(ex,ey,ez);
             GL11.glVertex3f(sx,ey,sz); GL11.glVertex3f(ex,ey,sz);
         GL11.glEnd();
         // restore state
         GL11.glEnable(GL11.GL_CULL_FACE);
         GL11.glDepthMask(true);
         GL11.glEnable(GL11.GL_TEXTURE_2D);
     }
     
     private static String textModBuild(String txt) {
         if (!modBuildActive || !optBuild || tagBuildEnabled.length()==0) return txt;
         return txt + tagBuildEnabled + " ";
     }
     
     
     //=ZMod=Compass===========================================================
     private static boolean modCompassEnabled, modCompassActive;
     private static String tagCompassAlternate;
     private static int keyCompassSet, keyCompassToggle;
     private static boolean optCompassShowPos;
     private static boolean compassHaveOrig, compassHaveMine, compassShowOrig = true;
     private static int compassOX, compassOY, compassOZ, compassMX, compassMY, compassMZ;
 
     private static boolean initModCompass() {
         if (!checkStatus(MOD_COMPASS, "compass")) return false;
         log("info: loading config for \"compass\"");
         tagCompassAlternate = getString("tagCompassAlternate", "altSpawn");
         return modCompassActive = true;
     }
     
     private static void quitModCompass() {
         if (!modCompassActive) return;
         modCompassActive = false;
     }
     
     private static void optionsModCompass() {
         boolean wasEnabled = modCompassEnabled;
         modCompassEnabled = getSetBool(modCompassEnabled, "modCompassEnabled", false,     "Enable Compass mod");
         if (wasEnabled != modCompassEnabled) {
             if (modCompassEnabled && !modCompassActive) initModCompass(); 
             if (!modCompassEnabled && modCompassActive) quitModCompass();
         }
         keyCompassSet = getSetBind(keyCompassSet, "keyCompassSet",              Keyboard.KEY_INSERT, "Set alternate compasspoint");
         keyCompassToggle = getSetBind(keyCompassToggle, "keyCompassToggle",        Keyboard.KEY_HOME, "Toggle compasspont original/alternate");
         optCompassShowPos = getSetBool(optCompassShowPos, "optCompassShowPos", true, "Show coordinates");
     }
     
     private static void updateModCompass() {
         if (!modCompassActive || isHell) return;
         if (isWorldChange) { compassHaveMine = false; compassShowOrig = true; }
         int cX = world.getSpawnX(), cY = world.getSpawnY(), cZ = world.getSpawnZ();
         int pX = fix(posX), pY = fix(posY), pZ = fix(posZ);
         if (!compassHaveOrig || ((cX!=compassOX || cY!=compassOY || cZ!=compassOZ) && (cX!=compassMX || cY!=compassMY || cZ!=compassMZ))) {
             compassOX = cX; compassOY = cY; compassOZ = cZ; compassHaveOrig = true;
         }
         if (!isMenu) {
             if (keyPress(keyCompassToggle)) compassShowOrig = !compassShowOrig;
             if (keyPress(keyCompassSet)) {
                 compassMX = pX; compassMY = pY; compassMZ = pZ;
                 compassHaveMine = true;
                 compassShowOrig = false;
             }
         }
         if (compassShowOrig) { cX = compassOX; cY = compassOY; cZ = compassOZ; } else { cX = compassMX; cY = compassMY; cZ = compassMZ; }
         world.setSpawnPosition(cX, cY, cZ);
     }
     
     private static String getAngleName(float grad) {
         grad -= 22.5f + 90.0f;
         while (grad > 360f) grad -= 360f;
         while (grad < 0f) grad += 360f;
         if (grad <  45f) return "NW";
         if (grad <  90f) return "N";
         if (grad < 135f) return "NE";
         if (grad < 180f) return "E";
         if (grad < 225f) return "SE";
         if (grad < 270f) return "S";
         if (grad < 315f) return "SW";
         return "W";
     }
     
     private static String textModCompassShared(String txt) {
         if ((modCompassActive && optCompassShowPos) || (modInfoActive && optInfoShowPos)) txt += "(" + fix(posX) + "," + fix(posY) + "," + fix(posZ) + " \u00a77" + getAngleName(player.rotationYaw) + "\u00a7f) ";
         if (modCompassActive && !compassShowOrig && tagCompassAlternate.length()>0) txt += tagCompassAlternate + " ";
         return txt;
     }
     
     
     //=ZMod=Sun===============================================================
     private static boolean modSunEnabled, modSunActive;
     private static String tagSunTime;
     private static String optSunServerCmd;
     private static int keySunTimeAdd, keySunTimeSub, keySunStop, keySunTimeNormal, keySunServer;
     private static int optSunTimeStep;
     private static boolean optSunServerCmdPlus;
     private static boolean sunTimeStop, sunSleeping;
     private static long sunTimeOffset, sunTimeMoment;
 
     private static boolean initModSun() {
         if (!checkStatus(MOD_SUN, "sun")) return false;
         log("info: loading config for \"sun\"");
         tagSunTime = getString("tagSunTime", "time");
         optSunServerCmd = getString("optSunServerCmd", "/time add");
         return modSunActive = true;
     }
     
     private static void quitModSun() {
         if (!modSunActive) return;
         modSunActive = false;
     }
 
     private static void optionsModSun() {
         boolean wasEnabled = modSunEnabled;
         modSunEnabled = getSetBool(modSunEnabled, "modSunEnabled", false,     "Enable Sun mod");
         if (wasEnabled != modSunEnabled) {
             if (modSunEnabled && !modSunActive) initModSun(); 
             if (!modSunEnabled && modSunActive) quitModSun();
         }
         keySunTimeAdd = getSetBind(keySunTimeAdd, "keySunTimeAdd",              Keyboard.KEY_ADD, "Add time");
         keySunTimeSub = getSetBind(keySunTimeSub, "keySunTimeSub",              Keyboard.KEY_SUBTRACT, "Subtract time");
         optSunTimeStep = getSetInt(optSunTimeStep / 20, "optSunTimeStep", 30, 1, 600, "Time step in seconds") * 20;
         keySunStop = getSetBind(keySunStop, "keySunStop",                    Keyboard.KEY_END, "Stop / resume sun-time");
         keySunTimeNormal = getSetBind(keySunTimeNormal, "keySunTimeNormal",        Keyboard.KEY_EQUALS, "Restore time");
         keySunServer = getSetBind(keySunServer, "keySunServer",                Keyboard.KEY_LSHIFT, "Modifier to change real time (SSP/SMP)");
         optSunServerCmdPlus = getSetBool(optSunServerCmdPlus, "optSunServerCmdPlus", false, "Use '+' for adding time in SMP");
     }
 
     private static void updateModSun() {
         if (!modSunActive) return;
         long time = getTime();
         if (getIsSleeping(player)) sunSleeping = true;
         else if (sunSleeping) { sunSleeping = false; sunTimeOffset = 0; }
         if (!isMenu) {
             if (keyDown(keySunServer)) {
                 if (isMultiplayer) {
                     if (keyPress(keySunTimeAdd)) sendChat(optSunServerCmd+(optSunServerCmdPlus ? " +" : " ")+optSunTimeStep);
                     else if (keyPress(keySunTimeSub)) sendChat(optSunServerCmd+" -"+optSunTimeStep);
                 } else {
                     if (keyPress(keySunTimeAdd)) setTime(getTime() + optSunTimeStep);
                     else if (keyPress(keySunTimeSub)) setTime(getTime() - optSunTimeStep);
                 }
             } else {
                 if (keyPress(keySunTimeAdd)) { if (sunTimeStop) sunTimeMoment += optSunTimeStep; sunTimeOffset += optSunTimeStep; }
                 else if (keyPress(keySunTimeSub)) { if (sunTimeStop) sunTimeMoment -= optSunTimeStep; sunTimeOffset -= optSunTimeStep; }
             }
             if (keyPress(keySunStop)) { sunTimeStop = !sunTimeStop; if (sunTimeStop) sunTimeMoment = time; }
             if (keyPress(keySunTimeNormal)) { sunTimeStop = false; sunTimeOffset = 0; }
         }
         if (sunTimeStop) { sunTimeOffset -= time - sunTimeMoment; sunTimeMoment = time; }
     }
     
     private static String textModSun(String txt) {
         if (!modSunActive || sunTimeOffset==0) return txt;
         return txt + tagSunTime + (sunTimeOffset<0 ? "" : "+") + (sunTimeOffset/20) + " ";
     }
     
     public static long sunOffsetHandle() { return modSunActive && sunTimeOffset!=0 ? sunTimeOffset : 0; }
     
     
     //=ZMod=Fly===============================================================
     private static boolean modFlyEnabled, modFlyActive, modFlyAllowed, modNoClipAllowed;
     private static String tagFly, tagFlyNoClip;
     private static int keyFlyOn, keyFlyOff, keyFlyUp, keyFlyDown, keyFlySpeed, keyFlyToggle, keyFlyRun, keyFlyNoClip;
     private static double optFlySpeedVertical, optFlySpeedMulNormal, optFlySpeedMulModifier, optFlyRunSpeedMul, optFlyRunSpeedVMul;
     private static double optFlyJump, optFlyJumpHigh;
     private static boolean optFlySpeedIsToggle, optFlyRunSpeedIsToggle, optFlyNoClip, optFlyVanillaFly, optFlyVanillaSprint;
     private static boolean fly, flySpeed, flyRun, flyNoClip, flyUp, flyDown;
     private static boolean flyProjection, flySpeedProjection, flyRunProjection, flyNoClipProjection, flyUpProjection, flyDownProjection;
     private static boolean playerClassActive;
     private static boolean flew = false, moveOnGround;
 
     private static boolean initModFly() {
         if (!checkStatus(MOD_FLY, "fly")) return false;
         log("info: loading config for \"fly\"");
         keyFlyOn = getBind("keyFlyOn",                        Keyboard.KEY_NONE);
         keyFlyOff = getBind("keyFlyOff",                      Keyboard.KEY_NONE);
         flyNoClip = false;
         tagFly = getString("tagFly", "flying");
         tagFlyNoClip = getString("tagFlyNoClip", "noclip");
         return modFlyActive = true;
     }
     
     private static void quitModFly() {
         if (!modFlyActive) return;
         modFlyActive = false;
     }
     
     private static void optionsModFly() {
         boolean wasEnabled = modFlyEnabled;
         modFlyEnabled = getSetBool(modFlyEnabled, "modFlyEnabled", false,     "Enable Fly mod");
         if (wasEnabled != modFlyEnabled) {
             if (modFlyEnabled && !modFlyActive) initModFly(); 
             if (!modFlyEnabled && modFlyActive) quitModFly();
         }
         keyFlyToggle            = getSetBind(keyFlyToggle,                   "keyFlyToggle", Keyboard.KEY_F           , "Toggle fly mode", FEATURE_FLY);
         optFlySpeedMulNormal    = getSetLog((float)optFlySpeedMulNormal,   "optFlySpeedMulNormal"    , 1.0f, 0.1f, 100.0f , "Flying speed");
         keyFlyUp                = getSetBind(keyFlyUp,                       "keyFlyUp",     Keyboard.KEY_E           , "Fly up");
         keyFlyDown              = getSetBind(keyFlyDown,                     "keyFlyDown",   Keyboard.KEY_Q           , "Fly down");
         optFlySpeedVertical     = getSetLog((float)optFlySpeedVertical,    "optFlySpeedVertical"     , 0.2f, 0.1f, 10.0f  , "Vertical flying speed");
         keyFlySpeed             = getSetBind(keyFlySpeed,                    "keyFlySpeed",  Keyboard.KEY_LSHIFT      , "Fly speed modifier");
         optFlySpeedIsToggle     = getSetBool(optFlySpeedIsToggle,            "optFlySpeedIsToggle"   , false, "Fly speed modifier is toggle");
         optFlySpeedMulModifier  = getSetLog((float)optFlySpeedMulModifier, "optFlySpeedMulModifier"  , 2.0f, 1.0f, 100.0f , "Flying speed with speed modifier");
         keyFlyRun               = getSetBind(keyFlyRun,                      "keyFlyRun",    Keyboard.KEY_LSHIFT      , "Running speed modifier");
         optFlyRunSpeedIsToggle  = getSetBool(optFlyRunSpeedIsToggle,         "optFlyRunSpeedIsToggle", false, "Run speed modifier is toggle");
         optFlyRunSpeedMul       = getSetLog((float)optFlyRunSpeedMul,      "optFlyRunSpeedMul"       , 1.5f, 0.1f, 100.0f , "Running speed");
         optFlyRunSpeedVMul      = getSetLog((float)optFlyRunSpeedVMul,     "optFlyRunSpeedVMul"      , 1.5f, 0.1f, 100.0f , "Vertical speed (ladders / water)");
         keyFlyNoClip            = getSetBind(keyFlyNoClip,                   "keyFlyNoClip", Keyboard.KEY_F8          , "Toggle no-clip mode", FEATURE_NOCLIP);
         optFlyNoClip            = getSetBool(optFlyNoClip,                   "optFlyNoClip"          , true , "No-clip is enabled by default");
         optFlyJump              = getSetLog((float)optFlyJump,             "optFlyJump"              , 1.0f, 1.0f, 100.0f , "Jump speed");
         optFlyJumpHigh          = getSetLog((float)optFlyJumpHigh,         "optFlyJumpHigh"          , 1.25f, 1.0f, 100.0f, "Jump speed with speed modifier (run)");
         optFlyVanillaFly        = getSetBool(optFlyVanillaFly,               "optFlyVanillaFly", true, "Allow vanilla MC fly toggle");
         optFlyVanillaSprint     = getSetBool(optFlyVanillaSprint,            "optFlyVanillaSprint", true, "Allow vanilla MC sprint toggle");
     }
 
     private static void updateModFly() {
         if (isWorldChange) {
             flyNoClip = optFlyNoClip && modFlyActive && modNoClipAllowed;
             setNoClip(modFlyAllowed && flyNoClip && fly);
         }
         if (!modFlyActive || isMenu) return;
         if (isControllingProjection()) {
             boolean flyPrev = flyProjection;
             if (keyPress(keyFlyToggle)) flyProjection = !flyProjection;
             else if (keyDown(keyFlyOn)) flyProjection = true;
             else if (keyDown(keyFlyOff)) flyProjection = false;
             if (flyPrev != flyProjection) {
                 cheatProjection.capabilities.isFlying = flyProjection;
             }
             if (flyProjection && keyPress(keyFlyNoClip)) cheatProjection.noClip = flyNoClipProjection = !flyNoClipProjection;
             else if (flyPrev != flyProjection) cheatProjection.noClip = flyProjection && flyNoClipProjection;
             if (!optFlySpeedIsToggle) flySpeedProjection = keyDown(keyFlySpeed);
             else if (keyPress(keyFlySpeed)) flySpeedProjection = !flySpeedProjection;
             if (!optFlyRunSpeedIsToggle) flyRunProjection = keyDown(keyFlyRun);
             else if (keyPress(keyFlyRun)) flyRunProjection = !flyRunProjection;
             flyUpProjection = keyDown(keyFlyUp);
             flyDownProjection = keyDown(keyFlyDown);
         } else {
             if (!optFlySpeedIsToggle) flySpeedProjection = false;
             if (!optFlyRunSpeedIsToggle) flyRunProjection = false;
             flyUpProjection = false;
             flyDownProjection = false;
         }
         if (isControllingPlayer()) {
             boolean flyPrev = fly;
             if (keyPress(keyFlyToggle)) fly = !fly;
             else if (keyDown(keyFlyOn)) fly = true;
             else if (keyDown(keyFlyOff)) fly = false;
             if (!modFlyAllowed && fly) {
                 fly = false;
                 chatClient("\u00a74zombe's \u00a72fly\u00a74-mod is not allowed on this server.");
             }
             if (flyPrev != fly) {
                 player.capabilities.isFlying = fly;
                 if (player.capabilities.allowFlying) player.sendPlayerAbilities();
             }
             if (fly && keyPress(keyFlyNoClip)) setNoClip(flyNoClip = !flyNoClip);
             else if (flyPrev != fly) setNoClip(fly && flyNoClip);
             if (!optFlySpeedIsToggle) flySpeed = keyDown(keyFlySpeed);
             else if (keyPress(keyFlySpeed)) flySpeed = !flySpeed;
             if (!optFlyRunSpeedIsToggle) flyRun = keyDown(keyFlyRun);
             else if (keyPress(keyFlyRun)) flyRun = !flyRun;
             flyUp = keyDown(keyFlyUp);
             flyDown = keyDown(keyFlyDown);
         } else {
             if (!optFlySpeedIsToggle) flySpeed = false;
             if (!optFlyRunSpeedIsToggle) flyRun = false;
             flyUp = false;
             flyDown = false;
         }
     }
 
     private static String textModFly(String txt) {
         if (!modFlyActive || !fly) return txt;
         if (tagFly.length()>0) txt += tagFly + " ";
         if (flyNoClip && tagFlyNoClip.length()>0) txt += tagFlyNoClip + " ";
         return txt;
     }
 
     public static boolean playerOnGround() {
         return (playerClassActive && player != null) ? moveOnGround : (player != null) ? getEntityOnGround(player) : true;
     }
 
     private static void flyOnServerPlayerUpdate(EntityPlayer ent) {
         if (!modFlyActive || !modFlyAllowed) return;
         ent.noClip = player.noClip;                               // necessary for noclip
         if (!player.capabilities.allowFlying)
         ent.capabilities.isFlying = player.capabilities.isFlying; // necessary for damage-over-lava bug
     }
 
     private static void flyOnClientPlayerUpdate(EntityPlayer ent) {
         if (!playerClassActive || player == null || (modFlyActive && modFlyAllowed && fly)) return;
         setEntityOnGround(player, moveOnGround); // no sure if it has any use
     }
 
     public static boolean allowVanillaFly() {
         return !modFlyActive || !modFlyAllowed || optFlyVanillaFly; 
     }
 
     public static boolean allowVanillaSprint() {
         return !modFlyActive || !modFlyAllowed || optFlyVanillaSprint; 
     }
 
     public static boolean isFly() {
         return fly;
     }
 
     public static void setFly(boolean fly) {
         ZMod.fly = fly;
     }
 
     public static void flyHandle(EntityPlayer ent, double mx, double my, double mz) {
         float flyTmp = 0;
         if (ent == player) {
             if (modFlyAllowed && modFlyActive) {
                 flyTmp = getEntitySteps(ent);
                 if (fly) {
                     player.movementInput.sneak = false;
                     my = 0d;
                     if (!isMenu) {
                         if (flyUp) my += optFlySpeedVertical;
                         if (flyDown) my -= optFlySpeedVertical;
                         double mul = flySpeed ? optFlySpeedMulModifier : optFlySpeedMulNormal;
                         mx*=mul; my*=mul; mz*=mul;
                         setFall(ent, 0f); setEntityMotionY(ent, 0f); flew = true;
                     }
                 } else if (flyRun) {
                     mx *= optFlyRunSpeedMul;
                     mz *= optFlyRunSpeedMul;
                     int id = mapXGetId(fix(getEntityPosX(ent)), fix(getEntityPosY(ent)), fix(getEntityPosZ(ent)));
                     if (id == 65 || (id >= 8 && id <= 11)) my *= optFlyRunSpeedVMul;
                 }
             }
         }
         if (ent == cheatProjection) {
             if (modFlyAllowed && modFlyActive) {
                 flyTmp = getEntitySteps(ent);
                 if (flyProjection) {
                     cheatProjection.movementInput.sneak = false;
                     my = 0d;
                     if (!isMenu) {
                         if (flyUpProjection) my += optFlySpeedVertical;
                         if (flyDownProjection) my -= optFlySpeedVertical;
                         double mul = flySpeedProjection ? optFlySpeedMulModifier : optFlySpeedMulNormal;
                         mx*=mul; my*=mul; mz*=mul;
                         setFall(ent, 0f); setEntityMotionY(ent, 0f);
                     }
                 } else if (flyRunProjection) {
                     mx *= optFlyRunSpeedMul;
                     mz *= optFlyRunSpeedMul;
                     int id = mapXGetId(fix(getEntityPosX(ent)), fix(getEntityPosY(ent)), fix(getEntityPosZ(ent)));
                     if (id == 65 || (id >= 8 && id <= 11)) my *= optFlyRunSpeedVMul;
                 }
             }
         }
         /*
         if (player != null && ent instanceof EntityPlayerMP && player.getEntityName().equals(ent.getEntityName())) {
             ent.noClip = player.noClip;
         }
         */
         flyCallSuper(ent, mx, my, mz);
         if (ent == player) {
             moveOnGround = getEntityOnGround(ent);
             playerClassActive = true;
 
             if (modFlyAllowed && modFlyActive) {
                 if (fly) {
                     player.movementInput.sneak = false;
                     setFall(ent, 0f); setEntityOnGround(ent, true); setEntitySteps(ent, flyTmp); 
                     ent.capabilities.isFlying = true;
                     if (ent.capabilities.allowFlying)
                     ent.sendPlayerAbilities();
                 } else if (flew && !getEntityOnGround(ent)) {
                     setFall(ent, 0f); setEntityOnGround(ent, true);
                     if (ent.capabilities.allowFlying)
                     ent.sendPlayerAbilities();
                 }
                 else flew = false;
             }
 
             if (cheating && !optCheatFallDamage) { setFall(ent, 0f); setEntityOnGround(ent, true); }
         }
         if (ent == cheatProjection) {
             playerClassActive = true;
 
             if (modFlyAllowed && modFlyActive) {
                 if (flyProjection) {
                     cheatProjection.movementInput.sneak = false;
                     setEntitySteps(ent, flyTmp);
                     ent.capabilities.isFlying = true;
                 }
             }
             setFall(ent, 0f);
             setEntityOnGround(ent, true);
         }
     }
 
     public static double flyJumpHandle(EntityPlayer ent) {
         if (!modFlyAllowed || !modFlyActive) return 1.0D;
         if (ent == player) return flyRun ? optFlyJumpHigh : optFlyJump;
         if (ent == cheatProjection) return flyRunProjection ? optFlyJumpHigh : optFlyJump;
         return 1.0D;
     }
 
     //=ZMod=Craft=============================================================
     private static boolean modCraftEnabled, modCraftActive;
     private static int keyCraftAll;
 
     private static boolean initModCraft() {
         if (!checkStatus(MOD_CRAFT, "craft")) return false;
         log("info: loading config for \"craft\"");
         return modCraftActive = true;
     }
     
     private static void quitModCraft() {
         if (!modCraftActive) return;
         modCraftActive = false;
     }
 
     private static void optionsModCraft() {
         boolean wasEnabled = modCraftEnabled;
         modCraftEnabled = getSetBool(modCraftEnabled, "modCraftEnabled", false,     "Enable Craft mod");
         if (wasEnabled != modCraftEnabled) {
             if (modCraftEnabled && !modCraftActive) initModCraft(); 
             if (!modCraftEnabled && modCraftActive) quitModCraft();
         }
         keyCraftAll = getSetBind(keyCraftAll, "keyCraftAll", Keyboard.KEY_LSHIFT, "Craft-all modifier key");
     }
 
     public static int craftingHandle() {
         return modCraftActive && keyDown(keyCraftAll) ? 64 : 1;
     }
     
 
     //=ZMod=Path==============================================================
     private static boolean modPathEnabled, modPathActive;
     private static int keyPathShow, keyPathDelete;
     private static boolean optPathShow;
     private static int optPathPoints, optPathSpacing;
     private static float optPathMin, optPathAnimSpeed;
     private static Mark optPathColor;
     private static int pathCount, pathLast;
     private static float pathAnimCur, pathf[];
 
     private static boolean initModPath() {
         if (!checkStatus(MOD_PATH, "path")) return false;
         log("info: loading config for \"path\"");
         optPathPoints = getInt("optPathPoints", 8192, 256, 32768); pathf = new float[3 * optPathPoints];
         optPathMin = getFloat("optPathMin", 0.25f, 0.1f, 4f); optPathMin *= optPathMin;
         optPathColor = getColor("optPathColor", 0xff0000);
         return modPathActive = true;
     }
     
     private static void quitModPath() {
         if (!modPathActive) return;
         modPathActive = false;
     }
     
     private static void optionsModPath() {
         boolean wasEnabled = modPathEnabled;
         modPathEnabled = getSetBool(modPathEnabled, "modPathEnabled", false,     "Enable Path mod");
         if (wasEnabled != modPathEnabled) {
             if (modPathEnabled && !modPathActive) initModPath(); 
             if (!modPathEnabled && modPathActive) quitModPath();
         }
         keyPathShow = getSetBind(keyPathShow, "keyPathShow",                  Keyboard.KEY_BACK, "Show / hide path");
         optPathShow = getSetBool(optPathShow, "optPathShow", false, "Path is shown by default");
         optPathSpacing = getSetInt(optPathSpacing - 2, "optPathSpacing", 6, 0, 32, "Spacing") + 2;
         optPathAnimSpeed = getSetFloat(optPathAnimSpeed, "optPathAnimSpeed", 8f, 0f, 32f, "Animation speed");
         keyPathDelete = getSetBind(keyPathDelete, "keyPathDelete",              Keyboard.KEY_DELETE, "Delete path");
     }
 
     private static void updateModPath() {
         if (!modPathActive || isMenu) return;
         if (keyPress(keyPathShow)) optPathShow = !optPathShow;
         if (keyPress(keyPathDelete)) pathCount = 0;
     }
     
     public static void drawModPath(float x, float y, float z) {
         if (!modPathActive) return;
         // get previous location
         float px = (float)posX, py = (float)posY, pz = (float)posZ;
         float tx = pathf[pathLast] - px, ty = pathf[pathLast+1] - (py - 1.25f), tz = pathf[pathLast+2] - pz;
         float dist = tx*tx + ty*ty + tz*tz;
         // do we have a new pathpoint
         if (dist > optPathMin) {
             pathLast += 3; if (pathLast >= pathf.length) pathLast = 0;
             if (pathCount < optPathPoints) pathCount++;
             pathf[pathLast] = px;
             pathf[pathLast+1] = py - 1.25f;
             pathf[pathLast+2] = pz;
         }
         // draw the path?
         if (optPathShow && pathCount>3) {
             pathAnimCur += seconds * optPathAnimSpeed;
             if (pathAnimCur > optPathSpacing) pathAnimCur -= optPathSpacing;
             float x1 = pathf[pathLast] - x, y1 = pathf[pathLast+1] - y, z1 = pathf[pathLast+2] - z, x2, y2, z2;
             int cnt = pathCount-1, at = pathLast, anim = ((pathf.length - pathLast) / 3 + (int)pathAnimCur) % optPathSpacing;
             int skip = 4;
             GL11.glDisable(GL11.GL_TEXTURE_2D);
             GL11.glEnable(GL11.GL_DEPTH_TEST);
             GL11.glDisable(GL11.GL_BLEND);
             GL11.glDisable(GL11.GL_FOG);
             GL11.glColor3ub(optPathColor.r,optPathColor.g,optPathColor.b);
             GL11.glBegin(GL11.GL_LINES);
             do {
                 x2 = x1; y2 = y1; z2 = z1;
                 at -= 3; if (at<0) at = pathf.length - 3;
                 x1 = pathf[at] - x; y1 = pathf[at+1] - y; z1 = pathf[at+2] - z;
                 if (optPathSpacing > 2) {
                     if (++anim == optPathSpacing) anim = 0;
                     if (anim <= 1 && skip < 0) { GL11.glVertex3f(x1,y1,z1); GL11.glVertex3f(x2,y2,z2); }
                 } else if (skip < 0) { GL11.glVertex3f(x1,y1,z1); GL11.glVertex3f(x2,y2,z2); }
                 skip--;
             } while ((--cnt) != 0);
             GL11.glEnd();
         }
     }
     
     
     //=ZMod=Recipe============================================================
     private static boolean modRecipeEnabled, modRecipeActive;
     private static boolean optRecipeShowId, optRecipeDump, optRecipeVanillaMP, optRecipeShowHelp;
     private static List recipesSP, recipesMP;
     private static int recipesMobType;
     private static IRecipe selRecipe;
     
     private static boolean initModRecipe() {
         if (!checkStatus(MOD_RECIPE, "recipe")) return false;
         log("info: loading config for \"recipe\" - deferred");
         optRecipeDump = getBool("optRecipeDump", false);
         optRecipeVanillaMP = getBool("optRecipeVanillaMP", false);
         if (optRecipeVanillaMP) recipesMP = (List)((ArrayList)getValue(fCMRecipes, getCManager())).clone(); // separate copy for MP
         else recipesMP = (List)getValue(fCMRecipes, getCManager()); // no separate copy - use the same for both (default to support server recipes).
         return modRecipeActive = true;
     }
     
     private static void quitModRecipe() {
         if (!modRecipeActive) return;
         modRecipeActive = false;
     }
     
     private static void optionsModRecipe() {
         boolean wasEnabled = modRecipeEnabled;
         modRecipeEnabled = getSetBool(modRecipeEnabled, "modRecipeEnabled", false,     "Enable Recipe mod");
         if (wasEnabled != modRecipeEnabled) {
             if (modRecipeEnabled && !modRecipeActive) initModRecipe(); 
             if (!modRecipeEnabled && modRecipeActive) quitModRecipe();
         }
         optRecipeShowId = getSetBool(optRecipeShowId, "optRecipeShowId", true, "Show selected item id");
         optRecipeShowHelp = getSetBool(optRecipeShowHelp, "optRecipeShowHelp", true, "Show recipe helper");
     }
     
     private static void deferredModRecipe() {
         if (!modRecipeActive || recipesSP != null) return;
         log("info: continuing to load \"recipes\"");
         recipesSP = (List)getValue(fCMRecipes, getCManager());
         log("info: "+recipesSP.size()+" SP recipes before loading mod");
         parse(recipesSP, "recipes.txt",    RECIPES); sortRecipes(recipesSP);
         log("info: "+recipesSP.size()+" SP recipes after loading mod");
         if (optRecipeDump) {
             log("==== recipe dump ====");
             String res;
             for (int recipeNr=0;recipeNr<recipesSP.size();recipeNr++) {
                 Object obj = recipesSP.get(recipeNr);
                 if (obj instanceof ShapedRecipes) {
                     ItemStack items = (ItemStack)getValue(fRResA, obj);
                     int itemId = getItemsId(items), meta = getItemsInfo(items);
                     ItemStack arr[] = (ItemStack[])getValue(fRMap, obj);
                     res = getNameForId(itemId);
                     if (itemId>0 || getItemHasSubTypes(getItem(itemId)) || getItemDmgCap(getItem(itemId))>0) res += "/"+meta;
                     res += " " + getItemsCount(items) + " " + getValue(fRWidth, obj) + " " + getValue(fRHeight, obj);
                     for (int ingredientNr=0;ingredientNr<arr.length;ingredientNr++) {
                         if (arr[ingredientNr]==null) { res += " 0"; continue; }
                         itemId = getItemsId(arr[ingredientNr]); meta = getItemsInfo(arr[ingredientNr]);
                         res += " "+getNameForId(itemId);
                         if (itemId>0 || getItemHasSubTypes(getItem(itemId)) || getItemDmgCap(getItem(itemId))>0) res += "/"+meta;
                     }
                 } else if (obj instanceof ShapelessRecipes) {
                     ItemStack items = (ItemStack)getValue(fRResB, obj);
                     int itemId = getItemsId(items), meta = getItemsInfo(items);
                     List arr = (List)getValue(fRList, obj);
                     res = getNameForId(itemId);
                     if (itemId>0 || getItemHasSubTypes(getItem(itemId)) || getItemDmgCap(getItem(itemId))>0) res += "/"+meta;
                     res += " " + getItemsCount(items) + " " + arr.size() + " 0";
                     for (int ingredientNr=0;ingredientNr<arr.size();ingredientNr++) {
                         items = (ItemStack)arr.get(ingredientNr);
                         itemId = getItemsId(items); meta = getItemsInfo(items);
                         res += " "+getNameForId(itemId);
                         if (itemId>0 || getItemHasSubTypes(getItem(itemId)) || getItemDmgCap(getItem(itemId))>0) res += "/"+meta;
                     }
                 } else res = "Unknown type";
                 log(res);
             }
         }
     }
     
     private static void updateModRecipe() {
         if (!modRecipeActive) return;
         setValue(fCMRecipes, getCManager(), isMultiplayer ? recipesMP : recipesSP);
     }
 
     private static void updateModRecipeShared() { // update mob-type for spawner block
         recipesMobType = 0;
         if (isMultiplayer || (!modRecipeActive && !modBuildActive)) return;
         ItemStack items = invItemsArr[getInvCur()];
         if (items != null && getItemsId(items)==52) {
             int meta = getItemsInfo(items);
             if (meta >= 0 && meta < MAXTYPE && meta != PLAYER && meta != LIVING) recipesMobType = meta;
         }
     }
     private static void drawGuiModRecipe() {
         // draw "recipe"
         if (modRecipeActive && optRecipeShowHelp && isMenu && !isTMIEnabled()) {
             ArrayList recipes = new ArrayList();
             ArrayList make = new ArrayList();
             ItemStack items[] = new ItemStack[9], unique[] = new ItemStack[9];
             IRecipe showRecipe = null, defaultRecipe = null;
             boolean match[] = new boolean[9];
             // build list from the shit on crafting grid
             int last = 0, count, uniques = 0;
             Next: for (int i=0;i<9;i++) {
                 ItemStack obj = getGridItem(i);
                 if (obj != null) {
                     items[last++] = obj;
                     for (int j=0;j<uniques;j++) if (isItemsMatch(items[last-1], unique[j])) continue Next;
                     unique[uniques++] = obj;
                 }
             }
             // is there at least something on the table
             if (last > 0) {
                 // dimensions / sizes
                 int sizeX = 176, sizeY = 166; // hardcoded inv screen size => assuming it does not change
                 int scrW = getScrWidthS(), scrH = getScrHeightS();
                 int ofs = 2, ofsX = ofs + sizeX + (scrW - sizeX) / 2, ofsY = 2 + (scrH - sizeY) / 2, ofsY2 = ofsY + 24;
                 int row = (scrW - sizeX - 4) / (2 * 16);
                 int nr;
                 // search the recipe list for matches
                 List search = (List)getValue(fCMRecipes, getCManager());
                 Iterator it = search.iterator();
                 // check every recipe
                 while (it.hasNext()) {
                     IRecipe recipe = (IRecipe)it.next();
                     count = last;
                     for (int i=0;i<last;i++) match[i] = false;
                     // does this recipe make the first item on grid?
                     ItemStack result = null;
                     // for normal recipe
                     if (recipe instanceof ShapedRecipes) {
                         ItemStack obj[] = (ItemStack[])getValue(fRMap, recipe);
                         result = (ItemStack)getValue(fRResA, recipe);
                         // for every recipe item
                         for (int i=0;i<obj.length;i++) if (obj[i] != null) {
                             // check every item on table for match
                             for (int j=0;j<last;j++) if (!match[j] && isItemsMatch(items[j], obj[i])) {
                                 match[j] = true;
                                 count--;
                                 break;
                             }
                         }
                     // for shapeless recipe
                     } else if (recipe instanceof ShapelessRecipes) {
                         List objs = (List)getValue(fRList, recipe);
                         result = (ItemStack)getValue(fRResB, recipe);
                         // for every recipe item
                         Iterator itIng = objs.iterator();
                         while (itIng.hasNext()) {
                             ItemStack obj = (ItemStack)itIng.next();
                             // check every item on table for match
                             for (int j=0;j<last;j++) if (!match[j] && isItemsMatch(items[j], obj)) {
                                 match[j] = true;
                                 count--;
                                 break;
                             }
                         }
                     } else continue;
                     if (count == 0) recipes.add(recipe); // everything on table is present in recipe (ignoring placement)
                     for (int i=0;i<uniques;i++) {
                         int id = getItemsId(unique[i]);
                         ItemStack uniqueX = getItemHasSubTypes(getItem(id)) ? unique[i] : newItems(id, 1); // remove damage
                         if (isItemsMatch(uniqueX, result)) make.add(recipe); // found how it is made
                     }
                 }
                 // did we get any results
                 if (recipes.size() > 0) {
                     if (recipes.size() == 1) {
                         showText("1 recipe needs", ofs, ofsY, 0xeeeeee);
                         showText("" + (last > 1 ? "those items" : "that item") + " :D", ofs, ofsY + 10, 0xeeeeee);
                     } else {
                         showText("" + recipes.size() + " recipes need", ofs, ofsY, 0xdddddd);
                         showText("" + (last > 1 ? "those items" : "that item") + " :)", ofs, ofsY + 10, 0xdddddd);
                     }
                 } else {
                     showText("Can not craft anything", ofs, ofsY, 0x882222);
                     showText("with " + (last > 1 ? "those items :/" : "that item :("), ofs, ofsY + 10, 0x882222);
                 }
                 ofsY2 = 40 + ofsY + ((recipes.size() + row - 1) / row) * 16;
                 if (make.size() > 0) showText("Recipes for:", ofs, ofsY2 - 10, 0xeeeeee);
                 // SET DRAWING STATE:
                 GL11.glEnable(GL11.GL_LIGHTING);
                 GL11.glPushMatrix(); GL11.glRotatef(120F, 1.0F, 0.0F, 0.0F); // light reorientation (why the hell is the light not placed in the right place to start with !?)
                 setXItemLighting();
                 GL11.glPopMatrix();
                 GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
                 GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                 // draw found recipe candidates
                 if (recipes.size() > 0) {
                     it = recipes.iterator();
                     nr = 0;
                     // show all potential results
                     while (it.hasNext()) {
                         IRecipe recipe = (IRecipe)it.next();
                         if (defaultRecipe == null) defaultRecipe = recipe;
                         ItemStack obj = null;
                         if (recipe instanceof ShapedRecipes) obj = (ItemStack)getValue(fRResA, recipe);
                         else if (recipe instanceof ShapelessRecipes) obj = (ItemStack)getValue(fRResB, recipe);
                         else continue; // ugh!? this is unlikely to ever happen
                         int x = ofs + (nr % row) * 16, y = 20 + ofsY + (nr / row) * 16;
                         if (mouseX>=x && mouseY>=y && mouseX<x+16 && mouseY<y+16) {
                             showRecipe = recipe;
                             if (Mouse.isButtonDown(0)) selRecipe = recipe;
                         }
                         drawItem(obj, x, y);
                         nr++;
                     }
                 }
                 // draw ingredient recipes
                 if (make.size() > 0) {
                     it = make.iterator();
                     nr = 0;
                     while (it.hasNext()) {
                         IRecipe recipe = (IRecipe)it.next();
                         if (defaultRecipe == null) defaultRecipe = recipe;
                         ItemStack obj = null;
                         if (recipe instanceof ShapedRecipes) obj = (ItemStack)getValue(fRResA, recipe);
                         else if (recipe instanceof ShapelessRecipes) obj = (ItemStack)getValue(fRResB, recipe);
                         else continue; // ugh!? this is unlikely to ever happen
                         int x = ofs + (nr % row) * 16, y = ofsY2 + (nr / row) * 16;
                         if (mouseX>=x && mouseY>=y && mouseX<x+16 && mouseY<y+16) {
                             showRecipe = recipe;
                             if (Mouse.isButtonDown(0)) selRecipe = recipe;
                         }
                         drawItem(obj, x, y);
                         nr++;
                     }
                 }
                 // show the selected recipe
                 String txtCount = null, txtRecipeType = null;
                 if (showRecipe == null) showRecipe = selRecipe;
                 if (showRecipe == null) showRecipe = defaultRecipe;
                 if (showRecipe != null) {
                     // show selected / only recipe
                     if (showRecipe instanceof ShapedRecipes) {
                         ItemStack obj[] = (ItemStack[])getValue(fRMap, showRecipe), res = (ItemStack)getValue(fRResA, showRecipe);
                         int w = (Integer)getValue(fRWidth, showRecipe), h = (Integer)getValue(fRHeight, showRecipe);
                         for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
                             int at = y*w + x;
                             if (at >= obj.length || obj[at] == null) continue;
                             drawItem(obj[at], ofsX + x * 16, 28 + ofsY + y * 16);
                         }
                         drawItem(res, ofsX, ofsY);
                         txtCount = "     x "+getItemsCount(res);
                         txtRecipeType = "Shaped recipe:";
                     } else if (showRecipe instanceof ShapelessRecipes) {
                         List objs = (List)getValue(fRList, showRecipe);
                         ItemStack res = (ItemStack)getValue(fRResB, showRecipe);
                         it = objs.iterator();
                         int x = 0, y = 0;
                         while (it.hasNext()) {
                             drawItem((ItemStack)it.next(), ofsX + x * 16, 28 + ofsY + y * 16);
                             x++;
                             if (x > 3) { x = 0; y++; }
                         }
                         drawItem(res, ofsX, ofsY);
                         txtCount = "     x "+getItemsCount(res);
                         txtRecipeType = "Shapeless recipe:";
                     }
                 }
                 // restore state
                 GL11.glDisable(GL11.GL_LIGHTING);
                 GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
                 // show some more text
                 if (txtCount != null) {
                     showText(txtCount, ofsX, ofsY + 6, 0xeeeeee);
                     showText(txtRecipeType, ofsX, ofsY + 18, 0xeeeeee);
                 }
             }
         } else selRecipe = null;
     }
     
     private static String textModRecipe(String txt) {
         if (!modRecipeActive || !optRecipeShowId) return txt;
         ItemStack items = invItemsArr[getInvCur()];
         if (items != null) {
             int id = getItemsId(items);
             txt += "id:" + (getItemHasSubTypes(getItem(id)) ? id + "/" + getItemsInfo(items) : id ) + " ";
         }
         return txt;
     }
     
     //=ZMod=Safe==============================================================
     private static boolean modSafeEnabled, modSafeActive;
     private static String tagSafe;
     private static int keySafeShow;
     private static int keySafeGhost;
     private static Mark optSafeDangerColor, optSafeDangerColorSun;
     private static boolean optSafeShowWithSun;
     private static int optSafeLookupRadius;
     private static final int safeMax = 2048;
     private static Mark safeMark[];
     private static boolean safeShow;
     private static boolean safeGhost;
     private static int safeCur, safeUpdate;
 
     private static boolean initModSafe() {
         if (!checkStatus(MOD_SAFE, "safe")) return false;
         log("info: loading config for \"safe\"");
         safeMark = new Mark[safeMax];
         safeCur = 0;
         safeUpdate = 0;
         safeShow = false;
         safeGhost = false;
         optSafeDangerColor = getColor("optSafeDangerColor", 0xff0000);
         optSafeDangerColorSun = getColor("optSafeDangerColorSun", 0xdddd00);
         tagSafe = getString("tagSafe", "safe");
         return modSafeActive = true;
     }
     
     private static void quitModSafe() {
         if (!modSafeActive) return;
         modSafeActive = false;
     }
 
     private static void optionsModSafe() {
         boolean wasEnabled = modSafeEnabled;
         modSafeEnabled = getSetBool(modSafeEnabled, "modSafeEnabled", false,     "Enable Safe mod");
         if (wasEnabled != modSafeEnabled) {
             if (modSafeEnabled && !modSafeActive) initModSafe(); 
             if (!modSafeEnabled && modSafeActive) quitModSafe();
         }
         keySafeShow = getSetBind(keySafeShow, "keySafeShow",                  Keyboard.KEY_L, "Show / hide un-safe markers");
         keySafeGhost = getSetBind(keySafeGhost, "keySafeGhost",                  Keyboard.KEY_K, "Toggle show marks through walls");
         optSafeShowWithSun = getSetBool(optSafeShowWithSun, "optSafeShowWithSun", true, "Mark 'safe at midday' differently");
         optSafeLookupRadius = getSetInt(optSafeLookupRadius, "optSafeLookupRadius", 16, 0, 64, "Un-safe lookup radius");
     }
 
     private static void updateModSafe() {
         if (!modSafeActive || isMenu) return;
         if (keyPress(keySafeShow)) safeShow = !safeShow;
         if (keyPress(keySafeGhost)) safeGhost = !safeGhost;
     }
     
     private static void drawModSafe(float x, float y, float z) {
         float mx, my, mz;
         if (!modSafeActive || !safeShow) return;
         if (--safeUpdate<0) {
             safeUpdate = 16;
             reCheckSafe(fix(x), fix(y), fix(z));
         }
         if (safeGhost) GL11.glDisable(GL11.GL_DEPTH_TEST);
         else           GL11.glEnable( GL11.GL_DEPTH_TEST);
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         GL11.glDisable(GL11.GL_BLEND);
         GL11.glDisable(GL11.GL_FOG);
         GL11.glBegin(GL11.GL_LINES);
         for (int i = 0; i < safeCur; ++i) {
             Mark got = safeMark[i];
             byte r,g,b;
             if (got.r==1) {
                 r = optSafeDangerColorSun.r; g = optSafeDangerColorSun.g; b = optSafeDangerColorSun.b;
             } else {
                 r = optSafeDangerColor.r; g = optSafeDangerColor.g; b = optSafeDangerColor.b;
             }
             GL11.glColor3ub(r,g,b);
             mx = got.x - x; my = got.y - y; mz = got.z - z;
             GL11.glVertex3f(mx+0.4f,my,mz+0.4f); GL11.glVertex3f(mx-0.4f,my,mz-0.4f);
             GL11.glVertex3f(mx+0.4f,my,mz-0.4f); GL11.glVertex3f(mx-0.4f,my,mz+0.4f);
         }
         GL11.glEnd();
     }
     
     private static String textModSafe(String txt) {
         if (!modSafeActive || !safeShow || tagSafe.length()==0) return txt;
         return txt + tagSafe + " ";
     }
     
     private static boolean emptySpaceHere(int pX, int pY, int pZ) {
         double x = pX + 0.5, y = pY, z = pZ + 0.5;
         //double r = 0.3, h = 1.8; // skeleton size
         //double r = 0.35, h = 0.5; // cave spider size
         double r = 0.3, h = 0.5; // hybrid size
         AxisAlignedBB aabb = new AxisAlignedBB(x - r, y, z - r, x + r, y + h, z + r);
         return map.getAllCollidingBoundingBoxes(aabb).isEmpty() && !map.isAnyLiquid(aabb);
     }
     
     private static boolean couldSpawnHere(int x, int y, int z) {
         try {
             return y >= 0
                 && getBlockLightLevel(x,y,z) < 8
                 && SpawnerAnimals.canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, map, x,y,z)
                 && emptySpaceHere(x,y,z);
         } catch (Exception e) {
             return false;
         }
     }
 
     private static void reCheckSafe(int pX, int pY, int pZ) {
         safeCur = 0;
         for (int x = pX-optSafeLookupRadius; x <= pX+optSafeLookupRadius; ++x)
         for (int y = pY-optSafeLookupRadius; y <= pY+optSafeLookupRadius; ++y)
         for (int z = pZ-optSafeLookupRadius; z <= pZ+optSafeLookupRadius; ++z) {
             if (couldSpawnHere(x,y,z)) {
                 safeMark[safeCur] = new Mark(x,y,z, optSafeShowWithSun && (getSkyLightLevel(x,y,z) > 7));
                 ++safeCur;
                 if (safeCur == safeMax) return;
             }
         }
     }
     
     
     //=ZMod=Boom==============================================================
     private static boolean modBoomEnabled, modBoomActive;
     private static int optBoomSafeRange;
     private static float optBoomDropOreChance, optBoomDropChance, optBoomScaleTNT, optBoomScaleCreeper, optBoomScaleFireball;
 
     private static boolean initModBoom() {
         if (!checkStatus(MOD_BOOM, "boom")) return false;
         log("info: loading config for \"boom\"");
         return modBoomActive = true;
     }
     
     private static void quitModBoom() {
         if (!modBoomActive) return;
         modBoomActive = false;
     }
 
     private static void optionsModBoom() {
         boolean wasEnabled = modBoomEnabled;
         modBoomEnabled = getSetBool(modBoomEnabled, "modBoomEnabled", false,     "Enable Boom mod");
         if (wasEnabled != modBoomEnabled) {
             if (modBoomEnabled && !modBoomActive) initModBoom(); 
             if (!modBoomEnabled && modBoomActive) quitModBoom();
         }
         optBoomDropOreChance = (float)getSetInt((int)(optBoomDropOreChance * 100f), "optBoomDropOreChance", 100, 0, 100, "Drop ore chance") / 100f;
         optBoomDropChance = (float)getSetInt((int)(optBoomDropChance * 100f), "optBoomDropChance", 30, 0, 100, "Drop non-ore chance") / 100f;
         optBoomScaleTNT = getSetFloat(optBoomScaleTNT, "optBoomScaleTNT", 1f, 0.1f, 10f, "TNT explosion multiplier");
         optBoomScaleCreeper = getSetFloat(optBoomScaleCreeper, "optBoomScaleCreeper", 1f, 0.1f, 10f, "Creeper explosion multiplier");
         optBoomScaleFireball = getSetFloat(optBoomScaleFireball, "optBoomScaleFireball", 1f, 0.1f, 10f, "Fireball explosion multiplier");
         optBoomSafeRange  = getSetInt(optBoomSafeRange, "optBoomSafeRange", 16, -1, 32, "Damage prevention range (-1=inf, 0=off)");
     }
 
     public static float boomDropHandle(int blockId) {
         if (modBoomActive && !isMultiplayer) {
             if ((block[blockId] & ORE) != 0) return optBoomDropOreChance;
             else return optBoomDropChance;
         }
         return 0.3f;
     }
 
     public static boolean boomDamageHandle(int eX, int eY, int eZ, int type) {
         boolean damage = true;
         if (type == 1 || type == 2) {
             if (optBoomSafeRange == -1) damage = false;
             else if (optBoomSafeRange > 0) {
                 int x1=eX-optBoomSafeRange,x2=eX+optBoomSafeRange,y1=eY-optBoomSafeRange,y2=eY+optBoomSafeRange,z1=eZ-optBoomSafeRange,z2=eZ+optBoomSafeRange;
                 Search: for (int x=x1;x<=x2;x++) for (int y=y1;y<=y2;y++) for (int z=z1;z<=z2;z++) if ((block[mapXGetId(x,y,z)] & CRAFT)!=0) { damage = false; break Search; }
             }
         }
         return damage;
     }
 
     public static float boomScaleHandle(float boom, int type) {
         if (!modBoomActive || isMultiplayer) return boom;
         switch(type) {
             case 1: boom *= optBoomScaleCreeper; break; // creeper
             case 2: boom *= optBoomScaleFireball; break; // fireball
             case 3: boom *= optBoomScaleTNT; break; // tnt
         }
         return boom;
     }
 
 
     //=ZMod=Spawn=============================================================
     private static boolean modSpawnEnabled, modSpawnActive;
     private static boolean optSpawnSupportMods, optSpawnAllowInNonAir, optSpawnAllowOnNonNatural, optSpawnAllowOnGrass, optSpawnAllowOnCobble, optSpawnAllowOnSand, optSpawnAllowOnGravel, optSpawnAllowOnTree, optSpawnAllowOnSandstone;
     private static int optSpawnPigReduction, optSpawnChickenReduction, optSpawnCowReduction, optSpawnSheepReduction, optSpawnSquidReduction, optSpawnGhastReduction, optSpawnWolfReduction;
     private static int optSpawnSpiderReduction, optSpawnSkeletonReduction, optSpawnCreeperReduction, optSpawnZombieReduction, optSpawnSlimeReduction, optSpawnPigZombieReduction;
     private static int optSpawnCaveSpiderReduction, optSpawnEndermanReduction, optSpawnSilverfishReduction;
 
     private static boolean initModSpawn() {
         if (!checkStatus(MOD_SPAWN, "spawn")) return false;
         log("info: loading config for \"spawn\"");
         optSpawnSupportMods = getBool("optSpawnSupportMods", true);
 
         optSpawnPigReduction = getInt("optSpawnPigReduction", 75, 0, 100);
         optSpawnChickenReduction = getInt("optSpawnChickenReduction", 0, 0, 100);
         optSpawnCowReduction = getInt("optSpawnCowReduction", 0, 0, 100);
         optSpawnSheepReduction = getInt("optSpawnSheepReduction", 0, 0, 100);
         optSpawnSquidReduction = getInt("optSpawnSquidReduction", 0, 0, 100);
         optSpawnWolfReduction = getInt("optSpawnWolfReduction", 0, 0, 100);
 
         optSpawnSpiderReduction = getInt("optSpawnSpiderReduction", 0, 0, 100);
         optSpawnSkeletonReduction = getInt("optSpawnSkeletonReduction", 0, 0, 100);
         optSpawnCreeperReduction = getInt("optSpawnCreeperReduction", 0, 0, 100);
         optSpawnZombieReduction = getInt("optSpawnZombieReduction", 0, 0, 100);
         optSpawnSlimeReduction = getInt("optSpawnSlimeReduction", 0, 0, 100);
 
         optSpawnGhastReduction = getInt("optSpawnGhastReduction", 0, 0, 100);
         optSpawnPigZombieReduction = getInt("optSpawnPigZombieReduction", 0, 0, 100);
         optSpawnCaveSpiderReduction = getInt("optSpawnCaveSpiderReduction", 0, 0, 100);
         optSpawnEndermanReduction = getInt("optSpawnEndermanReduction", 0, 0, 100);
         optSpawnSilverfishReduction = getInt("optSpawnSilverfishReduction", 0, 0, 100);
         return modSpawnActive = true;
     }
     
     private static void quitModSpawn() {
         if (!modSpawnActive) return;
         modSpawnActive = false;
     }
 
     private static void optionsModSpawn() {
         boolean wasEnabled = modSpawnEnabled;
         modSpawnEnabled = getSetBool(modSpawnEnabled, "modSpawnEnabled", false,     "Enable Spawn mod");
         if (wasEnabled != modSpawnEnabled) {
             if (modSpawnEnabled && !modSpawnActive) initModSpawn(); 
             if (!modSpawnEnabled && modSpawnActive) quitModSpawn();
         }
         optSpawnAllowInNonAir = getSetBool(optSpawnAllowInNonAir, "optSpawnAllowInNonAir", false, "Allow in non-air (reeds, wheat etc)");
         optSpawnAllowOnNonNatural = getSetBool(optSpawnAllowOnNonNatural, "optSpawnAllowOnNonNatural", false, "Allow on non-natural");
         optSpawnAllowOnGrass = getSetBool(optSpawnAllowOnGrass, "optSpawnAllowOnGrass", true, "Allow on grass");
         optSpawnAllowOnCobble = getSetBool(optSpawnAllowOnCobble, "optSpawnAllowOnCobble", false, "Allow on cobble");
         optSpawnAllowOnSand = getSetBool(optSpawnAllowOnSand, "optSpawnAllowOnSand", true, "Allow on sand");
         optSpawnAllowOnGravel = getSetBool(optSpawnAllowOnGravel, "optSpawnAllowOnGravel", true, "Allow on gravel");
         optSpawnAllowOnTree = getSetBool(optSpawnAllowOnTree, "optSpawnAllowOnTree", false, "Allow on tree (leaves, trunk)");
         optSpawnAllowOnSandstone = getSetBool(optSpawnAllowOnSandstone, "optSpawnAllowOnSandstone", false, "Allow on sandstone");
     }
 
     private static void updateModSpawn(List list) {
         if (!modSpawnActive || isMultiplayer) return;
         Iterator it = list.iterator();
         int mask = 0;
         if (!optSpawnAllowOnGrass) mask |= GRASS;
         if (!optSpawnAllowOnCobble) mask |= COBBLE;
         if (!optSpawnAllowOnSand) mask |= SAND;
         if (!optSpawnAllowOnGravel) mask |= GRAVEL;
         if (!optSpawnAllowOnTree) mask |= TREE;
         if (!optSpawnAllowOnNonNatural) mask |= CRAFT;
         if (!optSpawnAllowOnSandstone) mask |= SANDSTONE;
         while (it.hasNext()) {
             Entity ent = (Entity)it.next();
             if (getEntityAge(ent)!=1) continue; // 1st beat
             boolean kill = false;
             switch(getEntityType(ent)) {
                 case GHAST:     if (optSpawnGhastReduction     != 0 && rnd.nextInt(100)<optSpawnGhastReduction    ) kill = true; break;
                 case COW:       if (optSpawnCowReduction       != 0 && rnd.nextInt(100)<optSpawnCowReduction      ) kill = true; break;
                 case SPIDER:    if (optSpawnSpiderReduction    != 0 && rnd.nextInt(100)<optSpawnSpiderReduction   ) kill = true; break;
                 case SHEEP:     if (optSpawnSheepReduction     != 0 && rnd.nextInt(100)<optSpawnSheepReduction    ) kill = true; break;
                 case SKELLY:    if (optSpawnSkeletonReduction  != 0 && rnd.nextInt(100)<optSpawnSkeletonReduction ) kill = true; break;
                 case CREEPER:   if (optSpawnCreeperReduction   != 0 && rnd.nextInt(100)<optSpawnCreeperReduction  ) kill = true; break;
                 case ZOMBIE:    if (optSpawnZombieReduction    != 0 && rnd.nextInt(100)<optSpawnZombieReduction   ) kill = true; break;
                 case SLIME:     if (optSpawnSlimeReduction     != 0 && rnd.nextInt(100)<optSpawnSlimeReduction    ) kill = true; break;
                 case PIG:       if (optSpawnPigReduction       != 0 && rnd.nextInt(100)<optSpawnPigReduction      ) kill = true; break;
                 case CHICKEN:   if (optSpawnChickenReduction   != 0 && rnd.nextInt(100)<optSpawnChickenReduction  ) kill = true; break;
                 case SQUID:     if (optSpawnSquidReduction     != 0 && rnd.nextInt(100)<optSpawnSquidReduction    ) kill = true; break;
                 case PIGZOMBIE: if (optSpawnPigZombieReduction != 0 && rnd.nextInt(100)<optSpawnPigZombieReduction) kill = true; break;
                 case WOLF:      if (optSpawnWolfReduction      != 0 && rnd.nextInt(100)<optSpawnWolfReduction     ) kill = true; break;
                 case CAVESPIDER:if(optSpawnCaveSpiderReduction!= 0 && rnd.nextInt(100)<optSpawnCaveSpiderReduction)kill = true; break;
                 case ENDERMAN:  if (optSpawnEndermanReduction  != 0 && rnd.nextInt(100)<optSpawnEndermanReduction ) kill = true; break;
                 case SILVERFISH:if(optSpawnSilverfishReduction!= 0 && rnd.nextInt(100)<optSpawnSilverfishReduction)kill = true; break;
                 case LIVING:    if (!optSpawnSupportMods) continue; break;
                 default: continue;
             }
             if (!kill) {
                 int x = fix(getEntityPosX(ent)), y = fix(getEntityPosY(ent)), z = fix(getEntityPosZ(ent));
                 if (!optSpawnAllowInNonAir && (block[mapXGetId(x,y,z)] & DECAL)!=0) kill = true;
                 if (mask!=0 && (block[mapXGetId(x,y-1,z)] & mask)!=0) kill = true;
             }
             if (kill) dieEntity(ent);
         }
     }
     
     
     //=ZMod=Ore===============================================================
     private static boolean modOreEnabled, modOreActive;
     private static boolean optOreLavaFloor;
     private static int[] optOreCoalRule, optOreIronRule, optOreGoldRule, optOreBlueRule, optOreRedRule, optOreDiamondRule;
 
     private static boolean initModOre() {
         if (!checkStatus(MOD_ORE, "ore")) return false;
         log("info: loading config for \"ore\"");
         optOreLavaFloor   = getBool("optOreLavaFloor", true);
         // chance_for_chunk / max_height / min_height / attempts / size / what_must_be_above
         if ((optOreCoalRule    = parseRule(getString("optOreCoalRule"   , "75/80/48/8/16/1  10/120/32/128/4/1 5/120/64/1/128/1"))) == null) return false;
         if ((optOreIronRule    = parseRule(getString("optOreIronRule"   , "100/80/16/8/16/1 100/96/8/16/8/1   5/120/64/128/1/1"))) == null) return false;
         if ((optOreGoldRule    = parseRule(getString("optOreGoldRule"   , "50/32/4/4/16/1   5/96/8/8/64/1"))) == null) return false;
         if ((optOreBlueRule    = parseRule(getString("optOreBlueRule"   , "100/32/8/2/8/1   5/56/48/64/2/1    5/96/48/1/32/1/1"))) == null) return false;
         if ((optOreRedRule     = parseRule(getString("optOreRedRule"    , "100/32/8/2/8/1   10/120/96/64/1/1"))) == null) return false;
         if ((optOreDiamondRule = parseRule(getString("optOreDiamondRule", "75/16/4/2/8/1    100/32/2/128/2/11 10/120/16/2/8/1"))) == null) return false;
         return modOreActive = true;
     }
     
     private static void quitModOre() {
         if (!modOreActive) return;
         modOreActive = false;
     }
     
     private static void optionsModOre() {
         boolean wasEnabled = modOreEnabled;
         modOreEnabled = getSetBool(modOreEnabled, "modOreEnabled", false,     "Enable Item mod");
         if (wasEnabled != modOreEnabled) {
             if (modOreEnabled && !modOreActive) initModOre(); 
             if (!modOreEnabled && modOreActive) quitModOre();
         }
     }
 
     private static void updateModOre() {
         if (!modOreActive || isMultiplayer || isHell) return;
         int cx = fix(posX) >> 4, cz = fix(posZ) >> 4, tx, ty, tz, id;
         for (int cxi=cx-3;cxi<=cx+3;cxi++) for (int czi=cz-3;czi<=cz+3;czi++) if (mapXGetChunkExists(cxi, czi)) {
             Chunk chunk = map.getChunkFromChunkCoords(cxi, czi);
             if (chunk.getBlockID(0, 0, 8) != 7) continue; // already done
             chunk.setBlockIDWithMetadata(0, 0, 8, 0, 0); chunk.setBlockIDWithMetadata(0, 1, 8, 7, 0); // mark the chunk done
             // clear ores
             for (int X=0;X<16;X++) for (int Z=0;Z<16;Z++) {
                 int max = chunk.getHeightValue(X, Z);
                 for (int Y=0;Y<=max;Y++) {
                     id = chunk.getBlockID(X, Y, Z);
                     if (id > 255) continue;
                     if ((block[id] & ORE) != 0) chunk.setBlockIDWithMetadata(X, Y, Z, 1, 0); // remove previous ore
                     else if (id == 7 && Y > 1) chunk.setBlockIDWithMetadata(X, Y, Z, optOreLavaFloor ? 11 : 1, 0); // lava or stone?
                     else if (Y == 1) chunk.setBlockIDWithMetadata(X, Y, Z, 7, 0); // flatten
                 }
             }
             // redistribute
             oreDistribute(chunk, optOreCoalRule   , 16);
             oreDistribute(chunk, optOreIronRule   , 15);
             oreDistribute(chunk, optOreGoldRule   , 14);
             oreDistribute(chunk, optOreBlueRule   , 21);
             oreDistribute(chunk, optOreRedRule    , 73);
             oreDistribute(chunk, optOreDiamondRule, 56);
             // request update
             chunkNeedsUpdate(cxi, czi);
         }
     }
 
     private static void oreDistribute(Chunk data, int rule[], int result) {
         int i, chunk, max, min, attempt, size, above, x, y, z, at, cnt;
         for (i=0;i<rule.length;i+=6) {
             chunk = rule[i + 0];
             max = rule[i + 1];
             min = rule[i + 2];
             attempt = rule[i + 3];
             size = rule[i + 4];
             above = rule[i + 5];
             if (chunk<100 && rnd.nextInt(100)>=chunk) continue;
             while (attempt-->0) {
                 x = rnd.nextInt(14)+1; y = rnd.nextInt(1+max-min) + min; z = rnd.nextInt(14)+1;
                 at = x<<11 | z<<7 | y;
                 // this hurts x_x
                 if (data.getBlockID(x, y, z)!=1
                 || data.getBlockID(x, y-1, z)!=1
                 || data.getBlockID(x, y+1, z)!=above
                 || data.getBlockID(x+1, y, z)!=1
                 || data.getBlockID(x-1, y, z)!=1
                 || data.getBlockID(x, y, z+1)!=1
                 || data.getBlockID(x, y, z-1)!=1) continue;
                 cnt = size - 1;
                 data.setBlockIDWithMetadata(x, y, z, result, 0);
                 while (cnt-->0) {
                     switch(rnd.nextInt(7)) {
                         case 0: continue;
                         case 1: x++; break;
                         case 2: x--; break;
                         case 3: y++; break;
                         case 4: y--; break;
                         case 5: z++; break;
                         case 6: z--; break;
                     }
                     x &= 15; z &= 15; y &= 255;
                     if (data.getBlockID(x, y, z)==1) data.setBlockIDWithMetadata(x, y, z, result, 0);
                 }
             }
         }
     }
     
 
     //=ZMod=Teleport==========================================================
     private static boolean modTeleportEnabled, modTeleportActive;
     private static int keyTeleportUp, keyTeleportDown, keyTeleportCursor;
     private static boolean optTeleportIsSelected;
     private static int optTeleportUseItem;
     private static int optTeleportItem, optTeleportPlayer, optTeleportCritter;
 
     private static boolean initModTeleport() {
         if (!checkStatus(MOD_TELEPORT, "teleport")) return false;
         log("info: loading config for \"teleport\"");
         optTeleportItem = getBlockId("optTeleportItem", 42); // iron block
         optTeleportPlayer = getBlockId("optTeleportPlayer", 41); // gold block
         optTeleportCritter = getBlockId("optTeleportCritter", 57); // diamond block
         optTeleportUseItem = getItemId("optTeleportUseItem", 331); // redstone dust
         return modTeleportActive = true;
     }
     
     private static void quitModTeleport() {
         if (!modTeleportActive) return;
         modTeleportActive = false;
     }
     
     private static void optionsModTeleport() {
         boolean wasEnabled = modTeleportEnabled;
         modTeleportEnabled = getSetBool(modTeleportEnabled, "modTeleportEnabled", false,     "Enable Teleport mod");
         if (wasEnabled != modTeleportEnabled) {
             if (modTeleportEnabled && !modTeleportActive) initModTeleport(); 
             if (!modTeleportEnabled && modTeleportActive) quitModTeleport();
         }
         keyTeleportUp = getSetBind(keyTeleportUp, "keyTeleportUp",              Keyboard.KEY_PRIOR, "Teleport up");
         keyTeleportDown = getSetBind(keyTeleportDown, "keyTeleportDown",          Keyboard.KEY_NEXT, "Teleport down");
         keyTeleportCursor = getSetBind(keyTeleportCursor, "keyTeleportCursor",      Keyboard.KEY_RIGHT, "Teleport at cursor");
         optTeleportIsSelected = getSetBool(optTeleportIsSelected, "optTeleportIsSelected", true, "Teleport item must be selected");
     }
 
     private static void updateModTeleport(List list) {
         if (!modTeleportActive || isMultiplayer) return;
         Iterator it = list.iterator();
         int type, x, y, z, id, ofs;
         Entity entTpSound = null;
         while (it.hasNext()) {
             Entity ent = (Entity)it.next();
             type = getEntityType(ent);
             if (isMultiplayer && ent!=player) continue;
             x = fix(getEntityPosX(ent)); y = fix(getEntityPosY(ent)) - 1; z = fix(getEntityPosZ(ent)); ofs=0;
             // find solid ground / teleportation pad
             id = mapXGetId(x,y,z);
             if ((block[id] & SOLID) == 0) { id = mapXGetId(x,--y,z); ofs++; }
             // check pad type vs entity type
             if (type == 0 && (id != optTeleportItem || !(ent instanceof EntityItem))) continue;
             if (type != 0 && type != PLAYER && id != optTeleportCritter) continue;
             if (type == PLAYER && id != optTeleportPlayer) continue;
             // get sign text
             String sign[] = null;
             id = mapXGetId(x, y+1, z); if (id == 63 || id == 68) sign = getSignText(x, y+1, z);
             id = mapXGetId(x, y+2, z); if (id == 63 || id == 68) sign = getSignText(x, y+2, z);
             id = mapXGetId(x, y-1, z); if (id == 63 || id == 68) sign = getSignText(x, y-1, z);
             if (sign == null) continue;
             // read teleportation info from sign and TP
             try {
                 x = 0; y = -1; z = 0;
                 boolean allow = false, needAllow = false;
                 for (int i=0;i<sign.length;i++) if (sign[i]!=null && sign[i].length()>1) {
                     if (sign[i].charAt(0)=='!') {
                         String part[] = sign[i].substring(1).split(",");
                         if (part.length != 3) break;
                         x = new Integer(part[0]);
                         y = new Integer(part[1]);
                         z = new Integer(part[2]);
                     } else if (sign[i].charAt(0)=='?') {
                         if (sign[i].charAt(1)=='!') {
                             String filter = sign[i].substring(2);
                             id = ( names.containsKey(filter) ? (Integer)names.get(filter) : parseIdInfo(filter) ) & 0xffff; // strip "damage" / info
                             if (type != 0 && type != PLAYER && id == type) { y=-1; break; } // mob filter match
                             if (type == 0 && (ent instanceof EntityItem && getItemsId(getEntityItemStack((EntityItem)ent)) == id)) { y=-1; break; } // item filter match
                         } else {
                             needAllow = true;
                             String filter = sign[i].substring(1);
                             id = ( names.containsKey(filter) ? (Integer)names.get(filter) : parseIdInfo(filter) ) & 0xffff; // strip "damage" / info
                             if (type != 0 && type != PLAYER && id == type) { allow = true; continue; } // mob filter match
                             if (type == 0 && (ent instanceof EntityItem && getItemsId(getEntityItemStack((EntityItem)ent)) == id)) { allow = true; continue; } // item filter match
                         }
                     }
                 }
                 if (y==-1 || (needAllow && !allow)) continue;
                 // teleport
                 Entity entBound = getOnEntity(ent);
                 if (entBound != null) {
                     // should be correct coordinates - but are not :(
                     setEntityPos(entBound, 0.5+x, 0.0+(y+ofs), 0.5+z);
                     setEntityPos(ent, 0.5+x, getMountOffset(entBound)+(y+ofs), 0.5+z);
                 } else setEntityPos(ent, 0.5+x, 0.5+(y+ofs), 0.5+z);
                 if (type==PLAYER) entTpSound = ent;
             } catch(Exception whatever) {} // failsafe
         }
         // handle non-pad teleporting
         if (!isMenu && inWhat == null) {
             int dir = 0, s;
             boolean teleport = false;
             if (keyPress(keyTeleportUp)) dir = 1;
             else if (keyPress(keyTeleportDown)) dir = -1;
             x = fix(getEntityPosX(player)); y = fix(getEntityPosY(player)); z = fix(getEntityPosZ(player));
             if (dir != 0) { // teleport up and down
                 boolean safe[] = new boolean[131], ok = false;
                 while (y > 1 && (block[mapXGetId(x,y-1,z)] & SOLID) == 0) y--; // land player
                 for (int i=0;i<131;i++) { // find all acceptable locations
                     id = mapXGetId(x,i,z);
                     if ((block[id] & SOLID) != 0 && id != 81) ok = true; // above ground ...
                     else if (id == 10 || id == 11 || id == 51 || id == 81 || i==y) ok = false; // ... above lava, fire, cactus or current location
                     safe[i] = ok && (block[id] & SPACE)!=0;
                     if (i>2 && !safe[i] && safe[i-1] && !safe[i-3]) safe[i-1] = safe[i-2] = false; // not enough room
                 }
                 for (int i=130;i>0;i--) if (safe[i] && safe[i-1]) safe[i] = false; // remove locations in air
                 while (y>0 && y<129) if (safe[y += dir]) {
                     if (y < 1 || (isHell && y>127)) break; // extra sanitizing
                     teleport = true; y++; break; // found the place
                 }
             } else if (keyPress(keyTeleportCursor) && rayTrace(256d, 0f)) { // teleport to cursor
                 x = rayHitX(); y = rayHitY(); z = rayHitZ(); s = rayHitSide();
                 if (s==0) y--; if (s==1) y++; if (s==2) z--; if (s==3) z++; if (s==4) x--; if (s==5) x++; // move to side
                 int i = y; id = 0; if (!fly) while (i>0 && (block[id = mapXGetId(x,i-1,z)] & SOLID) == 0) i--; // what is down there?
                 if (mapXGetId(x,i-1,z)!=81 && id != 10 && id != 11 && id != 51) { // ... nothing bad.
                     if ((block[mapXGetId(x,y+1,z)] & SOLID)!=0) y--; else if ((block[mapXGetId(x,y-1,z)] & SOLID)!=0) y++;
                     if ((y-i < 4) && (block[mapXGetId(x,y-1,z)] & SPACE)!=0 && (block[mapXGetId(x,y,z)] & SPACE)!=0 && (block[mapXGetId(x,y+1,z)] & SPACE)!=0) teleport = true; // plenty of space
                 }
             }
             if (teleport && optTeleportUseItem != 0) { // use the item
                 int cur = getInvCur(), use = -1, cnt;
                 for (int i=0;i<36;i++) if (invItemsArr[i]!=null && isItemsMatch(invItemsArr[i], optTeleportUseItem) && (!optTeleportIsSelected || i==cur)) { use = i; break; }
                 if (use == -1) teleport = false; // nothing to use
                 else {
                     cnt = getItemsCount(invItemsArr[use]) - 1;
                     if (cnt == 0) invItemsArr[use] = null; else setItemsCount(invItemsArr[use], cnt);
                 }
             }
             if (teleport) { // do the deed
                 setEntityPos(player, 0.5+x, 0.75+y, 0.5+z);
                 entTpSound = player;
             }
         }
         // teleport effect when player teleported
         if (entTpSound != null) noiseTP(entTpSound);
     }
     
     
     //=ZMod=Cheat=============================================================
     private static boolean modCheatEnabled, modCheatActive, modCheatAllowed;
     private static String tagCheater;
     private static int keyCheat, keyCheatShowMobs, keyCheatShowOres, keyCheatSee, keyCheatHighlight, keyCheatRemoveFire, keyCheatPossession, keyCheatProjection, keyCheatRelocate, keyCheatHealth, keyCheatDamage;
     private static boolean optCheat, optCheatFallDamage, optCheatDisableDamage, optCheatRestoreHealth, optCheatShowDangerous, optCheatShowNeutral, optCheatSeeIsToggle, optCheatShowMobsSize;
     private static boolean optCheatInfArrows, optCheatInfArmor, optCheatInfSword, optCheatInfTools, optCheatFireImmune, optCheatShowHealth;
     private static boolean optCheatNoAir, optCheatNerfEnderman, optCheatProjectionSwing, optCheatProjectionSpoof, optCheatProjectionPlace;
     private static int optCheatHighlightMode, optCheatShowOresRangeH, optCheatShowOresRangeV, optCheatShowMobsRange;
     private static float optCheatSeeDist;
     private static final int cheatMax = 16384, cheatItems = 400;
     private static Mark cheatMobs[], cheatOres[], cheatMark[];
     private static boolean cheating = false, cheatShowMobs = false, cheatShowOres = false, cheatSee, cheatDamage[], cheatHighlight;
     private static int cheatCur = 0;
     private static float cheatRefresh;
     private static FloatBuffer cheatAmbItems, cheatAmbGeom;
     private static EntityLiving cheatView;
     private static EntityLiving cheatPossession;
     private static EntityPlayerGhost cheatProjection;
     private static boolean cheatProjectionLock = false, cheatUnspoof = false;
     private static float cheatGamma, cheatYaw, cheatPitch;
     private static boolean cheatCarryBlocks[], cheatCarryOverride;
     private static Field fCarryBlocks = getField(EntityEnderman.class, "ee_canCarryBlocks");
     private static int optCheatBlockHitDelay = 5;
 
     private static boolean initModCheat() {
         if (!checkStatus(MOD_CHEAT, "cheat")) return false;
         log("info: loading config for \"cheat\"");
         cheatMobs = new Mark[MAXTYPE];
         cheatOres = new Mark[4096];
         cheatMark = new Mark[cheatMax]; cheatCur = 0;
         cheatDamage = new boolean[cheatItems];
         cheatAmbItems = makeBuffer(new float[] {4f, 4f, 4f, 1f});
         cheatAmbGeom  = makeBuffer(new float[] {0f, 0f, 0f, 1f});
         getDeprecated("optCheatHighlightMode");
         String val[];
         val = getString("optCheatShowOres", "15/0x008888, 82/0x00ffff, 14/0xffee00, 56/0xeeffff, 48/0x00ff00, 21/0x0000ff, 73/0xff0000, 52/0xff00ff, 16/0x444444").split("[\\t ]*,[\\t ]*");
         for (int i=0;i<val.length;i++) {
             String got[] = val[i].split("/");
             if (got.length == 2) {
                 Mark color = new Mark();
                 int id = names.containsKey(got[0]) ? (Integer)(names.get(got[0])) : parseUnsigned(got[0]);
                 if (id>0 && id<cheatOres.length && color.loadColor(got[1])) {
                     cheatOres[id] = color;
                 }
             } else {
                 err("error: config.txt @ optCheatOres - invalid ore/color pair \""+val[i]+"\"");
             }
         }
         val = getString("optCheatShowMobs", "1/0x000088, 3/0x880000, 5/0x888888, 6/0x008800, 7/0x888800, 8/0x880088, 12/0x008888, 11/0x000044, 2/0x444400, 4/0x444444, 9/0x440000, 10/0x004444, 14/0x004400, 15/0xffffff").split("[\\t ]*,[\\t ]*");
         for (int i=0;i<val.length;i++) {
             String got[] = val[i].split("/");
             if (got.length == 2) {
                 Mark color = new Mark();
                 int id = names.containsKey(got[0]) ? (Integer)(names.get(got[0])) : parseUnsigned(got[0]);
                 if (id>=0 && id<MAXTYPE && color.loadColor(got[1])) { cheatMobs[id] = color; continue; }
             }
             err("error: config.txt @ optCheatMobs - invalid mob/color pair \""+val[i]+"\"");
         }
         tagCheater = getString("tagCheater", "cheater");
         optCheatProjectionSwing = getBool("optCheatProjectionSwing", true);
         optCheatProjectionSpoof = getBool("optCheatProjectionSpoof", false);
         optCheatProjectionPlace = getBool("optCheatProjectionPlace", false);
         return modCheatActive = true;
     }
     
     private static void quitModCheat() {
         if (!modCheatActive) return;
         modCheatActive = false;
         cheatMobs = null;
         cheatOres = null;
         cheatMark = null;
         cheatDamage = null;
     }
     
     private static void optionsModCheat() {
         boolean wasEnabled = modCheatEnabled;
         modCheatEnabled = getSetBool(modCheatEnabled, "modCheatEnabled", false,     "Enable Cheat mod");
         if (wasEnabled != modCheatEnabled) {
             if (modCheatEnabled && !modCheatActive) initModCheat(); 
             if (!modCheatEnabled && modCheatActive) quitModCheat();
         }
         boolean prev = false;
         keyCheat = getSetBind(keyCheat, "keyCheat",                        Keyboard.KEY_Y, "Toggle cheat mode");
         optCheat = getSetBool(optCheat, "optCheat", false, "Cheats activated by default");
         optCheatShowHealth = false;
         //optCheatShowHealth = getSetBool(optCheatShowHealth, "optCheatShowHealth", true, "Show critter health");
         keyCheatHighlight = getSetBind(keyCheatHighlight, "keyCheatHighlight",      Keyboard.KEY_H, "Toggle light level");
         keyCheatRemoveFire = Keyboard.KEY_NONE;
         //keyCheatRemoveFire = getSetBind(keyCheatRemoveFire, "keyCheatRemoveFire",    Keyboard.KEY_N, "Remove fire nearby");
         keyCheatPossession = getSetBind(keyCheatPossession, "keyCheatPossession",    Keyboard.KEY_NUMPAD5, "Get player possession view", FEATURE_POSSESSION);
         keyCheatProjection = getSetBind(keyCheatProjection, "keyCheatProjection",    Keyboard.KEY_NUMPAD8, "Get ghost projection view", FEATURE_PROJECTION);
         keyCheatRelocate = getSetBind(keyCheatRelocate, "keyCheatRelocate",    Keyboard.KEY_NUMPAD2, "Relocate projection view", FEATURE_PROJECTION);
         keyCheatHealth = getSetBind(keyCheatHealth, "keyCheatHealth",            Keyboard.KEY_NONE, "Toggle health regen");
         keyCheatDamage = getSetBind(keyCheatDamage, "keyCheatDamage",            Keyboard.KEY_NONE, "Toggle damages (invulnerability)");
         keyCheatShowMobs = getSetBind(keyCheatShowMobs, "keyCheatShowMobs",        Keyboard.KEY_M, "Show / hide critters");
         optCheatShowMobsRange = getSetInt(optCheatShowMobsRange, "optCheatShowMobsRange", 0, 0, 256, "Show critters max range");
         optCheatShowMobsSize = getSetBool(optCheatShowMobsSize, "optCheatShowMobsSize", false, "Use critter height for markers");
         keyCheatShowOres = getSetBind(keyCheatShowOres, "keyCheatShowOres",        Keyboard.KEY_O, "Show / hide ores");
         optCheatShowOresRangeH = getSetInt(optCheatShowOresRangeH, "optCheatShowOresRangeH", 16, 4, 128, "Show ores horisontal range");
         optCheatShowOresRangeV = getSetInt(optCheatShowOresRangeV, "optCheatShowOresRangeV", 64, 4, 128, "Show ores vertical range");
         keyCheatSee = getSetBind(keyCheatSee, "keyCheatSee",                  Keyboard.KEY_I, "See through solid objects");
         optCheatSeeIsToggle = getSetBool(optCheatSeeIsToggle, "optCheatSeeIsToggle", false, "See through is a toggle");
         optCheatSeeDist = getSetFloat(optCheatSeeDist, "optCheatSeeDist", 4.0f, 1.0f, 32.0f, "See through cut distance");
         optCheatBlockHitDelay = getSetInt(optCheatBlockHitDelay, "optCheatBlockHitDelay", 5, 0, 5, "Block hit delay. Lower is faster, but also buggier (rollbacks)");
         optCheatRestoreHealth = getSetBool(optCheatRestoreHealth, "optCheatRestoreHealth", false, "Regen health");
         optCheatDisableDamage = getSetBool(optCheatDisableDamage, "optCheatDisableDamage", false, "Disable damage (=invulnerability)");
         optCheatFallDamage = getSetBool(optCheatFallDamage, "optCheatFallDamage", true, "Fall damage");
         optCheatFireImmune = getSetBool(optCheatFireImmune, "optCheatFireImmune", false, "Immune to fire");
         optCheatNerfEnderman = getSetBool(optCheatNerfEnderman, "optCheatNerfEnderman", false, "Enderman is allowed to pick up blocks.");
         optCheatNoAir = getSetBool(optCheatNoAir, "optCheatNoAir", false, "Infinite air supply");
         optCheatInfArmor = false;
         //optCheatInfArmor = getSetBool(optCheatInfArmor, "optCheatInfArmor", false, "Indestructible armor");
         if (modCheatActive) for (int i=298;i<=317;i++) cheatDamage[i] = optCheatInfArmor;
         optCheatInfSword = false;
         //optCheatInfSword = getSetBool(optCheatInfSword, "optCheatInfSword", false, "Indestructible sword/bow");
         if (modCheatActive) cheatDamage[267] = cheatDamage[268] = cheatDamage[272] = cheatDamage[276] = cheatDamage[283] = cheatDamage[261] = optCheatInfSword;
         optCheatInfTools = false;
         //optCheatInfTools = getSetBool(optCheatInfTools, "optCheatInfTools", false, "Indestructible tools");
         if (modCheatActive) cheatDamage[256] = cheatDamage[257] = cheatDamage[258] = cheatDamage[259] = cheatDamage[269] =
             cheatDamage[270] = cheatDamage[271] = cheatDamage[273] = cheatDamage[274] = cheatDamage[275] =
             cheatDamage[277] = cheatDamage[278] = cheatDamage[279] = cheatDamage[284] = cheatDamage[285] =
             cheatDamage[286] = cheatDamage[290] = cheatDamage[291] = cheatDamage[292] = cheatDamage[293] =
             cheatDamage[294] = cheatDamage[346] = cheatDamage[359] = optCheatInfTools;
     }
 
     private static int cheatArrowCount;
 
     private static void updateModCheat(List list) {
         if (minecraft.gameSettings.gammaSetting < 100f) cheatGamma = minecraft.gameSettings.gammaSetting;
         minecraft.gameSettings.gammaSetting = cheating && cheatHighlight && !isMenu ? 1000f : cheatGamma;
 
         if (!modCheatActive) return;
         
         if (isWorldChange) {
             cheating = optCheat && modCheatAllowed;
         }
 
         boolean enable = optCheatNerfEnderman && !isMultiplayer;
         if (enable != cheatCarryOverride) {
             cheatCarryOverride = enable;
             Object arr = getValue(fCarryBlocks, null);
             if (cheatCarryBlocks == null) {
                 cheatCarryBlocks = new boolean[256];
                 for (int i=0;i<256;i++) cheatCarryBlocks[i] = Array.getBoolean(arr, i);
             }
             for (int i=0;i<256;i++) Array.setBoolean(arr, i, enable ? false : cheatCarryBlocks[i]);
         }
 
         if (!isMenu && keyPress(keyCheat)) {
             cheating = !cheating;
             if (!modCheatAllowed && cheating) {
                 cheating = false;
                 chatClient("\u00a74zombe's \u00a72cheat\u00a74-mod is not allowed on this server.");
             }
         }
         
         if (cheatView != null) {
             if (isWorldChange || !cheating || getView()==player) {
                 if (cheatView == cheatProjection && !cheatProjectionLock) {
                     cheatProjection.movementInput = new MovementInput();
                     player.movementInput = playerMovementInput;
                     setView(player);
                     if (optCheatProjectionSpoof) {
                         setView(player);
                         syncCurrentItem();
                     }
                 }
                 cheatView = null;
             }
             if (cheatView == null) setView(player);
         }
         if (cheatPossession != null) {
             if (isWorldChange || !cheating || !list.contains(cheatPossession) || cheatView != cheatPossession) {
                 if (cheatView == cheatPossession) cheatView = null;
                 cheatPossession = null;
             }
             if (cheatView == null) setView(player);
         }
         if (cheatProjection != null) {
             if (isWorldChange || !cheating || !list.contains(cheatProjection.playerBody)) {
                 if (cheatView == cheatProjection && !cheatProjectionLock) {
                     cheatProjection.movementInput = new MovementInput();
                     player.movementInput = playerMovementInput;
                     if (optCheatProjectionSpoof) {
                         setView(player);
                         syncCurrentItem();
                     }
                 }
                 if (cheatView == cheatProjection) cheatView = null;
                 cheatProjection = null;
             }
             if (cheatView == null) setView(player);
         }
         if (cheatView != null) {
             String message = "View: \u00a79" + getEntityName(cheatView) + "\u00a7f";
             if (getView() == cheatProjection) {
                 if (flyProjection) message += " " + tagFly;
                 if (flyNoClipProjection) message += " " + tagFlyNoClip;
             }
             setMsg(MTAG2, message);
         }
         if (!cheating) return;
         if (!isMenu) {
             if (keyPress(keyCheatPossession)) {
                 if (cheatPossession != null) {
                     cheatPossession = null;
                     cheatView = null;
                     setView(player);
                 } else {
                     EntityLiving eye = getView();
                     // hold on to your hats - math follows
                     float x1, x2, x3, xt, y1, y2, y3, yt, z1, z2, z3, zt, yaw, pitch, u, distS, factor;
                     x1 = (float) getEntityPosX(eye); y1 = (float) getEntityPosY(eye); z1 = (float) getEntityPosZ(eye);
                     yaw = getEntityYaw(eye) * (float)(Math.PI / 180.0);
                     pitch = getEntityPitch(eye) * (float)(Math.PI / 180.0);
                     x2 = x1 + 100f * ( -(float)Math.sin(yaw) * (float)Math.abs(Math.cos(pitch)) );
                     y2 = y1 + 100f * ( -(float)Math.sin(pitch) );
                     z2 = z1 + 100f * ( (float)Math.cos(yaw) * (float)Math.abs(Math.cos(pitch)) );
                     EntityLiving best = null;
                     float bestDS = 1000000000f;
                     Iterator it = list.iterator();
                     while (it.hasNext()) {
                         Object obj = it.next();
                         if (!(obj instanceof EntityLiving) || obj == eye) continue;
                         EntityLiving ent = (EntityLiving)obj;
                         if (!(ent instanceof EntityPlayer)) continue; /* GOD-DAMN-IT ... can not view from other mobs */
                         x3 = (float)getEntityPosX(ent);  y3 = (float)getEntityPosY(ent);  z3 = (float)getEntityPosZ(ent);
                         if ((x2-x1)*(x3-x1) + (y2-y1)*(y3-y1) + (z2-z1)*(z3-z1) < 0f) continue;
                         factor = 1f / ( (x1-x3)*(x1-x3) + (y1-y3)*(y1-y3) + (z1-z3)*(z1-z3) );
                         xt = x2 - x1; yt = y2 - y1; zt = z2 - z1;  u = xt*xt + yt*yt + zt*zt;
                         u = ( (x3-x1)*(x2-x1) + (y3-y1)*(y2-y1) + (z3-z1)*(z2-z1) ) / u;
                         xt = x1 + u*(x2-x1) - x3; yt = y1 + u*(y2-y1) - y3; zt = z1 + u*(z2-z1) - z3;
                         distS = ( xt*xt + yt*yt + zt*zt ) * factor;
                         if (distS < bestDS) { best = ent; bestDS = distS; }
                     }
                     if (best != null) {
                         if (getView() == cheatProjection && !cheatProjectionLock) {
                             cheatProjection.movementInput = new MovementInput();
                             player.movementInput = playerMovementInput;
                             if (optCheatProjectionSpoof) {
                                 setView(player);
                                 syncCurrentItem();
                             }
                         }
                         setView(best);
                         if (best == player) best = null;
                         cheatView = cheatPossession = best;
                     }
                 }
             }
             if (keyPress(keyCheatProjection)) {
                 if (cheatProjection == null) {
                     cheatProjection = new EntityPlayerGhost(map, player);
                     cheatProjection.placeAt(getView());
                     if (getView() == player) {
                         cheatProjection.capabilities.isFlying = flyProjection = fly;
                         cheatProjection.noClip = flyProjection && (flyNoClipProjection = flyNoClip);
                     }
                 }
                 if (cheatView == cheatProjection) {
                     cheatView = null;
                     if (!cheatProjectionLock) {
                         player.movementInput = playerMovementInput;
                         cheatProjection.movementInput = new MovementInput();
                         if (optCheatProjectionSpoof) {
                             setView(player);
                             syncCurrentItem();
                         }
                     }
                 } else {
                     cheatView = cheatProjection;
                     if (!cheatProjectionLock) {
                         player.movementInput = new MovementInput();
                         cheatProjection.movementInput = playerMovementInput;
                     }
                 }
                 if (cheatView != null) setView(cheatView);
                 else                   setView(player);
             }
             if (keyPress(keyCheatRelocate)) {
                 //if (cheatProjection == null) {
                 if (true) {
                     boolean was = getView() == cheatProjection;
                     cheatProjection = new EntityPlayerGhost(map, player);
                     if (was) setView(cheatView = cheatProjection);
                     if (!was || cheatProjectionLock) {
                         cheatProjection.movementInput = new MovementInput();
                     } else {
                         cheatProjection.movementInput = playerMovementInput;
                     }
                     if (!was) {
                         flyProjection = ((EntityPlayer)getView()).capabilities.isFlying && modFlyActive && modFlyAllowed;
                         flyNoClipProjection =  flyProjection && getView().noClip && modFlyActive && modFlyAllowed;
                     } else if (cheatProjectionLock) {
                         flyProjection = fly;
                         flyNoClipProjection = flyNoClip;
                     }
                     cheatProjection.capabilities.isFlying = flyProjection;
                     cheatProjection.noClip = flyProjection && flyNoClipProjection;
                 }
                 cheatProjection.placeAt(cheatPossession != null ? cheatPossession : player);
             }
             if (keyPress(keyCheatShowMobs)) cheatShowMobs = !cheatShowMobs;
             if (keyPress(keyCheatShowOres)) cheatShowOres = !cheatShowOres;
             if (keyPress(keyCheatHighlight)) cheatHighlight = !cheatHighlight;
             if (keyPress(keyCheatHealth)) optCheatRestoreHealth = !optCheatRestoreHealth;
             if (keyPress(keyCheatDamage)) optCheatDisableDamage = !optCheatDisableDamage;
             if (optCheatSeeIsToggle) { if (keyPress(keyCheatSee)) cheatSee = !cheatSee; } else cheatSee = keyDown(keyCheatSee);
             if (!isMultiplayer && keyPress(keyCheatRemoveFire)) {
                 int x = fix(posX), y = fix(posY), z = fix(posZ);
                 for (int dx=-16;dx<=16;dx++) for (int dy=-16;dy<=16;dy++) for (int dz=-16;dz<=16;dz++) if (mapXGetId(x+dx,y+dy,z+dz)==51) mapXSetIdMeta(x+dx,y+dy,z+dz,0,0);
             }
         }
         if (!isMultiplayer) {
             /*
             if (!optCheatFallDamage) setFall(player, 0f);
             boolean arrowChk = true;
             if (optCheatInfArrows || optCheatInfArmor || optCheatInfSword || optCheatInfTools) for (int slot=0;slot<invItemsArr.length;slot++) if (invItemsArr[slot] != null) {
                 ItemStack items = invItemsArr[slot];
                 int id = getItemsId(items);
                 if (id < 256 || id >= cheatItems) continue;
                 if (optCheatInfArrows && id==262 && arrowChk) {
                     arrowChk = false;
                     int count = getItemsCount(items);
                     if (cheatArrowCount - 1 == count) setItemsCount(items, ++count);
                     cheatArrowCount = count;
                 } else if (cheatDamage[id]) setItemsInfo(items, 0);
             }
             if (optCheatInfArmor) for (int slot=0;slot<invArmorsArr.length;slot++) if (invArmorsArr[slot] != null) {
                 int id = getItemsId(invArmorsArr[slot]);
                 if (id < 256 || id >= cheatItems) continue;
                 if (cheatDamage[id]) setItemsInfo(invArmorsArr[slot], 0);
             }
             */
         }
     }
 
     private static void drawModCheat(float x, float y, float z, List list) {
         if (!modCheatActive) return;
         if (!cheatShowMobs && !cheatShowOres && !optCheatShowHealth && !optInfoShowHealth) return;
         float px = (float)posX, py = (float)posY, pz = (float)posZ, mx, my, mz, dx, dy, dz;
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         GL11.glDisable(GL11.GL_BLEND);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         if (!isMultiplayer && ((cheating && optCheatShowHealth) || optInfoShowHealth)) {
             GL11.glColor3ub((byte)0, (byte)128, (byte)0);
             GL11.glBegin(GL11.GL_QUADS);
             Iterator it=list.iterator();
             while (it.hasNext()) {
                 Object obj = it.next();
                 if (!(obj instanceof EntityLiving) || obj == player) continue;
                 EntityLiving ent = (EntityLiving)obj;
                 int health = getHealth(ent);
                 mx = (float)getEntityPrevPosX(ent); my = (float)getEntityPrevPosY(ent) + 0.4f; mz = (float)getEntityPrevPosZ(ent);
                 my += getEntityHeight(ent);
                 dx = mz - pz;
                 dz = -(mx - px);
                 float d = (float)Math.sqrt(dx*dx + dz*dz);
                 float pos = 0.25f * health;
                 mx -= x; my -= y; mz -= z;
                 if (d < 0.1f || d > 64f) continue;
                 dx /= d; dz /= d;
                 while (health>0) {
                     float ax1, ax2, az1, az2;
                     pos -= 0.3f;
                     ax1 = pos * dx * 0.1f; az1 = pos * dz * 0.1f;
                     pos -= 0.7f;
                     ax2 = pos * dx * 0.1f; az2 = pos * dz * 0.1f;
                     GL11.glVertex3f(mx+ax1, my-0.1f, mz+az1);
                     GL11.glVertex3f(mx+ax2, my-0.1f, mz+az2);
                     GL11.glVertex3f(mx+ax2, my+(health == 1 ? 0f : 0.1f), mz+az2);
                     GL11.glVertex3f(mx+ax1, my+(health == 1 ? 0f : 0.1f), mz+az1);
                     health -= 2;
                 }
             }
             GL11.glEnd();
         }
         GL11.glDisable(GL11.GL_DEPTH_TEST);
         GL11.glDisable(GL11.GL_FOG);
         if (modCheatAllowed && cheating && cheatShowMobs) {
             Iterator it=list.iterator();
             float range = optCheatShowMobsRange * optCheatShowMobsRange;
 //GL11.glEnable(GL11.GL_LINE_STIPPLE);
 //GL11.glLineStipple(1, (short)0x5555);
             GL11.glBegin(GL11.GL_LINES);
             while (it.hasNext()) {
                 Entity ent = (Entity)it.next();
                 int type = getEntityType(ent);
                 if (cheatMobs[type] == null || (ent == player && cheatView == null) || ent == cheatView) continue;
                 px = (float)getEntityPosX(ent);
                 py = (float)getEntityPosY(ent) - ent.yOffset;
                 pz = (float)getEntityPosZ(ent);
                 mx = (float)getEntityPrevPosX(ent);
                 my = (float)getEntityPrevPosY(ent) - ent.yOffset;
                 mz = (float)getEntityPrevPosZ(ent);
                 mx = mx + (px - mx) * frameDelta;
                 my = my + (py - my) * frameDelta;
                 mz = mz + (pz - mz) * frameDelta;
                 dx = mx - x; dy = my - y; dz = mz - z;
                 if (optCheatShowMobsRange > 0 && dx*dx + dy*dy + dz*dz > range) continue;
                 GL11.glColor3ub(cheatMobs[type].r, cheatMobs[type].g, cheatMobs[type].b);
                 GL11.glVertex3f(dx,dy,dz); GL11.glVertex3f(dx,dy+(optCheatShowMobsSize || !(ent instanceof EntityLiving) ? getEntityHeight(ent) : 2.0f),dz);
             }
             GL11.glEnd();
 //GL11.glDisable(GL11.GL_LINE_STIPPLE);
         }
         GL11.glBegin(GL11.GL_LINES);
         if (modCheatAllowed && cheating && cheatShowOres) {
             cheatRefresh += seconds;
             if (cheatRefresh > 0.3f) {
                 cheatReCheck(fix(x), fix(y), fix(z));
                 cheatRefresh -= 0.3f;
             }
             for (int i=0;i<cheatCur;i++) {
                 Mark got = cheatMark[i];
                 if (got == null) continue;
                 GL11.glColor3ub(got.r,got.g,got.b);
                 mx = got.x - x; my = got.y - y; mz = got.z - z;
                 GL11.glVertex3f(mx+0.25f,my+0.25f,mz+0.25f); GL11.glVertex3f(mx-0.25f,my-0.25f,mz-0.25f);
                 GL11.glVertex3f(mx+0.25f,my+0.25f,mz-0.25f); GL11.glVertex3f(mx-0.25f,my-0.25f,mz+0.25f);
                 GL11.glVertex3f(mx+0.25f,my-0.25f,mz+0.25f); GL11.glVertex3f(mx-0.25f,my+0.25f,mz-0.25f);
                 GL11.glVertex3f(mx+0.25f,my-0.25f,mz-0.25f); GL11.glVertex3f(mx-0.25f,my+0.25f,mz+0.25f);
             }
         }
         GL11.glEnd();
     }
     
     private static String textModCheat(String txt) {
         if (!modCheatAllowed || !modCheatActive || !cheating || tagCheater.length()==0) return txt;
         return txt + tagCheater + " ";
     }
     
     private static void cheatReCheck(int pX, int pY, int pZ) {
         cheatCur = 0;
         for (int x = pX - optCheatShowOresRangeH; x < pX + optCheatShowOresRangeH; ++x) 
         for (int y = pY - optCheatShowOresRangeV; y < pY + optCheatShowOresRangeV; ++y) 
         for (int z = pZ - optCheatShowOresRangeH; z < pZ + optCheatShowOresRangeH; ++z) {
             int id = mapXGetId(x,y,z);
             if (id > cheatOres.length) continue;
             Mark color = cheatOres[id];
             if (color == null) continue;
             cheatMark[cheatCur++] = new Mark(x,y,z,color);
             if (cheatCur == cheatMax) return;
         }
     }
 
     private static void cheatOnClientPlayerUpdate(EntityPlayer ent) {
         if (!modCheatActive || !modCheatAllowed) return;
         if (cheatProjection != null && getView() == cheatProjection) {
             cheatProjection.onUpdate();
         }
     }
 
     private static void cheatOnServerPlayerUpdate(EntityPlayer ent) {
         if (!modCheatActive || !modCheatAllowed) return;
         ent.capabilities.disableDamage = cheating && optCheatDisableDamage || ent.capabilities.isCreativeMode;
         ent.isImmuneToFire = cheating && optCheatFireImmune;
         if (cheating && optCheatRestoreHealth && getHealth(ent) < getMaxHealth(ent)) setHealth(ent, getHealth(ent)+1);
         if (cheating && optCheatNoAir) setAir(ent, 300);
         if (cheating && !optCheatFallDamage) setFall(ent, 0);
     }
 
     public static boolean isControllingPlayer() {
         return getView() != cheatProjection || cheatProjectionLock;
     }
 
     public static boolean isControllingProjection() {
         return getView() == cheatProjection && !cheatProjectionLock;
     }
 
     public static int blockHitDelay() {
         return (modCheatActive && cheating) ? optCheatBlockHitDelay : 5;
     }
 
     public static boolean allowSwing() {
         return isControllingPlayer() || optCheatProjectionSwing;
     }
 
     public static boolean allowItemSync() {
         return isControllingPlayer() || !optCheatProjectionSpoof;
     }
 
     public static boolean isCheating(){
         return cheating;
     }
 
     public static boolean onSetAngles(float par1, float par2) {
         if (player == null || isControllingPlayer()) return true;
         cheatProjection.setAngles(par1,par2);
         return false;
     }
 
     public static void beforeBlockDig() {
         if (isControllingProjection() && optCheatProjectionSpoof) {
             if (!cheatUnspoof) getPlayerController().switchToRealItem();
             cheatUnspoof = true;
         }
     }
     public static void afterBlockDig() {
         if (isControllingProjection() && optCheatProjectionSpoof) {
             if (cheatUnspoof) getPlayerController().switchToIdleItem();
             cheatUnspoof = false;
         }
     }
 
     public static void beforeBlockPlace() {
         if (isControllingProjection() && optCheatProjectionSpoof) {
             if (!cheatUnspoof) getPlayerController().switchToRealItem();
             cheatUnspoof = true;
         }
         if (isControllingProjection() && optCheatProjectionPlace) {
             cheatYaw = getEntityYaw(player);
             cheatPitch = getEntityPitch(player);
             setEntityYaw(player, getEntityYaw(cheatProjection));
             setEntityPitch(player, getEntityPitch(cheatProjection));
             player.sendMotionUpdates();
         }
     }
     public static void afterBlockPlace() {
         if (isControllingProjection() && optCheatProjectionSpoof) {
             if (cheatUnspoof) getPlayerController().switchToIdleItem();
             cheatUnspoof = false;
         }
         if (isControllingProjection() && optCheatProjectionPlace) {
             setEntityYaw(player, cheatYaw);
             setEntityPitch(player, cheatPitch);
             player.sendMotionUpdates();
         }
     }
     
     public static class EntityPlayerGhost extends EntityPlayer {
         private boolean isItemInUse = false;
         private EntityPlayer playerBody;
         private MovementInput movementInput;
         protected Minecraft mc;
 
         public EntityPlayerGhost(World par1World, EntityPlayer entityPlayer) {
             super(par1World);
             this.mc = minecraft;
             this.playerBody = entityPlayer;
             this.movementInput = new MovementInput();
             if (entityPlayer != null) {
                 this.username = entityPlayer.username + "'s projection";
                 this.skinUrl = entityPlayer.skinUrl;
             }
         }
         
         public void onUpdate() {
             this.lastTickPosX = this.posX;
             this.lastTickPosY = this.posY;
             this.lastTickPosZ = this.posZ;
             this.prevRotationYaw = this.rotationYaw;
             this.prevRotationPitch = this.rotationPitch;
             super.onUpdate();
         }
 
         public void onLivingUpdate() {
             if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
                 this.posX = this.posZ = 0.5D;
                 this.posX = 0.0D;
                 this.posZ = 0.0D;
                 this.rotationYaw = (float)this.ticksExisted / 12.0F;
                 this.rotationPitch = 10.0F;
                 this.posY = 68.5D;
             } else {
                 this.inPortal = false;
                 this.movementInput.updatePlayerMoveState();
 
                 if (this.movementInput.sneak && this.ySize < 0.2F) {
                     this.ySize = 0.2F;
                 }
 
                 this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
                 this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
                 this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
                 this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
 
                 super.onLivingUpdate();
             }
         }
         
         /**
          * Gets the player's field of view multiplier. (ex. when flying)
          */
         public float getFOVMultiplier() {
             float var1 = (this.landMovementFactor * this.getSpeedModifier() / this.speedOnGround + 1.0F) / 2.0F;
             return var1;
         }
         
         /**
           * Checks if this entity is inside of an opaque block
          */
         public boolean isEntityInsideOpaqueBlock() {
             return false;
         }
         
         private boolean isBlockTranslucent(int par1, int par2, int par3) {
             return this.worldObj.isBlockNormalCube(par1, par2, par3);
         }
     
         /**
          * Adds velocity to push the entity out of blocks at the specified x, y, z position Args: x, y, z
          */
         protected boolean pushOutOfBlocks(double par1, double par3, double par5) {
             //-Zmod-Fly-noclip------------------------------------------------
             if (this.noClip) return false;
             //----------------------------------------------------------------
 
             int var7 = MathHelper.floor_double(par1);
             int var8 = MathHelper.floor_double(par3);
             int var9 = MathHelper.floor_double(par5);
             double var10 = par1 - (double)var7;
             double var12 = par5 - (double)var9;
 
             if (this.isBlockTranslucent(var7, var8, var9) || this.isBlockTranslucent(var7, var8 + 1, var9)) {
                 boolean var14 = !this.isBlockTranslucent(var7 - 1, var8, var9) && !this.isBlockTranslucent(var7 - 1, var8 + 1, var9);
                 boolean var15 = !this.isBlockTranslucent(var7 + 1, var8, var9) && !this.isBlockTranslucent(var7 + 1, var8 + 1, var9);
                 boolean var16 = !this.isBlockTranslucent(var7, var8, var9 - 1) && !this.isBlockTranslucent(var7, var8 + 1, var9 - 1);
                 boolean var17 = !this.isBlockTranslucent(var7, var8, var9 + 1) && !this.isBlockTranslucent(var7, var8 + 1, var9 + 1);
                 byte var18 = -1;
                 double var19 = 9999.0D;
 
                 if (var14 && var10 < var19) {
                     var19 = var10;
                     var18 = 0;
                 }
                 if (var15 && 1.0D - var10 < var19) {
                     var19 = 1.0D - var10;
                     var18 = 1;
                 }
                 if (var16 && var12 < var19) {
                     var19 = var12;
                     var18 = 4;
                 }
                 if (var17 && 1.0D - var12 < var19) {
                     var19 = 1.0D - var12;
                     var18 = 5;
                 }
 
                 float var21 = 0.1F;
 
                 if (var18 == 0) {
                     this.motionX = (double)(-var21);
                 }
                 if (var18 == 1) {
                     this.motionX = (double)var21;
                 }
                 if (var18 == 4) {
                     this.motionZ = (double)(-var21);
                 }
                 if (var18 == 5) {
                     this.motionZ = (double)var21;
                 }
             }
 
             return false;
         }
 
         /**
          * Performs a ray trace for the distance specified and using the partial tick time. Args: distance, partialTickTime
          */
         public MovingObjectPosition rayTrace(double distance, float delta) {
             MovingObjectPosition objectMouseOver = super.rayTrace(actualPlayerReach() +1, delta);
             if (objectMouseOver == null || objectMouseOver.sideHit == -1) return objectMouseOver;
             int x = objectMouseOver.blockX;
             int y = objectMouseOver.blockY;
             int z = objectMouseOver.blockZ;
             if (!digCheckReachDig(x,y,z) && !digCheckReachUse(x,y,z,objectMouseOver.sideHit)) return null;
             return objectMouseOver;
         }
         
         public void placeAt(Entity ent) {
             setEntityPos(this, ent.posX, ent.boundingBox.minY + this.yOffset - this.ySize, ent.posZ);
             this.rotationYaw = ent.rotationYaw;
             this.rotationPitch = ent.rotationPitch;
             this.prevRotationYaw = ent.prevRotationYaw;
             this.prevRotationPitch = ent.prevRotationPitch;
             this.cameraYaw = 0;
             this.cameraPitch = 0;
             this.motionX = 0;
             this.motionY = 0;
             this.motionZ = 0;
         }
 
         public void placeAt(double x, double y, double z) {
             setEntityPos(this, x, y, z);
             this.motionX = 0;
             this.motionY = 0;
             this.motionZ = 0;
         }
         
         public ItemStack getHeldItem() {
             return playerBody.getHeldItem();
         }
         
         public void updateEntityActionState() {
             super.updateEntityActionState();
             this.moveStrafing = this.movementInput.moveStrafe;
             this.moveForward = this.movementInput.moveForward;
             this.isJumping = this.movementInput.jump;
         }
         
 
         public ChunkCoordinates getPlayerCoordinates() {
             return new ChunkCoordinates(MathHelper.floor_double(this.posX + 0.5D), 
                                         MathHelper.floor_double(this.posY + 0.5D), 
                                         MathHelper.floor_double(this.posZ + 0.5D));
         }
         
         protected boolean isClientWorld() { return true; }
         protected boolean isPVPEnabled() { return false; }
         public void sendChatToPlayer(String par1Str) {}
         public boolean canCommandSenderUseCommand(int par1, String par2Str) { return false; }
         public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) { return false; }
         protected int applyPotionDamageCalculations(DamageSource par1DamageSource, int par2) { return 0; }
     }
     
     //=ZMod=Resize============================================================
     private static boolean modResizeEnabled, modResizeActive;
     private static int resizeChanceBig[], resizeChanceSmall[];
     private static float resizeSize[];
 
     private static boolean initModResize() {
         if (!checkStatus(MOD_RESIZE, "resize")) return false;
         log("info: loading config for \"resize\"");
         resizeChanceBig = new int[MAXTYPE]; resizeChanceSmall = new int[MAXTYPE]; resizeSize = new float[MAXTYPE];
         for (int i=0;i<MAXTYPE;i++) resizeChanceBig[i] = 100;
         // big
         resizeChanceBig[COW]    = 100 - getInt("optResizeCowBig"   , 10, 0, 100);
         resizeChanceBig[SPIDER] = 100 - getInt("optResizeSpiderBig", 10, 0, 100);
         resizeChanceBig[SHEEP]  = 100 - getInt("optResizeSheepBig" , 10, 0, 100);
         resizeChanceBig[SKELLY] = 100 - getInt("optResizeSkellyBig", 20, 0, 100);
         resizeChanceBig[ZOMBIE] = 100 - getInt("optResizeZombieBig", 20, 0, 100);
         resizeChanceBig[PIG]    = 100 - getInt("optResizePigBig"   , 10, 0, 100);
         // small
         resizeChanceSmall[COW]    = getInt("optResizeCowSmall"   , 30, 0, 100);
         resizeChanceSmall[SPIDER] = getInt("optResizeSpiderSmall", 50, 0, 100);
         resizeChanceSmall[SHEEP]  = getInt("optResizeSheepSmall" , 30, 0, 100);
         resizeChanceSmall[SKELLY] = getInt("optResizeSkellySmall", 10, 0, 100);
         resizeChanceSmall[ZOMBIE] = getInt("optResizeZombieSmall", 30, 0, 100);
         resizeChanceSmall[PIG]    = getInt("optResizePigSmall"   , 50, 0, 100);
         return modResizeActive = true;
     }
     
     private static void quitModResize() {
         if (!modResizeActive) return;
         modResizeActive = false;
     }
 
     private static void optionsModResize() {
         boolean wasEnabled = modResizeEnabled;
         modResizeEnabled = getSetBool(modResizeEnabled, "modResizeEnabled", false,     "Enable Resize mod");
         if (wasEnabled != modResizeEnabled) {
             if (modResizeEnabled && !modResizeActive) initModResize(); 
             if (!modResizeEnabled && modResizeActive) quitModResize();
         }
     }
 
     private static void updateResizeMod(List list) {
         if (!modResizeActive || isMultiplayer) return;
         Iterator it = list.iterator();
         int chance;
         while (it.hasNext()) {
             Entity obj = (Entity)it.next();
             if (getEntityAge(obj)!=1) continue; // not new entity
             int type = getEntityType(obj);
             if (type==0 || type==LIVING) continue; // bad entity type
             EntityLiving ent = (EntityLiving)obj;
             int max = ent.getMaxHealth(), health = getHealth(ent);
             float height = ent.height;
             if (resizeSize[type] < 0.01f) resizeSize[type] = getEntityHeight(ent); // store original size
             if (max == health) {
                 chance = rnd.nextInt(100);
                 if (resizeChanceSmall[type]>chance) { health >>= 1; height = resizeSize[type] * 0.5f; }
                 else if (resizeChanceBig[type]<chance) { health <<= 1; height = resizeSize[type] * 1.5f; }
             }
             else if ((max<<1) == health) height = resizeSize[type] * 0.5f;
             else if ((max>>1) == health) height = resizeSize[type] * 1.5f;
             else continue;
             setEntitySize(ent, height, health);
         }
     }
     
     public static void resizeHandle(EntityLiving ent) {
         if (!modResizeActive || isMultiplayer || resizeSize==null) return;
         float resize = resizeSize[getEntityType(ent)];
         if (resize<=0.000001f) return;
         float scale = getEntityHeight(ent) / resize;
         GL11.glScalef(scale, scale, scale);
     }
     
     
     //=ZMod=Furnace===========================================================
     private static boolean modFurnaceEnabled, modFurnaceActive;
     private static boolean optFurnaceFuelWaste, optFurnaceReturnBucket;
     private static int optFurnaceWoodFuel, optFurnaceInfiniteFuel, optFurnaceSmeltingTime;
     private static HashMap<Integer,Integer> furnaceFuel;
     private static HashMap<Integer,ItemStack> furnaceSmelting;
 
     private static boolean initModFurnace() {
         if (!checkStatus(MOD_FURNACE, "furnace")) return false;
         log("info: loading config for \"furnace\"");
         pFuel = furnaceFuel = new HashMap<Integer,Integer>();
         pSmelt = furnaceSmelting = new HashMap<Integer,ItemStack>();
         optFurnaceWoodFuel = getInt("optFurnaceWoodFuel", 300, 1, 32767);
         optFurnaceInfiniteFuel = getInt("optFurnaceInfiniteFuel", 32767, 1, 32767);
         parse(null, "fuel.txt", FUEL);
         parse(null, "smelting.txt", SMELTING);
         return modDigActive = true;
     }
     
     private static void quitModFurnace() {
         if (!modFurnaceActive) return;
         modFurnaceActive = false;
         pFuel = furnaceFuel = null;
         pSmelt = furnaceSmelting = null;
     }
     
     private static void optionsModFurnace() {
         boolean wasEnabled = modFurnaceEnabled;
         modFurnaceEnabled = getSetBool(modFurnaceEnabled, "modFurnaceEnabled", false,     "Enable Furnace mod");
         if (wasEnabled != modFurnaceEnabled) {
             if (modFurnaceEnabled && !modFurnaceActive) initModFurnace(); 
             if (!modFurnaceEnabled && modFurnaceActive) quitModFurnace();
         }
         optFurnaceSmeltingTime = getSetInt(optFurnaceSmeltingTime, "optFurnaceSmeltingTime", 200, 1, 1000, "Smelting time (20 = 1second)") & 0xfffe;
         if (optFurnaceSmeltingTime == 0) optFurnaceSmeltingTime = 1;
         optFurnaceFuelWaste = getSetBool(optFurnaceFuelWaste, "optFurnaceFuelWaste", true, "Fuel waste");
         optFurnaceReturnBucket = getSetBool(optFurnaceReturnBucket, "optFurnaceReturnBucket", false, "Return bucket from lava bucket");
     }
 
     public static boolean furnaceWasteHandle() { return isMultiplayer || !modFurnaceActive || optFurnaceFuelWaste; }
     public static int furnaceSmeltTimeHandle() { return isMultiplayer || !modFurnaceActive ? 200 : optFurnaceSmeltingTime; }
     public static boolean furnaceUseFuelHandle(int fuel, boolean canSmelt) { return isMultiplayer || !modFurnaceActive || (optFurnaceInfiniteFuel > fuel && (optFurnaceFuelWaste || canSmelt)); }
     public static ItemStack furnaceSmeltingHandle(Integer id) { if (modFurnaceActive && !isMultiplayer && furnaceSmelting!=null) return furnaceSmelting.get(id); return null; }
     public static int furnaceWoodFuelHandle() { return !isMultiplayer && modFurnaceActive ? optFurnaceWoodFuel : 300; }
     public static int furnaceFuelHandle(Integer id) { return !isMultiplayer && modFurnaceActive && furnaceFuel!=null && furnaceFuel.containsKey(id) ? furnaceFuel.get(id) : 0; }
     public static boolean furnaceWorldUpdateHandle(boolean mustBurn, int x, int y, int z) { return mustBurn != (mapXGetId(x,y,z)==62); }
     public static ItemStack furnaceDecFuelHandle(ItemStack items) {
         int count = getItemsCount(items);
         if (modFurnaceActive && !isMultiplayer && optFurnaceReturnBucket && getItemsId(items) == 327) return newItems(325, count);
         if (count == 1) return null;
         setItemsCount(items, count-1);
         return items;
     }
 
     //=ZMod=Dig===============================================================
     private static boolean modDigEnabled, modDigActive;
     private static boolean optDigHarvestAlways, optDigCheckReach, optDigSyncDigged;
     private static boolean optDigReachSet, optDigReachSetDig, optDigReachSetUse;
     private static float optDigSpeed, optDigReach, optDigReachDig, optDigReachUse;
     private static boolean featureCustomReachAvailable;
 
     private static boolean initModDig() {
         if (!checkStatus(MOD_DIG, "dig")) return false;
         log("info: loading config for \"dig\"");
         featureCustomReachAvailable = checkClass(NetServerHandler.class) && checkClass(PlayerControllerMP.class);
         return modDigActive = true;
     }
     
     private static void quitModDig() {
         if (!modDigActive) return;
         modDigActive = false;
     }
 
     private static void optionsModDig() {
         boolean wasEnabled = modDigEnabled;
         modDigEnabled = getSetBool(modDigEnabled, "modDigEnabled", false,     "Enable Dig mod");
         if (wasEnabled != modDigEnabled) {
             if (modDigEnabled && !modDigActive) initModDig(); 
             if (!modDigEnabled && modDigActive) quitModDig();
         }
         optDigCheckReach = getSetBool(optDigCheckReach, "optDigCheckReach", false, "Cancel out of reach actions");
         optDigSyncDigged = false; // broken as of 1.4.6, they removed the BlockDig Request-status (3)
         //optDigSyncDigged = getSetBool(optDigSyncDigged, "optDigSyncDigged", false, "Synchronize newly digged blocks");
         optDigHarvestAlways = false;
         //optDigHarvestAlways = getSetBool(optDigHarvestAlways, "optDigHarvestAlways", false, "Always harvest, regardless of tool");
         optDigReachSet = getSetBool(optDigReachSet, "optDigReachSet", false, "Set arm reach globally");
         optDigReach = getSetFloat(optDigReach, "optDigReach", 6f, 2f, 64f, "Global arm reach value");
         optDigReachSetDig = getSetBool(optDigReachSetDig, "optDigReachSetDig", false, "Set arm reach for digging blocks");
         optDigReachDig = getSetFloat(optDigReachDig, "optDigReachDig", 6f, 2f, 64f, "Dig arm reach value (default 6)");
         optDigReachSetUse = getSetBool(optDigReachSetUse, "optDigReachSetUse", false, "Set arm reach for using or placing blocks");
         optDigReachUse = getSetFloat(optDigReachUse, "optDigReachUse", 8f, 2f, 64f, "Use arm reach value (default 8)");
         optDigSpeed = 1f;
         //optDigSpeed = getSetFloat(optDigSpeed, "optDigSpeed", 2.0f, 0.1f, 10.0f, "Digging speed multiplier");
     }
 
     /*
     public static float digReachHandle() {
         float res = 4f;
         if (!isMultiplayer) {
             if (modDigActive && res < optDigReach) res = optDigReach;
             if (optBuild && modBuildActive && res < optBuildReach) res = optBuildReach;
         }
         return res;
     }
     
     public static boolean harvestableHandle(boolean harvest) {
         if (modDigActive && optDigHarvestAlways) harvest = true;
         if (modBuildActive && optBuild) {
             if (optBuildHarvestRule == -1) harvest = false;
             else if (optBuildHarvestRule == 1) harvest = true;
         }
         return harvest;
     }
 
     public static float digProgressHandle(float progress, int blockId) {
         if (modBuildActive && optBuild) return (block[blockId] & TOUCH) != 0 ? 0.1f : optBuildDigSpeed;
         else if (ZMod.modDigActive) return progress * optDigSpeed;
         return progress;
     }
     */
 
     public static float actualReachDig() {
         float reach = 6;
         if (modDigActive) {
             if (!isMultiplayer && featureCustomReachAvailable) {
                 if (optDigReachSet) reach = optDigReach;
                 if (optDigReachSetDig) reach = optDigReachDig;
             }
         }
         return reach;
     }
     public static float actualReachDigSq() {
         float reach = actualReachDig();
         return reach*reach;
     }
 
     public static float actualReachUse() {
         float reach = 8;
         if (modDigActive) {
             if (!isMultiplayer && featureCustomReachAvailable) {
                 if (optDigReachSet) reach = optDigReach;
                 if (optDigReachSetUse) reach = optDigReachUse;
             }
         }
         return reach;
     }
     public static float actualReachUseSq() {
         float reach = actualReachUse();
         return reach*reach;
     }
 
     public static float actualPlayerReach() {
         float reach = 4.5f;
         if (modDigActive) {
             reach = java.lang.Math.min(actualReachDig()-1.5f, actualReachUse());
         }
         if (player != null && player.capabilities.isCreativeMode) reach += 0.5;
         return reach;
     }
 
     public static boolean digCheckReachDig(int x, int y, int z) {
         if (!modDigActive || player == null || !optDigCheckReach) return true;
         double dx = player.posX - (x + 0.5);
         double dy = player.posY - (y + 0.5) + 1.5;
         double dz = player.posZ - (z + 0.5);
         double dist = dx*dx + dy*dy + dz*dz;
         return dist <= actualReachDigSq();
     }
 
     public static boolean digCheckReachUse(int x, int y, int z, int side) {
         if (!modDigActive || player == null || !optDigCheckReach) return true;
         double dx = player.posX - (x + 0.5);
         double dy = player.posY - (y + 0.5);
         double dz = player.posZ - (z + 0.5);
         double dist = dx*dx + dy*dy + dz*dz;
         return dist < actualReachUseSq();
     }
 
     /*
     private static Packet makeBlockRequestPacket(int x, int y, int z) {
         return new Packet14BlockDig(3, x,y,z, -1);
     }
 
     private static void askBlockInfo(int x, int y, int z) {
         queuePacket(makeBlockRequestPacket(x,y,z));
     }
     */
     private static void askBlockInfo(int x, int y, int z) {
         // DNAA. broken feature as of 1.4.6
     }
 
     private static void digOnBlockDigged(int x, int y, int z, int side) {
         if (!modDigActive || player == null) return;
         if (optDigSyncDigged) {
             askBlockInfo(x,y,z);
         }
     }
 
     public static void onBlockDigged(int x, int y, int z, int side) {
         digOnBlockDigged(x,y,z,side);
     }
     
     //=ZMod=Weather===========================================================
     private static boolean modWeatherEnabled, modWeatherActive;
     private static int keyWeatherRain, keyWeatherThunderstorm, keyWeatherMayhem, keyWeatherLightning;
     private static boolean optWeatherLocked, optWeatherNoDraw;
     private static int optWeatherThunderChance, optWeatherThunderMayhemChance;
     private static Mark optWeatherRainTime, optWeatherNoRainTime, optWeatherThunderTime, optWeatherNoThunderTime;
     private static String tagWeatherRaining, tagWeatherThundering, tagWeatherMayhem;
     private static boolean weatherMayhem;
 
     private static boolean initModWeather() {
         if (!checkStatus(MOD_WEATHER, "weather")) return false;
         log("info: loading config for \"weather\"");
         optWeatherThunderChance = getInt("optWeatherThunderChance", 100000, 1, 500000);
         optWeatherThunderMayhemChance = getInt("optWeatherThunderMayhemChance", 2000, 1, 10000);
         optWeatherRainTime = getIntRange("optWeatherRainTime", 180, 600, 10, 3600);
         optWeatherRainTime.min *= 20; optWeatherRainTime.max *= 20; optWeatherRainTime.max -= optWeatherRainTime.min - 1; // -1 to prevent the case of Random.nextInt(0)
         optWeatherNoRainTime = getIntRange("optWeatherNoRainTime", 600, 8400, 10, 14400);
         optWeatherNoRainTime.min *= 20; optWeatherNoRainTime.max *= 20; optWeatherNoRainTime.max -= optWeatherNoRainTime.min - 1;
         optWeatherThunderTime = getIntRange("optWeatherThunderTime", 180, 600, 10, 3600);
         optWeatherThunderTime.min *= 20; optWeatherThunderTime.max *= 20; optWeatherThunderTime.max -= optWeatherThunderTime.min - 1;
         optWeatherNoThunderTime = getIntRange("optWeatherNoThunderTime", 600, 8400, 10, 14400);
         optWeatherNoThunderTime.min *= 20; optWeatherNoThunderTime.max *= 20; optWeatherNoThunderTime.max -= optWeatherNoThunderTime.min - 1;
         tagWeatherRaining = getString("tagWeatherRaining", "raining");
         tagWeatherThundering = getString("tagWeatherThundering", "thunder");
         tagWeatherMayhem = getString("tagWeatherMayhem", "mayhem");
         return modWeatherActive = true;
     }
     
     private static void quitModWeather() {
         if (!modWeatherActive) return;
         modWeatherActive = false;
     }
     
     private static void optionsModWeather() {
         boolean wasEnabled = modWeatherEnabled;
         modWeatherEnabled = getSetBool(modWeatherEnabled, "modWeatherEnabled", false,     "Enable Weather mod");
         if (wasEnabled != modWeatherEnabled) {
             if (modWeatherEnabled && !modWeatherActive) initModWeather(); 
             if (!modWeatherEnabled && modWeatherActive) quitModWeather();
         }
         keyWeatherRain = getSetBind(keyWeatherRain, "keyWeatherRain",            Keyboard.KEY_J, "Toggle rain");
         keyWeatherThunderstorm = getSetBind(keyWeatherThunderstorm, "keyWeatherThunderstorm", Keyboard.KEY_K, "Toggle thunderstorm");
         keyWeatherMayhem = getSetBind(keyWeatherMayhem, "keyWeatherMayhem",        Keyboard.KEY_LSHIFT, "Mayhem modifier");
         keyWeatherLightning = getSetBind(keyWeatherLightning, "keyWeatherLightning",  Keyboard.KEY_U, "Spawn lightning at cursor");
         optWeatherLocked = getSetBool(optWeatherLocked, "optWeatherLocked", false, "Lock natural weather changes");
         optWeatherNoDraw = getSetBool(optWeatherNoDraw, "optWeatherNoDraw", false, "Do not draw rain");
     }
 
     private static void updateModWeather() {
         if (!modWeatherActive || isMultiplayer) return;
         // spawn lightning
         if (!isMenu && keyPress(keyWeatherLightning) && rayTrace(256d, 0f)) {
             int x = rayHitX(),y = rayHitY(),z = rayHitZ(), s = rayHitSide();
             if (s==2) z--; if (s==3) z++; if (s==4) x--; if (s==5) x++;
             while ((block[mapXGetId(x,y,z)] & SOLID) == 0) y--;
             while ((block[mapXGetId(x,y,z)] & SOLID) != 0) y++;
             spawnLightning(x, y, z);
         }
         // weather control
         overloadMapRandom();
         int wrt = getRainTime(), wtt = getThunderTime();
         boolean wr = getRain(), wt = getThunder(), boost, raining, thundering;
         if (!isMenu && keyPress(keyWeatherRain)) { wrt = 1; }
         else if (!isMenu && keyPress(keyWeatherThunderstorm)) {
             // intended behaviour: 0/1 initial state (key down in "boost" case). state change: "+" start, "-" end, "." no-change, "!" flip.
             // raining     0+ 1. 0+ 1. 0+ 1. 0+ 1.
             // thundering  0+ 0+ 1+ 1- 0+ 0+ 1+ 1.
             // boost       0. 0. 0. 0. 1+ 1+ 1+ 1!
             boost = keyDown(keyWeatherMayhem); raining = wr; thundering = wt;
             if (!raining) { wrt = 1; wr = false; } // adjust raining
             if (!(raining && thundering)) { wtt = 1; wt = false; } else if (!boost) { wtt = 1; wt = true; } // adjust thundering
             if (boost) weatherMayhem = !(raining && thundering) || !weatherMayhem; // adjust weather mayhem
         }
         if (wrt == 0) { wr = true; } // minecraft seems to reset all weather whenever one sleeps causing it to never rain in the morning - it seems :/ ... WHY!?
         if (wrt <= 1) {
             wr = !wr;
             if (wr) wrt = rnd.nextInt(optWeatherRainTime.max) + optWeatherRainTime.min;
             else { wrt = rnd.nextInt(optWeatherNoRainTime.max) + optWeatherNoRainTime.min; weatherMayhem = false; }
         }
         if (wtt <= 1) {
             wt = !wt;
             if (wt) wtt = rnd.nextInt(optWeatherThunderTime.max) + optWeatherThunderTime.min;
             else { wtt = rnd.nextInt(optWeatherNoThunderTime.max) + optWeatherNoThunderTime.min; weatherMayhem = false; }
         }
         if (!isHell && optWeatherLocked) { wrt++; wtt++; } // weather timers are stopped in hell
         setRain(wr); setThunder(wt); setRainTime(wrt); setThunderTime(wtt);
     }
 
     private static String textModWeather(String txt) {
         if (!modWeatherActive || isMultiplayer || !getRain()) return txt;
         return txt + (getThunder() ? (weatherMayhem ? tagWeatherMayhem : tagWeatherThundering) : tagWeatherRaining) + " ";
     }
     
     public static boolean forwardRenderRainSnow() {
         return !optWeatherNoDraw;
     }
     
     public static void beginRenderRainSnow(float delta) {
         ZMod.drawModsRender((float) delta);
     }
     
     public static void endRenderRainSnow(float delta) {
         
     }
     
     public static int mapRandomHandle(int n, int res) {
         if (n==0x186a0 && modWeatherActive && !isMultiplayer) {
             if (weatherMayhem) return rnd.nextInt(optWeatherThunderMayhemChance);
             return rnd.nextInt(optWeatherThunderChance);
         }
         return res;
     }
     
     
     //=ZMod=Growth============================================================
     private static boolean modGrowthEnabled, modGrowthActive;
     private static boolean optGrowthRooting, optGrowthPlanting;
     private static int optGrowthFlower, optGrowthShroom, optGrowthPumpkin, optGrowthSappling, optGrowthReed, optGrowthRootingSpace, optGrowthRootingTime;
     private static float growthSqrRadius;
 
     private static boolean initModGrowth() {
         if (!checkStatus(MOD_GROWTH, "growth")) return false;
         log("info: loading config for \"growth\"");
         optGrowthPlanting = getBool("optGrowthPlanting", true);
         optGrowthFlower = getInt("optGrowthFlower", 25, 1, 1000);
         optGrowthShroom = getInt("optGrowthShroom", 100, 1, 1000);
         optGrowthPumpkin = getInt("optGrowthPumpkin", 50, 1, 1000);
         optGrowthSappling = getInt("optGrowthSappling", 25, 1, 1000);
         optGrowthReed = getInt("optGrowthReed", 10, 1, 1000);
         optGrowthRootingSpace = getInt("optGrowthRootingSpace", 3, 1, 5);
         optGrowthRootingTime = getInt("optGrowthRootingTime", 10, 1, 300) * 20;
         growthSqrRadius = (0.5f + optGrowthRootingSpace) * (0.5f + optGrowthRootingSpace);
         return modGrowthActive = true;
     }
     
     private static void quitModGrowth() {
         if (!modGrowthActive) return;
         modGrowthActive = false;
     }
     
     private static void optionsModGrowth() {
         boolean wasEnabled = modGrowthEnabled;
         modGrowthEnabled = getSetBool(modGrowthEnabled, "modGrowthEnabled", false,     "Enable Growth mod");
         if (wasEnabled != modGrowthEnabled) {
             if (modGrowthEnabled && !modGrowthActive) initModGrowth(); 
             if (!modGrowthEnabled && modGrowthActive) quitModGrowth();
         }
         optGrowthRooting = getSetBool(optGrowthRooting, "optGrowthRooting", true, "Auto root sapplings");
     }
     
     private static void updateModGrowth(List list) {
         if (!modGrowthActive || isMultiplayer) return;
         // grow plants.
         int pX = fix(posX) >> 4, pZ = fix(posZ) >> 4;
         for (int cX=-15;cX<=15;cX++) for (int cZ=-15;cZ<=15;cZ++) if (mapXGetChunkExists(pX + cX, pZ + cZ)) {
             Chunk chunk = map.getChunkFromChunkCoords(pX + cX, pZ + cZ);
             int blockId, meta, pos = rnd.nextInt(256), X = pos & 15, Z = pos >> 4, Y = chunk.getHeightValue(X, Z) + 1; // is it first opaque or one before that?
             for (int i=1;i<=Y;i++) if ((block[blockId = chunk.getBlockID(X,i,Z)] & GROW)!=0 && chunk.getBlockID(X,i-1,Z)==3) {
                 int chance = Integer.MAX_VALUE;
                 switch(blockId) {
                     case 6:           chance = optGrowthSappling; break; // sappling
                     case 37: case 38: chance = optGrowthFlower;   break; // flower
                     case 39: case 40: chance = optGrowthShroom;   break; // shroom
                     case 83:          chance = optGrowthReed;     break; // reed
                     case 86:          chance = optGrowthPumpkin;  break; // pumpkin
                 }
                 if (chance >= 1000 || rnd.nextInt(chance) != 0) continue;
                 int rX = X + rnd.nextInt(3) - 1 + ((pX + cX) << 4), rZ = Z + rnd.nextInt(3) - 1 + ((pZ + cZ) << 4);
                 if (mapXGetId(rX, i - 1, rZ)!=3 || mapXGetId(rX, i, rZ)!=0) continue;
                 int light = getLightLevel(rX, i, rZ, 0);
                 if (blockId == 39 || blockId == 40) { if (light > 13) continue; } else if (light < 8) continue; // light check
                 meta = chunk.getBlockMetadata(X, i, Z);
                 mapXSetIdMeta(rX, i, rZ, blockId, meta);
             }
         }
         // root sapplings
         if (optGrowthRooting || optGrowthPlanting) {
             ArrayList<EntityItem> die = new ArrayList<EntityItem>();
             Iterator iter = list.iterator();
             GrowthList: while (iter.hasNext()) {
                 Object obj = iter.next();
                 if (!(obj instanceof EntityItem)) continue;
                 EntityItem ent = (EntityItem)obj;
                 int age = getEntityAge(ent);
                 if (age != optGrowthRootingTime && age != 5980) continue; // configurable     (items die at: 6000)
                 ItemStack items = getEntityItemStack(ent);
                 int stack = getItemsId(items);
                 if (getItemsCount(items) != 1 || (!(stack == 6 && optGrowthRooting) && !(stack == 295 && optGrowthPlanting)) ) continue;
                 int X = fix(getEntityPosX(ent)), Y = fix(getEntityPosY(ent)), Z = fix(getEntityPosZ(ent));
                 if (Y < 0 || mapXGetId(X, Y, Z) != 0) continue; // need empty space
                 if (getLightLevel(X, Y, Z, 0) < 8) continue; // need light (using day setting)
                 int blockId = mapXGetId(X, Y - 1, Z);
                 if (stack == 295) {
                     if (blockId != 60) continue; // need farmland to grow
                     // rooting
                     mapXSetIdMeta(X, Y, Z, 59, 0);
                 } else {
                     if (blockId != 2 && blockId != 3) continue; // need grass or dirt to grow
                     for (int aX=-optGrowthRootingSpace;aX<=optGrowthRootingSpace;aX++) for (int aZ=-optGrowthRootingSpace;aZ<=optGrowthRootingSpace;aZ++) {
                         if (aX * aX + aZ * aZ > growthSqrRadius) continue;
                         for (int aY=-optGrowthRootingSpace;aY<=optGrowthRootingSpace;aY++) {
                             blockId = mapXGetId(X + aX, Y + aY, Z + aZ);
                             if (blockId == 17 || blockId == 6) continue GrowthList;
                         }
                     }
                     // rooting
                     mapXSetIdMeta(X, Y, Z, 6, getItemsInfo(items));
                 }
                 die.add(ent);
             }
             for (int deathrow=0;deathrow<die.size();deathrow++)  dieEntity(die.get(deathrow));
         }
     }
     
 
     //#ZMod#Mark##############################################################
     private static final class Mark { // whore class
         public float x,y,z;
         public int min, max;
         public byte r,g,b,a;
         public Mark() { }
         // safe mark
         public Mark(int bx, int by, int bz, boolean sun) { x = 0.5f + bx; y = by + 0.13f; z = 0.5f + bz; r = sun ? (byte)1 : (byte)0; }
         // ore mark
         public Mark(int bx, int by, int bz, Mark c) { x = 0.5f + bx; y = 0.5f + by; z = 0.5f + bz; r = c.r; g = c.g; b = c.b; }
         // range mark
         public Mark(int a, int b) { min = a; max = b; }
         // color mark
         public Mark(int color) { loadColor(color); }
         public void loadColor(int color) { b = (byte)(color & 255); g = (byte)((color>>8) & 255); r = (byte)((color>>16) & 255); }
         public boolean loadColor(String color) {
             int c = names.containsKey(color) ? (Integer)(names.get(color)) : parseUnsigned(color);
             if (c < 0) return false;
             loadColor(c);
             return true;
         }
         // item mark
         public static Mark makeItem(int id) {
             Mark mark = new Mark();
             Item item;
             item = getItem(id); mark.setMaxStack(getItemMax(item)); mark.setMaxDamage(getItemDmgCap(item));
             if (id >= 256) return mark;
             mark.setLightEmission(getBlockLight(id)); mark.setLightReduction(getBlockOpacity(id));
             Block block = getBlock(id); mark.setStrength(getBlockStrength(block)); mark.setResistance(getBlockResist(block)); mark.setSlipperiness(getBlockSlip(block));
             mark.setFireBurn(getFireBurn(id)); mark.setFireSpread(getFireSpread(id));
             return mark;
         }
         public void setMaxStack(int val) { r = (byte)val; }
         public void setMaxDamage(int val) { max = val; }
         public void setLightEmission(int val) { g = (byte)val; }
         public void setLightReduction(int val) { min = val; }
         public void setStrength(float val) { x = val; }
         public void setResistance(float val) { y = val; }
         public void setSlipperiness(float val) { z = val; }
         public void setFireBurn(int val) { b = (byte)val; }
         public void setFireSpread(int val) { a = (byte)val; }
         public void activate(int id) {
             Item item;
             item = getItem(id); setItemMax(item, r); setItemDmgCap(item, max);
             if (id >= 256) return;
             setBlockLight(id, g); setBlockOpacity(id, min);
             Block block = getBlock(id); setBlockStrength(block, x); setBlockResist(block, y); setBlockSlip(block, z);
             ZMod.setFireSpread(id, a); ZMod.setFireBurn(id, b);
         }
     }
     
     private static final class Text {
         public String msg; public int x, y, color;
         public Text(String pmsg, int px, int py, int pcolor) { msg = pmsg; x = px; y = py; color = pcolor; }
     }
 
     //#ZMod#Options###########################################################
     private static final class Options extends GuiScreen {
     
         public Options() {
             optionSel = -1;
         }
     
         public void drawScreen(int par1, int par2, float par3) {
             optionsMods(this);
         }
         
         protected void keyTyped(char c, int key) {
             //if(optionSel == -1) super.keyTyped(c, key);     have to disable Escape completely - keys are checked at different times
         }
         
         //public boolean scrGetPauseGame() {    it defaults to pause and one can not pause multiplayer
         //    return !isMultiplayer;
         //}
 
     }
     
     private static void drawHelp(int x, int y, String help, int status) {
         int w = showTextLength(help);
         int colorOk = 0xcccccc, colorBroken = 0x999999, colorMissing = 0xcccc99;
         boolean hover = mouseX>=x && mouseY>=y && mouseX<x+w && mouseY<y+11;
         int color = colorOk;
         String statusmsg = null;
         if ((status & STATUS_BROKEN) != 0) {
             color = colorBroken;
             statusmsg = "This feature is broken";
         } else if ((status & STATUS_MISSING) != 0) {
             color = colorMissing;
             statusmsg = "Feature unavailable: missing class file";
         }
         showText(help, x, y, color);
         if (hover && statusmsg != null) {
             w = showTextLength(statusmsg);
             x += 10; y -= 11;
             opt.drawRect(x-1, y-1, x+w+1, y+11, 0x99000000);
             showText(statusmsg, x, y, 0xcc9999);
         }
     }
     
     private static boolean drawBtn(int x, int y, int w, String caption, String help, boolean selected, boolean state, boolean center, boolean restart, int status) {
         x *= 5; y *= 11; w *= 5; // add y offset if needed here
         int stateOn = 0xff66bb66, stateOff = 0xffbb6666, 
             stateBroken = 0xff666666, stateMissing = 0xff888866;
         boolean hover = mouseX>=x && mouseY>=y && mouseX<x+w && mouseY<y+11;
         //if (!optionsModEnabled) { caption = "???"; restart = true; center = true; }
         if (hover) { stateOn = 0xff88dd88; stateOff = 0xffdd8888;
                      stateBroken = 0xff888888; stateMissing = 0xffaaaa88; }
         int stateColor = state ? stateOn : stateOff;
         String statusmsg = null;
         if (help != null) {
         } else if ((status & STATUS_BROKEN) != 0) {
             stateColor = stateBroken;
             statusmsg = "This feature is broken";
         } else if ((status & STATUS_MISSING) != 0) {
             stateColor = stateMissing;
             statusmsg = "Feature unavailable: missing class file";
         }
         if (selected) opt.drawRect(x-1, y-1, x+w+1, y+11, 0xff0066cc);
         if (restart) opt.drawGradientRect(x, y, x+w, y+10, stateOn, stateOff);
         else opt.drawRect(x, y, x+w, y+10, stateColor);
         x++; y++;
         if (help != null) {
             drawHelp(x+w+8, y, help, status);
             //showText(help, x+w+8, y, 0xcccccc);
         }
         if (center) x += (w - showTextLength(caption)) >> 1;
         showText(caption, x, y, 0xffffff);
         if (hover && statusmsg != null) {
             w = showTextLength(statusmsg);
             x += 10; y -= 11;
             opt.drawRect(x-1, y-1, x+w+1, y+11, 0x99000000);
             showText(statusmsg, x, y, 0xcc9999);
         }
         return hover && mousePress(0);
     }
     
     private static final int SCALE_DISCRETE = 1,
                              SCALE_LINEAR   = 2,
                              SCALE_LOG      = 3;
     
     private static float drawBar(int x, int y, int w, float val, float min, float max, String help, int scale, int status) {
         x *= 5; y *= 11; w *= 5; // add y offset if needed here
         float ratio = (scale != SCALE_LOG)
                     ? (val - min) / (max - min)
                     : (float) (Math.log(val/min) / Math.log(max/min));
         int bar = x + (int)(w * ratio);
         boolean hover = mouseX>=x && mouseY>=y && mouseX<x+w+1 && mouseY<y+11;
         //if (!optionsModEnabled) bar = -10;
         opt.drawRect(x, y+4, x+w, y+5, 0xff66bb66);
         opt.drawRect(bar-1, y+2, bar+1, y+8, 0xffffffff);
         if (help != null) {
             drawHelp(x+w+8, y, help, status);
             //showText(help, x+w+8, y+1, 0xcccccc);
         }
         if (hover) {
             ratio = (mouseX - x) / (float)w;
             if (ratio < 0) ratio = 0;
             if (ratio > 1) ratio = 1;
             float res = (scale != SCALE_LOG)
                       ? min + ratio * (max - min)
                       : (float) (min * Math.pow(max/min, ratio));
             showText(String.format((scale == SCALE_DISCRETE) ? "%.0f (%.0f)" : "%.2f (%.2f)", res, val), mouseX, mouseY - 9, 0xffffcc);
             if (Mouse.isButtonDown(0)) val = res;
         }
         return val;
     }
 
     private static void updateConfigFile(String find, String replace) {
         try {
             String fn = path + "config.txt";
 
             BufferedReader reader = new BufferedReader(new FileReader(fn));
             StringBuilder builder = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) builder.append(line).append("\r\n");
             reader.close();
 
             FileWriter writer = new FileWriter(fn);
             writer.write(builder.toString().replaceAll(find, replace));
             writer.close();
         } catch(Exception whatever) { err("damn", whatever); }
     }
 
     private static int getSetInt(int current, String name, int initial, int min, int max, String help) {
         return getSetInt(current, name, initial, min, max, help, 0);
     }
 
     private static int getSetInt(int current, String name, int initial, int min, int max, String help, int feature) {
         if (!initialized) return getInt(name, initial, min, max);
         optionNr++; // new GUI element
         int ofs = optionOfs != 0 ? optionOfs + 1 : 0; if (optionNr <= ofs || optionOfs + optionsPage < optionNr) return current;
         int res = (int)drawBar(22, optionNr - optionOfs, 20, current, min, max, help, SCALE_DISCRETE, status[feature]);
         if (res != current) {
             updateConfigFile("(?m)^"+name+"\\W.*$", String.format(Locale.ENGLISH, "%-22s= %d", name, res));
             return res;
         }
         return current;
     }
 
     private static float getSetFloat(float current, String name, float initial, float min, float max, String help) {
         return getSetFloat(current, name, initial, min, max, help, 0);
     }
 
     private static float getSetFloat(float current, String name, float initial, float min, float max, String help, int feature) {
         if (!initialized) return getFloat(name, initial, min, max);
         optionNr++; // new GUI element
         int ofs = optionOfs != 0 ? optionOfs + 1 : 0; if (optionNr <= ofs || optionOfs + optionsPage < optionNr) return current;
         float res = drawBar(22, optionNr - optionOfs, 20, current, min, max, help, SCALE_LINEAR, status[feature]);
         if (res != current) {
             updateConfigFile("(?m)^"+name+"\\W.*$", String.format(Locale.ENGLISH, "%-22s= %6.2f", name, res));
             return res;
         }
         return current;
     }
     
     private static float getSetLog(float current, String name, float initial, float min, float max, String help) {
         return getSetLog(current, name, initial, min, max, help, 0);
     }
     
     private static float getSetLog(float current, String name, float initial, float min, float max, String help, int feature) {
         if (!initialized) return getFloat(name, initial, min, max);
         optionNr++; // new GUI element
         int ofs = optionOfs != 0 ? optionOfs + 1 : 0; if (optionNr <= ofs || optionOfs + optionsPage < optionNr) return current;
         float res = drawBar(22, optionNr - optionOfs, 20, current, min, max, help, SCALE_LOG, status[feature]);
         if (res != current) {
             updateConfigFile("(?m)^"+name+"\\W.*$", String.format(Locale.ENGLISH, "%-22s= %6.2f", name, res));
             return res;
         }
         return current;
     }
 
     private static int getSetBind(int current, String name, int initial, String help) {
         return getSetBind(current, name, initial, help, 0);
     }
 
     private static int getSetBind(int current, String name, int initial, String help, int feature) {
         if (!initialized) return getBind(name, initial);
         optionNr++; // new GUI element
         int ofs = optionOfs != 0 ? optionOfs + 1 : 0; if (optionNr <= ofs || optionOfs + optionsPage < optionNr) return current;
         // draw element / select if clicked / return without changes if not selected
         if (drawBtn(22, optionNr - optionOfs, 20, optionSel == optionNr ? "..." : keyName(current), help, optionSel == optionNr, true, true, false, status[feature])) optionSel = optionNr;
         else if (optionSel != optionNr) return current;
         // try to rebind
         for (int key = 1; key<255; key++) if (keyDown(key)) {
             optionSel = -1; // unselect
             if (key == Keyboard.KEY_ESCAPE) key = Keyboard.KEY_NONE;
             updateConfigFile("(?m)^"+name+"\\W.*$", String.format(Locale.ENGLISH, "%-22s= %s", name, keyName(key)));
             return key;
         }
         // no keys pressed - until next time then
         return current;
     }
 
     private static boolean getSetBool(boolean current, String name, boolean initial, String help) {
         return getSetBool(current, name, initial, help, 0);
     }
     
     private static boolean getSetBool(boolean current, String name, boolean initial, String help, int feature) {
         if (!initialized) return getBool(name, initial);
         optionNr++; // new GUI element
         int ofs = optionOfs != 0 ? optionOfs + 1 : 0; if (optionNr <= ofs || optionOfs + optionsPage < optionNr) return current;
         if (drawBtn(22, optionNr - optionOfs, 20, current ? "yes" : "no", help, false, current, true, false, status[feature])) {
             updateConfigFile("(?m)^"+name+"\\W.*$", String.format(Locale.ENGLISH, "%-22s= %s", name, current ? "no" : "yes"));
             return !current;
         }
         // no change
         return current;
     }
 
 
     //#ZMod#Helpers###########################################################
 
     private static void drawItem(ItemStack obj, int x, int y) {
         int id = getItemsId(obj), meta = getItemsInfo(obj), tmp = meta;
         boolean any = false; // unused atm :/
         if (tmp == -1) { tmp = 0; any = true; }
         setItemsInfo(obj, tmp); // meta fix
         renderItemGUI(x, y, obj);
         setItemsInfo(obj, meta); // restore meta
     }
 
     private static String getTime(long time) {
         time += optInfoTimeOffset; // time counting offset as 0:00 is beginning of day normally
         int daytime = (int)(time % 24000), h = daytime / 1000, m = (int)((daytime % 1000) * 0.06f);
         return (h<10 ? "0" : "") + h + (m<10 ? " : 0" : " : ") + m;
     }
 
     private static String getRealTime(long time) {
         long d = time / 1728000; time %= 1728000;
         long h = time / 72000; time %= 72000;
         long m = time / 1200; time %= 1200;
         long s = time / 20; time %= 20;
         long u = time / 2;
         return ""+d+(h<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+h+(m<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+m+(s<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+s+"\u00a7f . \u00a79"+u;
     }
 
     private static String getEntityName(Entity ent) {
         if (!(ent instanceof EntityLiving)) return "inanimate object";
         if (ent instanceof EntityPlayer) return getPlayerName((EntityPlayer)ent);
         return typeName[getEntityType(ent)];
     }
     
     private static String getNameForId(int id, int meta) { return getNameForId(id | ((meta==-1 ? 9999 : meta) << 16)); }
     private static String getNameForId(int id) {
         Set set = names.entrySet();
         Iterator it = set.iterator();
         String found = null, key;
         while (it.hasNext()) {
             Map.Entry ent = (Map.Entry)it.next();
             if ((Integer)ent.getValue() != id) continue;
             key = (String)ent.getKey();
             if (key.equals("Ghast")) continue;
             if (key.equals("Cow")) continue;
             if (key.equals("Spider")) continue;
             if (key.equals("Sheep")) continue;
             if (key.equals("Skelly")) continue;
             if (key.equals("Creeper")) continue;
             if (key.equals("Zombie")) continue;
             if (key.equals("Slimes")) continue;
             if (key.equals("Pig")) continue;
             if (key.equals("Chicken")) continue;
             if (key.equals("Squid")) continue;
             if (key.equals("Pigzombie")) continue;
             if (key.equals("Player")) continue;
             if (key.equals("Other")) continue;
             if (key.equals("Wolf")) continue;
             if (key.equals("Cavespider")) continue;
             if (key.equals("Enderman")) continue;
             if (key.equals("Silverfish")) continue;
             if (key.equals("LavaSlime")) continue;
             if (key.equals("MushroomCow")) continue;
             if (key.equals("Villager")) continue;
             if (key.equals("SnowMan")) continue;
             if (key.equals("Blaze")) continue;
             if (key.equals("EnderDragon")) continue;
             if (key.equals("Golem")) continue;
             if (key.equals("Ocelot")) continue;
             if (key.equals("adminium")) continue;
             if (found == null || found.length() < key.length()) found = key;
         }
         if (found == null && id < 65536) {
             found = getNameForId(id, -1);
             if (found.charAt(0)=='6' && found.length()>6) found = null;
         }
         return found == null ? ""+id : found;
     }
 
     private static boolean TMI_initialized;
     private static Method mTMI_isEnabled, mTMI_getInstance;
     private static boolean isTMIEnabled() {
         if (!TMI_initialized) {
             TMI_initialized = true;
             try {
                 Class c = Class.forName("TMIConfig");
                 mTMI_isEnabled = c.getMethod("isEnabled", new Class[]{});
                 mTMI_getInstance = c.getMethod("getInstance", new Class[]{});
             } catch(Exception whatever) { mTMI_isEnabled = null; }
         }
         if (mTMI_isEnabled == null) return false;
         boolean res = false;
         try {
             Object TMIConfig = mTMI_getInstance.invoke(null, new Object[]{});
             res = (Boolean)mTMI_isEnabled.invoke(TMIConfig, new Object[]{});
         } catch(Exception whatever) { // TMI fails at program exit
             return false;
         }
         return res;
     }
 
     // ################################################################################################################ MC Wrapped functions
     private static void updateMousePos() {
         ScaledResolution kst = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight);
         int sx = kst.getScaledWidth();
         int sy = kst.getScaledHeight();
         mouseX = (Mouse.getX() * sx) / minecraft.displayWidth;
         mouseY = sy - (Mouse.getY() * sy) / minecraft.displayHeight - 1;
     }
 
     //#ZMod#Wrappers##########################################################
     //-ZMod-Wrapper-Minecraft-------------------------------------------------
     private static String getPath() { String res = ""; try { res = Minecraft.getMinecraftDir().getCanonicalPath(); } catch(Exception whatever) { res = ""; } return res; }
     //private static int getTexture(String name) { return minecraft.renderEngine.getTexture(name); }
     private static void bindTexture(String name) {
         minecraft.renderEngine.func_98187_b(name);
     }
     private static List getChat() { return (List)getValue(fChat, minecraft.ingameGUI.getChatGUI()); }
     private static boolean getIsMenu() { return minecraft.currentScreen != null; }
     private static boolean getIsOptions() { return minecraft.currentScreen instanceof Options; }
     private static boolean getIsMultiplayer() { return !minecraft.isSingleplayer(); }
     private static PlayerControllerMP getPlayerController() { return minecraft.playerController; } // used to detect world change - do not actually care about the object itself
     private static boolean rayTrace(double dist, float f) { return (rayHit = minecraft.renderViewEntity.rayTrace(dist, f)) != null; }
     private static void overloadRenderGlobal() { minecraft.renderGlobal = render = new ZRG(minecraft, minecraft.renderEngine); }
     private static void overloadEntityRender() { minecraft.entityRenderer = new ZER(minecraft, minecraft.entityRenderer); }
     private static Field fAchivement = getField(GuiAchievement.class, "ga_theAchievement");
     private static void killAchievement() { setValue(fAchivement, minecraft.guiAchievement, null); }
     private static EntityClientPlayerMP getPlayer() { return minecraft.thePlayer; }
     private static Object getRenderer() { return minecraft.entityRenderer; }
     private static RenderItem itemRenderer;
 
     //-ZMod-Wrapper-NetClientHandler------------------------------------------
     private static NetClientHandler getSendQueue() { return minecraft.getNetHandler(); }
     private static void queuePacket(NetClientHandler queue, Packet packet) { queue.addToSendQueue(packet); }
     private static void queuePacket(Packet packet) { queuePacket(getSendQueue(), packet); }
 
     //-ZMod-Wrapper-PlayerControllerMP
     private static void syncCurrentItem(PlayerControllerMP controller) {
         controller.syncCurrentItem();
     }
     private static void syncCurrentItem() {
         syncCurrentItem(getPlayerController());
     }
 
     //-ZMod-Wrapper-Entity----------------------------------------------------
     private static Field fFireImmune = getField(Entity.class, "e_isImmuneToFire");
     private static void setNoClip(boolean val) {
         if (player.noClip != val) {
             if (isMultiplayer) {
                 if (modNoClipAllowed) sendChat(val ? "/noclip enabled" : "/noclip disabled");
                 else { chatClient("\u00a74zombe's \u00a72noclip\u00a74-mod is not enabled on this server."); val = false; }
             }
             player.noClip = val;
         }
     }
     private static World getEntityWorld(Entity ent) { return ent.worldObj; }
     private static float getEntityWidth(Entity ent) { return ent.width; }
     private static float getEntityHeight(Entity ent) { return ent.height; }
     private static float getEntityYaw(Entity ent) { return ent.rotationYaw; }
     private static void setEntityYaw(Entity ent, float yaw) { ent.rotationYaw = yaw; }
     private static float getEntityPitch(Entity ent) { return ent.rotationPitch; }
     private static void setEntityPitch(Entity ent, float pitch) { ent.rotationPitch = pitch; }
     private static Entity getOnEntity(Entity ent) { return ent.ridingEntity; }
     private static double getMountOffset(Entity ent) { return ent.getMountedYOffset(); }
     private static float getEntitySteps(Entity ent) {    return ent.distanceWalkedModified; }
     private static void setEntitySteps(Entity ent, float val) { ent.distanceWalkedModified = val; }
     private static void dieEntity(Entity ent) { ent.setDead(); }
     private static boolean getEntityOnGround(Entity ent) {    return ent.onGround; }
     private static void setEntityOnGround(Entity ent, boolean val) { ent.onGround = val; }
     private static double getEntityMotionX(Entity ent) {    return ent.motionX; }
     private static void setEntityMotionX(Entity ent, double val) { ent.motionX = val; }
     private static double getEntityMotionY(Entity ent) {    return ent.motionY; }
     private static void setEntityMotionY(Entity ent, double val) { ent.motionY = val; }
     private static double getEntityMotionZ(Entity ent) {    return ent.motionZ; }
     private static void setEntityMotionZ(Entity ent, double val) { ent.motionZ = val; }
     private static double getEntityPrevPosX(Entity ent) {    return ent.lastTickPosX; }
     private static void setEntityPrevPosX(Entity ent, double val) { ent.lastTickPosX = val; }
     private static double getEntityPrevPosY(Entity ent) {    return ent.lastTickPosY; }
     private static void setEntityPrevPosY(Entity ent, double val) { ent.lastTickPosY = val; }
     private static double getEntityPrevPosZ(Entity ent) {    return ent.lastTickPosZ; }
     private static void setEntityPrevPosZ(Entity ent, double val) { ent.lastTickPosZ = val; }
     private static double getEntityPosX(Entity ent) { return ent.posX; }
     private static void setEntityPosX(Entity ent, double val) { ent.posX = val; }
     private static double getEntityPosY(Entity ent) { return ent.posY; }
     private static void setEntityPosY(Entity ent, double val) { ent.posX = val; }
     private static double getEntityPosZ(Entity ent) { return ent.posZ; }
     private static void setEntityPosZ(Entity ent, double val) { ent.posX = val; }
     private static void setFire0(Entity ent) { ent.extinguish(); }
     private static void setAir(Entity ent, int val) { ent.setAir(val); }
     private static void setFall(Entity ent, float val) { ent.fallDistance = val; }
     private static int getEntityAge(Entity ent) { return ent.ticksExisted; }
     private static void setEntityAge(Entity ent, int val) { ent.ticksExisted = val; }
     private static void setEntityFireImmune(Entity ent, boolean immune) { 
         //setValue(fFireImmune, ent, immune);
         ent.isImmuneToFire = immune;
     }
     private static int getEntityType(Entity ent) {
         if (!(ent instanceof EntityLiving)) return 0; // early out
         // subtypes
         if (ent instanceof EntityMagmaCube) return LAVASLIME;
         if (ent instanceof EntityPigZombie) return PIGZOMBIE;
         if (ent instanceof EntityCaveSpider) return CAVESPIDER;
         if (ent instanceof EntityMooshroom) return REDCOW;
         // night dwellers
         if (ent instanceof EntityZombie) return ZOMBIE;
         if (ent instanceof EntityEnderman) return ENDERMAN;
         if (ent instanceof EntitySkeleton) return SKELLY;
         if (ent instanceof EntityCreeper) return CREEPER;
         // the rest
         if (ent instanceof EntitySlime) return SLIME;
         if (ent instanceof EntitySquid) return SQUID;
         if (ent instanceof EntitySpider) return SPIDER;
         if (ent instanceof EntitySheep) return SHEEP;
         if (ent instanceof EntityVillager) return VILLAGER;
         if (ent instanceof EntitySnowman) return SNOWMAN;
         if (ent instanceof EntityChicken) return CHICKEN;
         if (ent instanceof EntityPig) return PIG;
         if (ent instanceof EntityCow) return COW;
         if (ent instanceof EntityWolf) return WOLF;
         if (ent instanceof EntityBlaze) return BLAZE;
         if (ent instanceof EntityIronGolem) return GOLEM;
         if (ent instanceof EntityOcelot) return OCELOT;
         // rare or junk
         if (ent instanceof EntityGhast) return GHAST;
         if (ent instanceof EntityPlayer) return PLAYER;
         if (ent instanceof EntitySilverfish) return SILVERFISH;
         if (ent instanceof EntityGiantZombie) return ZOMBIE;
         if (ent instanceof EntityDragon) return DRAGON;
         // unknown living
         return LIVING;
     }
     private static void setEntityPos(Entity ent, double x, double y, double z) {
         ent.setPosition(0.0D, 0.0D, 0.0D);
         ent.prevPosX = ent.lastTickPosX = ent.posX = x;
         ent.prevPosY = ent.lastTickPosY = ent.posY = y;
         ent.prevPosZ = ent.lastTickPosZ = ent.posZ = z;
         ent.setPosition(x, y, z);
     }
 
     //-ZMod-Wrapper-EntityLiving----------------------------------------------
     private static EntityLiving getView() { return minecraft.renderViewEntity; }
     private static void setView(EntityLiving ent) { minecraft.renderViewEntity = ent; }
     private static Field fHealth = getField(EntityLiving.class, "el_health");
     private static void setHealth(EntityLiving ent, int val) {
         //setValue(fHealth, ent, val);
         ent.setEntityHealth(val);
     }
     private static int getHealth(EntityLiving ent) {
         //return (Integer)getValue(fHealth, ent);
         return ent.getHealth();
     }
     private static int getMaxHealth(EntityLiving ent) {
         return ent.getMaxHealth();
     }
     private static void setEntitySize(EntityLiving ent, float height, int health) {
         ent.width *= height / ent.height;
         ent.height = height;
         setHealth(ent, health);
         ent.setPosition(ent.posX, ent.posY, ent.posZ);
     }
 
     //-ZMod-Wrapper-EntityPlayer----------------------------------------------
     private static void flyCallSuper(EntityPlayer ent, double mx, double my, double mz) { ent.callSuper(mx,my,mz); }
     private static boolean getIsSleeping(EntityPlayer ent) { return player.isPlayerSleeping(); }
     private static String getPlayerName(EntityPlayer ent) { return ent.username; }
     private static ChunkCoordinates getSpawn(EntityPlayer ent) { return ent.getBedLocation(); }
     private static ChunkCoordinates getBed(EntityPlayer ent) { return ent.playerLocation; }
     private static void sendChat(String var0) { ((EntityClientPlayerMP)player).sendChatMessage(var0); }
 
     //-ZMod-Wrapper-EntityMinecart--------------------------------------------
     //private static Field fCartFuel = getField(EntityMinecart.class, "em_fuel");
     //private static int getCartType(EntityMinecart ent) { return ent.minecartType; }
     //private static int getCartFuel(EntityMinecart ent) { return (Integer)getValue(fCartFuel, ent); }
     //private static void setCartFuel(EntityMinecart ent, int val) { setValue(fCartFuel, ent, val); }
     
     //-ZMod-Wrapper-EntityItem------------------------------------------------
     private static ItemStack getEntityItemStack(EntityItem ent) { return ent.getEntityItem(); }
     
     //-ZMod-Wrapper-TileEntity------------------------------------------------
     private static void setChanged(TileEntity tent) { tent.onInventoryChanged(); }
     private static NBTTagCompound getTileEntityCopy(TileEntity ent) {
         if (ent == null) return null;
         NBTTagCompound nbt = new NBTTagCompound();
         ent.writeToNBT(nbt);
         return nbt;
     }
     private static NBTTagCompound mapGetTileCopy(int x,int y,int z) {
         TileEntity ent = getTileEntity(x,y,z);
         if (ent == null) return null;
         NBTTagCompound nbt = new NBTTagCompound();
         ent.writeToNBT(nbt);
         return nbt;
     }
     private static NBTTagCompound setNBTPos(NBTTagCompound nbt, int x, int y, int z) {
         nbt.setInteger("x", x);
         nbt.setInteger("y", y);
         nbt.setInteger("z", z);
         return nbt;
     }
     private static void setTileEntityFromCopy(TileEntity ent, NBTTagCompound nbt) {
         ent.readFromNBT(nbt);
     }
     private static void mapSetTileCopy(NBTTagCompound nbt, int x,int y,int z) {
         nbt.setInteger("x", x);
         nbt.setInteger("y", y);
         nbt.setInteger("z", z);
         getTileEntity(x,y,z).readFromNBT(nbt);
     }
     private static void loadTileEntityFromNBT(ZP250 obj) {
         try {
         NBTTagCompound data = obj.nbtData;
         NBTTagCompound.writeNamedTag(data, new DataOutputStream(new ByteArrayOutputStream())); // NBT stuff probably never changes
         TileEntity ent = TileEntity.createAndLoadEntity(data);
         if (map != getMap()) return; // just in case
         map.setBlockTileEntity(ent.xCoord, ent.yCoord, ent.zCoord, ent);
         } catch(Exception fuckoffyoufuckingpiceofshitofajavaretardationidonotcare) {}
     }
     // ---------------------------------------------------------------------------------------------------------------- TileEntityFurnace
     private static Field fFurnaceItems = getField(TileEntityFurnace.class, "tef_furnaceItemStacks");
     private static ItemStack[] getFurnaceItems(Object tent) { return (ItemStack[])getValue(fFurnaceItems, tent); }
     // ---------------------------------------------------------------------------------------------------------------- TileEntityChest
     private static Field fChestItems = getField(TileEntityChest.class, "tec_chestContents");
     private static ItemStack[] getChestItems(Object tent) { return (ItemStack[])getValue(fChestItems, tent); }
     // ---------------------------------------------------------------------------------------------------------------- TileEntityDispenser
     private static Field fDispItems = getField(TileEntityDispenser.class, "ted_dispenserContents");
     private static ItemStack[] getDispItems(Object tent) { return (ItemStack[])getValue(fDispItems, tent); }
     // ---------------------------------------------------------------------------------------------------------------- TileEntitySign
     private static String[] getSignText(int x, int y, int z) { return ((TileEntitySign)getTileEntity(x,y,z)).signText; }
     // ---------------------------------------------------------------------------------------------------------------- ItemStack
     private static Icon getItemsIcon(ItemStack items) { return items.getIconIndex(); }
     private static ItemStack newItemsE(int id, int count) { return newItems(id & 0xffff, count, id >> 16); }
     private static ItemStack newItems(int id, int count, int param) { return new ItemStack(id, count, param == 9999 ? -1 : param); }
     private static ItemStack newItems(int id, int count) { return newItems(id, count, 0); }
     private static int getItemsId(ItemStack items) { return items.itemID; }
     private static int getItemsCount(ItemStack items) { return items.stackSize; }
     private static void setItemsCount(ItemStack items, int cnt) { items.stackSize = cnt; }
     private static int getItemsInfo(ItemStack items) { return items.getItemDamage(); }
     private static void setItemsInfo(ItemStack items, int val) { items.setItemDamage(val); }
     private static boolean isItemsMatch(ItemStack items, int val) { return ( getItemsId(items) | ((val >> 16) == 9999 ? 0x270f0000 : (getItemsInfo(items) << 16)) ) == val; }
     private static boolean isItemsMatch(ItemStack items, ItemStack match) { return getItemsId(items) == getItemsId(match) && (getItemsInfo(match) < 0 || getItemsInfo(match) == getItemsInfo(items)); }
     // ---------------------------------------------------------------------------------------------------------------- CraftingManager
     private static Field fCMRecipes = getField(CraftingManager.class, "cm_recipes");
     private static CraftingManager getCManager() { return CraftingManager.getInstance(); }
     // ---------------------------------------------------------------------------------------------------------------- ShapedRecipes
     private static Field fRWidth = getField(ShapedRecipes.class, "sr_recipeWidth"), fRHeight = getField(ShapedRecipes.class, "sr_recipeHeight"), fRMap = getField(ShapedRecipes.class, "sr_recipeItems"), fRResA = getField(ShapedRecipes.class, "sr_recipeOutput");
     private static ShapedRecipes newRecipeNormal(int id, int count, int width, int height, ItemStack ingredients[]) { return new ShapedRecipes(width, height, ingredients, newItemsE(id, count)); }
     // ---------------------------------------------------------------------------------------------------------------- ShapelessRecipes
     private static Field fRList = getField(ShapelessRecipes.class, "lr_recipeItems"), fRResB = getField(ShapelessRecipes.class, "lr_recipeOutput");
     private static ShapelessRecipes newRecipeShapeless(int id, int count, List ingredients) { return new ShapelessRecipes(newItemsE(id, count), ingredients); }
     // ---------------------------------------------------------------------------------------------------------------- RecipeSorter
     private static void sortRecipes(List recipes) { Collections.sort(recipes, new RecipeSorter(getCManager())); }
     // ---------------------------------------------------------------------------------------------------------------- InventoryCrafting
     private static Field fCBTable = getField(InventoryCrafting.class, "ic_stackList");
     private static InventoryCrafting newCraftingGrid(int width, int height, ItemStack search[]) { InventoryCrafting grid = new InventoryCrafting((Container)null, width, height); setValue(fCBTable, grid, search); return grid; }
     private static boolean isRecipeMatch(int i, InventoryCrafting grid) { return ((IRecipe)pList.get(i)).matches(grid, map); }
     // ---------------------------------------------------------------------------------------------------------------- WorldInfo
     private static WorldInfo getWorld() { return getMap().getWorldInfo(); }
     private static boolean getRain() { return world.isRaining(); }
     private static boolean getThunder() { return world.isThundering(); }
     private static void setRain(boolean val) { world.setRaining(val); }
     private static void setThunder(boolean val) { world.setThundering(val); }
     private static int getRainTime() { return world.getRainTime(); }
     private static int getThunderTime() { return world.getThunderTime(); }
     private static void setRainTime(int val) { world.setRainTime(val); }
     private static void setThunderTime(int val) { world.setThunderTime(val); }
     private static long getTime() { return world.getWorldTime(); }
     private static void setTime(long val) { world.setWorldTime(val); }
     private static long getSeed() { return world.getSeed(); }
     private static String getName() { return world.getWorldName(); }
     
     //-ZMod-Wrapper-World-- --------------------------------------------------
     private static World getMap() { return minecraft.theWorld; }
     
     private static int getWorldId(World world, int x, int y, int z) { return world.getBlockId(x,y,z); }
     private static int getWorldMeta(World world, int x, int y, int z) { return world.getBlockMetadata(x,y,z); }
     private static TileEntity getWorldTileEntity(World world, int x, int y, int z) { return world.getBlockTileEntity(x,y,z); }
     private static void setWorldIdWithNotify(World world, int x, int y, int z, int id) { world.func_94575_c(x,y,z, id); }
     private static void setWorldIdMetaWithNotify(World world, int x, int y, int z, int id, int meta) { world.setBlockAndMetadataWithNotify(x,y,z, id, meta, 1); }
     private static void setWorldIdMetaWithoutNotify(World world, int x, int y, int z, int id, int meta) { world.setBlockAndMetadataWithNotify(x,y,z, id, meta, 0); }
     private static void markWorldNeedsUpdate(World world, int x, int y, int z) {
         world.notifyBlockChange(x,y,z, getWorldId(world, x,y,z));
         world.markBlockForUpdate(x,y,z);
     }
     private static void markWorldNeedsUpdate(World world, int sx, int sy, int sz, int ex, int ey, int ez) {
         for (int x = sx; x <= ex; ++x)
         for (int y = sy; y <= ey; ++y)
         for (int z = sz; z <= ez; ++z)
         markWorldNeedsUpdate(world, x,y,z);
     }
     
     private static void spawnLightning(int x, int y, int z) { map.spawnEntityInWorld(new EntityLightningBolt(map, x, y, z)); }
     
     // new light functions, the one used in the F3 debug screen
     private static int getBlockLightLevel(int x, int y, int z) {
         return map.getChunkFromBlockCoords(x, z).getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15);
     }
     private static int getSkyLightLevel(int x, int y, int z) {
         return map.getChunkFromBlockCoords(x, z).getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15);
     }
     private static int getRealLightLevel(int x, int y, int z) {
         return map.getChunkFromBlockCoords(x, z).getBlockLightValue(x & 15, y, z & 15, 0);
     }
     // old light functions
     // last param = 0  -> sky or real light level, dunno
     // last param = 16 -> block light level (artificial light sources)
     private static int getLightLevel(int x, int y, int z, int kst) { return map.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockLightValue(x & 0xf, y, z & 0xf, kst);}
     // brightness
     private static float getLight(int x, int y, int z) { return map.getLightBrightness(x, y, z); }
     // real light level
     private static int getLightLevel(int x,int y,int z) { return map.getFullBlockLightValue(x, y, z); }
     private static int mapXGetId(int x, int y, int z) { return getWorldId(map, x,y,z); }
     private static int mapXGetMeta(int x, int y, int z) { return getWorldMeta(map, x,y,z); }
     private static void mapXSetIdMetaNoUpdate(int x, int y, int z, int id, int meta) { setWorldIdMetaWithoutNotify(map, x,y,z, id, meta); }
     private static void mapXSetIdMeta(int x, int y, int z, int id, int meta) { setWorldIdMetaWithNotify(map, x,y,z, id, meta); }
     private static void mapXSetId(int x, int y, int z, int id) { setWorldIdWithNotify(map, x,y,z, id); }
     private static boolean mapXGetChunkExists(int cx, int cy) { return map.getChunkProvider().chunkExists(cx, cy); }
     private static void chunkNeedsUpdate(int cx, int cz) { cx <<= 4; cz <<= 4; map.markBlockRangeForRenderUpdate(cx, 0, cz, cx+15, 127, cz+15); }
     private static void mapXNeedsUpdate(int sx, int sy, int sz, int ex, int ey, int ez) { markWorldNeedsUpdate(map, sx, sy, sz, ex, ey, ez); }
     private static List getEntities() { return (List)((ArrayList)map.loadedEntityList).clone(); }
     private static void noiseTP(Entity ent) { map.playSoundAtEntity(ent, "mob.slimeattack", 0.4f,( (rnd.nextFloat() - rnd.nextFloat())*0.2f + 1.0f )*0.8f); }
     private static void overloadMapRandom() { if (map.rand != null && !(map.rand instanceof ZRND)) map.rand = new ZRND(map.rand); }
     private static TileEntity getTileEntity(int x, int y, int z) { return getWorldTileEntity(map, x,y,z); }
     // ----------------------------------------------------------------------------------------------------------------
     private static String getBiomeName(int x, int z) { return map.getChunkFromBlockCoords(x, z).getBiomeGenForWorldCoords(x & 0xf, z & 0xf, map.getWorldChunkManager()).biomeName; }
     // ---------------------------------------------------------------------------------------------------------------- GuiIngame
     private static Field fChat = getField(GuiNewChat.class, "gnc_ChatLines");
     // ---------------------------------------------------------------------------------------------------------------- ChatLine
     private static String getChatLine(List var0, int var1) {
         ChatLine line = (ChatLine) var0.get(var1);
         return (line != null) ? line.getChatLineString() : null;
     }
     private static void chatClient(String var0) { player.addChatMessage(var0); }
     // ---------------------------------------------------------------------------------------------------------------- MovingObjectPosition
     private static MovingObjectPosition rayHit;
     private static int rayHitX() { return rayHit.blockX; }
     private static int rayHitY() { return rayHit.blockY; }
     private static int rayHitZ() { return rayHit.blockZ; }
     private static int rayHitSide() { return rayHit.sideHit; }
     // ---------------------------------------------------------------------------------------------------------------- InventoryPlayer
     private static InventoryPlayer getInventory(EntityPlayer ent) { return ent.inventory; }
     private static ItemStack invItemsArr[], invArmorsArr[];
     private static ItemStack[] getInvItems(InventoryPlayer inv) { return inv.mainInventory; }
     private static ItemStack[] getInvArmors(InventoryPlayer inv) { return inv.armorInventory; }
     private static void setInvItems(int loc, ItemStack items) { invItemsArr[loc] = items; }
     private static int getInvCur() { return inv.currentItem; }
     private static void setInvCur(int cur) { inv.currentItem = cur; }
     // ---------------------------------------------------------------------------------------------------------------- ChunkCoordinates
     private static int getX(ChunkCoordinates pos) { return pos.posX; }
     private static int getY(ChunkCoordinates pos) { return pos.posY; }
     private static int getZ(ChunkCoordinates pos) { return pos.posZ; }
     // ---------------------------------------------------------------------------------------------------------------- Block
     private static Block getBlock(int id) { return Block.blocksList[id]; }
     private static void setBlock(int id, Block val) { Block.blocksList[id] = val; }
     private static boolean getBlockIsSpawn(int id) { return Block.blocksList[id] != null && Block.blocksList[id].blockMaterial.isOpaque() && Block.blocksList[id].renderAsNormalBlock(); }
     private static void setBlockGraphicsLevel(Block block, boolean flag) { ((BlockLeaves)block).setGraphicsLevel(flag); }
     private static Field fBlockStrength = getField(Block.class, "b_blockHardness");
     private static float getBlockStrength(Block block) { return (Float)getValue(fBlockStrength, block); }
     private static void setBlockStrength(Block block, float val) { setValue(fBlockStrength, block, val); }
     private static Field fBlockResist = getField(Block.class, "b_blockResistance");
     private static float getBlockResist(Block block) { return (Float)getValue(fBlockResist, block); }
     private static void setBlockResist(Block block, float val) { setValue(fBlockResist, block, val); }
     private static float getBlockSlip(Block block) { return block.slipperiness; }
     private static void setBlockSlip(Block block, float val) { block.slipperiness = val; }
     private static Material getBlockMaterial(Block block) { return block.blockMaterial; }
     private static boolean getBlockIsOpaque(int id) { return Block.opaqueCubeLookup[id]; }
     private static int getBlockOpacity(int id) { return Block.lightOpacity[id]; }
     private static void setBlockOpacity(int id, int val) { Block.lightOpacity[id] = val; }
     private static int getBlockLight(int id) { return Block.lightValue[id]; }
     private static void setBlockLight(int id, int val) { Block.lightValue[id] = val; }
     // ---------------------------------------------------------------------------------------------------------------- Material
     private static boolean getIsLiquid(Material mat) { return mat.isLiquid(); }
     private static boolean getIsCover(Material mat) { return mat.getCanBlockGrass(); }
     private static boolean getIsSolid(Material mat) { return mat.isSolid(); }
     private static boolean getIsBurnable(Material mat) { return mat.getCanBurn(); }
     private static boolean getIsReplaceable(Material mat) { return mat.isReplaceable(); }
     // ---------------------------------------------------------------------------------------------------------------- Items
     private static Item getItem(int id) { return Item.itemsList[id]; }
     private static int getItemMax(Item item) { return item == null ? 0 : item.getItemStackLimit(); }
     private static void setItemMax(Item item, int val) { if (item != null) item.setMaxStackSize(val); }
     private static int getItemDmgCap(Item item) { return item.getMaxDamage(); }
     private static void setItemDmgCap(Item item, int val) { item.setMaxDamage(val); }
     private static String getItemName(Item item) { return item.getUnlocalizedName(); }
     private static boolean getItemHasSubTypes(Item item) { return item.getHasSubtypes(); }
     // ---------------------------------------------------------------------------------------------------------------- GameSettings
     private static void setOrtho(){
         ScaledResolution var0 = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight);
         GL11.glOrtho(0.0D, var0.getScaledWidth_double(), var0.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
     }
     private static int getScrWidthS() { return new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight).getScaledWidth(); }
     private static int getScrHeightS() { return new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight).getScaledHeight(); }
     private static boolean isHideGUI() { return minecraft.gameSettings.hideGUI; }
     private static boolean isShowDebug() { return minecraft.gameSettings.showDebugInfo; }
     private static int getKeyJump() { return minecraft.gameSettings.keyBindJump.keyCode; }
     private static int getKeyGo() { return minecraft.gameSettings.keyBindForward.keyCode; }
     private static int getKeyBack() { return minecraft.gameSettings.keyBindLeft.keyCode; }
     private static int getViewDistance() { return minecraft.gameSettings.renderDistance; }
     // ---------------------------------------------------------------------------------------------------------------- BlockFire
     private static Field fBlockFireSpread = getField(BlockFire.class, "bf_chanceToEncourageFire");
     private static int getFireSpread(int id) { return Array.getInt(getValue(fBlockFireSpread, getBlock(51)), id); }
     private static void setFireSpread(int id, int val) { Array.setInt(getValue(fBlockFireSpread, getBlock(51)), id, val); }
     private static Field fBlockFireBurn = getField(BlockFire.class, "bf_abilityToCatchFire");
     private static int getFireBurn(int id) { return Array.getInt(getValue(fBlockFireBurn, getBlock(51)), id); }
     private static void setFireBurn(int id, int val) { Array.setInt(getValue(fBlockFireBurn, getBlock(51)), id, val); }
     private static ItemStack getGridItem(int nr) {
                 return minecraft.currentScreen instanceof GuiCrafting ? ((ContainerWorkbench)((ContainerWorkbench)((GuiContainer)((GuiContainer)minecraft.currentScreen)).inventorySlots)).craftMatrix.getStackInSlot(nr) : (minecraft.currentScreen instanceof GuiInventory ? ((ContainerPlayer)((ContainerPlayer)((GuiContainer)((GuiContainer)minecraft.currentScreen)).inventorySlots)).craftMatrix.getStackInSlot(nr) : null);
                 }
     // ---------------------------------------------------------------------------------------------------------------- RenderItem
     private static void renderItemGUI(int x, int y, ItemStack items) {
         if (itemRenderer == null) itemRenderer = new RenderItem();
         itemRenderer.renderItemIntoGUI(minecraft.fontRenderer, minecraft.renderEngine, items, x, y);
     }
     // ----------------------------------------------------------------------------------------------------------------
     private static void setXItemLighting() { RenderHelper.enableStandardItemLighting(); }
     private static boolean getIsHell() { return mapXGetId(fix(posX),127,fix(posZ))==7; } // hackish, there is certainly a better way - but the hell with it.
     private static void showText(String str, int x, int y, int color) { minecraft.fontRenderer.drawStringWithShadow(str,x,y,color); }
     private static int showTextLength(String str) { return minecraft.fontRenderer.getStringWidth(str); }
 
     // ########################################################################################################################### logging
     private static void initLogAndPath() {
         path = getPath();
         path += File.separatorChar + "mods" + File.separatorChar + "zombe" + File.separatorChar;
         try { File make = new File(path); make.mkdirs(); } catch(Exception whatever) { path = ""; }
         try { File tmp = new File(path + "log.txt"); out = new PrintStream(tmp); logPath = tmp.getCanonicalPath(); } catch(Exception whatever) { logPath = "failed to create one :("; out = System.out; }
         log("=========== logging ===========");
         log("ZModPack: version "+version);
         log("Log started at: "+(new Timestamp((new Date()).getTime())));
     }
 
     public static void log(String text) {
         if (out == null) initLogAndPath();
         out.println(text);
     }
 
     private static void err(String text) {
         if (logErrors <= 0) return;
         log(text);
         if (showError == null) {
             showError = text;
             setMsg(MERR, "ZMod: errors detected - one or more mods affected\n"+(logErrors==8 ? "First " : "Next ")+showError+"\nLog: "+logPath, 0xff8888);
         }
         if (logErrors-- == 0) log("info: stopping error logging.");
     }
 
     private static void err(String text, Exception e) {
         err(text);
         if (logErrors <= 0) return;
         log("### Exception: " + e.toString());
         e.printStackTrace(out);
     }
     
     public static void logc(String text) {
         log(text);
         chatClient(text);
     }
     
     //#ZMod#Config############################################################
     private static int getBind(String name, int init) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         if (val.equals("")) return Keyboard.KEY_NONE;
         int i = Keyboard.getKeyIndex(val.toUpperCase());
         if (i == Keyboard.KEY_NONE) { err("error: config.txt @ "+name+" - invalid key name \""+val+"\""); return init; }
         return i;
     }
 
     private static float getFloat(String name, float init, float min, float max) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         float f = parseFloat(val, Float.NaN);
         if (Float.isNaN(f)) { err("error: config.txt @ "+name+" - float expected but garbage found \""+val+"\""); return init; }
         else if (f<min || f>max) { err("error: config.txt @ "+name+" - must be between "+min+" and "+max+" \""+val+"\""); return init; }
         return f;
     }
 
     private static int getInt(String name, int init, int min, int max) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         int i = parseInteger(val, Integer.MIN_VALUE);
         if (i==Integer.MIN_VALUE) { err("error: config.txt @ "+name+" - integer expected but garbage found \""+val+"\""); return init; }
         else if (i<min || i>max) { err("error: config.txt @ "+name+" - must be between "+min+" and "+max+" \""+val+"\""); return init; }
         return i;
     }
 
     private static String getString(String name, String init) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         return val;
     }
 
     private static boolean getBool(String name, boolean init) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         if (val.equals("1") || val.equals("yes") || val.equals("true") || val.equals("on")) return true;
         if (val.equals("0") || val.equals("no") || val.equals("false") || val.equals("off")) return false;
         err("error: config.txt @ "+name+" - must be one of (1, yes, true, on, 0, no, false, off) \""+val+"\"");
         return init;
     }
     
     private static Mark getColor(String name, int color) {
         Mark res = new Mark(color);
         String val = conf.getProperty(name);
         if (val == null) return res;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         if (!res.loadColor(val)) err("error: config.txt @ "+name+" - undefined or invalid color \""+val+"\"");
         return res;
     }
     
     private static Mark getIntRange(String name, int initMin, int initMax, int min, int max) {
         Mark res = new Mark(min, max);
         String val = conf.getProperty(name);
         if (val == null) return res;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         String part[] = val.split(" *\\.\\. *");
         int a = initMin, b = initMax;
         if (part.length == 2) { a = parseInteger(part[0], Integer.MIN_VALUE); b = parseInteger(part[1], Integer.MIN_VALUE); }
         if (part.length != 2) err("error: config.txt @ "+name+" - invalid range specification \""+val+"\"");
         else if (a == Integer.MIN_VALUE) err("error: config.txt @ "+name+" - integer expected but garbage found \""+part[0]+"\" in \""+val+"\"");
         else if (b == Integer.MIN_VALUE) err("error: config.txt @ "+name+" - integer expected but garbage found \""+part[1]+"\" in \""+val+"\"");
         else if (a > b) err("error: config.txt @ "+name+" - range begins after end \""+val+"\"");
         else if (a < min || b > max) err("error: config.txt @ "+name+" - range \""+val+"\" is out of bounds ("+min+" .. "+max+")");
         else { res.min = a; res.max = b; }
         return res;
     }
     
     private static int getBlockId(String name, int init) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         int id = parseUnsigned(val);
         if (names.containsKey(val)) id = (Integer)names.get(val);
         if ((id & 65535) > 255) err("error: config.txt @ "+name+" - non-block name or id out of block id range \""+val+"\"");
         else if ((id>>16) != 9999 && (id>>16) != 0) err("error: config.txt @ "+name+" - block has metadata (ex: colored wool) \""+val+"\".");
         else return id & 255;
         return init;
     }
     
     private static int getItemId(String name, int init) {
         String val = conf.getProperty(name);
         if (val == null) return init;
         val = val.replaceAll("[\t\r\n]+", " ").trim();
         int id = parseIdInfo(val);
         if (names.containsKey(val)) id = (Integer)names.get(val);
         if (id == -1) err("error: config.txt @ "+name+" - invalid name or id \""+val+"\"");
         else return id;
         return init;
     }
 
     private static boolean getDeprecated(String name) {
         if (initialized || conf.getProperty(name) == null) return false;
         log("notice: config.txt @ "+name+" - this option is deprecated");
         return true;
     }
     
     private static boolean getBroken(String name) {
         if (initialized || conf.getProperty(name) == null) return false;
         log("notice: config.txt @ "+name+" - this option is disabled in this release");
         return true;
     }
 
     // ########################################################################################################################### reflect
     private static void reportException(Exception error) {
         if (exceptionReported) return;
         exceptionReported = true;
         err("exception in reflection code encountered !", error);
     }
     private static boolean getClassExists(String name) { try { if (Class.forName(name) != null) return true; } catch(Exception whatever) { return false; } return false; }
     private static Field getField(Class c, String name) {
         int index = -1;
         for (int i=0;i<MCPnames.length;i+=3) if (name.equals(MCPnames[i])) { index = i; break; }
         if (index == -1) { err("getField failed for: " + name); return null; }
         
         try {
             Field field = c.getDeclaredField(MCPnames[index+1]); field.setAccessible(true); return field;
         } catch(Exception whatever) {
             try {
                 Field field = c.getDeclaredField(MCPnames[index+2]); field.setAccessible(true); return field;
             } catch(Exception error) {
                 err("exception in reflection code encountered ! (missing field: '"+MCPnames[index+1]+"', obfus: '"+MCPnames[index+2]+"')", error);
                 return null;
             }
         }
     }
     private static Object getValue(Field field, Object obj) { try { return field.get(obj); } catch(Exception error) { reportException(error); } return null; }
     private static void setValue(Field field, Object obj, Object val) { try { field.set(obj, val); } catch(Exception error) { reportException(error); } }
     private static Class getClass(String name) { try { return Class.forName(name); } catch(Exception error) { reportException(error); } return null; }
     private static Object getResult(Method m, Object obj, Object param[]) { try { return m.invoke(obj, param); } catch(Exception error) { reportException(error); } return null; }
     private static Object getResult(Method m) { return getResult(m, null, new Object[]{}); }
     private static Object getResult(Method m, Object obj) { return getResult(m, obj, new Object[]{}); }
     private static Object getResult(Method m, Object obj, Object p1) { return getResult(m, obj, new Object[]{p1}); }
     private static Object getResult(Method m, Object obj, Object p1, Object p2) { return getResult(m, obj, new Object[]{p1,p2}); }
     private static Object getResult(Method m, Object obj, Object p1, Object p2, Object p3) { return getResult(m, obj, new Object[]{p1,p2,p3}); }
     private static Object getResult(Method m, Object obj, Object p1, Object p2, Object p3, Object p4) { return getResult(m, obj, new Object[]{p1,p2,p3,p4}); }
     private static boolean checkClass(Class c) {
         return checkClass("zmodmarker", c);
     }
     private static boolean checkClass(String name, Class c) {
         try {
             Field field = c.getDeclaredField(name);
             if (field != null) return true;
         } catch(Exception whatever) { }
         return false;
     }
     private static boolean checkClass(Class c, String mod) { return checkClass(c, mod, null); }
     private static boolean checkClass(Class c, String mod, String warning) {
         if (checkClass(c)) return true;
         if (warning == null) err("error: "+c.getName()+".class has not been installed - "+mod+" mod disabled");
         else log("warning: "+c.getName()+".class has not been installed - "+mod+" mod: "+warning);
         return false;
     }
     
     private static boolean checkStatus(int feature) {
         return status[feature] == STATUS_AVAILABLE;
     }
     
     private static boolean checkStatus(int feature, String what) {
         if (status[feature] == STATUS_AVAILABLE) return true;
         String msg = what+": ";
         if ((status[feature] & STATUS_BROKEN) != 0) msg += "this feature is broken";
         else if ((status[feature] & STATUS_MISSING) != 0) msg += "feature unavailable: a required class file is missing";
         else msg += "feature unavailable: reason unknown";
         if (initialized) err("error: "+msg);
         else log("warning: "+msg);
         return false;
     }
 
 
     // ########################################################################################################################### util
     private static int fix(double d) { return (int)Math.floor(d); } // returns correct integer coordinate
     private static void delMsg(int rank) { texts[rank] = null; }
     private static void setMsg(String msg) { setMsg(2, msg); }
     private static void setMsg(int rank, String msg) { setMsg(rank, msg, 0xffffff, 2+rank*2, 2+rank*12); }
     private static void setMsg(int rank, String msg, int color) { texts[rank] = new Text(msg, 2+rank*2, 2+rank*12, color); }
     private static void setMsg(int rank, String msg, int color, int x, int y) { texts[rank] = new Text(msg, x, y, color); }
     private static boolean keyPress(int key) { boolean res = !keys[key]; return (keys[key] = keyDown(key)) && res; }
     private static boolean keyDown(int key) { return key != 0 && Keyboard.isKeyDown(key); }
     private static String keyName(int key) { if (key == 0) {return "";} else {String res = Keyboard.getKeyName(key); return res != null ? res : (""+key); }}
     private static boolean mousePress(int nr) { boolean res = !keysM[nr]; return (keysM[nr] = Mouse.isButtonDown(nr)) && res; }
     private static float sgn(float f) { return f<0f ? -1f : (f>0f ? 1f : 0f); }
     private static FloatBuffer makeBuffer(int length) { return ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(); }
     private static FloatBuffer makeBuffer(float[] array) { return (FloatBuffer)ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).flip(); }
     private static void obliqueNearPlaneClip(float a, float b, float c, float d) {
         float matrix[] = new float[16];
         float x, y, z, w, dot;
         FloatBuffer buf = makeBuffer(16);
         GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
         buf.get(matrix).rewind();
         x = (sgn(a) + matrix[8]) / matrix[0];
         y = (sgn(b) + matrix[9]) / matrix[5];
         z = -1.0F;
         w = (1.0F + matrix[10]) / matrix[14];
         dot = a*x + b*y + c*z + d*w;
         matrix[2] = a * (2f / dot);
         matrix[6] = b * (2f / dot);
         matrix[10] = c * (2f / dot) + 1.0F;
         matrix[14] = d * (2f / dot);
         buf.put(matrix).rewind();
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         GL11.glLoadMatrix(buf);
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
     }
 
     // ########################################################################################################################### parsers
     private static HashMap pNames;
     private static HashSet<String> pFiles;
     private static HashMap<Integer, Integer> pFuel;
     private static HashMap<Integer, ItemStack> pSmelt;
     private static List pList;
 
     private static void parse(List list, String file, int section) {
         pList = list;
         pNames = names==null ? new HashMap() : (HashMap)(((HashMap)names).clone());
         pFiles = new HashSet<String>();
         parseFile(file, section);
     }
     
     private static void parseFile(String file, int section) {
         if (!pFiles.add(file)) {
             err("error: recursion detected - \""+file+"\" is already included");
             return;
         }
         String data = "", fn = path + file;
         try {
             byte[] buffer = new byte[(int) new File(fn).length()];
             BufferedInputStream stream = new BufferedInputStream(new FileInputStream(fn));
             stream.read(buffer);
             stream.close();
             data = new String(buffer);
             log("info: parsing \""+file+"\"");
         } catch(Exception error) {
             err("error: failed to load \""+file+"\"", error);
             data = "";
         }
         String lines[] = data.split("\\r?\\n");
         int at;
         for (int line=0;line<lines.length;line++) {
             if (lines[line].startsWith("[IGNORE]")) section = IGNORE;
             else if (lines[line].startsWith("[NAMEMAP]")) section = NAMEMAP;
             else if (lines[line].startsWith("[RECIPES]")) section = RECIPES;
             else if (lines[line].startsWith("[FUEL]")) section = FUEL;
             else if (lines[line].startsWith("[SMELTING]")) section = SMELTING;
             else if (lines[line].startsWith("[ITEMS]")) section = ITEMS;
             else if (lines[line].startsWith("[NAMEMAP=")) parseFile(lines[line].substring(9).replaceAll("\\].*\\z",""), NAMEMAP);
             else if (lines[line].startsWith("[RECIPES=")) parseFile(lines[line].substring(9).replaceAll("\\].*\\z",""), RECIPES);
             else if (lines[line].startsWith("[FUEL=")) parseFile(lines[line].substring(6).replaceAll("\\].*\\z",""), FUEL);
             else if (lines[line].startsWith("[SMELTING=")) parseFile(lines[line].substring(10).replaceAll("\\].*\\z",""), SMELTING);
             else if (lines[line].startsWith("[ITEMS=")) parseFile(lines[line].substring(7).replaceAll("\\].*\\z",""), ITEMS);
             else if (section == NAMEMAP) parseNames(lines[line], file, line + 1);
             else if (section == RECIPES) parseRecipe(lines[line], file, line + 1);
             else if (section == FUEL) parseFuel(lines[line], file, line + 1);
             else if (section == SMELTING) parseSmelting(lines[line], file, line + 1);
             else if (section == ITEMS) parseItems(lines[line], file, line + 1);
         }
     }
     
     private static int parseUnsigned(String str) { return parseInteger(str, -1); }
 
     private static int parseInteger(String str, int fail) {
         try {
             return Integer.decode(str);
         } catch(Exception whatever) {
             return fail;
         }
     }
 
     private static float parseFloat(String str, float fail) {
         try {
             return Float.parseFloat(str);
         } catch(Exception whatever) {
             return fail;
         }
     }
 
     private static int parseIdInfo(String text) {
         try {
             String part[] = text.split("/");
             if (part.length>2) return -1;
             int id = Integer.decode(part[0]);
             if (part.length==2) {
                 int info = Integer.decode(part[1]);
                 if (info<0) info = 9999;
                 id += info << 16;
             }
             return id;
         } catch(Exception error) {
             return -1;
         }
     }
     
     private static void parseFuel(String src, String file, int line) {
         String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
         if (got.length != 2) {
             if (got.length == 1 && !got[0].equals("")) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid fuel definition");
         } else {
             int fuel = pNames.containsKey(got[0]) ? (Integer)pNames.get(got[0]) : parseUnsigned(got[0]);
             if (fuel > 0) fuel &= 0xffff;
             int time = parseUnsigned(got[1]);
             if (fuel<=0) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid fuel definition (\""+got[0]+"\" is unknown or invalid)");
             else if (time<=0) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid fuel definition (\""+got[1]+"\" is invalid time)");
             else pFuel.put(fuel, time);
         }
     }
     
     private static void parseSmelting(String src, String file, int line) {
         String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
         if (got.length != 2) {
             if (got.length == 1 && !got[0].equals("")) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid smelt definition");
         } else {
             int itemId = pNames.containsKey(got[0]) ? (Integer)pNames.get(got[0]) : parseIdInfo(got[0]);
             int id = pNames.containsKey(got[1]) ? (Integer)pNames.get(got[1]) : parseUnsigned(got[1]);
             if (id > 0) id &= 0xffff;
             ItemStack item = itemId>0 ? newItemsE(itemId, 1) : null;
             if (id<=0) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid smelting definition (\""+got[1]+"\" is unknown or invalid)");
             else if (item==null) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid smelting definition (\""+got[0]+"\" is unknown or invalid)");
             else pSmelt.put(id, item);
         }
     }
 
     private static void parseNames(String src, String file, int line) {
         String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
         if ((got.length & 1) != 0) {
             if (got.length != 1 && !got[0].equals("")) err("error: "+file+" @ line#" + line + " \"" + src + "\" - incomplete name definition");
         } else for (int at=0;at<got.length;at+=2) {
             int id = parseIdInfo(got[at+1]);
             if (id==-1) err("error: "+file+" @ line#" + line + " \"" + src + "\" - non numbers in name definition");
             else pNames.put(got[at], id);
         }
     }
 
     private static void addRecipe(int width, int height, ItemStack recipeMap[], int id, int count) {
         boolean normal = height > 0;
         ItemStack search[] = recipeMap;
         List list = null;
         if (!normal) {
             search = new ItemStack[9];
             list = new ArrayList();
             for (int i=0;i<recipeMap.length;i++) list.add(search[i] = recipeMap[i]);
             width = height = 3;
         }
         InventoryCrafting grid = newCraftingGrid(width, height, search);
         // find and remove match
         for (int i=0;i<pList.size();i++) if (isRecipeMatch(i, grid)) { pList.remove(i); break; }
         // add new
         if (id!=0) pList.add( normal ? newRecipeNormal(id, count, width, height, recipeMap) : newRecipeShapeless(id, count, list) );
     }
 
     private static void parseRecipe(String src, String file, int line) {
         String trouble = ""; // parsing trouble - will contain the offending substring
         String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
         if (got.length < 5) {
             if (got.length != 1 && !got[0].equals("")) log("error: "+file+" @ line#" + line + " \"" + src + "\" - incomplete recipe definition");
             return;
         }
         try {
             trouble = got[0];
             int width = Integer.decode(got[2]), height = Integer.decode(got[3]), count = Integer.decode(got[1]);
             int itemnr = pNames.containsKey(got[0]) ? (Integer)pNames.get(got[0]) : parseIdInfo(got[0]);
             if (itemnr<0 || count<0 || (itemnr>0 && count<=0)) {
                 err("error: "+file+" @ line#" + line + " \"" + src + "\" - bad recipe result");
                 return;
             }
             if (itemnr != 0 && getItem(itemnr & 0xffff) == null) {
                 log("warning: "+file+" @ line#" + line + " \"" + src + "\" - resulting item does not exist, recipe ignored");
                 return;
             }
             if (height != 0 && (width*height+4 != got.length || width <= 0 || height <= 0 || width > 3 || height > 3)) {
                 err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid recipe size (" + width + "," + height + ")");
                 return;
             } else if (height == 0 && (width+4 != got.length || width <= 0)) {
                 err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid recipe size (" + width + ")");
                 return;
             }
             ItemStack recipeMap[] = new ItemStack[width*(height==0?1:height)];
             for (int at=4;at<got.length;at++) {
                 trouble = got[at];
                 int value = pNames.containsKey(got[at]) ? (Integer)pNames.get(got[at]) : parseIdInfo(got[at]);
                 if (value == -1 || (height==0 && value==0)) throw new Exception("("+value+" "+got[at]+" "+(pNames.containsKey(got[at])?"+":"-")+")");
                 recipeMap[at - 4] = value<=0 ? null : newItemsE(value, 1);
             }
             addRecipe(width, height, recipeMap, itemnr, count);
         } catch(Exception whatever) {
             err("error: "+file+" @ line#" + line + " \"" + src + "\" - \"" + trouble + "\" is unknown or malformed");
             err("???",whatever);
         }
     }
 
     private static void parseItems(String src, String file, int line) {
         String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
         if (got.length < 2) {
             if (got.length == 1 && !got[0].equals("")) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid items definition");
         } else {
             int id, stack = 0, damage = 0, emit = 0, absorb = 0, load = 0, spread = 0, burn = 0;
             float strength = 0f, resist = 0f, slip = 0f;
             Item item;
             id = ( pNames.containsKey(got[0]) ? (Integer)pNames.get(got[0]) : parseUnsigned(got[0]) ) & 0xffff;
             if (id > 32000 || (item = getItem(id))==null) log("warning: "+file+" @ line#" + line + " \"" + src + "\" - item/block does not exist, definition ignored");
             else {
                 if (!got[1].equals("?")) { load |= 1; stack = parseUnsigned(got[1]); }
                 if (id >= 256) {
                     if (got.length > 2 && !got[2].equals("?") && !getItemHasSubTypes(item) && getItemDmgCap(item)>0) { load |= 2; damage = parseUnsigned(got[2]); }
                 } else {
                     if (got.length > 2 && !got[2].equals("?")) { load |= 4; emit = parseUnsigned(got[2]); }
                     if (got.length > 3 && !got[3].equals("?")) { load |= 8; absorb = parseUnsigned(got[3]); }
                     if (got.length > 4 && !got[4].equals("?")) { load |= 16; strength = parseFloat(got[4], Float.NaN); }
                     if (got.length > 5 && !got[5].equals("?")) { load |= 32; resist = parseFloat(got[5], Float.NaN); }
                     if (got.length > 6 && !got[6].equals("?")) { load |= 64; slip = parseFloat(got[6], Float.NaN); }
                     if (got.length > 7 && !got[7].equals("?")) { load |= 128; spread = parseUnsigned(got[7]); }
                     if (got.length > 8 && !got[8].equals("?")) { load |= 256; burn = parseUnsigned(got[8]); }
                 }
                 if ((load & 1) != 0 && (stack < 1 || stack > 64)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid max stack size (\""+got[1]+"\")");
                 else if ((load & 2) != 0 && damage < 1) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid max damage (\""+got[2]+"\")");
                 else if ((load & 4) != 0 && (emit < 0 || emit > 15)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid light emission (\""+got[2]+"\")");
                 else if ((load & 8) != 0 && (absorb < 0 || absorb > 255)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid light reduction (\""+got[3]+"\")");
                 else if ((load & 16) != 0 && (Float.isNaN(strength) || strength < -1f || strength > 100f)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid strength (\""+got[4]+"\")");
                 else if ((load & 32) != 0 && (Float.isNaN(resist) || resist < 0f || resist > 18000000f)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid resistance (\""+got[5]+"\")");
                 else if ((load & 64) != 0 && (Float.isNaN(slip) || slip < 0.5f || slip > 1f)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid slipperiness (\""+got[6]+"\")");
                 else if ((load & 128) != 0 && (spread < 0 || spread > 100)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid fire spread value (\""+got[7]+"\")");
                 else if ((load & 256) != 0 && (burn < 0 || burn > 100)) err("error: "+file+" @ line#" + line + " \"" + src + "\" - invalid burn speed value (\""+got[8]+"\")");
                 else {
                     Mark mark = itemMine[id];
                     if ((load & 1) != 0) mark.setMaxStack(stack);
                     if ((load & 2) != 0) mark.setMaxDamage(damage);
                     if ((load & 4) != 0) mark.setLightEmission(emit);
                     if ((load & 8) != 0) mark.setLightReduction(absorb);
                     if ((load & 16) != 0) mark.setStrength(strength);
                     if ((load & 32) != 0) mark.setResistance(resist);
                     if ((load & 64) != 0) mark.setSlipperiness(slip);
                     if ((load & 128) != 0) mark.setFireSpread(spread);
                     if ((load & 256) != 0) mark.setFireBurn(burn);
                 }
             }
         }
     }
 
     private static int[] parseRule(String rule) {
         String got[] = rule.split("[\t ]+"), part[];
         int res[] = new int[got.length * 6];
         for (int i=0;i<got.length;i++) {
             part = got[i].split("/");
             if (part.length != 6) { modOreEnabled = false; err("error : ore-mod disabled - invalid ore rule found \""+rule+"\""); return null; }
             res[i*6 + 0] = new Integer(part[0]);
             res[i*6 + 1] = new Integer(part[1]);
             res[i*6 + 2] = new Integer(part[2]);
             res[i*6 + 3] = new Integer(part[3]);
             res[i*6 + 4] = new Integer(part[4]);
             res[i*6 + 5] = new Integer(part[5]);
         }
         return res;
     }
     
 }

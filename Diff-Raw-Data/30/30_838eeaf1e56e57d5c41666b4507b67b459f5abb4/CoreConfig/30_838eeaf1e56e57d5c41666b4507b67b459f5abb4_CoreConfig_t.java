 
 package shadow.mods.metallurgy;
 
 import java.io.File;
 import java.io.IOException;
 
 import shadow.mods.metallurgy.base.mod_MetallurgyBaseMetals;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraftforge.common.Configuration;
 
 public class CoreConfig  {
 
 	public static int ItemGoldDustID;
 	public static int ItemIronDustID;
 	public static boolean DiamondEnabled;
 	public static boolean EmeraldEnabled;
 	public static boolean LapisLazuliEnabled;
 	public static boolean RedstoneEnabled;
 	public static boolean goldEnabled;
 	public static boolean ironEnabled;
 	
 	public static int DiamondVeinCount;
 	public static int DiamondOreCount;
 	public static int DiamondOreHeight;
 
 	public static int EmeraldVeinCount;
 	public static int EmeraldOreCount;
 	public static int EmeraldOreHeight;
 
 	public static int LapisLazuliVeinCount;
 	public static int LapisLazuliOreCount;
 	public static int LapisLazuliOreHeight;
 
 	public static int RedstoneVeinCount;
 	public static int RedstoneOreCount;
 	public static int RedstoneOreHeight;
 
 	public static int IronVeinCount;
 	public static int IronOreCount;
 	public static int IronOreHeight;
 
 	public static int GoldVeinCount;
 	public static int GoldOreCount;
 	public static int GoldOreHeight;
 	
 	public static boolean crushersEnabled;
 	public static int crusherID;
 	public static double stoneCrusherSpeed;
 	public static double copperCrusherSpeed;
 	public static double bronzeCrusherSpeed;
 	public static double ironCrusherSpeed;
 	public static double steelCrusherSpeed;
 	public static boolean enableTextureOverrides;
 	
 	public static void init()
 	{
 		File file = new File(mod_MetallurgyCore.proxy.getMinecraftDir() + "/config/Metallurgy");
     	file.mkdir();
     	File newFile = new File(mod_MetallurgyCore.proxy.getMinecraftDir() + "/config/Metallurgy/MetallurgyCore.cfg");
     	
         try
         {
             newFile.createNewFile();
             System.out.println("Successfully created/read configuration file");
         }
         catch (IOException e)
         {
             System.out.println("Could not create configuration file for mod_MetallugyBase. Reason:");
             System.out.println(e);
         }
 
         // [Forge] Configuration class, used as config method 
         Configuration config = new Configuration(newFile);
 
         config.load();
 
     	enableTextureOverrides = config.getOrCreateBooleanProperty("Enable", "Texture Override", true).getBoolean(true);
     	
     	ItemGoldDustID = config.getOrCreateIntProperty("Gold Dust ID", "Item Ids", 26200).getInt(26200);
    	ItemIronDustID = config.getOrCreateIntProperty("Iron Dust ID", "Item Ids", 26201).getInt(26201);
     	
     	DiamondEnabled = config.getOrCreateBooleanProperty("Enable Diamond", "Ores", true).getBoolean(true);
     	EmeraldEnabled = config.getOrCreateBooleanProperty("Enable Emerald", "Ores", true).getBoolean(true);
     	LapisLazuliEnabled = config.getOrCreateBooleanProperty("Enable Lapis Lazuli", "Ores", true).getBoolean(true);
     	RedstoneEnabled = config.getOrCreateBooleanProperty("Enable Redstone", "Ores", true).getBoolean(true);
     	goldEnabled = config.getOrCreateBooleanProperty("Enable Gold", "Ores", true).getBoolean(true);
     	ironEnabled = config.getOrCreateBooleanProperty("Enable Iron", "Ores", true).getBoolean(true);
 
     	DiamondVeinCount = config.getOrCreateIntProperty("Diamond Vein Count", "Ore Generation", 4).getInt(4);
     	DiamondOreCount = config.getOrCreateIntProperty("Diamond Ore Count", "Ore Generation", 4).getInt(4);
     	DiamondOreHeight = config.getOrCreateIntProperty("Diamond Ore Height", "Ore Generation", 128).getInt(128);
     	
     	EmeraldVeinCount = config.getOrCreateIntProperty("Emerald Vein Count", "Ore Generation", 4).getInt(4);
     	EmeraldOreCount = config.getOrCreateIntProperty("Emerald Ore Count", "Ore Generation", 1).getInt(1);
     	EmeraldOreHeight = config.getOrCreateIntProperty("Emerald Ore Height", "Ore Generation", 128).getInt(128);
 
     	LapisLazuliVeinCount = config.getOrCreateIntProperty("LapisLazuili Vein Count", "Ore Generation", 4).getInt(4);
     	LapisLazuliOreCount = config.getOrCreateIntProperty("LapisLazuili Ore Count", "Ore Generation", 4).getInt(4);
     	LapisLazuliOreHeight = config.getOrCreateIntProperty("LapisLazuili Ore Height", "Ore Generation", 128).getInt(128);
 
     	RedstoneVeinCount = config.getOrCreateIntProperty("Redstone Vein Count", "Ore Generation", 4).getInt(4);
     	RedstoneOreCount = config.getOrCreateIntProperty("Redstone Ore Count", "Ore Generation", 4).getInt(4);
     	RedstoneOreHeight = config.getOrCreateIntProperty("Redstone Ore Height", "Ore Generation", 128).getInt(128);
     	
     	// Gold Generation
     	GoldVeinCount = config.getOrCreateIntProperty("Gold Vein Count", "Ore Generation", 4).getInt(4);
     	GoldOreCount = config.getOrCreateIntProperty("Gold Ore Count", "Ore Generation", 4).getInt(4);
     	GoldOreHeight = config.getOrCreateIntProperty("Gold Ore Height", "Ore Generation", 128).getInt(128);
     	
     	// Iron Generation
     	IronVeinCount = config.getOrCreateIntProperty("Iron Vein Count", "Ore Generation", 6).getInt(6);
     	IronOreCount = config.getOrCreateIntProperty("Iron Ore Count", "Ore Generation", 6).getInt(6);
     	IronOreHeight = config.getOrCreateIntProperty("Iron Ore Height", "Ore Generation", 128).getInt(128);
 
     	crusherID = config.getOrCreateBlockIdProperty("Crusher", 900).getInt(900);
     	crushersEnabled = config.getOrCreateBooleanProperty("Enable Crushers", "Machines", true).getBoolean(true);
 
     	stoneCrusherSpeed = config.getOrCreateIntProperty("Stone Speed", "Crushers", 32000).getInt(32000)/1000f;
     	copperCrusherSpeed = config.getOrCreateIntProperty("Copper Speed", "Crushers", 30000).getInt(30000)/1000f;
     	bronzeCrusherSpeed = config.getOrCreateIntProperty("Bronze Speed", "Crushers", 28000).getInt(28000)/1000f;
     	ironCrusherSpeed = config.getOrCreateIntProperty("Iron Speed", "Crushers", 25000).getInt(25000)/1000f;
     	steelCrusherSpeed = config.getOrCreateIntProperty("Steel Speed", "Crushers", 21000).getInt(21000)/1000f;
     	config.save();
 	}
 	
 	
 }

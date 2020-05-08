 package shadow.mods.metallurgy.fantasy;
 
 import java.io.File;
 import java.io.IOException;
 
 import shadow.mods.metallurgy.base.mod_MetallurgyBaseMetals;
 import shadow.mods.metallurgy.nether.AlloyNetherEnum;
 import shadow.mods.metallurgy.nether.OreNetherEnum;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraftforge.common.Configuration;
 
 public class FantasyConfig  {
 
 	public static int FantasyMetalsVeinID;
 	public static int FantasyMetalsBrickID;
 	public static int FantasyAlloysBrickID;
 
 	public static boolean[] alloyEnabled = new boolean[5];
 	public static boolean[] metalEnabled = new boolean[12];
 	public static boolean furnaceEnabled;
 	
	public static float[] extractorSpeeds = new float[11];
 	
 	public static int ItemStartID;
 
 	public static int[] VeinCount = new int[12];
 	public static int[] OreCount = new int[12];
 	public static int[] OreHeight = new int[12];
 	
 	public static void init()
 	{
 		File file = new File(mod_MetallurgyFantasy.proxy.getMinecraftDir() + "/config/Metallurgy");
     	file.mkdir();
     	File newFile = new File(mod_MetallurgyFantasy.proxy.getMinecraftDir() + "/config/Metallurgy/MetallurgyFantasy.cfg");
     	
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
 
         /* [Forge] Configuration class, used as config method */
         Configuration config = new Configuration(newFile);
 
         /* Load the configuration file */
         config.load();
         
         FantasyMetalsVeinID = config.getOrCreateBlockIdProperty("Metal Ore", 919).getInt(919);
     	FantasyMetalsBrickID = config.getOrCreateBlockIdProperty("Metal Brick", 920).getInt(920);
     	FantasyAlloysBrickID = config.getOrCreateBlockIdProperty("Alloy Brick", 921).getInt(921);
     	
     	extractorSpeeds[0] = config.getOrCreateIntProperty("Prometheum", "Abstractor Speeds", 22000).getInt(22000)/1000;    	
     	extractorSpeeds[1] = config.getOrCreateIntProperty("DeepIron", "Abstractor Speeds", 20000).getInt(20000)/1000;    	
     	extractorSpeeds[2] = config.getOrCreateIntProperty("BlackSteel", "Abstractor Speeds", 18000).getInt(18000)/1000;    	
     	extractorSpeeds[3] = config.getOrCreateIntProperty("Oureclase", "Abstractor Speeds", 16000).getInt(16000)/1000;    	
     	extractorSpeeds[4] = config.getOrCreateIntProperty("Aredrite", "Abstractor Speeds", 14000).getInt(14000)/1000;    	
     	extractorSpeeds[5] = config.getOrCreateIntProperty("Mithril", "Abstractor Speeds", 12000).getInt(12000)/1000;    	
     	extractorSpeeds[6] = config.getOrCreateIntProperty("Haderoth", "Abstractor Speeds", 10000).getInt(10000)/1000;    	
     	extractorSpeeds[7] = config.getOrCreateIntProperty("Orichalcum", "Abstractor Speeds", 8000).getInt(8000)/1000;    	
     	extractorSpeeds[8] = config.getOrCreateIntProperty("Adamantine", "Abstractor Speeds", 6000).getInt(6000)/1000;    	
     	extractorSpeeds[9] = config.getOrCreateIntProperty("Atlarus", "Abstractor Speeds", 4000).getInt(4000)/1000;    	    	
     	extractorSpeeds[10] = config.getOrCreateIntProperty("Tartarite", "Abstractor Speeds", 2000).getInt(2000)/1000;
 
     	for(int i = 0; i < AlloyFantasyEnum.numMetals; i++)
     		alloyEnabled[i] = config.getOrCreateBooleanProperty(AlloyFantasyEnum.names[i] + " Enabled", "Ores", true).getBoolean(true);
     	for(int i = 0; i < OreFantasyEnum.numMetals; i++)
     		metalEnabled[i] = config.getOrCreateBooleanProperty(OreFantasyEnum.names[i] + " Enabled", "Ores", true).getBoolean(true);
 
     	ItemStartID = config.getOrCreateIntProperty("Item Start IDs", "Item Ids Uses next 850", 27550).getInt(27550);
 
     	for(int i = 0; i < OreFantasyEnum.numMetals; i++)
     	{
 	    	VeinCount[i] = config.getOrCreateIntProperty(OreFantasyEnum.names[i] + " Vein Count", "Ore Generation", OreFantasyEnum.defaultVeinCount[i]).getInt(OreFantasyEnum.defaultVeinCount[i]);
 	    	OreCount[i] = config.getOrCreateIntProperty(OreFantasyEnum.names[i] + " Ore Count", "Ore Generation", OreFantasyEnum.defaultOreCount[i]).getInt(OreFantasyEnum.defaultOreCount[i]);
 	    	OreHeight[i] = config.getOrCreateIntProperty(OreFantasyEnum.names[i] + " Height", "Ore Generation", OreFantasyEnum.defaultOreHeight[i]).getInt(OreFantasyEnum.defaultOreHeight[i]);
     	}
     	
     	config.save();
 	}
 }

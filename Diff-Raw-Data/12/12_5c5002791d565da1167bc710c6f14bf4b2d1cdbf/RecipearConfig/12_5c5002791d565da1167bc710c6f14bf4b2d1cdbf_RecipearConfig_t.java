 package mods.recipear;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.List;
 
 import net.minecraftforge.common.Configuration;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 public class RecipearConfig {
 	public static boolean removeingame = false;
 	public static boolean debug = false;
 	public static int removeingameinterval = 60;
 	public static String placeholderName = "$cBanned Recipe";
 	public static String placeholderDescription = "$eThis item have been disabled by the server.";
 
 	public RecipearConfig(FMLPreInitializationEvent event) 
 	{
 		File recipearDataFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + File.separator + "Recipear");
 		recipearDataFolder.mkdir();
 
 		Properties(event, recipearDataFolder);
 		BannedRecipesProperties(event, recipearDataFolder);
 	}
 
 	private static void Properties(FMLPreInitializationEvent event, File recipearDataFolder) 
 	{
 		RecipearLogger.info("loading Recipear" + File.separator + "Core.cfg");
 		Configuration cfg = new Configuration(new File(recipearDataFolder + File.separator + "Core.cfg"));
 		try
 		{
 			cfg.load();
 			removeingame = cfg.get("Features", "RemoveIngame", removeingame, "Set this to true if you want to remove banned items from players at login and every interval").getBoolean(false);
 			removeingameinterval = cfg.get("Features", "RemoveIngameInterval", removeingameinterval, "Interval in seconds to check if player have a banned item, default is 60 seconds").getInt(60);
 			debug  = cfg.get(cfg.CATEGORY_GENERAL, "Debug", debug, "Turns on debug output in console/log, good if you need to see the inner workings.").getBoolean(false);
 			placeholderDescription = cfg.get("BannedItem", "Description", placeholderDescription, "The Description of banned item, supports color formatting").getString();
 			placeholderName = cfg.get("BannedItem", "Name", placeholderName, "The name of the item that is banned, supports color formatting").getString();
 		}
 		catch (Exception e) 
 		{
 			RecipearLogger.severe("has a problem loading Recipear" + File.separator + "Core.cfg: " + e.getMessage());
 		}
 		finally 
 		{
 			cfg.save();
 		}
 	}
 
 	private static void BannedRecipesProperties(FMLPreInitializationEvent event, File recipearDataFolder) {
 		try
 		{
 			RecipearLogger.info("loading Recipear" + File.separator + "BannedRecipes.cfg");
 			File file = new File(recipearDataFolder + File.separator + "BannedRecipes.cfg");
 			String comment = 
 					"# Format for removing items by ID is <ID>[:METADATA][:TYPE] and by Names is <NAME>[:TYPE], every recipe ban needs to be seperated with a new line,\n"
 					+ "# also supports comments.. and to see supported TYPE's check out the log file.\n"
 					+ "# Examples:\n"
 					+ "#  58 #Removes Crafting Table entirely from being crafted\n"
 					+ "#  CraftingTable #same as above\n"
 					+ "#  CrAfTiNGTaBlE #same as above, but please note that you should always keep a good structure\n"
 					+ "#  Iron Dust # Will disable it from being crafted entirely\n"
 					+ "#  Iron Dust:MACERATOR # Will disable it only from IC2 Macerator\n";
 
 			if(createCfgFile(file, comment)) {
 				List<String> lines = Files.readLines(file, Charsets.ISO_8859_1);
 
 				int count = 0, ITEMID = 0, METADATA = -1;
 				String NAME = null;
 
 				for(String line : lines) {
 					count++;
					if((line.length() > 0) && (!line.substring(0,1).contains("#"))) 
 					{
 						// Clean from comments and whitespace
 						line = line.replaceAll("\\s+","").split("#")[0];
 
 						String[] BannedRecipeRaw = line.split(":");
 
 						NAME = null;
 						ITEMID = 0;
 						METADATA = -1;
 						String TYPE = "DEFAULT";
 
 						try 
 						{
 							if(RecipearUtil.isInteger(BannedRecipeRaw[0]) && (Integer.valueOf(BannedRecipeRaw[0]) > 0)) {
 								ITEMID = Integer.parseInt(BannedRecipeRaw[0]);
 								METADATA = (BannedRecipeRaw.length > 1) ? Integer.parseInt(BannedRecipeRaw[1]) : -1;
 							} else {
 								NAME = BannedRecipeRaw[0].toLowerCase();
 							}
 							TYPE = (BannedRecipeRaw.length > 2) ? BannedRecipeRaw[2].toUpperCase() : "DEFAULT";
 						}
 						catch (Exception e) 
 						{
 							RecipearLogger.warning("Failed to add BannedRecipe at line " + count + ", check your formatting");
 							continue;
 						}
 
 						BannedRecipe BANNEDRECIPE = new BannedRecipe(ITEMID, METADATA, TYPE);
 
 						if(ITEMID == 0)
 							BANNEDRECIPE.name = NAME;
 
 						BannedRecipes.AddBannedRecipe(BANNEDRECIPE);
 
 						RecipearLogger.info("Added: " + BANNEDRECIPE.toString());
 					}
 				}
 			}
 		} 
 		catch (Exception e) 
 		{
			e.printStackTrace();
 		}
 	}
 
 	private static boolean createCfgFile(File file, String comment) {
 		if(!file.exists()) 
 		{
 			try 
 			{
 				file.createNewFile();
 				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 				bw.write(comment);
 				bw.close();
 				return true;
 			}
 			catch (Exception e) 
 			{
 				RecipearLogger.info(e.getMessage());
 				return false;
 			}
 		}
 
 		return true;
 	}
 }

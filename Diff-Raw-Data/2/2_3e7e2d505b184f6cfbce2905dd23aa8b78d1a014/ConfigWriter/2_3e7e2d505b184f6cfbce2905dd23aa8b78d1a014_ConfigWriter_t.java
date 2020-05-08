 package net.minecraft.src.ZeldaOoT;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.Item;
 import net.minecraft.src.ZeldaOoT.Resource.ItemMaps;
 import net.minecraft.src.forge.Configuration;
 import net.minecraft.src.forge.MinecraftForge;
 import net.minecraft.src.forge.Property;
 
 public class ConfigWriter
 {static boolean autoAssign;
 static String MODNAME;
 static File MODFILE;
 static int BlockID;
 /**
  * Adds a new Item Config line
  *
  * @param Config Name
  * @param config
  * @param default value
  */
 public static int ItemConfig (String ItemName, Configuration config, int def)
 {
	return Integer.parseInt(config.getOrCreateIntProperty(ItemName, Configuration.CATEGORY_ITEM, getItemID(def)).value);
 }
 
 /**
  * Adds a new Block Config line
  *
  * @param Config Name
  * @param config
  * @param default value
  */
 public static int BlockConfig (String BlockName, Configuration config, int def)
 {BlockID = Integer.parseInt(config.getOrCreateBlockIdProperty(BlockName, getBlockID(def, BlockName)).value);
 	return Integer.parseInt(config.getOrCreateBlockIdProperty(BlockName, getBlockID(def, BlockName)).value);
 }
 
 /**
  * Adds a new Block Config line
  *
  * @param Config Name
  * @param config
  * @param default value
  */
 public static int BlockConfig1 (String BlockName, Configuration config, int def)
 {
 	return Integer.parseInt(config.getOrCreateIntProperty(BlockName, Configuration.CATEGORY_BLOCK, getBlockID(def, BlockName)).value);
 }
 
 /**
  * Adds a new Config line
  * Used for Integers and is able to put in new categories
  *
  * @param Config Name
  * @param config
  * @param default value
  * @param cat  0 = block 1 = item 2 = general 3 = new category
  */
 public static int INTConfig (String ItemName, Configuration config, int def, int cat , String Category, int Type)
 {String SetCategory = null;
 	int newDef = 0;
 	switch (cat)
 	{
 	case 0: SetCategory = Configuration.CATEGORY_BLOCK;
 	case 1: SetCategory = Configuration.CATEGORY_ITEM;
 	case 2: SetCategory = Configuration.CATEGORY_GENERAL;
 	case 3: SetCategory = Category;
 	}
 	
 	switch (Type)
 	{
 	case 0: newDef = getBlockID(def, ItemName);
 	case 1: newDef = getItemID(def);
 	}
 	return Integer.parseInt(config.getOrCreateIntProperty(ItemName, SetCategory, newDef).value);
 }
 
 
 
 /**
  * Adds a new Config line
  * Used for Booleans and is able to put in new categories
  *
  * @param Config Name
  * @param config
  * @param default value
  * @param cat  0 = block 1 = item 2 = general 3 = new category
  */
 
 public static boolean BooleanConfig (String BooleanName, Configuration config, boolean def, int cat , String Category)
 {String SetCategory = null;
 	switch (cat)
 	{
 	case 0: SetCategory = Configuration.CATEGORY_BLOCK;
 	case 1: SetCategory = Configuration.CATEGORY_ITEM;
 	case 2: SetCategory = Configuration.CATEGORY_GENERAL;
 	case 3: SetCategory = Category;
 	}
 	
 	
 	return Boolean.parseBoolean(config.getOrCreateBooleanProperty(BooleanName, SetCategory, def).value);
 }
 
 /**
  * Adds a new Boolean Config line
  *
  * @param Config Name
  * @param config
  * @param default value
  */
 public static boolean BooleanConfig (String BooleanName, Configuration config, boolean def)
 {
 	return Boolean.parseBoolean(config.getOrCreateBooleanProperty(BooleanName, Configuration.CATEGORY_GENERAL, def).value);
 }
 
 /**
  * Makes new File for Config
  *
  * @param mod
  */
 private static File GetFile(String mod)
 {
 	MODNAME = mod;
 	MODFILE = new File(Minecraft.getMinecraftDir() + "/config/" + mod + ".cfg");	
 	return new File(Minecraft.getMinecraftDir() + "/config/" + mod + ".cfg");	
 }
 
 
 public static boolean AutoAssign(Configuration config)
 {  
 	autoAssign = Boolean.parseBoolean(config.getOrCreateBooleanProperty("AutoAssign", Configuration.CATEGORY_GENERAL, true).value); ;
 	 config.generalProperties.get("AutoAssign").comment = "Use this to Remap BlocksIDs USE WITH CAUTION CAN CORRUPT WORLDS";
 	if (autoAssign == true)
 	{
 		config.blockProperties.clear();
 		((Property)config.generalProperties.get("AutoAssign")).value = "false";
 	}
 	
 	return Boolean.parseBoolean(config.getOrCreateBooleanProperty("AutoAssign", Configuration.CATEGORY_GENERAL, false).value);
 	
 }
 
 /**
  * A Try/Catch function for unexpected results
  *
  * @param File
  * @param Mod
  */
 private static void ErrorCatcher(File newFile, String mod)
 {
 	try{
 		newFile.createNewFile();
 		System.out.println("Successfully created/read configuration file"); 
 	}
 
 	catch(IOException e){
 		System.out.println("Could not create configuration file for "+ mod + ". Reason:");
 		System.out.println(e);
 	}	
 }
 
 private static int getBlockID(int def, String BlockName)
 {int id = -1;
 	
 	
 if (autoAssign)
 		{
 	if(Block.blocksList[def] == null)
 	{
 		id = def;
 	}
 	else{
 	for(int i = def; i < 4096; i++)
 	{
 		if(Block.blocksList[i] == null & Item.itemsList[i] == null)
 		{
 			return id = i;
 		}
 	}
 	for(int i = def - 1; i > 0; i--)
 	{
 		if(Block.blocksList[i] == null & Item.itemsList[i] == null)
 		{
 			return id = i;
 		}
 	}
 	
 		}
 		}
 	if (!autoAssign)
 	{
 		id = BlockID;
 		if (Block.blocksList[id] != null)
 		{MinecraftForge.killMinecraft(MODNAME, "Block "+BlockName+" conflicts with "+Block.blocksList[id]+""+ " please set Auto Assign to true");}
 	}
 	
 	if (id == -1)
 		{MinecraftForge.killMinecraft(MODNAME, "Not Enough BlockIDs");}
 	
 	
 	return id;
 }
 
 private static int getItemID(int def)
 {
 	for(int i = def; i < 32000; i++)
 	{
 		if (Item.itemsList[i] == null)
 		{
 			return i;
 		}
 	}
 	for(int i = def; i > 0; i--)
 	{
 		if (Item.itemsList[i] == null)
 		{
 			return i;
 		}
 	}
 	return -1;
 }
 public static Configuration CreateConfig(String mod)
 {
 File newFile = GetFile(mod);
 ErrorCatcher(newFile, mod);
 return new Configuration(newFile);
 }
 
 }

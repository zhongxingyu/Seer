 package mods.pchan3;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import mods.pchan3.pirate.EntityPirate;
 import mods.pchan3.steamboat.EntitySteamBoat;
 import mods.pchan3.steamboat.ItemSteamBoat;
 import mods.pchan3.steamship.EntityAirship;
 import mods.pchan3.steamship.ItemAirship;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.biome.BiomeGenBase;
 
 import org.lwjgl.input.Keyboard;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 /**
 *
 * @author pchan3
 */
 @Mod(modid = "pchan3", name = "PChan3 mods", version = "0.4")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false,channels = {"Steamship" },
 		packetHandler = PacketHandler.class)
 public class PChan3Mods{
 	@Instance("pchan3")
 	public static PChan3Mods instance;
 	@SidedProxy(clientSide="mods.pchan3.ClientProxy", serverSide="mods.pchan3.CommonProxy")
 	public static CommonProxy proxy;
 	private static int steamboatItemID=12500,airshipItemID=12503,engineItemID=12502,balloonItemID=12501;
 	private static boolean  ENABLE_AIRSHIP=true,ENABLE_STEAMBOAT=true,ENABLE_PIRATE=true;
 	public static boolean SHOW_BOILER=true;
     public static Item airShip,engine,balloon,steamBoat;  
     public static int KEY_UP = Keyboard.KEY_NUMPAD8,KEY_DOWN = Keyboard.KEY_NUMPAD2;
     public static int KEY_CHEST = Keyboard.KEY_R,KEY_FIRE = Keyboard.KEY_NUMPAD5;
     public static int GUI_ID=100;
     private static String SPAWNABLE_BIOMES;
 			
 	 
   @PreInit
   public void preload(FMLPreInitializationEvent event){
 	  instance=this;
 	  // Read properties file.
 		Properties properties = new Properties();
 		File file=new File(getMinecraftBaseDir()+ "/config/pchan3.properties");
 		if (file.exists()){
 			try{
 			FileInputStream sourceFile =new FileInputStream(file);
 			try {
 				properties.load(sourceFile);
 				SHOW_BOILER=Boolean.parseBoolean(properties.getProperty("show_boiler"));
 				ENABLE_AIRSHIP=Boolean.parseBoolean(properties.getProperty("Enable_Airship"));
 				ENABLE_STEAMBOAT=Boolean.parseBoolean(properties.getProperty("Enable_Steamboat"));
 				ENABLE_PIRATE=Boolean.parseBoolean(properties.getProperty("Enable_Pirate"));
 				airshipItemID=Integer.parseInt(properties.getProperty("AirshipItemID"));
 				engineItemID=Integer.parseInt(properties.getProperty("EngineItemID"));
 				balloonItemID=Integer.parseInt(properties.getProperty("BalloonItemID"));
 				steamboatItemID=Integer.parseInt(properties.getProperty("SteamboatItemID"));
 				GUI_ID=Integer.parseInt(properties.getProperty("GUI_ID"));
 				SPAWNABLE_BIOMES=properties.getProperty("Pirate_spawn_in_Biomes");	   
 				} 
 			finally{sourceFile.close();}		
 			}catch (IOException e) 
 			{e.printStackTrace();}
 		}
 		else{
 			
 			StringBuilder sb = new StringBuilder("");
 			for(int m=0; m<BiomeGenBase.biomeList.length; m++)
 			{	
 				if(BiomeGenBase.biomeList[m]!=null )
 				{
 				sb.append(BiomeGenBase.biomeList[m].biomeName+",");
 				}
 			}
 		    properties.setProperty("show_boiler", "true");
 		    properties.setProperty("Enable_Airship", "true");
 		    properties.setProperty("Enable_Steamboat","true");
 		    properties.setProperty("Enable_Pirate", "true");
 		    properties.setProperty("AirshipItemID", "12503");
 		    properties.setProperty("EngineItemID", "12502");
 		    properties.setProperty("BalloonItemID", "12501");
 		    properties.setProperty("SteamboatItemID","12500");
 		    properties.setProperty("GUI_ID", "100");
 		    properties.setProperty("Pirate_spawn_in_Biomes",sb.toString());	
 		    SPAWNABLE_BIOMES=sb.toString();
 		    try{
 		// Write properties file.
 		    FileOutputStream fileOut = new FileOutputStream(file);
 		    	try{
 		    		properties.store(fileOut, null);
 		    	}finally{fileOut.close();}
 		    }
 			catch (IOException e) {e.printStackTrace();}		
 		}
   }
   @Init
     public void load(FMLInitializationEvent event) { 
 	  
 	  if (ENABLE_AIRSHIP)
 	{
 	// Engine
 	engine = new Item(engineItemID).setUnlocalizedName("pchan3:Engine").setCreativeTab(CreativeTabs.tabTransport);
 	LanguageRegistry.addName(engine, "Engine");
 	GameRegistry.addRecipe(new ItemStack(engine, 1), new Object[]{
 	    "###",
 	    "#X#",
 	    "###",
 	    Character.valueOf('#'), Item.ingotIron,
 	    Character.valueOf('X'), Block.pistonBase});
      
   // Balloon
 	balloon = new Item(balloonItemID).setUnlocalizedName("pchan3:Balloon").setCreativeTab(CreativeTabs.tabTransport);
 	LanguageRegistry.addName(balloon, "Balloon");
 	GameRegistry.addRecipe(new ItemStack(balloon, 1), new Object[]{
 	    "###",
 	    "###",
 	    "L L",
 	    Character.valueOf('#'), Item.leather,
 	    Character.valueOf('L'), Item.silk});
 	
 	//AirShip
     airShip = new ItemAirship(airshipItemID).setUnlocalizedName("pchan3:Airship");
 	LanguageRegistry.addName(airShip, "Airship");
 	EntityRegistry.registerModEntity(EntityAirship.class, "Airship", 1, this, 40, 1, true);
 	GameRegistry.addRecipe(new ItemStack(airShip, 1), new Object[]{
 	    "XBX",
 	    "EFE",
 	    "XDX",
 	    Character.valueOf('X'), Item.silk,
 	    Character.valueOf('B'), balloon,
 	    //Character.valueOf('C'), Block.chest,
 	    Character.valueOf('E'), engine,
 	    //Character.valueOf('L'), Block.dispenser,
 	    Character.valueOf('D'), Item.boat,
 	    Character.valueOf('F'), Block.furnaceIdle});
 		
 	}
 	//Boat
      if (ENABLE_STEAMBOAT)
  	{
 	steamBoat = new ItemSteamBoat(steamboatItemID).setUnlocalizedName("pchan3:Steamboat");
 	LanguageRegistry.addName(steamBoat, "Steam Boat");
 	EntityRegistry.registerModEntity(EntitySteamBoat.class, "SteamBoat", 2,this,40,1,false);
 	GameRegistry.addRecipe(new ItemStack(steamBoat, 1), new Object[] {
         "#X#",
         "###",
         Character.valueOf('#'), Block.planks,
         Character.valueOf('X'), Item.ingotIron
     });
  	}
   }  
   public static File getMinecraftBaseDir()
   {
       if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
       {
           return FMLClientHandler.instance().getClient().getMinecraftDir();
       }         
       return FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
   }
   private BiomeGenBase[] getAvailableBiomes(){
 	  String[] biome = SPAWNABLE_BIOMES.split(",");
 	  BiomeGenBase[] chosenBiome=BiomeGenBase.biomeList;
 	  boolean flag;
 	  for(int k=0;k<chosenBiome.length;k++)
 	  {
 		  flag=false;
 		  if(chosenBiome[k]!=null)
 		  {	
 			
 			for(int id=0;id<biome.length;id++)
 			{ 
				if(biome[id]!=null && chosenBiome[k].biomeName.equalsIgnoreCase(biome[id].trim()) && chosenBiome[k].getSpawnableList(EnumCreatureType.monster)!=null)
 				{			
 					flag = true;				
 				}
 			}		
 		  }
 		  if (!flag)
 		  {
 			  System.arraycopy(chosenBiome,k+1,chosenBiome,k,chosenBiome.length-1-k);
 		  }
 	  }
	return  chosenBiome; 
   }
   @PostInit
   public void modsLoaded(FMLPostInitializationEvent event)
   {
 	//Pirate
		if (!(!ENABLE_PIRATE || SPAWNABLE_BIOMES==""))
 		{			
 			EntityRegistry.registerModEntity(EntityPirate.class, "Pirate", 3, this, 80, 1, true);
 			try{//FIXME
 			EntityRegistry.addSpawn(EntityPirate.class, 6,1,5, EnumCreatureType.monster, getAvailableBiomes());
 			}
 			catch(NullPointerException n){
			EntityRegistry.addSpawn(EntityPirate.class, 6,1,5, EnumCreatureType.monster, BiomeGenBase.ocean);
 			}
 			LanguageRegistry.instance().addStringLocalization("entity.Pirate.name", "en_US", "Pirate");
 		}
 		proxy.registerRenderInformation();
 		NetworkRegistry.instance().registerGuiHandler(this,proxy);	  
   }
 }

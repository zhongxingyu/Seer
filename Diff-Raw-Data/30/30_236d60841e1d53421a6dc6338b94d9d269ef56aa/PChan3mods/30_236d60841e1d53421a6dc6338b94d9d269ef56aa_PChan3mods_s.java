 package mods.pchan3;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
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
 import net.minecraft.world.biome.SpawnListEntry;
 import net.minecraftforge.common.BiomeDictionary;
 import net.minecraftforge.common.Configuration;
 
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
 @Mod(modid = "pchan3", name = "PChan3 mods", version = "0.6")
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
     private static String[] SPAWNABLE_BIOMES=new String[]{"Ocean","Plains"};
 	private Configuration config;	
 	 
 	@PreInit
 	public void preload(FMLPreInitializationEvent event){
 		instance=this;
 		// Read properties file.
 		config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		SHOW_BOILER=config.get("general", "show_boiler", true).getBoolean(true);
 		ENABLE_AIRSHIP=config.get("general", "Enable_Airship", true).getBoolean(true);
 		ENABLE_STEAMBOAT=config.get("general", "Enable_Steamboat", true).getBoolean(true);
 		ENABLE_PIRATE=config.get("general", "Enable_Pirate", true).getBoolean(true);
 		airshipItemID=config.get("item", "AirshipID", airshipItemID).getInt();
 		engineItemID=config.get("item", "EngineID", engineItemID).getInt();
 		balloonItemID=config.get("item", "BalloonID", balloonItemID).getInt();
 		steamboatItemID=config.get("item", "SteamboatID", steamboatItemID).getInt();
 		GUI_ID=config.get("item", "GUI_ID", GUI_ID).getInt();	   
 		
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
 	private BiomeGenBase[] getAvailableBiomes(){
 		  ArrayList<BiomeGenBase> result=new ArrayList<BiomeGenBase>();
 		  Iterator<BiomeGenBase> itr = Arrays.asList(BiomeGenBase.biomeList).iterator();
 		  BiomeGenBase biome=null;
 		  while(itr.hasNext())
 		  {
 				biome=itr.next();
 				if(biome!=null)
 					for(int id=0;id<SPAWNABLE_BIOMES.length;id++)
 					{ 
 						if(SPAWNABLE_BIOMES[id]!=null &&  SPAWNABLE_BIOMES[id]!="" && biome.biomeName.equalsIgnoreCase(SPAWNABLE_BIOMES[id].trim()))
 						{			
 							result.add(biome);
 							break;
 						}
 					}
 		  }
 		  result.trimToSize();
 		  return (result.size()!=0 && !result.isEmpty())?(BiomeGenBase[]) result.toArray(new BiomeGenBase[result.size()]):null; 
 	}
 	@PostInit
 	public void modsLoaded(FMLPostInitializationEvent event)
 	{
 	//Pirate
 		if (ENABLE_PIRATE)
 		{
 			SPAWNABLE_BIOMES=config.get("general", "Pirate_spawn_in_Biomes", SPAWNABLE_BIOMES).getStringList();
 			if (SPAWNABLE_BIOMES!=null && SPAWNABLE_BIOMES.length!=0){		
 				EntityRegistry.registerModEntity(EntityPirate.class, "Pirate", 3, this, 80, 1, true);
 				BiomeGenBase[] biomes=getAvailableBiomes();
 				for(int i = 0; i < biomes.length ; i++)
 				{
 					List spawns=null;
 					if( biomes[i]!=null)
 						spawns = biomes[i].getSpawnableList(EnumCreatureType.monster);
 					if (spawns!=null){
 						spawns.add(new SpawnListEntry(EntityPirate.class, 2, 1, 5));
 						System.out.println("Pirate added to biome "+biomes[i].biomeName);
 					}
 				}
 				
 				LanguageRegistry.instance().addStringLocalization("entity.Pirate.name", "en_US", "Pirate");
 			}
 		}
 		if(config.hasChanged())
 			config.save();
 		proxy.registerRenderInformation();
 		NetworkRegistry.instance().registerGuiHandler(this,proxy);	  
 	}
 }

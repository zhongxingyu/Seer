 package Abascus.DLCCraft.common;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import Abascus.DLCCraft.common.Client.RenderTickHandler;
 import Abascus.DLCCraft.common.Client.TickHandlerClient;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = "Abascus_DLCCraft", name = "DLC Craft", version = "0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "DLCCraft" }, packetHandler = Abascus.DLCCraft.common.PacketHandler.class)
 public class DLCCraft 
 {
 	@SidedProxy(clientSide = "Abascus.DLCCraft.common.Client.ClientProxy", serverSide = "Abascus.DLCCraft.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	@Instance("Abascus_DLCCraft")
 	public static DLCCraft instance;
 
 	public boolean startUpInfo = true;
 
 	public String[] Msg = {"Abc", "def"};
 	public String[] Capes;
 
 	public int CoinID = 800;
 	public int DLCID = 801;
 	public int ShopID = 802;
 	public DLCCraftItem coin;
 	public Item dlc;
 	public Item shop;
 	public static PlayerTracker playerTracker;
 	
 	public DLCCraft()
 	{
 
 	}
 
 	@EventHandler
 	public void pre(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		startUpInfo = config.get(Configuration.CATEGORY_GENERAL, "Has Startup Info",true).getBoolean(true);
 		CoinID = config.get(Configuration.CATEGORY_ITEM, "Coin Item ID",800).getInt();
 		DLCID = config.get(Configuration.CATEGORY_ITEM, "DLC Item ID",801).getInt();
 		ShopID = config.get(Configuration.CATEGORY_ITEM, "Shop Item ID",802).getInt();
 
 		config.save();
 		//Msg = grab("https://dl.dropboxusercontent.com/u/58920433/Mods%20Download/DLCCraft/Msg.txt");
 		//Capes = grab("https://dl.dropboxusercontent.com/u/58920433/Mods%20Download/DLCCraft/Capes.txt");
 
 		coin = (DLCCraftItem) (new DLCCraftItem(CoinID)).setUnlocalizedName("coin").setCreativeTab(CreativeTabs.tabMaterials);
 		dlc = (new DLCCraftItem(DLCID)).setUnlocalizedName("dlc").setCreativeTab(CreativeTabs.tabMaterials);
 		shop = (new DLCCraftItem(ShopID)).setUnlocalizedName("shop").setCreativeTab(CreativeTabs.tabMaterials);
 
 		LanguageRegistry.instance().addName(coin, "Coin");
 		LanguageRegistry.instance().addName(dlc, "DLC");
 		LanguageRegistry.instance().addName(shop, "DLC Shop");
 		
 		
 		WeightedRandomChestContent dlc1 = new WeightedRandomChestContent(new ItemStack(dlc, 1), 1, 4, 2);
 		WeightedRandomChestContent dlc2 = new WeightedRandomChestContent(new ItemStack(dlc, 1), 1, 3, 0);
 		
 		ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, dlc1);
 		ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, dlc2);
 		ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, dlc2);
 		ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, dlc2);
 		ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, dlc2);
 		ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, dlc2);
 
 		MinecraftForge.EVENT_BUS.register(new EventManager());
 		
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		proxy.registerRenderInformation();
 
 		playerTracker = new PlayerTracker();
 		GameRegistry.registerPlayerTracker(playerTracker);
 		MinecraftForge.EVENT_BUS.register(playerTracker);
 		 
 		TickRegistry.registerTickHandler(new Tickhandler(), Side.SERVER);
 		
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) 
 	{
 	}
 
 	public static String[] grab(String location)
 	{
 		try
 		{
 			HttpURLConnection conn = null;
 			while (location != null && !location.isEmpty())
 			{
 				URL url = new URL(location);
 				conn = (HttpURLConnection) url.openConnection();
 				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
 				conn.connect();
 				location = conn.getHeaderField("Location");
 			}
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
 			String line = null;
 			ArrayList<String> changelog = new ArrayList<String>();
 			while ((line = reader.readLine()) != null)
 			{
 				if (line.startsWith("#"))
 				{
 					continue;
 				}
 				if (line.isEmpty())
 				{
 					continue;
 				}
 
 				changelog.add(line);
 			}
 
 			return changelog.toArray(new String[0]);
 
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 		return new String[]{"Unable to get Mod Information"};
 	}
 
 }

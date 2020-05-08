 package mods.learncraft.common;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import mods.learncraft.commands.CommandReady;
 import mods.learncraft.commands.CommandTeamscore;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockChest;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarted;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.client.gui.achievement.*;
 import net.minecraft.command.ICommandManager;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.stats.Achievement;
 import net.minecraft.stats.AchievementList;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 
 @Mod(modid="MC_LearnCraft", name="LearnCraft", version="0.8")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 
 public class Common {
 	
     // The instance of your mod that Forge uses.
     @Instance("Common")
     public static Common instance;
 
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="mods.learncraft.client.ClientProxy", serverSide="mods.learncraft.common.CommonProxy")
     public static CommonProxy proxy;
     
     public static int DiggerID;
     public static Digger Dig;
     
     public static int LCBlockID;
     public static Block LCBlock;
     
     public static int BlockBorderID;
     public static Block BorderBlock;
     
     public static int InvisibleBlockID;
     public static Block InvisibleBlock;
     
     public static DBQueries dbqueries = null;
 
     public static int LBlockChestID;
     public static LBlockChest lchest;
     
     public static int TeamChestID;
     public static TeamChest TeamChest;
     
     public static List<EntityPlayer> playerlist = new ArrayList<EntityPlayer>();
 
     public static int currentNumPlayers = 0;
     public static int playersReady = 0;
     public static Team blueteam = new Team("blue");
     public static Team goldteam = new Team("gold");
     public static Team winningteam = null;
     
     public static LinkedList<String> notifications = new LinkedList<String>();
     
     public static boolean inProgress = false;
     public static boolean teleportOn = true;
     
     // Use the state to iterate through the different phases of the arena game
     public static String state = "";
     
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		
 		config.load();
 		System.out.println(config.toString());
 		LCBlockID = config.getBlock("LCBlock", 501).getInt();
 		LBlockChestID = config.getBlock("lchest", 502).getInt();
 		BlockBorderID = config.getBlock("BorderBlock", 503).getInt();
 		DiggerID = config.getBlock("Dig", 504).getInt();
 		InvisibleBlockID = config.getBlock("InvisibleBlock", 505).getInt();
 		TeamChestID = config.getBlock("TeamChest", 506).getInt();
 		
 		config.save();
     }
     
     public static Block testBlock;
     public static Block testBlock2;
     public static Block testBlock3;
     public static Block testBlock4;
     public static Block blockDesignateGold;
     public static Block blockDesignateBlue;
     
     @Init
     public void load(FMLInitializationEvent event) {
     	
     	LCBlock = (new LBlock(LCBlockID, Material.iron)).setUnlocalizedName("lcblock");
         LanguageRegistry.addName(LCBlock, "Learning block");
         GameRegistry.registerBlock(LCBlock, "lcblock");
         
         BorderBlock = (new BorderBlock(BlockBorderID, Material.iron)).setUnlocalizedName("border_block");
         LanguageRegistry.addName(BorderBlock, "Border Block");
         GameRegistry.registerBlock(BorderBlock, "border_block");
 
         lchest = (LBlockChest) (new LBlockChest(LBlockChestID, 0)).setUnlocalizedName("lc_chest");
         LanguageRegistry.addName(lchest, "Learning chest");
         GameRegistry.registerBlock(lchest, "lc_chest");
         GameRegistry.registerTileEntity(TileEntityLChest.class, "LChest.chest");
         
         Dig = (Digger) (new Digger(DiggerID)).setUnlocalizedName("digger");
         LanguageRegistry.addName(Dig, "Digger");
         GameRegistry.registerItem(Dig, "Digger");
         
         InvisibleBlock = new InvisibleBlock(InvisibleBlockID, Material.air).setUnlocalizedName("invisBlock");
         LanguageRegistry.addName(InvisibleBlock, "Invisible Block");
         GameRegistry.registerBlock(InvisibleBlock, "Invisible Block");
         
         TeamChest = (TeamChest) (new TeamChest(TeamChestID, 0)).setUnlocalizedName("teamchest");
         LanguageRegistry.addName(TeamChest, "TeamChest");
         GameRegistry.registerBlock(TeamChest, "teamchest");
         GameRegistry.registerTileEntity(TileEntityTeamChest.class, "TeamChest.chest");
     	
 		
 		testBlock = new BlockTestBlock(515, 
 				Material.rock).setUnlocalizedName("testblock");     														
 		GameRegistry.registerBlock(testBlock, "testblock");	
		LanguageRegistry.addName(testBlock, "Gold Up");
 		
 		testBlock2 = new BlockTestBlock2(516, 
 				Material.rock).setUnlocalizedName("testblock2");     														
 		GameRegistry.registerBlock(testBlock2, "testblock2");		
 		LanguageRegistry.addName(testBlock2, "Blue Up");
 
 		
 		testBlock3 = new BlockTestBlock3(517, 
 				Material.rock).setUnlocalizedName("testblock3");     														
 		GameRegistry.registerBlock(testBlock3, "testblock3");		
 		LanguageRegistry.addName(testBlock3, "Gold Down");
    	
        	   	
 		testBlock4 = new BlockTestBlock4(518, 
 				Material.rock).setUnlocalizedName("testblock4");     														
 		GameRegistry.registerBlock(testBlock4, "testblock4");		
 		LanguageRegistry.addName(testBlock4, "Blue Down");
 		
 		blockDesignateGold = new BlockTeamDesignateGold(519, 
 				Material.rock).setUnlocalizedName("blockDesignateGold");     														
 		GameRegistry.registerBlock(blockDesignateGold, "blockDesignateGold");	
 		LanguageRegistry.addName(blockDesignateGold, "Gold Team Designation");
         
 		blockDesignateBlue = new BlockTeamDesignateBlue(520, 
 				Material.rock).setUnlocalizedName("blockDesignateBlue");     														
 		GameRegistry.registerBlock(blockDesignateBlue, "blockDesignateBlue");	
 		LanguageRegistry.addName(blockDesignateBlue, "Blue Team Designation");
         
         
         
         proxy.registerTileEntitySpecialRenderer();
     	proxy.registerRenderThings();
     	
     	// Add DBQueries to the Common handler
 		try {
 			dbqueries = new DBQueries();
 		} catch (SQLException e) {
 			// e.printStackTrace();
 		}
 		
 		MinecraftForge.EVENT_BUS.register(new EventHookContainerClass());
 		NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
 
     }
     
     @ServerStarting
     public void serverStart(FMLServerStartingEvent event)
     {
 		MinecraftServer server = MinecraftServer.getServer(); //Gets current server
 		ICommandManager command = server.getCommandManager(); //Gets the command manager to use for server
 		ServerCommandManager serverCommand = ((ServerCommandManager) command); //Turns it into another form to use
 		
 		serverCommand.registerCommand(new CommandTeamscore());
 		serverCommand.registerCommand(new CommandReady());
     }
     
     
     public void registerRenderInformation()
     {
 
     }
 
     public void registerTileEntitySpecialRenderer()
     {
 
     }
 
 	public static Team getTeam(EntityPlayer player) {
 		// TODO Auto-generated method stub
 		if(Common.blueteam.hasPlayer(player)) {
 			return Common.blueteam;
 		} else if(Common.goldteam.hasPlayer(player)) {
 			return Common.goldteam;
 		}
 		return null;
 	}
 
 	public static void teleportPlayerTo(EntityPlayer player, String loc) {
 		if(teleportOn) {
 			float f = 1.0F;
 			double x = 0, y = 0, z = 0;
 			if(loc.matches("choose_team")) {
 				// z = 604 - 592
 				// x = 55.45 - 58.1
 				x = 57.81;
 				y = 123;
 				z = 600.58;
 			}
 			if(loc.matches("gold_spawn")) {
 				// z = 604 - 592
 				// x = 218 - 202
 				x = 207.96;
 				y = 118;
 				z = 698.06;
 			}
 			if(loc.matches("blue_spawn")) {
 				x = 211;
 				y = 116.1;
 				z = 499;
 			}
 			if(loc.matches("gold_arena")) {
 				x = 117.17;
 				y = 74;
 				z = 548.35;
 			}
 			if(loc.matches("blue_arena")) {
 				x = 299.42;
 				y = 74;
 				z = 649.95;
 			}
 			if(loc.matches("maze_spawn")) {
 				x = 264.65;
 				y = 4.1;
 				z = 464.49;
 			}
 			player.setPosition(x, y, z);
 		}
 	}
 
 	public static void announce(String message) {
 		Common.notifications.add(message);
 	}
 
 	public static void setTeamWon(Team team) {
 		winningteam = team;
 	}
 
 }

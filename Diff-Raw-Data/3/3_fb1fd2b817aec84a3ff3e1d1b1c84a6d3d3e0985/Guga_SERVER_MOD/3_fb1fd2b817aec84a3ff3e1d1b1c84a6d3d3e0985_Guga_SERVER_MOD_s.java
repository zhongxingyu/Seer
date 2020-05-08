 package me.Guga.Guga_SERVER_MOD;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import me.Guga.Guga_SERVER_MOD.Handlers.ChatHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GameMasterHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaAuctionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaBanHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaCommands;
 import me.Guga.Guga_SERVER_MOD.Handlers.HomesHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.PlacesHandler;
 //import me.Guga.Guga_SERVER_MOD.Handlers.GugaFlyHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaMCClientHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaRegionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.SpawnsHandler;
 import me.Guga.Guga_SERVER_MOD.Listeners.GugaBlockListener;
 import me.Guga.Guga_SERVER_MOD.Listeners.GugaEntityListener;
 import me.Guga.Guga_SERVER_MOD.Listeners.GugaMessageListener;
 import me.Guga.Guga_SERVER_MOD.Listeners.GugaPlayerListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 //import Native.*;
 public class Guga_SERVER_MOD extends JavaPlugin
 {	
 	public void onDisable() 
 	{
 		log.info("GUGA MINECRAFT SERVER MOD has been disabled.");
 		GugaEvent.ClearAllGroups();
 		SaveProfessions();
 		SaveCurrency();
 		GugaAnnouncement.SaveAnnouncements();
 		GugaPort.SavePlaces();
 		GugaRegionHandler.SaveRegions();
 		GugaAuctionHandler.SaveAuctions();
 		GugaAuctionHandler.SavePayments();
 		GugaBanHandler.SaveBans();
 		PlacesHandler.savePlaces();
 		//GugaFlyHandler.SaveFly();
 		SpawnsHandler.SaveSpawns();
 		arena.SavePvpStats();
 		arena.SaveArenas();
 		logger.SaveWrapperBreak();
 		logger.SaveWrapperPlace();
 	}
 
 	public void onEnable() 
 	{	
 		PluginManager pManager = this.getServer().getPluginManager();
 		pManager.registerEvents(pListener, this);
 		pManager.registerEvents(bListener, this);
 		pManager.registerEvents(enListener, this);
 		
 		
 		GugaMCClientHandler.SetPlugin(this);
 		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "Guga");
 		Bukkit.getMessenger().registerIncomingPluginChannel(this, "Guga", msgListener);
 		GugaPort.SetPlugin(this);
 		GugaCommands.SetPlugin(this);
 		GugaAnnouncement.SetPlugin(this);
 		AutoSaver.SetPlugin(this);
 		GugaRegionHandler.SetPlugin(this);
 		GugaAuctionHandler.SetPlugin(this);
 		GameMasterHandler.SetPlugin(this);
 		GugaBanHandler.SetPlugin(this);
 		GugaEvent.SetPlugin(this);
 		GugaParty.SetPlugin(this);
 		GugaTeams.SetPlugin(this);
 		//GugaFlyHandler.SetPlugin(this);
 		ChatHandler.SetPlugin(this);
 		BasicWorld.SetPlugin(this);
 		SpawnsHandler.SetPlugin(this);
 		PlacesHandler.setPlugin(this);
 		HomesHandler.setPlugin(this);
 
 		if (getServer().getWorld("arena") == null)
 		{
 			//getServer().createWorld("arena", Environment.NORMAL);
 			getServer().createWorld(WorldCreator.name("arena").environment(Environment.NORMAL));
 		}
 		if(getServer().getWorld("world_event")==null)
 		{
 			getServer().createWorld(WorldCreator.name("world_event").environment(Environment.NORMAL));
 		}
 		if(getServer().getWorld("world_basic")==null)
 		{
 			getServer().createWorld(WorldCreator.name("world_basic").environment(Environment.NORMAL));
 		}
 		if(getServer().getWorld("world_mine")==null)
 		{
 			getServer().createWorld(WorldCreator.name("world_mine").environment(Environment.NORMAL));
 		}
 		if(getServer().getWorld("world_adventure")==null)
 		{
 			getServer().createWorld(WorldCreator.name("world_adventure").environment(Environment.NORMAL));
 		}
 		arena.LoadArenas();
 		arena.LoadPvpStats();
 		getServer().getWorld("arena").setPVP(true);
 		getServer().getWorld("arena").setFullTime(4000);
 		getServer().getWorld("world").setPVP(false);
 		getServer().getWorld("world").setSpawnFlags(true, true);
 		getServer().getWorld("world_nether").setPVP(false);
 		getServer().getWorld("arena").setSpawnFlags(false, false);
 		getServer().getWorld("world_event").setPVP(false);
 		getServer().getWorld("world_event").setSpawnFlags(false, false);
 		getServer().getWorld("world_basic").setPVP(false);
 		getServer().getWorld("world_basic").setSpawnFlags(true, true);
 		getServer().getWorld("world_mine").setFullTime(4000);
 		getServer().getWorld("world_mine").setPVP(false);
 		getServer().getWorld("world_mine").setSpawnFlags(false, false);
 		scheduler = getServer().getScheduler();
 		LoadProfessions();
 		LoadCurrency();
 		//GugaPort.LoadPlaces();
 		GugaRegionHandler.LoadRegions();
 		GugaAuctionHandler.LoadAuctions();
 		GugaAuctionHandler.LoadPayments();
 		GugaBanHandler.LoadBans();
 		chests = new GugaChests(this);
 		furnances = new GugaFurnances(this);
 		dispensers = new GugaDispensers(this);
 		GameMasterHandler.LoadGMs();
 		GugaAnnouncement.LoadAnnouncements();
 		GugaAnnouncement.StartAnnouncing();
 		GugaMCClientHandler.LoadMACWhiteList();
 		GugaMCClientHandler.LoadMinecraftOwners();
 		GugaPlayerListener.LoadCreativePlayers();
 		PlacesHandler.loadPlaces();
 		//GugaFlyHandler.LoadFly();
 		GugaBanHandler.LoadIpWhiteList();
 		SpawnsHandler.LoadSpawns();
 		HomesHandler.loadHomes();
 		AutoSaver.StartSaver();
 		//this.socketServer = new GugaSocketServer(12451, this);
 		//this.socketServer.ListenStart();
 		GugaMCClientHandler.ReloadSkins();
 		log.info("GUGA MINECRAFT SERVER MOD " + version + " is running.");
 		log.info("Created by Guga 2011.");
 	}
 	public void SaveCurrency()
 	{
 		log.info("Saving Currency Data...");
 		GugaFile file = new GugaFile(currencyFile, GugaFile.WRITE_MODE);
 		file.Open();
 		String line;
 		String currency;
 		String vipExp;
 		String name;
 		Iterator<GugaVirtualCurrency> i = playerCurrency.iterator();
 		while (i.hasNext())
 		{
 			GugaVirtualCurrency p = i.next();
 			name = p.GetPlayerName();
 			vipExp = Long.toString(p.GetExpirationDate());
 			currency = Integer.toString(p.GetCurrency());
 			line = name + ";" + currency + ";" + vipExp;
 			file.WriteLine(line);
 		}
 		file.Close();
 	}
 	public void LoadCurrency()
 	{
 		log.info("Loading Currency Data...");
 		GugaFile file = new GugaFile(currencyFile, GugaFile.READ_MODE);	
 		file.Open();
 		String line;
 		String []splittedLine;
 		long vipExp;
 		String name;
 		int currency;
 		while ((line = file.ReadLine()) != null)
 		{
 			splittedLine = line.split(";");
 			name = splittedLine[0];
 			currency = Integer.parseInt(splittedLine[1]);				
 			vipExp = Long.parseLong(splittedLine[2]);
 			playerCurrency.add(new GugaVirtualCurrency(this, name, currency,new Date(vipExp)));
 		}
 		file.Close();
 	}
 	public void SaveProfessions()
 	{
 		log.info("Saving Professions Data...");
 		GugaFile file = new GugaFile(professionsFile, GugaFile.WRITE_MODE);
 		file.Open();
 		String line;
 		Collection<GugaProfession> profCollection;
 		profCollection = professions.values();
 		Object[] objectArray;
 		objectArray = profCollection.toArray();
 		GugaProfession prof;
 		int i =0;
 		while (i<objectArray.length)
 		{
 			prof = (GugaProfession) objectArray[i];
 			line = prof.GetPlayerName() + ";" + prof.GetProfession() + ";" + prof.GetXp();
 			file.WriteLine(line);
 			i++;
 		}
 		file.Close();
 	}
 	public void LoadProfessions()
 	{
 		log.info("Loading Professions Data...");
 		GugaFile file = new GugaFile(professionsFile, GugaFile.READ_MODE);	
 		file.Open();
 		String line;
 		String []splittedLine;
 		String pName;
 		String profName;
 		String xp;
 		while ((line = file.ReadLine()) != null)
 		{
 			splittedLine = line.split(";");
 			pName = splittedLine[0];
 			profName = splittedLine[1];
 			xp = splittedLine[2];
 			if (profName.matches("Miner"))
 			{
 				professions.put(pName, new GugaProfession(pName,Integer.parseInt(xp),this));
 			}
 			else if (profName.matches("Hunter"))
 			{
 				professions.put(pName, new GugaProfession(pName,Integer.parseInt(xp),this));
 			}
 			else if (profName.matches("Profession"))
 			{
 				professions.put(pName, new GugaProfession(pName,Integer.parseInt(xp),this));
 			}
 		}
 		file.Close();
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		if (sender instanceof Player)
 		{
 		//*****************************************/who*****************************************
 		 if(cmd.getName().equalsIgnoreCase("who") && (sender instanceof Player))
 		 { 
 		   GugaCommands.CommandWho((Player)sender);
 		   return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("event"))
 		 {
 			 GugaCommands.CommandEvent((Player)sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("team"))
 		 {
 			 GugaCommands.CommandTeam((Player)sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("socket"))
 		 {
 			 	GugaCommands.TestCommand(args);
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("places") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandPlaces((Player)sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("ah") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandAH((Player)sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("arena") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandArena((Player) sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("ew") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandEventWorld((Player) sender, args);
 			 return true;
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("aw") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandAdventureWorld((Player) sender, args);
 			 return true;
 		 }
 		 else if(cmd.getName().equalsIgnoreCase("debug") && (sender instanceof ConsoleCommandSender))
 		 {
 			 GugaCommands.CommandDebug();
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("shop")) && (sender instanceof Player))		
 		 {
 			 GugaCommands.CommandShop((Player)sender,args);	 
 			 return true;
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("vip")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandVIP((Player)sender, args);
 			 return true;
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("pp")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandPP((Player)sender, args);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("invite")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandInvite((Player)sender, args);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("p")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandSendPartyMsg((Player)sender, args);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("party")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandParty((Player)sender, args);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("locker")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandLocker((Player)sender);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("fly")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandFly((Player)sender, args);
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("home")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandHome((Player)sender, args);
 			 return true;
 		 }
 		 else if ((cmd.getName().equalsIgnoreCase("world")) && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandWorld((Player)sender);
 			 return true;
 		 }
 		//*****************************************module*****************************************
 		 else if(cmd.getName().equalsIgnoreCase("module") && (sender instanceof ConsoleCommandSender))
 		 {
 			 GugaCommands.CommandModule(args);
 			 return true;
 		 }
 		//*****************************************/help*****************************************
 		 else if (cmd.getName().equalsIgnoreCase("help"))
 		 {
 			 if (sender instanceof Player)
 			 {
 				 GugaCommands.CommandHelp((Player) sender);
 				 return true;
 			 }
 			 else if (sender instanceof ConsoleCommandSender)
 			 {
 				 log.info("module	-	enables or disables specified module");
 				 return true;
 			 }
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("r"))
 		 {
 			 if (sender instanceof Player)
 			 {
 				 GugaCommands.CommandReply((Player) sender, args);
 				 return true;
 			 }
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("gm"))
 		 {
 			 if (sender instanceof Player)
 			 {
 				 GugaCommands.CommandGM((Player)sender,args);
 			 }
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("rpg"))
 		 {
 			 if (sender instanceof Player)
 			 {
 				 GugaCommands.CommandRpg((Player)sender,args);
 				 return true;
 			 }
 		 }
 		 else if (cmd.getName().equalsIgnoreCase("feedback") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandFeedback((Player) sender, args);
 			 return true;
 		 }
 		 //*****************************************/status*****************************************
 		 else if(cmd.getName().equalsIgnoreCase("y") && (sender instanceof Player))
 		 {
 			 GugaCommands.CommandConfirm((Player)sender,args);
 		 }
 		 //*****************************************/register*****************************************
 		 /*else if(cmd.getName().equalsIgnoreCase("register") && (sender instanceof Player))
 		 {
 			if (config.accountsModule)
 			{
 				GugaCommands.CommandRegister((Player)sender, args);
 				return true;
 			}			
 			else
 			{
 				sender.sendMessage("This is not enabled on this server!");
 				return true;
 			}
 		 }*/
 		 /*else if(cmd.getName().equalsIgnoreCase("password") && (sender instanceof Player))
 		 {
 			 if (config.accountsModule)
 				{
 				 GugaCommands.CommandPassword((Player)sender, args);
 					return true;
 				}			
 				else
 				{
 					sender.sendMessage("This is not enabled on this server!");
 					return true;
 				}
 		 }*/
 		//*****************************************/lock*****************************************
 		 else if(cmd.getName().equalsIgnoreCase("lock") && (sender instanceof Player))
 		 {
 			 if (config.chestsModule)
 			 {
 				 GugaCommands.CommandLock((Player)sender);
 				 return true;
 			 }
 			 else
 			 {
 				 Player p = (Player)sender;
 				 p.sendMessage("This is not enabled on this server!");
 				 return true;
 			 }	
 		 }
 		//*****************************************/unlock*****************************************
 		 else if(cmd.getName().equalsIgnoreCase("unlock") && (sender instanceof Player))
 		 {
 			 if (config.chestsModule)
 			 {
 				 GugaCommands.CommandUnlock((Player)sender);		
 				 return true;
 			 }
 			 else
 			 {
 				 Player p = (Player)sender;
 				 p.sendMessage("This is not enabled on this server!");
 				 return true;
 			 }
 		 }
 		//*****************************************/login*****************************************
 		 else if(cmd.getName().equalsIgnoreCase("login") && (sender instanceof Player))
 		 {
 			 if (config.accountsModule)
 			 {	 
 				 GugaCommands.CommandLogin((Player)sender, args);
 				 return true;
 			 }
 			 else
 			 {
 				 sender.sendMessage("This is not enabled on this server!");
 				 return true;
 			 }
 		 }
		 return false;
 	}
 	public GugaVirtualCurrency FindPlayerCurrency(String pName)
 	{
 		Iterator<GugaVirtualCurrency> i = playerCurrency.iterator();
 		while (i.hasNext())
 		{
 			GugaVirtualCurrency p = i.next();
 			if (p.GetPlayerName().equalsIgnoreCase(pName))
 			{
 				return p;
 			}
 		}
 		return null;
 	}
 	public void GenerateBlockType(Player p, int typeID, int x, int y, int z)
 	{
 		Location baseLoc = p.getTargetBlock(null, 50).getLocation();
 		int xBase = baseLoc.getBlockX();
 		int yBase = baseLoc.getBlockY();
 		int zBase = baseLoc.getBlockZ();
 		int x2 = x;
 		int y2 = y;
 		int z2 = z;
 		Block block;
 		int i = 0;
 		if ( y < 0)
 		{
 			y2 = 0;
 			i += y + 1;
 		}
 		else
 		{
 			y2 = y-1;
 		}
 		while (i <= y2)
 		{
 			int i2 = 0;
 			if (z <0 )
 			{
 				z2 = 0;
 				i2 += z + 1;
 			}
 			else
 			{
 				z2 = z-1;
 			}
 			while (i2<=z2)
 			{
 				int i3 = 0;
 				if (x<0)
 				{
 					x2 = 0;
 					i3 += x + 1;
 				}
 				else
 				{
 					x2 = x-1;
 				}
 				while (i3<=x2)
 				{
 					block = p.getWorld().getBlockAt(xBase+i3, yBase+i, zBase+i2);
 					block.setTypeId(typeID);
 					i3++;
 				}
 				i2++;
 			}
 			i++;
 		}
 	}
 	
 	public void GenerateBlockType2(Player p, int typeID1, int typeID2, int x, int y, int z)
 	{
 		Location baseLoc = p.getTargetBlock(null, 50).getLocation();
 		int xBase = baseLoc.getBlockX();
 		int yBase = baseLoc.getBlockY();
 		int zBase = baseLoc.getBlockZ();
 		int x2 = x;
 		int y2 = y;
 		int z2 = z;
 		int i = 0;
 		if ( y < 0)
 		{
 			y2 = 0;
 			i += y + 1;
 		}
 		else
 		{
 			y2 = y-1;
 		}
 		while (i <= y2)
 		{
 			int i2 = 0;
 			if (z <0 )
 			{
 				z2 = 0;
 				i2 += z + 1;
 			}
 			else
 			{
 				z2 = z-1;
 			}
 			while (i2<=z2)
 			{
 				int i3 = 0;
 				if (x<0)
 				{
 					x2 = 0;
 					i3 += x + 1;
 				}
 				else
 				{
 					x2 = x-1;
 				}
 				while (i3<=x2)
 				{
 					if (p.getWorld().getBlockTypeIdAt(xBase+i3, yBase+i, zBase+i2)==typeID1)
 					{
 						p.getWorld().getBlockAt(xBase+i3, yBase+i, zBase+i2).setTypeId(typeID2,true);
 					}
 					i3++;
 				}
 				i2++;
 			}
 			i++;
 		}
 	}
 	public Location GetAvailablePortLocation(Location loc)
 	{
 		Location tpLoc = loc.getWorld().getHighestBlockAt(loc).getLocation();
 		boolean canTeleport = false;
 		int i = loc.getBlockY();
 		while (!canTeleport)
 		{
 			loc = tpLoc;
 			loc.add(0, 1, 0);
 			if (loc.getBlock().getTypeId() == 0)
 			{
 				if (loc.getBlock().getRelative(BlockFace.UP).getTypeId() == 0)
 				{
 					tpLoc = loc;
 					break;
 				}
 			}
 			if (i >= 127)
 			{
 				break;
 			}
 			i++;
 		}
 		return tpLoc;
 	}
 	
 	public HashMap<String,GugaProfession> professions = new HashMap<String,GugaProfession>();
 	
 	// ************* chances *************
 	public int IRON = 0;
 	public int GOLD = 1;
 	public int DIAMOND = 2;
 	public int EMERALD = 3;
 	public boolean debug = false;
 	public boolean redstoneDebug = false;
 	
 	public static final String version = "3.5.4";
 	private static final String professionsFile = "plugins/Professions.dat";
 	private static final String currencyFile = "plugins/Currency.dat";
 
 	public final Logger log = Logger.getLogger("Minecraft");
 	public BukkitScheduler scheduler;
 	
 	//public GugaSocketServer socketServer;
 	public final GugaConfiguration config = new GugaConfiguration(this);
 	public final GugaPlayerListener pListener = new GugaPlayerListener(this);
 	public final GugaEntityListener enListener = new GugaEntityListener(this);
 	public final GugaBlockListener bListener = new GugaBlockListener(this);
 	public final GugaMessageListener msgListener = new GugaMessageListener(this);
 	public final GugaAccounts acc = new GugaAccounts(this);
 	public GugaChests chests;
 	public GugaFurnances furnances;
 	public GugaDispensers dispensers;
 	public final GugaLogger logger = new GugaLogger(this);
 	public GugaArena arena = new GugaArena(this);
 	public GugaEventWorld EventWorld = new GugaEventWorld(this);
 	public AdventureWorld AdventureWorld = new AdventureWorld(this);
 	public ArrayList<GugaVirtualCurrency> playerCurrency = new ArrayList<GugaVirtualCurrency>();
 	public HashMap <Player,GugaAccounts> accounts = new HashMap<Player, GugaAccounts>();
 }

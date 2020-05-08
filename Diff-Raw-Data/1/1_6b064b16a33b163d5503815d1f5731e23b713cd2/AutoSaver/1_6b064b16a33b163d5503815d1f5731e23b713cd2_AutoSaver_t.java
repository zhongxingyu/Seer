 package me.Guga.Guga_SERVER_MOD;
 
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaAuctionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaBanHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaRegionHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.PlacesHandler;
 import me.Guga.Guga_SERVER_MOD.Handlers.SpawnsHandler;
 
 
 public abstract class AutoSaver 
 {
 	public static void SetPlugin(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	
 	public static void StartSaver()
 	{
 		plugin.scheduler.scheduleAsyncRepeatingTask(plugin, new Runnable() {
 			public void run()
 			{
 				SaveWorldStructures();
 				plugin.log.info("[AutoSaver] Saving worlds...");
 			}
 		}, 6000, 6000);
 	}
 	
 	public static void SaveWorldStructures()
 	{
 		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-all");
 		plugin.getServer().savePlayers();
 	}
 	public static void SaveAll()
 	{
 		plugin.SaveProfessions();
 		plugin.SaveCurrency();
 		GugaAnnouncement.SaveAnnouncements();
 		GugaPort.SavePlaces();
 		GugaRegionHandler.SaveRegions();
 		GugaAuctionHandler.SaveAuctions();
 		GugaAuctionHandler.SavePayments();
 		GugaBanHandler.SaveBans();
 		PlacesHandler.savePlaces();
 		SpawnsHandler.SaveSpawns();
 		plugin.arena.SavePvpStats();
 		plugin.arena.SaveArenas();
 		plugin.logger.SaveWrapperBreak();
 		plugin.logger.SaveWrapperPlace();
 		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-all");
 		plugin.getServer().savePlayers();
 	}
 	private static Guga_SERVER_MOD plugin;
 }

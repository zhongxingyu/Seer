 package net.slipcor.pvparena.modules.flyspectate;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.classes.PALocation;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.events.PAJoinEvent;
 import net.slipcor.pvparena.loadables.ArenaModule;
 
 public class FlySpectate extends ArenaModule {
 	public FlySpectate() {
 		super("FlySpectate");
 	}
 	RealSpectateListener listener = null;
 	
 	int priority = 3;
 	
 	@Override
 	public String version() {
		return "v1.1.0.334";
 	}
 
 	@Override
 	public PACheck checkJoin(CommandSender sender,
 			PACheck res, boolean join) {
 		if (join && (arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE) || !arena.isFightInProgress()))
 			return res;
 		
 		if (arena.getFighters().size() < 1) {
 			res.setError(this, Language.parse(MSG.ERROR_NOPLAYERFOUND));
 		}
 		
 		if (res.getPriority() < priority || (join && res.hasError())) {
 			res.setPriority(this, priority);
 		}
 		return res;
 	}
 	
 	RealSpectateListener getListener() {
 		if (listener == null) {
 			listener = new RealSpectateListener(this);
 			Bukkit.getPluginManager().registerEvents(listener, PVPArena.instance);
 		}
 		return listener;
 	}
 	
 	@Override
 	public void commitJoin(final Player player, final ArenaTeam team) {
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 				commitSpectate(player);
 			}
 			
 		}
 		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 3L);
 	}
 
 	@Override
 	public void commitSpectate(final Player player) {
 		debug.i("committing FLY spectate", player);
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		ap.setLocation(new PALocation(ap.get().getLocation()));
 		
 		ap.debugPrint();
 		
 		ap.setArena(arena);
 		ap.setStatus(Status.WATCH);
 		debug.i("switching:", player);
 		getListener().hidePlayerLater(player);
 		
 		if (ap.getState() == null) {
 			
 			final Arena arena = ap.getArena();
 
			final PAJoinEvent event = new PAJoinEvent(arena, player, false);
			Bukkit.getPluginManager().callEvent(event);

 			ap.createState(player);
 			ArenaPlayer.backupAndClearInventory(arena, player);
 			ap.dump();
 			
 			
 			if (ap.getArenaTeam() != null && ap.getArenaClass() == null) {
 				final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
 				if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
 					arena.chooseClass(player, null, autoClass);
 				}
 				if (autoClass == null) {
 					arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
 					return;
 				}
 			}
 		}
 
 		
 		long delay = arena.getArenaConfig().getBoolean(CFG.PERMS_FLY)?6L:5L;
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 				arena.tpPlayerToCoordName(player, "spectator");
 				player.setGameMode(GameMode.CREATIVE);
 				player.setAllowFlight(true);
 				player.setFlying(true);
 				arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
 			}
 		}
 		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new RunLater(), delay);
 	}
 	
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 		getListener().hideAllSpectatorsLater();
 	}
 	
 	@Override
 	public void reset(boolean force) {
 		if (listener != null)
 			listener.stop();
 	}
 	
 	@Override
 	public void unload(Player player) {
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			p.showPlayer(player);
 		}
 		
 		player.setAllowFlight(false);
 		player.setFlying(false);
 	}
 }

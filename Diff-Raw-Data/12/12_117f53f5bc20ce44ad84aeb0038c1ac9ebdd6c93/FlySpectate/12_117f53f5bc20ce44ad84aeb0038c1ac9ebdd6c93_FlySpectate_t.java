 package net.slipcor.pvparena.modules.flyspectate;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.classes.PALocation;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;
 
 public class FlySpectate extends ArenaModule {
 	public FlySpectate() {
 		super("FlySpectate");
 	}
 	RealSpectateListener listener = null;
 	
 	int priority = 3;
 	
 	@Override
 	public String version() {
		return "v1.0.0.0";
 	}
 
 	@Override
 	public PACheck checkJoin(CommandSender sender,
 			PACheck res, boolean join) {
 		if (join)
 			return res;
 		
 		if (arena.getFighters().size() < 1) {
 			res.setError(this, Language.parse(MSG.ERROR_NOPLAYERFOUND));
 		}
 		
 		if (res.getPriority() < priority) {
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
 	public void commitSpectate(final Player player) {
 		debug.i("committing REAL spectate", player);
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		ap.setLocation(new PALocation(ap.get().getLocation()));
 		Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance, new PlayerStateCreateRunnable(ap, ap.get()), 2L);
 		
 		ap.setArena(arena);
 		ap.setStatus(Status.WATCH);
 		debug.i("switching:", player);
 		getListener().hidePlayerLater(player);
 		class RunLater implements Runnable {
 
 			@Override
 			public void run() {
 				arena.tpPlayerToCoordName(player, "spectator");
 				player.setGameMode(GameMode.CREATIVE);
 				
 			}
 		}
 		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new RunLater(), 5L);
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
 		
 		getListener().removeSpectator(player);
 	}
 }

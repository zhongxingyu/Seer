 package net.slipcor.pvparena.modules.realspectate;
 
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
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
 
 public class RealSpectate extends ArenaModule {
 	public RealSpectate() {
 		super("RealSpectate");
 	}
 	RealSpectateListener listener = null;
 	
 	int priority = 2;
 	
 	@Override
 	public String version() {
		return "v0.10.3.0";
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
 
 	@Override
 	public void commitSpectate(Player player) {
 		debug.i("committing REAL spectate", player);
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		ap.setLocation(new PALocation(ap.get().getLocation()));
 		Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance, new PlayerStateCreateRunnable(ap, ap.get()), 2L);
 		//ArenaPlayer.prepareInventory(arena, ap.get());
 		ap.setArena(arena);
 		ap.setStatus(Status.WATCH);
 		debug.i("switching:", player);
 		getListener().switchPlayer(player, null, true);
 	}
 	
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 		for (SpectateWrapper sw : getListener().spectated_players.values()) {
 			sw.update();
 		}
 	}
 	
 	
 	@Override
 	public void parseStart() {
 		getListener();
 	}
 	
 	@Override
 	public void reset(boolean force) {
 		getListener();
 		HashSet<SpectateWrapper> list = new HashSet<SpectateWrapper>();
 		HashSet<Player> pList = new HashSet<Player>();
 		for (Player p : getListener().spectated_players.keySet()) {
 			pList.add(p);
 		}
 		for (SpectateWrapper sw : getListener().spectated_players.values()) {
 			list.add(sw);
 		}
 		
 		for (SpectateWrapper sw : list)  {
 			sw.stopHard();
 		}
 		getListener().spectated_players.clear();
 		
 	}
 	
 	@Override
 	public void unload(Player player) {
 		HashSet<SpectateWrapper> list = new HashSet<SpectateWrapper>();
 		for (SpectateWrapper sw : getListener().spectated_players.values()) {
 			list.add(sw);
 		}
 		
 
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			p.showPlayer(player);
 		}
 		
 		for (SpectateWrapper sw : list)  {
 			if (sw.hasSpectator(player)) {
 				sw.removeSpectator(player);
 				return;
 			}
 		}
 		
 		if (arena.getFighters().size() < 1) {
 			HashSet<SpectateWrapper> list2 = new HashSet<SpectateWrapper>();
 			for (SpectateWrapper sw : getListener().spectated_players.values()) {
 				list2.add(sw);
 			}
 			
 			for (SpectateWrapper sw : list2)  {
 				sw.stopSpectating();
 			}
 			getListener().spectated_players.clear();
 		}
 	}
 	
 	RealSpectateListener getListener() {
 		if (listener == null) {
 			listener = new RealSpectateListener(this);
 			Bukkit.getPluginManager().registerEvents(listener, PVPArena.instance);
 		}
 		return listener;
 	}
 }

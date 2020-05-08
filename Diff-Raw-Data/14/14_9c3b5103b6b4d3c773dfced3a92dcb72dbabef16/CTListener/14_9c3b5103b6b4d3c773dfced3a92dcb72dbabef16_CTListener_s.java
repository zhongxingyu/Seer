 package net.slipcor.pvparena.modules.colorteams;
 
 import java.util.HashSet;
 
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.core.Config.CFG;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
 
 public class CTListener implements Listener {
 	HashSet<String> removals = new HashSet<String>();
 
 	@EventHandler
 	public void onNameReceive(PlayerReceiveNameTagEvent event) {
 		Player p = event.getNamedPlayer();
 		
 		if (p == null) {
 			return;
 		}
 		
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName() );
 		
		if (ap == null || ap.getArena() == null || !ap.getArena().getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
 			if (removals.contains(ap.getName())) {
 				event.setTag(ap.getName());
 			}
 			return;
 		}
 		if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)) {
 			event.setTag(" ");
 			return;
 		}
 		
 		for (ArenaTeam at : ap.getArena().getTeams()) {
 			if (at.getTeamMembers().contains(ap)) {
 				event.setTag(at.colorizePlayer(p));
 				removals.add(ap.getName());
 				return;
 			}
 		}
 	}
 }

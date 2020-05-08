 package paalbra.BedTime;
 
 import org.bukkit.entity.Player;
 
 public class ConfirmPlayerHasEnteredBed implements Runnable {
 
 	BedTime plugin;
 	Player player;
 	int numRepeat;
 
 	public ConfirmPlayerHasEnteredBed(BedTime plugin, Player player) {
 		this.plugin = plugin;
 		this.player = player;
 		this.numRepeat = 10;
 	}
 
 	public ConfirmPlayerHasEnteredBed(BedTime plugin, Player player, int numRepeat) {
 		this.plugin = plugin;
 		this.player = player;
 		this.numRepeat = numRepeat;
 	}
 
 	@Override
 	public void run() {
 		if (player.isSleeping()) {
 			plugin.getServer().getPluginManager().callEvent(new PlayerHasEnteredBedEvent(player));
 		} else if (numRepeat > 0) {
 			plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new ConfirmPlayerHasQuit(plugin, player, numRepeat - 1), 5L);
 		} else {
			plugin.log.warning(plugin.prefix + "Player(" + player.getName() + ") has not become offline even though he should have");
 		}
 	}
 }

 package graindcafe.tribu;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class WaveStarter implements Runnable {
 	private Tribu plugin;
 	private boolean scheduled;
 	private int taskID;
 	private int waveNumber;
 	public WaveStarter(Tribu instance) {
 		plugin = instance;
 		waveNumber = 1;
 		scheduled = false;
 	}
 	
 	private int calcPolynomialFunction(int x, List<Double> coef) {
 		if (coef == null || coef.size() == 0)
 			return 0;
 		byte i = (byte) (coef.size() - 1);
 		byte j;
 		int r = 0;
 		int tmpX;
 
 		for (double c : coef) {
 			j = 0;
 			tmpX = 1;
 			while (j < i) {
 				tmpX *= x;
 				j++;
 			}
 			r += Math.round(c * tmpX);
 			i--;
 		}
 		return r;
 	}
 
 	public void cancelWave() {
 		if (scheduled) {
 			plugin.getServer().getScheduler().cancelTask(taskID);
 			scheduled = false;
 		}
 	}
 
 	public int getWaveNumber() {
 		return waveNumber;
 	}
 
 	public void incrementWave() {
 		waveNumber++;
 	}
 
 	public void resetWave() {
 		waveNumber = 1;
 	}
 
 	@Override
 	public void run() {
 		if (plugin.isRunning()) {
 			if (plugin.config().WaveStartTeleportPlayers) {
 				for (Player p : plugin.getPlayers()) {
 					p.teleport(plugin.getLevel().getInitialSpawn());
 				}
 			}
 			if (plugin.config().WaveStartSetTime)
 				plugin.getLevel().getInitialSpawn().getWorld().setTime(plugin.config().WaveStartSetTimeTo);
 			int max = calcPolynomialFunction(waveNumber, plugin.config().ZombiesQuantity);
 			int health = calcPolynomialFunction(waveNumber, plugin.config().ZombiesHealth);
 			int timeToSpawn = calcPolynomialFunction(waveNumber, plugin.config().ZombiesTimeToSpawn)/max; 
 			scheduled = false;
 			plugin.revivePlayers(false);
 			plugin.getLevel().onWaveStart();
 			for(Player pp:plugin.deadPeople.keySet())
 			{
 				plugin.revivePlayer(pp);
 				plugin.deadPeople.remove(pp);
 				pp.teleport(plugin.getLevel().getInitialSpawn());
				Tribu.messagePlayer(pp,"You have been revived.",ChatColor.GREEN);
 			}
 			
 			plugin.getSpawnTimer().StartWave(max, health,timeToSpawn);
 			plugin.messagePlayers(
 							String.format(plugin.getLocale("Broadcast.StartingWave"), String.valueOf(waveNumber), String.valueOf(max),
 									String.valueOf(health)));
 			plugin.getSpawner().startingCallback();
 		}
 	}
 
 	public void scheduleWave(int delay) {
 		if (!scheduled && plugin.isRunning()) {
 			taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
 			scheduled = true;
 			plugin.messagePlayers(
 					String.format(plugin.getLocale("Broadcast.Wave"), String.valueOf(plugin.getWaveStarter().getWaveNumber()),
 							String.valueOf(delay / 20)));
 		}
 	}
 
 }

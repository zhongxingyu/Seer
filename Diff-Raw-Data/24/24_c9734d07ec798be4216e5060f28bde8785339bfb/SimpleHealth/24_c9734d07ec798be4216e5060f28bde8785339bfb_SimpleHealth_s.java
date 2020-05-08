 package minny.simplehealth;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
 
 public class SimpleHealth extends JavaPlugin {
 
 	private final SimpleHealthPlayerListener playerListener = new SimpleHealthPlayerListener(
 			this);
	ScoreboardManager manager = Bukkit.getScoreboardManager();
	Scoreboard board = manager.getNewScoreboard();
 
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() {

 		board.registerNewObjective("showhealth", "health");
 
 		Objective objective = board.getObjective("showhealth");
 		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
 		objective.setDisplayName("/ 20");
 
 		PluginManager pm = getServer().getPluginManager();
 
 		pm.registerEvents(playerListener, this);
 
 	}
 
 }

 package me.kevin.uhc;
 
 import me.kevin.uhc.commands.ScatterCommand;
 import me.kevin.uhc.commands.TeamCommand;
 import me.kevin.uhc.listeners.EntityListener;
 import me.kevin.uhc.listeners.PlayerListener;
 import me.kevin.uhc.tasks.BorderCheckerTask;
 import me.kevin.uhc.tasks.HealthUpdaterTask;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 public class UltraHardcore extends JavaPlugin {
     public boolean ingame = false;
     public int radius = 500;
 
     public PlayerListener listener = new PlayerListener(this);
 
     public Scoreboard scoreboard;
     public Team spectator;
     public Objective score;
     public Objective health;
     public Objective headHealth;
 
     public BorderCheckerTask borderCheckerTask = new BorderCheckerTask(this);
     public HealthUpdaterTask healthUpdaterTask = new HealthUpdaterTask(this);
 
     public PlayerListener playerListener = new PlayerListener(this);
     public EntityListener entityListener = new EntityListener(this);
 
     @Override
     public void onEnable() {
         scoreboard = getServer().getScoreboardManager().getNewScoreboard();
         score = scoreboard.registerNewObjective("Score", "dummy");
         health = scoreboard.registerNewObjective("Health", "dummy");
         headHealth = scoreboard.registerNewObjective("Headhealth", "dummy");
 
         score.setDisplaySlot(DisplaySlot.SIDEBAR);
         health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
         headHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
         headHealth.setDisplayName("/ 20");
 
         for (World world : getServer().getWorlds()) {
             world.setGameRuleValue("naturalRegeneration", "false");
         }
         getCommand("team").setExecutor(new TeamCommand(this));
         getCommand("scatter").setExecutor(new ScatterCommand(this));
 
         getServer().getPluginManager().registerEvents(playerListener, this);
         getServer().getPluginManager().registerEvents(entityListener, this);
 
         getServer().getScheduler().runTaskTimer(this, borderCheckerTask, 10, 10);
         getServer().getScheduler().runTaskTimer(this, healthUpdaterTask, 60, 60);
     }
 
     public Team getTeamByPlayer(Player player) {
         for (Team team : scoreboard.getTeams()) {
             if (team.getPlayers().contains(player)) {
                 return team;
             }
         }
         return null;
     }
 
     public void setSpectator(Player player) {
         player.setGameMode(GameMode.ADVENTURE);
         player.setAllowFlight(true);
         player.setFlying(true);
         for (Player pl : getServer().getOnlinePlayers()) {
             if (pl != player) {
                 pl.hidePlayer(player);
             }
         }
     }
 
     public void updateHealth(Player player) {
        String displayName = getChatColor((int)player.getHealth()) + player.getName();
        displayName = displayName.substring(0, Math.min(16, displayName.length()));
        player.setPlayerListName(displayName);
        health.getScore(getServer().getOfflinePlayer(displayName)).setScore((int)player.getHealth());
         headHealth.getScore(player).setScore((int)player.getHealth());
     }
 
     public ChatColor getChatColor(int health) {
         if (health <= 4) {
             return ChatColor.RED;
         } else if (health <= 10) {
             return ChatColor.YELLOW;
         } else if (health <= 16) {
             return ChatColor.BLUE;
         } else {
             return ChatColor.GREEN;
         }
     }
 
 }

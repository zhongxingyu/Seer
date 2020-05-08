 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cyberiantiger.minecraft.scoreshare;
 
 import org.cyberiantiger.minecraft.scoreshare.api.TeamProviderListener;
 import org.cyberiantiger.minecraft.scoreshare.api.TeamProviderFactory;
 import org.cyberiantiger.minecraft.scoreshare.api.TeamProvider;
 import org.cyberiantiger.minecraft.scoreshare.api.Team;
 import org.cyberiantiger.minecraft.scoreshare.api.ObjectiveProviderListener;
 import org.cyberiantiger.minecraft.scoreshare.api.ObjectiveProviderFactory;
 import org.cyberiantiger.minecraft.scoreshare.api.ObjectiveProvider;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicePriority;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.cyberiantiger.minecraft.scoreshare.api.ScoreShareAPI;
 import org.cyberiantiger.minecraft.scoreshare.unsafe.CBShim;
 import org.cyberiantiger.minecraft.scoreshare.unsafe.ScoreModifier;
 
 /**
  * The plugin class.
  * <p>
  * <b>Do not link against this class.</b>
  * <p>
  * To access the API use:
  * <pre><code>getServer().getServicesManager().getRegistrations(TeamProviderFactory.class)</code></pre>
  * <p>
  * To add your own teams to the scoreboard use {@link TeamProviderFactory}.
  * <p>
 * To add your own objectives to the scoreboard use
 * {@link ObjectiveProviderFactory}.
  *
  * @see TeamProviderFactory
  * @see ObjectiveProviderFactory
  */
 public class ScoreShare extends JavaPlugin implements Listener {
 
     private final boolean bukkitSucks = true;
     private Map<Player, PlayerScoreboard> scoreboards = new WeakHashMap<Player, PlayerScoreboard>();
     private List<ObjectiveProviderFactory<Plugin>> objectiveProviderFactories;
     private List<TeamProviderFactory<Plugin>> teamProviderFactories;
     private final ScoreShareAPI api = new ScoreShareAPI() {
         @Override
         public void reset() {
             ScoreShare.this.reset();
         }
 
         @Override
         public void reset(Player player) {
             ScoreShare.this.reset(player);
         }
 
         @Override
         public void setTeamProvider(Plugin source, String name) {
             for (Player p : getServer().getOnlinePlayers()) {
                 setTeamProvider(p, source, name);
             }
         }
 
         @Override
         public void setTeamProvider(Player player, Plugin source, String name) {
             PlayerScoreboard scoreboard = scoreboards.get(player);
             scoreboard.setTeamProvider(scoreboard.getTeamProvider(source, name));
         }
 
         @Override
         public void setObjectiveProvider(DisplaySlot slot, Plugin source, String name) {
             for (Player p : getServer().getOnlinePlayers()) {
                 setObjectiveProvider(p, slot, source, name);
             }
         }
 
         @Override
         public void setObjectiveProvider(Player player, DisplaySlot slot, Plugin source, String name) {
             PlayerScoreboard scoreboard = scoreboards.get(player);
             scoreboard.setDisplayObjectiveProvider(slot, scoreboard.getObjectiveProvider(source, name));
         }
     };
 
     private void reset() {
         for (Player p : getServer().getOnlinePlayers()) {
             reset(p);
         }
     }
 
     private void reset(Player player) {
         PlayerScoreboard scoreboard = new PlayerScoreboard(player);
         scoreboards.put(player, scoreboard);
         if (getConfig().isString("defaults.teams")) {
             TeamProvider<Plugin> t = scoreboard.getTeamProvider(getConfig().getString("defaults.teams"));
             scoreboard.setTeamProvider(t);
         }
         for (DisplaySlot slot : DisplaySlot.values()) {
             if (getConfig().isString("defaults." + slot.toString().toLowerCase())) {
                 ObjectiveProvider<Plugin> o = scoreboard.getObjectiveProvider(getConfig().getString("defaults." + slot.toString().toLowerCase()));
                 scoreboard.setDisplayObjectiveProvider(slot, o);
             }
         }
     }
 
     @Override
     public void onEnable() {
         super.onEnable();
         saveDefaultConfig();
         // Register our own providers.
         ServicesManager manager = getServer().getServicesManager();
         manager.register(ScoreShareAPI.class, api, this, ServicePriority.Normal);
         manager.register(ObjectiveProviderFactory.class, new HealthProvider(this), this, ServicePriority.Normal);
         manager.register(ObjectiveProviderFactory.class, new ServerStatsProvider(this), this, ServicePriority.Normal);
         manager.register(TeamProviderFactory.class, new WorldProvider(this), this, ServicePriority.Normal);
 
         // This should run after all other plugins are loaded even for a /reload
         getServer().getScheduler().runTask(this, new Runnable() {
             @Override
             public void run() {
                 reset();
                 getServer().getPluginManager().registerEvents(ScoreShare.this, ScoreShare.this);
             }
         });
     }
 
     @Override
     public void onDisable() {
         super.onDisable();
         for (Map.Entry<Player, PlayerScoreboard> e : scoreboards.entrySet()) {
             e.getValue().dispose(e.getKey());
         }
         scoreboards.clear();
         objectiveProviderFactories = null;
         teamProviderFactories = null;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         // Hack, we only have one command assume it's the right one.
         if (args.length == 0) {
             // Usage
             return false;
         }
         if (!(sender instanceof Player)) {
             sender.sendMessage("Only players have Scoreboards");
             return true;
         }
         PlayerScoreboard scoreboard = scoreboards.get(sender);
         if ("info".equals(args[0])) {
             TeamProvider<Plugin> teamProvider = scoreboard.getTeamProvider();
             if (teamProvider != null) {
                 sender.sendMessage("Current teams provider: " + teamProvider.getPlugin().getName() + '.' + teamProvider.getName());
             }
             ObjectiveProvider<Plugin> objectiveProvider = scoreboard.getDisplayObjectiveProvider(DisplaySlot.BELOW_NAME);
             if (objectiveProvider != null) {
                 sender.sendMessage("Nametag objective set to: " + objectiveProvider.getPlugin().getName() + '.' + objectiveProvider.getName());
             }
             objectiveProvider = scoreboard.getDisplayObjectiveProvider(DisplaySlot.SIDEBAR);
             if (objectiveProvider != null) {
                 sender.sendMessage("Sidebar objective set to: " + objectiveProvider.getPlugin().getName() + '.' + objectiveProvider.getName());
             }
             objectiveProvider = scoreboard.getDisplayObjectiveProvider(DisplaySlot.PLAYER_LIST);
             if (objectiveProvider != null) {
                 sender.sendMessage("List objective set to: " + objectiveProvider.getPlugin().getName() + '.' + objectiveProvider.getName());
             }
             Collection<TeamProvider<Plugin>> teamProviders = scoreboard.getTeamProviders();
             if (teamProviders.isEmpty()) {
                 sender.sendMessage("No available team providers.");
             } else {
                 sender.sendMessage(teamProviders.size() + " team provider(s) available:");
                 for (TeamProvider<Plugin> p : teamProviders) {
                     sender.sendMessage("    " + p.getPlugin().getName() + '.' + p.getName());
                 }
             }
             Collection<ObjectiveProvider<Plugin>> objectiveProviders = scoreboard.getObjectiveProviders();
             if (objectiveProviders.isEmpty()) {
                 sender.sendMessage("No available objective providers.");
             } else {
                 sender.sendMessage(objectiveProviders.size() + " objective provider(s) available:");
                 for (ObjectiveProvider<Plugin> p : objectiveProviders) {
                     sender.sendMessage("    " + p.getPlugin().getName() + '.' + p.getName());
                 }
             }
         } else if ("teams".equalsIgnoreCase(args[0])) {
             if (args.length != 2) {
                 return false;
             }
             TeamProvider<Plugin> teamProvider;
             if ("none".equalsIgnoreCase(args[1])) {
                 teamProvider = null;
             } else {
                 teamProvider = scoreboard.getTeamProvider(args[1]);
                 if (teamProvider == null) {
                     sender.sendMessage("No team provider found named: " + args[1]);
                     return true;
                 }
             }
             scoreboard.setTeamProvider(teamProvider);
         } else if ("belowname".equalsIgnoreCase(args[0])) {
             if (args.length != 2) {
                 return false;
             }
             ObjectiveProvider<Plugin> objectiveProvider;
             if ("none".equalsIgnoreCase(args[1])) {
                 objectiveProvider = null;
             } else {
                 objectiveProvider = scoreboard.getObjectiveProvider(args[1]);
                 if (objectiveProvider == null) {
                     sender.sendMessage("No team provider found named: " + args[1]);
                     return true;
                 }
             }
             scoreboard.setDisplayObjectiveProvider(DisplaySlot.BELOW_NAME, objectiveProvider);
         } else if ("sidebar".equalsIgnoreCase(args[0])) {
             if (args.length != 2) {
                 return false;
             }
             ObjectiveProvider<Plugin> objectiveProvider;
             if ("none".equalsIgnoreCase(args[1])) {
                 objectiveProvider = null;
             } else {
                 objectiveProvider = scoreboard.getObjectiveProvider(args[1]);
                 if (objectiveProvider == null) {
                     sender.sendMessage("No team provider found named: " + args[1]);
                     return true;
                 }
             }
             scoreboard.setDisplayObjectiveProvider(DisplaySlot.SIDEBAR, objectiveProvider);
         } else if ("list".equalsIgnoreCase(args[0])) {
             if (args.length != 2) {
                 return false;
             }
             ObjectiveProvider<Plugin> objectiveProvider;
             if ("none".equalsIgnoreCase(args[1])) {
                 objectiveProvider = null;
             } else {
                 objectiveProvider = scoreboard.getObjectiveProvider(args[1]);
                 if (objectiveProvider == null) {
                     sender.sendMessage("No team provider found named: " + args[1]);
                     return true;
                 }
             }
             scoreboard.setDisplayObjectiveProvider(DisplaySlot.PLAYER_LIST, objectiveProvider);
         } else if ("savedefault".equalsIgnoreCase(args[0])) {
             if (args.length != 1) {
                 return false;
             }
             if (!sender.hasPermission("scoreshare.savedefault")) {
                 sender.sendMessage("You do not have permission to save the default scoreboard.");
                 return true;
             }
             TeamProvider<Plugin> t = scoreboard.getTeamProvider();
             if (t == null) {
                 getConfig().set("defaults.teams", null);
             } else {
                 getConfig().set("defaults.teams", t.getPlugin().getName() + '.' + t.getName());
             }
             for (DisplaySlot slot : DisplaySlot.values()) {
                 ObjectiveProvider<Plugin> o = scoreboard.getDisplayObjectiveProvider(slot);
                 if (o == null) {
                     getConfig().set("defaults." + slot.name().toLowerCase(), null);
                 } else {
                     getConfig().set("defaults." + slot.name().toLowerCase(), o.getPlugin().getName() + '.' + o.getName());
                 }
             }
             saveConfig();
         } else if ("reset".equalsIgnoreCase(args[0])) {
             if (args.length != 1) {
                 return false;
             }
            reset((Player) sender);
         } else if ("resetall".equalsIgnoreCase(args[0])) {
             if (args.length != 1) {
                 return false;
             }
             if (!sender.hasPermission("scoreshare.resetall")) {
                 sender.sendMessage("You do not have permission to reset everybody's scoreboard");
                 return true;
             }
             reset();
         } else {
             return false;
         }
         return true;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerJoin(PlayerJoinEvent e) {
         reset(e.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent e) {
         PlayerScoreboard scoreboard = scoreboards.remove(e.getPlayer());
         scoreboard.dispose(e.getPlayer());
     }
 
     private List<ObjectiveProviderFactory<Plugin>> getObjectiveProviderFactories() {
         if (objectiveProviderFactories == null) {
             objectiveProviderFactories = new ArrayList<ObjectiveProviderFactory<Plugin>>();
             for (RegisteredServiceProvider<ObjectiveProviderFactory> rsp
                     : getServer().getServicesManager().getRegistrations(ObjectiveProviderFactory.class)) {
                 objectiveProviderFactories.add(rsp.getProvider());
             }
         }
         return objectiveProviderFactories;
     }
 
     private List<TeamProviderFactory<Plugin>> getTeamProviderFactories() {
         if (teamProviderFactories == null) {
             teamProviderFactories = new ArrayList<TeamProviderFactory<Plugin>>();
             for (RegisteredServiceProvider<TeamProviderFactory> rsp
                     : getServer().getServicesManager().getRegistrations(TeamProviderFactory.class)) {
                 teamProviderFactories.add(rsp.getProvider());
             }
         }
         return teamProviderFactories;
     }
 
     private class PlayerScoreboard {
 
         private final Scoreboard scoreboard;
         private final List<ObjectiveProvider<Plugin>> objectiveProviders;
         private final List<TeamProvider<Plugin>> teamProviders;
         private final Map<DisplaySlot, ObjectiveProvider> displayObjectiveProviders =
                 new EnumMap<DisplaySlot, ObjectiveProvider>(DisplaySlot.class);
         private Map<DisplaySlot, ObjectiveProviderListener> displayObjectiveProviderListeners =
                 new EnumMap<DisplaySlot, ObjectiveProviderListener>(DisplaySlot.class);
 
         {
             for (DisplaySlot displaySlot : DisplaySlot.values()) {
                 displayObjectiveProviderListeners.put(displaySlot, new ObjectiveProviderListenerImpl(displaySlot));
             }
         }
         private TeamProvider teamProvider = null;
         private TeamProviderListener teamProviderListener = new TeamProviderListenerImpl();
 
         public PlayerScoreboard(Player player) {
             this.scoreboard = getServer().getScoreboardManager().getNewScoreboard();
             objectiveProviders = new ArrayList<ObjectiveProvider<Plugin>>();
             for (ObjectiveProviderFactory objectiveProviderFactory : getObjectiveProviderFactories()) {
                 ObjectiveProvider objectiveProvider = objectiveProviderFactory.getProvider(player);
                 if (objectiveProvider != null) {
                     objectiveProviders.add(objectiveProvider);
                 }
             }
             Collections.sort(objectiveProviders, new Comparator<ObjectiveProvider>() {
                 @Override
                 public int compare(ObjectiveProvider o1, ObjectiveProvider o2) {
                     int result = o1.getPlugin().getName().toLowerCase().compareTo(o2.getPlugin().getName().toLowerCase());
                     if (result != 0) {
                         return result;
                     }
                     return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                 }
             });
             teamProviders = new ArrayList<TeamProvider<Plugin>>();
             for (TeamProviderFactory teamProviderFactory : getTeamProviderFactories()) {
                 TeamProvider teamProvider = teamProviderFactory.getTeamProvider(player);
                 if (teamProvider != null) {
                     teamProviders.add(teamProvider);
                 }
             }
             Collections.sort(teamProviders, new Comparator<TeamProvider>() {
                 @Override
                 public int compare(TeamProvider o1, TeamProvider o2) {
                     int result = o1.getPlugin().getName().toLowerCase().compareTo(o2.getPlugin().getName().toLowerCase());
                     if (result != 0) {
                         return result;
                     }
                     return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                 }
             });
             player.setScoreboard(scoreboard);
         }
 
         public Collection<TeamProvider<Plugin>> getTeamProviders() {
             return teamProviders;
         }
 
         public Collection<ObjectiveProvider<Plugin>> getObjectiveProviders() {
             return objectiveProviders;
         }
 
         public TeamProvider<Plugin> getTeamProvider(Plugin plugin, String name) {
             for (TeamProvider<Plugin> t : teamProviders) {
                 if (plugin == t.getPlugin() && name.equals(t.getName())) {
                     return t;
                 }
             }
             return null;
         }
 
         public TeamProvider<Plugin> getTeamProvider(String provider) {
             for (TeamProvider<Plugin> t : teamProviders) {
                 if (provider.equalsIgnoreCase(t.getPlugin().getName() + '.' + t.getName())) {
                     return t;
                 }
             }
             return null;
         }
 
         public ObjectiveProvider<Plugin> getObjectiveProvider(Plugin plugin, String name) {
             for (ObjectiveProvider<Plugin> t : objectiveProviders) {
                 if (plugin == t.getPlugin() && name.equals(t.getName())) {
                     return t;
                 }
             }
             return null;
         }
 
         public ObjectiveProvider<Plugin> getObjectiveProvider(String provider) {
             for (ObjectiveProvider<Plugin> t : objectiveProviders) {
                 if (provider.equalsIgnoreCase(t.getPlugin().getName() + '.' + t.getName())) {
                     return t;
                 }
             }
             return null;
         }
 
         public TeamProvider getTeamProvider() {
             return teamProvider;
         }
 
         public void setTeamProvider(TeamProvider<Plugin> teamProvider) {
             if (this.teamProvider == teamProvider) {
                 return;
             }
             if (this.teamProvider != null) {
                 this.teamProvider.removeListener(teamProviderListener);
                 this.teamProvider = null;
                 for (org.bukkit.scoreboard.Team team : scoreboard.getTeams()) {
                     team.unregister();
                 }
             }
             if (teamProvider != null) {
                 for (Team team : teamProvider.getTeams()) {
                     org.bukkit.scoreboard.Team bukkitTeam = scoreboard.registerNewTeam(team.getName());
                     copy(team, bukkitTeam);
                 }
                 teamProvider.addListener(teamProviderListener);
                 this.teamProvider = teamProvider;
             }
         }
 
         public ObjectiveProvider getDisplayObjectiveProvider(DisplaySlot slot) {
             return displayObjectiveProviders.get(slot);
         }
 
         public void setDisplayObjectiveProvider(DisplaySlot slot, ObjectiveProvider objectiveProvider) {
             ObjectiveProvider installedObjectiveProvider = displayObjectiveProviders.get(slot);
 
             if (installedObjectiveProvider == objectiveProvider) {
                 return;
             }
 
             if (installedObjectiveProvider != null) {
                 installedObjectiveProvider.removeListener(displayObjectiveProviderListeners.get(slot));
                 scoreboard.getObjective(slot).unregister();
                 displayObjectiveProviders.put(slot, null);
             }
 
             if (objectiveProvider != null) {
                Objective objective = scoreboard.registerNewObjective(objectiveProvider.getName()+'_'+slot.name(), objectiveProvider.getCriteria());
                 displayObjectiveProviders.put(slot, objectiveProvider);
                 objectiveProvider.addListener(displayObjectiveProviderListeners.get(slot));
                 objective.setDisplaySlot(slot);
                 objective.setDisplayName(objectiveProvider.getDisplayName());
                 Map<OfflinePlayer, Integer> initialScores = objectiveProvider.getScores();
                 for (Map.Entry<OfflinePlayer, Integer> e : initialScores.entrySet()) {
                     objective.getScore(e.getKey()).setScore(e.getValue());
                 }
             }
         }
 
         public void dispose(Player player) {
             setTeamProvider(null);
             for (DisplaySlot displaySlot : DisplaySlot.values()) {
                 setDisplayObjectiveProvider(displaySlot, null);
             }
             // Attempt to restore the player's original scoreboard.
             if (player != null) {
                 player.setScoreboard(getServer().getScoreboardManager().getMainScoreboard());
             }
         }
 
         private class ObjectiveProviderListenerImpl implements ObjectiveProviderListener {
 
             private final DisplaySlot displaySlot;
 
             public ObjectiveProviderListenerImpl(DisplaySlot displaySlot) {
                 this.displaySlot = displaySlot;
             }
 
             @Override
             public void putScore(OfflinePlayer player, int score) {
                 Objective objective = scoreboard.getObjective(displaySlot);
                 if (objective != null) {
                     objective.getScore(player).setScore(score);
                 }
             }
 
             @Override
             public void removeScore(OfflinePlayer player) {
                 if (bukkitSucks) {
                     CBShim.createShim(ScoreModifier.class, ScoreShare.this).reset(scoreboard, displaySlot, player);
                 } else {
                     // scoreboard.getObjective(displaySlot).getScore(player).reset();
                 }
             }
         }
 
         private class TeamProviderListenerImpl implements TeamProviderListener {
 
             @Override
             public void addTeam(String teamName) {
                 scoreboard.registerNewTeam(teamName);
             }
 
             @Override
             public void removeTeam(String teamName) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.unregister();
                 }
             }
 
             @Override
             public void modifyTeamDisplayName(String teamName, String displayName) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.setDisplayName(displayName);
                 }
             }
 
             @Override
             public void modifyTeamPrefix(String teamName, String prefix) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.setPrefix(prefix);
                 }
             }
 
             @Override
             public void modifyTeamSuffix(String teamName, String suffix) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.setSuffix(suffix);
                 }
             }
 
             @Override
             public void modifyTeamFriendlyFire(String teamName, boolean friendlyFire) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.setAllowFriendlyFire(friendlyFire);
                 }
             }
 
             @Override
             public void modifyTeamSeeInvisibleFriendlies(String teamName, boolean seeInvisibleFriendlies) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.setCanSeeFriendlyInvisibles(seeInvisibleFriendlies);
                 }
             }
 
             @Override
             public void addTeamMember(String teamName, OfflinePlayer member) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.addPlayer(member);
                 }
             }
 
             @Override
             public void removeTeamMember(String teamName, OfflinePlayer member) {
                 org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
                 if (team != null) {
                     team.removePlayer(member);
                 }
             }
         }
     }
 
     private static void copy(org.cyberiantiger.minecraft.scoreshare.api.Team myTeam, org.bukkit.scoreboard.Team team) {
         team.setDisplayName(myTeam.getDisplayName());
         team.setPrefix(myTeam.getPrefix());
         team.setSuffix(myTeam.getSuffix());
         team.setAllowFriendlyFire(myTeam.isFriendlyFire());
         team.setCanSeeFriendlyInvisibles(myTeam.isSeeInvisibleFriendlies());
         for (OfflinePlayer p : myTeam.getMembers()) {
             team.addPlayer(p);
         }
     }
 }

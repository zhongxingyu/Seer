 package fr.aumgn.tobenamed.game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 
 import fr.aumgn.tobenamed.TBN;
 import fr.aumgn.tobenamed.TBNColor;
 import fr.aumgn.tobenamed.exception.NoSuchTeam;
 import fr.aumgn.tobenamed.exception.NotEnoughTeams;
 import fr.aumgn.tobenamed.exception.PlayerNotInGame;
 import fr.aumgn.tobenamed.region.GameSpawn;
 import fr.aumgn.tobenamed.stage.PauseStage;
 import fr.aumgn.tobenamed.stage.Stage;
 import fr.aumgn.tobenamed.util.TBNUtil;
 import fr.aumgn.tobenamed.util.Vector;
 
 public class Game {
 
     private Stage stage;
     private World world;
     private GameSpawn spawn;
     private Map<String, Team> teams;
     private Map<Player, Team> players;
     private Spectators spectators;
     private int turnCount;
 
     public Game(List<String> teamsName, World world, Vector spawnPoint) {
         this.stage = null;
         this.teams = new LinkedHashMap<String, Team>();
         Iterator<TBNColor> colors = TBNColor.
                 getRandomColors(teamsName.size()).iterator();
         for (String teamName : teamsName) {
             teams.put(teamName, new Team(teamName, colors.next()));
         }
         if (teams.keySet().size() < 2) {
             throw new NotEnoughTeams();
         }
         this.world = world;
         spawn = new GameSpawn(spawnPoint);
         players = new HashMap<Player, Team>();
         spectators = new Spectators(this);
        turnCount = -1;
     }
 
     public Stage getStage() {
         return stage;
     }
 
     private void unregisterStageListeners() {
         for (Listener listener : stage.getListeners()) {
             HandlerList.unregisterAll(listener);
         }
     }
 
     private void registerStageListeners() {
         for (Listener listener : stage.getListeners()) {
             Bukkit.getPluginManager().registerEvents(listener, TBN.getPlugin());
         }
     }
 
     public void nextStage(Stage newStage) {
         if (newStage == null) {
             throw new IllegalArgumentException("New stage cannot be null");
         }
         if (stage != null) {
             unregisterStageListeners();
             stage.stop();
         }
         stage = newStage;
         registerStageListeners();
         stage.start();
     }
 
     public boolean isPaused() {
         return (stage instanceof PauseStage);
     }
 
     public void pause() {
         if (stage == null) {
             throw new UnsupportedOperationException();
         }
         unregisterStageListeners();
         stage.pause();
         stage = new PauseStage(this, stage);
         stage.start();
         registerStageListeners();
     }
 
     public void resume() {
         unregisterStageListeners();
         stage.stop();
         stage = ((PauseStage) stage).getOldStage();
         registerStageListeners();
         stage.resume();
     }
 
     public World getWorld() {
         return world;
     }
 
     public GameSpawn getSpawn() {
         return spawn;
     }
 
     public Team getTeam(String name) {
         if (!teams.containsKey(name)) {
             throw new NoSuchTeam(name);
         }
         return teams.get(name);
     }
 
     public Team getTeam(Player player) {
         if (!players.containsKey(player)) {
             throw new PlayerNotInGame();
         }
         return players.get(player);
     }
 
     public Spectators getSpectators() {
         return spectators;
     }
 
     public int getTurnCount() {
         return turnCount;
     }
 
     public void incrementTurnCount() {
         ++turnCount;
     }
 
     public void sendMessage(String message) {
         for (Team team : getTeams()) {
             team.sendMessage(message);
         }
         for (Player spectator : spectators) {
             spectator.sendMessage(message);
         }
     }
 
     public boolean contains(Player player) {
         return players.containsKey(player);
     }
 
     public List<Team> getTeams() {
         return new ArrayList<Team>(teams.values());
     }
 
     private Team getTeamWithMinimumPlayers() {
         int minimum = Integer.MAX_VALUE;
         List<Team> roulette = null; 
         for (Team team : getTeams()) {
             int size = team.size();
             if (size < minimum) {
                 minimum = size;
                 roulette = new ArrayList<Team>();
                 roulette.add(team);
             } else if (size == minimum) {
                 roulette.add(team);
             } 
         }
 
         return TBNUtil.pickRandom(roulette);
     }
 
     public void addPlayer(Player player, Team team) {
         if (team == null) {
             team = getTeamWithMinimumPlayers();
         }
 
         team.addPlayer(player);
         players.put(player, team);
     }
 
     public void removePlayer(Player player) {
         Team team = getTeam(player);
         team.removePlayer(player);
         players.remove(player);
     }
 
     public void decreaseLives(Team team) {
         team.decreaseLives();
         if (team.getLives() == 0) {
             teams.remove(team.getName());
             if (teams.size() > 1) {
                 for (Player player : team.getPlayers()) {
                     players.remove(player);
                 }
                 String msg = ChatColor.RED +"L'équipe " + team.getDisplayName() 
                         + ChatColor.RED + " a perdu la partie.";
                 sendMessage(msg);
                 team.sendMessage(msg);
             } else {
                 Team winningTeam = teams.values().iterator().next();
                 String msg = ChatColor.GREEN + "L'équipe " + winningTeam.getDisplayName() +
                         ChatColor.GREEN + " a gagné la partie.";
                 sendMessage(msg);
                 team.sendMessage(msg);
                 TBN.forceStop();
             }
         } else {
             sendMessage(ChatColor.YELLOW + "L'équipe " + team.getDisplayName() + 
                     ChatColor.YELLOW + " a perdu une vie. " + team.getLives() + " restantes.");
         }
     }
 }

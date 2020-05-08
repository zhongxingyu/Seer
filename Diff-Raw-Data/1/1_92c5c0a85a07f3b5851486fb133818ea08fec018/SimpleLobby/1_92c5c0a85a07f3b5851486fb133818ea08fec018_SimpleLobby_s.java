 package me.limebyte.battlenight.core.battle;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.Lobby;
 import me.limebyte.battlenight.api.managers.ArenaManager;
 import me.limebyte.battlenight.api.managers.ScoreManager.ScoreboardState;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.battle.battles.FFABattle;
 import me.limebyte.battlenight.core.battle.battles.TDMBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.PlayerData;
 import me.limebyte.battlenight.core.tosort.Teleporter;
 import me.limebyte.battlenight.core.util.LobbyTimer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class SimpleLobby implements Lobby {
 
     private BattleNightAPI api;
     private Set<String> players;
     private Arena arena;
     private boolean starting = false;
     private LobbyTimer timer;
     private Random random;
 
     public SimpleLobby(BattleNightAPI api) {
         this.api = api;
         players = new HashSet<String>();
         timer = new LobbyTimer(api, this, 10L);
         this.random = new Random();
     }
 
     @Override
     public void addPlayer(Player player) {
         Messenger messenger = api.getMessenger();
         Battle battle = api.getBattle();
 
         if (players.contains(player.getName())) {
             messenger.tell(player, Message.ALREADY_IN_LOBBY);
             return;
         }
 
         if (battle != null) {
             if (battle.containsPlayer(player)) {
                 messenger.tell(player, Message.ALREADY_IN_BATTLE);
                 return;
             }
 
             if (players.size() >= battle.getMaxPlayers()) {
                 messenger.tell(player, Message.BATTLE_FULL);
                 return;
             }
         }
 
         ArenaManager arenas = api.getArenaManager();
         if (!arenas.getLounge().isSet()) {
             messenger.tell(player, Message.WAYPOINTS_UNSET);
             return;
         }
 
         players.add(player.getName());
 
         PlayerData.store(player);
         api.getMessenger().log(Level.INFO, "Saved");
         PlayerData.reset(player);
         api.getMessenger().log(Level.INFO, "Resest");
         Teleporter.tp(player, arenas.getLounge());
         api.getMessenger().log(Level.INFO, "Teleported");
         api.getScoreManager().addPlayer(player);
 
         if (starting) {
             api.setPlayerClass(player, api.getClassManager().getRandomClass());
         }
 
         messenger.tell(player, Message.JOINED_LOBBY);
         messenger.tellLobby(Message.PLAYER_JOINED_LOBBY, player);
 
     }
 
     @Override
     public void removePlayer(Player player) {
         Messenger messenger = api.getMessenger();
 
         if (!players.contains(player.getName())) {
             messenger.tell(player, Message.NOT_IN_LOBBY);
             return;
         }
 
         players.remove(player.getName());
 
         api.getScoreManager().removePlayer(player);
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         
         Metadata.remove(player, "ready");
         Metadata.remove(player, "kills");
         Metadata.remove(player, "deaths");
         Metadata.remove(player, "voted");
     }
 
     @Override
     public Set<String> getPlayers() {
         return players;
     }
 
     @Override
     public boolean contains(Player player) {
         return players.contains(player.getName());
     }
 
     protected void addPlayerFromBattle(Player player) {
         Messenger messenger = api.getMessenger();
 
         if (players.contains(player.getName())) {
             messenger.tell(player, Message.ALREADY_IN_LOBBY);
             return;
         }
 
         ArenaManager arenas = api.getArenaManager();
         if (!arenas.getLounge().isSet()) {
             messenger.tell(player, Message.WAYPOINTS_UNSET);
             return;
         }
 
         players.add(player.getName());
 
         PlayerData.reset(player);
         Teleporter.tp(player, arenas.getLounge());
 
         api.getPlayerClass(player).equip(player);
 
         messenger.tell(player, Message.JOINED_LOBBY);
         messenger.tellLobby(Message.PLAYER_JOINED_LOBBY, player);
     }
 
     @Override
     public void startBattle() {
         Messenger messenger = api.getMessenger();
         Battle battle = api.getBattle();
         ArenaManager manager = api.getArenaManager();
 
         if (battle != null && battle.isInProgress()) throw new IllegalStateException("Battle in progress!");
         battle = getNewBattle();
 
         if (players.size() < battle.getMinPlayers()) throw new IllegalStateException("Not enough players!");
         if (manager.getReadyArenas(1).isEmpty()) throw new IllegalStateException("No arenas!");
 
         List<Arena> arenas = new ArrayList<Arena>();
         int votes = 0;
         for (Arena a : api.getScoreManager().getVotableArenas()) {
             int v = a.getVotes();
             if (v > votes) {
                 arenas.clear();
                 votes = v;
             }
             if (v == votes) arenas.add(a);
         }
 
         arena = arenas.get(random.nextInt(arenas.size()));
         battle.setArena(arena);
         messenger.tellLobby(Message.ARENA_CHOSEN, battle.getType(), arena);
         api.getScoreManager().setState(ScoreboardState.BATTLE);
 
         starting = true;
         timer.start();
     }
 
     public void start() {
         Battle battle = api.getBattle();
 
         for (String name : players) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) continue;
 
             Metadata.remove(player, "ready");
             Metadata.remove(player, "voted");
             Metadata.remove(player, "vote");
             battle.addPlayer(player);
         }
         battle.start();
         players.clear();
 
         for (Arena arena : api.getArenaManager().getArenas()) {
             arena.setVotes(0);
         }
 
         starting = false;
     }
 
     @Override
     public boolean isStarting() {
         return starting;
     }
 
     private Battle getNewBattle() {
         String id = ConfigManager.get(Config.MAIN).getString("Battle.Type", "FFA");
 
         int duration = ConfigManager.get(Config.MAIN).getInt("Battle.Duration", 300);
         int minPlayers = ConfigManager.get(Config.MAIN).getInt("Battle.MinPlayers", 2);
         int maxPlayers = ConfigManager.get(Config.MAIN).getInt("Battle.MaxPlayers", 0);
 
         if (duration < 5) duration = 5;
         if (minPlayers < 2) minPlayers = 2;
         if (maxPlayers < 1) maxPlayers = Integer.MAX_VALUE;
         if (minPlayers > maxPlayers) maxPlayers = minPlayers;
 
         if (id.equalsIgnoreCase("TDM")) return new TDMBattle(api, duration, minPlayers, maxPlayers);
 
         return new FFABattle(api, duration, minPlayers, maxPlayers);
     }
 
 }

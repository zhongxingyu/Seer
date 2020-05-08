 package me.limebyte.battlenight.core.battle;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.Lobby;
 import me.limebyte.battlenight.api.battle.ScorePane;
 import me.limebyte.battlenight.api.managers.ArenaManager;
 import me.limebyte.battlenight.api.managers.BattleManager;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.PlayerData;
 import me.limebyte.battlenight.core.tosort.SafeTeleporter;
 import me.limebyte.battlenight.core.util.SimpleScorePane;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class SimpleLobby implements Lobby {
 
     private BattleNightAPI api;
     private Set<String> players;
     private Arena arena;
     private SimpleScorePane scoreboard;
     
     public SimpleLobby(BattleNightAPI api) {
         this.api = api;
         players = new HashSet<String>();
         scoreboard = new SimpleScorePane();
     }
     
     @Override
     public void addPlayer(Player player) {
         Messenger messenger = api.getMessenger();
         Battle battle = api.getBattleManager().getBattle();
         
         if (battle.containsPlayer(player)) {
             messenger.tell(player, Message.ALREADY_IN_BATTLE);
             return;
         }
         
         if (players.size() >= battle.getMaxPlayers()) {
             messenger.tell(player, Message.BATTLE_FULL);
             return;
         }
         
         ArenaManager arenas = api.getArenaManager();
         if (!arenas.getLounge().isSet()) {
             messenger.tell(player, Message.WAYPOINTS_UNSET);
             return;
         }
 
         if (arena == null) {
             if (arenas.getReadyArenas(1).isEmpty()) {
                 messenger.tell(player, Message.NO_ARENAS);
                 return;
             }
             arena = arenas.getRandomArena(1);
         }
         
         players.add(player.getName());
         
         PlayerData.store(player);
         PlayerData.reset(player);
         SafeTeleporter.tp(player, arenas.getLounge());
         scoreboard.addPlayer(player);
         
         messenger.tell(player, Message.JOINED_LOBBY, arena);
         messenger.tellEveryoneExcept(player, Message.PLAYER_JOINED_LOBBY, player);
         
     }
 
     @Override
     public void removePlayer(Player player) {
         Messenger messenger = api.getMessenger();
         
        if (!players.contains(player)) {
             messenger.tell(player, Message.NOT_IN_LOBBY);
             return;
         }
         
         players.remove(player.getName());
         
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
 
         Metadata.remove(player, "ready");
         Metadata.remove(player, "kills");
         Metadata.remove(player, "deaths");
     }
     
     @Override
     public Set<String> getPlayers() {
         return new HashSet<String>(players);
     }
 
     @Override
     public void startBattle() {
         BattleManager manager = api.getBattleManager();
         Battle battle = manager.getBattle();
         
         if (battle.isInProgress()) throw new IllegalStateException("Battle in progress!");
         if (players.size() < battle.getMinPlayers()) throw new IllegalStateException("Not enough players!");
         battle.setArena(arena);
         
         for (String name : players) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null) continue;
             
             scoreboard.removePlayer(player);
             Metadata.remove(player, "ready");
             battle.addPlayer(player);
         }
         battle.start();
         
         players.clear();
     }
 
     @Override
     public ScorePane getScoreboard() {
         return scoreboard;
     }
     
 }

 package me.naithantu.ArenaPVP.Arena.ArenaExtras;
 
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import me.naithantu.ArenaPVP.Arena.Settings.ArenaSettings;
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 import me.naithantu.ArenaPVP.Util.PlayerConfigUtil;
 import me.naithantu.ArenaPVP.Util.Util;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 
 public class ArenaPlayerController {
     private Arena arena;
     private ArenaSettings settings;
     private ArenaSpectators arenaSpectators;
 
     public ArenaPlayerController(Arena arena, ArenaSettings settings, ArenaSpectators arenaSpectators) {
         this.arena = arena;
         this.settings = settings;
         this.arenaSpectators = arenaSpectators;
     }
 
     public void joinGame(Player player, ArenaTeam chosenTeam) {
         ArenaTeam teamToJoin = selectTeam(player, chosenTeam);
         if (teamToJoin != null) {
             Util.msg(player, "You joined team " + teamToJoin.getColoredName() + "!");
 
             //Teleport first to avoid problems with MVInventories
             Location location = teamToJoin.joinTeam(player, arena);
             PlayerConfigUtil.savePlayerConfig(player, new YamlStorage("players", player.getName()), location);
 
             arenaSpectators.onPlayerJoin(player);
 
             arena.getGamemode().updateTabs();
         }
     }
 
     public void leaveGame(final ArenaPlayer arenaPlayer) {
         Player player = Bukkit.getPlayerExact(arenaPlayer.getPlayerName());
 
         YamlStorage playerStorage = new YamlStorage("players", player.getName());
         if (player.isDead()) {
             Configuration playerConfig = playerStorage.getConfig();
             playerConfig.set("saved.hastoleave", true);
             playerStorage.saveConfig();
         } else {
             PlayerConfigUtil.loadPlayerConfig(player, playerStorage);
         }
 
        //Make sure player is no longer hidden.
        arenaSpectators.showSpectator(player);

         arenaPlayer.getTeam().leaveTeam(arenaPlayer, player);
 
         arena.getGamemode().clearTab(player);
         arena.getGamemode().updateTabs();
     }
 
     public ArenaTeam selectTeam(Player player, ArenaTeam chosenTeam) {
         //Check if player chose a team he wants to join.
         if (chosenTeam != null) {
             //Check if that team isn't full yet.
             if (chosenTeam.getPlayers().size() >= settings.getMaxPlayers() / arena.getTeams().size()) {
                 Util.msg(player, "The team you tried to join is full, joining a different team...");
             } else {
                 return chosenTeam;
             }
         }
 
         //Check if game isn't full yet.
         if (settings.getMaxPlayers() == 0 || getTotalPlayers() < settings.getMaxPlayers()) {
             //Find team with the least players and join that.
             ArenaTeam teamToJoin = null;
             for (ArenaTeam team : arena.getTeams()) {
                 if (teamToJoin == null || team.getPlayers().size() < teamToJoin.getPlayers().size()) {
                     teamToJoin = team;
                 }
             }
             return teamToJoin;
         } else {
             Util.msg(player, "That arena is full!");
             return null;
         }
     }
 
     private int getTotalPlayers() {
         int totalPlayers = 0;
         for (ArenaTeam team : arena.getTeams()) {
             totalPlayers += team.getPlayers().size();
         }
         return totalPlayers;
     }
 }

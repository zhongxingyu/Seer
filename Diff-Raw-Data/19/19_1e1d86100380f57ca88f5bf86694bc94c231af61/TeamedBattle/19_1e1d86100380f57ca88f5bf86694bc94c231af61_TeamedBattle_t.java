 package me.limebyte.battlenight.api.battle;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
 import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.Metadata;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class TeamedBattle extends Battle {
 
     private List<Team> teams = new ArrayList<Team>();
 
     public TeamedBattle() {
 
     }
 
     public boolean addTeam(Team team) {
         String name = team.getName();
         for (Team t : teams) {
             if (t.getName().equals(name)) return false;
         }
         team.setLives(getBattleLives());
         if (!teams.add(team)) return false;
         setMinPlayers(teams.size());
         return true;
     }
 
     public boolean removeTeam(Team team) {
         if (!teams.remove(team)) return false;
         setMinPlayers(teams.size());
         return true;
     }
 
     public List<Team> getLeadingTeams() {
         if (teams.size() == 0) return null;
 
         List<Team> leading = new ArrayList<Team>();
         for (Team team : teams) {
             if (leading.isEmpty()) {
                 leading.add(team);
                 continue;
             }
 
             int leadingKills = leading.get(0).getKills();
            if (leadingKills < team.getKills()) leading.clear();
 
            leading.add(team);
         }
 
         return leading;
     }
 
     public List<Team> getTeams() {
         return teams;
     }
 
     @Override
     public boolean addPlayer(Player player) {
         if (!assignTeam(player)) return false;
         boolean worked = super.addPlayer(player);
         if (worked) {
             Team team = getTeam(player);
             Messenger.tell(player, Message.JOINED_TEAM, team);
             Messenger.tellEveryoneExcept(player, true, Message.PLAYER_JOINED_TEAM, player, team);
         }
         return worked;
     }
 
     @Override
     public boolean removePlayer(Player player) {
         setTeam(player, null);
         return super.removePlayer(player);
     }
 
     private boolean assignTeam(Player player) {
         if (teams.size() < 2) return false;
 
         Team smallest = null;
         for (Team team : teams) {
             if (smallest == null || team.getSize() < smallest.getSize()) smallest = team;
         }
 
         setTeam(player, smallest);
         return true;
     }
 
     public void setTeam(Player player, Team team) {
         String prevTeam = Metadata.getString(player, "team");
         if (prevTeam != null) {
             for (Team t : teams) {
                 if (t.getName().equals(prevTeam)) {
                     t.decrementSize();
                     break;
                 }
             }
         }
 
         if (team != null) {
             Metadata.set(player, "team", team.getName());
             team.incrementSize();
         } else {
             Metadata.remove(player, "team");
         }
     }
 
     public Team getTeam(Player player) {
         String teamName = Metadata.getString(player, "team");
         if (teamName != null) {
             for (Team team : teams) {
                 if (team.getName().equals(teamName)) return team;
             }
         }
         return null;
     }
 
     public boolean areEnemies(Player player1, Player player2) {
         return (!getTeam(player1).getName().equals(getTeam(player2).getName()));
     }
 
     @Override
     public Location toSpectator(Player player, boolean death) {
         if (!containsPlayer(player)) return null;
         setTeam(player, null);
         return super.toSpectator(player, death);
     }
 
     @Override
     protected String getWinMessage() {
         String message;
         List<Team> leading = getLeadingTeams();
 
         if (leading.isEmpty() || leading.size() == getTeams().size()) {
             message = Message.DRAW.getMessage();
         } else if (leading.size() == 1) {
            message = Messenger.format(Message.TEAM_WON, leading.get(0));
         } else {
             message = Messenger.format(Message.TEAM_WON, leading);
         }
 
         return message;
     }
 
     @Override
     public int getLives(Player player) {
         Team team = getTeam(player);
         if (team != null) return team.getLives();
         return 0;
     }
 
     @Override
     public void setLives(Player player, int lives) {
         Team team = getTeam(player);
         if (team != null) team.setLives(lives);
     }
 
     @Override
     public boolean shouldEnd() {
         if (!isInProgress()) return false;
         for (Team team : getTeams()) {
             if (team.getSize() < 1) return true;
         }
         return false;
     }
 
     @Override
     protected void addKill(Player player) {
         super.addKill(player);
 
         Team team = getTeam(player);
         if (team != null) team.addKill();
     }
 
     @Override
     public boolean stop() {
         if (!onStop()) return false;
 
         Iterator<String> pIt = getPlayers().iterator();
         while (pIt.hasNext()) {
             Player player = toPlayer(pIt.next());
             if (player == null) {
                 pIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             setTeam(player, null);
             pIt.remove();
         }
 
         Iterator<String> sIt = getSpectators().iterator();
         while (sIt.hasNext()) {
             Player player = toPlayer(sIt.next());
             if (player == null) {
                 sIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             sIt.remove();
         }
 
         for (Team team : teams) {
             if (team == null) continue;
             team.reset(this);
         }
 
         getLeadingPlayers().clear();
         setArena(null);
         inProgress = false;
         return true;
     }
 
     @Override
     public void onPlayerDeath(BattleDeathEvent event) {
         Player player = event.getPlayer();
         Player killer = player.getKiller();
 
        if (killer != null) addKill(killer);
 
         int deaths = Metadata.getInt(player, "deaths");
         Metadata.set(player, "deaths", ++deaths);
 
         decrementLives(player);
         int lives = getLives(player);
 
         if (lives > 0) {
             String message = "Your team has " + lives + " lives remaining.";
             if (lives == 1) message = ChatColor.RED + "Last life!";
             Team team = getTeam(player);
 
             for (String name : getPlayers()) {
                 Player p = toPlayer(name);
                 if (p == null) continue;
                 if (getTeam(p) != team) continue;
                 Messenger.tell(p, message);
             }
 
             event.setCancelled(true);
         }
     }
 }

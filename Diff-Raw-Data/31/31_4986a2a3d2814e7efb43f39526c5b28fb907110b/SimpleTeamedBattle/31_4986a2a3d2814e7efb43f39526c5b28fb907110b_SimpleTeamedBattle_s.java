 package me.limebyte.battlenight.core.battle;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Team;
 import me.limebyte.battlenight.api.battle.TeamedBattle;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.SafeTeleporter;
 import me.limebyte.battlenight.core.util.BattleNightScoreboard;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class SimpleTeamedBattle extends SimpleBattle implements TeamedBattle {
 
     private List<Team> teams = new ArrayList<Team>();
 
     public SimpleTeamedBattle(BattleNightAPI api, int duration, int minPlayers, int maxPlayers) {
         super(api, duration, minPlayers, maxPlayers);
     }
 
     @Override
     public void addDeath(Player player) {
         super.addDeath(player);
 
         Team team = getTeam(player);
         if (team != null) {
             team.addDeath();
         }
 
         BattleNightScoreboard.setKDR(player, (int) Math.round(getKDR(player) * 100));
     }
 
     @Override
     public void addKill(Player player) {
         super.addKill(player);
 
         Team team = getTeam(player);
         if (team != null) {
             team.addKill();
         }
 
         BattleNightScoreboard.setKDR(player, (int) Math.round(getKDR(player) * 100));
     }
 
     @Override
     public boolean addPlayer(Player player) {
         if (!assignTeam(player)) return false;
         boolean worked = super.addPlayer(player);
         if (worked) {
             Messenger messenger = api.getMessenger();
             Team team = getTeam(player);
            BattleNightScoreboard.addPlayer(player, team.getName());
             messenger.tell(player, Message.JOINED_TEAM, team);
             messenger.tellEveryoneExcept(player, Message.PLAYER_JOINED_TEAM, player, team);
         }
         return worked;
     }
 
     public boolean addTeam(Team team) {
         String name = team.getName();
         for (Team t : teams) {
             if (t.getName().equals(name)) return false;
         }
         if (!teams.add(team)) return false;
         setMinPlayers(teams.size());
        BattleNightScoreboard.addTeam(team.getDisplayName(), team.getColour(), false);
         return true;
     }
 
     public boolean areEnemies(Player player1, Player player2) {
         return !getTeam(player1).getName().equals(getTeam(player2).getName());
     }
 
     public List<Team> getLeadingTeams() {
         if (teams.size() == 0) return null;
 
         List<Team> leading = new ArrayList<Team>();
         for (Team team : teams) {
             if (leading.isEmpty()) {
                 leading.add(team);
                 continue;
             }
 
             Team inLead = leading.get(0);
             double leadKD = inLead.getKDR();
             double teamKD = team.getKDR();
 
             if (leadKD > teamKD) {
                 continue;
             }
             if (leadKD < teamKD) {
                 leading.clear();
             }
 
             leading.add(team);
         }
 
         return leading;
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
 
     public List<Team> getTeams() {
         return teams;
     }
 
     @Override
     public boolean removePlayer(Player player) {
         setTeam(player, null);
         BattleNightScoreboard.removePlayer(player);
         return super.removePlayer(player);
     }
 
     public boolean removeTeam(Team team) {
         if (!teams.remove(team)) return false;
         setMinPlayers(teams.size());
         return true;
     }
 
     public void setTeam(Player player, Team team) {
         String prevTeam = Metadata.getString(player, "team");
         if (prevTeam != null) {
             for (Team t : teams) {
                 if (t.getName().equals(prevTeam)) {
                     if (t instanceof SimpleTeam) ((SimpleTeam) t).decrementSize();
                     break;
                 }
             }
         }
 
         if (team != null) {
             Metadata.set(player, "team", team.getName());
             if (team instanceof SimpleTeam) ((SimpleTeam) team).incrementSize();
         } else {
             Metadata.remove(player, "team");
         }
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
     public boolean stop() {
         for (String name : getPlayers()) {
             Player player = toPlayer(name);
             if (player == null) {
                 continue;
             }
             setTeam(player, null);
             BattleNightScoreboard.removePlayer(player);
         }
 
         boolean result = super.stop();
 
         for (Team team : teams) {
             if (team == null) {
                 continue;
             }
             if (team instanceof SimpleTeam) ((SimpleTeam) team).reset(this);
         }
 
         return result;
     }
 
     @Override
     public Location toSpectator(Player player, boolean death) {
         if (!containsPlayer(player)) return null;
         setTeam(player, null);
         return super.toSpectator(player, death);
     }
 
     private boolean assignTeam(Player player) {
         if (teams.size() < 2) return false;
 
         Team smallest = null;
         for (Team team : teams) {
             if (smallest == null || team.getSize() < smallest.getSize()) {
                 smallest = team;
             }
         }
 
         setTeam(player, smallest);
         return true;
     }
 
     @Override
     protected String getWinMessage() {
         Messenger messenger = api.getMessenger();
         String message;
         List<Team> leading = getLeadingTeams();
 
         if (leading.isEmpty() || leading.size() == getTeams().size()) {
             message = Message.DRAW.getMessage();
         } else if (leading.size() == 1) {
             message = messenger.format(Message.TEAM_WON, leading.get(0));
         } else {
             message = messenger.format(Message.TEAM_WON, leading);
         }
 
         return message;
     }
 
     @Override
     protected void teleportAllToSpawn() {
         @SuppressWarnings("unchecked")
         List<SimpleWaypoint> waypoints = (ArrayList<SimpleWaypoint>) getArena().getSpawnPoints().clone();
         List<SimpleWaypoint> free = waypoints;
         Random random = new Random();
 
         HashMap<String, SimpleWaypoint> spawns = new HashMap<String, SimpleWaypoint>();
 
         for (Team team : getTeams()) {
             if (free.size() <= 0) {
                 free = waypoints;
             }
             int id = random.nextInt(free.size());
             spawns.put(team.getName(), free.get(id));
             free.remove(id);
         }
 
         for (String name : getPlayers()) {
             Player player = toPlayer(name);
             if (player == null || !player.isOnline()) {
                 continue;
             }
             SafeTeleporter.tp(player, spawns.get(Metadata.getString(player, "team")).getLocation());
         }
     }
 }

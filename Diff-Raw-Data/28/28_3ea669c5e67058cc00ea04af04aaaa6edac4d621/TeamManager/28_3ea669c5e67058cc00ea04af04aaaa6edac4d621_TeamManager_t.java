 package jk_5.nailed.map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import jk_5.nailed.NailedModContainer;
 import jk_5.nailed.players.Player;
 import jk_5.nailed.players.Team;
 import jk_5.nailed.players.TeamBuilder;
 import lombok.Getter;
 import net.minecraft.network.packet.Packet209SetPlayerTeam;
 
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class TeamManager {
 
     @Getter private final Map map;
     @Getter private final Team defaultTeam;
     @Getter private final List<Team> teams = Lists.newArrayList();
 
     private final java.util.Map<Player, Team> playerTeamMap = Maps.newHashMap();
 
     public TeamManager(Map map){
         this.map = map;
         this.defaultTeam = new Team(this.map, "unknown-" + this.map.getSaveFileName());
 
         if(this.map.getMappack() == null) return;
 
         for(TeamBuilder builder : this.map.getMappack().getMappackMetadata().getDefaultTeams()){
             this.teams.add(builder.build(this.map));
         }
     }
 
     public Team getTeam(String name){
         for(Team team : this.teams){
             if(team.getTeamId().equals(name)){
                 return team;
             }
         }
         return null;
     }
 
     public Team getPlayerTeam(Player player) {
         Team team = this.playerTeamMap.get(player);
         if(team == null) return this.defaultTeam;
         return team;
     }
 
     public void setPlayerTeam(Player player, Team team) {
         if(this.playerTeamMap.get(player) == team) return;
         if(this.playerTeamMap.containsKey(player)){
             team.onRemovePlayer(player);
             this.playerTeamMap.remove(player);
         }
         this.playerTeamMap.put(player, team);
         team.onAddPlayer(player);
         player.getEntity().refreshDisplayName();
     }
 
     public void onWorldSet(){
         for(Team team : this.teams){
             team.onWorldSet();
         }
     }
 
     public void onPlayerJoinedMap(Player player){
         if(!this.playerTeamMap.containsKey(player)){
             this.playerTeamMap.put(player, this.defaultTeam);
             player.getEntity().refreshDisplayName();
         }
         Team team = this.playerTeamMap.get(player);
         if(team.getScoreboardTeam() != null){
             player.sendPacket(new Packet209SetPlayerTeam(team.getScoreboardTeam(), 0));
         }
         team.addPlayerToScoreboardTeam(player);
     }
 
     public void onPlayerLeftMap(Player player){
         if(!this.playerTeamMap.containsKey(player)){
             this.playerTeamMap.put(player, this.defaultTeam);
             player.getEntity().refreshDisplayName();
         }
         Team team = this.playerTeamMap.get(player);
         team.removePlayerFromScoreboardTeam(player);
         if(team.getScoreboardTeam() != null){
             player.sendPacket(new Packet209SetPlayerTeam(team.getScoreboardTeam(), 1));
         }
     }
 
     public void onGameStarted(){
         for(Team team : this.teams){
             if(team.getTeamSpeakChannelID() == -1){
                 NailedModContainer.getTeamspeakClient().createChannelFor(team);
             }
             NailedModContainer.getTeamspeakClient().movePlayersIntoChannel(team);
         }
     }
 
     public void onGameEnded(){
         for(Team team : this.teams){
             if(team.getTeamSpeakChannelID() != -1){
                 NailedModContainer.getTeamspeakClient().movePlayersToLobby(team);
                 NailedModContainer.getTeamspeakClient().removeChannel(team);
             }
         }
     }
 }

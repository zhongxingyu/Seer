 package fr.aumgn.diamondrush.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.diamondrush.region.TeamSpawn;
 import fr.aumgn.diamondrush.region.Totem;
 import fr.aumgn.diamondrush.util.Util;
 import fr.aumgn.diamondrush.util.Vector;
 
 public class Team {
 
     private String name;
     private TeamColor color;
     private Player foreman;
     private List<Player> players;
     private Totem totem;
     private TeamSpawn spawn;
     private int lives;
 
     public Team(String name, TeamColor color) {
         this.name = name;
         this.color = color;
         this.foreman = null;
         this.players = new ArrayList<Player>();
         this.totem = null;
         this.spawn = null;
         this.lives = 5;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDisplayName() {
         return color.getChatColor() + name + ChatColor.RESET;
     }
 
     public TeamColor getColor() {
         return color;
     }
 
     public Player getForeman() {
         return foreman;
     }
 
     public void setForeman(Player foreman) {
         this.foreman = foreman;
     }
 
     public List<Player> getPlayers() {
         return players;
     }
 
     public Totem getTotem() {
         return totem;
     }
 
     public void setTotem(Vector pos, int worldHeight) {
         totem = new Totem(pos, worldHeight);
     }
 
     public TeamSpawn getSpawn() {
         return spawn;
     }
 
     public void setSpawn(Vector pos, int worldHeight) {
         spawn = new TeamSpawn(pos);
     }
 
     public int getLives() {
         return lives;
     }
 
     public void sendMessage(String message) {
         for (Player player : players) {
             player.sendMessage(message);
         }
     }
 
     public int size() {
         return players.size();
     }
 
     public void setTeamName(Player player) {
         String rawName = ChatColor.stripColor(player.getDisplayName());
         String teamName = color.getChatColor() + rawName + ChatColor.RESET;
         player.setDisplayName(teamName);
         if (teamName.length() > 16) {
             player.setPlayerListName(teamName.substring(0, 16));
         } else {
             player.setPlayerListName(teamName);
         }
     }
 
     void addPlayer(Player player) {
         players.add(player);
         setTeamName(player);
     }
 
     void removePlayer(Player player) {
         players.remove(player);
         if (player.equals(foreman)) {
             foreman = Util.pickRandom(players);
            sendMessage(foreman.getDisplayName() + 
                     " est maintenant le chef d'equipe.");
         }
     }
 
     void decreaseLives() {
         --lives;
     }
 }

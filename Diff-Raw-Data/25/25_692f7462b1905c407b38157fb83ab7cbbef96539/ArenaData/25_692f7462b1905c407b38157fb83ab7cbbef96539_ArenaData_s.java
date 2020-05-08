  package me.dalton.capturethepoints;
  
 import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.LinkedList;
  import java.util.List;
 import java.util.Set;
 import org.bukkit.entity.Player;
  
  public class ArenaData {
    public String name;
    public String world;
    public HashMap<String, CTPPoints> teamSpawns = new HashMap<String, CTPPoints>();
    public List<CTPPoints> capturePoints = new LinkedList<CTPPoints>();
    public Lobby lobby;
    public int x1;
    public int z1;
    public int x2;
    public int z2;
    public int minimumPlayers = 2;
    public int maximumPlayers = 9999;
    
    // Kjhf
     /** Get all Players in this arena as a list of playername strings
      * @param ctp CaptureThePoints instance
      * @return The playername list */
     public List<String> getPlayersPlaying(CaptureThePoints ctp) {
         if (!ctp.mainArena.equals(this)) {
             return null;
         }
         List<String> players = new ArrayList<String>();
        Set<Player> playerset = ctp.playerData.keySet();
        for (Player p : playerset) {
             if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).color == null) { 
                 continue; // Player is not yet in game.
             } else {
                 players.add(p.getName());
             }
         }
         return players;        
     }
  }
 

 package com.bpodgursky.hubris.universe;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Range;
 import com.google.gson.Gson;
 import org.apache.commons.lang.StringUtils;
 
 
 public class GameState {
 
   public final Map<Integer, Player> playersByID;
   public final Map<Integer, Star> starsByID;
   public final Map<Integer, Fleet> fleetsByID;
   public final Map<String, Tech> techStatesByName;
   public final Alliance alliance;
   public final Game gameData;
   private final int playerId;
   private final GameState previousState;
 
   public GameState(GameState previousState, Game gameData, Collection<Player> players, Collection<Star> stars, Collection<Fleet> fleets,
                    Collection<Tech> techs, Alliance alliance, int playerId) {
 
     this.previousState = previousState;
     this.playersByID = Maps.newHashMap();
     for (Player p : players) {
       playersByID.put(p.id, p);
     }
 
     this.starsByID = Maps.newHashMap();
     for (Star s : stars) {
       starsByID.put(s.id, s);
     }
 
     this.fleetsByID = Maps.newHashMap();
     for (Fleet f : fleets) {
       fleetsByID.put(f.id, f);
     }
 
     this.techStatesByName = Maps.newHashMap();
     for (Tech t : techs) {
       techStatesByName.put(t.researchName, t);
     }
 
     this.alliance = alliance;
     this.gameData = gameData;
     this.playerId = playerId;
   }
 
   public Star getStar(int starId, boolean useHistoric){
 
     if(!useHistoric){
       return starsByID.get(starId);
     }
 
     Star currentInfo = starsByID.get(starId);
 
     if(currentInfo.economy != null){
       return currentInfo;
     }
 
     Star lastVisible = getLastVisible(starId);
 
     return merge(currentInfo, lastVisible);
   }
 
   private boolean equal(Integer int1, Integer int2){
     return int1 == null && int2 == null || int1 != null && int2 != null && int1.intValue() == int2.intValue();
   }
 
   private Star merge(Star visible, Star lastVisible){
 
     Integer playerVisible = visible.getPlayerNumber();
     Integer playerLast = lastVisible.getPlayerNumber();
 
     //  if the same player controls it, last state is the best guess
     if(equal(playerVisible, playerLast)){
       return lastVisible;
     } else {
       return new Star(visible.getName(), visible.getPlayerNumber(),
           null, null, null,
           lastVisible.getIndustry(), lastVisible.getIndustryUpgrade(),
           lastVisible.getScience(), lastVisible.getScienceUpgrade(),
          visible.getId(), visible.getX(), visible.getY(), null, visible.getResources(), lastVisible.getFleets());
 
     }
   }
 
   public GameState previousState(){
     return previousState;
   }
 
   private Star getLastVisible(int starId){
     GameState state = this;
 
     while(state != null){
       Star star = state.getStar(starId, false);
       if(star.economy != null){
         return star;
       }
       state = state.previousState();
     }
 
     return null;
   }
 
   public String toString() {
     return new Gson().toJson(this);
   }
 
   public Map<Integer, Player> getPlayers(){
     return playersByID;
   }
 
   private transient static final Map<Integer, String> colorsByID = new HashMap<Integer, String>();
 
   static {
     colorsByID.put(-1, "#778899");
     colorsByID.put(0, "red");
     colorsByID.put(1, "blue");
     colorsByID.put(2, "green");
     colorsByID.put(3, "brown");
     colorsByID.put(4, "#ADD8E6");
     colorsByID.put(5, "blueviolet");
     colorsByID.put(6, "#D2691E");
     colorsByID.put(7, "#00008B");
     colorsByID.put(8, "darkorange");
     colorsByID.put(9, "coral");
     colorsByID.put(10, "crimson");
     colorsByID.put(11, "black");
     colorsByID.put(12, "gold");
 
   }
 
   public void writeGnuPlot(String epsName) throws IOException, InterruptedException {
     StringBuilder builder = new StringBuilder();
     builder.append("set terminal postscript eps enhanced color \"Times-Roman\" 3\n");
     builder.append("set output \"" + epsName + "\"\n");
 
     String root = UUID.randomUUID().toString().replaceAll("-", "");
     String tmpStarRoot = "tmp_star_data_" + root;
     String tmpFleetRoot = "tmp_fleet_data_" + root;
 
     Map<Integer, FileWriter> playerStarFiles = new HashMap<Integer, FileWriter>();
     Map<Integer, FileWriter> playerFleetFiles = new HashMap<Integer, FileWriter>();
 
     for (Integer player : playersByID.keySet()) {
       playerStarFiles.put(player, new FileWriter(tmpStarRoot + "_" + player));
       playerFleetFiles.put(player, new FileWriter(tmpFleetRoot + "_" + player));
     }
     playerStarFiles.put(-1, new FileWriter(tmpStarRoot + "_none"));
     playerFleetFiles.put(-1, new FileWriter(tmpFleetRoot + "_none"));
 
     for (Star s : starsByID.values()) {
       String label = (s.ships == null ? "" : s.ships + "-") + "[" +
           (s.economy == null ? "" : s.economy + ",") +
           (s.industry == null ? "" : s.industry + ",") +
           (s.science == null ? "" : s.science) + "]" +
           (s.resources == null ? "" : "-" + s.resources);
 
       playerStarFiles.get(s.playerNumber).append(s.x + "\t" + (-s.y) + "\t" + label + "\n");
     }
 
     for (Map.Entry<Integer, FileWriter> entry : playerStarFiles.entrySet()) {
       builder.append("set style line " + (entry.getKey() + 10) + " lt rgb \"" + colorsByID.get(entry.getKey()) + "\" lw 3 pt 6\n");
     }
 
     builder.append("set pointsize .1\n");
     builder.append("set nokey\n");
     builder.append("unset border\n");
     builder.append("unset xtics\n");
     builder.append("unset ytics\n");
     builder.append("plot ");
 
     List<String> plots = new ArrayList<String>();
     for (Map.Entry<Integer, FileWriter> entry : playerStarFiles.entrySet()) {
       entry.getValue().close();
       plots.add("'" + tmpStarRoot + "_" + (entry.getKey() == -1 ? "none" : entry.getKey()) + "' using 1:2:3 with labels left offset -5,-1 point ls " + (entry.getKey() + 10));
     }
 
     builder.append(StringUtils.join(plots, ", \\\n"));
     builder.append("\n");
 
     FileWriter plotFile = new FileWriter("tmp_figure_" + root + ".gnu");
     plotFile.write(builder.toString());
     plotFile.close();
 
     Process p = Runtime.getRuntime().exec("gnuplot tmp_figure_" + root + ".gnu");
     p.waitFor();
 
     for (Map.Entry<Integer, FileWriter> entry : playerFleetFiles.entrySet()) {
       new File(tmpStarRoot + "_" + entry.getKey()).delete();
       new File(tmpFleetRoot + "_" + entry.getKey()).delete();
     }
     new File("tmp_figure_" + root + ".gnu").delete();
     new File(tmpStarRoot + "_none").delete();
     new File(tmpFleetRoot + "_none").delete();
   }
 
   public Game getGameData() {
     return gameData;
   }
 }

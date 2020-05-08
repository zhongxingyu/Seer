 /*
  * MiningContest
  * Copyright (C) 2011 IndiPlex
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.indiplex.miningcontest.logic;
 
 import de.indiplex.manager.IPMAPI;
 import de.indiplex.miningcontest.MiningContest;
 import de.indiplex.miningcontest.generator.Base;
 import de.indiplex.miningcontest.generator.Outpost;
 import de.indiplex.miningcontest.logic.classes.MCClass;
 import de.indiplex.miningcontest.map.Map;
 import de.indiplex.miningcontest.map.MapChunk;
 import de.indiplex.miningcontest.map.MapParser;
 import de.indiplex.virtualchests.VCAPI;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author IndiPlex <Cartan12@indiplex.de>
  */
 public class MiCo {
 
     private ArrayList<Team> teams = new ArrayList<Team>();
     public boolean initializing = false;
     public boolean started = false;
     private int nextTeam = 0;
     private Random r = new Random(System.currentTimeMillis());
     private Map map;
     private IPMAPI api;
     private MapChunk lobby;
     private ArrayList<WithDoorsAndSigns> checkedChunks = new ArrayList<WithDoorsAndSigns>();
     private ArrayList<Outpost> outposts = new ArrayList<Outpost>();
     public long elapsedTime;
     public long startingTime;
     public GameThread gameThread;
     public StartThread startThread;
     private World micoWorld;
     private Shop shop;
     private ArrayList<Player> players = new ArrayList<Player>(); // ArrayList to check players faster
 
     public MiCo() {
         this.elapsedTime = 0;
         setMap("Map3.png");
         shop = new Shop(this);
         shop.init();
     }
 
     private void setMap(String mapName) {
        map = MapParser.parseMap("plugins/IndiPlex Manager/config/Mining Contest/res/" + mapName);
         MapChunk[] mapChunks = map.getMapChunks();
         int t = 0;
         for (MapChunk mc : mapChunks) {
             if (mc.getType().equals(MapChunk.Type.BASE)) {
                 Team team = new Team(t, this);
                 team.setBase((Base) mc);
                 ((Base) mc).setTeam(team);
                 teams.add(team);
                 checkedChunks.add((WithDoorsAndSigns) mc);
             } else if (mc.getType().equals(MapChunk.Type.LOBBY)) {
                 lobby = mc;
             } else if (mc.getType().equals(MapChunk.Type.OUTPOST)) {
                 checkedChunks.add((WithDoorsAndSigns) mc);
                 outposts.add((Outpost) mc);
             }
         }
         System.out.println(outposts.size());
     }
 
     public Shop getShop() {
         return shop;
     }
 
     public Map getMap() {
         return map;
     }
 
     public void setApi(IPMAPI api) {
         this.api = api;
     }
 
     public ArrayList<Player> getPlayers() {
         return players;
     }
 
     public void stop() {
         VCAPI vc = (VCAPI) MiningContest.getAPI().getAPI("vc");
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 p.sendMessage("You got " + t.getPoints(p));
                 World w = Bukkit.getWorlds().get(0);
                 p.teleport(w.getSpawnLocation());
                 
                 vc.fillInventory(p.getInventory(), "mico_"+p.getName());
                 vc.removeInventory("mico_"+p.getName());
                 p.updateInventory();
             }
         }
         reset();
     }
 
     public void reset() {
         for (Team t : teams) {
             t.reset();
         }
         elapsedTime = 0;
         started = false;
         initializing = false;
         if (gameThread != null) {
             gameThread.setRunning(false);
         }
     }
 
     public void init(int startTime, Integer[] intervals) {
         startThread = new StartThread(startTime, intervals, this);
         new Thread(startThread).start();
     }
 
     public void start() {
         Bukkit.getServer().broadcastMessage("The mining-contest starts now!");
         VCAPI vc = (VCAPI) MiningContest.getAPI().getAPI("vc");
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 Location loc = new Location(Bukkit.getWorld("ContestWorld"), t.getBase().getPos().x * 16 + 3, 53, t.getBase().getPos().y * 16 + 3);
                 p.teleport(loc);
                 
                 vc.storeInventory(p.getInventory(), "mico_"+p.getName());
                 p.getInventory().clear();
                 p.updateInventory();
             }
         }
         for (Team t : teams) {
             players.addAll(t.getMembers());
         }
         gameThread = new GameThread(this);
         new Thread(gameThread).start();
         started = true;
     }
 
     public Point getBase(int team) {
         return teams.get(team).getBase().getPos();
     }
 
     public int getTeamCount() {
         return teams.size();
     }
 
     public boolean isInBase(int x, int z) {
         for (Team t : teams) {
             Point p = t.getBase().getPos();
             if (p.x == x && p.y == z) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isInBase(Location loc) {
         return isInBase(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && loc.getWorld().getName().equalsIgnoreCase("ContestWorld");
     }
 
     public boolean isInOutpost(Location loc) {
         return isInOutpost(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && loc.getWorld().getName().equalsIgnoreCase("ContestWorld");
     }
 
     public boolean isInsideTeamArea(Player player) {
         Team t = getTeam(player);
         Location loc = player.getLocation();
         if (!loc.getWorld().getName().equalsIgnoreCase("ContestWorld")) {
             return false;
         }
         if (t.getBase().isInside(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
             return true;
         }
         for (Outpost out:outposts) {
             if (out.getTeam().equals(t)) {
                 if (out.isInside(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isInOutpost(int x, int y, int z) {
         for (MapChunk mc : outposts) {
             if (mc.isInside(x, y, z)) {
                 return true;
             }
         }
         return false;
     }
 
     public MapChunk getLobby() {
         return lobby;
     }
 
     public ArrayList<Outpost> getOutposts() {
         return outposts;
     }
 
     private boolean isInBase(int x, int y, int z) {
         for (Team t : teams) {
             MapChunk mc = t.getBase();
             if (mc.isInside(x, y, z)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean canDestroy(int x, int y, int z, World world) {
         boolean b = false;
         for (MapChunk mc : outposts) {
             if (mc.isInside(x, y, z)) {
                 b = true;
                 break;
             }
         }
         if (!b) {
             for (Team t : teams) {
                 if (t.getBase().isInside(x, y, z)) {
                     b = true;
                     break;
                 }
             }
         }
         if (!b) {
             if (lobby.isInside(x, y, z)) {
                 b = true;
             }
         }
         return !(world.getName().equalsIgnoreCase("ContestWorld") && b);
     }
 
     public boolean canDestroy(Location loc) {
         return canDestroy(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
     }
 
     public static boolean isInMap(int x, int z) {
         return (x <= 10 && x >= -10) && (z <= 10 && z >= -10);
     }
 
     public static boolean isBorderChunk(int x, int z) {
         return (x == 10 || x == -10) || (z == 10 || z == -10);
     }
 
     public ArrayList<Team> getTeams() {
         return teams;
     }
 
     public World getContestWorld() {
         return micoWorld;
     }
 
     public Team getNextTeam() {
         Team t = teams.get(nextTeam);
         return t;
     }
 
     public void nextTeam() {
         nextTeam++;
         if (nextTeam == teams.size()) {
             nextTeam = 0;
         }
     }
 
     public ArrayList<WithDoorsAndSigns> getCheckedChunks() {
         return checkedChunks;
     }
 
     public void setNextTeam(int nextTeam) {
         this.nextTeam = nextTeam;
     }
 
     public boolean isMiCoPlayer(Player player) {
         return started && players.contains(player);
     }
 
     public Team getTeam(Player player) {
         for (Team t : teams) {
             if (t.hasMember(player)) {
                 return t;
             }
         }
         return null;
     }
 
     public void printPoints() {
         Team winningTeam = getWinningTeam();
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 printPoints(p, t, winningTeam);
             }
         }
     }
     
     public void printPoints(Player player) {
         printPoints(player, getTeam(player), getWinningTeam());
     }
     
     private void printPoints(Player p, Team pTeam, Team winningTeam) {
         p.sendMessage("You have " + pTeam.getPoints(p) + " points and your teams has " + pTeam.getTeamPoints() + " points!");
                 if (pTeam.equals(winningTeam)) {
                     p.sendMessage("Your team is winning :)");
                 } else {
                     p.sendMessage("Your team is losing :(");
                 }
     }
     
     public Team getWinningTeam() {
         Team winningTeam = null;
         for (Team t : teams) {
             if (winningTeam == null || t.getTeamPoints() > winningTeam.getTeamPoints()) {
                 winningTeam = t;
             }
         }
         return winningTeam;
     }
 
     public MCClass getClass(Player p) {
         for (Team t : teams) {
             if (t.hasMember(p)) {
                 return t.getClass(p);
             }
         }
         return null;
     }
 }

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
 import de.indiplex.miningcontest.generator.Lobby;
 import de.indiplex.miningcontest.generator.Outpost;
 import de.indiplex.miningcontest.logic.classes.MiCoClass;
 import de.indiplex.miningcontest.map.Map;
 import de.indiplex.miningcontest.map.MapChunk;
 import de.indiplex.miningcontest.map.MapParser;
 import de.indiplex.miningcontest.util.MiCoConfig;
 import de.indiplex.multiworlds.MultiWorldsAPI;
 import de.indiplex.virtualchests.VCAPI;
 import java.io.File;
 import java.util.ArrayList;
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
     private Map map;
     private IPMAPI api;
     private Lobby lobby;
     private ArrayList<WithDoorsAndSigns> checkedChunks = new ArrayList<WithDoorsAndSigns>();
     private ArrayList<Outpost> outposts = new ArrayList<Outpost>();
     public long elapsedTime;
     public long startingTime;
     public GameThread gameThread;
     public StartThread startThread;
     private World micoWorld;
     private Shop shop;
     private MiCoConfig config = new MiCoConfig();
     private ArrayList<Player> players = new ArrayList<Player>(); // ArrayList to check players faster
 
     /**
      * Creates a new MiningContest
      */
     public MiCo() {
         this.elapsedTime = 0;
         shop = new Shop(this);
        shop.init();
     }
 
     private void setMap(String mapName) {
         teams.clear();
         map = MapParser.parseMap(new File(MiningContest.getAPI().getDataFolder(), "res/" + mapName));
         if (map==null) {
             return;
         }
         MapChunk[] mapChunks = map.getMapChunks();
         int t = 0;
         for (MapChunk mc : mapChunks) {
             if (mc.getType().equals(MapChunk.Type.BASE)) {
                 Team team = new Team(t, this);
                 team.setBase((Base) mc);
                 ((Base) mc).setTeam(team);
                 teams.add(team);
                 t++;
                 checkedChunks.add((WithDoorsAndSigns) mc);
             } else if (mc.getType().equals(MapChunk.Type.LOBBY)) {
                 lobby = (Lobby) mc;
             } else if (mc.getType().equals(MapChunk.Type.OUTPOST)) {
                 checkedChunks.add((WithDoorsAndSigns) mc);
                 outposts.add((Outpost) mc);
             }
         }
     }
 
     /**
      * Resets the world MiningContest is working in
      */
     public void resetWorld() {
         micoWorld = Bukkit.getWorld("ContestWorld");
         ((MultiWorldsAPI) MiningContest.getAPI().getAPI("MultiWorlds")).resetWorld(micoWorld, 10);
     }
 
     /**
      * Get the MiCoShop
      * @return Shop The shop
      */
     public Shop getShop() {
         return shop;
     }
 
     /**
      * Get the map
      * @return Map The map
      */
     public Map getMap() {
         return map;
     }
 
     /**
      * Sets the IPMAPI
      * @param api The IPMAPI to set
      */
     public void setApi(IPMAPI api) {
         this.api = api;
         config.load();
         MiningContest.log.info(MiningContest.pre + "Loaded config");
         setMap(config.getMapName());
     }
 
     /**
      * Get the list of all player in the MiningContest
      * @return ArrayList<Player> The Players
      */
     public ArrayList<Player> getPlayers() {
         return players;
     }
 
     /**
      * Stops the contest
      */
     @SuppressWarnings(value="deprecation")
     public void stop() {
         VCAPI vc = (VCAPI) MiningContest.getAPI().getAPI("vc");
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 World w = Bukkit.getWorlds().get(0);
                 p.teleport(w.getSpawnLocation());
 
                 vc.fillInventory(p.getInventory(), "mico_" + p.getName());
                 vc.removeInventory("mico_" + p.getName());
                 p.updateInventory();
             }
         }
         reset();
     }
 
     /**
      * Resets the entire contest
      */
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
         if (startThread != null) {
             startThread.setRunning(false);
         }
     }
 
     /**
      * Initializes the MiningContest
      * @param startTime The time when it should start
      * @param intervals The intervals where it should broadcast
      */
     public void init(int startTime, Integer[] intervals) {
         resetWorld();
         initializing = true;
         if (intervals != null) {
             startThread = new StartThread(startTime, intervals, this);
             new Thread(startThread).start();
         }
     }
 
     /**
      * Ends the MiningContest
      */
     public void end() {
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 p.sendMessage("You got " + t.getPoints(p) + " points!");
             }
         }
         stop();
     }
 
     /**
      * Get the MiCoConfig
      * @return MiCoConfig The config
      */
     public MiCoConfig getConfig() {
         return config;
     }
 
     /**
      * Starts the MiCo
      */
     @SuppressWarnings(value="deprecation")
     public void start() {
         initializing = false;
         Bukkit.getServer().broadcastMessage("The mining-contest starts now!");
         VCAPI vc = (VCAPI) MiningContest.getAPI().getAPI("vc");
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 Location loc = new Location(Bukkit.getWorld("ContestWorld"), t.getBase().getPos().x * 16 + 3, 53, t.getBase().getPos().y * 16 + 3);
                 p.teleport(loc);
 
                 vc.storeInventory(p.getInventory(), "mico_" + p.getName());
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
 
     /**
      * Get the base of the specified team
      * @param team The team to which the base should belong
      * @return Base The base
      */
     public Base getBase(int team) {
         return teams.get(team).getBase();
     }
 
     /**
      * Get the team size of the MiningContest
      * @return int The team size
      */
     public int getTeamCount() {
         return teams.size();
     }
 
     /**
      * Checks whether a point is in a base
      * @param x The x-position
      * @param y The y-position
      * @param z The z-position
      * @return boolean true if in base, false if not 
      */
     private boolean isInBase(int x, int y, int z) {
         for (Team t : teams) {
             MapChunk mc = t.getBase();
             if (mc.isInside(x, y, z)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks whether a location is in a base
      * @param loc The location
      * @return boolean true if in base, false if not 
      */
     public boolean isInBase(Location loc) {
         return isInBase(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && loc.getWorld().getName().equalsIgnoreCase("ContestWorld");
     }
 
     /**
      * Checks whether a point is in a outpost
      * @param x The x-position
      * @param y The y-position
      * @param z The z-position
      * @return boolean true if in outpost, false if not 
      */
     public boolean isInOutpost(int x, int y, int z) {
         for (MapChunk mc : outposts) {
             if (mc.isInside(x, y, z)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks whether a location is in a outpost
      * @param loc The location
      * @return boolean true if in outpost, false if not 
      */
     public boolean isInOutpost(Location loc) {
         return isInOutpost(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && loc.getWorld().getName().equalsIgnoreCase("ContestWorld");
     }
 
     /**
      * Checks whether a player is inside his team area
      * @param player The player
      * @return boolean true if playeris inside, false if not
      */
     public boolean isInsideTeamArea(Player player) {
         Team t = getTeam(player);
         Location loc = player.getLocation();
         if (!loc.getWorld().getName().equalsIgnoreCase("ContestWorld")) {
             return false;
         }
         if (t.getBase().isInside(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
             return true;
         }
         for (Outpost out : outposts) {
             if (out==null || out.getTeam()==null) {
                 continue;
             }
             if (out.getTeam().equals(t)) {
                 if (out.isInside(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Get the MiCo lobby
      * @return Lobby The lobby
      */
     public Lobby getLobby() {
         return lobby;
     }
 
     /**
      * Get all outposts
      * @return ArrayList<Outpost> All outposts
      */
     public ArrayList<Outpost> getOutposts() {
         return outposts;
     }
 
     /**
      * Checks whether a block is destroyable
      * @param x The x-postion
      * @param y The y-position
      * @param z The z-position
      * @param world The world
      * @return boolean true if block is destroyable, false if not
      */
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
 
     /**
      * Checks whether a block is destroyable
      * @param loc The location
      * @return boolean true if block is destroyable, false if not
      */
     public boolean canDestroy(Location loc) {
         return canDestroy(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
     }
 
     /**
      * Checks whether a pint is in the map
      * @param x The x-postion
      * @param z The z-postion
      * @return boolean true if the point is inside, false if not
      */
     public static boolean isInMap(int x, int z) {
         return (x <= 10 && x >= -10) && (z <= 10 && z >= -10);
     }
 
     /**
      * Checks whether a chuck is a border chunk
      * @param x The x-postion
      * @param z The z-postion
      * @return boolean true if chunk is a border chunk, false if not
      */
     public static boolean isBorderChunk(int x, int z) {
         return (x == 10 || x == -10) || (z == 10 || z == -10);
     }
 
     /**
      * Get all teams
      * @return ArrayList<Team> All teams
      */
     public ArrayList<Team> getTeams() {
         return teams;
     }
 
     /**
      * Get the world where MiningContest is currently working in
      * @return World The world
      */
     public World getContestWorld() {
         return micoWorld;
     }
 
     /**
      * Get the next team
      * @return Team The next team
      */
     public Team getNextTeam() {
         Team t = teams.get(nextTeam);
         return t;
     }
 
     /**
      * Increases the nextTeam variable
      */
     public void nextTeam() {
         nextTeam++;
         if (nextTeam == teams.size()) {
             nextTeam = 0;
         }
     }
 
     /**
      * Get all Chunks WithDoorsAndSigns
      * @return ArrayList<WithDoorsAndSigns> The chunks
      */
     public ArrayList<WithDoorsAndSigns> getCheckedChunks() {
         return checkedChunks;
     }
 
     /**
      * Sets the next team
      * @param nextTeam The team number
      */
     public void setNextTeam(int nextTeam) {
         this.nextTeam = nextTeam;
     }
 
     /**
      * Checks whether a player has joined the MiCo
      * @param player The player
      * @return boolean true if player joined MiCo, false if not
      */
     public boolean isMiCoPlayer(Player player) {
         return started && players.contains(player);
     }
 
     /**
      * Get the team of a player
      * @param player The Player
      * @return Team The team
      */
     public Team getTeam(Player player) {
         for (Team t : teams) {
             if (t.hasMember(player)) {
                 return t;
             }
         }
         return null;
     }
 
     /**
      * Prints the points to the winning team
      */
     public void printPoints() {
         Team winningTeam = getWinningTeam();
         for (Team t : teams) {
             for (Player p : t.getMembers()) {
                 printPoints(p, t, winningTeam);
             }
         }
     }
 
     /**
      * Prints the points to the player
      * @param player The player
      */
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
 
     public MiCoClass getClass(Player p) {
         for (Team t : teams) {
             if (t.hasMember(p)) {
                 return t.getClass(p);
             }
         }
         return null;
     }
 }

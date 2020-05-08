 package com.bukkit.gemo.FalseBook.IC.ICs.worldedit;
 
 import com.bukkit.gemo.FalseBook.IC.FalseBookICCore;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseIC;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.bukkit.gemo.FalseBook.IC.ICs.Lever;
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.ChatUtils;
 import com.bukkit.gemo.utils.SignUtils;
 import com.bukkit.gemo.utils.UtilPermissions;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class ICTeleporter extends BaseIC {
 
     private int maxByPlayer = 5;
     private HashMap<String, TeleporterList> playerICList = new HashMap<String, TeleporterList>();
     private Random random = new Random();
 
     public ICTeleporter() {
         this.ICName = "TELEPORTER";
         this.ICNumber = "ic.teleporter";
         setICGroup(ICGroup.WORLDEDIT);
         this.chipState = new BaseChip(true, false, false, "Clock", "", "");
         this.chipState.setOutputs("Output = Input", "", "");
         this.chipState.setLines("Name of this station", "[ Name of the target station ]");
         this.ICDescription = "The MC1700 will teleport you to another MC1700 when the input (the \"clock\") goes from low to high and you are standing on the ic. The stationnames are stored for each player seperatly (means: 2 players can have the same stationnames).<br />It will automaticly search for the teleportplatform above the ic.<br/><br/><br /><b>LWC:</b><br/>If you secure the IC-Sign with LWC, only users with permissions can teleport with this IC (using /cmodify).<br /><br /><b>Extra Permissionnodes for this IC:</b><br />- falsebook.ic.mc1700.ignoreMaximum : players with this node will ignore the maximum number of teleporters (default: 5)<br/>- falsebook.ic.mc1700.destroyall : players with this node are allowed to destroy all teleporter-ICs (even from other players)";
     }
 
     public void onImport() {
         loadConfig();
         loadICs();
     }
 
     private int getListSize() {
         int count = 0;
         for (Entry<String, TeleporterList> entry : this.playerICList.entrySet()) {
             count += ((TeleporterList) entry.getValue()).getSize();
         }
         return count;
     }
 
     private int getListSizeForPlayer(String playerName) {
         if (this.playerICList.containsKey(playerName)) {
             return this.playerICList.get(playerName).getSize();
         }
         return 0;
     }
 
     private void loadConfig() {
         try {
             File file = new File("plugins/FalseBook/ICPlugins/configs");
             file.mkdir();
             file = new File(file, "MC1700.yml");
             if (!file.exists()) {
                 saveConfig();
                 return;
             }
 
             YamlConfiguration config = new YamlConfiguration();
             config.load(file);
             this.maxByPlayer = config.getInt("settings.maximumICsPerPlayer");
         } catch (Exception e) {
             FalseBookICCore.printInConsole("Config for Transport-IC [MC1700] could not be loaded!");
 
             e.printStackTrace();
             saveICs();
         }
     }
 
     private void saveConfig() {
         try {
             File file = new File("plugins/FalseBook/ICPlugins/configs");
             file.mkdir();
             file = new File(file, "MC1700.yml");
             if (file.exists()) {
                 file.delete();
             }
             YamlConfiguration config = new YamlConfiguration();
             config.set("settings.maximumICsPerPlayer", Integer.valueOf(this.maxByPlayer));
             config.save(file);
         } catch (Exception e) {
             FalseBookICCore.printInConsole("Config for Transport-IC [MC1700] could not be saved!");
             e.printStackTrace();
         }
     }
 
     private void loadICs() {
         try {
             File file = new File("plugins/FalseBook/ICPlugins/data");
             file.mkdir();
             file = new File(file, "MC1700.yml");
             if (!file.exists()) {
                 return;
             }
             YamlConfiguration config = new YamlConfiguration();
             config.load(file);
 
             int playercount = config.getInt("playercount");
             for (int i = 1; i <= playercount; i++) {
                 String playerName = config.getString("player." + i + ".name");
                 int iccount = config.getInt("player." + i + ".count");
 
                 for (int j = 1; j <= iccount; j++) {
                     Location location = BlockUtils.LocationFromString(config.getString("player." + i + ".ic." + j));
                     if (location == null) {
                         continue;
                     }
                     location.getChunk().load(true);
                     if (location.getBlock().getTypeId() != Material.WALL_SIGN.getId()) {
                         continue;
                     }
                     Sign sign = (Sign) location.getBlock().getState();
                     if (sign.getLine(1).length() < 1) {
                         continue;
                     }
                     addTeleporter(playerName, sign.getLine(1), location);
                 }
             }
 
             FalseBookICCore.printInConsole(getListSize() + " Transport-ICs [MC1700] loaded.");
         } catch (Exception e) {
             FalseBookICCore.printInConsole("Transport-ICs [MC1700] could not be loaded!");
             e.printStackTrace();
         }
     }
 
     private void saveICs() {
         try {
             File file = new File("plugins/FalseBook/ICPlugins/data");
             file.mkdir();
             file = new File(file, "MC1700.yml");
             if (file.exists()) {
                 file.delete();
             }
             YamlConfiguration config = new YamlConfiguration();
             int playercount = this.playerICList.size();
             config.set("playercount", Integer.valueOf(playercount));
             int i = 1;
             for (Entry<String, TeleporterList> entry : this.playerICList.entrySet()) {
                 int ICCount = ((TeleporterList) entry.getValue()).getSize();
                 if (ICCount < 1) {
                     continue;
                 }
                 config.set("player." + i + ".count", Integer.valueOf(ICCount));
                 config.set("player." + i + ".name", entry.getKey());
                 int j = 1;
                 for (Entry<String, String> tpEntry : ((TeleporterList) entry.getValue()).getAll().entrySet()) {
                     config.set("player." + i + ".ic." + j, tpEntry.getValue());
                     j++;
                 }
                 i++;
             }
             config.save(file);
         } catch (Exception e) {
             FalseBookICCore.printInConsole("Transport-ICs [MC1700] could not be saved!");
             e.printStackTrace();
         }
     }
 
     private String getStationOwner(Location location) {
         for (Entry<String, TeleporterList> entry : this.playerICList.entrySet()) {
             if (((TeleporterList) entry.getValue()).TeleporterExistsByLocation(BlockUtils.LocationToString(location))) {
                 return (String) entry.getKey();
             }
         }
         return null;
     }
 
     private Sign getTargetStation(String playername, String stationName) {
         if (!this.playerICList.containsKey(playername)) {
             return null;
         }
         if (!this.playerICList.get(playername).TeleporterExistsByName(stationName)) {
             return null;
         }
         Location location = this.playerICList.get(playername).getLocation(stationName);
         if (location.getBlock().getTypeId() != Material.WALL_SIGN.getId()) {
             return null;
         }
         return (Sign) location.getBlock().getState();
     }
 
     private void addTeleporter(String playerName, String stationName, Location location) {
         TeleporterList tList = this.playerICList.get(playerName);
         if (tList == null) {
             tList = new TeleporterList();
             this.playerICList.put(playerName, tList);
         }
         if (tList.TeleporterExistsByName(stationName)) {
             return;
         }
         tList.addTeleporter(stationName, location);
     }
 
     private void removeTeleporter(Location location) {
         boolean removed = false;
         String playerName = null;
         String locString = BlockUtils.LocationToString(location);
         for (Entry<String, TeleporterList> entry : this.playerICList.entrySet()) {
             if (((TeleporterList) entry.getValue()).TeleporterExistsByLocation(locString)) {
                 ((TeleporterList) entry.getValue()).removeTeleporterByLocation(locString);
                 removed = true;
                 playerName = (String) entry.getKey();
                 break;
             }
         }
         if (removed) {
             if (this.playerICList.get(playerName).getSize() < 1) {
                 this.playerICList.remove(playerName);
             }
             saveICs();
         }
     }
 
     private Location getTargetLocation(HashSet<Block> targetBlocks) {
         int randomPos = this.random.nextInt(targetBlocks.size());
         Iterator<Block> iterator = targetBlocks.iterator();
         int i = 0;
         while (iterator.hasNext()) {
             if (i == randomPos) {
                 return iterator.next().getRelative(BlockFace.UP).getLocation().clone();
             }
             iterator.next();
             i++;
         }
 
         return null;
     }
 
     private ArrayList<Player> getPlayersInPosition(HashSet<Block> blockList) {
         ArrayList<Player> playerList = new ArrayList<Player>();
         Player[] pList = Bukkit.getOnlinePlayers();
 
         for (Player player : pList) {
             Iterator<Block> iterator = blockList.iterator();
             while (iterator.hasNext()) {
                 Block thisBlock = iterator.next();
                 if (BlockUtils.LocationEquals(player.getLocation(), thisBlock.getRelative(BlockFace.UP).getLocation())) {
                     playerList.add(player);
                 }
             }
         }
         return playerList;
     }
 
     private HashSet<Block> getBaseBlocks(Sign signBlock) {
         Block topBlock = getICBlock(signBlock).getBlock().getRelative(BlockFace.UP);
         HashSet<Block> transportBlockList = new HashSet<Block>();
         ArrayList<Block> toCheckList = new ArrayList<Block>();
         HashSet<Block> checkedBlocksList = new HashSet<Block>();
         toCheckList.add(topBlock);
         if (topBlock.getTypeId() == Material.AIR.getId()) {
             return transportBlockList;
         }
         if ((BlockUtils.canPassThrough(topBlock.getRelative(BlockFace.UP).getTypeId())) && (BlockUtils.canPassThrough(topBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getTypeId()))) {
             transportBlockList.add(topBlock);
         }
         int MAXBLOCKS = 40;
         int MAXSEARCH = 1000;
         int k = 0;
         for (int j = 0; j < toCheckList.size(); j++) {
             Block block = toCheckList.get(j);
             ArrayList<Block> neighbours = BlockUtils.getDirectNeighbours(block, false);
             for (Block nBlock : neighbours) {
                 k++;
                 if (k >= 1000) {
                     break;
                 }
                 if (checkedBlocksList.contains(nBlock)) {
                     continue;
                 }
                 if ((nBlock.getTypeId() == topBlock.getTypeId()) && (nBlock.getData() == topBlock.getData()) && (BlockUtils.canPassThrough(nBlock.getRelative(BlockFace.UP).getTypeId())) && (BlockUtils.canPassThrough(nBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getTypeId()))) {
                     transportBlockList.add(nBlock);
                     toCheckList.add(nBlock);
                     if (transportBlockList.size() >= 40) {
                         break;
                     }
                 }
             }
             if (transportBlockList.size() >= 40) {
                 break;
             }
             if (k >= 1000) {
                 break;
             }
             checkedBlocksList.add(block);
         }
         return transportBlockList;
     }
 
     public void checkCreation(SignChangeEvent event) {
         if (event.getLine(1).length() < 1) {
             SignUtils.cancelSignCreation(event, "Please enter a name of this teleport-IC in line 3.");
             return;
         }
 
         if (getTargetStation(event.getPlayer().getName(), event.getLine(1)) != null) {
             SignUtils.cancelSignCreation(event, "A [MC1700] with this name already exists. Please choose another name.");
             return;
         }
 
         if ((this.maxByPlayer < 0) || (getListSizeForPlayer(event.getPlayer().getName()) < this.maxByPlayer) || (UtilPermissions.playerCanUseCommand(event.getPlayer(), "falsebook.ic.mc1700.ignoreMaximum"))) {
             addTeleporter(event.getPlayer().getName(), event.getLine(1), event.getBlock().getLocation());
             saveICs();
         } else {
             SignUtils.cancelSignCreation(event, "You have reached the maximumnumber of Teleport-ICs ( " + this.maxByPlayer + " ).");
             return;
         }
     }
 
     public void onBreakByExplosion(Sign signBlock) {
         removeTeleporter(signBlock.getBlock().getLocation());
     }
 
     public boolean onBreakByPlayer(Player player, Sign signBlock) {
         String owner = getStationOwner(signBlock.getBlock().getLocation());
         if (owner != null) {
             if ((owner.equalsIgnoreCase(player.getName())) || (UtilPermissions.playerCanUseCommand(player, "falsebook.ic.mc1700.destroyall"))) {
                 removeTeleporter(signBlock.getBlock().getLocation());
                 return true;
             }
             return false;
         }
 
         removeTeleporter(signBlock.getBlock().getLocation());
         return true;
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if ((currentInputs.isInputOneHigh()) && (previousInputs.isInputOneLow())) {
             if (signBlock.getLine(1).length() < 1) {
                 return;
             }
 
             if (signBlock.getLine(2).length() < 1) {
                 return;
             }
 
             String stationOwner = getStationOwner(signBlock.getBlock().getLocation());
             if (stationOwner == null) {
                 return;
             }
 
             Sign targetStation = getTargetStation(stationOwner, signBlock.getLine(2));
             if (targetStation == null) {
                 return;
             }
 
             HashSet<Block> transportBlockList = getBaseBlocks(signBlock);
             if (transportBlockList.size() < 1) {
                 return;
             }
 
             ArrayList<Player> playerList = getPlayersInPosition(transportBlockList);
             if (playerList.size() < 1) {
                 return;
             }
 
            if (!targetStation.getLine(1).equalsIgnoreCase("ic.teleporter")) {
                 return;
             }
             if (!targetStation.getLine(1).equalsIgnoreCase(signBlock.getLine(2))) {
                 return;
             }
             HashSet<Block> transportBlockListTarget = getBaseBlocks(targetStation);
             if (transportBlockListTarget.size() < 1) {
                 return;
             }
 
             for (Player player : playerList) {
                 if ((!player.isOnline()) && (player.isDead())) {
                     continue;
                 }
                 Location targetLoc = getTargetLocation(transportBlockListTarget);
                 if (targetLoc == null) {
                     ChatUtils.printError(player, "[FB-IC]", "[MC1700]: Something went wrong... ");
                 } else {
                     targetLoc.setYaw(player.getLocation().getYaw());
                     targetLoc.setPitch(player.getLocation().getPitch());
                     targetLoc.setX(targetLoc.getX() + 0.5D);
                     targetLoc.setZ(targetLoc.getZ() + 0.5D);
                     player.teleport(targetLoc);
                     ChatUtils.printInfo(player, "[FB-IC]", ChatColor.GREEN, "You were teleported to '" + signBlock.getLine(2) + "'!");
                 }
             }
             switchLever(Lever.BACK, signBlock, true);
         } else {
             switchLever(Lever.BACK, signBlock, false);
         }
     }
 
     public void onRightClick(Player player, Sign signBlock) {
         String owner = getStationOwner(signBlock.getBlock().getLocation());
         if (owner == null) {
             return;
         }
         ChatUtils.printInfo(player, "[FB-IC]", ChatColor.GRAY, "Creator: " + ChatColor.WHITE + owner);
         String secText = "";
         //if (LWCProtection.canAccessWithCModify(player, signBlock.getBlock()))
         secText = ChatColor.GREEN + "allowed" + ChatColor.GRAY;
         //else {
         // secText = ChatColor.RED + "not allowed" + ChatColor.GRAY;
         //}
         ChatUtils.printInfo(player, "[FB-IC]", ChatColor.GRAY, "You are " + secText + " to use this teleporter!");
     }
 }

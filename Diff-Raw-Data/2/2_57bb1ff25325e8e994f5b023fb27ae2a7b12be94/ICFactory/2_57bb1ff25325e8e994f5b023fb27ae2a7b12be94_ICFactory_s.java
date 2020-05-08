 package com.bukkit.gemo.FalseBook.IC;
 
 import com.bukkit.gemo.FalseBook.IC.ExecutionEvents.DelayedICExecutionEvent;
 import com.bukkit.gemo.FalseBook.IC.ExecutionEvents.ICExecutionEvent;
 import com.bukkit.gemo.FalseBook.IC.ExecutionEvents.ICRunningTask;
 import com.bukkit.gemo.FalseBook.IC.ICs.*;
 import com.bukkit.gemo.FalseBook.IC.ICs.selftriggered.ICSReceiver;
 import com.bukkit.gemo.FalseBook.IC.ICs.selftriggered.ICSTransmitter;
 import com.bukkit.gemo.FalseBook.IC.ICs.standard.ICReceiver;
 import com.bukkit.gemo.FalseBook.IC.Plugins.SelfmadeICLoader;
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.ChatUtils;
 import com.bukkit.gemo.utils.ICUtils;
 import com.bukkit.gemo.utils.MyEventStatistic;
 import com.bukkit.gemo.utils.SignUtils;
 import com.grover.mingebag.ic.DataTypeManager;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class ICFactory {
 
     private String API_VERSION = "1.1";
     private SelfmadeICLoader loader = null;
     private FalseBookICCore plugin = null;
     private ICRunningTask TASK = null;
     private PersistenceHandler persistence = null;
     private ArrayList<NotLoadedIC> failedICs = new ArrayList<NotLoadedIC>();
     private ConcurrentHashMap<String, SelftriggeredBaseIC> SensorList = new ConcurrentHashMap<String, SelftriggeredBaseIC>();
     private HashMap<String, BaseIC> registeredTICs = new HashMap<String, BaseIC>();
     private HashMap<String, SelftriggeredBaseIC> registeredSTICs = new HashMap<String, SelftriggeredBaseIC>();
     // Grover data type
     private DataTypeManager data_type;
     // end
     public MyEventStatistic statistic = new MyEventStatistic();
 
     public ICFactory(FalseBookICCore instance) {
         this.plugin = instance;
     }
 
     public void init(PersistenceHandler persistence) {
         registerICs();
         this.TASK = new ICRunningTask(this.plugin);
         this.persistence = persistence;
 
         // Grover data type
         data_type = new DataTypeManager();
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 
             public void run() {
                 data_type.clean();
             }
         }, 10000, 10000);
     }
 
     private void registerSingleTIC(BaseIC thisIC) {
         thisIC.initCore();
         this.registeredTICs.put(thisIC.getICNumber(), thisIC);
     }
 
     private void registerSingleSTIC(SelftriggeredBaseIC thisIC) {
         thisIC.initCore();
         this.registeredSTICs.put(thisIC.getICNumber(), thisIC);
     }
 
     private void registerICs() {
         ICUpgrade.addUpgrader("[MC0111]", new ICUpgraderMC("ics.receive"));
         registerSingleSTIC(new ICSReceiver());
         
         ICUpgrade.addUpgrader("[MC1110]", new ICUpgraderMC("ics.transmit"));
         registerSingleSTIC(new ICSTransmitter());
         
         ICUpgrade.addUpgrader("[MC1111]", new ICUpgraderMC("ic.receive"));
         registerSingleTIC(new ICReceiver());
     }
 
     private boolean checkICInstance(Object thisIC) {
         if (!(thisIC instanceof BaseIC)) {
             return false;
         }
         if (((BaseIC) thisIC).getICGroup() == null) {
             return false;
         }
         return (((BaseIC) thisIC).getICName() != null) && (((BaseIC) thisIC).getICNumber() != null);
     }
 
     public void importSelfmadeICs() {
         File folder = new File("plugins/FalseBook/ICPlugins");
         folder.mkdirs();
 
         this.loader = new SelfmadeICLoader();
         for (File file : folder.listFiles()) {
             if ((file.isFile()) && (file.getName().endsWith(".jar"))) {
                 ExternalICPackage thisPackage = this.loader.loadPlugin(file);
                 if (thisPackage != null) {
                     int ICCount = 0;
                     int FailedICCount = 0;
                     if (this.API_VERSION.equalsIgnoreCase(thisPackage.getAPI_VERSION())) {
                         for (Class entry : thisPackage.getICList()) {
                             try {
                                 Object newIC = entry.newInstance();
                                 if (!checkICInstance(newIC)) {
                                     FalseBookICCore.printInConsole("ERROR: Could not import '" + entry.getSimpleName() + "'! (Not initialized correctly.)");
                                     FailedICCount++;
                                 } else if ((newIC instanceof SelftriggeredBaseIC)) {
                                     SelftriggeredBaseIC thisIC = (SelftriggeredBaseIC) newIC;
                                     if (!this.registeredSTICs.containsKey(thisIC.getICNumber())) {
                                         if (thisPackage.isShowImportMessages()) {
                                             FalseBookICCore.printInConsole("imported STIC: " + thisIC.getICName() + " ( " + thisIC.getICNumber() + " )");
                                         }
                                         registerSingleSTIC(thisIC);
                                         thisIC.onImport();
                                         ICCount++;
                                     } else {
                                         FalseBookICCore.printInConsole("ERROR: Could not register selfmade " + thisIC.getICNumber() + "! STIC is already registered!");
                                         FailedICCount++;
                                     }
                                 } else if ((newIC instanceof BaseIC)) {
                                     BaseIC thisIC = (BaseIC) newIC;
                                     if (!this.registeredTICs.containsKey(thisIC.getICNumber())) {
                                         if (thisPackage.isShowImportMessages()) {
                                             FalseBookICCore.printInConsole("imported TIC: " + thisIC.getICName() + " ( " + thisIC.getICNumber() + " )");
                                         }
                                         registerSingleTIC(thisIC);
                                         thisIC.onImport();
                                         ICCount++;
                                     } else {
                                         FalseBookICCore.printInConsole("ERROR: Could not register selfmade " + thisIC.getICNumber() + "! IC is already registered!");
                                         FailedICCount++;
                                     }
                                 } else {
                                     FalseBookICCore.printInConsole("ERROR: Could not import '" + entry.getSimpleName() + "'!");
                                     FailedICCount++;
                                 }
                             } catch (Exception e) {
                                 e.printStackTrace();
                                 FalseBookICCore.printInConsole("ERROR: Could not import '" + entry.getSimpleName() + "'!");
                                 FailedICCount++;
                             }
                         }
                     } else {
                         FalseBookICCore.printInConsole("ERROR: API-Versions of  '" + file.getName() + "' does not match! Current API-Version of FalseBookIC is " + this.API_VERSION);
                     }
                     if (ICCount > 0) {
                         FalseBookICCore.printInConsole("'" + thisPackage.getClass().getSimpleName() + "' imported '" + ICCount + "' ICs.");
                     }
                     if (FailedICCount > 0) {
                         FalseBookICCore.printInConsole("ERROR: '" + thisPackage.getClass().getSimpleName() + "' could not import '" + FailedICCount + "' ICs.");
                     }
                 } else {
                     FalseBookICCore.printInConsole("ERROR: Could not import '" + file.getName() + "'!");
                 }
             }
         }
     }
 
     public boolean STICExists(Location loc) {
         for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
             SelftriggeredBaseIC IC = iterator.next();
             if (BlockUtils.LocationEquals(IC.getSignBlock().getBlock().getLocation(), loc)) {
                 IC = null;
                 return true;
             }
         }
         return false;
     }
 
     public BaseIC getIC(String line) {
         line = line.toLowerCase();
         BaseIC ic = this.registeredTICs.get(line);
         if (ic == null) {
             ic = this.registeredSTICs.get(line);
             if (ic == null) {
                ic = getICByAuto(line);
             }
         }
         return ic;
     }
 
     public BaseIC getICByName(String line) {
         line = line.toLowerCase().trim();
         if (line.startsWith("*")) {
             line = line.replace("*", "");
             for (SelftriggeredBaseIC entry : this.registeredSTICs.values()) {
                 if (line.equalsIgnoreCase(entry.getICName().trim().replace(" ", "").replace("_", "").replace("-", ""))) {
                     return entry;
                 }
             }
         } else if (line.startsWith("=")) {
             line = line.replace("=", "");
             for (BaseIC entry : this.registeredTICs.values()) {
                 if (line.equalsIgnoreCase(entry.getICName().trim().replace(" ", "").replace("_", "").replace("-", ""))) {
                     return entry;
                 }
             }
         }
 
         return null;
     }
 
     public BaseIC getICByAuto(String line) {
         if (line.length() < 5) {
             return null;
         }
 
         line = line.toLowerCase().trim();
         for (BaseIC entry : this.registeredTICs.values()) {
             if (entry.getICNumber().length() >= line.length()) {
                 if (entry.getICNumber().substring(0, line.length()).equals(line)) {
                     return entry;
                 }
             }
         }
 
         return null;
     }
 
     public SelftriggeredBaseIC getSTIC(String line) {
         return this.registeredSTICs.get(line.toLowerCase());
     }
 
     public ArrayList<NotLoadedIC> getFailedICs() {
         return (ArrayList<NotLoadedIC>) this.failedICs.clone();
     }
 
     public boolean isBlockBreakable(List<Block> blockList) {
         for (int i = 0; i < blockList.size(); i++) {
             try {
                 Block block = blockList.get(i);
                 if (block.getType().equals(Material.WALL_SIGN)) {
                     Sign signBlock = (Sign) block.getState();
                     BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
 
                     if (thisIC != null) {
                         return false;
                     }
                 } else {
                     Block b = null;
                     byte signData = -1;
                     ArrayList<Block> bList = BlockUtils.getDirectNeighbours(block, true);
                     for (int j = 0; j < bList.size(); j++) {
                         b = bList.get(j);
 
                         if (b.getType().equals(Material.WALL_SIGN)) {
                             Sign signBlock = (Sign) b.getState();
                             signData = signBlock.getRawData();
                             if (((signData != 2) || (j != 3)) && ((signData != 4) || (j != 1)) && ((signData != 5) || (j != 0)) && ((signData != 3) || (j != 2))) {
                                 continue;
                             }
                             BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
                             if (thisIC != null) {
                                 return false;
                             }
                         }
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return true;
     }
 
     public boolean isBlockBreakable(Block block) {
         try {
             if (block.getType().equals(Material.WALL_SIGN)) {
                 Sign signBlock = (Sign) block.getState();
                 BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
 
                 if (thisIC != null) {
                     return false;
                 }
             } else {
                 Block b = null;
                 byte signData = -1;
                 ArrayList<Block> bList = BlockUtils.getDirectNeighbours(block, true);
                 for (int j = 0; j < bList.size(); j++) {
                     b = bList.get(j);
 
                     if (b.getType().equals(Material.WALL_SIGN)) {
                         Sign signBlock = (Sign) b.getState();
                         signData = signBlock.getRawData();
                         if (((signData != 2) || (j != 3)) && ((signData != 4) || (j != 1)) && ((signData != 5) || (j != 0)) && ((signData != 3) || (j != 2))) {
                             continue;
                         }
                         BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
                         if (thisIC != null) {
                             return false;
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return true;
     }
 
     public void addSelftriggeredIC(Location location, SelftriggeredBaseIC IC) {
         this.SensorList.put(location.toString(), IC);
     }
 
     public void removeFailedIC(NotLoadedIC IC) {
         this.failedICs.remove(IC);
     }
 
     public void addFailedIC(NotLoadedIC IC) {
         this.failedICs.add(IC);
     }
 
     public int getFailedICsSize() {
         return this.failedICs.size();
     }
 
     public void handleExplodeEvent(EntityExplodeEvent event) {
         if (!isBlockBreakable(event.blockList())) {
             if (!FalseBookICCore.getInstance().isAllowExplosionForICs()) {
                 event.setYield(0.0F);
                 event.setCancelled(true);
             } else {
                 for (Block block : event.blockList()) {
                     if (block.getType().equals(Material.WALL_SIGN)) {
                         Sign signBlock = (Sign) block.getState();
                         BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
 
                         if (thisIC == null) {
                             continue;
                         }
                         SelftriggeredBaseIC IC;
                         if ((thisIC instanceof SelftriggeredBaseIC)) {
                             Location loc = block.getLocation();
 
                             for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
                                 IC = iterator.next();
                                 if (BlockUtils.LocationEquals(loc, IC.getSignBlock().getBlock().getLocation())) {
                                     this.SensorList.remove(IC.getSignBlock().getBlock().getLocation().toString());
                                     this.persistence.removeSelftriggeredIC(IC.getSignBlock().getBlock().getLocation());
                                     IC.onBreakByExplosion(IC.getSignBlock());
                                 }
                             }
 
                             IC = null;
                         } else {
                             thisIC.onBreakByExplosion(signBlock);
                         }
                         signBlock = null;
                         thisIC = null;
                     } else {
                         Block b = null;
                         byte signData = -1;
                         ArrayList<Block> bList = BlockUtils.getDirectNeighbours(block, false);
                         for (int i = 0; i < bList.size(); i++) {
                             b = bList.get(i);
 
                             if (b.getType().equals(Material.WALL_SIGN)) {
                                 Sign signBlock = (Sign) b.getState();
                                 signData = signBlock.getRawData();
                                 if (((signData != 2) || (i != 3)) && ((signData != 4) || (i != 1)) && ((signData != 5) || (i != 0)) && ((signData != 3) || (i != 2))) {
                                     continue;
                                 }
                                 BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
                                 if (thisIC == null) {
                                     continue;
                                 }
                                 SelftriggeredBaseIC IC;
                                 if ((thisIC instanceof SelftriggeredBaseIC)) {
                                     Location loc2 = b.getLocation();
 
                                     for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
                                         IC = iterator.next();
                                         if (!BlockUtils.LocationEquals(loc2, IC.getSignBlock().getBlock().getLocation())) {
                                             continue;
                                         }
                                         this.SensorList.remove(IC.getSignBlock().getBlock().getLocation().toString());
                                         this.persistence.removeSelftriggeredIC(IC.getSignBlock().getBlock().getLocation());
                                         IC.onBreakByExplosion(IC.getSignBlock());
                                     }
 
                                     IC = null;
                                 } else {
                                     thisIC.onBreakByExplosion(signBlock);
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public void handleEntityChangeBlock(EntityChangeBlockEvent event) {
         if (!isBlockBreakable(event.getBlock())) {
             event.setCancelled(true);
         }
     }
 
     public void handlePistonExtend(BlockPistonExtendEvent event) {
         if (!isBlockBreakable(event.getBlocks())) {
             event.setCancelled(true);
         }
     }
 
     public void handlePistonRetract(BlockPistonRetractEvent event) {
         if (!event.isSticky()) {
             return;
         }
         try {
             if (!isBlockBreakable(event.getRetractLocation().getBlock())) {
                 event.setCancelled(true);
             }
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
     }
 
     public void handleBlockBreak(BlockBreakEvent event) {
         Block block = event.getBlock();
         if (block.getType().equals(Material.WALL_SIGN)) {
             Sign signBlock = (Sign) block.getState();
             BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
 
             if (thisIC == null) {
                 return;
             }
 
             if (!thisIC.hasPermission(event.getPlayer())) {
                 event.setCancelled(true);
                 ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                 thisIC = null;
                 signBlock = null;
                 return;
             }
             SelftriggeredBaseIC IC;
             if ((thisIC instanceof SelftriggeredBaseIC)) {
                 Location loc = block.getLocation();
 
                 for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
                     IC = iterator.next();
                     if (BlockUtils.LocationEquals(loc, IC.getSignBlock().getBlock().getLocation())) {
                         if (IC.onBreakByPlayer(event.getPlayer(), IC.getSignBlock())) {
                             this.SensorList.remove(IC.getSignBlock().getBlock().getLocation().toString());
                             this.persistence.removeSelftriggeredIC(IC.getSignBlock().getBlock().getLocation());
                             ChatUtils.printSuccess(event.getPlayer(), "[FB-IC]", IC.getICNumber() + " removed.");
                             break;
                         }
                         event.setCancelled(true);
                         ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                         return;
                     }
                 }
 
                 IC = null;
             } else {
                 if (!thisIC.onBreakByPlayer(event.getPlayer(), signBlock)) {
                     event.setCancelled(true);
                     ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                     return;
                 }
                 ChatUtils.printSuccess(event.getPlayer(), "[FB-IC]", thisIC.getICName() + " removed.");
             }
 
             signBlock = null;
             thisIC = null;
         } else {
             Block b = null;
             byte signData = -1;
             ArrayList<Block> bList = BlockUtils.getDirectNeighbours(block, false);
             for (int i = 0; i < bList.size(); i++) {
                 b = bList.get(i);
 
                 if (b.getType().equals(Material.WALL_SIGN)) {
                     Sign signBlock = (Sign) b.getState();
                     signData = signBlock.getRawData();
                     if (((signData != 2) || (i != 3)) && ((signData != 4) || (i != 1)) && ((signData != 5) || (i != 0)) && ((signData != 3) || (i != 2))) {
                         continue;
                     }
                     BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
                     if (thisIC == null) {
                         continue;
                     }
 
                     if (!thisIC.hasPermission(event.getPlayer())) {
                         event.setCancelled(true);
                         ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                         thisIC = null;
                         signBlock = null;
                         return;
                     }
                     SelftriggeredBaseIC IC;
                     if ((thisIC instanceof SelftriggeredBaseIC)) {
                         Location loc2 = b.getLocation();
 
                         for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
                             IC = iterator.next();
                             if (!BlockUtils.LocationEquals(loc2, IC.getSignBlock().getBlock().getLocation())) {
                                 continue;
                             }
                             if (IC.onBreakByPlayer(event.getPlayer(), IC.getSignBlock())) {
                                 this.SensorList.remove(IC.getSignBlock().getBlock().getLocation().toString());
                                 this.persistence.removeSelftriggeredIC(IC.getSignBlock().getBlock().getLocation());
                                 ChatUtils.printSuccess(event.getPlayer(), "[FB-IC]", IC.getICNumber() + " removed.");
                             } else {
                                 event.setCancelled(true);
                                 ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                                 return;
                             }
                         }
 
                         IC = null;
                     } else {
                         if (!thisIC.onBreakByPlayer(event.getPlayer(), signBlock)) {
                             event.setCancelled(true);
                             ChatUtils.printError(event.getPlayer(), "[FB-IC]", "You are not allowed to destroy this IC!");
                             return;
                         }
                         ChatUtils.printSuccess(event.getPlayer(), "[FB-IC]", thisIC.getICNumber() + " removed.");
                     }
                 }
             }
         }
     }
 
     public void handleRedstoneEvent(Block block, BlockRedstoneEvent event, int delayTicks, int searchTry) {
         if (block.getType().equals(Material.WALL_SIGN)) {
             Sign signBlock = (Sign) block.getState();
             if (signBlock == null) {
                 return;
             }
             
             if (signBlock.getLine(0) == null) {
                 return;
             }
             
             BaseIC thisIC = getIC(signBlock.getLine(0).toLowerCase());
             if (thisIC == null) {
                 boolean upgraded;
 
                 // Loop over all upgrades.
                 do {
                     upgraded = false;
                     if(ICUpgrade.needsUpgrade(signBlock.getLine(1))) {
                         ICUpgrader u = ICUpgrade.getUpgrader(signBlock.getLine(1));
                         if(u.preCheckUpgrade(signBlock)) {
                             u.upgrade(signBlock);
                             upgraded = true;
                         }
                     }
                     else if(ICUpgrade.needsUpgrade(signBlock.getLine(0))) {
                         ICUpgrader u = ICUpgrade.getUpgrader(signBlock.getLine(0));
                         if(u.preCheckUpgrade(signBlock)) {
                             u.upgrade(signBlock);
                             upgraded = true;
                         }
                     }
                     if(upgraded) {
                         String newName = signBlock.getLine(0).toLowerCase();
                         thisIC = getIC(newName);
                     }
                 } while(upgraded && thisIC == null);
 
                 if(thisIC == null) {
                     return;
                 }
             }
 
             if ((thisIC instanceof ICSTransmitter)) {
                 for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
                     SelftriggeredBaseIC IC = iterator.next();
                     if ((!(IC instanceof ICSTransmitter))
                             || (!BlockUtils.LocationEquals(IC.getSignBlock().getBlock().getLocation(), block.getLocation()))) {
                         continue;
                     }
                     ((ICSTransmitter) IC).setStatus(event.getNewCurrent() > 0);
                     IC = null;
                     return;
                 }
 
             }
 
             boolean[] currentInputs = new boolean[3];
             currentInputs[0] = ICUtils.isInputHigh(signBlock, 1);
             currentInputs[1] = ICUtils.isInputHigh(signBlock, 2);
             currentInputs[2] = ICUtils.isInputHigh(signBlock, 3);
 
             InputState currentState = new InputState(signBlock);
 
             synchronized (this.TASK.getQueuedICs()) {
                 if (delayTicks <= 1) {
                     ICExecutionEvent thisEvent = new ICExecutionEvent(thisIC, signBlock, currentState);
                     if (this.TASK.getQueuedICs().size() + 1 < this.TASK.getMaxExecutions()) {
                         ArrayList<Location> positions = ICUtils.getBlockPositions(signBlock);
                         if ((thisIC.getChipState().hasInput(event.getBlock(), positions))
                                 && (!this.TASK.getQueuedICsPos().containsKey(signBlock.getBlock().getLocation().toString()))) {
                             this.TASK.getQueuedICs().add(thisEvent);
                             this.TASK.getQueuedICsPos().put(signBlock.getBlock().getLocation().toString(), Integer.valueOf(0));
                         }
                     }
                 } else {
                     DelayedICExecutionEvent thisEvent = new DelayedICExecutionEvent(thisIC, signBlock, currentState);
                     this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, thisEvent, delayTicks + searchTry);
                 }
 
             }
 
             if ((this.TASK.getExeTaskID() == -1) && (this.TASK.getQueuedICs().size() > 0)) {
                 this.TASK.setExeTaskID(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this.TASK, 1L));
             }
         } else if ((block.getType().equals(Material.DIODE_BLOCK_OFF)) || (block.getType().equals(Material.DIODE_BLOCK_ON))) {
             if (searchTry > 10) {
                 return;
             }
             int delay = delayTicks + (int) Math.floor(block.getData() / 4) + 1;
             BlockRedstoneEvent rEvent = new BlockRedstoneEvent(block, event.getOldCurrent(), event.getNewCurrent());
             int data = block.getData() % 4;
             Block tBlock = block.getRelative(0, 0, 1);
 
             if (data != 2) {
                 if (data == 0) {
                     tBlock = block.getRelative(0, 0, -1);
                 } else if (data == 1) {
                     tBlock = block.getRelative(1, 0, 0);
                 } else if (data == 3) {
                     tBlock = block.getRelative(-1, 0, 0);
                 }
             }
             if ((tBlock.getTypeId() == Material.DIODE_BLOCK_ON.getId()) || (tBlock.getTypeId() == Material.DIODE_BLOCK_OFF.getId())) {
                 if (tBlock.getData() % 4 == data) {
                     handleRedstoneEvent(tBlock, rEvent, delay, searchTry + 1);
                 }
             } else {
                 handleRedstoneEvent(tBlock, rEvent, delay, searchTry + 1);
             }
        } else if(searchTry <= 10) {
             handleRedstoneEvent(block.getRelative(1, 0, 0), event, delayTicks, Integer.MAX_VALUE);
             handleRedstoneEvent(block.getRelative(-1, 0, 0), event, delayTicks, Integer.MAX_VALUE);
             handleRedstoneEvent(block.getRelative(0, 0, 1), event, delayTicks, Integer.MAX_VALUE);
             handleRedstoneEvent(block.getRelative(0, 0, -1), event, delayTicks, Integer.MAX_VALUE);
         }
     }
 
     public void handleSignChange(SignChangeEvent event) {
         Player player = event.getPlayer();
 
         BaseIC thisIC = getIC(event.getLine(0).toLowerCase());
 
         if (thisIC == null) {
             boolean upgraded = false;
             
             if(ICUpgrade.needsUpgrade(event.getLine(1))) {
                 upgraded = true;
             }
             else if(ICUpgrade.needsUpgrade(event.getLine(0))) {
                 upgraded = true;
             }
 
             if(upgraded == true) {
                 event.setCancelled(true);
                 SignUtils.cancelSignCreation(event, "IC-Signs must be named using current names.");
             }
             
             return;
         }
 
         event.setLine(0, thisIC.getICNumber());
 
         if (!event.getBlock().getType().equals(Material.WALL_SIGN)) {
             event.setCancelled(true);
             SignUtils.cancelSignCreation(event, "IC-Signs must be built on a wall.");
             return;
         }
 
         if (!thisIC.hasPermission(player)) {
             event.setCancelled(true);
             SignUtils.cancelSignCreation(event, "You are not allowed to build a " + thisIC.getICName());
             return;
         }
 
 
         if (!(thisIC instanceof SelftriggeredBaseIC)) {
             thisIC.checkCreation(event);
             if (!event.isCancelled()) {
                 thisIC.initCore();
                 thisIC.notifyCreationSuccess(player);
             }
         } else {
             SelftriggeredBaseIC newIC = null;
 
             SelftriggeredBaseIC nIC = this.registeredSTICs.get(event.getLine(0).toLowerCase());
             boolean startUpComplete = false;
             if (nIC != null) {
                 try {
                     newIC = nIC.getClass().newInstance();
                     newIC.initIC(this.plugin, event.getBlock().getLocation());
                     newIC.checkCreation(event);
                     startUpComplete = newIC.onLoad(event.getLines());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else {
                 newIC = null;
             }
 
             if ((!event.isCancelled()) && (newIC != null) && (startUpComplete)) {
                 thisIC.initCore();
                 this.persistence.addSelftriggeredICToDB(0, event.getBlock().getLocation());
                 this.SensorList.put(event.getBlock().getLocation().toString(), newIC);
                 thisIC.notifyCreationSuccess(player);
             }
         }
         thisIC = null;
     }
 
     public void handleLeftClick(PlayerInteractEvent event) {
         if (event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
             Sign sign = (Sign) event.getClickedBlock().getState();
             BaseIC IC = getIC(sign.getLine(0).toLowerCase());
             if (IC == null) {
                 return;
             }
             if ((IC instanceof SelftriggeredBaseIC)) {
                 if (STICExists(event.getClickedBlock().getLocation())) {
                     IC.onLeftClick(event.getPlayer(), sign);
                     return;
                 }
                 ChatUtils.printInfo(event.getPlayer(), "[FB-IC]", ChatColor.GRAY, "This selftriggered IC is not initialized. Rightlick to initialize it.");
                 return;
             }
 
             IC.onLeftClick(event.getPlayer(), sign);
 
             IC = null;
         }
     }
 
     public void handleRightClick(PlayerInteractEvent event) {
         if (event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
             Sign sign = (Sign) event.getClickedBlock().getState();
             BaseIC IC = getIC(sign.getLine(0).toLowerCase());
             if (IC == null) {
                 return;
             }
             if ((IC instanceof SelftriggeredBaseIC)) {
                 SelftriggeredBaseIC thisIC = (SelftriggeredBaseIC) IC;
 
                 if (STICExists(event.getClickedBlock().getLocation())) {
                     IC.onRightClick(event.getPlayer(), sign);
                     return;
                 }
 
                 event.setUseInteractedBlock(Result.DENY);
                 event.setUseItemInHand(Result.DENY);
                 event.setCancelled(true);
 
                 boolean startUpComplete = false;
                 boolean initComplete = false;
                 try {
                     thisIC = thisIC.getClass().newInstance();
                     initComplete = thisIC.initIC(this.plugin, event.getClickedBlock().getLocation());
                     startUpComplete = thisIC.onLoad(sign.getLines());
                     if ((startUpComplete) && (initComplete)) {
                         thisIC.initCore();
                         this.persistence.addSelftriggeredICToDB(0, event.getClickedBlock().getLocation());
                         this.SensorList.put(event.getClickedBlock().getLocation().toString(), thisIC);
                         thisIC.notifyCreationSuccess(event.getPlayer());
                         return;
                     }
                     ChatUtils.printError(event.getPlayer(), "[FB-IC]", "Could not recreate the IC.");
                 } catch (Exception e) {
                     e.printStackTrace();
                     ChatUtils.printError(event.getPlayer(), "[FB-IC]", "Error while recreating " + thisIC.getICNumber() + "!");
                 }
             } else {
                 IC.onRightClick(event.getPlayer(), sign);
             }
             IC = null;
         }
     }
 
     public HashMap<String, BaseIC> getRegisteredTICs() {
         HashMap<String, BaseIC> result = new HashMap<String, BaseIC>();
         result.putAll(this.registeredTICs);
         return result;
     }
 
     public int getSensorListSize() {
         return this.SensorList.size();
     }
 
     public int getRegisteredTICsSize() {
         return this.registeredTICs.size();
     }
 
     public int getRegisteredSTICsSize() {
         return this.registeredSTICs.size();
     }
 
     public HashMap<String, SelftriggeredBaseIC> getRegisteredSTICs() {
         HashMap<String, SelftriggeredBaseIC> result = new HashMap<String, SelftriggeredBaseIC>();
         result.putAll(this.registeredSTICs);
         return result;
     }
 
     public Set<Entry<String, SelftriggeredBaseIC>> getRegisteredSTICsEntrys() {
         return this.registeredSTICs.entrySet();
     }
 
     public Iterator<SelftriggeredBaseIC> getSensorListIterator() {
         return this.SensorList.values().iterator();
     }
 
     public void executeSTICs() {
         long start = System.nanoTime();
 
         for (Iterator<SelftriggeredBaseIC> iterator = this.SensorList.values().iterator(); iterator.hasNext();) {
             SelftriggeredBaseIC IC = iterator.next();
 
             if ((IC.getICNumber().equalsIgnoreCase("ics.transmit")) || (IC.getICNumber().equalsIgnoreCase("ics.receive"))) {
                 continue;
             }
             if (IC.validateIC()) {
                 IC.Execute();
             }
         }
         this.statistic.update(System.nanoTime() - start);
     }
 
     public void clearSensorList() {
         this.SensorList = new ConcurrentHashMap<String, SelftriggeredBaseIC>();
     }
 
     public void clearFailedICs() {
         this.failedICs = new ArrayList<NotLoadedIC>();
     }
 
     // Grover data type
     public DataTypeManager getDataTypeManager() {
         return this.data_type;
     }
 }

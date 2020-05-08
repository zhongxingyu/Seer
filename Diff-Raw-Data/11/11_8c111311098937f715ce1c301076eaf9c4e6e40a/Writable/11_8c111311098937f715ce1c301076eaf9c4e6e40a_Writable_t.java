 
 package me.exphc.Writable;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.*;
 
 enum WritingState {
     NOT_WRITING,        // initial state, timed out, or finished writing
     CLICKED_PAPER,      // onPlayerInteract(), when right-click paper
     PLACED_SIGN,        // onBlockPlace(), when placed temporary sign
                         // onSignChange(), after wrote sign
 }
 
 class WritableSignPlaceTimeoutTask implements Runnable {
     static public ConcurrentHashMap<Player, Integer> taskIDs = new ConcurrentHashMap<Player, Integer>();
 
     static Logger log = Logger.getLogger("Minecraft");
     Player player;
 
     public WritableSignPlaceTimeoutTask(Player p) {
         player = p;
     }
 
     public void run() {
         if (Writable.getWritingState(player) != WritingState.PLACED_SIGN) {
             log.info("did not place sign in time");
             
             WritablePlayerListener.restoreSavedItem(player);
             Writable.setWritingState(player, WritingState.NOT_WRITING);
         }
 
         WritableSignPlaceTimeoutTask.taskIDs.remove(player);
     }
 }
 
 class WritablePlayerListener extends PlayerListener {
     Logger log = Logger.getLogger("Minecraft");
     Writable plugin;
 
     static private ConcurrentHashMap<Player, ItemStack> savedItemStack = new ConcurrentHashMap<Player, ItemStack>();
     static private ConcurrentHashMap<Player, Integer> savedItemSlot = new ConcurrentHashMap<Player, Integer>();
     static public ConcurrentHashMap<Player, ChatColor> currentColor = new ConcurrentHashMap<Player, ChatColor>();
 
     public WritablePlayerListener(Writable pl) {
         plugin = pl;
     }
 
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock();
         ItemStack item = event.getItem();
         Player player = event.getPlayer();
         Action action = event.getAction();
 
         if (item != null && item.getType() == Material.PAPER && action == Action.RIGHT_CLICK_BLOCK) {
             if (Writable.getWritingState(player) != WritingState.NOT_WRITING) {
                 player.sendMessage("You are already writing");
                 // TODO: stop other writing, restore (like timeout), cancel task? 
                 return;
             }
 
             // TODO: prevent writing on >1 stacks? or try to shuffle around?
 
             // Check block to ensure is realistically hard surface to write on (stone, not gravel or sand, etc.)
             if (!plugin.isWritingSurface(block)) {
                 player.sendMessage("You need a hard surface to write on, not "+block.getType().toString().toLowerCase());
                 return;
             }
 
             // Check if have writing implement and ink
             int implementSlot = Writable.findImplementSlot(player);
             if (implementSlot == -1) {
                 player.sendMessage("To write, you must have a writing implement in your hotbar");
                 return;
             }
 
             int inkSlot = Writable.findInkSlot(player, implementSlot);
             if (inkSlot == -1) {
                 ItemStack implementItem = player.getInventory().getItem(implementSlot);
 
                 log.info("slot"+implementSlot);
                 player.sendMessage("To write, place ink next to the " + implementItem.getType().toString().toLowerCase() + " in your inventory");
                 return;
             }
             ItemStack inkItem = player.getInventory().getItem(inkSlot);
             ChatColor color = Writable.getChatColor(inkItem);
 
            // Optionally use up ink
             if (plugin.isInkConsumable()) {
                 int amount = inkItem.getAmount();
 
                 if (amount > 1) {
                     inkItem.setAmount(amount - 1);
                 } else {
                    player.getInventory().clear(inkSlot);
                 }
                // Docs say deprecated and should not be relied on, but, client won't update without it
                player.updateInventory();
             }
 
             
             // If blank, assign new ID
             short id = item.getDurability();
             if (id == 0) {
                 id = plugin.getNewPaperID();
                 item.setDurability(id);
             } else {
                 if (Writable.isPaperFull(id)) {
                     player.sendMessage("Sorry, this paper is full");
                     return;
                 }
             }
             
             player.sendMessage(color+"Right-click to write on paper #"+id);
 
 
             // Save off old item in hand to restore, and ink color to use
             savedItemStack.put(player, item);
             savedItemSlot.put(player, player.getInventory().getHeldItemSlot());
             currentColor.put(player, color); 
 
             // Quickly change to sign, so double right-click paper = place sign to write on
             player.setItemInHand(new ItemStack(Material.SIGN, 1));
             // TODO: if have >1, save off old paper?
 
             Writable.setWritingState(player, WritingState.CLICKED_PAPER);
             
             // Timeout to NOT_WRITING if our sign isn't used in a sufficient time
             WritableSignPlaceTimeoutTask task = new WritableSignPlaceTimeoutTask(player);
             int taskID = Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, task, plugin.getConfig().getLong("signTimeout", 2*20));
 
             // Save task to cancel if did in fact make it to PLACED_SIGN in time
             WritableSignPlaceTimeoutTask.taskIDs.put(player, taskID);
         }
     }
 
     // Restore previous item held by player, before started writing (do not use setItemInHand())
     // Returns items restored
     static public ItemStack restoreSavedItem(Player player) {
         ItemStack items = savedItemStack.get(player);
         int slot = savedItemSlot.get(player);
 
         player.getInventory().setItem(slot, items);
 
         savedItemStack.remove(player);
         savedItemSlot.remove(player);
         currentColor.remove(player);
 
         return items;
     }
 
     static public void readPaperToPlayer(Player player, int id) {
         if (id == 0) {
             player.sendMessage("Double right-click to write on this blank paper");
             return;
         }
 
         ArrayList<String> lines = Writable.readPaper(id);
         
         if (lines.size() == 0) {
             player.sendMessage("Paper #"+id+" is blank");
             return;
         }
 
         player.sendMessage("Reading paper #"+id+":");
         for (String line: lines) {
             player.sendMessage(" "+line);
         }
 
         // Chat shows 10 recent lines normally
         // can press 't' to show 20 recent lines
         if (lines.size() > 10) {
             player.sendMessage("Press 't' to show the full text of paper #"+id);
         }
 
         // Text on paper is meant to only fit in one chat screen, so no pagination needed
     }
 
 
     // When change to in inventory slot, read back
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItem(event.getNewSlot());
 
         if (item != null && item.getType() == Material.PAPER) {
             int id = item.getDurability();
 
             // TODO: only if not zero
             readPaperToPlayer(player, id);
         }
     }
 
     // If pickup a paper with writing on it, let know
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         ItemStack item = event.getItem().getItemStack();
 
         if (item != null && item.getType() == Material.PAPER) {
             if (item.getDurability() != 0) {
                 event.getPlayer().sendMessage("You picked up paper, mysteriously scribbled");
             } 
         }
     }
 
 }
 
 class WritableBlockListener extends BlockListener {
     Logger log = Logger.getLogger("Minecraft");
     Writable plugin;
 
     public WritableBlockListener(Writable pl) {
         plugin = pl;
     }
 
     public void onBlockPlace(BlockPlaceEvent event) {
         Block block = event.getBlock();
         Player player = event.getPlayer();
 
         if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
             WritingState state = Writable.getWritingState(player);
 
             // Did they get this sign from right-clicking paper?
             if (state == WritingState.CLICKED_PAPER) {
                 // We made it, stop timeout task (so won't revert to NOT_WRITING and take back sign)
                 int taskID = WritableSignPlaceTimeoutTask.taskIDs.get(player);
                 WritableSignPlaceTimeoutTask.taskIDs.remove(player);
                 Bukkit.getScheduler().cancelTask(taskID);
 
                 Writable.setWritingState(player, WritingState.PLACED_SIGN);
                 // TODO: store paper ID
             } else {
                 log.info("Place non-paper sign");
             }
         }
     }
 
     public void onSignChange(SignChangeEvent event) {
         Block block = event.getBlock();
         Player player = event.getPlayer();
         String[] lines = event.getLines();
 
         WritingState state = Writable.getWritingState(player);
         if (state != WritingState.PLACED_SIGN) {    
             log.info("Changing sign not from paper");
             return;
         }
 
         // This sign text came from a sign from clicking paper
 
         // Destroy sign
         block.setType(Material.AIR);
 
         ChatColor color = WritablePlayerListener.currentColor.get(player);
 
         // Restore previous item 
         ItemStack paperItem = WritablePlayerListener.restoreSavedItem(player);
 
         // Write
         int id = paperItem.getDurability();
         plugin.writePaper(id, lines, color);
 
         // Finish up
         Writable.setWritingState(player, WritingState.NOT_WRITING);
         WritablePlayerListener.readPaperToPlayer(player, id);
     }
 }
 
 // Like Material, but also has MaterialData
 // Like ItemStack, but different data is different!
 class MaterialWithData implements Comparable {
     int material;
     byte data;
     
     public MaterialWithData(Material m, MaterialData d) {
         material = m.getId();
         data = d.getData();
     }
 
     public MaterialWithData(Material m) {
         material = m.getId();
         data = 0;
     }
 
     public int hashCode() {
         return material * data;
     }
 
     public boolean equals(Object rhs) {
         return compareTo(rhs) == 0;
     }
 
     public int compareTo(Object obj) {
         int ret;
 
         if (!(obj instanceof MaterialWithData)) {
             return -1;
         }
         MaterialWithData rhs = (MaterialWithData)obj;
 
         ret = material - rhs.material;
         if (ret != 0) {
             return ret;
         }
 
         return data - rhs.data;
     }
 
     public String toString() {
         return "MaterialWithData("+material+","+data+")";
     }
 }
 
 
 public class Writable extends JavaPlugin {
     static Logger log = Logger.getLogger("Minecraft");
     WritablePlayerListener playerListener;
     WritableBlockListener blockListener;
 
     static private ConcurrentHashMap<Player, WritingState> writingState;
     static private ConcurrentHashMap<Integer, ArrayList<String>> paperTexts;    // TODO: paper class?
 
     static private List<Material> writingImplementMaterials;
     static private List<Material> writingSurfaceMaterials;
     static private HashMap<MaterialWithData,ChatColor> inkColors;
 
     static private short nextPaperID;
     static private int paperLengthLineCap;
     static private boolean consumeInk;
 
 
     public void onEnable() {
         writingState = new ConcurrentHashMap<Player, WritingState>();
 
         loadConfig();
 
         playerListener = new WritablePlayerListener(this);
         blockListener = new WritableBlockListener(this);
 
         Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, org.bukkit.event.Event.Priority.Normal, this);
         Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, playerListener, org.bukkit.event.Event.Priority.Normal, this);
         Bukkit.getPluginManager().registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, org.bukkit.event.Event.Priority.Normal, this);
 
         Bukkit.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, org.bukkit.event.Event.Priority.Normal, this);
         Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, org.bukkit.event.Event.Priority.Normal, this);
 
         configurePaperStacking();
 
         log.info("Writable enabled");
     }
 
 
     private void loadConfig() {
         getConfig().options().copyDefaults(true);
         saveConfig();
 
         List<String> implementsStrings = getConfig().getStringList("writingImplements");
 
         writingImplementMaterials = new ArrayList<Material>(); 
 
         for (String implementString: implementsStrings) {
             Material implementMaterial = Material.matchMaterial(implementString);
 
             if (implementMaterial == null) {
                 log.info("Invalid implement material: " + implementString);
                 // TODO: error
                 continue;
             }
 
             writingImplementMaterials.add(implementMaterial);
         }
 
         List<String> surfacesStrings = getConfig().getStringList("writingSurfaces");
 
         writingSurfaceMaterials = new ArrayList<Material>();
 
         for (String surfaceString: surfacesStrings) {
             Material surfaceMaterial = Material.matchMaterial(surfaceString);
 
             if (surfaceMaterial == null) {
                 log.info("Invalid surface material: " + surfaceString);
                 // TODO: error;
                 continue;
             }
 
             writingSurfaceMaterials.add(surfaceMaterial);
         }
 
         MemorySection inksSection = (MemorySection)getConfig().get("inks");
         Map<String,Object> inksMap = inksSection.getValues(true);
         
         inkColors = new HashMap<MaterialWithData,ChatColor>();
 
         Iterator it = inksMap.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
             String inkString = (String)pair.getKey();
             String colorString = (String)pair.getValue();
 
             MaterialWithData ink = lookupInk(inkString);
             if (ink == null) { 
                 log.info("Invalid ink item: " + inkString);
                 // TODO: error
                 continue;
             }
 
             ChatColor inkColor = ChatColor.valueOf(colorString.toUpperCase());
             if (inkColor == null) {
                 log.info("Invalid ink color: " + colorString);
                 // TODO: error
                 continue;
             }
 
             inkColors.put(ink, inkColor);
         }
 
         nextPaperID = (short)getConfig().getInt("nextPaperID", 1);
         paperLengthLineCap = getConfig().getInt("paperLengthLineCap", 7);
         consumeInk = getConfig().getBoolean("consumeInk", false);
 
         loadPapers();
     }
 
     private MaterialWithData lookupInk(String s) {
         Material material = Material.matchMaterial(s);
         if (material != null) {
             return new MaterialWithData(material);
         }
         DyeColor dyeColor = getDyeColor(s);
         if (dyeColor == null) {
             dyeColor = getDyeColor(s.replace("_dye", ""));
         }
         if (dyeColor != null) {
             Dye data = new Dye();
             data.setColor(dyeColor);
             ItemStack item = data.toItemStack();
 
             return new MaterialWithData(item.getType(), item.getData());
         }
         return null;
     }
 
     private DyeColor getDyeColor(String s) {
         // Unfortunately Bukkit doesn't have these names anywhere I can find
         // http://www.minecraftwiki.net/wiki/Data_values#Dyes
         if (s.equals("ink_sac")) {
             return DyeColor.BLACK;
         } else if (s.equals("rose_red")) {
             return DyeColor.RED;
         } else if (s.equals("cactus_green")) {
             return DyeColor.GREEN;
         } else if (s.equals("cocoa_beans")) {
             return DyeColor.BROWN;
         } else if (s.equals("lapis_lazuli")) {
             return DyeColor.BLUE;
         } else if (s.equals("purple")) {
             return DyeColor.PURPLE;
         } else if (s.equals("cyan")) {
             return DyeColor.CYAN;
         } else if (s.equals("light_gray")) {
             return DyeColor.SILVER;
         } else if (s.equals("gray")) {
             return DyeColor.GRAY;
         } else if (s.equals("pink")) {
             return DyeColor.PINK;
         } else if (s.equals("lime")) {
             return DyeColor.LIME;
         } else if (s.equals("dandelion_yellow")) {
             return DyeColor.YELLOW;
         } else if (s.equals("light_blue")) {
             return DyeColor.LIGHT_BLUE;
         } else if (s.equals("magenta")) {
             return DyeColor.MAGENTA;
         } else if (s.equals("orange")) {
             return DyeColor.ORANGE;
         } else if (s.equals("bone_meal")) {
             return DyeColor.WHITE;
         }
         try {
             return DyeColor.valueOf(s);
         } catch (Exception e) {
             return null;
         }
     }
 
     // Find writing implement in player's inventory and matching ink color
     public static int findImplementSlot(Player player) {
         PlayerInventory inventory = player.getInventory();
 
         for (int i = 0; i < 9; i += 1) {
             ItemStack implementItem = inventory.getItem(i);
 
             if (isWritingImplement(implementItem)) {
                 return i;
             }
         }
 
         return -1;
     }
 
     // Get nearby ink next to writing implement in inventory, if any
     public static int findInkSlot(Player player, int implementSlot) {
         PlayerInventory inventory = player.getInventory();
         ItemStack inkItem;
         int inkSlot;
 
         inkSlot = implementSlot - 1;
         if (inkSlot > 0) {
             inkItem = inventory.getItem(inkSlot);
             log.info("findInkSlot-1="+inkItem+"s"+inkSlot);
             if (getChatColor(inkItem) != null) {
                 return inkSlot;
             }
         }
 
         inkSlot = implementSlot + 1;
         inkItem = inventory.getItem(inkSlot);
         log.info("findInkSlot+1="+inkItem+"s"+inkSlot);
         if (getChatColor(inkItem) != null) {
             return inkSlot;
         }
 
         return -1;
     }
 
     private static boolean isWritingImplement(ItemStack item) {
         return writingImplementMaterials.contains(item.getType());
     }
 
     public static boolean isWritingSurface(Block block) {
         return writingSurfaceMaterials.contains(block.getType());
     }
 
     // Get chat color used for given writing ink
     public static ChatColor getChatColor(ItemStack item) {
         ChatColor color = inkColors.get(new MaterialWithData(item.getType(), item.getData()));
 
         return color;
     }
 
     // Try to make paper stack by damage ID, or otherwise stack by one
     // Based on http://code.google.com/p/nisovin-minecraft-bukkit-plugins/source/browse/trunk/BookWorm/src/com/nisovin/bookworm/BookWorm.java
     // http://dev.bukkit.org/server-mods/bookworm/
     private void configurePaperStacking() {
         try {
         boolean ok = false;
             // attempt to make papers with different data values stack separately
             try {
                 // obfuscated method name, check BookWorm for updates
                 String methodName = getConfig().getString("stack-by-data-fn", "a");//"bQ");
                 Method method = net.minecraft.server.Item.class.getDeclaredMethod(methodName, boolean.class);
                 if (method.getReturnType() == net.minecraft.server.Item.class) {
                     method.setAccessible(true);
                     method.invoke(net.minecraft.server.Item.PAPER, true);
                     ok = true;
                 }
             } catch (Exception e) {
                 log.info("Not stacking papers together");
             }
             if (!ok) {
                 // otherwise limit stack size to 1
                 Field field = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
                 field.setAccessible(true);
                 field.setInt(net.minecraft.server.Item.PAPER, 1);
             } else {
                 log.info("Successfully changed paper stacking");
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void onDisable() {
         saveConfig();
 
         log.info("Writable disabled");
     }
 
     // Manipulate state machine
     static public void setWritingState(Player player, WritingState newState) {
         WritingState oldState = getWritingState(player);
 
         //log.info("State change "+player.getName()+": "+oldState+" -> "+newState);
 
         if (newState == WritingState.NOT_WRITING) {
             writingState.remove(player);
         } else {
             writingState.put(player, newState);
         }
     }
 
     static public WritingState getWritingState(Player player) {
         WritingState state = writingState.get(player);
 
         return state == null ? WritingState.NOT_WRITING : state;
     }
 
     // Manipulate paper text
     public void writePaper(int id, String[] newLines, ChatColor color) {
         ArrayList<String> lines = readPaper(id);
 
         lines.addAll(formatLines(newLines, color));
 
         paperTexts.put(new Integer(id), lines);
 
         savePaper(id);
     }
 
     // Format 4x15 sign text reasonably into paragraphs
     public ArrayList<String> formatLines(String[] inLines, ChatColor color) {
         ArrayList<String> outLines = new ArrayList<String>();
         final int MAX_SIGN_LINE_LENGTH = 15;
 
         StringBuffer lineBuffer = new StringBuffer();
 
         // Add lines intelligently
         for (int i = 0; i < inLines.length; i += 1) {
             String line = inLines[i];
 
             // Blank line = new paragraph
             if (line == null || line.equals("")) {
                 // unless blank themselves
                 if (lineBuffer.toString().length() != 0) {
                     
                     outLines.add(color + lineBuffer.toString());
                     lineBuffer = new StringBuffer();
                 }
             } else {
                 // Text lines = concatenate
                 lineBuffer.append(line);
 
                 if (line.length() < MAX_SIGN_LINE_LENGTH) {
                     // Not at limit, add spacing
                     // (if hit limit, probably not a word break)
                     lineBuffer.append(" ");
                 }
             }
         }
 
         if (lineBuffer.toString().length() != 0) {
             outLines.add(color + lineBuffer.toString());
         }
 
         return outLines;
     }
 
     // Write paper contents to disk
     public void savePaper(int id) {
         String path = pathForPaper(id);
         log.info("saving to "+path);
 
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(path));
 
             ArrayList<String> lines = readPaper(id);
             for (String line: lines) {
                 out.write(line);
                 out.newLine();
             }
             out.close();
         } catch (IOException e) {
             log.info("Error saving #"+id+": "+e.getMessage());
         }
     }
 
     private String pathForPaper(int id) {
         String filename = String.format("%4x", id).replace(" ","0");    // TODO: %.4x
         String path = getDataFolder() + System.getProperty("file.separator") + filename + ".txt";
 
         return path;
     }
 
     private ArrayList<String> loadPaper(int id) {
         String path = pathForPaper(id);
         ArrayList<String> lines = new ArrayList<String>();
 
         try {
             BufferedReader in = new BufferedReader(new FileReader(path));
             String line;
             do
             {
                 line = in.readLine();
 
                 if (line != null) {
                     lines.add(line);
                 }
             } while(line != null);
         } catch (IOException e) {
             log.info("Error loading paper #"+id+": "+e.getMessage());
             // may have been deleted
             return new ArrayList<String>();
         }
 
         return lines;
     }
 
     // Load all papers from disk
     private void loadPapers() {
         paperTexts = new ConcurrentHashMap<Integer, ArrayList<String>>();
 
         for (int i = 1; i < nextPaperID; i += 1) {
             ArrayList<String> lines = loadPaper(i);
 
             paperTexts.put(i, lines);
         }
     }
 
     static public ArrayList<String> readPaper(int id) {
         ArrayList<String> lines = paperTexts.get(id);
 
         if (lines == null) {
             return new ArrayList<String>();   // empty array
         } else {
             return lines;
         }
     }
 
     short getNewPaperID() {
         short id = nextPaperID;
 
         nextPaperID += 1;
         getConfig().set("nextPaperID", nextPaperID);
         saveConfig();
 
         return id;
     }
 
     // Return whether more text can be written on the paper
     static boolean isPaperFull(int id) {
         ArrayList<String> lines = paperTexts.get(id);
 
         // formatLines() adds up to 2 lines of text
         return lines != null && lines.size() > paperLengthLineCap;
     }
 
     static boolean isInkConsumable() {
         return consumeInk;
     }
 }

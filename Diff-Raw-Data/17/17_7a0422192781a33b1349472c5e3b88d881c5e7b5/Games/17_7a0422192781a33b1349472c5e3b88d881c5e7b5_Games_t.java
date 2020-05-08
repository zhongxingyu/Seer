 package tzer0.Games;
 
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import tzer0.Games.GOL.CellType;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Plugin for bukkit adding different games.
  * 
  * @author TZer0
  */
 public class Games extends JavaPlugin {
     PluginDescriptionFile pdfFile;
     Configuration conf;
     private final GamesPlayerListener playerListener = new GamesPlayerListener(this);
     private final GamesBlockListener blockListener = new GamesBlockListener(this);
     public PermissionHandler permissions;
     @SuppressWarnings("unused")
     private final String name = "Games";
     LinkedList<World> iterationSet;
     LinkedHashSet<Integer> invalid;
     HashMap<World, LinkedList<Board>> boards;
     HashMap<Player, PlayerStorage> selected;
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         getServer().getScheduler().cancelTasks(this);
         System.out.println(pdfFile.getName() + " disabled.");
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onEnable()
      */
     public void onEnable() {
         invalid = new LinkedHashSet<Integer>();
         invalid.add(Material.TORCH.getId());
         invalid.add(Material.TNT.getId());
         invalid.add(Material.BEDROCK.getId());
         invalid.add(Material.SEEDS.getId());
         invalid.add(Material.LEVER.getId());
         invalid.add(Material.RAILS.getId());
         invalid.add(Material.POWERED_RAIL.getId());
         invalid.add(Material.PORTAL.getId());
         invalid.add(Material.LAVA.getId());
         invalid.add(Material.WATER.getId());
         invalid.add(Material.CAKE.getId());
         invalid.add(Material.BED_BLOCK.getId());
         invalid.add(Material.MOB_SPAWNER.getId());
         invalid.add(Material.FURNACE.getId());
         invalid.add(Material.CHEST.getId());
         invalid.add(Material.DISPENSER.getId());
         invalid.add(Material.BURNING_FURNACE.getId());
         invalid.add(Material.STATIONARY_LAVA.getId());
         invalid.add(Material.STATIONARY_WATER.getId());
         invalid.add(Material.SAND.getId());
         invalid.add(Material.GRAVEL.getId());
         invalid.add(Material.SAPLING.getId());
         invalid.add(Material.LEAVES.getId());
         invalid.add(Material.YELLOW_FLOWER.getId());
         invalid.add(Material.RED_ROSE.getId());
         invalid.add(Material.RED_MUSHROOM.getId());
         invalid.add(Material.BROWN_MUSHROOM.getId());
         invalid.add(Material.REDSTONE_TORCH_OFF.getId());
         invalid.add(Material.REDSTONE_TORCH_ON.getId());
         invalid.add(Material.FIRE.getId());
         invalid.add(Material.REDSTONE_WIRE.getId());
         invalid.add(Material.WORKBENCH.getId());
         invalid.add(Material.TRAP_DOOR.getId());
         invalid.add(Material.SNOW_BLOCK.getId());
         invalid.add(Material.ICE.getId());
         conf = getConfiguration();
         reload();
         pdfFile = this.getDescription();
         PluginManager tmp = getServer().getPluginManager();
         //tmp.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
         //tmp.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
     }
 
     public void reload() {
         if (selected == null) {
             selected = new HashMap<Player, PlayerStorage>();
         }
         boards = new HashMap<World, LinkedList<Board>>();
         setupPermissions();
         iterationSet = new LinkedList<World>();
         for (World world : getServer().getWorlds()) {
             iterationSet.add(world);
             LinkedList<Board> tmp = new LinkedList<Board>();
             boards.put(world, tmp);
             Map<String, ConfigurationNode> nodes = conf.getNodes(String.format("boards.%s", world.getName()));
             if (nodes != null) {
                 for (String node : nodes.keySet()) {
                     String type = nodes.get(node).getString("type", "gol");
                     if (type.equalsIgnoreCase("gol")) {
                         tmp.add(new GOL(node, world, null, null, true, this, conf));
                     } else if (type.equalsIgnoreCase("tetris")) {
                         tmp.add(new Tetris(node, world, null, null, true, this, conf));
                     }
                 }
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         if (sender instanceof Player) {
             Player pl = (Player) sender;
             if ((permissions == null && !pl.isOp()) || 
                     (permissions != null && !permissions.has(pl, "games.admin"))) {
                 pl.sendMessage(ChatColor.RED + "You do not have access to this command!");
                 return true;
             }
             boolean help = false;
             boolean golHelp = false;
             boolean tetrisHelp = false;
             PlayerStorage store = selected.get(pl);
             if (store == null) {
                 store = new PlayerStorage();
                 selected.put(pl, store);
             }
             int l = args.length;
             if (l >= 1) {
                 if (args[0].equalsIgnoreCase("gol")) {
                     golHelp = true;
                 } else if (args[0].equalsIgnoreCase("tetris")) {
                     tetrisHelp = true;
                 } else if (args[0].equalsIgnoreCase("node") || args[0].equalsIgnoreCase("n")) {
                     if (l == 1) {
                         posInfo(store.pos1, "Node 1", pl);
                         posInfo(store.pos2, "Node 2", pl);
                     } else {
                         if (args[1].equals("1")) {
                             store.pos1 = pl.getLocation();
                             posInfo(store.pos1, "Node 1", pl);
                         } else if (args[1].equals("2")) {
                             store.pos2 = pl.getLocation();
                             posInfo(store.pos2, "Node 2", pl);
                         } else {
                             pl.sendMessage(ChatColor.RED + "Argument must be 1 or 2.");
                         }
                     }
                 } else if (args[0].equalsIgnoreCase("addboard") || args[0].equalsIgnoreCase("ab")) {
                     if (store.pos1 == null || store.pos2 == null) {
                         pl.sendMessage("Invalid position.");
                         posInfo(store.pos1, "Node 1", pl);
                         posInfo(store.pos2, "Node 2", pl);
                         return true;
                     } else if (store.pos1.getWorld() != store.pos2.getWorld()) {
                         pl.sendMessage("Locations are in different worlds");
                         posInfo(store.pos1, "Node 1", pl);
                         posInfo(store.pos2, "Node 2", pl);
                         return true;
                     }
                     if (l == 3) {
                         if (findBoard(args[2]) == null) {
                             store.board = null;
                             if (args[1].equalsIgnoreCase("gol")) {
                                 store.board = new GOL(args[2], store.pos1.getWorld(), store.pos1, store.pos2, false, this, conf);
                             } else if (args[1].equalsIgnoreCase("tetris")) {
                                 store.board = new Tetris(args[2], store.pos1.getWorld(), store.pos1, store.pos2, false, this, conf);
                             }
                             if (store.board != null) {
                                 boards.get(store.pos1.getWorld()).add(store.board);
                                 pl.sendMessage(ChatColor.GREEN+"New board created and selected.");
                                 store.type = null;
                             } else {
                                 pl.sendMessage(ChatColor.RED + "Invalid board-type!");
                             }
 
                         } else {
                             pl.sendMessage(ChatColor.RED+"Board with that name already exists!");
                         }
                     } else {
                         pl.sendMessage("Please provide board-type and area-name.");
                     }
 
                 } else if (args[0].equalsIgnoreCase("deleteboard") || args[0].equalsIgnoreCase("db")) {
                     if (l == 2) {
                         Board tmp = findBoard(args[1]);
                         if (tmp != null) {
                             boards.get(tmp.startblock.getWorld()).remove(tmp);
                             conf.removeProperty(String.format("boards.%s.%s",tmp.startblock.getWorld().getName(), tmp.name));
                             conf.save();
                             pl.sendMessage(ChatColor.GREEN + "Removed board.");
                         } else {
                             pl.sendMessage(ChatColor.RED + "No such board.");
                         }
                     } else {
                         pl.sendMessage(ChatColor.RED + "Please provide board-name");
                     }
                 } else if (args[0].equalsIgnoreCase("selectboard") ||  args[0].equalsIgnoreCase("sb")) {
                     if (l >= 2) {
                         Board tmp = findBoard(args[1]);
                         if (tmp == null) {
                             pl.sendMessage(ChatColor.RED + "Not found.");
                         } else {
                             pl.sendMessage(ChatColor.GREEN + "Selected.");
                             store.board = tmp;
                             store.board.info(pl);
                             store.type = null;
                         }
                     }
                 } else if (args[0].equalsIgnoreCase("listboards") ||  args[0].equalsIgnoreCase("lb")) {
                     int page = 0;
                     if (l == 2) {
                         page = toInt(args[1]);
                     }
                     showBoards(pl, page);
                 } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                     reload();
                 } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("bi")) {
                     if (checkBoard(store.board, pl)) {
                         store.board.info(pl);
                     }
                 } else if (args[0].equalsIgnoreCase("savestate") || args[0].equalsIgnoreCase("ss")) {
 
                 } else if (store.board instanceof GOL) {
                     if (!handleGOLParam((GOL)store.board, store.type, pl, args, false, store)) {
                         pl.sendMessage("No such command.");
                         help = true;
                     }
                 } else if (store.board instanceof Tetris) {
                     if (!handleTetrisParam((Tetris)store.board, pl, args, false)) {
                         pl.sendMessage("No such command.");
                         help = true;
                     }
                 }
             } else {
                 help = true;
             }
             if (help || golHelp || tetrisHelp) {
                 if (l == 0 || (!golHelp && !tetrisHelp)) {
                     sender.sendMessage(ChatColor.YELLOW + String.format("%s by TZer0 (tzer0.jan@gmail.com), v%s",pdfFile.getName(), pdfFile.getVersion()));
                     sender.sendMessage(ChatColor.YELLOW + "[] denotes arguments, {} means optional, () are aliases");
                     sender.sendMessage(ChatColor.YELLOW + "red commands require a selected board");
                     sender.sendMessage(ChatColor.GREEN + "Commands:");
                     sender.sendMessage(ChatColor.GREEN + "(n)ode {[1/2]} - shows or sets nodes");
                     sender.sendMessage(ChatColor.GREEN + "(a)dd(b)oard [gol/tetris] [name] - adds a new board");
                     sender.sendMessage(ChatColor.GREEN + "(d)elete(b)oard [name] - deletes a board");
                     sender.sendMessage(ChatColor.GREEN + "(s)elect(b)oard [name] - selects a board");
                     sender.sendMessage(ChatColor.GREEN + "(l)ist(b)oard [page] - lists boards");
                     sender.sendMessage(ChatColor.RED + "(b)oard(i)nfo - shows info about the board");
                     sender.sendMessage(ChatColor.GREEN + "(r)eload - reloads settings");
                     sender.sendMessage(ChatColor.GREEN + "/g gol for game of life-specific commands");
                     sender.sendMessage(ChatColor.GREEN + "/g tetris for tetris-specific commands");
                 } else if (golHelp) {
                     sender.sendMessage(ChatColor.YELLOW + "Game of life-specific (select board first) commands:");
                     sender.sendMessage(ChatColor.YELLOW + "red commands require a selected type");
                     sender.sendMessage(ChatColor.GREEN + "(i)terate(b)oard {[steps]} - iterate steps times.");
                     sender.sendMessage(ChatColor.GREEN + "(a)dd(t)ype [id] - adds a type");
                     sender.sendMessage(ChatColor.GREEN + "(d)elete(t)ype [id] - removes a type");
                     sender.sendMessage(ChatColor.GREEN + "(s)elect(t)ype [id] - selects a type");
                     sender.sendMessage(ChatColor.GREEN + "(d)efault [materialID] - sets a default cell-type");
                     sender.sendMessage(ChatColor.RED + "(m)od(t)ype [cmin/cmax/smin/smax/aw/dw] [v] - mods a type");
                     sender.sendMessage(ChatColor.RED + "(w)in(p)os {(s)et} - shows/sets redstone-torch placement after win.");
                 } else if (tetrisHelp) {
                     sender.sendMessage(ChatColor.YELLOW + "Tetris-specific (select board first) commands:");
                     sender.sendMessage(ChatColor.GREEN + "(m)odify(p)roperties [block(min)/block(max)/(dir)ection] [val]");
                     sender.sendMessage(ChatColor.GREEN + "start");
                     sender.sendMessage(ChatColor.GREEN + "stop");
                 }
 
             }
         }
         return true;
     }
 
     public boolean handleTetrisParam(Tetris board, Player pl, String []args, boolean sign) {
         int l = args.length;
         if (l >= 1) {
             if (args[0].equalsIgnoreCase("start")) {
                 board.startGame(pl);
                 return true;
             } else if (args[0].equalsIgnoreCase("stop")) {
                 board.stopGame(pl);
                 return true;
             } else if (args[0].equalsIgnoreCase("modifyproperties") || args[0].equalsIgnoreCase("mp")) {
                 if (l >= 2) {
                     if (l == 3) {
                         if (args[1].equalsIgnoreCase("blockmax") || args[1].equalsIgnoreCase("max")) {
                             board.blockmax = Math.max(toInt(args[2]), board.blockmin);
                         } else if (args[1].equalsIgnoreCase("blockmin") || args[1].equalsIgnoreCase("min")) {
                             board.blockmin = Math.min(toInt(args[2]), board.blockmax);
                         } else if (args[1].equalsIgnoreCase("direction") || args[1].equalsIgnoreCase("dir")) {
                             board.tdir = (char) toInt(args[2]);
                         }
                         board.info(pl);
                         board.save();
                     }
                 }
             } 
         }
         return false;
     }
 
     public boolean handleGOLParam(GOL board, CellType type, Player pl, String []args, boolean sign, PlayerStorage store) {
         int l = args.length;
         if (l >= 1) {
             if (args[0].equalsIgnoreCase("iterateboard") || args[0].equalsIgnoreCase("ib")) {
                 if (checkBoard(board, pl)) {
                     if (l == 2) {
                         board.iterate(null, toInt(args[1]));
                     } else {
                         board.iterate(null, 1);
                     }
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
                 if (checkBoard(board, pl)) {
                     board.clear(null);
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("circuitboard") || args[0].equalsIgnoreCase("cb")) {
                 board.CircuitBoard(pl.getLocation().getBlock());
                 return true;
             } else if (args[0].equalsIgnoreCase("loadstate") || args[0].equalsIgnoreCase("ls")) {
                 return true;
             } else if (args[0].equalsIgnoreCase("modtype") || args[0].equalsIgnoreCase("mt")) {
                 if (checkType(type, pl)) {
                     if (l >= 2) {
                         if (l >= 3) {
                             int val = toInt(args[2]);
                             if (args[1].equalsIgnoreCase("survmin") || args[1].equalsIgnoreCase("smin")) {
                                 type.survMin = val;
                             } else if (args[1].equalsIgnoreCase("survmax") || args[1].equalsIgnoreCase("smax")) {
                                 type.survMax = val;
                             } else if (args[1].equalsIgnoreCase("creamin") || args[1].equalsIgnoreCase("cmin")) {
                                 type.creaMin = val;
                             } else if (args[1].equalsIgnoreCase("creamax") || args[1].equalsIgnoreCase("cmax")) {
                                 type.creaMax = val;
                             } else if (args[1].equalsIgnoreCase("addweakness") || args[1].equalsIgnoreCase("aw")) {
                                 type.addOther(val, pl);
                             } else if (args[1].equalsIgnoreCase("deleteweakness") || args[1].equalsIgnoreCase("dw")) {         
                                 type.removeOther(val, pl);
                             }
                             type.info(pl);
                             if (!sign) {
                                 type.save();
                             }
                         } else {
                             pl.sendMessage(ChatColor.RED + "Please provide value.");
                         }
                     } else {
                         pl.sendMessage(ChatColor.RED + "Please provide field and value.");
                     }
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("default") || args[0].equalsIgnoreCase("d")) {
                 if (l == 2) {
                     board.changeDefault(toInt(args[1]), pl);
                 } else {
                     store.board.info(pl);
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("selecttype") || args[0].equalsIgnoreCase("st")) {
                 if (l == 2) {
                     CellType tmp = board.findCell(toInt(args[1]));
                     if (tmp == null) {
                         pl.sendMessage(ChatColor.RED + "Not found.");
                     } else {
                         store.type = tmp;
                         pl.sendMessage(ChatColor.GREEN + "Selected.");
                         tmp.info(pl);
                     }
                 } else {
                     pl.sendMessage(ChatColor.RED + "Please provide material type.");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("addtype") || args[0].equalsIgnoreCase("at")) {
                 if (l == 2) {
                     store.type = board.addCell(toInt(args[1]), pl);
                 } else {
                     pl.sendMessage(ChatColor.RED + "Please provide material type.");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("deletetype") || args[0].equalsIgnoreCase("dt")) {
                 if (l == 2) {
                     if (store.type == ((GOL)store.board).findCell(toInt(args[1]))) {
                         store.type = null;
                     }
                     board.removeCell(toInt(args[1]), pl);
                     for (CellType tmp : ((GOL)store.board).races) {
                         tmp.removeOther(toInt(args[1]), null);
                     }
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("winpos") || args[0].equalsIgnoreCase("wp")) {
                 if (l >= 2 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("s"))) {
                     board.winpos = pl.getLocation().getBlock();
                     board.save();
                     pl.sendMessage(ChatColor.GREEN + "Set!");
                 } else if (l >= 2 && (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("r"))) {
                     board.winpos = null;
                     board.save();
                     pl.sendMessage(ChatColor.GREEN + "Removed!");
                 }
                 board.info(pl);
                 return true;
             }
         }
         return false;
     }
 
     public void showBoards(Player pl, int page) {
         if (page < 0) {
             page = 0;
         }
         int i = 0;
         int numbrd = numberOfBoards();
         pl.sendMessage(ChatColor.GREEN + String.format("Boards %d to %d", Math.min(page*10, numbrd), Math.min((page+1)*10, numbrd)));
         for (World wrd : iterationSet) {
             for (Board brd : boards.get(wrd)) {
                 if (i > (page+1)*10) {
                     break;
                 } else if (i >= page*10) {
                     brd.shortInfo(pl);
                 }
                 i++;
             }
         }
     }
 
     public LinkedList<Board> allBoards() {
         LinkedList<Board> out = new LinkedList<Board>();
         for (World world : getServer().getWorlds()) {
             LinkedList<Board> list = boards.get(world);
             if (list != null) {
                 for (Board brd : list) {
                     out.add(brd);
                 }
             }
         }
         return out;
     }
 
     public int numberOfBoards() {
         int i = 0;
         for (World wrd : iterationSet) {
             if (boards.get(wrd) != null) {
                 i+= boards.get(wrd).size();
             }
         }
         return i;
     }
 
     public Board findBoard(String bname) {
         for (World w : boards.keySet()) {
             for (Board brd : boards.get(w)) {
                 if (brd.name.equalsIgnoreCase(bname)) {
                     return brd;
                 }
             }
         }
         return null;
     }
 
     public boolean checkGOL(Board board, Player pl) {
         if (!checkBoard(board, pl)) {
             return false;
         } else if (!(board instanceof GOL)) {
             pl.sendMessage(ChatColor.RED + "This is not a game of life-board!");
             return false;
         }
         return true;
     }
 
     public boolean checkTetris(Board board, Player pl) {
         if (!checkBoard(board, pl)) {
             return false;
         } else if (!(board instanceof Tetris)) {
             pl.sendMessage(ChatColor.RED + "This is not a Tetris-board!");
             return false;
         }
         return true;
     } 
 
     public boolean checkBoard(Board board, Player pl) {
         if (board == null) {
             pl.sendMessage(ChatColor.RED + "No board selected, please select one.");
             return false;
         }
         return true;
     }
 
     public boolean checkType(CellType type, Player pl) {
         if (type == null) {
             pl.sendMessage(ChatColor.RED + "No type selected, please select one.");
             return false;
         }
         return true;
     }
 
     public boolean checkNode(PlayerStorage store, Player pl) {
         return true;
     }
 
     public void posInfo(Location pos, String name, Player pl) {
         if (pos != null) {
             pl.sendMessage(ChatColor.YELLOW + String.format("%s: (%d, %d, %d) in %s", 
                     name, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), pos.getWorld().getName()));
         } else {
             pl.sendMessage(ChatColor.RED + String.format("%s is not set.", name));
         }
     }
     /**
      * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
      */
     private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
         if (this.permissions == null) {
             if (test != null) {
                 this.permissions = ((Permissions) test).getHandler();
             } else {
                 System.out.println(ChatColor.YELLOW
                         + "Permissons not detected - defaulting to OP!");
             }
         }
     }
     /**
      * Converts to int if valid, if not: returns -1
      * @param in
      * @param sender
      * @return
      */
     public int toInt(String in) {
         int out = -1;
         if (checkInt(in) && in.length() != 0) {
             out = Integer.parseInt(in);
         }
         return out;
     }
     /**
      * Checks if a string is valid as a representation of an unsigned int.
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!Character.isDigit(chars[i])) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean isValidMaterial(int m) {
         if (Material.getMaterial(m) == null || invalid.contains(m) || m > 255) {
             return false;
         }
         return true;
     }
    private class PlayerStorage {
        public Location pos1, pos2;
        public Board board;
        public CellType type;
         public PlayerStorage() {
             pos1 = pos2 = null;
             board = null;
             type = null;
         }
     }
 }

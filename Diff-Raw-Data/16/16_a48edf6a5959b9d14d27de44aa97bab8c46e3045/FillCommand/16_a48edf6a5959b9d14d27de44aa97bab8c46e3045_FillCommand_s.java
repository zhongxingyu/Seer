 package info.bytecraft.commands;
 
 import static org.bukkit.ChatColor.BLUE;
 import static org.bukkit.ChatColor.DARK_AQUA;
 import static org.bukkit.ChatColor.RED;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.material.MaterialData;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.blockfill.AbstractFiller;
 import info.bytecraft.blockfill.Copy;
 import info.bytecraft.blockfill.Filler;
 import info.bytecraft.blockfill.History;
 import info.bytecraft.blockfill.Paster;
 import info.bytecraft.blockfill.Replacer;
 import info.bytecraft.blockfill.SavedBlocks;
 import info.bytecraft.blockfill.TestFiller;
 import info.bytecraft.blockfill.TestReplacer;
 import info.bytecraft.blockfill.Undo;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 
 @SuppressWarnings("deprecation")
 public class FillCommand extends AbstractCommand
 {
 
     private static int MAX_FILL_SIZE = (10 * 16) * (10 * 16) * 128;
 
     private History undoHistory;
     private History copyHistory;
 
     public FillCommand(Bytecraft tregmine, String command)
     {
         super(tregmine, command);
 
         undoHistory = new History();
         copyHistory = new History();
     }
 
     @Override
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if (!player.getRank().canFill()) {
             player.sendMessage(getInvalidPermsMessage());
             return true;
         }
 
         Server server = plugin.getServer();
         BukkitScheduler scheduler = server.getScheduler();
 
         // undo
         if (args.length > 0 && "undo".equals(args[0])) {
             World world = player.getWorld();
             SavedBlocks blocks = undoHistory.get(player);
             if (blocks == null) {
                 return true;
             }
 
             Undo undo = new Undo(plugin, world, blocks, 100000);
             undo.setScheduleState(scheduler,
                     scheduler.scheduleSyncRepeatingTask(plugin, undo, 0, 1));
             
             player.sendMessage(DARK_AQUA + "Undo in progress.");
 
             return true;
         }
 
         Block b1 = player.getFillBlock1();
 
         if (args.length > 0 && "paste".equals(args[0])) {
             if (b1 == null) {
                 player.sendMessage(DARK_AQUA
                         + "Specify the point where you want to paste.");
                 return true;
             }
 
            double theta =
                    args.length > 1 ? Double.parseDouble(args[1]) * Math.PI
                            / 180.0 : 0.0;
             player.sendMessage(DARK_AQUA + "Rotating " + theta + " radians.");
 
             World world = player.getWorld();
             SavedBlocks blocks = copyHistory.get(player);
             if (blocks == null) {
                 return true;
             }
 
             Paster paster =
                     new Paster(undoHistory, player, world, b1, blocks, theta,
                             100000);
             paster.setScheduleState(scheduler,
                     scheduler.scheduleSyncRepeatingTask(plugin, paster, 0, 1));
 
             player.sendMessage(DARK_AQUA + "Paste in progress.");
 
             return true;
         }
 
         Block b2 = player.getFillBlock2();
 
         if (b1 == null || b2 == null) {
             player.sendMessage(DARK_AQUA + "You need to select two corners!");
             return true;
         }
 
         // execute a copy
         AbstractFiller filler = null;
         if (args.length > 0 && "copy".equals(args[0])) {
             filler = new Copy(plugin, copyHistory, player, b1, b2, 100000);
             player.sendMessage(DARK_AQUA + "Copied selected area.");
         }
 
         // otherwise, try regular fills
         else {
             MaterialData mat = parseMaterial(args[0]);
             MaterialData toMat =
                     args.length > 1 ? parseMaterial(args[1]) : null;
 
             // regular fills
             if (mat != null && toMat == null) {
 
                 if (!player.isOp() &&
                         !mat.toItemStack().getType().isBlock()) {
                     player.sendMessage(RED + "Disabled!");
                     return true;
                 }
 
                 player.sendMessage("You filled with " + DARK_AQUA
                         + mat.toString() + "(" + mat.getItemTypeId() + ")");
 
                 if (command.equals("fill")) {
                     filler =
                             new Filler(plugin, undoHistory, player, b1, b2, mat, 100000);
                     plugin.getLogger().info("[FILL] " + player.getName() + " filled ["
                             + b1.getLocation().getBlockX() + ","
                             + b1.getLocation().getBlockZ() + ","
                             + b1.getLocation().getBlockY() + "] - ["
                             + b2.getLocation().getBlockX() + ","
                             + b2.getLocation().getBlockZ() + ","
                             + b2.getLocation().getBlockY() + "]  with "
                             + mat.toString() + " " + mat.getItemTypeId());
                     
                     try(IContext ctx = plugin.createContext()){
                         ctx.getLogDAO().insertFillLog(player, filler, mat.getItemType(), "fill");
                     } catch (DAOException e) {
                         throw new RuntimeException(e);
                     }
                 }
 
                 if (command.equals("testfill")) {
                     filler = new TestFiller(plugin, player, b1, b2, mat, 100000);
                 }
             }
 
             // replacers
             if (mat != null && toMat != null) {
 
                 if (!player.isOp() &&
                         !mat.toItemStack().getType().isBlock()) {
                     player.sendMessage(RED + "Disabled!");
                     return true;
 
                 }
 
                 player.sendMessage("You replaced " + DARK_AQUA + mat.toString()
                         + "(" + mat.getItemTypeId() + ")" + BLUE + "with"
                         + DARK_AQUA + toMat.toString() + "("
                         + toMat.getItemTypeId() + ")");
 
                 if (command.equals("fill")) {
                     filler = new Replacer(plugin, undoHistory, player, b1, b2, mat,
                                     toMat, 100000);
                     
                     try(IContext ctx = plugin.createContext()){
                         ctx.getLogDAO().insertFillLog(player, filler, toMat.getItemType(), "fill");
                     } catch (DAOException e) {
                         throw new RuntimeException(e);
                     }
                 }
 
                 if (command.equals("testfill")) {
                     filler = new TestReplacer(plugin, player, b1, b2, mat, toMat, 100000);
                 }
 
                 plugin.getLogger().info("[FILL] " + player.getName() + " replaced with "
                         + toMat.toString() + " " + toMat.getItemTypeId() + "["
                         + b1.getLocation().getBlockX() + ","
                         + b1.getLocation().getBlockZ() + ","
                         + b1.getLocation().getBlockY() + "] - ["
                         + b2.getLocation().getBlockX() + ","
                         + b2.getLocation().getBlockZ() + ","
                         + b2.getLocation().getBlockY() + "] with "
                         + mat.toString() + " " + mat.getItemTypeId());
             }
         }
 
         if (filler == null) {
             player.sendMessage(RED + "Invalid command!");
             return false;
         }
 
         if (filler.getTotalVolume() > MAX_FILL_SIZE) {
             player.sendMessage(DARK_AQUA + "Selected area is too big ("
                     + filler.getTotalVolume() + ")!");
             return true;
         }
 
         // execute action
         if (filler != null) {
             player.sendMessage(DARK_AQUA + "Total volume is "
                     + filler.getTotalVolume() + ".");
             filler.setScheduleState(scheduler,
                     scheduler.scheduleSyncRepeatingTask(plugin, filler, 0, 1));
         }
 
         return true;
     }
 
     private MaterialData parseMaterial(String str)
     {
         Material material;
         MaterialData data;
 
         try {
             byte subType = 0;
             if (str.matches("^[0-9]+$")) {
                 material = Material.getMaterial(Integer.parseInt(str));
             }
             else if (str.matches("^[0-9]+:[0-9]+$")) {
                 String[] segmentedInput = str.split(":");
 
                 int materialType = Integer.parseInt(segmentedInput[0]);
                 subType = Byte.parseByte(segmentedInput[1]);
 
                 material = Material.getMaterial(materialType);
             }
             else if (str.matches("^[A-Za-z_]+:[0-9]+$")) {
                 String[] segmentedInput = str.split(":");
 
                 material =
                         Material.getMaterial(segmentedInput[0].toUpperCase());
                 subType = Byte.parseByte(segmentedInput[1]);
             }
             else {
                 material = Material.getMaterial(str.toUpperCase());
             }
 
             if (material == null) {
                 return null;
             }
 
             data = new MaterialData(material, subType);
 
             return data;
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
 }

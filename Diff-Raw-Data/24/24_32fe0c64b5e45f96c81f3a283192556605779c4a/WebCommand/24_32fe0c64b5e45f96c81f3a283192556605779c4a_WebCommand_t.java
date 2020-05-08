 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.other
  * Created: 2013/01/30 0:30:46
  */
 package net.syamn.sakuracmd.commands.other;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 /**
  * WebCommand (WebCommand.java)
  * @author syam(syamn)
  */
 public class WebCommand extends BaseCommand {
     public WebCommand() {
         bePlayer = false;
         name = "web";
         perm = null;
         argLength = 1;
         usage = "<- web link commands";
     }
 
     private static final String reset = "\u00A7r";
 
     @Override
     public void execute() throws CommandException {
         String action = args.remove(0);
 
         // web tploc [name] (world) [x] [y] [z] (yaw) (pitch) [message]
        if (action.equalsIgnoreCase("tploc") && args.size() >= 7){
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                 @Override public void run(){
                     tploc();
                 }
             }, 1L);
             return;
         }
 
         throw new CommandException("&cUndefined command!");
     }
 
     private void tploc() {
         final String targetName = args.remove(0);
 
         Player target = Bukkit.getPlayer(targetName);
         if (target == null || !target.isOnline()) {
             LogUtil.warning("Teleport aborted. Player offline: " + targetName);
             return;
         }
 
         // check location
         World world = target.getWorld();
         if (!StrUtil.isDouble(args.get(0))) {
             String wname = args.remove(0);
             world = Bukkit.getWorld(wname);
             if (world == null) {
                 LogUtil.warning("Teleport aborted. World not found: " + wname);
                 return; // World not found
             }
         }
         if (!StrUtil.isDouble(args.get(0)) || !StrUtil.isDouble(args.get(1)) || !StrUtil.isDouble(args.get(2))) {
             LogUtil.warning("Teleport aborted. Invalid location: " + args.get(0) + "," + args.get(1) + "," + args.get(2));
             return; // invalid location
         }
 
         double x = Double.parseDouble(args.remove(0));
         double y = Double.parseDouble(args.remove(0));
         double z = Double.parseDouble(args.remove(0));
         Location loc = new Location(world, x, y, z);
 
         // check yaw/pitch
         if (args.size() >= 2 && StrUtil.isFloat(args.get(0)) && StrUtil.isFloat(args.get(1))) {
             loc.setYaw(Float.valueOf(args.remove(0)));
             loc.setPitch(Float.valueOf(args.remove(0)));
         }
 
         String msg = (args.size() > 0) ? StrUtil.join(args, " ") : null;
 
         // do action
         target.teleport(loc, TeleportCause.PLUGIN);
         if (msg != null) Util.message(target, msg);
         LogUtil.info("Teleported player:" + target.getName() + " to " + loc.getWorld().getName() + ":" + loc.getX() + "," + loc.getY() + "," + loc.getZ());
     }
 
     @Override
     public boolean permission(CommandSender sender) {
         if (sender instanceof Player){
             return false;
         }
         return true;
     }
 }

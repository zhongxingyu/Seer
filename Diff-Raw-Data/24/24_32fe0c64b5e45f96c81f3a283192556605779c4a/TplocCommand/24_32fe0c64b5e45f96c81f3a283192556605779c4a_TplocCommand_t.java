 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.tp
  * Created: 2013/01/31 20:14:41
  */
 package net.syamn.sakuracmd.commands.tp;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 /**
  * TplocCommand (TplocCommand.java)
  * @author syam(syamn)
  */
 public class TplocCommand extends BaseCommand{
     public TplocCommand(){
        bePlayer = false;
         name = "tploc";
         perm = Perms.TPLOC;
         argLength = 3;
         usage = "<x> <y> <z> [world] [player] <- tp to location";
     }
 
     @Override
     public void execute() throws CommandException{
        if (!isPlayer && args.size() < 5){
            throw new CommandException("&c引数が足りません！");
        }
        
         if (!StrUtil.isDouble(args.get(0)) || !StrUtil.isDouble(args.get(1)) || !StrUtil.isDouble(args.get(2))){
             throw new CommandException("&c数値ではない座標情報が含まれています！");
         }
 
         final double x = Double.parseDouble(args.remove(0));
         final double y = Double.parseDouble(args.remove(0));
         final double z = Double.parseDouble(args.remove(0));
 
        World world = null;
        Player target = null;
        
        if (isPlayer){
            world = player.getWorld();
            target = player;
        }
 
         if (args.size() >= 1){
             world = Bukkit.getWorld(args.get(0));
             if (world == null){
                 throw new CommandException("&cワールド " + args.get(0) + " が見つかりません！");
             }
 
             if (args.size() >= 2){
                 target = Bukkit.getPlayer(args.remove(1));
                 if (target == null || !target.isOnline()){
                     throw new CommandException("&cプレイヤー " + args.get(1) + " が見つかりません！");
                 }
             }
         }
 
         final SakuraPlayer sp = PlayerManager.getPlayer(target);
         final Location toLoc = new Location(world, x, y, z);
 
         target.teleport(toLoc, TeleportCause.COMMAND);
 
         if (!sender.equals(target)){
             Util.message(sender, "&a" + sp.getName() + " &aを " + StrUtil.getLocationString(toLoc) + " に移動させました");
         }
         Util.message(target, "&a" + StrUtil.getLocationString(toLoc) + " にテレポートしました");
     }
 }

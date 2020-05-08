 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.player
  * Created: 2013/01/12 1:03:37
  */
 package net.syamn.sakuracmd.commands.player;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * FlyCommand (FlyCommand.java)
  * @author syam(syamn)
  */
 public class FlyCommand extends BaseCommand{
     public FlyCommand(){
         bePlayer = false;
         name = "fly";
         perm = Perms.FLY;
         argLength = 0;
         usage = "[player] <- toggle flymode";
     }
 
     public void execute() throws CommandException{
         if (args.size() == 0 && !isPlayer){
             throw new CommandException("&cプレイヤー名を指定してください！");
         }
         
         final Player target = (args.size() > 0) ? Bukkit.getPlayer(args.get(0)) : player;
         if (target == null || !target.isOnline()){
             throw new CommandException("&cプレイヤーが見つかりません！");
         }
         final SakuraPlayer sp = PlayerManager.getPlayer(target);
         
         // self-check
         if (!sender.equals(target) && !Perms.FLY_OTHER.has(sender)){
             throw new CommandException("&c他人の飛行モードを変更する権限がありません！");
         }
         
         if (sp.hasPower(Power.FLY)){
             // Remove fly power
             sp.removePower(Power.FLY);
             SakuraCmdUtil.changeFlyMode(target, false);
             
             if (!sender.equals(target)){
                Util.message(sender, "&3" + sp.getName() + " &3の飛行モードを解除しました");
             }
             Util.message(target, "&3あなたの飛行モードは解除されました");
         }else{
             // Add fly power
             sp.addPower(Power.FLY);
             SakuraCmdUtil.changeFlyMode(target, true);
             
             if (!sender.equals(target)){
                 Util.message(sender, "&3" + sp.getName() + " &3を飛行モードにしました");
             }
             Util.message(target, "&3あなたは飛行モードになりました");
         }
     }
 }

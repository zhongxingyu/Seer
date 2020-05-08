 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.other
  * Created: 2013/01/09 14:40:54
  */
 package net.syamn.sakuracmd.commands.other;
 
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.SakuraPlayer;
 import net.syamn.utils.Util;
 import net.syamn.utils.economy.EconomyUtil;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * MfmfCommand (MfmfCommand.java)
  * @author syam(syamn)
  */
 public class MfmfCommand extends BaseCommand {
     public MfmfCommand() {
         bePlayer = false;
         name = "mfmf";
         perm = null;
         argLength = 0;
         usage = "<player> <- mfmf!";
     }
     
     @Override
     public void execute() throws CommandException {
         if (args.size() == 0) {
             Util.message(sender, " &aもふもふ...？&7 /もふもふ <プレイヤー名>");
             if (isPlayer){
                 Util.message(sender, " &aあなたは今までに" + PlayerManager.getPlayer(player).getData().getMofCount() + "回もふもふされました！");
             }
         } else {
             final Player target = Bukkit.getPlayer(args.get(0));
             if (target == null || !target.isOnline()){
                 throw new CommandException(" &6もふ...？ 相手が見つからないです...。");
             }
             if (target.equals(sender)){
                 throw new CommandException("&c自分をもふもふできません！");
             }
             
             final SakuraPlayer sp = PlayerManager.getPlayer(target);
            final String senderName = (isPlayer) ? PlayerManager.getPlayer(player).getName() : sender.getName();
             
             boolean paid = false;
             if (isPlayer && SCHelper.getInstance().isEnableEcon()){
                 paid = EconomyUtil.takeMoney(player, 150.0D); // -150 Coin
             }
             
             if (paid){
                 EconomyUtil.addMoney(target, 100.0D); // +100 Coin
                 
                 final int total = PlayerManager.getPlayer(target).getData().addMofCount(); // mof count++
                 Util.message(target, " &6'" + senderName + "'&aにもふもふされました！(+100Coin)(" + total + "回目)");
                 Util.message(sender, " &6'" + sp.getName() + "'&aをもふもふしました！&c(-150Coin)");
             }else{
                 Util.message(target, " &6'" + senderName + "'&aにもふもふされました！");
                 Util.message(sender, " &6'" + sp.getName() + "'&aをもふもふしました！");
             }
         }
     }
     
     @Override
     public boolean permission(CommandSender sender) {
         return true;
     }
 }

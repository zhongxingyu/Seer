 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.other
  * Created: 2013/02/09 13:53:18
  */
 package net.syamn.sakuracmd.commands.other;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map.Entry;
 
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.serial.EndResetWorldData;
 import net.syamn.sakuracmd.worker.EndResetWorker;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.TimeUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * EndResetCommand (EndResetCommand.java)
  * @author syam(syamn)
  */
 public class EndResetCommand extends BaseCommand {
     public EndResetCommand() {
         bePlayer = false;
         name = "endreset";
         perm = Perms.END_RESET;
         argLength = 0;
         usage = "[args] <- endreset commands";
     }
 
     private EndResetWorker worker = null;
 
     @Override
     public void execute() throws CommandException {
         worker = EndResetWorker.getInstance();
         if (worker == null){
             throw new CommandException("&c現在ワーカーがオフラインのためコマンドを実行できません");
         }
 
         if (args.size() == 0){
            if (!(sender instanceof Player)){
                throw new CommandException("&c有効なアクションではありません (timer/force/ignore/list)");
            }

            final World world = player.getWorld();
            if (world.getEnvironment() != Environment.THE_END){
                throw new CommandException("&cここはエンドワールドではありません！");
            }

            runResetTask(world);
             return;
         }
 
         String action = args.get(0).toLowerCase(Locale.ENGLISH);
         if (action.equals("timer")){
             if (args.size() < 3){
                 throw new CommandException("&c引数が足りません！ /endreset timer set/disable world_name");
             }
             String sub = args.get(1).toLowerCase(Locale.ENGLISH);
             if (sub.equals("set")){
                 if (args.size() < 4){
                     throw new CommandException("&c引数が足りません！ /endreset timer set world_name hours");
                 }
                 World world = Bukkit.getWorld(args.get(2));
                 if (world == null){
                     throw new CommandException("&cそのワールドが見つかりません！");
                 }
                 if (world.getEnvironment() != Environment.THE_END){
                     throw new CommandException("&cそのワールドはエンドではありません！");
                 }
                 if (!StrUtil.isInteger(args.get(3))){
                     throw new CommandException("&c数値ではありません: " + args.get(3));
                 }
                 worker.putWorldData(world.getName(), new EndResetWorldData(Integer.parseInt(args.get(3))));
                 Util.message(sender, "&aワールド " + world.getName() + " は" + args.get(3) + "時間毎にリセットされます");
             }
             else if (sub.equals("disable")){
                 if (!worker.getWorldDataMap().containsKey(args.get(2))){
                     throw new CommandException("&c" + args.get(2) + " がリストに見つかりません");
                 }
                 worker.removeWorldData(args.get(2));
                 Util.message(sender, "&aワールド " + args.get(2) + " は自動でリセットされなくなりました");
             }
             else{
                 throw new CommandException("&c不正なサブコマンドです /endreset timer set/disable");
             }
         }
         else if (action.equals("force")){
             if (args.size() < 2){
                 throw new CommandException("&c引数が足りません！ /endreset force world_name");
             }
             World world = Bukkit.getWorld(args.get(1));
             if (world == null){
                 throw new CommandException("&cそのワールドが見つかりません！");
             }
             if (world.getEnvironment() != Environment.THE_END){
                 throw new CommandException("&cそのワールドはエンドではありません！");
             }
 
             runResetTask(world);
         }
         else if (action.equals("list")){
             // All end worlds
             List<String> endWorlds = new ArrayList<String>();
             for (final World w : Bukkit.getWorlds()){
                 if (w != null && w.getEnvironment() == Environment.THE_END){
                     endWorlds.add(w.getName());
                 }
             }
             if (endWorlds.isEmpty()){
                 Util.message(sender, "&cエンドワールドが見つかりません");
             }else{
                 Util.message(sender, "&6エンドワールドリスト: &d" + StrUtil.join(endWorlds, "&7, &d"));
             }
 
             // Auto reset worlds
             if (worker.getWorldDataMap().isEmpty()){
                 Util.message(sender, "&c自動リセットが有効になっているワールドはありません");
             }
             else{
                 Util.message(sender, "&d 自動リセットが有効になっているワールドリスト: ");
                 for (Entry<String, EndResetWorldData> entry : worker.getWorldDataMap().entrySet()){
                     EndResetWorldData data = entry.getValue();
                     int interval = data.getInterval();
                     String next = TimeUtil.getReadableTime(TimeUtil.getDateByUnixSeconds(data.getNextReset()));
 
                     Util.message(sender, "&e - &d" + entry.getKey() + "&7: &6次回リセット &3" + next + "以降 &6(" + interval +"時間毎)");
                 }
             }
             return;
         }
         else{
            throw new CommandException("&c有効なアクションではありません (timer/force/ignore/list)");
         }
         worker.updateSaveFlag();
     }
 
     private void runResetTask(final World world){
         String wname = world.getName();
         Util.broadcastMessage("&dワールド '&6" + wname + "&d' をリセットしています... (" + sender.getName() + ")");
 
         Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
             @Override public void run(){
                 worker.regen(world);
             }
         }, 1L);
     }
 
     @Override
     public boolean permission(CommandSender sender) {
         return true;
     }
 }

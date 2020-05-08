 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.commands.other
  * Created: 2013/01/08 15:42:46
  */
 package net.syamn.sakuracmd.commands.other;
 
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.commands.BaseCommand;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.exception.CommandException;
 
 /**
  * SakuraCmdCommand (SakuraCmdCommand.java)
  * @author syam(syamn)
  */
 public class SakuraCmdCommand extends BaseCommand{
     public SakuraCmdCommand(){
         bePlayer = false;
         name = "sakuracmd";
         perm = Perms.SAKURACMD;
         argLength = 0;
         usage = "<- admin commands";
     }
     
     public void execute() throws CommandException{
         if (args.size() < 1){
            throw new CommandException("引数が足りません！");
         }
         final String func = args.remove(0);
         
         // reload
         if (func.equalsIgnoreCase("reload")){
             PlayerManager.saveAll();
             Util.message(sender, "&aPlayer data saved!");
             if (args.size() > 0 && args.get(0).equalsIgnoreCase("all")){
                 SCHelper.getInstance().reload();
                 Util.message(sender, "&aSakuraCmd plugin reloaded!");
             }else{
                 try {
                     SCHelper.getInstance().getConfig().loadConfig(false);
                 } catch (Exception ex) {
                     LogUtil.warning("an error occured while trying to load the config file.");
                     ex.printStackTrace();
                     return;
                 }
                 Util.message(sender, "&aSakuraCmd configuration reloaded!");
             }
             return; // reload
         }
         
         // save
         if (func.equalsIgnoreCase("save")){
             PlayerManager.saveAll(true);
             Util.message(sender, "&aPlayer data force saved!");
             return; // save
         }
         
        throw new CommandException("引数が不正です！");
     }
 }

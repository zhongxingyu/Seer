 /**
  * Advertise - Package: syam.advertise.command
  * Created: 2012/11/30 13:24:59
  */
 package syam.advertise.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import syam.advertise.Advertise;
 import syam.advertise.Perms;
 import syam.advertise.announce.Ad;
 import syam.advertise.database.Database;
 import syam.advertise.exception.CommandException;
 import syam.advertise.util.Actions;
 import syam.advertise.util.Util;
 
 /**
  * RemoveCommand (RemoveCommand.java)
  * @author syam(syamn)
  */
 public class RemoveCommand extends BaseCommand {
     public RemoveCommand() {
         bePlayer = false;
         name = "remove";
         argLength = 1;
         usage = "<Ads ID> <- remove your advertise";
     }
 
     @Override
     public void execute() throws CommandException {
         // check id
         if (!Util.isInteger(args.get(0))){
             throw new CommandException("&cNot a number: " + args.get(0));
         }
         final int data_id = Integer.parseInt(args.get(0));
         if (data_id <= 0){
             throw new CommandException("&cInvalid number: " + data_id);
         }
 
         Ad ad;
         try{
             ad= new Ad(data_id);
         }catch (IllegalArgumentException ex){
             throw new CommandException("&c広告ID " + data_id + " が見つかりません！");
         }
 
         // bypass check
         boolean other = false;
         if (sender instanceof Player){
            if (!ad.getPlayerName().equals(player.getName())){
                 if (!Perms.REMOVE_OTHER.has(sender)){
                     throw new CommandException("&c指定したIDはあなたの広告ではありません！");
                 }else{
                     other = true;
                 }
             }
         }else{
             other = true;
         }
 
         if (ad.getStatus() != 0 || ad.getExpired() <= Util.getCurrentUnixSec()){
             throw new CommandException("&c指定したIDはアクティブ広告ではありません！");
         }
 
         // remove
         plugin.getManager().removeAdvertise(data_id, other);
 
         // send message
         Actions.message(sender, "&a次の広告(#" + data_id + ")を削除しました！");
         Actions.message(sender, "&7->&f " + ad.getText());
 
         if (other){
             Player p = Bukkit.getPlayerExact(ad.getPlayerName());
             if (p != null && p.isOnline()){
                 Actions.message(p, msgPrefix + "&c次の広告(#" + data_id + ")はスタッフ &6" + sender.getName() + "&cによって削除されました");
                 Actions.message(p, "&7->&f " + ad.getText());
             }
         }
     }
 
     @Override
     public boolean permission() {
         return (Perms.REMOVE.has(sender) || Perms.REMOVE_OTHER.has(sender));
     }
 }

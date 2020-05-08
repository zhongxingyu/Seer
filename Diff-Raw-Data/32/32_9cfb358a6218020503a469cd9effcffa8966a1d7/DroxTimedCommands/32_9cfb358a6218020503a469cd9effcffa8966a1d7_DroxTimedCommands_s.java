 package de.hydrox.bukkit.DroxPerms;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.hydrox.bukkit.DroxPerms.data.IDataProvider;
 
 public class DroxTimedCommands implements CommandExecutor {
 
     private DroxPerms plugin;
    private IDataProvider dp;
 
     public DroxTimedCommands(DroxPerms plugin) {
         this.plugin = plugin;
        dp = plugin.dataProvider;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String lbl,
             String[] args) {
         if (!(sender.hasPermission("droxperms.timed"))) {
             sender.sendMessage("You don't have permission to modify timed data.");
             return true;
         }
         //Promote user along a track
         if (args[0].equalsIgnoreCase("promote") && args.length == 4) {
             String player = args[1];
             String track = args[2];
             if(!isModifySelf(sender,player)){sender.sendMessage("Cannot modify self");return true;}
             
             long time = Long.parseLong(args[3]);
             if (player == null) {
                 sender.sendMessage(ChatColor.RED + "Operation unsuccessfull. non-unique username given or player not online?");
                 return true;
             }
 
            if(!dp.setTimedTrack(sender, player, track, time)){
                 sender.sendMessage(ChatColor.RED + "Operation unsuccessful.");
             }
 
         }
 
         //add a timed subgroup to a user
         if (args[0].equalsIgnoreCase("addsub") && args.length == 4) {
             String player = args[1];
             String subgroup = args[2];
             if(!isModifySelf(sender,player)){sender.sendMessage("Cannot modify self");return true;}
             
             long time = Long.parseLong(args[3]);
             if (player == null) {
                 sender.sendMessage(ChatColor.RED + "Operation unsuccessfull. non-unique username given or player not online?");
                 return true;
             }
 
            if(!dp.addTimedSubgroup(sender, player, subgroup, time)){
                 sender.sendMessage(ChatColor.RED + "Operation unsuccessful.");
             }
 
         }
 
         //
         if (args[0].equalsIgnoreCase("info") && args.length == 2) {
             String player = args[1];
            String track = dp.getTimedTrack(sender, player);
            long expires = dp.getTimedTrackExpires(sender, player);
 
            Map<String,Long> sg = dp.getTimedSubgroups(sender, player);
             if(sg==null){sender.sendMessage(ChatColor.RED + "Operation unsuccessful");}
 
             if(track !=null){
                 sender.sendMessage("TRACK " + track + " " + expires + " expires: " + new SimpleDateFormat().format(new Date(expires * 1000L)));
             }
 
             for(Entry<String,Long> e : sg.entrySet()){
                 sender.sendMessage("SUBGROUP " + e.getKey() + " " + e.getValue() + " expires " + new SimpleDateFormat().format(new Date(e.getValue() * 1000L)));
             }
             
 
         }
 
         //process player times and demote as nessecary
         if (args[0].equalsIgnoreCase("checktimed") && args.length == 2) {
             String player = args[1];
            if(!dp.processTimes(sender, player)){
                 sender.sendMessage(ChatColor.RED + "Operation unsuccessful");
             }
             
             return true;
         }
 
         return true;
     }
 
     /**
      * Check if sender is the player to be modified, if so, check permission
      * @param sender
      * @param name
      * @return true if user has permission to modify themselves/the target, false otherwise
      */
     private boolean isModifySelf(CommandSender sender,String name){
         return sender instanceof Player ? ((Player)sender).getName().equals(name) ? ((Player)sender).hasPermission("droxperms.players.self") : true : true;
         
         
     }
 }

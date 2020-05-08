 package net.lahwran.bukkit.capsystem;
 
 import net.lahwran.capsystem.Capability;
 import net.lahwran.capsystem.Capsystem;
 import net.lahwran.capsystem.Commsystem;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class Listener extends PlayerListener implements CommandExecutor
 {
 
     public final Main plugin;
 
     public Listener(final Main plugin)
     {
         this.plugin = plugin;
     }
 
     private static final String colorEncode(int v)
     {
         String hex = String.format("%x", v);
         StringBuilder newstring = new StringBuilder();
         for(int i=0; i<hex.length(); i++)
         {
             newstring.append(Capsystem.colorchar+hex.substring(i, i+1));
         }
         return newstring.toString();
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         //Ask client to tell about themselves
         Player player = event.getPlayer();
        player.sendRawMessage(Capsystem.prefix + colorEncode(Capsystem.protocolVersion));
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
     {
         if (!(sender instanceof Player)) {
             sender.sendMessage("You're not a player!");
             return true;
         }
         System.out.print(command.getName());
         if (command.getName().equals("@caps"))
         {
             for (String s:split)
             {
                 Capsystem.addCap((Player)sender, s);
             }
         }
         else if (command.getName().equals("@cap"))
         {
             if (split.length > 0 && split[0].equals("done"))
             {
                 Capsystem.sendServerCaps(Capsystem.prefix, (Player)sender);
             }
         }
         else if (command.getName().equals("@comm"))
         {
             StringBuilder full = new StringBuilder();
             for(int i=0; i<split.length; i++)
             {
                 if (i>0)
                 {
                     full.append(" "); //TODO: if it does things to multiple spaces, this will silently mangle!!!
                 }
                 full.append(split[i]);
             }
             Commsystem.dispatch((Player)sender, full.toString());
         }
         return true;
     }
 }

 package io.github.alshain01.flags.command;
 
 import io.github.alshain01.flags.Flags;
 import io.github.alshain01.flags.Message;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permissible;
 
 public class SectorCommand implements CommandExecutor {
     private enum SectorCommandType {
         DELETE('d'), DELETEALL('a'), DELETETOPLEVEL('t');
 
         final char alias;
 
         SectorCommandType(char alias) {
             this.alias = alias;
         }
 
         static SectorCommandType get(String name) {
             for(SectorCommandType t : SectorCommandType.values()) {
                 if(name.toLowerCase().equals(t.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(t.alias))) {
                     return t;
                 }
             }
             return null;
         }
 
         boolean hasPermission(Permissible permissible) {
             return permissible.hasPermission("flags.sector." + this.toString().toLowerCase());
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(!(sender instanceof Player)) {
             sender.sendMessage(Message.NoConsoleError.get());
             return true;
         }
 
          if(args.length < 1) {
             sender.sendMessage(getUsage(sender));
             return true;
         }
 
         final SectorCommandType cType = SectorCommandType.get(args[0]);
         if(cType == null) {
             sender.sendMessage(getUsage(sender));
             return true;
         }
 
         if(!cType.hasPermission(sender)) {
            sender.sendMessage(Message.FlagPermError.get().replace("{Type}", Message.Command.get()));
             return true;
         }
 
         switch(cType) {
             case DELETE:
                 sender.sendMessage(Flags.getSectorManager().delete(((Player)sender).getLocation())
                     ? Message.DeleteSector.get()
                     : Message.NoSectorError.get());
                 return true;
             case DELETETOPLEVEL:
                 sender.sendMessage(Flags.getSectorManager().deleteTopLevel(((Player)sender).getLocation())
                         ? Message.DeleteSector.get()
                         : Message.NoSectorError.get());
             case DELETEALL:
                 Flags.getSectorManager().clear();
                 sender.sendMessage(Message.DeleteAllSectors.get());
                 return true;
         }
         sender.sendMessage(getUsage(sender));
         return true;
     }
 
     private String getUsage(Permissible player) {
         StringBuilder helpText = new StringBuilder("/sector <");
         boolean first = true;
 
         for(SectorCommandType c : SectorCommandType.values()) {
             if(c.hasPermission(player)) {
                 if(!first) { helpText.append(" | "); }
                 helpText.append(c.toString().toLowerCase());
                 first = false;
             }
         }
         return helpText.append(">").toString();
     }
 }

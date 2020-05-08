 package info.bytecraft.commands;
 
 import static org.bukkit.ChatColor.GOLD;
 import static org.bukkit.ChatColor.WHITE;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.math.Point;
 import info.bytecraft.api.math.Rectangle;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.IZoneDAO;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Flag;
 import info.bytecraft.zones.Zone.Permission;
 
 public class TownCommand extends AbstractCommand
 {
 
     public TownCommand(Bytecraft instance)
     {
         super(instance, "town");
     }
     
     
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         Location loc = player.getLocation();
         Zone zone = plugin.getZoneAt(loc.getWorld(), new Point(loc.getBlockX(), loc.getBlockZ()));
         if(zone == null){
             player.sendMessage(ChatColor.RED + "You are not in a zone!");
             return true;
         }
         
         Permission perm = zone.getUser(player);
         
         if(args.length == 0){
             player.sendMessage(ChatColor.YELLOW + "You are currently in zone: " + zone.getName());
         }else if(args.length == 1){
             //town info
             if("info".equalsIgnoreCase(args[0])){
                 return info(player, zone);
             }
         }else if(args.length == 2){
             //town deluser name
             if ("deluser".equalsIgnoreCase(args[0])) {
                 if ((perm != null && perm == Permission.OWNER) || player.getRank().canEditZones()) {
                     
                     List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[1]);
                     if(cantidates.size() != 1){
                         return true;
                     }
                     BytecraftPlayer target = cantidates.get(0);
                    this.delUser(zone, target, player);
                     return true;
                 }
             }
         }else if(args.length == 3){
             //town adduser <player> rank
             //town flag <flag> <true/false>
             if("adduser".equalsIgnoreCase(args[0])){
                 if((perm != null && perm == Permission.OWNER) || player.getRank().canEditZones()){
                     
                     List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[1]);
                     if(cantidates.size() != 1){
                         return true;
                     }
                     
                     BytecraftPlayer target = cantidates.get(0);
                     
                     Permission otherPerm = Permission.fromString(args[2]);
                     if(otherPerm != null){
                         this.addUser(zone, player, target, otherPerm);
                         return true;
                     }else{
                         player.sendMessage(ChatColor.RED + "Invalid permission");
                         return true;
                     }
                 }
             }else if("flag".equalsIgnoreCase(args[0])){
                 if((perm != null && perm == Permission.OWNER) || player.getRank().canEditZones()){
                    Flag flag = Flag.fromString(args[1]);
                     if (flag == null) {
                         player.sendMessage(ChatColor.RED + "Flag not found");
                         return true;
                     }
                     boolean value = Boolean.parseBoolean(args[2]);
                     this.updateFlag(zone, flag, value);
                     player.sendMessage(ChatColor.RED + "[" + zone.getName() + "] Changed " + flag.name().toLowerCase() + " to " + value);
                 }
             }
         }
         
         return true;
     }
     
     private boolean info(BytecraftPlayer player, Zone zone)
     {
         Rectangle rect = zone.getRectangle();
         player.sendMessage(GOLD + "Information about " + zone.getName());
         player.sendMessage(GOLD + "ID: " + WHITE + zone.getId());
         player.sendMessage((GOLD + "Rect: " + WHITE + "("+ 
         rect.getLeft() + ", " + + rect.getTop() + ") (" 
                 + rect.getRight()+ ", " + + rect.getBottom() + ")").replace(",", ChatColor.GOLD + ","));
         player.sendMessage(GOLD
                 + "Enter: " + WHITE
                 + (!zone.hasFlag(Flag.WHITELIST) ? "Everyone (true)"
                         : "Only allowed (false)"));
         player.sendMessage(GOLD
                 + "Build: " + WHITE
                 + (zone.hasFlag(Flag.BUILD) ? "Everyone (true)"
                         : "Only makers (false)"));
         player.sendMessage(GOLD + "PVP: " + WHITE + zone.hasFlag(Flag.PVP));
        player.sendMessage(GOLD + "Hostiles: " + WHITE + zone.hasFlag(Flag.HOSTILES));
         player.sendMessage(GOLD + "Enter message: " + WHITE + zone.getEnterMessage());
         player.sendMessage(GOLD + "Exit Message: " + WHITE + zone.getExitMessage());
         return true;
     }
     
     private void addUser(Zone zone, BytecraftPlayer player, BytecraftPlayer target, Permission p)
     {
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             String addConfirm = p.getAddedConfirm();
             player.sendMessage(ChatColor.RED
                     + "[" + zone.getName() + "] "
                     + String.format(addConfirm, target.getDisplayName() + ChatColor.RED, zone.getName()));
 
                 String addNotif = p.getAddedNotif();
                 target.sendMessage(ChatColor.RED + "[" + zone.getName() + "] "
                         + String.format(addNotif, zone.getName()));
             dao.addUser(zone, target.getName(), p);
            zone.addPermissions(target.getName(), p);
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
     
     private boolean delUser(Zone zone, BytecraftPlayer target, BytecraftPlayer deleter)
     {
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             Permission p = zone.getUser(target);
             if (p == null) {
                 deleter.sendMessage(ChatColor.RED + "[" + zone.getName() + "] "
                         + target.getDisplayName() + ChatColor.RED
                         + " does not have any permissions in " + zone.getName());
                 return false;
             }
             String delConfirm = p.getDelConfirm();
             deleter.sendMessage(ChatColor.RED + "[" + zone.getName() + "] "
                     + String.format(delConfirm, target.getDisplayName() + ChatColor.RED, zone.getName()));
             
                 String delNotif = p.getDelNotif();
                 target.sendMessage(ChatColor.RED + "[" + zone.getName() + "] "  
                 + String.format(delNotif, zone.getName()));
                zone.removePermission(target.getName());
             return dao.deleteUser(zone, target.getName());
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
     
     private void updateFlag(Zone zone, Flag flag, boolean value)
     {
         zone.setFlag(flag, value);
         
         try(IContext ctx = plugin.createContext()){
             IZoneDAO dao = ctx.getZoneDAO();
             dao.updateFlag(zone, flag, String.valueOf(value));
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 }

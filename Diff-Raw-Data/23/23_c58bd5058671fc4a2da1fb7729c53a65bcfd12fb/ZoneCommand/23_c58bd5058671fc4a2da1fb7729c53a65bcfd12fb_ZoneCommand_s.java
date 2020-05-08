 package info.bytecraft.commands;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 
 import static org.bukkit.ChatColor.*;
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.math.Rectangle;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Flag;
 import info.bytecraft.zones.Zone.Permission;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.IZoneDAO;
 
 public class ZoneCommand extends AbstractCommand
 {
 
     public ZoneCommand(Bytecraft instance)
     {
         super(instance, "zone");
     }
 
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if (args.length == 2)// zone create|delete name
         {
             if ("create".equalsIgnoreCase(args[0])) {
                 if (player.getRank().canCreateZones()) {
                     if (!zoneExists(args[1])) {
                         if (createZone(player, args[1])) {
                             player.sendMessage(ChatColor.RED + "Created zone "
                                     + args[1]);
                             return true;
                         }
                     }
                 }
             }
             else if ("delete".equalsIgnoreCase(args[0])) {
                 if (player.getRank().canCreateZones()) {
                     if (!zoneExists(args[1])) {
                         player.sendMessage(ChatColor.RED + "Zone " + args[1]
                                 + " doesn't exist");
                     }
                     else {
                         deleteZone(args[1]);
                         player.sendMessage(ChatColor.RED + "Deleted zone "
                                 + args[1]);
                     }
                 }
             }else if("info".equalsIgnoreCase(args[0])){
                 if(!zoneExists(args[1])){
                     player.sendMessage(ChatColor.RED + "Zone " + args[0] + " not found");
                     return true;
                 }
                 Zone zone = plugin.getZone(args[1]);
                 Rectangle rect = zone.getRectangle();
                 player.sendMessage(GOLD + "Information about " + args[1]);
                 player.sendMessage(GOLD + "ID: " + WHITE + zone.getId());
                 player.sendMessage((GOLD + "Rect: " + WHITE + "("+ 
                 rect.getLeft() + ", " + + rect.getTop() + ") (" 
                         + rect.getRight()+ ", " + + rect.getBottom() + ")").replace(",", ChatColor.GOLD + ","));
                 player.sendMessage(GOLD
                         + "Enter: " + WHITE
                         + (zone.hasFlag(Flag.WHITELIST) ? "Everyone (true)"
                                 : "Only allowed (false)"));
                 player.sendMessage(GOLD
                         + "Build: " + WHITE
                         + (zone.hasFlag(Flag.BUILD) ? "Everyone (true)"
                                 : "Only makers (false)"));
                 player.sendMessage(GOLD + "PVP: " + WHITE + zone.hasFlag(Flag.PVP));
                player.sendMessage(GOLD + "Hostiles: " + WHITE + zone.hasFlag(Flag.HOSTILE));
                 player.sendMessage(GOLD + "Enter message: " + WHITE + zone.getEnterMessage());
                 player.sendMessage(GOLD + "Exit Message: " + WHITE + zone.getExitMessage());
                 return true;
             }
         } else if (args.length == 3){ //zone deluser zone player
             if("deluser".equalsIgnoreCase(args[0])){
                 if(!zoneExists(args[1])){
                     player.sendMessage(ChatColor.RED + "Zone not found");
                     return true;
                 }
                 Zone zone = plugin.getZone(args[1]);
                 Permission p = zone.getUser(player);
                 if ((p != null && p == Permission.OWNER) || player.getRank().canEditZones()) {
                     List<BytecraftPlayer> cantidates =
                             plugin.matchPlayer(args[2]);
                     if (cantidates.size() > 1) {
                         return true;
                     }
                     BytecraftPlayer target = cantidates.get(0);
 
                     this.delUser(zone, target, player);
                 }
             }
         } else if (args.length == 4) {
             if ("adduser".equalsIgnoreCase(args[0])) {
                 if (!zoneExists(args[1])) {
                     player.sendMessage(ChatColor.RED + "Zone " + args[1]
                             + " not found");
                     return true;
                 } 
                 
                 Zone zone = plugin.getZone(args[1]);
                 Permission p = zone.getUser(player);
                 if ((p != null && p == Permission.OWNER) || player.getRank().canEditZones()) {
                     String pattern = args[2];
 
                     List<BytecraftPlayer> cantidates =
                             plugin.matchPlayer(pattern);
                     if (cantidates.size() > 1) {
                         return true;
                     }
 
                     BytecraftPlayer target = cantidates.get(0);
 
                     Permission p2 = Permission.fromString(args[3]);
                     if (p2 == null) {
                         player.sendMessage(ChatColor.RED + args[3] + " is not a valid permission");
                     }else{
                         this.addUser(zone, target, p2, player);
                     }
                 }
             }else if(args[0].equalsIgnoreCase("flag")){
                 if(!zoneExists(args[1])){
                     player.sendMessage(ChatColor.RED + "Zone " + args[1] + " not found");
                     return true;
                 }//zone flag zone flag_name value
                 Zone zone = plugin.getZone(args[1]);
                 Permission p = zone.getUser(player);
                 if ((p != null && p == Permission.OWNER) || player.getRank().canEditZones()) {
                     Flag flag = Flag.valueOf(args[2].toUpperCase());
                     if (flag == null) {
                         player.sendMessage(ChatColor.RED + "Flag not found");
                         return true;
                     }
 
                     boolean value = Boolean.parseBoolean(args[3]);
                     this.updateFlag(zone, flag, value);
                     player.sendMessage(ChatColor.RED + "[" + zone.getName() + "] Changed " + flag.name().toLowerCase() + " to " + value);
                 }
             }
         }
         else if (args.length >= 4) {//zone entermsg zone message
             //zone entermsg test hello fuckers mother
             if (args[0].equalsIgnoreCase("entermsg")
                     || args[0].equalsIgnoreCase("exitmsg")) {
                 if(!zoneExists(args[1])){
                     player.sendMessage(ChatColor.RED + "Zone " + args[1] + " not found");
                     return true;
                 }
                 Zone zone = plugin.getZone(args[1]);
                 Permission p = zone.getUser(player);
                 if ((p != null && p == Permission.OWNER) || player.getRank().canEditZones()) {
                     Flag flag =
                             args[0].equalsIgnoreCase("entermsg") ? Flag.ENTERMSG
                                     : Flag.EXITMSG;
 
                     StringBuilder sb = new StringBuilder();
                     for (int i = 2; i < args.length; i++) {
                         sb.append(args[i] + " ");
                     }
 
                     this.changeSetting(zone, flag, sb.toString());
                     player.sendMessage(ChatColor.RED + "[" + zone.getName()
                             + "] Changed " + flag.name().toLowerCase() + " to " + sb.toString());
                 }
             }
         }
         return true;
     }
 
     private boolean createZone(BytecraftPlayer player, String name)
     {
         if (zoneExists(name)) {
             player.sendMessage(ChatColor.RED + "Zone " + name
                     + " already exists");
             return false;
         }
         Zone zone = new Zone(name);
         zone.setWorld(player.getWorld().getName());
         Block b1 = player.getZoneBlock1();
         Block b2 = player.getZoneBlock2();
         if(b1 == null || b2 == null){
             player.sendMessage(ChatColor.RED + "Please select 2 points");
             return true;
         }
         
         zone.setRectangle(new Rectangle(b1.getX(), b1.getZ(), b2.getX(), b2.getZ()));
         
         try (IContext ctx = plugin.createContext()) {
                 IZoneDAO dao = ctx.getZoneDAO();
                 List<Zone> zones = plugin.getZones(player.getWorld().getName());
                 for (Zone other : zones) {
                 if(other.getName().equalsIgnoreCase(zone.getName())){
                     continue;
                 }
                     if (zone.intersects(other)) {
                         player.sendMessage(ChatColor.RED
                                 + "Zone intersects with zone: " + other.getName()
                                 + " . Please try somewhere else.");
                         return true;
                     }
                 }
                 dao.createZone(zone, player);
                 plugin.addZone(zone);
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
         return true;
     }
 
     private boolean deleteZone(String name)
     {
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             dao.deleteZone(name);
             plugin.deleteZone(plugin.getZone(name));
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
         return true;
     }
 
     private boolean zoneExists(String name)
     {
         return plugin.getZone(name) != null;
     }
 
     private void changeSetting(Zone zone, Flag flag, String value)
     {
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             dao.updateFlag(zone, flag, value);
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void addUser(Zone zone, BytecraftPlayer target, Permission p,
             BytecraftPlayer player)
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

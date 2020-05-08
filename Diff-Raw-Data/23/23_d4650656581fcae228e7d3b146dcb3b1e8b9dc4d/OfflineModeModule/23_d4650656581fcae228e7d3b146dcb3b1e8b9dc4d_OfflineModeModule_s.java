 package com.thevoxelbox.voxelguest;
 
 import com.thevoxelbox.commands.Command;
 import com.thevoxelbox.commands.CommandPermission;
 import com.thevoxelbox.voxelguest.modules.MetaData;
 import com.thevoxelbox.voxelguest.modules.Module;
 import com.thevoxelbox.voxelguest.modules.ModuleEvent;
 import com.thevoxelbox.voxelguest.players.GuestPlayer;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 @MetaData(name="Offline Mode", description="Manage your server in offline mode!")
 public class OfflineModeModule extends Module {
     protected List<String> needsUnlock = new ArrayList<String>();
     
     protected YamlConfiguration tempBan = new YamlConfiguration();
     protected static HashMap<String, Long> timemap = new HashMap<String, Long>();
     protected static List<String> banned = new LinkedList<String>();
     
     private static File f = new File("plugins/VoxelGuest/tempban.yml");
     
     @Override
     public Module install() {
         if (OfflineModeModule.class.isAnnotationPresent(MetaData.class)) {
             MetaData md = OfflineModeModule.class.getAnnotation(MetaData.class);
             return new OfflineModeModule(md.name(), md.description());
         } else
             return null;
     }
     
     public OfflineModeModule(String name, String description) {
         this.name = name;
         this.description = description;
     }
     
     @Override
     public String getLoadMessage() {
         return "Offline mode manager loaded";
     }
     
     @Override
     public void enable() {
         if (!f.exists()) {
             try {
                 f.createNewFile();
                 return;
             } catch (IOException ex) {
                 VoxelGuest.log("Could not create new " + f.getAbsolutePath(), 2);
                 return;
             }
         }
         
         try {
             tempBan.load(f);
         } catch (FileNotFoundException ex) {
             return;
         } catch (IOException ex) {
             return;
         } catch (InvalidConfigurationException ex) {
             return;
         }
         
         setEnabled(true);
     }
     
     class Commands {
         @Command(aliases={"opass", "offlinepass"},
                 bounds={0, -1},
                 help="For a player, set your offline password using §c/opass [password]\n" +
                 "On the console, set another's password using §c/opass [player] [password]",
                 playerOnly=false)
         @CommandPermission(permission="voxelguest.offline.opass")
         public void offlinePass(CommandSender cs, String[] args) {
             if (cs instanceof Player) {
                 Player p = (Player) cs;
                 GuestPlayer gp = VoxelGuest.getGuestPlayer(p);
                 String concat = "";
                 
                 for (int i = 0; i < args.length; i++) {
                     concat = concat + args[i] + " ";
                 }
                 
                 concat = concat.trim();
                 
                 try {
                     setPassword(gp.getPlayer().getName(), concat);
                     cs.sendMessage(ChatColor.GRAY + "Offline password set to: " + ChatColor.GREEN + concat);
                 } catch (CouldNotStoreOfflinePasswordException ex) {
                     cs.sendMessage(ex.getMessage());
                 }
             }
         }
         
         @Command(aliases={"opardon", "offlinepardon"},
                 bounds={1, 1},
                 help="To pardon an offline mode ban on the console, use §/opardon [player]"
         )
         public void offlinePardon(CommandSender cs, String[] args) {
             if (cs instanceof Player) {
                 cs.sendMessage("§cConsole-only command");
                 return;
             }
             
             removeTempBan(args[0]);
             cs.sendMessage("Removed temporary ban on " + args[0]);
         }
     }
     
     @ModuleEvent(event=PlayerJoinEvent.class)
    public void onPlayerJoin(PlayerJoinEvent event) {
         if (isActive() && !needsUnlock.contains(event.getPlayer().getName())) {
             GuestPlayer gp = new GuestPlayer(event.getPlayer());
             
             if (gp.get(VoxelGuest.getPluginId(VoxelGuest.getInstance()), "offline-password") == null) {
                 event.getPlayer().kickPlayer("You do not have an offline mode account.");
                return;
             } else if (isTempBanned(event.getPlayer().getName())) {
                 event.getPlayer().kickPlayer("You have been banned for hacking this account.");
                return;
             }
             
             needsUnlock.add(event.getPlayer().getName());
             event.getPlayer().sendMessage(ChatColor.RED + "Please enter your offline password.");
         }
     }
     
     @ModuleEvent(event=PlayerQuitEvent.class)
    public void onPlayerQuit(PlayerQuitEvent event) {
         if (isActive() && needsUnlock.contains(event.getPlayer().getName()))
             needsUnlock.remove(event.getPlayer().getName());
     }
     
     @ModuleEvent(event=PlayerKickEvent.class)
    public void onPlayerKick(PlayerKickEvent event) {
         if (isActive() && needsUnlock.contains(event.getPlayer().getName()))
             needsUnlock.remove(event.getPlayer().getName());
     }
     
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
         if (isActive() && needsUnlock.contains(event.getPlayer().getName()))
             if (isPassword(event.getPlayer().getName(), event.getFormat())) {
                 needsUnlock.remove(event.getPlayer().getName());
                 event.getPlayer().sendMessage(ChatColor.GREEN + "You have unlocked your player.");
             } else {
                 event.getPlayer().kickPlayer("You have entered your password incorrectly.");
                 logTempBan(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().toString(), System.currentTimeMillis());
             }   
     }
     
     public boolean isActive() {
         return !Bukkit.getServer().getOnlineMode();
     }
     
     public boolean isPassword(String name, String input) {
         String protectedPass = VoxelGuest.getGuestPlayer(Bukkit.getPlayer(name)).get(VoxelGuest.getPluginId(VoxelGuest.getInstance()), "offline-password").toString();
         String test = "";
         byte[] shhash = new byte[40];
 
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-1");
             md.update(input.getBytes("iso-8859-1"), 0, input.length());
             shhash = md.digest();
             test = convertToHex(shhash);
             
             if (test.equals(protectedPass))
                 return true;
         } catch (NoSuchAlgorithmException e) {
             return false;
         } catch (UnsupportedEncodingException e) {
             return false;
         }
         
         return false;
     }
     
     public void setPassword(String name, String input) throws CouldNotStoreOfflinePasswordException {
         byte[] shhash = new byte[40];
         String store = "";
         
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-1");
             md.update(input.getBytes("iso-8859-1"), 0, input.length());
             shhash = md.digest();
             store = convertToHex(shhash);
             
             VoxelGuest.getGuestPlayer(Bukkit.getPlayer(name)).store(VoxelGuest.getPluginId(VoxelGuest.getInstance()), "offline-password", store);
         } catch (NoSuchAlgorithmException e) {
             throw new CouldNotStoreOfflinePasswordException("Fatal error in storage - NoSuchAlgorithmException");
         } catch (UnsupportedEncodingException e) {
             throw new CouldNotStoreOfflinePasswordException("Fatal error in storage - UnsupportedEncodingException");
         }
     }
     
     public boolean isTempBanned(String player) {
         int counts = tempBan.getInt(player+".counts", 0);
         
         if (timemap.containsKey(player) && (System.currentTimeMillis() - timemap.get(player)) > 1800000) {
             tempBan.set(player+".counts", 0);
             
             if (banned.contains(player))
                 banned.remove(player);
             
             return false;
         }
         
         if (banned.contains(player) && timemap.containsKey(player) && (System.currentTimeMillis() - timemap.get(player)) <= 1800000) {
             return true;
         } else if (banned.contains(player)) {
             timemap.put(player, System.currentTimeMillis());
             return true;
         } else if (counts >= 3) {
             timemap.put(player, System.currentTimeMillis());
             banned.add(player);
             return true;
         } else {
             return false;
         }
     }
     
     public void logTempBan(String player, String IP, Long time) {
         tempBan.set(player+".IP", IP);
 
         int counts = tempBan.getInt(player+".counts", 0);
         if (counts < 3) {
             counts++;
             
             if (banned.contains(player))
                 banned.remove(player);
         }
         
         tempBan.set(player+".counts", counts);
         timemap.put(player, System.currentTimeMillis());
         
         
         if (counts >= 3)
             banned.add(player);
         try {
             tempBan.save(f);
         } catch (IOException ex) {
             VoxelGuest.log("Could not log temp ban on " + player, 1);
         }
     }
     
     public void removeTempBan(String player) {
         tempBan.set(player+".IP", null);
         tempBan.set(player+".counts", null);
         tempBan.set(player+".ban", null);
         tempBan.set(player, null);
         
         if (banned.contains(player))
             banned.remove(player);
         try {
             tempBan.save(f);
         } catch (IOException ex) {
             VoxelGuest.log("Could not remove temp ban on " + player, 1);
         }
     }
     
     private static String convertToHex(byte[] data) { 
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < data.length; i++) { 
             int halfbyte = (data[i] >>> 4) & 0x0F;
             int two_halfs = 0;
             do { 
                 if ((0 <= halfbyte) && (halfbyte <= 9)) 
                     buf.append((char) ('0' + halfbyte));
                 else 
                     buf.append((char) ('a' + (halfbyte - 10)));
                 halfbyte = data[i] & 0x0F;
             } while(two_halfs++ < 1);
         } 
         return buf.toString();
     }
 }

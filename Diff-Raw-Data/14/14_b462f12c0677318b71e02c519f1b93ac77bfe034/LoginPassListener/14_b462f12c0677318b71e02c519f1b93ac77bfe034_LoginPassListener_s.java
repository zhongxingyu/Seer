 package mccarthy.brian.loginpass;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.canarymod.api.entity.Player;
 import net.canarymod.hook.Hook;
 import net.canarymod.hook.command.PlayerCommandHook;
 import net.canarymod.hook.entity.DamageHook;
 import net.canarymod.hook.player.ChatHook;
 import net.canarymod.hook.player.ConnectionHook;
 import net.canarymod.hook.player.ItemHook;
 import net.canarymod.hook.player.LeftClickHook;
 import net.canarymod.hook.player.LoginChecksHook;
 import net.canarymod.hook.player.PlayerMoveHook;
 import net.canarymod.hook.player.RightClickHook;
 import net.canarymod.plugin.PluginListener;
 
 /**
  * 
  * @author Brian McCarthy
  *
  */
 public class LoginPassListener extends PluginListener {
     private List<String> unAuthedPlayers = new ArrayList<String>();
 
     public Hook onCommand(PlayerCommandHook hook) {
         if (hook.getCommand()[0].equalsIgnoreCase("/LoginPass") || hook.getCommand()[0].equalsIgnoreCase("/LP")) {
             try {
                 if (hook.getCommand()[1].equalsIgnoreCase("login")) {
                     if (!LoginPass.passes.containsKey(hook.getPlayer().getName())) {
                         LoginPassActions.sendMessage(hook.getPlayer(), "Please create a password first.");
                         hook.setCancelled();
                         return hook;
                     }
                     String hexHash = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[2]));
                     String storedHash = LoginPass.passes.getString(hook.getPlayer().getName());
                     if (!hexHash.equals(storedHash)) {
                         LoginPassActions.sendMessage(hook.getPlayer(), "Passes do not match!");
                         hook.setCancelled();
                         return hook;
                     } else {
                         LoginPassActions.sendMessage(hook.getPlayer(), "Congratulations, you have successfully logged in.");
                         unAuthedPlayers.remove(hook.getPlayer().getName());
                         //LoginPassActions.sendMessage(hook.getPlayer(), "Contains: " + unAuthedPlayers.contains(hook.getPlayer().getName()) + ", " + unAuthedPlayers.size());
                         hook.setCancelled();
                         return hook;
                     }
                     
                 } /*else if (hook.getCommand()[1].equalsIgnoreCase("test")) {
                     byte[] bytes = LoginPassActions.hash("WWOL", "TEST");
                     StringBuilder sb = new StringBuilder();
                     for (byte currByte : bytes) {
                         sb.append(currByte);
                     }
                     LoginPassActions.sendMessage(hook.getPlayer(), "Hash: " + sb.toString());
                     LoginPassActions.sendMessage(hook.getPlayer(), "Hash (hex): " + LoginPassActions.toHex(bytes));
                     
                 }*/ else if (hook.getCommand()[1].equalsIgnoreCase("create")) {
                     if (LoginPass.passes.containsKey(hook.getPlayer().getName())) {
                         LoginPassActions.sendMessage(hook.getPlayer(), "You have already set a password.");
                         hook.setCancelled();
                         return hook;
                     }
                     String hexHash = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[2]));
                     String hexHash2 = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[3]));
                     if (!hexHash.equals(hexHash2)) {
                         LoginPassActions.sendMessage(hook.getPlayer(), "Please make sure you type you password the same both times!.");
                         hook.setCancelled();
                         return hook;
                     }
                     LoginPass.passes.setString(hook.getPlayer().getName(), hexHash);
                     LoginPass.passes.save();
                     unAuthedPlayers.remove(hook.getPlayer().getName());
                     LoginPassActions.sendMessage(hook.getPlayer(), "Pass created! Auto logged in.");
                     
                 } /*else if (hook.getCommand()[1].equalsIgnoreCase("test2")) {
                     String hexHash = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[2]));
                     String hexHash2 = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[3]));
                     LoginPassActions.sendMessage(hook.getPlayer(), "Hash1: " + hexHash);
                     LoginPassActions.sendMessage(hook.getPlayer(), "Hash2: " + hexHash2);
                     LoginPassActions.sendMessage(hook.getPlayer(), "Same?: " + hexHash.equals(hexHash2));
                     
                 }*/ else if (hook.getCommand()[1].equalsIgnoreCase("change")) {
                     String hexHash = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[2]));
                     String storedHash = LoginPass.passes.getString(hook.getPlayer().getName());
                     String hexHash2 = LoginPassActions.toHex(LoginPassActions.hash(hook.getPlayer().getName(), hook.getCommand()[3]));
                     if (!storedHash.equals(hexHash)) {
                         LoginPassActions.sendMessage(hook.getPlayer(), "Please enter your old pass correctly.");
                         hook.setCancelled();
                         return hook;
                     }
                     LoginPass.passes.setString(hook.getPlayer().getName(), hexHash2);
                     LoginPass.passes.save();
                     LoginPassActions.sendMessage(hook.getPlayer(), "Pass changed!");
                 }
             } catch (Exception e) {
                 LoginPassActions.sendHelp(hook.getPlayer());
             }
             hook.setCancelled();
         } else {
             if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_COMMAND) {
                 hook.setCancelled();
             }
             return hook;
         }
         return hook;
     }
 
     public Hook onPlayerMove(PlayerMoveHook hook) {
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_MOVEMENT) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not move while not logged in!");
             hook.getPlayer().teleportTo(hook.getFrom().getX(), hook.getFrom().getY(), hook.getFrom().getZ(), hook.getPlayer().getPitch(), hook.getPlayer().getRotation());
         }
         return hook;
     }
 
     public Hook onDamage(DamageHook hook) {
         if (hook.getDefender().isPlayer()) {
             Player player = hook.getDefender().getPlayer();
             if (unAuthedPlayers.contains(player.getName()) && LoginPassSettings.BLOCK_DAMAGE) {
                 if (hook.getAttacker().isPlayer()) {
                     Player attacker = hook.getAttacker().getPlayer();
                     LoginPassActions.sendMessage(attacker, "That person has not logged in so you can not attack them!");
                 }
                 hook.setCancelled();
             }
         } else if (hook.getAttacker().isPlayer()) {
             Player player = (Player) hook.getAttacker();
             if (unAuthedPlayers.contains(player.getName()) && LoginPassSettings.BLOCK_ATTACK) {
                 LoginPassActions.sendMessage(player, "You can not attack while not logged in!");
                 hook.setCancelled();
             }
         }
         return hook;
     }
 
     public Hook onPlayerConnect(ConnectionHook hook) {
         unAuthedPlayers.add(hook.getPlayer().getName());
         if (LoginPass.passes.containsKey(hook.getPlayer().getName())) {
             LoginPassActions.sendMessage(hook.getPlayer(), "Please login using /loginpass login <pass>.");
         } else {
             LoginPassActions.sendMessage(hook.getPlayer(), "Please create a password using /loginpass create <pass> <pass>.");
         }
         return hook;
     }
 
     public Hook onItemDrop(ItemHook hook) {
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_ITEM_DROP) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not drop items while not logged in!");
             hook.setCancelled();
             hook.getPlayer().getInventory().updateInventory();
         }
         return hook;
     }
 
     public Hook onItemPickup(ItemHook hook) {
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_ITEM_PICKUP) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not pickup items while not logged in!");
             hook.setCancelled();
             hook.getPlayer().getInventory().updateInventory();
         }
         return hook;
     }
 
     public Hook onChat(ChatHook hook) {
         if (hook.getMessage().toLowerCase().startsWith("loginpass") || hook.getMessage().toLowerCase().startsWith("lp")) {
             LoginPassActions.sendMessage(hook.getPlayer(), "Please start your messages with something other than \"loginpass\" or \"lp\"!");
             LoginPassActions.sendMessage(hook.getPlayer(), "This is to make sure you don't accidently enter your pass in chat.");
             hook.setCancelled();
         }
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_CHAT) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not chat while not logged it!");
             hook.setCancelled();
         }
         return hook;
     }
     
     public Hook onBlockRightClicked(RightClickHook hook) {
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_RIGHTCLICK) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not right click while not logged in!");
             hook.setCancelled();
             hook.getBlockPlaced().update();
         }
         return hook;
     }
     
     public Hook onBlockLeftClicked(LeftClickHook hook) {
         if (unAuthedPlayers.contains(hook.getPlayer().getName()) && LoginPassSettings.BLOCK_LEFTCLICK) {
             LoginPassActions.sendMessage(hook.getPlayer(), "You can not left click while not logged in!");
             hook.setCancelled();
             hook.getBlock().update();
         }
         return hook;
     }
     
     public Hook onLoginChecks(LoginChecksHook hook) {
         if (LoginPassSettings.CHECK_IP) {
             boolean correctIP = LoginPassActions.ipMatches(hook.getName(), hook.getIp());
             if (!correctIP) {
                 hook.setKickReason("Your IP does not match an aloud one!");
             }
         }
         return hook;
     }
 }

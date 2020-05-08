 package ca.agnate.EconXP;
 
 import java.util.List;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import ca.agnate.EconXP.EconXPMsg.Msg;
 
 public class EconXPCommands implements CommandExecutor {
     
 	private boolean meanAdmins;
     private Server server;
     private EconXP plugin;
     
     public EconXPCommands (EconXP aPlugin) {
         plugin = aPlugin;
         server = plugin.getServer();
         meanAdmins = (server.getPluginManager().getPlugin("Mean Admins") != null);
     }
     
     /**
      * Handles all command parsing.
      * Unrecognized commands return false, giving the sender a
      * list of valid commands (from plugin.yml).
      */
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	// If there's nothing after the /econxp, quit.
         if (args.length == 0) { return false; }
     	
     	// Get base command.
     	String base = args[0].toLowerCase();
     	
     	// Search the Node list for the valid command (checks for partial spelling).
         Node cmd = getBaseCommand( base );
     	
     	// Play nice with Mean Admins.
         if (meanAdmins  &&  cmd == null) {
             server.getPluginManager().getPlugin("Mean Admins").onCommand(sender, command, label, args);
             return true;
         }
         
         // If no command is found (ie. it's null),
         if ( cmd == null ) {
             EconXP.sendMsg(sender, Msg.COMMAND_NOT_FOUND.get());
             return false;
         }
         
         // Determine if the sender is a player (and an op), or the console.
         //boolean player  = (sender instanceof Player);
         //boolean op      = player && ((Player) sender).isOp();
         boolean console = (sender instanceof ConsoleCommandSender);
         
         // Cast the sender to Player if possible.
         //Player p = (player) ? (Player)sender : null;
         
         // Grab the command base and any arguments.
         String arg1 = (args.length > 1) ? args[1].toLowerCase() : "";
         String arg2 = (args.length > 2) ? args[2].toLowerCase() : "";
         String arg3 = (args.length > 3) ? args[3].toLowerCase() : "";
         
         
        // Check for self-balance based on permissions.
         if ( cmd.equals(Node.BALANCE)  &&  (arg1.isEmpty()  ||  arg1.toLowerCase().equals(sender.getName().toLowerCase())) ) {
         	// If they can't access the balance of others, check themselves.
         	cmd = Node.BALANCESELF;
         }
         
         // Verify that player has permission to use this.
         if ( plugin.has(sender, cmd) == false ) {
             EconXP.sendMsg(sender, Msg.COMMAND_NOT_ALLOWED.get(sender.getName()) );
             return true;
         }
         
         // Command:  /econxp add <player> <amount>
         if ( cmd.equals(Node.ADD) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the amount.
             int amount = validateAmount( sender, arg2 );
             if ( amount < 0 ) { return true; }
             
             // Add the experience to the player.
             EconXP.sendMsg( sender, Msg.PLAYER_ADD.get(target.getName(), ""+plugin.addExp(target, amount)) );
             return true;
         }
         // Command: /econxp subtract <player> <amount>
         else if ( cmd.equals(Node.SUBTRACT) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the amount.
             int amount = validateAmount( sender, arg2 );
             if ( amount < 0 ) { return true; }
             
             // Subtract the experience from the player.
             EconXP.sendMsg( sender, Msg.PLAYER_SUBTRACT.get(target.getName(), ""+plugin.removeExp(target, amount)) );
             return true;
         }
         // Command:  /econxp set <player> <amount>
         else if ( cmd.equals(Node.SET) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the amount.
             int amount = validateAmount( sender, arg2 );
             if ( amount < 0 ) { return true; }
             
             // Add the experience to the player.
             EconXP.sendMsg( sender, Msg.PLAYER_SET.get(target.getName(), ""+plugin.setExp(target, amount)) );
             return true;
         }
         // Command: /econxp balance <player>
         else if ( cmd.equals(Node.BALANCE) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer( sender, arg1 );
             
         	if ( target == null ) { return true; }
             
             // Report back the player's balance.
             EconXP.sendMsg( sender, Msg.PLAYER_BALANCE.get(target.getName(), ""+plugin.getExp(target)) );
             return true;
         }
         // Command: /econxp balanceself
         else if ( cmd.equals(Node.BALANCESELF) ) {
         	// If this is the console, error.
         	if ( console ) {
         		EconXP.sendMsg( sender, Msg.COMMAND_CONSOLE_BALANCESELF_NOT_ALLOWED.get() );
         		return true;
         	}
         	
         	// Report back the player's balance.
             EconXP.sendMsg( sender, Msg.PLAYER_BALANCESELF.get(sender.getName(), ""+plugin.getExp((Player)sender)) );
             return true;
         }
         // Command: /econxp clear <player>
         else if ( cmd.equals(Node.CLEAR) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Report back how much was cleared.
             EconXP.sendMsg( sender, Msg.PLAYER_CLEAR.get(target.getName(), ""+plugin.clearExp(target)) );
             return true;
         }
         // Command: /econxp version
         else if ( cmd.equals(Node.VERSION) ) {
             // Report back the version number.
             EconXP.sendMsg( sender, "Version #"+plugin.getDescription().getVersion() );
             return true;
         }
         // Command: /econxp give <receiver> <amount>
         else if ( cmd.equals(Node.GIVE) ) {
         	// If the console typed this, cancel it.
         	if ( console ) {
         		EconXP.sendMsg( sender, Msg.COMMAND_CONSOLE_GIVE_NOT_ALLOWED.get() );
         		return true;
         	}
         	
             // Get the receiving player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the amount.
             int amount = validateAmount( sender, arg2 );
             if ( amount < 0 ) { return true; }
             
             // Transfer the experience from the player to the receiver.
             EconXP.sendMsg( sender, Msg.PLAYER_GIVE.get(target.getName(), ""+plugin.giveExp((Player) sender, target, amount)) );
             return true;
         }
         // Command: /econxp transfer <giver> <amount> <receiver>
         else if ( cmd.equals(Node.TRANSFER) ) {
         	// Get the giving player.
             OfflinePlayer giver = validatePlayer(sender, arg1 );
             if ( giver == null ) { return true; }
             
             // Get the amount.
             int amount = validateAmount( sender, arg2 );
             if ( amount < 0 ) { return true; }
             
             // Get the receiving player.
             OfflinePlayer receiver = validatePlayer(sender, arg3 );
             if ( receiver == null ) { return true; }
             
             // Transfer the experience from the player to the receiver.
             EconXP.sendMsg( sender, Msg.PLAYER_TRANSFER.get(giver.getName(), ""+plugin.giveExp(giver, receiver, amount), receiver.getName()) );
             return true;
         }
         // Command: /econxp multiply <player> <multiplier>
         else if ( cmd.equals(Node.MULTIPLY) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the multiplier.
             float multiplier = validateFloatAmount( sender, arg2 );
             if ( multiplier < 0 ) { return true; }
             
             // Multiply the experience from the player.
             EconXP.sendMsg( sender, Msg.PLAYER_MULTIPLY.get(target.getName(), ""+multiplier, ""+plugin.multiplyExp(target, multiplier)) );
             return true;
         }
         // Command: /econxp divide <player> <divisor>
         else if ( cmd.equals(Node.DIVIDE) ) {
             // Get the target player.
         	OfflinePlayer target = validatePlayer(sender, arg1 );
             if ( target == null ) { return true; }
             
             // Get the divisor.
             float divisor = validateFloatAmount( sender, arg2 );
             if ( divisor < 0 ) { return true; }
             
             // Divide the experience from the player.
             EconXP.sendMsg( sender, Msg.PLAYER_DIVIDE.get(target.getName(), ""+divisor, ""+plugin.divideExp(target, divisor)) );
             return true;
         }
         
         // Command wasn't handled.
         EconXP.sendMsg(sender, Msg.COMMAND_UNHANDLED.get(cmd.toString()));
         return true;
     }
     
     private Node getBaseCommand ( String base ) {
     	List<Node> list = Node.allNodes();
     	
     	for (Node node : list) {
     		if ( contains(base, node.toString()) ) {
                 return node;
             }
     	}
     	
     	return null;
     }
     private OfflinePlayer validatePlayer (CommandSender sender, String player) {
         // If not player name was given,
         if ( player.isEmpty() ) {
             EconXP.sendMsg(sender, Msg.PLAYER_NOT_GIVEN.get());
             return null;
         }
         
         // Get the target player.
         Player target = plugin.getOnlinePlayer( player );
         
         // If there's a target,
         if ( target != null ) {
         	return target;
         }
         
         OfflinePlayer offTarget = plugin.getOfflinePlayer( player );
         
        if ( offTarget != null ) {
         	return offTarget;
         }
         
         EconXP.sendMsg(sender, Msg.PLAYER_NOT_EXISTS.get(player));
         return null;
     }
     private int validateAmount (CommandSender sender, String value) {
         // If no amount was given,
         if ( value.isEmpty() ) {
             EconXP.sendMsg(sender, Msg.AMOUNT_NOT_GIVEN.get());
             return -1;
         }
         
         // Get the amount.
         int amount = Integer.parseInt(value);
         
         // If the amount is <= zero,
         if ( amount <= 0 ) {
             EconXP.sendMsg(sender, Msg.AMOUNT_INVALID.get(value));
             return -1;
         }
         
         // Success!
         return amount;
     }
     private float validateFloatAmount (CommandSender sender, String value) {
         // If no amount was given,
         if ( value.isEmpty() ) {
             EconXP.sendMsg(sender, Msg.AMOUNT_NOT_GIVEN.get());
             return -1;
         }
         
         // Get the amount.
         float amount = Float.parseFloat(value);
         
         // If the amount is <= zero,
         if ( amount <= 0 ) {
             EconXP.sendMsg(sender, Msg.AMOUNT_INVALID.get(value));
             return -1;
         }
         
         // Success!
         return amount;
     }
     
     private boolean contains (String str, String command) {
         return command.toLowerCase().contains(str.toLowerCase());
     }
 }

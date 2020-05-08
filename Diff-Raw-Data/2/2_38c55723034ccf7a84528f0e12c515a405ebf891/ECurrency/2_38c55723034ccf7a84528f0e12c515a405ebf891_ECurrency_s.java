 package net.year4000.ecurrency;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sk89q.commandbook.CommandBook;
 import com.sk89q.commandbook.session.SessionComponent;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.zachsthings.libcomponents.ComponentInformation;
 import com.zachsthings.libcomponents.Depend;
 import com.zachsthings.libcomponents.InjectComponent;
 import com.zachsthings.libcomponents.bukkit.BukkitComponent;
 import com.zachsthings.libcomponents.config.ConfigurationBase;
 import com.zachsthings.libcomponents.config.Setting;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 @ComponentInformation(friendlyName = "eCurrency", desc = "Players balance to buy and sell on the server.")
 @Depend(components = SessionComponent.class)
 public class ECurrency extends BukkitComponent implements Listener {
 	
 	private LocalConfiguration config;
 	private String conponent = "[eCurrency]";
 	@InjectComponent private SessionComponent sessions;
 	
     
     public void enable() {
     	config = configure(new LocalConfiguration());
         CommandBook.registerEvents(this);
         registerCommands(Commands.class);
         Logger.getLogger(conponent).log(Level.INFO, conponent+" has been enabled.");
     }
 
 	
     public void reload() {
         super.reload();
         configure(config);
         Logger.getLogger(conponent).log(Level.INFO, conponent+" has been reloaded.");
     }
     
     public static class LocalConfiguration extends ConfigurationBase {
     	@Setting("start-balance") public int startBalance = 50;
     	@Setting("money-name") public String moneyName = "Credits";
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onJoin(PlayerJoinEvent event){
     	Player player = event.getPlayer();
     	ECurrencySession session = sessions.getSession(ECurrencySession.class, player);
     	
     	if(session.getBalance() == 0){
     		session.setBalance(config.startBalance);
     	}
     }
     
     public class Commands{
     	 @Command(aliases = {"balance", "bal", "money"}, usage = "[add|remove|set] [amount] [player]",
                  desc = "All balance related command", flags = "", max = 3)
     	 @CommandPermissions({"ecurrency.balance", "ecurrency.balance.add", "ecurrency.balance.remove", "ecurrency.balance.set", "ecurrency.balance.other"})
          public void balance(CommandContext args, CommandSender player) throws CommandException {
     		 ECurrencySession session = null;
 
     		 if(args.argsLength() == 3){
     			 if(Bukkit.getOfflinePlayer(args.getString(2)).hasPlayedBefore()){
     				 session = sessions.getSession(ECurrencySession.class, Bukkit.getOfflinePlayer(args.getString(2)).getPlayer());
     			 } else{
     				 player.sendMessage(ChatColor.YELLOW + "That player has not logon before.");
     			 }
     		 } else if(args.argsLength() == 2){
     			 session = sessions.getSession(ECurrencySession.class, player);
 	    		 if(args.getString(0).equalsIgnoreCase("add")){
 	    			CommandBook.inst().checkPermission(player, "ecurrency.balance.add");
 					int addTo = session.getBalance() + args.getInteger(1);
 	    			 if(addTo < 0){
 	    				 session.setBalance(0);
 	    			 }else{
 		 				 session.setBalance(addTo);
 	    			 }
 	    		 } else if(args.getString(0).equalsIgnoreCase("remove")){
 	    			 CommandBook.inst().checkPermission(player, "ecurrency.balance.remove");
 	    			 int removeTo = session.getBalance() - args.getInteger(1);
 	    			 if(removeTo < 0){
 	    				 session.setBalance(0);
 	    			 }else{
 		 				 session.setBalance(removeTo);
 	    			 }
 	    		 } else if(args.getString(0).equalsIgnoreCase("set")){
 	    			 CommandBook.inst().checkPermission(player, "ecurrency.balance.set");
 	    			 int setTo = args.getInteger(1);
 	    			 if(setTo < 0){
 	    				 session.setBalance(0);
 	    			 }else{
 		 				 session.setBalance(setTo);
 	    			 }
 	    		 }
 	    		 player.sendMessage(ChatColor.YELLOW + session.getOwner().getName() + "'s balance is " + session.getBalance() + " " + config.moneyName.toLowerCase() + ".");
     		 } else if(args.argsLength() == 1){
     			 CommandBook.inst().checkPermission(player, "ecurrency.balance.other");
     			 Player p = Bukkit.getPlayerExact(args.getString(0));
     			 session = sessions.getSession(ECurrencySession.class, p);
    			 player.sendMessage(ChatColor.YELLOW + p.getName() + " balance is " + session.getBalance() + " " + config.moneyName.toLowerCase() + ".");
     		 } else{
     			 session = sessions.getSession(ECurrencySession.class, player);
     			 player.sendMessage(ChatColor.YELLOW + "Your balance is " + session.getBalance() + " " + config.moneyName.toLowerCase() + ".");
     		 }
          }
     }
 }

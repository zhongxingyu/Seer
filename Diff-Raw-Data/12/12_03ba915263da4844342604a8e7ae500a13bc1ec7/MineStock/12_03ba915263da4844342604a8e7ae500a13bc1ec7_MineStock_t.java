 /****************************************************\
  * MineStock Bukkit/Vault Plugin					*
  * Author: abujaki21							    *
  * Version: 0.1.0 Pre-alpha 132892					*
  * Description: Stock trading plugin for vault and	*
  * 	Bukkit-enabled minecraft servers				*
 \****************************************************/
 
 package io.github.abujaki.minestock;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MineStock extends JavaPlugin {
 
     private static final Logger log = Logger.getLogger("Minecraft");
     public static Economy econ = null;
     public static Permission perms = null;
     public static Chat chat = null;
     //private TransactionEngine transactionEngine = new TransactionEngine();
     
     
 //Enabler and Disabler
     @Override
     public void onDisable() {
         log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
         //transactionEngine.save(); will save unresolved orders to file 
     }
 
     @Override
     public void onEnable() {
         if (!setupEconomy() ) {
             log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         setupPermissions();
         setupChat();
         //transactionEngine.load(); will load the unresolved orders from file
     }
  
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
     	if(command.getLabel().equalsIgnoreCase("stockbuy")){
     		//stockbuy stock amount priceeach
     		if(!(sender instanceof Player)){
     			sender.sendMessage("This command requires you to be a logged in player");
     			return true;
     		}
     		else{
     	    	//Check to see if we have 3 arguments
     			if (args.length >= 4) {
     		           sender.sendMessage("Too many arguments");
     		           return false;
     		        } 
     		        if (args.length <= 2) {
     		           sender.sendMessage("Not enough arguments");
     		           return false;
     		        }
     		    //Convert args 2 and 3 to integers
     	    	try{
     		    int amt = Integer.parseInt(args[1]);
     	    	int price = Integer.parseInt(args[2]);    	    	
     	    		if ((amt <= 0)||(price <= 0)){ //Smartass check
     	    			sender.sendMessage("You cannot buy with negative values.");
     	    			return false;
     	    		}
     	    		else return buyStock(sender, args[0], amt, price);	//-----------Finally run the buystock code
     	    	}
     	    	catch(NumberFormatException e){
     	    		return false; //arg 1 or 2 was not a number
     	    	}
     		}
     	}else if(command.getLabel().equalsIgnoreCase("stocksell")){
     		//stockbuy stock amount priceeach
     		if(!(sender instanceof Player)){
     			sender.sendMessage("This command requires you to be a logged in player");
     			return true;
     		}
     		else{
     			//Check to see if we have 3 arguments
     			if (args.length >= 4) {
     		           sender.sendMessage("Too many arguments");
     		           return false;
     		        } 
     		        if (args.length <= 2) {
     		           sender.sendMessage("Not enough arguments");
     		           return false;
     		        }
     		    //Convert args 2 and 3 to integers
     	    	try{
     		    int amt = Integer.parseInt(args[1]);
     	    	int price = Integer.parseInt(args[2]);    	    	
     	    		if ((amt <= 0)||(price <= 0)){ //Smartass check
     	    			sender.sendMessage("You cannot buy with negative values.");
     	    			return false;
     	    		}
     	    		else return sellStock(sender, args[0], amt, price); //-----------Finally run the sellstock code
     	    	}
     	    	catch(NumberFormatException e){
     	    		return false; //arg 1 or 2 was not a number
     	    	}
     		}
     	}else return false;
     }
 
     private boolean buyStock(CommandSender sender, String stock, int amount, int priceEach){
     	//function to place a stock order
     	sender.sendMessage("You wish to buy " + String.valueOf(amount) + " of " + stock + " stock at " + String.valueOf(priceEach) + ".");
     	if(econ.getBalance(sender.getName()) >= (amount * priceEach)){	
    		EconomyResponse r = econ.withdrawPlayer(sender.getName(), amount*priceEach);
    		//Copypasta from example code
    		if(r.transactionSuccess()) {
    			sender.sendMessage(String.format("You paid %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
    			//transactionEngine.submitBuyOrder(new StockOrder(stock, amount, priceEach, sender.getName()); //Or something to that effect
    		} else {
    			sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
    		}
     	} else {
     		sender.sendMessage("You don't have enough money to make that order");
     	}
     	return true;
     }
     
     @SuppressWarnings("unused") //TODO - Remove when stock-checking code is implemented
 	private boolean sellStock(CommandSender sender, String stock, int amount, int priceEach){
     	//function to put stocks up for sale
     	sender.sendMessage("You wish to sell " + String.valueOf(amount) + " of " + stock + " stock at " + String.valueOf(priceEach) + ".");
     	if(true){//TODO: Check to see if there's enough stocks to sell
    		EconomyResponse r = econ.depositPlayer(sender.getName(), amount * priceEach);
     		
     		//copypasta from example code
     		if(r.transactionSuccess()) {
     			sender.sendMessage(String.format("You were paid %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
     		} else {
     			sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
     		}
     	} else {
     		sender.sendMessage("You don't have that many stocks to sell");
     		return false;
     	}
     	return true;
     }
     
 //Vault Setup Block
     private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     private boolean setupChat() {
         RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
         try{
         chat = rsp.getProvider();
         return chat != null;
         }
         catch(NullPointerException e){
         	log.severe("Error in getting chat provider");
         	return false;
         }
     }
 
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
 //End of Vault Setups
     
 //Demo Vault code for Reference
 /*
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         if(!(sender instanceof Player)) {
             log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
             return true;
         }
 
         Player player = (Player) sender;
 
         if(command.getLabel().equals("test-economy")) {
             // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
             sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
             EconomyResponse r = econ.depositPlayer(player.getName(), 1.05);
             if(r.transactionSuccess()) {
                 sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
             } else {
                 sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
             }
             return true;
         } else if(command.getLabel().equals("test-permission")) {
             // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
             if(perms.has(player, "example.plugin.awesome")) {
                 sender.sendMessage("You are awesome!");
             } else {
                 sender.sendMessage("You suck!");
             }
             return true;
         } else {
             return false;
         }}
 */
     //End of Demo code
     
 }//End of Class

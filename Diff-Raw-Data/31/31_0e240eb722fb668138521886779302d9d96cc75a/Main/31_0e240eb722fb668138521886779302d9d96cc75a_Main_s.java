 package de.zwinkie.regcheck;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 /**
  * 
  * @author Cyan & Tuqe
  * @since August 2012
  */
 
 // Begin main class
 public class Main extends JavaPlugin{
 	
 	public void onEnable(){
 		//do something
 		getLogger().info("RegCheck started.");
 	}
 	
 	public void onDisable(){
 		//stop it!
 		getLogger().info("RegCheck has been disabled.");
 	}
 	
 	
 	// I definitely didn't get this quite right, the if else nesting is
 	// getting the better of me.
 	// I was basically trying to do this: Declare onCommand, if command = /check then return true,
 	// if not return false. If true, check permission, check args, then follow through with
 	// the meat of the command.
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	if(cmd.getName().equalsIgnoreCase("check")){ // If the player types /check, do this:
     		//Check if player has permission
     	    if(player.hasPermission("regcheck.lookup")) {
     	    	//If yes, follow through. Check arg amount:
         		if (args.length > 1) {
     	           sender.sendMessage("Incorrect usage.");
      	           return false;
     	        } 
     	        if (args.length < 1) {
      	           sender.sendMessage("You must specify a username to lookup.");
      	           return false;
      	        }
    	     }else{
     	        sender.sendMessage("You are not allowed to use that command.")
     	        return false;
    	     }
    		return true;
    	} //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
     	return false; 
     }
 
 
 	
 }

 package uk.codingbadgers.bsign.listener;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 import uk.codingbadgers.bsign.bSignModule;
 import uk.codingbadgers.bsign.sign.CommandSign;
 import uk.codingbadgers.bsign.sign.InfoSign;
 import uk.codingbadgers.bsign.sign.Sign;
 import uk.codingbadgers.bsign.sign.WebSign;
 
 /**
  * The listener interface for receiving block events.
  * The class that is interested in processing a block
  * event implements this interface. When
  * the block event occurs, that object's appropriate
  * method is invoked.
  */
 public class BlockListener implements Listener {
 	
 	/**
 	 * On sign change. Used to create an instance of a new bSign
 	 *
 	 * @param event the event
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onSignChange(SignChangeEvent event) {
 		
 		final Player creator = (Player)event.getPlayer();
 		final Location signLocation = event.getBlock().getLocation();
 		
 		Sign newSign = null;
 		
 		if (event.getLine(0).equalsIgnoreCase("--weblink--")) {
 			
 			// see if they have permissions to use signs
 			if (!bSignModule.hasPermission(creator, "bfundamental.bsign.create.web"))
 				return;
 			
 			newSign = new WebSign(creator, signLocation);
 			bSignModule.sendMessage("bSign", creator, bSignModule.MODULE.getLanguageValue("WEBSIGN-SETUP"));
 		}
 		else if (event.getLine(0).equalsIgnoreCase("--command--")) {
 			
 			// see if they have permissions to use signs
 			if (!bSignModule.hasPermission(creator, "bfundamental.bsign.create.command"))
 				return;
 			
 			newSign = new CommandSign(creator, signLocation);
 			bSignModule.sendMessage("bSign", creator, bSignModule.MODULE.getLanguageValue("COMMANDSIGN-SETUP"));
 		}
 		else if (event.getLine(0).equalsIgnoreCase("--info--")) {
 			
 			// see if they have permissions to use signs
 			if (!bSignModule.hasPermission(creator, "bfundamental.bsign.create.info"))
 				return;
 			
 			newSign = new InfoSign(creator, signLocation);
 			bSignModule.sendMessage("bSign", creator, bSignModule.MODULE.getLanguageValue("INFOSIGN-SETUP"));
 		}
 		
 		bSignModule.SIGNS.add(newSign);
 		
 	}
 	
 	/**
 	 * On block break. Used to destroy an instance of a bSign
 	 *
 	 * @param event the event
 	 */
 	@EventHandler(priority = EventPriority.LOW)
 	public void onBlockBreak(BlockBreakEvent event) {
 
 		if (bSignModule.SIGNS == null)
 			return;
 		
 		Block block = event.getBlock();
 		
 		// if its not a sign, we dont care.
 		if (!(block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST))
 			return;
 		
 		// find the sign at the location
 		Sign contextSign = null;
 		for (Sign sign : bSignModule.SIGNS) {
 			
 			if (sign == null)
 				continue;
 			
 			if (sign.getLocation().equals(block.getLocation())) {
 				contextSign = sign;
 				break;
 			}
 		}
 		
 		// not a bsign so carry on
 		if (contextSign == null)
 			return;
 		
 		String location = contextSign.getLocation().getX() + "," + contextSign.getLocation().getY() + "," + contextSign.getLocation().getZ() + "," + contextSign.getLocation().getWorld().getName();
 		
 		String addSign = "DELETE FROM bSign " +
 				"WHERE Location = '" +
 				location + 
 				"'";
		bSignModule.DATABASE.query(addSign);
 		
 		bSignModule.SIGNS.remove(contextSign);
 		bSignModule.sendMessage("bSign", event.getPlayer(), bSignModule.MODULE.getLanguageValue("SIGN-REMOVED"));
 		
 	}
 
 }

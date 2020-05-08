 package net.gamerservices.rpchat;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.object.Resident;
 
 public class LocalMessage implements CommandExecutor {
 
 	private rpchat parent;
 	private Towny towny;
 	
 	public LocalMessage(rpchat rpchat) {
 		// TODO Auto-generated constructor stub
 		this.parent = rpchat;
 		this.towny = this.parent.getTowny();
 	}
 
 	public static String arrayToString(String[] a, String separator) {
 	    String result = "";
 	    if (a.length > 0) {
 	        result = a[0];    // start with the first element
 	        for (int i=1; i<a.length; i++) {
 	            result = result + separator + a[i];
 	        }
 	    }
 	    return result;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
 			String[] arg3) {
 		// TODO Auto-generated method stub
 		String message = arrayToString(arg3," ");
 		if (message.compareTo("") == 0)
 		{
 			return false;
 		}
 		// find the player
 		try 
 		{
 			Player player = parent.getServer().getPlayer(arg0.getName());
 			
 			// now that we have the player lets grab the players towny info
 			
 			Resident res = towny.getTownyUniverse().getResident(player.getName());
 			
 			
 			
 			String town = "";
 			try 
 			{
 				town = res.getTown().getName();
 			} 
 			catch (Exception e)
 			{
 				town = ""; // no town
 			}
 			
 			String nation = "";
 			
 			try 
 			{
 				nation = res.getTown().getNation().getName();
 			} 
 			catch (Exception e)
 			{
 				nation = ""; // no nation
 			}
 			
 			
 			
 
 			// find players around player
 			int count = 0;
 			
 			for (Player p : player.getWorld().getPlayers())
 			{
 				if (p.equals(player))
 				{
 					// talking to self
 					if (p.getWorld().getName().compareTo("Redstone") == 0)
 					{
 						p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] [YELL] " + player.getName() + ChatColor.YELLOW + " yells '" + message + "'");
 					} else {
 						p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] [RP] " + player.getName() + ChatColor.YELLOW + " says '" + message + "'");						
 					}
 
 					
 				} else {
 					// not talking to self
 					// first we need to check if this player is in the redstone world, if so they receive the message by deafult
 					if (p.getWorld().getName().compareTo("Redstone") == 0)
 					{
 						// ARE in Redstone world - do none distance based checking
 						p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] [YELL] " + player.getName() + ChatColor.YELLOW + " yells '" + message + "'");
 						count++;
 					} else {
 						// NOT in Redstone world - do distance based checking
 					
 						// this player is in the players world, are they in range?
 						double x1 = p.getLocation().getX();
 		                double y1 = p.getLocation().getY();
 		                double z1 = p.getLocation().getZ();
 		
 		                double x2 = player.getLocation().getX();
 		                double y2 = player.getLocation().getY();
 		                double z2 = player.getLocation().getZ();
 						
 						int xdist = (int) (x1 - x2);
 		                int ydist = (int) (y1 - y2);
 		                int zdist = (int) (z1 - z2);
 		                
 		                if ((xdist < -300 || xdist > 300) || (ydist < -300 || ydist > 300) || (zdist < -300 || zdist > 300)) {
 		                    // out of range to do this
 		                	
 		                } else {
 		                	
		                	p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] [RP] " + player.getName() + ChatColor.YELLOW + " says '" + message + "'");
 		                	count++;
 		                }
 					}
 					
 				}
 			}
 			
 			if (count < 1)
 			{
 				player.sendMessage(ChatColor.GRAY + "* You speak but nobody hears you (Use worldwide /ooc <msg> instead.)");
 			}
 			
 			return true;
 		} 
 		catch (Exception e)
 		{
 			// could not find player
 			System.out.println("[RPChat Error]: " + e.getMessage());
 
 		}
 				
 		return false;
 	}
 
 }

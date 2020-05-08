 package net.gamerservices.rpchat;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.palmergames.bukkit.towny.NotRegisteredException;
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.object.Resident;
 
 import de.bananaco.permissions.Permissions;
 import de.bananaco.permissions.info.InfoReader;
 
 public class OOCMessage implements CommandExecutor {
 
 
 	private Towny towny;
 	private rpchat parent;
     
 	public OOCMessage(rpchat rpchat) {
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
 		// find the player
 		try 
 		{
 			Player player = parent.getServer().getPlayer(arg0.getName());
 			if (message.compareTo("") > 0)
 			{
 				sendOOC(player,message);
 				return true;
 			} else {
 				return false;
 			}
 			
 		} 
 		catch (Exception e)
 		{
 			// could not find player
 			System.out.println("[RPChat Error]: " + e.getMessage());
 		}
 				
 		return false;
 	}
 	
 	
 	
 
 	public void sendOOC(Player player, String message) {
 		// TODO Auto-generated method stub
 		// now that we have the player lets grab the players towny info
 		
 		Resident res;
 		try {
 			res = towny.getTownyUniverse().getResident(player.getName());
 			String tag = "";
 			
 			try {
 				InfoReader info = Permissions.getInfoReader();
 				tag = info.getPrefix(player);			
 				tag = this.parent.formatString(tag);
 			}
 			catch (Exception e)
 			{
 				tag = "Guest";
 			}
 			
 			
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
					p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] ["+tag+"]"+ ChatColor.WHITE+"[OOC] " + player.getName() + ChatColor.WHITE + ": " + message);
 				} else {
 							// not talking to self
 							// ARE in the same world and not self - no distance based checking required
					p.sendMessage("[" + ChatColor.GOLD + nation + ChatColor.WHITE + "|" + ChatColor.AQUA + town + ChatColor.WHITE + "] ["+tag+"]"+ ChatColor.WHITE+"[OOC] " + player.getName() + ChatColor.WHITE + ": " + message);
 							count++;
 				}
 			}
 						
 			if (count < 1)
 			{
 				player.sendMessage(ChatColor.GRAY + "* You speak but nobody hears you (There is no-one in this world, try another world or use global chat /g)");
 			}
 			
 			
 		} catch (NotRegisteredException e) {
 			// TODO Auto-generated catch block
 			// player not registered so do not send message
 		}
 
 		
 	}
 
 }

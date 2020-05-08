 package net.lotrcraft.minepermit;
 
 import java.util.ArrayList;
 
 import net.lotrcraft.minepermit.miner.Miner;
 import net.lotrcraft.minepermit.plot.Plot;
 import net.lotrcraft.minepermit.world.PermitWorld;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 public class PlotCommandInterpreter implements CommandExecutor {
 
 	private MinePermit mp;
 
 	public PlotCommandInterpreter(MinePermit minePermit) {
 		this.mp = minePermit;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		
 		if(sender instanceof ConsoleCommandSender){
 			mp.log.info("Console cannot use the Plot command");
 			return true;
 		}
 		
 		if(args.length == 0)
 			return false;
 		
 		if(args[0].equalsIgnoreCase("list")){
 			
 			
 			if(args.length > 1){
 				World w = mp.getServer().getWorld(args[1]);
 				
 				if(w == null){
 					sender.sendMessage(ChatColor.RED + "Invalid World!");
 					return true;
 				}
 				
 				PermitWorld pw = mp.getPWM().getPermitWorld(w);
 				if(pw == null){
 					sender.sendMessage(ChatColor.DARK_GRAY + "That world is not able to have plots.");
					return true;
 				}
 				
 				if(pw.getPlots().size() == 0)
 					sender.sendMessage("You have no plots in that world!");
 				else
 					sender.sendMessage("Your Plots in world " + w.getName() + ":");
 				
 				for (Plot p : pw.getPlots()){
 					if(p.getOwner().equals(sender.getName()))
 						sender.sendMessage(ChatColor.GREEN + p.toString());
 					else if(p.canUse(sender.getName()))
 						sender.sendMessage(ChatColor.YELLOW + p.toString());
 				}
 				
 				return true;
 			} else {
 				
 				ArrayList<Plot> plots = mp.getMinerManager().getMiner(sender.getName()).getPlots();
 				
 				if(plots.size() == 0)
 					sender.sendMessage("You have no plots!");
 				else
 					sender.sendMessage("Your Plots:");
 				
 				for (Plot p : plots){
 					sender.sendMessage(ChatColor.GREEN + p.toString());
 				}
 				
 				return true;
 			}
 		}
 		
 		if(args[0].equalsIgnoreCase("buy")){
 			
 			if(args.length < 3)
 				return false;
 			
 			PermitWorld pw = mp.getPWM().getPermitWorld(mp.getServer().getPlayer(sender.getName()).getLocation().getWorld());
 			
 			if(pw == null){
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 			
 			String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 			
 			if(loc1.length < 2 || loc2.length < 2)
 				return false;
 			
 			Plot new1;
 			try {
 				new1 = pw.getNewPlot(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]));
 			} catch (NumberFormatException e){
 				return false;
 			}
 			
 			if(new1 == null){
 				sender.sendMessage(ChatColor.DARK_RED + "Plot invalid!");
 				return true;
 			}
 			
 			//TODO: Charge player
 			
 			if(!pw.registerPlot(new1)){
 				sender.sendMessage(ChatColor.DARK_RED + "Couldn't buy plot!");
 				return true;
 			}
 			
 			Miner m = mp.getMinerManager().getMiner(sender.getName());
 			
 			if(args.length >= 4)
 				new1.setName(args[3]);
 			else {
 				String base = "Plot";
 				int num = 1;
 				while(m.getPlot(base + num) != null)
 					num++;
 				
 				new1.setName(base + num);
 			}
 			
 			sender.sendMessage(ChatColor.GOLD + "You have bought a new plot! It has been named " + new1.getName());
 			
 			m.addPlot(new1);
 			
 			new1.createCorners();
 			
 			return true;
 			
 		}
 		
 		if(args[0].equalsIgnoreCase("price")){
 			if(args.length < 3)
 				return false;
 			
 			PermitWorld pw = mp.getPWM().getPermitWorld(mp.getServer().getPlayer(sender.getName()).getLocation().getWorld());
 			
 			if(pw == null){
 				sender.sendMessage("This world does not allow you to buy plots!");
 				return true;
 			}
 			
 			String[] loc1 = args[1].split(":"), loc2 = args[2].split(":");
 			
 			if(loc1.length < 2 || loc2.length < 2)
 				return false;
 			
 			Plot new1;
 			try {
 				new1 = pw.getNewPlot(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]));
 			} catch (NumberFormatException e){
 				return false;
 			}
 			
 			if(new1 == null){
 				sender.sendMessage(ChatColor.DARK_RED + "Plot invalid!");
 				return true;
 			}
 			
 			sender.sendMessage(ChatColor.GOLD + "The price to buy this plot is " + new1.calculateCost());
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 }

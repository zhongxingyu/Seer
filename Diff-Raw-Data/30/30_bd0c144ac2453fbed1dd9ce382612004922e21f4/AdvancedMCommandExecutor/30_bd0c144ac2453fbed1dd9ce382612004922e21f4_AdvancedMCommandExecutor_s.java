 package me.groot314.AdvancedM;
 
 
 import java.util.ArrayList;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 
 public class AdvancedMCommandExecutor implements CommandExecutor {
 	
 	static ArrayList<String> output = new ArrayList<String>();
 
 	private AdvancedM plugin; //pointer to methods in main class
 	public AdvancedMCommandExecutor(AdvancedM plugin){
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if(cmd.getName().equalsIgnoreCase("am")){
 			switch (args.length) {
 			case 0:
 				return false; 
 			case 1:
				if(args.equals("ipl") || args.equals("iplist")){
 					amListIp(plugin.iplist, plugin.plist, output, sender);
 				}
 				return true;
 			default:
 				break;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static void amListIp(ArrayList<String> lip,ArrayList<Player> pl, ArrayList<String> out,CommandSender sender){
 		for (int i = 0; i < lip.size(); i++) {
 			System.out.println(lip.get(i));
 			sender.sendMessage(lip.get(i) + ":");
 			
 			for (int j = 0; j < pl.size(); j++) {
				String pip = pl.get(j).getAddress().getAddress().getHostAddress();
				pip.replace("/", "");
				if(pip == lip.get(j)){
					out.add(pl.get(j).getName());
 				}
 			}
			System.out.println(output);
 			sender.sendMessage(output.toString());
			output.clear();
 		}
 	}
 	
 }

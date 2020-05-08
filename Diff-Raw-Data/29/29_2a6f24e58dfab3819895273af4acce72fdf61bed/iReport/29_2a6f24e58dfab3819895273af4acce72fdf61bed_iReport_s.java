 package iReport;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class iReport extends JavaPlugin {
 
     public iReport() {
     }
 
     @Override
     @SuppressWarnings("unused")
     public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
         String player = sender.getName();
         String target = args[0];
         if ((cmd.getName().equalsIgnoreCase("greport")) && (args.length == 1)) {
             if (!sender.hasPermission("ireport.greport")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission");
                 return true;
             }
             String already = (String) getConfig().get("reports.griefing." + player);
             sender.sendMessage(ChatColor.BLUE + "You successfully reported " + ChatColor.RED + target);
             getConfig().set("reports.griefing." + player, Rlocation.getxyz(this, args[0]) + "; " + target);
 
             saveConfig();
             for (int i = 0; i < sender.getServer().getOnlinePlayers().length; i++) {
            	if (sender.getServer().getOnlinePlayers()[i].isOp()) {
					sender.getServer().getOnlinePlayers()[i].sendMessage(ChatColor.RED+target+" has ben reported for grifing");
				}
			}
 
             return true;
         }
         if ((cmd.getName().equalsIgnoreCase("hreport")) && (args.length == 2)) {
             if (!sender.hasPermission("ireport.hreport")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission");
                 return true;
             }
             String already = (String) getConfig().get("reports.hacking." + player);
             getConfig().set("reports.hacking." + player, new StringBuilder("type: ").append(args[1]).toString() + "; " + target);
             sender.sendMessage(ChatColor.BLUE + "You successfully reported " + ChatColor.RED + target);
             saveConfig();
 
             for (int i = 0; i < sender.getServer().getOnlinePlayers().length; i++) {
            	if (sender.getServer().getOnlinePlayers()[i].isOp()) {
					sender.getServer().getOnlinePlayers()[i].sendMessage(ChatColor.RED+target+" has ben reported for hacking "+args[1]);
				}
			}
             return true;
         }
         if ((cmd.getName().equalsIgnoreCase("sreport")) && (args.length == 1)) {
             if (!sender.hasPermission("ireport.sreport")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission");
                 return true;
             }
             String already = (String) getConfig().get("reports.swearing." + player);
             getConfig().set("reports.swearing." + player, "; " + target);
             sender.sendMessage(ChatColor.BLUE + "You successfully reported " + ChatColor.RED + target);
             saveConfig();
 
             for (int i = 0; i < sender.getServer().getOnlinePlayers().length; i++) {
            	if (sender.getServer().getOnlinePlayers()[i].isOp()) {
					sender.getServer().getOnlinePlayers()[i].sendMessage(ChatColor.RED+target+" has ben reported for swearing");
				}
			}
             return true;
         }
 
         if (cmd.getName().equalsIgnoreCase("ireport")) {
             sender.sendMessage(ChatColor.YELLOW + "==============================");
             sender.sendMessage(ChatColor.BLUE + "/greport - Report a griefer");
             sender.sendMessage(ChatColor.BLUE + "/hreport - Report a hacker");
             sender.sendMessage(ChatColor.BLUE + "/sreport - Report a swearer");
             sender.sendMessage(ChatColor.BLUE + "/ireport - Show this help menu");
             sender.sendMessage(ChatColor.YELLOW + "==============================");
             sender.sendMessage(ChatColor.GREEN + "Created by tudse145");
     
             return true;
         }else
             return false;
     }
     
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         List<String> l = new ArrayList<String>();
         if (sender.isOp()) {
             l.add("hreport");
             l.add("greport");
             l.add("sreport");
		}
         if (sender.hasPermission("ireport.hreport")) {
             l.add("hreport");
         }
         if (sender.hasPermission("ireport.greport")) {
             l.add("greport");
         }
         if (sender.hasPermission("ireport.sreport")) {
             l.add("sreport");
         }
         l.add("ireport");
         return l;
     }
 
     @Override
     public void onEnable() {
         saveConfig();
         getConfig().options().copyDefaults(true);
 
     }
 }

 package iReport;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class iReport extends JavaPlugin {
 
     public static final List<String> REPORTLIST = new ArrayList<String>();
     MYSQL sql;
 
     public iReport() {
     }
 
     @Override
     @SuppressWarnings("unused")
     public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
         String player = sender.getName();
         String target = "";
         try {
             target = args[0];
         } catch (ArrayIndexOutOfBoundsException e) {
         }
         if ((cmd.getName().equalsIgnoreCase("greport")) && (args.length == 1)) {
             if (!sender.hasPermission("ireport.greport") && !sender.isOp()) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission");
                 return true;
             }
             String already = (String) getConfig().get("reports.griefing." + player);
             sender.sendMessage(ChatColor.BLUE + "You successfully reported " + ChatColor.RED + target);
             getConfig().set("reports.griefing." + player, Rlocation.getxyz(this, args[0]) + "; " + target);
 
             saveConfig();
             for (Player p : sender.getServer().getOnlinePlayers()) {
                 if (p.isOp() || p.hasPermission("iReport.seereport")) {
                     p.sendMessage(ChatColor.RED + player + " has reported " + target + " for griefing");
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
 
             for (Player p : sender.getServer().getOnlinePlayers()) {
                 if (p.isOp() || p.hasPermission("iReport.seereport")) {
                     p.sendMessage(ChatColor.RED + player + " has reported " + target + " for hacking " + args[1]);
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
 
             for (Player p : sender.getServer().getOnlinePlayers()) {
                 if (p.isOp() || p.hasPermission("iReport.seereport")) {
                     p.sendMessage(ChatColor.RED + player + " has reported " + target + " for swearing");
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
         }
         if (cmd.getName().equalsIgnoreCase("reports")) {
             try {
                 Scanner sc = new Scanner(new File("plugins/iReport/", "config.yml"));
                 while (sc.hasNext()) {
                     sender.sendMessage(sc.nextLine());
                 }
                 sc.close();
             } catch (FileNotFoundException e) {
                 
             }
 
            
             return true;
         }
         if (cmd.getName().equalsIgnoreCase("reports")) {
             return true;
         }
         return false;
     }
 
     public List<String> name() {
         List<String> l = new ArrayList<String>();
         return l;
 
     }
 
     public MYSQL getMYSQL() {
         PluginManager pm = this.getServer().getPluginManager();
         if (sql == null) {
             try {
                 sql = new MYSQL();
                 Reports Reports = new Reports(this);
                 pm.registerEvents(Reports, this);
                 this.getCommand("greport").setExecutor(Reports);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return this.sql;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         List<String> l = new ArrayList<String>();
         if (sender.isOp()) {
             l.add("hreport");
             l.add("greport");
             l.add("sreport");
            return l;
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
         // getMYSQL();
     }
 }

 package me.odium.simplehelptickets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.FileConfigurationOptions;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleHelpTickets extends JavaPlugin {
   Logger log = Logger.getLogger("Minecraft");
 
   // Custom Config  
   private FileConfiguration StorageConfig = null;
   private File StorageConfigFile = null;
 
   public void reloadStorageConfig() {
     if (StorageConfigFile == null) {
       StorageConfigFile = new File(getDataFolder(), "StorageConfig.yml");
     }
     StorageConfig = YamlConfiguration.loadConfiguration(StorageConfigFile);
 
     // Look for defaults in the jar
     InputStream defConfigStream = getResource("StorageConfig.yml");
     if (defConfigStream != null) {
       YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
       StorageConfig.setDefaults(defConfig);
     }
   }
   public FileConfiguration getStorageConfig() {
     if (StorageConfig == null) {
       reloadStorageConfig();
     }
     return StorageConfig;
   }
   public void saveStorageConfig() {
     if (StorageConfig == null || StorageConfigFile == null) {
       return;
     }
     try {
       StorageConfig.save(StorageConfigFile);
     } catch (IOException ex) {
       Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + StorageConfigFile, ex);
     }
   }
   // End Custom Config
 
   public static String getCurrentDTG (String string) {
     Calendar currentDate = Calendar.getInstance();
     SimpleDateFormat dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");    
     return dtgFormat.format (currentDate.getTime());
   }  
 
   public void onEnable(){    
     log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
     FileConfiguration cfg = getConfig();
     FileConfigurationOptions cfgOptions = cfg.options();
     cfgOptions.copyDefaults(true);
     cfgOptions.copyHeader(true);
     saveConfig();
     // Load Custom Config
     FileConfiguration ccfg = getStorageConfig();
     FileConfigurationOptions ccfgOptions = ccfg.options();
     ccfgOptions.copyDefaults(true);
     ccfgOptions.copyHeader(true);
     saveStorageConfig();
 
     // declare new listener
     new PListener(this);
   }
 
   public void onDisable(){ 
     log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled."); 
   }
 
   public class PListener implements Listener {
 
     public PListener(SimpleHelpTickets instance) {
       Plugin plugin = instance;
       Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin(PlayerJoinEvent event) {      
       boolean ShowTicketsOnJoin = getConfig().getBoolean("ShowTicketsOnJoin");
       if (ShowTicketsOnJoin == true) {
         Player player = event.getPlayer();
         if (player.hasPermission("sht.admin")) {
           int ticklength = getStorageConfig().getStringList("Tickets").size();
           if (ticklength == 0) {
             // DO NOTHING
           } else if(ticklength > 0) {
             player.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "There are currently " + ChatColor.GOLD + ticklength + ChatColor.GRAY + " open Help Tickets");
           }
         } 
       } else {
         // Do Some Nothing
       }
     }
   }
 
   public String myGetPlayerName(String name) { 
     Player caddPlayer = getServer().getPlayerExact(name);
     String pName;
     if(caddPlayer == null) {
       caddPlayer = getServer().getPlayer(name);
       if(caddPlayer == null) {
         pName = name;
       } else {
         pName = caddPlayer.getName();
       }
     } else {
       pName = caddPlayer.getName();
     }
     return pName;
   }
 
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     Player player = null;
     if (sender instanceof Player) {
       player = (Player) sender;
     }
 
     if(cmd.getName().equalsIgnoreCase("helptickets")){ 
       sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "SimpleHelpTickets" + " v" + getDescription().getVersion() + ChatColor.GOLD + " --");
       sender.sendMessage(ChatColor.BLUE + " /ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket. [Stand @ Location]");
       sender.sendMessage(ChatColor.BLUE + " /tickets" + ChatColor.WHITE + " - View Your Tickets.");
       sender.sendMessage(ChatColor.BLUE + " /checkticket <#>" + ChatColor.WHITE + " - Check one of your ticket's info.");
       sender.sendMessage(ChatColor.BLUE + " /closeticket <#>" + ChatColor.WHITE + " - Close one of your tickets.");
       if(player == null || player.hasPermission("sht.admin")) {
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Admin Commands" + ChatColor.GOLD + " --");
         sender.sendMessage(ChatColor.RED + " /tickets" + ChatColor.WHITE + " - List all tickets");
         sender.sendMessage(ChatColor.RED + " /checkticket <#>" + ChatColor.WHITE + " - Check a ticket's info.");
         sender.sendMessage(ChatColor.RED + " /taketicket <#>" + ChatColor.WHITE + " - Assign yourself to a ticket.");
         sender.sendMessage(ChatColor.RED + " /replyticket <#> <Reply>" + ChatColor.WHITE + " - Reply to a Ticket.");
         sender.sendMessage(ChatColor.RED + " /closeticket <#>" + ChatColor.WHITE + " - Close a ticket.");      
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("sht")){ 
       if(args.length == 0) {      
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.GRAY + "SimpleHelpTickets" + " v" + getDescription().getVersion() + ChatColor.GOLD + " --");
         sender.sendMessage(ChatColor.BLUE + " /ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket. [Stand @ Location]");
         sender.sendMessage(ChatColor.BLUE + " /tickets" + ChatColor.WHITE + " - View Your Tickets.");
         sender.sendMessage(ChatColor.BLUE + " /checkticket <#>" + ChatColor.WHITE + " - Check one of your ticket's info.");
         sender.sendMessage(ChatColor.BLUE + " /closeticket <#>" + ChatColor.WHITE + " - Close one of your tickets.");
         if(player == null || player.hasPermission("sht.admin")) {
           sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.GRAY + "Admin Commands" + ChatColor.GOLD + " --");
           sender.sendMessage(ChatColor.RED + " /tickets" + ChatColor.WHITE + " - List all tickets");
           sender.sendMessage(ChatColor.RED + " /checkticket <#>" + ChatColor.WHITE + " - Check a ticket's info.");
           sender.sendMessage(ChatColor.RED + " /taketicket <#>" + ChatColor.WHITE + " - Assign yourself to a ticket.");
           sender.sendMessage(ChatColor.RED + " /replyticket <#> <Reply>" + ChatColor.WHITE + " - Reply to a Ticket.");
           sender.sendMessage(ChatColor.RED + " /closeticket <#>" + ChatColor.WHITE + " - Close a ticket.");      
         }
         return true;
       } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
         if(player.hasPermission("sht.reload") || player == null) {
           reloadConfig();
           sender.sendMessage(ChatColor.GREEN + "Config Reloaded!");
           return true;
         } else {
           sender.sendMessage(ChatColor.RED + "You do not have permission");
         }
       }
     }
 
 
     if(cmd.getName().equalsIgnoreCase("ticket")){     
       if(args.length == 0) {
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "SimpleHelpTickets" + " v" + getDescription().getVersion() + ChatColor.GOLD + " --");
         sender.sendMessage(ChatColor.BLUE + " /ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket. [Stand @ Location]");
         sender.sendMessage(ChatColor.BLUE + " /tickets" + ChatColor.WHITE + " - View Your Tickets.");
         sender.sendMessage(ChatColor.BLUE + " /checkticket <#>" + ChatColor.WHITE + " - Check one of your ticket's info.");
         sender.sendMessage(ChatColor.BLUE + " /closeticket <#>" + ChatColor.WHITE + " - Close one of your tickets.");
         if(player == null || player.hasPermission("sht.admin")) {
           sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Admin Commands" + ChatColor.GOLD + " --");
           sender.sendMessage(ChatColor.RED + " /tickets" + ChatColor.WHITE + " - List all tickets");
           sender.sendMessage(ChatColor.RED + " /checkticket <#>" + ChatColor.WHITE + " - Check a ticket's info.");
           sender.sendMessage(ChatColor.RED + " /taketicket <#>" + ChatColor.WHITE + " - Assign yourself to a ticket.");
           sender.sendMessage(ChatColor.RED + " /replyticket <#> <Reply>" + ChatColor.WHITE + " - Reply to a Ticket.");
           sender.sendMessage(ChatColor.RED + " /closeticket <#>" + ChatColor.WHITE + " - Close a ticket.");          
         }
       } else {
 
         // CONSOLE COMMANDS
         if (player == null) {
           StringBuilder sb = new StringBuilder();
           for (String arg : args)
             sb.append(arg + " ");            
               String[] temp = sb.toString().split(" ");
               String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
               sb.delete(0, sb.length());
               for (String details : temp2)
               {
                 sb.append(details);
                 sb.append(" ");
               }
               String details = sb.toString();  
               String sb2 = "none";
               java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
               int Tsize = Tickets.size();
               if (Tsize == 0) {
                 int TicketNumber = 0;
                 Tickets.add(""+TicketNumber); 
                 getStorageConfig().set("Tickets", Tickets); 
                 getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
                 getStorageConfig().set(TicketNumber+".dates", getCurrentDTG("date")); // insert the date
                 getStorageConfig().set(TicketNumber+".placedby", "Console"); // insert who placed the ticket
                 getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
                 getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert reply
                 getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed     
                 ++TicketNumber;
                 getStorageConfig().set("ticketnumber", TicketNumber);  // insert the ticket
 
                 String username = "Console";              
                 if (getStorageConfig().getString(username) == null) {
                   getStorageConfig().set(username, 1);
                 } else {
                   String UsersNoOfTickets = getStorageConfig().getString(username);
                   int temp1 = Integer.parseInt( UsersNoOfTickets );
                   ++temp1;
                   getStorageConfig().set(username, temp1);
                 }
                 saveStorageConfig();
               } else {
                 --Tsize;
                 String finalinlist = Tickets.get(Tsize);            
                 int sizetemp = Integer.parseInt( finalinlist );
                 sizetemp++;
                 int TicketNumber = sizetemp;
                 Tickets.add(""+TicketNumber);
                 getStorageConfig().set("Tickets", Tickets);  // create the new ticket name
                 getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
                 getStorageConfig().set(TicketNumber+".dates", getCurrentDTG("date")); // insert the date
                 getStorageConfig().set(TicketNumber+".placedby", "Console"); // insert who placed the ticket
                 getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
                 getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert admin who has viewed
                 getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed 
                 getStorageConfig().set("ticketnumber", Tickets.size());  // Set the ticketnumber to the size of the Ticket's list
 
                 String username = "Console";          
                 if (getStorageConfig().getString(username) == null) {
                   getStorageConfig().set(username, 1);
                 } else {
                   String UsersNoOfTickets = getStorageConfig().getString(username);
                   int temp1 = Integer.parseInt( UsersNoOfTickets );
                   ++temp1;
                   getStorageConfig().set(username, temp1);
                 }
                 saveStorageConfig();
               }          
 
               sender.sendMessage(ChatColor.GREEN + "Your ticket has been logged and will be reviewed shortly");
 
               Player[] players = Bukkit.getOnlinePlayers();
               for(Player op: players){
                 if(op.hasPermission("sht.admin")) {
                   op.sendMessage(ChatColor.GOLD + "* " + "Console" + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
                 }
               }
               return true;
               // Player Commands
         } else if (getStorageConfig().getString(player.getDisplayName()) != null ) {
           String UsersNoOfTickets = getStorageConfig().getString(player.getDisplayName());
           int temp1 = Integer.parseInt( UsersNoOfTickets );
           int MaxTickets = getConfig().getInt("MaxTickets");
           if (temp1 >= MaxTickets) {
             sender.sendMessage(ChatColor.RED + "You've reached your limit of " + MaxTickets + " tickets.");
             return true;
           }
         }
         StringBuilder sb = new StringBuilder();
         for (String arg : args)
           sb.append(arg + " ");            
             String[] temp = sb.toString().split(" ");
             String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
             sb.delete(0, sb.length());
             for (String details : temp2)
             {
               sb.append(details);
               sb.append(" ");
             }
             String details = sb.toString();
             World PlayerWorld = player.getWorld();
             String PlayerWorldName = PlayerWorld.getName();
             double locX = player.getLocation().getX();
             double locY = player.getLocation().getY();
             double locZ = player.getLocation().getZ();
 
             StringBuilder sb1 = new StringBuilder();
             sb1.append(PlayerWorldName+",");
             sb1.append(locX+",");
             sb1.append(locY+",");
             sb1.append(locZ);
             String sb2 = sb1.toString();
 
             java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
             int Tsize = Tickets.size();
             if (Tsize == 0) {
               int TicketNumber = 0;
               Tickets.add(""+TicketNumber); 
               getStorageConfig().set("Tickets", Tickets); 
               getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
               getStorageConfig().set(TicketNumber+".dates", getCurrentDTG("date")); // insert the date
               getStorageConfig().set(TicketNumber+".placedby", player.getDisplayName()); // insert who placed the ticket
               getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
               getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert reply
               getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed     
               ++TicketNumber;
               getStorageConfig().set("ticketnumber", TicketNumber);  // insert the ticket
 
               String username = player.getDisplayName();              
               if (getStorageConfig().getString(username) == null) {
                 getStorageConfig().set(username, 1);
               } else {
                 String UsersNoOfTickets = getStorageConfig().getString(username);
                 int temp1 = Integer.parseInt( UsersNoOfTickets );
                 ++temp1;
                 getStorageConfig().set(username, temp1);
               }
               saveStorageConfig();
             } else {
               --Tsize;
               String finalinlist = Tickets.get(Tsize);            
               int sizetemp = Integer.parseInt( finalinlist );
               sizetemp++;
               int TicketNumber = sizetemp;
               Tickets.add(""+TicketNumber);
               getStorageConfig().set("Tickets", Tickets);  // create the new ticket name
               getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
               getStorageConfig().set(TicketNumber+".dates", getCurrentDTG("date")); // insert the date
               getStorageConfig().set(TicketNumber+".placedby", player.getDisplayName()); // insert who placed the ticket
               getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
               getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert admin who has viewed
               getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed 
               getStorageConfig().set("ticketnumber", Tickets.size());  // Set the ticketnumber to the size of the Ticket's list
 
               String username = player.getDisplayName();              
               if (getStorageConfig().getString(username) == null) {
                 getStorageConfig().set(username, 1);
               } else {
                 String UsersNoOfTickets = getStorageConfig().getString(username);
                 int temp1 = Integer.parseInt( UsersNoOfTickets );
                 ++temp1;
                 getStorageConfig().set(username, temp1);
               }
               saveStorageConfig();
             }          
 
             sender.sendMessage(ChatColor.GREEN + "Your ticket has been logged and will be reviewed shortly");
 
             Player[] players = Bukkit.getOnlinePlayers();
             for(Player op: players){
               if(op.hasPermission("sht.admin")) {
                 op.sendMessage(ChatColor.GOLD + "* " + sender.getName() + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
               }
             }
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("tickets")){     
       int NumberOfTickets = getStorageConfig().getStringList("Tickets").size();
       if (NumberOfTickets == 0) {
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Current Help Tickets" + ChatColor.GOLD + " --");
         sender.sendMessage(ChatColor.WHITE + " There are currently no help tickets to display.");  
       } else if( NumberOfTickets != 0) {
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Current Help Tickets" + ChatColor.GOLD + " --");
         for(int i = 0; i < NumberOfTickets; ++i) {
           java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
           String TicketNumber = Tickets.get(i);
           String TicketDesc = getStorageConfig().getString(TicketNumber+".description");
           String TicketPB = getStorageConfig().getString(TicketNumber+".placedby");
           String TicketREPLY = getStorageConfig().getString(TicketNumber+".reply");          
           if (player != null && !player.hasPermission("sht.admin") && TicketPB.contains(player.getDisplayName())) {            
             if (!TicketREPLY.equalsIgnoreCase("none")) {
               sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.YELLOW + TicketNumber + ChatColor.GOLD + ") " + ChatColor.BLUE + TicketPB + ChatColor.YELLOW + ": " + TicketDesc);  
             } else {
               sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.WHITE + TicketNumber + ChatColor.GOLD + ") " + ChatColor.BLUE + TicketPB + ChatColor.WHITE + ": " + TicketDesc);
             }
           } else if(player == null || player.hasPermission("sht.admin")) {            
             if (!TicketREPLY.equalsIgnoreCase("none")) {
               sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.YELLOW + TicketNumber + ChatColor.GOLD + ") " + ChatColor.BLUE + TicketPB + ChatColor.YELLOW + ": " + TicketDesc);
             } else {
               sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.WHITE + TicketNumber + ChatColor.GOLD + ") " + ChatColor.BLUE + TicketPB + ChatColor.WHITE + ": " + TicketDesc);
             }
           }
         }
         return true;
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("checkticket")){
       for (char c : args[0].toCharArray()) {
         if (!Character.isDigit(c)) {
           sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
           return true;
         }
       }
       int ticketno = Integer.parseInt( args[0] );
       //      java.util.List<String> tickets = getStorageConfig().getStringList("Tickets");  
       String placedby =  getStorageConfig().getString(ticketno+".placedby");
       if (player != null && !placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
         sender.sendMessage("This is not your ticket to check");
       } else {
         String tickdesc = getStorageConfig().getString(ticketno+".description");
         String date = getStorageConfig().getString(ticketno+".dates");        
         //          String loc =  getStorageConfig().getString(ticketno+".location");
         String reply = getStorageConfig().getString(ticketno+".reply");
         String admin = getStorageConfig().getString(ticketno+".admin");        
 
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " --");
         sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
         sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
         sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
         sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
         sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);
         return true;
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("replyticket")){      
       for (char c : args[0].toCharArray()) {
         if (!Character.isDigit(c)) {
           sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
           return true;
         }
       }
       int ticketno = Integer.parseInt( args[0] );
       StringBuilder sb = new StringBuilder();
       for (String arg : args)
         sb.append(arg + " ");          
           String[] temp = sb.toString().split(" ");
           String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
           sb.delete(0, sb.length());
           for (String details : temp2)
           {
             sb.append(details);
             sb.append(" ");
           }
           String details = sb.toString();                
           if (player == null) {
             getStorageConfig().set(ticketno+".reply", "(Console) " + details);
           } else {
             getStorageConfig().set(ticketno+".reply", "(" + player.getDisplayName() + ") " + details);
           }                    
           saveStorageConfig();
           sender.sendMessage(ChatColor.GOLD + "* " + ChatColor.WHITE + " Replied to ticket " + ChatColor.GOLD + ticketno + ChatColor.WHITE + ".");
           String placedby =  getStorageConfig().getString(ticketno+".placedby");
           if (this.getServer().getPlayer(placedby) != null) {
             Player target = this.getServer().getPlayer(placedby);
             target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + player.getDisplayName() + ChatColor.GRAY + " has replied to your help ticket.");
           }
     }
 
     if(cmd.getName().equalsIgnoreCase("taketicket")){
       if (player == null) {
         sender.sendMessage("This command can only be run by a player, use /checkticket instead.");
         return true;
       }
       for (char c : args[0].toCharArray()) {
         if (!Character.isDigit(c)) {
           sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
           return true;
         }
       }
       int ticketno = Integer.parseInt( args[0] );
      java.util.List<String> tickets = getStorageConfig().getStringList("Tickets");            

      if (ticketno > tickets.size() || tickets.size() == 0) {
         sender.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Ticket " + ChatColor.GOLD + ticketno + ChatColor.GRAY + " Does Not Exist");
         return true;
       } else {
 
         String tickdesc = getStorageConfig().getString(ticketno+".description");
         String date = getStorageConfig().getString(ticketno+".dates");
         String placedby =  getStorageConfig().getString(ticketno+".placedby");
         String loc =  getStorageConfig().getString(ticketno+".location");
         String reply = getStorageConfig().getString(ticketno+".reply");
         String admin = getStorageConfig().getString(ticketno+".admin");
 
         getStorageConfig().set(ticketno+".admin", player.getDisplayName());      
         saveStorageConfig();
 
        if (loc.contains("none")) {
           sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " --");
           sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
           sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
           sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.RED + "None [Console Ticket]");
           sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
           sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
           sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);          
           String tickuser = myGetPlayerName(placedby);
           if(this.getServer().getPlayer(tickuser) == null) {
             return true;  
           } else {
             String admin1 = player.getDisplayName();
             Player target = this.getServer().getPlayer(tickuser);
             String TicketReview = getConfig().getString("TicketBeingReviewedMsg");
             if (TicketReview == null) {
               target.sendMessage(ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin1 + ChatColor.GRAY + " is reviewing your help ticket");
               return true;
             } else {
               target.sendMessage(ChatColor.GREEN + TicketReview);
               return true;
             }
 
           }
         } else {
           String[] vals = loc.split(",");
           World world = Bukkit.getWorld(vals[0]);
           double x = Double.parseDouble(vals[1]);        
           double y = Double.parseDouble(vals[2]);
           double z = Double.parseDouble(vals[3]);
           Location locc = new Location(world, x, y, z);
           player.teleport(locc);
           sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " --");
           sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
           sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
           sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
           sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
           sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);
           //      sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.GREEN + tickLOC);
           String tickuser = myGetPlayerName(placedby);
           if(this.getServer().getPlayer(tickuser) == null) {
             return true;  
           } else {
             String admin1 = player.getDisplayName();
             Player target = this.getServer().getPlayer(tickuser);
             String TicketReview = getConfig().getString("TicketBeingReviewedMsg");
             if (TicketReview == null) {
               target.sendMessage(ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin1 + ChatColor.GRAY + " is reviewing your help ticket");
               return true;
             } else {
               target.sendMessage(ChatColor.GREEN + TicketReview);
               return true;
             }
 
           }
         }
       }
     }
 
     // NOT USED
     if(cmd.getName().equalsIgnoreCase("delticket")){
       if(args.length == 0) {        
         sender.sendMessage(ChatColor.WHITE + "/delticket <#>");
       } else if(args.length == 1) {
         for (char c : args[0].toCharArray()) {
           if (!Character.isDigit(c)) {
             sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
             return true;
           }
         }
         int ticketno = Integer.parseInt( args[0] );
         int TicketNumber = getStorageConfig().getInt("ticketnumber");
 
         String placedby =  getStorageConfig().getString(ticketno+".placedby");
         if (!placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
           sender.sendMessage("This is not your ticket to delete");
         } else {
 
           java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
           Tickets.remove(args[0]);
           getStorageConfig().set("Tickets", Tickets);
           getStorageConfig().set(args[0], null);
           --TicketNumber;
           getStorageConfig().set("ticketnumber", TicketNumber);
           saveStorageConfig();
 
           sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " deleted");
           return true;
         }
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("closeticket")){
       if(args.length == 0) {        
         sender.sendMessage(ChatColor.WHITE + "/closeticket <#>");
       } else if(args.length == 1) {
         for (char c : args[0].toCharArray()) {
           if (!Character.isDigit(c)) {
             sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
             return true;
           }
         }
         int ticketno = Integer.parseInt( args[0] );
         int TicketNumber = getStorageConfig().getInt("ticketnumber");
 
         String placedby =  getStorageConfig().getString(ticketno+".placedby");
         if (player != null && !placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
           sender.sendMessage("This is not your ticket to delete");
         } else if(player != null && placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) { // if the player owns the ticket (NOTADMIN)
 
           java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
           Tickets.remove(args[0]);
           getStorageConfig().set("Tickets", Tickets);
           getStorageConfig().set(args[0], null);
           --TicketNumber;
           getStorageConfig().set("ticketnumber", TicketNumber);
 
           String username = player.getDisplayName();
           String UsersNoOfTickets = getStorageConfig().getString(username);
           int temp1 = Integer.parseInt( UsersNoOfTickets );
           --temp1;
           if (temp1 == 0) {
             getStorageConfig().set(username, null);
           } else {
             getStorageConfig().set(username, temp1);
           }          
           saveStorageConfig();
           sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " closed.");
           Player[] players = Bukkit.getOnlinePlayers();
           for(Player op: players){
             if(op.hasPermission("sht.admin") && getConfig().getBoolean("NotifyAdminOnTicketClose")) {
               op.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "User " + ChatColor.GOLD + player.getDisplayName() + ChatColor.GRAY + " has closed ticket " + ChatColor.GOLD + ticketno);
             }
           } 
           return true;  
         } else if(player == null || player.hasPermission("sht.admin")) {
           java.util.List<String> Tickets = getStorageConfig().getStringList("Tickets");
           Tickets.remove(args[0]);
           getStorageConfig().set("Tickets", Tickets);
           getStorageConfig().set(args[0], null);
           --TicketNumber;
           getStorageConfig().set("ticketnumber", TicketNumber);
 
           String username = placedby;
           String UsersNoOfTickets = getStorageConfig().getString(username);
           int temp1 = Integer.parseInt( UsersNoOfTickets );
           --temp1;
           if (temp1 == 0) {
             getStorageConfig().set(username, null);
           } else {
             getStorageConfig().set(username, temp1);
           }
           saveStorageConfig();
           sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " closed.");
           if (player == null) {
             String admin = "Console";
             if (this.getServer().getPlayer(placedby) != null) {
               Player target = this.getServer().getPlayer(placedby);
               target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed your help ticket");
             }
             Player[] players = Bukkit.getOnlinePlayers();
             for(Player op: players){
               if(op.hasPermission("sht.admin") && getConfig().getBoolean("NotifyAdminOnTicketClose")) {
                 op.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed ticket " + ChatColor.GOLD + ticketno);
               }
             }
           } else {
             String admin = player.getDisplayName();
             if (this.getServer().getPlayer(placedby) != null) {
               Player target = this.getServer().getPlayer(placedby);
               target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed your help ticket");
             }
             Player[] players = Bukkit.getOnlinePlayers();
             for(Player op: players){
               if(op.hasPermission("sht.admin") && getConfig().getBoolean("NotifyAdminOnTicketClose")) {
                 op.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed ticket " + ChatColor.GOLD + ticketno);
               }
             }
           }
           return true;
         }
       }    
     }
 
     return true;
     // END
   }
 }

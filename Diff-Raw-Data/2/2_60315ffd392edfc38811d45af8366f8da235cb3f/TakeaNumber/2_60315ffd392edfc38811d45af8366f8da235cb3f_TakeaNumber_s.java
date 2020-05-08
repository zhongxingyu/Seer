 package me.olddragon.takeanumber;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfigurationOptions;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TakeaNumber extends JavaPlugin {
     
   static final Logger log = Logger.getLogger("Minecraft");
   public static SimpleDateFormat date_format = null;
   
   private YamlConfiguration tickets_config = null;
   private File tickets_file = null;
   
   @SuppressWarnings("unused")
   private PListener listener = null;
   
   public static String getCurrentDate () { return date_format.format (Calendar.getInstance().getTime()); }
   
   public void loadTickets() {
     if (tickets_file == null) { tickets_file = new File(getDataFolder(), "Tickets.yml"); }
     tickets_config = YamlConfiguration.loadConfiguration(tickets_file);
     InputStream defaults = getResource("Tickets.yml");
     if (defaults != null) { tickets_config.setDefaults(YamlConfiguration.loadConfiguration(defaults)); }
   }
   public YamlConfiguration getTickets() {
     if (tickets_config == null) { loadTickets(); }
     return tickets_config;
   }
   public void saveTickets() {
     if (tickets_config == null || tickets_file == null) { return; }
     try {
       tickets_config.save(tickets_file);
     } catch (IOException ex) {
       Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + tickets_file.toString(), ex);
     }
   }
 
   @Override
   public void onEnable(){
     // Load configuration
     FileConfigurationOptions cfgOptions = getConfig().options();
     cfgOptions.copyDefaults(true);
     cfgOptions.copyHeader(true);
     saveConfig();
 
     if (date_format == null) {
       String format = getConfig().getString("DateFormat");
       try {
         date_format = new SimpleDateFormat(format);
       } catch (IllegalArgumentException ex) {
         Logger.getLogger(JavaPlugin.class.getName()).log(Level.WARNING, "Invalid date format: " + format, ex);
         date_format = new SimpleDateFormat();
       }
     }
 
     // Load Tickets
     FileConfigurationOptions ticketOptions = getTickets().options();
     ticketOptions.copyDefaults(true);
     ticketOptions.copyHeader(true);
     saveTickets();
 
     // declare new listener
     this.listener = new PListener(this);
     
     log.log(Level.INFO, "[{0}] {1} enabled.", new Object[]{ getDescription().getName(), getDescription().getVersion() }); 
   }
 
   @Override
   public void onDisable(){ 
     log.log(Level.INFO, "[{0}] {1} disabled.", new Object[]{getDescription().getName(), getDescription().getVersion()}); 
   }
 
   public class PListener implements Listener {
 
     public PListener(TakeaNumber instance) {
       Plugin plugin = instance;
       Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin(PlayerJoinEvent event) {      
       if (getConfig().getBoolean("ShowTicketsOnJoin") == true) {
         Player player = event.getPlayer();
         if (player != null && player.hasPermission("tan.admin")) {
           if (getConfig().getBoolean("AlwaysLoadTickets", false)) { loadTickets(); }
           int ticklength = getTickets().getStringList("Tickets").size();
           if(ticklength > 0) {
             player.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "There are currently " + ChatColor.GOLD + ticklength + ChatColor.GRAY + " open Help Tickets");
           }
         } 
       }
     }
   }
 
   /**
    * Get the player name
    * @param name
    * @return
    */
   public String getPlayerName(String name) { 
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
   
   /**
    * Display the list of commands
    * @param sender Who to send the list too
    * @param isAdmin Show the administrator commands
    */
   protected void displayCommands (CommandSender sender, boolean isAdmin) {
     sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "TakeaNumber" + " v" + getDescription().getVersion() + ChatColor.GOLD + " --");
     sender.sendMessage(ChatColor.BLUE + " /ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket. [Stand @ Location]");
     sender.sendMessage(ChatColor.BLUE + " /tickets" + ChatColor.WHITE + " - View Your Tickets.");
     sender.sendMessage(ChatColor.BLUE + " /checkticket <#>" + ChatColor.WHITE + " - Check one of your ticket's info.");
     sender.sendMessage(ChatColor.BLUE + " /closeticket <#>" + ChatColor.WHITE + " - Close one of your tickets.");
     if(isAdmin) {
       sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Admin Commands" + ChatColor.GOLD + " --");
       sender.sendMessage(ChatColor.RED + " /tickets" + ChatColor.WHITE + " - List all tickets");
       sender.sendMessage(ChatColor.RED + " /checkticket <#>" + ChatColor.WHITE + " - Check a ticket's info.");
       sender.sendMessage(ChatColor.RED + " /taketicket <#>" + ChatColor.WHITE + " - Assign yourself to a ticket.");
       sender.sendMessage(ChatColor.RED + " /replyticket <#> <Reply>" + ChatColor.WHITE + " - Reply to a Ticket.");
       sender.sendMessage(ChatColor.RED + " /closeticket <#>" + ChatColor.WHITE + " - Close a ticket.");      
     }
   }
 
   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     Player player = null;
     if (sender instanceof Player) {
       player = (Player) sender;
     }
     boolean isConsole = player == null;
     boolean isAdmin = player == null || player.hasPermission("tan.admin");
     
     if (getConfig().getBoolean("AlwaysLoadTickets", false)) { loadTickets(); }
 
    if(cmd.getName().equalsIgnoreCase("tickethelp")){ 
       displayCommands(sender, isAdmin);
     }
 
     if(cmd.getName().equalsIgnoreCase("ticket")){
       if(args.length == 0) {
         displayCommands(sender, isAdmin);
       } else {
         
         if (player != null && getTickets().getString(player.getDisplayName()) != null ) {
           int count = getTickets().getInt(player.getDisplayName());
           int MaxTickets = getConfig().getInt("MaxTickets");
           if (count >= MaxTickets) {
             sender.sendMessage(ChatColor.RED + "You've reached your limit of " + MaxTickets + " tickets.");
             return true;
           }
         }
         
         java.util.List<String> tickets = getTickets().getStringList("Tickets");
         String next_ticket = String.valueOf(tickets.isEmpty() ? 0 : Integer.parseInt(Ticket.load(getTickets(), tickets.get(tickets.size() - 1)).getId(), 10) + 1);
         Ticket ticket = new Ticket(getTickets(), next_ticket);
 
         StringBuilder details = new StringBuilder();
         for (int i=0; i<args.length; i++) { details.append(args[i]).append(" "); }
 
         ticket.description = details.toString();
         ticket.dates = getCurrentDate();
         
         // CONSOLE COMMANDS
         if (isConsole) {
           newTicket(next_ticket, "Console");
           ticket.placed_by = "Console";
         } else {
           newTicket(next_ticket, player.getDisplayName());
           ticket.placed_by = player.getDisplayName();
           ticket.location = String.format("%s,%d,%d,%d", player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
         }
         
         ticket.save();
         saveTickets();
         
         sender.sendMessage(
             ChatColor.GREEN + "Your ticket (#" + ChatColor.RED + ticket.getId() + ChatColor.GREEN + ") has been logged and will be reviewed shortly." +
             "Use " + ChatColor.YELLOW + "/checkticket " + ticket.getId() + ChatColor.GREEN + " to review the status in the future.");
         notifyAdmins(ChatColor.GOLD + "* " + (isConsole ? "Console" : player.getDisplayName()) + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
       }
     }
 
     if(cmd.getName().equalsIgnoreCase("tickets")){
       java.util.List<String> Tickets = getTickets().getStringList("Tickets");
       if (Tickets.isEmpty()) {
         sender.sendMessage(ChatColor.WHITE + " There are currently no help tickets to display.");  
       } else {
         sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Current Help Tickets" + ChatColor.GOLD + " --");
         String name = player == null ? null : player.getDisplayName();
         for (String id : Tickets) {
           Ticket ticket = Ticket.load(getTickets(), id);
           if (ticket != null && (isAdmin || name != null && ticket.placed_by.contains(name))) {
             ChatColor color = !ticket.reply.equalsIgnoreCase("none") ? ChatColor.YELLOW : ChatColor.WHITE;
             sender.sendMessage(ChatColor.GOLD + " (" + color + ticket.getId() + ChatColor.GOLD + ") " + ChatColor.BLUE + ticket.placed_by + color + ": " + ticket.description);
           }
         }
       }
       
       return true;
     }
 
     if(cmd.getName().equalsIgnoreCase("checkticket")){
       if (! isTicket(args[0])) { sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]); return true; }
       String id = args[0];
       Ticket ticket = Ticket.load(getTickets(), id);
       
       if (ticket == null) {
         sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id);
       } else if (!isAdmin && !ticket.placed_by.contains(player.getDisplayName())) {
         sender.sendMessage("This is not your ticket to check");
       } else {
         ticket.toMessage(sender);
       }
       
       return true;
     }
 
     if(cmd.getName().equalsIgnoreCase("replyticket")){      
       if (! isTicket(args[0])) { sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]); return true; }
       String id = args[0];
       Ticket ticket = Ticket.load(getTickets(), id);
       
       if (ticket == null) {
         sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id);
         return true;
       }
       
       StringBuilder details = new StringBuilder();
       for (int i=1; i<args.length; i++) { details.append(args[i]).append(" "); }
       
       ticket.reply = player == null ? "(Console) " + details.toString() : "(" + player.getDisplayName() + ") " + details.toString();
       ticket.save();
       saveTickets();
       
       sender.sendMessage(ChatColor.GOLD + "* " + ChatColor.WHITE + " Replied to ticket " + ChatColor.GOLD + id + ChatColor.WHITE + ".");
       
       String placedby = ticket.placed_by;
       if (this.getServer().getPlayer(placedby) != null) {
         Player target = this.getServer().getPlayer(placedby);
         target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + player.getDisplayName() + ChatColor.GRAY + " has replied to your help ticket.");
       }
       
       return true;
     }
 
     if (cmd.getName().equalsIgnoreCase("taketicket")) {
       if (player == null) { sender.sendMessage("This command can only be run by a player, use /checkticket instead."); return true; }
       if (! isTicket(args[0])) { sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]); return true; }
       String id = args[0];
       Ticket ticket = Ticket.load(getTickets(), id);
 
       if (ticket == null) {
         sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id);
         return true;
       }
       
       ticket.admin = player.getDisplayName();
       ticket.save();
       saveTickets();
       
       ticket.toMessage(sender);
       
       if (ticket.location.equalsIgnoreCase("none")) {
         String[] vals = ticket.location.split(",");
         World world = Bukkit.getWorld(vals[0]);
         double x = Double.parseDouble(vals[1]);        
         double y = Double.parseDouble(vals[2]);
         double z = Double.parseDouble(vals[3]);
         player.teleport(new Location(world, x, y, z));
       }
       
       String user = getPlayerName(ticket.placed_by);
       if (this.getServer().getPlayer(user) != null) {
         String admin = player.getDisplayName();
         Player target = this.getServer().getPlayer(user);
         String message = getConfig().getString("TicketBeingReviewedMsg");
         target.sendMessage(message != null ? ChatColor.GREEN + message : ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " is reviewing your help ticket");
       }
       
       return true;
     }
 
     if(cmd.getName().equalsIgnoreCase("closeticket")){
       if(args.length == 0) {        
         sender.sendMessage(ChatColor.WHITE + "/closeticket <#>");
       } else if(args.length == 1) {
         String id = args[0];
         if (! isTicket(id)) { sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return true; }
         Ticket ticket = Ticket.load(getTickets(), id);
         if (ticket == null) {
           sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id);
           return true;
         }
 
         String placedby =  ticket.placed_by;
         if (player != null && placedby.contains(player.getDisplayName())) {
           closeTicket(id);
           sender.sendMessage(ChatColor.GREEN + " Ticket " + id + " closed.");
           
           notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "User " + ChatColor.GOLD + player.getDisplayName() + ChatColor.GRAY + " has closed ticket " + ChatColor.GOLD + id);
         } else if (isAdmin) {
           closeTicket(id);          
           sender.sendMessage(ChatColor.GREEN + " Ticket " + id + " closed.");
           
           String admin = (player == null ? "Console" : player.getDisplayName());
           Player target = this.getServer().getPlayer(placedby);
           if (target != null) { target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed your help ticket"); }
           
           notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has closed ticket " + ChatColor.GOLD + id);
         } else {
           sender.sendMessage("This is not your ticket to delete");
         }
       }
       
       return true;
     }
 
     return true;
     // END
   }
   
   /**
    * Add a ticket and increment the users ticket count
    * @param id
    * @param user
    */
   protected void newTicket (String id, String user) {
     java.util.List<String> Tickets = getTickets().getStringList("Tickets");
     Tickets.add(id); 
     getTickets().set("Tickets", Tickets);
     getTickets().set("counts."+user, getTickets().getInt("counts."+user) + 1);
   }
   
   /**
    * Close a ticket and decrement the the users ticket count 
    * @param ticket
    */
   protected void closeTicket (String ticket) {
     String user = Ticket.load(getTickets(), ticket).placed_by;
     int count = getTickets().getInt("counts."+user) - 1;
     getTickets().set("counts."+user, count == 0 ? null : count);
 
     java.util.List<String> Tickets = getTickets().getStringList("Tickets");
     Tickets.remove(ticket);
     getTickets().set("Tickets", Tickets);
     getTickets().set(ticket, null);
 
     saveTickets();
   }
   
   /**
    * Format for tickets
    */
   private static java.util.regex.Pattern ticket_format = java.util.regex.Pattern.compile("^\\d+$", java.util.regex.Pattern.CASE_INSENSITIVE);
   
   /**
    * Checks to see if a string represents a ticket id
    * @param str string to check
    * @return true if the string matches the ticket format
    */
   protected boolean isTicket (String str) {
     return ticket_format.matcher(str).matches();
   }
   
   /**
    * Notify all online administrators
    * @param message message to send
    */
   protected void notifyAdmins (String message) {
     if (! getConfig().getBoolean("NotifyAdminOnTicketClose")) { return; }
     Player[] players = Bukkit.getOnlinePlayers();
     for(Player op : players) { if(op.hasPermission("tan.admin")) { op.sendMessage(message); } }
   }
 }

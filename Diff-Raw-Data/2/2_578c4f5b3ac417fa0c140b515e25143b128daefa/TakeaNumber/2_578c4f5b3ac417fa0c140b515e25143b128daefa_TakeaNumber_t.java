 package me.olddragon.takeanumber;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
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
 
     log.log(Level.INFO, "[%s] %s enabled.", new Object[]{ getDescription().getName(), getDescription().getVersion() });
     expireTickets();
   }
 
   
   @Override
   public void onDisable(){
     log.log(Level.INFO, "[%s] %s disabled.", new Object[]{ getDescription().getName(), getDescription().getVersion() });
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
 
   public class State {
     public CommandSender sender;
     public Player player;
     public String name;
     public boolean isConsole;
     public boolean isAdmin;
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
   protected void usage (State state) {
     List<String> commands = getConfig().getStringList("commands");
     for (String command : commands) {
       Command cmd = getCommand(command);
       state.sender.sendMessage(ChatColor.BLUE + cmd.getUsage());
     }
     /*
     state.sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Commands" + ChatColor.GOLD + " --");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket open <Description> " + ChatColor.WHITE + " - Open a ticket. Your current location will be recorded.");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket list               " + ChatColor.WHITE + " - View your tickets.");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket check <#>          " + ChatColor.WHITE + " - Check one of your ticket's info.");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket reply <#> <message>" + ChatColor.WHITE + " - Reply to one of your tickets.");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket close <#>          " + ChatColor.WHITE + " - Close one of your tickets.");
     state.sender.sendMessage(ChatColor.BLUE + " /ticket delete <#>         " + ChatColor.WHITE + " - Delete one of your tickets.");
     if(state.isAdmin) {
       state.sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Admin Commands" + ChatColor.GOLD + " --");
       state.sender.sendMessage(ChatColor.RED + " /ticket list               " + ChatColor.WHITE + " - List all tickets");
       state.sender.sendMessage(ChatColor.RED + " /ticket check <#>          " + ChatColor.WHITE + " - Check a ticket's info.");
       state.sender.sendMessage(ChatColor.RED + " /ticket take <#>           " + ChatColor.WHITE + " - Assign yourself to a ticket.");
       state.sender.sendMessage(ChatColor.RED + " /ticket visit <#>          " + ChatColor.WHITE + " - Teleport yourself to a ticket location.");
       state.sender.sendMessage(ChatColor.RED + " /ticket reply <#> <message>" + ChatColor.WHITE + " - Reply to a ticket.");
       state.sender.sendMessage(ChatColor.RED + " /ticket close <#> [message]" + ChatColor.WHITE + " - Close a ticket.");
       state.sender.sendMessage(ChatColor.RED + " /ticket delete <#>         " + ChatColor.WHITE + " - Delete a ticket.");
     }
     */
   }
 
   @Override
   public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
     String command = cmd.getName().toLowerCase();
 
     State state = new State();
     state.sender = sender;
     state.player = sender instanceof Player ? (Player) sender : null;
     state.name = state.player == null ? "" : state.player.getDisplayName();
     state.isConsole = state.player == null;
     state.isAdmin = state.player == null || state.player.hasPermission("tan.admin");
 
     if (getConfig().getBoolean("AlwaysLoadTickets", false)) { loadTickets(); }
 
     if      (command.equals("ticket-help")    && args.length == 0) { usage(state);            }
     else if (command.equals("ticket-list")    && args.length == 0) { cmdList(state, args);    }
     else if (command.equals("ticket-open")    && args.length >  0) { cmdOpen(state, args);    }
     else if (command.equals("ticket-check")   && args.length == 1) { cmdCheck(state, args);   }
     else if (command.equals("ticket-take")    && args.length == 1) { cmdTake(state, args);    }
     else if (command.equals("ticket-visit")   && args.length == 1) { cmdVisit(state, args);   }
     else if (command.equals("ticket-reply")   && args.length >  1) { cmdReply(state, args);   }
     else if (command.equals("ticket-resolve")                    ) { cmdResolve(state, args); }
     else if (command.equals("ticket-delete")  && args.length == 0) { cmdDelete(state, args);  }
     else { usage(state); }
 
     return true;
   }
 
   /**
    * Get a tickets information
    * @param state
    * @param args
    */
   private void cmdCheck(State state, String[] args) {
     String id = args[0];
     if (! isTicket(id)) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
   
     if (ticket == null) {
       state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id);
     } else if (!state.isAdmin && !ticket.placed_by.equals(state.name)) {
       state.sender.sendMessage("This is not your ticket to check");
     } else {
       ticket.toMessage(state.sender);
     }
   }
 
   /**
    * Delete a ticket
    * @param state
    * @param args
    */
   private void cmdDelete(State state, String[] args) {
     String id = args[0];
     if (! isTicket(id)) { state.player.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
     if (ticket == null) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { state.sender.sendMessage("This is not one of your tickets"); return; }
     
     deleteTicket(id);
     
     if (state.isAdmin) {
       String admin = state.isConsole ? "(Console)" : state.name;
       
       Player target = getServer().getPlayer(ticket.placed_by);
       if (target != null) { target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has deleted your help ticket"); }
       
       notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has deleted ticket " + ChatColor.GOLD + id);
     } else {
       notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "User " + ChatColor.GOLD + state.name + ChatColor.GRAY + " has deleted ticket " + ChatColor.GOLD + id);
     }
   }
 
   /**
    * List the tickets
    * @param state
    * @param args
    */
   private void cmdList(State state, String[] args) {
     java.util.List<String> Tickets = getTickets().getStringList("Tickets");
     if (Tickets.isEmpty()) {
       state.sender.sendMessage(ChatColor.WHITE + " There are currently no help tickets to display.");
     } else {
       state.sender.sendMessage(ChatColor.GOLD + "-- " + ChatColor.WHITE + "Current Help Tickets" + ChatColor.GOLD + " --");
       for (String id : Tickets) {
         Ticket ticket = Ticket.load(getTickets(), id);
         if (ticket != null && (state.isAdmin || ticket.placed_by.equals(state.name))) {
           ChatColor color = 
               !ticket.reply.equals("none") ? ChatColor.YELLOW :
               !ticket.resolve.equals("none") ? ChatColor.GREEN :
               ChatColor.RED;
           state.sender.sendMessage(
             ChatColor.GOLD + " (" + color + ticket.getId() + ChatColor.GOLD + ") " +
             ChatColor.BLUE + ticket.placed_by + ": " + color + ticket.description +
             (ticket.location.equals("none") ? "" : " @ " + ticket.location)
           );
         }
       }
     }
   }
 
   /**
    * Open a new ticket
    * @param state
    * @param args
    */
   private void cmdOpen(State state, String[] args) {
     if (state.player != null) {
       int count = getTickets().getInt(state.name, 0);
       int MaxTickets = getConfig().getInt("MaxTickets");
       if (count >= MaxTickets) {
         state.player.sendMessage(ChatColor.RED + "You've reached your limit of " + MaxTickets + " tickets.");
         return;
       }
     }
   
     java.util.List<String> tickets = getTickets().getStringList("Tickets");
     String next_ticket = String.valueOf(tickets.isEmpty() ? 0 : Integer.parseInt(Ticket.load(getTickets(), tickets.get(tickets.size() - 1)).getId(), 10) + 1);
     Ticket ticket = new Ticket(getTickets(), next_ticket);
   
     StringBuilder message = new StringBuilder();
     for (int i=0; i<args.length; i++) { message.append(args[i]).append(" "); }
   
     ticket.description = message.toString();
     ticket.dates = getCurrentDate();
   
     if (state.isConsole) {
       newTicket(next_ticket, "Console");
       ticket.placed_by = "Console";
     } else {
       newTicket(next_ticket, state.player.getDisplayName());
       ticket.placed_by = state.player.getDisplayName();
       ticket.location = String.format("%s,%d,%d,%d",
         state.player.getWorld().getName(),
         (int)state.player.getLocation().getX(),
         (int)state.player.getLocation().getY(),
         (int)state.player.getLocation().getZ()
       );
     }
   
     ticket.save();
     saveTickets();
   
     state.sender.sendMessage(String.format(
         "%2$sYour ticket (%1$s#%4$s%2$s) has been logged and will be reviewed shortly. Use %3$s/ticket check %4$s %2$sto review the status in the future.",
         new Object[] { ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ticket.getId() }
     ));
     notifyAdmins(String.format(
         "%2$s* %1$s%3$s %2$shas opened a ticket",
         new Object[] { ChatColor.GOLD, ChatColor.WHITE, (state.isConsole ? "Console" : state.name) }
     ));
   }
 
   /**
    * Reply to a ticket
    * @param state
    * @param args
    */
   private void cmdReply(State state, String[] args) {
     String id = args[0];
     if (! isTicket(id)) { state.player.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
     if (ticket == null) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { state.sender.sendMessage("This is not one of your tickets"); return; }
     
     StringBuilder message = new StringBuilder();
     for (int i=1; i<args.length; i++) { message.append(args[i]).append(" "); }
 
     ticket.reply = (state.isConsole ? "(Console) " : "(" + state.name + ") ") + message.toString();
     ticket.save();
     saveTickets();
 
     state.sender.sendMessage(ChatColor.GOLD + "* " + ChatColor.WHITE + " Replied to ticket " + ChatColor.GOLD + id + ChatColor.WHITE + ".");
 
     Player target = getServer().getPlayer(ticket.placed_by);
     if (target != null) { target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + state.name + ChatColor.GRAY + " has replied to your help ticket."); }
   }
 
   /**
    * Close a ticket
    * @param state
    * @param args
    */
   private void cmdResolve(State state, String[] args) {
     String id = args[0];
     if (! isTicket(id)) { state.player.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
     if (ticket == null) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     if (!state.isAdmin && !ticket.placed_by.equals(state.name)) { state.sender.sendMessage("This is not one of your tickets"); return; }
     
     StringBuilder resolve = new StringBuilder();
     resolve.append("(").append(TakeaNumber.getCurrentDate()).append(") ");
     if (args.length > 1) {
       for (int i=1; i<args.length; i++) { resolve.append(args[i]).append(" "); }
     } else {
       resolve.append("resolved");
     }
   
     ticket.resolve = resolve.toString();
     ticket.resolved_on = TakeaNumber.getCurrentDate();
     resolveTicket(id);
     state.sender.sendMessage(ChatColor.GREEN + " Ticket " + id + " resolved.");
     if (state.isAdmin) {
       String admin = state.isConsole ? "(Console)" : state.name;
       
       Player target = getServer().getPlayer(ticket.placed_by);
       if (target != null) { target.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has resolved your help ticket"); }
       
       notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin + ChatColor.GRAY + " has resolved ticket " + ChatColor.GOLD + id);
     } else {
       notifyAdmins(ChatColor.GOLD + "* " + ChatColor.GRAY + "User " + ChatColor.GOLD + state.name + ChatColor.GRAY + " has resolved ticket " + ChatColor.GOLD + id);
     }
   }
 
   /**
    * Take a ticket from the list
    * @param state
    * @param args
    */
   private void cmdTake(State state, String[] args) {
     if (! state.isAdmin) { state.player.sendMessage("This command can only be run by an admin, use '/ticket check' instead."); return; }
     String id = args[0];
     if (! isTicket(id)) { state.player.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
     if (ticket == null) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
   
     ticket.admin = state.name;
     ticket.save();
     saveTickets();
   
     ticket.toMessage(state.sender);
   
     Player target = getServer().getPlayer(getPlayerName(ticket.placed_by));
     if (target != null) { target.sendMessage(ChatColor.GRAY + "Administrator " + ChatColor.GOLD + state.name + ChatColor.GRAY + " is reviewing your help ticket"); }
   }
 
   /**
    * Teleport the player to the location from which a ticket was submitted
    * @param state
    * @param args
    */
   private void cmdVisit(State state, String[] args) {
     if (! state.isAdmin) { state.player.sendMessage("This command can only be run by an admin, use '/ticket check' instead."); return; }
     String id = args[0];
     if (! isTicket(id)) { state.player.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
     Ticket ticket = Ticket.load(getTickets(), id);
     if (ticket == null) { state.sender.sendMessage(ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + id); return; }
 
     if (ticket.location.equals("none")) {
       String[] vals = ticket.location.split(",");
       World world = Bukkit.getWorld(vals[0]);
       double x = Double.parseDouble(vals[1]);
       double y = Double.parseDouble(vals[2]);
       double z = Double.parseDouble(vals[3]);
       state.player.teleport(new Location(world, x, y, z));
     }
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
   protected void resolveTicket (String ticket) {
     String user = "counts." + Ticket.load(getTickets(), ticket).placed_by;
     int count = getTickets().getInt(user) - 1;
     getTickets().set(user, count == 0 ? null : count);
     saveTickets();
   }
   
   final static long DAY_IN_MS = 1000 * 60 * 60 * 24;
 
   protected void expireTickets () {
     int days = getConfig().getInt("ResolvedTicketExpiration", 7);
     if (days == 0) { return; }
     Date expiration = new Date(System.currentTimeMillis() - (days * TakeaNumber.DAY_IN_MS));
     java.util.List<String> Tickets = getTickets().getStringList("Tickets");
     int count = 0;
     log.log(Level.INFO, "Deleting Expired Tickets");
     for (String id : Tickets) {
       try {
         Ticket ticket = Ticket.load(getTickets(), id);
         Date resolved_on = date_format.parse(ticket.resolved_on);
         if (resolved_on.before(expiration)) { deleteTicket(id); }
       } catch (ParseException e) {
        log.log(Level.WARNING, "Error reading resolved on date for ticket - %s", new Object[] { e.getLocalizedMessage() });
       }
     }
     log.log(Level.INFO, "Deleted %d Tickets", new Object[] { count });
   }
 
   /**
    * Remove the ticket from the list
    * @param ticket
    */
   protected void deleteTicket (String ticket) {
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

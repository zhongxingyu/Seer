 package com.connor.helpdesk;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class HelpCommandExecutor implements CommandExecutor {
 
     private HelpDesk helpDeskInstance;
     
     public HelpCommandExecutor(HelpDesk helpDesk) {
         this.helpDeskInstance = helpDesk;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (!(sender instanceof Player))
             return false;
 
         Player player = (Player) sender;
 
         if (args.length < 1 || args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
             displayManual(player);
             return true;
         }
         
         if (args[0].equalsIgnoreCase("file") || args[0].equalsIgnoreCase("create")) {
             return createTicket(player, args);
         }
         
         if (args[0].equalsIgnoreCase("list")) {
             return listTickets(player);
         }
         
         if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("read")) {
             return readTicket(player, args);
         }
         
         if (args[0].equalsIgnoreCase("assign")) {
             return assignTicket(player, args);
         }
 
         if (args[0].equalsIgnoreCase("elevate")) {
             return elevateTicket(player, args);
         }
 
         if (args[0].equalsIgnoreCase("remove")) {
             return removeTicket(player, args);
         }
         
         if (args[0].equalsIgnoreCase("complete")) {
             return completeTicket(player, args);
         }
         
         if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("urgent")) {
             return markTicketUrgent(player, args);
         }
         
         if (args[0].equalsIgnoreCase("demote") || args[0].equalsIgnoreCase("noturgent")) {
             return markTicketNormal(player, args);
         }
 
         return false;
     }
 
     private boolean displayManual(Player player) {
         player.sendMessage(ChatColor.GRAY + "/ " + ChatColor.GOLD + "HelpDesk");
         player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "file/create <message>" + ChatColor.GRAY + ": Creates a help ticket");
         
         if (player.hasPermission("helpdesk.mod") || player.hasPermission("helpdesk.admin") || player.hasPermission("helpdesk.op")) {
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "list" + ChatColor.GRAY + ": Lists currently-open tickets");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "read <ID>" + ChatColor.GRAY + ": Reads the contents of a help ticket");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "assign <ID>" + ChatColor.GRAY + ": Assigns you to the ticket");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "elevate <ID>" + ChatColor.GRAY + ": Elevates the ticket level to ADMIN or OP");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "urgent <ID>" + ChatColor.GRAY + ": Marks a ticket as urgent");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "noturgent <ID>" + ChatColor.GRAY + ": Marks a ticket as normal");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "complete <ID>" + ChatColor.GRAY + ": Marks the ticket as complete");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "remove <ID>" + ChatColor.GRAY + ": Removes the ticket");
         } else {
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "read <ID>" + ChatColor.GRAY + ": Reads a help ticket");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "urgent <ID>" + ChatColor.GRAY + ": Marks a ticket as urgent");
             player.sendMessage(ChatColor.GRAY + "| " + ChatColor.YELLOW + "noturgent <ID>" + ChatColor.GRAY + ": Marks a ticket as normal");
         }
         return false;
     }
     
     private boolean createTicket(Player player, String[] args) {
         if (args.length < 2)
             return false;
         
         StringBuilder contents = new StringBuilder().append(args[1]);
         for (int i = 2; i < args.length; i++) {
             contents.append(" ").append(args[i]);
         }
         
         HelpTicket ticket = new HelpTicket(player.getName(), contents.toString());
         helpDeskInstance.addTicket(ticket);
         
         helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "submitted by " + ticket.getUserFiled(), ticket));
         player.sendMessage(ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "Ticket submitted. Your ticket ID is " + ChatColor.DARK_GREEN + ticket.getID());
         
         return true;
     }
     
     private boolean readTicket(Player player, String[] args) {
         if (args.length < 2) 
             return true;
 
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
         
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
         
         if (!(ticket.getUserFiled().equalsIgnoreCase(player.getName())) && !helpDeskInstance.isHelpdeskStaff(player))
             return true;
         
         if (ticket.isAssigned()) {
             player.sendMessage(ChatColor.GRAY + "Ticket assigned to " + ticket.getAssignedUser() + ":");
         }
         player.sendMessage(ChatColor.DARK_GREEN + "Ticket " + ticket.getID() + ChatColor.GRAY + " (" + ticket.getUserFiled() + "): " + ChatColor.WHITE + ticket.getContents());
         
         return true;
     }
     
     private boolean assignTicket(Player player, String[] args) {
         if (args.length < 2)
             return false;
 
         if (!helpDeskInstance.isHelpdeskStaff(player))
             return true;
         
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
         
         if (ticket.isAssigned()) {
             player.sendMessage(ChatColor.GRAY + "Ticket already assigned to " + ticket.getAssignedUser());
             return true;
         }
         
         ticket.setAssignedUser(player.getName());
         helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "assigned to " + ticket.getAssignedUser(), ticket));
         
         Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
         if (filed != null) 
             filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "assigned to " + ticket.getAssignedUser(), ticket));
         return true;
     }
 
     private boolean elevateTicket(Player player, String[] args) {
         if (args.length < 2)
             return false;
 
         if (!helpDeskInstance.isHelpdeskStaff(player))
             return true;
         
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
         
         HelpLevel level = ticket.getLevel();
         ticket.elevate(player);
         if (level != ticket.getLevel()) {
             helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "elevated to " + ticket.getLevel(), ticket));
             Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
             if (filed != null)
                 filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "elevated to " + ticket.getLevel() + " by " + player.getName(), ticket));
         } else {
             player.sendMessage(ChatColor.GRAY + "Ticket couldn't be elevated");
         }
         
         return true;
     }
 
     private boolean removeTicket(Player player, String[] args) {
         if (args.length < 2)
             return false;
 
         if (!helpDeskInstance.isHelpdeskStaff(player))
             return true;
 
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
 
         if (helpDeskInstance.removeTicket(ticket)) {
             helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "removed by " + player.getName(), ticket));
             Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
             if (filed != null)
                 filed.sendMessage(ticketWasMessage(ChatColor.RED, "removed by " + player.getName(), ticket));
         }
 
         return true;
     }
 
     private boolean completeTicket(Player player, String[] args) {
         if (args.length < 2)
             return true;
 
         if (!helpDeskInstance.isHelpdeskStaff(player))
             return true;
 
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
 
         ticket.setCompleted();
         if (helpDeskInstance.removeTicket(ticket)) {
             helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "marked as complete by " + player.getName(), ticket));
             Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
             if (filed != null)
                 filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "marked as complete by " + player.getName(), ticket));
         }
 
         return true;
     }
 
     private boolean listTickets(Player player) {
         if (!player.hasPermission("helpdesk.mod")
                 && !player.hasPermission("helpdesk.admin")
                 && !player.hasPermission("helpdesk.op"))
             return true;
         
         ArrayList<HelpTicket> tickets = helpDeskInstance.sortTicketsByTime();
         
         if (player.hasPermission("helpdesk.admin")) {
             Collections.sort(tickets, new Comparator<HelpTicket>() {
                 public int compare(HelpTicket o1, HelpTicket o2) {
                     if (o1.getLevel() == HelpLevel.ADMIN && o2.getLevel() != HelpLevel.ADMIN) {
                         return -1;
                     } else if (o2.getLevel() == HelpLevel.ADMIN && o1.getLevel() != HelpLevel.ADMIN) {
                         return 1;
                     } else if (o1.getLevel() == HelpLevel.ADMIN && o2.getLevel() == HelpLevel.ADMIN) {
                         if (o1.getID() < o2.getID()) {
                             return -1;
                         } else {
                             return 1;
                         }
                     }
                     return 0;
                 }
             });
         }
         if (player.hasPermission("helpdesk.op")) {
             Collections.sort(tickets, new Comparator<HelpTicket>() {
                 public int compare(HelpTicket o1, HelpTicket o2) {
                     if (o1.getLevel() == HelpLevel.OP && o2.getLevel() != HelpLevel.OP) {
                         return -1;
                     } else if (o2.getLevel() == HelpLevel.OP && o1.getLevel() != HelpLevel.OP) {
                         return 1;
                     } else if (o1.getLevel() == HelpLevel.OP && o2.getLevel() == HelpLevel.OP) {
                         if (o1.getID() < o2.getID()) {
                             return -1;
                         } else {
                             return 1;
                         }
                     }
                     return 0;
                 }
             });
         }
         
         Collections.sort(tickets, new Comparator<HelpTicket>() {
             public int compare(HelpTicket o1, HelpTicket o2) {
                 if (o1.isUrgent() && !o2.isUrgent()) {
                     return -1;
                 } else if (o2.isUrgent() && !o1.isUrgent()) {
                     return 1;
                 } else if (o1.isUrgent() && o2.isUrgent()) {
                     if (o1.getID() < o2.getID()) {
                         return -1;
                     } else {
                         return 1;
                     }
                 }
                 return 0;
             }
         });
         
         player.sendMessage(ChatColor.GRAY + "/ Filed Tickets");
         for (int i = 0; i < 8; i++) {
             if (tickets.size() <= i)
                 break;
             
            if (tickets.get(i).getLevel() == HelpLevel.ADMIN && !player.hasPermission("helpdesk.admin"))
                 continue;
             else if (tickets.get(i).getLevel() == HelpLevel.OP && !player.hasPermission("helpdesk.op"))
                 continue;
             
             String assignedTag = "";
             
             if (tickets.get(i).isAssigned()) {
                 if (!tickets.get(i).getAssignedUser().equals(player.getName())) {
                     continue;    
                 } else {
                     assignedTag = ChatColor.RED + "[ASSIGNED]";
                 }
             }
             
             String urgentTag = tickets.get(i).isUrgent() ? ChatColor.RED + "[!]" : "";
             player.sendMessage(ChatColor.GRAY + "| " + assignedTag + urgentTag + ChatColor.GOLD + "[" + tickets.get(i).getLevel() + "]" + ChatColor.DARK_GREEN + "Ticket " + tickets.get(i).getID() + ChatColor.GRAY + " by " + ChatColor.DARK_GREEN + tickets.get(i).getUserFiled() + ChatColor.WHITE + ": " + tickets.get(i).getContents());
         }
         return true;
     }
 
     private boolean markTicketUrgent(Player player, String[] args) {
         if (args.length < 2)
             return false;
 
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
 
         if (!(ticket.getUserFiled().equalsIgnoreCase(player.getName())) && !helpDeskInstance.isHelpdeskStaff(player))
             return true;
 
         if (!ticket.isUrgent()) {
             ticket.setUrgent(true);
             helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.RED, "marked as URGENT by " + player.getName(), ticket));
             Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
             if (filed != null) {
                 if (player == filed) {
                     filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "marked as URGENT", ticket));
                 } else {
                     filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "marked as URGENT by " + player.getName(), ticket));
                 }
             }
         }
 
         return true;
     }
 
     private boolean markTicketNormal(Player player, String[] args) {
         if (args.length < 2)
             return false;
 
         HelpTicket ticket = helpDeskInstance.getTicketWithID(args[1]);
 
         if (ticket == null) {
             player.sendMessage(ChatColor.GRAY + "Invalid ticket ID");
             return true;
         }
 
         if (!(ticket.getUserFiled().equalsIgnoreCase(player.getName())) && !helpDeskInstance.isHelpdeskStaff(player))
             return true;
 
         if (ticket.isUrgent()) {
             ticket.setUrgent(false);
             helpDeskInstance.notifyAllHelpdeskStaff(staffTicketMessage(ChatColor.DARK_GREEN, "marked as NORMAL by " + player.getName(), ticket));
             Player filed = helpDeskInstance.getServer().getPlayerExact(ticket.getUserFiled());
             if (filed != null) {
                 if (player == filed) {
                     filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "marked as NORMAL", ticket));
                 } else {
                     filed.sendMessage(ticketWasMessage(ChatColor.DARK_GREEN, "marked as NORMAL by " + player.getName(), ticket));
                 }
             }
         }
 
         return true;
     }
     
     public String ticketWasMessage(ChatColor eventColor, String eventText, HelpTicket ticket) {
         return ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "Your ticket (" + ChatColor.DARK_GREEN + ticket.getID() + ChatColor.GRAY + ") was " + eventColor + eventText;
     }
     
     public String staffTicketMessage(ChatColor eventColor, String eventText, HelpTicket ticket) {
         return ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "Ticket " + ChatColor.DARK_GREEN + ticket.getID() + ChatColor.GRAY + " was " + eventColor + eventText; 
     }
 }

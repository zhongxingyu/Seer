 package net.robbytu.banjoserver.bungee.directsupport;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Tickets {
     private static HashMap<String, Ticket> usersInTicket = new HashMap<String, Ticket>();
 
     public static Ticket[] getOpenTickets() {
         Connection conn = Main.conn;
         ArrayList<Ticket> tickets = new ArrayList<Ticket>();
 
         try {
             // Create a new select statement
             PreparedStatement statement = conn.prepareStatement("SELECT id, username, admin, status, question, server, date_created, date_accepted, date_resolved FROM bs_tickets WHERE status = 'open'");
             ResultSet result = statement.executeQuery();
 
             // For each ticket ...
             while(result.next()) {
                 // Create a new Ticket instance
                 Ticket ticket = new Ticket();
 
                 // Fill in properties
                 ticket.id = result.getInt(1);
                 ticket.username = result.getString(2);
                 ticket.admin = result.getString(3);
                 ticket.status = result.getString(4);
                 ticket.question = result.getString(5);
                 ticket.server = result.getString(6);
                 ticket.date_created = result.getInt(7);
                 ticket.date_accepted = result.getInt(8);
                 ticket.date_resolved = result.getInt(9);
 
                 // Add ban to return array
                 tickets.add(ticket);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         // Return the array of tickets
         return tickets.toArray(new Ticket[tickets.size()]);
     }
 
     public static Ticket[] getTickets(String username, String status) {
         Connection conn = Main.conn;
         ArrayList<Ticket> tickets = new ArrayList<Ticket>();
 
         try {
             // Create a new select statement
             PreparedStatement statement = conn.prepareStatement("SELECT id, username, admin, status, question, server, date_created, date_accepted, date_resolved FROM bs_tickets WHERE username LIKE ? AND status = ?");
             statement.setString(1, username);
             statement.setString(2, status);
             ResultSet result = statement.executeQuery();
 
             // For each ticket ...
             while(result.next()) {
                 // Create a new Ticket instance
                 Ticket ticket = new Ticket();
 
                 // Fill in properties
                 ticket.id = result.getInt(1);
                 ticket.username = result.getString(2);
                 ticket.admin = result.getString(3);
                 ticket.status = result.getString(4);
                 ticket.question = result.getString(5);
                 ticket.server = result.getString(6);
                 ticket.date_created = result.getInt(7);
                 ticket.date_accepted = result.getInt(8);
                 ticket.date_resolved = result.getInt(9);
 
                 // Add ban to return array
                 tickets.add(ticket);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         // Return the array of tickets
         return tickets.toArray(new Ticket[tickets.size()]);
     }
 
     public static Ticket getTicket(int id) {
         Connection conn = Main.conn;
 
         try {
             // Create a new select statement
             PreparedStatement statement = conn.prepareStatement("SELECT id, username, admin, status, question, server, date_created, date_accepted, date_resolved FROM bs_tickets WHERE id = ?");
             statement.setInt(1, id);
             ResultSet result = statement.executeQuery();
 
             // Fetch ticket
             result.next();
 
             // Create a new Ticket instance
             Ticket ticket = new Ticket();
 
             // Fill in properties
             ticket.id = result.getInt(1);
             ticket.username = result.getString(2);
             ticket.admin = result.getString(3);
             ticket.status = result.getString(4);
             ticket.question = result.getString(5);
             ticket.server = result.getString(6);
             ticket.date_created = result.getInt(7);
             ticket.date_accepted = result.getInt(8);
             ticket.date_resolved = result.getInt(9);
 
             // Return ticket
             return ticket;
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public static void updateTicket(Ticket ticket) {
         // Update Ticket object in database
         try {
             Connection conn = Main.conn;
             PreparedStatement statement = conn.prepareStatement("UPDATE bs_tickets SET username = ?, admin = ?, status = ?, question = ?, server = ?, date_created = ?, date_accepted = ?, date_resolved = ? WHERE id = ?");
 
             statement.setString(1, ticket.username);
             statement.setString(2, ticket.admin);
             statement.setString(3, ticket.status);
             statement.setString(4, ticket.question);
             statement.setString(5, ticket.server);
             statement.setInt(6, ticket.date_created);
             statement.setInt(7, ticket.date_accepted);
             statement.setInt(8, ticket.date_resolved);
             statement.setInt(9, ticket.id);
 
             statement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static void createTicket(Ticket ticket) {
         // Create Ticket object in database
         try {
             Connection conn = Main.conn;
             PreparedStatement statement = conn.prepareStatement("INSERT INTO bs_tickets (username, admin, status, question, server, date_created, date_accepted, date_resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
 
             statement.setString(1, ticket.username);
             statement.setString(2, ticket.admin);
             statement.setString(3, ticket.status);
             statement.setString(4, ticket.question);
             statement.setString(5, ticket.server);
             statement.setInt(6, ticket.date_created);
             statement.setInt(7, ticket.date_accepted);
             statement.setInt(8, ticket.date_resolved);
 
             statement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static boolean inTicket(String name) {
         return (usersInTicket.containsKey(name));
     }
 
     public static Ticket getCurrentTicketForUser(String name) {
         return (inTicket(name)) ? usersInTicket.get(name) : null;
     }
 
     public static void remindAdminsOfOpenTickets() {
         Ticket[] tickets = getOpenTickets();
         if (tickets.length > 0) {
             for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
                 if(player.hasPermission("bs.admin") || player.hasPermission("bs.helper")) {
                     player.sendMessage(" ");
                     player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Er zijn " + tickets.length + " open tickets.");
                    for(Ticket ticket : tickets) player.sendMessage(ChatColor.GOLD + "  #" + ticket.id + ": " + ((ticket.question.length() > 40) ? ticket.question.substring(0, 40) : ticket.question));
                    player.sendMessage(ChatColor.GRAY + "Je kan tickets accepteren met /ticket accept [id]");
                     player.sendMessage(" ");
                 }
             }
         }
     }
 }

 package net.robbytu.banjoserver.bungee.votes;
 
 import net.md_5.bungee.api.ChatColor;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 
 public class Votes {
     public static void handleVote(Vote vote) {
         Connection conn = Main.conn;
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM bs_votes WHERE username LIKE ? AND timestamp < ?");
             statement.setString(1, vote.username);
             statement.setInt(2, (int) ((int) System.currentTimeMillis() / 1000L));
             ResultSet result = statement.executeQuery();
 
             boolean canRedeem = true;
 
             if(result.next()) {
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(" ");
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(ChatColor.GREEN + "Bedankt voor het stemmen op de Banjoserver!");
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(" ");
                 canRedeem = false;
             }
 
             statement = conn.prepareStatement("INSERT INTO bs_votes (username, serviceName, address, timestamp, redeemed) VALUES (?, ?, ?, ?, ?)");
 
             statement.setString(1, vote.username);
             statement.setString(2, vote.serviceName);
             statement.setString(3, vote.address);
             statement.setInt(4, vote.timestamp);
             statement.setInt(5, ((canRedeem == true) ? 0 : 2));
 
             statement.executeUpdate();
 
             if(canRedeem) {
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(" ");
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(ChatColor.GREEN + "Bedankt voor het stemmen op de Banjoserver.");
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(ChatColor.GRAY + "Type nu " + ChatColor.WHITE + "/vote" + ChatColor.GRAY + " om je beloning te innen!");
                Main.instance.getProxy().getPlayer(vote.username).sendMessage(" ");
             }
         }
         catch (Exception ignored) {}
     }
 
     public static Vote[] openVotes() {
         Connection conn = Main.conn;
         ArrayList<Vote> votes = new ArrayList<Vote>();
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT username, serviceName, address, timestamp FROM bs_votes WHERE timestamp < ? AND redeemed = ?");
 
             statement.setInt(1, (int) ((int) System.currentTimeMillis() / 1000L));
             statement.setInt(2, 0);
 
             ResultSet result = statement.executeQuery();
 
             while(result.next()) {
                 Vote vote = new Vote();
 
                 vote.username = result.getString(1);
                 vote.serviceName = result.getString(2);
                 vote.address = result.getString(3);
                 vote.timestamp = result.getInt(4);
 
                 votes.add(vote);
             }
 
             return votes.toArray(new Vote[votes.size()]);
         }
         catch(Exception ignored) {
             return null;
         }
     }
 }

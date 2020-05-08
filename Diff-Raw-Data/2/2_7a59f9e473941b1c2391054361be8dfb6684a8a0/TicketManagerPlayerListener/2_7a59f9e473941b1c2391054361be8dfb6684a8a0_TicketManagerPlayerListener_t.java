 package me.pyros.ticketmanager;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.persistence.Basic;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.player.PlayerListener;
 
 public class TicketManagerPlayerListener extends PlayerListener{ 
	String user="minecraft",password="testpass";
     public static Basic plugin;
     public boolean onCommand(CommandSender player, Command cmd, String commandLabel, String[] args){
     	if(cmd.getName().equalsIgnoreCase("ticket")){
     		/////////////////////////////////////////////////////////////////////
     		if(player.hasPermission("ticketmanager.use") && args[1]=="open") {
     			String data = null;
     			String[] tags = null;
     			int i=0;
     			for(int l=2;l<(args.length);l++){
     				if(args[l]=="t:"){
     					tags[i]=args[l];
     					i++;
     				}
     				else data = data + args[l];
     			}
 	            Connection con = null;
 	            try{
 	            	con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/minecraft", user, password);
 	            	PreparedStatement st;
 	            	st = con.prepareStatement("insert into ticketmanger_tickets (user,text) values ('"+player+"','"+data+"')");
 	            	st.executeUpdate();
 	            	int id = st.getResultSet().getInt("id");
 	            	for(int l=0;l<i;l++){
 	            		st = con.prepareStatement("insert into ticketmanager_tags (id,tag) values ("+id+",'"+tags[l]+"')");
 	            		st.executeUpdate();
 	            	}
 	            	st.close();
 	            } catch (SQLException ex) {}
     		}
     		/////////////////////////////////////////////////////////////////////
     		if(player.hasPermission("ticketmanager.use") && args[1]=="info"){
 	            Connection con = null;
 	            try{
 	            	con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/minecraft", user, password);
 	            	PreparedStatement getTicket;
 	            	getTicket = con.prepareStatement("select * from ticketmanager_tickets where id="+args[2]);
 	            	getTicket.executeUpdate();
 	            	getTicket.close();
 	            	ResultSet ticket_results = getTicket.getResultSet();
 	            	
 	            	PreparedStatement getTags;
 	            	getTags = con.prepareStatement("select * from ticketmanager_tags where tid="+args[2]);
 	            	getTags.executeUpdate();
 	            	getTags.close();
 	            	String tags = null;
 	            	for(int l=0;l<4;l++){
 	            		tags = tags + getTags.getResultSet().getString("tag")+", ";
 	            	}	            	
 	            	if(player.hasPermission("ticketmanager.mod") || ""+player+""==ticket_results.getString("user")){
 		            	player.sendMessage("**********");
 	            		player.sendMessage("[TM] Ticket #"+args[2]+" Active? "+ticket_results.getInt("active"));
 	            		player.sendMessage("[TM] Date: "+ticket_results.getString("timestamp"));
 	            		player.sendMessage("[TM] User: "+ticket_results.getString("user"));
 	            		player.sendMessage("[TM] Tags: "+tags);
 	            		player.sendMessage("[TM] Text: "+ticket_results.getString("text"));
 		            	player.sendMessage("**********");
 	            	}
 	            }  catch (SQLException ex) {}	            
     		}
     		/////////////////////////////////////////////////////////////////////
     		if(player.hasPermission("ticketmanager.mod") && args[1]=="active"){
 	            Connection con = null;
 	            try{
 	            	con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/minecraft", user, password);
 	            	PreparedStatement getTickets;
 	            	getTickets = con.prepareStatement("select * from ticketmanager_tickets where active=1");
 	            	getTickets.executeUpdate();
 	            	getTickets.close();
 	            	ResultSet ticket_results = getTickets.getResultSet();
 	            	player.sendMessage("**********");
 	            	for(int l=0;l<10;l++){
 	            		player.sendMessage("[TM] User: "+ticket_results.getString("user")+". Ticket: "+ticket_results.getString("text"));
 	            	}
 	            	player.sendMessage("**********");
 	            } catch (SQLException ex) {}
     		}
     		/////////////////////////////////////////////////////////////////////
     		if(player.hasPermission("ticketmanager.mod") && args[1]=="close"){
 	            Connection con = null;
 	            try{
 	            	con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/minecraft", user, password);
 	            	PreparedStatement closeTicket;
 	            	closeTicket = con.prepareStatement("update ticketmanager_tickets set active=0 where id="+args[2]);
 	            	closeTicket.executeUpdate();
 	            	closeTicket.close();
 	            } catch (SQLException ex) {}
     		}
     		/////////////////////////////////////////////////////////////////////
     		if(args[1]=="help"){
     			if(player.hasPermission("ticketmanager.use")) {player.sendMessage("/ticket open t:tag1 t:tag2 [message]");}
     			if(player.hasPermission("ticketmanager.mod")) {
     				player.sendMessage("/ticket close [id]");
     				player.sendMessage("/ticket info [id]");
     				player.sendMessage("/ticket active (-a)");
     			}
     		}
     		/////////////////////////////////////////////////////////////////////
     	} 
     	return false; 
     }
 }

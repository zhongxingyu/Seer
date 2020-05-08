 /*
  * ForgetCommand.java
  * 
  *  $Id $
  *  Author: smadden
  *
  *    Copyright (C) 2010 Sean Madden
  *
  *    Please see the pertinent documents for licensing information.
  *
  */
 
 package com.seanmadden.deepthought.responders.factoidcommands;
 
 import java.sql.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.seanmadden.deepthought.Configuration;
 import com.seanmadden.deepthought.IRCClient;
 import com.seanmadden.deepthought.Message;
 import com.seanmadden.deepthought.responders.FactoidResponder;
 
 public class ForgetCommand extends FactoidCommand {
 
 	public ForgetCommand(FactoidResponder resp) {
 		super(resp);
 	}
 
 	@Override
 	public boolean checkCommand(Message message, IRCClient client) {
 		if (message.getMessage().matches(client.getNick() + "[:,] forget that$")) {
 			if (message.getUser() != null && !message.getUser().isOpper()) {
 				return false;
 			}
 			Connection conn = Configuration.getInstance().getConn();
 			PreparedStatement ps;
 			try {
 				if(resp.lastID == 0){
 					ps = conn.prepareStatement("select id from factoids order by id group by id limit 1;");
 					ps.execute();
 					ResultSet rs = ps.getResultSet();
 					if(rs == null){
 						return false;
 					}
 					resp.lastID = rs.getInt("id");
 				}
				ps = conn.prepareStatement("select * from factoids where id is ?;");
 				ps.setInt(1, resp.lastID);
 				ps.execute();
 				ResultSet rs = ps.getResultSet();
 				if(rs == null){
 					return false;
 				}
 				String trigger = rs.getString("trigger").replaceAll("%", "");
 				String response = rs.getString("response");
 				
 				ps = conn
						.prepareStatement("delete from factoids where id is ?;");
 				ps.setInt(1, resp.lastID);
 				ps.execute();
 				message.respondWith("Okay " +message.getNick()+ ", I forgot that " + trigger + " is "
 						+ response, client);
 				return true;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else if (message.getMessage().matches(client.getNick()+"[:,] (?:forget ?|delete ?)#(\\d+)$")) {
 			Matcher m = Pattern.compile(client.getNick()+"[:,] (?:forget ?|delete ?)#(\\d+)$").matcher(
 					message.getMessage());
 			if (!m.matches()) {
 				return false;
 			}
 			try {
 				int number = Integer.parseInt(m.group(1));
 				Connection conn = Configuration.getInstance().getConn();
 				PreparedStatement ps;
 				try {
					ps = conn.prepareStatement("select * from factoids where id is ?;");
 					ps.setInt(1, number);
 					ps.execute();
 					ResultSet rs = ps.getResultSet();
 					if(rs == null){
 						return false;
 					}
 					String trigger = rs.getString("trigger").replaceAll("%", "");
 					String response = rs.getString("response");
 					
 					ps = conn
							.prepareStatement("delete from factoids where id is ?;");
 					ps.setInt(1, number);
 					ps.execute();
 					message.respondWith("Okay " +message.getNick()+ ", I forgot that " + trigger + " is "
 							+ response, client);
 					return true;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} catch (NumberFormatException e) {
 				message.respondWith("That's not a number!", client);
 				return false;
 			}
 
 		}
 		return false;
 	}
 
 }

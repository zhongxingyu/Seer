 package to.joe.manager;
 
 //import java.net.InetSocketAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 //import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 import to.joe.J2;
 import to.joe.util.Ban;
 import to.joe.util.Flag;
 import to.joe.util.Report;
 import to.joe.util.User;
 import to.joe.util.Warp;
 
 public class MySQL {
 	private String user,pass,db;
 	private int serverNumber;
 	private J2 j2;
 	private String aliasdb="alias";
 	//private SimpleDateFormat formatter = new SimpleDateFormat("MM-dd hh:mm");
 	public MySQL(String User,String Pass, String DB, int ServerNumber, J2 J2){
 		user=User;
 		pass=Pass;
 		db=DB;
 		serverNumber=ServerNumber;
 		j2=J2;
 	}
 	public String user(){
 		return user;
 	}
 	public String pass(){
 		return pass;
 	}
 	public String db(){
 		return db;
 	}
 	public int servnum(){
 		return serverNumber;
 	}
 	public Connection getConnection() {
 		try {
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException ex) {
 				j2.logWarn(ChatColor.RED+"SQL FAIL D:");
 			}
 			return DriverManager.getConnection(db + "?autoReconnect=true&user=" + user + "&password=" + pass);
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+"SQL FAIL :(");
 		}
 		return null;
 	}
 
 
 	public ChatColor toColor(int input){
 		switch(input){
 		case 0: return ChatColor.BLACK;
 		case 1: return ChatColor.DARK_BLUE;
 		case 2: return ChatColor.DARK_GREEN;
 		case 3: return ChatColor.DARK_AQUA;
 		case 4: return ChatColor.DARK_RED;
 		case 5: return ChatColor.DARK_PURPLE;
 		case 6: return ChatColor.GOLD;
 		case 7: return ChatColor.GRAY;
 		case 8: return ChatColor.DARK_GRAY;
 		case 9: return ChatColor.BLUE;
 		case 10: return ChatColor.GREEN;
 		case 11: return ChatColor.AQUA;
 		case 12: return ChatColor.RED;
 		case 13: return ChatColor.LIGHT_PURPLE;
 		case 14: return ChatColor.YELLOW;
 		case 15: return ChatColor.WHITE;
 		}
 		return null;
 	}
 
 	public String stringClean(String toClean){
 		return toClean.replace('\"', '_').replace('\'', '_').replace(';', '_').replace(',', '_');
 	}
 
 	public User getUser(String name){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			String state="SELECT * FROM j2users WHERE name=\""+ name +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			if(rs.next()){
 				String Flags=rs.getString("flags");
 				ArrayList<Flag> flags=new ArrayList<Flag>();
 				for(int x=0;x<Flags.length();x++){
 					flags.add(Flag.byChar(Flags.charAt(x)));
 				}
 				j2.debug("User "+name+" in "+rs.getString("group")+" with "+ Flags);
 				return new User(name, toColor(rs.getInt("color")), rs.getString("group"), flags, j2.getServer().getWorld("world"), rs.getString("safeword"));
 			}
 			else{
 				String state2="INSERT INTO j2users (`name`,`group`,`color`,`flags`) values (\""+name+"\",\"regular\",10,\"n\")";
 				j2.debug("Query: "+state2);
 				ps = conn.prepareStatement(state2);
 				ps.executeUpdate();
 				ArrayList<Flag> f=new ArrayList<Flag>();
 				f.add(Flag.NEW);
 				return new User(name, ChatColor.GREEN, "regular", f,j2.getServer().getWorld("world"),"");
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user "+name+" from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 				j2.logWarn(ChatColor.RED+ "Unable to load user "+name+" from MySQL. Oh hell");
 			}
 		}
 		j2.logWarn(ChatColor.RED+ "Unable to load user "+name+" from MySQL. Oh hell");
 		return null;
 	}
 
 
 
 	public void setFlags(String name, ArrayList<Flag> flags){
 		j2.debug("Calling setFlags");
 		Connection conn = null;
 		PreparedStatement ps = null;
 		name=stringClean(name);
 		String flaglist="";
 		if(!flags.isEmpty()){
 			for(Flag f : flags){
 				flaglist+=f.getChar();
 			}
 		}
 		try {
 			conn = getConnection();
 			String state="UPDATE j2users SET flags=\""+ flaglist +"\" WHERE name=\""+ name +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 	public void setGroup(String name, String group){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		name=stringClean(name);
 		group=stringClean(group);
 		try {
 			conn = getConnection();
 			String state="UPDATE j2users SET group=\""+ group +"\" WHERE name=\""+ name +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 	public void ban(String name,String reason, long time, String admin,Location location){
 		j2.mcbans.processBan(name, admin, reason);
 		j2.panda.remove(name);
 		Connection conn = null;
 		PreparedStatement ps = null;
 		double x=0,y=0,z=0;
 		float pitch=0,yaw=0;
 		String world="";
 		if(location!=null){
 			x=location.getX();
 			y=location.getY();
 			z=location.getZ();
 			pitch=location.getPitch();
 			yaw=location.getYaw();
 			world=location.getWorld().getName();
 		}
 		try {
 			conn = getConnection();
 			Date curTime=new Date();
 			long timeNow=curTime.getTime()/1000;
 			long unBanTime;
 			if(time==0)
 				unBanTime=0;
 			else
 				unBanTime=timeNow+(60*time);
 			String state="INSERT INTO j2bans (name,reason,admin,unbantime,timeofban,x,y,z,pitch,yaw,world,server) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setString(1, stringClean(name.toLowerCase()));
 			ps.setString(2, stringClean(reason));
 			ps.setString(3, stringClean(admin));
 			ps.setLong(4, unBanTime);
 			ps.setLong(5, timeNow);
 			ps.setDouble(6, x);
 			ps.setDouble(7,y);
 			ps.setDouble(8,z);
 			ps.setFloat(9,pitch);
 			ps.setFloat(10, yaw);
 			ps.setString(11, world);
 			ps.setInt(12, this.serverNumber);
 			ps.executeUpdate();
 			Ban newban=new Ban(name.toLowerCase(),reason,unBanTime,timeNow,timeNow,false);
 			j2.kickbans.bans.add(newban);
 
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 	public String checkBans(String username){
 		Date curTime=new Date();
 		long timeNow=curTime.getTime()/1000;
 		String reason=null;
 		ArrayList<Ban> banhat=new ArrayList<Ban>(j2.kickbans.bans);
 		for (Ban ban : banhat){
 			if(ban.isBanned() && ban.isTemp() && ban.getTimeOfUnban()<timeNow){
 				//unban(user);
 				//tempbans
 			}
 			if(ban.getTimeLoaded()>timeNow-60 && ban.getName().equalsIgnoreCase(username) && ban.isBanned()){
 				reason="Banned: "+ban.getReason();
 			}
 			if(ban.getTimeLoaded()<timeNow-60){
 				j2.kickbans.bans.remove(ban);
 			}
 		}
 		if(reason==null){
 			Connection conn = null;
 			PreparedStatement ps = null;
 			ResultSet rs = null;
 			try {
 				conn = getConnection();
 				String state="SELECT name,reason,unbantime,timeofban FROM j2bans WHERE unbanned=0 and name=\""+username+"\"";
 				j2.debug("Query: "+state);
 				ps = conn.prepareStatement(state);
 				rs = ps.executeQuery();
 				while (rs.next()) {
 					reason=rs.getString("reason");
 					Ban ban=new Ban(rs.getString("name"),reason,rs.getLong("unbantime"),timeNow,rs.getLong("timeofban"),false);
 					j2.kickbans.bans.add(ban);
 					reason="Banned: "+reason;
 				}
 			} catch (SQLException ex) {
 				j2.logWarn(ChatColor.RED+ "Unable to load j2Bans. You're not going to like this.");
 			} finally {
 				try {
 					if (ps != null) {
 						ps.close();
 					}
 					if (rs != null) {
 						rs.close();
 					}
 					if (conn != null) {
 						conn.close();
 					}
 				} catch (SQLException ex) {
 				}
 			}
 		}
 		return reason;
 	}
 	public ArrayList<Ban> getBans(String playerName, boolean allbans){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String username=this.stringClean(playerName);
 		ArrayList<Ban> bans=new ArrayList<Ban>();
 		try {
 			conn = getConnection();
 			String notallbans="";
 			if(!allbans)
 				notallbans=" and unbanned=0";
 			String state="SELECT name,reason,timeofban,unbantime,unbanned FROM j2bans WHERE name=\""+username+"\""+notallbans;
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				bans.add(new Ban(rs.getString("name"),rs.getString("reason"),rs.getLong("unbantime"),0,rs.getLong("timeofban"),rs.getBoolean("unbanned")));
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load j2Bans. You're not going to like this.");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return bans;
 	}
 	public void unban(String aname){
 		j2.mcbans.processUnban(aname);
 		Connection conn = null;
 		PreparedStatement ps = null;
 		String name=stringClean(aname);
 		try {
 			conn = j2.mysql.getConnection();
 
 			for (Ban ban : j2.kickbans.bans) {
 				if (ban.getName().equalsIgnoreCase(name)) {
 					ban.unBan();
 				}
 			}
 			String state="UPDATE j2bans SET unbanned=1 WHERE name=\""+ name.toLowerCase() +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public void loadMySQLData() {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 
 		try {
 			conn = getConnection();
 			HashMap<String, ArrayList<Flag>> groups = new HashMap<String, ArrayList<Flag>>();
 			String state="SELECT name,flags FROM j2groups where server=" + serverNumber;
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				String name=rs.getString("name");
 
 				String Flags=rs.getString("flags");
 				j2.debug("Group: "+name+" with flags "+Flags);
 				ArrayList<Flag> flags=new ArrayList<Flag>();
 				for(int x=0;x<Flags.length();x++){
 					flags.add(Flag.byChar(Flags.charAt(x)));
 				}
 				groups.put(name, flags);
 			}
 			j2.debug("Loaded "+groups.size()+ " groups");
 			j2.users.setGroups(groups);
 
 			//reports
 			String state2="SELECT id,user,x,y,z,pitch,yaw,message,world,time,closed from reports where server="+serverNumber+" and closed=0";
 			ps = conn.prepareStatement(state2);
 			j2.debug("Query: "+state2);
 			rs = ps.executeQuery();
 			while (rs.next()){
 				String user=rs.getString("user");
 				Location loc=new Location(j2.getServer().getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw"));
 				j2.reports.addReportViaSQL(new Report(rs.getInt("id"), loc, user, rs.getString("message"),rs.getLong("time"),rs.getBoolean("closed")));
 				j2.debug("Adding new report to list, user "+user);
 			}
 
 			//warps
 
 			String state3="SELECT * FROM warps where server=" + serverNumber+" and flag!=\"w\"";
 			j2.debug("Query: "+state3);
 			ps = conn.prepareStatement(state3);
 			rs = ps.executeQuery();
 			int count=0;
 			while (rs.next()) {
 				j2.warps.addWarpViaMysql(new Warp(rs.getString("name"), rs.getString("player"), 
 						new Location(j2.getServer().getWorld(rs.getString("world")),
 								rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
 								rs.getFloat("pitch"), rs.getFloat("yaw")),
 								Flag.byChar(rs.getString("flag").charAt(0))));
 				count++;
 			}
 			j2.debug("Loaded "+count+ " warps");
 
 			//jailing
 
 			/*String state4="SELECT user,reason from jail where server="+serverNumber+" and free=0";
 			ps = conn.prepareStatement(state4);
 			j2.debug("Query: "+state4);
 			rs = ps.executeQuery();
 			HashMap<String,String> tempjail=new HashMap<String,String>();
 			while (rs.next()){
 				tempjail.put(rs.getString("user"), rs.getString("reason"));				
 			}
 			j2.log.info("Loaded "+tempjail.size()+" jailings");
 			j2.users.jailSet(tempjail);*/
 
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load base data from MySQL. Oh hell");
 			j2.maintenance=true;
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public void addReport(Report report){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			Location loc=report.getLocation();
 			long time=report.getTime();
 			//String state="INSERT INTO reports (`user`,`message`,`x`,`y`,`z`,`pitch`,`yaw`,`server`,`world`,`time`) VALUES ('?','?',?,?,?,?,?,?,'?',?)";
 			String state="INSERT INTO `reports` (`user`,`message`,`x`,`y`,`z`,`pitch`,`yaw`,`server`,`world`,`time`) VALUES ('"+stringClean(report.getUser())+"','"+stringClean(report.getMessage())+"',"+loc.getX()+","+loc.getY()+","+loc.getZ()+","+loc.getPitch()+","+loc.getYaw()+","+serverNumber+",'"+loc.getWorld().getName()+"',"+time+");";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			//j2.log.info(report.getUser());
 			//String cleanUser=stringClean(report.getUser());
 			//j2.log.info(cleanUser);
 			//String cleanMessage=stringClean(report.getMessage());
 			/*ps.setString(1, cleanUser);
 			ps.setString(2, cleanMessage);
 			ps.setDouble(3, loc.getX());
 			ps.setDouble(4, loc.getY());
 			ps.setDouble(5, loc.getZ());
 			ps.setFloat(6, loc.getPitch());
 			ps.setFloat(7, loc.getYaw());
 			ps.setInt(8, serverNumber);
 			ps.setString(9, loc.getWorld().getName());
 			ps.setLong(10, time);*/
 			ps.executeUpdate();
 			String state2="SELECT id,user,x,y,z,pitch,yaw,message,world,time,closed from reports where server="+serverNumber+" and closed=0 and id>"+j2.reports.maxid;
 			ps = conn.prepareStatement(state2);
 			j2.debug("Query: "+state2);
 			rs = ps.executeQuery();
 			while (rs.next()){
 				String user=rs.getString("user");
 				Location loc2=new Location(j2.getServer().getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("pitch"), rs.getFloat("yaw"));
 				j2.reports.addReportAndAlert(new Report(rs.getInt("id"), loc2, user, rs.getString("message"),rs.getLong("time"),rs.getBoolean("closed")));
 				j2.debug("Adding new report to list, user "+user);
 			}
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public void closeReport(int id, String admin, String reason){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		try {
 			conn = getConnection();
 			String state="UPDATE reports SET closed=1,admin=?,reason=? where id=?";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setString(1, admin);
 			ps.setString(2,reason);
 			ps.setInt(3, id);
 			ps.executeUpdate();
 			j2.debug("Report "+id+" closed by "+admin);
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public ArrayList<Warp> getHomes(String playername){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		ArrayList<Warp> homes=new ArrayList<Warp>();
 		try {
 			conn = getConnection();
 			String state="SELECT * FROM warps where server=? and flag=? and player=?";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setInt(1, serverNumber);
 			ps.setString(2, String.valueOf(Flag.Z_HOME_DESIGNATION.getChar()));
 			ps.setString(3, playername);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				homes.add(new Warp(rs.getString("name"), rs.getString("player"), 
 						new Location(j2.getServer().getWorld(rs.getString("world")),
 								rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
 								rs.getFloat("pitch"), rs.getFloat("yaw")),
 								Flag.byChar(rs.getString("flag").charAt(0))));
 			}
 			j2.debug("Loaded "+homes.size()+ " warps");
 
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load homes for "+playername+" from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return homes;
 	}
 
 	public void removeWarp(Warp warp){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		try {
 			conn = getConnection();
 			String state="DELETE FROM warps WHERE name=? and player=? and server=? and flag=?";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setString(1, warp.getName());
 			ps.setString(2, warp.getPlayer());
 			ps.setInt(3, serverNumber);
 			ps.setString(4, String.valueOf(warp.getFlag().getChar()));
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 	public void addWarp(Warp warp){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		try {
 			conn = getConnection();
 			String state="INSERT INTO warps (name,player,server,flag,world,x,y,z,pitch,yaw) VALUES (?,?,?,?,?,?,?,?,?,?)";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setString(1, stringClean(warp.getName()));
 			ps.setString(2, stringClean(warp.getPlayer()));
 			ps.setInt(3, serverNumber);
 			ps.setString(4, String.valueOf(warp.getFlag().getChar()));
 			Location loc=warp.getLocation();
 			ps.setString(5,loc.getWorld().getName());
 			ps.setDouble(6, loc.getX());
 			ps.setDouble(7, loc.getY());
 			ps.setDouble(8, loc.getZ());
 			ps.setFloat(9, loc.getPitch());
 			ps.setFloat(10, loc.getPitch());
 			ps.executeUpdate();
 		} catch (SQLException ex) {
 
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public void userIP(String name,String ip){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			//String ip=address.getAddress().getHostAddress();
 			String state="SELECT * FROM "+aliasdb+" WHERE Name=\""+ name +"\" and IP=\""+ip+"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			if(rs.next()){
 				int count = rs.getInt("Logins");
 				count++;
 				ps = conn.prepareStatement("UPDATE "+aliasdb+" set Logins="+count+",Time=now() where Name=\""+ name +"\" and IP=\""+ip+"\"");
 				ps.executeUpdate();
 			}
 			else{
 				String state2="INSERT INTO "+aliasdb+" (`Name`,`IP`,`Time`,`Logins`) values (\""+name+"\",\""+ip+"\",now(),1)";
 				j2.debug("Query: "+state2);
 				ps = conn.prepareStatement(state2);
 				ps.executeUpdate();
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 	}
 
 	public ArrayList<String> IPGetIPs(String name){
 		ArrayList<String> IPs=new ArrayList<String>();
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			String state="SELECT IP FROM "+aliasdb+" WHERE Name=\""+ name +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			while(rs.next()){
 				IPs.add(rs.getString("IP"));
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return IPs;
 	}
 
 	public ArrayList<String> IPGetNames(String IP){
 		ArrayList<String> names=new ArrayList<String>();
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			String state="SELECT Name FROM "+aliasdb+" WHERE IP=\""+ IP +"\"";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			while(rs.next()){
 				names.add(rs.getString("Name"));
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return names;
 	}
 
 	public HashMap<String, Long> IPGetNamesOnIP(String IP){
 		HashMap<String, Long> nameDates = new HashMap<String, Long>();
 
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			String state="SELECT `Name`, `Time` FROM "+aliasdb+" WHERE IP=\""+ IP +"\" ORDER BY `Time` DESC LIMIT 5";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			while(rs.next()){
				nameDates.put(rs.getString("Name"), rs.getTimestamp("Time").getTime());
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 			j2.debug("Exception: " + ex.getMessage());
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return nameDates;
 	}
 
 	public String IPGetLast(String name){
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		name=name.split(" ")[0];
 		String result="";
 		try {
 			conn = getConnection();
 			String state="SELECT IP FROM "+aliasdb+" WHERE Name='"+name+"' order by Time desc limit 1";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			rs = ps.executeQuery();
 			if(rs.next()){
 				result=rs.getString("IP");
 			}
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return result;
 	}
 
 	public HashMap<String,Flag> getPerms(){
 		HashMap<String,Flag> perms=new HashMap<String,Flag>();
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			conn = getConnection();
 			String state="SELECT permission,flag FROM perms where server=?";
 			j2.debug("Query: "+state);
 			ps = conn.prepareStatement(state);
 			ps.setInt(1, serverNumber);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				perms.put(rs.getString("permission"), Flag.byChar(rs.getString("flag").charAt(0)));
 			}
 			j2.debug("Loaded "+perms.size()+ " permissions");
 
 		} catch (SQLException ex) {
 			j2.logWarn(ChatColor.RED+ "Unable to load user/ip from MySQL. Oh hell");
 		} finally {
 			try {
 				if (ps != null) {
 					ps.close();
 				}
 				if (rs != null) {
 					rs.close();
 				}
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException ex) {
 			}
 		}
 		return perms;
 	}
 }

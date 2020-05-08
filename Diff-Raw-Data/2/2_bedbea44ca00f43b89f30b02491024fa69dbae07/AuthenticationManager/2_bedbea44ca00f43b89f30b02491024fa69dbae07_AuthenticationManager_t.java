 package com.nexus.main;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 
 import com.nexus.NexusServer;
 import com.nexus.interfaces.ITickHandler;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.mysql.TableList;
 import com.nexus.utils.JSONPacket;
 import com.nexus.utils.Utils;
 
 public class AuthenticationManager implements ITickHandler{
 
 	private final int MaxIdleTime = 1800;
 	private int AuthUpdateTicks = 0;
 	
 	private final String Salt = "f0c194aa2f260785fcc138ef2c823b662abc2427";
 	
 	public AuthenticationManager(){
 		NexusServer.Instance.Timer.RegisterTickHandler(this);
 	}
 	
 	public boolean isTokenExpired(String Token){
 		try{
 			Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 		    ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Token='%s'", TableList.TABLE_SESSIONS, Token));
 		    
 		    if(!rs.first()){
 		    	rs.close();
 		    	stmt.close();
 		    	conn.close();
 		    	return true;
 		    }
 		    
 		    Timestamp timestamp = rs.getTimestamp("Alive");
 		    conn.close();
 		    
 		    if(rs.wasNull()){
 		    	rs.close();
 		    	stmt.close();
 		    	conn.close();
 		    	return true;
 		    }
 		    
 		    Date date = timestamp;
 		    Date now = Calendar.getInstance().getTime();
 		    
 		    this.UpdateTimestamps();
 		    
 		    int NowTime = Integer.parseInt(Long.toString(now.getTime()).substring(0,Long.toString(now.getTime()).length() - 3));
 		    int Expires = Integer.parseInt(Long.toString(date.getTime()).substring(0,Long.toString(date.getTime()).length() - 3));
 
 		    rs.close();
 		    stmt.close();
 		    return (NowTime - Expires) > MaxIdleTime;
 		}catch (SQLException e) {
 			e.printStackTrace();
 			return true;
 		}
 	}
 	
 	public boolean isTokenValid(String Token, String IPAddress){
 		try {
 			Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 		    ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM %s WHERE Address='%s' AND Token='%s'", TableList.TABLE_SESSIONS, IPAddress, Token));
 
 		    if(!rs.first()){
 		    	rs.close();
 		    	stmt.close();
 		    	conn.close();
 		    	return true;
 		    }
 
 		    if(rs.getInt(1) == 0){
 		    	rs.close();
 		    	stmt.close();
 			    conn.close();
 		    	return false;
 		    }
 		    
 		    rs.close();
 		    stmt.close();
 		    conn.close();
 		    return !isTokenExpired(Token);
 		}catch(SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public void UpdateTimestamps(){
 		//TODO ENABLE TOKEN EXPIRING AND FIX KEEPALIVE
 	    /*try {
 			Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 			stmt.executeUpdate(String.format("UPDATE %s SET Alive=NULL where NOW() - Alive > %s", TableList.TABLE_SESSIONS, MaxIdleTime));
 
 		    stmt.close();
 		    conn.close();
 		}catch(Exception e){
 			
 		}*/
 	}
 	
 	public void KillAllTokens(){
 		try {
 			Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 			stmt.executeUpdate(String.format("UPDATE %s SET Alive=NULL", TableList.TABLE_SESSIONS));
 		    stmt.close();
 			conn.close();
 		}catch(Exception e){
 			
 		}
 	}
 	
 	public JSONPacket GetToken(String MasterToken, String ClientIP){
 		JSONPacket Packet = new JSONPacket();
 		NexusServer.Instance.AuthenticationManager.UpdateTimestamps();
 		if(!NexusServer.Instance.AuthenticationManager.isTokenValid(MasterToken, ClientIP)){
 			Packet.addErrorPayload("Invalid Token");
 			return Packet;
 		}
 		try {
 			Connection conn = MySQLHelper.GetConnection();
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Token = '%s'", TableList.TABLE_SESSIONS, MasterToken));
 			rs.first();
 			if(rs.getString("Address").equalsIgnoreCase(ClientIP)){
 				try {
 					Connection conn1 = MySQLHelper.GetConnection();
 				    Statement stmt1 = conn.createStatement();
 				    ResultSet rs1 = stmt1.executeQuery(String.format("SELECT COUNT(*) FROM %s WHERE Username='%s'", TableList.TABLE_USERS, rs.getString("User")));
 				    rs1.first();
 				    if(rs1.getInt(1) == 1) {
 				    	Calendar cal = Calendar.getInstance();
 				    	Date now = cal.getTime();
 				    	String Token = CreateToken(rs.getString("User"));
 				    	System.out.println(Token);
 				    	String time = Long.toString(now.getTime());
 				    	stmt.executeUpdate(String.format("INSERT INTO %s (User, Token, Address, Created) VALUES ('%s', '%s', '%s', '%s')", TableList.TABLE_SESSIONS, rs.getString("User"), Token, ClientIP, time));
 				    	rs.close();
 					    stmt.close();
 					    conn.close();
 					    rs1.close();
 					    stmt1.close();
 					    conn1.close();
 					    Packet.addErrorPayload("none");
 					    Packet.put("token", Token);
 						return Packet;
 				    }
 					rs1.close();
 					stmt1.close();
 					conn1.close();
 				}catch (SQLException e){
 				    rs.close();
 				    stmt.close();
 				    conn.close();
 					System.err.println("ERR2");
 					e.printStackTrace();
 					Packet.addErrorPayload("Database Error");
 					return Packet;
 				}
 			}
 		    rs.close();
 		    stmt.close();
 		    conn.close();
 		}catch(Exception e){
 			System.err.println("ERR1");
 			e.printStackTrace();
 			Packet.addErrorPayload("Database Error");
 			return Packet;
 		}
 		Packet.addErrorPayload("Invalid Token request");
 		return Packet;
 	}
 	
 	public JSONPacket GetToken(String Username, String Hash, String ClientIP){
 		JSONPacket Packet = new JSONPacket();
 		
 		String PasswordInput = Hash.split(this.Salt)[0];
 		@SuppressWarnings("unused")
 		String TimestampInput = Hash.split(this.Salt)[1];
 		
     	Calendar cal = Calendar.getInstance();
     	Date now = cal.getTime();
     	
     	@SuppressWarnings("unused")
 		int Time = Integer.parseInt(Long.toString(now.getTime()).substring(0,Long.toString(now.getTime()).length() - 3));
     	
 		/*if((GetSHA1String(Integer.toString(Time)).equalsIgnoreCase(TimestampInput)) ||
 			(GetSHA1String(Integer.toString(Time-1)).equalsIgnoreCase(TimestampInput)) ||
 			(GetSHA1String(Integer.toString(Time-2)).equalsIgnoreCase(TimestampInput)) ||
 			(GetSHA1String(Integer.toString(Time-3)).equalsIgnoreCase(TimestampInput)) ||
 			(GetSHA1String(Integer.toString(Time-4)).equalsIgnoreCase(TimestampInput)) ||
 			(GetSHA1String(Integer.toString(Time-5)).equalsIgnoreCase(TimestampInput))){}else{
 			Packet.addErrorPayload("Invalid timestamp");
 			return Packet;
 		}*/
 		
 		try{
 			Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 		    ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM %s WHERE Username='%s' AND Password='%s'", TableList.TABLE_USERS, Username, GetSHA1String(PasswordInput)));
 		    rs.first();
 		    
 		    if(rs.getInt(1) == 1) {
 		    	String Token = CreateToken(Username);
 		    	String time = Long.toString(now.getTime());
 		    	stmt.executeUpdate(String.format("INSERT INTO %s (User, Token, Address, Created) VALUES ('%s', '%s', '%s', '%s')", TableList.TABLE_SESSIONS, Username, Token, ClientIP, time));
 		    	
 		    	Packet.addErrorPayload("none");
 		    	Packet.put("token", Token);
 		    }else{
 		    	Packet.addErrorPayload("Invalid username or password");
 		    }
 		    rs.close();
 		    stmt.close();
 		    conn.close();
 		}catch (SQLException e){
 			e.printStackTrace();
 			Packet.addErrorPayload("Database Error");
 		}
 		return Packet;
 	}
 	
 	public static String CreateToken(String Username){
		return Utils.encrypt(Username + ":" + Long.toString(System.nanoTime())).replace("=", "");
 	}
 	
 	public static String GetSHA1String(String Input){
 		try {
 			MessageDigest md;
 			md = MessageDigest.getInstance("SHA-1");
 	    	md.reset();
 	    	byte[] b = md.digest(Input.getBytes());
 	    	
 	    	String result = "";
 	    	for (int i=0; i < b.length; i++) {
 	    		result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
 	    	}
 	    	return result;
 		}catch(NoSuchAlgorithmException e){
 			return "";
 		}
 	}
 
 	@Override
 	public void OnTick(){
 		AuthUpdateTicks ++;
 		if(AuthUpdateTicks >= NexusServer.Instance.Timer.GetTicksPerSecond() * 60){
 			AuthUpdateTicks = 0;
 			this.UpdateTimestamps();
 		}
 	}
 }

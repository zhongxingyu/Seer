 package failkidz.fkzteam.beans;
 
 import java.sql.Array;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class TeamHandler {
 	Connection conn;
 	ArrayList<String> players;
 	ArrayList<String> teams;
 	public TeamHandler() {
 		players = new ArrayList<String>();
 		teams = new ArrayList<String>();
 	}
 
 	public void addTeam(String teamName){
 		System.out.println("Added team");
 		teams.add(teamName);
 	}
 
 	public void addPlayer(String playerName){
 		System.out.println("Added player");
 		players.add(playerName);
 	}
 
 	public String viewTeams(){
 		try{
 		Context initCtx = new InitialContext();
 		Context envCtx = (Context) initCtx.lookup("java:comp/env");
 		DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 		conn = ds.getConnection();
 		Statement stmt = conn.createStatement();
 		ResultSet rs = null;        
 		String query = "SELECT * FROM teams;";
 		rs = stmt.executeQuery(query);
 		
 		StringBuilder sb = new StringBuilder();
 		int size;
 		sb.append("<table class=\"table table-hover\">");
 		sb.append("<thead><th>Team #</th><th>Team Name</th><th>Player 1 Name</th><th>Player 2 Name</th></thead>");
 		size = sb.length();
 		while(rs.next()){
 			sb.append("<tr>");
 			sb.append("<td>"); sb.append(rs.getString(1)); sb.append("</td>");
 			sb.append("<td>"); sb.append(rs.getString(2)); sb.append("</td>");
 			sb.append("<td>"); sb.append(rs.getString(3)); sb.append("</td>");
 			sb.append("<td>"); sb.append(rs.getString(4)); sb.append("</td>");
 			sb.append("</tr>");
 		}
 		sb.append("</table>");
		rs.close();
		stmt.close();
		conn.close();
 		if(sb.length() == (size +8)){
 			return "There are currently no teams to list.";
 		}
 		
 		return sb.toString();
 		
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		return "";
 	}
 
 	public void createTeams(){
 		if(teams.size() == 0 || players.size() == 0){
 			System.out.println("size = 0");
 			return;
 		}
 
 		if( ( players.size() & 1 ) != 0 ){
 			players.add("Guest player");
 		}
 
 		Collections.shuffle(players);
 
 		int index = 0;
 		for(String team : teams){
 			System.out.println(team);
			if(index >= players.size())
				break;
 			new Team(players.get(index++),players.get(index++),team).insert();
 		}
		teams.clear();
		players.clear();
 
 	}
 }

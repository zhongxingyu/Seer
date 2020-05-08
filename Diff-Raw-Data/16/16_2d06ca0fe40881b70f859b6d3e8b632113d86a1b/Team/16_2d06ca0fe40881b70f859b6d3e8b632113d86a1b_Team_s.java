 package failkidz.fkzteam.beans;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class Team {
 	String player1;
 	String player2;
 	String teamName;
 
 	public Team(String player1, String player2, String teamName){
 		this.player1 = player1;
 		this.player2 = player2;
 		this.teamName = teamName;
 	}
 
 	public void insert(){
 		try {
 			Context initCtx = new InitialContext();
 			Context envCtx;	
 			envCtx = (Context) initCtx.lookup("java:comp/env");
 			DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 			Connection conn = ds.getConnection();
 			Statement st = conn.createStatement();
 			System.out.println("About to add entry to DB");
 			st.execute("INSERT INTO teams VALUES ( 0 , '"+teamName+"','"+player1+"','"+player2+"');");
 			System.out.println("Added entry to DB");
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 }

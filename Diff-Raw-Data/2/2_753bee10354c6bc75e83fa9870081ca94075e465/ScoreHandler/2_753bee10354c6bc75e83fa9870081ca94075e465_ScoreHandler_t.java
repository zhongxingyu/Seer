 package failkidz.fkzteam.beans;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class ScoreHandler {
 	
 	ArrayList<ScoreRowBean> rows;
 	
 	public ScoreHandler(){
 		rows = new ArrayList<ScoreRowBean>();
 	}
 	
 	public void getRows(){
 		Connection conn = null;
 		
 		//Create the connection to the database
 		try{
 			Context initCtx = new InitialContext();
 			Context envCtx = (Context) initCtx.lookup("java:comp/env");
 			DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 			conn = ds.getConnection();			
 		}
 		catch(SQLException e){
 			
 		}
 		catch(NamingException e){
 
 		}
 		
 		//Execute the query and read the resultset
 		ResultSet rs = null;
 		Statement stmt = null;
 		try{
 			stmt = conn.createStatement();
			String query = "SELECT * FROM score ORDER BY points DESC;";
 			rs = stmt.executeQuery(query);
 			while(rs.next()){
 				ScoreRowBean row = new ScoreRowBean(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),rs.getInt(7));
 				rows.add(row);
 			}
 		}
 		catch(SQLException e){
 			
 		}
 		finally{
 			try {
 				rs.close();
 				stmt.close();
 				conn.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public String getHtmlTable(){
 		StringBuilder sb = new StringBuilder();
 		
 		if(rows.size() > 0){
 			sb.append("<table class=\"table table-hover\">\n");
 			sb.append("<thead>\n");
 			sb.append("<tr>\n");
 			sb.append("<th>Team name</th>\n");
 			sb.append("<th>Games played</th>\n");
 			sb.append("<th>Games won</th>\n");
 			sb.append("<th>Games lost</th>\n");
 			sb.append("<th>Goal scores</th>\n");
 			sb.append("<th>Goal against</th>\n");
 			sb.append("<th>Goal difference</th>\n");
 			sb.append("<th>Points</th>\n");
 			sb.append("</tr>\n");
 			sb.append("</thead>\n");
 			sb.append("<tbody>\n");
 			
 			for(ScoreRowBean row : rows){
 				sb.append(row.getHtmlRow());				
 			}
 			sb.deleteCharAt(sb.length()-1);
 			sb.append("</tbody>\n");
 			sb.append("</table>");
 			
 		}
 		else{
 			sb.append("No rows to display");
 		}
 		return sb.toString();		
 	}
 	public String getJSON(){
 		StringBuilder sb = new StringBuilder();
 		
 		if(rows.size() > 0){
 			sb.append("{\"scoreboard\": [\n");
 			for(ScoreRowBean row : rows){
 				sb.append(row.getJSONArray());
 				sb.append(",\n");
 			}
 			sb.deleteCharAt(sb.length()-2);
 			sb.append("]}");
 		}
 		else{
 			sb.append("No rows to display");
 		}
 		
 		return sb.toString();
 	}
 	
 
 }

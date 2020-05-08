 package data;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 
 import objects.Deadline;
 import objects.ModulKu;
 
 public class DeadLineData extends KillConnections{
 	
 	//private static Connection con;
 	private static final String NEWDEADLINE = "Insert into deadlinedata Values(?,?,?)";
	private static final String NEWDEADLINEMESSAGE = "SELECT dozid FROM moduldata WHERE responsibleid=?";
 	
 	public DeadLineData(){
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();  
 		}
 		
 		try {
 			con =  (Connection) DriverManager.getConnection("jdbc:mysql://localhost/MMS", "root", "root");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}	
 	}
 	
      // in Datanbank schreiben
	public LinkedList<Integer> newDeadlineMessage (int responsibleid){
 		
 		PreparedStatement psmt = null;
 		ResultSet data = null;
 
 		try {
 			LinkedList<Integer> list = new LinkedList<Integer>();
 			con.setAutoCommit(false);
 
 			psmt = con.prepareStatement(NEWDEADLINEMESSAGE);
			psmt.setInt(1, responsibleid);
 			
 			data = psmt.executeQuery();
 			
 			while(data.next()){
				
 				boolean add = true;
				Integer insertValue = new Integer(data.getInt("dozid"));
				for(int i = 0; i < list.size(); i++){
					if(list.get(i).equals(insertValue)){
 						add = false;
					}
				}
				if(add){
 					list.add(insertValue);
				}
 			}
 			return list;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			closeConnections(data, psmt);	
 		}
 	}
 
 	public void newDeadLine(Deadline deadline){
 		
 		PreparedStatement psmt = null;
 
 		try {
 			con.setAutoCommit(false);
 	
 			psmt = con.prepareStatement(NEWDEADLINE);
 			psmt.setInt(1, deadline.getid());
 			psmt.setString(2, deadline.getdatum());
 			psmt.setString(3, deadline.getpdf());
 			psmt.executeUpdate();
 			con.commit();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				closeConnections(null, psmt);
 		}
     }
 }

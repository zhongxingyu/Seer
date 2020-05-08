 package at.ac.tuwien.secsyseng.sqlinjection.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class Var3Dao {
 
 	public Var3Dao() {
 	};
 
 	public int Var3InsertSecure(int id, String name) throws SQLException {
 
 		Connection c = ConnectionFactory.getConnection();
 		PreparedStatement pstmt = c
 				.prepareStatement("insert into var3data values (?, ?);");
 
 		pstmt.setInt(1, id);
 		pstmt.setString(2, name);
 
 		return pstmt.executeUpdate();
 
 	}
 
 	public int Var3InsertUnsecure(int id, String name) throws SQLException {
 
 		Connection c = ConnectionFactory.getConnection();
 		Statement st = c.createStatement();
 		return st.executeUpdate("insert into var3data values (" + id + ", '"
 				+ name + "' );");
 
 	}
 
 	public ResultSet getData() throws SQLException {
 
 		Connection c = ConnectionFactory.getConnection();
 		Statement st = c.createStatement();
 		ResultSet rs = st.executeQuery("select * from var3data;");
 		return rs;
 	}
 }

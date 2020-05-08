 package sap.project1;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.sql.DataSource;
 
 public class HallDAO {
 
 	private DataSource dataSource;
 	
 	public HallDAO(DataSource newDataSource) throws SQLException {
         setDataSource(newDataSource);
     }
 	
 	public DataSource getDataSource() {
         return dataSource;
     }
 	public void setDataSource(DataSource newDataSource) throws SQLException {
         this.dataSource = newDataSource;
     }
 	
 	public void addHall(Hall hall) throws SQLException {
         Connection connection = dataSource.getConnection();
 
         try {
             PreparedStatement pstmt = connection
                    .prepareStatement("INSERT INTO HALLS (NUMBER, N_ROWS, N_COLUMNS) VALUES (?, ?, ?)");
             pstmt.setInt(1, hall.getNumber());
             pstmt.setInt(2, hall.getRows());
             pstmt.setInt(3, hall.getColumns());
             pstmt.executeUpdate();
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
     }
 	
 	public List<Hall> viewAllHalls() throws SQLException {
         Connection connection = dataSource.getConnection();
         try {
             PreparedStatement pstmt = connection
                     .prepareStatement("SELECT NUMBER, N_ROWS, N_COLUMNS FROM HALLS");
             ResultSet rs = pstmt.executeQuery();
             ArrayList<Hall> list = new ArrayList<Hall>();
             while (rs.next()) {
                 Hall h = new Hall();
                 h.setNumber(rs.getInt(1));
                 h.setRows(rs.getInt(2));
                 h.setColumns(rs.getInt(3));
                 list.add(h);
             }
             return list;
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
     }
 	
 	
 	public Hall searchByNumber(int num) throws SQLException {
         Connection connection = dataSource.getConnection();
         try {
             PreparedStatement pstmt = connection
                     .prepareStatement("SELECT NUMBER, N_ROWS, N_COLUMNS FROM HALLS WHERE NUMBER=?");
             pstmt.setInt(1, num);
             ResultSet rs = pstmt.executeQuery();
             Hall h = new Hall();
             while (rs.next()) {     
                 h.setNumber(rs.getInt(1));
                 h.setRows(rs.getInt(2));
                 h.setColumns(rs.getInt(3));
             }
             return h;
         } finally {
             if (connection != null) {
                 connection.close();
             }
         }
     }
 	
 }

 package ca.ubc.cpsc304.r3.db;
 
 // general sql imports
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import ca.ubc.cpsc304.r3.dto.HoldRequestDetailedDto;
 import ca.ubc.cpsc304.r3.dto.HoldRequestDto;
 
 public class HoldRequestDao {
 	
 	private ConnectionService connService;
 	
 	public HoldRequestDao(ConnectionService connService){
 		this.connService = connService;
 	}
 	
 	public List<HoldRequestDto> getByID(int id) throws SQLException{
 		List<HoldRequestDto> queryResult = new ArrayList<HoldRequestDto>();
 		Connection conn = null; 
 		try {
 			conn = connService.getConnection();	
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT * "+
 					"FROM holdrequest "  +
 					"WHERE bid=?"); 
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next()){
 				// for each row, put the data in the dto
 				// and add it to list of results
 				HoldRequestDto dto = new HoldRequestDto();
 				dto.setBid(rs.getInt("bid"));
 				dto.setHid_(rs.getInt("hid"));
 				dto.setCallNumber(rs.getInt("callNumber"));
 				dto.setIssuedDate(rs.getDate("issuedDate"));
 				queryResult.add(dto);
 			}
 		} catch (SQLException e) {
 			// two options here. either don't catch this exception and 
 			// make the caller handle it, or wrap it in a more 
 			// descriptive exception depending on the situation.
 			// I'll just throw it
 			throw e;
 			
 		} finally {
 			// don't forget to close the connection
 			// when you're done with it
 			if(conn != null){
 				conn.close();
 			}
 		}
 		return queryResult;
 	}
 	
 	public List<HoldRequestDetailedDto> getDetailedByID(int id) throws SQLException{
 		List<HoldRequestDetailedDto> queryResult = new ArrayList<HoldRequestDetailedDto>();
 		Connection conn = null; 
 		try {
 			conn = connService.getConnection();	
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT H.bid, H.hid, H.callNumber, H.issuedDate, B.mainAuthor, B.title "+
 					"FROM holdrequest H, book B "  +
 					"WHERE H.bid=? AND H.callNumber=B.callNumber"); 
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next()){
 				// for each row, put the data in the dto
 				// and add it to list of results
 				HoldRequestDetailedDto dto = new HoldRequestDetailedDto();
 				dto.setBid(rs.getInt("bid"));
 				dto.setHid_(rs.getInt("hid"));
 				dto.setCallNumber(rs.getInt("callNumber"));
 				dto.setIssuedDate(rs.getDate("issuedDate"));
 				dto.setMainAuthor(rs.getString("mainAuthor"));
 				dto.setTitle(rs.getString("title"));
 				queryResult.add(dto);
 			}
 		} catch (SQLException e) {
 			// two options here. either don't catch this exception and 
 			// make the caller handle it, or wrap it in a more 
 			// descriptive exception depending on the situation.
 			// I'll just throw it
 			throw e;
 			
 		} finally {
 			// don't forget to close the connection
 			// when you're done with it
 			if(conn != null){
 				conn.close();
 			}
 		}
 		return queryResult;
 	}
 	
 	public int placeByCallNumberAndID(int callNo, int borrowerID) throws SQLException{
 		Connection conn = null; 
 		try {
 			// first get the current date to be used when placing the hold request
 			java.util.Date now = new java.util.Date();
 			java.sql.Date sqlNow = new java.sql.Date(now.getTime());
 			
 			conn = connService.getConnection();	
 			PreparedStatement ps = conn.prepareStatement(
 					"insert into holdrequest(bid, callNumber, issuedDate) " +
 					"values(?, ?, ?)");
 			
 			ps.setInt(1, borrowerID);
 			ps.setInt(2, callNo);
 			ps.setDate(3, sqlNow);
 
 			ps.executeUpdate();
 			
 			
 			PreparedStatement ps2 = conn.prepareStatement(
 					"SELECT * "+
 					"FROM bookcopy "  +
 					"WHERE status='in' AND callNumber=?"); // matches any main author that contains <keyword>
 			ps2.setInt(1, callNo);
 			ResultSet rs2 = ps2.executeQuery();
 			rs2.last();
 			int numIn = rs2.getRow();		
 
 			if(numIn>0){
 				int copyToReserve = rs2.getInt("copyNo");
 				
 				
 				conn = connService.getConnection();	
 				PreparedStatement ps3 = conn.prepareStatement(
 						"UPDATE bookcopy " + 
						"SET status='on hold'" +
 						" WHERE callNumber=? AND copyNo=?");
 
 				ps3.setInt(1, callNo);
 				ps3.setInt(2, copyToReserve);
 				ps3.executeUpdate();
 				
 				return 1;
 			}
 			else{
 				return 0;
 			}
 		
 		} catch (SQLException e) {
 			// two options here. either don't catch this exception and 
 			// make the caller handle it, or wrap it in a more 
 			// descriptive exception depending on the situation.
 			// I'll just throw it
 			throw e;
 			
 		} finally {
 			// don't forget to close the connection
 			// when you're done with it
 			if(conn != null){
 				conn.close();
 			}
 		}
 	}
 }
 

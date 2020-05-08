 package com.rau.evoting.data;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.rau.evoting.ElGamal.ChaumPedersenProof;
 import com.rau.evoting.models.Vote;
 
 public class ElectionVoteDP {
 
 	public static final String TABLE_NAME = "ElectionVotes";
 	
 	public static final String ID = "id";
 	public static final String ELECTION_ID = "electId";
 	public static final String USER_ID = "userId";
 	public static final String AUDIT_BALLOT = "auditBallot";
 	public static final String AUDIT_SEQUENCE = "auditSequence";
 	public static final String ENCODED1 = "encoded1";
 	public static final String ENCODED2 = "encoded2";
 	public static final String ANSWER_ID = "answerId";
 	public static final String CHAUM_PEDERSEN1 = "chaumPedersen1";
 	public static final String CHAUM_PEDERSEN2 = "chaumPedersen2";
 	
 	
 	public static int setElectionVote(int elId, int voterId, int auditBallot, String auditSequence, 
 			String encoded1, String encoded2, int answerId, String chaumPedersen1, String chaumPedersen2) {
 		int id = 0;
 		Connection con = null;
 		try {
 			con =  SqlDataProvider.getInstance().getConnection();
			String sql = "insert into " + TABLE_NAME + "( "
 					+ ELECTION_ID + "," + USER_ID +  "," + AUDIT_BALLOT +  "," 
 					+ AUDIT_SEQUENCE + "," + ENCODED1 + "," + ENCODED2 + "," + ANSWER_ID 
 					+ "," + CHAUM_PEDERSEN1 +  "," + CHAUM_PEDERSEN2 + ") values(?,?,?,?,?,?,?,?,?)" 
 					+ " select SCOPE_IDENTITY() as " + ID;
			PreparedStatement statement = con.prepareStatement(sql);
 			statement.setInt(1, elId);
 			statement.setInt(2, voterId);
 			statement.setInt(3, auditBallot);
 			statement.setString(4, auditSequence);
 			statement.setString(5, encoded1);
 			statement.setString(6, encoded2);
 			statement.setInt(7, answerId);
 			statement.setString(8, chaumPedersen1);
 			statement.setString(9, chaumPedersen2);
			ResultSet rs = statement.executeQuery();
 			if(rs.next()) {
 				id = rs.getInt(ID);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (con != null) {
 					con.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return id;
 	}
 	
 	public static Vote getVote(int id) {
 		Vote vote = null;
 		Connection con = null;
 		try {
 			con =  SqlDataProvider.getInstance().getConnection();
 			String sql = "select * from " + TABLE_NAME + " where id = ?";
 			PreparedStatement statement = con.prepareStatement(sql);
 			statement.setInt(1, id);
 			
 			ResultSet rs = statement.executeQuery();
 			if(rs.next()) {
 				vote = new Vote(rs.getInt(ID), rs.getInt(ELECTION_ID), rs.getInt(USER_ID),
 						rs.getInt(AUDIT_BALLOT), rs.getString(ENCODED1), rs.getString(ENCODED2),
 						rs.getString(AUDIT_SEQUENCE), rs.getInt(ANSWER_ID),
 						rs.getString(CHAUM_PEDERSEN1), rs.getString(CHAUM_PEDERSEN2));
 						
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (con != null) {
 					con.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return vote;
 	}
 	
 }

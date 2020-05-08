 package com.parq.server.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.parq.server.dao.model.object.UserPrePaidAccountBalance;
 
 public class UserPrePaidAccountBalanceDao extends AbstractParqDaoParent {
 
 	private static final String sqlGetUserAccountBalance = 
		"SELECT account_balance, user_id FROM prepaidaccountbalance " +
 		"	WHERE user_id = ? AND is_deleted IS NOT TRUE";
 	
 	private static final String sqlUpdateUserAccountBalance = 
		"UPDATE prepaidaccountbalance SET account_balance = ? WHERE user_id = ?";
 	
 	private static final String sqlDeleteUserPrePaidAccount = 
		"UPDATE prepaidaccountbalance SET is_deleted = TRUE WHERE user_id = ?";
 	
 	public UserPrePaidAccountBalance getUserPrePaidAccountBalance(long userId) {
 		if (userId <= 0) {
 			throw new IllegalStateException("userId is invalid: " + userId);
 		}
 		
 		UserPrePaidAccountBalance accountBalance = null;
 		// query the DB for the account balance object
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlGetUserAccountBalance);
 			pstmt.setLong(1, userId);
 			// System.out.println(pstmt);
 			ResultSet rs = pstmt.executeQuery();
 			accountBalance = createAccountBalanceObject(rs);
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		
 		return accountBalance;
 	}
 	
 	public boolean updateUserPrePaidAccountBalance(long userId, int newBalance) {
 		if (userId <= 0) {
 			throw new IllegalStateException("userId is invalid: " + userId);
 		}
 		else if (newBalance <= 0) {
 			throw new IllegalStateException("New User balance is invalid for User: " 
 					+ userId + ", new balance: " + newBalance);
 		}
 		
 		boolean updateSuccessful = false;
 		// update the user account balance
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlUpdateUserAccountBalance);
 			pstmt.setInt(1, newBalance);
 			pstmt.setLong(2, userId);
 			// System.out.println(pstmt);
 			updateSuccessful = pstmt.executeUpdate() == 1;
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		
 		return updateSuccessful;
 	}
 	
 	public boolean deleteUserPrePaidAccountBalance(long userId) {
 		if (userId <= 0) {
 			throw new IllegalStateException("userId is invalid: " + userId);
 		}
 		boolean deleteSuccessful = false;
 		// delete the user account balance
 		PreparedStatement pstmt = null;
 		Connection con = null;
 		try {
 			con = getConnection();
 			pstmt = con.prepareStatement(sqlDeleteUserPrePaidAccount);
 			pstmt.setLong(1, userId);
 			// System.out.println(pstmt);
 			deleteSuccessful = pstmt.executeUpdate() == 1;
 		} catch (SQLException sqle) {
 			System.out.println("SQL statement is invalid: " + pstmt);
 			sqle.printStackTrace();
 			throw new RuntimeException(sqle);
 		} finally {
 			closeConnection(con);
 		}
 		
 		return deleteSuccessful;
 	}
 	
 	private UserPrePaidAccountBalance createAccountBalanceObject(ResultSet rs) 
 		throws SQLException {
 		if (rs == null || !rs.isBeforeFirst()) {
 			return null;
 		}
 		rs.first();
 
 		long userId = rs.getLong("user_id");
 		int accountBal = rs.getInt("account_balance");
 		UserPrePaidAccountBalance accBal = new UserPrePaidAccountBalance();
 		accBal.setUserId(userId);
 		accBal.setAccountBalance(accountBal);
 		return accBal;
 	}
 	
 }

 package de.hotmail.gurkilein.bankcraft.database.mysql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 import de.hotmail.gurkilein.bankcraft.database.AccountDatabaseInterface;
 
 public class ExperienceMysqlInterface implements AccountDatabaseInterface <Integer>{
 
 	private Connection conn;
 	@SuppressWarnings("unused")
 	private Bankcraft bankcraft;
 
 	public ExperienceMysqlInterface(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		this.conn = ((DatabaseManagerMysql)bankcraft.getDatabaseManagerInterface()).getConnection();
 	}
 
 	@Override
 	public boolean hasAccount(String player) {
 		      try {
 		 
 		        String sql = "SELECT `player_name` FROM `bc_accounts` WHERE `player_name` = ?";;
 		        PreparedStatement preparedStatement = conn.prepareStatement(sql);
 		        preparedStatement.setString(1, player.toLowerCase());
 		        
 		        ResultSet result = preparedStatement.executeQuery();
 		 
 		        while (result.next()) {
 		        	return true;
 		        }
 		      } catch (SQLException e) {
 		        e.printStackTrace();
 		      }
 		      return false;
 	}
 
 	@Override
 	public boolean createAccount(String player) {
 		try {
 			 
 	        String sql = "INSERT INTO `bc_accounts`(`player_name`, `balance`, `balance_xp`) " +
 	                     "VALUES(?, ?, ?)";
 	        PreparedStatement preparedStatement = conn.prepareStatement(sql);
 	        
 	        preparedStatement.setString(1, player.toLowerCase());
 	        preparedStatement.setString(2, "0");
 	        preparedStatement.setString(3, "0");
 	        
 	        preparedStatement.executeUpdate();
 	        return true;
 	      } catch (SQLException e) {
 	        e.printStackTrace();
 	      }
 		return false;
 	}
 
 	@Override
 	public Integer getBalance(String player) {
 		if (!hasAccount(player.toLowerCase())) {
 			createAccount(player.toLowerCase());
 		}
 		
 	      try {
 	 
 	        String sql = "SELECT `balance_xp` FROM `bc_accounts` WHERE `player_name` = ?";
 	        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
 	        preparedUpdateStatement.setString(1, player.toLowerCase());
 	        ResultSet result = preparedUpdateStatement.executeQuery();
 	 
 	        
 	        while (result.next()) {
	        	return Integer.parseInt(result.getString("balance_xp"));
 	        }
 	      } catch (SQLException e) {
 	        e.printStackTrace();
 	      }
 		return null;
 	}
 
 	@Override
 	public boolean setBalance(String player, Integer amount) {
 		if (!hasAccount(player.toLowerCase())) {
 			createAccount(player.toLowerCase());
 		}
 		
         try {
 			String updateSql = "UPDATE `bc_accounts` " +
 			        "SET `balance_xp` = ?" +
 			        "WHERE `player_name` = ?";
 			PreparedStatement preparedUpdateStatement = conn.prepareStatement(updateSql);
 			preparedUpdateStatement.setString(1, amount+"");
 			preparedUpdateStatement.setString(2, player.toLowerCase());
 			preparedUpdateStatement.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
         return false;
 	}
 
 	@Override
 	public boolean addToAccount(String player, Integer amount) {
 		if (!hasAccount(player.toLowerCase())) {
 			createAccount(player.toLowerCase());
 		}
 		
 		if (amount < 0) {
 			return removeFromAccount(player.toLowerCase(), -amount);
 		}
 		
 		Integer currentBalance = getBalance(player.toLowerCase());
 		if (currentBalance <= Integer.MAX_VALUE-amount) {
 			setBalance(player.toLowerCase(), currentBalance+amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean removeFromAccount(String player, Integer amount) {
 		if (!hasAccount(player.toLowerCase())) {
 			createAccount(player.toLowerCase());
 		}
 		
 		if (amount < 0) {
 			return addToAccount(player.toLowerCase(), -amount);
 		}
 		
 		Integer currentBalance = getBalance(player.toLowerCase());
 		if (currentBalance >= Integer.MIN_VALUE+amount) {
 			setBalance(player.toLowerCase(), currentBalance-amount);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String[] getAccounts() {
 		
 	      Statement query;
 	      try {
 	        query = conn.createStatement();
 	 
 	        String sql = "SELECT `player_name` FROM `bc_accounts`";
 	        ResultSet result = query.executeQuery(sql);
 	 
 	        List <String> loadingList= new ArrayList <String>();
 	        while (result.next()) {
 	        	loadingList.add(result.getString("player_name"));
 	        }
 	        return loadingList.toArray(new String [0]);
 	        
 	      } catch (SQLException e) {
 	        e.printStackTrace();
 	      }
 		return null;
 	}
 
 }

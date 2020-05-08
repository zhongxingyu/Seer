package adg.red.controllers;

 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class Account {
 	
 	private Connection _conn;
 	
 	public Account(Connection conn){
 		_conn = conn;
 	}
 
 	public PreparedStatement getValidateAccountSql(int bid) throws SQLException
 	{
 		PreparedStatement ps = _conn.prepareStatement("SELECT FROM branch WHERE branch_id = ?");
 		ps.setInt(1, bid);
 		return ps;
 	}
 	
 	public void executeSql(PreparedStatement ps)
 	{
 		try
 		{
 			_conn.commit();
 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 
 			try 
 			{
 				_conn.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
 	
 	public void addAccount(int bid) throws SQLException
 	{
 		executeSql(getValidateAccountSql(bid));
 	}
 	
 	// get sql statement
 	public String getShowAccountSql(int bid)
 	{
 		String sql = "SELECT * FROM branch WHERE branch_id=";
 		sql += String.valueOf(bid);
 		return sql;
 	}
 	
 	public ResultSet showAccount(int bid)
 	{
 		return executeSql(getShowAccountSql(bid));		
 	}
 	 
 	public ResultSet executeSql(String sql)
 	{
 		String     bid;
 		String     bname;
 		String     baddr;
 		String     bcity;
 		String     bphone;
 		Statement  stmt;
 		ResultSet  rs;
 		try
 		{
 			stmt = _conn.createStatement();
 			rs = stmt.executeQuery(sql);
 
 			while(rs.next())
 			{
 				// for display purposes get everything from Oracle 
 				// as a string
 
 				// simplified output formatting; truncation may occur
 
 				bid = rs.getString("branch_id");
 				System.out.printf("%-10.10s", bid);
 
 				bname = rs.getString("branch_name");
 				System.out.printf("%-20.20s", bname);
 
 				baddr = rs.getString("branch_addr");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-20.20s", " ");
 				}
 				else
 				{
 					System.out.printf("%-20.20s", baddr);
 				}
 
 				bcity = rs.getString("branch_city");
 				System.out.printf("%-15.15s", bcity);
 
 				bphone = rs.getString("branch_phone");
 				if (rs.wasNull())
 				{
 					System.out.printf("%-15.15s\n", " ");
 				}
 				else
 				{
 					System.out.printf("%-15.15s\n", bphone);
 				}      
 			}
 			// close the statement; 
 			// the ResultSet will also be closed
 			stmt.close();
 			return rs;
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 		}
 		return null;
 	}
 
 
 }

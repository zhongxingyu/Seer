 package delicious.pos.business.logic.dao.gen;
 
 import delicious.pos.App;
 import delicious.pos.business.logic.dao.BaseDAO;
 import delicious.pos.business.logic.dao.JDBCUtilities;
 import delicious.pos.business.logic.view.gen.EmployeeView;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class EmployeeDAO extends BaseDAO
 {			
 	public EmployeeDAO () {
 
 	}
 	
 	public Object[] getColumnNames()
 	{
 		List<String> columnNames = new ArrayList<String>();
 		columnNames.add("userName");
 		columnNames.add("salary");
 		columnNames.add("phone");
 		columnNames.add("position");
 		return columnNames.toArray();
 	}
 	
 	public Object[][] getAllAsArray()
 	{
 		ArrayList<EmployeeView> employeeViews = findAll();
 		Object[][] employees = new Object[employeeViews.size()][];
 		
 		for(int i = 0;i < employeeViews.size();i++) 
 		{
 			List<Object> employee = new ArrayList<Object>();
 			employee.add(employeeViews.get(i).getUserName());
 			employee.add(employeeViews.get(i).getSalary());
 			employee.add(employeeViews.get(i).getPhone());
 			employee.add(employeeViews.get(i).getPosition());
 			employees[i] = employee.toArray();
 		}
 		
 		return employees;
 	}
 	
 	public ArrayList<EmployeeView> findAll()
 	{
 		ArrayList<EmployeeView> result = new ArrayList<EmployeeView>();
 		Statement statement = null;
 	    
 	    String query = "SELECT userName, salary, phone, position ";
 	    query += "FROM Employees";
 	    
 	    try 
 	    {
 	    	statement = App.DBConnection.createStatement();
 	    	ResultSet resultSet = statement.executeQuery(query);
 
 	      while (resultSet.next()) 
 	      {
 	    	  EmployeeView employee  = new EmployeeView(resultSet.getString("userName"), resultSet.getFloat("salary"), resultSet.getString("phone"), resultSet.getString("position"));
 	          result.add(employee);
 	      }
 	    } 
 	    catch (SQLException e) 
 	    {
 			JDBCUtilities.printSQLException(e);
 	    } 
 	    finally 
 	    {
 	    	if (statement != null) 
 	    	{ 
 	    		try {
 					statement.close();
 				} catch (SQLException e) {
 					JDBCUtilities.printSQLException(e);
 				}
 	    	}
 	    }
 	    return result;
 	}
 	
 	public void persist(EmployeeView employee)
 	{
 		if(rowCount(employee.getUserName()) > 0) 
 	    {
 	    	this.update(employee);
 	    } else 
 	    {
 	    	this.insert(employee);
 	    }
 	}
 	
 	public void insert(EmployeeView employee)
 	{
 		Statement stmt = null;
 	    try 
 	    {
 	      stmt = App.DBConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 	      ResultSet uprs = stmt.executeQuery("SELECT * FROM Employees");
 
 	      uprs.moveToInsertRow();
 
 	      uprs.updateString("userName", employee.getUserName());
 	      uprs.updateFloat("salary", employee.getSalary());
 	      uprs.updateString("phone", employee.getPhone());
 	      uprs.updateString("position", employee.getPosition());
 
 	      uprs.insertRow();
 	      uprs.beforeFirst();
 
 	    } catch (SQLException e) 
 	    {
 	      JDBCUtilities.printSQLException(e);
 	    } 
 	    finally 
 	    {
 	      if (stmt != null) 
 	      { 
 	    		try {
 					stmt.close();
 				} catch (SQLException e) {
 					JDBCUtilities.printSQLException(e);
 				}
 	      }
 	    }
 	}
 	
 	public void update(EmployeeView employee) {
 		PreparedStatement stmt = null;
 		String query = "Update Employees set " +
 						"userName = ?" +
 						", salary = ?" +
 						", phone = ?" +
 						", position = ?" +
						" where userName = ?";
 		
 		try {
 			stmt = App.DBConnection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 			stmt.setString(1, employee.getUserName());
 			stmt.setFloat(2, employee.getSalary());
 			stmt.setString(3, employee.getPhone());
 			stmt.setString(4, employee.getPosition());
 			stmt.setString(5, employee.getUserName());
 			stmt.executeUpdate();
 			stmt.close();
 
 		} catch (SQLException e) 
 		{ 
 			JDBCUtilities.printSQLException(e); 
 		} 
 		
 		finally {
 			if(stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException e) { JDBCUtilities.printSQLException(e); }
 			}
 		}
 	}
 	
 	public void remove(EmployeeView employee)
 	{
 	    PreparedStatement stmt = null;
 	    
 	    String query = "SELECT userName FROM Employees ";
 	    query += "WHERE userName = ?";
 	    
 	    try 
 	    {
 	      stmt = App.DBConnection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 	      stmt.setString(1, employee.getUserName());
 	      
 	      ResultSet uprs = stmt.executeQuery();
 
 	      while (uprs.next()) 
 	      {
 	    	  uprs.deleteRow();
 	      }
 
 	    } catch (SQLException e) 
 	    {
 	      JDBCUtilities.printSQLException(e);
 	    } 
 	    finally 
 	    {
 	      if (stmt != null) 
 	      { 
 	    		try {
 					stmt.close();
 				} catch (SQLException e) {
 					JDBCUtilities.printSQLException(e);
 				}
 	      }
 	    }
 	}
 	
 	private int rowCount(String primaryKey)
 	{
 		PreparedStatement stmt = null;
 	    ResultSet rs = null;
 	    String query = "SELECT COUNT(*) FROM Employees WHERE username = ?";
 	    int rowCount = -1;
 	    
 	    try 
 	    {
 	    	stmt = App.DBConnection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		    stmt.setString(1, primaryKey);
 		    
 		    rs = stmt.executeQuery();
 
 	    	rs.next();
 	    	rowCount = rs.getInt(1);
 	    } catch (SQLException e) {
 	    	JDBCUtilities.printSQLException(e);
 		} finally 
 	    {
 			if (stmt != null) 
 		    { 
 				try {
 					stmt.close();
 				} catch (SQLException e) {
 					JDBCUtilities.printSQLException(e);
 				}
 		    }
 	    }
 	    
 	    return rowCount;		
 	}
 }

 package database;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 import model.Account;
 import model.Company;
 import model.Department;
 import model.Employee;
 import model.Manager;
 
 public class DepartmentDBAO {
 	Connection con;
 	private boolean conFree = true;
 
 	// Database configuration
 	public static String url = "jdbc:mysql://filehaven.ch30tsalfl52.ap-southeast-1.rds.amazonaws.com:3306/filehaven";
 	public static String dbdriver = "com.mysql.jdbc.Driver";
 	public static String username = "filehaven";
 	public static String password = "filehaven";
 
 	public DepartmentDBAO() throws Exception {
 		try {
 			DB db = new DB();
 			con = db.getConnection();
 		} catch (Exception ex) {
 			System.out.println("Exception in DepartmentDBAO: " + ex);
 			throw new Exception("Couldn't open connection to database: "
 					+ ex.getMessage());
 		}
 	}
 
 	public boolean checkAvailability(String departmentName, int companyID) {
 		boolean status = false;
 		try {
 
 			String selectStatement = "SELECT * FROM department WHERE Name=? AND companyID=?";
 			System.out.println(selectStatement);
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, departmentName);
 			prepStmt.setInt(2, companyID);
 
 			ResultSet rs = prepStmt.executeQuery();
 
 			while (rs.next()) {
 				Department department = new Department();
 				department.setId(rs.getInt("ID"));
 				status =true;	
 				
 			}
 		} catch (SQLException ex) {
 			releaseConnection();
 			ex.printStackTrace();
 		}
 		
 		return status;
 
 	}
 
 	public boolean insertDepartment(Department department, String user) {
 
 		boolean status = false;
 
 		try {
 
 			// TODO change the sql statement,change name to username
 			String statement = "(SELECT CompanyID FROM account WHERE UserName='"
 					+ user + "'))";
 			String selectStatement = "insert into department (Name,Description,DepartmentLogo,CompanyID) values(?,?,?,"
 					+ statement;
 			System.out.println(selectStatement);
 
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, department.getDepartmentName());
 			prepStmt.setString(2, department.getDepartmentDescription());
 			prepStmt.setString(3, department.getDepartmentLogo());
 
 			if (prepStmt.executeUpdate() == 1) {
 				status = true;
 				prepStmt.close();
 				releaseConnection();
 				System.out.println("Successful in department");
 			}
 
 		} catch (SQLException ex) {
 			releaseConnection();
 			ex.printStackTrace();
 		}
 		return status;
 	}
 
 	public boolean updateDepartment(Department department, String user,
 			int departmentId) {
 
 		boolean status = false;
 
 		try {
 			String statement = "(SELECT CompanyID FROM account WHERE Name='"
 					+ user + "')";
 			String selectStatement = "UPDATE department SET Name=? , Description=?, DepartmentLogo=?,CompanyID="
 					+ statement + " WHERE ID=?";
 			System.out.println(selectStatement);
 
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, department.getDepartmentName());
 			prepStmt.setString(2, department.getDepartmentDescription());
 			prepStmt.setString(3, department.getDepartmentLogo());
 			prepStmt.setInt(4, departmentId);
 
 			if (prepStmt.executeUpdate() == 1) {
 				status = true;
 				prepStmt.close();
 				releaseConnection();
 				System.out.println("Successful in updating department");
 			}
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			releaseConnection();
 
 		}
 		return status;
 	}
 
 	public ArrayList<Department> getCompanyDepartment(String user) {
 		ArrayList<Department> d1 = new ArrayList<Department>();
 		// TODO change the sql statement,change name to username
 		try {
			String selectStatement = "SELECT * FROM department WHERE CompanyID=(SELECT CompanyID FROM account WHERE Name=?)";
 			System.out.println(selectStatement);
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, user);
 
 			ResultSet rs = prepStmt.executeQuery();
 
 			while (rs.next()) {
 				Department department = new Department();
 				department
 						.setDepartmentName(rs.getString(rs.findColumn("Name")));
 				department.setDepartmentLogo(rs.getString(rs
 						.findColumn("DepartmentLogo")));
 				department.setDepartmentDescription(rs.getString(rs
 						.findColumn("Description")));
 
 				d1.add(department);
 
 			}
 			prepStmt.close();
 			releaseConnection();
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			releaseConnection();
 
 		}
 
 		return d1;
 	}
 
 	// TODO change the sql statement,change name to username
 	public ArrayList<Department> getDepartmentDetails(String user,
 			int departmentId) {
 		ArrayList<Department> d1 = new ArrayList<Department>();
 
 		try {
 			String selectStatement = "SELECT * FROM department WHERE CompanyID=(SELECT CompanyID FROM account WHERE Name=?) AND ID=?";
 			System.out.println(selectStatement);
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, user);
 			prepStmt.setInt(2, departmentId);
 
 			ResultSet rs = prepStmt.executeQuery();
 
 			while (rs.next()) {
 				Department department = new Department();
 				department
 						.setDepartmentName(rs.getString(rs.findColumn("Name")));
 				department.setDepartmentLogo(rs.getString(rs
 						.findColumn("DepartmentLogo")));
 				department.setDepartmentDescription(rs.getString(rs
 						.findColumn("Description")));
 
 				d1.add(department);
 
 			}
 			prepStmt.close();
 			releaseConnection();
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			releaseConnection();
 
 		}
 
 		return d1;
 	}
 
 	// TODO change the sql statement,change name to username
 	public int getDepartmentID(String departmentName) {
 		int id = 0;
 		try {
 			String sql = "SELECT ID FROM department d WHERE d.name='"
 					+ departmentName + "'";
 			getConnection();
 			PreparedStatement prest = con.prepareStatement(sql);
 			ResultSet rs = prest.executeQuery();
 
 			while (rs.next()) {
 				id = rs.getInt(rs.findColumn("ID"));
 			}
 
 			prest.close();
 			releaseConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			releaseConnection();
 		}
 
 		return id;
 
 	}
 
 	public int getDepID(String departmentName, int companyID) {
 		int id = 0;
 		try {
 			String sql = "SELECT ID FROM department d WHERE d.name=? AND d.CompanyID=?";
 			getConnection();
 			PreparedStatement prest = con.prepareStatement(sql);
 			prest.setString(1, departmentName);
 			prest.setInt(2, companyID);
 			ResultSet rs = prest.executeQuery();
 
 			while (rs.next()) {
 				id = rs.getInt(rs.findColumn("ID"));
 			}
 
 			prest.close();
 			releaseConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			releaseConnection();
 		}
 
 		return id;
 
 	}
 
 	public int getDepartmentIDOfCompany(String departmentName, String user) {
 
 		int id = 0;
 		try {
 			String sql = "SELECT ID FROM department d WHERE d.name='"
 					+ departmentName
 					+ "' AND CompanyID=(SELECT CompanyID From account a WHERE a.UserName='"
 					+ user + "')";
 			System.out.println(sql);
 			getConnection();
 			PreparedStatement prest = con.prepareStatement(sql);
 			ResultSet rs = prest.executeQuery();
 
 			while (rs.next()) {
 				id = rs.getInt(rs.findColumn("ID"));
 			}
 
 			prest.close();
 			releaseConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			releaseConnection();
 		}
 
 		return id;
 	}
 	
 	public ArrayList<String> getEmployeesUserName(Employee employ,int companyID){
 
 		ArrayList<String> userNames= new ArrayList<String>();
 		try {
 			
 			for (int i = 0; i < employ.getEmployees().size(); i++) {
 			String sql = "SELECT UserName FROM account WHERE Name=? AND companyID=?";
 			System.out.println(sql);
 			getConnection();
 			PreparedStatement prest = con.prepareStatement(sql);
 			prest.setString(1, employ.getEmployees().get(i));
 			prest.setInt(2, companyID);
 			ResultSet rs = prest.executeQuery();
 
 			while (rs.next()) {
 				userNames.add(rs.getString("UserName"));
 			}
 			
 			prest.close();
 			releaseConnection();
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			releaseConnection();
 		}
 
 		return userNames;
 	}
 	
 
 	public boolean updateEmployeeDepartment(Employee employ,
 			String departmentName, Account user) {
 
 		boolean status = false;
 
 		try {
 			int departmentId = getDepartmentIDOfCompany(departmentName, user.getUserName());
 			ArrayList<String> userNames=getEmployeesUserName(employ,user.getCompanyID());
 
 			for (int i = 0; i < employ.getEmployees().size(); i++) {
 				String sql = "UPDATE employee e SET e.DepartmentID ="
 						+ departmentId + " WHERE UserName = ?";
 				System.out.println(sql);
 				getConnection();
 				PreparedStatement prest = con.prepareStatement(sql);
 				System.out.println(userNames.get(i));
 				prest.setString(1, userNames.get(i));
 
 				if (prest.executeUpdate() == 1) {
 					status = true;
 					prest.close();
 					releaseConnection();
 					System.out.println("Successful in employee");
 				}
 
 			}
 		}
 
 		catch (SQLException ex) {
 			releaseConnection();
 			ex.printStackTrace();
 		}
 
 		return status;
 	}
 
 	// TODO change the sql statement,change name to username
 	public boolean deleteDepartment(String departmentName, int companyID) {
 		boolean status = false;
 
 		try {
 			String selectStatement = "DELETE FROM department WHERE Name=? AND CompanyID=?";
 			System.out.println(selectStatement);
 
 			getConnection();
 
 			PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 			prepStmt.setString(1, departmentName);
 			prepStmt.setInt(2, companyID);
 
 			if (prepStmt.executeUpdate() == 1) {
 				status = true;
 				prepStmt.close();
 				releaseConnection();
 				System.out.println("Successful in deleting department");
 			}
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 			releaseConnection();
 
 		}
 		return status;
 	}
 
 	// THis method needs some reworking--Refer to your old stuff
 	// public ArrayList<Employee> getSelectedEmployees(String username){
 	// ArrayList<Employee> m1 = new ArrayList<Employee>();
 	//
 	// try {
 	// String selectStatement =
 	// "SELECT * FROM employee e INNER JOIN account a ON e.UserName=a.UserName WHERE CompanyID=(SELECT CompanyID FROM account WHERE UserName=?)";
 	// System.out.println(selectStatement);
 	// getConnection();
 	//
 	// PreparedStatement prepStmt = con.prepareStatement(selectStatement);
 	// prepStmt.setString(1, username);
 	//
 	// ResultSet rs = prepStmt.executeQuery();
 	//
 	//
 	// while (rs.next()) {
 	// Employee employee = new Employee();
 	// employee.setName(rs.getString(rs.findColumn("Name")));
 	// employee.setGender(rs.getString("Gender").charAt(0));
 	// employee.setDOB(rs.getDate("DateOfBirth"));
 	// employee.setPhoneNumber(rs.getString("PhoneNumber"));
 	// employee.setEmail(rs.getString("Email"));
 	// employee.setAddress(rs.getString("NRIC"));
 	// employee.setDepartmentID(rs.getInt("DepartmentID"));
 	//
 	// m1.add(employee);
 	//
 	//
 	// }
 	// prepStmt.close();
 	// releaseConnection();
 	//
 	//
 	// } catch (SQLException ex) {
 	// ex.printStackTrace();
 	// releaseConnection();
 	//
 	//
 	// }
 	//
 	// return m1;
 	// }
 
 	// public boolean updateEmployeesDepartment(Employee employ,
 	// String departmentName, String user,Account currentUser) {
 	//
 	// boolean status = false;
 	//
 	// int departmentId = getDepartmentIDOfCompany(departmentName, user);
 	// ArrayList<Employee> a1 =
 	// getSelectedEmployees(currentUser.getCompanyID());
 	// try {
 	//
 	// for (int i = 0; i < employ.getEmployees().size(); i++) {
 	//
 	//
 	// if (departmentId == a1.get(i).getDepartmentID() &&
 	// !employ.getEmployees().get(i).equals(a1.get(i).getName())) {
 	// // set it to null;
 	//
 	// String sql =
 	// "UPDATE employee e INNER JOIN account a ON e.AccountID=a.ID SET e.DepartmentID = NULL"
 	// + " WHERE a.Name = ?";
 	// System.out.println(sql);
 	// getConnection();
 	// PreparedStatement prest = con.prepareStatement(sql);
 	// System.out.println(a1.get(i).getName());
 	// prest.setString(1, a1.get(i).getName());
 	//
 	// if (prest.executeUpdate() == 1) {
 	// status = true;
 	// prest.close();
 	// releaseConnection();
 	// System.out.println("Successful in employee");
 	// }
 	// }
 	//
 	// }
 	// }
 	//
 	// catch (Exception ex) {
 	// // TODO Auto-generated catch block
 	// releaseConnection();
 	// ex.printStackTrace();
 	// }
 	//
 	// return status;
 	// }
 
 	protected synchronized Connection getConnection() {
 		while (conFree == false) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 			}
 		}
 
 		conFree = false;
 		notify();
 
 		return con;
 	}
 
 	public void remove() {
 		try {
 			con.close();
 		} catch (SQLException ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 
 	protected synchronized void releaseConnection() {
 		while (conFree == true) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 			}
 		}
 
 		conFree = true;
 		notify();
 	}
 
 //	public static void main(String args[])
 //	{
 //		DepartmentDBAO d1;
 //		try {
 //			d1 = new DepartmentDBAO();
 //			System.out.println(d1.checkAvailability("Accounts", 8));
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		
 //	}
 
 }

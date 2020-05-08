 package controller;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 import model.Employee;
 import model.User;
 import model.userRights.EmployeeRights;
 import model.userRights.UserRights;
 
 
 public class DbController {
 
	protected Connection db = null;
 	private static DbController instance = null;
 
 	protected DbController() {
 		String url = "jdbc:mysql://localhost:3306/mms";
 		String user = "sopratest";
 		String pw = "sopratest";
 
 		String treiber = "org.gjt.mm.mysql.Driver";
 
 		try {
 			Class.forName(treiber).newInstance();
 			db = DriverManager.getConnection(url, user, pw);
 			instance = this;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static DbController getInstance() {
 		if (instance == null) {
 			instance = new DbController();
 			return instance;
 		} else
 			return instance;
 
 	}
 }

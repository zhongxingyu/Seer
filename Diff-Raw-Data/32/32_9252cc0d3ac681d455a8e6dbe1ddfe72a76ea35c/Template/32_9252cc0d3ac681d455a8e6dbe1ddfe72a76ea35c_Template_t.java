package adg.red.controllers;

 import java.sql.SQLException;
 
 
 public class Template {
 
 	/**
 	 * @param args
 	 * @throws SQLException 
 	 */
 	public static void main(String[] args) throws SQLException {
 		
		LoginController2 login = new LoginController2();
 		login.showAccount(20);
 	}
 
 }

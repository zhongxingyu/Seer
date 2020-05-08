 package org.realty;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static java.lang.System.out;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 	public static void main(String[] args) throws SQLException {
 
 	
 
 		UserJdbcDAO addtest1 = new UserJdbcDAO();
 
 	
 
 		List<User> users = new ArrayList<User>();
 
 		users = addtest1.findAll();
 
 		
 		for (User o : users) {
 
 			out.printf("%10d %9s %12s %15s\n", o.getUserId(), o.getName(),
 					o.getPassword(), o.getPhoneNumber());
 		}
		
		//addtest1.delete(24L);
 
 	}
 }

 /**
  * 
  */
 package de.unistuttgart.iste.se.adohive.controller.mysql.test;
 
 import de.unistuttgart.iste.se.adohive.controller.AdoHiveController;
 
 /**
  * @author rashfael
  *
  */
 public class MySqlInit {
 
 	public static void init() {
 		AdoHiveController.setDriver("com.mysql.jdbc.Driver");
		AdoHiveController.setConnectionString("jdbc:mysql://localhost:3306/aidgertest?user=root");
 	}
 }

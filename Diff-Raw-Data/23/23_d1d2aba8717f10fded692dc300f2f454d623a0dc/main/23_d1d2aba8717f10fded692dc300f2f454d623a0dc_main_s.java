 package rec;
 
 import rec.database.MySQLConnection;
 
 public class main {
 
 	public static void main(String[] args) {
 		GUI gui = new GUI();
		MySQLConnection.getKaufverhalten();
 		
 	}
 }

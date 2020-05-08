 package blue.hotel.main;
 
import org.hsqldb.Server;

 import blue.hotel.gui.MainFrame;
 
 public class Main {
 	public static void main(String [] args) {
		//start db
		String[] dbArgs = { "-database.0", "data/blue_hotel.db",
				"-no_system_exit", "true" };
		Server.main(dbArgs);
		
		//start gui
 		MainFrame main = new MainFrame();
 		main.setVisible(true);
		
 	}
 }

 package Testing;
 
 import java.io.File;
 
 import game.FourInARowGame;
 import general.Config;
 import io.FileMonitor;
 
 public class MDE_FileMonitorTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//FileMonitor my_monitor = new FileMonitor(new File("C:\\Users\\Michi\\Desktop\\test.txt"));
 		//FileMonitor my_monitor = new FileMonitor(null);
		FourInARowGame testGame = new FourInARowGame(Config.SETCOUNT, "C:\\Users\\Michi\\Desktop");
 		
 		testGame.startNewSet();
 		//my_monitor.startMonitoring();
 		
 		//my_monitor.setFilePath(new File("C:\\Users\\Michi\\Desktop\\test.txt"));
 	}
 
 }

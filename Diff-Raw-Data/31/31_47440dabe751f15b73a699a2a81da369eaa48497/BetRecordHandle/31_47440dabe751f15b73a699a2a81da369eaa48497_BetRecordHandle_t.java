 package secret.app.baccarat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BetRecordHandle {
 	
 	public static int[][] betMoney = {
		{100, 100, 0},
 		{400, 400, 100},
 		{1100, 1100, 200},
 		{2600, 2600, 300},
 		{6000, 6000, 700},
 		{13600, 13600, 1500},
 		{30800, 30800, 3500}
 	}; 
 	
 	public static List<String> storedPostFlopList = new ArrayList<String>();
 	public static List<String> crazyModeBinaryList = new ArrayList<String>();
 	public static List<String> betRecordHistory = new ArrayList<String>();
 	
 	public static int winCounts;
 	public static int lostCounts;
 	public static int drawCOunts;
 
 }

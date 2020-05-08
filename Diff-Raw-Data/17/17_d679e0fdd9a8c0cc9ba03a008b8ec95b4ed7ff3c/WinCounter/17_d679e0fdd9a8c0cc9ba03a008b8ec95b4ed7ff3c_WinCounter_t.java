 package dungeon.ai.assessment1;
 
 import java.util.ArrayList;
 
 public class WinCounter {
 	
 	/** vector to count how many of the last 50games have been won */
 	private final static int WIN_LOSE_RANGE = 50;
	private static boolean[] won = null;
 	private static int count = 0;	
 
 	/**
 	 * init the array to calc percentage of won games. 
 	 */
 	public static void init() {
 		if( won == null) {
			won = new boolean[WIN_LOSE_RANGE];
			for(int i=0;i<won.length;i++) {
				won[i] = false;
 			}
 		}		
 	}
 	
 	/**
 	 * Store if last game was won or lost. 
 	 * @param winLose false for lost, true for won.
 	 */
 	public static void setWinLos(boolean winLose) {
		won[count] = winLose;
 		count++;
 		count%=WIN_LOSE_RANGE;
 	}
 	
 	/**
 	 * @return Returns the percentage of the won games out of the last WIN_LOSE_RANGE games.
 	 */
 	public static double getPercentageWon() {
 		int cnt = 0;
		for(int i=0;i<won.length;i++) {
			if (won[i]) {
 				cnt++;
 			}
 		}
 		return ((double)cnt)/WIN_LOSE_RANGE;
 	}
 	
 	public static void out() {
 		System.out.println("Percentage of won games our of last " + WinCounter.WIN_LOSE_RANGE + " games = " + WinCounter.getPercentageWon() + "%");
 	}
 }

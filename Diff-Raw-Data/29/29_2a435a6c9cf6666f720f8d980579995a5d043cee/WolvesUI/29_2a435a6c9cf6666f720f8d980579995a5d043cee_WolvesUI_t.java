 package wolves;
 
 
 public interface WolvesUI {
 	
 	public static byte VISION_INNO = 1;
 	public static byte VISION_WOLF = 2;
 	public static byte VISION_NOPE = 0;
 	
 	public static int INDEX_GOOD = 0;
 	public static int INDEX_EVIL = 1;
 	public static int INDEX_ALIVE = 2;
 	public static int INDEX_DEAD = 3;
 	
 	public static int ROLE_BLUE = 1; //negative numbers are dead
 	public static int ROLE_SEER = 2;
 	public static int ROLE_WOLF = 3;
 	
 	
 	public int getNumPlayers();
 	public int getNumWolves();
 	
	public void displayMessage(String message);
 	public void displayEndGame(int RoundNum, WinCodes WinCode, int[] knownRoles);
 	public int inputLynchTarget();
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles);
 	public int inputSeerTarget(int inSeer);
 	public void displaySingleVision(int Seer, int Target, byte Vision);
 	public void displayAllStates(String AllStateText);
 	public boolean getDebugMode();
 	public int InputSingleWolfTarget(int inPlayer);
 	public String inputName();
 	public void displayPlayerIDs(String[] inArray);
 	public String[] SetNames();
 	public void displayHistory(String HistoryText);
 
 }

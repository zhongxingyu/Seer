 package model.save;
 
 import java.io.File;
 
 /**
  * Holds and returns the different save paths for the game.
  * @author Vidar Eriksson, Linus Hagvall
  *
  */
 public class SavePath {
 	
 	
 	
 	/**
 	 * Gives the folder where all the data should be stored.
 	 * @return the folder where all the data should be stored.
 	 */
	public static String getSavePath(){
		File file = new File(System.getenv("APPDATA") + "/Operation5a/");
		file.mkdir();
		
		return file.getPath() + "/";
 	}
 	
 	/**
 	 * Gives the folder the savefiles should be stored in.
 	 * @return the folder the savefiles should be stored in.
 	 */
 	public static String getSaveFolder() {
 		File file = new File(getSavePath() + "saves/");
 		file.mkdir();
 		
 		return file.getPath() + "/";
 	}
 	
 	/**
 	 * Gives an array of all paths to all the save files that exists.
 	 * @return an array of all paths to all the save files that exists.
 	 */
 	public static String[] getSaveFiles() {
 		File file = new File(SavePath.getSaveFolder());
 		File[] files = file.listFiles();
 		
 		String[] arr = new String[files.length];
 		for(int i = 0; i < files.length; i++) {
 			arr[i] = files[i].getName();
 		}
 		return arr;
 	}
 	
 	/**
 	 * 
 	 * @return the path settings is to be saved at.
 	 */
 	public static String getSettingsPath(){
 		return getSavePath() + "Settings.txt";
 	}
 	/**
 	 * 
 	 * @return the path the high score is to be saved at.
 	 */
 	public static String getHighScorePath(){
 		return getSavePath() + "HighScore.txt";
 	}
 }

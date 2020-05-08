 package riskyspace.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import riskyspace.model.Player;
 import riskyspace.model.World;
 
 /**
  * A class for handling game data used for saving and loading games.
  * All information required to load a game is saved to file. You can load last autosaved
  * game, or you can load a game through its name as String.
  * 
  * @author rapp
  */
 public class GameDataHandler {
 	
 	private static File riskySpace = null;
 	private static File riskySave = null;
 	private static File oldSave = null;
 	private static File newSave = null;
 	
 	public final static String LAST_AUTOSAVE = "LAST-AUTOSAVE";
 	public final static String PREVIOUS_AUTOSAVE = "PREV-AUTOSAVE";
 	
 	/**
 	 * A default constructor.
 	 */
 	private GameDataHandler() {
 		;
 	}
 	
 	public static void init() {
 		init(System.getProperty("user.home"));
 	}
 	
 	public static void init(String saveFolder) {
 		saveFolder = saveFolder + File.separator + "RiskySpace" + File.separator;
 		riskySpace = new File(saveFolder);
 		riskySave = new File(saveFolder + File.separator + "Save");
 		
 		oldSave = new File(riskySave + File.separator + PREVIOUS_AUTOSAVE + ".rsg");
 		newSave = new File(riskySave + File.separator + LAST_AUTOSAVE + ".rsg");
 		
 		// Create the folders and files unless they already exist.
 		try {
 			riskySpace.mkdir();
 			riskySave.mkdir();
 			oldSave.createNewFile();
 			newSave.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Save the current game to default game name "lastAutoSave".
 	 * The game currently saved in "last_autosave" will be moved to "previous_autosave".
 	 * "previous_autosave" will be overwritten.
 	 * @param world - the world describing the game you wish to save.
 	 * @param players - a list containing the active players in the game.
 	 * @param currentPlayer - the player whose turn it was when the save was performed.
 	 * @param turn - an integer representing a game counter of how many turns have been played.
 	 * @param gameMode - the game mode setting of game.
 	 */
 	public static void autoSave(World world, List<Player> players, Player currentPlayer, int turn, String gameMode) {
 		try {
 			oldSave.createNewFile();
 			newSave.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 		
 		//Store the former last_autosave as previous_autosave
 		if (oldSave.delete()) {
 			newSave.renameTo(oldSave);
 		}
 		
 		saveGame(world, players, currentPlayer, turn, gameMode, LAST_AUTOSAVE);
 	}
 	
 	/**
 	 * Saves the game with a custom name that will not be overwritten by saveGame.
 	 * @param world - the world describing the game you wish to save.
 	 * @param players - a list containing the active players in the game.
 	 * @param currentPlayer - the player whose turn it was when the save was performed.
 	 * @param turn - an integer representing a game counter of how many turns have been played.
 	 * @param gameMode - the game mode setting of game.
 	 * @param gameName - the name of the file to which you want to save the game.
 	 */
 	public static void saveGame(World world, List<Player> players, Player currentPlayer, int turn,
 			String gameMode, String gameName) {
		
		
 		try {
			FileOutputStream fos = new FileOutputStream(riskySave + File.separator + gameName + ".rsg");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(new SavedGame(world, players, currentPlayer, turn, gameMode, gameName));
 			oos.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}	
 	
 	/**
 	 * Load a game from file name.
 	 * @param gameName - the name of the file which game has been saved to.
 	 * @return 
 	 * @throws IOException
 	 */
 	public static SavedGame loadGame(String gameName) throws IOException {	
 		try {
 			FileInputStream fis = new FileInputStream(riskySave + File.separator + gameName.toUpperCase() + ".rsg");
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			SavedGame gameData = (SavedGame) ois.readObject();
 			ois.close();
 			
 			return gameData;
 			
 		} catch (ClassNotFoundException  e) {
 			System.out.println(e);
 		}
 		return null;
 	}
 	
 	/**
 	 * Load the game currently saved in default file "lastAutoSave".
 	 */
 	public static void loadAutoSave() {
 		try {
 			loadGame(LAST_AUTOSAVE);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * View the games in the RiskySpace\\Save folder.
 	 * @return - An array containing save file names as strings.
 	 */
 	public static String[] getSavedGames() {
 		File directory = riskySave;
 		FilenameFilter rsgFilter = new FilenameFilter() {
 			public boolean accept(File directory, String fileName) {
 				return fileName.endsWith(".rsg");
 			}
 		};
 		if (directory.list(rsgFilter).length == 0) {
 			return new String[] {"No saved games found in: \n" + directory.getPath()};
 		} else {
 			String[] gameNames = directory.list(rsgFilter);
 			for (int i = 0; i < gameNames.length; i++) {
 				gameNames[i] = gameNames[i].substring(0, gameNames[i].length()-4);
 			}
 			return gameNames;
 		}
 	}
 	
 	/**
 	 * Get load game information for a specific game file.
 	 * @param gameName - the name of the save file you want to inspect.
 	 * @return - an array of strings representing players, turn and game mode of the save file.
 	 */
 	public static String[] getGameInfo(String gameName) {
 		FileInputStream fis;
 		ObjectInputStream ois = null;
 
 			try {
 				fis = new FileInputStream(riskySave + File.separator + gameName.toUpperCase() + ".rsg");
 				ois = new ObjectInputStream(fis);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			String[] gameInfo = {null, null, null};
 		
 			try {
 
 				SavedGame gameData = (SavedGame) ois.readObject();
 		
 				gameInfo[0] = gameData.getPlayers().toString();
 				gameInfo[1] = gameData.getTurn()+"";
 				gameInfo[2] = gameData.getGameMode();
 			
 				ois.close();				
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		return gameInfo;
 	}
 	
 	public static File getSaveFolder() {
 		return riskySpace;
 	}
 }

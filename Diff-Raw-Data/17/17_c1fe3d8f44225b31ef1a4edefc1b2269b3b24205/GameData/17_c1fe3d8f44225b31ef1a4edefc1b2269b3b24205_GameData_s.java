 package riskyspace.data;
 
 import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.sun.xml.internal.bind.v2.runtime.Name;
 
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.Ship;
 import riskyspace.model.ShipType;
 import riskyspace.model.World;
 
 public class GameData {
 	
 	private Path riskySpace = null;
 	private Path riskySave = null;
 	private Path pathOldSave = null;
 	private Path pathNewSave = null;
 	
 	private String saveFolder = "c:\\RiskySpace\\Save\\";
 	
 	File newSave = null;
 	File oldSave = null;
 	
 	private final static Position pos = new Position (1, 1);
 	
 	public GameData() {
 		
 		pathOldSave = FileSystems.getDefault().getPath(saveFolder+"previous_autosave.rsg");
 		pathNewSave = FileSystems.getDefault().getPath(saveFolder+"last_autosave.rsg");
 
 		riskySpace = FileSystems.getDefault().getPath("c:\\RiskySpace");
 		riskySave = FileSystems.getDefault().getPath(saveFolder);
 		
 		try {
 			if (Files.notExists(riskySpace) && Files.notExists(riskySave)) {
 				Files.createDirectory(riskySpace);
 				Files.createDirectory(riskySave);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public GameData(String saveFolder) {
 		this();
 		this.saveFolder = saveFolder;
 	}
 
 	public void autoSave(World world, List<Player> players, Player currentPlayer, int turn,
 			String gameMode) throws IOException {
 
 		new File(pathOldSave.toString()).createNewFile();	
 		new File(pathNewSave.toString()).createNewFile();
 		
 		Files.copy(pathNewSave, pathOldSave, REPLACE_EXISTING);
 
 		saveGame(world, players, currentPlayer, turn, gameMode, null);
 	}
 	
 	public void saveGame(World world, List<Player> players, Player currentPlayer, int turn,
 			String gameMode, String gameName) {
 	
 		FileOutputStream fos;
 		try {
 			if (gameName == null) {
 				fos = new FileOutputStream(pathNewSave.toString());
 			} else {
 				File customSave = new File(saveFolder + gameName + ".rsg");
 				fos = new FileOutputStream(customSave);
 			}
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 		
 			oos.writeObject(world);
 			oos.writeObject(players);
 			oos.writeObject(currentPlayer);
 			oos.writeInt(turn);
 			oos.writeObject(gameMode);
 			
 			oos.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 	
 	public static void move(Path from, Path to) {	
 		try {
 			Files.move(from, to, REPLACE_EXISTING);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	
 	public void loadGame(String gameName) throws IOException {	
 		try {
 			FileInputStream fis;
 			if (gameName == null) {
 				fis = new FileInputStream(pathNewSave.toString());
 			} else {
 				Path pathCustomSave = FileSystems.getDefault().getPath(saveFolder+ gameName + ".rsg");
 				fis = new FileInputStream(pathCustomSave.toString());
 			}
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			
 			World world = (World) ois.readObject();
			Player[] players = (Player[]) ois.readObject();
 			Player currentPlayer = (Player) ois.readObject();
 			int turn = (Integer) ois.readInt();
 			String gameMode = (String) ois.readObject();
 			
 			/* TODO
 			 * Use this info to create a new game instance:
 			 * new GameManager() <- needs to be implemented
 			 * GameManager.init(world);  <- need new init for a world loaded from file
 			 */
 						
 			ois.close();
 		} catch (ClassNotFoundException  e) {
 			System.out.println(e);
 		}
 	}
 	
 	public void loadAutoSave() {
 		try {
 			loadGame(null);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String[] getSavedGames() {
 		
 		File directory = new File(saveFolder);
 		FilenameFilter rsgFilter = new FilenameFilter() {
 			public boolean accept(File directory, String fileName) {
 				return fileName.endsWith(".rsg");
 			}
 		};
 		if (directory.list(rsgFilter).length == 0) {
 			return new String[] {"No saved games found in: \n" + saveFolder};
 		} else {
 			String[] gameNames = directory.list(rsgFilter);
 			for (int i = 0; i < gameNames.length; i++) {
 				gameNames[i] = gameNames[i].substring(0, gameNames[i].length()-4);
 			}
 			return gameNames;
 		}
 	}
 	
 	public String[] getGameInfo(String gameName) {
 		
 		Path pathCustomSave = FileSystems.getDefault().getPath(saveFolder + gameName + ".rsg");
 		
 		FileInputStream fis;
 		ObjectInputStream ois = null;
 
 			try {
 				fis = new FileInputStream(pathCustomSave.toString());
 				ois = new ObjectInputStream(fis);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			String[] gameInfo = {null, null, null};
 		
 			try {
 				World world = (World) ois.readObject();
 				List<Player> players = (List<Player>) ois.readObject();
 				Player currentPlayer = (Player) ois.readObject();
 				int turn = (Integer) ois.readInt();
 				String gameMode = (String) ois.readObject();
 				
 				gameInfo[0] = players.toString();
 				gameInfo[1] = turn+"";
 				gameInfo[2] = gameMode;
				
 				ois.close();
 				
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		return gameInfo;
 	}
 	
 	
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
 		World world = new World();
 		
 		for (int i = 0; i < 2; i++) {
 			world.getTerritory(pos).addFleet(new Fleet(new Ship(ShipType.SCOUT), Player.BLUE));
 		}
 		
 		GameData gd = new GameData();
 		List<Player> players = new ArrayList<Player>();
 		players.add(Player.BLUE);
 		players.add(Player.RED);
 		
 		for (int i = 0; i < gd.getSavedGames().length; i++) {
 			System.out.println(gd.getSavedGames()[i]);
 		}
 		
		gd.autoSave(world, null, Player.BLUE, 0, "Annihilation");
 		
 		gd.saveGame(world, players, Player.RED, 15, "Annihilation", "game7");
 		
		for (int i = 0; i < gd.getGameInfo("game7").length; i++) {
			System.out.println(gd.getGameInfo("game7")[i]);
 		}
 		
 		gd.loadAutoSave();
 	}
 }

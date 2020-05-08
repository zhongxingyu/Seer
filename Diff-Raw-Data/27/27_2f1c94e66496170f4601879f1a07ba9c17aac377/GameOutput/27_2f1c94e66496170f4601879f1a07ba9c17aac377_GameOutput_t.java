 package game.ui;
 
 
 import static org.junit.Assert.fail;
 import game.gui.InfoPanel;
 import game.logic.Game;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import maze_objects.Dragon;
 import maze_objects.Eagle;
 import maze_objects.Hero;
 import maze_objects.Maze;
 import maze_objects.MazeSymbol;
 import maze_objects.Sword;
 import maze_objects.Tile;
 
 public class GameOutput {
 	private static String PROMPT = "> ";
 	
 	public static String SAVE_EXTENSION = ".nanner";
 	public static String SAVE_EXTENSION_TYPE = "nanner";
 	public static String SAVE_EXTENSION_DESCRIPTION = "nanner Files";
 
 	//Returns an array with the symbols of the corresponding maze tiles
 	public static char[][] getMazeSymbols(Maze m) { 
 		char[][] mazePositions = new char[m.getRows()][m.getColumns()];
 
 		for(int r = 0; r < m.getRows(); r++)
 			for(int c = 0; c < m.getColumns(); c++) {
 				Tile currentTile = m.getPositions()[r][c];
 				switch(currentTile) {
 				case wall:
 					mazePositions[r][c] = MazeSymbol.wall;
 					break;
 				case empty:
 					mazePositions[r][c] = MazeSymbol.empty;
 					break;
 				case exit:
 					mazePositions[r][c] = MazeSymbol.exit;
 					break;
 				}
 			}
 		return mazePositions;
 	}
 	
 	public static void showSaveGameDialog(Game game) {
 		JFileChooser fileChooser = new JFileChooser();
 		
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(SAVE_EXTENSION_DESCRIPTION, SAVE_EXTENSION_TYPE);
 		fileChooser.setFileFilter(filter);
 		
 		if (fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
 			
 			File file = fileChooser.getSelectedFile();
 			
 			String filePath = file.getAbsolutePath();
 			
 			if(!filePath.endsWith(SAVE_EXTENSION)) {
 			    file = new File(filePath + SAVE_EXTENSION);
 			}
 			
 			// save to file
 			save(game, file);
 		}
 	}
 
 	//Returns an array with the symbols of the corresponding maze tiles
 	public static BufferedImage[][] getMazePictures(Maze m, MazePictures pictures) { 
 		BufferedImage[][] mazePositions = new BufferedImage[m.getRows()][m.getColumns()];
 
 		for(int r = 0; r < m.getRows(); r++)
 			for(int c = 0; c < m.getColumns(); c++) {
 				Tile currentTile = m.getPositions()[r][c];
 				switch(currentTile) {
 				case wall:
 					mazePositions[r][c] = pictures.wall;
 					break;
 				case empty:
 					mazePositions[r][c] = pictures.empty;
 					break;
 				case exit:
 					mazePositions[r][c] = pictures.exit;
 					break;
 				}
 			}
 		return mazePositions;
 	}
 
 	public static void printMaze(Maze m) {
 
 		char[][] mazePositions = getMazeSymbols(m);
 
 		for(int i = 0; i < 100; i++)
 			System.out.println();
 		for (int x = 0; x < m.getRows(); x++) {
 			for (int y = 0; y < m.getColumns(); y++) {
 				System.out.print(mazePositions[x][y]);
 				System.out.print(MazeSymbol.space);
 			}
 			System.out.print('\n');
 		}
 	}
 
 	/*public static void printMaze(Graphics g, Maze m) {
 
 		BufferedImage mazeImages[][] = GameOutput.getMazePictures(m);
 
 		for(int r = 0; r < m.getRows(); r++)
 			for(int c = 0; c < m.getColumns(); c++) {
 				g.drawImage(mazeImages[r][c], c * mazeImages[r][c].getWidth(), r * mazeImages[r][c].getHeight(), null);
 			}
 	}*/
 
 	public static void printGame(Game g) {
 
 		Maze m = g.getMaze();
 		Hero h = g.getHero();
 		ArrayList<Dragon> d = g.getDragons();
 		Sword s = g.getSword();
 		Eagle e = g.getEagle();
 
 		char[][] mazePositions = getMazeSymbols(m);
 
 		// hero printing
 		if(h.print)
 			if(h.getState() == Hero.ARMED || h.getState() == Hero.EXITED_MAZE)
 				mazePositions[h.getRow()][h.getColumn()] = MazeSymbol.armedHero;
 			else
 				mazePositions[h.getRow()][h.getColumn()] = MazeSymbol.hero;
 
 		// dragon printing
 		for(int i = 0; i < g.getNumberOfDragons(); i++) {
 			if(d.get(i).getState() == Dragon.ALIVE && d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = MazeSymbol.guardedSword;
 			else if(d.get(i).getState() == Dragon.ALIVE && !d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = MazeSymbol.dragon;
 			else if(d.get(i).getState () == Dragon.ASLEEP && d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = MazeSymbol.sleepingGuardedSword;
 			else if(d.get(i).getState() == Dragon.ASLEEP && !d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = MazeSymbol.sleepingDragon;
 
 			if(!s.isTaken())
 				mazePositions[s.getRow()][s.getColumn()] = MazeSymbol.sword;
 		}
 
 		// eagle printing
 		if ( !e.isWithHero() && e.getState() != Eagle.DEAD){
 			switch (mazePositions[ e.getRow() ][e.getColumn() ] ) {
 			case MazeSymbol.hero:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagleOnHero;
 				break;
 			case MazeSymbol.dragon:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagleOnDragon;
 				break;
 			case MazeSymbol.wall:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagleOnWall;
 				break;
 			case MazeSymbol.empty:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagle;
 				break;
 			case MazeSymbol.sleepingDragon:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagleOnSleepingDragon;
 				break;
 			case MazeSymbol.sword:
 				mazePositions[ e.getRow() ][e.getColumn() ] = MazeSymbol.eagleWithSword;
 				break;
 			default:
 				break;
 			}
 		}
 
 		for (int x = 0; x < m.getRows(); x++) {
 			for (int y = 0; y < m.getColumns(); y++) {
 				System.out.print(mazePositions[x][y]);
 				System.out.print(MazeSymbol.space);
 			}
 			System.out.println();
 		}
 		System.out.println();
 
 	}
 
 	public static void printGame(Game g, Graphics graphs, MazePictures pictures) {
 
 		Maze m = g.getMaze();
 		Hero h = g.getHero();
 		ArrayList<Dragon> d = g.getDragons();
 		Sword s = g.getSword();
 		Eagle e = g.getEagle();
 
 		BufferedImage[][] mazePositions = getMazePictures(m, pictures);
 
 		// dragon printing
 		for(int i = 0; i < d.size(); i++) {
 			if(d.get(i).getState() == Dragon.ALIVE && d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = pictures.guardedSword;
 			else if(d.get(i).getState() == Dragon.ALIVE && !d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = pictures.dragon;
 			else if(d.get(i).getState () == Dragon.ASLEEP && d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = pictures.sleepingGuardedSword;
 			else if(d.get(i).getState() == Dragon.ASLEEP && !d.get(i).getHasSword())
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = pictures.sleepingDragon;
 			else if(d.get(i).getState() == Dragon.DEAD)
 				mazePositions[d.get(i).getRow()][d.get(i).getColumn()] = pictures.deadDragon;
 		}
 		
 		if(!s.isTaken() && s.print)
 			mazePositions[s.getRow()][s.getColumn()] = pictures.sword;
 
 		// hero printing
 		if(h.print)
 			if(h.getState() == Hero.ARMED || h.getState() == Hero.EXITED_MAZE)
 				mazePositions[h.getRow()][h.getColumn()] = pictures.armedHero;
 			else if (h.getState() == Hero.DEAD)
 				mazePositions[h.getRow()][h.getColumn()] = pictures.deadHero;
 			else
 				mazePositions[h.getRow()][h.getColumn()] = pictures.hero;
 
 		// eagle printing
 		if ( !e.isWithHero() && e.getState() != Eagle.DEAD){
 			if(g.checkIfEagle(h.getRow(), h.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagleOnHero;
 			else if(g.checkIfOnAwakeDragon(e.getRow(), e.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagleOnDragon;
 			else if(g.checkIfOnSleepingDragon(e.getRow(), e.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagleOnSleepingDragon;
 			else if(g.checkIfSword(e.getRow(), e.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagleWithSword;
 			else if(m.checkIfWall(e.getRow(), e.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagleOnWall;
 			else if(m.checkIfEmpty(e.getRow(), e.getColumn()))
 				mazePositions[ e.getRow() ][e.getColumn() ] = pictures.eagle;
 		}
 
 		for (int x = 0; x < m.getRows(); x++) {
 			for (int y = 0; y < m.getColumns(); y++) {
 				graphs.drawImage(mazePositions[x][y],
 						y * mazePositions[x][y].getWidth(),
 						x * mazePositions[x][y].getHeight(), null);
 			}
 		}
 	}
 
 	public static void printAskForMove() {
 		System.out.print("Move your hero (WASD, only first input will be considered): ");
 		//System.out.print(PROMPT);
 	}
 
 	public static void printEventQueue(LinkedList<GameEvent> events) { //Prints the events on the event queue given
 		while(!events.isEmpty())
 			printEvent(events.removeFirst());
 	}
 
 	public static void printEventQueue(LinkedList<GameEvent> events, InfoPanel infoPanel) {
 		String info = new String();
 
 		while(!events.isEmpty())
 			info += (events.removeFirst().getMessage()) + '\n';
 
 		infoPanel.textPane.setText(info);
 	}
 
 	public static void printEvent(GameEvent ev) { //Prints the message field of the GameEvent given
 		System.out.println(ev.getMessage());
 	}
 
 	public static void printStartMessage() { //Prints a welcome message
 		System.out.println("Welcome, challenger.");
 	}
 
 	public static void printOptions(int n) { //Asks player for the maze options wanted. n = 0 to ask for maze type, n = 1 to ask for rows, n = 2 to ask for columns
 
 		switch(n) {
 		case 0:
 			System.out.println("Give a specific maze size? (Y/N): ");
 			System.out.print(PROMPT);
 			break;
 		case 1:
 			System.out.println("Please specify the desired maze size.");
 			System.out.println("Number of rows? ");
 			System.out.print(PROMPT);
 			break;
 		case 2:
 			System.out.println("Number of columns? ");
 			System.out.print(PROMPT);
 			break;
 		}
 
 	}
 
 	public static void printMazeSizeError() {
 		System.out.println("Invalid maze size given. Starting predefined maze!");
 	}
 
 	public static void printDragonOptions() { //Prints dragon type related options
 		System.out.println("\nPlease specify the type of dragons you want on the maze.");
 		System.out.println("0 for static dragons, 1 for an always awake dragons, 2 for randomly sleeping dragons.");
 		System.out.println("Choose your option ");
 		System.out.print(PROMPT);
 	}
 
 	public static void printMultipleDragonOptions() {
 		System.out.println("\nWould you like to generate a number of dragons proportional to the size of the maze?");
 		System.out.println("Otherwise, only one will be generated. (Y/N): ");
 		System.out.print(PROMPT);
 	}
 
 	public static void clearScreen(){
 		for(int i = 0; i < 100; i++)
 			System.out.println();
 	}
 
 	public static void save(Game game, File file){
 		try
 		{
 			FileOutputStream fos = new FileOutputStream(file);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(game);
 			oos.close();
 		}
 		catch (Exception ex)
 		{
 			fail("Exception thrown during test: " + ex.toString());
 		}
 	}
 
 	public static Game load(File file){
 		try
 		{
 			FileInputStream fis = new FileInputStream(file);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			Game game = (Game) ois.readObject();
 			ois.close();
 			return game;
 		}
 		catch (Exception ex)
 		{
			fail("Problem loading file: " + ex.toString());
 		}
 		return null;
 	}
 }

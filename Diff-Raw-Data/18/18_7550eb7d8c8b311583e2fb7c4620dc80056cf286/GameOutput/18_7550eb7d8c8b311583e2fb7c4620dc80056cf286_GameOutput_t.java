 package game.ui;
 
 
 import game.logic.Game;
 
 import java.util.LinkedList;
 
 import maze_objects.Dragon;
 import maze_objects.Hero;
 import maze_objects.MazeSymbol;
 import maze_objects.Maze;
 import maze_objects.Sword;
 
 public class GameOutput {
 
 	public static void printMaze(Maze m) {
 
 		char[][] mazePositions = m.getPositions();
 
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
 
 	public static void printGame(Game g) {
 
 		Maze m = g.getMaze();
 		Hero h = g.getHero();
 		Dragon d = g.getDragon();
 		Sword s = g.getSword();
 
 		char[][] mazePositions = new char[m.getRows()][m.getColumns()];
 		
 		for(int r = 0; r < m.getRows(); r++)
 			for(int c = 0; c < m.getColumns(); c++)
 				mazePositions[r][c] = m.getPositions()[r][c];
 
		if(h.getState() == Hero.ARMED || h.getState() == Hero.EXITED_MAZE)
 			mazePositions[h.getRow()][h.getColumn()] = MazeSymbol.armedHero;
 		else
 			mazePositions[h.getRow()][h.getColumn()] = MazeSymbol.hero;
 		
 		if(d.getState() == Dragon.ALIVE && d.getHasSword())
 			mazePositions[d.getRow()][d.getColumn()] = MazeSymbol.guardedSword;
 		else if(d.getState() == Dragon.ALIVE && !d.getHasSword())
 			mazePositions[d.getRow()][d.getColumn()] = MazeSymbol.dragon;
 		
 		if(!s.getTaken())
 			mazePositions[s.getRow()][s.getColumn()] = MazeSymbol.sword;
 
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
 	
 	public static void printAskForMove() {
 		System.out.print("Move your hero (WASD, only first input will be considered): ");
 	}
 
 	public static void printEventQueue(LinkedList<GameEvent> events) { //Prints the events on the event queue given
 		while(!events.isEmpty())
 			printEvent(events.removeFirst());
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
 			System.out.print("Give a specific maze size? (Y/N): ");
 			break;
 		case 1:
 			System.out.println("Please specify the desired maze size.");
 			System.out.print("Number of rows? ");
 			break;
 		case 2:
 			System.out.print("Number of columns? ");
 			break;
 		}
 
 	}
 
 	public static void printMazeSizeError() {
 		System.out.println("Invalid maze size given. Starting predefined maze!");
 	}
 
 	public static void print(Maze m){
 		char[][] mazePositions = new char[m.getRows()][m.getColumns()];
 		
 		for(int r = 0; r < m.getRows(); r++)
 			for(int c = 0; c < m.getColumns(); c++)
 				mazePositions[r][c] = m.getPositions()[r][c];
 		
 		for (int x = 0; x < m.getRows(); x++) {
 			for (int y = 0; y < m.getColumns(); y++) {
 				System.out.print(mazePositions[x][y]);
 				System.out.print(MazeSymbol.space);
 			}
 			System.out.print('\n');
 		}
 	}
 }

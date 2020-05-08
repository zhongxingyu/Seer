 package program;
 
 import java.util.Scanner;
 
 import model.Maze;
 import model.Player;
 
 public class WumpusController {
 	
 	private Player player;
 	private Maze maze;
 	private Scanner keyboard;
 	
 
 	public static void main(String[] args) {
 		WumpusController wumpusController = new WumpusController(40);
 		wumpusController.runGame();
 	}
 
 	public WumpusController(int size) {
 		maze = new Maze(size);
 		player = new Player();
 		player.setPosition(maze.getEntrance());
 		
 	}
 	
 	private void runGame() {
 		
 		printGreeting();
 		System.out.println("You can: ");
 		System.out.println("Go North (N)");
 		System.out.println("'Cheat' and print the maze (P)");
 		System.out.println("Quit the game (Q)");
 		
 		boolean gameOver = false;
 		keyboard = new Scanner(System.in);
 		
 		while(!gameOver) {
 			System.out.println();
 			System.out.print("What will you do next: ");
 			String command = keyboard.next();
 			
 			if(command.equals("N")) {
 				movePlayer("N");
 			}
 			else if(command.equals("E")) {
 				movePlayer("E");
 			}
			else if(command.equals("S")) {
 				movePlayer("S");
 			}
 			else if(command.equals("W")) {
 				movePlayer("W");
 			}
 			else if(command.equals("P")) {
 				System.out.println();
 				maze.printMaze(player.getPosition());
 			}
 			else if(command.equals("Q")) {
 				System.out.print("Are you sure you want to quit? Y/N: ");
 				String confirm = keyboard.next();
 				if(confirm.equals("Y")) {
 					gameOver = true;
 				}
 			}
 			else {
 				System.out.println("I don't know what you mean by " + command);
 			}
 			System.out.println();
 			maze.printMaze(player.getPosition());
 		}
 		
 		System.out.println("Thanks for playing!");
 	}
 
 	private void printGreeting() {
 		System.out.println("********************************************");		
 		System.out.println("You are standing  at the entrance of a cave.");		
 		System.out.println("Inside is the legenday Wumpus! You have been");		
 		System.out.println("tasked to kill it, good luck!");
 		System.out.println("********************************************");		
 	}
 
 	
 	public void movePlayer(String direction) {
 		int[] coordinates = maze.getRowAndColumn(player.getPosition());
 		int row = coordinates[0];
 		int column = coordinates[1];
 		if(direction.equals("N")){
 			if(maze.getRoom(row-1, column).isPath()){
 				player.setPosition(maze.getRoom(row-1, column));
 			}
 			else{
 				System.out.println("The path north is blocked!");
 			}
 		}
 		else if(direction.equals("E")) {
 			if(maze.getRoom(row, column+1).isPath()){
 				player.setPosition(maze.getRoom(row, column+1));
 			}
 			else{
 				System.out.println("The path east is blocked!");
 			}
 			
 		}
 		else if(direction.equals("S")) {
			if(maze.getRoom(row+1, column).isPath()){
				player.setPosition(maze.getRoom(row+1, column));
 			}
 			else{
				System.out.println("The path south is blocked!");
 			}
 			
 		}
 		else if(direction.equals("W")) {
 			if(maze.getRoom(row, column-1).isPath()){
 				player.setPosition(maze.getRoom(row, column-1));
 			}
 			else{
 				System.out.println("The path west is blocked!");
 			}
 			
 		}
 		
 	}
 	/*
 	 * Getters and setters, used in tests.
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 	
 	public Maze getMaze() {
 		return maze;
 	}
 	
 	public void setMaze(Maze maze) {
 		this.maze = maze;
 	}
 }

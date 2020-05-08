 // Author: Ronald Ip
 
 // Usage: java GameOfLife [MAX_ITERATIONS]
 // Adjust FRAME_RATE manually.
 
 import java.util.*;
 import java.io.*;
 
 public class GameOfLife {
 	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
 		long MAX_ITERATIONS = 300;
 		if(args.length == 1) {
 			MAX_ITERATIONS = Long.parseLong(args[0]);
 		}
 		final int FRAME_RATE = 200; // 1000 = 1s.
 
 		int[][] grid = parseInputFile();
 		
 		// Shutdown hook for Sig TERM message.
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				System.out.println("\nThanks for playing, goodbye...");
 			}
 		});
 
		for(int g = 0; g <= MAX_ITERATIONS; Thread.sleep(FRAME_RATE), g++) {
 			printClearScreen();
 			printGrid(grid, g);
 			advanceLife(grid);
 		}
 
 	}
 
 	public static void advanceLife(int[][] grid) {
 		int[][] newGrid = new int[grid.length][grid[0].length];
 		for(int i = 0; i < grid.length; i++) {
 			for(int j = 0; j < grid[0].length; j++) {
 				newGrid[i][j] = grid[i][j];
 				// Find living cell.
 				if(grid[i][j] == 1) { // Found the organism!
 					// Check living conditions.
 					int neighbours = 0;
 					for(int k = i - 1; k <= i + 1; k++) {
 						for(int l = j - 1; l <= j + 1; l++) {
 							if(k < 0 || k >= grid.length || l < 0 || l >= grid[0].length) {
 								continue;
 							} else {
 								neighbours += grid[k][l]; // Counts itself.
 							}
 						}
 					}
 					// Check living conditions.
 					switch(neighbours - 1) {
 						case 0: case 1:
 							// Loneiness.
 							newGrid[i][j] = 0; // Die.
 							break;
 						case 2: case 3:
 							// Survive.
 							//newGrid[i][j] = 1;
 							break;
 						default:
 							// Overpopulation.
 							newGrid[i][j] = 0; // Die.
 							break;
 					}
 					
 				} else {
 					// Empty cells.
 					// Check for respawn.
 					int neighbours = 0;
 					for(int k = i - 1; k <= i + 1; k++) {
 						for(int l = j - 1; l <= j + 1; l++) {
 							if(k < 0 || k >= grid.length || l < 0 || l >= grid[0].length) {
 								continue;
 							} else {
 								neighbours += grid[k][l]; // Counts itself, but doesn't matter.
 							}
 						}
 					}
 					// Check respawn conditions.
 					if(neighbours == 3) {
 						newGrid[i][j] = 1; // Respawn!
 					}
 				}
 			}
 		}
 
 		// Update grid with newGrid.
 		for(int i = 0; i < grid.length; i++) {
 			for(int j = 0; j < grid[0].length; j++) {
 				grid[i][j] = newGrid[i][j];
 			}
 		}
 	}
 
 	public static void printClearScreen() {
 		final String ANSI_CLS = "\u001b[2J";
 
 		if(System.getProperty("os.name").toLowerCase().indexOf( "win" ) >= 0) {
 			// On windows, brain dead terminal. Clear screen with \n instead.
 			System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
 		} else {
 			// ANSI terminal, life's good.
 			System.out.print(ANSI_CLS);
 		}
 	}
 
 	public static void printGrid(int[][] input, int generation) {
 		System.out.println("Generation: " + generation);
 		for(int[] r : input) {
 			System.out.print("|");
 			for(int c : r) {
 				//System.out.print(c);
 				System.out.print((c == 1)? "*" : " ");
 			}
 			System.out.println("|");
 		}
 	}
 
 	public static int[][] parseInputFile() throws FileNotFoundException {
 		try(Scanner sc = new Scanner(new File("GameOfLife.input.txt"))) {
 			ArrayList<String> lines = new ArrayList<String>();
 			// Read the file here.
 			while(sc.hasNextLine()) {
 				String line = sc.nextLine().trim();
 				if(line.length() > 0 && line.charAt(0) != '#') {
 					lines.add(line);
 				}
 			}
 			int[][] output = new int[lines.size()][lines.get(0).length()];
 
 			for(int i = 0; i < output.length; i++) {
 				for(int j = 0; j < output[0].length; j++) {
 					output[i][j] = Integer.parseInt("" + lines.get(i).charAt(j));
 				}
 			}
 
 			return output;
 
 		} catch(FileNotFoundException e) {
 			System.err.println("File not found: " + e.getMessage());
 			throw new FileNotFoundException(e.getMessage());
 			//System.exit(1);
 		}
 
 		// No entry zone.
 		//return new int[0][0];
 	}
 }

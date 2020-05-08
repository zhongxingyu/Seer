 package com.github.rocketsurgery;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
 public class Level {
 
 	// values for generating the level
 	private static List<Wire> wires;
 	private static List<Node> nodes;
 	private static final float X_SIZE = 12;
 	private static final float Y_SIZE = 10;
 	
 	private static final String[] GAME_MODES = 				{ // must match gameModeStateValues
 															"Lowest Score", 
 															"Most Solved"
 															};
	private static final Integer[] GAME_MODE_STATE_VALUES = 	{ // must match gameModes
 															UncrossTheWires.LOWEST_SCORE,
 															UncrossTheWires.MOST_SOLVED
 															};
 	private static int selectedMode = 0;
 	
 	public static final int MIN_DIFFICULTY = 10;
 	public static final int MAX_DIFFICULTY = 120;
 	private static int difficulty = MIN_DIFFICULTY;
 
 	public static void generateLevel(GameContainer gc) throws SlickException {
 
 		nodes = new ArrayList<Node>();
 		wires = new ArrayList<Wire>();
 
 		Random rand = new Random();
 		
 		for (int i = 0; i < difficulty; i++) {
 
 			// generates coordinates between 0 and xSize/ySize inclusive
 			float x = rand.nextInt((int) X_SIZE + 1);
 			float y = rand.nextInt((int) Y_SIZE + 1);

 			// test to see if node already exists
 			boolean alreadyExists = false;
 			for (Node node : nodes)
				if (node.getCenterX() == x && node.getCenterY() == y)
 					alreadyExists = true;
 
 			// generate don't itterate if node already exists
 			if (alreadyExists) {
 				i--;
 			} else
				nodes.add(Node.createNode(x, y, X_SIZE, Y_SIZE, gc));
 		}
 
 		for (Node node : nodes) {
 			Node otherNode;
 			boolean wireDuplicated;
 			do {
 
 				// make sure randomly picked node isn't the same as the first
 				do {
 					otherNode = nodes.get(rand.nextInt(difficulty));
 				} while (otherNode == node);
 
 				// test if the wire is a duplicate
 				wireDuplicated = false;
 				for (Wire wire : wires) {
 					if (wire.hasEnds(node, otherNode)) {
 						wireDuplicated = true;
 						break;
 					}
 				}
 			} while (wireDuplicated);
 
 			wires.add(new Wire(node, otherNode));
 		}
 
 		// test if it's already solved
 		boolean levelComplete = true;
 		for (int i = 0; i < wires.size() - 1; i++) {
 			for (int j = i + 1; j < wires.size(); j++) {
 				if (wires.get(i).intersects(wires.get(j))) {
 					levelComplete = false;
 					break;
 				}
 			}
 		}
 
 		if (levelComplete)
 			generateLevel(gc); //if the level is already solved, generate a new one
 	}
 	
 	// methods for other classes to change data
 	public static List<Wire> getWires() {
 		return wires;
 	}
 	
 	public static List<Node> getNodes() {
 		return nodes;
 	}
 		
 	public static String[] getGameModes() {
 		return GAME_MODES;
 	}
 	
 	public static Integer[] getGameModeStateValues() {
 		return GAME_MODE_STATE_VALUES;
 	}
 
 	public static int getDifficulty() {
 		return difficulty;
 	}
 
 	public static void changeDifficulty(int diff) {
 		difficulty += diff;
 		if (difficulty < MIN_DIFFICULTY) {
 			difficulty = MIN_DIFFICULTY;
 		} else if (difficulty > MAX_DIFFICULTY)
 			difficulty = MAX_DIFFICULTY;
 	}
 	
 	public static int getSelectedMode() {
 		return selectedMode;
 	}
 	
 	public static void changeSelectedMode(int diff) {
 		selectedMode += diff;
 		if (selectedMode < 0)
 			selectedMode = GAME_MODES.length - 1;
 		else if (selectedMode > GAME_MODES.length - 1)
 			selectedMode = 0;
 	}
 	
 	@Deprecated
 	public static void loadLevel(String title, GameContainer gc) throws SlickException {
 
 		nodes = new ArrayList<Node>();
 		wires = new ArrayList<Wire>();
 		
 		Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"));
 		// find header for level
 		String next;
 		do {
 			next = scan.next();
 		} while (!next.equalsIgnoreCase(title));
 
 		// find "nodes" header
 		scan.next();
 
 		// parse nodes
 		while (scan.hasNextInt()) {
 			if (scan.nextInt() != nodes.size()) {
 				scan.close();
 				throw new IllegalArgumentException();
 			}
 			nodes.add(Node.createNode(scan.nextFloat(), scan.nextFloat(), X_SIZE, Y_SIZE, gc));
 
 		}
 
 		System.out.println(gc.getWidth() + " " + gc.getHeight());
 
 		for (Node node : nodes)
 			System.out.println(node.getCenterX() + " " + node.getCenterY());
 
 		// skip "connections" header
 		scan.next();
 
 		while (scan.hasNextInt()) {
 			// read in values from next line
 			String[] values = scan.nextLine().split(" ");
 
 			// create wires from values
 			for (int i = 1; i < values.length; i++)
 				wires.add(new Wire(nodes.get(Integer.parseInt(values[0])), nodes.get(Integer.parseInt(values[i]))));
 		}
 
 		// close scan
 		scan.close();
 	}
 	
 	@Deprecated
 	public static String[] loadLevelNames() {
 		Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"));
 
 		ArrayList<String> tempLevels = new ArrayList<>();
 
 		while (scan.hasNext()) {
 			String temp = scan.next();
 			System.out.println(temp);
 			tempLevels.add(temp);
 			scan.next();
 			scan.next();
 			scan.next();
 			while (scan.hasNextInt())
 				scan.next();
 			scan.next();
 			while (scan.hasNextInt())
 				scan.next();
 		}
 
 		scan.close();
 
 		return tempLevels.toArray(new String[0]);
 	}
 
 }

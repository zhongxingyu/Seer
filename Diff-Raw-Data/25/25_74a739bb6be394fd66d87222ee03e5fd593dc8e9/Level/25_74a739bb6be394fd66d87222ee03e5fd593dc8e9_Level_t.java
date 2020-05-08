 package com.github.rocketsurgery;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.newdawn.slick.GameContainer;
 
 public class Level {
 
 	public static List<Wire> wires;
 	public static List<Node> nodes;
	public static final float xSize = 12;
	public static final float ySize = 10;
 	
 	// difficulty selections
 	public static final String[] difficulties = {
 												"easy", 
 												"medium", 
 												"hard", 
 												"whaaaaaaaaat" 
 												};
	// min and max node counts for each difficulty
	// the largest value must be less than xSize * ySize
 	private static final int[][] NODE_LIMITS = 	{
 												{ 5, 12 }, // easy
 												{ 12, 20 }, // medium
 												{ 20, 30 }, // hard
 												{ 30, 40 } // whaaaaaaaaaat
 												};
 	public static int selectedDifficulty = 0;
 
 	public static void generateLevel(int difficulty, GameContainer gc) {
 
 		nodes = new ArrayList<Node>();
 		wires = new ArrayList<Wire>();
 
 		Random rand = new Random();
 		
 		// generate a number of nodes between the limits in NODE_LIMITS inclusive
 		int numNodes = rand.nextInt(NODE_LIMITS[selectedDifficulty][1] - NODE_LIMITS[selectedDifficulty][0])
 				+ NODE_LIMITS[selectedDifficulty][0];
 
 		for (int i = 0; i < numNodes; i++) {
 
 			// generates coordinates between 0 and xSize/ySize inclusive
 			float x = rand.nextInt((int) xSize + 1);
 			float y = rand.nextInt((int) ySize + 1);
 
 			// test to see if node already exists
 			boolean alreadyExists = false;
 			for (Node node : nodes)
 				if (node.getCenterX() == x && node.getCenterY() == y)
 					alreadyExists = true;
 
 			if (alreadyExists) {
 				i--;
 			} else
 				nodes.add(new Node(x, y));
 		}
 
 		for (Node node : nodes) {
 			Node otherNode;
 			boolean wireDuplicated;
 			do {
 
 				// make sure randomly picked node isn't the same as the first
 				do {
 					otherNode = nodes.get(rand.nextInt(numNodes));
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
 			generateLevel(difficulty, gc); //if the level is already solved, generate a new one
 	}
 
 	@Deprecated
 	public static void loadLevel(String title, GameContainer gc) {
 
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
 			nodes.add(new Node(scan.nextFloat(), scan.nextFloat()));
 
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

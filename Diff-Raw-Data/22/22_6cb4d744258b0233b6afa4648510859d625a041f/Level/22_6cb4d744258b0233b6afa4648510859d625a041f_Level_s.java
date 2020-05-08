 package com.github.rocketsurgery;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.newdawn.slick.GameContainer;
 
 public class Level {
 
 	private List<Wire> wires;
 	private List<Node> nodes;
 
 	private Level(List<Wire> wires, List<Node> nodes) {
 		this.wires = wires;
 		this.nodes = nodes;
 	}
 
 	public static Level loadLevel(String title, GameContainer gc) {
 
 		Scanner scan = new Scanner(Level.class.getClassLoader().getResourceAsStream("levels.dat"));
 		// find header for level
 		String next;
 		do {
 			next = scan.next();
 		} while (!next.equalsIgnoreCase(title));
 
 		float xSize = scan.nextInt();
 		float ySize = scan.nextInt();
 
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		ArrayList<Wire> wires = new ArrayList<Wire>();
 
 		// find "nodes" header
 		scan.next();
 
 		// parse nodes
 		while (scan.hasNextInt()) {
 			if (scan.nextInt() != nodes.size()) {
 				scan.close();
 				throw new IllegalArgumentException();
 			}
			nodes.add(new MultiNode(scan.nextInt() * (gc.getWidth() - 200) / xSize + 100, scan.nextInt() / ySize * (gc.getHeight() - 200) + 100));
 
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
 		
 		// create level and return it
 		return new Level(wires, nodes);
 	}
 
 	public List<Wire> getWires() {
 		return this.wires;
 	}
 
 	public List<Node> getNodes() {
 		return this.nodes;
 	}
 
 }

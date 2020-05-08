 package uk.ac.cam.groupproj.racethewild;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Animal implements Comparable<Animal> {
 	
 	private int id;
 	private String graphic; // Contains the filepath of the photo image.
 	private String sprite; // The sprite shown on the scrolly map.
 	private String name;
 	private String description;
 	private Node[] nodes;
 	private Colour colour;
 	private String hint;
	private String distancePerDay;
 	
 	public int getID() { return id; }
 	public String getGraphicPath() { return graphic; }
 	public String getSpritePath() { return sprite; }
 	public String getName() { return name; }
 	public String getFacts() { return description; }
 	public Node[] getNodes() { return nodes; }
 	public Colour getColour() { return colour; }
 	public String getHint() { return hint; }
	public String getDistancePerDay() { return distancePerDay; }
 	
 	public int compareTo(Animal other) {
 		return this.id - other.id;
 	}
 
 	public void setColour(Colour colour) { this.colour = colour;
 		System.out.println("Colour of " + name + " set to " + colour);
 	}
 	
 	public Animal(int id, String name, String description, String hint, String graphic, 
			String sprite, String distancePerDay, List<String> nodeNames) {
 		this.id = id;
 		this.name = name;
 		this.description = description;
 		this.hint = hint;
 		this.graphic = graphic;
 		this.sprite = sprite;
 		this.colour = Colour.White; // Default to unfound, and update from save data later.
 		this.distancePerDay = distancePerDay;
 		this.nodes = new Node[nodeNames.size()];
 		for (int i = 0; i < nodeNames.size(); i++)
 			try {
 				nodes[i] = Engine.get().lookupNode(nodeNames.get(i));
 			} catch(NodeNotFoundException e) {
 				System.err.println("Attempted to add animal " + name + "<#" + id + 
 						"> to invalid node \"" + nodeNames.get(i) + "\".");
 			}
 	}
 	
 }

 package gui;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import state.PartConfig;
 
 import agents.Part;
 
 public class GUI_Part extends GUI_Component implements Serializable {
 	private static final long serialVersionUID = 1624349219473726840L;
 	static String filename = "data/partConfigList.ser"; // find the list of available
 	static ArrayList<PartConfig> partList; // list of available parts from this
 	PartConfig partConfig;
 	int x, y;
 	public Part agentPart;
 	
 	// List of available parts, use any of the following string when calling Part constructor
 	// ["Bruce", "Crush", "Dory", "Gill", "Peach", "Pearl", "Sheldon", "Squirt", "Tad"]
 
 	// Constructor to load up an existing part
 	public GUI_Part(Part p) {
 		this(p, 0, 0, GUI_Part.getPartConfig((p.getPartName())));
 	}
 
 	// Constructor to create an entirely new part (used only for parts manager)
 	public GUI_Part(Part p, PartConfig pc) {
 		this(p, 0, 0, pc);
 	}
 
 	// Constructor to load up an existing part with coordinates
 	public GUI_Part(Part p, int x1, int y1) {
 		this(p, x1, y1, GUI_Part.getPartConfig(p.getPartName()));
 	}
 	
 	//Master constructor
 	private GUI_Part(Part p, int x1, int y1, PartConfig pc) {
 		x = x1;
 		y = y1;
 		agentPart = p;
         partConfig = pc;
 		myDrawing = new Drawing(x1, y1, pc.getImageFile());
 	}
 	
 	// ***Getters and setters
 	
 	public String getImageName() {
 		return myDrawing.filename;
 	}
 	
 	public String getName() {                
 		return partConfig.getPartName();
 	}
 
 	
 	public Part getPart() {
 		return agentPart;
 	}
 
 	public void setCoordinates(int x1, int y1) {
 		x = x1;
 		y = y1;
 	}
 
 	// ***GUI_Component methods
 	public void paintComponent() {
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 	}
 	
 	public void updateGraphics() {
 		setCoordinates(x, y);
 	}
 
 	// ***Object methods (overrides)
 	public String toString() {
 		return this.partConfig.getPartName();
 	}
 	
 	// ***Static methods
         
 	// Returns the part config object base on name of part
 	static PartConfig getPartConfig(String partName) {
 		boolean exists = (new File(filename)).exists();
 		// if file exists and get an arrayList of parts
 		if (exists) {
 			partList = loadData(filename);
 			// Loop through every part in partList till you find the matching
 			// name
 			for (PartConfig partConfig : partList) {
 				if (partConfig.getPartName().equals(partName)) {
 					return partConfig;
 				}
 			}
 		}
 		
		return new PartConfig(partName, partName + "fish.png", "default fish", partName.hashCode());
 	}
 
 	// Load serialized data from path to file
 	public static ArrayList<PartConfig> loadData(String path) {
 		try {
 			FileInputStream fileIn = new FileInputStream(path);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			@SuppressWarnings("unchecked")
 			ArrayList<PartConfig> parts = (ArrayList<PartConfig>) in.readObject();
 			in.close();
 			fileIn.close();
 			return parts;
 		} catch (IOException i) {
 			i.printStackTrace();
 		} catch (ClassNotFoundException c) {
 			System.out.println("GUI Part class not found");
 			c.printStackTrace();
 		}
 		return null;
 	}
 }

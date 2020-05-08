 package com.stacktraceinc.hypercut2d;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PartListHandler extends AbstractModelObject {
 	public static final String RECTANGLE = "Sta\u010Diakampis";
 	public static final String SQUARE = "Kvadratas";
 	
 	private List<Part> parts = new ArrayList<Part>();
 	private int autoIndex = 0;
 	
 	public List<Part> getParts() { return parts; }
 	
 	public PartListHandler() {
 		
 	}
 	
 	public void createNewPart() {
 		autoIndex++;
 		// Automatic part creation based on previously selected?
		addPart(new Part("Detal #" + autoIndex, RECTANGLE, 200, 200));
 	}
 	
 	public void clonePart(int index) {
 		Part part = parts.get(index);
 		addPart(part.clone());
 	}
 	
 	public void erasePart(int index) {
 		List<Part> oldValue = parts;
 		parts = new ArrayList<Part>(parts);
 		parts.remove(index);
 		firePropertyChange("parts", oldValue, parts);
 	}
 	
 	public void addPart(Part part) {
 		List<Part> oldValue = parts;
 		parts = new ArrayList<Part>(parts);
 		parts.add(part);
 		firePropertyChange("parts", oldValue, parts);
 	}
 }

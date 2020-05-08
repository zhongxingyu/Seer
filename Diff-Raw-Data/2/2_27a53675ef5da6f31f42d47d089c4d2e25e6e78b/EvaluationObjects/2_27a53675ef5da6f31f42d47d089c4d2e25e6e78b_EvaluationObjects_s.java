 package com.soartech.bolt.evaluation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 public class EvaluationObjects {
 	private HashSet<String> colors;
 	private HashSet<String> sizes;
 	private HashSet<String> shapes;
 	private HashMap<Integer, EvaluationObject> objects;
 	private final static Random rnd = new Random(System.currentTimeMillis());
 	private final static EvaluationObjects instance = new EvaluationObjects();
 	
 	private EvaluationObjects() {
 		colors = new HashSet<String>();
 		sizes = new HashSet<String>();
 		shapes = new HashSet<String>();
 		objects = new HashMap<Integer, EvaluationObject>();
 		setupObjects();
 //		setupCommands();
 	}
 	
 	private void setupObjects() {
 		addObject(1, "small", "yellow", "circle");
 		addObject(2, "medium", "yellow", "triangle");
 		addObject(3, "large", "yellow", "triangle");
 		addObject(4, "medium", "red", "square");
 		addObject(5, "small", "red", "arch");
 		addObject(6, "large", "red", "rectangle");
 		addObject(7, "medium", "green", "triangle");
 		addObject(8, "small", "green", "rectangle");
		addObject(9, "large", "green", "arche");
 	}
 	
 //	private void setupCommands() {
 //		ScriptDataMap dm = ScriptDataMap.getInstance();
 //		for(String color : colors) {
 //			dm.addUiCommand("check color "+color, new CheckColor(color));
 //		}
 //	}
 	
 	public static EvaluationObjects getInstance() {
 		return instance;
 	}
 	
 	public void addObject(int id, String size, String color, String shape) {
 		sizes.add(size);
 		colors.add(color);
 		shapes.add(shape);
 		objects.put(new Integer(id), new EvaluationObject(size, color, shape));
 	}
 	
 	public void addObject(Integer id, EvaluationObject eo) {
 		objects.put(id, eo);
 	}
 	
 	public List<EvaluationObject> randomObjectOrdering() {
 		ArrayList<EvaluationObject> random = new ArrayList<EvaluationObject>();
 		for(EvaluationObject o : objects.values()) {
 			random.add(o);
 		}
 		Collections.shuffle(random, rnd);
 		return random;
 	}
 }

 package database;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import utils.Logger;
 
 
 public class Database {
 	private boolean attributesFinished;
 	private LinkedList<String> attributes = new LinkedList<String>();
 	private LinkedList<LinkedList<Integer>> list = new LinkedList<LinkedList<Integer>>();
 
 	public void addAttribute(String attribute) {
 		if (attributesFinished) {
 			System.out.println("should not add attributes anymore");
 		}
 
 		attributes.add(attribute);
 	}
 
 	public Record newRecord() {
 		attributesFinished = true;
 
 		return new Record(attributes);
 	}
 
 	public void add(Record record) {
 		if (!record.isValid()) {
 			System.out.println("adding invalid record");
 		}
 
 		list.add(record.getValues());
 	}
 
 	public String toString() {
 		return attributes.toString() + "\n" + list.toString();
 	}
 	
 	public void export() {
 		Logger logger = new Logger();
 		logger.logAndStartNewLine("@relation cjdm");
 		
 		for (String attribute : attributes) {
 			logger.logAndStartNewLine("@attribute " + attribute + " integer");
 		}
 		
 		logger.logAndStartNewLine("@data");
 		
 		for (LinkedList<Integer> record : list) {
 			Iterator<Integer> iter = record.iterator();
 			while (iter.hasNext()) {
 				Integer value = iter.next();
 				if (iter.hasNext()) {
 					logger.log(value + ", ");
 				} else {
 					logger.logAndStartNewLine(value + "");
 				}
 			}
 		}
 		
		logger.writeToFile("javaprojectsources\\", "export.arff");
 	}
 }

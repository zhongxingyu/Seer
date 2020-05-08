 package logic;
 
 import java.io.File;
 
 public class RandomMapGenerator {
 	private Map<String,Integer>randMap;
 	public RandomMapGenerator(String filename) {
		parseFile(filename);
 
 	}
 private void pareseFile(String f){
 	File freq = new File(f);
 	randMap = new HashMap<String,Integer>();
 }
 }

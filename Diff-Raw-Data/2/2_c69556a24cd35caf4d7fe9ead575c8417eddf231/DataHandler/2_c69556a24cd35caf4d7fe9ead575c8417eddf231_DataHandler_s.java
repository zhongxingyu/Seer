 package project6867;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.javaml.core.Dataset;
 import net.sf.javaml.core.DefaultDataset;
 import net.sf.javaml.core.Instance;
 import net.sf.javaml.core.SparseInstance;
 import net.sf.javaml.tools.DatasetTools;
 
 public class DataHandler {
 	private static Random rand = new Random();
 	private static int FEATURE_SIZE = 4697;
 	private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
 	private static DataType type;
 	private static Map<String, Map<Integer, Integer>> masks = new HashMap<String, Map<Integer, Integer>>();
 	public static enum DataType{FULL, ONE, FIVE, TEN};
 	/*
 	 * Loads a file into the data field.
 	 * 
 	 * @param filename
 	 */
 	
 	public DataHandler(){
 	}
 	
 	public static Dataset getCompositeDataset(int numRecords){
 		System.out.println("Started fetching data at " + dateFormat.format(new Date()));
 		Dataset data = new DefaultDataset();
 		DatasetTools.merge(data,
 				loadFile("cleanbasic.data", numRecords),
 				loadFile("cleanbasicenemies.data", numRecords),
 				loadFile("cleanbasicgaps.data", numRecords),
 				loadFile("cleanenemiesblocks.data", numRecords),
 				loadFile("cleanenemiesblocksgaps.data", numRecords));
 		System.out.println("Finished fetching data at " + dateFormat.format(new Date()));
 		return data;
 	}
 	
 	public static Dataset getMaskedCompositeDataset(String maskFile, int numRecords){
 		System.out.println("Started fetching data at " + dateFormat.format(new Date()));
 		Dataset data = new DefaultDataset();
 		DatasetTools.merge(data,
 				loadMaskedFile(maskFile, "cleanbasic.data", numRecords),
 				loadMaskedFile(maskFile, "cleanbasicenemies.data", numRecords),
 				loadMaskedFile(maskFile, "cleanbasicgaps.data", numRecords),
 				loadMaskedFile(maskFile, "cleanenemiesblocks.data", numRecords),
 				loadMaskedFile(maskFile, "cleanenemiesblocksgaps.data", numRecords));
 		System.out.println("Finished fetching data at " + dateFormat.format(new Date()));
 		return data;
 	}
 		
 	public static Dataset getDataset(int numRecords, DataType type){
 		Dataset data = new DefaultDataset();
 		switch(type){
 			case FULL:	data = getCompositeDataset(numRecords); break;
 			case ONE:	data = getMaskedCompositeDataset("forward@0.01_5000mixed.data", numRecords); break;
 			case FIVE:	data = getMaskedCompositeDataset("forward@0.05_5000mixed.data", numRecords); break;
 			case TEN:	data = getMaskedCompositeDataset("forward@0.1_5000mixed.data", numRecords); break;
 			default:	data = getMaskedCompositeDataset("forward@0.01_5000mixed.data", numRecords); break;
 		}
 		return data;
 	}
 		
 	public static Dataset removeFeatures(Dataset d, String maskFile){
 		Dataset data = new DefaultDataset();
 		Instance feature_vector;
 		Map<Integer, Integer> mask = getMask(maskFile);
 		for(Instance i : d){
 			feature_vector = new SparseInstance(mask.size());
 			for(Entry<Integer, Integer> e : mask.entrySet()){
 				feature_vector.put(e.getValue(), i.value(e.getKey()));
 			}
 			data.add(feature_vector);
 		}
 		return data;
 	}
 	
 	public static Instance removeFeatures(Instance i, String maskFile){
 		Map<Integer, Integer> mask = getMask(maskFile);
 		Instance feature_vector = new SparseInstance(mask.size());
 		for(Entry<Integer, Integer> e : mask.entrySet()){
 			feature_vector.put(e.getValue(), i.value(e.getKey()));
 		}
 		return feature_vector;
 	}
 	
 	private static Dataset loadFile(String filename, int numRecords) {
 		System.out.println("Loading " + filename);
 		Dataset data = new DefaultDataset();
 		try {			
 			BufferedReader r = new BufferedReader(new FileReader(filename));
 			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
 			Matcher m;
 			String s;
 			Instance feature_vector;
 			for(int i = 0; i < 10*numRecords; i++){s = r.readLine(); //while ((s = r.readLine()) != null) {
 				m = p.matcher(s);
 				if (m.matches()) {
 					feature_vector = new SparseInstance(FEATURE_SIZE);
 					feature_vector.setClassValue(m.group(1));
 					for(String idx : m.group(2).replaceAll(" ","").split(",")){
 						try{
 							int index = Integer.parseInt(idx);
 							feature_vector.put(index, 1.0);
 						}catch(Exception e){
 							i--;
 							continue;
 						}
 					}
 					data.add(feature_vector);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return data.folds(data.size()/numRecords, rand)[0]; 
 	}
 	
 	private static Dataset loadMaskedFile(String maskFile, String filename, int numRecords) {
 		System.out.println("Loading " + filename + " and keeping features as specified by " + maskFile);
 		Dataset data = new DefaultDataset();
 		Map<Integer, Integer> mask = getMask(maskFile); //Map between old index and new shorter index
		int featureCount = 0;
 		try {
 			BufferedReader r = new BufferedReader(new FileReader(filename));
 			Pattern p = Pattern.compile("(.*)\\s\\{(.*)\\}");
 			Matcher m;
 			String s = "";
 			Instance feature_vector;
 			for(int i = 0; i < 10*numRecords && s != null; i++){s = r.readLine(); //while ((s = r.readLine()) != null) {
 				m = p.matcher(s);
 				if (m.matches()) {
 					feature_vector = new SparseInstance(featureCount);
 					feature_vector.setClassValue(m.group(1));
 					for(String idx : m.group(2).replaceAll(" ","").split(",")){
 						if(idx.length() > 0){
 							try{
 								int index = Integer.parseInt(idx);
 								if(mask.containsKey(index))
 									feature_vector.put(mask.get(index), 1.0);
 							}catch(Exception e){
 								i--;
 								continue;
 							}
 						}
 					}
 					data.add(feature_vector);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return data.folds(data.size()/numRecords, rand)[0]; 
 	}
 
 	private static Map<Integer, Integer> getMask(String maskFile){
 		if(!masks.containsKey(maskFile)){
 			BufferedReader r;
 			int featureCount = 0;
 			try {
 				Map<Integer, Integer> mask = new HashMap<Integer, Integer>();
 				r = new BufferedReader(new FileReader(maskFile));
 				String[] maskString = r.readLine().replace(" ","").split(",");
 				for( String s : maskString){
 					mask.put(Integer.parseInt(s), featureCount);
 					featureCount++;
 				}
 				masks.put(maskFile, mask);		
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return masks.get(maskFile);
 		
 	}
 
 	public static String getMaskFile(DataType type) {
 		switch(type){
 			case FULL:	return null;
 			case ONE:	return "forward@0.01_5000mixed.data";
 			case FIVE:	return "forward@0.05_5000mixed.data"; 
 			case TEN:	return "forward@0.1_5000mixed.data"; 
 			default:	return "forward@0.01_5000mixed.data";
 		}
 	}
 }

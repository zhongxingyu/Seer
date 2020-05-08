 package com.aic.keywordanalysis;
 
 import java.io.*;
 import java.util.*;
 
//import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
 
 /**
  * InvertedIndex represents an inverted index.
  * 
  * It contains methods for creating, storing/loading, and using an inverted
  * index.
  */
 public class AIC_InvertedIndex {
 	public LinkedHashMap<String, Integer> sortHashMapByValuesD(
 			HashMap passedMap) {
 		List mapKeys = new ArrayList(passedMap.keySet());
 		List mapValues = new ArrayList(passedMap.values());
 		Collections.sort(mapValues);
 		Collections.sort(mapKeys);
 		Collections.reverse(mapValues);
 		Collections.reverse(mapKeys);
 		LinkedHashMap sortedMap = new LinkedHashMap();
 
 		Iterator valueIt = mapValues.iterator();
 		while (valueIt.hasNext()) {
 			Object val = valueIt.next();
 			Iterator keyIt = mapKeys.iterator();
 
 			while (keyIt.hasNext()) {
 				Object key = keyIt.next();
 				String comp1 = passedMap.get(key).toString();
 				String comp2 = val.toString();
 
 				if (comp1.equals(comp2)) {
 					passedMap.remove(key);
 					mapKeys.remove(key);
 					sortedMap.put((String) key, (Integer) val);
 					break;
 				}
 
 			}
 
 		}
 		return sortedMap;
 	}
 
 	public static void main(String args[]) throws IOException {
 		File folder = new File(
 				"C:\\Users\\lokesh\\Desktop\\AIC\\Project\\run2\\");
		DBUtils db=new DBUtils();
		//db.refreshDBSuggestions();
 		for (final File fileEntry : folder.listFiles()) {
 			if (!fileEntry.isDirectory()
 					&& fileEntry.getAbsolutePath().contains("ama.json")) {
 			    //if(fileEntry.getAbsolutePath().contains("oco.json") || fileEntry.getAbsolutePath().contains("ama.json"))
                   //  continue;
                 
 				String screenName = fileEntry.getName();
 				screenName = screenName.replace(".json", "");
 				Long cid= db.getCelebId(screenName);
 				System.out.println("Starting for --"+ cid+" --- "+
 						screenName);
 				if(cid==-1){
 					continue;
 				}
 				
 				JSONObject jsonObject;
 				String obj;
 				HashMap<String, Integer> Wordcount = new HashMap<String, Integer>();
 				int count = 0;
 				BufferedReader buf = new BufferedReader(new FileReader(
 						fileEntry.getAbsolutePath()));
 				while((obj=buf.readLine())!=null) {
 					jsonObject = (JSONObject)JSONValue.parse(obj);
 					if(jsonObject==null)continue;
 					String text = (String) jsonObject.get("text");
					// System.out.println(text);
 					String tokens[] = text.split(" ");
 					for (int i = 0; i < tokens.length; i++) {
 						if (!Wordcount.containsKey(tokens[i])) {
 							Wordcount.put(tokens[i], 1);
 						}
 						if (Wordcount.containsKey(tokens[i])) {
 							count = Wordcount.get(tokens[i]);
 							Wordcount.put(tokens[i], count + 1);
 						}
 					}
 
 				} ;
 				AIC_InvertedIndex ai = new AIC_InvertedIndex();
 				Wordcount = ai.sortHashMapByValuesD(Wordcount);
 				for (String key : Wordcount.keySet()) {
				//	System.out.println("" + key + ":" + Wordcount.get(key));
 					JSONObject sugJson = new JSONObject();
 					sugJson.put("ISACTIVE", true);   // change logic here for useless keywords ***********
 					sugJson.put("FREQ", Wordcount.get(key));
 					sugJson.put(("KEYWORD"),key);
 					sugJson.put("CID",cid);
 					db.createSuggestion(sugJson);
 				}
 		          System.out.print("Ending for --"+fileEntry.getName());
 
 			}
 		}
 		db.closeConnection();
 	}
 }

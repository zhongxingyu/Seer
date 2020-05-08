 package edu.mit.cci.wikipedia.articlenetwork;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class CreateNetworkFromAllData {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter("Data/Es/Category_LivingPeople_links_20110214.txt"));
 			BufferedReader br = new BufferedReader(new FileReader("Data/Es/Category_LivingPeople.txt"));
 			Map<String,String> peopleMap = new TreeMap<String,String>();
 			//Map<String,Map<String,Integer>> network = new HashMap<String,Map<String,Integer>>();
 			String line = "";
 			// Loading all id and name in LivingPeople categories
 			while ((line = br.readLine()) != null) {
 				String[] arr = line.split("\t");
 				peopleMap.put(arr[1], arr[0]);
 			}
 			System.out.println(peopleMap.size());
 			br = new BufferedReader(new FileReader("Data/Es/Category_LivingPeople_links_all_20110214-1.txt"));
 			Map<String,Integer> linkMap = new HashMap<String,Integer>();
 			String prevPersonName = "";
 			String personName = "";
 			while ((line = br.readLine()) != null) {
 				if (line.split("\t").length < 2)
 					continue;
 				personName = line.split("\t")[0];
 				//System.out.println(personName + "\t" + prevPersonName);
 				String linkName = line.split("\t")[1];
 				if (!prevPersonName.equals(personName) && prevPersonName.length() != 0) {
 					Iterator<String> itNode = linkMap.keySet().iterator();
 					while (itNode.hasNext()) {
 						String node = itNode.next();
 						int tie = linkMap.get(node);
						bw.write(personName + "\t" + node + "\t" + tie);
 						bw.newLine();
 						bw.flush();
 					}
 					linkMap = new HashMap<String,Integer>();
 					
 				}
 				if (peopleMap.containsKey(linkName)) {
 					//String linkName = peopleMap.get(linkId);
 					if (!personName.equals(linkName)) {
 						//Map<String,Integer> links = network.get(personName);
 						if (linkMap.containsKey(linkName)) {
 							int v = linkMap.get(linkName);
 							v++;
 							linkMap.put(linkName,v);
 						} else {
 							linkMap.put(linkName, 1);
 						}
 							//network.put(personName, links);
 					}
 				}
 				prevPersonName = personName;
 			}
 			Iterator<String> itNode = linkMap.keySet().iterator();
 			while (itNode.hasNext()) {
 				String node = itNode.next();
 				int tie = linkMap.get(node);
 				bw.write(personName + "\t" + node + "\t" + tie);
 				bw.newLine();
 				bw.flush();
 			}
 			bw.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }

 package org.siraya.dconfig;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 import java.util.*;
 
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * 
  * 
  * 
  * @author angus_chen
  * 
  */
 public class RootNode extends Node {
 	private static Logger logger = Logger.getLogger(RootNode.class.getName());
 	private Yaml yaml = new Yaml();
 	private int minBranchLevel = 0;
 	private Dimensions dimensions;
 
 
 	/**
 	 * load RootNode by directory
 	 * @param path
 	 * @throws IOException
 	 */
 	public RootNode(File path) throws IOException {
 
 		if (!path.isDirectory()) {
 			throw new NodeException("path is not directory");
 		}
 
 		File[] files = path.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().endsWith(".yaml");
 			}
 		});
 
 		dimensions = null;
 		//
 		// load dimension
 		//
 		for (File file : files) {
 			if (file.getName().equals("dimensions.yaml")) {
 				dimensions = new Dimensions(new java.io.FileInputStream(file));
 			}
 
 		}
 		if (dimensions == null) {
 			throw new NodeException("dimensions.yaml not found");
 		}
 		Yaml yaml = new Yaml();
 		Map<Branch, Map<String, Object>> map =new HashMap<Branch, Map<String, Object>>();
 		//
 		//
 		//
 		for (File file : files) {
 			if (file.getName().equals("dimensions.yaml")) {
 				continue;
 			}
 			
 			logger.info("parsing yaml file and get root node");
 			Object root = yaml.load(new java.io.FileInputStream(file));
 			if (!(root instanceof List)) {
 				throw new NodeException("config must start with list object");
 			}
 			
 			for (Map<String, Object> setting : (List<Map<String, Object>>) root) {
 				Branch currentBranch = RootNode.getCurrentBranch(dimensions, setting);
 				map.put(currentBranch, setting);
 			}
 		}
 		
 		Map<Branch, Map<String, Object>> result = this.sortByKeys(map);
 		for (Branch currentBranch: result.keySet()) {
 			logger.info("start parsing branch "+currentBranch.getId());
 			Map<String, Object> setting = map.get(currentBranch);
 			this.parse(currentBranch, setting);
 		}
 	}
     
 	/*
 	 * from 
 	 * http://javarevisited.blogspot.tw/2012/12/how-to-sort-hashmap-java-by-key-and-value.html
 	 * 
      * Paramterized method to sort Map e.g. HashMap or Hashtable in Java
      * throw NullPointerException if Map contains null key
      */
 	public static <K extends Comparable,V> Map<K,V> sortByKeys(Map<K,V> map){
         List<K> keys = new LinkedList<K>(map.keySet());
         Collections.sort(keys);
       
         //LinkedHashMap will keep the keys in the order they are inserted
         //which is currently sorted on natural ordering
         Map<K,V> sortedMap = new LinkedHashMap<K,V>();
         for(K key: keys){
             sortedMap.put(key, map.get(key));
         }
       
         return sortedMap;
     }	
 	
 	/**
 	 * construct of root node.
 	 * 
 	 * @param input
 	 * @param dimensions
 	 */
 	public RootNode(InputStream input, Dimensions dimensions) {
 		this.dimensions = dimensions;
 		this.load(input);
 	}
 	
 	public void load(InputStream input) {
 		logger.info("parsing yaml file and get root node");
 		Object root = yaml.load(input);
 		if (!(root instanceof List)) {
 			throw new NodeException("config must start with list object");
 		}
 		//
 		// loop settings
 		//
 		for (Map<String, Object> setting : (List<Map<String, Object>>) root) {
 			Branch currentBranch = getCurrentBranch(dimensions, setting);
 			this.parse(currentBranch, setting);
 		}
 	}
 		
 	/**
 	 * get current branch in setting
 	 * @param dimensions
 	 * @param setting
 	 * @return
 	 */
 	public static Branch getCurrentBranch(Dimensions dimensions, Map<String, Object> setting) {
 		String branch = (String) setting.get("settings");
 		Branch currentBranch = null;
 		if ("master".equals(branch)) {
 			//
 			// init root node
 			//
 			currentBranch = Branch.MASTER;
 
 		} else {
 			logger.info("load branch " + branch);
 			String[] tmp = branch.split("=");
			if (tmp.length != 2) {
                throw new NodeException(branch + " format error ");
            }
            Map<String,Branch> map = dimensions.getBranchMap(tmp[0]);
			if (map == null) {
                throw new NodeException("key "+ tmp[0] + " not exist ");
            }
			currentBranch = map.get(tmp[1]);
 			if (currentBranch == null) {
 				throw new NodeException("branch " + branch + " not exist");
 			}
 		}
 		return currentBranch;
 	}
 	
 	public void parse(Branch currentBranch, Map<String, Object> setting) {
 		//
 		// loading sequence.
 		//
 		if (currentBranch.getBranchLevel() < this.minBranchLevel) {
 			throw new NodeException("can't load branch level " + currentBranch.getId() +
 					" min level is "+ this.minBranchLevel + 
 					" current level is "+ currentBranch.getBranchLevel());
 		} else {
 			logger.info("set min level for " + currentBranch.getId());
 			this.minBranchLevel = currentBranch.getBranchLevel();
 		}
 		//
 		// init master node.
 		//
 		Node rootNode = this;
 		setting.remove("settings");
 		Set<String> keys = ((Map<String, Object>) setting).keySet();
 		for (String ikey : keys) {
 			initNodeTree(ikey, setting.get(ikey), rootNode, currentBranch);
 		}
 	}
 	
 	private void initNodeTree(String key, Object obj, Node parent,
 			Branch currentBranch) {
 		Node currentNode = parent.addChildNode(currentBranch, key, null);
 		if (obj instanceof Map) {
 			Map<String, Object> map = (Map<String, Object>) obj;
 			Set<String> keys = ((Map<String, Object>) obj).keySet();
 			for (String ikey : keys) {
 				Object value = map.get(ikey);
 				this.initNodeTree(ikey, value, currentNode, currentBranch);
 			}
 
 		} else {
 			currentNode.setValue(currentBranch, obj);
 		}
 	}
 	
 	public Dimensions getDimensions() {
 		return dimensions;
 	}
 
 }

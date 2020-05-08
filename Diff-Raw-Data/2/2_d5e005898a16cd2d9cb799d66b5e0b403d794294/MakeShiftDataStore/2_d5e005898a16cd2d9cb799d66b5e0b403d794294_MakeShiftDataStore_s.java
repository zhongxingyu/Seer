 package com.danlangford.selectoserve;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 public class MakeShiftDataStore {
 
 	private static MakeShiftDataStore instance = new MakeShiftDataStore();
 
 	public static MakeShiftDataStore getInstance() {
 		if (instance == null) {
 			instance = new MakeShiftDataStore();
 		}
 		return instance;
 	}
 
 	private HashMap<String, ServingComp> servers = new HashMap<String, ServingComp>();
 
 	private MakeShiftDataStore() {
 
 	}
 
 	public void addServer(ServingComp server) {
 		// check any input if you needs to
 		servers.put(server.getIp(), server);
 	}
 
 	public List<ServingComp> getServers() {
 		List<ServingComp> list = new ArrayList<ServingComp>(servers.values());
 		
 		Collections.sort(list, new Comparator<ServingComp>() {
 			@Override
 			public int compare(ServingComp o1, ServingComp o2) {
				return o1.getTime().compareTo(o2.getTime());
 			}
 		});
 
 		return list;
 	}
 
 }

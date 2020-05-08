 package com.felthat.nimble.rest;
 
 import java.util.Map;
 
 import com.felthat.nimble.graph.Graph;
 
 public class NimbleRequestMapper {
 
 	private GraphFactory graphFactory = new GraphFactory();//TODO inject this
 
 	public Graph map(NimbleRequest request) {
		Graph graph = graphFactory.create();
 		Map<String, Object> data = request.getData();
 		for(String key : data.keySet()){
 			Object object = data.get(key);
 			graph.put(key,object);
 		}
 		return graph;
 	}
 
 }

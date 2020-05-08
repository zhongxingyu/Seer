 package com.ghostrun.driving;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
import java.util.Random;
 import java.util.Set;
 
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.ghostrun.driving.impl.RouteImpl;
 import com.ghostrun.util.RandUtils;
 import com.google.android.maps.GeoPoint;
 
 public class NodeFactory {
 	public class NodesAndRoutes {
 		public List<Node> nodes;
 		public Map<NodePair, Route> routesMap;
 		public NodesAndRoutes(List<Node> nodes, Map<NodePair, Route> routesMap) {
 			this.nodes = nodes;
 			this.routesMap = routesMap;
 		}
 		
 		public List<Node> toNodes() {
 			Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
 			List<Node> result = new ArrayList<Node>();
 			
 			int max_id = 0;
 			for (Node n : nodes) {
 				nodeMap.put(n.id, n);
 				max_id = Math.max(n.id, max_id);
 			}
 			for (Map.Entry<NodePair, Route> entry : routesMap.entrySet()) {
 				NodePair p = entry.getKey();
 				Route r = entry.getValue();
 				
 				Node node1 = nodeMap.get(p.id1);
 				Node node2 = nodeMap.get(p.id2);
 				
 				Node lastNode = node1;
 				List<GeoPoint> lst = ((RouteImpl)r).getGeoPoints();
 				
 				/*
 				if (!lst.get(0).equals(node1.latlng) || !lst.get(lst.size()-1).equals(node2.latlng))
 					System.out.println("endpoints don't match...");
 				*/
 				
 				if (lst.get(0).equals(node1.latlng)) {
 					lst.remove(0);
 				}
 				if (lst.get(lst.size()-1).equals(node2.latlng)) {
 					lst.remove(lst.size()-1);
 				}
 				
 				if (lst.size() > 0) {
 					node1.removeNeighbor(node2);
 					node2.removeNeighbor(node1);
 				}
 				
 				if (!result.contains(node1)) {
 					result.add(node1);
 				}	
 				if (!result.contains(node2)) {
 					result.add(node2);
 				}
 				
 				for (GeoPoint pt : lst) {
 					// combine pts into nodes
 					Node node = new Node(pt, max_id++);
 					lastNode.addNeighbor(node);
 					node.addNeighbor(lastNode);
 					lastNode = node;
 					
 					result.add(node);
 				}
 				lastNode.addNeighbor(node2);
 				node2.addNeighbor(lastNode);
 			}
 			return result;
 		}
 	}
 	public GeoPoint getGeoPointFromMap(Map<String, Object> map) {
 		return new GeoPoint(((Long)map.get("lat")).intValue(), 
 				((Long)map.get("lng")).intValue());
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static List<Node> fromStaticMap(String serialized) {
 		
 		List<Node> results = new ArrayList<Node>();
 		JSONParser parser = new JSONParser();
 		ContainerFactory containerFactory = new ContainerFactory(){
 		    public List<Map<String, Object>> creatArrayContainer() {
 		      return new ArrayList<Map<String, Object>>();
 		    }
 
 		    public Map<String, Object> createObjectContainer() {
 		      return new HashMap<String, Object>();
 		    }
 		                        
 		  };
 		Map<String, Object> map = null;
 		try {
 			map = (Map<String, Object>)parser.parse(serialized, containerFactory);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
 		Collection<Object> values = map.values();
 		for (Object val : values) {
 			Map<String, Object> tmp = (HashMap<String, Object>)val;
 			//System.out.println("ERROR CAST: " + (Double)tmp.get("lat"));
 			int id = ((Long)tmp.get("id")).intValue();
 			//System.out.println("id: " + id);
 			Node n = new Node(new GeoPoint(((Long)tmp.get("lat")).intValue(), 
 					((Long)tmp.get("lng")).intValue()), id);
 			//System.out.println(n.latlng);
 			nodeMap.put(id, n);
 			
 			results.add(n);
 		}
 		
 		for (Object val : values) {
 			Map<String, Object> tmp = (HashMap<String, Object>)val;
 			Node n = nodeMap.get(((Long) tmp.get("id")).intValue());
 			for (Object neighborId : (List<Integer>)tmp.get("neighbors")) {
 				Node tmpNode = nodeMap.get(((Long)neighborId).intValue());
 				n.addNeighbor(tmpNode);
 				tmpNode.addNeighbor(n);
 			}
 		}
 		return results;
 	}
 	
 	public static List<Node> generateRandomMap(String serialized) {
 		List<Node> nodes = fromStaticMap(serialized);
 		List<Node> results = new ArrayList<Node>();
 		Set<Node> doneNodes = new HashSet<Node>();
 		final double randomTh = 0.3;
 		
 		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
 		for (Node n: nodes) {
 			nodeMap.put(n.id, n);
 		}
 		
 		Queue<Node> queue = new LinkedList<Node>();
 		Node n = nodes.get(RandUtils.nextInt(nodes.size())).clone();
 		queue.offer(n);
 		doneNodes.add(n);
 		
 		System.out.println("Picked node: " + n.id);
 		
 		/*
 		int rand = 100;
 		while (rand > 0) {
 			Node n1 = nodes.get(RandUtils.nextInt(nodes.size())).clone();
 			if (!results.contains(n1)) {
 				results.add(n1);
 			}
 			rand --;
 		}
 		*/
 		
 		while (queue.size() > 0) {
 			Node curNode = queue.poll();
 			Node newNode = curNode;
 			
 			results.add(newNode);
 			System.out.println("number of neighbors: " + n.neighbors.size());
 			
 			for (Node neighbor : nodeMap.get(newNode.id).neighbors) {
 				if (!doneNodes.contains(neighbor)) {
 					if (results.size() < 20 || (results.size() < 50 && RandUtils.nextDouble() < randomTh)) {
 						System.out.println("adding neighbor: " + results.size() + " queue size: " + queue.size());
 						Node n1 = neighbor.clone();
 						queue.offer(n1);
 						doneNodes.add(n1);
 						newNode.addNeighbor(n1);
 						n1.addNeighbor(newNode);
 					}
 				}
 			}
 		}
 		
 		/*
 		if (results.size() < 15) {
 			return generateRandomMap(serialized);
 		}
 		*/
 		return results;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public NodesAndRoutes fromMap(String serialized) {
 		JSONParser parser = new JSONParser();
 		ContainerFactory containerFactory = new ContainerFactory(){
 		    public List<Map<String, Object>> creatArrayContainer() {
 		      return new ArrayList<Map<String, Object>>();
 		    }
 
 		    public Map<String, Object> createObjectContainer() {
 		      return new HashMap<String, Object>();
 		    }
 		                        
 		  };
 		Map<String, Object> map = null;
 		try {
 			map = (Map<String, Object>)parser.parse(serialized, containerFactory);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		List<Node> nodes = new ArrayList<Node>();
 		Map<NodePair, Route> routesMap = new HashMap<NodePair, Route>();
 		
 		List<Map<String, Object>> nodesList = 
 			(List<Map<String, Object>>) map.get("nodes");
 		System.out.println("nodes: " + nodesList.size());
 		Object[] jsonNodes = (Object[]) (nodesList.toArray());
 		Map<Integer, Node> nodesMap = new HashMap<Integer, Node>();
 		for (int i = 0; i < jsonNodes.length; i++) {
 			Map<String, Object> m = (Map<String, Object>)jsonNodes[i];
 			int id = ((Long) m.get("id")).intValue();
 			Node n = new Node(getGeoPointFromMap(m), id);
 			nodes.add(n);
 			nodesMap.put(id, n);
 		}
 		
 		for (int i = 0; i < jsonNodes.length; i++) {
 			Node n = nodes.get(i);
 			Object[] neighbor = (Object[]) ((List<Map<String, Object>>)
 					((Map<String, Object>) jsonNodes[i]).get("neighbors")).toArray();
 			for (int j = 0; j < neighbor.length; j++) {
 				n.addNeighbor(nodesMap.get(((Long)neighbor[j]).intValue()));
 			}
 		}
 		
 		Map<Object, Object> jsonRoutes = (Map<Object, Object>) map.get("routes");
 		Set<Entry<Object,Object>> entries = jsonRoutes.entrySet();
 		for (Entry<Object, Object> entry : entries) {
 			String key = (String) entry.getKey();
 			int i = key.indexOf(" ");
 			int id1 = new Integer(key.substring(0, i));
 			int id2 = new Integer(key.substring(i+1));
 			
 			routesMap.put(new NodePair(id1, id2), 
 					new RouteImpl((Map<Object, Object>) entry.getValue()));
 		}
 		return new NodesAndRoutes(nodes, routesMap);
 	}
 }

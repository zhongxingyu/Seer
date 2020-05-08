 package org.misera.android.cavenav.graph;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class Graph {
 	
 	public HashMap<Integer,Edge> edges;
 	public HashMap<Integer,Vertex> vertices;
 
 	public Graph(String str) {
 		try {
 			JSONObject json = new JSONObject(str);
 			this.vertices = readVertices(json);
 			this.edges = readEdges(json);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public Edge findEdge(Vertex v1, Vertex v2) {
 		for (Integer key : edges.keySet()) {
 			Edge e = edges.get(key);
 			if ((e.startVertex == v1 || e.startVertex == v2) && (e.endVertex == v1 || e.endVertex == v2)) {
 				return e;
 			}
 		}
 		Log.e("CaveNav", String.format("No edge found with %s %s", v1, v2));
 		return null;
 	}
 	
 	public Vertex nearestVertex(int posX, int posY) {
 		Vertex out = null;
 		double distOut = Integer.MAX_VALUE;
 		for (Integer key : vertices.keySet()) {
 			Vertex v = vertices.get(key);
 			if (out == null) {
 				out = v;
 				distOut = distance(posX, posY, out);
 			} else {
 				double distV = distance(posX, posY, v);
 				if (distV < distOut) {
 					out = v;
 					distOut = distV;
 				}
 			}
 		}
 		if (distOut < 50) {
 			return out;
 		} else {
 			return null;
 		}
 	}
 	
 	public static double getDistance(Vertex v1, Vertex v2) {
 		int a = v1.x - v2.x;
 		int b = v1.y - v2.y;
 		return Math.sqrt(a*a + b*b);
 	}
 
 	private double distance(int posX, int posY, Vertex v) {
 		int a = v.x - posX;
 		int b = v.y - posY;
 		return Math.sqrt(a*a + b*b);
 	}
 
 	private HashMap<Integer, Vertex> readVertices(JSONObject json) throws JSONException {
 		HashMap<Integer,Vertex> vertices = new HashMap<Integer,Vertex>();
 		JSONObject nodes = json.getJSONObject("nodes");
 		Iterator<String> nodeIds = nodes.keys();
 		while (nodeIds.hasNext()) {
 			String id = nodeIds.next();
 			JSONObject node = nodes.getJSONObject(id);
 			JSONObject coords = node.getJSONObject("coordinates");
 			int x = coords.getInt("x") - 7;
 			int y = coords.getInt("y") - 52;
 			Vertex newVertex = new Vertex(x, y);
 			vertices.put(Integer.decode(id), newVertex);
 		}
 		return vertices;
 	}
 	
     private HashMap<Integer, Edge> readEdges(JSONObject json) throws JSONException {
     	HashMap<Integer,Edge> edges = new HashMap<Integer, Edge>();
 		JSONObject edgesJson = json.getJSONObject("edges");
 		Iterator<String> edgeIds = edgesJson.keys();
 		while (edgeIds.hasNext()) {
 			String edgeId = edgeIds.next();
 			JSONObject endpoints = edgesJson.getJSONObject(edgeId).getJSONObject("nodes");
 			Integer startVertexId = new Integer(endpoints.getInt("start"));
 			Integer endVertexId = new Integer(endpoints.getInt("end"));
 			
 			if (vertices.containsKey(startVertexId) && vertices.containsKey(endVertexId)) {
 				Vertex startVertex = vertices.get(startVertexId);
 				Vertex endVertex = vertices.get(endVertexId);
 				Edge newEdge = new Edge(startVertex, endVertex);
 				startVertex.addEdge(newEdge);
 				endVertex.addEdge(newEdge);
 				Integer id = Integer.decode(edgeId);
 				edges.put(id, newEdge);
 			} else {
 				// there seem to be non-existing vertex id referenced in the json
 		    	Log.e("CaveNav", String.format("%s. vertex ids: (%d, %d)", edgeId, startVertexId, endVertexId));
 			}
 		}
 		return edges;
 	}
 
 }

 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * This class is used to randomly generate several query patterns
  * @author Administrator
  *
  */
 public class QueryGenerator {
 	Graph g = new Graph();
 	int sizeofComponent = 0;
 	int lengthofSuperEdge = 0;
 	
 	public QueryGenerator(String fileName, int s, int l) throws FileNotFoundException {
 		g.loadGraphFromFile(fileName);
 		sizeofComponent = s;
 		lengthofSuperEdge = l;		
 	}
 	
 	public static void main(String[] args) throws IOException {
		QueryGenerator test = new QueryGenerator("Amazon",5,2);
 		test.generate();
 	}
 	
 	public void generate() throws IOException {
 		//Pick up the first edge
 		Integer[] start = new Integer[g.children.keySet().size()];
 		g.children.keySet().toArray(start);
 		Random r = new Random();
 		
 		Integer startVertex = null;
 		
 		while (true) {
 			int p = r.nextInt(start.length);
 		
 			startVertex = start[p];
 			
 			if (g.children.containsKey(startVertex) && g.children.get(startVertex).size() > 0) {
 				break;
 			}
 		}
 		
 		
 		List<Edge> firstComponent = generateComponent(startVertex);
 		
 		Integer SEsource = startVertex;
 		//Super-edge generation
 		for (int i = 0; i < lengthofSuperEdge; i++) {
 			List<Integer> schild = g.children.get(SEsource);
 			Random x = new Random();
 			int pc = x.nextInt(schild.size());
 			SEsource = schild.get(pc);
 		}
 		
 		System.out.println(startVertex + "-->" + SEsource);
 		List<Edge> secondComponent = generateComponent(SEsource);
 		
 		firstComponent.addAll(secondComponent);
 
 		HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
 		
 		HashSet<Integer> vertices = new HashSet<Integer>();
 		
 		for (Edge e : firstComponent) {
 			if (!children.containsKey(e.source))
 				children.put(e.source, new LinkedList<Integer>());
 			children.get(e.source).add(e.target);
 			
 			if (!vertices.contains(e.source))
 				vertices.add(e.source);
 			if (!vertices.contains(e.target))
 				vertices.add(e.target);
 		}
 
 		FileWriter fstream = new FileWriter("query");
 		BufferedWriter out = new BufferedWriter(fstream);
 		for (Integer i : vertices) {
 			String k = String.valueOf(i);
 			System.out.println(k);
 			out.write(k);
 			List<String> as = g.attributes.get(i);
 			for (String s: as)
 				out.write("," + s);
 			out.write("\r\n");
 			
 			String temp = "";
 			if (children.containsKey(i)) {
 				for (Integer j : children.get(i)) {
 					temp += j + ","  + "1,";
 				}
 				//Remove the last comma
 				temp = temp.substring(0, temp.length() - 1);
 			}
 			else
 				temp = "-1";
 
 			if (i.equals(startVertex)) {
 				temp += "," + SEsource + "," + lengthofSuperEdge;
 			}
 			
 			out.write(temp + "\r\n");
 		}
 		out.close();		
 	}
 		
 	/**
 	 * Generate the query pattern
 	 * @param n the number of query patterns
 	 */
 	public List<Edge> generateComponent(Integer startVertex) {
 		
 		List<Integer> children = g.children.get(startVertex);
 		Random s = new Random();
 		int q = s.nextInt(children.size());
 		
 		Integer endVertex = children.get(q);
 		
 		List<Edge> edgelist = new LinkedList<Edge>();
 		
 		
 		Edge temp = new Edge(startVertex, endVertex);
 		edgelist.add(temp);
 		
 		List<Edge> edgepool = new LinkedList<Edge>();
 		
 		//Add all the parents and children in
 		for (Integer i : g.children.get(endVertex)) {
 			Edge t = new Edge(endVertex, i);
 			if (!edgepool.contains(t)) {
 				edgepool.add(t);
 			}
 		}
 		
 		for (Integer i : g.children.get(startVertex)) {
 			Edge t = new Edge(startVertex, i);
 			if (!edgepool.contains(t)) {
 				edgepool.add(t);
 			}			
 		}
 		if (g.parents.containsKey(endVertex)) {
 			for (Integer i : g.parents.get(endVertex)) {
 				Edge t = new Edge(i, endVertex);
 				if (!edgepool.contains(t)) {
 					edgepool.add(t);
 				}			
 			}
 		}
 		if (g.parents.containsKey(startVertex)) {
 			for (Integer i : g.parents.get(startVertex)) {
 				Edge t = new Edge(i, startVertex);
 				if (!edgepool.contains(t)) {
 					edgepool.add(t);
 				}						
 			}
 		}
 		
 		int edgeCount = 1;
 		
 		while (edgeCount < sizeofComponent) {
 			//Pick up one edge from edge pool
 			Random rg = new Random();
 			int pos = rg.nextInt(edgepool.size());
 			Edge choosen = edgepool.get(pos);
 			edgelist.add(choosen);
 			edgepool.remove(pos);
 			
 			//Add all the parents and children in
 			for (Integer i : g.children.get(choosen.target)) {
 				Edge t = new Edge(choosen.target, i);
 				if (!edgepool.contains(t)) {
 					edgepool.add(t);
 				}
 			}
 			
 			for (Integer i : g.children.get(choosen.source)) {
 				Edge t = new Edge(choosen.source, i);
 				if (!edgepool.contains(t)) {
 					edgepool.add(t);
 				}			
 			}
 			
 			if (g.parents.containsKey(choosen.target)) {
 				for (Integer i : g.parents.get(choosen.target)) {
 					Edge t = new Edge(i, choosen.target);
 					if (!edgepool.contains(t)) {
 						edgepool.add(t);
 					}			
 				}
 			}
 			
 			if (g.parents.containsKey(choosen.source)) {
 				for (Integer i : g.parents.get(choosen.source)) {
 					Edge t = new Edge(i, choosen.source);
 					if (!edgepool.contains(t)) {
 						edgepool.add(t);
 					}						
 				}
 			}
 			edgeCount++;
 		}
 		return edgelist;		
 	}
 }

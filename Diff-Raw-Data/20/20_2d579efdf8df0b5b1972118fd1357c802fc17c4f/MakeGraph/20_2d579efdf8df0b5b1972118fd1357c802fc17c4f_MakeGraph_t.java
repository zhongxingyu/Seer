 package tech.com; 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class MakeGraph {
 	
 	static String file = "titlecycle";
 
 	/*
 	 * See Graph_Format.txt
 	 */
 	
 	public static void main(String args[]) throws IOException {
 		Map<Integer,String> lines = importLines();
 		System.out.println("loaded");
 		Map<Integer,Set<Overlap>> overlaps = Overlap.overlap(lines);
 		System.out.println("overlaps");
 		
 		System.out.println(longestPathDA(overlaps));
 		//List<Integer> useless = foundUseless(overlaps,flip(overlaps));
 		
 		//Map<Integer,Set<Overlap>> pruned = prune(overlaps,useless);
 		/*
 		System.out.println(foundUseless(pruned,flip(pruned)));
 		
 		String root = new File("").getAbsolutePath();
 		root = root.substring(0, root.lastIndexOf(File.separator));
 		File f = new File(root + File.separator + "c" + File.separator + file);
 		System.out.println(f.getAbsoluteFile());
 		f.createNewFile();
 		OutputStream o = new FileOutputStream(f);
 		writeGraph(overlaps,o);
 		o.close();
 		
 		System.out.println(overlaps);
 		*/
 		//System.out.println(computeLength(overlaps,longestPathDA(overlaps)));
 		
 		//System.out.println(overlaps);
 		//Map<Integer,Set<Overlap>> co = contract(overlaps);
 		//System.out.println("contracted");
 		//System.out.println(co);
 		//Set<List<Integer>> o = cycles(overlaps);
 		//Set<List<Integer>> o2 = cycles(co);
 
 		//System.out.println(o);
 		//System.out.println(contract(removeUseless(overlaps)));	
 	}
 	
 	public static List<Integer> longestPathDA(Map<Integer,Set<Overlap>> g) {
 		List<Integer> bestPath = null;
 		int pathLength = 0;
 		
 		List<List<Integer>> cycles = new ArrayList<List<Integer>>(cycles(g));
 		
 		int[] cuts = new int[cycles.size()];
 		Overlap[] removedEdges = new Overlap[cycles.size()];
 		
		search : while(true) {
 			
 			for(int i=0;i<cuts.length;i++) {
 				int u = cycles.get(i).get(cuts[i]);
 				int v = cycles.get(i).get((cuts[i]+1) % cycles.get(i).size());
 				List<Overlap> edges = new ArrayList<Overlap>(g.get(u));
 				Overlap o = edges.remove(edges.indexOf(new Overlap(0,v)));
 				g.get(u).remove(o);	
 				removedEdges[i] = o;
 			}
 			System.out.println(g);
 			
 			List<Integer> newPath = longestPathDAG(g);
 			int newLen = computeLength(g,newPath);
 			
 			if(newLen >= pathLength) {
 				newLen = pathLength;
 				bestPath = newPath;
 			}
 			
 			for(int i=0;i<cuts.length;i++) {
 				g.get(cycles.get(i).get(cuts[i])).add(removedEdges[i]);
 			}
 			
			int carryIndex = 0;
			cuts[carryIndex]++;
			while(cuts[carryIndex] == cycles.get(carryIndex).size()) {
				cuts[carryIndex] = 0;
				carryIndex++;
				if(carryIndex == cycles.size())
					break search ;
				cuts[carryIndex]++;
			
 			}
 		}
 		
 		return bestPath;	
 		
 	}
 	
 	private static int computeLength(Map<Integer, Set<Overlap>> g, List<Integer> newPath) {
 		int len = 0;
 		int curIndex = 0;
 		
 		while(curIndex < newPath.size()-1) {	
 			List<Overlap> os = new ArrayList<Overlap>(g.get(newPath.get(curIndex)));
 			Overlap o = os.get(os.indexOf(new Overlap(0,newPath.get(curIndex+1))));
 			len += o.len;		
 			curIndex++;
 		}
 		return len;
 	}
 
 	public static void writeGraph(Map<Integer,Set<Overlap>> g, OutputStream out) throws IOException {
 		
 		out.write(intToTwoBytes(g.size()));
 		
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet()) {
 			out.write(intToTwoBytes(e.getKey()));
 			out.write(intToTwoBytes(e.getValue().size()));
 			for(Overlap o : e.getValue()) {
 				out.write(intToTwoBytes(o.suf));
 				out.write(o.len & 0xff);
 				out.write(0);
 			}
 		}
 		out.flush();		
 	}
 	
 	public static byte[] intToTwoBytes(int i) {
 		byte[] res = new byte[2];
 		res[0] = (byte)i;
 		res[1] = (byte)(i>>8);
 		
 		return res;
 	}
 	
 	public static Map<Integer,Set<Overlap>> prune(Map<Integer,Set<Overlap>> g, List<Integer> u) {
 		Map<Integer,Set<Overlap>> newG = new HashMap<Integer,Set<Overlap>>();
 		
 		Set<Integer> useless = new HashSet<Integer>(u);
 		
 		for(Entry<Integer,Set<Overlap>> ent : g.entrySet()) {
 			if(useless.contains(ent))
 				continue;
 			
 			List<Overlap> os = new LinkedList<Overlap>(ent.getValue());
 			Set<Overlap> newOs = new HashSet<Overlap>();
 			
 			for(Overlap o : os)
 				if(!useless.contains(o.suf))
 					newOs.add(new Overlap(o));
 					
 			newG.put(ent.getKey(), newOs);				
 		}		
 		
 		return newG;
 	}
 	
 	public static List<Integer> longestPathDAG(Map<Integer,Set<Overlap>> g) {
 		List<Integer> l = new LinkedList<Integer>();
 		Map<Integer,Set<Integer>> flipped = flip(g);
 		
 		
 		Integer[] topSort = topSort(g,flipped);
 		System.out.println(Arrays.toString(topSort));
 		
 		int[] weights = new int[g.size()];
 		int[] parent = new int[g.size()];
 		
 		for(int i=0;i<parent.length-1;i++)
 			parent[i] = parent.length-1;
 		parent[parent.length - 1] = -2;
 		
 		for(Integer v : topSort) {
 			System.out.println("VVVVVVVVVVVVVVVVVVVV");
 			System.out.println("Current v is "  + v);
 
 			for(Overlap o : g.get(v)) 
 				if(weights[o.suf] <= weights[v] + o.len) {
 					weights[o.suf] = weights[v] + o.len;
 					parent[o.suf] = v;
 				}
 			System.out.println(Arrays.toString(parent));
 			System.out.println(Arrays.toString(weights));
 			System.out.println("^^^^^^^^^^^^^^^^^^^^");
 		}
 		
 
 		
 		int max = 0, maxI = -1;
 		for(int i=0;i<parent.length;i++)
 			if(weights[i] > max) {
 				max = weights[i];
 				maxI = i;
 			}
 		
 		int cur = maxI;
 		while(cur != -2) {
 			l.add(0,cur);
 			cur = parent[cur];
 		}
 		return l;		
 	}
 	
 	public static Map<Integer,Set<Integer>> flip(Map<Integer,Set<Overlap>> g) {
 		Map<Integer,Set<Integer>> flipped = new HashMap<Integer,Set<Integer>>();
 		for(Integer i : g.keySet())
 			flipped.put(i, new HashSet<Integer>());
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet())
 			for(Overlap o : e.getValue())
 				flipped.get(o.suf).add(e.getKey());
 		return flipped;
 	}
 	
 	public static List<Integer> foundUseless(Map<Integer,Set<Overlap>> g, Map<Integer,Set<Integer>> gflip) {
 		
 		List<Integer> useless = new LinkedList<Integer>();
 		
 		for(Integer i : g.keySet()) {
 			AtomicInteger count = new AtomicInteger(-2);
 
 			DFS2(i,g,(Set<Integer>)new HashSet<Integer>(),new LinkedList<Integer>(), count);
 			DFS3(i,gflip,(Set<Integer>)new HashSet<Integer>(),new LinkedList<Integer>(), count);
 			
 			if(count.get() < 1000)
 				useless.add(i);
 		}
 		
 		return useless;
 	}
 	
 	private static void DFS3(Integer v, Map<Integer, Set<Integer>> g, Set<Integer> visited, LinkedList<Integer> path, AtomicInteger state) {
 		
 		if(visited.contains(v))
 			return;
 		
 		visited.add(v);
 		path.add(v);
 		state.incrementAndGet();
 		
 		for(Integer n : g.get(v))
 			DFS3(n,g,visited,path,state);
 		path.remove(v);		
 	}
 
 	private static void DFS2(Integer v, Map<Integer,Set<Overlap>> g, Set<Integer> visited, LinkedList<Integer> path, AtomicInteger state) {
 		
 		if(visited.contains(v))
 			return;
 		
 		visited.add(v);
 		path.add(v);
 		state.incrementAndGet();
 		
 		for(Overlap n : g.get(v))
 			DFS2(n.suf,g,visited,path,state);
 		path.remove(v);
 	}
 	
 	public static Integer[] topSort(Map<Integer,Set<Overlap>> g, Map<Integer,Set<Integer>> flipped) {
 		
 		LinkedList<Integer> sources = new LinkedList<Integer>();
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet())
 			if(e.getValue().size() == 0)
 				sources.add(0,e.getKey());
 			
 		System.out.println(sources);
 		int index = 0;
 		Integer[] topSort = new Integer[g.size()];
 		LinkedList<LinkedList<Integer>> toDo = new LinkedList<LinkedList<Integer>>();
 		LinkedList<Integer> path = new LinkedList<Integer>();
 		Set<Integer> visited = new HashSet<Integer>();
 		
 
 		toDo.add(0,sources);
 		
 		while(true) {
 			
 			LinkedList<Integer> top = toDo.get(0);
 			if(top.size() == 0) {
 				if(path.size() == 0)
 					break;
 				
 				System.out.println("exit " + path.get(0));
 				toDo.remove();
 				topSort[index++] = path.remove();
 				continue;
 			}
 			
 			int v = top.remove(0);
 			
 			if(visited.contains(v)) {
 				System.out.println("touch " + v);
 				continue;
 			}
 			
 			System.out.println("enter " + v);
 			visited.add(v);
 			path.add(0,v);
 			
 			LinkedList<Integer> nei = new LinkedList<Integer>();
 			for(Integer n : flipped.get(v))
 				nei.add(0,n);
 			toDo.add(0,nei);
 		}
 		
 		return topSort;
 	}
 	
 	public static Map<Integer,Integer> longestPath(Map<Integer,Set<Overlap>> g) {
 		Map<Integer,Integer> parents = new HashMap<Integer,Integer>();
 		
 		
 		return parents;
 	}
 	
 	public static Set<List<Integer>> cycles(Map<Integer,Set<Overlap>> g) {
 		
 		Set<List<Integer>> cycles = new HashSet<List<Integer>>();
 				
 		DFS(new Integer(g.size()-1),g,(Set<Integer>)new HashSet<Integer>(),new LinkedList<Integer>(), cycles);
 		
 		return cycles;
 	}
 	
 	private static void DFS(Integer v, Map<Integer,Set<Overlap>> g, Set<Integer> visited, LinkedList<Integer> path, Set<List<Integer>> state) {
 		
 		if(visited.contains(v)) {
 			for(int i = 0; i < path.size(); i++) {
 				if(path.get(i).equals(v)) {
 					state.add(new ArrayList<Integer>(path.subList(i, path.size())));
 					return;
 				}
 			}
 			return;
 		}
 		
 		visited.add(v);
 		path.add(v);
 		
 		for(Overlap n : g.get(v))
 			DFS(n.suf,g,visited,path,state);
 		path.remove(v);
 	}
 	
 	public static Map<Integer,String> importLines() throws IOException {
 		Map<Integer,String> lines = new LinkedHashMap<Integer,String>();		
 		
 		File f = new File(file);
 		BufferedReader br = new BufferedReader(new FileReader(f));
 		String s;
 		int index = 0;
 		while((s = br.readLine()) != null)
 			lines.put(new Integer(index++),s);
 		
 		return lines;		
 	}
 	
 	public static Map<Integer,Set<Overlap>> removeUseless(Map<Integer,Set<Overlap>> g) {
 		Map<Integer,Set<Overlap>> newG = new LinkedHashMap<Integer,Set<Overlap>>();
 		
 		Set<Integer> useful = new HashSet<Integer>();
 		
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet())
 			if(e.getValue().size() != 0) {
 				useful.add(e.getKey());
 				//newG.put(e.getKey(), e.getValue());
 			} else {
 				for(Entry<Integer,Set<Overlap>> e2 : g.entrySet()) {
 					if(e2.getKey() != g.size()-1 && e2.getValue().contains(e.getKey())) {
 						useful.add(e.getKey());
 						break;
 						//newG.put(e.getKey(), e.getValue());
 					}
 				}
 			}
 		
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet())
 			if(useful.contains(e.getKey())) {
 				Set<Overlap> newO = new HashSet<Overlap>();
 				for(Overlap o : e.getValue())
 					if(useful.contains(e.getKey()))
 						newO.add(o);
 						
 				newG.put(e.getKey(), newO);
 			}
 				
 		return newG;			
 	}
 	
 	/**
 	 * contracts any vertices that only have one out edge backwards
 	 * 
 	 * @param g
 	 * @return
 	 */
 	public static Map<Integer,Set<Overlap>> contract(Map<Integer,Set<Overlap>> g) {
 		Map<Integer,Set<Overlap>> newG = new LinkedHashMap<Integer,Set<Overlap>>();
 		
 		class Ent {
 			Integer l;
 			Set<Overlap> o;
 			
 			Ent(Integer l, Set<Overlap> o) {
 				this.l = l;
 				this.o = o;
 			}
 		}
 		
 		List<Ent> es = new ArrayList<Ent>(g.size());
 		for(Entry<Integer,Set<Overlap>> e : g.entrySet()) {
 			Set<Overlap> newO = new LinkedHashSet<Overlap>();
 			for(Overlap o : e.getValue())
 				newO.add(new Overlap(o));
 			
 			es.add(new Ent(new Integer(e.getKey()), newO));
 		}
 				
 		int i;
 		do {
 			//We don't need to look at es.size() - 1, which is the added vertex to everything
 			for(i=0;i<es.size()-1;i++) {
 				Ent e = es.get(i);
 				if(e.o.size() == 1) {
 					//System.out.println("Processing vertex " + e.l);
 					Overlap oldO = e.o.toArray(new Overlap[0])[0];
 					Overlap searchO = new Overlap(0,e.l);
 					//System.out.println("Old out edge is " + oldO);
 					
 					//Look for any vertices that have out edges to the one we're removing
 					for(int j=0;j<es.size();j++) {
 						if(i == j)
 							continue;
 						Ent inNei = es.get(j);
 						
 						//If a vertex does have an out edge to the one we're removing, update it
 						if(inNei.o.contains(searchO)) {
 							//System.out.println("Vertex " + inNei.l + " mapped to what we're removing");
 							Overlap newO = null;
 							
 							//Find the old edge to e and make the new one to e's out neighbor
 							Overlap[] os = inNei.o.toArray(new Overlap[0]);
 							for(int k=0;k<os.length;k++)
 								if(os[k].equals(searchO)) {
 									newO = os[k].merge(oldO);						
 									inNei.o.remove(os[k]);
 									break;
 								}
 							
 							//System.out.println("New edge is " + newO);
 							
 							if(inNei.o.contains(searchO)) {
 								for(int k=0;k<os.length;k++)
 									if(os[k].equals(oldO.suf)) {
 										if(os[k].len <= newO.len) {
 											inNei.o.remove(os[k]);
 											inNei.o.add(newO);
 											break;
 										}
 									}
 							} else {
 								inNei.o.add(newO);
 							}
 						}
 						
 					}				
 
 					es.remove(i);	
 					Map<Integer,Set<Overlap>> temp = new LinkedHashMap<Integer,Set<Overlap>>();
 					for(Ent tempe : es)
 						temp.put(tempe.l,tempe.o);
 					//System.out.println(temp);
 					i = -1;
 					break;
 				}
 			}
 		} while(i == -1);
 		
 		for(Ent e : es)
 			newG.put(e.l,e.o);		
 		
 		return newG;
 	}
 }

 package optimization;
 
 import flow_graph.FlowGraph;
 import flow_graph.AssemFlowGraph;
 import graph.Graph;
 import graph.Node;
 import temp.Temp;
 import java.util.*;
 
 public class LivenessAnalysis {
 
 	private FlowGraph flowGraph;
 	private Graph graph;  
     
 	public Hashtable<Node, HashSet<Temp>> in;
 	public Hashtable<Node, HashSet<Temp>> out;
 	public Hashtable<Node, HashSet<Temp>> def;
 	public Hashtable<Node, HashSet<Temp>> use; 
 	
 	public ArrayList<Node> nodes;
	public Hashtable<Node, Boolean> mark; 
 	
 	public void DFS (Node n) {
		if(n==null || mark.get(n)) return;
 		if(n.succ()!=null) {
 			mark.put(n, true);
 			for (Node i: n.succ())
 				DFS(i);
 		}
 		nodes.add(n);
 	}
 	
 	public LivenessAnalysis (AssemFlowGraph g) {
 		
 		//### Algorithm: ###
 		// for each n
 		//     in[n] <- { }; out[n] <- { }
 		// repeat
 		//     for each n
 		//         in'[n] <- in[n]; out'[n] <- out[n]
 		//         in[n] <- use[n] U (out[n] - def [n])
 		//         out[n] <- U(s<-succ[n], in[s])
 		// until in'[n] = in[n] and out'[n] = out[n] for all n
 		
 		
 		// DFS to define the order of operations
 		nodes = new ArrayList<Node>();
		mark = new Hashtable<Node, Boolean>();
 		for(Node i: g.nodes())
			mark.put(i, false);
 		DFS(g.nodes().head);
 		Collections.reverse(nodes);
 		
 		in = new Hashtable<Node, HashSet<Temp>>();
 		out = new Hashtable<Node, HashSet<Temp>>();
 		def = new Hashtable<Node, HashSet<Temp>>();
 		use = new Hashtable<Node, HashSet<Temp>>();
 		
 		// for each n
 		for(Node n: nodes) {
 			// in[n] <- { }; out[n] <- { }
 			in.put(n, new HashSet<Temp>());
 			out.put(n, new HashSet<Temp>());
 			def.put(n, new HashSet<Temp>());
 			use.put(n, new HashSet<Temp>());
 			
 			// def and use
 			if(g.instr(n).def()!=null) {
 				for (Temp i: g.instr(n).def()) {
 					def.get(n).add(i);
 				}
 			}
 			if(g.instr(n).use()!=null) {
 				for (Temp i: g.instr(n).use()) {
 					use.get(n).add(i);
 				}
 			}
 		}
 		
 		HashSet<Temp> in_;
 		HashSet<Temp> out_;
 		// repeat
 		boolean quit;
 		do {
 			quit = true;
 			
 			// for each n
 			for(Node n: nodes) {
 				// in'[n] <- in[n]; out'[n] <- out[n]
 				in_ = (HashSet<Temp>)in.get(n).clone();
 				out_ = (HashSet<Temp>)out.get(n).clone();
 				
 				// in[n] <- use[n] U (out[n] - def[n])
 				HashSet<Temp> tmp = (HashSet<Temp>)out_.clone();
 				for(Temp i: def.get(n))
 					tmp.remove(i);
 				for(Temp i: use.get(n))
 					tmp.add(i);
 				in.put(n, tmp);
 				
 				// out[n] <- U(s<-succ[n], in[s])
 				tmp = new HashSet<Temp>();
 				if(n.succ()!=null) {
 					for(Node s: n.succ()) {
 						for(Temp i: in.get(s))
 							tmp.add(i);
 					}
 				}
 				out.put(n, tmp);
 				
 				// until in'[n] = in[n] and out'[n] = out[n] for all n
 				if ( !in.get(n).equals(in_) || !out.get(n).equals(out_) ) {
 					quit = false;
 				}
 			}
 		} while(!quit);
 	}
 	
 	public HashSet<Temp> getOut(Node n) {
 		return out.get(n);
 	}
 	
 	public void dump() {
 		for(Node n: nodes) {
 			System.out.print("n: " + n);
 			System.out.print(" in: " + in.get(n) + " ");
 			System.out.print(" out: " + out.get(n) + " ");
 			//System.out.print(" def: " + def.get(n) + " ");
 			//System.out.print(" use: " + use.get(n) + " ");
 			System.out.println("");
 		}
 		System.out.println("");
 		
 	}
 }
 

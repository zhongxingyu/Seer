 package Generator.NFA;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 public class NFASimulator {
 	private Queue<Clone> Q;
 	private int numMoves;
 	
 	private NFA regex;
 	private String code;
 	
 	public NFASimulator(NFA nfa) {
 		regex = nfa;
 		reset();
 	}
 	
 	public NFASimulator() {
 		this(null);
 	}
 	
 	public void reset() {
 		Q = new LinkedList<Clone>();
 		numMoves = 0;
 	}
 	
 	public void setNFA(NFA nfa) {
 		regex = nfa;
 		reset();
 	}
 	
 	public void setCode(String in) {
 		code = in;
 		reset();
 	}
 	
 	public Result parse(NFA nfa, String in) {
 		regex = nfa;
 		code = in;
 		return parse();
 	}
 	
 	public Result parse(String in) {
 		code = in;
 		return parse();
 	}
 	
 	public Result parse() {
 		Q.add(new Clone(regex.start(), 0, ""));
 		Result r = null;
 		while(!Q.isEmpty()) {
 			Clone c = Q.poll();
 			Node  u = c.node;
 			List<Transition> adj = u.adjacencyList();
 			for(Transition e : adj) {
 				if(e.isEpsilonTransition()) {
 					Q.add(new Clone(e.end(), c.pos, c.token));
 					numMoves ++;
 				} else if(c.pos != code.length()) {
 					char m = code.charAt(c.pos);
 					if(e.isTriggered(m)) {
 						Q.add(new Clone(e.end(), c.pos+1, c.token + m));
 						numMoves ++;
 					}
 				}
 			}
 			//System.out.println(c);
 			if(u.isFinal() && c.pos == code.length()) {
 				r = new Result(c.token, numMoves);
				break;
 			}
 		}
 		if(r == null) {
 			r = new Result("Failed", numMoves);
 		}
 		reset();
 		return r;
 	}
 	
 	private class Clone {
 		Node node;
 		int pos;
 		String token;
 		
 		private Clone(Node u, int p, String t) {
 			node = u;
 			pos = p;
 			token = t;
 		}
 		
 		public String toString() {
			return token + " at " + pos + " " + node;
 		}
 	}
 	
 	public class Result {
 		public String token;
 		public int numMoves;
 		
 		private Result(String t, int m) {
 			token = t;
 			numMoves = m;
 		}
 		
 		public String toString() {
 			return token + " in " + numMoves + " moves.";
 		}
 	}
 }

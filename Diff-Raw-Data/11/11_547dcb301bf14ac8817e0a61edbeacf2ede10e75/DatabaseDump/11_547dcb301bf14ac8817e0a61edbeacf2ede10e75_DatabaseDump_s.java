 package edu.berkeley.gamesman.tool;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.database.FileDatabase;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * @author Steven Schlansker
  * @author Jeremy Fleischman
  * @param <S> 
  *
  */
 public class DatabaseDump<S> {
 	private static final EnumMap<PrimitiveValue, String> PRIMITIVE_COLORS = new EnumMap<PrimitiveValue, String>(PrimitiveValue.class);
 	static {
 		PRIMITIVE_COLORS.put(PrimitiveValue.Undecided, "black");
 		PRIMITIVE_COLORS.put(PrimitiveValue.Lose, "red");
 		PRIMITIVE_COLORS.put(PrimitiveValue.Win, "green");
 		PRIMITIVE_COLORS.put(PrimitiveValue.Tie, "yellow");
 	}
 	
 	private final PrintWriter w;
 	private final Database db;
 	private final Game<S> gm;
 	private final boolean pruneInvalid;
 	/**
 	 * @param conf
 	 */
 	public DatabaseDump(Configuration conf) {
 		Database db = conf.openDatabase();
 		//db.initialize(conf.getPropertyWithPrompt("gamesman.db.uri"), null);
 		
 		String dottyFile = conf.getPropertyWithPrompt("gamesman.dotty.uri");
 		PrintWriter w = null;
 		try {
 			w = new PrintWriter(new FileWriter(new File(new URI(dottyFile))));
 		} catch(URISyntaxException e) {
 			Util.fatalError("Invalid URI: " + dottyFile, e);
 		} catch(IOException e) {
 			Util.fatalError("Could not open URI: " + dottyFile, e);
 		}
 		
 		Game<S> gm = Util.checkedCast(db.getConfiguration().getGame());
 		pruneInvalid = conf.getProperty("gamesman.dotty.prune", null) != null;
 		this.w = w;
 		this.db = db;
 		this.gm = gm;
 	}
 	
 	private void dump() {
 		if(pruneInvalid)
 			System.out.println("Pruning invalid hashes from the game tree");
 		System.out.println("Running, this may take a while...");
 		w.println("digraph gamesman_dump {");
 		w.println("\tfontname = \"Courier\";");
 		
 		//TODO - this is a nasty way of ensuring everything is on the same row
 		//there's almost definitely a better way of doing this
 		HashMap<Long, ArrayList<BigInteger>> levels = new HashMap<Long, ArrayList<BigInteger>>();
 		if(pruneInvalid) {
 			//TODO - make this work with BFS and maxRemoteness!
 			HashSet<BigInteger> seen = new HashSet<BigInteger>();
 			Queue<BigInteger> fringe = new LinkedList<BigInteger>();
 			for(S s : gm.startingPositions())
 				fringe.add(gm.stateToHash(s));
 			while(!fringe.isEmpty()) {
 				BigInteger parentHash = fringe.remove();
 				if(seen.contains(parentHash)) continue;				
 				seen.add(parentHash);
 				printNode(parentHash, levels, seen, fringe);
 			}
 		} else {
 			for(BigInteger i : Util.bigIntIterator(gm.lastHash()))
 				printNode(i, levels, null, null);
 		}
 		
 		for(Long level : levels.keySet()) {
 			w.print("{ rank=same; ");
 			for(BigInteger hash : levels.get(level)) {
 				w.print("h" + hash + "; ");
 			}
 			w.print(" };\n");
 		}
 		
 		w.println("}");
 		
 		w.close();
 	}
 	
 	private void printNode(BigInteger parentHash, HashMap<Long, ArrayList<BigInteger>> levels, HashSet<BigInteger> seen, Queue<BigInteger> fringe) {
 		Util.assertTrue((seen == null) == (fringe == null), "seen and fringe must both be null or not null!");
 		
 		long remoteness = db.getRecord(parentHash).get(RecordFields.Remoteness);
 		ArrayList<BigInteger> arr = levels.get(remoteness);
 		if(arr == null) {
 			arr = new ArrayList<BigInteger>();
 			levels.put(remoteness, arr);
 		}
 		arr.add(parentHash);
 		
 		S parent = gm.hashToState(parentHash);
 		
 		TreeMap<String, String> attrs = new TreeMap<String, String>();
 		PrimitiveValue v = db.getRecord(parentHash).get();
 		attrs.put("label","<"+parentHash+"<br/>"+gm.displayState(parent).replaceAll("\n", "<br align=\"left\"/>")+"<br/>"+v+" >");
 		
 		String color = PRIMITIVE_COLORS.get(v);
 		Util.assertTrue(color != null, "No color specified for primitive value: " + v);
 		
 		attrs.put("color",color);
 		attrs.put("fontname","courier");
 		
 		PrimitiveValue pv = gm.primitiveValue(parent);
 		if(!pv.equals(PrimitiveValue.Undecided))
 			attrs.put("style","filled");
 		
 		Util.assertTrue(pv.equals(PrimitiveValue.Undecided) || pv.equals(v), "Primitive values don't match! "+pv
 				+" (db says "+v+") for pos "+parentHash+"\n"+gm.displayState(parent));
 		
 		w.print("\th"+parentHash+" [ ");
 		boolean didOne = false;
 		for(Entry<String, String> attr : attrs.entrySet()){
 			w.print((didOne?',':' ')+" "+attr.getKey()+" = "+attr.getValue());
 			didOne = true;
 		}
 		w.println(" ];");
 		for(Pair<String,S> child : gm.validMoves(parent)){
 			BigInteger childHash = gm.stateToHash(child.cdr);
 			//no reason to add the child to the fringe if we've already seen him
 			//this would work fine if we just added him anyways, but this is probably better
 			if(pruneInvalid && !seen.contains(childHash)) 
 				fringe.add(childHash);
 			w.println("\th"+parentHash+" -> h"+childHash+" [ label = \""+child.car+"\" ];");
 		}
 	}
 
 	/**
 	 * @param <S> 
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static <S> void main(String[] args) {
 		if(args.length < 1) Util.fatalError("Please specify a jobfile");
 		Configuration conf = new Configuration(args[0]);
 		Util.debugInit(conf);
 		new DatabaseDump<S>(conf).dump();
 	}
 }

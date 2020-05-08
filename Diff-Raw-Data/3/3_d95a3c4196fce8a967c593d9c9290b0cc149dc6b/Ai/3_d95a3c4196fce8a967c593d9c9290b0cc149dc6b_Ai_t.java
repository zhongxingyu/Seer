 package jothello;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 
 import org.luaj.vm2.Globals;
 import org.luaj.vm2.LuaTable;
 import org.luaj.vm2.LuaValue;
 import org.luaj.vm2.lib.jse.JsePlatform;
 
 public class Ai {
 	
 	Globals globals;
 	public final static int CORNER_WEIGHT = 10;
 	public final static double DISC_WEIGHT = 0.01;
 	public final static int MONTE_CARLO_TREE_SEARCH = 0;
 	public final static int MINIMAX_ALPHA_BETA = 1;	
 	public final int algo_type;	
 	LuaValue monteCarlo = null;
 	LuaValue miniMax = null;
 	LuaTable param = null;
 	
 	public Ai(int algo_type) {
 		String script = "lib/ai.lua";
 		
 		// create an environment to run in
 		globals = JsePlatform.standardGlobals();
 		globals.load(new aif());
 		
 		// Use the convenience function on the globals to load a chunk.
 		LuaValue chunk = globals.loadFile(script);
 		
 		// Use any of the "call()" or "invoke()" functions directly on the chunk.
 		chunk.call( LuaValue.valueOf(script) );
 		
 		this.algo_type = algo_type;
 		
 		monteCarlo = globals.get("monteCarlo");
 		miniMax = globals.get("miniMax");
 		param = new LuaTable();
 		//param.set("time", 10);
 		//param.set("fixed_depth", 4);
 		param.set("max_depth", 4);
		param.set("use_tt", LuaValue.valueOf(true));
		param.set("get_pv", LuaValue.valueOf(true));
		//param.set("no_tt_move_ordering", LuaValue.valueOf(true));
 	}
 	
 	public Point selectMove(Jothello jothello) {
 		ArrayList<Point> legalMoves = jothello.getAllMoves();
 		LuaValue retvals = null;
 		
 		if(algo_type == MONTE_CARLO_TREE_SEARCH) {			
 			retvals = monteCarlo.call(LuaValue.valueOf(jothello.getGameStateString()), LuaValue.valueOf(jothello.getNumberOfMoves()));
 		}else {			
 			retvals = miniMax.call(LuaValue.valueOf(jothello.getGameStateString()), param);
 		}		
 		//System.out.println("best_move_index : " + retvals.toint());
 		return legalMoves.get(retvals.toint());
 	}	
 }
 

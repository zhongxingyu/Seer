 package cyfn.rubics;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CubeSolver {
 
 	private List<String> turnLog;
 	private RubicsCube cube;
 	private int maxTurns;
 	
 	private enum Action {CW(""),CCW("i"),HALFTURN("2");
 		private String str;
 		Action(String str) {	
 			this.str = str;
 		}
 		public String toString() {
 			return str;
 		}
 	}
 	private Side[] sides = {Side.UP, Side.RIGHT, Side.FRONT};
 	
 	public CubeSolver(RubicsCube cube, int maxTurns) {
 		this.cube = cube;
 		this.maxTurns = maxTurns;
 		turnLog = new ArrayList<>(maxTurns);
 	}
 	
 	public void run() {
 		solve(null,0);
 	}
 	
 	private boolean solve(Side lastSide, int depth) {
 		if(cube.isSolved()) return true;
 		if(depth>=maxTurns) return false;
 		
 		for (Side side: sides) {
 			if(lastSide==side) continue;
 			for (Action action : Action.values()) {
 				turnCube(action, side);
 				boolean result = solve(side, depth+1);
 				if(result == false)
					undo();
 				else
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	public String getSolution() {
 		String result = "";
 		for(String str : turnLog) {
 			result+=str+" ";
 		}
 		return result;
 	}
 	
 	private void turnCube(Action action, Side side) {
 		turnLog.add("" + side.toString().substring(0, 1) + action);
 		switch(action) {
 		case CW: 
 			cube.turnFace(Direction.CLOCKWISE, side); 
 			break;
 		case CCW: 
 			cube.turnFace(Direction.COUNTERCLOCKWISE, side); 
 			break;
 		case HALFTURN: 
 			cube.turnFace(Direction.COUNTERCLOCKWISE, side);
 			cube.turnFace(Direction.COUNTERCLOCKWISE, side);
 		}
 	}
 	
	private void undo() {
 		turnLog.remove(turnLog.size()-1);
 		cube.undoLastTurn();
 	}
	
	
 }

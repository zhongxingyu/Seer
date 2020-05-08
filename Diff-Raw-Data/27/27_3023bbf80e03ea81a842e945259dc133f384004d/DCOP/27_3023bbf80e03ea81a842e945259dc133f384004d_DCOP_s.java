 package mybot.algo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import core.Aim;
 import core.Tile;
 import mybot.GameState;
 import mybot.MapTile;
 
 public class DCOP {
 
 	private static DCOP instance;
 
	private int viewRadius, viewRadiusSquared;
 	private static HashMap<Aim, List<int[]>> viewDiff;
 
 	public static DCOP getInstance() {
 		if (instance == null)
 			instance = new DCOP();
 		return instance;
 	}
 
 	public static List<MapTile> getNeighbourMapTilesOrderedByExploringValue(MapTile tile) {
 		List<MapTile> results = tile.getPassableNeighbours();
 		Collections.sort(results, getInstance().new ExploringValueComparator(tile));
 		return results;
 	}
 
 	public static int getMoveExploringValue(Tile tile, Aim aim) {
 		int value = 0;
 		for (MapTile diffTile : populateViewDiffFromTileTo(tile, aim))
 			value += diffTile.getUnseenDuration();
 		return value;
 	}
 
 	public static List<MapTile> populateViewDiffFromTileTo(Tile tile, Aim aim) {
 		List<MapTile> diffs = new ArrayList<MapTile>();
 		for (int[] delta : viewDiff.get(aim))
 			diffs.add(GameState.getMap().getTile(tile.getRow() + delta[0], tile.getCol() + delta[1]));
 		return diffs;
 	}
 
 	public static void initGameSpecificVariables() {
		getInstance().viewRadiusSquared = GameState.getCore().getAttackRadius2();
		getInstance().viewRadius = (int) Math.sqrt(getInstance().viewRadiusSquared);
 		getInstance().generateViewDifferenceMap();
 	}
 
 	private void initialsViewDifferenceMap() {
 		viewDiff = new HashMap<Aim, List<int[]>>();
 		for (Aim aim : Aim.values())
 			viewDiff.put(aim, new ArrayList<int[]>());
 	}
 
 	private void generateViewDifferenceMap() {
 		initialsViewDifferenceMap();
 		for (int col = -viewRadius - 1; col <= viewRadius + 1; col++)
			for (int row = -viewRadius - 1; row < viewRadius + 1; row++)
 				if (!isVectorInViewRadius(row, col))
 					for (Aim aim : Aim.values())
 						addPointToViewDifferenceMapIfInViewRadius(aim, row + aim.getRowDelta(), col + aim.getColDelta());
 	}
 
 	private void addPointToViewDifferenceMapIfInViewRadius(Aim aim, int row, int col) {
 		if (isVectorInViewRadius(row, col))
 			viewDiff.get(aim).add(new int[] { row, col });
 	}
 
 	private boolean isVectorInViewRadius(int row, int col) {
 		return sizeOfVectorSquared(row, col) <= viewRadiusSquared;
 	}
 
 	public static int sizeOfVectorSquared(int row, int col) {
 		return row * row + col * col;
 	}
 
 	public class ExploringValueComparator implements Comparator<MapTile> {
 
 		MapTile origin;
 
 		public ExploringValueComparator(MapTile origin) {
 			this.origin = origin;
 		}
 
 		@Override
 		public int compare(MapTile o1, MapTile o2) {
 			return getValue(o1).compareTo(getValue(o2));
 		}
 
 		private Integer getValue(MapTile tile) {
			return (Integer) getMoveExploringValue(origin, origin.getAimTo(tile));
 		}
 
 	}
 }

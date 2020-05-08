 package teropa.globetrotter.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import teropa.globetrotter.client.common.Rectangle;
 import teropa.globetrotter.client.common.Size;
 import teropa.globetrotter.client.event.internal.ViewPanEvent;
 
 public class Grid implements ViewPanEvent.Handler {
 
 	private static final int BUFFER = 2;
 	
 	private final int tileWidth;
 	private final int tileHeight;
 	private final ViewContext ctx;
 	
 	private int numCols;
 	private int numRows;
 	private int[] tileXs;
 	private int[] tileYs;
 
 	private int[] coords;
 	
 	public Grid(Size tileSize, ViewContext ctx) {
 		this.tileWidth = tileSize.getWidth();
 		this.tileHeight = tileSize.getHeight();
 		this.ctx = ctx;		
		ctx.getView().addViewPanHandler(this);
 	}
 
 	public int getNumCols() {
 		return numCols;
 	}
 
 	public int getNumRows() {
 		return numRows;
 	}
 
 	public void init(Size fullSize) {
 		notifyAllTilesRemoved();
 		
 		numCols = fullSize.getWidth() / tileWidth;
 		numRows = fullSize.getHeight() / tileHeight;
 		
 		tileXs = initTileXs();
 		tileYs = initTileYs();
 
 		coords = getVisibleCoords(ctx.getVisibleRectangle());
 		Set<Tile> tiles = new HashSet<Grid.Tile>();
 		for (int xIdx = coords[0] ; xIdx <= coords[1] ; xIdx++) {
 			for (int yIdx = coords[2] ; yIdx <= coords[3] ; yIdx++) {
 				tiles.add(makeTile(xIdx, yIdx));
 			}
 		}
 		notifyNewTiles(tiles);
 	}
 
 	private int[] initTileXs() {
 		int[] res = new int[numCols];
 		for (int i=0 ; i<numCols ; i++) {
 			res[i] = i * tileWidth;
 		}
 		return res;
 	}
 
 	private int[] initTileYs() {
 		int[] res = new int[numRows];
 		for (int i=0 ; i<numRows ; i++) {
 			res[i] = i * tileHeight;
 		}
 		return res;
 	}
 
 	public void onViewPanned(ViewPanEvent event) {
 		final int[] newCoords = getVisibleCoords(ctx.getVisibleRectangle());
 		if (Arrays.equals(newCoords, coords)) {
 			return;
 		}
 		
 		final int oldXFrom = coords[0], oldXTo = coords[1], oldYFrom = coords[2], oldYTo = coords[3];
 		final int newXFrom = newCoords[0], newXTo = newCoords[1], newYFrom = newCoords[2], newYTo = newCoords[3];
 		
 		final Set<Tile> removedTiles = new HashSet<Grid.Tile>();
 		removedTiles.addAll(makeTiles(oldXFrom, newXFrom - 1, oldYFrom, oldYTo));
 		removedTiles.addAll(makeTiles(newXTo + 1, oldXTo, oldYFrom, oldYTo));
 		removedTiles.addAll(makeTiles(oldXFrom, oldXTo, oldYFrom, newYFrom - 1));
 		removedTiles.addAll(makeTiles(oldXFrom, oldXTo, newYTo + 1, oldYTo));
 		notifyRemovedTiles(removedTiles);
 		
 		final Set<Tile> newTiles = new HashSet<Tile>();
 		newTiles.addAll(makeTiles(newXFrom, oldXFrom - 1, newYFrom, newYTo));
 		newTiles.addAll(makeTiles(oldXTo + 1, newXTo, newYFrom, newYTo));
 		newTiles.addAll(makeTiles(newXFrom, newXTo, newYFrom, oldYFrom - 1));
 		newTiles.addAll(makeTiles(newXFrom, newXTo, oldYTo + 1, newYTo));
 		notifyNewTiles(newTiles);
 
 		coords = newCoords;
 	}
 
 	private void notifyRemovedTiles(final Set<Tile> removedTiles) {
 		List<Layer> layers = ctx.getLayers();
 		int sz = layers.size();
 		for (int i=0 ; i<sz ; i++) {
 			layers.get(i).onTilesDeactivated(removedTiles);
 		}
 	}
 
 	private void notifyAllTilesRemoved() {
 		List<Layer> layers = ctx.getLayers();
 		int sz = layers.size();
 		for (int i=0 ; i<sz ; i++) {
 			layers.get(i).onAllTilesDeactivated();
 		}
 	}
 
 	private void notifyNewTiles(final Set<Tile> newTiles) {
 		List<Layer> layers = ctx.getLayers();
 		int sz = layers.size();
 		for (int i=0 ; i<sz ; i++) {
 			layers.get(i).onTilesActivated(newTiles);
 		}
 	}
 
 	private List<Tile> makeTiles(int fromX, int toX, int fromY, int toY) {
 		List<Tile> result = new ArrayList<Tile>();
 		for (int xIdx = fromX ; xIdx <= toX ; xIdx++) {
 			for (int yIdx = fromY ; yIdx <= toY ; yIdx++) {
 				result.add(makeTile(xIdx, yIdx));
 			}
 		}
 		return result;
 	}
 	
 	private Tile makeTile(int xIdx, int yIdx) {
 		int x = tileXs[xIdx];
 		int y = tileYs[yIdx];
 		return new Tile(xIdx, yIdx, x, y);
 	}
 	
 
 	private int[] getVisibleCoords(Rectangle area) {
 		int xStart = 0;
 		while (xStart < tileXs.length && tileXs[xStart] < area.x)
 			xStart++;
 		if (xStart > 0) xStart--;
 		xStart = Math.max(0, xStart - BUFFER);
 		
 		int xEnd = xStart;
 		while (xEnd < tileXs.length && tileXs[xEnd] < area.x + area.width)
 			xEnd++;
 		if (xEnd > xStart) xEnd--;
 		xEnd = Math.min(numCols - 1, xEnd + BUFFER);
 		
 		int yStart = 0;
 		while (yStart < tileYs.length && tileYs[yStart] < area.y)
 			yStart++;
 		if (yStart > 0) yStart--;
 		yStart = Math.max(0, yStart - BUFFER);
 		
 		int yEnd = yStart;
 		while (yEnd < tileYs.length && tileYs[yEnd] < area.y + area.height)
 			yEnd++;
 		if (yEnd > yStart) yEnd--;
 		yEnd = Math.min(numRows - 1, yEnd + BUFFER);
 		
 		return new int[] { xStart, xEnd, yStart, yEnd };
 	}
 	
 	public class Tile {
 		
 		private final int col;
 		private final int row;
 		private final int leftX;
 		private final int topY;
 		
 		public Tile(int col, int row, int leftX, int topY) {
 			this.col = col;
 			this.row = row;
 			this.leftX = leftX;
 			this.topY = topY;
 		}
 		
 		public int getCol() {
 			return col;
 		}
 
 		public int getRow() {
 			return row;
 		}
 		
 		public int getLeftX() {
 			return leftX;
 		}
 		
 		public int getTopY() {
 			return topY;
 		}
 		
 		public int getWidth() {
 			return tileWidth;
 		}
 		
 		public int getHeight() {
 			return tileHeight;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			if (obj == null) return false;
 			Tile o = (Tile)obj;
 			return col == o.col && row == o.row;
 		}
 		
 		@Override
 		public int hashCode() {
 			int hash = 42;
 			hash = 37 * hash + col;
 			hash = 37 * hash + row;
 			return hash;
 		}
 		
 		@Override
 		public String toString() {
 			return "Tile ["+col+","+row+"]";
 		}
 	}
 
 	
 }

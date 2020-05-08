 package com.lunasoft.dynasty.tools.mapeditor;
 
 import com.lunasoft.dynasty.tools.mapeditor.TileData.ReliefType;
 
 class DemHexifier {
 
 	private final short[][] heights;
 	private final HexDimensions hexDimensions;
 
 	DemHexifier(short[][] heights, HexDimensions hexDimensions) {
 		this.heights = heights;
 		this.hexDimensions = hexDimensions;
 	}
 
 	GameMap hexify() {
 		int numCols = heights.length;
 		int numRows = heights[0].length;
 		HexGridDimensions gridDimensions = HexGridDimensions.coveringArea(hexDimensions,
 				numCols, numRows);
 		System.out.println("hexifier dimensions: " + gridDimensions);
 		System.out.format("numCols = %d, numRows = %d%n", numCols, numRows);
 		GameMap gameMap = new GameMap(false, gridDimensions.getWidth(),
 				gridDimensions.getHeight());
 		for (int i = 0; i < gridDimensions.getWidth(); i++) {
 			for (int j = 0; j < gridDimensions.getHeight(); j++) {
 				double[] center = gridDimensions.getCenter(i, j);
 				double westEdge = center[0] - hexDimensions.getInRadius();
 				double eastEdge = center[0] + hexDimensions.getInRadius();
 				int minCol = Math.max(0, 1 + (int) westEdge);
 				int centerCol = (int) center[0];
 				int maxCol = Math.min(numCols - 1, (int) eastEdge);
 				System.out.format("i,j = %d,%d: col = %d-%d%n", i, j, minCol, maxCol);
 				DataAccumulator accumulator = new DataAccumulator();
 				for (int col = minCol; col <= centerCol; col++) {
 					if (col >= numCols) {
 						continue;
 					}
 					double southEdge = HexGridDimensions.getDiagonalSlope() * (col - westEdge)
 							+ center[1] + 0.5 * hexDimensions.getCircumRadius();
 					double northEdge = -HexGridDimensions.getDiagonalSlope() * (col - westEdge)
 							+ center[1] - 0.5 * hexDimensions.getCircumRadius();
 					int minRow = Math.max(0, 1 + (int) northEdge);
 					int maxRow = Math.min(numRows - 1, (int) southEdge);
 					assert(distance(i, j, col, minRow) <= hexDimensions.getCircumRadius());
 					assert(distance(i, j, col, maxRow) <= hexDimensions.getCircumRadius());
 					processColumn(accumulator, col, minRow, maxRow);
 				}
 				for (int col = centerCol + 1; col <= maxCol; col++) {
 					double southEdge = -HexGridDimensions.getDiagonalSlope() * (col - center[0])
 							+ center[1] + hexDimensions.getCircumRadius();
 					double northEdge = HexGridDimensions.getDiagonalSlope() * (col - center[0])
 							+ center[1] - hexDimensions.getCircumRadius();
 					int minRow = Math.max(0, 1 + (int) northEdge);
 					int maxRow = Math.min(numRows - 1, (int) southEdge);
 					assert(distance(i, j, col, minRow) <= hexDimensions.getCircumRadius());
 					assert(distance(i, j, col, maxRow) <= hexDimensions.getCircumRadius());
 					processColumn(accumulator, col, minRow, maxRow);
 				}
				gameMap.setTile(i, j, new TileData(getTerrainType(accumulator)));
 			}
 		}
 		return gameMap;
 	}
 
 	private double distance(int x0, int y0, int x1, int y1) {
 		int xx = x0 - x1;
 		int yy = y0 - y1;
 		return Math.sqrt(xx*xx + yy*yy);
 	}
 
 	private void processColumn(DataAccumulator accumulator, int col, int minRow,
 			int maxRow) {
 		System.out.format("col = %d: row = %d-%d%n", col, minRow, maxRow);
 		for (int row = minRow; row <= maxRow; row++) {
 			if (heights[col][row] > 0) {
 				accumulator.add(heights[col][row]);
 			} else {
 				accumulator.addNull();
 			}
 		}
 	}
 
 	private ReliefType getTerrainType(DataAccumulator accumulator) {
 		if (accumulator.getFractionReal() > 0.3) {
 			return ReliefType.PLAINS;
 		}
 		return ReliefType.WATER;
 	}
 
 	private static class DataAccumulator {
 		private int numPoints = 0;
 		private int numRealPoints = 0;
 		private int sum = 0;
 		private int sumSquares = 0;
 		private Integer max;
 		private Integer min;
 
 		void addNull() {
 			numPoints++;
 		}
 
 		void add(int value) {
 			numPoints++;
 			numRealPoints++;
 			sum += value;
 			sumSquares += value * value;
 			if (max == null || value > max) {
 				max = value;
 			}
 			if (min == null || value < min) {
 				min = value;
 			}
 		}
 
 		double getFractionReal() {
 			if (numPoints > 0) {
 				return numRealPoints / (double) numPoints;
 			}
 			return Double.NaN;
 		}
 
 		double getMean() {
 			if (numRealPoints > 0) {
 				return sum / (double) numRealPoints;
 			}
 			return Double.NaN;
 		}
 
 		int getMax() {
 			return max;
 		}
 
 		int getMin() {
 			return min;
 		}
 	}
 }

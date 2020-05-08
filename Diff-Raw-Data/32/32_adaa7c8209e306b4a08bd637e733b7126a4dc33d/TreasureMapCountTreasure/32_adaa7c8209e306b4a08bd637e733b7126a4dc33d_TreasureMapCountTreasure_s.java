 package com.francesc.treasuremap;
 
 public class TreasureMapCountTreasure extends Map {
 
 	/**
 	 * Threshold to help to autogenerate map.
 	 */
 	private double threshold;
 
 	/**
 	 * Add the threshold. For autogeneraded treasure maps.
 	 * 
 	 * @param i
 	 *            map width to generate
 	 * @param j
 	 *            map height to generate
 	 * @param threshold
 	 *            to
 	 */
 	public TreasureMapCountTreasure(int i, int j, double threshold) {
 		super(i, j);
 		this.threshold = threshold;
 	}
 
 	/**
 	 * Treasure map from outer param
 	 * 
 	 * @param map
 	 *            int [][] treasure map
 	 */
 	public TreasureMapCountTreasure(int[][] map) {
 		super(map);
 	}
 
 	/**
 	 * If random > threshold this is going to be a treasure point.
 	 * 
 	 * @return int 1 if is a treasure point, 0 if no
 	 */
 	@Override
 	protected int populatePoint() {
 		double rand = Math.random();
 		if (rand > threshold) {
 			rand = 1;
 		} else {
 			rand = 0;
 		}
 		return (int) rand;
 	}
 
 	/**
 	 * redirects to solvePointIterative
 	 */
 	@Override
	public int solvePoint(int x, int y) {
		return solvePointAddToNeighbour(x, y);
 	}
 
 	/**
 	 * If a treasure is found, add +1 to neigbours
 	 * 
 	 * @param x
 	 *            width point to add +1
 	 * @param y
 	 *            height point to add +1
 	 */
 	private void addTreasureToNeighbours(int x, int y) {
 
 		for (int i = x - 1; i < x + 2; i++) {
 
 			for (int j = y - 1; j < y + 2; j++) {
 
 				if (isInGrid(i, j)) {
 					getMap()[i][j]++;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Solves the problem using iterative approach. This approach is quicker
 	 * than recursive
 	 * 
 	 * @param map
 	 *            int [][] treasure map
 	 * @param x
 	 *            width to evaluate
 	 * @param y
 	 *            height to evaluate
 	 * @return int how many treasures are nearby?
 	 */
	private int solvePointAddToNeighbour(int x, int y) {
		int count = 0;

 		for (int i = x - 1; i < x + 2; i++) {
 
 			for (int j = y - 1; j < y + 2; j++) {
 
 				if (isTreasurePoint(i, j)) {
 					addTreasureToNeighbours(i, j);
 				}
 			}
 		}

		return count;
 	}
 
 	/**
 	 * Threshold getter
 	 * 
 	 * @return double threshold
 	 */
 	public double getThreshold() {
 		return threshold;
 	}
 
 	/**
 	 * Threshold setter
 	 * 
 	 * @param threshold
 	 */
 	public void setThreshold(double threshold) {
 		this.threshold = threshold;
 	}
 
 }

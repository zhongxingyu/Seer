 /* This file is part of Eternity II Editor.
  *
  * Eternity II Editor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Eternity II Editor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Eternity II Editor.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Eternity II Editor project is hosted on SourceForge:
  * http://sourceforge.net/projects/eternityii/
  * and maintained by Yannick Kirschhoffer <alcibiade@alcibiade.org>
  */
 
 package org.alcibiade.eternity.editor.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 
 public class GridModel extends AbstractQuadGrid implements Cloneable {
 
 	private static final long serialVersionUID = 1L;
 
 	private boolean readOnly;
 
 	private Set<GridObserver> gridObservers = new HashSet<GridObserver>();
 
 	private Pattern defaultpat;
 
 	public GridModel() {
 		this(2);
 	}
 
 	public GridModel(int size) {
 		super(size, size);
 		defaultpat = Pattern.getDefaultPattern();
 		readOnly = false;
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 		for (QuadModel quad : gridQuads) {
 			quad.setReadOnly(readOnly);
 		}
 	}
 
 	@Override
 	public GridModel clone() {
 		GridModel newgrid = new GridModel(getSize());
 
 		for (int i = 0; i < gridQuads.size(); i++) {
 			newgrid.gridQuads.set(i, gridQuads.get(i).clone());
 		}
 
 		return newgrid;
 	}
 
 	public int getSize() {
 		return super.getWidth();
 	}
 
 	public void setSize(int size) {
 		assert !readOnly;
 
 		super.setSize(size, size);
 		notifyGridSizeUpdated();
 	}
 
 	public void copyTo(GridModel destGrid) {
 		if (destGrid.getSize() != getSize()) {
 			destGrid.setSize(getSize());
 		}
 
 		for (int i = 0; i < gridQuads.size(); i++) {
 			getQuad(i).copyTo(destGrid.getQuad(i), false);
 		}
 
 		destGrid.notifyGridUpdated();
 	}
 
 	public void reset() {
 		assert !readOnly;
 
 		for (QuadModel quad : gridQuads) {
 			quad.clear();
 		}
 	}
 
 	public void shuffle() {
 		assert !readOnly;
 
 		Random rand = new Random();
 
 		for (int i = 0; i < getSize() * getSize(); i++) {
 			int swap_pos = rand.nextInt(getSize() * getSize());
 			int rotations = rand.nextInt(4);
 
 			QuadModel quad = gridQuads.get(i);
 			QuadModel target = gridQuads.get(swap_pos);
 
 			if (!quad.isLocked() && !target.isLocked()) {
 				quad.rotateClockwise(rotations);
 				quad.swap(target);
 			}
 		}
 	}
 
 	public void addGridObserver(GridObserver observer) {
 		gridObservers.add(observer);
 	}
 
 	public void removeGridObserver(GridObserver observer) {
 		gridObservers.remove(observer);
 	}
 
 	public boolean isComplete() {
 		return countPairs() == countConnections();
 	}
 
 	public int countConnections() {
 		return 2 * getSize() * (getSize() - 1);
 	}
 
 	public int countPairs() {
 		int pairs = 0;
 
 		for (int y = 0; y < getSize(); y++) {
 			for (int x = 0; x < getSize(); x++) {
 				QuadModel quad = getQuad(x, y);
 
 				if (x < getSize() - 1) {
 					QuadModel eastquad = getQuad(x + 1, y);
 					Pattern eastpat = quad.getPattern(QuadModel.DIR_EAST);
 
 					if (eastpat != defaultpat && eastpat == eastquad.getPattern(QuadModel.DIR_WEST)) {
 						pairs++;
 					}
 				}
 
 				if (y < getSize() - 1) {
 					QuadModel southquad = getQuad(x, y + 1);
 					Pattern southpat = quad.getPattern(QuadModel.DIR_SOUTH);
 
 					if (southpat != defaultpat
 							&& southpat == southquad.getPattern(QuadModel.DIR_NORTH)) {
 						pairs++;
 					}
 				}
 			}
 		}
 
 		return pairs;
 	}
 
 	public int countPairs(int index) {
 		return countPairs(index % getSize(), index / getSize());
 	}
 
 	public int countPairs(int x, int y) {
 		return countPairs(x, y, 1);
 	}
 
 	public int countPairs(int x, int y, int borderweight) {
 		if (x < 0 || x >= getSize() || y < 0 || y >= getSize()) {
 			return 0;
 		}
 
 		int pairs = 0;
 		QuadModel quad = getQuad(x, y);
 
 		if (x == 0) {
 			if (quad.getPattern(QuadModel.DIR_WEST) == defaultpat) {
 				pairs += borderweight;
 			}
 		} else {
 			QuadModel westquad = getQuad(x - 1, y);
 			Pattern westpat = quad.getPattern(QuadModel.DIR_WEST);
 
 			if (westpat != defaultpat && westpat == westquad.getPattern(QuadModel.DIR_EAST)) {
 				pairs++;
 			}
 		}
 
 		if (x < getSize() - 1) {
 			QuadModel eastquad = getQuad(x + 1, y);
 			Pattern eastpat = quad.getPattern(QuadModel.DIR_EAST);
 
 			if (eastpat != defaultpat && eastpat == eastquad.getPattern(QuadModel.DIR_WEST)) {
 				pairs++;
 			}
 		} else {
 			if (quad.getPattern(QuadModel.DIR_EAST) == defaultpat) {
 				pairs += borderweight;
 			}
 		}
 
 		if (y == 0) {
 			if (quad.getPattern(QuadModel.DIR_NORTH) == defaultpat) {
 				pairs += borderweight;
 			}
 		} else {
 			QuadModel northquad = getQuad(x, y - 1);
 			Pattern northpat = quad.getPattern(QuadModel.DIR_NORTH);
 
 			if (northpat != defaultpat && northpat == northquad.getPattern(QuadModel.DIR_SOUTH)) {
 				pairs++;
 			}
 		}
 
 		if (y < getSize() - 1) {
 			QuadModel southquad = getQuad(x, y + 1);
 			Pattern southpat = quad.getPattern(QuadModel.DIR_SOUTH);
 
 			if (southpat != defaultpat && southpat == southquad.getPattern(QuadModel.DIR_NORTH)) {
 				pairs++;
 			}
 		} else {
 			if (quad.getPattern(QuadModel.DIR_SOUTH) == defaultpat) {
 				pairs += borderweight;
 			}
 		}
 
 		return pairs;
 	}
 
 	public int countOccurences(Pattern pattern) {
 		int occurences = 0;
 
 		for (QuadModel quad : gridQuads) {
 			if (quad.getPattern(QuadModel.DIR_NORTH) == pattern) {
 				occurences++;
 			}
 			if (quad.getPattern(QuadModel.DIR_SOUTH) == pattern) {
 				occurences++;
 			}
 			if (quad.getPattern(QuadModel.DIR_EAST) == pattern) {
 				occurences++;
 			}
 			if (quad.getPattern(QuadModel.DIR_WEST) == pattern) {
 				occurences++;
 			}
 		}
 
 		return occurences;
 	}
 
 	public int countOpenOccurences(Pattern pattern) {
 		int result = 0;
 
 		for (int y = 0; y < getSize(); y++) {
 			for (int x = 0; x < getSize(); x++) {
 				QuadModel quad = getQuad(x, y);
 				QuadModel quad_n = getQuad(x, y - 1);
 				QuadModel quad_e = getQuad(x + 1, y);
 				QuadModel quad_s = getQuad(x, y + 1);
 				QuadModel quad_w = getQuad(x - 1, y);
 
 				if (quad.getPattern(QuadModel.DIR_NORTH) == pattern && quad_n != null
 						&& quad_n.isClear()) {
 					result++;
 				}
 
 				if (quad.getPattern(QuadModel.DIR_EAST) == pattern && quad_e != null
 						&& quad_e.isClear()) {
 					result++;
 				}
 
 				if (quad.getPattern(QuadModel.DIR_SOUTH) == pattern && quad_s != null
 						&& quad_s.isClear()) {
 					result++;
 				}
 
 				if (quad.getPattern(QuadModel.DIR_WEST) == pattern && quad_w != null
 						&& quad_w.isClear()) {
 					result++;
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public String getStatusMessage() {
 		String message = null;
 
 		List<Pattern> allpatterns = Pattern.getAllPatterns();
 
 		for (Pattern pattern : allpatterns) {
 			int occurences = countOccurences(pattern);
 
 			if (pattern == Pattern.getDefaultPattern()) {
 				int desired = 4 * getSize();
 
 				if (occurences < desired) {
 					message = "Too few border tiles";
 				} else if (occurences > desired) {
 					message = "Too many border tiles";
 				}
 			} else {
 				if ((occurences % 2) == 1) {
 					message = "Odd amount of pat. " + pattern.getCode();
 				}
 			}
 		}
 
 		if (message == null) {
 			message = "Ok";
 		}
 
 		return message;
 	}
 
 	public void fromQuadString(String text) throws QuadsFormatException {
 		assert !readOnly;
 
 		text = stripComments(text);
 
 		StringTokenizer stok = new StringTokenizer(text);
 
 		int patternCount = countTextPatterns(text);
 		double gridsize = Math.sqrt(patternCount / 4);
 
 		if (gridsize != Math.round(gridsize) || gridsize < 2) {
 			throw new QuadsFormatException("Inconsistent grid size. " + patternCount
 					+ " values found instead of n * n * 4");
 		}
 
 		int i_gridsize = (int) Math.round(gridsize);
 
 		int ix_quad = 0;
 		int ix_dir = 0;
 		boolean lock = false;
 
 		List<QuadModel> quadbuffer = new ArrayList<QuadModel>();
 
 		while (stok.hasMoreTokens() && ix_quad < i_gridsize * i_gridsize) {
 			int pattern_code;
 			String token = stok.nextToken();
 
 			if (token.contains("[")) {
 				lock = true;
 				token = token.replaceAll("\\[", "");
 			}
 
 			if (token.contains("]")) {
 				lock = false;
 				token = token.replaceAll("\\]", "");
 			}
 
 			if (token.length() > 0) {
 				try {
 					pattern_code = Integer.parseInt(token);
 				} catch (NumberFormatException ex) {
 					throw new QuadsFormatException("Incorrect number format '" + token + "'");
 				}
 
 				Pattern pattern = Pattern.getPatternByCode(pattern_code);
 
 				if (pattern == null) {
 					throw new QuadsFormatException("Unknown pattern code " + pattern_code);
 				}
 
 				while (ix_quad >= quadbuffer.size()) {
 					QuadModel quad = new QuadModel();
 					quad.setLocked(lock);
 					quadbuffer.add(quad);
 				}
 
 				if (pattern != null) {
 					quadbuffer.get(ix_quad).setPattern(ix_dir, pattern);
 
 					ix_dir++;
 
 					if (ix_dir == 4) {
 						ix_dir = 0;
 						ix_quad++;
 					}
 				}
 			}
 		}
 
 		setSize(i_gridsize);
 		for (int qi = 0; qi < quadbuffer.size(); qi++) {
 			quadbuffer.get(qi).copyTo(gridQuads.get(qi));
 		}
 	}
 
 	private String stripComments(String text) {
 		text = text.replace('\r', '\n');
 		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#.*[\n]",
 				java.util.regex.Pattern.MULTILINE);
 
 		Matcher matcher = pattern.matcher(text);
 		text = matcher.replaceAll("");
 		return text;
 	}
 
 	private static int countTextPatterns(String text) {
 		StringTokenizer stok = new StringTokenizer(text, "\n \t[]");
 		return stok.countTokens();
 	}
 
 	private void notifyGridSizeUpdated() {
 		for (GridObserver observer : gridObservers) {
 			observer.gridSizeUpdated(getSize());
 		}
 	}
 
 	private void notifyGridUpdated() {
 		for (GridObserver observer : gridObservers) {
 			observer.gridUpdated();
 		}
 	}
 
 	public int optimizeQuadRotation(int index) {
 		assert !readOnly;
 
 		int bestOrientation = 0;
 		int best_pairs = 0;
 		int x = index % getSize();
 		int y = index / getSize();
 
 		QuadModel quad = getQuad(index);
 
 		for (int i = 0; i < 4; i++) {
 			int pairs = countPairs(x, y, 4);
 
 			if (pairs > best_pairs) {
 				best_pairs = pairs;
 				bestOrientation = i;
 			}
 
 			quad.rotateClockwise();
 		}
 
 		quad.rotateClockwise(bestOrientation);
 
 		return bestOrientation;
 	}
 
 	public void swap(int indexA, int indexB) {
 		assert !readOnly;
 
 		QuadModel quadA = getQuad(indexA);
 		QuadModel quadB = getQuad(indexB);
 
 		QuadModel buffer = new QuadModel();
 		quadA.copyTo(buffer);
 		quadB.copyTo(quadA);
 		buffer.copyTo(quadB);
 	}
 
 	public QuadModel getMissingQuad(int index) {
 		int x = index % getSize();
 		int y = index / getSize();
 		return getMissingQuad(x, y);
 	}
 
 	public QuadModel getMissingQuad(int x, int y) {
 		QuadModel quad = new QuadModel();
 
 		for (int d = 0; d < 4; d++) {
 			QuadModel neighbor = getNeighbor(x, y, d);
 
 			Pattern missingPattern;
 
 			if (neighbor == null) {
 				missingPattern = defaultpat;
 			} else {
 				int oppD = (d + 2) % 4;
 				missingPattern = neighbor.getPattern(oppD);
 
 				if (missingPattern == defaultpat) {
 					missingPattern = null;
 				}
 			}
 
 			quad.setPattern(d, missingPattern);
 		}
 
 		return quad;
 	}
 
 	public List<Integer> getMatchingQuads(QuadModel missing, int degrees) {
 		List<Integer> matches = new ArrayList<Integer>();
 		int sides = missing.countPattern(defaultpat);
 
 		for (int i = 0; i < gridQuads.size(); i++) {
 			QuadModel q = gridQuads.get(i);
 			if (q.equalsIgnoreRotation(missing, degrees) && q.countPattern(defaultpat) == sides) {
 				matches.add(i);
 			}
 		}
 
 		return matches;
 	}
 
 	public PatternStats getPatternStats() {
 		SortedSet<Pattern> patterns = new TreeSet<Pattern>();
 		SortedSet<Pattern> innerPatterns = new TreeSet<Pattern>();
 		SortedSet<Pattern> outerPatterns = new TreeSet<Pattern>();
 		int duplicates = 0;
 
 		for (int y = 0; y < getSize(); y++) {
 			for (int x = 0; x < getSize(); x++) {
 				QuadModel quad = getQuad(x, y);
 
 				for (int dy = y; dy < getSize(); dy++) {
 					for (int dx = 0; dx < getSize(); dx++) {
 						if (dy > y || dx > x) {
 							QuadModel q2 = getQuad(dx, dy);
 							if (quad.equalsIgnoreRotation(q2)) {
 								duplicates += 1;
 							}
 						}
 					}
 				}
 
 				for (int d = 0; d < 4; d++) {
 					Pattern pattern = quad.getPattern(d);
 
 					if (pattern != defaultpat) {
 						patterns.add(pattern);
 
 						if ((x == 0 || x == getSize() - 1)
 								&& (d == QuadModel.DIR_NORTH || d == QuadModel.DIR_SOUTH)) {
 							outerPatterns.add(pattern);
 						} else if ((y == 0 || y == getSize() - 1)
 								&& (d == QuadModel.DIR_WEST || d == QuadModel.DIR_EAST)) {
 							outerPatterns.add(pattern);
 						} else {
 							innerPatterns.add(pattern);
 						}
 					}
 				}
 			}
 		}
 
 		return new PatternStats(patterns, outerPatterns, innerPatterns, duplicates);
 	}
 
 	public boolean isHSymetric() {
 		GridModel g2 = clone();
 		g2.flipHorizontal();
 		return equals(g2);
 	}
 
 	public boolean isVSymetric() {
 		GridModel g2 = clone();
 		g2.flipVertical();
 		return equals(g2);
 	}
 
 	public boolean isRSymetric() {
 		GridModel g2 = clone();
 		g2.rotateClockwise();
 		return equals(g2);
 	}
 
 	public int countExternalBorders(int index) {
 		int borders = countExternalBorders(index % getSize(), index / getSize());
 		return borders;
 	}
 
 	public int countExternalBorders(int x, int y) {
 		int borders = 0;
 
 		if (x == 0 || x == getSize() - 1) {
 			borders++;
 		}
 
 		if (y == 0 || y == getSize() - 1) {
 			borders++;
 		}
 
 		return borders;
 	}
 
 	public void rotateClockwise(int steps) {
 		assert !readOnly;
 
 		for (int i = 0; i < steps; i++) {
 			rotateClockwise();
 		}
 	}
 
 	public void rotateCounterclockwise(int steps) {
 		assert !readOnly;
 
 		for (int i = 0; i < steps; i++) {
 			rotateCounterclockwise();
 		}
 	}
 
 	public void rotateCounterclockwise() {
 		assert !readOnly;
 		rotateClockwise(3);
 	}
 
 	public void rotateClockwise() {
 		assert !readOnly;
 
 		List<QuadModel> originalQuads = new ArrayList<QuadModel>();
 		for (QuadModel quad : gridQuads) {
 			originalQuads.add(quad.clone());
 		}
 
 		for (int y = 0; y < getSize(); y++) {
 			for (int x = 0; x < getSize(); x++) {
 				QuadModel source = originalQuads.get(x + getSize() * y);
 				QuadModel destination = gridQuads.get((getSize() - 1 - y) + (getSize() * x));
 				source.copyTo(destination);
 				destination.rotateClockwise();
 			}
 		}
 	}
 
 	public void rotateCenterClockwise(int steps) {
 		assert !readOnly;
 
 		for (int i = 0; i < steps; i++) {
 			rotateCenterClockwise();
 		}
 	}
 
 	public void rotateCenterClockwise() {
 		assert !readOnly;
 
 		List<QuadModel> originalQuads = new ArrayList<QuadModel>();
 		for (QuadModel quad : gridQuads) {
 			originalQuads.add(quad.clone());
 		}
 
 		for (int y = getSize() / 2 - 1; y <= (getSize() + 1) / 2; y++) {
 			for (int x = getSize() / 2 - 1; x <= (getSize() + 1) / 2; x++) {
 				QuadModel source = originalQuads.get(x + getSize() * y);
 				QuadModel destination = gridQuads.get((getSize() - 1 - y) + (getSize() * x));
 				source.copyTo(destination);
 				destination.rotateClockwise();
 			}
 		}
 	}
 
 	public void rotateCenterCounterclockwise(int steps) {
 		assert !readOnly;
 
 		for (int i = 0; i < steps; i++) {
 			rotateCenterCounterclockwise();
 		}
 	}
 
 	public void rotateCenterCounterclockwise() {
 		assert !readOnly;
 		rotateCenterClockwise(3);
 	}
 
 	public void flipVertical() {
 		assert !readOnly;
 		rotateClockwise();
 		flipHorizontal();
 		rotateCounterclockwise();
 	}
 
 	public void flipHorizontal() {
 		assert !readOnly;
 		List<QuadModel> originalQuads = new ArrayList<QuadModel>();
 		for (QuadModel quad : gridQuads) {
 			originalQuads.add(quad.clone());
 		}
 
 		int size = getSize();
 
 		for (int y = 0; y < size; y++) {
 			for (int x = 0; x < size; x++) {
 				QuadModel source = originalQuads.get(x + size * y);
 				QuadModel destination = gridQuads.get((size - 1 - x) + (size * y));
 				source.copyTo(destination);
 				destination.flipHorizontal();
 			}
 		}
 	}
 
 	/*
 	 * GENETIC ALGORITHMS
 	 */
 
 	/*
 	 *	Counts the number of non-empty quads on this grid
 	 */
 	public int countFilledQuads() {
 		int count = 0;
 		for(int i = 0; i < this.getPositions(); i++)
 			if(!this.getQuad(i).isClear())
 				count++;
 		return count;
 	} // TESTED, OK
 
 	/*
 	 *	Counts the number of quads equivalent to the given quad on this grid
 	 */
 	public int countQuadOccurences(QuadModel q) {
 		int count = 0;
 
 		for(int i = 0; i < this.getPositions(); i++)
 			if(q.equalsIgnoreRotation(this.getQuad(i)))
 				count++;
 
 		return count;
 	} // TESTED, OK
 
 	/*
 	 *	Returns a Set of the positions that are not empty on this grid
 	 */
 	public TreeSet<Integer> getFilledPositions() {
 		TreeSet<Integer> positions = new TreeSet<Integer>();
 		for(int i = 0; i < this.getPositions(); i++)
 			if(!this.getQuad(i).isClear())
 				positions.add(i);
 		return positions;
 	} // TESTED, OK
 
 	/*
 	 * Given 2 incomplete GridModels, checks if there are no duplicate pieces in
 	 * both boards.
 	 * Assumes piece positions from "a" and "b" do not overlap.
 	 */
 	public boolean allPiecesValid(GridModel a, GridModel b) {
 		boolean allValid = true;
 
 		GridModel incomplete = GridModel.joinGrids(a, b);
 		
 		// if the occurences of the pieces in both "original" and "incomplete"
 		// differ, the features are incompatible
 		for(int i = 0; allValid && i < incomplete.getPositions(); i++) {
 			QuadModel q = incomplete.getQuad(i);
 
 			if(q.isClear()) continue;
 
 			if(this.countQuadOccurences(q) < incomplete.countQuadOccurences(q))
 				allValid = false;
 		}
 		return allValid;
 	}
 
 	/*
 	 * Joins two incomplete boards. Assumes "a" and "b" have no overlapping pieces
 	 */
 	public static GridModel joinGrids(GridModel a, GridModel b) {
 		GridModel join = a.clone();
 		for(int i = 0; i < b.getPositions(); i++) {
 			if(b.getQuad(i).isClear()) continue;
 			join.setQuad(i, b.getQuad(i));
 		}
 		return join;
 	}
 
 	/* given an incomplete board, return the pieces of the complete
 	 * board that can be used to complete the incomplete board
 	 */
 	public GridModel remainingPieces(GridModel incomplete) {
 		GridModel remaining = this.clone();
 		remaining.setReadOnly(false);
 
 		for(int i = 0; i < incomplete.getPositions(); i++) {
 			QuadModel qi = incomplete.getQuad(i);
 
 			if(!qi.isClear()) {
 				for(int j = 0; j < remaining.getPositions(); j++) {
 					QuadModel qr = remaining.getQuad(j);
 
 					if(qr.geneticEquals(qi)) {
 						qr.clear();
 						break;
 					}
 				}
 			}
 		}
 		
 		return remaining;
 	}
 
 	private int getTestScore(QuadModel testQuad, int index) {
 		int score = 0;
 		
 		for (int dir = 0; dir < 4; dir++) {
 			QuadModel neighbor = getNeighbor(index, dir);
 			if (neighbor != null && testQuad.getPattern(dir) == neighbor.getOppositePattern(dir))
 				score++;
 		}
 		
 		return score;
 	}
 			
 	public void completeWith(GridModel remaining) {
 
 		QuadModel current, candidate, bestQuad;
 		int bestScore;
 		int score;
		int candidatePos = 0; // for clearing
 		for(int i = 0; i < this.getPositions(); i++) {
 			current = this.getQuad(i);
 			if(!current.isClear()) continue;
 			
 			bestScore = -1;
 			bestQuad = null;
 			
 			// get best candidate piece from remaining
 			for(int c = 0; c < remaining.getPositions(); c++) {
 				candidate = remaining.getQuad(c).clone();
 				if(candidate.isClear()) continue;
 				
 				for (int d = 0; d < 4; d++) {
 					candidate.rotateClockwise();
 					score = getTestScore(candidate, i);
 					
 					if (score > bestScore) {
 						bestScore = score;
 						bestQuad = candidate;
						candidatePos = c;
 					}
 					
 				}
 			}
 			this.setQuad(i, bestQuad);
			remaining.getQuad(candidatePos).clear();
 		}
 	}
 
 	/*
 	 * Tries to return the best grid containing compatible features from A joined with B
 	 * If there is no compatible features in B, returns a grid with the best feature from A
 	 */
 	public GridModel getCompatibleFeatures(ArrayList<GridModel> featuresA, ArrayList<GridModel> featuresB) {
 		GridModel fa = null;
 
 		// select best feature from a
 		for(GridModel g : featuresA) {
 			if(fa == null) fa = g.clone();
 			if(g.countFilledQuads() > fa.countFilledQuads()) g.copyTo(fa);
 		}
 
 		// try to get a compatible feature from b
 		for(GridModel fb : featuresB) {
 
 			// are the positions compatible? (i.e., not overlapping?)
 			TreeSet<Integer> overlapping = fa.getFilledPositions();
 			overlapping.retainAll(fb.getFilledPositions());

			/*System.out.println(fa.toQuadString());
			System.out.println(fb.toQuadString());
			System.out.println(overlapping.size());
			System.out.println("\n-------------\n");*/

 			if(overlapping.size() == 0 && this.allPiecesValid(fa, fb)) {
 				return GridModel.joinGrids(fa,fb);
 			}
 		}
 
 		return fa;
 	}
 
 	// iterative approach
 	public ArrayList<GridModel> getFeatures() {
 
 		Set<Integer> visited = new TreeSet<Integer>();
 		ArrayList<GridModel> features = new ArrayList<GridModel>();
 
 		for(int i = 0; i < this.getPositions(); i++) {
 			if(!visited.contains(i)) {
 				GridModel feature = new GridModel(this.getSize());
 				getFeatures(i, feature, visited);
 				features.add(feature);
 			}
 		}
 		return features;
 	}
 
 	private void getFeatures(int index, GridModel feature, Set<Integer> visited) {
 
 		QuadModel current = this.getQuad(index);
 
 		feature.setQuad(index, current);
 
 		for(int dir = 0; dir < 4; dir++) {
 
 			QuadModel neighbor = this.getNeighbor(index, dir);
 			int neighborIndex = this.computeNeighborIndex(index, dir);
 
 			if((neighbor != null)) {
 				if(feature.getQuad(neighborIndex).isClear()) {
 					if(!visited.contains(neighborIndex)) {
 						if(current.getPattern(dir) == neighbor.getOppositePattern(dir)) {
 							getFeatures(neighborIndex, feature, visited);
 						}
 					}
 				}
 			}
 		}
 		visited.add(index);
 	}
 	
 }
 

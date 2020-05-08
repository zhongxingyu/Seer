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
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class QuadModel implements Cloneable, Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final int DIR_NORTH = 0;
 	public static final int DIR_EAST = 1;
 	public static final int DIR_SOUTH = 2;
 	public static final int DIR_WEST = 3;
 
 	private Pattern[] patterns;
 
 	private Set<QuadObserver> observers = new HashSet<QuadObserver>();
 	private Pattern defaultpat;
 	private boolean locked = false;
 	private boolean readOnly = false;
 
 	public QuadModel() {
 		defaultpat = Pattern.getDefaultPattern();
 		patterns = new Pattern[4];
 		patterns[0] = defaultpat;
 		patterns[1] = defaultpat;
 		patterns[2] = defaultpat;
 		patterns[3] = defaultpat;
 	}
 
 	public QuadModel(Pattern... initialPatterns) {
 		defaultpat = Pattern.getDefaultPattern();
 		patterns = new Pattern[4];
 		patterns[0] = initialPatterns[0];
 		patterns[1] = initialPatterns[1];
 		patterns[2] = initialPatterns[2];
 		patterns[3] = initialPatterns[3];
 	}
 
 	@Override
 	public QuadModel clone() {
 		QuadModel newQuad = new QuadModel();
 		copyTo(newQuad);
 		return newQuad;
 	}
 
 	public void copyTo(QuadModel q) {
 		copyTo(q, true);
 	}
 
 	protected void copyTo(QuadModel q, boolean notifyObservers) {
 		assert !q.readOnly;
 		q.patterns[DIR_NORTH] = patterns[DIR_NORTH];
 		q.patterns[DIR_EAST] = patterns[DIR_EAST];
 		q.patterns[DIR_SOUTH] = patterns[DIR_SOUTH];
 		q.patterns[DIR_WEST] = patterns[DIR_WEST];
 		q.locked = isLocked();
 
 		if (notifyObservers) {
 			q.notifyQuadUpdated();
 		}
 	}
 
 	public boolean isClear() {
 		return patterns[DIR_NORTH] == defaultpat && patterns[DIR_EAST] == defaultpat
 				&& patterns[DIR_SOUTH] == defaultpat && patterns[DIR_WEST] == defaultpat;
 	}
 
 	public boolean isNull() {
 		return patterns[DIR_NORTH] == null && patterns[DIR_EAST] == null
 				&& patterns[DIR_SOUTH] == null && patterns[DIR_WEST] == null;
 	}
 
 	public int countPattern(Pattern pattern) {
 		int result = 0;
 
 		if (patterns[0] == pattern) {
 			result++;
 		}
 
 		if (patterns[1] == pattern) {
 			result++;
 		}
 
 		if (patterns[2] == pattern) {
 			result++;
 		}
 
 		if (patterns[3] == pattern) {
 			result++;
 		}
 
 		return result;
 	}
 
 	public int countDefaultPattern() {
 		return countPattern(defaultpat);
 	}
 
 	public boolean isLocked() {
 		return locked;
 	}
 
 	public void setLocked(boolean l) {
 		if (locked != l) {
 			locked = l;
 			notifyQuadUpdated();
 		}
 	}
 
 	public void clear() {
 		assert !readOnly;
 		patterns[DIR_NORTH] = defaultpat;
 		patterns[DIR_SOUTH] = defaultpat;
 		patterns[DIR_WEST] = defaultpat;
 		patterns[DIR_EAST] = defaultpat;
 		setLocked(false);
 		notifyQuadUpdated();
 	}
 
 	public Pattern getPattern(int direction) {
 		return patterns[direction];
 	}
 
 	//
 	public Pattern getOppositePattern(int direction) {
 		return patterns[(direction + 2) % 4];
 	}
 
 	public void setPattern(int direction, Pattern pattern) {
 		assert !readOnly;
 		patterns[direction] = pattern;
 		notifyQuadUpdated();
 	}
 
 	public void swap(QuadModel other) {
 		assert !readOnly;
 		assert !other.readOnly;
 		Pattern[] my_old_patterns = patterns;
 		patterns = other.patterns;
 		other.patterns = my_old_patterns;
 
 		notifyQuadUpdated();
 		other.notifyQuadUpdated();
 	}
 
 	public void rotatePattern(int direction) {
 		assert !readOnly;
 		rotatePattern(direction, true);
 	}
 
 	public void rotatePattern(int direction, boolean forward) {
 		assert !readOnly;
 
 		List<Pattern> allpats = Pattern.getAllPatterns();
 
 		Pattern pat = getPattern(direction);
 		int patix = allpats.indexOf(pat);
 
 		int offset = 1;
 
 		if (!forward) {
 			offset = -1;
 		}
 
 		setPattern(direction, allpats.get((patix + offset + allpats.size()) % allpats.size()));
 	}
 
 	public void rotateClockwise() {
 		assert !readOnly;
 
 		// Rotate array to the right
 		Pattern buf = patterns[0];
 		patterns[0] = patterns[3];
 		patterns[3] = patterns[2];
 		patterns[2] = patterns[1];
 		patterns[1] = buf;
 		notifyQuadUpdated();
 	}
 
 	public void rotateClockwise(int steps) {
 		assert !readOnly;
 
 		int stepsLeft = steps;
 		while (stepsLeft-- > 0) {
 			rotateClockwise();
 		}
 	}
 
 	public void rotateCounterclockwise() {
 		assert !readOnly;
 
 		// Rotate array to the left
 		Pattern buf = patterns[0];
 		patterns[0] = patterns[1];
 		patterns[1] = patterns[2];
 		patterns[2] = patterns[3];
 		patterns[3] = buf;
 		notifyQuadUpdated();
 	}
 
 	public void rotateCounterclockwise(int steps) {
 		assert !readOnly;
 
 		int stepsLeft = steps;
 		while (stepsLeft-- > 0) {
 			rotateCounterclockwise();
 		}
 	}
 
 	public void addQuadObserver(QuadObserver observer) {
 		observers.add(observer);
 	}
 
 	public void removeQuadObserver(QuadObserver observer) {
 		observers.remove(observer);
 	}
 
 	private void notifyQuadUpdated() {
 		if (observers.size() > 0) {
 			for (QuadObserver observer : observers) {
 				observer.quadUpdated();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object o) {
 		boolean result = false;
 
 		if (o instanceof QuadModel) {
 			QuadModel oQuad = (QuadModel) o;
 			result = locked == oQuad.locked;
 			result = result && Arrays.deepEquals(patterns, oQuad.patterns);
 		} else {
 			result = false;
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return Arrays.hashCode(patterns);
 	}
 
 	/**
 	 * Compute the maximum number of matching sides.
 	 * 
 	 * @param quad
 	 *            The quad to compare
 	 * @return the number of matches.
 	 */
 	public int matchDegrees(QuadModel quad) {
 		int matchDegrees = 0;
 
 		for (int rot = 0; rot < 4; rot++) {
 			int degrees = 0;
 			for (int a = 0; a < 4; a++) {
 				Pattern pat1 = patterns[(a + rot) % 4];
 				Pattern pat2 = quad.patterns[a];
 				if (pat1 == null || pat2 == null || pat1 == pat2) {
 					degrees += 1;
 				}
 			}
 
 			matchDegrees = Math.max(matchDegrees, degrees);
 		}
 
 		return matchDegrees;
 	}
 
 	public boolean matches(QuadModel quad) {
 		boolean result = true;
 
 		for (int a = 0; result && a < 4; a++) {
 			Pattern pat1 = patterns[a];
 			Pattern pat2 = quad.patterns[a];
 			if (pat1 != null && pat2 != null && pat1 != pat2) {
 				result = false;
 			}
 		}
 
 		return result;
 	}
 
 	public boolean equalsIgnoreRotation(QuadModel quad) {
 		return equalsIgnoreRotation(quad, 4);
 	}
 
 	public boolean equalsIgnoreRotation(QuadModel quad, int degrees) {
 		return matchDegrees(quad) >= degrees;
 	}
 
 	//
 	public boolean geneticEquals(QuadModel quad) {
 
		QuadModel compare = this.clone();
 
		for(int d = 0; d < 4; d++) {
			if(compare.getPattern(0) == quad.getPattern(0)
			&& compare.getPattern(1) == quad.getPattern(1)
			&& compare.getPattern(2) == quad.getPattern(2)
			&& compare.getPattern(3) == quad.getPattern(3)) {
 				return true;
 			}
 			compare.rotateClockwise();
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "Q[" + getPattern(0) + "," + getPattern(1) + "," + getPattern(2) + ","
 				+ getPattern(3) + "]";
 	}
 
 	public void flipHorizontal() {
 		assert !readOnly;
 
 		Pattern w = patterns[DIR_WEST];
 		patterns[DIR_WEST] = patterns[DIR_EAST];
 		patterns[DIR_EAST] = w;
 	}
 
 	public void flipVertical() {
 		assert !readOnly;
 
 		Pattern n = patterns[DIR_NORTH];
 		patterns[DIR_NORTH] = patterns[DIR_SOUTH];
 		patterns[DIR_SOUTH] = n;
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	public String save() {
 		return String.format("%d %d %d %d", getPattern(0).getCode(), getPattern(1).getCode(),
 				getPattern(2).getCode(), getPattern(3).getCode());
 	}
 
 	public void load(String text) {
 		String[] patternStrings = text.split(" ");
 		for (int i = 0; i < 4; i++) {
 			int patternCode = Integer.parseInt(patternStrings[i]);
 			Pattern pattern = Pattern.getPatternByCode(patternCode);
 			setPattern(i, pattern);
 		}
 	}
 }

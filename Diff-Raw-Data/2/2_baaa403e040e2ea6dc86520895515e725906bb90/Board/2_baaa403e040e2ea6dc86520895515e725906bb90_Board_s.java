 package com.ankurdave.boggle;
 import java.util.Arrays;
 import java.util.HashSet;
 public class Board implements Comparable<Board> {
 	class Letter {
 		private char data;
 		private boolean hasBeenHit = false;
 		private int X;
 		private int Y;
 		public Letter(char data, int X, int Y) {
 			this.data = data;
 			this.X = X;
 			this.Y = Y;
 		}
 		@Override public Letter clone() {
 			Letter thisClone = new Letter(data, X, Y);
 			thisClone.setHasBeenHit(hasBeenHit);
 			return thisClone;
 		}
 		@Override public boolean equals(Object o) {
 			Letter that = (Letter) o;
 			if (this.getData() == that.getData() && this.getX() == that.getX()
 			        && this.getY() == that.getY()) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		public char getData() {
 			return data;
 		}
 		public boolean getHasBeenHit() {
 			return hasBeenHit;
 		}
 		public int getX() {
 			return X;
 		}
 		public int getY() {
 			return Y;
 		}
 		public void setHasBeenHit(boolean hasBeenHit) {
 			this.hasBeenHit = hasBeenHit;
 		}
 		@Override public String toString() {
 			return "Letter[" + "data=" + getData() + "; " + "X=" + getX()
 			        + "; " + "Y=" + getY() + "; " + "hasBeenHit="
 			        + getHasBeenHit() + "]";
 		}
 		/**
 		 * Recursively traverses the board, starting from this Letter.
 		 * @param soFar the String that has been accumulated so far (for use in
 		 * recursive traversal)
 		 * @param parentNode the dictionary letter node associated with the
 		 * parent of this Letter (allows dictionary lookup)
 		 */
 		public void traverse(String soFar, Dictionary.Letter parentNode) {
 			// don't traverse if this has already been used
 			if (hasBeenHit) { return; }
 			
 			soFar += data;
 			
 			// Verify that the current Letter is a valid part of a word
 			Dictionary.Letter currentNode = parentNode.getChild(data);
 			if (currentNode == null) {
 				return;
 			}
 			
 			// Add to the list of found words if appropriate
			if (soFar.length() > 2 && parentNode.getEndsWord()) {
 				words.add(soFar);
 			}
 			
 			// Make sure we don't traverse back onto this node during recursion
 			hasBeenHit = true;
 			
 			// Do the recursive traversal
 			// Letter above
 			if (Y - 1 >= 0 && Y - 1 < sideLength) {
 				board[X][Y - 1].traverse(soFar, currentNode);
 			}
 			// Letter below
 			if (Y + 1 >= 0 && Y + 1 < sideLength) {
 				board[X][Y + 1].traverse(soFar, currentNode);
 			}
 			// Letter right
 			if (X + 1 >= 0 && X + 1 < sideLength) {
 				board[X + 1][Y].traverse(soFar, currentNode);
 			}
 			// Letter left
 			if (X - 1 >= 0 && X - 1 < sideLength) {
 				board[X - 1][Y].traverse(soFar, currentNode);
 			}
 			// Letter up-left
 			if (X - 1 >= 0 && X - 1 < sideLength && Y - 1 >= 0
 			        && Y - 1 < sideLength) {
 				board[X - 1][Y - 1].traverse(soFar, currentNode);
 			}
 			// Letter up-right
 			if (X + 1 >= 0 && X + 1 < sideLength && Y - 1 >= 0
 			        && Y - 1 < sideLength) {
 				board[X + 1][Y - 1].traverse(soFar, currentNode);
 			}
 			// Letter down-left
 			if (X - 1 >= 0 && X - 1 < sideLength && Y + 1 >= 0
 			        && Y + 1 < sideLength) {
 				board[X - 1][Y + 1].traverse(soFar, currentNode);
 			}
 			// Letter down-right
 			if (X + 1 >= 0 && X + 1 < sideLength && Y + 1 >= 0
 			        && Y + 1 < sideLength) {
 				board[X + 1][Y + 1].traverse(soFar, currentNode);
 			}
 			// now that this word attempt has finished, it's OK for other
 			// letters to traverse onto this one
 			hasBeenHit = false;
 		}
 	}
 	private int age = 1;
 	private Letter[][] board;
 	private Dictionary dict;
 	private char[][] grid;
 	private int score;
 	private int sideLength;
 	private HashSet<String> words = new HashSet<String>();
 	public Board(char[][] grid, Dictionary dict) {
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		assert dict != null;
 		sideLength = grid.length;
 		this.grid = grid;
 		this.dict = dict;
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				board[i][j] = new Letter(grid[i][j], i, j);
 			}
 		}
 	}
 	public Board(char[][] grid, String path) {
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		sideLength = grid.length;
 		this.grid = grid;
 		dict = new Dictionary();
 		dict.buildDictionary(path);
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				board[i][j] = new Letter(grid[i][j], i, j);
 			}
 		}
 	}
 	public Board(String s, int sideLength, Dictionary dict) {
 		// TODO: error handling
 		String[] parts = s.split(" ", 2);
 		String gridS = parts[0];
 		int score = Integer.parseInt(parts[1]);
 		char[][] grid = new char[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				grid[i][j] = gridS.charAt(i * sideLength + j);
 			}
 		}
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		this.score = score;
 		this.grid = grid;
 		this.sideLength = sideLength;
 		this.dict = dict;
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				board[i][j] = new Letter(grid[i][j], i, j);
 			}
 		}
 	}
 	@Override public Board clone() {
 		Letter[][] boardClone = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				boardClone[i][j] = board[i][j].clone();
 			}
 		}
 		Board thisClone = new Board(grid, dict);
 		return thisClone;
 	}
 	public int compareTo(Board that) {
 		if (this.getScore() > that.getScore()) {
 			return 1;
 		} else if (this.getScore() < that.getScore()) {
 			return -1;
 		} else {
 			return 0;
 		}
 	}
 	/**
      * Traverses the Boggle board, makes a list of words, and finds the score.
      *
      */
 	public void generate() { /*@ \label{Board.java:generate} @*/
 		// on each of the letters of the board
 		// traverse the possible words recursively
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				board[i][j].traverse("", dict.getRoot());
 			}
 		}
 		int score = 0;
 		Object[] words = getWords().toArray();
 		for (Object word : words) {
 			String wordString = (String) word;
 			int length = wordString.length();
 			// minimum length is 3
 			if (length < 3) {
 				continue;
 			}
 			// calculate score
 			if (length == 3 || length == 4) {
 				score += 1;
 			} else if (length == 5) {
 				score += 2;
 			} else if (length == 6) {
 				score += 3;
 			} else if (length == 7) {
 				score += 5;
 			} else if (length >= 8) {
 				score += 11;
 			}
 		}
 		this.score = score;
 	}
 	public int getAge() {
 		return age;
 	}
 	public Dictionary getDict() {
 		return dict;
 	}
 	// TODO make this automatically call generate() if score is not set. Then make generate private
 	public int getScore() {
 		return score;
 	}
 	public int getSideLength() {
 		return sideLength;
 	}
 	public HashSet<String> getWords() {
 		return words;
 	}
 	public String[] getWordsSorted() {
 		String[] wordsArray = (String[]) words.toArray(new String[words.size()]);
 		Arrays.sort(wordsArray, new ByStringLength());
 		// convert to array
 		return wordsArray;
 	}
 	public String gridToString() {
 		String s = "";
 		for (char c[] : grid) {
 			for (char d : c) {
 				s += d;
 			}
 		}
 		return s;
 	}
 	public void incrementAge() {
 		age++;
 	}
 	/**
      * Merges two Boggle boards randomly.<BR>
      * Calculates the score of each board and on each character in the grid,
      * chooses randomly between three choices for the child:
      * <UL>
      * <LI>use the character from the higher-scoring grid (weighted 6.6/10, or
      * 6/10 if incestuous)
      * <LI>use the character from the lower-scoring grid (weighted 3.3/10, or
      * 3/10 if incestuous)
      * <LI>use a random character (weighted 0.1/10, or 1/10 if incestuous)
      * </UL>
      * @param that Boggle board to merge with the calling board
      * @return the child board
      */
 	public Board merge(Board that) { /*@ \label{Board.java:merge} @*/
 		if (this.sideLength != that.sideLength) { return null; }
 		// init child
 		char[][] childGrid = new char[sideLength][sideLength];
 		// determine which one is higher or lower
 		Board higher;
 		Board lower;
 		// caller is higher
 		if (this.getScore() > that.getScore()) {
 			higher = this;
 			lower = that;
 		}
 		// parameter is higher
 		else if (that.getScore() < this.getScore()) {
 			higher = that;
 			lower = this;
 		}
 		// they are equal; choose randomly
 		else {
 			if ((int) (Math.random() * 2) == 0) {
 				higher = this;
 				lower = that;
 			} else {
 				higher = that;
 				lower = this;
 			}
 		}
 		// check if the parents are too similar /*@ \label{Board.java:incest} @*/
 		int sameLetters = 0;
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				if (higher.grid[i][j] == lower.grid[i][j]) {
 					sameLetters++;
 				}
 			}
 		}
 		// if they are, mark it as incestuous
 		boolean incest = (float) sameLetters / (sideLength * sideLength) >= 0.85;
 		double higherChance = 6.6, lowerChance = 3.3;
 		if (incest) {
 			higherChance = 6;
 			lowerChance = 3;
 		}
 		// construct the child grid
 		double temp;
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				temp = Math.random() * 10; // 0-9.9
 				// higher
 				if (temp >= 0 && temp < higherChance) {
 					childGrid[i][j] = higher.grid[i][j];
 				} else if (temp >= higherChance
 				        && temp < (higherChance + lowerChance)) {
 					// 6-9
 					childGrid[i][j] = lower.grid[i][j];
 				} else {
 					// 9.9-10 or 9-10
 					childGrid[i][j] = randomLetter();
 				}
 			}
 		}
 		// make the child board
 		Board child = new Board(childGrid, dict);
 		return child;
 	}
 	public Board mutate(int mutationProbability) {
 		assert mutationProbability >= 0 && mutationProbability <= 100;
 		char[][] gridMutated = new char[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				if ((int) (Math.random() * 100) < mutationProbability) {
 					gridMutated[i][j] = randomLetter();
 				} else {
 					gridMutated[i][j] = grid[i][j];
 				}
 			}
 		}
 		Board thisMutated = new Board(gridMutated, dict);
 		return thisMutated;
 	}
 	@Override public String toString() {
 		return gridToString() + " " + getScore();
 	}
 	private char randomLetter() {
 		return (char) (Math.random() * (90 - 65 + 1) + 65);
 	}
 }

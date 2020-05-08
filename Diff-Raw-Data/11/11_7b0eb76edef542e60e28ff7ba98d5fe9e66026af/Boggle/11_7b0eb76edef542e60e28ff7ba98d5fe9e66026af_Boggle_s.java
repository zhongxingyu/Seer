 import java.util.Arrays;
 import java.util.HashSet;
 
 /**
  * Solves a Boggle board.
  * 
  * @author ankur
  */
 public class Boggle implements Comparable
 {
 	/**
 	 * Length of one side of the Boggle board.
 	 */
 	private int sideLength;
 	/**
 	 * Total score of all words found in the Boggle board.
 	 */
 	private int score;
 	/**
 	 * Simple grid of characters that makes the basic information of a Boggle
 	 * board.
 	 */
 	private char[][] grid;
 	/**
 	 * "Smart" grid of Letters that can generate a word list.
 	 */
 	private Letter[][] board;
 	/**
 	 * Unordered list of words found in the Boggle board.
 	 */
 	private HashSet<String> words = new HashSet<String>();
 	/**
 	 * Dictionary to check possible words against.
 	 */
 	private Dictionary dict;
 
 	/**
 	 * @param grid
 	 *        square array of characters from which to construct the board
 	 * @param path
 	 *        path to newline-separated dictionary file
 	 */
 	public Boggle(char[][] grid, String path)
 	{
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		// copy params to fields
 		sideLength = grid.length;
 		this.grid = grid;
 		// init dictionary
 		dict = new Dictionary();
 		dict.buildDictionary(path);
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 			for (int j = 0; j < sideLength; j++)
 				board[i][j] = new Letter(grid[i][j], i, j);
 	}
 
 	/**
 	 * @param grid
 	 *        square array of characters from which to construct the board
 	 * @param dict
 	 *        pre-filled dictionary
 	 */
 	public Boggle(char[][] grid, Dictionary dict)
 	{
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		assert dict != null;
 		// copy params to fields
 		sideLength = grid.length;
 		this.grid = grid;
 		this.dict = dict;
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 			for (int j = 0; j < sideLength; j++)
 				board[i][j] = new Letter(grid[i][j], i, j);
 	}
 	
	public Boggle(String s, int sideLength, Dictionary dict) {
 		// todo: error handling
 		
 		char[][] grid = new char[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 		{
 			for (int j = 0; j < sideLength; j++)
 			{
 				grid[i][j] = s.charAt(i * sideLength + j);
 			}
 		}
 		
 		assert grid.length == grid[0].length;
 		assert grid.length > 0;
 		assert dict != null;
 		// copy params to fields
 		this.grid = grid;
 		this.dict = dict;
 		this.sideLength = sideLength;
 		// make board from grid
 		board = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 			for (int j = 0; j < sideLength; j++)
 				board[i][j] = new Letter(grid[i][j], i, j);
 	}
 
 	/**
 	 * Merges two Boggle boards randomly.<BR>
 	 * Calculates the score of each board and on each character in the grid,
 	 * chooses randomly between three choices for the child:
 	 * <UL>
 	 * <LI>use the character from the higher-scoring grid (weighted 5/10)
 	 * <LI>use the character from the lower-scoring grid (weighted 4/10)
 	 * <LI>use a random character (weighted 1/10)
 	 * </UL>
 	 * 
 	 * @param that
 	 *        Boggle board to merge with the calling board
 	 * @return the child board
 	 */
 	public Boggle merge(Boggle that)
 	{
 		if (this.sideLength != that.sideLength) return null;
 		// init child
 		char[][] childGrid = new char[sideLength][sideLength];
 		// determine which one is higher or lower
 		Boggle higher;
 		Boggle lower;
 		// caller is higher
 		if (this.getScore() > that.getScore())
 		{
 			higher = this;
 			lower = that;
 		}
 		// parameter is higher
 		else if (that.getScore() < this.getScore())
 		{
 			higher = that;
 			lower = this;
 		}
 		// they are equal; choose randomly
 		else
 		{
 			if ((int) (Math.random() * 2) == 0)
 			{
 				higher = this;
 				lower = that;
 			}
 			else
 			{
 				higher = that;
 				lower = this;
 			}
 		}
 		// construct the child grid
 		int temp;
 		for (int i = 0; i < sideLength; i++)
 		{
 			for (int j = 0; j < sideLength; j++)
 			{
 				temp = (int) (Math.random() * 10); // 0-9
 				// higher
 				if (temp >= 0 && temp < 5) // 0-4
 				childGrid[i][j] = higher.grid[i][j];
 				// lower
 				else if (temp >= 5 && temp < 9) // 5-8
 				childGrid[i][j] = lower.grid[i][j];
 				else
 				// 9
 				childGrid[i][j] = randomLetter();
 			}
 		}
 		// make the child board
 		Boggle child = new Boggle(childGrid, dict);
 		return child;
 	}
 
 	/**
 	 * Traverses the Boggle board, makes a list of words, and finds the score.
 	 */
 	public void generate()
 	{
 		// on each of the letters of the board
 		// traverse the possible words recursively
 		for (int i = 0; i < sideLength; i++)
 			for (int j = 0; j < sideLength; j++)
 				board[i][j].traverse("");
 		int score = 0;
 		Object[] words = getWords().toArray();
 		for (Object word : words)
 		{
 			String wordString = (String)word;
 			int length = wordString.length();
 			// minimum length is 3
 			if (length < 3) continue;
 			// calculate score
 			if (length == 3 || length == 4) score += 1;
 			else if (length == 5) score += 2;
 			else if (length == 6) score += 3;
 			else if (length == 7) score += 5;
 			else if (length >= 8) score += 11;
 		}
 		this.score = score;
 	}
 
 	/**
 	 * Randomly mutates some of the characters.
 	 * 
 	 * @param mutationProbability
 	 *        the percent probability that each character will be mutated
 	 * @return the mutated board
 	 */
 	public Boggle mutate(int mutationProbability)
 	{
 		assert mutationProbability >= 0 && mutationProbability <= 100;
 		char[][] gridMutated = new char[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 		{
 			for (int j = 0; j < sideLength; j++)
 			{
 				if ((int) (Math.random() * 100) < mutationProbability) gridMutated[i][j] = randomLetter();
 				else gridMutated[i][j] = grid[i][j];
 			}
 		}
 		Boggle thisMutated = new Boggle(gridMutated, dict);
 		return thisMutated;
 	}
 
 	/**
 	 * @return side length of the board
 	 */
 	public int getSideLength()
 	{
 		return sideLength;
 	}
 
 	/**
 	 * @return the list of words in the board
 	 */
 	public HashSet<String> getWords()
 	{
 		return words;
 	}
 
 	/**
 	 * @return the sorted list of words in the board
 	 */
 	public Object[] getWordsSorted()
 	{
 		Object[] words = this.words.toArray();
 		Arrays.sort(words, new ByStringLength());
 		// convert to array
 		return words;
 	}
 
 	/**
 	 * @return the score of the board
 	 */
 	public int getScore()
 	{
 		return score;
 	}
 
 	/**
 	 * @return the Dictionary used in generating the words in this board
 	 */
 	public Dictionary getDict()
 	{
 		return dict;
 	}
 
 	/**
 	 * @return a random letter in the range A-Z
 	 */
 	private char randomLetter()
 	{
 		return (char) (Math.random() * (90 - 65 + 1) + 65);
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		String s = "Boggle[" + "sideLength=" + sideLength + "; " + "score="
 			+ getScore() + "; " + "grid=";
 		for (char c[] : grid)
 		{
 			for (char d : c)
 			{
 				s += d;
 			}
 		}
 		s += "]";
 		return s;
 	}
 	
 	public String gridToString() {
 		String s = "";
 		for (char c[] : grid)
 		{
 			for (char d : c)
 			{
 				s += d;
 			}
 		}
 		return s;
 	}
 	
 	/**
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	public int compareTo(Object o)
 	{
 		Boggle that = (Boggle)o;
 		if (this.getScore() > that.getScore()) return 1;
 		else if (this.getScore() < that.getScore()) return -1;
 		else return 0;
 	}
 
 	/**
 	 * @see java.lang.Object#clone()
 	 */
 	public Boggle clone()
 	{
 		Letter[][] boardClone = new Letter[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++)
 		{
 			for (int j = 0; j < sideLength; j++)
 			{
 				boardClone[i][j] = board[i][j].clone();
 			}
 		}
 		Boggle thisClone = new Boggle(grid, dict);
 		return thisClone;
 	}
 
 	/**
 	 * Represents each letter of a Boggle board.
 	 * 
 	 * @author ankur
 	 */
 	class Letter
 	{
 		/**
 		 * Character data that the Letter represents.
 		 */
 		private char data;
 		/**
 		 * X position in the Boggle board.
 		 */
 		private int X;
 		/**
 		 * Y position in the Boggle board.
 		 */
 		private int Y;
 		/**
 		 * Whether this Letter has been used already in the current word.
 		 */
 		private boolean hasBeenHit = false;
 
 		/**
 		 * @param data
 		 *        character that the Letter represents
 		 * @param X
 		 *        horizontal position in the Boggle board
 		 * @param Y
 		 *        vertical position in the Boggle board
 		 */
 		public Letter(char data, int X, int Y)
 		{
 			this.data = data;
 			this.X = X;
 			this.Y = Y;
 		}
 
 		/**
 		 * @return character represented by this Letter
 		 */
 		public char getData()
 		{
 			return data;
 		}
 
 		/**
 		 * @return X position of this Letter in the Boggle board
 		 */
 		public int getX()
 		{
 			return X;
 		}
 
 		/**
 		 * @return Y position of this Letter in the Boggle board
 		 */
 		public int getY()
 		{
 			return Y;
 		}
 
 		/**
 		 * @return whether this Letter has been used in a word before
 		 */
 		public boolean getHasBeenHit()
 		{
 			return hasBeenHit;
 		}
 
 		/**
 		 * @param hasBeenHit
 		 *        new value for hasBeenHit
 		 */
 		public void setHasBeenHit(boolean hasBeenHit)
 		{
 			this.hasBeenHit = hasBeenHit;
 		}
 
 		/**
 		 * Traverses the Boggle board recursively.
 		 * 
 		 * @param soFar
 		 *        string found so far in the traversal
 		 */
 		public void traverse(String soFar)
 		{
 			// don't traverse if this has already been used
 			if (hasBeenHit) return;
 			soFar += data;
 			// don't traverse deeper if it doesn't begin a word so far
 			if (!dict.beginsWord(soFar.toLowerCase())) return;
 			// only add it to the found words if it's longer than 2 chars and is
 			// a word
 			if (soFar.length() > 2 && dict.isWord(soFar.toLowerCase()))
 			{
 				words.add(soFar);
 			}
 			// mark this Letter as already used so adjacent Letters don't
 			// traverse back onto it
 			hasBeenHit = true;
 			// traverse each Letter around this one recursively
 			// Letter above
 			if (Y - 1 >= 0 && Y - 1 < sideLength)
 				board[X][Y - 1].traverse(soFar);
 			// Letter below
 			if (Y + 1 >= 0 && Y + 1 < sideLength)
 				board[X][Y + 1].traverse(soFar);
 			// Letter right
 			if (X + 1 >= 0 && X + 1 < sideLength)
 				board[X + 1][Y].traverse(soFar);
 			// Letter left
 			if (X - 1 >= 0 && X - 1 < sideLength)
 				board[X - 1][Y].traverse(soFar);
 			// Letter up-left
 			if (X - 1 >= 0 && X - 1 < sideLength && Y - 1 >= 0
 				&& Y - 1 < sideLength) board[X - 1][Y - 1].traverse(soFar);
 			// Letter up-right
 			if (X + 1 >= 0 && X + 1 < sideLength && Y - 1 >= 0
 				&& Y - 1 < sideLength) board[X + 1][Y - 1].traverse(soFar);
 			// Letter down-left
 			if (X - 1 >= 0 && X - 1 < sideLength && Y + 1 >= 0
 				&& Y + 1 < sideLength) board[X - 1][Y + 1].traverse(soFar);
 			// Letter down-right
 			if (X + 1 >= 0 && X + 1 < sideLength && Y + 1 >= 0
 				&& Y + 1 < sideLength) board[X + 1][Y + 1].traverse(soFar);
 			// now that this word attempt has finished, it's OK for other
 			// letters to traverse onto this one
 			hasBeenHit = false;
 		}
 
 		/**
 		 * @see java.lang.Object#clone()
 		 */
 		public Letter clone()
 		{
 			Letter thisClone = new Letter(data, X, Y);
 			thisClone.setHasBeenHit(hasBeenHit);
 			return thisClone;
 		}
 
 		/**
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		public boolean equals(Object o)
 		{
 			Letter that = (Letter)o;
 			if (this.getData() == that.getData() && this.getX() == that.getX()
 				&& this.getY() == that.getY()) return true;
 			else return false;
 		}
 
 		/**
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString()
 		{
 			return "Letter[" + "data=" + getData() + "; " + "X=" + getX()
 				+ "; " + "Y=" + getY() + "; " + "hasBeenHit=" + getHasBeenHit()
 				+ "]";
 		}
 	}
 }

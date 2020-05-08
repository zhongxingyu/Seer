 package board;
 
 import java.util.HashSet;
 
 import dictionary.Alphabet;
 import dictionary.Trie;
 
 /**
  * Class for representing a square on the board. Each square has a maximum of
  * four neighbors, less if the square happens to be located near an edge. The
  * square keeps track of where on the board it is located, what content it is
  * carrying, and its neighbors. Each square also has a cross-check set. Which is
  * the set of legal characters to be placed. This set changes as the surrounding
  * does, since a word has to be formed in whatever direction.
  * 
  * @author diana
  * 
  */
 public class Square {
 	static final char INVALID_SQUARE = (char) -1;
 	static final char BUSY_SQUARE = 0;
 	public static final char EMPTY = 48;
 	public static final char WALL = 48;
 	public static final char TWO_LETTER_BONUS = 49;
 	public static final char THREE_LETTER_BONUS = 50;
 	public static final char TWO_WORD_BONUS = 51;
 	public static final char THREE_WORD_BONUS = 52;
 	public static final char CENTER_SQUARE = 53;
 
 	private char content;
 	private int row;
 	private int column;
 
 	private boolean isAnchor;
 
 	private HashSet<Character> crosschecks;
 	Square nextLeft;
 	Square nextRight;
 	Square nextUp;
 	Square nextDown;
 
 	public Square(char content, int row, int column) {
 		this.crosschecks = new HashSet<Character>();
 		this.content = content;
 		this.row = row;
 		this.column = column;
 		initCrossCheck();
 	}
 
 	/**
 	 * Set the four neighbors. This to make it easier to "walk the board" when
 	 * searching for possible moves.
 	 * 
 	 * @param left
 	 * @param right
 	 * @param up
 	 * @param down
 	 */
 	public void setNeighbours(Square left, Square right, Square up, Square down) {
 
 		this.nextLeft = left;
 		this.nextRight = right;
 		this.nextUp = up;
 		this.nextDown = down;
 
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!(obj instanceof Square)) {
 			return false;
 		}
 
 		Square sq = (Square) obj;
 
 		return this.row == sq.getRow() && this.column == sq.getColumn();
 	}
 
 	@Override
 	public int hashCode() {
 		String hashy = Integer.toString(this.row) + Integer.toString(this.column);
 		return Integer.parseInt(hashy);
 	}
 
 	/**
 	 * Set content of this square.
 	 * 
 	 * @param content
 	 */
 	public void setContent(char content, boolean transposed) {
 		this.content = content;
 		isAnchor = false;
 
 		if (this.containsLetter()) {
 			Square neighbor;
 			if (!(neighbor = this.getNextDown(transposed)).containsLetter())
 				neighbor.setAnchor(true);
 			if (!(neighbor = this.getNextUp(transposed)).containsLetter())
 				neighbor.setAnchor(true);
 			if (!(neighbor = this.getNextLeft(transposed)).containsLetter())
 				neighbor.setAnchor(true);
 			if (!(neighbor = this.getNextRight(transposed)).containsLetter())
 				neighbor.setAnchor(true);
 		}
 	}
 
 	/**
 	 * Calculate which letters are legal to place on this square. This depends
 	 * on the neighbors, since a word has to be formed in any direction.
 	 */
 	public void calculateCrossCheckSet(boolean transposed) {
 		String wordDownwards = "";
 		String wordUpwards = "";
 
 		Square downSquare = getNextDown(transposed);
 		Square upSquare = getNextUp(transposed);
 
 		if (downSquare != null && downSquare.containsLetter()) {
 			wordDownwards = verticalWord("", downSquare, true, transposed);
 		}
 
 		if (upSquare != null && upSquare.containsLetter()) {
 			wordUpwards = verticalWord("", upSquare, false, transposed);
 		}
 
 		crosschecks.clear();
 		if (wordUpwards.equals("") && wordDownwards.equals("")) {
 			initCrossCheck();
 		} else {
 			for (int i = 0; i < Alphabet.alphabet.length; i++) {
 				char letter = Alphabet.alphabet[i];
 
 				String checkWord = wordUpwards + letter + wordDownwards;
 				if (Trie.findWord(checkWord))
 					crosschecks.add(Alphabet.alphabet[i]);
 			}
 		}
 	}
 
 	public void initCrossCheck() {
 		for (int i = 0; i < Alphabet.alphabet.length; i++) {
 			crosschecks.add(Alphabet.alphabet[i]);
 		}
 	}
 
 	/**
 	 * Help method for calculating cross check set. Find which vertical word is
 	 * already in place, to later see what extensions can be made in any
 	 * direction of the found word.
 	 * 
 	 * @param word
 	 * @param square
 	 * @param wordDown
 	 * @return
 	 */
 	private String verticalWord(String word, Square square, boolean wordDown,
 			boolean transposed) {
 		if (square == null || !square.containsLetter())
 			return word;
 
 		char letter = square.getContent();
 		if (transposed) {
 		}
		if (!wordDown)
			return (transposed) ? letter
					+ verticalWord(word, square.getNextUp(transposed),
							wordDown, transposed) : verticalWord(word,
					square.getNextUp(transposed), wordDown, transposed)
					+ letter;
		else
			return (transposed) ? verticalWord(word,
					square.getNextDown(transposed), wordDown, transposed)
					+ letter : letter
 					+ verticalWord(word, square.getNextDown(transposed),
 							wordDown, transposed);
 	}
 
 	/**
 	 * Check if this square contains a letter.
 	 * 
 	 * @return True if letter is placed on this square.
 	 */
 	public boolean containsLetter() {
 		return content > CENTER_SQUARE;
 	}
 
 	/**
 	 * Check if square is occupied.
 	 * 
 	 * @return true if empty square
 	 */
 	public boolean isEmpty() {
 		return !containsLetter();
 	}
 
 	/**
 	 * Get content of square, can be either a letter, bonus or just an empty
 	 * normal square.
 	 * 
 	 * @return
 	 */
 	public char getContent() {
 		return content;
 	}
 
 	public boolean isAnchor() {
 		return isAnchor;
 	}
 
 	public void setAnchor(boolean isAnchor) {
 		this.isAnchor = isAnchor;
 	}
 
 	/**
 	 * Get cross-check set.
 	 * 
 	 * @return
 	 */
 	public HashSet<Character> getCrossChecks() {
 		return crosschecks;
 	}
 
 	public boolean crossCheckContains(char letter) {
 		return crosschecks.contains(letter);
 	}
 
 	/**
 	 * Get which column this square is located on the board.
 	 * 
 	 * @return
 	 */
 	public int getColumn() {
 		return this.column;
 	}
 
 	/**
 	 * Get which row this square is located on the board.
 	 * 
 	 * @return
 	 */
 	public int getRow() {
 		return this.row;
 	}
 
 	public Square getNextRight(boolean transposed) {
 		return (transposed) ? nextDown : nextRight;
 	}
 
 	public Square getNextLeft(boolean transposed) {
 		return (transposed) ? nextUp : nextLeft;
 	}
 
 	public Square getNextDown(boolean transposed) {
 		return (transposed) ? nextRight : nextDown;
 	}
 
 	public Square getNextUp(boolean transposed) {
 		return (transposed) ? nextLeft : nextUp;
 	}
 
 	public void setCenterSquare() {
 		this.content = Square.CENTER_SQUARE;
 	}
 }

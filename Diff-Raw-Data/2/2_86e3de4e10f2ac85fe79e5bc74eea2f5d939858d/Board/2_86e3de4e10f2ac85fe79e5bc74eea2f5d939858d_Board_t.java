 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.*;
 
 public class Board implements Comparable<Board> {
 	public int currX, currY;
 	public static int width = -1, height = -1;
	public static final int PLWEIGHT = 0, HWEIGHT = 5, SWEIGHT = 100, NWEIGHT = 0;
 	public char[][] board;
 	public static int[][] BOARDWEIGHT;
 	public static int[][] DEADLOCKS;
 	Board parent;
 	char parentMove;
 
 	int pathLenght;
 	int heuristic;
 	int solved;
 	int nearBox;
 	private int hashCode;
 
 
 	public Board(Board board, char move) {
 		this.currX = board.currX;
 		this.currY = board.currY;
 		this.width = board.width;
 		this.height = board.height;
 		parent = null;
 
 		this.board = new char[width][height];
 
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y)
 				this.board[x][y] = board.board[x][y];
 
 		}
 
 		doMove(move);
 		//BoardWeightCalculator.calculateBoardWeight(this);
 		computeHash();
 	}
 
 	public Board(Board board) {
 		this.currX = board.currX;
 		this.currY = board.currY;
 		this.width = board.width;
 		this.height = board.height;
 		parent = null;
 
 		this.board = new char[width][height];
 
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y)
 				this.board[x][y] = board.board[x][y];
 
 		}
 		computeHash();
 	}
 
 	public Board(BufferedReader lIn) throws IOException {
 
 		String lLine = lIn.readLine();
 
 		// read number of rows
 		int lNumRows = Integer.parseInt(lLine);
 		height = lNumRows;
 
 		lLine = lIn.readLine();
 		width = lLine.length();
 
 		board = new char[width][height];
 
 		// read each row
 		for (int y = 0; y < height; y++) {
 			assert (width == lLine.length());
 			for (int x = 0; x < width; ++x) {
 				char c = lLine.charAt(x);
 				if (c == '@') {
 					// @ is nothing special to the board, it's just you!
 					currX = x;
 					currY = y;
 					board[x][y] = ' '; // Replace with empty boardspace
 				} else if (c == '+') {
 					// + is nothing special to the board, it's just you on a
 					// goal!
 					currX = x;
 					currY = y;
 					board[x][y] = '.'; // Replace with normal goal.
 				} else {
 					board[x][y] = c;
 				}
 			}
 			if (!(y == height - 1))
 				lLine = lIn.readLine();
 			// here, we would store the row somewhere, to build our board
 			// in this demo, we just print it
 			// System.out.println(lLine);
 		}
 
 		SimpleDeadlockFinder.notDeadLockSquare(new Board(this));
 		//BoardWeightCalculator.calculateBoardWeight(this);
 
 		// System.out.println("NUM: "+findPossibleMoves().size());
 		parent = null;
 		computeHash();
 	}
 
 	public Board(String[] b) throws IOException {
 
 		// read number of rows
 		height = b.length;
 		width = b[0].length();
 
 		board = new char[width][height];
 
 		// read each row
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; ++x) {
 				char c = b[y].charAt(x);
 				if (c == '@') {
 					// @ is nothing special to the board, it's just you!
 					currX = x;
 					currY = y;
 					board[x][y] = ' '; // Replace with empty boardspace
 				} else if (c == '+') {
 					// + is nothing special to the board, it's just you on a
 					// goal!
 					currX = x;
 					currY = y;
 					board[x][y] = '.'; // Replace with normal goal.
 				} else {
 					board[x][y] = c;
 				}
 			}
 		}
 
 		SimpleDeadlockFinder.notDeadLockSquare(new Board(this));
 		//BoardWeightCalculator.calculateBoardWeight(this);
 
 		// System.out.println("NUM: "+findPossibleMoves().size());
 		parent = null;
 		
 		computeHash();
 	}
 
 	/*
 	 * Counts the number of unsolvedboxes. When 0 a solution is found.
 	 */
 	public int unsolvedBoxes() {
 		int sum = 0;
 		for (int x = 0; x < width; ++x)
 			for (int y = 0; y < height; ++y)
 				if (board[x][y] == '.' || board[x][y] == '+')
 					++sum;
 		return sum;
 	}
 
 	/*
 	 * Counts the number of solvedboxes. When 0 a solution is found.
 	 */
 	public int solvedBoxes() {
 		int sum = 0;
 		for (int x = 0; x < width; ++x)
 			for (int y = 0; y < height; ++y)
 				if (board[x][y] == '*')
 					++sum;
 		return sum;
 	}
 
 	/*
 	 * Moves the little warehouse keeper; U up - D down - L left - R right
 	 */
 	private int doMove(char move) {
 		assert (findPossibleMoves().contains(move));
 		switch (move) {
 		case 'U':
 			currY--;
 			if (board[currX][currY] == '$') {
 				board[currX][currY] = ' ';
 
 				if (board[currX][currY - 1] == '.')
 					board[currX][currY - 1] = '*';
 				else
 					board[currX][currY - 1] = '$';
 				
 				return 1;
 			} else if (board[currX][currY] == '*') {
 				board[currX][currY] = '.';
 
 				if (board[currX][currY - 1] == '.')
 					board[currX][currY - 1] = '*';
 				else
 					board[currX][currY - 1] = '$';
 			}
 			break;
 		case 'D':
 			currY++;
 			if (board[currX][currY] == '$') {
 				board[currX][currY] = ' ';
 
 				if (board[currX][currY + 1] == '.')
 					board[currX][currY + 1] = '*';
 				else
 					board[currX][currY + 1] = '$';
 				
 				return 1;
 			} else if (board[currX][currY] == '*') {
 				board[currX][currY] = '.';
 
 				if (board[currX][currY + 1] == '.')
 					board[currX][currY + 1] = '*';
 				else
 					board[currX][currY + 1] = '$';
 			}
 			break;
 		case 'L':
 			currX--;
 			if (board[currX][currY] == '$') {
 				board[currX][currY] = ' ';
 
 				if (board[currX - 1][currY] == '.')
 					board[currX - 1][currY] = '*';
 				else
 					board[currX - 1][currY] = '$';
 				
 				return 1;
 			} else if (board[currX][currY] == '*') {
 				board[currX][currY] = '.';
 
 				if (board[currX - 1][currY] == '.')
 					board[currX - 1][currY] = '*';
 				else
 					board[currX - 1][currY] = '$';
 			}
 			break;
 		case 'R':
 			currX++;
 			if (board[currX][currY] == '$') {
 				board[currX][currY] = ' ';
 
 				if (board[currX + 1][currY] == '.')
 					board[currX + 1][currY] = '*';
 				else
 					board[currX + 1][currY] = '$';
 				
 				return 1;
 			} else if (board[currX][currY] == '*') {
 				board[currX][currY] = '.';
 
 				if (board[currX + 1][currY] == '.')
 					board[currX + 1][currY] = '*';
 				else
 					board[currX + 1][currY] = '$';
 			}
 			break;
 		}
 		return 0;
 	}
 
 	public int calcHeuristic() {
 		int sum = 0;
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; ++y) {
 				if (board[x][y] == '$')
 					sum += BOARDWEIGHT[x][y];
 			}
 		}
 		return sum;
 	}
 
 	/*
 	 * public int score2() { Vector<BoardPos> boxes = new Vector<BoardPos>();
 	 * Vector<BoardPos> holders = new Vector<BoardPos>();
 	 * 
 	 * for (int x = 0; x < width; ++x) { for (int y = 0; y < height; ++y) { char
 	 * c = board[x][y]; if (c == '$') boxes.add(new BoardPos(x, y)); else if (c
 	 * == '.') holders.add(new BoardPos(x, y)); } }
 	 * 
 	 * int tot = 0; for (BoardPos box : boxes) { int last = Integer.MAX_VALUE;
 	 * for (BoardPos holder : holders) { int len = holder.distance(box); last =
 	 * Math.min(len, last); } tot += last; } return tot; }
 	 */
 
 	/*
 	 * Finds all possible moves from our current location.
 	 * 
 	 * @return Vector<Character> with all possible moves.
 	 */
 	public Vector<Character> findPossibleMoves() {
 		Vector<Character> ret = new Vector<Character>(4);
 
 		// UP
 		if (board[currX][currY - 1] == ' ' || board[currX][currY - 1] == '.')
 			ret.add('U');
 		else if (board[currX][currY - 1] == '$'
 				|| board[currX][currY - 1] == '*')
 			if (board[currX][currY - 2] == ' '
 					|| board[currX][currY - 2] == '.')
 				ret.add('U');
 		// DOWN
 		if (board[currX][currY + 1] == ' ' || board[currX][currY + 1] == '.')
 			ret.add('D');
 		else if (board[currX][currY + 1] == '$'
 				|| board[currX][currY + 1] == '*')
 			if (board[currX][currY + 2] == ' '
 					|| board[currX][currY + 2] == '.')
 				ret.add('D');
 		// LEFT
 		if (board[currX - 1][currY] == ' ' || board[currX - 1][currY] == '.')
 			ret.add('L');
 		else if (board[currX - 1][currY] == '$'
 				|| board[currX - 1][currY] == '*')
 			if (board[currX - 2][currY] == ' '
 					|| board[currX - 2][currY] == '.')
 				ret.add('L');
 		// RIGHT
 		if (board[currX + 1][currY] == ' ' || board[currX + 1][currY] == '.')
 			ret.add('R');
 		else if (board[currX + 1][currY] == '$'
 				|| board[currX + 1][currY] == '*')
 			if (board[currX + 2][currY] == ' '
 					|| board[currX + 2][currY] == '.')
 				ret.add('R');
 
 		assert (ret.size() <= 4);
 		return ret;
 	}
 
 	public boolean hasDeadlock() {
 
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y) {
 				if (board[x][y] == '$') {
 
 					if (DEADLOCKS[x][y] == 0)
 						return true;
 
 					// 4x squares
 					if (isBlocking(x - 1, y - 1) && isBlocking(x - 1, y)
 							&& isBlocking(x, y - 1)) {
 						// System.out.println(this);
 						return true;
 					}
 
 					if (isBlocking(x + 1, y - 1) && isBlocking(x, y - 1)
 							&& isBlocking(x + 1, y)) {
 						// System.out.println(this);
 						return true;
 					}
 
 					if (isBlocking(x + 1, y + 1) && isBlocking(x + 1, y)
 							&& isBlocking(x, y + 1)) {
 						// System.out.println(this);
 						return true;
 					}
 
 					if (isBlocking(x - 1, y + 1) && isBlocking(x - 1, y)
 							&& isBlocking(x, y + 1)) {
 						// System.out.println(this);
 						return true;
 					}
 
 				}
 			}
 		}
 
 		//if (FreezeDeadlockFinder.hasFreeze(new Board(this)))
 			//return true;
 
 		return false;
 	}
 
 	private boolean isWall(int x, int y) {
 		if (board[x][y] == '#')
 			return true;
 		else
 			return false;
 	}
 
 	private boolean isBlocking(int x, int y) {
 		if (board[x][y] == '#')
 			return true;
 
 		if (board[x][y] == '$')
 			return true;
 
 		if (board[x][y] == '*')
 			return true;
 
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return hashCode;
 	}
 	
 	private void computeHash() {
 		int hash = 0;
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y) {
 				hash = this.board[x][y] + (hash << 6) + (hash << 16) - hash;
 			}
 		}
 		hashCode = hash;
 	}
 
 	@Override
 	public boolean equals(Object b) {
 		if (this == b)
 			return true;
 		if (!(b instanceof Board))
 			return false;
 
 		Board that = (Board) b;
 
 		for (int x = 1; x < width-1; ++x) {
 			for (int y = 1; y < height-1; ++y) {
 				if (this.board[x][y] != that.board[x][y])
 					return false;
 			}
 		}
 		if (this.currY != that.currY)
 			return false;
 
 		if (this.currX != that.currX)
 			return false;
 
 		return true;
 	}
 
 	public int compareTo(Board b) {
 		return (this.heuristic * HWEIGHT + this.pathLenght * PLWEIGHT - this.solved * SWEIGHT + this.nearBox * NWEIGHT)
 				- (b.heuristic * HWEIGHT + b.pathLenght * PLWEIGHT - b.solved * SWEIGHT + b.nearBox * NWEIGHT);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		StringBuilder res = new StringBuilder();
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; ++x) {
 				if (currX == x && currY == y) {
 					if (board[x][y] == '.')
 						res.append('+');
 					else
 						res.append('@');
 				} else {
 					res.append(board[x][y]);
 				}
 			}
 			res.append('\n');
 		}
 		return res.toString();
 	}
 
 	public static String boardWeightToString() {
 		StringBuilder sb = new StringBuilder();
 		for (int y = 0; y < height; ++y) {
 			for (int x = 0; x < width; ++x) {
 				sb.append(BOARDWEIGHT[x][y] + "\t");
 			}
 			sb.append('\n');
 		}
 
 		return sb.toString();
 	}
 
 	public String deadlocksToString() {
 		StringBuilder sb = new StringBuilder();
 		for (int y = 0; y < height; ++y) {
 			for (int x = 0; x < width; ++x) {
 				sb.append(DEADLOCKS[x][y]);
 			}
 			sb.append('\n');
 		}
 
 		return sb.toString();
 	}
 
 }

 package edu.berkeley.gamesman.game;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * The game Connections
  * 
  * @author dnspies
  */
 public class Connections extends ConnectGame {
 	private class Edge {
 		final Point[] xPoints;
 		// xPoints[0] == down or left
 		// xPoints[1] == up or right
 		final Point[] oPoints;
 		// oPoints[0] == down or left
 		// oPoints[1] == up or right
 		private final int charNum;
 
 		Edge(int charNum) {
 			xPoints = new Point[2];
 			oPoints = new Point[2];
 			this.charNum = charNum;
 		}
 
 		char getChar() {
 			return board[charNum];
 		}
 	}
 
 	private class Point {
 		private final Edge[] edges;
 		// 0 == down
 		// 1 == left
 		// 2 == up
 		// 3 == right
 		private final int row, col;
 
 		// List in clockwise order
 
 		Point(int row, int col) {
 			this.row = row;
 			this.col = col;
 			edges = new Edge[4];
 		}
 	}
 
 	private int boardSide;
 	private int boardSize;
 	private Point[][] xPoints;
 	private Point[][] oPoints;
 	private Edge[][] vertEdges;
 	private Edge[][] horizEdges;
 	private char[] board;
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 */
 	public void initialize(Configuration conf) {
 		super.initialize(conf);
		boardSide = conf.getInteger("gamesman.game.side", 4);
 		boardSize = boardSide * boardSide + (boardSide - 1) * (boardSide - 1);
 		xPoints = new Point[boardSide + 1][boardSide];
 		// Bottom to top; Left to right
 		oPoints = new Point[boardSide + 1][boardSide];
 		// Left to right; Top to bottom
 		vertEdges = new Edge[boardSide][boardSide];
 		// Bottom to top; Left to right
 		horizEdges = new Edge[boardSide - 1][boardSide - 1];
 		// Bottom to top; Left to right (only shared edges)
 		board = new char[boardSize];
 		int ind = 0;
 		int horizInd = boardSide * boardSide;
 		for (int row = 0; row < boardSide; row++) {
 			for (int col = 0; col < boardSide; col++) {
 				vertEdges[row][col] = new Edge(ind++);
 				if (row < boardSide - 1 && col < boardSide - 1)
 					horizEdges[row][col] = new Edge(horizInd++);
 			}
 		}
 		for (int row = 0; row <= boardSide; row++) {
 			for (int col = 0; col < boardSide; col++) {
 				xPoints[row][col] = new Point(row, col);
 				oPoints[row][col] = new Point(row, col);
 				Edge nextXEdge = null;
 				Edge nextOEdge = null;
 				for (int i = 0; i < 4; i++) {
 					switch (i) {
 					case 0:
 						nextXEdge = Util.getElement(vertEdges, row - 1, col);
 						nextOEdge = Util.getElement(vertEdges, boardSide - 1
 								- col, row - 1);
 						break;
 					case 1:
 						nextXEdge = Util.getElement(horizEdges, row - 1,
 								col - 1);
 						nextOEdge = Util.getElement(horizEdges, boardSide - 1
 								- col, row - 1);
 						break;
 					case 2:
 						nextXEdge = Util.getElement(vertEdges, row, col);
 						nextOEdge = Util.getElement(vertEdges, boardSide - 1
 								- col, row);
 						break;
 					case 3:
 						nextXEdge = Util.getElement(horizEdges, row - 1, col);
 						nextOEdge = Util.getElement(horizEdges, boardSide - 2
 								- col, row - 1);
 						break;
 					}
 					if (nextXEdge != null) {
 						xPoints[row][col].edges[i] = nextXEdge;
 						nextXEdge.xPoints[i >> 1] = xPoints[row][col];
 					}
 					if (nextOEdge != null) {
 						oPoints[row][col].edges[i] = nextOEdge;
 						nextOEdge.oPoints[i >> 1] = oPoints[row][col];
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	protected int getBoardSize() {
 		return boardSize;
 	}
 
 	@Override
 	protected char[] getCharArray() {
 		return board;
 	}
 
 	@Override
 	protected void setToCharArray(char[] myPieces) {
 		if (board != myPieces) {
 			for (int i = 0; i < board.length; i++)
 				board[i] = myPieces[i];
 		}
 	}
 
 	@Override
 	public String displayState() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String describe() {
 		return "Connections " + boardSide + "x" + boardSide;
 	}
 
 	@Override
 	protected boolean isWin(char c) {
 		Point nextPoint = (c == 'X' ? xPoints[0][0] : oPoints[0][0]);
 		do {
 			nextPoint = testWin(c, nextPoint, c == 'X' ? 1 : 2);
 			if (nextPoint.row == boardSide)
 				return true;
 			nextPoint = Util.getElement(c == 'X' ? xPoints : oPoints, 0,
 					nextPoint.col + 1);
 		} while (nextPoint != null);
 		if (nextPoint == null)
 			return false;
 		else
 			return nextPoint.row == boardSide;
 	}
 
 	private Point testWin(char c, Point p, int dir) {
 		while (p.row != boardSide) {
 			Edge e = p.edges[dir];
 			while (e == null || e.getChar() != c) {
 				if (e == null)
 					if (dir != 1)
 						return p;
 				dir = ((dir + 1) & 3);
 				e = p.edges[dir];
 			}
 			int ind = 1 - (dir >> 1);
 			if (c == 'X')
 				p = e.xPoints[ind];
 			else
 				p = e.oPoints[ind];
 			dir = (dir - 1) & 3;
 		}
 		return p;
 	}
 }

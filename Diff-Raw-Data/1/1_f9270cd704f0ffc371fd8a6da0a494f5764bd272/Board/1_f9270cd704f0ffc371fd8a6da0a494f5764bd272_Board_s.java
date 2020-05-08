 package com.kyle.chess;
 
 import java.util.ArrayList;
 
 import com.kyle.chess.graphics.Screen;
 import com.kyle.chess.pieces.Bishop;
 import com.kyle.chess.pieces.ChessPiece;
 import com.kyle.chess.pieces.King;
 import com.kyle.chess.pieces.Knight;
 import com.kyle.chess.pieces.Pawn;
 import com.kyle.chess.pieces.Queen;
 import com.kyle.chess.pieces.Rook;
 
 public class Board {
 	Screen screen;
 	InputManager input;
 	ArrayList<ChessPiece> pieces = new ArrayList<ChessPiece>();
 	ArrayList<ChessSquare> squares = new ArrayList<ChessSquare>();
 
 	ChessPiece pieceBeingHeld = null;
 
 	public Board(Screen screen, InputManager input) {
 		this.screen = screen;
 		this.input = input;
 		createSquares();
 		setUpBoard();
 	}
 
 	public void createSquares() {
 		String c = "white";
 		String name = "";
 		for (int i = 1; i < 9; i++) {
 			if (c == "black") {
 				c = "white";
 			} else if (c == "white") {
 				c = "black";
 			}
 			for (int j = 0; j < 8; j++) {
 				switch (j) {
 				case 0:
 					name = "a" + i;
 					break;
 				case 1:
 					name = "b" + i;
 					break;
 				case 2:
 					name = "c" + i;
 					break;
 				case 3:
 					name = "d" + i;
 					break;
 				case 4:
 					name = "e" + i;
 					break;
 				case 5:
 					name = "f" + i;
 					break;
 				case 6:
 					name = "g" + i;
 					break;
 				case 7:
 					name = "h" + i;
 					break;
 				}
 				squares.add(new ChessSquare(name, c));
 				if (c == "black") {
 					c = "white";
 				} else if (c == "white") {
 					c = "black";
 				}
 			}
 		}
 	}
 
 	public void setUpBoard() {
 		pieces.add(new Rook("a1", "white"));
 		pieces.add(new Knight("b1", "white"));
 		pieces.add(new Bishop("c1", "white"));
 		pieces.add(new Queen("d1", "white"));
 		pieces.add(new King("e1", "white"));
 		pieces.add(new Bishop("f1", "white"));
 		pieces.add(new Knight("g1", "white"));
 		pieces.add(new Rook("h1", "white"));
 		pieces.add(new Pawn("a2", "white"));
 		pieces.add(new Pawn("b2", "white"));
 		pieces.add(new Pawn("c2", "white"));
 		pieces.add(new Pawn("d2", "white"));
 		pieces.add(new Pawn("e2", "white"));
 		pieces.add(new Pawn("f2", "white"));
 		pieces.add(new Pawn("g2", "white"));
 		pieces.add(new Pawn("h2", "white"));
		pieces.add(new Queen("h4", "black"));
 
 		pieces.add(new Rook("a8", "black"));
 		pieces.add(new Knight("b8", "black"));
 		pieces.add(new Bishop("c8", "black"));
 		pieces.add(new Queen("d8", "black"));
 		pieces.add(new King("e8", "black"));
 		pieces.add(new Bishop("f8", "black"));
 		pieces.add(new Knight("g8", "black"));
 		pieces.add(new Rook("h8", "black"));
 		pieces.add(new Pawn("a7", "black"));
 		pieces.add(new Pawn("b7", "black"));
 		pieces.add(new Pawn("c7", "black"));
 		pieces.add(new Pawn("d7", "black"));
 		pieces.add(new Pawn("e7", "black"));
 		pieces.add(new Pawn("f7", "black"));
 		pieces.add(new Pawn("g7", "black"));
 		pieces.add(new Pawn("h7", "black"));
 
 		for (int i = 0; i < pieces.size(); i++) {
 			for (int j = 0; j < squares.size(); j++) {
 				if (pieces.get(i).getCurrentSquare() == squares.get(j).getName()) {
 					squares.get(j).setPiece(pieces.get(i));
 				}
 			}
 		}
 	}
 
 	public static String getSquare(int x, int y) {
 		char location = ' ';
 
 		y = y / 100;
 		x = x / 100;
 
 		switch (x) {
 		case 0:
 			location = 'a';
 			break;
 		case 1:
 			location = 'b';
 			break;
 		case 2:
 			location = 'c';
 			break;
 		case 3:
 			location = 'd';
 			break;
 		case 4:
 			location = 'e';
 			break;
 		case 5:
 			location = 'f';
 			break;
 		case 6:
 			location = 'g';
 			break;
 		case 7:
 			location = 'h';
 			break;
 
 		}
 
 		return "" + location + (8 - y);
 	}
 
 	public void update() throws NullPointerException {
 
 		for (ChessPiece p : pieces) {
 			if (input.getSquare().equals(p.getCurrentSquare())) {
 
 				setPieceBeingHeld(p);
 				p.setIsHolding(true);
 				break;
 			}
 
 		}
 
 		if (pieceBeingHeld != null) {
 			pieceBeingHeld.setX(input.getMouseX() - 30);
 			pieceBeingHeld.setY(input.getMouseY() - 30);
 
 		}
 		if (input.getSquare() == "") {
 			pieceBeingHeld.placePiece();
 			pieceBeingHeld = null;
 		}
 	}
 
 	public void setPieceBeingHeld(ChessPiece p) {
 		pieceBeingHeld = p;
 	}
 
 	public void render() {
 		int color = 0xffffff;
 		for (int y = 0; y < 8; y++) {
 
 			if (color == 0x000000) {
 				color = 0xffffff;
 			} else {
 				color = 0x000000;
 			}
 
 			for (int x = 0; x < 8; x++) {
 				if (color == 0x000000) {
 					color = 0xffffff;
 				} else {
 					color = 0x000000;
 				}
 				screen.renderBoard(x, y, color);
 			}
 		}
 		renderPieces();
 
 	}
 
 	public void renderPieces() {
 		for (ChessPiece p : pieces) {
 			p.render(screen);
 		}
 		if (pieceBeingHeld != null) {
 			pieceBeingHeld.render(screen);
 		}
 	}
 }

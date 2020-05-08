 /** RemoveCheck
  * 
  * Pr�ft ob etwas vom Feld entfernt werden muss
  * 
  * Copyright: (c) 2011 <p>
  * Company: Gruppe 12 <p>
  * @author Julian Kipka & Lennart Henke
  * @version 2011.12.10
  */
 
 //TODO: Wenn keine Steine mehr das sing Spiel beenden.
 
 package de.gruppe12.shared;
 
 import de.fhhannover.inform.hnefatafl.vorgaben.BoardContent;
 import de.gruppe12.logic.GameLog;
 
 public class RemoveCheck {
 
 	private static boolean gamelog = false;
 	private static boolean commandLine = false;
 
 	/**
 	 * checkForRemove
 	 * 
 	 * Analysiert das Board und den Zug und entfert wenn noetig einen Stein oder
 	 * beendet im Fall dass der K�nig nicht mehr Ziehen kann das Spiel bzw.
 	 * setzt die n�tige flag
 	 * 
 	 * @param currentMove
 	 *            : Aktueller Zug
 	 * @param board
 	 *            : Aktuelles Board
 	 * @return : Das Board ohne die Steine die zu entfernen sind
 	 */
 	public static Board checkForRemove(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove,
 			Board board) {
 		long time1 = System.nanoTime();
 
 		Board temp = new Board();
 		board = doMove(currentMove, board);
 
 		temp.set(checkSurround(currentMove, board));
 		temp = checkForEnd(currentMove, board);
 
 		if (gamelog)
 			System.out.println("Removecheck brauchte "
 					+ (System.nanoTime() - time1) / 1000 + " µs");
 
 		return temp;
 
 	}
 
 	public static Board checkForRemove(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove,
 			Board board, boolean gameLog) {
 		gamelog = gameLog;
 		return checkForRemove(currentMove, board);
 	}
 
 	/**
 	 * doMove
 	 * 
 	 * Macht den Zug auf den Board und gibt das Board zur�ck
 	 * 
 	 * @param currentMove
 	 *            : Aktueller Zug
 	 * @param board
 	 *            : Aktuelles Board
 	 * @return : Das Board mit dem Zug
 	 */
 	private static Board doMove(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove,
 			Board board) {
 		BoardContent bc = board.getCellBC(currentMove.getFromCell());
 		board.setCell(new Cell(currentMove.getFromCell().getCol(), currentMove
 				.getFromCell().getRow(), BoardContent.EMPTY));
 		board.setCell(new Cell(currentMove.getToCell().getCol(), currentMove
 				.getToCell().getRow(), bc));
 		return board;
 	}
 
 	/**
 	 * checkSurround
 	 * 
 	 * Guckt ob um die gesogenen Figur sich ein Gegner und in Entfernung von 2
 	 * Feldern sich ein Verbuendeter befindet und entfert ggf. den Stein
 	 * 
 	 * @param currentMove
 	 *            : Aktueller Zug
 	 * @param board
 	 *            : Aktuelles Board
 	 * @return Mit entferten Stein
 	 */
 	private static BoardContent[][] checkSurround(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove,
 			Board board) {
 		int x = currentMove.getToCell().getCol(), y = currentMove.getToCell()
 				.getRow();
 		BoardContent me = board.getCellBC(currentMove.getToCell());
 		BoardContent tboard[][] = board.get();
 
 		/*
 		 * Ab hier Try-Catch weil es sein k�nnte das ein Stein sich an der Seite
 		 * befindet Es g�be sonst eine Index out of Bound Exception
 		 */
 
 		// Links gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 		// oder wenn Turm hinter Gegner
 		if (me == BoardContent.ATTACKER) {
 		try {
 			if (tboard[x - 1][y] == opposite(me)
 					&& (tboard[x - 2][y] == me || (x == 2 && (y == 0 || y == 12)))) {
 				tboard[x - 1][y] = BoardContent.EMPTY;
 				if (gamelog)
 					GameLog.logDebugEvent(opposite(me).toString()
 							+ " entfernt: " + (x - 1) + ", " + y);
 			}
 		} catch (Exception e) {
 
 		}
 
 		// Rechts gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 		// oder wenn Turm hinter Gegner
 		try {
 			if (tboard[x + 1][y] == opposite(me)
 					&& (tboard[x + 2][y] == me || (x == 10 && (y == 0 || y == 12)))) {
 				tboard[x + 1][y] = BoardContent.EMPTY;
 				if (gamelog)
 					GameLog.logDebugEvent(opposite(me).toString()
 							+ " entfernt: " + (x + 1) + ", " + y);
 			}
 		} catch (Exception e) {
 
 		}
 
 		// Unten gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 		// oder wenn Turm hinter Gegner
 		try {
 			if (tboard[x][y + 1] == opposite(me)
 					&& (tboard[x][y + 2] == me || (y == 10 && (x == 0 || x == 12)))) {
 				tboard[x][y + 1] = BoardContent.EMPTY;
 				if (gamelog)
 					GameLog.logDebugEvent(opposite(me).toString()
 							+ " entfernt: " + x + ", " + (y + 1));
 			}
 		} catch (Exception e) {
 
 		}
 
 		// Unten gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 		// oder wenn Turm hinter Gegner
 		try {
 			if (tboard[x][y - 1] == opposite(me)
 					&& (tboard[x][y - 2] == me || (y == 2 && (x == 0 || x == 12)))) {
 				tboard[x][y - 1] = BoardContent.EMPTY;
 				if (gamelog)
 					GameLog.logDebugEvent(opposite(me).toString()
 							+ " entfernt: " + x + ", " + (y - 1));
 			}
 		} catch (Exception e) {
 
 		}
 		}
 		else {
 			try {
 				if (tboard[x - 1][y] == BoardContent.ATTACKER
 						&& ((tboard[x - 2][y] == BoardContent.DEFENDER) || ((tboard[x - 2][y] == BoardContent.KING) )
 								|| (x == 2 && (y == 0 || y == 12)))) {
 					tboard[x - 1][y] = BoardContent.EMPTY;
 					if (gamelog)
						GameLog.logDebugEvent(opposite(me).toString()
 								+ " entfernt: " + (x - 1) + ", " + y);
 				}
 			} catch (Exception e) {
 
 			}
 
 			// Rechts gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 			// oder wenn Turm hinter Gegner
 			try {
 				if (tboard[x + 1][y] == BoardContent.ATTACKER
 						&& ((tboard[x + 2][y] == BoardContent.DEFENDER) || ((tboard[x + 2][y] == BoardContent.KING) )
 						|| (x == 10 && (y == 0 || y == 12)))) {
 					tboard[x + 1][y] = BoardContent.EMPTY;
 					if (gamelog)
						GameLog.logDebugEvent(opposite(me).toString()
 								+ " entfernt: " + (x + 1) + ", " + y);
 				}
 			} catch (Exception e) {
 
 			}
 
 			// Unten gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 			// oder wenn Turm hinter Gegner
 			try {
 				if (tboard[x][y + 1] == BoardContent.ATTACKER
 						&& ((tboard[x][y + 2] == BoardContent.DEFENDER) || ((tboard[x][y + 2] == BoardContent.KING)) 
 						|| (y == 10 && (x == 0 || x == 12)))) {
 					tboard[x][y + 1] = BoardContent.EMPTY;
 					if (gamelog)
						GameLog.logDebugEvent(opposite(me).toString()
 								+ " entfernt: " + x + ", " + (y + 1));
 				}
 			} catch (Exception e) {
 
 			}
 
 			// Unten gucken und evtl entfernen, wenn eigner Stein hinter Gegner -
 			// oder wenn Turm hinter Gegner
 			try {
 				if (tboard[x][y - 1] == BoardContent.ATTACKER
 						&& ((tboard[x][y - 2] == BoardContent.DEFENDER) || ((tboard[x][y - 2] == BoardContent.KING)) 
 						|| (y == 2 && (x == 0 || x == 12)))) {
 					tboard[x][y - 1] = BoardContent.EMPTY;
 					if (gamelog)
						GameLog.logDebugEvent(opposite(me).toString()
 								+ " entfernt: " + x + ", " + (y - 1));
 				}
 			} catch (Exception e) {
 
 			}
 		}
 
 		return tboard;
 	}
 
 	/**
 	 * checkForEnd
 	 * 
 	 * Pueft ob es ein Ende gegeben hat, also ob der Koenig umzingelt ist oder
 	 * es keinen Weg mehr zum ende gibt.
 	 * 
 	 * @param currentMove
 	 *            : Aktueller Zug
 	 * @param board
 	 *            : Aktuelles Board
 	 * @return Board mit gesetztem finish-Flag
 	 */
 	private static Board checkForEnd(
 			de.fhhannover.inform.hnefatafl.vorgaben.Move currentMove,
 			Board board) {
 
 		int x = currentMove.getToCell().getCol(), y = currentMove.getToCell()
 				.getRow();
 		BoardContent tboard[][] = board.get();
 		Board tb = board;
 
 		// Prüfen ob alle Fluchtburgen blockiert sind
 		boolean leftTopCornerblocked = (board.getCell(0, 2).getContent()
 				.equals(BoardContent.ATTACKER) && board.getCell(2, 0)
 				.getContent().equals(BoardContent.ATTACKER))
 				&& (board.getCell(1, 1).getContent()
 						.equals(BoardContent.ATTACKER)
 						|| (board.getCell(1, 2).getContent()
 								.equals(BoardContent.ATTACKER) && board
 								.getCell(2, 1).getContent()
 								.equals(BoardContent.ATTACKER)) || (board
 						.getCell(0, 1).getContent()
 						.equals(BoardContent.ATTACKER))
 						&& board.getCell(1, 0).getContent()
 								.equals(BoardContent.ATTACKER));
 
 		boolean rightTopCornerblocked = (board.getCell(10, 0).getContent()
 				.equals(BoardContent.ATTACKER) && board.getCell(12, 2)
 				.getContent().equals(BoardContent.ATTACKER))
 				&& (board.getCell(11, 1).getContent()
 						.equals(BoardContent.ATTACKER)
 						|| (board.getCell(10, 1).getContent()
 								.equals(BoardContent.ATTACKER) && board
 								.getCell(11, 2).getContent()
 								.equals(BoardContent.ATTACKER)) || (board
 						.getCell(11, 0).getContent()
 						.equals(BoardContent.ATTACKER))
 						&& board.getCell(12, 1).getContent()
 								.equals(BoardContent.ATTACKER));
 
 		boolean leftBottomCornerblocked = (board.getCell(0, 10).getContent()
 				.equals(BoardContent.ATTACKER) && board.getCell(2, 12)
 				.getContent().equals(BoardContent.ATTACKER))
 				&& (board.getCell(1, 11).getContent()
 						.equals(BoardContent.ATTACKER)
 						|| (board.getCell(1, 10).getContent()
 								.equals(BoardContent.ATTACKER) && board
 								.getCell(2, 11).getContent()
 								.equals(BoardContent.ATTACKER)) || (board
 						.getCell(0, 11).getContent()
 						.equals(BoardContent.ATTACKER))
 						&& board.getCell(1, 12).getContent()
 								.equals(BoardContent.ATTACKER));
 
 		boolean rightBottomCornerblocked = (board.getCell(10, 12).getContent()
 				.equals(BoardContent.ATTACKER) && board.getCell(12, 10)
 				.getContent().equals(BoardContent.ATTACKER))
 				&& (board.getCell(11, 11).getContent()
 						.equals(BoardContent.ATTACKER)
 						|| (board.getCell(10, 11).getContent()
 								.equals(BoardContent.ATTACKER) && board
 								.getCell(11, 10).getContent()
 								.equals(BoardContent.ATTACKER)) || (board
 						.getCell(12, 11).getContent()
 						.equals(BoardContent.ATTACKER))
 						&& board.getCell(11, 12).getContent()
 								.equals(BoardContent.ATTACKER));
 
 		if (commandLine)
 			if (leftBottomCornerblocked)
 				System.out.println("LB B");
 		if (commandLine)
 			if (leftTopCornerblocked)
 				System.out.println("LT B");
 		if (commandLine)
 			if (rightBottomCornerblocked)
 				System.out.println("RB B");
 		if (commandLine)
 			if (rightTopCornerblocked)
 				System.out.println("RT B");
 
 		// Wenn alle Fluchtburgen blockiert sind -> Angreifer gewinnt
 		if (leftTopCornerblocked && rightTopCornerblocked
 				&& leftBottomCornerblocked && rightBottomCornerblocked) {
 			tb.setFinish();
 			tb.setAttackerWon();
 			if (gamelog)
 				GameLog.logDebugEvent("Spielende");
 			return tb;
 		}
 
 		// "K�nig zieht auf Burg" beendet die Methode und setzt das Finish Flag
 		if (board.getCellBC(currentMove.getToCell()) == BoardContent.KING
 				&& ((x == 0 && y == 0) || (x == 0 && y == tboard[0].length - 1)
 						|| (x == tboard.length - 1 && y == 0) || (x == tboard.length - 1 && y == tboard[0].length - 1))) {
 			tb.setFinish();
 			tb.setDefenderWon();
 			if (gamelog)
 				GameLog.logDebugEvent("Spielende");
 			return tb;
 		}
 
 		/*
 		 * Suche K�nig JA ist ineffizient
 		 */
 		for (int i = 0; i < tboard.length; i++) {
 			for (int j = 0; j < tboard[i].length; j++) {
 				if (tboard[i][j] == BoardContent.KING) {
 					x = i;
 					y = j;
 				}
 			}
 		}
 
 		// Ist der K�nig umzingelt wenn ja su = 4
 		int sur = 0;
 
 		// links
 		try {
 			if (tboard[x - 1][y] == BoardContent.ATTACKER)
 				sur++;
 		} catch (IndexOutOfBoundsException e) {
 			sur++;
 		}
 
 		// Rechts
 		try {
 			if (tboard[x + 1][y] == BoardContent.ATTACKER)
 				sur++;
 		} catch (IndexOutOfBoundsException e) {
 			sur++;
 		}
 
 		// oben
 		try {
 			if (tboard[x][y - 1] == BoardContent.ATTACKER)
 				sur++;
 		} catch (IndexOutOfBoundsException e) {
 			sur++;
 		}
 
 		// unten
 		try {
 			if (tboard[x][y + 1] == BoardContent.ATTACKER)
 				sur++;
 		} catch (IndexOutOfBoundsException e) {
 			sur++;
 		}
 
 		if (sur == 4) {
 			if (gamelog)
 				GameLog.logDebugEvent("Spielende");
 			tb.setFinish();
 			tb.setAttackerWon();
 		}
 
 		return tb;
 
 	}
 
 	/**
 	 * opposite
 	 * 
 	 * Helfer zum ermitteln des Gegenspielers
 	 * 
 	 * @param bc
 	 * @return
 	 */
 	private static BoardContent opposite(BoardContent bc) {
 		if (bc == BoardContent.ATTACKER)
 			return BoardContent.DEFENDER;
 		else
 			return BoardContent.ATTACKER;
 	}
 }

 /**
  * 
  */
 package game;
 
 import general.Debug;
 
 import java.util.ArrayList;
 
 /**
 * @author Michi Bildet ein Vier-Gewinnt-Spielfeld ab.
  */
 public class Board {
 
 	/**
 	 * Darstellung mit Hilfe eines zweidimensionalen Arrays Der 0/0-Punkt wird
 	 * dabei unten links definiert.
 	 */
 	private Player[][] board;
 
 	// Anzahl Spalten
 	private final int columns;
 
 	// Anzahl Zeilen
 	private final int rows;
 
 	// Historie der Spielzuege
 	private ArrayList<Move> playerMoves;
 
 	/**
 	 * Konstruktor; Erstellt eine neues Spielfeld der angegebenen Groesse
 	 * 
 	 * @param columns
 	 *            Anzahle Spalten
 	 * @param row
 	 *            Anzahl Zeilen
 	 */
 	public Board(int columns, int rows) {
 		this.board = new Player[columns][rows];
 		this.columns = columns;
 		this.rows = rows;
 		this.playerMoves = new ArrayList<Move>(rows * columns);
 	}
 
 	/**
 	 * Findet das erste freie Feld einer Spalte und gibt dessen Index zurueck
 	 * 
 	 * @param column
 	 *            Die Spalte
 	 * @return Das ersten freien Feldes; NUll falls Spalte voll oder Spalte
 	 *         ungueltig
 	 */
 	public Field findFirstEmtpyFieldInColumn(int column) {
 		// ueberpruefen, ob der Wert gueltig ist
 		if (column < columns && column >= 0) {
 			// fuer alle felder in der angegebenen Spalte
 			for (int i = 0; i < rows; i++) {
 				if (board[column][i] == null) {
 					return new Field(column, i);
 				}
 			}
 		}
 
 		// Spalte ist voll oder Wert ist ungueltig
 		return null;
 	}
 
 	/**
 	 * Liefert die Belegung eines Feldes zurueck
 	 * 
 	 * @param row
 	 * @param column
 	 * @return
 	 */
 	public Player getField(int column, int row) {
 		// berprfen, ob der Zug gueltig ist
 		if (row < this.rows && column < this.columns && row >= 0 && column >= 0) {
 			return board[column][row];
 		}
 
 		return null;
 	}
 
 	/**
 	 * Ermittelt, ob auf dem Feld eine Gewinnsituation vorliegt
 	 * 
 	 * @return Der Gewinner oder NULL, falls keine Gewinner ermittelt werden
 	 *         konnte
 	 */
 	public Player findWinner() {
 		Player player;
 		int count;
 
 		// wir gehen jedes Feld durch und suchen anschlieend, ob dort eine
 		// Gewinnsituation vorliegt
 		for (int i = 0; i < this.rows; i++) {
 			for (int j = 0; j < this.columns; j++) {
 				// Spieler auslesen
 				player = this.getField(j, i);
 
 				// falls Feld nicht leer
 				if (player != null) {
 					// nach rechts suchen
 					count = 0;
 					for (int t = 1; t <= 3; t++)
 						if ((this.getField(j + t, i) != null)
 								&& (this.getField(j + t, i)) == player)
 							count++;
 					if (count >= 3)
 						return player;
 
 					// nach oben suchen
 					count = 0;
 					for (int t = 1; t <= 3; t++)
 						if ((this.getField(j, i + t) != null)
 								&& (this.getField(j, i + t)) == player)
 							count++;
 					if (count >= 3)
 						return player;
 
 					// nach oben rechts suchen
 					count = 0;
 					for (int t = 1; t <= 3; t++)
 						if ((this.getField(j + t, i + t) != null)
 								&& (this.getField(j + t, i + t)) == player)
 							count++;
 					if (count >= 3)
 						return player;
 
 					// nach unten rechts suchen
 					count = 0;
 					for (int t = 1; t <= 3; t++)
 						if ((this.getField(j + t, i - t) != null)
 								&& (this.getField(j + t, i - t)) == player)
 							count++;
 					if (count >= 3)
 						return player;
 				}
 			}
 		}
 
 		// falls keine Gewinner ermittelt werden konnte
 		return null;
 	}
 	
 
 	/**
 	 * Ermittelt die Menge der noch moeglichen Zuege
 	 * 
 	 * @return Array der Felder, in die momentan gesetzt werden kann
 	 */
 	public ArrayList<Field> getLegalMoves() {
 
 		ArrayList<Field> result = new ArrayList<Field>(columns);
 		// jede Spalte durchgehen und ersten freien Platz finden
 		for (int i = 0; i < columns; i++) {
 			Field field = findFirstEmtpyFieldInColumn(i);
 			if (field != null)
 				result.add(field);
 		}
 		// Array zurueckgeben
 		return result;
 	}
 	
 	
 
 	/**
 	 * Versucht einen Spielstein auf dem Feld zu setzen
 	 * @param move Der Zug, der  gemacht werden soll
 	 * @return TRUE, falls erfolgreich. FALSE, falls ungueltiger Zug
 	 */
 	public boolean makeMove(Move move) {
 		// Static.Debug(2, "Spieler " + move.getPlayer().toString() +
 		// " wirft in Spalte " + move.getColumn());
 		
 		if (board[move.getColumn()][move.getRow()] == null) {
 			
 			board[move.getColumn()][move.getRow()] = move.getPlayer();
 
 			// Move in der Liste speichern und Erfolg melden
 			playerMoves.add(move);
 			
 			Debug.log(1, String.format("Board: Spieler " + move.getPlayer()+" wirft in Spalte %d und belegt somit Feld %d / %d", move.getColumn(), move.getColumn(), move.getRow()));
 			return true;
 		}
 		// Spalte ist voll oder Platz schon belegt
 		return false;
 	}
 	
 	/**
 	 * Versucht einen Spielstein in eine Spalte zu werfen
 	 * @param player Der Spieler, fuer den geworfen werden soll
 	 * @param column Der Index der Spalte, in die geworfen werden soll
 	 * @return TRUE, falls erfolgreich. FALSE, falls ungueltiger Zug
 	 */
 	public boolean makeDrop(Player player, int column) {
 		//berechne Move zu diesem Wurf
 		Field targetField = this.findFirstEmtpyFieldInColumn(column);
 		Move my_move = new Move(player, targetField);
 		
 		//ueberpruefe Gueltigkeit
 		if (targetField != null) {
 			return this.makeMove(my_move);
 		}
 		
 		return false;
 	}
 	
 	
 
 	/**
 	 * Druckt das Spielfeld auf der Konsole
 	 */
 	public void print() {
 		// beginne mit oberster Zeile
 		for (int i = this.rows - 1; i >= 0; i--) {
 			// alle Spalten von links nach rechts
 			for (int j = 0; j < this.columns; j++) {
 				// Spieler ausgeben, falls gesetzt
 				if (this.board[j][i] != null)
 					System.out.print(" " + this.board[j][i].toString() + " ");
 				// ansonsten ein * fr ein leeres Feld
 				else
 					System.out.print(" * ");
 				if (j == columns - 1)
 					System.out.println();
 			}
 		}
 	}
 
 	public int getColumns() {
 		return columns;
 	}
 
 	public int getRows() {
 		return rows;
 	}
 
 	public ArrayList<Move> getPlayerMoves() {
 		return playerMoves;
 	}
 
 }

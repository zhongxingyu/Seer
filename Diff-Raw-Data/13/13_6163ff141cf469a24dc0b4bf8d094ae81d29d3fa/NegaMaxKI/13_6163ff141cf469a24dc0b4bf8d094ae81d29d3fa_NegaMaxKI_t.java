 /**
  * 
  */
 package ai;
 
 import java.util.ArrayList;
 
 import game.*;
 import game.Field;
 import game.Player;
 import general.Config;
 
 /**
  * Klasse zur Berechnung von 4Gewinnt Zuegen mit Hilfe eines NegaMax - Alpha -
  * Beta - Such - Algorithmus
  * 
  * @author Michi
  * 
  */
 public class NegaMaxKI implements IComputerPlayer {
 
 	
 	// Konstanten fr Bewertungsfunktion
 	private final int SCORE_WIN = 500000;
 	private final int SCORE_STRONG_THREAT = 200;
 	private final int SCORE_WEAK_THREAT = 10;
 
 	private Field calculatedMove;
 	private int[][] fieldValues;
 	
 	
 	//flag fr debug Ausgaben
 	private boolean debug = false;
 
 	public NegaMaxKI() {
 		this.initValues();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ai.IComputerPlayer#calcField(game.Player, game.Board, int)
 	 */
 	@Override
 	public Field calcField(Player currentPlayer, Board currentBoard, int level) {
 		
 		//Leve dynamisch erhoehen, falls bereits min. zwei Spalten voll
 		if (currentBoard.getLegalMoves().size()+1 < currentBoard.getColumns()) {
 			NegaMax(currentBoard, currentPlayer, level+1, -214748364,
 					214748364, true);
 		} else {
 			NegaMax(currentBoard, currentPlayer, level, -214748364,
 					214748364, true);
 		}
 		//this.NegaMaxUn(currentBoard, currentPlayer, level, true);
 		return calculatedMove;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ai.IComputerPlayer#calcFieldUnoptimized(game.Player, game.Board, int)
 	 */
 	public Field calcFieldUnoptimized(Player currentPlayer, Board currentBoard, int level) {
 
 		//Leve dynamisch erhoehen, falls bereits min. zwei Spalten voll
 		if (currentBoard.getLegalMoves().size()+1 < currentBoard.getColumns()) {
 			NegaMaxUnoptimized(currentBoard, currentPlayer, level+1, true);
 		} else {
 			NegaMaxUnoptimized(currentBoard, currentPlayer, level, true);
 		}
 		//this.NegaMaxUn(currentBoard, currentPlayer, level, true);
 		return calculatedMove;
 	}
 
 	/**
 	 * Durchsucht den Spielbaum mittels Alpha-Beta-Suche
 	 * 
 	 * @param tiefe
 	 *            Die Suchtiefe in Halbzgen
 	 * @param alpha
 	 *            Untere Schranke
 	 * @param beta
 	 *            Obere Schranke
 	 * @return Den ermittelten Wert
 	 */
 	private int NegaMax(Board currentBoard, Player currentPlayer, int depth,
 			int alpha, int beta, boolean isFirstLevelCall) {
 		
 /*		System.out.println("Aufruf AlphaBeta mit: ");
 		System.out.println("Tiefe="+depth);
 		System.out.println("Alpha="+alpha);
 		System.out.println("Beta="+beta);*/
 
 		int bestMoveIndex = 0;
 		// Kopie erstellen
 		currentBoard = currentBoard.clone();
 		
 
 		// falls Board in Endstellung oder Maximaltiefe erreicht
 		if ( currentBoard.isEndSituation() || (depth <= 0))
 			return evaluate(currentBoard, currentPlayer);
 
 		// sonst starte Rekursion
 		ArrayList<Field> moves = currentBoard.getLegalMoves();
 		preSort(moves);
 		
 		for (int i = 0; i < moves.size(); i++) {
 			
 			currentBoard.makeDrop(currentPlayer, moves.get(i).getColumn());
 
 			// Spielertausch 
 			Player nextPlayer = currentPlayer;
 			nextPlayer = (currentPlayer == Player.O) ? Player.X : Player.O;
 				
 			int value = -NegaMax(currentBoard, nextPlayer, depth-1, -beta, -alpha, false);
 						
 			if (debug && isFirstLevelCall) {
 				 System.out.println("Suchlevel: " + depth + " Spalte: " + moves.get(i).getColumn() + " Rckgabewert: "
 				 + value);
 			}
 
 				// mache Zug wieder rckgngig
 				currentBoard.undoLastMove();
 				
 				if (value > alpha) {
 					alpha = value;
 
 					if (isFirstLevelCall) {
 						// Speicher bisher besten Zug auch ab
 						bestMoveIndex = i;
 					}
 				}
 				
 			if (alpha >= beta) {
 				break;
 			}
 			
 		}//end for
 		
 		if (isFirstLevelCall) {
 			this.calculatedMove = moves.get(
 					bestMoveIndex);
 		}
 		return alpha;
 	}
 	
 	/**
 	 * Vergleiche {@link NegaMaxKI#NegaMax(Board, Player, int, int, int, boolean)}
 	 * Jedoch ohne jegliche Optimierung des Suchbaums.
 	 */
 	private int NegaMaxUnoptimized(Board currentBoard, Player currentPlayer, int depth, boolean isFirstLevelCall) {
 		
 /*		System.out.println("Aufruf AlphaBeta mit: ");
 		System.out.println("Tiefe="+depth);
 		System.out.println("Alpha="+alpha);
 		System.out.println("Beta="+beta);*/
 
 		int bestMoveIndex = 0;
 		int alpha = Integer.MIN_VALUE;
 		// Kopie erstellen
 		currentBoard = currentBoard.clone();
 		
 
 		// falls Board in Endstellung oder Maximaltiefe erreicht
 		if (currentBoard.isEndSituation() || (depth <= 0))
 			return evaluate(currentBoard, currentPlayer);
 
 		// sonst starte Rekursion
 		ArrayList<Field> moves = currentBoard.getLegalMoves();
 		preSort(moves);
 		
 		for (int i = 0; i < moves.size(); i++) {
 			
 			currentBoard.makeDrop(currentPlayer, moves.get(i).getColumn());
 
 			// Spielertausch
 			Player nextPlayer = currentPlayer;
 				nextPlayer = (currentPlayer == Player.O) ? Player.X : Player.O;
 				
 			int value = -NegaMaxUnoptimized(currentBoard, nextPlayer, depth-1, false);
 						
 			if (debug && isFirstLevelCall) {
 				 System.out.println("Suchlevel: " + depth + " Spalte: " + moves.get(i).getColumn() + " Rckgabewert: "
 				 + value);
 			}
 
 				// mache Zug wieder rckgngig
 				currentBoard.undoLastMove();
 				
 				if (value > alpha) {
 					alpha = value;
 
 					if (isFirstLevelCall) {
 						// Speicher bisher besten Zug auch ab
 						bestMoveIndex = i;
 					}
 				}
 		}//end for
 		
 		if (isFirstLevelCall) {
 			this.calculatedMove = moves.get(
 					bestMoveIndex);
 		}
 		return alpha;
 	}
 	
 	/**
 	 * Sortiert die Liste der mglichen Zge auf Grundlage statischer Feldwerte vor.
 	 * Steigert die Effizient der Alpha-Beta-Suche
 	 * @param moves
 	 */
 	private void preSort(ArrayList<Field> moves) {
 		boolean unsorted=true;
 		Field temp;
 
 		while (unsorted){
 			unsorted = false;
 			for (int i=0; i < moves.size()-1; i++) {
 
 				if (getStaticFieldValue(moves.get(i)) < getStaticFieldValue(moves.get(i+1))) {                      
 					 temp = moves.get(i);
 					 moves.set(i, moves.get(i+1));
 					 moves.set(i+1, temp);
 					unsorted = true;
 				}      
 			}
 		} 
 	}
 
 	public int evaluate(Board currentBoard, Player currentPlayer) {
 		int score = 0;
 		
 	//	currentBoard.print();
 		
 		Player opponentPlayer = (currentPlayer == Player.O) ? Player.X
 				: Player.O;
 		
 		Player winner = currentBoard.findWinner();
 
 		// Teste auf Sieg
 		if (winner == currentPlayer)
 			return SCORE_WIN;
 
 		// Test auf Niederlage
 		if (winner == opponentPlayer)
 			return -SCORE_WIN;
 
 
 		// Ansonsten bewerte Stellung nach Heuristik
 		// erkennen
 		Player player;
 		ArrayList<Field> arrStrongThreatsX = new ArrayList<Field>();
 		ArrayList<Field> arrStrongThreatsO = new ArrayList<Field>();
 		int weakThreatsX = 0, weakThreatsO = 0;
 
 		// Ermittle fr jedes Feld, ob es zu einer Drohung gehrt
 		for (int i = 0; i < currentBoard.getRows(); i++) {
 			for (int j = 0; j < currentBoard.getColumns(); j++) {
 				// Spieler auslesen
 				player = currentBoard.getField(j, i);
 
 				// falls Feld leer
 				if (currentBoard.getField(j, i) == null) {
 
 					// nach rechts suchen
 					if (currentBoard.getField(j + 2, i) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j + 2, i);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j + 1, i) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j + 3, i) == player
 									|| currentBoard.getField(j - 1, i) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					// nach links suchen
 					if (currentBoard.getField(j - 2, i) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j - 2, i);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j - 1, i) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j - 3, i) == player
 									|| currentBoard.getField(j + 1, i) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					// nach unten suchen
 					if (currentBoard.getField(j, i - 2) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j, i - 2);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j, i - 1) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j, i - 3) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					// nach oben rechts suchen
 					if (currentBoard.getField(j + 2, i + 2) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j + 2, i + 2);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j + 1, i + 1) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j + 3, i + 3) == player
 									|| currentBoard.getField(j - 1, i - 1) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					// nach unten rechts suchen
 					if (currentBoard.getField(j + 2, i - 2) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j + 2, i - 2);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j + 1, i - 1) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j + 3, i - 3) == player
 									|| currentBoard.getField(j - 1, i + 1) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					//nach links oben 
 					if (currentBoard.getField(j - 2, i + 2) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j - 2, i + 2);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j - 1, i + 1) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j - 3, i + 3) == player
 									|| currentBoard.getField(j + 1, i - 1) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 
 					// nach links unten
 					if (currentBoard.getField(j - 2, i - 2) != null) {
 						// Spieler bestimmen
 						player = currentBoard.getField(j - 2, i - 2);
 						//mgliche schwache drohung
 						if (currentBoard.getField(j - 1, i - 1) == player) {
 							// Test auf starke Drohung
 							if (currentBoard.getField(j - 3, i - 3) == player
 									|| currentBoard.getField(j + 1, i + 1) == player) {
 								// merken
 								if (player == Player.X)
 									arrStrongThreatsX.add(new Field(j, i));
 								if (player == Player.O)
 									arrStrongThreatsO.add(new Field(j, i));
 							} else { //nur Schwache Drohung
 								// entsprechenden Zhler hochsetzen
 								if (player == Player.X)
 									weakThreatsX++;
 								if (player == Player.O)
 									weakThreatsO++;
 							}
 						}
 					}
 				}//end if
 			}
 		}// end doppelter for-loop
 
 		/*
 		System.out.println("Anzahl starke Drohung von X: " + arrStrongThreatsX.size());
 		System.out.println("Anzahl starke Drohung von O: " + arrStrongThreatsO.size());
 		System.out.println("Anzahl schwache Drohung von X: " + weakThreatsX);
 		System.out.println("Anzahl schwache Drohung von O: " + weakThreatsO);
 		*/
 		
 		
 		//bewerter alle Drohunge von X nach ihrere Lage (je tiefer desto besser)
 		for (int i = 0; i < arrStrongThreatsX.size(); i++) {
 			int height = arrStrongThreatsX.get(i).getRow();
 			int value = SCORE_STRONG_THREAT-(5*height);
 			
			//Test auf Akutheit 
			if (height == 1 || 
					currentBoard.getField(arrStrongThreatsX.get(i).getColumn(), height-2) != null )
 				value = value*2;
 			
 			//Zuweisung je nach Farbe
 			if (currentPlayer == Player.X) score += value;
 			else score -= value;
 		}
 
 		//bewerter alle Drohunge von O nach ihrere Lage (je tiefer desto besser)
 		for (int i = 0; i < arrStrongThreatsO.size(); i++) {
 			int height = arrStrongThreatsO.get(i).getRow();
 			int value = SCORE_STRONG_THREAT-(5*height);
 			
			//Test auf Akutheit
			if (height == 1 || 
					currentBoard.getField(arrStrongThreatsO.get(i).getColumn(), height-2) != null )
 				value = value*2;
 			
 			//Zuweisung je nach Farbe
 			if (currentPlayer == Player.X) score -= value;
 			else score += value;
 		}
 
 		if (currentPlayer == Player.X) {
 			score += (weakThreatsX * SCORE_WEAK_THREAT);
 			score -= (weakThreatsO * SCORE_WEAK_THREAT);
 		}
 
 		if (currentPlayer == Player.O) {
 			score -= (weakThreatsX * SCORE_WEAK_THREAT);
 			score += (weakThreatsO * SCORE_WEAK_THREAT);
 		}
 		
 		return score;
 	}
 
 	/**
 	 * Ermittelt einen statischen Wert fr eine Feld, ausgehend davon, wie viele
 	 * Viererreiehn rein theoretisch durch das Feld gelegt werden knnen.
 	 * Dadurch werden zentrumsnahe Felder bevorzugt untersucht
 	 * 
 	 * @param f
 	 * @return Der statische Feldwert
 	 */
 	private int getStaticFieldValue(Field f) {
 		return this.fieldValues[f.getColumn()][f.getRow()];
 	}
 
 	/**
 	 * Belegt jedes Feld mit einem Wert vor, der angibt wie viele Viererreiehn
 	 * rein theoretisch durch das Feld gelegt werden knnen.
 	 */
 	private void initValues() {
 		this.fieldValues = new int[Config.COLS][Config.ROWS];
 		this.fieldValues[0][0] = 3;
 		this.fieldValues[0][1] = 4;
 		this.fieldValues[0][2] = 5;
 		this.fieldValues[0][3] = 5;
 		this.fieldValues[0][4] = 4;
 		this.fieldValues[0][5] = 3;
 		this.fieldValues[1][0] = 4;
 		this.fieldValues[1][1] = 6;
 		this.fieldValues[1][2] = 8;
 		this.fieldValues[1][3] = 8;
 		this.fieldValues[1][4] = 6;
 		this.fieldValues[1][5] = 4;
 		this.fieldValues[2][0] = 5;
 		this.fieldValues[2][1] = 8;
 		this.fieldValues[2][2] = 11;
 		this.fieldValues[2][3] = 11;
 		this.fieldValues[2][4] = 8;
 		this.fieldValues[2][5] = 5;
 		this.fieldValues[3][0] = 7;
 		this.fieldValues[3][1] = 10;
 		this.fieldValues[3][2] = 13;
 		this.fieldValues[3][3] = 13;
 		this.fieldValues[3][4] = 10;
 		this.fieldValues[3][5] = 7;
 		this.fieldValues[4][0] = 5;
 		this.fieldValues[4][1] = 8;
 		this.fieldValues[4][2] = 11;
 		this.fieldValues[4][3] = 11;
 		this.fieldValues[4][4] = 8;
 		this.fieldValues[4][5] = 5;
 		this.fieldValues[5][0] = 4;
 		this.fieldValues[5][1] = 6;
 		this.fieldValues[5][2] = 8;
 		this.fieldValues[5][3] = 8;
 		this.fieldValues[5][4] = 6;
 		this.fieldValues[5][5] = 4;
 		this.fieldValues[6][0] = 3;
 		this.fieldValues[6][1] = 4;
 		this.fieldValues[6][2] = 5;
 		this.fieldValues[6][3] = 5;
 		this.fieldValues[6][4] = 4;
 		this.fieldValues[6][5] = 3;
 	}
 
 }

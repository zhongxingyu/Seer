 /**
  * Beinhaltet alle Klassen zur Abbildung der VierGewinnt-Spiellogik
  * Ein Spiel besteht dabei aus mehreren Saetzen
  */
 package game;
 
 import general.Config;
 import general.Debug;
 import io.FileMonitor;
 import io.FileWriter;
 import io.ServerResponse;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 import java.util.Observer;
 
 import ai.*;
 
 /**
  * Bildet ein Vier-Gewinnt-Spiel mit mehreren Saetzen ab
  * 
  * @author Michi
  * 
  */
 public class FourInARowGame extends Observable implements Observer {
 
 	//Konstanten fuer Spielsiegerstatus
 	public final static int GAME_WON = 0;
 	public final static int GAME_LOST = 1;
 	public final static int GAME_OPEN = -1;
 
 	// Konstanten fuer Client Satus
 	public final static String NOT_STARTED = "Spiel noch nicht gestartet";
 	public final static String WAITING = "Gestartet: Warte auf Server";
 	public final static String CALCULATING = "Berechne";
 	public final static String SET_ENDED = "Satz beendet";
 	public final static String SET_PAUSED = "Satz pausiert";
 	public final static String SET_RESTARTED = "Satz zurckgesetzt";
 	public final static String GAME_ENDED = "Spiel beendet";
 
 	// Der Zusatnd des Clienten
 	private String clientStatus;
 
 	// Die Saetze
 	private ArrayList<Set> sets;
 
 	// Index des aktuellen Satzes
 	private int setIndex = -1;
 	
 	// Indikator, ob der Satz zurckgesetz wurde
 	private boolean setWasRestarted = false;
 
 	// Die Anzahl der Saetze
 	private int setCount;
 
 	// Der Pfad zum Kommunikationsverzeichnis
 	private String commDirString;
 	private File commDir;
 
 	// Der Filemonitor zur Ueberwachung
 	private FileMonitor commDirMonitor;
 
 	// Der Filewriter, zur Uebermittlung unseres Zuges an den Server
 	private FileWriter fileWriter;
 
 	// Die ID der Gegnergruppe zur spaeteren Analyse
 	private int enemyGroup;
 
 	/**
 	 * Konstrukor, Erstellt ein neues Spielobjekt
 	 * 
 	 * @param setCount
 	 *            Die Anzahl an Saetzen
 	 */
 	public FourInARowGame(int setCount) {
 		sets = new ArrayList<Set>();
 		this.setCount = setCount;
 		this.clientStatus = FourInARowGame.NOT_STARTED;
 		Debug.log(10, "Neues Spiel gestartet ...");
 	}
 
 	/**
 	 * Ermittelt den Spielstand nach Saetzen
 	 * 
 	 * @return String der Form
 	 *         "Anzahl von uns gewonnen Saetze":"Anzahl von Gegner gewonnen Saetze"
 	 *         ; z.B. "2:1"
 	 */
 	public String getScore() {
 		String result;
 		int won = 0;
 		int lost = 0;
 
 		// Ermittele Anzahl gewonnen bzw. verlorene Saetze
 		for (int i = 0; i < sets.size(); i++) {
 			if (sets.get(i).getOurPlayer() == sets.get(i).getWinnerPlayer()) {
 				won++;
 			}
 			if (sets.get(i).getEnemyPlayer() == sets.get(i).getWinnerPlayer()) {
 				lost++;
 			}
 		}
 		result = won + ":" + lost;
 		return result;
 	}
 
 	/**
 	 * Ueberprueft, ob wir bereits das komplette Spiel gewonnen haben
 	 * 
 	 * @return Liefer TRUE zurueck, falls wir nach Saetzen gewonnen haben, FALSE
 	 *         sonst
 	 */
 	public boolean checkWinStatus() {
 		int won = 0;
 		for (int i = 0; i < sets.size(); i++) {
 			if (sets.get(i).getOurPlayer() == sets.get(i).getWinnerPlayer()) {
 				won++;
 			}
 		}
 		return won > (setCount / 2);
 	}
 
 	/**
 	 * Ermittelt den Sieger eines komplette Spiels
 	 * 
 	 * @return -1, Falls noch kein Gewinner feststeht. 0, Falls wir gewonnen
 	 *         haben. 1, Falls die Gegnergruppe gewonnen hat
 	 */
 	public int getGameWinner() {
 
 		int won = 0;
 		int lost = 0;
 
 		// Ermittele Anzahl gewonnen bzw. verlorene Saetze
 		for (int i = 0; i < sets.size(); i++) {
 			if (sets.get(i).getOurPlayer() == sets.get(i).getWinnerPlayer()) {
 				won++;
 			}
 			if (sets.get(i).getEnemyPlayer() == sets.get(i).getWinnerPlayer()) {
 				lost++;
 			}
 		}
 
 		// falls wir einedeutig gewonnen haben
 		if (won > (setCount / 2)) {
 			return FourInARowGame.GAME_WON;
 		}
 
 		// falls die Gegnerseite eindeutig gewonnen hat
 		if (lost > (setCount / 2)) {
 			return FourInARowGame.GAME_LOST;
 		}
 
 		// falls (noch) kein eindeutiger Gewinner ermittelt werden konnte
 		return FourInARowGame.GAME_OPEN;
 	}
 
 	/**
 	 * Setzt eine neues Kommunikationsverzeichnis
 	 * 
 	 * @param commDirString
 	 *            Der Pfad zum Verzeichnis
 	 */
 	public void setCommDir(String commDirString) {
 		this.commDirString = commDirString;
 
 		this.commDir = new File(commDirString);
 
 		// Teste Kommunikationspfad
 		if (commDir.canWrite())
 			Debug.log(20, "Kommunikationspfad erfolgreich eingerichtet.");
 		else
 			Debug.error("Angegebenes Kommunikationsverzeichnes konnte nicht eingerichtet werden!");
 	}
 
 	/**
 	 * Liefert den Pfad zum Kommunikationsverzeichniss
 	 * 
 	 * @return Der Pfad als String
 	 */
 	public String getCommDir() {
 		return this.commDirString;
 	}
 
 	/**
 	 * Gibt den Zustand des Client an. Siehe dazu entsprechende Konstanten.
 	 * 
 	 * @return Der Zustand des Clienten
 	 */
 	public String getClientStatus() {
 		return clientStatus;
 	}
 
 	/**
 	 * Beginnt einen neuen Satz und startet die Ueberwachung des
 	 * Kommunikationspfades#
 	 * 
 	 * @param ourPlayer
 	 *            Unsere "Farbe" bzw. Spieler
 	 */
 	public void startNewSet(Player ourPlayer) {
 		//Neuen Satz erstellen
 		Set newSet = new Set(ourPlayer);
 		newSet.setStartTime(new Date());
 		
 		//Nur neuen Satz erstellen, falls es sich nicht um einen Restart handelt
 		if (!setWasRestarted) {
 			sets.add(newSet);
 			setIndex++;
 		} 
 		//ansonsten momentanen Satz berschreiben
 		else {
 			sets.set(setIndex, newSet);
 			this.setWasRestarted = false;
 		}
 		
 		// Kommunikationsklassen entsprechend einrichten
 		if (ourPlayer == Player.X) {
 			this.commDirMonitor = new FileMonitor(new File(commDirString
 					+ System.getProperty("file.separator")
 					+ Config.FILENAME_SERVER2SPIELER_X));
 			this.fileWriter = new FileWriter(commDirString
 					+ System.getProperty("file.separator")
 					+ Config.FILENAME_SPIELER_X2SERVER);
 		} else {
 			this.commDirMonitor = new FileMonitor(new File(commDirString
 					+ System.getProperty("file.separator")
 					+ Config.FILENAME_SERVER2SPIELER_O));
 			this.fileWriter = new FileWriter(commDirString
 					+ System.getProperty("file.separator")
 					+ Config.FILENAME_SPIELER_O2SERVER);
 		}
 
 		// Wir werden ueber Aenderungen informiert - hier: sobald eine neue
 		// Antwort des Servers empfangen wurde
 		this.commDirMonitor.addObserver(this);
 
 		// Starte Ueberwachung des Kommunikationspfades
 		commDirMonitor.startMonitoring();
 
 		// setze Client Status
 		this.clientStatus = FourInARowGame.WAITING;
 
 		// Log
 		Debug.log(10, "Satz #" + (setIndex + 1) + " gestartet.");
 	}
 
 	/**
 	 * Beendet den akutellen Satz
 	 */
 	public void endSet() {
 		commDirMonitor.stopMonitoring();
 		this.getCurrentSet().setEndTime(new Date());
 		this.getCurrentSet().setHasEnded(true);
 
 		// setze Status neu
 		this.clientStatus = FourInARowGame.SET_ENDED;
 		
 		Debug.log(10, "Satz #" + (setIndex + 1) + " beendet.");
 
 		//Falls Spiel zu Ende setze Status
 		if (this.sets.size() == this.setCount) {
 			this.clientStatus = FourInARowGame.GAME_ENDED;
 			Debug.log(10, "Spiel nach regulrer Anzahl an Stzen jetzt beendet. Endstand: " + this.getScore());
 		}
 	}
 	
 	/**
 	 * Pausiert den aktuellen Satz und stoppt die berwachung des Kommunikationspfades
 	 */
 	public void pauseCurrentset() {
 		this.commDirMonitor.stopMonitoring();
 		this.clientStatus = FourInARowGame.SET_PAUSED;
 		Debug.log(10, "Satz pausiert.");
 	}
 
 	/**
 	 * Reaktiviert die Ueberwachung des Kommuniaktionspfades
 	 */
 	public void resumeCurrentSet(){
 		this.commDirMonitor.startMonitoring();
 		this.clientStatus = FourInARowGame.WAITING;
 		Debug.log(10, "Satz fortgesetzt.");
 	}
 	
 	/**
 	 * Setzt den aktuellen Satz zurck
 	 */
 	public void resetCurrentSet() {
 		//Momentanen Satz mit Dummyvariable berschreiben fr UI
 		//Wird in jedem Fall beim eigentlich Start neu gesetz
 		sets.set(setIndex, new Set(Player.X));
 		this.setWasRestarted = true;
 		this.clientStatus = FourInARowGame.SET_RESTARTED;
 		Debug.log(10, "Satz #" + (setIndex + 1) + " wurde zurckgesetzt.");
 	}
 
 	/**
 	 * Liefert den gerade aktuellen Satz
 	 * 
 	 * @return Eine Referenz auf den Satz
 	 */
 	public Set getCurrentSet() {
 		if (setIndex >= 0 && setIndex < sets.size()) {
 			return sets.get(setIndex);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Liefer alle Saetze eines Spiels
 	 * 
 	 * @return ArrayList mit den Saetzen
 	 */
 	public ArrayList<Set> getSets() {
 		return sets;
 	}
 
 	/**
 	 * Gibt die Anzahl der Spielsaetze dieses Spiels zurck
 	 * 
 	 * @return Anzahl der Spielsaetze
 	 */
 	public int getSetCount() {
 		return setCount;
 	}
 
 	/**
 	 * 
 	 * @return Die ID der Gegnergruppe
 	 */
 	public int getEnemyGroup() {
 		return enemyGroup;
 	}
 
 	/**
 	 * Setz den Gegner
 	 * 
 	 * @param enemyGroup
 	 *            Die ID der Gruppe, gegen die gespielt wird
 	 */
 	public void setEnemyGroup(int enemyGroup) {
 		this.enemyGroup = enemyGroup;
 	}
 
 	/**
 	 * Wird vom FileMonitor aufgerufen, sobald eine neue Datei des Servers
 	 * gefunden und ausgewertet werden konnte
 	 * 
 	 * @param arg0
 	 *            Das Objekt, welches das Update ausgeloest hat
 	 * @param arg1
 	 *            Die Serverantwort, die aus der gefunden XML-Datei gelesen
 	 *            wurde
 	 */
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// Teste, ob es sich wirklich um eine korrekte Instanz handelt
 		if (arg1 != null && arg1 instanceof ServerResponse) {
 			//Zeitstempel, Empfang der Serverantwort
 			double start;
 			double duration;
 			
 			Debug.log(30, "Game: Von FilemMonitor ueber neues Update benachrichtigt.");
 			// Falls es sich um eine korrekte Serverantwort handelt in eigene
 			// Variable casten
 			ServerResponse serverResponse = (ServerResponse) arg1;
 			
 			//Zeitstempel auslesen
 			start = serverResponse.getTimestamp();
 
 			// Satus setzen
 			this.clientStatus = FourInARowGame.CALCULATING;
 
 			// Serverantwort speichern
 			this.getCurrentSet().setLastServerState(serverResponse);
 
 			// Falls ein Gegnerzug uebermittelt wurde, muss dieser auch bei uns
 			// abgebildet werden
 			if (serverResponse.getGegnerzug() != -1) {
 				this.getCurrentSet().makeDrop(
 						this.getCurrentSet().getEnemyPlayer(),
 						serverResponse.getGegnerzug());
 				//Loginformation
 				Move lastMove = this
 						.getCurrentSet()
 						.getBoard()
 						.getPlayerMoves()
 						.get(getCurrentSet().getBoard().getPlayerMoves().size() - 1);
 				Debug.log(
 						1,
 						String.format(
 								"Spieler "
 										+ lastMove.getPlayer()
 										+ " wirft in Spalte %d und belegt somit Feld %d / %d",
 								lastMove.getColumn(), lastMove.getColumn(),
 								lastMove.getRow()));
 				this.getCurrentSet().setStartingPlayer(
 						this.getCurrentSet().getEnemyPlayer());
 			} else { // Falls kein Gegnerzug uebermittelt wurde, sind wir in
 						// jedem Fall der startende Spieler
 				this.getCurrentSet().setStartingPlayer(
 						this.getCurrentSet().getOurPlayer());
 			}
 
 			// alle Oberserver informieren
 			setChanged();
 			notifyObservers(serverResponse);
 			
 			//Statusvariablen fuer eigene Zugberechnung
 			Player ourPlayer = this.getCurrentSet().getOurPlayer();
 			Board currentBoard = this.getCurrentSet().getBoard();
 			Board clonedBoard = currentBoard.clone(); //fuer verschieden KI Berechnungen
					
 
 			// Falls Freigabe fuer uns erfolgt wird unser naechster Zug
 			// berechnet
 			if (serverResponse.getFreigabe()) {
 				IComputerPlayer ki = new NegaMaxKI(); //ndern, falls andere KI gewollt
 				
 				//Zur Sicherheit zunchst schnellen (Zufalls-)zug bestimmen und in Dateischreiben
 				Field randomField = ki.calcField(ourPlayer, clonedBoard, 2);
 				
 				if (this.fileWriter.writeMove(randomField.getColumn())) {
					this.getCurrentSet().makeDrop(ourPlayer, randomField.getColumn());
 					Debug.log(30,
 							"Durch (Random-KI) berechneter eigener Zug: ("
 									+ randomField.getColumn() + "/"
 									+ randomField.getRow() + ")");
 				} else {
 					Debug.error("File-Writer konnte schnellen Zug nicht schreiben!");
 				}
 				//Eigentliche Zugberechnung
 				Field calculatedField = ki.calcField(ourPlayer, clonedBoard, this.getCurrentSet().getKiLevel());
 			    
 				//Zeitmessung
 				duration = System.currentTimeMillis() - start;
 			    
 			    //Falls erfolgreich und im Zeitfenster (plus Puffer: doppelte Zugriffszeit)
 				if (calculatedField != null && duration < (Config.MAX_CALCTIME-2*Config.TIMERINTERVALL)) {
 					Debug.log(30, "Durch KI-Level "+ this.getCurrentSet().getKiLevel() +" berechneter eigener Zug: ("
 							+ calculatedField.getColumn() + "/"
 							+ calculatedField.getRow() + ")");
 					Debug.log(30, "Benoetigte Zeit seit Empfang der Serverantwort in ms: "
 							+ duration);
 					
 					// Unseren Zug per FileWriter an den Server uebermitteln
 					if (this.fileWriter.writeMove(calculatedField.getColumn())) {
 						//Randomzug ueberschreiben
 						currentBoard.undoLastMove();
						this.getCurrentSet().makeDrop(ourPlayer,
 								calculatedField.getColumn());
 					} else {
 						Debug.error("File-Writer konnte ordentlichen Zug nicht schreiben!");
 					}
 					
 				}
 				
 				//Zeitmessung
 				duration = System.currentTimeMillis() - start;
 				
 				//Falls weniger als die Haelfte der Zeit gebraucht
 				//Starte erneute Berechnung mit hoehrem Level
 				if (duration < (Config.MAX_CALCTIME-Config.TIMERINTERVALL)/2
 						&& !currentBoard.isEndSituation()) {
 					Debug.log(30, "Ausreichend Zeitpuffer vorhanden. Starte tiefere Suche ...");
 					
 					//Berechne mit level+1
 					int newlevel = this.getCurrentSet().getKiLevel()+1;
 					Field calculatedField2 = ki.calcField(ourPlayer, clonedBoard, newlevel);
 					
 					//Zeitmessung
 					duration = System.currentTimeMillis() - start;
 					
 					//Falls erfolgreich und immernoch im Zeitfenster (plus Puffer: doppelte Zugriffszeit)
 					if (calculatedField2 != null && duration < (Config.MAX_CALCTIME-2*Config.TIMERINTERVALL)) {
 						Debug.log(10, "Durch KI-Level "+ newlevel +" berechneter eigener Zug: ("
 								+ calculatedField2.getColumn() + "/"
 								+ calculatedField2.getRow() + ")");
 						Debug.log(10, "Benoetigte Zeit seit Empfang der Serverantwort in ms: "
 								+ duration);
 						
 						// Unseren Zug per FileWriter an den Server uebermitteln
 						if (this.fileWriter.writeMove(calculatedField2.getColumn())) {
 							//Ersten ordentlichen Zug ueberschreiben
 							currentBoard.undoLastMove();
							this.getCurrentSet().makeDrop(ourPlayer,
 									calculatedField2.getColumn());
 						} else {
 							Debug.error("File-Writer konnte zweiten ordentlichen Zug nicht schreiben!");
 						}
 
 					} else { //falls zweiter Zug nicht mehr im Zeitfenster
 						Debug.log(30, "Verwerfen zweiten Zug, da Berechnung zu langsam.");
 						Debug.log(30, "Waehle durch KI-Level "+ this.getCurrentSet().getKiLevel() +" berechneter eigener Zug: ("
 								+ calculatedField.getColumn() + "/"
 								+ calculatedField.getRow() + ")");
 						Debug.log(30, "Benoetigte Zeit seit Empfang der Serverantwort in ms: "
 								+ duration);
 					}
 				}//end if: Zeit fuer zweite Berechnung
 			}
 
 			// Falls ein Gewinner aus unserer Sicht feststeht ausgeben
 			Player winner;
 			if ((winner = currentBoard.findWinner()) != null) {
 				Debug.log(1, "Satz #" + (setIndex + 1) + " wurde bereits von Spieler "
 						+ winner + " gewonnen! (Aus Client Sicht)");
 			}
 
 			// Falls Gewinner laut Server feststeht, beenden wir in jedem Fall
 			// den Satz
 			if (serverResponse.getSatzstatus().equalsIgnoreCase("beendet")) {
 				Debug.log(1, "Satz #" + (setIndex + 1) + " wurde von Spieler "
 						+ serverResponse.getSieger()
 						+ " gewonnen! (Aus Server Sicht)");
 		
 				this.endSet();
 			} else { // sonst warten auf neachsten Zug
 				this.clientStatus = FourInARowGame.WAITING;
 			}
 
 			// alle Oberserver informieren
 			setChanged();
 			notifyObservers(serverResponse);
 
 		}
 
 	}
 }

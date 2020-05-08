 package game;
 
 import gui.Gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import rules.Rules;
 
 import components.Field;
 
 /**
  * Die "Hauptklasse" des Schachroboters, hier wird das Spiel gestartet.
  * @author Florian Franke
  * 23.10.2012
  *
  */
 public class GameCoordinator
 {
 	/**
 	 * Private Variable, die die Instanz des GameCoordinators enthaelt.
 	 */
 	private static GameCoordinator instance = null;
 	
 	/**
 	 * Enthaelt das Spielfeld
 	 */
 	public Field field;
 	
 	/**
 	 * Enthaelt alle Zuege des aktuellen Spiels
 	 */
 	private List<Move> moves = new ArrayList<Move>();
 	
 	/**
 	 * Enthaelt den aktuellen Zug
 	 */
 	private Move currentMove = null;
 	
 	/**
 	 * Gui-Objekt
 	 */
 	private Gui gui;
 	
 	/**
 	 * Rules-Objekt
 	 */
 	private Rules rules = new Rules();
 	
 	/**
 	 * Ist der aktuelle Zug der letzte?
 	 */
 	private boolean lastMove = false;
 	
 	/**
 	 * Sollen die Zuege etc. ausgegeben werden?
 	 */
 	private boolean logging = false;
 	
 	
 	/**
 	 * Privater Konstruktor, damit nur eine Instanz vom GameCoordintor erstellt werden kann.
 	 */
 	private GameCoordinator(boolean logging)
 	{
 		// Feld-Instanz holen
 		this.field = Field.getInstance();
 		
 		// Logging?
 		this.logging = logging;
 	}
 	
 	/**
 	 * Gibt zurueck, ob das Spiel beendet ist.
 	 * @return True: Spiel beendet; False: Spiel laeuft noch
 	 */
 	public boolean isEndOfGame()
 	{
 		return this.lastMove;
 	}
 	
 	/**
 	 * Fuehrt einen Zug aus.
 	 * Der Zug soll beim Aufruf gueltig sein!
 	 */
 	public void execMove()
 	{
 		// Zug ausgeben?
 		if (this.logging)
 			System.out.println(this.currentMove.getMoveAsText());
 		
 		// Wurde geschmissen?
 		if (this.currentMove.isCaptured()) {
 			// Geschmissene Figur vom Feld entfernen
 			this.field.removeFigureAt(this.currentMove.getFieldTo());
 			
 			// TODO Roboter soll Figur entfernen
 		}
 		
 		// TODO Roboter soll Figur bewegen
 		
 		// Gui soll Figur bewegen
 		// Gui muss zuerst den Zug grafisch ausfuehren, da sie auf die
 		// Informationen des Feldes (fieldFrom) zugreift.
 		// Wuerde Field zuerst aktualisiert werden, koennte die Gui nicht
 		// mehr auf die zu versetzende Figur zugreifen!
 		this.gui.getCheckerboard().setCheckerboardInformation(this.currentMove);
 		
 		// Figur soll Zug durchfuehren
 		this.field.moveFigure(this.currentMove.getFieldFrom(), this.currentMove.getFieldTo());
 		
 		// War es der letzte Zug?
 		this.lastMove = this.currentMove.isCheckMate();
 		
 		// TODO Wurde inzwischen die GUI beendet? => Spiel ist beendet
 	}
 	
 	/**
 	 * Neuer Zug wird dem aktuellen Spielverlauf hinzugefuegt.
 	 * Entweder ein vom Spieler ausgefuerter Zug oder von der AI.
 	 * @return True: Gueltiger Zug wurde gespeichert;
 	 * False: Ungueltiger Zug, Fehlermeldung anzeigen
 	 */
 	public boolean receiveMove(Move newMove)
 	{
 		// Zug soll ausgefuehrt werden
 		// Zug ueberpruefen
		if (!this.rules.checkMove(this.field, newMove)) {
 			// Fehlermeldung anzeigen (GUI)
 			this.gui.showWarning("Ungueltiger Zug!");
 			System.out.println("Ungueltiger Zug laut Rules.checkMove()");
 			return false;
 		} else {
 			// currentMove aktualisieren
 			this.currentMove = newMove;
 			// Figurtyp bestimmen
 			this.currentMove.setFigure();
 			// Aktuellen Zug hinzufuegen
 			this.moves.add(this.currentMove);
 			return true;
 		}
 	}
 	
 	/**
 	 * Setzt die Gui
 	 * @param gui GUI
 	 */
 	public void setGui(Gui gui)
 	{
 		this.gui = gui;
 	}
 	
 	/**
 	 * Gibt alle Zuege des Spiels zurueck
 	 * @return ArrayList mit allen Zuegen
 	 */
 	public List<Move> getAllMoves()
 	{
 		return this.moves;
 	}
 	
 	/**
 	 * Um die Instanz des GameCoordinators zu holen.
 	 * @return GameCoordinator-Instanz
 	 */
 	public static GameCoordinator getInstance(boolean logging)
 	{
 		if (instance == null)
 			instance = new GameCoordinator(logging);
 		
 		return instance;
 	}
 }

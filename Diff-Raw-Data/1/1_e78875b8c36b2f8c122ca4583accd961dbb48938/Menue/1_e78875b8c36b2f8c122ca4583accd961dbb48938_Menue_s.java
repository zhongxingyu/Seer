 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.WindowConstants;
 
 /**
  * Beinhaltet die Main- Methode , erstellt Fenster & Menue , startet & beendet
  * das Programm , verwaltet Tastatureingaben und fuehrt Neustart des Spiels
  * durch
  * 
  * @author Kolja Salewski
  */
 public class Menue implements KeyListener {
 
 	// Deklaration & Initialisierung:
 
 	/**
 	 * Hauptframe des Programmes
 	 */
 	static JFrame frame;
 
 	public static Sound sound = new Sound();
 
 	public static boolean theme;
 
 	public static boolean mapLoaded = false;
 
 	// private int anzahlLevel = 2; // nacher levelanzahl get methode;
 	//
 	//
 	// private JFrame frame;
 	//
 	// /**
 	// * enthaelt .wav-Datei fuer Explosionsgeraeusch der Bombe
 	// */
 	// public static Sound exp = new Sound("explosion.wav");
 
 	/**
 	 * true, wenn Bot aktiviert ist
 	 */
 	public static boolean bot = false;
 
 	/**
 	 * Bot1-Objekt (Bestimmung der Bewegung und Bombenaktion fuer Bot1)
 	 */
 	public static Bot bot1;
 
 	/**
 	 * Button im Leistenmenue ( Schliesst das Programm )
 	 */
 	private final Action_Beenden Action_Beenden = new Action_Beenden(); // Aktion
 																		// zum
 																		// Beenden
 																		// des
 																		// Spiels
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Neustart des Spieles )
 	 */
 	private final Action_Neu Action_Neu = new Action_Neu(); // Aktion zum
 															// Neustart des
 															// Spiels erstellen
 
 	/**
 	 * Button im Leistenmenue ( Laden des Spieles )
 	 */
 	private final Action_Load Action_Load = new Action_Load(); // Aktion zum
 																// Neustart des
 																// Spiels
 																// erstellen
 	/**
 	 * Button im Leistenmenue ( Speichern des Spieles )
 	 */
 	private final Action_Save Action_Save = new Action_Save(); // Aktion zum
 																// Neustart des
 																// Spiels
 																// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zum Singleplayer - Modus )
 	 */
 	private final Action_Singleplayer Action_Singleplayer = new Action_Singleplayer(); // Aktion
 																						// zum
 																						// Wechsel
 																						// in
 																						// den
 																						// Singleplayer-Modus
 																						// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zum HotSeat - Modus )
 	 */
 	private final Action_HotSeat Action_HotSeat = new Action_HotSeat(); // Aktion
 																		// zum
 																		// Wechsel
 																		// in
 																		// den
 																		// HotSeat-Modus
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zum Server - Modus )
 	 */
 	private final Action_Server Action_Server = new Action_Server(); // Aktion
 																		// zum
 																		// Wechsel
 																		// in
 																		// den
 																		// Server-Modus
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zum Client - Modus )
 	 */
 	private final Action_Client Action_Client = new Action_Client(); // Aktion
 																		// zum
 																		// Wechsel
 																		// in
 																		// den
 																		// Client-Modus
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zu Level 1)
 	 */
 	private final Action_Level_1 Action_Level_1 = new Action_Level_1(); // Aktion
 																		// zum
 																		// Wechsel
 																		// zu
 																		// Level
 																		// 1
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zu Level 2)
 	 */
 	private final Action_Level_2 Action_Level_2 = new Action_Level_2(); // Aktion
 																		// zum
 																		// Wechsel
 																		// zu
 																		// Level
 																		// 2
 																		// erstellen
 
 	/**
 	 * Button im Leistenmenue ( Wechsel zu Map - Editor )
 	 */
 	private final Action_Map_Editor_start Action_Map_Editor_start = new Action_Map_Editor_start(); // Aktion
 	// zum
 	// Wechsel
 	// zu
 	// Map-
 	// Editor
 	//
 	private final Action_Map_Editor_contin Action_Map_Editor_contin = new Action_Map_Editor_contin(); // Aktion
 	// zum
 	// Wechsel
 	// zu
 	// Map-
 	// Editor
 	// erstellen
 
 	private final Action_MultiplayerBot Action_MultiplayerBot = new Action_MultiplayerBot(); // Aktion
 	//zum Wechsel
 	//in den
 	//botgesteuerten
 	//Multiplayer-Modus
 	//erstellen
 
 	/**
 	 * Buttons für den Schwierigkeitsgrad ( schweirigkeit ist Zeitabhängig )
 	 * 
 	 * @author Andrej Morlang
 	 */
 	private final Action_noob Action_noob = new Action_noob(); // Aktion zum
 																// setzen der
 																// Schwierigkeit
 																// (Anfänger)..
 	private final Action_Leicht Action_Leicht = new Action_Leicht(); // ..
 																		// (Leicht)
 																		// ..
 	private final Action_Mittel Action_Mittel = new Action_Mittel(); // ..
 																		// (Mittel)
 																		// ..
 	private final Action_Schwer Action_Schwer = new Action_Schwer(); // ..
 																		// (Schwer).
 	private final Action_Config Action_Config = new Action_Config(); // Aktion
 	//zum Wechsel
 	// Konfigurieren der Einstellungen!
 	//erstellen
 
 	static boolean twoPlayer = false;
 	static boolean hotSeat = false;
 	static boolean lan = false;
 	static Server serverThread;
 	static Client clientThread;
 	static boolean antwort_erhalten = false;
 	static Window win;
 	static int zeit = 0;
 	static Zeit spieltimer = new Zeit(); // objekt zeit (mit Zeitlimit)
 	static boolean anfrage_geschickt = false;
 	private static boolean running; //Abfrage für den Timer
 	private static JLabel zeitAnzeige;
 	static Timer tim;
 
 	private static int[][] map; // Internes Spielfeld
 	/**
 	 * Objekt der MapEditor - Klasse ; enthaelt den editor ;
 	 */
 	private static MapEditor mapping;
 	static boolean editorlaeuft = false;
 	/**
 	 * Objekt der Map - Klasse ; enthaelt die Daten des Spielfeldes ;
 	 */
 	private static Map game; // Grafisches Spielfeld
 
 	/**
 	 * enthaelt die Informationen ueber die Spielerposition ((x ,y) - Koordinate
 	 * )
 	 */
 	private static Hulk hulk1, hulk2; // Spielfiguren
 
 	private static int[] a; // Fortbewegung der Spielfiguren
 	public boolean spiel_neugestartet;
 	static int n = MapLoader.get_n(); // Breite & Hoehe des Spielfelds
 
 	/* Konstruktor: */
 	/**
 	 * {@code initialize()} legt die Panels in das JFrame {@code frame()} &
 	 * erstellt die grafische Oberflaeche des Spieles
 	 */
 	public Menue() {
 		spiel_neugestartet = false;
 
 		map = MapLoader.laden(MapLoader.get_level());
 		game = new Map(map);
 
 		int hulk1Startx, hulk1Starty, hulk2Startx, hulk2Starty;
 
 		hulk1Startx = MapLoader.get_icon_x(map, 1);
 		hulk1Starty = MapLoader.get_icon_y(map, 1);
 		// Startposition von Hulk 2 aus dem Spielfeld auslesen
 		hulk2Startx = MapLoader.get_icon_x(map, 10);
 		hulk2Starty = MapLoader.get_icon_y(map, 10);
 
 		hulk1 = new Hulk(hulk1Startx, hulk1Starty, 1); // 1. Spielerfigur erzeugen // getHulkfunktion anwenden
 		hulk2 = new Hulk(hulk2Startx, hulk2Starty, 10); // 2. Spielerfigur erzeugenget hulk 2 funktion
 		bot1 = new Bot();	//1. Bot erzeugen
 
 		initialize();
 		a = new int[3];
 	}
 
 	/* METHODEN: */
 
 	private static void botStart() {
 		if (bot1.getStart() == 0)
 			bot1.start();
 	}
 
 	// initialize-Methode:
 	/**
 	 * Initialisiert das Spielfeld ( Panels , frames etc .),
 	 */
 	private void initialize() {
 		frame = new JFrame(); // Fenster erstellen
 		frame.setTitle("Bomberhulk"); // Fenstertitel setzen
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Programm beim
 																// Schliessen
 																// des Fensters
 																// beenden
 		frame.setResizable(true); // Fenster soll nicht skalierbar sein
 		frame.pack();
 
 		JMenuBar menuBar = new JMenuBar(); // Menueleiste erstellen
 		frame.setJMenuBar(menuBar); // Menueleiste hinzufuegen
 
 		menuBar.isFocusable();
 
 		JMenu mnSpiel = new JMenu("Spiel"); // Menuepunkt "Spiel" erstellen
 		menuBar.add(mnSpiel); // Menuepunkt "Spiel" hinzufuegen
 
 		JMenuItem mntmNeu = new JMenuItem("Neu"); // Untermenuepunkt "Neu"
 													// erstellen
 		mnSpiel.add(mntmNeu); // Untermenuepunkt "Neu" hinzufuegen
 		mntmNeu.setAction(Action_Neu); // Aktion "Action_Neu" hinzufuegen
 
 		JMenuItem mntmSave = new JMenuItem("Speichern"); // Untermenuepunkt
 															// "Speichern"
 															// erstellen
 		mnSpiel.add(mntmSave); // Untermenuepunkt "Save" hinzufuegen
 		mntmSave.setAction(Action_Save); // Aktion "Action_Save" hinzufuegen
 
 		JMenuItem mntmLoad = new JMenuItem("Laden"); // Untermenuepunkt
 														// "Laden"
 														// erstellen
 		mnSpiel.add(mntmLoad); // Untermenuepunkt "Load" hinzufuegen
 		mntmLoad.setAction(Action_Load); // Aktion "Action_Load" hinzufuegen
 
 		JMenuItem mntmBeenden = new JMenuItem("Beenden"); // Untermenuepunkt
 															// "Beenden"
 															// erstellen
 		mnSpiel.add(mntmBeenden); // Untermenuepunkt "Beenden" hinzufuegen
 		mntmBeenden.setAction(Action_Beenden); // Aktion "Action_Beenden"
 												// hinzufuegen
 
 		JMenu mnModus = new JMenu("Modus"); // Menuepunkt "Modus" erstellen
 		menuBar.add(mnModus); // Menuepunkt "Modus" hinzufuegen
 
 		JMenuItem mntmSingleplayer = new JMenuItem("Singleplayer"); // Untermenuepunkt
 																	// "Singleplayer"
 																	// erstellen
 		mnModus.add(mntmSingleplayer); // Untermenuepunkt "Singleplayer"
 										// hinzufuegen
 		mntmSingleplayer.setAction(Action_Singleplayer); // Aktion
 															// "Action_Singleplayer"
 															// hinzufuegen
 
 		JMenu mnMultiplayer = new JMenu("Multiplayer"); // Untermenue
 														// "Multiplayer"
 														// erstellen
 		mnModus.add(mnMultiplayer); // Untermenue "Multiplayer" hinzufuegen
 
 		JMenuItem mntmHotSeat = new JMenuItem("Hot Seat"); // Untermenuepunkt
 															// "Hot Seat"
 															// erstellen
 		mnMultiplayer.add(mntmHotSeat); // Untermenuepunkt "Hot Seat"
 										// hinzufuegen
 		mntmHotSeat.setAction(Action_HotSeat); // Aktion "Action_HotSeat"
 												// hinzufuegen
 
 		JMenuItem mntmMultiplayerBot = new JMenuItem("Bot"); // Menueunterpunkt
 																// "Bot"
 																// erstellen
 		mnMultiplayer.add(mntmMultiplayerBot); // Menueunterpunkt "Multiplayer -
 												// Bot hinzugefuegen
 		mntmMultiplayerBot.setAction(Action_MultiplayerBot);
 
 		JMenu mnLAN = new JMenu("LAN"); // Untermenue
 										// "LAN"
 										// erstellen
 		mnMultiplayer.add(mnLAN); // Untermenue "LAN" hinzufuegen
 
 		JMenuItem mntmServer = new JMenuItem("Server"); // Untermenuepunkt
 														// "Server" erstellen
 		mnLAN.add(mntmServer); // Untermenuepunkt "Server" hinzufuegen
 		mntmServer.setAction(Action_Server); // Aktion "Action_Server"
 												// hinzufuegen
 
 		JMenuItem mntmClient = new JMenuItem("Client"); // Untermenuepunkt
 														// "Client" erstellen
 		mnLAN.add(mntmClient); // Untermenuepunkt "Client" hinzufuegen
 		mntmClient.setAction(Action_Client); // Aktion "Action_Client"
 												// hinzufuegen
 
 		JMenu mnLevel = new JMenu("Level"); // Menuepunkt "Level" erstellen
 		menuBar.add(mnLevel); // Menuepunkt "Level" hinzufuegen
 
 		JMenuItem mntmLevel_1 = new JMenuItem("1"); // Untermenuepunkt
 													// "1" erstellen
 		mnLevel.add(mntmLevel_1); // Untermenuepunkt "1" hinzufuegen
 		mntmLevel_1.setAction(Action_Level_1); // Aktion "Action_Level_1"
 												// hinzufuegen
 
 		JMenuItem mntmLevel_2 = new JMenuItem("2"); // Untermenuepunkt
 													// "2" erstellen
 		mnLevel.add(mntmLevel_2); // Untermenuepunkt "2" hinzufuegen
 		mntmLevel_2.setAction(Action_Level_2); // Aktion "Action_Level_2"
 												// hinzufuegen
 
 		JMenu mnEditor = new JMenu("Map - Editor"); // Menuepunkt "Editor" erstellen
 		menuBar.add(mnEditor); // Menuepunkt "Editor" hinzufuegen
 
 		JMenuItem mntmOpen = new JMenuItem("Open"); // Untermenuepunkt "Open"
 													// erstellen
 		mnEditor.add(mntmOpen); // Untermenuepunkt "Open" hinzufuegen
 		mntmOpen.setAction(Action_Map_Editor_start); // Aktion "Action_Map_Editor_start"
 		// hinzufuegen
 
 		JMenuItem mntmContin = new JMenuItem("Contin"); // Untermenuepunkt "Open"
 		// erstellen
 		mnEditor.add(mntmContin); // Untermenuepunkt "Open" hinzufuegen
 		mntmContin.setAction(Action_Map_Editor_contin); // Aktion "Action_Map_Editor_contin"
 		// hinzufuegen
 
 		JMenu mnZeit = new JMenu("Schwierigkeit"); // Menuepunkt "Schwierigkeit"
 													// erstellen
 		menuBar.add(mnZeit); // Menuepunkt "Schwierigkeit" hinzufuegen
 
 		JMenuItem noob = new JMenuItem("Anfänger"); // unterpunkt "Anfänger"
 													// erstellen..
 		mnZeit.add(noob); // .. hinzufügen zur Schwierigkeit (Oberpunkt)
 		noob.setAction(Action_noob); // .. befehl zum Ausführen erstellen
 
 		JMenuItem Leicht = new JMenuItem("Leicht"); // dasselbe schema wie vier
 													// zeilen drüber
 		mnZeit.add(Leicht);
 		Leicht.setAction(Action_Leicht);
 
 		JMenuItem Mittel = new JMenuItem("Mittel");
 		mnZeit.add(Mittel);
 		Mittel.setAction(Action_Mittel);
 
 		JMenuItem Schwer = new JMenuItem("Schwer");
 		mnZeit.add(Schwer);
 		Schwer.setAction(Action_Schwer);
 
 		JMenu mnOption = new JMenu("Einstellungen"); // Menuepunkt "Modus" erstellen
 		menuBar.add(mnOption); // Menuepunkt "Modus" hinzufuegen
 
 		JMenuItem mntmConfig = new JMenuItem("Konfigurieren"); // Untermenuepunkt
 																// "Singleplayer"
 																// erstellen
 		mnOption.add(mntmConfig); // Untermenuepunkt "Singleplayer"
 									// hinzufuegen
 		mntmConfig.setAction(Action_Config); // Aktion
 												// "Action_Singleplayer"
 												// hinzufuegen
 
 		int anzahl = 2;
 
 		JPanel south = new JPanel(new GridLayout(1, anzahl));
 		zeitAnzeige = new JLabel("" + Zeit.get_restZeit());
 
 		// Deklarationen oder Aktuelle Meldungen einfuegen
 		JButton exit_button = new JButton("Beenden");
 		ActionListener exit = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		};
 
 		// Labels für Zeit oder Meldungen!
 
 		exit_button.addActionListener(exit);
 		south.add(zeitAnzeige);
 		south.add(exit_button);
 		frame.getContentPane().add(south, BorderLayout.SOUTH);// south - Panel hinzufuegen
 		frame.getContentPane().add(game); // Spielfeld hinzufuegen
 
 		game.bilder_skalieren();
 		game.init(); // Spielfeld zeichnen
 		game.addKeyListener(this); // Keylistener zum Spielfeld hinzufuegen
 		game.setFocusable(true); // Spielfeld fokussierbar machen
 		game.requestFocus(); // Fokus auf Spielfeld setzen
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.addComponentListener(new ComponentListener() {
 			public void componentResized(ComponentEvent e) {
 				frame.setSize(frame.getSize().height, frame.getSize().height);
 				game.breite = frame.getSize().width / 13;
 				game.hoehe = frame.getSize().height / 13;
 				System.out.println("Fenster skaliert");
 				game.bilder_skalieren();
 				game.removeAll(); // ...entferne alle bisherigen Komponenten vom Panel...
 				game.refresh();
 			}
 
 			public void componentMoved(ComponentEvent e) {
 
 			}
 
 			public void componentShown(ComponentEvent e) {
 
 			}
 
 			public void componentHidden(ComponentEvent e) {
 
 			}
 		});
 	}
 
 	//    frame.addComponentListener(new ComponentListener() {
 	//    	public void componentResized(ComponentEvent e) {
 	//    		game.breite = frame.getSize().width;
 	//    		game.hoehe = frame.getSize().height;
 	//    		System.out.println("Fenster skaliert");
 	//            game.refresh();
 	//        }
 	//    	
 	//    	public void componentMoved(ComponentEvent e) {
 	//        }
 	//     
 	//        public void componentShown(ComponentEvent e) {
 	//        }
 	//     
 	//        public void componentHidden(ComponentEvent e) {
 	//        }
 	//        
 	//    	});
 
 	// keyPressed-Methode:
 	/**
 	 * Horcht , ob eine Taste gedrueckt wurde und wertet die Aktion gegebenfalls
 	 * aus . Gueltige Aktionen sind : Hoch -, Links -, Rechts -, Runtertaste (
 	 * Bewegung ) und Leertaste ( Bombe ) für den 1. Spieler sowie W, A, S, D (
 	 * Bewegung ) und E ( Bombe ) für den 2. Spieler
 	 */
 	public void keyPressed(KeyEvent Key) {
 		// Key-Methoden fuer 1. Spieler
 		// Pfeiltaste oben:
 		if (Key.getKeyCode() == KeyEvent.VK_UP) {
 			System.out.println("Oben S1"); // Test
 			System.out.println(); // Test
 
 			a[0] = 0;
 			a[1] = -1;
 			a[2] = 1;
 		}
 
 		// Pfeiltaste links:
 		else if (Key.getKeyCode() == KeyEvent.VK_LEFT) {
 			System.out.println("Links S1"); // Test
 			System.out.println(); // Test
 
 			a[0] = -1;
 			a[1] = 0;
 			a[2] = 1;
 		}
 
 		// Pfeiltaste rechts:
 		else if (Key.getKeyCode() == KeyEvent.VK_RIGHT) {
 			System.out.println("Rechts S1"); // Test
 			System.out.println(); // Test
 
 			a[0] = 1;
 			a[1] = 0;
 			a[2] = 1;
 		}
 
 		// Pfeiltaste unten:
 		else if (Key.getKeyCode() == KeyEvent.VK_DOWN) {
 			System.out.println("Unten S1"); // Test
 			System.out.println(); // Test
 
 			a[0] = 0;
 			a[1] = 1;
 			a[2] = 1;
 		}
 
 		// Leertaste (Bombe):
 		else if (Key.getKeyCode() == KeyEvent.VK_SPACE) {
 			if (clientThread != null) {
 				spieler2_bombe();
 				System.out.println("Spieler 2 hat Bombe gelegt");
 
 				clientThread.out.println("bomb");
 			}
 
 			else if (serverThread != null) {
 				spieler1_bombe();
 				System.out.println("Spieler 1 hat Bombe gelegt");
 
 				if (serverThread.verbunden) {
 					serverThread.out.println("bomb");
 				}
 
 			}
 
 			else {
 				spieler1_bombe();
 			}
 
 		}
 
 		// Key-Methoden fuer 2. Spieler
 		// Taste W (oben):
 		else if (Key.getKeyCode() == KeyEvent.VK_W && hotSeat) {
 			System.out.println("Oben S2"); // Test
 			System.out.println(); // Test
 
 			a[0] = 0;
 			a[1] = -1;
 			a[2] = 2;
 		}
 
 		// Taste A (links):
 		else if (Key.getKeyCode() == KeyEvent.VK_A && hotSeat) {
 			System.out.println("Links S2"); // Test
 			System.out.println(); // Test
 
 			a[0] = -1;
 			a[1] = 0;
 			a[2] = 2;
 		}
 
 		// Taste S (unten):
 		else if (Key.getKeyCode() == KeyEvent.VK_S && hotSeat) {
 			System.out.println("Unten S2"); // Test
 			System.out.println(); // Test
 
 			a[0] = 0;
 			a[1] = 1;
 			a[2] = 2;
 		}
 
 		// Taste D (rechts):
 		else if (Key.getKeyCode() == KeyEvent.VK_D && hotSeat) {
 			System.out.println("Rechts S2"); // Test
 			System.out.println(); // Test
 
 			a[0] = 1;
 			a[1] = 0;
 			a[2] = 2;
 		}
 
 		// Taste E (Bombe):
 		else if (Key.getKeyCode() == KeyEvent.VK_E && hotSeat) {
 			spieler2_bombe();
 		}
 
 		else {
 			a[0] = 0;
 			a[1] = 0;
 		}
 
 		// Bewegungen durchfuehren:
 		if (Key.getKeyCode() != KeyEvent.VK_SPACE
 				&& Key.getKeyCode() != KeyEvent.VK_E
 				&& !(a[0] == 0 && a[1] == 0)) {
 
 			// wenn Spieler 1 Bewegungen durchfuehrt
 			if (a[2] == 1) {
 				spieler1_aktionen(a[0], a[1]);
 			}
 
 			// wenn Spieler 2 Bewegungen durchfuehrt
 			if (a[2] == 2 && hotSeat) {
 				spieler2_aktionen(a[0], a[1]);
 			}
 
 		}
 
 	}
 
 	// spieler1_bombe-Methode:
 	/**
 	 * Ueberprueft , ob der 1. Spieler noch eine Bombe legen darf , verringert
 	 * ggf . die max . Bomben - Anzahl und ruft die bombe_legen - Methode aus
 	 * der Map - Klasse auf
 	 */
 	static void spieler1_bombe() {
 		System.out.println("Bombe S1"); // Test
 		System.out.println(); // Test
 
 		if ((Menue.get_hulk(1).get_max_bomben()) > 0) { // falls der 1.
 														// Spieler (noch)
 														// eine Bombe legen
 														// darf...
 			System.out.println("max_bomben S1 vor Legen: "
 					+ Menue.get_hulk(1).get_max_bomben()); // Test
 			System.out.println(); // Test
 
 			Menue.get_hulk(1).set_max_bomben(
 					(Menue.get_hulk(1).get_max_bomben()) - 1); 	// ...dekrementiere
 																// die
 																// Anzahl
 																// der
 																// maximalen
 																// Bomben
 																// von
 																// Spieler 1
 																// um 1,
 
 			System.out.println("max_bomben S1 nach Legen: "
 					+ Menue.get_hulk(1).get_max_bomben()); // Test
 			System.out.println(); // Test
 
 			game.bombe_legen(1); 	// ...lass den 1. Spieler eine Bombe legen,
 									// ...
 
 			game.removeAll(); 	// ...entferne alle bisherigen Komponenten vom
 								// Panel und...
 			game.refresh(); 	// ...zeichne alle Komponenten des Panels neu
 		}
 
 	}
 
 	// spieler2_bombe-Methode:
 	/**
 	 * Ueberprueft , ob der 2. Spieler noch eine Bombe legen darf , verringert
 	 * ggf . die max . Bomben - Anzahl und ruft die bombe_legen - Methode aus
 	 * der Map - Klasse auf
 	 */
 	static void spieler2_bombe() {
 		System.out.println("Bombe S2"); // Test
 		System.out.println(); 			// Test
 
 		if ((Menue.get_hulk(2).get_max_bomben()) > 0) { // falls der 2.
 														// Spieler (noch)
 														// eine Bombe legen
 														// darf...
 			System.out.println("max_bomben S2 vor Legen: "
 					+ Menue.get_hulk(2).get_max_bomben()); // Test
 			System.out.println(); // Test
 
 			Menue.get_hulk(2).set_max_bomben(
 					(Menue.get_hulk(2).get_max_bomben()) - 1); 	// ...dekrementiere
 																// die
 																// Anzahl
 																// der
 																// maximalen
 																// Bomben
 																// von
 																// Spieler 2
 																// um 1,
 
 			System.out.println("max_bomben S2 nach Legen: "
 					+ Menue.get_hulk(2).get_max_bomben()); 	// Test
 			System.out.println(); 							// Test
 
 			game.bombe_legen(2); 	// ...lass den 2. Spieler eine Bombe legen,
 									// ...
 
 			game.removeAll();	// ...entferne alle bisherigen Komponenten vom
 								// Panel und...
 			game.refresh(); 	// ...zeichne alle Komponenten des Panels neu
 		}
 	}
 
 	// spieler1_aktionen-Methode:
 	/**
 	 * Ueberprueft , ob der Spieler mit den Pfeiltasten im Client -, Server -
 	 * oder Einspieler - Modus ist . Je nach Modus : Client : 2. Spielfigur
 	 * bewegen & Bewegungen dem Server mitteilen ; Server : 1. Spielfigur
 	 * bewegen & Bewegungen dem Client mitteilen ; Einspieler : 1. Spielfigur
 	 * bewegen
 	 */
 	void spieler1_aktionen(int x, int y) {
 		// Bewegung Spieler 1
 		if (clientThread != null) {
 			spieler2_aktionen(x, y);
 			System.out.println("x-Bewegung von Spieler 2: " + x);
 			System.out.println("y-Bewegung von Spieler 2: " + y);
 
 			clientThread.out.println("" + x);
 			clientThread.out.println("" + y);
 		}
 
 		else if (serverThread != null) {
 			spieler1_aktionen2(x, y);
 			System.out.println("x-Bewegung von Spieler 1: " + x);
 			System.out.println("y-Bewegung von Spieler 1: " + y);
 
 			if (serverThread.verbunden) {
 				serverThread.out.println("" + x);
 				serverThread.out.println("" + y);
 			}
 
 		}
 
 		else {
 			spieler1_aktionen2(x, y);
 		}
 
 	}
 
 	// spieler1_aktionen2-Methode:
 	/**
 	 * Ueberprueft , ob sich die 1. Spielfigur in die gewuenschte Richtung
 	 * bewegen kann . Je nachdem , welches Feld als nächstes betreten wird ,
 	 * werden unterschiedliche Aktionen durchgefuehrt ( Bewegung , Sieg ,
 	 * Niederlage ).
 	 */
 	static void spieler1_aktionen2(int x, int y) {
 		if (Map.map[hulk1.get_x() + x][hulk1.get_y() + y] == 2 				// falls
 				// das
 				// naechste
 				// Feld
 				// ein
 				// Weg-Feld,...
 				|| Map.map[hulk1.get_x() + x][hulk1.get_y() + y] == 12 		// ...oder
 				// Bomben-Item-Feld...
 				|| Map.map[hulk1.get_x() + x][hulk1.get_y() + y] == 15) { 	// ...oder
 																			// Flammen-Item
 																			// Feld
 																			// ist...
 
 			game.move_Hulk(x, y, 1); 	// ...dann bewege
 										// Spielerfigur 1 auf
 										// dem Spielfeld,...
 			game.removeAll(); 	// ...entferne alle bisherigen Komponenten
 								// vom Panel...
 			game.refresh(); // ...und zeichne alle Komponenten des
 							// Panels neu
 			a[0] = 0;
 			a[1] = 0;
 		}
 
 		// Sieg Spieler 1
 		else if (Map.map[hulk1.get_x() + x][hulk1.get_y() + y] == 7) { 	// falls
 																		// das
 																		// naechste
 																		// Feld
 																		// das
 																		// Ziel-Feld
 																		// ist
 			System.out.println("Spieler 1 hat gewonnen"); 	// Test
 			System.out.println(); 							// Test
 			sound.playZiel();
 			abfrage_neustarten();
 		}
 
 		// Niederlage Spieler 1
 		else if (Map.map[hulk1.get_x() + x][hulk1.get_y() + y] == 6) { 	// falls
 																		// das
 																		// naechste
 																		// Feld
 																		// ein
 																		// Explosions-Feld
 																		// ist
 			System.out.println("Spieler 1 hat verloren"); 	// Test
 			System.out.println(); 							// Test
 			sound.playTod();
 
 			abfrage_neustarten();
 		}
 
 	}
 
 	// spieler2_aktionen-Methode:
 	/**
 	 * Ueberprueft , ob sich die 2. Spielfigur in die gewuenschte Richtung
 	 * bewegen kann . Je nachdem , welches Feld als nächstes betreten wird ,
 	 * werden unterschiedliche Aktionen durchgefuehrt ( Bewegung , Sieg ,
 	 * Niederlage ).
 	 */
 	static void spieler2_aktionen(int x, int y) {
 		// Bewegung Spieler 2
 		if (Map.map[hulk2.get_x() + x][hulk2.get_y() + y] == 2 				// falls
 				// das
 				// naechste
 				// Feld
 				// ein
 				// Weg-Feld,...
 				|| Map.map[hulk2.get_x() + x][hulk2.get_y() + y] == 12 		// ...oder
 				// Bomben-Item-Feld...
 				|| Map.map[hulk2.get_x() + x][hulk2.get_y() + y] == 15) { 	// ...oder
 																			// Flammen-Item
 																			// Feld
 																			// ist...
 
 			game.move_Hulk(x, y, 2); 	// ...dann bewege
 										// Spielfigur 2 auf dem
 										// Spielfeld,...
 			game.removeAll(); 	// ...entferne alle bisherigen Komponenten
 								// vom Panel...
 			game.refresh(); 	// ...und zeichne alle Komponenten des
 			// Panels neu
 			a[0] = 0;
 			a[1] = 0;
 		}
 
 		// Sieg Spieler 2
 		else if (Map.map[hulk2.get_x() + x][hulk2.get_y() + y] == 7) { 	// falls
 																		// das
 																		// naechste
 																		// Feld
 																		// das
 																		// Ziel-Feld
 																		// ist
 			System.out.println("Spieler 2 hat gewonnen"); 	// Test
 			System.out.println(); 							// Test
 			sound.playZiel();
 			abfrage_neustarten();
 		}
 
 		// Niederlage Spieler 2
 		else if (Map.map[hulk2.get_x() + x][hulk2.get_y() + y] == 6) { 	// falls
 																		// das
 																		// naechste
 																		// Feld
 																		// ein
 																		// Explosions-Feld
 																		// ist
 			System.out.println("Spieler 2 hat verloren"); 	// Test
 			System.out.println(); 							// Test
 			sound.playTod();
 
 			abfrage_neustarten();
 		}
 
 	}
 
 	public void keyTyped(KeyEvent Key) {
 	}
 
 	public void keyReleased(KeyEvent Key) {
 	}
 
 	//botStop-Methode
 	/**
 	 * beendet den Bot-Thread. Verwendung bei Wechsel des Spielmodus.
 	 */
 	static void botStop() {
 		bot1.interrupt();
 	}
 
 	// reset_Hulk-Methode:
 	/**
 	 * Setzt die beiden Spielfiguren auf ihre Startpositionen zurueck . Die 1.
 	 * Spielfigur landet immer in der oberen linken Ecke . Die 2. Spielfigur
 	 * landet immer in der unteren rechten Ecke .
 	 */
 	static void reset_Hulk() {
 		hulk1.set_x(1);
 		hulk1.set_y(1);
 
 		hulk2.set_x(n - 2);
 		hulk2.set_y(n - 2);
 
 		if (bot) {
 			bot1.set_x(n - 2);
 			bot1.set_y(n - 2);
 			botStart();
 		}
 	}
 
 	// spiel_neustarten-Methode:
 	/**
 	 * Startet das Spiel folgendermaßen neu : Zuruecksetzen der Spielfiguren ,
 	 * max . Anzahl Bomben und Bomben - Radien , Entfernen aktueller Bomben ,
 	 * Reinitialisieren der internen und grafischen Spielfelder. Erweitert fuer
 	 * Bot.
 	 */
 	static void spiel_neustarten() {
 
 		System.out.println("Spiel neugestartet"); 	// Test
 		System.out.println(); 						// Test
 
 		// Hulk zurueckpositionieren:
 		reset_Hulk();
 
 		// Maximale Anzahl an Bomben zuruecksetzen:
 		get_hulk(1).set_max_bomben(1);
 		get_hulk(2).set_max_bomben(1);
 
 		// Bomben-Radius zuruecksetzen:
 		get_hulk(1).set_bomben_radius(2);
 		get_hulk(2).set_bomben_radius(2);
 
 		if (bot) {
 			get_bot(1).set_bomben_radius(2);
 			get_bot(1).set_max_bomben(1);
 		}
 
 		// Gelegte Bomben entfernen:
 		for (int x = 0; x < n; x++) {
 			for (int y = 0; y < n; y++) {
 				game.bomb[x][y].liegt = false;
 			}
 
 		}
 
 		// Spielfeld intern reinitialisieren:
 		Map.set_map(MapLoader.laden(MapLoader.get_level()));
 
 		// Bilder erneut skalieren:
 		game.bilder_skalieren();
 
 		// Spielfeld grafisch reinitialisieren:
 		game.removeAll();
 		game.refresh();
 
 		// Boolean-Werte zuruecksetzen:
 		antwort_erhalten = false;
 		anfrage_geschickt = false;
 
 		System.out.println("antwort_erhalten nach neustart = "
 				+ antwort_erhalten); 	// Test
 		System.out.println(); 			// Test
 
 		System.out.println("anfrage_geschickt nach neustart = "
 				+ anfrage_geschickt); 	// Test
 		System.out.println(); 			// Test
 	}
 
 	// abfrage_neustarten-Methode:
 	/**
 	 * Fragt den Benutzer , ob er das Spiel neustarten oder beenden möchte und
 	 * fuehrt die jeweilige Aktion aus .
 	 */
 	static void abfrage_neustarten() {
 		int eingabe = 0;
 
 		if (serverThread != null) {
 			createAndShowGui(
 					"Spieler 2 wurde eine Anfrage zum Neustart des Spiels geschickt. Warte ",
 					" auf Antwort...", 60, 600, 100, 0, "", "neustart");
 		}
 
 		else {
 			eingabe = JOptionPane.showConfirmDialog(null,
 					"Möchten Sie noch eine Runde spielen?", "Spiel zuende",
 					JOptionPane.YES_NO_OPTION);
 
 			if (eingabe == 0) {
 				if (clientThread != null) {
 					clientThread.out
 							.println("Spieler 2 moechte das Spiel neustarten. Soll das Spiel neugestartet werden?");
 					createAndShowGui(
 							"Spieler 1 wurde eine Anfrage zum Neustart des Spiels geschickt. Warte ",
 							" auf Antwort...", 60, 600, 100, 0, "", "neustart");
 				}
 
 				else {
 					spiel_neustarten();
 				}
 
 				if (zeit != 0) {
 					spieltimer.timer.cancel();
 					spieltimer = new Zeit();
 					spieltimer.laufzeit(zeit); 	// die Zeit ist in Sekunden 180sek = 3min
 												// (Spielzeit/Rundenzeit)
 				}
 
 			}
 
 			else if (eingabe == 1) {
 				System.exit(0);
 			}
 
 		}
 
 	}
 
 	// singleplayer_starten-Methode:
 	/**
 	 * Startet den Singleplayer - Modus
 	 */
 	static void singleplayer_starten() {
 		twoPlayer = false;
 		hotSeat = false;
 		bot = false;
 		botStop();
 
 		if (lan == true) {
 			lan_modus_beenden();
 		}
 
 		System.out.println("Singleplayer-Modus aktiviert"); // Test
 		System.out.println(); 								// Test
 
 		spiel_neustarten();
 	}
 
 	// createAndShowGui-Methode:
 	/**
 	 * Oeffnet Timer - Dialog
 	 */
 	@SuppressWarnings("serial")
 	static void createAndShowGui(final String text_anfang,
 			final String text_ende, final int zeit, final int breite,
 			final int hoehe, final int level, final String schwierigkeitsgrad,
 			final String aktion) {
 		final JLabel label = new JLabel();
 		int timerDelay = 1000;
 		antwort_erhalten = false;
 		new Timer(timerDelay, new ActionListener() {
 			int timeLeft = zeit;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Window win = SwingUtilities.getWindowAncestor(label);
 				((JDialog) win)
 						.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 				if (timeLeft > 0 && antwort_erhalten == false) {
 					label.setText(text_anfang + timeLeft + " Sekunden"
 							+ text_ende);
 					win.setSize(breite, hoehe);
 					antwort_verarbeiten(win, e, level, schwierigkeitsgrad,
 							aktion);
 					timeLeft--;
 				}
 
 				else {
 					((Timer) e.getSource()).stop();
 					win.setVisible(false);
 				}
 
 			}
 
 		}) {
 			{
 				setInitialDelay(0);
 			}
 		}.start();
 
 		JOptionPane.showOptionDialog(frame, label, "",
 				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
 				new Object[] {}, null);
 
 		antwort_erhalten = false;
 	}
 
 	// antwort_verarbeiten-Methode:
 	/**
 	 * Verarbeitet die Server -/ Client - Antworten
 	 */
 	static void antwort_verarbeiten(Window win, ActionEvent e, int level,
 			String schwierigkeitsgrad, String aktion) {
 		if (serverThread != null) {
 			if (serverThread.antwort.equals("yes")) {
 				if (aktion.equals("level")) {
 					serverThread.out.println("level");
 					serverThread.out.println(level);
 					MapLoader.set_level(level);
 					// createAndShowGui("Spieler 2 moechte ebenfalls zu Level "
 					// + level + " wechseln. Es wird in ", " zu Level " + level
 					// + " gewechselt...", 5, 600, 100, level, "", "level"); // BITTE
 					// AUSKOMMENTIERT LASSEN
 				}
 
 				else if (aktion.equals("neustart")) {
 					serverThread.out.println("neustart");
 				}
 
 				else if (aktion.equals("schwierigkeitsgrad")) {
 					serverThread.out.println("schwierigkeitsgrad");
 					serverThread.out.println(schwierigkeitsgrad);
 
 					schwierigkeitsgrad_aendern(schwierigkeitsgrad);
 				}
 
 				//				else {
 				//					createAndShowGui(
 				//							"Spieler 2 moechte ebenfalls das Spiel neustarten. Das Spiel wird in ",
 				//							" neugestartet...", 5, 600, 100, 0);
 				//				}
 
 				spiel_neustarten();
 			}
 
 			//			else if (serverThread.antwort.equals("no")) {
 			//				antwort_erhalten = true;
 			//				((Timer) e.getSource()).stop();
 			//				win.setVisible(false);
 			//				serverThread.antwort = "leer";
 			//				if (level != 0) {
 			//					 Menue.createAndShowGui("Spieler 2 moechte nicht zu Level "
 			//					 + level + " wechseln.\nDas Spiel wird in ",
 			//					 " fortgesetzt...", 5, 300, 100, 0); // BITTE
 			//					 AUSKOMMENTIERT LASSEN & NICHT LOESCHEN
 			//				}
 			//
 			//				else {
 			//					Menue.createAndShowGui(
 			//							"Spieler 2 moechte das Spiel nicht neustarten.\nDas Spiel wird in ",
 			//							" fortgesetzt...", 5, 300, 100, 0);
 			//				}
 			//
 			//			}
 
 			if (!(serverThread.antwort.equals("leer"))) {
 				antwort_erhalten = true;
 				((Timer) e.getSource()).stop();
 				win.setVisible(false);
 				serverThread.antwort = "leer";
 			}
 
 		}
 
 		else if (clientThread != null) {
 			if (clientThread.antwort.equals("yes")) {
 				if (aktion.equals("level")) {
 					clientThread.out.println("level");
 					clientThread.out.println(level);
 					MapLoader.set_level(level);
 					// createAndShowGui("Spieler 1 moechte ebenfalls zu Level "
 					// + level + " wechseln. Es wird in ",
 					// " zu Level 1 gewechselt...", 5, 600, 100, level, "", "level"); //
 					// BITTE AUSKOMMENTIERT LASSEN & NICHT LOESCHEN
 				}
 
 				else if (aktion.equals("neustart")) {
 					clientThread.out.println("neustart");
 				}
 
 				else if (aktion.equals("schwierigkeitsgrad")) {
 					clientThread.out.println("schwierigkeitsgrad");
 					clientThread.out.println(schwierigkeitsgrad);
 
 					schwierigkeitsgrad_aendern(schwierigkeitsgrad);
 				}
 
 				//				else {
 				//					createAndShowGui(
 				//							"Spieler 1 moechte ebenfalls das Spiel neustarten. Das Spiel wird in ",
 				//							" neugestartet...", 5, 600, 100, 0);
 				//				}
 
 				spiel_neustarten();
 			}
 
 			//			else if (clientThread.antwort.equals("no")) {
 			//				antwort_erhalten = true;
 			//				((Timer) e.getSource()).stop();
 			//				win.setVisible(false);
 			//				clientThread.antwort = "leer";
 			//				if (level != 0) {
 			//					 Menue.createAndShowGui("Spieler 1 moechte nicht zu Level "
 			//					 + level + " wechseln.\nDas Spiel wird in ",
 			//					 " fortgesetzt...", 5, 300, 100, 0); // BITTE
 			//					 AUSKOMMENTIERT LASSEN & NICHT LOESCHEN
 			//				}
 			//
 			//				else {
 			//					Menue.createAndShowGui(
 			//							"Spieler 1 moechte das Spiel nicht neustarten.\nDas Spiel wird in ",
 			//							" fortgesetzt...", 5, 300, 100, 0);
 			//				}
 			//
 			//			}
 
 			if (!(clientThread.antwort.equals("leer"))) {
 				antwort_erhalten = true;
 				((Timer) e.getSource()).stop();
 				win.setVisible(false);
 				clientThread.antwort = "leer";
 			}
 
 		}
 
 	}
 
 	// schwierigkeitsgrad_aendern-Methode:
 	/**
 	 * Aendert den Schwierigkeitsgrad
 	 */
 	@SuppressWarnings("serial")
 	static void schwierigkeitsgrad_aendern(String schwierigkeitsgrad) {
 
 		System.out.println("antwort_erhalten vor neustart = "
 				+ antwort_erhalten); // Test
 		System.out.println(); // Test
 
 		System.out.println("anfrage_geschickt vor neustart = "
 				+ anfrage_geschickt); // Test
 		System.out.println(); // Test
 
 		if (clientThread != null && anfrage_geschickt == false
 				&& antwort_erhalten == false) {
 			anfrage_geschickt = true;
 			clientThread.out
 					.println("Spieler 2 moechte zum Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad
 							+ "' wechseln. Soll zum Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad + "' gewechselt werden?");
 			createAndShowGui(
 					"Spieler 1 wurde eine Anfrage zum Wechsel zu Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad + "' geschickt. Warte ",
 					" auf Antwort...", 60, 750, 100, 0, schwierigkeitsgrad,
 					"schwierigkeitsgrad");
 		}
 
 		else if (serverThread != null && anfrage_geschickt == false
 				&& antwort_erhalten == false) {
 			anfrage_geschickt = true;
 			serverThread.out
 					.println("Spieler 1 moechte zum Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad
 							+ "' wechseln. Soll zum Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad + "' gewechselt werden?");
 			createAndShowGui(
 					"Spieler 2 wurde eine Anfrage zum Wechsel zu Schwierigkeitsgrad '"
 							+ schwierigkeitsgrad + "' geschickt. Warte ",
 					" auf Antwort...", 60, 750, 100, 0, schwierigkeitsgrad,
 					"schwierigkeitsgrad");
 		}
 
 		else {
 			if (schwierigkeitsgrad.equals("Anfänger")) {
 				zeit = 0;
 
 			}
 
 			else if (schwierigkeitsgrad.equals("Leicht")) {
 				zeit = 180;
 
 			}
 
 			else if (schwierigkeitsgrad.equals("Mittel")) {
 				zeit = 90;
 
 			}
 
 			else if (schwierigkeitsgrad.equals("Schwer")) {
 				zeit = 45;
 
 			}
 			if (running)
 				tim.stop();
 			spiel_neustarten();
 			System.out.println(schwierigkeitsgrad); // Test
 			anfrage_geschickt = false;
 			antwort_erhalten = false;
 			int timerZeit = 1000;
 
 			running = true;
 			ActionListener action = new ActionListener() {
 				int timeLeft = zeit;
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (timeLeft > 0 && running) {
 
 						zeitAnzeige.setText("Restzeit: " + timeLeft);
 						timeLeft--;
 					}
 
 					else {
 						running = false;
 						tim.stop();
 
 					}
 				}
 			};
 			tim = new Timer(timerZeit, action) {
 				{
 					setInitialDelay(0);
 				}
 			};
 			tim.start();
 
 			if (zeit != 0) {
 				spieltimer.timer.cancel();
 				spieltimer = new Zeit(); 	// objekt zeit (mit Zeitlimit)
 				spieltimer.laufzeit(zeit);	// zeit in Sekunden
 				Zeit.set_restZeit(zeit);
 
 			}
 
 		}
 
 	}
 
 	// lan_modus_beenden-Methode:
 	/**
 	 * Beendet den LAN-Modus
 	 */
 	static void lan_modus_beenden() {
 		if (serverThread != null) {
 			serverThread.interrupt();
 
 			try {
 				if (serverThread.clientSocket != null) {
 					serverThread.clientSocket.close();
 				}
 				serverThread.serverSocket.close();
 			}
 
 			catch (IOException e1) {
 				e1.printStackTrace();
 			}
 
 			serverThread = null;
 			System.out.println("Server beendet"); 	// Test
 			System.out.println(); 					// Test
 		}
 
 		else if (clientThread != null) {
 			clientThread.interrupt();
 
 			try {
 				clientThread.socket.close();
 			}
 
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			clientThread = null;
 			System.out.println("Client beendet"); 	// Test
 			System.out.println(); 					// Test
 		}
 
 		lan = false;
 	}
 
 	/* INTERNE KLASSEN: */
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Beenden" , beendet das Spiel
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Beenden extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Beenden() {
 			putValue(NAME, "Beenden");
 			putValue(SHORT_DESCRIPTION, "Beenden des Spiels");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Neu" , startet das Spiel neu
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Neu extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Neu() {
 			putValue(NAME, "Neu");
 			putValue(SHORT_DESCRIPTION, "Neustart des Spiels");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (clientThread != null) {
 				clientThread.out
 						.println("Spieler 2 moechte das Spiel neustarten. Soll das Spiel neugestartet werden?");
 				createAndShowGui(
 						"Spieler 1 wurde eine Anfrage zum Neustart des Spiels geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 0, "", "neustart");
 			}
 
 			else if (serverThread != null) {
 				serverThread.out
 						.println("Spieler 1 moechte das Spiel neustarten. Soll das Spiel neugestartet werden?");
 				createAndShowGui(
 						"Spieler 2 wurde eine Anfrage zum Neustart des Spiels geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 0, "", "neustart");
 			}
 
 			else {
 				spiel_neustarten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Laden" , lädt ein zuvor
 	 * gespeichertes Spiel
 	 * 
 	 * @author Tobias Korfmacher
 	 */
 	private class Action_Load extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Laden - Buttons
 		 */
 		public Action_Load() {
 			putValue(NAME, "Laden");
 			putValue(SHORT_DESCRIPTION, "Laden eines Spiels");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Laedt einen zuvor gespeicherten Spielstand aus einer Datei .
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (lan == true) {
 				int antwort = JOptionPane.showConfirmDialog(null,
 						"Diese Aktion beendet den LAN-Modus. Fortfahren?",
 						"LAN-Modus", JOptionPane.YES_NO_OPTION);
 				switch (antwort) {
 				case 0:
 					lan_modus_beenden();
 					map = MapLoader.level_laden();
 					spiel_neustarten();
 					break;
 				case 1:
 					break;
 				}
 
 			}
 
 			else {
 				map = MapLoader.level_laden();
 				spiel_neustarten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Speichern" , speichert das aktuelle
 	 * Spiel
 	 * 
 	 * @author Tobias Korfmacher
 	 */
 	private class Action_Save extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Speichern - Buttons
 		 */
 		public Action_Save() {
 			putValue(NAME, "Speichern");
 			putValue(SHORT_DESCRIPTION, "Speichern des Spiels");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Speichert den aktuellen Spielstand in einer Datei .
 		 */
 		public void actionPerformed(ActionEvent e) {
 			MapLoader.level_speichern(map);
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Singleplayer" , wechselt zum
 	 * Singleplayer - Modus
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Singleplayer extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Singleplayer - Buttons
 		 */
 		public Action_Singleplayer() {
 			putValue(NAME, "Singleplayer");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Singleplayer-Modus");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Ueberprueft , ob man sich im Mehrspieler - Modus befindet und
 		 * wechselt ggf . zum Einzelspieler - Modus . Anschliessend wird das
 		 * Spiel neugestartet .
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (twoPlayer == true) {
 
 				singleplayer_starten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "HotSeat" , wechselt zum HotSeat -
 	 * Modus
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_HotSeat extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des HotSeat - Buttons
 		 */
 		public Action_HotSeat() {
 			putValue(NAME, "HotSeat");
 			putValue(SHORT_DESCRIPTION, "Wechsel in HotSeat-Modus");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Ueberprueft , ob man sich bereits im HotSeat - Modus befindet und
 		 * wechselt anderenfalls dorthin . Anschließend wird das Spiel
 		 * neugestartet .
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (hotSeat == false) {
 				twoPlayer = true;
 				hotSeat = true;
 
 				if (lan == true) {
 					lan_modus_beenden();
 				}
 
 				System.out.println("HotSeat-Modus aktiviert"); // Test
 				System.out.println(); // Test
 
 				spiel_neustarten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Server" , wechselt zum Server -
 	 * Modus
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Server extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Server - Buttons
 		 */
 		public Action_Server() {
 			putValue(NAME, "Server");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Server-Modus");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Ueberprueft , ob man sich bereits im Server - Modus befindet und
 		 * wechselt anderenfalls dorthin . Falls man sich vorher im Client -
 		 * Modus befunden hat , wird der Client - Thread beendet . Der Server -
 		 * Modus wird als neuer Thread gestartet .
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (serverThread == null) {
 				twoPlayer = true;
 				lan = true;
 				hotSeat = false;
 
 				if (clientThread != null) {
 					clientThread.interrupt();
 					System.out.println("Client beendet"); 	// Test
 					System.out.println(); 					// Test
 				}
 
 				System.out.println("Server-Modus aktiviert"); 	// Test
 				System.out.println(); 							// Test
 
 				serverThread = new Server();
 				serverThread.start();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Client" , wechselt zum Client -
 	 * Modus
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Client extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Client - Buttons
 		 */
 		public Action_Client() {
 			putValue(NAME, "Client");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Client-Modus");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Ueberprueft , ob man sich bereits im Client - Modus befindet und
 		 * wechselt anderenfalls dorthin . Falls man sich vorher im Server -
 		 * Modus befunden hat , wird der Server - Thread beendet . Der Client -
 		 * Modus wird als neuer Thread gestartet
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (clientThread == null) {
 				twoPlayer = true;
 				lan = true;
 				hotSeat = false;
 
 				if (serverThread != null) {
 					serverThread.interrupt();
 
 					try {
 						if (serverThread.clientSocket != null) {
 							serverThread.clientSocket.close();
 						}
 
 						serverThread.serverSocket.close();
 					}
 
 					catch (IOException e1) {
 						e1.printStackTrace();
 					}
 
 					serverThread = null;
 					System.out.println("Server beendet"); 	// Test
 					System.out.println();					// Test
 				}
 
 				System.out.println("Client-Modus aktiviert"); 	// Test
 				System.out.println(); 							// Test
 
 				clientThread = new Client();
 				clientThread.start();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Singleplayer" , wechselt zum
 	 * Singleplayer - Modus
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Config extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Singleplayer - Buttons
 		 */
 		public Action_Config() {
 			putValue(NAME, "konfigurieren");
 			putValue(SHORT_DESCRIPTION, "Einstellungen konfigurieren");
 		}
 
 		// actionPerformed-Methode:
 		/**
 	
 		 */
 		public void actionPerformed(ActionEvent e) {
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Level 1" , wechselt zu Level 1
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Level_1 extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Level1 - Buttons
 		 */
 		public Action_Level_1() {
 			putValue(NAME, "1");
 			putValue(SHORT_DESCRIPTION, "Wechsel zu Level 1");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Wechselt zum 1. Level und startet das Spiel neu
 		 */
 		public void actionPerformed(ActionEvent e) {
 
 			if (clientThread != null) {
 				clientThread.out
 						.println("Spieler 2 moechte zu Level 1 wechseln. Soll zu Level 1 gewechselt werden?");
 				createAndShowGui(
 						"Spieler 1 wurde eine Anfrage zum Wechsel zu Level 1 geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 1, "", "level");
 			}
 
 			else if (serverThread != null) {
 				serverThread.out
 						.println("Spieler 1 moechte zu Level 1 wechseln. Soll zu Level 1 gewechselt werden?");
 				createAndShowGui(
 						"Spieler 2 wurde eine Anfrage zum Wechsel zu Level 1 geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 1, "", "level");
 			}
 
 			else {
 				MapLoader.set_level(1);
 				spiel_neustarten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Level 2" , wechselt zu Level 2
 	 * 
 	 * @author Kolja Salewski
 	 */
 	private class Action_Level_2 extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des Level2 - Buttons
 		 */
 		public Action_Level_2() {
 			putValue(NAME, "2");
 			putValue(SHORT_DESCRIPTION, "Wechsel zu Level 2");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Wechselt zum 2. Level und startet das Spiel neu
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (clientThread != null) {
 				clientThread.out
 						.println("Spieler 2 moechte zu Level 2 wechseln. Soll zu Level 2 gewechselt werden?");
 				createAndShowGui(
 						"Spieler 1 wurde eine Anfrage zum Wechsel zu Level 2 geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 2, "", "level");
 			}
 
 			else if (serverThread != null) {
 				serverThread.out
 						.println("Spieler 1 moechte zu Level 2 wechseln. Soll zu Level 2 gewechselt werden?");
 				createAndShowGui(
 						"Spieler 2 wurde eine Anfrage zum Wechsel zu Level 2 geschickt. Warte ",
 						" auf Antwort...", 60, 600, 100, 2, "", "level");
 			}
 
 			else {
 				MapLoader.set_level(2);
 				spiel_neustarten();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "MapEditor" , startet den Map -
 	 * Editor
 	 * 
 	 * @author Tobias Korfmacher
 	 */
 	private class Action_Map_Editor_start extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des MapEdior - Buttons
 		 */
 		public Action_Map_Editor_start() {
 			putValue(NAME, "Starten");
 			putValue(SHORT_DESCRIPTION, "Starten des Map-Editors");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Startet den Map - Editor
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (lan == true) {
 				int antwort = JOptionPane.showConfirmDialog(null,
 						"Diese Aktion beendet den LAN-Modus. Fortfahren?",
 						"LAN-Modus", JOptionPane.YES_NO_OPTION);
 				switch (antwort) {
 				case 0:
 					lan_modus_beenden();
 					mapping = new MapEditor();
 					editorlaeuft = true;
 					break;
 				case 1:
 					break;
 				}
 
 			}
 
 			else {
 				mapping = new MapEditor();
 				editorlaeuft = true;
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "MapEditor" , fortsetzen den Map -
 	 * Editor
 	 * 
 	 * @author Tobias Korfmacher
 	 */
 	private class Action_Map_Editor_contin extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		// Konstruktor:
 		/**
 		 * Setzt den Namen und die Kurzbeschreibung des MapEdior - Buttons
 		 */
 		public Action_Map_Editor_contin() {
 			putValue(NAME, "Fortsetzen");
 			putValue(SHORT_DESCRIPTION, "Fortsetzen des Map-Editors");
 		}
 
 		// actionPerformed-Methode:
 		/**
 		 * Setzt den Map - Editor fort falls er schonmal gelaufen ist
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (editorlaeuft) {
 
 			} else {
 				if (lan == true) {
 					int antwort = JOptionPane.showConfirmDialog(null,
 							"Diese Aktion beendet den LAN-Modus. Fortfahren?",
 							"LAN-Modus", JOptionPane.YES_NO_OPTION);
 					switch (antwort) {
 					case 0:
 						lan_modus_beenden();
 						mapping = new MapEditor();
 						editorlaeuft = true;
 						break;
 					case 1:
 						break;
 					}
 
 				}
 
 				else {
 					mapping = new MapEditor();
 					editorlaeuft = true;
 				}
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Multiplayer - Bot" , aktiviert den
 	 * Bot als 2. Spieler
 	 * 
 	 * @author Sebastian Dittmann
 	 * 
 	 */
 	public class Action_MultiplayerBot extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_MultiplayerBot() {
 			putValue(NAME, "Bot");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Bot-Modus");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (bot == false) {
 				bot = true;
 				System.out.println("Bot aktiviert"); //Test
 				System.out.println();
 
 				spiel_neustarten();
 			}
 		}
 	}
 
 	/**
 	 * Klassen für den Button "Schwierigkeit" erstellen . ( zur änderung der
 	 * Schwierigkeit )
 	 * 
 	 * @author Andrej Morlang
 	 */
 	private class Action_noob extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_noob() { // zuweisung für von Action_noob aus der Zeile
 								// 110 & 255
 			putValue(NAME, "Anfänger");
 			putValue(SHORT_DESCRIPTION, "Spiel ohne Zeitlimit");
 		}
 
 		public void actionPerformed(ActionEvent e) { 	// wenn es ausgeführt wird
 														// ..
 			schwierigkeitsgrad_aendern("Anfänger");
 		}
 
 	}
 
 	private class Action_Leicht extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Leicht() {
 			putValue(NAME, "Leicht");
 			putValue(SHORT_DESCRIPTION, "Für Spieler, die Zeit brauchen");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			schwierigkeitsgrad_aendern("Leicht");
 		}
 
 	}
 
 	private class Action_Mittel extends AbstractAction { 	// siehe Erleuterungen
 															// von oben (..
 		private static final long serialVersionUID = 1L; 	// ..class
 															// Action_Leicht und
 															// ..
 															// ..class
 															// Action_noob)
 
 		public Action_Mittel() {
 			putValue(NAME, "Mittel");
 			putValue(SHORT_DESCRIPTION, "Für erfahrene Spieler");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			schwierigkeitsgrad_aendern("Mittel");
 		}
 
 	}
 
 	private class Action_Schwer extends AbstractAction { // siehe oben
 		private static final long serialVersionUID = 1L;
 
 		public Action_Schwer() {
 			putValue(NAME, "Schwer");
 			putValue(SHORT_DESCRIPTION, "Für blitzschnelle Spieler");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			schwierigkeitsgrad_aendern("Schwer");
 		}
 
 	}
 
 	/* setter & getter: */
 
 	// get_newPos-Methode:
 	/**
 	 * Gibt Array mit neuer Spielfigurenposition zurueck
 	 * 
 	 * @return a
 	 */
 	public static int[] get_newPos() {
 		return a;
 	}
 
 	// get_game-Methode:
 	/**
 	 * Gibt das grafische Spielfeld zurueck
 	 * 
 	 * @return game
 	 */
 	public static Map get_game() {
 		return game;
 	}
 
 	// set_game-Methode:
 	/**
 	 * Setzt das grafische Spielfeld
 	 * 
 	 * @param game
 	 */
 	public void set_game(Map game) {
 		Menue.game = game;
 	}
 
 	// get_hulk-Methode:
 	/**
 	 * Gibt jeweiliges Spielerobjekt zurueck
 	 * 
 	 * @param a
 	 * @return hulk1 / hulk2
 	 */
 	public static Hulk get_hulk(int a) {
 		if (a == 1)
 			return hulk1;
 		if (a == 2)
 			return hulk2;
 		else
 			return null;
 	}
 
 	// set_hulk1-Methode:
 	/**
 	 * Setzt das 1. Spielerobjekt
 	 * 
 	 * @param hulk
 	 */
 	public void set_hulk1(Hulk hulk) {
 		Menue.hulk1 = hulk;
 	}
 
 	// setHulk2-Methode:
 	/**
 	 * Setzt das 1. Spielerobjekt
 	 * 
 	 * @param hulk2
 	 */
 	public static void setHulk2(Hulk hulk2) {
 		Menue.hulk2 = hulk2;
 	}
 
 	// get_map-Methode:
 	/**
 	 * Gibt Positionen der Icons im Spielfeld zurueck
 	 * 
 	 * @return map
 	 */
 	public static int[][] get_map() {
 		return map;
 	}
 
 	//get_bot-Methode:
 
 	/**
 	 * @return bot1-Objekt fuer eventuelle Erweiterung auf mehrere Bots
 	 */
 
 	public static Bot get_bot(int bot) {
 		return bot1;
 	}
 
 	// getMultiplayer-Methode:
 	/**
 	 * Gibt zureck , ob Mehrspieler - Modus aktiv ist
 	 * 
 	 * @return twoPlayer
 	 */
 	public static boolean getMultiplayer() {
 		return twoPlayer;
 	}
 
 	// set_clientThread-Methode:
 	/**
 	 * Setzt das clientThread - Objekt
 	 * 
 	 * @param clientThread
 	 */
 	public static void set_clientThread(Client clientThread) {
 		Menue.clientThread = clientThread;
 	}
 
 	// set_serverThread-Methode:
 	/**
 	 * Setzt das serverThread - Objekt
 	 * 
 	 * @param serverThread
 	 */
 	public static void set_serverThread(Server serverThread) {
 		Menue.serverThread = serverThread;
 	}
 
 	/**
 	 * 
 	 * @return . wav - Datei fuer Soundausgabe
 	 */
 	// public static Sound get_EXP(){
 	// return exp;
 	// }
 
 	/**
 	 * 
 	 * @return Boolean-Wert, ob Bot aktiviert ist
 	 */
 	public static boolean getBot() {
 		return bot;
 	}
 
 	public static void set_mapLoaded(boolean Wert) {
 		mapLoaded = Wert;
 	}
 
 	// main-Methode:
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			// run-Methode:
 			/**
 			 * Erstellt das Programmfenster und stellt es dar
 			 */
 			public void run() {
 				try {
 					new Menue();
 					Menue.frame.setVisible(true);
 				}
 
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		});
 
 	}
 
 	public static boolean get_mapLoaded() {
 		return mapLoaded;
 	}
 
 }

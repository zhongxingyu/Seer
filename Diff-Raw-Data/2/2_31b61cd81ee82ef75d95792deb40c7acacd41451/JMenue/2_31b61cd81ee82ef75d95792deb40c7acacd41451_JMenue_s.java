 package Bomberman;
 
 import java.awt.FlowLayout;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 
 public class JMenue extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	// Spielfeldgroesse:
 	public static int mapWidth = 19;
 	public static int mapHeight = 19;
 	// Kachelgroesse (Standard: 32x32 Pixel):
 	public static final int tileWidth = 32;
 	public static final int tileHeight = 32;
 
 	public static JFeld feld;
 	public static JJFrame frame;
 	static Figur bm1, bm2;
 	public static JFrame choice = new JLevelauswahl();
 
 	private static final long serialVersionUID = 1L;
 
 	public JMenue(String t) {
 
 		setTitle(t);
 		LayoutManager manager = new FlowLayout();
 		setLayout(manager);
 
 		JButton spielstarten1P = new JButton("Zufallskarte (1P)");
 		spielstarten1P.setActionCommand("go1");
 		spielstarten1P.addActionListener(this);
 		add(spielstarten1P);
 		JButton spielstarten2P = new JButton("Multiplayer (2P)");
 		spielstarten2P.setActionCommand("go2");
 		spielstarten2P.addActionListener(this);
 		add(spielstarten2P);
 		JButton random = new JButton("Levelauswahl");
 		random.setActionCommand("go3");
 		random.addActionListener(this);
 		add(random);
 		JButton spielbeenden = new JButton("Spiel beenden");
 		spielbeenden.setActionCommand("exit");
 		spielbeenden.addActionListener(this);
 		add(spielbeenden);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 
 		// Spiel beenden
 		if (arg0.getActionCommand().equals("exit")) {
 			System.exit(0);
 		}
 		// Spiel starten 1P
 		if (arg0.getActionCommand().equals("go1")) {
 
 			// Spielfeld erstellen
 			feld = new JFeld(mapWidth, mapHeight, tileWidth, tileHeight,
 					"random", false);
 			// Frame definieren
 			frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 					feld);
 			frame.setTitle("Bomberman - Random (1P)");
 			bm1 = new Figur(1, 1);
 			// Steuerung hinzufgen
 			new Control(frame, bm1, feld, 0);
 			// Menue ausblenden beim Spielstart
 			setVisible(false);
 
 		}
 		// Spiel starten 2P
 		if (arg0.getActionCommand().equals("go2")) {
 
 			feld = new JFeld(mapWidth, mapHeight, tileWidth, tileHeight,
 					"random", true);
 			frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 					feld);
 			frame.setTitle("Bomberman - Random (2P)");
 			bm1 = new Figur(1, 1);
 			bm2 = new Figur(mapHeight - 2, mapWidth - 2);
 			new Control(frame, bm1, feld, 0);
 			new Control(frame, bm2, feld, 1);
 			setVisible(false);
 
 		}
 		if (arg0.getActionCommand().equals("go3")) {
 
 			// Spielfeld auslesen
 			setVisible(false);
 			// Zeigt Auswahl an
 			choice.setVisible(true);
 			// Listener der auf Auswahl reagiert
 			// Bug: Fehler beim Beenden, da durch setSelectedIndex(-1)
 			// das Item "null" ausgewählt wird. Kann von Mapreader natuerlich
 			// nicht erkannt werden. -> Muss beim Beenden ins Menue und
 			// den Listener entfernen!
 			JLevelauswahl.levellist.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent e) {
 					JComboBox selectedChoice = (JComboBox) e.getSource();
 					String level = (String) selectedChoice.getSelectedItem();
 					Mapreader create = new Mapreader(level);
 					feld = new JFeld(create.getWidth(), create.getHeight(),
 							tileWidth, tileHeight, level, false);
 					frame = new JJFrame(mapWidth, mapHeight, tileWidth,
 							tileHeight, feld);
 					frame.setTitle("Bomberman - " + level);
 					choice.dispose();
 					frame.addWindowListener(new WindowAdapter() {
 						public void windowClosing(WindowEvent e) {
 							frame.dispose();
 							JLevelauswahl.levellist.setSelectedIndex(-1);
 							setVisible(true);
 						}
 					});
 				}
 			});
 
 			// Menue ausblenden beim Spielstart
 
 		}
 
 	}
 }

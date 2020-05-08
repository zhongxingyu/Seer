 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

 import java.io.File;
 import java.util.Enumeration;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 // Autor T. K.
 public class MapEditor extends JPanel {
 	private static final long 	serialVersionUID 	= 1L;
 	final static int 			n 					= MapLoader.get_n();
 	private static int[][] 		map 				= new int[n][n];
 	protected static boolean 	saved 				= false;
 	Icon 						pic;
 	static int 					iconSatz;
 	
 	private static boolean 		abfrageHulk1 		= false,
 								abfrageHulk2 		= false,
 								abfrageAusgang 		= false,
 								abbruch 			= false,
 								exist 				= false;
 	
 	private static File 		f;
 	
 	private static String 		eingabe,
 								levelnummer;
 	
 	private static JButton 		feld[][] 			= new JButton[n][n];
 	
 	private static int 			oldLevel 			= MapLoader.get_level(),
 								frameWi,
 								frameHe,
 								power;
 
 	/* Konstruktor: */
 	public MapEditor() {
 		edit();
 	}
 
 	/* METHODEN: */
 
 	// edit-Methode:
 	public int edit() {
 		setVisible(true);
 		int anzahlIcons = 9;
 		boolean richtigeAbfrage = false;
 		frameWi=Menue.get_width();
 		frameHe=Menue.get_height();
 		
 		do {
 			eingabe = 	JOptionPane.showInputDialog(null,
 						"Bitte geben Sie die Level-Nummer ein:", "Levelnummer", // Levelnummerdialog
 						JOptionPane.OK_CANCEL_OPTION);
 
 			if (eingabe == null) {
 				System.out.println("Boo"); // Test
 				
 				Menue.get_game().setVisible(true);
 				MapLoader.set_level(oldLevel);
 				richtigeAbfrage = true;
 				setVisible(false);		
 				Menue.get_game().bilder_skalieren(MapLoader.get_iconSatz());
 				Menue.get_game().init(); 				// Spielfeld zeichnen
 				Menue.get_game().setFocusable(true); 	// Spielfeld fokussierbar machen
 				Menue.get_game().requestFocus(); 		// Fokus auf Spielfeld setzen
 				Menue.setFrame(frameWi,frameHe);
 				Menue.spiel_neustarten();
 				abbruch = true;
 				
 				return 0;
 			}
 			
 			else if (eingabe.equals("")) {
 				richtigeAbfrage = false;				
 			}
 			
 			else {
 				MapLoader.set_level(Integer.parseInt(eingabe));
 				richtigeAbfrage = true;
 			}
 			
 		} while (richtigeAbfrage != true);
 
 		// datei existiert?
 		String dateiName = "src/Maps/Level-";
 		dateiName = dateiName + eingabe + ".txt";
 
 		f = new File(dateiName);
 		
 		if (f.exists() == true) {
 			iconSatz = MapLoader.get_iconSatzLevel(dateiName);
 			MapLoader.set_iconSatz(iconSatz);
 
 			map = MapLoader.laden(MapLoader.get_level());
 			exist = true;
 		}
 
 		else {
 			int iconAbfrage = JOptionPane.showConfirmDialog(null,
 					"Mchten Sie den ersten Icon-Satz nutzen?", "Icon-Satz?",
 					JOptionPane.YES_NO_OPTION);
 			
 			switch (iconAbfrage) {
 				case 0:
 					MapLoader.set_iconSatz(1);
 					iconSatz = 1;
 					break;
 					
 				case 1:
 					MapLoader.set_iconSatz(2);
 					iconSatz = 2;
 					break;
 			}
 
 			for (int i = 0; i < n; i++) { // Map mit Mauern umranden
 
 				map[i][0] = 4;
 				map[0][i] = 4;
 				map[n - 1][i] = 4;
 				map[i][n - 1] = 4;
 
 			}
 			
 			for (int i = 1; i < n - 1; i++) {
 				for (int j = 1; j < n - 1; j++) { // Rest mit Weg fuellen	
 					map[i][j] = 2;
 				}
 				
 			}
 			
 			MapLoader.level_speichern(map, "Level-" + eingabe);
 		}
 		
 		Menue.get_game().bilder_skalieren(iconSatz);
 		
 //		frame = new JFrame();
 //		frame.setBounds(100, 100, 450, 300);
 //		frame.setTitle("Map-Editor"); // Fenstertitel setzen
 //		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 
 		levelnummer = "Level-" + eingabe;
 		
 //		JLabel levelname = new JLabel(levelnummer, JLabel.CENTER);
 //		add(levelname, BorderLayout.NORTH);
 
 		JButton save_button = new JButton("Level speichern");
 		
 		ActionListener save = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (exist) {
 					int dateiAbfrage = 	JOptionPane.showConfirmDialog(null,
 										"Die Datei existiert bereits.\n"
 										+ "Mchten Sie die Datei berschreiben?",
 										"berschreiben?",
 										JOptionPane.YES_NO_OPTION);
 
 					if (dateiAbfrage == 0) {
 
 					}
 
 					else {
 						String ersatzNummer = 	JOptionPane.showInputDialog(null,
 												"Geben Sie die einen Ersatznamen ein!",
 												"ersatz", JOptionPane.PLAIN_MESSAGE);
 
 						MapLoader.level_speichern(map, "Level-" + ersatzNummer);
 						saved = true;
 					}
 
 				}
 
 				else {
 					MapLoader.level_speichern(map, levelnummer);
 					saved = true;
 				}
 
 //				Menue.spiel_neustarten();
 			}
 
 		};
 		
 		setVisible(true); // frame.setVisible(true)
 		JPanel place = new JPanel(new GridLayout(1,4));
 		
 //		frame.setResizable(false); // Fenster soll nicht skalierbar sein
 
 		JButton exit_button = new JButton("Editor beenden"); // "Editor beenden"-Button hinzufuegen
 		
 		ActionListener exit = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				if (saved) {
 					Menue.setMappingVisible(false);
 					MapLoader.set_level(oldLevel);
 					Menue.setGameVisible(true);
 					Menue.spiel_neustarten();
 				}
 
 				else if (!abbruch) {
 					System.out.println("Speichern?"); // Test
 					
 					int eingabe = 	JOptionPane.showConfirmDialog(null,
 									"Wollen Sie die neue Map speichern?",
 									"Gespeichert?",
 									JOptionPane.YES_NO_CANCEL_OPTION);
 
 					switch (eingabe) {
 						case 0:
 							MapLoader.level_speichern(map, levelnummer);
 							saved = true;
 							Menue.setMappingVisible(false);
 							MapLoader.set_level(oldLevel);
 							Menue.setGameVisible(true);
 							Menue.spiel_neustarten();
 							break;
 							
 						case 1:
 							Menue.setMappingVisible(false);
 							
 							if (!exist) {
 								f.deleteOnExit();
 								f.delete(); // funktioniert noch nicht
 							}
 							
 							MapLoader.set_level(oldLevel);
 							Menue.setGameVisible(true);
 							Menue.spiel_neustarten();
 							break;
 							
 						case 2: // abbrechen, nichts machen
 							break;
 					}
 					
 				}
 				
 			}
 			
 		};
 
 		JButton test_button = new JButton("Testen");
 		
 		ActionListener test = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				MapLoader.level_speichern(map, "Level-" + eingabe);
 				Menue.setMappingVisible(false);
 				Menue.setGameVisible(true);
 //				Menue.get_game().setVisible(true);
 //				MapLoader.set_level(Integer.parseInt(eingabe));
 //				Menue.get_game().bilder_skalieren();
 //				Menue.get_game().init(); 				// Spielfeld zeichnen
 //				Menue.get_game().setFocusable(true); 	// Spielfeld fokussierbar machen
 //				Menue.get_game().requestFocus(); 		// Fokus auf Spielfeld setzen
 //				Menue.setFrame(frameWi,frameHe);
 				Menue.spiel_neustarten();
 			}
 
 		};
 
 //		JButton freigabe_button = new JButton("Level freigeben");
 //		ActionListener freigabe = new ActionListener() {
 //			public void actionPerformed(ActionEvent e) {
 //				boolean copy = false;
 //				// level implentieren wenn es luft!
 //				// level in level ordner koppieren
 //
 //				if(copy){
 //					JOptionPane.showMessageDialog(null,
 //							"Das Level wurde erfolgreich eingebunden"); // Einbindung ins Menue fehlt noch. kolja
 //				}else{
 //					JOptionPane.showMessageDialog(null,
 //							"Es gab Probleme beim einbinden");
 //				}
 //
 //			}
 //
 //		};
 
 		// Icons
 		JRadioButton icon[] = new JRadioButton[anzahlIcons];
 		final ButtonGroup buttonGroup = new ButtonGroup();
 		icon[0] = new JRadioButton("Hulk", true);
 		icon[1] = new JRadioButton("Weg");
 		icon[2] = new JRadioButton("Block");
 		icon[3] = new JRadioButton("Mauer");
 		icon[4] = new JRadioButton("Ausgang");
 		icon[5] = new JRadioButton("Block-Ausgang");
 		icon[6] = new JRadioButton("Block/Flammen-Item");
 		icon[7] = new JRadioButton("Block/Bomben-Item");
 		icon[8] = new JRadioButton("2.Spieler");
 
 		// eventuell noch mehrere
 		JPanel radioPanel = new JPanel(new GridLayout(2, anzahlIcons / 2));
 		
 		for (int k = 0; k < anzahlIcons; k++) {
 			buttonGroup.add(icon[k]);
 			radioPanel.add(icon[k]);
 		}
 
 		add(radioPanel, BorderLayout.WEST);
 		exit_button.addActionListener(exit);
 		save_button.addActionListener(save);
 		test_button.addActionListener(test);
 //		freigabe_button.addActionListener(freigabe);
 		place.add(exit_button);
 		place.add(save_button);
 		place.add(test_button);
 //		place.add(freigabe_button);
 		add(place, BorderLayout.WEST);
 		JPanel buttonPanel = new JPanel(new GridLayout(n, n));
 
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				final int a = i;
 				final int b = j;
 				
 				ActionListener list = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						String name = getSelectedButton(buttonGroup).getText();
 						
 						if (name != null) {
 							if (name == "Hulk") {
 								if (!abfrageHulk1) {
 									power = 1;									
 									pic = getPic(1);
 									abfrageHulk1 = true;									
 								}
 								
 								else {
 									System.out.println("Hulk1 wurde schon gesetzt"); // Test
 									
 									int neu = 	JOptionPane.showConfirmDialog
 												(null,"Neue Position?",
 												"Bereits gesetzt",
 												JOptionPane.YES_NO_OPTION);
 									
 									if(neu == 0) {
 										// Alte Position des Hulks mit Weg ueberschreiben:
 										feld	[MapLoader.get_icon_x(map, 1)]
 												[MapLoader.get_icon_y(map, 1)]
 												.setIcon(getPic(2));
 										
 										map		[MapLoader.get_icon_x(map, 1)]
 												[MapLoader.get_icon_y(map, 1)]
 												= 2;
 										
 										// Neue Position des Hulks setzen:
 										power = 1;
 										pic = getPic(1);
 									}
 									
 									else {
 										power = 2;
 										pic = getPic(2);
 									}
 										
 								}
 								
 							}
 							
 							else if (name == "Weg") {
 								power = 2;
 								pic = getPic(2);
 							}
 							
 							else if (name == "Block") {
 								power = 3;
 								pic = getPic(3);
 							}
 							
 							else if (name == "Mauer") {
 								power = 4;
 								pic = getPic(4);
 							}
 							
 							else if (name == "Ausgang") {
 								if (!abfrageAusgang) {
 									power = 7;
 									pic = getPic(7);
 									abfrageAusgang = true;
 								}
 								
 								else {
 									System.out.println("Ausgang wurde schon gesetzt"); // Test
 									
 									int neu = 	JOptionPane.showConfirmDialog
 												(null,"Neue Position?",
 												"Bereits gesetzt",
 												JOptionPane.YES_NO_OPTION);
 									
 									if (neu == 0) {
 										// Alte Position des Ausgangs mit Weg ueberschreiben:
 										feld	[MapLoader.get_icon_x(map, 7)]
 												[MapLoader.get_icon_y(map, 7)]
 												.setIcon(getPic(2));
 										
 										map		[MapLoader.get_icon_x(map, 7)]
 												[MapLoader.get_icon_y(map, 7)]
 												= 2;
 										
 										// Neue Position des Ausgangs setzen:
 										power = 7;
 										pic = getPic(7);
 									}
 									
 									else {
 										power = 2;
 										pic = getPic(2);
 									}
 									
 								}
 								
 							}
 							
 							else if (name == "Block-Ausgang") {
 								if (!abfrageAusgang) {
 									power = 8;
									pic = getPic(8);
 									abfrageAusgang = true;
 								}
 								
 								else {
 									System.out.println("Ausgang wurde schon gesetzt"); // Test
 									
 									int neu = 	JOptionPane.showConfirmDialog
 												(null,"Neue Position?", "Bereits gesetzt",
 												JOptionPane.YES_NO_OPTION);
 									
 									if (neu == 0) {
 										// Alte Position des Block-Ausgangs mit Weg ueberschreiben:
 										feld	[MapLoader.get_icon_x(map, 8)]
 												[MapLoader.get_icon_y(map, 8)]
 												.setIcon(getPic(2));
 										
 										map		[MapLoader.get_icon_x(map, 8)]
 												[MapLoader.get_icon_y(map, 8)]
 												= 2;
 										
 										// Neue Position des Block-Ausgangs setzen:
 										power = 8;
										pic = getPic(8);
 									}
 									
 									else {
 										power = 2;
 										pic = getPic(2);
 									}
 										
 								}
 								
 							}
 							
 							else if (name == "Block/Flammen-Item") {
 								power = 9;
 								pic = getPic(9);
 							}
 							
 							else if (name == "Block/Bomben-Item") {
 								power = 12;
 								pic = getPic(12);
 							}
 							
 							else if (name == "2.Spieler") {
 								if (!abfrageHulk2) {
 									power = 10;
 									pic = getPic(10);
 									abfrageHulk2 = true;
 								}
 								
 								else {
 									System.out.println("Hulk2 wurde schon gesetzt"); // Test
 									
 									int neu = 	JOptionPane.showConfirmDialog
 												(null,"Neue Position?",
 												"Bereits gesetzt",
 												JOptionPane.YES_NO_OPTION);
 									
 									if (neu == 0) {
 										// Alte Position des 2. Hulks mit Weg ueberschreiben:
 										feld	[MapLoader.get_icon_x(map, 10)]
 												[MapLoader.get_icon_y(map, 10)]
 												.setIcon(getPic(2));
 										map		[MapLoader.get_icon_x(map, 10)]
 												[MapLoader.get_icon_y(map, 10)]
 												= 2;
 										
 										// Neue Position des 2. Hulks setzen:
 										power = 10;
 										pic = getPic(10);
 									}
 									
 									else {
 										power = 2;
 										pic = getPic(2);
 									}
 										
 								}
 								
 							}
 							
 							map[a][b] = power;
 							feld[a][b].setIcon(pic); // ...Grafik auf Button
 							saved = false;
 						}
 							
 					}
 						
 				};
 
 				feld[i][j] = new JButton();
 				pic = getPic(map[i][j]);
 				feld[i][j].setIcon(pic);
 				feld[i][j].setPreferredSize(new Dimension(40,40)); // Button-Gre
				feld[i][j].addActionListener(list);
 				buttonPanel.add(feld[i][j]);
			}
 			
 			add(buttonPanel);
 		}
 		
 		return 1;		
 	}
 
 	/* setter & getter: */
 	// getSelectedButton-Methode:
 	public static JRadioButton getSelectedButton(ButtonGroup group) {
 		Enumeration<AbstractButton> e = group.getElements();
 		
 		while (e.hasMoreElements()) {
 			AbstractButton b = e.nextElement();
 			
 			if (b.isSelected())
 				return (JRadioButton) b;
 		}
 		
 		return null;
 	}
 
 	public static Icon getPic(int i) {
 		Icon pic = null;
 		
 		switch (i) {
 			case 0: // Block/Bomben-Item 	
 				pic = Menue.get_game().bomben_item_icon;
 				break;
 				
 			case 1: // Hulk
 				pic = Menue.get_game().hulk_icon;
 				break;
 	
 			case 2: // Weg
 				pic = Menue.get_game().weg_icon;
 				break;
 	
 			case 3: // Block
 				pic = Menue.get_game().block_icon;
 				break;
 	
 			case 4: // Mauer (nicht zerstoerbar)
 				pic = Menue.get_game().mauer_icon;
 				break;
 	
 			case 7: // Ausgang
 				pic = Menue.get_game().exit_icon;
 				break;
 	
 			case 8: // Block/Ausgang	
 				pic = Menue.get_game().exit_icon;
 				break;
 	
 			case 9: // Block/Flammen-Item 	
 				pic = Menue.get_game().flammen_item_icon;
 				break;
 	
 			case 10: // 2. Spieler
 				pic = Menue.get_game().hulk2_icon;
 				break;
 				
 			case 12: // Block/Bomben-Item 	
 				pic = Menue.get_game().bomben_item_icon;
 				break;
 				
 			case 15: // Block/Flammen-Item 	
 				pic = Menue.get_game().flammen_item_icon;
 				break;
 		}
 		
 		return pic;
 	}
 	
 	public static boolean get_saved() {
 		return saved;
 	}
 	
 	public static void set_saved(boolean fred) {
 		saved = fred;
 	}
 	
 	public static int[][] get_map() {
 		return map;
 	}
 	
 	public static String get_levelnummer() {
 		return levelnummer;
 	}
 	
 	public static boolean get_exist() {
 		return exist;
 	}
 	
 	public static File get_f() {
 		return f;
 	}	
 	
 	public static boolean get_abbruch() {
 		return abbruch;
 	}
 	
 	public void set_abbruch(boolean wert) {
 		abbruch = wert;
 	}
 
 }

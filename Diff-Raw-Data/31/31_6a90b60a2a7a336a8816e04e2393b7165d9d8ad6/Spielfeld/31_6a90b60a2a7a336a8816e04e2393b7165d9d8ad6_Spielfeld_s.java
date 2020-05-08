 package Spielfeld;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Random;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import Gui.Main;
 import Gui.OeffnenDialogClass;
 import Objects.Bomb;
 import Objects.Spieler;
 
 /**
  * Klasse zur Erstellung des Basis-Spielfelds mit den zugewiesenen Grafiken
  * 
  * @author Gruppe37
  * @version 0.9
  * @param Main
  *            parent
  */
 public class Spielfeld extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Variablen der verschiedenen Stati eines Spielfeldblocks und der Grafiken
 	 * bei Spielende
 	 */
 	private ImageIcon solidBlock;
 	private ImageIcon brkbleBlock;
 	private ImageIcon grndBlock;
 	private ImageIcon player;
 	private ImageIcon player2;
 	private ImageIcon bombe;
 	private ImageIcon exp_h;
 	private ImageIcon exp_v;
 	private ImageIcon exp_m;
 	private ImageIcon playeronbomb;
 	private ImageIcon player2onbomb;
 	private ImageIcon portal;
 	private ImageIcon player1wins;
 	private ImageIcon player2wins;
 	private ImageIcon bothplayerdead;
 
 	/** horizontale Feldgre */
 	private int Feldgroesse_x = 100;
 
 	/** vertikale Feldgre */
 	private int Feldgroesse_y = 100;
 
 	/** Dichte der zerstrbaren Blcke */
 	private double Blockdichte = 0.7;
 
 	/** Array in dem das Feld erstellt wird */
 	private final JLabel fblock[][] = new JLabel[Feldgroesse_x][Feldgroesse_y];
 
 	/** Definition der einzelnen Spielfeldelemente */
 	private final int blockStatus[][] = new int[Feldgroesse_x][Feldgroesse_y];
 
 	/** horizontale Koordinate des Spielfelds */
 	private int m = 0;
 	/** vertikale Koordinate des Spielfelds */
 	private int n = 0;
 	/** horizontale Koordinate von Spieler 1 */
 	private final int x[] = { 1, 1 };
 	/** vertikale Koordinate von Spieler 1 */
 	private final int y[] = { 1, 1 };
 	private int bombsLeftP1 = 2;
 	private int bombsLeftP2 = 2;
 	/** horizontale Koordinate der Bombe von Spieler 1 */
 	private final int a[][] = new int[2][3];
 	/** vertikale Koo rdinate der Bombe von Spieler 1 */
 	private final int b[][] = new int[2][3];
 
 	// Radien der beiden bomben
 	/** Radius der Bombe von Spieler 1 */
 	private final int radius1 = 2;
 	/** Radius der Bombe von Spieler 2 */
 	private final int radius2 = 2;
 	/** Radius von Spieler 1 und 2 fr die Explosion */
 	private final int radius[] = new int[2];
 
 	/** freies Bodenfeld */
 	private final int ground = 13;
 	/** unzerstrbarer Block */
 	private final int solid = 1;
 	/** zerstrbarer Block */
 	private final int breakblock = 2;
 	/** Bombe auf einem freien Feld */
 	private final int bombesetzen = 3;
 	/** Spieler 1 auf seiner Bombe */
 	private final int spieler_bombe[] = { 4, 12 };
 	/** Mittelpunkt der Explosion */
 	private final int explosion_mitte = 5;
 	/** horizontale Komponente der Explosion */
 	private final int explosion_horizontal = 6;
 	/** vertikale Komponente der Explosion */
 	private final int explosion_vertikal = 7;
 	/** Spielfigur von Spieler 1 */
 	private final int spieler[] = { 8, 11 };
 	/** Ausgang unter einem zerstrbaren Block */
 	private final int versteckterausgang = 9;
 	/** offengelegter Ausgang */
 	private final int ausgang = 10;
 
 	/*
 	 * Fhigkeit Bomben zu legen wird ermglicht
 	 */
 	/** Fhigkeit des 1. Spielers eine Bombe zu legen */
 	public static boolean nextbomb[] = { true, true };
 
 	/**
 	 * Festlegung, dass die Spielfiguren am Leben sind
 	 */
 
 	boolean playeralive[] = { true, true };
 
 	/** Gibt an, ob ein Portal vorhanden ist */
 	private static boolean Portal_vorhanden = false;
 
 	private final Main window;
 
 	/** Spielfeld wird im Fenster Main angezeigt */
 	public Spielfeld(Main parent) {
 
 		loadContentImages();
 
 		// Fenstereinstellungen
 		window = parent;
 	}
 
 	/****************************************
 	 * Laden der einzelnen Icons der Blcke *
 	 ****************************************/
 	private void loadContentImages() {
 		solidBlock = new ImageIcon("Images/HardBlock.png");
 		brkbleBlock = new ImageIcon("Images/breakstone.jpg");
 		grndBlock = new ImageIcon("Images/ground.jpg");
 		player = new ImageIcon("Images/Player.png");
 		player2 = new ImageIcon("Images/Player2.png");
 		bombe = new ImageIcon("Images/bomb.jpg");
 		exp_h = new ImageIcon("Images/exp_h.jpg");
 		exp_v = new ImageIcon("Images/exp_v.jpg");
 		exp_m = new ImageIcon("Images/exp_m.jpg");
 		playeronbomb = new ImageIcon("Images/Playeronbomb.png");
 		player2onbomb = new ImageIcon("Images/Player2onbomb.png");
 		portal = new ImageIcon("Images/portal.gif");
 		player1wins = new ImageIcon("Images/Player1Wins.jpg");
 		player2wins = new ImageIcon("Images/Player2Wins.jpg");
 		bothplayerdead = new ImageIcon("Images/BothPlayerDead.jpg");
 	}
 
 	/*******************************
 	 * Initialisieren der XML-Datei*
 	 ******************************/
 
 	public static void XMLInit() throws SAXException, IOException,
 			ParserConfigurationException, NullPointerException,
 			FileNotFoundException {
 		OeffnenDialogClass oeffne = new OeffnenDialogClass(null);
 		File field = new File(oeffne.getLevelName());
 
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder documentBuilder = documentBuilderFactory
 				.newDocumentBuilder();
 		Document dok = documentBuilder.parse(field);
 		XMLReader.handleChannelTag(dok);
 	}
 
 	public void XMLFeld() {
 		try {
 			XMLInit();
 
 			Feldgroesse_x = XMLReader.breite_max;
 			Feldgroesse_y = XMLReader.hoehe_max;
 			window.setSize(((Feldgroesse_x * 30) + 10),
 					((Feldgroesse_y * 30) + 50));
 			window.gamepanel.setBounds(0, 0, Feldgroesse_x * 30,
 					Feldgroesse_y * 30);
 
 			for (int breite = 0; breite < Feldgroesse_x; breite++) {
 				for (int hoehe = 0; hoehe < Feldgroesse_y; hoehe++) {
 					if (XMLReader.xmlStatus[breite][hoehe] == 0) {
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"Spielfeld entspricht nicht dem vorgegeben Format. Bitte Datei berarbeiten oder anderes Spielfeld auswhlen");
 						XMLInit();
 					}
 					if (XMLReader.xmlStatus[breite][hoehe] == XMLReader.solid) {
 						blockStatus[breite][hoehe] = solid;
 					} else if (XMLReader.xmlStatus[breite][hoehe] == XMLReader.breakblock) {
 						blockStatus[breite][hoehe] = breakblock;
 					} else if (XMLReader.xmlStatus[breite][hoehe] == XMLReader.ground) {
 						blockStatus[breite][hoehe] = ground;
 					} else if (XMLReader.xmlStatus[breite][hoehe] == XMLReader.spieler) {
 						blockStatus[breite][hoehe] = spieler[0];
 						x[0] = breite;
 						y[0] = hoehe;
 					} else if (XMLReader.xmlStatus[breite][hoehe] == XMLReader.spieler2) {
 						blockStatus[breite][hoehe] = spieler[1];
 						x[1] = breite;
 						y[1] = hoehe;
 					}
 
 				}
 
 			}
 
 			/*******************************************************
 			 * Sicherstellung, dass die Startpositionen frei sind. *
 			 *******************************************************/
 			window.setSize(((Feldgroesse_x * 30) + 10),
 					((Feldgroesse_y * 30) + 50));
 			window.gamepanel.setBounds(0, 0, Feldgroesse_x * 30,
 					Feldgroesse_y * 30);
 
 			// Spieler setzen und Positions-Reset bei Neustart
 			zeichnen();
 			XMLReader.Reset();
 			playeralive[0] = true;
 			playeralive[1] = true;
 			radius[0] = radius1;
 			radius[1] = radius2;
 
 		} catch (SAXException e) {
 
 		} catch (IOException e) {
 
 		} catch (ParserConfigurationException e) {
 
 		} catch (NullPointerException e) {
 
 		}
 	}
 
 	/*
 	 * Standardspielfeld mit variabler Groesse und zuflligem aufbau aus
 	 * zerstrbare und freien Blcken in Array schreiben
 	 */
 
 	public void standardfeld() {
 
 		/** horizontale Feldgre */
 		Feldgroesse_x = Gui.Einstellungen.breite;
 		/** vertikale Feldgre */
 		Feldgroesse_y = Gui.Einstellungen.hoehe;
 
 		/** Dichte der zerstrbaren Blcke */
 		Blockdichte = (Gui.Einstellungen.dichte * 0.01);
 
 		window.setSize(((Feldgroesse_x * 30) + 10), ((Feldgroesse_y * 30) + 50));
 		window.gamepanel
 				.setBounds(0, 0, Feldgroesse_x * 30, Feldgroesse_y * 30);
 		for (n = 0; n < Feldgroesse_y; n++) {
 			for (m = 0; m < Feldgroesse_x; m++) {
 				if (m % 2 != 1 && n % 2 != 1 || m == 0 || n == 0
 						|| n == Feldgroesse_y - 1 || m == Feldgroesse_x - 1) {
 					blockStatus[m][n] = solid;
 				}
 
 				else {
 					if ((1 - Math.random()) >= Blockdichte) {
 						blockStatus[m][n] = ground;
 					} else {
 						blockStatus[m][n] = breakblock;
 					}
 				}
 
 			}
 		}
 
 		/*******************************************************
 		 * Sicherstellung, dass die Startpositionen frei sind. *
 		 *******************************************************/
 
 		// Oben links
 		blockStatus[1][1] = ground;
 		blockStatus[1][2] = ground;
 		blockStatus[2][1] = ground;
 		// Oben rechts
 		blockStatus[Feldgroesse_x - 2][1] = ground;
 		blockStatus[Feldgroesse_x - 2][2] = ground;
 		blockStatus[Feldgroesse_x - 3][1] = ground;
 		// Unten links
 		blockStatus[1][Feldgroesse_y - 2] = ground;
 		blockStatus[1][Feldgroesse_y - 3] = ground;
 		blockStatus[2][Feldgroesse_y - 2] = ground;
 		// Unten rechts
 		blockStatus[Feldgroesse_x - 2][Feldgroesse_y - 2] = ground;
 		blockStatus[Feldgroesse_x - 2][Feldgroesse_y - 3] = ground;
 		blockStatus[Feldgroesse_x - 3][Feldgroesse_y - 2] = ground;
 
 		// Spieler setzen und Positions-Reset bei Neustart
 		/** horizontale Position von Spieler 1 */
 		x[0] = 1;
 		/** vertikale Position von Spieler 1 */
 		y[0] = 1;
 		/** horizontale Position von Spieler 2 */
 		x[1] = (Feldgroesse_x - 2);
 		/** vertikale Position von Spieler 2 */
 		y[1] = (Feldgroesse_y - 2);
 		blockStatus[x[0]][y[0]] = spieler[0];
 		blockStatus[x[1]][y[1]] = spieler[1];
 		playeralive[0] = true;
 		playeralive[1] = true;
 		radius[0] = radius1;
 		radius[1] = radius2;
 		Portal_vorhanden = false;
 	}
 
 	/***********************************************************************
 	 * Auslesen der Blockstati und zuordnung der passenden Icons zu diesen *
 	 ***********************************************************************/
 	public void zeichnen() {
 		window.gamepanel.removeAll();
 		for (m = 0; m < Feldgroesse_x; m++) {
 			for (n = 0; n < Feldgroesse_y; n++) {
 				if (blockStatus[m][n] == ground) {
 					fblock[m][n] = new JLabel(grndBlock);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 
 				else if (blockStatus[m][n] == solid) {
 					fblock[m][n] = new JLabel(solidBlock);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 
 				else if (blockStatus[m][n] == breakblock) {
 					fblock[m][n] = new JLabel(brkbleBlock);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 				/*
 				 * Bombe und Explosion
 				 */
 				else if (blockStatus[m][n] == bombesetzen) {
 					fblock[m][n] = new JLabel(bombe);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == spieler_bombe[0]) {
 					fblock[m][n] = new JLabel(playeronbomb);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == spieler_bombe[1]) {
 					fblock[m][n] = new JLabel(player2onbomb);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == explosion_mitte) {
 					fblock[m][n] = new JLabel(exp_m);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == explosion_horizontal) {
 					fblock[m][n] = new JLabel(exp_h);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == explosion_vertikal) {
 					fblock[m][n] = new JLabel(exp_v);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 
 				/*
 				 * Spieler
 				 */
 				else if (blockStatus[m][n] == spieler[0]) {
 					fblock[m][n] = new JLabel(player);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 
 				/*
 				 * Spieler2
 				 */
 				else if (blockStatus[m][n] == spieler[1]) {
 					fblock[m][n] = new JLabel(player2);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 
 				/*
 				 * Ausgang
 				 */
 				else if (blockStatus[m][n] == ausgang) {
 					fblock[m][n] = new JLabel(portal);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				} else if (blockStatus[m][n] == versteckterausgang) {
 					fblock[m][n] = new JLabel(brkbleBlock);
 					window.gamepanel.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 				}
 			}
 		}
 	}
 
 	/*****************
 	 * Bombe *
 	 *****************/
 	public void explozeichnen(int playerNR, int bombsLeft) {
 
 		int k = a[playerNR][bombsLeft];
 		int l = b[playerNR][bombsLeft];
 		System.out.println(k + "+" + l);
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (blockStatus[k][l] == spieler_bombe[playerNR]) {
 				playeralive[playerNR] = false;
 			}
 			if (k + z1 < Feldgroesse_x) {
 				if (blockStatus[k + z1][l] == solid) {
 					break;
 				} else {
					if (blockStatus[k + z1][l] == spieler[playerNR]) {
						playeralive[playerNR] = false;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (k - z1 > 0) {
 				if (blockStatus[k - z1][l] == solid) {
 					break;
 				} else {
					if (blockStatus[k - z1][l] == spieler[playerNR]) {
						playeralive[playerNR] = false;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (l + z1 < Feldgroesse_y) {
 				if (blockStatus[k][l + z1] == solid) {
 					break;
 				} else {
					if (blockStatus[k][l + z1] == spieler[playerNR]) {
						playeralive[playerNR] = false;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (l - z1 > 0) {
 				if (blockStatus[k][l - z1] == solid) {
 					break;
 				} else {
					if (blockStatus[k][l - z1] == spieler[playerNR]) {
						playeralive[playerNR] = false;
 					}
 				}
 			}
 
 		}
 		if (playeralive[0] == false || playeralive[1] == false) {
 			if (playeralive[0] == false && playeralive[1] == false) {
 				game_over.start();
 			} else {
 				zufallsPortal();
 			}
 		}
 		/**********************************************************
 		 * ersetzen der break- und spieler blocks durch explosion *
 		 **********************************************************/
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (k + z1 < Feldgroesse_x) {
 				if (blockStatus[k + z1][l] == solid) {
 					break;
 				} else {
 					if (blockStatus[k + z1][l] == spieler[1]
 							|| blockStatus[k + z1][l] == spieler[0]
 							|| blockStatus[k + z1][l] == breakblock
 							|| blockStatus[k + z1][l] == ground) {
 						blockStatus[k + z1][l] = explosion_horizontal;
 					}
 					if (blockStatus[k + z1][l] == bombesetzen
 							|| blockStatus[k + z1][l] == spieler_bombe[1]
 							|| blockStatus[k + z1][l] == spieler_bombe[0]) {
 						// explosion2.stop();
 						// explosion2_zeichnen.start();
 					}
 					if (blockStatus[k + z1][l] == versteckterausgang) {
 						blockStatus[k + z1][l] = ausgang;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (k - z1 > 0) {
 				if (blockStatus[k - z1][l] == solid) {
 					break;
 				} else {
 					if (blockStatus[k - z1][l] == spieler[1]
 							|| blockStatus[k - z1][l] == spieler[0]
 							|| blockStatus[k - z1][l] == breakblock
 							|| blockStatus[k - z1][l] == ground) {
 						blockStatus[k - z1][l] = explosion_horizontal;
 					}
 					if (blockStatus[k - z1][l] == bombesetzen
 							|| blockStatus[k - z1][l] == spieler_bombe[1]
 							|| blockStatus[k - z1][l] == spieler_bombe[0]) {
 						// explosion2.stop();
 						// explosion2_zeichnen.start();
 					}
 					if (blockStatus[k - z1][l] == versteckterausgang) {
 						blockStatus[k - z1][l] = ausgang;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (l - z1 > 0) {
 				if (blockStatus[k][l - z1] == solid) {
 					break;
 				} else {
 					if (blockStatus[k][l - z1] == spieler[1]
 							|| blockStatus[k][l - z1] == spieler[0]
 							|| blockStatus[k][l - z1] == breakblock
 							|| blockStatus[k][l - z1] == ground) {
 						blockStatus[k][l - z1] = explosion_vertikal;
 					}
 					if (blockStatus[k][l - z1] == bombesetzen
 							|| blockStatus[k][l - z1] == spieler_bombe[1]
 							|| blockStatus[k][l - z1] == spieler_bombe[0]) {
 						// explosion2.stop();
 						// explosion2_zeichnen.start();
 					}
 					if (blockStatus[k][l - z1] == versteckterausgang) {
 						blockStatus[k][l - z1] = ausgang;
 					}
 				}
 			}
 		}
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (l + z1 < Feldgroesse_y) {
 				if (blockStatus[k][l + z1] == solid) {
 					break;
 				} else {
 					if (blockStatus[k][l + z1] == spieler[1]
 							|| blockStatus[k][l + z1] == spieler[0]
 							|| blockStatus[k][l + z1] == breakblock
 							|| blockStatus[k][l + z1] == ground) {
 						blockStatus[k][l + z1] = explosion_vertikal;
 					}
 					if (blockStatus[k][l + z1] == bombesetzen
 							|| blockStatus[k][l + z1] == spieler_bombe[1]
 							|| blockStatus[k][l + z1] == spieler_bombe[0]) {
 						// explosion.stop();
 						// explozeichnen(playerNR, bombsLeft);
 					}
 					if (blockStatus[k][l + z1] == versteckterausgang) {
 						blockStatus[k][l + z1] = ausgang;
 					}
 
 				}
 			}
 		}
 		blockStatus[k][l] = explosion_mitte;
 		zeichnen();
 		Sound.soundeffekt("Audio/boom.au");
 
 	}
 
 	public void exploende(int playerNR, int bombs) {
 
 		int k = a[playerNR][bombs];
 		int l = b[playerNR][bombs];
 		for (int z1 = 1; z1 <= radius[playerNR]; z1++) {
 			if (k + z1 < Feldgroesse_x) {
 				if (blockStatus[k + (z1 - 1)][l] != solid) {
 					if (blockStatus[k + z1][l] == explosion_horizontal) {
 						blockStatus[k + z1][l] = ground;
 					}
 
 				}
 			}
 			if (k - z1 > 0) {
 				if (blockStatus[k - (z1 - 1)][l] != solid) {
 					if (blockStatus[k - z1][l] == explosion_horizontal) {
 						blockStatus[k - z1][l] = ground;
 					}
 
 				}
 			}
 			if (l - z1 > 0) {
 				if (blockStatus[k][l - (z1 - 1)] != solid) {
 					if (blockStatus[k][l - z1] == explosion_vertikal) {
 						blockStatus[k][l - z1] = ground;
 					}
 
 				}
 			}
 			if (l + z1 < Feldgroesse_y) {
 				if (blockStatus[k][l + (z1 - 1)] != solid) {
 					if (blockStatus[k][l + z1] == explosion_vertikal) {
 						blockStatus[k][l + z1] = ground;
 					}
 
 				}
 			}
 		}
 		blockStatus[k][l] = ground;
 		zeichnen();
 
 		nextbomb[0] = true;
 		if (playerNR == 0)
 			bombsLeftP1++;
 
 		if (playerNR == 1)
 			bombsLeftP2++;
 
 	}
 
 	/*********************************************************************
 	 * Timer fr eine kurze verzgerung vor neustart bei sieg/niederlage *
 	 *********************************************************************/
 	// Einblenden der Sieg/ Niederlage Bilder
 	javax.swing.Timer game_over = new javax.swing.Timer(600,
 			new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (playeralive[0] == true && playeralive[1] == false) {
 						window.setSize(320, 230);
 						JLabel endscreen;
 						window.gamepanel.removeAll();
 						endscreen = new JLabel(player1wins);
 						endscreen.setBounds(0, 0, 320, 180);
 						window.gamepanel.add(endscreen);
 						Sound.stoppen();
 						Sound.soundeffekt("Audio/player1wins.au");
 
 					}
 
 					if (playeralive[0] == false && playeralive[1] == true) {
 						window.setSize(330, 230);
 						JLabel endscreen;
 						window.gamepanel.removeAll();
 						endscreen = new JLabel(player2wins);
 						endscreen.setBounds(0, 0, 320, 180);
 						window.gamepanel.add(endscreen);
 						Sound.stoppen();
 						Sound.soundeffekt("Audio/player2wins.au");
 					}
 
 					if (playeralive[0] == false && playeralive[1] == false) {
 						window.setSize(330, 230);
 						JLabel endscreen;
 						window.gamepanel.removeAll();
 						endscreen = new JLabel(bothplayerdead);
 						endscreen.setBounds(0, 0, 320, 180);
 						window.gamepanel.add(endscreen);
 						Sound.stoppen();
 						Sound.soundeffekt("Audio/bothplayersdead2.au");
 
 					}
 					x[0] = 1;
 					y[0] = 1;
 					x[1] = (Feldgroesse_x - 2);
 					y[1] = (Feldgroesse_y - 2);
 					blockStatus[x[0]][y[0]] = spieler[0];
 					blockStatus[x[1]][y[1]] = spieler[1];
 					playeralive[0] = true;
 					playeralive[1] = true;
 					game_over_intern.start();
 					game_over.stop();
 
 				}
 			});
 
 	// Neustart
 	javax.swing.Timer game_over_intern = new javax.swing.Timer(3800,
 			new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					Sound.loopen();
 					standardfeld();
 					zeichnen();
 					game_over_intern.stop();
 				}
 			});
 
 	/*****************************************************************
 	 * erstellt ein Portal unter einem zuflligen zerstrbaren Block *
 	 *****************************************************************/
 	private void zufallsPortal() {
 		if (Portal_vorhanden == false) {
 			Random r = new Random();
 			int random_x = r.nextInt(Feldgroesse_x);
 			int random_y = r.nextInt(Feldgroesse_y);
 
 			if (blockStatus[random_x][random_y] == breakblock) {
 				blockStatus[random_x][random_y] = versteckterausgang;
 				Portal_vorhanden = true;
 			} else if (blockStatus[random_x][random_y] == ground) {
 				blockStatus[random_x][random_y] = ausgang;
 				Portal_vorhanden = true;
 			} else {
 				zufallsPortal();
 			}
 		}
 
 	}
 
 	/***********************************
 	 * Methode fuer die erste Steurung *
 	 **********************************/
 
 	public void control(int playerNR) {
 		boolean moveRight = Spieler.moveRight;
 		/** Bewegung nach links */
 		boolean moveLeft = Spieler.moveLeft;
 		/** Bewegung nach unten */
 		boolean moveDown = Spieler.moveDown;
 		/** Bewegung nach oben */
 		boolean moveUp = Spieler.moveUp;
 		/** Bombe legen */
 		boolean bomb = Spieler.bomb;
 
 		if (moveRight == true
 				&& playeralive[playerNR] == true
 				&& (blockStatus[x[playerNR] + 1][y[playerNR]] == ground || blockStatus[x[playerNR] + 1][y[playerNR]] == ausgang)) {
 			if (blockStatus[x[playerNR] + 1][y[playerNR]] == ausgang) {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 				game_over.start();
 			} else if (blockStatus[x[playerNR]][y[playerNR]] == spieler_bombe[playerNR]) {
 				blockStatus[x[playerNR]][y[playerNR]] = bombesetzen;
 			} else {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 			}
 			x[playerNR]++;
 			blockStatus[x[playerNR]][y[playerNR]] = spieler[playerNR];
 			zeichnen();
 		}
 		if (moveLeft == true
 				&& playeralive[playerNR] == true
 				&& (blockStatus[x[playerNR] - 1][y[playerNR]] == ground || blockStatus[x[playerNR] - 1][y[playerNR]] == ausgang)) {
 			if (blockStatus[x[playerNR] - 1][y[playerNR]] == ausgang) {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 				game_over.start();
 			} else if (blockStatus[x[playerNR]][y[playerNR]] == spieler_bombe[playerNR]) {
 				blockStatus[x[playerNR]][y[playerNR]] = bombesetzen;
 			} else {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 			}
 			x[playerNR]--;
 			blockStatus[x[playerNR]][y[playerNR]] = spieler[playerNR];
 
 			zeichnen();
 		}
 		if (moveUp == true
 				&& playeralive[playerNR] == true
 				&& (blockStatus[x[playerNR]][y[playerNR] - 1] == ground || blockStatus[x[playerNR]][y[playerNR] - 1] == ausgang)) {
 			if (blockStatus[x[playerNR]][y[playerNR] - 1] == ausgang) {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 				game_over.start();
 			} else if (blockStatus[x[playerNR]][y[playerNR]] == spieler_bombe[playerNR]) {
 				blockStatus[x[playerNR]][y[playerNR]] = bombesetzen;
 			} else {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 			}
 			y[playerNR]--;
 			blockStatus[x[playerNR]][y[playerNR]] = spieler[playerNR];
 
 			zeichnen();
 		}
 
 		if (moveDown == true
 				&& playeralive[playerNR] == true
 				&& (blockStatus[x[playerNR]][y[playerNR] + 1] == ground || blockStatus[x[playerNR]][y[playerNR] + 1] == ausgang)) {
 
 			if (blockStatus[x[playerNR]][y[playerNR] + 1] == ausgang) {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 				game_over.start();
 			}
 			if (blockStatus[x[playerNR]][y[playerNR]] == spieler_bombe[playerNR]) {
 				blockStatus[x[playerNR]][y[playerNR]] = bombesetzen;
 			} else {
 				blockStatus[x[playerNR]][y[playerNR]] = ground;
 			}
 			y[playerNR]++;
 			blockStatus[x[playerNR]][y[playerNR]] = spieler[playerNR];
 			zeichnen();
 		}
 
 		if (bomb == true && playeralive[playerNR] == true) {
 
 			if (playerNR == 0 && bombsLeftP1 >= 0) {
 
 				blockStatus[x[0]][y[0]] = spieler_bombe[0];
 
 				a[0][bombsLeftP1] = x[0];
 				b[0][bombsLeftP1] = y[0];
 
 				Bomb bombe = new Bomb(this, 0, bombsLeftP1);
 				zeichnen();
 				bombe.explosion.start();
 				bombsLeftP1--;
 
 			}
 
 			else if (playerNR == 1 && bombsLeftP2 >= 0) {
 				blockStatus[x[1]][y[1]] = spieler_bombe[1];
 
 				a[1][bombsLeftP2] = x[1];
 				b[1][bombsLeftP2] = y[1];
 
 				Bomb bombe = new Bomb(this, 1, bombsLeftP2);
 				zeichnen();
 				bombe.explosion.start();
 				bombsLeftP2--;
 
 			}
 		}
 	}
 
 	// Getter - Setter
 	public int getFeldgroesse_x() {
 		return Feldgroesse_x;
 	}
 
 	public int getFeldgroesse_y() {
 		return Feldgroesse_y;
 	}
 
 	public int[][] getBlockStatus() {
 		return blockStatus;
 	}
 
 }

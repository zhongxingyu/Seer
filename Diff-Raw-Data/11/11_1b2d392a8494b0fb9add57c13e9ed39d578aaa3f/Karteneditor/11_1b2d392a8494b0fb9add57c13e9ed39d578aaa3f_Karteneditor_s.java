 package Spielfeld;
 
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import Gui.Main;
 
 /**
  * Klasse zur Erstellung des Basis-Spielfelds mit den zugewiesenen Grafiken
  * 
  * @author Gruppe37
  * @version 0.9
  * @param Main
  *            parent
  */
 public class Karteneditor extends JPanel implements ChangeListener,
 		MouseListener {
 
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
 	private ImageIcon portal;
 	private ImageIcon blank_field;
 
 	/** horizontale Koordinate des Spielfelds */
 	private int m = 0;
 	/** vertikale Koordinate des Spielfelds */
 	private int n = 0;
 	private final int blank = 0;
 	private final int solid = 1;
 	private final int breakblock = 2;
 	private final int ground = 3;
 	private final int spieler = 8;
 	private final int versteckterausgang = 9;
 	private final int ausgang = 10;
 	private final int spieler2 = 11;
 
 	boolean brkbleButton_on;
 	boolean solidButton_on;
 	boolean grndButton_on;
 	boolean playerButton_on;
 	boolean player2Button_on;
 
 	static final int hoehe_MIN = 5;
 	static final int hoehe_MAX = 30;
 	static final int hoehe_INIT = 11;
 
 	static final int breite_MIN = 5;
 	static final int breite_MAX = 30;
 	static final int breite_INIT = 11;
 
 	public static int breite = breite_INIT;
 	public static int hoehe = hoehe_INIT;
 
 	private int Feldgroesse_x = breite;
 	private int Feldgroesse_y = hoehe;
 
 	/** Array in dem das Feld erstellt wird */
 	private final JLabel fblock[][] = new JLabel[breite_MAX][hoehe_MAX];
 	/** Definition der einzelnen Spielfeldelemente */
 	private final int blockStatus[][] = new int[breite_MAX][hoehe_MAX];
 
 	private final JSlider hoehe_s = new JSlider(hoehe_MIN, hoehe_MAX,
 			hoehe_INIT);
 	private final JSlider breite_s = new JSlider(breite_MIN, breite_MAX,
 			breite_INIT);
 	JLabel nameSolid = new JLabel("Solid");
 	JLabel namePlayer2 = new JLabel("Spieler2");
 	JLabel nameBrkble = new JLabel("Zerstrbar");
 	JLabel namePlayer1 = new JLabel("Spieler1");
 	JLabel nameGrnd = new JLabel("Boden");
 	JLabel nameHoehe = new JLabel("Hoehe");
 	JLabel nameBreite = new JLabel("Breite");
 
 	final JToggleButton solidButton = new JToggleButton(solidBlock);
 	final JToggleButton player2Button = new JToggleButton(player2);
 	final JToggleButton playerButton = new JToggleButton(player);
 	final JToggleButton brkbleButton = new JToggleButton(brkbleBlock);
 	final JToggleButton grndButton = new JToggleButton(grndBlock);
 
 	private final Main window;
 
 	/** Spielfeld wird im Fenster Main angezeigt */
 	public Karteneditor(Main parent) {
 
 		window = parent;
 		window.setResizable(false);
 		window.addMouseListener(this);
 
 		loadContentImages();
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
 		portal = new ImageIcon("Images/portal.gif");
 		blank_field = new ImageIcon("Images/bg.png");
 	}
 
 	public void standardfeld() {
 		remove();
 		Feldgroesse_x = breite;
 		Feldgroesse_y = hoehe;
 		window.setSize(((Feldgroesse_x * 30) + 160),
 				((Feldgroesse_y * 30) + 50));
 		window.gameedit.setBounds(0, 0, Feldgroesse_x * 30, Feldgroesse_y * 30);
 
 		for (n = 0; n < Feldgroesse_y; n++) {
 			for (m = 0; m < Feldgroesse_x; m++) {
 				if (m == 0 || n == 0 || n == Feldgroesse_y - 1
 						|| m == Feldgroesse_x - 1) {
 					blockStatus[m][n] = solid;
 				} else if (blockStatus[m][n] == 0) {
 					blockStatus[m][n] = blank;
 				}
 			}
 
 		}
 		Button();
 		Slider();
 		// updateUI();
 		zeichnen();
 
 	}
 
 	public void zeichnen() {
 		window.gameedit.removeAll();
 		for (m = 0; m < Feldgroesse_x; m++) {
 			for (n = 0; n < Feldgroesse_y; n++) {
 				if (blockStatus[m][n] == ground) {
 					fblock[m][n] = new JLabel(grndBlock);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 
 				else if (blockStatus[m][n] == solid) {
 					fblock[m][n] = new JLabel(solidBlock);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 
 				else if (blockStatus[m][n] == breakblock) {
 					fblock[m][n] = new JLabel(brkbleBlock);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 				/*
 				 * Spieler
 				 */
 				else if (blockStatus[m][n] == spieler) {
 					fblock[m][n] = new JLabel(player);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 
 				/*
 				 * Spieler2
 				 */
 				else if (blockStatus[m][n] == spieler2) {
 					fblock[m][n] = new JLabel(player2);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 
 				/*
 				 * Ausgang
 				 */
 				else if (blockStatus[m][n] == ausgang) {
 					fblock[m][n] = new JLabel(portal);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == versteckterausgang) {
 					fblock[m][n] = new JLabel(brkbleBlock);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				} else if (blockStatus[m][n] == blank) {
 					fblock[m][n] = new JLabel(blank_field);
 					window.gameedit.add(fblock[m][n]);
 					fblock[m][n].setBounds(m * 30, n * 30, 30, 30);
 
 				}
 			}
 		}
 	}
 
 	public void Blocksetzen() {
 
 		if (m == 0 || n == 0 || n == Feldgroesse_y - 1
 				|| m == Feldgroesse_x - 1) {
 			if (brkbleButton_on == true || solidButton_on == true
 					|| grndButton_on == true || playerButton_on == true
 					|| player2Button_on == true) {
 				JOptionPane.showMessageDialog(null,
 						"Verndern der Randblcke nicht mglich.");
 			}
 		} else {
 			if (solidButton_on == true) {
 				blockStatus[m][n] = solid;
 			} else if (grndButton_on == true) {
 				blockStatus[m][n] = ground;
 			} else if (brkbleButton_on == true) {
 				blockStatus[m][n] = breakblock;
 			} else if (playerButton_on == true) {
 				for (int x = 0; x < Feldgroesse_y; x++) {
 					for (int y = 0; y < Feldgroesse_x; y++) {
 						if (blockStatus[x][y] == spieler) {
 							blockStatus[x][y] = blank;
 						}
 					}
 
 				}
 				blockStatus[m][n] = spieler;
 			} else if (player2Button_on == true) {
 				for (int x = 0; x < Feldgroesse_y; x++) {
 					for (int y = 0; y < Feldgroesse_x; y++) {
 						if (blockStatus[x][y] == spieler2) {
 							blockStatus[x][y] = blank;
 						}
 					}
 
 				}
 				blockStatus[m][n] = spieler2;
 			}
 			zeichnen();
 		}
 	}
 
 	public void mausposi() {
 		PointerInfo info = MouseInfo.getPointerInfo();
 		Point location = info.getLocation();
 		m = Math.round((location.x - 5) / 30);
 		n = Math.round((location.y - 45) / 30);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
		mausposi();
		if (m < Feldgroesse_x && n < Feldgroesse_y) {
			Blocksetzen();
		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent paramMouseEvent) {
		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent paramMouseEvent) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent paramMouseEvent) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent paramMouseEvent) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void Slider() {
 		window.setLayout(null);
 
 		/*
 		 * Einstellungen fuer Hoehen-Slider
 		 */
 		hoehe_s.setBounds(((Feldgroesse_x * 30) + 20), 210, 120, 50);
 		hoehe_s.addChangeListener(this);
 		hoehe_s.setPaintTicks(true);
 		hoehe_s.setMajorTickSpacing(5);
 		hoehe_s.setMinorTickSpacing(1);
 		hoehe_s.setPaintLabels(true);
 
 		nameHoehe.setBounds(((Feldgroesse_x * 30) + 20), 200, 80, 10);
 		nameHoehe.setToolTipText("Die Hoehe des Spielfelds in Bausteinen");
 
 		/*
 		 * Einstellungen fuer Breiten-Slider
 		 */
 		breite_s.setBounds(((Feldgroesse_x * 30) + 20), 270, 120, 50);
 		breite_s.addChangeListener(this);
 		breite_s.setPaintTicks(true);
 		breite_s.setMajorTickSpacing(5);
 		breite_s.setMinorTickSpacing(1);
 		breite_s.setPaintLabels(true);
 
 		nameBreite.setBounds(((Feldgroesse_x * 30) + 20), 260, 80, 10);
 		nameBreite.setToolTipText("Die Breite des Spielfelds in Bausteinen");
 
 		window.add(breite_s);
 		window.add(hoehe_s);
 		window.add(nameBreite);
 		window.add(nameHoehe);
 
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		JSlider source = (JSlider) e.getSource();
 
 		if (!source.getValueIsAdjusting()) {
 			for (n = 0; n < Feldgroesse_y; n++) {
 				for (m = 0; m < Feldgroesse_x; m++) {
 					if (m == 0 || n == 0 || n == Feldgroesse_y - 1
 							|| m == Feldgroesse_x - 1) {
 						blockStatus[m][n] = blank;
 					}
 				}
 			}
 			breite = breite_s.getValue();
 			hoehe = hoehe_s.getValue();
 			Feldgroesse_x = breite;
 			Feldgroesse_y = hoehe;
 			window.setSize(((Feldgroesse_x * 30) + 160),
 					((Feldgroesse_y * 30) + 50));
 			window.gameedit.setBounds(0, 0, Feldgroesse_x * 30,
 					Feldgroesse_y * 30);
 			standardfeld();
 		}
 
 	}
 
 	public void Button() {
 		/*
 		 * BUTTON
 		 */
 
 		solidButton.setIcon(solidBlock);
 		player2Button.setIcon(player2);
 		playerButton.setIcon(player);
 		brkbleButton.setIcon(brkbleBlock);
 		grndButton.setIcon(grndBlock);
 		grndButton.setBounds(((Feldgroesse_x * 30) + 20), 20, 30, 30);
 		grndButton.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (grndButton.isSelected()) {
 					brkbleButton.setEnabled(false);
 					solidButton.setEnabled(false);
 					playerButton.setEnabled(false);
 					player2Button.setEnabled(false);
 					grndButton_on = true;
 				} else {
 					brkbleButton.setEnabled(true);
 					solidButton.setEnabled(true);
 					grndButton.setEnabled(true);
 					playerButton.setEnabled(true);
 					player2Button.setEnabled(true);
 					grndButton_on = false;
 				}
 			}
 		});
 		/*
 		 * BUTTON
 		 */
 
 		brkbleButton.setBounds(((Feldgroesse_x * 30) + 20), 70, 30, 30);
 		brkbleButton.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (brkbleButton.isSelected()) {
 					solidButton.setEnabled(false);
 					grndButton.setEnabled(false);
 					playerButton.setEnabled(false);
 					player2Button.setEnabled(false);
 					brkbleButton_on = true;
 
 				} else {
 					brkbleButton.setEnabled(true);
 					solidButton.setEnabled(true);
 					grndButton.setEnabled(true);
 					playerButton.setEnabled(true);
 					player2Button.setEnabled(true);
 					brkbleButton_on = false;
 				}
 			}
 		});
 		/*
 		 * BUTTON
 		 */
 
 		playerButton.setBounds(((Feldgroesse_x * 30) + 90), 20, 30, 30);
 		playerButton.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (playerButton.isSelected()) {
 					brkbleButton.setEnabled(false);
 					solidButton.setEnabled(false);
 					grndButton.setEnabled(false);
 					player2Button.setEnabled(false);
 					playerButton_on = true;
 				} else {
 					brkbleButton.setEnabled(true);
 					solidButton.setEnabled(true);
 					grndButton.setEnabled(true);
 					playerButton.setEnabled(true);
 					player2Button.setEnabled(true);
 					playerButton_on = false;
 				}
 			}
 		});
 		/*
 		 * BUTTON
 		 */
 		player2Button.setBounds(((Feldgroesse_x * 30) + 90), 70, 30, 30);
 		player2Button.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (player2Button.isSelected()) {
 					brkbleButton.setEnabled(false);
 					solidButton.setEnabled(false);
 					grndButton.setEnabled(false);
 					playerButton.setEnabled(false);
 					player2Button_on = true;
 				} else {
 					brkbleButton.setEnabled(true);
 					solidButton.setEnabled(true);
 					grndButton.setEnabled(true);
 					playerButton.setEnabled(true);
 					player2Button.setEnabled(true);
 					player2Button_on = false;
 				}
 			}
 		});
 		/*
 		 * BUTTON
 		 */
 
 		solidButton.setBounds(((Feldgroesse_x * 30) + 20), 120, 30, 30);
 		solidButton.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (solidButton.isSelected()) {
 					brkbleButton.setEnabled(false);
 					grndButton.setEnabled(false);
 					playerButton.setEnabled(false);
 					player2Button.setEnabled(false);
 					solidButton_on = true;
 				} else {
 					brkbleButton.setEnabled(true);
 					solidButton.setEnabled(true);
 					grndButton.setEnabled(true);
 					playerButton.setEnabled(true);
 					player2Button.setEnabled(true);
 					solidButton_on = false;
 				}
 			}
 		});
 		nameSolid.setBounds(((Feldgroesse_x * 30) + 20), 105, 80, 10);
 		nameBrkble.setBounds(((Feldgroesse_x * 30) + 20), 55, 80, 10);
 		namePlayer1.setBounds(((Feldgroesse_x * 30) + 90), 5, 80, 10);
 		namePlayer2.setBounds(((Feldgroesse_x * 30) + 90), 55, 80, 10);
 
 		nameGrnd.setBounds(((Feldgroesse_x * 30) + 20), 5, 80, 10);
 
 		window.add(nameSolid);
 		window.add(nameGrnd);
 		window.add(nameBrkble);
 		window.add(namePlayer1);
 		window.add(namePlayer2);
 
 		window.add(solidButton);
 		window.add(grndButton);
 		window.add(brkbleButton);
 		window.add(playerButton);
 		window.add(player2Button);
 	}
 
 	public void remove() {
 
 		window.getContentPane().remove(breite_s);
 		window.getContentPane().remove(hoehe_s);
 		window.getContentPane().remove(nameBreite);
 		window.getContentPane().remove(nameHoehe);
 		window.getContentPane().remove(nameSolid);
 		window.getContentPane().remove(nameGrnd);
 		window.getContentPane().remove(nameBrkble);
 		window.getContentPane().remove(namePlayer1);
 		window.getContentPane().remove(namePlayer2);
 		window.getContentPane().remove(solidButton);
 		window.getContentPane().remove(grndButton);
 		window.getContentPane().remove(brkbleButton);
 		window.getContentPane().remove(playerButton);
 		window.getContentPane().remove(player2Button);
 	}
 
 }

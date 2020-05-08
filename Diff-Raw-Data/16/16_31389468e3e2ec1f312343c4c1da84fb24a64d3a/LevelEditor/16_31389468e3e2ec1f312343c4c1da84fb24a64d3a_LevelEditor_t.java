 package de.hhu.propra12.gruppe27.bomberman.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Properties;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import de.hhu.propra12.gruppe27.bomberman.core.Block;
 import de.hhu.propra12.gruppe27.bomberman.core.Level;
 import de.hhu.propra12.gruppe27.bomberman.core.LevelGen;
 import de.hhu.propra12.gruppe27.bomberman.core.Path;
 import de.hhu.propra12.gruppe27.bomberman.core.PathFinder;
 import de.hhu.propra12.gruppe27.bomberman.core.SysEinst;
 import de.hhu.propra12.gruppe27.bomberman.core.Wall;
 
 /**
  * Klasse LevelEditor implementiert Serializable und dient der Einstellung der
  * Level
  * 
  * @author Gruppe 27
  * @version 1.0
  * 
  */
 
 public class LevelEditor extends Level implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Feldbestimmung Koordinaten des Feldes: x und y
 	 */
 
 	SysEinst system = SysEinst.getSystem();
 	int feldx = system.getfeldxbml();
 	int feldy = system.getfeldybml();
 
 	int intfeld = 0;
 
 	Exit e;
 
 	JButton button[][] = new JButton[feldx][feldy];
 
 	/**
 	 * Konstruktor
 	 */
 
 	public LevelEditor(int feldx, int feldy) {
 		super(feldx, feldy);
 	}
 
 	/**
 	 * Frame initialisieren und Sichtbar machen. Feste Groesse. Mittig
 	 * ausrichten
 	 */
 
 	public void leveleditor() {
 
 		final JFrame frameeditor = new JFrame("Leveleditor");
 		frameeditor.setVisible(true);
 		frameeditor.setResizable(false);
 		frameeditor.setLocationRelativeTo(null);
 		frameeditor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		JPanel panel = new JPanel(new GridBagLayout());
 		frameeditor.add(panel);
 
 		frameeditor.getContentPane().add(panel, BorderLayout.NORTH);
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.gridx = 0;
 		c.gridy = 0;
 
 		for (int i = 0; i < feldx; i++) {
 			for (int j = 0; j < feldy; j++) {
 
 				button[i][j] = new JButton("F");
 				c.gridx = i + 1;
 				c.gridy = j;
 				c.fill = GridBagConstraints.HORIZONTAL;
 				// c.weightx = 1.0;
 				panel.add(button[i][j], c);
 
 				laxbr[i][j] = new Path(i, j, this);
 
 			}
 		}
 
 		/**
 		 * Buttons um die Felder zu setzen F = freies Feld, W = feste Wand, Z =
 		 * zerstoerbarer Block
 		 * 
 		 */
 
 		ButtonGroup group = new ButtonGroup();
 		JToggleButton buttonF = new JToggleButton("Frei");
 		c.gridx = 0;
 		c.gridy = 0;
 		panel.add(buttonF, c);
 		group.add(buttonF);
 
 		JToggleButton buttonW = new JToggleButton("Feste Wand");
 		c.gridx = 0;
 		c.gridy = 1;
 		panel.add(buttonW, c);
 		group.add(buttonW);
 
 		JToggleButton buttonZ = new JToggleButton("Zerstoerbar");
 		c.gridx = 0;
 		c.gridy = 2;
 		panel.add(buttonZ, c);
 		group.add(buttonZ);
 
 		// JToggleButton buttonE = new JToggleButton("Ausgang");
 		// c.gridx = 0;
 		// c.gridy = 3;
 		// panel.add(buttonE, c);
 		// group.add(buttonE);
 		//
 		// JToggleButton buttonV = new JToggleButton("Versteckt");
 		// c.gridx = 0;
 		// c.gridy = 4;
 		// panel.add(buttonV, c);
 		// group.add(buttonV);
 
 		JButton buttonImp = new JButton("Importieren");
 		c.gridx = 0;
 		c.gridy = 6;
 		panel.add(buttonImp, c);
 
 		JButton buttonEx = new JButton("Exportieren");
 		c.gridx = 0;
 		c.gridy = 7;
 		panel.add(buttonEx, c);
 
 		frameeditor.setSize(frameeditor.getPreferredSize());
 
 		/**
 		 * ActionListener wird hinzufuegt. Wenn ein Button gedrueckt wird, wird
 		 * ueberprueft, was das aktuelle Label ist und wird umbenannt. Dabei
 		 * wird das Fenster an die neue Buttongroesse angepasst, um zu
 		 * verhindern, dass Buttons ausserhalb des Fensterrands geraten
 		 */
 
 		for (int i = 0; i < feldx; i++) {
 			for (int j = 0; j < feldy; j++) {
 
 				final int k = i;
 				final int l = j;
 
 				button[i][j].addActionListener(new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 
 						if (0 == intfeld) {
 							laxbrpath(k, l);
 							button[k][l].setText("F");
 							frameeditor.setSize(frameeditor.getPreferredSize());
 						}
 						if (1 == intfeld) {
 							laxbrwall(k, l);
 							button[k][l].setText("W");
 							frameeditor.setSize(frameeditor.getPreferredSize());
 						}
 						if (2 == intfeld) {
 							laxbrblock(k, l);
 							button[k][l].setText("B");
 							frameeditor.setSize(frameeditor.getPreferredSize());
 						}
 						// if (3 == intfeld) {
 						// laxbrexit(k, l);
 						// button[k][l].setText("E");
 						// frameeditor.setSize(frameeditor.getPreferredSize());
 						// }
 						// if (4 == intfeld) {
 						// laxbrhidden(k, l);
 						// button[k][l].setText("V");
 						// frameeditor.setSize(frameeditor.getPreferredSize());
 						// }
 
 					}
 				});
 			}
 		}
 
 		/**
 		 * Action Listener wird hinzugefuegt fuer Feld 0
 		 */
 
 		buttonF.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				intfeld = 0;
 
 			}
 		});
 
 		/**
 		 * Action Listener wird hinzugefuegt fuer Feld 1
 		 */
 
 		buttonW.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				intfeld = 1;
 			}
 		});
 
 		/**
 		 * Action Listener wird hinzugefuegt fuer Feld 2
 		 */
 
 		buttonZ.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				intfeld = 2;
 			}
 		});
 
 		// /**
 		// * Action Listener wird hinzugefuegt fuer Feld 3
 		// */
 		//
 		// buttonE.addActionListener(new ActionListener() {
 		//
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		//
 		// intfeld = 3;
 		// }
 		// });
 		//
 		// /**
 		// * Action Listener wird hinzugefuegt fuer Feld 4
 		// */
 		//
 		// buttonV.addActionListener(new ActionListener() {
 		//
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		//
 		// intfeld = 4;
 		// }
 		// });
 
 		/**
 		 * ActionListener wird hinzugefuegt, wenn Datei falsch eingelesen wird,
 		 * erscheint eine Fehlermeldung
 		 */
 
 		buttonImp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				OpenFileDialog filedialog = new OpenFileDialog();
 
 				if (importlvl(filedialog.loadFile(new Frame(),
 						"Level importieren...", ".\\data\\levels", "*.bml"))) {
 					System.out.println("Level erfolgreich eingelesen!");
 					frameeditor.setSize(frameeditor.getPreferredSize());
 				} else {
 					System.out.println("Fehler beim Einlesen der Datei!");
 				}
 			}
 		});
 
 		/**
 		 * ActionListener wird hinzugefuegt Leveleigenschaften werden defieniert
 		 */
 
 		buttonEx.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int[][] ToBeChecked = convertButtonsToMap();
 
 				if ((PathFinder.check(ToBeChecked, 1, 1,
 						ToBeChecked[0].length - 2, 1))
						&& (PathFinder.check(ToBeChecked,
								ToBeChecked[0].length - 2, 1,
 								ToBeChecked[0].length - 2,
 								ToBeChecked[1].length - 2))
						&& (PathFinder.check(ToBeChecked,
								ToBeChecked[0].length - 2,
								ToBeChecked[1].length - 2, 1,
 								ToBeChecked[1].length - 2))) {
 					Properties levelstructure = new Properties();
 					levelstructure.setProperty("LEVEL", convertButtons());
 					levelstructure.setProperty("LAENGE",
 							Integer.toString(feldx));
 					levelstructure.setProperty("BREITE",
 							Integer.toString(feldy));
 
 					OpenFileDialog filedialog = new OpenFileDialog();
 
 					if (exportlvl(
 							filedialog.saveFile(new Frame(),
 									"Level exportieren...", ".\\data\\levels",
 									"*.bml"), levelstructure)) {
 						System.out.println("Level erfolgreich gespeichert!");
 					} else {
 						System.out
 								.println("Fehler beim speichern der Leveldatei!");
 					}
 				} else {
 					System.out
 							.println("Das Level ist inkonsistent!Nachricht an den Benutzer raus!\n");
 					JOptionPane
 							.showMessageDialog(
 									null,
 									"Ihr erstelltes Level ist inkonsistent!\nBitte ueberpruefen sie Ihr Level und wiederholen Sie den Vorgang!",
 									"Level inkonsistent!",
 									JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * Methoden um die einzelnen Felder zu bestimmen
 	 * 
 	 * @param i
 	 * @param j
 	 *            Weg, Wand, Block, Exit werden definiert
 	 */
 
 	public void laxbrpath(int i, int j) {
 		laxbr[i][j] = new Path(i, j, this);
 	}
 
 	public void laxbrwall(int i, int j) {
 		laxbr[i][j] = new Wall(i, j, this);
 	}
 
 	public void laxbrblock(int i, int j) {
 		laxbr[i][j] = new Block(i, j, this);
 	}
 
 	public void laxbrexit(int i, int j) {
 		laxbr[i][j] = new Path(i, j, this);
 		e = new Exit(laxbr[i][j]);
 	}
 
 	public void laxbrhidden(int i, int j) {
 		laxbr[i][j] = new Block(i, j, this);
 		e = new Exit(laxbr[i][j]);
 	}
 
 	/**
 	 * Eine Methode, die die Labels der Level-Editor Buttons in einen String
 	 * umsetzt und fuer das Ende der ersten Dimension immer ein Semikolon setzt.
 	 * 
 	 * @return Gibt einen String zurueck, der die Button-Labels enthaelt.
 	 * */
 	public String convertButtons() {
 		String output = "";
 
 		for (int i = 0; i < feldx; i++) {
 			for (int j = 0; j < feldy; j++) {
 
 				String compare = button[j][i].getText(); // Damit .getText()
 															// nicht bei jedem
 															// if ausgef�hrt
 															// werden muss.
 
 				if (compare == "F") {
 					output += "0";
 				} else if (compare == "W") {
 					output += "1";
 				} else if (compare == "B") {
 					output += "2";
 				} else if (compare == "E") {
 					output += 3;
 				} else if (compare == "V") {
 					output += 4;
 				}
 			}
 			output += ";";
 
 			/**
 			 * Um das Ende einer Array Dimension zu deklarieren, wird ein
 			 * Semikolon eingefuegt. Siehe auch Levelimporter in LevelGen.java
 			 */
 
 		}
 		return output;
 	}
 
 	/**
 	 * Konvertiert die Buttons des LevelEditors in 1 und 0 fuer W, B und F
 	 * 
 	 * @return Gibt ein int Array zurueck in Form von 1 und 0.
 	 */
 	public int[][] convertButtonsToMap() {
 		int[][] ergebnis = new int[feldx][feldy];
 
 		for (int i = 0; i < feldx; i++) {
 			for (int j = 0; j < feldy; j++) {
 
 				if ((button[j][i].getText() == "F")
 						|| (button[j][i].getText() == "B")) {
 					ergebnis[i][j] = 1;
 				} else {
 					ergebnis[i][j] = 0;
 				}
 			}
 		}
 
 		return ergebnis;
 	}
 
 	/**
 	 * Eine Funktion, um die Levelstruktur in eine *.bml-Datei zu schreiben.
 	 * Initialisiert einen BufferedOutPutStream(FileOutPutStream(Pfad)) und
 	 * verwendet .store aus der Properties-Lib, um den Dateiinhalt zu schreiben.
 	 * 
 	 * @param filepath
 	 *            Totaler Pfad zur Datei in die geschrieben werden soll.
 	 * @param ToBeSaved
 	 *            Property-Objekt, dass geschrieben/gespeichert werden soll.
 	 * @return Gibt true bei erfolgreichem Export oder false bei nicht
 	 *         erfolgreichem Export zurueck.
 	 */
 	public boolean exportlvl(String filepath, Properties ToBeSaved) {
 		String manual = "\nInformationen:\n0 ist ein begehbarer Weg\n1 ist ein unzerst�rbarer Block\n2 ist ein zerst�rbarer Block\n3 ist ein Ausgang\n4 ist ein Versteck";
 		if (filepath != null) {
 			try {
 				BufferedOutputStream bos = new BufferedOutputStream( // BOS
 																		// initialisieren
 						new FileOutputStream(filepath)); // FOS initialisieren
 
 				ToBeSaved.store(bos, "Exported BomberMan-Level" + manual);
 
 				/**
 				 * .store ist eine Properties Methode und uebergibt die Wertde
 				 * an den OutPutStream Datei/Stream wird geschlossen Wenn
 				 * erfolgreich, dann wird true zurueckgegeben
 				 */
 
 				bos.close();
 				return true;
 			} catch (IOException eIO) {
 				System.out.println("Fehler:" + eIO.getMessage());
 				return false; // Bei IOException Konsolenausgabe + false
 								// zur�ck.
 			}
 		} else {
 			System.out.println("Keine Zieldatei ausgew\u00e4hlt!");
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param Levelpath
 	 * @return false/true Damit die Datei nicht immer neu geoeffnet werden muss
 	 *         Reihenfolge der Buchstabenkombinationen V, E, Z, W wird
 	 *         festgelegt
 	 */
 
 	public boolean importlvl(String Levelpath) {
 		try {
 			String input = LevelGen.readFile(Levelpath, "LEVEL");
 			int i = 0;
 			int j = 0;
 
 			for (int k = 0; k < (input.length() - 1); k++) {
 
 				if (input.charAt(k) == ';') {
 					j++;
 					i = 0;
 				} else if (input.charAt(k) == '4') {
 					button[j][i].setText("V");
 					i++;
 				} else if (input.charAt(k) == '3') {
 					button[j][i].setText("E");
 					i++;
 				} else if (input.charAt(k) == '2') {
 					button[j][i].setText("Z");
 					i++;
 				} else if (input.charAt(k) == '1') {
 					button[j][i].setText("W");
 					i++;
 				} else {
 					button[j][i].setText("F");
 					i++;
 				}
 
 			}
 		} catch (IOException eIO) {
 			System.out.println(eIO.getMessage());
 			return false;
 		} catch (NumberFormatException eNFE) {
 			System.out.println(eNFE.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Startmenue wird festgelegt
 	 */
 
 	@Override
 	public int[] getStartposition(int spielernummer) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }

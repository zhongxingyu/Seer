 package Standard;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import parser.RasterParser;
 import parser.XMLParser;
 import MapEditor.MapEditor;
 
 public class Fenster implements ActionListener {
 	public JFrame f;
 	private JFrame fc;
 	private JPanel p;
 	Clip clip;
 	private boolean play;
 	private boolean stop;
 	/** Erstellung eines Spielfeldes sp */
 	private Spielfeld sp;
 	private int width;
 	private int height;
 	private int columns;
 	private int length;
 	private int mode;
 
 	JLabel scoreLabel;
 	JLabel scoreLabel2;
 	private int highscore1;
 	private int highscore2;
 	private int highscore3;
 	private String hsname1;
 	private String hsname2;
 	private String hsname3;
 
 	/**
 	 * Fenster erstellen
 	 * 
 	 * @param width
 	 * @param height
 	 * @param col
 	 * @param blockLength
 	 */
 	public Fenster(int width, int height, int col, int blockLength) {
 		this.width = width;
 		this.height = height;
 		columns = col;
 		length = blockLength;
 		play = true;
 		stop = false;
 		sp = new Spielfeld(width, height, columns, length, false, Main.raster);
 		mode = 1;
 		try {
 			AudioInputStream audioInputStream = AudioSystem
 					.getAudioInputStream(new File("sounds/melodie.wav"));
 			BufferedInputStream bufferedInputStream = new BufferedInputStream(
 					audioInputStream);
 			AudioFormat af = audioInputStream.getFormat();
 			int size = (int) (af.getFrameSize() * audioInputStream
 					.getFrameLength());
 			byte[] audio = new byte[size];
 			DataLine.Info info = new DataLine.Info(Clip.class, af, size);
 			bufferedInputStream.read(audio, 0, size);
 			clip = (Clip) AudioSystem.getLine(info);
 			clip.open(af, audio, 0, size);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		initFrame();
 	}
 
 	/** Frame und Panel erstellen */
 	private void initFrame() {
 		f = new JFrame("Bomberman");
 		f.setResizable(false);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setSize(width, height + length);
 
 		p = new JPanel();
 		f.setContentPane(p);
 		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 		p.add(sp);
 		f.addKeyListener(sp);
 
 		p.setSize(width, height);
 		initMenue();
 		f.setVisible(true);
 		playSound();
 	}
 
 	private void playSound() {
 		if (play)
 			clip.loop(Clip.LOOP_CONTINUOUSLY);
 		else
 			clip.stop();
 	}
 
 	/** Menueleiste */
 	private void initMenue() {
 		// MENUEBAR
 		JMenuBar menueLeiste = new JMenuBar();
 		JMenu menue = new JMenu("Optionen");
 
 		JMenuItem speichern = new JMenuItem("Speichern");
 		speichern.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				// SPEICHERN
 				OurJFileChooser jfc = new OurJFileChooser("maps/");
 				int status = jfc.showSaveDialog(null);
 				if (status == JFileChooser.APPROVE_OPTION) {
 					XMLParser xml = new XMLParser(jfc.getSelectedFile()
 							.toString());
 					RasterParser rp = new RasterParser();
 					xml.writeXML(rp.untransform(sp.raster, columns, length),
 							columns);
 				} else
 					System.out.println("Dateiauswahl abgebrochen");
 			}
 		});
 		JMenuItem speichernMP = new JMenuItem("Speichern");
 		speichernMP.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				// SPEICHERN
 				OurJFileChooser jfc = new OurJFileChooser("mapsMP/");
 				int status = jfc.showSaveDialog(null);
 				if (status == JFileChooser.APPROVE_OPTION) {
 					XMLParser xml = new XMLParser(jfc.getSelectedFile()
 							.toString());
 					RasterParser rp = new RasterParser();
 					xml.writeXML(rp.untransform(sp.raster, columns, length),
 							columns);
 				} else
 					System.out.println("Dateiauswahl abgebrochen");
 			}
 		});
 		JMenuItem laden = new JMenuItem("Laden");
 		laden.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				// LADEN
 				OurJFileChooser jfc = new OurJFileChooser("maps/");
 				int status = jfc.showOpenDialog(null);
 				if (status == JFileChooser.APPROVE_OPTION) {
 					XMLParser xml = new XMLParser(jfc.getSelectedFile()
 							.toString());
 					RasterParser rp = new RasterParser();
 					Main.raster = rp.transform(xml.readXML(columns), columns,
 							length);
 					System.out.println("Laden");
 					f.dispose();
 					restart(Main.f.mode, true);
 				} else
 					System.out.println("Dateiauswahl abgebrochen");
 
 			}
 		});
 		JMenuItem ladenMP = new JMenuItem("Laden");
 		ladenMP.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				// LADEN
 				OurJFileChooser jfc = new OurJFileChooser("mapsMP/");
 				int status = jfc.showOpenDialog(null);
 				if (status == JFileChooser.APPROVE_OPTION) {
 					XMLParser xml = new XMLParser(jfc.getSelectedFile()
 							.toString());
 					RasterParser rp = new RasterParser();
 					Main.raster = rp.transform(xml.readXML(columns), columns,
 							length);
 					System.out.println("Laden");
 					f.dispose();
 					restart(Main.f.mode, true);
 				} else
 					System.out.println("Dateiauswahl abgebrochen");
 
 			}
 		});
 		JMenuItem beenden = new JMenuItem("Beenden");
 		beenden.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				saveHighscore();
 				System.exit(0);
 			}
 		});
 
 		JMenuItem highscore = new JMenuItem("Highscore");
 		highscore.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				fc = new JFrame("Highscore");
 				fc.setLocation((f.getWidth() - fc.getSize().width) / 2,
 						(f.getHeight() - fc.getSize().height) / 2);
 				fc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				fc.setSize(300, 200);
 				JPanel panel = new JPanel();
 				JLabel namen = new JLabel("<html><body>Highscore:<br>1. "
 						+ hsname1 + " " + highscore1 + "<br>2. " + hsname2
 						+ " " + highscore2 + "<br>3. " + hsname3 + " "
 						+ highscore3 + "</body></html>");
 				panel.add(namen);
 				JButton close = new JButton("Schließen");
 				close.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(
 							java.awt.event.ActionEvent object) {
 						fc.dispose();
 					}
 				});
				panel.add(close);
 				fc.add(panel);
 				fc.setVisible(true);
 			}
 		});
 
 		JMenuItem start = new JMenuItem("Neues Spiel");
 		start.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				f.dispose();
 				restart(1, false);
 				if (sp.bm2 != null) {
 					sp.bm2.deleteScore();
 				}
 				sp.bm.deleteScore();
 			}
 		});
 
 		JMenuItem tutorial = new JMenuItem("Tutorial Level");
 		tutorial.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				Tutorial tut = new Tutorial(sp);
 				f.addKeyListener(tut);
 				tut.run();
 			}
 		});
 
 		JMenuItem sound = new JMenuItem("Musik An/Aus");
 		sound.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				if (play) {
 					play = false;
 					playSound();
 				} else {
 					play = true;
 					playSound();
 				}
 			}
 		});
 		JMenuItem credits = new JMenuItem("Credits");
 		credits.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				fc = new JFrame("Credits");
 				fc.setLocation((f.getWidth() - fc.getSize().width) / 2,
 						(f.getHeight() - fc.getSize().height) / 2);
 				fc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				fc.setSize(300, 200);
 				JPanel panel = new JPanel();
 				JLabel namen = new JLabel(
 						"<html><body><br>Dieses unglaublich gute Projekt<br> wurde geschrieben von:<br><br>Marie Arsoy<br>Moritz Kanzler<br>Tobias Küper<br>Sascha Kuhnke<br>Tim Fischer<br></body></html>");
 				panel.add(namen);
 				JButton close = new JButton("Schließen");
 				close.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(
 							java.awt.event.ActionEvent object) {
 						fc.dispose();
 					}
 				});
 				panel.add(close);
 				fc.add(panel);
 				fc.setVisible(true);
 			}
 		});
 		JMenu mp = new JMenu("2-Spieler-Modus");
 		JMenuItem mehrspieler;
 		if (this.mode == 2)
 			mehrspieler = new JMenuItem("Neues Spiel");
 		else
 			mehrspieler = new JMenuItem("Starten");
 		mehrspieler.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				f.dispose();
 				restart(2, true);
 			}
 		});
 		JMenuItem mehrspielerExit = new JMenuItem("Beenden");
 		mehrspielerExit.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				if (mode == 2) {
 					f.dispose();
 					restart(1, true);
 				}
 			}
 		});
 		JMenu editor = new JMenu("Editor");
 		JMenuItem editstart = new JMenuItem("Einzelspieler");
 		editstart.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				MapEditor me = new MapEditor(1);
 				me.setVisible(true);
 			}
 		});
 		JMenuItem editstartMP = new JMenuItem("Mehrspieler");
 		editstartMP.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent object) {
 				MapEditor me = new MapEditor(2);
 				me.setVisible(true);
 			}
 		});
 
 		scoreLabel = new JLabel();
 		scoreLabel2 = new JLabel();
 		updateScore();
 		scoreLabel.setBounds(0, 0, 300, 100);
 		scoreLabel.setVisible(true);
 		scoreLabel2.setBounds(0, 0, 300, 100);
 		scoreLabel2.setVisible(true);
 
 		f.add(scoreLabel);
 		f.add(scoreLabel2);
 
 		menueLeiste.add(menue);
 		menue.add(start);
 		menue.addSeparator();
 		if (this.mode == 1) {
 			menue.add(speichern);
 			menue.add(laden);
 		} else {
 			menue.add(speichernMP);
 			menue.add(ladenMP);
 		}
 		menue.addSeparator();
 		menue.add(tutorial);
 		menue.add(sound);
 		menue.add(credits);
 		menue.add(highscore);
 		menue.addSeparator();
 		menue.add(beenden);
 		menueLeiste.add(mp);
 		mp.add(mehrspieler);
 		mp.add(mehrspielerExit);
 		menueLeiste.add(editor);
 		editor.add(editstart);
 		editor.add(editstartMP);
 		f.setJMenuBar(menueLeiste);
 	}
 
 	/** getter and setter */
 	public int width() {
 		return width;
 	}
 
 	public void width(int width) {
 		this.width = width;
 	}
 
 	public int height() {
 		return height;
 	}
 
 	public void height(int height) {
 		this.height = height;
 	}
 
 	/**
 	 * Neustarten des Spieles
 	 * 
 	 * @param mode
 	 */
 	public void getHighscore() {
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(
 					"highscore/highscore.txt"));
 			String zeile = null;
 			zeile = input.readLine();
 			highscore1 = Integer.parseInt(zeile);
 			zeile = input.readLine();
 			highscore2 = Integer.parseInt(zeile);
 			zeile = input.readLine();
 			highscore3 = Integer.parseInt(zeile);
 			zeile = input.readLine();
 			hsname1 = zeile;
 			zeile = input.readLine();
 			hsname2 = zeile;
 			zeile = input.readLine();
 			hsname3 = zeile;
 			input.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveHighscore() {
 		try {
 			BufferedWriter save = new BufferedWriter(new FileWriter(
 					"highscore/highscore.txt"));
 			String string = highscore1 + "";
 			save.write(string);
 			save.newLine();
 			string = highscore2 + "";
 			save.write(string);
 			save.newLine();
 			string = highscore3 + "";
 			save.write(string);
 			save.newLine();
 			save.write(hsname1);
 			save.newLine();
 			save.write(hsname2);
 			save.newLine();
 			save.write(hsname3);
 			save.flush();
 			save.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getHighscoreName() {
 		String name = JOptionPane
 				.showInputDialog(
 						null,
 						"<html><body>Herzlichen Glückwunsch!<br>Du bist einer der Besten Drei in diesem Spiel!<br>Bitte gebe deinen Namen ein.</body></html>");
 		return name;
 	}
 
 	public void restart(int mode, boolean resetScore) {
 		int tmp = 0, tmp2 = 0;
 		if (sp.bm2 != null) {
 			tmp2 = sp.bm2.getScore();
 		}
 		tmp = sp.bm.getScore();
 		String name = "";
 		if (tmp > highscore1) {
 			highscore3 = highscore2;
 			highscore2 = highscore1;
 			highscore1 = tmp;
 			hsname3 = hsname2;
 			hsname2 = hsname1;
 			name = getHighscoreName();
 			hsname1 = name;
 		} else if (tmp > highscore2) {
 			highscore3 = highscore2;
 			highscore2 = tmp;
 			hsname3 = hsname2;
 			name = getHighscoreName();
 			hsname2 = name;
 		} else if (tmp > highscore3) {
 			highscore3 = tmp;
 			name = getHighscoreName();
 			hsname3 = name;
 		}
 		if (sp.bm2 != null) {
 			tmp2 = sp.bm2.getScore();
 			if (tmp2 > highscore1) {
 				highscore3 = highscore2;
 				highscore2 = highscore1;
 				highscore1 = tmp2;
 			} else if (tmp2 > highscore2) {
 				highscore3 = highscore2;
 				highscore2 = tmp2;
 			} else if (tmp2 > highscore3)
 				highscore3 = tmp2;
 		}
 
 		this.mode = mode;
 		sp.bm.removeAllBombsFromList();
 		stop = true;
 		clip.stop();
 		tmp = 0;
 		tmp2 = 0;
 		if (!resetScore) {
 			if (sp.bm2 != null) {
 				tmp2 = sp.bm2.getScore();
 			}
 			tmp = sp.bm.getScore();
 		}
 		if (mode == 2) {
 			sp = new Spielfeld(width, height, columns, length, true,
 					Main.raster);
 			sp.bm2.removeAllBombsFromList();
 			stop = false;
 		} else {
 			sp = new Spielfeld(width, height, columns, length, false,
 					Main.raster);
 			stop = false;
 		}
 		sp.bm.addScore(tmp);
 		if (sp.bm2 != null)
 			sp.bm2.addScore(tmp2);
 		initFrame();
 	}
 
 	public void updateScore() {
 		if (sp.bm2 == null)
 			scoreLabel.setText("Du hast:" + sp.bm.getScore() + " Punkte\n");
 		else {
 			scoreLabel.setText("Spieler 1 hat:" + sp.bm.getScore() + " Punkte");
 			scoreLabel2.setText("Spieler 2 hat:" + sp.bm2.getScore()
 					+ " Punkte");
 		}
 	}
 
 	/**
 	 * Verstecken des alten Fensters
 	 * 
 	 * @param messageDialog
 	 * @param renew
 	 */
 	public void dispose(String messageDialog, boolean renew) {
 		if (renew) {
 			int answer = JOptionPane.showConfirmDialog(f, messageDialog
 					+ " Möchtest du ein neues Spiel starten?", "Game Over",
 					JOptionPane.YES_NO_OPTION);
 			if (answer == JOptionPane.YES_OPTION) {
 				stop = true;
 				clip.stop();
 
 				f.dispose();
 			} else {
 				System.exit(0);
 			}
 		}
 	}
 
 	/**
 	 * Aktionen des ActionListeners
 	 * 
 	 * @param e
 	 */
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 }

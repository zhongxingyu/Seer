 package Editor;
 
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import main.GlobalGraphics;
 import main.Sound;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class GLevelEditor extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	static int xsize = 0;
 	static int ysize = 0;
 	static JPanel EditorPanel;
 
 	static JButton SinglePlayer = new JButton("SinglePlayer");
 	static JButton MultiPlayer = new JButton("MultiPlayer");
 	static JButton editLevel = new JButton("EditLevel");
 	static JButton setExit = new JButton("setExit");
 	static JButton enemy = new JButton("MicroCanonicalBear");
 	static JButton save = new JButton("SaveLevel");
 	static JButton randomLevel = new JButton("RandomLevel");
 	static JButton stonegrid = new JButton("addStoneGrid");
 	static JSpinner XSpinner;
 	static JSpinner YSpinner;
 	JTextField namefield;
 	JLabel labelx;
 	JLabel labely;
 	JLabel labelname;
 	JLabel EditOrSetExit;
 	int Modus = 0;
 	// 0: EditLevel
 	// 1: setExit
 	// 2: Brchen
 	boolean exitExistent = false;
 	boolean stoneGridOn = false;
 	int exitPosition[] = new int[2];
 	int spawnpoints[][];
 	Container cp = this.getContentPane();
 	static boolean Singleplayer = true;
 	public static Level level;
 	String levelname;
 	int frameSizeX = GlobalGraphics.panelSizeX;
 	int frameSizeY = GlobalGraphics.panelSizeY;
 
 	public static ArrayList<int[]> bearlist = new ArrayList<int[]>();
 
 	public void intitialize() {
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// this.pack();
 		// this.setSize(new Dimension(frameSizeX, frameSizeY));
 
 		GLevelEditor.EditorPanel = new JPanel() {
 
 			private static final long serialVersionUID = 1L;
 
 			public void paintComponent(Graphics g) {
 				g.drawImage(Global.mauersolid, 0, 0, (xsize + 1)
 						* Global.sqsize, (ysize + 1) * Global.sqsize,
 						EditorPanel);
 
 				level.drawComponent(g, EditorPanel);
 				for (int i = 0; i < bearlist.size(); i++)
 					g.drawImage(Global.seeker, bearlist.get(i)[0]
 							* Global.sqsize,
 							bearlist.get(i)[1] * Global.sqsize, Global.sqsize,
 							Global.sqsize, EditorPanel);
 
 			}
 		};
 		EditorPanel.setVisible(false);
 		setExit.setVisible(false);
 		stonegrid.setVisible(false);
 		editLevel.setVisible(false);
 		save.setVisible(false);
 		enemy.setVisible(false);
 		randomLevel.setVisible(false);
 		// SinglePlayerButton
 		GridBagConstraints spc = new GridBagConstraints();
 		spc.gridx = 0;
 		spc.gridy = 3;
 		spc.gridwidth = 1;
 		spc.fill = GridBagConstraints.HORIZONTAL;
 		spc.weightx = 1.0;
 		cp.setLayout(new GridBagLayout());
 		cp.add(SinglePlayer, spc);
 		SinglePlayer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GLevelEditor.Singleplayer = true;
 				MultiPlayer.setVisible(false);
 				SinglePlayer.setVisible(false);
 				XSpinner.setVisible(false);
 				YSpinner.setVisible(false);
 				labely.setVisible(false);
 				labelx.setVisible(false);
 				labelname.setVisible(false);
 				namefield.setVisible(false);
 
 				levelname = namefield.getText();
 				xsize = Integer.valueOf(XSpinner.getValue().toString());
 				ysize = Integer.valueOf(YSpinner.getValue().toString());
 				EditorPanel.setSize(xsize * Global.sqsize, ysize
 						* Global.sqsize);
 
 				EditorPanel.setVisible(true);
 				setExit.setVisible(true);
 				editLevel.setVisible(true);
 				EditOrSetExit.setVisible(true);
 				createEmptyLevel();
 				save.setVisible(true);
 				stonegrid.setVisible(true);
 				enemy.setVisible(true);
 				randomLevel.setVisible(true);
 				resizeFrame();
 
 			}
 
 		});
 
 		// MultiplayerButton
 		GridBagConstraints mpc = new GridBagConstraints();
 		mpc.gridx = 1;
 		mpc.gridy = 3;
 		// mpc.gridwidth = 2;
 		mpc.fill = GridBagConstraints.HORIZONTAL;
 		mpc.weightx = 1.0;
 		// cp.setLayout(new GridBagLayout());
 		cp.add(MultiPlayer, mpc);
 		MultiPlayer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				GLevelEditor.Singleplayer = false;
 				MultiPlayer.setVisible(false);
 				SinglePlayer.setVisible(false);
 				XSpinner.setVisible(false);
 				YSpinner.setVisible(false);
 				labely.setVisible(false);
 				labelx.setVisible(false);
 				labelname.setVisible(false);
 				namefield.setVisible(false);
 
 				levelname = namefield.getText();
 				xsize = Integer.valueOf(XSpinner.getValue().toString());
 				ysize = Integer.valueOf(YSpinner.getValue().toString());
 
 				EditorPanel.setSize(xsize * Global.sqsize, xsize
 						* Global.sqsize);
 				EditorPanel.repaint();
 				EditorPanel.setVisible(true);
 				save.setVisible(true);
 				stonegrid.setVisible(true);
 				randomLevel.setVisible(true);
 
 				createEmptyLevel();
 
 				resizeFrame();
 
 			}
 		});
 
 		// setExit Button
 		GridBagConstraints se = new GridBagConstraints();
 		se.gridx = 1;
 		se.gridy = 1;
 		se.gridwidth = 1;
 		se.fill = GridBagConstraints.HORIZONTAL;
 		se.weightx = 1.0;
 		// cp.setLayout(new GridBagLayout());
 		cp.add(setExit, se);
 		setExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				EditOrSetExit.setText("SetExit");
 				Modus = 1;
 
 			}
 		}
 
 		);
 
 		// EditLevelButton
 		GridBagConstraints el = new GridBagConstraints();
 		el.gridx = 0;
 		el.gridy = 1;
 		el.gridwidth = 1;
 		el.fill = GridBagConstraints.HORIZONTAL;
 		el.weightx = 1.0;
 		// cp.setLayout(new GridBagLayout());
 		cp.add(editLevel, el);
 		editLevel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				EditOrSetExit.setText("EditLevel");
 				Modus = 0;
 
 			}
 		}
 
 		);
 		// BR Button
 		GridBagConstraints bb = new GridBagConstraints();
 		bb.gridx = 1;
 		bb.gridy = 2;
 		bb.gridwidth = 1;
 		bb.fill = GridBagConstraints.HORIZONTAL;
 		// sl.weightx = 1.0;
 		// cp1.setLayout(new GridBagLayout());
 		cp.add(enemy, bb);
 		enemy.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				EditOrSetExit.setText("SetBear");
 				Modus = 2;
 			}
 		});
 
 		// saveLevel Button
 		GridBagConstraints sl = new GridBagConstraints();
 		sl.gridx = 0;
 		sl.gridy = 3;
 		sl.gridwidth = 2;
 		sl.fill = GridBagConstraints.HORIZONTAL;
 		// sl.weightx = 1.0;
 		// cp1.setLayout(new GridBagLayout());
 		cp.add(save, sl);
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// Konsistenzprfung
 				ExitPathfinder pathfinder = new ExitPathfinder();
 						
 					boolean exitErreichbar = pathfinder.find(0, 0,
 							exitPosition[0], exitPosition[1]);
 				
 					boolean GegnerErreichbar = pathfinder.find(0, 0, xsize - 1,
 							ysize - 1);
 				
 				if (exitErreichbar | (!Singleplayer && GegnerErreichbar)) {
 					// Speichern
 					if (Singleplayer && exitExistent) {
 						try {
 							saveLevel();
 						} catch (ParserConfigurationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TransformerException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						System.exit(0);
 					}
 					if (Singleplayer && !exitExistent) {
 						new Sound("src/sounds/pleaseOpen.wav", 2000).start();
 					}
 					if (!Singleplayer) {
 						try {
 							saveLevel();
 						} catch (ParserConfigurationException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TransformerException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						if (!Singleplayer && !GegnerErreichbar) {
 							new Sound("src/sounds/pleaseOpen.wav", 2000)
 									.start();
 						}
 						System.exit(0);
 					}
 				} else {
 					new Sound("src/sounds/pleaseOpen.wav", 2000).start();
 					EditOrSetExit.setText("Exit blocked");
 				}
 				if (!Singleplayer && !GegnerErreichbar) {
 					new Sound("src/sounds/pleaseOpen.wav", 2000).start();
 				}
 
 			}
 		});
 		// randomLevel Button
 		GridBagConstraints rl = new GridBagConstraints();
 		rl.gridx = 0;
 		rl.gridy = 4;
 		rl.gridwidth = 2;
 		rl.fill = GridBagConstraints.HORIZONTAL;
 		// sl.weightx = 1.0;
 		// cp1.setLayout(new GridBagLayout());
 		cp.add(randomLevel, rl);
 		randomLevel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 
 				ExitPathfinder pathfinder = new ExitPathfinder();
 				boolean exitErreichbar = false;
 				boolean GegnerErreichbar = false;
 
 				Modus = 0;
 				EditOrSetExit.setText("EditLevel");
 				Exit exit = new Exit();
 				if (Singleplayer) {
 					while (!exitErreichbar) {
 						createEmptyLevel();
 						for (int x = xsize - 1; x >= 0; x--) {
 							for (int y = ysize - 1; y >= 0; y--) {
 								int imax = RandomNumber(0, 3);
 								for (int i = 0; i < imax; i++)
 									changeField(x, y);
 								EditorPanel.repaint();
 							}
 
 							EditorPanel.repaint();
 						}
 						exitExistent = false;
 						int xrand = RandomNumber((int) xsize / 2, xsize);
 						int yrand = RandomNumber((int) ysize / 2, ysize);
 						if (level.getField(xrand, yrand) instanceof Floor) {
 							level.setField(xrand, yrand, exit);
 							EditorPanel.repaint();
 							exitExistent = true;
 							exitPosition[0] = xrand;
 							exitPosition[1] = yrand;
 						}
 						if (level.getField(xrand, yrand) instanceof Earth) {
 							level.getField(xrand, yrand).setTransformto(exit);
 							EditorPanel.repaint();
 							exitExistent = true;
 							exitPosition[0] = xrand;
 							exitPosition[1] = yrand;
 
 						}
 						if (exitExistent)
 							exitErreichbar = pathfinder.find(0, 0,
 									exitPosition[0], exitPosition[1]);
 						else
 							exitErreichbar = false;
 					}
 				} else {
 
 					while (!GegnerErreichbar) {
 						createEmptyLevel();
 						for (int x = xsize - 1; x >= 0; x--) {
 							for (int y = ysize - 1; y >= 0; y--) {
 								int imax = RandomNumber(0, 3);
 								for (int i = 0; i < imax; i++)
 									changeField(x, y);
 								EditorPanel.repaint();
 							}
 						}
 						EditorPanel.repaint();
 						GegnerErreichbar = pathfinder.find(0, 0, xsize - 1,
 								ysize - 1);
 					}
 
 				}
 
 			}
 		});
 		GridBagConstraints sg = new GridBagConstraints();
 		sg.gridx = 0;
 		sg.gridy = 2;
 		sg.gridwidth = 1;
 		sg.fill = GridBagConstraints.HORIZONTAL;
 		// sl.weightx = 1.0;
 		// cp1.setLayout(new GridBagLayout());
 		cp.add(stonegrid, sg);
 		stonegrid.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				createStoneGrid();
 			}
 		});
 
 		// Spinner for XSize
 		SpinnerModel modelx = new SpinnerNumberModel(9, 3, 20, 1);
 		XSpinner = new JSpinner(modelx);
 		GridBagConstraints spinnerx = new GridBagConstraints();
 		spinnerx.gridx = 0;
 		spinnerx.gridy = 2;
 		// spc.gridwidth = 2;
 		spinnerx.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weightx = 1.0;
 		cp.add(XSpinner, spinnerx);
 		XSpinner.setVisible(true);
 
 		labelx = new JLabel("X-Size");
 		GridBagConstraints lx = new GridBagConstraints();
 		lx.gridx = 1;
 		lx.gridy = 1;
 		// spc.gridwidth = 2;
 		lx.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weightx = 1.0;
 		cp.add(labelx, lx);
 		labelx.setVisible(true);
 
 		// Spinner for Ysize
 		SpinnerModel modely = new SpinnerNumberModel(9, 3, 20, 1);
 		YSpinner = new JSpinner(modely);
 		GridBagConstraints spinnery = new GridBagConstraints();
 		spinnery.gridx = 1;
 		spinnery.gridy = 2;
 		// spc.gridwidth = 2;
 		spinnery.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weighty = 1.0;
 		cp.add(YSpinner, spinnery);
 		YSpinner.setVisible(true);
 
 		labely = new JLabel("Y-Size");
 		GridBagConstraints ly = new GridBagConstraints();
 		ly.gridx = 0;
 		ly.gridy = 1;
 		// spc.gridwidth = 2;
 		ly.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weightx = 1.0;
 		cp.add(labely, ly);
 		labely.setVisible(true);
 
 		// Textfield for Level name
 		namefield = new JTextField("Level0");
 		GridBagConstraints nf = new GridBagConstraints();
 		nf.gridx = 1;
 		nf.gridy = 0;
 		// spc.gridwidth = 2;
 		nf.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weighty = 1.0;
 		cp.add(namefield, nf);
 		namefield.setVisible(true);
 
 		labelname = new JLabel("   Level name : ");
 		GridBagConstraints ln = new GridBagConstraints();
 		ln.gridx = 0;
 		ln.gridy = 0;
 		// spc.gridwidth = 2;
 		ln.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weightx = 1.0;
 		cp.add(labelname, ln);
 		labelname.setVisible(true);
 
 		EditOrSetExit = new JLabel("EditLevel");
 		GridBagConstraints EoE = new GridBagConstraints();
 		EoE.gridx = 0;
 		EoE.gridy = 0;
 		// spc.gridwidth = 2;
 		EoE.fill = GridBagConstraints.HORIZONTAL;
 		// spinner.weightx = 1.0;
 		cp.add(EditOrSetExit, EoE);
 		EditOrSetExit.setVisible(false);
 
 		// Panel
 		GridBagConstraints pa = new GridBagConstraints();
 		pa.fill = GridBagConstraints.BOTH;
 		pa.gridx = 0;
 		pa.gridy = 5;
 		pa.gridwidth = 2;
 		pa.gridheight = 4;
 		pa.weightx = 1.0;
 		pa.weighty = 4.0;
 		// cp.setLayout(new GridBagLayout());
 		cp.add(EditorPanel, pa);
 
 		// MouseListener
 		GLevelEditor.EditorPanel.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {
 				int mousefield[] = new int[2];
 				mousefield[0] = (int) (e.getX() / Global.sqsize);
 				mousefield[1] = (int) (e.getY() / Global.sqsize);
 				if (0 <= mousefield[0] && mousefield[0] <= xsize - 1
 						&& 0 <= mousefield[1] && mousefield[1] <= ysize - 1
 						&& (Modus == 0 || Modus == 1))
 					changeField(mousefield[0], mousefield[1]);
 				if (Modus == 2) {
 					setBear(mousefield[0], mousefield[1]);
 				}
 
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 		this.setVisible(true);
 		resizeFrame();
 
 	}
 
 	private void setBear(int x, int y) {
 		if (level.getField(x, y) instanceof Floor) {
 			int[] coordinates = { x, y };
 			bearlist.add(coordinates);
 			EditorPanel.repaint();
 		}
 
 	}
 
 	public void resizeFrame() {
 		if (xsize != 0)
 			this.setSize((xsize + 1) * Global.sqsize, (ysize + 1)
 					* Global.sqsize + 160);
 		else
 			this.pack();
 	}
 
 	public void createEmptyLevel() {
 		if (Singleplayer) {
 			spawnpoints = new int[1][2];
 			spawnpoints[0][0] = 0;
 			spawnpoints[0][1] = 0;
 		} else {
 			spawnpoints = new int[2][2];
 			spawnpoints[0][0] = 0;
 			spawnpoints[0][1] = 0;
 			spawnpoints[1][0] = xsize - 1;
 			spawnpoints[1][1] = ysize - 1;
 		}
 
 		level = new Level(xsize, ysize, spawnpoints);
 		Field floor = new Floor(); // Boden
 		// Erstmal alles auf Floor setzen
 		for (int x = 0; x < xsize; x++) {
 			for (int y = 0; y < ysize; y++) {
 				level.setField(x, y, floor);
 			}
 		}
 
 	}
 
 	public void createStoneGrid() {
 		new Sound("src/sounds/plobb.wav", 200).start();
 		Field floor = new Floor(); // Boden
 		Field stone = new Stone(); // Unzerstrbarer Block
 
 		createEmptyLevel();
 		bearlist.clear();
 		EditorPanel.repaint();
 		exitExistent = false;
 		if (!stoneGridOn) {
 
 			for (int y = 1; y < ysize; y = y + 2) {
 				for (int x = 1; x < xsize; x = x + 2) {
 					level.setField(x, y, stone);
 					EditorPanel.validate();
 				}
 			}
 			level.setField(spawnpoints[0][0], spawnpoints[0][1], floor);
 			EditorPanel.repaint();
 			if (!Singleplayer) {
 				level.setField(spawnpoints[1][0], spawnpoints[1][1], floor);
 				EditorPanel.repaint();
 			}
 			stoneGridOn = true;
 		} else {
 			stoneGridOn = false;
 		}
 
 	}
 
 	private void changeField(int x, int y) {
 		Field floor = new Floor(); // Boden
 		Field stone = new Stone(); // Unzerstrbarer Block
 		Earth earth = new Earth(); // Zerstrbarer Block
 		Field exit = new Exit();
 		boolean besetzt;
 
 		if ((x == 0 && y == 0) || (x == 1 && y == 0) || (x == 0 && y == 1))
 			besetzt = true;
 		else
 			besetzt = false;
 		if (!Singleplayer) {
 			if ((x == xsize - 1 && y == ysize - 1)
 					|| (x == xsize - 2 && y == ysize - 1)
 					|| (x == xsize - 1 && y == ysize - 2))
 				besetzt = true;
 		}
 		for (int i = 0; i < bearlist.size(); i++) {
 			if ((bearlist.get(i)[0] == x && bearlist.get(i)[1] == y)) {
 				besetzt = true;
 			}
 		}
 		if (!besetzt) {
 			if (Modus == 0) {
 				new Sound("src/sounds/plobb.wav", 100).start();
 				if (level.getField(x, y).isTransformable() == null) {
 					if (!level.getField(x, y).isExit()) {
 						if (!level.getField(x, y).isSolid()) {
 							level.setField(x, y, earth);
 							EditorPanel.repaint();
 						} else {
 							level.setField(x, y, floor);
 							EditorPanel.repaint();
 						}
 					}
 				} else {
 					if (!level.getField(x, y).isTransformable().isExit()) {
 						level.setField(x, y, stone);
 						EditorPanel.repaint();
 					}
 				}
 
 			}
 			if (Modus == 1) {
 
 				if (!exitExistent) {
 
 					if (level.getField(x, y).isSolid()) {
 						if (level.getField(x, y).isTransformable() != null) {
 							level.getField(x, y).setTransformto(exit);
 							EditorPanel.repaint();
 							exitPosition[0] = x;
 							exitPosition[1] = y;
 							exitExistent = true;
 							new Sound("src/sounds/platsch.wav", 200).start();
 						}
 
 					} else {
 						level.setField(x, y, exit);
 						level.getField(x, y).markAsExit(true);
 						EditorPanel.repaint();
 						exitPosition[0] = x;
 						exitPosition[1] = y;
 						exitExistent = true;
 						new Sound("src/sounds/platsch.wav", 200).start();
 
 					}
 
 				} else {
 					if (level.getField(x, y).isExit()
 							|| (level.getField(x, y).isTransformable() != null && level
 									.getField(x, y).isTransformable().isExit())) {
 						level.setField(x, y, floor);
 						level.getField(x, y).markAsExit(false);
 						exitExistent = false;
 						new Sound("src/sounds/plingg.wav", 200).start();
 						EditorPanel.repaint();
 					}
 				}
 			}
 		}
 
 	}
 
 	public void saveLevel() throws ParserConfigurationException,
 			TransformerException {
 		// Create document
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder documentBuilder = documentBuilderFactory
 				.newDocumentBuilder();
 		Document document = documentBuilder.newDocument();
 		Element rootElement = document.createElement(levelname);
 		document.appendChild(rootElement);
 
 		rootElement.setAttribute("xsize", Integer.toString(xsize));
 		rootElement.setAttribute("ysize", Integer.toString(ysize));
 		// Lese Felder ein
 		Element fieldsElement = document.createElement("fields");
 		rootElement.appendChild(fieldsElement);
 		for (int x = 1; x <= xsize; x++) {
 			for (int y = 1; y <= ysize; y++) {
 				Element fieldElement = document.createElement("field");
 				fieldsElement.appendChild(fieldElement);
 				fieldElement.setAttribute("x", Integer.toString(x));
 				fieldElement.setAttribute("y", Integer.toString(y));
 				fieldElement.setAttribute("type", level.getField(x - 1, y - 1)
 						.getFieldtype());
 
 			}
 		}
 		// Ausgang
		Element exitsElement = document.createElement("exits");
		rootElement.appendChild(exitsElement);
		Element exitElement = document.createElement("exit");
		exitsElement.appendChild(exitElement);		
		if (Singleplayer) {			
 			exitElement
 					.setAttribute("x", Integer.toString(exitPosition[0] + 1));
 			exitElement
 					.setAttribute("y", Integer.toString(exitPosition[1] + 1));
		}else{exitElement
			.setAttribute("x", Integer.toString(1));
	exitElement
			.setAttribute("y", Integer.toString(1));}
 		// Spawnpoints
 		Element spawnpointsElement = document.createElement("spawnpoints");
 		rootElement.appendChild(spawnpointsElement);
 		Element spawnpointElement = document.createElement("spawnpoint");
 		spawnpointsElement.appendChild(spawnpointElement);
 		spawnpointElement.setAttribute("x",
 				Integer.toString(spawnpoints[0][0] + 1));
 		spawnpointElement.setAttribute("y",
 				Integer.toString(spawnpoints[0][1] + 1));
 		if (!Singleplayer) {
 			Element spawnpointElement2 = document.createElement("spawnpoint");
 			spawnpointsElement.appendChild(spawnpointElement2);
 			spawnpointElement2.setAttribute("x",
 					Integer.toString(spawnpoints[1][0] + 1));
 			spawnpointElement2.setAttribute("y",
 					Integer.toString(spawnpoints[1][1] + 1));
 		}
 
 		// Brchenspawnpoints speichern
 
 		Element spawnpointsbaerElement = document
 				.createElement("spawnpointsbaer");
 		rootElement.appendChild(spawnpointsbaerElement);
 
 		for (int i = 0; i < bearlist.size(); i++) {
 			Element spawnpointbaerElement = document
 					.createElement("spawnpoint");
 			spawnpointsbaerElement.appendChild(spawnpointbaerElement);
 			spawnpointbaerElement.setAttribute("x",
 					Integer.toString(bearlist.get(i)[0] + 1));
 			spawnpointbaerElement.setAttribute("y",
 					Integer.toString(bearlist.get(i)[1] + 1));
 		}
 
 		// speichern
 
 		TransformerFactory transformerFactory = TransformerFactory
 				.newInstance();
 		Transformer transformer = transformerFactory.newTransformer();
 		DOMSource source = new DOMSource(document);
 		StreamResult result = new StreamResult(new File("Levels/" + levelname
 				+ ".txt"));
 		transformer.transform(source, result);
 		System.out.println("gespeichert");
 	}
 
 	public static int RandomNumber(int low, int high) {
 		high++;
 		return (int) (Math.random() * (high - low) + low);
 	}
 
 	public static void main(String args[]) {
 
 		// new Sound("src/sounds/blabal.wav", true);
 
 		final GLevelEditor KLaus = new GLevelEditor();
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				KLaus.intitialize();
 			}
 		});
 
 	}
 }

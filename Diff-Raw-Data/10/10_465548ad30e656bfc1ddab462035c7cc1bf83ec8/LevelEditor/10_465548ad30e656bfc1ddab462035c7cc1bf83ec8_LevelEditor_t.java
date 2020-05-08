 package toritools.leveleditor;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import toritools.entity.Entity;
 import toritools.io.Importer;
 import toritools.map.ToriMapIO;
 import toritools.math.Vector2;
 import toritools.xml.ToriXML;
 
 /**
  * The core level editor. What a mess! :D
  * 
  * @author toriscope
  * 
  */
 public class LevelEditor {
 
 	/**
 	 * The current level file being edited.
 	 */
 	private File levelFile, workingDirectory;
 
 	/**
 	 * Map of int layers to list of existing entities.
 	 */
 	private List<Entity> entities = new ArrayList<Entity>();
 
 	/**
 	 * The current placeable entity.
 	 */
 	private Entity current = null;
 
 	/**
 	 * The currently selected entity.
 	 */
 	private Entity selected = null;
 
 	/**
 	 * The panel where the entity selection buttons are added and removed.
 	 */
 	private JPanel buttonPanel = new JPanel();
 
 	/**
 	 * The root frame.
 	 */
 	private JFrame frame = new JFrame("ToriEditor");
 
 	/**************
 	 * EDITOR VARIABLES
 	 **************/
 
 	/**
 	 * Size of the grid. Usually square for now.
 	 */
 	private Dimension gridSize = new Dimension(32, 32);
 
 	/**
 	 * Size of the editing world.
 	 */
 	private Dimension levelSize = new Dimension(1000, 1000);
 
 	/**
 	 * This object handles layering information.
 	 */
 	private LayerEditor layerEditor = new LayerEditor(this);
 
 	/**
 	 * This object handles instance variables.
 	 */
 	private VariableEditor varEditor = new VariableEditor(this);
 
 	/**
 	 * The text of this is where you can set some neat data to display to the
 	 * user.
 	 */
 	private JLabel fileLabel = new JLabel("FILE LABEL");
 	private JLabel gridLabel = new JLabel("GRID LABEL");
 	private JLabel levelSizeLabel = new JLabel("WORLD SIZE LABEL");
 	private JLabel editModeLabel = new JLabel("EDITMODE");
 
 	private Vector2 wallStart, wallEnd;
 	private boolean makeWall = false, makingWall = false;
 
 	/**
 	 * Mouse controller.
 	 */
 	private MouseAdapter mouseAdapter = new MouseAdapter() {
 
 		@Override
 		public void mousePressed(MouseEvent m) {
 			if (makeWall) {
 				wallStart = wallEnd = getClosestGridPoint(new Vector2(
 						m.getPoint()));
 				makingWall = true;
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent m) {
 			if (makeWall && makingWall) {
 				wallEnd = getClosestGridPoint(new Vector2(m.getPoint()));
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent m) {
 			if (makeWall && makingWall) {
 				wallEnd = getClosestGridPoint(new Vector2(m.getPoint()));
 				makeWall(wallStart, wallEnd.sub(wallStart));
 				makingWall = makeWall = false;
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			frame.requestFocus();
 			makeWall = makingWall = false;
 			if (arg0.getButton() == MouseEvent.BUTTON3) {
 				selectOverlapping(new Vector2(arg0.getPoint()));
 				varEditor.setEntity(selected);
 				repaint();
 			} else if (arg0.getButton() == MouseEvent.BUTTON1) {
 				System.err.println("Click");
 				if (current != null) {
 					Vector2 p = new Vector2(arg0.getPoint());
 					deleteOverlapping(p);
 					p.set(getClosestGridPoint(p));
 					Entity e = new Entity();
 					e.setFile(current.getFile());
 					e.pos = p.clone();
 					e.dim = current.dim.clone();
 					e.sprite = current.sprite;
 					e.layer = layerEditor.getCurrentLayer();
 					addEntity(e);
 				}
 			}
 			repaint();
 		}
 	};
 
 	/**
 	 * This JPanel is where the actual drawing take splace.
 	 */
 	@SuppressWarnings("serial")
 	private JPanel drawPanel = new JPanel() {
 		{
 			setPreferredSize(new Dimension(640, 480));
 			this.addMouseListener(mouseAdapter);
 			this.addMouseMotionListener(mouseAdapter);
 
 			this.setFocusable(true);
 		}
 
 		public void paintComponent(Graphics g) {
 			draw(g);
 		}
 	};
 
 	public LevelEditor() throws IOException, ParserConfigurationException,
 			TransformerException {
 		/*
 		 * LOAD THE CONFIG FILE.
 		 */try {
 			Node configNode = ToriXML.parse(new File("config.xml"))
 					.getElementsByTagName("config").item(0);
 			Node recentNode = configNode.getAttributes().getNamedItem("recent");
 			if (recentNode != null) {
 				File f;
 				if (!(f = new File(recentNode.getNodeValue())).exists())
 					throw new NullPointerException();
 				setLevelFile(f);
 
 			}
 		} catch (final Exception NullPointer) {
 			JOptionPane.showMessageDialog(null,
 					"There was an issue loading the recent level file!");
 			setLevelFile(importNewFileDialog());
 		}
 
 		setupGUI();
 		reloadLevel();
 
 		/*
 		 * Add the keyboard handler.
 		 */
 		frame.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_DELETE) {
 					deleteSelected();
 				}
 			}
 		});
 
 		repaint();
 	}
 
 	/**
 	 * Sets the working level file and the working directory.
 	 * 
 	 * @param file
 	 *            the level xml file.
 	 */
 	private void setLevelFile(final File file) {
 		this.levelFile = file;
 		this.workingDirectory = file.getParentFile();
 	}
 
 	/**
 	 * If a level.xml file exists, load the data from it. Uses levelFile.
 	 * 
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws TransformerException
 	 */
 	private void reloadLevel() throws IOException,
 			ParserConfigurationException, TransformerException {
 		// Create the essential level.xml file
 		clear();
 		if (levelFile.exists()) {
 			Document doc = ToriXML.parse(levelFile);
 			loadLevel(doc);
 		} else {
 			saveLevel();
 		}
 		saveConfig();
 	}
 
 	/**
 	 * Save the config file.
 	 * 
 	 * @throws ParserConfigurationException
 	 * @throws TransformerException
 	 */
 	private void saveConfig() throws ParserConfigurationException,
 			TransformerException {
 		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 		Document doc = docBuilder.newDocument();
 
 		// Create level element
 		Element editorElement = doc.createElement("editor");
 		doc.appendChild(editorElement);
 
 		Element configElement = doc.createElement("config");
 		editorElement.appendChild(configElement);
 
 		configElement.setAttribute("recent", levelFile.getPath());
 
 		ToriXML.saveXMLDoc(new File("config.xml"), doc);
 	}
 
 	/**
 	 * Create the GUI components and menu.
 	 */
 	private void setupGUI() {
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 		frame.setFocusable(true);
 
 		JPanel dummyPanel = new JPanel();
 		frame.add(new JScrollPane(drawPanel), BorderLayout.CENTER);
 		frame.add(new JScrollPane(buttonPanel), BorderLayout.EAST);
 		dummyPanel.setLayout(new BoxLayout(dummyPanel, BoxLayout.Y_AXIS));
 		dummyPanel.add(layerEditor);
 		dummyPanel.add(varEditor);
 		frame.add(dummyPanel, BorderLayout.WEST);
 
 		/*
 		 * Form the status bar
 		 */
 		dummyPanel = new JPanel();
 		dummyPanel.setBackground(Color.WHITE);
 		dummyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		dummyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		dummyPanel.add(fileLabel);
 		dummyPanel.add(new JLabel("|"));
 		dummyPanel.add(gridLabel);
 		dummyPanel.add(new JLabel("|"));
 		dummyPanel.add(levelSizeLabel);
 		dummyPanel.add(new JLabel("|"));
 		dummyPanel.add(editModeLabel);
 
 		frame.add(dummyPanel, BorderLayout.NORTH);
 
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
 
 		/*
 		 * Setup menu
 		 */
 
 		JMenuBar menuBar = new JMenuBar();
 
 		/**
 		 * FILE MENU
 		 */
 
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem open = new JMenuItem("Open");
 		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 				Event.CTRL_MASK));
 		open.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				File f = importNewFileDialog();
 				if (f != null) {
 					setLevelFile(f);
 					try {
 						reloadLevel();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		fileMenu.add(open);
 		JMenuItem save = new JMenuItem("Save");
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					saveLevel();
 					JOptionPane.showMessageDialog(null, "Level Saved!");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				Event.CTRL_MASK));
 		fileMenu.add(save);
 		
 		JMenuItem close = new JMenuItem("Close");
 		close.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		fileMenu.add(close);
 		menuBar.add(fileMenu);
 
 		// Entity Menu
 		JMenu entityMenu = new JMenu("Entities");
 
 		JMenuItem makeWallEntry = new JMenuItem("Make Wall");
 		makeWallEntry.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				makeWall = true;
 				makingWall = false;
 				selected = null;
 				repaint();
 			}
 		});
 		entityMenu.add(makeWallEntry);
 		makeWallEntry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
 				Event.CTRL_MASK));
 
 		JMenuItem importXml = new JMenuItem("Import XML Entity");
 		importXml.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				File f = importNewFileDialog();
 				if (f != null)
 					try {
 						importEntity(f);
 						repaint();
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					}
 			}
 		});
 		entityMenu.add(importXml);
 		importXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
 				Event.CTRL_MASK));
 
 		JMenuItem deleteAll = new JMenuItem("Delete All");
 		deleteAll.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				entities.clear();
 				repaint();
 			}
 		});
 		entityMenu.add(deleteAll);
 		menuBar.add(entityMenu);
 
 		/**
 		 * SETTINGS MENU
 		 */
 		JMenu settingsMenu = new JMenu("Settings");
 		JMenuItem gridMenu = new JMenuItem("Grid Size");
 		gridMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					int i = Integer.parseInt(JOptionPane
 							.showInputDialog("Input an integer grid size:"));
 					gridSize.setSize(new Dimension(i, i));
 					repaint();
 				} catch (final Exception i) {
 					return;
 				}
 
 			}
 		});
 		settingsMenu.add(gridMenu);
 		JMenuItem levelSizeItem = new JMenuItem("Level Size");
 		levelSizeItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					String result = JOptionPane
 							.showInputDialog("Input an integer world width, height (ex. 1000, 5000):");
 					String vals[] = result.split(",");
 					levelSize.width = Integer.parseInt(vals[0].trim());
 					levelSize.height = Integer.parseInt(vals[1].trim());
 					repaint();
 				} catch (final Exception i) {
 					return;
 				}
 
 			}
 		});
 		settingsMenu.add(levelSizeItem);
 		menuBar.add(settingsMenu);
 
 		/**
 		 * HELP MENU
 		 */
 		JMenuItem item = new JMenuItem("Help");
 		item.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Everything is still in progress!\n ~tori"
 										+ "\n\nLeft click to place the latest selected object!\nRight click to select!\nDELETE to delete selected object!");
 			}
 		});
 		menuBar.add(item);
 
 		frame.setJMenuBar(menuBar);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Make the first detected overlapping, visible object, selected.
 	 * 
 	 * @param p
 	 *            the mouse location.
 	 */
 	public void selectOverlapping(final Vector2 p) {
 		Entity selected = null;
 		for (Entity e : entities) {
			if (new Rectangle((int) e.pos.getX(), (int) e.pos.getY(),
 							(int) e.dim.getX(), (int) e.dim.getY())
 							.contains(new Point((int) p.x, (int) p.y))
 					&& this.selected != e) {
 				selected = e;
 				break;
 			}
 		}
 		this.selected = selected;
 	}
 
 	/**
 	 * Delete the entity that is currently selected.
 	 */
 	public void deleteSelected() {
 		if (selected != null) {
 			removeEntity(selected);
 		}
 	}
 
 	/**
 	 * Delete the first object detected overlapping.
 	 * 
 	 * @param p
 	 *            the mouse point.
 	 */
 	public void deleteOverlapping(final Vector2 p) {
 		Entity selected = null;
 		for (Entity e : entities) {
 			if (e.layer == layerEditor.getCurrentLayer()
 					&& new Rectangle((int) e.pos.getX(), (int) e.pos.getY(),
 							(int) e.dim.getX(), (int) e.dim.getY())
 							.contains(new Point((int) p.x, (int) p.y))
 					&& this.selected != e) {
 				selected = e;
 				break;
 			}
 		}
 		removeEntity(selected);
 	}
 
 	/**
 	 * Add an entity, and give it to a layer.
 	 * 
 	 * @param e
 	 *            the entity
 	 */
 	private void addEntity(final Entity e) {
 		entities.add(e);
 		repaint();
 	}
 
 	/**
 	 * Remove an entity from all layers.
 	 * 
 	 * @param e
 	 *            entity to remove.
 	 */
 	public void removeEntity(final Entity e) {
 		entities.remove(e);
 		repaint();
 	}
 
 	/**
 	 * Remove entity from one layer and add to another.
 	 * 
 	 * @param e
 	 *            entity to move.
 	 * @param layer
 	 *            layer to add to.
 	 */
 	@SuppressWarnings("unused")
 	private void transferEntity(final Entity e, final int layer) {
 		removeEntity(e);
 		e.layer = layer;
 		addEntity(e);
 		repaint();
 	}
 
 	/**
 	 * Save level.xml.
 	 * 
 	 * @throws ParserConfigurationException
 	 * @throws TransformerException
 	 * @throws IOException
 	 * @throws DOMException
 	 */
 	public void saveLevel() throws ParserConfigurationException,
 			TransformerException, DOMException, IOException {
 		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 		Document doc = docBuilder.newDocument();
 
 		// Create level element
 		Element levelElement = doc.createElement("level");
 		doc.appendChild(levelElement);
 
 		// Add the attributes of the level
 		HashMap<String, String> props = new HashMap<String, String>();
 		props.put("width", levelSize.width + "");
 		props.put("height", levelSize.height + "");
 		levelElement.setAttribute("map", ToriMapIO.writeMap(null, props));
 
 		// Save the objects
 		Element objectsElements = doc.createElement("objects");
 		levelElement.appendChild(objectsElements);
 		for (Entity e : entities) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("position.x", e.pos.getX() + "");
 			map.put("position.y", e.pos.getY() + "");
 			map.put("template",
 					e.getFile()
 							.getPath()
 							.substring(
 									e.getFile()
 											.getPath()
 											.indexOf(workingDirectory.getName())
 											+ workingDirectory.getName()
 													.length()));
 			map.put("layer", e.layer + "");
 			map.putAll(e.variables.getVariables());
 			Element object = doc.createElement("entity");
 			object.setAttribute("map", ToriMapIO.writeMap(null, map));
 			objectsElements.appendChild(object);
 		}
 
 		ToriXML.saveXMLDoc(levelFile, doc);
 	}
 
 	/**
 	 * Load the entity data from level.xml.
 	 * 
 	 * @param doc
 	 *            the doc of level.xml.
 	 * @throws DOMException
 	 * @throws FileNotFoundException
 	 */
 	private void loadLevel(final Document doc) throws FileNotFoundException,
 			DOMException {
 		HashMap<String, String> props = ToriMapIO.readMap(doc
 				.getElementsByTagName("level").item(0).getAttributes()
 				.getNamedItem("map").getNodeValue());
 
 		// Extract level instance info
 		levelSize.width = Integer.parseInt(props.get("width"));
 		levelSize.height = Integer.parseInt(props.get("height"));
 
 		NodeList entities = doc.getElementsByTagName("entity");
 		for (int i = 0; i < entities.getLength(); i++) {
 			Node e = entities.item(i);
 			HashMap<String, String> mapData = ToriMapIO.readMap(e
 					.getAttributes().getNamedItem("map").getNodeValue());
 			int layer = Integer.parseInt(mapData.get("layer"));
 			float x = Float.parseFloat(mapData.get("position.x"));
 			float y = Float.parseFloat(mapData.get("position.y"));
 			File f = new File(workingDirectory + mapData.get("template"));
 			Entity ent = importEntity(f);
 			ent.pos = new Vector2(x, y);
 			try {
 				ent.dim.x = Float.parseFloat(mapData.get("dimensions.x"));
 				ent.dim.y = Float.parseFloat(mapData.get("dimensions.y"));
 			} catch (NullPointerException exc) {
 			}
 			layerEditor.setLayerVisibility(layer, true);
 			ent.variables.getVariables().putAll(mapData);
 			ent.layer = layer;
 			addEntity(ent);
 		}
 	}
 
 	/**
 	 * Bring up a file picker to import a new item.
 	 */
 	private File importNewFileDialog() {
 		JFileChooser fileChooser = new JFileChooser();
 		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
 				"xml files", "xml"));
 		fileChooser.setCurrentDirectory(workingDirectory);
 		int ret = fileChooser.showDialog(null, "Import an xml file");
 		if (ret == JFileChooser.APPROVE_OPTION) {
 			return fileChooser.getSelectedFile();
 		}
 		return null;
 	}
 
 	/**
 	 * Fully import and add an xml entity. if the entity is a new type, it will
 	 * be given a new button and added to button panel.
 	 * 
 	 * @param file
 	 *            the file of the xml.
 	 * @return the generated entity.
 	 * @throws FileNotFoundException
 	 */
 	private Entity importEntity(final File file) throws FileNotFoundException {
 		HashMap<String, String> data = ToriMapIO.readMap(file);
 		
 		final Entity e = Importer.importEntity(file, null);
 		try {
 			String[] s = data.get("sprite.editor").split(",");
 			e.sprite.set(Integer.parseInt(s[0].trim()), Integer.parseInt(s[1].trim()));
 		} catch(final Exception exc) {
 			//The sprite remains at 1,1;
 		}
 		
 		if (!entityExists(e)) {
 			ImageIcon bI = new ImageIcon();
 			bI.setImage(e.sprite.getImage().getScaledInstance(32, 32, 0));
 			JButton b = new JButton(bI);
 			b.setToolTipText(data.get("description"));
 			b.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					setCurrent(e);
 				}
 			});
 			buttonPanel.add(b);
 			repaint();
 		}
 		return e;
 	}
 
 	private boolean entityExists(final Entity e) {
 		for (Entity e2 : entities) {
 			if (e2.file.equals(e.file))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Draw the state of the level.
 	 * 
 	 * @param g
 	 *            graphics!
 	 */
 	public void draw(Graphics g) {
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, levelSize.width, levelSize.height);
 
 		/*
 		 * DRAW THE GRID
 		 */
 		if (gridSize.width > 1) {
 			g.setColor(Color.BLACK);
 			for (int x = 0; x < levelSize.width; x += gridSize.width)
 				g.drawLine(x, 0, x, levelSize.height);
 
 			for (int y = 0; y < drawPanel.getHeight(); y += gridSize.height)
 				g.drawLine(0, y, drawPanel.getWidth(), y);
 		}
 
 		/**
 		 * DRAW ENTITIES in layer order.
 		 */
 		Collections.sort(entities, new Comparator<Entity>() {
 			@Override
 			public int compare(Entity a, Entity b) {
 				return new Integer(b.layer).compareTo(new Integer(a.layer));
 			}
 		});
 		for (Entity e : entities) {
 			if (layerEditor.isLayerVisible(e.layer))
 				e.draw(g, new Vector2());
 			if (selected == e) {
 				g.setColor(Color.RED);
 				g.drawRect((int) e.pos.getX(), (int) e.pos.getY(),
 						(int) e.dim.getX(), (int) e.dim.getY());
 			}
 		}
 		g.setColor(Color.RED);
 		g.draw3DRect(0, 0, levelSize.width, levelSize.height, true);
 
 		if (makingWall) {
 			g.drawRect((int) wallStart.x, (int) wallStart.y, (int) wallEnd.x
 					- (int) wallStart.x, (int) wallEnd.y - (int) wallStart.y);
 		}
 	}
 
 	/**
 	 * Clear the GUI state.
 	 */
 	private void clear() {
 		entities.clear();
 		current = null;
 		selected = null;
 		buttonPanel.removeAll();
 		layerEditor.clear();
 	}
 
 	public void setCurrent(final Entity e) {
 		current = e;
 	}
 
 	/**
 	 * Forces repaint on frame and updates status bar.
 	 */
 	public void repaint() {
 		fileLabel.setText(levelFile.getName());
 		gridLabel.setText("Grid: " + (int) gridSize.getWidth() + " x "
 				+ (int) gridSize.getHeight());
 		levelSizeLabel.setText("Level Size: " + (int) levelSize.getWidth()
 				+ " x " + (int) levelSize.getHeight());
 		if (selected != null) {
			editModeLabel.setText("Editing Single Entity: " + selected.type);
 		} else if (makeWall) {
 			editModeLabel.setText("Click and Drag to Draw Wall");
 		} else {
 			editModeLabel.setText("Click to Place Entity");
 		}
 		drawPanel.setPreferredSize(levelSize);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	public void makeWall(final Vector2 pos, final Vector2 dim) {
 		try {
 			if (dim.x == 0 || dim.y == 0)
 				return;
 			Entity wall = this.importEntity(new File(
 					"levels/objects/wall/wall.entity"));
 			wall.pos = pos.clone();
 			wall.dim = dim.clone();
 			wall.variables.setVar("dimensions.x", dim.x + "");
 			wall.variables.setVar("dimensions.y", dim.y + "");
			wall.layer =layerEditor.getCurrentLayer();
			addEntity(wall);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Vector2 getClosestGridPoint(final Vector2 p) {
 		return new Vector2(((int) p.x / (int) gridSize.width) * gridSize.width,
 				((int) p.y / (int) gridSize.height) * gridSize.height);
 	}
 }

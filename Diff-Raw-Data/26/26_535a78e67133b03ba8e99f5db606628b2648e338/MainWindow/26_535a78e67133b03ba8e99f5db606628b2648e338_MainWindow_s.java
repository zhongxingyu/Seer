 package net.buddat.wplanner.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Image;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 
 import net.buddat.wplanner.WPlanner;
 import net.buddat.wplanner.gui.GraphicPanel.EditState;
 import net.buddat.wplanner.gui.resources.ResourceManager;
 import net.buddat.wplanner.gui.resources.ResourceManager.ImageType;
 import net.buddat.wplanner.map.Map;
 import net.buddat.wplanner.util.Constants;
 
 import javax.swing.JButton;
 import javax.swing.BoxLayout;
 import javax.swing.SwingConstants;
 import javax.swing.JToolBar;
 import javax.swing.JRadioButton;
 import javax.swing.JLabel;
 import java.awt.Component;
 import java.util.Enumeration;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import java.io.File;
 
 public class MainWindow extends JFrame implements ActionListener, ChangeListener, WindowListener {
 
 	private static final long serialVersionUID = 6408703853685252009L;
 	
 	private ResourceManager resources;
 	
 	private JPanel contentPane;
 	
 	private static final String CMD_NEW = "new", CMD_OPEN = "open", CMD_SAVE = "save", CMD_SAVEAS = "saveas",
 			CMD_SAVEIMG = "saveimage", CMD_SAVEIMGAS = "saveimageas", CMD_EXIT = "exit",
 			CMD_UNDO = "undo", CMD_REDO = "redo", CMD_RESIZEMAP = "resizemap",
 			CMD_ZOOMIN = "zoomin", CMD_ZOOMOUT = "zoomout", 
 			CMD_HELP = "help", CMD_ABOUT = "about",
 			CMD_TERRAIN_PENCIL = "terrain_pencil", CMD_TERRAIN_BRUSH = "terrain_brush", CMD_TERRAIN_FILL = "terrain_fill", 
 			CMD_TERRAIN_ERASER = "terrain_eraser", CMD_TERRAIN_PICKER = "terrain_picker",
 			CMD_OBJECTS_PENCIL = "objects_pencil", CMD_OBJECTS_ERASER = "objects_eraser", CMD_OBJECTS_PICKER = "objects_picker",
 			CMD_FENCE_PENCIL = "fence_pencil", CMD_FENCE_LINE = "fence_line", CMD_FENCE_ERASER = "fence_eraser", CMD_FENCE_PICKER = "fence_picker",
 			CMD_LABEL = "label",
 			CMD_OVERLAY_PENCIL = "overlay_pencil", CMD_OVERLAY_BRUSH = "overlay_brush", CMD_OVERLAY_ERASER = "overlay_eraser", 
 			CMD_OVERLAY_PICKER = "overlay_picker", CMD_OVERLAY_COLOR = "overlay_color",
 			CMD_LAYERUP = "layerup", CMD_LAYERDOWN = "layerdown",
 			CMD_BRUSHSIZE = "brushsize",
 			CHANGE_SELECTOR_RESIZE = "selectorsize";
 
 	private static final String TAB_SELECTOR = "tabselector";
 	
 	private int brushSize = 3;
 	private int selectedTerrain = 0, selectedObject = 0, selectedFence = 0;
 	private Color overlayColor = new Color(Color.WHITE.getRed(), Color.WHITE.getBlue(), Color.WHITE.getGreen(), 127);
 	private Color labelColor = Color.BLACK;
 	
 	JCheckBoxMenuItem chckbxmntmTerrain;
 	JCheckBoxMenuItem chckbxmntmObjects;
 	JCheckBoxMenuItem chckbxmntmFences;
 	JCheckBoxMenuItem chckbxmntmOverlay;
 	JCheckBoxMenuItem chckbxmntmLabels;
 	JCheckBoxMenuItem chckbxmntmGrid;
 	
 	JTabbedPane tabEditor;
 	ButtonGroup toolGroup;
 	ButtonGroup terrainGroup, objectGroup, fenceGroup;
 	
 	JTabbedPane tabSelector;
 
 	public MainWindow() {
 		init();
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(WPlanner.getConfig().getWindowWidth(), WPlanner.getConfig().getWindowHeight());
 		setLocationRelativeTo(null);
 		setTitle(Constants.PROGRAM_NAME + " | v." + Constants.VERSION_MAJOR + "." + Constants.VERSION_MIDI +
 				"." + Constants.VERSION_MINOR);
 		
 		addWindowListener(this);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmNew = new JMenuItem("New");
 		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
 		mntmNew.setActionCommand(CMD_NEW);
 		mntmNew.addActionListener(this);
 		mnFile.add(mntmNew);
 		
 		JMenuItem mntmOpen = new JMenuItem("Open...");
 		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
 		mntmOpen.setActionCommand(CMD_OPEN);
 		mntmOpen.addActionListener(this);
 		mnFile.add(mntmOpen);
 		
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
 		mntmSave.setActionCommand(CMD_SAVE);
 		mntmSave.addActionListener(this);
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
 		mntmSaveAs.setActionCommand(CMD_SAVEAS);
 		mntmSaveAs.addActionListener(this);
 		mnFile.add(mntmSaveAs);
 		
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 		
 		JMenuItem mntmSaveImage = new JMenuItem("Save Image");
 		mntmSaveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
 		mntmSaveImage.setActionCommand(CMD_SAVEIMG);
 		mntmSaveImage.addActionListener(this);
 		mnFile.add(mntmSaveImage);
 		
 		JMenuItem mntmSaveImageAs = new JMenuItem("Save Image As...");
 		mntmSaveImageAs.setActionCommand(CMD_SAVEIMGAS);
 		mntmSaveImageAs.addActionListener(this);
 		mnFile.add(mntmSaveImageAs);
 		
 		JSeparator separator_1 = new JSeparator();
 		mnFile.add(separator_1);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
 		mntmExit.setActionCommand(CMD_EXIT);
 		mntmExit.addActionListener(this);
 		mnFile.add(mntmExit);
 		
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 		
 		JMenuItem mntmUndo = new JMenuItem("Undo");
 		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
 		mntmUndo.setActionCommand(CMD_UNDO);
 		mntmUndo.addActionListener(this);
 		mnEdit.add(mntmUndo);
 		
 		JMenuItem mntmRedo = new JMenuItem("Redo");
 		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
 		mntmRedo.setActionCommand(CMD_REDO);
 		mntmRedo.addActionListener(this);
 		mnEdit.add(mntmRedo);
 		
 		JSeparator separator_2 = new JSeparator();
 		mnEdit.add(separator_2);
 		
 		JMenuItem mntmResizeMap = new JMenuItem("Resize Map...");
 		mntmResizeMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
 		mntmResizeMap.setActionCommand(CMD_RESIZEMAP);
 		mntmResizeMap.addActionListener(this);
 		mnEdit.add(mntmResizeMap);
 		
 		JMenu mnView = new JMenu("View");
 		menuBar.add(mnView);
 		
 		JMenuItem mntmZoomIn = new JMenuItem("Zoom In");
 		mntmZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK));
 		mntmZoomIn.setActionCommand(CMD_ZOOMIN);
 		mntmZoomIn.addActionListener(this);
 		mnView.add(mntmZoomIn);
 		
 		JMenuItem mntmZoomOut = new JMenuItem("Zoom Out");
 		mntmZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
 		mntmZoomOut.setActionCommand(CMD_ZOOMOUT);
 		mntmZoomOut.addActionListener(this);
 		mnView.add(mntmZoomOut);
 		
 		JSeparator separator_3 = new JSeparator();
 		mnView.add(separator_3);
 		
 		chckbxmntmTerrain = new JCheckBoxMenuItem("Terrain");
 		chckbxmntmTerrain.setSelected(true);
 		mnView.add(chckbxmntmTerrain);
 		
 		chckbxmntmObjects = new JCheckBoxMenuItem("Objects");
 		chckbxmntmObjects.setSelected(true);
 		mnView.add(chckbxmntmObjects);
 		
 		chckbxmntmFences = new JCheckBoxMenuItem("Fences");
 		chckbxmntmFences.setSelected(true);
 		mnView.add(chckbxmntmFences);
 		
 		chckbxmntmOverlay = new JCheckBoxMenuItem("Overlay");
 		chckbxmntmOverlay.setSelected(true);
 		mnView.add(chckbxmntmOverlay);
 		
 		chckbxmntmLabels = new JCheckBoxMenuItem("Labels");
 		chckbxmntmLabels.setSelected(true);
 		mnView.add(chckbxmntmLabels);
 		
 		JSeparator separator_5 = new JSeparator();
 		mnView.add(separator_5);
 		
 		chckbxmntmGrid = new JCheckBoxMenuItem("Grid");
 		chckbxmntmGrid.setSelected(true);
 		mnView.add(chckbxmntmGrid);
 		
 		JMenu mnHelp = new JMenu("Help");
 		menuBar.add(mnHelp);
 		
 		JMenuItem mntmHelp = new JMenuItem("Help");
 		mntmHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 		mntmHelp.setActionCommand(CMD_HELP);
 		mntmHelp.addActionListener(this);
 		mnHelp.add(mntmHelp);
 		
 		JSeparator separator_4 = new JSeparator();
 		mnHelp.add(separator_4);
 		
 		JMenuItem mntmAbout = new JMenuItem("About");
 		mntmAbout.setActionCommand(CMD_ABOUT);
 		mntmAbout.addActionListener(this);
 		mnHelp.add(mntmAbout);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		contentPane.add(panel, BorderLayout.NORTH);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		
 		JToolBar menuTools = new JToolBar();
 		panel.add(menuTools);
 		
 		JButton btnNew = new JButton();
 		btnNew.setToolTipText("Create a new map");
 		btnNew.setIcon(getResources().getGuiImage(Constants.GUI_NEW));
 		btnNew.setActionCommand(CMD_NEW);
 		btnNew.addActionListener(this);
 		btnNew.setBorder(new EmptyBorder(2, 2, 5, 2));
 		menuTools.add(btnNew);
 		
 		JButton btnOpen = new JButton();
 		btnOpen.setToolTipText("Open an existing map");
 		btnOpen.setIcon(getResources().getGuiImage(Constants.GUI_OPEN));
 		btnOpen.setActionCommand(CMD_OPEN);
 		btnOpen.addActionListener(this);
 		btnOpen.setBorder(new EmptyBorder(2, 2, 5, 2));
 		menuTools.add(btnOpen);
 		
 		JButton btnSave = new JButton();
 		btnSave.setToolTipText("Save the current map");
 		btnSave.setIcon(getResources().getGuiImage(Constants.GUI_SAVE));
 		btnSave.setActionCommand(CMD_SAVE);
 		btnSave.addActionListener(this);
 		btnSave.setBorder(new EmptyBorder(2, 2, 5, 2));
 		menuTools.add(btnSave);
 		
 		JButton btnSaveImage = new JButton();
 		btnSaveImage.setToolTipText("Save the current map as an image");
 		btnSaveImage.setIcon(getResources().getGuiImage(Constants.GUI_SAVE_IMAGE));
 		btnSaveImage.setActionCommand(CMD_SAVEIMG);
 		btnSaveImage.addActionListener(this);
 		btnSaveImage.setBorder(new EmptyBorder(2, 2, 5, 2));
 		menuTools.add(btnSaveImage);
 		
 		toolGroup = new ButtonGroup();
 		
 		JToolBar terrainTools = new JToolBar();
 		terrainTools.setToolTipText("Terrain Tools");
 		panel.add(terrainTools);
 		
 		JLabel lblTerrain = new JLabel("Terrain:");
 		lblTerrain.setBorder(new EmptyBorder(2, 2, 2, 5));
 		terrainTools.add(lblTerrain);
 		
 		JRadioButton rdbtnTerrainPencil = new JRadioButton();
 		rdbtnTerrainPencil.setToolTipText("Terrain pencil - changes single tiles");
 		rdbtnTerrainPencil.setSelected(true);
 		rdbtnTerrainPencil.setIcon(getResources().getGuiImage(Constants.GUI_PENCIL));
 		rdbtnTerrainPencil.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnTerrainPencil.setBorderPainted(true);
 		rdbtnTerrainPencil.setActionCommand(CMD_TERRAIN_PENCIL);
 		rdbtnTerrainPencil.addActionListener(this);
 		terrainTools.add(rdbtnTerrainPencil);
 		toolGroup.add(rdbtnTerrainPencil);
 		
 		JRadioButton rdbtnTerrainBrush = new JRadioButton();
 		rdbtnTerrainBrush.setToolTipText("Terrain brush - changes multiple tiles");
 		rdbtnTerrainBrush.setIcon(getResources().getGuiImage(Constants.GUI_BRUSH));
 		rdbtnTerrainBrush.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnTerrainBrush.setActionCommand(CMD_TERRAIN_BRUSH);
 		rdbtnTerrainBrush.addActionListener(this);
 		terrainTools.add(rdbtnTerrainBrush);
 		toolGroup.add(rdbtnTerrainBrush);
 		
 		JRadioButton rdbtnTerrainFill = new JRadioButton();
 		rdbtnTerrainFill.setToolTipText("Terrain fill - fills an area");
 		rdbtnTerrainFill.setIcon(getResources().getGuiImage(Constants.GUI_FILL));
 		rdbtnTerrainFill.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnTerrainFill.setActionCommand(CMD_TERRAIN_FILL);
 		rdbtnTerrainFill.addActionListener(this);
 		terrainTools.add(rdbtnTerrainFill);
 		toolGroup.add(rdbtnTerrainFill);
 		
 		JRadioButton rdbtnTerrainEraser = new JRadioButton();
 		rdbtnTerrainEraser.setToolTipText("Terrain eraser - erases a single tile");
 		rdbtnTerrainEraser.setIcon(getResources().getGuiImage(Constants.GUI_ERASER));
 		rdbtnTerrainEraser.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnTerrainEraser.setActionCommand(CMD_TERRAIN_ERASER);
 		rdbtnTerrainEraser.addActionListener(this);
 		terrainTools.add(rdbtnTerrainEraser);
 		toolGroup.add(rdbtnTerrainEraser);
 		
 		JRadioButton rdbtnTerrainPicker = new JRadioButton();
 		rdbtnTerrainPicker.setToolTipText("Terrain picker - sets selected terrain to chosen tile");
 		rdbtnTerrainPicker.setIcon(getResources().getGuiImage(Constants.GUI_PICKER));
 		rdbtnTerrainPicker.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnTerrainPicker.setActionCommand(CMD_TERRAIN_PICKER);
 		rdbtnTerrainPicker.addActionListener(this);
 		terrainTools.add(rdbtnTerrainPicker);
 		toolGroup.add(rdbtnTerrainPicker);
 		
 		JLabel lblObjects = new JLabel("Objects:");
 		lblObjects.setBorder(new EmptyBorder(2, 10, 2, 5));
 		terrainTools.add(lblObjects);
 		
 		JRadioButton rdbtnObjectsPencil = new JRadioButton();
 		rdbtnObjectsPencil.setToolTipText("Objects pencil - changes single objects");
 		rdbtnObjectsPencil.setIcon(getResources().getGuiImage(Constants.GUI_PENCIL));
 		rdbtnObjectsPencil.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnObjectsPencil.setActionCommand(CMD_OBJECTS_PENCIL);
 		rdbtnObjectsPencil.addActionListener(this);
 		terrainTools.add(rdbtnObjectsPencil);
 		toolGroup.add(rdbtnObjectsPencil);
 		
 		JRadioButton rdbtnObjectsEraser = new JRadioButton();
 		rdbtnObjectsEraser.setToolTipText("Object eraser - erases single objects");
 		rdbtnObjectsEraser.setIcon(getResources().getGuiImage(Constants.GUI_ERASER));
 		rdbtnObjectsEraser.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnObjectsEraser.setActionCommand(CMD_OBJECTS_ERASER);
 		rdbtnObjectsEraser.addActionListener(this);
 		terrainTools.add(rdbtnObjectsEraser);
 		toolGroup.add(rdbtnObjectsEraser);
 		
 		JRadioButton rdbtnObjectsPicker = new JRadioButton();
 		rdbtnObjectsPicker.setToolTipText("Object picker - sets selected object to the chosen object");
 		rdbtnObjectsPicker.setIcon(getResources().getGuiImage(Constants.GUI_PICKER));
 		rdbtnObjectsPicker.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnObjectsPicker.setActionCommand(CMD_OBJECTS_PICKER);
 		rdbtnObjectsPicker.addActionListener(this);
 		terrainTools.add(rdbtnObjectsPicker);
 		toolGroup.add(rdbtnObjectsPicker);
 		
 		JLabel lblFences = new JLabel("Fences:");
 		lblFences.setBorder(new EmptyBorder(2, 10, 2, 5));
 		terrainTools.add(lblFences);
 		
 		JRadioButton rdbtnFencePencil = new JRadioButton();
 		rdbtnFencePencil.setToolTipText("Fence pencil - changes single fences");
 		rdbtnFencePencil.setIcon(getResources().getGuiImage(Constants.GUI_PENCIL));
 		rdbtnFencePencil.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnFencePencil.setActionCommand(CMD_FENCE_PENCIL);
 		rdbtnFencePencil.addActionListener(this);
 		terrainTools.add(rdbtnFencePencil);
 		toolGroup.add(rdbtnFencePencil);
 
 		JRadioButton rdbtnFenceLine = new JRadioButton();
 		rdbtnFenceLine.setToolTipText("Fence line - creates a line of the same fence");
 		rdbtnFenceLine.setIcon(getResources().getGuiImage(Constants.GUI_LINE));
 		rdbtnFenceLine.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnFenceLine.setActionCommand(CMD_FENCE_LINE);
 		rdbtnFenceLine.addActionListener(this);
 		terrainTools.add(rdbtnFenceLine);
 		toolGroup.add(rdbtnFenceLine);
 		
 		JRadioButton rdbtnFenceEraser = new JRadioButton();
 		rdbtnFenceEraser.setToolTipText("Fence eraser - removes a single fence");
 		rdbtnFenceEraser.setIcon(getResources().getGuiImage(Constants.GUI_ERASER));
 		rdbtnFenceEraser.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnFenceEraser.setActionCommand(CMD_FENCE_ERASER);
 		rdbtnFenceEraser.addActionListener(this);
 		terrainTools.add(rdbtnFenceEraser);
 		toolGroup.add(rdbtnFenceEraser);
 		
 		JRadioButton rdbtnFencePicker = new JRadioButton();
 		rdbtnFencePicker.setToolTipText("Fence picker - sets the selected fence to the chosen fence");
 		rdbtnFencePicker.setIcon(getResources().getGuiImage(Constants.GUI_PICKER));
 		rdbtnFencePicker.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnFencePicker.setActionCommand(CMD_FENCE_PICKER);
 		rdbtnFencePicker.addActionListener(this);
 		terrainTools.add(rdbtnFencePicker);
 		toolGroup.add(rdbtnFencePicker);
 		
 		JLabel lblMisc = new JLabel("Misc:");
 		lblMisc.setBorder(new EmptyBorder(2, 10, 2, 5));
 		terrainTools.add(lblMisc);
 		
 		JRadioButton rdbtnLabel = new JRadioButton();
 		rdbtnLabel.setToolTipText("Label tool - lets you change the tile label");
 		rdbtnLabel.setIcon(getResources().getGuiImage(Constants.GUI_LABEL));
 		rdbtnLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnLabel.setActionCommand(CMD_LABEL);
 		rdbtnLabel.addActionListener(this);
 		terrainTools.add(rdbtnLabel);
 		toolGroup.add(rdbtnLabel);
 		
 		JRadioButton rdbtnOverlayPencil = new JRadioButton();
 		rdbtnOverlayPencil.setToolTipText("Overlay pencil - highlights a single tile");
 		rdbtnOverlayPencil.setIcon(getResources().getGuiImage(Constants.GUI_PENCIL));
 		rdbtnOverlayPencil.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnOverlayPencil.setActionCommand(CMD_OVERLAY_PENCIL);
 		rdbtnOverlayPencil.addActionListener(this);
 		terrainTools.add(rdbtnOverlayPencil);
 		toolGroup.add(rdbtnOverlayPencil);
 		
 		JRadioButton rdbtnOverlayBrush = new JRadioButton();
 		rdbtnOverlayBrush.setToolTipText("Overlay brush - highlights multiple tiles");
 		rdbtnOverlayBrush.setIcon(getResources().getGuiImage(Constants.GUI_BRUSH));
 		rdbtnOverlayBrush.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnOverlayBrush.setActionCommand(CMD_OVERLAY_BRUSH);
 		rdbtnOverlayBrush.addActionListener(this);
 		terrainTools.add(rdbtnOverlayBrush);
 		toolGroup.add(rdbtnOverlayBrush);
 		
 		JRadioButton rdbtnOverlayEraser = new JRadioButton();
 		rdbtnOverlayEraser.setToolTipText("Overlay eraser - removes highlight from single tile");
 		rdbtnOverlayEraser.setIcon(getResources().getGuiImage(Constants.GUI_ERASER));
 		rdbtnOverlayEraser.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnOverlayEraser.setActionCommand(CMD_OVERLAY_ERASER);
 		rdbtnOverlayEraser.addActionListener(this);
 		terrainTools.add(rdbtnOverlayEraser);
 		toolGroup.add(rdbtnOverlayEraser);
 		
 		JRadioButton rdbtnOverlayPicker = new JRadioButton();
 		rdbtnOverlayPicker.setToolTipText("Overlay picker - sets overlay colour to the chosen tile's overlay colour");
 		rdbtnOverlayPicker.setIcon(getResources().getGuiImage(Constants.GUI_PICKER));
 		rdbtnOverlayPicker.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
 		rdbtnOverlayPicker.setActionCommand(CMD_OVERLAY_PICKER);
 		rdbtnOverlayPicker.addActionListener(this);
 		terrainTools.add(rdbtnOverlayPicker);
 		toolGroup.add(rdbtnOverlayPicker);
 		
 		JButton btnOverlayColor = new JButton();
 		btnOverlayColor.setToolTipText("Overlay colour chooser - sllows you to choose the overlay colour");
 		btnOverlayColor.setIcon(getResources().getGuiImage(Constants.GUI_COLOR_CHOOSER));
 		btnOverlayColor.setBorder(new EmptyBorder(2, 2, 5, 2));
 		btnOverlayColor.setActionCommand(CMD_OVERLAY_COLOR);
 		btnOverlayColor.addActionListener(this);
 		terrainTools.add(btnOverlayColor);
 		
 		JLabel lblBrushSize = new JLabel("Brush:");
 		lblBrushSize.setBorder(new EmptyBorder(2, 10, 2, 5));
 		panel.add(lblBrushSize);
 		
 		JSpinner spinner = new JSpinner();
 		spinner.setModel(new SpinnerNumberModel(3, 3, 21, 2));
 		spinner.setMaximumSize(new Dimension(40, 25));
 		spinner.setName(CMD_BRUSHSIZE);
 		spinner.addChangeListener(this);
 		panel.add(spinner);
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setName(CHANGE_SELECTOR_RESIZE);
 		contentPane.add(splitPane, BorderLayout.CENTER);
 		
 		tabSelector = new JTabbedPane(JTabbedPane.TOP);
 		tabSelector.setName(TAB_SELECTOR);
 		tabSelector.addChangeListener(this);
 		splitPane.setLeftComponent(tabSelector);
 		
 		setupSelectorTab("Terrain", ImageType.TERRAIN);
 		setupSelectorTab("Objects", ImageType.OBJECT);
 		setupSelectorTab("Fences", ImageType.FENCE);
 		
 		tabEditor = new JTabbedPane(JTabbedPane.TOP);
 		splitPane.setRightComponent(tabEditor);
 		
 		JToolBar viewTools = new JToolBar();
 		viewTools.setOrientation(SwingConstants.VERTICAL);
 		contentPane.add(viewTools, BorderLayout.EAST);
 		
 		JButton btnZoomIn = new JButton();
 		btnZoomIn.setToolTipText("Zoom In");
 		btnZoomIn.setIcon(getResources().getGuiImage(Constants.GUI_ZOOM_IN));
 		btnZoomIn.setBorder(new EmptyBorder(2, 2, 5, 2));
 		btnZoomIn.setActionCommand(CMD_ZOOMIN);
 		btnZoomIn.addActionListener(this);
 		viewTools.add(btnZoomIn);
 		
 		JButton btnZoomOut = new JButton();
 		btnZoomOut.setToolTipText("Zoom Out");
 		btnZoomOut.setIcon(getResources().getGuiImage(Constants.GUI_ZOOM_OUT));
 		btnZoomOut.setBorder(new EmptyBorder(2, 2, 5, 2));
 		btnZoomOut.setActionCommand(CMD_ZOOMOUT);
 		btnZoomOut.addActionListener(this);
 		viewTools.add(btnZoomOut);
 		
 		JButton btnLayerUp = new JButton();
 		btnLayerUp.setToolTipText("Layer Up");
 		btnLayerUp.setIcon(getResources().getGuiImage(Constants.GUI_LAYER_UP));
 		btnLayerUp.setBorder(new EmptyBorder(2, 2, 5, 2));
 		btnLayerUp.setActionCommand(CMD_LAYERUP);
 		btnLayerUp.addActionListener(this);
 		viewTools.add(btnLayerUp);
 		
 		JButton btnLayerDown = new JButton();
 		btnLayerDown.setToolTipText("Layer Down");
 		btnLayerDown.setIcon(getResources().getGuiImage(Constants.GUI_LAYER_DOWN));
 		btnLayerDown.setBorder(new EmptyBorder(2, 2, 5, 2));
 		btnLayerDown.setActionCommand(CMD_LAYERDOWN);
 		btnLayerDown.addActionListener(this);
 		viewTools.add(btnLayerDown);
 
 		setVisible(true);
 	}
 	
 	private void setupSelectorTab(String name, final ImageType type) {
 		JPanel pnl = new JPanel();
 		//TODO: FIX ME
 		pnl.setLayout(new GridLayout(0, 4));
 		final ButtonGroup btnGroup = new ButtonGroup();
 		
 		for (String img : getResources().getImageKeyList(type)) {
 			if (type == ImageType.FENCE && img.endsWith("_1"))
 				continue;
 			
 			ImageIcon imgIcon = new ImageIcon(getResources().getSelectorImage(type, img).getScaledInstance(48, 48, Image.SCALE_DEFAULT));
 			ItemSelectionPanel itemPnl = new ItemSelectionPanel(this, img, imgIcon);
 			
 			JRadioButton btn = itemPnl.getSelectionButton();
 			btnGroup.add(itemPnl.getSelectionButton());
 			
 			btn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					Enumeration<AbstractButton> buttons = btnGroup.getElements();
 					
 					while (buttons.hasMoreElements()) {
 						JRadioButton btn = (JRadioButton) buttons.nextElement();
 						btn.setBorderPainted(false);
 				
 						if (btn.isSelected()) {
 							btn.setBorderPainted(true);
 							switch (type) {
 								case TERRAIN:
 									setSelectedTerrain(btn.getActionCommand());
 									break;
 								case OBJECT:
 									setSelectedObject(btn.getActionCommand());
 									break;
 								case FENCE:
 									setSelectedFence(btn.getActionCommand());
 									break;
 								default:
 									break;
 							}
 						}
 					}
 				}
 			});
 			
 			pnl.add(itemPnl);
 		}
 		
 		switch (type) {
 			case TERRAIN:
 				terrainGroup = btnGroup;
 				break;
 			case OBJECT:
 				objectGroup = btnGroup;
 				break;
 			case FENCE:
 				fenceGroup = btnGroup;
 				break;
 			default:
 				break;
 		}
 		
 		JScrollPane scrollTerrain = new JScrollPane(pnl, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollTerrain.getVerticalScrollBar().setUnitIncrement(WPlanner.getConfig().getScrollBarIncrement());
 		
 		tabSelector.addTab(name, scrollTerrain);
 	}
 	
 	public void init() {
 		resources = new ResourceManager();
 	}
 	
 	public void addMap(Map m) {
 		GraphicPanel gfx = new GraphicPanel(this, m);
 		JScrollPane graphicScroll = new JScrollPane(gfx, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		graphicScroll.getVerticalScrollBar().setUnitIncrement(WPlanner.getConfig().getScrollBarIncrement());
 		
 		tabEditor.addTab(m.getMapName(), graphicScroll);
 		tabEditor.setTabComponentAt(tabEditor.getTabCount() - 1, new ClosableTab(this, m.getMapName()));
 		tabEditor.setSelectedIndex(tabEditor.getTabCount() - 1);
 		
 		getSelectedToolButton().doClick();
 	}
 	
 	public void removeMap(String mapName) {
 		for (int i = 0; i < tabEditor.getTabCount(); i++) {
 			if (tabEditor.getTitleAt(i).equals(mapName))
 				tabEditor.remove(i);
 		}
 	}
 	
 	public ResourceManager getResources() {
 		return resources;
 	}
 
 	public void setSelectedTerrain(String terrainName) {
 		selectedTerrain = getResources().getTerrainId(terrainName);
 	}
 	
 	public void forceSelection(ImageType type, int id) {
 		String btnName = getResources().getImageKeyList(type).get(id);
 		
 		Enumeration<AbstractButton> buttons; 
 		
 		switch (type) {
 			case TERRAIN:
 				buttons = terrainGroup.getElements();
 				break;
 			case OBJECT:
 				buttons = objectGroup.getElements();
 				break;
 			case FENCE:
 				buttons = fenceGroup.getElements();
 				break;
 			default:
 				return;
 		}
 		
 		while (buttons.hasMoreElements()) {
 			JRadioButton btn = (JRadioButton) buttons.nextElement();
 	
 			if (btn.getActionCommand().equals(btnName)) {
 				btn.doClick();
 				return;
 			}
 		}
 	}
 	
 	public int getSelectedTerrain() {
 		return selectedTerrain;
 	}
 	
 	public void setSelectedObject(String objectName) {
 		selectedObject = getResources().getObjectId(objectName);
 	}
 	
 	public int getSelectedObject() {
 		return selectedObject;
 	}
 	
 	public void setSelectedFence(String fenceName) {
 		selectedFence = getResources().getFenceId(fenceName);
 	}
 	
 	public int getSelectedFence() {
 		return selectedFence;
 	}
 
 	public boolean isCaveEntrance(int tileType) {
 		return tileType == getResources().getTerrainId("distance-cave.jpg");
 	}
 	
 	public void setBrushSize(int newSize) {
 		brushSize = newSize;
 	}
 	
 	public int getBrushSize() {
 		return brushSize;
 	}
 	
 	public void setOverlayColor(Color c) {
 		overlayColor = c;
 	}
 
 	public Color getOverlayColor() {
 		return overlayColor;
 	}
 	
 	public void setLabelColor(Color c) {
 		labelColor = c;
 	}
 	
 	public Color getLabelColor() {
 		return labelColor;
 	}
 	
 	public void closeMaps() {
 		WPlanner.getMapManager().removeAllMaps();
 	}
 	
 	public void saveWindowSize() {
 		WPlanner.getConfig().setWindowWidth(this.getWidth(), true);
 		WPlanner.getConfig().setWindowHeight(this.getHeight(), true);
 	}
 	
 	private void updateSelectedTool() {
 		Enumeration<AbstractButton> buttons = toolGroup.getElements();
 		while (buttons.hasMoreElements()) {
 			JRadioButton btn = (JRadioButton) buttons.nextElement();
 			btn.setBorderPainted(false);
 	
 			if (btn.isSelected())
 				btn.setBorderPainted(true);
 		}
 	}
 	
 	public JRadioButton getToolButton(String tool) {
 		Enumeration<AbstractButton> buttons = toolGroup.getElements();
 		
 		while (buttons.hasMoreElements()) {
 			JRadioButton btn = (JRadioButton) buttons.nextElement();
 			if (btn.getActionCommand().equals(tool))
 				return btn;
 		}
 		
 		return null;
 	}
 	
 	public JRadioButton getSelectedToolButton() {
 		Enumeration<AbstractButton> buttons = toolGroup.getElements();
 		
 		while (buttons.hasMoreElements()) {
 			JRadioButton btn = (JRadioButton) buttons.nextElement();
 			if (btn.isSelected())
 				return btn;
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String actionCmd = e.getActionCommand();
 		
 		if (tabEditor ==  null)
 			return;
 		GraphicPanel selectedPanel = null;
 		if (tabEditor.getTabCount() > 0)
 			selectedPanel = (GraphicPanel) ((JScrollPane) tabEditor.getSelectedComponent()).getViewport().getComponent(0);
 		
 		if (actionCmd.equals(CMD_NEW)) {
 			new NewDialog();
 			return;
 		} else if (actionCmd.equals(CMD_OPEN)) {
 			JFileChooser jfc = new JFileChooser(WPlanner.getConfig().getDefaultSaveDir());
 			jfc.setFileFilter(new FileFilter() {
 				@Override
 				public boolean accept(File f) {
 					if (f.isDirectory())
 						return true;
 					
 					if (f.getAbsolutePath().endsWith(".wp2"))
 						return true;
 					
 					return false;
 				}
 
 				@Override
 				public String getDescription() {
 					return "WPlanner Map (.wp2)";
 				}
 			});
 			
 			int retVal = jfc.showOpenDialog(this);
 			if (retVal == JFileChooser.APPROVE_OPTION) {
 				String mapFile = WPlanner.getMapManager().loadMap(jfc.getSelectedFile().getPath());
 				WPlanner.getMainWindow().addMap(WPlanner.getMapManager().getMap(mapFile));
 			}
 			return;
 		} else if (actionCmd.equals(CMD_EXIT)) {
 			closeMaps();
 			saveWindowSize();
 			System.exit(NORMAL);
 			return;
 		} else if (actionCmd.equals(CMD_HELP)) {
 			new HelpDialog();
 			return;
 		} else if (actionCmd.equals(CMD_ABOUT)) {
 			new AboutDialog();
 			return;
 		}
 		
 		updateSelectedTool();
 
 		if (selectedPanel == null)
 			return;
 		
 		String mapName = tabEditor.getTitleAt(tabEditor.getSelectedIndex());
 		
 		if (actionCmd.equals(CMD_SAVE)) {
 			WPlanner.getMapManager().saveMap(mapName);
 			return;
 		} else if (actionCmd.equals(CMD_SAVEAS)) {
 			WPlanner.getMapManager().openSaveDialog(mapName);
 			return;
 		} else if (actionCmd.equals(CMD_SAVEIMG)) {
 			WPlanner.getMapManager().saveMapImage(mapName, selectedPanel.getMapImage(), false);
 			return;
 		} else if (actionCmd.equals(CMD_SAVEIMGAS)) {
 			WPlanner.getMapManager().saveMapImage(mapName, selectedPanel.getMapImage(), true);			
 			return;
 		}
 		
 		if (actionCmd.equals(CMD_UNDO)) {
 			selectedPanel.undo();
 		} else if (actionCmd.equals(CMD_REDO)) {
 			selectedPanel.redo();
 		} else if (actionCmd.equals(CMD_RESIZEMAP)) {
 			selectedPanel.resizeMap();		
 		} else if (actionCmd.equals(CMD_ZOOMIN)) {
 			selectedPanel.zoomIn();
 		} else if (actionCmd.equals(CMD_ZOOMOUT)) {
 			selectedPanel.zoomOut();
 		} else if (actionCmd.equals(CMD_TERRAIN_PENCIL)) {
 			selectedPanel.setEditState(EditState.TERRAIN_PENCIL);
 		} else if (actionCmd.equals(CMD_TERRAIN_BRUSH)) {
 			selectedPanel.setEditState(EditState.TERRAIN_BRUSH);
 		} else if (actionCmd.equals(CMD_TERRAIN_FILL)) {
 			selectedPanel.setEditState(EditState.TERRAIN_FILL);
 		} else if (actionCmd.equals(CMD_TERRAIN_ERASER)) {
 			selectedPanel.setEditState(EditState.TERRAIN_ERASER);
 		} else if (actionCmd.equals(CMD_TERRAIN_PICKER)) {
 			selectedPanel.setEditState(EditState.TERRAIN_PICKER);
 		} else if (actionCmd.equals(CMD_OBJECTS_PENCIL)) {
 			selectedPanel.setEditState(EditState.OBJECT_PENCIL);
 		} else if (actionCmd.equals(CMD_OBJECTS_ERASER)) {
 			selectedPanel.setEditState(EditState.OBJECT_ERASER);
 		} else if (actionCmd.equals(CMD_OBJECTS_PICKER)) {
 			selectedPanel.setEditState(EditState.OBJECT_PICKER);
 		} else if (actionCmd.equals(CMD_FENCE_PENCIL)) {
 			selectedPanel.setEditState(EditState.FENCE_PENCIL);
 		} else if (actionCmd.equals(CMD_FENCE_LINE)) {
 			selectedPanel.setEditState(EditState.FENCE_LINE);
 		} else if (actionCmd.equals(CMD_FENCE_ERASER)) {
 			selectedPanel.setEditState(EditState.FENCE_ERASER);
 		} else if (actionCmd.equals(CMD_FENCE_PICKER)) {
 			selectedPanel.setEditState(EditState.FENCE_PICKER);
 		} else if (actionCmd.equals(CMD_LABEL)) {
 			selectedPanel.setEditState(EditState.LABEL);
 		} else if (actionCmd.equals(CMD_OVERLAY_PENCIL)) {
 			selectedPanel.setEditState(EditState.OVERLAY_PENCIL);
 		} else if (actionCmd.equals(CMD_OVERLAY_BRUSH)) {
 			selectedPanel.setEditState(EditState.OVERLAY_BRUSH);
 		} else if (actionCmd.equals(CMD_OVERLAY_ERASER)) {
 			selectedPanel.setEditState(EditState.OVERLAY_ERASER);
 		} else if (actionCmd.equals(CMD_OVERLAY_PICKER)) {
 			selectedPanel.setEditState(EditState.OVERLAY_PICKER);
 		} else if (actionCmd.equals(CMD_OVERLAY_COLOR)) {
 			new ColorDialog();
 		} else if (actionCmd.equals(CMD_LAYERUP)) {
 			selectedPanel.setCaveLayer(false);
 		} else if (actionCmd.equals(CMD_LAYERDOWN)) {
 			selectedPanel.setCaveLayer(true);
 		}
 	}
 	
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		String componentName = ((Component) e.getSource()).getName();
 		
 		if (componentName.equals(CMD_BRUSHSIZE)) {
 			setBrushSize((Integer) ((JSpinner) e.getSource()).getValue());
 		} else if (componentName.equals(TAB_SELECTOR)) {
 			switch (tabSelector.getSelectedIndex()) {
 				case 0:
 					getToolButton(CMD_TERRAIN_PENCIL).doClick();
 					break;
 				case 1:
 					getToolButton(CMD_OBJECTS_PENCIL).doClick();
 					break;
 				case 2:
 					getToolButton(CMD_FENCE_PENCIL).doClick();
 					break;
 				default:
 					break;	
 			}
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) { }
 
 	@Override
 	public void windowClosed(WindowEvent arg0) { }
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		closeMaps();
 		saveWindowSize();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) { }
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) { }
 
 	@Override
 	public void windowIconified(WindowEvent arg0) { }
 
 	@Override
 	public void windowOpened(WindowEvent arg0) { }
 }

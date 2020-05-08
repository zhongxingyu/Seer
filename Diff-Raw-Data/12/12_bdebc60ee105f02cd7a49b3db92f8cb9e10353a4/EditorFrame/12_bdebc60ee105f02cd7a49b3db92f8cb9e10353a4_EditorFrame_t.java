 /**
  * This file is part of Hazelnutt.
  * 
  * Hazelnutt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Hazelnutt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Hazelnutt.  If not, see <http://www.gnu.org/licenses/>.
  */
 package es.darkhogg.hazelnutt;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import es.darkhogg.bbcc2.BugsRom;
 import es.darkhogg.bbcc2.ComboType;
 import es.darkhogg.bbcc2.Enemy;
 import es.darkhogg.bbcc2.EnemyType;
 import es.darkhogg.bbcc2.EntityCollection;
 import es.darkhogg.bbcc2.Item;
 import es.darkhogg.bbcc2.ItemType;
 import es.darkhogg.bbcc2.Level;
 import es.darkhogg.bbcc2.RomLevel;
 import es.darkhogg.gameboy.Palette;
 import es.darkhogg.util.IntVector;
 
 @SuppressWarnings( "serial" )
 public class EditorFrame extends JFrame {
 
 	private static final Palette BUGS_PALETTE = new Palette(
 		null, new Color( 0x600060 ), new Color( 0xF860F8 ), new Color( 0xF8D0F8 )
 	);
 	
 	private static final Palette ITEMS_PALETTE = new Palette(
 		null, new Color( 0x603000 ), new Color( 0xF8B060 ), new Color( 0xF8E8D0 )
 	);
 	
 	private static final Palette DOOR_ITEMS_PALETTE = new Palette(
 		null, new Color( 0x006000 ), new Color( 0x60F860 ), new Color( 0xD0F8D0 )
 	);
 	
 	private static final Palette LIGHT_ENEMY_PALETTE = new Palette(
 		null, new Color( 0x600000 ), new Color( 0xF89090 ), new Color( 0xF8D0D0 )
 	);	
 	
 	private static final Palette DARK_ENEMY_PALETTE = new Palette(
 		null, new Color( 0x600000 ), new Color( 0xD83030 ), new Color( 0xF89090 )
 	);
 	
 	private final Configuration config;
 	private final Logger logger;
 	
 	private JPanel contentPane;
 	
 	private FileFilter hlfFileFilter = new FileFilter(){
 		@Override
 		public boolean accept ( File file ) {
 			if ( file.isDirectory() ) {
 				return true;
 			}
 			
 			String fname = file.getName();
 			int pos = fname.lastIndexOf( "." );
 			return pos >= 0
 				&& fname.substring( pos+1 ).equalsIgnoreCase( "HLF" );
 		}
 		@Override
 		public String getDescription () {
 			return "Hazelnutt Level Format (*.hlf)";
 		}
 	};
 	
 	private JCheckBoxMenuItem menuViewSpawn;
 	private JCheckBoxMenuItem menuViewItems;
 	private JCheckBoxMenuItem menuViewDoorItems;
 	private JCheckBoxMenuItem menuViewEnemies;
 	private JCheckBoxMenuItem menuViewTypes;
 	
 	private JToggleButton barViewSpawn;
 	private JToggleButton barViewItems;
 	private JToggleButton barViewDoorItems;
 	private JToggleButton barViewEnemies;
 	private JToggleButton barViewTypes;
 	private JMenu mnHelp;
 	private JMenuItem mntmAbout;
 	private JPanel panel;
 	private JMenuItem menuLoadRom;
 	private JMenuItem menuSaveRom;
 	private JMenuItem menuExit;
 	private JButton barLoadRom;
 	private JButton barSaveRom;
 	private PropertiesPanel propertiesPanel;
 	
 	private JFileChooser fileChooser, hlfFileChooser;
 	private File loadedFile = null;
 	private BugsRom loadedRom = null;
 	private Level selectedLevel = null;
 	private int selectedLevelNum = 0;
 	private LinkedList<String> recentFiles;
 	
 	private JMenuItem menuSaveLevel;
 	private JMenuItem menuReloadLevel;
 	private JMenuItem menuLoadLevel;
 	private JButton barLoadLevel;
 	private JButton barReloadLevel;
 	private JButton barSaveLevel;
 	private JMenuItem mntmCheckUpdates;
 	private JMenuItem mntmPreferences;
 	private JScrollPane scrollPane;
 	private LevelDisplay levelDisplay;
 	
 	private JToggleButton scaleButton;
 	private SelectorPanel selectorPanel;
 	
 	private boolean romHasChanged, levelHasChanged;
 	private boolean romFeaturesEnabled, levelFeaturesEnabled;
 	
 	private static final Map<ComboType,Image> helpSet;
 	private JPanel levelWrapper;
 	private JCheckBoxMenuItem scaleMenuItem;
 	private JMenuItem menuClearLevel;
 	private JMenuItem menuSaveRomAs;
 	private JMenuItem menuImportLevel;
 	private JMenuItem menuExportLevel;
 	private JButton barClearLevel;
 	private JButton barImportLevel;
 	private JButton barExportLevel;
 	private JMenu menuRecentFiles;
 	static {
 		try {
 			EnumMap<ComboType,Image> hs =
 				new EnumMap<ComboType,Image>( ComboType.class );
 		
 			BufferedImage iconSet = ImageIO.read( EditorFrame.class.getResource(
 				"/es/darkhogg/hazelnutt/help_icons.png" ) );
 			
 			hs.put( ComboType.DOOR, iconSet.getSubimage( 16, 0, 16, 16 ) );
 			hs.put( ComboType.FINAL, iconSet.getSubimage( 32, 0, 16, 16 ) );
 			hs.put( ComboType.ROPE_FLOOR, iconSet.getSubimage( 0, 0, 16, 16 ) );
 			hs.put( ComboType.STAIRS, iconSet.getSubimage( 0, 16, 16, 16 ) );
 			hs.put( ComboType.STAIRS_DOOR_LEFT, iconSet.getSubimage( 80, 0, 16, 16 ) );
 			hs.put( ComboType.STAIRS_DOOR_RIGHT, iconSet.getSubimage( 64, 0, 16, 16 ) );
 			hs.put( ComboType.UP_LEAP, iconSet.getSubimage( 48, 0, 16, 16 ) );
 			
 			helpSet = Collections.unmodifiableMap( hs );
 		} catch ( Throwable e ) {
 			throw new RuntimeException( e );
 		}
 	}
 	
 	/**
 	 * Create the frame. The GUI will be initialized on the Event Dispatch
 	 * thread. If the initialization is not completed, this constructor throws
 	 * an unspecified RuntimeException with its cause set to the exception that
 	 * originally caused the initialization error.
 	 * 
 	 * @throws InvocationTargetException 
 	 * @throws InterruptedException 
 	 */
 	public EditorFrame () {
 		config = Hazelnutt.getConfiguration();
 		logger = Hazelnutt.getLogger();
 		
 		//initializeGui();
 		try {
 			SwingUtilities.invokeAndWait( new Runnable(){
 				@Override public void run () {
 					initializeGui();
 				}
 			});
 		} catch ( InterruptedException e ) {
 			throw new RuntimeException( e );
 		} catch ( InvocationTargetException e ) {
 			throw new RuntimeException( e );
 		}
 	}
 	
 	private void initializeGui () {
 		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
 		setBounds( 100, 100, 640, 480 );
 		
 		addWindowListener(new WindowAdapter() {
 			@Override public void windowClosing(WindowEvent arg0) {
 				actionExit();
 			}
 		});
 
 		fileChooser = new JFileChooser();
 		//fileChooser.setFileFilter( hlfFileFilter );
 		
 		hlfFileChooser = new JFileChooser();
 		hlfFileChooser.setFileFilter( hlfFileFilter );
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		mnFile.setMnemonic('F');
 		menuBar.add(mnFile);
 		
 		menuLoadRom = new JMenuItem("Load ROM...");
 		menuLoadRom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
 		menuLoadRom.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionOpenRom( null );
 			}
 		});
 		menuLoadRom.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_open.png")));
 		menuLoadRom.setMnemonic('O');
 		mnFile.add(menuLoadRom);
 		
 		menuSaveRom = new JMenuItem("Save ROM");
 		menuSaveRom.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionSaveRom();
 			}
 		});
 		menuSaveRom.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_save.png")));
 		menuSaveRom.setMnemonic('S');
 		menuSaveRom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
 		mnFile.add(menuSaveRom);
 		
 		menuSaveRomAs = new JMenuItem("Save ROM As...");
 		menuSaveRomAs.setMnemonic('a');
 		menuSaveRomAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
 		menuSaveRomAs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionSaveRomAs();
 			}
 		});
 		menuSaveRomAs.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_save_as.png")));
 		mnFile.add(menuSaveRomAs);
 		
 		mnFile.addSeparator();
 		
 		menuLoadLevel = new JMenuItem("Load Level...");
 		menuLoadLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
 		menuLoadLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionLoadLevel();
 			}
 		});
 		menuLoadLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_load_level.png")));
 		menuLoadLevel.setMnemonic('L');
 		mnFile.add(menuLoadLevel);
 		
 		menuReloadLevel = new JMenuItem("Reload Level");
 		menuReloadLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
 		menuReloadLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionReloadLevel();
 			}
 		});
 		menuReloadLevel.setMnemonic('R');
 		menuReloadLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_reload_level.png")));
 		mnFile.add(menuReloadLevel);
 		
 		menuSaveLevel = new JMenuItem("Save Level");
 		menuSaveLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
 		menuSaveLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionSaveLevel();
 			}
 		});
 		menuSaveLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_save_level.png")));
 		menuSaveLevel.setMnemonic('v');
 		
 		mnFile.add(menuSaveLevel);
 
 		//*
 		mnFile.addSeparator();
 		
 		menuImportLevel = new JMenuItem("Import Level...");
 		menuImportLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
 		menuImportLevel.setMnemonic('i');
 		menuImportLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionImportLevel();
 			}
 		});
 		menuImportLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_import.png")));
 		mnFile.add(menuImportLevel);
 		
 		menuExportLevel = new JMenuItem("Export Level...");
 		menuExportLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
 		menuExportLevel.setMnemonic('e');
 		menuExportLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionExportLevel();
 			}
 		});
 		menuExportLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_export.png")));
 		mnFile.add(menuExportLevel);
 		//*/
 		
 		mnFile.addSeparator();
 		
 		menuExit = new JMenuItem("Exit");
 		menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
 		menuExit.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_exit.png")));
 		menuExit.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent arg0) {
 				actionExit();
 			}
 		});
 		
 		menuRecentFiles = new JMenu("Recent Files...");
 		mnFile.add(menuRecentFiles);
 		
 		mnFile.addSeparator();
 		menuExit.setMnemonic('X');
 		mnFile.add(menuExit);
 		
 		JMenu mnEdit = new JMenu("Edit");
 		mnEdit.setMnemonic('E');
 		menuBar.add(mnEdit);
 		
 		menuViewSpawn = new JCheckBoxMenuItem("View Spawn Point");
 		menuViewSpawn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
 		menuViewSpawn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( false );
 			}
 		});
 		
 		menuClearLevel = new JMenuItem("Clear Level");
 		menuClearLevel.setMnemonic('c');
 		menuClearLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
 		menuClearLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionClearLevel();
 			}
 		});
 		menuClearLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_clear_level.png")));
 		mnEdit.add(menuClearLevel);
 		
 		mnEdit.addSeparator();
 		
 		menuViewSpawn.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_spawn.png")));
 		menuViewSpawn.setMnemonic('S');
 		menuViewSpawn.setSelected(true);
 		mnEdit.add(menuViewSpawn);
 		
 		menuViewItems = new JCheckBoxMenuItem("View Items");
 		menuViewItems.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
 		menuViewItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( false );
 			}
 		});
 		menuViewItems.setSelected(true);
 		menuViewItems.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_items.png")));
 		menuViewItems.setMnemonic('I');
 		mnEdit.add(menuViewItems);
 		
 		menuViewDoorItems = new JCheckBoxMenuItem("View Door Items");
 		menuViewDoorItems.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
 		menuViewDoorItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( false );
 			}
 		});
 		menuViewDoorItems.setSelected(true);
 		menuViewDoorItems.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_door_items.png")));
 		menuViewDoorItems.setMnemonic('D');
 		mnEdit.add(menuViewDoorItems);
 		
 		menuViewEnemies = new JCheckBoxMenuItem("View Enemies");
 		menuViewEnemies.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
 		menuViewEnemies.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( false );
 			}
 		});
 		menuViewEnemies.setSelected(true);
 		menuViewEnemies.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_enemies.png")));
 		menuViewEnemies.setMnemonic('E');
 		mnEdit.add(menuViewEnemies);
 		
 		menuViewTypes = new JCheckBoxMenuItem("View Combo Types");
 		menuViewTypes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0));
 		menuViewTypes.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( false );
 			}
 		});
 		menuViewTypes.setSelected(true);
 		menuViewTypes.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_combo_infos.png")));
 		menuViewTypes.setMnemonic('C');
 		mnEdit.add(menuViewTypes);
 		
 		mnEdit.addSeparator();
 		
 		scaleMenuItem = new JCheckBoxMenuItem("Scale Level Display");
 		scaleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0));
 		scaleMenuItem.setMnemonic('l');
 		scaleMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionScale( false );
 			}
 		});
 		scaleMenuItem.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_x2.png")));
 		mnEdit.add(scaleMenuItem);
 		
 		//mnEdit.addSeparator();
 		
 		mntmPreferences = new JMenuItem("Preferences...");
 		mntmPreferences.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionPreferences();
 			}
 		});
 		mntmPreferences.setMnemonic('P');
 		mntmPreferences.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_preferences.png")));
 		//mnEdit.add(mntmPreferences);
 		
 		mnHelp = new JMenu("Help");
 		mnHelp.setMnemonic('H');
 		menuBar.add(mnHelp);
 		
 		mntmCheckUpdates = new JMenuItem("Check for Updates...");
 		mntmCheckUpdates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
 		mntmCheckUpdates.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionCheckUpdates();
 			}
 		});
 		mntmCheckUpdates.setMnemonic('c');
 		mntmCheckUpdates.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_update.png")));
 		mnHelp.add(mntmCheckUpdates);
 		
 		mntmAbout = new JMenuItem("Readme...");
 		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 		mntmAbout.setMnemonic('r');
 		mntmAbout.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/about.png")));
 		mntmAbout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionAbout();
 			}
 		});
 		mnHelp.addSeparator();
 		mnHelp.add(mntmAbout);		
 		
 		contentPane = new JPanel();
 		contentPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
 		contentPane.setLayout( new BorderLayout( 0, 0 ) );
 		setContentPane( contentPane );
 		
 		JToolBar toolBar = new JToolBar();
 		toolBar.setRollover(true);
 		toolBar.setFloatable(false);
 		contentPane.add(toolBar, BorderLayout.NORTH);
 		
 		barLoadRom = new JButton("");
 		barLoadRom.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionOpenRom( null );
 			}
 		});
 		barLoadRom.setToolTipText("Open");
 		barLoadRom.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_open.png")));
 		toolBar.add(barLoadRom);
 		
 		barSaveRom = new JButton("");
 		barSaveRom.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionSaveRom();
 			}
 		});
 		barSaveRom.setToolTipText("Save");
 		barSaveRom.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_save.png")));
 		toolBar.add(barSaveRom);
 		
 		toolBar.addSeparator();
 		
 		barImportLevel = new JButton("");
 		barImportLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionImportLevel();
 			}
 		});
 		barImportLevel.setToolTipText("Import Level");
 		barImportLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_import.png")));
 		toolBar.add(barImportLevel);
 		
 		barExportLevel = new JButton("");
 		barExportLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionExportLevel();
 			}
 		});
 		barExportLevel.setToolTipText("Export Level");
 		barExportLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_export.png")));
 		toolBar.add(barExportLevel);
 		
 		toolBar.addSeparator();
 		
 		barLoadLevel = new JButton("");
 		barLoadLevel.setToolTipText("Load Level");
 		barLoadLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionLoadLevel();
 			}
 		});
 		barLoadLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_load_level.png")));
 		toolBar.add(barLoadLevel);
 		
 		barReloadLevel = new JButton("");
 		barReloadLevel.setToolTipText("Reload Current Level");
 		barReloadLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionReloadLevel();
 			}
 		});
 		barReloadLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_reload_level.png")));
 		toolBar.add(barReloadLevel);
 		
 		barSaveLevel = new JButton("");
 		barSaveLevel.setToolTipText("Save Level");
 		barSaveLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionSaveLevel();
 			}
 		});
 		barSaveLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_save_level.png")));
 		toolBar.add(barSaveLevel);
 		
 		toolBar.addSeparator();
 		
 		barViewItems = new JToggleButton("");
 		barViewItems.setToolTipText("Toggle View Items");
 		barViewItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( true );
 			}
 		});
 		
 		barViewSpawn = new JToggleButton("");
 		barViewSpawn.setToolTipText("Toggle View Spawn");
 		barViewSpawn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( true );
 			}
 		});
 		
 		barClearLevel = new JButton("");
 		barClearLevel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionClearLevel();
 			}
 		});
 		barClearLevel.setToolTipText("Clear Level");
 		barClearLevel.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_clear_level.png")));
 		toolBar.add(barClearLevel);
 		
 		toolBar.addSeparator();
 		
 		barViewSpawn.setSelected(true);
 		barViewSpawn.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_spawn.png")));
 		toolBar.add(barViewSpawn);
 		barViewItems.setSelected(true);
 		barViewItems.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_items.png")));
 		toolBar.add(barViewItems);
 		
 		barViewDoorItems = new JToggleButton("");
 		barViewDoorItems.setToolTipText("Toggle View Door Items");
 		barViewDoorItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( true );
 			}
 		});
 		barViewDoorItems.setSelected(true);
 		barViewDoorItems.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_door_items.png")));
 		toolBar.add(barViewDoorItems);
 		
 		barViewEnemies = new JToggleButton("");
 		barViewEnemies.setToolTipText("toggle View Enemies");
 		barViewEnemies.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( true );
 			}
 		});
 		barViewEnemies.setSelected(true);
 		barViewEnemies.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_enemies.png")));
 		toolBar.add(barViewEnemies);
 		
 		barViewTypes = new JToggleButton("");
 		barViewTypes.setToolTipText("Toggle view Combo Help");
 		barViewTypes.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionToggle( true );
 			}
 		});
 		barViewTypes.setSelected(true);
 		barViewTypes.setIcon(new ImageIcon(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/icon_combo_infos.png")));
 		toolBar.add(barViewTypes);
 		
 		barViewSpawn.setSelected( config.getBoolean( "Hazelnutt.gui.viewSpawn", true ) );
 		barViewItems.setSelected( config.getBoolean( "Hazelnutt.gui.viewItems", true ) );
 		barViewDoorItems.setSelected( config.getBoolean( "Hazelnutt.gui.viewDoorItems", true ) );
 		barViewEnemies.setSelected( config.getBoolean( "Hazelnutt.gui.viewEnemies", true ) );
 		barViewTypes.setSelected( config.getBoolean( "Hazelnutt.gui.viewHelp", true ) );
 		
 		panel = new JPanel();
 		contentPane.add(panel, BorderLayout.EAST);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		
 		selectorPanel = new SelectorPanel();
 		selectorPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Combo & Entity Selector", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 70, 213)));
 		panel.add(selectorPanel);
 		
 		propertiesPanel = new PropertiesPanel( null );
 		propertiesPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Level Properties", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 70, 213)));
 		panel.add(propertiesPanel);
 		
 		toolBar.addSeparator();
 		
 		scaleButton = new JToggleButton("");
 		scaleButton.setToolTipText("Toggle Scale Display");
 		scaleButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				actionScale( true );
 			}
 		});
 		scaleButton.setIcon(new ImageIcon(EditorFrame.class.getResource("/es/darkhogg/hazelnutt/icon_x2.png")));
 		toolBar.add(scaleButton);
 		
 		scaleButton.setSelected( config.getBoolean( "Hazelnutt.gui.scaled", false ) );
 		
 		Component horizontalGlue = Box.createHorizontalGlue();
 		toolBar.add(horizontalGlue);
 		
 		Component verticalGlue = Box.createVerticalGlue();
 		panel.add(verticalGlue);
 		
 		levelWrapper = new JPanel();
 		levelWrapper.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Level", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 70, 213)));
 		contentPane.add(levelWrapper, BorderLayout.CENTER);
 		levelWrapper.setLayout(new BorderLayout(0, 0));
 
 		scrollPane = new JScrollPane();
 		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		levelWrapper.add(scrollPane);
 		
 		levelDisplay = new LevelDisplay();
 		levelDisplay.addEditListener( new EditListener (){
 			@Override public void leftPressed ( int x, int y ) {
 				SelectionType st = selectorPanel.getSelectionType();
 				Object so = selectorPanel.getSelectedObject();
 				
 				if ( st == SelectionType.COMBO ) {
 					paintComboAt( x, y, ( (Byte) so ).byteValue() );
 				} else if ( st == SelectionType.SPAWN ) {
 					selectedLevel.getRomLevel().setSpawn(
 						new IntVector( x, y ) );

					levelHasChanged = true;
 				} else if ( st == SelectionType.ITEM ) {
 					Item ent = new Item( (ItemType) so, x, y );
 					EntityCollection<Item> ents = selectedLevel.getRomLevel().getItems();
 					for ( Iterator<Item> it = ents.iterator(); it.hasNext(); ) {
 						Item item = it.next();
 						if ( item.getX() == x && item.getY() == y ) {
 							it.remove();
 						}
 					}
 					if ( ents.size() <= ents.maxSize() ) {
 						ents.add( ent );
						levelHasChanged = true;
 					}
 				} else if ( st == SelectionType.DOOR_ITEM ) {
 					Item ent = new Item( (ItemType) so, x, y );
 					EntityCollection<Item> ents = selectedLevel.getRomLevel().getDoorItems();
 					for ( Iterator<Item> it = ents.iterator(); it.hasNext(); ) {
 						Item item = it.next();
 						if ( item.getX() == x && item.getY() == y ) {
 							it.remove();
 						}
 					}
 					if ( ents.size() <= ents.maxSize() ) {
 						ents.add( ent );
						levelHasChanged = true;
 					}
 				} else if ( st == SelectionType.ENEMY ) {
 					Enemy ent = new Enemy( (EnemyType) so, x, y );
 					EntityCollection<Enemy> ents = selectedLevel.getRomLevel().getEnemies();
 					for ( Iterator<Enemy> it = ents.iterator(); it.hasNext(); ) {
 						Enemy enem = it.next();
 						if ( enem.getX() == x && enem.getY() == y ) {
 							it.remove();
 						}
 					}
 					if ( ents.size() <= ents.maxSize() ) {
 						ents.add( ent );
						levelHasChanged = true;
 					}
 				}
 
 				updateTitle();
 				levelDisplay.repaint();
 				levelHasChanged = true;
 			}
 			@Override public void centerPressed ( int x, int y ) {
 				selectorPanel.forceComboSelection(
 					selectedLevel.getRomLevel().getData()[x][y]
 				);
 			}
 			@Override public void rightPressed ( int x, int y ) {
 				deleteVisibleEntitiesAt( x, y );
 			}
 			@Override public void leftDragged ( int x, int y ) {
 				if ( selectorPanel.getSelectionType() == SelectionType.COMBO ) {
 					paintComboAt( x, y,
 						((Byte)selectorPanel.getSelectedObject()).byteValue() );
 				}
 			}
 			@Override public void centerDragged ( int x, int y ) {
 				// Nothing to de here
 			}
 			@Override public void rightDragged ( int x, int y ) {
 				deleteVisibleEntitiesAt( x, y );
 			}
 			
 			
 			// Utility methods
 			/*private void selectComboAt ( int x, int y ) {
 				
 			}/**/
 			private void paintComboAt ( int x, int y, byte value ) {
 				selectedLevel.getRomLevel().getData()[ x ][ y ] = value;
 				levelHasChanged = true;
 				
 				updateTitle();
 				levelDisplay.repaint();
 			}
 			private void deleteVisibleEntitiesAt ( int x, int y ) {
 				Collection<Item> items = selectedLevel.getRomLevel().getItems();
 				Collection<Item> doorItems = selectedLevel.getRomLevel().getDoorItems();
 				Collection<Enemy> enemies = selectedLevel.getRomLevel().getEnemies();
 				
 				if ( barViewItems.isSelected() ) {
 					for ( Iterator<Item> it = items.iterator(); it.hasNext(); ) {
 						Item item = it.next();
 						if ( item.getX() == x && item.getY() == y ) {
 							it.remove();
 							levelHasChanged = true;
 						}
 					}
 				}
 				
 				if ( barViewDoorItems.isSelected() ) {
 					for ( Iterator<Item> it = doorItems.iterator(); it.hasNext(); ) {
 						Item doorItem = it.next();
 						if ( doorItem.getX() == x && doorItem.getY() == y ) {
 							it.remove();
							levelHasChanged = true;
 						}
 					}
 				}
 				
 				if ( barViewEnemies.isSelected() ) {
 					for ( Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
 						Enemy enem = it.next();
 						if ( enem.getX() == x && enem.getY() == y ) {
 							it.remove();
							levelHasChanged = true;
 						}
 					}
 				}
 
 				updateTitle();
 				levelDisplay.repaint();
 			}
 		});
 		scrollPane.setViewportView(levelDisplay);
 		
 		updateTitle();
 		actionToggle( true );
 		actionScale( true );
 		setIconImage(
 			Toolkit.getDefaultToolkit().getImage(EditorFrame.class.getResource(
 			"/es/darkhogg/hazelnutt/witch_hazel_big.png")));
 		setRomFeaturesEnabled( false );
 		setLevelFeaturesEnabled( false );
 		
 		if ( config.getBoolean( "Hazelnutt.gui.maximum", false ) ) {
 			setExtendedState( getExtendedState() | JFrame.MAXIMIZED_BOTH );
 		} else {
 			int x = config.getInt( "Hazelnutt.gui.location.x", Integer.MIN_VALUE );
 			int y = config.getInt( "Hazelnutt.gui.location.y", Integer.MIN_VALUE );
 			int w = config.getInt( "Hazelnutt.gui.size.width", Integer.MIN_VALUE );
 			int h = config.getInt( "Hazelnutt.gui.size.height", Integer.MIN_VALUE );
 			
 			if (
 				x == Integer.MIN_VALUE || x == Integer.MIN_VALUE
 			 || w == Integer.MIN_VALUE || h == Integer.MIN_VALUE
 			) {
 				setLocationRelativeTo( null );
 			} else {
 				setLocation( x, y );
 				setSize( w, h );
 			}
 		}
 		
 		propertiesPanel.addApplyListener( new PropertiesPanel.ApplyListener(){
 			@Override
 			public void apply () {
 				levelHasChanged = true;
 				updateTitle();
 				updateDisplay();
 			}
 		});
 		
 		if ( config.containsKey( "Hazelnutt.gui.lastDirectory" ) ) {
 			File lastDir = new File( config.getString( "Hazelnutt.gui.lastDirectory" ) );
 			if ( lastDir.exists() && lastDir.isDirectory() ) {
 				//logger.debug( "Resetting the last directory: '" + lastDir + "'" );
 				fileChooser.setCurrentDirectory( lastDir );
 			}
 		}
 		
 		// Configure the PgUp and PgDn shortcuts
 		InputMap im = contentPane.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
 		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_UP, 0 ), "PgUp" );
 		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_DOWN, 0 ), "PgDn" );
 		ActionMap am = contentPane.getActionMap();
 		am.put( "PgUp", new AbstractAction(){
 			@Override public void actionPerformed ( ActionEvent arg0 ) {
 				actionLevelUp();
 			}
 		});
 		am.put( "PgDn", new AbstractAction(){
 			@Override public void actionPerformed ( ActionEvent arg0 ) {
 				actionLevelDown();
 			}
 		});
 		
 		// Get the recent files
 		recentFiles = new LinkedList<String>(
 			Arrays.asList(
 				config.getStringArray( "Hazelnutt.gui.recentFiles" )
 			)
 		);
 		updateRecentFiles();
 		
 	}
 
 	private void selectLevelNum ( int num ) {
 		logger.trace( "Trying to select level #" + (num+1) );
 		
 		if ( num >= 0 && num < 29 ) {
 			// Level number is correct
 			logger.info( "Level #" + (num+1) + " selected" );
 			
 			selectedLevelNum = num;
 			selectLevel( new Level( loadedRom.getLevel( num ) ) );
 			
 		} else {
 			// Level number is incorrect
 			logger.warn( "Level #" + (num+1) + " is an incorrect level" );
 			
 			selectLevel( null );
 		}
 
 		updateTitle();
 	}
 	
 	private void selectLevel ( Level level ) {
 		selectedLevel = level;
 		
 		if ( level == null ) {
 			setLevelFeaturesEnabled( false );
 		} else {
 			levelHasChanged = false;
 			setLevelFeaturesEnabled( true );
 		}
 		
 		propertiesPanel.setLevel( selectedLevel );
 		levelDisplay.setLevel( selectedLevel );
 		updateDisplay();
 		actionToggle( false );
 	}
 	
 	private void updateDisplay () {
 		if ( selectedLevel != null ) {
 			levelDisplay.setComboSet(
 				loadedRom.getThemeSpriteSet(
 					selectedLevel.getRomLevel().getTheme().getValue() ),
 				BugsRom.LEVEL_PALETTE
 			);
 			levelDisplay.setBugsSprite(
 				loadedRom.getBugsSprite(), BUGS_PALETTE );
 			levelDisplay.setItemSet(
 				loadedRom.getItemSpriteSet(), ITEMS_PALETTE, DOOR_ITEMS_PALETTE );
 			levelDisplay.setEnemySet(
 				loadedRom.getEnemySpriteSet(
 					selectedLevel.getEnemyGroup().getValue()-3 ),
 				LIGHT_ENEMY_PALETTE, DARK_ENEMY_PALETTE
 			);
 			levelDisplay.setHelpSet( helpSet );
 
 			selectorPanel.comboSelector.setComboSet( levelDisplay.getComboSet() );
 			selectorPanel.comboSelector.setHelpSet( levelDisplay.getHelpSet() );
 			
 			selectorPanel.entitySelector.setSpawnImage( levelDisplay.getSpawnImage() );
 			selectorPanel.entitySelector.setItemSet( levelDisplay.getItemSet() );
 			selectorPanel.entitySelector.setDoorItemSet( levelDisplay.getDoorItemSet() );
 			selectorPanel.entitySelector.setEnemySet( levelDisplay.getEnemySet() );
 			selectorPanel.entitySelector.updateDisplay();
 		}
 	}
 	
 	private void updateTitle () {
 		setTitle( "Hazelnutt "
 			+ (loadedRom==null?"":" - "+loadedFile)
 			+ (romHasChanged?"*":"")
 			+ (selectedLevel==null?"":" - Level "+(selectedLevelNum+1))
 			+ (levelHasChanged?"*":"")
 		);
 		
 		updateSaveFeatures();
 	}
 	
 	private void updateSaveFeatures () {
 		if ( romFeaturesEnabled ) {
 			menuSaveRom.setEnabled( romHasChanged );
 			barSaveRom.setEnabled( romHasChanged );
 		}
 		if ( levelFeaturesEnabled ) {
 			menuReloadLevel.setEnabled( levelHasChanged );
 			menuSaveLevel.setEnabled( levelHasChanged );
 			barReloadLevel.setEnabled( levelHasChanged );
 			barSaveLevel.setEnabled( levelHasChanged );
 		}
 	}
 	
 	private void updateRecentFiles () {
 		// Clean
 		menuRecentFiles.removeAll();
 		
 		// Remove while recentFiles > N
 		while ( recentFiles.size() >= 8 ) {
 			recentFiles.removeLast();
 		}
 		
 		// Add every menu item
 		if ( recentFiles != null ) {
 			for ( String str : recentFiles ) {
 				final JMenuItem mi = new JMenuItem( str );
 				menuRecentFiles.add( mi );
 				mi.addActionListener( new ActionListener(){
 					@Override public void actionPerformed ( ActionEvent ae ) {
 						String fileName = mi.getText();
 						actionOpenRom( fileName );
 					}
 				});
 			}
 		}
 	}
 	
 	protected boolean checkRomModified () {
 		if ( !checkLevelModified() ) {
 			return false;
 		}
 		
 		if ( romHasChanged ) {
 			int res = JOptionPane.showConfirmDialog( this,
 				"The currently open ROM has modified levels but is not saved to disk.\n" +
 					"Do you want to save it now?",
 				"Confirm ROM Changes",
 				JOptionPane.YES_NO_CANCEL_OPTION );
 			if ( res == JOptionPane.YES_OPTION ) {
 				actionSaveRom();
 			} else if ( res == JOptionPane.NO_OPTION ) {
 				
 			} else {
 				return false;
 			}
 		}
 		
 		updateTitle();
 		return true;
 	}
 	
 	private boolean checkLevelModified () {
 		if ( !checkPropertiesModified() ) {
 			return false;
 		}
 		
 		if ( levelHasChanged ) {
 			int res = JOptionPane.showConfirmDialog( this,
 				"The currently selected level has changed but not saved to the ROM.\n" +
 					"Do you want to save it now?",
 				"Confirm Level Changes",
 				JOptionPane.YES_NO_CANCEL_OPTION );
 			if ( res == JOptionPane.YES_OPTION ) {
 				loadedRom.setLevel( selectedLevelNum, selectedLevel );
 				levelHasChanged = false;
 				romHasChanged = true;
 			} else if ( res == JOptionPane.NO_OPTION ) {
 				
 			} else {
 				return false;
 			}
 		}
 		
 		updateTitle();
 		return true;
 	}
 	
 	private boolean checkPropertiesModified () {
 		if ( propertiesPanel.hasChanged() ) {
 			int res = JOptionPane.showConfirmDialog( this,
 				"Properties for this level were changed but not applied.\n" +
 					"Do you want to apply them now?",
 				"Confirm Properties Changes",
 				JOptionPane.YES_NO_CANCEL_OPTION );
 			if ( res == JOptionPane.YES_OPTION ) {
 				propertiesPanel.actionApply();
 			} else if ( res == JOptionPane.NO_OPTION ) {
 				propertiesPanel.actionReset();
 			} else {
 				return false;
 			}
 		}
 		
 		updateTitle();
 		return true;
 	}
 	
 	private void actionScale ( boolean toolBar ) {
 		if ( toolBar ) {
 			scaleMenuItem.setSelected( scaleButton.isSelected() );
 		} else {
 			scaleButton.setSelected( scaleMenuItem.isSelected() );
 		}
 		
 		boolean selected = scaleButton.isSelected();
 		levelDisplay.setScale( selected?2:1 );
 		config.setProperty( "Hazelnutt.gui.scaled", selected );
 	}
 	
 	private void actionPreferences () {
 		ConfigDialog cd = new ConfigDialog();
 		cd.setLocationRelativeTo( null );
 		cd.setVisible( true );
 	}
 
 	private void setRomFeaturesEnabled ( boolean enabled ) {
 		romFeaturesEnabled = enabled;
 		
 		menuSaveRom.setEnabled( enabled );
 		menuSaveRomAs.setEnabled( enabled );
 		menuLoadLevel.setEnabled( enabled );
 		barSaveRom.setEnabled( enabled );
 		barLoadLevel.setEnabled( enabled );
 
 		if ( !enabled ) {
 			setLevelFeaturesEnabled( false );
 		}
 		
 		updateSaveFeatures();
 	}
 	
 	private void setLevelFeaturesEnabled ( boolean enabled ) {
 		levelFeaturesEnabled = enabled;
 		
 		menuReloadLevel.setEnabled( enabled );
 		menuSaveLevel.setEnabled( enabled );
 
 		menuExportLevel.setEnabled( enabled );
 		menuImportLevel.setEnabled( enabled );
 
 		menuClearLevel.setEnabled( enabled );
 		
 		menuViewSpawn.setEnabled( enabled );
 		menuViewItems.setEnabled( enabled );
 		menuViewDoorItems.setEnabled( enabled );
 		menuViewEnemies.setEnabled( enabled );
 		menuViewTypes.setEnabled( enabled );
 
 		barReloadLevel.setEnabled( enabled );
 		barSaveLevel.setEnabled( enabled );
 
 		barExportLevel.setEnabled( enabled );
 		barImportLevel.setEnabled( enabled );
 
 		barClearLevel.setEnabled( enabled );
 		
 		barViewSpawn.setEnabled( enabled );
 		barViewItems.setEnabled( enabled );
 		barViewDoorItems.setEnabled( enabled );
 		barViewEnemies.setEnabled( enabled );
 		barViewTypes.setEnabled( enabled );
 		//barSelectLevel.setEnabled( enabled );
 		
 		propertiesPanel.setEnabled( enabled );
 		selectorPanel.setEnabled( enabled );
 
 		scaleButton.setEnabled( enabled );
 		scaleMenuItem.setEnabled( enabled );
 		
 		if ( enabled ) {
 			setRomFeaturesEnabled( true );
 		}
 		
 		updateSaveFeatures();
 	}
 	
 	private void actionToggle ( boolean toolBar ) {
 		// Sync both menu and toolbar
 		if ( toolBar ) {
 			menuViewSpawn.setSelected( barViewSpawn.isSelected() );
 			menuViewItems.setSelected( barViewItems.isSelected() );
 			menuViewDoorItems.setSelected( barViewDoorItems.isSelected() );
 			menuViewEnemies.setSelected( barViewEnemies.isSelected() );
 			menuViewTypes.setSelected( barViewTypes.isSelected() );
 		} else {
 			barViewSpawn.setSelected( menuViewSpawn.isSelected() );
 			barViewItems.setSelected( menuViewItems.isSelected() );
 			barViewDoorItems.setSelected( menuViewDoorItems.isSelected() );
 			barViewEnemies.setSelected( menuViewEnemies.isSelected() );
 			barViewTypes.setSelected( menuViewTypes.isSelected() );
 		}
 		
 		// Update the level display
 		levelDisplay.setDisplayFlags(
 			barViewSpawn.isSelected(), barViewItems.isSelected(),
 			barViewEnemies.isSelected(), barViewDoorItems.isSelected(),
 			barViewTypes.isSelected()
 		);
 		
 		// Update the combo selector
 		selectorPanel.comboSelector.setDisplayHelp( barViewTypes.isSelected() );
 		
 		// Update the program configuration
 		config.setProperty( "Hazelnutt.gui.viewSpawn", barViewSpawn.isSelected() );
 		config.setProperty( "Hazelnutt.gui.viewItems", barViewItems.isSelected() );
 		config.setProperty( "Hazelnutt.gui.viewDoorItems", barViewDoorItems.isSelected() );
 		config.setProperty( "Hazelnutt.gui.viewEnemies", barViewEnemies.isSelected() );
 		config.setProperty( "Hazelnutt.gui.viewHelp", barViewTypes.isSelected() );
 	}
 
 	private void actionOpenRom ( String fileName ) {
 		if ( checkRomModified() ) {
 			
 			int res = fileName == null
 					? fileChooser.showOpenDialog( this )
 					: JFileChooser.APPROVE_OPTION;
 			if ( res == JFileChooser.APPROVE_OPTION ) {
 				File romFile = fileName == null
 							 ? fileChooser.getSelectedFile()
 							 : new File( fileName );
 				try {
 					logger.trace( "Loading Rom: '" + romFile + "'" );
 					
 					// Load it
 					long stTime = System.nanoTime();
 					BugsRom rom = BugsRom.loadFromFile( romFile );
 					
 					// Everything went fine, update the fields
 					logger.info( "Rom Loaded: '" + romFile + "'" );
 					logger.trace( "Took "
 						+ (((System.nanoTime()-stTime)/1000)/1000d)
 						+ " ms to load the ROM" );
 					loadedFile = romFile;
 					loadedRom = rom;
 					romHasChanged = false;
 					selectLevelNum( -1 );
 					setRomFeaturesEnabled( true );
 					
 					String path = loadedFile.getAbsolutePath();
 					recentFiles.remove( path );
 					recentFiles.addFirst( path );
 					updateRecentFiles();
 					
 					selectLevelNum( 0 );
 					
 				} catch ( Exception e ) {
 					logger.info( "Errror loading '" + romFile + "': " + e );
 					e.printStackTrace();
 					JOptionPane.showMessageDialog( this,
 						"An error ocurred while loading the file\n\n"
 							+ "File: " + romFile + "\n"
 							+ "Error: " + e,
 						"Error", JOptionPane.ERROR_MESSAGE );
 				}
 			}
 			
 			updateTitle();
 		}
 	}
 	
 	private boolean actionSaveRom () {
 		try {
 			checkLevelModified();
 			
 			long stTime = System.nanoTime();
 			
 			loadedRom.saveToFile( loadedFile );
 			romHasChanged = false;
 			
 			logger.info( "Rom Saved: '" + loadedFile + "'" );
 			logger.trace( "Took "
 					+ (((System.nanoTime()-stTime)/1000)/1000d)
 					+ " ms to save the ROM" );
 			
 			return true;
 		} catch ( Exception e ) {
 			logger.info( "Errror saving '" + loadedFile + "': " + e );
 			e.printStackTrace();
 			JOptionPane.showMessageDialog( this,
 				"An error ocurred while saving the file\n\n"
 					+ "File: " + loadedFile + "\n"
 					+ "Error: " + e,
 				"Error", JOptionPane.ERROR_MESSAGE );
 		} finally {
 			updateTitle();
 		}
 		
 		return false;
 	}
 	
 	private void actionSaveRomAs () {
 		fileChooser.showSaveDialog( this );
 		
 		File oldFile = loadedFile;
 		loadedFile = fileChooser.getSelectedFile();
 		if ( !actionSaveRom() ) {
 			loadedFile = oldFile;
 		}
 		
 		updateTitle();
 	}
 
 	private void actionLoadLevel () {
 		LevelSelectDialog lsd = new LevelSelectDialog( selectedLevelNum+1 );
 		lsd.setVisible( true );
 		
 		if ( lsd.isAccepted() ) {
 			if ( checkLevelModified() ) {
 				selectLevelNum( lsd.getSelectedNumber()-1 );
 			}
 		}
 	}
 
 	private void actionLevelUp () {
 		System.out.println( "LevelUp " + selectedLevelNum + " + 1" );
 		if ( levelFeaturesEnabled && selectedLevelNum < 28 && checkLevelModified() ) {
 			selectLevelNum( selectedLevelNum+1 );
 		}
 	}
 	
 	private void actionLevelDown () {
 		System.out.println( "LevelDown " + selectedLevelNum + " - 1"  );
 		if ( levelFeaturesEnabled && selectedLevelNum > 0 && checkLevelModified() ) {
 			selectLevelNum( selectedLevelNum-1 );
 		}
 	}
 	
 	private void actionReloadLevel () {
 		if ( levelHasChanged ) {
 			int res = JOptionPane.showConfirmDialog( this,
 				"If you reload this level, unsaved changes will be lost.\n" + 
 					"Are you sure you want to reload this level?",
 				"Reload Level Confirm", JOptionPane.YES_NO_OPTION );
 			if ( res == JOptionPane.YES_OPTION ) {
 				selectLevelNum( selectedLevelNum );
 			}
 		}
 		
 		updateTitle();
 	}
 	
 	private void actionSaveLevel () {
 		checkPropertiesModified();
 		
 		loadedRom.setLevel( selectedLevelNum, selectedLevel );
 		levelHasChanged = false;
 		romHasChanged = true;
 
 		updateTitle();
 	}
 	
 	private void actionClearLevel () {
 		int res = JOptionPane.showConfirmDialog( this,
 			"This action will delete everything in the level.\n" + 
 				"Are you sure you want to clear this level?",
 			"Clear Level Confirm", JOptionPane.YES_NO_OPTION );
 		
 		if ( res == JOptionPane.YES_OPTION ) {
 			RomLevel rl = selectedLevel.getRomLevel();
 			
 			byte[][] data = rl.getData();
 			for ( int i = 0; i < rl.getSize().getX(); i++ ) {
 				for ( int j = 0; j < rl.getSize().getY(); j++ ) {
 					data[ i ][ j ] = 0x03;
 				}
 			}
 			
 			for ( Iterator<?> it = rl.getItems().iterator(); it.hasNext(); ) {
 				it.next();
 				it.remove();
 			}
 			
 			for ( Iterator<?> it = rl.getDoorItems().iterator(); it.hasNext(); ) {
 				it.next();
 				it.remove();
 			}
 			
 			for ( Iterator<?> it = rl.getEnemies().iterator(); it.hasNext(); ) {
 				it.next();
 				it.remove();
 			}
 			
 			levelDisplay.repaint();
 			levelHasChanged = true;
 		}
 
 		updateTitle();
 	}
 	
 	private void actionImportLevel () {
 		int res = hlfFileChooser.showOpenDialog( this );
 		
 		if ( res == JFileChooser.APPROVE_OPTION ) {
 			File file = hlfFileChooser.getSelectedFile();
 			try {
 				Level level = HazelnuttLevelFormat.getInstance().loadLevelFromFile( file );
 
 				int currArea = selectedLevel.getRomLevel().getSize().getX()
 							 * selectedLevel.getRomLevel().getSize().getY();
 				int newArea = level.getRomLevel().getSize().getX()
 							* level.getRomLevel().getSize().getY();
 				
 				if ( currArea != newArea ) {
 					JOptionPane.showMessageDialog( this,
 						"The currently selected level and the level you are " +
 							"trying to import have different areas and are " +
 							"not compatible\n" +
 							"\nCurrent Level Area: " + currArea +
 							"\nImported Level Area: " + newArea,
 						"Error", JOptionPane.WARNING_MESSAGE );
 				} else {
 					selectLevel( level );
 					levelHasChanged = true;
 				}
 			} catch ( Exception e ) {
 				logger.info( "Error loading the file: '" + file + "'" );
 				e.printStackTrace();
 				JOptionPane.showMessageDialog( this,
 					"An error ocurred while loading the file\n\n"
 						+ "File: " + loadedFile + "\n"
 						+ "Error: " + e,
 					"Error", JOptionPane.ERROR_MESSAGE );
 			}
 		}
 	}
 	
 	private void actionExportLevel () {
 		int res = hlfFileChooser.showSaveDialog( this );
 		
 		if ( res == JFileChooser.APPROVE_OPTION ) {
 			File file = hlfFileChooser.getSelectedFile();
 			try {
 				HazelnuttLevelFormat.getInstance().saveLevelToFile( selectedLevel, file );
 			} catch ( Exception e ) {
 				logger.info( "Error saving the file: '" + file + "'" );
 				e.printStackTrace();
 				JOptionPane.showMessageDialog( this,
 					"An error ocurred while saving the file\n\n"
 						+ "File: " + loadedFile + "\n"
 						+ "Error: " + e,
 					"Error", JOptionPane.ERROR_MESSAGE );
 			}
 		}
 	}
 	
 	protected void actionCheckUpdates () {
 		CheckUpdatesDialog dialog = new CheckUpdatesDialog( this );
 		dialog.setModal( true );
 		dialog.setVisible( true );
 	}
 	
 	private void actionExit () {
 		logger.trace( "Program exit requested..." );
 
 		boolean close = true;
 		if ( !checkRomModified() ) {
 			close = false;
 		}
 		
 		if ( close && config.getBoolean( "Hazelnutt.gui.confirmExit", true ) ) {
 			int res = JOptionPane.showConfirmDialog(
 				this,
 				"Do you really want to close Hazelnutt?",
 				"Confirm",
 				JOptionPane.YES_NO_OPTION
 			);
 			close = (res==JOptionPane.OK_OPTION);
 		}
 		
 		if ( close ) {
 			logger.info( "Saving configuration values..." );
 			
 			boolean maximum = (getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0;
 			config.setProperty( "Hazelnutt.gui.maximum", maximum );
 			
 			Point loc = getLocation();
 			Dimension size = getSize();
 			config.setProperty( "Hazelnutt.gui.location.x", (int)loc.getX() );
 			config.setProperty( "Hazelnutt.gui.location.y", (int)loc.getY() );
 			config.setProperty( "Hazelnutt.gui.size.width", (int)size.getWidth() );
 			config.setProperty( "Hazelnutt.gui.size.height", (int)size.getHeight() );
 			
 			logger.debug( "Saving the last opened directory: '"
 				+ fileChooser.getCurrentDirectory() + "'" );
 			config.setProperty( "Hazelnutt.gui.lastDirectory",
 				fileChooser.getCurrentDirectory() );
 			
 			logger.debug( "Saving the recently opened files" );
 			config.clearProperty( "Hazelnutt.gui.recentFiles" );
 			for ( String str : recentFiles ) {
 				config.addProperty( "Hazelnutt.gui.recentFiles", str );
 			}
 			
 			logger.trace( "Closing program..." );
 			dispose();
 			
 			//memoryMonitor.cancel( true );
 		}
 	}
 	
 	private void actionAbout () {
 		JDialog dialog = new AboutDialog();
 		dialog.setModal( true );
 		dialog.setVisible( true );
 	}
 
 	/*public JPanel getLogPanel () {
 		return logPanel;
 	}//*/
 }

 package com.kartoflane.superluminal.core;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.zip.ZipFile;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.graphics.*;
 
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 
 import com.kartoflane.superluminal.elements.Anchor;
 import com.kartoflane.superluminal.elements.CursorBox;
 import com.kartoflane.superluminal.elements.FTLDoor;
 import com.kartoflane.superluminal.elements.FTLGib;
 import com.kartoflane.superluminal.elements.FTLMount;
 import com.kartoflane.superluminal.elements.FTLRoom;
 import com.kartoflane.superluminal.elements.FTLShip;
 import com.kartoflane.superluminal.elements.Grid;
 import com.kartoflane.superluminal.elements.GridBox;
 import com.kartoflane.superluminal.elements.HullBox;
 import com.kartoflane.superluminal.elements.ShieldBox;
 import com.kartoflane.superluminal.elements.Slide;
 import com.kartoflane.superluminal.elements.SystemBox;
 import com.kartoflane.superluminal.elements.Systems;
 import com.kartoflane.superluminal.elements.Tooltip;
 import com.kartoflane.superluminal.painter.Cache;
 import com.kartoflane.superluminal.painter.LayeredPainter;
 import com.kartoflane.superluminal.ui.DirectoriesWindow;
 import com.kartoflane.superluminal.ui.ErrorDialog;
 import com.kartoflane.superluminal.ui.ExportDialog;
 import com.kartoflane.superluminal.ui.GibDialog;
 import com.kartoflane.superluminal.ui.GibPropertiesWindow;
 import com.kartoflane.superluminal.ui.NewShipWindow;
 import com.kartoflane.superluminal.ui.PropertiesWindow;
 import com.kartoflane.superluminal.ui.ShipBrowser;
 import com.kartoflane.superluminal.ui.ShipPropertiesWindow;
 
 public class Main {
 		// === CONSTANTS
 	/**
 	 * Frequency of canvas redrawing (if constantRedraw == true)
 	 */
 	private final static int INTERVAL = 25;
 	/**
 	 * Size of corner indicators on currently selected room
 	 */
 	private final static int CORNER = 10;
 	/**
 	 * Width of the drawing area, in grid cells
 	 */
 	public static int GRID_W = 26;
 	public static int GRID_W_MAX = 30;
 	/**
 	 * Height of the drawing area, in grid cells
 	 */
 	public static int GRID_H = 20;
 	public static int GRID_H_MAX = 24;
 	
 	public final static int REACTOR_MAX_PLAYER = 25;
 	public final static int REACTOR_MAX_ENEMY = 32;
 	
 	public final static String APPNAME = "Superluminal";
 	public final static String VERSION = "2013.02.23";
 	
 		// === Important objects
 	public static Shell shell;
 	public static Canvas canvas;
 	public static FTLShip ship;
 	public static ShipPropertiesWindow shipDialog;
 	public static PropertiesWindow sysDialog;
 	public static ExportDialog exDialog;
 	public static MessageBox box;
 	public static ErrorDialog erDialog;
 	public static GibDialog gibDialog;
 	public static GibPropertiesWindow gibWindow;
 	public static Transform currentTransform;
 	public static LayeredPainter layeredPainter;
 	public static CursorBox cursor;
 	public static DirectoriesWindow dirWindow;
 	public static ShipBrowser browser;
 	
 		// === Preferences
 		// ship explorer
 	public static String dataPath = "";
 	public static String resPath = "";
 		// edit menu
 	public static boolean removeDoor = true;
 	public static boolean snapMounts = true;
 	public static boolean snapMountsToHull = true;
 	public static boolean arbitraryPosOverride = true;
 		// view menu
 	public static boolean showAnchor = true;
 	public static boolean showMounts = true;
 	public static boolean showRooms = true;
 	public static boolean showHull = true;
 	public static boolean showFloor = true;
 	public static boolean showShield = true;
 	public static boolean loadFloor = true;
 	public static boolean loadShield = true;
 	public static boolean loadSystem = true;
 		// export dialog
 	public static String exportPath = "";
 		// other
 	public static String projectPath = "";
 	
 		// ===  Mouse related
 	public static Point mousePos = new Point(0,0);
 	public static Point mousePosLastClick = new Point(0,0);
 	public static Point dragRoomAnchor = new Point(0,0);
 	public static boolean leftMouseDown = false;
 	public static boolean rightMouseDown = false;
 	
 		// === Generic booleans
 	public static boolean hullSelected = false;
 	public static boolean shieldSelected = false;
 	public static boolean modShift = false;
 	public static boolean modAlt = false;
 	public static boolean modCtrl = false;
 	
 		// === Internal
 	public static boolean debug = false;
 	
 		// === Variables used to store a specific element out of a set (currently selected room, etc)
 	public static FTLRoom selectedRoom = null;
 	public static FTLDoor selectedDoor = null;
 	public static FTLMount selectedMount = null;
 	public static FTLGib selectedGib = null;
 	
 		// === Rectangle variables, used for various purposes.
 	private static Rectangle phantomRect = null;
 	public static Rectangle shieldEllipse = new Rectangle(0,0,0,0);
 	
 		// === Flags for Weapon Mounting tool
 	public static Slide mountToolSlide = Slide.UP;
 	public static boolean mountToolMirror = true;
 	public static boolean mountToolHorizontal = true;
 	
 		// === Image holders
 	public static Image hullImage = null;
 	public static Image floorImage = null;
 	public static Image shieldImage = null;
 	public static Image cloakImage = null;
 	public static Image tempImage = null;
 	public static Image pinImage = null;
 	public static Image tickImage = null;
 	public static Image crossImage = null;
 	public static Map<String, Integer> weaponFrameWidthMap = new HashMap<String, Integer>();
 	
 		// === Miscellaneous
 	public static Rectangle[] corners = new Rectangle[4];
 	private static String lastMsg = "";
 	private static String interiorPath = "";
 	/**
 	 * Path of current project file, for quick saving via Ctrl+S
 	 */
 	public static String currentPath = null;
 
 	/**
 	 * Contains room IDs currently in use.
 	 */
 	public static HashSet<Integer> idList = new HashSet<Integer>();
 	/**
 	 * Images (tools and systems) are loaded once and then references are held in this map for easy access.
 	 */
 	public static HashMap<String, Image> toolsMap = new HashMap<String, Image>();
 
 	// === GUI elements' variables, for use in listeners and functions that reference them
 	public static Menu menuSystem;
 	public static Menu menuGib;
 	public static ArrayList<MenuItem> menuGibItems = new ArrayList<MenuItem>();
 	//private static Label helpIcon;
 	private static Label text;
 	private static Label mGridPosText;
 	private static Label shipInfoText;
 	public static MenuItem mntmClose;
 	private static Text txtX;
 	private static Text txtY;
 	private Label mPosText;
 	private Button btnHull;
 	public static Button btnShields;
 	private Button btnFloor;
 	private Button btnCloak;
 	private Button btnMiniship;
 	private Label canvasBg;
 	private FormData fd_canvas;
 	private boolean shellStateChange;
 	
 	private MenuItem mntmUnload;
 	private MenuItem mntmShowFile;
 	private Menu menu_imageBtns;
 	private Button sourceBtn;
 	private MenuItem mntmPath;
 
 	private MenuItem mntmShowFloor;
 	private MenuItem mntmShowShield;
 	public static Button btnCloaked;
 	private Button btnPirate;
 	private static Button btnXminus;
 	private static Button btnXplus;
 	private static Button btnYminus;
 	private static Button btnYplus;
 	private MenuItem mntmConToPlayer;
 	private MenuItem mntmConToEnemy;
 	public static Font appFont;
 	public static ToolItem tltmPointer;
 	public static ToolItem tltmRoom;
 	public static ToolItem tltmDoor;
 	public static ToolItem tltmMount;
 	public static ToolItem tltmSystem;
 	public static ToolItem tltmGib;
 
 	public static Anchor anchor;
 	public static GridBox gridBox;
 	public static HashMap<Systems, SystemBox> systemsMap = new HashMap<Systems, SystemBox>();
 	public static Grid grid;
 	public static HullBox hullBox;
 	public static ShieldBox shieldBox;
 	public static Tooltip tooltip;
 	
 	// =================================================================================================== //
 	
 	/*
 	 * ===== REMINDER: INCREMENT SHIP'S VERSION ON MAJOR RELEASES!
 	 * === TODO
 	 * 	- gibs editor - animation
 	 * 	- .ftl loading - fix negative offset issue
 	 * 		- detect if ship has negative offset, if yes then move all rooms by that offset as much as possible, and set offset to 0
 	 * 		- something's fucked up with loading; it's saying that the blueprint is not found in the file, even though it is there. WTF.
	 * 	- weapon i drone presets are not being loaded
 	 * 	- linking doors to rooms -> overrides automatically assigned left/right top/down IDs
 	 * 
 	 * == LOW PRIO:
 	 * 	- tools
 	 * 		- room splitting
 	 * 		- mounts - indicator for mirror and slide dir
 	 *  - room interior -> make it part of SysBox, not FTLRoom + maybe add possibility to link glow and other images, and automatically correctly export them?
 	 *  - change from ConfigIO to Properties java class
 	 * 	- Perhaps re-allocate precision mode to ctrl-drag and change shift-drag to only move things along one axis, like it works it Photoshop.
 	 * 	- Pirate version of ships viewable, similar to cloak?
 	 * 
 	 * =========================================================================
 	 * CHANGELOG:
 	 * 	- glow images can now be exported along with the interior image they are associated with, however they HAVE TO BE NAMED ACCORDINGLY -> in the same folder as base interior image and named <name>_glow
 	 * 	- archives are now unpacked automatically by the editor. The only thing you have to do is point it to original, packed, .dat archives inside /resources/ folder in your FTL installation.
 	 *  - images that cannot be overriden are now always exported, default or not
 	 *  - dialogs should now open to correct locations and retain their own paths
 	 */
 	
 	// =================================================================================================== //
 	
 	public static void main(String[] args) {
 		boolean enableCommandLine = true;
 		if (enableCommandLine) {
 			ArrayList<String> argsList = new ArrayList<String>();
 			for (String arg : args) {
 				argsList.add(arg);
 			}
 			
 			debug = argsList.contains("-debug");
 			ShipIO.IOdebug = argsList.contains("-IOdebug");
 			if (argsList.contains("-log")) {
 				try {
 					System.setOut(new PrintStream(new FileOutputStream("debug.log")));
 					System.setErr(new PrintStream(new FileOutputStream("debug.log")));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		try {
 			Main window = new Main();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void open() {
 		final Display display = Display.getDefault();
 		
 		shell = new Shell(SWT.SHELL_TRIM | SWT.BORDER);
 		shell.setLayout(new GridLayout(2, false));
 		shell.setText(APPNAME + " - Ship Editor");
 		shell.setLocation(100,50);
 		
 		// resize the window as to not exceed screen dimensions, with maximum size being defined by GRID_W_MAX and GRID_H_MAX
 		GRID_W = ((int) ((display.getBounds().width-35))/35);
 		GRID_H = ((int) ((display.getBounds().height-150))/35);
 		GRID_W = (GRID_W > GRID_W_MAX) ? GRID_W_MAX : GRID_W;
 		GRID_H = (GRID_H > GRID_H_MAX) ? GRID_H_MAX : GRID_H;
 		
 		// create config file if it doesn't exist already
 		if (!ConfigIO.configExists()) {
 			ConfigIO.saveConfig();
 		}
 		
 		// load values from config
 		
 		// browse
 		exportPath = ConfigIO.scourFor("exportPath");
 		projectPath = ConfigIO.scourFor("projectPath");
 		// files
 		dataPath = ConfigIO.scourFor("dataPath");
 		resPath = ConfigIO.scourFor("resPath");
 		// edit
 		removeDoor = ConfigIO.getBoolean("removeDoor");
 		arbitraryPosOverride = ConfigIO.getBoolean("arbitraryPosOverride");
 		//snapMounts = ConfigIO.getBoolean("snapMounts");
 		//snapMountsToHull = ConfigIO.getBoolean("snapMountsToHull");
 		// view
 		//showAnchor = ConfigIO.getBoolean("showAnchor");
 		//showMounts = ConfigIO.getBoolean("showMounts");
 		//showRooms = ConfigIO.getBoolean("showRooms");
 		//showHull = ConfigIO.getBoolean("showHull");
 		//showFloor = ConfigIO.getBoolean("showFloor");
 		//showShield = ConfigIO.getBoolean("showShield");
 		loadFloor = ConfigIO.getBoolean("loadFloor");
 		loadShield = ConfigIO.getBoolean("loadShield");
 		loadSystem = ConfigIO.getBoolean("loadSystem");
 		
 		
 		appFont = new Font(Display.getCurrent(), "Monospaced", 9, SWT.NORMAL);
 		if (appFont == null) {
 			appFont = new Font(shell.getDisplay(), "Serif", 9, SWT.NORMAL);
 		}
 		if (appFont == null) {
 			appFont = new Font(shell.getDisplay(), "Courier", 9, SWT.NORMAL);
 		}
 		
 		// used as a default, "null" transformation to fall back to in order to do regular drawing.
 		currentTransform = new Transform(shell.getDisplay());
 		
 		sysDialog = new PropertiesWindow(shell);
 		shipDialog = new ShipPropertiesWindow(shell);
 		erDialog = new ErrorDialog(shell);
 		gibDialog = new GibDialog(shell);
 		gibWindow = new GibPropertiesWindow(shell);
 		dirWindow = new DirectoriesWindow(shell);
 		browser = new ShipBrowser(shell);
 		
 		createContents();
 		
 		shell.setFont(appFont);
 		
 		shell.setEnabled(true);
 		
 		if (ShipIO.isNull(dataPath) || ShipIO.isNull(resPath) || !(new File("archives")).exists()) {
 			dataPath = null;
 			resPath = null;
 			shell.setEnabled(false);
 			dirWindow.btnClose.setEnabled(false);
 			dirWindow.label.setText("Please, browse to your FTL installation directory and select data.dat and resource.dat archives.");
 			dirWindow.open();
 		}
 		
 		if (!ShipIO.isNull(dataPath) && !ShipIO.isNull(resPath))
 			ShipIO.fetchShipNames();
 		if (ShipIO.errors.size() > 0) {
 			erDialog.printErrors(ShipIO.errors);
 			erDialog.open();
 		}
 		
 		shell.setMinimumSize(GRID_W*35, GRID_H*35);
 		shell.open();
 		
 		shell.addControlListener(new ControlAdapter() {
 			public void controlResized(ControlEvent e) {
 				GRID_W = ((int) ((canvasBg.getBounds().width))/35);
 				GRID_H = ((int) ((canvasBg.getBounds().height))/35);
 				fd_canvas.right.offset = GRID_W*35+5;
 				fd_canvas.bottom.offset = GRID_H*35+5;
 				canvas.setSize(GRID_W*35, GRID_H*35);
 				
 				if (grid != null)
 					grid.setSize(GRID_W*35, GRID_H*35);
 			}
 		});
 		
 		shellStateChange = shell.getMaximized();
 		
 		display.timerExec(INTERVAL, new Runnable() {
 			public void run() {
 				if (canvas.isDisposed()) return;
 				if (shellStateChange != shell.getMaximized()) {
 					shellStateChange = shell.getMaximized();
 					GRID_W = ((int) ((canvasBg.getBounds().width))/35);
 					GRID_H = ((int) ((canvasBg.getBounds().height))/35);
 					fd_canvas.right.offset = GRID_W*35;
 					fd_canvas.bottom.offset = GRID_H*35;
 					canvas.setSize(GRID_W*35, GRID_H*35);
 						
 					if (grid != null)
 						grid.setSize(GRID_W*35, GRID_H*35);
 				}
 					
 				// === update info text fields; mousePos and rudimentary ship info
 				mGridPosText.setText("(" + (int)(1+Math.floor(mousePos.x/35)) + ", " + (int)(1+Math.floor(mousePos.y/35)) + ")");
 				mPosText.setText("(" + mousePos.x + ", " + mousePos.y + ")");
 				if (ship != null)
 					shipInfoText.setText("rooms: " + ship.rooms.size() + ",  doors: " + ship.doors.size());
 
 				display.timerExec(INTERVAL, this);
 			}
 		});
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	protected void createContents() {
 		//highlightColor = shell.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		tempImage = SWTResourceManager.getImage(Main.class, "/org/eclipse/jface/dialogs/images/help.gif");
 		
 	// === Load images to a map for easy access
 		
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/room.png");
 		toolsMap.put("room", tempImage);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/door.png");
 		toolsMap.put("door", tempImage);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/pointer.png");
 		toolsMap.put("pointer", tempImage);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/mount.png");
 		toolsMap.put("mount", tempImage);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/system.png");
 		toolsMap.put("system", tempImage);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/gib.png");
 		toolsMap.put("gib", tempImage);
 		
 		pinImage = SWTResourceManager.getImage(Main.class, "/img/pin.png");
 		tickImage = SWTResourceManager.getImage(Main.class, "/img/check.png");
 		crossImage = SWTResourceManager.getImage(Main.class, "/img/cross.png");
 
 	// === Menu bar
 		
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 
 	// === File menu
 		
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 		Menu menu_file = new Menu(mntmFile);
 		mntmFile.setMenu(menu_file);
 	
 		// === File -> New ship
 		final MenuItem mntmNewShip = new MenuItem(menu_file, SWT.NONE);
 		mntmNewShip.setText("New Ship \tCtrl + N");
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 		// === File -> Load ship
 		final MenuItem mntmLoadShip = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShip.setText("Load Ship...\tCtrl + L");
 		
 		// === File -> Load ship from ftl
 		final MenuItem mntmLoadShipFTL = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShipFTL.setText("Load Ship From .ftl...");
 		
 		// === File -> Open project
 		final MenuItem mntmLoadShipProject = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShipProject.setText("Open Project...\tCtrl + O");
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 		
 		// === File -> Change archives
 		final MenuItem mntmArchives = new MenuItem(menu_file, SWT.NONE);
 		mntmArchives.setText("Change Archives...");
 		
 		new MenuItem(menu_file, SWT.SEPARATOR);
 		
 		// === File -> Save project
 		final MenuItem mntmSaveShip = new MenuItem(menu_file, SWT.NONE);
 		mntmSaveShip.setText("Save Project \tCtrl + S");
 		mntmSaveShip.setEnabled(false);
 		
 		// === File -> Save project as
 		final MenuItem mntmSaveShipAs = new MenuItem(menu_file, SWT.NONE);
 		mntmSaveShipAs.setText("Save Project As...");
 		mntmSaveShipAs.setEnabled(false);
 		
 		// === File -> Export ship
 		final MenuItem mntmExport = new MenuItem(menu_file, SWT.NONE);
 		mntmExport.setText("Export Ship... \tCtrl + E");
 		mntmExport.setEnabled(false);
 		
 		new MenuItem(menu_file, SWT.SEPARATOR);
 		
 		// === File -> Close project
 		mntmClose = new MenuItem(menu_file, SWT.NONE);
 		mntmClose.setText("Close Project");
 		mntmClose.setEnabled(false);
 		
 	// === Edit menu
 		
 		MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
 		mntmEdit.setText("Edit");
 		Menu menu_edit = new Menu(mntmEdit);
 		mntmEdit.setMenu(menu_edit);
 		
 		// === Edit -> Automatic door clean
 		MenuItem mntmRemoveDoors = new MenuItem(menu_edit, SWT.CHECK);
 		mntmRemoveDoors.setSelection(true);
 		mntmRemoveDoors.setText("Automatic Door Cleanup");
 		mntmRemoveDoors.setSelection(removeDoor);
 		
 		MenuItem mntmArbitraryPositionOverride = new MenuItem(menu_edit, SWT.CHECK);
 		mntmArbitraryPositionOverride.setText("Arbitrary Position Overrides Pin");
 		mntmArbitraryPositionOverride.setSelection(arbitraryPosOverride);
 		
 		new MenuItem(menu_edit, SWT.SEPARATOR);
 		
 		mntmConToPlayer = new MenuItem(menu_edit, SWT.NONE);
 		mntmConToPlayer.setEnabled(false);
 		mntmConToPlayer.setText("Convert To Player");
 		
 		mntmConToEnemy = new MenuItem(menu_edit, SWT.NONE);
 		mntmConToEnemy.setEnabled(false);
 		mntmConToEnemy.setText("Convert To Enemy");
 		
 	// === View menu
 		
 		MenuItem mntmView = new MenuItem(menu, SWT.CASCADE);
 		mntmView.setText("View");
 		Menu menu_view = new Menu(mntmView);
 		mntmView.setMenu(menu_view);
 		
 		// === View -> Errors console
 		MenuItem mntmOpenErrorsConsole = new MenuItem(menu_view, SWT.NONE);
 		mntmOpenErrorsConsole.setText("Open Errors Console");
 		
 		new MenuItem(menu_view, SWT.SEPARATOR);
 		
 		// === View -> Show anchor
 		final MenuItem mntmShowAnchor = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowAnchor.setText("Show Anchor \t1");
 		mntmShowAnchor.setSelection(showAnchor);
 		
 		// === View -> Show mounts
 		final MenuItem mntmShowMounts = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowMounts.setText("Show Mounts \t2");
 		mntmShowMounts.setSelection(showMounts);
 		
 		// === View -> show rooms
 		final MenuItem mntmShowRooms = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowRooms.setText("Show Rooms And Doors \t3");
 		mntmShowRooms.setSelection(showRooms);
 		
 		// === View -> graphics
 		MenuItem mntmGraphics = new MenuItem(menu_view, SWT.CASCADE);
 		mntmGraphics.setText("Graphics");
 		
 		Menu menu_graphics = new Menu(mntmGraphics);
 		mntmGraphics.setMenu(menu_graphics);
 		
 		// === View -> graphics -> show hull
 		final MenuItem mntmShowHull = new MenuItem(menu_graphics, SWT.CHECK);
 		mntmShowHull.setText("Show Hull \t4");
 		mntmShowHull.setSelection(showHull);
 		
 		// === View -> graphics -> show floor
 		mntmShowFloor = new MenuItem(menu_graphics, SWT.CHECK);
 		mntmShowFloor.setText("Show Floor\t5");
 		mntmShowFloor.setSelection(showFloor);
 		
 		// === View -> graphics -> show shield
 		mntmShowShield = new MenuItem(menu_graphics, SWT.CHECK);
 		mntmShowShield.setText("Show Shield\t6");
 		mntmShowShield.setSelection(showShield);
 		
 		new MenuItem(menu_view, SWT.SEPARATOR);
 		
 		// === View -> load floor
 		final MenuItem mntmLoadFloorGraphic = new MenuItem(menu_view, SWT.CHECK);
 		mntmLoadFloorGraphic.setText("Load Floor Graphic");
 		mntmLoadFloorGraphic.setSelection(loadFloor);
 		
 		// === View -> load shield
 		final MenuItem mntmLoadShieldGraphic = new MenuItem(menu_view, SWT.CHECK);
 		mntmLoadShieldGraphic.setText("Load Shield Graphic");
 		mntmLoadShieldGraphic.setSelection(loadShield);
 		
 		// === View -> load system graphic
 		MenuItem mntmLoadSystem = new MenuItem(menu_view, SWT.CHECK);
 		mntmLoadSystem.setText("Load System Graphics");
 		mntmLoadSystem.setSelection(loadSystem);
 		
 	// === Tool bar
 		
 		// === Container - holds all the items on the left side of the screen
 		Composite toolBarHolder = new Composite(shell, SWT.NONE);
 		toolBarHolder.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
 		GridLayout gl_toolBarHolder = new GridLayout(2, false);
 		gl_toolBarHolder.marginWidth = 0;
 		gl_toolBarHolder.marginHeight = 0;
 		toolBarHolder.setLayout(gl_toolBarHolder);
 
 		// === Container -> Tools - tool bar containing the tool icons
 		final ToolBar toolBar = new ToolBar(toolBarHolder, SWT.NONE);
 		toolBar.setFont(appFont);
 		GridData gd_toolBar = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
 		gd_toolBar.minimumHeight = -1;
 		gd_toolBar.minimumWidth = -1;
 		toolBar.setLayoutData(gd_toolBar);
 		
 		// === Container -> Tools -> Pointer
 		tltmPointer = new ToolItem(toolBar, SWT.RADIO);
 		tltmPointer.setWidth(60);
 		tltmPointer.setSelection(true);
 		tltmPointer.setImage(toolsMap.get("pointer"));
 		tltmPointer.setToolTipText("Selection Tool [Q]"
 									+ShipIO.lineDelimiter+" -Click to selet an object"
 									+ShipIO.lineDelimiter+" -Click and hold to move the object around"
 									+ShipIO.lineDelimiter+" -For rooms, click on a corner and drag to resize the room" 
 									+ShipIO.lineDelimiter+" -Right-click to assign a system to the selected room"
 									+ShipIO.lineDelimiter+" -Double click on a room to set its' system's level and power"
 									+ShipIO.lineDelimiter+" -For weapon mounts, hull and shields, press down Shift for precision mode");
 		
 		// === Container -> Tools -> Room creation
 		tltmRoom = new ToolItem(toolBar, SWT.RADIO);
 		tltmRoom.setWidth(60);
 		tltmRoom.setToolTipText("Room Creation Tool [W]"
 								+ShipIO.lineDelimiter+" -Click and drag to create a room"
 								+ShipIO.lineDelimiter+" -Hold down Shift and click to split rooms");
 		tltmRoom.setImage(toolsMap.get("room"));
 		
 		// === Container -> Tools -> Door creation
 		tltmDoor = new ToolItem(toolBar, SWT.RADIO);
 		tltmDoor.setWidth(60);
 		tltmDoor.setToolTipText("Door Creation Tool [E]"
 								+ShipIO.lineDelimiter+" - Hover over an edge of a room and click to place door");
 		tltmDoor.setImage(toolsMap.get("door"));
 		
 		// === Container -> Tools -> Weapon mounting
 		tltmMount = new ToolItem(toolBar, SWT.RADIO);
 		tltmMount.setWidth(60);
 		tltmMount.setToolTipText("Weapon Mounting Tool [R]"
 									+ShipIO.lineDelimiter+" -Click to place a weapon mount"
 									+ShipIO.lineDelimiter+" -Right-click to change the mount's rotation"
 									+ShipIO.lineDelimiter+" -Shift-click to mirror the mount along its axis"
 									+ShipIO.lineDelimiter+" -Shift-right-click to change the direction in which the weapon opens"
 									+ShipIO.lineDelimiter+" (the last three also work with Selection Tool)");
 		tltmMount.setImage(toolsMap.get("mount"));
 
 		// === Container -> Tools -> System operating slot
 		tltmSystem = new ToolItem(toolBar, SWT.RADIO);
 		tltmSystem.setWidth(60);
 		tltmSystem.setToolTipText("System Station Tool [T]"
 									+ShipIO.lineDelimiter+" - Click to place an operating station (only mannable systems + medbay)"
 									+ShipIO.lineDelimiter+" - Right-click to reset the station to default"
 									+ShipIO.lineDelimiter+" - Shift-click to change facing of the station");
 		tltmSystem.setImage(toolsMap.get("system"));
 		
 		tltmGib = new ToolItem(toolBar, SWT.RADIO);
 		tltmGib.setWidth(60);
 		tltmGib.setToolTipText("Gib Editing Tool [G]");
 		tltmGib.setImage(toolsMap.get("gib"));
 
 		tltmPointer.setEnabled(false);
 		tltmRoom.setEnabled(false);
 		tltmDoor.setEnabled(false);
 		tltmMount.setEnabled(false);
 		tltmSystem.setEnabled(false);
 		tltmGib.setEnabled(false);
 		
 		// === Container -> buttonComposite
 		Composite composite = new Composite(toolBarHolder, SWT.NONE);
 		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
 		gd_composite.heightHint = 30;
 		composite.setLayoutData(gd_composite);
 		GridLayout gl_composite = new GridLayout(13, false);
 		gl_composite.marginTop = 2;
 		gl_composite.marginHeight = 0;
 		composite.setLayout(gl_composite);
 		
 		Label label = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
 		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 		
 		// === Container -> buttonComposite -> Hull image button
 		btnHull = new Button(composite, SWT.NONE);
 		GridData gd_btnHull = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnHull.minimumWidth = 70;
 		btnHull.setLayoutData(gd_btnHull);
 		btnHull.setFont(appFont);
 		btnHull.setEnabled(false);
 		btnHull.setText("Hull");
 
 		// === Container -> buttonComposite -> shield image button
 		btnShields = new Button(composite, SWT.NONE);
 		GridData gd_btnShields = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnShields.minimumWidth = 70;
 		btnShields.setLayoutData(gd_btnShields);
 		btnShields.setFont(appFont);
 		btnShields.setToolTipText("Shield is aligned in relation to rooms. Place a room before choosing shield graphic.");
 		btnShields.setEnabled(false);
 		btnShields.setText("Shields");
 
 		// === Container -> buttonComposite -> floor image button
 		btnFloor = new Button(composite, SWT.NONE);
 		GridData gd_btnFloor = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnFloor.minimumWidth = 70;
 		btnFloor.setLayoutData(gd_btnFloor);
 		btnFloor.setFont(appFont);
 		btnFloor.setEnabled(false);
 		btnFloor.setText("Floor");
 
 		// === Container -> buttonComposite -> cloak image button
 		btnCloak = new Button(composite, SWT.NONE);
 		GridData gd_btnCloak = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnCloak.minimumWidth = 70;
 		btnCloak.setLayoutData(gd_btnCloak);
 		btnCloak.setSize(70, 25);
 		btnCloak.setFont(appFont);
 		btnCloak.setEnabled(false);
 		btnCloak.setText("Cloak");
 
 		// === Container -> buttonComposite -> miniship image button
 		btnMiniship = new Button(composite, SWT.NONE);
 		GridData gd_btnMiniship = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnMiniship.minimumWidth = 85;
 		btnMiniship.setLayoutData(gd_btnMiniship);
 		btnMiniship.setSize(70, 25);
 		btnMiniship.setEnabled(false);
 		btnMiniship.setFont(appFont);
 		btnMiniship.setText("MiniShip");
 		
 		// === Container -> buttonComposite -> Popup Menu
 		menu_imageBtns = new Menu(shell);
 		btnHull.setMenu(menu_imageBtns);
 		btnShields.setMenu(menu_imageBtns);
 		btnFloor.setMenu(menu_imageBtns);
 		btnCloak.setMenu(menu_imageBtns);
 		btnMiniship.setMenu(menu_imageBtns);
 		
 		mntmPath = new MenuItem(menu_imageBtns, SWT.NONE);
 		mntmPath.setEnabled(false);
 		
 		new MenuItem(menu_imageBtns, SWT.SEPARATOR);
 
 		// === Container -> buttonComposite -> Popup Menu -> Reset Path
 		mntmUnload = new MenuItem(menu_imageBtns, SWT.NONE);
 		mntmUnload.setText("Unload Image");
 
 		// === Container -> buttonComposite -> Popup Menu -> Show File
 		mntmShowFile = new MenuItem(menu_imageBtns, SWT.NONE);
 		mntmShowFile.setText("Show Directory");
 		
 		Label label_1 = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
 		label_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 		
 		// === Container -> Properties
 		final Button btnShipProperties = new Button(composite, SWT.NONE);
 		btnShipProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnShipProperties.setFont(appFont);
 		btnShipProperties.setText("Properties");
 		btnShipProperties.setEnabled(false);
 		
 		Label label_2 = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
 		label_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 		
 		// === Container -> set position composite
 		Composite coSetPosition = new Composite(composite, SWT.NONE);
 		coSetPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
 		GridLayout gl_coSetPosition = new GridLayout(7, false);
 		gl_coSetPosition.marginWidth = 0;
 		gl_coSetPosition.marginHeight = 0;
 		coSetPosition.setLayout(gl_coSetPosition);
 		
 		// === Cotnainer -> set position composite -> X
 		Label lblX = new Label(coSetPosition, SWT.NONE);
 		lblX.setFont(appFont);
 		GridData gd_lblX = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
 		gd_lblX.horizontalIndent = 5;
 		lblX.setLayoutData(gd_lblX);
 		lblX.setText("X:");
 
 		// === Cotnainer -> set position composite -> X buttons container
 		Composite setPosXBtnsCo = new Composite(coSetPosition, SWT.NONE);
 		GridLayout gl_setPosXBtnsCo = new GridLayout(2, false);
 		gl_setPosXBtnsCo.horizontalSpacing = 0;
 		gl_setPosXBtnsCo.verticalSpacing = 0;
 		gl_setPosXBtnsCo.marginWidth = 0;
 		gl_setPosXBtnsCo.marginHeight = 0;
 		setPosXBtnsCo.setLayout(gl_setPosXBtnsCo);
 		setPosXBtnsCo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
 
 		// === Cotnainer -> set position composite -> X buttons container -> X minus
 		btnXminus = new Button(setPosXBtnsCo, SWT.CENTER);
 		btnXminus.setToolTipText("Subtract 35");
 		btnXminus.setEnabled(false);
 		btnXminus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnXminus.setFont(appFont);
 		btnXminus.setText("-");
 		btnXminus.setBounds(0, 0, 18, 25);
 
 		// === Cotnainer -> set position composite -> X buttons container -> X plus
 		btnXplus = new Button(setPosXBtnsCo, SWT.CENTER);
 		btnXplus.setToolTipText("Add 35");
 		btnXplus.setEnabled(false);
 		btnXplus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnXplus.setBounds(0, 0, 75, 25);
 		btnXplus.setFont(appFont);
 		btnXplus.setText("+");
 
 		// === Cotnainer -> set position composite -> X text field
 		txtX = new Text(coSetPosition, SWT.BORDER);
 		txtX.setEnabled(false);
 		txtX.setFont(appFont);
 		txtX.setTextLimit(5);
 		txtX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
 
 		// === Cotnainer -> set position composite -> Y
 		Label lblY = new Label(coSetPosition, SWT.NONE);
 		GridData gd_lblY = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
 		gd_lblY.horizontalIndent = 5;
 		lblY.setLayoutData(gd_lblY);
 		lblY.setFont(appFont);
 		lblY.setText("Y:");
 
 		// === Cotnainer -> set position composite -> Y buttons container
 		Composite setPosYBtnsCo = new Composite(coSetPosition, SWT.NONE);
 		GridLayout gl_setPosYBtnsCo = new GridLayout(2, false);
 		gl_setPosYBtnsCo.verticalSpacing = 0;
 		gl_setPosYBtnsCo.marginWidth = 0;
 		gl_setPosYBtnsCo.marginHeight = 0;
 		gl_setPosYBtnsCo.horizontalSpacing = 0;
 		setPosYBtnsCo.setLayout(gl_setPosYBtnsCo);
 
 		// === Cotnainer -> set position composite -> Y buttons container -> Y minus
 		btnYminus = new Button(setPosYBtnsCo, SWT.CENTER);
 		btnYminus.setToolTipText("Subtract 35");
 		btnYminus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnYminus.setFont(appFont);
 		btnYminus.setText("-");
 		btnYminus.setEnabled(false);
 
 		// === Cotnainer -> set position composite -> Y buttons container -> Y plus
 		btnYplus = new Button(setPosYBtnsCo, SWT.CENTER);
 		btnYplus.setToolTipText("Add 35");
 		btnYplus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnYplus.setFont(appFont);
 		btnYplus.setText("+");
 		btnYplus.setEnabled(false);
 
 		// === Cotnainer -> set position composite -> Y text field
 		txtY = new Text(coSetPosition, SWT.BORDER);
 		txtY.setEnabled(false);
 		txtY.setFont(appFont);
 		txtY.setTextLimit(5);
 		txtY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
 		new Label(coSetPosition, SWT.NONE);
 		
 		Label label_3 = new Label(composite, SWT.SEPARATOR | SWT.RIGHT);
 		label_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 
 		// === Cotnainer -> state buttons composite
 		Composite stateBtnsCo = new Composite(composite, SWT.NONE);
 		GridLayout gl_stateBtnsCo = new GridLayout(2, false);
 		gl_stateBtnsCo.marginWidth = 0;
 		gl_stateBtnsCo.verticalSpacing = 0;
 		gl_stateBtnsCo.marginHeight = 0;
 		stateBtnsCo.setLayout(gl_stateBtnsCo);
 		stateBtnsCo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
 
 		// === Cotnainer -> state buttons composite -> cloaked
 		btnCloaked = new Button(stateBtnsCo, SWT.TOGGLE | SWT.CENTER);
 		btnCloaked.setEnabled(false);
 		btnCloaked.setFont(appFont);
 		btnCloaked.setImage(SWTResourceManager.getImage(Main.class, "/img/smallsys/smallcloak.png"));
 		btnCloaked.setToolTipText("View the cloaked version of the ship.");
 		btnCloaked.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		// === Cotnainer -> state buttons composite -> pirate
 		btnPirate = new Button(stateBtnsCo, SWT.TOGGLE | SWT.CENTER);
 		btnPirate.setEnabled(false);
 		btnPirate.setFont(appFont);
 		btnPirate.setToolTipText("View the pirate version of the ship.");
 		btnPirate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnPirate.setImage(SWTResourceManager.getImage(Main.class, "/img/pirate.png"));
 		new Label(composite, SWT.NONE);
 		
 	// === Canvas
 		
 		Composite canvasHolder = new Composite(shell, SWT.NONE);
 		canvasHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4));
 		canvasHolder.setLayout(new FormLayout());
 		
 		// Main screen where ships are displayed
 		canvas = new Canvas(canvasHolder, SWT.NONE | SWT.TRANSPARENT | SWT.BORDER | SWT.DOUBLE_BUFFERED);
 		Color c = new Color(shell.getDisplay(), 64, 64, 64);
 		canvas.setBackground(c);
 		c.dispose();
 		fd_canvas = new FormData();
 		fd_canvas.bottom = new FormAttachment(0, GRID_H*35);
 		fd_canvas.right = new FormAttachment(0, GRID_W*35);
 		fd_canvas.top = new FormAttachment(0);
 		fd_canvas.left = new FormAttachment(0);
 		canvas.setLayoutData(fd_canvas);
 
 		layeredPainter = new LayeredPainter();
 		canvas.addPaintListener(layeredPainter);
 		
 		grid = new Grid(GRID_W, GRID_H);
 		
 		anchor = new Anchor();
 		anchor.setLocation(0,0,false);
 		anchor.setSize(GRID_W*35, GRID_H*35);
 		layeredPainter.add(anchor, LayeredPainter.ANCHOR);
 		
 		cursor = new CursorBox();
 		cursor.setBorderThickness(2);
 		cursor.setSize(35, 35);
 		cursor.setBorderColor(new RGB(0,0,255));
 		layeredPainter.add(cursor, LayeredPainter.SELECTION);
 		
 		hullBox = new HullBox();
 		layeredPainter.add(hullBox, LayeredPainter.HULL);
 		
 		shieldBox = new ShieldBox();
 		layeredPainter.add(shieldBox, LayeredPainter.SHIELD);
 		
 		tooltip = new Tooltip();
 
 		SystemBox tempBox = new SystemBox(Systems.PILOT);
 		tempBox.setImage("/img/systems/s_pilot_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.DOORS);
 		tempBox.setImage("/img/systems/s_doors_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.SENSORS);
 		tempBox.setImage("/img/systems/s_sensors_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.OXYGEN);
 		tempBox.setImage("/img/systems/s_oxygen_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.MEDBAY);
 		tempBox.setImage("/img/systems/s_medbay_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.SHIELDS);
 		tempBox.setImage("/img/systems/s_shields_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.WEAPONS);
 		tempBox.setImage("/img/systems/s_weapons_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.ENGINES);
 		tempBox.setImage("/img/systems/s_engines_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.DRONES);
 		tempBox.setImage("/img/systems/s_drones_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.TELEPORTER);
 		tempBox.setImage("/img/systems/s_teleporter_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.CLOAKING);
 		tempBox.setImage("/img/systems/s_cloaking_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		tempBox = new SystemBox(Systems.ARTILLERY);
 		tempBox.setImage("/img/systems/s_artillery_overlay.png", true);
 		systemsMap.put(tempBox.getSystemName(), tempBox);
 		layeredPainter.add(tempBox, LayeredPainter.SYSTEM_ICON);
 		tempBox.setVisible(false);
 		
 		canvasBg = new Label(canvasHolder, SWT.NONE);
 		c = Cache.checkOutColor(canvasBg, new RGB(96, 96, 96));
 		canvasBg.setBackground(c);
 		FormData fd_canvasBg = new FormData();
 		fd_canvasBg.bottom = new FormAttachment(100);
 		fd_canvasBg.right = new FormAttachment(100);
 		fd_canvasBg.top = new FormAttachment(0);
 		fd_canvasBg.left = new FormAttachment(0);
 		canvasBg.setLayoutData(fd_canvasBg);
 		
 	// === System assignment context menu
 			
 		// === Systems
 		menuSystem = new Menu(canvas);
 			
 		// === Systems -> Empty
 		final MenuItem mntmEmpty = new MenuItem(menuSystem, SWT.RADIO);
 		mntmEmpty.setSelection(true);
 		mntmEmpty.setText("None");
 		
 		// === Systems -> Systems
 		MenuItem mntmSystems = new MenuItem(menuSystem, SWT.CASCADE);
 		mntmSystems.setText("Systems");
 		Menu menu_systems = new Menu(mntmSystems);
 		mntmSystems.setMenu(menu_systems);
 
 		// === Systems -> Systems -> Oxygen
 		final MenuItem mntmOxygen = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalloxygen.png");
 		mntmOxygen.setImage(tempImage);
 		mntmOxygen.setText("Oxygen");
 
 		// === Systems -> Systems -> Medbay
 		final MenuItem mntmMedbay = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallmedbay.png");
 		mntmMedbay.setImage(tempImage);
 		mntmMedbay.setText("Medbay");
 
 		// === Systems -> Systems -> Shields
 		final MenuItem mntmShields = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallshields.png");
 		mntmShields.setImage(tempImage);
 		mntmShields.setText("Shields");
 
 		// === Systems -> Systems -> Weapons
 		final MenuItem mntmWeapons = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallweapons.png");
 		mntmWeapons.setImage(tempImage);
 		mntmWeapons.setText("Weapons");
 
 		// === Systems -> Systems -> Engines
 		final MenuItem mntmEngines = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallengines.png");
 		mntmEngines.setImage(tempImage);
 		mntmEngines.setText("Engines");
 
 		// === Systems -> Subsystems
 		MenuItem mntmSubsystems = new MenuItem(menuSystem, SWT.CASCADE);
 		mntmSubsystems.setText("Subsystems");
 		Menu menu_subsystems = new Menu(mntmSubsystems);
 		mntmSubsystems.setMenu(menu_subsystems);
 
 		// === Systems -> Subsystems -> Pilot
 		final MenuItem mntmPilot = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallpilot.png");
 		mntmPilot.setImage(tempImage);
 		mntmPilot.setText("Pilot");
 
 		// === Systems -> Subsystems -> Doors
 		final MenuItem mntmDoors = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalldoor.png");
 		mntmDoors.setImage(tempImage);
 		mntmDoors.setText("Doors");
 
 		// === Systems -> Subsystems -> Sensors
 		final MenuItem mntmSensors = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallsensors.png");
 		mntmSensors.setImage(tempImage);
 		mntmSensors.setText("Sensors");
 
 		// === Systems -> Special
 		MenuItem mntmSpecial = new MenuItem(menuSystem, SWT.CASCADE);
 		mntmSpecial.setText("Special");
 		Menu menu_special = new Menu(mntmSpecial);
 		mntmSpecial.setMenu(menu_special);
 
 		// === Systems -> Special -> Drones
 		final MenuItem mntmDrones = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smalldrones.png");
 		mntmDrones.setImage(tempImage);
 		mntmDrones.setText("Drones");
 
 		// === Systems -> Special -> Teleporter
 		final MenuItem mntmTeleporter = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallteleporter.png");
 		mntmTeleporter.setImage(tempImage);
 		mntmTeleporter.setText("Teleporter");
 
 		// === Systems -> Special -> Cloaking
 		final MenuItem mntmCloaking = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallcloak.png");
 		mntmCloaking.setImage(tempImage);
 		mntmCloaking.setText("Cloaking");
 
 		// === Systems -> Special -> Artillery
 		final MenuItem mntmArtillery = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = SWTResourceManager.getImage(Main.class, "/img/smallsys/smallartillery.png");
 		mntmArtillery.setImage(tempImage);
 		mntmArtillery.setText("Artillery");
 		
 		new MenuItem(menuSystem, SWT.SEPARATOR);
 		
 		// === Systems -> Set System Image
 		final MenuItem mntmSysImage = new MenuItem(menuSystem, SWT.NONE);
 		mntmSysImage.setEnabled(false);
 		mntmSysImage.setText("Set Interior Image...");
 		
 	// === Gib Assignment Context Menu
 
 		menuGib = new Menu(canvas);
 		
 		// === Text Info Fields
 		
 		Composite textHolder = new Composite(shell, SWT.NONE);
 		GridData gd_textHolder = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
 		gd_textHolder.heightHint = 18;
 		textHolder.setLayoutData(gd_textHolder);
 		textHolder.setLayout(new FormLayout());
 		
 		// === Position of the pointer on the grid
 		mGridPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
 		mGridPosText.setFont(appFont);
 		FormData fd_mGridPosText = new FormData();
 		fd_mGridPosText.bottom = new FormAttachment(100);
 		fd_mGridPosText.top = new FormAttachment(0);
 		fd_mGridPosText.left = new FormAttachment(0);
 		mGridPosText.setLayoutData(fd_mGridPosText);
 		
 		mPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
 		mPosText.setFont(appFont);
 		FormData fd_mPosText = new FormData();
 		fd_mPosText.bottom = new FormAttachment(100);
 		fd_mPosText.top = new FormAttachment(mGridPosText, 0, SWT.TOP);
 		fd_mPosText.left = new FormAttachment(mGridPosText, 6);
 		mPosText.setLayoutData(fd_mPosText);
 		
 		// === Number of rooms and doors in the ship
 		shipInfoText = new Label(textHolder, SWT.BORDER);
 		fd_mPosText.right = new FormAttachment(shipInfoText, -6);
 		fd_mGridPosText.right = new FormAttachment(shipInfoText, -84);
 		shipInfoText.setFont(appFont);
 		FormData fd_shipInfoText = new FormData();
 		fd_shipInfoText.bottom = new FormAttachment(100);
 		fd_shipInfoText.top = new FormAttachment(0);
 		fd_shipInfoText.left = new FormAttachment(0, 129);
 		shipInfoText.setLayoutData(fd_shipInfoText);
 		
 		// === Status bar
 		text = new Label(textHolder, SWT.WRAP | SWT.BORDER);
 		fd_shipInfoText.right = new FormAttachment(text, -6);
 		text.setFont(appFont);
 		FormData fd_text = new FormData();
 		fd_text.bottom = new FormAttachment(100);
 		fd_text.right = new FormAttachment(100);
 		fd_text.left = new FormAttachment(0, 265);
 		fd_text.top = new FormAttachment(0);
 		text.setLayoutData(fd_text);
 		new Label(shell, SWT.NONE);
 
 		shell.pack();
 		
 	// =======================
 	// === BOOKMARK: LISTENERS
 	// =======================
 		
 		Integer[] ignoredLayers = {LayeredPainter.SELECTION, LayeredPainter.GRID, LayeredPainter.ANCHOR, LayeredPainter.SYSTEM_ICON, LayeredPainter.GIB};
 		final MouseInputAdapter mouseListener = new MouseInputAdapter(ignoredLayers);
 		canvas.addMouseMoveListener(mouseListener);
 		canvas.addMouseTrackListener(mouseListener);
 		canvas.addMouseListener(mouseListener);
 
 		// === SELECTED ITEM POSITION
 	
 		txtX.addVerifyListener(new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				String string = e.text;
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9') && ('-'!=chars[i])) {
 						e.doit = false;
 						return;
 					}
 				}
 			}
 		});
 
 		txtY.addVerifyListener(new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				String string = e.text;
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9') && ('-'!=chars[i])) {
 						e.doit = false;
 						return;
 					}
 				}
 			}
 		});
 		
 		txtX.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				if (e.detail == SWT.TRAVERSE_RETURN) {
 					updateSelectedPosition();
 				} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
 					updateSelectedPosText();				  
 				}
 				canvas.forceFocus();
 				e.doit = false;
 			}
 		});
 		
 		txtY.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				if (e.detail == SWT.TRAVERSE_RETURN) {
 					updateSelectedPosition();
 				} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
 					updateSelectedPosText();		  
 				}
 				canvas.forceFocus();
 				e.doit = false;
 			}
 		});
 
 		btnXminus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtX.setText(""+(Integer.valueOf(txtX.getText())-((selectedRoom==null) ? 35 : 1)));
 				updateSelectedPosition();
 			} });
 
 		btnXplus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtX.setText(""+(Integer.valueOf(txtX.getText())+((selectedRoom==null) ? 35 : 1)));
 				updateSelectedPosition();
 			} });
 
 		btnYminus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtY.setText(""+(Integer.valueOf(txtY.getText())-((selectedRoom==null) ? 35 : 1)));
 				updateSelectedPosition();
 			} });
 
 		btnYplus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtY.setText(""+(Integer.valueOf(txtY.getText())+((selectedRoom==null) ? 35 : 1)));
 				updateSelectedPosition();
 			} });
 		
 			
 	// === IMAGE BUTTONS
 		
 		btnMiniship.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.miniPath);
 				dialog.setFileName(ship.miniPath);
 				String path = dialog.open();
 				
 				if (!ShipIO.isNull(path)) {
 					Main.ship.miniPath = path;
 				}
 				updateButtonImg();
 				canvas.setFocus();
 			}
 		});
 		
 		btnFloor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.floorPath);
 				dialog.setFileName(ship.floorPath);
 				String path = dialog.open();
 				
 				if (!ShipIO.isNull(path)) {
 					Main.ship.floorPath = path;
 					
 					ShipIO.loadImage(path, "floor");
 					canvas.redraw();
 				}
 				updateButtonImg();
 				canvas.setFocus();
 			}
 		});
 		
 		btnCloak.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.cloakPath);
 				dialog.setFileName(ship.cloakPath);
 				String path = dialog.open();
 				
 				if (!ShipIO.isNull(path)) {
 					if (ShipIO.isDefaultResource(new File(path)))
 						Main.ship.cloakOverride = path;
 					
 					Main.ship.cloakPath = path;
 					btnCloaked.setEnabled(!tltmGib.getSelection());
 					
 					ShipIO.loadImage(path, "cloak");
 					canvas.redraw();
 				}
 				updateButtonImg();
 				canvas.setFocus();
 			}
 		});
 		
 		btnShields.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.shieldPath);
 				dialog.setFileName(ship.shieldPath);
 				
 				String path = dialog.open();
 				Main.ship.shieldOverride = null;
 				
 				if (!ShipIO.isNull(path)) {
 					if (ShipIO.isDefaultResource(new File(path)))
 						Main.ship.shieldOverride = path;
 					
 					Main.ship.shieldPath = path;
 					
 					ShipIO.loadImage(path, "shields");
 					
 					if (ship.isPlayer)
 						if (shieldImage != null && !shieldImage.isDisposed()) {
 							Rectangle temp = shieldImage.getBounds();
 							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						}
 					
 					updateButtonImg();
 					
 					canvas.redraw();
 				}
 				canvas.setFocus();
 			}
 		});
 		
 		btnHull.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.imagePath);
 				dialog.setFileName(ship.imagePath);
 				String path = dialog.open();
 				
 				
 				if (!ShipIO.isNull(path)) {
 					Main.ship.imagePath = path;
 					
 					ShipIO.loadImage(path, "hull");
 					canvas.redraw();
 				}
 				updateButtonImg();
 				canvas.setFocus();
 			}
 		});
 		
 		btnShipProperties.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				shell.setEnabled(false);
 				shipDialog.open();
 				
 				shell.setEnabled(true);
 				canvas.redraw();
 				canvas.setFocus();
 			}
 		});
 
 		btnHull.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			} });
 		btnShields.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			} });
 		btnFloor.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			} });
 		btnCloak.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			} });
 		btnMiniship.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			} });
 		
 		menu_imageBtns.addMenuListener(new MenuAdapter() {
 			String s = null;
 			public void menuShown(MenuEvent e) {
 				if (sourceBtn == btnHull) {
 					s = ship.imagePath;
 				} else if (sourceBtn == btnShields) {
 					s = ship.shieldPath;
 				} else if (sourceBtn == btnFloor) {
 					s = ship.floorPath;
 				} else if (sourceBtn == btnCloak) {
 					s = ship.cloakPath;
 				} else if (sourceBtn == btnMiniship) {
 					s = ship.miniPath;
 				}
 				mntmUnload.setEnabled(!ShipIO.isNull(s));
 				mntmShowFile.setEnabled(!ShipIO.isNull(s));
 				if (!ShipIO.isNull(s)) {
 					mntmPath.setText("..."+s.substring(s.lastIndexOf(ShipIO.pathDelimiter)));
 				} else {
 					mntmPath.setText("");
 				}
 			}
 		});
 		
 		mntmUnload.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (sourceBtn == btnHull) {
 					hullBox.setHullImage(null);
 				} else if (sourceBtn == btnShields) {
 					shieldBox.setImage(null, true);
 					ship.shieldOverride = null;
 				} else if (sourceBtn == btnFloor) {
 					hullBox.setFloorImage(null);
 				} else if (sourceBtn == btnCloak) {
 					hullBox.setCloakImage(null);
 					ship.cloakOverride = null;
 					btnCloaked.setSelection(false);
 					btnCloaked.setEnabled(false);
 				} else if (sourceBtn == btnMiniship) {
 					ship.miniPath = null;
 				}
 				updateButtonImg();
 				updatePainter();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowFile.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				File file = null;
 				if (sourceBtn == btnHull) {
 					file = new File(ship.imagePath);
 				} else if (sourceBtn == btnShields) {
 					file = new File(ship.shieldPath);
 				} else if (sourceBtn == btnFloor) {
 					file = new File(ship.floorPath);
 				} else if (sourceBtn == btnCloak) {
 					file = new File(ship.cloakPath);
 				} else if (sourceBtn == btnMiniship) {
 					file = new File(ship.miniPath);
 				}
 
 				if (Desktop.isDesktopSupported()) {
 						Desktop desktop = Desktop.getDesktop();
 					if (file.exists() && desktop != null) {
 						try {
 							desktop.open(file.getParentFile());
 						} catch (IOException ex) {
 						}
 					}
 				} else {
 					erDialog.print("Error: show file - desktop not supported.");
 				}
 			}
 		});
 		
 	// === STATE BUTTONS
 
 		btnCloaked.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				canvas.redraw();
 			}
 		});
 
 	// === SHELL
 		
 		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
 			public void handleEvent(Event e)
 			{
 				if (e.keyCode == SWT.SHIFT)
 					modShift = false;
 				if (e.keyCode == SWT.ALT)
 					modAlt = false;
 				if (e.keyCode == SWT.CTRL)
 					modCtrl = false;
 			}
 		});
 		
 		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
 			public void handleEvent(Event e) {
 				if (e.keyCode == SWT.ALT)
 					modAlt = true;
 				if (e.keyCode == SWT.SHIFT)
 					modShift = true;
 				if (e.keyCode == SWT.CTRL) {
 					modCtrl = true;
 					
 					if (mouseListener.dragee != null) {
 						mouseListener.dragee.setOffset(mouseListener.dragee.getBounds().x, mouseListener.dragee.getBounds().y);
 					}
 				}
 				
 				// check to make sure that the hotkeys won't be triggered while the user is modifying fields in another window
 				if (shell.isEnabled() && !txtX.isFocusControl() && !txtY.isFocusControl()) {
 					
 						// === element deletion
 					if ((selectedMount != null || selectedRoom != null || selectedDoor != null) && (e.keyCode == SWT.DEL || (e.stateMask == SWT.SHIFT && e.keyCode == 'd'))) {
 						Rectangle redrawBounds = null;
 						if (selectedRoom != null) {
 							Point oldLow = null;
 							Point oldHigh = null;
 							if (Main.ship != null) {
 								oldLow = Main.ship.findLowBounds();
 								oldHigh = Main.ship.findHighBounds();
 								oldLow.x = oldLow.x + (oldHigh.x - oldLow.x)/2;
 								oldLow.y = oldLow.y + (oldHigh.y - oldLow.y)/2;
 							}
 							
 							redrawBounds = selectedRoom.getBounds();
 							selectedRoom.dispose();
 							ship.rooms.remove(selectedRoom);
 							removeUnalignedDoors();
 							ship.reassignID();
 							selectedRoom = null;
 							
 							if (Main.ship != null) {
 								Point p = Main.ship.findLowBounds();
 								Point pt = Main.ship.findHighBounds();
 								p.x = p.x + (pt.x - p.x)/2;
 								p.y = p.y + (pt.y - p.y)/2;
 								
 								pt.x = p.x - oldLow.x;
 								pt.y = p.y - oldLow.y;
 								
 								p = Main.shieldBox.getLocation();
 								Main.shieldBox.setLocation(p.x + pt.x, p.y + pt.y);
 							}
 							
 							if (ship.rooms.size() == 0) {
 								btnShields.setEnabled(false);
 							}
 						} else if (selectedDoor != null) {
 							redrawBounds = selectedDoor.getBounds();
 							selectedDoor.dispose();
 							ship.doors.remove(selectedDoor);
 							selectedDoor = null;
 						} else if (selectedMount != null) {
 							redrawBounds = selectedMount.getBounds();
 							
 							selectedMount.dispose();
 							ship.mounts.remove(selectedMount);
 							selectedMount = null;
 							
 							redrawBounds.x -= 40;
 							redrawBounds.y -= 40;
 							redrawBounds.width += 80;
 							redrawBounds.height += 80;
 						}
 						
 						if (redrawBounds != null)
 							canvasRedraw(redrawBounds, false);
 						
 						// === deselect
 					} else if (e.keyCode == SWT.ESC) {
 						if (selectedRoom != null) selectedRoom.deselect();
 						selectedRoom = null;
 						if (selectedDoor != null) selectedDoor.deselect();
 						selectedDoor = null;
 						if (selectedMount != null) selectedMount.deselect();
 						selectedMount = null;
 						if (hullSelected) hullBox.deselect();
 						if (shieldSelected) shieldBox.deselect();
 						
 						// === file menu options
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 's' && mntmSaveShip.getEnabled()) {
 						mntmSaveShip.notifyListeners(SWT.Selection, null);
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'n') {
 						mntmNewShip.notifyListeners(SWT.Selection, null);
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'l') {
 						mntmLoadShip.notifyListeners(SWT.Selection, null);
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'o') {
 						mntmLoadShipProject.notifyListeners(SWT.Selection, null);
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'e' && mntmExport.getEnabled()) {
 						mntmExport.notifyListeners(SWT.Selection, null);
 						
 						// === show / hide graphics
 					} else if (e.keyCode == '1') {
 						showAnchor = !showAnchor;
 						mntmShowAnchor.setSelection(showAnchor);
 						canvas.redraw();
 					} else if (e.keyCode == '2') {
 						showMounts = !showMounts;
 						showMounts();
 						mntmShowMounts.setSelection(showMounts);
 						canvas.redraw();
 					} else if (e.keyCode == '3') {
 						showRooms = !showRooms;
 						showRooms();
 						mntmShowRooms.setSelection(showRooms);
 						canvas.redraw();
 					} else if (e.keyCode == '4') {
 						showHull = !showHull;
 						hullBox.setVisible(showHull || showFloor);
 						mntmShowHull.setSelection(showHull);
 						canvas.redraw();
 					} else if (e.keyCode == '5') {
 						showFloor = !showFloor;
 						hullBox.setVisible(showHull || showFloor);
 						mntmShowFloor.setSelection(showFloor);
 						canvas.redraw();
 					} else if (e.keyCode == '6') {
 						showShield = !showShield;
 						mntmShowShield.setSelection(showShield);
 						canvas.redraw();
 						
 						// === pin
 					} else if (e.keyCode == '`' || e.keyCode == SWT.SPACE) {
 						if (selectedRoom != null) selectedRoom.setPinned(!selectedRoom.isPinned());
 						if (selectedDoor != null) selectedDoor.setPinned(!selectedDoor.isPinned());
 						if (selectedMount != null) selectedMount.setPinned(!selectedMount.isPinned());
 						if (selectedGib != null) selectedGib.setPinned(!selectedGib.isPinned());
 						if (hullSelected) hullBox.setPinned(!hullBox.isPinned());
 						if (shieldSelected) shieldBox.setPinned(!shieldBox.isPinned());
 						
 						ship.hullPinned = hullBox.isPinned();
 						ship.shieldPinned = shieldBox.isPinned();
 						canvas.redraw();
 						
 						// === tool hotkeys
 					} else if (e.stateMask == SWT.NONE && (e.keyCode == 'q' || e.keyCode == 'w' || e.keyCode == 'e' || e.keyCode == 'r' || e.keyCode == 't' || e.keyCode == 'g')) {
 						deselectAll();
 						boolean prev = tltmGib.getSelection();
 						tltmPointer.setSelection(e.keyCode == 'q');
 						tltmRoom.setSelection(e.keyCode == 'w');
 						tltmDoor.setSelection(e.keyCode == 'e');
 						tltmMount.setSelection(e.keyCode == 'r');
 						tltmSystem.setSelection(e.keyCode == 't');
 						tltmGib.setSelection(e.keyCode == 'g');
 						
 						gibDialog.setVisible(tltmGib.getSelection());
 						if (prev && !tltmGib.getSelection()) gibWindow.escape();
 
 						btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 						if (prev || tltmGib.getSelection())
 							canvas.redraw();
 						
 						// === nudge function
 					} else if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT) {
 						// sending it to an auxiliary function as to not make a clutter here
 						if (e.stateMask != SWT.CTRL)
 							nudgeSelected(e.keyCode);
 					}
 				}
 			}
 		});
 		
 		SelectionAdapter adapter = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (gibDialog.isVisible()) {
 					gibDialog.setVisible(false);
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath));
 					canvas.redraw();
 				}
 			}
 		};
 		
 		tltmPointer.addSelectionListener(adapter);
 		tltmRoom.addSelectionListener(adapter);
 		tltmDoor.addSelectionListener(adapter);
 		tltmMount.addSelectionListener(adapter);
 		tltmSystem.addSelectionListener(adapter);
 		
 		tltmGib.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				gibDialog.setVisible(true);
 				btnCloaked.setEnabled(false);
 				canvas.redraw();
 			}
 		});
 		
 	// === SYSTEM CONTEXT MENU
 		
 		menuSystem.addMenuListener(new MenuAdapter() {
 			@Override
 			public void menuShown(MenuEvent e) {
 				if (selectedRoom != null) {
 					mntmEmpty.setSelection(selectedRoom.getSystem() == Systems.EMPTY);
 					// === subsystems
 					mntmPilot.setSelection(selectedRoom.getSystem() == Systems.PILOT);
 					mntmPilot.setEnabled(!isSystemAssigned(Systems.PILOT, selectedRoom));
 					mntmSensors.setSelection(selectedRoom.getSystem() == Systems.SENSORS);
 					mntmSensors.setEnabled(!isSystemAssigned(Systems.SENSORS, selectedRoom));
 					mntmDoors.setSelection(selectedRoom.getSystem() == Systems.DOORS);
 					mntmDoors.setEnabled(!isSystemAssigned(Systems.DOORS, selectedRoom));
 					// === systems
 					mntmEngines.setSelection(selectedRoom.getSystem() == Systems.ENGINES);
 					mntmEngines.setEnabled(!isSystemAssigned(Systems.ENGINES, selectedRoom));
 					mntmMedbay.setSelection(selectedRoom.getSystem() == Systems.MEDBAY);
 					mntmMedbay.setEnabled(!isSystemAssigned(Systems.MEDBAY, selectedRoom));
 					mntmOxygen.setSelection(selectedRoom.getSystem() == Systems.OXYGEN);
 					mntmOxygen.setEnabled(!isSystemAssigned(Systems.OXYGEN, selectedRoom));
 					mntmShields.setSelection(selectedRoom.getSystem() == Systems.SHIELDS);
 					mntmShields.setEnabled(!isSystemAssigned(Systems.SHIELDS, selectedRoom));
 					mntmWeapons.setSelection(selectedRoom.getSystem() == Systems.WEAPONS);
 					mntmWeapons.setEnabled(!isSystemAssigned(Systems.WEAPONS, selectedRoom));
 					// === special
 					mntmArtillery.setSelection(selectedRoom.getSystem() == Systems.ARTILLERY);
 					mntmArtillery.setEnabled(!isSystemAssigned(Systems.ARTILLERY, selectedRoom));
 					mntmCloaking.setSelection(selectedRoom.getSystem() == Systems.CLOAKING);
 					mntmCloaking.setEnabled(!isSystemAssigned(Systems.CLOAKING, selectedRoom));
 					mntmDrones.setSelection(selectedRoom.getSystem() == Systems.DRONES);
 					mntmDrones.setEnabled(!isSystemAssigned(Systems.DRONES, selectedRoom));
 					mntmTeleporter.setSelection(selectedRoom.getSystem() == Systems.TELEPORTER);
 					mntmTeleporter.setEnabled(!isSystemAssigned(Systems.TELEPORTER, selectedRoom));
 					// ===
 					mntmSysImage.setEnabled(!selectedRoom.getSystem().equals(Systems.EMPTY));
 				}
 			}
 		});
 		
 		mntmEmpty.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				selectedRoom.assignSystem(Systems.EMPTY);
 				canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 			} });
 		mntmOxygen.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.OXYGEN, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.OXYGEN);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmMedbay.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.MEDBAY, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.MEDBAY);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmShields.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.SHIELDS, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.SHIELDS);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmWeapons.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.WEAPONS, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.WEAPONS);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmEngines.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.ENGINES, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.ENGINES);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmDoors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.DOORS, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.DOORS);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmPilot.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.PILOT, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.PILOT);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmSensors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.SENSORS, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.SENSORS);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmDrones.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.DRONES, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.DRONES);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmArtillery.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.ARTILLERY, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.ARTILLERY);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmTeleporter.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.TELEPORTER, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.TELEPORTER);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmCloaking.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.CLOAKING, selectedRoom)) {
 					selectedRoom.assignSystem(Systems.CLOAKING);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			} });
 		mntmSysImage.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.png"};
 				dialog.setFilterExtensions(filterExtensions);
 				
 				dialog.setFilterPath(interiorPath);
 				dialog.setFileName(interiorPath);
 
 				String path = dialog.open();
 				
 				if (!ShipIO.isNull(path) && selectedRoom != null) {
 					interiorPath = new String(path);
 					selectedRoom.setInterior(path);
 				}
 			}
 		});
 
 	// === GIB CONTEXT MENU
 
 		menuGib.addMenuListener(new MenuAdapter() {
 			@Override
 			public void menuShown(MenuEvent e) {
 				for (MenuItem mntm : menuGibItems) {
 					if (!mntm.isDisposed())
 						mntm.dispose();
 					mntm = null;
 				}
 				menuGibItems.clear();
 				
 				MenuItem none = new MenuItem(menuGib, SWT.RADIO);
 				none.setText("None");
 				none.setSelection(Main.selectedMount != null);
 				addListenerToGibItem(none, null);
 				menuGibItems.add(none);
 				
 				MenuItem temp = null;
 				for (FTLGib g : ship.gibs) {
 					temp = new MenuItem(menuGib, SWT.RADIO);
 					temp.setText("Gib " + g.number);
 					temp.setSelection(Main.selectedMount != null && Main.selectedMount.gib == g.number);
 					if (temp.getSelection()) 
 						none.setSelection(false);
 					addListenerToGibItem(temp, g);
 					menuGibItems.add(temp);
 				}
 			}
 		});
 		
 	// === FILE MENU
 		
 		mntmNewShip.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				
 				int create = new NewShipWindow(shell).open();
 				shell.setEnabled(true);
 				if (create != 0) {
 					mntmClose.notifyListeners(SWT.Selection, null);
 					
 					ship = new FTLShip();
 					ship.isPlayer = create == 1;
 					anchor.setLocation(140, 140);
 					Main.ship.anchor.x = 140;
 					Main.ship.anchor.y = 140;
 					Main.ship.offset.x = 0;
 					Main.ship.offset.y = 0;
 					
 					print("New ship created.");
 					
 					anchor.setVisible(true);
 					
 					tltmPointer.setEnabled(true);
 					tltmRoom.setEnabled(true);
 					tltmDoor.setEnabled(true);
 					tltmMount.setEnabled(true);
 					tltmSystem.setEnabled(true);
 					tltmGib.setEnabled(true);
 					btnHull.setEnabled(true);
 					if (ship.rooms.size() > 0) {
 						btnShields.setEnabled(ship.isPlayer);
 						btnShields.setToolTipText(null);
 					}
 					btnCloak.setEnabled(true);
 					btnFloor.setEnabled(ship.isPlayer);
 					btnMiniship.setEnabled(ship.isPlayer);
 					btnShipProperties.setEnabled(true);
 					updateButtonImg();
 					
 					if (!ship.isPlayer) {
 						ship.shieldPath = resPath + ShipIO.pathDelimiter + "img" + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "enemy_shields.png";
 						ShipIO.loadImage(ship.shieldPath, "shields");
 						shieldEllipse.x = GRID_W*35/2-100;
 						shieldEllipse.y = GRID_H*35/2-100;
 						shieldEllipse.width = 200;
 						shieldEllipse.height = 200;
 					}
 					
 					mntmSaveShip.setEnabled(true);
 					mntmSaveShipAs.setEnabled(true);
 					mntmExport.setEnabled(true);
 					mntmClose.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					
 					currentPath = null;
 					
 					mntmConToPlayer.setEnabled(!ship.isPlayer);
 					mntmConToEnemy.setEnabled(ship.isPlayer);
 
 					canvas.redraw();
 				}
 			}
 		});
 		
 		mntmLoadShip.addSelectionListener(new SelectionAdapter() {
 			@SuppressWarnings("static-access")
 			public void widgetSelected(SelectionEvent e) {
 				ShipBrowser shipBrowser = new ShipBrowser(shell);
 				shipBrowser.shell.open();
 
 				shipBrowser.shell.addDisposeListener(new DisposeListener() {
 					@Override
 					public void widgetDisposed(DisposeEvent e)
 					{
 						if (ship != null) {
 							anchor.setVisible(true);
 							
 							tltmPointer.setEnabled(true);
 							tltmRoom.setEnabled(true);
 							tltmDoor.setEnabled(true);
 							tltmMount.setEnabled(true);
 							tltmSystem.setEnabled(true);
 							tltmGib.setEnabled(true);
 							btnHull.setEnabled(true);
 							if (ship.rooms.size() > 0) {
 								btnShields.setEnabled(ship.isPlayer);
 								btnShields.setToolTipText(null);
 							}
 							btnCloak.setEnabled(true);
 							btnFloor.setEnabled(ship.isPlayer);
 							btnMiniship.setEnabled(ship.isPlayer);
 							btnShipProperties.setEnabled(true);
 							updateButtonImg();
 							
 							mntmSaveShip.setEnabled(true);
 							mntmSaveShipAs.setEnabled(true);
 							mntmExport.setEnabled(true);
 							mntmClose.setEnabled(true);
 							mntmArchives.setEnabled(false);
 							
 							if (ship.isPlayer) {
 								if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
 									Rectangle temp = shieldImage.getBounds();
 									shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
 									shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
 									shieldEllipse.width = temp.width;
 									shieldEllipse.height = temp.height;
 								} else {
 									shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - ship.ellipse.width + ship.ellipse.x;
 									shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - ship.ellipse.height + ship.ellipse.y;
 									shieldEllipse.width = ship.ellipse.width*2;
 									shieldEllipse.height = ship.ellipse.height*2;
 								}
 							} else {
 								shieldEllipse.width = ship.ellipse.width*2;
 								shieldEllipse.height = ship.ellipse.height*2;
 								shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 + ship.ellipse.x - ship.ellipse.width;
 								shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 + ship.ellipse.y - ship.ellipse.height + 110;
 							}
 							//ShipIO.updateIndexImgMaps();
 							
 							btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 							
 							currentPath = null;
 							
 							mntmConToPlayer.setEnabled(!ship.isPlayer);
 							mntmConToEnemy.setEnabled(ship.isPlayer);
 							
 							canvas.redraw();
 						}
 						if (ShipIO.errors.size() == 0 && Main.ship != null) {
 							Main.print(((Main.ship.shipName!=null)?(Main.ship.shipClass + " \"" + Main.ship.shipName + "\""):(Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
 						} else if (ShipIO.errors.size() > 0) {
 							Main.print("Errors occured during ship loading; some data may be missing.");
 							Main.erDialog.printErrors(ShipIO.errors);
 							Main.erDialog.open();
 							
 							ShipIO.errors.clear();
 						}
 					}
 				});
 			}
 		});
 		
 		mntmLoadShipFTL.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] {"*.ftl"};
 				dialog.setFilterExtensions(filterExtensions);
 				
 				if (ShipIO.isNull(interiorPath)) interiorPath = resPath;
 				dialog.setFilterPath(interiorPath);
 				
 				dialog.setText("");
 				String path = dialog.open();
 				
 				if (!ShipIO.isNull(path)) {
 					debug("Load ship from .ftl:", true);
 					mntmClose.notifyListeners(SWT.Selection, null);
 					
 					ZipFile zf = null;
 					try {
 						zf = new ZipFile(path);
 						File temp = new File("sprlmnl_tmp");
 						temp.mkdirs();
 
 						debug("\textracting contents of .ftl package to temporary directory... ", false);
 						ShipIO.unzipFileToDirectory(zf, temp);
 						debug("done", true);
 						
 						File unpacked_blueprints = null;
 						unpacked_blueprints = new File("sprlmnl_tmp" + ShipIO.pathDelimiter + "data" + ShipIO.pathDelimiter + "autoBlueprints.xml.append");
 						ArrayList<String> blueList = new ArrayList<String>();
 						ArrayList<String> tempList = null;
 						
 						debug("\tscanning " + unpacked_blueprints.getName() + " for ship blueprints... ", false);
 						tempList = (ArrayList<String>) ShipIO.preScanFTL(unpacked_blueprints);
 						debug("done" + ((blueList == null || blueList.size()==0) ? ", none found" : ""), true);
 						
 						if (tempList != null) blueList.addAll(tempList);
 						
 						if (blueList == null || blueList.size()==0) {
 							unpacked_blueprints = new File("sprlmnl_tmp" + ShipIO.pathDelimiter + "data" + ShipIO.pathDelimiter + "blueprints.xml.append");
 							debug("\tscanning " + unpacked_blueprints.getName() + " for ship blueprints... ", false);
 							blueList = (ArrayList<String>) ShipIO.preScanFTL(unpacked_blueprints);
 							debug("done" + ((blueList == null || blueList.size()==0) ? ", none found" : ""), true);
 							
 							if (tempList != null) blueList.addAll(tempList);
 						}
 						
 						if (blueList.size()!=0) {
 							if (blueList.size() == 1) {
 								ShipIO.loadShip(blueList.get(0), unpacked_blueprints);
 							} else {
 								// display dialog
 							}
 						} else {
 							Main.erDialog.print("Error: load ship from .ftl - no ship declarations found in the package. No data was loaded.");
 						}
 						
 						debug("\tdeleting temporary directory... ", false);
 						ShipIO.deleteFolderContents(temp);
 						if (temp.exists()) ShipIO.rmdir(temp);
 						debug("done", true);
 					} catch (IOException ex) {
 					} finally {
 						if (zf != null)
 							try {
 								zf.close();
 							} catch (IOException ex) {
 							}
 					}
 				}
 
 				if (ship != null) {
 					anchor.setVisible(true);
 					
 					tltmPointer.setEnabled(true);
 					tltmRoom.setEnabled(true);
 					tltmDoor.setEnabled(true);
 					tltmMount.setEnabled(true);
 					tltmSystem.setEnabled(true);
 					tltmGib.setEnabled(true);
 					btnHull.setEnabled(true);
 					if (ship.rooms.size() > 0) {
 						btnShields.setEnabled(ship.isPlayer);
 						btnShields.setToolTipText(null);
 					}
 					btnCloak.setEnabled(true);
 					btnFloor.setEnabled(ship.isPlayer);
 					btnMiniship.setEnabled(ship.isPlayer);
 					btnShipProperties.setEnabled(true);
 					updateButtonImg();
 					
 					mntmSaveShip.setEnabled(true);
 					mntmSaveShipAs.setEnabled(true);
 					mntmExport.setEnabled(true);
 					mntmClose.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					
 					if (ship.isPlayer) {
 						if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
 							Rectangle temp = shieldImage.getBounds();
 							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						} else {
 							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - ship.ellipse.width + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - ship.ellipse.height + ship.ellipse.y;
 							shieldEllipse.width = ship.ellipse.width*2;
 							shieldEllipse.height = ship.ellipse.height*2;
 						}
 					} else {
 						shieldEllipse.width = ship.ellipse.width*2;
 						shieldEllipse.height = ship.ellipse.height*2;
 						shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 + ship.ellipse.x - ship.ellipse.width;
 						shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 + ship.ellipse.y - ship.ellipse.height + 110;
 					}
 					//ShipIO.updateIndexImgMaps();
 					
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 					
 					currentPath = null;
 					
 					mntmConToPlayer.setEnabled(!ship.isPlayer);
 					mntmConToEnemy.setEnabled(ship.isPlayer);
 					
 					canvas.redraw();
 				}
 				if (ShipIO.errors.size() == 0 && Main.ship != null) {
 					Main.print(((Main.ship.shipName!=null)?(Main.ship.shipClass + " - " + Main.ship.shipName):(Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
 				} else if (ShipIO.errors.size() > 0) {
 					Main.print("Errors occured during ship loading; some data may be missing.");
 					Main.erDialog.printErrors(ShipIO.errors);
 					Main.erDialog.open();
 					
 					ShipIO.errors.clear();
 				}
 			}
 		});
 		
 		mntmSaveShip.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentPath == null) {
 					ShipIO.askSaveDir();
 				} else {
 					ShipIO.saveShipProject(currentPath);
 					
 					ConfigIO.saveConfig();
 					print("Project saved successfully.");
 				}
 				
 				ConfigIO.saveConfig();
 			}
 		});
 		
 		mntmSaveShipAs.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ShipIO.askSaveDir();
 				
 				ConfigIO.saveConfig();
 				print("Project saved successfully.");
 			}
 		});
 		
 		mntmExport.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				exDialog = new ExportDialog(shell);
 				exDialog.open();
 				
 				shell.setEnabled(true);
 			}
 		});
 		
 		mntmLoadShipProject.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ShipIO.askLoadDir();
 				
 				if (ship != null) {
 					anchor.setVisible(true);
 					
 					tltmPointer.setEnabled(true);
 					tltmRoom.setEnabled(true);
 					tltmDoor.setEnabled(true);
 					tltmMount.setEnabled(true);
 					tltmSystem.setEnabled(true);
 					tltmGib.setEnabled(true);
 					btnHull.setEnabled(true);
 					if (ship.rooms.size() > 0) {
 						btnShields.setEnabled(ship.isPlayer);
 						btnShields.setToolTipText(null);
 					}
 					btnCloak.setEnabled(true);
 					btnFloor.setEnabled(ship.isPlayer);
 					btnMiniship.setEnabled(ship.isPlayer);
 					btnShipProperties.setEnabled(true);
 					updateButtonImg();
 
 					if (ship.isPlayer) {
 						if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
 							Rectangle temp = shieldImage.getBounds();
 							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - temp.width/2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - temp.height/2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						} else {
 							shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 - ship.ellipse.width + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y*35 + ship.computeShipSize().y/2 - ship.ellipse.height + ship.ellipse.y;
 							shieldEllipse.width = ship.ellipse.width*2;
 							shieldEllipse.height = ship.ellipse.height*2;
 						}
 					} else {
 						shieldEllipse.width = ship.ellipse.width*2;
 						shieldEllipse.height = ship.ellipse.height*2;
 						shieldEllipse.x = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().x/2 + ship.ellipse.x - ship.ellipse.width;
 						shieldEllipse.y = ship.anchor.x + ship.offset.x*35 + ship.computeShipSize().y/2 + ship.ellipse.y - ship.ellipse.height + 110;
 					}
 					
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 
 					mntmSaveShip.setEnabled(true);
 					mntmSaveShipAs.setEnabled(true);
 					mntmExport.setEnabled(true);
 					mntmClose.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					
 					mntmConToPlayer.setEnabled(!ship.isPlayer);
 					mntmConToEnemy.setEnabled(ship.isPlayer);
 					
 					ConfigIO.saveConfig();
 					
 					canvas.redraw();
 				}
 				
 				if (ShipIO.errors.size() == 0) {
 					Main.print("Project loaded successfully.");
 				} else {
 					Main.print("Errors occured during project loading. Some data may be missing");
 					Main.erDialog.printErrors(ShipIO.errors);
 					Main.erDialog.open();
 					ShipIO.errors.clear();
 				}
 			}
 		});
 		
 		mntmArchives.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				shell.setEnabled(false);
 				dirWindow.label.setText("If you wish to unpack the archives again, just browse to the .dat files again." + ShipIO.lineDelimiter
 						+ "Note however that the old unpacked archives used by Superluminal will be deleted.");
 				dirWindow.btnClose.setEnabled(true);
 				dirWindow.open();
 			}
 		});
 		
 		mntmClose.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (ship != null) {
 					for (MenuItem mntm : menuGibItems) {
 						if (!mntm.isDisposed())
 							mntm.dispose();
 						mntm = null;
 					}
 					menuGibItems.clear();
 					
 					for (FTLRoom r : ship.rooms)
 						r.dispose();
 					for (FTLDoor d : ship.doors)
 						d.dispose();
 					for (FTLMount m : ship.mounts)
 						m.dispose();
 					for (FTLGib g : ship.gibs)
 						g.dispose();
 					
 					ship.rooms.clear();
 					ship.doors.clear();
 					ship.mounts.clear();
 					ship.gibs.clear();
 					
 					hullBox.setHullImage(null);
 					hullBox.setFloorImage(null);
 					hullBox.setCloakImage(null);
 					shieldBox.setImage(null, true);
 					
 					gibDialog.clearList();
 					gibDialog.letters.clear();
 				}
 				
 				btnCloaked.setEnabled(false);
 				idList.clear();
 				clearButtonImg();
 				currentPath = null;
 				
 				shieldEllipse.x = 0;
 				shieldEllipse.y = 0;
 				shieldEllipse.width = 0;
 				shieldEllipse.height = 0;
 				
 				hullBox.setLocation(0, 0);
 				hullBox.setSize(0, 0);
 				shieldBox.setLocation(0, 0);
 				shieldBox.setSize(0, 0);
 				
 				anchor.setVisible(false);
 				
 				tltmPointer.setEnabled(false);
 				tltmRoom.setEnabled(false);
 				tltmDoor.setEnabled(false);
 				tltmMount.setEnabled(false);
 				tltmSystem.setEnabled(false);
 				tltmGib.setEnabled(false);
 				btnHull.setEnabled(false);
 				btnShields.setEnabled(false);
 				btnCloak.setEnabled(false);
 				btnFloor.setEnabled(false);
 				btnMiniship.setEnabled(false);
 				btnShipProperties.setEnabled(false);
 				txtX.setEnabled(false);
 				txtY.setEnabled(false);
 
 				mntmSaveShip.setEnabled(false);
 				mntmSaveShipAs.setEnabled(false);
 				mntmExport.setEnabled(false);
 				mntmClose.setEnabled(false);
 				mntmArchives.setEnabled(true);
 
 				ship = null;
 				
 				canvas.redraw();
 			}
 		});
 		
 	// === EDIT MENU
 		
 		mntmRemoveDoors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeDoor = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 
 		mntmArbitraryPositionOverride.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				arbitraryPosOverride = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 			}
 		});
 		
 		mntmConToPlayer.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				convertToPlayer();
 			}
 		});
 		
 		mntmConToEnemy.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				convertToEnemy();
 			}
 		});
 	
 	// === VIEW MENU
 		
 		mntmOpenErrorsConsole.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				erDialog.open();
 			}
 		});
 
 		mntmShowAnchor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showAnchor = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowMounts.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showMounts = ((MenuItem) e.widget).getSelection();
 				showMounts();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowRooms.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showRooms = ((MenuItem) e.widget).getSelection();
 				showRooms();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowHull.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showHull = ((MenuItem) e.widget).getSelection();
 				hullBox.setVisible(showHull || showFloor);
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowFloor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showFloor = ((MenuItem) e.widget).getSelection();
 				hullBox.setVisible(showHull || showFloor);
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 		
 		mntmShowShield.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showShield = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 
 		mntmLoadFloorGraphic.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				loadFloor = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 
 		mntmLoadShieldGraphic.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				loadShield = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 
 		mntmLoadSystem.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				loadSystem = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 				canvas.redraw();
 			}
 		});
 	}
 	
 //======================================================
 // === BOOKMARK: AUXILIARY METHODS
 	
 	// === SHIP CONVERSIONS
 	
 	public void convertToPlayer() {
 		if (Main.ship != null) {
 			ship.isPlayer = true;
 			
 			btnShields.setEnabled(ship.rooms.size() > 0);
 			
 			ship.shieldPath = null;
 			ship.shieldOverride = null;
 			if (shieldImage != null && !shieldImage.isDisposed() && !ShipIO.loadingSwitch)
 				shieldImage.dispose();
 			shieldImage = null;
 			
 			shieldEllipse.x = 0;
 			shieldEllipse.y = 0;
 			shieldEllipse.width = 0;
 			shieldEllipse.height = 0;
 			
 			updateButtonImg();
 			
 			btnFloor.setEnabled(true);
 			btnMiniship.setEnabled(true);
 			
 			if (ship.weaponsBySet) ship.weaponSet.clear();
 			if (ship.dronesBySet) ship.droneSet.clear();
 			ship.weaponsBySet = false;
 			ship.dronesBySet = false;
 			
 			ship.minSec = 0;
 			ship.maxSec = 0;
 			
 			ship.crewMax = 8;
 
 			mntmConToPlayer.setEnabled(false);
 			mntmConToEnemy.setEnabled(true);
 			
 			print("Ship converted to player.");
 		}
 	}
 	
 	public void convertToEnemy() {
 		if (Main.ship != null) {
 			ship.isPlayer = false;
 			
 			ship.shipName = null;
 			ship.descr = null;
 			
 			btnShields.setEnabled(false);
 			
 			if (shieldImage != null && !shieldImage.isDisposed() && !ShipIO.loadingSwitch)
 				shieldImage.dispose();
 			shieldImage = null;
 
 			ship.shieldOverride = null;
 			ship.shieldPath = resPath + ShipIO.pathDelimiter + "img" + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "enemy_shields.png";
 			ShipIO.loadImage(ship.shieldPath, "shields");
 			
 			if (floorImage != null && !floorImage.isDisposed() && !ShipIO.loadingSwitch)
 				floorImage.dispose();
 			floorImage = null;
 			ship.floorPath = null;
 			ship.cloakOverride = null;
 			
 			ship.miniPath = null;
 			
 			btnFloor.setEnabled(false);
 			btnMiniship.setEnabled(false);
 			
 			updateButtonImg();
 			
 			mntmConToPlayer.setEnabled(true);
 			mntmConToEnemy.setEnabled(false);
 			
 			print("Ship converted to enemy.");
 		}
 	}
 
 	// === ROUNDING TO GRID
 	
 	/**
 	 * Aligns to closest line of the grid.
 	 */
 	public static int roundToGrid(int a) {
 		return Math.round(a/35)*35;
 	}
 	/**
 	 * Aligns to the lowest (left-most / top-most) line of the grid.
 	 */
 	public static int downToGrid(int a) {
 		return (int) (Math.ceil(a/35)*35);
 	}
 	/**
 	 * Aligns to the highest (right-most / bottom-most) line of the grid.
 	 */
 	public static int upToGrid(int a) {
 		return (int) (Math.floor(a/35)*35);
 	}
 	
 
 	//=================
 	// === GENERAL
 
 	public static void canvasRedraw(Rectangle rect, boolean all) {
 		Main.canvas.redraw(rect.x, rect.y, rect.width, rect.height, all);
 	}
 	
 	public static void copyRect(Rectangle source, Rectangle destination) {
 		if (source != null && destination != null) {
 			destination.x = source.x;
 			destination.y = source.y;
 			destination.width = source.width;
 			destination.height = source.height;
 		}
 	}
 	
 	public static Rectangle cloneRect(Rectangle rect) {
 		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
 	}
 
 	/**
 	 * Fixes a rectangle to have positive values height and width, moving its (x,y) origin as needed.
 	 * The end effect is a rectangle that stays in exactly the same place, but has positive height and width.
 	 * @param r Rectangle that needs to be fixed.
 	 * @return Fixed rectangle
 	 */
 	public static Rectangle fixRect(Rectangle r) {
 		Rectangle rect = new Rectangle(0,0,0,0);
 		rect.x = r.width<0 ? r.x+r.width : r.x;
 		rect.y = r.height<0 ? r.y+r.height : r.y;
 		rect.width = r.width<0 ? -r.width : r.width;
 		rect.height = r.height<0 ? -r.height : r.height;
 		return rect;
 	}
 
 	/**
 	 * Checks if given rect overlaps any of the already placed rooms. If given rect is inside the roomsList set, it doesn't perform check against that rect (meaning it won't return true).
 	 * 
 	 * @param rect rectangle to be checked
 	 * @param treatAs if set to another rectangle, the self-exclusive check will be performed against that rectangle, and not the one in the first parameter. Can be set to null if not used.
 	 * @return true if rect given in parameter overlaps any of already placed rooms/rects.
 	 */
 	public static boolean doesRectOverlap(Rectangle rect, Rectangle treatAs) {
 		for (FTLRoom r : ship.rooms) {
 			if (rect.intersects(r.getBounds()) && ((treatAs != null && r.getBounds() != treatAs) || (treatAs == null && r.getBounds() != rect)) ) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if a rect is wholly contained within another.
 	 */
 	public static boolean containsRect(Rectangle r1, Rectangle r2) {
 		return r1.contains(r2.x, r2.y) && r1.contains(r2.x+r2.width, r2.y+r2.height); 
 	}
 	
 	public Rectangle getRectFromClick() {
 		Rectangle tempRect = new Rectangle(0,0,35,35);
 		for(int x=0; x<GRID_W; x++) {
 			for(int y=0; y<GRID_H; y++) {
 				tempRect.x = x*35;
 				tempRect.y = y*35;
 				if (tempRect.contains(mousePosLastClick)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public static Rectangle getRectAt(int x, int y) {
 		Rectangle tempRect = new Rectangle(0,0,35,35);
 		Point p = new Point(x, y);
 		for(int i=0; i<GRID_W; i++) {
 			for(int j=0; j<GRID_H; j++) {
 				tempRect.x = i*35;
 				tempRect.y = j*35;
 				if (tempRect.contains(p)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public Rectangle getRectFromMouse() {
 		Rectangle tempRect = new Rectangle(0,0,35,35);
 		for(int x=0; x<GRID_W; x++) {
 			for(int y=0; y<GRID_H; y++) {
 				tempRect.x = x*35;
 				tempRect.y = y*35;
 				if (tempRect.contains(mousePos)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void nudgeSelected(int event) {
 		// check so that if none is selected the function won't even bother going in
 		if (hullSelected || shieldSelected || selectedGib != null || selectedMount != null || selectedRoom != null) {
 			switch (event) {
 				case (SWT.ARROW_UP):
 					if (selectedRoom != null) {
 						phantomRect = cloneRect(selectedRoom.getBounds());
 						phantomRect.y -= 35;
 						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.y >= ship.anchor.y)
 							selectedRoom.setLocation(selectedRoom.getLocation().x, selectedRoom.getLocation().y-35);
 					}
 					if (selectedMount != null) selectedMount.setLocation(selectedMount.getLocation().x, selectedMount.getLocation().y - ((modShift) ? 35 : 1));
 					if (selectedGib != null && tltmGib.getSelection()) selectedGib.setLocationAbsolute(selectedGib.getLocation().x, selectedGib.getLocation().y - ((modShift) ? 35 : 1));
 					if (hullSelected) hullBox.setLocation(hullBox.getLocation().x, hullBox.getLocation().y - ((modShift) ? 35 : 1));
 					if (shieldSelected) shieldBox.setLocation(shieldBox.getLocation().x, shieldBox.getLocation().y - ((modShift) ? 35 : 1));
 					break;
 				case (SWT.ARROW_DOWN):
 					if (selectedRoom != null) {
 						phantomRect = cloneRect(selectedRoom.getBounds());
 						phantomRect.y += 35;
 						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.y + phantomRect.height <= GRID_H*35)
 							selectedRoom.setLocation(selectedRoom.getLocation().x, selectedRoom.getLocation().y+35);
 					}
 					if (selectedMount != null) selectedMount.setLocation(selectedMount.getLocation().x, selectedMount.getLocation().y + ((modShift) ? 35 : 1));
 					if (selectedGib != null && tltmGib.getSelection()) selectedGib.setLocationAbsolute(selectedGib.getLocation().x, selectedGib.getLocation().y + ((modShift) ? 35 : 1));
 					if (hullSelected) hullBox.setLocation(hullBox.getLocation().x, hullBox.getLocation().y + ((modShift) ? 35 : 1));
 					if (shieldSelected) shieldBox.setLocation(shieldBox.getLocation().x, shieldBox.getLocation().y + ((modShift) ? 35 : 1));
 					break;
 				case (SWT.ARROW_LEFT):
 					if (selectedRoom != null) {
 						phantomRect = cloneRect(selectedRoom.getBounds());
 						phantomRect.x -= 35;
 						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.x >= ship.anchor.x)
 							selectedRoom.setLocation(selectedRoom.getLocation().x-35, selectedRoom.getLocation().y);
 					}
 					if (selectedMount != null) selectedMount.setLocation(selectedMount.getLocation().x - ((modShift) ? 35 : 1), selectedMount.getLocation().y);
 					if (selectedGib != null && tltmGib.getSelection()) selectedGib.setLocationAbsolute(selectedGib.getLocation().x - ((modShift) ? 35 : 1), selectedGib.getLocation().y);
 					if (hullSelected) hullBox.setLocation(hullBox.getLocation().x - ((modShift) ? 35 : 1), hullBox.getLocation().y);
 					if (shieldSelected) shieldBox.setLocation(shieldBox.getLocation().x - ((modShift) ? 35 : 1), shieldBox.getLocation().y);
 					break;
 				case (SWT.ARROW_RIGHT):
 					if (selectedRoom != null) {
 						phantomRect = cloneRect(selectedRoom.getBounds());
 						phantomRect.x += 35;
 						if (!doesRectOverlap(phantomRect, selectedRoom.getBounds()) && phantomRect.x + phantomRect.width <= GRID_W*35)
 							selectedRoom.setLocation(selectedRoom.getLocation().x+35, selectedRoom.getLocation().y);
 					}
 					if (selectedMount != null) selectedMount.setLocation(selectedMount.getLocation().x + ((modShift) ? 35 : 1), selectedMount.getLocation().y);
 					if (selectedGib != null && tltmGib.getSelection()) selectedGib.setLocationAbsolute(selectedGib.getLocation().x + ((modShift) ? 35 : 1), selectedGib.getLocation().y);
 					if (hullSelected) hullBox.setLocation(hullBox.getLocation().x + ((modShift) ? 35 : 1), hullBox.getLocation().y);
 					if (shieldSelected) shieldBox.setLocation(shieldBox.getLocation().x + ((modShift) ? 35 : 1), shieldBox.getLocation().y);
 					break;
 				default: break;
 			}
 			
 			if (selectedRoom != null) updateCorners(selectedRoom);
 			if (shieldSelected) {
 				ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width/2) - (ship.findLowBounds().x + ship.computeShipSize().x/2);
 				ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height/2) - (ship.findLowBounds().y + ship.computeShipSize().y/2) - ((ship.isPlayer) ? 0 : 110);
 				ship.ellipse.width = shieldEllipse.width/2;
 				ship.ellipse.height = shieldEllipse.height/2;
 			}
 			updateSelectedPosText();
 			
 			canvas.redraw();
 		}
 	}
 	
 
 	//=================
 	// === ROOM RELATED
 	
 	public static void updateCorners(FTLRoom r) {
 		corners[0] = new Rectangle(r.getBounds().x, r.getBounds().y, CORNER, CORNER);
 		corners[1] = new Rectangle(r.getBounds().x+r.getBounds().width-CORNER, r.getBounds().y, CORNER, CORNER);
 		corners[2] = new Rectangle(r.getBounds().x, r.getBounds().y+r.getBounds().height-CORNER, CORNER, CORNER);
 		corners[3] = new Rectangle(r.getBounds().x+r.getBounds().width-CORNER, r.getBounds().y+r.getBounds().height-CORNER, CORNER, CORNER);
 	}
 
 	public static int getLowestId() {
 		int i = -1;
 		idList.add(-1);
 		while(i < GRID_W*GRID_H && idList.contains(i)) {
 			i++;
 		}
 		return i;
 	}
 	
 	public static Point findFarthestCorner(FTLRoom r, Point p) {
 		double d = 0;
 		double t = 0;
 		Point pt = null;
 		for (int i = 0; i < 4; i++) {
 			t = Math.sqrt( Math.pow(r.corners[i].x - p.x, 2) + Math.pow(r.corners[i].y - p.y,2) );
 			if (d<t) {
 				d = t;
 				pt = r.corners[i];
 			}	
 		}
 		return pt;
 	}
 	
 	public static FTLRoom getRoomContainingRect(Rectangle rect) {
 		if (rect != null) {
 			for (FTLRoom r : ship.rooms) {
 				if (r.getBounds().intersects(rect))
 					return r;
 			}
 		}
 		return null;
 	}
 
 	public static boolean isSystemAssigned(Systems sys, FTLRoom r) {
 		for (FTLRoom rm : ship.rooms) {
 			if (r != null && rm != r && rm.getSystem() == sys)
 				return true;
 		}
 		return false;
 	}
 	
 	public static boolean isSystemAssigned(Systems sys) {
 		for (FTLRoom rm : ship.rooms) {
 			if (rm.getSystem() == sys)
 				return true;
 		}
 		return false;
 	}
 	
 	public static boolean isSystemAssigned(Systems sys, FTLShip ship) {
 		for (FTLRoom rm : ship.rooms) {
 			if (rm.getSystem() == sys)
 				return true;
 		}
 		return false;
 	}
 	
 	public static FTLRoom getRoomWithSystem(Systems sys) {
 		for (FTLRoom rm : ship.rooms) {
 			if (rm.getSystem().equals(sys))
 				return rm;
 		}
 		return null;
 	}
 	
 	public static Rectangle getRectFromStation(FTLRoom r) {
 		r.slot = Main.ship.slotMap.get(r.getSystem());
 		int w = r.getBounds().width/35;
 		int y = (int) Math.floor(r.slot/w);
 		int x = r.slot - y* w;
 		
 		return new Rectangle(r.getBounds().x+x*35, r.getBounds().y+y*35, 35, 35);
 	}
 
 	public static int getStationFromRect(Rectangle rect) {
 		int x,y,slot=-2;
 		for (FTLRoom r : ship.rooms) {
 			if (r.getBounds().intersects(rect)) {
 				x = (rect.x - r.getBounds().x)/35;
 				y = (rect.y - r.getBounds().y)/35;
 				slot = r.getBounds().width/35 * y + x;
 			}
 		}
 		
 		return slot;
 	}
 	
 	public static Rectangle getStationDirected(FTLRoom r) {
 		final int STATION_SIZE = 15;
 		Rectangle rect = getRectFromStation(r);
 		r.dir = Main.ship.slotDirMap.get(r.getSystem());
 		
 		if (r.dir.equals(Slide.UP)) {
 			rect.height = STATION_SIZE;
 		} else if (r.dir.equals(Slide.RIGHT)) {
 			rect.x += 35 - STATION_SIZE;
 			rect.width = STATION_SIZE;
 		} else if (r.dir.equals(Slide.DOWN)) {
 			rect.y += 35 - STATION_SIZE;
 			rect.height = STATION_SIZE;
 		} else if (r.dir.equals(Slide.LEFT)) {
 			rect.width = STATION_SIZE;
 		}
 		
 		return rect;
 	}
 	
 	//=================
 	// === DOOR RELATED
 	
 	public static void removeUnalignedDoors() {
 		if (removeDoor && Main.ship != null) {
 			// can't iterate over ship.doors because it throws concurrentModification exception
 			// dump doors to an object array and iterate over it
 			Object[] array = ship.doors.toArray();
 			for (Object o : array) {
 				FTLDoor d = (FTLDoor) o;
 				if (!isDoorAtWall(d.getBounds())) {
 					d.dispose();
 					ship.doors.remove(d);
 					d = null;
 				}
 			}
 			array = null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param rect Rectangle which matches the parameters of a wall;
 	 * @return FTLDoor at the given rect, if there is one.
 	 */
 	public static FTLDoor wallToDoor(Rectangle rect) {
 		for (FTLDoor dr : ship.doors) {
 			if (rect != null && rect.intersects(dr.getBounds()) && rect.width == dr.getBounds().width) {
 				return dr;
 			}
 		}
 		return null;
 	}
 
 	public static boolean isDoorAtWall(Rectangle rect) {
 		for (FTLRoom r : ship.rooms) {
 			if (rect != null && r != null && rect.intersects(r.getBounds()) && !containsRect(r.getBounds(), rect))
 				return true;
 		}
 		return false;
 	}
 	
 	public static Rectangle getDoorAt(int x, int y) {
 		Rectangle dr = new Rectangle(0,0,0,0);
 		Point p = new Point(x, y);
 		for(int i=0; i<GRID_W; i++) {
 			for(int j=0; j<GRID_H; j++) {
 				// horizontal
 				dr.x = i*35+2; dr.y = j*35-3; dr.width = 31; dr.height = 6;
 				if (dr.contains(p))
 					return dr;
 				
 				dr.x = i*35-3; dr.y = j*35+2; dr.width = 6; dr.height = 31;
 				if (dr.contains(p))
 					return dr;
 			}
 		}
 		return null;
 	}
 	
 	public Rectangle getDoorFromMouse() {
 		Rectangle dr = new Rectangle(0,0,0,0);
 		for(int x=0; x<GRID_W; x++) {
 			for(int y=0; y<GRID_H; y++) {
 				// horizontal
 				dr.x = x*35+2; dr.y = y*35-3; dr.width = 31; dr.height = 6;
 				if (dr.contains(mousePos))
 					return dr;
 				
 				dr.x = x*35-3; dr.y = y*35+2; dr.width = 6; dr.height = 31;
 				if (dr.contains(mousePos))
 					return dr;
 			}
 		}
 		return null;
 	}
 	
 	//=================
 	// === MOUNT RELATED
 	
 	
 	public static int getMountIndex(FTLMount m) {
 		int i = -1;
 		for (FTLMount mt : ship.mounts) {
 			i++;
 			if (mt == m) break;
 		}
 		return i;
 	}
 	
 	public static FTLMount getMountFromPoint(int x, int y) {
 		for (FTLMount m : ship.mounts) {
 			if (m.getBounds().contains(x, y)) {
 				return m;
 			}
 		}
 		
 		return null;
 	}
 	
 	//=========================
 	// === AUXILIARY / LAZYNESS
 	
 	public void clearButtonImg() {
 		btnHull.setImage(null);
 		btnShields.setImage(null);
 		btnFloor.setImage(null);
 		btnCloak.setImage(null);
 		btnMiniship.setImage(null);
 	}
 	
 	public void updateButtonImg() {
 		btnHull.setImage((ShipIO.isNull(ship.imagePath) ? crossImage : tickImage));
 		btnShields.setImage((ShipIO.isNull(ship.shieldPath) ? crossImage : tickImage));
 		btnFloor.setImage((ShipIO.isNull(ship.floorPath) ? crossImage : tickImage));
 		btnCloak.setImage((ShipIO.isNull(ship.cloakPath) ? crossImage : tickImage));
 		btnMiniship.setImage((ShipIO.isNull(ship.miniPath) ? crossImage : tickImage));
 	}
 	
 	public static void updateSelectedPosText() {
 		boolean enable = selectedMount != null || selectedRoom != null || selectedGib != null || hullSelected || shieldSelected;
 		
 		txtX.setEnabled(enable);
 		btnXplus.setEnabled(enable);
 		btnXminus.setEnabled(enable);
 		txtY.setEnabled(enable);
 		btnYplus.setEnabled(enable);
 		btnYminus.setEnabled(enable);
 		
 		if (!enable) {
 			txtX.setText("");
 			txtY.setText("");
 		}
 		
 		if (selectedMount != null) {
 			txtX.setText(""+(selectedMount.getBounds().x+selectedMount.getBounds().width/2));
 			txtY.setText(""+(selectedMount.getBounds().y+selectedMount.getBounds().height/2));
 		} else if (selectedDoor != null) {
 			txtX.setText(""+(selectedDoor.getBounds().x/35+1));
 			txtY.setText(""+(selectedDoor.getBounds().y/35+1));
 		} else if (selectedRoom != null) {
 			txtX.setText(""+(selectedRoom.getBounds().x/35+1));
 			txtY.setText(""+(selectedRoom.getBounds().y/35+1));
 		} else if (selectedGib != null) {
 			txtX.setText(""+(selectedGib.getBounds().x));
 			txtY.setText(""+(selectedGib.getBounds().y));
 		} else if (hullSelected) {
 			txtX.setText(""+(ship.imageRect.x));
 			txtY.setText(""+(ship.imageRect.y));
 		} else if (shieldSelected) {
 			txtX.setText(""+(shieldEllipse.x));
 			txtY.setText(""+(shieldEllipse.y));
 		}
 	}
 	
 	public static void updateSelectedPosition() {
 		int x=0, y=0;
 		boolean doit = true;
 		try {
 			doit = !ShipIO.isNull(txtY.getText());
 			x = Integer.parseInt(txtX.getText());
 			y = Integer.parseInt(txtY.getText());
 		} catch (NumberFormatException e) {
 			doit = false;
 		}
 		
 		if (doit) {
 			if (selectedMount != null && (!selectedMount.isPinned() || arbitraryPosOverride)) {
 				if (x >= GRID_W*35) x = GRID_W*35-15;
 				if (y >= GRID_H*35) y = GRID_H*35-15;
 				if (x <= -selectedMount.getBounds().width) x = 15-selectedMount.getBounds().width;
 				if (y <= -selectedMount.getBounds().height) y = 15-selectedMount.getBounds().height;
 				
 				selectedMount.setLocation(x, y);
 			} else if (selectedDoor != null && (!selectedDoor.isPinned() || arbitraryPosOverride)) {
 				//selectedDoor.setLocation(x, y);
 			} else if (selectedRoom != null && (!selectedRoom.isPinned() || arbitraryPosOverride)) {
 				if (x > GRID_W-selectedRoom.getBounds().width/35) x = GRID_W - selectedRoom.getBounds().width/35 + 1;
 				if (y > GRID_H-selectedRoom.getBounds().height/35) y = GRID_H - selectedRoom.getBounds().height/35 + 1;
 				if (x <= ship.anchor.x/35) x = ship.anchor.x/35 + 1;
 				if (y <= ship.anchor.y/35) y = ship.anchor.y/35 + 1;
 				
 				Rectangle collisionCheck = new Rectangle((x-1)*35, (y-1)*35, selectedRoom.getBounds().width, selectedRoom.getBounds().height);
 				if (!doesRectOverlap(collisionCheck, selectedRoom.getBounds())) {
 					x = (x-1) * 35;
 					y = (y-1) * 35;
 					selectedRoom.setLocation(x, y);
 					//updateCorners(selectedRoom);
 				}
 			} else if (selectedGib != null && (!selectedGib.isPinned() || arbitraryPosOverride)) {
 				if (x >= GRID_W*35) x = GRID_W*35-15;
 				if (y >= GRID_H*35) y = GRID_H*35-15;
 				if (x <= -selectedGib.getBounds().width) x = 15-selectedGib.getBounds().width;
 				if (y <= -selectedGib.getBounds().height) y = 15-selectedGib.getBounds().height;
 				
 				selectedGib.setLocationAbsolute(x, y);
 			} else if (hullSelected && (!ship.hullPinned || arbitraryPosOverride)) {
 				if (x >= GRID_W*35) x = GRID_W*35-15;
 				if (y >= GRID_H*35) y = GRID_H*35-15;
 				if (x <= -ship.imageRect.width) x = 15-ship.imageRect.width;
 				if (y <= -ship.imageRect.height) y = 15-ship.imageRect.height;
 
 				hullBox.setLocation(x, y);
 			} else if (shieldSelected && (!ship.shieldPinned || arbitraryPosOverride)) {
 				if (x >= GRID_W*35) x = GRID_W*35-15;
 				if (y >= GRID_H*35) y = GRID_H*35-15;
 				if (x <= -shieldEllipse.width) x = 15-shieldEllipse.width;
 				if (y <= -shieldEllipse.height) y = 15-shieldEllipse.height;
 
 				shieldBox.setLocation(x, y);
 			}
 			
 			canvas.redraw();
 		}
 	}
 	
 	public void drawToolIcon(PaintEvent e, String name) {
 		e.gc.setAlpha(255);
 		e.gc.drawImage(toolsMap.get(name),0, 0, 24, 24, mousePos.x+10, mousePos.y+10, 19, 20);
 	}
 	
 	/**
 	 * Prints given message to the box in top right corner of the app.
 	 */
 	public static void print(String msg) {
 		if (!msg.equals("")) {
 			lastMsg = msg;
 		}
 		text.setText(lastMsg);
 	}
 	
 	public static void debug(String msg, boolean ln) {
 		if (debug) {
 			if (ln) {
 				System.out.println(msg);
 			} else {
 				System.out.print(msg);
 			}
 		}
 	}
 	
 	public static void updatePainter() {
 		anchor.setLocation(ship.anchor.x, ship.anchor.y,true);
 		for (FTLRoom rm : ship.rooms) {
 			rm.assignSystem(rm.getSystem());
 			if (!rm.getSystem().equals(Systems.EMPTY)) {
 				rm.getSysBox().setVisible(true);
 			}
 		}
 		
 		hullBox.setHullImage(ship.imagePath);
 		hullBox.setCloakImage(ship.cloakPath);
 		if (ship.isPlayer) {
 			hullBox.setFloorImage(ship.floorPath);
 		} else {
 			hullBox.setFloorImage(null);
 		}
 		hullBox.setLocation(ship.imageRect.x, ship.imageRect.y);
 
 		shieldBox.setImage(ship.shieldPath, true);
 		
 		for (FTLMount m : ship.mounts)
 			m.setLocation(ship.imageRect.x + m.pos.x, ship.imageRect.y + m.pos.y);
 		
 		for (FTLGib g : ship.gibs)
 			g.setLocationRelative(g.position.x, g.position.y);
 		
 		showRooms();
 		showMounts();
 		
 		Point p = ship.findLowBounds();
 		Point pt = ship.findHighBounds();
 		p.x = p.x + (pt.x - p.x)/2;
 		p.y = p.y + (pt.y - p.y)/2;
 		pt = shieldBox.getLocation();
 		pt.x += shieldBox.getBounds().width/2; pt.y += shieldBox.getBounds().height/2;
 		p.x = p.x - pt.x;
 		p.y = p.y - pt.y;
 		shieldBox.setLocation(shieldBox.getLocation().x + p.x + ship.ellipse.x, shieldBox.getLocation().y + p.y + ship.ellipse.y + ((!ship.isPlayer) ? 110 : 0));
 		
 		shieldBox.setVisible(showShield);
 		hullBox.setVisible(showHull || showFloor);
 	}
 	
 	public static void registerItemsForPainter() {
 		if (ship != null) {
 			for (FTLRoom r : ship.rooms) {
 				layeredPainter.add(r, LayeredPainter.ROOM);
 				idList.add(r.id);
 			}
 			
 			if (ship.rooms.size() > 0) {
 				Main.btnShields.setEnabled(ship.isPlayer);
 				Main.btnShields.setToolTipText(null);
 			}
 			
 			for (FTLDoor d : ship.doors) {
 				layeredPainter.add(d, LayeredPainter.DOOR);
 			}
 			for (FTLMount m : ship.mounts) {
 				layeredPainter.add(m, LayeredPainter.MOUNT);
 			}
 			for (FTLGib g : ship.gibs) {
 				layeredPainter.addAsFirst(g, LayeredPainter.GIB);
 				gibDialog.letters.add(g.ID);
 			}
 			gibDialog.refreshList();
 		}
 	}
 	
 	public static void stripUnserializable() {
 		if (ship != null) {
 			for (FTLRoom r : ship.rooms)
 				r.stripUnserializable();
 			for (FTLDoor d : ship.doors)
 				d.stripUnserializable();
 			for (FTLMount m : ship.mounts)
 				m.stripUnserializable();
 			for (FTLGib g : ship.gibs)
 				g.stripUnserializable();
 		}
 	}
 	
 	public static void loadUnserializable() {
 		if (ship != null) {
 			for (FTLRoom r : ship.rooms)
 				r.loadUnserializable();
 			for (FTLDoor d : ship.doors)
 				d.loadUnserializable();
 			for (FTLMount m : ship.mounts)
 				m.loadUnserializable();
 			for (FTLGib g : ship.gibs)
 				g.loadUnserializable();
 		}
 	}
 	
 	public static void showRooms() {
 		if (ship != null) {
 			for (FTLRoom r : ship.rooms) {
 				r.setVisible(showRooms);
 			}
 			for (FTLDoor d : ship.doors) {
 				d.setVisible(showRooms);
 			}
 			for (Systems sys : systemsMap.keySet()) {
 				SystemBox sysbox = systemsMap.get(sys); 
 				sysbox.setVisible(showRooms && sysbox.getRoom() != null);
 			}
 		}
 	}
 	
 	public static void showMounts() {
 		if (ship != null) {
 			for (FTLMount m : ship.mounts) {
 				m.setVisible(showMounts);
 			}
 		}
 	}
 	
 	public static void deselectAll() {
 		if (selectedDoor != null) selectedDoor.deselect();
 		if (selectedRoom != null) selectedRoom.deselect();
 		if (selectedMount != null) selectedMount.deselect();
 		if (selectedGib != null) selectedGib.deselect();
 		if (hullSelected) hullBox.deselect();
 		if (shieldSelected) shieldBox.deselect();
 	}
 	
 	private static void addListenerToGibItem(MenuItem mntm, FTLGib g) {
 		final int number = (g==null) ? 0 : g.number;
 		SelectionAdapter adapter = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (Main.selectedMount != null) {
 					Main.selectedMount.gib = number;
 				}
 			}
 		};
 		
 		mntm.addSelectionListener(adapter);
 	}
 	
 	public static String getFilename(String path) {
 		return path.substring(path.lastIndexOf(ShipIO.pathDelimiter)+1) + ShipIO.pathDelimiter;
 	}
 	
 	public static String getParent(String path) {
 		return path.substring(0, path.lastIndexOf(ShipIO.pathDelimiter)) + ShipIO.pathDelimiter;
 	}
 }
 
 

 package com.kartoflane.superluminal.core;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.zip.ZipFile;
 
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.UndoManager;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Transform;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import com.kartoflane.superluminal.elements.Anchor;
 import com.kartoflane.superluminal.elements.AxisFlag;
 import com.kartoflane.superluminal.elements.CursorBox;
 import com.kartoflane.superluminal.elements.FTLDoor;
 import com.kartoflane.superluminal.elements.FTLGib;
 import com.kartoflane.superluminal.elements.FTLItem;
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
 import com.kartoflane.superluminal.painter.PaintBox;
 import com.kartoflane.superluminal.ui.AboutWindow;
 import com.kartoflane.superluminal.ui.DirectoriesWindow;
 import com.kartoflane.superluminal.ui.ErrorDialog;
 import com.kartoflane.superluminal.ui.ExportDialog;
 import com.kartoflane.superluminal.ui.GibDialog;
 import com.kartoflane.superluminal.ui.GibPropertiesWindow;
 import com.kartoflane.superluminal.ui.GlowWindow;
 import com.kartoflane.superluminal.ui.NewShipDialog;
 import com.kartoflane.superluminal.ui.PropertiesWindow;
 import com.kartoflane.superluminal.ui.RenameGibDialog;
 import com.kartoflane.superluminal.ui.ShipBrowser;
 import com.kartoflane.superluminal.ui.ShipChoiceDialog;
 import com.kartoflane.superluminal.ui.ShipPropertiesWindow;
 import com.kartoflane.superluminal.ui.TipWindow;
 import com.kartoflane.superluminal.undo.UEListener;
 import com.kartoflane.superluminal.undo.Undoable;
 import com.kartoflane.superluminal.undo.UndoableDeleteEdit;
 
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
 	public final static int MAX_MOUNTS = 8;
 	public static final int MAX_DESCRIPTION_LENGTH = 200;
 
 	public final static String APPNAME = "Superluminal";
 	public final static String VERSION = "13-07-08";
 
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
 	public static ShipChoiceDialog choiceDialog;
 	public static GlowWindow glowWindow;
 	public static TipWindow tipWindow;
 
 	// === Preferences
 	public static boolean forbidBossLoading = true;
 	// ship explorer
 	public static String dataPath = "";
 	public static String resPath = "";
 	public static String installPath = "";
 	// edit menu
 	public static boolean removeDoor = true;
 	public static boolean snapMounts = true;
 	public static boolean snapMountsToHull = true;
 	public static boolean arbitraryPosOverride = true;
 	// view menu
 	public static boolean showGrid = true;
 	public static boolean showAnchor = true;
 	public static boolean showMounts = true;
 	public static boolean showRooms = true;
 	public static boolean showHull = true;
 	public static boolean showFloor = true;
 	public static boolean showShield = true;
 	public static boolean showStations = true;
 	public static boolean loadFloor = true;
 	public static boolean loadShield = true;
 	public static boolean loadSystem = true;
 	// export dialog
 	public static String exportPath = "";
 	// other
 	public static String projectPath = "";
 	public static boolean shownIncludeWarning = false;
 	public static boolean showTips = true;
 
 	// === Mouse related
 	public static Point mousePos = new Point(0, 0);
 	public static Point mousePosLastClick = new Point(0, 0);
 	public static Point dragRoomAnchor = new Point(0, 0);
 	public static boolean leftMouseDown = false;
 	public static boolean rightMouseDown = false;
 
 	// === Generic booleans
 	/** Use Main.hullBox.isSelected() instead */
 	@Deprecated
 	public static boolean hullSelected = false;
 	/** Use Main.shieldBox.isSelected() instead */
 	@Deprecated
 	public static boolean shieldSelected = false;
 	public static boolean modShift = false;
 	public static boolean modAlt = false;
 	public static boolean modCtrl = false;
 	public static boolean arrowDown = false;
 
 	// === Internal
 	public static boolean debug = true;
 	public static boolean log = true;
 
 	// === Variables used to store a specific element out of a set (currently selected room, etc)
 	public static FTLRoom selectedRoom = null;
 	public static FTLDoor selectedDoor = null;
 	public static FTLMount selectedMount = null;
 	public static FTLGib selectedGib = null;
 
 	public static Rectangle shieldEllipse = new Rectangle(0, 0, 0, 0);
 
 	// === Flags for Weapon Mounting tool
 	public static Slide mountToolSlide = Slide.UP;
 	public static boolean mountToolMirror = false;
 	public static boolean mountToolHorizontal = true;
 
 	// === Image holders
 	public static Image hullImage = null;
 	public static Image floorImage = null;
 	public static Image shieldImage = null;
 	public static Image cloakImage = null;
 	public static Image tempImage = null;
 	@Deprecated
 	public static Image pinImage = null;
 	public static Image tickImage = null;
 	public static Image crossImage = null;
 	public static Map<String, Integer> weaponFrameWidthMap = new HashMap<String, Integer>();
 
 	// === Miscellaneous
 	public static Rectangle[] corners = new Rectangle[4];
 	private static String lastMsg = "";
 	public static String interiorPath = "";
 	private static String ftlLoadPath = "";
 	private static String importPath = "";
 	private static String includePath = "";
 	public static File temporaryFiles = null;
 	public static boolean temporaryFilesInUse = false;
 	/** Path of current project file, for quick saving via Ctrl+S */
 	public static String currentPath = null;
 	/** Contains room IDs currently in use. */
 	public static HashSet<Integer> idList = new HashSet<Integer>();
 	/** Images (tools and systems) are loaded once and then references are held in this map for easy access. */
 	public static HashMap<String, Image> toolsMap = new HashMap<String, Image>();
 
 	// === GUI elements' variables, for use in listeners and functions that reference them
 	public static Menu menuSystem;
 	public static Menu menuGib;
 	public static ArrayList<MenuItem> menuGibItems = new ArrayList<MenuItem>();
 	private static Label text;
 	private static Label mGridPosText;
 	private static Label shipInfoText;
 	public static MenuItem mntmClose;
 	private static Text txtX;
 	private static Text txtY;
 	private Label mPosText;
 	private static Button btnHull;
 	public static Button btnShields;
 	private static Button btnFloor;
 	private static Button btnCloak;
 	private static Button btnMiniship;
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
 	private MenuItem mntmShowStations;
 	public static Button btnCloaked;
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
 	private static Spinner stepSpinner;
 
 	/** List containing all tips displayed when the editor is launched. */
 	public static ArrayList<String> tipsList;
 
 	/**
 	 * The amount that gets added to X/Y coordinates of selected item, whenever the +/- values are pressed. Modified by the stepSpinner.
 	 */
 	public static int arbitraryStep = 1;
 
 	/**
 	 * When set to true, the program uses Properties java class to save config. When set to false, saves config by directly writing values into a file.
 	 */
 	public static final boolean propertiesSwitch = false;
 
 	/** Direction of shift-move dragging. */
 	public static AxisFlag dragDir = null;
 
 	public static boolean animateGibs = false;
 	public static long timeElapsed = 0;
 
 	private static MenuItem mntmView;
 	private static MenuItem mntmEdit;
 	private static MenuItem mntmNewShip;
 	private static MenuItem mntmLoadShip;
 	private static MenuItem mntmLoadShipFTL;
 	private static MenuItem mntmImport;
 	private static MenuItem mntmLoadShipProject;
 	private static MenuItem mntmInclude;
 	private static MenuItem mntmArchives;
 	private static MenuItem mntmSaveShip;
 	private static MenuItem mntmSaveShipAs;
 	private static MenuItem mntmExport;
 
 	// Undo Manager
 	public static MenuItem mntmUndo;
 	public static MenuItem mntmRedo;
 	public static UndoableEditListener ueListener = new UEListener();
 	public static boolean savedSinceAction = true;
 	/** Values: SWT.YES, SWT.NO, SWT.CANCEL */
 	public static int askSaveChoice = 0;
 	public static boolean askedChoice = false;
 
 	public static UndoManager undoManager = new UndoManager();
 
 	// =================================================================================================== //
 
 	/*
 	 * ===== REMINDER: INCREMENT SHIP'S VERSION ON MAJOR RELEASES! AND UPDATE VERSION STRING!
 	 * === TODO
 	 * == IMMEDIATE PRIO: (bug fixes)
 	 * 
 	 * == MEDIUM PRIO: (new features)
 	 * - multiple systems for the same room for enemy ships
 	 * - fix (ie. implement...) weapon mount animation?
 	 * 
 	 * == LOW PRIO: (optional tweaks)
 	 * - port to Apache zip
 	 * 
 	 * =========================================================================
 	 * CHANGELOG:
 	 * 	- rearranged UI - the editor can now be resized down to about 650x350 px
 	 * 	- fixed a bug with undo offset operation (was moving the anchor, but didn't actually update the offset values)
 	 * 	- manually-linked doors are now correctly loaded when loading a ship from ftl archive.
 	 * 	- fixed a crash that would occur if you tried to nudge an object after clicking a button (for example cloak)
 	 * 	- fixed a crash when closing room properties window when no room was selected
 	 *  - weapon mounts will no longer get snapped to weird positions should they end up outside of visible area
 	 *  - the drop-down lists of augments, weapons and drones are now sorted alphabetically, and there's some spacing between the name and the blueprint name of the item to make it easier to read
 	 *  - corrected a dumb design oversight that prevented the editor from being able to use fractional values for gib rotation. Gib animation should now be much more smooth.
 	 *  - weapon mount tool now displays the image of the dummy mount instead of an ambiguous green box, controls are the same
 	 *  - the ship list in Ship Browser can now be sorted either by blueprint names (default), or by class names of ships
	 *  - the Ship Browser can now be resized
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
 			log = !argsList.contains("-nolog");
 		}
 
 		if (log)
 			try {
 				System.setOut(new PrintStream(new FileOutputStream("debug.log")));
 				System.setErr(new PrintStream(new FileOutputStream("debug.log")));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 
 		try {
 			Main window = new Main();
 			window.open();
 		} catch (Exception e) {
 			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 			box.setMessage("Superluminal has encountered an unexpected error and must close." + ShipIO.lineDelimiter + "Your active project will be saved to Superluminal's directory if possible."
 					+ ShipIO.lineDelimiter + ShipIO.lineDelimiter + "Please find crash.log file in Superluminal's directory and post its contents in the thread at FTL forums (link in About window).");
 			box.open();
 
 			if (Main.ship != null)
 				ShipIO.saveShipProject("crash_save.shp");
 
 			try {
 				if (log) {
 					System.setOut(new PrintStream(new FileOutputStream("crash.log")));
 					System.setErr(new PrintStream(new FileOutputStream("crash.log")));
 				}
 			} catch (FileNotFoundException ex) {
 			}
 
 			e.printStackTrace();
 		}
 	}
 
 	public void open() throws Exception {
 		final Display display = Display.getDefault();
 
 		shell = new Shell(SWT.SHELL_TRIM | SWT.BORDER);
 		shell.setLayout(new GridLayout(2, false));
 		shell.setText(APPNAME + " - Ship Editor");
 		shell.setLocation(100, 50);
 
 		Image smallIcon = Cache.checkOutImage(shell, "/img/Superluminal-2_16.png");
 		Image mediumIcon = Cache.checkOutImage(shell, "/img/Superluminal-2_32.png");
 		Image largeIcon = Cache.checkOutImage(shell, "/img/Superluminal-2_64.png");
 		Image hugeIcon = Cache.checkOutImage(shell, "/img/Superluminal-2_128.png");
 		Image[] images = new Image[] { smallIcon, mediumIcon, largeIcon, hugeIcon };
 		shell.setImages(images);
 
 		// resize the window as to not exceed screen dimensions, with maximum size being defined by GRID_W_MAX and GRID_H_MAX
 		GRID_W = ((int) ((0.7f * display.getBounds().width - shell.getLocation().x)) / 35);
 		GRID_H = ((int) ((0.6f * display.getBounds().height - shell.getLocation().y)) / 35);
 		GRID_W = (GRID_W > GRID_W_MAX) ? GRID_W_MAX : (GRID_W < 18) ? 18 : GRID_W;
 		GRID_H = (GRID_H > GRID_H_MAX) ? GRID_H_MAX : (GRID_H < 7) ? 7 : GRID_H;
 
 		if (!ConfigIO.configExists())
 			ConfigIO.saveConfig();
 
 		if (propertiesSwitch) {
 			// open properties
 			Properties properties = new Properties();
 			FileReader fr = null;
 			try {
 				fr = new FileReader("superluminal.ini");
 				properties.load(fr);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (fr != null)
 						fr.close();
 				} catch (IOException e1) {
 				}
 			}
 
 			// load values from config
 			exportPath = properties.getProperty("exportPath");
 			projectPath = properties.getProperty("projectPath");
 			installPath = properties.getProperty("installPath");
 			dataPath = properties.getProperty("dataPath");
 			resPath = properties.getProperty("resPath");
 			removeDoor = Boolean.valueOf(properties.getProperty("removeDoor"));
 			arbitraryPosOverride = Boolean.valueOf(properties.getProperty("arbitraryPosOverride"));
 			forbidBossLoading = Boolean.valueOf(properties.getProperty("forbidBossLoading"));
 			showTips = Boolean.valueOf(properties.getProperty("showTips"));
 		} else {
 			// create config file if it doesn't exist already
 			if (!ConfigIO.configExists())
 				ConfigIO.saveConfig();
 
 			// load values from config
 			exportPath = ConfigIO.scourFor("exportPath");
 			projectPath = ConfigIO.scourFor("projectPath");
 			installPath = ConfigIO.scourFor("installPath");
 			// files
 			dataPath = ConfigIO.scourFor("dataPath");
 			resPath = ConfigIO.scourFor("resPath");
 			// edit
 			removeDoor = ConfigIO.getBoolean("removeDoor");
 			arbitraryPosOverride = ConfigIO.getBoolean("arbitraryPosOverride");
 			// view
 			forbidBossLoading = ConfigIO.getBoolean("forbidBossLoading");
 			shownIncludeWarning = ConfigIO.getBoolean("shownIncludeWarning");
 			showTips = ConfigIO.getBoolean("showTips");
 		}
 
 		appFont = new Font(Display.getCurrent(), "Monospaced", 9, SWT.NORMAL);
 		if (appFont == null) {
 			debug("Monospaced font not found, loading Serif.");
 			appFont = new Font(shell.getDisplay(), "Serif", 9, SWT.NORMAL);
 		}
 		if (appFont == null) {
 			debug("Serif font not found, loading Courier.");
 			appFont = new Font(shell.getDisplay(), "Courier", 9, SWT.NORMAL);
 		}
 		if (appFont == null)
 			debug("No suitable font was found. We're doomed!");
 
 		// used as a default, "null" transformation to fall back to in order to do regular drawing.
 		currentTransform = new Transform(shell.getDisplay());
 
 		sysDialog = new PropertiesWindow(shell);
 		shipDialog = new ShipPropertiesWindow(shell);
 		erDialog = new ErrorDialog(shell);
 		gibDialog = new GibDialog(shell);
 		gibWindow = new GibPropertiesWindow(shell);
 		dirWindow = new DirectoriesWindow(shell);
 		browser = new ShipBrowser(shell);
 		choiceDialog = new ShipChoiceDialog(shell);
 		glowWindow = new GlowWindow(shell);
 		GibDialog.gibRename = new RenameGibDialog(gibDialog.getShell());
 
 		tipsList = new ArrayList<String>();
 		tipsList.add("You can quickly switch between tools using Q, W, E, R, T and G keys.");
 		tipsList.add("Each tool has a tooltip detailing its functions - hover over an icon to show the tooltip.");
 		tipsList.add("Selection Tool (Q) also allows you to modify (rotate, change direction, etc) already placed weapon mounts.");
 		tipsList.add("You can use arrow keys to nudge any selected object (except for doors).");
 		tipsList.add("You can individually hide various elements of the ship (like hull, shield, rooms) using options in the View menu");
 		tipsList.add("You can pin down objects by pressing ~ (tilde) or Spacebar, preventing them from being accidentally moved.");
 		tipsList.add("You can split big rooms into smaller ones, by holding down Shift and clicking with the Room Creation tool.");
 		tipsList.add("You can use Edit > Calculate Optimal Offset to make the ship centered in-game. You should recalculate offset after making changes to room layout.");
 		tipsList.add("Left-clicking on the anchor box allows you to freely move the ship around the editor area - it doesn't change the ship's position in-game.");
 		tipsList.add("Shift-left-clicking on anchor box allows you to manually set the ship's offset values - it DOES change the ship's position in-game.");
 		tipsList.add("If you leave all crew spinners at 0, the game will consider your ship as an automated ship, with all systems appearing manned by inexperienced crew (they will slowly repair by themselves, too).");
 		tipsList.add("All movable objects can be moved in a single direction by holding down Shift.");
 		tipsList.add("You can hold down Ctrl while moving an object if you want to place it more precisely.");
 		tipsList.add("You can change the nudge value of -/+ buttons by changing the number in the leftmost box beneath the toolbar.");
 		tipsList.add("You can right-click on the image buttons (hull, shield, etc) to bring up a small popup menu with options.");
 		tipsList.add("You can right-click on hull graphic to select and move the shield graphic - useful when the shield is completely obscured by the hull.");
 		tipsList.add("You can make weapons float with gibs when the ship is destroyed - in Gib Editor, right click on a mount to assign it to a gib.");
 		tipsList.add("Holding down Shift while pressing an arrow key will cause the selected object to be moved by 35 pixels.");
 		tipsList.add("You can load a ship directly from a .ftl mod file, by using File > Load Ship From .ftl...");
 		tipsList.add("You can import room layout from another ship's .txt file by using File > Import Room Layout...");
 		tipsList.add("You can convert an enemy ship to player ship (and vice versa) by using Edit > Convert options.");
 		tipsList.add("You can use File > Change Archives... options to change the .dat archives used by the editor (or to unpack them again).");
 		tipsList.add("You can copy contents of a .ftl mod into Superluminal's files by using File > Include Mod... option, allowing you to use assets from that mod."
 				+ "\n\nNote that using assets from another mod will cause your ship to have a dependency on that mod");
 		tipsList.add("You can right-click on the anchor box to set a precise vertical offset of your ship. Shift-right-clicking sets horizontal offset. Double-right-clicking on the box resets the respective offset to 0.");
 		tipsList.add("On player ships, systems that don't have a station placed will use default station positions, which may cause them to be unreachable in small enough rooms."
 				+ "\nEnemy ships' stations are random by default, so you don't have to place them in order for your ship to work.");
 
 		tipWindow = new TipWindow(shell);
 		tipWindow.updateButtons();
 		tipWindow.setLocation(shell.getLocation().x + 300, shell.getLocation().y + 200);
 
 		createContents();
 
 		// undoManager.setLimit(200);
 
 		shell.setFont(appFont);
 
 		shell.setEnabled(true);
 
 		if (ShipIO.isNull(dataPath) || ShipIO.isNull(resPath) || !(new File("archives")).exists()) {
 			dataPath = null;
 			resPath = null;
 			shell.setEnabled(false);
 			enableMenus(false);
 			dirWindow.label.setText("Please, browse to your FTL installation directory and select data.dat and resource.dat archives located in /resources/ folder.");
 
 			File installFile = dirWindow.findInstallation();
 			if (installFile != null)
 				Main.installPath = installFile.getAbsolutePath();
 
 			dirWindow.open();
 		}
 
 		if (!ShipIO.isNull(dataPath) && !ShipIO.isNull(resPath))
 			ShipIO.fetchShipNames();
 		if (ShipIO.errors.size() > 0) {
 			erDialog.printErrors(ShipIO.errors);
 			erDialog.open();
 		}
 
 		shell.pack();
 		shell.setMinimumSize(19 * 35, 12 * 35);
 		shell.open();
 
 		if (showTips && !dirWindow.shell.isVisible()) {
 			tipWindow.open();
 		}
 
 		shell.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlMoved(ControlEvent e) {
 				Point p = shell.getLocation();
 				gibDialog.autoReposition = true;
 				gibDialog.setLocation(p.x + gibDialog.relativePosition.x, p.y + gibDialog.relativePosition.y);
 				gibDialog.autoReposition = false;
 			}
 
 			@Override
 			public void controlResized(ControlEvent e) {
 				GRID_W = ((int) ((canvasBg.getBounds().width)) / 35);
 				GRID_H = ((int) ((canvasBg.getBounds().height)) / 35);
 				fd_canvas.right.offset = GRID_W * 35 + 5;
 				fd_canvas.bottom.offset = GRID_H * 35 + 5;
 				canvas.setSize(GRID_W * 35, GRID_H * 35);
 
 				if (grid != null)
 					grid.setSize(GRID_W * 35, GRID_H * 35);
 			}
 		});
 
 		shell.addListener(SWT.Close, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if (!askedChoice) {
 					askSaveChoice();
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							event.doit = false;
 							askedChoice = false;
 							return;
 					}
 				}
 				mntmClose.notifyListeners(SWT.Selection, null);
 				askedChoice = false;
 			}
 		});
 
 		shellStateChange = shell.getMaximized();
 
 		display.timerExec(INTERVAL, new Runnable() {
 			public void run() {
 				// used to resize grid when the editor window is maximized
 				if (canvas.isDisposed())
 					return;
 				if (shellStateChange != shell.getMaximized()) {
 					shellStateChange = shell.getMaximized();
 					GRID_W = ((int) ((canvasBg.getBounds().width)) / 35);
 					GRID_H = ((int) ((canvasBg.getBounds().height)) / 35);
 					fd_canvas.right.offset = GRID_W * 35;
 					fd_canvas.bottom.offset = GRID_H * 35;
 					canvas.setSize(GRID_W * 35, GRID_H * 35);
 
 					if (grid != null)
 						grid.setSize(GRID_W * 35, GRID_H * 35);
 				}
 
 				// === update info text fields; mousePos and rudimentary ship info
 				mGridPosText.setText("(" + (int) (1 + Math.floor(mousePos.x / 35)) + ", " + (int) (1 + Math.floor(mousePos.y / 35)) + ")");
 				mPosText.setText("(" + mousePos.x + ", " + mousePos.y + ")");
 				if (ship != null)
 					shipInfoText.setText("rooms: " + ship.rooms.size() + ",  doors: " + ship.doors.size());
 
 				// === animate gibs
 				if (tltmGib.getSelection() && animateGibs) {
 					timeElapsed += INTERVAL;
 					animateGibs = timeElapsed < 4300; // no idea why it's not 6000ms, but with this value the animation lasts ~6 seconds
 					for (FTLGib g : Main.ship.gibs) {
 						g.animX += g.animVel * Math.cos((g.animDir - 90) * Math.PI / 180);
 						g.animY += g.animVel * Math.sin((g.animDir - 90) * Math.PI / 180);
 						g.setLocation((int) Math.round(g.animX), (int) Math.round(g.animY));
 						g.setRotation(g.animRotation + (float) g.animAng);
 						/*
 						 * for (FTLMount m : Main.ship.mounts) {
 						 * if (m.gib==g.number) {
 						 * //m.animX = g.animX + Math.cos((g.animRotation-90)*Math.PI/180)*(m.animX-g.animX) - Math.sin((g.animRotation-90)*Math.PI/180)*(m.animY-g.animY);
 						 * //m.animY = g.animY + Math.sin((g.animRotation-90)*Math.PI/180)*(m.animX-g.animX) + Math.cos((g.animRotation-90)*Math.PI/180)*(m.animY-g.animY);
 						 * 
 						 * //m.animX = g.animX + Math.cos((g.animRotation-90)*Math.PI/180)*Math.abs(m.animPos.x-(g.position.x))
 						 * // - Math.sin((g.animRotation-90)*Math.PI/180)*Math.abs(m.animPos.y-(g.position.y));
 						 * //m.animY = g.animY + Math.sin((g.animRotation-90)*Math.PI/180)*Math.abs(m.animPos.x-(g.position.x))
 						 * // + Math.cos((g.animRotation-90)*Math.PI/180)*Math.abs(m.animPos.y-(g.position.y));
 						 * 
 						 * m.animX = g.animX + m.animRel.x * Math.cos((g.animRotation-90)*Math.PI/180) - m.animRel.y * Math.sin((g.animRotation-90)*Math.PI/180);
 						 * m.animY = g.animY + m.animRel.y * Math.sin((g.animRotation-90)*Math.PI/180) + m.animRel.y * Math.cos((g.animRotation-90)*Math.PI/180);
 						 * 
 						 * m.setLocationAbsolute((int) Math.round(m.animX), (int) Math.round(m.animY));
 						 * m.setRotation((int) Math.round(g.animRotation + (m.isRotated() ? 90 : 0)));
 						 * }
 						 * }
 						 */
 					}
 					if (!animateGibs) {
 						timeElapsed = 0;
 						shell.setEnabled(true);
 						gibDialog.enableButtons(true);
 						for (FTLGib g : Main.ship.gibs) {
 							g.setLocationRelative(g.position.x, g.position.y);
 							g.setRotation(0);
 						}
 						for (FTLMount m : Main.ship.mounts) {
 							// m.setLocation(m.animPos.x, m.animPos.y);
 							// m.setRotation((int) Math.round(m.isRotated() ? 90 : 0));
 							// if (m.gib==0) m.setVisible(true);
 							m.setVisible(true);
 						}
 					}
 					canvas.redraw();
 				}
 
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
 		// Load images to a map for easy access
 		tempImage = Cache.checkOutImage(shell, "/img/room.png");
 		toolsMap.put("room", tempImage);
 		tempImage = Cache.checkOutImage(shell, "/img/door.png");
 		toolsMap.put("door", tempImage);
 		tempImage = Cache.checkOutImage(shell, "/img/pointer.png");
 		toolsMap.put("pointer", tempImage);
 		tempImage = Cache.checkOutImage(shell, "/img/mount.png");
 		toolsMap.put("mount", tempImage);
 		tempImage = Cache.checkOutImage(shell, "/img/system.png");
 		toolsMap.put("system", tempImage);
 		tempImage = Cache.checkOutImage(shell, "/img/gib.png");
 		toolsMap.put("gib", tempImage);
 
 		// pinImage = Cache.checkOutImage(shell, "/img/pin.png");
 		tickImage = Cache.checkOutImage(shell, "/img/check.png");
 		crossImage = Cache.checkOutImage(shell, "/img/cross.png");
 
 		// === Menu bar
 
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 
 		// === File menu
 
 		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
 		mntmFile.setText("File");
 		Menu menu_file = new Menu(mntmFile);
 		mntmFile.setMenu(menu_file);
 
 		// === File -> New ship
 		mntmNewShip = new MenuItem(menu_file, SWT.NONE);
 		mntmNewShip.setText("New Ship \tCtrl + N");
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 		// === File -> Load ship
 		mntmLoadShip = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShip.setText("Load Ship...\tCtrl + L");
 
 		// === File -> Load ship from ftl
 		mntmLoadShipFTL = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShipFTL.setText("Load Ship From .ftl...");
 
 		// === File -> Open project
 		mntmLoadShipProject = new MenuItem(menu_file, SWT.NONE);
 		mntmLoadShipProject.setText("Open Project...\tCtrl + O");
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 
 		// === File -> Change archives
 		mntmArchives = new MenuItem(menu_file, SWT.NONE);
 		mntmArchives.setText("Change Archives...");
 
 		// === File -> Include mod
 		mntmInclude = new MenuItem(menu_file, SWT.NONE);
 		mntmInclude.setText("Include Mod...");
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 
 		// === File -> Save project
 		mntmSaveShip = new MenuItem(menu_file, SWT.NONE);
 		mntmSaveShip.setText("Save Project \tCtrl + S");
 		mntmSaveShip.setEnabled(false);
 
 		// === File -> Save project as
 		mntmSaveShipAs = new MenuItem(menu_file, SWT.NONE);
 		mntmSaveShipAs.setText("Save Project As...");
 		mntmSaveShipAs.setEnabled(false);
 
 		// === File -> Export ship
 		mntmExport = new MenuItem(menu_file, SWT.NONE);
 		mntmExport.setText("Export Ship... \tCtrl + E");
 		mntmExport.setEnabled(false);
 
 		new MenuItem(menu_file, SWT.SEPARATOR);
 
 		// === File -> Close project
 		mntmClose = new MenuItem(menu_file, SWT.NONE);
 		mntmClose.setText("Close Project");
 		mntmClose.setEnabled(false);
 
 		// === Edit menu
 
 		mntmEdit = new MenuItem(menu, SWT.CASCADE);
 		mntmEdit.setText("Edit");
 		Menu menu_edit = new Menu(mntmEdit);
 		mntmEdit.setMenu(menu_edit);
 
 		// === Edit -> Undo
 		mntmUndo = new MenuItem(menu_edit, SWT.NONE);
 		mntmUndo.setText("Undo \tCtrl+Z");
 		mntmUndo.setEnabled(false);
 
 		// === Edit -> Redo
 		mntmRedo = new MenuItem(menu_edit, SWT.NONE);
 		mntmRedo.setText("Redo \tCtrl+Y");
 		mntmRedo.setEnabled(false);
 
 		new MenuItem(menu_edit, SWT.SEPARATOR);
 
 		// === Edit -> Automatic door clean
 		MenuItem mntmRemoveDoors = new MenuItem(menu_edit, SWT.CHECK);
 		mntmRemoveDoors.setSelection(true);
 		mntmRemoveDoors.setText("Automatic Door Cleanup");
 		mntmRemoveDoors.setSelection(removeDoor);
 
 		// === Edit -> Arbitrary position override
 		MenuItem mntmArbitraryPositionOverride = new MenuItem(menu_edit, SWT.CHECK);
 		mntmArbitraryPositionOverride.setText("Arbitrary Position Overrides Pin");
 		mntmArbitraryPositionOverride.setSelection(arbitraryPosOverride);
 
 		new MenuItem(menu_edit, SWT.SEPARATOR);
 
 		// === Edit -> Import room layout
 		mntmImport = new MenuItem(menu_edit, SWT.NONE);
 		mntmImport.setText("Import Room Layout...");
 		mntmImport.setEnabled(false);
 
 		// === Edit -> Calculate optimal offset
 		final MenuItem mntmCalculateOptimalOffset = new MenuItem(menu_edit, SWT.NONE);
 		mntmCalculateOptimalOffset.setText("Calculate Optimal Offset");
 		mntmCalculateOptimalOffset.setEnabled(false);
 
 		new MenuItem(menu_edit, SWT.SEPARATOR);
 
 		// === Edit -> Convert to player
 		mntmConToPlayer = new MenuItem(menu_edit, SWT.NONE);
 		mntmConToPlayer.setEnabled(false);
 		mntmConToPlayer.setText("Convert To Player");
 
 		// === Edit -> Convert to enemy
 		mntmConToEnemy = new MenuItem(menu_edit, SWT.NONE);
 		mntmConToEnemy.setEnabled(false);
 		mntmConToEnemy.setText("Convert To Enemy");
 
 		// === View menu
 
 		mntmView = new MenuItem(menu, SWT.CASCADE);
 		mntmView.setText("View");
 		Menu menu_view = new Menu(mntmView);
 		mntmView.setMenu(menu_view);
 
 		// === View -> Errors console
 		MenuItem mntmOpenErrorsConsole = new MenuItem(menu_view, SWT.NONE);
 		mntmOpenErrorsConsole.setText("Open Errors Console");
 
 		// === View -> Tips window
 		MenuItem mntmOpenTipsWindow = new MenuItem(menu_view, SWT.NONE);
 		mntmOpenTipsWindow.setText("Open Tips Window");
 
 		new MenuItem(menu_view, SWT.SEPARATOR);
 
 		// === View -> Show grid
 		final MenuItem mntmGrid = new MenuItem(menu_view, SWT.CHECK);
 		mntmGrid.setSelection(showGrid);
 		mntmGrid.setText("Show Grid \tX");
 
 		new MenuItem(menu_view, SWT.SEPARATOR);
 
 		// === View -> Show anchor
 		final MenuItem mntmShowAnchor = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowAnchor.setText("Show Anchor\t1");
 		mntmShowAnchor.setSelection(showAnchor);
 
 		// === View -> Show mounts
 		final MenuItem mntmShowMounts = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowMounts.setText("Show Mounts\t2");
 		mntmShowMounts.setSelection(showMounts);
 
 		// === View -> show rooms
 		final MenuItem mntmShowRooms = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowRooms.setText("Show Rooms And Doors\t3");
 		mntmShowRooms.setSelection(showRooms);
 
 		// === View -> show hull
 		final MenuItem mntmShowHull = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowHull.setText("Show Hull\t4");
 		mntmShowHull.setSelection(showHull);
 
 		// === View -> show floor
 		mntmShowFloor = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowFloor.setText("Show Floor\t5");
 		mntmShowFloor.setSelection(showFloor);
 
 		// === View -> show shield
 		mntmShowShield = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowShield.setText("Show Shield\t6");
 		mntmShowShield.setSelection(showShield);
 
 		// === View -> show stations
 		mntmShowStations = new MenuItem(menu_view, SWT.CHECK);
 		mntmShowStations.setText("Show Stations\t7");
 		mntmShowStations.setSelection(showStations);
 
 		/*
 		 * new MenuItem(menu_view, SWT.SEPARATOR);
 		 * 
 		 * // === View -> load floor
 		 * final MenuItem mntmLoadFloorGraphic = new MenuItem(menu_view, SWT.CHECK);
 		 * mntmLoadFloorGraphic.setText("Load Floor Graphic");
 		 * mntmLoadFloorGraphic.setSelection(loadFloor);
 		 * mntmLoadFloorGraphic.setEnabled(false);
 		 * 
 		 * // === View -> load shield
 		 * final MenuItem mntmLoadShieldGraphic = new MenuItem(menu_view, SWT.CHECK);
 		 * mntmLoadShieldGraphic.setText("Load Shield Graphic");
 		 * mntmLoadShieldGraphic.setSelection(loadShield);
 		 * mntmLoadShieldGraphic.setEnabled(false);
 		 * 
 		 * // === View -> load system graphic
 		 * MenuItem mntmLoadSystem = new MenuItem(menu_view, SWT.CHECK);
 		 * mntmLoadSystem.setText("Load System Graphics");
 		 * mntmLoadSystem.setSelection(loadSystem);
 		 * mntmLoadSystem.setEnabled(false);
 		 */
 
 		// === About menu
 
 		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
 		mntmAbout.setText("About");
 		// === Tool bar
 
 		// === Container - holds all the items on the left side of the screen
 		Composite toolBarHolder = new Composite(shell, SWT.NONE);
 		toolBarHolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		GridLayout gl_toolBarHolder = new GridLayout(2, false);
 		gl_toolBarHolder.marginWidth = 0;
 		gl_toolBarHolder.marginHeight = 0;
 		toolBarHolder.setLayout(gl_toolBarHolder);
 
 		// === Container -> Tools - tool bar containing the tool icons
 		final ToolBar toolBar = new ToolBar(toolBarHolder, SWT.NONE);
 		toolBar.setFont(appFont);
 		GridData gd_toolBar = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
 		gd_toolBar.minimumHeight = -1;
 		gd_toolBar.minimumWidth = -1;
 		toolBar.setLayoutData(gd_toolBar);
 
 		// === Container -> Tools -> Pointer
 		tltmPointer = new ToolItem(toolBar, SWT.RADIO);
 		tltmPointer.setImage(toolsMap.get("pointer"));
 		tltmPointer.setWidth(60);
 		tltmPointer.setSelection(true);
 		tltmPointer.setToolTipText("Selection Tool [Q]"
 				+ ShipIO.lineDelimiter + " -Click to selet an object"
 				+ ShipIO.lineDelimiter + " -Click and hold to move the object around"
 				+ ShipIO.lineDelimiter + " -For rooms, click on a corner and drag to resize the room"
 				+ ShipIO.lineDelimiter + " -Right-click on a room to assign a system to it"
 				+ ShipIO.lineDelimiter + " -Double click on a room to set its system's level"
 				+ ShipIO.lineDelimiter + " -Double click on a mount to view its details"
 				+ ShipIO.lineDelimiter + " -Press down Ctrl for precision mode"
 				+ ShipIO.lineDelimiter + " -Press down Shift to move the object along a single axis");
 
 		// === Container -> Tools -> Room creation
 		tltmRoom = new ToolItem(toolBar, SWT.RADIO);
 		tltmRoom.setWidth(60);
 		tltmRoom.setToolTipText("Room Creation Tool [W]"
 				+ ShipIO.lineDelimiter + " -Click and drag to create a room"
 				+ ShipIO.lineDelimiter + " -Hold down Shift and click to split rooms");
 		tltmRoom.setImage(toolsMap.get("room"));
 
 		// === Container -> Tools -> Door creation
 		tltmDoor = new ToolItem(toolBar, SWT.RADIO);
 		tltmDoor.setWidth(60);
 		tltmDoor.setToolTipText("Door Creation Tool [E]"
 				+ ShipIO.lineDelimiter + " - Hover over an edge of a room and click to place door"
 				+ ShipIO.lineDelimiter + " - Hold down shift and left/right click on a door to select it, then drag the mouse"
 				+ ShipIO.lineDelimiter + "(while holding the button down) over to a room to link the door to it.");
 		tltmDoor.setImage(toolsMap.get("door"));
 
 		// === Container -> Tools -> Weapon mounting
 		tltmMount = new ToolItem(toolBar, SWT.RADIO);
 		tltmMount.setWidth(60);
 		tltmMount.setToolTipText("Weapon Mounting Tool [R]"
 				+ ShipIO.lineDelimiter + " -Click to place a weapon mount"
 				+ ShipIO.lineDelimiter + " -Right-click to change the mount's rotation"
 				+ ShipIO.lineDelimiter + " -Alt-right-click to mirror the mount along its axis"
 				+ ShipIO.lineDelimiter + " -Shift-right-click to change the direction in which the weapon opens"
 				+ ShipIO.lineDelimiter + " (already placed mounts can be edited with the Selection Tool)");
 		tltmMount.setImage(toolsMap.get("mount"));
 
 		// === Container -> Tools -> System operating slot
 		tltmSystem = new ToolItem(toolBar, SWT.RADIO);
 		tltmSystem.setWidth(60);
 		tltmSystem.setToolTipText("System Station Tool [T]"
 				+ ShipIO.lineDelimiter + " - Click to place an operating station (only mannable systems + medbay)"
 				+ ShipIO.lineDelimiter + " - Right-click to reset the station to default"
 				+ ShipIO.lineDelimiter + " - Shift-click to change facing of the station");
 		tltmSystem.setImage(toolsMap.get("system"));
 
 		tltmGib = new ToolItem(toolBar, SWT.RADIO);
 		tltmGib.setWidth(60);
 		tltmGib.setToolTipText("Gib Editing Tool [G]\n"
 				+ " -Click to select an already placed gib.\n"
 				+ " -Clicking a gib on the list is the same as clicking on it on the screen.\n"
 				+ " -Double-click on a gib to open its properties.\n"
 				+ " -Use arrow keys to nudge the gib.\n"
 				+ " -RMB acts exactly the same as LMB, but doesn't change selection.\n"
 				+ " -Pressing H hides currently selected gib.\n"
 				+ " -Use Move Up/Move Down buttons to change the layering order.");
 		tltmGib.setImage(toolsMap.get("gib"));
 		tltmPointer.setEnabled(false);
 		tltmRoom.setEnabled(false);
 		tltmDoor.setEnabled(false);
 		tltmMount.setEnabled(false);
 		tltmSystem.setEnabled(false);
 		tltmGib.setEnabled(false);
 
 		// === Container -> buttonComposite
 		Composite composite = new Composite(toolBarHolder, SWT.NONE);
 		GridData gd_composite = new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 1);
 		gd_composite.heightHint = 30;
 		composite.setLayoutData(gd_composite);
 		GridLayout gl_composite = new GridLayout(5, false);
 		gl_composite.marginTop = 2;
 		gl_composite.marginHeight = 0;
 		composite.setLayout(gl_composite);
 
 		// === Container -> buttonComposite -> Hull image button
 		btnHull = new Button(composite, SWT.NONE);
 		GridData gd_btnHull = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnHull.minimumWidth = 75;
 		btnHull.setLayoutData(gd_btnHull);
 		btnHull.setFont(appFont);
 		btnHull.setEnabled(false);
 		btnHull.setText("Hull");
 
 		// === Container -> buttonComposite -> shield image button
 		btnShields = new Button(composite, SWT.NONE);
 		GridData gd_btnShields = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnShields.minimumWidth = 75;
 		btnShields.setLayoutData(gd_btnShields);
 		btnShields.setFont(appFont);
 		btnShields.setToolTipText("Shield is aligned in relation to rooms. Place a room before choosing shield graphic.");
 		btnShields.setEnabled(false);
 		btnShields.setText("Shields");
 
 		// === Container -> buttonComposite -> floor image button
 		btnFloor = new Button(composite, SWT.NONE);
 		GridData gd_btnFloor = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnFloor.minimumWidth = 75;
 		btnFloor.setLayoutData(gd_btnFloor);
 		btnFloor.setFont(appFont);
 		btnFloor.setEnabled(false);
 		btnFloor.setText("Floor");
 
 		// === Container -> buttonComposite -> cloak image button
 		btnCloak = new Button(composite, SWT.NONE);
 		GridData gd_btnCloak = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_btnCloak.minimumWidth = 75;
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
 
 		// === Container -> set position composite
 		Composite coSetPosition = new Composite(toolBarHolder, SWT.NONE);
 		coSetPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		GridLayout gl_coSetPosition = new GridLayout(9, false);
 		gl_coSetPosition.marginWidth = 0;
 		gl_coSetPosition.marginHeight = 0;
 		coSetPosition.setLayout(gl_coSetPosition);
 
 		// === Container -> step spinner
 		stepSpinner = new Spinner(coSetPosition, SWT.BORDER);
 		stepSpinner.setSelection(arbitraryStep);
 		stepSpinner.setMinimum(1);
 		stepSpinner.setToolTipText("Step Value" + ShipIO.lineDelimiter
 				+ "Set up the value that gets added whenever you click the -/+ buttons");
 		stepSpinner.setEnabled(false);
 
 		// === Cotnainer -> set position composite -> X
 		Label lblX = new Label(coSetPosition, SWT.NONE);
 		lblX.setFont(appFont);
 		GridData gd_lblX = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
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
 
 		// === Cotnainer -> set position composite -> X buttons container -> X minus
 		btnXminus = new Button(setPosXBtnsCo, SWT.CENTER);
 		btnXminus.setToolTipText("Subtract");
 		btnXminus.setEnabled(false);
 		btnXminus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnXminus.setFont(appFont);
 		btnXminus.setText("-");
 		btnXminus.setBounds(0, 0, 18, 25);
 
 		// === Cotnainer -> set position composite -> X buttons container -> X plus
 		btnXplus = new Button(setPosXBtnsCo, SWT.CENTER);
 		btnXplus.setToolTipText("Add");
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
 		txtX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
 
 		// === Cotnainer -> set position composite -> Y
 		Label lblY = new Label(coSetPosition, SWT.NONE);
 		GridData gd_lblY = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
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
 		btnYminus.setToolTipText("Subtract");
 		btnYminus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnYminus.setFont(appFont);
 		btnYminus.setText("-");
 		btnYminus.setEnabled(false);
 
 		// === Cotnainer -> set position composite -> Y buttons container -> Y plus
 		btnYplus = new Button(setPosYBtnsCo, SWT.CENTER);
 		btnYplus.setToolTipText("Add");
 		btnYplus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		btnYplus.setFont(appFont);
 		btnYplus.setText("+");
 		btnYplus.setEnabled(false);
 
 		// === Cotnainer -> set position composite -> Y text field
 		txtY = new Text(coSetPosition, SWT.BORDER);
 		txtY.setEnabled(false);
 		txtY.setFont(appFont);
 		txtY.setTextLimit(5);
 		txtY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
 
 		// === Container -> Properties
 		final Button btnShipProperties = new Button(coSetPosition, SWT.NONE);
 		btnShipProperties.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
 		btnShipProperties.setFont(appFont);
 		btnShipProperties.setText("Properties");
 		btnShipProperties.setEnabled(false);
 
 		// === Cotnainer -> state buttons composite -> cloaked
 		btnCloaked = new Button(coSetPosition, SWT.TOGGLE | SWT.CENTER);
 		btnCloaked.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		btnCloaked.setEnabled(false);
 		btnCloaked.setFont(appFont);
 		btnCloaked.setImage(Cache.checkOutImage(shell, "/img/smallsys/smallcloak.png"));
 		btnCloaked.setToolTipText("View the cloaked version of the ship.");
 
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
 		fd_canvas.bottom = new FormAttachment(0, GRID_H * 35);
 		fd_canvas.right = new FormAttachment(0, GRID_W * 35);
 		fd_canvas.top = new FormAttachment(0);
 		fd_canvas.left = new FormAttachment(0);
 		canvas.setLayoutData(fd_canvas);
 
 		layeredPainter = new LayeredPainter();
 		canvas.addPaintListener(layeredPainter);
 
 		grid = new Grid(GRID_W, GRID_H);
 		grid.setVisible(showGrid);
 
 		anchor = new Anchor();
 		anchor.setLocation(0, 0, false);
 		anchor.setSize(GRID_W * 35, GRID_H * 35);
 		layeredPainter.add(anchor, LayeredPainter.ANCHOR);
 
 		cursor = new CursorBox();
 		cursor.setBorderThickness(2);
 		cursor.setSize(35, 35);
 		cursor.setBorderColor(new RGB(0, 0, 255));
 		layeredPainter.add(cursor, LayeredPainter.SELECTION);
 
 		hullBox = new HullBox();
 		layeredPainter.add(hullBox, LayeredPainter.HULL);
 
 		shieldBox = new ShieldBox();
 		layeredPainter.add(shieldBox, LayeredPainter.SHIELD);
 
 		tooltip = new Tooltip(Main.canvas);
 
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
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smalloxygen.png");
 		mntmOxygen.setImage(tempImage);
 		mntmOxygen.setText("Oxygen");
 
 		// === Systems -> Systems -> Medbay
 		final MenuItem mntmMedbay = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallmedbay.png");
 		mntmMedbay.setImage(tempImage);
 		mntmMedbay.setText("Medbay");
 
 		// === Systems -> Systems -> Shields
 		final MenuItem mntmShields = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallshields.png");
 		mntmShields.setImage(tempImage);
 		mntmShields.setText("Shields");
 
 		// === Systems -> Systems -> Weapons
 		final MenuItem mntmWeapons = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallweapons.png");
 		mntmWeapons.setImage(tempImage);
 		mntmWeapons.setText("Weapons");
 
 		// === Systems -> Systems -> Engines
 		final MenuItem mntmEngines = new MenuItem(menu_systems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallengines.png");
 		mntmEngines.setImage(tempImage);
 		mntmEngines.setText("Engines");
 
 		// === Systems -> Subsystems
 		MenuItem mntmSubsystems = new MenuItem(menuSystem, SWT.CASCADE);
 		mntmSubsystems.setText("Subsystems");
 		Menu menu_subsystems = new Menu(mntmSubsystems);
 		mntmSubsystems.setMenu(menu_subsystems);
 
 		// === Systems -> Subsystems -> Pilot
 		final MenuItem mntmPilot = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallpilot.png");
 		mntmPilot.setImage(tempImage);
 		mntmPilot.setText("Pilot");
 
 		// === Systems -> Subsystems -> Doors
 		final MenuItem mntmDoors = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smalldoor.png");
 		mntmDoors.setImage(tempImage);
 		mntmDoors.setText("Doors");
 
 		// === Systems -> Subsystems -> Sensors
 		final MenuItem mntmSensors = new MenuItem(menu_subsystems, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallsensors.png");
 		mntmSensors.setImage(tempImage);
 		mntmSensors.setText("Sensors");
 
 		// === Systems -> Special
 		MenuItem mntmSpecial = new MenuItem(menuSystem, SWT.CASCADE);
 		mntmSpecial.setText("Special");
 		Menu menu_special = new Menu(mntmSpecial);
 		mntmSpecial.setMenu(menu_special);
 
 		// === Systems -> Special -> Drones
 		final MenuItem mntmDrones = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smalldrones.png");
 		mntmDrones.setImage(tempImage);
 		mntmDrones.setText("Drones");
 
 		// === Systems -> Special -> Teleporter
 		final MenuItem mntmTeleporter = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallteleporter.png");
 		mntmTeleporter.setImage(tempImage);
 		mntmTeleporter.setText("Teleporter");
 
 		// === Systems -> Special -> Cloaking
 		final MenuItem mntmCloaking = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallcloak.png");
 		mntmCloaking.setImage(tempImage);
 		mntmCloaking.setText("Cloaking");
 
 		// === Systems -> Special -> Artillery
 		final MenuItem mntmArtillery = new MenuItem(menu_special, SWT.RADIO);
 		tempImage = Cache.checkOutImage(shell, "/img/smallsys/smallartillery.png");
 		mntmArtillery.setImage(tempImage);
 		mntmArtillery.setText("Artillery");
 
 		new MenuItem(menuSystem, SWT.SEPARATOR);
 
 		// === Systems -> Set System Image
 		final MenuItem mntmSysImage = new MenuItem(menuSystem, SWT.NONE);
 		mntmSysImage.setEnabled(false);
 		mntmSysImage.setText("Set Interior Image...");
 
 		// === Systems -> Remove System Image
 		final MenuItem mntmRemoveInterior = new MenuItem(menuSystem, SWT.NONE);
 		mntmRemoveInterior.setText("Remove Interior Image");
 
 		new MenuItem(menuSystem, SWT.SEPARATOR);
 
 		// === Systems -> Set Glow Images
 		final MenuItem mntmGlow = new MenuItem(menuSystem, SWT.NONE);
 		mntmGlow.setEnabled(false);
 		mntmGlow.setText("Set Glow Images...");
 
 		// === Gib Assignment Context Menu
 
 		menuGib = new Menu(canvas);
 
 		// === Text Info Fields
 		Composite textHolder = new Composite(shell, SWT.NONE);
 		GridLayout gl_textHolder = new GridLayout(4, false);
 		gl_textHolder.verticalSpacing = 0;
 		gl_textHolder.marginWidth = 0;
 		gl_textHolder.marginHeight = 0;
 		textHolder.setLayout(gl_textHolder);
 		textHolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		// === Position of the pointer on the grid
 		mGridPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
 		GridData gd_mGridPosText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_mGridPosText.widthHint = 70;
 		gd_mGridPosText.minimumWidth = 150;
 		mGridPosText.setLayoutData(gd_mGridPosText);
 		mGridPosText.setFont(appFont);
 
 		mPosText = new Label(textHolder, SWT.BORDER | SWT.CENTER);
 		GridData gd_mPosText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_mPosText.widthHint = 110;
 		gd_mPosText.minimumWidth = 150;
 		mPosText.setLayoutData(gd_mPosText);
 		mPosText.setFont(appFont);
 
 		// === Number of rooms and doors in the ship
 		shipInfoText = new Label(textHolder, SWT.BORDER);
 		shipInfoText.setAlignment(SWT.CENTER);
 		GridData gd_shipInfoText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_shipInfoText.widthHint = 160;
 		gd_shipInfoText.minimumWidth = 300;
 		shipInfoText.setLayoutData(gd_shipInfoText);
 		shipInfoText.setFont(appFont);
 
 		// === Status bar
 		text = new Label(textHolder, SWT.WRAP | SWT.BORDER);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		text.setFont(appFont);
 		new Label(shell, SWT.NONE);
 
 		shell.pack();
 
 		// =======================
 		// === BOOKMARK: LISTENERS
 		// =======================
 
 		Integer[] ignoredLayers = { LayeredPainter.SELECTION, LayeredPainter.GRID, LayeredPainter.ANCHOR, LayeredPainter.SYSTEM_ICON, LayeredPainter.GIB };
 		final MouseInputAdapter mouseListener = new MouseInputAdapter(ignoredLayers);
 		canvas.addMouseMoveListener(mouseListener);
 		canvas.addMouseTrackListener(mouseListener);
 		canvas.addMouseListener(mouseListener);
 
 		// === IMAGE BUTTONS
 
 		btnMiniship.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.miniPath);
 				dialog.setFileName(ship.miniPath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					Main.ship.miniPath = path;
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 				updateButtonImg();
 				canvas.forceFocus();
 			}
 		});
 
 		btnFloor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.floorPath);
 				dialog.setFileName(ship.floorPath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					hullBox.registerDown(Undoable.FLOOR);
 					Main.ship.floorPath = path;
 
 					ShipIO.loadImage(path, "floor");
 					hullBox.registerUp(Undoable.FLOOR);
 					canvas.redraw();
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 				updateButtonImg();
 				canvas.forceFocus();
 			}
 		});
 
 		btnCloak.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.cloakPath);
 				dialog.setFileName(ship.cloakPath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					hullBox.registerDown(Undoable.CLOAK);
 
 					Main.ship.cloakPath = path;
 					btnCloaked.setEnabled(!tltmGib.getSelection());
 
 					ShipIO.loadImage(path, "cloak");
 					hullBox.registerUp(Undoable.CLOAK);
 					canvas.redraw();
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 				updateButtonImg();
 				canvas.forceFocus();
 			}
 		});
 
 		btnShields.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.shieldPath);
 				dialog.setFileName(ship.shieldPath);
 
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					// if (ShipIO.isDefaultResource(new File(path)))
 					// Main.ship.shieldOverride = path;
 					shieldBox.registerDown(Undoable.IMAGE);
 
 					ship.shieldPath = path;
 					ShipIO.loadImage(path, "shields");
 
 					shieldBox.registerUp(Undoable.IMAGE);
 
 					if (ship.isPlayer)
 						if (shieldImage != null && !shieldImage.isDisposed()) {
 							Rectangle temp = shieldImage.getBounds();
 							shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - temp.width / 2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - temp.height / 2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						}
 
 					updateButtonImg();
 
 					canvas.redraw();
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 				canvas.forceFocus();
 			}
 		});
 
 		btnHull.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(ship.imagePath);
 				dialog.setFileName(ship.imagePath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					hullBox.registerDown(Undoable.IMAGE);
 
 					Main.ship.imagePath = path;
 					ShipIO.loadImage(path, "hull");
 
 					hullBox.registerUp(Undoable.IMAGE);
 					canvas.redraw();
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 				updateButtonImg();
 				canvas.forceFocus();
 			}
 		});
 
 		btnHull.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			}
 		});
 		btnShields.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			}
 		});
 		btnFloor.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			}
 		});
 		btnCloak.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			}
 		});
 		btnMiniship.addMouseListener(new MouseAdapter() {
 			public void mouseUp(MouseEvent e) {
 				sourceBtn = (Button) e.widget;
 			}
 		});
 
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
 					mntmPath.setText("..." + s.substring(s.lastIndexOf(ShipIO.pathDelimiter)));
 				} else {
 					mntmPath.setText("");
 				}
 			}
 		});
 
 		mntmUnload.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (sourceBtn == btnHull) {
 					hullBox.registerDown(Undoable.IMAGE);
 					hullBox.setHullImage(null);
 					hullBox.registerUp(Undoable.IMAGE);
 				} else if (sourceBtn == btnShields) {
 					shieldBox.registerDown(Undoable.IMAGE);
 					shieldBox.setImage(null, true);
 					shieldBox.registerUp(Undoable.IMAGE);
 				} else if (sourceBtn == btnFloor) {
 					hullBox.registerDown(Undoable.FLOOR);
 					hullBox.setFloorImage(null);
 					hullBox.registerUp(Undoable.FLOOR);
 				} else if (sourceBtn == btnCloak) {
 					hullBox.registerDown(Undoable.CLOAK);
 					hullBox.setCloakImage(null);
 					btnCloaked.setSelection(false);
 					btnCloaked.setEnabled(false);
 					hullBox.registerUp(Undoable.CLOAK);
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
 				// cloak graphic is handled by HullBox class, the button itself is only used to provide the toggle functionality
 				canvas.redraw();
 			}
 		});
 
 		stepSpinner.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				arbitraryStep = stepSpinner.getSelection();
 			}
 		});
 
 		btnShipProperties.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				shell.setEnabled(false);
 				shipDialog.open();
 
 				shell.setEnabled(true);
 				canvas.redraw();
 				canvas.forceFocus();
 			}
 		});
 
 		// === SELECTED ITEM POSITION
 
 		txtX.addVerifyListener(new VerifyListener() {
 			public void verifyText(VerifyEvent e) {
 				String string = e.text;
 				char[] chars = new char[string.length()];
 				string.getChars(0, chars.length, chars, 0);
 				for (int i = 0; i < chars.length; i++) {
 					if (!('0' <= chars[i] && chars[i] <= '9') && ('-' != chars[i])) {
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
 					if (!('0' <= chars[i] && chars[i] <= '9') && ('-' != chars[i])) {
 						e.doit = false;
 						return;
 					}
 				}
 			}
 		});
 
 		txtX.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				if (e.detail == SWT.TRAVERSE_RETURN) {
 					PaintBox box = getSelected();
 					box.registerDown(Undoable.MOVE);
 					updateSelectedPosition();
 					box.registerUp(Undoable.MOVE);
 				} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
 					updateSelectedPosText();
 				}
 				canvas.forceFocus();
 				e.doit = (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS);
 			}
 		});
 
 		txtY.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				if (e.detail == SWT.TRAVERSE_RETURN) {
 					PaintBox box = getSelected();
 					box.registerDown(Undoable.MOVE);
 					updateSelectedPosition();
 					box.registerUp(Undoable.MOVE);
 				} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
 					updateSelectedPosText();
 				}
 				canvas.forceFocus();
 				e.doit = (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS);
 			}
 		});
 
 		btnXminus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtX.setText("" + (Integer.valueOf(txtX.getText()) - ((selectedRoom == null) ? arbitraryStep : 1)));
 				updateSelectedPosition();
 				canvas.forceFocus();
 			}
 		});
 
 		btnXplus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtX.setText("" + (Integer.valueOf(txtX.getText()) + ((selectedRoom == null) ? arbitraryStep : 1)));
 				updateSelectedPosition();
 				canvas.forceFocus();
 			}
 		});
 
 		btnYminus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtY.setText("" + (Integer.valueOf(txtY.getText()) - ((selectedRoom == null) ? arbitraryStep : 1)));
 				updateSelectedPosition();
 				canvas.forceFocus();
 			}
 		});
 
 		btnYplus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				txtY.setText("" + (Integer.valueOf(txtY.getText()) + ((selectedRoom == null) ? arbitraryStep : 1)));
 				updateSelectedPosition();
 				canvas.forceFocus();
 			}
 		});
 
 		// === SHELL
 
 		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				if (e.keyCode == SWT.SHIFT) {
 					modShift = false;
 					dragDir = null;
 				}
 				if (e.keyCode == SWT.ALT || e.stateMask == SWT.ALT) {
 					modAlt = false;
 				}
 				if (e.keyCode == SWT.CTRL)
 					modCtrl = false;
 
 				if (arrowDown && (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)
 						&& e.stateMask != SWT.CTRL && e.stateMask != SWT.ALT) {
 					arrowDown = false;
 					PaintBox box = getSelected();
 					box.registerUp(Undoable.MOVE);
 				}
 			}
 		});
 
 		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				if (e.keyCode == SWT.SHIFT)
 					modShift = true;
 				if (e.keyCode == SWT.ALT || e.stateMask == SWT.ALT) {
 					modAlt = true;
 				}
 				if (e.keyCode == SWT.CTRL) {
 					modCtrl = true;
 
 					if (mouseListener.dragee != null) {
 						mouseListener.dragee.setOffset(mouseListener.dragee.getBounds().x, mouseListener.dragee.getBounds().y);
 					}
 				}
 
 				// check to make sure that the hotkeys won't be triggered while the user is modifying fields in another window
 				if (shell.isEnabled() && !txtX.isFocusControl() && !txtY.isFocusControl() && !gibWindow.isVisible() && !GibDialog.gibRename.isVisible()) {
 
 					// === element deletion
 					if ((selectedMount != null || selectedRoom != null || selectedDoor != null || selectedGib != null) && (e.keyCode == SWT.DEL || (e.stateMask == SWT.SHIFT && e.keyCode == 'd'))) {
 						PaintBox box = getSelected();
 						deleteObject(box);
 						if (box instanceof FTLGib && selectedGib != null) {
 							gibDialog.btnDeleteGib.setEnabled(false);
 							gibDialog.refreshList();
 						}
 						if (box != null) {
 							AbstractUndoableEdit aue = new UndoableDeleteEdit(box);
 							Main.ueListener.undoableEditHappened(new UndoableEditEvent(box, aue));
 							Main.addEdit(aue);
 						}
 
 						// === deselect
 					} else if (e.keyCode == SWT.ESC) {
 						if (selectedRoom != null)
 							selectedRoom.deselect();
 						selectedRoom = null;
 						if (selectedDoor != null)
 							selectedDoor.deselect();
 						selectedDoor = null;
 						if (selectedMount != null)
 							selectedMount.deselect();
 						selectedMount = null;
 						if (hullBox.isSelected())
 							hullBox.deselect();
 						if (shieldBox.isSelected())
 							shieldBox.deselect();
 
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
 
 						// === undo / redo functions
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'z' && mntmUndo.isEnabled()) {
 						mntmUndo.notifyListeners(SWT.Selection, null);
 					} else if (e.stateMask == SWT.CTRL && e.keyCode == 'y' && mntmRedo.isEnabled()) {
 						mntmRedo.notifyListeners(SWT.Selection, null);
 
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
 					} else if (e.keyCode == '7') {
 						showStations = !showStations;
 						mntmShowStations.setSelection(showStations);
 						canvas.redraw();
 					} else if (e.keyCode == 'x') {
 						showGrid = !showGrid;
 						mntmGrid.setSelection(showGrid);
 						grid.setVisible(showGrid);
 
 						canvas.redraw();
 
 						// === pin
 					} else if (e.keyCode == '`' || e.keyCode == SWT.SPACE) {
 						PaintBox box = getSelected();
 						if (box != null) {
 							// box.registerDown(Undoable.PIN);
 							box.setPinned(!box.isPinned());
 							if (box instanceof FTLRoom)
 								((FTLRoom) box).updateColor();
 							canvasRedraw(box.getBounds(), false);
 						}
 
 						// === tool hotkeys
 					} else if (e.stateMask == SWT.NONE && Main.ship != null && (e.keyCode == 'q' || e.keyCode == 'w' || e.keyCode == 'e' || e.keyCode == 'r' || e.keyCode == 't' || e.keyCode == 'g')) {
 						deselectAll();
 						updateSelectedPosText();
 						boolean prevGib = tltmGib.getSelection();
 						boolean prevMount = tltmMount.getSelection();
 
 						tltmPointer.setSelection(e.keyCode == 'q');
 						tltmRoom.setSelection(e.keyCode == 'w');
 						tltmDoor.setSelection(e.keyCode == 'e');
 						tltmMount.setSelection(e.keyCode == 'r');
 						tltmSystem.setSelection(e.keyCode == 't');
 						tltmGib.setSelection(e.keyCode == 'g');
 
 						gibDialog.setVisible(tltmGib.getSelection());
 						if (prevGib && !tltmGib.getSelection())
 							gibWindow.escape();
 
 						btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 						if (prevGib || prevMount || tltmGib.getSelection())
 							canvas.redraw();
 
 						// === nudge function
 					} else if ((e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)
 							&& e.stateMask != SWT.CTRL && e.stateMask != SWT.ALT) {
 						if (!arrowDown) {
 							arrowDown = true;
 							PaintBox box = getSelected();
 							box.registerDown(Undoable.MOVE);
 						}
 						nudgeSelected(e.keyCode);
 					} else if (tltmGib.getSelection() && e.keyCode == 'h') {
 						if (gibDialog.btnHideGib.isEnabled()) {
 							gibDialog.btnHideGib.notifyListeners(SWT.Selection, null);
 						}
 					}
 				}
 			}
 		});
 
 		SelectionAdapter adapter = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				deselectAll();
 				updateSelectedPosText();
 				if (gibDialog.isVisible()) {
 					gibDialog.setVisible(false);
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath));
 					canvas.redraw();
 				}
 			}
 		};
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
 					mntmSysImage.setEnabled(!selectedRoom.getSystem().equals(Systems.EMPTY) && !selectedRoom.getSystem().equals(Systems.TELEPORTER));
 					mntmGlow.setEnabled(selectedRoom.getSystem().equals(Systems.PILOT) || selectedRoom.getSystem().equals(Systems.SHIELDS)
 							|| selectedRoom.getSystem().equals(Systems.WEAPONS) || selectedRoom.getSystem().equals(Systems.CLOAKING) || selectedRoom.getSystem().equals(Systems.ENGINES));
 				}
 			}
 		});
 
 		mntmEmpty.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 				selectedRoom.assignSystem(Systems.EMPTY);
 				selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 				canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 			}
 		});
 		mntmOxygen.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.OXYGEN, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.OXYGEN);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmMedbay.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.MEDBAY, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.MEDBAY);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmShields.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.SHIELDS, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.SHIELDS);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmWeapons.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.WEAPONS, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.WEAPONS);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmEngines.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.ENGINES, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.ENGINES);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmDoors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.DOORS, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.DOORS);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmPilot.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.PILOT, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.PILOT);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmSensors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.SENSORS, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.SENSORS);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmDrones.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.DRONES, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.DRONES);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmArtillery.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.ARTILLERY, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.ARTILLERY);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmTeleporter.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.TELEPORTER, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.TELEPORTER);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 		mntmCloaking.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!isSystemAssigned(Systems.CLOAKING, selectedRoom)) {
 					selectedRoom.registerDown(Undoable.ASSIGN_SYSTEM);
 					selectedRoom.assignSystem(Systems.CLOAKING);
 					selectedRoom.registerUp(Undoable.ASSIGN_SYSTEM);
 					canvas.redraw(selectedRoom.getBounds().x, selectedRoom.getBounds().y, selectedRoom.getBounds().width, selectedRoom.getBounds().height, true);
 				}
 			}
 		});
 
 		mntmSysImage.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.png" };
 				dialog.setFilterExtensions(filterExtensions);
 
 				dialog.setFilterPath(interiorPath);
 				dialog.setFileName(interiorPath);
 
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && selectedRoom != null && new File(path).exists()) {
 					selectedRoom.registerDown(Undoable.IMAGE);
 
 					interiorPath = new String(path);
 					selectedRoom.setInterior(path);
 
 					selectedRoom.registerUp(Undoable.IMAGE);
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage(Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 			}
 		});
 
 		mntmGlow.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				glowWindow.open();
 			}
 		});
 
 		mntmRemoveInterior.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				selectedRoom.registerDown(Undoable.IMAGE);
 				selectedRoom.setInterior(null);
 				selectedRoom.registerUp(Undoable.IMAGE);
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
 				if (!askedChoice) {
 					askSaveChoice();
 					askedChoice = false;
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							return;
 					}
 				}
 
 				undoManager.discardAllEdits();
 
 				int create = new NewShipDialog(shell).open();
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
 						// ShipIO.loadImage(ship.shieldPath, "shields");
 						shieldBox.setSize(200, 200);
 						shieldBox.setLocation(anchor.getLocation().x, anchor.getLocation().y);
 					}
 
 					mntmSaveShip.setEnabled(true);
 					mntmSaveShipAs.setEnabled(true);
 					mntmExport.setEnabled(true);
 					mntmClose.setEnabled(true);
 					mntmImport.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					mntmInclude.setEnabled(false);
 					mntmCalculateOptimalOffset.setEnabled(true);
 
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
 				if (!askedChoice) {
 					askSaveChoice();
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							askedChoice = false;
 							return;
 					}
 				}
 
 				ShipBrowser shipBrowser = new ShipBrowser(shell);
 				shipBrowser.shell.open();
 
 				shipBrowser.shell.addDisposeListener(new DisposeListener() {
 					@Override
 					public void widgetDisposed(DisposeEvent e) {
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
 							mntmImport.setEnabled(true);
 							mntmArchives.setEnabled(false);
 							mntmInclude.setEnabled(false);
 							mntmCalculateOptimalOffset.setEnabled(true);
 
 							if (ship.isPlayer) {
 								if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
 									Rectangle temp = shieldImage.getBounds();
 									shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - temp.width / 2 + ship.ellipse.x;
 									shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - temp.height / 2 + ship.ellipse.y;
 									shieldEllipse.width = temp.width;
 									shieldEllipse.height = temp.height;
 								} else {
 									shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - ship.ellipse.width + ship.ellipse.x;
 									shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - ship.ellipse.height + ship.ellipse.y;
 									shieldEllipse.width = ship.ellipse.width * 2;
 									shieldEllipse.height = ship.ellipse.height * 2;
 								}
 							} else {
 								shieldEllipse.width = ship.ellipse.width * 2;
 								shieldEllipse.height = ship.ellipse.height * 2;
 								shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 + ship.ellipse.x - ship.ellipse.width;
 								shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 + ship.ellipse.y - ship.ellipse.height + 110;
 							}
 							// ShipIO.updateIndexImgMaps();
 
 							btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 
 							currentPath = null;
 
 							mntmConToPlayer.setEnabled(!ship.isPlayer);
 							mntmConToEnemy.setEnabled(ship.isPlayer);
 
 							canvas.redraw();
 						}
 						if (ShipIO.errors.size() == 0 && Main.ship != null) {
 							Main.print(((Main.ship.shipName != null) ? (Main.ship.shipClass + " \"" + Main.ship.shipName + "\"") : (Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
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
 				if (!askedChoice) {
 					askSaveChoice();
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							askedChoice = false;
 							return;
 					}
 				}
 
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.ftl" };
 				dialog.setFilterExtensions(filterExtensions);
 
 				dialog.setFilterPath(ftlLoadPath);
 				dialog.setFileName(ftlLoadPath);
 
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					ftlLoadPath = new String(path);
 					debug("Load ship from .ftl:", true);
 
 					temporaryFiles = new File("sprlmnl_tmp");
 					temporaryFiles.mkdirs();
 					temporaryFilesInUse = true;
 
 					if (temporaryFiles != null && temporaryFiles.exists()) {
 						debug("\tdeleting temporary directory... ", false);
 						ShipIO.deleteFolderContents(temporaryFiles);
 						if (temporaryFiles.exists())
 							ShipIO.rmdir(temporaryFiles);
 						debug("done", true);
 					}
 
 					ZipFile zf = null;
 					try {
 						zf = new ZipFile(path);
 
 						debug("\textracting contents of .ftl package to temporary directory... ", false);
 						ShipIO.unzipFileToDirectory(zf, temporaryFiles);
 						debug("done", true);
 
 						File unpacked_blueprints = null;
 						unpacked_blueprints = new File("sprlmnl_tmp" + ShipIO.pathDelimiter + "data" + ShipIO.pathDelimiter + "autoBlueprints.xml.append");
 						ArrayList<String> blueList = new ArrayList<String>();
 						ArrayList<String> tempList = null;
 
 						debug("\tscanning " + unpacked_blueprints.getName() + " for ship blueprints... ", false);
 						tempList = (ArrayList<String>) ShipIO.preScanFTL(unpacked_blueprints);
 						debug("done" + ((blueList == null || blueList.size() == 0) ? ", none found" : ""), true);
 
 						if (tempList != null)
 							blueList.addAll(tempList);
 
 						if (blueList == null || blueList.size() == 0) {
 							unpacked_blueprints = new File("sprlmnl_tmp" + ShipIO.pathDelimiter + "data" + ShipIO.pathDelimiter + "blueprints.xml.append");
 							debug("\tscanning " + unpacked_blueprints.getName() + " for ship blueprints... ", false);
 							blueList = (ArrayList<String>) ShipIO.preScanFTL(unpacked_blueprints);
 							debug("done" + ((blueList == null || blueList.size() == 0) ? ", none found" : ""), true);
 
 							if (tempList != null)
 								blueList.addAll(tempList);
 						}
 
 						if (blueList.size() != 0) {
 							// loading weapons, drones and augments contained within package
 							// store old maps to temporary variables so that they can be restored once the ship is closed.
 							ShipIO.oldWeaponMap = new HashMap<String, FTLItem>(ShipIO.weaponMap);
 							ShipIO.oldDroneMap = new HashMap<String, FTLItem>(ShipIO.droneMap);
 							ShipIO.oldAugMap = new HashMap<String, FTLItem>(ShipIO.augMap);
 							ShipIO.oldWeaponSetMap = new HashMap<String, HashSet<String>>(ShipIO.weaponSetMap);
 							ShipIO.oldDroneSetMap = new HashMap<String, HashSet<String>>(ShipIO.droneSetMap);
 
 							// load data from .ftl package into maps
 							debug("\tloading declarations from package...", true);
 							ShipIO.loadDeclarationsFromFile(unpacked_blueprints);
 							debug("\t\tdone", true);
 
 							if (blueList.size() == 1) {
 								mntmClose.notifyListeners(SWT.Selection, null);
 								ShipIO.loadShip(blueList.get(0), unpacked_blueprints, -1);
 							} else {
 								choiceDialog.setChoices(blueList, unpacked_blueprints);
 								String blueprint = choiceDialog.open();
 
 								mntmClose.notifyListeners(SWT.Selection, null);
 								ShipIO.loadShip(blueprint, unpacked_blueprints, choiceDialog.declaration);
 							}
 						} else {
 							Main.erDialog.print("Error: load ship from .ftl - no ship declarations found in the package. No data was loaded.");
 						}
 					} catch (IOException ex) {
 					} finally {
 						if (zf != null)
 							try {
 								zf.close();
 							} catch (IOException ex) {
 							}
 
 						if (ShipIO.oldWeaponMap.size() > 0) {
 							ShipIO.clearMaps();
 							ShipIO.weaponMap.putAll(ShipIO.oldWeaponMap);
 							ShipIO.droneMap.putAll(ShipIO.oldDroneMap);
 							ShipIO.augMap.putAll(ShipIO.oldAugMap);
 							ShipIO.weaponSetMap.putAll(ShipIO.oldWeaponSetMap);
 							ShipIO.droneSetMap.putAll(ShipIO.oldDroneSetMap);
 
 							ShipIO.clearOldMaps();
 						}
 					}
 
 					temporaryFilesInUse = false;
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
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
 					mntmImport.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					mntmInclude.setEnabled(false);
 					mntmCalculateOptimalOffset.setEnabled(true);
 
 					if (ship.isPlayer) {
 						if (loadShield && shieldImage != null && !shieldImage.isDisposed()) {
 							Rectangle temp = shieldImage.getBounds();
 							shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - temp.width / 2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - temp.height / 2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						} else {
 							shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - ship.ellipse.width + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - ship.ellipse.height + ship.ellipse.y;
 							shieldEllipse.width = ship.ellipse.width * 2;
 							shieldEllipse.height = ship.ellipse.height * 2;
 						}
 					} else {
 						shieldEllipse.width = ship.ellipse.width * 2;
 						shieldEllipse.height = ship.ellipse.height * 2;
 						shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 + ship.ellipse.x - ship.ellipse.width;
 						shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 + ship.ellipse.y - ship.ellipse.height + 110;
 					}
 					// ShipIO.updateIndexImgMaps();
 
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 
 					currentPath = null;
 
 					mntmConToPlayer.setEnabled(!ship.isPlayer);
 					mntmConToEnemy.setEnabled(ship.isPlayer);
 
 					canvas.redraw();
 				}
 				if (ShipIO.errors.size() == 0 && Main.ship != null) {
 					Main.print(((Main.ship.shipName != null) ? (Main.ship.shipClass + " - " + Main.ship.shipName) : (Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "] loaded successfully.");
 				} else if (ShipIO.errors.size() > 0) {
 					Main.print("Errors occured during ship loading; some data may be missing.");
 					Main.erDialog.printErrors(ShipIO.errors);
 					Main.erDialog.open();
 
 					ShipIO.errors.clear();
 				}
 
 				Main.shell.setEnabled(true);
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
 				if (!askedChoice) {
 					askSaveChoice();
 					askedChoice = false;
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							return;
 					}
 				}
 
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
 							shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - temp.width / 2 + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - temp.height / 2 + ship.ellipse.y;
 							shieldEllipse.width = temp.width;
 							shieldEllipse.height = temp.height;
 						} else {
 							shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 - ship.ellipse.width + ship.ellipse.x;
 							shieldEllipse.y = ship.anchor.y + ship.offset.y * 35 + ship.computeShipSize().y / 2 - ship.ellipse.height + ship.ellipse.y;
 							shieldEllipse.width = ship.ellipse.width * 2;
 							shieldEllipse.height = ship.ellipse.height * 2;
 						}
 					} else {
 						shieldEllipse.width = ship.ellipse.width * 2;
 						shieldEllipse.height = ship.ellipse.height * 2;
 						shieldEllipse.x = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().x / 2 + ship.ellipse.x - ship.ellipse.width;
 						shieldEllipse.y = ship.anchor.x + ship.offset.x * 35 + ship.computeShipSize().y / 2 + ship.ellipse.y - ship.ellipse.height + 110;
 					}
 
 					btnCloaked.setEnabled(!ShipIO.isNull(ship.cloakPath) && !tltmGib.getSelection());
 
 					recalculateShieldCenter();
 
 					mntmSaveShip.setEnabled(true);
 					mntmSaveShipAs.setEnabled(true);
 					mntmExport.setEnabled(true);
 					mntmClose.setEnabled(true);
 					mntmImport.setEnabled(true);
 					mntmArchives.setEnabled(false);
 					mntmInclude.setEnabled(false);
 					mntmCalculateOptimalOffset.setEnabled(true);
 
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
 
 		mntmImport.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.txt" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(importPath);
 				dialog.setFileName(importPath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					ShipIO.loadLayout(new File(path));
 					canvas.redraw();
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 			}
 		});
 
 		mntmArchives.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				shell.setEnabled(false);
 				dirWindow.label.setText("If you wish to unpack the archives again, just browse to the .dat files again." + ShipIO.lineDelimiter
 						+ "Note however that the old unpacked archives used by Superluminal will be deleted.");
 				dirWindow.open();
 			}
 		});
 
 		mntmInclude.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!shownIncludeWarning) {
 					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
 					box.setMessage("IMPORTANT INFORMATION:" + ShipIO.lineDelimiter + "Superluminal has to be restarted after a mod is included for the changes to take effect!");
 					box.open();
 
 					shownIncludeWarning = true;
 					ConfigIO.saveConfig();
 				}
 
 				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 				String[] filterExtensions = new String[] { "*.txt", "*.xml", "*.append", "*.png", "*.ftl" };
 				dialog.setFilterExtensions(filterExtensions);
 				dialog.setFilterPath(includePath);
 				dialog.setFileName(includePath);
 				String path = dialog.open();
 
 				if (!ShipIO.isNull(path) && new File(path).exists()) {
 					includePath = path;
 					File f = new File(includePath);
 					if (f.exists()) {
 						if (ShipIO.includeMod(f, false)) {
 							print("Mod loaded successfully");
 						} else {
 							print("An error occured while loading mod; check debug.log for stack trace.");
 						}
 					} else {
 						print("Cannot load mod because no such file was found");
 					}
 				} else {
 					if (path != null) {
 						MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 						box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 						box.open();
 					}
 				}
 			}
 		});
 
 		mntmClose.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!askedChoice) {
 					askSaveChoice();
 					askedChoice = false;
 					switch (askSaveChoice) {
 						case SWT.YES:
 							mntmSaveShip.notifyListeners(SWT.Selection, null);
 							break;
 
 						case SWT.NO:
 							break;
 
 						case SWT.CANCEL:
 							return;
 					}
 				}
 
 				undoManager.discardAllEdits();
 
 				if (ship != null) {
 					for (MenuItem mntm : menuGibItems) {
 						if (!mntm.isDisposed())
 							mntm.dispose();
 						mntm = null;
 					}
 					menuGibItems.clear();
 
 					for (SystemBox sb : systemsMap.values())
 						sb.clearInterior();
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
 
 				if (temporaryFiles != null && !temporaryFilesInUse) {
 					debug("\tdeleting temporary directory... ", false);
 					ShipIO.deleteFolderContents(temporaryFiles);
 					if (temporaryFiles.exists())
 						ShipIO.rmdir(temporaryFiles);
 					debug("done", true);
 					temporaryFiles = null;
 					temporaryFilesInUse = false;
 				}
 
 				shieldBox.deselect();
 				hullBox.deselect();
 				selectedRoom = null;
 				selectedDoor = null;
 				selectedMount = null;
 				selectedGib = null;
 
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
 				stepSpinner.setEnabled(false);
 
 				mntmSaveShip.setEnabled(false);
 				mntmSaveShipAs.setEnabled(false);
 				mntmExport.setEnabled(false);
 				mntmClose.setEnabled(false);
 				mntmImport.setEnabled(false);
 				mntmArchives.setEnabled(true);
 				mntmInclude.setEnabled(true);
 				mntmConToEnemy.setEnabled(false);
 				mntmConToPlayer.setEnabled(false);
 				mntmCalculateOptimalOffset.setEnabled(false);
 
 				ship = null;
 				askedChoice = false;
 				askSaveChoice = 0;
 				savedSinceAction = true;
 				shell.setText(APPNAME + " - Ship Editor");
 
 				canvas.redraw();
 			}
 		});
 
 		// === EDIT MENU
 
 		mntmUndo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (undoManager.canUndo()) {
 					print(undoManager.getUndoPresentationName());
 					undoManager.undo();
 				}
 				updateUndoButtons();
 			}
 		});
 
 		mntmRedo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (undoManager.canRedo()) {
 					print(undoManager.getRedoPresentationName());
 					undoManager.redo();
 				}
 				updateUndoButtons();
 			}
 		});
 
 		mntmRemoveDoors.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeDoor = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 			}
 		});
 
 		mntmArbitraryPositionOverride.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				arbitraryPosOverride = ((MenuItem) e.widget).getSelection();
 				ConfigIO.saveConfig();
 			}
 		});
 
 		mntmCalculateOptimalOffset.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (ship == null || ship.rooms.size() == 0)
 					return;
 
 				anchor.registerDown(Undoable.MOVE);
 
 				Point size = ship.computeShipSize();
 
 				if (ship.isPlayer) {
 					// ~18 is max X size, 12 is max Y size
 					final int X_MAX = 16;
 					final int Y_MAX = 11;
 
 					size.x /= 35;
 					size.y /= 35;
 
 					ship.offset.x = Math.max((X_MAX - size.x) / 2, 0);
 					ship.offset.y = Math.max((Y_MAX - size.y) / 2, 0);
 
 					size = ship.findLowBounds();
 					int x = size.x - ship.offset.x * 35;
 					int y = size.y - ship.offset.y * 35;
 					anchor.setLocation(x, y, false);
 					Main.ship.anchor.x = x;
 					Main.ship.anchor.y = y;
 				} else {
 					// enemy window is 376 x 504, 55px top margin
 					// final int WIDTH = 376;
 					final int HEIGHT = 504;
 					final int TOP_MARGIN = 55;
 
 					ship.offset.x = 0;
 					ship.offset.y = 0;
 
 					ship.horizontal = 0;
 					ship.vertical = (HEIGHT / 2 - size.y / 2 - (int) (TOP_MARGIN * 1.5));
 				}
 
 				if (anchor.getBox().x < 0)
 					anchor.setLocation(0, anchor.getBox().y, true);
 				if (anchor.getBox().y < 0)
 					anchor.setLocation(anchor.getBox().x, 0, true);
 
 				anchor.registerUp(Undoable.MOVE);
 
 				canvas.redraw();
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
 
 		mntmOpenTipsWindow.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				tipWindow.open();
 			}
 		});
 
 		mntmGrid.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showGrid = ((MenuItem) e.widget).getSelection();
 				grid.setVisible(showGrid);
 				canvas.redraw();
 			}
 		});
 
 		mntmShowAnchor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showAnchor = ((MenuItem) e.widget).getSelection();
 				canvas.redraw();
 			}
 		});
 
 		mntmShowMounts.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showMounts = ((MenuItem) e.widget).getSelection();
 				showMounts();
 				canvas.redraw();
 			}
 		});
 
 		mntmShowRooms.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showRooms = ((MenuItem) e.widget).getSelection();
 				showRooms();
 				canvas.redraw();
 			}
 		});
 
 		mntmShowHull.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showHull = ((MenuItem) e.widget).getSelection();
 				hullBox.setVisible(showHull || showFloor);
 				canvas.redraw();
 			}
 		});
 
 		mntmShowFloor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showFloor = ((MenuItem) e.widget).getSelection();
 				hullBox.setVisible(showHull || showFloor);
 				canvas.redraw();
 			}
 		});
 
 		mntmShowShield.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showShield = ((MenuItem) e.widget).getSelection();
 				canvas.redraw();
 			}
 		});
 
 		mntmShowStations.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				showStations = ((MenuItem) e.widget).getSelection();
 				canvas.redraw();
 			}
 		});
 
 		mntmAbout.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				AboutWindow about = new AboutWindow(shell);
 				about.shell.open();
 				about = null;
 			}
 		});
 
 		/*
 		 * mntmLoadFloorGraphic.addSelectionListener(new SelectionAdapter() {
 		 * public void widgetSelected(SelectionEvent e) {
 		 * loadFloor = ((MenuItem) e.widget).getSelection();
 		 * ConfigIO.saveConfig();
 		 * canvas.redraw();
 		 * }
 		 * });
 		 * 
 		 * mntmLoadShieldGraphic.addSelectionListener(new SelectionAdapter() {
 		 * public void widgetSelected(SelectionEvent e) {
 		 * loadShield = ((MenuItem) e.widget).getSelection();
 		 * ConfigIO.saveConfig();
 		 * canvas.redraw();
 		 * }
 		 * });
 		 * 
 		 * mntmLoadSystem.addSelectionListener(new SelectionAdapter() {
 		 * public void widgetSelected(SelectionEvent e) {
 		 * loadSystem = ((MenuItem) e.widget).getSelection();
 		 * ConfigIO.saveConfig();
 		 * canvas.redraw();
 		 * }
 		 * });
 		 */
 	}
 
 	// ======================================================
 	// === BOOKMARK: AUXILIARY METHODS
 
 	// === SHIP CONVERSIONS
 
 	public void convertToPlayer() {
 		if (Main.ship != null) {
 			ship.isPlayer = true;
 
 			btnShields.setEnabled(ship.rooms.size() > 0);
 
 			ship.shieldPath = null;
 			if (shieldImage != null && !shieldImage.isDisposed())
 				shieldBox.setImage(null, true);
 			shieldImage = null;
 
 			shieldEllipse.x = 0;
 			shieldEllipse.y = 0;
 			shieldEllipse.width = 0;
 			shieldEllipse.height = 0;
 
 			updateButtonImg();
 
 			btnFloor.setEnabled(true);
 			btnMiniship.setEnabled(true);
 
 			if (ship.weaponsBySet)
 				ship.weaponSet.clear();
 			if (ship.dronesBySet)
 				ship.droneSet.clear();
 			ship.weaponsBySet = false;
 			ship.dronesBySet = false;
 
 			ship.minSec = 0;
 			ship.maxSec = 0;
 
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
 
 			if (shieldImage != null && !shieldImage.isDisposed())
 				shieldBox.setImage(null, true);
 			shieldImage = null;
 
 			ship.shieldPath = resPath + ShipIO.pathDelimiter + "img" + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "enemy_shields.png";
 			ShipIO.loadImage(ship.shieldPath, "shields");
 
 			if (floorImage != null && !floorImage.isDisposed())
 				hullBox.setFloorImage(null);
 			floorImage = null;
 			ship.floorPath = null;
 
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
 		return Math.round(a / 35) * 35;
 	}
 
 	/**
 	 * Aligns to the lowest (left-most / top-most) line of the grid.
 	 */
 	public static int downToGrid(int a) {
 		return (int) (Math.ceil(a / 35) * 35);
 	}
 
 	/**
 	 * Aligns to the highest (right-most / bottom-most) line of the grid.
 	 */
 	public static int upToGrid(int a) {
 		return (int) (Math.floor(a / 35) * 35);
 	}
 
 	// =================
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
 	 * 
 	 * @param r
 	 *            Rectangle that needs to be fixed.
 	 * @return Fixed rectangle
 	 */
 	public static Rectangle fixRect(Rectangle r) {
 		Rectangle rect = new Rectangle(0, 0, 0, 0);
 		rect.x = r.width < 0 ? r.x + r.width : r.x;
 		rect.y = r.height < 0 ? r.y + r.height : r.y;
 		rect.width = r.width < 0 ? -r.width : r.width;
 		rect.height = r.height < 0 ? -r.height : r.height;
 		return rect;
 	}
 
 	/**
 	 * Checks if given rect overlaps any of the already placed rooms. If given rect is inside the roomsList set, it doesn't perform check against that rect (meaning it won't return true).
 	 * 
 	 * @param rect
 	 *            rectangle to be checked
 	 * @param treatAs
 	 *            if set to another rectangle, the self-exclusive check will be performed against that rectangle, and not the one in the first parameter. Can be set to null if not used.
 	 * @return true if rect given in parameter overlaps any of already placed rooms/rects.
 	 */
 	public static boolean doesRectOverlap(Rectangle rect, Rectangle treatAs) {
 		for (FTLRoom r : ship.rooms) {
 			if (rect.intersects(r.getBounds()) && ((treatAs != null && r.getBounds() != treatAs) || (treatAs == null && r.getBounds() != rect))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if a rect is wholly contained within another.
 	 */
 	public static boolean containsRect(Rectangle r1, Rectangle r2) {
 		return r1.contains(r2.x, r2.y) && r1.contains(r2.x + r2.width, r2.y + r2.height);
 	}
 
 	public Rectangle getRectFromClick() {
 		Rectangle tempRect = new Rectangle(0, 0, 35, 35);
 		for (int x = 0; x < GRID_W; x++) {
 			for (int y = 0; y < GRID_H; y++) {
 				tempRect.x = x * 35;
 				tempRect.y = y * 35;
 				if (tempRect.contains(mousePosLastClick)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static Rectangle getRectAt(int x, int y) {
 		Rectangle tempRect = new Rectangle(0, 0, 35, 35);
 		Point p = new Point(x, y);
 		for (int i = 0; i < GRID_W; i++) {
 			for (int j = 0; j < GRID_H; j++) {
 				tempRect.x = i * 35;
 				tempRect.y = j * 35;
 				if (tempRect.contains(p)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 
 	public Rectangle getRectFromMouse() {
 		Rectangle tempRect = new Rectangle(0, 0, 35, 35);
 		for (int x = 0; x < GRID_W; x++) {
 			for (int y = 0; y < GRID_H; y++) {
 				tempRect.x = x * 35;
 				tempRect.y = y * 35;
 				if (tempRect.contains(mousePos)) {
 					return tempRect;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void nudgeSelected(int event) {
 		// check so that if none is selected the function won't even bother going in
 		if (hullBox.isSelected() || shieldBox.isSelected() || selectedGib != null || selectedMount != null || selectedRoom != null) {
 			Rectangle tempRect;
 			switch (event) {
 				case (SWT.ARROW_UP):
 					if (selectedRoom != null) {
 						tempRect = cloneRect(selectedRoom.getBounds());
 						tempRect.y -= 35;
 						if (!doesRectOverlap(tempRect, selectedRoom.getBounds()) && tempRect.y >= ship.anchor.y)
 							selectedRoom.setLocation(selectedRoom.getLocation().x, selectedRoom.getLocation().y - 35);
 					}
 					if (selectedMount != null)
 						selectedMount.setLocation(selectedMount.getLocation().x, selectedMount.getLocation().y - ((modShift) ? 35 : 1));
 					if (selectedGib != null && tltmGib.getSelection())
 						selectedGib.setLocationAbsolute(selectedGib.getLocation().x, selectedGib.getLocation().y - ((modShift) ? 35 : 1));
 					if (hullBox.isSelected())
 						hullBox.setLocation(hullBox.getLocation().x, hullBox.getLocation().y - ((modShift) ? 35 : 1));
 					if (shieldBox.isSelected())
 						shieldBox.setLocation(shieldBox.getLocation().x, shieldBox.getLocation().y - ((modShift) ? 35 : 1));
 					break;
 				case (SWT.ARROW_DOWN):
 					if (selectedRoom != null) {
 						tempRect = cloneRect(selectedRoom.getBounds());
 						tempRect.y += 35;
 						if (!doesRectOverlap(tempRect, selectedRoom.getBounds()) && tempRect.y + tempRect.height <= GRID_H * 35)
 							selectedRoom.setLocation(selectedRoom.getLocation().x, selectedRoom.getLocation().y + 35);
 					}
 					if (selectedMount != null)
 						selectedMount.setLocation(selectedMount.getLocation().x, selectedMount.getLocation().y + ((modShift) ? 35 : 1));
 					if (selectedGib != null && tltmGib.getSelection())
 						selectedGib.setLocationAbsolute(selectedGib.getLocation().x, selectedGib.getLocation().y + ((modShift) ? 35 : 1));
 					if (hullBox.isSelected())
 						hullBox.setLocation(hullBox.getLocation().x, hullBox.getLocation().y + ((modShift) ? 35 : 1));
 					if (shieldBox.isSelected())
 						shieldBox.setLocation(shieldBox.getLocation().x, shieldBox.getLocation().y + ((modShift) ? 35 : 1));
 					break;
 				case (SWT.ARROW_LEFT):
 					if (selectedRoom != null) {
 						tempRect = cloneRect(selectedRoom.getBounds());
 						tempRect.x -= 35;
 						if (!doesRectOverlap(tempRect, selectedRoom.getBounds()) && tempRect.x >= ship.anchor.x)
 							selectedRoom.setLocation(selectedRoom.getLocation().x - 35, selectedRoom.getLocation().y);
 					}
 					if (selectedMount != null)
 						selectedMount.setLocation(selectedMount.getLocation().x - ((modShift) ? 35 : 1), selectedMount.getLocation().y);
 					if (selectedGib != null && tltmGib.getSelection())
 						selectedGib.setLocationAbsolute(selectedGib.getLocation().x - ((modShift) ? 35 : 1), selectedGib.getLocation().y);
 					if (hullBox.isSelected())
 						hullBox.setLocation(hullBox.getLocation().x - ((modShift) ? 35 : 1), hullBox.getLocation().y);
 					if (shieldBox.isSelected())
 						shieldBox.setLocation(shieldBox.getLocation().x - ((modShift) ? 35 : 1), shieldBox.getLocation().y);
 					break;
 				case (SWT.ARROW_RIGHT):
 					if (selectedRoom != null) {
 						tempRect = cloneRect(selectedRoom.getBounds());
 						tempRect.x += 35;
 						if (!doesRectOverlap(tempRect, selectedRoom.getBounds()) && tempRect.x + tempRect.width <= GRID_W * 35)
 							selectedRoom.setLocation(selectedRoom.getLocation().x + 35, selectedRoom.getLocation().y);
 					}
 					if (selectedMount != null)
 						selectedMount.setLocation(selectedMount.getLocation().x + ((modShift) ? 35 : 1), selectedMount.getLocation().y);
 					if (selectedGib != null && tltmGib.getSelection())
 						selectedGib.setLocationAbsolute(selectedGib.getLocation().x + ((modShift) ? 35 : 1), selectedGib.getLocation().y);
 					if (hullBox.isSelected())
 						hullBox.setLocation(hullBox.getLocation().x + ((modShift) ? 35 : 1), hullBox.getLocation().y);
 					if (shieldBox.isSelected())
 						shieldBox.setLocation(shieldBox.getLocation().x + ((modShift) ? 35 : 1), shieldBox.getLocation().y);
 					break;
 				default:
 					break;
 			}
 
 			if (selectedRoom != null)
 				updateCorners(selectedRoom);
 			if (shieldBox.isSelected()) {
 				ship.ellipse.x = (shieldEllipse.x + shieldEllipse.width / 2) - (ship.findLowBounds().x + ship.computeShipSize().x / 2);
 				ship.ellipse.y = (shieldEllipse.y + shieldEllipse.height / 2) - (ship.findLowBounds().y + ship.computeShipSize().y / 2) - ((ship.isPlayer) ? 0 : 110);
 				ship.ellipse.width = shieldEllipse.width / 2;
 				ship.ellipse.height = shieldEllipse.height / 2;
 			}
 			updateSelectedPosText();
 
 			canvas.redraw();
 		}
 	}
 
 	// =================
 	// === ROOM RELATED
 
 	public static void updateCorners(FTLRoom r) {
 		corners[0] = new Rectangle(r.getBounds().x, r.getBounds().y, CORNER, CORNER);
 		corners[1] = new Rectangle(r.getBounds().x + r.getBounds().width - CORNER, r.getBounds().y, CORNER, CORNER);
 		corners[2] = new Rectangle(r.getBounds().x, r.getBounds().y + r.getBounds().height - CORNER, CORNER, CORNER);
 		corners[3] = new Rectangle(r.getBounds().x + r.getBounds().width - CORNER, r.getBounds().y + r.getBounds().height - CORNER, CORNER, CORNER);
 	}
 
 	public static void recalculateShieldCenter() {
 		if (Main.ship != null) {
 			Point center = new Point(0, 0);
 			center.x = shieldEllipse.x + shieldEllipse.width / 2 - ship.ellipse.x;
 			center.y = shieldEllipse.y + shieldEllipse.height / 2 - ship.ellipse.y + (ship.isPlayer ? 0 : 110);
 
 			Point low = ship.findLowBounds();
 			Point high = ship.findHighBounds();
 
 			// new center
 			low.x = low.x + (high.x - low.x) / 2;
 			low.y = low.y + (high.y - low.y) / 2;
 
 			// difference
 			center.x = center.x - low.x;
 			center.y = center.y - low.y;
 
 			low = shieldBox.getLocation();
 			shieldBox.setLocation(low.x - center.x, low.y - center.y);
 		}
 	}
 
 	public static int getLowestId() {
 		int i = -1;
 		idList.add(-1);
 		while (i < GRID_W * GRID_H && idList.contains(i)) {
 			i++;
 		}
 		return i;
 	}
 
 	public static int getLowestMountIndex(FTLShip ship) {
 		int i = -1;
 		HashSet<Integer> indexList = new HashSet<Integer>();
 		indexList.add(-1); // so that we can iterate over the set
 		for (FTLMount m : ship.mounts)
 			indexList.add(m.index);
 
 		while (i < MAX_MOUNTS && indexList.contains(i)) {
 			i++;
 		}
 
 		return i;
 	}
 
 	/**
 	 * @param i
 	 *            Index that is to be checked
 	 * @return True if index is taken, false is it's free
 	 */
 	public static boolean isMountIndexTaken(int i) {
 		for (FTLMount m : ship.mounts)
 			if (m.index == i)
 				return true;
 		return false;
 	}
 
 	public static Point findFarthestCorner(FTLRoom r, Point p) {
 		double d = 0;
 		double t = 0;
 		Point pt = null;
 		for (int i = 0; i < 4; i++) {
 			t = Math.sqrt(Math.pow(r.corners[i].x - p.x, 2) + Math.pow(r.corners[i].y - p.y, 2));
 			if (d < t) {
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
 
 	public static FTLRoom getRoomAt(int x, int y) {
 		for (FTLRoom r : ship.rooms) {
 			if (r.getBounds().contains(x, y))
 				return r;
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
 		int w = r.getBounds().width / 35;
 		int y = (int) Math.floor(r.slot / w);
 		int x = r.slot - y * w;
 
 		return new Rectangle(r.getBounds().x + x * 35, r.getBounds().y + y * 35, 35, 35);
 	}
 
 	public static int getStationFromRect(Rectangle rect) {
 		int x, y, slot = -2;
 		for (FTLRoom r : ship.rooms) {
 			if (r.getBounds().intersects(rect)) {
 				x = (rect.x - r.getBounds().x) / 35;
 				y = (rect.y - r.getBounds().y) / 35;
 				slot = r.getBounds().width / 35 * y + x;
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
 
 	// =================
 	// === DOOR RELATED
 
 	public static void removeUnalignedDoors() {
 		if (removeDoor && Main.ship != null) {
 			// can't iterate over ship.doors because it throws concurrentModification exception
 			// dump doors to an object array and iterate over it instead
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
 	 * @param rect
 	 *            Rectangle which matches the parameters of a wall;
 	 * @return FTLDoor at the given rect, if there is one. Null otherwise
 	 */
 	public static FTLDoor wallToDoor(Rectangle rect) {
 		for (FTLDoor dr : ship.doors) {
 			if (rect != null && rect.intersects(dr.getBounds()) && rect.width == dr.getBounds().width) {
 				return dr;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a door entity at given coordinates, if there is any. Null otherwise.
 	 */
 	public static FTLDoor getDoorAt(int x, int y) {
 		for (FTLDoor dr : ship.doors) {
 			if (dr.getBounds().contains(x, y)) {
 				return dr;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if given rectangle is positioned at the border of a room (at a wall)
 	 */
 	public static boolean isDoorAtWall(Rectangle rect) {
 		for (FTLRoom r : ship.rooms) {
 			if (rect != null && r != null && rect.intersects(r.getBounds()) && !containsRect(r.getBounds(), rect))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a rectangle with dimensions of a door at given coordinates, properly oriented.
 	 */
 	public static Rectangle getDoorRectAt(int x, int y) {
 		Rectangle dr = new Rectangle(0, 0, 0, 0);
 		Point p = new Point(x, y);
 		for (int i = 0; i < GRID_W; i++) {
 			for (int j = 0; j < GRID_H; j++) {
 				// horizontal
 				dr.x = i * 35 + 2;
 				dr.y = j * 35 - 3;
 				dr.width = 31;
 				dr.height = 6;
 				if (dr.contains(p))
 					return dr;
 
 				// vertical
 				dr.x = i * 35 - 3;
 				dr.y = j * 35 + 2;
 				dr.width = 6;
 				dr.height = 31;
 				if (dr.contains(p))
 					return dr;
 			}
 		}
 		return null;
 	}
 
 	public Rectangle getDoorFromMouse() {
 		return getDoorRectAt(mousePos.x, mousePos.y);
 	}
 
 	// =================
 	// === MOUNT RELATED
 
 	public static int getMountIndex(FTLMount m) {
 		int i = -1;
 		for (FTLMount mt : ship.mounts) {
 			i++;
 			if (mt == m)
 				break;
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
 
 	// =========================
 	// === AUXILIARY / LAZYNESS
 
 	public static void addEdit(AbstractUndoableEdit aue) {
 		undoManager.addEdit(aue);
 		updateUndoButtons();
 	}
 
 	public static void updateUndoButtons() {
 		mntmUndo.setText(undoManager.getUndoPresentationName() + "\tCtrl+Z");
 		mntmRedo.setText(undoManager.getRedoPresentationName() + "\tCtrl+Y");
 		mntmUndo.setEnabled(undoManager.canUndo());
 		mntmRedo.setEnabled(undoManager.canRedo());
 	}
 
 	public void clearButtonImg() {
 		btnHull.setImage(null);
 		btnShields.setImage(null);
 		btnFloor.setImage(null);
 		btnCloak.setImage(null);
 		btnMiniship.setImage(null);
 	}
 
 	public static void updateButtonImg() {
 		btnHull.setImage((ShipIO.isNull(ship.imagePath) ? crossImage : tickImage));
 		btnShields.setImage((ShipIO.isNull(ship.shieldPath) ? crossImage : tickImage));
 		btnFloor.setImage((ShipIO.isNull(ship.floorPath) ? crossImage : tickImage));
 		btnCloak.setImage((ShipIO.isNull(ship.cloakPath) ? crossImage : tickImage));
 		btnMiniship.setImage((ShipIO.isNull(ship.miniPath) ? crossImage : tickImage));
 	}
 
 	public static void updateSelectedPosText() {
 		boolean enable = (selectedMount != null && selectedMount.isSelected())
 				|| (selectedRoom != null && selectedRoom.isSelected())
 				|| (selectedGib != null && selectedGib.isSelected())
 				|| hullBox.isSelected() || shieldBox.isSelected();
 
 		txtX.setEnabled(enable);
 		btnXplus.setEnabled(enable);
 		btnXminus.setEnabled(enable);
 		txtY.setEnabled(enable);
 		btnYplus.setEnabled(enable);
 		btnYminus.setEnabled(enable);
 		stepSpinner.setEnabled(enable);
 
 		if (!enable) {
 			txtX.setText("");
 			txtY.setText("");
 		}
 
 		if (selectedMount != null) {
 			txtX.setText("" + (selectedMount.getBounds().x + selectedMount.getBounds().width / 2));
 			txtY.setText("" + (selectedMount.getBounds().y + selectedMount.getBounds().height / 2));
 		} else if (selectedDoor != null) {
 			txtX.setText("" + (selectedDoor.getBounds().x / 35 + 1));
 			txtY.setText("" + (selectedDoor.getBounds().y / 35 + 1));
 		} else if (selectedRoom != null) {
 			txtX.setText("" + (selectedRoom.getBounds().x / 35 + 1));
 			txtY.setText("" + (selectedRoom.getBounds().y / 35 + 1));
 		} else if (selectedGib != null) {
 			txtX.setText("" + (selectedGib.getBounds().x));
 			txtY.setText("" + (selectedGib.getBounds().y));
 		} else if (hullBox.isSelected()) {
 			txtX.setText("" + (ship.imageRect.x));
 			txtY.setText("" + (ship.imageRect.y));
 		} else if (shieldBox.isSelected()) {
 			txtX.setText("" + (shieldEllipse.x));
 			txtY.setText("" + (shieldEllipse.y));
 		}
 	}
 
 	public static void updateSelectedPosition() {
 		int x = 0, y = 0;
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
 				if (x >= GRID_W * 35)
 					x = GRID_W * 35 - 15;
 				if (y >= GRID_H * 35)
 					y = GRID_H * 35 - 15;
 				if (x <= -selectedMount.getBounds().width)
 					x = 15 - selectedMount.getBounds().width;
 				if (y <= -selectedMount.getBounds().height)
 					y = 15 - selectedMount.getBounds().height;
 
 				selectedMount.setLocation(x, y);
 			} else if (selectedDoor != null && (!selectedDoor.isPinned() || arbitraryPosOverride)) {
 				// selectedDoor.setLocation(x, y);
 			} else if (selectedRoom != null && (!selectedRoom.isPinned() || arbitraryPosOverride)) {
 				if (x > GRID_W - selectedRoom.getBounds().width / 35)
 					x = GRID_W - selectedRoom.getBounds().width / 35 + 1;
 				if (y > GRID_H - selectedRoom.getBounds().height / 35)
 					y = GRID_H - selectedRoom.getBounds().height / 35 + 1;
 				if (x <= ship.anchor.x / 35)
 					x = ship.anchor.x / 35 + 1;
 				if (y <= ship.anchor.y / 35)
 					y = ship.anchor.y / 35 + 1;
 
 				Rectangle collisionCheck = new Rectangle((x - 1) * 35, (y - 1) * 35, selectedRoom.getBounds().width, selectedRoom.getBounds().height);
 				if (!doesRectOverlap(collisionCheck, selectedRoom.getBounds())) {
 					x = (x - 1) * 35;
 					y = (y - 1) * 35;
 					selectedRoom.setLocation(x, y);
 					// updateCorners(selectedRoom);
 				}
 			} else if (selectedGib != null && (!selectedGib.isPinned() || arbitraryPosOverride)) {
 				if (x >= GRID_W * 35)
 					x = GRID_W * 35 - 15;
 				if (y >= GRID_H * 35)
 					y = GRID_H * 35 - 15;
 				if (x <= -selectedGib.getBounds().width)
 					x = 15 - selectedGib.getBounds().width;
 				if (y <= -selectedGib.getBounds().height)
 					y = 15 - selectedGib.getBounds().height;
 
 				selectedGib.setLocationAbsolute(x, y);
 			} else if (hullBox.isSelected() && (!hullBox.isPinned() || arbitraryPosOverride)) {
 				if (x >= GRID_W * 35)
 					x = GRID_W * 35 - 15;
 				if (y >= GRID_H * 35)
 					y = GRID_H * 35 - 15;
 				if (x <= -ship.imageRect.width)
 					x = 15 - ship.imageRect.width;
 				if (y <= -ship.imageRect.height)
 					y = 15 - ship.imageRect.height;
 
 				hullBox.setLocation(x, y);
 			} else if (shieldBox.isSelected() && (!shieldBox.isPinned() || arbitraryPosOverride)) {
 				if (x >= GRID_W * 35)
 					x = GRID_W * 35 - 15;
 				if (y >= GRID_H * 35)
 					y = GRID_H * 35 - 15;
 				if (x <= -shieldEllipse.width)
 					x = 15 - shieldEllipse.width;
 				if (y <= -shieldEllipse.height)
 					y = 15 - shieldEllipse.height;
 
 				shieldBox.setLocation(x, y);
 			}
 
 			canvas.redraw();
 		}
 	}
 
 	public static void enableMenus(boolean enable) {
 		mntmView.setEnabled(enable);
 		mntmEdit.setEnabled(enable);
 
 		mntmNewShip.setEnabled(enable);
 		mntmLoadShip.setEnabled(enable);
 		mntmLoadShipFTL.setEnabled(enable);
 		mntmLoadShipProject.setEnabled(enable);
 
 		mntmInclude.setEnabled(enable);
 	}
 
 	public void drawToolIcon(PaintEvent e, String name) {
 		e.gc.setAlpha(255);
 		e.gc.drawImage(toolsMap.get(name), 0, 0, 24, 24, mousePos.x + 10, mousePos.y + 10, 19, 20);
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
 
 	/** True for println, false for print */
 	public static void debug(Object msg, boolean ln) {
 		if (debug) {
 			if (ln) {
 				System.out.println("" + msg);
 			} else {
 				System.out.print("" + msg);
 			}
 		}
 	}
 
 	public static void debug(Object msg) {
 		if (debug)
 			System.out.println("" + msg);
 	}
 
 	public static void unpackFTL(String path) {
 		if (!ShipIO.isNull(path) && new File(path).exists()) {
 			ftlLoadPath = new String(path);
 			debug("Unpacking .ftl:", true);
 
 			temporaryFiles = new File("sprlmnl_tmp");
 			temporaryFiles.mkdirs();
 			temporaryFilesInUse = true;
 
 			if (temporaryFiles != null && temporaryFiles.exists()) {
 				debug("\tdeleting temporary directory... ", false);
 				ShipIO.deleteFolderContents(temporaryFiles);
 				if (temporaryFiles.exists())
 					ShipIO.rmdir(temporaryFiles);
 				debug("done", true);
 			}
 
 			ZipFile zf = null;
 			try {
 				zf = new ZipFile(path);
 
 				debug("\textracting contents of .ftl package to temporary directory... ", false);
 				ShipIO.unzipFileToDirectory(zf, temporaryFiles);
 				debug("done", true);
 			} catch (IOException ex) {
 			} finally {
 				if (zf != null)
 					try {
 						zf.close();
 					} catch (IOException ex) {
 					}
 			}
 		} else {
 			if (path != null) {
 				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
 				box.setMessage("" + Paths.get(path).getFileName() + ShipIO.lineDelimiter + "File was not found." + ShipIO.lineDelimiter + "Check the file's name and try again.");
 				box.open();
 			}
 		}
 	}
 
 	public static void deleteTemporary() {
 		if (!temporaryFilesInUse) {
 			if (temporaryFiles != null && temporaryFiles.exists()) {
 				debug("\tdeleting temporary directory... ", false);
 				ShipIO.deleteFolderContents(temporaryFiles);
 				if (temporaryFiles.exists())
 					ShipIO.rmdir(temporaryFiles);
 				debug("done", true);
 			}
 		} else {
 			print("Can't delete temporary files because they're still in use");
 		}
 	}
 
 	public static void updatePainter() {
 		anchor.setLocation(ship.anchor.x, ship.anchor.y, true);
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
 		p.x = p.x + (pt.x - p.x) / 2;
 		p.y = p.y + (pt.y - p.y) / 2;
 		pt = shieldBox.getLocation();
 		pt.x += shieldBox.getBounds().width / 2;
 		pt.y += shieldBox.getBounds().height / 2;
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
 			Object[] gibs = ship.gibs.toArray();
 			for (Object g : gibs)
 				((FTLGib) g).loadUnserializable();
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
 		if (selectedDoor != null)
 			selectedDoor.deselect();
 		if (selectedRoom != null)
 			selectedRoom.deselect();
 		if (selectedMount != null)
 			selectedMount.deselect();
 		if (selectedGib != null)
 			selectedGib.deselect();
 		if (hullBox.isSelected())
 			hullBox.deselect();
 		if (shieldBox.isSelected())
 			shieldBox.deselect();
 	}
 
 	private static void addListenerToGibItem(MenuItem mntm, FTLGib g) {
 		final int number = (g == null) ? 0 : g.number;
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
 		return path.substring(path.lastIndexOf(ShipIO.pathDelimiter) + 1) + ShipIO.pathDelimiter;
 	}
 
 	public static String getParent(String path) {
 		return path.substring(0, path.lastIndexOf(ShipIO.pathDelimiter)) + ShipIO.pathDelimiter;
 	}
 
 	public static float getAngle(int cx, int cy, int tx, int ty) {
 		float angle = (float) Math.toDegrees(Math.atan2(tx - cx, ty - cy));
 
 		if (angle < 0) {
 			angle += 360;
 		}
 
 		return angle;
 	}
 
 	public static float getAngle(Point center, Point target) {
 		return getAngle(center.x, center.y, target.x, target.y);
 	}
 
 	public void askSaveChoice() {
 		if (!savedSinceAction) {
 			MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
 			box.setText("Unsaved Changes");
 			box.setMessage("You have unsaved modifications. Save changes?");
 			askSaveChoice = box.open();
 			askedChoice = true;
 		}
 	}
 
 	/**
 	 * Hides the object and unregisters it from lists, painters and the like, but keeps it stored in memory
 	 * so that its deletion can be undone
 	 */
 	public static void deleteObject(PaintBox box) {
 		if (!(box instanceof FTLRoom || box instanceof FTLDoor || box instanceof FTLMount || box instanceof FTLGib)) {
 			throw new IllegalArgumentException("Not an instance of deletable object.");
 		}
 
 		Rectangle redrawBounds = null;
 		// unregister and hide
 		if (box instanceof FTLRoom) {
 			((FTLRoom) box).deselect();
 			ship.rooms.remove(box);
 			idList.remove(((FTLRoom) box).id);
 			recalculateShieldCenter();
 			layeredPainter.remove(box);
 
 			if (ship.rooms.size() == 0)
 				btnShields.setEnabled(false);
 
 			box.setVisible(false);
 			redrawBounds = box.getBounds();
 		} else if (box instanceof FTLDoor) {
 			((FTLDoor) box).deselect();
 			ship.doors.remove(box);
 			layeredPainter.remove(box);
 			box.setVisible(false);
 
 			redrawBounds = box.getBounds();
 		} else if (box instanceof FTLMount) {
 			((FTLMount) box).deselect();
 			ship.mounts.remove(box);
 			layeredPainter.remove(box);
 
 			redrawBounds = box.getBounds();
 			redrawBounds.x -= 40;
 			redrawBounds.y -= 40;
 			redrawBounds.width += 80;
 			redrawBounds.height += 80;
 		} else if (box instanceof FTLGib) {
 			FTLGib g = (FTLGib) box;
 			if (gibWindow.isVisible())
 				gibWindow.escape();
 			g.deselect();
 			gibDialog.removeGibFromList(g);
 			Main.ship.gibs.remove(g);
 			box.setVisible(false);
 			gibDialog.refreshList();
 			Main.canvas.redraw(g.getBounds().x - 1, g.getBounds().y - 1, g.getBounds().width + 2, g.getBounds().height + 2, false);
 		}
 
 		if (redrawBounds != null)
 			canvasRedraw(redrawBounds, true);
 	}
 
 	public static void recreateObject(PaintBox box) {
 		if (!(box instanceof FTLRoom || box instanceof FTLDoor || box instanceof FTLMount || box instanceof FTLGib)) {
 			throw new IllegalArgumentException("Not an instance of creatable object.");
 		}
 
 		Rectangle redrawBounds = null;
 		// register back and show
 		if (box instanceof FTLRoom) {
 			((FTLRoom) box).id = getLowestId();
 			((FTLRoom) box).add(ship);
 			((FTLRoom) box).assignSystem(((FTLRoom) box).sysBox);
 
 			box.setVisible(true);
 			redrawBounds = box.getBounds();
 		} else if (box instanceof FTLDoor) {
 			((FTLDoor) box).add(ship);
 
 			box.setVisible(true);
 			redrawBounds = box.getBounds();
 		} else if (box instanceof FTLMount) {
 			FTLMount m = (FTLMount) box;
 			if (isMountIndexTaken(m.index) || m.index > ship.mounts.size()) {
 				ship.mounts.add(m); // if the index is taken -- too bad (shouldn't really ever happen, tbh)
 			} else {
 				ship.mounts.add(m.index, m); // insert it at the same index it was previously
 			}
 			layeredPainter.add(box, LayeredPainter.MOUNT);
 			m.setRotated(m.isRotated());
 
 			ShipIO.loadWeaponImages(Main.ship);
 			ShipIO.remapMountsToWeapons();
 
 			box.setVisible(true);
 			redrawBounds = box.getBounds();
 			cursor.mount_canBePlaced = ship.mounts.size() < MAX_MOUNTS;
 		} else if (box instanceof FTLGib) {
 			FTLGib g = (FTLGib) box;
 			Main.ship.gibs.add(g.number - 1, g);
 			box.setVisible(true);
 			gibDialog.addGibToListAtIndex(g, g.number - 1);
 			gibDialog.refreshList();
 			Main.canvas.redraw(g.getBounds().x - 1, g.getBounds().y - 1, g.getBounds().width + 2, g.getBounds().height + 2, false);
 		}
 
 		if (redrawBounds != null)
 			canvasRedraw(redrawBounds, true);
 	}
 
 	/**
 	 * Destroys the object
 	 */
 	public static void reallyDeleteObject(PaintBox box) {
 		Rectangle redrawBounds = null;
 		if (box instanceof FTLRoom) {
 			FTLRoom r = (FTLRoom) box;
 
 			Point oldLow = null;
 			Point oldHigh = null;
 			if (Main.ship != null) {
 				oldLow = Main.ship.findLowBounds();
 				oldHigh = Main.ship.findHighBounds();
 				oldLow.x = oldLow.x + (oldHigh.x - oldLow.x) / 2;
 				oldLow.y = oldLow.y + (oldHigh.y - oldLow.y) / 2;
 			}
 
 			redrawBounds = r.getBounds();
 			r.dispose();
 			ship.rooms.remove(r);
 			removeUnalignedDoors();
 			ship.reassignID();
 			r = null;
 
 			if (Main.ship != null) {
 				Point p = Main.ship.findLowBounds();
 				Point pt = Main.ship.findHighBounds();
 				p.x = p.x + (pt.x - p.x) / 2;
 				p.y = p.y + (pt.y - p.y) / 2;
 
 				pt.x = p.x - oldLow.x;
 				pt.y = p.y - oldLow.y;
 
 				p = shieldBox.getLocation();
 				shieldBox.setLocation(p.x + pt.x, p.y + pt.y);
 			}
 
 			if (ship.rooms.size() == 0) {
 				btnShields.setEnabled(false);
 			}
 		} else if (box instanceof FTLDoor) {
 			FTLDoor d = (FTLDoor) box;
 			redrawBounds = d.getBounds();
 			d.dispose();
 			ship.doors.remove(d);
 			d = null;
 		} else if (box instanceof FTLMount) {
 			FTLMount m = (FTLMount) box;
 			redrawBounds = m.getBounds();
 
 			m.dispose();
 			ship.mounts.remove(m);
 			m = null;
 
 			redrawBounds.x -= 40;
 			redrawBounds.y -= 40;
 			redrawBounds.width += 80;
 			redrawBounds.height += 80;
 		} else if (box instanceof FTLGib) {
 			FTLGib g = (FTLGib) box;
 			if (gibWindow.isVisible())
 				gibWindow.escape();
 			g.deselect();
 			gibDialog.removeGibFromList(g);
 			g.dispose();
 			Main.ship.gibs.remove(g);
 			gibDialog.letters.remove(g.ID);
 			Main.canvas.redraw(g.getBounds().x - 1, g.getBounds().y - 1, g.getBounds().width + 2, g.getBounds().height + 2, false);
 		}
 
 		if (redrawBounds != null)
 			canvasRedraw(redrawBounds, false);
 	}
 
 	public static PaintBox getSelected() {
 		if (selectedRoom != null) {
 			return selectedRoom;
 		} else if (selectedDoor != null) {
 			return selectedDoor;
 		} else if (selectedMount != null) {
 			return selectedMount;
 		} else if (selectedGib != null) {
 			return selectedGib;
 		} else if (hullBox != null && hullBox.isSelected()) {
 			return hullBox;
 		} else if (shieldBox != null && shieldBox.isSelected()) {
 			return shieldBox;
 		}
 		return null;
 	}
 }

 package com.kartoflane.superluminal.core;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.nio.file.NoSuchFileException;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.InputMismatchException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.TreeItem;
 import com.kartoflane.superluminal.elements.ExportProgress;
 import com.kartoflane.superluminal.elements.FTLDoor;
 import com.kartoflane.superluminal.elements.FTLGib;
 import com.kartoflane.superluminal.elements.FTLItem;
 import com.kartoflane.superluminal.elements.FTLMount;
 import com.kartoflane.superluminal.elements.FTLRoom;
 import com.kartoflane.superluminal.elements.FTLShip;
 import com.kartoflane.superluminal.elements.Slide;
 import com.kartoflane.superluminal.elements.Systems;
 import com.kartoflane.superluminal.painter.Cache;
 import com.kartoflane.superluminal.painter.PaintBox;
 import com.kartoflane.superluminal.ui.IncludeAskDialog;
 import com.kartoflane.superluminal.ui.ShipBrowser;
 
 public class ShipIO {
 	public static Set<String> errors = new HashSet<String>();
 
 	public static Set<String> playerBlueprintNames = new TreeSet<String>();
 	public static Set<String> enemyBlueprintNames = new TreeSet<String>();
 	public static Set<String> otherBlueprintNames = new TreeSet<String>();
 	public static Map<String, String> playerShipNames = new HashMap<String, String>();
 	public static Map<String, String> enemyShipNames = new HashMap<String, String>();
 	public static Map<String, String> otherShipNames = new HashMap<String, String>();
 
 	public static Map<String, FTLItem> weaponMap = new HashMap<String, FTLItem>();
 	public static Map<String, FTLItem> droneMap = new HashMap<String, FTLItem>();
 	public static Map<String, FTLItem> augMap = new HashMap<String, FTLItem>();
 	public static Map<String, HashSet<String>> weaponSetMap = new HashMap<String, HashSet<String>>();
 	public static Map<String, HashSet<String>> droneSetMap = new HashMap<String, HashSet<String>>();
 
 	// variables used when loading a ship from .ftl package
 	public static Map<String, FTLItem> oldWeaponMap = new HashMap<String, FTLItem>();
 	public static Map<String, FTLItem> oldDroneMap = new HashMap<String, FTLItem>();
 	public static Map<String, FTLItem> oldAugMap = new HashMap<String, FTLItem>();
 	public static Map<String, HashSet<String>> oldWeaponSetMap = new HashMap<String, HashSet<String>>();
 	public static Map<String, HashSet<String>> oldDroneSetMap = new HashMap<String, HashSet<String>>();
 
 	public static Map<FTLMount, String> mountWeaponMap = new HashMap<FTLMount, String>();
 
 	public static FTLShip shipBeingLoaded;
 	public static String pathDelimiter = System.getProperty("file.separator");
 	public static String lineDelimiter = "\n";
 	static boolean namesFetched = false;
 
 	public static boolean createFtl = false;
 	public static boolean deleteTemp = false;
 	@Deprecated
 	public static boolean dontCheck = false;
 
 	public static boolean ignoreNextTag = false;
 	private static boolean negativeX = false;
 	private static boolean negativeY = false;
 
 	private static LinkedList<String> playerList = new LinkedList<String>();
 
 	public static boolean IOdebug = false;
 
 	// =================================================================
 
 	public static boolean isNull(String path) {
 		return path == null || (path != null && (path.equals("") || path.equals("null")));
 	}
 
 	public static void debug(String msg) {
 		if (IOdebug) {
 			System.out.println(msg);
 		}
 	}
 
 	// =======================
 	// === IMPORTING / LOADING
 
 	/**
 	 * Clears the tree list and updates it.
 	 */
 	public static void loadTree() {
 		TreeItem blueprint;
 
 		ShipBrowser.clearTrees();
 
 		final int FORMAT_CHARACTERS = 30;
 
 		if (ShipBrowser.sortByBlueprint) {
 			for (String s : playerBlueprintNames) {
 				blueprint = new TreeItem(ShipBrowser.trtmPlayer, SWT.NONE);
 				blueprint.setText(String.format("%-" + FORMAT_CHARACTERS + "s (%s)", playerShipNames.get(s), s));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 			for (String s : enemyBlueprintNames) {
 				blueprint = new TreeItem(ShipBrowser.trtmEnemy, SWT.NONE);
 				blueprint.setText(String.format("%-" + FORMAT_CHARACTERS + "s (%s)", enemyShipNames.get(s), s));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 			for (String s : otherBlueprintNames) {
 				blueprint = new TreeItem(ShipBrowser.trtmOther, SWT.NONE);
 				blueprint.setText(String.format("%-" + FORMAT_CHARACTERS + "s (%s)", otherShipNames.get(s), s));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 		} else {
 			HashMap<String, String> nameMap = new HashMap<String, String>();
 			TreeSet<String> nameSet = new TreeSet<String>();
 			for (String s : playerBlueprintNames) {
 				nameMap.put(playerShipNames.get(s), String.format("%-" + FORMAT_CHARACTERS + "s (%s)", playerShipNames.get(s), s));
 				nameSet.add(playerShipNames.get(s));
 			}
 			for (String name : nameSet) {
 				blueprint = new TreeItem(ShipBrowser.trtmPlayer, SWT.NONE);
 				blueprint.setText(nameMap.get(name));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 			nameMap.clear();
 			nameSet.clear();
 			for (String s : enemyBlueprintNames) {
 				nameMap.put(enemyShipNames.get(s), String.format("%-" + FORMAT_CHARACTERS + "s (%s)", enemyShipNames.get(s), s));
 				nameSet.add(enemyShipNames.get(s));
 			}
 			for (String name : nameSet) {
 				blueprint = new TreeItem(ShipBrowser.trtmEnemy, SWT.NONE);
 				blueprint.setText(nameMap.get(name));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 			nameMap.clear();
 			nameSet.clear();
 			for (String s : otherBlueprintNames) {
 				nameMap.put(otherShipNames.get(s), String.format("%-" + FORMAT_CHARACTERS + "s (%s)", otherShipNames.get(s), s));
 				nameSet.add(otherShipNames.get(s));
 			}
 			for (String name : nameSet) {
 				blueprint = new TreeItem(ShipBrowser.trtmOther, SWT.NONE);
 				blueprint.setText(nameMap.get(name));
 				blueprint.setFont(Main.monoFont);
 				ShipBrowser.ships.add(blueprint);
 			}
 		}
 	}
 
 	/**
 	 * Clears the blueprint and name maps, fills then anew and then reloads the tree list.
 	 */
 	public static void reloadBlueprints() {
 		playerBlueprintNames.clear();
 		enemyBlueprintNames.clear();
 		otherBlueprintNames.clear();
 		playerShipNames.clear();
 		enemyShipNames.clear();
 		otherShipNames.clear();
 
 		fetchShipNames();
 		loadTree();
 	}
 
 	/**
 	 * Since using the scanner class there's no way of knowing what lies ahead in the file, I have to do a pre-scan of the file and count number of
 	 * declarations for the given blueprint, so that I can then load the latest (modded default ships)
 	 * 
 	 * @param blueprintName
 	 * @return
 	 */
 	public static int preScan(String blueprintName, File blueprints) {
 		FileReader fr;
 		Scanner sc = null;
 		String s;
 		Pattern pattern;
 		Matcher matcher;
 		int blueprintCount = 0;
 
 		try {
 			fr = new FileReader(blueprints);
 			sc = new Scanner(fr);
 			sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 			pattern = Pattern.compile("<shipBlueprint\\s*name\\s*=\\s*\"(" + blueprintName + "\")(.*?)");
 
 			while (sc.hasNext()) {
 				s = sc.next();
 				matcher = pattern.matcher(s);
 				if (matcher.find())
 					blueprintCount++;
 			}
 		} catch (FileNotFoundException e) {
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 
 		return blueprintCount;
 	}
 
 	public static List<String> preScan(File fileToScan, String blueprint) {
 		String s;
 		Scanner sc = null;
 		FileReader fr;
 		Pattern pattern;
 		Matcher matcher;
 		List<String> blueList = new ArrayList<String>();
 
 		try {
 			fr = new FileReader(fileToScan);
 			sc = new Scanner(fr);
 			sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 			pattern = Pattern.compile("<shipBlueprint\\s*name\\s*=\\s*\"(" + blueprint + ")\".*?");
 
 			while (sc.hasNext()) {
 				s = sc.next();
 				matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					blueList.add(matcher.group(1));
 				}
 			}
 		} catch (FileNotFoundException e) {
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 		return blueList;
 	}
 
 	public static List<String> preScanFTL(File fileToScan) {
 		String s;
 		Scanner sc = null;
 		FileReader fr;
 		Pattern pattern;
 		Matcher matcher;
 		List<String> blueList = new ArrayList<String>();
 
 		try {
 			fr = new FileReader(fileToScan);
 			sc = new Scanner(fr);
 			sc.useDelimiter(Pattern.compile(lineDelimiter));
 			pattern = Pattern.compile("<shipBlueprint\\s*name\\s*=\\s*\"(.*?)\".*?");
 
 			while (sc.hasNext()) {
 				s = sc.next();
 				matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					blueList.add(matcher.group(1));
 				}
 			}
 		} catch (FileNotFoundException e) {
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 		return blueList;
 	}
 
 	/**
 	 * Loads the given ship.
 	 * 
 	 * @param blueprintName
 	 *            The blueprint name of the ship that is to be loaded.
 	 */
 	public static void loadShip(String blueprintName, File fileToScan, int blueprintToLoad) {
 		FileReader fr;
 		Scanner sc = null;
 		String s;
 		Pattern pattern;
 		Matcher matcher;
 		boolean ignoreNextTag = false;
 		FTLRoom r;
 		String sysName;
 		int level, power, id;
 		boolean systemList = false;
 		File f = null;
 
 		Main.undoManager.discardAllEdits();
 
 		boolean scanDefault = fileToScan == null;
 		if (scanDefault)
 			fileToScan = new File(Main.dataPath + pathDelimiter + "autoBlueprints.xml");
 
 		int declarationCount = blueprintToLoad;
 		int declarationsOmitted = 0;
 
 		if (fileToScan.getName().contains("autoBlueprints")) {
 			if (blueprintToLoad == -1)
 				declarationCount = preScan(blueprintName, fileToScan);
 			debug("Load ship - number of declarations for " + blueprintName + ": " + (declarationCount));
 			// scan autoBlueprints.xml for mention of the blueprintName, if there's one, load it.
 			try {
 				fr = new FileReader(fileToScan);
 				sc = new Scanner(fr);
 				sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 				Main.debug("Load ship - scanning " + fileToScan.getName() + " for " + blueprintName + "...", IOdebug);
 
 				ship: while (sc.hasNext()) {
 					s = sc.next();
 
 					ignoreNextTag = isCommentedOut(s);
 
 					pattern = Pattern.compile("(.*)(<shipBlueprint name\\s*?=\\s*?\")(.*?)(\".*)"); // (\\s*?layout\\s*?=\\s*?\")(.*?)(\"\\s*?img\\s*?=\\s*?\")(.*?)(\">)
 					matcher = pattern.matcher(s);
 
 					if (matcher.find() && matcher.group(3).equals(blueprintName)) {
 						if (declarationsOmitted < declarationCount) {
 							declarationsOmitted++;
 
 							if (!ignoreNextTag && declarationsOmitted == declarationCount) {
 								debug("\tfound");
 								debug("\tDeclarations skipped: " + (declarationsOmitted - 1));
 								shipBeingLoaded = new FTLShip();
 								shipBeingLoaded.blueprintName = blueprintName;
 								shipBeingLoaded.isPlayer = false;
 
 								// === LOAD SHIP LAYOUT
 								String temp = matcher.group(4);
 								pattern = Pattern.compile(".*?layout.*?=.*?\"(.*?)\".*");
 								matcher = pattern.matcher(temp);
 								if (matcher.find()) {
 									shipBeingLoaded.layout = matcher.group(1);
 								} else {
 									Main.erDialog.print("Error: load ship - ship blueprint has no layout declared. Aborting.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								if (!new File(fileToScan.getParent() + pathDelimiter + shipBeingLoaded.layout + ".txt").exists()
 										|| !new File(fileToScan.getParent() + pathDelimiter + shipBeingLoaded.layout + ".xml").exists()) {
 									Main.erDialog.add("Error: load ship - shipname.txt or shipname.xml was not found. Loading aborted.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								loadShipLayout(fileToScan.getParent());
 
 								// === LOAD SHIP IMAGE
 								String str;
 								pattern = Pattern.compile(".*?img\\s*?=\\s*?\"(.*?)\".*");
 								matcher = pattern.matcher(temp);
 								if (matcher.find()) {
 									shipBeingLoaded.imageName = matcher.group(1);
 								} else {
 									Main.erDialog.print("Error: load ship - ship blueprint has no image declared. Aborting.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								String parentPath = null;
 								if (scanDefault) {
 									parentPath = fileToScan.getParentFile().getParent() + (pathDelimiter + Main.resPath.substring(Main.resPath.lastIndexOf(pathDelimiter)));
 								} else {
 									parentPath = fileToScan.getParentFile().getParentFile().getAbsolutePath();
 								}
 
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_base.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.imagePath = str;
 								} else {
 									shipBeingLoaded.imagePath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_base.png";
 								}
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_cloak.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.cloakPath = str;
 								} else {
 									shipBeingLoaded.cloakPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_cloak.png";
 								}
 								str = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "enemy_shields.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.shieldPath = str;
 								} else {
 									shipBeingLoaded.shieldPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "enemy_shields.png";
 								}
 
 								while (sc.hasNext() && !s.contains("</shipBlueprint") && !s.contains("</ship")) {
 									s = sc.next();
 
 									pattern = Pattern.compile("(<class>)(.*?)(</class>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find())
 										shipBeingLoaded.shipClass = matcher.group(2);
 
 									pattern = Pattern.compile("(<minSector>)(.*?)(</minSector>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.minSec = Integer.valueOf(matcher.group(2));
 									} else {
 										shipBeingLoaded.minSec = 1;
 									}
 
 									pattern = Pattern.compile("(<maxSector>)(.*?)(</maxSector>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.maxSec = Integer.valueOf(matcher.group(2));
 									} else {
 										shipBeingLoaded.maxSec = 8;
 									}
 
 									pattern = Pattern.compile("(<systemList>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										while (sc.hasNext() && !s.contains("</systemList>")) {
 											s = sc.next();
 
 											ignoreNextTag = isCommentedOut(s);
 
 											if (!ignoreNextTag) {
 												pattern = Pattern.compile("(.*?<)(.*?)(\\s*?power\\s*?=\\s*?\")(.*?)(\"\\s*?room\\s*?=\\s*?\")(.*?)(\")(.*?>)"); // max\\s*?=\\s*?\")(.*?)(\\
 												matcher = pattern.matcher(s);
 												if (matcher.find()) {
 													power = 0;
 													level = 0;
 
 													id = Integer.valueOf(matcher.group(6));
 													sysName = matcher.group(2).toUpperCase();
 
 													str = matcher.group(8);
 
 													try {
 														power = Integer.valueOf(matcher.group(4));
 														level = power;
 													} catch (NumberFormatException e) {
 														pattern = Pattern.compile("(\\d*)(\"\\s*?max\\s*?=\\s*?\")(\\d*)(.*?)");
 														matcher = pattern.matcher(matcher.group(4));
 														if (matcher.find()) {
 															power = Integer.valueOf(matcher.group(1));
 															level = Integer.valueOf(matcher.group(3));
 														}
 													}
 
 													boolean start = true;
 													pattern = Pattern.compile("(.*?)(\\s*?start\\s*?=\\s*?\")(.*?)(\".*?)");
 													matcher = pattern.matcher(str);
 													if (matcher.find()) {
 														start = Boolean.valueOf(matcher.group(3));
 													}
 
 													r = shipBeingLoaded.getRoomWithId(id);
 													if (r != null) {
 														try {
 															r.assignSystem(Systems.valueOf(sysName));
 
 															shipBeingLoaded.levelMap.put(r.getSystem(), level);
 															shipBeingLoaded.powerMap.put(r.getSystem(), power);
 															shipBeingLoaded.startMap.put(r.getSystem(), start);
 															Main.systemsMap.get(r.getSystem()).setAvailable(shipBeingLoaded.startMap.get(r.getSystem()));
 														} catch (IllegalArgumentException e) {
 															Main.erDialog.add("Warning: tried to load a non-existing system [" + sysName + "]");
 														}
 
 														if (!str.contains("/>")) {
 															s = sc.next(); // <slot>
 															s = sc.next(); // <direction>
 															pattern = Pattern.compile("(.*?)(<direction>)(.*?)(</direction>)");
 															matcher = pattern.matcher(s);
 															if (matcher.find()) {
 																shipBeingLoaded.slotDirMap.put(r.getSystem(), Slide.valueOf(matcher.group(3).toUpperCase()));
 																s = sc.next(); // <number>
 															}
 															pattern = Pattern.compile("(.*?)(<number>)(.*?)(</number>)");
 															matcher = pattern.matcher(s);
 															if (matcher.find()) {
 																shipBeingLoaded.slotMap.put(r.getSystem(), Integer.valueOf(matcher.group(3)));
 															}
 														} else if (r.getSystem().equals(Systems.WEAPONS) || r.getSystem().equals(Systems.SHIELDS) || r.getSystem().equals(Systems.ENGINES) || r.getSystem().equals(Systems.PILOT) || r.getSystem().equals(Systems.MEDBAY)) {
 															// get defaults
 															shipBeingLoaded.slotMap.put(r.getSystem(), FTLRoom.getDefaultSlot(r.getSystem()));
 															shipBeingLoaded.slotDirMap.put(r.getSystem(), FTLRoom.getDefaultDir(r.getSystem()));
 														}
 													} else {
 														Main.erDialog.add("Error: load ship - rooms with specified ID not found. Some systems' data may be missing.");
 													}
 												}
 											}
 										}
 									}
 
 									// === armaments declaration type 1
 									// weapons list
 									pattern = Pattern.compile("(<weaponList missiles\\s*?=\\s*?\")(.*?)(\"\\s*?load\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.weaponsBySet = true;
 
 										shipBeingLoaded.weaponSet.add(matcher.group(4));
 										shipBeingLoaded.missiles = Integer.valueOf(matcher.group(2));
 
 										// default values
 										shipBeingLoaded.weaponCount = 4;
 										shipBeingLoaded.weaponSlots = 4;
 									}
 
 									// drone list
 									pattern = Pattern.compile("(<droneList drones\\s*?=\\s*?\")(.*?)(\"\\s*?load\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.dronesBySet = true;
 
 										shipBeingLoaded.droneSet.add(matcher.group(4));
 										shipBeingLoaded.drones = Integer.valueOf(matcher.group(2));
 
 										// default values
 										shipBeingLoaded.droneCount = 3;
 										shipBeingLoaded.droneSlots = 3;
 									}
 
 									// === armaments declaration type 2
 									// weapons list
 									pattern = Pattern.compile("(<weaponList count\\s*?=\\s*?\")(.*?)(\"\\s*?missiles\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.weaponsBySet = false;
 
 										shipBeingLoaded.missiles = Integer.valueOf(matcher.group(4));
 										shipBeingLoaded.weaponCount = Integer.valueOf(matcher.group(2));
 										while (sc.hasNext() && !s.contains("</weaponList>")) {
 											s = sc.next();
 
 											ignoreNextTag = isCommentedOut(s);
 
 											pattern = Pattern.compile("(<weapon name\\s*?=\\s*?\")(.*?)\".*?");
 											matcher = pattern.matcher(s);
 											if (matcher.find() && !ignoreNextTag) {
 												shipBeingLoaded.weaponSet.add(matcher.group(2));
 											}
 										}
 									}
 
 									// drone list
 									pattern = Pattern.compile("(<droneList count\\s*?=\\s*?\")(.*?)(\" drones\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.dronesBySet = false;
 
 										shipBeingLoaded.drones = Integer.valueOf(matcher.group(4));
 										shipBeingLoaded.droneCount = Integer.valueOf(matcher.group(2));
 										while (sc.hasNext() && !s.contains("</droneList>")) {
 											s = sc.next();
 
 											ignoreNextTag = isCommentedOut(s);
 
 											pattern = Pattern.compile("(<drone name\\s*?=\\s*?\")(.*?)\".*?");
 											matcher = pattern.matcher(s);
 											if (matcher.find() && !ignoreNextTag) {
 												shipBeingLoaded.droneSet.add(matcher.group(2));
 											}
 										}
 									}
 
 									// slots
 									pattern = Pattern.compile("(<weaponSlots>)(.*?)(</weaponSlots>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find())
 										shipBeingLoaded.weaponSlots = Integer.valueOf(matcher.group(2));
 
 									pattern = Pattern.compile("(<droneSlots>)(.*?)(</droneSlots>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find())
 										shipBeingLoaded.droneSlots = Integer.valueOf(matcher.group(2));
 
 									// hull health
 									pattern = Pattern.compile("(<health amount\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.hullHealth = Integer.valueOf(matcher.group(2));
 									}
 
 									// power
 									pattern = Pattern.compile("(<maxPower amount\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.reactorPower = Integer.valueOf(matcher.group(2));
 									}
 
 									// crew
 									pattern = Pattern.compile("(<crewCount amount\\s*?=\\s*?\")(.*?)(\"\\s*?max\\s*?=\\s*?\")(.*?)(\"\\s*?class\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.crewMap.put(matcher.group(6), Integer.valueOf(matcher.group(2)));
 										shipBeingLoaded.crewMaxMap.put(matcher.group(6), Integer.valueOf(matcher.group(4)));
 									} else {
 										pattern = Pattern.compile("(<crewCount amount\\s*?=\\s*?\")(.*?)(\"\\s*?class\\s*?=\\s*?\")(.*?)\".*?");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.crewMap.put(matcher.group(4), Integer.valueOf(matcher.group(2)));
 											shipBeingLoaded.crewMaxMap.put(matcher.group(4), Integer.valueOf(matcher.group(2)));
 										} else {
 											pattern = Pattern.compile("(<crewCount amount\\s*?=\\s*?\")(.*?)\".*?");
 											matcher = pattern.matcher(s);
 											if (matcher.find()) {
 												shipBeingLoaded.crewMap.put("random", Integer.valueOf(matcher.group(2)));
 												shipBeingLoaded.crewMaxMap.put("random", Integer.valueOf(matcher.group(2)));
 											}
 										}
 									}
 
 									// augments
 									pattern = Pattern.compile("(<aug name\\s*?=\\s*?\")(.*?)\".*?");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										shipBeingLoaded.augmentSet.add(matcher.group(2));
 									}
 									// overrides
 									pattern = Pattern.compile("(<shieldImage>)(.*?)(</shieldImage>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										// shipBeingLoaded.shieldOverride = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_shields1.png";
 										shipBeingLoaded.shieldPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_shields1.png";
 									}
 									pattern = Pattern.compile("(<cloakImage>)(.*?)(</cloakImage>)");
 									matcher = pattern.matcher(s);
 									if (matcher.find()) {
 										// shipBeingLoaded.cloakOverride = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_cloak.png";
 										shipBeingLoaded.cloakPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_cloak.png";
 									}
 
 									// TODO boarding AI ?
 								}
 								break ship;
 							} else if (!ignoreNextTag) {
 								debug("Load ship - skipped a declaration.");
 							}
 						}
 					}
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: load ship - " + fileToScan.getName() + " file not found [" + fileToScan.getAbsolutePath() + "]");
 			} catch (NoSuchElementException e) {
 				Main.erDialog.add("Error: load ship - end of file reached - probably does not contain valid ship blueprints.");
 			} finally {
 				if (sc != null)
 					sc.close();
 			}
 
 			if (!IOdebug)
 				Main.debug(" done", true);
 		}
 
 		String old = (scanDefault) ? "autoBlueprints.xml" : fileToScan.getName();
 		if (scanDefault)
 			fileToScan = new File(Main.dataPath + pathDelimiter + "blueprints.xml");
 
 		// if no matching blueprint name was found in the first file, search the blueprints.xml
 		if (shipBeingLoaded == null && fileToScan.getName().contains("blueprints")) {
 			Main.debug("Load ship - no " + blueprintName + " ship tag found in " + old, true);
 
 			if (blueprintToLoad == -1)
 				declarationCount = preScan(blueprintName, fileToScan);
 			declarationsOmitted = 0;
 
 			debug("Load ship - number of declarations for " + blueprintName + ": " + (declarationCount));
 
 			try {
 				fr = new FileReader(fileToScan);
 				sc = new Scanner(fr);
 				sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 				Main.debug("Load ship - scanning " + fileToScan.getName() + " for " + blueprintName + "...", IOdebug);
 
 				pattern = Pattern.compile("(.*?)(<shipBlueprint name\\s*?=\\s*?\")(" + blueprintName + ")(\".*)"); // (\\s*?layout\\s*?=\\s*?\")(.*?)(\"\\s*?img\\s*?=\\s*?\")(.*?)(\">)
 				// ("(<shipBlueprint name\\s*?=\\s*?\")(.*?)(\"\\s*?layout\\s*?=\\s*?\")(.*?)(\"\\s*?img\\s*?=\\s*?\")(.*?)(\">)");
 				scan: while (sc.hasNext()) {
 					s = sc.next();
 					ignoreNextTag = isCommentedOut(s);
 
 					matcher = pattern.matcher(s);
 					// load section names
 					if (matcher.find()) {
 						if (declarationsOmitted < declarationCount) {
 							declarationsOmitted++;
 
 							if (!ignoreNextTag && declarationsOmitted == declarationCount) {
 								debug("\tfound");
 								debug("\tDeclarations skipped: " + (declarationsOmitted - 1));
 								shipBeingLoaded = new FTLShip();
 								shipBeingLoaded.blueprintName = blueprintName;
 								shipBeingLoaded.isPlayer = true;
 
 								// === LOAD SHIP LAYOUT
 								String temp = matcher.group(4);
 								pattern = Pattern.compile(".*?layout.*?=.*?\"(.*?)\".*");
 								matcher = pattern.matcher(temp);
 								if (matcher.find()) {
 									shipBeingLoaded.layout = matcher.group(1);
 								} else {
 									Main.erDialog.print("Error: load ship - ship blueprint has no layout declared. Aborting.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								if (!new File(fileToScan.getParent() + pathDelimiter + shipBeingLoaded.layout + ".txt").exists()
 										|| !new File(fileToScan.getParent() + pathDelimiter + shipBeingLoaded.layout + ".xml").exists()) {
 									Main.erDialog.add("Error: load ship - shipname.txt or shipname.xml was not found. Loading aborted.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								loadShipLayout(fileToScan.getParent());
 
 								// === LOAD SHIP IMAGES
 								String str;
 								pattern = Pattern.compile(".*?img\\s*?=\\s*?\"(.*?)\".*");
 								matcher = pattern.matcher(temp);
 								if (matcher.find()) {
 									shipBeingLoaded.imageName = matcher.group(1);
 								} else {
 									Main.erDialog.print("Error: load ship - ship blueprint has no image declared. Aborting.");
 									shipBeingLoaded = null;
 									sc.close();
 									return;
 								}
 
 								String parentPath = null;
 								if (scanDefault) {
 									parentPath = fileToScan.getParentFile().getParent() + (pathDelimiter + Main.resPath.substring(Main.resPath.lastIndexOf(pathDelimiter)));
 								} else {
 									parentPath = fileToScan.getParentFile().getParentFile().getAbsolutePath();
 								}
 
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_base.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.imagePath = str;
 								} else {
 									shipBeingLoaded.imagePath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_base.png";
 								}
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_floor.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.floorPath = str;
 								} else {
 									shipBeingLoaded.floorPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_floor.png";
 								}
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "customizeUI" + pathDelimiter + "miniship_" + shipBeingLoaded.imageName + ".png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.miniPath = str;
 								} else {
 									shipBeingLoaded.miniPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "customizeUI" + pathDelimiter + "miniship_" + shipBeingLoaded.imageName + ".png";
 								}
 
 								str = shipBeingLoaded.imageName;
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + str + "_shields1.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.shieldPath = str;
 								} else {
 									shipBeingLoaded.shieldPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_shields1.png";
 								}
 
 								str = shipBeingLoaded.imageName;
 								str = parentPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + str + "_cloak.png";
 								f = new File(str);
 								if (f.exists()) {
 									shipBeingLoaded.cloakPath = str;
 								} else {
 									shipBeingLoaded.cloakPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + shipBeingLoaded.imageName + "_cloak.png";
 								}
 
 								while (sc.hasNext() && !s.contains("</shipBlueprint>")) {
 									s = sc.next();
 
 									ignoreNextTag = isCommentedOut(s);
 
 									if (!ignoreNextTag) {
 										if (isNull(shipBeingLoaded.shipClass)) {
 											pattern = Pattern.compile(".*?(<class>)(.*?)(</class>.*?)");
 											matcher = pattern.matcher(s);
 											if (matcher.find())
 												shipBeingLoaded.shipClass = matcher.group(2);
 										}
 										if (isNull(shipBeingLoaded.shipName)) {
 											pattern = Pattern.compile(".*?(<name>)(.*?)(</name>)");
 											matcher = pattern.matcher(s);
 											if (matcher.find())
 												shipBeingLoaded.shipName = matcher.group(2);
 										}
 										if (isNull(shipBeingLoaded.descr)) {
 											pattern = Pattern.compile(".*?(<desc>)(.*?)(</desc>)");
 											matcher = pattern.matcher(s);
 											if (matcher.find())
 												shipBeingLoaded.descr = matcher.group(2);
 										}
 										if (!systemList && s.contains("<systemList>")) {
 											systemList = true;
 
 											debug("\tloading systems...");
 
 											while (sc.hasNext() && !s.contains("</systemList>")) {
 												s = sc.next();
 
 												ignoreNextTag = isCommentedOut(s);
 
 												if (!s.contains("artillery") && !ignoreNextTag) {
 													pattern = Pattern.compile("(.*?)(<)(.*?)(\\s*?power\\s*?=\\s*?\")(.*?)(\"\\s*?room\\s*?=\\s*?\")(.*?)(\"\\s*?start\\s*?=\\s*?\")(.*?)(\")(.*?)(>|/>)");
 													matcher = pattern.matcher(s);
 													if (matcher.find() && !matcher.group(3).contains("!--")) {
 														debug("\t\tfound " + matcher.group(3));
 														debug("\t\t\tlooking for room ID: " + matcher.group(7) + "...");
 														r = shipBeingLoaded.getRoomWithId(Integer.valueOf(matcher.group(7)));
 
 														if (r != null) {
 															debug("\t\t\tfound");
 
 															try {
 																r.assignSystem(Systems.valueOf(matcher.group(3).toUpperCase()));
 
 																shipBeingLoaded.levelMap.put(r.getSystem(), Integer.valueOf(matcher.group(5)));
 																shipBeingLoaded.powerMap.put(r.getSystem(), Integer.valueOf(matcher.group(5)));
 																shipBeingLoaded.startMap.put(r.getSystem(), Boolean.valueOf(matcher.group(9)));
 																Main.systemsMap.get(r.getSystem()).setAvailable(shipBeingLoaded.startMap.get(r.getSystem()));
 
 																str = matcher.group(12);
 																s = matcher.group(11);
 																pattern = Pattern.compile("(\\s*?img\\s*?=\\s*?\")(.*?)(\")");
 																matcher = pattern.matcher(s);
 																if (matcher.find()) {
 																	if (r.sysBox != null) {
 																		r.interiorData.interiorPath = matcher.group(2);
 
 																		if (fileToScan != null) {
 																			r.interiorData.interiorPath = fileToScan.getParentFile().getParentFile().getAbsolutePath() + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + r.interiorData.interiorPath + ".png";
 																			// attempt to load glow images
 																			File glow = null;
 																			if (!r.getSystem().equals(Systems.CLOAKING)) {
 																				for (int i = 1; i <= 3; i++) {
 																					glow = new File(fileToScan.getParentFile().getParentFile().getAbsolutePath() + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + matcher.group(2) + "_glow" + i + ".png");
 																					if (glow.exists())
 																						r.sysBox.setGlowImage(glow.getAbsolutePath(), i);
 																				}
 																			} else {
 																				glow = new File(fileToScan.getParentFile().getParentFile().getAbsolutePath() + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + matcher.group(2) + "_glow1" + ".png");
 																				if (glow.exists())
 																					r.sysBox.setGlowImage(glow.getAbsolutePath(), 1);
 																			}
 																		} else {
 																			r.interiorData.interiorPath = "skip.loading";
 																		}
 																		f = new File(r.interiorData.interiorPath);
 																		if (!f.exists()) {
 																			r.interiorData.interiorPath = matcher.group(2);
 																			r.interiorData.interiorPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + r.interiorData.interiorPath + ".png";
 
 																			// attempt to load glow images
 																			File glow = null;
 																			if (!r.getSystem().equals(Systems.CLOAKING)) {
 																				for (int i = 1; i <= 3; i++) {
 																					glow = new File(Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + matcher.group(2) + "_glow" + i + ".png");
 																					if (glow.exists())
 																						r.sysBox.setGlowImage(glow.getAbsolutePath(), i);
 																				}
 																			} else {
 																				glow = new File(Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + matcher.group(2) + "_glow1" + ".png");
 																				if (glow.exists())
 																					r.sysBox.setGlowImage(glow.getAbsolutePath(), 1);
 																			}
 																		}
 
 																		if (Main.loadSystem) {
 																			r.setInterior(r.interiorData.interiorPath);
 																			// Main.erDialog.add("Error: load interior images - interior image not found.");
 																		}
 																	}
 																} else if (!r.getSystem().equals(Systems.TELEPORTER) && !r.getSystem().equals(Systems.EMPTY)) {
 																	// load default sysImg for the room (teleporter doesn't have default graphic)
 																	if (Main.loadSystem && r.sysBox != null) {
 																		r.interiorData.interiorPath = "room_" + r.getSystem().toString().toLowerCase();
 																		r.interiorData.interiorPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + r.interiorData.interiorPath + ".png";
 																		r.setInterior(r.interiorData.interiorPath);
 
 																		// attempt to load glow images
 																		File glow = null;
 																		if (!r.getSystem().equals(Systems.CLOAKING)) {
 																			for (int i = 1; i <= 3; i++) {
 																				glow = new File(Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + "room_" + r.getSystem().toString().toLowerCase() + "_glow" + i + ".png");
 																				if (glow.exists())
 																					r.sysBox.setGlowImage(glow.getAbsolutePath(), i);
 																			}
 																		} else {
 																			glow = new File(Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + "room_" + r.getSystem().toString().toLowerCase() + "_glow1" + ".png");
 																			if (glow.exists())
 																				r.sysBox.setGlowImage(glow.getAbsolutePath(), 1);
 																		}
 																	}
 																}
 
 																if (str.equals(">")) {
 																	s = sc.next(); // <slot>
 																	s = sc.next(); // <direction>
 																	pattern = Pattern.compile("(.*?)(<direction>)(.*?)(</direction>)");
 																	matcher = pattern.matcher(s);
 																	if (matcher.find()) {
 																		shipBeingLoaded.slotDirMap.put(r.getSystem(), Slide.valueOf(matcher.group(3).toUpperCase()));
 																		s = sc.next(); // <number>
 																	}
 																	pattern = Pattern.compile("(.*?)(<number>)(.*?)(</number>)");
 																	matcher = pattern.matcher(s);
 																	if (matcher.find()) {
 																		shipBeingLoaded.slotMap.put(r.getSystem(), Integer.valueOf(matcher.group(3)));
 																	}
 																} else if (r.getSystem().equals(Systems.WEAPONS) || r.getSystem().equals(Systems.SHIELDS) || r.getSystem().equals(Systems.ENGINES) || r.getSystem().equals(Systems.PILOT) || r.getSystem().equals(Systems.MEDBAY)) {
 																	// get defaults
 																	shipBeingLoaded.slotMap.put(r.getSystem(), FTLRoom.getDefaultSlot(r.getSystem()));
 																	shipBeingLoaded.slotDirMap.put(r.getSystem(), FTLRoom.getDefaultDir(r.getSystem()));
 																}
 															} catch (IllegalArgumentException e) {
 																Main.erDialog.add("Error: tried to load \"" + matcher.group(3) + "\" as system.");
 															} catch (NullPointerException e) {
 																// Main.erDialog.add("Error: tried to assign system " + matcher.group(3) + " to room ID: " + Integer.valueOf(matcher.group(7)) +
 																// ", but no such room was found.");
 															}
 														} else {
 															debug("\t\t\tnot found");
 														}
 													}
 												} else if (!ignoreNextTag) {
 													pattern = Pattern.compile("(.*?)(<)(.*?)(\\s*?power\\s*?=\\s*?\")(.*?)(\"\\s*?room\\s*?=\\s*?\")(.*?)(\")(.*?)(>|/>)");
 													matcher = pattern.matcher(s);
 													if (matcher.find()) {
 														debug("\t\tfound " + matcher.group(3));
 														debug("\t\t\tlooking for room ID: " + matcher.group(7) + "...");
 														r = shipBeingLoaded.getRoomWithId(Integer.valueOf(matcher.group(7)));
 
 														if (r != null) {
 															debug("\t\t\tfound");
 														} else {
 															debug("\t\t\tnot found");
 														}
 
 														r.assignSystem(Systems.valueOf(matcher.group(3).toUpperCase()));
 
 														shipBeingLoaded.levelMap.put(r.getSystem(), Integer.valueOf(matcher.group(5)));
 														shipBeingLoaded.powerMap.put(r.getSystem(), Integer.valueOf(matcher.group(5)));
 													}
 												}
 											}
 
 											debug("\tdone");
 										}
 
 										// weapons list
 										pattern = Pattern.compile("(<weaponList count\\s*?=\\s*?\")(.*?)(\" missiles\\s*?=\\s*?\")(.*?)(\"\\s*?>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.weaponsBySet = false;
 
 											shipBeingLoaded.missiles = Integer.valueOf(matcher.group(4));
 											shipBeingLoaded.weaponCount = Integer.valueOf(matcher.group(2));
 											while (sc.hasNext() && !s.contains("</weaponList>")) {
 												s = sc.next();
 
 												ignoreNextTag = isCommentedOut(s);
 
 												pattern = Pattern.compile("(<weapon name\\s*?=\\s*?\")(.*?)\".*?");
 												matcher = pattern.matcher(s);
 												if (matcher.find() && !ignoreNextTag) {
 													shipBeingLoaded.weaponSet.add(matcher.group(2));
 												}
 											}
 										}
 
 										// drone list
 										pattern = Pattern.compile("(<droneList count\\s*?=\\s*?\")(.*?)(\" drones\\s*?=\\s*?\")(.*?)(\"\\s*?>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.dronesBySet = false;
 
 											shipBeingLoaded.drones = Integer.valueOf(matcher.group(4));
 											shipBeingLoaded.droneCount = Integer.valueOf(matcher.group(2));
 											while (sc.hasNext() && !s.contains("</droneList>")) {
 												s = sc.next();
 
 												ignoreNextTag = isCommentedOut(s);
 
 												pattern = Pattern.compile("(<drone name\\s*?=\\s*?\")(.*?)\".*?");
 												matcher = pattern.matcher(s);
 												if (matcher.find() && !ignoreNextTag) {
 													shipBeingLoaded.droneSet.add(matcher.group(2));
 												}
 											}
 										}
 
 										// slots
 										pattern = Pattern.compile("(<weaponSlots>)(.*?)(</weaponSlots>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find())
 											shipBeingLoaded.weaponSlots = Integer.valueOf(matcher.group(2));
 
 										pattern = Pattern.compile("(<droneSlots>)(.*?)(</droneSlots>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find())
 											shipBeingLoaded.droneSlots = Integer.valueOf(matcher.group(2));
 
 										// hull health
 										pattern = Pattern.compile("(<health amount\\s*?=\\s*?\")(.*?)\".*?");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.hullHealth = Integer.valueOf(matcher.group(2));
 										}
 
 										// power
 										pattern = Pattern.compile("(<maxPower amount\\s*?=\\s*?\")(.*?)\".*?");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.reactorPower = Integer.valueOf(matcher.group(2));
 										}
 
 										// crew
 										pattern = Pattern.compile(".*?(<crewCount amount\\s*?=\\s*?\")(.*?)(\" class=\")(.*?)(\".*?)");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.crewMap.put(matcher.group(4), Integer.valueOf(matcher.group(2)));
 										} else {
 											pattern = Pattern.compile("(<crewCount amount\\s*?=\\s*?\")(.*?)\".*?");
 											matcher = pattern.matcher(s);
 											if (matcher.find()) {
 												shipBeingLoaded.crewMap.put("random", Integer.valueOf(matcher.group(2)));
 											}
 										}
 
 										// augments
 										pattern = Pattern.compile("(<aug name\\s*?=\\s*?\")(.*?)\".*?");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											shipBeingLoaded.augmentSet.add(matcher.group(2));
 										}
 
 										// overrides
 										pattern = Pattern.compile("(<shieldImage>)(.*?)(</shieldImage>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											// shipBeingLoaded.shieldOverride = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_shields1.png";
 											shipBeingLoaded.shieldPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_shields1.png";
 										}
 										pattern = Pattern.compile("(<cloakImage>)(.*?)(</cloakImage>)");
 										matcher = pattern.matcher(s);
 										if (matcher.find()) {
 											// shipBeingLoaded.cloakOverride = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_cloak.png";
 											shipBeingLoaded.cloakPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + matcher.group(2) + "_cloak.png";
 										}
 									}
 								}
 								break scan;
 							} else if (!ignoreNextTag) {
 								debug("Load ship - skipped a declaration.");
 							}
 						}
 					}
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: load ship - " + fileToScan.getName() + " file not found [" + fileToScan.getAbsolutePath() + "]");
 			} catch (NoSuchElementException e) {
 				Main.erDialog.add("Error: load ship - end of file reached - probably does not contain valid ship blueprints.");
 			} finally {
 				if (sc != null)
 					sc.close();
 			}
 
 			if (!IOdebug)
 				Main.debug(" done", true);
 		}
 
 		if (shipBeingLoaded != null) {
 			Main.ship = shipBeingLoaded;
 			loadShipImages(Main.ship, fileToScan);
 
 			for (FTLMount m : Main.ship.mounts)
 				m.setLocation(Main.ship.imageRect.x + m.pos.x, Main.ship.imageRect.y + m.pos.y);
 
 			if (!Main.ship.weaponsBySet) {
 				remapMountsToWeapons();
 				loadWeaponImages(Main.ship);
 			}
 
 			Main.updatePainter();
 
 			Point p = shipBeingLoaded.computeShipSize();
 			p = new Point(35 * Math.round((Main.GRID_W * 35 - p.x) / 70 - shipBeingLoaded.offset.x), 35 * Math.round((Main.GRID_H * 35 - p.y) / 70 - shipBeingLoaded.offset.y));
 			p.x = p.x < 0 ? 0 : p.x;
 			p.y = p.y < 0 ? 0 : p.y;
 			Main.anchor.setLocation(p.x, p.y);
 
 			if (negativeX || negativeY) {
 				p = Main.ship.findLowBounds();
 
 				int dx = p.x - Main.ship.anchor.x;
 				int dy = p.y - Main.ship.anchor.y;
 				for (FTLRoom rm : Main.ship.rooms) {
 					Point pt = rm.getLocation();
 					rm.setLocationAbsolute(pt.x - dx, pt.y - dy);
 				}
 				for (FTLDoor dr : Main.ship.doors) {
 					Point pt = dr.getLocation();
 					dr.setLocationAbsolute(pt.x - dx, pt.y - dy);
 				}
 			}
 
 			shipBeingLoaded = null;
 			Main.debug("Load ship - " + ((Main.ship.shipName != null) ? (Main.ship.shipClass + " \"" + Main.ship.shipName + "\"") : (Main.ship.shipClass)) + " [" + Main.ship.blueprintName + "]" + " loaded successfully.", true);
 		} else {
 			Main.debug("Load ship - no " + blueprintName + " ship tag found in autoBlueprints.xml or blueprints.xml. Loading failed.", true);
 		}
 	}
 
 	public static boolean isCommentedOut(String input) {
 		// ^ - start, $ - end, \A - start of string, \Z - end of string
 		Pattern pattern;
 		Matcher matcher;
 
 		pattern = Pattern.compile("^*?<!--.*?");
 		matcher = pattern.matcher(input);
 		if (matcher.find())
 			ignoreNextTag = true;
 
 		pattern = Pattern.compile(".*?-->");
 		matcher = pattern.matcher(input);
 		if (matcher.find() && ignoreNextTag)
 			ignoreNextTag = false;
 
 		return ignoreNextTag;
 	}
 
 	public static void loadLayout(File fileToLoad) {
 		FileReader fr = null;
 		Scanner sc = null;
 		String s;
 		FTLRoom room;
 		FTLDoor door;
 
 		int x, y, ox = 0, oy = 0;
 		boolean foundSection = false;
 		FTLShip ship = Main.ship;
 
 		for (FTLRoom r : ship.rooms)
 			r.dispose();
 		for (FTLDoor d : ship.doors)
 			d.dispose();
 
 		ship.rooms.clear();
 		ship.doors.clear();
 		Main.idList.clear();
 
 		debug("Importing layout from " + fileToLoad.getName() + "...");
 
 		try {
 			fr = new FileReader(fileToLoad);
 			sc = new Scanner(fr);
 
 			sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 			while (sc.hasNext()) {
 				s = sc.next();
 				if (s.equals("X_OFFSET")) {
 					ox = sc.nextInt();
 					negativeX = ox < 0;
 					foundSection = true;
 				} else if (s.equals("Y_OFFSET")) {
 					oy = sc.nextInt();
 					negativeY = oy < 0;
 					foundSection = true;
 				} else if (s.equals("ROOM")) {
 					room = new FTLRoom(0, 0, 0, 0);
 					room.id = sc.nextInt();
 
 					x = sc.nextInt() * 35;
 					y = sc.nextInt() * 35;
 					room.setLocationAbsolute(x, y);
 
 					x = sc.nextInt() * 35;
 					y = sc.nextInt() * 35;
 					room.setSize(x, y);
 
 					room.add(ship);
 
 					foundSection = true;
 				} else if (s.equals("DOOR")) {
 					door = new FTLDoor();
 
 					x = sc.nextInt() * 35 + ship.offset.x * 35;
 					y = sc.nextInt() * 35 + ship.offset.y * 35;
 					door.setLocationAbsolute(x, y);
 
 					sc.nextInt();
 					sc.nextInt();
 
 					x = sc.nextInt();
 					door.horizontal = (x == 0) ? true : false;
 
 					Point p = door.getLocation();
 					if (door.horizontal) {
 						door.setLocationAbsolute(p.x + 2, p.y - 3);
 					} else {
 						door.setLocationAbsolute(p.x - 3, p.y + 2);
 					}
 					door.fixRectOrientation();
 
 					door.add(ship);
 
 					foundSection = true;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load ship layout - txt - file not found.");
 		} catch (InputMismatchException e) {
 			Main.erDialog.add("Error: load ship layout - txt - data structure differs from expected. File is wrongly formatted.");
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 
 		ship.offset.x = ox;
 		ship.offset.y = oy;
 
 		if (foundSection && ship != null) {
 			Main.print("Successfully imported room layout from " + fileToLoad.getName());
 			Point p;
 			for (FTLRoom r : ship.rooms) {
 				p = r.getLocation();
 				r.setLocationAbsolute(ship.anchor.x + p.x + ship.offset.x * 35, ship.anchor.y + p.y + ship.offset.y * 35);
 			}
 			for (FTLDoor d : ship.doors) {
 				p = d.getLocation();
 				d.setLocationAbsolute(ship.anchor.x + p.x + ship.offset.x * 35, ship.anchor.y + p.y + ship.offset.y * 35);
 			}
 			Main.updatePainter();
 		} else {
 			Main.erDialog.add("Error: load ship layout - no matching section headers found, no data was loaded.");
 		}
 	}
 
 	/**
 	 * Loads the layout declared for the ship that is currently being loaded.
 	 */
 	public static void loadShipLayout(String parentPath) {
 		FileReader filer;
 		Scanner scanner = null;
 		String s;
 		int x = 0, y = 0;
 		FTLRoom room;
 		FTLDoor door;
 		Rectangle ellipse;
 		boolean foundSection = false;
 
 		Pattern pattern;
 		Matcher matcher;
 
 		FTLShip ship = shipBeingLoaded;
 
 		// === LOAD TXT FILE
 
 		try {
 			filer = new FileReader(parentPath + pathDelimiter + ship.layout + ".txt");
 			scanner = new Scanner(filer);
 
 			debug("\tloading layout data from " + ship.layout + ".txt");
 
 			while (scanner.hasNext()) {
 				s = scanner.next();
 				if (s.equals("X_OFFSET")) {
 					ship.offset.x = scanner.nextInt();
 					debug("\t\tX_OFFSET section found: " + ship.offset.x);
 					negativeX = ship.offset.x < 0;
 					foundSection = true;
 				} else if (s.equals("Y_OFFSET")) {
 					ship.offset.y = scanner.nextInt();
 					debug("\t\tY_OFFSET section found: " + ship.offset.y);
 					negativeY = ship.offset.y < 0;
 					foundSection = true;
 				} else if (s.equals("VERTICAL")) {
 					ship.vertical = Integer.valueOf(scanner.next());
 					debug("\t\tVERTICAL section found: " + ship.vertical);
 					foundSection = true;
 				} else if (s.equals("HORIZONTAL")) {
 					ship.horizontal = Integer.valueOf(scanner.next());
 					debug("\t\tHORIZONTAL section found: " + ship.horizontal);
 					foundSection = true;
 				} else if (s.equals("ELLIPSE")) {
 					ellipse = new Rectangle(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
 					// ellipse has format of (width, height, xoffset, yoffset), while Rectangle class has (x, y, width, height)
 					// so we've got to sort it out
 					ship.ellipse.x = ellipse.width;
 					ship.ellipse.y = ellipse.height;
 					ship.ellipse.width = ellipse.x;
 					ship.ellipse.height = ellipse.y;
 
 					debug("\t\tELLIPSE section found: " + ellipse);
 					foundSection = true;
 				} else if (s.equals("ROOM")) {
 					room = new FTLRoom(0, 0, 0, 0);
 					room.id = scanner.nextInt();
 
 					x = scanner.nextInt() * 35 + ship.offset.x * 35;
 					y = scanner.nextInt() * 35 + ship.offset.y * 35;
 					room.setLocationAbsolute(x, y);
 
 					x = scanner.nextInt() * 35;
 					y = scanner.nextInt() * 35;
 					room.setSize(x, y);
 
 					room.add(shipBeingLoaded);
 
 					debug("\t\tROOM: (" + room.id + ") " + room.getBounds());
 					foundSection = true;
 				} else if (s.equals("DOOR")) {
 					door = new FTLDoor();
 
 					// position
 					x = scanner.nextInt() * 35 + ship.offset.x * 35;
 					y = scanner.nextInt() * 35 + ship.offset.y * 35;
 					door.setLocationAbsolute(x, y);
 
 					// room IDs
 					door.leftId = scanner.nextInt();
 					door.rightId = scanner.nextInt();
 
 					// horizontal / vertical
 					x = scanner.nextInt();
 					door.horizontal = (x == 0) ? true : false;
 
 					Point p = door.getLocation();
 					if (door.horizontal) {
 						door.setLocationAbsolute(p.x + 2, p.y - 3);
 					} else {
 						door.setLocationAbsolute(p.x - 3, p.y + 2);
 					}
 					door.fixRectOrientation();
 
 					door.add(ship);
 
 					debug("\t\tDOOR: (" + ((door.horizontal) ? ("HORIZONTAL") : ("VERTICAL")) + ") " + door.getBounds().x / 35 + ", " + door.getBounds().y / 35);
 					foundSection = true;
 				}
 			}
 
 			resetUnnecessaryLinkIds();
 
 			ship.offset.x = Math.max(ship.offset.x, 0);
 			ship.offset.y = Math.max(ship.offset.y, 0);
 
 			debug("\tdone");
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load ship layout - txt - file not found.");
 		} catch (InputMismatchException e) {
 			Main.erDialog.add("Error: load ship layout - txt - data structure differs from expected. File is wrongly formatted.");
 		} finally {
 			if (scanner != null)
 				scanner.close();
 		}
 
 		// === LOAD XML FILE
 
 		try {
 			filer = new FileReader(parentPath + pathDelimiter + ship.layout + ".xml");
 			scanner = new Scanner(filer);
 			scanner.useDelimiter(Pattern.compile(lineDelimiter));
 
 			debug("\tloading layout data from " + ship.layout + ".xml");
 
 			while (scanner.hasNext()) {
 				s = scanner.next();
 				if (ship.imageRect.width == 0 && ship.imageRect.height == 0) {
 					pattern = Pattern.compile("^\\s*?<img x=\"(.*?)\" y=\"(.*?)\" w=\"(.*?)\" h=\"(.*?)\"\\s*?/\\s*?>");
 					matcher = pattern.matcher(s);
 					if (matcher.find()) {
 						ship.imageRect = new Rectangle(0, 0, 0, 0);
 						ship.imageRect.x = ship.offset.x * 35 + Integer.valueOf(matcher.group(1));
 						ship.imageRect.y = ship.offset.y * 35 + Integer.valueOf(matcher.group(2));
 						ship.imageRect.width = Integer.valueOf(matcher.group(3));
 						ship.imageRect.height = Integer.valueOf(matcher.group(4));
 						debug("\t\tShip image bounds found: " + ship.imageRect);
 					}
 				} else if (ship.mounts.size() == 0 && s.contains("<weaponMounts>")) {
 					FTLMount m;
 					ship.mounts = new LinkedList<FTLMount>();
 
 					debug("\t\tloading weapon mounts:");
 					while (scanner.hasNext() && !s.contains("</weaponMounts>") && !s.contains("mounts testing")) {
 						s = scanner.next();
 						pattern = Pattern.compile("(<mount x=\")(.*?)(\" y=\")(.*?)(\" rotate=\")(.*?)(\" mirror=\")(.*?)(\" gib=\")(.*?)(\" slide=\")(.*?)(\"\\s*?/\\s*?>)");
 						matcher = pattern.matcher(s);
 						if (matcher.find()) {
 							m = new FTLMount();
 							m.slide = Slide.valueOf(matcher.group(12).toUpperCase());
 							m.mirror = Boolean.valueOf(matcher.group(8));
 							m.setRotated(Boolean.valueOf(matcher.group(6)));
 							m.pos.x = Integer.valueOf(matcher.group(2));
 							m.pos.y = Integer.valueOf(matcher.group(4));
 							m.gib = Integer.valueOf(matcher.group(10));
 
 							m.add(shipBeingLoaded);
 							debug("\t\t\tfound mount " + ship.mounts.size() + ": " + m.pos + ", " + m.slide.toString().toLowerCase());
 						}
 					}
 					debug("\t\tdone");
 				} else if (ship.gibs.size() == 0 && s.contains("<explosion>")) {
 					debug("\t\tloading gibs:");
 
 					FTLGib g = null;
 					while (scanner.hasNext() && !s.contains("</explosion>")) {
 						s = scanner.next();
 						pattern = Pattern.compile("<gib(\\d*)>");
 						matcher = pattern.matcher(s);
 						if (matcher.find()) {
 							g = new FTLGib();
 							g.number = Integer.valueOf(matcher.group(1));
 
 							while (scanner.hasNext() && !s.contains("</gib")) {
 								s = scanner.next();
 								pattern = Pattern.compile(".*?<velocity\\s*?min=\"(.*?)\"\\s*?max=\"(.*?)\"/>");
 								matcher = pattern.matcher(s);
 								if (matcher.find()) {
 									g.minVel = Double.valueOf(matcher.group(1));
 									g.maxVel = Double.valueOf(matcher.group(2));
 								}
 
 								pattern = Pattern.compile(".*?<direction\\s*?min=\"(.*?)\"\\s*?max=\"(.*?)\"/>");
 								matcher = pattern.matcher(s);
 								if (matcher.find()) {
 									g.minDir = Integer.valueOf(matcher.group(1));
 									g.maxDir = Integer.valueOf(matcher.group(2));
 								}
 
 								pattern = Pattern.compile(".*?<angular\\s*?min=\"(.*?)\"\\s*?max=\"(.*?)\"/>");
 								matcher = pattern.matcher(s);
 								if (matcher.find()) {
 									g.minAng = Double.valueOf(matcher.group(1));
 									g.maxAng = Double.valueOf(matcher.group(2));
 								}
 
 								pattern = Pattern.compile(".*?<x>(\\d*)</x>");
 								matcher = pattern.matcher(s);
 								if (matcher.find()) {
 									g.position.x = Integer.valueOf(matcher.group(1));
 								}
 
 								pattern = Pattern.compile(".*?<y>(\\d*)</y>");
 								matcher = pattern.matcher(s);
 								if (matcher.find()) {
 									g.position.y = Integer.valueOf(matcher.group(1));
 								}
 							}
 
 							g.add(ship);
 						}
 					}
 					debug("\t\tdone");
 				}
 			}
 			debug("\tdone");
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load ship layout - xml - file not found.");
 		} catch (InputMismatchException e) {
 			Main.erDialog.add("Error: load ship layout - xml - data structure differs from expected. File is wrongly formatted.");
 		} finally {
 			if (scanner != null)
 				scanner.close();
 		}
 
 		if (foundSection && ship != null) {
 			Main.print(ship.layout + " loaded successfully.");
 			debug(ship.layout + " loaded successfully.");
 		} else {
 			Main.erDialog.add("Error: load ship layout - no matching section headers found, no data was loaded.");
 		}
 	}
 
 	/**
 	 * 
 	 * @param ship
 	 *            Ship which supplies paths for files that are to be loaded.
 	 * @param fileToScan
 	 *            Only needed when loading from an .ftl package, null otherwise.
 	 */
 	public static void loadShipImages(FTLShip ship, File fileToScan) {
 		try {
 			if (!isNull(Main.resPath) && ship != null) {
 				if (!isNull(ship.imagePath) && new File(ship.imagePath).exists()) {
 					Main.hullBox.setHullImage(ship.imagePath);
 				} else if (!isNull(ship.imagePath)) {
 					Main.erDialog.add("Warning: load ship images - hull image not found. (" + ship.imagePath + ")");
 					ship.imagePath = null;
 				}
 				// load floor
 				File f;
 				if (!isNull(ship.floorPath)) {
 					f = new File(ship.floorPath);
 					if (ship.isPlayer && f.exists() && Main.loadFloor) {
 						Main.hullBox.setFloorImage(ship.floorPath);
 					} else if (ship.isPlayer && !f.exists()) {
 						Main.erDialog.add("Warning: load ship images - floor image not found. (" + ship.floorPath + ")");
 						ship.floorPath = null;
 					}
 				}
 				// load shield
 				if (Main.loadShield) {
 					if (!isNull(ship.shieldPath)) {
 						f = new File(ship.shieldPath);
 						if (f.exists()) {
 							Main.shieldBox.setImage(ship.shieldPath, true);
 						} else {
 							Main.erDialog.add("Warning: load ship images - shield image not found. (" + ship.shieldPath + ")");
 							ship.shieldPath = null;
 						}
 					}
 				}
 				// load cloak
 				if (!isNull(ship.cloakPath)) {
 					f = new File(ship.cloakPath);
 					if (f.exists()) {
 						Main.hullBox.setCloakImage(ship.cloakPath);
 					} else {
 						Main.erDialog.add("Warning: load ship images - cloak image not found. (" + ship.cloakPath + ")");
 						ship.cloakPath = null;
 					}
 				}
 				// load gibs
 				String path;
 				for (FTLGib g : ship.gibs) {
 					if (g != null) {
 						path = g.getPath();
 						if (isNull(path))
 							path = ship.imageName + "_gib" + g.number + ".png";
 						f = new File(path);
 						if (f.exists()) {
 							g.setImage(path, false);
 						} else {
 							// loading from .ftl
 							path = ship.imageName + "_gib" + g.number + ".png";
 							if (fileToScan != null)
 								path = fileToScan.getParentFile().getParentFile().getAbsolutePath() + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + ship.imageName + "_gib" + g.number + ".png";
 							;
 							f = new File(path);
 							if (f.exists()) {
 								g.setImage(path, false);
 							} else {
 								// loading from edtior's resources path
 								path = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + ship.imageName + "_gib" + g.number + ".png";
 								f = new File(path);
 								if (f.exists()) {
 									g.setImage(path, false);
 								} else {
 									Main.erDialog.add("Warning: load ship images - image for gib number " + g.number + " could not be found. (" + path + ")");
 									g.dispose();
 								}
 							}
 						}
 					}
 				}
 			} else {
 				Main.erDialog.add("Error: load ship images - resource path is not set, unable to find images.");
 			}
 		} catch (SWTException e) {
 			Main.erDialog.add("Error: load ship images - one of the ship's images could not be found.");
 			if (Main.hullImage == null)
 				Main.ship.imagePath = null;
 			if (Main.floorImage == null)
 				Main.ship.floorPath = null;
 			if (Main.shieldImage == null)
 				Main.ship.shieldPath = null;
 			if (Main.cloakImage == null)
 				Main.ship.cloakPath = null;
 		}
 	}
 
 	public static void loadImage(String path, String mod) {
 		File f = null;
 		// === Hull
 		if (mod.equals("hull")) {
 			if (isNull(Main.ship.imageName)) {
 				Main.ship.imageName = path.substring(path.lastIndexOf(pathDelimiter) + 1, path.lastIndexOf("."));
 				if (Main.ship.imageName.contains("_base"))
 					Main.ship.imageName = Main.ship.imageName.substring(0, Main.ship.imageName.indexOf("_base"));
 			}
 			if (isNull(Main.ship.layout)) {
 				Main.ship.layout = path.substring(path.lastIndexOf(pathDelimiter) + 1, path.lastIndexOf("."));
 				if (Main.ship.layout.contains("_base"))
 					Main.ship.layout = Main.ship.layout.substring(0, Main.ship.layout.indexOf("_base"));
 			}
 
 			Main.ship.imagePath = path;
 			f = new File(Main.ship.imagePath);
 			if (f.exists()) {
 				Main.hullBox.setHullImage(path);
 			} else {
 				Main.erDialog.add("Warning: load image - hull image file not found.");
 			}
 
 			Main.ship.imageRect.width = Main.hullBox.getBounds().width;
 			Main.ship.imageRect.height = Main.hullBox.getBounds().height;
 
 			// === Cloak
 		} else if (mod.equals("cloak")) {
 			f = new File(Main.ship.cloakPath);
 			if (f.exists()) {
 				Main.hullBox.setCloakImage(path);
 			} else {
 				Main.erDialog.add("Warning: load image - cloak image file not found.");
 			}
 
 			// === Shields
 		} else if (mod.equals("shields")) {
 			f = new File(Main.ship.shieldPath);
 			if (Main.loadShield) {
 				if (f.exists()) {
 					Main.shieldBox.setImage(path, true);
 				} else {
 					Main.erDialog.add("Warning: load image - shield image file not found.");
 				}
 			}
 
 			// === Floor
 		} else if (mod.equals("floor")) {
 			f = new File(Main.ship.floorPath);
 			if (Main.loadFloor) {
 				if (f.exists()) {
 					Main.hullBox.setFloorImage(path);
 				} else {
 					Main.erDialog.add("Warning: load image - floor image file not found.");
 				}
 			}
 		}
 
 		Main.updatePainter();
 	}
 
 	public static void remapMountsToWeapons() {
 		String wpn = null;
 		if (!Main.ship.weaponsBySet) {
 			mountWeaponMap.clear();
 
 			for (int i = 0; i < Main.ship.weaponSet.size(); i++) {
 				wpn = Main.ship.weaponSet.get(i);
 				if (wpn != null)
 					mountWeaponMap.put(Main.ship.getMountWithIndex(i), wpn);
 			}
 		}
 	}
 
 	/**
 	 * Returns name of the image the weapon uses ingame.
 	 * 
 	 * @param weaponName
 	 * @return
 	 */
 	private static String getWeaponArtFileName(FTLMount m, FTLItem weapon) {
 		String result = null;
 		FileReader filer;
 		Scanner sc = null;
 		String s;
 		Pattern pattern;
 		Matcher matcher;
 		boolean found = false;
 
 		try {
 			File f = new File("sprlmnl_tmp" + pathDelimiter + "data" + pathDelimiter + "animations.xml.append");
 			if (f.exists()) {
 				filer = new FileReader(f);
 				sc = new Scanner(filer);
 				sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 				while (sc.hasNext() && (result == null || !found)) {
 					s = sc.next();
 					pattern = Pattern.compile("(<animSheet name=\")(.*?)(\" w=\")(.*?)(\".*?fw=\")(.*?)(\".*?>)(.*?)(</animSheet>)");
 					matcher = pattern.matcher(s);
 					if (matcher.find() && matcher.group(2).toLowerCase().equals(weapon.img.toLowerCase())) {
 						Main.weaponFrameWidthMap.put(weapon.blueprint, Integer.valueOf(matcher.group(6)));
 						result = matcher.group(8);
 					}
 
 					pattern = Pattern.compile("<weaponAnim name=\"(.*?)\">");
 					matcher = pattern.matcher(s);
 					if (matcher.find() && matcher.group(1).toLowerCase().equals(weapon.img.toLowerCase())) {
 						while (sc.hasNext() && !s.contains("</weaponAnim>")) {
 							s = sc.next();
 
 							pattern = Pattern.compile("<mountPoint x=\"(\\d*?)\" y=\"(\\d*?)\"");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								m.mountPoint.x = Integer.parseInt(matcher.group(1));
 								m.mountPoint.y = Integer.parseInt(matcher.group(2));
 								found = true;
 							}
 						}
 					}
 				}
 			}
 
 			if (result == null) {
 				if (sc != null)
 					sc.close();
 
 				filer = new FileReader(Main.dataPath + pathDelimiter + "animations.xml");
 				sc = new Scanner(filer);
 				sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 				while (sc.hasNext() && (result == null || !found)) {
 					s = sc.next();
 					pattern = Pattern.compile("(<animSheet name=\")(.*?)(\" w=\")(.*?)(\".*?fw=\")(.*?)(\".*?>)(.*?)(</animSheet>)");
 					matcher = pattern.matcher(s);
 					if (matcher.find() && matcher.group(2).toLowerCase().equals(weapon.img.toLowerCase())) {
 						Main.weaponFrameWidthMap.put(weapon.blueprint, Integer.valueOf(matcher.group(6)));
 						result = matcher.group(8);
 					}
 
 					pattern = Pattern.compile("<weaponAnim name=\"(.*?)\">");
 					matcher = pattern.matcher(s);
 					if (!found && matcher.find() && matcher.group(1).toLowerCase().equals(weapon.img.toLowerCase())) {
 						while (sc.hasNext() && !s.contains("</weaponAnim>")) {
 							s = sc.next();
 
 							pattern = Pattern.compile("<mountPoint x=\"(\\d*?)\" y=\"(\\d*?)\"");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								m.mountPoint.x = Integer.parseInt(matcher.group(1));
 								m.mountPoint.y = Integer.parseInt(matcher.group(2));
 								found = true;
 							}
 						}
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: get weapon art - animations.xml not found in data directory.");
 		} finally {
 			sc.close();
 		}
 
 		return result;
 	}
 
 	public static void loadWeaponImages(FTLShip ship) {
 		String path = null;
 		FTLItem wpn = null;
 		int index = -1;
 
 		for (FTLMount m : Main.ship.mounts) {
 			m.setImage(null);
 		}
 
 		if (!ship.weaponsBySet) {
 			for (String blue : ship.weaponSet) {
 				index++;
 				wpn = getItem(blue);
 				if (wpn != null && !isNull(wpn.img)) {
 					FTLMount mt = null;
 					if (index < Main.ship.mounts.size())
 						mt = Main.ship.getMountWithIndex(index);
 
 					if (mt != null) {
 						path = getWeaponArtFileName(mt, wpn);
 
 						if (path != null) {
 							String temppath = "sprlmnl_tmp" + pathDelimiter + "img" + pathDelimiter + path;
 							if (new File(temppath).exists()) {
 								mt.setImage(temppath, Main.weaponFrameWidthMap.get(blue));
 								continue;
 							}
 
 							path = Main.resPath + pathDelimiter + "img" + pathDelimiter + path;
 
 							mt.setImage(path, Main.weaponFrameWidthMap.get(blue));
 						}
 					} else {
 						Main.erDialog.add("Warning: load weapon images - tried to get mount with index " + index + ", but returned null.");
 					}
 				} else if (wpn != null) {
 					Main.erDialog.add("Warning: load weapon images - tried to load " + blue + " as weapon, has no associated image. [weaponArt property in weapon's declaration points to an image that doesn't exist]");
 				} else {
 					Main.erDialog.add("Warning: load weapon images - tried to get item from blueprint " + blue + ", returned null. [item declaration was not loaded from the blueprints.xml]");
 				}
 			}
 		}
 	}
 
 	// =============
 	// === EXPORTING
 	// =============
 
 	public static void deleteFolderContents(File folder) {
 		File[] files = folder.listFiles();
 		if (files != null) { // some JVMs return null for empty dirs
 			for (File f : files) {
 				if (!f.isDirectory()) {
 					f.delete();
 				} else {
 					deleteFolderContents(f);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Exports the currently opened ship to file in format recognised by FTL. Creates a new directory at the desstination, named after the ship's blueprint name.
 	 * 
 	 * @param path
 	 *            Location of the file to which the ship is to be exporeted.
 	 */
 	public static void export(String path, String fileName) {
 		String pathDir = path + pathDelimiter + Main.ship.blueprintName;
 
 		ExportProgress exp = new ExportProgress();
 		exp.open(Main.exDialog.shell);
 
 		FileWriter fw = null;
 		File destination = new File(pathDir);
 		File source;
 		destination.mkdirs();
 		deleteFolderContents(destination);
 
 		// ===============
 		// === Copy images
 
 		// hull
 		try {
 			source = new File(Main.ship.imagePath);
 			destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + Main.ship.imageName + "_base.png");
 			if (source.exists()) {
 				destination.mkdirs();
 				java.nio.file.Files.copy(Paths.get(Main.ship.imagePath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 			} else if (!source.exists()) {
 				Main.erDialog.add("Error: export - hull image not found.");
 			}
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: export - hull image not found.");
 		} catch (IOException e) {
 			Main.erDialog.add("Error: export - hull image - general IO error - details logged in debug.log");
 			e.printStackTrace();
 			System.out.println("");
 		} catch (NullPointerException e) {
 			Main.erDialog.add("Warning: export - hull has not been set up.");
 		}
 		exp.progressBar.setSelection(10);
 
 		// shields
 		if (Main.ship.isPlayer) {
 			try {
 				source = new File(Main.ship.shieldPath);
 				destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + Main.ship.imageName + "_shields1.png");
 				if (source.exists()) {
 					destination.mkdirs();
 					if (!isNull(Main.ship.shieldPath)) {
 						java.nio.file.Files.copy(Paths.get(Main.ship.shieldPath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 					}
 				} else if (!source.exists()) {
 					Main.erDialog.add("Error: export - shield image not found.");
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: export - shield image not found.");
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - shield image - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			} catch (NullPointerException e) {
 				Main.erDialog.add("Warning: export - shield has not been set up.");
 			}
 		}
 		exp.progressBar.setSelection(20);
 
 		// floor
 		if (Main.ship.isPlayer && !isNull(Main.ship.floorPath)) {
 			try {
 				source = new File(Main.ship.floorPath);
 				destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + Main.ship.imageName + "_floor.png");
 				if (source.exists()) {
 					destination.mkdirs();
 					java.nio.file.Files.copy(Paths.get(Main.ship.floorPath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 				} else if (!source.exists()) {
 					Main.erDialog.add("Error: export - floor image not found.");
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: export - floor image not found.");
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - floor image - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			} catch (NullPointerException e) {
 				Main.erDialog.add("Warning: export - floor has not been set up.");
 			}
 		}
 		exp.progressBar.setSelection(30);
 
 		// cloak
 		if (!isNull(Main.ship.cloakPath)) {
 			try {
 				source = new File(Main.ship.cloakPath);
 				destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + Main.ship.imageName + "_cloak.png");
 				if (source.exists()) {
 					destination.mkdirs();
 					if (!isNull(Main.ship.cloakPath)) {
 						java.nio.file.Files.copy(Paths.get(Main.ship.cloakPath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 					}
 				} else if (!source.exists()) {
 					Main.erDialog.add("Error: export - cloak image not found.");
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: export - cloak image not found.");
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - cloak image - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			} catch (NullPointerException e) {
 				Main.erDialog.add("Warning: export - cloak has not been set up.");
 			}
 		}
 		exp.progressBar.setSelection(40);
 
 		// miniship
 		if (Main.ship.isPlayer && !isNull(Main.ship.miniPath)) {
 			try {
 				source = new File(Main.ship.miniPath);
 				destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "customizeUI" + pathDelimiter + "miniship_" + Main.ship.imageName + ".png");
 				if (source.exists()) {
 					destination.mkdirs();
 					java.nio.file.Files.copy(Paths.get(Main.ship.miniPath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 				} else if (!source.exists()) {
 					Main.erDialog.add("Error: export - miniship image not found.");
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: export - miniship image not found.");
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - miniship image - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			} catch (NullPointerException e) {
 				Main.erDialog.add("Warning: export - miniship image has not been set up.");
 			}
 		}
 		exp.progressBar.setSelection(50);
 
 		// interior images
 		if (Main.ship.rooms.size() > 0) {
 			try {
 				double d = 10 / Main.ship.rooms.size();
 				double progress = 0;
 				String img;
 				for (FTLRoom r : Main.ship.rooms) {
 					if (!r.getSystem().equals(Systems.EMPTY) && !r.getSystem().equals(Systems.TELEPORTER) && r.sysBox != null && !isNull(r.interiorData.interiorPath)) {
 						img = r.interiorData.interiorPath.substring(r.interiorData.interiorPath.lastIndexOf(pathDelimiter));
 						source = new File(r.interiorData.interiorPath);
 						destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + img);
 						// destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + Main.ship.imageName + "_room_" +
 						// r.getSystem().toString().toLowerCase() + ".png");
 
 						destination.mkdirs();
 						java.nio.file.Files.copy(Paths.get(r.interiorData.interiorPath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 						String tempDest = destination.getAbsolutePath().replace(".png", "");
 
 						try {
 							if (r.sysBox.getSystemName().equals(Systems.CLOAKING)) {
 								if (!isNull(r.interiorData.glowPath1))
 									java.nio.file.Files.copy(Paths.get(r.interiorData.glowPath1), Paths.get(tempDest + "_glow.png"), StandardCopyOption.REPLACE_EXISTING);
 							} else {
 								if (!isNull(r.interiorData.glowPath1))
 									java.nio.file.Files.copy(Paths.get(r.interiorData.glowPath1), Paths.get(tempDest + "_glow1.png"), StandardCopyOption.REPLACE_EXISTING);
 								if (!isNull(r.interiorData.glowPath2))
 									java.nio.file.Files.copy(Paths.get(r.interiorData.glowPath2), Paths.get(tempDest + "_glow2.png"), StandardCopyOption.REPLACE_EXISTING);
 								if (!isNull(r.interiorData.glowPath3))
 									java.nio.file.Files.copy(Paths.get(r.interiorData.glowPath3), Paths.get(tempDest + "_glow3.png"), StandardCopyOption.REPLACE_EXISTING);
 							}
 						} catch (FileNotFoundException e) {
 							Main.erDialog.add("Warning: export - one of the glow images for " + source.getName() + " was not found.");
 						} catch (NoSuchFileException e) {
 							Main.erDialog.add("Warning: export - one of the glow images for " + source.getName() + " was not found.");
 						}
 					}
 					progress += d;
 					exp.progressBar.setSelection(50 + (int) progress);
 				}
 			} catch (FileNotFoundException e) {
 				Main.erDialog.add("Error: export - one of room interior images was not found.");
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - interior images - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			} catch (NullPointerException e) {
 				Main.erDialog.add("Error: export - one of system interior images has not been set up.");
 			}
 		}
 		exp.progressBar.setSelection(60);
 
 		// gibs
 		if (Main.ship.gibs.size() > 0) {
 			try {
 				double d = 10 / Main.ship.gibs.size();
 				double progress = 0;
 				for (FTLGib g : Main.ship.gibs) {
 					source = new File(g.getPath());
 					destination = new File(pathDir + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + Main.ship.imageName + "_gib" + g.number + ".png");
 					if (source.exists()/* && (dontCheck || !isDefaultResource(source)) */) {
 						destination.mkdirs();
 						java.nio.file.Files.copy(Paths.get(g.getPath()), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
 					}
 					progress += d;
 					exp.progressBar.setSelection(60 + (int) progress);
 				}
 			} catch (IOException e) {
 				Main.erDialog.add("Error: export - gibs images - general IO error - details logged in debug.log");
 				e.printStackTrace();
 				System.out.println("");
 			}
 		}
 		exp.progressBar.setSelection(70);
 
 		// ====================
 		// === Write layout.txt
 
		lineDelimiter = "\r\n";
		
 		try {
 			destination = new File(pathDir + pathDelimiter + "data");
 			destination.mkdirs();
 
 			fw = new FileWriter(pathDir + pathDelimiter + "data" + pathDelimiter + fileName + ".txt", false);
 
 			fw.write("X_OFFSET" + lineDelimiter);
 			Main.ship.offset.x = Math.round(((float) Main.ship.findLowBounds().x - (float) Main.ship.anchor.x) / 35);
 			fw.write(Main.ship.offset.x + lineDelimiter);
 
 			fw.write("Y_OFFSET" + lineDelimiter);
 			Main.ship.offset.y = Math.round(((float) Main.ship.findLowBounds().y - (float) Main.ship.anchor.y) / 35);
 			fw.write(Main.ship.offset.y + lineDelimiter);
 
 			fw.write("HORIZONTAL" + lineDelimiter);
 			fw.write(Main.ship.horizontal + lineDelimiter);
 
 			fw.write("VERTICAL" + lineDelimiter);
 			fw.write(Main.ship.vertical + lineDelimiter);
 
 			fw.write("ELLIPSE" + lineDelimiter);
 			if (Main.shieldImage != null && !Main.shieldImage.isDisposed() && Main.ship.isPlayer) {
 				fw.write(Main.shieldImage.getBounds().width / 2 + lineDelimiter);
 				fw.write(Main.shieldImage.getBounds().height / 2 + lineDelimiter);
 			} else {
 				fw.write(Main.shieldEllipse.width / 2 + lineDelimiter);
 				fw.write(Main.shieldEllipse.height / 2 + lineDelimiter);
 			}
 
 			fw.write(Main.ship.ellipse.x + lineDelimiter);
 			fw.write(Main.ship.ellipse.y + lineDelimiter);
 
 			SortedSet<FTLRoom> set = new TreeSet<FTLRoom>();
 			set.addAll(Main.ship.rooms);
 			for (FTLRoom r : set) {
 				fw.write("ROOM" + lineDelimiter);
 				fw.write(r.id + lineDelimiter);
 
 				fw.write((r.getBounds().x - Main.ship.anchor.x - Main.ship.offset.x * 35) / 35 + lineDelimiter);
 				fw.write((r.getBounds().y - Main.ship.anchor.y - Main.ship.offset.y * 35) / 35 + lineDelimiter);
 				fw.write(r.getBounds().width / 35 + lineDelimiter);
 				fw.write(r.getBounds().height / 35 + lineDelimiter);
 			}
 
 			int x, y;
 			for (FTLDoor d : Main.ship.doors) {
 				fw.write("DOOR" + lineDelimiter);
 				x = (d.getBounds().x - Main.ship.anchor.x - Main.ship.offset.x * 35 + (d.horizontal ? -2 : 3)) / 35;
 				y = (d.getBounds().y - Main.ship.anchor.y - Main.ship.offset.y * 35 + (d.horizontal ? 3 : -2)) / 35;
 
 				fw.write(x + lineDelimiter);
 				fw.write(y + lineDelimiter);
 				x = (d.leftId == -2) ? Main.ship.findLeftRoom(d) : d.leftId;
 				y = (d.rightId == -2) ? Main.ship.findRightRoom(d) : d.rightId;
 				fw.write(((x == -1) ? y : x) + lineDelimiter);
 				fw.write(((x != -1) ? y : x) + lineDelimiter);
 				fw.write((d.horizontal ? 0 : 1) + lineDelimiter);
 			}
 
 			fw.close();
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: export - file not found.");
 		} catch (IOException e) {
 			Main.debug("IO Exception while writing layout.txt:");
 			e.printStackTrace();
 		} finally {
 			try {
 				fw.close();
 			} catch (IOException e) {
 				Main.debug("IO Exception while closing export stream:");
 				e.printStackTrace();
 			}
 			fw = null;
 		}
 		exp.progressBar.setSelection(80);
 
 		// ====================
 		// === Write layout.xml
		
 		try {
 			fw = new FileWriter(pathDir + pathDelimiter + "data" + pathDelimiter + fileName + ".xml", false);
 
 			fw.write("<!-- Copyright (c) 2012 by Subset Games. All rights reserved -->" + lineDelimiter);
 			fw.write("<!-- Superluminal exported file -->" + lineDelimiter);
 
 			fw.write(lineDelimiter);
 
 			// hull image positioning
			fw.write("<img x=\"" + (Main.hullBox.getLocation().x - Main.ship.anchor.x - Main.ship.offset.x * 35)
					+ "\" y=\"" + (Main.hullBox.getLocation().y - Main.ship.anchor.y - Main.ship.offset.y * 35)
 					+ "\" w=\"" + Main.ship.imageRect.width
 					+ "\" h=\"" + Main.ship.imageRect.height + "\"/>" + lineDelimiter);
 
 			fw.write(lineDelimiter);
 
 			// weapon mounts
 			fw.write("<weaponMounts>" + lineDelimiter);
 			FTLMount mt = null;
 			int count = 0;
 			for (int i = 0; i < Main.ship.mounts.size(); i++) {
 				FTLMount m = Main.ship.getMountWithIndex(i);
 
 				if (m == null) {
 					Main.erDialog.add("No weapon mount found for index " + (i + 1));
 					continue;
 				}
 				
 				if (count < Main.ship.weaponSlots) {
 					fw.write("\t");
 					fw.write("<mount x=\"" + m.pos.x + "\" ");
 					fw.write("y=\"" + m.pos.y + "\" ");
 					fw.write("rotate=\"" + m.isRotated() + "\" ");
 					fw.write("mirror=\"" + m.mirror + "\" ");
 					fw.write("gib=\"" + m.gib + "\" ");
 					fw.write("slide=\"" + m.slide.toString().toLowerCase() + "\"/>");
 					fw.write(lineDelimiter);
 					count++;
 				} else if (count == Main.ship.weaponSlots && Main.isSystemAssigned(Systems.ARTILLERY)) {
 					mt = m;
 				}
 			}
 
 			// (weapon slots)th mount is associated with artillery, has to be written as the last one on the list
 			if (mt != null) {
 				fw.write("\t");
 				fw.write("<mount x=\"" + mt.pos.x + "\" ");
 				fw.write("y=\"" + mt.pos.y + "\" ");
 				fw.write("rotate=\"" + mt.isRotated() + "\" ");
 				fw.write("mirror=\"" + mt.mirror + "\" ");
 				fw.write("gib=\"" + mt.gib + "\" ");
 				fw.write("slide=\"" + mt.slide.toString().toLowerCase() + "\"/>");
 				fw.write(lineDelimiter);
 			}
 
 			fw.write("</weaponMounts>" + lineDelimiter);
 
 			fw.write(lineDelimiter);
 
 			// gibs
 			DecimalFormat threeDec = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
 			fw.write("<explosion>" + lineDelimiter);
 			for (FTLGib g : Main.ship.gibs) {
 				fw.write("\t" + "<gib" + g.number + ">" + lineDelimiter);
 				fw.write("\t\t" + "<velocity min=\"" + threeDec.format(g.minVel) + "\" max=\"" + threeDec.format(g.maxVel) + "\"/>" + lineDelimiter);
 				fw.write("\t\t" + "<direction min=\"" + g.minDir + "\" max=\"" + g.maxDir + "\"/>" + lineDelimiter);
 				fw.write("\t\t" + "<angular min=\"" + threeDec.format(g.minAng) + "\" max=\"" + threeDec.format(g.maxAng) + "\"/>" + lineDelimiter);
 				fw.write("\t\t" + "<x>" + g.position.x + "</x>" + lineDelimiter);
 				fw.write("\t\t" + "<y>" + g.position.y + "</y>" + lineDelimiter);
 				fw.write("\t" + "</gib" + g.number + ">" + lineDelimiter);
 			}
 			fw.write("</explosion>");
 
 			fw.close();
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: export - file not found.");
 		} catch (IOException e) {
 			Main.debug("IO Exception while writing layout.xml:");
 			e.printStackTrace();
 		} finally {
 			try {
 				fw.close();
 			} catch (IOException e) {
 				Main.debug("IO Exception while closing export stream:");
 				e.printStackTrace();
 			}
 			fw = null;
 		}
 
 		// ========================
 		// === Write blueprints.xml.append
 
 		try {
 			// === Player Ship
 			if (Main.ship.isPlayer) {
 				fw = new FileWriter(pathDir + pathDelimiter + "data" + pathDelimiter + "blueprints.xml.append");
 
 				// general information
 				fw.write("<shipBlueprint name=\"" + Main.ship.blueprintName + "\" layout=\"" + Main.ship.layout + "\" img=\"" + Main.ship.imageName + "\">");
 				fw.write(lineDelimiter);
 				fw.write("\t<class>" + Main.ship.shipClass + "</class>");
 				fw.write(lineDelimiter);
 				fw.write("\t<name>" + Main.ship.shipName + "</name>");
 				fw.write(lineDelimiter);
 				fw.write("\t<desc>" + Main.ship.descr + "</desc>");
 
 				// systems
 				fw.write(lineDelimiter);
 				fw.write("\t<systemList>" + lineDelimiter);
 
 				for (FTLRoom r : Main.ship.rooms) {
 					if (r.sysBox != null && !r.getSystem().equals(Systems.EMPTY)) {
 						fw.write("\t\t<" + r.getSystem().toString().toLowerCase() + " power=\"" + Main.ship.levelMap.get(r.getSystem()) + "\" room=\"" + r.id + "\" start=\"" + Main.ship.startMap.get(r.getSystem()) + "\"");
 						if (!isNull(r.interiorData.interiorPath)) {
 							fw.write(" img=\"" + r.interiorData.interiorPath.substring(r.interiorData.interiorPath.lastIndexOf(pathDelimiter) + 1, r.interiorData.interiorPath.lastIndexOf('.')) + "\"");
 						}
 						if ((Main.ship.slotMap.keySet().contains(r.getSystem()) && Main.ship.slotMap.get(r.getSystem()) != -2) || r.getSystem().equals(Systems.MEDBAY)) {
 							fw.write(">");
 							fw.write(lineDelimiter);
 							fw.write("\t\t\t" + "<slot>" + lineDelimiter);
 							if (!r.getSystem().equals(Systems.MEDBAY))
 								fw.write("\t\t\t\t" + "<direction>" + Main.ship.slotDirMap.get(r.getSystem()).toString().toLowerCase() + "</direction>" + lineDelimiter);
 							fw.write("\t\t\t\t" + "<number>" + Main.ship.slotMap.get(r.getSystem()) + "</number>" + lineDelimiter);
 							fw.write("\t\t\t" + "</slot>" + lineDelimiter);
 							fw.write("\t\t" + "</" + r.getSystem().toString().toLowerCase() + ">");
 						} else {
 							fw.write("/>");
 						}
 						fw.write(lineDelimiter);
 					}
 				}
 				fw.write("\t" + "</systemList>" + lineDelimiter);
 
 				fw.write("\t" + "<weaponSlots>" + Main.ship.weaponSlots + "</weaponSlots>" + lineDelimiter);
 				fw.write("\t" + "<droneSlots>" + Main.ship.droneSlots + "</droneSlots>" + lineDelimiter);
 
 				// weapons
 				fw.write("\t" + "<weaponList count=\"" + Main.ship.weaponCount + "\" missiles=\"" + Main.ship.missiles + "\">" + lineDelimiter);
 				for (String blue : Main.ship.weaponSet)
 					fw.write("\t\t" + "<weapon name=\"" + blue + "\"/>" + lineDelimiter);
 				fw.write("\t" + "</weaponList>" + lineDelimiter);
 
 				// drones
 				fw.write("\t" + "<droneList count=\"" + Main.ship.droneCount + "\" drones=\"" + Main.ship.drones + "\">" + lineDelimiter);
 				for (String blue : Main.ship.droneSet)
 					fw.write("\t\t" + "<drone name=\"" + blue + "\"/>" + lineDelimiter);
 				fw.write("\t" + "</droneList>" + lineDelimiter);
 
 				// health & power
 				fw.write("\t" + "<health amount=\"" + Main.ship.hullHealth + "\"/>" + lineDelimiter);
 				fw.write("\t" + "<maxPower amount=\"" + Main.ship.reactorPower + "\"/>" + lineDelimiter);
 
 				// crew
 				boolean auto = true;
 				for (String key : Main.ship.crewMap.keySet()) {
 					if (Main.ship.crewMap.get(key) > 0) {
 						auto = false;
 						if (!key.equals("random")) {
 							fw.write("\t" + "<crewCount amount=\"" + Main.ship.crewMap.get(key) + "\" class=\"" + key + "\"/>");
 							fw.write(lineDelimiter);
 						} else {
 							fw.write("\t" + "<crewCount amount=\"" + Main.ship.crewMap.get(key) + "\"/>");
 							fw.write(lineDelimiter);
 						}
 					}
 				}
 				if (auto) {
 					fw.write("\t" + "<crewCount amount=\"0\" class=\"human\"/>");
 					fw.write(lineDelimiter);
 				}
 
 				// augments
 				for (String aug : Main.ship.augmentSet) {
 					fw.write("\t" + "<aug name=\"" + aug + "\"/>" + lineDelimiter);
 				}
 
 				// overrides
 				/*
 				 * if (!isNull(Main.ship.shieldOverride))
 				 * fw.write("\t"+"<shieldImage>"+Main.ship.shieldOverride+"</shieldImage>" + lineDelimiter);
 				 * if (!isNull(Main.ship.cloakOverride))
 				 * fw.write("\t"+"<cloakImage>"+Main.ship.cloakOverride+"</cloakImage>" + lineDelimiter);
 				 */
 
 				fw.write("</shipBlueprint>");
 
 				// === Enemy Ship
 			} else {
 				fw = new FileWriter(pathDir + pathDelimiter + "data" + pathDelimiter + "autoBlueprints.xml.append");
 
 				// general information
 				fw.write("<shipBlueprint name=\"" + Main.ship.blueprintName + "\" layout=\"" + Main.ship.layout + "\" img=\"" + Main.ship.imageName + "\">");
 				fw.write(lineDelimiter);
 				fw.write("\t<class>" + Main.ship.shipClass + "</class>");
 				fw.write(lineDelimiter);
 
 				fw.write("\t<minSector>" + Main.ship.minSec + "</minSector>");
 				fw.write(lineDelimiter);
 				fw.write("\t<maxSector>" + Main.ship.maxSec + "</maxSector>");
 				fw.write(lineDelimiter);
 
 				// systems
 				fw.write("\t<systemList>" + lineDelimiter);
 
 				for (FTLRoom r : Main.ship.rooms) {
 					if (!r.getSystem().equals(Systems.EMPTY)) {
 						fw.write("\t\t<" + r.getSystem().toString().toLowerCase()
 								+ " power=\"" + Main.ship.powerMap.get(r.getSystem())
 								+ "\" max=\"" + Main.ship.levelMap.get(r.getSystem())
 								+ "\" room=\"" + r.id
 								+ "\" start=\"" + Main.ship.startMap.get(r.getSystem()) + "\"");
 
 						if ((Main.ship.slotMap.keySet().contains(r.getSystem()) && Main.ship.slotMap.get(r.getSystem()) != -2) || r.getSystem().equals(Systems.MEDBAY)) {
 							fw.write(">");
 							fw.write(lineDelimiter);
 							fw.write("\t\t\t" + "<slot>" + lineDelimiter);
 							if (!r.getSystem().equals(Systems.MEDBAY))
 								fw.write("\t\t\t\t" + "<direction>" + Main.ship.slotDirMap.get(r.getSystem()).toString().toLowerCase() + "</direction>" + lineDelimiter);
 							fw.write("\t\t\t\t" + "<number>" + Main.ship.slotMap.get(r.getSystem()) + "</number>" + lineDelimiter);
 							fw.write("\t\t\t" + "</slot>" + lineDelimiter);
 							fw.write("\t\t" + "</" + r.getSystem().toString().toLowerCase() + ">");
 						} else {
 							fw.write("/>");
 						}
 						fw.write(lineDelimiter);
 					}
 				}
 				fw.write("\t" + "</systemList>" + lineDelimiter);
 
 				// weapons
 				if (Main.ship.weaponSet.size() > 0) {
 					if (Main.ship.weaponsBySet) {
 						fw.write("\t" + "<weaponList missiles=\"" + Main.ship.missiles + "\" load=\"" + Main.ship.weaponSet.toArray()[0] + "\"/>" + lineDelimiter);
 					} else {
 						fw.write("\t" + "<weaponList count=\"" + Main.ship.weaponCount + "\" missiles=\"" + Main.ship.missiles + "\">" + lineDelimiter);
 						for (String blue : Main.ship.weaponSet)
 							fw.write("\t\t" + "<weapon name=\"" + blue + "\"/>" + lineDelimiter);
 						fw.write("\t" + "</weaponList>" + lineDelimiter);
 					}
 				}
 
 				// drones
 				if (Main.ship.droneSet.size() > 0) {
 					if (Main.ship.dronesBySet) {
 						fw.write("\t" + "<droneList drones=\"" + Main.ship.drones + "\" load=\"" + Main.ship.droneSet.toArray()[0] + "\"/>" + lineDelimiter);
 					} else {
 						fw.write("\t" + "<droneList count=\"" + Main.ship.droneCount + "\" drones=\"" + Main.ship.drones + "\">" + lineDelimiter);
 						for (String blue : Main.ship.droneSet)
 							fw.write("\t\t" + "<drone name=\"" + blue + "\"/>" + lineDelimiter);
 						fw.write("\t" + "</droneList>" + lineDelimiter);
 					}
 				}
 
 				// health & power
 				fw.write("\t" + "<health amount=\"" + Main.ship.hullHealth + "\"/>" + lineDelimiter);
 				fw.write("\t" + "<maxPower amount=\"" + Main.ship.reactorPower + "\"/>" + lineDelimiter);
 
 				// crew
 				boolean auto = true;
 				for (String key : Main.ship.crewMap.keySet()) {
 					if (Main.ship.crewMaxMap.get(key) > 0) {
 						auto = false;
 						if (!key.equals("random")) {
 							fw.write("\t" + "<crewCount amount=\"" + Main.ship.crewMap.get(key) + "\" max=\"" + Main.ship.crewMaxMap.get(key) + "\" class=\"" + key + "\"/>");
 							fw.write(lineDelimiter);
 						} else {
 							fw.write("\t" + "<crewCount amount=\"" + Main.ship.crewMap.get(key) + "\" max=\"" + Main.ship.crewMaxMap.get(key) + "\"/>");
 							fw.write(lineDelimiter);
 						}
 					}
 				}
 				if (auto) {
 					fw.write("\t" + "<crewCount amount=\"0\" max=\"0\" class=\"human\"/>");
 					fw.write(lineDelimiter);
 				}
 
 				if (Main.isSystemAssigned(Systems.TELEPORTER))
 					fw.write("\t" + "<boardingAI>sabotage</boardingAI>" + lineDelimiter); // TODO ?
 
 				// augments
 				for (String aug : Main.ship.augmentSet) {
 					fw.write("\t" + "<aug name=\"" + aug + "\"/>" + lineDelimiter);
 				}
 
 				// overrides
 				/*
 				 * if (!isNull(Main.ship.shieldOverride))
 				 * fw.write("\t"+"<shieldImage>"+Main.ship.shieldOverride.substring(Main.ship.shieldOverride.lastIndexOf(ShipIO.pathDelimiter)+1,
 				 * Main.ship.shieldOverride.lastIndexOf("_"))+"</shieldImage>" + lineDelimiter);
 				 * if (!isNull(Main.ship.cloakOverride))
 				 * fw.write("\t"+"<cloakImage>"+Main.ship.cloakOverride.substring(Main.ship.cloakOverride.lastIndexOf(ShipIO.pathDelimiter)+1, Main.ship.cloakOverride.lastIndexOf("_"))+"</cloakImage>"
 				 * + lineDelimiter);
 				 */
 
 				fw.write("</shipBlueprint>");
 			}
 			exp.progressBar.setSelection(90);
 
 			fw.close();
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: export - file not found.");
 		} catch (IOException e) {
 			Main.debug("IO Exception while writing blueprint files:");
 			e.printStackTrace();
 		} finally {
 			try {
 				fw.close();
 			} catch (IOException e) {
 				Main.debug("IO Exception while closing export stream:");
 				e.printStackTrace();
 			}
 			fw = null;
 		}
 		exp.progressBar.setSelection(100);
 
 		exp.shell.dispose();
 		exp = null;
		lineDelimiter = "\n";
 
 		if (createFtl) {
 			// apacheZipToFTL(path, Main.ship.shipClass);
 			zipToFTL(path, Main.ship.shipClass);
 		}
 
 		if (deleteTemp) {
 			File f = new File(path + pathDelimiter + Main.ship.blueprintName);
 			deleteFolderContents(f);
 			rmdir(f);
 			f = null;
 		}
 	}
 
 	public static void rmdir(final File folder) {
 		if (folder.isDirectory()) {
 			File[] list = folder.listFiles();
 			if (list != null) {
 				for (int i = 0; i < list.length; i++) {
 					File tmpF = list[i];
 					if (tmpF.isDirectory()) {
 						rmdir(tmpF);
 					}
 					tmpF.delete();
 				}
 			}
 			if (!folder.delete()) {
 				Main.erDialog.add("Error: can't delete folder: " + folder);
 			}
 		}
 	}
 	
 	public static void zipToFTL(String path, String fileName) {
 		try {
 			FileOutputStream fos = new FileOutputStream((path + pathDelimiter + fileName + ".ftl"));
 			ZipOutputStream zos = new ZipOutputStream(fos);
 
 			// TODO set encoding
 
 			path = path + pathDelimiter + Main.ship.blueprintName;
 
 			zipDir(path, zos);
 
 			zos.close();
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: zip to ftl - file not found.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// PROJECT IO
 
 	public static void loadSystemImages(File fileToScan) {
 		for (FTLRoom r : Main.ship.rooms) {
 			if (r.sysBox != null)
 				loadSystemImage(r, fileToScan);
 		}
 	}
 
 	public static void loadSystemImage(FTLRoom r, File fileToScan) {
 		if (r.sysBox != null && r.interiorData != null) {
 			if (isNull(r.interiorData.interiorPath) && !r.getSystem().equals(Systems.TELEPORTER) && !r.getSystem().equals(Systems.EMPTY)) { // load default graphics (teleporter doesn't have default
 																																			// graphic)
 				r.interiorData.interiorPath = Main.resPath + pathDelimiter + "img" + pathDelimiter + "ship" + pathDelimiter + "interior" + pathDelimiter + "room_" + r.getSystem().toString().toLowerCase() + ".png";
 			}
 
 			if (Main.loadSystem && !isNull(r.interiorData.interiorPath))
 				r.sysBox.interior = Cache.checkOutImageAbsolute(r.sysBox, r.interiorData.interiorPath);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public static void loadShipProject(String path) {
 		ObjectInputStream ois = null;
 		try {
 			debug("Load ship project:");
 			debug("\tcreating stream...");
 			ois = new ObjectInputStream(new FileInputStream(path));
 			debug("\t\tdone");
 
 			debug("\treading ship...");
 			Main.ship = (FTLShip) ois.readObject();
 			debug("\t\tdone");
 
 			if (Main.ship.version < 6) {
 				Main.erDialog.add("Warning: compatibility with projects from versions 6-2 and earlier was broken in 18-2-13. Project can't be loaded.");
 				Main.ship = null;
 				return;
 			}
 
 			if (Main.ship.version < 9) {
 				for (FTLMount m : Main.ship.mounts)
 					m.mountPoint = new Point(0, 0);
 			}
 
 			if (Main.ship.version < 12) {
 				Main.ship.crewMaxMap = new HashMap<String, Integer>();
 
 				Main.ship.crewMaxMap.put("human", 0);
 				Main.ship.crewMaxMap.put("engi", 0);
 				Main.ship.crewMaxMap.put("energy", 0);
 				Main.ship.crewMaxMap.put("mantis", 0);
 				Main.ship.crewMaxMap.put("slug", 0);
 				Main.ship.crewMaxMap.put("rock", 0);
 				Main.ship.crewMaxMap.put("crystal", 0);
 				Main.ship.crewMaxMap.put("ghost", 0);
 			}
 
 			if (Main.ship.version < 13) {
 				for (FTLRoom r : Main.ship.rooms) {
 					r.setBorderThickness(2);
 					r.setBorderMode(PaintBox.BORDER_INSIDE);
 				}
 			}
 
 			if (Main.ship.version < 14) {
 				for (FTLGib g : Main.ship.gibs) {
 					if (g.getRotation() == 0)
 						g.setRotation(g.getOldRotation()); // get the old, deprecated value and assign it.
 				}
 				for (FTLMount m : Main.ship.mounts) {
 					if (m.getRotation() == 0)
 						m.setRotation(m.getOldRotation()); // get the old, deprecated value and assign it.
 				}
 				if (!Main.ship.isPlayer) {
 					Main.ship.crewMap.put("random", 0);
 					Main.ship.crewMaxMap.put("random", 0);
 					
 					for (Systems sys : Main.ship.slotDirMap.keySet()) {
 						if (Main.ship.slotDirMap.get(sys) == null)
 							Main.ship.slotDirMap.put(sys, Slide.UP);
 					}
 				}
 			}
 			
 			if (Main.ship.version < 15) {
 				int i = 0;
 				for (FTLMount m : Main.ship.mounts) {
 					for (FTLMount mt : Main.ship.mounts) {
 						if (m != mt && m.index == mt.index)
 							mt.index = i;
 					}
 					i++;
 				}
 			}
 
 			debug("\tloading linked ship images...");
 			loadShipImages(Main.ship, null);
 			debug("\t\tdone");
 
 			debug("\tloading weapon images...");
 			loadWeaponImages(Main.ship);
 			debug("\t\tdone");
 
 			debug("\tremapping mounts to weapons...");
 			remapMountsToWeapons();
 			debug("\t\tdone");
 
 			debug("\tloading unserializable data...");
 			Main.loadUnserializable();
 			debug("\t\tdone");
 
 			if (Main.ship.version < 10) {
 				for (FTLRoom r : Main.ship.rooms)
 					if (r.sysBox != null)
 						r.interiorData.interiorPath = r.img;
 				for (FTLMount m : Main.ship.mounts)
 					m.imageLoc = new Point(0, 0);
 			}
 
 			if (Main.ship.version < 11) {
 				for (FTLMount m : Main.ship.mounts)
 					m.animPos = new Point(0, 0);
 			}
 
 			if (Main.ship.isPlayer) {
 				debug("\tloading room interiors' images...");
 				loadSystemImages(null);
 				debug("\t\tdone");
 			}
 
 			for (FTLRoom r : Main.ship.rooms)
 				if (r.sysBox != null)
 					r.sysBox.setAvailable(Main.ship.startMap.get(r.getSystem()));
 
 			debug("\tregistering items for painter...");
 			Main.registerItemsForPainter();
 			debug("\t\tdone");
 			debug("\tupdating painter...");
 			Main.updatePainter();
 			debug("\t\tdone");
 
 			Main.undoManager.discardAllEdits();
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load ship project - file not found.");
 		} catch (ClassNotFoundException e) {
 			Main.erDialog.add("Error: load ship project - class not found. File does not contain the FTLShip class or comes from another version of the program.");
 		} catch (StreamCorruptedException e) {
 			Main.erDialog.add("Error: load ship project - file is corrupted - might have been manually changed.");
 		} catch (EOFException e) {
 			Main.erDialog.add("Error: load ship project - end of file reached - file is corrupted, probably due to an error during its saving.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (ois != null)
 					ois.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void saveShipProject(String path) {
 		try {
 			debug("Save ship project:");
 			debug("\tcreating stream...");
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			debug("\t\tdone");
 
 			debug("\tstripping unserializable data...");
 			Main.layeredPainter.setSuppressed(true);
 			Main.stripUnserializable();
 			debug("\t\tdone");
 
 			Main.ship.version = FTLShip.VERSION;
 
 			debug("\tdumping ship...");
 			oos.writeObject(Main.ship);
 			debug("\t\tdone");
 
 			oos.close();
 
 			debug("\tloading unserializable data...");
 			Main.loadUnserializable();
 			debug("\t\tdone");
 
 			debug("\tloading weapon images...");
 			loadWeaponImages(Main.ship);
 			debug("\t\tdone");
 
 			if (Main.ship.isPlayer) {
 				debug("\tloading system images...");
 				loadSystemImages(null);
 				debug("\t\tdone");
 			}
 
 			Main.savedSinceAction = true;
 			Main.shell.setText(Main.APPNAME + " - Ship Editor");
 
 			Main.layeredPainter.setSuppressed(false);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// ASK THE USER TO SPECIFY SAVE DIRECTORY
 	public static void askSaveDir() {
 		FileDialog dialog = new FileDialog(Main.shell, SWT.SAVE);
 		String[] filterExtensions = new String[] { "*.shp" };
 		dialog.setFilterExtensions(filterExtensions);
 		dialog.setFilterPath(Main.projectPath);
 		dialog.setFileName(Main.projectPath);
 
 		Main.currentPath = dialog.open();
 
 		if (Main.currentPath != null) {
 			Main.projectPath = Main.currentPath;
 			saveShipProject(Main.currentPath);
 		}
 	}
 
 	// ASK THE USER TO SPECIFY FILE TO LOAD
 	public static void askLoadDir() {
 		FileDialog dialog = new FileDialog(Main.shell, SWT.OPEN);
 		String[] filterExtensions = new String[] { "*.shp" };
 		dialog.setFilterExtensions(filterExtensions);
 		dialog.setFilterPath(Main.projectPath);
 		dialog.setFileName(Main.projectPath);
 
 		Main.currentPath = dialog.open();
 
 		if (Main.currentPath != null) {
 			String s = Main.currentPath;
 			Main.projectPath = Main.currentPath;
 			Main.mntmClose.notifyListeners(SWT.Selection, null);
 			Main.currentPath = s;
 			loadShipProject(Main.currentPath);
 		}
 	}
 
 	// ========================================
 	// === AUXILIARY
 
 	/**
 	 * Includes the modFile in the editor's archives.
 	 * Assumes any .txt, .xml or .append files belong into the /data/ directory, automatically merges .append files
 	 * 
 	 * @param modFile
 	 */
 	public static boolean includeMod(File modFile, boolean loadingPackage) {
 		String name = modFile.getName();
 		boolean success = false;
 
 		if (name.contains(".append")) {
 			FileReader fr = null;
 			FileWriter fw = null;
 			try {
 				File base = new File(Main.dataPath + ShipIO.pathDelimiter + name.replace(".append", ""));
 				if (base.exists()) {
 					fr = new FileReader(modFile);
 					fw = new FileWriter(base, true);
 
 					fw.write(ShipIO.lineDelimiter);
 
 					char[] buffer = new char[1024];
 					int chars = 0;
 					while (chars != -1) {
 						chars = fr.read(buffer);
 
 						// fw.write(buffer) writes empty indices as well; needed a way to work around it
 						for (int i = 0; i < chars; i++) {
 							fw.write(buffer[i]);
 						}
 					}
 					success = true;
 				} else {
 					Main.debug("Error: include mod - no base counterpart for the .append was found. Unable to merge.");
 					success = false;
 				}
 			} catch (IOException e) {
 				Main.debug("Error: include mod - IO exception while copying .append file.");
 				success = false;
 				e.printStackTrace();
 			} finally {
 				try {
 					if (fr != null)
 						fr.close();
 					if (fw != null)
 						fw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 		} else if (name.contains(".xml")) {
 			try {
 				java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(Main.dataPath).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 				success = true;
 			} catch (IOException e) {
 				Main.debug("Error: include mod - IO exception while copying .xml file.");
 				success = false;
 				e.printStackTrace();
 			}
 
 		} else if (name.contains(".txt")) {
 			try {
 				java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(Main.dataPath).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 				success = true;
 			} catch (IOException e) {
 				Main.debug("Error: include mod - IO exception while copying .txt file.");
 				success = false;
 				e.printStackTrace();
 			}
 
 		} else if (name.contains(".png") && !loadingPackage) {
 			IncludeAskDialog dialog = new IncludeAskDialog(Main.shell);
 			dialog.open();
 
 			int result = dialog.result;
 			String dest = Main.resPath + ShipIO.pathDelimiter + "img";
 
 			try {
 				if (result == -1 || result == 0) {
 					// canceled, abort
 					success = true;
 				} else if (result == 1) {
 					// ship-related
 					dest = dest + ShipIO.pathDelimiter + "ship";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 					success = true;
 				} else if (result == 2) {
 					// interior-related
 					dest = dest + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "interior";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 					success = true;
 				} else if (result == 3) {
 					// weapon-related
 					dest = dest + ShipIO.pathDelimiter + "weapons";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 					success = true;
 				}
 			} catch (IOException e) {
 				Main.debug("Error: include mod - IO exception while copying image.");
 				success = false;
 				e.printStackTrace();
 			}
 
 		} else if (name.contains(".png") && loadingPackage) {
 			String path = modFile.getParent();
 			String dest = Main.resPath + ShipIO.pathDelimiter + "img";
 			try {
 				if (path.contains("weapons")) {
 					dest = dest + ShipIO.pathDelimiter + "weapons";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 				} else if (path.contains("interior")) {
 					dest = dest + ShipIO.pathDelimiter + "ship" + ShipIO.pathDelimiter + "interior";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 				} else if (path.contains("ship")) {
 					dest = dest + ShipIO.pathDelimiter + "ship";
 					java.nio.file.Files.copy(Paths.get(modFile.getAbsolutePath()), Paths.get(dest).resolve(modFile.getName()), StandardCopyOption.REPLACE_EXISTING);
 				}
 			} catch (IOException e) {
 				Main.debug("Error: include mod - IO exception while copying images from .ftl package.");
 				success = false;
 				e.printStackTrace();
 			}
 
 			success = true;
 
 		} else if (name.contains(".ftl")) {
 			Main.unpackFTL(modFile.getAbsolutePath());
 			Main.debug("Merging...");
 			mergeTemporary(Main.temporaryFiles);
 			Main.debug("/tdone");
 			Main.temporaryFilesInUse = false;
 			Main.deleteTemporary();
 			success = true;
 		}
 
 		return success;
 	}
 
 	private static void mergeTemporary(File folder) {
 		File[] files = folder.listFiles();
 		if (files != null) { // some JVMs return null for empty dirs
 			for (File f : files) {
 				if (!f.isDirectory()) {
 					includeMod(f, true);
 				} else {
 					mergeTemporary(f);
 				}
 			}
 		}
 	}
 
 	public static void fetchShipNames() {
 		loadDeclarationsFromFile(null);
 
 		for (String s : playerBlueprintNames) {
 			playerShipNames.put(s, getShipName(s));
 		}
 		for (String s : enemyBlueprintNames) {
 			enemyShipNames.put(s, getShipName(s));
 		}
 		for (String s : otherBlueprintNames) {
 			otherShipNames.put(s, getShipName(s));
 		}
 		namesFetched = true;
 	}
 
 	public static String getShipName(String blueprintName, File fileToScan, int blueprintCount) {
 		String name = "";
 		FileReader fr;
 		Scanner sc = null;
 		Pattern pattern;
 		Matcher matcher;
 		String s;
 		int count = 0;
 
 		try {
 			fr = new FileReader(fileToScan);
 			sc = new Scanner(fr);
 			sc.useDelimiter(Pattern.compile(lineDelimiter));
 
 			scan: while (sc.hasNext()) {
 				s = sc.next();
 				pattern = Pattern.compile(".*?(<shipBlueprint name=\")" + blueprintName + "(\".*?)");
 				matcher = pattern.matcher(s);
 				if (matcher.find() && count < blueprintCount) {
 					count++;
 					if (count == blueprintCount) {
 						boolean nClass = false;
 						boolean nName = false;
 
 						name: while (sc.hasNext() && !s.contains("</shipBlueprint>")) {
 							s = sc.next();
 							pattern = Pattern.compile(".*?<class>(.*?)</class>");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								name += matcher.group(1);
 								nClass = true;
 							}
 
 							pattern = Pattern.compile(".*?<name>(.*?)</name>");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								name += " \"" + matcher.group(1) + "\"";
 								nName = true;
 							}
 							if (nClass && nName)
 								break name;
 						}
 					}
 				} else if (count > blueprintCount) {
 					break scan;
 				}
 			}
 		} catch (IOException e) {
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 
 		return name;
 	}
 
 	public static String getShipName(String blueprintName) {
 		FileReader fr;
 		Scanner sc;
 		Pattern pattern;
 		Matcher matcher;
 		String s = null;
 		String name = null;
 
 		try {
 			fr = new FileReader(Main.dataPath + pathDelimiter + "autoBlueprints.xml");
 			sc = new Scanner(fr);
 			sc.useDelimiter("\n");
 
 			scan: while (sc.hasNext()) {
 				s = sc.next();
 				pattern = Pattern.compile("(<shipBlueprint name=\")" + blueprintName + "(\".*?)");
 				matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					while (sc.hasNext() && !s.contains("</shipBlueprint>")) {
 						s = sc.next();
 						pattern = Pattern.compile("(<class>)(.*?)(</class>)");
 						matcher = pattern.matcher(s);
 						if (matcher.find()) {
 							name = matcher.group(2);
 							break scan;
 						}
 					}
 				}
 			}
 
 			sc.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (isNull(name)) {
 			try {
 				fr = new FileReader(Main.dataPath + pathDelimiter + "blueprints.xml");
 				sc = new Scanner(fr);
 				sc.useDelimiter("\n");
 
 				scan: while (sc.hasNext()) {
 					s = sc.next();
 					pattern = Pattern.compile("(<shipBlueprint name=\")(.*?)(\" layout=\")(.*?)(\" img=\")(.*?)(\">)");
 					matcher = pattern.matcher(s);
 					if (matcher.find() && s.contains(blueprintName)) {
 						seek: while (sc.hasNext() && !s.contains("</blueprintList>")) {
 							s = sc.next();
 							pattern = Pattern.compile(".*?(<name>)(.*?)(</name>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								name = matcher.group(2);
 								break seek;
 							}
 						}
 						break scan;
 					}
 				}
 				sc.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return name;
 	}
 
 	/** Only to be called when NO SHIP IS OPENED, may crash otherwise. */
 	public static void clearMaps() {
 		weaponMap.clear();
 		droneMap.clear();
 		augMap.clear();
 		weaponSetMap.clear();
 		droneSetMap.clear();
 
 		/*
 		 * playerShipNames.clear();
 		 * enemyShipNames.clear();
 		 * otherShipNames.clear();
 		 */
 	}
 
 	/** Only to be called after entries have been put into current maps. */
 	public static void clearOldMaps() {
 		oldWeaponMap.clear();
 		oldDroneMap.clear();
 		oldAugMap.clear();
 		oldWeaponSetMap.clear();
 		oldDroneSetMap.clear();
 	}
 
 	/**
 	 * Scans the supplied file, looking for blueprints and declarations of ships, weapons, drones and augments to load and make available to the user.
 	 * 
 	 * @param fileToScan
 	 *            file that is to be scanned; when set to null, performs the scan on Superluminal's own extracted archives
 	 */
 	public static void loadDeclarationsFromFile(File fileToScan) {
 		Scanner sc = null;
 		String s;
 		Pattern pattern;
 		Matcher matcher;
 		HashSet<String> tempSet;
 		FTLItem tempItem;
 
 		namesFetched = false;
 
 		boolean ignoreNextTag = false;
 		/**
 		 * This flag is changed once the Crystal Bomber ship is reached, since it is the last of the default ships.
 		 * All ships below it are then classified as "other", not "enemy".
 		 */
 		boolean defaultShips = true;
 
 		boolean scanDefault = fileToScan == null;
 
 		if (scanDefault) {
 			Main.debug("Load declarations - starting scanning of own archives...", true);
 		} else {
 			Main.debug("Load declarations - starting scanning of external files...", true);
 		}
 
 		String[] playerBlueprints = { "PLAYER_SHIP_HARD", "PLAYER_SHIP_HARD_2",
 				"PLAYER_SHIP_MANTIS", "PLAYER_SHIP_MANTIS_2",
 				"PLAYER_SHIP_STEALTH", "PLAYER_SHIP_STEALTH_2",
 				"PLAYER_SHIP_CIRCLE", "PLAYER_SHIP_CIRCLE_2",
 				"PLAYER_SHIP_FED", "PLAYER_SHIP_FED_2",
 				"PLAYER_SHIP_JELLY", "PLAYER_SHIP_JELLY_2",
 				"PLAYER_SHIP_ROCK", "PLAYER_SHIP_ROCK_2",
 				"PLAYER_SHIP_ENERGY", "PLAYER_SHIP_ENERGY_2",
 				"PLAYER_SHIP_CRYSTAL", "PLAYER_SHIP_CRYSTAL_2" };
 
 		playerList.clear();
 		for (String blue : playerBlueprints)
 			playerList.add(blue);
 
 		playerBlueprints = null;
 
 		try {
 			if (scanDefault)
 				fileToScan = new File(Main.dataPath + pathDelimiter + "autoBlueprints.xml");
 			InputStream is = new FileInputStream(fileToScan);
 			String contents = convertStreamToString(is);
 			try {
 				is.close();
 			} catch (IOException e) {
 			}
 			
 			contents.replaceAll("(?s)<!--.*?-->", "");
 
 			sc = new Scanner(contents);
 			sc.useDelimiter("\n");
 
 			Main.debug("Load declarations - scanning as autoBlueprints.xml...", IOdebug);
 			while (sc.hasNext()) {
 				s = sc.next();
 
 				ignoreNextTag = isCommentedOut(s);
 
 				// load section names
 				pattern = Pattern.compile("(<shipBlueprint name=\")(.*?)(\".*?$)"); // layout=\")(.*?)(\" img=\")(.*?)(\"\\s*?>)
 				matcher = pattern.matcher(s);
 				if (matcher.find() && !ignoreNextTag) {
 					debug("\tfound a ship blueprint tag: " + matcher.group(2));
 					if (defaultShips) {
 						enemyBlueprintNames.add(matcher.group(2));
 						if (matcher.group(2).equals("CRYSTAL_BOMBER") || !scanDefault)
 							defaultShips = false;
 					} else {
 						otherBlueprintNames.add(matcher.group(2));
 					}
 				} else if (!ignoreNextTag) {
 					pattern = Pattern.compile("(<blueprintList name=\")(.*?)(\"\\s*?>)");
 					matcher = pattern.matcher(s);
 					if (matcher.find()) {
 						debug("\tfound a blueprint list tag: " + matcher.group(2));
 						tempSet = new HashSet<String>();
 
 						String set = matcher.group(2);
 
 						debug("\t\tlist's contents:");
 						while (sc.hasNext() && !s.contains("</blueprintList>")) {
 							s = sc.next();
 
 							ignoreNextTag = isCommentedOut(s);
 
 							pattern = Pattern.compile("(<name>)(.*?)(</name>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find() && !ignoreNextTag) {
 								debug("\t\t- " + matcher.group(2));
 								tempItem = new FTLItem();
 								tempItem.blueprint = matcher.group(2);
 								tempSet.add(tempItem.blueprint);
 							}
 						}
 
 						if (set.contains("DRONE")) {
 							droneSetMap.put(set, tempSet);
 						} else if (set.contains("WEAPON")) {
 							weaponSetMap.put(set, tempSet);
 						}
 					}
 				}
 			}
 			if (IOdebug) {
 				Main.debug("Load declarations - scanning as autoBlueprints.xml - done", true);
 			} else {
 				Main.debug(" done", true);
 			}
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load declarations - autoBlueprints.xml file not found [" + fileToScan.getAbsolutePath() + "]");
 			Main.debug("Error: load declarations - autoBlueprints.xml - file not found [" + fileToScan.getAbsolutePath() + "]", true);
 			e.printStackTrace();
 		} catch (NoSuchElementException e) {
 			Main.erDialog.add("Error: load declarations - autoBlueprints.xml - end of file reached");
 			Main.debug("Error: load declarations - autoBlueprints.xml - end of file reached", true);
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 
 		try {
 			if (scanDefault)
 				fileToScan = new File(Main.dataPath + pathDelimiter + "blueprints.xml");
 			InputStream is = new FileInputStream(fileToScan);
 			String contents = convertStreamToString(is);
 			try {
 				is.close();
 			} catch (IOException e) {
 			}
 			
 			contents.replaceAll("(?s)<!--.*?-->", "");
 
 			sc = new Scanner(contents);
 			sc.useDelimiter("\n");
 
 			Main.debug("Load declarations - scanning as blueprints.xml...", IOdebug);
 			while (sc.hasNext()) {
 				s = sc.next();
 
 				// weapons
 				pattern = Pattern.compile("(<weaponBlueprint name=\")(.*?)(\">)");
 				matcher = pattern.matcher(s);
 				if (matcher.find() && !matcher.group(2).equals("CRYSTAL_1") && !matcher.group(2).equals("ARTILLERY_FED")) { // the two are not intended to be loaded, they have no images
 					debug("\tfound a weapon blueprint tag: " + matcher.group(2));
 					tempItem = new FTLItem();
 					tempItem.blueprint = matcher.group(2);
 
 					if (tempItem != null) {
 						while (sc.hasNext() && !s.contains("</weaponBlueprint>")) {
 							s = sc.next();
 							pattern = Pattern.compile("(<type>)(.*?)(</type>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find())
 								tempItem.category = matcher.group(2);
 
 							pattern = Pattern.compile("(<title>)(.*?)(</title>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find())
 								tempItem.name = matcher.group(2);
 
 							pattern = Pattern.compile("(<weaponArt>)(.*?)(</weaponArt>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								tempItem.img = matcher.group(2);
 							}
 						}
 						weaponMap.put(tempItem.blueprint, tempItem);
 					}
 				}
 
 				// drones
 				pattern = Pattern.compile("(<droneBlueprint name=\")(.*?)(\">)");
 				matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					debug("\tfound a drone blueprint tag: " + matcher.group(2));
 					tempItem = new FTLItem();
 					tempItem.blueprint = matcher.group(2);
 
 					if (tempItem != null) {
 						while (sc.hasNext() && !s.contains("</droneBlueprint>")) {
 							s = sc.next();
 							pattern = Pattern.compile("(<type>)(.*?)(</type>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find())
 								tempItem.category = matcher.group(2);
 
 							pattern = Pattern.compile("(<title>)(.*?)(</title>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find())
 								tempItem.name = matcher.group(2);
 						}
 						droneMap.put(tempItem.blueprint, tempItem);
 					}
 				}
 
 				// augments
 				ignoreNextTag = isCommentedOut(s);
 
 				pattern = Pattern.compile("(<augBlueprint name=\")(.*?)(\">)");
 				matcher = pattern.matcher(s);
 				if (matcher.find() && !ignoreNextTag) {
 					debug("\tfound an augment blueprint tag: " + matcher.group(2));
 					tempItem = new FTLItem();
 					tempItem.blueprint = matcher.group(2);
 
 					if (tempItem != null) {
 						while (sc.hasNext() && !s.contains("</augBlueprint>")) {
 							s = sc.next();
 
 							pattern = Pattern.compile("(<title>)(.*?)(</title>)");
 							matcher = pattern.matcher(s);
 							if (matcher.find()) {
 								tempItem.name = matcher.group(2);
 							}
 						}
 						augMap.put(tempItem.blueprint, tempItem);
 					}
 				}
 
 				// ships
 				ignoreNextTag = isCommentedOut(s);
 
 				pattern = Pattern.compile("(<shipBlueprint name=\")(.*?)(\".*?$)"); // layout=\")(.*?)(\" img=\")(.*?)(\">)
 				matcher = pattern.matcher(s);
 				if (matcher.find() && !ignoreNextTag) {
 					debug("\tfound a ship blueprint tag: " + matcher.group(2));
 					if (playerList.contains(matcher.group(2))) {
 						playerBlueprintNames.add(matcher.group(2));
 					} else {
 						otherBlueprintNames.add(matcher.group(2));
 					}
 				}
 			}
 
 			// removing blueprints that are problematic to load or not intended to be used at all
 			// (mostly due to scripted behaviour within the game)
 			otherBlueprintNames.remove("PLAYER_SHIP_EASY"); // not used, commented out
 			otherBlueprintNames.remove("TUTORIAL_PIRATE"); // first enemy ship in tutorial
 			otherBlueprintNames.remove("PLAYER_SHIP_TUTORIAL"); // player's ship in tutorial
 			otherBlueprintNames.remove("IMPOSSIBLE_PIRATE"); // second enemy ship in tutorial
 			otherBlueprintNames.remove("DEFAULT"); // default ship blueprint, labelled as "crash protection", better to not mess with it
 			otherBlueprintNames.remove("LONG_ELITE_MED"); // not used
 			otherBlueprintNames.remove("LONG_ELITE_HARD"); // not used
 			if (Main.forbidBossLoading) {
 				otherBlueprintNames.remove("BOSS_1"); // boss ship, scripted behaviour
 				otherBlueprintNames.remove("BOSS_2"); // --- || ---
 				otherBlueprintNames.remove("BOSS_3"); // --- || ---
 			}
 
 			if (IOdebug) {
 				Main.debug("Load declarations - scanning as blueprints.xml - done", true);
 			} else {
 				Main.debug(" done", true);
 			}
 		} catch (FileNotFoundException e) {
 			Main.erDialog.add("Error: load declarations - blueprints.xml file not found [" + fileToScan.getAbsolutePath() + "]");
 			Main.debug("Error: load declarations - blueprints.xml - file not found [" + fileToScan.getAbsolutePath() + "]", true);
 			e.printStackTrace();
 		} catch (NoSuchElementException e) {
 			Main.erDialog.add("Error: load declarations - blueprints.xml - end of file reached");
 			Main.debug("Error: load declarations - blueprints.xml - end of file reached", true);
 		} finally {
 			if (sc != null)
 				sc.close();
 		}
 	}
 
 	public static FTLItem getItem(String blueprint) {
 		FTLItem it = null;
 		for (String b : weaponMap.keySet()) {
 			it = weaponMap.get(b);
 			if (it.blueprint.equals(blueprint))
 				return it;
 		}
 		for (String b : droneMap.keySet()) {
 			it = droneMap.get(b);
 			if (it.blueprint.equals(blueprint))
 				return it;
 		}
 		for (String b : augMap.keySet()) {
 			it = augMap.get(b);
 			if (it.blueprint.equals(blueprint))
 				return it;
 		}
 		return null;
 	}
 
 	public static void zipDir(String source, ZipOutputStream zos) {
 		FileInputStream fis = null;
 		ZipEntry ze = null;
 		File zipDir, file = null;
 
 		try {
 			zipDir = new File(source);
 
 			String[] dirList = zipDir.list();
 			byte[] readBuffer = new byte[2156];
 			int bytesIn = 0;
 
 			for (int i = 0; i < dirList.length; i++) {
 				file = new File(zipDir, dirList[i]);
 				if (file.isDirectory()) {
 					String filePath = file.getPath().replace("\\", "/");
 					zipDir(filePath, zos);
 					continue;
 				}
 
 				fis = new FileInputStream(file);
 				file = new File(file.getPath().substring(file.getPath().indexOf(Main.ship.blueprintName) + Main.ship.blueprintName.length() + 1).replace("\\", "/"));
 				ze = new ZipEntry(file.getPath().replace("\\", "/"));
 				zos.putNextEntry(ze);
 
 				while ((bytesIn = fis.read(readBuffer)) != -1) {
 					zos.write(readBuffer, 0, bytesIn);
 				}
 
 				fis.close();
 			}
 		} catch (Exception e) {
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	public static void unzipFileToDirectory(ZipFile zipFile, File destinationDir) {
 		Enumeration<? extends ZipEntry> files = zipFile.entries();
 		File f = null;
 		FileOutputStream fos = null;
 		InputStream eis = null;
 
 		while (files.hasMoreElements()) {
 			try {
 				ZipEntry entry = (ZipEntry) files.nextElement();
 				eis = zipFile.getInputStream(entry);
 				byte[] buffer = new byte[1024];
 				int bytesRead = 0;
 
 				f = new File(destinationDir.getAbsolutePath() + File.separator + entry.getName());
 
 				if (entry.isDirectory()) {
 					f.mkdirs();
 					continue;
 				} else {
 					f.getParentFile().mkdirs();
 					f.createNewFile();
 				}
 
 				fos = new FileOutputStream(f);
 
 				while ((bytesRead = eis.read(buffer)) != -1) {
 					fos.write(buffer, 0, bytesRead);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				continue;
 			} finally {
 				try {
 					if (fos != null)
 						fos.close();
 					if (eis != null)
 						eis.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	public static void readZip(String zipFile) {
 		try {
 			ZipFile zf = new ZipFile(zipFile);
 			Enumeration<? extends ZipEntry> entries = zf.entries();
 
 			while (entries.hasMoreElements()) {
 				ZipEntry ze = (ZipEntry) entries.nextElement();
 				long size = ze.getSize();
 				if (size > 0) {
 					BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
 					br.close();
 				}
 			}
 			zf.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void resetUnnecessaryLinkIds() {
 		if (shipBeingLoaded == null)
 			return;
 		int l, r;
 		for (FTLDoor d : shipBeingLoaded.doors) {
 			l = shipBeingLoaded.findLeftRoom(d);
 			r = shipBeingLoaded.findRightRoom(d);
 			// reset whenever the door is linked to any adjacent room
 			// normally we'd reset leftId only if it was equal to l,
 			// but FTL itself is very inconsistent in this regard
 			// (rightId links to left room, leftId links to right room...)
 			if (d.leftId == l || d.leftId == r || d.leftId == -1)
 				d.leftId = -2;
 			if (d.rightId == l || d.rightId == r || d.rightId == -1)
 				d.rightId = -2;
 		}
 	}
 	
 	/**
 	 * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
 	 * The stream the function receives is closed later on.
 	 */
 	@SuppressWarnings("resource")
 	public static String convertStreamToString(InputStream is) {
 		Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
 		return s.hasNext() ? s.next() : "";
 	}
 }

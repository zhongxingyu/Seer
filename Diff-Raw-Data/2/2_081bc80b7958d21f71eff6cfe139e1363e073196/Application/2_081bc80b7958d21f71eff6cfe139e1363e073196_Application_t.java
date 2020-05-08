 /*
  *	ePad 2.0 Multitouch Customizable Painting Platform
  *  Copyright (C) 2012 Dmitry Pyryeskin and Jesse Hoey, University of Waterloo
  *  
  *  This file is part of ePad 2.0.
  *
  *  ePad 2.0 is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  ePad 2.0 is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with ePad 2.0. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.uwaterloo.epad;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.ResourceBundle;
 
 import javax.xml.transform.TransformerException;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PGraphics;
 import processing.core.PImage;
 import vialab.SMT.TouchClient;
 import vialab.SMT.TouchDraw;
 import vialab.SMT.TouchSource;
 import vialab.SMT.Zone;
 import ca.uwaterloo.epad.painting.Brush;
 import ca.uwaterloo.epad.painting.Eraser;
 import ca.uwaterloo.epad.painting.Paint;
 import ca.uwaterloo.epad.prompting.PromptManager;
 import ca.uwaterloo.epad.ui.ApplicationState;
 import ca.uwaterloo.epad.ui.Button;
 import ca.uwaterloo.epad.ui.Canvas;
 import ca.uwaterloo.epad.ui.Container;
 import ca.uwaterloo.epad.ui.Drawer;
 import ca.uwaterloo.epad.ui.FileBrowser;
 import ca.uwaterloo.epad.ui.ResetDialog;
 import ca.uwaterloo.epad.ui.FileBrowser.FileButton;
 import ca.uwaterloo.epad.ui.MoveableItem;
 import ca.uwaterloo.epad.ui.SaveDialog;
 import ca.uwaterloo.epad.ui.SplashScreen;
 import ca.uwaterloo.epad.util.DrawingPrinter;
 import ca.uwaterloo.epad.util.Settings;
 import ca.uwaterloo.epad.util.TTSManager;
 import ca.uwaterloo.epad.xml.SaveFile;
 import ca.uwaterloo.epad.xml.SimpleMarshaller;
 import ca.uwaterloo.epad.xml.XmlAttribute;
 
 public class Application extends PApplet implements ActionListener {
 	private static final long serialVersionUID = -1354251777507926593L;
 
 	public static final int TOP_DRAWER = 0;
 	public static final int LEFT_DRAWER = 1;
 	public static final int RIGHT_DRAWER = 2;
 	
 	public static final String ITEM_ADDED = "item added";
 	public static final String ITEM_REMOVED = "item removed";
 	public static final String BRUSH_SELECTED = "brush selected";
 	public static final String PAINT_SELECTED = "paint selected";
 
 	// Colour scheme
 	@XmlAttribute public static int backgroundColour = 0xFFFFFFFF;
 	@XmlAttribute public static String backgroundImage = null;
 	@XmlAttribute public static int primaryColour = 0;
 	@XmlAttribute public static int secondaryColour = 0;
 	@XmlAttribute public static int textColour = 0;
 	@XmlAttribute public static int transparentColour = 0;
 	@XmlAttribute public static int transparentAlpha = 0;
 	@XmlAttribute public static int deleteColour = 0;
 
 	// GUI components
 	private static Brush selectedBrush;
 	private static Paint selectedPaint;
 	private static Drawer leftDrawer;
 	private static Drawer rightDrawer;
 	private static Drawer topDrawer;
 	private static Canvas canvas;
 	
 	// Misc variables
 	private static PFont font;
 	private static ResourceBundle uiStrings = ResourceBundle.getBundle("ca.uwaterloo.epad.res.UI", Settings.locale);
 	private static PImage bg;
 	private static long lastActionTime;
 	private static ApplicationState state;
 	
 	private static ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
 	private static Application instance;
 	
 	private static ArrayList<Brush> brushes = new ArrayList<Brush>();
 	private static ArrayList<Paint> paints = new ArrayList<Paint>();
 
 	public void setup() {
 		size(Settings.width, Settings.height, P3D);
 		frameRate(Settings.targetFPS);
 		
 		instance = this;
 		
 		font = createDefaultFont(20);
 		
 		// figure out the touch source
 		TouchSource source;
 		switch (Settings.touchSourse.toUpperCase()) {
 		case "TUIO_DEVICE": source = TouchSource.TUIO_DEVICE; break;
 		case "MOUSE": source = TouchSource.MOUSE; break;
 		case "WM_TOUCH": source = TouchSource.WM_TOUCH; break;
 		case "ANDROID": source = TouchSource.ANDROID; break;
 		case "SMART": source = TouchSource.SMART; break;
 		default: source = TouchSource.MOUSE;
 		}
 
 		TouchClient.init(this, source);
 		TouchClient.setWarnUnimplemented(false);
 		TouchClient.setDrawTouchPoints(TouchDraw.SMOOTH, 0);
 		
 		loadGUI();
 		loadLayout(Settings.dataFolder + Settings.defaultLayoutFile);
 		
 		PromptManager.init(this);
 		TTSManager.init();
 		setActionPerformed();
 		state = ApplicationState.IDLE;
 		SplashScreen.remove();
 	}
 
 	public void draw() {
 		background(backgroundColour);
 		if (backgroundImage != null && backgroundImage.length() > 0) {
 			imageMode(CORNER);
 			image(bg, 0, 0, displayWidth, displayHeight);
 		}
 		
 		if (Settings.showDebugInfo) {
 			String s1 = Math.round(frameRate) + "fps, # of zones: " + TouchClient.getZones().length;
 			String s2 = "brushes: " + brushes.size() + ", paints: " + paints.size();
 			
 			String s3 = "state: ";
 			switch(state) {
 			case RUNNING: s3 += "Running"; break;
 			case IDLE: s3 += "idle"; break;
 			case PAUSED: s3 += "paused"; break;
 			}
 			
 			text(s1, 10, 10);
 			text(s2, 10, 20);
 			text(s3, 10, 30);
 			
 			/*
 			text(s1, 10, height - 30);
 			text(s2, 10, height - 20);
 			text(s3, 10, height - 10);
 			
 			text(s1, width - 150, 10);
 			text(s2, width - 150, 20);
 			text(s3, width - 150, 30);
 			
 			text(s1, width - 150, height - 30);
 			text(s2, width - 150, height - 20);
 			text(s3, width - 150, height - 10);
 			*/
 		}
 		
 		if (getInactiveTime() >= Settings.resetPromptDelay && !ResetDialog.IsOnScreen())
 			TouchClient.add(new ResetDialog());
 	}
 	
 	public boolean sketchFullScreen() {
 		return true;
 	}
 	
 	private static void makeControlPanel(Container c) {
 		int w = 180;
 		int h = 70;
 		int x = 20;
 		int y = c.height - h - 60;
 		
 		Button b = new Button(x, y, w, h, uiStrings.getString("SavePaintingButton"), 20, font);
 		b.setStaticPressMethod("save", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		x += w + 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("LoadPaintingButton"), 20, font);
 		b.setStaticPressMethod("load", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		x += w + 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("ClearPaintingButton"), 20, font);
 		b.setStaticPressMethod("clearCanvas", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		x += w + 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("ResetButton"), 20, font);
 		b.setStaticPressMethod("resetToDefaults", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		x += w + 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("ColouringPagesButton"), 20, font);
 		b.setStaticPressMethod("colouringMode", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		x += w + 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("PrintButton"), 20, font);
 		b.setStaticPressMethod("print", Application.class);
 		b.setColourScheme(c.getPrimaryColour(), c.getSecondaryColour(), c.getSecondaryColour());
 		c.addItem(b);
 		
 		/*
 		x = instance.width - w - 20;
 		
 		b = new Button(x, y, w, h, uiStrings.getString("ExitButton"), 30, instance.createFont("Arial", 30));
 		b.setStaticPressMethod("closeApplication", Application.class);
 		b.setColourScheme(0xFFCC0000, 0xFFFF4444, 0xFFFF4444);
 		c.addItem(b);
 		*/
 	}
 
 	public void keyPressed() {
 		if (key == ' ') {
 			takeScreenshot();
 		}
 	}
 	
 	public static ApplicationState getState() {
 		return state;
 	}
 	
 	public static void pauseApplication() {
 		state = ApplicationState.PAUSED;
 		PromptManager.pause();
 	}
 	
 	public static void resumeApplication() {
 		if (state == ApplicationState.PAUSED)
 			PromptManager.resume();
 		else if (state == ApplicationState.IDLE)
 			PromptManager.reset();
 			
 		state = ApplicationState.RUNNING;
 	}
 	
 	public static void idleApplication() {
 		state = ApplicationState.IDLE;
 		PromptManager.pause();
 	}
 
 	public static void setSelectedPaint(Paint p) {
 		if (p == selectedPaint) return;
 		
 		if (selectedPaint != null)
 			selectedPaint.deselect();
 		selectedPaint = p;
 		if (selectedPaint != null)
 			selectedPaint.select();
 		
 		notifyListeners(p, PAINT_SELECTED);
 	}
 
 	public static Paint getSelectedPaint() {
 		return selectedPaint;
 	}
 	
 	public static ArrayList<Paint> getAllPaints() {
 		return paints;
 	}
 
 	public static void setSelectedBrush(Brush b) {
 		if (b == selectedBrush) return;
 		
 		if (selectedBrush != null)
 			selectedBrush.deselect();
 		selectedBrush = b;
 		if (selectedBrush != null)
 			selectedBrush.select();
 		
 		notifyListeners(b, BRUSH_SELECTED);
 	}
 
 	public static Brush getSelectedBrush() {
 		return selectedBrush;
 	}
 	
 	public static ArrayList<Brush> getAllBrushes() {
 		return brushes;
 	}
 
 	public static Zone[] getChildren() {
 		return TouchClient.getZones();
 	}
 
 	public static void setDrawer(Drawer newDrawer, int drawerId) {
 		switch (drawerId) {
 		case TOP_DRAWER:
 			if (topDrawer != null) TouchClient.remove(topDrawer);
 			topDrawer = newDrawer;
 			break;
 		case LEFT_DRAWER:
 			if (leftDrawer != null) TouchClient.remove(leftDrawer);
 			leftDrawer = newDrawer;
 			break;
 		case RIGHT_DRAWER:
 			if (rightDrawer != null) TouchClient.remove(rightDrawer);
 			rightDrawer = newDrawer;
 			break;
 		default: return;
 		}
 		TouchClient.add(newDrawer);
 	}
 	
 	public static Drawer getDrawer(int drawerId) {
 		switch(drawerId) {
 		case TOP_DRAWER: return topDrawer;
 		case LEFT_DRAWER: return leftDrawer;
 		case RIGHT_DRAWER: return rightDrawer;
 		default: return null;
 		}
 	}
 	
 	public static void setCanvas(Canvas newCanvas) {
 		if (canvas != null) {
 			TouchClient.remove(canvas);
 		}
 		canvas = newCanvas;
 		
 		TouchClient.add(canvas);
 	}
 	
 	public static Canvas getCanvas() {
 		return canvas;
 	}
 	
 	public static boolean isItemAboveDrawer(MoveableItem item) {
 		if (leftDrawer != null && leftDrawer.isItemAbove(item))
 			return true;
 		else if (rightDrawer != null && rightDrawer.isItemAbove(item))
 			return true;
 		else if (topDrawer != null && topDrawer.isItemAbove(item))
 			return true;
 		else
 			return false;
 	}
 	
 	public static void setActionPerformed() {
 		if (state == ApplicationState.IDLE) {
 			state = ApplicationState.RUNNING;
 			PromptManager.reset();
 		}
 		lastActionTime = System.currentTimeMillis();
 	}
 	
 	public static long getInactiveTime() {
 		return System.currentTimeMillis() - lastActionTime;
 	}
 	
 	public static void addItem(MoveableItem item) {
 		if (item instanceof Paint)
 			paints.add((Paint)item);
 		else if (item instanceof Brush && !(item instanceof Eraser))
 			brushes.add((Brush)item);
 		
 		TouchClient.add(item);
 		notifyListeners(item, ITEM_ADDED);
 	}
 	
 	public static void removeItem(MoveableItem item) {
 		if (item instanceof Paint)
 			paints.remove(item);
 		else if (!(item instanceof Eraser))
 			brushes.remove(item);
 		
 		TouchClient.remove(item);
 		notifyListeners(item, ITEM_REMOVED);
 	}
 	
 	private static void notifyListeners(Object source, String message) {
 		for (int i = 0; i < listeners.size(); i++) {
 			ActionListener listener = listeners.get(i);
 			if (listener != null)
 				listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_FIRST, message));
 			else
 				System.err.println("Application.notifyListeners(): null list element " + i);
 		}
 	}
 
 	public static void addListener(ActionListener listener) {
 		if (!listeners.contains(listener))
 			listeners.add(listener);
 	}
 	
 	public static boolean removeListener(ActionListener listener) {
 		return listeners.remove(listener);
 	}
 	
 	public static void loadGUI() {
 		try {
 			SimpleMarshaller.unmarshallGui(instance, new File(Settings.dataFolder + Settings.guiFile));
 		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
 			e.printStackTrace();
 			instance.exit();
 		}
 
 		if (backgroundImage != null && backgroundImage.length() > 0) {
 			bg = instance.loadImage(Settings.dataFolder + backgroundImage);
 		}
 		
 		// create default canvas (in case it is not specified in the layout file
 		setCanvas(new Canvas((instance.width-800)/2, (instance.height-600)/2, 800, 600, 255));
 		
 		// create control buttons
 		makeControlPanel(topDrawer.getContainer());
 	}
 	
 	public static void clearWorkspace() {
 		brushes.clear();
 		paints.clear();
 		
 		// remove all zones
 		Zone[] zones = TouchClient.getZones();
 		for (int i=0; i < zones.length; i++) {
 			Zone z = zones[i];
 			TouchClient.remove(z);
 		}
 		
 		loadGUI();
 	}
 	
 	public static void loadSave(SaveFile save) {
 		clearWorkspace();
 		loadLayout(save.layoutPath);
 		canvas.clearAndLoad(save.drawingPath);
 		
 		// Put drawers on top
 		if (leftDrawer != null)
 			TouchClient.putZoneOnTop(leftDrawer);
 		if (rightDrawer != null)
 			TouchClient.putZoneOnTop(rightDrawer);
 		if (topDrawer != null) {
 			TouchClient.putZoneOnTop(topDrawer);
 		}
 		
 		setSelectedBrush(getAllBrushes().get(0));
 		setSelectedPaint(getAllPaints().get(0));
 		
 		PromptManager.reset();
 	}
 	
 	public static void saveLayout(String filename) {
 		try {
 			SimpleMarshaller.marshallLayout(new File(filename));
 			System.out.println("Layout saved: " + filename);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void loadLayout(String filename) {
 		try {
 			SimpleMarshaller.unmarshallLayout(new File(filename));
 		} catch (IllegalArgumentException | IllegalAccessException | TransformerException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
 			e.printStackTrace();
 		}
 		
 		// Put drawers on top
 		if (leftDrawer != null)
 			TouchClient.putZoneOnTop(leftDrawer);
 		if (rightDrawer != null)
 			TouchClient.putZoneOnTop(rightDrawer);
 		if (topDrawer != null) {
 			TouchClient.putZoneOnTop(topDrawer);
 		}
 		
 		setSelectedBrush(getAllBrushes().get(0));
 		setSelectedPaint(getAllPaints().get(0));
 	}
 	
 	public static void takeScreenshot() {
 		Date now = new Date();
 		SimpleDateFormat sdt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS");
 
 		String filename = "data\\screenshot_" + sdt.format(now) + ".png";
 
 		PGraphics pg;
 		pg = instance.createGraphics(instance.width, instance.height, P3D);
 		pg.beginDraw();
 		instance.draw();
 		TouchClient.draw();
 		PromptManager.draw();
 		pg.endDraw();
 		
 		if (pg.save(filename))
 			System.out.println("Screenshot saved: " + filename);
 		else
 			System.err.println("Failed to save screenshot");
 	}
 	
 	public static void save() {
 		TouchClient.add(new SaveDialog());
 	}
 	
 	public static void load() {
 		FileBrowser saveFileBrowser = new FileBrowser(uiStrings.getString("SaveFileBrowserHeaderText"), Settings.saveFolder,
 				SaveFile.SAVE_FILE_EXT, Settings.fileBrowserColumns, Settings.fileBrowserRows);
 		saveFileBrowser.addListener(instance);
 		TouchClient.add(saveFileBrowser);
 	}
 	
 	public static void clearCanvas() {
 		canvas.clear();
 	}
 	
 	public static void resetToDefaults() {
 		clearWorkspace();
 		loadLayout(Settings.dataFolder + Settings.defaultLayoutFile);
 		// Put drawers on top
 		if (leftDrawer != null)
 			TouchClient.putZoneOnTop(leftDrawer);
 		if (rightDrawer != null)
 			TouchClient.putZoneOnTop(rightDrawer);
 		if (topDrawer != null) {
 			TouchClient.putZoneOnTop(topDrawer);
 		}
 		
 		setSelectedBrush(getAllBrushes().get(0));
 		setSelectedPaint(getAllPaints().get(0));
 		setActionPerformed();
 		
 		PromptManager.reset();
 		state = ApplicationState.IDLE;
 	}
 	
 	public static void colouringMode() {
 		FileBrowser imageFileBrowser = new FileBrowser(uiStrings.getString("ImageFileBrowserHeaderText"), Settings.colouringFolder,
 				null, Settings.fileBrowserColumns, Settings.fileBrowserRows);
 		imageFileBrowser.addListener(instance);
 		TouchClient.add(imageFileBrowser);
 		//canvas.toggleOverlay();
 	}
 	
 	public static void print() {
		new Thread(new DrawingPrinter(canvas.getDrawing(false), Settings.showPrintDialog)).start();
 	}
 	
 	public static void closeApplication() {
 		TTSManager.dispose();
 		instance.exit();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(FileBrowser.FILE_SELECTED)) {
 			FileButton fb = (FileButton) e.getSource();
 			if (fb.fileType == FileBrowser.FILE_TYPE_IMAGE) {
 				getCanvas().setOverlayImage(fb.filePath);
 			} else if (fb.fileType == FileBrowser.FILE_TYPE_SAVE) {
 				loadSave(fb.save);
 			}
 		}
 	}
 }

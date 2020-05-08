 package mobiledisco;
 
 import TUIO.*;
 import ddf.minim.*;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PImage;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Vector;
 
 
 public class MobileDisco extends PApplet {
 	
 	public static void main(String _args[]) {
 		boolean FULLSCREEN = true;
 		if (FULLSCREEN  == true) {
			PApplet.main(new String[] { "--present",  "--hide-stop", mobiledisco.MobileDisco.class.getName() });
 		} else {
 			PApplet.main(new String[] { mobiledisco.MobileDisco.class.getName() });
 		}
 
 	}
 	
 	private static final long serialVersionUID = 1L;
 
 	TuioProcessing tuioClient;
 	public Minim m = new Minim(this);
 	public PFont font;
 	public ArrayList trackList;
 	public ArrayList playList;
 
 	PImage b;
 	PImage[] cursor;
 
 	////////////////////// SETUP //////////////////////////////////////
 	
 	float cursor_size = 15;
 	float object_size = 60;
 	float table_size = 760;
 	float scale_factor = 1;
 	int stopLimit = 100; // zeit bis der track anhlt
 	String[] fileNames;
 	public String myPrinterText;
 	float colorVal;
 	int leftBorder = 72; // linker Rand
 	int shiftX = 250; // verschiebung auf der x achse zur ellipse
 	int black = color(0, 0, 0); 
 	int pink = color(226, 0, 122);
	
 	///////////////////////////////////////////////////////////////////
 
 	// let's set a filter (which returns true if file's extension is .mp3)
 	java.io.FilenameFilter txtFilter = new java.io.FilenameFilter() {
 		public boolean accept(File dir, String name) {
 			return name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".aif");
 		}
 	};
 	
 	
 	public void setup() {
 		size(1280, 800, OPENGL);
 		smooth();
 		hint(ENABLE_OPENGL_4X_SMOOTH);
 	    hint(DISABLE_OPENGL_2X_SMOOTH);
 		noStroke();
 		fill(255);
 		loop();
 		frameRate(30);
 		font = loadFont("font.vlw"); 
 		// hintergrundbild laden
 		b = loadImage("gui.png");
 		cursor = new PImage[7];
 		for(int i = 0; i < 2; i++ ){
 			  cursor[i] = loadImage("cursor" + i + ".png");
 	  }
 		
 		scale_factor = height / table_size;
 		tuioClient = new TuioProcessing(this);
 		//println("the sketch path is "+sketchPath);		
		//fileNames = listFileNames(sketchPath, txtFilter);
 		
 		// mp3 order auswhlen & mp3s laden
 		String folderPath = selectFolder();
 		fileNames = listFileNames(folderPath, txtFilter);
 
 		//println(fileNames);
 		println("Loaded " +fileNames.length +" tracks in total");
 		myPrinter("Loaded " +fileNames.length +" tracks in total");
 		
 
 		// container erstellen
 		trackList = new ArrayList();
 		for (int i = 0; i < fileNames.length; i++) {
			trackList.add(new trackContainer(this, m, folderPath+fileNames[i]));
 		}
 		
 		playList = new ArrayList();
 
 	}
 
 	public void draw() {
 		imageMode(CORNER);
 		image(b, 0, 0);
 		fill(0);
 		myPrinterUpdate(); // schreibt playlist und status
 		
 		textAlign(CENTER);
 		textFont(font);
 
 		Vector tuioObjectList = tuioClient.getTuioObjects();
 		for (int i = 0; i < tuioObjectList.size(); i++) {
 
 			TuioObject tobj = (TuioObject) tuioObjectList.elementAt(i);
 
 			if (tobj.getSymbolID() < fileNames.length) {
 				trackContainer myTrack = (trackContainer) trackList.get(tobj.getSymbolID());
 				pushMatrix();
 				translate(tobj.getScreenX(width)+shiftX, tobj.getScreenY(height));
 				myTrack.drawCursor();
 				popMatrix();
 			}
 		}
 
 		for (int i = 0; i < fileNames.length; i++) {
 			trackContainer myTrack = (trackContainer) trackList.get(i);
 			myTrack.fader(); // player laden
 		}
 	}
 
 	public void addTuioObject(TuioObject tobj) {
 		/*
 		 * println("add object " + tobj.getSymbolID() + " (" +
 		 * tobj.getSessionID() + ") " + tobj.getX() + " " + tobj.getY() + " " +
 		 * tobj.getAngle());
 		 */
 		println("add object " + tobj.getSymbolID());
 		if (tobj.getSymbolID() < fileNames.length) {
 
 			trackContainer myTrack = (trackContainer) trackList.get(tobj
 					.getSymbolID());
 			myTrack.load(); // player laden
 		} else {
 			println("Object " + tobj.getSymbolID()
 					+ " is not assigned to a song");
 			myPrinter("Object " + tobj.getSymbolID()
 					+ " is not assigned to a song");
 
 		}
 	}
 
 	public void removeTuioObject(TuioObject tobj) {
 		println("remove object " + tobj.getSymbolID());
 		if (tobj.getSymbolID() < fileNames.length) {
 			trackContainer myTrack = (trackContainer) trackList.get(tobj
 					.getSymbolID());
 			myTrack.pause(); // player laden
 		}
 	}
 
 	public void updateTuioObject(TuioObject tobj) {
 		if (tobj.getSymbolID() < fileNames.length) {
 			trackContainer myTrack = (trackContainer) trackList.get(tobj
 					.getSymbolID());
 			myTrack.update(dist(tobj.getScreenX(width),
 					tobj.getScreenY(height), width / 2, height / 2));
 		}
 	}
 
 	public void addTuioCursor(TuioCursor tcur) {
 	}
 
 	public void updateTuioCursor(TuioCursor tcur) {
 	}
 
 	public void removeTuioCursor(TuioCursor tcur) {
 	}
 
 	public void refresh(TuioTime bundleTime) {
 		redraw();
 	}
 
 	public void stop() {
 		m.stop();
 		super.stop();
 	}
 	
 	public void myPrinter(String t) {
 		myPrinterText = t;
 	}
 	
 	public void myPrinterUpdate() {
 		textAlign(LEFT);
 		textFont(font);
 		text(myPrinterText, leftBorder, 675);
 		
 		for(int i = playList.size()-1; i >= 0; i--) {
 		
 		fill(0,0,0,255-(playList.size()-i)*20);
 		String t = (String) playList.get(i); 
 		text(t, leftBorder, 245+(playList.size()-i)*18);
 		}
 	}
 
 	String[] listFileNames(String dir, java.io.FilenameFilter extension) {
 
 		File file = new File(dir);
 		if (file.isDirectory()) {
 			String names[] = file.list(extension);
 			return names;
 		} else {
 			return null;
 		}
 	}
 }

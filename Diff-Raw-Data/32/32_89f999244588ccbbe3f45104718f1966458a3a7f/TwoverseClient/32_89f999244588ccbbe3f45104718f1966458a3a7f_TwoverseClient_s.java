 package twoverse;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PImage;
 import tuio.TuioClient;
 import tuio.TuioCursor;
 import tuio.TuioObject;
 import twoverse.gui.Button;
 import twoverse.gui.ImageButton;
 import twoverse.gui.RectButton;
 import twoverse.object.Galaxy;
 import twoverse.util.GalaxyShape;
 import twoverse.util.PhysicsVector3d;
 import twoverse.util.Point;
 
 public class TwoverseClient extends PApplet {
 	private final static boolean DEBUG = true;
	private final static boolean USE_TUIO = false;
 	private final int GUI_SIDE_MIN_X = screen.width - 100;
 	private final int GUI_TOP_MIN_Y = screen.height - 100;
 	/** TUIO & Control Members **/
 	private TuioClient mTuioClient;
 
 	/** GUI Members **/
 	private PFont mButtonFont;
 	private ArrayList<Button> mMainMenu;
 
 	private enum Mode {
 		NONE, CREATE, MOVE, ROTATE, ZOOM, EVOLVE, LEARN
 	}
 
 	private Mode mCurrentMode;
 
 	/** Object & Server Members **/
 	private ObjectManagerClient mObjectManager;
 	private RequestHandlerClient mRequestHandler;
 
 	/** Other **/
 	int cont = 99; // TODO consider making local
 	int obj = 99; // TODO consider making local
 	int inv = 99; // TODO consider making local
 	int mBackgroundColor = color(0); // TODO consider making local
 
 	public void setup() {
 
 		frameRate(30);
 		size(800, 600);
 		mCurrentMode = Mode.NONE;
 
 		mObjectManager = new ObjectManagerClient();
 		mRequestHandler = new RequestHandlerClient(mObjectManager);
 
 		initializeButtons();
 
 		// Run TUIO Client
 		mTuioClient = new TuioClient(this);
 	}
 
 	private void initializeButtons() {
 		// TODO pull all of these settings out to conf file
 		// make the names meaningful
 		mMainMenu = new ArrayList<Button>();
 		mButtonFont = loadFont("twoverse/data/NimbusSanL-BoldCond-48.vlw");
 
 		int baseColor = color(102);
 
 		// Ojbect button positions
 		int dx = 60;
 		int x, x0 = 100, y0 = 40, r0 = 30;
 
 		// Action Button positions
 		int rectx0 = 540;
 		int recty0 = 60;
 		int drect = 30;
 		int dy = drect + 10;
 
 		// Slider positions
 		int slidex0 = 30, slidey0 = 90;
 		int slidex1 = 50, slidey1 = 350;
 
 		// Investigate button positions
 		int invxsize = 40, invysize = 25, invx0 = 120, invy0 = 350, dinvx = invxsize + 10;
 
 		// Object create Buttons
 
 		int buttoncolor = color(100);
 		int highlight = color(100);
 
 		Button button = new RectButton(this, new Point(rectx0, recty0),
 				buttoncolor, highlight, "Create", drect, drect);
 		button.addChild(new ImageButton(this, new Point(x0, y0), buttoncolor,
 				highlight, "Satellite",
 				loadImage("twoverse/images/sputnik.jpg"), r0));
 		button.addChild(new ImageButton(this, new Point(x0 + dx, y0),
 				buttoncolor, highlight, "Planet",
 				loadImage("twoverse/images/12382-Planet_Ven.jpg"), r0));
 		button.addChild(new ImageButton(this, new Point(x0 + 2 * dx, y0),
 				buttoncolor, highlight, "Star",
 				loadImage("twoverse/images/sun.gif"), r0));
 		button.addChild(new ImageButton(this, new Point(x0 + 3 * dx, y0),
 				buttoncolor, highlight, "Galaxy",
 				loadImage("twoverse/images/hst_galaxy.JPG"), r0));
 		button.addChild(new ImageButton(this, new Point(x0 + 4 * dx, y0),
 				buttoncolor, highlight, "Pulsar",
 				loadImage("twoverse/images/230240main_Pulsar1_sm.jpg"), r0));
 		mMainMenu.add(button);
 
 		mMainMenu.add(new RectButton(this, new Point(rectx0, recty0 + dy),
 				buttoncolor, highlight, "Move", drect, drect));
 
 		button = new RectButton(this, new Point(rectx0, recty0 + dy * 3),
 				buttoncolor, highlight, "Evolve", drect, drect);
 		button.addChild(new RectButton(this, new Point(slidex1, slidey1),
 				buttoncolor, highlight, "evolve", 150, 15));
 		mMainMenu.add(button);
 
 		button = new RectButton(this, new Point(rectx0, recty0 + dy * 5),
 				buttoncolor, highlight, "Learn", drect, drect);
 		button.addChild(new RectButton(this, new Point(invx0, invy0),
 				buttoncolor, highlight, "Read", invxsize, invysize));
 		button.addChild(new RectButton(this, new Point(invx0 + dinvx, invy0),
 				buttoncolor, highlight, "Hear", invxsize, invysize));
 		button.addChild(new RectButton(this,
 				new Point(invx0 + 2 * dinvx, invy0), buttoncolor, highlight,
 				"watch", invxsize, invysize));
 		mMainMenu.add(button);
 
 	}
 
 	public void draw() {
 		background(mBackgroundColor);
 	updateButtons();
 		updateUniverse();
 
 		if (DEBUG && USE_TUIO) {
 			// Draw each cursor to the screen for debugging
 			TuioCursor[] tuioCursorList = mTuioClient.getTuioCursors();
 			for (int i = 0; i < tuioCursorList.length; i++) {
 				rect(tuioCursorList[i].getScreenX(width), tuioCursorList[i]
 						.getScreenY(height), 10, 10);
 			}
 		}
 	}
 
 	void updateUniverse() {
 		ArrayList<Galaxy> galaxies = mObjectManager.getGalaxies();
 		for (int i = 0; i < galaxies.size(); i++) {
 			rect((int) (galaxies.get(i).getPosition().getX()), (int) (galaxies
 					.get(i).getPosition().getY()), 10, 10);
 		}
 	}
 
 	void updateButtons() {
 		if (USE_TUIO) {
 			TuioCursor[] tuioCursorList = mTuioClient.getTuioCursors();
 			for (int i = 0; i < tuioCursorList.length; i++) {
 				for (int j = 0; j < mMainMenu.size(); j++) {
 					mMainMenu.get(j).update(
 							new Point(tuioCursorList[i].getScreenX(width),
 									tuioCursorList[i].getScreenY(height)));
 				}
 			}
 		} else {
 			for (int i = 0; i < mMainMenu.size(); i++) {
 				mMainMenu.get(i).update(new Point(mouseX, mouseY));
 			}
 		}
 	}
 
 	/**
 	 * TUIO Callbacks: Unfortunately, these must be implemented in the PApplet
 	 * class itself, so we can't pull it out to a MultitouchHandler class
 	 */
 
 	//TODO remove this...
 	private static int galaxy_counter = 0;
	void addTuioObject(TuioObject tobj) {
		// println("add object "+tobj.getFiducialID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle());
		if (tobj.getScreenX(screen.width) < GUI_SIDE_MIN_X
				&& tobj.getScreenY(screen.height) < GUI_TOP_MIN_Y) {
 			mObjectManager.add(new Galaxy(galaxy_counter++, -1, "theBody", null, null, -1,
					new Point(tobj.getScreenX(screen.width), tobj.getScreenY(screen.width), 0), new PhysicsVector3d(1, 2, 3, 4),
 					new PhysicsVector3d(5, 6, 7, 8), new GalaxyShape(1, "test",
 							"test"), 1000.5, 2000.20));
 		}
 	}
 	
 	public void mousePressed() {
 		if (mouseX < GUI_SIDE_MIN_X
 				&& mouseY < GUI_TOP_MIN_Y) {
 			mObjectManager.add(new Galaxy(galaxy_counter++, -1, "theBody", null, null, -1,
 					new Point(mouseX, mouseY, 0), new PhysicsVector3d(1, 2, 3, 4),
 					new PhysicsVector3d(5, 6, 7, 8), new GalaxyShape(1, "test",
 							"test"), 1000.5, 2000.20));
 		}
 	}
 
	void removeTuioObject(TuioObject tobj) {
		// println("remove object "+tobj.getFiducialID()+" ("+tobj.getSessionID()+")");
	}

	void updateTuioObject(TuioObject tobj) {
		// println("update object "+tobj.getFiducialID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle()
		// +" "+tobj.getMotionSpeed()+" "+tobj.getRotationSpeed()+" "+tobj.getMotionAccel()+" "+tobj.getRotationAccel());
	}

 	public static void main(String args[]) {
 		PApplet.main(new String[] { "--present", "twoverse.TwoverseClient" });
 	}
 }

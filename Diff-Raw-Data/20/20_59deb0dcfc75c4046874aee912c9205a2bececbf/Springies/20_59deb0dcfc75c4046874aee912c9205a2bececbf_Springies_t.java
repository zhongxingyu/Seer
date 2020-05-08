 package springies;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import jboxGlue.Mass;
 import jboxGlue.WorldManager;
 import jgame.platform.JGEngine;
 import springies.Controls;
 
 @SuppressWarnings("serial")
 public class Springies extends JGEngine {
 
 	// Master Array of massLists
 	ArrayList<HashMap<String, Mass>> massMaps = new ArrayList<HashMap<String, Mass>>();
 
 	int initialArea = 10; // Initial wall margin
 	int toggleGravity = 0; // Gravity on/off (TODO: Set this in BoardSetup?)
 
 	BoardSetup boardSet = new BoardSetup(this);
 	EnvironmentForces envForce = new EnvironmentForces(this);
 	Controls controller = new Controls(this, boardSet);
 
 	public Springies() {
 		int height = 600;
 		double aspect = 16.0 / 9.0;
 		initEngine((int) (height * aspect), height);
 	}
 
 	@Override
 	public void initCanvas() {
 		setCanvasSettings(1, // width of the canvas in tiles
 				1, // height of the canvas in tiles
 				displayWidth(), // width of one tile
 				displayHeight(), // height of one tile
 				null,// foreground colour -> use default colour white
 				null,// background colour -> use default colour black
 				null // standard font -> use default font
 		);
 	}
 
 	@Override
 	public void initGame() {
 		setFrameRate(60, 2);
 		WorldManager.initWorld(this);
 
 		boardSet.setWalls(initialArea);
 		boardSet.makeAssembly();
 		boardSet.fileIn();
 	}
 
 	@Override
 	public void doFrame() {
 		controller.checkUserInput();
 		envForce.calculateGravitationalForce(toggleGravity);
 		// update game objects
 		WorldManager.getWorld().step(1f, 1);
 		moveObjects();
	// 	Uncomment this out if you want wall sticking issues again...
	//	checkCollision(2, 1);
 	}
 
 	@Override
 	public void paintFrame() {
 	}
 
 	public ArrayList<HashMap<String, Mass>> getMassMaps() {
 		return massMaps;
 	}
 
 	public void addMassMap(HashMap<String, Mass> map) {
 		massMaps.add(map);
 	}
 
 	public void clearMassMaps() {
 		massMaps.clear();
 	}
 }

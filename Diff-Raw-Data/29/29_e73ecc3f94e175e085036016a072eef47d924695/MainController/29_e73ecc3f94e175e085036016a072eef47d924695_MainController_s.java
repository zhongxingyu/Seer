 package fr.odai.zerozeroduck.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.model.Patate;
 import fr.odai.zerozeroduck.model.World;
 
 public class MainController {
 
 	enum Keys {
 		PATATE, KILLALL
 	}
 
 	private World world;
 	private Array<Patate> patates;
 
 	static Map<Keys, Boolean> keys = new HashMap<MainController.Keys, Boolean>();
 	static {
 		keys.put(Keys.PATATE, false);
 		keys.put(Keys.KILLALL, false);
 	};
 
 	public MainController(World world) {
 		this.world = world;
 		this.patates = world.getPatates();
 	}
 
 	// ** Key presses and touches **************** //
 
 	public void patatePressed() {
 		keys.put(Keys.PATATE, true);
 	}
 
 	public void killallPressed() {
 		keys.put(Keys.KILLALL, true);
 	}
 
 	public void patateReleased() {
 		keys.put(Keys.PATATE, false);
 	}
 
 	public void killallReleased() {
 		keys.put(Keys.KILLALL, false);
 	}
 
 	/** The main update method **/
 	public void update(float delta) {
 		processInput();
 		
 		for(Patate patate : this.patates) {
			patate.walkForward(delta);
 		}
 		
 		world.update(delta);
 	}
 
 	private void processInput() {
 		if (keys.get(Keys.PATATE)) {
 			
 			Patate patate = new Patate(new Vector2(1,1));
 			patates.add(patate);
 			
 			// Disable until new keystroke
 			keys.put(Keys.PATATE, false);
 		}
 	}
 }

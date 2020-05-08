 package fr.odai.zerozeroduck.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.model.Patate;
 import fr.odai.zerozeroduck.model.Trap;
 import fr.odai.zerozeroduck.model.World;
 
 public class MainController {
 
 	public enum Keys {
		PATATE, KILLALL, TRAP_S, TRAP_F, TRAP_H, TRAP_K, UNDEFINED
 	}
 
 	private World world;
 	private Array<Patate> patates;
 	private Array<Trap> traps;
 
 	static Map<Keys, Boolean> keys = new HashMap<MainController.Keys, Boolean>();
 	static {
 		keys.put(Keys.PATATE, false);
 		keys.put(Keys.KILLALL, false);
		keys.put(Keys.TRAP_S, false);
		keys.put(Keys.TRAP_F, false);
		keys.put(Keys.TRAP_H, false);
		keys.put(Keys.TRAP_K, false);
		keys.put(Keys.UNDEFINED, false);
 	};
 
 	public MainController(World world) {
 		this.world = world;
 		this.patates = world.getPatates();
 		this.traps = world.getTraps();
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
 			patate.setState(Patate.State.WALKING);
 			patate.update(delta);
 		}
 		
 		world.update(delta);
 	}
 
 	private void processInput() {
 		for(Trap trap: this.world.getTraps()) {
 			if(keys.get(trap.getAssociatedKey())) {
 				trap.activate();
 			}
 		}
 		
 		if (keys.get(Keys.PATATE)) {
 			Patate patate = new Patate(new Vector2(1,1));
 			patates.add(patate);
 			
 			// Disable until new keystroke
 			keys.put(Keys.PATATE, false);
 		}
 		
 		if (keys.get(Keys.KILLALL)) {
 			patates.clear();
 		}
 	}
 }

 package fr.odai.zerozeroduck.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.GameScreen;
 import fr.odai.zerozeroduck.model.Carrot;
 import fr.odai.zerozeroduck.model.Duck;
 import fr.odai.zerozeroduck.model.Patate;
 import fr.odai.zerozeroduck.model.Trap;
 import fr.odai.zerozeroduck.model.Unit;
 import fr.odai.zerozeroduck.model.World;
 import fr.odai.zerozeroduck.sound.FouleSound;
 
 public class MainController {
 
 	public enum Keys {
 		PATATE, KILLALL, TRAP_S, TRAP_F, TRAP_H, TRAP_K, UNDEFINED
 	}
 
 	private GameScreen screen;
 	private World world;
 	private Array<Unit> units;
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
 
 	public MainController(World world, GameScreen screen) {
 		this.world = world;
 		this.screen = screen;
 		this.units = world.getUnits();
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
 	
 	public void trapPressed(Keys key) {
 		keys.put(key, true);
 	}
 	
 	public void trapReleased(Keys key) {
 		keys.put(key, false);
 	}
 
 	/** The main update method **/
 	public void update(float delta) {
 		processInput();
 		
 		if(world.getTotalPool()==0 && world.getUnits().size==0){
			//screen.gameWin();
 		}
 		
 		if(world.getDuck().getState()==Duck.State.DEAD){
			//screen.gameOver();
 		}
 		
 		for(Unit unit : this.units) {
 			unit.update(delta);
 		}
 		
 		for(Trap trap : this.traps) {
 			trap.update(delta);
 		}
 		
 		world.getDuck().update(delta);
 		
 		world.update(delta);
 	}
 
 	private void processInput() {
 		for(Trap trap: this.world.getTraps()) {
 			if(keys.get(trap.getAssociatedKey())) {
 				trap.activate();
 			}
 		}
 		
 		if (keys.get(Keys.PATATE)) {
 			Carrot carrot = new Carrot(new Vector2(0.5f,1), this.world, this.world.getAtlas());
 			units.add(carrot);
 			
 			// Disable until new keystroke
 			keys.put(Keys.PATATE, false);
 		}
 		
 		if (keys.get(Keys.KILLALL)) {
 			units.clear();
 		}
 	}
 }

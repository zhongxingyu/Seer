 package ddmp.projecttetra;
 
 import java.util.ArrayList;
 
 import org.andengine.engine.handler.IUpdateHandler;
 
 import com.badlogic.gdx.math.Vector2;
 
 import ddmp.projecttetra.entity.Planet;
 
 /**
  * Holds a reference to all planets currently spawned in game.
  */
 public class PlanetManager implements IUpdateHandler {
 	
 	private static final int MAX_SPAWNED_PLANETS = 20;
 	
 	private ArrayList<Planet> planets;
 	
 	public PlanetManager() {
 		planets = new ArrayList<Planet>();
 	}
 	
 	public void addPlanet(Planet planet) {
 		if(planets.size() >= MAX_SPAWNED_PLANETS) {
 			return;
 		}
 		
 		planets.add(planet);
 	}
 	
 	public boolean canSpawn() {
 		return planets.size() < MAX_SPAWNED_PLANETS;
 	}
 	
 	
 	
 	@Override
 	public void onUpdate(float pSecondsElapsed) {
 		int size = planets.size();
 		for(int i = size - 1; i >= 0; i--) {
 			if(planets.get(i).isDestroyed()) {
 				planets.remove(planets.get(i));
 			}
 		}
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean isGravitated(Vector2 point) {
 		for(Planet planet : planets) {
			if (planet.isGravitating(point)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }

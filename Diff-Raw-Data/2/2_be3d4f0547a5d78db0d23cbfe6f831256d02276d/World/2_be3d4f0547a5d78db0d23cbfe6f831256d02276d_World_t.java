 package fr.odai.zerozeroduck.model;
 
 import com.badlogic.gdx.Files;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 
 import fr.odai.zerozeroduck.controller.MainController;
 
 
 public class World {
 	/** Bump per minutes */
 	public static final int BPM = 85;
 		
 	/** Traps **/
 	Array<Trap> traps = new Array<Trap>();
 	/** Startpoints for the units **/
 	Array<Vector2> startpoints = new Array<Vector2>();
 	/** Units in this world **/
 	Array<Patate> patates = new Array<Patate>();
 	
 	private Array<Float> floor_pos = new Array<Float>();
 
 	/** Our player controlled hero **/
 	Duck duck;
 	
 	int score = 0;
 
 	// Getters -----------
 	public Duck getDuck() {
 		return duck;
 	}
 	public Array<Trap> getTraps() {
 		return traps;
 	}
 	public Array<Patate> getPatates() {
 		return patates;
 	}
 	public int getScore() {
 		return score;
 	}
 	// --------------------
 
 	public Array<Float> getFloorPos() {
 		return floor_pos;
 	}
 	public void setFloorPos(Array<Float> floor_pos) {
 		this.floor_pos = floor_pos;
 	}
 	public World() {
 		createDemoWorld();
 	}
 
 	private void createDemoWorld() {
 		duck = new Duck(new Vector2(9, 1), this);
 
 		Trap trap = new Trap(0.5f,new Vector2(2,1), 10);
 		trap.setAssociatedKey(MainController.Keys.TRAP_S);
 		traps.add(trap);
 		
 		Pixmap floor_pixmap = new Pixmap(Gdx.files.internal("images/Stage0-floor.png"));
 		int width = floor_pixmap.getWidth();
 		setFloorPos(new Array<Float>(width));
 		for(int i = 0; i < width; i++) {
 			int j = 0;
 			Color color = new Color();
 			for(; j < floor_pixmap.getHeight(); j++) {
 				Color.rgba8888ToColor(color, floor_pixmap.getPixel(i, j));
 				if(color.r > 0.8f && color.g > 0.8f && color.b > 0.8f) {
 					break;
 				}
 			}
 			getFloorPos().add(7.f - j / (float) floor_pixmap.getHeight() * 7.f);
 		}
 	}
 	
 	public float getFloorHeight(float x) {
 		int index = Math.round(x / 10 * getFloorPos().size);
		if(index >= getFloorPos().size)
 			return getFloorPos().get(getFloorPos().size - 1);
 		else if(index < 0)
 			return getFloorPos().get(0);
 		return getFloorPos().get(index);
 	}
 	
 	public void update(float delta) {
 		// Auto-deleting when getting out of screen
 		for(Patate patate: patates) {
 			if(patate.position.x > 10 || patate.position.x < 0) {
 				patates.removeValue(patate, true);
 			}
 		}
 	}
 }

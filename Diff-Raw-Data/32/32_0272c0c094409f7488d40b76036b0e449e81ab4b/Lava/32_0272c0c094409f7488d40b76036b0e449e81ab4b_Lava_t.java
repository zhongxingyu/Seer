 package org.group20.sunstruck.world.map.segments;
 
 import java.util.HashMap;
 import org.group20.sunstruck.Game;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Lava extends Theme {
 
	private TextureRegion[] textures = { 
			Game.textureAtlas.findRegion("lava1"),
 			Game.textureAtlas.findRegion("lava2") };
 	private HashMap<MapTypes, TextureRegion> transitions = new HashMap<MapTypes, TextureRegion>();
 
 	public Lava() {
 		setType(MapTypes.LAVA);
 		setTextures(textures);
 		transitions.put(MapTypes.ROCK,
 				Game.textureAtlas.findRegion("lavaToRock"));
 		setTransitions(transitions);
 	}
 }

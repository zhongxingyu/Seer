 package game.things;
 
 import java.util.Collections;
 import java.util.List;
 
 import util.Area;
 import util.Direction;
 import util.Position;
 import game.*;
 
 public class Wall extends AbstractGameThing {
 	private final String renderer;
 
 	public Wall(GameWorld world, String name){
 		super(world);
 		renderer = name;
 		update();
 	}
 	
 	public Wall(GameWorld world){
 		this(world, "wallcross");
 	}
 
 	public String renderer(){
 		return renderer;
 	}

	public String name(){
		return "Wall";
	}
 }

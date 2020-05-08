 package com.soc.game.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.artemis.systems.VoidEntitySystem;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.soc.game.components.Player;
 import com.soc.game.components.Position;
 import com.soc.utils.Globals;
 
 public class CameraSystem extends VoidEntitySystem{
 	@Mapper ComponentMapper<Position> pm;
 		
 	public CameraSystem(OrthographicCamera camera) {
 		super();
 		this.camera = camera;
 	}
 
 	private OrthographicCamera camera;
 
 	@Override
 	protected void processSystem() {
 		camera.position.set(Globals.playerPosition.x, Globals.playerPosition.y, 0);
		camera.update();
 	}
 
 }

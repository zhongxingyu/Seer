 package com.gemserk.games.taken;
 
 import com.artemis.Entity;
 import com.artemis.EntityProcessingSystem;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.gemserk.commons.artemis.components.Spatial;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.artemis.components.SpriteComponent;
 import com.gemserk.commons.artemis.systems.ActivableSystem;
 import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
 import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
 import com.gemserk.games.taken.components.CameraFollowComponent;
 
 public class CameraFollowSystem extends EntityProcessingSystem implements ActivableSystem {
 
 	private final ActivableSystem activableSystem = new ActivableSystemImpl();
 
 	public CameraFollowSystem() {
 		super(CameraFollowComponent.class);
 	}
 
 	@Override
 	public void toggle() {
 		activableSystem.toggle();
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return activableSystem.isEnabled();
 	}
 
 	@Override
 	protected void process(Entity e) {

 		CameraFollowComponent cameraFollowComponent = e.getComponent(CameraFollowComponent.class);
 		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
 		SpriteComponent spriteComponent = e.getComponent(SpriteComponent.class);
 
 		Camera camera = cameraFollowComponent.getCamera();
 		Spatial spatial = spatialComponent.getSpatial();
 
 		Sprite sprite = spriteComponent.getSprite();
		float width = SpriteUtils.getOriginalHeight(sprite);
 		
 		camera.setPosition(spatial.getX(), spatial.getY());
 		camera.setZoom(width / spatial.getWidth());

 	}
 
 }

 package com.punchline.javalib.entities.systems.render;
 
 import java.util.Comparator;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.render.Parallax;
 import com.punchline.javalib.entities.components.render.Renderable;
 import com.punchline.javalib.entities.systems.ComponentSystem;
 import com.punchline.javalib.utils.Convert;
 
 /**
  * System for rendering every {@link Entity} that has a {@link Renderable} component.
  * @author Natman64
  *
  */
 public final class RenderSystem extends ComponentSystem {
 	
 	/**
 	 * Comparator implementation used for properly layering Renderables.
 	 * @author Natman64
 	 *
 	 */
 	private class RenderableComparator implements Comparator<Entity> {
 
 		@Override
 		public int compare(Entity o1, Entity o2) {
 			Renderable r1 = o1.getComponent(Renderable.class);
 			Renderable r2 = o2.getComponent(Renderable.class);
 			
 			return r1.getLayer() - r2.getLayer();
 		}
 		
 	}
 	
 	private Camera camera;
 	private SpriteBatch spriteBatch;
 	private RenderableComparator comparator = new RenderableComparator();
 	
 	//region Initialization/Disposal
 	
 	/**
 	 * Constructs a RenderSystem.
 	 * @param camera The camera for rendering.
 	 */
 	@SuppressWarnings("unchecked")
 	public RenderSystem(Camera camera) {
 		super(Renderable.class);
 		
 		this.camera = camera;
 		
 		spriteBatch = new SpriteBatch();
 		spriteBatch.enableBlending();
 	}
 
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 	}
 	
 	//endregion
 	
 	//region Processing
 	
 	@Override
 	public void processEntities() {
 		
 		if (processingListChanged) entities.sort(comparator);
 		
 		camera.update();
 		spriteBatch.setProjectionMatrix(camera.combined);
 		
 		spriteBatch.begin();
 		
 		super.processEntities();
 		
 		spriteBatch.end();
 		
 	}
 
 	@Override
 	protected void process(Entity e) {
 		
 		Renderable r = (Renderable)e.getComponent(Renderable.class);
 		
 		if (e.hasComponent(Transform.class)) { 
 			Transform t = (Transform)e.getComponent(Transform.class);
 			
 			Vector2 pos = Convert.metersToPixels(t.getPosition().cpy());
 			float angle = t.getRotation();
 			
			//Handle position setting for parallax scrolling.
 			if(e.hasComponent(Parallax.class)){
 				Parallax p = e.getComponent(Parallax.class);
 				// v = (v - c.p) * modulus_velocity
 				r.setPosition((new Vector2(p.getCameraPosition())).scl((1 - p.getDepthRatio())).add(pos.cpy()));
 			}
 			else
 				r.setPosition(pos);
 			
 			r.setRotation((float)Math.toDegrees(angle));
 		}		
 		r.draw(spriteBatch, deltaSeconds());
 		
 	}	
 	
 	//endregion
 	
 }

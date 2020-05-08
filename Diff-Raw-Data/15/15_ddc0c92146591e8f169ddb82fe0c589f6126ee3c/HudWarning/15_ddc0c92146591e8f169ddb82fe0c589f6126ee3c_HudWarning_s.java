 package com.punchline.NinjaSpacePirate.gameplay.entities.components.render;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.components.ComponentManager;
 import com.punchline.javalib.entities.components.render.Sprite;
 import com.punchline.javalib.entities.processes.ExpirationProcess;
 import com.punchline.javalib.utils.Convert;
 
 /**
  * A warning displayed at the top of the screen to warn the player about a coming obstacle
  * @author Natman64
  * @created Oct 13, 2013
  */
 public class HudWarning extends Sprite {
 
 	/**
 	 * Possible types of HUD warning.
 	 * @author Natman64
 	 * @created Oct 13, 2013
 	 */
 	public enum WarningType {
 		GreenArrow,
 		WarningSign
 	}
 	
 	private static final float Y_POSITION = Convert.metersToPixels(5.5f);
 	
 	private static final float ON_TIME = 0.5f;
 	private static final float OFF_TIME = 0.2f;
 	private static final float LIFE_SPAN = 2 * ON_TIME + 1 * OFF_TIME;
 	
 	private EntityWorld world;
 	private float elapsedTime = 0f;
 	
 	/**
 	 * Creates a HudWarning
 	 * @param world
 	 * @param type {@link WarningType} of the warning.
 	 * @param lane The lane of the hallway, range [0, 6]
 	 */
 	public HudWarning(EntityWorld world, WarningType type, int lane) {
 		super(world.getSpriteSheet(), type.toString());
 		
 		this.world = world;
 		
		setPosition(Convert.metersToPixels(new Vector2(lane - 2, 0))); //y set on draw
 		setLayer(105); //in front of everything
 	}
 	
 	@Override
 	public void onAdd(ComponentManager container) {
 		world.getProcessManager().attach(new ExpirationProcess(LIFE_SPAN, (Entity) container));
 	}
 
 	@Override
 	public void draw(SpriteBatch spriteBatch, float deltaSeconds) {
 		elapsedTime += deltaSeconds;
 		
 		if (isVisible(elapsedTime)) {
 			Vector2 pos = getPosition();
 			setPosition(new Vector2(pos.x, world.getCamera().position.cpy().y + Y_POSITION));
 			
 			super.draw(spriteBatch, deltaSeconds);
 		}
 	}
 	
 	private boolean isVisible(float elapsedTime) {
 		return !(elapsedTime >= ON_TIME && elapsedTime <= ON_TIME + OFF_TIME);
 	}
 
 }

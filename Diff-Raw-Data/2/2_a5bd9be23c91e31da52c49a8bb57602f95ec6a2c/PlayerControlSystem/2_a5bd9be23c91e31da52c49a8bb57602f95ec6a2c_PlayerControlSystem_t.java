 package com.punchline.NinjaSpacePirate.gameplay.entities.systems;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.NinjaSpacePirate.gameplay.entities.components.render.NPCMultiRenderable;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Body;
 import com.punchline.javalib.entities.components.physical.Velocity;
 import com.punchline.javalib.entities.components.render.Renderable;
 import com.punchline.javalib.entities.systems.InputSystem;
 import com.punchline.javalib.utils.Convert;
 
 /**
  * The PlayerControlSystem.
  * @author Natman64
  *
  */
 public class PlayerControlSystem extends InputSystem {
 	
 	private static final float PLAYER_SPEED = Convert.pixelsToMeters(15);
 	
 	private Entity player;
 	
 	/**
 	 * Constructs the PlayerControlSystem.
 	 * @param input
 	 */
 	public PlayerControlSystem(InputMultiplexer input) {
 		super(input);
 	}
 
 	@Override
 	public boolean canProcess(Entity e) {
 		return e.getTag().equals("Player");
 	}
 
 	@Override
 	protected void process(Entity e) {
 		if (player == null) player = e;
 	}
 
 	@Override
 	public void processEntities() {
		player = null;
		
 		super.processEntities();
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (player == null) return false;
 		
 		NPCMultiRenderable mr = (NPCMultiRenderable) player.getComponent(Renderable.class);
 		
 		if (mr.isDead()) return false;
 		
 		Body b = player.getComponent(Body.class);
 		
 		Velocity v = player.getComponent(Velocity.class);
 		
 		if (keycode == Keys.LEFT) {
 			b.setRotation((float)Math.toRadians(180));
 			v.setLinearVelocity(new Vector2(-PLAYER_SPEED, 0));
 			return true;
 		} else if (keycode == Keys.DOWN) {
 			b.setRotation((float)Math.toRadians(270));
 			v.setLinearVelocity(new Vector2(0, -PLAYER_SPEED));
 			return true;
 		} else if (keycode == Keys.UP) {
 			b.setRotation((float)Math.toRadians(90));
 			v.setLinearVelocity(new Vector2(0, PLAYER_SPEED));
 			return true;
 		} else if (keycode == Keys.RIGHT) {
 			b.setRotation(0);
 			v.setLinearVelocity(new Vector2(PLAYER_SPEED, 0));
 			return true;
 		} else if (keycode == Keys.ESCAPE) {
 			mr.die();
 			v.setLinearVelocity(new Vector2());
 			return true;
 		} else if (keycode == Keys.SPACE) {			
 			v.setLinearVelocity(new Vector2());
 			return true;
 		}
 		
 		return false;
 	}
 	
 }

 package com.madcowd.buildnhide.entities.systems;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Body;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.render.AnimatedSprite;
 import com.punchline.javalib.entities.systems.InputSystem;
 
 public class PlayerControlSystem extends InputSystem {
 
 	private static final Vector2 PLAYER_VELOCITY = new Vector2(1, 0);
 
 	public PlayerControlSystem(InputMultiplexer input) {
 		super(input);
 	}
 
 	@Override
 	public boolean canProcess(Entity e) {
 		return e.getTag().equals("Player");
 	}
 
 	Vector2 velocity = new Vector2(0, 0);
 
 	Entity jayEntity;
 	Vector2 jayOffset = new Vector2(0, 0);
 	boolean isSmoking = false;
 	boolean jump = false;
 	Vector2 autoMovement = new Vector2(0, 0);
 
 	@Override
 	public void process(Entity e) {
 
 		// CAMERA
 
 		// MOVEMENT
 		Body b = e.getComponent(Body.class);
 
 		// BEGIN AUTOSCROLLING
 		if (autoMovement != PLAYER_VELOCITY
 				&& b.getPosition().x > world.getBounds().x + 20
 				&& b.getLinearVelocity().y == 0)
 			autoMovement = PLAYER_VELOCITY;
 
 		b.setPosition(b
 				.getPosition()
 				.cpy()
 				.add(velocity.cpy().add(autoMovement)
 						.scl(this.deltaSeconds() * 12)));
 
 		// Jumping
 		if (jump && b.getLinearVelocity().y == 0) {
 			b.getBody().applyLinearImpulse(0, 30, 0, 0, true);
 		}
 
 		// ANimatyion
 		AnimatedSprite as = e.getComponent(AnimatedSprite.class);
 		Vector2 linv = b.getLinearVelocity();
 
 		if (velocity.x + autoMovement.x > 0) {
 			as.setState("Right", true);
 			// Set jayOffset
 			jayOffset = new Vector2(2.5f, 1.125f);
 
 		} else if (velocity.x + autoMovement.x < 0) {
 			as.setState("Left", true);
 			// Set jayOffset
 			jayOffset = new Vector2(-4f, 1.4f);
 		}
 
 		if (linv.y == 0 && velocity.x + autoMovement.x != 0)
			as.resume();
 		else
 			as.pause();
 
 		// SMOKING CODE
 		if (isSmoking) {
 			if (jayEntity == null) {
 				jayEntity = world.createEntity("Jay", b.getPosition().cpy()
 						.add(jayOffset), as.getState());
 			}
 
 			// Update jay position
 			((Transform) jayEntity.getComponent(Transform.class)).setPosition(b
 					.getPosition().cpy().add(jayOffset));
 
 			// Update jay direction
 			AnimatedSprite jayAS = jayEntity.getComponent(AnimatedSprite.class);
 
 			if (!jayAS.getState().equals(as.getState())) // IF the states
 															// differ, switch
 															// without a
 															// continued time.
 				jayAS.setState(as.getState(), false);
 
 		} else if (jayEntity != null) {
 			jayEntity.delete();
 			jayEntity = null;
 		}
 
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.A) {
 			velocity.scl(0, 1).add(-0.5f, 0);
 
 			return true;
 		}
 
 		if (keycode == Keys.D) {
 			velocity.scl(0, 1).add(0.5f, 0);
 			return true;
 		}
 
 		if (keycode == Keys.SPACE) {
 			jump = true;
 			return true;
 		}
 
 		if (keycode == Keys.J) {
 
 			if (!isSmoking) {
 				world.setTimeCoefficient(0.5f);
 				isSmoking = true;
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Keys.A || keycode == Keys.D) {
 			velocity.scl(0, 1);
 			return true;
 		}
 		if (keycode == Keys.SPACE) {
 			jump = false;
 			return true;
 		}
 
 		if (keycode == Keys.J) {
 
 			if (isSmoking) {
 				world.setTimeCoefficient(1);
 				isSmoking = false;
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 }

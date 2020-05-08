 package se.chalmers.segway.game;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 
 import se.chalmers.segway.entities.Player;
 
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class PlayerContact implements ContactListener {
 
 	private Player player;
 	private Engine engine;
 
 	
 	/**
 	 * @return the player
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * @param player the player to set
 	 */
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	/**
 	 * @return the engine
 	 */
 	public Engine getEngine() {
 		return engine;
 	}
 
 	/**
 	 * @param engine the engine to set
 	 */
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		final Fixture x1 = contact.getFixtureA();
 		final Fixture x2 = contact.getFixtureB();
 
 		if (x1.getBody().getUserData() != null
 				&& x2.getBody().getUserData() != null) {
			if (x2.getBody().getUserData().equals("player")) {
 				player.increaseFootContacts();
 			}
 		}
 
		if (x1.getBody().getUserData().equals("platform3")
				&& x2.getBody().getUserData().equals("player")) {
			x1.getBody().setType(BodyType.DynamicBody);
 		}
 
		if (x1.getBody().getUserData().equals("platform2")
				&& x2.getBody().getUserData().equals("player")) {
 			engine.registerUpdateHandler(new TimerHandler(0.2f,
 					new ITimerCallback() {
 						public void onTimePassed(
 								final TimerHandler pTimerHandler) {
 							pTimerHandler.reset();
 							engine.unregisterUpdateHandler(pTimerHandler);
							x1.getBody().setType(BodyType.DynamicBody);
 						}
 					}));
 		}
 
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		final Fixture x1 = contact.getFixtureA();
 		final Fixture x2 = contact.getFixtureB();
 
 		if (x1.getBody().getUserData() != null
 				&& x2.getBody().getUserData() != null) {
			if (x2.getBody().getUserData().equals("player")) {
 				player.decreaseFootContacts();
 			}
 		}
 
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {
 
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {
 
 	}
 
 }

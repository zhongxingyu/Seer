 package com.secondhand.view.entities;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.powerup.PowerUp;
 
 public class PowerUpView implements IEntityView, PropertyChangeListener {
 
	private Engine engine;
	private GameWorld gameWorld;
 	
	public PowerUpView(Engine engine, GameWorld gameWorld) {
 		this.engine = engine;
 		this.gameWorld = gameWorld;
 		
 		//  you probably don't want to add the player again(was alreay done in PlayerView)
 		//gameWorld.getPlayer().addListener(this);
 	}
 	
 	public TimerHandler createTimer(final Player player, final PowerUp powerUp) {
 		return new TimerHandler(powerUp.getDuration(), new ITimerCallback() {
 			private Player user = player; 
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				if (user.getPowerUps().contains(powerUp))
 					user.removePowerUp(powerUp);
				if (!powerUp.hasAnother(player)) {
 					// TODO: Unattach the powerups texture from player (ex: shield makes the player glow)
				}
 			}
 		});
 	}
 
 	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String eventName = event.getPropertyName();
 		if (eventName.equals(Player.ADD_POWER_UP)) {
 
 			final Player player = gameWorld.getPlayer();
 			final PowerUp powerUp = ((PowerUp) event.getNewValue());
 			engine.registerUpdateHandler(createTimer(player, powerUp));

 			if (!powerUp.hasAnother(player)) {
 				// TODO: Attach the powerups texture to player (ex: shield makes the player glow)
			}
 			
 			// TODO: Implement floating text here
 //			if (powerUp.hasText()) {
 //				showFadingTextNotifier(powerUp.getText(),
 //						new Vector2(player.getX(), player.getY()));
 		}
 	}
 	
 }

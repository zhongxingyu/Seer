 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ultraextreme.model.GameModel;
 import ultraextreme.model.ModelInput;
 import ultraextreme.model.Player;
 import ultraextreme.model.enemy.IEnemy;
 import ultraextreme.model.entity.EnemyShip;
 import ultraextreme.model.entity.IEntity;
 import ultraextreme.model.entity.PlayerShip;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.util.Timer;
 import ultraextreme.view.GameObjectSprite;
 import ultraextreme.view.GameScene;
 import ultraextreme.view.SpriteFactory;
 import android.util.Log;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * @author Johan Gronvall
  * @author Viktor Anderling
  * 
  */
 public class GameLoop implements IUpdateHandler, PropertyChangeListener {
 
 	// TODO(natan) perhaps refactor this variable?
 	private static final float ON_HIT_BLINK_TIME = 0.1f;
 	private static final int PLAYER_INVINCIBILITY_BLINKS = 12; // Must be an
 																// even
 																// number!
 
 	private final GameScene gameScene;
 	private final GameModel gameModel;
 	private final List<GameObjectSprite> gameObjectSprites;
 	private final VertexBufferObjectManager vertexBufferObjectManager;
 	private Vector2d scalingQuotient;
 
 	private boolean firing;
 	private double moveX;
 	private double moveY;
 	private boolean specialAttack;
 
 	private List<Timer> timerList;
 
 	public GameLoop(final GameScene gameScene, final GameModel gameModel,
 			final List<GameObjectSprite> gameObjectSprites,
 			final VertexBufferObjectManager vertexBufferObjectManager,
 			double screenWidth, final double screenHeight) {
 
 		final Dimension screenDimension = new Dimension(screenWidth,
 				screenHeight);
 		this.scalingQuotient = screenDimension.getQuotient(Constants
 				.getLevelDimension());
 		this.gameScene = gameScene;
 		this.gameModel = gameModel;
 		this.gameObjectSprites = gameObjectSprites;
 		this.vertexBufferObjectManager = vertexBufferObjectManager;
 		this.timerList = new LinkedList<Timer>();
 	}
 
 	@Override
 	public void onUpdate(float time) {
 		gameModel.update(new ModelInput(moveX / scalingQuotient.x, moveY
 				/ scalingQuotient.y, firing, specialAttack), time);
 		moveX = 0;
 		moveY = 0;
 		specialAttack = false;
 		for (GameObjectSprite sprite : gameObjectSprites) {
 			sprite.update();
 		}
 		updateTimers(time);
 	}
 
 	@Override
 	public void reset() {
 		setFiring(false);
 		timerList.clear();
 		for (GameObjectSprite gOS : gameObjectSprites) {
 			gOS.reset();
 		}
 	}
 
 	/**
 	 * If a new sprite is created, adds it to the scene and to the list if a
 	 * sprite is removed, removes it from the scene and the list
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals(Constants.EVENT_ENEMY_DAMAGED)) {
 			EnemyShip ship = (EnemyShip) event.getNewValue();
 			Timer timer = new Timer(Constants.EVENT_ENEMY_DAMAGED,
 					ON_HIT_BLINK_TIME, ship);
 			timerList.add(timer);
 			getSprite(ship).onHitBlink();
 		} else if (event.getPropertyName().equals(
 				Constants.EVENT_ENTITY_INVINCIBLE)) {
 			Object o = event.getNewValue();
 			if (o instanceof Player) {
 				Player player = (Player) o;
 				PlayerShip ship = player.getShip();
 				Timer timer = new Timer(
 						Constants.EVENT_ENTITY_INVINCIBLE,
 						(float) (player.getInvincibilityTime() / PLAYER_INVINCIBILITY_BLINKS),
 						ship, PLAYER_INVINCIBILITY_BLINKS - 1);
 				timerList.add(timer);
 				getSprite(ship).invincibilityBlink();
 			}
 		} else if (event.getPropertyName().equals(Constants.EVENT_NEW_ENTITY)) {
 			IEntity entity;
 
 			if (event.getNewValue() instanceof IEnemy) {
 				entity = ((IEnemy) event.getNewValue()).getShip();
 			} else { // if item or bullet
 				entity = (IEntity) event.getNewValue();
 			}
 			final GameObjectSprite newSprite = SpriteFactory.getNewSprite(
 					entity, vertexBufferObjectManager);
 			gameScene.attachChild(newSprite);
 			gameObjectSprites.add(newSprite);
 
 		} else if (event.getPropertyName().equals(
 				Constants.EVENT_REMOVED_ENTITY)) {
 			IEntity entity;
 			if (event.getNewValue() instanceof IEnemy) {
 				entity = ((IEnemy) event.getNewValue()).getShip();
 			} else { // if item or bullet
 				entity = (IEntity) event.getNewValue();
 			}
 			// Find the GameObjectSprite that has a reference to this entity and
 			// remove
 			// it from the GameObjectSprite list and from the render scene.
			GameObjectSprite sprite = getSprite(entity);
			if(sprite != null) {
				gameScene.detachChild(sprite);
				gameObjectSprites.remove(sprite);
 			}
 		}
 	}
 
 	/**
 	 * Updates all the timers, performs actions and removes stopped timers.
 	 * 
 	 * @param timeElapsed
 	 */
 	private void updateTimers(float timeElapsed) {
 		Iterator<Timer> i = timerList.iterator();
 		while (i.hasNext()) {
 			Timer timer = i.next();
 			if (timer.update(timeElapsed)) {
 				Object o = timer.getObject();
 				String propertyName = timer.getPropertyName();
 
 				boolean timerDeprecated = false;
 				// The different actions that may be performed.
 				if (propertyName.equals(Constants.EVENT_ENEMY_DAMAGED)) {
 					GameObjectSprite sprite = getSprite((IEntity) o);
 					if (sprite == null) {
 						timerDeprecated = true;
 					} else {
 						sprite.onHitBlink();
 					}
 				}
 				if (propertyName.equals(Constants.EVENT_ENTITY_INVINCIBLE)) {
 					GameObjectSprite sprite = getSprite((IEntity) o);
 					if (sprite == null) {
 						timerDeprecated = true;
 					} else {
 						sprite.invincibilityBlink();
 					}
 				}
 				// add more actions here!
 				if (!timer.isRunning() || timerDeprecated) {
 					i.remove();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the sprite that corresponds to this entity.
 	 * 
 	 * @return The sprite if it exists, else null.
 	 */
 	private GameObjectSprite getSprite(IEntity entity) {
 		Iterator<GameObjectSprite> i = gameObjectSprites.iterator();
 		while (i.hasNext()) {
 			GameObjectSprite sprite = (i.next());
 			if (sprite.getEntity() == entity) {
 				return sprite;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets if the player is firing or not.
 	 * 
 	 * @param b
 	 *            true if the player is firing, otherwise false.
 	 */
 	public void setFiring(boolean b) {
 		this.firing = b;
 	}
 
 	/**
 	 * Adds the provided change to the movement.
 	 * 
 	 * @param dX
 	 *            The change in the x axis.
 	 * @param dY
 	 *            The change in the y axis.
 	 */
 	public void addToMovement(double dX, double dY) {
 		this.moveX += dX;
 		this.moveY += dY;
 	}
 
 	/**
 	 * Orders the player to fire a special attack.
 	 */
 	public void fireSpecialAttack() {
 		specialAttack = true;
 		Log.d("DEBUG", "specialAttack " + specialAttack);
 	}
 }

 package com.jjaz.aetherflames.physics;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
 import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
 import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
 import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
 import org.andengine.extension.multiplayer.protocol.util.MessagePool;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.util.debug.Debug;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.World;
 import com.jjaz.aetherflames.AetherFlamesConstants;
 import com.jjaz.aetherflames.ProjectileWeapon;
 import com.jjaz.aetherflames.Ship;
 import com.jjaz.aetherflames.messages.client.CollisionClientMessage;
 import com.jjaz.aetherflames.messages.client.GameStateClientMessage;
 import com.jjaz.aetherflames.messages.client.NewBulletClientMessage;
 import com.jjaz.aetherflames.messages.server.CollisionServerMessage;
 import com.jjaz.aetherflames.messages.server.GameStateServerMessage;
 import com.jjaz.aetherflames.messages.server.NewBulletServerMessage;
 
 public class DistributedFixedStepPhysicsWorld extends FixedStepPhysicsWorld implements AetherFlamesConstants {
 	
 	// Game state variables
 	private int mID;
 	private Map<Integer, Ship> mShips;
 	private Map<Integer, Body> mBullets;
 	private int mNextBulletID;
 	
 	// Frame rate variables
 	private int mMaximumStepsPerUpdate;
 	private final float mTimeStep;
 	private float mSecondsElapsedAccumulator;
 	private int mFrameNum;
 	
 	// Networking variables
 	private ServerConnector<SocketConnection> mServerConnector;
 	private MessagePool<IMessage> mMessagePool;
 	
 	public DistributedFixedStepPhysicsWorld(final int pStepsPerSecond, final Vector2 pGravity, final boolean pAllowSleep, final int pVelocityIterations, final int pPositionIterations) {
 		super(pStepsPerSecond, pGravity, pAllowSleep, pVelocityIterations, pPositionIterations);
 		this.mBullets = new HashMap<Integer, Body>();
 		this.mNextBulletID = this.mID * 100000;
 		this.mMaximumStepsPerUpdate = 10;
 		this.mTimeStep = 1.0f / pStepsPerSecond;
 		this.mSecondsElapsedAccumulator = 0;
 		this.mFrameNum = 0;
 		this.mMessagePool = new MessagePool<IMessage>();
 		
 		initMessagePool();
 	}
 	
 	/**
 	 * Initializes the message pool
 	 */
 	private void initMessagePool() {
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_GAME_STATE, GameStateClientMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_NEW_BULLET, NewBulletClientMessage.class);
 		this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_COLLISION, CollisionClientMessage.class);
 	}
 	
 
 	/**
 	 * Registers all gameplay message types to be handled by the designated methods in the ClientGameManager class
 	 */
 	private void registerMessageHandlers() {
 		try {
 			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_GAME_STATE, GameStateServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final GameStateServerMessage gameStateMessage = (GameStateServerMessage)pServerMessage;
 					DistributedFixedStepPhysicsWorld.this.handleGameStateMessage(gameStateMessage);
 				}
 			});
 			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_NEW_BULLET, NewBulletServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final NewBulletServerMessage newBulletMessage = (NewBulletServerMessage)pServerMessage;
 					DistributedFixedStepPhysicsWorld.this.handleNewBulletMessage(newBulletMessage);
 				}
 			});
 			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_COLLISION, CollisionServerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final CollisionServerMessage collisionMessage = (CollisionServerMessage)pServerMessage;
 					DistributedFixedStepPhysicsWorld.this.handleCollisionMessage(collisionMessage);
 				}
 			});
 
 		} catch (final Throwable t) {
 			Debug.e(t);
 		}
 	}
 	
 	/**
 	 * Reports the game state to the server
 	 */
 	private void reportState() {		
 		Ship ship = this.mShips.get(this.mID);
 		
 		// set the message fields and send the message if the ship is still alive
 		if (ship != null && this.mServerConnector != null) {
 			GameStateClientMessage message = (GameStateClientMessage)this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_GAME_STATE);
 			message.setFrameNumber(this.mFrameNum);
 			message.setShipState(this.mShips.get(this.mID));
 
 			if(this.mShips.get(this.mID).getHealth() < 1000)
 			{
 				int x = 10;
 				x = x;
 			}
 			
 			// send the message
 			try {
 				this.mServerConnector.sendClientMessage(message);
 			} catch (IOException e) {
 				Debug.e(e);
 			} finally {
 				this.mMessagePool.recycleMessage(message);
 			}
 		}
 	}
 	
 	/**
 	 * Update the ship and estimate the new position based on previous values
 	 * 
 	 * @param ship The ship to update
 	 * @param messageFrame The frame at which the message was sent
 	 * @param currentFrame The current frame on the local device
 	 * @param angle New angle
 	 * @param angularVelocity New angularVelocity
 	 * @param position New position
 	 * @param velocity New velocity
 	 */
 	private void updateShipBody(Ship ship, int messageFrame, int currentFrame, float angle, float angularVelocity, Vector2 position, Vector2 velocity) {
 		int frameDiff = currentFrame - messageFrame;
 		float framePeriod = 1.0f / FRAMES_PER_SECOND;
 		float dt = frameDiff * framePeriod;
 		
 		// estimate the current position and angle
 		float curAngle = angle + angularVelocity * dt;
 		Vector2 curPos = position.add(velocity.mul(dt));
 		
 		ship.setAngle(curAngle);
 		//ship.setAngularVelocity(angularVelocity);
 		ship.setPosition(curPos);
 		ship.setVelocity(velocity);
 		
 	}
 	
 	/**
 	 * Update the ship and estimate the new position based on previous values
 	 * 
 	 * @param messageFrame The frame at which the message was sent
 	 * @param currentFrame The current frame on the local device
 	 * @param weapons The weapons of the current ship
 	 * @param bulletID ID of the fired bullet
 	 * @param type Bullet type
 	 * @param angle Angle upon creation
 	 * @param position Position upon creation
 	 * @param velocity Velocity upon creation
 	 */
 	private void createNewBullet(int messageFrame, int currentFrame, ArrayList<ProjectileWeapon> weapons, int bulletID, int type, float angle, Vector2 position, Vector2 velocity) {
 		int frameDiff = currentFrame - messageFrame;
 		float framePeriod = 1.0f / FRAMES_PER_SECOND;
 		float dt = frameDiff * framePeriod;
 		
 		int i = 0;
 		ProjectileWeapon weapon;
 		while (i < weapons.size() && weapons.get(i).getType() != type) {
 			i++;
 		}
 		
 		// fire the weapon if it exists
 		if (i < weapons.size()) {
 			weapon = weapons.get(i);
 			Vector2 curPos = position.add(velocity.mul(dt));
 			Body bulletBody = weapon.fire(bulletID, curPos, velocity, angle);
 			
 			// put bullet body reference into world map
 			this.mBullets.put(bulletID, bulletBody);
 		}
 	}
 	
 	/**
 	 * Handle game update messages
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleGameStateMessage(GameStateServerMessage message) {
 		int messageFrame = message.mFrameNum;
 		int currentFrame = this.mFrameNum;
 		
 		if (currentFrame - messageFrame > MAX_GAME_STATE_DELAY) {
 			return;
 		} else if (currentFrame < messageFrame) {
 			this.mFrameNum = messageFrame;
 			currentFrame = messageFrame;
 		}
 		
 		int shipID = message.mShipID;
 		Ship ship = this.mShips.get(shipID);
 		
 		// update the ship if it still exists and its not the local ship
 		if (ship != null && shipID != this.mID) {
 			// update physical ship parameters
 			float angle = message.mOrientation;
 			float angularVelocity = message.mAngularVelocity;
 			Vector2 position = new Vector2(message.mShipPosX, message.mShipPosY);
 			Vector2 velocity = new Vector2(message.mShipVelocityX, message.mShipVelocityY);
 			updateShipBody(ship, messageFrame, currentFrame, angle, angularVelocity, position, velocity);
 		
 			// set nonphysical ship parameters
 			ship.setHealth(message.mHealth);
 			if(message.mHealth < 1000)
 			{
 				int x = 10;
 				x = x;
 			}
 			
 			ship.setEnergy(message.mEnergy);
 			if (message.mShieldActive) {
 				ship.activateShields();
 			} else {
 				ship.deactivateShields();
 			}
 		}
 	}
 	
 	/**
 	 * Handle new bullet messages.
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleNewBulletMessage(NewBulletServerMessage message) {
 		int messageFrame = message.mFrameNum;
 		int currentFrame = this.mFrameNum;
 		
 		if (currentFrame - messageFrame > MAX_GAME_STATE_DELAY) {
 			return;
 		} else if (currentFrame < messageFrame) {
 			this.mFrameNum = messageFrame;
 			currentFrame = messageFrame;
 		} 
 		
 		Ship ship = this.mShips.get(message.mShipID);
 		
 		if (ship != null && message.mShipID != this.mID) {
 			int bulletID = message.mBulletID;
 			int type = message.mBulletType;
 			float angle = message.mAngle;
 			Vector2 position = new Vector2(message.mPosX, message.mPosY);
 			Vector2 velocity = new Vector2(message.mVelocityX, message.mVelocityY);
 			createNewBullet(messageFrame, currentFrame, ship.getAvailableWeapons(), bulletID, type, angle, position, velocity);
 		}
 	}
 	/**
 	 * Handle collision messages.
 	 * Deletes the bullet object from the game, assumes that the game state
 	 * update from the sender will reflect the damage incurred.
 	 * 
 	 * @param message The message to handle
 	 */
 	public void handleCollisionMessage(CollisionServerMessage message) {
 		int bulletID = message.mBulletID;
 		int shipID = message.mShipID;
 		
 		if (shipID != this.mID) {
 			Body bulletBody = this.mBullets.get(bulletID);
 			
 			// delete the bullet if it exists
 			if (bulletBody != null) {
 				bulletBody.setUserData("delete");
 			}
			this.mBullets.remove(bulletID);
 		}
 	}
 	
 	/**
 	 * Gets the next bullet id
 	 * 
 	 * @return Bullet ID
 	 */
 	public int nextBulletID() {
 		int id = this.mNextBulletID;
 		this.mNextBulletID++;
 		return id;
 	}
 	
 	/**
 	 * Registers a new bullet to be sent to other users
 	 * 
 	 * @param bulletID The bullet id
 	 * @param type The bullet type
 	 * @param body The bullet body
 	 * @param position The starting bullet position
 	 * @param velocity The starting velocity
 	 * @param angle The launch angle
 	 * 
 	 * @return True if successful, false otherwise
 	 */
 	public boolean registerBullet(int bulletID, int type, Body body, Vector2 position, Vector2 velocity, float angle) {
 		
 		// add new bullet to table of bullet body objects
 		this.mBullets.put(bulletID, body);
 			
 		// create message
 		NewBulletClientMessage message = (NewBulletClientMessage)this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_NEW_BULLET);
 		message.setNewBullet(this.mFrameNum, this.mID, bulletID, type, velocity.x, velocity.y, position.x, position.y, angle);
 			
 		// send message
 		boolean retval = true;
 		try {
 			this.mServerConnector.sendClientMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 			retval = false;
 		} finally {
 			this.mMessagePool.recycleMessage(message);
 		}
 			
 		return retval;
 	}
 	
 	/**
 	 * Registers a new collision to be sent to other users
 	 * 
 	 * @param bulletID The bullet ID
 	 * @param shipID The ID of the sender (and affected ship)
 	 */
 	public void registerCollision(int bulletID, int shipID) {
 		// create message
 		CollisionClientMessage message = (CollisionClientMessage)this.mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_COLLISION);
 		message.setCollision(bulletID, shipID);
 
 		// remove entry for the given bullet
 		this.mBullets.remove(bulletID);
 		
 		// send message
 		try {
 			this.mServerConnector.sendClientMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		} finally {
 			this.mMessagePool.recycleMessage(message);
 		}
 	}
 	
 	@Override
 	public void onUpdate(final float pSecondsElapsed) {
 		this.mRunnableHandler.onUpdate(pSecondsElapsed);
 		this.mSecondsElapsedAccumulator += pSecondsElapsed;
 
 		final int velocityIterations = this.mVelocityIterations;
 		final int positionIterations = this.mPositionIterations;
 
 		final World world = this.mWorld;
 		final float stepLength = this.mTimeStep;
 		
 		int stepsAllowed = this.mMaximumStepsPerUpdate;
 		
 		while(this.mSecondsElapsedAccumulator >= stepLength && stepsAllowed > 0) {
 			world.step(stepLength, velocityIterations, positionIterations);
 			this.mSecondsElapsedAccumulator -= stepLength;
 			stepsAllowed--;
 			
 			if ((this.mFrameNum % FRAMES_PER_UPDATE) == 0) {
 				this.reportState();
 			}
 			
 			this.mFrameNum++;
 		}
 		
 		this.mPhysicsConnectorManager.onUpdate(pSecondsElapsed);
 	}
 	
 	/**
 	 * Removes the given ship from the list
 	 * 
 	 * @param id The id of the ship to remove
 	 */
 	public void removeShip(int id) {
 		this.mShips.remove(id);
 	}
 	
 	// Getters
 	public int getID() {
 		return this.mID;
 	}
 	
 	public int frameNum() {
 		return this.mFrameNum;
 	}
 	
 	// Setters
 	public void setID(int id) {
 		this.mID = id;
 		this.mNextBulletID = this.mID * 1000000; // makes sure there are enough IDs
 	}
 	
 	public void startGame() {
 		this.mFrameNum = 0;
 	}
 	
 	public void setShips(Map<Integer, Ship> m) {
 		this.mShips = m;
 	}
 	
 	public void setServerConnector(ServerConnector<SocketConnection> connector) {
 		this.mServerConnector = connector;
 		
 		registerMessageHandlers();
 	}
 	
 }

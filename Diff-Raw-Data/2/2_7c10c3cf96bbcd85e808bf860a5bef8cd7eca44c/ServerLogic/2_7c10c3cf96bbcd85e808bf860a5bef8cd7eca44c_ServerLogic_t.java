 /*******************************************************************************
  * Copyright (C) 2012 Henrik Voß, Sven Nobis and Nicolas Gramlich (AndEngine)
  * 
  * This file is part of VHackAndroidGame
  * (https://github.com/SvenTo/VHackAndroidGame)
  * 
  * VHackAndroidGame is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ******************************************************************************/
 package de.dsi8.vhackandroidgame.logic.impl;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.shape.IAreaShape;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.util.math.MathUtils;
 import org.andlabs.andengine.extension.physicsloader.PhysicsEditorLoader;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 
 import de.dsi8.dsi8acl.communication.contract.ICommunicationPartner;
 import de.dsi8.dsi8acl.communication.contract.IServerCommunication;
 import de.dsi8.dsi8acl.communication.contract.IServerCommunicationListener;
 import de.dsi8.dsi8acl.communication.handler.AbstractMessageHandler;
 import de.dsi8.dsi8acl.communication.impl.CommunicationPartner;
 import de.dsi8.dsi8acl.communication.impl.ServerCommunication;
 import de.dsi8.dsi8acl.connection.contract.IConnector;
 import de.dsi8.dsi8acl.connection.model.ConnectionParameter;
 import de.dsi8.dsi8acl.connection.model.Message;
 import de.dsi8.dsi8acl.exception.ConnectionProblemException;
 import de.dsi8.dsi8acl.exception.InvalidMessageException;
 import de.dsi8.vhackandroidgame.RacerGameActivity;
 import de.dsi8.vhackandroidgame.communication.NetworkRectangle;
 import de.dsi8.vhackandroidgame.communication.model.CarMessage;
 import de.dsi8.vhackandroidgame.communication.model.CarMessage.ACTION;
 import de.dsi8.vhackandroidgame.communication.model.CollisionMessage;
 import de.dsi8.vhackandroidgame.communication.model.CollisionType;
 import de.dsi8.vhackandroidgame.communication.model.GameModeMessage;
 import de.dsi8.vhackandroidgame.communication.model.PlayerInfoMessage;
 import de.dsi8.vhackandroidgame.communication.model.QRCodeMessage;
 import de.dsi8.vhackandroidgame.communication.model.QRCodeMessage.QRCodePosition;
 import de.dsi8.vhackandroidgame.handler.DriveMessageHandler;
 import de.dsi8.vhackandroidgame.logic.contract.IServerLogic;
 import de.dsi8.vhackandroidgame.logic.contract.IServerLogicListener;
 import de.dsi8.vhackandroidgame.logic.model.PresentationPartner;
 import de.dsi8.vhackandroidgame.logic.model.RemotePartner;
 import de.stalhut.networkfinderlibrary.UDPServer;
 
 /**
  * The logic on the {@link RacerGameActivity}.
  * 
  * @author Henrik Voß <hennevoss@gmail.com>
  * 
  */
 public class ServerLogic implements IServerLogic, IServerCommunicationListener,
 		ContactListener {
 
 	/**
 	 * Log-Tag.
 	 */
 	private static final String LOG_TAG = ServerLogic.class.getSimpleName();
 
 	/**
 	 * Interface to the {@link RacerGameActivity}.
 	 */
 	private final IServerLogicListener listener;
 
 	/**
 	 * Interface to the server communication.
 	 */
 	private final IServerCommunication communication;
 
 	private final VHackAndroidGameConfiguration gameConfig;
 	private final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1,
 			0.5f, 0.5f);
 
 	private PhysicsWorld mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0),
 			false);
 
 	/**
 	 * All connected remote partner.
 	 */
 	private Map<Integer, RemotePartner> remotePartner = new HashMap<Integer, RemotePartner>();
 
 	/**
 	 * Number of remote partner.
 	 */
 	private int numRemotePartner = 0;
 
 	/**
 	 * 
 	 */
 	private Map<Integer, PresentationPartner> presentationPartner = new HashMap<Integer, PresentationPartner>();
 
 	/**
 	 * Number of presentation partner.
 	 */
 	private int numPresentationPartner = 0;
 
 	private boolean qrCodeVisible = true;
 
 	private UDPServer udpServer = new UDPServer();
 
 	/**
 	 * Creates the logic.
 	 * 
 	 * @param listener
 	 *          Interface to the {@link RacerGameActivity}.
 	 * 
 	 * @param listener
 	 *            Interface to the {@link RacerGameActivity}.
 	 */
 	public ServerLogic(Context context,
 			VHackAndroidGameConfiguration gameConfig,
 			IServerLogicListener listener) {
 		this.listener = listener;
 		this.gameConfig = gameConfig;
 		IConnector connector = gameConfig.getProtocol().createConnector();
 		this.communication = new ServerCommunication(this, connector, 20);
 
 		final PhysicsEditorLoader loader = new PhysicsEditorLoader();
 		try {
 			loader.load(context, this.mPhysicsWorld, "track.xml",
 					(IAreaShape) null, false, false);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 
 		Body goalBody = createBoxBody(this.mPhysicsWorld, 1095, 810, 80, 120,
 				0, BodyType.StaticBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0, true));
 		goalBody.setUserData("goal");
 
 		Body firstCheckpointBody = createBoxBody(this.mPhysicsWorld, 1085, 535,
 				160, 55, 0, BodyType.StaticBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0, true));
 		firstCheckpointBody.setUserData("first");
 
 		Body secondCheckpointBody = createBoxBody(this.mPhysicsWorld, 915, 100,
 				55, 150, 0, BodyType.StaticBody,
 				PhysicsFactory.createFixtureDef(0, 0, 0, true));
 		secondCheckpointBody.setUserData("second");
 
 		mPhysicsWorld.setContactListener(this);
 		
 		new UpdateThread().start();
 	}
 
 
 	public static Body createBoxBody(final PhysicsWorld pPhysicsWorld,
 			final float pX, final float pY, final float pWidth,
 			final float pHeight, final float pRotation,
 			final BodyType pBodyType, final FixtureDef pFixtureDef) {
 		final BodyDef boxBodyDef = new BodyDef();
 		boxBodyDef.type = pBodyType;
 
 		boxBodyDef.position.x = pX - 1920 / 2 - pWidth * 0.5f;
 		boxBodyDef.position.y = pY - 1080 / 2 - pHeight * 0.5f;
 
 		final Body boxBody = pPhysicsWorld.createBody(boxBodyDef);
 
 		final PolygonShape boxPoly = new PolygonShape();
 
 		final float halfWidth = pWidth * 0.5f
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 		final float halfHeight = pHeight * 0.5f
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 
 		boxPoly.setAsBox(halfWidth, halfHeight);
 		pFixtureDef.shape = boxPoly;
 
 		boxBody.createFixture(pFixtureDef);
 
 		boxPoly.dispose();
 
 		boxBody.setTransform(boxBody.getWorldCenter(),
 				MathUtils.degToRad(pRotation));
 
 		return boxBody;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start() {
 		this.communication.startListen();
 		this.udpServer.start();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void close() {
 		this.communication.close();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void newPartner(ICommunicationPartner partner) {
 		Log.i(LOG_TAG, "newPartner");
 		partner
 				.registerMessageHandler(new AbstractMessageHandler<GameModeMessage>() {
 					@Override
 					public void handleMessage(CommunicationPartner partner,
 							GameModeMessage message) throws InvalidMessageException {
 						if (message.remote) {
 							newRemotePartner(partner);
 						} else {
 							newPresentationPartner(partner);
 						}
 					}
 				});
 
 		// this.listener.addCar(partner.getId());
 		// TODO send addCar to the GamePresentationLogic
 	}
 
 	/**
 	 * A new remote partner is connecting.
 	 * 
 	 * @param partner
 	 *          the new remote partner
 	 */
 	private void newRemotePartner(CommunicationPartner partner) {
 		RemotePartner rPartner = new RemotePartner();
 		partner.registerMessageHandler(new DriveMessageHandler(this));
 		rPartner.communicationPartner = partner;
 		rPartner.id = this.numRemotePartner++;
 
 		final float PX = 230;
 		final float PY = 280;
 		final float ROTATION = 0;
 
 		// TODO make network-magic to the rectangle
 		Rectangle rectangle = new NetworkRectangle(rPartner.id, this, PX, PY,
 				RacerGameActivity.CAR_SIZE, RacerGameActivity.CAR_SIZE);
 
 		rPartner.body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, rectangle,
 				BodyType.DynamicBody, PhysicsFactory.createFixtureDef(1, 0.1f, 0.5f));
 
 		rPartner.physicsConnector = new PhysicsConnector(rectangle, rPartner.body,
 				true, true);
 
 		this.mPhysicsWorld.registerPhysicsConnector(rPartner.physicsConnector);
 
 		this.remotePartner.put(rPartner.id, rPartner);
 
 		CarMessage message = new CarMessage();
 		message.positionX = PX;
 		message.positionY = PY;
 		message.rotation = ROTATION;
 		message.action = ACTION.ADD;
 		message.id = rPartner.id;
 		sendMessageToAllPresentationPartner(message);
 
 		rPartner.communicationPartner.sendMessage(new PlayerInfoMessage("Player " + rPartner.id, rPartner.id));
 		
 		this.listener.registerPlayer(rPartner.id);
 	}
 
 	/**
 	 * A new presentation partner is connecting.
 	 * 
 	 * @param partner
 	 *          the new presentation partner
 	 */
 	private void newPresentationPartner(CommunicationPartner partner) {
 		PresentationPartner pPartner = new PresentationPartner();
 		pPartner.communicationPartner = partner;
 		pPartner.id = this.numPresentationPartner++;
 
 		this.presentationPartner.put(pPartner.id, pPartner);
 		
 		for (RemotePartner rPartner : this.remotePartner.values()) {
 			CarMessage carMessage = new CarMessage();
 			carMessage.action = ACTION.ADD;
 			carMessage.id = rPartner.id;
 			pPartner.communicationPartner.sendMessage(carMessage);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void connectionLost(ICommunicationPartner partner,
 			ConnectionProblemException ex) {
 		Log.i(LOG_TAG, "connectionLost", ex);
 		int idOfRemotePartner = getIdOfRemotePartner(partner);
 		RemotePartner rPartner = this.remotePartner.get(idOfRemotePartner);
 		if (rPartner != null) {
 			this.mPhysicsWorld.unregisterPhysicsConnector(rPartner.physicsConnector);
 			CarMessage carMessage = new CarMessage();
 			carMessage.action = ACTION.REMOVE;
 			carMessage.id = rPartner.id;
 			sendMessageToAllPresentationPartner(carMessage);
 
 			rPartner.body = null;
 			rPartner.physicsConnector = null;
 			rPartner.communicationPartner.close();
 			rPartner.communicationPartner = null;
 			this.remotePartner.remove(rPartner.id);
 			this.listener.removePlayer(rPartner.id);
 		}
 
 		int idOfPresentationPartner = getIdOfPresentationPartner(partner);
 		PresentationPartner pPartner = this.presentationPartner
 				.get(idOfPresentationPartner);
 		if (pPartner != null) {
 			pPartner.communicationPartner.close();
 			pPartner.communicationPartner = null;
 			this.presentationPartner.remove(pPartner.id);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void socketListenerProblem(Exception ex) {
 		Log.e(LOG_TAG, "socketListenerProblem", ex);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void collisionDetected(int carId, CollisionType collidesWith) {
 		this.remotePartner.get(carId)
 						  .communicationPartner
 						  .sendMessage(new CollisionMessage(collidesWith));
 	}
 
 	/**
 	 * Return's the id of an remote partner.
 	 * 
 	 * @param partner
 	 *          the remote partner is to be returned to the id
 	 * @return id of the remote partner
 	 */
 	private int getIdOfRemotePartner(ICommunicationPartner partner) {
 		Iterator<Entry<Integer, RemotePartner>> iterator = this.remotePartner
 				.entrySet().iterator();
 		while (iterator.hasNext()) {
 			Entry<Integer, RemotePartner> next = iterator.next();
 			if (partner == next.getValue().communicationPartner) {
 				return next.getKey().intValue();
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Return's the id of an remote partner.
 	 * 
 	 * @param partner
 	 *          the remote partner is to be returned to the id
 	 * @return id of the remote partner
 	 */
 	private int getIdOfPresentationPartner(ICommunicationPartner partner) {
 		Iterator<Entry<Integer, PresentationPartner>> iterator = this.presentationPartner
 				.entrySet().iterator();
 		while (iterator.hasNext()) {
 			Entry<Integer, PresentationPartner> next = iterator.next();
 			if (partner == next.getValue().communicationPartner) {
 				return next.getKey().intValue();
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public void onCreateScene() {
 		this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0),
 				false, 8, 1);
 
 		this.mPhysicsWorld.setContactListener(this);
 	}
 
 	@Override
 	public void beginContact(Contact contact) {
 		int firstCarId = getCarIdFromBody(contact.getFixtureA().getBody());
 
 		if (firstCarId > -1) {
 			collisionDetected(firstCarId, getCollisionType(contact.getFixtureA()));
 			checkCheckpointCollision(contact, firstCarId);
 		}
 
 		int secondCarId = getCarIdFromBody(contact.getFixtureB().getBody());
 		if (secondCarId > -1) {
 			collisionDetected(secondCarId, getCollisionType(contact.getFixtureB()));
 			checkCheckpointCollision(contact, secondCarId);
 		}
 	}
 	
 	public CollisionType getCollisionType(Fixture fixture) {
 		int carId = getCarIdFromBody(fixture.getBody());
 		if(carId > -1) {
 			return CollisionType.CAR;
 		} else if(fixture.getRestitution() > 0) {
 			return CollisionType.BUMPER;
 		} else {
 			return CollisionType.WALL;
 		}
 	}
 	
 	private void checkCheckpointCollision(Contact contact, int carId) {
 		final RemotePartner partner = this.remotePartner.get(carId);
 		if ("goal".equals(contact.getFixtureA().getBody().getUserData())
 				|| "goal".equals(contact.getFixtureA().getBody().getUserData())) {
 			checkpointsPassed(partner);
 		} else if ("first"
 				.equals(contact.getFixtureA().getBody().getUserData())
 				|| "first"
 						.equals(contact.getFixtureA().getBody().getUserData())) {
 			checkpointsPassed(partner);
 
 		} else if ("second".equals(contact.getFixtureA().getBody()
 				.getUserData())
 				|| "second".equals(contact.getFixtureA().getBody()
 						.getUserData())) {
 			checkpointsPassed(partner);
 
 		}
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) { /* Not required */
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) { /* Not required */
 	}
 
 	@Override
 	public void endContact(Contact arg0) { /* Not required */
 	}
 
 	private int getCarIdFromBody(Body body) {
 		for (RemotePartner rPartner : this.remotePartner.values()) {
 			if (rPartner.body == body) {
 				return rPartner.id;
 			}
 		}
 
 		return -1;
 	}
 
 	public Body getCarBody(CommunicationPartner partner) {
 		for (RemotePartner rPartner : this.remotePartner.values()) {
 			if (rPartner.communicationPartner == partner) {
 				return rPartner.body;
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public PhysicsWorld getPhysicsWorld() {
 		return this.mPhysicsWorld;
 	}
 
 	public void sendMessageToAllPresentationPartner(Message message) {
 		for (PresentationPartner p : this.presentationPartner.values()) {
 			p.communicationPartner.sendMessage(message);
 		}
 	}
 
 	public class UpdateThread extends Thread {
 		@Override
 		public void run() {
 			long lastUpdate = System.currentTimeMillis();
 			while (true) {
 				final long now = System.currentTimeMillis();
 				final float timeElapsed = (now - lastUpdate) / 1000f;
 				ServerLogic.this.mPhysicsWorld.onUpdate(timeElapsed);
 				lastUpdate = now;
 				try {
 					sleep(20);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					break;
 				}
 			}
 		}
 	}
 
 	public void checkpointsPassed(RemotePartner remotePartner) {
 		this.listener.incrementCheckpointsPassed(remotePartner.id);
 	}
 
 	@Override
 	public void showBardcode() {
 		if (qrCodeVisible) {
 			sendMessageToAllPresentationPartner(new QRCodeMessage(null, QRCodePosition.CENTER));
 		} else {
 			ConnectionParameter connectionDetails = gameConfig
 					.getConnectionDetails();
 			sendMessageToAllPresentationPartner(new QRCodeMessage(connectionDetails
 					.toConnectionURL(), QRCodePosition.CENTER));
 		}
 		qrCodeVisible = !qrCodeVisible;
 
 	}
 }

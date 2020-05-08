 /*
  * The Init terrain and init materials functions were both taken from the JME example code
  * and modified. The rest of the code is almost entirely written from scratch.
  */
 
 /*
  * Copyright (c) 2009-2010 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package mygame;
 
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.Bone;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.messages.InputMessages;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.PhysicsTickListener;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Client;
 import com.jme3.network.connection.Server;
 import com.jme3.network.events.ConnectionListener;
 import com.jme3.network.events.MessageListener;
 import com.jme3.network.message.Message;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.network.sync.ServerSyncService;
 import com.jme3.scene.Node;
 import com.jme3.system.AppSettings;
 import com.jme3.system.JmeContext;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.util.SkyFactory;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import jme3tools.converters.ImageToAwt;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 import mygame.messages.CharStatusMessage;
 import mygame.messages.ClientReadyMessage;
 import mygame.messages.HasID;
 
 public class BladeServer extends SimpleApplication implements MessageListener,ConnectionListener{
     ConcurrentHashMap<Long,Node> modelMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Vector3f> upperArmAnglesMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Vector3f> upperArmVelsMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Float> elbowWristAngleMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Float> elbowWristVelMap=new ConcurrentHashMap();
     HashSet<Long> playerSet=new HashSet();
     ConcurrentHashMap<Long,Client> clientMap=new ConcurrentHashMap();
     ConcurrentHashMap<Client,Long> playerIDMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Vector3f> charPositionMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Vector3f> charVelocityMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Float> charAngleMap=new ConcurrentHashMap();
     ConcurrentHashMap<Long,Float> charTurnVelMap=new ConcurrentHashMap();
 
     ConcurrentHashMap<Long, Deque<Vector3f[]>> prevStates = new ConcurrentHashMap();
    
     ConcurrentHashMap<Long, Float> charLifeMap = new ConcurrentHashMap();
 
     private final long timeBetweenSyncs=10;
     private final int numPrevStates = 9;
     private final int goBackNumStates = 3;
     private long timeOfLastSync=0;
     
     private long currentPlayerID=0;
     private static BladeServer app;
     private BulletAppState bulletAppState;
     private TerrainQuad terrain;
     Material mat_terrain;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     private RigidBodyControl terrain_phy;
     private RigidBodyControl basic_phy;
     private boolean updateNow;
     float airTime = 0;
 
     Server server;
     ServerSyncService serverSyncService;
 
     public static void main(String[] args) {
         app = new BladeServer();
         AppSettings appSettings=new AppSettings(true);
         appSettings.setFrameRate(60);
         app.setSettings(appSettings);
         //app.start();
         app.start(JmeContext.Type.Headless);
     }
 
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(CharStatusMessage.class);
         Serializer.registerClass(CharCreationMessage.class);
         Serializer.registerClass(CharDestructionMessage.class);
         Serializer.registerClass(ClientReadyMessage.class);
         InputMessages.registerInputClasses();
         
         try {
             server = new Server(BladeMain.port,BladeMain.port);
             server.start();
         }
         catch(Exception e){
             e.printStackTrace();
         }
 
         InputMessages.addInputMessageListeners(server, this);
         server.addConnectionListener(this);
         server.addMessageListener(this,CharCreationMessage.class,CharDestructionMessage.class,CharStatusMessage.class,ClientReadyMessage.class);
 
         flyCam.setMoveSpeed(50);
         bulletAppState = new BulletAppState();
         
         stateManager.attach(bulletAppState);
         rootNode.attachChild(SkyFactory.createSky(
         assetManager, "Textures/Skysphere.jpg", true));
         initMaterials();
         initTerrain();
 
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
 
         flyCam.setEnabled(true);
         this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().enableDebug(this.getAssetManager());
         
         PhysicsCollisionListener physListener = new PhysicsCollisionListener() {
 
             public void collision(PhysicsCollisionEvent event) {
 
                 PhysicsCollisionObject a = event.getObjectA();
                 PhysicsCollisionObject b = event.getObjectB();
                 
                 if ((a != null && b != null && a instanceof ControlID && b instanceof ControlID
                         && ((ControlID)a).getID() != ((ControlID)b).getID())) {
                     
                     System.out.println("Collision!");
                     //System.out.println("A: " + a.getOverlappingCount()
                     //        + " B: " + b.getOverlappingCount());
                     
                     long hurtPlayerID;
                     if((a instanceof SwordControl) && (b instanceof BodyControl)){
                         hurtPlayerID=((ControlID)b).getID();
                         charLifeMap.put(hurtPlayerID, charLifeMap.get(hurtPlayerID)*0.999f);
                     }
                     else if((b instanceof SwordControl) && (a instanceof BodyControl)){
                         hurtPlayerID=((ControlID)a).getID();
                         charLifeMap.put(hurtPlayerID, charLifeMap.get(hurtPlayerID)*0.999f);
                     }
                     
                     long playerID1 = Long.valueOf(((ControlID)a).getID());
                     long playerID2 = Long.valueOf(((ControlID)b).getID());
                     
                     Deque player1Deque = prevStates.get(playerID1);
                     Deque player2Deque = prevStates.get(playerID2);
                     
                     // go back some number of states
                     for (int i = 1; i < goBackNumStates; i++) {
                         player1Deque.pollLast();
                         player2Deque.pollLast();
                     }
                     
                     Vector3f[] p1State = (Vector3f[])player1Deque.pollLast();
                     Vector3f[] p2State = (Vector3f[])player2Deque.pollLast();
                     
                     // replace the removed states
                     
                     Vector3f[] p1Next = (Vector3f[])player1Deque.pollLast();
                     Vector3f[] p2Next = (Vector3f[])player2Deque.pollLast();
                     
                     for (int i = 0; i < goBackNumStates; i++) {
                         player1Deque.offerLast(p1Next);
                         player2Deque.offerLast(p2Next);
                     }
                     
                     // reposition the character as recorded in the previous state
                     upperArmAnglesMap.put(playerID1, p1State[0]);
                     upperArmAnglesMap.put(playerID2, p2State[0]);
 
                     elbowWristAngleMap.put(playerID1, p1State[1].getX());
                     elbowWristAngleMap.put(playerID2, p2State[1].getX());
 
                     charAngleMap.put(playerID1, p1State[1].getY());
                     charAngleMap.put(playerID2, p2State[1].getY());
 
                     charPositionMap.put(playerID1, p1State[2]);
                     charPositionMap.put(playerID2, p2State[2]);
                     
                     modelMap.get(playerID1).getControl(CharacterControl.class).setPhysicsLocation(charPositionMap.get(playerID1));
                     modelMap.get(playerID2).getControl(CharacterControl.class).setPhysicsLocation(charPositionMap.get(playerID2));
                     updateCharacters(timer.getTimePerFrame());
                     
                     //System.out.println("A1: " + a.getOverlappingCount()
                     //        + " B1: " + b.getOverlappingCount());
                     /* zeroing out velocities
                     // rotateStop
                     upperArmVelsMap.get(playerID1).z = 0;
                     upperArmVelsMap.get(playerID2).z = 0;
 
                     // stopMouseMovement
                     upperArmVelsMap.get(playerID1).x = upperArmVelsMap.get(playerID1).y = 0;
                     upperArmVelsMap.get(playerID2).x = upperArmVelsMap.get(playerID2).y = 0;
 
                     // stop arm
                     elbowWristVelMap.put(playerID1, 0f);
                     elbowWristVelMap.put(playerID2, 0f);
 
                     // stop char turn
                     charTurnVelMap.put(playerID1, 0f);
                     charTurnVelMap.put(playerID2, 0f);
 
                     // stop forward move
                     charVelocityMap.get(playerID1).z = 0;
                     charVelocityMap.get(playerID2).z = 0;
 
                     // stop left right move
                     charVelocityMap.get(playerID1).x = 0;
                     charVelocityMap.get(playerID2).x = 0;
                      * 
                      */
                     
                     /*
                     upperArmVelsMap.put(playerID1, upperArmVelsMap.get(playerID1).mult(-1.0f));
                     upperArmVelsMap.put(playerID2, upperArmVelsMap.get(playerID2).mult(-1.0f));
                     elbowWristVelMap.put(playerID1, elbowWristVelMap.get(playerID1)*-1.0f);
                     elbowWristVelMap.put(playerID2, elbowWristVelMap.get(playerID2)*-1.0f);
                     charVelocityMap.put(playerID1, charVelocityMap.get(playerID1).mult(-1.0f));
                     charVelocityMap.put(playerID2, charVelocityMap.get(playerID2).mult(-1.0f));
                     charTurnVelMap.put(playerID1, charTurnVelMap.get(playerID1)*-1.0f);
                     charTurnVelMap.put(playerID2, charTurnVelMap.get(playerID2)*-1.0f);
                      * 
                      * 
                      */
                     //updateNow = true;
                 }
             }
         };
            
         PhysicsTickListener physTickListener = new PhysicsTickListener() {
             public void prePhysicsTick(PhysicsSpace space, float f) {
                 updateCharacters(timer.getTimePerFrame());    
                 //System.out.println("tpf: " + timer.getTimePerFrame() + " fps: " + timer.getFrameRate());
             }
 
             public void physicsTick(PhysicsSpace space, float f) {
                 updateClients();
             }
         };
         
         //bulletAppState.getPhysicsSpace().setAccuracy((float)(1.0/120.0));
         System.out.println("Accuracy: " + bulletAppState.getPhysicsSpace().getAccuracy());
         
         this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addCollisionListener(physListener);
         this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addTickListener(physTickListener);
         updateNow = false;
     }
 
     @Override
     public void simpleUpdate(float tpf){
         //updateCharacters(tpf);
     }
 
     public void updateCharacters(float tpf) {
         for(Iterator<Long> playerIterator=playerSet.iterator(); playerIterator.hasNext();){
             long playerID = playerIterator.next();
             Vector3f upperArmAngles = upperArmAnglesMap.get(playerID);
             
             Vector3f[] prevState = new Vector3f[3];
             
             prevState[0] = upperArmAnglesMap.get(playerID);
             upperArmAnglesMap.put(playerID, CharMovement.extrapolateUpperArmAngles(upperArmAngles,
                     upperArmVelsMap.get(playerID), tpf));
 
             prevState[1] = new Vector3f(elbowWristAngleMap.get(playerID), 0f, 0f);
             elbowWristAngleMap.put(playerID, CharMovement.extrapolateLowerArmAngles(elbowWristAngleMap.get(playerID),
                     elbowWristVelMap.get(playerID), tpf));
 
             prevState[1].setY(charAngleMap.get(playerID));
             charAngleMap.put(playerID, CharMovement.extrapolateCharTurn(charAngleMap.get(playerID),
                     charTurnVelMap.get(playerID), tpf));
 
             CharacterControl control=modelMap.get(playerID).getControl(CharacterControl.class);
             float xDir,zDir;
             zDir=FastMath.cos(charAngleMap.get(playerID));
             xDir=FastMath.sin(charAngleMap.get(playerID));
             Vector3f viewDirection=new Vector3f(xDir,0,zDir);
             control.setViewDirection(viewDirection);
 
             Vector3f forward,up,left;
             float xVel,zVel;
             xVel=charVelocityMap.get(playerID).x;
             zVel=charVelocityMap.get(playerID).z;
             forward=new Vector3f(viewDirection);
             up=new Vector3f(0,1,0);
             left=up.cross(forward);
 
             control.setWalkDirection(left.mult(xVel).add(forward.mult(zVel)));
             
             prevState[2] = charPositionMap.get(playerID).clone();
             charPositionMap.get(playerID).set(modelMap.get(playerID).getControl(CharacterControl.class).getPhysicsLocation()); // getLocalTranslation
             
             CharMovement.setUpperArmTransform(upperArmAnglesMap.get(playerID), modelMap.get(playerID));
             CharMovement.setLowerArmTransform(elbowWristAngleMap.get(playerID), modelMap.get(playerID));
 
             // Adjust the sword collision shape in accordance with arm movement.
             // first, get rotation and position of hand
             Bone hand = modelMap.get(playerID).getControl(AnimControl.class).getSkeleton().getBone("swordHand");
             Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
             Vector3f position = hand.getModelSpacePosition();
 
             // set the position of the sword to the position of the hand
             Node swordNode = (Node) modelMap.get(playerID).getChild("sword");
             Bone swordBone = swordNode.getControl(AnimControl.class).getSkeleton().getBone("swordBone");
             swordNode.setLocalRotation(rotation);
             swordNode.setLocalTranslation(position);
 
             // adjust for difference in rotation
             Quaternion swordRot = swordBone.getModelSpaceRotation();
             Quaternion adjust = (new Quaternion()).fromAngles(FastMath.HALF_PI, 0, 0);
             Matrix3f swordRotMat = swordRot.mult(adjust).toRotationMatrix();
 
             // adjust for difference in position of wrist and middle of sword
             Vector3f shiftPosition = swordRot.mult(new Vector3f(0f, 1.8f, 0f));
 
             // build new collision shape
             CompoundCollisionShape cShape = new CompoundCollisionShape();
             Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
             cShape.addChildShape(new BoxCollisionShape(boxSize), Vector3f.ZERO, swordRotMat);
             CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);
 
             // remove GhostControl from PhysicsSpace, apply change, put in PhysicsSpace
             SwordControl sword = modelMap.get(playerID).getChild("sword").getControl(SwordControl.class);
             bulletAppState.getPhysicsSpace().remove(sword);
             sword.setCollisionShape(cShape);
             bulletAppState.getPhysicsSpace().add(sword);
 
             // get rid of oldest, add newest previous state
             if (prevStates.get(playerID).size() >= numPrevStates) {
                 prevStates.get(playerID).pollFirst();
             }
             prevStates.get(playerID).offerLast(prevState);
         }
     }
     
     public void updateClients() {
         long currentTime = System.currentTimeMillis();
         if ((currentTime - timeOfLastSync > timeBetweenSyncs)|| updateNow) {
             timeOfLastSync = currentTime;
             List<Long> playerList=new LinkedList();
             playerList.addAll(playerSet);
             for (Long sourcePlayerID:playerList) {
                 for (Long destPlayerID:playerList) {
                     try {
                         clientMap.get(destPlayerID).send(new CharStatusMessage(upperArmAnglesMap.get(sourcePlayerID), 
                                 upperArmVelsMap.get(sourcePlayerID),charPositionMap.get(sourcePlayerID),
                                 charVelocityMap.get(sourcePlayerID),elbowWristAngleMap.get(sourcePlayerID),
                                 elbowWristVelMap.get(sourcePlayerID),charAngleMap.get(sourcePlayerID),
                                 charTurnVelMap.get(sourcePlayerID),sourcePlayerID,charLifeMap.get(sourcePlayerID)));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (NullPointerException ex){
                         if(playerSet.contains(destPlayerID))
                             playerSet.remove(destPlayerID); // if the client has disconnected, remove its id
                     }
                 }
             }
             updateNow = false;
         }
     }
 
     public void initTerrain() {
 
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
 
         mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("Textures/alpha1.1.png"));
 
         Texture grass = assetManager.loadTexture("Textures/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex1", grass);
         mat_terrain.setFloat("m_Tex1Scale", 64f);
 
         Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
         dirt.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex2", dirt);
         mat_terrain.setFloat("m_Tex2Scale", 32f);
 
         Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
         rock.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex3", rock);
         mat_terrain.setFloat("m_Tex3Scale", 128f);
 
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
         heightmap = new ImageBasedHeightMap(
                 ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
         heightmap.load();
         terrain = new TerrainQuad("my terrain", 65, 1025, heightmap.getHeightMap());
         terrain.setMaterial(mat_terrain);
 
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 2f, 2f);
 
         rootNode.attachChild(terrain);
 
         terrain_phy = new RigidBodyControl(0.0f);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
         terrain.addControl(terrain_phy);
         bulletAppState.getPhysicsSpace().add(terrain_phy);
 
     }
 
     public void initMaterials() {
         wall_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key = new TextureKey("Textures/road.jpg");
         key.setGenerateMips(true);
         Texture tex = assetManager.loadTexture(key);
         wall_mat.setTexture("ColorMap", tex);
 
         stone_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key2 = new TextureKey("Textures/road.jpg");
         key2.setGenerateMips(true);
 
         Texture tex2 = assetManager.loadTexture(key2);
         stone_mat.setTexture("ColorMap", tex2);
 
         floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key3 = new TextureKey("Textures/grass.jpg");
         key3.setGenerateMips(true);
         Texture tex3 = assetManager.loadTexture(key3);
         tex3.setWrap(WrapMode.Repeat);
         floor_mat.setTexture("ColorMap", tex3);
 
     }
 
     public void messageReceived(Message message) {
         
 
         if (message instanceof ClientReadyMessage) {
             try {
                 long newPlayerID = currentPlayerID++;
                 Client client = message.getClient();
                 System.out.println("Received ClientReadyMessage");
 
                 final Node model = Character.createCharacter("Models/Female.mesh.xml", "Models/sword.mesh.xml", assetManager, bulletAppState, true, newPlayerID);
                 
                 //rootNode.attachChild(model);
                 //rootNode.attachChild(geom1);
                 modelMap.put(newPlayerID, model);
                 upperArmAnglesMap.put(newPlayerID, new Vector3f());
                 upperArmVelsMap.put(newPlayerID, new Vector3f());
                 elbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
                 elbowWristVelMap.put(newPlayerID, new Float(0f));
                 charPositionMap.put(newPlayerID, new Vector3f());
                 charVelocityMap.put(newPlayerID, new Vector3f());
                 charAngleMap.put(newPlayerID, 0f);
                 charTurnVelMap.put(newPlayerID, 0f);
                 charLifeMap.put(newPlayerID, 1f);
 
                 prevStates.put(newPlayerID, new ArrayDeque<Vector3f[]>(numPrevStates));
                 
                 client.send(new CharCreationMessage(newPlayerID, true));
                 for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
                     long destPlayerID = playerIterator.next();
                     clientMap.get(destPlayerID).send(new CharCreationMessage(newPlayerID, false)); // send this new character to all other clients
                     client.send(new CharCreationMessage(destPlayerID, false)); // send all other client's characters to this client
                     System.out.println("Sent CharCreationMessage");
                 }
                 System.out.println("client connected:" + newPlayerID + "," + client);
                 playerSet.add(newPlayerID);
                 clientMap.put(newPlayerID, client);
                 playerIDMap.put(client, newPlayerID);
                 
                 Future action = app.enqueue(new Callable() {
                     
                     public Object call() throws Exception {
                         rootNode.attachChild(model);
                         return null;
                     }
                 });
                 //to retrieve return value (waits for call to finish, fire&forget otherwise):
                 //action.get();
                 
             } catch (IOException ex) {
                 Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             HasID hasID=(HasID)message;
             long playerID=hasID.getID();
             
             if (playerSet.contains(playerID)) {
                 if (message instanceof InputMessages.RotateUArmCC) {
                     System.out.println("rotateCC");
                     upperArmVelsMap.get(playerID).z = -1;
                 } else if (message instanceof InputMessages.RotateUArmC) {
                     System.out.println("rotateC");
                     upperArmVelsMap.get(playerID).z = 1;
                 } else if (message instanceof InputMessages.StopRotateTwist) {
                     System.out.println("rotateStop");
                     upperArmVelsMap.get(playerID).z = 0;
                 } else if (message instanceof InputMessages.MouseMovement) {
                     InputMessages.MouseMovement mouseMovement = (InputMessages.MouseMovement) message;
                     upperArmVelsMap.get(playerID).x = FastMath.cos(mouseMovement.angle);
                     upperArmVelsMap.get(playerID).y = FastMath.sin(mouseMovement.angle);
                 } else if (message instanceof InputMessages.StopMouseMovement) {
                     upperArmVelsMap.get(playerID).x = upperArmVelsMap.get(playerID).y = 0;
                 } else if (message instanceof InputMessages.LArmUp) {
                     System.out.println("arm up");
                     elbowWristVelMap.put(playerID, 1f);
                 } else if (message instanceof InputMessages.LArmDown) {
                     System.out.println("arm down");
                     elbowWristVelMap.put(playerID, -1f);
                 } else if (message instanceof InputMessages.StopLArm) {
                     System.out.println("arm stop");
                     elbowWristVelMap.put(playerID, 0f);
                 } else if (message instanceof InputMessages.MoveCharBackword) {
                     System.out.println("Move foreward");
                     charVelocityMap.get(playerID).z = -CharMovement.charBackwordSpeed;
                 } else if (message instanceof InputMessages.MoveCharForward) {
                     System.out.println("Move backword");
                     charVelocityMap.get(playerID).z = CharMovement.charForwardSpeed;
                 } else if (message instanceof InputMessages.MoveCharLeft) {
                     System.out.println("Move left");
                     charVelocityMap.get(playerID).x = CharMovement.charStrafeSpeed;
                 } else if (message instanceof InputMessages.MoveCharRight) {
                     System.out.println("Move right");
                     charVelocityMap.get(playerID).x = -CharMovement.charStrafeSpeed;
                 } else if (message instanceof InputMessages.TurnCharLeft) {
                     charTurnVelMap.put(playerID, 1f);
                 } else if (message instanceof InputMessages.TurnCharRight) {
                     charTurnVelMap.put(playerID, -1f);
                 } else if (message instanceof InputMessages.StopCharTurn) {
                     charTurnVelMap.put(playerID, 0f);
                 } else if (message instanceof InputMessages.StopForwardMove) {
                     charVelocityMap.get(playerID).z = 0;
                 } else if (message instanceof InputMessages.StopLeftRightMove) {
                     System.out.println("Stop Left Right Move");
                     charVelocityMap.get(playerID).x = 0;
                 }
             } else {
                 System.out.println("PlayerID " + playerID + " is not in the set");
             }
         }
     }
 
     public void messageSent(Message message) {
   //      System.out.println("Sending message to "+message.getClient());
   //      System.out.println("Sending message "+message.getClass());
     }
 
     public void objectReceived(Object object) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void objectSent(Object object) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void clientConnected(Client client) {
          
     }
 
     public void clientDisconnected(Client client) {
         System.out.println("client disconnecting is " + client);
         final long playerID = playerIDMap.get(client);
         List<Long> players = new LinkedList();
         Node model=modelMap.get(playerID);
         bulletAppState.getPhysicsSpace().remove(model.getChild("sword").getControl(SwordControl.class));
         bulletAppState.getPhysicsSpace().remove(model.getControl(BodyControl.class));
         bulletAppState.getPhysicsSpace().remove(model.getControl(CharacterControl.class));
         //rootNode.detachChild(modelMap.get(playerID));
         playerIDMap.remove(client);
         clientMap.remove(playerID);
         players.addAll(playerSet);
         playerSet.remove(playerID);
         players.remove(playerID);
         prevStates.remove(playerID);
         for (Long destID : players) {
             try {
                 clientMap.get(destID).send(new CharDestructionMessage(playerID));
             } catch (IOException ex) {
                 Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         Future action = app.enqueue(new Callable() {
 
             public Object call() throws Exception {
                 rootNode.detachChild(modelMap.get(playerID));
                 return null;
             }
         });
         //to retrieve return value (waits for call to finish, fire&forget otherwise):
         //action.get();
        modelMap.remove(playerID);
     }
 }

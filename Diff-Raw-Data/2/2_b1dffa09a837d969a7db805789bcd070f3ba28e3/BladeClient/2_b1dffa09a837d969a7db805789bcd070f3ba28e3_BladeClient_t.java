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
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import mygame.messages.InputMessages;
 import mygame.messages.CharPositionMessage;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Client;
 import com.jme3.network.events.ConnectionListener;
 import com.jme3.network.events.MessageListener;
 import com.jme3.network.message.Message;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Node;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jme3tools.converters.ImageToAwt;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 
 /**
  *
  * @author blah
  */
 public class BladeClient extends SimpleApplication implements MessageListener, RawInputListener, ConnectionListener, AnimEventListener {
 
     private ChaseCamera chaseCam;
     private Node model;
     HashMap<Long, Node> modelMap = new HashMap();
     HashMap<Long, Vector3f> upperArmAnglesMap = new HashMap();
     HashMap<Long, Vector3f> upperArmVelsMap = new HashMap();
     HashMap<Long, Float> elbowWristAngleMap = new HashMap();
     HashMap<Long, Float> elbowWristVelMap = new HashMap();
     HashSet<Long> playerSet = new HashSet();
     HashMap<Long, Vector3f> charPositionMap = new HashMap();
     HashMap<Long, Vector3f> charVelocityMap = new HashMap();
     HashMap<Long, Float> charAngleMap = new HashMap();
     HashMap<Long, Float> charTurnVelMap = new HashMap();
     HashMap<Long, AnimChannel> animChannelMap = new HashMap();
     private BulletAppState bulletAppState;
     private TerrainQuad terrain;
     Material mat_terrain;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     private RigidBodyControl terrain_phy;
     CharacterControl character;
     Client client;
     boolean clientSet = false;
     private Vector3f upperArmAngles = new Vector3f();
     private Vector3f upperArmVels = new Vector3f();
     private float elbowWristAngle = CharMovement.Constraints.lRotMin;
     private float elbowWristVel = 0;
     private long playerID = 0;
 
     public static void main(String[] args) {
         BladeClient app = new BladeClient();
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(CharPositionMessage.class);
         Serializer.registerClass(CharCreationMessage.class);
         Serializer.registerClass(CharDestructionMessage.class);
         InputMessages.registerInputClasses();
 
 
         flyCam.setMoveSpeed(50);
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         initMaterials();
         initTerrain();
 
         try {
             client = new Client(BladeMain.serverIP, BladeMain.port, BladeMain.port);
             client.start();
 
             client.addMessageListener(this, CharCreationMessage.class, CharDestructionMessage.class, CharPositionMessage.class);
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         InputMessages.addInputMessageListeners(client, this);
 
         client.addConnectionListener(this);
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
 
 
         flyCam.setEnabled(false);
 
 
 
 
     }
     private boolean mouseCurrentlyStopped = true;
 
     @Override
     public void simpleUpdate(float tpf) {
         if (clientSet) {
             characterUpdate(tpf);
             if ((System.currentTimeMillis() - timeOfLastMouseMotion) > mouseMovementTimeout && !mouseCurrentlyStopped) {
                 try {
 
                     client.send(new InputMessages.StopMouseMovement(playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 currentMouseEvents = 0;
                 timeOfLastMouseMotion = System.currentTimeMillis();
                 mouseCurrentlyStopped = true;
             }
         }
     }
 
     public void characterUpdate(float tpf) {
         //       System.out.println("character update");
         for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
             long nextPlayerID = playerIterator.next();
             CharMovement.setUpperArmTransform(upperArmAnglesMap.get(nextPlayerID), modelMap.get(nextPlayerID));
             CharMovement.setLowerArmTransform(elbowWristAngleMap.get(nextPlayerID), modelMap.get(nextPlayerID));
 
             //     modelMap.get(nextPlayerID).setLocalTranslation(new Vector3f(100,100,100));
             modelMap.get(nextPlayerID).setLocalTranslation(charPositionMap.get(nextPlayerID));
             modelMap.get(nextPlayerID).setLocalRotation((new Quaternion()).fromAngleAxis(charAngleMap.get(nextPlayerID), new Vector3f(0, 1, 0)));
 
             //        System.out.println("Char position is "+charPositionMap.get(nextPlayerID)+", local tranlsation "+modelMap.get(nextPlayerID).getLocalTranslation());
         }
     }
 
     public void registerInput() {
         inputManager.addRawInputListener(this);
         inputManager.setCursorVisible(false);
     }
 
     public void initTerrain() {
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
 
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("Textures/alpha.png"));
 
         /** 1.2) Add GRASS texture into the red layer (m_Tex1). */
         Texture grass = assetManager.loadTexture("Textures/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex1", grass);
         mat_terrain.setFloat("m_Tex1Scale", 64f);
 
         /** 1.3) Add DIRT texture into the green layer (m_Tex2) */
         Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
         dirt.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex2", dirt);
         mat_terrain.setFloat("m_Tex2Scale", 32f);
 
         /** 1.4) Add ROAD texture into the blue layer (m_Tex3) */
         Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
         rock.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex3", rock);
         mat_terrain.setFloat("m_Tex3Scale", 128f);
 
         /** 2. Create the height map */
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture("Textures/grayscale.png");
         heightmap = new ImageBasedHeightMap(
                 ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
         heightmap.load();
 
         /** 3. We have prepared material and heightmap. Now we create the actual terrain:
          * 3.1) We create a TerrainQuad and name it "my terrain".
          * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
          * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
          * 3.4) As LOD step scale we supply Vector3f(1,1,1).
          * 3.5) At last, we supply the prepared heightmap itself.
          */
         terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
 
         /** 4. We give the terrain its material, position & scale it, and attach it. */
         terrain.setMaterial(mat_terrain);
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 1f, 2f);
         rootNode.attachChild(terrain);
 
         /** 5. The LOD (level of detail) depends on were the camera is: */
         List<Camera> cameras = new ArrayList<Camera>();
         cameras.add(getCamera());
         TerrainLodControl control = new TerrainLodControl(terrain, cameras);
         terrain_phy = new RigidBodyControl(0.0f);
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
         if (message instanceof CharCreationMessage) {
             System.out.println("Creating character");
             CharCreationMessage creationMessage = (CharCreationMessage) message;
             long newPlayerID = creationMessage.playerID;
             Node newModel = Character.createCharacter("Models/Fighter.mesh.xml", assetManager, bulletAppState, false);
             rootNode.attachChild(newModel);
             if (creationMessage.controllable) {
                 playerID = newPlayerID;
                 model = newModel;
                 System.out.println("claiming player id " + playerID);
 
                 chaseCam = new ChaseCamera(cam, model, inputManager);
                 chaseCam.setSmoothMotion(true);
                 chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
                 chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
                 registerInput();
                 clientSet = true;
             }
             modelMap.put(newPlayerID, newModel);
             playerSet.add(newPlayerID);
             upperArmAnglesMap.put(newPlayerID, new Vector3f());
             upperArmVelsMap.put(newPlayerID, new Vector3f());
             elbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
             elbowWristVelMap.put(newPlayerID, new Float(0f));
             charPositionMap.put(newPlayerID, new Vector3f());
             charVelocityMap.put(newPlayerID, new Vector3f());
             charAngleMap.put(newPlayerID, 0f);
             charTurnVelMap.put(newPlayerID, 0f);
      //      modelMap.get(newPlayerID).getControl(AnimControl.class).addListener(this);
             animChannelMap.put(newPlayerID, modelMap.get(newPlayerID).getControl(AnimControl.class).createChannel());
             animChannelMap.get(newPlayerID).setAnim("stand");
         } else if (message instanceof CharPositionMessage) {
             //   System.out.println("modifying position");
             if (clientSet) {
 
                 CharPositionMessage charPosition = (CharPositionMessage) message;
                 long messagePlayerID = charPosition.playerID;
 
                 upperArmAnglesMap.put(messagePlayerID, charPosition.upperArmAngles.clone());
                 upperArmVelsMap.put(messagePlayerID, charPosition.upperArmVels.clone());
                 elbowWristAngleMap.put(messagePlayerID, charPosition.elbowWristAngle);
                 elbowWristVelMap.put(messagePlayerID, charPosition.elbowWristVel);
                 //          System.out.println("new position received is "+charPosition.charPosition);
                 charPositionMap.put(messagePlayerID, charPosition.charPosition);
                 charVelocityMap.put(messagePlayerID, charPosition.charVelocity);
                 charAngleMap.put(messagePlayerID, charPosition.charAngle);
                 charTurnVelMap.put(messagePlayerID, charPosition.charTurnVel);
                 if (animChannelMap.get(messagePlayerID) != null) {
                     if (charVelocityMap.get(messagePlayerID).equals(new Vector3f(0, 0, 0))) {
                         if (animChannelMap.get(messagePlayerID).getAnimationName().equals("walk")) {
                             animChannelMap.get(messagePlayerID).setAnim("stand");
                         }
                     } else {
                         if (animChannelMap.get(messagePlayerID).getAnimationName().equals("stand")) {
                             animChannelMap.get(messagePlayerID).setAnim("walk");
                         }
                     }
                 }
             }
         }
     }
 
     public void messageSent(Message message) {
         System.out.println(message.getClass());
     }
 
     public void objectReceived(Object object) {
     }
 
     public void objectSent(Object object) {
     }
 
     public void beginInput() {
     }
 
     public void endInput() {
     }
 
     public void onJoyAxisEvent(JoyAxisEvent evt) {
     }
 
     public void onJoyButtonEvent(JoyButtonEvent evt) {
     }
     private final int eventsPerPacket = 10; // how many events should happen before next packet is sent
     private final long mouseMovementTimeout = 100; // how long until we propose to send a StopMouseMovement message
     private long timeOfLastMouseMotion = 0; // how long since last movement
     private int currentMouseEvents = 0;
     private int currentDX = 0;
     private int currentDY = 0;
     private int prevDeltaWheel = 0;
 
     public void onMouseMotionEvent(MouseMotionEvent evt) {
 
         float dy = evt.getDY(), dx = evt.getDX();
         if (dy != 0 || dx != 0) {
             currentMouseEvents++;
             currentDX += dx;
             currentDY += dy;
 
             if (currentMouseEvents >= eventsPerPacket) {
                 try {
                     float angle = FastMath.atan2(currentDY, currentDX);
                     if (angle < 0) {
                         angle = FastMath.TWO_PI + angle;
                     }
                     client.send(new InputMessages.MouseMovement(angle, playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 currentMouseEvents = 0;
                 currentDX = 0;
                 currentDY = 0;
             }
 
             timeOfLastMouseMotion = System.currentTimeMillis();
             mouseCurrentlyStopped = false;
         }
 
         try {
             if (evt.getDeltaWheel() > 0) {
                 if (prevDeltaWheel < 0 && !(elbowWristAngle == CharMovement.Constraints.lRotMax)) {
                     client.send(new InputMessages.StopLArm(playerID));
                 } else {
                     client.send(new InputMessages.LArmDown(playerID));
                 }
                 prevDeltaWheel = 1;
             } else if (evt.getDeltaWheel() < 0) {
                 if (prevDeltaWheel > 0 && !(elbowWristAngle == CharMovement.Constraints.lRotMin)) {
                     client.send(new InputMessages.StopLArm(playerID));
                 } else {
                     client.send(new InputMessages.LArmUp(playerID));
                 }
                 prevDeltaWheel = -1;
             }
         } catch (IOException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     private boolean prevPressed = false;
 
     public void onMouseButtonEvent(MouseButtonEvent evt) {
         if (evt.isPressed() != prevPressed) {
             if (evt.isPressed()) {
                 if (evt.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                     try {
                         client.send(new InputMessages.RotateUArmCC(playerID));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else if (evt.getButtonIndex() == MouseInput.BUTTON_RIGHT) {
                     try {
                         client.send(new InputMessages.RotateUArmC(playerID));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             } else {
                 try {
                     client.send(new InputMessages.StopRotateTwist(playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         prevPressed = evt.isPressed();
         inputManager.setCursorVisible(false);
 
         if (evt.getButtonIndex() == MouseInput.BUTTON_MIDDLE) {
             try {
                 client.send(new InputMessages.StopLArm(playerID));
             } catch (IOException ex) {
                 Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void onKeyEvent(KeyInputEvent evt) {
         try {
             int key = evt.getKeyCode();
 
             switch (key) {
                 case KeyInput.KEY_E:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.MoveCharForward(playerID));
                     } else {
                         client.send(new InputMessages.StopForwardMove(playerID));
                     }
                     break;
                 case KeyInput.KEY_S:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.MoveCharLeft(playerID));
                     } else {
                         client.send(new InputMessages.StopLeftRightMove(playerID));
                     }
                     break;
                 case KeyInput.KEY_D:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.MoveCharBackword(playerID));
                     } else {
                         client.send(new InputMessages.StopForwardMove(playerID));
                     }
                     break;
                 case KeyInput.KEY_F:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.MoveCharRight(playerID));
                     } else {
                         client.send(new InputMessages.StopLeftRightMove(playerID));
                     }
                     break;
                 case KeyInput.KEY_W:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.TurnCharLeft(playerID));
                     } else {
                         client.send(new InputMessages.StopCharTurn(playerID));
                     }
                     break;
                 case KeyInput.KEY_R:
                     if (evt.isPressed()) {
                         client.send(new InputMessages.TurnCharRight(playerID));
                     } else {
                         client.send(new InputMessages.StopCharTurn(playerID));
                     }
                     break;
             }
         } catch (IOException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void destroy() {
         super.destroy();
         try {
             client.disconnect();
         } catch (Throwable ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void clientConnected(Client client) {
     }
 
     public void clientDisconnected(Client client) {
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
 
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         
     }
 }

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
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.messages.InputMessages;
 import mygame.messages.CharPositionMessage;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bounding.BoundingVolume;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.collision.CollisionResults;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Client;
 import com.jme3.network.connection.Server;
 import com.jme3.network.events.ConnectionListener;
 import com.jme3.network.events.MessageListener;
 import com.jme3.network.message.Message;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.network.sync.ServerSyncService;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Node;
 import com.jme3.system.JmeContext;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.util.SkyFactory;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import jme3tools.converters.ImageToAwt;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 import mygame.messages.HasID;
 
 public class BladeServer extends SimpleApplication implements MessageListener,ConnectionListener{
     HashMap<Long,Node> modelMap=new HashMap();
     HashMap<Long,Vector3f> upperArmAnglesMap=new HashMap();
     HashMap<Long,Vector3f> upperArmVelsMap=new HashMap();
     HashMap<Long,Float> elbowWristAngleMap=new HashMap();
     HashMap<Long,Float> elbowWristVelMap=new HashMap();
     HashSet<Long> playerSet=new HashSet();
     HashMap<Long,Client> clientMap=new HashMap();
     HashMap<Long,Vector3f> charPositionMap=new HashMap();
     HashMap<Long,Vector3f> charVelocityMap=new HashMap();
     HashMap<Long,Float> charAngleMap=new HashMap();
     HashMap<Long,Float> charTurnVelMap=new HashMap();
 
     private long currentPlayerID=0;
 
     private BulletAppState bulletAppState;
     private TerrainQuad terrain;
     Material mat_terrain;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     private RigidBodyControl terrain_phy;
     float airTime = 0;
 
     Server server;
     ServerSyncService serverSyncService;
 
     public static void main(String[] args) {
         BladeServer app = new BladeServer();
         //app.start();
         app.start(JmeContext.Type.Headless);
     }
 
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(CharPositionMessage.class);
         Serializer.registerClass(CharCreationMessage.class);
         Serializer.registerClass(CharDestructionMessage.class);
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
         server.addMessageListener(this,CharCreationMessage.class,CharDestructionMessage.class,CharPositionMessage.class);
 
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
 
         /*
         PhysicsCollisionGroupListener coll = new PhysicsCollisionGroupListener() {
 
             public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) {
                 System.out.println("COLLISION?");
                 return false;
             }
             */
             /*
             public void collision(PhysicsCollisionEvent event) {
             String name1 = event.getNodeA().getName();
             String name2 = event.getNodeB().getName();
 
             //System.out.println(name1 + " " + name2);
 
             if (event.getNodeA().getControl(RigidBodyControl.class) != null &&
             event.getNodeB().getControl(RigidBodyControl.class) != null &&
             event.getNodeA().getControl(RigidBodyControl.class).getCollisionGroup() == PhysicsCollisionObject.COLLISION_GROUP_02 &&
             event.getNodeB().getControl(RigidBodyControl.class).getCollisionGroup() == PhysicsCollisionObject.COLLISION_GROUP_02) {
             System.out.println("Rigid Collision!");
             }
              *
              */
             /*
             if (Long.parseLong(name1) == )
 
             GhostControl gControl = modelMap.get(playerID).getControl(GhostControl.class);
             if (gControl.getOverlappingCount() > 2) {
             System.out.println("GHOST COLLISION: " + gControl.getOverlappingCount());
             }
              *
              */
         //};
 
         //bulletAppState.getPhysicsSpace().addCollisionGroupListener(coll, PhysicsCollisionObject.COLLISION_GROUP_02);
     }
 
     @Override
     public void simpleUpdate(float tpf){
         
         updateCharacters(tpf);
     }
     
     private void handleCollisions(Long playerID) {
 
         CollisionResults results = new CollisionResults();
         Node player = modelMap.get(playerID);
         //Node otherPlayer = null;
         for (Map.Entry<Long, Node> playerEntry : modelMap.entrySet()) {
             if (playerEntry.getKey() != playerID) {
                 long pID = playerEntry.getKey();
 
                 BoundingVolume bv = modelMap.get(pID).getWorldBound();
                 //otherPlayer = playerEntry.getValue();
                 player.collideWith(bv, results);
 
                 if (results.size() > 0) {
                     System.out.println("Server: COLLISION DETECTED");
                 }
             }
         }
     }
 
     private long timeOfLastSync=0;
     private final long timeBetweenSyncs=100;
     public void updateCharacters(float tpf) {
 
         for(Iterator<Long> playerIterator=playerSet.iterator(); playerIterator.hasNext();){
             long playerID = playerIterator.next();
             Vector3f upperArmAngles = upperArmAnglesMap.get(playerID);
             upperArmAnglesMap.put(playerID, CharMovement.extrapolateUpperArmAngles(upperArmAngles,
                     upperArmVelsMap.get(playerID), tpf));
             elbowWristAngleMap.put(playerID, CharMovement.extrapolateLowerArmAngles(elbowWristAngleMap.get(playerID),
             elbowWristVelMap.get(playerID), tpf));
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
             charPositionMap.get(playerID).set(modelMap.get(playerID).getLocalTranslation());
 
             CharMovement.setUpperArmTransform(upperArmAnglesMap.get(playerID), modelMap.get(playerID));
             CharMovement.setLowerArmTransform(elbowWristAngleMap.get(playerID), modelMap.get(playerID));
             handleCollisions(playerID);
         }
         
         long currentTime = System.currentTimeMillis();
         if (currentTime - timeOfLastSync > timeBetweenSyncs) {
             timeOfLastSync = currentTime;
             for (Iterator<Long> sourcePlayerIterator = playerSet.iterator(); sourcePlayerIterator.hasNext();) {
                 long sourcePlayerID = sourcePlayerIterator.next();
                 for (Iterator<Long> destPlayerIterator = playerSet.iterator(); destPlayerIterator.hasNext();) {
                     long destPlayerID = destPlayerIterator.next();
                     try {
                         clientMap.get(destPlayerID).send(new CharPositionMessage(upperArmAnglesMap.get(sourcePlayerID), 
                                 upperArmVelsMap.get(sourcePlayerID),charPositionMap.get(sourcePlayerID),
                                 charVelocityMap.get(sourcePlayerID),elbowWristAngleMap.get(sourcePlayerID),
                                 elbowWristVelMap.get(sourcePlayerID),charAngleMap.get(sourcePlayerID),
                                 charTurnVelMap.get(sourcePlayerID),sourcePlayerID));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }
 
     public void initTerrain() {
         
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
 
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("Textures/alpha1.1.png"));
 
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
         Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
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
 
         /** Add in houses **/
         Node block = House.createHouse("Models/Main.mesh.j3o", assetManager, bulletAppState, true);
         rootNode.attachChild(block);
         
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
                 charVelocityMap.get(playerID).z=-CharMovement.charBackwordSpeed;
             } else if (message instanceof InputMessages.MoveCharForward) {
                 System.out.println("Move backword");
                 charVelocityMap.get(playerID).z=CharMovement.charForwardSpeed;
             } else if (message instanceof InputMessages.MoveCharLeft) {
                 System.out.println("Move left");
                 charVelocityMap.get(playerID).x=CharMovement.charStrafeSpeed;
             } else if (message instanceof InputMessages.MoveCharRight) {
                 System.out.println("Move right");
                 charVelocityMap.get(playerID).x=-CharMovement.charStrafeSpeed;
             } else if (message instanceof InputMessages.TurnCharLeft) {
                 charTurnVelMap.put(playerID, 1f);
             } else if (message instanceof InputMessages.TurnCharRight) {
                 charTurnVelMap.put(playerID, -1f);
             } else if (message instanceof InputMessages.StopCharTurn) {
                 charTurnVelMap.put(playerID,0f);
             } else if (message instanceof InputMessages.StopForwardMove) {
                 charVelocityMap.get(playerID).z=0;
             } else if (message instanceof InputMessages.StopLeftRightMove) {
                 System.out.println("Stop Left Right Move");
                 charVelocityMap.get(playerID).x=0;
             }
         }
         else{
             System.out.println("PlayerID "+playerID+" is not in the set");
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
         try {
             long playerID=currentPlayerID++;
<<<<<<< HEAD:src/mygame/BladeServer.java
            Node model = Character.createCharacter("Models/Fighter.mesh.j3o", assetManager, bulletAppState,true);
=======
            Node model = Character.createCharacter("Models/FighterRight.mesh.j3o", assetManager, bulletAppState,true, playerID);
            /*
            WireBox wBox = new WireBox();
            wBox.fromBoundingBox(new BoundingBox(Vector3f.ZERO, 0.5f, 0.5f, 0.5f));
            Geometry geom1 = new Geometry("WireBox", wBox);
            Material m1 = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
            m1.setColor("Color", ColorRGBA.Blue);
            geom1.setMaterial(m1);
            model.attachChild(geom1);
             *
             */
           
>>>>>>> 7a17f8dcf9b43e6eedfee88271f7e73e07089c52:src/mygame/BladeServer.java
             rootNode.attachChild(model);
             //rootNode.attachChild(geom1);
             modelMap.put(playerID, model);
             upperArmAnglesMap.put(playerID, new Vector3f());
             upperArmVelsMap.put(playerID, new Vector3f());
             elbowWristAngleMap.put(playerID, new Float(CharMovement.Constraints.lRotMin));
             elbowWristVelMap.put(playerID, new Float(0f));
             charPositionMap.put(playerID, new Vector3f());
             charVelocityMap.put(playerID, new Vector3f());
             charAngleMap.put(playerID, 0f);
             charTurnVelMap.put(playerID, 0f);
             client.send(new CharCreationMessage(playerID,true));
             for(Iterator<Long> playerIterator=playerSet.iterator();playerIterator.hasNext();){
                 long destPlayerID=playerIterator.next();
                 clientMap.get(destPlayerID).send(new CharCreationMessage(playerID,false)); // send this new character to all other clients
                 client.send(new CharCreationMessage(destPlayerID,false)); // send all other client's characters to this client
             }
             System.out.println("client connected:" + playerID+","+client);
             playerSet.add(playerID);
             clientMap.put(playerID, client);
             
         } catch (IOException ex) {
             Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void clientDisconnected(Client client) {
         
     }
 }

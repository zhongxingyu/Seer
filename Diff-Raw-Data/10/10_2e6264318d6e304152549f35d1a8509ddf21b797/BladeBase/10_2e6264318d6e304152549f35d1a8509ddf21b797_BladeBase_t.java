 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.texture.Texture2D;
 import com.jme3.util.SkyFactory;
 import com.jme3.water.WaterFilter;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Future;
 import jme3tools.converters.ImageToAwt;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 import mygame.messages.CharStatusMessage;
 import mygame.messages.ClientReadyMessage;
 import mygame.messages.InputMessages;
 import mygame.messages.SwordBodyCollisionMessage;
 import mygame.messages.SwordSwordCollisionMessage;
 
 /**
  *
  * @author blah
  */
 public class BladeBase extends SimpleApplication{
     HashSet<Long> playerSet = new HashSet();
     ConcurrentHashMap<Long, Character> charMap = new ConcurrentHashMap();
     
     Material mat_terrain;
     Material lighting;
     Material house_mat;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     Material bloodMat;
     Material clankMat;
     
     private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
     
     private Node house;
     private Node tree;
     private Node sceneNodes;
     
     private TerrainQuad terrain;
     
     private RigidBodyControl terrain_phy;
     
     private FilterPostProcessor fpp;
     WaterFilter water;
     
     private float initialWaterHeight = -9.2f;
     
     BulletAppState bulletAppState;
     
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(CharStatusMessage.class);
         Serializer.registerClass(CharCreationMessage.class);
         Serializer.registerClass(CharDestructionMessage.class);
         Serializer.registerClass(ClientReadyMessage.class);
         Serializer.registerClass(SwordSwordCollisionMessage.class);
         Serializer.registerClass(SwordBodyCollisionMessage.class);
         InputMessages.registerInputClasses();
         
         sceneNodes = new Node("Scene");
         rootNode.attachChild(sceneNodes);
         
         bulletAppState=new BulletAppState();
         stateManager.attach(bulletAppState);
         initMaterials();
         initTerrain();
         initWater();
         
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
         
         rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Skysphere.jpg", true));
     }
     
     public void initTerrain() {
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
         //house_mat = new Material(assetManager,"Common/MatDefs/Water/SimpleWater.j3md");
         mat_terrain.setBoolean("useTriPlanarMapping", false);
         mat_terrain.setBoolean("WardIso", true);
         mat_terrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/alpha1.1.png"));
         
         Texture grass = assetManager.loadTexture("Textures/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap", grass);
         mat_terrain.setFloat("m_DiffuseMap_0_scale", 64f);
 
         Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
         dirt.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap_1", dirt);
         mat_terrain.setFloat("m_DiffuseMap_1_scale", 16f);
 
         Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
         rock.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap_2", rock);
         mat_terrain.setFloat("m_DiffuseMap_2_scale", 128f);
 
         Texture normalMap0 = assetManager.loadTexture("Textures/grass_normal.png");
         normalMap0.setWrap(WrapMode.Repeat);
         Texture normalMap1 = assetManager.loadTexture("Textures/dirt_normal.png");
         normalMap1.setWrap(WrapMode.Repeat);
         Texture normalMap2 = assetManager.loadTexture("Textures/road_normal.png");
         normalMap2.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("NormalMap", normalMap0);
         mat_terrain.setTexture("NormalMap_1", normalMap2);
         mat_terrain.setTexture("NormalMap_2", normalMap2);
         lighting = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         lighting.setTexture("DiffuseMap", grass);
         lighting.setTexture("NormalMap", normalMap1);
         lighting.setBoolean("WardIso", true);
 
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
         heightmap = new ImageBasedHeightMap(
                 ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
         heightmap.load();
         terrain = new TerrainQuad("my terrain", 65, 1025, heightmap.getHeightMap());
         terrain.setMaterial(mat_terrain);
 
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 2f, 2f);
         terrain.setShadowMode(ShadowMode.CastAndReceive);
         //rootNode.attachChild(terrain);
         sceneNodes.attachChild(terrain);
         terrain_phy = new RigidBodyControl(0.0f);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
         terrain.addControl(terrain_phy);
         bulletAppState.getPhysicsSpace().add(terrain_phy);
         
         /*for(int i = 0; i < 9; i++){
             Tree[i] = (Node) assetManager.loadModel("Models/Tree.mesh.j3o");
         }*/
         int xDiff = 0;
         int yDiff = 0;
         for(int i = 0 ; i < 9; i++){
             for(int j = 0; j < 9; j++){
             tree = (Node) assetManager.loadModel("Models/Tree.mesh.j3o");
             tree.setLocalTranslation(-80.0f + xDiff, 10.0f, 35.0f+yDiff);
             sceneNodes.attachChild(tree);
             RigidBodyControl tree_phy = new RigidBodyControl(0.0f);
             tree_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
             tree_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
             tree.addControl(tree_phy);
             bulletAppState.getPhysicsSpace().add(tree_phy);
             xDiff = xDiff - 25;
             
             }
             xDiff = 0;
             yDiff = yDiff - 25;
         }
        house = (Node)assetManager.loadModel("Models/Cube.mesh_1.j3o");
        house.setLocalTranslation(0.0f, 15.0f, 70.0f);
         house.setShadowMode(ShadowMode.CastAndReceive);
         house.setLocalScale(13f);
        //house.setMaterial(wall_mat); 
         //Does not work atm house_mat.setTexture("m_Tex1", rock);
         //House.setMaterial(house_mat);
        //house.setMaterial(wall_mat);
         //rootNode.attachChild(House);
         sceneNodes.attachChild(house);
         RigidBodyControl house_phy = new RigidBodyControl(0.0f);
         house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
         house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
         house.addControl(house_phy);
         bulletAppState.getPhysicsSpace().add(house_phy);
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
 
         bloodMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
         bloodMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
         
         clankMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
         clankMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
     }
     
     public void initWater() {
         fpp = new FilterPostProcessor(assetManager);
         water = new WaterFilter(rootNode, lightDir);
         water.setWaveScale(0.003f);
         water.setMaxAmplitude(2f);
         water.setFoamExistence(new Vector3f(1f, 4, 0.5f));
         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
         water.setRefractionStrength(0.2f);
         water.setWaterHeight(initialWaterHeight);
         fpp.addFilter(water);
         viewPort.addProcessor(fpp);
         inputManager.addListener(new ActionListener() {
 
             public void onAction(String name, boolean isPressed, float tpf) {
                 if(isPressed){
                     if(name.equals("foam1")){
                         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam.jpg"));                      
                     }
                     if(name.equals("foam2")){
                         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
                     }
                     if(name.equals("foam3")){
                         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam3.jpg"));
                     }
                 }
             }
         }, "foam1","foam2","foam3");
         inputManager.addMapping("foam1", new KeyTrigger(keyInput.KEY_1));
         inputManager.addMapping("foam2", new KeyTrigger(keyInput.KEY_2));
         inputManager.addMapping("foam3", new KeyTrigger(keyInput.KEY_3));
     }
     
     public void createEffect(final Vector3f coords,final Material material) {
         final BladeBase app=this;
         Future action = app.enqueue(new Callable() {
 
             public Object call() throws Exception {
                 
                 final ParticleEmitter explosion = new ParticleEmitter("SwordSword", ParticleMesh.Type.Triangle, 30);
                 explosion.setMaterial(material);
                 rootNode.attachChild(explosion);
                 explosion.setLocalTranslation(coords);
                 ColorRGBA color;
                 if(material==clankMat){
                     color=ColorRGBA.Gray;
                 }
                 else{
                     color=ColorRGBA.Red;
                 }
                 explosion.setStartColor(color);
                 explosion.setEndColor(color);
                 explosion.emitAllParticles();
                 TimerTask task = new TimerTask() { // create a task that will detach the explosion later
 
                     public void run() {
                         Future action = app.enqueue(new Callable() {
 
                             public Object call() throws Exception {
                                 rootNode.detachChild(explosion);
                                 return null;
                             }
                         });
 
                     }
                 };
                 Timer timer = new Timer();
                 timer.schedule(task, 1000L);
                 return null;
             }
         });
     }
 }

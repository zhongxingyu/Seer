 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package level;
 
 import Helper.Constants;
 import character.CharacterFactory;
 import character.Player;
 import com.jme3.app.Application;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.scene.Node;
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.light.DirectionalLight;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import java.util.ArrayList;
 import java.util.List;
 import character.Opponent;
 
 /**
  *
  * @author Lena
  */
 public class Heaven extends LevelState implements ActionListener, PhysicsCollisionListener, AnimEventListener {
 
     //parts in Node unterteilt, die attached & detached werden,
     private Item lastCloudZero;
     private LevelState partZero;
     private Item lastCloudOne;
     private LevelState partOne;
 	
     private final float LEVEL_BOTTOM = -10.f;    
    private List<Opponent> opponents = new ArrayList<Opponent>();
     private List<Cloud> movableClouds = new ArrayList();
     private List<Cloud> moveClouds = new ArrayList();
 
     public Heaven() {
         super();
     }
 
     public Node getRootNode() {
         return this.rootNode;
     }
 
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.rootNode = game.getRootNode();
         bulletAppState.getPhysicsSpace().addCollisionListener(this);
         bulletAppState.getPhysicsSpace().add(player.control);
         bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 
         initKeys();
         loadPartZero();
         loadPartOne();
         loadAmbient();
 
         // TODO create enemy
         Opponent enemy = CharacterFactory.createOpponent("Enemy",
                 assetManager, new Vector3f(5, 10, 0), game.getBulletAppState());
         opponents.add(enemy);
         game.getRootNode().attachChild(enemy.model);
     }
 
     public Opponent getOpponent(String name) {
         for (int i = 0; i < opponents.size(); i++) {
             if (name.equals(opponents.get(i).getName())) {
                 return opponents.get(i);
             }
         }
 
         return null;
     }
 
     private void loadPartZero() {
         // Level 0 //
         // Part 0
         partZero = new LevelState();
         partZero.initialize(stateManager, game);
         Cloud cloud = null;
         
        // part.setParent(this);
         //Start
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(0, 0, 0), bulletAppState, assetManager).model);
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(6, 0, 0), bulletAppState, assetManager).model);
 
         // fallende Wolken
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(9, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(9, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(12, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(12, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(15, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(15, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(18, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(18, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(21, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(21, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(24, 0, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(24, LEVEL_BOTTOM - 3, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
 
         //Part1
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(31, 3, 0), bulletAppState, assetManager).model);
         // erster Gegner
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_Long_1v1, new Vector3f(45, 1, 0), bulletAppState, assetManager).model);
         // bewegende Wolke nach oben/unten
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(56, 3, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, true, new Vector3f(56, 14, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         // item
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(50, 18, 0), bulletAppState, assetManager).model);
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(62, 13, 0), bulletAppState, assetManager).model);
         // zweiter Gegner
         partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_MEDIUM_1v1, new Vector3f(73, 6, 0), bulletAppState, assetManager).model);
   
       //Part 2
       // Treppe
       cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(80, 9, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(80, LEVEL_BOTTOM - 3, 0));
       movableClouds.add(cloud);
       partZero.rootNode.attachChild(cloud.model);
       cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(85, 12, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(85, LEVEL_BOTTOM - 3, 0));
       movableClouds.add(cloud);
       partZero.rootNode.attachChild(cloud.model);
       cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(90, 15, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(90, LEVEL_BOTTOM - 3, 0));
       movableClouds.add(cloud);
       partZero.rootNode.attachChild(cloud.model);
       cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(95, 18, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, false, new Vector3f(95, LEVEL_BOTTOM - 3, 0));
       movableClouds.add(cloud);
       partZero.rootNode.attachChild(cloud.model);
       partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(100, 21, 0), bulletAppState, assetManager).model);
 
       // geheime PLatform mit Gegner
       partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_Long_1v1, new Vector3f(112, 21, 0), bulletAppState, assetManager).model);
       partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_Long_1v1, new Vector3f(98, 2, 0), bulletAppState, assetManager).model);
       partZero.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_Long_1v1, new Vector3f(112, 2, 0), bulletAppState, assetManager).model);
       // bewegende Wolke nach oben/unten mit Item
       cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(124, 2, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, false, new Vector3f(124, 21, 0));
       movableClouds.add(cloud);
       partZero.rootNode.attachChild(cloud.model);
       lastCloudZero = cloud;
       rootNode.attachChild(lastCloudZero.model);
         
         this.game.attachState(partZero);
         rootNode.attachChild(partZero.rootNode);
     }
 
     private void loadPartOne() {
         //Part 4
         partOne = new LevelState();
         partOne.initialize(stateManager, game);
         Cloud cloud = null;
         
         //Part 3
        // Level 1 //
        partOne.rootNode.attachChild(lastCloudZero.model);
        partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(123, 27, 0), bulletAppState, assetManager).model);
        // dritter Gegner
        partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(116, 30, 0), bulletAppState, assetManager).model);
        // bewegende Wolken oben/unten
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(107, 32, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, true, new Vector3f(107, 38, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(98, 34, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, true, new Vector3f(98, 40, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(89, 30, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, true, new Vector3f(89, 36, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
 
        // bewegende Wolken links/rechts
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(82, 36, 0), bulletAppState, assetManager, Cloud.DIRECTION_LEFT, true, new Vector3f(72, 36, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(74, 42, 0), bulletAppState, assetManager, Cloud.DIRECTION_LEFT, true, new Vector3f(64, 42, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(66, 48, 0), bulletAppState, assetManager, Cloud.DIRECTION_LEFT, true, new Vector3f(56, 48, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(58, 54, 0), bulletAppState, assetManager, Cloud.DIRECTION_LEFT, true, new Vector3f(45, 54, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
         
       
        partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_MEDIUM_1v1, new Vector3f(40, 54, 0), bulletAppState, assetManager).model);
        //Todestern
        partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(35, 56, 0), bulletAppState, assetManager).model);
        
        // bewegende Wolke oben/unten
        cloud = ItemFactory.createMovableCloud(Constants.CLOUD_MEDIUM_1v1, new Vector3f(30, 54, 0), bulletAppState, assetManager, Cloud.DIRECTION_DOWN, true, new Vector3f(30, 20, 0));
        movableClouds.add(cloud);
        partZero.rootNode.attachChild(cloud.model);
         // vierter Gegner
        partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_MEDIUM_1v1, new Vector3f(15, 20, 0), bulletAppState, assetManager).model);
 
         // bewegende Wolke oben/unten mit Todesternhinderniss
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(5, 20, 0), bulletAppState, assetManager, Cloud.DIRECTION_UP, true, new Vector3f(5, 64, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         //Todesstern
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(5, 40, 0), bulletAppState, assetManager).model);
         partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(9, 34, 0), bulletAppState, assetManager).model);
         // item
         partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(14, 38, 0), bulletAppState, assetManager).model);
         partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(9, 42, 0), bulletAppState, assetManager).model);
         // bewegende Wolke links/rechts
         cloud = ItemFactory.createMovableCloud(Constants.CLOUD_MEDIUM_1v1, new Vector3f(10, 64, 0), bulletAppState, assetManager, Cloud.DIRECTION_RIGHT, false, new Vector3f(72, 64, 0));
         movableClouds.add(cloud);
         partZero.rootNode.attachChild(cloud.model);
         // viele Todessterne
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(18, 64, 0), bulletAppState, assetManager).model);
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(40, 64, 0), bulletAppState, assetManager).model);
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(40, 75, 0), bulletAppState, assetManager).model);
         
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(58, 64, 0), bulletAppState, assetManager).model);
         partOne.rootNode.attachChild(ItemFactory.createDeathStar(new Vector3f(68, 64, 0), bulletAppState, assetManager).model);
 
         // mit Todesstern und fünftem Gegner
         partOne.rootNode.attachChild(ItemFactory.createCloud(Constants.CLOUD_Long_1v1, new Vector3f(80, 64, 0), bulletAppState, assetManager).model);
         // sechster Gegner
         lastCloudOne = ItemFactory.createCloud(Constants.CLOUD_SMALL_1v2, new Vector3f(90, 68, 0), bulletAppState, assetManager);
         rootNode.attachChild(lastCloudOne.model);
 
         this.game.attachState(partOne);
         rootNode.attachChild(partOne.rootNode);
     }
 
     @Override
     public void onAction(String name, boolean isPressed, float tpf) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void collision(PhysicsCollisionEvent event) {
         if ( event.getNodeA().getName().equals("Player")
                 && event.getNodeB().getName().equals("DeathStar")) 
         {
             player.adjustHealth(-3);
         }
         else if ( event.getNodeB().getName().equals("Player") 
                     && event.getNodeA().getName().equals("DeathStar")) 
         {
             player.adjustHealth(-3);
         }
         else if(event.getNodeA().getName().contains("movable") && event.getNodeB().getName().contains("Player"))
         {
             String cloud = event.getNodeA().getName();
             String cloudIndex = cloud.split("e")[1];
             Cloud cloudItem = (Cloud) movableClouds.get(Integer.getInteger(cloudIndex));
             if(!moveClouds.contains(cloudItem))
             {
                 moveClouds.add(cloudItem);
             }
         }
         else if(event.getNodeA().getName().contains("Player") && event.getNodeB().getName().contains("movable"))
         {
            
            String cloud = event.getNodeB().getName();
            String cloudIndex = cloud.split("e")[1];
            Cloud cloudItem = (Cloud) movableClouds.get(Integer.parseInt(cloudIndex)); 
            if(!moveClouds.contains(cloudItem))
             {
                 moveClouds.add(cloudItem);
             }
         }
     }
 
     @Override
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private void initKeys() {
         game.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
         game.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
         game.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_A));
         game.getInputManager().addMapping("RangeAttack", new KeyTrigger(KeyInput.KEY_S));
         game.getInputManager().addMapping("MeleeAttack", new KeyTrigger(KeyInput.KEY_D));
         game.getInputManager().addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
 
         game.getInputManager().addListener(analogListener, new String[]{"Left", "Right", "Jump"});
         game.getInputManager().addListener(actionListener, new String[]{"RangeAttack", "MeleeAttack", "Escape"});
     }
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String name, float value, float tpf) {
             // move left
             if (name.equals("Left")) {
                 player.currentDirection = Player.Direction.LEFT;
                 player.isMoving = true;
             }
 
             // move right
             if (name.equals("Right")) {
                 player.currentDirection = Player.Direction.RIGHT;
                 player.isMoving = true;
             }
 
             // jump
             if (name.equals("Jump")) {
                 if (player.currentJumpDelay >= player.JUMP_DELAY && player.canJump) {
                     player.currentJumpDelay = 0;
                     player.jumping = true;
                 }
             }
         }
     };
     private ActionListener actionListener = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
             if (name.equals("RangeAttack") && keyPressed) {
                 game.attachState(player.handleRangedAttack());
             }
 
             if (name.equals("MeleeAttack") && keyPressed) {
             }
 
             if (name.equals("Escape") && keyPressed) {
             }
         }
     };
 
     @Override
     public void update(float tpf) {
         player.handleMovement(tpf, game);
 		
	// check for death
         if (player.getPlayerLocation().y < LEVEL_BOTTOM || player.isDead())
         {
             game.detachState(partZero);
             game.detachState(partOne);
             
             loadPartZero();
             loadPartOne();
             
             rootNode.detachChildNamed("Player");            
             game.player = player = CharacterFactory.createPlayer("Dragon", assetManager);
             getRootNode().attachChild(player.model);
         }
         
         //move clouds
         for(Cloud cloud : moveClouds)
         {
             cloud.move(tpf);
         }
     }
 
     private void loadAmbient() {
         Node skyBox = (Node) assetManager.loadModel("Models/Ambient/Heaven/Sky.j3o");
         Node ambient_clouds = (Node) assetManager.loadModel("Models/Ambient/Heaven/AmbientClouds.j3o");
         Material mat_tt = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat_tt.setTexture("ColorMap", assetManager.loadTexture("Textures/Ambient_Sky_Clouds_UVLayout.png"));
         mat_tt.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         ambient_clouds.setLocalTranslation(ambient_clouds.getLocalTranslation().x, ambient_clouds.getLocalTranslation().y, (float) (ambient_clouds.getLocalTranslation().z - 40.0f));
         mat_tt.setTransparent(true);
 
         ambient_clouds.setMaterial(mat_tt);
 
         ambient_clouds.setQueueBucket(Bucket.Transparent);
         DirectionalLight sun = new DirectionalLight();
         sun.setColor(ColorRGBA.White);
         sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
 
         rootNode.addLight(sun);
         rootNode.attachChild(skyBox);
         rootNode.attachChild(ambient_clouds);
 
     }
 }

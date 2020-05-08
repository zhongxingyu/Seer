 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Cylinder;
 import event.Event;
 import event.EventListener;
 import event.EventManager;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 
 
 /**
  *
  * @author PeterTheOne
  */
 public class View implements EventListener {
     
     private Game game;
     private Node rootNode;
     private AssetManager assetManager;
     private EventManager evtManager;
     
     public View(Game game) {
         this.game = game;
         this.rootNode = this.game.getRootNode();
         assetManager = this.game.getAssetManager();
         evtManager = this.game.getEventManager();
         
         evtManager.addListener(this, SpaceshipCreatedEvent.TYPE);
         evtManager.addListener(this, SpaceshipDestroyedEvent.TYPE);
         evtManager.addListener(this, SpaceshipMovedEvent.TYPE);
         evtManager.addListener(this, SpaceshipAttackEvent.TYPE);
         evtManager.addListener(this, LaserDestroyedEvent.TYPE);
         evtManager.addListener(this, PlanetCreatedEvent.TYPE);
     }
 
     public void handleEvent(Event event) {
         if (SpaceshipCreatedEvent.TYPE.equals(event.getType())) {
             handleSpaceshipCreatedEvent((SpaceshipCreatedEvent) event);
         } else if (SpaceshipDestroyedEvent.TYPE.equals(event.getType())) {
             handleSpaceshipDestroyedEvent((SpaceshipDestroyedEvent) event);
         } else if (SpaceshipMovedEvent.TYPE.equals(event.getType())) {
             handleSpaceshipMovedEvent((SpaceshipMovedEvent) event);
         } else if (SpaceshipAttackEvent.TYPE.equals(event.getType())) {
             handleSpaceshipAttackEvent((SpaceshipAttackEvent) event);
         } else if (LaserDestroyedEvent.TYPE.equals(event.getType())){
             handleLaserDestroyedEvent((LaserDestroyedEvent) event);
         }
         else if (PlanetCreatedEvent.TYPE.equals(event.getType())){
             handlePlanetCreatedEvent((PlanetCreatedEvent) event);
         }
     }
 
     private void handleSpaceshipCreatedEvent(SpaceshipCreatedEvent event) {
         String name = event.getSpaceshipName();
         if (rootNode.getChild(name) == null) {
             //TODO: fix spaceship is turned backwards.
             Spatial shipSpatial = assetManager.loadModel("Models/shipA_OBJ/shipA_OBJ.j3o");
             shipSpatial.setName("ship");
             shipSpatial.scale(0.1f);
             Node spaceshipNode = new Node();
             spaceshipNode.setName(event.getSpaceshipName());
             Node spaceshipRotateNode = new Node();
             spaceshipRotateNode.setName(event.getSpaceshipName() + "Rotate");
             spaceshipRotateNode.attachChild(shipSpatial);
             spaceshipNode.attachChild(spaceshipRotateNode);
             this.rootNode.attachChild(spaceshipNode);
             
             //fire
             ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
             Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
             mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
             fire.setMaterial(mat_red);
             fire.setImagesX(2); fire.setImagesY(2); // 2x2 texture animation
             fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
             fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
             fire.setInitialVelocity(new Vector3f(0, 0, 10));
             fire.setStartSize(1.5f);
             fire.setEndSize(0.1f);
             fire.setGravity(0);
             fire.setLowLife(0.5f);
             fire.setHighLife(3f);
             fire.setVelocityVariation(0.3f);
             fire.setName("fire");
             spaceshipRotateNode.attachChild(fire);
         } else {
             //TODO: ouput error: "spaceship has allready been created"
         }
     }
     private void handlePlanetCreatedEvent (PlanetCreatedEvent event){
           Sphere c = new Sphere(30,30,10);
         Geometry planet = new Geometry(event.getPlanetName(), c);
         Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         
         planet.setMaterial(mat);
         planet.setName(event.getPlanetName());
         planet.setLocalTranslation(event.getPos());
             this.rootNode.attachChild(planet);
             
     }
 
     private void handleSpaceshipDestroyedEvent(SpaceshipDestroyedEvent event) {
         if (this.rootNode.detachChildNamed(event.getSpaceshipName()) == -1) {
             //TODO: output error: "laser not found, cannot be removed"
         }
     }
 
     private void handleSpaceshipMovedEvent(SpaceshipMovedEvent event) {
         Node shipNode = (Node) this.rootNode.getChild(event.getSpaceshipName());
        if (shipNode != null) {
             shipNode.setLocalTranslation(event.getSpaceshipPos());
          
             ParticleEmitter fire = (ParticleEmitter) shipNode.getChild("fire");
             fire.setInitialVelocity(event.getVelocity().normalize().mult((float) 5));
            
            Node shipRotateNode = (Node) shipNode.getChild(event.getSpaceshipName() + "Rotate");        
            if (shipRotateNode != null) {
                shipRotateNode.lookAt(event.getSpaceshipPos().add(event.getVelocity()), Vector3f.UNIT_Y);
            } else {
                //TODO: output error: "spaceshipRotateNode not found, cannot be moved"
            }
         } else {
             //TODO: output error: "spaceship not found, cannot be moved"
         }
     }
 
     private void handleSpaceshipAttackEvent(SpaceshipAttackEvent event) {
         Spatial attacker = this.rootNode.getChild(event.getSpaceshipAttackerName());
         Spatial target = this.rootNode.getChild(event.getSpaceshipTargetName());
         if (attacker == null || target == null) {
             return;
             //TODO: output error: "spaceship not found, cannot be attacked"
         }
         Vector3f attackerPos = attacker.getWorldTranslation();
         Vector3f targetPos = target.getWorldTranslation();
         
         float length = targetPos.subtract(attackerPos).length();
         
         Cylinder c = new Cylinder(12, 12, 0.1f, length, true);
         Geometry laser = new Geometry(event.getLaserName(), c);
         Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.Red);
         laser.setMaterial(mat);
         
         laser.setLocalTranslation(new Vector3f(0, 0, length / 2f));
         Node laserNode = new Node("laser");
         laserNode.attachChild(laser);
        laserNode.lookAt(targetPos.subtract(attackerPos), Vector3f.UNIT_Y);
         Node shipNode = (Node) this.rootNode.getChild(event.getSpaceshipAttackerName());
         shipNode.attachChild(laserNode);
     }
 
     private void handleLaserDestroyedEvent(LaserDestroyedEvent event) {
         Node node = (Node) this.rootNode.getChild(event.getSpaceshipAttackerName());
         if (node == null || node.detachChildNamed("laser") == -1) {
             //TODO: output error: "laser not found, cannot be removed"
         }
     }
 }

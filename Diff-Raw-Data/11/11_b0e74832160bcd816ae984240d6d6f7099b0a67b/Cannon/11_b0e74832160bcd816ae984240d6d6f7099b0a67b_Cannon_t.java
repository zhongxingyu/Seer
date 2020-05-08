 package testgame.items.weapons;
 
 import jme3test.bullet.BombControl;
 import testgame.items.Weapon;
 import testgame.player.Player;
 
 import com.jme3.app.Application;
 import com.jme3.asset.AssetManager;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.material.Material;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.scene.shape.Sphere.TextureMode;
 import com.jme3.texture.Texture;
 
 /**
  * Cannon weapon. Can not be instantiated before game runs.
  * @author Jonatan Dahl
  *
  */
 public class Cannon extends Weapon {
 
 	private Material 				bulletMat;
 	private Sphere 					bullet;
 	private SphereCollisionShape 	bulletCollisionShape;
 	private AssetManager 			assetManager;
 	private Player 					player;
 	private BulletAppState 			bulletAppState;
 
 	public Cannon(String name, Application app) {
 		super(WeaponType.CANNON, name);
 
 		this.assetManager 	= app.getAssetManager();
 		this.bulletAppState = app.getStateManager().getState(BulletAppState.class);
 		this.player 		= app.getStateManager().getState(Player.class);
 		
 		bulletMat 					= new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 	  	bullet 						= new Sphere(32, 32, 0.1f, true, false);
 	  	bulletCollisionShape 		= new SphereCollisionShape(0.4f);
 	  	TextureKey bulletTexKey 	= new TextureKey("Textures/Terrain/Rock/Rock.PNG");
                //TextureKey bulletTexKey 	= new TextureKey("Textures/terrain/cliffs.jpg");
		
                bulletTexKey.setGenerateMips(true);
 		Texture bulletTex = assetManager.loadTexture(bulletTexKey);
 		bulletMat.setTexture("ColorMap", bulletTex);
 		bullet.setTextureMode(TextureMode.Projected);
 	}
 	
 	public void shoot() {
 		Geometry bulletGeom = new Geometry("bullet", bullet);
 		RigidBodyControl bulletNode = new BombControl(assetManager,
 				bulletCollisionShape, 1);
 		Vector3f spawnlocation = player.getLocation().add(
 				player.getLookDirection().mult(5));
 		bulletGeom.setMaterial(bulletMat);
 		bulletGeom.setShadowMode(ShadowMode.CastAndReceive);
 		bulletGeom.setLocalTranslation(spawnlocation);
 		bulletNode.setLinearVelocity(player.getLookDirection().mult(150));
 		bulletNode.removeCollideWithGroup(2);
 		bulletGeom.addControl(bulletNode);
 		player.rootNode.attachChild(bulletGeom);
 		bulletAppState.getPhysicsSpace().add(bulletNode);
 	}
 }

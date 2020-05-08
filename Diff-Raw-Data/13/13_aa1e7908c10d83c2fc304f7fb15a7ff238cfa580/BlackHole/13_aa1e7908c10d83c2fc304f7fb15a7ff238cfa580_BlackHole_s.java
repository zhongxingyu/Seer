 package bullets;
 
 import ships.Entity;
 import ships.Player;
 import base.Runner;
 import base.Updatable;
 
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Sphere;
 
 public class BlackHole extends Projectile {
 
 	float START_TIME = 5, END_TIME = 15;
 	float time = 0;
 	
 	float pullSpeed = 3000000;
 	
 	public BlackHole(Gun gun) {
 		super(gun);
 	}
 
 	@Override
 	public Spatial createNewSpatial() {
 		Geometry sphere = new Geometry("blackHoleBuller", new Sphere(32, 32, 10));
 		Material blackHole = new Material(Runner.getInstance().getAssetManager(), 
 				"Common/MatDefs/Misc/Unshaded.j3md");
 		blackHole.setColor("Color", gun.bulletColor);
 		sphere.setMaterial(blackHole);
 		return sphere;
 	}
 
 	@Override
 	public CollisionShape createNewCollisionShape() {
 		return CollisionShapeFactory.createBoxShape(new Geometry("bullet", new Sphere(32, 32, 10)));
 	}
 	
 	@Override
 	public boolean isValid(){
 		return time < END_TIME;
 	}
 	
 	@Override
 	public void updateObject(float tpf){
 		super.updateObject(tpf);
 		time+=tpf;
 		if(time > START_TIME)
 			velocity = new Vector3f(0,0,0);
 		for(Updatable u: Runner.getGameState().updatables){
 			if(u instanceof Entity && ! (u instanceof Player) ){
 				Entity s = (Entity)u;
 				Vector3f shipLoc = s.getNode().getWorldTranslation();
 				Vector3f dist = getPhysicsLocation().subtract(shipLoc);
 				float multiplier = pullSpeed / FastMath.pow(dist.length(),2) * tpf;
				((Entity)u).getNode().move(dist.normalize().mult(multiplier));
 			}
 		}
 	}
 
 }

 package bullets;
 
 import java.util.ArrayList;
 
 import ships.Ship;
 import base.Runner;
 
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Sphere;
 
 public class EMP extends Projectile {
 
 	boolean activated = false;
 	
 	float growthRate = 4f;
 	float radius = 2;
 	float maxRadius = 500;
 	
 	ArrayList<Ship> hit = new ArrayList<Ship>();
 	
 	public EMP(Gun gun) {
 		super(gun);
 	}
 
 	@Override
 	public boolean onCollision(Ship ship) {
		super.onCollision(ship);
		if(!hit.contains(ship)){
 			activated = true;
 			hit.add(ship);
 			ship.enabled = false;
 		}
 		return false;	//no explosion required
 	}
 
 	@Override
 	public Spatial createNewSpatial() {
         Geometry electricSphere = new Geometry("empElectric", new Sphere(32,32,2));
         Material electric = Runner.getInstance().getAssetManager()
 				.loadMaterial("ShaderBlow/Materials/Electricity/electricity1_2.j3m");
         electricSphere.setMaterial(electric);
         electricSphere.setQueueBucket(Bucket.Transparent);
         return electricSphere;
 	}
 
 	@Override
 	public CollisionShape createNewCollisionShape() {
 		return new SphereCollisionShape(2);
 	}
 	
 	@Override
 	public void updateObject(float tpf){
 		super.updateObject(tpf);
 		if(activated){
 			float multiplier = FastMath.pow(growthRate,tpf);
 			radius *= multiplier;
 			super.spatial.scale(multiplier);
 			super.getCollisionShape().setScale(super.getCollisionShape().getScale().mult(multiplier));
 		}
 	}
 	
 	@Override
 	public boolean isValid(){
 		return radius < maxRadius;
 	}
 
 }

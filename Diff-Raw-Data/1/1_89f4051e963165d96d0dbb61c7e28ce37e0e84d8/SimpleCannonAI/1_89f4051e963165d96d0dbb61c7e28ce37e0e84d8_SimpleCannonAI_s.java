 package structureAIs;
 
 import model.Location;
 import model.Model;
 import projectiles.Projectile;
import projectiles.SimpleProjectile;
 import structures.Structure;
 
 public class SimpleCannonAI implements StructureAI{
 
 	@Override
 	public void next(Structure s) {
 		s.fire();
 	}
 
 }

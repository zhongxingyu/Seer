 package atl.space.entities;
 
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class EntityBuilder {
 	
 	public static Entity point(float x, float y, float z) {
 		Entity temp = new Entity("defaultpoint");
 		temp.addComponent(new PointRenderComponent());
 		temp.position = new Vector3f(x, y, z);
 		return temp;
 	}
 	public static Entity mover(float x, float y, float z, float dx, float dy, float dz) {
 		Entity temp = new Entity("defaultmover");
		temp.addComponent(new PointRenderComponent()); //PointTrailRenderComponent doesn't work :O
 		temp.addComponent(new MovementComponent(new Vector3f(dx, dy, dz)));
 		temp.position = new Vector3f(x, y, z);
 		return temp;
 	}
 	public static Entity trailer(Vector3f pos, Vector3f speed, int traillength, float trailfade) {
 		Entity temp = new Entity("defaultmover");
 		temp.addComponent(new PointTrailRenderComponent(traillength, trailfade));
 		temp.addComponent(new MovementComponent(new Vector3f(speed)));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity facer(Vector3f pos, Vector3f dirMoving, Vector3f dirFacing){
 		Entity temp = new Entity("defaultfacer");
 		temp.addComponent(new FacingComponent(dirFacing));
 		temp.addComponent(new MovementComponent(dirMoving));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity emitter(Vector3f pos, Entity emission){
 		Entity temp = new Entity("defaultemitter");
 		temp.addComponent(new EmissionComponent(emission));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity accelerator(Vector3f pos, Vector3f dirMoving, Vector3f dirFacing, Vector3f acceleration){
 		Entity temp = new Entity("defaultaccelerator");
 		temp.addComponent(new FacingComponent(dirFacing));
 		temp.addComponent(new MovementComponent(dirMoving));
 		temp.addComponent(new AccelComponent(acceleration));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity turner(Vector3f pos, Vector3f dirMoving, Vector3f dirFacing, Vector3f turn, float turnrate){
 		Entity temp = new Entity("defaultturner");
 		temp.addComponent(new FacingComponent(dirFacing));
 		temp.addComponent(new MovementComponent(dirMoving));
 		temp.addComponent(new RTurningComponent(turn, turnrate));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity dumbAuto(Vector3f pos, Vector3f dirMoving, Vector3f dirFacing, Vector3f acceleration, Vector3f turn){
 		Entity temp = new Entity("dumbAuto");
 		temp.addComponent(new FacingComponent(dirFacing));
 		temp.addComponent(new MovementComponent(dirMoving));
 		temp.addComponent(new AccelComponent(acceleration));
 		temp.addComponent(new TurningComponent(turn));
 		temp.position = new Vector3f(pos);
 		return temp;
 	}
 	public static Entity smartAuto(Vector3f pos, Vector3f dirMoving, Vector3f dirFacing, Vector3f acceleration, float maxAccelF, float maxAccelB, float maxAccelS, Vector3f turn, float maxturn){
 		Entity temp = new Entity("smartAuto");
 		temp.addComponent(new FacingComponent(dirFacing));
 		temp.addComponent(new MovementComponent(dirMoving));
 		temp.addComponent(new RDAccelComponent(acceleration, maxAccelF, maxAccelB, maxAccelS));
 		temp.addComponent(new RTurningComponent(turn, maxturn));
 		temp.position = new Vector3f(pos);
 		return temp;	
 	}
 	
 	public static Entity getNearest(Entity origin, List<Entity> entities){
 		Entity nearest = entities.get(0);
 		float longestdistance = origin.getDistance(nearest.position);
 		//Vector3f temp = new Vector3f();
 		for(Entity e : entities){
 			float dist = origin.getDistance(e.position);
 			if(dist < longestdistance){
 				nearest = e;
 				longestdistance = dist;
 			}
 		}
 		return nearest;
 	}
 	
 	
 }

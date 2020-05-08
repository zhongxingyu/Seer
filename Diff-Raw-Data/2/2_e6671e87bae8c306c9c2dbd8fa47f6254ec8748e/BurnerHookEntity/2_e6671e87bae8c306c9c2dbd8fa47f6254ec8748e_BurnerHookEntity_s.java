 package pudgewars.entities.hooks;
 
 import java.util.Random;
 
 import pudgewars.Game;
 import pudgewars.entities.PudgeEntity;
 import pudgewars.particles.VelocityParticle;
 import pudgewars.util.Time;
 import pudgewars.util.Vector2;
 
 public class BurnerHookEntity extends NormalHookEntity {
 
 	public final static double BURN_INTERVAL_DIST = 2;
 
 	public double dist;
 
 	public BurnerHookEntity(PudgeEntity e, Vector2 target) {
 		super(e, target);
 
 		this.damage = 0;
 	}
 
 	public void update() {
 		super.update();
 
 		if (hooked != null) {
 			// System.out.println("Somebody was hooked.");
 			// double xDist = rigidbody.velocity.x * Time.getTickInterval();
 			// double yDist = rigidbody.velocity.y * Time.getTickInterval();
 			// double tDist = Math.sqrt(xDist * xDist + yDist * yDist);
 
 			dist += rigidbody.speed * Time.getTickInterval();
 			// System.out.println("Dist: " + dist);
 			if (dist >= BURN_INTERVAL_DIST) {
 				dist -= BURN_INTERVAL_DIST;
 				if (hooked instanceof PudgeEntity) {
 					if(((PudgeEntity) hooked).stats.subLife(1)){
 						owner.stats.addExp(2);
 						owner.stats.addKill();
						hooked = null;
 						return;
 					};
 						
 					// Add Blood Particles
 					Random r = new Random();
 					for (int i = 0; i < r.nextInt(10) + 10; i++) {
 						Vector2 posOffset = new Vector2(r.nextDouble() - 0.5, r.nextDouble() - 0.5);
 						posOffset.scale(0.5);
 						Vector2 velOffset = new Vector2(r.nextDouble() - 0.5, r.nextDouble() - 0.5);
 						velOffset.scale(4);
 						Game.entities.addParticle( //
 								new VelocityParticle("blood", 3, 3, 1, //
 										Vector2.add(posOffset, transform.position), //
 										velOffset, 0.25));
 					}
 				}
 			}
 		}
 	}
 
 	public String getNetworkString() {
 		String s = "BURNERHOOK:" + owner.ClientID + ":";
 		s += (target == null) ? "null" : target.getNetString();
 		s += ":" + transform.position.getNetString();
 		s += ":" + rigidbody.velocity.getNetString();
 		return s;
 	}
 
 	public void setNetworkString(String s) {
 		wasUpdated = true;
 		String[] t = s.split(":");
 		transform.position.setNetString(t[0]);
 		rigidbody.velocity.setNetString(t[1]);
 	}
 }

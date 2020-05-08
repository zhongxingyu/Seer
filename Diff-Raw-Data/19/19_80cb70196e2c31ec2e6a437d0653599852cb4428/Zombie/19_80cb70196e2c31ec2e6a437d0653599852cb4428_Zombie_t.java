 package projectubernahme.lifeforms;
 
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.gfx.LifeformZombieGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 public class Zombie extends Lifeform {
 	
 
 	private static ConvertedGraphics cg = new LifeformZombieGraphics();
 
 	public Zombie(MainSimulator sim) {
 		super(sim);
 		setCanFly(false);
 		setCanSee(true);
 		canMove = true;
 		setBiomass(60.0 + Math.random()*60);
 	}
 
 	public Zombie(MainSimulator sim, double d, double e) {
 		this(sim);
 		x = d;
 		y = e;
 	}
 
 	@Override
 	public void act(int sleepTime) {
 		if (!isControlled()) {
 			/* find the closest human to this zombie */
 			Human closest = null;
 			double minDistance = Double.MAX_VALUE;
 			
 			for(Lifeform n : getNeighbors()) {
 				if (n.getClass().getName().endsWith("Human")) {
 					double distance = distance(n);
 					if (distance < minDistance) {
 						minDistance = distance;
 						closest = (Human) n;
 					}
 				}
 			}
 			
 			/* if there is a victim ... */
 			if (closest != null) {
 				if (minDistance < 0.3) {
 					ingest(closest);
 				}
 				else {
					double xdist, ydist;
					xdist = closest.getX() - getX();
					ydist = closest.getY() - getY();
 					
					viewAngle = Math.atan2(ydist, xdist);
					localxvsign = 1;
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public boolean canSee(Lifeform l) {
 		return distance(l) < 5.0;
 	}
 
 	@Override
 	public ConvertedGraphics getConvertedGraphics() {
 		// TODO Auto-generated method stub
 		return cg;
 	}
 
 }

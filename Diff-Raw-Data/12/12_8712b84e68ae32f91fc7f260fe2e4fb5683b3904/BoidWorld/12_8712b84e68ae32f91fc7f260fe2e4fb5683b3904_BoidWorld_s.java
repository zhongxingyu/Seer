 package demos;
 
 import vitro.*;
 import vitro.plane.*;
 import java.util.*;
 
 
 public class BoidWorld extends Plane {
 
 	public Boid createBoid() { return new Boid(this); }
 
 	public BoidWorld(double width, double height) {
 		super(width, height);
 	}
 	
 	public boolean done() {
 		return false;
 	}
 	
 	public class Boid extends PlaneActor {
 
 		private double angle = 0.0;
 
 		public Boid(Plane model) {
 			super(model);
 		}
 		
 		public Set<Boid> flock() {
 			Set<Boid> ret = new HashSet<Boid>();
 			for(Actor actor : model.positions.keySet()) {
 				if(actor instanceof Boid && positions.get(actor) != null) {
 					ret.add((Boid)actor);
 				}
 			}
 			return ret;
 		}
 
 		// here we just set it to what it should be... no agent needed!
 		public Set<Action> actions() {
 			Set<Action> ret = new HashSet<Action>(); // Forget anything else... I'm a boid!
 
 			// only continue if I am in the world
 			Position myPos = positions.get(this);
 			if(myPos == null) {
 				return ret;
 			}
 
 			// calculate!
 			Vector2 centerMass = Vector2.ZERO;
 			Vector2 centerHead = Vector2.ZERO;
 			Vector2 repulsion  = Vector2.ZERO;
 
 			for(Boid boid : flock()) {
 				Position theirPos = positions.get(boid);
 
 				centerMass = centerMass.add(Position.ZERO.displace(theirPos).mul(1.0 / flock().size()));
 				centerHead = centerHead.add(new Vector2(Math.cos(boid.angle), Math.sin(boid.angle)).mul(1.0 / flock().size()));
 
				Vector2 repulse = myPos.displace(theirPos);
				if(repulse.normSq() > 0) {
 					repulsion = repulsion.add(repulse.normalize().mul(1.0 / repulse.normSq()));
 				}
 			}
			repulsion  = repulsion.mul(1.0 / flock().size());
 
 			Vector2 heading = Vector2.ZERO;
 			heading = heading.add(myPos.displace(new Position(centerMass)).normalize());
			heading = heading.add(centerHead.normalize());
			heading = heading.add(repulsion.mul(10.0));
 
 			angle = Math.atan2(heading.y, heading.x);
 
 			Position newPos = myPos.translate(heading.normalize().mul(0.1));
 			newPos = new Position(Math.min(Math.max(newPos.x, 0.0), width), Math.min(Math.max(newPos.y, 0.0), height));
 			ret.add(new MoveAction(model, newPos, this));
 
 			// here we compute the new location (position + heading)
 			/*
 			Vector2 centerMass = Vector2.ZERO;
 			Vector2 centerHead = Vector2.ZERO;
 			Vector2 repulsion  = Vector2.ZERO;
 
 			for(Boid boid : flock()) {
 				Frame frame = frames.get(boid);
 
 				Vector2 position = new Vector2(frame.x, frame.y);
 				double angle = frame.angle;
 
 				centerMass = centerMass.add(position);
 				centerHead = centerHead.add(new Vector2(Math.cos(angle), Math.sin(angle)));
 
 				Vector2 displace = (new Vector2(frames.get(this).x, frames.get(this).y)).sub(position);
 				if(displace.normSq() != 0.0) {
 					repulsion = repulsion.add(displace.normalize().mul(1.0 / displace.normSq()));
 				}
 			}
 			centerMass = centerMass.mul(1.0 / flock().size()).sub(new Vector2(frames.get(this).x, frames.get(this).y));
 			centerHead = centerHead.mul(1.0 / flock().size());
 			repulsion  = repulsion.mul(1.0 / flock().size());
 
 			Vector2 heading = Vector2.ZERO;
 			heading = heading.add(centerMass.normalize());
 			heading = heading.add(centerHead.normalize());
 			heading = heading.add(repulsion.mul(2.0));
 			heading = heading.normalize().mul(0.1);
 
 			Frame newFrame = reference();
 			newFrame = newFrame.translate(heading.x, heading.y);
 			newFrame = newFrame.rotate(Math.atan2(-reference().y + newFrame.y, -reference().x + newFrame.x) - newFrame.angle);
 			newFrame = new Frame(Math.min(Math.max(newFrame.x, 0.0), width), Math.min(Math.max(newFrame.y, 0.0), height), newFrame.angle);
 
 			ret.add(new MoveAction(model, newFrame, this));
 			*/
 			return ret;
 		}
 	}
 }

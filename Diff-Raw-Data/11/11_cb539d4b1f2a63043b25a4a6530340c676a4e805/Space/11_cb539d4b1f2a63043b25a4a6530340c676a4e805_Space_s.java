 package net.spacesim.client;
 
 public class Space {
 
 	private Body[] bodies;
 	public int bodyCount;
 
 	public Space() {
 		bodies = new Body[10000];
 	}
 
 	public void render() {
 		for(int i = 0; i < bodyCount; i++) {
 			Body body = bodies[i];
 			if(body == null) continue;
 			body.render();
 		}
 	}
 
 	public void update() {
 		for(int i = 0; i < bodyCount; i++) {
 			Body body = bodies[i];
 			if(body == null) continue;
 			body.tick();
 		}
 	}
 
 	public Body[] getBodies() {
 		return bodies;
 	}
 
 	public void add(Body body) {
 		for(int i = 0; i < bodies.length; i++) {
 			if(bodies[i] == null) {
 				bodies[i] = body;
 				bodyCount++;
 				break;
 			}
 		}
 	}
 
 	public void remove(Body body) {
 		bodies[body.position] = null;
 	}
 
 }

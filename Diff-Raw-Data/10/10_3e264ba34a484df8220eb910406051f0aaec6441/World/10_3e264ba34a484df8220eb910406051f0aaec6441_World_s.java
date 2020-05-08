 package edu.chl.codenameg.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class World {
 
 	List<Entity> entities;
 
 	public World() {
 		this.entities = new ArrayList<Entity>();
 	}
 
 	public void add(Entity e) {
 		entities.add(e);
 	}
 
 	public List<Entity> getEntities() {
 		// return new ArrayList<Entity>(this.entities);
 		return this.entities;
 	}
 
 	public Camera getCamera() {
 		return new Camera();
 	}
 
 	public void update(int elapsedTime) {
 		for (Entity e : this.getEntities()) {
 			e.update(elapsedTime);
 			this.checkCollision(e);
 		}
 
 	}
 
 	private void checkCollision(Entity e) {
 		for (Entity target : this.getEntities()) {
 			if (target != e) {
 				boolean collision = false;
				if (e.getPosition() == target.getPosition()) {
 					collision = true;
 				}
 				if (collision) {
 					e.collide(target);
 					target.collide(e);
 				}
 			}
 		}
 	}
 
 }

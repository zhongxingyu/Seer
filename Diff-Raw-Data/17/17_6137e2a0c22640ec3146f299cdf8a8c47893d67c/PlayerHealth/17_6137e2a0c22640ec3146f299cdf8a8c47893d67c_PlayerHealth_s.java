 package edu.csupomona.kyra.component.health;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.csupomona.kyra.entity.Entity;
 
 public class PlayerHealth extends HealthComponent {
 	ArrayList<Entity> enemies;
 	ArrayList<Entity> hearts;
 	Timer timer;
 	boolean vulnerable;
 	
 	public PlayerHealth(String id, int health, ArrayList<Entity> enemies, ArrayList<Entity> hearts) {
 		super(id, health);
 		this.enemies = enemies;
 		this.hearts = hearts;
 		timer = new Timer();
 	}
 	
 	protected void makeTempInvulnerable() {
 		makeInvulnerable();
 		timer = new Timer();
 		TimerTask end = new TimerTask() {
 			public void run() {
 				makeVulnerable();
 			}
 		};
 		timer.schedule(end, 1200);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
		System.out.println("Health check: vulnerable? " + isVulnerable());
 		if (isVulnerable()) {
 			Polygon polygon = owner.getPhysicsComponent().getPolygon();
 			for (Entity enemy : enemies) {
 				Polygon enemyPoly = enemy.getPhysicsComponent().getPolygon();
 				if ((enemyPoly != null) && (enemyPoly.intersects(polygon))) {
 					setBadHit();
 					addHealth(-1);
 					makeTempInvulnerable();
 					break;
 				}
 			}
 			for (Entity heart : hearts) {
 				Polygon heartPoly = heart.getPhysicsComponent().getPolygon();
 				if ((heartPoly != null) && (heartPoly.intersects(polygon))) {
 					setGoodHit();
 					addHealth(1);
 				}
 			}
 		}
 	}
 }

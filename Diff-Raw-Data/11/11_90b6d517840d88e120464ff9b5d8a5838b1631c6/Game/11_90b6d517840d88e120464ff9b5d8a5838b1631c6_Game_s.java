 package game;
 import java.util.ArrayList;
 
 import util.Vector2D;
 
 import engine.Field;
 import engine.Pathplanner;
 
 
 public class Game {
 	
 	public Field f;
 	Pathplanner p;
 	public Unit u;
 	public ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
 	public Vector2D c = new Vector2D(0,0);
 	public int tileSize;
 	
 	public Game(Field f, Vector2D startpos) {
 		this.f = f;
 		this.p = new Pathplanner(f);
 		u = new Unit(startpos, this);
 	}
 	
 	
 	public void leftclick(Vector2D clickPos) {
		System.out.println(f.tileIndexAt(clickPos));
		System.out.println(f.tileValueAt(clickPos));
 		u.moveTo(clickPos);
 	}
 	
 	public void rightclick(Vector2D clickPos) {
 		projectiles.add(new Projectile(u.pos, clickPos.subtract(u.pos), this));
 	}
 	/*
 	public void rightclick(Vector2D clickPos) {
 		u.pos = clickPos;
 	}
 	*/
 	public void step(double dt) {
 		u.process(dt);
 		for(Projectile proj : projectiles) {
 			proj.process(dt);
 		}
 	}
 	
 }

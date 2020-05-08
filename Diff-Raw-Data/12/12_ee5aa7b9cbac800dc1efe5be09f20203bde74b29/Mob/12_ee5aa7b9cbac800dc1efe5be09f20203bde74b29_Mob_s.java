 package nl.tdegroot.games.nemesis.entity;
 
 import nl.tdegroot.games.nemesis.Camera;
 import nl.tdegroot.games.nemesis.level.Level;
 
 public class Mob extends Entity {
 
 	protected boolean isMoving = false;
 
 	protected float movementSpeed = 0.0f;
 	protected Level level;
 
 	public Mob(float x, float y, Level level) {
 		super(x, y);
 		this.level = level;
 	}
 
 	public void update() {
 
 	}
 
 	public void move(float xa, float ya) {
 		x += xa;
 		y += ya;
 		if (x < 0)
 			x = 0;
 		if (y < 0)
 			y = 0;
		if (x > level.getPixelWidth())
			x = level.getPixelWidth();
		if (y > level.getPixelHeight())
			y = level.getPixelHeight();
 
 	}
 
 	public void render(Camera camera) {
 
 	}
 
 }

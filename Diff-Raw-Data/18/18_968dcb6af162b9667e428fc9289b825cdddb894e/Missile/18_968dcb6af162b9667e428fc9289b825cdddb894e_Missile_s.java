 package game;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Path2D;
 import java.awt.geom.Rectangle2D;
 
 public class Missile extends Projectile {
	public static final float MASS = 20;
 	
 	public Missile(Lander firedBy, Vector position, Vector velocity, MapManager mapManager) {
 		super(firedBy, position, velocity, mapManager);
 	}
 
 	@Override
 	public float getMass() {
 		return MASS;
 	}
 
 	@Override
 	public float getBoundingRadius() {
 		return 4;
 	}
 
 	@Override
 	public void render(Graphics2D g) {
 		AffineTransform savedXf = g.getTransform();
 
 		g.translate(position.x, position.y);
 		g.rotate(getAngle());
 
 		// Shape shape = new Rectangle2D.Float(-HEIGHT/2, -WIDTH/2, HEIGHT, WIDTH);
 		g.setColor(Color.YELLOW);
 		// g.fill(shape);
 
 		Path2D.Float path = new Path2D.Float();
 		path.moveTo(HEIGHT / 2 - 8, -WIDTH / 2 + 5);
 		path.lineTo(HEIGHT / 2, 0);
 		path.lineTo(HEIGHT / 2 - 8, WIDTH / 2 - 5);
 		path.lineTo(-HEIGHT / 2 + 6, WIDTH / 2 - 5);
 		path.lineTo(-HEIGHT / 2, WIDTH / 2);
 		path.lineTo(-HEIGHT / 2, -WIDTH / 2);
 		path.lineTo(-HEIGHT / 2 + 6, -WIDTH / 2 + 5);
 		path.closePath();
 		g.fill(path);
 
 		g.setTransform(savedXf);
 	}
 
 	@Override
 	public void collidedWith(PhysicsObject other) {
 		mapManager.makeExplosion(firedBy.getPlayer(), position, BLAST_RADIUS, YIELD);
 		mapManager.removeProjectile(this);
 	}
 
 	@Override
 	public Rectangle2D getBounds() {
 		Shape shape = new Rectangle2D.Float(-HEIGHT / 2, -WIDTH / 2, HEIGHT, WIDTH);
 		AffineTransform xf = new AffineTransform();
 		xf.translate(position.x, position.y);
 		xf.rotate(getAngle());
 		shape = xf.createTransformedShape(shape);
 		return shape.getBounds2D();
 	}
 
 	private float getAngle() {
 		return velocity.angle();
 	}
 
 	private static final float WIDTH = 14;
 	private static final float HEIGHT = 30;
 
 	private static final float BLAST_RADIUS = 50;
 	private static final float YIELD = 40;
 }

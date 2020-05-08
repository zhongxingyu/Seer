 package projectiles;
 
 import interactions.Interaction;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.Set;
 
 import creeps.Creep;
 import model.Entity;
 import model.EntityImpl;
 import model.Hitbox;
 import model.Location;
 import model.Model;
 
 public abstract class Projectile extends EntityImpl {
	private static final int MOVE_STEP = 2;
 	
 	protected int moveStep = MOVE_STEP;
 	
 	protected Location vector;
 	protected double angle;
 	protected double movelength = 10;
 	
 	public boolean friendly = true;
 	
 	protected Interaction attack;
 
 	public Projectile(Location start, Location direction) {
 		super(start);
 		vector = direction;
 		angle = Math.atan2(vector.y - location.y, vector.x - location.x);
 	}
 
 	@Override
 	public Hitbox getHitbox() {
 		return hitbox;
 	}
 
 	public void setHitbox(Hitbox h) {
 		this.hitbox = h;
 	}
 
 	public void draw(Graphics g) {
 		Graphics2D g2d = (Graphics2D) g;
 		BufferedImage sprite = getSprite ();
 		int imgsize = Math.max(sprite.getHeight() * 2, sprite.getWidth() * 2);
 		
 	    BufferedImage dimg = new BufferedImage(imgsize, imgsize, BufferedImage.TYPE_INT_ARGB);
 	    Graphics2D gimg = dimg.createGraphics();
 	    gimg.rotate(angle, imgsize/2, imgsize/2);
 	    gimg.drawImage(getSprite(), imgsize/4, imgsize/4, null);
 	    gimg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 	    g2d.drawImage(dimg, null, location.x - imgsize/2, location.y - imgsize/2);
 	}
 
 	@Override
 	public void update() {
 		ticks++;
 		
 		if (ticks == tickspeed) {
 			ticks = 0;
 			
 			Set<? extends Entity> es;
 			if (friendly) es = Model.model.intersectsCreeps(hitbox);
 			else es = Model.model.intersectsFriendly(hitbox);
 			
 			unitMove(moveStep);
 			
 			for (Entity e : es) {
 				e.interact(attack);
 				Model.model.killEntity(this);
 			}
 		}
 	}
 	
 	@Override
 	public void interact(Interaction i) {
 		// not needed for projectiles, as they only interact with others.
 	}
 	
 	/**
 	 * helper function that calculates and applies unit movement ratios so we
 	 * don't have to always copy out the fucking trig.
 	 */
 	protected final void unitMove(int magnitude) {
 		double xunit = Math.cos(angle);
 		double yunit = Math.sin(angle);
 		
		// FIXME bug in projectile motion
		
		location.x += xunit * magnitude;
		location.y += yunit * magnitude;
 		
 		hitbox.move((int)(xunit*magnitude), (int)(yunit*magnitude));
 	}
 
 	@Override
 	protected Hitbox makeHitbox() {
 		BufferedImage sprite = getSprite ();
 		Hitbox h = new Hitbox (location.x, location.y, sprite.getWidth(), sprite.getHeight());
 		h.rotate(angle);
 		return h;
 	}
 }

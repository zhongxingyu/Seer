 package items.projectiles;
 
 import entities.Entity;
 import game.config.Config;
 
 import map.Cell;
 import map.tileproperties.TileProperty;
 
 import org.lwjgl.util.Renderable;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import utils.MapLoader;
 import utils.Position;
 
 public class Projectile implements Renderable{
 	
 	private final Animation moving;
 	private Animation sprite;
 	private static final float NORMAL_SPEED = 0.45f/32f;
 	private final Position xy,dxdy;
 	private final float width,height;
 	private final int damage;
 	private final double angle;
 	private Rectangle hitbox;
 	
 
 	public Projectile(Position xy,float width,float height, int damage, double angle ){
 		Image[] movementForward = null;
 		try {
 //			movementForward = new Image[]{new Image("data/images/nyan_0.gif"),new Image("data/images/nyan_1.gif"),new Image("data/images/nyan_2.gif"),new Image("data/images/nyan_3.gif"),
 //					new Image("data/images/nyan_4.gif"), new Image("data/images/nyan_5.gif")};
 		    movementForward = new Image[]{new Image("data/images/projectile.png")};
 		} catch (SlickException e) {
 			//do shit all
 		}
 //		int[] duration = {200,200,200,200,200,200};
 		moving = new Animation(movementForward, 200, false);
 		sprite=moving;
 		
 		this.angle = angle;
 		this.damage = damage;
 		this.width = width; this.height = height; this.hitbox = new Rectangle(xy.getX(), xy.getY(), width, height);
 		this.xy = xy;
 		this.dxdy = new Position((float)Math.cos(angle),(float)Math.sin(angle)); //currently only works horizontally
 		this.dxdy.scale(NORMAL_SPEED);
 		
 	}
 	
 	public Projectile(float x, float y, float width, float height, int damage, double angle){
 		this(new Position(x,y),width,height,damage,angle);
 	}
 	
 	@Override
 	public void render() {
 				
 	}
 
 	public void update(long DELTA) {
 		xy.translate(dxdy); //ignores gravity
 		hitbox.setLocation(xy.getX(), xy.getY());
 		sprite.update(DELTA);
 		Cell currentCell = MapLoader.getCurrentCell();
 		float x = xy.getX(); float y = xy.getY();
 		if((int)x < 0 || (int)y <= 0 || (int)x >= currentCell.getWidth() - 1 || (int)y >= currentCell.getHeight() - 1 || 
		        "true".equals(currentCell.getTile((int)(x + 0.5), (int)(y + 0.5)).lookupProperty(TileProperty.BLOCKED))) {
             MapLoader.getCurrentCell().removeProjectile(this);
 		}
 		for (Entity e : MapLoader.getCurrentCell().getEntities()) {
 		    // apply damage
 		    if (e.intersects(hitbox) && !e.equals(currentCell.getPlayer())) {
 	            e.takeDamage(damage);
 	            currentCell.removeProjectile(this);
 	        }
 		}
 		
 	}
 	
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		return new Projectile(xy.getX(), xy.getY(), width, height,damage,angle);
 	}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) {
 		sprite.draw((int)((xy.getX()-1)*Config.getTileSize()), (int)((xy.getY()-1)*Config.getTileSize()), new Color(255,255,255));
 		
 	}
 
 }

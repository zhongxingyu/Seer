 package model;
 
 import interactions.Interaction;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import map.Map;
 import map.Tile;
 
 public abstract class EntityImpl implements Entity {	
 	protected static final int RIGHT = Map.MAP_WIDTH * Tile.TILE_WIDTH;
 	protected static final int BOTTOM = Map.MAP_HEIGHT * Tile.TILE_HEIGHT;
 	
 	protected Hitbox hitbox;
 	protected Location location;
 	
 	protected int width;
 	protected int height;
 	
 	protected BufferedImage image;
 	protected BufferedImage attackingImage;
 	
 	protected int tickspeed = 40; // number of ticks to get before update
 	protected int ticks = 0;
 
 	protected int health;
 	
 	public abstract void update();
 	protected abstract Hitbox makeHitbox();
 	public abstract BufferedImage getSprite ();
 	
 	public EntityImpl (Location location) {
 		this.location = location;
 		hitbox = makeHitbox();
 		
 		initialiseSpriteSheet();
 		Image i = getSprite ();
 		width = i.getWidth(null);
 		height = i.getHeight(null);
 	}
 	
 	/**
 	 * +ve amount => less health afterwards. 
 	 */
 	public void reduceHealth (int amount) {
 		health -= amount;
 	}
 	
 	public void draw (Graphics g) {
 		g.drawImage(getSprite(), location.x, location.y,  64, getSprite().getHeight(), null);
 	}
 	
 	public void interact(Interaction i) {
 		i.apply(this);
 	}
 	
 	public void move (int x, int y) {
 		int toX = location.x + x;
 		int toY = location.y + y;
 		if (toX  < Tile.TILE_WIDTH || toY < 0 || toX + width + 128 >= RIGHT || toY + height >= BOTTOM) return;		
 		
 		location.x += x;
 		location.y += y;
 		hitbox.move(x, y);
 	}
 	
 	public Location getLocation () {
 		return location;
 	}
 	
 	public Hitbox getHitbox(){
 		return this.hitbox;
 	}
 	
 }

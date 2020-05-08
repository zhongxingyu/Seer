 package level;
 
 import java.util.Map;
 
 import entity.Creature;
 
 public class SubLevel extends Level {
 	private Level parent;
 	
 	private int x;
 	private int y;
 	
 	/**
 	 * Set sublevel in the parent's center
 	 */
 	public SubLevel(Level parent, int width, int height) {
 		super(width, height);
 		
 		this.setParent(parent);
 		this.setX((this.getParent().getWidth() - width) / 2);
 		this.setY((this.getParent().getHeight() - height) / 2);
 	}
 	
 	public SubLevel(Level parent, int x, int y, int width, int height) {
 		super(width, height);
 		
 		this.setParent(parent);
 		this.setX(x);
 		this.setY(y);
 	}
 	
 	@Override
 	public Tile tile(int x, int y) {
 		return this.getParent().tile(this.getX() + x, this.getY() + y);
 	}
 	
 	@Override
 	public Creature getCreature(int x, int y) {
 		return this.getParent().getCreature(this.getX() + x, this.getY() + y);
 	}
 
 	public Level getParent() {
 		return parent;
 	}
 
 	public void setParent(Level parent) {
 		this.parent = parent;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		x = Math.max(x, 0);
 		x = Math.min(x, this.getParent().getWidth() - this.getWidth());
 		
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		y = Math.max(y, 0);
 		y = Math.min(y, this.getParent().getHeight() - this.getHeight());
 		
 		this.y = y;
 	}
 	
 	public void setCenter(Point point) {
 		this.setCenterX(point.getX());
 		this.setCenterY(point.getY());
 	}
 	
 	public void setCenterX(int x) {
 		this.setX(x - this.getWidth() / 2);
 	}
 	
 	public void setCenterY(int y) {
		this.setY(x - this.getHeight() / 2);
 	}
 	
 	@Override
 	public Map<Point, Creature> getCreatures() {
 		return this.getParent().getCreatures();
 	}
 
 }

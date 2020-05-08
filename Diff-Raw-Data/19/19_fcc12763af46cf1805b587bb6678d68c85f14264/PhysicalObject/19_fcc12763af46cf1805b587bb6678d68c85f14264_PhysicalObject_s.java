 package nitrogene.objecttree;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Point;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 
 import nitrogene.collision.Vector;
 import nitrogene.util.ImageBase;
 import nitrogene.util.Movement;
 import nitrogene.world.ArenaMap;
 
 public class PhysicalObject {
 	protected Movement movement;
 	protected ArenaMap map;
 	protected Shape boundbox;
 	protected Image mainimg;
 	protected float scalefactor;
 	protected int delta;
 	protected float rotation;
 	protected float width, height;
 
 	public PhysicalObject(float width, float height, Image img, float scalefactor, ArenaMap map){
 		width = 0;
 		height = 0;
 		this.map = map;
 		this.mainimg = img;
 		this.scalefactor = scalefactor;
 		movement = new Movement(map.getUpbound(), map.getDownbound(), map.getLeftbound(), map.getRightbound());
 	}
 	
 	protected void init(float width, float height){
 		this.width = width;
 		this.height = height;
 	}
 	
 	public void move(int thrust, int delta){
 		movement.Accelerate(new Vector(boundbox.getCenterX(),boundbox.getCenterY()), delta);
 		float mm = delta/1000f;
 		float gj = thrust*1f;
 		boundbox.setX(boundbox.getX()+((movement.getDx()*gj)*mm));
 		boundbox.setY(boundbox.getY()+((movement.getDy()*gj)*mm));
 	}
 	
 	public boolean isColliding(PhysicalObject obj){
 		
 		/*if(this.boundbox.getCenterX() + width + this.movement.getDx() >= obj.getCenterX() ||
 				this.boundbox.getCenterY() + height + this.movement.getDy() >= obj.getCenterY() ||
 				this.boundbox.getCenterX() - width - this.movement.getDx() <= obj.getCenterX() ||
 				this.boundbox.getCenterY() - height - this.movement.getDy() <= obj.getY()
 				){*/
 		double dist = Math.sqrt((boundbox.getCenterX()-obj.getCenterX())*(boundbox.getCenterX()-obj.getCenterX()) + (boundbox.getCenterY()-obj.getCenterY())*(boundbox.getCenterY()-obj.getCenterY()));
 		if(this.boundbox.getCenterX() + width + this.movement.getDx() >= dist || 
 			this.boundbox.getCenterY() + width + this.movement.getDy() >= dist){
 					return this.boundbox.intersects(obj.boundbox);
 		}
 		else return false;
 	}
 	
 	public boolean isContaining(float x, float y){
 		if(this.boundbox.getCenterX() + width + this.movement.getDx() >= x ||
 				this.boundbox.getCenterY() + height + this.movement.getDy() >= y ||
 				this.boundbox.getCenterX() - width - this.movement.getDx() <= x ||
 				this.boundbox.getCenterY() - height - this.movement.getDy() <= y
 				){
 		return this.boundbox.contains(x, y);
 		}
 		else return false;
 	}
 	
 	public float getRotation(){
 		return rotation;
 	}
 	
 	public Image getImage(){
 		return mainimg;
 	}
 	protected void changeOpac(float changepercent){
 		mainimg.setImageColor(1f, 1f, 1f, changepercent * 0.1f);
 	}
 	public Shape getShape(){
 		return boundbox;
 	}
 	public float getX(){
 		return boundbox.getX();
 	}
 	public float getY(){
 		return boundbox.getY();
 	}
 	public float getCenterX(){
 		if(boundbox!=null){
 		return boundbox.getCenterX();
 		}else return 0f;
 	}
 	public float getCenterY(){
 		if(boundbox!=null){
 		return boundbox.getCenterY();
 		}else return 0f;
 	}
 	public void setX(float x){
 		boundbox.setX(x);
 	}
 	public void setY(float y){
 		boundbox.setY(y);
 	}
 	public void setCenterX(float x){
 		boundbox.setCenterX(x);
 	}
 	public void setCenterY(float y){
 		boundbox.setCenterY(y);
 	}
 	public Movement getMovement(){
 		return movement;
 	}
 	public void setScale(float scale){
 		this.scalefactor = scale;
 	}
 	public float getScale(){
 		return this.scalefactor;
 	}
 	public Shape getBoundbox(){
 		return boundbox;
 	}
 	public void update(int delta) {
 	}
 }

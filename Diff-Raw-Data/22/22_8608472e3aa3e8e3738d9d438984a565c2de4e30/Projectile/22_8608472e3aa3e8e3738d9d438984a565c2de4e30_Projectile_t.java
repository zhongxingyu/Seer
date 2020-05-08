 package projekt;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 public class Projectile implements Cloneable{
 	protected Rectangle hitBox;
 	protected float speed = 0.1f;
 	protected Vector2d pos = new Vector2d(0, 0);
 	protected Vector2d destination = new Vector2d(0, 0);
 	protected float damage;
 	protected float time = 0f;
 	protected Image image;
 	protected Image origImage;
 	protected double rotation = 0f;
 	protected Entity owner;
 	
 	public Projectile(Vector2d destination, float damage, float speed, Image image, Entity owner) throws SlickException{
 		this.destination = this.destination.add(destination);
 		this.damage = damage;
 		this.speed = speed;
 		this.image = new Image(image.getResourceReference());
 		this.owner = owner;
 		this.hitBox = new Rectangle(pos.getX(), pos.getY(), image.getWidth(), image.getHeight());
 	}
 	public Projectile(float destX, float destY, float damage, float speed, Image image, Entity owner)throws SlickException{
 		this(new Vector2d(destX, destY), damage, speed, image, owner);
 	}
 	
 	public boolean collidesWith(Entity entity){
 		return this.hitBox.intersects(entity.getHitBox());
 	}
 	public int getHeight(){
 		return this.image.getHeight();
 	}
 	public int getWidth(){
 		return this.image.getWidth();
 	}
 	public Entity getOwner(){
 		return this.owner;
 	}
 	public void damage(Entity entity){
 		entity.setDamage(damage);
 	}
 	public void create(float x, float y){
 		this.pos = new Vector2d(x, y);
 	}
 	public void update(int delta){
 		this.image = this.image.copy();
		Vector2d normalized = destination.getNormalized();
 		this.pos.setX((this.pos.getX() + speed*delta*normalized.getX()));
 		this.pos.setY((this.pos.getY() + speed*delta*normalized.getY()));
 		this.hitBox.setLocation(this.pos.getX(), this.pos.getY());
		rotation = Math.toDegrees(Math.atan2(normalized.getY(),normalized.getX()));
 		this.image.setRotation((float)rotation);
			
 	}
 	
 	public void draw(int xOffset, int yOffset){
 		this.image.draw(this.pos.getX()+xOffset, this.pos.getY()+yOffset);
 		
 	}
 	public Projectile Clone() throws CloneNotSupportedException{
 		return (Projectile)super.clone();
 	}
 }

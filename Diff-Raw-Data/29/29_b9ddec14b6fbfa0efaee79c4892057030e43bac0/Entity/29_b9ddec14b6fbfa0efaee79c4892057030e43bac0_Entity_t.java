 package chalmers.TDA367.B17.model;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.controller.GameController.RenderLayer;
 import org.newdawn.slick.geom.*;
 
 public abstract class Entity{
 	protected int id; // The id of this object
 	protected boolean active;
 	protected Shape shape;
 	protected String spriteID;
 	private double rotation;
 	protected RenderLayer renderLayer;
 
 	/**
 	 * 
 	 * 
 	 */
 	public Entity(){
 		this.id = GameController.getInstance().getWorld().generateID();
 		GameController.getInstance().getWorld().addEntity(this);
 		active = true;
 		shape = new Point(-1, -1);
 		rotation = 0;
 		spriteID = "";
 		renderLayer = GameController.RenderLayer.FOURTH;
 	}
 	
 	public RenderLayer getRenderLayer(){
 		return renderLayer;
 	}
 	
 	public boolean isActive(){
 		return active;
 	}
 	
 	public double getRotation(){
 		return rotation;
 	}
 	
	public void setRotation(double rotation){
		this.rotation = rotation;
	}
	
 	/**
 	 * Get the position of this entity
 	 * @return A vector containing the position
 	 */
 	public Vector2f getPosition(){
 		return new Vector2f(shape.getCenterX(), shape.getCenterY());
 	}
 	
 	/**
 	 * Get the id of this entity
 	 * @return The id of this object
 	 */
 	public int getId(){
 		return id;
 	}
 	
 	/**
 	 * Get the size of this entity
 	 * @return The size of this entity
 	 */
 	public Vector2f getSize(){
 		return new Vector2f(shape.getWidth(), shape.getHeight());
 	}
 
 
 	/**
 	 * Set the size of this entity
 	 * @param size The new size of this entity
 	 */
 	public void setSize(Vector2f size){
 		this.shape = this.shape.transform(Transform.createScaleTransform(size.getX()/this.shape.getWidth(), size.getY()/this.shape.getHeight()));
 	}
 
 	/**
 	 * Set the id of this entity
 	 * @param id The new id for this object
 	 */
 	public void setId(int id){
 		this.id = id;
 	}
 	
 	/**
 	 * Set the position of this entity
 	 * @param position The values for the new position
 	 */
 	public void setPosition(Vector2f position){
 		if(this.shape instanceof Point)
 			shape.setLocation(position.x, position.y);
 		else
 			this.shape = this.shape.transform(Transform.createTranslateTransform(position.getX() - getPosition().getX(), position.getY() - getPosition().getY()));
 	}
 
 	public Shape getShape() {
 		return shape;
 	}
 
 	public void setShape(Shape shape) {
 		this.shape = shape;
 	}
 
 	public void update(int delta){
 		
 	}
 
 	/**
 	 * Get the position of where the entity's sprite is to be drawn.
 	 * @return The position of the entity's sprite.
 	 */
 	public Vector2f getSpritePosition(){
 		return new Vector2f(getPosition().x - getShape().getWidth()/2, getPosition().y - getShape().getHeight()/2);
 	}
 
 	/**
 	 * Callback method for collisions.
 	 * This gets called when two game objects (entities) collides.
 	 * @param entity the other object
 	 */
 	public void didCollideWith(Entity entity){}
 
 	public void destroy(){
 		GameController.getInstance().getWorld().removeEntity(this);
 	}
 	
 	public String getSpriteID(){
 		return spriteID;
 	}
 }

 package org.gaem.bodies;
 
 import org.gaem.ObjectManager;
 import org.jsfml.graphics.Color;
 import org.jsfml.graphics.Drawable;
 import org.jsfml.graphics.FloatRect;
 import org.jsfml.graphics.RectangleShape;
 import org.jsfml.graphics.RenderStates;
 import org.jsfml.graphics.RenderTarget;
 import org.jsfml.system.Vector2f;
 
 public abstract class AbstractBody implements Drawable{
 	// ...:
 	private RectangleShape boundingBox = new RectangleShape();
 	{
 		boundingBox.setOutlineColor(Color.BLACK);
 		boundingBox.setOutlineThickness(-1);
 		boundingBox.setFillColor(Color.TRANSPARENT);
 		setBounded(true);
 	}
 	
 	// Geometry:
 	protected Vector2f position;
 	protected Vector2f size;
 	protected Vector2f origin;
 	
 	//Physics
 	protected Vector2f v = new Vector2f(0 ,0);
 	
 	//Dependencys
 	protected ObjectManager objectManager;
 	
 	// Behavior:
 	private boolean killer;
 	private boolean immortal;
 	protected boolean collidable;
 	protected boolean isOnGround;
 	
 	// Drawing:
 	private boolean bounded;
 	
 	// For fun:
 	private int colourRG = 255;
 	private int colourSpd = 370;
 	
 	public AbstractBody() {
 		setPosition(new Vector2f(0, 0));
 		setSize(new Vector2f(10, 10));
 	}
 	
 	public AbstractBody(Vector2f position) {
 		setPosition(position);
 		setSize(new Vector2f(10, 10));
 	}
 	
 	public AbstractBody(Vector2f position, Vector2f size) {
 		setPosition(position);
 		setSize(size);
 	}
 	
 	// Boilerplate:
 	public Vector2f getPosition() {
 		return position;
 	}
 	
 	public void setPosition(Vector2f position) {
 		this.position = position;
 		boundingBox.setPosition(position);
 	}
 	
 	public void setPosition(float x, float y) {
 		this.position = new Vector2f(x,y);
 		boundingBox.setPosition(position);
 	}
 	
 	public Vector2f getSize() {
 		return size;
 	}
 	
 	public void setSize(float x, float y) {
 		this.size = new Vector2f(x, y);
 		boundingBox.setSize(size);
 	}
 	
 	public void setSize(Vector2f size) {
 		this.setSize(size.x, size.y);
 	}
 	
 	public Vector2f getOrigin() {
 		return origin;
 	}
 	
 	public void setOrigin(Vector2f origin) {
 		this.origin = origin;
 	}
 	
 	public boolean isKiller() {
 		return killer;
 	}
 	
 	public void setKiller(boolean killer) {
 		this.killer = killer;
 	}
 	
 	public boolean isImmortal() {
 		return immortal;
 	}
 	
 	public void setImmortal(boolean immortal) {
 		this.immortal = immortal;
 	}
 	
 	public boolean isBounded() {
 		return bounded;
 	}
 	
 	public void setBounded(boolean bounded) {
 		this.bounded = bounded;
 	}
 	
 	public void setOutlineColour(Color colour) {
 		boundingBox.setOutlineColor(colour);
 	}
 	
 	//Just for fun:
 	public void fancyBeam() {
 		colourRG = 255;
 	}
 	
 	public void fancyBeamUpdate (float dt) {
 		colourRG -= (int) colourSpd * dt;
 		if (colourRG < 0) {
 			colourRG = 0;
 		}
 		boundingBox.setOutlineColor(new Color(colourRG, colourRG/4, colourRG/6));
 	}
 	
 	// Useful stuff:
 	public void shift(float dx, float dy) {
 		setPosition(position.x + dx, position.y + dy);
 	}
 	
 	public void move (float dx, float dy) {
 		if (collidable) {
 			//Calculating ort
 			float abs_r = (float) Math.sqrt(dx*dx + dy*dy);
 			
 			// Prevent from division by zero
 			if (abs_r == 0) {
 				return;
 			}
 			
 			Vector2f tr = new Vector2f(0, 0);
 			Vector2f ir = new Vector2f(dx/abs_r, dy/abs_r);
 			boolean collides;
 			
 			// Pre-check
 			if (objectManager.getCollision(this) != null) {
 				System.err.println("Unresolved collision!");
 			}
 			
 		
 			// Approximation loop
 			while (Math.abs(tr.x) <= Math.abs(dx) && Math.abs(tr.y) <= Math.abs(dy)) {
 				//step
 				
 				shift(ir.x, ir.y);
 				tr = Vector2f.add(ir, tr);
 				
 				if (objectManager.getCollision(this) != null) {
 					//step back
 					
 					shift(-ir.x, -ir.y);
 					tr = Vector2f.sub(ir, tr);
 					
 					if (ir.x != 0) {
 						// Move over X-axis
 						shift(ir.x, 0);
 						//Test collision
 						collides = objectManager.checkCollision(this);
 						// Return after moving
 						shift(-ir.x, 0);
 						
 						if(collides) {
 							//X collision!
 							//resolve X collision
 							
 							//Zero-velocity
 							v = new Vector2f(0, v.y);
 							
 							//Accurate positioning
 							shift(ir.x, 0);
 							AbstractBody other = objectManager.getCollision(this);
 							
 							if (ir.x > 0) {
 								this.setPosition(other.position.x - this.size.x, position.y);
 							} else {
 								this.setPosition(other.position.x + other.size.x, position.y);
 							}
 							
 							if (ir.y != 0) {
 								ir = new Vector2f(0, ir.y > 0 ? 1 : -1);
 							} else {
 								break;
 							}
 							
 						}
 						
 					}
 					
 					if (ir.y != 0) {
 						// Move over Y-axis
 						shift(0, ir.y);
 						//Test collision
 						collides = objectManager.checkCollision(this);
 						// Return after moving
 						shift(0, -ir.y);
 						
 						if (collides) {
 							//Y collision!
 							//resolve Y collision
 							
 							//Zero-velocity
 							v = new Vector2f(v.x, 0);
 							
 							//Accurate positioning
 							shift(0, ir.y);
 							AbstractBody other = objectManager.getCollision(this);
 							
 							if (ir.y > 0) {
 								this.setPosition(position.x, other.position.y - this.size.y);
 							} else {
								this.setPosition(position.x, other.position.y + other.size.y);
 							}
 							
 							if (ir.x != 0) {
 								ir = new Vector2f(ir.x > 0 ? 1 : -1, 0);
 							} else {
 								break;
 							}
 						}
 					}
 				}
 			}
 		} else {
 			shift(dx, dy);
 		}
 		
 		
 	}
 	
 	//TODO SHIT
 	public void move(Vector2f dr) {
 		move(dr.x, dr.y);
 	}
 	
 	public boolean checkCollision(AbstractBody other) {
 		float dxPos = this.position.x - other.position.x;
 		if ((this.size.x > -dxPos) && (other.size.x > dxPos)) {
 			float dyPos = this.position.y - other.position.y;
 			if ((this.size.y > -dyPos) && (other.size.y > dyPos)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void drawBoundingBox(RenderTarget target, RenderStates states) {
 		if(bounded) {
 			target.draw(boundingBox, states);
 		}
 	}
 	
 	// Interface:
 	public abstract void update(float dt);
 
 	public abstract void draw(RenderTarget target, RenderStates states);
 }

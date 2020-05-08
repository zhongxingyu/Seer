 package beatbots.simulation;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Note implements Entity {
 	
	private static final float NOTE_SPEED = 0.1f;
 
 	private Node startingNode;
 	
 	private boolean isActive;
 	
 	private Vector2f position;
 	private Vector2f velocity;
 	
 	private Node target;
 	
 	@Override
 	public boolean isActive() {
 		
 		return this.isActive;
 	}
 	
 	public Vector2f getPosition() {
 		
 		return this.position;
 	}
 	
 	public Note(Node startingNode) {
 		
 		super();
 		
 		this.startingNode = startingNode;
 		
 		this.position = new Vector2f();
 		this.velocity = new Vector2f();
 		
 		this.target = null;
 	}
 	
 	@Override
 	public void init(GameContainer container) {
 		
 		this.isActive = true;
 		
 		this.position.set(this.startingNode.getPosition());
 		
 		this.target = this.startingNode.getSuccessor();
 		
		this.setVelocityToTarget();
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) {
 		
 		if (this.target == null) {
 			
 			this.deactivate();
 		}
 		else {
 			
 			this.position.add(this.velocity.copy().scale(delta));
 			
 			if (this.position.distanceSquared(this.target.getPosition()) < NOTE_SPEED * NOTE_SPEED) {
 				
 				this.position.set(this.target.getPosition());
 				
 				this.target = this.target.getSuccessor();
 				
				this.setVelocityToTarget();
 			}
 		}
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics graphics) {
 		
 		graphics.setColor(Color.white);
 		
 		graphics.drawRect(
 				this.position.x - 2f,  
 				this.position.y - 2f, 
 				4f, 
 				4f);
 	}
 
 	@Override
 	public void deactivate() {
 		
 		this.isActive = false;
 	}
 
 	@Override
 	public void destroy(GameContainer gameContainer) {
 		
 	}
 	
 	private void setVelocityToTarget() {
 		
 		this.velocity.set(this.target.getPosition().copy().sub(this.position).normalise().scale(NOTE_SPEED));
 	}
 }

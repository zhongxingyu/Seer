 package platformer.core.model.gameobject.impl;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.systems.Simulatable;
 import platformer.core.renderer.Renderable;
 import platformer.core.renderer.RendererInstructions;
 import platformer.core.renderer.impl.StaticTextureRenderInstructions;
 
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class ExampleGameObject implements GameObject, Simulatable, Renderable {
 	private Vector3 position;
 	private Rectangle bounds;
 	private Body body;
 	private boolean isGrounded;
 	private RendererInstructions rendererInstructions;
 
 	public ExampleGameObject(Vector3 position, Rectangle bounds, Body body) {
 		this.position = position;
 		this.bounds = bounds;
 		this.body = body;
 		this.rendererInstructions = new StaticTextureRenderInstructions(
 				"stickman");
 	}
 
 	@Override
 	public boolean canBeRemoved() {
 		return false;
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return bounds;
 	}
 
 	@Override
 	public String getId() {
 		return "player";
 	}
 
 	@Override
 	public Body getPhysicsBody() {
 		return body;
 	}
 
 	@Override
 	public Vector3 getPosition() {
 		return position;
 	}
 
 	@Override
 	public RendererInstructions getRendererInstructions() {
 		return rendererInstructions;
 	}
 
 	@Override
 	public String getTextureName() {
 		return null;
 	}
 
 	@Override
 	public boolean isGrounded() {
 		return isGrounded;
 	}
 
 	@Override
 	public void move(Vector3 vector) {
 
 		if (vector.y != 0) {
			float impulseRatio = body.getMass() * 2;
 			// Jump
 			if (isGrounded) {
 				body.applyLinearImpulse(new Vector2(0, impulseRatio),
 						body.getWorldCenter());
 			}
 		}
 
 		if (vector.x != 0) {
 			// Run
 			body.applyForce(new Vector2((body.getMass()) * vector.x, 0),
 					body.getWorldCenter());
 		}
 	}
 
 	@Override
 	public void setIsGrounded(boolean bool) {
 		this.isGrounded = bool;
 	}
 
 	@Override
 	public void setPosition(Vector3 position) {
 		this.position = position;
 	}
 
 }

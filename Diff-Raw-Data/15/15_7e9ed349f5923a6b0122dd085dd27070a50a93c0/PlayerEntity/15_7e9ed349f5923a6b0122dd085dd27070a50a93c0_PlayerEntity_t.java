 package uk.me.jumped.gentlemenfighter.gameplay.entities;
 
 import uk.me.jumped.gentlemenfighter.Constants;
 import uk.me.jumped.gentlemenfighter.graphics.AnimatedSprite;
 import uk.me.jumped.gentlemenfighter.graphics.Frame;
 import uk.me.jumped.gentlemenfighter.input.AbstractController;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.controllers.Controller;
 import com.badlogic.gdx.controllers.ControllerListener;
 import com.badlogic.gdx.controllers.PovDirection;
 import com.badlogic.gdx.controllers.mappings.Ouya;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 
 public class PlayerEntity extends Entity {
 
 	private AbstractController controller;
 	private static final float HEIGHT = 100f, WIDTH = 100f, FACINGCHANGE_DEADZONE = 0.1f;
	private static final float JUMP_FORCE = 30000f * Constants.WORLD_TO_BOX;
 	private static final float GROUNDED_STICK_SCALER = 1000f;
	private static final float AIR_STICK_SCALER = 300f;
 	private AnimatedSprite walkingSprite = new AnimatedSprite();
 	private boolean facingForward = true;
 	public final String playerSpriteName;
 	
 	public boolean grounded = false;
 
 	public PlayerEntity(float x, float y, final String playerSpriteName, final AbstractController controller, EntityManager parentManager) {
 		super(genPlayerBodyDef(x,y), genFixtureDef(), parentManager);
 		this.playerSpriteName = playerSpriteName;
 		this.controller = controller;
 		
 		// sprite setup.
 		this.walkingSprite.Batch = parentManager.getGameplayScreen().getMidSpriteBatch();
 		this.walkingSprite.X = x;
 		this.walkingSprite.Y = y;
 		this.walkingSprite.Height = HEIGHT;
 		this.walkingSprite.Width = WIDTH;
 		
 		Frame f = null;
 		
 		TextureRegion[] tr = TextureRegion.split(new Texture(Gdx.files.internal(Constants.Files.Graphics.PLAYER + this.playerSpriteName + ".png")), 32, 32)[0];
 		for(TextureRegion r : tr) {
 			f = new Frame();
 			f.Region = r;
 			this.walkingSprite.Frames.add(f);
 		}
 		
 	}
 
 	public void setController(AbstractController controller) {
 		this.controller = controller;
 	}
 
 	@Override
 	public void update(float elapsedMS) {
 		
 		if (this.grounded) {
 			Vector2 vel = new Vector2(this.controller.getHorizonatalMove()
 					* GROUNDED_STICK_SCALER, 0).mul(Constants.WORLD_TO_BOX);
 			this.body.applyForceToCenter(vel);
 			
 			if(this.controller.isJumpPressed())
 			{
 				this.body.applyForceToCenter(0f, JUMP_FORCE);
 				this.grounded = false;
 			}
 		}
 		else {
 			Vector2 vel = new Vector2(this.controller.getHorizonatalMove()
 					* AIR_STICK_SCALER, 0).mul(Constants.WORLD_TO_BOX);
 			this.body.applyForceToCenter(vel);
 		}
 		
 		Vector2 curVol = this.body.getLinearVelocity().mul(Constants.BOX_TO_WORLD);
 		
 		if(curVol.x > FACINGCHANGE_DEADZONE) {
 			facingForward = true;
 		}
 		else if(curVol.x < -FACINGCHANGE_DEADZONE) {
 			facingForward = false;
 		}
 		else { // velocity deadzone.
 			this.walkingSprite.CurrentFrameNum = 0;
 		}
 		
 		this.walkingSprite.flippedFlags = (this.facingForward) ? AnimatedSprite.FLIPPED_NONE : AnimatedSprite.FLIPPED_HORIZONTAL;
 		
 		Vector2 pos = this.getPosition();
 		
 		this.walkingSprite.X = pos.x - (WIDTH / 2);
 		this.walkingSprite.Y = pos.y - (HEIGHT / 2);
 		
 		this.walkingSprite.update(Math.abs(elapsedMS * curVol.x) * 0.05f);
 		
 		Gdx.app.log("Player", this.playerSpriteName + ", Box pos: " + this.body.getPosition() + ", world pos:" + this.getPosition() + ", Velocity: " + curVol);
 	}
 
 	@Override
 	public void render() {
 		this.walkingSprite.render();
 		
 	}
 	
 	private static FixtureDef genFixtureDef() {
 		// could be cached.
 		FixtureDef fixDef = new FixtureDef();
 		PolygonShape shape = new PolygonShape();
 		// the box is slightly smaller to get the edges closer to the character sprite.
 		shape.setAsBox(((WIDTH - 60) / 2) * Constants.WORLD_TO_BOX, (HEIGHT / 2) * Constants.WORLD_TO_BOX);
 		fixDef.shape = shape;
 		fixDef.density = 0.9f; 
 		fixDef.friction = 0.4f;
 		fixDef.restitution = 0f;
 		
 		return fixDef;
 	}
 	
 	private static BodyDef genPlayerBodyDef(float posX, float posY) {
 		// could be cached partially.
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.fixedRotation = true;
 		bodyDef.type = BodyType.DynamicBody;
 		bodyDef.position.set(new Vector2(posX + (WIDTH / 2), posY + (HEIGHT / 2)).mul(Constants.WORLD_TO_BOX));
 		
 		return bodyDef;
 	}
 }

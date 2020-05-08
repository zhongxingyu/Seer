 package com.gamegear.firstwing;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.utils.Array;
 import com.gamegear.firstwing.actors.Actor;
 import com.gamegear.firstwing.helper.Helper;
 import com.gamegear.firstwing.levels.json.LevelProperties.ColorEnum;
 import com.gamegear.firstwing.screens.GameScreen;
 import com.gamegear.firstwing.screens.GameScreen.GameState;
 
 public class WorldRenderer {
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 7f;
 	
 	private GameScreen game;
 	private OrthographicCamera cam;
 	
 	private float timestep;
 
 	/** for debug rendering **/
 	@SuppressWarnings("unused")
 	private Box2DDebugRenderer debugRenderer;
 	private ShapeRenderer shapeRenderer;
 
 	/** Textures **/
 	private boolean debug = false;
 	@SuppressWarnings("unused")
 	private int width;
 	@SuppressWarnings("unused")
 	private int height;
 	private Iterator<Body> tmpBodies;
 	
 	/** Background **/
 	private Color currentBgColor;
 	private Color sourceBgColor;
 	private Color targetBgColor;
 	private float maxtime = 2.5f;
 	private float step = 0;
 	private Boolean changeBackgroundColor = false;
 	
 	// Particle System
 	// Explosion pool
 	private ParticleEffect prototype;
 	private ParticleEffectPool pool;
 	private Array<PooledEffect> effects;
 	
 	// After burner
 	//ParticleEffect[] p;
 	HashMap<ColorEnum,ParticleEffect> p;
 	Vector2 behindShip;
 	ColorEnum activeAfterburner;
 	
 	public float cameraX = 0;
 	public float cameraY = 0;
 	private float currentSpeed = 1;
 	
 	public void setSize (int w, int h) {
 		this.width = w;
 		this.height = h;
 	}
 	public boolean isDebug() {
 		return debug;
 	}
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	public WorldRenderer(GameScreen game, boolean debug) {
 		this.game = game;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cameraX = CAMERA_WIDTH / 2f;
 		this.cameraY = CAMERA_HEIGHT / 2f;
 		this.cam.position.set(this.cameraX, this.cameraY, 0);
 		this.cam.update();
 		this.debug = debug;
 		this.debugRenderer = new Box2DDebugRenderer();
 		this.shapeRenderer = new ShapeRenderer();
 		this.shapeRenderer.setProjectionMatrix(cam.combined);
 		
 		//Particle effect
 		prototype = new ParticleEffect();
 		prototype.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects"));
 		//prototype.setPosition(world.getBob().getBody().getWorldCenter().x, world.getBob().getBody().getWorldCenter().y);
 		
 		behindShip = game.level.getPlayer().getBody().getWorldPoint(new Vector2(-0.3f,0));
 		
 		activeAfterburner = ColorEnum.none;
 		this.changeAfterBurnerColor(ColorEnum.none);
 		
 		pool = new ParticleEffectPool(prototype, 2, 20);
 		effects = new Array<PooledEffect>();
 		
 		this.currentBgColor = Helper.darkenColor(Helper.colorEnumToColor(ColorEnum.blue), 0.5f);
 	}
 	
 	public void changeAfterBurnerColor(ColorEnum color)
 	{
 		behindShip = game.level.getPlayer().getBody().getWorldPoint(new Vector2(-0.3f,0));
 		if(p == null)
 		{
 			p = new HashMap<ColorEnum, ParticleEffect>();
 			for(ColorEnum pColor : ColorEnum.values()){
 				p.put(pColor, new ParticleEffect());
 				p.get(pColor).load(Gdx.files.internal("effects/afterburner-" + pColor.toString() + ".p"), Gdx.files.internal("effects"));
 				p.get(pColor).setPosition(this.behindShip.x, this.behindShip.y);
 			}
 		}
 		
 		if(activeAfterburner == color)
 		{
 			return;
 		}
 		else
 		{
 			// Stop current afterburner
 			p.get(activeAfterburner).reset();
 			activeAfterburner = color;
 			
 			// Start new one
 			p.get(activeAfterburner).setPosition(behindShip.x, behindShip.y);
 			p.get(activeAfterburner).start();
 		}
 	}
 	
 	public void reset()
 	{
 		this.cameraX = CAMERA_WIDTH / 2f;
 		this.cameraY = CAMERA_HEIGHT / 2f;
 		this.cam.position.set(this.cameraX, this.cameraY, 0);
 		this.currentSpeed = 1;
 	}
 	
 	public void callParticleSystem(float x, float y)
 	{
 		PooledEffect effect = pool.obtain();
 		effect.setPosition(x, y);
 		effect.update(this.timestep);
 		
 		effects.add(effect);
 	}
 	
 	public void render(GameState state, SpriteBatch batch) {
 		this.timestep = state == GameState.Running ? Gdx.graphics.getDeltaTime() : 0f;
 		moveCamera(game.level.getPlayer().getBody().getWorldCenter().x, game.level.getPlayer().getBody().getWorldCenter().y, game.level.getSpeed(cameraX));
 		
 		if(this.changeBackgroundColor){
 			this.changeBackgroundColor();
 		}
 		
 		shapeRenderer.begin(ShapeType.FilledRectangle);
 	        shapeRenderer.setColor(this.currentBgColor);
 	        shapeRenderer.filledRect(0, 0, cam.viewportWidth, cam.viewportHeight);
         shapeRenderer.end();
         
         batch.begin();
 			for(Sprite bg : game.level.getBackground()){
				if(bg.getX() - cam.position.x < (int)CAMERA_WIDTH + 1 && bg.getX() - cam.position.x > -(int)CAMERA_WIDTH - 1){
 					bg.draw(batch);
 				}
 			}
 			
 			tmpBodies = game.world.getBodies();
 			while(tmpBodies.hasNext()){
 				Body node = tmpBodies.next();
 				
 				if(node.getUserData() != null && node.getUserData() instanceof Actor)
 				{
 					Actor actor = (Actor) node.getUserData();
 					float rotationAngle = actor.getBody().getAngle() * MathUtils.radiansToDegrees - 90;
 					Vector2 position = actor.getBody().getPosition();
 					float width = actor.getWidth();
 					float height = actor.getHeight();
 					float scale = actor.getScale();
 					if (position.x - cam.position.x < (int) CAMERA_WIDTH / 2 + 1
 						&& position.x - cam.position.x > -(int) CAMERA_WIDTH / 2 - 1
 						&& position.y - cam.position.y < (int) CAMERA_HEIGHT /2 + 1
 						&& position.y - cam.position.y > -(int) CAMERA_HEIGHT / 2 - 1
 						) {
 						try{
 							batch.draw(
 								actor.getTexture(),
 								position.x - width / 2,
 								position.y - height / 2,
 								width / 2,
 								height / 2,
 								width,
 								height,
 								scale,
 								scale,
 								rotationAngle,
 								false);
 						} catch (NullPointerException ex){
 							Gdx.app.log("Rendering", "Name: " + actor.getProperties().Name + " || " + ex.getMessage());
 						}
 					}
 					
 					if(actor.isAnimation())
 					{
 						actor.update(Gdx.graphics.getDeltaTime());
 					}
 				}
 			}
 			
 			// Render particle effects
 			for(PooledEffect effect : effects)
 			{
 				effect.draw(batch, this.timestep);
 				if(effect.isComplete()){
 					effects.removeValue(effect, true);
 					effect.free();
 				}
 			}
 			
 			behindShip = game.level.getPlayer().getBody().getWorldPoint(new Vector2(-0.3f,0));
 			p.get(activeAfterburner).setPosition(behindShip.x, behindShip.y);
 			p.get(activeAfterburner).update(this.timestep);
 			p.get(activeAfterburner).draw(batch, this.timestep);
 		batch.end();
 		//debugRenderer.render(world.getWorld(), cam.combined);
 		
 		game.world.step(this.timestep, 3, 3);
 	}
 	
 	public void moveCamera(float x,float y, float speed){
 		if(this.currentSpeed != speed){
 			this.sourceBgColor = this.currentBgColor;
 			this.targetBgColor = this.getColor((int)speed);
 			this.changeBackgroundColor = true;
 		}
 		
 		this.currentSpeed = speed;
 		
 		//Cap camera at the top and bottom
 		if(y + 4 > 12) { cameraY = 8f; }
 		else if(y - 3 < 0) { cameraY = 3f; }
 		else { cameraY = y; }
 		
 		//Move camera with speed
 		cameraX += this.timestep * currentSpeed;
 		
 		if(speed > 2.5f)
 		{
 			cam.position.set(cameraX + (float)(Math.random() - 0.5)/25, cameraY + (float)(Math.random() - 0.5)/25, 0);
 		}
 		else
 		{
 			cam.position.set(cameraX, cameraY, 0);
 		}
         
         cam.update();
 	}
 	
 	private Color getColor(int speed){
		if(speed > 2.5f){
 			return Helper.darkenColor(Helper.colorEnumToColor(ColorEnum.red), 0.5f);
		} else if(speed > 1.5f) {
 			return Helper.darkenColor(Helper.colorEnumToColor(ColorEnum.orange), 0.5f);
 		} else{
 			return Helper.darkenColor(Helper.colorEnumToColor(ColorEnum.blue), 0.5f);
 		}
 	}
 	
 	private void changeBackgroundColor()
 	{
 	    if(this.step >= this.maxtime){
 	    	this.step = 0;
 	    	this.changeBackgroundColor = false;
 	        return;
 	    }
 	    
 	    this.step += this.timestep;
 	    float percentComplete = this.step / this.maxtime;
 	    float percentGone = 1 - percentComplete;
 	    currentBgColor.r = this.sourceBgColor.r * percentGone + this.targetBgColor.r * percentComplete;
 	    currentBgColor.g = this.sourceBgColor.g * percentGone + this.targetBgColor.g * percentComplete;
 	    currentBgColor.b = this.sourceBgColor.b * percentGone + this.targetBgColor.b * percentComplete;
 	}
 	
 	public OrthographicCamera getCam() {
 		return cam;
 	}
 }

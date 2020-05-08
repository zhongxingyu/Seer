 package com.gamegear.firstwing;
 
 import java.util.Iterator;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.utils.Array;
 import com.gamegear.firstwing.actors.Actor;
 
 public class WorldRenderer {
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 7f;
 	
 	private FwWorld world;
 	private OrthographicCamera cam;
 
 	/** for debug rendering **/
 	private Box2DDebugRenderer debugRenderer;
 	private ShapeRenderer shapeRenderer;
 
 	/** Textures **/
 	public static TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("textures/textures.pack"));
 	private SpriteBatch spriteBatch;
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
 	ParticleEffect[] p;
 	Vector2 behindShip;
 	int activeAfterburner = 7;
 	String[] particleFiles = {"effects/afterburner-red.p","effects/afterburner-yellow.p","effects/afterburner-blue.p","effects/afterburner-lightblue.p", "effects/afterburner-orange.p", "effects/afterburner-purple.p","effects/afterburner-green.p","effects/afterburner.p"};
 	
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
 
 	public WorldRenderer(FwWorld world, boolean debug) {
 		this.world = world;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cameraX = CAMERA_WIDTH / 2f;
 		this.cameraY = CAMERA_HEIGHT / 2f;
 		this.cam.position.set(this.cameraX, this.cameraY, 0);
 		this.cam.update();
 		this.debug = debug;
 		this.spriteBatch = new SpriteBatch();
 		this.debugRenderer = new Box2DDebugRenderer();
 		this.shapeRenderer = new ShapeRenderer();
 		this.shapeRenderer.setProjectionMatrix(cam.combined);
 		
 		//Particle effect
 		prototype = new ParticleEffect();
 		prototype.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects"));
 		//prototype.setPosition(world.getBob().getBody().getWorldCenter().x, world.getBob().getBody().getWorldCenter().y);
 		
 		behindShip = world.getBob().getBody().getWorldPoint(new Vector2(-0.3f,0));
 		
 		this.changeAfterBurnerColor(0);
 		
 		pool = new ParticleEffectPool(prototype, 2, 20);
 		effects = new Array<PooledEffect>();
 		
 		this.currentBgColor = new Color(0f, 0f, 0.5f, 1f);
 	}
 	
 	public void changeAfterBurnerColor(int color)
 	{
 		if(p == null)
 		{
 			p = new ParticleEffect[8];
 			for(int i = 0; i < 8; i++)
 			{
 				p[i] = new ParticleEffect();
 				p[i].load(Gdx.files.internal(particleFiles[i]), Gdx.files.internal("effects")); //files.internal loads from the "assets" folder
 				p[i].setPosition(behindShip.x, behindShip.y);
 			}
 		}
 		
 		if(activeAfterburner == color)
 		{
 			return;
 		}
 		
 		activeAfterburner = color;
 		
 		for(int i = 0; i < 8; i++)
 		{
 			if(i != activeAfterburner)
 			{
 				p[i].reset();
 			}else
 			{
 				p[activeAfterburner].setPosition(behindShip.x, behindShip.y);
 				p[activeAfterburner].start();
 			}
 		}
 		//p.update(Gdx.graphics.getDeltaTime());
 	}
 	
 	public void callParticleSystem(float x, float y)
 	{
 		PooledEffect effect = pool.obtain();
 		effect.setPosition(x, y);
 		effect.update(Gdx.graphics.getDeltaTime());
 		
 		effects.add(effect);
 //		prototype.reset();
 //		prototype.setPosition(x,y);
 //		prototype.start();
 //		System.out.println("Effect restarting");
 	}
 	
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		moveCamera(world.getBob().getBody().getWorldCenter().x, world.getBob().getBody().getWorldCenter().y, world.level.getSpeed(cameraX));
 		
 		if(this.changeBackgroundColor){
 			this.changeBackgroundColor();
 		}
 		
 		shapeRenderer.begin(ShapeType.FilledRectangle);
 	        shapeRenderer.setColor(this.currentBgColor);
 	        shapeRenderer.filledRect(0, 0, cam.viewportWidth, cam.viewportHeight);
         shapeRenderer.end();
         
 		spriteBatch.setProjectionMatrix(cam.combined);
 		spriteBatch.begin();
 			for(Sprite bg : world.level.getBackground()){
 				if(bg.getX() - cam.position.x < (int)CAMERA_WIDTH && bg.getX() - cam.position.x > -(int)CAMERA_WIDTH){
 					bg.draw(spriteBatch);
 				}
 			}
 			
 			tmpBodies = world.world.getBodies();
 			Body node;
 			while(tmpBodies.hasNext()){
 				node = tmpBodies.next();
 				
 				if(node.getUserData() != null && node.getUserData() instanceof Actor)
 				{
 					Actor actor = (Actor) node.getUserData();
 					float rotationAngle = actor.getBody().getAngle() * MathUtils.radiansToDegrees - 90;
 					Vector2 position = actor.getBody().getPosition();
 					float width = actor.getWidth();
 					float height = actor.getHeight();
 					float scale = actor.getScale();
 					
 					spriteBatch.draw(
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
 				}
 			}
 			
 			// Render particle effects
 			for(PooledEffect effect : effects)
 			{
 				effect.draw(spriteBatch, Gdx.graphics.getDeltaTime());
 				if(effect.isComplete()){
 					effects.removeValue(effect, true);
 					effect.free();
 				}
 			}
 			
 			behindShip = world.getBob().getBody().getWorldPoint(new Vector2(-0.3f,0));
 			p[activeAfterburner].setPosition(behindShip.x, behindShip.y);
 			p[activeAfterburner].update(Gdx.graphics.getDeltaTime());
 			p[activeAfterburner].draw(spriteBatch, Gdx.graphics.getDeltaTime());
 		spriteBatch.end();
 		//debugRenderer.render(world.getWorld(), cam.combined);
 		//Gdx.app.log("Stats", "active: " + effects.size + " | max: " + pool.max);
 		
 		world.world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 	}
 	
 	public void draw(SpriteBatch batch, float parentAlpha){
 		//prototype.draw(batch);
 		p[activeAfterburner].draw(batch);
 	}
 	
 	public void moveCamera(float x,float y, float speed){
 		if(this.currentSpeed != speed){
 			this.sourceBgColor = this.currentBgColor;
			this.targetBgColor = this.getColor((int)speed);
			this.changeBackgroundColor = true;
			this.changeAfterBurnerColor((int)speed);
 		}
 		
 		this.currentSpeed = speed;
 		
 		//Cap camera at the top and bottom
 		if(y + 4 > 10) { cameraY = 6f; }
 		else if(y - 3 < 0) { cameraY = 3f; }
 		else { cameraY = y; }
 		
 		//Cap camera at the sides
 //		if(x + 5 > world.level.getWidth()){cameraX = world.level.getWidth() - 5;}
 //		else if(x - 5 < 0) {cameraX = 5;}
 //		else{cameraX = x;}
 		
 		//Move camera with speed
 		cameraX += Gdx.graphics.getDeltaTime() * currentSpeed;
 		
 		//Gdx.app.log("Camera", "X:" + cameraX + "," + x + " Y:" + cameraY + "," + y);
 		
         cam.position.set(cameraX, cameraY, 0);
         cam.update();
 	}
 	
 	private Color getColor(int speed){
 		if(speed == 3){
 			return new Color(0.5f, 0f, 0f, 1f);
 		} else if(speed == 2) {
 			return new Color(0.5f, 0.25f, 0f, 1f);
 		} else{
 			return new Color(0f, 0f, 0.5f, 1f);
 		}
 	}
 	
 	private void changeBackgroundColor()
 	{
 	    if(this.step >= this.maxtime){
 	    	this.step = 0;
 	    	this.changeBackgroundColor = false;
 	        return;
 	    }
 	    
 	    this.step += Gdx.graphics.getDeltaTime();
 	    float percentComplete = this.step / this.maxtime;
 	    float percentGone = 1 - percentComplete;
 	    float red = this.sourceBgColor.r * percentGone + this.targetBgColor.r * percentComplete;
 	    float green = this.sourceBgColor.g * percentGone + this.targetBgColor.g * percentComplete;
 	    float blue = this.sourceBgColor.b * percentGone + this.targetBgColor.b * percentComplete;
 	    float alpha = this.sourceBgColor.a * percentGone + this.targetBgColor.a * percentComplete;
 	    
 	    this.currentBgColor = new Color(red, green, blue, alpha);
 	}
 	
 	public OrthographicCamera getCam() {
 		return cam;
 	}
 }

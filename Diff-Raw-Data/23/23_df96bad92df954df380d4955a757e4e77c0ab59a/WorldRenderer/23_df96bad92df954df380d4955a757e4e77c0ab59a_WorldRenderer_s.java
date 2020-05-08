 package com.mass;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
 import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.RayCastCallback;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.WorldManifold;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.List;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.utils.Array;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 import com.mass.Weapon.WeaponType;
 
 public class WorldRenderer {
 	Box2DDebugRenderer renderer;
 	public static float CAMERA_WIDTH = 10f;
 	public static  float CAMERA_HEIGHT = 15f;
 	public float ppuX;	// pixels per unit on the X axis
 	public float ppuY;	// pixels per unit on the Y axis
 	public float zoom;
 	
 	public float camera_angle = 0;
 	public float setted_angle = 0;
 	//private SpriteBatch spriteBatch;
 	
 	private SpriteBatch spriteBatch;
 	private Pixmap spriteB;
 	Texture texture;
 	public  Map<String, TextureRegion> textureRegions;// = new HashMap<String, TextureRegion>();
 	public  Map<String, TextureRegion> textureRegions2;// = new HashMap<String, TextureRegion>();
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Sprite sprite;
 	
 	public float currentRotationWorld;
 	
 	public Player player;
 	
 	
 	Array<Planet> planets;
 	Array<Asteroid> asteroids;
 	
 	Vector2 planetPosition;
 	
 	Button imgButton;
 	Button img2Button;
 	Slider slider;
 	Image arrow;
 	
 	private static final float RUNNING_FRAME_DURATION = 0.06f;
 	
 	NewWorld world;
 	
 	Body cutBody;
 	
 	Skin skin;
 	Stage stage;
 	
 	ImmediateModeRenderer10 ray;
 	
 	/** a hit body **/
 	Body hitBody = null;
 	Body chainBody = null;
 	/** our ground box **/
 	Body groundBody;
 	
 	private OrthographicCamera cam;
 	private Box2DDebugRenderer  debugRenderer;
     static final float BOX_STEP=1/60f;  
     static final int BOX_VELOCITY_ITERATIONS=2;  
     static final int BOX_POSITION_ITERATIONS=3; 
     
     static final int screenWidth = Gdx.graphics.getWidth();
     static final int screenHeight = Gdx.graphics.getHeight();
     
     TextureRegion image;
     
     private Table container;
     private int ANGLE = 0;
     //FixtureAtlas fixtureAtlas;
 	private int width, height;
 	
 	Body firstBody = null;
 	Body secondBody = null;
 	
 	BitmapFont font;
 	
 	public WorldRenderer(final NewWorld world, float w, float h, boolean debug) {
 		renderer = new Box2DDebugRenderer();
 		font = new BitmapFont();
 		this.world = world;
 		CAMERA_WIDTH = w;
 		CAMERA_HEIGHT = h;
 		
 		image = new TextureRegion(new Texture(Gdx.files.internal("data/planet_01.png")));
 		
 		spriteBatch = new SpriteBatch();
 		 textureRegions = new HashMap<String, TextureRegion>();
 		//loadTextures();
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.viewportHeight = 8f;  
 		this.cam.viewportWidth = 12f;  
 		
 		zoom = this.cam.zoom = 15;
 		ray=new ImmediateModeRenderer10();
 		
 		//Texture img2 = new Texture(Gdx.files.internal("data/arr.png"));
 		
 		//arrow = new Image(img2);
 		
 	}
 	
 	public void resize (int width, int height) {
 		
 		ppuX = (float)width / CAMERA_WIDTH;
 		ppuY = (float)height / CAMERA_HEIGHT;
         //stage.setViewport(width, height, false);
 	}
 	
 	public Skin returnSkin(){
 		return skin;
 	}
 	
 	public Slider returnSlider(){
 		return slider;
 	}
 	
 	public void SetScreenSize(float delta) {
 		zoom = delta/10;
 		//if (zoom > this.cam.zoom) {
 		for (int i = 0; i<zoom; i++) {
 			this.cam.zoom = i;
 			this.cam.update();
 		}
 	}
 	
 	public void SetBodyAngle(Body body, float angle) {
 		body.setAngularVelocity(0);
 		body.setTransform(body.getPosition(), angle);
 	}
 	
 	public void SetCamera(float x, float y, float f){
 		camera_angle = (float) ((float) world.getPlayer().getAngle() - 90);
 		float substrat = camera_angle - setted_angle;
 		setted_angle = camera_angle;
 		currentRotationWorld = substrat;
 		
 		this.cam.position.set(x, y, 0);
 		if (substrat != 0) {
 			this.cam.rotate(substrat, 0, 0, 1);
 		}
 		this.cam.normalizeUp();
 		this.cam.update();
 	}  
 	
 	public OrthographicCamera getCamera() {
 		return this.cam;
 	}
 	
 	public void dispose(){
 		stage.dispose();
 		world.dispose();
 		spriteBatch.dispose();
 	}
 	
 	
 	RayCastCallback laserFired = new RayCastCallback() {
 		
 		@Override
 		public float reportRayFixture(Fixture fixture, Vector2 point,
 				Vector2 normal, float fraction) {
 			
 			
 		  Vector2 vec2 = new  Vector2( (float)Math.sin(setted_angle* MathUtils.degreesToRadians)*10, (float)Math.cos(setted_angle* MathUtils.degreesToRadians)*10);
 		  Vector2 p2 = new Vector2(point.x , point.y);
 		  
 			return 0;
 		}
    };
 	
    Vector3 arrowPoint = new Vector3();
    
 	public void render(float delta) {
 		//BitmapFont font = new BitmapFont();
 		renderer.render(world.getWorld(), cam.combined);
 		//renderer.render(world, camera.combined);
 		Vector2 point1 = world.getPlayer().getBody().getPosition();
 		
 		ray.begin(GL10.GL_LINE_LOOP);
 		planets = world.getPlanets();
 		if (planets.size > 1) {
 			for(Planet planet : planets) {
 				Vector2 point2 = planet.getBody().getPosition();
 				ray.color(1, 0, 0, 1);
 				ray.vertex(point1.x,point1.y,0);
 				ray.color(0, 1, 0, 1);
 				ray.vertex(point2.x, point2.y, 0);
 			}
 		}
 		ray.end();
 
 		drawAll();  
 		
 		changeAngle();
 		
 		changeEnergy();
 		
 		// make WORLD step
 		world.getWorld().step(delta, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
 		world.getWorld().clearForces();
 		//connectBodies();
 		//world.killBodies();
 		world.reloadMap(world.getPlayer().getBody().getPosition());
 		
 		SetCamera(world.getPlayer().getBody().getPosition().x, world.getPlayer().getBody().getPosition().y, 0);
 		
 	}
 	
 	private void drawAll() {
 		//spriteBatch.setProjectionMatrix(cam.combined);
 		spriteBatch.begin();
 		font.draw(spriteBatch, "Ship position x : "+(int)world.getPlayer().getBody().getPosition().x+"  y : "+(int)world.getPlayer().getBody().getPosition().y, 20, 60);
 		font.draw(spriteBatch, "Energy : " + (int)world.getPlayer().getEnergy(), 20, 20);
 		//drawPlayer();
 		drawPlanet();
 		drawTrash();
 		
 		spriteBatch.end();
 	}
 	
 	private void changeEnergy() {
 		// set ship ENERGY
 		int player_energy = (int) world.getPlayer().getEnergy();
 		if (player_energy < world.getPlayer().MAX_ENERGY) {
 			world.getPlayer().setEnergy(world.getPlayer().energy_dump);
 		}
 	}
 	
 	private void changeAngle() {
 		Body player = world.getPlayer().getBody();
 		double accelY = (float) Gdx.input.getAccelerometerY();
 		player.applyAngularImpulse(0);
 		player.setAngularVelocity(0);
 		
 		float angleNor = (float) ((double) (-accelY) * 2.2500223);
 		int angleVelo = (int) angleNor;
 		if (angleNor < 0) {
 			angleVelo = (int) -angleNor;
 		}
 		
 		if (angleVelo > 2) {
 			player.applyForce(-angleNor * (world.getPlayer().getCurrentVelocity() / 2), angleNor * world.getPlayer().getCurrentVelocity() * 2, player.getPosition().x, player.getPosition().y);
 			player.applyAngularImpulse(angleNor);
 		}
 	}
 	
 	Vector3 testPoint = new Vector3();
 	Array<Body> founded = new Array<Body>();
 	QueryCallback callback = new QueryCallback() {
 		@Override
 		public boolean reportFixture (Fixture fixture) {
 			
 			//if (fixture.testPoint(testPoint.x, testPoint.y)) {
 				
 				Object bodyUserData = fixture.getBody().getUserData();
 				
 				if (bodyUserData == "asteroid") {
 					hitBody = fixture.getBody();
 					
 					
 
 					Vector2 planetDistance = new Vector2(0,0);
 			        planetDistance.add(hitBody.getPosition());
 					planetDistance.sub(world.getPlayer().getBody().getPosition());
 					float finalDistance = planetDistance.len();
 					planetDistance.x = -planetDistance.x;
 					planetDistance.y = -planetDistance.y;
 					
 			        float vecSum = Math.abs(planetDistance.x)+Math.abs(planetDistance.y);
 			        planetDistance.mul((1/vecSum)*1000f/finalDistance);
 			        hitBody.applyForce(planetDistance, hitBody.getWorldCenter());
 					
 					hitBody.setActive(true);
 				}
 				return false;
 			//} else
 			//	return true;
 		}
 	};
 	
 	public void chainWithIP() {
 		Array<Body> chains = world.getChainsBodies();
 		if (chains.size > 0) {
 			for (Body chain : chains) {
 				chainBody = chain;
 				testPoint.set(chain.getPosition().x, chain.getPosition().y, 0);
 				getCamera().unproject(testPoint);
 				
 				
 				//world.getWorld().QueryAABB(callback, testPoint.x - 30, testPoint.y - 30, testPoint.x + 30, testPoint.y + 30);
 				
 				
 				for (Body found : founded) {
 					Body body = found;
 				      Vector2 bodyCom = body.getWorldCenter();
 				      
 				      //ignore bodies outside the blast range
 				      //if ( bodyCom.len() >= 11 )
 				      //    continue;
 				          
 				      //applyBlastImpulse(body, chain.getPosition(), bodyCom, 10 );
 				      
 						ray.begin(GL10.GL_LINE_LOOP);
 						ray.color(1, 0, 0, 1);
 						ray.vertex(bodyCom.x,bodyCom.y,0);
 						ray.end();
 				  }
 				
 				ray.dispose();
 			}
 		}
 	}
 	
 	 void applyBlastImpulse(Body body, Vector2 blastCenter, Vector2 applyPoint, float blastPower) {
 		  Vector2 blastDir = new Vector2 (applyPoint.x - blastCenter.x, applyPoint.y - blastCenter.y);
 	      float distance = blastCenter.dst(applyPoint);
 	      //ignore bodies exactly at the blast point - blast direction is undefined
 	      if ( distance == 0 )
 	          return;
 	      float invDistance = 1 / distance;
 	      float impulseMag = blastPower * invDistance * invDistance;
 	      Vector2 newImpulse = new Vector2(impulseMag * blastDir.x, impulseMag * blastDir.y);
 	      body.applyLinearImpulse( newImpulse, applyPoint );
 	  }
 	
 	public void connectBodies() {
 		Array<Body> binded = world.getBindedBodies();
 		Array<Body> connected = world.getConnectedBodies();
 		if (connected.size > 0) {
 			
 			firstBody = connected.first();
 			binded.add(firstBody);
 			secondBody = connected.peek();
 			binded.add(secondBody);
 			connected.clear();
 	        
 			if (firstBody != null && secondBody != null) {
 				firstBody.setBullet(false);
 				Vector2 worldpoint = new Vector2(0,0);
 				Vector2 worldCoordsAnchorPoint = firstBody.getWorldPoint( worldpoint );
 				WeldJointDef jointDef = new WeldJointDef();
 				jointDef.initialize(firstBody, secondBody, worldCoordsAnchorPoint);
 		        world.getWorld().createJoint(jointDef);
 		        firstBody = null;
 		        secondBody = null;
 			}
 	        
 		}
 	}
 	
 	private void loadTextures() {
 		
 		texture  = new Texture(Gdx.files.internal("images/atlas.png"));
 		TextureRegion tmpLeftRight[][] = TextureRegion.split(texture, texture.getWidth()/ 2, texture.getHeight()/2 );
 		TextureRegion left2[][] = tmpLeftRight[0][0].split(tmpLeftRight[0][0].getRegionWidth()/2, tmpLeftRight[0][0].getRegionHeight());
 		TextureRegion left[][] = left2[0][0].split(left2[0][0].getRegionWidth()/4, left2[0][0].getRegionHeight()/8);
 		
 		textureRegions.put("player",  left[0][0]);
 		textureRegions.put("brick1",  left[0][1]);
 		textureRegions.put("brick2",  left[1][0]);
 		textureRegions.put("brick3",  left[1][1]);
 		
 		
 		textureRegions.put("navigation-arrows", tmpLeftRight[0][1]);
 		
 		TextureRegion rightbot[][] = tmpLeftRight[1][1].split(tmpLeftRight[1][1].getRegionWidth()/2,tmpLeftRight[1][1].getRegionHeight()/2);
 		
 		textureRegions.put("khob",   rightbot[0][1]); 
 		
 	}
 	
 	/**
 	 *  .
 	 */
 	private void drawPlayer(){
 		//spriteBatch.draw(textureRegions.get("player"), (world.getPlayer().getPosition().x-Player.SIZE/2)*ppuX,(world.getPlayer().getPosition().y-Player.SIZE/2)*ppuY, Player.SIZE*ppuX,Player.SIZE*ppuY );
 	    Vector2 bottlePos = world.getPlayer().getBody().getPosition();
 	    TextureRegion image2 = new TextureRegion(new Texture(Gdx.files.internal("data/shipIstr.png")));
 	    
 	    Sprite bottleSprite = new Sprite(image2);
 	    bottleSprite.setSize(27, 27);
 	    bottleSprite.setPosition(screenWidth/2 - 10, screenHeight/2);
 	    bottleSprite.setOrigin(world.getShipOrigin().x, world.getShipOrigin().y);
 	    bottleSprite.rotate90(false);
 	    bottleSprite.draw(spriteBatch);
 	    
 	}
 	
 	private void drawPlanet(){
 	    Vector3 drawPosition = new Vector3();
 	    float angleDeg = world.getPlayer().getBody().getAngle()* MathUtils.radiansToDegrees;
 	    
 		planets = world.getPlanets();
 		if (planets.size > 1) {
 			for(Planet planet : planets) {
 				float angle = planet.getBody().getAngle()* MathUtils.radiansToDegrees;
 				planetPosition = planet.getBody().getPosition();
 				drawPosition.set(planetPosition.x, planetPosition.y, 0);
 			    this.cam.project(drawPosition);
 			    float radius = planet.radius;
 			    sprite = new Sprite(image);
 			    //spriteBatch.draw(image, drawPosition.x-(radius*3), drawPosition.y-(radius*3), radius*6, radius*6);
 			    sprite.setSize(radius*6, radius*6);
			    //sprite.setOrigin(0, 0);
 			    //sprite.setOrigin(drawPosition.x-180, drawPosition.y-100);
 			    sprite.setPosition(drawPosition.x-(radius*3), drawPosition.y-(radius*3));
				sprite.setRotation(angle);
 				sprite.draw(spriteBatch);
 				
 			}
 		}
 	}
 	
 	public void drawTrash(){
 	    Vector3 drawPosition = new Vector3();
 	    float angleDeg = world.getPlayer().getBody().getAngle()* MathUtils.radiansToDegrees;
 	    Sprite sprite;
 		asteroids = world.getAsteroids();
 		if (asteroids.size > 1) {
 			
 			for(Asteroid asteroid : asteroids) {
 				
				float angle = asteroid.getBody().getAngle()* MathUtils.radiansToDegrees;
				planetPosition = asteroid.getBody().getPosition();
				drawPosition.set(planetPosition.x, planetPosition.y, 0);
 			    this.cam.project(drawPosition);
 			    float radius = 3;
 			    sprite = asteroid.region;
 			    sprite.setSize(radius*6, radius*6);
			    //sprite.setOrigin(0, 0);
 			    sprite.setPosition(drawPosition.x-(radius*3), drawPosition.y-(radius*3));
				//sprite.setRotation(angle);
 				sprite.draw(spriteBatch);
 				
 			}
 		}
 	}
 }

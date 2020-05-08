 package com.climber;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 public class Game implements ApplicationListener {
 	private Random random;
 
 	private OrthographicCamera camera;
 
 	private Texture mt_character;
 	private TextureRegion mtr_character;
 	private Texture mt_ground;
 	private Texture mt_Block;
 	private Texture mt_JoystickBackground;
 	private Texture mt_JoystickArrows;
 	private Texture mt_JumpButton;
 	private Texture mt_Lava;
 	private Texture mt_DeathScreen;
 	private SpriteBatch m_batch;
 
 	private Sprite ms_Character;
 	private Sprite ms_Ground;
 	private Sprite ms_JoystickBackground;
 	private Sprite ms_JoystickArrows;
 	private Sprite ms_JumpButton;
 	private Sprite ms_Lava;
 
 	private ArrayList<Sprite> mal_FallingBlocks;
 	
 	private BitmapFont font;
 
 	private static int WIDTH = 15;
 	private static int HEIGHT = 10;
 	
 	private Vector2 SCREEN_RESOLUTION;
 
 	private static final float CAMERA_SPEED = 0.05f;
 	private static final float CAMERA_SPEED_FALLING = 0.1f;
 
 	private long ml_lastBlockCreation; // the time the last block was created
 	private long ml_timeBetweenBlockCreations; // how long in between blocks to
 												// wait before creating a new
 												// one
 	
 	private boolean mb_isTouchingRightArrow = false;
 	private boolean mb_isTouchingLeftArrow = false;
 	
 	private float LAVA_SPEED = 0.8f;
 	
 	private Quadtree m_quadTree;
 	private ArrayList<Object> m_listOfAllBricks;
 	
 	private boolean m_noClipCam = true;
 	float CAM_SPEED = 0.5f;
 	
 	/** the immediate mode renderer to output our debug drawings **/
 	private ImmediateModeRenderer10 renderer;
 	private int mi_NumberOfQuads;
 	private int mi_NumberOfBricks;
 	private boolean mb_DebugMode = true;
 	private boolean mb_DrawQuads = false;
 	
 	private boolean mb_PlayerIsKilled;
 	
 	private ParticleEngine m_particles;	
 	
 	private boolean mb_GameOver;
 	
 	private ScoreManager m_scoreManager;
 
 	private FileHandle m_highScoreFile;
 
 	@Override
 	public void create() {
 		//create all of the textures
 		mt_character = new Texture(Gdx.files.internal("character.png"));
 		mtr_character = new TextureRegion(mt_character, 0, 0, 100, 150);
 
 		mt_ground = new Texture(Gdx.files.internal("ground.png"));
 		m_batch = new SpriteBatch();
 
 		mt_Block = new Texture(Gdx.files.internal("greenblock.png"));
 		
 		mt_JoystickArrows = new Texture(Gdx.files.internal("joystick-arrows.png"));
 		mt_JoystickBackground = new Texture(Gdx.files.internal("joystick-background.png"));
 		mt_JumpButton = new Texture(Gdx.files.internal("jump-button.png"));
 		
 		mt_DeathScreen = new Texture(Gdx.files.internal("deathscreen.png"));
 		
 		mt_Lava = new Texture(Gdx.files.internal("lava.png"));
 		
 		ms_JoystickBackground = new Sprite(1, 1, 2.8f, 2.8f); //the x and y don't matter because it is the hud which is based off of the camera's x and y
 		ms_JoystickArrows = new Sprite(1,1, 2.5f, 2.5f);
 		ms_JumpButton = new Sprite(1,1, 2.5f, 2.5f);
 
 		ms_Ground = new Sprite(0, -4, 15, 5);
 
 		mal_FallingBlocks = new ArrayList<Sprite>();
 		
 		// Microsoft's CornFlowerBlue color
 		Gdx.gl10.glClearColor(0.4f, 0.6f, 0.9f, 1);
 
 		// create the random number generator
 		random = new Random();
 
 		// create a camera
 		camera = new OrthographicCamera(WIDTH, HEIGHT);
 		
 		//get the resolution
 		SCREEN_RESOLUTION = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		font = new BitmapFont();
         font.setColor(Color.RED);
 
         renderer = new ImmediateModeRenderer10();
         
         m_scoreManager = new ScoreManager(5);
         
         m_highScoreFile = Gdx.files.external("data/highscores.xml");
  
         StartNewGame();
 	}
 	
 	private float convertYToFeet(float yMiddle) {
 		float yToFeetConversionFactor = 3.0f;
 		return yMiddle * yToFeetConversionFactor;
 	}
 
 	private void StartNewGame() {
 		camera.position.set((float) (WIDTH / 2.0f), HEIGHT / 2, 0);
 		
 		ms_Character = new Sprite(5, 2, 1, 1.5f, true);
 		ms_Lava = new Sprite(0, -20, 15, 15);
 		
 		ml_lastBlockCreation = System.currentTimeMillis();
 		ml_timeBetweenBlockCreations = 1000; // 5 seconds in milliseconds
 		
         m_quadTree = new Quadtree(new Rectangle(-2.5f, 0, WIDTH+5, 100));
         mi_NumberOfBricks = 0;
         mi_NumberOfQuads = 0;
         
         Texture t = new Texture(Gdx.files.internal("particle.png"));
         m_particles = new ParticleEngine(t);
         
     	mb_PlayerIsKilled = false;
     	mb_GameOver = false;
     	
     	mal_FallingBlocks = new ArrayList<Sprite>();
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void render() {
 		GL10 gl = Gdx.graphics.getGL10();
 
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 		m_batch.setProjectionMatrix(camera.combined);
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		
 		mb_GameOver = m_particles.isDone();
 		
 		draw();
 		
 		if (!mb_PlayerIsKilled) {
 			handleInputAlive();
 		} else {
 			handleInputDead();
 		}
 		
 		simulate();
 			
 	}
 
 	private void draw() {
 		
 		if (!mb_GameOver) {
 			m_batch.begin();
 
 			m_listOfAllBricks = null;
 			m_listOfAllBricks = m_quadTree.getListOfAll();
 			mi_NumberOfBricks = m_listOfAllBricks.size();
 			//System.out.println("Current amount of blocks on screen: " + m_listOfAllBricks.size());
 			for (int i = 0; i < m_listOfAllBricks.size(); i++) {
 				Sprite currentBlock = (Sprite)m_listOfAllBricks.get(i);
 
 				m_batch.draw(mt_Block, currentBlock.getM_x(),
 						currentBlock.getM_y(), currentBlock.getM_width(),
 						currentBlock.getM_height());
 			}
 
 			//draw the ground
 			m_batch.draw(mt_ground, ms_Ground.getM_x(), ms_Ground.getM_y(),
 					ms_Ground.getM_width(), ms_Ground.getM_height());
 		
 			//draw the lava
 			m_batch.draw(mt_Lava, ms_Lava.getM_x(), ms_Lava.getM_y(), ms_Lava.getM_width(), ms_Lava.getM_height());
 
 			// draw the character
 			m_batch.setColor(1.0f, 1.0f, 1.0f, ms_Character.getAlpha());
 			m_batch.draw(mtr_character, ms_Character.getM_x(),
 					ms_Character.getM_y(), ms_Character.getM_width(),
 					ms_Character.getM_height());
 			m_batch.setColor(Color.WHITE);
 		
 			//draw the particles that appear during the characters death
 			m_particles.draw(m_batch);
 
 		
 			//draw the on screen controls
 			m_batch.draw(mt_JoystickBackground, camera.position.x - 7.16f,
 					camera.position.y - 4.65f, ms_JoystickBackground.getM_width(),
 					ms_JoystickBackground.getM_height());
 			m_batch.draw(mt_JoystickArrows, camera.position.x - 7,
 					camera.position.y - 4.5f, ms_JoystickArrows.getM_width(),
 					ms_JoystickArrows.getM_height());
 
 			m_batch.draw(mt_JumpButton, camera.position.x + 5,
 					camera.position.y - 4.5f, ms_JumpButton.getM_width(),
 					ms_JumpButton.getM_height());
 
 			m_batch.end();
 			
 			//draw the fps on the screen
 	        m_batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 	        m_batch.begin();
 	        font.draw(m_batch, "fps: " + Gdx.graphics.getFramesPerSecond() , 0, 20);
 	        font.draw(m_batch, "bricks: " + mi_NumberOfBricks, 0, 40);
 	        if ( mb_DrawQuads ) font.draw(m_batch, "quads: " + mi_NumberOfQuads, 0, 60);
 	        m_batch.end();
 	        
 	        //draw all of the quads
 	        if ( mb_DebugMode && mb_DrawQuads )
 	        	debugDrawQuads();
 		} else { //draw the death screen
 			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 	        m_batch.begin();
 			m_batch.draw(mt_DeathScreen, camera.position.x-3,
 					camera.position.y-3, 6, 6);
 	        m_batch.end();
 
 	        m_batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 	        m_batch.begin();
 	        String deathString = "You climbed " + convertYToFeet(ms_Character.getYMiddle()) + " feet!";
 	        
 	        font.draw(m_batch, deathString, 
 	        		(Gdx.graphics.getWidth() / 2.0f) - deathString.length(), Gdx.graphics.getHeight() / 1.5f);
 	        font.draw(m_batch, "High Score: " + m_scoreManager.getTopScore() + " feet.", 0, Gdx.graphics.getHeight() - 20);
 	        m_batch.end();
 		}
 
 
 	}
 	
 	private void debugDrawQuads() {
 		GL10 gl = Gdx.graphics.getGL10();
 		
 		ArrayList<Rectangle> r = m_quadTree.getAllQuads();
 		//System.out.println("Number of quads: " + r.size());
 		mi_NumberOfQuads = r.size();
 		
 		gl.glLineWidth(2.0f);
 		renderer.begin(GL10.GL_LINES);
 		for ( int i = 0; i < r.size(); i++ ) {
 			Rectangle t = r.get(i);
 			Vector3 bl = new Vector3((t.x)/WIDTH, (t.y - camera.position.y)/HEIGHT, 0);
 			Vector3 br = new Vector3(((t.x+t.width))/WIDTH, (t.y - camera.position.y)/HEIGHT, 0);
 			Vector3 tl = new Vector3((t.x)/WIDTH, ((t.y+t.height) - camera.position.y)/HEIGHT, 0);
 			Vector3 tr = new Vector3(((t.x+t.width))/WIDTH, ((t.y+t.height) - camera.position.y)/HEIGHT, 0);
 
 			bl.x *= SCREEN_RESOLUTION.x;
 			br.x *= SCREEN_RESOLUTION.x;
 			tr.x *= SCREEN_RESOLUTION.x;
 			tl.x *= SCREEN_RESOLUTION.x;
 			
 			bl.y *= SCREEN_RESOLUTION.y;
 			br.y *= SCREEN_RESOLUTION.y;
 			tr.y *= SCREEN_RESOLUTION.y;
 			tl.y *= SCREEN_RESOLUTION.y;
 			
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(bl);
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(br);
 
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(br);
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(tr);
 
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(tr);
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(tl);
 
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(tl);
 			renderer.color(1, 0, 0, 1);
 			renderer.vertex(bl);
 		}
 		renderer.end();
 	}
 
 	private void handleInputDead() {
 
 		Vector3 input = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
 		camera.unproject(input);
 		
 		Rectangle r = new Rectangle(-1.2f, 0.45f, 2.2f, 1.42f );
 		
 		if ((Gdx.input.justTouched() && r.contains(camera.position.x - input.x, camera.position.y - input.y))
 				|| Gdx.input.isKeyPressed(Keys.ENTER)) {
 			StartNewGame();
 		}
 	}
 	
 	private void handleInputAlive() {
 		/* Touch screen controls */
				
 		/* Multitouch */
 		for ( int i = 0; i < 3; i++ ) {
 			if (!Gdx.input.isTouched(i))
 				continue;
 			
 			if (!mb_DebugMode || (Gdx.app.getType().toString().equals("Android")))
 				m_noClipCam = false;
 
 			Vector3 input = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
 			camera.unproject(input);
 			//System.out.println("X: " + (camera.position.x - input.x) + " - Y: " + (camera.position.y - input.y));
 			
 			//check if the jump button is pressed
 			if ( (camera.position.x - input.x) < -5 && (camera.position.x - input.x) > -7 &&(camera.position.y - input.y > 2) && (camera.position.y - input.y) < 4)
				ms_Character.jump();
 			
 			//check if the joystick is pressed
 			float screenX = camera.position.x - input.x;
 			float screenY = camera.position.y - input.y;
 			if ( screenX > 4.5f && screenX < 5.5f && screenY > 2.5f && screenY < 4.0f) {
 				if (!mb_isTouchingRightArrow)
 					mb_isTouchingRightArrow = true;
 			}
 			if ( mb_isTouchingRightArrow && screenX > 5.5f)
 				mb_isTouchingRightArrow = false;
 			
 			if ( screenX > 6.0f && screenY > 2.0f && screenY < 4.0f) {
 				if (!mb_isTouchingLeftArrow)
 					mb_isTouchingLeftArrow = true;
 			}
 			if ( mb_isTouchingLeftArrow && screenX < 6.0f)
 				mb_isTouchingLeftArrow = false;
 		}
 		
 		if ( Gdx.input.justTouched()) {
 			
 			//add a block when you touch the screen in debug mode
 			if (mb_DebugMode) {
 				Vector3 input = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
 				camera.unproject(input);
 				//addBlock();
 			}
 		}
 		
 		if ( mb_isTouchingRightArrow) {
 			ms_Character.moveX(Gdx.graphics.getDeltaTime());
 
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) > WIDTH)
 				ms_Character.setM_x(0);
 		}
 		
 		if (mb_isTouchingLeftArrow) {
 			ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 
 			 // if the middle of the character goes off the screen to the left, put him on the right
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) < 0)
 				ms_Character.setM_x(WIDTH);
 		}
 		
 		if ( !Gdx.input.isTouched()) {
 			if ( mb_isTouchingRightArrow)
 				mb_isTouchingRightArrow = false;
 			if ( mb_isTouchingLeftArrow)
 				mb_isTouchingLeftArrow = false;
 			
 		}
 		
 		
 		/* Keyboard controls */
 		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) {
 			m_noClipCam = false;
 			ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 
 			 // if the middle of the character goes off the screen to the left, put him on the right
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) < 0)
 				ms_Character.setM_x(WIDTH);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) {
 			m_noClipCam = false;
 			ms_Character.moveX(Gdx.graphics.getDeltaTime());
 
 			if (ms_Character.getM_x() + (ms_Character.getM_width() / 2) > WIDTH)
 				ms_Character.setM_x(0);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_UP)
 				|| Gdx.input.isKeyPressed(Keys.SPACE)) {
 			m_noClipCam = false;
 			ms_Character.jump();
 		}
 	
 		
 		//no clip cam
 		if (Gdx.input.isKeyPressed(Keys.W)){
 			m_noClipCam = true;
 			camera.position.y += CAM_SPEED;
 		}
 		
 		if (Gdx.input.isKeyPressed(Keys.S)){
 			m_noClipCam = true;
 			camera.position.y -= CAM_SPEED;
 		}
 
 	}
 
 	private void simulate() {
 		if ( !mb_PlayerIsKilled) {
 			simulateCharacter();
 			simulateCamera();
 	        checkForNewBlocks();
	        simulateLava();
 			m_quadTree.simulate(ms_Ground);
 		}
 		
 		if ( ms_Character.isMb_isDying())
 			ms_Character.fadeOut();
 		m_particles.simulate();
 	}
 	
 	private void simulateLava() {
 		ms_Lava.moveY(Gdx.graphics.getDeltaTime(), LAVA_SPEED);
 	}
 
 	private void simulateCamera() {
 		if (m_noClipCam) {
 			return;
 		}
 		
 		boolean panUp = false;
 		boolean panDown = false;
 
 		float charY = ms_Character.getM_y();
 		if (camera.position.y > charY) {
 			if (camera.position.y - charY > 2)
 				panDown = true;
 			else
 				panDown = false;
 		}
 
 		if (camera.position.y < charY) {
 			if (charY - camera.position.y > 1)
 				panUp = true;
 			else
 				panUp = false;
 		}
 
 		if (panUp)
 			camera.position.y += CAMERA_SPEED;
 
 		if (panDown)
 			camera.position.y -= ms_Character.isMb_isFalling() ? CAMERA_SPEED_FALLING
 					: CAMERA_SPEED;
 	}
 	
 	private void simulateCharacter() {
 		ms_Character.simulate();
 		
 		ArrayList<Object> blocks = m_quadTree.query(ms_Character.getRectangle());
 		
 		boolean left = false;
 		boolean right = false;
 		boolean top = false;
 		boolean bottom = false;
 		
 		for ( int i = 0; i < blocks.size(); i++ ) {
 			Sprite current = (Sprite)blocks.get(i);
 			
 			if ( current.getRectangle().overlaps(ms_Character.getRectangle())) {
 				Collision c = new Collision(ms_Character.getRectangle(), current.getRectangle());
 				
 				if ( c.isCollidingBottom() ) {
 					bottom = true;
 				} else if ( c.isCollidingTop() ) {
 					top = true;
 				} else if ( c.isCollidingLeft() ) {
 					left = true;
 				} else if ( c.isCollidingRight() ) {
 					right = true;
 				}
 			}
 		}
 		
 		// check if the character landed on the ground
 		if (ms_Character.getM_y() < ms_Ground.getM_y()
 				+ ms_Ground.getM_height()) {
 			bottom = true;
 		}
 		
 		if ( bottom ) {
 			ms_Character.setFalling(false);
 			ms_Character.setSliding(false);
 		} else {
 			if (!ms_Character.isSliding() && ! ms_Character.isMb_isJumping())
 				ms_Character.setFalling(true);
 		}
 		
 		if ( top ) {
 			if ( ms_Character.isMb_isJumping()) {
 				ms_Character.interruptJump();
 			} else if ( !ms_Character.isMb_isFalling()){
 				KillPlayer();
 			}
 		}
 		
 		if ( left ) {
 			ms_Character.moveX(Gdx.graphics.getDeltaTime());
 			if ( ms_Character.isMb_isJumping() || ms_Character.isMb_isFalling()) {
 				//start sliding
 				ms_Character.startSliding();
 				ms_Character.setSlidingLeft(true);
 			}
 		}
 		
 		if ( right ) {
 			ms_Character.moveX(-Gdx.graphics.getDeltaTime());
 			if ( ms_Character.isMb_isJumping() || ms_Character.isMb_isFalling()) {
 				//start sliding
 				ms_Character.startSliding();
 				ms_Character.setSlidingRight(true);
 			}
 		}
 		
 		if ( !right && !left && ms_Character.isSliding()) {
 			ms_Character.setSliding(false);
 			ms_Character.setFalling(true);
 		}
 	}
 	
 	private void KillPlayer() {
 		mb_PlayerIsKilled = true;
 		Rectangle temp = ms_Character.getRectangle();
 		m_particles.start(temp.x + (temp.width/2.0f), temp.y + (temp.height/2.0f), 100);
 		ms_Character.setMb_isDying(true);
 		m_scoreManager.addScore(convertYToFeet(ms_Character.getYMiddle()));
 	}
 
 	private void checkForNewBlocks() {
 		long randomOffset = (random.nextLong() % 1000) + 500; // create a
 																// random offset
 																// ranging
 																// between
 																// 1000-3000
 																// milliseconds
 		if (System.currentTimeMillis() - ml_lastBlockCreation > (ml_timeBetweenBlockCreations + randomOffset)) {
 			addBlock();
 			ml_lastBlockCreation = System.currentTimeMillis();
 			//System.out.println("Time to add a new block!");
 		}
 	}
 
 	private void addBlock() {
 
 		// randomize the x position of the block
 		float x = randomFloat(0, WIDTH);
 
 		// randomize the size of the block
 		float size = randomFloat(1, 3);
 		
 		//float y = mf_HighestBlock;
 
 		// create new block at a random X value, the top of the screen, and with
 		// the width and height of the block as 3.0f, and set it to be falling
 		Sprite block = new Sprite(x, getHighestBlockTop() /*+ (HEIGHT/2)*/ + 1/*input.y*/, size,
 				size, true);
 		block.setFallingSpeed(0.05f);
 
 		mal_FallingBlocks.add(block);
 		m_quadTree.insert(block);
 	}
 	
 	/**
 	 * Gets the position of the top of the highest block
 	 * @return
 	 */
 	private float getHighestBlockTop() {
 		float highest = 0.0f;
 		
 		for ( int i = 0; i < mal_FallingBlocks.size(); i++){
 			Sprite t = mal_FallingBlocks.get(i);
 			float yh = t.getM_y() + t.getM_height();
 			
 			if ( yh > highest)
 				highest = yh;
 		}
 		
 		return highest;
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private float randomFloat(float min, float max) {
 		return (float) (min + (Math.random() * (max - min)));
 	}
 
 }

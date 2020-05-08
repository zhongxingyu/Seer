 package com.me.mygdxgame;
 
 
 import com.me.mygdxgame.Block1.BlockType;
 import com.me.mygdxgame.Block1.FacingDir;
 
 import com.badlogic.gdx.Gdx; 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10; 
 import com.badlogic.gdx.graphics.OrthographicCamera; 
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer; 
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType; 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 //Renders blocks and actors into the world
 
 @SuppressWarnings("unused")
 public class WorldRenderer { 
 	
 	private static final float CAM_WIDTH = 1024f; 
 	private static final float CAM_HEIGHT = 720f; 
 	
 	private World world; 
 	private DirectionController controller;
 	private OrthographicCamera cam;	
 	ShapeRenderer debugRend = new ShapeRenderer();
 	
 	private Texture ballTexture; 
 	private Texture groundTexture; 
 	private Texture wallSouthTexture; 
 	private Texture wallWestTexture;
 	private Texture wallEastTexture;
 	private Texture wallNorthTexture;
 	private Texture cornerSouthTexture;
 	private Texture cornerNorthTexture;
 	private Texture cornerEastTexture;
 	private Texture cornerWestTexture;
 	private Texture holeTexture;
 	private Texture backgroundTexture; 
 	private Texture arrowTexture;
 	private SpriteBatch sprite; 
 	private Sprite trajectorySprite;
 	private Stage stage;
 	private direcLogic directLogic; 
 	
 	private Trajectory traject;
 	
 	private int width; 
 	private int height; 
 	private float ppuX; //pixels/unit on width
 	private float ppuY; //pixels/unit on height
 	private boolean debug = false; 
 	public boolean renderTrajectory = true;
 	
 	public void setSize(int w, int h) {
 		this.width = w; 
 		this.height = h;
 		ppuX = (float)width/CAM_WIDTH; 
 		ppuY = (float)height/CAM_HEIGHT;
 	}
 	
 	
 	public WorldRenderer (World world, boolean debug) { 
 		this.world = world; 
 		this.cam = new OrthographicCamera(1024,720); 
 		this.cam.position.set(512f, 360f, 0); 
 		this.cam.update(); 
 		this.debug = debug;
 		this.renderTrajectory = true;
 		sprite = new SpriteBatch(); 
 		loadTextures(); 
 	}
 	
 	public void render() {
		//if(renderTrajectory){
 			drawBallTrajectory();
 			directLogic.update();
		//}				
 		sprite.begin();
 		    sprite.draw(backgroundTexture, 0, 0);
 			drawGround();
 			drawWall();
 			drawCorners();
 			drawHole();
 			drawBall();
 			
 		sprite.end();
		stage.act(); 
		stage.draw();
 		if(debug) {
 			debug(); 
 		}		
 	}
 	
 	private void loadTextures() {
 		ballTexture = new Texture (Gdx.files.internal("images/ball.png"));
 		groundTexture = new Texture (Gdx.files.internal("images/grass.png"));
 		wallSouthTexture = new Texture (Gdx.files.internal("images/wall-north.png"));
 		wallNorthTexture = new Texture (Gdx.files.internal("images/wall-south.png"));
 		wallEastTexture = new Texture (Gdx.files.internal("images/wall-west.png"));
 		wallWestTexture = new Texture (Gdx.files.internal("images/wall-east.png"));
 		cornerWestTexture = new Texture (Gdx.files.internal("images/corner-w.png"));
 		cornerNorthTexture = new Texture (Gdx.files.internal("images/corner-n.png"));
 		cornerEastTexture = new Texture (Gdx.files.internal("images/corner-e.png"));
 		cornerSouthTexture = new Texture (Gdx.files.internal("images/corner-s.png"));
 		holeTexture = new Texture (Gdx.files.internal("images/hole.png"));
 		backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));		
 		arrowTexture = new Texture(Gdx.files.internal("images/circle.png"));
 		arrowTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 	}
 	private void drawBallTrajectory() {
 		trajectorySprite = new Sprite(arrowTexture);
 		controller = new DirectionController();
		Ball ball = world.getBall();
 		directLogic = new direcLogic(controller, ball.getPosition());
 		traject = new Trajectory(controller, trajectorySprite);
 		traject.setX(128f);
 		traject.setY(20 + ballTexture.getHeight() * 0.7f);
 		traject.setWidth(50f); 
 		traject.setHeight(50f);
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
 		stage.addActor(traject);
 	}
	private void drawBall() {	
 		Ball ball = world.getBall();
 		sprite.draw(ballTexture, ball.getPosition().x * ppuX, ball.getPosition().y * ppuY,
 				Ball.SIZE * ppuX, Ball.SIZE * ppuY);		
 	}
 	
 	private void drawGround(){
 		for(Block1 block : world.getGroundBlocks()){
 			sprite.draw(groundTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 					Block1.SIZE * ppuX, Block1.SIZE * ppuY);
 		}
 	}
 	
 	private void drawWall() {
 		for(Block1 block : world.getWallBlocks()){
 			//if(block.type == BlockType.Wall)
 			//change texture position based on it's FacingDir
 			if(block.dir == FacingDir.NORTH){ //draw blocks from getWallBlocks() that face north
 				sprite.draw(wallNorthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false); 
 				}
 				if(block.dir == FacingDir.SOUTH){
 					sprite.draw(wallSouthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 							Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);	
 				}
 				if(block.dir == FacingDir.EAST){
 					sprite.draw(wallEastTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 							Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);
 				}
 				if(block.dir == FacingDir.WEST){
 					sprite.draw(wallWestTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 							Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);
 				}
 			}
 		}
 	
 	private void drawCorners() {
 		for(Block1 block : world.getCornerBlocks()){
 			//change texture position based on FacingDir
 			if(block.dir == FacingDir.NORTH){ //draw blocks from getCornerBlocks() 
 				sprite.draw(cornerEastTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false); 
 			}
 			if(block.dir == FacingDir.SOUTH){
 				sprite.draw(cornerWestTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);	
 			}
 			if(block.dir == FacingDir.EAST){
 				sprite.draw(cornerNorthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);	
 			} 
 			if(block.dir == FacingDir.WEST){
 				sprite.draw(cornerSouthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false);	
 			}
 		}
 	}
 	
 	private void drawHole() {
 		for(Block1 block : world.getHoleBlock()) {
 			sprite.draw(holeTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 					Block1.SIZE * ppuX, Block1.SIZE * ppuY);
 		}
 	}
 	
 	public void setRenderTraject(boolean value){
 		this.renderTrajectory = value;
 	}
 	public boolean getRenderTraject(){
 		return this.renderTrajectory;
 	}
 	//if the for loop is changed to world.getCornerBlocks or getWallBlocks, it 
 	//will render the outline of where they are rendered
 	public void debug() { 
 		//render the outline of the blocks
 		debugRend.setProjectionMatrix(cam.combined); 
 		debugRend.begin(ShapeType.Rectangle); 
 		for(Block1 block : world.getWallBlocks()) {
 			Polygon poly = block.getBounds(); 
 			float x1 = poly.getX();
 			float y1 = poly.getY(); 
 			debugRend.setColor(new Color(1,0,0,1)); //red
 			debugRend.rect(x1, y1, Block1.SIZE, Block1.SIZE);
 		}
 		//render the outline of the ball
 		Ball ball = world.getBall(); 
 		Polygon poly = ball.getBounds(); 
 		float x1 = poly.getX(); 
 		float y1 = poly.getY(); 
 		debugRend.setColor(new Color(0,1,0,1)); //green
 		debugRend.rect(x1, y1, Ball.SIZE, Ball.SIZE);
 		debugRend.end();
 	}
 	
 }

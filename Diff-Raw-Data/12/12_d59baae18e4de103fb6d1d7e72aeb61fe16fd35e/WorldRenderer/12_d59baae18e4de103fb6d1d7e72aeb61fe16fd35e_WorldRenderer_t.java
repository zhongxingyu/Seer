 package com.me.mygdxgame;
 
 import com.me.mygdxgame.Ball; 
 import com.me.mygdxgame.Block1; 
 import com.me.mygdxgame.Block1.FacingDir;
 import com.me.mygdxgame.World; 
 import com.badlogic.gdx.Gdx; 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10; 
 import com.badlogic.gdx.graphics.OrthographicCamera; 
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer; 
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType; 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Rectangle;
 
 //Renders blocks and actors into the world
 
 @SuppressWarnings("unused")
 public class WorldRenderer { 
 	
 	private static final float CAM_WIDTH = 20f; 
 	private static final float CAM_HEIGHT = 14f; 
 	
 	private World world; 
 	private OrthographicCamera cam;	
 	ShapeRenderer debugRend = new ShapeRenderer();
 	
 	private Texture ballTexture; 
 	private Texture groundTexture; 
 	private Texture wallTexture;
 	private Texture wallNorthTexture; 
 	private Texture wallEastTexture;
 	private Texture cornerTexture;
 	private Texture holeTexture;
 	private Texture backgroundTexture; 
 	private SpriteBatch sprite; 
 	
 	private int width; 
 	private int height; 
 	private float ppuX; //pixels/unit on width
 	private float ppuY; //pixels/unit on height
 	private boolean debug = false; 
 	
 	public void setSize(int w, int h) {
 		this.width = w; 
 		this.height = h;
 		ppuX = (float)width/CAM_WIDTH; 
 		ppuY = (float)height/CAM_HEIGHT;
 	}
 	
 	
 	public WorldRenderer (World world, boolean debug) { 
 		this.world = world; 
 		this.cam = new OrthographicCamera(20,14); 
 		this.cam.position.set(10, 7f, 0); 
 		this.cam.update(); 
 		this.debug = debug;
 		sprite = new SpriteBatch(); 
 		loadTextures(); 
 	}
 	
 	public void render() {
 		sprite.begin();
 		    sprite.draw(backgroundTexture, 0, 0);
 			drawBall();
 			drawGround();
 			drawWall();
 			drawCorners();
 			drawHole();
 		sprite.end(); 
 		if(debug) {
 			debug(); 
 		}		
 	}
 	
 	private void loadTextures() {
 		ballTexture = new Texture (Gdx.files.internal("images/ball.png"));
 		groundTexture = new Texture (Gdx.files.internal("images/open.png"));
 		wallNorthTexture = new Texture (Gdx.files.internal("images/wall_north.png"));
 		wallEastTexture = new Texture (Gdx.files.internal("images/wall_east.png"));
 		cornerTexture = new Texture (Gdx.files.internal("images/corner.png"));
 		holeTexture = new Texture (Gdx.files.internal("images/hole.png"));
 		backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));
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
 			if(block.dir == FacingDir.NORTH){ //draw blocks from getWallBlocks() that face north
 			sprite.draw(wallNorthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 					Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false); 
 			}
 			if(block.dir == FacingDir.SOUTH){
 				sprite.draw(wallNorthTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, true);	
 			}
 			if(block.dir == FacingDir.EAST){
 				sprite.draw(wallEastTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, true);
 			}
 			if(block.dir == FacingDir.WEST){
 				sprite.draw(wallEastTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, true, false);
 			}
 		}
 	}
 	
 	private void drawCorners() {
 		for(Block1 block : world.getCornerBlocks()){
			if(block.dir == FacingDir.NORTH){ //draw blocks from getCornerBlocks() 
 				sprite.draw(cornerTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, false); 
 			}
 			if(block.dir == FacingDir.SOUTH){
 				sprite.draw(cornerTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, true, true);	
 			}
 			if(block.dir == FacingDir.EAST){
 				sprite.draw(cornerTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, false, true);	
 			} 
 			if(block.dir == FacingDir.WEST){
 				sprite.draw(cornerTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 						Block1.SIZE * ppuX, Block1.SIZE * ppuY, 0, 0, 32, 32, true, false);	
 			}
 		}
 	}
 	
 	private void drawHole() {
 		for(Block1 block : world.getHoleBlock()) {
 			sprite.draw(holeTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY, 
 					Block1.SIZE * ppuX, Block1.SIZE * ppuY);
 		}
 	}
	//if the for loop is changed to world.getCornerBlocks or getWallBlocks, it 
	//will render the outline of where they are rendered
 	public void debug() { 
 		//render the outline of the blocks
 		debugRend.setProjectionMatrix(cam.combined); 
 		debugRend.begin(ShapeType.Rectangle); 
 		for(Block1 block : world.getBlocks()) {
 			Polygon poly = block.getBounds(); 
 			float x1 = block.getPosition().x + poly.getX();
 			float y1 = block.getPosition().y + poly.getY(); 
 			debugRend.setColor(new Color(1,0,0,1)); //red
 			debugRend.rect(x1, y1, poly.getScaleX(), poly.getScaleY());
 		}
 		//render the outline of the ball
 		Ball ball = world.getBall(); 
 		Rectangle rect = ball.getBounds(); 
 		float x1 = ball.getPosition().x + rect.x; 
 		float y1 = ball.getPosition().y + rect.y; 
 		debugRend.setColor(new Color(0,1,0,1)); //green
 		debugRend.rect(x1, y1, rect.width, rect.height);
 		debugRend.end();
 	}
 	
 }

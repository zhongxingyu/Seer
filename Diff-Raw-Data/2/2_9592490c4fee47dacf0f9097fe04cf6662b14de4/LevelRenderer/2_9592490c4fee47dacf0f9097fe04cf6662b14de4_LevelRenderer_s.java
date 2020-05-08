 package com.me.Roguish.View;
 
 import com.me.Roguish.Model.Level;
 import com.me.Roguish.Model.Entity;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.tiled.*;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.math.Vector3;
 
 public class LevelRenderer {
 
 	private static final float CAMERA_WIDTH = 320f;
 	private static final float CAMERA_HEIGHT = 480f;
 	private static final float RUNNING_FRAME_DURATION = 0.06f;
 	//private static final float TILE_WIDTH = 32f;
 	
 	private Level level;
 	private OrthographicCamera cam;
 	
 	//Texture Regions
 	private TextureRegion heroTexture;
 	
 	private boolean debug = false;
 	private int width;
 	private int height;
 	private float ppuX;	// pixels per unit on the X axis
 	private float ppuY;	// pixels per unit on the Y axis
 	private float centerX; // centered tile x
 	private float centerY; // centered tile y
 	private TileMapRenderer tileMapRenderer;
 	private SpriteBatch spriteBatch;
 	private BitmapFont font;
 	
 	private Texture hud1;
 	private Texture hud2;
 	private Texture hud3;
 	private Texture hud4;
 	private TextureAtlas atlas;
 	
 	public LevelRenderer(Level level, boolean debug) {
 		this.level = level;	
 		this.debug = debug;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
 		this.cam.update();
 		tileMapRenderer = new TileMapRenderer(this.level.map, this.level.atlas, 32, 32);
 		spriteBatch = new SpriteBatch();
 		font = new BitmapFont(Gdx.files.internal("data/font/Arial16.fnt"),
                 Gdx.files.internal("data/font/Arial16_0.png"), false);
 		font.setColor(Color.RED);
 		
 		setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 		loadTextures();
 	}
 	
 
 	public void setSize (int w, int h) {
 		this.width = w;
 		this.height = h;
 		ppuX = (float)width / CAMERA_WIDTH;
 		ppuY = (float)height / CAMERA_HEIGHT;
 		centerX = ppuX * 32;
 		centerY = ppuY * 32;
 	}
 	
 	public void loadTextures(){
		atlas = new TextureAtlas(Gdx.files.internal("data/entity/pack/Entity.pack"));
 		//heroTexture = atlas.findRegion("Hero");
 
 		hud1 = new Texture(Gdx.files.internal("data/gui/old/Hud_1_256x256.png"));
 		hud2 = new Texture(Gdx.files.internal("data/gui/old/Hud_2_256x256.png"));
 		hud3 = new Texture(Gdx.files.internal("data/gui/old/Hud_3_256x256.png"));
 		hud4 = new Texture(Gdx.files.internal("data/gui/old/Hud_4_256x32.png"));	
 	}
 	
 	
 	public void render(){
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		spriteBatch.disableBlending();
 		spriteBatch.begin();
 		renderTiles();
 		spriteBatch.end();
 		
 		spriteBatch.enableBlending();
 		spriteBatch.begin();
 		renderEntities();
 		renderHud();
 				
 		if (debug)
 			drawDebug();
 		
 		spriteBatch.end();
 	}
 	public void renderTiles() {
         tileMapRenderer.getProjectionMatrix().set(cam.combined);
 		 
 		Vector3 tmp = new Vector3();
 		tmp.set(0, 0, 0);
 		cam.unproject(tmp);
 		
 		tileMapRenderer.render((int) tmp.x, (int) tmp.y, width, height);
 		//tileMapRenderer.render((int) tmp.x, (int) tmp.y,CAMERA_HEIGHT, CAMERA_HEIGHT);
         cam.zoom = 1.0f;
         cam.update();
         
         tileMapRenderer.render(cam);
 
 	}
 
 	private void renderEntities(){
 		for (Entity ent : level.getEntities()) {
 			spriteBatch.draw(new TextureRegion(atlas.findRegion(ent.getTexture())), ent.getX() * centerX + centerX/2 - 16,(14 - ent.getY()) * centerY);
 			
 		}
 	}
 	
 	private void renderHud(){
 		spriteBatch.draw(new TextureRegion(hud1),width-199,223);
 		spriteBatch.draw(new TextureRegion(hud2),width-199,75);
 		spriteBatch.draw(new TextureRegion(hud3),width-199,-74);
 		spriteBatch.draw(new TextureRegion(hud4),width-199,0);
 		
 	}
 	
 	private void drawDebug(){
 		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
 		font.draw(spriteBatch, "Width: " + Gdx.graphics.getWidth(), 10, 40);
 		font.draw(spriteBatch, "Height: " + Gdx.graphics.getHeight(), 10, 60);
 		font.draw(spriteBatch, "HeroX: " + level.getHero().getX(), 10, 80);
 		font.draw(spriteBatch, "HeroY: " + level.getHero().getY(), 10, 100);
 		
 		//font.draw(spriteBatch, "TEST - + ? TEST", 20, 40);
 	}
 }

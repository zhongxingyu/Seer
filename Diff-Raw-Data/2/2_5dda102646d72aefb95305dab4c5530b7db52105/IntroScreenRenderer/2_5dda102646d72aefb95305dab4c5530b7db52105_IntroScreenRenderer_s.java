 package com.ai.ant;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 
 public class IntroScreenRenderer {
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 10f; //unit control
 
 	private OrthographicCamera cam; //not sure 
 	
 	private int width;
 	private int height;
 	
 	private float ppuX;
 	private float ppuY;
 	
 	private IntroScreen intro;
 	
 	ShapeRenderer debugRenderer = new ShapeRenderer();
 	SpriteBatch batch = new SpriteBatch();
 	private Texture logoTexture;
 
 	public IntroScreenRenderer(IntroScreen intro)
 	{
 		this.intro = intro;
 		this.cam = new OrthographicCamera(50,50);
 		this.cam.position.set(25, 25, 0);
 		this.cam.update();
 		loadTextures();
 		batch = new SpriteBatch();
 	}
 	
 	public void render()
 	{
 
 		float p = (float) Gdx.graphics.getWidth();
 		
 		batch.begin();
 		loadLogo();
		batch.draw(logoTexture, p/4, 0, 300, 300);
 		//Gdx.app.log("into", "rendering");
 
 		batch.end();
 
 			
 
 		
 	}
 	
 	public IntroScreenRenderer getRenderer()
 	{
 		return this;
 	}
 	
 	
 	public void loadLogo() {
 		Character logo = intro.getLogo();
 		batch.draw(logoTexture, logo.getPosition().x * ppuX, logo.getPosition().y * ppuY, logo.SIZE * ppuX, logo.SIZE * ppuY);
 	}
 	
 	public void loadTextures() {
 		logoTexture = new Texture(Gdx.files.internal("Title1.png"));
 		Gdx.app.log("into", "loaded texture");
 
 	}
 	
 	
 	public void setSize(int w, int h) {
 		this.width = w;
 		this.height = h;
 		ppuX = (float) width/ CAMERA_WIDTH;
 		ppuY = (float) height/ CAMERA_HEIGHT;
 	}
 }

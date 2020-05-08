 package cs.honours.idw;
 
 import cs.honours.idw.reader.*;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 import cs.honours.idw.reader.GameObject;
 import cs.honours.idw.reader.managers.InputManager;
 import cs.honours.idw.reader.managers.ScreenManager;
 import cs.honours.idw.reader.managers.TextureManager;
 
 public class PrototypeMain implements ApplicationListener {
 	public static OrthographicCamera camera;
 	private SpriteBatch spriteBatch;
 	private Texture texture;
 	private Sprite sprite;
 
 	public enum State {Home, Library, Search, Reader}
 	public static State currentState = State.Home;
 	float viewportWidth, viewportHeight;
 
 
 	private static final int VIRTUAL_WIDTH = 480;
 	private static final int VIRTUAL_HEIGHT = 800;
 	private static final float ASPECT_RATIO = (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
 
 	TextureManager textureManager;
 	InputManager inputManager;
 	ScreenManager screenManager;
 	public static Vector2 crop;
 	public static Rectangle viewport = new Rectangle(0,0,800,980);
 
 	public boolean backKeyDown = false;
 
 
 	@Override
 	public void create() {	
 		textureManager = new TextureManager();
 		inputManager = new InputManager();
 		screenManager = new ScreenManager();
 
 
 		viewportWidth = Gdx.graphics.getWidth();
 		viewportHeight = Gdx.graphics.getHeight();
 
 		camera = new OrthographicCamera(1, viewportHeight/viewportWidth);
 		camera.setToOrtho(true, 480, 800);
 
 		spriteBatch = new SpriteBatch();
 		spriteBatch.setProjectionMatrix(camera.combined);
 
 		Gdx.input.setCatchBackKey(true);
 		Gdx.input.setInputProcessor(new GestureDetector(inputManager));
 		
 	}
 
 	@Override
 	public void dispose() {
 		//System.exit(0);
 
 	}
 
 	@Override
 	public void render() {		
 		update();
 		draw();
 	}
 
 	public void update(){
 
 		//libtex.updatePos(new Vector2(1,1));
 		screenManager.update();
 		//System.out.println("Touch position: ("+Gdx.input.getX()+", "+Gdx.input.getY()+")");
 
 		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
 				(int) viewport.width, (int) viewport.height);
 		//System.out.println(viewport);
 		if(!Gdx.input.isTouched()){
 			InputManager.isTouchDown=false;
 			//InputManager.currentInputState=null;
 			//
 		}
 		//if(InputManager.currentInputState!=null)
 		//	System.out.println(InputManager.currentInputState);
 
 		if(Gdx.input.isKeyPressed(Keys.BACK)){
 			if(!backKeyDown){
 				screenManager.toPreviousScreen();
				screenManager.currentlyReadingSomething=false;
 				backKeyDown=true;
 			}
 		}
 		else
 			backKeyDown=false;
 
 
 
 
 	}
 
 	public void draw(){
 
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 
 		spriteBatch.begin();
 		screenManager.draw(spriteBatch);
 		//libtex.getSprite().draw(spriteBatch);
 		spriteBatch.end();
 	}
 
 	@Override
 	public void resize(int width, int height)
 	{
 		
 		// calculate new viewport
 		float aspectRatio = (float)width/(float)height;
 		float scale = 1f;
 		crop = new Vector2(0f, 0f);
 
 		if(aspectRatio > ASPECT_RATIO)
 		{
 			scale = (float)height/(float)VIRTUAL_HEIGHT;
 			crop.x = (width - VIRTUAL_WIDTH*scale)/2f;
 		}
 		else if(aspectRatio < ASPECT_RATIO)
 		{
 			scale = (float)width/(float)VIRTUAL_WIDTH;
 			crop.y = (height - VIRTUAL_HEIGHT*scale)/2f;
 		}
 		else
 		{
 			scale = (float)width/(float)VIRTUAL_WIDTH;
 		}
 
 		float w = (float)VIRTUAL_WIDTH*scale;
 		float h = (float)VIRTUAL_HEIGHT*scale;
 
 		viewport = new Rectangle(crop.x, crop.y, w, h);
 		
 
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }

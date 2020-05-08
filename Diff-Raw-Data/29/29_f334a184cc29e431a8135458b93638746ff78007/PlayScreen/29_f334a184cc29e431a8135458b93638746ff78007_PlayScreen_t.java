 package com.gamez.zombiecampus;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.gamez.actordef.GAMEZPlayer;
 
 public class PlayScreen implements Screen {
 
     final AndroidGame game;
     final World world;
     final GAMEZPlayer player;
     private Texture background;
     private SpriteBatch spriteBatch;
     private float alpha = 0.0f;
 
     // Creates a background object initialized with time and weather condition
     private Background bg = new Background(new WUWeatherTime());
     
     OrthographicCamera camera;
 
     public PlayScreen(final AndroidGame g) {
     	game = g;
         camera = new OrthographicCamera();
         camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        world = new World(new Vector2(0,-1), true)
        player = new GAMEZPlayer()
     }
     
     @Override
     public void render(float delta) {
     	Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         camera.update();
         game.batch.setProjectionMatrix(camera.combined);
         spriteBatch.begin();
         
         if(alpha >= .95f)
         		alpha = 1.0f;
         else
         	alpha = alpha + .05f;
                         
         background = new Texture(bg.Draw());
                
         spriteBatch.setColor(1.0f, 1.0f, 1.0f, alpha);
         spriteBatch.draw(background, 0, 0, camera.viewportWidth, camera.viewportHeight);
         spriteBatch.end();   
         checkButtons();
         
         background.dispose();
     }
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		spriteBatch = new SpriteBatch();
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
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
	public void checkButtons(){
		// Check location of user touch input
		// jump button
		if(Touch.getButtonTouched(Gdx.input.getX(), Gdx.input.getY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 50, 40, 100))
			player.move(0, 5);
		// fire button
		else if(Touch.getButtonTouched(Gdx.input.getX(), Gdx.input.getY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 51, 100, 40, 100))
			player.getWeapon().fire();
		// pause button
		else if(Touch.getButtonTouched(Gdx.input.getX(), Gdx.input.getY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 100, 0, 39))
			System.out.println("Pause everything here")
	}
 }

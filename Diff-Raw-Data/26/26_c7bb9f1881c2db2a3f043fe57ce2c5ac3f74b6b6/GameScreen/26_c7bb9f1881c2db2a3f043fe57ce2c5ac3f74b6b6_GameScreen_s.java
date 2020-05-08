 package com.mobi.badvibes;
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenEquations;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.mobi.badvibes.controller.WorldController;
 import com.mobi.badvibes.model.world.World;
 import com.mobi.badvibes.nimators.BadVibesScreenAccessor;
 import com.mobi.badvibes.util.ContentManager;
 import com.mobi.badvibes.view.WorldRenderer;
 
 public class GameScreen extends BadVibesScreen
 {
     private WorldController controller;
     private WorldRenderer   renderer;
 
     private int             width, height;
     private Texture         sprites;
     
     private ShapeRenderer 	shapeRenderer;
 	private Rectangle railPosition;
 	private Rectangle platformPosition;
     
     @Override
     protected void initialize()
     {
         // TODO Auto-generated method stub
         sprites     = ContentManager.loadImage("data/game/sprites.png");
         // TODO, get full src rect but determine destination rectangle
         
         width       = Gdx.graphics.getWidth();
         height      = Gdx.graphics.getHeight();
         
         shapeRenderer = new ShapeRenderer();
 
         Tween.registerAccessor(GameScreen.class, new BadVibesScreenAccessor());
         
         controller = new WorldController();
         renderer = new WorldRenderer(controller.getWorld());
         
         Timeline.createSequence()
         .push(Tween.to(this, BadVibesScreenAccessor.OPACITY, 0.5f).target(1).ease(TweenEquations.easeInCubic))
         .start(tweenManager);
         
 
         float railWidth = width;
		float heightOfRail = 187;
		float railHeight = width / 1280.0f * heightOfRail;
 		railPosition = new Rectangle(0, World.RAIL_Y_OFFSET, railWidth, railHeight);
         float platformWidth = width;
		float heightOfPlatform = 578;
		float platformHeight = width / 1280.0f * heightOfPlatform;
 		platformPosition = new Rectangle(0, World.PLATFORM_Y_OFFSET, platformWidth, platformHeight);
     }
 
     @Override
     protected void renderScreen(float delta)
     {
     	controller.update(delta);
     	  	
         spriteBatch.setProjectionMatrix(camera.projection);
         spriteBatch.setTransformMatrix(camera.view);
 
         shapeRenderer.setProjectionMatrix(camera.projection);
         shapeRenderer.setTransformMatrix(camera.view);
         
         shapeRenderer.begin(ShapeType.FilledRectangle);
         shapeRenderer.setColor(54/255f, 52/255f, 50/255f, 1.0f);
         
         shapeRenderer.filledRect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         shapeRenderer.end();
         
         
         spriteBatch.begin();
	        spriteBatch.draw(sprites, railPosition.x, railPosition.y, railPosition.width, railPosition.height, 0, 0, 1280, 187, true, true);
	        spriteBatch.draw(sprites, platformPosition.x, platformPosition.y, platformPosition.width, platformPosition.height, 0, 187, 1280, 578, true, true);
 	        /*
 	        1280x187 - rails
 	        1280x578 - platform
 	        */
         spriteBatch.end();
         
         drawTiles(shapeRenderer);
         renderer.render(spriteBatch, delta);
         
         
     }
     
     private void drawTiles(ShapeRenderer shapeRenderer) {
     	
     	shapeRenderer.begin(ShapeType.Rectangle);
 		shapeRenderer.setColor(Color.RED);
 		
 		for (int y = 0; y < World.GRID_HEIGHT; y++)
 			for (int x = 0; x < World.GRID_WIDTH; x++)
 			shapeRenderer.rect(World.X_OFFSET + x * World.GRID_CELL_WIDTH, World.PLATFORM_Y_OFFSET + y * World.GRID_CELL_HEIGHT, World.GRID_CELL_WIDTH, World.GRID_CELL_HEIGHT);
 		shapeRenderer.end();
 	}
 
 	@Override
     public void hide()
     {
         Gdx.input.setInputProcessor(null);
     }
 
     @Override
     public void show()
     {
         Gdx.input.setInputProcessor(this);
     }
 
     @Override
     public void pause()
     {
 
     }
 
     @Override
     public void resume()
     {
 
     }
 
     @Override
     public boolean keyDown(int keycode)
     {
         return false;
     }
 
     @Override
     public boolean keyUp(int keycode)
     {
         return false;
     }
 
     @Override
     public boolean keyTyped(char character)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean touchDown(int screenX, int screenY, int pointer, int button)
     {
         return false;
     }
 
     @Override
     public boolean touchUp(int screenX, int screenY, int pointer, int button)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean touchDragged(int screenX, int screenY, int pointer)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean mouseMoved(int screenX, int screenY)
     {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean scrolled(int amount)
     {
         // TODO Auto-generated method stub
         return false;
     }
 }

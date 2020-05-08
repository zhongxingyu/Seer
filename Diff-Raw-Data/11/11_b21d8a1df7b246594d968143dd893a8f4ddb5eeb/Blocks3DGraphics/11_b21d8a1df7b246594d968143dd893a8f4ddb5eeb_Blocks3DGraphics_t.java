 package de.blocks;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 public class Blocks3DGraphics implements ApplicationListener {
     public static StillModel  PlaneModel;
     public static StillModel  BlockModel;
     private BlocksGame        blocksGame;
     private PerspectiveCamera camera;
     private ShaderProgram     shader;
     private boolean           previousSpacePress;
     private SpriteBatch       spriteBatch;
     private BitmapFont        font;
     private float		      startAzimuth;
 
     @Override
     public void create() {
         final ObjLoader loader = new ObjLoader();
         Blocks3DGraphics.PlaneModel = loader.loadObj(Gdx.files.internal("data/plane.obj"));
         Blocks3DGraphics.BlockModel = loader.loadObj(Gdx.files.internal("data/block.obj"));
 
         blocksGame = new BlocksGame();
 
         camera = new PerspectiveCamera(45.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         camera.near = 0.1f;
        camera.translate(0.0f, 0.0f, 40.0f);
         camera.lookAt(0.0f, 0.0f, 0.0f);
         camera.update();
 
         spriteBatch = new SpriteBatch();
         font = new BitmapFont();
 
         String shaderSuffix;
 
         if (Gdx.app.getType() == ApplicationType.Android) {
             shaderSuffix = "Android";
         } else {
             shaderSuffix = "Desktop";
         }
 
         shader = new ShaderProgram(Gdx.files.internal("data/basicVertexShader" + shaderSuffix), Gdx.files.internal("data/basicFragmentShader" + shaderSuffix));
 
         if (!shader.isCompiled()) {
             System.out.println("Shader compilation failed!");
             shader.begin();
             System.out.println(shader.getLog());
             shader.end();
         } else {
             System.out.println("Shader compiled sucessfully!");
         }
 
         previousSpacePress = false;
         
         if (Gdx.input.isPeripheralAvailable(Peripheral.Compass)) {
         	if (Gdx.input.getAzimuth() > -135.0f) {
         		startAzimuth = Gdx.input.getAzimuth() - 45.0f;
         	}
         	else {
         		startAzimuth = 180.0f - 45.0f + (180.0f - Gdx.input.getAzimuth());
         	}
         }
         else {
         	startAzimuth = 0;
         }
         
     }
 
     @Override
     public void dispose() {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void render() {
         final float delta = Gdx.graphics.getDeltaTime();
         blocksGame.update(delta);
 
         // Movement input
         if (Gdx.input.isKeyPressed(Keys.A)) {
             blocksGame.getGameField().moveBlocks(-0.2f, 0.0f);
         }
 
         if (Gdx.input.isKeyPressed(Keys.D)) {
             blocksGame.getGameField().moveBlocks(0.2f, 0.0f);
         }
 
         if (Gdx.input.isKeyPressed(Keys.W)) {
             blocksGame.getGameField().moveBlocks(0.0f, 0.2f);
         }
 
         if (Gdx.input.isKeyPressed(Keys.S)) {
             blocksGame.getGameField().moveBlocks(0.0f, -0.2f);
         }
 
         if (Gdx.input.isKeyPressed(Keys.SPACE)) {
             if (!previousSpacePress) {
                 blocksGame.nextColor();
             }
 
             previousSpacePress = true;
         } else {
             previousSpacePress = false;
         }
 
         if (Gdx.app.getType() == ApplicationType.Android) {
             blocksGame.getGameField().moveBlocks(0.01f * Gdx.input.getDeltaX(), -0.01f * Gdx.input.getDeltaY());
             
             // touch control of color change to be removed in final version
             if(Gdx.input.justTouched()) {
                 blocksGame.nextColor();
                 Gdx.input.vibrate(50); 
             }
             
             // color change by rotating device
             if (getRotationDegree(startAzimuth, Gdx.input.getAzimuth()) >= 90.0f) {
             	startAzimuth = Gdx.input.getAzimuth();
             	blocksGame.nextColor();
             	Gdx.input.vibrate(50);            	
             }                     
             
             //Accelerator axis are inverted due to landscape mode !!
             float deltaY = Gdx.input.getAccelerometerX() / 10;
             float deltaX = Gdx.input.getAccelerometerY() / 10;
                        
            blocksGame.getGameField().moveBlocks(deltaX, 0.0f);
            blocksGame.getGameField().moveBlocks(0.0f, -deltaY);
         }
 
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
         Gdx.gl.glEnable(GL10.GL_BLEND);
         Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
         shader.begin();
         shader.setUniformMatrix("u_viewProjectionMatrix", camera.combined);
         blocksGame.render(shader);
         shader.end();
         
         spriteBatch.begin();
         
         if(Gdx.input.isPeripheralAvailable(Peripheral.Compass)) {
                 font.draw(spriteBatch, "Azimuth: " + Gdx.input.getAzimuth(), 10, Gdx.graphics.getHeight() - 10);
                 font.draw(spriteBatch, "Pitch: " + Gdx.input.getPitch(), 10, Gdx.graphics.getHeight() - 30);
                 font.draw(spriteBatch, "Roll: " + Gdx.input.getRoll(), 10, Gdx.graphics.getHeight() - 50);
                 font.draw(spriteBatch, "AccelerometerZ: " + Gdx.input.getAccelerometerZ(), 10, Gdx.graphics.getHeight() - 70); 
         } else {
             font.draw(spriteBatch, "No Compass available", 10, Gdx.graphics.getHeight() - 10);
         }
         
         font.draw(spriteBatch, "Score : " + blocksGame.getScore(), Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 10);
         spriteBatch.end();
     }
 
     // calculating rotation
     public float getRotationDegree(float oldAzimuth, float newAzimuth) {
     	
     	float degree;
     	
     	oldAzimuth += 180;
     	newAzimuth += 180;  	    
     	degree = newAzimuth - oldAzimuth;
     	    	
     	if (Math.abs(degree) > 180) {
     		if (degree >= 0) {
     			degree = -360 + degree;
     		}
     		else {
     			degree = 360 + degree;
     		}
     	}
 
     	return degree;
     }
     
     @Override
     public void resize(final int width, final int height) {
     }
 
     @Override
     public void resume() {
     }
 }

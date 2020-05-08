 package org.racenet.racesow;
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.racenet.framework.BitmapFont;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.CameraText;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GLGraphics;
 import org.racenet.framework.GLTexture;
 import org.racenet.framework.SpriteBatcher;
 import org.racenet.framework.Vector2;
 import org.racenet.framework.interfaces.Game;
 import org.racenet.framework.interfaces.Screen;
 import org.racenet.framework.interfaces.Input.TouchEvent;
 
 import android.util.Log;
 
 class GameScreen extends Screen {
 		
 	public Player player;
 	CameraText ups, fps, timer;
 	public Map map;
 	
 	Vector2 gravity = new Vector2(0, -30);
 	Camera2 camera;
 	GLGraphics glGraphics;
 	
 	boolean jumpPressed = false;
 	float jumpPressedTime = 0;
 	boolean shootPressed = false;
 	float shootPressedTime = 0;
 	SpriteBatcher batcher;
 	
 	int fpsInterval = 5;
 	int frames = 10;
 	float sumDelta = 0;
 	
 	public GameScreen(Game game, String mapName) {
 			
 		super(game);
 		glGraphics = ((GLGame)game).getGLGraphics();
 		
 		GLTexture.APP_FOLDER = "racesow";
 		
 		float camWidth =  80;
 		float camHeight = 80 * (float)game.getScreenHeight() / (float)game.getScreenWidth();
 		
 		batcher = new SpriteBatcher(glGraphics, 96);
 		GLTexture texture = new GLTexture((GLGame)game, "font.png");
 		BitmapFont font = new BitmapFont(texture, 0, 0, 17, 30, 50);
 		
 		fps = new CameraText(batcher, font, glGraphics.getGL(), camWidth / 2 - 10, camHeight / 2 - 3);
 		ups = new CameraText(batcher, font, glGraphics.getGL(), camWidth / 2 - 25, camHeight / 2 - 3);
 		timer = new CameraText(batcher, font, glGraphics.getGL(), camWidth / 2 - 40, camHeight / 2 - 3);
 		
 		camera = new Camera2(glGraphics, camWidth, camHeight);
 		camera.position.set(0, camHeight / 2);
 		camera.addHud(fps);
 		camera.addHud(ups);
 		camera.addHud(timer);
 
 		
 		map = new Map(glGraphics.getGL(), camWidth, camHeight);
 		map.load((GLGame)game, mapName);
 		player = new Player((GLGame)game, map, camera, map.playerX, map.playerY);
 		
 	}
 
 	public void update(float deltaTime) {
 		
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			
 			TouchEvent e = touchEvents.get(i);
 			
 			if (e.type == TouchEvent.TOUCH_DOWN) {
 				
 				if (e.x / (float)game.getScreenWidth() > 0.5f) {
 					
 					if (!this.jumpPressed) {
 						
 						this.jumpPressed = true;
 						this.jumpPressedTime = 0;
 					}
 					
 				} else {
 					
 					if (!this.shootPressed) {
 						
 						this.shootPressed = true;
 						this.shootPressedTime = 0;
 					}
 				}
 				
 
 			} else if (e.type == TouchEvent.TOUCH_UP) {
 				
				if (e.x / camera.frustumWidth > 5) {
 					
 					this.jumpPressed = false;
 					this.jumpPressedTime = 0;
 					
 				} else {
 					
 					this.shootPressed = false;
 					this.shootPressedTime = 0;
 				}
 			}
 		}
 		
 		if (this.jumpPressed) {
 			
 			player.jump(this.jumpPressedTime);
 			jumpPressedTime += deltaTime;
 		}
 		
 		if (this.shootPressed) {
 			
 			player.shoot(this.shootPressedTime);
 			shootPressedTime += deltaTime;
 		}
 		
 		player.move(gravity, deltaTime, jumpPressed);
 		
 		float camY = camera.frustumHeight / 2;
 		if (player.getPosition().y + 8 > camera.frustumHeight) {
 			
 			camY = player.getPosition().y - camera.frustumHeight / 2 + 8;
 		}
 		
 		camera.setPosition(player.getPosition().x + 20, camY);		
 		map.update(camera.position, deltaTime);
 
 		frames--;
 		sumDelta += deltaTime;
 		if (frames == 0) {
 		
 			fps.text = "fps " + String.valueOf(new Integer((int)(fpsInterval / sumDelta)));
 			frames = fpsInterval;
 			sumDelta = 0;
 			
 		}
 
 		ups.text = "ups " + String.valueOf(new Integer((int)player.virtualSpeed));
 		timer.text = "t " + String.format("%.2f", map.getCurrentTime());
 	}
 
 	public void present(float deltaTime) {
 		
 		GL10 gl = glGraphics.getGL();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		gl.glFrontFace(GL10.GL_CCW);
 		gl.glEnable(GL10.GL_CULL_FACE);
 		gl.glCullFace(GL10.GL_BACK);
 		
 		camera.setViewportAndMatrices();
 		
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		
 		map.draw();
 		player.draw();
 		
 		synchronized(player) {
 		
 			camera.drawHud();
 		}
 	}
 
 	public void pause() {
 
 	}
 
 	public void resume() {
 
 		this.map.reloadTextures();
 		this.player.reloadTextures();
 	}
 
 	public void dispose() {
 
 		this.map.dispose();
 		this.player.dispose();
 	}
 }

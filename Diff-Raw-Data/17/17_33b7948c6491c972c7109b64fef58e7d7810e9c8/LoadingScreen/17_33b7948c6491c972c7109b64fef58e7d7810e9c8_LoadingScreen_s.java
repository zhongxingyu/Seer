 package org.racenet.racesow;
 
 import java.io.File;
 
 import org.racenet.framework.BitmapFont;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GLTexture;
 import org.racenet.framework.Screen;
 import org.racenet.framework.SpriteBatcher;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.Vector2;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.opengl.GLES10;
 
 /**
  * Class beeing invoked when loading a map
  * 
  * @author soh-zolex
  *
  */
 class LoadingScreen extends Screen {
 
 	public Camera2 camera;
	TexturedBlock header;
 	SpriteBatcher batcher;
 	String mapName;
 	public String demoFile;
 	BitmapFont loading;
 	int frames = 0;
 	
 	/**
 	 * Prepare the header graphics and the loading text
 	 * 
 	 * @param GLGame game
 	 * @param String mapName
 	 */
 	public LoadingScreen(GLGame game, String mapName, String demoFile) {
 			
 		super(game);
 		this.mapName = mapName;
 		this.demoFile = demoFile;
 		
 		this.camera = new Camera2(80,  80 * (float)game.getScreenHeight() / (float)game.getScreenWidth());
 		
 		this.batcher = new SpriteBatcher(96);
 		GLTexture font = new GLTexture("font.png");
 		this.loading = new BitmapFont(font, 0, 0, 17, 30, 50);
 		
 		GLTexture.APP_FOLDER = "racesow";
 		header = new TexturedBlock("racesow.png", TexturedBlock.FUNC_NONE, -1, -1, 0, 0, new Vector2(0, 0), new Vector2(camera.frustumWidth, 0));
 		header.vertices[0].x = 0;
 		header.vertices[0].y = camera.frustumHeight - header.height;
		header.texture.setFilters(GLES10.GL_LINEAR, GLES10.GL_LINEAR);
 	}
 
 	/**
 	 * Be sure to clear the touch event buffer
 	 */
 	public void update(float deltaTime) {
 		
 		if (this.frames++ == 2) {
 			
 			SharedPreferences prefs = ((Activity)this.game).getSharedPreferences("racesow", Context.MODE_PRIVATE);
 			boolean sound = prefs.getBoolean("sound", true);
 			boolean ambience = prefs.getBoolean("ambience", true);
 			boolean blur = prefs.getBoolean("blur", true);
 			boolean demos = prefs.getBoolean("demos", true);
 			String name = prefs.getString("name", "player");
 			
 			DemoParser parser = null;
 			if (this.demoFile != null) {
 				
 				parser = new DemoParser();
 				String folder = "racesow" + File.separator + "demos" + File.separator;
 				if (!parser.parse(folder + this.demoFile)) {
 					
 					this.game.runOnUiThread(new Runnable() {
 						
 						public void run() {
 						
 							new AlertDialog.Builder(LoadingScreen.this.game)
 					        .setMessage("Could not load the demo")
 					        .setNeutralButton("Back", null)
 					        .show();
 						}
 					});
 					
 					this.game.finish();
 				}
 				
 				this.mapName = parser.map;
 			}
 			
 			// right after drawing the loading screen load
 			// the map and player and pass it to the GameScreen
 			Map map = new Map(this.camera, prefs.getBoolean("gfx", true), demos);
 			map.load(this.mapName, ambience, this.demoFile != null);
 			Player player = new Player(name, map, this.camera, sound, blur, demos);
 			
 			Racesow.stopMusic();
 			Racesow.IN_GAME = true;
 			game.setScreen(new GameScreen(this.game, this.camera, map, player, parser, demos));
 		}
 	}
 
 	/**
 	 * Show the loading screen and load the map
 	 */
 	public void present(float deltaTime) {
 		
 		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);
 		
 		GLES10.glFrontFace(GLES10.GL_CCW);
 		GLES10.glEnable(GLES10.GL_CULL_FACE);
 		GLES10.glCullFace(GLES10.GL_BACK);
 		
 		this.camera.setViewportAndMatrices(this.game.glView.getWidth(), this.game.glView.getHeight());
 		
 		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
 		GLES10.glEnable(GLES10.GL_BLEND);
 		GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
 		
 		this.header.draw();
 		this.loading.draw(this.batcher, "LOADING", 0.1f, 0.1f, this.camera.frustumWidth / 2 -10, this.camera.frustumHeight / 2);
 	}
 
 	/**
 	 * Nothing to do on GLGame pause
 	 */
 	public void pause() {
 
 	}
 
 	/**
 	 * Reload all textures when the
 	 * openGL context was lost
 	 */
 	public void resume() {
 
 		this.header.reloadTexture();
 		this.loading.texture.reload();
 	}
 
 	/**
 	 * Get rid of all textures when
 	 * leaving the screen
 	 */
 	public void dispose() {
 
 		this.header.dispose();
 		this.loading.texture.dispose();
 	}
 }

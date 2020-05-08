 package com.flexymind.labirynth.screens.game.OpenGL;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.flexymind.labirynth.R;
 import com.flexymind.labirynth.objects.GameLevel;
 import com.flexymind.labirynth.screens.game.GameScreen;
 import com.flexymind.labirynth.screens.start.StartScreen;
 import com.flexymind.labirynth.storage.LevelStorage;
 import com.flexymind.labirynth.storage.Settings;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GLRenderer implements GLSurfaceView.Renderer{
 	public static final int FPS = 60;
 	
 	private long updMillisec = 1000 / FPS;
 	
 	private long lastDrawTime = 0;
 	
 	private long drawTick = 0;
 	
 	private GameLevel level = null;
 	
 	private String gameLevelString = "";
 	
 	private Context context = null;
 	
	private boolean firsttime = true;
 	
 	public GLRenderer(Context context, String gamelevel){
 		this.context = context;
 		gameLevelString = gamelevel;
 	}
 	
 	public GLRenderer(Context context, int gamelevelID){
 		LevelStorage lvlstor = new LevelStorage(context);
 		gameLevelString = lvlstor.getLevelNames().get(gamelevelID);
 		this.context = context;
 	}
 	
 	public void onDrawFrame(GL10 gl) {
 		
 		drawTick = SystemClock.elapsedRealtime() - lastDrawTime;
 		lastDrawTime = SystemClock.elapsedRealtime();
 		
     	if (drawTick < updMillisec){
     		SystemClock.sleep(updMillisec - drawTick);
     	}
     	
     	level.onUpdate();
     	
    	if (level.getIsFinished() && firsttime){
     		// start activity with finish menu
    		firsttime = false;
    		
 			StartScreen.startActivity.runOnUiThread(new Runnable(){
 
 				public void run() {
 		    		final Dialog dialog = new Dialog(context);
 		    		
 					dialog.setContentView(R.layout.finishmenu);
 					dialog.setTitle("You WIN!");
 
 					TextView text = (TextView) dialog.findViewById(R.id.score);
 					text.setText(String.format("Your score -  %d", (int)level.getScore()));
 
 					Button dialogButtonRestart = (Button) dialog.findViewById(R.id.dialogButtonRestart);
 					// if button is clicked, close the custom dialog
 					dialogButtonRestart.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							GameScreen.startNextLevel();
 							dialog.dismiss();
 						}
 					});
 
 					Button dialogButtonLevels = (Button) dialog.findViewById(R.id.dialogButtonLevels);
 					// if button is clicked, close the custom dialog
 					dialogButtonLevels.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							if (StartScreen.startActivity != null){
 								StartScreen.startActivity.finishActivity(StartScreen.ID_GAMESCREEN);
 							}
 						}
 					});
 
 					Button dialogButtonQuit = (Button) dialog.findViewById(R.id.dialogButtonQuit);
 					// if button is clicked, close the custom dialog
 					dialogButtonQuit.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							if (StartScreen.startActivity != null){
 								StartScreen.startActivity.finishActivity(StartScreen.ID_GAMESCREEN);
 								StartScreen.startActivity.finishActivity(StartScreen.ID_CHOISELEVELSCREEN);
 							}
 						}
 					});
 					dialog.show();
 				}
 			});
     	}
     	
 		// clear Screen and Depth Buffer
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
 		// Reset the Modelview Matrix
 		gl.glLoadIdentity();
 		
 		// Drawing
 		gl.glTranslatef(0.0f, 0.0f, -600); // move INTO the screen
 											// is the same as moving the camera
 											// away
 											// gl.glScalef(0.5f, 0.5f, 0.5f); //
 											// scale the square to 50%
 		// otherwise it will be too large
 		if (level != null){
 			level.onDraw(gl);
 		}
 		
 		gl.glFlush();
 	}
 
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		if (height == 0) { // Prevent A Divide By Zero By
 			height = 1; // Making Height Equal One
 		}
 		gl.glViewport(0, 0, width, height); // Reset The Current Viewport
 		gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
 		gl.glLoadIdentity(); // Reset The Projection Matrix
 
 		// Calculate The Aspect Ratio Of The Window // -579.411255f
 		//GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 10,
 		//		1200.0f);
 		
 		gl.glOrthof(	-Settings.getCurrentXRes() / 2,
 						Settings.getCurrentXRes() / 2,
 						-Settings.getCurrentYRes() / 2, 
 						Settings.getCurrentYRes() / 2, 
 						200, 
 						1200.0f);
 		
 		gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
 		gl.glLoadIdentity(); // Reset The Modelview Matrix
 	}
 
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		
         LevelStorage lvlstor = new LevelStorage(context);
 		
 		level = lvlstor.loadGameLevelbyName(gl, gameLevelString);
 		
 		gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
 		gl.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		gl.glClearDepthf(1.0f);                     //Depth Buffer Setup
 		gl.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing
 		gl.glDepthFunc(GL10.GL_LEQUAL);             //The Type Of Depth Testing To Do
 	    gl.glEnable(GL10.GL_BLEND); 				//Enable alpha blending
 	    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); //Set the blend function
 		
 		//Really Nice Perspective Calculations
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 		gl.glDisable(GL10.GL_DITHER);
 	}
 	
 	public void destroyLevel(){
 		level.destoyLevel();
 	}
 }

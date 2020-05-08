 package com.psywerx.dh;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Stack;
 
 import android.media.MediaPlayer;
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 
 public class Game {
 
     static GlProgram program;
     static int WIDTH = 100;
     static int HEIGHT = 100;
     private static Background bg;
     static ScoreBoard top;
     static Menu menu;
     static PlayButton playButton;
     static ContinueButton continueButton;
     static RestartButton restartButton;
     static ShareButton shareButton;
     private static boolean gameCreated = false;
     static char prevState = 'M';
     
     protected static Player player1;
     protected static float position;
     protected static float smoothPosition;
     
     protected static Random rand = new Random();
     protected static float[] projection = new float[16];
     protected static float[] model_projection = new float[16];
     
     
     protected static int currentLevel = 0;
     
     private static Levels lvls = new Levels();
     private static final int PRELOAD_SIZE = 1000;
     static Stack<Enemy> preloadedEnemies = new Stack<Enemy>();
     public static boolean moving = false;
     public static char state = 'M';
     public static Hint hint;
     public static LevelHints levelHints;
     private static float[] model_view_projection = new float[16];
     static SoundButton soundButton;
     public static boolean sound = true;
     static MediaPlayer mp;
 
     static void create(GlProgram program) {
         if(gameCreated) return;
         Game.program = program;
         
         preloadedEnemies = new Stack<Enemy>();
         for(int i = 0; i < PRELOAD_SIZE; i++){
             preloadedEnemies.push(new Enemy());
         }
         mp = MediaPlayer.create(MyRenderer.context, R.raw.dh);
         mp.setLooping(true);
         bg = new Background();
         menu = new Menu();
         hint = new Hint();
         levelHints = new LevelHints();
         playButton = new PlayButton();
         continueButton = new ContinueButton();
         restartButton = new RestartButton();
         shareButton = new ShareButton();
         soundButton = new SoundButton();
         //SceneGraph.activeObjects.add(bg);
         
         Game.reset();
         Game.state = 'M';
         gameCreated = true;
         
     }
     public static void reset(){
         Iterator<Drawable> dws = SceneGraph.activeObjects.iterator();
         while (dws.hasNext()) {
             Drawable d = dws.next();
             if(d instanceof Enemy){
                 Game.preloadedEnemies.push((Enemy)d);
             }
         }
         SceneGraph.activeObjects = new LinkedList<Drawable>();
         top = new ScoreBoard();
         //SceneGraph.activeObjects.add(top);
         
         player1 = new Player();
         player1.move(0.5f, 0, 0);
         SceneGraph.activeObjects.add(player1);
         Game.state = 'G';
         levelHints.reset();
         lvls.levels[currentLevel].reset();
         
         if(Game.sound){
             //mp.seekTo(0);
             mp.start();
         }
     }
 
     static void tick(Float theta) {
         switch (Game.state) {
         case 'M':
            if(!Game.sound && mp.isPlaying()) mp.pause();
            if(Game.sound && !mp.isPlaying()) mp.start();
             break;
         case 'P':
            //if(mp.isPlaying()) mp.pause();
             break;
         case 'E':
            // if(mp.isPlaying()) mp.pause();
             bg.tick(theta);
             break;
         case 'A':
            if(mp.isPlaying()) mp.pause();
             break;
         default:
            if(!Game.sound && mp.isPlaying()) mp.pause();
            if(Game.sound && !mp.isPlaying()) mp.start();
             bg.tick(theta);
             lvls.levels[currentLevel].tick(theta);
             SceneGraph.tick(theta);
             Game.smoothPosition = 0.9f*Game.smoothPosition + 0.1f*Game.player1.position[0];
             top.tick(theta);
             hint.tick(theta);
             levelHints.tick(theta);
             break;
         }
         menu.tick(theta);
 
     }
     
     static void draw() {
         resetFrame();
 
         switch (Game.state) {
         case 'M':
             bg.draw();
             SceneGraph.draw();
             menu.draw();
             playButton.draw();
             soundButton.draw();
             break;
         case 'P':
             bg.draw();
             menu.draw();
             SceneGraph.draw();
             top.draw();
             menu.draw();
             continueButton.draw();
             soundButton.draw();
             break;
         case 'E':
             bg.draw();
             menu.draw();
             SceneGraph.draw();
             top.draw();
             menu.draw();
             restartButton.draw();
             shareButton.draw();
             break;
         default:
             bg.draw();
             SceneGraph.draw();
             top.draw();
             menu.draw();
             hint.draw();
             levelHints.draw();
             break;
         }
 
     }
 
     private static void resetFrame() {
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         GLES20.glClearColor(0f, 0f, 0f, 1f);
         GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT
                 | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
         GLES20.glUseProgram(program.program);
         GLES20.glUniform1i(program.samplerLoc, 0);
         GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, program.texture);
 
         float ratio = WIDTH / (float) HEIGHT;
 
         Matrix.setLookAtM(model_projection, 0, 0, 0, -1f, 0, 0, 0, 0, 1, 0);
         Matrix.frustumM(model_view_projection, 0, ratio, -ratio, -1, 1,
                 0.9999f, 40);
         Matrix.rotateM(model_projection, 0, smoothPosition*-2, 0, 1, 0);
         
         Matrix.multiplyMM(projection, 0, model_view_projection, 0,
                 model_projection, 0);
         GLES20.glUniformMatrix4fv(program.projectionMatrixLoc, 1, false,
                 projection, 0);
 
         GLES20.glEnable(GLES20.GL_BLEND);
         GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
     }
 }

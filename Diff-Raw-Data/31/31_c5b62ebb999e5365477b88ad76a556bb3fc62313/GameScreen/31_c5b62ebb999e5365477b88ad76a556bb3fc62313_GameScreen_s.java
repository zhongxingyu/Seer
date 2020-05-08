 package com.badlogic.androidgames.fishboy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 
 import com.badlogic.androidgames.framework.Game;
 import com.badlogic.androidgames.framework.Graphics;
 import com.badlogic.androidgames.framework.Input.TouchEvent;
 import com.badlogic.androidgames.framework.Screen;
 import com.badlogic.androidgames.framework.math.Vector2;
 
 public class GameScreen extends Screen {
     enum GameState {
         Running,
         GameOver
     }
     GameState state = GameState.Running;
     
     private static int TOTAL_FISHES = 100;
     private Random rand;
     private int lifes = 4;
     private int level = 1;
     private int score = 0;
     private Boy boy;
     private Clouds clouds;
     private List<Fish> fishes;
     
     
     public GameScreen(Game game) {
         super(game);
         rand = new Random();
         boy = new Boy(new Vector2(130, 90));
         clouds = new Clouds(game);
         //createFishes();
         if (Settings.soundEnabled){
         	Assets.menu.stop();
         	Assets.theme.play();
         	Assets.theme.setLooping(true);
         }
         
         fishes = new ArrayList<Fish>();
         createFishes();
     }
     
     
     private void createFishes() {
         //cria peixes
         for (int i = 0; i <= TOTAL_FISHES; ++i) {
             Vector2 pos = new Vector2( 
             		rand.nextInt(game.getGraphics().getWidth() - Assets.fish.getWidth() + 1) + Assets.fish.getWidth(),
             		game.getGraphics().getHeight()
             	);
             this.fishes.add(new Fish(pos, game));
         }
 	}
 	
 
 	@Override
     public void update(float deltaTime) {
         List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
         game.getInput().getKeyEvents();
         if(state == GameState.Running)
             updateRunning(touchEvents, deltaTime);
         if(state == GameState.GameOver)
         	game.setScreen(new GameOverScreen(game));
     }
     
     private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {        
         int len = touchEvents.size();
         for(int i = 0; i < len; i++) {
             TouchEvent event = touchEvents.get(i);
             if(event.type == TouchEvent.TOUCH_DOWN) {
             	boy.stop = false;
             	if(event.x <= game.getGraphics().getWidth() / 2 ) {
                 	boy.moveLeft = true;
                 }
                 if(event.x > game.getGraphics().getWidth() / 2) {
                     boy.moveLeft = false;
                 }
             }
             
             if(event.type == TouchEvent.TOUCH_UP) {
             	boy.stop = true;
             }
         }
         boy.update(deltaTime);
         level = score / 100 + 1;
         
         for (int i = 0; i < fibbonacciFishes(level); i++)
         {
             fishes.get(i).update(deltaTime);
             if (fishes.get(i).isDead)
             {
                 lifes--;
                 fishes.get(i).reboot(game);
             }else if (fishes.get(i).hit(boy.getBounds()))
             {
                 score += 10;
                 fishes.get(i).reboot(game);
             }
         }
         
         if(lifes == 0) {
             state = GameState.GameOver;
         }
         clouds.update(deltaTime);
     }
     
     private int fibbonacciFishes(int level){
     	int[] sequence = new int[] { 1, 1, 2, 3, 5 };
         if (level > 5)
             return 5;
         else
             return sequence[level % sequence.length];
     }
 
     @Override
     public void present(float deltaTime) {
         Graphics g = game.getGraphics();
         g.drawPixmap(Assets.background, 0, 0);
         drawWorld();
         if(state == GameState.Running){
         	drawRunningUI();
         	drawFishes();
         }
         drawText(g, "" + score, 20 - ("" + score).length()*20 / 2, g.getHeight() - 42);  
     }
     
     private void drawFishes() {
     	Graphics g = game.getGraphics();
         //debug
       	Paint paint = new Paint();
         paint.setColor(Color.rgb(0, 0,255));
         paint.setStyle(Style.STROKE);
     	
 		for (int i = 0; i < fishes.size() ; i++){
 			g.drawPixmap(Assets.fish, (int) fishes.get(i).position.x, (int) fishes.get(i).position.y);
 	        g.drawRect(fishes.get(i).bounds, paint);

 		}
		drawText(g, "" + fishes.get(1).position.y, 50, 50); 
 	}
 
 
 	private void drawWorld() {
         Graphics g = game.getGraphics();
         g.drawPixmap(Assets.fishBoy,  (int) boy.position.x, (int) boy.position.y);
         g.drawPixmap(Assets.fish, 270, (int) 270);
         
         //clouds
         clouds.drawClouds();
         
         //debug
       	Paint paint = new Paint();
         paint.setColor(Color.rgb(0, 0,255));
         paint.setStyle(Style.STROKE);
         game.getGraphics().drawRect(boy.getBounds(), paint);
     }
    
     
     private void drawRunningUI() {
         Graphics g = game.getGraphics();
         Vector2 lifePos = new Vector2(g.getWidth()/2 - 50,20);
         for (int i=0 ; i < lifes; i++ ){
         	g.drawPixmap(Assets.heart,(int) lifePos.x, (int) lifePos.y);
         	lifePos.x += (float) Assets.heart.getWidth();
         }
     }
 
     
     public void drawText(Graphics g, String line, int x, int y) {
         int len = line.length();
         for (int i = 0; i < len; i++) {
             char character = line.charAt(i);
 
             if (character == ' ') {
                 x += 20;
                 continue;
             }
 
             int srcX = 0;
             int srcWidth = 0;
             if (character == '.') {
                 srcX = 200;
                 srcWidth = 10;
             } else {
                 srcX = (character - '0') * 20;
                 srcWidth = 20;
             }
 
             g.drawPixmap(Assets.numbers, x, y, srcX, 0, srcWidth, 32);
             x += srcWidth;
         }
     }
     
     @Override
     public void pause() {
 
     }
 
     @Override
     public void resume() {
         
     }
 
     @Override
     public void dispose() {
     	Assets.theme.stop();
     	
     }
 }

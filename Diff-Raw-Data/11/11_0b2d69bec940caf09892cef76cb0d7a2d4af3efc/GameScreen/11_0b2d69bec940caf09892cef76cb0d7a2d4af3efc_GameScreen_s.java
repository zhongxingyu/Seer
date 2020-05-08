 package com.jeremyP.diseasedefense;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.annotation.SuppressLint;
 import android.graphics.Color;
 
 import com.jeremyP.diseasedefense.framework.Game;
 import com.jeremyP.diseasedefense.framework.Graphics;
 import com.jeremyP.diseasedefense.framework.Input.TouchEvent;
 import com.jeremyP.diseasedefense.framework.Screen;
 
 public class GameScreen extends Screen {
 	private Character character;
 	private ArrayList<Enemy> enemy;
 	private int enemyindex;
 	private EnemyPool enemies;
 	private GameState state;
 	private Graphics g;
 	private int timer;
 	private int min;
 	private int xOffset;
 	private int xEnemyOffset;
 	private int xCoord;
 	private int yCoord;
 	//private int enemiesKilled;
 	//private int enemySpeed = 1;
 	private int numEnemies = 1;
 	//private int enemyHealth = 1;
 	private Level level;
 
 	enum GameState {
 		Ready, Running, Paused, GameOver, Beaten
 	}
 
 	@SuppressLint("UseValueOf")
 	public GameScreen(Game game) {
 		super(game);
 		state = GameState.Running;
 		level = new Level();
 		g = game.getGraphics();
 		character = new Character(g);
 		enemy = new ArrayList<Enemy>();
 		enemy.add(new Enemy(g, Assets.badGuys[level.whatLevel()-1], 1, 1, 1));	//Add one L1 enemy
 		enemyindex = 0;
 		enemies = new EnemyPool(g, character, numEnemies, 1);
 		timer = 0;
 		min = 0;
 		xCoord = character.getCoords().getX();
 		yCoord = character.getCoords().getY();
 		//enemiesKilled = 0;
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 		game.getInput().getKeyEvents();
 
 		if (state == GameState.Running) {
 			updateRunning(touchEvents, deltaTime);
 		}
 
 		if (state == GameState.GameOver) {
 			updateGameOver(touchEvents);
 		}
 		
 		if (state == GameState.Beaten){
 			updateBeaten(touchEvents);
 		}
 
 		if (state == GameState.Paused) {
 			updatePaused(touchEvents);
 		}
 	}
 
 	private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			int x = event.x;
 			int y = event.y;
 
 			//Moves your character to where user dragged character
 			if (event.type == TouchEvent.TOUCH_DRAGGED) {
 				if (character != null) {
 					character.update(x, y);
 				}
 			}
 
 			//Paused game
 			if (event.type == TouchEvent.TOUCH_UP) {
 				if (event.x < 64 && event.y < 64) {
 					if (Settings.soundEnabled)
 						Assets.click.play(1);
 					state = GameState.Paused;
 					return;
 				}
 			}
 		}
 
 		//Move enemy towards the character
 		//if (enemy != null && character != null) {
 		if (enemyindex != -1 && character != null) {
 			enemy.get(enemyindex).update(character.getCoords());
 		}
 
 		//The enemy has been hit
 		//if (enemy != null && character != null && character.getFlyingState() && enemy.hasCollided(character.getWeapon().getOrigin())) {
 		if (enemyindex != -1 && character != null && character.getFlyingState() && enemy.get(enemyindex).hasCollided(character.getWeapon().getOrigin())) {
 			enemy.get(enemyindex).getHit();
 			character.stopFlying();
 			if (enemy.get(enemyindex).isDead()) {
 				//enemy = null;
 				character.stopFlying();
 				//enemiesKilled += 1;
 				level.addScore(enemy.get(enemyindex).getScore());
 				enemyindex = -1;
 				
 				//You've beaten the game
 				if(level.isEnd()){
 					state = GameState.Beaten;
 				}
 				//You've gone to the next level
 				else if(level.isChanged()){
 					int enemySum = enemy.size();
 					
 					//Create new level enemy
 					//int speed = enemy.get(enemySum-1).getSpeed() +1;
 					int speed = 1;
 					//TODO: Figure out how to make different badGuys go faster than others
 					int health = enemy.get(enemySum-1).getHealth() +1;
 					int points = enemy.get(enemySum-1).getSpeed() +1;
 					
 					//TODO: Add more new enemy types into the assets
 					//This if statement will stay here until more enemy variations are created
 					if(level.whatLevel() < 3){
 						Enemy newenemy = new Enemy(g, Assets.badGuys[level.whatLevel()-1] ,speed, health, points);
 						
 						//Add new level enemy
 						int moreEnemies = enemySum * 2;
 						for(int i=0; i < moreEnemies; i++){
 							enemy.add(newenemy);
 						}
 					}
 					
 				}
 			}
 		}
 
 		//Enemy has hit character
 		//if (enemy != null && character != null && character.hasCollided(enemy.getCoords())) {
 		if (enemyindex != -1 && character != null && character.hasCollided(enemy.get(enemyindex).getCoords())) {
 			//enemy = null;
 			enemyindex = -1;
 			character.getHit();
 			if (character.getHealth() <= 0) {
 				character = null;
 				state = GameState.GameOver;
 			}
 		}
 
 		//Create new enemy after he's dead
		if(enemyindex == -1){
 			createEnemy();
 		}
 		/*if (enemy == null && timer % 100 == 0) {
 			if (enemiesKilled % 30 == 0 && enemiesKilled != 0) {
 				enemyHealth += 1;
 				numEnemies += 1;
 				newAnimation = true;
 				createEnemy();
 			}else {
 				createEnemy();
 			}
 		}*/
 
 		timer += 1;
 	}
 
 	private void updateGameOver(List<TouchEvent> touchEvents) {
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 				if (event.x >= 128 && event.x <= 192 && event.y >= 200 && event.y <= 264) {
 					g.clear(0);
 					if (Settings.soundEnabled) {
 						Assets.click.play(1);
 					}
 					game.setScreen(new MainMenuScreen(game));
 					return;
 				}
 			}
 		}
 	}
 	
 	//TODO: Calculate what happens when the users wins!
 	private void updateBeaten(List<TouchEvent> touchEvents){
 		;
 	}
 
 	private void updatePaused(List<TouchEvent> touchEvents) {
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 				
 				if (event.x > 80 && event.x <= 240) {
 					
 					if (event.y > 100 && event.y <= 148) {
 						if (Settings.soundEnabled)
 							Assets.click.play(1);
 						state = GameState.Running;
 						return;
 					}
 					
 					if (event.y > 148 && event.y < 196) {
 						if (Settings.soundEnabled)
 							Assets.click.play(1);
 						g.clear(0);
 						game.setScreen(new MainMenuScreen(game));
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	public void drawGameOver() {
 		g.clear(0);
 		g.drawPixmap(Assets.gameover, -15, 100);
 	}
 	
 	public void drawBeaten(){
 		g.clear(0);
 		g.drawPixmap(Assets.youWin, 0, 75);
 		g.drawPixmap(Assets.contin, 75, 200);
 	}
 
 	public void drawRunning() {
 		drawBackground();
 		drawPauseButton();
 
 		//Draw character health
 		for (int i = 0; i < character.getHealth(); i++) {
 			g.drawRect(xOffset + 10, g.getHeight() - 20, 25, 25, Color.BLUE);
 			xOffset += 50;
 		}
 
 		//Draw enemy health
		/*if (enemy != null) {
			for (int i = 0; i < enemy.getHealth(); i++) {
 				g.drawRect(xEnemyOffset + 275, 20, 25, 25, Color.GREEN);
 				xEnemyOffset -= 50;
 			}
		}*/
 
 		//if (enemy != null) {
 		if(enemyindex != -1){
 			enemy.get(enemyindex).present();
 		}
 
 		if (character != null) {
 			character.present();
 		}
 		xOffset = 0;
 		xEnemyOffset = 0;
 
 		//drawText(g, Integer.toString(enemiesKilled), g.getWidth() - 60, g.getHeight() - 35);
 		drawText(g, Integer.toString(level.getScore()), g.getWidth() - 60, g.getHeight() - 35);
 
 	}
 
 	/**
 	 * NOT BEING USED
 	 * @param enemy
 	 */
 	/*public void enemyCheck(Enemy enemy) {
 		if (enemy != null && character != null) {
 			enemy.update(character.getCoords());
 		}
 
 		if (enemy != null && character != null && character.getFlyingState() && enemy.hasCollided(character.getWeapon().getOrigin())) {
 			enemy.getHit();
 			character.stopFlying();
 			if (enemy.getHealth() <= 0) {
 				enemy = null;
 				character.stopFlying();
 				enemiesKilled += 1;
 				level.addScore();
 				//Check if level has changed
 			}
 		}
 
 		if (enemy != null && character != null && character.hasCollided(enemy.getCoords())) {
 			enemy = null;
 			character.getHit();
 			if (character.getHealth() <= 0) {
 				character = null;
 				state = GameState.GameOver;
 			}
 		}
 
 		if (enemy == null && timer % 100 == 0) {
 			if (enemiesKilled % 30 == 0 && enemiesKilled != 0) {
 				enemyHealth += 1;
 				numEnemies += 1;
 				newAnimation = true;
 				createEnemy();
 			} else {
 				createEnemy();
 			}
 		}
 
 		timer += 1;
 	}*/
 
 	public void createEnemy() {
 		//enemy = new Enemy(g, enemySpeed, enemyHealth, newAnimation);
 		
 		enemyindex = (new Random()).nextInt(enemy.size());
 		xCoord = character.getCoords().getX();
 		yCoord = character.getCoords().getY();
 
 		while (xCoord == character.getCoords().getX() && yCoord == character.getCoords().getY()) {
 			xCoord = min + (int) (Math.random() * ((g.getWidth() - min) + 1));
 			yCoord = min + (int) (Math.random() * ((g.getHeight() - min) + 1));
 		}
 
 		enemy.get(enemyindex).setCoords(xCoord, yCoord);
 	}
 
 	@Override
 	public void present(float deltaTime) {
 		if (state == GameState.GameOver) {
 			drawGameOver();
 		}
 
 		if(state == GameState.Beaten){
 			drawBeaten();
 		}
 		
 		if (state == GameState.Running) {
 			drawRunning();
 		}
 
 		if (state == GameState.Paused) {
 			drawPaused();
 		}
 
 	}
 
 	private void drawPaused() {
 		Graphics g = game.getGraphics();
 
 		g.drawPixmap(Assets.pauseMenu, 80, 100);
 	}
 
 	private void drawPauseButton() {
 		g.drawPixmap(Assets.pauseButton, 1, 1);
 	}
 
 	/**
 	 * draw the Background image of the current level
 	 */
 	private void drawBackground() {
 		/*if (enemiesKilled < 15) {
 			g.drawPixmap(Assets.levelBackground1, 0, 0);
 		}else if(enemiesKilled < 30){
 			g.drawPixmap(Assets.levelBackground2, 0, 0);
 		}else if(enemiesKilled < 45){
 			g.drawPixmap(Assets.levelBackground3, 0, 0);
 		}else if(enemiesKilled < 60){
 			g.drawPixmap(Assets.levelBackground4, 0, 0);
 		}else if(enemiesKilled < 75){
 			g.drawPixmap(Assets.levelBackground5, 0, 0);
 		}else if(enemiesKilled < 90){
 			g.drawPixmap(Assets.levelBackground6, 0, 0);
 		}*/
 		g.drawPixmap(Assets.levelBackground[level.whatLevel()-1], 0, 0);
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
 
 	}
 }

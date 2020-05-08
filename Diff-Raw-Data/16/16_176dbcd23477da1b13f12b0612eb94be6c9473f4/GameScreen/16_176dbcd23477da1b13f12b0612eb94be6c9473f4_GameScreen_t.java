 package com.buildcoolrobots.games.pgcgame.Screens;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import me.pagekite.glen3b.gjlib.ExtendedLabel;
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseGameSprite;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseScreen;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.DPad;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.StateManager;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.GameImage;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ScreenType;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.ShipTypes;
 import com.buildcoolrobots.games.pgcgame.Ships.BaseShip;
 import com.buildcoolrobots.games.pgcgame.Ships.Enemies.BaseEnemyShip;
 
 public class GameScreen extends BaseScreen {
 	
 	Texture[] EnemyTextures = new Texture[2];
 	
 	public static DPad Dpad;
 	
 	ArrayList<BaseEnemyShip> enemies;
 	BaseShip Ship;
 	BaseGameSprite FireButton;
 	BaseGameSprite PauseButton;
 	ExtendedLabel xy;
 	String coor = "0,0";
 	
 	SpriteManager _allSprites;
 	
 	float timeSinceLastFire = 0;
 	final float fireDelay = 0.150f;
 	
 	float timeSinceLastEnemySpawn = 0;
 	final float enemySpawnTimer = 5;
 	
 	public GameScreen(SpriteManager allSprites, SpriteBatch spriteBatch, ScreenType screenType) {
 		super(allSprites, spriteBatch, screenType);
 		
 		EnemyTextures[0] = ShipTypes.ENEMYDRONE1.GameTexture();
 		EnemyTextures[1] = ShipTypes.ENEMYDRONE2.GameTexture();
 		
 		enemies = new ArrayList<BaseEnemyShip>();
 		Ship = new BaseShip(new Vector2(100, Gdx.graphics.getHeight()/2 - ShipTypes.PLAYERSHIP.GameTexture().getHeight()/2), ShipTypes.PLAYERSHIP.GameTexture(), allSprites);
 		Dpad = new DPad();
 		FireButton = new BaseGameSprite(GameImage.FIREBUTTON.ImageTexture());
 		PauseButton = new BaseGameSprite(GameImage.PAUSEBUTTON.ImageTexture());
 		PauseButton.setPosition(Gdx.graphics.getWidth() - PauseButton.getWidth(), Gdx.graphics.getHeight() - PauseButton.getHeight());
 		
 		xy = new ExtendedLabel(coor , GameImage.DEBUGFONT.ImageText());
 		xy.setPosition(25, Gdx.graphics.getHeight() - 50);
 		//xy.setFontScale(2,2);
 		
 		allSprites.add(xy);
 		allSprites.add(Ship);
 		allSprites.add(Dpad);
 		allSprites.add(FireButton);
 		allSprites.add(PauseButton);
 		
 		//Save allSprites reference
 		_allSprites = allSprites;
 		
 		
 	}
 	
 	Random randomNum = new Random();
 	
 	public void update(float deltaTime) {
 		super.update(deltaTime);
 		timeSinceLastFire += deltaTime;
 		timeSinceLastEnemySpawn += deltaTime;
 		
 		if (timeSinceLastEnemySpawn >= enemySpawnTimer) {
 			BaseEnemyShip enemy = new BaseEnemyShip(Vector2.Zero, EnemyTextures[randomNum.nextInt(2)],getAllSprites());
			enemy.setPosition(Gdx.graphics.getWidth(), randomNum.nextInt(Gdx.graphics.getHeight() - 2*(int)enemy.getHeight() - (int)enemy.getHeight()) + (int)enemy.getHeight());
 			enemies.add(enemy);
 			enemies.get(enemies.size()-1).xSpeed = -3;
 		    _allSprites.add(enemy);
 		    timeSinceLastEnemySpawn = 0;
 		}
 		
 		for(int i = 0; i < enemies.size(); i++){
 			enemies.get(i).Update();
 		}
 		
 		if (Gdx.input.isTouched()) {
 			coor = "" + Gdx.input.getX() + "," + (Gdx.graphics.getHeight() - Gdx.input.getY());
 			xy.setText(coor + "\ndir: " + Dpad.shipDirection.toString());
 			
 			if (!SettingsScreen.leftyMode) {
 				FireButton.setPosition(Gdx.graphics.getWidth() - FireButton.getWidth(),0);
 			} else {
 				FireButton.setPosition(0, 0);
 			}
 			
 	        
 		}
 
 		Ship.move(Dpad.shipDirection);		
 		
		boolean isShooting = false;
 		for (int i = 0; i < 2; i++) {
 			if (Gdx.input.isTouched(i) &&
 					Gdx.input.getX(i) >= FireButton.getX() && Gdx.input.getX(i) <= FireButton.getX() + FireButton.getWidth() &&
 					Gdx.graphics.getHeight() - Gdx.input.getY(i) >= FireButton.getY() && Gdx.graphics.getHeight() - Gdx.input.getY(i) <= FireButton.getY() + FireButton.getHeight()) {
 					
 					if(timeSinceLastFire >= fireDelay) {
 						Ship.shoot();
 						timeSinceLastFire = 0;
 					}
					isShooting = true;
 				}		
 		}
 		
		if (isShooting) {
			FireButton.setColor(Color.PINK);
		} else {
			FireButton.setColor(Color.WHITE);
		}
		
 		if (Gdx.input.isTouched() &&
 				Gdx.input.getX() >= PauseButton.getX() && Gdx.input.getX() <= PauseButton.getX() + PauseButton.getWidth() &&
 				Gdx.graphics.getHeight() - Gdx.input.getY() >= PauseButton.getY() && Gdx.graphics.getHeight() - Gdx.input.getY() <= PauseButton.getY() + PauseButton.getHeight()) {
 			StateManager.SwitchScreen(ScreenType.PAUSESCREEN);
 		}
 	}
 }

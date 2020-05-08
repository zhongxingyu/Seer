 package com.glowman.spaceunit.game.mapObject.enemy;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.game.strategy.GameStrategy;
 
 /**
  *
  */
 public class EnemyFactory {
 
	private static int _gameType = -1;

	public static void setGameType(int gameType) { _gameType = gameType; }

 	//TODO game balance here
	public static Enemy createEnemy() {

		if (_gameType == -1) { throw new Error("game type not inited!"); }
 		Enemy result;
 		double random = Math.random();
		if (random < .33 || _gameType == GameStrategy.RUN_GAME) {
 			Sprite image = new Sprite(Assets.soImages[Math.round((float)Math.random())]);
 			result = new StupidEnemy(image);
 		} else if (random < .63) {
 			result = new Mine();
 		} else {
 			result = new Alien();
 		}
 
 		return result;
 	}
 }

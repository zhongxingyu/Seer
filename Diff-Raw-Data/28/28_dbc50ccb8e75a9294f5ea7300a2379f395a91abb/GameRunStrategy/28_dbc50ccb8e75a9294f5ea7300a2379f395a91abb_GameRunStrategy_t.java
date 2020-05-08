 package com.glowman.spaceunit.game.strategy;
 
 import android.util.Log;
 import com.badlogic.gdx.math.Vector2;
 import com.glowman.spaceunit.game.core.TouchEvent;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.Ship;
import com.glowman.spaceunit.game.mapObject.enemy.EnemyFactory;
 
 import java.lang.Math;
 import java.util.ArrayList;
 
 /**
  *
  */
 public class GameRunStrategy extends GameStrategy {
 
 	public GameRunStrategy(Ship ship)
 	{
 		super(ship);
		EnemyFactory.setGameType(GameStrategy.RUN_GAME);
 	}
 
 	@Override
 	public void tick(float delta)
 	{
 		super.tick(delta);
 		super._heroShip.tick(delta);
 
 		//TODO set game balance here
 		if (Math.random() < .03) { this.createEnemy(); }
 		if (_enemies != null)
 		{
 			for (Enemy enemy : _enemies)
 			{
 				enemy.tick(delta);
 			}
 		}
 		if (_enemies != null)
 		{
 			this.checkEnemyHits();
 		}
 	}
 
 	@Override
 	public void touchDown(TouchEvent touch) {
 		_heroShip.setTargetPosition(new Vector2(touch.x, touch.y));
 		_heroShip.setMoving(true);
 	}
 
 	@Override
 	public void touchUp(TouchEvent touch) {
 
 		_heroShip.setMoving(false);
 	}
 
 	@Override
 	public void touchMove(TouchEvent touch) {
 
 		_heroShip.setTargetPosition(new Vector2(touch.x, touch.y));
 	}
 
 
 	private void checkEnemyHits()
 	{
 		ArrayList<Enemy> enemiesForExplosion = new ArrayList<Enemy>();
 		float distance;
 		float radius1, radius2;
 		Vector2 position1, position2;
 		for (int i = 0; i < (_enemies.size() - 1); ++i) {
 			for (int j = i + 1; j < _enemies.size(); ++j) {
 				radius1 = _enemies.get(i).getHeight()/2;
 				radius2 = _enemies.get(j).getHeight()/2;
 				position1 = _enemies.get(i).getCenterPosition();
 				position2 = _enemies.get(j).getCenterPosition();
 
 				distance = position1.dst(position2);
 				if (distance < radius1 + radius2)
 				{
 					enemiesForExplosion.add(_enemies.get(i));
 					enemiesForExplosion.add(_enemies.get(j));
 				}
 			}
 		}
 
 		for (Enemy enemy : enemiesForExplosion)
 		{
 			super.explodeEnemy(enemy);
 		}
 	}
 
 }

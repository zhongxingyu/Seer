 package com.glowman.spaceunit.game.strategy;
 
 import android.util.Log;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 
 import com.badlogic.gdx.math.Vector3;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.CoordinatesTranslator;
 import com.glowman.spaceunit.core.TouchEvent;
 import com.glowman.spaceunit.game.SpeedFactory;
 import com.glowman.spaceunit.game.mapObject.Bullet;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.MovingSpaceObject;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.enemy.EnemyFactory;
 
 import java.util.ArrayList;
 
 /**
  *
  */
 public class GameShootStrategy extends GameStrategy {
 
 	public static final float BULLET_SPEED = 5;
 
 	private ArrayList<Bullet> _bullets;
 
 	public GameShootStrategy(Ship ship)
 	{
 		super(ship);
 		EnemyFactory.setGameType(GameStrategy.SHOOT_GAME);
 	}
 
 	@Override
 	public ArrayList<Bullet> getBullets() {
 		return _bullets;
 	}
 
 	@Override
 	public void tick(float delta)
 	{
 		super.tick(delta);
 		_heroShip.tick(delta);
 
 		if (_heroShip.isReadyForShoot())
 		{
 			this.shootBullet();
 		}
 
 		if (_bullets != null)
 		{
 			for (Bullet bullet : _bullets)
 			{
 				bullet.move();
 			}
 			this.checkBulletsForRemove();
 			this.checkBulletsHit();
 		}
 
 		//TODO set game balance here
 		if (Math.random() < 0.3f) { this.createEnemy(); }
 		if (_enemies != null)
 		{
 			for(MovingSpaceObject enemy : _enemies)
 			{
 				enemy.tick(delta);
 			}
 		}
 	}
 
 	@Override
 	public void touchUp(TouchEvent touch){
 		if (touch.pointer == _movingTouch)
 		{
 			_movingTouch = _shootingTouch;
 		}
 		_heroShip.setShooting(false);
 		_shootingTouch = -1;
 		_heroShip.setMoving(_movingTouch != -1);
 	}
 
 	@Override
 	public void touchDown(TouchEvent touch){
 		if (_movingTouch != -1)
 		{
 			_shootingTouch = touch.pointer;
 			_heroShip.setTargetForShooting(new Vector2(touch.x, touch.y));
 			_heroShip.setShooting(true);
 		}
 		else
 		{
 			_movingTouch = touch.pointer;
 			_heroShip.setTargetPosition(new Vector2(touch.x, touch.y));
 			_heroShip.setMoving(true);
 		}
 
 	}
 	@Override
 	public void touchMove(TouchEvent touch){
 		if (_movingTouch == touch.pointer)
 		{
 
 			_heroShip.setTargetPosition(new Vector2(touch.x, touch.y));
 		}
 
 	}
 
 	@Override
 	protected Enemy createEnemy()
 	{
 		Enemy enemy = super.createEnemy();
 		enemy.setRandomBorderPosition();
 		enemy.setGeneralSpeed(SpeedFactory.getSpeed(enemy, GameStrategy.RUN_GAME));
 		enemy.setRotationSpeed(5 * ((float)Math.random() * 2 - 1)); //TODO kick it out
 		enemy.moveTo((float)Math.random() * Assets.VIRTUAL_WIDTH,
 						(float)Math.random() * Assets.VIRTUAL_HEIGHT);
		enemy.setTarget(_heroShip);
 		return enemy;
 	}
 
 	private void shootBullet()
 	{
 		if (_bullets == null)
 		{
 			_bullets = new ArrayList<Bullet>();
 		}
 
 		if (super._shootingTouch == -1) { throw new Error("shooting touch not exist!!"); }
 
 		int touchX = Gdx.input.getX(_shootingTouch);
 		int touchY = Gdx.input.getY(_shootingTouch);
 
 		Vector3 targetPoint =  CoordinatesTranslator.toVirtualView(touchX, touchY);
 
 		Vector2 bulletPosition = _heroShip.getPosition();
 
 		float dx = targetPoint.x - bulletPosition.x;
 		float dy = targetPoint.y - bulletPosition.y;
 		float h = (float)Math.sqrt(dx * dx + dy * dy);
 		float vx = 0;
 		float vy = 0;
 		if (h != 0){
 			vx = dx / h;
 			vy = dy / h;
 		}
 
 		Sprite bulletView = new Sprite(Assets.bullet);
 		Bullet bullet = new Bullet(bulletView, BULLET_SPEED, vx, vy);
 		bullet.getView().setPosition(bulletPosition.x, bulletPosition.y);
 		_bullets.add(bullet);
 	}
 
 	private void checkBulletsForRemove()
 	{
 		float bulletX;
 		float bulletY;
 		ArrayList<Bullet> bulletsForRemove = new ArrayList<Bullet>();
 		for(Bullet bullet : _bullets)
 		{
 			bulletX = bullet.getView().getX();
 			bulletY = bullet.getView().getY();
 			if ((bulletX < 0) ||
 					(bulletX > Assets.VIRTUAL_WIDTH) ||
 					(bulletY < 0) ||
 					(bulletY > Assets.VIRTUAL_HEIGHT))
 			{
 				Log.d("hz", "remove bullet!");
 				bulletsForRemove.add(bullet);
 			}
 		}
 
 		for (Bullet bullet : bulletsForRemove)
 		{
 			_bullets.remove(bullet);
 		}
 	}
 
 	private void checkBulletsHit()
 	{
 		if (_enemies == null) { return; }
 		ArrayList<Enemy> enemiesForExplosion = new ArrayList<Enemy>();
 		ArrayList<Bullet> bulletsForRemove = new ArrayList<Bullet>();
 		float distance;
 		float enemyRadius;
 		Vector2 enemyPosition;
 
 		for(Bullet bullet : _bullets)
 		{
 			for (int i = 0; i < _enemies.size(); ++i) {
 				enemyRadius = (_enemies.get(i)).getHeight()/2;
 				enemyPosition = _enemies.get(i).getPosition();
 
 				distance = enemyPosition.dst(bullet.getView().getX(), bullet.getView().getY());
 				if (distance < enemyRadius)
 				{
 					enemiesForExplosion.add(_enemies.get(i));
 					bulletsForRemove.add(bullet);
 				}
 			}
 		}
 
 		for(Enemy enemy : enemiesForExplosion)
 		{
 			super.explodeEnemy(enemy);
 		}
 		enemiesForExplosion.clear();
 
 		for(Bullet bullet : bulletsForRemove)
 		{
 			_bullets.remove(bullet);
 		}
 		bulletsForRemove.clear();
 	}
 
 
 }

 package com.glowman.spaceunit.game.strategy;
 
 import android.util.Log;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 
 import com.badlogic.gdx.math.Vector3;
 import com.glowman.spaceunit.Assets;
 import com.glowman.spaceunit.CoordinatesTranslator;
 import com.glowman.spaceunit.core.TouchEvent;
 import com.glowman.spaceunit.game.IShooter;
 import com.glowman.spaceunit.game.Shooter;
 import com.glowman.spaceunit.game.balance.EnemySetCollector;
 import com.glowman.spaceunit.game.balance.SpeedCollector;
 import com.glowman.spaceunit.game.mapObject.Bullet;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.Ship;
 import com.glowman.spaceunit.game.mapObject.enemy.EnemyFactory;
 
 import java.util.ArrayList;
 
 /**
  *
  */
 public class GameShootStrategy extends GameStrategy {
 
 	private IShooter _shooter;
 
 	public GameShootStrategy(Ship ship)
 	{
 		super(ship);
 		_shooter = new Shooter();
 		EnemyFactory.init(GameStrategy.SHOOT_GAME, ship, _shooter);
 		_availableEnemyTypes = EnemySetCollector.getEnemySet(GameStrategy.SHOOT_GAME);
 	}
 
 	@Override
 	public Sprite[] getDrawableObjects() {
 		Sprite[] basicObjects = super.getDrawableObjects();
		int bulletSize = _shooter.getBullets() == null ? 0 : _shooter.getBullets().size();
		int length = basicObjects.length + bulletSize;
 		Sprite[] result = new Sprite[length];
 		int i = 0;
 		for (i = 0; i < basicObjects.length; ++i) {
 			result[i] = basicObjects[i];
 		}
 		for (Bullet bullet : _shooter.getBullets()) {
 			result[i] = bullet.getImage();
 			++i;
 		}
 		return result;
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
 
 		if (_shooter.getBullets() != null)
 		{
 			for (Bullet bullet : _shooter.getBullets())
 			{
 				bullet.tick(delta);
 			}
 			this.checkBulletsForRemove();
 			this.checkBulletsHit();
 			this.checkHeroHit();
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
 	protected void setEnemyParams(Enemy enemy)
 	{
 		enemy.setRandomBorderPosition();
 		enemy.setGeneralSpeed(SpeedCollector.getEnemySpeed(enemy.getEnemyType(), GameStrategy.RUN_GAME));
 		enemy.setRotationSpeed(5 * ((float)Math.random() * 2 - 1)); //TODO kick it out
 		enemy.moveTo((float) Math.random() * Assets.VIRTUAL_WIDTH,
 				(float) Math.random() * Assets.VIRTUAL_HEIGHT);
 		enemy.setTarget(_heroShip);
 	}
 
 	private void shootBullet()
 	{
 		if (super._shootingTouch == -1) { throw new Error("shooting touch not exist!!"); }
 
 		int touchX = Gdx.input.getX(_shootingTouch);
 		int touchY = Gdx.input.getY(_shootingTouch);
 
 		Vector3 targetPoint =  CoordinatesTranslator.toVirtualView(touchX, touchY);
 		Vector2 bulletPosition = _heroShip.getPosition();
 
 		_shooter.shoot(bulletPosition, new Vector2(targetPoint.x, targetPoint.y));
 	}
 
 	private void checkBulletsForRemove()
 	{
 		float bulletX;
 		float bulletY;
 		ArrayList<Bullet> bulletsForRemove = new ArrayList<Bullet>();
 		for(Bullet bullet : _shooter.getBullets())
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
 			_shooter.getBullets().remove(bullet);
 		}
 	}
 
 	private void checkBulletsHit()
 	{
 		if (_enemies == null) { return; }
 		ArrayList<Enemy> enemiesForExplosion = new ArrayList<Enemy>();
 		ArrayList<Bullet> bulletsForRemove = new ArrayList<Bullet>();
 
 		for(Bullet bullet : _shooter.getBullets())
 		{
 			for (int i = 0; i < _enemies.size(); ++i) {
 				if (bullet.getOwner() == _enemies.get(i)) continue;
 
 				if (super.checkObjectsHit(_enemies.get(i), bullet)) {
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
 			_shooter.getBullets().remove(bullet);
 		}
 		bulletsForRemove.clear();
 	}
 
 	private void checkHeroHit() {
 		Enemy enemyToExplode = null;
 		for (Enemy enemy : _enemies) {
 			if (super.checkObjectsHit(_heroShip, enemy)) {
 				enemyToExplode = enemy;
 				super.gameOver();
 				break;
 			}
 		}
 		if (enemyToExplode != null) {
 			super.explodeEnemy(enemyToExplode);
 			super.explodeHero();
 		}
 		else {
 			for (Bullet bullet : _shooter.getBullets()) {
 				if (bullet.getOwner() != _heroShip &&
 						super.checkObjectsHit(_heroShip, bullet)) {
 					super.explodeHero();
 					super.gameOver();
 				}
 			}
 		}
 	}
 
 
 }

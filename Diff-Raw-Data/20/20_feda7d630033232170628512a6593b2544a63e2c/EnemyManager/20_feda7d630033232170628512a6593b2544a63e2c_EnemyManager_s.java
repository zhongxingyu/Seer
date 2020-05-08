 package ch.zhaw.arsphema.controller;
 
 import ch.zhaw.arsphema.model.Explosion;
 import ch.zhaw.arsphema.model.Hero;
 import ch.zhaw.arsphema.model.enemies.AbstractEnemy;
 import ch.zhaw.arsphema.model.enemies.EnemyFactory;
 import ch.zhaw.arsphema.model.enemies.EnemyGroup;
 import ch.zhaw.arsphema.model.shot.Shot;
 
 import com.badlogic.gdx.utils.Array;
 
 public class EnemyManager {
 
 	private Array<EnemyGroup> groups, killedGroups;
 	private Array<AbstractEnemy> killedEnemies;
 	private Array<Explosion> explosions;
 	private static EnemyFactory enemyFactory;
 	private static boolean dropEnemies = false;
 	private float nextEnemyToDrop = 0;
 	//set to hard so the calculation creates easy groups at the beginning
 	private int groupDiffcultyOne = 2; // latest deployment
 	private int groupDiffcultyTwo = 2; // second latest deployment
 	private int groupDiffcultyThree = 2; // third latest deployment
 	private PowerUpManager pum;
 	
 	
 	public EnemyManager(PowerUpManager pum)
 	{
 		killedEnemies = new Array<AbstractEnemy>();
 		enemyFactory = EnemyFactory.getInstance();
 		groups = new Array<EnemyGroup>();
 		killedGroups = new Array<EnemyGroup>();
 		explosions = new Array<Explosion>();
 		this.pum = pum;
 	}
 	
 	public void computeEnemyMovements(float delta) {
 		for (EnemyGroup group : groups) {
 			if (group.move(delta)){
 				killedEnemies.addAll(group.getMembers());
 			}
 			removeEnemies(group);
 		}
 		removeEnemyGroups();
 		removeExplosions();
 	}
 
 	public void colideWithHero(final Hero hero) {
 		for (final EnemyGroup group : groups) {
 			if(group.overlaps(hero)){
 				for (final AbstractEnemy enemy : group.getMembers()) {
 					if (enemy.overlaps(hero)) {
 						killedEnemies.add(enemy);
 						hero.lowerHealth(enemy.getCollisionDamage());
 					}
 				}
 				removeEnemies(group);
 			}
 		}
 	}
 
 	/**
 	 * @param shotManager
 	 * @return points earned in this round
 	 */
 	public int killEnemies(final ShotManager shotManager) {
 		int totalPoints = 0;
 		for(Shot shot : shotManager.getHeroShots())
 		{
 			//shot overlaps with group
 			for (final EnemyGroup group : groups) {
 				if(shot.overlaps(group)){
 					for(AbstractEnemy enemy : group.getMembers()){
 						if(shot.overlaps(enemy))
 						{
 							if(enemy.lowerHealth(shot.getDamage())){
 								totalPoints += enemy.getBasePoints();
 								killedEnemies.add(enemy);
 								explosions.add(new Explosion(enemy.x+enemy.width/2,enemy.y+enemy.height/2));
 								pum.createPowerUp(enemy.x, enemy.y); // create a power up
 								//remove enemy from group
 							}
 							shotManager.getShotsToRemove().add(shot);
 						}
 					}
 					removeEnemies(group);
 					// no use for "continue" since groups can overlap
 				}
 			}
 		}
 //		if(enemies.size == 0){ //TODO maybe add
 //			nextEnemyToDrop /= 2; //if all enemies killed, next drop of enemy is sooner
 //		}
 		return totalPoints;
 	}
 
 	private void removeEnemies(final EnemyGroup group) {
 		for(final AbstractEnemy enemy : killedEnemies)
 		{
 			group.getMembers().removeValue(enemy, false);
 		}
 		killedEnemies.clear();
 	}
 	
 	private void removeEnemyGroups() {
 		for(final EnemyGroup group : groups){
 			if(group.getMembers().size == 0){
 				killedGroups.add(group);
 			}
 		}
 		for(final EnemyGroup enemyGroup : killedGroups){
 			groups.removeValue(enemyGroup, false);
 		}
 		killedEnemies.clear();
 	}
 
 	private void removeExplosions() {
 		for(final Explosion explosion : explosions)
 		{
 			if (explosion.isFinished())
 				explosions.removeValue(explosion, false);
 		}
 	}
 	
 	public void enemyShooting(final ShotManager shotManager, final float delta) {
 		for(final EnemyGroup group : groups){
 			for(final AbstractEnemy enemy : group.getMembers())
 			{
 				final Array<Shot> tempShot = enemy.shoot(delta);
 				if(tempShot != null)
 					shotManager.getEnemyShots().addAll(tempShot);
 			}
 		}
 	}
 
 	public void dropEnemies(float delta, float elapsed) {
 		if (dropEnemies) {
 			if(nextEnemyToDrop <= 0) {
 				addEnemies(elapsed);
 				// check elapsed time for next enemy set to drop 
 				// now after 100 sec every 0.5 sec new enemy should appear (approach is linea)
 				nextEnemyToDrop = 4 - (elapsed * 5 / 100);
 				if(nextEnemyToDrop < 0.5f)
 					nextEnemyToDrop = 0.5f;
 			} else {
 				nextEnemyToDrop -= delta;
 			}
 		}
 	}
 
 	private void addEnemies(final float elapsed) {
 		//assumption: beginning of only hard enemies @200secs
 		final int lastGroupsDifficulty = groupDiffcultyOne + groupDiffcultyTwo + groupDiffcultyThree;
 		final int nextGroupDiffLevel = calculateNextGroupDifficultyLevel(elapsed, lastGroupsDifficulty);
 		groupDiffcultyThree = groupDiffcultyTwo;
 		groupDiffcultyTwo = groupDiffcultyOne;
 		groupDiffcultyOne = nextGroupDiffLevel;
 		final EnemyGroup group = enemyFactory.createGroupByDifficultyLevel(nextGroupDiffLevel, elapsed);
 		final Array<AbstractEnemy> newEnemies = group.getMembers(); 
 		if (newEnemies != null) {
 			groups.add(group);
 		}
 	}
 	
	private int calculateNextGroupDifficultyLevel(final float elapsed, final int sumDifLevel)
 	{
		//TODO check
 		final float div;
 		if(sumDifLevel == 0)
 			div = 1;
 		else
 			div = 1 / sumDifLevel;
		float result = elapsed / 100 * div / 3; 
 		if(result > 2)
 			return 2;
 		return Math.round(result);
 	}
 	
 	public Array<AbstractEnemy> getKilledEnemies() {
 		return killedEnemies;
 	}
 
 	public void setKilledEnemies(Array<AbstractEnemy> killedEnemies) {
 		this.killedEnemies = killedEnemies;
 	}
 
 	public static void activateEnemyFactory(){
 		dropEnemies = true;
 	}
 	public static void deactivateEnemyFactory(){
 		dropEnemies = false;
 	}
 
 	public Array<Explosion> getExplosions() {
 		return explosions;
 	}
 
 	public void setExplosions(Array<Explosion> explosions) {
 		this.explosions = explosions;
 	}
 
 	public Array<EnemyGroup> getGroups() {
 		return groups;
 	}
 	
 }

 package com.glowman.spaceunit.game.mapObject.enemy.behaviour;
 
 import com.glowman.spaceunit.game.mapObject.SpaceObject;
 import com.glowman.spaceunit.game.mapObject.enemy.Enemy;
 import com.glowman.spaceunit.game.mapObject.enemy.behaviour.options.AlarmBehaviourOptions;
 import com.glowman.spaceunit.game.mapObject.enemy.behaviour.options.BehaviourOptions;
 
 /**
  *
  */
 public class EnemyAlarmBehaviour extends AEnemyBehaviourWithTarget {
 	private float distance;
 	private AEnemyBehaviour[] executeBehaviours;
 
 	public EnemyAlarmBehaviour(Enemy enemy, SpaceObject target, BehaviourOptions options) {
 		super(EnemyBehaviourNameENUM.ALARM, enemy, target);
 
 		if (!(options instanceof AlarmBehaviourOptions)) {
 			throw new Error("need only AlarmBehaviourOptions here");
 		}
 
 		AlarmBehaviourOptions alarmOptions = (AlarmBehaviourOptions) options;
 		distance = alarmOptions.distance;
 		executeBehaviours = alarmOptions.executeBehaviours;
 	}
 
 	@Override
 	public void tick(float delta) {
 		if (_enemy.getCenterPosition().dst(_target.getCenterPosition()) < distance) {

			for (AEnemyBehaviour behaviour : executeBehaviours) {
				if (!_enemy.hasBehaviour(behaviour.getName())) {
					_enemy.addBehaviour(behaviour);
				}
 			}
 		}
 		else {
			for (AEnemyBehaviour behaviour : executeBehaviours) {
				if (_enemy.hasBehaviour(behaviour.getName())) {
					_enemy.removeBehaviour(behaviour.getName());
				}
 			}
 		}
 
 	}
 }

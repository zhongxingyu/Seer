 package linewars.gamestate.mapItems.strategies.combat;
 
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.MapItem;
 import linewars.gamestate.mapItems.Turret;
 import linewars.gamestate.mapItems.Unit;
 import linewars.gamestate.mapItems.MapItemModifier.MapItemModifiers;
 import linewars.gamestate.mapItems.strategies.StrategyConfiguration;
 import linewars.gamestate.mapItems.strategies.turret.MinimumRangeTurretStrategy;
 import configuration.Usage;
 import editor.abilitiesstrategies.AbilityStrategyEditor;
 import editor.abilitiesstrategies.EditorProperty;
 import editor.abilitiesstrategies.EditorUsage;
 
 public class MoveBetweenShotsConfiguration extends CombatStrategyConfiguration {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5967291726281631003L;
 	
 	private static final String strategyName = "Move Between Shots";
 	
 	static{
 		StrategyConfiguration.setStrategyConfigMapping(strategyName, MoveBetweenShotsConfiguration.class, AbilityStrategyEditor.class);
 	}
 
 	private static final String cooldownName = "cooldown";
 	private static final Usage cooldownUsage = Usage.NUMERIC_FLOATING_POINT;
 	private static final EditorUsage cooldownEditorUsage = EditorUsage.PositiveReal;
 	private static final String cooldownDescription = "The amount of time between shots.";
 	
 	private static final String durationName = "duration";
 	private static final Usage durationUsage = Usage.NUMERIC_FLOATING_POINT;
 	private static final EditorUsage durationEditorUsage = EditorUsage.PositiveReal;
 	private static final String durationDescription = "The amount of time the Unit should remain in place after takig a shot.";
 	
 	public MoveBetweenShotsConfiguration(){
 		this.setPropertyForName(cooldownName, new EditorProperty(cooldownUsage, null, cooldownEditorUsage, cooldownDescription));
 		this.setPropertyForName(durationName, new EditorProperty(durationUsage, durationName, durationEditorUsage, durationDescription));
 	}
 	
 	@Override
 	public CombatStrategy createStrategy(MapItem m) {
 		return new MoveBetweenShots((Unit) m);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null || !(obj instanceof MoveBetweenShotsConfiguration)) return false;
 		MoveBetweenShotsConfiguration other = (MoveBetweenShotsConfiguration) obj;
 		if(other.getPropertyForName(cooldownName).getValue().equals(this.getPropertyForName(cooldownName).getValue())){
 			if(other.getPropertyForName(durationName).getValue().equals(this.getPropertyForName(durationName).getValue())){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public class MoveBetweenShots implements CombatStrategy{
 		
 		private Unit unit;
 		private double lastShotStartedTime;
 
 		public MoveBetweenShots(Unit owner) {
 			this.unit = owner;
 			lastShotStartedTime = 0;
 		}
 
 		@Override
 		public String name() {
 			return "Move Between Shots";
 		}
 
 		@Override
 		public CombatStrategyConfiguration getConfig() {
 			return MoveBetweenShotsConfiguration.this;
 		}
 
 		@Override
 		public double getRange() {
 			double maxTurretRange = 0;
 			for(Turret t : unit.getTurrets()){
 				if(t.getTurretStrategy().getRange() > maxTurretRange){
 					maxTurretRange = t.getTurretStrategy().getRange();
 				}
 			}
 			return maxTurretRange;
 		}
 
 		@Override
 		public void fight(Unit[] availableEnemies, Unit[] availableAllies) {
 			double duration = (Double) getPropertyForName(durationName).getValue();
 			double cooldown = (Double) getPropertyForName(cooldownName).getValue();
 			double currentTime = unit.getGameState().getTime();
 			
 			double fireRateModifier = unit.getModifier().getModifier(MapItemModifiers.fireRate);
 			duration /= fireRateModifier;
 			cooldown /= fireRateModifier;
 			
 			boolean firing = false;
 			boolean readyToFire = false;
 			if(currentTime < duration + lastShotStartedTime){
 				firing = true;
 			}else if(currentTime > cooldown + lastShotStartedTime){
 				readyToFire = true;
 			}
 			
 			if(firing){
 				return;
 			}
 			
 			//so we know that we are either moving or about to shoot
 			//either way, we need a target!!
 			double closestTargetDistance = Double.MAX_VALUE;
 			Position closestTarget = null;
 			Position myPos = unit.getPosition();
 			for(Unit toCheck : availableEnemies){
 				Position enemyPosition = toCheck.getPosition();
 				double targetDistance = enemyPosition.subtract(myPos).length();
 				if(targetDistance < closestTargetDistance){
 					closestTarget = enemyPosition;
 					closestTargetDistance = targetDistance;
 				}
 			}
 			
 			//if we aren't ready to fire yet, let's just move towards our target
 			if(!readyToFire || closestTargetDistance > getRange()){
 				//as long as it is farther than our minimum range!!
 				double minimumRange = getMinimumRange();
 				if(closestTargetDistance > minimumRange){
 					unit.getMovementStrategy().setTarget(new Transformation(closestTarget, unit.getRotation()));
 				}
 				return;
 			}
 			
 			//Now we know what to shoot; let's do so!
 			for(Turret t : unit.getTurrets()){				
 				MinimumRangeTurretStrategy strat = (MinimumRangeTurretStrategy) t.getTurretStrategy();
 				strat.setTarget(closestTarget);
 				strat.fight(availableEnemies, availableAllies);
 			}
 			
 			//We should also change how we are facing so that shit looks better
 			unit.getMovementStrategy().setTarget(new Transformation(myPos, closestTarget.subtract(myPos).getAngle()));
 			
 			lastShotStartedTime = currentTime;
 		}
 
 		private double getMinimumRange() {
 			double minimumRange = Double.MAX_VALUE;
 			for(Turret t : unit.getTurrets()){
 				MinimumRangeTurretStrategy strat = (MinimumRangeTurretStrategy) t.getTurretStrategy();
 				double currentRange = strat.getMinimumRange();
 				if(currentRange < minimumRange){
 					minimumRange = currentRange;
 				}
 			}
 
 			return minimumRange;
 		}
 		
 	}
 }

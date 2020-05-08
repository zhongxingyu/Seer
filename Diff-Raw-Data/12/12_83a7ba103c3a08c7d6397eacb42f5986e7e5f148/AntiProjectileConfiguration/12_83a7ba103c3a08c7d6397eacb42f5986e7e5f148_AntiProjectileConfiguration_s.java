 package linewars.gamestate.mapItems.strategies.targeting;
 
 import java.util.Random;
 
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.MapItem;
 import linewars.gamestate.mapItems.MapItemModifier.MapItemModifiers;
 import linewars.gamestate.mapItems.MapItemState;
 import linewars.gamestate.mapItems.Projectile;
 import linewars.gamestate.mapItems.strategies.StrategyConfiguration;
 import linewars.gamestate.mapItems.strategies.collision.CollisionStrategyConfiguration;
 import linewars.gamestate.shapes.Shape;
 import utility.AugmentedMath;
 import utility.Observable;
 import utility.Observer;
 import configuration.Usage;
 import editor.abilitiesstrategies.AbilityStrategyEditor;
 import editor.abilitiesstrategies.EditorProperty;
 import editor.abilitiesstrategies.EditorUsage;
 
 public strictfp class AntiProjectileConfiguration extends TargetingStrategyConfiguration implements Observer {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6174791286606222739L;
 	/**
 	 * A probability that influences the target selected by an anti-projectile missile
 	 */
 	public static final double SWAP_TO_BEST_CHANCE = 0.3;
 
 	static {
 		StrategyConfiguration.setStrategyConfigMapping("Anti Projectile",
 				AntiProjectileConfiguration.class, AbilityStrategyEditor.class);
 	}
 	
 	private double speed;
 	private double turningRadsPerSec;
 	
 	public strictfp class AntiProjectile implements TargetingStrategy
 	{
 
 		private Projectile projectile;
 		private Projectile target;
 		
 		private boolean clockwise;
 		
 		private AntiProjectile(Projectile p)
 		{
 			projectile = p;
			Random gen = new Random(p.getID());
 			clockwise = gen.nextBoolean();
 		}
 		
 		@Override
 		public String name() {
 			return "Targets the closest projectile and then tries to hit it";
 		}
 
 		@Override
 		public TargetingStrategyConfiguration getConfig() {
 			return AntiProjectileConfiguration.this;
 		}
 
 		@Override
 		public Transformation getTarget() {
 			if(target != null && target.getState().equals(MapItemState.Dead)){
 				target = null;
 			}
 			if(target == null)
 			{
 				pickNewTarget();
 			}
 			
 			double currentAngle = projectile.getRotation();
 			double maxTurn = turningRadsPerSec*projectile.getGameState().getLastLoopTime();
 			
 			//if no target exists, let's enter a holding pattern!
 			if(target == null){
 				return doHoldingPattern(currentAngle, maxTurn);
 			}
 			
 			double actualTurn = computeTurnAngle(currentAngle, maxTurn);
 			
 			double speedModifier = projectile.getModifier().getModifier(MapItemModifiers.moveSpeed);
 			Position actualChange = Position.getUnitVector(
 					actualTurn + currentAngle).scale(
 					speed * speedModifier * projectile.getGameState().getLastLoopTime());
 			
 			Transformation target = new Transformation(actualChange, actualTurn);
 			
 			target = target.add(projectile.getTransformation());
 			//need to handle projectile collisions here
 			checkForCollisionsWithProjectiles(target);
 			
 			return target;
 		}
 
 		private double computeTurnAngle(double currentAngle, double maxTurn) {
 			double desiredAngle = target.getPosition().subtract(projectile.getPosition()).getAngle();
 			double relativeDesiredAngle = desiredAngle - currentAngle;
 			double normalizedRelativeDesiredAngle = AugmentedMath.getAngleInPiToNegPi(relativeDesiredAngle);
 			double actualTurn;
 			if(Math.abs(normalizedRelativeDesiredAngle) > maxTurn)
 				actualTurn = normalizedRelativeDesiredAngle > 0 ? maxTurn : -1 * maxTurn;
 			else
 				actualTurn = normalizedRelativeDesiredAngle;
 			return actualTurn;
 		}
 
 		private void pickNewTarget() {
 			Random gen = new Random(projectile.getGameState().getTimerTick() + projectile.getID());
 			double minDistance = Double.POSITIVE_INFINITY;
 			for(Projectile p : projectile.getLane().getProjectiles())
 			{
 				if(p.getOwner() == projectile.getOwner()){
 					continue;
 				}
 				if(p.getState().equals(MapItemState.Dead)){
 					continue;
 				}
 				
 				double currentDistance = p.getPosition().distanceSquared(projectile.getPosition()); 
 				if(currentDistance < minDistance)
 				{
 					if(target == null || gen.nextDouble() <= SWAP_TO_BEST_CHANCE){
 						minDistance = currentDistance;
 						target = p;
 					}
 				}
 			}
 		}
 
 		private Transformation doHoldingPattern(double currentAngle,
 				double maxTurn) {
 			if(clockwise){
 				maxTurn *= -1;
 			}
 			double speedModifier = projectile.getModifier().getModifier(MapItemModifiers.moveSpeed);
 			Position actualChange = Position.getUnitVector(
 					maxTurn + currentAngle).scale(
 					speed * speedModifier * projectile.getGameState().getLastLoopTime());
 			Transformation target = new Transformation(actualChange, maxTurn);
 			
 			target = target.add(projectile.getTransformation());
 			return target;
 		}
 		
 		private void checkForCollisionsWithProjectiles(Transformation target)
 		{
 			Position change = target.getPosition().subtract(projectile.getPosition());
 			Shape body = projectile.getBody().stretch(new Transformation(change, target.getRotation() - projectile.getRotation()));
 			for(Projectile p : projectile.getLane().getProjectiles())
 			{
 				if(projectile.getState().equals(MapItemState.Dead))
 					break;
 				if(p.getState().equals(MapItemState.Dead)){
 					continue;
 				}
 				if(CollisionStrategyConfiguration.isAllowedToCollide(p, projectile) &&
 						body.isCollidingWith(p.getBody()))
 					projectile.getImpactStrategy().handleImpact(p);
 			}
 		}
 		
 	}
 	
 	public AntiProjectileConfiguration()
 	{
 		super.setPropertyForName("speed", new EditorProperty(
 				Usage.NUMERIC_FLOATING_POINT, 0, EditorUsage.PositiveReal,
 				"The speed of the projectile per second"));
 		super.setPropertyForName("turningRadsPerSec", new EditorProperty(
 				Usage.NUMERIC_FLOATING_POINT, 0, EditorUsage.PositiveReal,
 				"The number of radians this projectile can turn at per second"));
 		super.addObserver(this);
 	}
 
 	@Override
 	public TargetingStrategy createStrategy(MapItem m) {
 		if(m instanceof Projectile)
 			return new AntiProjectile((Projectile) m);
 		else
 			throw new IllegalArgumentException("Only projectiles may have targeting strategies");
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof AntiProjectileConfiguration)
 		{
 			AntiProjectileConfiguration apc = (AntiProjectileConfiguration) obj;
 			return apc.speed == speed &&
 					apc.turningRadsPerSec == turningRadsPerSec;
 		}
 		else
 			return false;
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if(o == this && arg.equals("speed"))
 			speed = (double)(Double)super.getPropertyForName("speed").getValue();
 		if(o == this && arg.equals("turningRadsPerSec"))
 			turningRadsPerSec = (double)(Double)super.getPropertyForName("turningRadsPerSec").getValue();
 	}
 
 }

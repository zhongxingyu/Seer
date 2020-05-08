 package linewars.gamestate.mapItems;
 
 
 import linewars.gamestate.GameState;
 import linewars.gamestate.Lane;
 import linewars.gamestate.Player;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.strategies.impact.ImpactStrategy;
 import linewars.gamestate.mapItems.strategies.targeting.TargetingStrategy;
 import linewars.gamestate.shapes.Shape;
 
 /**
  * 
  * @author , Connor Schenck
  *
  * This class represents a projectile. It knows how the projectile collides
  * with map items and what it should do upon impact. It is a map item.
  */
 public strictfp class Projectile extends MapItemAggregate {
 
 	private ProjectileDefinition definition;
 	private ImpactStrategy iStrat;
 	private TargetingStrategy tStrat;
 	private Lane lane;
 	private double durability;
 	
 	private Shape tempBody = null;
 	
 	/**
 	 * Creates a projectile at transformation t with definition def,
 	 * collision strategy cs, and impact strategy is. Sets its container
 	 * to l.
 	 * 
 	 * @param t		the transformation this projectile starts at
 	 * @param def	the definition that created this projectile
 	 * @param cs	the collision strategy for this projectile
 	 * @param is	the impact strategy for this projectile
 	 * @param l		the lane that this projectile is in
 	 */
 	public Projectile(Transformation t, ProjectileDefinition def, Player owner, GameState gameState) {
 		super(t, def, gameState, owner);
 		definition = def;
 		iStrat = def.getImpactStratConfig().createStrategy(this);
 		durability = def.getBaseDurability();
 		tStrat = def.getTargetingStratConfig().createStrategy(this);
 	}
 	
 	public void setLane(Lane l)
 	{
 		lane = l;
 	}
 	
 	public Lane getLane()
 	{
 		return lane;
 	}
 	
 	public double getDurability()
 	{
 		return durability;
 	}
 	
 	public void setDurability(double d)
 	{
 		durability = d;
 		if(durability <= 0)
 			this.setState(MapItemState.Dead);
 	}
 
 	/**
 	 * This method moves the projetile forward at a constant velocity, checks for collisions,
 	 * and calls its impact strategy on those collisions.
 	 */
 	public void move()
 	{
 		//make sure we're not already dead
 		if(this.getState().equals(MapItemState.Dead))
 			return;
 		
 		//first check to see if this projectile is outside the lane
 		double pointRatio = lane.getClosestPointRatio(this.getPosition());
 		Transformation t = lane.getPosition(pointRatio);
 		if(this.getPosition().distanceSquared(t.getPosition()) > Math.pow(lane.getWidth()/2, 2) ||
 				pointRatio >= 1.0 || pointRatio <= 0.0)
 		{
 			this.setState(MapItemState.Dead);
 			return;
 		}
 		
 		Transformation target = tStrat.getTarget();
 		Position change = target.getPosition().subtract(this.getPosition());
 		
 		tempBody = this.getBody().stretch(new Transformation(change, target.getRotation()));
 		
 		//this is the raw list of items colliding with this projetile's path
 		MapItem[] rawCollisions = lane.getCollisions(this);
		if(rawCollisions.length > 0)
			System.out.println(rawCollisions.length + " stuff");
 		tempBody = null;
 		//this list will be the list of how far along that path each map item is
 		double[] scores = new double[rawCollisions.length];
 		//the negative sine of the angle that the path was rotated by from 0 rads
 		double sine = -change.getY();
 		//the negative cosine of the angle that the path was rotated by from 0 rads
 		double cosine = -change.getX();
 		//calculate the x coordinate of each map item relative to this projectile and rotated
 		//"back" from how this projetile's path was rotated. This will define the order in which
 		//the projectile hit each map item
 		for(int i = 0; i < scores.length; i++)
 		{
 			Position p = rawCollisions[i].getPosition().subtract(this.getPosition());
 			scores[i] = cosine*p.getX() - sine*p.getY();
 		}
 			
 		MapItem[] collisions = new MapItem[scores.length];
 		
 		//since this list will never be that big, its ok to use selection sort
 		for(int i = 0; i < collisions.length; i++)
 		{
 			int smallest = 0;
 			for(int j = 1; j < collisions.length; j++)
 				if(scores[j] < scores[smallest])
 					smallest = j;
 			
 			collisions[i] = rawCollisions[smallest];
 			scores[smallest] = Double.MAX_VALUE;
 		}
 		
 		//move the projectile before calling the impact strategy so that the impact strategy may move the projectile
 		this.setTransformation(target);
 		//there's no need to call the collision strategy, it was taken into account when calculating collision
 		for(int i = 0; i < collisions.length && !this.getState().equals(MapItemState.Dead); i++)
 			iStrat.handleImpact(collisions[i]);
 	}
 
 	@Override
 	public MapItemDefinition<? extends MapItem> getDefinition() {
 		return definition;
 	}
 	
 	/**
 	 * 
 	 * @return	the impact strategy of this projetile
 	 */
 	public ImpactStrategy getImpactStrategy()
 	{
 		return iStrat;
 	}
 	
 	@Override
 	public boolean isCollidingWith(MapItem m)
 	{
 		if(tempBody == null)
 			return super.isCollidingWith(m);
 		else
 		{
 			if(!m.getCollisionStrategy().canCollideWith(this))
 				return false;
 			else
 				return tempBody.isCollidingWith(m.getBody());
 		}
 	}
 
 	@Override
 	public boolean equals(Object o){
 		if(o == null) return false;
 		if(!(o instanceof Projectile)) return false;
 		Projectile other = (Projectile) o;
 		if(!other.getBody().equals(getBody())) return false;
 		//TODO test other things in here
 		return true;
 	}
 
 	@Override
 	protected void setDefinition(MapItemDefinition<? extends MapItem> def) {
 		definition = (ProjectileDefinition) def;
 	}
 }

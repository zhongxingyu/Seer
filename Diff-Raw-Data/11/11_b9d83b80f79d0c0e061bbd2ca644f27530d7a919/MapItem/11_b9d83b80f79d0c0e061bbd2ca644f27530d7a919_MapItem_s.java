 package linewars.gamestate.mapItems;
 
 import java.util.ArrayList;
 
 import linewars.display.Animation;
 import linewars.display.DisplayConfiguration;
 import linewars.gamestate.GameState;
 import linewars.gamestate.Player;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.MapItemModifier.Constant;
 import linewars.gamestate.mapItems.MapItemModifier.MapItemModifiers;
 import linewars.gamestate.mapItems.abilities.Ability;
 import linewars.gamestate.mapItems.abilities.AbilityDefinition;
 import linewars.gamestate.mapItems.strategies.collision.CollisionStrategy;
 import linewars.gamestate.mapItems.strategies.collision.CollisionStrategyConfiguration;
 import linewars.gamestate.shapes.Shape;
 import utility.Observable;
 import utility.Observer;
 /**
  * 
  * @author , Connor Schenck
  *
  * This class represents the basic item on the map. Any agent
  * on the map is a mapItem. This mapItem knows where it is,
  * how it's oriented, what state it's in and how long its been
  * in that state, and it knows what abilities are currently active
  * on itself.
  */
 public strictfp abstract class MapItem implements Observer {
 	
 	//the position of this map item in map coordinates
 	//and the rotation of this map item where 0 radians is facing directly right
 	//THIS IS THE CENTER OF THE MAP ITEM
 	private Shape body;
 	
 	//the state of the map item
 	private MapItemState state;
 	//the time at which the map item entered its state
 	private long stateStart;
 	
 	private MapItemModifier modifier;
 	
 	private CollisionStrategy cStrat;
 	
 	//all the current abilities active on this map item
 	private ArrayList<Ability> activeAbilities;
 	
 	private Player owner;
 	private GameState gameState;
 	
 	private int ID;
 	
 	public MapItem(Transformation trans, MapItemDefinition<? extends MapItem> def, Player owner, GameState gameState)
 	{
 		setDefinition(def);
 		body = def.getBodyConfig().construct(trans);
 		state = MapItemState.Idle;
 		stateStart = (long) (gameState.getTime()*1000);
 		activeAbilities = new ArrayList<Ability>();
 		cStrat = def.getCollisionStrategyConfig().createStrategy(this);
 		this.owner = owner;
 		this.gameState = gameState;
 		
 		for(AbilityDefinition ad : def.getAbilityDefinitions())
 			if(ad.startsActive())
 				this.addActiveAbility(ad.createAbility(this));
 		
 		ID = gameState.getNextMapItemID();
 		
 		modifier = new MapItemModifier();
 		for(MapItemModifiers m : MapItemModifiers.values())
 			modifier.setMapping(m, new Constant(1.0));
 		
 		def.addObserver(this);
 	}
 	
 	protected abstract void setDefinition(MapItemDefinition<? extends MapItem> def);
 	
 	public int getID()
 	{
 		return ID;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		return ID;
 	}
 	
 	public void pushModifier(MapItemModifier mim)
 	{
 		mim.setWrapped(modifier);
 		modifier = mim;
 	}
 	
 	public void removeModifier(MapItemModifier mim)
 	{
 		modifier = modifier.removeModifierLayer(mim);
 	}
 	
 	public MapItemModifier getModifier()
 	{
 		return modifier;
 	}
 	
 	public abstract MapItemDefinition<? extends MapItem> getDefinition();
 	
 	public GameState getGameState()
 	{
 		return gameState;
 	}
 	
 	/**
 	 * This method updates all the map item's currently active abilities
 	 * and handles any other tasks that need to be accomplished by the map
 	 * item in one loop of the game thread and are not handles elsewhere.
 	 */
 	public void updateMapItem()
 	{
 		for(int i = 0; i < activeAbilities.size();)
 		{
 			//only update this ability if the mapItem isn't dead or if the
 			//ability isn't killable
 			if(!state.equals(MapItemState.Dead) || !activeAbilities.get(i).killable())
 				activeAbilities.get(i).update();
 			
 			//remove finished abilities
 			if(activeAbilities.get(i).finished())
 				activeAbilities.remove(i);
 			else
 				i++;
 					
 		}
 	}
 	
 	@Override
 	public void update(Observable o, Object obj)
 	{
 		String name = (String)obj;
 		if(name.equals("cStrat"))
 			cStrat = getDefinition().getCollisionStrategyConfig().createStrategy(this);
 		else if(name.equals("body"))
 			body = getDefinition().getBodyConfig().construct(body.position());
 	}
 	
 	/**
 	 * Adds the given ability to the list of active abilities for this map item
 	 * 
 	 * @param a	the ability to add
 	 */
 	public void addActiveAbility(Ability a)
 	{
 		activeAbilities.add(a);
 	}
 	
 	public void removeActiveAbility(Ability a)
 	{
 		activeAbilities.remove(a);
 	}
 	
 	/**
 	 * 
 	 * @return	the abilities currently active on this map item
 	 */
 	public Ability[] getActiveAbilities()
 	{
 		return activeAbilities.toArray(new Ability[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	the position of the map item
 	 */
 	public Position getPosition()
 	{
 		return getBody().position().getPosition();
 	}
 	
 	/**
 	 * 
 	 * @return	the rotation of the map item
 	 */
 	public double getRotation()
 	{
 		return getBody().position().getRotation();
 	}
 	
 	/**
 	 * 
 	 * @return	the transformation (position and rotation) of the map item
 	 */
 	public Transformation getTransformation()
 	{
 		return getBody().position();
 	}
 	
 	/**
 	 * 
 	 * @param p	the position to set the map item at
 	 */
 	public void setPosition(Position p)
 	{
 		body = getBody().transform(new Transformation(p.subtract(this.getPosition()), 0));
 	}
 	
 	/**
 	 * 
 	 * @param rot	the new rotation of the map item
 	 */
 	public void setRotation(double rot)
 	{
 		body = getBody().transform(new Transformation(new Position(0, 0), rot - getBody().position().getRotation()));
 	}
 	
 	/**
 	 * 
 	 * @param t the new transformation of the map item
 	 */
 	public void setTransformation(Transformation t)
 	{
 		Position current = getBody().position().getPosition();
 		Transformation change = new Transformation(t.getPosition().subtract(current), t.getRotation() - getBody().position().getRotation());
 		body = getBody().transform(change);
 	}
 	
 	/**
 	 * 
 	 * @return	the current state of the map item
 	 */
 	public MapItemState getState()
 	{
 		return state;
 	}
 	
 	/**
 	 * 
 	 * @param m	the state to set the map item to
 	 */
 	public void setState(MapItemState m)
 	{
 		if(this.getDefinition().isValidState(m))
 		{
 			state = m;
 			stateStart = (long) (this.getGameState().getTime()*1000);
 		}
 		else
 			throw new IllegalStateException(m.toString() + " is not a valid state for a " + this.getDefinition().getName());
 	}
 	
 	/**
 	 * 
 	 * @return	the time at which the map item entered its current state, in ms
 	 */
 	public double getStateStartTime()
 	{
 		return stateStart;
 	}
 	
 	/**
 	 * 
 	 * @return	the list of ability definitions available to this map item
 	 */
 	public AbilityDefinition[] getAvailableAbilities()
 	{
 		return this.getDefinition().getAbilityDefinitions().toArray(new AbilityDefinition[0]);
 	}
 	
 	/**
 	 * 
 	 * @return	the owner of this map item
 	 */
 	public Player getOwner()
 	{
 		return owner;
 	}
 	
 	/**
 	 * 
 	 * @return	the collision strategy associated with this map item
 	 */
 	public final CollisionStrategy getCollisionStrategy()
 	{
 		return cStrat;
 	}
 	
 	/**
 	 * 
 	 * @return	the name of this map item
 	 */
 	public String getName()
 	{
 		return this.getDefinition().getName();
 	}
 	
 	/**
 	 * This method takes in a map item and checks to see if the two are colliding.
 	 * It first checks their collision strategies to see if the can collide, and if
 	 * they can, checks to see if each of their bodies are colliding.
 	 * 
 	 * @param m		the map item to check collision with
 	 * @return		true if this and m can collide and are colliding, false otherwise
 	 */
 	public boolean isCollidingWith(MapItem m)
 	{
 		if(!CollisionStrategyConfiguration.isAllowedToCollide(m, this))
 			return false;
 		
 		return this.getBody().isCollidingWith(m.getBody());
 	}
 
 	/**
 	 * 
 	 * @return	the width of the bounding rectangle for this map item
 	 */
 	public double getWidth() {
 		return this.getBody().boundingRectangle().getWidth();
 	}
 
 	/**
 	 * 
 	 * @return	the height of the bounding rectangle for this map item
 	 */
 	public double getHeight() {
 		return this.getBody().boundingRectangle().getHeight();
 	}
 		
 	/**
 	 * 
 	 * @return	the radius of the bounding circle of this unit
 	 */
 	public double getRadius() {
 		return getBody().boundingCircle().getRadius();
 	}
 	
 	/**
 	 * 
 	 * @return	the shape that represents the body of this map item
 	 */
 	public Shape getBody()
 	{
 		return body;
 	}
 	
 	/**
 	 * Sets the shape of this map item. Maintains the transformation
 	 * that this map item was at before setting the shape (ie ignores
 	 * the transformation of s)
 	 * 
 	 * @param s
 	 */
 	public void setBody(Shape s)
 	{
 		Transformation t = getBody().position();
 		body = s;
 		setTransformation(t);
 	}
 	
 	/**
 	 * 
 	 * @return	whether or not this unit is finished and may be removed from the field
 	 */
 	public boolean finished()
 	{
 		Ability[] activeAbilities = this.getActiveAbilities();
 		for(Ability a : activeAbilities)
 			if(!a.killable())
 				return false;
 		
 		//TODO this is a hack
 		DisplayConfiguration dc = (DisplayConfiguration) this.getDefinition().getDisplayConfiguration();
 		Animation a = dc.getAnimation(MapItemState.Dead);
 		if(a != null)
 		{
 			double time = 0;
 			for(int i = 0; i < a.getNumImages(); i++)
 				time += a.getImageTime(i);
 			if(time > (this.getGameState().getTime() - this.getGameState().getLastLoopTime())*1000
 					- this.getStateStartTime())
 				return false;
 		}
 		
 		//if I'm at the point where I'm going to be removed, then I need to stop observing my definition
 		this.getDefinition().removeObserver(this);
 		return true;
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(obj instanceof MapItem)
 		{
 			MapItem m = (MapItem) obj;
 			return ((m.body == null && body == null) || m.body.equals(body)) &&
 					m.state == state &&
 					m.stateStart == stateStart &&
 					m.modifier.equals(modifier) &&
 					m.cStrat.equals(cStrat) &&
 					m.activeAbilities.equals(activeAbilities) &&
 					m.getID() == ID &&
 					m.getDefinition().equals(this.getDefinition());
 		}
 		else
 			return false;
 	}
 }

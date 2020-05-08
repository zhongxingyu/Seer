 /*
  Copyright (C) 2012 William James Dyce
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package wjd.teutoburg.regiment;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import wjd.amb.control.EUpdateResult;
 import wjd.amb.view.Colour;
 import wjd.amb.view.ICanvas;
 import wjd.math.Rect;
 import wjd.math.V2;
 import wjd.teutoburg.collision.Agent;
 import wjd.teutoburg.collision.Collider;
 import wjd.teutoburg.simulation.Tile;
 import wjd.util.BoundedValue;
 import wjd.util.Timer;
 
 /**
  *
  * @author wdyce
  * @since Dec 4, 2012
  */
 public abstract class RegimentAgent extends Agent
 {  
   /* NESTING */
   public enum State 
   {
       WAITING, CHARGING, FIGHTING, RALLYING, DEAD
   }
     
   /* CONSTANTS */
   private static final float ZOOM_IMPOSTER_THRESHOLD = 0.25f;
   private static final double ATTACK_FUMBLE_CHANCE = 0.0;
   private static final int MAX_NUMBER_OF_KILLS_PER_SOLDIER = 3;
   private static final int MAX_ATTACKS_PER_TURN = 1;
   protected static final int REACH = 1;
   protected static final float MAX_TIME_BETWEEN_ATTACKS = 1000.0f;
   
   /* ATTRIBUTES */
   // model
   private int strength;
   private Faction faction;
   protected State state;
   // combat
   protected Timer attackRecharge = new Timer(10);
   protected boolean attackReady;
   protected int hitsToTake;
   protected Set<RegimentAgent> combat = new HashSet<RegimentAgent>();
   // position
   private final V2 grid_pos = new V2();
   public Tile tile;
   private boolean sharing_tile = false;
   // organisation
   private Formation formation;
   // view
   private boolean nearby = true;
   private V2 left = new V2();
   // ai
   private final Rect perception_box = new Rect(Tile.SIZE.clone().scale(8));
   protected RegimentAgent nearestAlly, nearestEnemy;
   protected float nearestAllyDist2, nearestEnemyDist2;
   protected RegimentAgent nearestActivAlly;
   protected float nearestActivAllyDist2;
   protected boolean in_woods;
   // corpses
   private List<Cadaver> dead_pile;
 
 
   /* METHODS */
   // constructors
   public RegimentAgent(V2 start_position, int start_strength, Tile tile_, 
                                                               Faction faction)
   {
     // default
     super(start_position);
     left.reset(direction).left();
     
     // save parameters
     this.strength = start_strength;
     this.faction = faction;
     this.tile = tile_;
     tile.agent = this;
     grid_pos.reset(c.centre).scale(Tile.ISIZE).floor();
     
     // calculate unit positions based on the strength of the unit
     formation = faction.createFormation(this);
     setRadius(formation.reform());
     hitsToTake = 0;
     
     // cadavers stored until the scene collects them
     dead_pile = new LinkedList<Cadaver>();
     
     // combat
     attackReady = false;
     attackRecharge.setMax((int)(MAX_TIME_BETWEEN_ATTACKS/strength));
 
     // initialise status
     state = State.WAITING;
   }
 
   // accessors -- package
   
   int getStrength()
   {
     return strength;
   }
 
   V2 getDirection()
   {
     return direction;
   }
 
   V2 getLeft()
   {
     return left;
   }
   
   Faction getFaction()
   {
     return faction;
   }
   
   float getPerceptionRadius()
   {
     return (perception_box.w * 0.5f);
   }
   
   
   // accessors -- protected
   
   protected boolean canSee(RegimentAgent a)
   {
     // override if needed!
 	  return true;
   }
   
 
   // accessors -- public 
   
   public boolean isFormedUp()
   {
     return (formation instanceof Formation.Turtle);
   }
   
   public void setFormedUp(boolean form_up)
   {
     // skip if this is already the case
     if (form_up == isFormedUp())
       return;
       
     // otherwise change formation...
     if (form_up)
       formation = new Formation.Turtle(this);
     else
       formation = new Formation.Rabble(this);
     
     // ... and reform!
     setRadius(formation.reform());
   }
   
   @Override
   public String toString()
   {
 	  StringBuilder print_state = new StringBuilder();
 	  print_state.append("State : "+state+"\n");
 	  print_state.append("Strength : "+strength+"\n");
 	  print_state.append("Armed attacks : "+attackReady+"\n");
 	  print_state.append("Hits to take : "+hitsToTake+"\n");
 	  
 	  return print_state.toString();
   }
   
 
   // mutators
   public EUpdateResult killSoldiers(int n_killed)
   {
     if(n_killed > strength)
       n_killed = strength;
     
     // transform dying men into corpses
     for(int i = 0; i < n_killed; i++)
       dead_pile.add(new Cadaver(formation.getSoldierPosition(strength-i-1), faction));
     
     // remove the dead
     strength -= n_killed;
     attackRecharge.setMax((int)(MAX_TIME_BETWEEN_ATTACKS/strength));
     
     // destroy the regiment if too many are dead
     if (strength == 0)
     {
       state = State.DEAD;
       return EUpdateResult.DELETE_ME;
     }
     
     // reform the regiment if it is still alive
     else
     {
       setRadius(formation.reform());
       return EUpdateResult.CONTINUE;
     }
   }
   
   public void bringOutYourDead(Collection<Cadaver> cemetary)
   {
     cemetary.addAll(dead_pile);
     dead_pile.clear();
   }
 
   /* INTERFACE */
   
   protected abstract EUpdateResult ai(int t_delta, Iterable<Tile> percepts);
   
   protected abstract double chanceToHit(RegimentAgent defender);
   
   protected abstract double chanceToBlock(RegimentAgent defender);
   
   protected abstract boolean isEnemy(RegimentAgent other);
   
   protected abstract boolean isAlly(RegimentAgent other);
   
   /* OVERRIDES -- AGENT */
   
   @Override
   protected void directionChange()
   {
     // default
     super.directionChange();
     left.reset(direction).left();
     
     // we need to recache the soldiers' positions if in view close to us
     if (visible)
       formation.reposition();
   }
   
   @Override
   protected void positionChange()
   {
     // default
     super.positionChange();
     
     // change tile
     tryClaimTile();
     
     // we need to recache the soldiers' positions if in view close to us
     if (visible)
       formation.reposition();
   }
   
   @Override
   public EUpdateResult update(int t_delta)
   {
 	  // hitsToTake
 	  if(hitsToTake > 0)
 	  {
 		  //System.out.println(hitsToTake+" de mes soldats ont été tués");
 		  if(killSoldiers(hitsToTake) == EUpdateResult.DELETE_ME)
 			  state = State.DEAD;
 		  hitsToTake = 0;
 	  }
 
 	  // dead
 	  if(state == State.DEAD)
 	  {
 		  return EUpdateResult.DELETE_ME;
 	  }
 
 	  // default
 	  EUpdateResult result = super.update(t_delta);
 	  if (result != EUpdateResult.CONTINUE)
 		  return result;
 
 	  // timers
 	  if(state == State.FIGHTING 
 			  && !attackReady
 			  && attackRecharge.update(t_delta) == EUpdateResult.FINISHED)
 	  {
 		  attackReady = true;
 	  }
 
 	  // choose action
 	  perception_box.centrePos(c.centre);
 	  Iterable<Tile> percepts = tile.grid.createSubGrid(perception_box);
 	  cachePercepts(percepts);
 	  setHitableEnemies();
 	  if(ai(t_delta, percepts) == EUpdateResult.DELETE_ME)
 		  return EUpdateResult.DELETE_ME;
 
 	  // snap out of collisions
 	  if(sharing_tile)
 	  {      
 		  Tile t = tile.grid.gridToTile(grid_pos);
		  if(t!= null && t.agent != null)
 		  {
 			  V2 push = c.centre.clone().sub(t.agent.c.centre).scale(0.001f);
 			  speed.add(push);
 		  }
 	  }
 
 	  // set level of detail
 	  formation.setDetail(visible && nearby);
 
 	  // all clear!
 	  return EUpdateResult.CONTINUE;
   }
 
   @Override
   public void render(ICanvas canvas)
   {
     // skip if not in the camera's view
     if (visible = (canvas.getCamera().canSee(visibility_box)))
     {
       // we'll turn off the details if we're too far away
       nearby = (canvas.getCamera().getZoom() >= ZOOM_IMPOSTER_THRESHOLD);
      
       // render the formation depending on the level of detail
       formation.render(canvas);
       
       canvas.text(""+state, c.centre);
       //canvas.box(perception_box, false);
     }
     else
       nearby = false;
     
     canvas.setColour(Colour.WHITE);
     if(!attackReady)
     {
       canvas.setLineWidth(3.0f);
       canvas.circle(c.centre, c.radius, false);
     }
     //canvas.text(state+" ; "+attackReady+" ; "+attackRecharge.balance() , c.centre);
   }
   
   @Override
   public void collisionEvent(Collider other, float overlap)
   {
     // fight enemies
     RegimentAgent r = (RegimentAgent)other;
     if(isEnemy(r) && r.state != State.DEAD)
       combat.add(r);
       
     
     // snap out of collision
     super.collisionEvent(other, overlap);
   }
   
   /* SUBROUTINES */
   
   private void setHitableEnemies()
   {
     // add new enemies to combat
     Iterable<Tile> neighbours = tile.grid.getNeighbours(tile, true);
     for(Tile t : neighbours)
     {
       if(t.agent != null && isEnemy(t.agent) && t.agent.state != State.DEAD && t.agent.getCircle().collides(c))
         combat.add(t.agent);
     }
     
     RegimentAgent r;
     // remove old enemies from combat if they're no longer in contact
     Iterator<RegimentAgent> it = combat.iterator();
     while(it.hasNext())
     {
     	r = it.next();
 			if(!r.getCircle().collides(c) || r.state == State.DEAD)
         it.remove();
     }
     
   }
   
   private void tryClaimTile()
   {
     // have we moved into a new tile?
     grid_pos.reset(c.centre).scale(Tile.ISIZE).floor();
     if (grid_pos.x != tile.grid_position.x || grid_pos.y != tile.grid_position.y)
     {
       // tile outside of grid!
       if(!tile.grid.validGridPos(grid_pos))
         return;
       
       Tile new_tile = tile.grid.tiles[(int) grid_pos.y][(int) grid_pos.x];
 
       // try to claim new tile
       if (new_tile.setRegiment(this))
       {
         // success :)
         sharing_tile = false;
         tile.setRegiment(null);
         tile = new_tile;
       }
 
       // failure :(
       else
       {
         // try to claim neighbouring tile instead
         sharing_tile = true;
         for(Tile t : new_tile.grid.getNeighbours(new_tile, true))
           if(t.setRegiment(this))
           {
             tile.setRegiment(null);
             tile = t;
             break;
           }
       }
     }
   }
   
   protected void cachePercepts(Iterable<Tile> percepts)
   {
     // reset
     nearestAlly = nearestEnemy = null;
 	  nearestAllyDist2 = nearestEnemyDist2 = Float.MAX_VALUE;
 	  nearestActivAlly = null;
 	  nearestActivAllyDist2 = Float.MAX_VALUE;
 	  
     
     // check if we're in the woods
     in_woods = !(tile.forest_amount.isEmpty());
     
     // check all tiles in view 
     for(Tile t : percepts)
 	{
       // skip if dead or non visible
       if(t.agent == null || !canSee(t.agent) || t.agent.state == State.DEAD)
         continue;
       
       RegimentAgent r = t.agent;
       
       // cache nearest enemy
 		  if(isEnemy(r))
 		  {
 			  float dist2 = r.getCircle().centre.distance2(c.centre);
 			  if(dist2 < nearestEnemyDist2)
 			  {
 				  nearestEnemy = r;
 				  nearestEnemyDist2 = dist2;
 			  }
 		  }
       
       // cache nearest ally
       else if(isAlly(r))
 		  {
 			  float dist2 = r.getCircle().centre.distance2(c.centre);
 			  if(dist2 < nearestAllyDist2)
 			  {
 				  nearestAlly = r;
 				  nearestAllyDist2 = dist2;
 			  }
 			  if(r.state != State.WAITING && dist2 < nearestActivAllyDist2)
 			  {
 				  nearestActivAlly = r;
 				  nearestActivAllyDist2 = dist2;
 			  }
 		  }
 	  }
   }
   
   /* COMBAT */
   
   protected EUpdateResult melee(RegimentAgent enemy)
   { 
     // determine the number of kills
     int aKills = rollKillsAgainst(enemy),
         bKills = enemy.rollKillsAgainst(this);
     
     // apply this number of kills AFTER determining each side's result
     enemy.hitsToTake += aKills;
     //System.out.println(bKills+" de mes soldats sont morts dans mon attaque");
     return killSoldiers(bKills);
   }
   
   protected int rollKillsAgainst(RegimentAgent other)
   {
     // pause between attacks
     if(!attackReady)
       return 0;
 
     // compute attack value
     double total_attack = 0.0;
     total_attack += Math.random()
     				* MAX_NUMBER_OF_KILLS_PER_SOLDIER
                     * this.chanceToHit(other) 
                     * (1 - other.chanceToBlock(this))
                     * (1 - ATTACK_FUMBLE_CHANCE);
     
     attackReady = false;
     // return number of kills
     return (int)Math.round(total_attack);
   }
   
   protected EUpdateResult randomAttack()
   {
 	  if(attackReady)
 	  {
 		  // pick a random enemy to attack
 		  int attack_i = (int)(Math.random() * combat.size()), i = 0;
 		  for(RegimentAgent r : combat)
 		  {
 			  if(i == attack_i)
 				  if(melee(r) == EUpdateResult.DELETE_ME)
 					  return EUpdateResult.DELETE_ME;
 			  i++;
 		  }
 	  }
 	  return EUpdateResult.CONTINUE;
   }
 }

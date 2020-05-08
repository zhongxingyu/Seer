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
 
 import wjd.amb.control.EUpdateResult;
 import wjd.math.M;
 import wjd.math.V2;
 import wjd.teutoburg.regiment.RegimentAgent.State;
 import wjd.teutoburg.simulation.Tile;
 import wjd.util.Timer;
 
 /**
  *
  * @author wdyce
  * @since Dec 15, 2012
  */
 public class RomanRegiment extends RegimentAgent
 {
 	public static class State extends RegimentAgent.State
 	{
 		public static final State RALLYING = new State(5,"rallying");
 		public static final State ESCAPING = new State(6,"escaping");
 		public static final State DEFENDING = new State(7,"defending");
 		
 		protected State(int v, String k) {super(v, k);}
 	}
 		
   /* CONSTANTS */
   private static final int REGIMENT_SIZE = 6*6;
   
   // combat
   private static final double BLOCK_CHANCE_TURTLE = 0.7;
   private static final double BLOCK_CHANCE_RABBLE = 0.3;
   private static final double ATTACK_CHANCE = 0.5;
   private static final int FLANK_MIN_ANGLE = 135;
   
   // movement
   private static final float SPEED_FACTOR = 0.3f;
   private static final float MAX_TURN_TURTLE 
                         = 10.0f * (float)Math.PI / 180.0f, 
                           // 20 degrees per millisecond
                             MAX_TURN_RABBLE
                         = 50.0f * (float)Math.PI / 180.0f; 
                           // 90 degrees per millisecond
   
   /* ATTRIBUTES */
   protected Timer defendingAgainstNobody = new Timer(5000);
  protected Timer rallyingWithNobody = new Timer(5000);
   
   /* METHODS */
 
   // constructors
   
   public RomanRegiment(V2 position, Tile t, Faction faction)
   {
     super(position, REGIMENT_SIZE, t, faction);
  // initialise status
     state = State.ESCAPING;
   }
 
   // accessors
 
   // mutators
   
   /* IMPLEMENTS -- REGIMENTAGENT */
   
   @Override
   protected boolean canSee(RegimentAgent a)
   {
 	  if(!hornHeard && !a.tile.forest_amount.isEmpty())
 		  return false;
 	  else
 		  return true;
   }
 
   
   
   // artificial intelligence
   protected EUpdateResult escaping(int t_delta, Iterable<Tile> percepts)
   {
 	  V2 escape_direction = getCircle().centre.clone().add(0, -10);
 	  
 	  if(nearestEnemy != null && !hornHeard)
 	  {
 		  soundTheHorn();
 	  }
 	  else if(hornHeard)
 	  {
 		  state = State.RALLYING;
 	  }
 	  else
 	  {
 		  V2 new_direction = escape_direction.clone(), tmp;
 		  int nbPossibleNeigh = 1;
 		  for(Tile t : percepts)
 		  {
 			  if(	t != tile 
 					&& t.pixel_position.y >= tile.pixel_position.y
 					&& t.pixel_position.x != tile.pixel_position.x)
 			  {
 				  nbPossibleNeigh++;
 				  if(!(t.forest_amount.isEmpty()))
 				  {
 					  tmp = new V2(t.pixel_position, c.centre);
 					  tmp.normalise();
 					  tmp.scale(t.forest_amount.balance());
 					  new_direction.add(tmp);
 					  nbPossibleNeigh--;
 				  }
 			  }
 		  }
 		  if(nbPossibleNeigh == 1)
 			  turnTowardsGradually(escape_direction, getMaxTurn());
 		  else
 			  turnTowardsGradually(new_direction, getMaxTurn());
 		  advance(SPEED_FACTOR*t_delta);
 	  }
 	  return EUpdateResult.CONTINUE;
   }
   
   protected EUpdateResult rallying(int t_delta, Iterable<Tile> percepts)
   {
 	  if(nearestAlly != null) // I see an ally
 	  {
		  rallyingWithNobody.fill();
 		  if(isProtected())
 		  {
 			  state = State.DEFENDING;
 		  }
 		  else // I'm not protected : flanckable
 		  {
			  formGiganticTurtle(t_delta, percepts);
 		  }
 	  }
 	  else // I can't see any ally
 	  {
 		  if(rallyingWithNobody.update(t_delta) == EUpdateResult.FINISHED)
 		  {
 			  state = State.ESCAPING;
 		  }
 		  else
 		  {
 			  if(hornFaction == getFaction())
 			  {
 				  // I'm going to rally the horn-bearer
 				  V2 prev_centre = c.centre.clone();
 				  faceTowards(hornDirection);
 				  advance(SPEED_FACTOR*t_delta);
 				  V2 way = c.centre.clone();
 				  way.sub(prev_centre);
 				  hornDirection.add(way);  
 			  }
 			  else // the horn was sounded by an ennemi
 			  {
 				  // I'm alone and ennemies are attacking
 				  state = State.ESCAPING;
 			  }
 		  }
 	  }
 
 	  return EUpdateResult.CONTINUE;
   }
   
   protected EUpdateResult defending(int t_delta)
   {
 	  if(!isProtected()) // I'm not protected
 	  {
 		  state = State.RALLYING;
 	  }
 	  else // I'm protected
 	  {
 		  if(nearestEnemy != null) // I see an enemy
 		  {
 			  defendingAgainstNobody.fill();
 			  faceTowards(nearestEnemy.getCircle().centre);
 		  }
 		  else // I can't see an enemy
 		  {
 			  if(defendingAgainstNobody.update(t_delta) == EUpdateResult.FINISHED)
 			  {
 				  state = State.ESCAPING;
 				  hornHeard = false;
 			  }
 		  }
 	  }
 	  return EUpdateResult.CONTINUE;
   }
 
   @Override
   protected EUpdateResult ai(int t_delta, Iterable<Tile> percepts)
   {
 	  if(super.ai(t_delta, percepts) == EUpdateResult.DELETE_ME)
 		  return EUpdateResult.DELETE_ME;
 	  
 	  if(state == State.ESCAPING)
 	  {
 		  if(escaping(t_delta, percepts) == EUpdateResult.DELETE_ME)
 			  return EUpdateResult.DELETE_ME;
 	  }
 	  if(state == State.RALLYING)
 	  {
 		  if(rallying(t_delta, percepts) == EUpdateResult.DELETE_ME)
 			  return EUpdateResult.DELETE_ME;
 	  }
 	  if(state == State.DEFENDING)
 	  {
 		  if(defending(t_delta) == EUpdateResult.DELETE_ME)
 			  return EUpdateResult.DELETE_ME;
 	  }
 	  return EUpdateResult.CONTINUE;
   }
   
   
   // parameters
   @Override
   protected double chanceToBlock(RegimentAgent attacker)
   {
     if(isFormedUp())
     {
       // deform if flank-attack
       if((V2.angleBetween(getDirection(), attacker.getDirection())*180.0/2.0) 
             < FLANK_MIN_ANGLE)
       {
         setFormedUp(false);
       }
       else
         return BLOCK_CHANCE_TURTLE;
     }
     return BLOCK_CHANCE_RABBLE;
   }
   
   @Override
   protected double chanceToHit(RegimentAgent defender)
   {
     return ATTACK_CHANCE;
   }
   
   @Override
   protected boolean isEnemy(RegimentAgent other)
   {
     return (other.state == State.DEAD) 
           ? false
           : (other instanceof BarbarianRegiment);
   }
   
   @Override
   protected boolean isAlly(RegimentAgent other)
   {
     return (other.state == State.DEAD) 
           ? false
           : (other instanceof RomanRegiment);
   }
   
   protected boolean isProtected()
   {
 	  if(alliesFormedAround.size() >= 3)
 	  {
 		  return true;
 	  }
 	  return false;
   }
   
   protected void formGiganticTurtle(int t_delta, Iterable<Tile> percepts)
   {
 	  // TODO : setFormedUp(false) when relaying ?
	  V2 new_direction = c.centre.clone(), tmp;
 	  for(Tile t : percepts)
 	  {
 		  if(t != tile)
 		  {
 			  if(t.agent != null && this.isAlly(t.agent))
 			  {
				  tmp = new V2(c.centre, t.agent.getCircle().centre);
 				  tmp.norm(tmp.norm()/Tile.SIZE.norm());
 				  new_direction.add(tmp);
 			  }
 			  if(!(t.forest_amount.isEmpty()))
 			  {
				  tmp = new V2(t.grid_position, c.centre);
 				  tmp.normalise();
 				  tmp.scale(t.forest_amount.balance());
 				  new_direction.add(tmp);
 			  }
 		  }
 		  
 	  }
 	  faceTowards(new_direction);
 	  advance(SPEED_FACTOR*t_delta);
   }
   
   @Override
   protected EUpdateResult fighting()
   {
 	  if(!combat.isEmpty())
 	  {
 		 if(randomAttack() == EUpdateResult.DELETE_ME)
 			return EUpdateResult.DELETE_ME;
 	  }
 	  else
 	  {
 		  state = State.DEFENDING;
 	  }
 	  return EUpdateResult.CONTINUE;
   }
   
   
   /* SUBROUTINES */
 
   private float getMaxTurn()
   {
     return ((isFormedUp()) ? MAX_TURN_TURTLE : MAX_TURN_RABBLE); 
   }
   
   @Override
   public boolean faceTowards(V2 target)
   {
 	  return turnTowardsGradually(target, getMaxTurn());
   }
 }

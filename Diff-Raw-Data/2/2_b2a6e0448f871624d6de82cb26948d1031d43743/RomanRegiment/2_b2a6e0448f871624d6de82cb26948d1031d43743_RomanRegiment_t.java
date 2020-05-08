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
 import wjd.math.V2;
 import wjd.teutoburg.simulation.Tile;
 import wjd.util.Timer;
 
 /**
  *
  * @author wdyce
  * @since Dec 15, 2012
  */
 public class RomanRegiment extends RegimentAgent
 {
   
   /* NESTING */
 	public static class RomanState extends State
 	{
 		public static final RomanState RALLYING = new RomanState(5, "rallying");
 		public static final RomanState MARCHING = new RomanState(6, "marching");
 		public static final RomanState DEFENDING = new RomanState(7, "defending");
 		
 		protected RomanState(int v, String k) 
     {
       super(v, k);
     }
 	}
   
   /* CLASS VARIABLES */
   private static final V2 temp1 = new V2(), temp2 = new V2();
 		
   /* CONSTANTS */
   private static final int REGIMENT_SIZE = 6*6;
   
   // combat
   private static final double BLOCK_CHANCE_TURTLE = 0.7;
   private static final double BLOCK_CHANCE_RABBLE = 0.3;
   private static final double ATTACK_CHANCE = 0.6;
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
   protected Timer defendingAgainstNobody = new Timer(10000);
   protected Timer rallyingWithNobody = new Timer(2000);
   
   /* METHODS */
 
   // constructors
   
   public RomanRegiment(V2 position, Tile t, Faction faction)
   {
     super(position, REGIMENT_SIZE, t, faction);
     
     // initialise status
     state = RomanState.MARCHING;
   }
   
   /* IMPLEMENTS -- REGIMENTAGENT */
   
   @Override
   protected boolean canSee(RegimentAgent a)
   {
     // FIXME - not sure this is exactly what we want: rather it is a state
     // we enter after we have first sighted an enemy
 	  return (heardHorn != null || a.tile.forest_amount.isEmpty());
   }
 
   
   
   // artificial intelligence
 
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
 		  defendingAgainstNobody.empty();
 		  rallyingWithNobody.empty();
 		  state = RomanState.DEFENDING;
 	  }
 	  return EUpdateResult.CONTINUE;
   }
   
   protected EUpdateResult marching(int t_delta, Iterable<Tile> percepts)
   {
 	  V2 escape_direction = getCircle().centre.clone().add(0, -10);
 	  
 	  if(nearestEnemy != null && heardHorn == null && soundedHorn == null)
 	  {
		  soundTheHorn();
 	  }
 	  else if(heardHorn != null)
 	  {
 		  state = RomanState.RALLYING;
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
 			  faceTowards(escape_direction);
 		  else
 			  faceTowards(new_direction);
 		  advance(SPEED_FACTOR * t_delta);
 	  }
 	  return EUpdateResult.CONTINUE;
   }
   
   protected EUpdateResult rallying(int t_delta, Iterable<Tile> percepts)
   {
 	  if(nearestAlly != null) // I see an ally
 	  {
 		  if(isProtected())
 		  {
 			  state = RomanState.DEFENDING;
 		  }
 		  else // I'm not protected : flanckable
 		  {
 			  if(rallyingWithNobody.update(t_delta) == EUpdateResult.FINISHED)
 			  {
 				  state = RomanState.MARCHING;
 			  }
 			  else
 			  {
 				  formMetaTurtle(t_delta, percepts);
 			  }
 		  }
 	  }
 	  else // I can't see any ally
 	  {
 		  if(rallyingWithNobody.update(t_delta) == EUpdateResult.FINISHED)
 		  {
 			  state = RomanState.MARCHING;
 		  }
 		  else
 		  {
 			  if(heardHorn != null && isAlly(heardHorn.source))
 			  {
 				  // I'm going to rally the horn-bearer
 				  faceTowards(temp1.reset(heardHorn.position).sub(c.centre));
 				  advance(SPEED_FACTOR * t_delta); 
 			  }
 			  else // the horn was sounded by an enemy
 			  {
 				  // I'm alone and enemies are attacking
 				  state = RomanState.MARCHING;
 			  }
 		  }
 	  }
 
 	  return EUpdateResult.CONTINUE;
   }
   
   protected EUpdateResult defending(int t_delta)
   {  
 	  if(!isProtected()) // I'm not protected
 	  {
 		  state = RomanState.RALLYING;
 	  }
 	  else // I'm protected
 	  {
 		  if(tile.forest_amount.balance() < 0.2 && !isFormedUp())
 			  setFormedUp(true);
 		  else
 		  {
 			  Tile tileToFace = null;
 			  if(alliesFormedAround.size() == 3)
 				  for(Tile t : tile.grid.getNeighbours(tile, false))
 					  if(t.agent == null)
 						  tileToFace = t;
 
 			  if(tileToFace != null)
 			  {
 				  tileToFace.getCentrePosition(temp1);
 				  faceTowards(temp1);
 			  }
 			  else if(nearestEnemy != null) // I see an enemy
 			  {
 				  defendingAgainstNobody.empty();
 				  faceTowards(nearestEnemy.getCircle().centre);
 			  }
 			  else // I can't see an enemy
 			  {
 				  if(defendingAgainstNobody.update(t_delta) == EUpdateResult.FINISHED)
 				  {
 					  state = RomanState.MARCHING;
 					  rallyingWithNobody.empty();
 				  }
 			  }
 		  }
 	  }
 	  return EUpdateResult.CONTINUE;
   }
 
   @Override
   protected EUpdateResult ai(int t_delta, Iterable<Tile> percepts)
   {
 	  if(in_woods && isFormedUp() && tile.forest_amount.balance() > 0.4)
 	  {
 		  setFormedUp(false);
 	  }
 	  
 	  if(super.ai(t_delta, percepts) == EUpdateResult.DELETE_ME)
 		  return EUpdateResult.DELETE_ME;
 	  
 	  if(state == RomanState.MARCHING)
 	  {
 		  if(marching(t_delta, percepts) == EUpdateResult.DELETE_ME)
 			  return EUpdateResult.DELETE_ME;
 	  }
 	  if(state == RomanState.RALLYING)
 	  {
 		  if(rallying(t_delta, percepts) == EUpdateResult.DELETE_ME)
 			  return EUpdateResult.DELETE_ME;
 	  }
 	  if(state == RomanState.DEFENDING)
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
     return (other.state == RomanState.DEAD) 
           ? false
           : (other instanceof BarbarianRegiment);
   }
   
   @Override
   protected boolean isAlly(RegimentAgent other)
   {
     return (other.state == RomanState.DEAD) 
           ? false
           : (other instanceof RomanRegiment);
   }
   
   @Override
   protected float getSpeedFactor()
   {
     return SPEED_FACTOR;
   }
     
   /* OVERRIDES */
       
   @Override
   public boolean faceTowards(V2 target)
   {
 	  return turnTowardsGradually(target, getMaxTurn());
   }
   
   
   
   
   /* SUBROUTINES */
 
   private float getMaxTurn()
   {
     return ((isFormedUp()) ? MAX_TURN_TURTLE : MAX_TURN_RABBLE); 
   }
   
   private boolean isProtected()
   {
 	  if(alliesFormedAround.size() >= 3 
     || (nearestAlly != null && alliesFormedAround.size() > 0
         && nearestAlly.state == RomanState.DEFENDING))
 	  {		  
 		  return true;
 	  }
 	  return false;
   }
   
   private void formMetaTurtle(int t_delta, Iterable<Tile> percepts)
   {
 	  // TODO : setFormedUp(false) when relaying ?
 	  V2 new_direction = c.centre.clone();
 	  for(Tile t : percepts)
 	  {
 		  if(t != tile)
 		  {
 			  if(t.agent != null && this.isAlly(t.agent))
 			  {
 				  temp1.reset(t.agent.getCircle().centre).sub(c.centre);
 				  new_direction.add(temp1.norm(temp1.norm() / Tile.DIAGONAL));
 			  }
 			  if(!(t.forest_amount.isEmpty()))
 			  {
           t.getCentrePosition(temp1);
           temp2.reset(c.centre);
           new_direction.add(temp2.sub(temp1).normalise().scale(t.forest_amount.balance()));
 			  }
 		  }
 		  
 	  }
 	  faceTowards(new_direction);
 	  advance(SPEED_FACTOR * t_delta);
   }
 }

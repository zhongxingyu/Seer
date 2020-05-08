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
 
 import wjd.math.V2;
 import wjd.teutoburg.regiment.RegimentAgent.State;
 import wjd.teutoburg.simulation.Tile;
 
 /**
  *
  * @author wdyce
  * @since Dec 15, 2012
  */
 public class RomanRegiment extends RegimentAgent
 {
   /* CONSTANTS */
   private static final int REGIMENT_SIZE = 6*6;
   private static final float SPEED_FACTOR = 0.6f;//0.06f;
   private static final double BLOCK_CHANCE_TURTLE = 0.6;
   private static final double BLOCK_CHANCE_RABBLE = 0.2;
   private static final double ATTACK_CHANCE = 0.5;
   private static final int FLANK_MIN_ANGLE = 55;
   
   /* ATTRIBUTES */
   
   /* METHODS */
 
   // constructors
   
   public RomanRegiment(V2 position, Tile t, Faction faction)
   {
     super(position, REGIMENT_SIZE, t, faction);
     defense_potential = 10;
     attack_potential = 10;
   }
 
   // accessors
 
   // mutators
   
   /* IMPLEMENTS -- REGIMENTAGENT */
 
   @Override
   protected void ai(int t_delta, Iterable<Tile> percepts)
   {
 	  V2 escape_point = getCircle().centre.clone();
 	  escape_point.add(0, -10);
 	  RomanRegiment nearestRoman = null;
 	  float distanceFromRoman = Float.MAX_VALUE, tmp;
 	  BarbarianRegiment nearestBarbarian = null;
 	  float distanceFromBarbarian = Float.MAX_VALUE;
 	  
 	  for(Tile t : percepts)
 	  {
 		  if(t.agent instanceof RomanRegiment)
 		  {
 			  tmp = t.agent.getCircle().centre.distance2(c.centre);
 			  if(t.agent.state != State.DEAD && tmp < distanceFromRoman)
 			  {
 				  nearestRoman = (RomanRegiment)t.agent;
 				  distanceFromRoman = tmp;
 			  }
 		  }
 		  else if(t.agent instanceof BarbarianRegiment)
 		  {
 			  tmp = t.agent.getCircle().centre.distance2(c.centre);
 			  if(t.agent.state != State.DEAD && tmp < distanceFromBarbarian)
 			  {
 				  nearestBarbarian = (BarbarianRegiment)t.agent;
 				  distanceFromBarbarian = tmp;
 			  }
 		  }
 	  }
 
 	  if(nearestBarbarian != null && state != State.FIGHTING && c.collides(nearestBarbarian.getCircle()))
 	  {
 		  state = State.FIGHTING;
 	  }
 	  if(state == State.FIGHTING)
 	  {
       if(nearestBarbarian != null)
         melee(this, nearestBarbarian);
       else
         state = State.WAITING;
 	  }
 	  else if(state == State.WAITING)
 	  {
 		  if(nearestBarbarian != null)
 		  {
 			  state = State.CHARGING;
 		  }
 		  else
 		  {
 			  faceTowards(escape_point);
 			  advance(SPEED_FACTOR*t_delta);
 		  }
 	  }
 	  else if(state == State.CHARGING)
 	  {
 		  if(nearestBarbarian != null)
 		  {
 			  faceTowards(nearestBarbarian.getCircle().centre);
 			  float min = Math.min(SPEED_FACTOR*t_delta, (float)Math.sqrt(distanceFromBarbarian));
 			  advance(min);
 			  if(min == distanceFromBarbarian)
 				  state = State.FIGHTING;
 		  }
 		  else
 		  {
 			  state = State.WAITING;
 		  }
 	  }
   }
   
   @Override
   protected double chanceToBlock(RegimentAgent attacker)
   {
     if(isFormedUp())
     {
       // deform if flank-attack
       if(V2.angleBetween(getDirection(), attacker.getDirection()) > FLANK_MIN_ANGLE)
         setFormedUp(false);
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
 }

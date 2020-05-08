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
 
 import wjd.math.Rect;
 import wjd.math.V2;
 import wjd.teutoburg.collision.Collider;
 import wjd.teutoburg.simulation.Tile;
 
 /**
  *
  * @author wdyce
  * @since Dec 15, 2012
  */
 public class BarbarianRegiment extends RegimentAgent
 {
   /* CONSTANTS */
   private static final int REGIMENT_SIZE = 63; // = 1 + 2 + 4 + ... + 16 + 32
   private static final float SPEED_FACTOR = 0.12f;
   
   /* ATTRIBUTES */
   
   /* METHODS */
 
   // constructors
   
   public BarbarianRegiment(V2 position, Tile t, Faction faction)
   {
     super(position, REGIMENT_SIZE, t, faction);
     defense_potential = 1;
     attack_potential = 5;
   }
 
   // accessors
 
   // mutators
   
   /* IMPLEMENTS -- REGIMENTAGENT */
 
   @Override
   protected void ai(int t_delta, Iterable<Tile> percepts)
   {
 	  RomanRegiment nearestRoman = null;
 	  float distanceFromRoman = Float.MAX_VALUE, tmp;
 	  BarbarianRegiment nearestChargingBarbarian = null;
 	  float distanceFromBarbarian = Float.MAX_VALUE;
 	  
 	  for(Tile t : percepts)
 	  {
 		  if(t.agent instanceof RomanRegiment)
 		  {
 			  tmp = t.agent.getCircle().centre.distance2(c.centre);
 			  if(tmp < distanceFromRoman)
 			  {
 				  nearestRoman = (RomanRegiment)t.agent;
 				  distanceFromRoman = tmp;
 			  }
 		  }
 		  else if(t.agent instanceof BarbarianRegiment && t.agent != this)
 		  {
 			  tmp = t.agent.getCircle().centre.distance2(c.centre);
 			  if((t.agent.state == State.CHARGING || t.agent.state == State.FIGHTING) && tmp < distanceFromBarbarian)
 			  {
 				  nearestChargingBarbarian = (BarbarianRegiment)t.agent;
 				  distanceFromBarbarian = tmp;
 			  }
 		  }
 	  }
 	  // stay hidden behind trees while romans are not near enough
 	  // if no romans are visible, stay and wait
 	  // else if not charging or fighting, charge !!!
 	  // else if charging, charge
 	  // else if fighting and has an ennemy, fight
 	  if(nearestRoman != null && state != State.FIGHTING && c.collides(nearestRoman.getCircle()))
 	  {
 		  state = State.FIGHTING;
 	  }
 	  if(state == State.FIGHTING)
 	  {
 		  if(nearestRoman != null)
       	  {
             if(attackArmed)
              attack(nearestRoman);
           }
 		  else
 			  state = State.WAITING;
 	  }
 	  else if(state == State.WAITING)
 	  {
 		  if(nearestRoman != null)// && distanceFromRoman <= getPerceptionRadius())
 		  {
 			  state = State.CHARGING;
 		  }
 		  else if(nearestChargingBarbarian != null)
 		  {
 			  faceTowards(nearestChargingBarbarian.getCircle().centre);
 			  float min = Math.min(SPEED_FACTOR*t_delta, ((float)Math.sqrt(distanceFromBarbarian)-2*c.radius));
 			  advance(min);
 		  }
 	  }
 	  else if(state == State.CHARGING)
 	  {
 		  if(nearestRoman != null)
 		  {
 			  // charge : turn in front of roman, then advance
 			  faceTowards(nearestRoman.getCircle().centre);
 			  float min = Math.min(SPEED_FACTOR*t_delta, (float)Math.sqrt(distanceFromRoman));
 			  advance(min);
 			  if(min == distanceFromRoman)
 				  state = State.FIGHTING;
 			  //System.out.println("je suis "+this+" et ma nouvelle tuile c'est : "+tile);
 		  }
 		  else
 		  {
 			  state = State.WAITING;
 		  }
 	  }
   }
   
   
 }

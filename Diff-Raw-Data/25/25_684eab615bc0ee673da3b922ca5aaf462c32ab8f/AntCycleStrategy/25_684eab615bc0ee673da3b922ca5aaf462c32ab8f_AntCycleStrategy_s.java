 /**
  * AntCycleStrategy.java
  * 
  * Copyright 2010 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.acs.impl;
 
 /**
  * A strategy which adds pheromone to each edge of a tour only after a full
  * cycle is complete, not while the tour is in progress.
  * 
  * The amount of pheromone added to each edge of a tour is inversely
  * proportional to the total distance of that tour. In this way, globally good
  * solutions get more pheromone than locally good solutions (most of the time).
  * 
  * @author Jeffrey Finkelstein
  * @since 0.5
  */
 // TODO is there a simpler way to accomplish this strategy? maybe give one
 // method both edge distance and total distance?
 public class AntCycleStrategy extends AbstractPheromoneUpdateStrategy {
 
   /**
    * Returns the quotient of the pheromone quantity index divided by the
    * specified total distance of the tour.
    * 
    * @param totalDistance
    *          The value by which to divide the pheromone quantity index.
    * @return The quotient of the pheromone quantity index divided by the
    *         specified total distance of the tour.
   * @see jmona.acs.PheromoneUpdateStrategy#trailToAddAllEdges(double)
    * @see jmona.acs.impl.AbstractPheromoneUpdateStrategy#pheromoneQuantityIndex()
    */
   @Override
   public double pheromoneToAddFullCycle(final double totalDistance) {
     return this.pheromoneQuantityIndex() / totalDistance;
   }
 
   /**
    * Always returns zero.
    * 
    * @param edgeDistance
    *          This parameter is ignored.
    * @return Zero.
   * @see jmona.acs.PheromoneUpdateStrategy#trailToAddSingleEdge(double)
    */
   @Override
   public double pheromoneToAddSingleEdge(final double edgeDistance) {
     return NO_PHEROMONE;
   }
 
 }

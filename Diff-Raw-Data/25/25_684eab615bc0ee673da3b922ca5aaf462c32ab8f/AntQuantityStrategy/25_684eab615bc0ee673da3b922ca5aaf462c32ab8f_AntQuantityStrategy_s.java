 /**
  * AntQuantityStrategy.java
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
  * A strategy which adds pheromone to each edge of an Ant's tour as it
  * encounters it.
  * 
  * The amount of pheromone added to each edge is inversely proportional to the
  * distance of that edge. In this way, locally good solutions get more pheromone
  * than globally good solutions (most of the time).
  * 
  * @author Jeffrey Finkelstein
  * @since 0.5
  */
 public class AntQuantityStrategy extends AbstractPheromoneUpdateStrategy {
 
   /**
    * Always returns zero.
    * 
    * @param totalDistance
    *          This parameter is ignored.
    * @return Zero.
   * @see jmona.acs.PheromoneUpdateStrategy#trailToAddAllEdges(double)
    */
   @Override
   public double pheromoneToAddFullCycle(final double totalDistance) {
     return NO_PHEROMONE;
   }
 
   /**
    * Returns the quotient of the pheromone quantity index divided by the
    * specified distance of this particular edge.
    * 
    * @param edgeDistance
    *          The distance of the edge to which this amount of pheromone will be
    *          added.
    * @return The quotient of the pheromone quantity index divided by the
    *         specified distance of this particular edge.
   * @see jmona.acs.PheromoneUpdateStrategy#trailToAddSingleEdge(double)
    * @see jmona.acs.impl.AbstractPheromoneUpdateStrategy#pheromoneQuantityIndex()
    */
   @Override
   public double pheromoneToAddSingleEdge(final double edgeDistance) {
     return this.pheromoneQuantityIndex() / edgeDistance;
   }
 
 }

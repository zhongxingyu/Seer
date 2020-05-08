 /**
  * AntDensityStrategy.java
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
  * A strategy which adds the full pheromone quantity index amount of pheromone
  * to each edge in an Ant's tour, independent of both the weight of the edge and
  * the total distance of the tour.
  * 
  * @author Jeffrey Finkelstein
  * @since 0.5
  */
 public class AntDensityStrategy extends AbstractPheromoneUpdateStrategy {
 
   /**
    * Always returns zero.
    * 
    * @param totalDistance
    *          This parameter is ignored.
    * @return Zero.
   * @see jmona.acs.PheromoneUpdateStrategy#pheromoneToAddFullCycle(double)
    */
   @Override
   public double pheromoneToAddFullCycle(final double totalDistance) {
     return NO_PHEROMONE;
   }
 
   /**
    * Returns the {@link #pheromoneQuantityIndex()} exactly.
    * 
    * @param edgeDistance
    *          This parameter is ignored.
    * @return The pheromone quantity index exactly.
   * @see jmona.acs.PheromoneUpdateStrategy#pheromoneToAddSingleEdge(double)
    * @see jmona.acs.impl.AbstractPheromoneUpdateStrategy#pheromoneQuantityIndex()
    */
   @Override
   public double pheromoneToAddSingleEdge(final double edgeDistance) {
     return this.pheromoneQuantityIndex();
   }
 
 }

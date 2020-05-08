 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.region;
 
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.controlflow.Edge;
 import fr.jamgotchian.abcd.core.graph.MutableDirectedGraph;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class BlockRegion extends AbstractRegion {
  
     private final Set<Edge> edges;
     
     private final List<Region> regions;
     
     BlockRegion(Set<Edge> edges, List<Region> regions) {
         if (edges == null) {
             throw new ABCDException("edges == null");
         }                           
         if (regions == null) {
             throw new ABCDException("regions == null");
         }
         if (regions.size() < 2) {
             throw new ABCDException("regions < 2");
         }
         this.edges = edges;
         this.regions = regions;
     }
     
     public RegionType getType() {
         return RegionType.BLOCK;
     }
 
     public Region getEntryRegion() {
         return regions.get(0);
     }
 
     public Region getExitRegion() {
         return regions.get(regions.size()-1);
     }
 
     public List<Region> getRegions() {
         return Collections.unmodifiableList(regions);
     }
 
     public Collection<Region> getChildRegions() {
         return Collections.unmodifiableList(regions);
     }
 
     public Collection<Edge> getChildEdges() {
         return Collections.unmodifiableSet(edges);
     }
 
     public void collapse(MutableDirectedGraph<Region, Edge> graph) {
         graph.addVertex(this);
         Regions.moveHandlers(graph, regions.get(0), this);
         Regions.moveIncomingEdges(graph, regions.get(0), this);
        Regions.moveOutgoingEdges(graph, regions.get(regions.size()-1), this);
         Regions.removeEdges(graph, edges);
         Regions.removeRegions(graph, regions);
     }
 }

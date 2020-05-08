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
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class TryCatchRegion extends AbstractRegion {
 
     private final Region tryRegion1;
 
     private final Edge tryEdge1;
 
     private Region tryRegion2;
 
     private Edge tryEdge2;
 
     private final Collection<CatchRegion> catchRegions;
 
     TryCatchRegion(Region tryRegion1, Edge tryEdge1, Region tryRegion2, Edge tryEdge2,
                    Collection<CatchRegion> catchRegions) {
         if (tryRegion1 == null) {
             throw new ABCDException("tryRegion1 == null");
         }
         if (tryEdge1 == null) {
             throw new ABCDException("tryEdge1 == null");
         }
         if (catchRegions == null) {
             throw new ABCDException("catchRegions == null");
         }
         if (catchRegions.isEmpty()) {
             throw new ABCDException("catchRegions.isEmpty()");
         }
         this.tryRegion1 = tryRegion1;
         this.tryEdge1 = tryEdge1;
         this.tryRegion2 = tryRegion2;
         this.tryEdge2 = tryEdge2;
         this.catchRegions = catchRegions;
     }
 
     public Region getTryRegion1() {
         return tryRegion1;
     }
 
     public Edge getTryEdge1() {
         return tryEdge1;
     }
 
     public Region getTryRegion2() {
         return tryRegion2;
     }
 
     public Edge getTryEdge2() {
         return tryEdge2;
     }
 
     public Collection<CatchRegion> getCatchRegions() {
         return Collections.unmodifiableCollection(catchRegions);
     }
 
     public RegionType getType() {
         return RegionType.TRY_CATCH;
     }
 
     public Region getEntryRegion() {
         return tryRegion1;
     }
 
     public Region getExitRegion() {
         return null;
     }
 
     public Collection<Region> getChildRegions() {
         List<Region> regions = new ArrayList<Region>();
         regions.add(tryRegion1);
         if (tryRegion2 != null) {
             regions.add(tryRegion2);
         }
         for (CatchRegion _catch : catchRegions) {
             regions.add(_catch.getRegion());
         }
         return regions;
     }
 
     public Collection<Edge> getChildEdges() {
         List<Edge> edges = new ArrayList<Edge>();
         edges.add(tryEdge1);
         if (tryEdge2 != null) {
             edges.add(tryEdge2);
         }
         for (CatchRegion _catch : catchRegions) {
             edges.add(_catch.getIncomingEdge());
             edges.add(_catch.getOutgoingEdge());
         }
         return edges;
     }
 
     public void collapse(MutableDirectedGraph<Region, Edge> graph) {
         graph.addVertex(this);
         CatchRegion firstCatch = catchRegions.iterator().next();
         Regions.moveHandlers(graph, firstCatch.getRegion(), this);
         Regions.moveIncomingEdges(graph, tryRegion1, this);
         Edge exitEdge = tryEdge2 != null ? tryEdge2 : tryEdge1;
         Region exitRegion = graph.getEdgeTarget(exitEdge);
         graph.removeEdge(tryEdge1);
         if (tryEdge2 != null) {
             graph.removeEdge(tryEdge2);
         }
         graph.addEdge(this, exitRegion, exitEdge);
         if (tryRegion2 != null) {
            Regions.moveOutgoingEdges(graph, tryRegion2, this);
         } else {
            Regions.moveOutgoingEdges(graph, tryRegion1, this);
         }
         for (CatchRegion _catch : catchRegions) {
             graph.removeEdge(_catch.getIncomingEdge());
             graph.removeEdge(_catch.getOutgoingEdge());
             graph.removeVertex(_catch.getRegion());
         }
         graph.removeVertex(tryRegion1);
         if (tryRegion2 != null) {
             graph.removeVertex(tryRegion2);
         }
     }
 }

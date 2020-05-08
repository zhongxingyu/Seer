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
 
 import com.google.common.base.Objects;
 import fr.jamgotchian.abcd.core.controlflow.BasicBlock;
 import fr.jamgotchian.abcd.core.controlflow.Edge;
 import fr.jamgotchian.abcd.core.graph.DirectedGraph;
 import fr.jamgotchian.abcd.core.graph.MutableDirectedGraph;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class Regions {
 
     private Regions() {
     }
 
     public static Collection<Edge> getOutgoingEdgesOf(DirectedGraph<Region, Edge> graph,
                                                       Region region, boolean exceptional) {
         List<Edge> outgoingEdges = new ArrayList<Edge>();
         for (Edge edge : graph.getOutgoingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 outgoingEdges.add(edge);
             }
         }
         return outgoingEdges;
     }
 
     public static Collection<Edge> getIncomingEdgesOf(DirectedGraph<Region, Edge> graph,
                                                       Region region, boolean exceptional) {
         List<Edge> incomingEdges = new ArrayList<Edge>();
         for (Edge edge : graph.getIncomingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 incomingEdges.add(edge);
             }
         }
         return incomingEdges;
     }
 
     public static Collection<Region> getSuccessorsOf(DirectedGraph<Region, Edge> graph,
                                                      Region region, boolean exceptional) {
         List<Region> successors = new ArrayList<Region>();
         for (Edge edge : graph.getOutgoingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 successors.add(graph.getEdgeTarget(edge));
             }
         }
         return successors;
     }
 
     public static Collection<Region> getPredecessorsOf(DirectedGraph<Region, Edge> graph,
                                                        Region region, boolean exceptional) {
         List<Region> predecessors = new ArrayList<Region>();
         for (Edge edge : graph.getIncomingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 predecessors.add(graph.getEdgeSource(edge));
             }
         }
         return predecessors;
     }
 
     public static Edge getFirstOutgoingEdgeOf(DirectedGraph<Region, Edge> graph,
                                               Region region, boolean exceptional) {
         for (Edge edge : graph.getOutgoingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 return edge;
             }
         }
         return null;
     }
 
     public static Edge getFirstIncomingEdgeOf(DirectedGraph<Region, Edge> graph,
                                               Region region, boolean exceptional) {
         for (Edge edge : graph.getIncomingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 return edge;
             }
         }
         return null;
     }
 
     public static Region getFirstSuccessorOf(DirectedGraph<Region, Edge> graph,
                                              Region region, boolean exceptional) {
         for (Edge edge : graph.getOutgoingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 return graph.getEdgeTarget(edge);
             }
         }
         return null;
     }
 
     public static Region getFirstPredecessorOf(DirectedGraph<Region, Edge> graph,
                                                Region region, boolean exceptional) {
         for (Edge edge : graph.getIncomingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 return graph.getEdgeSource(edge);
             }
         }
         return null;
     }
 
     public static int getSuccessorCountOf(DirectedGraph<Region, Edge> graph,
                                           Region region, boolean exceptional) {
         int count = 0;
         for (Edge edge : graph.getOutgoingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 count++;
             }
         }
         return count;
     }
 
     public static int getPredecessorCountOf(DirectedGraph<Region, Edge> graph,
                                             Region region, boolean exceptional) {
         int count = 0;
         for (Edge edge : graph.getIncomingEdgesOf(region)) {
             if ((exceptional && edge.isExceptional())
                     || (!exceptional && !edge.isExceptional())) {
                 count++;
             }
         }
         return count;
     }
 
     public static boolean sameHandlers(DirectedGraph<Region, Edge> graph, Region region1, Region region2) {
         Collection<Edge> edges1 = getOutgoingEdgesOf(graph, region1, true);
         Collection<Edge> edges2 = getOutgoingEdgesOf(graph, region2, true);
         if (edges1.size() != edges2.size()) {
             return false;
         }
         for (Edge edge1 : edges1) {
             Region handler1 = graph.getEdgeTarget(edge1);
             String exceptionClassName1 = (String) edge1.getValue();
             boolean found = false;
             for (Edge edge2 : edges2) {
                 Region handler2 = graph.getEdgeTarget(edge2);
                 String exceptionClassName2 = (String) edge2.getValue();
                 if (handler1.equals(handler2)
                         && Objects.equal(exceptionClassName1, exceptionClassName2)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 return false;
             }
         }
         return true;
     }
 
     public static boolean sameHandlers(DirectedGraph<Region, Edge> graph, List<Region> regions) {
         if (regions.size() <= 1) {
             return true;
         }
         for (int i = 1; i < regions.size(); i++) {
             if (!sameHandlers(graph, regions.get(i), regions.get(i-1))) {
                 return false;
             }
         }
         return true;
     }
 
     public static boolean sameHandlers(DirectedGraph<Region, Edge> graph, Region... regions) {
         return sameHandlers(graph, Arrays.asList(regions));
     }
 
     public static void moveHandlers(MutableDirectedGraph<Region, Edge> graph, Region region1, Region region2) {
         Collection<Edge> exceptionalEdges = getOutgoingEdgesOf(graph, region1, true);
         for (Edge edge : exceptionalEdges) {
             Region handler = graph.getEdgeTarget(edge);
             graph.removeEdge(edge);
             graph.addEdge(region2, handler, edge);
         }
     }
 
     public static void moveIncomingEdges(MutableDirectedGraph<Region, Edge> graph, Region from, Region to) {
         Collection<Edge> incomingEdges = graph.getIncomingEdgesOf(from);
         for (Edge incomingEdge : new ArrayList<Edge>(incomingEdges)) {
                 Region source = graph.getEdgeSource(incomingEdge);
                 graph.removeEdge(incomingEdge);
                 graph.addEdge(source, to, incomingEdge);
         }
     }
 
    public static void moveUnexceptionalOutgoingEdges(MutableDirectedGraph<Region, Edge> graph, 
                                                      Region from, Region to) {
         Collection<Edge> outgoingEdges = graph.getOutgoingEdgesOf(from);
         for (Edge outgoingEdge : new ArrayList<Edge>(outgoingEdges)) {
             if (!outgoingEdge.isExceptional()) {
                 Region target = graph.getEdgeTarget(outgoingEdge);
                 graph.removeEdge(outgoingEdge);
                 graph.addEdge(to, target, outgoingEdge);
             }
         }
     }
 
     public static void removeRegions(MutableDirectedGraph<Region, Edge> graph, Collection<Region> regions) {
         for (Region region : regions) {
             graph.removeVertex(region);
         }
     }
 
     public static void removeEdges(MutableDirectedGraph<Region, Edge> graph, Collection<Edge> edges) {
         for (Edge edge : edges) {
             graph.removeEdge(edge);
         }
     }
     
     public static Region getDeepEntryRegion(DirectedGraph<Region, Edge> graph, Region region) {
         Region entry = null;
         for (Region r = region; r.getType() != RegionType.BASIC_BLOCK; r = r.getEntryRegion()) {
             entry = r;
         }
         return entry;
     }
 
     public static Region getDeepExitRegion(DirectedGraph<Region, Edge> graph, Region region) {
         Region entry = null;
         for (Region r = region; r != null && r.getType() != RegionType.BASIC_BLOCK; r = r.getExitRegion()) {
             entry = r;
         }
         return entry;
     }
     
     public static BasicBlock getDeepEntryBasicBlock(DirectedGraph<Region, Edge> graph, Region region) {
         Region r = null;
         for (r = region; r.getType() != RegionType.BASIC_BLOCK; r = r.getEntryRegion()) {
             // nothing
         }
         return ((BasicBlockRegion) r).getBasicBlock();
     }
 
     public static BasicBlock getDeepExitBasicBlock(DirectedGraph<Region, Edge> graph, Region region) {
         Region r = null;
         for (r = region; r != null && r.getType() != RegionType.BASIC_BLOCK; r = r.getExitRegion()) {
             // nothing
         }
         return r == null ? null : ((BasicBlockRegion) r).getBasicBlock();
     }
 }

 /*
  * Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
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
 
 package fr.jamgotchian.abcd.core.graph;
 
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 class DirectedGraphImpl<V, E> implements MutableDirectedGraph<V, E> {
 
     private static class Connection<V> {
 
         private final V source;
 
         private final V target;
 
         private Connection(V source, V target) {
             this.source = source;
             this.target = target;
         }
 
         public V getSource() {
             return source;
         }
 
         public V getTarget() {
             return target;
         }
     }
 
     private static class Neighbors<V, E> {
 
         private final Map<V, E> predecessors;
 
         private final Map<V, E> successors;
 
         private Neighbors() {
             predecessors = new LinkedHashMap<V, E>();
             successors = new LinkedHashMap<V, E>();
         }
 
         public Map<V, E> getPredecessors() {
             return predecessors;
         }
 
         public Map<V, E> getSuccessors() {
             return successors;
         }
     }
 
     private final Map<E, Connection<V>> edges;
 
     private final Map<V, Neighbors<V, E>> vertices;
 
     DirectedGraphImpl() {
         edges = new HashMap<E, Connection<V>>();
         vertices = new HashMap<V, Neighbors<V, E>>();
     }
 
     public void addVertex(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         if (vertices.containsKey(vertex)) {
             throw new ABCDException("Vertex " + vertex + " already present");
         }
         vertices.put(vertex, new Neighbors<V, E>());
     }
 
     public void addEdge(V source, V target, E edge) {
         if (source == null) {
             throw new ABCDException("source == null");
         }
         if (target == null) {
             throw new ABCDException("target == null");
         }
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         if (edges.containsKey(edge)) {
             throw new ABCDException("Edge " + edge + " already present");
         }
         if (!vertices.containsKey(source)) {
             throw new ABCDException("Source vertex " + source + " not found");
         }
         if (!vertices.containsKey(target)) {
             throw new ABCDException("Target vertex " + target + " not found");
         }
         edges.put(edge, new Connection<V>(source, target));
         vertices.get(source).getSuccessors().put(target, edge);
         vertices.get(target).getPredecessors().put(source, edge);
     }
 
     public Set<V> getVertices() {
         return Collections.unmodifiableSet(vertices.keySet());
     }
 
     public int getVertexCount() {
         return vertices.size();
     }
 
     public Set<E> getEdges() {
         return Collections.unmodifiableSet(edges.keySet());
     }
 
     public int getEdgeCount() {
         return edges.size();
     }
 
     public E getEdge(V source, V target) {
         Neighbors<V, E> neighbors = vertices.get(source);
         if (neighbors != null) {
             return neighbors.getSuccessors().get(target);
         }
         neighbors = vertices.get(target);
         if (neighbors != null) {
             return neighbors.getPredecessors().get(source);
         }
         return null;
     }
 
     public boolean containsEdge(V source, V target) {
         return getEdge(source, target) != null;
     }
 
     public V getEdgeSource(E edge) {
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         Connection<V> connection = edges.get(edge);
         if (connection == null) {
             throw new ABCDException("Edge " + edge + " not found");
         }
         return connection.getSource();
     }
 
     public V getEdgeTarget(E edge) {
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         Connection<V> connection = edges.get(edge);
         if (connection == null) {
             throw new ABCDException("Edge " + edge + " not found");
         }
         return connection.getTarget();
     }
 
     public Collection<E> getOutgoingEdgesOf(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         Neighbors<V, E> neighbors = vertices.get(vertex);
         if (neighbors == null) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
         return neighbors.getSuccessors().values();
     }
 
     public E getFirstOutgoingEdgeOf(V vertex) {
         Iterator<E> it = getOutgoingEdgesOf(vertex).iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     public Collection<E> getIncomingEdgesOf(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         Neighbors<V, E> neighbors = vertices.get(vertex);
         if (neighbors == null) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
         return neighbors.getPredecessors().values();
     }
 
     public E getFirstIncomingEdgeOf(V vertex) {
         Iterator<E> it = getIncomingEdgesOf(vertex).iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     public Set<V> getSuccessorsOf(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         Neighbors<V, E> neighbors = vertices.get(vertex);
         if (neighbors == null) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
         return neighbors.getSuccessors().keySet();
     }
 
     public V getFirstSuccessorOf(V vertex) {
         Iterator<V> it = getSuccessorsOf(vertex).iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     public int getSuccessorCountOf(V vertex) {
         return getOutgoingEdgesOf(vertex).size();
     }
 
     public Set<V> getPredecessorsOf(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         Neighbors<V, E> neighbors = vertices.get(vertex);
         if (neighbors == null) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
         return neighbors.getPredecessors().keySet();
     }
 
     public V getFirstPredecessorOf(V vertex) {
         Iterator<V> it = getPredecessorsOf(vertex).iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     public int getPredecessorCountOf(V vertex) {
         return getIncomingEdgesOf(vertex).size();
     }
 
     public void removeEdge(E edge) {
         if (edge == null) {
             throw new ABCDException("edge == null");
         }
         Connection<V> connection = edges.get(edge);
         if (connection == null) {
             throw new ABCDException("Edge " + edge + " not found");
         }
         edges.remove(edge);
         V source = connection.getSource();
         V target = connection.getTarget();
         Neighbors<V, E> neighbors = vertices.get(source);
         if (neighbors != null) {
             neighbors.getSuccessors().remove(target);
         }
         neighbors = vertices.get(target);
         if (neighbors != null) {
             neighbors.getPredecessors().remove(source);
         }
     }
 
     public boolean removeEdge(V source, V target) {
         E edge = getEdge(source, target);
         if  (edge != null) {
             removeEdge(edge);
             return true;
         } else {
             return false;
         }
     }
 
     public void removeVertex(V vertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
 
         if (!vertices.containsKey(vertex)) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
 
         Set<E> edgesToRemove = new HashSet<E>();
 
         Iterator<Map.Entry<E, Connection<V>>> it = edges.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<E, Connection<V>> entry = it.next();
             E edge = entry.getKey();
             Connection<V> connection = entry.getValue();
             if (connection.getSource().equals(vertex) || connection.getTarget().equals(vertex)) {
                 edgesToRemove.add(edge);
             }
         }
 
         for (E edge : edgesToRemove) {
             removeEdge(edge);
         }
 
         vertices.remove(vertex);
     }
 
     public void splitVertex(V vertex, V newVertex) {
         if (vertex == null) {
             throw new ABCDException("vertex == null");
         }
         if (newVertex == null) {
             throw new ABCDException("newVertex == null");
         }
         if (vertices.containsKey(newVertex)) {
             throw new ABCDException("New vertex " + newVertex + " already present");
         }
         Neighbors<V, E> neighbors = vertices.get(vertex);
         if (neighbors == null) {
             throw new ABCDException("Vertex " + vertex + " not found");
         }
         Neighbors<V, E> newNeighbors = new Neighbors<V, E>();
         for (Map.Entry<V, E> entry : neighbors.getSuccessors().entrySet()) {
             V target = entry.getKey();
             E edge = entry.getValue();
             newNeighbors.getSuccessors().put(target, edge);
 
             Neighbors<V, E> targetNeighbors = vertices.get(target);
             targetNeighbors.getPredecessors().remove(vertex);
             targetNeighbors.getPredecessors().put(newVertex, edge);
 
             edges.put(edge, new Connection<V>(newVertex, target));
         }
         neighbors.getSuccessors().clear();
         vertices.put(newVertex, newNeighbors);
     }
 
     public void reversePostOrderDFS(V v, List<V> vertices, List<E> edges, boolean invert) {
         reversePostOrderDFS(v, new HashSet<V>(), vertices, edges, invert);
     }
 
     public void reversePostOrderDFS(V v, Set<V> visited, List<V> vertices, List<E> edges,
                                     boolean invert) {
         visited.add(v);
         for (E e : invert ? getIncomingEdgesOf(v) : getOutgoingEdgesOf(v)) {
             V s = invert ? getEdgeSource(e) : getEdgeTarget(e);
             if (!visited.contains(s)) {
                 reversePostOrderDFS(s, visited, vertices, edges, invert);
                 if (edges != null) {
                     edges.add(0, e);
                 }
             }
         }
         if (vertices != null) {
             vertices.add(0, v);
         }
     }
 
     public Tree<V, E> getReversePostOrderDFST(V root, boolean invert) {
         List<V> dfstBlocks = new ArrayList<V>();
         List<E> dfstEdges = new ArrayList<E>();
         reversePostOrderDFS(root, dfstBlocks, dfstEdges, invert);
 
         // build depth first spanning Tree
         MutableTree<V, E> dfst = new TreeImpl<V, E>(root);
         for (int i = 0; i < dfstEdges.size(); i++) {
             E edge = dfstEdges.get(i);
             V parent = getEdgeSource(edge);
             V node = getEdgeTarget(edge);
             dfst.addNode(parent, node, edge);
         }
 
         return dfst;
     }
 
     public String toString(E edge) {
         return getEdgeSource(edge) + "->" + getEdgeTarget(edge);
     }
 
     public String toString(Collection<E> edges) {
         StringBuilder builder = new StringBuilder("[");
         for (Iterator<E> it = edges.iterator(); it.hasNext();) {
             builder.append(toString(it.next()));
             if (it.hasNext()) {
                 builder.append(", ");
             }
         }
         builder.append("]");
         return builder.toString();
     }
 
     public void writeDOT(Writer writer, String name) throws IOException {
         writeDOT(writer, name, new DefaultAttributeProvider<V>(),
                 new DefaultAttributeProvider<E>());
     }
 
     public void writeDOT(Writer writer, String name, AttributeProvider<V> vertexAttrs,
                          AttributeProvider<E> edgeAttrs) throws IOException {
         writer.append("digraph ").append(name).append(" {\n");
         for (E edge : getEdges()) {
             V source = getEdgeSource(edge);
             V target = getEdgeTarget(edge);
             DirectedGraphs.writeEdgeDOT(writer, edge, source, target, edgeAttrs);
         }
         for (V vertex : getVertices()) {
             DirectedGraphs.writeVertexDOT(writer, vertex, vertexAttrs);
         }
         writer.append("}");
     }
 
     @Override
     public DirectedGraphImpl<V, E> clone() {
         DirectedGraphImpl<V, E> clone = new DirectedGraphImpl<V, E>();
         for (V v : getVertices()) {
            addVertex(v);
         }
         for (E e : getEdges()) {
            addEdge(getEdgeSource(e), getEdgeTarget(e), e);
         }
         return clone;
     }
 }

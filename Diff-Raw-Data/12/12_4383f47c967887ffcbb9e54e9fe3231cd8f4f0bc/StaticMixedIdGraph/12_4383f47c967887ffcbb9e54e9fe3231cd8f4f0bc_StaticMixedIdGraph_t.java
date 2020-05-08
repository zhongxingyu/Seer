 /*
  * Copyright (C) 2013 Marton Makai
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package qdg;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Iterator;
 
 import qdg.api.EntityMap;
 import qdg.api.MixedGraph;
 import qdg.bits.AbstractIdEntity;
 import qdg.bits.AbstractIdMap;
 import qdg.bits.AbstractMixedGraph;
 import qdg.bits.ArcLace;
 import qdg.bits.ArcLace.ArcData;
 import qdg.bits.ArcLace.NodeData;
 import qdg.bits.ConcatIterator;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 
 /**
 * Mixed graph with integer ids of the nodes and edges. Ids have to be provided
 * by the caller when the nodes and edges are added, with the attention that
 * the node ids as well the edge ids should be unique.
 * 
 * Nodes and edges are stored in arrays with their ids corresponding to
 * indices, hence memory consumption is proportional to the maximum id.
 * 
 * One particular use case is to copy a graph or a subgraph of a graph
 * indexed by integers, with keeping the integer ids of the underlying graph.
  * 
  * @author Marton Makai
  */
 public class StaticMixedIdGraph extends AbstractMixedGraph
 		implements MixedGraph, Serializable {
 	
 	private static final long serialVersionUID = 4444550416599231556L;
 
 	public static class N extends AbstractIdEntity
 			implements Node {
 		
 		public N(int id) {
 			super(id);
 		}
 	
 		@Override
 		public String toString() {
 			return "Node [id=" + id + "]";
 		}
 	}
 	
 	protected static class CombinedNodeData implements Serializable {
 		
 		private static final long serialVersionUID = 8855112085916381992L;
 
 		protected NodeData arcData = new NodeData();
 		
 		protected NodeData uEdgeData = new NodeData();
     	
 		@Override
 		public String toString() {
 			return "CombinedNodeData [arcData=" + arcData + ", uEdgeData="
 					+ uEdgeData + "]";
 		}
 	}
 	
 	public static class E extends AbstractIdEntity
 			implements Edge {
 		
 		private boolean directed;
 
 		public E(int id, boolean directed) {
 			super(id);
 			this.directed = directed;
 		}
 
 		@Override
 		public String toString() {
 			return "E [directed=" + directed + ", id=" + id + "]";
 		}
 	}
 	
 	@Override
 	public boolean isDirected(Edge edge) {
 		return ((E) edge).directed;
 	}
 	
 	protected StaticSparseArrayList<CombinedNodeData> nodes =
 			new StaticSparseArrayList<CombinedNodeData>();
 	
 	private class ArcNodeMap implements EntityMap<Integer, NodeData> {
 		
 		@Override
 		public NodeData put(Integer k, NodeData v) {
 			CombinedNodeData c = nodes.get(k);
 			if (c == null) {
 				c = new CombinedNodeData();
 			}
 			c.arcData = v;
 			return v;
 		}
 
 		@Override
 		public NodeData get(Object k) {
 			CombinedNodeData c = nodes.get(k);
 			if (c == null) {
 				return null;
 			}
 			return c.arcData;
 		}
 	}
 	
 	protected transient EntityMap<Integer, NodeData> arcNodes =
 			new ArcNodeMap();
 	
 	private class UEdgeNodeMap implements EntityMap<Integer, NodeData> {
 		
 		@Override
 		public NodeData put(Integer k, NodeData v) {
 			CombinedNodeData c = nodes.get(k);
 			if (c == null) {
 				c = new CombinedNodeData();
 			}
 			c.uEdgeData = v;
 			return v;
 		}
 
 		@Override
 		public NodeData get(Object k) {
 			CombinedNodeData c = nodes.get(k);
 			if (c == null) {
 				return null;
 			}
 			return c.uEdgeData;
 		}
 	}
 	
 	protected transient EntityMap<Integer, NodeData> uEdgeNodes =
 			new UEdgeNodeMap();
 	
 	protected StaticSparseArrayList<ArcData<Integer>> arcData =
 			new StaticSparseArrayList<ArcData<Integer>>();
 	
 	protected transient ArcLace<Integer> arcLace =
 			new ArcLace<Integer>(arcNodes, arcData);
 	
 	protected StaticSparseArrayList<ArcData<Integer>> uEdgeData =
 			new StaticSparseArrayList<ArcData<Integer>>();
 	
 	protected transient ArcLace<Integer> uEdgeLace =
 			new ArcLace<Integer>(uEdgeNodes, uEdgeData);
 	
 	private static Function<Integer, Node> constructNode =
 			new Function<Integer, Node>() {
 
 		@Override
 		public Node apply(Integer id) {
 			return new N(id);
 		}
 	};
 	
 	private static Function<Integer, Edge> constructArc =
 			new Function<Integer, Edge>() {
 
 		@Override
 		public Edge apply(Integer id) {
 			return new E(id, true);
 		}
 	};
 	
 	private static Function<Integer, Edge> constructUEdge =
 			new Function<Integer, Edge>() {
 
 		@Override
 		public Edge apply(Integer id) {
 			return new E(id, false);
 		}
 	};
 	
 	@Override
 	public Node getSource(Edge edge) {
 		if (((E) edge).directed) {
 			return new N(arcLace.getSource(((E) edge).getId()));
 		} else {
 			return new N(uEdgeLace.getSource(((E) edge).getId()));
 		}
 	}
 
 	@Override
 	public Node getTarget(Edge edge) {
 		if (((E) edge).directed) {
 			return new N(arcLace.getTarget(((E) edge).getId()));
 		} else {
 			return new N(uEdgeLace.getTarget(((E) edge).getId()));
 		}
 	}
 	
 	@Override
 	public Iterator<Node> getNodeIterator() {
 		return Iterators.transform(nodes.keyIterator(), constructNode);
 	}
 	
 	@Override
 	public Iterator<Edge> getArcIterator() {
 		return Iterators.transform(arcLace.getArcIterator(), constructArc);
 	}
 
 	@Override
 	public Iterator<Edge> getUEdgeIterator() {
 		return Iterators.transform(uEdgeLace.getArcIterator(), constructUEdge);
 	}
 
 	@Override
 	public Iterator<Edge> getOutArcIterator(Node node) {
 		return Iterators.transform(
 				arcLace.getOutArcIterator(((N) node).getId()), constructArc);
 	}
 
 	@Override
 	public Iterator<Edge> getInArcIterator(Node node) {
 		return Iterators.transform(
 				arcLace.getInArcIterator(((N) node).getId()), constructArc);
 	}
 
 	@Override
 	public Iterator<Edge> getIncidentArcIterator(Node node) {
 		return Iterators.transform(new ConcatIterator<Integer>(
 				arcLace.getOutArcIterator(((N) node).getId()),
 				arcLace.getInArcIterator(((N) node).getId())), constructArc);
 	}
 
 	@Override
 	public Iterator<Edge> getIncidentUEdgeIterator(Node node) {
 		return Iterators.transform(new ConcatIterator<Integer>(
 				uEdgeLace.getOutArcIterator(((N) node).getId()),
 				uEdgeLace.getInArcIterator(((N) node).getId())),
 				constructUEdge);
 	}
 	
 	public Node addNode(int id) {
 		nodes.put(id, new CombinedNodeData());
 		return new N(id);
 	}
 	
 	public Edge addArc(int id, Node source, Node target) {
 		N s = (N) source;
 		N t = (N) target;
 		arcData.put(id, new ArcData<Integer>(s.getId(), t.getId()));
 		arcLace.laceArc(id, s.getId(), t.getId());
 		return new E(id, true);
 	}
 	
 	public Edge addUEdge(int id, Node source, Node target) {
 		N s = (N) source;
 		N t = (N) target;
 		uEdgeData.put(id, new ArcData<Integer>(s.getId(), t.getId()));
 		uEdgeLace.laceArc(id, s.getId(), t.getId());
 		return new E(id, false);
 	}
 	
 	protected class NodeMap<V> extends AbstractIdMap<Node, V> {
 		
 		private static final long serialVersionUID = 1167032365456389038L;
 
 		@Override
 		public String toString() {
 			return "NodeMap [" +
 					Joiner.on(", ").join(Iterables.transform(getNodes(), f)) +
 					"]";
 		}
 	}
 
 	@Override
 	public <V> EntityMap<Node, V> createNodeMap() {
 		return new NodeMap<V>();
 	}
 	
 	protected class EdgeMap<V> implements EntityMap<Edge, V>, Serializable {
 		
 		private static final long serialVersionUID = -3899482894397129726L;
 
 		protected ArcMap<V> arcMap = new ArcMap<V>();
 		
 		protected UEdgeMap<V> uEdgeMap = new UEdgeMap<V>();
 		
 		@Override
 		public V put(Edge k, V v) {
 			if (((E) k).directed) {
 				return arcMap.put((Edge) k, v);
 			} else {
 				return uEdgeMap.put((Edge) k, v);
 			}
 		}
 
 		@Override
 		public V get(Object k) {
 			if (((E) k).directed) {
 				return arcMap.get((Edge) k);
 			} else {
 				return uEdgeMap.get((Edge) k);
 			}
 		}
 		
 		private class PrintEntry implements Function<Edge, String> {
 
 			@Override
 			public String apply(Edge k) {
 				return "" + k + "=" + get(k);
 			}
 		};
 		
 		protected transient Function<Edge, String> f = new PrintEntry();
 		
 		@Override
 		public String toString() {
 			return "EdgeMap [" +
 					Joiner.on(", ").join(Iterables.transform(getEdges(), f)) +
 					"]";
 		}
 
 		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 			in.defaultReadObject();
 			f = new PrintEntry();
 		}
 		
 		private void writeObject(ObjectOutputStream out) throws IOException {
 			out.defaultWriteObject();
 		}
 	}
 
 	@Override
 	public <V> EntityMap<Edge, V> createEdgeMap() {
 		return new EdgeMap<V>();
 	}
 	
 	protected class ArcMap<V> extends AbstractIdMap<Edge, V> {
 		
 		private static final long serialVersionUID = -5275110474745662141L;
 
 		@Override
 		public String toString() {
 			return "ArcMap [" +
 					Joiner.on(", ").join(Iterables.transform(getArcs(), f)) +
 					"]";
 		}
 	}
 
 	protected class UEdgeMap<V> extends AbstractIdMap<Edge, V> {
 		
 		private static final long serialVersionUID = 7117159471854014975L;
 
 		@Override
 		public String toString() {
 			return "EdgeMap [" +
 					Joiner.on(", ").join(Iterables.transform(getUEdges(), f)) +
 					"]";
 		}
 	}
 	
 	@Override
 	public <V> EntityMap<Edge, V> createUEdgeMap() {
 		return new UEdgeMap<V>();
 	}
 	
 	@Override
 	public <V> EntityMap<Edge, V> createArcMap() {
 		return new ArcMap<V>();
 	}
 	
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		arcLace = new ArcLace<Integer>(arcNodes, arcData);
 		uEdgeLace = new ArcLace<Integer>(uEdgeNodes, uEdgeData);
 		arcNodes = new ArcNodeMap();
 		uEdgeNodes = new UEdgeNodeMap();
 	}
 	
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 	}
 }

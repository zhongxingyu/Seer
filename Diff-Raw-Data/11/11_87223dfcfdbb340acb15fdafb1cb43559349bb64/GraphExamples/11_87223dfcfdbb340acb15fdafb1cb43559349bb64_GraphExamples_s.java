 package logic.extlib;
 
 import defs.DecorableConstants;
 import defs.FormatHelper;
 import defs.VertexFormat;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import logic.AlgorithmDataProcessor;
 import logic.Recorder;
 
 public class GraphExamples<V, E> {
 	static final Object ACTIVE = DecorableConstants.ACTIVE;
 	static final Object VISITED = DecorableConstants.VISITED;
 	static final Object NUMBER = DecorableConstants.NUMBER;
 	static final Object INCOUNT = DecorableConstants.INCOUNT;
 	static final Object DISTANCE = DecorableConstants.DISTANCE;
 	static final Object PQLOCATOR = DecorableConstants.PQLOCATOR;
 	static final Object WEIGHT = DecorableConstants.WEIGHT;
 	static final Object MSF = DecorableConstants.MSF;
 	private Recorder recorder;
 
 	public void setRecorder(AlgorithmDataProcessor processor) {
 		this.recorder = new Recorder(processor);
 	}
 
 	public final int kruskal(Graph<V, E> g) {
 		if (g.isDirected())
 			throw new RuntimeException("We need an undirected graph!");
 		// Returns the number of connected components
 		// Finds the minimum spanning forrest:
 		// each edge belonging to the minimum spanning forrest gets
 		// an attribute MSF. The value of MSF is an integer
 		// indicating the number of the connected component
 		// it belongs to. The attribute
 		// MSF is also assigned to the vertices.
 		recorder.recordStep(g);
 		int n = g.numberOfVertices();
 		final Object CLUSTER = new Object();
 		ArrayList<Vertex<V>>[] clusters = new ArrayList[n];
 		Iterator<Vertex<V>> it = g.vertices();
 		int num = 0;// g.numberOfVertices();
 		while (it.hasNext()) {
 			// all vertices are in their own cluster
 			Vertex<V> v = it.next();
 			clusters[num].add(v);
 			v.set(CLUSTER, clusters[num++]);
 			v.set(MSF, null); // show to which component v belongs
 			recorder.recordStep(g);
 		}
 		HeapPriorityQueue<Double, Edge<E>> pq = new HeapPriorityQueue<>();
 		Iterator<Edge<E>> eit = g.edges();
 		while (eit.hasNext()) {
 			double w = 1.0;
 			Edge<E> e = eit.next();
 			if (e.has(WEIGHT))
 				w = (Double) e.get(WEIGHT);
 			pq.insert(w, e);
 		}
 		while (!pq.isEmpty()) {
 			// take the shortest edge from hq:
 			Edge<E> e = pq.removeMin().element();
 			// get the endPoints:
 			Vertex<V>[] endV = g.endVertices(e);
 			ArrayList<Vertex<V>> c0 = (ArrayList<Vertex<V>>) endV[0]
 					.get(CLUSTER);
 			ArrayList<Vertex<V>> c1 = (ArrayList<Vertex<V>>) endV[1]
 					.get(CLUSTER);
 			if (c0 != c1) {
 				if (c1.size() < c0.size()) {
 					ArrayList<Vertex<V>> tmp = c0;
 					c0 = c1;
 					c1 = tmp;
 				}
 				e.set(MSF, c0);
 				recorder.recordStep(g);
 				num--;
 				// now copy all elements from c1 to c0 and change the attribute
 				for (Vertex<V> v : c1) {
 					c0.add(v);
 					v.set(CLUSTER, c0);
 				}
 				c1.clear();
 			}
 		}
 		return num;
 	}
 
 	public void findGateways(Graph<V, E> g, boolean dijkstra) {
 		// if 'v' and 'w' are vertices of 'g' then
 		// 'v' has a attribute 'w'. The the value of
 		// this attribute is (say) 'u'. Then 'u' has
 		// the following meaning: if there is no path
 		// from 'v' to 'w', 'u' is null. Otherwise
 		// 'u' is the first vertex (after 'v') on a shortest path
 		// fromn 'v' to 'w'.
 		// (where the lenght of a path is
 		// the number of vertices on the path->hopping distance)
 		// if dijkstra is set to true we take into acount
 		// the weights of the edges (instead of the hopping distance)
 
 		Iterator<Vertex<V>> it = g.vertices();
 		while (it.hasNext()) {
 			Vertex<V> v = it.next();
 			// clear the attribute v for all vertices
 			Iterator<Vertex<V>> it2 = g.vertices();
 			while (it2.hasNext()) {
 				Vertex<V> w = it2.next();
 				if (v.has(w))
 					w.destroy(v);
 				v.set(w, null);
 			}
 			// now find the shortest pathes to all u and
 			// set the attribute v
 			if (dijkstra)
 				dijkstra(g, v);
 			else
 				bfs(g, v);
 		}
 	}
 
 	public void dijkstra(Graph<V, E> g, Vertex<V> s) {
 		recorder.recordStep(g);
 		HeapPriorityQueue<Double, Vertex<V>> hq = new HeapPriorityQueue<>();
 		Iterator<Vertex<V>> it = g.vertices();
 		while (it.hasNext()) {
 			Vertex<V> v = it.next();
 			v.set(DISTANCE, Double.POSITIVE_INFINITY);
 			Locator<Double, Vertex<V>> loc = hq.insert(
 					(Double) v.get(DISTANCE), v);
 			v.set(PQLOCATOR, loc);
 			s.set(v, null);
 			s.set(DISTANCE, 0.0);
 		}
 		hq.replaceKey((Locator<Double, Vertex<V>>) s.get(PQLOCATOR), 0.0);
 
 		System.out.println("Distance StartVertex" + s.get(DISTANCE));
 		s.set(s, s);
 		while (!hq.isEmpty()) {
 			Locator<Double, Vertex<V>> loc = hq.removeMin();
 			Vertex<V> v = loc.element();
 			// no modify the distance of all neighbours
 			Iterator<Edge<E>> eit;
 			if (g.isDirected())
 				eit = g.incidentOutEdges(v);
 			else
 				eit = g.incidentEdges(v);
 			while (eit.hasNext()) {
 				Edge<E> e = eit.next();
 				double weight = 1.0; // if no weight is entered
 				if (e.has(WEIGHT))
 					weight = (Double) e.get(WEIGHT);
 
 				Vertex<V> u = g.opposite(e, v);
 				// System.out.println("Vertex " +
 				// FormatHelper.getFormat(VertexFormat.class, u).getLabel() +
 				// " Distance " + u.get(DISTANCE));
 				double newDist = (Double) u.get(DISTANCE) + weight;
 				;
 				if ((Double) u.get(DISTANCE) < Double.POSITIVE_INFINITY) {
 					newDist = (Double) u.get(DISTANCE) + weight;
 				} else {
 					newDist = weight;
 				}
 
 				if (newDist < (Double) u.get(DISTANCE)) {
 					u.set(DISTANCE, newDist);
 					hq.replaceKey(
 							(Locator<Double, Vertex<V>>) u.get(PQLOCATOR),
 							newDist);
 					// now set as best gateway (until now) v
 					// i.e v is the gateway from u to s.
 					if (v == s) {
 						s.set(u, u);
 
 					} else {
 						s.set(u, s.get(v));
 
 					}
 					recorder.recordStep(g);
 				}
 			}
 
 		}
 
 	}
 
 	public void setTopologicalNumbers(Graph<V, E> g) {
 		if (!g.isDirected())
 			throw new RuntimeException("Not directed!");
 		LinkedList<Vertex<V>> li = new LinkedList<>();
 		Iterator<Vertex<V>> it = g.vertices();
 		while (it.hasNext()) {
 			Vertex<V> v = it.next();
 			int cnt = g.inDegree(v);
 			v.set(INCOUNT, cnt);
 			if (cnt == 0)
 				li.add(v);
 
 		}
 		int num = 0;
 		while (!li.isEmpty()) {
 			Vertex<V> v = li.remove();
 			v.set(NUMBER, num++);
 			Iterator<Edge<E>> eit = g.incidentOutEdges(v);
 			while (eit.hasNext()) {
 				Vertex<V> w = g.destination(eit.next());
 				int newCnt = (Integer) w.get(INCOUNT) - 1;
 				w.set(INCOUNT, newCnt);
 				if (newCnt == 0)
 					li.add(w);
 			}
 		}
 		if (num != g.numberOfVertices())
 			throw new RuntimeException("not a DAG!");
 	}
 
 	public void bfs(Graph<V, E> g, Vertex<V> v) {
 		// add the attribute v to all vertices w
 		// the value of this attribute is the first
 		// vertex on a shortest path from w to v
 		LinkedList<Vertex<V>> li = new LinkedList<Vertex<V>>();
 
 		v.set(v, v);
 		v.set(VISITED, null);
 		recorder.recordStep(g);
 		li.addFirst(v);
 		while (li.size() > 0) {
 			Vertex<V> w = li.removeLast();
 			// FormatHelper.getFormat(VertexFormat.class, w).setActive();
 			// recorder.recordStep(g);
 			Iterator<Edge<E>> eit;
 			if (g.isDirected())
 				eit = g.incidentOutEdges(w);
 			else
 				eit = g.incidentEdges(w);
 			while (eit.hasNext()) {
 				Edge<E> e = eit.next();
 				Vertex<V> u = g.opposite(e, w);
 				if (v.get(u) == null) {// u not yet visited
 					if (w == v) {
 						v.set(u, u);
 					} else {
 						v.set(u, v.get(w));
 					}
 					li.addFirst(u);
 					e.set(VISITED, null);
 				}
 			}
 			w.set(VISITED, null);
 			recorder.recordStep(g);
 
 		}
 	}
 
 	public LinkedList<Vertex<V>> findPath(Graph<V, E> g, Vertex<V> v,
 			Vertex<V> w) {
 		LinkedList<Vertex<V>> li = new LinkedList<>();
 		if ((Vertex<V>) v.get(w) == null)
 			return li;
 		Vertex<V> next = v;
 		while (next != null) {
 			li.add(next);
 			if (next == w)
 				break;
 			next = (Vertex<V>) next.get(w);
 		}
 		return li;
 	}
 
 	public boolean isConnected(Graph<V, E> g) {
 		Vertex<V> v = g.aVertex();
 		traverse(g, v);
 		Iterator<Vertex<V>> it = g.vertices();
 		int n = 0;
 		while (it.hasNext()) {
 			Vertex<V> w = it.next();
 			if (w.has(VISITED)) {
 				w.destroy(VISITED);
 				n++;
 			}
 		}
 		return n == g.numberOfVertices();
 	}
 
 	/**
 	 * @param g
 	 * @param v
 	 */
 	private void traverse(Graph<V, E> g, Vertex<V> v) {
 		v.set(VISITED, null);
 		recorder.recordStep(g);
 		// System.out.println(v);
 		// now start the traversal at
 		// all neighbours which are
 		// not allready visited
 		Iterator<Edge<E>> it = g.incidentEdges(v);
 		while (it.hasNext()) {
 			Vertex<V> w = g.opposite(it.next(), v);
 			if (!w.has(VISITED)) {
 				traverse(g, w);
 			}
 		}
 
 	}
 
 	/**
 	 * @param args
 	 * 
 	 */
 	public static void main(String[] args) {
 		IncidenceListGraph<String, String> g = new IncidenceListGraph<String, String>(
 				true);
 		GraphExamples<String, String> ge = new GraphExamples<>();
 		// add an example graph:
 		//
 		// A-->B-->C-->D
 		// |\ | | /|
 		// | \ | | / |
 		// v vv vv v
 		// E-->F G-->H
 		// | / /|\ |
 		// | / / | \ |
 		// vv v v vv
 		// I-->J-->K L
 		// |\ /| |
 		// | \ / | |
 		// v v v v v
 		// M-->N-->O-->P
 		// vertices
 		Vertex<String> vA = g.insertVertex("A");
 		Vertex<String> vB = g.insertVertex("B");
 		Vertex<String> vC = g.insertVertex("C");
 		Vertex<String> vD = g.insertVertex("D");
 		Vertex<String> vE = g.insertVertex("E");
 		Vertex<String> vF = g.insertVertex("F");
 		Vertex<String> vG = g.insertVertex("G");
 		Vertex<String> vH = g.insertVertex("H");
 		Vertex<String> vI = g.insertVertex("I");
 		Vertex<String> vJ = g.insertVertex("J");
 		Vertex<String> vK = g.insertVertex("K");
 		Vertex<String> vL = g.insertVertex("L");
 		Vertex<String> vM = g.insertVertex("M");
 		Vertex<String> vN = g.insertVertex("N");
 		Vertex<String> vO = g.insertVertex("O");
 		Vertex<String> vP = g.insertVertex("P");
 		// edges:
 		/* Edge<String> eAB = */g.insertEdge(vA, vB, "AB");
 		g.insertEdge(vB, vC, "BC");
 		g.insertEdge(vC, vD, "CD");
 		g.insertEdge(vA, vE, "AE");
 		g.insertEdge(vA, vF, "AF");
 		g.insertEdge(vB, vF, "BF");
 		g.insertEdge(vC, vG, "CG");
 		g.insertEdge(vD, vG, "DG");
 		g.insertEdge(vD, vH, "DH");
 		g.insertEdge(vE, vF, "EF");
 		g.insertEdge(vG, vH, "GH");
 		g.insertEdge(vE, vI, "EI");
 		g.insertEdge(vF, vI, "FI");
 		g.insertEdge(vG, vJ, "GJ");
 		g.insertEdge(vG, vK, "GK");
 		g.insertEdge(vG, vL, "GL");
 		g.insertEdge(vH, vL, "HL");
 		g.insertEdge(vI, vJ, "IJ");
 		g.insertEdge(vJ, vK, "JK");
 		g.insertEdge(vI, vM, "IM");
 		g.insertEdge(vI, vN, "IN");
 		g.insertEdge(vK, vN, "KN");
 		g.insertEdge(vK, vO, "KO");
 		g.insertEdge(vL, vP, "LP");
 		g.insertEdge(vM, vN, "MN");
 		g.insertEdge(vN, vO, "NO");
 		g.insertEdge(vO, vP, "OP");
 
 		// final int N = 1000;
 		// Random rand = new Random(322873);
 		// Vertex<String> [] vs = new Vertex[N];
 		// for (int i = 0;i<N;i++){
 		// vs[i] = g.insertVertex("v"+i);
 		// }
 		// for (int i=0;i<N;i++)
 		// for (int k=i+1;k<N;k++)
 		// if (rand.nextDouble()< 0.005)
 		// g.insertEdge(vs[i],vs[k], i+":"+k);
 		// findGateways(g);
 		// System.out.println("path "+findPath(g, vs[0], vs[N-1]));
 		// System.out.println("connected? "+isConnected(g));
 		System.out.println(g);
 		ge.setTopologicalNumbers(g);
 		// LinkedList<Vertex<String>> vList = new LinkedList<Vertex<String>>();
 		// dfSearch(g, vA,vList);
 		// System.out.println(vList);
 		// Iterator<Edge<String>> eit = g.edges();
 		// while(eit.hasNext()){
 		// Edge<String> e = eit.next();
 		// if (e.has(DISCOVERY_EDGE)) System.out.println(e);
 		// }
 	}
 
 	public void customAlgorithm(Graph<V, E> g, Vertex<V> vStart) {
 		testalgorithm(g, vStart);
 	}
 
 	private void testalgorithm(Graph<V, E> g, Vertex<V> vStart) {
 		vStart.set(ACTIVE, null);
 		recorder.recordStep(g);
 		Iterator<Edge<E>> itE = g.incidentEdges(vStart);
 		Edge<E> e = null;
		Vertex<V> vNext = null;
 		int i = 10;
 		while (itE.hasNext()) {
 			e = itE.next();
			vNext = g.opposite(e, vStart);
 			e.set(ACTIVE, null);
 			vStart.set(VISITED, null);
 			vStart.set(DISTANCE, i);
 			recorder.recordStep(g);
 			e.set(VISITED, null);
			vNext.set(ACTIVE, null);
 			recorder.recordStep(g);
			vStart = vNext;
 			i += 10;
 		}
 	}
 }

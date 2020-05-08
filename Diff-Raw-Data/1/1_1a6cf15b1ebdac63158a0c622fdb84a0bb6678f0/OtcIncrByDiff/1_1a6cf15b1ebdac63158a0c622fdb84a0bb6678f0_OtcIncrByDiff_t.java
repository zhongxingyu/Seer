 package dna.metrics.triangles.open;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import dna.diff.Diff;
 import dna.diff.DiffNotApplicableException;
 import dna.graph.Edge;
 import dna.graph.Graph;
 import dna.graph.Node;
 import dna.metrics.triangles.ClusteringCoefficient;
 
 public class OtcIncrByDiff extends ClusteringCoefficient {
 
 	public OtcIncrByDiff() {
 		super("otcIncrByDiff", true, false, true);
 	}
 
 	@Override
 	protected void init(Graph g) {
		super.init(g);
 		this.nodeTriangles = new ArrayList<Set<OpenTriangle>>(
 				g.getNodes().length);
 		this.nodePotentials = new ArrayList<Set<OpenTriangle>>(
 				g.getNodes().length);
 		for (int i = 0; i < g.getNodes().length; i++) {
 			this.nodeTriangles.add(new HashSet<OpenTriangle>());
 			this.nodePotentials.add(new HashSet<OpenTriangle>());
 		}
 		this.triangles = new HashSet<OpenTriangle>();
 		this.potentials = new HashSet<OpenTriangle>();
 	}
 
 	private ArrayList<Set<OpenTriangle>> nodeTriangles;
 
 	private ArrayList<Set<OpenTriangle>> nodePotentials;
 
 	private Set<OpenTriangle> triangles;
 
 	private Set<OpenTriangle> potentials;
 
 	public Set<OpenTriangle> getTriangles() {
 		return this.triangles;
 	}
 
 	public Set<OpenTriangle> getPotentials() {
 		return this.potentials;
 	}
 
 	@Override
 	protected boolean compute_() {
 		try {
 			for (Node n : this.g.getNodes()) {
 				for (Node u : n.getNeighbors()) {
 					for (Node v : n.getNeighbors()) {
 						if (u.getIndex() == v.getIndex()) {
 							continue;
 						}
 						OpenTriangle t;
 						t = new OpenTriangle(n, u, v);
 						if (this.potentials.add(t)) {
 							this.potentialCount++;
 							this.nodePotentialCount[n.getIndex()]++;
 						}
 						if (u.hasOut(v)) {
 							if (this.triangles.add(t)) {
 								this.triangleCount++;
 								this.nodeTriangleCount[n.getIndex()]++;
 							}
 						}
 					}
 				}
 			}
 			this.computeCC();
 			return true;
 		} catch (InvalidOpenTriangleException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	protected boolean applyBeforeDiff_(Diff d) {
 		try {
 			for (Edge e : d.getRemovedEdges()) {
 				Node v = e.getSrc();
 				Node w = e.getDst();
 				// System.out.println("removing edge: " + e);
 				// (1)
 				for (Node x : intersect(v.getNeighbors(), w.getNeighbors())) {
 					this.remove(new OpenTriangle(x, v, w), 1);
 				}
 				if (!this.g.containsEdge(new Edge(w, v))) {
 					// System.out.println("continue...");
 					continue;
 				}
 				// (2)
 				for (Node x : intersect(v.getNeighbors(), w.getIn())) {
 					this.remove(new OpenTriangle(v, x, w), 2);
 				}
 				// (3)
 				for (Node x : intersect(v.getNeighbors(), w.getOut())) {
 					this.remove(new OpenTriangle(v, w, x), 3);
 				}
 				// (4)
 				for (Node x : intersect(w.getNeighbors(), v.getIn())) {
 					this.remove(new OpenTriangle(w, x, v), 4);
 				}
 				// (5)
 				for (Node x : intersect(w.getNeighbors(), v.getOut())) {
 					this.remove(new OpenTriangle(w, v, x), 5);
 				}
 				// (6)
 				for (Node x : v.getNeighbors()) {
 					if (x.equals(w)) {
 						continue;
 					}
 					this.removePotential(new OpenTriangle(v, w, x), 6);
 					this.removePotential(new OpenTriangle(v, x, w), 6);
 				}
 				// (7)
 				for (Node x : w.getNeighbors()) {
 					if (x.equals(v)) {
 						continue;
 					}
 					this.removePotential(new OpenTriangle(w, v, x), 7);
 					this.removePotential(new OpenTriangle(w, x, v), 7);
 				}
 			}
 			return true;
 		} catch (InvalidOpenTriangleException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	protected boolean applyAfterDiff_(Diff d) {
 		try {
 			for (Edge e : d.getAddedEdges()) {
 				Node v = e.getSrc();
 				Node w = e.getDst();
 				// System.out.println("adding edge: " + e);
 				// (1)
 				for (Node x : intersect(v.getNeighbors(), w.getNeighbors())) {
 					this.add(new OpenTriangle(x, v, w), 1);
 				}
 				if (!this.g.containsEdge(new Edge(w, v))) {
 					// System.out.println("continue...");
 					continue;
 				}
 				// (2)
 				for (Node x : intersect(v.getNeighbors(), w.getIn())) {
 					this.add(new OpenTriangle(v, x, w), 2);
 				}
 				// (3)
 				for (Node x : intersect(v.getNeighbors(), w.getOut())) {
 					this.add(new OpenTriangle(v, w, x), 3);
 				}
 				// (4)
 				for (Node x : intersect(w.getNeighbors(), v.getIn())) {
 					this.add(new OpenTriangle(w, x, v), 4);
 				}
 				// (5)
 				for (Node x : intersect(w.getNeighbors(), v.getOut())) {
 					this.add(new OpenTriangle(w, v, x), 5);
 				}
 				// (6)
 				for (Node x : v.getNeighbors()) {
 					if (x.equals(w)) {
 						continue;
 					}
 					this.addPotential(new OpenTriangle(v, w, x), 6);
 					this.addPotential(new OpenTriangle(v, x, w), 6);
 				}
 				// (7)
 				for (Node x : w.getNeighbors()) {
 					if (x.equals(v)) {
 						continue;
 					}
 					this.addPotential(new OpenTriangle(w, v, x), 7);
 					this.addPotential(new OpenTriangle(w, x, v), 7);
 				}
 			}
 			return true;
 		} catch (InvalidOpenTriangleException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 	}
 
 	private boolean add(OpenTriangle t, int type) {
 		if (this.triangles.add(t)) {
 			this.triangleCount++;
 			this.nodeTriangleCount[t.getOrigin().getIndex()]++;
 			return true;
 		}
 		return false;
 	}
 
 	private boolean remove(OpenTriangle t, int type) {
 		if (this.triangles.remove(t)) {
 			this.triangleCount--;
 			this.nodeTriangleCount[t.getOrigin().getIndex()]--;
 			return true;
 		}
 		return false;
 	}
 
 	private boolean addPotential(OpenTriangle t, int type) {
 		if (this.potentials.add(t)) {
 			this.potentialCount++;
 			this.nodePotentialCount[t.getOrigin().getIndex()]++;
 			return true;
 		}
 		return false;
 	}
 
 	private boolean removePotential(OpenTriangle t, int type) {
 		if (this.potentials.remove(t)) {
 			this.potentialCount--;
 			this.nodePotentialCount[t.getOrigin().getIndex()]--;
 			return true;
 		}
 		return false;
 	}
 
 	private Set<Node> intersect(Set<Node> s1, Set<Node> s2) {
 		Set<Node> s = new HashSet<Node>();
 		s.addAll(s1);
 		s.retainAll(s2);
 		return s;
 	}
 
 	@Override
 	protected boolean applyAfterEdgeAddition_(Diff d, Edge e)
 			throws DiffNotApplicableException {
 		throw new DiffNotApplicableException(this.getName()
 				+ " - cannot be applied after edge addition");
 	}
 
 	@Override
 	protected boolean applyAfterEdgeRemoval_(Diff d, Edge e)
 			throws DiffNotApplicableException {
 		throw new DiffNotApplicableException(this.getName()
 				+ " - cannot be applied after edge deletion");
 	}
 
 	@Override
 	public void reset_() {
 		super.reset_();
 		this.triangles = null;
 		this.potentials = null;
 		this.nodeTriangles = null;
 		this.nodePotentials = null;
 	}
 }

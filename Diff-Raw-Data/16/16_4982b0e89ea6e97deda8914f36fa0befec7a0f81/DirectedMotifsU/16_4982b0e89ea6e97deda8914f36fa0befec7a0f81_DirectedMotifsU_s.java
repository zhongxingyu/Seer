 package dna.metrics.motifs.directedMotifs;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import dna.graph.IElement;
 import dna.graph.edges.DirectedEdge;
 import dna.graph.nodes.DirectedNode;
 import dna.metrics.motifs.directedMotifs.DirectedMotif.DirectedMotifType;
 import dna.metrics.motifs.directedMotifs.exceptions.DirectedMotifInvalidEdgeAdditionException;
 import dna.metrics.motifs.directedMotifs.exceptions.DirectedMotifInvalidEdgeRemovalException;
 import dna.metrics.motifs.directedMotifs.exceptions.DirectedMotifSplittingException;
 import dna.updates.batch.Batch;
 import dna.updates.update.EdgeAddition;
 import dna.updates.update.EdgeRemoval;
 import dna.updates.update.NodeRemoval;
 import dna.updates.update.Update;
 import dna.util.Log;
 
 public class DirectedMotifsU extends DirectedMotifs {
 
 	private HashSet<Integer> allMotifs;
 
 	private HashSet<DirectedMotif> allRealMotifs;
 
 	private HashMap<Integer, HashSet<DirectedMotif>> nodeMotifs;
 
 	private void addMotif(DirectedMotif m) {
 		Log.debug("adding " + m + " (" + m.hashCode() + ")\n" + m.asString());
 		this.allMotifs.add(m.hashCode());
 		this.allRealMotifs.add(m);
 		this.nodeMotifs.get(m.getA().getIndex()).add(m);
 		this.nodeMotifs.get(m.getB().getIndex()).add(m);
 		this.nodeMotifs.get(m.getC().getIndex()).add(m);
 		this.motifs.incr(m.getIndex());
		this.motifs.incrDenominator();
 	}
 
 	private void removeMotif(DirectedMotif m) {
 		Log.debug("removing " + m + " (" + m.hashCode() + ")\n" + m.asString());
 		this.allMotifs.remove(m.hashCode());
 		this.allRealMotifs.remove(m);
 		this.nodeMotifs.get(m.getA().getIndex()).remove(m);
 		this.nodeMotifs.get(m.getB().getIndex()).remove(m);
 		this.nodeMotifs.get(m.getC().getIndex()).remove(m);
 		this.motifs.decr(m.getIndex());
		this.motifs.decrDenominator();
 	}
 
 	private HashSet<DirectedMotif> getNodeMotifs(int index) {
 		if (this.nodeMotifs.containsKey(index)) {
 			return this.nodeMotifs.get(index);
 		}
 		HashSet<DirectedMotif> s = new HashSet<DirectedMotif>();
 		this.nodeMotifs.put(index, s);
 		return s;
 	}
 
 	public DirectedMotifsU() {
		super("DirectedMotifsU", ApplicationType.BeforeUpdate, MetricType.exact);
 	}
 
 	@Override
 	public boolean applyBeforeBatch(Batch b) {
 		return false;
 	}
 
 	@Override
 	public boolean applyAfterBatch(Batch b) {
 		return false;
 	}
 
 	@Override
 	public boolean applyAfterUpdate(Update u) {
 		return false;
 	}
 
 	private void addToMotifs(Iterable<DirectedMotif> motifs, DirectedEdge e,
 			DirectedNode exclude) {
 		for (DirectedMotif m : motifs) {
 			if (exclude != null && m.contains(exclude)) {
 				continue;
 			}
 			String s = m.asString();
 			this.motifs.decr(m.getIndex());
 			try {
 				m.addEdge(e);
 				Log.debug("changing motif: " + m + "\n" + m.asStringFrom(s));
 			} catch (DirectedMotifInvalidEdgeAdditionException e1) {
 				// e1.printStackTrace();
 			}
 			this.motifs.incr(m.getIndex());
 		}
 	}
 
 	private void removeFromMotifs(Iterable<DirectedMotif> motifs,
 			DirectedEdge e, DirectedNode exclude) {
 		List<DirectedMotif> removedMotifs = new LinkedList<DirectedMotif>();
 		for (DirectedMotif m : motifs) {
 			if (exclude != null && m.contains(exclude)) {
 				continue;
 			}
 			if (!m.contains(e.getSrc()) || !m.contains(e.getDst())) {
 				continue;
 			}
 			String s = m.asString();
 			this.motifs.decr(m.getIndex());
 			try {
 				m.removeEdge(e);
 				Log.debug("changing motif: " + m + "\n" + m.asStringFrom(s));
 			} catch (DirectedMotifInvalidEdgeRemovalException e1) {
 				// e1.printStackTrace();
 			} catch (DirectedMotifSplittingException e1) {
 				// e1.printStackTrace();
 				removedMotifs.add(m);
 			}
 			this.motifs.incr(m.getIndex());
 		}
 		for (DirectedMotif m : removedMotifs) {
 			this.removeMotif(m);
 		}
 	}
 
 	private void addNewMotifs(DirectedEdge e) {
 		HashMap<Integer, DirectedMotif> newMotifs = new HashMap<Integer, DirectedMotif>();
 		this.addNewMotif5(e.getSrc(), e.getDst(), newMotifs);
 		this.addNewMotif6(e.getSrc(), e.getDst(), newMotifs);
 		this.addNewMotif1(e.getSrc(), e.getDst(), newMotifs);
 		this.addNewMotif2(e.getSrc(), e.getDst(), newMotifs);
 		this.addNewMotif3a(e.getSrc(), e.getDst(), newMotifs);
 		this.addNewMotif3b(e.getSrc(), e.getDst(), newMotifs);
 		for (DirectedMotif m : newMotifs.values()) {
 			this.addMotif(m);
 		}
 	}
 
 	private void addNewMotif5(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = N(a) => a-c-b
 		for (IElement node : a.getNeighbors()) {
 			DirectedNode c = (DirectedNode) node;
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(a, c, b);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(a, c, b,
 					DirectedMotifType.DM05));
 		}
 	}
 
 	private void addNewMotif6(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = N(b) => b-c-a
 		for (IElement node : b.getNeighbors()) {
 			DirectedNode c = (DirectedNode) node;
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(b, c, a);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(b, c, a,
 					DirectedMotifType.DM06));
 		}
 	}
 
 	private void addNewMotif1(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = out(a) \ in(a) => a-b-c
 		for (IElement edge : a.getOutgoingEdges()) {
 			DirectedNode c = (DirectedNode) ((DirectedEdge) edge).getDst();
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(a, b, c);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(a, b, c,
 					DirectedMotifType.DM01));
 		}
 	}
 
 	private void addNewMotif2(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = in(b) \ out(b) => b-a-c
 		for (IElement edge : b.getIncomingEdges()) {
 			DirectedNode c = (DirectedNode) ((DirectedEdge) edge).getSrc();
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(b, a, c);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(b, a, c,
 					DirectedMotifType.DM02));
 		}
 	}
 
 	private void addNewMotif3a(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = in(a) \ out(a) => a-c-b
 		for (IElement edge : a.getIncomingEdges()) {
 			DirectedNode c = (DirectedNode) ((DirectedEdge) edge).getSrc();
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(a, c, b);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(a, c, b,
 					DirectedMotifType.DM03));
 		}
 	}
 
 	private void addNewMotif3b(DirectedNode a, DirectedNode b,
 			HashMap<Integer, DirectedMotif> newMotifs) {
 		// c = out(b) \ in(b) => b-a-c
 		for (IElement edge : b.getOutgoingEdges()) {
 			DirectedNode c = (DirectedNode) ((DirectedEdge) edge).getDst();
 			if (c.equals(a) || c.equals(b)) {
 				continue;
 			}
 			int hash = DirectedMotif.getHashCode(b, a, c);
 			if (this.allMotifs.contains(hash) || newMotifs.containsKey(hash)) {
 				continue;
 			}
 			newMotifs.put(hash, new DirectedMotif(b, a, c,
 					DirectedMotifType.DM03));
 		}
 	}
 
 	@Override
 	public boolean applyBeforeUpdate(Update u) {
 		if (u instanceof EdgeAddition) {
 			DirectedEdge e = (DirectedEdge) ((EdgeAddition) u).getEdge();
 
 			Log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
 			Log.debug(">>> add edge " + e);
 			Log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
 
 			this.addToMotifs(this.getNodeMotifs(e.getSrc().getIndex()), e, null);
 			this.addToMotifs(this.getNodeMotifs(e.getDst().getIndex()), e,
 					e.getSrc());
 			this.addNewMotifs(e);
 
 			Log.debug(this.allRealMotifs.toString());
 
 		} else if (u instanceof EdgeRemoval) {
 			DirectedEdge e = (DirectedEdge) ((EdgeRemoval) u).getEdge();
 
 			Log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
 			Log.debug(">>> remove edge " + e);
 			Log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
 
 			this.removeFromMotifs(this.getNodeMotifs(e.getSrc().getIndex()), e,
 					null);
 			this.removeFromMotifs(this.getNodeMotifs(e.getDst().getIndex()), e,
 					e.getSrc());
 
 			Log.debug(this.allRealMotifs.toString());
 
 		} else if (u instanceof NodeRemoval) {
 			// TODO implement node removal
 			return true;
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean compute() {
 		this.motifs.setDenominator(0);
 		return true;
 	}
 
 	public void init_() {
 		super.init_();
 		this.allMotifs = new HashSet<Integer>();
 		this.allRealMotifs = new HashSet<DirectedMotif>();
 		this.nodeMotifs = new HashMap<Integer, HashSet<DirectedMotif>>();
 	}
 
 }

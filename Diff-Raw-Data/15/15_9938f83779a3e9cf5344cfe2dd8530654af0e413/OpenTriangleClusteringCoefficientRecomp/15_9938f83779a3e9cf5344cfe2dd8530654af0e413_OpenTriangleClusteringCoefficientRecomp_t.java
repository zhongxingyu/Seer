 package dna.metrics.clusterCoefficient;
 
 import dna.graph.directed.DirectedEdge;
 import dna.graph.directed.DirectedGraph;
 import dna.graph.directed.DirectedNode;
 import dna.updates.Batch;
 import dna.updates.Update;
 import dna.util.ArrayUtils;
 
 @SuppressWarnings("rawtypes")
 public class OpenTriangleClusteringCoefficientRecomp extends
 		OpenTriangleClusteringCoefficient {
 
 	public OpenTriangleClusteringCoefficientRecomp() {
 		super("openTriangleClusteringCoefficientRecomp",
 				ApplicationType.Recomputation);
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
 	public boolean applyBeforeUpdate(Update u) {
 		return false;
 	}
 
 	@Override
 	public boolean applyAfterUpdate(Update u) {
 		return false;
 	}
 
 	@Override
 	public boolean compute() {
 		this.triangleCount = 0;
 		this.potentialCount = 0;
 		DirectedGraph g = (DirectedGraph) this.g;
 		for (DirectedNode n : g.getNodes()) {
 			this.nodeTriangleCount[n.getIndex()] = 0;
 			this.nodePotentialCount[n.getIndex()] = 0;
 			for (DirectedNode u : n.getNeighbors()) {
 				for (DirectedNode v : n.getNeighbors()) {
 					if (u.equals(v)) {
 						continue;
 					}
 					this.nodePotentialCount[n.getIndex()]++;
 					if (u.hasEdge(new DirectedEdge(u, v))) {
 						this.nodeTriangleCount[n.getIndex()]++;
 					}
 				}
 			}
 			this.triangleCount += this.nodeTriangleCount[n.getIndex()];
 			this.potentialCount += this.nodePotentialCount[n.getIndex()];
 			if (this.nodePotentialCount[n.getIndex()] == 0) {
 				this.localCC[n.getIndex()] = 0;
 			} else {
 				this.localCC[n.getIndex()] = (double) this.nodeTriangleCount[n
 						.getIndex()]
 						/ (double) this.nodePotentialCount[n.getIndex()];
 			}
 		}
 
 		if (this.potentialCount == 0) {
 			this.globalCC = 0;
 		} else {
 			this.globalCC = (double) this.triangleCount
 					/ (double) this.potentialCount;
 		}
		this.averageCC = ArrayUtils.avgIgnoreNaN(this.localCC);
 
 		return true;
 	}
 
 }

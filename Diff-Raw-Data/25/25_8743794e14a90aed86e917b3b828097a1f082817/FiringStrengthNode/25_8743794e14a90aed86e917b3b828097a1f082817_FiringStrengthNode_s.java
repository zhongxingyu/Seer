 package nodes;
 
 import java.util.Iterator;
 
 // layer 2
 
 public class FiringStrengthNode extends Node{
 	public NormFSNode primarySuccessorNode;
 
 	public FiringStrengthNode() {
 		super();
 	}
 	
 	public void setPrimarySuccessorNode(NormFSNode primarySuccessorNode) {
 		this.primarySuccessorNode = primarySuccessorNode;
 	}
 	
 	// firing strength function for FiringStrengthNode
 		private double firingStrengthFunction(double value1, double value2) {
 			//return Math.min(value1, value2);
 			return value1 * value2;
 		}
 	
 	public double computeFiringStrength() {
 		return computeFiringStrength(null);
 	}
 		
 	public double computeFiringStrength(Node exclude) {
 		Iterator<Node> predNodes = predecessorNodes.iterator();
		Node next;
		double result = predNodes.next().getOutput();
 		while(predNodes.hasNext()) {
 			next = predNodes.next();
 			if(next.equals(exclude)) {
 				continue;
 			}
 			result = firingStrengthFunction(result, next.getOutput());
 		}
 		
 		return result;
 	}
 
 	@Override
 	public void accept(NodeVisitor visitor) {
 		visitor.visit(this);
 	}
 }

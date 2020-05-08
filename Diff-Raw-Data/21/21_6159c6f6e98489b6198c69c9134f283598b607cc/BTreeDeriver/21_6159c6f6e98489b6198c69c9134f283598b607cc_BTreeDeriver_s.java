 package mission.two;
 
 import java.util.ArrayList;
 
 public class BTreeDeriver<E> {

 	
 	/*
 	 * @Julien
 	 * 
 	 * 
 	 */
 	public void derivationAdd(BTNode bTnode, ArrayList<BTNode> bTree){
 		BTNode lChild = (BTNode) bTnode.getLeft();
 		BTNode rChild = (BTNode) bTnode.getRight();
 		
 		bTnode.setElement((E) "+");
 		bTnode.setLeft(deriveExpression(lChild));
		bTnode.setRight(deriveExpression(rChild);
 	}
 	
 	
 	/*
 	 * @Julien
 	 * 
 	 */
 	public void derivationSub(BTNode bTnode, ArrayList<BTNode> bTree){
 		BTNode lChild = (BTNode) bTnode.getLeft();
 		BTNode rChild = (BTNode) bTnode.getRight();
 		
 		bTnode.setElement((E) "-");
 		bTnode.setLeft(deriveExpression(lChild));
 		bTnode.setRight(deriveExpression(rChild));
 	}
 	
 	
 	/*
 	 * @Julien
 	 * 
 	 */
 	public void derivationMul(BTNode bTnode, ArrayList<BTNode> bTree){
 		BTNode lChild = (BTNode) bTnode.getLeft();
 		BTNode rChild = (BTNode) bTnode.getRight();
 		
		BTNode mul1 = new BTNode("*", deriveExpression(lChild), lChild, bTnode.element()); //f'*g
		BTNode mul2 = new BTNode("*", lChild, deriveExpression(rChild), bTnode.element()); // f*g'
 		
 		bTnode.setElement((E) "+");
 		bTnode.setLeft(mul1);
 		bTnode.setRight(mul2);
 	}
 	
 	
 	/*
 	 * @Julien
 	 * 
 	 */
 	public void derivationDiv(BTNode<E> bTnode, ArrayList<BTNode> bTree){
 		BTNode lChild = (BTNode) bTnode.getLeft();
 		BTNode rChild = (BTNode) bTnode.getRight();
 		
 		BTNode sub1 = new BTNode(null ,null ,null ,null);
 		
 		BTNode mul1 = new BTNode("*", deriveExpression(lChild), lChild, sub1); // f'*g
 		BTNode mul2 = new BTNode("*", lChild, deriveExpression(rChild), sub1); // f*g'
 
		BTNode exp1 = new BTNode("^", lChild, deriveExpression(rChild), bTnode.element()); //g^2
 		
 		sub1.setElement("-");
 		sub1.setLeft(mul1);
 		sub1.setRight(mul2);
 		sub1.setParent(bTnode);
 		
 		bTnode.setElement((E) "-");
 		bTnode.setLeft(sub1);
 		bTnode.setRight(exp1);
	}		
 }
 
 

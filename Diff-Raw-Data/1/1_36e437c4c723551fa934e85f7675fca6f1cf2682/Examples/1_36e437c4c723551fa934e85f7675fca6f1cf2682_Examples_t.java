 import tester.*;
 
 public class Examples {
 	
 	public boolean isAVL(IBST tree) {
 		if (tree.height() == 0) {return true;}
 		else {
			DataBST dbst = (DataBST)tree;
 			boolean currBalanced = Math.abs(dbst.leftChild.height() - dbst.rightChild.height()) <= 1;
 			return currBalanced &&
 					isAVL(dbst) &&
 					isAVL(dbst);
 		}
 	}
 	
 	IQueue Q = new Queue();
 	IStack S = new Stack();
 	IPriorityQueue P = new PriorityQueue();
 	
 	AVLTree A = new EmptyAVLTree();
 	AVLTree B = new EmptyAVLTree();
 	
 	ISet set = new EmptyAVLTree();
 	
 	// testing basic element containing
 	boolean testAVL(Tester t){
 		return t.checkExpect(A.addElem(4).addElem(6).addElem(3).addElem(5).hasElem(4),
 				true);
 	}
 	
 	// testing auto-balance feature
 	boolean testAVL2(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(((AVLTree)A.addElem(5).addElem(6).addElem(7)).height(),
 				2);
 	}
 	
 	// testing combination of add and remove
 	boolean testAVL3(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(A.addElem(8).addElem(14).addElem(4).addElem(3)
 				.addElem(7).addElem(9).addElem(15).remElem(14).remElem(8).hasElem(3), 
 				true);
 	}
 	
 	// testing extremely unbalanced tree
 	boolean testAVL4(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(((AVLTree)A.addElem(1).addElem(2).addElem(3).addElem(4).addElem(5))
 				.height(),  3);
 	}
 	
 	boolean testAVL5(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(((AVLTree)A.addElem(5).addElem(6).addElem(7)).isBalanced(), true);
 	}
 	
 	boolean testAVL6(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(((AVLTree)A.addElem(5).addElem(4).addElem(3)).largestElem(), 5);
 	}
 
 	boolean testAVL7(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(((AVLTree)A.addElem(5).addElem(4).addElem(3)).smallestElem(), 3);
 	}
 
 	// Two cases depending on which side is made root (depending on a random object)
 	boolean testAVL8(Tester t){
 		A = new EmptyAVLTree();
 		B = new EmptyAVLTree();
 		return t.checkOneOf(((AVLTree)A.addElem(5).addElem(4).addElem(3)).remElem(4),
 				(AVLTree)B.addElem(5).addElem(3),
 				(AVLTree)B.addElem(3).addElem(5)
 		);
 	}
 
 	boolean testAVL9(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkOneOf(((AVLTree)A.addElem(5).addElem(4).addElem(7).addElem(6)
 				.addElem(9).addElem(10)).isBalanced(), true);
 	}
 	
 	boolean testAVL10(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkOneOf(((AVLTree)A.addElem(5).addElem(3).addElem(6).addElem(2)
 				.addElem(4).addElem(1)).isBalanced(), true);
 	}
 	
 	boolean testAVL11(Tester t){
 		A = new EmptyAVLTree();
 		return t.checkExpect(isAVL((AVLTree)A.addElem(1).addElem(2).addElem(3)
 				.addElem(4).addElem(5)), true);
 	}
 	
 	// --------------------------------------------------------------------------------------------
 	
 	boolean testQueue(Tester t) {
 		return t.checkExpect(new Queue().enqueue(3).dequeue(),
 				new Queue());
 	}
 	
 	boolean testQueue2(Tester t) {
 		return t.checkExpect(new Queue().enqueue(7).enqueue(4).enqueue(5).dequeue(),
 				new Queue().enqueue(4).enqueue(5));
 	}
 	
 	boolean testQueue3(Tester t) {
 		return t.checkExpect(new Queue().enqueue(4).enqueue(5).front(),
 				4);
 	}
 	
 	boolean testQueue4(Tester t) {
 		return t.checkExpect(new Queue().enqueue(4).enqueue(5).dequeue(),
 				new Queue().enqueue(5));
 	}
 
 // ------------------------------------------------------------------------------------------------
 	
 	boolean testStack(Tester t) {
 		return t.checkExpect(new Stack().push(45).pop(),
 				new Stack());
 	}
 	
 	boolean testStack2(Tester t) {
 		return t.checkExpect(new Stack().push(45).push(768).pop(),
 				new Stack().push(45));
 	}
 
 	boolean testStack3(Tester t) {
 		return t.checkExpect(new Stack().push(7).push(4).push(5).pop(),
 				new Stack().push(7).push(4));
 	}
 
 	boolean testStack4(Tester t) {
 		return t.checkExpect(new Stack().push(7).push(4).push(5).top(), 5);
 	}
 
 	boolean testStack5(Tester t) {
 		return t.checkExpect(new Stack().push(7).push(4).push(5).pop().top(), 4);
 	}
 	
 // ------------------------------------------------------------------------------------------------
 	
 	boolean testPQ(Tester t) {
 		return t.checkExpect(new PriorityQueue().addElt(5).remMinElt(), 
 				new PriorityQueue());
 	}
 	
 	boolean testPQ2(Tester t) {
 		return t.checkExpect(new PriorityQueue().addElt(5).addElt(3).remMinElt(),
 				new PriorityQueue().addElt(5));
 	}
 	
 	boolean testPQ3(Tester t) {
 		return t.checkExpect(new PriorityQueue().addElt(5).addElt(3).getMinElt(), 3);
 	}
 	
 	boolean testPQ4(Tester t) {
 		return t.checkExpect(new PriorityQueue().addElt(7).addElt(4).addElt(5).remMinElt(),
 				new PriorityQueue().addElt(7).addElt(5));
 	}
 
 	boolean testPQ5(Tester t) {
 		return t.checkExpect(new PriorityQueue().addElt(7).addElt(4).addElt(5).remMinElt()
 				.getMinElt(),  5);
 	}
 }
 
 /*
 class Examples {
   Examples(){}
   
   IBST b1 = new DataBST (5, new MtBST(), new MtBST());
   IBST b2 = b1.addElem(3).addElem(4).addElem(8).addElem(7);
   IBST b2no4 = b1.addElem(3).addElem(8).addElem(7);
   IBST b2no8 = b1.addElem(3).addElem(4).addElem(7);
   IBST b2rem5 = new DataBST(4, new MtBST(), new MtBST()).addElem(3).addElem(8).addElem(7);
 
   // does size work as expected?
   boolean test1 (Tester t) {
     return t.checkExpect(b2.size(), 5);
   }
   
   // do size and addElem interact properly on a new element?
   boolean test2 (Tester t) {
     return t.checkExpect(b1.addElem(7).size(), 2);
   }
     
   // do size and addElem interact properly on a duplicate element?
   boolean test3 (Tester t) {
     return t.checkExpect(b1.addElem(5).size(), 1);
   }
   
   // check removal in left subtree
   boolean test4 (Tester t) {
     return t.checkExpect(b2.remElem(4), b2no4);
   }
   
   // check removal in right subtree
   boolean test5 (Tester t) {
     return t.checkExpect(b2.remElem(8), b2no8);
   }
 
   // check removal of root
   boolean test6 (Tester t) {
     return t.checkExpect(b2.remElem(5), b2rem5);
   }
 
 }
  */ 

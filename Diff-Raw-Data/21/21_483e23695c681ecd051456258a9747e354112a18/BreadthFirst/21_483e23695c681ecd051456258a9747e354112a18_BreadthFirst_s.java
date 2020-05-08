 package jjtraveler;
 
 /**
 * <code>BF(v) = Seq(v,All(EnQ),Try(Seq(DeQ,BF(v)))</code>
  * <p>
 * Visit a tree in breadth-first order.
  */
 
 import java.util.LinkedList;
 import java.util.Collection;
 
 public class BreadthFirst {
 
     public BreadthFirst(Visitor v) {
 	pending = new LinkedList();
 	this.v = v;
     }
 
     public BreadthFirst(Visitor v, Collection c) {
 	pending = new LinkedList(c);
 	this.v = v;
     }
 
     LinkedList pending;
     Visitor v;
 
     public Visitable visit(Visitable x)
       throws VisitFailure {
 	Visitable result = v.visit(x);
 	int childCount = result.getChildCount();
 
         for (int i = 0; i < childCount; i++) {
            pending.addLast(x.getChildAt(i));
         }
 
 	if (pending.size() != 0) {
	    result = (Visitable) pending.removeFirst();
	    result = this.visit(result);
 	}
 
         return result;
     }
 }
 	

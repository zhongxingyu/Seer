 package jjtraveler;
 
 /**
 * <code>All(v).visit(T(t1,...,tN) = T(t1.visit(v), ..., tN.visit(v))</code>
  * <p>
  * Basic visitor combinator with one visitor argument, that applies
  * this visitor to all children.
  */
 
 public class All implements Visitor {
 
 	public Visitor v;
 
 	public All(Visitor v) {
 		this.v = v;
 	}
 
 	public Visitable visit(Visitable any) throws VisitFailure {
 		int childCount = any.getChildCount();
 		Visitable result = any;
 		for (int i = 0; i < childCount; i++) {
 			result.setChildAt(i, v.visit(result.getChildAt(i)));
 		}
 		return result;
 	}
 
 	// Factory method
 	public All make(Visitor v) {
 		return new All(v);
 	}
 	protected void setArgumentTo(Visitor v) {
 		this.v = v;
 	}
 }

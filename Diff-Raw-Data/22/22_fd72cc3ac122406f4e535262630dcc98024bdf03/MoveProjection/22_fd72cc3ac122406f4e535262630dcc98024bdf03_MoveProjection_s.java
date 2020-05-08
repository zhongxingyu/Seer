 package optimization;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import relationenalgebra.CrossProduct;
 import relationenalgebra.IOneChildNode;
 import relationenalgebra.ITreeNode;
 import relationenalgebra.ITwoChildNode;
 import relationenalgebra.Join;
 import relationenalgebra.Projection;
 import relationenalgebra.Selection;
 
 public class MoveProjection implements IOptimization {
 
     @Override
     public ITreeNode optimize(ITreeNode plan) {
         if (!(plan instanceof Projection)) {
             if (plan instanceof ITwoChildNode) {
                 ((ITwoChildNode) plan).setSecondChild(
                         optimize(((ITwoChildNode) plan).getSecondChild()));
             }
             if (plan instanceof IOneChildNode) {
                 ((IOneChildNode) plan).setChild(
                         optimize(((IOneChildNode) plan).getChild()));
             }
             return plan;
         }
         Projection p = (Projection)plan;
         p.setChild(optimize(p.getChild()));
 
         return moveProjection(p);
     }
 
     private static ITreeNode moveProjection(Projection p) {
         ITreeNode child = p.getChild();
         if (child instanceof Selection) {
         	
         	
         	Selection s = (Selection)child;
         	
             Set<String> selectionAttributes = s.getExpr().getAttributes();
             Set<String> projectionAttributes = p.getAttributes();
             
             if (selectionAttributes.containsAll(projectionAttributes)) {
             	p.setChild(s.getChild());
             	s.setChild(moveProjection(p));
             }
            
        	return s;
         } else if (child instanceof CrossProduct) {
         	// handle cross product or join
         	
         	/* model:
         	 * 					  		 /-> firstChild
         	 * rootNode -> twoChildNode -|
         	 * 					  		 \-> secondChild
         	 */
         	ITwoChildNode twoChildNode = (ITwoChildNode)child;
         	ITreeNode firstChild = twoChildNode.getChild();
         	ITreeNode secondChild = twoChildNode.getSecondChild();
         	IOneChildNode rootNode = twoChildNode;
         	
         	// attributes from the (input) projection
         	Set<String> L = new HashSet<String>(p.getColumnnames());
 
         	// attributes from the children
         	Set<String> firstChildAttributes = firstChild.getAttributes();
         	Set<String> projectedFirstChildAttributes = intersect(firstChildAttributes, L);
         	Set<String> secondChildAttributes = secondChild.getAttributes();
         	Set<String> projectedSecondChildAttributes = intersect(secondChildAttributes, L);
         	
         	// does the join expression contain extra attributes?
         	if (twoChildNode instanceof Join) {
         		Set<String> c = ((Join) twoChildNode).getExpr().getAttributes();
         		if (!L.containsAll(c)) {        			
         			// retain the current projection
         			rootNode = p;
         			
         			// extend children attributes
         			projectedFirstChildAttributes.addAll(intersect(firstChildAttributes, c));
         			projectedSecondChildAttributes.addAll(intersect(secondChildAttributes, c));
         		}
         	}
         	
         	// create children projections
         	if (projectedFirstChildAttributes.size() > 0) {
 	        	Projection firstChildProjection = new Projection(new ArrayList<String>(projectedFirstChildAttributes));
 	        	firstChildProjection.setChild(twoChildNode.getChild());
 	        	firstChild = moveProjection(firstChildProjection);
 	        	twoChildNode.setChild(firstChild);
         	}
         	
         	if (projectedSecondChildAttributes.size() > 0) {
         		Projection secondChildProjection = new Projection(new ArrayList<String>(projectedSecondChildAttributes));
         		secondChildProjection.setChild(twoChildNode.getSecondChild());
             	// move projections
             	secondChild = moveProjection(secondChildProjection);
             	
             	// update relations and return result
             	twoChildNode.setSecondChild(secondChild);
             }
         	
         	
         	return rootNode;
         } else {
         	return p; // cannot move
         }
     }
 
     /**
      * Intersects two sets
      * 
      * @param setA
      * @param setB
      * @return intersection of setA and setB
      */
 	private static <T> Set<T> intersect(Set<T> setA, Set<T> setB) {
 		Set<T> result = new HashSet<T>();
 		if (setA.size() > setB.size()) {
 			Set<T> temp = setB;
 			setB = setA;
 			setA = temp;
 		}
 		for (T s : setA) {
 			if (setB.contains(s)) {
 				result.add(s);
 			}
 		}
 		return result;
 	}
 
 }

 package org.ilrt.wf.facets;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import java.util.Collections;
 import java.util.LinkedList;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * Warning! This interface is likely to change as Damian and Mike pull their respective hair out!
  *
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  * @author Damian Steer (d.steer.@bristol.ac.uk)
  */
 public interface FacetQueryService {
 
 
     /**
      * Extract a full tree hierarchy. The result may include labels on the resources.
      * @param base Where to start building the tree.
      * @param prop The property relating nodes in the hierarchy
      * @param isBroader If true traverse prop object to subject, otherwise subject to object
      * @return
      */
     Tree<Resource> getHierarchy(Resource base, Property prop, boolean isBroader);
 
     /**
      * We want to find all the possible refinements for a facet state. For example, in a SKOS
      * hierarchy that represents countries, if the the current facet state represented the
      * United Kingdom, we would expect refinements for England, Northern Ireland, Scotland and
      * Wales.
      *
      * @param base Where to start building the tree.
      * @param prop The property relating nodes in the hierarchy
      * @param isBroader If true traverse prop object to subject, otherwise subject to object
      * @return                      the possible further refinements to a facet,
      */
     List<Resource> getRefinements(Resource base, Property prop, boolean isBroader);
 
     /**
      * Return the number of matches for future facet states - for example, there my be
      * 8 conferences being held in Bristol. By separating the count from the refinement
      * method call, it does allow the option to configure applications not to
      * produce counts since they can be an expensive operation.
      *
      * @param currentFacetStates    the current state of all facets
      * @return                      the future states and their associated count.
      */
     Map<FacetState, Integer> getCounts(List<? extends FacetState> currentFacetStates);
 
     /**
      * Get the number of resources which fall under the given states.
      *
      * @param currentFacentStates
      * @return The total number of resources which match the current state.
      */
     int getCount(List<? extends FacetState> currentFacentStates);
 
     /**
      * Get the resources that match the current states.
      *
      * @param currentFacentStates
      * @param offset Start at this point in whole result list
      * @param number Get at most this many results
      * @return
      */
     List<Resource> getResults(List<? extends FacetState> currentFacetStates, int offset, int number);
 
     /**
      * A simple tree implemenation for returning the hierarchy.
      * @param <T>
      */
     public static class Tree<T> {
         private final List<Tree<T>> children = new LinkedList<Tree<T>>();
         private final T node;
 
         /**
          * Make a tree node with given value.
          * @param node The value of this node.
          */
         public Tree(T node) {
             if (node == null) throw new IllegalArgumentException("Node cannot be null");
             this.node = node;
         }
 
         public T getValue() { return node; }
 
         /**
          * Get the children (if any) of this tree node.
          * @return An unmodifiable list of children.
          */
         public List<Tree<T>> getChildren() { return Collections.unmodifiableList(children); }
 
         /**
          * Add a child descending from this node.
          * @param child
          * @return This node (for chaining)
          */
         public Tree addChild(Tree child) {
             if (children.isEmpty()) { children.add(child); return this; }
             // insert at correct position.
             // Imperfect: hash doesn't look down tree very far
             int index = 0;
             while ((index < children.size()) &&
                     (children.get(index).hashCode() < child.hashCode())) index++;
             children.add(index, child);
             return this;
         }
 
         /**
          * Is this the end of a tree?
          * @return true if the are no descendants of this node.
          */
         public boolean isLeaf() { return children.isEmpty(); }
 
         @Override
         public int hashCode() {
             return node.hashCode() | children.size();
         }
 
         /**
          * This is flawed, so be careful! For our use cases it is ok
          *
          * @param other
          * @return true if other has same contents in same structure
          */
         @Override
         public boolean equals(Object other) {
             // Is it a tree?
             if (!(other instanceof Tree)) return false;
             Tree ot = (Tree) other;
             // Is the node value the same?
             if (!node.equals(ot.getValue())) return false;
             List<Tree> ochildren = ot.getChildren();
             // Same number of children?
             if (children.size() != ochildren.size()) return false;
             // Compare each child pairwise
             // Due to hash ordering this isn't perfect, however we won't touch the
             // problem cases (multiple children with same value)
             for (int i = 0; i < children.size(); i++) {
                 if (!children.get(i).equals(ochildren.get(i))) return false;
             }
             return true;
         }
 
         @Override
         public String toString() {
            return toString(0);
        }

        public String toString(int indent) {
            StringBuilder is = new StringBuilder(indent * 2);
            for (int i = 0; i < indent; i++) is.append("  ");
             StringBuilder sb = new StringBuilder();
            sb.append(is).append("|-").append(node).append("\n");
            for (Tree<T> x: children) sb.append(x.toString(indent + 1));
             return sb.toString();
         }
     }
 }

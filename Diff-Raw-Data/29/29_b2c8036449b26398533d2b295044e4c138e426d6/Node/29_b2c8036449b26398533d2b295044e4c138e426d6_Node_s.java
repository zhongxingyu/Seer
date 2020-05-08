 package student;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A node in the abstract syntax tree of a program.
  */
 public abstract class Node<SubNodeType extends Node<?>> implements Cloneable {
 
     private static final String mutationDescriptions[][] = new String [][] {
         new String[]{"The node ", " was removed. If its parent node needed a replacement node, "
             + "one of its randomly selected children of the right kind was used."},
         new String[]{"The order of two children of the node "," was switched."},
         new String[]{"The node "," and its children were replaced with a copy of another "
             + "randomly selected node of the right kind, found somewhere in the rule set. The entire "
             + "AST subtree rooted at the selected node was copied."},
         new String[]{"The node ","was replaced with a randomly chosen node of the same"
             + " kind but its children remained the same."},
         new String[]{"A newly created node was inserted as the parent of the node ",", taking"
             + " its place in the tree. If the newly created node needed more than one child, the children "
             + "that are not the original node were copies of randomly chosen nodes of the right kind from "
             + "the whole rule set." }};
     protected Node<Node<SubNodeType>> parent;
     protected List<SubNodeType> children;
     private int mutationType = 0;
 
     /**
      * Creates a new Node with the given nodes as children.
      * @param childNodes the new Node's children.
      */
     protected Node(List<SubNodeType> childNodes) {
         children = childNodes;
     }
 
     /**
      * Creates a new Node with the given nodes as children.
      * @param subs the new Node's children.
      */
     protected Node(SubNodeType... subs) {
         this(Arrays.asList(subs));
     }
     
     /**
      * Creates a new Node to accept a variable number of children.
      */
     protected Node() {
         this(new LinkedList<SubNodeType>());
     }
 
     /**
      * Randomizes the Node without changing its children
      *
      * @return true if the operation is supported for this type of Node, false
      * otherwise
      */
     public boolean randomize() {
         return false;
     }
 
     /**
      * The number of nodes in this AST, including the current node. This can be
      * helpful for implementing mutate() correctly.
      */
     public int size() {
         int siz = 1;
         for (SubNodeType n : children) {
             siz += n.size();
         }
         return siz;
     }
 
     /**
      * Return a version of the same AST with one random mutation in it. May have
      * side effects on the original AST.
      * @param p the topmost node
      * @return a version of the same AST with one random mutation in it from the topmost node
      */
     public final Node<?> mutate(Program p) {
         Node mutated = FaultInjector.injectFault(this, p);
         return mutated;
     }
 
     /**
      * Stores the type of mutation that was last applied
      *
      * @param i the type of mutation
      */
     public void setMutationType(int i) {
         mutationType = i;
     }
     
     /**
      * Returns a description of the last mutation applied.
      */
     String mutationDescription(){
         if(mutationType!=0)
             return (mutationDescriptions[mutationType][0] + "Node["+toString()+"]"+ mutationDescriptions[mutationType][1]);
         else
             return "";
     }
     /**
      * Appends the program represented by this node prettily to the given
      * StringBuffer.
      *
      * @param sb The StringBuffer to be appended
      */
     public void prettyPrint(StringBuffer sb) {
         toString(sb);
     }
     
     /**
      * Places a string representation of the Node into the buffer.
      * @param sb The buffer to accept the Node's string representation
      * @return sb
      */
     protected abstract StringBuffer toString(StringBuffer sb);
     
     /**
      * Returns a lexically-equivalent string to this node. 
      * 
      * It is guaranteed that this string will parse if inserted into a
      * correct context, but the whitespace, delimiters, &c. are not guaranteed 
      * to be in any way optimal.
      */
     @Override
     public final String toString() {
         return toString(new StringBuffer()).toString();
     }
 
     /**
      * Checks if the node has a parent
      *
      * @return whether the node has a parent
      */
     public boolean hasParent() {
         return parent != null;
     }
 
     /**
      * Checks if the node has children
      *
      * @return whether the node has children
      */
     public boolean hasChildren() {
         return !children.isEmpty();
     }
 
     /**
      * Sets the node's parent to the given node
      *
      * @param n the given node
      */
     public final void setParent(Node<Node<SubNodeType>> n) {
         parent = n;
     }
 
     /**
      * Retrieves the node's parent
      *
      * @return the node's parent
      */
     public Node<?> getParent() {
         return parent;
     }
 
     /**
      * Creates a copy of the node and its subtree
      *
      * @return copy of node and its subtree
      */
     public Node<?> copy() {
         try {
             return (Node<?>) this.clone();
         } catch (CloneNotSupportedException ex) {
             throw new Error("Yes, we *do* support .clone()");
         }
     }
 
     /**
      * Flattens the subtree starting from the current node
      *
      * @return the subtree starting from the current node as a Node array
      */
     public Node<?>[] toArray() {
        Node<?>[] arr = (Node[]) buildList(new LinkedList<Node<?>>()).toArray();
         return arr;
     }
 
     /**
      * Recursive helper method for toArray, creates a list of Nodes of the
      * subtree starting from the current node
      */
     protected LinkedList<Node<?>> buildList(LinkedList<Node<?>> list) {
         list.add(this);
         return list;
     }
 
     /**
      * Sets the current node to the given node(including its subtree)
      *
      * @param n
      */
     public void set(Node n) {
         int pos = parent.children.indexOf(this);
         parent.children.set(pos, n);
     }
 
     /**
      * Retrieves the number of children the node has
      *
      * @return number of children the node has
      */
     public int numChildren() {
         return children.size();
     }
 
     /**
      * Swaps two children of the Node, if there are more than two, the two
      * children are randomly selected Precondition: Node has at least 2 children
      */
     public boolean swapChildren() {
         int nc = numChildren();
         if (nc < 2) {
             return false;
         }
         int c1, c2;
         do {
             c1 = (int) (Math.random() * nc);
             c2 = (int) (Math.random() * nc);
         } while (c1 == c2);
         SubNodeType temp = children.get(c1);
         children.set(c1, children.get(c2));
         children.set(c2, temp);
         return true;
     }
 
     /**
      * Deletes a random child of this node
      * @return Whether the deletion was successful.
      */
     public boolean deleteChild() {
         return deleteChild(children.get((int) (numChildren() * Math.random())));
     }
 
     /**
      * Delete the given child node Precondition: n must be a child of the node
      *
      * @param n the given child node
      */
     public boolean deleteChild(SubNodeType n) {
         return children.remove(n);
     }
     
     /**
      * Retrieves a random child node
      * @return a random child of the node
      */
     public Node<?> randomChild()
     {
     	return children.get((int)(Math.random()*children.size()));
     }
     /**
      * Replaces one child with another
      * @param old the old child to replace
      * @param neww the new child to replace with
      */
     public void replaceChild(SubNodeType old, SubNodeType neww)
     {
         children.set(children.indexOf(old), neww);
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         Node n = (Node)super.clone();
         List<SubNodeType> nc = new ArrayList<SubNodeType>(children.size());
         for(SubNodeType sn : children)
             nc.add((SubNodeType)sn.clone());
         n.children = nc;
         return n;
     }
     
 }

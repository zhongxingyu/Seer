 /*
  * DvRlib - Container
  * Duncan van Roermund, 2010-2012
  * WeightedTree.java
  */
 
 package dvrlib.container;
 
 import dvrlib.generic.Pair;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 public class WeightedTree<E> implements Iterable<E> {
    protected WeightedTreeNode<E> root = null;
 
    /**
     * Returns the number of elements in this tree.
     * O(1).
     */
    public int size() {
       return (root == null ? 0 : root.size);
    }
    /**
     * Returns true if this tree holds no elements, false otherwise.
     * O(1).
     */
    public boolean isEmpty() {
       return root == null;
    }
    /**
     * Removes all elements from this tree.
     * O(1).
     */
    public void clear() {
       root = null;
    }
 
    /**
     * Adds a node with the given key and value to this tree.
     * @return A boolean indicating whether the operation succeeded.
     * @throws IllegalArgumentException When the given key is <code>null</code>.
     * @see WeightedTree#rebalanceUp(WeightedTreeNode)
     * O(height)
     */
    public boolean add(double key, E value) {
       if(root == null)
          root = new WeightedTreeNode<E>(null, key, value);
       else {
          WeightedTreeNode<E> node = root;
          while(true) {
             if(key < node.key) {
                if(node.left == null) {
                   node.left = new WeightedTreeNode<E>(node, key, value);
                   break;
                }
                else
                   node = node.left;
             }
             else if(key > node.key) {
                if(node.right == null) {
                   node.right = new WeightedTreeNode<E>(node, key, value);
                   break;
                }
                else
                   node = node.right;
             }
             else { // key == node.key
                node.add(value);
                break;
             }
          }
          rebalanceUp(node);
       }
       return true;
    }
 
    /**
     * Returns whether this tree contains a node with the given key and value.
     * @return true if this tree contains the given key/value combination, false otherwise.
     * O(height)
     */
    public boolean contains(double key, E value) {
       WeightedTreeNode<E> node = root;
       while(node != null) {
          if(key < node.key)
             node = node.left;
          else if(key > node.key)
             node = node.right;
          else // key == node.key
             break;
       }
       return (node == null ? false : node.contains(value));
    }
 
    /**
     * Returns a (key, element) from the tree, reached with the given normalized index.
     * For example, in a tree with these four items: {(1, A), (2, B), (1.3, C), (1.7, D)}, the sum of the indices is 6.
     * A call to getWeighted(0.5) will then return the item with the summed index (0.5 * 6 = 3).
     * Moving through the tree from the smallest index, (A: 1 &lt; 3), (C: 1.3 &lt; (3 - 1)), (D: 1.7 &gt;= (3 - 1 - 1.3)).
     * This means the item with the largest index has the most chance of getting selected, hence the weight.
     * @param normIndex A double between 0 and 1.
     */
    public Pair<Double, E> getWeighted(double normIndex) {
       if(normIndex < 0 || normIndex > 1)
          throw new IllegalArgumentException("The argument of WeightedTree.getWeighted(double) should be between 0 and 1");
       return (root == null ? null : root.getWeighted(normIndex));
    }
 
    /**
     * Returns the node with the smallest key in this tree.
     * O(height).
     */
    protected WeightedTreeNode<E> getMin() {
       return (root == null ? null : root.getMin());
    }
    /**
     * Returns the node with the largest key in this tree.
     * O(height).
     */
    protected WeightedTreeNode<E> getMax() {
       return (root == null ? null : root.getMax());
    }
 
    /**
     * Returns an element from the node with the smallest key along with that key.
     * @see WeightedTree#getMin()
     * @see WeightedTreeNode#peek()
     */
    public Pair<Double, E> peekMin() {
       WeightedTreeNode<E> node = getMin();
       return new Pair<Double, E>(node.key, node.peek());
    }
    /**
     * Returns an element from the node with the largest key along with that key.
     * @see WeightedTree#getMax()
     * @see WeightedTreeNode#peek()
     */
    public Pair<Double, E> peekMax() {
       WeightedTreeNode<E> node = getMax();
       return new Pair<Double, E>(node.key, node.peek());
    }
 
    /**
     * Removes an element from the given node and returns its key along with its data.
     * If the node contains only one element, it is removed from the tree.
     * @see WeightedTree#remove(WeightedTreeNode)
     * O(node.depth).
     */
    protected Pair<Double, E> pop(WeightedTreeNode<E> node) {
       Pair<Double, E> item = new Pair<Double, E>(node.key, node.pop());
       if(node.values.isEmpty())
          remove(node);
       else
          updateSizeUp(node);
       return item;
    }
    /**
     * Removes an element from the node with the smallest key and returns its key and its data.
     * @see WeightedTree#getMin()
     * @see WeightedTree#pop(WeightedTreeNode)
     */
    public Pair<Double, E> popMin() {
       return pop(getMin());
    }
    /**
     * Removes an element from the node with the largest key and returns its key along with its data.
     * @see WeightedTree#getMax()
     * @see WeightedTree#pop(WeightedTreeNode)
     */
    public Pair<Double, E> popMax() {
       return pop(getMax());
    }
 
    /**
     * Removes the given key and value from this tree.
     * @return A boolean indicating whether the value was actually removed.
     */
    public boolean remove(double key, E value) {
       WeightedTreeNode<E> node = root;
       while(node != null) {
          if(key < node.key)
             node = node.left;
          else if(key > node.key)
             node = node.right;
          else { // key == node.key
             if(node.remove(value)) {
               if(node.values.isEmpty())
                  remove(node);
               else
                  rebalanceUp(node);
                return true;
             }
             return false;
          }
       }
       return false;
    }
 
    /**
     * Removes the given node from this tree.
     * O(node.height + node.depth).
     */
    protected void remove(WeightedTreeNode<E> node) {
       if(node.left == null || node.right == null)
          removeExternal(node);
       else {
          throw new IllegalArgumentException("WeightedTree.remove(WeightedTreeNode) is only implemented for external nodes");
          /*WeightedTreeNode<E> n;
          int dH = node.getLeftHeight() - node.getRightHeight(),
              dS = node.getLeftSize() - node.getRightSize();
          if(dH > 0 || (dH == 0 && dS > 0)) {
             if(root == node) {
                root = node.left;
                root.parent = null;
             }
             else
                node.parent.replace(node, node.left);
 
             n = getMax(node.left);
             n.right = node.right;
             rebalanceUp(n);
          }
          else {
             if(root == node) {
                root = node.right;
                root.parent = null;
             }
             else
                node.parent.replace(node, node.right);
 
             n = getMax(node.right);
             n.left = node.left;
             rebalanceUp(n);
          }*/
       }
    }
    /**
     * Removes the given external node from this tree.
     * Do not call this method directly but use <code>remove(WeightedTreeNode)</code>.
     * @param node The node that is to be removed. This has to be an external node, i.e. it may not have both a left and a right subtree.
     * @see WeightedTree#remove(WeightedTreeNode)
     * O(node.depth).
     */
   private void removeExternal(WeightedTreeNode<E> node) {
       if(node == null)
          throw new IllegalArgumentException("Cannot remove null node");
      if(node.left != null && node.right != null)
         throw new IllegalArgumentException("Can only remove external nodes");
       else if(node == root) {
          root = (node.left != null ? node.left : node.right);
          if(root != null)
             root.parent = null;
       }
       else {
          node.parent.replaceChild(node, (node.left != null ? node.left : node.right));
          rebalanceUp(node.parent);
          updateSizeUp(node.parent);
          node.parent = null;
       }
    }
 
    /**
     * Updates the size, weight and height of the given node and all its ancestors.
     * @see WeightedTreeNode#updateSize()
     * O(node.depth).
     */
    protected void updateSizeUp(WeightedTreeNode<E> node) {
       for(; node != null; node = node.parent) {
          node.updateSize();
       }
    }
 
    /**
     * Ensures the given node and all its ancestors are balanced.
     * @see WeightedTree#rebalance(WeightedTreeNode)
     * O(node.depth).
     */
    protected void rebalanceUp(WeightedTreeNode<E> node) {
       for(; node != null; node = node.parent) {
          node.updateSize();
          node = rebalance(node);
       }
    }
    /**
     * Rebalances the given node by performing rotations if necessary.
     * @return The node at the original position of the given node after the rebalancing.
     * O(1).
     */
    protected WeightedTreeNode<E> rebalance(WeightedTreeNode<E> node) {
       if(node != null) {
          int balance = node.getLeftHeight() - node.getRightHeight();
          if(balance > 1) {
             balance = node.left.getLeftHeight() - node.left.getRightHeight();
             if(balance > 0)
                node = singleRotation(node, node.left, node.left.left, false);
             else
                node = doubleRotation(node, node.left, node.left.right, false);
          }
          else if(balance < -1) {
             balance = node.right.getLeftHeight() - node.right.getRightHeight();
             if(balance > 0)
                node = doubleRotation(node, node.right, node.right.left, true);
             else
                node = singleRotation(node, node.right, node.right.right, true);
          }
          else
             node.updateSize();
       }
       return node;
    }
 
    /**
     * Performs a single rotation with the three given nodes:
     * z                             z
     *  \              y            /            y
     *   y   becomes  / \   and    y   becomes  / \
     *    \          z   x        /            x   z
     *     x                     x
     * @param ZLeft   Indicates whether the z node ends up as the left child of the node
     * @return        The new top node: y
     * O(1).
     */
    protected WeightedTreeNode<E> singleRotation(WeightedTreeNode<E> z, WeightedTreeNode<E> y, WeightedTreeNode<E> x, boolean ZLeft) {
       /*if(z == null || y == null)
          throw new IllegalArgumentException("Cannot rotate on null nodes");
       if(z != null && z.parent == null && z != root)
          throw new IllegalStateException("Node without parent other than the root encountered");*/
 
       // Maintain root and parents
       if(z == root) {
          root = y;
          root.parent = null;
       }
       else
          z.parent.replaceChild(z, y);
       z.parent = y;
 
       // Set children
       if(ZLeft) {
          z.replaceChild(z.right, y.left);
          y.left  = z;
       }
       else {
          z.replaceChild(z.left, y.right);
          y.right = z;
       }
 
       // Maintain metadata
       z.updateSize();
       y.updateSize();
 
       return y;
    }
    /**
     * Performs a double rotation with the three given nodes:
     * z                             z
     *  \                           /
     *   \             x           /             x
     *    \  becomes  / \   and   /    becomes  / \
     *     y         z   y       y             y   z
     *    /                       \
     *   x                         x
     *
     * @param ZLeft   Indicates whether the z node ends up as the left child of the top node
     * @return        The new top node: x
     * O(1).
     */
    protected WeightedTreeNode<E> doubleRotation(WeightedTreeNode<E> z, WeightedTreeNode<E> y, WeightedTreeNode<E> x, boolean ZLeft) {
       assert (z != null && y != null) : "Cannot rotate on null nodes";
       assert (z == root || z.parent != null) : "Node without parent other than the root encountered";
 
       // Maintain root and parents
       if(z == root) {
          root = x;
          root.parent = null;
       }
       else
          z.parent.replaceChild(z, x);
       y.parent = x;
       z.parent = x;
 
       // Set children
       if(ZLeft) {
          z.replaceChild(z.right, x.left);
          y.replaceChild(y.left, x.right);
          x.left  = z;
          x.right = y;
       }
       else {
          z.replaceChild(z.left, x.right);
          y.replaceChild(y.right, x.left);
          x.left  = y;
          x.right = z;
       }
 
       // Maintain metadata
       z.updateSize();
       y.updateSize();
       x.updateSize();
 
       return x;
    }
 
    /**
     * Returns an iterator to the elements in this tree, starting from the element with the largest weight.
     * @see WeightedTreeIterator(WeightedTree)
     */
    @Override
    public Iterator<E> iterator() {
       return new WeightedTreeIterator<E>(this);
    }
 
    /**
     * Returns the elements in this tree as an array.
     */
    public Object[] toArray() {
       Object array[] = new Object[size()];
       int i = 0;
       for(E e : this) {
          array[i++] = e;
       }
       return array;
    }
    /**
     * Copies the elements in this tree to the given array.
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
       if(a.length < size())
          a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
       int i = 0;
       for(E e : this) {
          a[i++] = (T) e;
       }
       if(i < a.length - 1)
          a[i] = null;
       return a;
    }
 
    /**
     * Prints this tree to stdout.
     * @see print(java.io.PrintStream)
     */
    public void print() {
       print(System.out);
    }
 
    /**
     * Prints this tree to the given stream.
     * O(n).
     */
    public void print(java.io.PrintStream out) {
       out.println("dvrlib.container.WeightedTree(" + size() + ")");
       if(root != null)
          root.print(out, "");
    }
 }

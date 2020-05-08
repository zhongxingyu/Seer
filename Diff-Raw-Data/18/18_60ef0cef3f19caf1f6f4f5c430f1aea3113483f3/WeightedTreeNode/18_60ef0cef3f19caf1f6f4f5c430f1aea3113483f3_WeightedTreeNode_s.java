 /*
  * DvRlib - Container
  * Duncan van Roermund, 2010-2012
  * WeightedTreeNode.java
  */
 
 package dvrlib.container;
 
 import dvrlib.generic.Pair;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class WeightedTreeNode<E> {
    protected WeightedTreeNode<E> parent = null, left = null, right = null;
    protected List<E> values = new ArrayList<E>();
    protected double key, weight;
    protected int size = 1, height = 0;
 
    public WeightedTreeNode(WeightedTreeNode<E> parent, double key, E value) {
       this.parent = parent;
       this.key = key;
       values.add(value);
       weight = key;
    }
 
    /**
     * Returns whether this node contains the given value.
     */
    public boolean contains(E value) {
       return values.contains(value);
    }
 
    public int getHeight() {
       return height;
    }
 
    public int getLeftHeight() {
       return (left == null ? -1 : left.height);
    }
 
    public int getRightHeight() {
       return (right == null ? -1 : right.height);
    }
 
    public int getSize() {
       return size;
    }
 
    public int getLeftSize() {
       return (left == null ? 0 : left.size);
    }
 
    public int getRightSize() {
       return (right == null ? 0 : right.size);
    }
 
    public double getWeight() {
       return weight;
    }
 
    public double getLeftWeight() {
       return (left == null ? 0 : left.weight);
    }
 
    public double getRightWeight() {
       return (right == null ? 0 : right.weight);
    }
 
    /**
     * Returns the element that was added last.
     * O(1).
     */
    public E peek() {
       return values.get(values.size() - 1);
    }
 
    /**
     * Removes the element that was added last.
     * O(1).
     */
    public E pop() {
       return values.remove(values.size() - 1);
    }
 
    /**
     * Adds the given value to this node.
     * @return A boolean indicating whether the value was actually added.
     */
    protected boolean add(E value) {
      return (values.contains(value) ? false : values.add(value));
    }
 
    /**
     * Returns the node with the smallest key in the subtree of this node.
     * O(height).
     */
    protected WeightedTreeNode<E> getMin() {
       return (left == null ? this : left.getMin());
    }
 
    /**
     * Returns the node with the largest key in the subtree of this node.
     * O(height).
     */
    protected WeightedTreeNode<E> getMax() {
       return (right == null ? this : right.getMax());
    }
 
    /**
     * @see WeightedTree#getWeighted(double)
     */
    protected Pair<Double, E> getWeighted(double normIndex) {
       assert (normIndex >= 0d && normIndex < 1d) : "normIndex should be between 0 (inclusive) and 1 (exclusive), got " + normIndex;
 
       double l = getLeftWeight();
       if(l > 0d) {
          l = l / weight;
          if(normIndex < l)
             return left.getWeighted(normIndex / l);
       }
       double r = getRightWeight();
       if(r > 0d) {
          r = (weight - r) / weight;
          if(normIndex > r)
             return right.getWeighted((normIndex - r) / (1d - r));
       }
       else
          r = 1d;
 
       return new Pair<Double, E>(key, values.get((int)(values.size() * (normIndex - l) / (r - l))));
    }
 
    /**
     * Removes the given value from this node.
     * @return A boolean indicating whether the value was actually removed.
     */
    protected boolean remove(E value) {
       return values.remove(value);
    }
 
    /**
     * Replaces the old subtree by the new one.
     * @param old The subtree that will be replaced, either <code>old == left</code> or <code>old == right</code> should hold.
     * O(1).
     */
    protected void replaceChild(WeightedTreeNode<E> old, WeightedTreeNode<E> that) {
       if(old == left)
          left = that;
       else if(old == right)
          right = that;
       else
          throw new IllegalArgumentException("WeightedTreeNode.replaceChild(WeightedTreeNode, WeightedTreeNode) expects the first argument to be either of its subnodes");
 
       if(that != null)
          that.parent = this;
    }
 
    /**
     * Updates the size, height and weight of this node.
     * O(1).
     */
    protected void updateSize() {
       size   = getLeftSize() + values.size() + getRightSize();
       height = 1 + Math.max(getLeftHeight(), getRightHeight());
       weight = getLeftWeight() + key * values.size() + getRightWeight();
    }
 
    @Override
    public String toString() {
       return "TreeNode(" + key + "," + values + ")[" + height + "|" + size + "|" + weight + "]{" + left + "," + right + "}";
    }
 
    /**
     * Prints this node and its children to the given stream.
     * O(height).
     */
    public void print(java.io.PrintStream out, String prefix) {
       if(left != null)
          left.print(out, prefix + "\t");
       else
          out.println(prefix + "\tnull");
       out.println(prefix + "(" + key + "," + values + ")[" + height + "|" + size + "|" + weight + "]");
       if(right != null)
          right.print(out, prefix + "\t");
       else
          out.println(prefix + "\tnull");
    }
 }

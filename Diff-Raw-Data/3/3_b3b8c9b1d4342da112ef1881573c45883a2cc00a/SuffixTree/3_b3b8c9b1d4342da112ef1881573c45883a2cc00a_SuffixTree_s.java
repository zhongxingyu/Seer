 package suffixtree;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 public class SuffixTree {
    private InternalNode   root;
    private List<String>   strings;
    private List<LeafNode> leaves;
 
    public class RepeatEntry {
       private SuffixTree    tree;
       private int           stringIndex;
       private List<Integer> starts;
       private int           length;
 
       public RepeatEntry(SuffixTree tree, int stringIndex,
             List<Integer> starts, int length) {
          this.tree = tree;
          this.stringIndex = stringIndex;
          this.starts = starts;
          this.length = length;
       }
 
       public List<Integer> getStarts() {
          return starts;
       }
 
       public int getLength() {
          return length;
       }
 
       public String toString() {
         return tree.strings.get(stringIndex).substring(starts.get(0), length);
       }
    }
 
    protected SuffixTree(List<String> strings) {
       this.root = new InternalNode();
       this.leaves = new LinkedList<LeafNode>();
       this.strings = new ArrayList<String>(strings.size());
       for (String s : strings) {
          this.strings.add(s + "$");
       }
    }
 
    /**
     * Creates a suffix tree from the given string.
     * 
     * @return
     */
    public static SuffixTree create(String string) {
       List<String> strings = new ArrayList<String>(1);
       strings.add(string);
       return create(strings);
    }
 
    // TOOD Make SuffixTree generic then this can be public
    protected static SuffixTree create(List<String> strings) {
       SuffixTree tree = new SuffixTree(strings);
       tree.fill();
       return tree;
    }
 
    protected void fill() {
       for (String string : strings) {
          for (int i = 0; i < string.length(); ++i) {
             LeafNode leaf = new LeafNode(string, i, string.length());
             root.insertNode(leaf);
             leaves.add(leaf);
          }
       }
 
       for (LeafNode leaf : leaves) {
          InternalNode node = leaf.parent;
          // Add all leaves to internal nodes
          while (node != null) {
             node.addLeaf(leaf);
             node = node.parent;
          }
       }
    }
 
    public List<LeafNode> getLeaves() {
       return leaves;
    }
 
    public List<Integer> getOccurrences(String string) {
       int nodeCharIndex = 0;
 
       Node node = root.getChild(string.charAt(0));
       if (node == null)
          return new ArrayList<Integer>(0);
 
       for (int i = 0; i < string.length(); ++i, ++nodeCharIndex) {
          if (nodeCharIndex >= node.length()) {
             if (node instanceof InternalNode) {
                node = ((InternalNode) node).getChild(string.charAt(i));
             } else {
                node = null;
             }
             if (node == null) {
                return new ArrayList<Integer>(0);
             }
 
             nodeCharIndex = 0;
          }
 
          if (string.charAt(i) != node.charAt(nodeCharIndex))
             return new ArrayList<Integer>(0);
       }
 
       List<Integer> retval = null;
 
       if (node instanceof LeafNode) {
          // This is a leaf so there is only one occurrence.
          retval = new ArrayList<Integer>(1);
          retval.add(((LeafNode) node).getStart());
       } else if (node instanceof InternalNode) {
          // This is an internal node so there are multiple occurrences.
          List<LeafNode> leaves = ((InternalNode) node).getLeaves();
          retval = new ArrayList<Integer>(leaves.size());
          for (LeafNode leaf : leaves) {
             retval.add(leaf.getStart());
          }
       } else {
          // This shouldn't happen
          throw new RuntimeException();
       }
 
       return retval;
    }
 
    public List<RepeatEntry> findRepeats(int length) {
       List<Node> leftDiverseNodes = root.getLeftDiverseNodes();
       List<RepeatEntry> repeats = new ArrayList<RepeatEntry>(
             leftDiverseNodes.size());
       for (Node node : leftDiverseNodes) {
          if (node.toString().length() >= length) {
             Collection<LeafNode> leaves = ((InternalNode) node).getLeaves();
             List<Integer> starts = new ArrayList<Integer>(leaves.size());
             for (LeafNode leaf : leaves) {
                starts.add(leaf.start);
             }
             repeats.add(new RepeatEntry(this, 0, starts, node.toString()
                   .length()));
          }
       }
       return repeats;
    }
 
    public String debugString() {
       return root.debugString();
    }
 }

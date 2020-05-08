 package octree;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class OctNode {
     // Static class contants for value passing
     static final int OCTNODE_INTERMEDIATE = 0;
     static final int OCTNODE_EMPTY = 1;
     static final int OCTNODE_LEAF = 2;
     
     // Private data members
     private Integer depth = 0;
     private int type = OCTNODE_EMPTY;
     private Boolean sign = false;
     private Boolean[] signs = new Boolean[8];
     private ArrayList<OctNode> children = new ArrayList<OctNode>();
     private Float coords[], origin[], length, potential = 0.0f;
 
     // OctNode constructors
     public OctNode(Integer depth, OctNode children[], Float[] origin, Float length) throws OctNodeException {
         this.depth = depth;
         this.type = OCTNODE_INTERMEDIATE;
         if (children.length != 8) throw new InvalidOctNodeCreationParameterException();
         this.children.addAll(Arrays.asList(children));
         this.length = length;
         this.origin = Arrays.copyOf(origin, origin.length);
     }
 
     public OctNode(Integer depth, Boolean sign, Float[] origin, Float length) {
         this.depth = depth;
         this.type = OCTNODE_EMPTY;
         this.sign = sign;
         this.length = length;
         this.origin = Arrays.copyOf(origin, origin.length);
     }
 
     public OctNode(Integer depth, Boolean signs[], Float coords[], Float[] origin, Float length) throws OctNodeException {
         this.depth = depth;
         this.type = OCTNODE_LEAF;
         this.signs = Arrays.copyOf(signs, signs.length);
         if (coords.length != 3) throw new InvalidOctNodeCreationParameterException();
         this.coords = Arrays.copyOf(coords, coords.length);
         this.length = length;
         this.origin = Arrays.copyOf(origin, origin.length);
     }
     
     public OctNode(Integer depth, byte signs, Float coords[], Float[] origin, Float length) throws OctNodeException {
         this.depth = depth;
         this.type = OCTNODE_LEAF;
         byte temp = signs;
         for (int i = 0; i < 8; i++) {
             this.signs[i] = (temp % 2 == 0)?true:false;
             temp /= 2;
         }
         if (coords.length != 3) throw new InvalidOctNodeCreationParameterException();
         this.coords = Arrays.copyOf(coords, coords.length);
         this.length = length;
         this.origin = Arrays.copyOf(origin, origin.length);
     }
 
     // Getters
     public Boolean getSign() throws OctNodeException {
         switch (type) {
             case OCTNODE_EMPTY:
                 return this.sign;
             case OCTNODE_INTERMEDIATE:
                 throw new InvalidOctNodeTypeException();
             case OCTNODE_LEAF:
                 int i = 0;
                 for (Boolean b: this.signs)
                     if (b == true)
                         if (++i > 4)
                             return true;
                 return false;
             default:
                 throw new UnrecognizedOctNodeTypeException();
         }
     }
 
     public Boolean getSignForElementWithID(int id) throws OctNodeException {
         if (this.type != OCTNODE_LEAF) throw new InvalidOctNodeTypeException();
         if (id < 0 || id > 7) throw new InvalidElementIDOctNodeException();
         return this.signs[id];
     }
     
     public ArrayList<OctNode> getChildren() throws OctNodeException {
         if (this.type != OCTNODE_INTERMEDIATE) throw new InvalidOctNodeTypeException();
         return this.children;
     }
     
     public OctNode getChildWithID(int id) throws OctNodeException {
         if (this.type != OCTNODE_INTERMEDIATE) throw new InvalidOctNodeTypeException();
         if (id < 0 || id > 7) throw new InvalidElementIDOctNodeException();
         return this.children.get(id);
     }
     
     public Float[] getCoordinates() throws OctNodeException {
         if (this.type != OCTNODE_LEAF) throw new InvalidOctNodeTypeException();
         return Arrays.copyOf(this.coords, this.coords.length);
     }
     
     public Float[] getOrigin() { return Arrays.copyOf(this.origin, this.origin.length); }
     
     public Float[] getOriginAntipode() {
        Float[] result = Arrays.copyOf(this.coords, this.coords.length);
        for (Float f: result)
            f += this.length;
         return result;
     }
 
     public Float getLength() { return this.length; }
     public Integer getNodeDepth() { return this.depth; }
     
     public Integer getMaxDepth() throws OctNodeException {
         switch (type) {
             case OCTNODE_INTERMEDIATE:
                 Integer result = this.depth;
                 for (OctNode node: this.children) {
                     Integer temp = node.getMaxDepth();
                     if (temp > result) result = temp;
                 }
                 return result;
             case OCTNODE_LEAF:
             case OCTNODE_EMPTY:
                 return this.depth;
             default:
                 throw new UnrecognizedOctNodeTypeException();
         }
     }
 
     public ArrayList<Float[]> getAllVertices() throws OctNodeException {
         ArrayList<Float[]> result = new ArrayList<Float[]>();
         switch (type) {
             case OCTNODE_INTERMEDIATE:
                 for (OctNode node: this.children)
                     result.addAll(node.getAllVertices());
                 return result;
             case OCTNODE_LEAF:
                 Float[] temp = Arrays.copyOf(this.coords, this.coords.length);
                 result.add(temp);
                 return result;
             case OCTNODE_EMPTY:
                 return result;
             default:
                 throw new UnrecognizedOctNodeTypeException();
         }
         
     }
 }

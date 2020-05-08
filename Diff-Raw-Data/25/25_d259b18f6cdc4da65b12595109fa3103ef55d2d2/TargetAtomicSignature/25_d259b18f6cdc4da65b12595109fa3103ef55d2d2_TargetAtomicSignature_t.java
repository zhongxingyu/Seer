 package org.openscience.cdk.signature;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IBond;
 import org.openscience.cdk.interfaces.IChemObjectBuilder;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
 
 /**
  * A target atomic signature records the structural context that an atom must 
  * have - that is, its neighbours, and neighbours of neighbours - in the final
  * structure. It is more abstract than just the AtomicSignature, which is 
  * derived from a molecule.
  * 
  *  
  * @author maclean
  *
  */
 public class TargetAtomicSignature implements Comparable<TargetAtomicSignature> {
     
     private class Node {
         
         public String symbol;
         
         public String edgeSymbol;
         
         public int label;
         
         public Node parent;
         
         public ArrayList<Node> children;
         
         public boolean visited;
         
         public Node(String symbol, Node parent) {
             this(symbol, "", -1, parent);
         }
         
         public Node(String symbol, String edgeSymbol, Node parent) {
             this(symbol, edgeSymbol, -1, parent);
         }
         
         public Node(String symbol, String edgeSymbol, int label, Node parent) {
             this.symbol = symbol;
             this.edgeSymbol = edgeSymbol;
             this.label = label;
             this.parent = parent;
             this.children = new ArrayList<Node>();
             this.visited = false;
         }
         
         private void toMolecule(IChemObjectBuilder builder,
                                 IMolecule mol,
                                 IAtom parentAtom,
                                 Map<Integer, Integer> labelAtomNumberMap) {
             if (this.visited) return;
             
             // add a new atom if necessary, or use an existing one if labelled
             IAtom atom;
             if (this.isLabelled()) {
                 if (labelAtomNumberMap.containsKey(label)) {
                     int number = labelAtomNumberMap.get(label);
                     atom = mol.getAtom(number);
                 } else {
                     atom = builder.newAtom(this.symbol);
                     mol.addAtom(atom);
                     int atomIndex = mol.getAtomCount() - 1;
                     labelAtomNumberMap.put(label, atomIndex);
                 }
                 
             } else {
               // XXX - what if the symbol is not an atom symbol?
                 atom = builder.newAtom(this.symbol);
                 mol.addAtom(atom);
             }
             
             // now check to see if a bond should be added
             if (parentAtom != null) {
                 IBond.Order order = convertBondSymbolToOrder();
                 IBond bond = builder.newBond(parentAtom, atom, order);
                 
                 if (!mol.contains(bond)) {
                     mol.addBond(bond);
                 }
             }
             for (Node child : this.children) {
                 child.toMolecule(builder, mol, atom, labelAtomNumberMap);
             }
         }
         
         public IBond.Order convertBondSymbolToOrder() {
             if (edgeSymbol.equals("=")) {
                 return IBond.Order.DOUBLE;
             } else if (edgeSymbol.equals("#")) {
                 return IBond.Order.TRIPLE;
             } else {
                 return IBond.Order.SINGLE;
             }
         }
         
         public boolean isLabelled() {
             return this.label != -1;
         }
         
         public void toString(StringBuffer buffer) {
             buffer.append(this.toString());
             if (this.children.size() == 0) return;
             buffer.append("(");
             for (Node child : this.children) {
                 child.toString(buffer);
             }
             buffer.append(")");
         }
         
         public String toString() {
             StringBuffer sb = new StringBuffer();
             sb.append(this.edgeSymbol);
             sb.append('[').append(this.symbol);
             if (this.isLabelled()) {
                 sb.append(',').append(this.label);
             }
             sb.append(']');
             return sb.toString();
         }
     }
     
     private Node root;
     
     private String name;
     
     private int height;
     
     private int count;
     
     public TargetAtomicSignature(String signatureString, String name, int count) {
         this(signatureString);
         this.name = name;
         this.count = count;
     }
     
     public TargetAtomicSignature(String signatureString, String name) {
         this(signatureString, name, 1);
     }
     
     public TargetAtomicSignature(String signatureString, int count) {
         this.root = this.parse(signatureString);
         this.count = count;
     }
     
     public TargetAtomicSignature(String signatureString) {
         this.root = this.parse(signatureString);
         this.count = 1;
     }
     
     public int compareTo(TargetAtomicSignature o) {
         return this.toString().compareTo(o.toString());
     }
     
     public int getCount() {
         return count;
     }
 
     public int getHeight() {
         return this.height;
     }
     
     public String toCanonicalSignatureString() {
         // TODO make a canonical string from the tree - this can be done more
         // easily than for the DAG built from a molecule, as that needs the 
         // tricky up-and-down traversal to take account of both vertices with  
         // multiple parents, and the labelling.
         
 //        return this.toString(); // XXX - not canonical!
         StringBuffer buffer = new StringBuffer();
         root.toString(buffer);
         return buffer.toString();
     }
     
     public IMolecule toMolecule() {
         return this.toMolecule(NoNotificationChemObjectBuilder.getInstance());
     }
 
     public IMolecule toMolecule(IChemObjectBuilder builder) {
         IMolecule molecule = builder.newMolecule();
         this.root.toMolecule(
                 builder, molecule, null, new HashMap<Integer, Integer>());
         return molecule;
     }
 
     public ArrayList<String> getSignatureStringsFromRootChildren(int height) {
         IMolecule molecule = this.toMolecule();
         
         // XXX - this assumes that the molecule is built from the root!
         IAtom rootAtom = molecule.getAtom(0);
         
         MoleculeSignature signature = new MoleculeSignature(molecule);
         ArrayList<String> sigStrings = new ArrayList<String>();
         for (IAtom child : molecule.getConnectedAtomsList(rootAtom)) {
             int i = molecule.getAtomNumber(child);
            String subSignature = signature.signatureStringForVertex(i, height); 
            sigStrings.add(subSignature);
         }
         return sigStrings;
     }
     
     /**
      * Starting from the root of this signature, return a sub-signature of
      * height h.
      * 
      * @param h the height to go out to
      * @return a string representation of the sub-signature
      */
     public String getSubSignature(int h) {
        IMolecule molecule = this.toMolecule();
        MoleculeSignature signature = new MoleculeSignature(molecule);
        return signature.signatureStringForVertex(0, h);
     }
     
     /**
      * Get a signature string starting at a child of the root - in other words
      * the signature string in the subgraph made from the neighbours of the
      * root. This will include the root for signatures of height greater than
      * zero.
      * 
      * @param startNodeIndex the index of the child of the root
      * @param h the height to go out to
      * @return
      */
     public String getSignatureString(int startNodeIndex, int h) {
         return getSignatureString(this.root.children.get(startNodeIndex), h);
     }
     
     private String getSignatureString(Node start, int h) {
         StringBuffer buffer = new StringBuffer();
         traverse(start, 0, h, buffer);
         clearVisited(root);
         return buffer.toString();
     }
 
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         if (this.name != null) {
             buffer.append(" ");
             buffer.append(this.name);
             buffer.append(" ");
         }
         this.root.toString(buffer);
         return buffer.toString();
     }
 
     private void traverse(Node current, int h, int maxH, StringBuffer buffer) {
         if (current.visited) return;
         buffer.append(current.toString());
         current.visited = true;
         if (h <= maxH) {
             boolean visitedChildren = visitedChildren(current);
             if (current.children.size() > 0 && !visitedChildren) {
                 buffer.append("(");
             }
             for (Node child : current.children) {
                 traverse(child, h + 1, maxH, buffer);
             }
             if (current.parent != null) {
                 if (visitedChildren && !current.parent.visited) {
                     buffer.append("(");
                 }
                 traverse(current.parent, h + 1, maxH, buffer);
                 if (visitedChildren && !current.parent.visited) {
                     buffer.append(")");
                 }
             }
             if (current.children.size() > 0 && !visitedChildren) {
                 buffer.append(")");
             }
         }
     }
     
     private boolean visitedChildren(Node node) {
         for (Node child : node.children) {
             if (child.visited) {
                 continue;
             } else {
                 return false;
             }
         }
         return true;
     }
     
     private void clearVisited(Node n) {
         n.visited = false;
         for (Node child : n.children) {
             clearVisited(child);
         }
     }
     
     private Node parse(String s) {
         Node root = null;
         Node parent = null;
         Node current = null;
         
         int symbolStart = 0;
         int labelStart = -1;
         
         int maxHeight = 0;
         int currentHeight = 0;
         
         String edgeSymbol = "";
         
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (c == '(') {
                 parent = current;
                 currentHeight++;
                 if (currentHeight > maxHeight) {
                     maxHeight = currentHeight;
                 }
             } else if (c == ')') {
                 current = parent;
                 parent = current.parent;
                 currentHeight--;
             } else if (c == '[') {
                 symbolStart = i + 1;
             } else if (c == ',') {
                 labelStart = i + 1;
             } else if (c == ']') {
                 String ss;
                 if (labelStart == -1) {
                     ss = s.substring(symbolStart, i);
                 } else {
                     ss = s.substring(symbolStart, labelStart - 1);
                 }
                 if (root == null) {
                     root = new Node(ss, null);
                     parent = root;
                     current = root;
                 } else {
                     if (labelStart != -1) {
                         int label = Integer.parseInt(s.substring(labelStart, i)); 
                         current = new Node(ss, edgeSymbol, label, parent);
                     } else {
                         current = new Node(ss, edgeSymbol, parent);
                     }
                     parent.children.add(current);
                 }
                 labelStart = -1;
                 edgeSymbol = "";
             } else if (c == 'p') {
                 // ignore, for now
             } else if (c == '=') {
                 edgeSymbol = String.valueOf(c);
             } else if (c == '#') {
                 edgeSymbol = String.valueOf(c);
             } 
         }
         
         this.height = maxHeight;
         
         return root;
     }
 }

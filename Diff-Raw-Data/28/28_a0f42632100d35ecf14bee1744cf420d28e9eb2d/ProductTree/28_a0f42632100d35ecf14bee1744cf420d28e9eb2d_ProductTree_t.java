 package de.enwida.web.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.enwida.transport.DataResolution;
 import de.enwida.web.db.model.CalendarRange;
 import de.enwida.web.utils.ProductLeaf;
 import de.enwida.web.utils.ProductNode;
 
 public class ProductTree implements Cloneable {
     
     public static class ProductAttributes {
         public int productId;
         public List<DataResolution> resolutions;
         public CalendarRange timeRange;
 
         public ProductAttributes(int productId, List<DataResolution> resolutions, CalendarRange timeRange) {
             this.productId = productId;
             this.resolutions = resolutions;
             this.timeRange = timeRange;
         }
     }
     
     private ProductNode root;
     private int tso;
     
     public ProductTree() { }
 
     public ProductTree(ProductNode root) {
         this.root = root;
     }
 
     public ProductTree(int tso) {
         this.tso = tso;
         this.root = new ProductNode(0, "root", new ArrayList<ProductNode>());
     }
     
     /**
      * Flattens the tree to a list
      * @return a {@link List} of {@link ProductAttributes} containing the product id and
      *         attributes from the corresponding leaf
      */
     public List<ProductAttributes> flatten() {
         final List<ProductAttributes> result = new ArrayList<ProductAttributes>();
         for (final ProductNode node : root.getChildren()) {
             flatten(node, "", result);
         }
         return result;
     }
     
     private static void flatten(ProductNode node, String productId, List<ProductAttributes> accumulator) {
         if (node instanceof ProductLeaf) {
             final ProductLeaf leaf = (ProductLeaf) node;
             accumulator.add(new ProductAttributes(Integer.parseInt(productId + leaf.getId()), leaf.getResolution(), leaf.getTimeRange()));
         } else if (node.getChildren() != null) {
             for (final ProductNode child : node.getChildren()) {
                 flatten(child, productId + node.getId(), accumulator);
             }
         }
     }
     
     /**
      * Traverses the tree based on a product id
      * @param product The product id (e.g. 211)
      * @return The corresponding {@link ProductLeaf}
      */
     public ProductLeaf getLeaf(int product) {
         return getLeaf(String.valueOf(product), getRoot(), null);
     }
     
     private static ProductLeaf getLeaf(String product, ProductNode node, List<ProductNode> trace) {
         // Leave breadcrumbs
         if (trace != null) {
             trace.add(node);
         }
 
         if (product.isEmpty()) {
             if (node instanceof ProductLeaf ){
                 return (ProductLeaf) node;
             } else  {
                 // We should have arrived at a leaf but failed
                 return null;
             }
         }
        // Find longest prefix match
         ProductNode nextNode = null;
         for (ProductNode child : node.getChildren()) {
        	final String sId = String.valueOf(child.getId());
        	if (product.startsWith(sId)) {
        		if (nextNode == null || sId.length() > String.valueOf(nextNode.getId()).length()) {
        			nextNode = child;
        		}
        	}
         }
         // Check if we found the next node
         if (nextNode == null) {
             return null;
         }
        final String remainingId = product.substring(String.valueOf(nextNode.getId()).length());
         return getLeaf(remainingId, nextNode, trace);
     }
     
     public void cleanTree() {
         // Clone list in order to avoid concurrent modifications
         final List<ProductNode> childrenClone = new ArrayList<ProductNode>(root.getChildren());
         for (ProductNode child : childrenClone) {
             cleanTree(child, root);
         }
     }
     
     private static void cleanTree(ProductNode node, ProductNode parent) {
         if (node instanceof ProductLeaf) {
             // Nothing to clean
             return;
         }
 
         // Clone list in order to avoid concurrent modifications
         final List<ProductNode> childrenClone = new ArrayList<ProductNode>(node.getChildren());
 
         // Recurse
         for (final ProductNode child : childrenClone) {
             cleanTree(child, node);
         }
 
         // Check if node still has children
         if (node.getChildren() == null || node.getChildren().size() == 0) {
             // Remove node if it has no children
             parent.getChildren().remove(node);
         }
     }
     
     public void removeProduct(int productId) {
         final List<ProductNode> trace = new ArrayList<ProductNode>();
         final ProductLeaf leaf = getLeaf(String.valueOf(productId), getRoot(), trace);
         final ProductNode parent = trace.get(trace.size() - 2);
         
         if (parent != null) {
             parent.getChildren().remove(leaf);
         }
         cleanTree();
     }
     
     public ProductTree clone() {
         final ProductTree result = new ProductTree(tso);
         result.root = root.clone();
         return result;
     }
     
     public ProductNode getRoot() {
         return root;
     }
     
     public void addNode(ProductNode node) {
         root.addChild(node);
     }
 
     public int getTso() {
         return tso;
     }
 
     public void setTso(int tso) {
         this.tso = tso;
     }
 
 	public void setRoot(ProductNode root) {
 		this.root = root;
 	}
 
 }

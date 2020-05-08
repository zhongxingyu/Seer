 package tree;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.tree.DefaultTreeModel;
 
 /** Represents a graph in a tree datastructure */
 public class CustomTree {
 
 	private static DefaultTreeModel tree;
 	private static ArrayList<CustomTreeNode> childs;
 
 	public CustomTree() {
 		CustomTreeNode root = new CustomTreeNode("DefaultRoot");
 		tree = new DefaultTreeModel(root);
 		childs = new ArrayList<CustomTreeNode>();
 	}
 
 	public static CustomTreeNode getRoot() {
 		return (CustomTreeNode) tree.getRoot();
 	}
 
 	public static void setRoot(CustomTreeNode root) {
 		tree.setRoot(root);
 	}
 
 	/** Ads a child to specified root or parent node */
 	public static void addChildtoRootX(CustomTreeNode child, CustomTreeNode root) {
 		root.add(child);
 	}
 
 	/**
 	 * gets the childs of the root node in the tree
 	 * 
 	 * @return ArrayList with all childs of the root node in the tree
 	 */
 	public static ArrayList<CustomTreeNode> getChildren() {
 		childs.clear();
 		traverse(tree);
 		return childs;
 	}
 
 	/**
 	 * Übernommen von Internet, macht tree walk
 	 * 
 	 * @param model
 	 *            the tree data structure
 	 */
 	private static void traverse(DefaultTreeModel model) {
 		CustomTreeNode root;
 		if (model != null) {
 			root = (CustomTreeNode) model.getRoot();
 			walk(model, root);
 		} else {
 			System.out.println("Tree is empty.");
 		}
 	}
 
 	/**
 	 * Übernommen von Internet, macht tree walk
 	 * 
 	 * @param model
 	 *            the tree data structure
 	 * @param root
 	 *            the root node in the tree
 	 */
 	private static void walk(DefaultTreeModel model, CustomTreeNode root) {
 		int childCount;
 		childCount = model.getChildCount(root);
 		for (int i = 0; i < childCount; i++) {
 			CustomTreeNode child = (CustomTreeNode) model.getChild(root, i);
 			if (model.isLeaf(child)) {
 				childs.add(child);
 			} else {
 				childs.add(child);
 				walk(model, child);
 			}
 		}
 	}
	
	public String toString(){
 		childs = getChildren();
 		HashMap<Integer, String> bundles = new HashMap<Integer, String>();
		String treeString ="";
 		int key;
		for(CustomTreeNode child : childs){
			if(child.getBundle() != null)
 				key = Integer.parseInt(child.getBundle());
 			else
 				key = 0;
			if(bundles.get(key) != null)
 				bundles.put(key, bundles.get(key) + child.toString());
 			else
 				bundles.put(key, child.toString());
 		}
		for(int i = 1; i < bundles.size(); i++) {
			treeString += bundles.get(i).toString() + " ∧ ";
			}
 		treeString += bundles.get(0);
 
		return "("+treeString +")";
 	}
 
 }

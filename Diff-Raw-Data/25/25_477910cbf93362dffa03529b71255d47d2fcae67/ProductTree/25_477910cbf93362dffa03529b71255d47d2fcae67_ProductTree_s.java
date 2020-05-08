 package pimp.gui.tree;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeExpansionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import pimp.model.Product;
 /**
  * 
  * @author Joel Mason
  *
  */
 
 public class ProductTree extends JTree {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 662247613490338231L;
 	
 	// Product tree.
 	private NodeItem root;
 	private DefaultTreeModel model;
 	private NodeItem lastSelectedNode;
 
     private ActionListener classSelectListener;
     private ActionListener productSelectedListener;
     private ActionListener productEditedListener;
     
 	private HashMap<String, NodeItem> map;
 	
 	public ProductTree() {
 		super();
 		
 		// Create the model.
 		root = new NodeItem(Product.class);
 		model = new DefaultTreeModel(root);
 		map = new HashMap<String, NodeItem>();
 		
 		// Set model and settings.
 		setModel(model);
 		getSelectionModel().setSelectionMode
 			(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		setShowsRootHandles(false);
 		setToggleClickCount(1); // One click expand.
 		
 		CustomIconRenderer renderer = new CustomIconRenderer();
 		setCellRenderer(renderer);
 		
 		addTreeSelectionListener(new TreeSelectionListener() {
 			 /**
 			  * The valueChanged listener is called all the time.
 			  * The most crucial part is that it accurately distinguishes between class and product selection.
 			  * If it's a class selection, it fires a classSelectionEvent which is either handled as a DB
 			  * or cache query. A product selection results in a productSelection event firing which ends up
 			  * filling in the form in the GUI.
 			  */
 			@Override
 			public void valueChanged(TreeSelectionEvent event) {
 				if(event.isAddedPath()){
 					if (productEditedListener != null && lastSelectedNode != null && (!lastSelectedNode.isClassNode())) {
 						ActionEvent edited = new ActionEvent((Product)lastSelectedNode.getStoredObject(), 0, "");
 						productEditedListener.actionPerformed(edited);
 					}
 					
 					TreePath path = event.getNewLeadSelectionPath();
 					NodeItem selectedNode = (NodeItem) path.getLastPathComponent();
 					
 					if (selectedNode.isClassNode()) {
 						String s = selectedNode.getStoredObject().toString();
 						ActionEvent i = new ActionEvent(model, 0, s);
 						classSelectListener.actionPerformed(i);
 					}
 					else {	
 						//The user has selected a product. So we'll update the form.
 						ActionEvent s = new ActionEvent((Product)selectedNode.getStoredObject(), 0, "");
 						productSelectedListener.actionPerformed(s);
 						lastSelectedNode = selectedNode;
 					}
 
 				}
 			}
 		});
 		
 		addTreeExpansionListener(new TreeExpansionListener() {
 			
 			@Override
 			public void treeCollapsed(TreeExpansionEvent event) {
 			}
 			/**
 			 * The treeExpanded method is similar to valueChanged - it fires the same classSelected event
 			 * but seeing as products can't have children it doesn't need to worry about updating the form.
 			 * The main purpose in this existing is for when products are added and automatically scrolled to.
 			 * This doesn't could as a valueChanged event, but a treeExpanded event, and it needs to react accordingly.
 			 */
 			@Override
 			public void treeExpanded(TreeExpansionEvent event) {
 				
 				TreePath path = (TreePath) event.getPath();
 				if (!(path == null)){
 					NodeItem selectedNode = (NodeItem) event.getPath().getLastPathComponent();				
 					Object o = selectedNode.getStoredObject();
 					if (o.getClass().equals(Class.class)){
 						String s = o.toString();
 						ActionEvent i = new ActionEvent(model, 0, s);
 						classSelectListener.actionPerformed(i);
 					}
 				}
 			}
 		});
 		
 		
 	}
 	
 	/**
 	 * This returns an arrayList of the selected product(s). It's a list because if
 	 * the user is on a parent node then it should return the children of that node.
 	 * This is mostly used to send information to the delete method when the delete listener fires.
 	 * There is a confirmation if the user selects a parent to ensure they don't accidentally delete a 
 	 * bunch of things, and the user cannot select the root node and wipe everything this way either.
 	 * 
 	 * @return the products selected.
 	 */
 	public ArrayList<Product> getSelectedProduct() {
 		
 		ArrayList<Product> products = new ArrayList<Product>();
 		
 		NodeItem selectedNode = getSelectedNode(); 
 		Object o = selectedNode.getStoredObject();
 		
 		if(!(selectedNode == root)){
 			//if it's a single node, return the class contained in it.
 			if(!o.getClass().equals(Class.class)){				
 				products.add((Product) o);
 			}
 			else
 			{
 				int n = JOptionPane.showConfirmDialog(
 					    this.getParent(),
 					    "This will delete all products in this category\n" + "Are you sure you wish to do this?" ,
 					    "Delete Products",
 					    JOptionPane.YES_NO_OPTION);
 				if (n == 0)
 				{
 					//Get the children
 					Enumeration<NodeItem> children = selectedNode.children();
 					while (children.hasMoreElements())
 					{
 						NodeItem child = (NodeItem) children.nextElement();
 						if (!child.getStoredObject().getClass().equals(Class.class)){
 							products.add((Product) child.getStoredObject());
 						}	
 						//Recurse to get grandchildren
 						if (child.getChildCount() > 0){
 							products.addAll(EnumToList(child.children()));
 						}
 					}
 				}
 			}
 		}
 		return products;
 	}
 	
 	public ArrayList<Product> EnumToList(Enumeration<NodeItem> e){
 		ArrayList<Product> list = new ArrayList<Product>();
 		while (e.hasMoreElements())
 		{
 			NodeItem child = (NodeItem) e.nextElement();
 			if (!child.getStoredObject().getClass().equals(Class.class)){
 				list.add((Product) child.getStoredObject());
 			}
 			//Recursive
 			if (child.getChildCount() > 0){
 				list.addAll(EnumToList(child.children()));
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * This is used as a helped method for getSelectedProducts
 	 * @return selectedNode the selected node (leaf or parent)
 	 */
 	private NodeItem getSelectedNode(){
 		TreePath selectionPath = getSelectionPath();
		NodeItem selectedNode = (NodeItem)
 				selectionPath.getLastPathComponent();
		return selectedNode;
 	}
 	
 	/**
 	 * Adds a product to the tree. It looks up the map with its class type to figure out 
 	 * where it should be inserted.
 	 * @param product
 	 */
 	public void addProduct(Product product){	
 		NodeItem node = new NodeItem(product);
 		NodeItem parent = getNodeFromMap(product.getClass().toString());
 		
 		model.insertNodeInto(node, (MutableTreeNode) parent, parent.getChildCount());
 		scrollPathToVisible(new TreePath(node.getPath()));
 		
 		// Also fires an event as insertNode is called when a product is added
 		// but the valueChanged listener is not triggered, so manually trigger it here.
 		ActionEvent s = new ActionEvent(product, 0, "");
 		productSelectedListener.actionPerformed(s);
 	}
 	
 	/**
 	 * Remove a specific product. Uses the findNode helper method to work out
 	 * the node's parent, then it's a simple call.
 	 * @param p the product to remove.
 	 */
 	public void removeProduct(Product p) {
 		NodeItem n = findNode(p);
 		if (n != null)
 		{
 			model.removeNodeFromParent(n);
 			repaint();
 		}
 	}
 	
 	/**
 	 * This is called when an update event trickles back down to the tree from the model.
 	 * Updates the string representing the node on the tree based on the produt's toString() method.
 	 * Also uses the helper method findNode.
 	 * @param p
 	 */
 	public void updateProduct(Product p){
 		NodeItem n = findNode(p);
 		if (n != null)
 		{
 			n.setName(p);
 			repaint();
 		}
 	}
 	
 	/**
 	 * This method is a little tricky - given product p it can work out the parent of it,
 	 * but not the actual node where p is stored. So it has to Enumerate over the set of children.
 	 * If it finds a node containing the same object as the one it has, that's the one we want.
 	 * @param p the product to find
 	 * @return the node containing Product p
 	 */
 	public NodeItem findNode(Product p){
 		Class<?> c = p.getClass();
 		NodeItem parent = getNodeFromMap(c.toString());
 		Enumeration<NodeItem> children = parent.children();
 		
 		while (children.hasMoreElements()){
 			NodeItem child = children.nextElement();
 			if(child.getStoredObject().equals(p))
 			{
 				return child;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Method called from GUI in the event of a database switchout.
 	 */
 	public void empty(){
 		map.clear();
 		root = new NodeItem(Product.class);
 		model = new DefaultTreeModel(root);
 		setModel(model);
 		repaint();
 		updateUI();
 	}
 	
 	/**
 	 * Takes a classList and adds each class to the productTree
 	 * @param classList
 	 */
 	public void addProductStructure(Set<Class<?>> classList){
 		for (Class<?> c : classList) {
 			addProductStructure(c);
 		}
 		// Automatically expand the product node.
 		expandPath(new TreePath(root));
 	}
 	
 	/**
 	 * Adds a class to the productTree. These are the abstract product types and
 	 * serve as parent nodes to product instances (e.g. Jacket XXL Red)
 	 * and also abstract product subtypes (e.g. Jackets)
 	 * @param c the class to add.
 	 */
 	public void addProductStructure(Class<?> c){
 		//if c has a superclass that isn't Product (i.e if it's a subcategory)
 		//yet the superclass hasn't been added yet, recursively call this method
 		//until we either get to a category that HAS been added, or start at the top
 		//category and recurse our way back down.
 		if (c.getSuperclass() != Product.class && !hasClassBeenAdded(c.getSuperclass()))
 		{
 			addProductStructure(c.getSuperclass());
 		}
 		//Due to the above statement, classes may have been added in a different order
 		//to how they are stored in the classList. This stops them re-adding themselves.
 		//You cannot put this check around the initial method call as it will not work.
 		if (!hasClassBeenAdded(c))
 		{
 			NodeItem parent;
 			NodeItem node = new NodeItem(c);
 			//Get the appropriate parent node for this category
 			parent = getNodeFromMap(c.getSuperclass().toString());
 			//Add it to the nodeMap for future subcategories to use
 			addToNodeMap(c.toString(), node);
 			//insert the node into the tree and scroll it
 			model.insertNodeInto(node, parent, parent.getChildCount());
 		}
 	}
 	
 	/**
 	 * Method to determine whether the class has been added to the tree yet.
 	 * Not entirely necessary but tidies code a little.
 	 * @param c class name
 	 * @return true if the class has been added to the tree.
 	 */
 	public boolean hasClassBeenAdded(Class<?> c){
 		if (getNodeFromMap(c.toString()).equals(root))
 		{
 			return false;
 		}
 		return true;	
 	}
 	
 	/**
 	 * Insert item to the nodeMap
 	 * @param s the class's toString() value
 	 * @param n the node 
 	 */
 	public void addToNodeMap(String s, NodeItem n){
 		map.put(s, n);		
 	}
 	
 	/**
 	 * Used to insert subcategories under the correct node.
 	 * @param s the class
 	 * @return n the node (insertion point). If there is no correlating
 	 * node it returns the rootNode - this happens when a tier 1 product type
 	 * calls this method.
 	 */
 	public NodeItem getNodeFromMap(String s){
 		NodeItem n = map.get(s);
 		if (n == null) {
 			return root;
 		}
 		return n;	
 	}
 	
 	/**
 	 *  Self explanatory. 
 	 * @param a
 	 */
 	
 	public void addClassSelectListener(ActionListener a){
 		classSelectListener = a;
 	}
 	
 	public void addProductSelectedListener(ActionListener a){
 		productSelectedListener = a;
 	}
 	
 	public void addProductEditedListener(ActionListener a){
 		productEditedListener = a;
 	}
 	
 }

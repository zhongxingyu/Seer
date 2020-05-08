 package pimp.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.WindowConstants;
 
 import pimp.annotations.CompanionForm;
 import pimp.controller.ProductController;
 import pimp.form.FormBuilder;
 import pimp.form.ProductForm;
 import pimp.gui.tree.ProductTree;
 import pimp.model.Product;
 import pimp.model.ProductModel;
 
 /**
  * The main user interface window.
  * 
  * @author Daniel Schramm, Joel Harrison, Ellie Rasmus, Joel Mason
  *
  */
 public class ProductGui extends JFrame {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4171458789832604658L;
 
 	// Controller
 	private ProductController controller;
 	
 	// Views
 	private JFrame frame;
 	private ProductTree tree; 
 	private JScrollPane treeScrollPanel;
 	
 	// A reference to the form builder, we use this to create forms and retrieve objects from forms. 
 	private FormBuilder fb;
 	@SuppressWarnings("rawtypes")
 	private ProductForm form;
 	private Product selectedProduct;
 	
 	/** 
 	 * Constructor. Takes a reference to the controller and the model.
 	 */
 	public ProductGui(final ProductController controller, ProductModel model) {
 		
 		// Setup controller.
 		this.controller = controller;
 		
 		// Setup observation of model. This is so that the view will accurately reflect the
 		// contents of the model.
 		model.addProductsAddedListener(new productAddedListener());
 		model.addProductsDeletedListener(new productDeletedListener());
 		model.addProductUpdatedListener(new productUpdatedListener());
 		
 		// Setup view.
 		frame = new JFrame("Product Inventory Management Program (PIMP)");
 		frame.setPreferredSize(new Dimension(700, 500));
 
 		// Exit application when close button clicked. Also commit the cache
 		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 
 		
 		// Exit application when close button clicked. Also commit the cache.
 		frame.addWindowListener(new WindowAdapter(){
 		      @SuppressWarnings("static-access")
 			public void windowClosing(WindowEvent we){
 		    	  Object[] options = {"Yes",
 		                    "No",
 		                    "Cancel"};
 		    	  
 		    	  //Confirmation dialog for saving the changes.
 		    	  int n = JOptionPane.showOptionDialog(frame,
 		    			  "Would you like to save the changes you have made?" ,
 						    "Save Changes?",
 		    			    JOptionPane.YES_NO_CANCEL_OPTION,
 		    			    JOptionPane.QUESTION_MESSAGE,
 		    			    null,
 		    			    options,
 		    			    options[2]);		    	  
 					if (n == 0)
 					{
 						//User specified to save
 						controller.commitCache();
 					}
 					if (n != 2)
 					{
 						frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
 			    	    System.exit(0);
 					}
 		      }});
 		
 		// Create product tree.
 		tree = new ProductTree();
 		tree.setPreferredSize(new Dimension(200, 18));
 		tree.addClassSelectListener(new classChangedListener());
 		tree.addProductSelectedListener(new productSelectedListener());
 		//tree.addProductEditedListener(new productEditedListener());
 		treeScrollPanel = new JScrollPane(tree);
 		treeScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
 		treeScrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		// Add panels.
 		frame.getContentPane().add(treeScrollPanel, BorderLayout.WEST);
 		frame.getContentPane().add(createButtonPanel(), BorderLayout.NORTH);
 		
 		// Initialise form builder
 		fb = new FormBuilder();
 	}
 	
 	/**
 	 * Show the gui.
 	 */
 	public void display() {
 		frame.pack();							// Size the window to fit contents.
 		frame.setLocationRelativeTo(null); 		// Center frame on screen.
 		frame.setVisible(true); 				// Show the gui.
 	}
 	
 	/**
 	 * This method creates and returns the button panel.
 	 */
 	private JPanel createButtonPanel() {	
 		
 		// Create New Button
 		JButton btnNew = new JButton("New");
 		btnNew.addActionListener(new ActionListener() {
 			@Override()
 			public void actionPerformed(ActionEvent e) {
 				controller.createNewProduct();	
 			}
 		});
 
 		// Create Open Database Button
 		JButton btnOpenProducts = new JButton("Open");
 		btnOpenProducts.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/** A new database has been opened. We need to reset the tree,
 				 *  reload the classes and redraw the GUI.
 				 */
 				tree.empty();
 				controller.open();
 				controller.loadClasses();
 				invalidate();
 				validate();
 				repaint();
 			}
 		});
 		
 		// Create Delete Button
 		JButton btnDelete = new JButton("Delete");
 		btnDelete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/**
 				 *  This will give the controller a list of products set to be deleted.
 				 *  Must return a list as the user may have clicked on a parent node - 
 				 *  attempting to delete all products in that category.
 				 **/
				controller.removeFromCache(tree.getSelectedProduct());
 			}
 		});
 		
 		// Create Copy Product Button
 		JButton btnCopyProduct = new JButton("Copy");
 		btnCopyProduct.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/**
 				 * Calls the controller to create a deep copy of the product, 
 				 * based on the current state of the form.
 				 */
 				Product c = getCurrentProductState();
 				if(c != null){
 					controller.createNewProduct(c);
 				}
 			}
 		});
 		
 		// Create Save As Database Button
 		JButton btnSaveAsProducts = new JButton("Save As");
 		btnSaveAsProducts.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/**
 				 * A call to the controller to save the database as a new name/location.
 				 * Effectively an "export" button.
 				 */
 				controller.saveAs();
 			}
 		});
 		
 		JButton btnCommit = new JButton("Save");
 		btnCommit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				/**
 				 * Firstly, this should save the current state of the product in case 
 				 * the user has been editing it.
 				 * Secondly it calls the controller to commit changes to the cache
 				 * to the persistence layer.
 				 */
 				updateProduct(selectedProduct);
 				controller.commitCache();
 			}
 		});
 		
 		// Add buttons to panels.
 		//Product related buttons
 		JPanel leftPanel = new JPanel(new FlowLayout());
 		leftPanel.add(btnNew);
 		leftPanel.add(btnCopyProduct);
 		leftPanel.add(btnDelete);
 		
 		//DB related buttons
 		JPanel rightPanel = new JPanel(new FlowLayout());
 		rightPanel.add(btnOpenProducts);
 		rightPanel.add(btnCommit);
 		rightPanel.add(btnSaveAsProducts);
 		
 		// Add panels to button panel.
 		JPanel buttonPanel = new JPanel(new BorderLayout());
 		buttonPanel.add(leftPanel, BorderLayout.WEST);
 		buttonPanel.add(rightPanel, BorderLayout.EAST);
 
 		return buttonPanel;
 	}
 		
 	/*
 	 * This checks whether we are currently used a form or a companion form, 
 	 * and returns the object representing the current form state. 
 	 * 
 	 **/
 	public Product getCurrentProductState(){
 		Product c;
 		if(form != null){
 			try {
 				c = (Product) form.getProduct();
 				return c;
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 * This is called by the product tree on valueChange so that when a new tree item is 
 	 * selected, any edits made to the previously selected product will be saved. 
 	 * The current object state is retrieved by passing the form/companion form through
 	 * the form builder.
 	 * */	
 
 	
 	class classChangedListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		/**
 		 * The user has clicked on a new class in the tree.
 		 * This needs to tell the controller, which will query the model
 		 * to see if that class has been loaded yet.
 		 * If it hasn't, we expect the products to be loaded and
 		 * the event flow will come trickling back down.
 		 */
 			controller.getProductsByClass(e.getActionCommand());
 		}
 	}
 	
 	/**
 	 * This method tells the controller that a product has been modified.
 	 * It gives the controller the original product and another product, 
 	 * effectively representing a list of changes needing to be made to
 	 * the original. This is processed further up the chain, and changes are made.
 	 * @param original product
 	 */
 	public void updateProduct(Product original){
 		try {
 			//Get the changes from the current state of the form
 			Product updated = getCurrentProductState();
 			if (updated != null) { // Check for blank form.
 			controller.updateCacheItem(original, updated);
 			}
 		} catch (IllegalArgumentException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * The user has selected a product in the view.
 	 * This needs to be reflected by changing the form to display
 	 * the details of the product.
 	 *
 	 */
 	class productSelectedListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			updateProductForm((Product) e.getSource());
 		}
 	}
 	
 	
 	/**
 	 * Method to update the form based on the currently selected product.
 	 * @param product, the product to display details for
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void updateProductForm(Product product) {
 		try {
 			//If there's a current form being displayed
 			if (form != null) {
 				//Save the state of the product being displayed 
 				updateProduct(selectedProduct);
 				//And remove the form to make ready for the new one.
 				frame.getContentPane().remove(form);
 			}
 			
 			Class<?> companionClass = null;
 			
 			/**
 			 * If the product's class has an annotation stating that there's an attached 
 			 * companionForm, then try to get that form and assign it to CompanionClass.
 			 */
 			if (product.getClass().isAnnotationPresent(CompanionForm.class)) {
 				CompanionForm cf = product.getClass().getAnnotation(CompanionForm.class);
 				String className = null;
 				if(!cf.form().equals(CompanionForm.NULL)){
 					className = cf.form();
 					try {
 						companionClass = Class.forName(className, true, product.getClass().getClassLoader());
 					} catch (ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			/**
 			 * If that worked (and it's not null) set that as the form, and feed the 
 			 * product into that form type. That will use reflection and display the details etc.
 			 */
 			if(companionClass != null)
 			{
 				form = (ProductForm) companionClass.newInstance();
 				form.setProduct(product);
 				frame.getContentPane().add(form, BorderLayout.CENTER);
 			}
 			/**
 			 * Otherwise it failed - there was an annotation but no form was actually found.
 			 * Make a form with out own formbuilder.
 			 */
 			else
 			{
 				form = fb.createForm(product);
 				frame.getContentPane().add(form, BorderLayout.CENTER);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 		//Update the gui and the variable tracking the selected product.
 		selectedProduct = product;
 		frame.validate();
 		frame.repaint();
 	}
 	
 	public void setClasses(Set<Class<?>> classList) {
 		// Need to get rid of any companion form classes in the directory.
 		tree.addProductStructure(classList);
 	}
 	
 	/* Listeners to observe model. 
 	 * These correspond to changes in the cache/model, not
 	 * changes in the gui.*/
 	
 	/**
 	 * Products have been added to the model.
 	 * Pass this information down to the tree.
 	 *
 	 */
 	class productAddedListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			@SuppressWarnings("unchecked")
 			List<Product> products = (List<Product>) e.getSource();
 			for (Product p : products){
 				tree.addProduct(p);
 			}
 		}
 	}
 	
 	/**
 	 * Products have been deleted from the model.
 	 *
 	 */
 	class productDeletedListener implements ActionListener{
 		@SuppressWarnings("unchecked")
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//Remove from tree;
 			List<Product> products = (List<Product>) e.getSource();
 			for (Product p : products){
 				tree.removeProduct(p);
 			}
 		}
 	}
 	/**
 	 * Products have been updated in the model.
 	 */
 	class productUpdatedListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent u) {
 			tree.updateProduct((Product) u.getSource());
 		}
 	}
 
 	/**
 	 * Helper method for database switchouts.
 	 */
 	public void empty() {
 		tree.empty();
 	}
 
 }

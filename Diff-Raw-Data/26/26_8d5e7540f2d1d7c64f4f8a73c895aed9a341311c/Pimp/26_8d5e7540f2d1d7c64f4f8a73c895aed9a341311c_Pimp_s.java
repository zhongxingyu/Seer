 /**
  * This is the main class for the PIMP program.
  */
 
 package pimp;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import pimp.gui.MainDisplay;
 import pimp.gui.SelectProductDialog;
 import pimp.productdefs.Car;
 import pimp.productdefs.Product;
 import pimp.testdefs.TestClass;
 
 /**
  * The Pimp class acts as the controller for our inventory management program.
  */
 public class Pimp {
 	
 	public MainDisplay gui; // Why is this static, can we change? -DS
 	private List<Product> products;
 	
 	// Maintain class list.
 	private ProductLoader loader;
 	
 	/** 
 	 * Pimp is essentially the Controller
 	 * This involves applying appropriate ActionListeners to the given View
 	 */
 	public Pimp(MainDisplay gui) {
 		// Create empty product list.
 		products = new ArrayList<Product>();
 		loader = new ProductLoader("directory"); //perhaps directory will have to be a cmd argument
 		this.gui = gui;
 		gui.setVisible(true);
 		gui.addNewProductListener(new newProductListener());
 		
 		/** TODO add code to automatically load previous product list. */
 		//loadProducts();
 		
 		/**
 		 * The code below needs to be commented and refactored -DS
 		 */
 		FormBuilder fb = new FormBuilder(TestClass.class);
 		@SuppressWarnings("deprecation")
 		TestClass tc1 = new TestClass(10, 12.0, "PIMP", new Date(2012, 4, 3), Color.BLUE);
 		JPanel newForm;
 		try {
 			newForm = (JPanel) fb.fillForm(tc1);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			newForm = new JPanel();
 			newForm.setBackground(Color.RED);
 			newForm.setSize(new Dimension(200, 200));
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			newForm = new JPanel();
 			newForm.setBackground(Color.RED);
 			newForm.setSize(new Dimension(200, 200));
 			//e.printStackTrace();
 		}
 		setDynamicProductForm(newForm);
 		
 	}
 
 	public void setDynamicProductForm(JPanel form){
 		//Where should this kind of logic be? Oh this is terribly confusing.
 		form.setBounds(0, 0, gui.dynamicPanel.getWidth(), gui.dynamicPanel.getHeight());
 		gui.dynamicPanel.removeAll();
 		gui.dynamicPanel.add(form);
 		form.setVisible(true);
 		gui.validate();
 		gui.setVisible(true);
 		gui.repaint();
 	}
 	
 	/**
 	 * This ActionListener is applied to the New button on the main gui. 
 	 * When clicked it needs to launch a NewProductDialog, retrieve the input
 	 * from that and create a product of the returned type
 	 */
 	class newProductListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//The action has been triggered by the main gui
 			// create new product dialog.
 			SelectProductDialog selectDialog = new SelectProductDialog(gui, loader.getClassList());
 				
 			//The event has been triggered by the new product dialog. 
 			//This means we can go ahead and create the product now.
 			Class<? extends Product> c = selectDialog.getSelectedClass();
			if (c != null) {
				System.out.println("You selected: " + c.getName());
			}
			else System.out.println("No selection.");
 			
 			try {
 				/*Currently crashing here, but I think this is because all I've
 				  had to test with so far is the abstract Product class.
 				*/
				products.add(c.newInstance());
 				
 				//TODO Other things that will need to happen here
 			} catch (InstantiationException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IllegalAccessException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 		
 	}
 	
 	/**
 	 * Delete the specified product.
 	 * 
 	 * @param p
 	 */
 	class deleteButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 	/** 
 	 * Save products to file.
 	 */
 	class saveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 	/**
 	 * Load products from file.
 	 */
 	class loadButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 }

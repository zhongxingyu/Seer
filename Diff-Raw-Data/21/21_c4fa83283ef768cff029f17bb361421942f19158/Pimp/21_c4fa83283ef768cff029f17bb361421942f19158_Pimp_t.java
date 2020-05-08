 /**
  * This is the main class for the PIMP program.
  */
 
 package pimp;
 
 // Gui
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.File;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import pimp.gui.DatabaseSelector;
 import pimp.gui.MainDisplay;
 import pimp.gui.SelectProductDialog;
 import pimp.model.ProductModel;
 import pimp.model.Status;
 import pimp.persistence.DataAccessor;
 import pimp.productdefs.Drink;
 import pimp.productdefs.Product;
import pimp.productdefs.Shoe;
 
 
 /**
  * The Pimp class acts as the controller for our inventory management program.
  * 
  * @author Daniel Schramm, Ellie Rasmus
  */
 public class Pimp {
 	
 	// Database stuff
 	private String defaultDatabaseName = "test.db";
 	
 	private ProductModel cache;
 	
 	// Product classes.
 	private DirectoryClassLoader dcl;
 	private String productPackage = "pimp.productdefs";
 	private String productDir = "products"; /* Not sure what format this should take
 												may need to be a cmd argument. */ 
 	
 	private MainDisplay gui;	// View.	
 	
 	/**
 	 * Main method just creates a new Pimp object.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Pimp();
 	}
 	
 	/** 
 	 * Pimp is essentially the Controller
 	 * This involves applying appropriate ActionListeners to the given View
 	 */
 	public Pimp() {
 		// Load class definitions.
 		dcl = new DirectoryClassLoader(productDir, productPackage);
 		
 		//Initialise cache
 		cache = new ProductModel();
 		
 		// Initialize Gui
 		gui = new MainDisplay(this, cache);
 		
 		// Load existing products.
 		initialiseDB(defaultDatabaseName);
 		
 		// Make form.
 		createForm();
 
 		gui.display();
 	}
 	
 	private void createForm() {
 		
 		// Fill the form.
 		Drink drink = new Drink();
 		drink.capacity = "Large";
 		drink.flavour = "Blue";
 		drink.name = "Gatorade";
 		drink.quantity = 40;
 		
 		// Update the form displayed by the GUI.
 		gui.updateProductForm(drink);
 
 		/*Shoe shoe = new Shoe();
 		shoe.name = "STYLISH SHOOOOE";
 		shoe.quantity = 4;
 		shoe.shoeSize = 12;
 		shoe.sizingSystem = "EU";
 		gui.updateProductForm(shoe);*/
 
 	}
 	
 	public List<Class<?>> getClassList() {
 		return dcl.getClassList();
 	}
 
 	public List<Class<?>> getConcreteProductList() {
 		List<Class<?>> fullList = getClassList();
 		List<Class<?>> productClassList = new ArrayList<Class<?>>();
 		for(Class<?> c: fullList){
 			/*if(c.getSuperclass() == Product.class){
 				productClassList.add(c);
 			}*/
 			if(getAllSuperclasses(c).contains(Product.class)){
 				productClassList.add(c);
 			}
 		}
 		return productClassList;
 	}
 	
 	public List<Class<?>> getAllSuperclasses(Class<?> c){
 		List<Class<?>> superClasses = new ArrayList<Class<?>>();
 		Class sc = c.getSuperclass();
 		while(sc != null && sc != Object.class){
 			superClasses.add(sc);
 			sc = sc.getSuperclass();
 		}
 		return superClasses;
 	}
 	
 	public void createNewProduct() {
 		// Create and show product dialog.
 		SelectProductDialog selectDialog = new SelectProductDialog(gui, 
 				getConcreteProductList());
 		
 		// Create product from selected class.
 		Class<? extends Product> c = selectDialog.getSelectedClass();
 		if (c != null) {
 			try {
 				Product p = c.newInstance();
 				p.name = "New " + c.getSimpleName();
 				cache.add(p);
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void getProductsByClass(String className) {
 		if (!cache.isLoaded(className)){
 			Map<Integer, Product> m = DataAccessor.getIdToProductMap(className);
 			ArrayList<Product> l = new ArrayList<Product>();
 			for (Product p : m.values()) {
 			    l.add(p);
 			}
 			cache.add(l);
 			cache.addToClassesLoaded(className);
 		}
 	}	
 
 	/**
 	 * Delete a list of products.
 	 * 
 	 * @param products the list of products to be deleted.
 	 */
 	public void remove(List<Product> products){
 		//Fire it over to the cache.
 		cache.delete(products);
 	}
 	
 
 	/**
 	 * Can someone please figure out how to get pairs working
 	 * @param p
 	 */
 	public void update(ArrayList<Product> p){
 		Product product = p.get(0);
 		Product changes = p.get(1);
 		cache.update(product, changes);
 	}
 
 	
 	public void initialiseDB(String databaseName) {
 		DataAccessor.initialise(databaseName);
 		// Load existing products.
		List<Class<?>> cpl = getConcreteProductList();
		gui.setClasses(cpl); // must be called before setProducts.
 	}
 	
 	/**
 	 * Brings up a dialog to select the database file
 	 * and load products from that database.
 	 */
 	public void open() {
 		DatabaseSelector dbs = new DatabaseSelector();
 		File file = dbs.getFile("Open");
 		if (file != null) {
 			gui.tree.empty(); // TODO need to encapsulate
 			initialiseDB(file.getName());
 			JOptionPane.showMessageDialog(gui, 
 					"Database " + file.getName() + " opened");
 		}
 	}
 	
 	/**
 	 * Brings up a dialog to choose/create new database location.
 	 * The current database will be copied to the new location, 
 	 * and all subsequent database transactions will be performed at
 	 * the new location.
 	 */
 	public void saveAs() {
 		// Get file to process from user.
 		DatabaseSelector dbs = new DatabaseSelector();
 		File file = dbs.getFile("Export");
 		
 		if (file != null) {	// If the user selected a file...
 			try {
 				DataAccessor.exportDb(file);
 				JOptionPane.showMessageDialog(gui, 
 						"Database successfully exported to " + 
 						file.getName() + ".");
 			} catch (Exception e1) {
 				e1.printStackTrace();
 				JOptionPane.showMessageDialog(gui, 
 					"Could not export database to this location");
 			}
 		}
 	}
 
 }

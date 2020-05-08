 package pimp.productdefs;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 import pimp.productdefs.Product;
import pimp.productdefs.ShoeCompainionForm;
 
 public class Shoe extends Product {
 	
 	public double shoeSize;
 	public String sizingSystem;
 	
 	public Shoe() {
 		super();
 		super.setCompanionFormClass(ShoeCompainionForm.class);
 	}
 	
 	
 	@Override
 	public String toString() {
 		return this.name;
 	}
 
 
 	// ignore - Just used to test the shoe companion form 
 	public static void main(String[] args) {
 		// Commented out while changing comp. form
 		/*System.out.println("Hello");
 		Shoe s = new Shoe();
 		s.name = "Nike Air";
 		s.quantity = 10;
 		s.shoeSize = 9.5;
 		s.sizingSystem = "US";
 		
 		ShoeCompainionForm scf = new ShoeCompainionForm();
 		scf.updateForm(s);
 		
 		System.out.println(scf.getObject());
 		
 		JFrame testFrame = new JFrame();
 		testFrame.add(scf.getForm());
 		testFrame.pack();
 		testFrame.setVisible(true);*/
 	}
 }

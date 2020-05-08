 package pimp.form;
 
 import java.awt.Color;
 
 import javax.swing.JColorChooser;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 import org.junit.Before;
 
 import pimp.form.ColorFormElement;
 import pimp.form.StringFormElement;
 import pimp.shoe.Shoe;
 import pimp.shoe.ShoeCompainionForm;
 
 public class ShoeCompanionFormTest extends junit.framework.TestCase{
 	
 	Shoe s;
 	
 	@Before
 	public void setUp() throws Exception {
 		s = new Shoe();
 		s.name = "Nike Air Ones";
		s.quantity = 0;
 
 	}
 	
 	public void testSetValue() {
 		
 		ShoeCompainionForm scf = new ShoeCompainionForm();
 		scf.setProduct(s);
 	}
 
 	public void testGetValue() {
 		
 		ShoeCompainionForm scf = new ShoeCompainionForm();
 		scf.setProduct(s);
 		System.out.println(scf.getProduct());
 		scf.setProduct(scf.getProduct());
 		
 	}
 	
 	public void testGetValueAfterSetting() {
 		
 		ShoeCompainionForm scf = new ShoeCompainionForm();
 		s.shoeSize = 10.0;
 		s.sizingSystem = "NZ";
 		scf.setProduct(s);
 		
 		System.out.println(scf.getProduct());
 		
 	}
 }

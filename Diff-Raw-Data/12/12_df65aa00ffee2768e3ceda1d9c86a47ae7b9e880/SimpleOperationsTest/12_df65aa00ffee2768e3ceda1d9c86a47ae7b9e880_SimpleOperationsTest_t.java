 package gov.va.research.inlp.gate;
 
import org.apache.commons.validator.GenericValidator;

 import gov.va.research.inlp.gate.HeaderVO;
 import junit.framework.TestCase;
 
 
 public class SimpleOperationsTest extends TestCase {
 	
 	public void testSimpleInstantiation() {
 		HeaderVO c = new HeaderVO();
 		junit.framework.Assert.assertNotNull("Null object.", c);
 		
		
		String a = "";
		if (a != null && a.length() > 0) {
			
		}
		
		if (GenericValidator.isBlankOrNull(a)) {
			
		}
 	}
 
 }

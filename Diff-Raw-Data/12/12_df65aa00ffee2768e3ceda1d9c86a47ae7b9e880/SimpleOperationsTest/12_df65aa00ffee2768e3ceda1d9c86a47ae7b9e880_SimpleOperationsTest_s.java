 package gov.va.research.inlp.gate;
 
 import gov.va.research.inlp.gate.HeaderVO;
 import junit.framework.TestCase;
 
 
 public class SimpleOperationsTest extends TestCase {
 	
 	public void testSimpleInstantiation() {
 		HeaderVO c = new HeaderVO();
 		junit.framework.Assert.assertNotNull("Null object.", c);
 		
 	}
 
 }

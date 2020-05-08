 package model.common;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: murphyra
  * Date: 9/22/12
  * Time: 1:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BarcodeTest {
 	@Before
 	public void setUp() throws Exception {
		Barcode foo = Barcode.newFromId("123");
 	}
 
 	@Test
 	public void testGenerateFromId() throws Exception {
 	}
 
 	@Test
 	public void testGetNumber() throws Exception {
 
 	}
 
 	@Test
 	public void testToString() throws Exception {
 
 	}
 }

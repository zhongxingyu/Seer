 package org.event.manager.utils;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 
 public class UtilsTest {
 
 	@Test
 	public void create_simple_array() {
		assertNotNull(Utils.arrayOf());
 	}
 
 	@Test
 	public <T> void test_creation_with_different_data() {
		String[] array = Utils.arrayOf("1", "2", "3");
 		assertNotNull(array);
 		String[] expected = {"1","2","3"};
 		assertArrayEquals(expected,array);
 	}
 	
 	@Test
 	public void valid_emails() {
 		assertTrue(Utils.isEmailAdressValid("pepe@pepe-net.info"));
 	}
 
 	@Test
 	public void invalid_emails() {
 		assertFalse(Utils.isEmailAdressValid("pepepepito.net"));
 	}
 
 	@Test
 	public void invalid_images() {
 		assertFalse(Utils.isImageLinkValid("http://goear.com/listen/7d13372"));
 	}
 
 	@Test
 	public void valid_images() {
 		assertTrue(Utils.isImageLinkValid("http://www.kaoru.org/kaoru.png"));
 	}
 
 }

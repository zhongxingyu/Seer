 import junit.framework.TestCase;
 
 public class VarargsTest extends TestCase {
 	
 	public void test_varargs() {
 		String value = varargMethod("param1", "param2");
 		assertEquals("test", value);
 	}
 	
 	private String varargMethod( Object... arguments ){
 		return "test";
 	}
 }

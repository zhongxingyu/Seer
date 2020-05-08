 import junit.framework.TestCase;
 
 public class VarargsTest extends TestCase {
 	
 	public void test_varargs() {
 		String value = varargMethod("param1", "param2");
 		assertEquals("test", value);
 	}
 	
	public void test_phrase_string() {
		String value = cmsPhraseMethod("page", "param1");
		assertEquals("test", value);
	}
	
	public void test_phrase_int() {
		String value = cmsPhraseMethod("page", 42);
		assertEquals("test", value);
	}
	
        private String cmsPhraseMethod( String idPage, Object... arguments ) {
		return "test";
        }

 	private String varargMethod( Object... arguments ){
 		return "test";
 	}
 }

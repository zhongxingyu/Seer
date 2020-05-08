 package org.wings.plaf.css;
 
 import junit.framework.TestCase;
 
 public class UtilsTest extends TestCase {
 	public void test_escapeJS() {
		// Empty.
		assertEquals(null, Utils.escapeJS(null));
		assertEquals("", Utils.escapeJS(""));
		
 		// Generic escapes.
 		assertEquals("\\\\", Utils.escapeJS("\\"));
 		assertEquals("\\b", Utils.escapeJS("\b"));
 		assertEquals("\\f", Utils.escapeJS("\f"));
 		assertEquals("\\n", Utils.escapeJS("\n"));
 		assertEquals("\\r", Utils.escapeJS("\r"));
 		assertEquals("\\t", Utils.escapeJS("\t"));
 		
 		// Special characters are encoded as utf-8 escape
 		assertEquals("\\u0000", Utils.escapeJS("\u0000"));
 		assertEquals("\\u001F", Utils.escapeJS("\u001f"));
 		assertEquals(" ", Utils.escapeJS("\u0020")); // first non-special
 		
		// And now: all together ;-)
 		assertEquals("foo\\\\\\\"bar", Utils.escapeJS("foo\\\"bar"));
 		assertEquals("\\nfoo\\\\\\\"bar", Utils.escapeJS("\nfoo\\\"bar"));
 		assertEquals("\\nfoo\\\\\\\"bar\\t", Utils.escapeJS("\nfoo\\\"bar\t"));
 	}
 }

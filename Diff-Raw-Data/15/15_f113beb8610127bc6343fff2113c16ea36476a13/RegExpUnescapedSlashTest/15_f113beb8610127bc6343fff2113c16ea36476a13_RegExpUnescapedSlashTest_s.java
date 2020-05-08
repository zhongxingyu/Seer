 package net.sourceforge.htmlunit;
 
 import org.junit.Test;
import org.junit.runner.RunWith;
 
 /**
  * Test that regexp like "[/]" are parsed correctly.
  * https://bugzilla.mozilla.org/show_bug.cgi?id=368019
  * @author Marc Guillemot
  */
@RunWith(RhinoPatchedRunner.class)
 public class RegExpUnescapedSlashTest {
 
 	/**
 	 * @throws Exception if the test fails
 	 */
 	@Test
 	public void testUnescapedSlashInSquareBrackets() throws Exception {
 		final String script = "var o = /[/]/;";
 		
 		Utilities.executeScript(script);
 	}
 }

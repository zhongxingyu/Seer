 package view;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /*
  * Testing strategy -->
  * This java file is required to test if LaTeX code is renderable as a LaTeX image
  */
 public class LatexTest {
 
 	@Test
 	public void escapeString(){
		assertEquals("! \\@ \\# \\$ \\% \\^{}" +
				" \\& * \\{ \\} \\backslash } \\\\ \\textrm{",
 				Latex.escape("! @ # $ % ^ & * { } \\ \n"));
		assertEquals("\\backslash", Latex.escape("\\"));
 		assertEquals("\\~", Latex.escape("~"));
 		String original = "hello \\ person";
		String escaped = "hello \\backslash person";
 		assertEquals(escaped, Latex.escape(original));
 	}
 	
 	
 	@Test
 	public void isLatexTest(){
 		String latex = "\\frac {V_m} {K_M+S}";
 		assertTrue(Latex.isLatex(latex));
 	}
 }

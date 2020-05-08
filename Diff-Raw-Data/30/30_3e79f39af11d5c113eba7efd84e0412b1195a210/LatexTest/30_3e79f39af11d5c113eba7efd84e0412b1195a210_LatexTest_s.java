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
		assertEquals("! @ \\# \\$ \\% \\^{}" +
				" \\& * \\{ \\} \\textbackslash \\\\",
 				Latex.escape("! @ # $ % ^ & * { } \\ \n"));
		assertEquals("\\textbackslash", Latex.escape("\\"));
 		assertEquals("\\~", Latex.escape("~"));
 		String original = "hello \\ person";
		String escaped = "hello \\textbackslash person";
 		assertEquals(escaped, Latex.escape(original));
 	}
 	
	@Test
	public void newLineInRegularText(){
		assertEquals("Hello\\\\", Latex.escape("Hello\\n"));
	}
 	
 	@Test
 	public void isLatexTest(){
 		String latex = "\\frac {V_m} {K_M+S}";
 		assertTrue(Latex.isLatex(latex));
 	}
 }

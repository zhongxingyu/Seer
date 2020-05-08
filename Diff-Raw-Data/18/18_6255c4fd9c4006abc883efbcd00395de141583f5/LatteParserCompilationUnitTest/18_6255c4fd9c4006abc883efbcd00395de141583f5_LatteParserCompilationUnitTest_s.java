 /**
  * 
  */
 package latte.lexparse;
 
 import junit.framework.TestCase;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Test;
 
 
 /**
  * @author guydmann
  *
  */
 public class LatteParserCompilationUnitTest extends TestCase {
 	public void testCompilationUnit(String input) throws RecognitionException {
 		System.out.println(input + "\n");
 		System.out.println(LatteParser.makeParser(input).compilation_unit().tree.toString() + "\n-----------\n");
 		
 	}
 	
 	@Test
 	public void testCompilationUnit01() throws Exception
 	{
 		testCompilationUnit("class test { }");
 	}
 	
 	
 	@Test
 	public void testCompilationUnit02() throws Exception
 	{
 		testCompilationUnit("class test { int a; }");
 	}
 	
 	@Test
 	public void testCompilationUnit03() throws RecognitionException
 	{
 		testCompilationUnit("class test { int a; float q; char m; boolean t;}");
 	}
 	
 	@Test
 	public void testCompilationUnit04() throws RecognitionException
 	{
 		//This test is the mandelbrot dwell function
 		//from  my fractal project at http://github.com/guydmann/fractal_app/blob/master/fractal_dwell.js
 		//i have translated from js to latte
 		//should be usable and will be helpful later to have a real piece of code to test that i already know exactly what it does
 		
 		String test_string = "";
 		test_string += "int num;\n";
 		test_string += "num:= precision;\n";
 		test_string += "int breakout;\n";
 		test_string += "breakout:= 4;\n";
 		test_string += "float dx, dy;\n";
 		test_string += "dx:= cx;\n";
 		//test_string += "float dy;\n";
 		test_string += "dy := cy;\n";
 		test_string += "float dx2, dy2;\n";
 		test_string += "dx2 := dx * dx;\n";
 		//test_string += "float dy2;\n";
 		test_string += "dy2 := dy * dy;\n";
 		test_string += "#z <= z(n-1)^2 +c\n";
 		test_string += "int count;\n";
 		test_string += "count:= 0;\n";
 		//good to here
 		test_string += "while ( count < num && dx2 + dy2 <= breakout) {\n";
 		test_string += "\tdy := 2.0 * dx * dy + cy;\n";
 		test_string += "\tdx := dx2 - dy2 + cx;\n";
 		test_string += "\tdx2 := dx * dx;\n";
 		test_string += "\tdy2 := dy * dy;\n";
 		test_string += "\tcount:=count+1;\n";
 		test_string += "}\n";
 		test_string += "system.out.alert(count + ' ' + dx + ' ' +dy);\n";
 		test_string += "return count;\n";
 
 		testCompilationUnit("class FractalDwell { int DwellMandel( float cx, float  cy, int  precision)\n{\n" + test_string + "}\n}\n");
 	}
 	
 
 	
 	@Test
 	public void testCompilationUnit05() throws RecognitionException
 	{
 		String test_string = "";
 		test_string += "class FractalDwell {\n";
 		test_string += "int DwellBuddha( float cx, float cy, int precision , int width, int height, int xinc, int yinc, float fractarray[], int lx, int ty) {\n";
 		test_string += "int num;\n";
 		test_string += "int breakout;\n";
 		test_string += "num := precision;\n";
 		test_string += "breakout := 4;\n";
 		test_string += "float dx, dy;\n";
 		test_string += "dx := cx;\n";
 		test_string += "dy := cy;\n";
 		test_string += "float dx2, dy2;\n";
 		test_string += "dx2 := dx * dx;\n";
 		test_string += "dy2 := dy * dy;\n";
 		test_string += "#z <= z(n-1)^2 +c\n";
 		test_string += "int count;\n";
 		test_string += "count := 0;\n";
 		test_string += "while ( count < num && (dx2 + dy2) <= breakout) {\n";
 		
 		test_string += "dy := ( 2.0 * (dx * dy) ) + cy;\n";
 		test_string += "dx := dx2 - dy2 + cx;\n";
 		test_string += "dx2 := dx * dx;\n";
 		test_string += "dy2 := dy * dy;\n";
 		
 		test_string += "jt := Math.floor((dx-lx)/xinc);\n";
 		test_string += "kt := Math.floor((ty-dy)/yinc);\n";
 		
 		test_string += "if (jt>=0 && jt<width && kt>=0 && kt<height) {\n";
 		
 		
 		test_string += "if((jt*height)+kt=precision){\n";
		test_string += "fractarray[1] := (jt*height)+kt;\n";
 		test_string += "} else {\n";
		test_string += "fractarray[0] := (jt*height)+kt;\n";
 		test_string += "}\n";
 		
 		test_string += "}\n";
 		test_string += "count := count +1;\n";
 		test_string += "}\n";
 	    test_string += "return fractarray;\n";
 		test_string += "}\n";
 		test_string += "}";
 		testCompilationUnit(test_string);
 		
 	}
 	
 }

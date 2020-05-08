 package swp_compiler_ss13.fuc.test;
 
 
 import java.io.*;
 import java.util.Scanner;
 
 /**
  * 
  * 
  * Example programs with expected exitcodes and expected output, including the
  * examples from common/examples.
  * 
  * 
  * @author Jens V. Fischer
  */
 public class ExampleProgs {
 
 	/* M1 progs */
 
 	public static Object[] simpleAddProg() {
 		String prog = loadExample("simple_add.prog");
 		int expectedExitcode = 6;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] addProg(){
 		String prog = loadExample("add.prog");
 		int expectedExitcode = 27;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] simpleMulProg(){
 		String prog = loadExample("simple_mul.prog");
 		int expectedExitcode = 9;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] parenthesesProg(){
 		String prog = loadExample("paratheses.prog");
 		int expectedExitcode = 8;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 
 	/* M1 progs producing errors */
 
 	public static Object[] doubleDeclaration(){
 		String prog = loadExample("error_double_decl.prog");
 
 		int expectedExitcode = -1;
		String expectedOutput = "ERROR (DOUBLE_DECLARATION): The variable 'i' of type 'LongType' has been declared twice in this scope!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] invalidIds(){
 		String prog = loadExample("error_invalid_ids.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "ERROR (UNRECOGNIZED_TOKEN): Found undefined token 'foo$bar'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] multipleMinusENotation(){
 		String prog = loadExample("error_multiple_minus_e_notation.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "ERROR (UNRECOGNIZED_TOKEN): Found undefined token '10e----1'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] multiplePlusesInExp(){
 		String prog = loadExample("error_multiple_pluses_in_exp.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "ERROR (UNRECOGNIZED_TOKEN): Found undefined token '++'!";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] undefReturnProg(){
 		String prog = loadExample("error_undef_return.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "WARNNING (UNDEFINED): Variable “spam” may be used without initialization.";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* M1 additional progs */
 
 	/* test empty prog */
 	public static Object[] emptyProg() {
 		String prog = "";
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* M2 progs */
 
 	public static Object[] assignmentProg(){
 		String prog = loadExample("assignment.prog");
 		int expectedExitcode = 10;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] condProg() {
 		String prog = loadExample("cond.prog");
 		int expectedExitcode = 5;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	public static Object[] printProg() {
 		String prog = loadExample("print.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "" +
 				"true\n" +
 				"18121313223\n" +
 				"-2.323000e-99\n" +
 				"jagÄrEttString\"\n";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* M2 additional progs */
 
 	/* test prog for regression test against return bug */
 	public static Object[] returnProg() {
 		String prog = "return;";
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* array test prog 1 */
 	public static Object[] arrayProg1() {
 		String prog = "long l; long [ 3 ] a; a [ 0 ] = 42; l = a [ 0 ]; return l;";
 		int expectedExitcode = 42;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* array test prog 2 */
 	public static Object[] arrayProg2() {
 		String prog = "long [ 3 ] a; a [ 0 ] = 42; print a [ 0 ]; return; ";
 		int expectedExitcode = 0;
 		String expectedOutput = "42\n";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* array test prog 2 */
 	public static Object[] arrayProg3() {
 		String prog = "long [ 3 ] a; a [ 0 ] = 42; return a [ 0 ];";
 		int expectedExitcode = 42;
 		String expectedOutput = "";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	/* array test return Bool */
 
 	/**
 	 * As the compiler does not implement functions yet, the return statement is
 	 * effectivly the exit code of the implicit main function. As OS'es usually
 	 * allow only positive natural numbers as exit codes, only returning longs
 	 * is specified and implemented yet.
 	 */
 	public static Object[] returnBool() {
 		String prog = "bool b; b = true; return b";
 		int expectedExitcode = 0;
 		String expectedOutput = "42\n";
 		return new Object[]{prog, expectedExitcode, expectedOutput};
 	}
 
 	private static String loadExample(String progName) {
 		String userDir = System.getProperty("user.dir");
 		String pathPrexif;
 		if (userDir.endsWith("fuc/code"))
 			pathPrexif = "test/bin/swp_compiler_ss13/fuc/test/";
 		else
 			pathPrexif = "bin/swp_compiler_ss13/fuc/test/";
 		try {
 			return new Scanner( new File(pathPrexif + progName), "UTF-8" ).useDelimiter("\\A").next();
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException("file '" + progName + "' not found at '" + System.getProperty("user.dir") + "/" + pathPrexif + "'");
 		}
 	}
 
 }

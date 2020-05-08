 package swp_compiler_ss13.common.test;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import swp_compiler_ss13.common.report.ReportType;
 
 /**
  *
  *
  * Example programs with expected exitcodes (return), expected output (print)
  * and expected report types (from errlog), including the examples from
  * common/examples.
  *
  *
  * @author Jens V. Fischer
  */
 public class ExampleProgs {
 
 	/* M1 progs */
 
 	public static Object[] simpleAddProg() {
 		String prog = loadExample("m1"+File.separator+"simple_add.prog");
 		int expectedExitcode = 6;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] addProg(){
 		String prog = loadExample("m1"+File.separator+"add.prog");
 		int expectedExitcode = 27;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] simpleMulProg(){
 		String prog = loadExample("m1"+File.separator+"simple_mul.prog");
 		int expectedExitcode = 9;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] parenthesesProg(){
 		String prog = loadExample("m1"+File.separator+"paratheses.prog");
 		int expectedExitcode = 8;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 
 	/* M1 progs producing errors */
 
 	public static Object[] doubleDeclaration(){
 		String prog = loadExample("m1"+File.separator+"error_double_decl.prog");
 
 		int expectedExitcode = -1;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {ReportType.DOUBLE_DECLARATION};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] invalidIds(){
 		String prog = loadExample("m1"+File.separator+"error_invalid_ids.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {ReportType.UNRECOGNIZED_TOKEN};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] multipleMinusENotation(){
 		String prog = loadExample("m1"+File.separator+"error_multiple_minus_e_notation.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {ReportType.UNRECOGNIZED_TOKEN};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] multiplePlusesInExp(){
 		String prog = loadExample("m1"+File.separator+"error_multiple_pluses_in_exp.prog");
 		int expectedExitcode = -1;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {ReportType.WORD_NOT_IN_GRAMMAR};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] undefReturnProg(){
 		String prog = loadExample("m1"+File.separator+"error_undef_return.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* M2 progs */
 
 	public static Object[] assignmentProg(){
 		String prog = loadExample("m2"+File.separator+"assignment.prog");
 		int expectedExitcode = 10;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] condProg() {
 		String prog = loadExample("m2"+File.separator+"cond.prog");
 		int expectedExitcode = 5;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] printProg() {
 		String prog = loadExample("m2"+File.separator+"print.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "" +
 				"true\n" +
 				"18121313223\n" +
 				"-2.323000e-99\n" +
 				"jagÄrEttString\"\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* M3 example progs */
 
 	public static Object[] fibProg(){
 		String prog = loadExample("m3"+File.separator+"fib.prog");
 		int expectedExitcode = 233;
 		String expectedOutput = "6765\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] matrixMultiplicationProg(){
 		String prog = loadExample("m3"+File.separator+"matrixMultiplication.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "14|46\n28|92\n42|138\n56|184\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	public static Object[] newtonProg(){
 		String prog = loadExample("m3"+File.separator+"newton.prog");
 		int expectedExitcode = 0;
		String expectedOutput = "i hate floating point numbers1.4142156862745097\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* Additional progs */
 
 	/* test empty prog */
 	public static Object[] emptyProg() {
 		String prog = loadExample("additional"+File.separator+"empty.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* test prog for regression test against return bug */
 	public static Object[] returnProg() {
 		String prog = loadExample("additional"+File.separator+"return.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* array test prog 1 */
 	public static Object[] arrayProg1() {
 		String prog = loadExample("additional"+File.separator+"array1.prog");
 		int expectedExitcode = 42;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* array test prog 2 */
 	public static Object[] arrayProg2() {
 		String prog = loadExample("additional"+File.separator+"array2.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "42\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* array test prog 3 */
 	public static Object[] arrayProg3() {
 		String prog = loadExample("additional"+File.separator+"array3.prog");
 		int expectedExitcode = 42;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/**
 	 * As the compiler does not implement functions yet, the return statement is
 	 * effectivly the exit code of the implicit main function. As OS'es usually
 	 * allow only positive natural numbers as exit codes, only returning longs
 	 * is specified and implemented yet.
 	 */
 	public static Object[] returnBool() {
 		String prog = loadExample("additional"+File.separator+"return_bool.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "42\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	/* creative tests */
 	public static Object[] nestedLoopsProg() {
 		String prog = loadExample("additional"+File.separator+"nested_loops.prog");
 		int expectedExitcode = 29;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 	
 	public static Object[] simpleRecordProg() {
 		String prog = loadExample("additional"+File.separator+"simple_record.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 	
 	public static Object[] recordProg() {
 		String prog = loadExample("additional"+File.separator+"record.prog");
 		int expectedExitcode = 0;
 		String expectedOutput = "Skyrim was not released\n";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 	
 	public static Object[] calendarProg() {
 		String prog = loadExample("additional"+File.separator+"calendar.prog");
 		int expectedExitcode = 29;
 		String expectedOutput = "";
 		ReportType[] reportTypes = {};
 		return new Object[]{prog, expectedExitcode, expectedOutput, reportTypes};
 	}
 
 	private static String loadExample(String progName) {
 		String userDir = System.getProperty("user.dir");
 		Path path;
 
 		/* handling different paths depending on from where the tests are called*/
 		if (userDir.endsWith("fuc/code") || userDir.endsWith("crosstesting"))
 			path = FileSystems.getDefault().getPath("common", "examples", progName);
 		else if (userDir.endsWith("fuc" + File.separator + "code/test") || userDir.endsWith("crosstesting" + File.separator + "crosstest") )
 			path = FileSystems.getDefault().getPath("..", "common", "examples", progName);
 		else {
 		   String regex = ".*fuc\\" + File.separator + "code\\" + File.separator + "[^\\" + File.separator + "]*";
 		   if (userDir.matches(regex)) /* for paths directly under fuc\code */
    		   path = FileSystems.getDefault().getPath("..", "common", "examples", progName);
    		else /* will fail */
    			path = FileSystems.getDefault().getPath(progName);
 		}
 		try {
 			return new Scanner(path, "UTF-8").useDelimiter("\\A").next();
 		} catch (IOException e) {
 			throw new RuntimeException("file '" + progName + "' not found at '" +
 					path.toAbsolutePath().normalize() + "'");
 		} catch (NoSuchElementException n) {
 			return "";
 		}
 	}
 
 }

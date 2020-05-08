 package compilateur;
 
 import java.io.BufferedReader;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import junit.framework.TestCase;
 
 import compilateur.CompilationError.Error;
 
 /**
  * Unit tests.
  * @author Paul Chaignon
  * @author Damien Le Guen
  * @author Antoine Lejeune
  * @author Benoit Travers
  */
 public class TestYaka extends TestCase {
	
	/**
	 * First test just to intialize the Yaka compiler.
	 */
	public void testFirst() {
		new Yaka(System.in);
	}
 	
 	/**
 	 * Test the declaration of the variables.
 	 */
 	public void testVariablesDeclaration() {
 		this.testProgram("tests/declaration_variables", false, false);
 	}
 	
 	/**
 	 * Test all types of declarations.
 	 */
 	public void testDeclaration() {
 		this.testProgram("tests/declaration", false, false);
 	}
 	
 	/**
 	 * Test the program for affectations part.
 	 */
 	public void testAffectations() {
 		this.testProgram("tests/affectations", false, false);
 	}
 	
 	/**
 	 * Test the inputs and outputs.
 	 */
 	public void testInputOutput() {
 		this.testProgram("tests/input_output", false, false);
 	}
 	
 	/**
 	 * Test a simple iteration.
 	 */
 	public void testSimpleIteration() {
 		this.testProgram("tests/simple_iteration", false, false);
 	}
 	
 	/**
 	 * Test nested interations.
 	 */
 	public void testNestedIterations() {
 		this.testProgram("tests/nested_iterations", false, false);
 	}
 	
 	/**
 	 * Test an expression.
 	 * Written by the teachers.
 	 */
 	public void testTeachersExpr() {
 		for(int i=1 ; i<6 ; i++) {
 			this.testProgram("tests/tests_prof/expr"+i, false, false);
 		}
 	}
 	
 	/**
 	 * Test a simple condition bloc.
 	 */
 	public void testSimpleCondition() {
 		this.testProgram("tests/simple_condition", false, false);
 	}
 	
 	/**
 	 * Test some nested conditions.
 	 */
 	public void testNestedConditions() {
 		this.testProgram("tests/nested_conditions", false, false);
 	}
 	
 	/**
 	 * Test a simple function.
 	 */
 	public void testSimpleFunction() {
 		this.testProgram("tests/simple_function", false, false);
 	}
 	
 	/**
 	 * Test the functions example from the handout.
 	 */
 	public void testNestedFunctions() {
 		this.testProgram("tests/nested_functions", false, false);
 	}
 	
 	/**
 	 * Test the factorial functions.
 	 * The second one is tail recursive.
 	 */
 	public void testFactorial() {
 		this.testProgram("tests/factorial", false, false);
 	}
 	
 	/**
 	 * Test a recursive function to compute the Fibonacci numbers.
 	 */
 	public void testFibonacci() {
 		this.testProgram("tests/fibonacci", false, false);
 	}
 	
 	/**
 	 * Check that an error is raised when the returned type of a function is incorrect.
 	 */
 	public void testReturnedTypeError() {
 		this.testError("tests/errors/returned_type", Error.RETURNED_TYPE_INCORRECT);
 	}
 	
 	/**
 	 * Check that an error is raised when an undeclared ident is used.
 	 */
 	public void testIdentUnknownError() {
 		this.testError("tests/errors/ident_unknown", Error.IDENT_UNKNOWN);
 	}
 	
 	/**
 	 * Check that an error is raised when there isn't the right number of parameters at the call of a function.
 	 */
 	public void testNumberParametersError() {
 		this.testError("tests/errors/number_parameters", Error.NUMBER_PARAMETERS_INCORRECT);
 	}
 	
 	/**
 	 * Check that an error is raised when a parameter of a function doesn't have the right type.
 	 */
 	public void testParameterTypeError() {
 		this.testError("tests/errors/parameter_type", Error.PARAMETER_TYPE_INCORRECT);
 	}
 	
 	/**
 	 * Check that an error is raised when the types don't match at an affectation.
 	 */
 	public void testAffectationTypeError() {
 		this.testError("tests/errors/affectation_type", Error.TYPE_AFFECTATION_DONT_MATCH);
 	}
 	
 	/**
 	 * Check that an error is raised when the condition in an iteration is not boolean.
 	 */
 	public void testIterationTypeError() {
 		this.testError("tests/errors/iteration_type", Error.TYPE_ITERATION_INCORRECT);
 	}
 	
 	/**
 	 * Check that an error is raised when a conditional instruction doesn't have a boolean condition.
 	 */
 	public void testConditionTypeError() {
 		this.testError("tests/errors/condition_type", Error.TYPE_CONDITION_INCORRECT);
 	}
 	
 	/**
 	 * Check that an error is raised when the name for a variable is already taken.
 	 */
 	public void testNameAlreadyTakenError() {
 		this.testError("tests/errors/name_already_taken", Error.NAME_ALREADY_TAKEN);
 	}
 	
 	/**
 	 * Check that an error is raised when a boolean is compute with an integer.
 	 */
 	public void testOperandsDontLatchError() {
 		this.testError("tests/errors/operands_dont_match", Error.OPERANDS_DONT_MATCH);
 	}
 	
 	/**
 	 * Check that an error is raised when an invalid operation occurred.
 	 */
 	public void testInvalidOperationError() {
 		this.testError("tests/errors/invalid_operation", Error.INVALID_OPERATION);
 	}
 	
 	/**
 	 * Compile a Yaka program and check that an error if effectively raised.
 	 * @param file The path to the file containing the program.
 	 * @param error
 	 */
 	private void testError(String file, Error error) {
 		String program = getContentOfFile(file+".yaka");
 		compileToASM(program);
 		assertTrue(Yaka.errors.checkTypeError(error));
 		compileToYVM(program);
 		assertTrue(Yaka.errors.checkTypeError(error));
 	}
 	
 	/**
 	 * Compile a Yaka program and compare it to the YVM and ASM programs.
 	 * @param file The path to the files. They differ in their extensions.
 	 * @param showYVMCode True to show the YVM code.
 	 * @param showASMCode True to show the ASM code.
 	 */
 	private void testProgram(String file, boolean showYVMCode, boolean showASMCode) {
 		String program = getContentOfFile(file+".yaka");
 		
 		String programYVM = getContentOfFile(file+".yvm");
 		compileToYVM(program);
 		if(showYVMCode) {
 			System.out.println("YVM:");
 			System.out.println(Yaka.yvm.getProgram());
 		}
 		assertFalse(Yaka.errors.errorsOccurred());
 		assertEquals(programYVM, Yaka.yvm.getProgram());
 		
 		String programASM = getContentOfFile(file+".asm");
 		compileToASM(program);
 		if(showASMCode) {
 			System.out.println("ASM:");
 			System.out.println(Yaka.yvm.getProgram());
 		}
 		assertFalse(Yaka.errors.errorsOccurred());
 		assertEquals(programASM, Yaka.yvm.getProgram());
 	}
 	
 	/**
 	 * Compile the program to YVM code.
 	 * @param program The Yaka program.
 	 */
 	private static void compileToYVM(String program) {
 		InputStream input = new ByteArrayInputStream(program.getBytes());
 		Yaka.ReInit(input);
 		Yaka.initVariables(true);
 	    try {
 			Yaka.analyse();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Compile the program to ASM code.
 	 * @param program The Yaka program.
 	 */
 	private static void compileToASM(String program) {
 		InputStream input = new ByteArrayInputStream(program.getBytes());
 		Yaka.ReInit(input);
 		Yaka.initVariables(false);
 	    try {
 			Yaka.analyse();
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Get all the content of a file.
 	 * @param nameFile The name of the file.
 	 * @return The content of the file.
 	 */
 	private static String getContentOfFile(String nameFile) {
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(nameFile)));
 			String content = "";
 			String line;
 			while ((line = br.readLine()) != null) {
 				content += line+"\n";
 			}
 			br.close();
 			return content;
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		return null;
 	}
 }

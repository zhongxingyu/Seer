 package swp_compiler_ss13.fuc.backend;
 
 
 import junit.extensions.PA;
 import org.junit.Assert;
 import org.junit.Test;
 import swp_compiler_ss13.common.backend.Quadruple;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 
 
 public class ExecutorTest extends TestBase {
 
 	@Test
 	public void testTryToStartLLI() throws Exception {
 
 		boolean hasLLI;
 		String exceptionMsg = null;
 		Process p = null;
 		try {
 			p = (Process) PA.invokeMethod(Executor.class, "tryToStartLLI()");
 			hasLLI = true;
 		}
 		catch (Exception e) {
 			hasLLI = false;
 			exceptionMsg = e.getCause().getCause().getMessage();
 		}
 
 		if (hasLLI) {
 			assertNotNull(p);
 			assertNull(exceptionMsg);
 		}
 		else {
 			assertEquals("Cannot run program \"lli\": error=2, No such file or directory", exceptionMsg);
 			assertNull(p);
 		}
 	}
 
 	@Test
 	public void testRunIR() throws Exception {
 
 		LLVMBackend backend = new LLVMBackend();
 
 		ArrayList<Quadruple> tac = new ArrayList<>();
 
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_LONG, "#2", Quadruple.EmptyArgument, "longVar1"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_LONG, "#2", Quadruple.EmptyArgument, "longVar2"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_LONG, Quadruple.EmptyArgument, Quadruple.EmptyArgument, "result"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.ADD_LONG, "longVar1", "longVar2", "result"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_STRING, Quadruple.EmptyArgument, Quadruple.EmptyArgument, "s"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.LONG_TO_STRING, "result", Quadruple.EmptyArgument, "s"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.PRINT_STRING, "s", Quadruple.EmptyArgument, Quadruple.EmptyArgument));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.RETURN, "result", Quadruple.EmptyArgument, Quadruple.EmptyArgument));
 
 		InputStream targetCode = backend.generateTargetCode("targetCode", tac).get("targetCode.ll");
 		Executor.ExecutionResult result = Executor.runIR(targetCode);
 		Assert.assertEquals(4, result.exitCode);
 		Assert.assertEquals("4\n", result.output);
 	}
 
 	@Test
 	public void testCompileTAC() throws Exception {
 		ArrayList<Quadruple> tac = new ArrayList<>();
 
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_LONG, "#2", Quadruple.EmptyArgument, "longVar1"));
 		tac.add(new QuadrupleImpl(Quadruple.Operator.DECLARE_LONG, "#2", Quadruple.EmptyArgument, "longVar2"));
 
 		InputStream targetCodeIS = (InputStream) PA.invokeMethod(Executor.class, "compileTAC(java.util.List)", tac);
 		String targetCode = new java.util.Scanner(targetCodeIS).useDelimiter("\\A").next();
 		String expectedTargetCode = "  %longVar1 = alloca i64\n" +
 				"  store i64 2, i64* %longVar1\n" +
 				"  %longVar2 = alloca i64\n" +
 				"  store i64 2, i64* %longVar2\n" +
 				"  ret i64 0\n";
 
 		expectMain(expectedTargetCode, targetCode);
 	}
 
 	@Test
 	public void testReadTAC() throws Exception {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		PrintWriter out = new PrintWriter(os);
 		out.println("DECLARE_BOOLEAN|!|!|b");
 		out.println("DECLARE_BOOLEAN|!|!|rhs");
 		out.println("OR_BOOLEAN|#FALSE|#TRUE|b");
 		out.close();
 
		List<Quadruple> quadruples = (List<Quadruple>) PA.invokeMethod(Executor.class, "readTAC(java.io.InputStream)",
 				new ByteArrayInputStream(os.toByteArray()));
 
 		Quadruple[] expectedQuadruples = new Quadruple[3];
 		expectedQuadruples[0] = new QuadrupleImpl(Quadruple.Operator.DECLARE_BOOLEAN, Quadruple.EmptyArgument, Quadruple.EmptyArgument, "b");
 		expectedQuadruples[1] = new QuadrupleImpl(Quadruple.Operator.DECLARE_BOOLEAN, Quadruple.EmptyArgument, Quadruple.EmptyArgument, "rhs");
 		expectedQuadruples[2] = new QuadrupleImpl(Quadruple.Operator.OR_BOOLEAN, "#FALSE", "#TRUE", "b");
 
 		assertEquals(expectedQuadruples[0].toString(), quadruples.get(0).toString());
 		assertEquals(expectedQuadruples[1].toString(), quadruples.get(1).toString());
 		assertEquals(expectedQuadruples[2].toString(), quadruples.get(2).toString());
 	}
 
 	@Test
 	public void testRunTAC() throws Exception {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		PrintWriter out = new PrintWriter(os);
 		out.println("DECLARE_BOOLEAN|!|!|res");
 		out.println("DECLARE_STRING|!|!|res_str");
 		out.println("OR_BOOLEAN|#FALSE|#TRUE|res");
 		out.println("BOOLEAN_TO_STRING|res|!|res_str");
 		out.println("PRINT_STRING|res_str|!|!");
 		out.close();
 
 		Executor.ExecutionResult executionResult = Executor.runTAC(new ByteArrayInputStream(os.toByteArray()));
 
 		assertEquals("true\n", executionResult.output);
 	}
 
 }

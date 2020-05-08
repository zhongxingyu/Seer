 package test;
 
 import icode.Code;
 
 import java.io.File;
 import java.io.FileReader;
 
 import org.junit.Test;
 
 import parser.MiniT;
 import parser.Start;
 import semantic.Semantic;
 
 public class TestCodeGen extends TestCaseOutput {
 	private final static String DATA = "data/";
 	private final static String RESULT = "result/";
	private final static String RESULT_EXTENSION = ".res";
 
 	private void assertCorrectOutput(String testName) {
 		String fullName = testName;
 		genCode(DATA + fullName);
 		assertOutput(new File(RESULT + fullName + RESULT_EXTENSION));
 	}

 	@Test
	public void empty() {
		assertCorrectOutput("empty");
 	}
 
 	private static void genCode(String arg) {
 		try {
 			Code code = new Code();
 			MiniT parser = new MiniT(new FileReader(arg));
 			Start start = parser.start();
 			Semantic s = new Semantic();
 			start.nameAnalysis(s);
 			if (s.numErrors() > 0) {
 				s.printAnalysis();
 				return;
 			}
 			start.typeAnalysis(s);
 			if (s.numErrors() > 0) {
 				s.printAnalysis();
 				return;
 			}
 			start.genCode(code, s);
 			System.out.println(code);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println(e.getMessage());
 		}
 	}
 
 	public static void main(String[] args) {
 		genCode(args[0]);
 	}
 }

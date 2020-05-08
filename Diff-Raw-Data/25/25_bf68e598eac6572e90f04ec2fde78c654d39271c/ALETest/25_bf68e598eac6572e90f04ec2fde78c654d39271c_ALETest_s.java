 package jworldgen.test.unitTests;
 
 import static org.junit.Assert.*;
 import jworldgen.exceptionHandler.CriticalFailure;
 import jworldgen.parser.RuleParser;
 import jworldgen.parser.parseStructure.ParseALE;
 
 import org.junit.Test;
 
 public class ALETest {
 
	private static String[] ales ={"2 + 3","3 * 4 + 5 - 6","0*sin(3)+1*cos(0)-tan(0) > 0.9 && 0*sin(3)+1*cos(0)-tan(0) < 1.1"};
	private static Number[] results ={5,11,1};
 	@Test
 	public void testALEs() throws CriticalFailure {
 		for (int i = 0; i < ales.length; i++)
 		{
 			ParseALE expression;
 			expression = RuleParser.parseALE(ales[i]+";");
 			Number result = expression.evaluate(null, null);
 			System.out.println(ales[i]+" = "+result);
 			assertTrue(result == results[i]);
 		}
 	}
 }

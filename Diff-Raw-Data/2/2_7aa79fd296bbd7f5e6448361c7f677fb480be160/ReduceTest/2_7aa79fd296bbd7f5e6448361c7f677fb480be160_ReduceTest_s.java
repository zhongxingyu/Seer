 package swp_compiler_ss13.fuc.parser.parseTableGenerator.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Production;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Variable;
 import swp_compiler_ss13.fuc.parser.table.ActionEntry;
 import swp_compiler_ss13.fuc.parser.table.actions.Reduce;
 
 public class ReduceTest {
 	// Some data needed for the entries 
 	private static Reduce reduce;
 	private static Production production;
 	private static Variable variable;
 	private static ActionEntry parserTabEntry;
 	@BeforeClass
 	public static void setUpTest(){
 	variable = new Variable("Test");
	production= new Production(variable, null);
 	reduce = new Reduce(production);
 	}
 	
 	/* Begin test cases*/
 	@Test
 	public void testGetCount() {		
 		assertEquals((Integer)10, reduce);
 		
 	}
 	@Test
 	public void testGetType(){
 		assertEquals(parserTabEntry, reduce.getType());
 		
 	}
 	@Test
 	public void testGetProduction(){
 		assertEquals(production, reduce.getProduction());
 	}
 
 }

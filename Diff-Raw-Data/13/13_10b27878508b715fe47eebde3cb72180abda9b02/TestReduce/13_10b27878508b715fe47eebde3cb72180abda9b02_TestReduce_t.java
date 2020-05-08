package swp_compiler_ss13.fuc.parser.parseTableGenerator.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;

import swp_compiler_ss13.fuc.parser.parseTableGenerator.ParseTableEntry;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Production;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.Reduce;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Variable;
 
 public class TestReduce {
 	// Some data needed for the entries 
 	private static Reduce reduce;
 	private static Production production;
 	private static Variable variable;
 	private static ParseTableEntry parserTabEntry;
 	@BeforeClass
 	public static void setUpTest(){
 	variable = new Variable("Test");
 	production= new Production(variable, null);
 	reduce = new Reduce(10, 1, production);
 	parserTabEntry = new ParseTableEntry() {
 		
 		@Override
 		public ParseTableEntryType getType() {
 			// TODO Auto-generated method stub
 			return ParseTableEntryType.REDUCE;
 		}
 	};
 	}
 	
 	/* Begin test cases*/
 	@Test
 	public void testGetCount() {		
 		assertEquals((Integer)10, reduce.getCount());
 		
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

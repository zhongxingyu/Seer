 package org.vamdc.portal.session.queryBuilder.fields;
 
 
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.vamdc.dictionary.Restrictable;
 
 public class AbstractFieldTest {
 	
 	private AbstractField field;
 	
 	@Test
 	public void getSingleQuery() {
 		assertQueryEquals("Fe",KEYWORD+" = 'Fe'");
 	}
 	
 	@Test
 	public void trimSingleQuery(){
 		assertQueryEquals("Fe ",KEYWORD+" = 'Fe'");
 	}
 	
 	@Test
 	public void trimEmptyValue(){
 		assertQueryEquals(" ","");
 	}
 	
 	@Test
 	public void testLikeQuery(){
 		assertQueryEquals("Fe%",KEYWORD+" LIKE 'Fe%'");
 	}
 	
 	@Test
 	public void testLikeStarQuery(){
 		assertQueryEquals("Fe*",KEYWORD+" LIKE 'Fe%'");
 	}
 	
 	@Test
 	public void getInQuery(){
 		assertQueryEquals("Fe, Co",KEYWORD+" IN ('Fe','Co')");
 	}
 	
 	@Test
 	public void testNullKeyword() {
 		field = new SimpleField(null, "Atom Symbol");
 		field.setValue("Fe");
 		assertFalse(field.hasValue());
 		assertQueryEquals("Fe","");
 	}
 
 	private final static String KEYWORD = "AtomSymbol";
 	@Before
 	public void setupTest(){
 		field = new SimpleField(Restrictable.AtomSymbol, null);
 	}
 	
 	@After
 	public void clearTest(){
 		field = null;
 	}
 	
 	private void assertQueryEquals(String parameter, String supposedResult){
 		field.setValue(parameter);
 		String query = field.getQuery();
		System.out.println("Equals?");
		System.out.println(query);
		System.out.println(supposedResult);
 		assertTrue(query.equals(supposedResult));
 	}
 	
 }

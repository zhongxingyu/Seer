 package com.supersecure.pl;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.supersecure.pl.Database;
 
 public class DatabaseTest {
 
 	private Database database;
 
 	@Before
 	public void setUp() {
 		database = new Database();
 	}
 
 	@Test
	public void find_Similar_Named_Languages_For_Java_Is_Not_JavaScript() {
 
		database.add("Java");
 		database.add("JavaScript");
 		String[] similarLanguageNames = database
 				.findSimilarNamedLanguages("Java");
 
		assertEquals("Java", similarLanguageNames[0]);
 	}
 
 	@Test
 	public void find_Similar_Named_Languages_For_C() {
 
 		database.add("C++");
 		database.add("C");
 		database.add("C#");
 		String[] similarLanguageNames = database
				.findSimilarNamedLanguages("C");
 
 		assertArrayContains(similarLanguageNames, "C");
 		assertArrayContains(similarLanguageNames, "C#");
 	}
 
 	@Test
 	public void find_Similar_Named_Languages_For_Lisp() {
 
 		database.add("Lisp");
		database.add("ELisp");
 		String[] similarLanguageNames = database
 				.findSimilarNamedLanguages("Lisp");
 
		assertArrayContains(similarLanguageNames, "ELisp");
 	}
 
 	@Test
 	public void find_Similar_Named_Languages_For_Aga() {
 
 		database.add("Ada");
 		database.add("Agora");
 		String[] similarLanguageNames = database
 				.findSimilarNamedLanguages("Ado");
 
 		assertArrayContains(similarLanguageNames, "Ada");
 	}
 	
 
 	@Test
 	public void find_Similar_Named_Languages_For_Ado() {
 
 		database.add("Ada");
 		database.add("Agora");
 		String[] similarLanguageNames = database
 				.findSimilarNamedLanguages("Ado");
 
 		assertArrayContains(similarLanguageNames, "Ada");
 	}
 
 	
 	private void assertArrayContains(Object[] array, Object... objects) {
 		boolean contains = false;
 		for (Object object : objects) {
 			for (Object item : array) {
 				contains |= object.equals(item);
 			}
 			assertTrue(contains);
 			contains = false;
 		}
 	}
 
 }

 package differ;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
import differ.filediffer.diffObjectResult;
 import differ.filediffer.methodResult;
 
 public class testGetChangedMethod {
 
 	@Test
 	public void testGetChangedMethods_declaration() {
 		filediffer mydiffer = new filediffer("emptyText", "emptyText");
 		String input =  "private void privateFunction(int haha, custom hoho){\n}\n" +
 						"protect void protectFunction(){\n}\n" +
 						"public void publicFunction(int haha, Map<String> strings);\n" +
 						"impliedFunction(	int   haha );\n";
 		
 		ArrayList<methodResult> results = mydiffer.parseMethods(input);
 		assertEquals(results.size(), 4);
 		
 		assertEquals(results.get(0).className, "privateFunction");
 		assertEquals(results.get(0).signature, "int haha, custom hoho");
 		assertEquals(results.get(0).start, 0);
 		//TODO update the end test cuz it doesn't contain the whole function yet
 		assertEquals(results.get(0).end, 51);
 		
 		assertEquals(results.get(1).className, "protectFunction");
 		assertEquals(results.get(1).signature, "");
 		assertEquals(results.get(1).start, 54);
 		assertEquals(results.get(1).end, 85);
 		
 		assertEquals(results.get(2).className, "publicFunction");
 		assertEquals(results.get(2).signature, "int haha, Map<String> strings");
 		assertEquals(results.get(2).start, 88);
 		assertEquals(results.get(2).end, 146);
 		
 		assertEquals(results.get(3).className, "impliedFunction");
 		assertEquals(results.get(3).signature, "	int   haha ");
 		assertEquals(results.get(3).start, 147);
 		assertEquals(results.get(3).end, 177);
 	}
 	
 	/////////////////////////////////////////////////////////
 	// test location of the function
 	@Test
 	public void testGetChangedMethods_location() {
 		filediffer mydiffer = new filediffer("emptyText", "emptyText");
 		String input =  "	private void privatefunction(int haha, custom hoho){\n"+
 						"		int a = 5;\n"+
 						"		class new class { int a = 5;}\n"+
 						"	}\n";
 		
 		ArrayList<methodResult> results = mydiffer.parseMethods(input);
 		assertEquals(results.size(), 1);
 	}
 	
 	@Test
 	public void testParseFunctionParameters_positive() {
 		filediffer mydiffer = new filediffer("emptyText", "emptyText");
 		String input =  "int a, double b, Map<String> myMap";
 		
 		ArrayList<String> paras = mydiffer.parseFunctionParameters(input);
 		assertEquals(paras.size(), 3);
 		assertEquals(paras.get(0), "int");
 		assertEquals(paras.get(1), "double");
 		assertEquals(paras.get(2), "Map<String>");
 	}
 	
 	@Test
 	public void testDiffFilesLineMode() {
 		String oldText = "class A;\n public funtion A(); private int a;";
 		String newText = "class A;\n package haha;\n private int a;\n private function B();";
 		
 		filediffer mydiffer = new filediffer(oldText, newText);
 		mydiffer.diffFilesLineMode();
 		
 		List<diffObjectResult> deleteObjects = mydiffer.getDeleteObjects();
 		List<diffObjectResult> insertObjects = mydiffer.getInsertObjects();
 		
 		assertEquals(deleteObjects.size(), 1);
 		assertEquals(insertObjects.size(), 1);
 	}
 
 }

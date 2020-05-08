 package scanner;
 
 public class StringCharTests extends AutomatonTests {
 	public void testString() throws Exception {
 		assertTokens(token("STRING","astring 1.23.4 123"), "\"astring 1.23.4 123\"");
 	}
 	
 	public void testStringAndNumber() throws Exception {
 		setLog(true);
 		assertTokens(token("INT","123").and("STRING","astring"),"123\"astring\"");
 		setLog(false);
 	}
 	
 	public void testStringAndIdent() throws Exception {
 		assertTokens(token("INT","123").and("SEP",null).and("ID","qwe12").and("STRING","otro"), "123 qwe12\"otro\"");
 	}
 	
 	public void testChar() throws Exception {
 		assertTokens(token("CHAR","a"),"'a'");
 	}
 	public void testCharError() throws Exception {
		assertTokens(token("CHAR","a"),"'ab'");		
 	}
 }

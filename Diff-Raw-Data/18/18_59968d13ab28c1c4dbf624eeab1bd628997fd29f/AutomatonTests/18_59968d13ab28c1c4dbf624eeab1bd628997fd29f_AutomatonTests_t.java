 package scanner;
 import htmlscanner.Automaton;
 import htmlscanner.Token;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 import junit.framework.TestCase;
 
 
 public class AutomatonTests extends TestCase {
 	public void testEmtpy() throws Exception {
 		ScanResult result = scan("");
 		assertTrue(result.getTokens().isEmpty());
 		assertTrue(result.getErrors().isEmpty());
 	}
 	
 	public void testIdent() throws Exception {
 		
 		ScanResult result = scan("a10 bbb c001");
 		List<Token> tokens = Arrays.asList(new Token("ID","a10"), new Token("SEP",null),
 				new Token("ID","bbb"), new Token("SEP",null),
 				new Token("ID","c001"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testInt() throws Exception {
 		ScanResult result = scan("10 00 101");
 		List<Token> tokens = Arrays.asList(new Token("INT","10"), new Token("SEP",null),
 				new Token("INT","00"), new Token("SEP",null),
 				new Token("INT","101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testReal() throws Exception {
 		ScanResult result = scan("10 0.01 101");
 		List<Token> tokens = Arrays.asList(new Token("INT","10"), new Token("SEP",null),
 				new Token("REAL","0.01"), new Token("SEP",null),
 				new Token("INT","101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testIntAndIdent() throws Exception {
 		String input = "10a";
 		ScanResult result = scan(input);
		printResult(result);
 		List<Token> tokens = Arrays.asList(new Token("INT","10"), new Token("ID","a"));
 		assertTrue(isEqual(tokens,result.tokens));
 		assertTrue(result.errors.isEmpty());
 		
 	}
 
	private void printResult(ScanResult result) {
		System.out.println(result.tokens);
		System.out.println(result.errors);
	}
	
	public void testAllLetters() throws Exception {
		String allLetters = "abcdefghijklmnopqrstuvwxyz";
		allLetters += allLetters.toUpperCase();
		ScanResult result = scan(allLetters);
		printResult(result);
		List<Token> expected =Arrays.asList(new Token("ID",allLetters));
		assertTrue(isEqual(expected ,result.tokens));
	}

 	private ScanResult scan(String input) {
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		
 		ScanResult result = au.scan(input);
 		return result;
 	}
 
 	public static boolean isEqual(List<Token> tokens, List<Token> tokens2) {
 		Preconditions.checkNotNull(tokens);
 		Preconditions.checkNotNull(tokens2);
 		
 		if(tokens.isEmpty() && tokens2.isEmpty()){
 			return true;
 		}
 		
 		if(tokens.size() != tokens2.size()){
 			return false;
 		}
 		
 		for(int i = 0; i< tokens.size(); i++){
 			if(!(tokens.get(i).equals(tokens2.get(i)))){
 				return false;
 			}
 		}
 		return true;
 	}
 }
